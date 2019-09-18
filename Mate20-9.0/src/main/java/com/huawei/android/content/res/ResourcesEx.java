package com.huawei.android.content.res;

import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;

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

    private static Resources buildResources(AssetManager assets, DisplayMetrics metrics, Configuration config) {
        return new Resources(assets, metrics, config);
    }
}
