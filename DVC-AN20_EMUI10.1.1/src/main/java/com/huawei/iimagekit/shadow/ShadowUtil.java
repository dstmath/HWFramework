package com.huawei.iimagekit.shadow;

import android.graphics.Color;

public class ShadowUtil {
    static final float MAX_CHANNEL_VALUE = 255.0f;

    private ShadowUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static void processAlphaChannelBefore(int[] pixels) {
        for (int pi = 0; pi < pixels.length; pi++) {
            float pa = (float) Color.alpha(pixels[pi]);
            pixels[pi] = Color.argb((int) pa, (int) ((((float) Color.red(pixels[pi])) * pa) / MAX_CHANNEL_VALUE), (int) ((((float) Color.green(pixels[pi])) * pa) / MAX_CHANNEL_VALUE), (int) ((((float) Color.blue(pixels[pi])) * pa) / MAX_CHANNEL_VALUE));
        }
    }

    public static void processAlphaChannelAfter(int[] pixels) {
        for (int pi = 0; pi < pixels.length; pi++) {
            float pa = (float) Color.alpha(pixels[pi]);
            pixels[pi] = Color.argb((int) pa, (int) ((((float) Color.red(pixels[pi])) * MAX_CHANNEL_VALUE) / pa), (int) ((((float) Color.green(pixels[pi])) * MAX_CHANNEL_VALUE) / pa), (int) ((MAX_CHANNEL_VALUE * ((float) Color.blue(pixels[pi]))) / pa));
        }
    }
}
