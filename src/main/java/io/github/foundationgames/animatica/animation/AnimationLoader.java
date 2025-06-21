package io.github.foundationgames.animatica.animation;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.foundationgames.animatica.Animatica;
import io.github.foundationgames.animatica.util.Flags;
import io.github.foundationgames.animatica.util.exception.PropertyParseException;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.function.BiConsumer;

public final class AnimationLoader implements SimpleSynchronousResourceReloadListener {
    public static final String[] ANIM_PATHS = {
            "animatica/anim",
            "mcpatcher/anim",
            "optifine/anim"
    };
    private static final Identifier ID = Animatica.id("animation_storage");

    public static final AnimationLoader INSTANCE = new AnimationLoader();

    private final Int2IntMap animationIds = new Int2IntOpenHashMap();
    private final Set<AnimatedTexture> animatedTextures = new HashSet<>();

    private AnimationLoader() {
    }

    private static void findAllMCPAnimations(ResourceManager manager, BiConsumer<Identifier, Resource> action) {
        for (var path : ANIM_PATHS) {
            manager.findResources(path, p -> p.getPath().endsWith(".properties")).forEach(action);
        }
    }

    public boolean isAnimated(int glId) {
        return animationIds.containsKey(glId);
    }

    public int getAnimationId(int glId) {
        return animationIds.get(glId);
    }

    public void tickTextures() {
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(this::tickTextures);
        } else {
            for (var texture : animatedTextures) {
                texture.tick();
            }
        }
    }

    @Override
    public Identifier getFabricId() {
        return ID;
    }

    @Override
    public void reload(ResourceManager manager) {
        this.animatedTextures.clear();
        this.animationIds.clear();

        if (!Animatica.CONFIG.animatedTextures) {
            return;
        }

        Flags.ALLOW_INVALID_ID_CHARS = true;

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

        for (var targetId : animations.keySet()) {
            AnimatedTexture.tryCreate(manager, targetId, animations.get(targetId))
                    .ifPresent(tex -> {
                        var textures = MinecraftClient.getInstance().getTextureManager();
                        var tgt = textures.getTexture(targetId);

                        if (tgt != null) {
                            textures.registerTexture(
                                    Identifier.of(targetId.getNamespace(), targetId.getPath() + "-anim"), tex);

                            this.animatedTextures.add(tex);
                            this.animationIds.put(tgt.getGlId(), tex.getGlId());
                        }
                    });
        }

        Flags.ALLOW_INVALID_ID_CHARS = false;
    }
}
