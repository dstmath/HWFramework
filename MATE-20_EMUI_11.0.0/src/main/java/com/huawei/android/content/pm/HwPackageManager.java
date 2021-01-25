package com.huawei.android.content.pm;

import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.FeatureInfo;
import android.content.pm.IPackageManager;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.Binder;
import android.os.IBackupSessionCallback;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Singleton;
import com.huawei.android.content.pm.IHwPackageManager;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HwPackageManager {
    public static final int HW_FLAG_DELAY_DEXOPT = 8;
    public static final int HW_FLAG_DISABLE_APP_VERIFY = 4;
    public static final int HW_FLAG_DONT_KILL_PROCESS = 1;
    public static final int HW_FLAG_ONLY_PERFORM_BASE_DEXOPT = 2;
    private static final int INVALID_VALUE = -1;
    private static final Singleton<IHwPackageManager> PACKAGE_MANAGER_SINGLETON = new Singleton<IHwPackageManager>() {
        /* class com.huawei.android.content.pm.HwPackageManager.AnonymousClass1 */

        /* access modifiers changed from: protected */
        @Override // android.util.Singleton
        public IHwPackageManager create() {
            try {
                IPackageManager pms = IPackageManager.Stub.asInterface(ServiceManager.getService("package"));
                if (pms != null) {
                    return IHwPackageManager.Stub.asInterface(pms.getHwInnerService());
                }
                return null;
            } catch (RemoteException e) {
                Log.e(HwPackageManager.TAG, "IHwPackageManager create() failed");
                return null;
            }
        }
    };
    private static final String TAG = "HwPackageManager";

    private HwPackageManager() {
    }

    public static IHwPackageManager getService() {
        return PACKAGE_MANAGER_SINGLETON.get();
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

    public static int getAppUseSideMode(String packageName) {
        if (getService() == null) {
            return -1;
        }
        try {
            return getService().getAppUseSideMode(packageName);
        } catch (RemoteException e) {
            Log.e(TAG, "getAppUseSideMode failed " + e.getMessage());
            return -1;
        }
    }

    public static void setAppCanUninstall(String packageName, boolean canUninstall) {
        try {
            getService().setAppCanUninstall(packageName, canUninstall);
        } catch (RemoteException e) {
            Log.e(TAG, "setAppCanUninstall failed : packageName = " + packageName + e.getMessage());
        }
    }

    public static boolean setApplicationAspectRatio(String packageName, String aspectName, float ar) {
        try {
            return getService().setApplicationAspectRatio(packageName, aspectName, ar);
        } catch (RemoteException e) {
            Log.e(TAG, "set" + aspectName + "failed. " + e.getMessage());
            return false;
        }
    }

    public static float getApplicationAspectRatio(String packageName, String aspectName) {
        if (!UserHandle.isIsolated(Binder.getCallingUid())) {
            try {
                return getService().getApplicationAspectRatio(packageName, aspectName);
            } catch (RemoteException e) {
                Log.e(TAG, "get" + aspectName + "failed.  " + e.getMessage());
                return 0.0f;
            }
        } else {
            throw new SecurityException("isolated process not allowed to call ");
        }
    }

    public static List<String> getPreinstalledApkList() {
        if (!UserHandle.isIsolated(Binder.getCallingUid())) {
            try {
                return getService().getPreinstalledApkList();
            } catch (RemoteException e) {
                Log.e(TAG, "getPreinstalledApkList failed.  " + e.getMessage());
                return Collections.emptyList();
            }
        } else {
            throw new SecurityException("isolated process not allowed to call ");
        }
    }

    public static List<String> getHwPublicityAppList() {
        if (!UserHandle.isIsolated(Binder.getCallingUid())) {
            try {
                return getService().getHwPublicityAppList();
            } catch (RemoteException e) {
                Log.e(TAG, "getHwPublicityAppList failed.  " + e.getMessage());
                return Collections.emptyList();
            }
        } else {
            throw new SecurityException("isolated process not allowed to call ");
        }
    }

    public static ParcelFileDescriptor getHwPublicityAppParcelFileDescriptor() {
        if (!UserHandle.isIsolated(Binder.getCallingUid())) {
            try {
                return getService().getHwPublicityAppParcelFileDescriptor();
            } catch (RemoteException e) {
                Log.e(TAG, "getHwPublicityAppParcelFileDescriptor failed.  " + e.getMessage());
                return null;
            }
        } else {
            throw new SecurityException("isolated process not allowed to call ");
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
        if (!UserHandle.isIsolated(Binder.getCallingUid())) {
            try {
                return getService().getResourcePackageNameByIcon(pkgName, icon, userId);
            } catch (RemoteException e) {
                Log.e(TAG, "getResourcePackageNameByIcon failed " + e.getMessage());
                return null;
            }
        } else {
            throw new SecurityException("isolated process not allowed to call ");
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
        if (!UserHandle.isIsolated(Binder.getCallingUid())) {
            try {
                return getService().getScanInstallList();
            } catch (RemoteException e) {
                Log.e(TAG, "getScanInstallList failed " + e.getMessage());
                return Collections.emptyList();
            }
        } else {
            throw new SecurityException("isolated process not allowed to call ");
        }
    }

    public static void setHdbKey(String key) {
        if (UserHandle.isApp(Binder.getCallingUid()) || UserHandle.isIsolated(Binder.getCallingUid())) {
            throw new SecurityException("app or isolated process not allowed to call ");
        }
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
            Log.e(TAG, "readMspesFile failed, fileName = " + fileName + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + e.getMessage());
            return null;
        }
    }

    public static boolean writeMspesFile(String fileName, String content) {
        try {
            return getService().writeMspesFile(fileName, content);
        } catch (RemoteException e) {
            Log.e(TAG, "writeMspesFile failed, fileName = " + fileName + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + e.getMessage());
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

    public static int getPrivilegeAppType(String pkgName) {
        if (TextUtils.isEmpty(pkgName)) {
            return -1;
        }
        try {
            return getService().getPrivilegeAppType(pkgName);
        } catch (RemoteException e) {
            Log.e(TAG, "getPrivilegeAppType failed " + e.getMessage());
            return -1;
        }
    }

    public static void clearPreferredActivityAsUser(IntentFilter filter, int match, ComponentName[] set, ComponentName activity, int userId) {
        try {
            getService().clearPreferredActivityAsUser(filter, match, set, activity, userId);
        } catch (RemoteException e) {
            Log.e(TAG, "clearPreferredActivityAsUser failed " + e.getMessage());
        }
    }

    public static boolean setForceDarkSetting(List<String> packageNames, int forceDarkMode) {
        try {
            return getService().setForceDarkSetting(packageNames, forceDarkMode);
        } catch (RemoteException e) {
            Log.e(TAG, "setForceDarkSetting failed. " + e.getMessage());
            return false;
        }
    }

    public static int getForceDarkSetting(String packageName) {
        try {
            return getService().getForceDarkSetting(packageName);
        } catch (RemoteException e) {
            Log.e(TAG, "getForceDarkSetting failed.  " + e.getMessage());
            return 2;
        }
    }

    public static Map<String, String> getHwRenamedPackages(int flags) {
        Map<String, String> renamedPackages = new HashMap<>();
        try {
            return getService().getHwRenamedPackages(flags);
        } catch (RemoteException e) {
            Log.e(TAG, "getHwRenamedPackages failed.  " + e.getMessage());
            return renamedPackages;
        }
    }

    public static List<ApplicationInfo> getClusterApplications(int flags, int clusterMask, boolean isOnlyDisabled) {
        try {
            return getService().getClusterApplications(flags, clusterMask, isOnlyDisabled);
        } catch (RemoteException e) {
            Log.e(TAG, "getClusterApplications RemoteException ");
            return Collections.emptyList();
        }
    }

    public static List<HwHepPackageInfo> getInstalledHep(int flags) {
        try {
            return getService().getInstalledHep(flags);
        } catch (RemoteException e) {
            Log.e(TAG, "getInstalledHep RemoteException ");
            return Collections.emptyList();
        }
    }

    public static int uninstallHep(String packageName, int flags) {
        try {
            return getService().uninstallHep(packageName, flags);
        } catch (RemoteException e) {
            Log.e(TAG, "uninstallHep RemoteException ");
            return -1;
        }
    }

    public static void setVersionMatchFlag(int deviceType, int version, boolean isMatchSuccess) {
        try {
            getService().setVersionMatchFlag(deviceType, version, isMatchSuccess);
        } catch (RemoteException e) {
            Log.e(TAG, "setVersionMatchFlag failed." + e.getMessage());
        }
    }

    public static boolean getVersionMatchFlag(int deviceType, int version) {
        try {
            return getService().getVersionMatchFlag(deviceType, version);
        } catch (RemoteException e) {
            Log.e(TAG, "getVersionMatchFlag failed." + e.getMessage());
            return false;
        }
    }

    public static void setOpenFileResult(Intent intent, int retCode) {
        try {
            getService().setOpenFileResult(intent, retCode);
        } catch (RemoteException e) {
            Log.e(TAG, "setOpenFileResult failed." + e.getMessage());
        }
    }

    public static int getOpenFileResult(Intent intent) {
        try {
            return getService().getOpenFileResult(intent);
        } catch (RemoteException e) {
            Log.e(TAG, "getCanOpenFileFlag failed." + e.getMessage());
            return -1;
        }
    }

    public static int getDisplayChangeAppRestartConfig(int type, String pkgName) {
        if (pkgName == null) {
            return -1;
        }
        try {
            return getService().getDisplayChangeAppRestartConfig(type, pkgName);
        } catch (RemoteException e) {
            Log.e(TAG, "getDisplayChangeAppRestartConfig failed. " + e.getMessage());
            return -1;
        }
    }

    public static FeatureInfo[] getHwSystemAvailableFeatures() {
        try {
            IHwPackageManager pms = getService();
            if (pms == null) {
                return new FeatureInfo[0];
            }
            return pms.getHwSystemAvailableFeatures();
        } catch (RemoteException e) {
            Log.e(TAG, "getHwSystemAvailableFeatures RemoteException");
            return new FeatureInfo[0];
        } catch (Exception e2) {
            Log.e(TAG, "getHwSystemAvailableFeatures Exception");
            return new FeatureInfo[0];
        }
    }

    public static boolean hasHwSystemFeature(String featureName, int version) {
        try {
            IHwPackageManager pms = getService();
            if (!TextUtils.isEmpty(featureName)) {
                if (pms != null) {
                    return pms.hasHwSystemFeature(featureName, version);
                }
            }
            return false;
        } catch (RemoteException e) {
            Log.e(TAG, "hasHwSystemFeature RemoteException");
            return false;
        } catch (Exception e2) {
            Log.e(TAG, "hasHwSystemFeature Exception");
            return false;
        }
    }
}
