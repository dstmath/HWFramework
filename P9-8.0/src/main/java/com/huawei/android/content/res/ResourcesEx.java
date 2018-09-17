package com.huawei.android.content.res;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

public class ResourcesEx {
    public static Drawable getThemeDrawableByName(Resources resources, String name) {
        return resources.getThemeDrawableByName(name);
    }

    public static Bitmap getOptimizationIcon(Resources resources, Bitmap bmpSrc) {
        return resources.getOptimizationIcon(bmpSrc);
    }

    public static float getApplicationScale(Resources resources) {
        return resources.getCompatibilityInfo().applicationScale;
    }
}
