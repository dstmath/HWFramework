package huawei.android.app;

import android.app.IHwWallpaperInfoStub;
import android.app.WallpaperInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.hwtheme.HwThemeManager;

public class HwWallpaperInfoStubImpl implements IHwWallpaperInfoStub {
    private WallpaperInfo mWallpaperInfo;

    public HwWallpaperInfoStubImpl(WallpaperInfo ai) {
        this.mWallpaperInfo = ai;
    }

    public Drawable loadThumbnailWithoutTheme(PackageManager pm) {
        int thumbnailResource = 0;
        if (this.mWallpaperInfo != null) {
            thumbnailResource = this.mWallpaperInfo.getThumbnailResource();
        }
        if (thumbnailResource < 0 || this.mWallpaperInfo == null) {
            return null;
        }
        String packageName = this.mWallpaperInfo.getPackageName();
        HwThemeManager.removeIconCache(null, packageName, thumbnailResource, 0);
        Drawable dr = pm.getDrawable(packageName, thumbnailResource, this.mWallpaperInfo.getServiceInfo().applicationInfo);
        HwThemeManager.restoreIconCache(packageName, thumbnailResource);
        return dr;
    }
}
