package io.github.foundationgames.animatica.config;

import io.github.foundationgames.animatica.Animatica;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.SimpleOption;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class AnimaticaConfig {
    public static String ANIMATED_TEXTURES_KEY = "animated_textures";

    public static final String FILE_NAME = "animatica.properties";

    private final SimpleOption<Boolean> animatedTexturesOption;
    public boolean animatedTextures;

    public AnimaticaConfig() {
        try {
            load();
        } catch (IOException e) {
            Animatica.LOG.error("Error loading config during initialization!", e);
        }

        this.animatedTexturesOption = SimpleOption.ofBoolean(
                "option.animatica.animated_textures",
                this.animatedTextures,
                value -> {
                    this.animatedTextures = value;
                    try {
                        this.save();
                    } catch (IOException e) { Animatica.LOG.error("Error saving config while changing in game!", e); }
                    MinecraftClient.getInstance().reloadResources();
                }
        );
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

    public SimpleOption<Boolean> getAnimatedTexturesOption() {
        return animatedTexturesOption;
    }

    public void save() throws IOException {
        var file = getFile();
        var properties = new Properties();

        writeTo(properties);

        try (var out = Files.newOutputStream(file)) {
            properties.store(out, "Configuration file for Animatica");
        }
    }

    public void load() throws IOException {
        var file = getFile();
        var properties = new Properties();

        try (var in = Files.newInputStream(file)) {
            properties.load(in);
        }

        readFrom(properties);
    }

    private static boolean boolFrom(String s, boolean defaultVal) {
        return s == null ? defaultVal : "true".equals(s);
    }
}
