package io.github.foundationgames.animatica.util;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import io.github.foundationgames.animatica.util.exception.InvalidPropertyException;
import io.github.foundationgames.animatica.util.exception.MissingPropertyException;
import io.github.foundationgames.animatica.util.exception.PropertyParseException;
import net.minecraft.util.Identifier;

import java.util.Map;
import java.util.Properties;

public enum PropertyUtil {;
    public static String get(Identifier file, Properties properties, String key) throws PropertyParseException {
        var p = properties.getProperty(key);
        if (p == null) {
            throw new MissingPropertyException(file, key);
        }
        return p;
    }

    public static Properties getSubProperties(Properties properties, String key) {
        var p = new Properties();
        final var prefix = key + ".";
        for (String k : properties.stringPropertyNames()) {
            if (k.startsWith(prefix)) {
                var newKey = k.replaceFirst(prefix, "");
                p.setProperty(newKey, properties.getProperty(k));
            }
        }
        return p;
    }

    public static int getInt(Identifier file, Properties properties, String key) throws PropertyParseException {
        int r;
        try {
            r = Integer.parseInt(get(file, properties, key));
        } catch (NumberFormatException ignored) {
            throw new InvalidPropertyException(file, key, "integer (whole number)");
        }
        return r;
    }

    public static boolean getBool(Identifier file, Properties properties, String key) throws PropertyParseException {
        var p = get(file, properties, key);
        if ("false".equals(p) || "true".equals(p)) {
            return "true".equals(p);
        }
        throw new InvalidPropertyException(file, key, "boolean (false/true)");
    }

    public static String getOr(Identifier file, Properties properties, String key, String defaultVal) throws PropertyParseException {
        var p = properties.getProperty(key);
        if (p == null) {
            return defaultVal;
        }
        return p;
    }

    public static int getIntOr(Identifier file, Properties properties, String key, int defaultVal) throws PropertyParseException {
        var p = properties.getProperty(key);
        if (p == null) {
            return defaultVal;
        }
        int r;
        try {
            r = Integer.parseInt(p);
        } catch (NumberFormatException ignored) {
            throw new InvalidPropertyException(file, key, "integer");
        }
        return r;
    }

    public static boolean getBoolOr(Identifier file, Properties properties, String key, boolean defaultVal) throws PropertyParseException {
        var p = properties.getProperty(key);
        if (p == null) {
            return defaultVal;
        }
        if ("false".equals(p) || "true".equals(p)) {
            return "true".equals(p);
        }
        throw new InvalidPropertyException(file, key, "boolean (false/true)");
    }

    public static Map<Integer, Integer> intToIntMap(Properties in) {
        var builder = ImmutableMap.<Integer, Integer>builder();
        for (String k : in.stringPropertyNames()) {
            try {
                builder.put(Integer.parseInt(k), Integer.parseInt(in.getProperty(k)));
            } catch (NumberFormatException ignored) {}
        }
        return builder.build();
    }
}
