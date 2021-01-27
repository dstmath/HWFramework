package com.android.server.role;

import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.app.role.IOnRoleHoldersChangedListener;
import android.app.role.IRoleManager;
import android.app.role.RoleControllerManager;
import android.app.role.RoleManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.PermissionChecker;
import android.content.pm.PackageManager;
import android.content.pm.PackageManagerInternal;
import android.content.pm.PackageParser;
import android.content.pm.Signature;
import android.database.CursorWindow;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteCallback;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.ShellCallback;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManagerInternal;
import android.telephony.IFinancialSmsCallback;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.PackageUtils;
import android.util.Slog;
import android.util.SparseArray;
import android.util.proto.ProtoOutputStream;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.telephony.SmsApplication;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.BitUtils;
import com.android.internal.util.CollectionUtils;
import com.android.internal.util.DumpUtils;
import com.android.internal.util.FunctionalUtils;
import com.android.internal.util.IndentingPrintWriter;
import com.android.internal.util.Preconditions;
import com.android.internal.util.dump.DualDumpOutputStream;
import com.android.internal.util.function.pooled.PooledLambda;
import com.android.server.FgThread;
import com.android.server.LocalServices;
import com.android.server.SystemService;
import com.android.server.display.color.DisplayTransformManager;
import com.android.server.pm.DumpState;
import com.android.server.role.RoleManagerService;
import com.android.server.role.RoleUserState;
import com.android.server.utils.PriorityDump;
import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

public class RoleManagerService extends SystemService implements RoleUserState.Callback {
    private static final boolean DEBUG = false;
    static final boolean IS_FPGA = boardname.contains("fpga");
    private static final String LOG_TAG = RoleManagerService.class.getSimpleName();
    static String boardname = SystemProperties.get("ro.board.boardname", "0");
    private final AppOpsManager mAppOpsManager;
    @GuardedBy({"mControllersLock"})
    private final SparseArray<RoleControllerManager> mControllers = new SparseArray<>();
    private final Object mControllersLock = new Object();
    private final RoleHoldersResolver mLegacyRoleResolver;
    private final Handler mListenerHandler = FgThread.getHandler();
    @GuardedBy({"mLock"})
    private final SparseArray<RemoteCallbackList<IOnRoleHoldersChangedListener>> mListeners = new SparseArray<>();
    private final Object mLock = new Object();
    private final UserManagerInternal mUserManagerInternal;
    @GuardedBy({"mLock"})
    private final SparseArray<RoleUserState> mUserStates = new SparseArray<>();
    private final TelephonyManager telephonyManager;

    public interface RoleHoldersResolver {
        List<String> getRoleHolders(String str, int i);
    }

    public RoleManagerService(Context context, RoleHoldersResolver legacyRoleResolver) {
        super(context);
        this.mLegacyRoleResolver = legacyRoleResolver;
        RoleControllerManager.initializeRemoteServiceComponentName(context);
        this.mUserManagerInternal = (UserManagerInternal) LocalServices.getService(UserManagerInternal.class);
        this.mAppOpsManager = (AppOpsManager) context.getSystemService(AppOpsManager.class);
        LocalServices.addService(RoleManagerInternal.class, new Internal());
        PackageManagerInternal packageManagerInternal = (PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class);
        packageManagerInternal.setDefaultBrowserProvider(new DefaultBrowserProvider());
        packageManagerInternal.setDefaultDialerProvider(new DefaultDialerProvider());
        packageManagerInternal.setDefaultHomeProvider(new DefaultHomeProvider());
        registerUserRemovedReceiver();
        this.telephonyManager = (TelephonyManager) context.getSystemService(TelephonyManager.class);
    }

    private void registerUserRemovedReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.USER_REMOVED");
        getContext().registerReceiverAsUser(new BroadcastReceiver() {
            /* class com.android.server.role.RoleManagerService.AnonymousClass1 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                if (TextUtils.equals(intent.getAction(), "android.intent.action.USER_REMOVED")) {
                    RoleManagerService.this.onRemoveUser(intent.getIntExtra("android.intent.extra.user_handle", 0));
                }
            }
        }, UserHandle.ALL, intentFilter, null, null);
    }

    /* JADX DEBUG: Multi-variable search result rejected for r7v0, resolved type: com.android.server.role.RoleManagerService */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r0v0, types: [com.android.server.role.RoleManagerService$Stub, android.os.IBinder] */
    /* JADX WARNING: Unknown variable types count: 1 */
    @Override // com.android.server.SystemService
    public void onStart() {
        publishBinderService("role", new Stub());
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.PACKAGE_CHANGED");
        intentFilter.addAction("android.intent.action.PACKAGE_ADDED");
        intentFilter.addAction("android.intent.action.PACKAGE_REMOVED");
        intentFilter.addDataScheme("package");
        intentFilter.setPriority(1000);
        getContext().registerReceiverAsUser(new BroadcastReceiver() {
            /* class com.android.server.role.RoleManagerService.AnonymousClass2 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                int userId = UserHandle.getUserId(intent.getIntExtra("android.intent.extra.UID", -1));
                if (!"android.intent.action.PACKAGE_REMOVED".equals(intent.getAction()) || !intent.getBooleanExtra("android.intent.extra.REPLACING", false)) {
                    RoleManagerService.this.performInitialGrantsIfNecessaryAsync(userId);
                }
            }
        }, UserHandle.ALL, intentFilter, null, null);
    }

    @Override // com.android.server.SystemService
    public void onStartUser(int userId) {
        performInitialGrantsIfNecessary(userId);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private CompletableFuture<Void> performInitialGrantsIfNecessaryAsync(int userId) {
        if (this.mUserManagerInternal.isClonedProfile(userId)) {
            String str = LOG_TAG;
            Slog.i(str, "Skip perform inital grants for clone user " + userId);
            CompletableFuture<Void> result = new CompletableFuture<>();
            result.complete(null);
            return result;
        }
        RoleUserState userState = getOrCreateUserState(userId);
        String packagesHash = computeComponentStateHash(userId);
        if (!(!Objects.equals(packagesHash, userState.getPackagesHash()))) {
            return CompletableFuture.completedFuture(null);
        }
        TelephonyManager telephonyManager2 = this.telephonyManager;
        if (telephonyManager2 != null && telephonyManager2.isSmsCapable()) {
            migrateRoleIfNecessary("android.app.role.SMS", userId);
        }
        migrateRoleIfNecessary("android.app.role.ASSISTANT", userId);
        migrateRoleIfNecessary("android.app.role.DIALER", userId);
        migrateRoleIfNecessary("android.app.role.EMERGENCY", userId);
        migrateRoleIfNecessary("android.app.role.HOME", userId);
        Slog.i(LOG_TAG, "Granting default permissions...");
        CompletableFuture<Void> result2 = new CompletableFuture<>();
        getOrCreateController(userId).grantDefaultRoles(FgThread.getExecutor(), new Consumer(packagesHash, result2) {
            /* class com.android.server.role.$$Lambda$RoleManagerService$bWORjt1dBr7EfFhavgUePqPY2LM */
            private final /* synthetic */ String f$1;
            private final /* synthetic */ CompletableFuture f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                RoleManagerService.lambda$performInitialGrantsIfNecessaryAsync$0(RoleUserState.this, this.f$1, this.f$2, (Boolean) obj);
            }
        });
        return result2;
    }

    static /* synthetic */ void lambda$performInitialGrantsIfNecessaryAsync$0(RoleUserState userState, String packagesHash, CompletableFuture result, Boolean successful) {
        if (successful.booleanValue()) {
            if (!userState.getDestroyedState()) {
                userState.setPackagesHash(packagesHash);
            } else {
                Slog.i(LOG_TAG, "RoleUserState is destroyed");
            }
            result.complete(null);
            return;
        }
        result.completeExceptionally(new RuntimeException());
    }

    private void performInitialGrantsIfNecessary(int userId) {
        try {
            performInitialGrantsIfNecessaryAsync(userId).get((long) (IS_FPGA ? DisplayTransformManager.LEVEL_COLOR_MATRIX_INVERT_COLOR : 30), TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            String str = LOG_TAG;
            Slog.e(str, "Failed to grant defaults for user " + userId, e);
        }
    }

    private void migrateRoleIfNecessary(String role, int userId) {
        RoleUserState userState = getOrCreateUserState(userId);
        if (!userState.isRoleAvailable(role)) {
            List<String> roleHolders = this.mLegacyRoleResolver.getRoleHolders(role, userId);
            if (!roleHolders.isEmpty()) {
                String str = LOG_TAG;
                Slog.i(str, "Migrating " + role + ", legacy holders: " + roleHolders);
                userState.addRoleName(role);
                int size = roleHolders.size();
                for (int i = 0; i < size; i++) {
                    userState.addRoleHolder(role, roleHolders.get(i));
                }
            }
        }
    }

    private static String computeComponentStateHash(int userId) {
        PackageManagerInternal pm = (PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        pm.forEachInstalledPackage(FunctionalUtils.uncheckExceptions(new FunctionalUtils.ThrowingConsumer(out, pm, userId) {
            /* class com.android.server.role.$$Lambda$RoleManagerService$rhPnAyxD4OKoR2nprS9wiayfZjw */
            private final /* synthetic */ ByteArrayOutputStream f$0;
            private final /* synthetic */ PackageManagerInternal f$1;
            private final /* synthetic */ int f$2;

            {
                this.f$0 = r1;
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void acceptOrThrow(Object obj) {
                RoleManagerService.lambda$computeComponentStateHash$1(this.f$0, this.f$1, this.f$2, (PackageParser.Package) obj);
            }
        }), userId);
        return PackageUtils.computeSha256Digest(out.toByteArray());
    }

    static /* synthetic */ void lambda$computeComponentStateHash$1(ByteArrayOutputStream out, PackageManagerInternal pm, int userId, PackageParser.Package pkg) throws Exception {
        out.write(pkg.packageName.getBytes());
        out.write(BitUtils.toBytes(pkg.getLongVersionCode()));
        out.write(pm.getApplicationEnabledState(pkg.packageName, userId));
        ArraySet<String> enabledComponents = pm.getEnabledComponents(pkg.packageName, userId);
        int numComponents = CollectionUtils.size(enabledComponents);
        out.write(numComponents);
        for (int i = 0; i < numComponents; i++) {
            out.write(enabledComponents.valueAt(i).getBytes());
        }
        ArraySet<String> disabledComponents = pm.getDisabledComponents(pkg.packageName, userId);
        int numComponents2 = CollectionUtils.size(disabledComponents);
        for (int i2 = 0; i2 < numComponents2; i2++) {
            out.write(disabledComponents.valueAt(i2).getBytes());
        }
        for (Signature signature : pkg.mSigningDetails.signatures) {
            out.write(signature.toByteArray());
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private RoleUserState getOrCreateUserState(int userId) {
        RoleUserState userState;
        synchronized (this.mLock) {
            userState = this.mUserStates.get(userId);
            if (userState == null) {
                userState = new RoleUserState(userId, this);
                this.mUserStates.put(userId, userState);
            }
        }
        return userState;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private RoleControllerManager getOrCreateController(int userId) {
        RoleControllerManager controller;
        synchronized (this.mControllersLock) {
            controller = this.mControllers.get(userId);
            if (controller == null) {
                Context systemContext = getContext();
                try {
                    controller = RoleControllerManager.createWithInitializedRemoteServiceComponentName(FgThread.getHandler(), systemContext.createPackageContextAsUser(systemContext.getPackageName(), 0, UserHandle.of(userId)));
                    this.mControllers.put(userId, controller);
                } catch (PackageManager.NameNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return controller;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private RemoteCallbackList<IOnRoleHoldersChangedListener> getListeners(int userId) {
        RemoteCallbackList<IOnRoleHoldersChangedListener> remoteCallbackList;
        synchronized (this.mLock) {
            remoteCallbackList = this.mListeners.get(userId);
        }
        return remoteCallbackList;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private RemoteCallbackList<IOnRoleHoldersChangedListener> getOrCreateListeners(int userId) {
        RemoteCallbackList<IOnRoleHoldersChangedListener> listeners;
        synchronized (this.mLock) {
            listeners = this.mListeners.get(userId);
            if (listeners == null) {
                listeners = new RemoteCallbackList<>();
                this.mListeners.put(userId, listeners);
            }
        }
        return listeners;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onRemoveUser(int userId) {
        RemoteCallbackList<IOnRoleHoldersChangedListener> listeners;
        RoleUserState userState;
        synchronized (this.mLock) {
            listeners = (RemoteCallbackList) this.mListeners.removeReturnOld(userId);
            userState = (RoleUserState) this.mUserStates.removeReturnOld(userId);
        }
        synchronized (this.mControllersLock) {
            this.mControllers.remove(userId);
        }
        if (listeners != null) {
            listeners.kill();
        }
        if (userState != null) {
            userState.destroy();
        }
    }

    @Override // com.android.server.role.RoleUserState.Callback
    public void onRoleHoldersChanged(String roleName, int userId, String removedHolder, String addedHolder) {
        this.mListenerHandler.sendMessage(PooledLambda.obtainMessage($$Lambda$RoleManagerService$TCTA4I2bhEypguZihxs4ezif6t0.INSTANCE, this, roleName, Integer.valueOf(userId), removedHolder, addedHolder));
    }

    /* access modifiers changed from: private */
    public void notifyRoleHoldersChanged(String roleName, int userId, String removedHolder, String addedHolder) {
        RemoteCallbackList<IOnRoleHoldersChangedListener> listeners = getListeners(userId);
        if (listeners != null) {
            notifyRoleHoldersChangedForListeners(listeners, roleName, userId);
        }
        RemoteCallbackList<IOnRoleHoldersChangedListener> allUsersListeners = getListeners(-1);
        if (allUsersListeners != null) {
            notifyRoleHoldersChangedForListeners(allUsersListeners, roleName, userId);
        }
        if ("android.app.role.SMS".equals(roleName)) {
            SmsApplication.broadcastSmsAppChange(getContext(), UserHandle.of(userId), removedHolder, addedHolder);
        }
    }

    private void notifyRoleHoldersChangedForListeners(RemoteCallbackList<IOnRoleHoldersChangedListener> listeners, String roleName, int userId) {
        int broadcastCount = listeners.beginBroadcast();
        for (int i = 0; i < broadcastCount; i++) {
            try {
                try {
                    listeners.getBroadcastItem(i).onRoleHoldersChanged(roleName, userId);
                } catch (RemoteException e) {
                    Slog.e(LOG_TAG, "Error calling OnRoleHoldersChangedListener", e);
                }
            } catch (Throwable th) {
                listeners.finishBroadcast();
                throw th;
            }
        }
        listeners.finishBroadcast();
    }

    /* access modifiers changed from: private */
    public class Stub extends IRoleManager.Stub {
        private Stub() {
        }

        public boolean isRoleAvailable(String roleName) {
            Preconditions.checkStringNotEmpty(roleName, "roleName cannot be null or empty");
            return RoleManagerService.this.getOrCreateUserState(UserHandle.getUserId(getCallingUid())).isRoleAvailable(roleName);
        }

        public boolean isRoleHeld(String roleName, String packageName) {
            int callingUid = getCallingUid();
            RoleManagerService.this.mAppOpsManager.checkPackage(callingUid, packageName);
            Preconditions.checkStringNotEmpty(roleName, "roleName cannot be null or empty");
            Preconditions.checkStringNotEmpty(packageName, "packageName cannot be null or empty");
            ArraySet<String> roleHolders = RoleManagerService.this.getOrCreateUserState(UserHandle.getUserId(callingUid)).getRoleHolders(roleName);
            if (roleHolders == null) {
                return false;
            }
            return roleHolders.contains(packageName);
        }

        public List<String> getRoleHoldersAsUser(String roleName, int userId) {
            if (!RoleManagerService.this.mUserManagerInternal.exists(userId)) {
                String str = RoleManagerService.LOG_TAG;
                Slog.e(str, "user " + userId + " does not exist");
                return Collections.emptyList();
            }
            int userId2 = handleIncomingUser(userId, false, "getRoleHoldersAsUser");
            RoleManagerService.this.getContext().enforceCallingOrSelfPermission("android.permission.MANAGE_ROLE_HOLDERS", "getRoleHoldersAsUser");
            Preconditions.checkStringNotEmpty(roleName, "roleName cannot be null or empty");
            ArraySet<String> roleHolders = RoleManagerService.this.getOrCreateUserState(userId2).getRoleHolders(roleName);
            if (roleHolders == null) {
                return Collections.emptyList();
            }
            return new ArrayList(roleHolders);
        }

        public void addRoleHolderAsUser(String roleName, String packageName, @RoleManager.ManageHoldersFlags int flags, int userId, RemoteCallback callback) {
            if (!RoleManagerService.this.mUserManagerInternal.exists(userId)) {
                String str = RoleManagerService.LOG_TAG;
                Slog.e(str, "user " + userId + " does not exist");
                return;
            }
            int userId2 = handleIncomingUser(userId, false, "addRoleHolderAsUser");
            RoleManagerService.this.getContext().enforceCallingOrSelfPermission("android.permission.MANAGE_ROLE_HOLDERS", "addRoleHolderAsUser");
            Preconditions.checkStringNotEmpty(roleName, "roleName cannot be null or empty");
            Preconditions.checkStringNotEmpty(packageName, "packageName cannot be null or empty");
            Preconditions.checkNotNull(callback, "callback cannot be null");
            RoleManagerService.this.getOrCreateController(userId2).onAddRoleHolder(roleName, packageName, flags, callback);
        }

        public void removeRoleHolderAsUser(String roleName, String packageName, @RoleManager.ManageHoldersFlags int flags, int userId, RemoteCallback callback) {
            if (!RoleManagerService.this.mUserManagerInternal.exists(userId)) {
                String str = RoleManagerService.LOG_TAG;
                Slog.e(str, "user " + userId + " does not exist");
                return;
            }
            int userId2 = handleIncomingUser(userId, false, "removeRoleHolderAsUser");
            RoleManagerService.this.getContext().enforceCallingOrSelfPermission("android.permission.MANAGE_ROLE_HOLDERS", "removeRoleHolderAsUser");
            Preconditions.checkStringNotEmpty(roleName, "roleName cannot be null or empty");
            Preconditions.checkStringNotEmpty(packageName, "packageName cannot be null or empty");
            Preconditions.checkNotNull(callback, "callback cannot be null");
            RoleManagerService.this.getOrCreateController(userId2).onRemoveRoleHolder(roleName, packageName, flags, callback);
        }

        public void clearRoleHoldersAsUser(String roleName, @RoleManager.ManageHoldersFlags int flags, int userId, RemoteCallback callback) {
            if (!RoleManagerService.this.mUserManagerInternal.exists(userId)) {
                String str = RoleManagerService.LOG_TAG;
                Slog.e(str, "user " + userId + " does not exist");
                return;
            }
            int userId2 = handleIncomingUser(userId, false, "clearRoleHoldersAsUser");
            RoleManagerService.this.getContext().enforceCallingOrSelfPermission("android.permission.MANAGE_ROLE_HOLDERS", "clearRoleHoldersAsUser");
            Preconditions.checkStringNotEmpty(roleName, "roleName cannot be null or empty");
            Preconditions.checkNotNull(callback, "callback cannot be null");
            RoleManagerService.this.getOrCreateController(userId2).onClearRoleHolders(roleName, flags, callback);
        }

        public void addOnRoleHoldersChangedListenerAsUser(IOnRoleHoldersChangedListener listener, int userId) {
            if (userId == -1 || RoleManagerService.this.mUserManagerInternal.exists(userId)) {
                int userId2 = handleIncomingUser(userId, true, "addOnRoleHoldersChangedListenerAsUser");
                RoleManagerService.this.getContext().enforceCallingOrSelfPermission("android.permission.OBSERVE_ROLE_HOLDERS", "addOnRoleHoldersChangedListenerAsUser");
                Preconditions.checkNotNull(listener, "listener cannot be null");
                RoleManagerService.this.getOrCreateListeners(userId2).register(listener);
                return;
            }
            String str = RoleManagerService.LOG_TAG;
            Slog.e(str, "user " + userId + " does not exist");
        }

        public void removeOnRoleHoldersChangedListenerAsUser(IOnRoleHoldersChangedListener listener, int userId) {
            if (userId == -1 || RoleManagerService.this.mUserManagerInternal.exists(userId)) {
                int userId2 = handleIncomingUser(userId, true, "removeOnRoleHoldersChangedListenerAsUser");
                RoleManagerService.this.getContext().enforceCallingOrSelfPermission("android.permission.OBSERVE_ROLE_HOLDERS", "removeOnRoleHoldersChangedListenerAsUser");
                Preconditions.checkNotNull(listener, "listener cannot be null");
                RemoteCallbackList<IOnRoleHoldersChangedListener> listeners = RoleManagerService.this.getListeners(userId2);
                if (listener != null) {
                    listeners.unregister(listener);
                    return;
                }
                return;
            }
            String str = RoleManagerService.LOG_TAG;
            Slog.e(str, "user " + userId + " does not exist");
        }

        public void setRoleNamesFromController(List<String> roleNames) {
            RoleManagerService.this.getContext().enforceCallingOrSelfPermission("com.android.permissioncontroller.permission.MANAGE_ROLES_FROM_CONTROLLER", "setRoleNamesFromController");
            Preconditions.checkNotNull(roleNames, "roleNames cannot be null");
            RoleManagerService.this.getOrCreateUserState(UserHandle.getCallingUserId()).setRoleNames(roleNames);
        }

        public boolean addRoleHolderFromController(String roleName, String packageName) {
            RoleManagerService.this.getContext().enforceCallingOrSelfPermission("com.android.permissioncontroller.permission.MANAGE_ROLES_FROM_CONTROLLER", "addRoleHolderFromController");
            Preconditions.checkStringNotEmpty(roleName, "roleName cannot be null or empty");
            Preconditions.checkStringNotEmpty(packageName, "packageName cannot be null or empty");
            return RoleManagerService.this.getOrCreateUserState(UserHandle.getCallingUserId()).addRoleHolder(roleName, packageName);
        }

        public boolean removeRoleHolderFromController(String roleName, String packageName) {
            RoleManagerService.this.getContext().enforceCallingOrSelfPermission("com.android.permissioncontroller.permission.MANAGE_ROLES_FROM_CONTROLLER", "removeRoleHolderFromController");
            Preconditions.checkStringNotEmpty(roleName, "roleName cannot be null or empty");
            Preconditions.checkStringNotEmpty(packageName, "packageName cannot be null or empty");
            return RoleManagerService.this.getOrCreateUserState(UserHandle.getCallingUserId()).removeRoleHolder(roleName, packageName);
        }

        public List<String> getHeldRolesFromController(String packageName) {
            RoleManagerService.this.getContext().enforceCallingOrSelfPermission("com.android.permissioncontroller.permission.MANAGE_ROLES_FROM_CONTROLLER", "getRolesHeldFromController");
            Preconditions.checkStringNotEmpty(packageName, "packageName cannot be null or empty");
            return RoleManagerService.this.getOrCreateUserState(UserHandle.getCallingUserId()).getHeldRoles(packageName);
        }

        private int handleIncomingUser(int userId, boolean allowAll, String name) {
            return ActivityManager.handleIncomingUser(getCallingPid(), getCallingUid(), userId, allowAll, true, name, null);
        }

        /* JADX DEBUG: Multi-variable search result rejected for r8v0, resolved type: com.android.server.role.RoleManagerService$Stub */
        /* JADX WARN: Multi-variable type inference failed */
        public void onShellCommand(FileDescriptor in, FileDescriptor out, FileDescriptor err, String[] args, ShellCallback callback, ResultReceiver resultReceiver) {
            new RoleManagerShellCommand(this).exec(this, in, out, err, args, callback, resultReceiver);
        }

        public String getDefaultSmsPackage(int userId) {
            long identity = Binder.clearCallingIdentity();
            try {
                return (String) CollectionUtils.firstOrNull(getRoleHoldersAsUser("android.app.role.SMS", userId));
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        /* access modifiers changed from: protected */
        public void dump(FileDescriptor fd, PrintWriter fout, String[] args) {
            DualDumpOutputStream dumpOutputStream;
            if (DumpUtils.checkDumpPermission(RoleManagerService.this.getContext(), RoleManagerService.LOG_TAG, fout)) {
                if (args != null && ArrayUtils.contains(args, PriorityDump.PROTO_ARG)) {
                    dumpOutputStream = new DualDumpOutputStream(new ProtoOutputStream(fd));
                } else {
                    fout.println("ROLE MANAGER STATE (dumpsys role):");
                    dumpOutputStream = new DualDumpOutputStream(new IndentingPrintWriter(fout, "  "));
                }
                for (int userId : RoleManagerService.this.mUserManagerInternal.getUserIds()) {
                    RoleManagerService.this.getOrCreateUserState(userId).dump(dumpOutputStream, "user_states", 2246267895809L);
                }
                dumpOutputStream.flush();
            }
        }

        public void getSmsMessagesForFinancialApp(String callingPkg, Bundle params, IFinancialSmsCallback callback) {
            if (PermissionChecker.checkCallingOrSelfPermissionForDataDelivery(RoleManagerService.this.getContext(), "android:sms_financial_transactions") == 0) {
                new FinancialSmsManager(RoleManagerService.this.getContext()).getSmsMessages(new RemoteCallback(new RemoteCallback.OnResultListener(callback) {
                    /* class com.android.server.role.$$Lambda$RoleManagerService$Stub$2DaS8GFEsxV7psuQ8OMLocv4QEY */
                    private final /* synthetic */ IFinancialSmsCallback f$0;

                    {
                        this.f$0 = r1;
                    }

                    public final void onResult(Bundle bundle) {
                        RoleManagerService.Stub.lambda$getSmsMessagesForFinancialApp$0(this.f$0, bundle);
                    }
                }), params);
                return;
            }
            try {
                callback.onGetSmsMessagesForFinancialApp((CursorWindow) null);
            } catch (RemoteException e) {
            }
        }

        static /* synthetic */ void lambda$getSmsMessagesForFinancialApp$0(IFinancialSmsCallback callback, Bundle result) {
            CursorWindow messages = null;
            if (result == null) {
                Slog.w(RoleManagerService.LOG_TAG, "result is null.");
            } else {
                messages = (CursorWindow) result.getParcelable("sms_messages");
            }
            try {
                callback.onGetSmsMessagesForFinancialApp(messages);
            } catch (RemoteException e) {
            }
        }

        private int getUidForPackage(String packageName) {
            long ident = Binder.clearCallingIdentity();
            try {
                return RoleManagerService.this.getContext().getPackageManager().getApplicationInfo(packageName, DumpState.DUMP_CHANGES).uid;
            } catch (PackageManager.NameNotFoundException e) {
                return -1;
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    private class Internal extends RoleManagerInternal {
        private Internal() {
        }

        @Override // com.android.server.role.RoleManagerInternal
        public ArrayMap<String, ArraySet<String>> getRolesAndHolders(int userId) {
            return RoleManagerService.this.getOrCreateUserState(userId).getRolesAndHolders();
        }
    }

    /* access modifiers changed from: private */
    public class DefaultBrowserProvider implements PackageManagerInternal.DefaultBrowserProvider {
        private DefaultBrowserProvider() {
        }

        public String getDefaultBrowser(int userId) {
            return (String) CollectionUtils.firstOrNull(RoleManagerService.this.getOrCreateUserState(userId).getRoleHolders("android.app.role.BROWSER"));
        }

        public boolean setDefaultBrowser(String packageName, int userId) {
            CompletableFuture<Void> future = new CompletableFuture<>();
            RemoteCallback callback = new RemoteCallback(new RemoteCallback.OnResultListener(future) {
                /* class com.android.server.role.$$Lambda$RoleManagerService$DefaultBrowserProvider$hEu5ePDZ4NgTuveH0byeCRbh1RU */
                private final /* synthetic */ CompletableFuture f$0;

                {
                    this.f$0 = r1;
                }

                public final void onResult(Bundle bundle) {
                    RoleManagerService.DefaultBrowserProvider.lambda$setDefaultBrowser$0(this.f$0, bundle);
                }
            });
            if (packageName != null) {
                RoleManagerService.this.getOrCreateController(userId).onAddRoleHolder("android.app.role.BROWSER", packageName, 0, callback);
            } else {
                RoleManagerService.this.getOrCreateController(userId).onClearRoleHolders("android.app.role.BROWSER", 0, callback);
            }
            try {
                future.get(5, TimeUnit.SECONDS);
                return true;
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                String str = RoleManagerService.LOG_TAG;
                Slog.e(str, "Exception while setting default browser: " + packageName, e);
                return false;
            }
        }

        static /* synthetic */ void lambda$setDefaultBrowser$0(CompletableFuture future, Bundle result) {
            if (result != null) {
                future.complete(null);
            } else {
                future.completeExceptionally(new RuntimeException());
            }
        }

        public void setDefaultBrowserAsync(String packageName, int userId) {
            RemoteCallback callback = new RemoteCallback(new RemoteCallback.OnResultListener(packageName) {
                /* class com.android.server.role.$$Lambda$RoleManagerService$DefaultBrowserProvider$cU2Hhx52nmVnJXJvHuAnRTzxST0 */
                private final /* synthetic */ String f$0;

                {
                    this.f$0 = r1;
                }

                public final void onResult(Bundle bundle) {
                    RoleManagerService.DefaultBrowserProvider.lambda$setDefaultBrowserAsync$1(this.f$0, bundle);
                }
            });
            if (packageName != null) {
                RoleManagerService.this.getOrCreateController(userId).onAddRoleHolder("android.app.role.BROWSER", packageName, 0, callback);
            } else {
                RoleManagerService.this.getOrCreateController(userId).onClearRoleHolders("android.app.role.BROWSER", 0, callback);
            }
        }

        static /* synthetic */ void lambda$setDefaultBrowserAsync$1(String packageName, Bundle result) {
            if (!(result != null)) {
                String str = RoleManagerService.LOG_TAG;
                Slog.e(str, "Failed to set default browser: " + packageName);
            }
        }
    }

    private class DefaultDialerProvider implements PackageManagerInternal.DefaultDialerProvider {
        private DefaultDialerProvider() {
        }

        public String getDefaultDialer(int userId) {
            return (String) CollectionUtils.firstOrNull(RoleManagerService.this.getOrCreateUserState(userId).getRoleHolders("android.app.role.DIALER"));
        }
    }

    /* access modifiers changed from: private */
    public class DefaultHomeProvider implements PackageManagerInternal.DefaultHomeProvider {
        private DefaultHomeProvider() {
        }

        public String getDefaultHome(int userId) {
            return (String) CollectionUtils.firstOrNull(RoleManagerService.this.getOrCreateUserState(userId).getRoleHolders("android.app.role.HOME"));
        }

        public void setDefaultHomeAsync(String packageName, int userId, Consumer<Boolean> callback) {
            RemoteCallback remoteCallback = new RemoteCallback(new RemoteCallback.OnResultListener(packageName, callback) {
                /* class com.android.server.role.$$Lambda$RoleManagerService$DefaultHomeProvider$9eeqZaqhD2FohE8PZOcBaWBSZu4 */
                private final /* synthetic */ String f$0;
                private final /* synthetic */ Consumer f$1;

                {
                    this.f$0 = r1;
                    this.f$1 = r2;
                }

                public final void onResult(Bundle bundle) {
                    RoleManagerService.DefaultHomeProvider.lambda$setDefaultHomeAsync$0(this.f$0, this.f$1, bundle);
                }
            });
            if (packageName != null) {
                RoleManagerService.this.getOrCreateController(userId).onAddRoleHolder("android.app.role.HOME", packageName, 0, remoteCallback);
            } else {
                RoleManagerService.this.getOrCreateController(userId).onClearRoleHolders("android.app.role.HOME", 0, remoteCallback);
            }
        }

        static /* synthetic */ void lambda$setDefaultHomeAsync$0(String packageName, Consumer callback, Bundle result) {
            boolean successful = result != null;
            if (!successful) {
                String str = RoleManagerService.LOG_TAG;
                Slog.e(str, "Failed to set default home: " + packageName);
            }
            callback.accept(Boolean.valueOf(successful));
        }
    }
}
