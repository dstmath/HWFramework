package android.common;

import android.app.IHwWallpaperManagerEx;
import android.graphics.Bitmap;
import android.graphics.Rect;

public class DefaultHwWallpaperManager implements IHwWallpaperManagerEx {
    private static DefaultHwWallpaperManager sInstance = null;

    public static synchronized IHwWallpaperManagerEx getDefault() {
        DefaultHwWallpaperManager defaultHwWallpaperManager;
        synchronized (DefaultHwWallpaperManager.class) {
            if (sInstance == null) {
                sInstance = new DefaultHwWallpaperManager();
            }
            defaultHwWallpaperManager = sInstance;
        }
        return defaultHwWallpaperManager;
    }

    @Override // android.app.IHwWallpaperManagerEx
    public void setWallpaperStartingPoints(int[] offsets) {
    }

    @Override // android.app.IHwWallpaperManagerEx
    public Bitmap getBlurBitmap(Rect rect) {
        return null;
    }

    @Override // android.app.IHwWallpaperManagerEx
    public Bitmap createDefaultWallpaperBitmap(Bitmap bitmap) {
        return null;
    }
}
