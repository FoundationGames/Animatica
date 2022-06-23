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

    public static int lcm(int[] vals) {
        int ret = vals[0];
        int a, b;
        if (vals.length > 1) for (int i = 1; i < vals.length; i++) {
            a = ret; b = vals[i];

            int gcd;
            int ax = a; int bx = b;
            while (bx > 0) {
                int t = bx;
                bx = ax % bx;
                ax = t;
            }
            gcd = ax;

            ret = a * (b / gcd);
        }
        return ret;
    }
}
