package com.huawei.iimagekit.shadow;

import android.graphics.Color;

public class ShadowUtil {
    public static void processAlphaChannelBefore(int[] pixels) {
        for (int pi = 0; pi < pixels.length; pi++) {
            float pa = (float) Color.alpha(pixels[pi]);
            pixels[pi] = Color.argb((int) pa, (int) ((((float) Color.red(pixels[pi])) * pa) / 255.0f), (int) ((((float) Color.green(pixels[pi])) * pa) / 255.0f), (int) ((((float) Color.blue(pixels[pi])) * pa) / 255.0f));
        }
    }

    public static void processAlphaChannelAfter(int[] pixels) {
        for (int pi = 0; pi < pixels.length; pi++) {
            float pa = (float) Color.alpha(pixels[pi]);
            pixels[pi] = Color.argb((int) pa, (int) ((((float) Color.red(pixels[pi])) * 255.0f) / pa), (int) ((((float) Color.green(pixels[pi])) * 255.0f) / pa), (int) ((255.0f * ((float) Color.blue(pixels[pi]))) / pa));
        }
    }
}
