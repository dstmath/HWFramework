package com.huawei.anim.dynamicanimation.util;

public class Utils {
    private static final float a = 1.0E-5f;

    private Utils() {
    }

    public static boolean isFloatZero(float f) {
        return Math.abs(f) < a;
    }
}
