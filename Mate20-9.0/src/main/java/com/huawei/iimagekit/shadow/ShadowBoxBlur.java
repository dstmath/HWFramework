package com.huawei.iimagekit.shadow;

import android.graphics.Bitmap;
import android.graphics.Color;

public class ShadowBoxBlur {
    public static void doBlur(Bitmap bitmapForBlur, Bitmap blurredBitmap, int radius) {
        int w = bitmapForBlur.getWidth();
        int h = bitmapForBlur.getHeight();
        int[] pixels = new int[(w * h)];
        bitmapForBlur.getPixels(pixels, 0, w, 0, 0, w, h);
        if ((radius & 1) == 0) {
            radius--;
        }
        ShadowUtil.processAlphaChannelBefore(pixels);
        for (int i = 0; i < 2; i++) {
            boxBlurHorizontal(pixels, w, h, radius / 2);
            boxBlurVertical(pixels, w, h, radius / 2);
        }
        ShadowUtil.processAlphaChannelAfter(pixels);
        blurredBitmap.setPixels(pixels, 0, w, 0, 0, w, h);
    }

    private static void boxBlurHorizontal(int[] pixels, int w, int h, int halfRange) {
        int[] newColors;
        int y;
        long a;
        int[] iArr = pixels;
        int i = w;
        int i2 = halfRange;
        int[] newColors2 = new int[i];
        int index = 0;
        int y2 = 0;
        while (y2 < h) {
            int hits = 0;
            long a2 = 0;
            long r = 0;
            long g = 0;
            long b = 0;
            int x = -i2;
            while (x < i) {
                int oldPixel = (x - i2) - 1;
                if (oldPixel >= 0) {
                    int color = iArr[index + oldPixel];
                    if (color != 0) {
                        y = y2;
                        newColors = newColors2;
                        a2 -= (long) Color.alpha(color);
                        r -= (long) Color.red(color);
                        g -= (long) Color.green(color);
                        b -= (long) Color.blue(color);
                    } else {
                        y = y2;
                        newColors = newColors2;
                    }
                    hits--;
                } else {
                    y = y2;
                    newColors = newColors2;
                }
                int newPixel = x + i2;
                if (newPixel < i) {
                    int color2 = iArr[index + newPixel];
                    if (color2 != 0) {
                        int i3 = newPixel;
                        a2 += (long) Color.alpha(color2);
                        r += (long) Color.red(color2);
                        g += (long) Color.green(color2);
                        b += (long) Color.blue(color2);
                    }
                    hits++;
                }
                if (x >= 0) {
                    a = a2;
                    newColors[x] = Color.argb((int) (a2 / ((long) hits)), (int) (r / ((long) hits)), (int) (g / ((long) hits)), (int) (b / ((long) hits)));
                } else {
                    a = a2;
                }
                x++;
                y2 = y;
                newColors2 = newColors;
                a2 = a;
                i2 = halfRange;
                int i4 = h;
            }
            int[] newColors3 = newColors2;
            System.arraycopy(newColors3, 0, iArr, index, i);
            index += i;
            y2++;
            newColors2 = newColors3;
            i2 = halfRange;
        }
    }

    private static void boxBlurVertical(int[] pixels, int w, int h, int halfRange) {
        int x;
        int oldPixelOffset;
        int newPixelOffset;
        long a;
        int i = w;
        int i2 = h;
        int i3 = halfRange;
        int[] newColors = new int[i2];
        int oldPixelOffset2 = (-(i3 + 1)) * i;
        int newPixelOffset2 = i3 * i;
        int x2 = 0;
        while (x2 < i) {
            int hits = 0;
            long a2 = 0;
            long r = 0;
            long g = 0;
            long b = 0;
            int index = ((-i3) * i) + x2;
            int y = -i3;
            while (y < i2) {
                if ((y - i3) - 1 >= 0) {
                    oldPixelOffset = oldPixelOffset2;
                    int oldPixelOffset3 = pixels[index + oldPixelOffset2];
                    if (oldPixelOffset3 != 0) {
                        x = x2;
                        a2 -= (long) Color.alpha(oldPixelOffset3);
                        r -= (long) Color.red(oldPixelOffset3);
                        g -= (long) Color.green(oldPixelOffset3);
                        b -= (long) Color.blue(oldPixelOffset3);
                    } else {
                        x = x2;
                    }
                    hits--;
                } else {
                    oldPixelOffset = oldPixelOffset2;
                    x = x2;
                }
                if (y + i3 < i2) {
                    int color = pixels[index + newPixelOffset2];
                    if (color != 0) {
                        newPixelOffset = newPixelOffset2;
                        a2 += (long) Color.alpha(color);
                        r += (long) Color.red(color);
                        g += (long) Color.green(color);
                        b += (long) Color.blue(color);
                    } else {
                        newPixelOffset = newPixelOffset2;
                    }
                    hits++;
                } else {
                    newPixelOffset = newPixelOffset2;
                }
                if (y >= 0) {
                    a = a2;
                    newColors[y] = Color.argb((int) (a2 / ((long) hits)), (int) (r / ((long) hits)), (int) (g / ((long) hits)), (int) (b / ((long) hits)));
                } else {
                    a = a2;
                }
                i = w;
                index += i;
                y++;
                oldPixelOffset2 = oldPixelOffset;
                x2 = x;
                newPixelOffset2 = newPixelOffset;
                a2 = a;
            }
            int oldPixelOffset4 = oldPixelOffset2;
            int newPixelOffset3 = newPixelOffset2;
            int x3 = x2;
            for (int y2 = 0; y2 < i2; y2++) {
                pixels[(y2 * i) + x3] = newColors[y2];
            }
            x2 = x3 + 1;
            oldPixelOffset2 = oldPixelOffset4;
            newPixelOffset2 = newPixelOffset3;
        }
        int i4 = newPixelOffset2;
    }
}
