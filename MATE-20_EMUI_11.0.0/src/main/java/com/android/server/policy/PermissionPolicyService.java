package com.android.server.policy;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManagerInternal;
import android.content.pm.PackageParser;
import android.content.pm.PermissionInfo;
import android.os.Build;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.os.UserManagerInternal;
import android.permission.PermissionControllerManager;
import android.permission.PermissionManagerInternal;
import android.util.ArraySet;
import android.util.LongSparseLongArray;
import android.util.Pair;
import android.util.Slog;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.app.IAppOpsCallback;
import com.android.internal.app.IAppOpsService;
import com.android.internal.util.IntPair;
import com.android.internal.util.function.pooled.PooledLambda;
import com.android.server.FgThread;
import com.android.server.LocalServices;
import com.android.server.SystemService;
import com.android.server.pm.DumpState;
import com.android.server.pm.permission.PermissionManagerServiceInternal;
import com.android.server.policy.PermissionPolicyInternal;
import com.android.server.policy.PermissionPolicyService;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public final class PermissionPolicyService extends SystemService {
    private static final boolean DEBUG = false;
    private static final String LOG_TAG = PermissionPolicyService.class.getSimpleName();
    @GuardedBy({"mLock"})
    private final ArraySet<Pair<String, Integer>> mIsPackageSyncsScheduled = new ArraySet<>();
    @GuardedBy({"mLock"})
    private final SparseBooleanArray mIsStarted = new SparseBooleanArray();
    private final Object mLock = new Object();
    @GuardedBy({"mLock"})
    private PermissionPolicyInternal.OnInitializedCallback mOnInitializedCallback;

    public PermissionPolicyService(Context context) {
        super(context);
        LocalServices.addService(PermissionPolicyInternal.class, new Internal());
    }

    @Override // com.android.server.SystemService
    public void onStart() {
        PermissionManagerServiceInternal permManagerInternal = (PermissionManagerServiceInternal) LocalServices.getService(PermissionManagerServiceInternal.class);
        IAppOpsService appOpsService = IAppOpsService.Stub.asInterface(ServiceManager.getService("appops"));
        ((PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class)).getPackageList(new PackageManagerInternal.PackageListObserver() {
            /* class com.android.server.policy.PermissionPolicyService.AnonymousClass1 */

            public void onPackageAdded(String packageName, int uid) {
                onPackageChanged(packageName, uid);
            }

            public void onPackageChanged(String packageName, int uid) {
                int userId = UserHandle.getUserId(uid);
                if (PermissionPolicyService.this.isStarted(userId)) {
                    PermissionPolicyService.this.synchronizePackagePermissionsAndAppOpsForUser(packageName, userId);
                }
            }

            public void onPackageRemoved(String packageName, int uid) {
            }
        });
        permManagerInternal.addOnRuntimePermissionStateChangedListener(new PermissionManagerInternal.OnRuntimePermissionStateChangedListener() {
            /* class com.android.server.policy.$$Lambda$PermissionPolicyService$V2gOjn4rTBH_rbxagOzeOTvNfc */

            public final void onRuntimePermissionStateChanged(String str, int i) {
                PermissionPolicyService.this.synchronizePackagePermissionsAndAppOpsAsyncForUser(str, i);
            }
        });
        IAppOpsCallback appOpsListener = new IAppOpsCallback.Stub() {
            /* class com.android.server.policy.PermissionPolicyService.AnonymousClass2 */

            public void opChanged(int op, int uid, String packageName) {
                PermissionPolicyService.this.synchronizePackagePermissionsAndAppOpsAsyncForUser(packageName, UserHandle.getUserId(uid));
            }
        };
        ArrayList<PermissionInfo> dangerousPerms = permManagerInternal.getAllPermissionWithProtectionLevel(1);
        try {
            int numDangerousPerms = dangerousPerms.size();
            for (int i = 0; i < numDangerousPerms; i++) {
                PermissionInfo perm = dangerousPerms.get(i);
                if (!perm.isHardRestricted()) {
                    if (perm.backgroundPermission == null) {
                        if (perm.isSoftRestricted()) {
                            appOpsService.startWatchingMode(getSwitchOp(perm.name), (String) null, appOpsListener);
                            SoftRestrictedPermissionPolicy policy = SoftRestrictedPermissionPolicy.forPermission(null, null, null, perm.name);
                            if (policy.resolveAppOp() != -1) {
                                appOpsService.startWatchingMode(policy.resolveAppOp(), (String) null, appOpsListener);
                            }
                        }
                    }
                }
                appOpsService.startWatchingMode(getSwitchOp(perm.name), (String) null, appOpsListener);
            }
        } catch (RemoteException e) {
            Slog.wtf(LOG_TAG, "Cannot set up app-ops listener");
        }
    }

    /* access modifiers changed from: private */
    public static int getSwitchOp(String permission) {
        int op = AppOpsManager.permissionToOpCode(permission);
        if (op == -1) {
            return -1;
        }
        return AppOpsManager.opToSwitch(op);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    public void synchronizePackagePermissionsAndAppOpsAsyncForUser(String packageName, int changedUserId) {
        if (isStarted(changedUserId)) {
            synchronized (this.mLock) {
                if (this.mIsPackageSyncsScheduled.add(new Pair<>(packageName, Integer.valueOf(changedUserId)))) {
                    FgThread.getHandler().sendMessage(PooledLambda.obtainMessage($$Lambda$PermissionPolicyService$RYery4oeHNcS8uZ6BgM2MtZIvKw.INSTANCE, this, packageName, Integer.valueOf(changedUserId)));
                }
            }
        }
    }

    @Override // com.android.server.SystemService
    public void onBootPhase(int phase) {
        if (phase == 550) {
            UserManagerInternal um = (UserManagerInternal) LocalServices.getService(UserManagerInternal.class);
            int[] userIds = um.getUserIds();
            for (int userId : userIds) {
                if (um.isUserRunning(userId)) {
                    onStartUser(userId);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isStarted(int userId) {
        boolean z;
        synchronized (this.mLock) {
            z = this.mIsStarted.get(userId);
        }
        return z;
    }

    @Override // com.android.server.SystemService
    public void onStartUser(int userId) {
        PermissionPolicyInternal.OnInitializedCallback callback;
        if (!isStarted(userId)) {
            grantOrUpgradeDefaultRuntimePermissionsIfNeeded(userId);
            synchronized (this.mLock) {
                this.mIsStarted.put(userId, true);
                callback = this.mOnInitializedCallback;
            }
            synchronizePermissionsAndAppOpsForUser(userId);
            if (callback != null) {
                callback.onInitialized(userId);
            }
        }
    }

    @Override // com.android.server.SystemService
    public void onStopUser(int userId) {
        synchronized (this.mLock) {
            this.mIsStarted.delete(userId);
        }
    }

    private void grantOrUpgradeDefaultRuntimePermissionsIfNeeded(int userId) {
        String str = LOG_TAG;
        Slog.i(str, "grantOrUpgradeDefaultPermsIfNeeded(" + userId + ")");
        PackageManagerInternal packageManagerInternal = (PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class);
        if (packageManagerInternal.wereDefaultPermissionsGrantedSinceBoot(userId)) {
            String str2 = LOG_TAG;
            Slog.i(str2, "defaultPermsWereGrantedSinceBoot(" + userId + ")");
            CompletableFuture<Boolean> future = new CompletableFuture<>();
            new PermissionControllerManager(getUserContext(getContext(), UserHandle.of(userId)), FgThread.getHandler()).grantOrUpgradeDefaultRuntimePermissions(FgThread.getExecutor(), new Consumer(future, userId) {
                /* class com.android.server.policy.$$Lambda$PermissionPolicyService$lJark9JJoQWBDcJrqZvkVU5Buo */
                private final /* synthetic */ CompletableFuture f$0;
                private final /* synthetic */ int f$1;

                {
                    this.f$0 = r1;
                    this.f$1 = r2;
                }

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    PermissionPolicyService.lambda$grantOrUpgradeDefaultRuntimePermissionsIfNeeded$0(this.f$0, this.f$1, (Boolean) obj);
                }
            });
            try {
                future.get();
                packageManagerInternal.setRuntimePermissionsFingerPrint(Build.FINGERPRINT, userId);
            } catch (InterruptedException | ExecutionException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    static /* synthetic */ void lambda$grantOrUpgradeDefaultRuntimePermissionsIfNeeded$0(CompletableFuture future, int userId, Boolean successful) {
        if (successful.booleanValue()) {
            future.complete(null);
            return;
        }
        String message = "Error granting/upgrading runtime permissions for user " + userId;
        Slog.wtf(LOG_TAG, message);
        future.completeExceptionally(new IllegalStateException(message));
    }

    private static Context getUserContext(Context context, UserHandle user) {
        if (context.getUser().equals(user)) {
            return context;
        }
        try {
            return context.createPackageContextAsUser(context.getPackageName(), 0, user);
        } catch (PackageManager.NameNotFoundException e) {
            String str = LOG_TAG;
            Slog.e(str, "Cannot create context for user " + user, e);
            return null;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    public void synchronizePackagePermissionsAndAppOpsForUser(String packageName, int userId) {
        synchronized (this.mLock) {
            this.mIsPackageSyncsScheduled.remove(new Pair(packageName, Integer.valueOf(userId)));
        }
        PackageManagerInternal packageManagerInternal = (PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class);
        PackageInfo pkg = packageManagerInternal.getPackageInfo(packageName, 0, 1000, userId);
        if (pkg != null) {
            PermissionToOpSynchroniser synchroniser = new PermissionToOpSynchroniser(getUserContext(getContext(), UserHandle.of(userId)));
            synchroniser.addPackage(pkg.packageName);
            String[] sharedPkgNames = packageManagerInternal.getPackagesForSharedUserId(pkg.sharedUserId, userId);
            if (sharedPkgNames != null) {
                for (String sharedPkgName : sharedPkgNames) {
                    PackageParser.Package sharedPkg = packageManagerInternal.getPackage(sharedPkgName);
                    if (sharedPkg != null) {
                        synchroniser.addPackage(sharedPkg.packageName);
                    }
                }
            }
            synchroniser.syncPackages();
        }
    }

    private void synchronizePermissionsAndAppOpsForUser(int userId) {
        PermissionToOpSynchroniser synchronizer = new PermissionToOpSynchroniser(getUserContext(getContext(), UserHandle.of(userId)));
        ((PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class)).forEachPackage(new Consumer() {
            /* class com.android.server.policy.$$Lambda$PermissionPolicyService$K1QpWYLKz7rfj4y4fthPQy64Pek */

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                PermissionPolicyService.PermissionToOpSynchroniser.this.addPackage(((PackageParser.Package) obj).packageName);
            }
        });
        synchronizer.syncPackages();
    }

    /* access modifiers changed from: private */
    public static class PermissionToOpSynchroniser {
        private final SparseIntArray mAllUids = new SparseIntArray();
        private final AppOpsManager mAppOpsManager;
        private final Context mContext;
        private final ArrayList<OpToChange> mOpsToAllow = new ArrayList<>();
        private final ArrayList<OpToChange> mOpsToAllowIfDefault = new ArrayList<>();
        private final ArrayList<OpToChange> mOpsToDefault = new ArrayList<>();
        private final ArrayList<OpToChange> mOpsToForeground = new ArrayList<>();
        private final ArrayList<OpToChange> mOpsToForegroundIfAllow = new ArrayList<>();
        private final ArrayList<OpToChange> mOpsToIgnore = new ArrayList<>();
        private final ArrayList<OpToChange> mOpsToIgnoreIfDefault = new ArrayList<>();
        private final PackageManager mPackageManager;

        PermissionToOpSynchroniser(Context context) {
            this.mContext = context;
            this.mPackageManager = context.getPackageManager();
            this.mAppOpsManager = (AppOpsManager) context.getSystemService(AppOpsManager.class);
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void syncPackages() {
            LongSparseLongArray alreadySetAppOps = new LongSparseLongArray();
            int allowCount = this.mOpsToAllow.size();
            for (int i = 0; i < allowCount; i++) {
                OpToChange op = this.mOpsToAllow.get(i);
                setUidModeAllowed(op.code, op.uid, op.packageName);
                alreadySetAppOps.put(IntPair.of(op.uid, op.code), 1);
            }
            int allowIfDefaultCount = this.mOpsToAllowIfDefault.size();
            for (int i2 = 0; i2 < allowIfDefaultCount; i2++) {
                OpToChange op2 = this.mOpsToAllowIfDefault.get(i2);
                if (alreadySetAppOps.indexOfKey(IntPair.of(op2.uid, op2.code)) < 0 && setUidModeAllowedIfDefault(op2.code, op2.uid, op2.packageName)) {
                    alreadySetAppOps.put(IntPair.of(op2.uid, op2.code), 1);
                }
            }
            int foregroundIfAllowedCount = this.mOpsToForegroundIfAllow.size();
            for (int i3 = 0; i3 < foregroundIfAllowedCount; i3++) {
                OpToChange op3 = this.mOpsToForegroundIfAllow.get(i3);
                if (alreadySetAppOps.indexOfKey(IntPair.of(op3.uid, op3.code)) < 0 && setUidModeForegroundIfAllow(op3.code, op3.uid, op3.packageName)) {
                    alreadySetAppOps.put(IntPair.of(op3.uid, op3.code), 1);
                }
            }
            int foregroundCount = this.mOpsToForeground.size();
            for (int i4 = 0; i4 < foregroundCount; i4++) {
                OpToChange op4 = this.mOpsToForeground.get(i4);
                if (alreadySetAppOps.indexOfKey(IntPair.of(op4.uid, op4.code)) < 0) {
                    setUidModeForeground(op4.code, op4.uid, op4.packageName);
                    alreadySetAppOps.put(IntPair.of(op4.uid, op4.code), 1);
                }
            }
            int ignoreCount = this.mOpsToIgnore.size();
            for (int i5 = 0; i5 < ignoreCount; i5++) {
                OpToChange op5 = this.mOpsToIgnore.get(i5);
                if (alreadySetAppOps.indexOfKey(IntPair.of(op5.uid, op5.code)) < 0) {
                    setUidModeIgnored(op5.code, op5.uid, op5.packageName);
                    alreadySetAppOps.put(IntPair.of(op5.uid, op5.code), 1);
                }
            }
            int ignoreIfDefaultCount = this.mOpsToIgnoreIfDefault.size();
            for (int i6 = 0; i6 < ignoreIfDefaultCount; i6++) {
                OpToChange op6 = this.mOpsToIgnoreIfDefault.get(i6);
                if (alreadySetAppOps.indexOfKey(IntPair.of(op6.uid, op6.code)) < 0 && setUidModeIgnoredIfDefault(op6.code, op6.uid, op6.packageName)) {
                    alreadySetAppOps.put(IntPair.of(op6.uid, op6.code), 1);
                }
            }
            int defaultCount = this.mOpsToDefault.size();
            for (int i7 = 0; i7 < defaultCount; i7++) {
                OpToChange op7 = this.mOpsToDefault.get(i7);
                if (alreadySetAppOps.indexOfKey(IntPair.of(op7.uid, op7.code)) < 0) {
                    setUidModeDefault(op7.code, op7.uid, op7.packageName);
                    alreadySetAppOps.put(IntPair.of(op7.uid, op7.code), 1);
                }
            }
        }

        private void addOpIfRestricted(PermissionInfo permissionInfo, PackageInfo pkg) {
            String permission = permissionInfo.name;
            int opCode = PermissionPolicyService.getSwitchOp(permission);
            int uid = pkg.applicationInfo.uid;
            if (permissionInfo.isRestricted()) {
                boolean applyRestriction = (this.mPackageManager.getPermissionFlags(permission, pkg.packageName, this.mContext.getUser()) & DumpState.DUMP_KEYSETS) != 0;
                if (permissionInfo.isHardRestricted()) {
                    if (opCode == -1) {
                        return;
                    }
                    if (applyRestriction) {
                        this.mOpsToDefault.add(new OpToChange(uid, pkg.packageName, opCode));
                    } else {
                        this.mOpsToAllowIfDefault.add(new OpToChange(uid, pkg.packageName, opCode));
                    }
                } else if (permissionInfo.isSoftRestricted()) {
                    SoftRestrictedPermissionPolicy policy = SoftRestrictedPermissionPolicy.forPermission(this.mContext, pkg.applicationInfo, this.mContext.getUser(), permission);
                    if (opCode != -1) {
                        if (policy.canBeGranted()) {
                            this.mOpsToAllowIfDefault.add(new OpToChange(uid, pkg.packageName, opCode));
                        } else {
                            this.mOpsToDefault.add(new OpToChange(uid, pkg.packageName, opCode));
                        }
                    }
                    int op = policy.resolveAppOp();
                    if (op != -1) {
                        int desiredOpMode = policy.getDesiredOpMode();
                        if (desiredOpMode != 0) {
                            if (desiredOpMode != 1) {
                                if (desiredOpMode == 2) {
                                    Slog.wtf(PermissionPolicyService.LOG_TAG, "Setting appop to errored is not implemented");
                                } else if (desiredOpMode == 3) {
                                    this.mOpsToDefault.add(new OpToChange(uid, pkg.packageName, op));
                                } else if (desiredOpMode == 4) {
                                    Slog.wtf(PermissionPolicyService.LOG_TAG, "Setting appop to foreground is not implemented");
                                }
                            } else if (policy.shouldSetAppOpIfNotDefault()) {
                                this.mOpsToIgnore.add(new OpToChange(uid, pkg.packageName, op));
                            } else {
                                this.mOpsToIgnoreIfDefault.add(new OpToChange(uid, pkg.packageName, op));
                            }
                        } else if (policy.shouldSetAppOpIfNotDefault()) {
                            this.mOpsToAllow.add(new OpToChange(uid, pkg.packageName, op));
                        } else {
                            this.mOpsToAllowIfDefault.add(new OpToChange(uid, pkg.packageName, op));
                        }
                    }
                }
            }
        }

        private boolean isBgPermRestricted(String pkg, String perm, int uid) {
            try {
                PermissionInfo bgPermInfo = this.mPackageManager.getPermissionInfo(perm, 0);
                if (bgPermInfo.isSoftRestricted()) {
                    Slog.wtf(PermissionPolicyService.LOG_TAG, "Support for soft restricted background permissions not implemented");
                }
                if (!bgPermInfo.isHardRestricted() || (this.mPackageManager.getPermissionFlags(perm, pkg, UserHandle.getUserHandleForUid(uid)) & DumpState.DUMP_KEYSETS) == 0) {
                    return false;
                }
                return true;
            } catch (PackageManager.NameNotFoundException e) {
                String str = PermissionPolicyService.LOG_TAG;
                Slog.w(str, "Cannot read permission state of " + perm, e);
                return false;
            }
        }

        private void addOpIfFgPermissions(PermissionInfo permissionInfo, PackageInfo pkg) {
            String bgPermissionName = permissionInfo.backgroundPermission;
            if (bgPermissionName != null) {
                String permission = permissionInfo.name;
                int opCode = PermissionPolicyService.getSwitchOp(permission);
                String pkgName = pkg.packageName;
                int uid = pkg.applicationInfo.uid;
                if (pkg.applicationInfo.targetSdkVersion < 23) {
                    if ((this.mPackageManager.getPermissionFlags(bgPermissionName, pkg.packageName, UserHandle.getUserHandleForUid(uid)) & 64) == 0 && isBgPermRestricted(pkgName, bgPermissionName, uid)) {
                        this.mOpsToForegroundIfAllow.add(new OpToChange(uid, pkgName, opCode));
                    }
                } else if (this.mPackageManager.checkPermission(permission, pkgName) == 0) {
                    boolean isBgHardRestricted = isBgPermRestricted(pkgName, bgPermissionName, uid);
                    boolean isBgPermGranted = this.mPackageManager.checkPermission(bgPermissionName, pkgName) == 0;
                    if (isBgHardRestricted || !isBgPermGranted) {
                        this.mOpsToForeground.add(new OpToChange(uid, pkgName, opCode));
                    } else {
                        this.mOpsToAllow.add(new OpToChange(uid, pkgName, opCode));
                    }
                } else {
                    this.mOpsToIgnore.add(new OpToChange(uid, pkgName, opCode));
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void addPackage(String pkgName) {
            try {
                PackageInfo pkg = this.mPackageManager.getPackageInfo(pkgName, 4096);
                this.mAllUids.put(pkg.applicationInfo.uid, pkg.applicationInfo.uid);
                if (pkg.requestedPermissions != null) {
                    String[] strArr = pkg.requestedPermissions;
                    for (String permission : strArr) {
                        if (PermissionPolicyService.getSwitchOp(permission) != -1) {
                            try {
                                PermissionInfo permissionInfo = this.mPackageManager.getPermissionInfo(permission, 0);
                                addOpIfRestricted(permissionInfo, pkg);
                                addOpIfFgPermissions(permissionInfo, pkg);
                            } catch (PackageManager.NameNotFoundException e) {
                            }
                        }
                    }
                }
            } catch (PackageManager.NameNotFoundException e2) {
            }
        }

        private boolean setUidModeAllowedIfDefault(int opCode, int uid, String packageName) {
            return setUidModeIfMode(opCode, uid, 3, 0, packageName);
        }

        private void setUidModeAllowed(int opCode, int uid, String packageName) {
            setUidMode(opCode, uid, 0, packageName);
        }

        private boolean setUidModeForegroundIfAllow(int opCode, int uid, String packageName) {
            return setUidModeIfMode(opCode, uid, 0, 4, packageName);
        }

        private void setUidModeForeground(int opCode, int uid, String packageName) {
            setUidMode(opCode, uid, 4, packageName);
        }

        private boolean setUidModeIgnoredIfDefault(int opCode, int uid, String packageName) {
            return setUidModeIfMode(opCode, uid, 3, 1, packageName);
        }

        private void setUidModeIgnored(int opCode, int uid, String packageName) {
            setUidMode(opCode, uid, 1, packageName);
        }

        private void setUidMode(int opCode, int uid, int mode, String packageName) {
            if (this.mAppOpsManager.unsafeCheckOpRaw(AppOpsManager.opToPublicName(opCode), uid, packageName) != mode) {
                this.mAppOpsManager.setUidMode(opCode, uid, mode);
            }
        }

        private boolean setUidModeIfMode(int opCode, int uid, int requiredModeBefore, int newMode, String packageName) {
            if (this.mAppOpsManager.unsafeCheckOpRaw(AppOpsManager.opToPublicName(opCode), uid, packageName) != requiredModeBefore) {
                return false;
            }
            this.mAppOpsManager.setUidMode(opCode, uid, newMode);
            return true;
        }

        private void setUidModeDefault(int opCode, int uid, String packageName) {
            setUidMode(opCode, uid, 3, packageName);
        }

        /* access modifiers changed from: private */
        public class OpToChange {
            final int code;
            final String packageName;
            final int uid;

            OpToChange(int uid2, String packageName2, int code2) {
                this.uid = uid2;
                this.packageName = packageName2;
                this.code = code2;
            }
        }
    }

    private class Internal extends PermissionPolicyInternal {
        private Internal() {
        }

        @Override // com.android.server.policy.PermissionPolicyInternal
        public boolean checkStartActivity(Intent intent, int callingUid, String callingPackage) {
            if (callingPackage == null || !isActionRemovedForCallingPackage(intent, callingUid, callingPackage)) {
                return true;
            }
            String str = PermissionPolicyService.LOG_TAG;
            Slog.w(str, "Action Removed: starting " + intent.toString() + " from " + callingPackage + " (uid=" + callingUid + ")");
            return false;
        }

        @Override // com.android.server.policy.PermissionPolicyInternal
        public boolean isInitialized(int userId) {
            return PermissionPolicyService.this.isStarted(userId);
        }

        @Override // com.android.server.policy.PermissionPolicyInternal
        public void setOnInitializedCallback(PermissionPolicyInternal.OnInitializedCallback callback) {
            synchronized (PermissionPolicyService.this.mLock) {
                PermissionPolicyService.this.mOnInitializedCallback = callback;
            }
        }

        private boolean isActionRemovedForCallingPackage(Intent intent, int callingUid, String callingPackage) {
            String action = intent.getAction();
            if (action == null) {
                return false;
            }
            char c = 65535;
            int hashCode = action.hashCode();
            if (hashCode != -1673968409) {
                if (hashCode == 579418056 && action.equals("android.telecom.action.CHANGE_DEFAULT_DIALER")) {
                    c = 0;
                }
            } else if (action.equals("android.provider.Telephony.ACTION_CHANGE_DEFAULT")) {
                c = 1;
            }
            if (c != 0 && c != 1) {
                return false;
            }
            try {
                if (PermissionPolicyService.this.getContext().getPackageManager().getApplicationInfoAsUser(callingPackage, 0, UserHandle.getUserId(callingUid)).targetSdkVersion >= 29) {
                    return true;
                }
            } catch (PackageManager.NameNotFoundException e) {
                Slog.i(PermissionPolicyService.LOG_TAG, "Cannot find application info for " + callingPackage);
            }
            intent.putExtra("android.intent.extra.CALLING_PACKAGE", callingPackage);
            return false;
        }
    }
}
