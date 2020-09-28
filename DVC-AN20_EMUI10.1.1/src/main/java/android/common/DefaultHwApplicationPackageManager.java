package android.common;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

public class DefaultHwApplicationPackageManager implements HwPackageManager {
    private static DefaultHwApplicationPackageManager mInstance = null;

    public static synchronized HwPackageManager getDefault() {
        DefaultHwApplicationPackageManager defaultHwApplicationPackageManager;
        synchronized (DefaultHwApplicationPackageManager.class) {
            if (mInstance == null) {
                mInstance = new DefaultHwApplicationPackageManager();
            }
            defaultHwApplicationPackageManager = mInstance;
        }
        return defaultHwApplicationPackageManager;
    }

    @Override // android.common.HwPackageManager
    public CharSequence getAppLabelText(PackageManager packageManager, String packageName, int resid, ApplicationInfo appInfo) {
        return null;
    }

    @Override // android.common.HwPackageManager
    public Drawable getBadgedIconForTrustSpace(PackageManager packageManager) {
        return null;
    }
}
