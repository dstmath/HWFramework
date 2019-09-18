package com.android.server.pm.permission;

import android.content.Context;
import android.content.pm.PackageManagerInternal;
import android.content.pm.PackageParser;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.metrics.LogMaker;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.ServiceManager;
import android.os.Trace;
import android.os.UserHandle;
import android.os.UserManagerInternal;
import android.os.storage.StorageManager;
import android.os.storage.StorageManagerInternal;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.EventLog;
import android.util.Log;
import android.util.Slog;
import android.util.SparseArray;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.os.RoSystemProperties;
import com.android.internal.util.ArrayUtils;
import com.android.server.HwServiceFactory;
import com.android.server.LocalServices;
import com.android.server.ServiceThread;
import com.android.server.SystemConfig;
import com.android.server.Watchdog;
import com.android.server.pm.HwCustPackageManagerService;
import com.android.server.pm.PackageManagerService;
import com.android.server.pm.PackageManagerServiceUtils;
import com.android.server.pm.PackageSetting;
import com.android.server.pm.SharedUserSetting;
import com.android.server.pm.UserManagerService;
import com.android.server.pm.permission.DefaultPermissionGrantPolicy;
import com.android.server.pm.permission.PermissionManagerInternal;
import com.android.server.pm.permission.PermissionsState;
import huawei.cust.HwCustUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import libcore.util.EmptyArray;

public class PermissionManagerService {
    private static final int[] EMPTY_INT_ARRAY = new int[0];
    private static final int GRANT_DENIED = 1;
    private static final int GRANT_INSTALL = 2;
    private static final int GRANT_RUNTIME = 3;
    private static final int GRANT_UPGRADE = 4;
    private static final int MAX_PERMISSION_TREE_FOOTPRINT = 32768;
    private static final String TAG = "PackageManager";
    private static final int UPDATE_PERMISSIONS_ALL = 1;
    private static final int UPDATE_PERMISSIONS_REPLACE_ALL = 4;
    private static final int UPDATE_PERMISSIONS_REPLACE_PKG = 2;
    private final Context mContext;
    private HwCustPackageManagerService mCustPms = ((HwCustPackageManagerService) HwCustUtils.createObj(HwCustPackageManagerService.class, new Object[0]));
    /* access modifiers changed from: private */
    public final DefaultPermissionGrantPolicy mDefaultPermissionGrantPolicy;
    private final int[] mGlobalGids;
    private final Handler mHandler;
    private final HandlerThread mHandlerThread;
    /* access modifiers changed from: private */
    public final Object mLock;
    private final MetricsLogger mMetricsLogger = new MetricsLogger();
    private final PackageManagerInternal mPackageManagerInt;
    @GuardedBy("mLock")
    private ArraySet<String> mPrivappPermissionsViolations;
    /* access modifiers changed from: private */
    @GuardedBy("mLock")
    public final PermissionSettings mSettings;
    private final SparseArray<ArraySet<String>> mSystemPermissions;
    @GuardedBy("mLock")
    private boolean mSystemReady;
    private final UserManagerInternal mUserManagerInt;

    private class PermissionManagerInternalImpl extends PermissionManagerInternal {
        private PermissionManagerInternalImpl() {
        }

        public void systemReady() {
            PermissionManagerService.this.systemReady();
        }

        public boolean isPermissionsReviewRequired(PackageParser.Package pkg, int userId) {
            return PermissionManagerService.this.isPermissionsReviewRequired(pkg, userId);
        }

        public void revokeRuntimePermissionsIfGroupChanged(PackageParser.Package newPackage, PackageParser.Package oldPackage, ArrayList<String> allPackageNames, PermissionManagerInternal.PermissionCallback permissionCallback) {
            PermissionManagerService.this.revokeRuntimePermissionsIfGroupChanged(newPackage, oldPackage, allPackageNames, permissionCallback);
        }

        public void addAllPermissions(PackageParser.Package pkg, boolean chatty) {
            PermissionManagerService.this.addAllPermissions(pkg, chatty);
        }

        public void addAllPermissionGroups(PackageParser.Package pkg, boolean chatty) {
            PermissionManagerService.this.addAllPermissionGroups(pkg, chatty);
        }

        public void removeAllPermissions(PackageParser.Package pkg, boolean chatty) {
            PermissionManagerService.this.removeAllPermissions(pkg, chatty);
        }

        public boolean addDynamicPermission(PermissionInfo info, boolean async, int callingUid, PermissionManagerInternal.PermissionCallback callback) {
            return PermissionManagerService.this.addDynamicPermission(info, callingUid, callback);
        }

        public void removeDynamicPermission(String permName, int callingUid, PermissionManagerInternal.PermissionCallback callback) {
            PermissionManagerService.this.removeDynamicPermission(permName, callingUid, callback);
        }

        public void grantRuntimePermission(String permName, String packageName, boolean overridePolicy, int callingUid, int userId, PermissionManagerInternal.PermissionCallback callback) {
            PermissionManagerService.this.grantRuntimePermission(permName, packageName, overridePolicy, callingUid, userId, callback);
        }

        public void grantRequestedRuntimePermissions(PackageParser.Package pkg, int[] userIds, String[] grantedPermissions, int callingUid, PermissionManagerInternal.PermissionCallback callback) {
            PermissionManagerService.this.grantRequestedRuntimePermissions(pkg, userIds, grantedPermissions, callingUid, callback);
        }

        public void grantRuntimePermissionsGrantedToDisabledPackage(PackageParser.Package pkg, int callingUid, PermissionManagerInternal.PermissionCallback callback) {
            PermissionManagerService.this.grantRuntimePermissionsGrantedToDisabledPackageLocked(pkg, callingUid, callback);
        }

        public void revokeRuntimePermission(String permName, String packageName, boolean overridePolicy, int callingUid, int userId, PermissionManagerInternal.PermissionCallback callback) {
            PermissionManagerService.this.revokeRuntimePermission(permName, packageName, overridePolicy, callingUid, userId, callback);
        }

        public void updatePermissions(String packageName, PackageParser.Package pkg, boolean replaceGrant, Collection<PackageParser.Package> allPackages, PermissionManagerInternal.PermissionCallback callback) {
            PermissionManagerService.this.updatePermissions(packageName, pkg, replaceGrant, allPackages, callback);
        }

        public void updateAllPermissions(String volumeUuid, boolean sdkUpdated, Collection<PackageParser.Package> allPackages, PermissionManagerInternal.PermissionCallback callback) {
            PermissionManagerService.this.updateAllPermissions(volumeUuid, sdkUpdated, allPackages, callback);
        }

        public String[] getAppOpPermissionPackages(String permName) {
            return PermissionManagerService.this.getAppOpPermissionPackages(permName);
        }

        public int getPermissionFlags(String permName, String packageName, int callingUid, int userId) {
            return PermissionManagerService.this.getPermissionFlags(permName, packageName, callingUid, userId);
        }

        public void updatePermissionFlags(String permName, String packageName, int flagMask, int flagValues, int callingUid, int userId, PermissionManagerInternal.PermissionCallback callback) {
            PermissionManagerService.this.updatePermissionFlags(permName, packageName, flagMask, flagValues, callingUid, userId, callback);
        }

        public boolean updatePermissionFlagsForAllApps(int flagMask, int flagValues, int callingUid, int userId, Collection<PackageParser.Package> packages, PermissionManagerInternal.PermissionCallback callback) {
            return PermissionManagerService.this.updatePermissionFlagsForAllApps(flagMask, flagValues, callingUid, userId, packages, callback);
        }

        public void enforceCrossUserPermission(int callingUid, int userId, boolean requireFullPermission, boolean checkShell, String message) {
            PermissionManagerService.this.enforceCrossUserPermission(callingUid, userId, requireFullPermission, checkShell, false, message);
        }

        public void enforceCrossUserPermission(int callingUid, int userId, boolean requireFullPermission, boolean checkShell, boolean requirePermissionWhenSameUser, String message) {
            PermissionManagerService.this.enforceCrossUserPermission(callingUid, userId, requireFullPermission, checkShell, requirePermissionWhenSameUser, message);
        }

        public void enforceGrantRevokeRuntimePermissionPermissions(String message) {
            PermissionManagerService.this.enforceGrantRevokeRuntimePermissionPermissions(message);
        }

        public int checkPermission(String permName, String packageName, int callingUid, int userId) {
            return PermissionManagerService.this.checkPermission(permName, packageName, callingUid, userId);
        }

        public int checkUidPermission(String permName, PackageParser.Package pkg, int uid, int callingUid) {
            return PermissionManagerService.this.checkUidPermission(permName, pkg, uid, callingUid);
        }

        public PermissionGroupInfo getPermissionGroupInfo(String groupName, int flags, int callingUid) {
            return PermissionManagerService.this.getPermissionGroupInfo(groupName, flags, callingUid);
        }

        public List<PermissionGroupInfo> getAllPermissionGroups(int flags, int callingUid) {
            return PermissionManagerService.this.getAllPermissionGroups(flags, callingUid);
        }

        public PermissionInfo getPermissionInfo(String permName, String packageName, int flags, int callingUid) {
            return PermissionManagerService.this.getPermissionInfo(permName, packageName, flags, callingUid);
        }

        public List<PermissionInfo> getPermissionInfoByGroup(String group, int flags, int callingUid) {
            return PermissionManagerService.this.getPermissionInfoByGroup(group, flags, callingUid);
        }

        public PermissionSettings getPermissionSettings() {
            return PermissionManagerService.this.mSettings;
        }

        public DefaultPermissionGrantPolicy getDefaultPermissionGrantPolicy() {
            return PermissionManagerService.this.mDefaultPermissionGrantPolicy;
        }

        public BasePermission getPermissionTEMP(String permName) {
            BasePermission permissionLocked;
            synchronized (PermissionManagerService.this.mLock) {
                permissionLocked = PermissionManagerService.this.mSettings.getPermissionLocked(permName);
            }
            return permissionLocked;
        }
    }

    PermissionManagerService(Context context, DefaultPermissionGrantPolicy.DefaultPermissionGrantedCallback defaultGrantCallback, Object externalLock) {
        this.mContext = context;
        this.mLock = externalLock;
        this.mPackageManagerInt = (PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class);
        this.mUserManagerInt = (UserManagerInternal) LocalServices.getService(UserManagerInternal.class);
        this.mSettings = new PermissionSettings(context, this.mLock);
        this.mHandlerThread = new ServiceThread(TAG, 10, true);
        this.mHandlerThread.start();
        this.mHandler = new Handler(this.mHandlerThread.getLooper());
        Watchdog.getInstance().addThread(this.mHandler);
        this.mDefaultPermissionGrantPolicy = HwServiceFactory.getHwDefaultPermissionGrantPolicy(context, this.mHandlerThread.getLooper(), defaultGrantCallback, this);
        SystemConfig systemConfig = SystemConfig.getInstance();
        this.mSystemPermissions = systemConfig.getSystemPermissions();
        this.mGlobalGids = systemConfig.getGlobalGids();
        ArrayMap<String, SystemConfig.PermissionEntry> permConfig = SystemConfig.getInstance().getPermissions();
        synchronized (this.mLock) {
            for (int i = 0; i < permConfig.size(); i++) {
                SystemConfig.PermissionEntry perm = permConfig.valueAt(i);
                BasePermission bp = this.mSettings.getPermissionLocked(perm.name);
                if (bp == null) {
                    bp = new BasePermission(perm.name, PackageManagerService.PLATFORM_PACKAGE_NAME, 1);
                    this.mSettings.putPermissionLocked(perm.name, bp);
                }
                if (perm.gids != null) {
                    bp.setGids(perm.gids, perm.perUser);
                }
            }
        }
        LocalServices.addService(PermissionManagerInternal.class, new PermissionManagerInternalImpl());
    }

    public static PermissionManagerInternal create(Context context, DefaultPermissionGrantPolicy.DefaultPermissionGrantedCallback defaultGrantCallback, Object externalLock) {
        PermissionManagerInternal permMgrInt = (PermissionManagerInternal) LocalServices.getService(PermissionManagerInternal.class);
        if (permMgrInt != null) {
            return permMgrInt;
        }
        new PermissionManagerService(context, defaultGrantCallback, externalLock);
        return (PermissionManagerInternal) LocalServices.getService(PermissionManagerInternal.class);
    }

    /* access modifiers changed from: package-private */
    public BasePermission getPermission(String permName) {
        BasePermission permissionLocked;
        synchronized (this.mLock) {
            permissionLocked = this.mSettings.getPermissionLocked(permName);
        }
        return permissionLocked;
    }

    /* access modifiers changed from: private */
    public int checkPermission(String permName, String pkgName, int callingUid, int userId) {
        if (!this.mUserManagerInt.exists(userId)) {
            return -1;
        }
        PackageParser.Package pkg = this.mPackageManagerInt.getPackage(pkgName);
        if (pkg == null || pkg.mExtras == null || this.mPackageManagerInt.filterAppAccess(pkg, callingUid, userId)) {
            return -1;
        }
        PackageSetting ps = (PackageSetting) pkg.mExtras;
        boolean instantApp = ps.getInstantApp(userId);
        PermissionsState permissionsState = ps.getPermissionsState();
        if (permissionsState.hasPermission(permName, userId)) {
            if (!instantApp) {
                return 0;
            }
            synchronized (this.mLock) {
                BasePermission bp = this.mSettings.getPermissionLocked(permName);
                if (bp != null && bp.isInstant()) {
                    return 0;
                }
            }
        }
        if (!"android.permission.ACCESS_COARSE_LOCATION".equals(permName) || !permissionsState.hasPermission("android.permission.ACCESS_FINE_LOCATION", userId)) {
            return -1;
        }
        return 0;
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x0089 A[RETURN] */
    public int checkUidPermission(String permName, PackageParser.Package pkg, int uid, int callingUid) {
        int callingUserId = UserHandle.getUserId(callingUid);
        boolean isUidInstantApp = true;
        boolean isCallerInstantApp = this.mPackageManagerInt.getInstantAppPackageName(callingUid) != null;
        if (this.mPackageManagerInt.getInstantAppPackageName(uid) == null) {
            isUidInstantApp = false;
        }
        int userId = UserHandle.getUserId(uid);
        if (!this.mUserManagerInt.exists(userId)) {
            return -1;
        }
        if (pkg != null) {
            if (pkg.mSharedUserId != null) {
                if (isCallerInstantApp) {
                    return -1;
                }
            } else if (this.mPackageManagerInt.filterAppAccess(pkg, callingUid, callingUserId)) {
                return -1;
            }
            PermissionsState permissionsState = ((PackageSetting) pkg.mExtras).getPermissionsState();
            if (permissionsState.hasPermission(permName, userId) && (!isUidInstantApp || this.mSettings.isPermissionInstant(permName))) {
                return 0;
            }
            if (!"android.permission.ACCESS_COARSE_LOCATION".equals(permName) || !permissionsState.hasPermission("android.permission.ACCESS_FINE_LOCATION", userId)) {
                return -1;
            }
            return 0;
        }
        ArraySet<String> perms = this.mSystemPermissions.get(uid);
        if (perms != null) {
            if (perms.contains(permName)) {
                return 0;
            }
            if ("android.permission.ACCESS_COARSE_LOCATION".equals(permName) && perms.contains("android.permission.ACCESS_FINE_LOCATION")) {
                return 0;
            }
        }
        return -1;
    }

    /* access modifiers changed from: private */
    public PermissionGroupInfo getPermissionGroupInfo(String groupName, int flags, int callingUid) {
        PermissionGroupInfo generatePermissionGroupInfo;
        if (this.mPackageManagerInt.getInstantAppPackageName(callingUid) != null) {
            return null;
        }
        synchronized (this.mLock) {
            generatePermissionGroupInfo = PackageParser.generatePermissionGroupInfo(this.mSettings.mPermissionGroups.get(groupName), flags);
        }
        return generatePermissionGroupInfo;
    }

    /* access modifiers changed from: private */
    public List<PermissionGroupInfo> getAllPermissionGroups(int flags, int callingUid) {
        ArrayList<PermissionGroupInfo> out;
        if (this.mPackageManagerInt.getInstantAppPackageName(callingUid) != null) {
            return null;
        }
        synchronized (this.mLock) {
            out = new ArrayList<>(this.mSettings.mPermissionGroups.size());
            for (PackageParser.PermissionGroup pg : this.mSettings.mPermissionGroups.values()) {
                out.add(PackageParser.generatePermissionGroupInfo(pg, flags));
            }
        }
        return out;
    }

    /* access modifiers changed from: private */
    public PermissionInfo getPermissionInfo(String permName, String packageName, int flags, int callingUid) {
        if (this.mPackageManagerInt.getInstantAppPackageName(callingUid) != null) {
            return null;
        }
        synchronized (this.mLock) {
            BasePermission bp = this.mSettings.getPermissionLocked(permName);
            if (bp == null) {
                return null;
            }
            PermissionInfo generatePermissionInfo = bp.generatePermissionInfo(adjustPermissionProtectionFlagsLocked(bp.getProtectionLevel(), packageName, callingUid), flags);
            return generatePermissionInfo;
        }
    }

    /* access modifiers changed from: private */
    public List<PermissionInfo> getPermissionInfoByGroup(String groupName, int flags, int callingUid) {
        if (this.mPackageManagerInt.getInstantAppPackageName(callingUid) != null) {
            return null;
        }
        synchronized (this.mLock) {
            if (groupName != null) {
                try {
                    if (!this.mSettings.mPermissionGroups.containsKey(groupName)) {
                        return null;
                    }
                } catch (Throwable th) {
                    throw th;
                }
            }
            ArrayList<PermissionInfo> out = new ArrayList<>(10);
            for (BasePermission bp : this.mSettings.mPermissions.values()) {
                PermissionInfo pi = bp.generatePermissionInfo(groupName, flags);
                if (pi != null) {
                    out.add(pi);
                }
            }
            return out;
        }
    }

    private int adjustPermissionProtectionFlagsLocked(int protectionLevel, String packageName, int uid) {
        int protectionLevelMasked = protectionLevel & 3;
        if (protectionLevelMasked == 2) {
            return protectionLevel;
        }
        int appId = UserHandle.getAppId(uid);
        if (appId == 1000 || appId == 0 || appId == 2000) {
            return protectionLevel;
        }
        PackageParser.Package pkg = this.mPackageManagerInt.getPackage(packageName);
        if (pkg == null) {
            return protectionLevel;
        }
        if (pkg.applicationInfo.targetSdkVersion < 26) {
            return protectionLevelMasked;
        }
        PackageSetting ps = (PackageSetting) pkg.mExtras;
        if (ps == null || ps.getAppId() == appId) {
            return protectionLevel;
        }
        return protectionLevel;
    }

    /* access modifiers changed from: private */
    public void revokeRuntimePermissionsIfGroupChanged(PackageParser.Package newPackage, PackageParser.Package oldPackage, ArrayList<String> allPackageNames, PermissionManagerInternal.PermissionCallback permissionCallback) {
        int numOldPackagePermissions;
        PackageParser.Permission newPermission;
        String newPermissionGroupName;
        String oldPermissionGroupName;
        int[] userIds;
        int numUserIds;
        int userIdNum;
        String permissionName;
        PackageParser.Package packageR = newPackage;
        PackageParser.Package packageR2 = oldPackage;
        int numOldPackagePermissions2 = packageR2.permissions.size();
        ArrayMap<String, String> oldPermissionNameToGroupName = new ArrayMap<>(numOldPackagePermissions2);
        for (int i = 0; i < numOldPackagePermissions2; i++) {
            PackageParser.Permission permission = (PackageParser.Permission) packageR2.permissions.get(i);
            if (permission.group != null) {
                oldPermissionNameToGroupName.put(permission.info.name, permission.group.info.name);
            }
        }
        int numNewPackagePermissions = packageR.permissions.size();
        int newPermissionNum = 0;
        while (true) {
            int newPermissionNum2 = newPermissionNum;
            if (newPermissionNum2 < numNewPackagePermissions) {
                PackageParser.Permission newPermission2 = (PackageParser.Permission) packageR.permissions.get(newPermissionNum2);
                if ((newPermission2.info.getProtection() & 1) != 0) {
                    String permissionName2 = newPermission2.info.name;
                    String newPermissionGroupName2 = newPermission2.group == null ? null : newPermission2.group.info.name;
                    String oldPermissionGroupName2 = oldPermissionNameToGroupName.get(permissionName2);
                    if (newPermissionGroupName2 != null && !newPermissionGroupName2.equals(oldPermissionGroupName2)) {
                        int[] userIds2 = this.mUserManagerInt.getUserIds();
                        int numUserIds2 = userIds2.length;
                        int userIdNum2 = 0;
                        while (true) {
                            int userIdNum3 = userIdNum2;
                            if (userIdNum3 >= numUserIds2) {
                                break;
                            }
                            int userId = userIds2[userIdNum3];
                            int numPackages = allPackageNames.size();
                            int packageNum = 0;
                            while (true) {
                                numOldPackagePermissions = numOldPackagePermissions2;
                                int numOldPackagePermissions3 = packageNum;
                                if (numOldPackagePermissions3 >= numPackages) {
                                    break;
                                }
                                int numPackages2 = numPackages;
                                String packageName = allPackageNames.get(numOldPackagePermissions3);
                                ArrayMap<String, String> oldPermissionNameToGroupName2 = oldPermissionNameToGroupName;
                                if (checkPermission(permissionName2, packageName, 0, userId) == 0) {
                                    EventLog.writeEvent(1397638484, new Object[]{"72710897", Integer.valueOf(packageR.applicationInfo.uid), "Revoking permission " + permissionName2 + " from package " + packageName + " as the group changed from " + oldPermissionGroupName2 + " to " + newPermissionGroupName2});
                                    userIdNum = userIdNum3;
                                    numUserIds = numUserIds2;
                                    userIds = userIds2;
                                    oldPermissionGroupName = oldPermissionGroupName2;
                                    newPermissionGroupName = newPermissionGroupName2;
                                    permissionName = permissionName2;
                                    newPermission = newPermission2;
                                    try {
                                        revokeRuntimePermission(permissionName2, packageName, false, 1000, userId, permissionCallback);
                                    } catch (IllegalArgumentException e) {
                                        IllegalArgumentException illegalArgumentException = e;
                                        Slog.e(TAG, "Could not revoke " + permissionName + " from " + packageName, e);
                                    }
                                } else {
                                    userIdNum = userIdNum3;
                                    numUserIds = numUserIds2;
                                    userIds = userIds2;
                                    oldPermissionGroupName = oldPermissionGroupName2;
                                    newPermissionGroupName = newPermissionGroupName2;
                                    permissionName = permissionName2;
                                    newPermission = newPermission2;
                                }
                                packageNum = numOldPackagePermissions3 + 1;
                                permissionName2 = permissionName;
                                userIdNum3 = userIdNum;
                                numUserIds2 = numUserIds;
                                numOldPackagePermissions2 = numOldPackagePermissions;
                                numPackages = numPackages2;
                                oldPermissionNameToGroupName = oldPermissionNameToGroupName2;
                                userIds2 = userIds;
                                oldPermissionGroupName2 = oldPermissionGroupName;
                                newPermissionGroupName2 = newPermissionGroupName;
                                newPermission2 = newPermission;
                            }
                            int i2 = numUserIds2;
                            int[] iArr = userIds2;
                            String str = oldPermissionGroupName2;
                            String str2 = newPermissionGroupName2;
                            PackageParser.Permission permission2 = newPermission2;
                            ArrayMap<String, String> oldPermissionNameToGroupName3 = oldPermissionNameToGroupName;
                            String str3 = permissionName2;
                            userIdNum2 = userIdNum3 + 1;
                            numOldPackagePermissions2 = numOldPackagePermissions;
                            oldPermissionNameToGroupName = oldPermissionNameToGroupName3;
                            PackageParser.Package packageR3 = oldPackage;
                        }
                    }
                }
                newPermissionNum = newPermissionNum2 + 1;
                numOldPackagePermissions2 = numOldPackagePermissions2;
                oldPermissionNameToGroupName = oldPermissionNameToGroupName;
                PackageParser.Package packageR4 = oldPackage;
            } else {
                ArrayMap<String, String> arrayMap = oldPermissionNameToGroupName;
                return;
            }
        }
    }

    /* access modifiers changed from: private */
    public void addAllPermissions(PackageParser.Package pkg, boolean chatty) {
        int N = pkg.permissions.size();
        for (int i = 0; i < N; i++) {
            PackageParser.Permission p = (PackageParser.Permission) pkg.permissions.get(i);
            p.info.flags &= -1073741825;
            synchronized (this.mLock) {
                if (pkg.applicationInfo.targetSdkVersion > 22) {
                    p.group = this.mSettings.mPermissionGroups.get(p.info.group);
                    if (PackageManagerService.DEBUG_PERMISSIONS && p.info.group != null && p.group == null) {
                        Slog.i(TAG, "Permission " + p.info.name + " from package " + p.info.packageName + " in an unknown group " + p.info.group);
                    }
                }
                if (p.tree) {
                    this.mSettings.putPermissionTreeLocked(p.info.name, BasePermission.createOrUpdate(this.mSettings.getPermissionTreeLocked(p.info.name), p, pkg, this.mSettings.getAllPermissionTreesLocked(), chatty));
                } else {
                    this.mSettings.putPermissionLocked(p.info.name, BasePermission.createOrUpdate(this.mSettings.getPermissionLocked(p.info.name), p, pkg, this.mSettings.getAllPermissionTreesLocked(), chatty));
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void addAllPermissionGroups(PackageParser.Package pkg, boolean chatty) {
        int N = pkg.permissionGroups.size();
        StringBuilder r = null;
        for (int i = 0; i < N; i++) {
            PackageParser.PermissionGroup pg = (PackageParser.PermissionGroup) pkg.permissionGroups.get(i);
            PackageParser.PermissionGroup cur = this.mSettings.mPermissionGroups.get(pg.info.name);
            boolean isPackageUpdate = pg.info.packageName.equals(cur == null ? null : cur.info.packageName);
            if (cur == null || isPackageUpdate) {
                this.mSettings.mPermissionGroups.put(pg.info.name, pg);
                if (chatty && PackageManagerService.DEBUG_PACKAGE_SCANNING) {
                    if (r == null) {
                        r = new StringBuilder(256);
                    } else {
                        r.append(' ');
                    }
                    if (isPackageUpdate) {
                        r.append("UPD:");
                    }
                    r.append(pg.info.name);
                }
            } else {
                Slog.w(TAG, "Permission group " + pg.info.name + " from package " + pg.info.packageName + " ignored: original from " + cur.info.packageName);
                if (chatty && PackageManagerService.DEBUG_PACKAGE_SCANNING) {
                    if (r == null) {
                        r = new StringBuilder(256);
                    } else {
                        r.append(' ');
                    }
                    r.append("DUP:");
                    r.append(pg.info.name);
                }
            }
        }
        if (r != null && PackageManagerService.DEBUG_PACKAGE_SCANNING) {
            Log.d(TAG, "  Permission Groups: " + r);
        }
    }

    /* access modifiers changed from: private */
    public void removeAllPermissions(PackageParser.Package pkg, boolean chatty) {
        synchronized (this.mLock) {
            int N = pkg.permissions.size();
            StringBuilder r = null;
            for (int i = 0; i < N; i++) {
                PackageParser.Permission p = (PackageParser.Permission) pkg.permissions.get(i);
                BasePermission bp = this.mSettings.mPermissions.get(p.info.name);
                if (bp == null) {
                    bp = this.mSettings.mPermissionTrees.get(p.info.name);
                }
                if (bp != null && bp.isPermission(p)) {
                    bp.setPermission(null);
                    if (PackageManagerService.DEBUG_REMOVE && chatty) {
                        if (r == null) {
                            r = new StringBuilder(256);
                        } else {
                            r.append(' ');
                        }
                        r.append(p.info.name);
                    }
                }
                if (p.isAppOp()) {
                    ArraySet<String> appOpPkgs = this.mSettings.mAppOpPermissionPackages.get(p.info.name);
                    if (appOpPkgs != null) {
                        appOpPkgs.remove(pkg.packageName);
                    }
                }
            }
            if (r != null && PackageManagerService.DEBUG_REMOVE) {
                Log.d(TAG, "  Permissions: " + r);
            }
            int N2 = pkg.requestedPermissions.size();
            for (int i2 = 0; i2 < N2; i2++) {
                String perm = (String) pkg.requestedPermissions.get(i2);
                if (this.mSettings.isPermissionAppOp(perm)) {
                    ArraySet<String> appOpPkgs2 = this.mSettings.mAppOpPermissionPackages.get(perm);
                    if (appOpPkgs2 != null) {
                        appOpPkgs2.remove(pkg.packageName);
                        if (appOpPkgs2.isEmpty()) {
                            this.mSettings.mAppOpPermissionPackages.remove(perm);
                        }
                    }
                }
            }
            if (0 != 0 && PackageManagerService.DEBUG_REMOVE) {
                Log.d(TAG, "  Permissions: " + null);
            }
        }
    }

    /* access modifiers changed from: private */
    public boolean addDynamicPermission(PermissionInfo info, int callingUid, PermissionManagerInternal.PermissionCallback callback) {
        boolean added;
        boolean changed;
        if (this.mPackageManagerInt.getInstantAppPackageName(callingUid) != null) {
            throw new SecurityException("Instant apps can't add permissions");
        } else if (info != null && info.labelRes == 0 && info.nonLocalizedLabel == null) {
            throw new SecurityException("Label must be specified in permission");
        } else {
            BasePermission tree = this.mSettings.enforcePermissionTree(info.name, callingUid);
            synchronized (this.mLock) {
                BasePermission bp = this.mSettings.getPermissionLocked(info.name);
                added = bp == null;
                int fixedLevel = PermissionInfo.fixProtectionLevel(info.protectionLevel);
                if (added) {
                    enforcePermissionCapLocked(info, tree);
                    bp = new BasePermission(info.name, tree.getSourcePackageName(), 2);
                } else if (!bp.isDynamic()) {
                    throw new SecurityException("Not allowed to modify non-dynamic permission " + info.name);
                }
                changed = bp.addToTree(fixedLevel, info, tree);
                if (added) {
                    this.mSettings.putPermissionLocked(info.name, bp);
                }
            }
            if (changed && callback != null) {
                callback.onPermissionChanged();
            }
            return added;
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0042, code lost:
        return;
     */
    public void removeDynamicPermission(String permName, int callingUid, PermissionManagerInternal.PermissionCallback callback) {
        if (this.mPackageManagerInt.getInstantAppPackageName(callingUid) == null) {
            BasePermission enforcePermissionTree = this.mSettings.enforcePermissionTree(permName, callingUid);
            synchronized (this.mLock) {
                BasePermission bp = this.mSettings.getPermissionLocked(permName);
                if (bp != null) {
                    if (bp.isDynamic()) {
                        Slog.wtf(TAG, "Not allowed to modify non-dynamic permission " + permName);
                    }
                    this.mSettings.removePermissionLocked(permName);
                    if (callback != null) {
                        callback.onPermissionRemoved();
                    }
                }
            }
        } else {
            throw new SecurityException("Instant applications don't have access to this method");
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:241:0x039d, code lost:
        r15 = r12;
        r13 = r20;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:261:0x03e6, code lost:
        if (r4.equals(r2.packageName) != false) goto L_0x03e8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:319:0x058d, code lost:
        if (r3.isSystem() != false) goto L_0x059a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:325:0x059e, code lost:
        if (r3.isUpdatedSystem() != false) goto L_0x05a0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:328:?, code lost:
        r3.setInstallPermissionsFixed(true);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:331:0x05a5, code lost:
        r5 = r41;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:332:0x05a7, code lost:
        if (r5 == null) goto L_0x05b1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:333:0x05a9, code lost:
        r5.onPermissionUpdated(r19, r16);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:334:0x05b1, code lost:
        r11 = r16;
        r12 = r19;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:335:0x05b5, code lost:
        return;
     */
    /* JADX WARNING: Removed duplicated region for block: B:100:0x01b2  */
    /* JADX WARNING: Removed duplicated region for block: B:105:0x01c1  */
    private void grantPermissions(PackageParser.Package pkg, boolean replace, String packageOfInterest, PermissionManagerInternal.PermissionCallback callback) {
        int[] currentUserIds;
        boolean runtimePermissionsRevoked;
        boolean changedInstallPermission;
        int[] updatedUserIds;
        PackageSetting ps;
        String permName;
        int N;
        BasePermission bp;
        PackageSetting ps2;
        int[] currentUserIds2;
        int i;
        boolean isLegacySystemApp;
        PackageSetting ps3;
        boolean isLegacySystemApp2;
        int[] currentUserIds3;
        int i2;
        boolean z;
        boolean allowedSig;
        char c;
        int[] currentUserIds4;
        int[] updatedUserIds2;
        int i3;
        int[] iArr;
        boolean changedInstallPermission2;
        boolean changedInstallPermission3;
        char c2;
        PackageManagerService pms;
        PermissionManagerService permissionManagerService = this;
        PackageParser.Package packageR = pkg;
        String str = packageOfInterest;
        PermissionManagerInternal.PermissionCallback permissionCallback = callback;
        PackageSetting ps4 = (PackageSetting) packageR.mExtras;
        if (ps4 != null) {
            boolean isLegacySystemApp3 = permissionManagerService.mPackageManagerInt.isLegacySystemApp(packageR);
            PermissionsState permissionsState = ps4.getPermissionsState();
            PermissionsState origPermissions = permissionsState;
            currentUserIds = UserManagerService.getInstance().getUserIds();
            boolean runtimePermissionsRevoked2 = false;
            int[] updatedUserIds3 = EMPTY_INT_ARRAY;
            boolean changedInstallPermission4 = false;
            if (replace) {
                ps4.setInstallPermissionsFixed(false);
                if (!ps4.isSharedUser()) {
                    origPermissions = new PermissionsState(permissionsState);
                    permissionsState.reset();
                } else {
                    synchronized (permissionManagerService.mLock) {
                        updatedUserIds3 = permissionManagerService.revokeUnusedSharedUserPermissionsLocked(ps4.getSharedUser(), UserManagerService.getInstance().getUserIds());
                        if (!ArrayUtils.isEmpty(updatedUserIds3)) {
                            runtimePermissionsRevoked2 = true;
                        }
                    }
                }
            }
            permissionsState.setGlobalGids(permissionManagerService.mGlobalGids);
            synchronized (permissionManagerService.mLock) {
                try {
                    int N2 = packageR.requestedPermissions.size();
                    int[] updatedUserIds4 = updatedUserIds3;
                    int i4 = 0;
                    while (true) {
                        runtimePermissionsRevoked = runtimePermissionsRevoked2;
                        if (i4 < N2) {
                            try {
                                permName = (String) packageR.requestedPermissions.get(i4);
                                N = N2;
                                bp = permissionManagerService.mSettings.getPermissionLocked(permName);
                                updatedUserIds = updatedUserIds4;
                            } catch (Throwable th) {
                                th = th;
                                boolean z2 = isLegacySystemApp3;
                                int[] iArr2 = currentUserIds;
                                boolean z3 = changedInstallPermission4;
                                PackageSetting packageSetting = ps4;
                                boolean z4 = runtimePermissionsRevoked;
                                int[] iArr3 = updatedUserIds4;
                                while (true) {
                                    try {
                                        break;
                                    } catch (Throwable th2) {
                                        th = th2;
                                    }
                                }
                                throw th;
                            }
                            try {
                                boolean appSupportsRuntimePermissions = packageR.applicationInfo.targetSdkVersion >= 23;
                                try {
                                    if (PackageManagerService.DEBUG_INSTALL) {
                                        try {
                                            StringBuilder sb = new StringBuilder();
                                            changedInstallPermission = changedInstallPermission4;
                                            try {
                                                sb.append("Package ");
                                                sb.append(packageR.packageName);
                                                sb.append(" checking ");
                                                sb.append(permName);
                                                sb.append(": ");
                                                sb.append(bp);
                                                Log.i(TAG, sb.toString());
                                            } catch (Throwable th3) {
                                                th = th3;
                                                PackageSetting packageSetting2 = ps4;
                                                boolean z5 = isLegacySystemApp3;
                                            }
                                        } catch (Throwable th4) {
                                            th = th4;
                                            boolean z6 = changedInstallPermission4;
                                            PackageSetting packageSetting3 = ps4;
                                            boolean z7 = isLegacySystemApp3;
                                            int[] iArr4 = currentUserIds;
                                            boolean z8 = runtimePermissionsRevoked;
                                            int[] iArr5 = updatedUserIds;
                                            PermissionManagerInternal.PermissionCallback permissionCallback2 = callback;
                                            while (true) {
                                                break;
                                            }
                                            throw th;
                                        }
                                    } else {
                                        changedInstallPermission = changedInstallPermission4;
                                    }
                                    if (bp == null) {
                                        ps3 = ps4;
                                        isLegacySystemApp2 = isLegacySystemApp3;
                                        currentUserIds3 = currentUserIds;
                                        i2 = i4;
                                    } else if (bp.getSourcePackageSetting() == null) {
                                        ps3 = ps4;
                                        isLegacySystemApp2 = isLegacySystemApp3;
                                        currentUserIds3 = currentUserIds;
                                        i2 = i4;
                                    } else {
                                        if (packageR.applicationInfo.isInstantApp()) {
                                            if (!bp.isInstant()) {
                                                if (PackageManagerService.DEBUG_PERMISSIONS) {
                                                    Log.i(TAG, "Denying non-ephemeral permission " + bp.getName() + " for package " + packageR.packageName);
                                                }
                                                ps2 = ps4;
                                                isLegacySystemApp = isLegacySystemApp3;
                                                currentUserIds2 = currentUserIds;
                                                i = i4;
                                                updatedUserIds4 = updatedUserIds;
                                                changedInstallPermission4 = changedInstallPermission;
                                                i4 = i + 1;
                                                runtimePermissionsRevoked2 = runtimePermissionsRevoked;
                                                N2 = N;
                                                isLegacySystemApp3 = isLegacySystemApp;
                                                currentUserIds = currentUserIds2;
                                                ps4 = ps2;
                                                permissionManagerService = this;
                                                PermissionManagerInternal.PermissionCallback permissionCallback3 = callback;
                                            }
                                        }
                                        if (!bp.isRuntimeOnly() || appSupportsRuntimePermissions) {
                                            String perm = bp.getName();
                                            if (bp.isAppOp()) {
                                                allowedSig = false;
                                                z = true;
                                                permissionManagerService.mSettings.addAppOpPackage(perm, packageR.packageName);
                                            } else {
                                                allowedSig = false;
                                                z = true;
                                            }
                                            try {
                                                if (bp.isNormal()) {
                                                    c = 2;
                                                    isLegacySystemApp = isLegacySystemApp3;
                                                } else if (bp.isRuntime()) {
                                                    if (!appSupportsRuntimePermissions) {
                                                        if (!permissionManagerService.mSettings.mPermissionReviewRequired) {
                                                            c2 = 2;
                                                            pms = (PackageManagerService) ServiceManager.getService("package");
                                                            if (pms == null) {
                                                                isLegacySystemApp = isLegacySystemApp3;
                                                                try {
                                                                    if (pms.getHwPMSEx().isSystemAppGrantByMdm(packageR)) {
                                                                        c = 2;
                                                                    }
                                                                } catch (Throwable th5) {
                                                                    th = th5;
                                                                    PackageSetting packageSetting4 = ps4;
                                                                }
                                                            } else {
                                                                isLegacySystemApp = isLegacySystemApp3;
                                                            }
                                                            c = c2;
                                                        }
                                                    }
                                                    try {
                                                        if (origPermissions.hasInstallPermission(bp.getName())) {
                                                            c2 = 4;
                                                        } else if (isLegacySystemApp3) {
                                                            c2 = 4;
                                                        } else {
                                                            c2 = 3;
                                                        }
                                                        pms = (PackageManagerService) ServiceManager.getService("package");
                                                        if (pms == null) {
                                                        }
                                                        c = c2;
                                                    } catch (Throwable th6) {
                                                        th = th6;
                                                        boolean z9 = isLegacySystemApp3;
                                                        PackageSetting packageSetting5 = ps4;
                                                        int[] iArr6 = currentUserIds;
                                                        boolean z10 = runtimePermissionsRevoked;
                                                        int[] iArr7 = updatedUserIds;
                                                        boolean z11 = changedInstallPermission;
                                                        PermissionManagerInternal.PermissionCallback permissionCallback4 = callback;
                                                        while (true) {
                                                            break;
                                                        }
                                                        throw th;
                                                    }
                                                } else {
                                                    isLegacySystemApp = isLegacySystemApp3;
                                                    try {
                                                        if (bp.isSignature()) {
                                                            boolean allowedSig2 = permissionManagerService.grantSignaturePermission(perm, packageR, bp, origPermissions);
                                                            if (permissionManagerService.mCustPms != null && permissionManagerService.mCustPms.isHwFiltReqInstallPerm(packageR.packageName, perm)) {
                                                                allowedSig2 = false;
                                                            }
                                                            boolean allowedSig3 = allowedSig2;
                                                            if (allowedSig3) {
                                                                c = 2;
                                                                allowedSig = allowedSig3;
                                                            } else {
                                                                allowedSig = allowedSig3;
                                                            }
                                                        }
                                                        c = z;
                                                    } catch (Throwable th7) {
                                                        th = th7;
                                                        boolean z12 = runtimePermissionsRevoked;
                                                        int[] iArr8 = updatedUserIds;
                                                    }
                                                }
                                                if (PackageManagerService.DEBUG_PERMISSIONS) {
                                                    StringBuilder sb2 = new StringBuilder();
                                                    i = i4;
                                                    sb2.append("Granting permission ");
                                                    sb2.append(perm);
                                                    sb2.append(" to package ");
                                                    sb2.append(packageR.packageName);
                                                    Slog.i(TAG, sb2.toString());
                                                } else {
                                                    i = i4;
                                                }
                                                if (c != 1) {
                                                    if (!ps4.isSystem()) {
                                                        if (ps4.areInstallPermissionsFixed() && !allowedSig && !origPermissions.hasInstallPermission(perm) && !permissionManagerService.isNewPlatformPermissionForPackage(perm, packageR)) {
                                                            c = 1;
                                                        }
                                                    }
                                                    switch (c) {
                                                        case 2:
                                                            ps2 = ps4;
                                                            currentUserIds4 = currentUserIds;
                                                            char c3 = c;
                                                            int[] userIds = UserManagerService.getInstance().getUserIds();
                                                            int length = userIds.length;
                                                            updatedUserIds2 = updatedUserIds;
                                                            int i5 = 0;
                                                            while (i5 < length) {
                                                                int userId = userIds[i5];
                                                                if (origPermissions.getRuntimePermissionState(perm, userId) != null) {
                                                                    origPermissions.revokeRuntimePermission(bp, userId);
                                                                    origPermissions.updatePermissionFlags(bp, userId, 255, 0);
                                                                    updatedUserIds2 = ArrayUtils.appendInt(updatedUserIds2, userId);
                                                                }
                                                                i5++;
                                                            }
                                                            if (permissionsState.grantInstallPermission(bp) != -1) {
                                                                changedInstallPermission4 = true;
                                                                updatedUserIds4 = updatedUserIds2;
                                                                break;
                                                            }
                                                            break;
                                                        case 3:
                                                            ps2 = ps4;
                                                            currentUserIds4 = currentUserIds;
                                                            char c4 = c;
                                                            int[] userIds2 = UserManagerService.getInstance().getUserIds();
                                                            int length2 = userIds2.length;
                                                            updatedUserIds2 = updatedUserIds;
                                                            int i6 = 0;
                                                            while (i6 < length2) {
                                                                try {
                                                                    int userId2 = userIds2[i6];
                                                                    PermissionsState.PermissionState permissionState = origPermissions.getRuntimePermissionState(perm, userId2);
                                                                    int flags = permissionState != null ? permissionState.getFlags() : 0;
                                                                    if (origPermissions.hasRuntimePermission(perm, userId2)) {
                                                                        boolean revokeOnUpgrade = (flags & 8) != 0;
                                                                        if (revokeOnUpgrade) {
                                                                            flags &= -9;
                                                                            updatedUserIds2 = ArrayUtils.appendInt(updatedUserIds2, userId2);
                                                                        }
                                                                        iArr = userIds2;
                                                                        if (permissionManagerService.mSettings.mPermissionReviewRequired) {
                                                                            if (revokeOnUpgrade) {
                                                                                i3 = length2;
                                                                                if (permissionManagerService.mSettings.mPermissionReviewRequired && appSupportsRuntimePermissions && (flags & 64) != 0) {
                                                                                    updatedUserIds2 = ArrayUtils.appendInt(updatedUserIds2, userId2);
                                                                                    flags &= -65;
                                                                                }
                                                                            }
                                                                        }
                                                                        i3 = length2;
                                                                        if (permissionsState.grantRuntimePermission(bp, userId2) == -1) {
                                                                            updatedUserIds2 = ArrayUtils.appendInt(updatedUserIds2, userId2);
                                                                        }
                                                                        updatedUserIds2 = ArrayUtils.appendInt(updatedUserIds2, userId2);
                                                                        flags &= -65;
                                                                    } else {
                                                                        iArr = userIds2;
                                                                        i3 = length2;
                                                                        if (permissionManagerService.mSettings.mPermissionReviewRequired && !appSupportsRuntimePermissions) {
                                                                            if (PackageManagerService.PLATFORM_PACKAGE_NAME.equals(bp.getSourcePackageName()) && (flags & 64) == 0) {
                                                                                flags |= 64;
                                                                                updatedUserIds2 = ArrayUtils.appendInt(updatedUserIds2, userId2);
                                                                            }
                                                                            if (permissionsState.grantRuntimePermission(bp, userId2) != -1) {
                                                                                updatedUserIds2 = ArrayUtils.appendInt(updatedUserIds2, userId2);
                                                                            }
                                                                        }
                                                                    }
                                                                    int flags2 = flags;
                                                                    permissionsState.updatePermissionFlags(bp, userId2, flags2, flags2);
                                                                    i6++;
                                                                    userIds2 = iArr;
                                                                    length2 = i3;
                                                                } catch (Throwable th8) {
                                                                    th = th8;
                                                                    boolean z13 = runtimePermissionsRevoked;
                                                                    PermissionManagerInternal.PermissionCallback permissionCallback5 = callback;
                                                                    while (true) {
                                                                        break;
                                                                    }
                                                                    throw th;
                                                                }
                                                            }
                                                            break;
                                                        case 4:
                                                            PermissionsState.PermissionState permissionState2 = origPermissions.getInstallPermissionState(perm);
                                                            int flags3 = permissionState2 != null ? permissionState2.getFlags() : 0;
                                                            if (origPermissions.revokeInstallPermission(bp) != -1) {
                                                                PermissionsState.PermissionState permissionState3 = permissionState2;
                                                                origPermissions.updatePermissionFlags(bp, -1, 255, 0);
                                                                changedInstallPermission2 = true;
                                                            } else {
                                                                changedInstallPermission2 = changedInstallPermission;
                                                            }
                                                            int flags4 = flags3;
                                                            if ((flags4 & 8) == 0) {
                                                                try {
                                                                    int length3 = currentUserIds.length;
                                                                    changedInstallPermission3 = changedInstallPermission2;
                                                                    char c5 = c;
                                                                    updatedUserIds4 = updatedUserIds;
                                                                    int i7 = 0;
                                                                    while (i7 < length3) {
                                                                        try {
                                                                            int[] currentUserIds5 = currentUserIds;
                                                                            int i8 = length3;
                                                                            int userId3 = currentUserIds[i7];
                                                                            try {
                                                                                ps2 = ps4;
                                                                                if (permissionsState.grantRuntimePermission(bp, userId3) != -1) {
                                                                                    try {
                                                                                        permissionsState.updatePermissionFlags(bp, userId3, flags4, flags4);
                                                                                        updatedUserIds4 = ArrayUtils.appendInt(updatedUserIds4, userId3);
                                                                                    } catch (Throwable th9) {
                                                                                        th = th9;
                                                                                        int[] iArr9 = updatedUserIds4;
                                                                                        boolean z14 = runtimePermissionsRevoked;
                                                                                        boolean z15 = changedInstallPermission3;
                                                                                        PermissionManagerInternal.PermissionCallback permissionCallback52 = callback;
                                                                                        while (true) {
                                                                                            break;
                                                                                        }
                                                                                        throw th;
                                                                                    }
                                                                                }
                                                                                i7++;
                                                                                currentUserIds = currentUserIds5;
                                                                                length3 = i8;
                                                                                ps4 = ps2;
                                                                            } catch (Throwable th10) {
                                                                                th = th10;
                                                                                PackageSetting packageSetting6 = ps4;
                                                                                int[] iArr10 = updatedUserIds4;
                                                                                boolean z16 = runtimePermissionsRevoked;
                                                                                boolean z17 = changedInstallPermission3;
                                                                                PermissionManagerInternal.PermissionCallback permissionCallback6 = callback;
                                                                                while (true) {
                                                                                    break;
                                                                                }
                                                                                throw th;
                                                                            }
                                                                        } catch (Throwable th11) {
                                                                            th = th11;
                                                                            int[] iArr11 = currentUserIds;
                                                                            PackageSetting packageSetting7 = ps4;
                                                                            int[] iArr12 = updatedUserIds4;
                                                                            boolean z18 = runtimePermissionsRevoked;
                                                                            boolean z19 = changedInstallPermission3;
                                                                            PermissionManagerInternal.PermissionCallback permissionCallback7 = callback;
                                                                            while (true) {
                                                                                break;
                                                                            }
                                                                            throw th;
                                                                        }
                                                                    }
                                                                    ps2 = ps4;
                                                                    currentUserIds4 = currentUserIds;
                                                                } catch (Throwable th12) {
                                                                    th = th12;
                                                                    int[] iArr13 = currentUserIds;
                                                                    boolean z20 = changedInstallPermission2;
                                                                    PackageSetting packageSetting8 = ps4;
                                                                    boolean z21 = runtimePermissionsRevoked;
                                                                    int[] iArr14 = updatedUserIds;
                                                                    PermissionManagerInternal.PermissionCallback permissionCallback8 = callback;
                                                                    while (true) {
                                                                        break;
                                                                    }
                                                                    throw th;
                                                                }
                                                            } else {
                                                                ps2 = ps4;
                                                                currentUserIds4 = currentUserIds;
                                                                changedInstallPermission3 = changedInstallPermission2;
                                                                char c6 = c;
                                                                updatedUserIds4 = updatedUserIds;
                                                            }
                                                            changedInstallPermission4 = changedInstallPermission3;
                                                            break;
                                                        default:
                                                            ps2 = ps4;
                                                            currentUserIds2 = currentUserIds;
                                                            char c7 = c;
                                                            if (str != null) {
                                                                break;
                                                            }
                                                            if (PackageManagerService.DEBUG_PERMISSIONS) {
                                                                Slog.i(TAG, "Not granting permission " + perm + " to package " + packageR.packageName + " because it was previously installed without");
                                                                break;
                                                            }
                                                            break;
                                                    }
                                                } else {
                                                    ps2 = ps4;
                                                    currentUserIds2 = currentUserIds;
                                                    if (permissionsState.revokeInstallPermission(bp) != -1) {
                                                        permissionsState.updatePermissionFlags(bp, -1, 255, 0);
                                                        changedInstallPermission4 = true;
                                                        try {
                                                            Slog.i(TAG, "Un-granting permission " + perm + " from package " + packageR.packageName + " (protectionLevel=" + bp.getProtectionLevel() + " flags=0x" + Integer.toHexString(packageR.applicationInfo.flags) + ")");
                                                            updatedUserIds4 = updatedUserIds;
                                                            i4 = i + 1;
                                                            runtimePermissionsRevoked2 = runtimePermissionsRevoked;
                                                            N2 = N;
                                                            isLegacySystemApp3 = isLegacySystemApp;
                                                            currentUserIds = currentUserIds2;
                                                            ps4 = ps2;
                                                            permissionManagerService = this;
                                                            PermissionManagerInternal.PermissionCallback permissionCallback32 = callback;
                                                        } catch (Throwable th13) {
                                                            th = th13;
                                                            boolean z22 = runtimePermissionsRevoked;
                                                            int[] iArr15 = updatedUserIds;
                                                            PermissionManagerInternal.PermissionCallback permissionCallback522 = callback;
                                                            while (true) {
                                                                break;
                                                            }
                                                            throw th;
                                                        }
                                                    } else {
                                                        if (bp.isAppOp() && PackageManagerService.DEBUG_PERMISSIONS && (str == null || str.equals(packageR.packageName))) {
                                                            Slog.i(TAG, "Not granting permission " + perm + " to package " + packageR.packageName + " (protectionLevel=" + bp.getProtectionLevel() + " flags=0x" + Integer.toHexString(packageR.applicationInfo.flags) + ")");
                                                        }
                                                        updatedUserIds4 = updatedUserIds;
                                                        changedInstallPermission4 = changedInstallPermission;
                                                        i4 = i + 1;
                                                        runtimePermissionsRevoked2 = runtimePermissionsRevoked;
                                                        N2 = N;
                                                        isLegacySystemApp3 = isLegacySystemApp;
                                                        currentUserIds = currentUserIds2;
                                                        ps4 = ps2;
                                                        permissionManagerService = this;
                                                        PermissionManagerInternal.PermissionCallback permissionCallback322 = callback;
                                                    }
                                                }
                                            } catch (Throwable th14) {
                                                th = th14;
                                                boolean z23 = isLegacySystemApp3;
                                                int[] iArr16 = currentUserIds;
                                                PackageSetting packageSetting9 = ps4;
                                                boolean z24 = runtimePermissionsRevoked;
                                                int[] iArr17 = updatedUserIds;
                                                boolean z25 = changedInstallPermission;
                                                PermissionManagerInternal.PermissionCallback permissionCallback9 = callback;
                                                while (true) {
                                                    break;
                                                }
                                                throw th;
                                            }
                                        } else {
                                            if (PackageManagerService.DEBUG_PERMISSIONS) {
                                                Log.i(TAG, "Denying runtime-only permission " + bp.getName() + " for package " + packageR.packageName);
                                            }
                                            ps2 = ps4;
                                            isLegacySystemApp = isLegacySystemApp3;
                                            currentUserIds2 = currentUserIds;
                                            i = i4;
                                            updatedUserIds4 = updatedUserIds;
                                            changedInstallPermission4 = changedInstallPermission;
                                            i4 = i + 1;
                                            runtimePermissionsRevoked2 = runtimePermissionsRevoked;
                                            N2 = N;
                                            isLegacySystemApp3 = isLegacySystemApp;
                                            currentUserIds = currentUserIds2;
                                            ps4 = ps2;
                                            permissionManagerService = this;
                                            PermissionManagerInternal.PermissionCallback permissionCallback3222 = callback;
                                        }
                                    }
                                    if (str != null) {
                                        if (str.equals(packageR.packageName)) {
                                        }
                                        updatedUserIds4 = updatedUserIds;
                                        changedInstallPermission4 = changedInstallPermission;
                                        i4 = i + 1;
                                        runtimePermissionsRevoked2 = runtimePermissionsRevoked;
                                        N2 = N;
                                        isLegacySystemApp3 = isLegacySystemApp;
                                        currentUserIds = currentUserIds2;
                                        ps4 = ps2;
                                        permissionManagerService = this;
                                        PermissionManagerInternal.PermissionCallback permissionCallback32222 = callback;
                                    }
                                    if (PackageManagerService.DEBUG_PERMISSIONS) {
                                        Slog.i(TAG, "Unknown permission " + permName + " in package " + packageR.packageName);
                                    }
                                    updatedUserIds4 = updatedUserIds;
                                    changedInstallPermission4 = changedInstallPermission;
                                    i4 = i + 1;
                                    runtimePermissionsRevoked2 = runtimePermissionsRevoked;
                                    N2 = N;
                                    isLegacySystemApp3 = isLegacySystemApp;
                                    currentUserIds = currentUserIds2;
                                    ps4 = ps2;
                                    permissionManagerService = this;
                                    PermissionManagerInternal.PermissionCallback permissionCallback322222 = callback;
                                } catch (Throwable th15) {
                                    th = th15;
                                    boolean z26 = isLegacySystemApp3;
                                    int[] iArr18 = currentUserIds;
                                    boolean z27 = changedInstallPermission4;
                                    PackageSetting packageSetting10 = ps4;
                                    boolean z28 = runtimePermissionsRevoked;
                                    int[] iArr19 = updatedUserIds;
                                    PermissionManagerInternal.PermissionCallback permissionCallback10 = callback;
                                    while (true) {
                                        break;
                                    }
                                    throw th;
                                }
                            } catch (Throwable th16) {
                                th = th16;
                                boolean z29 = isLegacySystemApp3;
                                int[] iArr20 = currentUserIds;
                                boolean z30 = changedInstallPermission4;
                                PackageSetting packageSetting11 = ps4;
                                boolean z31 = runtimePermissionsRevoked;
                                int[] iArr21 = updatedUserIds;
                                while (true) {
                                    break;
                                }
                                throw th;
                            }
                        } else {
                            int i9 = N2;
                            PackageSetting ps5 = ps4;
                            boolean z32 = isLegacySystemApp3;
                            int[] iArr22 = currentUserIds;
                            changedInstallPermission = changedInstallPermission4;
                            updatedUserIds = updatedUserIds4;
                            if (changedInstallPermission || replace) {
                                ps = ps5;
                                if (!ps.areInstallPermissionsFixed()) {
                                    try {
                                    } catch (Throwable th17) {
                                        th = th17;
                                        PermissionManagerInternal.PermissionCallback permissionCallback5222 = callback;
                                        while (true) {
                                            break;
                                        }
                                        throw th;
                                    }
                                }
                            } else {
                                ps = ps5;
                            }
                            try {
                            } catch (Throwable th18) {
                                th = th18;
                                boolean z33 = runtimePermissionsRevoked;
                                int[] iArr23 = updatedUserIds;
                                PermissionManagerInternal.PermissionCallback permissionCallback11 = callback;
                                boolean z34 = changedInstallPermission;
                                while (true) {
                                    break;
                                }
                                throw th;
                            }
                        }
                    }
                } catch (Throwable th19) {
                    th = th19;
                    PackageSetting packageSetting12 = ps4;
                    boolean z35 = isLegacySystemApp3;
                    int[] iArr24 = currentUserIds;
                    while (true) {
                        break;
                    }
                    throw th;
                }
            }
        } else {
            return;
        }
        PermissionManagerInternal.PermissionCallback permissionCallback52222 = callback;
        while (true) {
            break;
        }
        throw th;
    }

    private boolean isNewPlatformPermissionForPackage(String perm, PackageParser.Package pkg) {
        int NP = PackageParser.NEW_PERMISSIONS.length;
        int ip = 0;
        while (ip < NP) {
            PackageParser.NewPermissionInfo npi = PackageParser.NEW_PERMISSIONS[ip];
            if (!npi.name.equals(perm) || pkg.applicationInfo.targetSdkVersion >= npi.sdkVersion) {
                ip++;
            } else {
                Log.i(TAG, "Auto-granting " + perm + " to old pkg " + pkg.packageName);
                return true;
            }
        }
        return false;
    }

    private boolean hasPrivappWhitelistEntry(String perm, PackageParser.Package pkg) {
        ArraySet<String> wlPermissions;
        if (pkg.isVendor()) {
            wlPermissions = SystemConfig.getInstance().getVendorPrivAppPermissions(pkg.packageName);
        } else if (pkg.isProduct()) {
            wlPermissions = SystemConfig.getInstance().getProductPrivAppPermissions(pkg.packageName);
        } else {
            wlPermissions = SystemConfig.getInstance().getPrivAppPermissions(pkg.packageName);
        }
        if ((wlPermissions != null && wlPermissions.contains(perm)) || (pkg.parentPackage != null && hasPrivappWhitelistEntry(perm, pkg.parentPackage))) {
            return true;
        }
        return false;
    }

    private boolean grantSignaturePermission(String perm, PackageParser.Package pkg, BasePermission bp, PermissionsState origPermissions) {
        Iterator it;
        PackageSetting disabledChildPs;
        ArraySet<String> deniedPermissions;
        String str = perm;
        PackageParser.Package packageR = pkg;
        boolean oemPermission = bp.isOEM();
        boolean vendorPrivilegedPermission = bp.isVendorPrivileged();
        boolean privilegedPermission = bp.isPrivileged() || bp.isVendorPrivileged();
        boolean privappPermissionsDisable = RoSystemProperties.CONTROL_PRIVAPP_PERMISSIONS_DISABLE;
        boolean platformPermission = PackageManagerService.PLATFORM_PACKAGE_NAME.equals(bp.getSourcePackageName());
        boolean platformPackage = PackageManagerService.PLATFORM_PACKAGE_NAME.equals(packageR.packageName);
        if (!privappPermissionsDisable && privilegedPermission && pkg.isPrivileged() && !platformPackage && platformPermission && !hasPrivappWhitelistEntry(perm, pkg)) {
            if (!this.mSystemReady && !pkg.isUpdatedSystemApp()) {
                if (pkg.isVendor()) {
                    deniedPermissions = SystemConfig.getInstance().getVendorPrivAppDenyPermissions(packageR.packageName);
                } else if (pkg.isProduct()) {
                    deniedPermissions = SystemConfig.getInstance().getProductPrivAppDenyPermissions(packageR.packageName);
                } else {
                    deniedPermissions = SystemConfig.getInstance().getPrivAppDenyPermissions(packageR.packageName);
                }
                if (!(deniedPermissions == null || !deniedPermissions.contains(str))) {
                    return false;
                }
                Slog.w(TAG, "Privileged permission " + str + " for package " + packageR.packageName + " - not in privapp-permissions whitelist");
                if (RoSystemProperties.CONTROL_PRIVAPP_PERMISSIONS_ENFORCE) {
                    if (this.mPrivappPermissionsViolations == null) {
                        this.mPrivappPermissionsViolations = new ArraySet<>();
                    }
                    this.mPrivappPermissionsViolations.add(packageR.packageName + ": " + str);
                }
            }
            if (RoSystemProperties.CONTROL_PRIVAPP_PERMISSIONS_ENFORCE) {
                return false;
            }
        }
        PackageParser.Package systemPackage = this.mPackageManagerInt.getPackage(this.mPackageManagerInt.getKnownPackageName(0, 0));
        boolean allowed = packageR.mSigningDetails.hasAncestorOrSelf(bp.getSourcePackageSetting().getSigningDetails()) || bp.getSourcePackageSetting().getSigningDetails().checkCapability(packageR.mSigningDetails, 4) || packageR.mSigningDetails.hasAncestorOrSelf(systemPackage.mSigningDetails) || systemPackage.mSigningDetails.checkCapability(packageR.mSigningDetails, 4);
        if (!allowed && ((privilegedPermission || oemPermission) && pkg.isSystem())) {
            if (pkg.isUpdatedSystemApp()) {
                PackageParser.Package disabledPkg = this.mPackageManagerInt.getDisabledPackage(packageR.packageName);
                PackageSetting disabledPs = disabledPkg != null ? (PackageSetting) disabledPkg.mExtras : null;
                if (disabledPs == null || !disabledPs.getPermissionsState().hasInstallPermission(str)) {
                    if (disabledPs != null && disabledPkg != null && isPackageRequestingPermission(disabledPkg, str) && ((privilegedPermission && disabledPs.isPrivileged()) || (oemPermission && disabledPs.isOem() && canGrantOemPermission(disabledPs, str)))) {
                        allowed = true;
                    }
                    if (packageR.parentPackage != null) {
                        PackageParser.Package disabledParentPkg = this.mPackageManagerInt.getDisabledPackage(packageR.parentPackage.packageName);
                        PackageSetting disabledParentPs = disabledParentPkg != null ? (PackageSetting) disabledParentPkg.mExtras : null;
                        if (disabledParentPkg != null && ((privilegedPermission && disabledParentPs.isPrivileged()) || (oemPermission && disabledParentPs.isOem()))) {
                            if (!isPackageRequestingPermission(disabledParentPkg, str) || !canGrantOemPermission(disabledParentPs, str)) {
                                PackageSetting packageSetting = disabledParentPs;
                                if (disabledParentPkg.childPackages != null) {
                                    Iterator it2 = disabledParentPkg.childPackages.iterator();
                                    while (true) {
                                        if (!it2.hasNext()) {
                                            break;
                                        }
                                        PackageParser.Package disabledParentPkg2 = disabledParentPkg;
                                        PackageParser.Package disabledChildPkg = (PackageParser.Package) it2.next();
                                        if (disabledChildPkg != null) {
                                            it = it2;
                                            disabledChildPs = (PackageSetting) disabledChildPkg.mExtras;
                                        } else {
                                            it = it2;
                                            disabledChildPs = null;
                                        }
                                        if (isPackageRequestingPermission(disabledChildPkg, str) && canGrantOemPermission(disabledChildPs, str)) {
                                            allowed = true;
                                            break;
                                        }
                                        disabledParentPkg = disabledParentPkg2;
                                        it2 = it;
                                    }
                                }
                            } else {
                                allowed = true;
                            }
                        }
                    }
                } else if ((privilegedPermission && disabledPs.isPrivileged()) || (oemPermission && disabledPs.isOem() && canGrantOemPermission(disabledPs, str))) {
                    allowed = true;
                }
            } else {
                allowed = (privilegedPermission && pkg.isPrivileged()) || (oemPermission && pkg.isOem() && canGrantOemPermission((PackageSetting) packageR.mExtras, str));
            }
            if (allowed && privilegedPermission && !vendorPrivilegedPermission && pkg.isVendor()) {
                Slog.w(TAG, "Permission " + str + " cannot be granted to privileged vendor apk " + packageR.packageName + " because it isn't a 'vendorPrivileged' permission.");
                allowed = false;
            }
        }
        if (!allowed) {
            if (!allowed && bp.isPre23() && packageR.applicationInfo.targetSdkVersion < 23) {
                allowed = true;
            }
            if (!allowed && bp.isInstaller() && packageR.packageName.equals(this.mPackageManagerInt.getKnownPackageName(2, 0))) {
                allowed = true;
            }
            if (!allowed && bp.isVerifier() && packageR.packageName.equals(this.mPackageManagerInt.getKnownPackageName(3, 0))) {
                allowed = true;
            }
            if (!allowed && bp.isPreInstalled() && pkg.isSystem()) {
                allowed = true;
            }
            if (allowed || !bp.isDevelopment()) {
                PermissionsState permissionsState = origPermissions;
            } else {
                allowed = origPermissions.hasInstallPermission(str);
            }
            if (allowed || !bp.isSetup()) {
            } else {
                boolean z = oemPermission;
                if (packageR.packageName.equals(this.mPackageManagerInt.getKnownPackageName(1, 0))) {
                    allowed = true;
                }
            }
            if (!allowed && bp.isSystemTextClassifier() && packageR.packageName.equals(this.mPackageManagerInt.getKnownPackageName(5, 0))) {
                allowed = true;
            }
        } else {
            PermissionsState permissionsState2 = origPermissions;
            boolean z2 = oemPermission;
        }
        if (!allowed) {
            allowed = this.mPackageManagerInt.getHwCertPermission(allowed, packageR, str);
        }
        return allowed;
    }

    private static boolean canGrantOemPermission(PackageSetting ps, String permission) {
        boolean z = false;
        if (!ps.isOem()) {
            return false;
        }
        Boolean granted = (Boolean) SystemConfig.getInstance().getOemPermissions(ps.name).get(permission);
        if (granted != null) {
            if (Boolean.TRUE == granted) {
                z = true;
            }
            return z;
        }
        throw new IllegalStateException("OEM permission" + permission + " requested by package " + ps.name + " must be explicitly declared granted or not");
    }

    /* access modifiers changed from: private */
    public boolean isPermissionsReviewRequired(PackageParser.Package pkg, int userId) {
        if (this.mSettings.mPermissionReviewRequired && pkg.applicationInfo.targetSdkVersion < 23 && pkg != null && pkg.mExtras != null) {
            return ((PackageSetting) pkg.mExtras).getPermissionsState().isPermissionReviewRequired(userId);
        }
        return false;
    }

    private boolean isPackageRequestingPermission(PackageParser.Package pkg, String permission) {
        int permCount = pkg.requestedPermissions.size();
        for (int j = 0; j < permCount; j++) {
            if (permission.equals((String) pkg.requestedPermissions.get(j))) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: private */
    @GuardedBy("mLock")
    public void grantRuntimePermissionsGrantedToDisabledPackageLocked(PackageParser.Package pkg, int callingUid, PermissionManagerInternal.PermissionCallback callback) {
        int[] iArr;
        int i;
        int i2;
        PackageParser.Package packageR = pkg;
        if (packageR.parentPackage != null && packageR.requestedPermissions != null) {
            PackageParser.Package disabledPkg = this.mPackageManagerInt.getDisabledPackage(packageR.parentPackage.packageName);
            if (disabledPkg != null && disabledPkg.mExtras != null) {
                PackageSetting disabledPs = (PackageSetting) disabledPkg.mExtras;
                if (disabledPs.isPrivileged() && !disabledPs.hasChildPackages()) {
                    int permCount = packageR.requestedPermissions.size();
                    int i3 = 0;
                    while (true) {
                        int i4 = i3;
                        if (i4 < permCount) {
                            String permission = (String) packageR.requestedPermissions.get(i4);
                            BasePermission bp = this.mSettings.getPermissionLocked(permission);
                            if (bp != null && (bp.isRuntime() || bp.isDevelopment())) {
                                int[] userIds = this.mUserManagerInt.getUserIds();
                                int length = userIds.length;
                                int i5 = 0;
                                while (i5 < length) {
                                    int userId = userIds[i5];
                                    if (disabledPs.getPermissionsState().hasRuntimePermission(permission, userId)) {
                                        i2 = i5;
                                        i = length;
                                        iArr = userIds;
                                        grantRuntimePermission(permission, packageR.packageName, false, callingUid, userId, callback);
                                    } else {
                                        i2 = i5;
                                        i = length;
                                        iArr = userIds;
                                    }
                                    i5 = i2 + 1;
                                    length = i;
                                    userIds = iArr;
                                }
                            }
                            i3 = i4 + 1;
                        } else {
                            return;
                        }
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void grantRequestedRuntimePermissions(PackageParser.Package pkg, int[] userIds, String[] grantedPermissions, int callingUid, PermissionManagerInternal.PermissionCallback callback) {
        for (int userId : userIds) {
            grantRequestedRuntimePermissionsForUser(pkg, userId, grantedPermissions, callingUid, callback);
        }
    }

    /* JADX INFO: finally extract failed */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x00ac, code lost:
        r0 = th;
     */
    private void grantRequestedRuntimePermissionsForUser(PackageParser.Package pkg, int userId, String[] grantedPermissions, int callingUid, PermissionManagerInternal.PermissionCallback callback) {
        BasePermission bp;
        PackageParser.Package packageR = pkg;
        int i = userId;
        String[] strArr = grantedPermissions;
        PackageSetting ps = (PackageSetting) packageR.mExtras;
        if (ps != null) {
            PermissionsState permissionsState = ps.getPermissionsState();
            boolean supportsRuntimePermissions = packageR.applicationInfo.targetSdkVersion >= 23;
            boolean instantApp = this.mPackageManagerInt.isInstantApp(packageR.packageName, i);
            Iterator it = packageR.requestedPermissions.iterator();
            while (it.hasNext()) {
                String permission = (String) it.next();
                synchronized (this.mLock) {
                    try {
                        bp = this.mSettings.getPermissionLocked(permission);
                    } catch (Throwable th) {
                        th = th;
                        String str = permission;
                        while (true) {
                            throw th;
                        }
                    }
                }
                if (bp != null && ((bp.isRuntime() || bp.isDevelopment()) && ((!instantApp || bp.isInstant()) && ((supportsRuntimePermissions || !bp.isRuntimeOnly()) && (strArr == null || ArrayUtils.contains(strArr, permission)))))) {
                    int flags = permissionsState.getPermissionFlags(permission, i);
                    if (!supportsRuntimePermissions) {
                        if (this.mSettings.mPermissionReviewRequired && (flags & 64) != 0) {
                            String str2 = permission;
                            updatePermissionFlags(permission, packageR.packageName, 64, 0, callingUid, i, callback);
                        }
                    } else if ((flags & 20) == 0) {
                        BasePermission basePermission = bp;
                        grantRuntimePermission(permission, packageR.packageName, false, callingUid, i, callback);
                    }
                }
            }
        }
    }

    /* JADX INFO: finally extract failed */
    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:91:0x01db, code lost:
        r0 = th;
     */
    public void grantRuntimePermission(String permName, String packageName, boolean overridePolicy, int callingUid, int userId, PermissionManagerInternal.PermissionCallback callback) {
        BasePermission bp;
        long token;
        String str = permName;
        String str2 = packageName;
        int i = userId;
        PermissionManagerInternal.PermissionCallback permissionCallback = callback;
        if (!this.mUserManagerInt.exists(i)) {
            Log.e(TAG, "No such user:" + i);
            return;
        }
        this.mContext.enforceCallingOrSelfPermission("android.permission.GRANT_RUNTIME_PERMISSIONS", "grantRuntimePermission");
        enforceCrossUserPermission(callingUid, i, true, true, false, "grantRuntimePermission");
        PackageParser.Package pkg = this.mPackageManagerInt.getPackage(str2);
        if (pkg == null || pkg.mExtras == null) {
            int i2 = callingUid;
            PackageParser.Package packageR = pkg;
            throw new IllegalArgumentException("Unknown package: " + str2);
        }
        synchronized (this.mLock) {
            try {
                bp = this.mSettings.getPermissionLocked(str);
            } catch (Throwable th) {
                th = th;
                int i3 = callingUid;
                PackageParser.Package packageR2 = pkg;
                while (true) {
                    throw th;
                }
            }
        }
        if (bp == null) {
            int i4 = callingUid;
            PackageParser.Package packageR3 = pkg;
            throw new IllegalArgumentException("Unknown permission: " + str);
        } else if (!this.mPackageManagerInt.filterAppAccess(pkg, callingUid, i)) {
            bp.enforceDeclaredUsedAndRuntimeOrDevelopment(pkg);
            if (!this.mSettings.mPermissionReviewRequired || pkg.applicationInfo.targetSdkVersion >= 23 || !bp.isRuntime()) {
                int uid = UserHandle.getUid(i, pkg.applicationInfo.uid);
                PackageSetting ps = (PackageSetting) pkg.mExtras;
                PermissionsState permissionsState = ps.getPermissionsState();
                int flags = permissionsState.getPermissionFlags(str, i);
                if ((flags & 16) != 0) {
                    throw new SecurityException("Cannot grant system fixed permission " + str + " for package " + str2);
                } else if (!overridePolicy && (flags & 4) != 0) {
                    throw new SecurityException("Cannot grant policy fixed permission " + str + " for package " + str2);
                } else if (bp.isDevelopment()) {
                    if (!(permissionsState.grantInstallPermission(bp) == -1 || permissionCallback == null)) {
                        callback.onInstallPermissionGranted();
                    }
                } else if (ps.getInstantApp(i) && !bp.isInstant()) {
                    throw new SecurityException("Cannot grant non-ephemeral permission" + str + " for package " + str2);
                } else if (pkg.applicationInfo.targetSdkVersion < 23) {
                    Slog.w(TAG, "Cannot grant runtime permission to a legacy app");
                } else {
                    int result = permissionsState.grantRuntimePermission(bp, i);
                    if (result != -1) {
                        if (result == 1 && permissionCallback != null) {
                            permissionCallback.onGidsChanged(UserHandle.getAppId(pkg.applicationInfo.uid), i);
                        }
                        if (bp.isRuntime()) {
                            logPermission(1243, str, str2);
                        }
                        if (permissionCallback != null) {
                            permissionCallback.onPermissionGranted(uid, i);
                        }
                        if ("android.permission.READ_EXTERNAL_STORAGE".equals(str) || "android.permission.WRITE_EXTERNAL_STORAGE".equals(str)) {
                            long token2 = Binder.clearCallingIdentity();
                            try {
                                if (this.mUserManagerInt.isUserInitialized(i)) {
                                    try {
                                        ((StorageManagerInternal) LocalServices.getService(StorageManagerInternal.class)).onExternalStoragePolicyChanged(uid, str2);
                                    } catch (Throwable th2) {
                                        th = th2;
                                        PackageParser.Package packageR4 = pkg;
                                        int i5 = result;
                                        token = token2;
                                    }
                                }
                                PackageParser.Package packageR5 = pkg;
                                int i6 = result;
                                Binder.restoreCallingIdentity(token2);
                            } catch (Throwable th3) {
                                th = th3;
                                PackageParser.Package packageR6 = pkg;
                                int i7 = result;
                                token = token2;
                                Binder.restoreCallingIdentity(token);
                                throw th;
                            }
                        } else {
                            PackageParser.Package packageR7 = pkg;
                            int i8 = result;
                        }
                        return;
                    }
                    int i9 = result;
                }
            }
        } else {
            throw new IllegalArgumentException("Unknown package: " + str2);
        }
    }

    /* access modifiers changed from: private */
    public void revokeRuntimePermission(String permName, String packageName, boolean overridePolicy, int callingUid, int userId, PermissionManagerInternal.PermissionCallback callback) {
        String str = permName;
        String str2 = packageName;
        int i = userId;
        PermissionManagerInternal.PermissionCallback permissionCallback = callback;
        if (!this.mUserManagerInt.exists(i)) {
            Log.e(TAG, "No such user:" + i);
            return;
        }
        this.mContext.enforceCallingOrSelfPermission("android.permission.REVOKE_RUNTIME_PERMISSIONS", "revokeRuntimePermission");
        enforceCrossUserPermission(Binder.getCallingUid(), i, true, true, false, "revokeRuntimePermission");
        PackageParser.Package pkg = this.mPackageManagerInt.getPackage(str2);
        if (pkg == null || pkg.mExtras == null) {
            throw new IllegalArgumentException("Unknown package: " + str2);
        } else if (!this.mPackageManagerInt.filterAppAccess(pkg, Binder.getCallingUid(), i)) {
            BasePermission bp = this.mSettings.getPermissionLocked(str);
            if (bp != null) {
                bp.enforceDeclaredUsedAndRuntimeOrDevelopment(pkg);
                if (!this.mSettings.mPermissionReviewRequired || pkg.applicationInfo.targetSdkVersion >= 23 || !bp.isRuntime()) {
                    PermissionsState permissionsState = ((PackageSetting) pkg.mExtras).getPermissionsState();
                    int flags = permissionsState.getPermissionFlags(str, i);
                    if ((flags & 16) != 0 && UserHandle.getCallingAppId() != 1000) {
                        throw new SecurityException("Non-System UID cannot revoke system fixed permission " + str + " for package " + str2);
                    } else if (!overridePolicy && (flags & 4) != 0) {
                        throw new SecurityException("Cannot revoke policy fixed permission " + str + " for package " + str2);
                    } else if (bp.isDevelopment()) {
                        if (!(permissionsState.revokeInstallPermission(bp) == -1 || permissionCallback == null)) {
                            callback.onInstallPermissionRevoked();
                        }
                    } else if (permissionsState.revokeRuntimePermission(bp, i) != -1) {
                        if (bp.isRuntime()) {
                            logPermission(1245, str, str2);
                        }
                        if (permissionCallback != null) {
                            int uid = UserHandle.getUid(i, pkg.applicationInfo.uid);
                            permissionCallback.onPermissionRevoked(pkg.applicationInfo.uid, i);
                        }
                    }
                }
            } else {
                throw new IllegalArgumentException("Unknown permission: " + str);
            }
        } else {
            throw new IllegalArgumentException("Unknown package: " + str2);
        }
    }

    @GuardedBy("mLock")
    private int[] revokeUnusedSharedUserPermissionsLocked(SharedUserSetting suSetting, int[] allUserIds) {
        int j;
        char c;
        int i;
        char c2;
        PermissionManagerService permissionManagerService = this;
        int[] iArr = allUserIds;
        ArraySet<String> usedPermissions = new ArraySet<>();
        List<PackageParser.Package> pkgList = suSetting.getPackages();
        if (pkgList == null || pkgList.size() == 0) {
            return EmptyArray.INT;
        }
        Iterator<PackageParser.Package> it = pkgList.iterator();
        while (true) {
            j = 0;
            if (!it.hasNext()) {
                break;
            }
            PackageParser.Package pkg = it.next();
            if (pkg.requestedPermissions != null) {
                int requestedPermCount = pkg.requestedPermissions.size();
                while (j < requestedPermCount) {
                    String permission = (String) pkg.requestedPermissions.get(j);
                    if (permissionManagerService.mSettings.getPermissionLocked(permission) != null) {
                        usedPermissions.add(permission);
                    }
                    j++;
                }
            }
        }
        PermissionsState permissionsState = suSetting.getPermissionsState();
        List<PermissionsState.PermissionState> installPermStates = permissionsState.getInstallPermissionStates();
        int i2 = installPermStates.size() - 1;
        while (true) {
            c = 255;
            if (i2 < 0) {
                break;
            }
            PermissionsState.PermissionState permissionState = installPermStates.get(i2);
            if (!usedPermissions.contains(permissionState.getName())) {
                BasePermission bp = permissionManagerService.mSettings.getPermissionLocked(permissionState.getName());
                if (bp != null) {
                    permissionsState.revokeInstallPermission(bp);
                    permissionsState.updatePermissionFlags(bp, -1, 255, 0);
                }
            }
            i2--;
        }
        int[] runtimePermissionChangedUserIds = EmptyArray.INT;
        int length = iArr.length;
        int[] runtimePermissionChangedUserIds2 = runtimePermissionChangedUserIds;
        int i3 = 0;
        while (i3 < length) {
            int userId = iArr[i3];
            List<PermissionsState.PermissionState> runtimePermStates = permissionsState.getRuntimePermissionStates(userId);
            int i4 = runtimePermStates.size() - 1;
            while (i4 >= 0) {
                PermissionsState.PermissionState permissionState2 = runtimePermStates.get(i4);
                if (!usedPermissions.contains(permissionState2.getName())) {
                    BasePermission bp2 = permissionManagerService.mSettings.getPermissionLocked(permissionState2.getName());
                    if (bp2 != null) {
                        permissionsState.revokeRuntimePermission(bp2, userId);
                        c2 = 255;
                        i = 0;
                        permissionsState.updatePermissionFlags(bp2, userId, 255, 0);
                        runtimePermissionChangedUserIds2 = ArrayUtils.appendInt(runtimePermissionChangedUserIds2, userId);
                        i4--;
                        j = i;
                        permissionManagerService = this;
                        c = c2;
                        int[] iArr2 = allUserIds;
                    }
                }
                c2 = 255;
                i = 0;
                i4--;
                j = i;
                permissionManagerService = this;
                c = c2;
                int[] iArr22 = allUserIds;
            }
            char c3 = c;
            int i5 = j;
            i3++;
            permissionManagerService = this;
            c = c3;
            iArr = allUserIds;
        }
        return runtimePermissionChangedUserIds2;
    }

    /* access modifiers changed from: private */
    public String[] getAppOpPermissionPackages(String permName) {
        if (this.mPackageManagerInt.getInstantAppPackageName(Binder.getCallingUid()) != null) {
            return null;
        }
        synchronized (this.mLock) {
            ArraySet<String> pkgs = this.mSettings.mAppOpPermissionPackages.get(permName);
            if (pkgs == null) {
                return null;
            }
            String[] strArr = (String[]) pkgs.toArray(new String[pkgs.size()]);
            return strArr;
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x003b, code lost:
        if (r9.mPackageManagerInt.filterAppAccess(r0, r12, r13) == false) goto L_0x003e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x003d, code lost:
        return 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x004a, code lost:
        return ((com.android.server.pm.PackageSetting) r0.mExtras).getPermissionsState().getPermissionFlags(r10, r13);
     */
    public int getPermissionFlags(String permName, String packageName, int callingUid, int userId) {
        if (!this.mUserManagerInt.exists(userId)) {
            return 0;
        }
        enforceGrantRevokeRuntimePermissionPermissions("getPermissionFlags");
        enforceCrossUserPermission(callingUid, userId, true, false, false, "getPermissionFlags");
        PackageParser.Package pkg = this.mPackageManagerInt.getPackage(packageName);
        if (pkg == null || pkg.mExtras == null) {
            return 0;
        }
        synchronized (this.mLock) {
            if (this.mSettings.getPermissionLocked(permName) == null) {
                return 0;
            }
        }
    }

    /* access modifiers changed from: private */
    public void updatePermissions(String packageName, PackageParser.Package pkg, boolean replaceGrant, Collection<PackageParser.Package> allPackages, PermissionManagerInternal.PermissionCallback callback) {
        int i;
        int i2 = 0;
        if (pkg != null) {
            i = 1;
        } else {
            i = 0;
        }
        if (replaceGrant) {
            i2 = 2;
        }
        int flags = i2 | i;
        updatePermissions(packageName, pkg, getVolumeUuidForPackage(pkg), flags, allPackages, callback);
        if (pkg != null && pkg.childPackages != null) {
            Iterator it = pkg.childPackages.iterator();
            while (it.hasNext()) {
                PackageParser.Package childPkg = (PackageParser.Package) it.next();
                updatePermissions(childPkg.packageName, childPkg, getVolumeUuidForPackage(childPkg), flags, allPackages, callback);
            }
        }
    }

    /* access modifiers changed from: private */
    public void updateAllPermissions(String volumeUuid, boolean sdkUpdated, Collection<PackageParser.Package> allPackages, PermissionManagerInternal.PermissionCallback callback) {
        int i;
        if (sdkUpdated) {
            i = 6;
        } else {
            i = 0;
        }
        updatePermissions(null, null, volumeUuid, i | 1, allPackages, callback);
    }

    private void updatePermissions(String changingPkgName, PackageParser.Package changingPkg, String replaceVolumeUuid, int flags, Collection<PackageParser.Package> allPackages, PermissionManagerInternal.PermissionCallback callback) {
        int flags2 = updatePermissions(changingPkgName, changingPkg, updatePermissionTrees(changingPkgName, changingPkg, flags));
        Trace.traceBegin(262144, "grantPermissions");
        boolean replace = false;
        if ((flags2 & 1) != 0) {
            for (PackageParser.Package pkg : allPackages) {
                if (pkg != changingPkg) {
                    grantPermissions(pkg, (flags2 & 4) != 0 && Objects.equals(replaceVolumeUuid, getVolumeUuidForPackage(pkg)), changingPkgName, callback);
                }
            }
        }
        if (changingPkg != null) {
            String volumeUuid = getVolumeUuidForPackage(changingPkg);
            if ((flags2 & 2) != 0 && Objects.equals(replaceVolumeUuid, volumeUuid)) {
                replace = true;
            }
            grantPermissions(changingPkg, replace, changingPkgName, callback);
        }
        Trace.traceEnd(262144);
    }

    private int updatePermissions(String packageName, PackageParser.Package pkg, int flags) {
        Set<BasePermission> needsUpdate = null;
        synchronized (this.mLock) {
            Iterator<BasePermission> it = this.mSettings.mPermissions.values().iterator();
            while (it.hasNext()) {
                BasePermission bp = it.next();
                if (bp.isDynamic()) {
                    bp.updateDynamicPermission(this.mSettings.mPermissionTrees.values());
                }
                if (bp.getSourcePackageSetting() == null) {
                    if (needsUpdate == null) {
                        needsUpdate = new ArraySet<>(this.mSettings.mPermissions.size());
                    }
                    needsUpdate.add(bp);
                } else if (packageName != null && packageName.equals(bp.getSourcePackageName())) {
                    if (pkg == null || !hasPermission(pkg, bp.getName())) {
                        Slog.i(TAG, "Removing old permission tree: " + bp.getName() + " from package " + bp.getSourcePackageName());
                        flags |= 1;
                        it.remove();
                    }
                }
            }
        }
        if (needsUpdate != null) {
            for (BasePermission bp2 : needsUpdate) {
                PackageParser.Package sourcePkg = this.mPackageManagerInt.getPackage(bp2.getSourcePackageName());
                synchronized (this.mLock) {
                    if (sourcePkg != null) {
                        try {
                            if (sourcePkg.mExtras != null) {
                                PackageSetting sourcePs = (PackageSetting) sourcePkg.mExtras;
                                if (bp2.getSourcePackageSetting() == null) {
                                    bp2.setSourcePackageSetting(sourcePs);
                                }
                            }
                        } finally {
                        }
                    }
                    Slog.w(TAG, "Removing dangling permission: " + bp2.getName() + " from package " + bp2.getSourcePackageName());
                    this.mSettings.removePermissionLocked(bp2.getName());
                }
            }
        }
        return flags;
    }

    private int updatePermissionTrees(String packageName, PackageParser.Package pkg, int flags) {
        Set<BasePermission> needsUpdate = null;
        synchronized (this.mLock) {
            Iterator<BasePermission> it = this.mSettings.mPermissionTrees.values().iterator();
            while (it.hasNext()) {
                BasePermission bp = it.next();
                if (bp.getSourcePackageSetting() == null) {
                    if (needsUpdate == null) {
                        needsUpdate = new ArraySet<>(this.mSettings.mPermissionTrees.size());
                    }
                    needsUpdate.add(bp);
                } else if (packageName != null && packageName.equals(bp.getSourcePackageName())) {
                    if (pkg == null || !hasPermission(pkg, bp.getName())) {
                        Slog.i(TAG, "Removing old permission tree: " + bp.getName() + " from package " + bp.getSourcePackageName());
                        flags |= 1;
                        it.remove();
                    }
                }
            }
        }
        if (needsUpdate != null) {
            for (BasePermission bp2 : needsUpdate) {
                PackageParser.Package sourcePkg = this.mPackageManagerInt.getPackage(bp2.getSourcePackageName());
                synchronized (this.mLock) {
                    if (sourcePkg != null) {
                        try {
                            if (sourcePkg.mExtras != null) {
                                PackageSetting sourcePs = (PackageSetting) sourcePkg.mExtras;
                                if (bp2.getSourcePackageSetting() == null) {
                                    bp2.setSourcePackageSetting(sourcePs);
                                }
                            }
                        } finally {
                        }
                    }
                    Slog.w(TAG, "Removing dangling permission tree: " + bp2.getName() + " from package " + bp2.getSourcePackageName());
                    this.mSettings.removePermissionLocked(bp2.getName());
                }
            }
        }
        return flags;
    }

    /* access modifiers changed from: private */
    public void updatePermissionFlags(String permName, String packageName, int flagMask, int flagValues, int callingUid, int userId, PermissionManagerInternal.PermissionCallback callback) {
        int flagValues2;
        int flagValues3;
        BasePermission bp;
        String str = permName;
        String str2 = packageName;
        int i = callingUid;
        int i2 = userId;
        PermissionManagerInternal.PermissionCallback permissionCallback = callback;
        if (this.mUserManagerInt.exists(i2)) {
            enforceGrantRevokeRuntimePermissionPermissions("updatePermissionFlags");
            enforceCrossUserPermission(i, i2, true, true, false, "updatePermissionFlags");
            if (i != 1000) {
                flagValues2 = flagValues & -17 & -33 & -65;
                flagValues3 = flagMask & -17 & -33;
            } else {
                flagValues3 = flagMask;
                flagValues2 = flagValues;
            }
            PackageParser.Package pkg = this.mPackageManagerInt.getPackage(str2);
            if (pkg == null || pkg.mExtras == null) {
                throw new IllegalArgumentException("Unknown package: " + str2);
            } else if (!this.mPackageManagerInt.filterAppAccess(pkg, i, i2)) {
                synchronized (this.mLock) {
                    bp = this.mSettings.getPermissionLocked(str);
                }
                if (bp != null) {
                    PermissionsState permissionsState = ((PackageSetting) pkg.mExtras).getPermissionsState();
                    boolean hadState = permissionsState.getRuntimePermissionState(str, i2) != null;
                    if (permissionsState.updatePermissionFlags(bp, i2, flagValues3, flagValues2) && permissionCallback != null) {
                        if (permissionsState.getInstallPermissionState(str) != null) {
                            callback.onInstallPermissionUpdated();
                        } else if (permissionsState.getRuntimePermissionState(str, i2) != null || hadState) {
                            permissionCallback.onPermissionUpdated(new int[]{i2}, false);
                        }
                    }
                    return;
                }
                throw new IllegalArgumentException("Unknown permission: " + str);
            } else {
                throw new IllegalArgumentException("Unknown package: " + str2);
            }
        }
    }

    /* access modifiers changed from: private */
    public boolean updatePermissionFlagsForAllApps(int flagMask, int flagValues, int callingUid, int userId, Collection<PackageParser.Package> packages, PermissionManagerInternal.PermissionCallback callback) {
        if (!this.mUserManagerInt.exists(userId)) {
            return false;
        }
        enforceGrantRevokeRuntimePermissionPermissions("updatePermissionFlagsForAllApps");
        enforceCrossUserPermission(callingUid, userId, true, true, false, "updatePermissionFlagsForAllApps");
        if (callingUid != 1000) {
            flagMask &= -17;
            flagValues &= -17;
        }
        boolean changed = false;
        for (PackageParser.Package pkg : packages) {
            PackageSetting ps = (PackageSetting) pkg.mExtras;
            if (ps != null) {
                changed |= ps.getPermissionsState().updatePermissionFlagsForAllPermissions(userId, flagMask, flagValues);
            }
        }
        return changed;
    }

    /* access modifiers changed from: private */
    public void enforceGrantRevokeRuntimePermissionPermissions(String message) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.GRANT_RUNTIME_PERMISSIONS") != 0 && this.mContext.checkCallingOrSelfPermission("android.permission.REVOKE_RUNTIME_PERMISSIONS") != 0) {
            throw new SecurityException(message + " requires " + "android.permission.GRANT_RUNTIME_PERMISSIONS" + " or " + "android.permission.REVOKE_RUNTIME_PERMISSIONS");
        }
    }

    /* access modifiers changed from: private */
    public void enforceCrossUserPermission(int callingUid, int userId, boolean requireFullPermission, boolean checkShell, boolean requirePermissionWhenSameUser, String message) {
        if (userId >= 0) {
            if (checkShell) {
                PackageManagerServiceUtils.enforceShellRestriction("no_debugging_features", callingUid, userId);
            }
            if (!((!requirePermissionWhenSameUser && userId == UserHandle.getUserId(callingUid)) || callingUid == 1000 || callingUid == 0)) {
                if (requireFullPermission) {
                    this.mContext.enforceCallingOrSelfPermission("android.permission.INTERACT_ACROSS_USERS_FULL", message);
                } else {
                    try {
                        this.mContext.enforceCallingOrSelfPermission("android.permission.INTERACT_ACROSS_USERS_FULL", message);
                    } catch (SecurityException e) {
                        this.mContext.enforceCallingOrSelfPermission("android.permission.INTERACT_ACROSS_USERS", message);
                    }
                }
            }
            return;
        }
        throw new IllegalArgumentException("Invalid userId " + userId);
    }

    private int calculateCurrentPermissionFootprintLocked(BasePermission tree) {
        int size = 0;
        for (BasePermission perm : this.mSettings.mPermissions.values()) {
            size += tree.calculateFootprint(perm);
        }
        return size;
    }

    private void enforcePermissionCapLocked(PermissionInfo info, BasePermission tree) {
        if (tree.getUid() != 1000) {
            if (info.calculateFootprint() + calculateCurrentPermissionFootprintLocked(tree) > 32768) {
                throw new SecurityException("Permission tree size cap exceeded");
            }
        }
    }

    /* access modifiers changed from: private */
    public void systemReady() {
        this.mSystemReady = true;
        if (this.mPrivappPermissionsViolations != null) {
            throw new IllegalStateException("Signature|privileged permissions not in privapp-permissions whitelist: " + this.mPrivappPermissionsViolations);
        }
    }

    private static String getVolumeUuidForPackage(PackageParser.Package pkg) {
        if (pkg == null) {
            return StorageManager.UUID_PRIVATE_INTERNAL;
        }
        if (!pkg.isExternal()) {
            return StorageManager.UUID_PRIVATE_INTERNAL;
        }
        if (TextUtils.isEmpty(pkg.volumeUuid)) {
            return "primary_physical";
        }
        return pkg.volumeUuid;
    }

    private static boolean hasPermission(PackageParser.Package pkgInfo, String permName) {
        for (int i = pkgInfo.permissions.size() - 1; i >= 0; i--) {
            if (((PackageParser.Permission) pkgInfo.permissions.get(i)).info.name.equals(permName)) {
                return true;
            }
        }
        return false;
    }

    private void logPermission(int action, String name, String packageName) {
        LogMaker log = new LogMaker(action);
        log.setPackageName(packageName);
        log.addTaggedData(1241, name);
        this.mMetricsLogger.write(log);
    }
}
