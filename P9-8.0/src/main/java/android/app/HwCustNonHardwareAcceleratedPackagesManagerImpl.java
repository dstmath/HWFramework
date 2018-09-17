package android.app;

import android.app.INonHardwareAcceleratedPackagesManager.Stub;
import android.content.ComponentName;
import android.content.HwCustContext;
import android.content.pm.ActivityInfo;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.util.Log;
import android.util.Slog;

public class HwCustNonHardwareAcceleratedPackagesManagerImpl extends HwCustNonHardwareAcceleratedPackagesManager {
    private static boolean HWDBG = false;
    private static boolean HWFLOW = false;
    private static final String TAG = "NonHardAccelPkgs";
    private INonHardwareAcceleratedPackagesManager mService;

    static {
        boolean z = true;
        boolean isLoggable = !Log.HWINFO ? Log.HWModuleLog ? Log.isLoggable(TAG, 4) : false : true;
        HWFLOW = isLoggable;
        if (!Log.HWLog) {
            z = Log.HWModuleLog ? Log.isLoggable(TAG, 3) : false;
        }
        HWDBG = z;
    }

    private void getService() {
        this.mService = Stub.asInterface(ServiceManager.getService(HwCustContext.NON_HARD_ACCEL_PKGS_SERVICE));
    }

    public HwCustNonHardwareAcceleratedPackagesManagerImpl() {
        getService();
    }

    public boolean shouldForceEnabled(ActivityInfo ai, ComponentName instrumentationClass) {
        if (!UserHandle.isApp(ai.applicationInfo.uid) || ai.applicationInfo.targetSdkVersion < 5 || (ai.flags & 512) != 0 || instrumentationClass != null) {
            return false;
        }
        String pkgName = ai.applicationInfo.packageName;
        boolean ret = false;
        if (this.mService != null) {
            try {
                if (this.mService.hasPackage(pkgName)) {
                    ret = this.mService.getForceEnabled(pkgName);
                } else {
                    this.mService.setForceEnabled(pkgName, false);
                }
            } catch (RemoteException e) {
                Log.e(TAG, "shouldForceEnabled: RemoteException", e);
                getService();
            }
        }
        if (HWDBG) {
            Slog.d(TAG, "shouldForceEnabled: " + pkgName + " " + ret);
        }
        return ret;
    }

    public void handlePackageAdded(String pkgName, boolean updated) {
        if (updated && HWDBG) {
            Slog.d(TAG, "handlePackageAdded: ignore and keep existing record upon upgrading");
        }
    }

    public void handlePackageRemoved(String pkgName, boolean removed) {
        if (removed && HWDBG) {
            Slog.d(TAG, "handlePackageRemoved: ignore and keep existing record upon removing");
        }
    }

    public void setForceEnabled(String pkgName, boolean force) {
        if (this.mService != null) {
            try {
                this.mService.setForceEnabled(pkgName, force);
            } catch (RemoteException e) {
                Log.e(TAG, "setForceEnabled: RemoteException", e);
                getService();
            }
        }
    }

    public boolean getForceEnabled(String pkgName) {
        if (this.mService != null) {
            try {
                return this.mService.getForceEnabled(pkgName);
            } catch (RemoteException e) {
                Log.e(TAG, "getForceEnabled: RemoteException", e);
                getService();
            }
        }
        return false;
    }

    public boolean hasPackage(String pkgName) {
        if (this.mService != null) {
            try {
                return this.mService.hasPackage(pkgName);
            } catch (RemoteException e) {
                Log.e(TAG, "hasPackage: RemoteException", e);
                getService();
            }
        }
        return false;
    }

    public void removePackage(String pkgName) {
        if (this.mService != null) {
            try {
                this.mService.removePackage(pkgName);
            } catch (RemoteException e) {
                Log.e(TAG, "removePackage: RemoteException", e);
                getService();
            }
        }
    }
}
