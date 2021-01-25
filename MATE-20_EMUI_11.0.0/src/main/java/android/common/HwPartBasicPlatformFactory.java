package android.common;

import android.app.DefaultHwWallpaperInfoStubImpl;
import android.app.IHwWallpaperInfoStub;
import android.app.IHwWallpaperManagerEx;
import android.app.WallpaperInfo;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.pm.DefaultHwPackageParser;
import android.content.pm.IHwPackageParser;
import android.content.res.HwConfigurationDummy;
import android.content.res.IHwConfiguration;
import android.cover.DefaultCoverManager;
import android.cover.IHwCoverManager;
import android.hwtheme.IHwThemeManagerFactory;
import android.util.Log;
import android.view.DefaultHwViewImpl;
import android.view.DefaultHwViewRootImpl;
import android.view.IHwView;
import android.view.IHwViewRootImpl;
import com.huawei.android.app.IWallpaperManagerEx;
import huawei.android.app.DefaultHwActivityThreadImpl;
import huawei.android.hwtheme.DefaultHwThemeManagerFactoryImpl;

public class HwPartBasicPlatformFactory {
    public static final String BASIC_PLATFORM_FACTORY_IMPL_NAME = "android.common.HwPartBasicPlatformFactoryImpl";
    private static final String TAG = "HwPartBasicPlatformFactory";
    private static HwPartBasicPlatformFactory mFactory;

    public static HwPartBasicPlatformFactory loadFactory(String factoryName) {
        HwPartBasicPlatformFactory hwPartBasicPlatformFactory = mFactory;
        if (hwPartBasicPlatformFactory != null) {
            return hwPartBasicPlatformFactory;
        }
        Object object = FactoryLoader.loadFactory(factoryName);
        if (object == null || !(object instanceof HwPartBasicPlatformFactory)) {
            mFactory = new HwPartBasicPlatformFactory();
        } else {
            mFactory = (HwPartBasicPlatformFactory) object;
        }
        if (mFactory != null) {
            Log.i(TAG, "add " + factoryName + " to memory.");
            return mFactory;
        }
        throw new RuntimeException("can't load any basic platform factory");
    }

    public IHwViewRootImpl getHwViewRootImpl() {
        return DefaultHwViewRootImpl.getDefault();
    }

    public HwActivityThread getHwActivityThread() {
        return DefaultHwActivityThreadImpl.getDefault();
    }

    public IHwPackageParser getHwPackageParser() {
        return DefaultHwPackageParser.getDefault();
    }

    public IHwView getHwView() {
        return DefaultHwViewImpl.getDefault();
    }

    public IHwConfiguration createHwConfiguration() {
        return new HwConfigurationDummy();
    }

    public IHwThemeManagerFactory getHwThemeManagerFactory() {
        return new DefaultHwThemeManagerFactoryImpl();
    }

    public HwPackageManager getHwApplicationPackageManager() {
        return DefaultHwApplicationPackageManager.getDefault();
    }

    public IHwCoverManager getCoverManager() {
        return DefaultCoverManager.getDefault();
    }

    public IHwWallpaperInfoStub getHwWallpaperInfoStub(WallpaperInfo ai) {
        return DefaultHwWallpaperInfoStubImpl.getDefault(ai);
    }

    public IHwWallpaperManagerEx getHuaweiWallpaperManager(Context context, IWallpaperManagerEx service, WallpaperManager wm) {
        return DefaultHwWallpaperManager.getDefault();
    }
}
