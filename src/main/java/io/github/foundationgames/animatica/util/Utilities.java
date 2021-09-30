package io.github.foundationgames.animatica.util;

import net.minecraft.util.Identifier;

public enum Utilities {;
    public static Identifier processPath(Identifier fileRelativeTo, Identifier path) {
        if (path.getPath().startsWith("./")) {
            int lInd = fileRelativeTo.getPath().lastIndexOf("/");
            if (lInd > 0) {
                var builder = new StringBuilder(fileRelativeTo.getPath());
                builder.replace(lInd, builder.length(), path.getPath().replaceFirst("\\./", "/"));
                return new Identifier(fileRelativeTo.getNamespace(), builder.toString());
            }
        } else if (path.getPath().startsWith("~/")) {
            return new Identifier(path.getNamespace(), path.getPath().replaceFirst("~/", "optifine/"));
        }
        return path;
    }
}
