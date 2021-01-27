package com.huawei.android.content.pm;

import android.app.ActivityThread;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.Log;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PackageManagerEx {
    public static final int DELETE_ALL_USERS = 2;
    public static final int DPERMISSION_DEFAULT = 0;
    public static final int DPERMISSION_DENY = 2;
    public static final int DPERMISSION_GRANT = 1;
    public static final int EXCLUSIVE_INSTALL = 4;
    public static final int FLAG_PERMISSION_GRANTED_BY_DEFAULT = 32;
    public static final int FLAG_PERMISSION_POLICY_FIXED = 4;
    public static final int FLAG_PERMISSION_REVOKE_ON_UPGRADE = 8;
    public static final int FLAG_PERMISSION_SYSTEM_FIXED = 16;
    public static final int FLAG_PERMISSION_USER_FIXED = 2;
    public static final int FLAG_PERMISSION_USER_SET = 1;
    public static final int INSTALL_REASON_USER_MDM = 9;
    public static final int INSTALL_REPLACE_EXISTING = 2;
    public static final int INSTALL_SUCCEEDED = 1;
    private static final int INVALID_VALUE = -1;
    public static final int MIGRATE_CE_DIR = 2;
    public static final int MIGRATE_DE_DIR = 1;
    private static final String TAG = "PackageManagerEx";
    private static boolean sIsMapleEnv = false;
    private static boolean sIsMapleEnvFlag = false;
    private HwOnPermissionsChangedListener mHwOnPermissionsChangedListener = null;
    private PackageManager.OnPermissionsChangedListener mPermissionChangeListener = new PackageManager.OnPermissionsChangedListener() {
        /* class com.huawei.android.content.pm.PackageManagerEx.AnonymousClass1 */

        public void onPermissionsChanged(int uid) {
            if (PackageManagerEx.this.mHwOnPermissionsChangedListener != null) {
                PackageManagerEx.this.mHwOnPermissionsChangedListener.onPermissionsChanged(uid);
            }
        }
    };

    public interface HwOnPermissionsChangedListener {
        void onPermissionsChanged(int i);
    }

    public static void deletePackage(PackageManager packageManager, String packageName, IPackageDeleteObserverEx observer, int flags) {
        packageManager.deletePackage(packageName, observer.getPackageDeleteObserver(), flags);
    }

    public static void grantRuntimePermission(PackageManager packageManager, String packageName, String permissionName, UserHandle user) {
        packageManager.grantRuntimePermission(packageName, permissionName, user);
    }

    public static void revokeRuntimePermission(PackageManager pm, String packageName, String permissionName, UserHandle user) {
        pm.revokeRuntimePermission(packageName, permissionName, user);
    }

    @Deprecated
    public static void installPackage(PackageManager pm, Uri packageUri, IPackageInstallObserverEx observer, int flags, String installerPackageName) {
        Log.e(TAG, "installPackage is removed!");
    }

    public static boolean getBlockUninstallForUser(String packageName, int userId) throws RemoteException {
        return ActivityThread.getPackageManager().getBlockUninstallForUser(packageName, userId);
    }

    public static ResolveInfo resolveIntent(Intent intent, String resolvedType, int flags, int userId) throws RemoteException {
        return ActivityThread.getPackageManager().resolveIntent(intent, resolvedType, flags, userId);
    }

    public static void deletePackageAsUser(PackageManager pm, String packageName, IPackageDeleteObserverEx observer, int flags, int userId) {
        pm.deletePackageAsUser(packageName, observer.getPackageDeleteObserver(), flags, userId);
    }

    public static ApplicationInfo getApplicationInfoAsUser(PackageManager pm, String packageName, int flags, int userId) throws PackageManager.NameNotFoundException {
        return pm.getApplicationInfoAsUser(packageName, flags, userId);
    }

    public static ResolveInfo resolveActivityAsUser(PackageManager pm, Intent intent, int flags, int userId) {
        return pm.resolveActivityAsUser(intent, flags, userId);
    }

    public static PackageInfo getPackageInfoAsUser(PackageManager pm, String packageName, int flags, int userId) throws PackageManager.NameNotFoundException {
        return pm.getPackageInfoAsUser(packageName, flags, userId);
    }

    public static void flushPackageRestrictionsAsUser(PackageManager pm, int userId) {
        if (pm != null) {
            pm.flushPackageRestrictionsAsUser(userId);
        }
    }

    public static boolean shouldShowRequestPermissionRationale(PackageManager pm, String permission) {
        return pm.shouldShowRequestPermissionRationale(permission);
    }

    public static int getPermissionFlags(PackageManager pm, String permissionName, String packageName, UserHandle user) {
        return pm.getPermissionFlags(permissionName, packageName, user);
    }

    public static void updatePermissionFlags(PackageManager pm, String permissionName, String packageName, int flagMask, int flagValues, UserHandle user) {
        pm.updatePermissionFlags(permissionName, packageName, flagMask, flagValues, user);
    }

    public static boolean isPermissionRemoved(PermissionInfo info) {
        return (info == null || (info.flags & 2) == 0) ? false : true;
    }

    public static List<PackageInfo> getInstalledPackagesAsUser(PackageManager pm, int flags, int userId) {
        return pm.getInstalledPackagesAsUser(flags, userId);
    }

    public static int getUidForSharedUser(PackageManager pm, String sharedUserName) throws PackageManager.NameNotFoundException {
        return pm.getUidForSharedUser(sharedUserName);
    }

    public static void deleteApplicationCacheFiles(PackageManager pm, String pkgName, IPackageDataObserverEx packageDataObserver) {
        pm.deleteApplicationCacheFiles(pkgName, packageDataObserver.getIPackageDataObserver());
    }

    public static List<ProviderInfo> queryContentProviders(PackageManager pm, String processName, int uid, int flags, String metaDataKey) {
        if (pm != null) {
            return pm.queryContentProviders(processName, uid, flags, metaDataKey);
        }
        return Collections.emptyList();
    }

    public static boolean isPackageAvailable(PackageManager pm, String packageName) {
        return pm.isPackageAvailable(packageName);
    }

    public static int installExistingPackageAsUser(PackageManager pm, String packageName, int userId) throws PackageManager.NameNotFoundException {
        return pm.installExistingPackageAsUser(packageName, userId);
    }

    public static List<ResolveInfo> queryIntentActivitiesAsUser(PackageManager pm, Intent intent, int flags, int userId) {
        return pm.queryIntentActivitiesAsUser(intent, flags, userId);
    }

    public static boolean isMapleEnv() {
        if (sIsMapleEnvFlag) {
            return sIsMapleEnv;
        }
        try {
            sIsMapleEnv = HwPackageManager.getService().isMapleEnv();
            sIsMapleEnvFlag = true;
            return sIsMapleEnv;
        } catch (RemoteException e) {
            return false;
        }
    }

    public void addHwOnPermissionsChangedListener(PackageManager pm, HwOnPermissionsChangedListener listener) {
        if (listener == null) {
            Log.e(TAG, "addHwOnPermissionsChangedListener listener is null!");
            return;
        }
        this.mHwOnPermissionsChangedListener = listener;
        pm.addOnPermissionsChangeListener(this.mPermissionChangeListener);
    }

    public void removeHwOnPermissionsChangedListener(PackageManager pm, HwOnPermissionsChangedListener listener) {
        HwOnPermissionsChangedListener hwOnPermissionsChangedListener = this.mHwOnPermissionsChangedListener;
        if (hwOnPermissionsChangedListener == null || this.mPermissionChangeListener == null) {
            Log.e(TAG, "removeHwOnPermissionsChangedListener mPermissionChangeListener is null!");
        } else if (listener != null && listener.equals(hwOnPermissionsChangedListener)) {
            pm.removeOnPermissionsChangeListener(this.mPermissionChangeListener);
            this.mHwOnPermissionsChangedListener = null;
        }
    }

    public static String getBackgroundPermissionName(PermissionInfo permissionInfo) {
        if (permissionInfo != null) {
            return permissionInfo.backgroundPermission;
        }
        return "";
    }

    public static List<String> getSystemWhiteList(String type) {
        try {
            return HwPackageManager.getService().getSystemWhiteList(type);
        } catch (RemoteException e) {
            Log.e(TAG, "getSystemWhiteList Fail");
            return null;
        }
    }

    public static boolean shouldSkipTriggerFreeform(String pkgName, int userId) {
        try {
            return HwPackageManager.getService().shouldSkipTriggerFreeform(pkgName, userId);
        } catch (RemoteException e) {
            Log.e(TAG, "shouldSkipTriggerFreeform fail!");
            return false;
        }
    }

    public static int getPrivilegeAppType(String pkgName) {
        IHwPackageManager pms;
        if (!TextUtils.isEmpty(pkgName) && (pms = HwPackageManager.getService()) != null) {
            try {
                return pms.getPrivilegeAppType(pkgName);
            } catch (RemoteException e) {
                Log.e(TAG, "getPrivilegeAppType failed " + e.getMessage());
            }
        }
        return -1;
    }

    public static String getPermissionControllerPackageName(PackageManager pm) {
        if (pm != null) {
            return pm.getPermissionControllerPackageName();
        }
        return "";
    }

    public static final int getPermissionReviewRequiredFlag() {
        return 64;
    }

    public static int installReasonForNoG(String packageName) {
        IPackageManager pm = ActivityThread.getPackageManager();
        if (pm == null) {
            Log.i(TAG, "installReasonForNoG: package manager is null for " + packageName);
            return 0;
        }
        try {
            return pm.getInstallReason(packageName, UserHandle.myUserId());
        } catch (RemoteException e) {
            Log.e(TAG, "installReasonForNoG failed " + e.getMessage());
            return 0;
        }
    }

    public static Bundle[] canGrantDPermissions(Bundle[] bundles) {
        if (bundles == null || bundles.length == 0 || bundles.length > 64) {
            Log.e(TAG, "canGrantDPermissions bundle is invalid");
            return new Bundle[0];
        }
        Bundle[] resultBundles = new Bundle[bundles.length];
        try {
            return HwPackageManager.getService().canGrantDPermissions(bundles);
        } catch (RemoteException e) {
            Log.e(TAG, "canGrantDPermissions failed " + e.getClass());
            return resultBundles;
        }
    }

    public static Map<String, String> getHwRenamedPackages(int flags) {
        Map<String, String> renamedPackages = new HashMap<>();
        IHwPackageManager pms = HwPackageManager.getService();
        if (pms != null) {
            try {
                return pms.getHwRenamedPackages(flags);
            } catch (RemoteException e) {
                Log.e(TAG, "getHwRenamedPackages failed " + e.getMessage());
            }
        }
        return renamedPackages;
    }
}
