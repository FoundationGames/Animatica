package io.github.foundationgames.animatica.animation;

import io.github.foundationgames.animatica.Animatica;
import io.github.foundationgames.animatica.animation.bakery.AnimationBakery;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.util.List;

public class BakedTextureAnimation {
    private static final Identifier EMPTY = new Identifier("empty");

    private final Identifier[] frames;

    private BakedTextureAnimation(Identifier[] frames) {
        this.frames = frames;
    }

    public static BakedTextureAnimation bake(ResourceManager resources, Identifier targetId, List<AnimationMeta> anims) {
        try {
            var bakery = new AnimationBakery(resources, targetId, anims);
            return new BakedTextureAnimation(bakery.bakeAndUpload());
        } catch (IOException e) { Animatica.LOG.error(e); }
        return new BakedTextureAnimation(new Identifier[] {EMPTY});
    }

    public Identifier getTextureForFrame() {
        if (frames.length <= 0) {
            return EMPTY;
        }
        return frames[(int)(Animatica.getTime() % frames.length)];
    }
}
