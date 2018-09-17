package android.app;

import android.content.ComponentName;
import android.content.pm.ActivityInfo;
import android.os.ServiceManager;
import huawei.cust.HwCustUtils;

public class HwCustNonHardwareAcceleratedPackagesManager {
    private static boolean mFlag = false;
    private static HwCustNonHardwareAcceleratedPackagesManager sInstance = null;

    public static synchronized HwCustNonHardwareAcceleratedPackagesManager getDefault() {
        HwCustNonHardwareAcceleratedPackagesManager hwCustNonHardwareAcceleratedPackagesManager;
        synchronized (HwCustNonHardwareAcceleratedPackagesManager.class) {
            if (sInstance == null) {
                sInstance = (HwCustNonHardwareAcceleratedPackagesManager) HwCustUtils.createObj(HwCustNonHardwareAcceleratedPackagesManager.class, new Object[0]);
            } else if (!(mFlag || ServiceManager.getService("nonhardaccelpkgs") == null)) {
                sInstance = (HwCustNonHardwareAcceleratedPackagesManager) HwCustUtils.createObj(HwCustNonHardwareAcceleratedPackagesManager.class, new Object[0]);
                mFlag = true;
            }
            hwCustNonHardwareAcceleratedPackagesManager = sInstance;
        }
        return hwCustNonHardwareAcceleratedPackagesManager;
    }

    public boolean shouldForceEnabled(ActivityInfo ai, ComponentName instrumentationClass) {
        return false;
    }

    public void handlePackageAdded(String pkgName, boolean updated) {
    }

    public void handlePackageRemoved(String pkgName, boolean removed) {
    }

    public void setForceEnabled(String pkgName, boolean force) {
    }

    public boolean getForceEnabled(String pkgName) {
        return false;
    }

    public boolean hasPackage(String pkgName) {
        return false;
    }

    public void removePackage(String pkgName) {
    }
}
