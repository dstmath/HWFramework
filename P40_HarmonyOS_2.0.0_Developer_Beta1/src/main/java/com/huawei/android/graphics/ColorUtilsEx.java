package com.huawei.android.graphics;

import com.android.internal.graphics.ColorUtils;

public class ColorUtilsEx {
    public static double calculateLuminance(int color) {
        return ColorUtils.calculateLuminance(color);
    }
}
