package com.android.internal.util;

public class FastMath {
    public static int round(float value) {
        return (int) ((8388608 + ((long) (1.6777216E7f * value))) >> 24);
    }
}
