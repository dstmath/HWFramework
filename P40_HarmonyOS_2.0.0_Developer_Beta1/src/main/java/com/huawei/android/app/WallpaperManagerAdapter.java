package com.huawei.android.app;

import android.app.AbsWallpaperManager;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.graphics.Bitmap;
import android.graphics.Rect;
import com.huawei.annotation.HwSystemApi;
import java.io.IOException;
import java.io.InputStream;

@HwSystemApi
public class WallpaperManagerAdapter {
    private WallpaperManagerAdapter() {
    }

    public static boolean setWallpaperComponent(WallpaperManager manager, ComponentName name) {
        return manager.setWallpaperComponent(name);
    }

    public static Bitmap getBitmap(WallpaperManager manager) {
        return manager.getBitmap();
    }

    public static Bitmap getBlurBitmap(WallpaperManager manager, Rect rect) {
        return manager.getBlurBitmap(rect);
    }

    public static void setCallback(WallpaperManager manager, AbsWallpaperManager.IBlurWallpaperCallback callback) {
        manager.setCallback(callback);
    }

    public static void forgetLoadedBlurWallpaper(WallpaperManager manager) {
        manager.forgetLoadedBlurWallpaper();
    }

    public static int[] getWallpaperStartingPoints(WallpaperManager manager) {
        return manager.getWallpaperStartingPoints();
    }

    public static void setBitmapWithOffsets(WallpaperManager manager, Bitmap bitmap, int[] offsets) throws IOException {
        manager.setBitmapWithOffsets(bitmap, offsets);
    }

    public static void setStreamWithOffsets(WallpaperManager manager, InputStream data, int[] offsets) throws IOException {
        manager.setStreamWithOffsets(data, offsets);
    }

    public static Bitmap peekBlurWallpaperBitmap(WallpaperManager manager, Rect rect) {
        return manager.peekBlurWallpaperBitmap(rect);
    }
}
