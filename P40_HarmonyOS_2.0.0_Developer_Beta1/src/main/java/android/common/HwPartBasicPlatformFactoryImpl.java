package android.common;

import android.app.HwWallpaperManagerEx;
import android.app.IHwWallpaperInfoStub;
import android.app.IHwWallpaperManagerEx;
import android.app.WallpaperInfo;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.pm.HwPackageParser;
import android.content.pm.IHwPackageParser;
import android.content.res.HwConfiguration;
import android.content.res.IHwConfiguration;
import android.cover.CoverManager;
import android.cover.IHwCoverManager;
import android.hwtheme.IHwThemeManagerFactory;
import android.view.HwViewImpl;
import android.view.HwViewRootImpl;
import android.view.IHwView;
import android.view.IHwViewRootImpl;
import com.huawei.android.app.IWallpaperManagerEx;
import huawei.android.app.HwActivityThreadImpl;
import huawei.android.app.HwApplicationPackageManager;
import huawei.android.app.HwWallpaperInfoStubImpl;
import huawei.android.hwtheme.HwThemeManagerFactoryImpl;

public class HwPartBasicPlatformFactoryImpl extends HwPartBasicPlatformFactory {
    public IHwViewRootImpl getHwViewRootImpl() {
        return HwViewRootImpl.getDefault();
    }

    public HwActivityThread getHwActivityThread() {
        return HwActivityThreadImpl.getDefault();
    }

    public IHwPackageParser getHwPackageParser() {
        return HwPackageParser.getDefault();
    }

    public IHwView getHwView() {
        return HwViewImpl.getDefault();
    }

    public IHwConfiguration createHwConfiguration() {
        return new HwConfiguration();
    }

    public IHwThemeManagerFactory getHwThemeManagerFactory() {
        return new HwThemeManagerFactoryImpl();
    }

    public HwPackageManager getHwApplicationPackageManager() {
        return HwApplicationPackageManager.getDefault();
    }

    public IHwCoverManager getCoverManager() {
        return CoverManager.getDefault();
    }

    public IHwWallpaperInfoStub getHwWallpaperInfoStub(WallpaperInfo ai) {
        return HwWallpaperInfoStubImpl.getDefault(ai);
    }

    public IHwWallpaperManagerEx getHuaweiWallpaperManager(Context context, IWallpaperManagerEx service, WallpaperManager wm) {
        return new HwWallpaperManagerEx(context, service, wm);
    }
}
