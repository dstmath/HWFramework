package huawei.android.app;

import android.app.DefaultHwWallpaperInfoStubImpl;
import android.app.WallpaperInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.hwtheme.HwThemeManager;
import com.huawei.android.app.WallpaperInfoEx;

public class HwWallpaperInfoStubImpl extends DefaultHwWallpaperInfoStubImpl {
    private static HwWallpaperInfoStubImpl sInstance;
    private static WallpaperInfo sWallpaperInfo;

    public static synchronized HwWallpaperInfoStubImpl getDefault(WallpaperInfo ai) {
        HwWallpaperInfoStubImpl hwWallpaperInfoStubImpl;
        synchronized (HwWallpaperInfoStubImpl.class) {
            sWallpaperInfo = ai;
            if (sInstance == null) {
                sInstance = new HwWallpaperInfoStubImpl();
            }
            hwWallpaperInfoStubImpl = sInstance;
        }
        return hwWallpaperInfoStubImpl;
    }

    public Drawable loadThumbnailWithoutTheme(PackageManager pm) {
        WallpaperInfo wallpaperInfo;
        int thumbnailResource = 0;
        WallpaperInfo wallpaperInfo2 = sWallpaperInfo;
        if (wallpaperInfo2 != null) {
            thumbnailResource = WallpaperInfoEx.getThumbnailResource(wallpaperInfo2);
        }
        if (thumbnailResource < 0 || (wallpaperInfo = sWallpaperInfo) == null || pm == null) {
            return null;
        }
        String packageName = wallpaperInfo.getPackageName();
        HwThemeManager.removeIconCache((String) null, packageName, thumbnailResource, 0);
        Drawable dr = pm.getDrawable(packageName, thumbnailResource, sWallpaperInfo.getServiceInfo().applicationInfo);
        HwThemeManager.restoreIconCache(packageName, thumbnailResource);
        return dr;
    }
}
