package com.huawei.android.app;

import android.app.AbsWallpaperManager.IBlurWallpaperCallback;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.graphics.Bitmap;
import android.graphics.Rect;
import huawei.android.app.HwWallpaperManager;
import java.io.IOException;
import java.io.InputStream;

public class WallpaperManagerEx {
    public static Bitmap getBlurBitmap(WallpaperManager wm, Rect rect) {
        HwWallpaperManager hwm = getHwWallpaperManager(wm);
        if (hwm != null) {
            return hwm.getBlurBitmap(rect);
        }
        return null;
    }

    public static void setCallback(WallpaperManager wm, IBlurWallpaperCallback callback) {
        HwWallpaperManager hwm = getHwWallpaperManager(wm);
        if (hwm != null) {
            hwm.setCallback(callback);
        }
    }

    public static void forgetLoadedBlurWallpaper(WallpaperManager wm) {
        HwWallpaperManager hwm = getHwWallpaperManager(wm);
        if (hwm != null) {
            hwm.forgetLoadedBlurWallpaper();
        }
    }

    public static HwWallpaperManager getHwWallpaperManager(WallpaperManager wm) {
        if (wm == null || !(wm instanceof HwWallpaperManager)) {
            return null;
        }
        return (HwWallpaperManager) wm;
    }

    public static int[] getWallpaperStartingPoints(WallpaperManager wm) {
        int[] defaultOffsets = new int[]{-1, -1, -1, -1};
        HwWallpaperManager hwm = getHwWallpaperManager(wm);
        if (hwm != null) {
            return hwm.getWallpaperStartingPoints();
        }
        return defaultOffsets;
    }

    public static void setBitmapWithOffsets(WallpaperManager wm, Bitmap bitmap, int[] offsets) throws IOException {
        HwWallpaperManager hwm = getHwWallpaperManager(wm);
        if (hwm != null) {
            hwm.setBitmapWithOffsets(bitmap, offsets);
        }
    }

    public static void setStreamWithOffsets(WallpaperManager wm, InputStream data, int[] offsets) throws IOException {
        HwWallpaperManager hwm = getHwWallpaperManager(wm);
        if (hwm != null) {
            hwm.setStreamWithOffsets(data, offsets);
        }
    }

    public static boolean setWallpaperComponent(WallpaperManager manager, ComponentName name) {
        return manager.setWallpaperComponent(name);
    }

    public static Bitmap getBitmap(WallpaperManager manager) {
        return manager.getBitmap();
    }
}
