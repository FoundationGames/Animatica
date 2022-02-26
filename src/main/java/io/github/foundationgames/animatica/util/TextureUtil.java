package io.github.foundationgames.animatica.util;

import net.minecraft.client.texture.NativeImage;
import net.minecraft.util.math.MathHelper;

public enum TextureUtil {;

    /**
     * Copy a section of an image into another image
     *
     * @param src The source image to copy from
     * @param u The u coordinate on the source image to start the selection from
     * @param v The v coordinate on the source image to start the selection from
     * @param w The width of the selection area
     * @param h The height of the selection area
     * @param dest The destination image to copy to
     * @param du The u coordinate on the destination image to place the selection at
     * @param dv The v coordinate on the destination image to place the selection at
     */
    public static void copy(NativeImage src, int u, int v, int w, int h, NativeImage dest, int du, int dv) {
        // iterate through the entire section of the image to be copied over
        for (int rx = 0; rx < w; rx++) {
            for (int ry = 0; ry < h; ry++) {
                // the current x/y coordinates in the source image
                int srcX = u + rx;
                int srcY = v + ry;
                // the corresponding target x/y coordinates in the target image
                int trgX = du + rx;
                int trgY = dv + ry;

                // set the color of the target pixel on the destination image
                // to the color from the corresponding pixel on the source image
                dest.setPixelColor(trgX, trgY, src.getPixelColor(srcX, srcY));
            }
        }
    }

    /**
     * Copy a blend between 2 sections on a source image to a destination image
     *
     * @param src The source image to copy from
     * @param u0 The u coordinate on the source image to start the first selection from
     * @param v0 The v coordinate on the source image to start the first selection from
     * @param u1 The u coordinate on the source image to start the second selection from
     * @param v1 The v coordinate on the source image to start the second selection from
     * @param w The width of the selection area
     * @param h The height of the selection area
     * @param dest The destination image to copy to
     * @param du The u coordinate on the destination image to place the selection at
     * @param dv The v coordinate on the destination image to place the selection at
     * @param blend The blend between the first selection from the source and the
     *              second (0 = solid first image, 1 = solid second image)
     */
    public static void blendCopy(NativeImage src, int u0, int v0, int u1, int v1, int w, int h, NativeImage dest, int du, int dv, float blend) {
        // iterate through the entire section of the image to be copied over
        for (int rx = 0; rx < w; rx++) {
            for (int ry = 0; ry < h; ry++) {
                // the first set of x/y coordinates in the source image
                int srcX0 = u0 + rx;
                int srcY0 = v0 + ry;
                // the second set of x/y coordinates in the source image
                int srcX1 = u1 + rx;
                int srcY1 = v1 + ry;
                // the corresponding target x/y coordinates in the target image
                int trgX = du + rx;
                int trgY = dv + ry;

                // set the color of the target pixel on the destination image to a blend
                // of the colors from the corresponding pixels on the source image
                dest.setPixelColor(trgX, trgY, lerpColor(src.getFormat(), src.getPixelColor(srcX0, srcY0), src.getPixelColor(srcX1, srcY1), blend));
            }
        }
    }

    public static int lerpColor(NativeImage.Format format, int c1, int c2, float delta) {
        int a1 = (c1 >> format.getAlphaChannelOffset()) & 0xFF;
        int r1 = (c1 >> format.getRedOffset()) & 0xFF;
        int g1 = (c1 >> format.getGreenOffset()) & 0xFF;
        int b1 = (c1 >> format.getBlueOffset()) & 0xFF;

        int a2 = (c2 >> format.getAlphaChannelOffset()) & 0xFF;
        int r2 = (c2 >> format.getRedOffset()) & 0xFF;
        int g2 = (c2 >> format.getGreenOffset()) & 0xFF;
        int b2 = (c2 >> format.getBlueOffset()) & 0xFF;

        // If the first or second color is transparent,
        // don't lerp any leftover rgb values and instead
        // only use those of the non-transparent color
        if (a1 <= 0) {
            r1 = r2;
            g1 = g2;
            b1 = b2;
        } else if (a2 <= 0) {
            r2 = r1;
            g2 = g1;
            b2 = b1;
        }

        int oa = (int)MathHelper.lerp(delta, a1, a2);
        int or = (int)MathHelper.lerp(delta, r1, r2);
        int og = (int)MathHelper.lerp(delta, g1, g2);
        int ob = (int)MathHelper.lerp(delta, b1, b2);

        return (oa << format.getAlphaChannelOffset()) | (or << format.getRedOffset()) | (og << format.getGreenOffset()) | (ob << format.getBlueOffset());
    }
}
