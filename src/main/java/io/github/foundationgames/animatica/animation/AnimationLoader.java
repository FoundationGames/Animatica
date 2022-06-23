package io.github.foundationgames.animatica.animation;

import io.github.foundationgames.animatica.Animatica;
import io.github.foundationgames.animatica.util.Flags;
import io.github.foundationgames.animatica.util.Utilities;
import io.github.foundationgames.animatica.util.exception.PropertyParseException;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Consumer;

public final class AnimationLoader implements SimpleSynchronousResourceReloadListener {
    public static final String[] ANIM_PATHS = {
            "animatica/anim",
            "mcpatcher/anim",
            "optifine/anim"
    };
    private static final Identifier ID = Animatica.id("animation_storage");

    public static final AnimationLoader INSTANCE = new AnimationLoader();

    private final Map<Identifier, BakedTextureAnimation> animatedTextures = new HashMap<>();

    private AnimationLoader() {
    }

    private static void findAllMCPAnimations(ResourceManager manager, BiConsumer<Identifier, Resource> action) {
        for (var path : ANIM_PATHS) {
            manager.findResources(path, p -> p.getPath().endsWith(".properties")).forEach(action);
        }
    }

    public BakedTextureAnimation getAnimation(Identifier id) {
        return animatedTextures.get(id);
    }

    @Override
    public Identifier getFabricId() {
        return ID;
    }

    @Override
    public void reload(ResourceManager manager) {
        if (!Animatica.CONFIG.animatedTextures) {
            this.animatedTextures.clear();
            return;
        }

        Flags.ALLOW_INVALID_ID_CHARS = true;

        this.animatedTextures.clear();
        var animations = new HashMap<Identifier, List<AnimationMeta>>();

        findAllMCPAnimations(manager, (id, resource) -> {
            try {
                try (var resourceInputStream = resource.getInputStream()) {
                    var ppt = new Properties();
                    ppt.load(resourceInputStream);

                    var anim = AnimationMeta.of(id, ppt);

                    var targetId = anim.target();
                    if (!animations.containsKey(targetId)) animations.put(targetId, new ArrayList<>());
                    animations.get(targetId).add(anim);
                }
            } catch (IOException | PropertyParseException e) {
                Animatica.LOG.error(e.getMessage());
            }
        });

        int[] totalSize = {0};

        for (var targetId : animations.keySet()) {
            if (Animatica.CONFIG.safeMode) {
                try {
                    debugAnimation(totalSize, manager, targetId, animations.get(targetId));
                } catch (IOException e) {
                    Animatica.LOG.error("Error printing Safe Mode debug for animation {}\n {}: {}", targetId, e.getClass().getName(), e.getMessage());
                }
            } else this.animatedTextures.put(targetId, BakedTextureAnimation.bake(manager, targetId, animations.get(targetId)));
        }

        if (Animatica.CONFIG.safeMode) {
            Animatica.LOG.info("=== ESTIMATED TOTAL ANIMATION SIZE: {} BYTES ===", totalSize[0]);
        }

        Flags.ALLOW_INVALID_ID_CHARS = false;
    }

    public static void debugAnimation(int[] totalSize, ResourceManager manager, Identifier targetTex, List<AnimationMeta> anims) throws IOException {
        int[] frameCounts = new int[anims.size()];
        int frameWidth;
        int frameHeight;
        int bytesPerPix;

        try (var target = manager.getResource(targetTex).getInputStream()) {
            try (var img = NativeImage.read(target)) {
                frameWidth = img.getWidth();
                frameHeight = img.getHeight();
                bytesPerPix = img.getFormat().getChannelCount();
            }
        }

        for (int i = 0; i < anims.size(); i++) {
            var meta = anims.get(i);

            try (var source = manager.getResource(meta.source()).getInputStream()) {
                var tex = NativeImage.read(source);
                frameCounts[i] = Math.max((int)Math.floor((float) tex.getHeight() / meta.height()), meta.getGreatestUsedFrame() + 1);
            }
        }

        int frameCount = Utilities.lcm(frameCounts);
        int animSizeEstimate = frameWidth * frameHeight * bytesPerPix * frameCount;

        totalSize[0] += animSizeEstimate;

        Animatica.LOG.info("--- ANIMATION DEBUG FOR TEXTURE '{}' ---", targetTex.toString());
        Animatica.LOG.info(" - Total Compiled Frame Count: {}", frameCount);
        Animatica.LOG.info(" - Frame Dimensions: {}px by {}px", frameWidth, frameHeight);
        Animatica.LOG.info(" - Estimated Animation Size: {} BYTES", animSizeEstimate);
    }
}
