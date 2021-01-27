package com.huawei.android.graphics;

import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;

public class PorterDuffColorFilterEx {
    public static int getColor(PorterDuffColorFilter filter) {
        return filter.getColor();
    }

    public static void setColor(PorterDuffColorFilter filter, int color) {
    }

    public static PorterDuff.Mode getMode(PorterDuffColorFilter filter) {
        return filter.getMode();
    }
}
