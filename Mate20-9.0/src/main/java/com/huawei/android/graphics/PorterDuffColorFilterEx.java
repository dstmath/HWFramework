package com.huawei.android.graphics;

import android.graphics.PorterDuffColorFilter;

public class PorterDuffColorFilterEx {
    public static int getColor(PorterDuffColorFilter filter) {
        return filter.getColor();
    }

    public static void setColor(PorterDuffColorFilter filter, int color) {
        filter.setColor(color);
    }
}
