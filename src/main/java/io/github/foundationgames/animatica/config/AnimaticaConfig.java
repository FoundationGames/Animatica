package io.github.foundationgames.animatica.config;

import io.github.foundationgames.animatica.Animatica;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.CyclingOption;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class AnimaticaConfig {
    public boolean animatedTextures;
    public Integer maxAnimFrames;

    public static String ANIMATED_TEXTURES_KEY = "animated_textures";
    public static String MAX_ANIM_FRAMES_KEY = "max_animation_frames";

    public static final String FILE_NAME = "animatica.properties";
    public static final String[] COMMENTS = {
            "Configuration file for Animatica",
            "animated_textures=<true|false> - Determines whether custom texture animation support should be enabled or not",
            "max_animation_frames=<integer value, or 'none'> - Maximum unique animation frames a texture can have, to prevent high RAM/VRAM usage (disabled when set to 'none')"
    };

    public static final CyclingOption<Boolean> ANIMATED_TEXTURES_OPTION = CyclingOption.create("option.animatica.animated_textures", opts -> {
        try {
            Animatica.CONFIG.load();
        } catch (IOException e) { Animatica.LOG.error("Error loading config for options screen!", e); }
        return Animatica.CONFIG.animatedTextures;
    }, (opts, option, value) -> {
        Animatica.CONFIG.animatedTextures = value;
        try {
            Animatica.CONFIG.save();
        } catch (IOException e) { Animatica.LOG.error("Error saving config while changing in game!", e); }
        MinecraftClient.getInstance().reloadResources();
    });

    public AnimaticaConfig() {
        try {
            load();
        } catch (IOException e) {
            Animatica.LOG.error("Error loading config during initialization!", e);
        }
    }

    public void writeTo(Properties properties) {
        properties.put(ANIMATED_TEXTURES_KEY, Boolean.toString(animatedTextures));
        properties.put(MAX_ANIM_FRAMES_KEY, maxAnimFrames == null ? "none" : maxAnimFrames.toString());
    }

    public void readFrom(Properties properties) {
        this.animatedTextures = boolFrom(properties.getProperty(ANIMATED_TEXTURES_KEY), true);
        this.maxAnimFrames = nullableIntFrom(properties.getProperty(MAX_ANIM_FRAMES_KEY), 8000);
    }

    public Path getFile() throws IOException {
        var file = FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
        if (!Files.exists(file)) {
            Files.createFile(file);
        }

        return file;
    }

    public void save() throws IOException {
        var file = getFile();

        var ppt = new Properties();
        writeTo(ppt);

        try (var os = Files.newOutputStream(file)) {
            ppt.store(os, String.join("\n", COMMENTS));
        }
    }

    public void load() throws IOException {
        var file = getFile();

        var ppt = new Properties();

        try (var is = Files.newInputStream(file)) {
            ppt.load(is);
        }

        readFrom(ppt);
    }

    private static boolean boolFrom(String s, boolean defaultVal) {
        return s == null ? defaultVal : "true".equals(s);
    }

    private static Integer nullableIntFrom(String s, Integer defaultVal) {
        try {
            return s == null ? defaultVal : (s.equals("none") ? null : Integer.parseInt(s));
        } catch (NumberFormatException ex) {
            Animatica.LOG.error("Value {} must be an integer, or 'none'", s);
        }
        return defaultVal;
    }
}
