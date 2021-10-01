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

    public static String ANIMATED_TEXTURES_KEY = "animated_textures";

    public static final String FILE_NAME = "animatica.properties";
    public static final String COMMENT = "Configuration file for Animatica";

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
    }

    public void readFrom(Properties properties) {
        this.animatedTextures = boolFrom(properties.getProperty(ANIMATED_TEXTURES_KEY), true);
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
            ppt.store(os, COMMENT);
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
}
