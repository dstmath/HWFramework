package android.app;

import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

public class DefaultHwWallpaperInfoStubImpl implements IHwWallpaperInfoStub {
    private static WallpaperInfo mWallpaperInfo;
    private static DefaultHwWallpaperInfoStubImpl sInstance;

    public static synchronized DefaultHwWallpaperInfoStubImpl getDefault(WallpaperInfo ai) {
        DefaultHwWallpaperInfoStubImpl defaultHwWallpaperInfoStubImpl;
        synchronized (DefaultHwWallpaperInfoStubImpl.class) {
            mWallpaperInfo = ai;
            if (sInstance == null) {
                sInstance = new DefaultHwWallpaperInfoStubImpl();
            }
            defaultHwWallpaperInfoStubImpl = sInstance;
        }
        return defaultHwWallpaperInfoStubImpl;
    }

    @Override // android.app.IHwWallpaperInfoStub
    public Drawable loadThumbnailWithoutTheme(PackageManager pm) {
        return null;
    }
}
