package io.github.foundationgames.animatica.animation.bakery;

import com.google.common.collect.ImmutableList;
import io.github.foundationgames.animatica.Animatica;
import io.github.foundationgames.animatica.animation.AnimationMeta;
import io.github.foundationgames.animatica.util.TextureUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class AnimationBakery implements AutoCloseable {
    public final Baking[] anims;
    private final NativeImage target;
    private int frame = 0;
    private final Deque<Identifier> frameIds = new ArrayDeque<>();
    private final Identifier targetTexId;

    public AnimationBakery(ResourceManager resources, Identifier targetTex, List<AnimationMeta> metas) throws IOException {
        this.anims = new Baking[metas.size()];
        for (int i = 0; i < metas.size(); i++) {
            this.anims[i] = new Baking(metas.get(i), resources);
        }

        try (var target = resources.getResourceOrThrow(targetTex).getInputStream()) {
            this.target = NativeImage.read(target);
        }

        this.targetTexId = targetTex;
    }

    public boolean hasNext() {
        for (var anim : anims) {
            if (!anim.isOnFrameZero()) {
                return true;
            }
        }
        return false;
    }

    public void advance() {
        var textures = MinecraftClient.getInstance().getTextureManager();

        boolean changed = frame <= 0;
        for (var anim : anims) {
            if (anim.isChanged()) {
                changed = true;
                break;
            }
        }

        if (changed) {
            var frameImg = new NativeImage(target.getFormat(), target.getWidth(), target.getHeight(), false);
            frameImg.copyFrom(target);

            Phase phase;
            for (var anim : anims) {
                phase = anim.getCurrentPhase();
                if (phase instanceof InterpolatedPhase iPhase) {
                    TextureUtil.blendCopy(anim.sourceTexture, 0, iPhase.prevV, 0, iPhase.v, anim.width, anim.height, frameImg, anim.targetX, anim.targetY, iPhase.blend.getBlend(anim.getPhaseFrame()));
                } else {
                    TextureUtil.copy(anim.sourceTexture, 0, phase.v, anim.width, anim.height, frameImg, anim.targetX, anim.targetY);
                }
            }

            var id = new Identifier(targetTexId.getNamespace(), targetTexId.getPath() + ".anim" + frameIds.size());
            textures.registerTexture(id, new NativeImageBackedTexture(frameImg));
            frameIds.addLast(id);
        } else {
            frameIds.addLast(frameIds.getLast());
        }

        for (var anim : anims) anim.advance();
        frame++;
    }

    public Identifier[] bakeAndUpload() {
        int i = -1;
        do {
            advance();
            i++;
        } while (hasNext() && (Animatica.CONFIG.maxAnimFrames == null || i < Animatica.CONFIG.maxAnimFrames));

        var ids = new Identifier[frameIds.size()];
        frameIds.toArray(ids);
        return ids;
    }

    @Override
    public void close() {
        for (var anim : anims) anim.close();

        this.target.close();
    }

    // Used to construct all phases of an animation and progress through them as the animation is baked
    public static class Baking implements AutoCloseable {
        private final List<Phase> phases;
        public final NativeImage sourceTexture;
        public final int targetX;
        public final int targetY;
        public final int width;
        public final int height;
        private final int duration;

        private int frame = 0;
        private Phase currentPhase = null;
        private int phaseFrame = 0;
        private boolean changed = true;

        // Assembles all animation phases for one texture animation being baked
        public Baking(AnimationMeta meta, ResourceManager resources) throws IOException {
            this.targetX = meta.targetX();
            this.targetY = meta.targetY();
            this.width = meta.width();
            this.height = meta.height();

            try (var source = resources.getResourceOrThrow(meta.source()).getInputStream()) {
                this.sourceTexture = NativeImage.read(source);
            }

            var phases = ImmutableList.<Phase>builder();
            int duration = 0;

            final int textureFrameCount = (int)Math.floor((float) sourceTexture.getHeight() / meta.height());
            final int animFrameCount = Math.max(textureFrameCount, meta.getGreatestUsedFrame() + 1);

            // The int array stored for each frame must contain the frame mapping and duration
            List<int[]> frames = new ArrayList<>();
            for (int f = 0; f < animFrameCount; f++) {
                if (f >= textureFrameCount && !meta.frameMapping().containsKey(f)) {
                    continue;
                }

                frames.add(new int[] {
                        meta.frameMapping().getOrDefault(f, f),
                        meta.frameDurations().getOrDefault(f, meta.defaultFrameDuration())
                });
            }

            for (int i = 0; i < frames.size(); i++) {
                int[] frame = frames.get(i);

                int fMap = frame[0];
                int fDuration = frame[1];

                int v = getVForFrame(fMap, textureFrameCount);
                int nextV = getVForFrame(frames.get(Math.floorMod(i + 1, frames.size()))[0], textureFrameCount);

                if (meta.interpolate()) {
                    if (meta.interpolationDelay() > 0) {
                        // Adds a static version of the current phase as a "delay" before the next interpolated phase (if specified in animation)
                        phases.add(new Phase(meta.interpolationDelay(), v));
                        duration += meta.interpolationDelay();
                    }

                    // Add interpolated animation phase
                    final int interpolatedDuration = fDuration - meta.interpolationDelay();
                    phases.add(new InterpolatedPhase(interpolatedDuration, v, nextV, (phaseFrame) -> ((float) phaseFrame / interpolatedDuration)));
                    duration += interpolatedDuration;
                } else {
                    phases.add(new Phase(fDuration, v));
                    duration += fDuration;
                }
            }

            this.duration = duration;
            this.phases = phases.build();

            updateCurrentPhase();
        }

        public void updateCurrentPhase() {
            changed = false;
            int progress = frame;

            for (var phase : phases) {
                progress -= phase.duration; // Take away as much progress as each phase is long, until progress is below zero
                if (progress < 0) {
                    if (currentPhase != phase) {
                        // Marks baking anim as changed should it be in a new, unique phase
                        changed = true;
                    }
                    if (phase instanceof InterpolatedPhase iPhase) changed = iPhase.hasChangingV(); // Marks baking anim as changed should its current phase be changing

                    this.currentPhase = phase;
                    this.phaseFrame = phase.duration + progress; // Adding progress to the phase duration results in how far it is into the phase

                    return;
                }
            }
        }

        public Phase getCurrentPhase() {
            return currentPhase;
        }

        public int getPhaseFrame() {
            return phaseFrame;
        }

        public boolean isOnFrameZero() {
            return frame <= 0;
        }

        public boolean isChanged() {
            return changed;
        }

        public void advance() {
            frame++;
            if (frame >= duration) {
                frame = 0;
            }
            updateCurrentPhase();
        }

        @Override
        public void close() {
            this.sourceTexture.close();
        }

        private int getVForFrame(int frame, int textureFrameCount) {
            return MathHelper.clamp(frame * this.height, 0, (textureFrameCount - 1) * this.height);
        }
    }

    // Represents a phase that an animation is in (loosely defined by the animation file's "tile"s)
    // Base class represents the simplest possible type of animation phase, which only needs to generate
    // one texture to be used over a period of time
    public static class Phase {
        public final int duration;
        public final int v;

        public Phase(int duration, int v) {
            this.duration = duration;
            this.v = v;
        }

        @Override
        public String toString() {
            return "Animation Bakery Phase { v: "+this.v+" }";
        }
    }

    // A phase that blends between its previous phase and itself, requiring the generation of many
    // more textures to construct the blend animation
    public static class InterpolatedPhase extends Phase {
        public final int prevV;
        public final BlendInterpolator blend;

        public InterpolatedPhase(int duration, int v1, int v2, BlendInterpolator blend) {
            super(duration, v2);
            this.prevV = v1;
            this.blend = blend;
        }

        public boolean hasChangingV() {
            return this.prevV != this.v;
        }
    }

    @FunctionalInterface
    public interface BlendInterpolator {
        float getBlend(int phaseFrame);
    }
}
