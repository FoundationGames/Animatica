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
     * @param blend How transparent the selection should be when copied onto the destination image (0 = solid, 1 = invisible)
     */
    public static void copy(NativeImage src, int u, int v, int w, int h, NativeImage dest, int du, int dv, float blend) {
        // iterate through the entire section of the image to be copied over
        for (int rx = 0; rx < w; rx++) {
            for (int ry = 0; ry < h; ry++) {
                // the current x/y coordinates in the source image
                int srcX = u + rx;
                int srcY = v + ry;
                // the corresponding target x/y coordinates in the target image
                int trgX = du + rx;
                int trgY = dv + ry;

                // the color of the pixel to be copied from the source image (in RGBA)
                int color = src.getPixelColor(srcX, srcY);

                if (blend > 0) {
                    // if blending is to occur, blend between the source and target pixels' colors
                    color = lerpRgba(dest.getPixelColor(trgX, trgY), color, blend);
                }

                // set the color of the target pixel on the destination image
                dest.setPixelColor(trgX, trgY, color);
            }
        }
    }

    public static int lerpRgba(int rgba1, int rgba2, float delta) {
        int r1 = (rgba1 >> 16) & 0xFF;
        int g1 = (rgba1 >> 8) & 0xFF;
        int b1 = rgba1 & 0xFF;
        int r2 = (rgba2 >> 16) & 0xFF;
        int g2 = (rgba2 >> 8) & 0xFF;
        int b2 = rgba2 & 0xFF;
        int or = (int)MathHelper.lerp(delta, r1, r2);
        int og = (int)MathHelper.lerp(delta, g1, g2);
        int ob = (int)MathHelper.lerp(delta, b1, b2);
        return (or << 16) | (og << 8) | ob;
    }
}
