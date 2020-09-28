package com.huawei.android.app;

import android.app.WallpaperManager;
import android.graphics.Bitmap;
import android.graphics.Rect;

public class WallpaperManagerExt {
    public static Bitmap getBlurBitmap(WallpaperManager wallpaperManager, Rect rect) {
        if (wallpaperManager != null) {
            return wallpaperManager.getBlurBitmap(rect);
        }
        return null;
    }
}
