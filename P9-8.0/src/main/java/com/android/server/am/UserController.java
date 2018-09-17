package com.android.server.am;

import android.app.AppGlobals;
import android.app.Dialog;
import android.app.IStopUserCallback;
import android.app.IUserSwitchObserver;
import android.app.KeyguardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.IIntentReceiver;
import android.content.Intent;
import android.content.pm.IPackageManager;
import android.content.pm.UserInfo;
import android.content.res.Configuration;
import android.encrypt.ISDCardCryptedHelper;
import android.hwtheme.HwThemeManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.IProgressListener;
import android.os.IRemoteCallback;
import android.os.IUserManager.Stub;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManagerInternal;
import android.os.storage.IStorageManager;
import android.os.storage.StorageManager;
import android.provider.Settings.System;
import android.util.ArraySet;
import android.util.IntArray;
import android.util.Pair;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.Toast;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.Preconditions;
import com.android.internal.widget.LockPatternUtils;
import com.android.server.HwServiceFactory;
import com.android.server.LocalServices;
import com.android.server.pm.UserManagerService;
import com.android.server.power.IHwShutdownThread;
import com.android.server.wm.WindowManagerService;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

final class UserController {
    static final int MAX_RUNNING_USERS = 2;
    private static final String TAG = "ActivityManager";
    static final int USER_SWITCH_TIMEOUT = 3000;
    @GuardedBy("mLock")
    private volatile ArraySet<String> mCurWaitingUserSwitchCallbacks;
    @GuardedBy("mLock")
    private int[] mCurrentProfileIds;
    @GuardedBy("mLock")
    private int mCurrentUserId;
    private final Handler mHandler;
    final Injector mInjector;
    private final Object mLock;
    private final LockPatternUtils mLockPatternUtils;
    @GuardedBy("mLock")
    private int[] mStartedUserArray;
    @GuardedBy("mLock")
    private final SparseArray<UserState> mStartedUsers;
    @GuardedBy("mLock")
    private int mTargetUserId;
    @GuardedBy("mLock")
    private final ArrayList<Integer> mUserLru;
    private volatile UserManagerService mUserManager;
    private final SparseIntArray mUserProfileGroupIdsSelfLocked;
    private final RemoteCallbackList<IUserSwitchObserver> mUserSwitchObservers;
    boolean mUserSwitchUiEnabled;

    static class Injector {
        private final ActivityManagerService mService;
        private UserManagerService mUserManager;
        private UserManagerInternal mUserManagerInternal;

        Injector(ActivityManagerService service) {
            this.mService = service;
        }

        protected Object getLock() {
            return this.mService;
        }

        protected Handler getHandler() {
            return this.mService.mHandler;
        }

        protected Context getContext() {
            return this.mService.mContext;
        }

        protected LockPatternUtils getLockPatternUtils() {
            return new LockPatternUtils(getContext());
        }

        protected int broadcastIntentLocked(Intent intent, String resolvedType, IIntentReceiver resultTo, int resultCode, String resultData, Bundle resultExtras, String[] requiredPermissions, int appOp, Bundle bOptions, boolean ordered, boolean sticky, int callingPid, int callingUid, int userId) {
            return this.mService.broadcastIntentLocked(null, null, intent, resolvedType, resultTo, resultCode, resultData, resultExtras, requiredPermissions, appOp, bOptions, ordered, sticky, callingPid, callingUid, userId);
        }

        int checkCallingPermission(String permission) {
            return this.mService.checkCallingPermission(permission);
        }

        WindowManagerService getWindowManager() {
            return this.mService.mWindowManager;
        }

        void activityManagerOnUserStopped(int userId) {
            this.mService.onUserStoppedLocked(userId);
        }

        void systemServiceManagerCleanupUser(int userId) {
            this.mService.mSystemServiceManager.cleanupUser(userId);
        }

        protected UserManagerService getUserManager() {
            if (this.mUserManager == null) {
                this.mUserManager = (UserManagerService) Stub.asInterface(ServiceManager.getService("user"));
            }
            return this.mUserManager;
        }

        UserManagerInternal getUserManagerInternal() {
            if (this.mUserManagerInternal == null) {
                this.mUserManagerInternal = (UserManagerInternal) LocalServices.getService(UserManagerInternal.class);
            }
            return this.mUserManagerInternal;
        }

        KeyguardManager getKeyguardManager() {
            return (KeyguardManager) this.mService.mContext.getSystemService(KeyguardManager.class);
        }

        void batteryStatsServiceNoteEvent(int code, String name, int uid) {
            this.mService.mBatteryStatsService.noteEvent(code, name, uid);
        }

        void systemServiceManagerStopUser(int userId) {
            this.mService.mSystemServiceManager.stopUser(userId);
        }

        boolean isRuntimeRestarted() {
            return this.mService.mSystemServiceManager.isRuntimeRestarted();
        }

        boolean isFirstBootOrUpgrade() {
            IPackageManager pm = AppGlobals.getPackageManager();
            try {
                return !pm.isFirstBoot() ? pm.isUpgrade() : true;
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }

        void sendPreBootBroadcast(int userId, boolean quiet, Runnable onFinish) {
            final Runnable runnable = onFinish;
            new PreBootBroadcaster(this.mService, userId, null, quiet) {
                public void onFinished() {
                    runnable.run();
                }
            }.sendNext();
        }

        void activityManagerForceStopPackageLocked(int userId, String reason) {
            this.mService.forceStopPackageLocked(null, -1, false, false, true, false, false, userId, reason);
        }

        int checkComponentPermission(String permission, int pid, int uid, int owningUid, boolean exported) {
            return this.mService.checkComponentPermission(permission, pid, uid, owningUid, exported);
        }

        void startHomeActivityLocked(int userId, String reason) {
            this.mService.startHomeActivityLocked(userId, reason);
        }

        void updateUserConfigurationLocked() {
            this.mService.updateUserConfigurationLocked();
        }

        void clearBroadcastQueueForUserLocked(int userId) {
            this.mService.clearBroadcastQueueForUserLocked(userId);
        }

        void enforceShellRestriction(String restriction, int userId) {
            this.mService.enforceShellRestriction(restriction, userId);
        }

        void showUserSwitchingDialog(UserInfo fromUser, UserInfo toUser) {
            Dialog d = new UserSwitchingDialog(this.mService, this.mService.mContext, fromUser, toUser, true);
            d.show();
            Window window = d.getWindow();
            LayoutParams lp = window.getAttributes();
            lp.width = -1;
            window.setAttributes(lp);
        }

        void updatePersistentConfiguration(Configuration config) {
            this.mService.updatePersistentConfiguration(config);
        }

        ActivityStackSupervisor getActivityStackSupervisor() {
            return this.mService.mStackSupervisor;
        }

        private boolean shouldSkipKeyguard(UserInfo first, UserInfo second) {
            if (this.mService.isHiddenSpaceSwitch(first, second)) {
                return getKeyguardManager().isKeyguardLocked();
            }
            return false;
        }
    }

    UserController(ActivityManagerService service) {
        this(new Injector(service));
    }

    UserController(Injector injector) {
        this.mCurrentUserId = 0;
        this.mTargetUserId = -10000;
        this.mStartedUsers = new SparseArray();
        this.mUserLru = new ArrayList();
        this.mStartedUserArray = new int[]{0};
        this.mCurrentProfileIds = new int[0];
        this.mUserProfileGroupIdsSelfLocked = new SparseIntArray();
        this.mUserSwitchObservers = new RemoteCallbackList();
        this.mUserSwitchUiEnabled = true;
        this.mInjector = injector;
        this.mLock = injector.getLock();
        this.mHandler = injector.getHandler();
        this.mStartedUsers.put(0, new UserState(UserHandle.SYSTEM));
        this.mUserLru.add(Integer.valueOf(0));
        this.mLockPatternUtils = this.mInjector.getLockPatternUtils();
        updateStartedUserArrayLocked();
    }

    void finishUserSwitch(UserState uss) {
        long startedTime = SystemClock.elapsedRealtime();
        synchronized (this.mLock) {
            finishUserBoot(uss);
            startProfilesLocked();
            stopRunningUsersLocked(2);
        }
        Slog.i(TAG, "_StartUser finishUserSwitch userid:" + uss.mHandle.getIdentifier() + " cost " + (SystemClock.elapsedRealtime() - startedTime) + " ms");
    }

    void stopRunningUsersLocked(int maxRunningUsers) {
        int num = this.mUserLru.size();
        int i = 0;
        while (num > maxRunningUsers && i < this.mUserLru.size()) {
            Integer oldUserId = (Integer) this.mUserLru.get(i);
            UserState oldUss = (UserState) this.mStartedUsers.get(oldUserId.intValue());
            if (oldUss == null) {
                this.mUserLru.remove(i);
                num--;
            } else if (oldUss.state == 4 || oldUss.state == 5) {
                num--;
                i++;
            } else if (oldUserId.intValue() == 0 || oldUserId.intValue() == this.mCurrentUserId) {
                if (UserInfo.isSystemOnly(oldUserId.intValue())) {
                    num--;
                }
                i++;
            } else if (stopUsersLocked(oldUserId.intValue(), false, null) != 0) {
                Slog.i(TAG, "stopRunningUsersLocked cannot stop " + oldUserId);
                num--;
                i++;
            } else {
                num--;
                i++;
            }
        }
    }

    private void finishUserBoot(UserState uss) {
        finishUserBoot(uss, null);
    }

    /* JADX WARNING: Missing block: B:32:0x016b, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void finishUserBoot(UserState uss, IIntentReceiver resultTo) {
        int userId = uss.mHandle.getIdentifier();
        Slog.d(TAG, "Finishing user boot " + userId);
        synchronized (this.mLock) {
            if (this.mStartedUsers.get(userId) != uss) {
                return;
            }
            if (uss.setState(0, 1)) {
                this.mInjector.getUserManagerInternal().setUserState(userId, uss.state);
                if (!(userId != 0 || (this.mInjector.isRuntimeRestarted() ^ 1) == 0 || (this.mInjector.isFirstBootOrUpgrade() ^ 1) == 0)) {
                    int uptimeSeconds = (int) (SystemClock.elapsedRealtime() / 1000);
                    MetricsLogger.histogram(this.mInjector.getContext(), "framework_locked_boot_completed", uptimeSeconds);
                    if (uptimeSeconds > 120) {
                        Slog.wtf("SystemServerTiming", "finishUserBoot took too long. uptimeSeconds=" + uptimeSeconds);
                    }
                }
                this.mHandler.sendMessage(this.mHandler.obtainMessage(64, userId, 0));
                Intent intent = new Intent("android.intent.action.LOCKED_BOOT_COMPLETED", null);
                intent.putExtra("android.intent.extra.user_handle", userId);
                intent.addFlags(150994944);
                this.mInjector.broadcastIntentLocked(intent, null, resultTo, 0, null, null, new String[]{"android.permission.RECEIVE_BOOT_COMPLETED"}, -1, null, true, false, ActivityManagerService.MY_PID, 1000, userId);
            }
            if (this.mInjector.getUserManager().isManagedProfile(userId) || this.mInjector.getUserManager().isClonedProfile(userId)) {
                UserInfo parent = this.mInjector.getUserManager().getProfileParent(userId);
                if (parent != null) {
                    if (isUserRunningLocked(parent.id, 4)) {
                        Slog.d(TAG, "User " + userId + " (parent " + parent.id + "): attempting unlock because parent is unlocked");
                        maybeUnlockUser(userId);
                    }
                }
                Slog.d(TAG, "User " + userId + " (parent " + (parent == null ? "<null>" : String.valueOf(parent.id)) + "): delaying unlock because parent is locked");
            } else {
                maybeUnlockUser(userId);
            }
        }
    }

    /* JADX WARNING: Missing block: B:19:0x0037, code:
            if (r0 == false) goto L_0x006d;
     */
    /* JADX WARNING: Missing block: B:20:0x0039, code:
            r6.mUnlockProgress.start();
            r6.mUnlockProgress.setProgress(5, r5.mInjector.getContext().getString(17039572));
            r5.mInjector.getUserManager().onBeforeUnlockUser(r1);
            r6.mUnlockProgress.setProgress(20);
            r5.mHandler.obtainMessage(59, r1, 0, r6).sendToTarget();
     */
    /* JADX WARNING: Missing block: B:21:0x006d, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void finishUserUnlocking(UserState uss) {
        int userId = uss.mHandle.getIdentifier();
        boolean proceedWithUnlock = false;
        synchronized (this.mLock) {
            if (this.mStartedUsers.get(uss.mHandle.getIdentifier()) != uss) {
            } else if (!StorageManager.isUserKeyUnlocked(userId)) {
            } else if (uss.setState(1, 2)) {
                this.mInjector.getUserManagerInternal().setUserState(userId, uss.state);
                proceedWithUnlock = true;
            }
        }
    }

    /* JADX WARNING: Missing block: B:33:0x0122, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void finishUserUnlocked(UserState uss) {
        int userId = uss.mHandle.getIdentifier();
        synchronized (this.mLock) {
            if (this.mStartedUsers.get(uss.mHandle.getIdentifier()) != uss) {
            } else if (!StorageManager.isUserKeyUnlocked(userId)) {
            } else if (uss.setState(2, 3)) {
                this.mInjector.getUserManagerInternal().setUserState(userId, uss.state);
                uss.mUnlockProgress.finish();
                Intent unlockedIntent = new Intent("android.intent.action.USER_UNLOCKED");
                unlockedIntent.putExtra("android.intent.extra.user_handle", userId);
                unlockedIntent.addFlags(1342177280);
                this.mInjector.broadcastIntentLocked(unlockedIntent, null, null, 0, null, null, null, -1, null, false, false, ActivityManagerService.MY_PID, 1000, userId);
                if (getUserInfo(userId).isManagedProfile()) {
                    UserInfo parent = this.mInjector.getUserManager().getProfileParent(userId);
                    if (parent != null) {
                        Intent intent = new Intent("android.intent.action.MANAGED_PROFILE_UNLOCKED");
                        intent.putExtra("android.intent.extra.USER", UserHandle.of(userId));
                        intent.addFlags(1342177280);
                        this.mInjector.broadcastIntentLocked(intent, null, null, 0, null, null, null, -1, null, false, false, ActivityManagerService.MY_PID, 1000, parent.id);
                    }
                }
                UserInfo info = getUserInfo(userId);
                if (Objects.equals(info.lastLoggedInFingerprint, Build.FINGERPRINT)) {
                    finishUserUnlockedCompleted(uss);
                } else {
                    boolean quiet;
                    if (!info.isManagedProfile() && !info.isClonedProfile()) {
                        quiet = false;
                    } else if (uss.tokenProvided) {
                        quiet = this.mLockPatternUtils.isSeparateProfileChallengeEnabled(userId) ^ 1;
                    } else {
                        quiet = true;
                    }
                    this.mInjector.sendPreBootBroadcast(userId, quiet, new -$Lambda$-wbdEBNBIl8hthLGGkbuzj1haLA(this, uss));
                }
            }
        }
    }

    private void finishUserUnlockedCompleted(UserState uss) {
        int userId = uss.mHandle.getIdentifier();
        synchronized (this.mLock) {
            if (this.mStartedUsers.get(uss.mHandle.getIdentifier()) != uss) {
                return;
            }
            UserInfo userInfo = getUserInfo(userId);
            if (userInfo == null) {
            } else if (StorageManager.isUserKeyUnlocked(userId)) {
                this.mInjector.getUserManager().onUserLoggedIn(userId);
                if (!(userInfo.isInitialized() || userId == 0)) {
                    Slog.d(TAG, "Initializing user #" + userId);
                    Intent intent = new Intent("android.intent.action.USER_INITIALIZE");
                    intent.addFlags(285212672);
                    final UserInfo userInfo2 = userInfo;
                    this.mInjector.broadcastIntentLocked(intent, null, new IIntentReceiver.Stub() {
                        public void performReceive(Intent intent, int resultCode, String data, Bundle extras, boolean ordered, boolean sticky, int sendingUser) {
                            UserController.this.mInjector.getUserManager().makeInitialized(userInfo2.id);
                        }
                    }, 0, null, null, null, -1, null, true, false, ActivityManagerService.MY_PID, 1000, userId);
                }
                Slog.i(TAG, "Sending BOOT_COMPLETE user #" + userId);
                if (!(userId != 0 || (this.mInjector.isRuntimeRestarted() ^ 1) == 0 || (this.mInjector.isFirstBootOrUpgrade() ^ 1) == 0)) {
                    MetricsLogger.histogram(this.mInjector.getContext(), "framework_boot_completed", (int) (SystemClock.elapsedRealtime() / 1000));
                }
                Intent intent2 = new Intent("android.intent.action.BOOT_COMPLETED", null);
                intent2.putExtra("android.intent.extra.user_handle", userId);
                intent2.addFlags(150994944);
                final int i = userId;
                this.mInjector.broadcastIntentLocked(intent2, null, new IIntentReceiver.Stub() {
                    public void performReceive(Intent intent, int resultCode, String data, Bundle extras, boolean ordered, boolean sticky, int sendingUser) throws RemoteException {
                        Slog.i(UserController.TAG, "Finished processing BOOT_COMPLETED for u" + i);
                    }
                }, 0, null, null, new String[]{"android.permission.RECEIVE_BOOT_COMPLETED"}, -1, null, true, false, ActivityManagerService.MY_PID, 1000, userId);
            }
        }
    }

    int restartUser(int userId, final boolean foreground) {
        return stopUser(userId, true, new IStopUserCallback.Stub() {
            /* synthetic */ void lambda$-com_android_server_am_UserController$3_23161(int userId, boolean foreground) {
                UserController.this.startUser(userId, foreground);
            }

            public void userStopped(int userId) {
                UserController.this.mHandler.post(new com.android.server.am.-$Lambda$-wbdEBNBIl8hthLGGkbuzj1haLA.AnonymousClass1(foreground, userId, this));
            }

            public void userStopAborted(int userId) {
            }
        });
    }

    int stopUser(int userId, boolean force, IStopUserCallback callback) {
        if (this.mInjector.checkCallingPermission("android.permission.INTERACT_ACROSS_USERS_FULL") != 0) {
            String msg = "Permission Denial: switchUser() from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + " requires " + "android.permission.INTERACT_ACROSS_USERS_FULL";
            Slog.w(TAG, msg);
            throw new SecurityException(msg);
        } else if (userId < 0 || userId == 0) {
            throw new IllegalArgumentException("Can't stop system user " + userId);
        } else {
            int stopUsersLocked;
            this.mInjector.enforceShellRestriction("no_debugging_features", userId);
            synchronized (this.mLock) {
                stopUsersLocked = stopUsersLocked(userId, force, callback);
            }
            return stopUsersLocked;
        }
    }

    private int stopUsersLocked(int userId, boolean force, IStopUserCallback callback) {
        if (userId == 0) {
            return -3;
        }
        if (isCurrentUserLocked(userId)) {
            return -2;
        }
        int[] usersToStop = getUsersToStopLocked(userId);
        for (int relatedUserId : usersToStop) {
            if (relatedUserId == 0 || isCurrentUserLocked(relatedUserId)) {
                if (ActivityManagerDebugConfig.DEBUG_MU) {
                    Slog.i(TAG, "stopUsersLocked cannot stop related user " + relatedUserId);
                }
                if (!force) {
                    return -4;
                }
                Slog.i(TAG, "Force stop user " + userId + ". Related users will not be stopped");
                stopSingleUserLocked(userId, callback);
                return 0;
            }
        }
        if (ActivityManagerDebugConfig.DEBUG_MU) {
            Slog.i(TAG, "stopUsersLocked usersToStop=" + Arrays.toString(usersToStop));
        }
        for (int userIdToStop : usersToStop) {
            stopSingleUserLocked(userIdToStop, userIdToStop == userId ? callback : null);
        }
        return 0;
    }

    private void stopSingleUserLocked(int userId, IStopUserCallback callback) {
        if (ActivityManagerDebugConfig.DEBUG_MU) {
            Slog.i(TAG, "stopSingleUserLocked userId=" + userId);
        }
        UserState uss = (UserState) this.mStartedUsers.get(userId);
        if (uss == null) {
            if (callback != null) {
                final IStopUserCallback iStopUserCallback = callback;
                final int i = userId;
                this.mHandler.post(new Runnable() {
                    public void run() {
                        try {
                            iStopUserCallback.userStopped(i);
                        } catch (RemoteException e) {
                        }
                    }
                });
            }
            return;
        }
        if (callback != null) {
            uss.mStopCallbacks.add(callback);
        }
        if (!(uss.state == 4 || uss.state == 5)) {
            uss.setState(4);
            this.mInjector.getUserManagerInternal().setUserState(userId, uss.state);
            updateStartedUserArrayLocked();
            long ident = Binder.clearCallingIdentity();
            try {
                Intent stoppingIntent = new Intent("android.intent.action.USER_STOPPING");
                stoppingIntent.addFlags(1073741824);
                stoppingIntent.putExtra("android.intent.extra.user_handle", userId);
                stoppingIntent.putExtra("android.intent.extra.SHUTDOWN_USERSPACE_ONLY", true);
                final int i2 = userId;
                final UserState userState = uss;
                IIntentReceiver stoppingReceiver = new IIntentReceiver.Stub() {
                    public void performReceive(Intent intent, int resultCode, String data, Bundle extras, boolean ordered, boolean sticky, int sendingUser) {
                        Handler -get2 = UserController.this.mHandler;
                        final int i = i2;
                        final UserState userState = userState;
                        -get2.post(new Runnable() {
                            public void run() {
                                UserController.this.finishUserStopping(i, userState);
                            }
                        });
                    }
                };
                this.mInjector.clearBroadcastQueueForUserLocked(userId);
                this.mInjector.broadcastIntentLocked(stoppingIntent, null, stoppingReceiver, 0, null, null, new String[]{"android.permission.INTERACT_ACROSS_USERS"}, -1, null, true, false, ActivityManagerService.MY_PID, 1000, -1);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    void finishUserStopping(int userId, UserState uss) {
        Intent shutdownIntent = new Intent("android.intent.action.ACTION_SHUTDOWN");
        shutdownIntent.putExtra("android.intent.extra.SHUTDOWN_USERSPACE_ONLY", true);
        shutdownIntent.addFlags(16777216);
        final UserState userState = uss;
        IIntentReceiver shutdownReceiver = new IIntentReceiver.Stub() {
            public void performReceive(Intent intent, int resultCode, String data, Bundle extras, boolean ordered, boolean sticky, int sendingUser) {
                Handler -get2 = UserController.this.mHandler;
                final UserState userState = userState;
                -get2.post(new Runnable() {
                    public void run() {
                        UserController.this.finishUserStopped(userState);
                    }
                });
            }
        };
        synchronized (this.mLock) {
            if (uss.state != 4) {
                return;
            }
            uss.setState(5);
            this.mInjector.getUserManagerInternal().setUserState(userId, uss.state);
            this.mInjector.batteryStatsServiceNoteEvent(16391, Integer.toString(userId), userId);
            this.mInjector.systemServiceManagerStopUser(userId);
            synchronized (this.mLock) {
                this.mInjector.broadcastIntentLocked(shutdownIntent, null, shutdownReceiver, 0, null, null, null, -1, null, true, false, ActivityManagerService.MY_PID, 1000, userId);
            }
        }
    }

    void finishUserStopped(UserState uss) {
        ArrayList<IStopUserCallback> callbacks;
        boolean stopped;
        int userId = uss.mHandle.getIdentifier();
        synchronized (this.mLock) {
            callbacks = new ArrayList(uss.mStopCallbacks);
            if (this.mStartedUsers.get(userId) != uss) {
                stopped = false;
            } else if (uss.state != 5) {
                stopped = false;
            } else {
                stopped = true;
                this.mStartedUsers.remove(userId);
                this.mInjector.getUserManagerInternal().removeUserState(userId);
                this.mUserLru.remove(Integer.valueOf(userId));
                updateStartedUserArrayLocked();
                this.mInjector.activityManagerOnUserStopped(userId);
                forceStopUserLocked(userId, "finish user");
            }
        }
        for (int i = 0; i < callbacks.size(); i++) {
            if (stopped) {
                try {
                    ((IStopUserCallback) callbacks.get(i)).userStopped(userId);
                } catch (RemoteException e) {
                }
            } else {
                ((IStopUserCallback) callbacks.get(i)).userStopAborted(userId);
            }
        }
        if (stopped) {
            UserInfo userInfo = getUserInfo(userId);
            if (!(userInfo != null ? userInfo.isHwHiddenSpace() : false)) {
                try {
                    getStorageManager().lockUserKey(userId);
                } catch (RemoteException re) {
                    throw re.rethrowAsRuntimeException();
                }
            }
            this.mInjector.systemServiceManagerCleanupUser(userId);
            synchronized (this.mLock) {
                this.mInjector.getActivityStackSupervisor().removeUserLocked(userId);
            }
            if (getUserInfo(userId).isEphemeral()) {
                this.mInjector.getUserManager().removeUser(userId);
            }
        }
    }

    private int[] getUsersToStopLocked(int userId) {
        int startedUsersSize = this.mStartedUsers.size();
        IntArray userIds = new IntArray();
        userIds.add(userId);
        synchronized (this.mUserProfileGroupIdsSelfLocked) {
            int userGroupId = this.mUserProfileGroupIdsSelfLocked.get(userId, -10000);
            for (int i = 0; i < startedUsersSize; i++) {
                int startedUserId = ((UserState) this.mStartedUsers.valueAt(i)).mHandle.getIdentifier();
                boolean sameGroup = userGroupId != -10000 ? userGroupId == this.mUserProfileGroupIdsSelfLocked.get(startedUserId, -10000) : false;
                boolean sameUserId = startedUserId == userId;
                if (sameGroup && !sameUserId) {
                    userIds.add(startedUserId);
                }
            }
        }
        return userIds.toArray();
    }

    private void forceStopUserLocked(int userId, String reason) {
        this.mInjector.activityManagerForceStopPackageLocked(userId, reason);
        Intent intent = new Intent("android.intent.action.USER_STOPPED");
        intent.addFlags(1342177280);
        intent.putExtra("android.intent.extra.user_handle", userId);
        this.mInjector.broadcastIntentLocked(intent, null, null, 0, null, null, null, -1, null, false, false, ActivityManagerService.MY_PID, 1000, -1);
    }

    private void stopGuestOrEphemeralUserIfBackground() {
        synchronized (this.mLock) {
            int num = this.mUserLru.size();
            for (int i = 0; i < num; i++) {
                Integer oldUserId = (Integer) this.mUserLru.get(i);
                UserState oldUss = (UserState) this.mStartedUsers.get(oldUserId.intValue());
                if (!(oldUserId.intValue() == 0 || oldUserId.intValue() == this.mCurrentUserId || oldUss.state == 4 || oldUss.state == 5)) {
                    UserInfo userInfo = getUserInfo(oldUserId.intValue());
                    if (userInfo.isEphemeral()) {
                        ((UserManagerInternal) LocalServices.getService(UserManagerInternal.class)).onEphemeralUserStop(oldUserId.intValue());
                    }
                    if (userInfo.isGuest() || userInfo.isEphemeral()) {
                        stopUsersLocked(oldUserId.intValue(), true, null);
                        break;
                    }
                }
            }
        }
    }

    void startProfilesLocked() {
        if (ActivityManagerDebugConfig.DEBUG_MU) {
            Slog.i(TAG, "startProfilesLocked");
        }
        List<UserInfo> profiles = this.mInjector.getUserManager().getProfiles(this.mCurrentUserId, false);
        List<UserInfo> profilesToStart = new ArrayList(profiles.size());
        for (UserInfo user : profiles) {
            if (!((user.flags & 16) != 16 || user.id == this.mCurrentUserId || (user.isQuietModeEnabled() ^ 1) == 0)) {
                profilesToStart.add(user);
            }
        }
        int profilesToStartSize = profilesToStart.size();
        int i = 0;
        while (i < profilesToStartSize) {
            startUser(((UserInfo) profilesToStart.get(i)).id, false);
            i++;
        }
        if (i < profilesToStartSize) {
            Slog.w(TAG, "More profiles than MAX_RUNNING_USERS");
        }
    }

    private IStorageManager getStorageManager() {
        return IStorageManager.Stub.asInterface(ServiceManager.getService("mount"));
    }

    private void setMultiDpi(WindowManagerService wms, int userId) {
        int dpi = SystemProperties.getInt("persist.sys.dpi", 0);
        int width = SystemProperties.getInt("persist.sys.rog.width", 0);
        int realdpi = SystemProperties.getInt("persist.sys.realdpi", 0);
        if (width > 0) {
            dpi = SystemProperties.getInt("persist.sys.realdpi", SystemProperties.getInt("persist.sys.dpi", SystemProperties.getInt("ro.sf.real_lcd_density", SystemProperties.getInt("ro.sf.lcd_density", 0))));
        }
        if (wms != null && dpi > 0) {
            Slog.i(TAG, "set multi dpi for user :" + userId + ", sys.dpi:" + dpi + ", readdpi:" + realdpi + ", width:" + width);
            wms.setForcedDisplayDensityForUser(0, dpi, userId);
        }
    }

    boolean startUser(int userId, boolean foreground) {
        if (this.mInjector.checkCallingPermission("android.permission.INTERACT_ACROSS_USERS_FULL") != 0) {
            String msg = "Permission Denial: switchUser() from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + " requires " + "android.permission.INTERACT_ACROSS_USERS_FULL";
            Slog.w(TAG, msg);
            throw new SecurityException(msg);
        }
        long startedTime = SystemClock.elapsedRealtime();
        Slog.i(TAG, "Starting userid:" + userId + " fg:" + foreground);
        long ident = Binder.clearCallingIdentity();
        try {
            synchronized (this.mLock) {
                int oldUserId = this.mCurrentUserId;
                if (oldUserId != userId) {
                    if (foreground) {
                        this.mInjector.getActivityStackSupervisor().setLockTaskModeLocked(null, 0, "startUser", false);
                    }
                    UserInfo userInfo = getUserInfo(userId);
                    if (userInfo == null) {
                        Slog.w(TAG, "No user info for user #" + userId);
                        Binder.restoreCallingIdentity(ident);
                        return false;
                    }
                    boolean isClonedProfile;
                    Intent intent;
                    if (foreground) {
                        if (userInfo.isManagedProfile()) {
                            Slog.w(TAG, "Cannot switch to User #" + userId + ": not a full user");
                            Binder.restoreCallingIdentity(ident);
                            return false;
                        }
                    }
                    setMultiDpi(this.mInjector.getWindowManager(), userId);
                    UserInfo lastUserInfo = getUserInfo(this.mCurrentUserId);
                    if (foreground && this.mUserSwitchUiEnabled && (this.mInjector.shouldSkipKeyguard(lastUserInfo, userInfo) ^ 1) != 0) {
                        this.mInjector.getWindowManager().startFreezingScreen(17432707, 17432706);
                    }
                    boolean needStart = false;
                    if (this.mStartedUsers.get(userId) == null) {
                        UserState userState = new UserState(UserHandle.of(userId));
                        this.mStartedUsers.put(userId, userState);
                        this.mInjector.getUserManagerInternal().setUserState(userId, userState.state);
                        updateStartedUserArrayLocked();
                        needStart = true;
                    }
                    UserState uss = (UserState) this.mStartedUsers.get(userId);
                    Integer userIdInt = Integer.valueOf(userId);
                    if (getUserInfo(userIdInt.intValue()) != null) {
                        isClonedProfile = getUserInfo(userIdInt.intValue()).isClonedProfile();
                    } else {
                        isClonedProfile = false;
                    }
                    if (!isClonedProfile) {
                        this.mUserLru.remove(userIdInt);
                        this.mUserLru.add(userIdInt);
                    }
                    if (foreground) {
                        this.mCurrentUserId = userId;
                        this.mInjector.updateUserConfigurationLocked();
                        this.mTargetUserId = -10000;
                        updateCurrentProfileIdsLocked();
                        this.mInjector.getWindowManager().setCurrentUser(userId, this.mCurrentProfileIds);
                        HwThemeManager.linkDataSkinDirAsUser(userId);
                        if (this.mUserSwitchUiEnabled && (this.mInjector.shouldSkipKeyguard(lastUserInfo, userInfo) ^ 1) != 0) {
                            this.mInjector.getWindowManager().setSwitchingUser(true);
                            this.mInjector.getWindowManager().lockNow(null);
                        }
                    } else {
                        Integer currentUserIdInt = Integer.valueOf(this.mCurrentUserId);
                        updateCurrentProfileIdsLocked();
                        this.mInjector.getWindowManager().setCurrentProfileIds(this.mCurrentProfileIds);
                        this.mUserLru.remove(currentUserIdInt);
                        this.mUserLru.add(currentUserIdInt);
                    }
                    if (uss.state == 4) {
                        uss.setState(uss.lastState);
                        this.mInjector.getUserManagerInternal().setUserState(userId, uss.state);
                        updateStartedUserArrayLocked();
                        needStart = true;
                    } else if (uss.state == 5) {
                        uss.setState(0);
                        this.mInjector.getUserManagerInternal().setUserState(userId, uss.state);
                        updateStartedUserArrayLocked();
                        needStart = true;
                    }
                    if (uss.state == 0) {
                        this.mInjector.getUserManager().onBeforeStartUser(userId);
                        this.mHandler.sendMessage(this.mHandler.obtainMessage(42, userId, 0));
                    }
                    if (foreground) {
                        this.mHandler.sendMessage(this.mHandler.obtainMessage(43, userId, oldUserId));
                        this.mHandler.removeMessages(34);
                        this.mHandler.removeMessages(36);
                        this.mHandler.sendMessage(this.mHandler.obtainMessage(34, oldUserId, userId, uss));
                        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(36, oldUserId, userId, uss), 3000);
                    }
                    if (needStart) {
                        intent = new Intent("android.intent.action.USER_STARTED");
                        intent.addFlags(1342177280);
                        intent.putExtra("android.intent.extra.user_handle", userId);
                        this.mInjector.broadcastIntentLocked(intent, null, null, 0, null, null, null, -1, null, false, false, ActivityManagerService.MY_PID, 1000, userId);
                    }
                    if (foreground) {
                        moveUserToForegroundLocked(uss, oldUserId, userId);
                    } else {
                        finishUserBoot(uss);
                    }
                    if (needStart) {
                        intent = new Intent("android.intent.action.USER_STARTING");
                        intent.addFlags(1073741824);
                        intent.putExtra("android.intent.extra.user_handle", userId);
                        this.mInjector.broadcastIntentLocked(intent, null, new IIntentReceiver.Stub() {
                            public void performReceive(Intent intent, int resultCode, String data, Bundle extras, boolean ordered, boolean sticky, int sendingUser) throws RemoteException {
                            }
                        }, 0, null, null, new String[]{"android.permission.INTERACT_ACROSS_USERS"}, -1, null, true, false, ActivityManagerService.MY_PID, 1000, -1);
                    }
                    Binder.restoreCallingIdentity(ident);
                    Slog.i(TAG, "_StartUser startUser userid:" + userId + " cost " + (SystemClock.elapsedRealtime() - startedTime) + " ms");
                    return true;
                }
            }
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
        return true;
    }

    void startUserInForeground(int targetUserId) {
        String text;
        boolean success = startUser(targetUserId, true);
        if (success) {
            text = this.mInjector.getContext().getResources().getString(33685809);
        } else {
            text = this.mInjector.getContext().getResources().getString(33685810);
        }
        Toast.makeText(this.mInjector.getContext(), text, 1).show();
        if (!success) {
            this.mInjector.getWindowManager().setSwitchingUser(false);
        }
    }

    boolean unlockUser(int userId, byte[] token, byte[] secret, IProgressListener listener) {
        if (this.mInjector.checkCallingPermission("android.permission.INTERACT_ACROSS_USERS_FULL") != 0) {
            String msg = "Permission Denial: unlockUser() from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + " requires " + "android.permission.INTERACT_ACROSS_USERS_FULL";
            Slog.w(TAG, msg);
            throw new SecurityException(msg);
        }
        long binderToken = Binder.clearCallingIdentity();
        try {
            boolean unlockUserCleared = unlockUserCleared(userId, token, secret, listener);
            return unlockUserCleared;
        } finally {
            Binder.restoreCallingIdentity(binderToken);
        }
    }

    boolean maybeUnlockUser(int userId) {
        return unlockUserCleared(userId, null, null, null);
    }

    private static void notifyFinished(int userId, IProgressListener listener) {
        if (listener != null) {
            try {
                listener.onFinished(userId, null);
            } catch (RemoteException e) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:20:0x004d A:{Splitter: B:6:0x0013, ExcHandler: android.os.RemoteException (r4_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:20:0x004d, code:
            r4 = move-exception;
     */
    /* JADX WARNING: Missing block: B:22:?, code:
            android.util.Slog.w(TAG, "Failed to unlock: " + r4.getMessage());
     */
    /* JADX WARNING: Missing block: B:32:0x007e, code:
            finishUserUnlocking(r13);
            r3 = new android.util.ArraySet();
            r15 = r18.mLock;
     */
    /* JADX WARNING: Missing block: B:33:0x008c, code:
            monitor-enter(r15);
     */
    /* JADX WARNING: Missing block: B:34:0x008d, code:
            r6 = 0;
     */
    /* JADX WARNING: Missing block: B:37:0x0096, code:
            if (r6 >= r18.mStartedUsers.size()) goto L_0x00f7;
     */
    /* JADX WARNING: Missing block: B:38:0x0098, code:
            r11 = r18.mStartedUsers.keyAt(r6);
            r8 = r18.mInjector.getUserManager().getProfileParent(r11);
     */
    /* JADX WARNING: Missing block: B:39:0x00ac, code:
            if (r8 == null) goto L_0x00f2;
     */
    /* JADX WARNING: Missing block: B:41:0x00b2, code:
            if (r8.id != r19) goto L_0x00f2;
     */
    /* JADX WARNING: Missing block: B:43:0x00b6, code:
            if (r11 == r19) goto L_0x00f2;
     */
    /* JADX WARNING: Missing block: B:44:0x00b8, code:
            android.util.Slog.d(TAG, "User " + r11 + " (parent " + r8.id + "): attempting unlock because parent was just unlocked");
            r3.add(java.lang.Integer.valueOf(r11));
     */
    /* JADX WARNING: Missing block: B:45:0x00f2, code:
            r6 = r6 + 1;
     */
    /* JADX WARNING: Missing block: B:47:0x00f7, code:
            monitor-exit(r15);
     */
    /* JADX WARNING: Missing block: B:48:0x00f8, code:
            r9 = r3.size();
            r6 = 0;
     */
    /* JADX WARNING: Missing block: B:49:0x00fd, code:
            if (r6 >= r9) goto L_0x0114;
     */
    /* JADX WARNING: Missing block: B:50:0x00ff, code:
            maybeUnlockUser(((java.lang.Integer) r3.valueAt(r6)).intValue());
            r6 = r6 + 1;
     */
    /* JADX WARNING: Missing block: B:55:0x0115, code:
            return true;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    boolean unlockUserCleared(int userId, byte[] token, byte[] secret, IProgressListener listener) {
        synchronized (this.mLock) {
            if (!StorageManager.isUserKeyUnlocked(userId)) {
                try {
                    getStorageManager().unlockUserKey(userId, getUserInfo(userId).serialNumber, token, secret);
                } catch (Exception e) {
                }
            }
            ISDCardCryptedHelper helper = HwServiceFactory.getSDCardCryptedHelper();
            if (helper != null) {
                UserInfo info = getUserInfo(userId);
                if (info != null) {
                    helper.unlockKey(userId, info.serialNumber, token, secret);
                }
            }
            UserState uss = (UserState) this.mStartedUsers.get(userId);
            if (uss == null) {
                notifyFinished(userId, listener);
                return false;
            }
            uss.mUnlockProgress.addListener(listener);
            uss.tokenProvided = token != null;
        }
    }

    void showUserSwitchDialog(Pair<UserInfo, UserInfo> fromToUserPair) {
        this.mInjector.showUserSwitchingDialog((UserInfo) fromToUserPair.first, (UserInfo) fromToUserPair.second);
    }

    void dispatchForegroundProfileChanged(int userId) {
        int observerCount = this.mUserSwitchObservers.beginBroadcast();
        for (int i = 0; i < observerCount; i++) {
            try {
                ((IUserSwitchObserver) this.mUserSwitchObservers.getBroadcastItem(i)).onForegroundProfileSwitch(userId);
            } catch (RemoteException e) {
            }
        }
        this.mUserSwitchObservers.finishBroadcast();
    }

    void dispatchUserSwitchComplete(int userId) {
        long startedTime = SystemClock.elapsedRealtime();
        this.mInjector.getWindowManager().setSwitchingUser(false);
        int observerCount = this.mUserSwitchObservers.beginBroadcast();
        for (int i = 0; i < observerCount; i++) {
            try {
                ((IUserSwitchObserver) this.mUserSwitchObservers.getBroadcastItem(i)).onUserSwitchComplete(userId);
            } catch (RemoteException e) {
            }
        }
        this.mUserSwitchObservers.finishBroadcast();
        Slog.i(TAG, "_StartUser dispatchUserSwitchComplete userid:" + userId + " cost " + (SystemClock.elapsedRealtime() - startedTime) + " ms");
    }

    void dispatchLockedBootComplete(int userId) {
        int observerCount = this.mUserSwitchObservers.beginBroadcast();
        for (int i = 0; i < observerCount; i++) {
            try {
                ((IUserSwitchObserver) this.mUserSwitchObservers.getBroadcastItem(i)).onLockedBootComplete(userId);
            } catch (RemoteException e) {
            }
        }
        this.mUserSwitchObservers.finishBroadcast();
    }

    private void stopBackgroundUsersIfEnforced(int oldUserId) {
        if (oldUserId != 0 && hasUserRestriction("no_run_in_background", oldUserId)) {
            synchronized (this.mLock) {
                if (ActivityManagerDebugConfig.DEBUG_MU) {
                    Slog.i(TAG, "stopBackgroundUsersIfEnforced stopping " + oldUserId + " and related users");
                }
                stopUsersLocked(oldUserId, false, null);
            }
        }
    }

    void timeoutUserSwitch(UserState uss, int oldUserId, int newUserId) {
        synchronized (this.mLock) {
            Slog.wtf(TAG, "User switch timeout: from " + oldUserId + " to " + newUserId);
            sendContinueUserSwitchLocked(uss, oldUserId, newUserId);
        }
    }

    void dispatchUserSwitch(UserState uss, int oldUserId, int newUserId) {
        Slog.d(TAG, "Dispatch onUserSwitching oldUser #" + oldUserId + " newUser #" + newUserId);
        final int observerCount = this.mUserSwitchObservers.beginBroadcast();
        if (observerCount > 0) {
            final ArraySet<String> curWaitingUserSwitchCallbacks = new ArraySet();
            synchronized (this.mLock) {
                uss.switching = true;
                this.mCurWaitingUserSwitchCallbacks = curWaitingUserSwitchCallbacks;
            }
            final AtomicInteger waitingCallbacksCount = new AtomicInteger(observerCount);
            final long dispatchStartedTime = SystemClock.elapsedRealtime();
            for (int i = 0; i < observerCount; i++) {
                try {
                    final String name = "#" + i + " " + this.mUserSwitchObservers.getBroadcastCookie(i);
                    synchronized (this.mLock) {
                        curWaitingUserSwitchCallbacks.add(name);
                    }
                    final int i2 = newUserId;
                    final UserState userState = uss;
                    final int i3 = oldUserId;
                    ((IUserSwitchObserver) this.mUserSwitchObservers.getBroadcastItem(i)).onUserSwitching(newUserId, new IRemoteCallback.Stub() {
                        /* JADX WARNING: Missing block: B:15:0x00dd, code:
            return;
     */
                        /* Code decompiled incorrectly, please refer to instructions dump. */
                        public void sendResult(Bundle data) throws RemoteException {
                            synchronized (UserController.this.mLock) {
                                long delay = SystemClock.elapsedRealtime() - dispatchStartedTime;
                                if (delay > 3000) {
                                    Slog.wtf(UserController.TAG, "User switch timeout: observer " + name + " sent result after " + delay + " ms");
                                }
                                Slog.d(UserController.TAG, "_StartUser User switch done: observer " + name + " sent result after " + delay + " ms" + ", total:" + observerCount);
                                if (curWaitingUserSwitchCallbacks != UserController.this.mCurWaitingUserSwitchCallbacks) {
                                    return;
                                }
                                curWaitingUserSwitchCallbacks.remove(name);
                                if (waitingCallbacksCount.decrementAndGet() == 0) {
                                    Slog.i(UserController.TAG, "_StartUser dispatchUserSwitch userid:" + i2 + " cost " + (SystemClock.elapsedRealtime() - dispatchStartedTime) + " ms");
                                    UserController.this.sendContinueUserSwitchLocked(userState, i3, i2);
                                }
                            }
                        }
                    });
                } catch (RemoteException e) {
                }
            }
        } else {
            synchronized (this.mLock) {
                sendContinueUserSwitchLocked(uss, oldUserId, newUserId);
            }
        }
        this.mUserSwitchObservers.finishBroadcast();
    }

    void sendContinueUserSwitchLocked(UserState uss, int oldUserId, int newUserId) {
        this.mCurWaitingUserSwitchCallbacks = null;
        this.mHandler.removeMessages(36);
        this.mHandler.sendMessage(this.mHandler.obtainMessage(35, oldUserId, newUserId, uss));
    }

    void continueUserSwitch(UserState uss, int oldUserId, int newUserId) {
        Slog.d(TAG, "Continue user switch oldUser #" + oldUserId + ", newUser #" + newUserId);
        if (this.mUserSwitchUiEnabled) {
            synchronized (this.mLock) {
                this.mInjector.getWindowManager().stopFreezingScreen();
            }
        }
        uss.switching = false;
        this.mHandler.removeMessages(55);
        this.mHandler.sendMessage(this.mHandler.obtainMessage(55, newUserId, 0));
        stopGuestOrEphemeralUserIfBackground();
        stopBackgroundUsersIfEnforced(oldUserId);
    }

    void moveUserToForegroundLocked(UserState uss, int oldUserId, int newUserId) {
        boolean homeInFront = this.mInjector.getActivityStackSupervisor().switchUserLocked(newUserId, uss);
        HwThemeManager.updateConfiguration(true);
        ContentResolver cr = this.mInjector.getContext().getContentResolver();
        Configuration config = new Configuration();
        HwThemeManager.retrieveSimpleUIConfig(cr, config, newUserId);
        config.fontScale = System.getFloatForUser(cr, "font_scale", config.fontScale, newUserId);
        this.mInjector.updatePersistentConfiguration(config);
        if (homeInFront) {
            this.mInjector.startHomeActivityLocked(newUserId, "moveUserToForeground");
        } else {
            this.mInjector.getActivityStackSupervisor().resumeFocusedStackTopActivityLocked();
        }
        EventLogTags.writeAmSwitchUser(newUserId);
        sendUserSwitchBroadcastsLocked(oldUserId, newUserId);
    }

    void sendUserSwitchBroadcastsLocked(int oldUserId, int newUserId) {
        List<UserInfo> profiles;
        int count;
        int i;
        int profileUserId;
        Intent intent;
        long ident = Binder.clearCallingIdentity();
        if (oldUserId >= 0) {
            try {
                profiles = this.mInjector.getUserManager().getProfiles(oldUserId, false);
                count = profiles.size();
                for (i = 0; i < count; i++) {
                    profileUserId = ((UserInfo) profiles.get(i)).id;
                    intent = new Intent("android.intent.action.USER_BACKGROUND");
                    intent.addFlags(1342177280);
                    intent.putExtra("android.intent.extra.user_handle", profileUserId);
                    this.mInjector.broadcastIntentLocked(intent, null, null, 0, null, null, null, -1, null, false, false, ActivityManagerService.MY_PID, 1000, profileUserId);
                }
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(ident);
            }
        }
        if (newUserId >= 0) {
            profiles = this.mInjector.getUserManager().getProfiles(newUserId, false);
            count = profiles.size();
            for (i = 0; i < count; i++) {
                profileUserId = ((UserInfo) profiles.get(i)).id;
                intent = new Intent("android.intent.action.USER_FOREGROUND");
                intent.addFlags(1342177280);
                intent.putExtra("android.intent.extra.user_handle", profileUserId);
                this.mInjector.broadcastIntentLocked(intent, null, null, 0, null, null, null, -1, null, false, false, ActivityManagerService.MY_PID, 1000, profileUserId);
            }
            intent = new Intent("android.intent.action.USER_SWITCHED");
            intent.addFlags(1342177280);
            intent.putExtra("android.intent.extra.user_handle", newUserId);
            Intent intent2 = intent;
            this.mInjector.broadcastIntentLocked(intent2, null, null, 0, null, null, new String[]{"android.permission.MANAGE_USERS"}, -1, null, false, false, ActivityManagerService.MY_PID, 1000, -1);
        }
        Binder.restoreCallingIdentity(ident);
    }

    int handleIncomingUser(int callingPid, int callingUid, int userId, boolean allowAll, int allowMode, String name, String callerPackage) {
        int callingUserId = UserHandle.getUserId(callingUid);
        if (callingUserId == userId || this.mInjector.getUserManagerInternal().isSameGroupForClone(callingUserId, userId)) {
            return userId;
        }
        int targetUserId = unsafeConvertIncomingUserLocked(userId);
        if (!(callingUid == 0 || callingUid == 1000)) {
            boolean allow;
            if (this.mInjector.checkComponentPermission("android.permission.INTERACT_ACROSS_USERS_FULL", callingPid, callingUid, -1, true) == 0) {
                allow = true;
            } else if (allowMode == 2) {
                allow = false;
            } else if (this.mInjector.checkComponentPermission("android.permission.INTERACT_ACROSS_USERS", callingPid, callingUid, -1, true) != 0) {
                allow = false;
            } else if (allowMode == 0) {
                allow = true;
            } else if (allowMode == 1) {
                allow = isSameProfileGroup(callingUserId, targetUserId);
            } else {
                throw new IllegalArgumentException("Unknown mode: " + allowMode);
            }
            if (!allow) {
                if (userId == -3) {
                    targetUserId = callingUserId;
                } else {
                    StringBuilder builder = new StringBuilder(128);
                    builder.append("Permission Denial: ");
                    builder.append(name);
                    if (callerPackage != null) {
                        builder.append(" from ");
                        builder.append(callerPackage);
                    }
                    builder.append(" asks to run as user ");
                    builder.append(userId);
                    builder.append(" but is calling from user ");
                    builder.append(UserHandle.getUserId(callingUid));
                    builder.append("; this requires ");
                    builder.append("android.permission.INTERACT_ACROSS_USERS_FULL");
                    if (allowMode != 2) {
                        builder.append(" or ");
                        builder.append("android.permission.INTERACT_ACROSS_USERS");
                    }
                    String msg = builder.toString();
                    Slog.w(TAG, msg);
                    throw new SecurityException(msg);
                }
            }
        }
        if (!allowAll && targetUserId < 0) {
            throw new IllegalArgumentException("Call does not support special user #" + targetUserId);
        } else if (callingUid != IHwShutdownThread.SHUTDOWN_ANIMATION_WAIT_TIME || targetUserId < 0 || !hasUserRestriction("no_debugging_features", targetUserId)) {
            return targetUserId;
        } else {
            throw new SecurityException("Shell does not have permission to access user " + targetUserId + "\n " + Debug.getCallers(3));
        }
    }

    int unsafeConvertIncomingUserLocked(int userId) {
        if (userId == -2 || userId == -3) {
            return getCurrentUserIdLocked();
        }
        return userId;
    }

    void registerUserSwitchObserver(IUserSwitchObserver observer, String name) {
        Preconditions.checkNotNull(name, "Observer name cannot be null");
        if (this.mInjector.checkCallingPermission("android.permission.INTERACT_ACROSS_USERS_FULL") != 0) {
            String msg = "Permission Denial: registerUserSwitchObserver() from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + " requires " + "android.permission.INTERACT_ACROSS_USERS_FULL";
            Slog.w(TAG, msg);
            throw new SecurityException(msg);
        }
        this.mUserSwitchObservers.register(observer, name);
    }

    void unregisterUserSwitchObserver(IUserSwitchObserver observer) {
        this.mUserSwitchObservers.unregister(observer);
    }

    UserState getStartedUserStateLocked(int userId) {
        return (UserState) this.mStartedUsers.get(userId);
    }

    boolean hasStartedUserState(int userId) {
        return this.mStartedUsers.get(userId) != null;
    }

    private void updateStartedUserArrayLocked() {
        int i;
        UserState uss;
        int num = 0;
        for (i = 0; i < this.mStartedUsers.size(); i++) {
            uss = (UserState) this.mStartedUsers.valueAt(i);
            if (!(uss.state == 4 || uss.state == 5)) {
                num++;
            }
        }
        this.mStartedUserArray = new int[num];
        num = 0;
        for (i = 0; i < this.mStartedUsers.size(); i++) {
            uss = (UserState) this.mStartedUsers.valueAt(i);
            if (!(uss.state == 4 || uss.state == 5)) {
                int num2 = num + 1;
                this.mStartedUserArray[num] = this.mStartedUsers.keyAt(i);
                num = num2;
            }
        }
    }

    void sendBootCompletedLocked(IIntentReceiver resultTo) {
        for (int i = 0; i < this.mStartedUsers.size(); i++) {
            finishUserBoot((UserState) this.mStartedUsers.valueAt(i), resultTo);
        }
    }

    void onSystemReady() {
        updateCurrentProfileIdsLocked();
    }

    private void updateCurrentProfileIdsLocked() {
        int i;
        List<UserInfo> profiles = this.mInjector.getUserManager().getProfiles(this.mCurrentUserId, false);
        int[] currentProfileIds = new int[profiles.size()];
        for (i = 0; i < currentProfileIds.length; i++) {
            currentProfileIds[i] = ((UserInfo) profiles.get(i)).id;
        }
        this.mCurrentProfileIds = currentProfileIds;
        synchronized (this.mUserProfileGroupIdsSelfLocked) {
            this.mUserProfileGroupIdsSelfLocked.clear();
            List<UserInfo> users = this.mInjector.getUserManager().getUsers(false);
            for (i = 0; i < users.size(); i++) {
                UserInfo user = (UserInfo) users.get(i);
                if (user.profileGroupId != -10000) {
                    this.mUserProfileGroupIdsSelfLocked.put(user.id, user.profileGroupId);
                }
            }
        }
    }

    int[] getStartedUserArrayLocked() {
        return this.mStartedUserArray;
    }

    boolean isUserStoppingOrShuttingDownLocked(int userId) {
        boolean z = true;
        UserState state = getStartedUserStateLocked(userId);
        if (state == null) {
            return false;
        }
        if (!(state.state == 4 || state.state == 5)) {
            z = false;
        }
        return z;
    }

    boolean isUserRunningLocked(int userId, int flags) {
        UserState state = getStartedUserStateLocked(userId);
        if (state == null) {
            return false;
        }
        if ((flags & 1) != 0) {
            return true;
        }
        if ((flags & 2) != 0) {
            switch (state.state) {
                case 0:
                case 1:
                    return true;
                default:
                    return false;
            }
        } else if ((flags & 8) != 0) {
            switch (state.state) {
                case 2:
                case 3:
                    return true;
                default:
                    return false;
            }
        } else if ((flags & 4) == 0) {
            return true;
        } else {
            switch (state.state) {
                case 3:
                    return true;
                default:
                    return false;
            }
        }
    }

    UserInfo getCurrentUser() {
        if (this.mInjector.checkCallingPermission("android.permission.INTERACT_ACROSS_USERS") == 0 || this.mInjector.checkCallingPermission("android.permission.INTERACT_ACROSS_USERS_FULL") == 0) {
            UserInfo currentUserLocked;
            synchronized (this.mLock) {
                currentUserLocked = getCurrentUserLocked();
            }
            return currentUserLocked;
        }
        String msg = "Permission Denial: getCurrentUser() from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + " requires " + "android.permission.INTERACT_ACROSS_USERS";
        Slog.w(TAG, msg);
        throw new SecurityException(msg);
    }

    UserInfo getCurrentUserLocked() {
        return getUserInfo(this.mTargetUserId != -10000 ? this.mTargetUserId : this.mCurrentUserId);
    }

    int getCurrentOrTargetUserIdLocked() {
        return this.mTargetUserId != -10000 ? this.mTargetUserId : this.mCurrentUserId;
    }

    int getCurrentUserIdLocked() {
        return this.mCurrentUserId;
    }

    private boolean isCurrentUserLocked(int userId) {
        return userId == getCurrentOrTargetUserIdLocked();
    }

    int setTargetUserIdLocked(int targetUserId) {
        this.mTargetUserId = targetUserId;
        return targetUserId;
    }

    int[] getUsers() {
        UserManagerService ums = this.mInjector.getUserManager();
        if (ums != null) {
            return ums.getUserIds();
        }
        return new int[]{0};
    }

    UserInfo getUserInfo(int userId) {
        return this.mInjector.getUserManager().getUserInfo(userId);
    }

    int[] getUserIds() {
        return this.mInjector.getUserManager().getUserIds();
    }

    boolean exists(int userId) {
        return this.mInjector.getUserManager().exists(userId);
    }

    boolean hasUserRestriction(String restriction, int userId) {
        return this.mInjector.getUserManager().hasUserRestriction(restriction, userId);
    }

    Set<Integer> getProfileIds(int userId) {
        Set<Integer> userIds = new HashSet();
        for (UserInfo user : this.mInjector.getUserManager().getProfiles(userId, false)) {
            userIds.add(Integer.valueOf(user.id));
        }
        return userIds;
    }

    boolean isSameProfileGroup(int callingUserId, int targetUserId) {
        boolean z = true;
        if (callingUserId == targetUserId) {
            return true;
        }
        synchronized (this.mUserProfileGroupIdsSelfLocked) {
            int callingProfile = this.mUserProfileGroupIdsSelfLocked.get(callingUserId, -10000);
            int targetProfile = this.mUserProfileGroupIdsSelfLocked.get(targetUserId, -10000);
            if (callingProfile == -10000) {
                z = false;
            } else if (callingProfile != targetProfile) {
                z = false;
            }
        }
        return z;
    }

    boolean isCurrentProfileLocked(int userId) {
        return ArrayUtils.contains(this.mCurrentProfileIds, userId);
    }

    int[] getCurrentProfileIdsLocked() {
        return this.mCurrentProfileIds;
    }

    /* JADX WARNING: Missing block: B:9:0x0015, code:
            if (r4.mLockPatternUtils.isSeparateProfileChallengeEnabled(r5) != false) goto L_0x001b;
     */
    /* JADX WARNING: Missing block: B:10:0x0017, code:
            return false;
     */
    /* JADX WARNING: Missing block: B:14:0x001b, code:
            r0 = r4.mInjector.getKeyguardManager();
     */
    /* JADX WARNING: Missing block: B:15:0x0025, code:
            if (r0.isDeviceLocked(r5) == false) goto L_0x002b;
     */
    /* JADX WARNING: Missing block: B:16:0x0027, code:
            r1 = r0.isDeviceSecure(r5);
     */
    /* JADX WARNING: Missing block: B:17:0x002b, code:
            return r1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    boolean shouldConfirmCredentials(int userId) {
        boolean z = false;
        synchronized (this.mLock) {
            if (this.mStartedUsers.get(userId) == null) {
                return false;
            }
        }
    }

    boolean isLockScreenDisabled(int userId) {
        return this.mLockPatternUtils.isLockScreenDisabled(userId);
    }

    void dump(PrintWriter pw, boolean dumpAll) {
        int i;
        pw.println("  mStartedUsers:");
        for (i = 0; i < this.mStartedUsers.size(); i++) {
            UserState uss = (UserState) this.mStartedUsers.valueAt(i);
            pw.print("    User #");
            pw.print(uss.mHandle.getIdentifier());
            pw.print(": ");
            uss.dump("", pw);
        }
        pw.print("  mStartedUserArray: [");
        for (i = 0; i < this.mStartedUserArray.length; i++) {
            if (i > 0) {
                pw.print(", ");
            }
            pw.print(this.mStartedUserArray[i]);
        }
        pw.println("]");
        pw.print("  mUserLru: [");
        for (i = 0; i < this.mUserLru.size(); i++) {
            if (i > 0) {
                pw.print(", ");
            }
            pw.print(this.mUserLru.get(i));
        }
        pw.println("]");
        if (dumpAll) {
            pw.print("  mStartedUserArray: ");
            pw.println(Arrays.toString(this.mStartedUserArray));
        }
        synchronized (this.mUserProfileGroupIdsSelfLocked) {
            if (this.mUserProfileGroupIdsSelfLocked.size() > 0) {
                pw.println("  mUserProfileGroupIds:");
                for (i = 0; i < this.mUserProfileGroupIdsSelfLocked.size(); i++) {
                    pw.print("    User #");
                    pw.print(this.mUserProfileGroupIdsSelfLocked.keyAt(i));
                    pw.print(" -> profile #");
                    pw.println(this.mUserProfileGroupIdsSelfLocked.valueAt(i));
                }
            }
        }
    }
}
