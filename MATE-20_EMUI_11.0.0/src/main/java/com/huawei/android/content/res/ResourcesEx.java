package com.huawei.android.content.res;

import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;

public class ResourcesEx {
    public static Drawable getThemeDrawableByName(Resources resources, String name) {
        Bitmap themeIcon;
        if (name == null || name.isEmpty() || (themeIcon = resources.getImpl().getHwResourcesImpl().getThemeIconByName(name)) == null) {
            return null;
        }
        return new BitmapDrawable(resources, themeIcon);
    }

    public static Bitmap getOptimizationIcon(Resources resources, Bitmap bmpSrc) {
        if (bmpSrc == null || bmpSrc.isRecycled()) {
            return null;
        }
        return resources.getImpl().getHwResourcesImpl().addShortcutBackgroud(bmpSrc);
    }

    public static float getApplicationScale(Resources resources) {
        return resources.getCompatibilityInfo().applicationScale;
    }

    private static Resources buildResources(AssetManager assets, DisplayMetrics metrics, Configuration config) {
        return new Resources(assets, metrics, config);
    }

    public static boolean removeIconCache(Resources resources, String packageName) {
        if (resources == null || packageName == null) {
            return false;
        }
        return resources.getImpl().getHwResourcesImpl().removeIconCache(packageName);
    }
}
