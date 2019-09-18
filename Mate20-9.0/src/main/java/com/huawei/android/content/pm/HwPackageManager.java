package com.huawei.android.content.pm;

import android.content.pm.IPackageManager;
import android.os.IBackupSessionCallback;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import android.util.Singleton;
import com.huawei.android.content.pm.IHwPackageManager;
import java.util.ArrayList;
import java.util.List;

public class HwPackageManager {
    private static final Singleton<IHwPackageManager> IPackageManagerSingleton = new Singleton<IHwPackageManager>() {
        /* access modifiers changed from: protected */
        public IHwPackageManager create() {
            try {
                return IHwPackageManager.Stub.asInterface(IPackageManager.Stub.asInterface(ServiceManager.getService("package")).getHwInnerService());
            } catch (RemoteException e) {
                Log.e(HwPackageManager.TAG, "IHwPackageManager create() fail: " + e);
                return null;
            }
        }
    };
    private static final String TAG = "HwPackageManager";

    public static IHwPackageManager getService() {
        return (IHwPackageManager) IPackageManagerSingleton.get();
    }

    public static boolean isPerfOptEnable(String packageName, int optType) {
        try {
            return getService().isPerfOptEnable(packageName, optType);
        } catch (RemoteException e) {
            Log.e(TAG, "isPerfOptEnable failed: catch RemoteException!");
            return false;
        }
    }

    public static int getAppUseNotchMode(String packageName) {
        try {
            return getService().getAppUseNotchMode(packageName);
        } catch (RemoteException e) {
            Log.e(TAG, "getAppUseNotchMode failed " + e.getMessage());
            return -1;
        }
    }

    public static void setAppUseNotchMode(String packageName, int mode) {
        try {
            getService().setAppUseNotchMode(packageName, mode);
        } catch (RemoteException e) {
            Log.e(TAG, "setAppUseNotchMode failed " + e.getMessage());
        }
    }

    public static void setAppCanUninstall(String packageName, boolean canUninstall) {
        try {
            getService().setAppCanUninstall(packageName, canUninstall);
        } catch (RemoteException e) {
            Log.e(TAG, "setAppCanUninstall failed : packageName = " + packageName + e.getMessage());
        }
    }

    public static boolean setApplicationMaxAspectRatio(String packageName, float ar) {
        try {
            return getService().setApplicationMaxAspectRatio(packageName, ar);
        } catch (RemoteException e) {
            Log.e(TAG, "setApplicationMaxAspectRatio failed. " + e.getMessage());
            return false;
        }
    }

    public static float getApplicationMaxAspectRatio(String packageName) {
        try {
            return getService().getApplicationMaxAspectRatio(packageName);
        } catch (RemoteException e) {
            Log.e(TAG, "getApplicationMaxAspectRatio failed.  " + e.getMessage());
            return 0.0f;
        }
    }

    public static boolean setApplicationAspectRatio(String packageName, String aspectName, float ar) {
        try {
            return getService().setApplicationAspectRatio(packageName, aspectName, ar);
        } catch (RemoteException e) {
            Log.e(TAG, "setApplicationMaxAspectRatio failed. " + e.getMessage());
            return false;
        }
    }

    public static float getApplicationAspectRatio(String packageName, String aspectName) {
        try {
            return getService().getApplicationAspectRatio(packageName, aspectName);
        } catch (RemoteException e) {
            Log.e(TAG, "getApplicationMaxAspectRatio failed.  " + e.getMessage());
            return 0.0f;
        }
    }

    public static List<String> getPreinstalledApkList() {
        try {
            return getService().getPreinstalledApkList();
        } catch (RemoteException e) {
            Log.e(TAG, "getPreinstalledApkList failed.  " + e.getMessage());
            return new ArrayList();
        }
    }

    public static List<String> getHwPublicityAppList() {
        try {
            return getService().getHwPublicityAppList();
        } catch (RemoteException e) {
            Log.e(TAG, "getHwPublicityAppList failed.  " + e.getMessage());
            return new ArrayList();
        }
    }

    public static ParcelFileDescriptor getHwPublicityAppParcelFileDescriptor() {
        try {
            return getService().getHwPublicityAppParcelFileDescriptor();
        } catch (RemoteException e) {
            Log.e(TAG, "getHwPublicityAppParcelFileDescriptor failed.  " + e.getMessage());
            return null;
        }
    }

    public static int startBackupSession(IBackupSessionCallback callback) {
        try {
            return getService().startBackupSession(callback);
        } catch (RemoteException e) {
            Log.e(TAG, "startBackupSession failed " + e.getMessage());
            return -1;
        }
    }

    public static int executeBackupTask(int sessionId, String taskCmd) {
        try {
            return getService().executeBackupTask(sessionId, taskCmd);
        } catch (RemoteException e) {
            Log.e(TAG, "executeBackupTask failed " + e.getMessage());
            return -1;
        }
    }

    public static int finishBackupSession(int sessionId) {
        try {
            return getService().finishBackupSession(sessionId);
        } catch (RemoteException e) {
            Log.e(TAG, "finishBackupSession failed " + e.getMessage());
            return -1;
        }
    }

    public static String getResourcePackageNameByIcon(String pkgName, int icon, int userId) {
        try {
            return getService().getResourcePackageNameByIcon(pkgName, icon, userId);
        } catch (RemoteException e) {
            Log.e(TAG, "getResourcePackageNameByIcon failed " + e.getMessage());
            return null;
        }
    }

    public static boolean scanInstallApk(String apkFile) {
        try {
            return getService().scanInstallApk(apkFile);
        } catch (RemoteException e) {
            Log.e(TAG, "scanInstallApk failed " + e.getMessage());
            return false;
        }
    }

    public static List<String> getScanInstallList() {
        try {
            return getService().getScanInstallList();
        } catch (RemoteException e) {
            Log.e(TAG, "getScanInstallList failed " + e.getMessage());
            return new ArrayList();
        }
    }

    public static void setHdbKey(String key) {
        try {
            getService().setHdbKey(key);
        } catch (RemoteException e) {
            Log.e(TAG, "setHdbKey failed " + e.getMessage());
        }
    }

    public static boolean pmInstallHwTheme(String themePath, boolean setwallpaper, int userId) {
        try {
            return getService().pmInstallHwTheme(themePath, setwallpaper, userId);
        } catch (RemoteException e) {
            Log.e(TAG, "pmInstallHwTheme failed " + e.getMessage());
            return false;
        }
    }

    public static String readMspesFile(String fileName) {
        try {
            return getService().readMspesFile(fileName);
        } catch (RemoteException e) {
            Log.e(TAG, "readMspesFile failed, fileName = " + fileName + " " + e.getMessage());
            return null;
        }
    }

    public static boolean writeMspesFile(String fileName, String content) {
        try {
            return getService().writeMspesFile(fileName, content);
        } catch (RemoteException e) {
            Log.e(TAG, "writeMspesFile failed, fileName = " + fileName + " " + e.getMessage());
            return false;
        }
    }

    public static String getMspesOEMConfig() {
        try {
            return getService().getMspesOEMConfig();
        } catch (RemoteException e) {
            Log.e(TAG, "getMspesOEMConfig failed " + e.getMessage());
            return null;
        }
    }

    public static int updateMspesOEMConfig(String src) {
        try {
            return getService().updateMspesOEMConfig(src);
        } catch (RemoteException e) {
            Log.e(TAG, "updateMspesOEMConfig failed " + e.getMessage());
            return -1;
        }
    }
}
