package com.huawei.android.app;

import android.app.IWallpaperManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import libcore.io.IoUtils;

public class HwWallpaperManagerEx {
    private static final String TAG = "HwWallpaperManagerEx";

    public static Bitmap screenshotLiveWallpaper(float scale, Bitmap.Config config) {
        if (scale <= 0.0f || scale > 1.0f) {
            Log.w(TAG, "scale is not in 0.0~1.0f");
            return null;
        }
        ParcelFileDescriptor fd = null;
        try {
            IWallpaperManager wallpaper = IWallpaperManager.Stub.asInterface(ServiceManager.getService("wallpaper"));
            int ordinal = -1;
            if (config != null) {
                ordinal = config.ordinal();
            }
            fd = wallpaper.screenshotLiveWallpaper(scale, ordinal);
            if (fd == null) {
                Log.w(TAG, "screenshotLiveWallpaper return is null");
                return null;
            }
            Bitmap decodeFileDescriptor = BitmapFactory.decodeFileDescriptor(fd.getFileDescriptor());
            IoUtils.closeQuietly(fd);
            return decodeFileDescriptor;
        } catch (RemoteException e) {
            Log.e(TAG, "screenshotWallpaper failed: " + e.getMessage());
            return null;
        } finally {
            IoUtils.closeQuietly(fd);
        }
    }
}
