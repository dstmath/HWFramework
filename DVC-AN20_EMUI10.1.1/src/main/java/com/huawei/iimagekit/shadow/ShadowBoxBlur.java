package com.huawei.iimagekit.shadow;

import android.graphics.Bitmap;
import android.graphics.Color;

public class ShadowBoxBlur {
    static final int ITERATIONS = 2;
    static final int RADIUS_NUM = 2;

    public static void doBlur(Bitmap bitmapForBlur, Bitmap blurredBitmap, int radius) {
        int width = bitmapForBlur.getWidth();
        int height = bitmapForBlur.getHeight();
        if (width > 0 && height > 0) {
            int[] pixels = new int[(width * height)];
            int blurRadius = radius;
            bitmapForBlur.getPixels(pixels, 0, width, 0, 0, width, height);
            if (radius % 2 == 0) {
                blurRadius = radius - 1;
            }
            ShadowUtil.processAlphaChannelBefore(pixels);
            for (int i = 0; i < 2; i++) {
                boxBlurHorizontal(pixels, width, height, blurRadius / 2);
                boxBlurVertical(pixels, width, height, blurRadius / 2);
            }
            ShadowUtil.processAlphaChannelAfter(pixels);
            blurredBitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        }
    }

    /* JADX INFO: Multiple debug info for r4v3 int: [D('newPixel' int), D('newColors' int[])] */
    private static void boxBlurHorizontal(int[] pixels, int width, int height, int halfRange) {
        int[] newColors;
        int j;
        long alpha;
        int[] iArr = pixels;
        int i = width;
        int i2 = halfRange;
        int index = 0;
        int[] newColors2 = new int[i];
        int j2 = 0;
        while (j2 < height) {
            int hits = 0;
            long alpha2 = 0;
            long red = 0;
            long green = 0;
            long blue = 0;
            int i3 = -i2;
            while (i3 < i) {
                int oldPixel = (i3 - i2) - 1;
                if (oldPixel >= 0) {
                    int color = iArr[index + oldPixel];
                    if (color != 0) {
                        j = j2;
                        newColors = newColors2;
                        alpha2 -= (long) Color.alpha(color);
                        red -= (long) Color.red(color);
                        green -= (long) Color.green(color);
                        blue -= (long) Color.blue(color);
                    } else {
                        newColors = newColors2;
                        j = j2;
                    }
                    hits--;
                } else {
                    newColors = newColors2;
                    j = j2;
                }
                int newPixel = i3 + i2;
                if (newPixel < i) {
                    int color2 = iArr[index + newPixel];
                    if (color2 != 0) {
                        alpha2 += (long) Color.alpha(color2);
                        red += (long) Color.red(color2);
                        green += (long) Color.green(color2);
                        blue += (long) Color.blue(color2);
                    }
                    hits++;
                }
                if (i3 >= 0) {
                    alpha = alpha2;
                    newColors[i3] = Color.argb((int) (alpha2 / ((long) hits)), (int) (red / ((long) hits)), (int) (green / ((long) hits)), (int) (blue / ((long) hits)));
                } else {
                    alpha = alpha2;
                }
                i3++;
                iArr = pixels;
                i = width;
                i2 = halfRange;
                j2 = j;
                newColors2 = newColors;
                alpha2 = alpha;
            }
            System.arraycopy(newColors2, 0, pixels, index, width);
            index += width;
            j2++;
            iArr = pixels;
            i = width;
            i2 = halfRange;
        }
    }

    /* JADX INFO: Multiple debug info for r3v6 int: [D('newPixel' int), D('newColors' int[])] */
    private static void boxBlurVertical(int[] pixels, int width, int height, int halfRange) {
        int[] newColors;
        int oldPixelOffset;
        int hits;
        long alpha;
        int newPixelOffset;
        int hits2 = halfRange;
        int[] newColors2 = new int[height];
        int oldPixelOffset2 = (-(hits2 + 1)) * width;
        int newPixelOffset2 = hits2 * width;
        int i = 0;
        while (i < width) {
            long alpha2 = 0;
            long red = 0;
            long green = 0;
            long blue = 0;
            int hits3 = 0;
            int index = ((-hits2) * width) + i;
            int j = -hits2;
            while (j < height) {
                if ((j - hits2) - 1 >= 0) {
                    int color = pixels[index + oldPixelOffset2];
                    if (color != 0) {
                        oldPixelOffset = oldPixelOffset2;
                        newColors = newColors2;
                        alpha2 -= (long) Color.alpha(color);
                        red -= (long) Color.red(color);
                        green -= (long) Color.green(color);
                        blue -= (long) Color.blue(color);
                    } else {
                        newColors = newColors2;
                        oldPixelOffset = oldPixelOffset2;
                    }
                    hits3--;
                } else {
                    newColors = newColors2;
                    oldPixelOffset = oldPixelOffset2;
                }
                if (j + hits2 < height) {
                    int color2 = pixels[index + newPixelOffset2];
                    if (color2 != 0) {
                        alpha2 += (long) Color.alpha(color2);
                        red += (long) Color.red(color2);
                        green += (long) Color.green(color2);
                        blue += (long) Color.blue(color2);
                    }
                    hits = hits3 + 1;
                } else {
                    hits = hits3;
                }
                if (j >= 0) {
                    newPixelOffset = newPixelOffset2;
                    alpha = alpha2;
                    newColors[j] = Color.argb((int) (alpha2 / ((long) hits)), (int) (red / ((long) hits)), (int) (green / ((long) hits)), (int) (blue / ((long) hits)));
                } else {
                    newPixelOffset = newPixelOffset2;
                    alpha = alpha2;
                }
                index += width;
                j++;
                hits3 = hits;
                oldPixelOffset2 = oldPixelOffset;
                newColors2 = newColors;
                newPixelOffset2 = newPixelOffset;
                alpha2 = alpha;
                hits2 = halfRange;
            }
            for (int j2 = 0; j2 < height; j2++) {
                pixels[(j2 * width) + i] = newColors2[j2];
            }
            i++;
            hits2 = halfRange;
            oldPixelOffset2 = oldPixelOffset2;
            newColors2 = newColors2;
            newPixelOffset2 = newPixelOffset2;
        }
    }
}
