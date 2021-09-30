package io.github.foundationgames.animatica.animation;

import io.github.foundationgames.animatica.Animatica;
import io.github.foundationgames.animatica.util.Flags;
import io.github.foundationgames.animatica.util.exception.PropertyParseException;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.util.*;
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

    private static void iterateAnimResources(ResourceManager manager, Consumer<Identifier> action) {
        for (var path : ANIM_PATHS) {
            manager.findResources(path, p -> p.endsWith(".properties")).forEach(action);
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
        Flags.ALLOW_INVALID_ID_CHARS = true;

        this.animatedTextures.clear();
        var animations = new HashMap<Identifier, List<AnimationMeta>>();

        iterateAnimResources(manager, id -> {
            // Animatica.LOG.info("Loading animation "+id);
            try {
                try (var resource = manager.getResource(id).getInputStream()) {
                    var ppt = new Properties();
                    ppt.load(resource);

                    var anim = AnimationMeta.of(id, ppt);

                    var targetId = anim.target();
                    if (!animations.containsKey(targetId)) animations.put(targetId, new ArrayList<>());
                    animations.get(targetId).add(anim);
                }
            } catch (IOException | PropertyParseException e) {
                Animatica.LOG.error(e.getMessage());
            }
        });
        for (var targetId : animations.keySet()) {
            this.animatedTextures.put(targetId, BakedTextureAnimation.bake(manager, targetId, animations.get(targetId)));
        }

        Flags.ALLOW_INVALID_ID_CHARS = false;
    }
}
