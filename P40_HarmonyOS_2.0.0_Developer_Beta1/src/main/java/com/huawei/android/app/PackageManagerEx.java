package com.huawei.android.app;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.FeatureInfo;
import android.content.pm.IPackageManager;
import android.content.pm.PackageInstaller;
import android.os.Binder;
import android.os.IBackupSessionCallback;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import android.util.Singleton;
import com.huawei.android.app.PresetPackage;
import com.huawei.android.content.pm.HwHepPackageInfo;
import com.huawei.android.content.pm.HwPackageManager;
import com.huawei.android.content.pm.HwPresetPackage;
import com.huawei.android.content.pm.IHwPackageManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PackageManagerEx {
    public static final int APP_USE_SIDE_MODE_EXPANDED = 1;
    public static final int APP_USE_SIDE_MODE_UNEXPANDED = 0;
    public static final int CLUSTER_MASK_ALL = 3;
    public static final int CLUSTER_MASK_BUNDLE = 1;
    public static final int CLUSTER_MASK_PLUGIN = 2;
    private static final Singleton<IPackageManager> DEFAULT = new Singleton<IPackageManager>() {
        /* class com.huawei.android.app.PackageManagerEx.AnonymousClass1 */

        /* access modifiers changed from: protected */
        public IPackageManager create() {
            return IPackageManager.Stub.asInterface(ServiceManager.getService("package"));
        }
    };
    public static final int HW_FLAG_DELAY_DEXOPT = 8;
    public static final int HW_FLAG_DISABLE_APP_VERIFY = 4;
    public static final int HW_FLAG_DONT_KILL_PROCESS = 1;
    public static final int HW_FLAG_ONLY_PERFORM_BASE_DEXOPT = 2;
    private static final int INVALID_VALUE = -1;
    private static final String IPACKAGE_MANAGER_DESCRIPTOR = "huawei.com.android.server.IPackageManager";
    private static final String TAG = "PackageManagerEx";
    public static final int TRANSACTION_CODE_FILE_BACKUP_EXECUTE_TASK = 1019;
    public static final int TRANSACTION_CODE_FILE_BACKUP_FINISH_SESSION = 1020;
    public static final int TRANSACTION_CODE_FILE_BACKUP_START_SESSION = 1018;
    public static final int TRANSACTION_CODE_GET_HDB_KEY = 1011;
    public static final int TRANSACTION_CODE_GET_IM_AND_VIDEO_APP_LIST = 1022;
    public static final int TRANSACTION_CODE_GET_MAX_ASPECT_RATIO = 1013;
    private static final int TRANSACTION_CODE_GET_PREINSTALLED_APK_LIST = 1007;
    public static final int TRANSACTION_CODE_GET_PUBLICITY_DESCRIPTOR = 1015;
    public static final int TRANSACTION_CODE_GET_PUBLICITY_INFO_LIST = 1014;
    @Deprecated
    public static final int TRANSACTION_CODE_GET_SCAN_INSTALL_LIST = 1017;
    private static final int TRANSACTION_CODE_IS_NOTIFICATION_SPLIT = 1021;
    @Deprecated
    public static final int TRANSACTION_CODE_SCAN_INSTALL_APK = 1016;
    @Deprecated
    public static final int TRANSACTION_CODE_SET_HDB_KEY = 1010;
    public static final int TRANSACTION_CODE_SET_MAX_ASPECT_RATIO = 1012;

    public static List<String> getPreinstalledApkList() {
        return HwPackageManager.getPreinstalledApkList();
    }

    private static IPackageManager getDefault() {
        return (IPackageManager) DEFAULT.get();
    }

    @Deprecated
    public static boolean checkGmsCoreUninstalled() {
        return false;
    }

    @Deprecated
    public static void deleteGmsCoreFromUninstalledDelapp() {
    }

    public static void setHdbKey(String key) {
        HwPackageManager.setHdbKey(key);
    }

    public static String getHdbKey() {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        String res = null;
        try {
            data.writeInterfaceToken(IPACKAGE_MANAGER_DESCRIPTOR);
            getDefault().asBinder().transact(TRANSACTION_CODE_GET_HDB_KEY, data, reply, 0);
            reply.readException();
            res = reply.readString();
        } catch (RemoteException e) {
            Log.e(TAG, "failed to getHdbKey");
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
            throw th;
        }
        reply.recycle();
        data.recycle();
        return res;
    }

    public static boolean setApplicationMaxAspectRatio(String packageName, float ar) {
        return HwPackageManager.setApplicationAspectRatio(packageName, "maxAspectRatio", ar);
    }

    public static float getApplicationMaxAspectRatio(String packageName) {
        return HwPackageManager.getApplicationAspectRatio(packageName, "maxAspectRatio");
    }

    public static boolean setForceDarkSetting(List<String> packageNames, int forceDarkMode) {
        return HwPackageManager.setForceDarkSetting(packageNames, forceDarkMode);
    }

    public static int getForceDarkSetting(String packageName) {
        return HwPackageManager.getForceDarkSetting(packageName);
    }

    public static boolean setApplicationAspectRatio(String packageName, String aspectName, float ar) {
        return HwPackageManager.setApplicationAspectRatio(packageName, aspectName, ar);
    }

    public static float getApplicationAspectRatio(String packageName, String aspectName) {
        return HwPackageManager.getApplicationAspectRatio(packageName, aspectName);
    }

    public static ParcelFileDescriptor getHwPublicityAppParcelFileDescriptor() {
        return HwPackageManager.getHwPublicityAppParcelFileDescriptor();
    }

    public static List<String> getHwPublicityAppList() {
        return HwPackageManager.getHwPublicityAppList();
    }

    public static boolean isNotificationAddSplitButton(String pkgName) {
        boolean isButton = false;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IPACKAGE_MANAGER_DESCRIPTOR);
            data.writeString(pkgName);
            boolean z = false;
            getDefault().asBinder().transact(TRANSACTION_CODE_IS_NOTIFICATION_SPLIT, data, reply, 0);
            reply.readException();
            if (reply.readInt() != 0) {
                z = true;
            }
            isButton = z;
        } catch (RemoteException e) {
            Log.e(TAG, "failed to get notification is split by RemoteException");
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
            throw th;
        }
        reply.recycle();
        data.recycle();
        return isButton;
    }

    public static int startBackupSession(IBackupSessionCallback callback) {
        return HwPackageManager.startBackupSession(callback);
    }

    public static int executeBackupTask(int sessionId, String taskCmd) {
        return HwPackageManager.executeBackupTask(sessionId, taskCmd);
    }

    public static int finishBackupSession(int sessionId) {
        return HwPackageManager.finishBackupSession(sessionId);
    }

    public static boolean scanInstallApk(String apkFile) {
        return HwPackageManager.scanInstallApk(apkFile);
    }

    public static List<String> getScanInstallList() {
        return HwPackageManager.getScanInstallList();
    }

    public static List<String> getSupportSplitScreenApps() {
        List<String> apps = new ArrayList<>();
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IPACKAGE_MANAGER_DESCRIPTOR);
            getDefault().asBinder().transact(TRANSACTION_CODE_GET_IM_AND_VIDEO_APP_LIST, data, reply, 0);
            reply.readException();
            reply.readStringList(apps);
        } catch (RemoteException e) {
            Log.e(TAG, "failed to getHwPublicityAppList");
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
            throw th;
        }
        reply.recycle();
        data.recycle();
        return apps;
    }

    public static int getAppUseNotchMode(String packageName) {
        IHwPackageManager pms = HwPackageManager.getService();
        if (pms == null) {
            return -1;
        }
        try {
            return pms.getAppUseNotchMode(packageName);
        } catch (RemoteException e) {
            Log.w(TAG, "getAppUseNotchMode RemoteException");
            return -1;
        }
    }

    public static void setAppUseNotchMode(String packageName, int mode) {
        IHwPackageManager pms = HwPackageManager.getService();
        if (pms != null) {
            try {
                pms.setAppUseNotchMode(packageName, mode);
            } catch (RemoteException e) {
                Log.w(TAG, "setAppUseNotchMode RemoteException");
            }
        }
    }

    public static int getAppUseSideMode(String packageName) {
        IHwPackageManager pms = HwPackageManager.getService();
        if (pms == null) {
            return -1;
        }
        try {
            return pms.getAppUseSideMode(packageName);
        } catch (RemoteException e) {
            Log.w(TAG, "getAppUseSidehMode RemoteException");
            return -1;
        }
    }

    public static void setAppUseSideMode(String packageName, int mode) {
        IHwPackageManager pms = HwPackageManager.getService();
        if (pms != null) {
            try {
                pms.setAppUseSideMode(packageName, mode);
            } catch (RemoteException e) {
                Log.w(TAG, "setAppUseSideMode RemoteException");
            }
        }
    }

    public static boolean setAllAppsUseSideMode(boolean isUse) {
        IHwPackageManager pms = HwPackageManager.getService();
        if (pms == null) {
            return false;
        }
        try {
            return pms.setAllAppsUseSideMode(isUse);
        } catch (RemoteException e) {
            Log.w(TAG, "setAllAppsUseSideMode RemoteException");
            return false;
        }
    }

    public static boolean setAllAppsUseSideModeAndStopApps(List<String> pkgs, boolean isUse) {
        if (pkgs == null || pkgs.isEmpty()) {
            Log.w(TAG, "setAllAppsUseSideMode, pkgs is empty");
            return true;
        }
        IHwPackageManager pms = HwPackageManager.getService();
        if (pms == null) {
            return false;
        }
        try {
            return pms.setAllAppsUseSideModeAndStopApps(pkgs, isUse);
        } catch (RemoteException e) {
            Log.w(TAG, "setAllAppsUseSideMode RemoteException");
            return false;
        }
    }

    public static boolean restoreAllAppsUseSideMode() {
        IHwPackageManager pms = HwPackageManager.getService();
        if (pms == null) {
            return false;
        }
        try {
            return pms.restoreAllAppsUseSideMode();
        } catch (RemoteException e) {
            Log.w(TAG, "restoreAllAppsUseSideMode RemoteException");
            return false;
        }
    }

    public static boolean isAllAppsUseSideMode(List<String> packages) {
        IHwPackageManager pms = HwPackageManager.getService();
        if (pms == null) {
            return false;
        }
        try {
            return pms.isAllAppsUseSideMode(packages);
        } catch (RemoteException e) {
            Log.w(TAG, "isAllAppsUseSideMode RemoteException");
            return false;
        }
    }

    public static void setAppCanUninstall(String packageName, boolean isCanUninstall) {
        HwPackageManager.setAppCanUninstall(packageName, isCanUninstall);
    }

    public static void clearPreferredActivityAsUser(IntentFilter filter, int match, ComponentName[] set, ComponentName activity, int userId) {
        HwPackageManager.clearPreferredActivityAsUser(filter, match, set, activity, userId);
    }

    public static List<ApplicationInfo> getClusterApplications(int flags, int clusterMask, boolean isOnlyDisabled) {
        return HwPackageManager.getClusterApplications(flags, clusterMask, isOnlyDisabled);
    }

    public static List<HepPackageInfo> getInstalledHep(int flags) {
        List<HwHepPackageInfo> queryList = HwPackageManager.getInstalledHep(flags);
        if (queryList == null) {
            return Collections.emptyList();
        }
        List<HepPackageInfo> hepPackagInfoList = new ArrayList<>(queryList.size());
        for (HwHepPackageInfo hwHepPackageInfo : queryList) {
            HepPackageInfo hepPackageInfo = new HepPackageInfo();
            hepPackageInfo.setPackageName(hwHepPackageInfo.getPackageName());
            hepPackageInfo.setPackagePath(hwHepPackageInfo.getPackagePath());
            hepPackageInfo.setVersionCode(hwHepPackageInfo.getVersionCode());
            hepPackageInfo.setStatus(hwHepPackageInfo.getStatus());
            hepPackagInfoList.add(hepPackageInfo);
        }
        return hepPackagInfoList;
    }

    public static PresetPackage getPresetPackage(String packageName) {
        HwPresetPackage queryResult;
        if (packageName == null || packageName.length() == 0 || (queryResult = HwPackageManager.getPresetPackage(packageName)) == null) {
            return null;
        }
        PresetPackage presetPackage = new PresetPackage();
        presetPackage.setPackageName(queryResult.getPackageName());
        presetPackage.setPackagePath(queryResult.getPackagePath());
        presetPackage.setType(PresetPackage.AppType.valueOf(queryResult.getType().ordinal()));
        return presetPackage;
    }

    public static int uninstallHep(String packageName, int flags) {
        return HwPackageManager.uninstallHep(packageName, flags);
    }

    public static void setVersionMatchFlag(int deviceType, int version, boolean isMatchSuccess) {
        HwPackageManager.setVersionMatchFlag(deviceType, version, isMatchSuccess);
    }

    public static boolean getVersionMatchFlag(int deviceType, int version) {
        return HwPackageManager.getVersionMatchFlag(deviceType, version);
    }

    public static void setOpenFileResult(Intent intent, int retCode) {
        HwPackageManager.setOpenFileResult(intent, retCode);
    }

    public static int getOpenFileResult(Intent intent) {
        return HwPackageManager.getOpenFileResult(intent);
    }

    public static int getDisplayChangeAppRestartConfig(int type, String pkgName) {
        IHwPackageManager pms;
        if (!(pkgName == null || (pms = HwPackageManager.getService()) == null)) {
            try {
                return pms.getDisplayChangeAppRestartConfig(type, pkgName);
            } catch (RemoteException e) {
                Log.w(TAG, "getDisplayChangeAppRestartConfig RemoteException");
            }
        }
        return -1;
    }

    public static void setHwInstallFlags(PackageInstaller.SessionParams sessionParams, int installFlags) {
        Log.d(TAG, "setHwInstallFlags:" + installFlags);
        if (sessionParams != null) {
            if ((installFlags & 1) != 0) {
                sessionParams.setDontKillApp(true);
            }
            if ((installFlags & 2) != 0) {
                sessionParams.hwInstallFlags |= 2;
            }
            if ((installFlags & 4) != 0) {
                sessionParams.hwInstallFlags |= 4;
            }
            if ((installFlags & 8) != 0) {
                sessionParams.hwInstallFlags |= 8;
            }
        }
    }

    public static FeatureInfo[] getHwSystemAvailableFeatures() {
        return HwPackageManager.getHwSystemAvailableFeatures();
    }

    public static boolean hasHwSystemFeature(String featureName) {
        if (featureName == null) {
            return false;
        }
        return HwPackageManager.hasHwSystemFeature(featureName, 0);
    }

    public static boolean hasHwSystemFeature(String featureName, int version) {
        if (featureName == null) {
            return false;
        }
        return HwPackageManager.hasHwSystemFeature(featureName, version);
    }

    public static boolean hasSystemSignaturePermission(Context context) {
        if (context != null && context.checkPermission("huawei.android.permission.HW_SIGNATURE_OR_SYSTEM", Binder.getCallingPid(), Binder.getCallingUid()) == 0) {
            return true;
        }
        return false;
    }
}
