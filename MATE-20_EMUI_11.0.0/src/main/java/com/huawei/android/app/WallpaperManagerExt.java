package com.huawei.android.app;

import android.app.WallpaperManager;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;

public class WallpaperManagerExt {
    public static Bitmap getBlurBitmap(WallpaperManager wallpaperManager, Rect rect) {
        if (wallpaperManager != null) {
            return wallpaperManager.getBlurBitmap(rect);
        }
        return null;
    }

    public static boolean isIWallpaperManagerNull(WallpaperManager wallpaperManager) {
        if (wallpaperManager == null || wallpaperManager.getIWallpaperManager() != null) {
            return false;
        }
        return true;
    }

    public static ParcelFileDescriptor getBlurWallpaper(WallpaperManager wallpaperManager, IWallpaperManagerCallbackEx wallpaperManagerCallbackExt) throws RemoteException {
        if (wallpaperManager == null || wallpaperManager.getIWallpaperManager() == null || wallpaperManagerCallbackExt == null) {
            return null;
        }
        return wallpaperManager.getIWallpaperManager().getBlurWallpaper(wallpaperManagerCallbackExt.getIWallpaperManagerCallback());
    }
}
