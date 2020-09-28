package com.huawei.android.app;

import android.app.AbsWallpaperManager;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.graphics.Bitmap;
import android.graphics.Rect;
import java.io.IOException;
import java.io.InputStream;

public class WallpaperManagerEx {
    public static Bitmap getBlurBitmap(WallpaperManager wm, Rect rect) {
        if (wm != null) {
            return WallpaperManagerAdapter.getBlurBitmap(wm, rect);
        }
        return null;
    }

    public static void setCallback(WallpaperManager wm, AbsWallpaperManager.IBlurWallpaperCallback callback) {
        if (wm != null) {
            WallpaperManagerAdapter.setCallback(wm, callback);
        }
    }

    public static void forgetLoadedBlurWallpaper(WallpaperManager wm) {
        if (wm != null) {
            WallpaperManagerAdapter.forgetLoadedBlurWallpaper(wm);
        }
    }

    public static int[] getWallpaperStartingPoints(WallpaperManager wm) {
        int[] defaultOffsets = {-1, -1, -1, -1};
        if (wm != null) {
            return WallpaperManagerAdapter.getWallpaperStartingPoints(wm);
        }
        return defaultOffsets;
    }

    public static void setBitmapWithOffsets(WallpaperManager wm, Bitmap bitmap, int[] offsets) throws IOException {
        if (wm != null) {
            WallpaperManagerAdapter.setBitmapWithOffsets(wm, bitmap, offsets);
        }
    }

    public static void setStreamWithOffsets(WallpaperManager wm, InputStream data, int[] offsets) throws IOException {
        if (wm != null) {
            WallpaperManagerAdapter.setStreamWithOffsets(wm, data, offsets);
        }
    }

    public static boolean setWallpaperComponent(WallpaperManager manager, ComponentName name) {
        return WallpaperManagerAdapter.setWallpaperComponent(manager, name);
    }

    public static Bitmap getBitmap(WallpaperManager manager) {
        return WallpaperManagerAdapter.getBitmap(manager);
    }
}
