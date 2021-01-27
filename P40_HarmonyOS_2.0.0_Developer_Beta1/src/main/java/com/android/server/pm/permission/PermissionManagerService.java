package com.android.server.pm.permission;

import android.app.ActivityManager;
import android.app.IUidObserver;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManagerInternal;
import android.content.pm.PackageParser;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.metrics.LogMaker;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.Trace;
import android.os.UserHandle;
import android.os.UserManagerInternal;
import android.os.storage.StorageManager;
import android.os.storage.StorageManagerInternal;
import android.permission.PermissionControllerManager;
import android.permission.PermissionManager;
import android.permission.PermissionManagerInternal;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.EventLog;
import android.util.Log;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.os.RoSystemProperties;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.function.pooled.PooledLambda;
import com.android.server.FgThread;
import com.android.server.HwServiceFactory;
import com.android.server.LocalServices;
import com.android.server.ServiceThread;
import com.android.server.SystemConfig;
import com.android.server.Watchdog;
import com.android.server.pm.DumpState;
import com.android.server.pm.HwCustPackageManagerService;
import com.android.server.pm.PackageManagerService;
import com.android.server.pm.PackageManagerServiceUtils;
import com.android.server.pm.PackageSetting;
import com.android.server.pm.SharedUserSetting;
import com.android.server.pm.UserManagerService;
import com.android.server.pm.permission.PermissionManagerServiceInternal;
import com.android.server.pm.permission.PermissionsState;
import com.android.server.policy.PermissionPolicyInternal;
import com.android.server.policy.SoftRestrictedPermissionPolicy;
import huawei.cust.HwCustUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import libcore.util.EmptyArray;

public class PermissionManagerService {
    private static final long BACKUP_TIMEOUT_MILLIS = TimeUnit.SECONDS.toMillis(60);
    private static final int BLOCKING_PERMISSION_FLAGS = 52;
    private static final int[] EMPTY_INT_ARRAY = new int[0];
    private static final Map<String, String> FULLER_PERMISSION_MAP = new HashMap();
    private static final int GRANT_DENIED = 1;
    private static final int GRANT_INSTALL = 2;
    private static final int GRANT_RUNTIME = 3;
    private static final int GRANT_UPGRADE = 4;
    private static final int MAX_PERMISSION_TREE_FOOTPRINT = 32768;
    private static final String TAG = "PackageManager";
    private static final int UPDATE_PERMISSIONS_ALL = 1;
    private static final int UPDATE_PERMISSIONS_REPLACE_ALL = 4;
    private static final int UPDATE_PERMISSIONS_REPLACE_PKG = 2;
    private static final int USER_PERMISSION_FLAGS = 3;
    @GuardedBy({"mLock"})
    private ArrayMap<String, List<String>> mBackgroundPermissions;
    private final Context mContext;
    private HwCustPackageManagerService mCustPms;
    private final DefaultPermissionGrantPolicy mDefaultPermissionGrantPolicy;
    private final int[] mGlobalGids;
    private final Handler mHandler;
    private final HandlerThread mHandlerThread;
    @GuardedBy({"mLock"})
    private final SparseBooleanArray mHasNoDelayedPermBackup;
    private final Object mLock;
    private final MetricsLogger mMetricsLogger;
    private PackageManagerService mPackageManager = null;
    private final PackageManagerInternal mPackageManagerInt;
    private PermissionControllerManager mPermissionControllerManager;
    @GuardedBy({"mLock"})
    private PermissionPolicyInternal mPermissionPolicyInternal;
    @GuardedBy({"mLock"})
    private ArraySet<String> mPrivappPermissionsViolations;
    @GuardedBy({"mLock"})
    private final ArrayList<PermissionManagerInternal.OnRuntimePermissionStateChangedListener> mRuntimePermissionStateChangedListeners;
    @GuardedBy({"mLock"})
    private final PermissionSettings mSettings;
    private final SparseArray<ArraySet<String>> mSystemPermissions;
    @GuardedBy({"mLock"})
    private boolean mSystemReady;
    private final UserManagerInternal mUserManagerInt;

    static {
        FULLER_PERMISSION_MAP.put("android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION");
        FULLER_PERMISSION_MAP.put("android.permission.INTERACT_ACROSS_USERS", "android.permission.INTERACT_ACROSS_USERS_FULL");
    }

    PermissionManagerService(Context context, Object externalLock) {
        this.mCustPms = (HwCustPackageManagerService) HwCustUtils.createObj(HwCustPackageManagerService.class, new Object[0]);
        this.mMetricsLogger = new MetricsLogger();
        this.mHasNoDelayedPermBackup = new SparseBooleanArray();
        this.mRuntimePermissionStateChangedListeners = new ArrayList<>();
        this.mContext = context;
        this.mLock = externalLock;
        this.mPackageManagerInt = (PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class);
        this.mUserManagerInt = (UserManagerInternal) LocalServices.getService(UserManagerInternal.class);
        this.mSettings = new PermissionSettings(this.mLock);
        this.mHandlerThread = new ServiceThread(TAG, 10, true);
        this.mHandlerThread.start();
        this.mHandler = new Handler(this.mHandlerThread.getLooper());
        Watchdog.getInstance().addThread(this.mHandler);
        this.mDefaultPermissionGrantPolicy = HwServiceFactory.getHwDefaultPermissionGrantPolicy(context, this.mHandlerThread.getLooper(), this);
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
        PermissionManagerServiceInternalImpl localService = new PermissionManagerServiceInternalImpl();
        LocalServices.addService(PermissionManagerServiceInternal.class, localService);
        LocalServices.addService(PermissionManagerInternal.class, localService);
    }

    public static PermissionManagerServiceInternal create(Context context, Object externalLock) {
        PermissionManagerServiceInternal permMgrInt = (PermissionManagerServiceInternal) LocalServices.getService(PermissionManagerServiceInternal.class);
        if (permMgrInt != null) {
            return permMgrInt;
        }
        new PermissionManagerService(context, externalLock);
        return (PermissionManagerServiceInternal) LocalServices.getService(PermissionManagerServiceInternal.class);
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
    /* access modifiers changed from: public */
    private int checkPermission(String permName, String pkgName, int callingUid, int userId) {
        PackageParser.Package pkg;
        if (!this.mUserManagerInt.exists(userId) || (pkg = this.mPackageManagerInt.getPackage(pkgName)) == null || pkg.mExtras == null || this.mPackageManagerInt.filterAppAccess(pkg, callingUid, userId)) {
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
        if (isImpliedPermissionGranted(permissionsState, permName, userId)) {
            return 0;
        }
        return -1;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int checkUidPermission(String permName, PackageParser.Package pkg, int uid, int callingUid) {
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
            if ((permissionsState.hasPermission(permName, userId) && (!isUidInstantApp || this.mSettings.isPermissionInstant(permName))) || isImpliedPermissionGranted(permissionsState, permName, userId)) {
                return 0;
            }
        } else {
            ArraySet<String> perms = this.mSystemPermissions.get(uid);
            if (perms != null) {
                if (perms.contains(permName)) {
                    return 0;
                }
                if (FULLER_PERMISSION_MAP.containsKey(permName) && perms.contains(FULLER_PERMISSION_MAP.get(permName))) {
                    return 0;
                }
            }
        }
        return -1;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private byte[] backupRuntimePermissions(UserHandle user) {
        CompletableFuture<byte[]> backup = new CompletableFuture<>();
        PermissionControllerManager permissionControllerManager = this.mPermissionControllerManager;
        Executor mainExecutor = this.mContext.getMainExecutor();
        Objects.requireNonNull(backup);
        permissionControllerManager.getRuntimePermissionBackup(user, mainExecutor, new PermissionControllerManager.OnGetRuntimePermissionBackupCallback(backup) {
            /* class com.android.server.pm.permission.$$Lambda$js2BSmz1ucAEj8fgl3jw5trxIjw */
            private final /* synthetic */ CompletableFuture f$0;

            {
                this.f$0 = r1;
            }

            public final void onGetRuntimePermissionsBackup(byte[] bArr) {
                this.f$0.complete(bArr);
            }
        });
        try {
            return backup.get(BACKUP_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            Slog.e(TAG, "Cannot create permission backup for " + user, e);
            return null;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void restoreRuntimePermissions(byte[] backup, UserHandle user) {
        synchronized (this.mLock) {
            this.mHasNoDelayedPermBackup.delete(user.getIdentifier());
            this.mPermissionControllerManager.restoreRuntimePermissionBackup(backup, user);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void restoreDelayedRuntimePermissions(String packageName, UserHandle user) {
        synchronized (this.mLock) {
            if (!this.mHasNoDelayedPermBackup.get(user.getIdentifier(), false)) {
                this.mPermissionControllerManager.restoreDelayedRuntimePermissionBackup(packageName, user, this.mContext.getMainExecutor(), new Consumer(user) {
                    /* class com.android.server.pm.permission.$$Lambda$PermissionManagerService$KZ0FIR02GsOfMAAOdWzIbkVHHM */
                    private final /* synthetic */ UserHandle f$1;

                    {
                        this.f$1 = r2;
                    }

                    @Override // java.util.function.Consumer
                    public final void accept(Object obj) {
                        PermissionManagerService.this.lambda$restoreDelayedRuntimePermissions$0$PermissionManagerService(this.f$1, (Boolean) obj);
                    }
                });
            }
        }
    }

    public /* synthetic */ void lambda$restoreDelayedRuntimePermissions$0$PermissionManagerService(UserHandle user, Boolean hasMoreBackup) {
        if (!hasMoreBackup.booleanValue()) {
            synchronized (this.mLock) {
                this.mHasNoDelayedPermBackup.put(user.getIdentifier(), true);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void addOnRuntimePermissionStateChangedListener(PermissionManagerInternal.OnRuntimePermissionStateChangedListener listener) {
        synchronized (this.mLock) {
            this.mRuntimePermissionStateChangedListeners.add(listener);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void removeOnRuntimePermissionStateChangedListener(PermissionManagerInternal.OnRuntimePermissionStateChangedListener listener) {
        synchronized (this.mLock) {
            this.mRuntimePermissionStateChangedListeners.remove(listener);
        }
    }

    private void notifyRuntimePermissionStateChanged(String packageName, int userId) {
        FgThread.getHandler().sendMessage(PooledLambda.obtainMessage($$Lambda$PermissionManagerService$NPd9St1HBvGAtg1uhMV2Upfww4g.INSTANCE, this, packageName, Integer.valueOf(userId)));
    }

    /* access modifiers changed from: private */
    public void doNotifyRuntimePermissionStateChanged(String packageName, int userId) {
        ArrayList<PermissionManagerInternal.OnRuntimePermissionStateChangedListener> listeners;
        synchronized (this.mLock) {
            if (!this.mRuntimePermissionStateChangedListeners.isEmpty()) {
                listeners = new ArrayList<>(this.mRuntimePermissionStateChangedListeners);
            } else {
                return;
            }
        }
        int listenerCount = listeners.size();
        for (int i = 0; i < listenerCount; i++) {
            listeners.get(i).onRuntimePermissionStateChanged(packageName, userId);
        }
    }

    private static boolean isImpliedPermissionGranted(PermissionsState permissionsState, String permName, int userId) {
        return FULLER_PERMISSION_MAP.containsKey(permName) && permissionsState.hasPermission(FULLER_PERMISSION_MAP.get(permName), userId);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private PermissionGroupInfo getPermissionGroupInfo(String groupName, int flags, int callingUid) {
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
    /* access modifiers changed from: public */
    private List<PermissionGroupInfo> getAllPermissionGroups(int flags, int callingUid) {
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
    /* access modifiers changed from: public */
    private PermissionInfo getPermissionInfo(String permName, String packageName, int flags, int callingUid) {
        if (this.mPackageManagerInt.getInstantAppPackageName(callingUid) != null) {
            return null;
        }
        synchronized (this.mLock) {
            BasePermission bp = this.mSettings.getPermissionLocked(permName);
            if (bp == null) {
                return null;
            }
            return bp.generatePermissionInfo(adjustPermissionProtectionFlagsLocked(bp.getProtectionLevel(), packageName, callingUid), flags);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private List<PermissionInfo> getPermissionInfoByGroup(String groupName, int flags, int callingUid) {
        if (this.mPackageManagerInt.getInstantAppPackageName(callingUid) != null) {
            return null;
        }
        synchronized (this.mLock) {
            if (groupName != null) {
                if (!this.mSettings.mPermissionGroups.containsKey(groupName)) {
                    return null;
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
        int appId;
        PackageParser.Package pkg;
        int protectionLevelMasked = protectionLevel & 3;
        if (protectionLevelMasked == 2 || (appId = UserHandle.getAppId(uid)) == 1000 || appId == 0 || appId == 2000 || (pkg = this.mPackageManagerInt.getPackage(packageName)) == null) {
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
    /* access modifiers changed from: public */
    private void revokeRuntimePermissionsIfGroupChanged(PackageParser.Package newPackage, PackageParser.Package oldPackage, ArrayList<String> allPackageNames, PermissionManagerServiceInternal.PermissionCallback permissionCallback) {
        String newPermissionGroupName;
        String oldPermissionGroupName;
        int[] userIds;
        int numUserIds;
        int userIdNum;
        String permissionName;
        PermissionManagerService permissionManagerService = this;
        int numOldPackagePermissions = oldPackage.permissions.size();
        ArrayMap<String, String> oldPermissionNameToGroupName = new ArrayMap<>(numOldPackagePermissions);
        for (int i = 0; i < numOldPackagePermissions; i++) {
            PackageParser.Permission permission = (PackageParser.Permission) oldPackage.permissions.get(i);
            if (permission.group != null) {
                oldPermissionNameToGroupName.put(permission.info.name, permission.group.info.name);
            }
        }
        int numNewPackagePermissions = newPackage.permissions.size();
        int newPermissionNum = 0;
        while (newPermissionNum < numNewPackagePermissions) {
            PackageParser.Permission newPermission = (PackageParser.Permission) newPackage.permissions.get(newPermissionNum);
            if ((newPermission.info.getProtection() & 1) != 0) {
                String permissionName2 = newPermission.info.name;
                String newPermissionGroupName2 = newPermission.group == null ? null : newPermission.group.info.name;
                String oldPermissionGroupName2 = oldPermissionNameToGroupName.get(permissionName2);
                if (newPermissionGroupName2 != null) {
                    if (!newPermissionGroupName2.equals(oldPermissionGroupName2)) {
                        int[] userIds2 = permissionManagerService.mUserManagerInt.getUserIds();
                        int numUserIds2 = userIds2.length;
                        int userIdNum2 = 0;
                        while (userIdNum2 < numUserIds2) {
                            int userId = userIds2[userIdNum2];
                            int numPackages = allPackageNames.size();
                            int packageNum = 0;
                            while (packageNum < numPackages) {
                                String packageName = allPackageNames.get(packageNum);
                                if (permissionManagerService.checkPermission(permissionName2, packageName, 0, userId) == 0) {
                                    userIdNum = userIdNum2;
                                    EventLog.writeEvent(1397638484, "72710897", Integer.valueOf(newPackage.applicationInfo.uid), "Revoking permission " + permissionName2 + " from package " + packageName + " as the group changed from " + oldPermissionGroupName2 + " to " + newPermissionGroupName2);
                                    numUserIds = numUserIds2;
                                    userIds = userIds2;
                                    oldPermissionGroupName = oldPermissionGroupName2;
                                    newPermissionGroupName = newPermissionGroupName2;
                                    permissionName = permissionName2;
                                    try {
                                        revokeRuntimePermission(permissionName2, packageName, false, userId, permissionCallback);
                                    } catch (IllegalArgumentException e) {
                                        Slog.e(TAG, "Could not revoke " + permissionName + " from " + packageName, e);
                                    }
                                } else {
                                    userIdNum = userIdNum2;
                                    numUserIds = numUserIds2;
                                    userIds = userIds2;
                                    oldPermissionGroupName = oldPermissionGroupName2;
                                    newPermissionGroupName = newPermissionGroupName2;
                                    permissionName = permissionName2;
                                }
                                packageNum++;
                                permissionName2 = permissionName;
                                numPackages = numPackages;
                                userIdNum2 = userIdNum;
                                numUserIds2 = numUserIds;
                                userIds2 = userIds;
                                oldPermissionGroupName2 = oldPermissionGroupName;
                                newPermissionGroupName2 = newPermissionGroupName;
                                permissionManagerService = this;
                            }
                            userIdNum2++;
                            numOldPackagePermissions = numOldPackagePermissions;
                            oldPermissionNameToGroupName = oldPermissionNameToGroupName;
                            permissionManagerService = this;
                        }
                    }
                }
            }
            newPermissionNum++;
            permissionManagerService = this;
            numOldPackagePermissions = numOldPackagePermissions;
            oldPermissionNameToGroupName = oldPermissionNameToGroupName;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void addAllPermissions(PackageParser.Package pkg, boolean chatty) {
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
    /* access modifiers changed from: public */
    private void addAllPermissionGroups(PackageParser.Package pkg, boolean chatty) {
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
            Log.d(TAG, "  Permission Groups: " + ((Object) r));
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void removeAllPermissions(PackageParser.Package pkg, boolean chatty) {
        ArraySet<String> appOpPkgs;
        ArraySet<String> appOpPkgs2;
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
                if (p.isAppOp() && (appOpPkgs2 = this.mSettings.mAppOpPermissionPackages.get(p.info.name)) != null) {
                    appOpPkgs2.remove(pkg.packageName);
                }
            }
            if (r != null && PackageManagerService.DEBUG_REMOVE) {
                Log.d(TAG, "  Permissions: " + ((Object) r));
            }
            int N2 = pkg.requestedPermissions.size();
            for (int i2 = 0; i2 < N2; i2++) {
                String perm = (String) pkg.requestedPermissions.get(i2);
                if (this.mSettings.isPermissionAppOp(perm) && (appOpPkgs = this.mSettings.mAppOpPermissionPackages.get(perm)) != null) {
                    appOpPkgs.remove(pkg.packageName);
                    if (appOpPkgs.isEmpty()) {
                        this.mSettings.mAppOpPermissionPackages.remove(perm);
                    }
                }
            }
            if (0 != 0 && PackageManagerService.DEBUG_REMOVE) {
                Log.d(TAG, "  Permissions: " + ((Object) null));
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean addDynamicPermission(PermissionInfo info, int callingUid, PermissionManagerServiceInternal.PermissionCallback callback) {
        boolean added;
        boolean changed;
        if (this.mPackageManagerInt.getInstantAppPackageName(callingUid) != null) {
            throw new SecurityException("Instant apps can't add permissions");
        } else if (info.labelRes == 0 && info.nonLocalizedLabel == null) {
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
    /* access modifiers changed from: public */
    private void removeDynamicPermission(String permName, int callingUid, PermissionManagerServiceInternal.PermissionCallback callback) {
        if (this.mPackageManagerInt.getInstantAppPackageName(callingUid) == null) {
            this.mSettings.enforcePermissionTree(permName, callingUid);
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
                    return;
                }
                return;
            }
        }
        throw new SecurityException("Instant applications don't have access to this method");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:429:0x0898, code lost:
        if (r10.equals(r9.packageName) != false) goto L_0x089a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:459:0x09d4, code lost:
        if (r11 != false) goto L_0x09d9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:466:0x09e3, code lost:
        if (r12.isSystem() != false) goto L_0x09f7;
     */
    /* JADX WARNING: Removed duplicated region for block: B:121:0x02db  */
    /* JADX WARNING: Removed duplicated region for block: B:122:0x02df  */
    /* JADX WARNING: Removed duplicated region for block: B:184:0x0414  */
    /* JADX WARNING: Removed duplicated region for block: B:185:0x0416  */
    /* JADX WARNING: Removed duplicated region for block: B:188:0x0421  */
    /* JADX WARNING: Removed duplicated region for block: B:189:0x0423  */
    /* JADX WARNING: Removed duplicated region for block: B:191:0x0426  */
    /* JADX WARNING: Removed duplicated region for block: B:223:0x0484 A[Catch:{ all -> 0x04da }] */
    /* JADX WARNING: Removed duplicated region for block: B:241:0x04b7 A[Catch:{ all -> 0x04da }] */
    /* JADX WARNING: Removed duplicated region for block: B:244:0x04be A[Catch:{ all -> 0x04da }] */
    /* JADX WARNING: Removed duplicated region for block: B:273:0x0586 A[SYNTHETIC, Splitter:B:273:0x0586] */
    /* JADX WARNING: Removed duplicated region for block: B:277:0x05a3  */
    /* JADX WARNING: Removed duplicated region for block: B:280:0x05b5  */
    /* JADX WARNING: Removed duplicated region for block: B:281:0x05b7  */
    /* JADX WARNING: Removed duplicated region for block: B:286:0x05c4 A[Catch:{ all -> 0x06c7 }] */
    /* JADX WARNING: Removed duplicated region for block: B:287:0x05c6 A[Catch:{ all -> 0x06c7 }] */
    /* JADX WARNING: Removed duplicated region for block: B:289:0x05c9 A[Catch:{ all -> 0x06c7 }] */
    /* JADX WARNING: Removed duplicated region for block: B:329:0x0644 A[Catch:{ all -> 0x06af }] */
    /* JADX WARNING: Removed duplicated region for block: B:355:0x0693 A[Catch:{ all -> 0x06af }] */
    /* JADX WARNING: Removed duplicated region for block: B:358:0x0699 A[Catch:{ all -> 0x06af }] */
    /* JADX WARNING: Removed duplicated region for block: B:489:0x0a3b  */
    /* JADX WARNING: Removed duplicated region for block: B:492:0x0a42 A[LOOP:5: B:491:0x0a40->B:492:0x0a42, LOOP_END] */
    private void restorePermissionState(PackageParser.Package pkg, boolean replace, String packageOfInterest, PermissionManagerServiceInternal.PermissionCallback callback) {
        PermissionsState origPermissions;
        boolean runtimePermissionsRevoked;
        Object obj;
        Throwable th;
        boolean z;
        String permName;
        int[] updatedUserIds;
        String upgradedActivityRecognitionPermission;
        int[] currentUserIds;
        PackageSetting ps;
        ArraySet<String> newImplicitPermissions;
        int i;
        PackageParser.Package r9;
        PermissionsState origPermissions2;
        int[] updatedUserIds2;
        String permName2;
        int grant;
        boolean allowedSig;
        int grant2;
        StringBuilder sb;
        int userId;
        boolean permissionPolicyInitialized;
        PermissionsState.PermissionState permState;
        int flags;
        boolean wasChanged;
        int flags2;
        int flags3;
        int flags4;
        boolean permissionPolicyInitialized2;
        int userId2;
        boolean wasChanged2;
        PermissionsState origPermissions3;
        PermissionsState.PermissionState permState2;
        boolean allowedSig2;
        boolean allowedSig3;
        ArraySet<String> newImplicitPermissions2;
        PermissionManagerService permissionManagerService = this;
        PackageParser.Package r8 = pkg;
        String str = packageOfInterest;
        PackageSetting ps2 = (PackageSetting) r8.mExtras;
        if (ps2 != null) {
            PermissionsState permissionsState = ps2.getPermissionsState();
            int[] currentUserIds2 = UserManagerService.getInstance().getUserIds();
            boolean runtimePermissionsRevoked2 = false;
            int[] updatedUserIds3 = EMPTY_INT_ARRAY;
            if (replace) {
                ps2.setInstallPermissionsFixed(false);
                if (!ps2.isSharedUser()) {
                    PermissionsState origPermissions4 = new PermissionsState(permissionsState);
                    permissionsState.reset();
                    origPermissions = origPermissions4;
                    runtimePermissionsRevoked = false;
                } else {
                    synchronized (permissionManagerService.mLock) {
                        updatedUserIds3 = permissionManagerService.revokeUnusedSharedUserPermissionsLocked(ps2.getSharedUser(), UserManagerService.getInstance().getUserIds());
                        if (!ArrayUtils.isEmpty(updatedUserIds3)) {
                            runtimePermissionsRevoked2 = true;
                        }
                    }
                    origPermissions = permissionsState;
                    runtimePermissionsRevoked = runtimePermissionsRevoked2;
                }
            } else {
                origPermissions = permissionsState;
                runtimePermissionsRevoked = false;
            }
            permissionsState.setGlobalGids(permissionManagerService.mGlobalGids);
            Object obj2 = permissionManagerService.mLock;
            synchronized (obj2) {
                try {
                    ArraySet<String> newImplicitPermissions3 = new ArraySet<>();
                    int N = r8.requestedPermissions.size();
                    int[] updatedUserIds4 = updatedUserIds3;
                    int i2 = 0;
                    boolean changedInstallPermission = false;
                    while (i2 < N) {
                        try {
                            permName = (String) r8.requestedPermissions.get(i2);
                        } catch (Throwable th2) {
                            th = th2;
                            obj = obj2;
                            while (true) {
                                try {
                                    break;
                                } catch (Throwable th3) {
                                    th = th3;
                                }
                            }
                            throw th;
                        }
                        try {
                            BasePermission bp = permissionManagerService.mSettings.getPermissionLocked(permName);
                            boolean appSupportsRuntimePermissions = r8.applicationInfo.targetSdkVersion >= 23;
                            if (PackageManagerService.DEBUG_INSTALL) {
                                upgradedActivityRecognitionPermission = null;
                                updatedUserIds = updatedUserIds4;
                                try {
                                    Log.i(TAG, "Package " + r8.packageName + " checking " + permName + ": " + bp);
                                } catch (Throwable th4) {
                                    th = th4;
                                    obj = obj2;
                                }
                            } else {
                                updatedUserIds = updatedUserIds4;
                                upgradedActivityRecognitionPermission = null;
                            }
                            if (bp != null) {
                                try {
                                    if (bp.getSourcePackageSetting() == null) {
                                        permName2 = permName;
                                        newImplicitPermissions = newImplicitPermissions3;
                                        i = i2;
                                        r9 = r8;
                                        ps = ps2;
                                        currentUserIds = currentUserIds2;
                                        origPermissions2 = origPermissions;
                                        updatedUserIds2 = null;
                                    } else {
                                        if (origPermissions.hasRequestedPermission(permName)) {
                                            newImplicitPermissions = newImplicitPermissions3;
                                        } else if (!r8.implicitPermissions.contains(permName) && !permName.equals("android.permission.ACTIVITY_RECOGNITION")) {
                                            newImplicitPermissions = newImplicitPermissions3;
                                        } else if (!r8.implicitPermissions.contains(permName)) {
                                            int numSplitPerms = PermissionManager.SPLIT_PERMISSIONS.size();
                                            int splitPermNum = 0;
                                            while (true) {
                                                if (splitPermNum >= numSplitPerms) {
                                                    newImplicitPermissions = newImplicitPermissions3;
                                                    break;
                                                }
                                                PermissionManager.SplitPermissionInfo sp = (PermissionManager.SplitPermissionInfo) PermissionManager.SPLIT_PERMISSIONS.get(splitPermNum);
                                                String splitPermName = sp.getSplitPermission();
                                                if (!sp.getNewPermissions().contains(permName)) {
                                                    newImplicitPermissions2 = newImplicitPermissions3;
                                                } else if (origPermissions.hasInstallPermission(splitPermName)) {
                                                    upgradedActivityRecognitionPermission = splitPermName;
                                                    newImplicitPermissions3.add(permName);
                                                    if (PackageManagerService.DEBUG_PERMISSIONS) {
                                                        newImplicitPermissions = newImplicitPermissions3;
                                                        Slog.i(TAG, permName + " is newly added for " + r8.packageName);
                                                    } else {
                                                        newImplicitPermissions = newImplicitPermissions3;
                                                    }
                                                } else {
                                                    newImplicitPermissions2 = newImplicitPermissions3;
                                                }
                                                splitPermNum++;
                                                numSplitPerms = numSplitPerms;
                                                newImplicitPermissions3 = newImplicitPermissions2;
                                            }
                                        } else {
                                            newImplicitPermissions3.add(permName);
                                            if (PackageManagerService.DEBUG_PERMISSIONS) {
                                                Slog.i(TAG, permName + " is newly added for " + r8.packageName);
                                                newImplicitPermissions = newImplicitPermissions3;
                                            } else {
                                                newImplicitPermissions = newImplicitPermissions3;
                                            }
                                        }
                                        if (r8.applicationInfo.isInstantApp() && !bp.isInstant()) {
                                            if (PackageManagerService.DEBUG_PERMISSIONS) {
                                                Log.i(TAG, "Denying non-ephemeral permission " + bp.getName() + " for package " + r8.packageName);
                                                i = i2;
                                                r9 = r8;
                                                ps = ps2;
                                                currentUserIds = currentUserIds2;
                                                origPermissions2 = origPermissions;
                                                updatedUserIds2 = null;
                                            } else {
                                                i = i2;
                                                r9 = r8;
                                                ps = ps2;
                                                currentUserIds = currentUserIds2;
                                                origPermissions2 = origPermissions;
                                                updatedUserIds2 = null;
                                            }
                                            updatedUserIds4 = updatedUserIds;
                                            i2 = i + 1;
                                            origPermissions = origPermissions2;
                                            r8 = r9;
                                            runtimePermissionsRevoked = runtimePermissionsRevoked;
                                            N = N;
                                            newImplicitPermissions3 = newImplicitPermissions;
                                            ps2 = ps;
                                            currentUserIds2 = currentUserIds;
                                            permissionManagerService = this;
                                        } else if (!bp.isRuntimeOnly() || appSupportsRuntimePermissions) {
                                            String perm = bp.getName();
                                            if (bp.isAppOp()) {
                                                allowedSig = false;
                                                grant = 1;
                                                permissionManagerService.mSettings.addAppOpPackage(perm, r8.packageName);
                                            } else {
                                                allowedSig = false;
                                                grant = 1;
                                            }
                                            if (bp.isNormal()) {
                                                grant2 = 2;
                                            } else if (bp.isRuntime()) {
                                                grant2 = (origPermissions.hasInstallPermission(bp.getName()) || upgradedActivityRecognitionPermission != null) ? 4 : 3;
                                            } else if (bp.isSignature()) {
                                                boolean allowedSig4 = permissionManagerService.grantSignaturePermission(perm, r8, bp, origPermissions);
                                                if (!allowedSig4 && permissionManagerService.mPackageManager != null && permissionManagerService.mPackageManager.getHwPMSEx().isSystemAppGrantByMdm(r8)) {
                                                    allowedSig4 = true;
                                                }
                                                if (permissionManagerService.mCustPms != null) {
                                                    allowedSig3 = allowedSig4;
                                                    if (permissionManagerService.mCustPms.isHwFiltReqInstallPerm(r8.packageName, perm)) {
                                                        allowedSig2 = false;
                                                        if (!allowedSig2) {
                                                            grant2 = 2;
                                                            allowedSig = allowedSig2;
                                                        } else {
                                                            allowedSig = allowedSig2;
                                                            grant2 = grant;
                                                        }
                                                    }
                                                } else {
                                                    allowedSig3 = allowedSig4;
                                                }
                                                allowedSig2 = allowedSig3;
                                                if (!allowedSig2) {
                                                }
                                            } else {
                                                grant2 = grant;
                                            }
                                            if (PackageManagerService.DEBUG_PERMISSIONS) {
                                                i = i2;
                                                Slog.i(TAG, "Considering granting permission " + perm + " to package " + r8.packageName);
                                            } else {
                                                i = i2;
                                            }
                                            if (grant2 != 1) {
                                                try {
                                                    if (!ps2.isSystem() && ps2.areInstallPermissionsFixed() && !bp.isRuntime() && !allowedSig && !origPermissions.hasInstallPermission(perm) && !permissionManagerService.isNewPlatformPermissionForPackage(perm, r8)) {
                                                        grant2 = 1;
                                                    }
                                                    if (grant2 == 2) {
                                                        ps = ps2;
                                                        currentUserIds = currentUserIds2;
                                                        PermissionsState origPermissions5 = origPermissions;
                                                        try {
                                                            int[] userIds = UserManagerService.getInstance().getUserIds();
                                                            int length = userIds.length;
                                                            updatedUserIds4 = updatedUserIds;
                                                            int i3 = 0;
                                                            while (i3 < length) {
                                                                try {
                                                                    int userId3 = userIds[i3];
                                                                    if (origPermissions5.getRuntimePermissionState(perm, userId3) != null) {
                                                                        origPermissions5.revokeRuntimePermission(bp, userId3);
                                                                        origPermissions5.updatePermissionFlags(bp, userId3, 130047, 0);
                                                                        updatedUserIds4 = ArrayUtils.appendInt(updatedUserIds4, userId3);
                                                                    }
                                                                    i3++;
                                                                    origPermissions5 = origPermissions5;
                                                                } catch (Throwable th5) {
                                                                    th = th5;
                                                                    obj = obj2;
                                                                    while (true) {
                                                                        break;
                                                                    }
                                                                    throw th;
                                                                }
                                                            }
                                                            origPermissions2 = origPermissions5;
                                                            if (permissionsState.grantInstallPermission(bp) != -1) {
                                                                r9 = pkg;
                                                                str = packageOfInterest;
                                                                changedInstallPermission = true;
                                                                updatedUserIds2 = null;
                                                            } else {
                                                                r9 = pkg;
                                                                str = packageOfInterest;
                                                                updatedUserIds2 = null;
                                                            }
                                                            i2 = i + 1;
                                                            origPermissions = origPermissions2;
                                                            r8 = r9;
                                                            runtimePermissionsRevoked = runtimePermissionsRevoked;
                                                            N = N;
                                                            newImplicitPermissions3 = newImplicitPermissions;
                                                            ps2 = ps;
                                                            currentUserIds2 = currentUserIds;
                                                            permissionManagerService = this;
                                                        } catch (Throwable th6) {
                                                            th = th6;
                                                            obj = obj2;
                                                            while (true) {
                                                                break;
                                                            }
                                                            throw th;
                                                        }
                                                    } else if (grant2 == 3) {
                                                        String perm2 = perm;
                                                        ps = ps2;
                                                        PermissionsState origPermissions6 = origPermissions;
                                                        try {
                                                            boolean hardRestricted = bp.isHardRestricted();
                                                            boolean softRestricted = bp.isSoftRestricted();
                                                            int length2 = currentUserIds2.length;
                                                            int[] updatedUserIds5 = updatedUserIds;
                                                            int i4 = 0;
                                                            while (i4 < length2) {
                                                                try {
                                                                    userId = currentUserIds2[i4];
                                                                    if (this.mPermissionPolicyInternal != null) {
                                                                        try {
                                                                            if (this.mPermissionPolicyInternal.isInitialized(userId)) {
                                                                                permissionPolicyInitialized = true;
                                                                                permState = origPermissions6.getRuntimePermissionState(perm2, userId);
                                                                                if (permState == null) {
                                                                                    try {
                                                                                        flags = permState.getFlags();
                                                                                    } catch (Throwable th7) {
                                                                                        th = th7;
                                                                                        obj = obj2;
                                                                                    }
                                                                                } else {
                                                                                    flags = 0;
                                                                                }
                                                                                wasChanged = false;
                                                                                boolean restrictionExempt = (origPermissions6.getPermissionFlags(bp.name, userId) & 14336) == 0;
                                                                                try {
                                                                                    boolean restrictionApplied = (origPermissions6.getPermissionFlags(bp.name, userId) & DumpState.DUMP_KEYSETS) == 0;
                                                                                    if (!appSupportsRuntimePermissions) {
                                                                                        if (!permissionPolicyInitialized || !hardRestricted) {
                                                                                            origPermissions6 = origPermissions6;
                                                                                            flags3 = flags;
                                                                                            if (permissionPolicyInitialized && softRestricted && !restrictionExempt && !restrictionApplied) {
                                                                                                flags3 |= DumpState.DUMP_KEYSETS;
                                                                                                wasChanged = true;
                                                                                            }
                                                                                        } else if (!restrictionExempt) {
                                                                                            if (permState == null || !permState.isGranted()) {
                                                                                                origPermissions6 = origPermissions6;
                                                                                            } else {
                                                                                                origPermissions6 = origPermissions6;
                                                                                                if (permissionsState.revokeRuntimePermission(bp, userId) != -1) {
                                                                                                    wasChanged = true;
                                                                                                }
                                                                                            }
                                                                                            if (!restrictionApplied) {
                                                                                                flags3 = flags | DumpState.DUMP_KEYSETS;
                                                                                                wasChanged = true;
                                                                                            } else {
                                                                                                flags3 = flags;
                                                                                            }
                                                                                        } else {
                                                                                            origPermissions6 = origPermissions6;
                                                                                            flags3 = flags;
                                                                                        }
                                                                                        if ((flags3 & 64) != 0) {
                                                                                            flags3 &= -65;
                                                                                            wasChanged = true;
                                                                                        }
                                                                                        if ((flags3 & 8) != 0) {
                                                                                            flags2 = flags3 & -9;
                                                                                            wasChanged = true;
                                                                                        } else {
                                                                                            if (!permissionPolicyInitialized || !hardRestricted || restrictionExempt) {
                                                                                                if (permState != null) {
                                                                                                    try {
                                                                                                        if (permState.isGranted()) {
                                                                                                            flags4 = flags3;
                                                                                                            if (permissionsState.grantRuntimePermission(bp, userId) == -1) {
                                                                                                                wasChanged = true;
                                                                                                                flags2 = flags4;
                                                                                                            }
                                                                                                        }
                                                                                                    } catch (Throwable th8) {
                                                                                                        th = th8;
                                                                                                        obj = obj2;
                                                                                                        while (true) {
                                                                                                            break;
                                                                                                        }
                                                                                                        throw th;
                                                                                                    }
                                                                                                }
                                                                                                flags4 = flags3;
                                                                                            } else {
                                                                                                flags4 = flags3;
                                                                                            }
                                                                                            flags2 = flags4;
                                                                                        }
                                                                                    } else {
                                                                                        origPermissions6 = origPermissions6;
                                                                                        flags2 = flags;
                                                                                        if (permState == null) {
                                                                                            if (PackageManagerService.PLATFORM_PACKAGE_NAME.equals(bp.getSourcePackageName()) && !bp.isRemoved()) {
                                                                                                flags2 |= 72;
                                                                                                wasChanged = true;
                                                                                            }
                                                                                        }
                                                                                        if (!permissionsState.hasRuntimePermission(bp.name, userId) && permissionsState.grantRuntimePermission(bp, userId) != -1) {
                                                                                            wasChanged = true;
                                                                                        }
                                                                                        if (permissionPolicyInitialized && ((hardRestricted || softRestricted) && !restrictionExempt && !restrictionApplied)) {
                                                                                            flags2 |= DumpState.DUMP_KEYSETS;
                                                                                            wasChanged = true;
                                                                                        }
                                                                                    }
                                                                                    if (permissionPolicyInitialized && (((!hardRestricted && !softRestricted) || restrictionExempt) && restrictionApplied)) {
                                                                                        flags2 &= -16385;
                                                                                        if (!appSupportsRuntimePermissions) {
                                                                                            flags2 |= 64;
                                                                                        }
                                                                                        wasChanged = true;
                                                                                    }
                                                                                    if (wasChanged) {
                                                                                        updatedUserIds5 = ArrayUtils.appendInt(updatedUserIds5, userId);
                                                                                    }
                                                                                    permissionsState.updatePermissionFlags(bp, userId, 130047, flags2);
                                                                                    i4++;
                                                                                    length2 = length2;
                                                                                    currentUserIds2 = currentUserIds2;
                                                                                    perm2 = perm2;
                                                                                } catch (Throwable th9) {
                                                                                    th = th9;
                                                                                    obj = obj2;
                                                                                    while (true) {
                                                                                        break;
                                                                                    }
                                                                                    throw th;
                                                                                }
                                                                            }
                                                                        } catch (Throwable th10) {
                                                                            th = th10;
                                                                            obj = obj2;
                                                                            while (true) {
                                                                                break;
                                                                            }
                                                                            throw th;
                                                                        }
                                                                    }
                                                                    permissionPolicyInitialized = false;
                                                                } catch (Throwable th11) {
                                                                    th = th11;
                                                                    obj = obj2;
                                                                    while (true) {
                                                                        break;
                                                                    }
                                                                    throw th;
                                                                }
                                                                try {
                                                                    permState = origPermissions6.getRuntimePermissionState(perm2, userId);
                                                                    if (permState == null) {
                                                                    }
                                                                    wasChanged = false;
                                                                    if ((origPermissions6.getPermissionFlags(bp.name, userId) & 14336) == 0) {
                                                                    }
                                                                    if ((origPermissions6.getPermissionFlags(bp.name, userId) & DumpState.DUMP_KEYSETS) == 0) {
                                                                    }
                                                                    if (!appSupportsRuntimePermissions) {
                                                                    }
                                                                    flags2 &= -16385;
                                                                    if (!appSupportsRuntimePermissions) {
                                                                    }
                                                                    wasChanged = true;
                                                                    if (wasChanged) {
                                                                    }
                                                                    permissionsState.updatePermissionFlags(bp, userId, 130047, flags2);
                                                                    i4++;
                                                                    length2 = length2;
                                                                    currentUserIds2 = currentUserIds2;
                                                                    perm2 = perm2;
                                                                } catch (Throwable th12) {
                                                                    th = th12;
                                                                    obj = obj2;
                                                                    while (true) {
                                                                        break;
                                                                    }
                                                                    throw th;
                                                                }
                                                            }
                                                            currentUserIds = currentUserIds2;
                                                            r9 = pkg;
                                                            str = packageOfInterest;
                                                            updatedUserIds4 = updatedUserIds5;
                                                            origPermissions2 = origPermissions6;
                                                            updatedUserIds2 = null;
                                                            i2 = i + 1;
                                                            origPermissions = origPermissions2;
                                                            r8 = r9;
                                                            runtimePermissionsRevoked = runtimePermissionsRevoked;
                                                            N = N;
                                                            newImplicitPermissions3 = newImplicitPermissions;
                                                            ps2 = ps;
                                                            currentUserIds2 = currentUserIds;
                                                            permissionManagerService = this;
                                                        } catch (Throwable th13) {
                                                            th = th13;
                                                            obj = obj2;
                                                            while (true) {
                                                                break;
                                                            }
                                                            throw th;
                                                        }
                                                    } else if (grant2 == 4) {
                                                        try {
                                                            PermissionsState.PermissionState permState3 = origPermissions.getInstallPermissionState(perm);
                                                            int flags5 = permState3 != null ? permState3.getFlags() : 0;
                                                            BasePermission bpToRevoke = upgradedActivityRecognitionPermission == null ? bp : permissionManagerService.mSettings.getPermissionLocked(upgradedActivityRecognitionPermission);
                                                            if (origPermissions.revokeInstallPermission(bpToRevoke) != -1) {
                                                                origPermissions.updatePermissionFlags(bpToRevoke, -1, 113663, 0);
                                                                changedInstallPermission = true;
                                                            }
                                                            boolean hardRestricted2 = bp.isHardRestricted();
                                                            boolean softRestricted2 = bp.isSoftRestricted();
                                                            int length3 = currentUserIds2.length;
                                                            int[] updatedUserIds6 = updatedUserIds;
                                                            int i5 = 0;
                                                            ps = ps2;
                                                            int flags6 = flags5;
                                                            while (i5 < length3) {
                                                                try {
                                                                    int userId4 = currentUserIds2[i5];
                                                                    if (permissionManagerService.mPermissionPolicyInternal != null) {
                                                                        try {
                                                                            userId2 = userId4;
                                                                            if (permissionManagerService.mPermissionPolicyInternal.isInitialized(userId2)) {
                                                                                permissionPolicyInitialized2 = true;
                                                                                wasChanged2 = false;
                                                                                boolean restrictionExempt2 = (origPermissions.getPermissionFlags(bp.name, userId2) & 14336) == 0;
                                                                                boolean restrictionApplied2 = (origPermissions.getPermissionFlags(bp.name, userId2) & DumpState.DUMP_KEYSETS) == 0;
                                                                                if (!appSupportsRuntimePermissions) {
                                                                                    if (!permissionPolicyInitialized2 || !hardRestricted2) {
                                                                                        permState2 = permState3;
                                                                                        origPermissions3 = origPermissions;
                                                                                        if (permissionPolicyInitialized2 && softRestricted2 && !restrictionExempt2 && !restrictionApplied2) {
                                                                                            flags6 |= DumpState.DUMP_KEYSETS;
                                                                                            wasChanged2 = true;
                                                                                        }
                                                                                    } else if (!restrictionExempt2) {
                                                                                        if (permState3 == null || !permState3.isGranted()) {
                                                                                            permState2 = permState3;
                                                                                            origPermissions3 = origPermissions;
                                                                                        } else {
                                                                                            permState2 = permState3;
                                                                                            origPermissions3 = origPermissions;
                                                                                            if (permissionsState.revokeRuntimePermission(bp, userId2) != -1) {
                                                                                                wasChanged2 = true;
                                                                                            }
                                                                                        }
                                                                                        if (!restrictionApplied2) {
                                                                                            flags6 |= DumpState.DUMP_KEYSETS;
                                                                                            wasChanged2 = true;
                                                                                        }
                                                                                    } else {
                                                                                        permState2 = permState3;
                                                                                        origPermissions3 = origPermissions;
                                                                                    }
                                                                                    if ((flags6 & 64) != 0) {
                                                                                        flags6 &= -65;
                                                                                        wasChanged2 = true;
                                                                                    }
                                                                                    if ((flags6 & 8) != 0) {
                                                                                        flags6 &= -9;
                                                                                        wasChanged2 = true;
                                                                                    } else if (!permissionPolicyInitialized2 || !hardRestricted2 || restrictionExempt2) {
                                                                                        try {
                                                                                            if (permissionsState.grantRuntimePermission(bp, userId2) != -1) {
                                                                                                wasChanged2 = true;
                                                                                            }
                                                                                        } catch (Throwable th14) {
                                                                                            th = th14;
                                                                                            obj = obj2;
                                                                                            while (true) {
                                                                                                break;
                                                                                            }
                                                                                            throw th;
                                                                                        }
                                                                                    }
                                                                                } else {
                                                                                    permState2 = permState3;
                                                                                    origPermissions3 = origPermissions;
                                                                                    if (!permissionsState.hasRuntimePermission(bp.name, userId2) && permissionsState.grantRuntimePermission(bp, userId2) != -1) {
                                                                                        flags6 |= 64;
                                                                                        wasChanged2 = true;
                                                                                    }
                                                                                    if (permissionPolicyInitialized2 && ((hardRestricted2 || softRestricted2) && !restrictionExempt2 && !restrictionApplied2)) {
                                                                                        flags6 |= DumpState.DUMP_KEYSETS;
                                                                                        wasChanged2 = true;
                                                                                    }
                                                                                }
                                                                                if (permissionPolicyInitialized2 && (((!hardRestricted2 && !softRestricted2) || restrictionExempt2) && restrictionApplied2)) {
                                                                                    int flags7 = flags6 & -16385;
                                                                                    if (!appSupportsRuntimePermissions) {
                                                                                        flags7 |= 64;
                                                                                    }
                                                                                    wasChanged2 = true;
                                                                                    flags6 = flags7;
                                                                                }
                                                                                if (wasChanged2) {
                                                                                    updatedUserIds6 = ArrayUtils.appendInt(updatedUserIds6, userId2);
                                                                                }
                                                                                permissionsState.updatePermissionFlags(bp, userId2, 130047, flags6);
                                                                                i5++;
                                                                                permissionManagerService = this;
                                                                                perm = perm;
                                                                                length3 = length3;
                                                                                permState3 = permState2;
                                                                                origPermissions = origPermissions3;
                                                                            }
                                                                        } catch (Throwable th15) {
                                                                            th = th15;
                                                                            obj = obj2;
                                                                            while (true) {
                                                                                break;
                                                                            }
                                                                            throw th;
                                                                        }
                                                                    } else {
                                                                        userId2 = userId4;
                                                                    }
                                                                    permissionPolicyInitialized2 = false;
                                                                    wasChanged2 = false;
                                                                    if ((origPermissions.getPermissionFlags(bp.name, userId2) & 14336) == 0) {
                                                                    }
                                                                    if ((origPermissions.getPermissionFlags(bp.name, userId2) & DumpState.DUMP_KEYSETS) == 0) {
                                                                    }
                                                                    if (!appSupportsRuntimePermissions) {
                                                                    }
                                                                    int flags72 = flags6 & -16385;
                                                                    if (!appSupportsRuntimePermissions) {
                                                                    }
                                                                    wasChanged2 = true;
                                                                    flags6 = flags72;
                                                                    if (wasChanged2) {
                                                                    }
                                                                    permissionsState.updatePermissionFlags(bp, userId2, 130047, flags6);
                                                                    i5++;
                                                                    permissionManagerService = this;
                                                                    perm = perm;
                                                                    length3 = length3;
                                                                    permState3 = permState2;
                                                                    origPermissions = origPermissions3;
                                                                } catch (Throwable th16) {
                                                                    th = th16;
                                                                    obj = obj2;
                                                                    while (true) {
                                                                        break;
                                                                    }
                                                                    throw th;
                                                                }
                                                            }
                                                            r9 = pkg;
                                                            updatedUserIds4 = updatedUserIds6;
                                                            currentUserIds = currentUserIds2;
                                                            origPermissions2 = origPermissions;
                                                            updatedUserIds2 = null;
                                                            str = packageOfInterest;
                                                            i2 = i + 1;
                                                            origPermissions = origPermissions2;
                                                            r8 = r9;
                                                            runtimePermissionsRevoked = runtimePermissionsRevoked;
                                                            N = N;
                                                            newImplicitPermissions3 = newImplicitPermissions;
                                                            ps2 = ps;
                                                            currentUserIds2 = currentUserIds;
                                                            permissionManagerService = this;
                                                        } catch (Throwable th17) {
                                                            th = th17;
                                                            obj = obj2;
                                                            while (true) {
                                                                break;
                                                            }
                                                            throw th;
                                                        }
                                                    } else if (str == null || str.equals(r8.packageName)) {
                                                        if (PackageManagerService.DEBUG_PERMISSIONS) {
                                                            Slog.i(TAG, "Not granting permission " + perm + " to package " + r8.packageName + " because it was previously installed without");
                                                            r9 = r8;
                                                            ps = ps2;
                                                            currentUserIds = currentUserIds2;
                                                            origPermissions2 = origPermissions;
                                                            updatedUserIds2 = null;
                                                        } else {
                                                            r9 = r8;
                                                            ps = ps2;
                                                            currentUserIds = currentUserIds2;
                                                            origPermissions2 = origPermissions;
                                                            updatedUserIds2 = null;
                                                        }
                                                        updatedUserIds4 = updatedUserIds;
                                                        i2 = i + 1;
                                                        origPermissions = origPermissions2;
                                                        r8 = r9;
                                                        runtimePermissionsRevoked = runtimePermissionsRevoked;
                                                        N = N;
                                                        newImplicitPermissions3 = newImplicitPermissions;
                                                        ps2 = ps;
                                                        currentUserIds2 = currentUserIds;
                                                        permissionManagerService = this;
                                                    } else {
                                                        r9 = r8;
                                                        ps = ps2;
                                                        currentUserIds = currentUserIds2;
                                                        origPermissions2 = origPermissions;
                                                        updatedUserIds2 = null;
                                                        updatedUserIds4 = updatedUserIds;
                                                        i2 = i + 1;
                                                        origPermissions = origPermissions2;
                                                        r8 = r9;
                                                        runtimePermissionsRevoked = runtimePermissionsRevoked;
                                                        N = N;
                                                        newImplicitPermissions3 = newImplicitPermissions;
                                                        ps2 = ps;
                                                        currentUserIds2 = currentUserIds;
                                                        permissionManagerService = this;
                                                    }
                                                } catch (Throwable th18) {
                                                    th = th18;
                                                    obj = obj2;
                                                    while (true) {
                                                        break;
                                                    }
                                                    throw th;
                                                }
                                            } else {
                                                ps = ps2;
                                                currentUserIds = currentUserIds2;
                                                origPermissions2 = origPermissions;
                                                try {
                                                    if (permissionsState.revokeInstallPermission(bp) != -1) {
                                                        updatedUserIds2 = null;
                                                        try {
                                                            permissionsState.updatePermissionFlags(bp, -1, 130047, 0);
                                                            try {
                                                                sb = new StringBuilder();
                                                                sb.append("Un-granting permission ");
                                                                sb.append(perm);
                                                                sb.append(" from package ");
                                                                r9 = pkg;
                                                            } catch (Throwable th19) {
                                                                th = th19;
                                                                obj = obj2;
                                                                while (true) {
                                                                    break;
                                                                }
                                                                throw th;
                                                            }
                                                        } catch (Throwable th20) {
                                                            th = th20;
                                                            obj = obj2;
                                                            while (true) {
                                                                break;
                                                            }
                                                            throw th;
                                                        }
                                                        try {
                                                            sb.append(r9.packageName);
                                                            sb.append(" (protectionLevel=");
                                                            sb.append(bp.getProtectionLevel());
                                                            sb.append(" flags=0x");
                                                            sb.append(Integer.toHexString(r9.applicationInfo.flags));
                                                            sb.append(")");
                                                            Slog.i(TAG, sb.toString());
                                                            str = packageOfInterest;
                                                            changedInstallPermission = true;
                                                            updatedUserIds4 = updatedUserIds;
                                                            i2 = i + 1;
                                                            origPermissions = origPermissions2;
                                                            r8 = r9;
                                                            runtimePermissionsRevoked = runtimePermissionsRevoked;
                                                            N = N;
                                                            newImplicitPermissions3 = newImplicitPermissions;
                                                            ps2 = ps;
                                                            currentUserIds2 = currentUserIds;
                                                            permissionManagerService = this;
                                                        } catch (Throwable th21) {
                                                            th = th21;
                                                            obj = obj2;
                                                            while (true) {
                                                                break;
                                                            }
                                                            throw th;
                                                        }
                                                    } else {
                                                        r9 = pkg;
                                                        updatedUserIds2 = null;
                                                        try {
                                                            if (!bp.isAppOp()) {
                                                                str = packageOfInterest;
                                                            } else if (PackageManagerService.DEBUG_PERMISSIONS) {
                                                                str = packageOfInterest;
                                                                if (str != null) {
                                                                }
                                                                Slog.i(TAG, "Not granting permission " + perm + " to package " + r9.packageName + " (protectionLevel=" + bp.getProtectionLevel() + " flags=0x" + Integer.toHexString(r9.applicationInfo.flags) + ")");
                                                            } else {
                                                                str = packageOfInterest;
                                                            }
                                                            updatedUserIds4 = updatedUserIds;
                                                            i2 = i + 1;
                                                            origPermissions = origPermissions2;
                                                            r8 = r9;
                                                            runtimePermissionsRevoked = runtimePermissionsRevoked;
                                                            N = N;
                                                            newImplicitPermissions3 = newImplicitPermissions;
                                                            ps2 = ps;
                                                            currentUserIds2 = currentUserIds;
                                                            permissionManagerService = this;
                                                        } catch (Throwable th22) {
                                                            th = th22;
                                                            obj = obj2;
                                                            while (true) {
                                                                break;
                                                            }
                                                            throw th;
                                                        }
                                                    }
                                                } catch (Throwable th23) {
                                                    th = th23;
                                                    obj = obj2;
                                                    while (true) {
                                                        break;
                                                    }
                                                    throw th;
                                                }
                                            }
                                        } else {
                                            if (PackageManagerService.DEBUG_PERMISSIONS) {
                                                Log.i(TAG, "Denying runtime-only permission " + bp.getName() + " for package " + r8.packageName);
                                                i = i2;
                                                r9 = r8;
                                                ps = ps2;
                                                currentUserIds = currentUserIds2;
                                                origPermissions2 = origPermissions;
                                                updatedUserIds2 = null;
                                            } else {
                                                i = i2;
                                                r9 = r8;
                                                ps = ps2;
                                                currentUserIds = currentUserIds2;
                                                origPermissions2 = origPermissions;
                                                updatedUserIds2 = null;
                                            }
                                            updatedUserIds4 = updatedUserIds;
                                            i2 = i + 1;
                                            origPermissions = origPermissions2;
                                            r8 = r9;
                                            runtimePermissionsRevoked = runtimePermissionsRevoked;
                                            N = N;
                                            newImplicitPermissions3 = newImplicitPermissions;
                                            ps2 = ps;
                                            currentUserIds2 = currentUserIds;
                                            permissionManagerService = this;
                                        }
                                    }
                                } catch (Throwable th24) {
                                    th = th24;
                                    obj = obj2;
                                    while (true) {
                                        break;
                                    }
                                    throw th;
                                }
                            } else {
                                permName2 = permName;
                                newImplicitPermissions = newImplicitPermissions3;
                                i = i2;
                                r9 = r8;
                                ps = ps2;
                                currentUserIds = currentUserIds2;
                                origPermissions2 = origPermissions;
                                updatedUserIds2 = null;
                            }
                            if (str == null || str.equals(r9.packageName)) {
                                if (PackageManagerService.DEBUG_PERMISSIONS) {
                                    Slog.i(TAG, "Unknown permission " + permName2 + " in package " + r9.packageName);
                                }
                            }
                            updatedUserIds4 = updatedUserIds;
                            i2 = i + 1;
                            origPermissions = origPermissions2;
                            r8 = r9;
                            runtimePermissionsRevoked = runtimePermissionsRevoked;
                            N = N;
                            newImplicitPermissions3 = newImplicitPermissions;
                            ps2 = ps;
                            currentUserIds2 = currentUserIds;
                            permissionManagerService = this;
                        } catch (Throwable th25) {
                            th = th25;
                            obj = obj2;
                            while (true) {
                                break;
                            }
                            throw th;
                        }
                    }
                    if (!changedInstallPermission) {
                        z = replace;
                    } else {
                        z = replace;
                    }
                    try {
                        if (!ps2.areInstallPermissionsFixed()) {
                            try {
                            } catch (Throwable th26) {
                                th = th26;
                                obj = obj2;
                                while (true) {
                                    break;
                                }
                                throw th;
                            }
                        }
                        if (!ps2.isUpdatedSystem()) {
                            try {
                                obj = obj2;
                                try {
                                    try {
                                        int[] updatedUserIds7 = checkIfLegacyStorageOpsNeedToBeUpdated(r8, z, setInitialGrantForNewImplicitPermissionsLocked(origPermissions, permissionsState, pkg, newImplicitPermissions3, revokePermissionsNoLongerImplicitLocked(permissionsState, r8, updatedUserIds4)));
                                        if (callback != null) {
                                            callback.onPermissionUpdated(updatedUserIds7, runtimePermissionsRevoked);
                                        }
                                        for (int userId5 : updatedUserIds7) {
                                            notifyRuntimePermissionStateChanged(r8.packageName, userId5);
                                        }
                                    } catch (Throwable th27) {
                                        th = th27;
                                        while (true) {
                                            break;
                                        }
                                        throw th;
                                    }
                                } catch (Throwable th28) {
                                    th = th28;
                                    while (true) {
                                        break;
                                    }
                                    throw th;
                                }
                            } catch (Throwable th29) {
                                th = th29;
                                obj = obj2;
                                while (true) {
                                    break;
                                }
                                throw th;
                            }
                        }
                        try {
                            ps2.setInstallPermissionsFixed(true);
                            obj = obj2;
                            int[] updatedUserIds72 = checkIfLegacyStorageOpsNeedToBeUpdated(r8, z, setInitialGrantForNewImplicitPermissionsLocked(origPermissions, permissionsState, pkg, newImplicitPermissions3, revokePermissionsNoLongerImplicitLocked(permissionsState, r8, updatedUserIds4)));
                            if (callback != null) {
                            }
                            while (r2 < r0) {
                            }
                        } catch (Throwable th30) {
                            th = th30;
                            obj = obj2;
                        }
                    } catch (Throwable th31) {
                        th = th31;
                        obj = obj2;
                        while (true) {
                            break;
                        }
                        throw th;
                    }
                } catch (Throwable th32) {
                    th = th32;
                    obj = obj2;
                    while (true) {
                        break;
                    }
                    throw th;
                }
            }
        }
    }

    private int[] revokePermissionsNoLongerImplicitLocked(PermissionsState ps, PackageParser.Package pkg, int[] updatedUserIds) {
        boolean supportsRuntimePermissions;
        PackageParser.Package r1 = pkg;
        String pkgName = r1.packageName;
        boolean supportsRuntimePermissions2 = r1.applicationInfo.targetSdkVersion >= 23;
        int[] users = UserManagerService.getInstance().getUserIds();
        int numUsers = users.length;
        int i = 0;
        int[] updatedUserIds2 = updatedUserIds;
        while (i < numUsers) {
            int userId = users[i];
            for (String permission : ps.getPermissions(userId)) {
                if (r1.implicitPermissions.contains(permission)) {
                    supportsRuntimePermissions = supportsRuntimePermissions2;
                } else if (!ps.hasInstallPermission(permission)) {
                    int flags = ps.getRuntimePermissionState(permission, userId).getFlags();
                    if ((flags & 128) != 0) {
                        BasePermission bp = this.mSettings.getPermissionLocked(permission);
                        int flagsToRemove = 128;
                        if ((flags & 52) != 0 || !supportsRuntimePermissions2) {
                            supportsRuntimePermissions = supportsRuntimePermissions2;
                        } else {
                            if (ps.revokeRuntimePermission(bp, userId) == -1) {
                                supportsRuntimePermissions = supportsRuntimePermissions2;
                            } else if (PackageManagerService.DEBUG_PERMISSIONS) {
                                StringBuilder sb = new StringBuilder();
                                supportsRuntimePermissions = supportsRuntimePermissions2;
                                sb.append("Revoking runtime permission ");
                                sb.append(permission);
                                sb.append(" for ");
                                sb.append(pkgName);
                                sb.append(" as it is now requested");
                                Slog.i(TAG, sb.toString());
                            } else {
                                supportsRuntimePermissions = supportsRuntimePermissions2;
                            }
                            flagsToRemove = 128 | 3;
                        }
                        ps.updatePermissionFlags(bp, userId, flagsToRemove, 0);
                        updatedUserIds2 = ArrayUtils.appendInt(updatedUserIds2, userId);
                    } else {
                        supportsRuntimePermissions = supportsRuntimePermissions2;
                    }
                } else {
                    supportsRuntimePermissions = supportsRuntimePermissions2;
                }
                r1 = pkg;
                supportsRuntimePermissions2 = supportsRuntimePermissions;
            }
            i++;
            r1 = pkg;
        }
        return updatedUserIds2;
    }

    private void inheritPermissionStateToNewImplicitPermissionLocked(ArraySet<String> sourcePerms, String newPerm, PermissionsState ps, PackageParser.Package pkg, int userId) {
        String pkgName = pkg.packageName;
        boolean isGranted = false;
        int flags = 0;
        int numSourcePerm = sourcePerms.size();
        for (int i = 0; i < numSourcePerm; i++) {
            String sourcePerm = sourcePerms.valueAt(i);
            if (ps.hasRuntimePermission(sourcePerm, userId) || ps.hasInstallPermission(sourcePerm)) {
                if (!isGranted) {
                    flags = 0;
                }
                isGranted = true;
                flags |= ps.getPermissionFlags(sourcePerm, userId);
            } else if (!isGranted) {
                flags |= ps.getPermissionFlags(sourcePerm, userId);
            }
        }
        if (isGranted) {
            if (PackageManagerService.DEBUG_PERMISSIONS) {
                Slog.i(TAG, newPerm + " inherits runtime perm grant from " + sourcePerms + " for " + pkgName);
            }
            ps.grantRuntimePermission(this.mSettings.getPermissionLocked(newPerm), userId);
        }
        ps.updatePermissionFlags(this.mSettings.getPermission(newPerm), userId, flags, flags);
    }

    private int[] checkIfLegacyStorageOpsNeedToBeUpdated(PackageParser.Package pkg, boolean replace, int[] updatedUserIds) {
        if (!replace || !pkg.applicationInfo.hasRequestedLegacyExternalStorage() || (!pkg.requestedPermissions.contains("android.permission.READ_EXTERNAL_STORAGE") && !pkg.requestedPermissions.contains("android.permission.WRITE_EXTERNAL_STORAGE"))) {
            return updatedUserIds;
        }
        return UserManagerService.getInstance().getUserIds();
    }

    private int[] setInitialGrantForNewImplicitPermissionsLocked(PermissionsState origPs, PermissionsState ps, PackageParser.Package pkg, ArraySet<String> newImplicitPermissions, int[] updatedUserIds) {
        boolean inheritsFromInstallPerm;
        String pkgName = pkg.packageName;
        ArrayMap<String, ArraySet<String>> newToSplitPerms = new ArrayMap<>();
        int numSplitPerms = PermissionManager.SPLIT_PERMISSIONS.size();
        for (int splitPermNum = 0; splitPermNum < numSplitPerms; splitPermNum++) {
            PermissionManager.SplitPermissionInfo spi = (PermissionManager.SplitPermissionInfo) PermissionManager.SPLIT_PERMISSIONS.get(splitPermNum);
            List<String> newPerms = spi.getNewPermissions();
            int numNewPerms = newPerms.size();
            for (int newPermNum = 0; newPermNum < numNewPerms; newPermNum++) {
                String newPerm = newPerms.get(newPermNum);
                ArraySet<String> splitPerms = newToSplitPerms.get(newPerm);
                if (splitPerms == null) {
                    splitPerms = new ArraySet<>();
                    newToSplitPerms.put(newPerm, splitPerms);
                }
                splitPerms.add(spi.getSplitPermission());
            }
        }
        int numNewImplicitPerms = newImplicitPermissions.size();
        int[] updatedUserIds2 = updatedUserIds;
        for (int newImplicitPermNum = 0; newImplicitPermNum < numNewImplicitPerms; newImplicitPermNum++) {
            String newPerm2 = newImplicitPermissions.valueAt(newImplicitPermNum);
            ArraySet<String> sourcePerms = newToSplitPerms.get(newPerm2);
            if (sourcePerms != null && !ps.hasInstallPermission(newPerm2)) {
                BasePermission bp = this.mSettings.getPermissionLocked(newPerm2);
                int[] users = UserManagerService.getInstance().getUserIds();
                int numUsers = users.length;
                int userNum = 0;
                while (true) {
                    if (userNum >= numUsers) {
                        break;
                    }
                    int userId = users[userNum];
                    if (!newPerm2.equals("android.permission.ACTIVITY_RECOGNITION")) {
                        ps.updatePermissionFlags(bp, userId, 128, 128);
                    }
                    int[] updatedUserIds3 = ArrayUtils.appendInt(updatedUserIds2, userId);
                    boolean inheritsFromInstallPerm2 = false;
                    int sourcePermNum = 0;
                    while (true) {
                        inheritsFromInstallPerm = inheritsFromInstallPerm2;
                        if (sourcePermNum >= sourcePerms.size()) {
                            break;
                        } else if (ps.hasInstallPermission(sourcePerms.valueAt(sourcePermNum))) {
                            inheritsFromInstallPerm = true;
                            break;
                        } else {
                            sourcePermNum++;
                            inheritsFromInstallPerm2 = inheritsFromInstallPerm;
                        }
                    }
                    if (origPs.hasRequestedPermission(sourcePerms) || inheritsFromInstallPerm) {
                        inheritPermissionStateToNewImplicitPermissionLocked(sourcePerms, newPerm2, ps, pkg, userId);
                        userNum++;
                        updatedUserIds2 = updatedUserIds3;
                        numUsers = numUsers;
                        users = users;
                        bp = bp;
                    } else {
                        if (PackageManagerService.DEBUG_PERMISSIONS) {
                            Slog.i(TAG, newPerm2 + " does not inherit from " + sourcePerms + " for " + pkgName + " as split permission is also new");
                        }
                        updatedUserIds2 = updatedUserIds3;
                    }
                }
            }
        }
        return updatedUserIds2;
    }

    private boolean isNewPlatformPermissionForPackage(String perm, PackageParser.Package pkg) {
        int NP = PackageParser.NEW_PERMISSIONS.length;
        for (int ip = 0; ip < NP; ip++) {
            PackageParser.NewPermissionInfo npi = PackageParser.NEW_PERMISSIONS[ip];
            if (npi.name.equals(perm) && pkg.applicationInfo.targetSdkVersion < npi.sdkVersion) {
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
        } else if (pkg.isProductServices()) {
            wlPermissions = SystemConfig.getInstance().getProductServicesPrivAppPermissions(pkg.packageName);
        } else {
            wlPermissions = SystemConfig.getInstance().getPrivAppPermissions(pkg.packageName);
        }
        if (!(wlPermissions != null && wlPermissions.contains(perm))) {
            return pkg.parentPackage != null && hasPrivappWhitelistEntry(perm, pkg.parentPackage);
        }
        return true;
    }

    private boolean grantSignaturePermission(String perm, PackageParser.Package pkg, BasePermission bp, PermissionsState origPermissions) {
        Iterator it;
        PackageSetting disabledChildPs;
        ArraySet<String> deniedPermissions;
        boolean oemPermission = bp.isOEM();
        boolean vendorPrivilegedPermission = bp.isVendorPrivileged();
        boolean privilegedPermission = bp.isPrivileged() || bp.isVendorPrivileged();
        boolean privappPermissionsDisable = RoSystemProperties.CONTROL_PRIVAPP_PERMISSIONS_DISABLE;
        boolean platformPermission = PackageManagerService.PLATFORM_PACKAGE_NAME.equals(bp.getSourcePackageName());
        boolean platformPackage = PackageManagerService.PLATFORM_PACKAGE_NAME.equals(pkg.packageName);
        if (!privappPermissionsDisable && privilegedPermission && pkg.isPrivileged() && !platformPackage && platformPermission && !hasPrivappWhitelistEntry(perm, pkg)) {
            if (!this.mSystemReady && !pkg.isUpdatedSystemApp()) {
                if (pkg.isVendor()) {
                    deniedPermissions = SystemConfig.getInstance().getVendorPrivAppDenyPermissions(pkg.packageName);
                } else if (pkg.isProduct()) {
                    deniedPermissions = SystemConfig.getInstance().getProductPrivAppDenyPermissions(pkg.packageName);
                } else if (pkg.isProductServices()) {
                    deniedPermissions = SystemConfig.getInstance().getProductServicesPrivAppDenyPermissions(pkg.packageName);
                } else {
                    deniedPermissions = SystemConfig.getInstance().getPrivAppDenyPermissions(pkg.packageName);
                }
                if (!(deniedPermissions == null || !deniedPermissions.contains(perm))) {
                    return false;
                }
                Slog.w(TAG, "Privileged permission " + perm + " for package " + pkg.packageName + " - not in privapp-permissions whitelist");
                if (RoSystemProperties.CONTROL_PRIVAPP_PERMISSIONS_ENFORCE) {
                    if (this.mPrivappPermissionsViolations == null) {
                        this.mPrivappPermissionsViolations = new ArraySet<>();
                    }
                    this.mPrivappPermissionsViolations.add(pkg.packageName + ": " + perm);
                }
            }
            if (RoSystemProperties.CONTROL_PRIVAPP_PERMISSIONS_ENFORCE) {
                return false;
            }
        }
        PackageParser.Package systemPackage = this.mPackageManagerInt.getPackage(this.mPackageManagerInt.getKnownPackageName(0, 0));
        boolean allowed = pkg.mSigningDetails.hasAncestorOrSelf(bp.getSourcePackageSetting().getSigningDetails()) || bp.getSourcePackageSetting().getSigningDetails().checkCapability(pkg.mSigningDetails, 4) || pkg.mSigningDetails.hasAncestorOrSelf(systemPackage.mSigningDetails) || systemPackage.mSigningDetails.checkCapability(pkg.mSigningDetails, 4);
        if (!allowed) {
            if (privilegedPermission || oemPermission) {
                if (pkg.isSystem()) {
                    if (pkg.isUpdatedSystemApp()) {
                        PackageParser.Package disabledPkg = this.mPackageManagerInt.getDisabledSystemPackage(pkg.packageName);
                        PackageSetting disabledPs = disabledPkg != null ? (PackageSetting) disabledPkg.mExtras : null;
                        if (disabledPs == null || !disabledPs.getPermissionsState().hasInstallPermission(perm)) {
                            if (disabledPs != null && disabledPkg != null && isPackageRequestingPermission(disabledPkg, perm) && ((privilegedPermission && disabledPs.isPrivileged()) || (oemPermission && disabledPs.isOem() && canGrantOemPermission(disabledPs, perm)))) {
                                allowed = true;
                            }
                            if (pkg.parentPackage != null) {
                                PackageParser.Package disabledParentPkg = this.mPackageManagerInt.getDisabledSystemPackage(((PackageParser.Package) pkg.parentPackage).packageName);
                                PackageSetting disabledParentPs = disabledParentPkg != null ? (PackageSetting) disabledParentPkg.mExtras : null;
                                if (disabledParentPkg != null) {
                                    if (!privilegedPermission || !disabledParentPs.isPrivileged()) {
                                        if (oemPermission) {
                                            if (!disabledParentPs.isOem()) {
                                            }
                                        }
                                    }
                                    if (isPackageRequestingPermission(disabledParentPkg, perm) && canGrantOemPermission(disabledParentPs, perm)) {
                                        allowed = true;
                                    } else if (disabledParentPkg.childPackages != null) {
                                        Iterator it2 = disabledParentPkg.childPackages.iterator();
                                        while (true) {
                                            if (!it2.hasNext()) {
                                                break;
                                            }
                                            PackageParser.Package disabledChildPkg = (PackageParser.Package) it2.next();
                                            if (disabledChildPkg != null) {
                                                it = it2;
                                                disabledChildPs = (PackageSetting) disabledChildPkg.mExtras;
                                            } else {
                                                it = it2;
                                                disabledChildPs = null;
                                            }
                                            if (isPackageRequestingPermission(disabledChildPkg, perm) && canGrantOemPermission(disabledChildPs, perm)) {
                                                allowed = true;
                                                break;
                                            }
                                            it2 = it;
                                            disabledParentPkg = disabledParentPkg;
                                        }
                                    }
                                }
                            }
                        } else if ((privilegedPermission && disabledPs.isPrivileged()) || (oemPermission && disabledPs.isOem() && canGrantOemPermission(disabledPs, perm))) {
                            allowed = true;
                        }
                    } else {
                        allowed = (privilegedPermission && pkg.isPrivileged()) || (oemPermission && pkg.isOem() && canGrantOemPermission((PackageSetting) pkg.mExtras, perm));
                    }
                    if (allowed && privilegedPermission && !vendorPrivilegedPermission && pkg.isVendor()) {
                        Slog.w(TAG, "Permission " + perm + " cannot be granted to privileged vendor apk " + pkg.packageName + " because it isn't a 'vendorPrivileged' permission.");
                        allowed = false;
                    }
                }
            }
        }
        if (!allowed) {
            if (!allowed && bp.isPre23() && pkg.applicationInfo.targetSdkVersion < 23) {
                allowed = true;
            }
            if (!allowed && bp.isInstaller() && (pkg.packageName.equals(this.mPackageManagerInt.getKnownPackageName(2, 0)) || pkg.packageName.equals(this.mPackageManagerInt.getKnownPackageName(6, 0)))) {
                allowed = true;
            }
            if (!allowed && bp.isVerifier() && pkg.packageName.equals(this.mPackageManagerInt.getKnownPackageName(3, 0))) {
                allowed = true;
            }
            if (!allowed && bp.isPreInstalled() && pkg.isSystem()) {
                allowed = true;
            }
            if (!allowed && bp.isDevelopment()) {
                allowed = origPermissions.hasInstallPermission(perm);
            }
            if (!allowed && bp.isSetup() && pkg.packageName.equals(this.mPackageManagerInt.getKnownPackageName(1, 0))) {
                allowed = true;
            }
            if (!allowed && bp.isSystemTextClassifier() && pkg.packageName.equals(this.mPackageManagerInt.getKnownPackageName(5, 0))) {
                allowed = true;
            }
            if (!allowed && bp.isConfigurator() && pkg.packageName.equals(this.mPackageManagerInt.getKnownPackageName(9, 0))) {
                allowed = true;
            }
            if (!allowed && bp.isWellbeing() && pkg.packageName.equals(this.mPackageManagerInt.getKnownPackageName(7, 0))) {
                allowed = true;
            }
            if (!allowed && bp.isDocumenter() && pkg.packageName.equals(this.mPackageManagerInt.getKnownPackageName(8, 0))) {
                allowed = true;
            }
            if (!allowed && bp.isIncidentReportApprover() && pkg.packageName.equals(this.mPackageManagerInt.getKnownPackageName(10, 0))) {
                allowed = true;
            }
            if (!allowed && bp.isAppPredictor() && pkg.packageName.equals(this.mPackageManagerInt.getKnownPackageName(11, 0))) {
                allowed = true;
            }
        }
        return this.mPackageManagerInt.getHwCertPermission(allowed, pkg, perm);
    }

    private static boolean canGrantOemPermission(PackageSetting ps, String permission) {
        if (!ps.isOem()) {
            return false;
        }
        Boolean granted = (Boolean) SystemConfig.getInstance().getOemPermissions(ps.name).get(permission);
        if (granted == null) {
            throw new IllegalStateException("OEM permission" + permission + " requested by package " + ps.name + " must be explicitly declared granted or not");
        } else if (Boolean.TRUE == granted) {
            return true;
        } else {
            return false;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isPermissionsReviewRequired(PackageParser.Package pkg, int userId) {
        if (pkg == null || pkg.applicationInfo == null || pkg.mExtras == null || pkg.applicationInfo.targetSdkVersion >= 23) {
            return false;
        }
        return ((PackageSetting) pkg.mExtras).getPermissionsState().isPermissionReviewRequired(userId);
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
    /* access modifiers changed from: public */
    @GuardedBy({"mLock"})
    private void grantRuntimePermissionsGrantedToDisabledPackageLocked(PackageParser.Package pkg, int callingUid, PermissionManagerServiceInternal.PermissionCallback callback) {
        PackageParser.Package disabledPkg;
        int i;
        int i2;
        if (!(pkg.parentPackage == null || pkg.requestedPermissions == null || (disabledPkg = this.mPackageManagerInt.getDisabledSystemPackage(pkg.parentPackage.packageName)) == null || disabledPkg.mExtras == null)) {
            PackageSetting disabledPs = (PackageSetting) disabledPkg.mExtras;
            if (disabledPs.isPrivileged() && !disabledPs.hasChildPackages()) {
                int permCount = pkg.requestedPermissions.size();
                for (int i3 = 0; i3 < permCount; i3++) {
                    String permission = (String) pkg.requestedPermissions.get(i3);
                    BasePermission bp = this.mSettings.getPermissionLocked(permission);
                    if (bp != null && (bp.isRuntime() || bp.isDevelopment())) {
                        int[] userIds = this.mUserManagerInt.getUserIds();
                        int length = userIds.length;
                        int i4 = 0;
                        while (i4 < length) {
                            int userId = userIds[i4];
                            if (disabledPs.getPermissionsState().hasRuntimePermission(permission, userId)) {
                                i2 = i4;
                                i = length;
                                grantRuntimePermission(permission, pkg.packageName, false, callingUid, userId, callback);
                            } else {
                                i2 = i4;
                                i = length;
                            }
                            i4 = i2 + 1;
                            length = i;
                        }
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void grantRequestedRuntimePermissions(PackageParser.Package pkg, int[] userIds, String[] grantedPermissions, int callingUid, PermissionManagerServiceInternal.PermissionCallback callback) {
        for (int userId : userIds) {
            grantRequestedRuntimePermissionsForUser(pkg, userId, grantedPermissions, callingUid, callback);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private List<String> getWhitelistedRestrictedPermissions(PackageParser.Package pkg, int whitelistFlags, int userId) {
        PackageSetting packageSetting = (PackageSetting) pkg.mExtras;
        if (packageSetting == null) {
            return null;
        }
        PermissionsState permissionsState = packageSetting.getPermissionsState();
        int queryFlags = 0;
        if ((whitelistFlags & 1) != 0) {
            queryFlags = 0 | 4096;
        }
        if ((whitelistFlags & 4) != 0) {
            queryFlags |= 8192;
        }
        if ((whitelistFlags & 2) != 0) {
            queryFlags |= 2048;
        }
        ArrayList<String> whitelistedPermissions = null;
        int permissionCount = pkg.requestedPermissions.size();
        for (int i = 0; i < permissionCount; i++) {
            String permissionName = (String) pkg.requestedPermissions.get(i);
            if ((permissionsState.getPermissionFlags(permissionName, userId) & queryFlags) != 0) {
                if (whitelistedPermissions == null) {
                    whitelistedPermissions = new ArrayList<>();
                }
                whitelistedPermissions.add(permissionName);
            }
        }
        return whitelistedPermissions;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setWhitelistedRestrictedPermissions(PackageParser.Package pkg, int[] userIds, List<String> permissions, int callingUid, int whitelistFlags, PermissionManagerServiceInternal.PermissionCallback callback) {
        for (int userId : userIds) {
            setWhitelistedRestrictedPermissionsForUser(pkg, userId, permissions, callingUid, whitelistFlags, callback);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:44:0x00ae, code lost:
        r0 = th;
     */
    private void grantRequestedRuntimePermissionsForUser(PackageParser.Package pkg, int userId, String[] grantedPermissions, int callingUid, PermissionManagerServiceInternal.PermissionCallback callback) {
        BasePermission bp;
        PackageSetting ps = (PackageSetting) pkg.mExtras;
        if (ps != null) {
            PermissionsState permissionsState = ps.getPermissionsState();
            boolean supportsRuntimePermissions = pkg.applicationInfo.targetSdkVersion >= 23;
            boolean instantApp = this.mPackageManagerInt.isInstantApp(pkg.packageName, userId);
            Iterator it = pkg.requestedPermissions.iterator();
            while (it.hasNext()) {
                String permission = (String) it.next();
                synchronized (this.mLock) {
                    bp = this.mSettings.getPermissionLocked(permission);
                }
                if (bp != null) {
                    if ((bp.isRuntime() || bp.isDevelopment()) && ((!instantApp || bp.isInstant()) && ((supportsRuntimePermissions || !bp.isRuntimeOnly()) && (grantedPermissions == null || ArrayUtils.contains(grantedPermissions, permission))))) {
                        int flags = permissionsState.getPermissionFlags(permission, userId);
                        if (supportsRuntimePermissions) {
                            if ((flags & 20) == 0) {
                                grantRuntimePermission(permission, pkg.packageName, false, callingUid, userId, callback);
                            }
                        } else if ((flags & 64) != 0) {
                            updatePermissionFlags(permission, pkg.packageName, 64, 0, callingUid, userId, false, callback);
                        }
                    }
                }
            }
            return;
        }
        return;
        while (true) {
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    /* JADX WARNING: Code restructure failed: missing block: B:96:0x021a, code lost:
        r0 = th;
     */
    private void grantRuntimePermission(String permName, String packageName, boolean overridePolicy, int callingUid, int userId, PermissionManagerServiceInternal.PermissionCallback callback) {
        BasePermission bp;
        if (!this.mUserManagerInt.exists(userId)) {
            Log.e(TAG, "No such user:" + userId);
            return;
        }
        this.mContext.enforceCallingOrSelfPermission("android.permission.GRANT_RUNTIME_PERMISSIONS", "grantRuntimePermission");
        enforceCrossUserPermission(callingUid, userId, true, true, false, "grantRuntimePermission");
        PackageParser.Package pkg = this.mPackageManagerInt.getPackage(packageName);
        if (pkg != null) {
            if (pkg.mExtras != null) {
                synchronized (this.mLock) {
                    bp = this.mSettings.getPermissionLocked(permName);
                }
                if (bp == null) {
                    throw new IllegalArgumentException("Unknown permission: " + permName);
                } else if (!this.mPackageManagerInt.filterAppAccess(pkg, callingUid, userId)) {
                    bp.enforceDeclaredUsedAndRuntimeOrDevelopment(pkg);
                    if (pkg.applicationInfo.targetSdkVersion >= 23 || !bp.isRuntime()) {
                        int uid = UserHandle.getUid(userId, pkg.applicationInfo.uid);
                        PackageSetting ps = (PackageSetting) pkg.mExtras;
                        PermissionsState permissionsState = ps.getPermissionsState();
                        int flags = permissionsState.getPermissionFlags(permName, userId);
                        if ((flags & 16) != 0) {
                            Log.e(TAG, "Cannot grant system fixed permission " + permName + " for package " + packageName);
                            return;
                        } else if (!overridePolicy && (flags & 4) != 0) {
                            Log.e(TAG, "Cannot grant policy fixed permission " + permName + " for package " + packageName);
                            return;
                        } else if (bp.isHardRestricted() && (flags & 14336) == 0) {
                            Log.e(TAG, "Cannot grant hard restricted non-exempt permission " + permName + " for package " + packageName);
                            return;
                        } else if (bp.isSoftRestricted() && !SoftRestrictedPermissionPolicy.forPermission(this.mContext, pkg.applicationInfo, UserHandle.of(userId), permName).canBeGranted()) {
                            Log.e(TAG, "Cannot grant soft restricted permission " + permName + " for package " + packageName);
                            return;
                        } else if (bp.isDevelopment()) {
                            if (permissionsState.grantInstallPermission(bp) != -1 && callback != null) {
                                callback.onInstallPermissionGranted();
                                return;
                            }
                            return;
                        } else if (ps.getInstantApp(userId) && !bp.isInstant()) {
                            throw new SecurityException("Cannot grant non-ephemeral permission" + permName + " for package " + packageName);
                        } else if (pkg.applicationInfo.targetSdkVersion < 23) {
                            Slog.w(TAG, "Cannot grant runtime permission to a legacy app");
                            return;
                        } else {
                            int result = permissionsState.grantRuntimePermission(bp, userId);
                            if (result != -1) {
                                if (result == 1 && callback != null) {
                                    callback.onGidsChanged(UserHandle.getAppId(pkg.applicationInfo.uid), userId);
                                }
                                if (bp.isRuntime()) {
                                    logPermission(1243, permName, packageName);
                                }
                                if (callback != null) {
                                    callback.onPermissionGranted(uid, userId);
                                }
                                if (bp.isRuntime()) {
                                    notifyRuntimePermissionStateChanged(packageName, userId);
                                }
                                if ("android.permission.READ_EXTERNAL_STORAGE".equals(permName) || "android.permission.WRITE_EXTERNAL_STORAGE".equals(permName)) {
                                    long token = Binder.clearCallingIdentity();
                                    try {
                                        if (this.mUserManagerInt.isUserInitialized(userId)) {
                                            ((StorageManagerInternal) LocalServices.getService(StorageManagerInternal.class)).onExternalStoragePolicyChanged(uid, packageName);
                                        }
                                        return;
                                    } finally {
                                        Binder.restoreCallingIdentity(token);
                                    }
                                } else {
                                    return;
                                }
                            } else {
                                return;
                            }
                        }
                    } else {
                        return;
                    }
                } else {
                    throw new IllegalArgumentException("Unknown package: " + packageName);
                }
            }
        }
        Log.e(TAG, "Unknown package: " + packageName);
        return;
        while (true) {
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void revokeRuntimePermission(String permName, String packageName, boolean overridePolicy, int userId, PermissionManagerServiceInternal.PermissionCallback callback) {
        if (!this.mUserManagerInt.exists(userId)) {
            Log.e(TAG, "No such user:" + userId);
            return;
        }
        this.mContext.enforceCallingOrSelfPermission("android.permission.REVOKE_RUNTIME_PERMISSIONS", "revokeRuntimePermission");
        enforceCrossUserPermission(Binder.getCallingUid(), userId, true, true, false, "revokeRuntimePermission");
        PackageParser.Package pkg = this.mPackageManagerInt.getPackage(packageName);
        if (pkg == null || pkg.mExtras == null) {
            throw new IllegalArgumentException("Unknown package: " + packageName);
        } else if (!this.mPackageManagerInt.filterAppAccess(pkg, Binder.getCallingUid(), userId)) {
            BasePermission bp = this.mSettings.getPermissionLocked(permName);
            if (bp != null) {
                bp.enforceDeclaredUsedAndRuntimeOrDevelopment(pkg);
                if (pkg.applicationInfo.targetSdkVersion >= 23 || !bp.isRuntime()) {
                    PermissionsState permissionsState = ((PackageSetting) pkg.mExtras).getPermissionsState();
                    int flags = permissionsState.getPermissionFlags(permName, userId);
                    if ((flags & 16) != 0 && UserHandle.getCallingAppId() != 1000) {
                        throw new SecurityException("Non-System UID cannot revoke system fixed permission " + permName + " for package " + packageName);
                    } else if (!overridePolicy && (flags & 4) != 0) {
                        throw new SecurityException("Cannot revoke policy fixed permission " + permName + " for package " + packageName);
                    } else if (bp.isDevelopment()) {
                        if (permissionsState.revokeInstallPermission(bp) != -1 && callback != null) {
                            callback.onInstallPermissionRevoked();
                        }
                    } else if (permissionsState.hasRuntimePermission(permName, userId) && permissionsState.revokeRuntimePermission(bp, userId) != -1) {
                        if (bp.isRuntime()) {
                            logPermission(1245, permName, packageName);
                        }
                        if (callback != null) {
                            callback.onPermissionRevoked(pkg.applicationInfo.uid, userId);
                        }
                        if (bp.isRuntime()) {
                            notifyRuntimePermissionStateChanged(packageName, userId);
                        }
                    }
                }
            } else {
                throw new IllegalArgumentException("Unknown permission: " + permName);
            }
        } else {
            throw new IllegalArgumentException("Unknown package: " + packageName);
        }
    }

    private void setWhitelistedRestrictedPermissionsForUser(PackageParser.Package pkg, int userId, List<String> permissions, int callingUid, int whitelistFlags, PermissionManagerServiceInternal.PermissionCallback callback) {
        int i;
        int permissionCount;
        ArraySet<String> oldGrantedRestrictedPermissions;
        int newFlags;
        int mask;
        PackageSetting ps = (PackageSetting) pkg.mExtras;
        if (ps != null) {
            PermissionsState permissionsState = ps.getPermissionsState();
            ArraySet<String> oldGrantedRestrictedPermissions2 = null;
            boolean updatePermissions = false;
            int permissionCount2 = pkg.requestedPermissions.size();
            int i2 = 0;
            while (i2 < permissionCount2) {
                String permissionName = (String) pkg.requestedPermissions.get(i2);
                BasePermission bp = this.mSettings.getPermissionLocked(permissionName);
                if (bp == null) {
                    Slog.w(TAG, "Cannot whitelist unknown permission: " + permissionName);
                } else if (bp.isHardOrSoftRestricted()) {
                    if (permissionsState.hasPermission(permissionName, userId)) {
                        if (oldGrantedRestrictedPermissions2 == null) {
                            oldGrantedRestrictedPermissions2 = new ArraySet<>();
                        }
                        oldGrantedRestrictedPermissions2.add(permissionName);
                        oldGrantedRestrictedPermissions = oldGrantedRestrictedPermissions2;
                    } else {
                        oldGrantedRestrictedPermissions = oldGrantedRestrictedPermissions2;
                    }
                    int oldFlags = permissionsState.getPermissionFlags(permissionName, userId);
                    int newFlags2 = oldFlags;
                    int mask2 = 0;
                    int whitelistFlagsCopy = whitelistFlags;
                    while (whitelistFlagsCopy != 0) {
                        int flag = 1 << Integer.numberOfTrailingZeros(whitelistFlagsCopy);
                        whitelistFlagsCopy &= ~flag;
                        if (flag == 1) {
                            mask2 |= 4096;
                            if (permissions == null || !permissions.contains(permissionName)) {
                                newFlags2 &= -4097;
                            } else {
                                newFlags2 |= 4096;
                            }
                        } else if (flag == 2) {
                            mask2 |= 2048;
                            if (permissions == null || !permissions.contains(permissionName)) {
                                newFlags2 &= -2049;
                            } else {
                                newFlags2 |= 2048;
                            }
                        } else if (flag == 4) {
                            mask2 |= 8192;
                            if (permissions == null || !permissions.contains(permissionName)) {
                                newFlags2 &= -8193;
                            } else {
                                newFlags2 |= 8192;
                            }
                        }
                    }
                    if (oldFlags == newFlags2) {
                        i = i2;
                        permissionCount = permissionCount2;
                        oldGrantedRestrictedPermissions2 = oldGrantedRestrictedPermissions;
                    } else {
                        boolean wasWhitelisted = (oldFlags & 14336) != 0;
                        boolean isWhitelisted = (newFlags2 & 14336) != 0;
                        if ((oldFlags & 4) != 0) {
                            boolean isGranted = permissionsState.hasPermission(permissionName, userId);
                            if (!isWhitelisted && isGranted) {
                                mask2 |= 4;
                                newFlags2 &= -5;
                            }
                        }
                        if (pkg.applicationInfo.targetSdkVersion >= 23 || wasWhitelisted || !isWhitelisted) {
                            newFlags = newFlags2;
                            mask = mask2;
                        } else {
                            newFlags = newFlags2 | 64;
                            mask = mask2 | 64;
                        }
                        i = i2;
                        permissionCount = permissionCount2;
                        updatePermissionFlags(permissionName, pkg.packageName, mask, newFlags, callingUid, userId, false, null);
                        oldGrantedRestrictedPermissions2 = oldGrantedRestrictedPermissions;
                        updatePermissions = true;
                    }
                    i2 = i + 1;
                    permissionCount2 = permissionCount;
                }
                i = i2;
                permissionCount = permissionCount2;
                i2 = i + 1;
                permissionCount2 = permissionCount;
            }
            if (updatePermissions) {
                restorePermissionState(pkg, false, pkg.packageName, callback);
                if (oldGrantedRestrictedPermissions2 != null) {
                    int oldGrantedCount = oldGrantedRestrictedPermissions2.size();
                    for (int i3 = 0; i3 < oldGrantedCount; i3++) {
                        if (!ps.getPermissionsState().hasPermission(oldGrantedRestrictedPermissions2.valueAt(i3), userId)) {
                            callback.onPermissionRevoked(pkg.applicationInfo.uid, userId);
                            return;
                        }
                    }
                }
            }
        }
    }

    /* JADX INFO: Multiple debug info for r7v2 int[]: [D('i' int), D('runtimePermissionChangedUserIds' int[])] */
    @GuardedBy({"mLock"})
    private int[] revokeUnusedSharedUserPermissionsLocked(SharedUserSetting suSetting, int[] allUserIds) {
        char c;
        boolean z;
        char c2;
        boolean z2;
        BasePermission bp;
        PermissionManagerService permissionManagerService = this;
        ArraySet<String> usedPermissions = new ArraySet<>();
        List<PackageParser.Package> pkgList = suSetting.getPackages();
        if (pkgList == null || pkgList.size() == 0) {
            return EmptyArray.INT;
        }
        for (PackageParser.Package pkg : pkgList) {
            if (pkg.requestedPermissions != null) {
                int requestedPermCount = pkg.requestedPermissions.size();
                for (int j = 0; j < requestedPermCount; j++) {
                    String permission = (String) pkg.requestedPermissions.get(j);
                    if (permissionManagerService.mSettings.getPermissionLocked(permission) != null) {
                        usedPermissions.add(permission);
                    }
                }
            }
        }
        PermissionsState permissionsState = suSetting.getPermissionsState();
        List<PermissionsState.PermissionState> installPermStates = permissionsState.getInstallPermissionStates();
        int i = installPermStates.size() - 1;
        while (true) {
            c = 64511;
            z = false;
            if (i < 0) {
                break;
            }
            PermissionsState.PermissionState permissionState = installPermStates.get(i);
            if (!usedPermissions.contains(permissionState.getName()) && (bp = permissionManagerService.mSettings.getPermissionLocked(permissionState.getName())) != null) {
                permissionsState.revokeInstallPermission(bp);
                permissionsState.updatePermissionFlags(bp, -1, 130047, 0);
            }
            i--;
        }
        int[] runtimePermissionChangedUserIds = EmptyArray.INT;
        int length = allUserIds.length;
        int[] runtimePermissionChangedUserIds2 = runtimePermissionChangedUserIds;
        int i2 = 0;
        while (i2 < length) {
            int userId = allUserIds[i2];
            List<PermissionsState.PermissionState> runtimePermStates = permissionsState.getRuntimePermissionStates(userId);
            int i3 = runtimePermStates.size() - 1;
            while (i3 >= 0) {
                PermissionsState.PermissionState permissionState2 = runtimePermStates.get(i3);
                if (!usedPermissions.contains(permissionState2.getName())) {
                    BasePermission bp2 = permissionManagerService.mSettings.getPermissionLocked(permissionState2.getName());
                    if (bp2 != null) {
                        permissionsState.revokeRuntimePermission(bp2, userId);
                        z2 = false;
                        c2 = 64511;
                        permissionsState.updatePermissionFlags(bp2, userId, 130047, 0);
                        runtimePermissionChangedUserIds2 = ArrayUtils.appendInt(runtimePermissionChangedUserIds2, userId);
                    } else {
                        z2 = false;
                        c2 = 64511;
                    }
                } else {
                    z2 = z;
                    c2 = 64511;
                }
                i3--;
                c = c2;
                z = z2;
                permissionManagerService = this;
            }
            i2++;
            z = z;
            permissionManagerService = this;
        }
        return runtimePermissionChangedUserIds2;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String[] getAppOpPermissionPackages(String permName) {
        if (this.mPackageManagerInt.getInstantAppPackageName(Binder.getCallingUid()) != null) {
            return null;
        }
        synchronized (this.mLock) {
            ArraySet<String> pkgs = this.mSettings.mAppOpPermissionPackages.get(permName);
            if (pkgs == null) {
                return null;
            }
            return (String[]) pkgs.toArray(new String[pkgs.size()]);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int getPermissionFlags(String permName, String packageName, int callingUid, int userId) {
        if (!this.mUserManagerInt.exists(userId)) {
            return 0;
        }
        enforceGrantRevokeGetRuntimePermissionPermissions("getPermissionFlags");
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
        if (this.mPackageManagerInt.filterAppAccess(pkg, callingUid, userId)) {
            return 0;
        }
        return ((PackageSetting) pkg.mExtras).getPermissionsState().getPermissionFlags(permName, userId);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updatePermissions(String packageName, PackageParser.Package pkg, boolean replaceGrant, Collection<PackageParser.Package> allPackages, PermissionManagerServiceInternal.PermissionCallback callback) {
        int i = 0;
        int i2 = pkg != null ? 1 : 0;
        if (replaceGrant) {
            i = 2;
        }
        int flags = i | i2;
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
    /* access modifiers changed from: public */
    private void updateAllPermissions(String volumeUuid, boolean sdkUpdated, Collection<PackageParser.Package> allPackages, PermissionManagerServiceInternal.PermissionCallback callback) {
        int i;
        if (sdkUpdated) {
            i = 6;
        } else {
            i = 0;
        }
        updatePermissions(null, null, volumeUuid, i | 1, allPackages, callback);
    }

    private void updatePermissions(String changingPkgName, PackageParser.Package changingPkg, String replaceVolumeUuid, int flags, Collection<PackageParser.Package> allPackages, PermissionManagerServiceInternal.PermissionCallback callback) {
        int flags2 = updatePermissions(changingPkgName, changingPkg, updatePermissionTrees(changingPkgName, changingPkg, flags), callback);
        synchronized (this.mLock) {
            if (this.mBackgroundPermissions == null) {
                this.mBackgroundPermissions = new ArrayMap<>();
                for (BasePermission bp : this.mSettings.getAllPermissionsLocked()) {
                    if (!(bp.perm == null || bp.perm.info == null || bp.perm.info.backgroundPermission == null)) {
                        String fgPerm = bp.name;
                        String bgPerm = bp.perm.info.backgroundPermission;
                        List<String> fgPerms = this.mBackgroundPermissions.get(bgPerm);
                        if (fgPerms == null) {
                            fgPerms = new ArrayList();
                            this.mBackgroundPermissions.put(bgPerm, fgPerms);
                        }
                        fgPerms.add(fgPerm);
                    }
                }
            }
        }
        if (this.mPackageManager == null) {
            this.mPackageManager = (PackageManagerService) ServiceManager.getService("package");
        }
        Trace.traceBegin(262144, "restorePermissionState");
        boolean replace = false;
        if ((flags2 & 1) != 0) {
            for (PackageParser.Package pkg : allPackages) {
                if (pkg != changingPkg) {
                    restorePermissionState(pkg, (flags2 & 4) != 0 && Objects.equals(replaceVolumeUuid, getVolumeUuidForPackage(pkg)), changingPkgName, callback);
                }
            }
        }
        if (changingPkg != null) {
            String volumeUuid = getVolumeUuidForPackage(changingPkg);
            if ((flags2 & 2) != 0 && Objects.equals(replaceVolumeUuid, volumeUuid)) {
                replace = true;
            }
            restorePermissionState(changingPkg, replace, changingPkgName, callback);
        }
        Trace.traceEnd(262144);
    }

    private int updatePermissions(String packageName, PackageParser.Package pkg, int flags, PermissionManagerServiceInternal.PermissionCallback callback) {
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
                        if (bp.isRuntime()) {
                            for (int userId : this.mUserManagerInt.getUserIds()) {
                                this.mPackageManagerInt.forEachPackage(new Consumer(bp, userId, callback) {
                                    /* class com.android.server.pm.permission.$$Lambda$PermissionManagerService$w2aPgVKY5ZkiKKZQUVsj6t4Bn4c */
                                    private final /* synthetic */ BasePermission f$1;
                                    private final /* synthetic */ int f$2;
                                    private final /* synthetic */ PermissionManagerServiceInternal.PermissionCallback f$3;

                                    {
                                        this.f$1 = r2;
                                        this.f$2 = r3;
                                        this.f$3 = r4;
                                    }

                                    @Override // java.util.function.Consumer
                                    public final void accept(Object obj) {
                                        PermissionManagerService.this.lambda$updatePermissions$1$PermissionManagerService(this.f$1, this.f$2, this.f$3, (PackageParser.Package) obj);
                                    }
                                });
                            }
                        }
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
                        if (sourcePkg.mExtras != null) {
                            PackageSetting sourcePs = (PackageSetting) sourcePkg.mExtras;
                            if (bp2.getSourcePackageSetting() == null) {
                                bp2.setSourcePackageSetting(sourcePs);
                            }
                        }
                    }
                    Slog.w(TAG, "Removing dangling permission: " + bp2.getName() + " from package " + bp2.getSourcePackageName());
                    this.mSettings.removePermissionLocked(bp2.getName());
                }
            }
        }
        return flags;
    }

    public /* synthetic */ void lambda$updatePermissions$1$PermissionManagerService(BasePermission bp, int userId, PermissionManagerServiceInternal.PermissionCallback callback, PackageParser.Package p) {
        String pName = p.packageName;
        ApplicationInfo appInfo = this.mPackageManagerInt.getApplicationInfo(pName, 0, 1000, 0);
        if (appInfo == null || appInfo.targetSdkVersion >= 23) {
            String permissionName = bp.getName();
            if (checkPermission(permissionName, pName, 1000, userId) == 0) {
                try {
                    revokeRuntimePermission(permissionName, pName, false, userId, callback);
                } catch (IllegalArgumentException e) {
                    Slog.e(TAG, "Failed to revoke " + permissionName + " from " + pName, e);
                }
            }
        }
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
                        if (sourcePkg.mExtras != null) {
                            PackageSetting sourcePs = (PackageSetting) sourcePkg.mExtras;
                            if (bp2.getSourcePackageSetting() == null) {
                                bp2.setSourcePackageSetting(sourcePs);
                            }
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
    /* access modifiers changed from: public */
    private void updatePermissionFlags(String permName, String packageName, int flagMask, int flagValues, int callingUid, int userId, boolean overridePolicy, PermissionManagerServiceInternal.PermissionCallback callback) {
        int flagValues2;
        int flagValues3;
        BasePermission bp;
        if (this.mUserManagerInt.exists(userId)) {
            enforceGrantRevokeRuntimePermissionPermissions("updatePermissionFlags");
            enforceCrossUserPermission(callingUid, userId, true, true, false, "updatePermissionFlags");
            if ((flagMask & 4) == 0 || overridePolicy) {
                if (callingUid != 1000) {
                    flagValues2 = flagValues & -17 & -33 & -65 & -4097 & -2049 & -8193 & -16385;
                    flagValues3 = flagMask & -17 & -33;
                } else {
                    flagValues3 = flagMask;
                    flagValues2 = flagValues;
                }
                PackageParser.Package pkg = this.mPackageManagerInt.getPackage(packageName);
                if (pkg == null || pkg.mExtras == null) {
                    Log.e(TAG, "Unknown package: " + packageName);
                } else if (!this.mPackageManagerInt.filterAppAccess(pkg, callingUid, userId)) {
                    synchronized (this.mLock) {
                        bp = this.mSettings.getPermissionLocked(permName);
                    }
                    if (bp != null) {
                        PermissionsState permissionsState = ((PackageSetting) pkg.mExtras).getPermissionsState();
                        boolean hadState = permissionsState.getRuntimePermissionState(permName, userId) != null;
                        boolean permissionUpdated = permissionsState.updatePermissionFlags(bp, userId, flagValues3, flagValues2);
                        if (permissionUpdated && bp.isRuntime()) {
                            notifyRuntimePermissionStateChanged(packageName, userId);
                        }
                        if (permissionUpdated && callback != null) {
                            if (permissionsState.getInstallPermissionState(permName) != null) {
                                callback.onInstallPermissionUpdated();
                            } else if (permissionsState.getRuntimePermissionState(permName, userId) != null || hadState) {
                                if ((flagValues3 & 65536) != 0) {
                                    callback.onOneTimePermissionFlagUpdated(UserHandle.getUid(userId, pkg.applicationInfo.uid));
                                }
                                callback.onPermissionUpdated(new int[]{userId}, false);
                            }
                        }
                    } else {
                        throw new IllegalArgumentException("Unknown permission: " + permName);
                    }
                } else {
                    throw new IllegalArgumentException("Unknown package: " + packageName);
                }
            } else {
                throw new SecurityException("updatePermissionFlags requires android.permission.ADJUST_RUNTIME_PERMISSIONS_POLICY");
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean updatePermissionFlagsForAllApps(int flagMask, int flagValues, int callingUid, int userId, Collection<PackageParser.Package> packages, PermissionManagerServiceInternal.PermissionCallback callback) {
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
    /* access modifiers changed from: public */
    private void enforceGrantRevokeRuntimePermissionPermissions(String message) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.GRANT_RUNTIME_PERMISSIONS") != 0 && this.mContext.checkCallingOrSelfPermission("android.permission.REVOKE_RUNTIME_PERMISSIONS") != 0) {
            throw new SecurityException(message + " requires android.permission.GRANT_RUNTIME_PERMISSIONS or android.permission.REVOKE_RUNTIME_PERMISSIONS");
        }
    }

    private void enforceGrantRevokeGetRuntimePermissionPermissions(String message) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.GET_RUNTIME_PERMISSIONS") != 0 && this.mContext.checkCallingOrSelfPermission("android.permission.GRANT_RUNTIME_PERMISSIONS") != 0 && this.mContext.checkCallingOrSelfPermission("android.permission.REVOKE_RUNTIME_PERMISSIONS") != 0) {
            throw new SecurityException(message + " requires android.permission.GRANT_RUNTIME_PERMISSIONS or android.permission.REVOKE_RUNTIME_PERMISSIONS or android.permission.GET_RUNTIME_PERMISSIONS");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void enforceCrossUserPermission(int callingUid, int userId, boolean requireFullPermission, boolean checkShell, boolean requirePermissionWhenSameUser, String message) {
        if (userId >= 0) {
            if (checkShell) {
                PackageManagerServiceUtils.enforceShellRestriction("no_debugging_features", callingUid, userId);
            }
            if ((requirePermissionWhenSameUser || userId != UserHandle.getUserId(callingUid)) && callingUid != 1000 && callingUid != 0) {
                if (requireFullPermission) {
                    this.mContext.enforceCallingOrSelfPermission("android.permission.INTERACT_ACROSS_USERS_FULL", message);
                    return;
                }
                try {
                    this.mContext.enforceCallingOrSelfPermission("android.permission.INTERACT_ACROSS_USERS_FULL", message);
                } catch (SecurityException e) {
                    this.mContext.enforceCallingOrSelfPermission("android.permission.INTERACT_ACROSS_USERS", message);
                }
            }
        } else {
            throw new IllegalArgumentException("Invalid userId " + userId);
        }
    }

    @GuardedBy({"mSettings.mLock", "mLock"})
    private int calculateCurrentPermissionFootprintLocked(BasePermission tree) {
        int size = 0;
        for (BasePermission perm : this.mSettings.mPermissions.values()) {
            size += tree.calculateFootprint(perm);
        }
        return size;
    }

    @GuardedBy({"mSettings.mLock", "mLock"})
    private void enforcePermissionCapLocked(PermissionInfo info, BasePermission tree) {
        if (tree.getUid() != 1000) {
            if (info.calculateFootprint() + calculateCurrentPermissionFootprintLocked(tree) > 32768) {
                throw new SecurityException("Permission tree size cap exceeded");
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void systemReady() {
        this.mSystemReady = true;
        if (this.mPrivappPermissionsViolations != null) {
            Slog.i(TAG, "Signature|privileged permissions not in privapp-permissions whitelist:" + this.mPrivappPermissionsViolations);
        }
        this.mPermissionControllerManager = (PermissionControllerManager) this.mContext.getSystemService(PermissionControllerManager.class);
        this.mPermissionPolicyInternal = (PermissionPolicyInternal) LocalServices.getService(PermissionPolicyInternal.class);
        try {
            ActivityManager.getService().registerUidObserver(new UidObserver(), 2, -1, (String) null);
        } catch (RemoteException e) {
            Slog.e(TAG, "fail register observer.", e);
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

    public ArrayMap<String, List<String>> getBackgroundPermissions() {
        return this.mBackgroundPermissions;
    }

    private class PermissionManagerServiceInternalImpl extends PermissionManagerServiceInternal {
        private PermissionManagerServiceInternalImpl() {
        }

        @Override // com.android.server.pm.permission.PermissionManagerServiceInternal
        public void systemReady() {
            PermissionManagerService.this.systemReady();
        }

        @Override // com.android.server.pm.permission.PermissionManagerServiceInternal
        public boolean isPermissionsReviewRequired(PackageParser.Package pkg, int userId) {
            return PermissionManagerService.this.isPermissionsReviewRequired(pkg, userId);
        }

        @Override // com.android.server.pm.permission.PermissionManagerServiceInternal
        public void revokeRuntimePermissionsIfGroupChanged(PackageParser.Package newPackage, PackageParser.Package oldPackage, ArrayList<String> allPackageNames, PermissionManagerServiceInternal.PermissionCallback permissionCallback) {
            PermissionManagerService.this.revokeRuntimePermissionsIfGroupChanged(newPackage, oldPackage, allPackageNames, permissionCallback);
        }

        @Override // com.android.server.pm.permission.PermissionManagerServiceInternal
        public void addAllPermissions(PackageParser.Package pkg, boolean chatty) {
            PermissionManagerService.this.addAllPermissions(pkg, chatty);
        }

        @Override // com.android.server.pm.permission.PermissionManagerServiceInternal
        public void addAllPermissionGroups(PackageParser.Package pkg, boolean chatty) {
            PermissionManagerService.this.addAllPermissionGroups(pkg, chatty);
        }

        @Override // com.android.server.pm.permission.PermissionManagerServiceInternal
        public void removeAllPermissions(PackageParser.Package pkg, boolean chatty) {
            PermissionManagerService.this.removeAllPermissions(pkg, chatty);
        }

        @Override // com.android.server.pm.permission.PermissionManagerServiceInternal
        public boolean addDynamicPermission(PermissionInfo info, boolean async, int callingUid, PermissionManagerServiceInternal.PermissionCallback callback) {
            return PermissionManagerService.this.addDynamicPermission(info, callingUid, callback);
        }

        @Override // com.android.server.pm.permission.PermissionManagerServiceInternal
        public void removeDynamicPermission(String permName, int callingUid, PermissionManagerServiceInternal.PermissionCallback callback) {
            PermissionManagerService.this.removeDynamicPermission(permName, callingUid, callback);
        }

        @Override // com.android.server.pm.permission.PermissionManagerServiceInternal
        public void grantRuntimePermission(String permName, String packageName, boolean overridePolicy, int callingUid, int userId, PermissionManagerServiceInternal.PermissionCallback callback) {
            PermissionManagerService.this.grantRuntimePermission(permName, packageName, overridePolicy, callingUid, userId, callback);
        }

        @Override // com.android.server.pm.permission.PermissionManagerServiceInternal
        public void grantRequestedRuntimePermissions(PackageParser.Package pkg, int[] userIds, String[] grantedPermissions, int callingUid, PermissionManagerServiceInternal.PermissionCallback callback) {
            PermissionManagerService.this.grantRequestedRuntimePermissions(pkg, userIds, grantedPermissions, callingUid, callback);
        }

        @Override // com.android.server.pm.permission.PermissionManagerServiceInternal
        public List<String> getWhitelistedRestrictedPermissions(PackageParser.Package pkg, int whitelistFlags, int userId) {
            return PermissionManagerService.this.getWhitelistedRestrictedPermissions(pkg, whitelistFlags, userId);
        }

        @Override // com.android.server.pm.permission.PermissionManagerServiceInternal
        public void setWhitelistedRestrictedPermissions(PackageParser.Package pkg, int[] userIds, List<String> permissions, int callingUid, int whitelistFlags, PermissionManagerServiceInternal.PermissionCallback callback) {
            PermissionManagerService.this.setWhitelistedRestrictedPermissions(pkg, userIds, permissions, callingUid, whitelistFlags, callback);
        }

        @Override // com.android.server.pm.permission.PermissionManagerServiceInternal
        public void grantRuntimePermissionsGrantedToDisabledPackage(PackageParser.Package pkg, int callingUid, PermissionManagerServiceInternal.PermissionCallback callback) {
            PermissionManagerService.this.grantRuntimePermissionsGrantedToDisabledPackageLocked(pkg, callingUid, callback);
        }

        @Override // com.android.server.pm.permission.PermissionManagerServiceInternal
        public void revokeRuntimePermission(String permName, String packageName, boolean overridePolicy, int userId, PermissionManagerServiceInternal.PermissionCallback callback) {
            PermissionManagerService.this.revokeRuntimePermission(permName, packageName, overridePolicy, userId, callback);
        }

        @Override // com.android.server.pm.permission.PermissionManagerServiceInternal
        public void updatePermissions(String packageName, PackageParser.Package pkg, boolean replaceGrant, Collection<PackageParser.Package> allPackages, PermissionManagerServiceInternal.PermissionCallback callback) {
            PermissionManagerService.this.updatePermissions(packageName, pkg, replaceGrant, allPackages, callback);
        }

        @Override // com.android.server.pm.permission.PermissionManagerServiceInternal
        public void updateAllPermissions(String volumeUuid, boolean sdkUpdated, Collection<PackageParser.Package> allPackages, PermissionManagerServiceInternal.PermissionCallback callback) {
            PermissionManagerService.this.updateAllPermissions(volumeUuid, sdkUpdated, allPackages, callback);
        }

        @Override // com.android.server.pm.permission.PermissionManagerServiceInternal
        public String[] getAppOpPermissionPackages(String permName) {
            return PermissionManagerService.this.getAppOpPermissionPackages(permName);
        }

        @Override // com.android.server.pm.permission.PermissionManagerServiceInternal
        public int getPermissionFlags(String permName, String packageName, int callingUid, int userId) {
            return PermissionManagerService.this.getPermissionFlags(permName, packageName, callingUid, userId);
        }

        @Override // com.android.server.pm.permission.PermissionManagerServiceInternal
        public void updatePermissionFlags(String permName, String packageName, int flagMask, int flagValues, int callingUid, int userId, boolean overridePolicy, PermissionManagerServiceInternal.PermissionCallback callback) {
            PermissionManagerService.this.updatePermissionFlags(permName, packageName, flagMask, flagValues, callingUid, userId, overridePolicy, callback);
        }

        @Override // com.android.server.pm.permission.PermissionManagerServiceInternal
        public boolean updatePermissionFlagsForAllApps(int flagMask, int flagValues, int callingUid, int userId, Collection<PackageParser.Package> packages, PermissionManagerServiceInternal.PermissionCallback callback) {
            return PermissionManagerService.this.updatePermissionFlagsForAllApps(flagMask, flagValues, callingUid, userId, packages, callback);
        }

        @Override // com.android.server.pm.permission.PermissionManagerServiceInternal
        public void enforceCrossUserPermission(int callingUid, int userId, boolean requireFullPermission, boolean checkShell, String message) {
            PermissionManagerService.this.enforceCrossUserPermission(callingUid, userId, requireFullPermission, checkShell, false, message);
        }

        @Override // com.android.server.pm.permission.PermissionManagerServiceInternal
        public void enforceCrossUserPermission(int callingUid, int userId, boolean requireFullPermission, boolean checkShell, boolean requirePermissionWhenSameUser, String message) {
            PermissionManagerService.this.enforceCrossUserPermission(callingUid, userId, requireFullPermission, checkShell, requirePermissionWhenSameUser, message);
        }

        @Override // com.android.server.pm.permission.PermissionManagerServiceInternal
        public void enforceGrantRevokeRuntimePermissionPermissions(String message) {
            PermissionManagerService.this.enforceGrantRevokeRuntimePermissionPermissions(message);
        }

        @Override // com.android.server.pm.permission.PermissionManagerServiceInternal
        public int checkPermission(String permName, String packageName, int callingUid, int userId) {
            return PermissionManagerService.this.checkPermission(permName, packageName, callingUid, userId);
        }

        @Override // com.android.server.pm.permission.PermissionManagerServiceInternal
        public int checkUidPermission(String permName, PackageParser.Package pkg, int uid, int callingUid) {
            return PermissionManagerService.this.checkUidPermission(permName, pkg, uid, callingUid);
        }

        @Override // com.android.server.pm.permission.PermissionManagerServiceInternal
        public PermissionGroupInfo getPermissionGroupInfo(String groupName, int flags, int callingUid) {
            return PermissionManagerService.this.getPermissionGroupInfo(groupName, flags, callingUid);
        }

        @Override // com.android.server.pm.permission.PermissionManagerServiceInternal
        public List<PermissionGroupInfo> getAllPermissionGroups(int flags, int callingUid) {
            return PermissionManagerService.this.getAllPermissionGroups(flags, callingUid);
        }

        @Override // com.android.server.pm.permission.PermissionManagerServiceInternal
        public PermissionInfo getPermissionInfo(String permName, String packageName, int flags, int callingUid) {
            return PermissionManagerService.this.getPermissionInfo(permName, packageName, flags, callingUid);
        }

        @Override // com.android.server.pm.permission.PermissionManagerServiceInternal
        public List<PermissionInfo> getPermissionInfoByGroup(String group, int flags, int callingUid) {
            return PermissionManagerService.this.getPermissionInfoByGroup(group, flags, callingUid);
        }

        @Override // com.android.server.pm.permission.PermissionManagerServiceInternal
        public PermissionSettings getPermissionSettings() {
            return PermissionManagerService.this.mSettings;
        }

        @Override // com.android.server.pm.permission.PermissionManagerServiceInternal
        public DefaultPermissionGrantPolicy getDefaultPermissionGrantPolicy() {
            return PermissionManagerService.this.mDefaultPermissionGrantPolicy;
        }

        @Override // com.android.server.pm.permission.PermissionManagerServiceInternal
        public BasePermission getPermissionTEMP(String permName) {
            BasePermission permissionLocked;
            synchronized (PermissionManagerService.this.mLock) {
                permissionLocked = PermissionManagerService.this.mSettings.getPermissionLocked(permName);
            }
            return permissionLocked;
        }

        @Override // com.android.server.pm.permission.PermissionManagerServiceInternal
        public ArrayList<PermissionInfo> getAllPermissionWithProtectionLevel(int protectionLevel) {
            ArrayList<PermissionInfo> matchingPermissions = new ArrayList<>();
            synchronized (PermissionManagerService.this.mLock) {
                int numTotalPermissions = PermissionManagerService.this.mSettings.mPermissions.size();
                for (int i = 0; i < numTotalPermissions; i++) {
                    BasePermission bp = PermissionManagerService.this.mSettings.mPermissions.valueAt(i);
                    if (!(bp.perm == null || bp.perm.info == null || bp.protectionLevel != protectionLevel)) {
                        matchingPermissions.add(bp.perm.info);
                    }
                }
            }
            return matchingPermissions;
        }

        public byte[] backupRuntimePermissions(UserHandle user) {
            return PermissionManagerService.this.backupRuntimePermissions(user);
        }

        public void restoreRuntimePermissions(byte[] backup, UserHandle user) {
            PermissionManagerService.this.restoreRuntimePermissions(backup, user);
        }

        public void restoreDelayedRuntimePermissions(String packageName, UserHandle user) {
            PermissionManagerService.this.restoreDelayedRuntimePermissions(packageName, user);
        }

        public void addOnRuntimePermissionStateChangedListener(PermissionManagerInternal.OnRuntimePermissionStateChangedListener listener) {
            PermissionManagerService.this.addOnRuntimePermissionStateChangedListener(listener);
        }

        public void removeOnRuntimePermissionStateChangedListener(PermissionManagerInternal.OnRuntimePermissionStateChangedListener listener) {
            PermissionManagerService.this.removeOnRuntimePermissionStateChangedListener(listener);
        }
    }

    /* access modifiers changed from: private */
    public class UidObserver extends IUidObserver.Stub {
        private UidObserver() {
        }

        public void onUidStateChanged(int uid, int procState, long procStateSeq) {
        }

        public void onUidGone(int uid, boolean isDisabled) {
            PermissionManagerService.this.handleUidGone(uid);
        }

        public void onUidActive(int uid) {
        }

        public void onUidIdle(int uid, boolean isDisabled) {
        }

        public void onUidCachedChanged(int uid, boolean isCached) {
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleUidGone(int uid) {
        String[] pkgNames = this.mPackageManager.getPackagesForUid(uid);
        if (pkgNames == null) {
            Slog.i(TAG, "handleUidGone pkgNames of uid:" + uid + " is null");
            return;
        }
        int userId = UserHandle.getUserId(uid);
        for (String pkgName : pkgNames) {
            PackageSetting ps = this.mPackageManager.getPackageSettingByPackageName(pkgName);
            if (ps != null) {
                PermissionsState permissionsState = ps.getPermissionsState();
                for (String permission : permissionsState.getPermissions(userId)) {
                    if ((65536 & permissionsState.getPermissionFlags(permission, userId)) != 0) {
                        try {
                            this.mPackageManager.revokeRuntimePermission(pkgName, permission, userId);
                        } catch (IllegalArgumentException e) {
                            Slog.w(TAG, "revokeRuntimePermission occurred exception. pkgName:" + pkgName + ", userId:" + userId);
                        }
                    }
                }
            }
        }
    }
}
