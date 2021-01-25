package com.android.systemui.shared.recents.utilities;

import android.graphics.Color;
import android.os.Handler;

public class Utilities {
    public static void postAtFrontOfQueueAsynchronously(Handler h, Runnable r) {
        h.sendMessageAtFrontOfQueue(h.obtainMessage().setCallback(r));
    }

    public static float computeContrastBetweenColors(int bg, int fg) {
        float fgB;
        float fgR;
        float bgR = ((float) Color.red(bg)) / 255.0f;
        float bgG = ((float) Color.green(bg)) / 255.0f;
        float bgB = ((float) Color.blue(bg)) / 255.0f;
        float bgL = ((bgR < 0.03928f ? bgR / 12.92f : (float) Math.pow((double) ((bgR + 0.055f) / 1.055f), 2.4000000953674316d)) * 0.2126f) + ((bgG < 0.03928f ? bgG / 12.92f : (float) Math.pow((double) ((bgG + 0.055f) / 1.055f), 2.4000000953674316d)) * 0.7152f) + ((bgB < 0.03928f ? bgB / 12.92f : (float) Math.pow((double) ((bgB + 0.055f) / 1.055f), 2.4000000953674316d)) * 0.0722f);
        float fgR2 = ((float) Color.red(fg)) / 255.0f;
        float fgG = ((float) Color.green(fg)) / 255.0f;
        float fgB2 = ((float) Color.blue(fg)) / 255.0f;
        if (fgR2 < 0.03928f) {
            fgR = fgR2 / 12.92f;
            fgB = fgB2;
        } else {
            fgB = fgB2;
            fgR = (float) Math.pow((double) ((fgR2 + 0.055f) / 1.055f), 2.4000000953674316d);
        }
        return Math.abs(((((0.2126f * fgR) + (0.7152f * (fgG < 0.03928f ? fgG / 12.92f : (float) Math.pow((double) ((fgG + 0.055f) / 1.055f), 2.4000000953674316d)))) + ((fgB < 0.03928f ? fgB / 12.92f : (float) Math.pow((double) ((fgB + 0.055f) / 1.055f), 2.4000000953674316d)) * 0.0722f)) + 0.05f) / (0.05f + bgL));
    }

    public static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
