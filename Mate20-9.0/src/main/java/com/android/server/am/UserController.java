package com.android.server.am;

import android.app.AppGlobals;
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
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.IProgressListener;
import android.os.IRemoteCallback;
import android.os.IUserManager;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManagerInternal;
import android.os.storage.IStorageManager;
import android.os.storage.StorageManager;
import android.provider.Settings;
import android.util.ArraySet;
import android.util.Flog;
import android.util.IntArray;
import android.util.Pair;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.util.TimingsTraceLog;
import android.util.proto.ProtoOutputStream;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.Preconditions;
import com.android.internal.widget.LockPatternUtils;
import com.android.server.FgThread;
import com.android.server.HwServiceFactory;
import com.android.server.LocalServices;
import com.android.server.SystemServiceManager;
import com.android.server.pm.UserManagerService;
import com.android.server.wm.WindowManagerService;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

class UserController implements Handler.Callback {
    static final int CONTINUE_USER_SWITCH_MSG = 20;
    static final int FOREGROUND_PROFILE_CHANGED_MSG = 70;
    static final int REPORT_LOCKED_BOOT_COMPLETE_MSG = 110;
    static final int REPORT_USER_SWITCH_COMPLETE_MSG = 80;
    static final int REPORT_USER_SWITCH_MSG = 10;
    private static final int SCREEN_STATE_FLAG_PASSWORD = 2;
    static final int START_PROFILES_MSG = 40;
    static final int START_USER_SWITCH_FG_MSG = 120;
    static final int START_USER_SWITCH_UI_MSG = 1000;
    static final int SYSTEM_USER_CURRENT_MSG = 60;
    static final int SYSTEM_USER_START_MSG = 50;
    static final int SYSTEM_USER_UNLOCK_MSG = 100;
    private static final String TAG = "ActivityManager";
    private static final int UNLOCK_TYPE_VALUE = 1;
    private static final int USER_SWITCH_CALLBACKS_TIMEOUT_MS = 5000;
    static final int USER_SWITCH_CALLBACKS_TIMEOUT_MSG = 90;
    static final int USER_SWITCH_TIMEOUT_MS = 3000;
    static final int USER_SWITCH_TIMEOUT_MSG = 30;
    long SwitchUser_Time;
    boolean isColdStart;
    /* access modifiers changed from: private */
    @GuardedBy("mLock")
    public volatile ArraySet<String> mCurWaitingUserSwitchCallbacks;
    @GuardedBy("mLock")
    private int[] mCurrentProfileIds;
    @GuardedBy("mLock")
    private volatile int mCurrentUserId;
    /* access modifiers changed from: private */
    public final Handler mHandler;
    boolean mHaveTryCloneProUserUnlock;
    final Injector mInjector;
    private boolean mIsSupportISec;
    /* access modifiers changed from: private */
    public final Object mLock;
    private final LockPatternUtils mLockPatternUtils;
    int mMaxRunningUsers;
    @GuardedBy("mLock")
    private int[] mStartedUserArray;
    @GuardedBy("mLock")
    private final SparseArray<UserState> mStartedUsers;
    @GuardedBy("mLock")
    private String mSwitchingFromSystemUserMessage;
    @GuardedBy("mLock")
    private String mSwitchingToSystemUserMessage;
    @GuardedBy("mLock")
    private volatile int mTargetUserId;
    @GuardedBy("mLock")
    private ArraySet<String> mTimeoutUserSwitchCallbacks;
    private final Handler mUiHandler;
    @GuardedBy("mLock")
    private final ArrayList<Integer> mUserLru;
    @GuardedBy("mLock")
    private final SparseIntArray mUserProfileGroupIds;
    private final RemoteCallbackList<IUserSwitchObserver> mUserSwitchObservers;
    boolean mUserSwitchUiEnabled;
    boolean misHiddenSpaceSwitch;

    @VisibleForTesting
    static class Injector {
        final ActivityManagerService mService;
        private UserManagerService mUserManager;
        private UserManagerInternal mUserManagerInternal;

        Injector(ActivityManagerService service) {
            this.mService = service;
        }

        /* access modifiers changed from: protected */
        public Handler getHandler(Handler.Callback callback) {
            return new Handler(this.mService.mHandlerThread.getLooper(), callback);
        }

        /* access modifiers changed from: protected */
        public Handler getUiHandler(Handler.Callback callback) {
            return new Handler(this.mService.mUiHandler.getLooper(), callback);
        }

        /* access modifiers changed from: protected */
        public Context getContext() {
            return this.mService.mContext;
        }

        /* access modifiers changed from: protected */
        public LockPatternUtils getLockPatternUtils() {
            return new LockPatternUtils(getContext());
        }

        /* access modifiers changed from: protected */
        public int broadcastIntent(Intent intent, String resolvedType, IIntentReceiver resultTo, int resultCode, String resultData, Bundle resultExtras, String[] requiredPermissions, int appOp, Bundle bOptions, boolean ordered, boolean sticky, int callingPid, int callingUid, int userId) {
            int broadcastIntentLocked;
            synchronized (this.mService) {
                try {
                    ActivityManagerService.boostPriorityForLockedSection();
                    broadcastIntentLocked = this.mService.broadcastIntentLocked(null, null, intent, resolvedType, resultTo, resultCode, resultData, resultExtras, requiredPermissions, appOp, bOptions, ordered, sticky, callingPid, callingUid, userId);
                } catch (Throwable th) {
                    while (true) {
                        ActivityManagerService.resetPriorityAfterLockedSection();
                        throw th;
                    }
                }
            }
            ActivityManagerService.resetPriorityAfterLockedSection();
            return broadcastIntentLocked;
        }

        /* access modifiers changed from: package-private */
        public int checkCallingPermission(String permission) {
            return this.mService.checkCallingPermission(permission);
        }

        /* access modifiers changed from: package-private */
        public WindowManagerService getWindowManager() {
            return this.mService.mWindowManager;
        }

        /* access modifiers changed from: package-private */
        public void activityManagerOnUserStopped(int userId) {
            synchronized (this.mService) {
                try {
                    ActivityManagerService.boostPriorityForLockedSection();
                    this.mService.onUserStoppedLocked(userId);
                } catch (Throwable th) {
                    while (true) {
                        ActivityManagerService.resetPriorityAfterLockedSection();
                        throw th;
                    }
                }
            }
            ActivityManagerService.resetPriorityAfterLockedSection();
        }

        /* access modifiers changed from: package-private */
        public void systemServiceManagerCleanupUser(int userId) {
            this.mService.mSystemServiceManager.cleanupUser(userId);
        }

        /* access modifiers changed from: protected */
        public UserManagerService getUserManager() {
            if (this.mUserManager == null) {
                this.mUserManager = IUserManager.Stub.asInterface(ServiceManager.getService("user"));
            }
            return this.mUserManager;
        }

        /* access modifiers changed from: package-private */
        public UserManagerInternal getUserManagerInternal() {
            if (this.mUserManagerInternal == null) {
                this.mUserManagerInternal = (UserManagerInternal) LocalServices.getService(UserManagerInternal.class);
            }
            return this.mUserManagerInternal;
        }

        /* access modifiers changed from: package-private */
        public KeyguardManager getKeyguardManager() {
            return (KeyguardManager) this.mService.mContext.getSystemService(KeyguardManager.class);
        }

        /* access modifiers changed from: package-private */
        public void batteryStatsServiceNoteEvent(int code, String name, int uid) {
            this.mService.mBatteryStatsService.noteEvent(code, name, uid);
        }

        /* access modifiers changed from: package-private */
        public boolean isRuntimeRestarted() {
            return this.mService.mSystemServiceManager.isRuntimeRestarted();
        }

        /* access modifiers changed from: package-private */
        public SystemServiceManager getSystemServiceManager() {
            return this.mService.mSystemServiceManager;
        }

        /* access modifiers changed from: package-private */
        public boolean isFirstBootOrUpgrade() {
            IPackageManager pm = AppGlobals.getPackageManager();
            try {
                return pm.isFirstBoot() || pm.isUpgrade();
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }

        /* access modifiers changed from: package-private */
        public void sendPreBootBroadcast(int userId, boolean quiet, Runnable onFinish) {
            final Runnable runnable = onFinish;
            AnonymousClass1 r0 = new PreBootBroadcaster(this.mService, userId, null, quiet) {
                public void onFinished() {
                    runnable.run();
                }
            };
            r0.sendNext();
        }

        /* access modifiers changed from: package-private */
        public void activityManagerForceStopPackage(int userId, String reason) {
            synchronized (this.mService) {
                try {
                    ActivityManagerService.boostPriorityForLockedSection();
                    this.mService.forceStopPackageLocked(null, -1, false, false, true, false, false, userId, reason);
                } catch (Throwable th) {
                    while (true) {
                        ActivityManagerService.resetPriorityAfterLockedSection();
                        throw th;
                    }
                }
            }
            ActivityManagerService.resetPriorityAfterLockedSection();
        }

        /* access modifiers changed from: package-private */
        public int checkComponentPermission(String permission, int pid, int uid, int owningUid, boolean exported) {
            return this.mService.checkComponentPermission(permission, pid, uid, owningUid, exported);
        }

        /* access modifiers changed from: protected */
        public void startHomeActivity(int userId, String reason) {
            synchronized (this.mService) {
                try {
                    ActivityManagerService.boostPriorityForLockedSection();
                    this.mService.startHomeActivityLocked(userId, reason);
                } catch (Throwable th) {
                    while (true) {
                        ActivityManagerService.resetPriorityAfterLockedSection();
                        throw th;
                    }
                }
            }
            ActivityManagerService.resetPriorityAfterLockedSection();
        }

        /* access modifiers changed from: package-private */
        public void updateUserConfiguration() {
            synchronized (this.mService) {
                try {
                    ActivityManagerService.boostPriorityForLockedSection();
                    this.mService.updateUserConfigurationLocked();
                } catch (Throwable th) {
                    while (true) {
                        ActivityManagerService.resetPriorityAfterLockedSection();
                        throw th;
                    }
                }
            }
            ActivityManagerService.resetPriorityAfterLockedSection();
        }

        /* access modifiers changed from: package-private */
        public void clearBroadcastQueueForUser(int userId) {
            synchronized (this.mService) {
                try {
                    ActivityManagerService.boostPriorityForLockedSection();
                    this.mService.clearBroadcastQueueForUserLocked(userId);
                } catch (Throwable th) {
                    while (true) {
                        ActivityManagerService.resetPriorityAfterLockedSection();
                        throw th;
                    }
                }
            }
            ActivityManagerService.resetPriorityAfterLockedSection();
        }

        /* access modifiers changed from: package-private */
        public void loadUserRecents(int userId) {
            synchronized (this.mService) {
                try {
                    ActivityManagerService.boostPriorityForLockedSection();
                    this.mService.getRecentTasks().loadUserRecentsLocked(userId);
                } catch (Throwable th) {
                    while (true) {
                        ActivityManagerService.resetPriorityAfterLockedSection();
                        throw th;
                    }
                }
            }
            ActivityManagerService.resetPriorityAfterLockedSection();
        }

        /* access modifiers changed from: package-private */
        public void startPersistentApps(int matchFlags) {
            this.mService.startPersistentApps(matchFlags);
        }

        /* access modifiers changed from: package-private */
        public void installEncryptionUnawareProviders(int userId) {
            this.mService.installEncryptionUnawareProviders(userId);
        }

        /* JADX WARNING: type inference failed for: r0v4, types: [android.app.Dialog] */
        /* JADX WARNING: type inference failed for: r1v5, types: [com.android.server.am.CarUserSwitchingDialog] */
        /* JADX WARNING: type inference failed for: r1v6, types: [com.android.server.am.UserSwitchingDialog] */
        /* access modifiers changed from: package-private */
        /* JADX WARNING: Multi-variable type inference failed */
        public void showUserSwitchingDialog(UserInfo fromUser, UserInfo toUser, String switchingFromSystemUserMessage, String switchingToSystemUserMessage) {
            ? r0;
            if (!this.mService.mContext.getPackageManager().hasSystemFeature("android.hardware.type.automotive")) {
                UserSwitchingDialog userSwitchingDialog = new UserSwitchingDialog(this.mService, this.mService.mContext, fromUser, toUser, true, switchingFromSystemUserMessage, switchingToSystemUserMessage);
                r0 = userSwitchingDialog;
            } else {
                CarUserSwitchingDialog carUserSwitchingDialog = new CarUserSwitchingDialog(this.mService, this.mService.mContext, fromUser, toUser, true, switchingFromSystemUserMessage, switchingToSystemUserMessage);
                r0 = carUserSwitchingDialog;
            }
            r0.show();
        }

        /* access modifiers changed from: package-private */
        public void updatePersistentConfiguration(Configuration config) {
            this.mService.updatePersistentConfiguration(config);
        }

        /* access modifiers changed from: package-private */
        public void reportGlobalUsageEventLocked(int event) {
            synchronized (this.mService) {
                try {
                    ActivityManagerService.boostPriorityForLockedSection();
                    this.mService.reportGlobalUsageEventLocked(event);
                } catch (Throwable th) {
                    while (true) {
                        ActivityManagerService.resetPriorityAfterLockedSection();
                        throw th;
                    }
                }
            }
            ActivityManagerService.resetPriorityAfterLockedSection();
        }

        /* access modifiers changed from: package-private */
        public void reportCurWakefulnessUsageEvent() {
            synchronized (this.mService) {
                try {
                    ActivityManagerService.boostPriorityForLockedSection();
                    this.mService.reportCurWakefulnessUsageEventLocked();
                } catch (Throwable th) {
                    while (true) {
                        ActivityManagerService.resetPriorityAfterLockedSection();
                        throw th;
                    }
                }
            }
            ActivityManagerService.resetPriorityAfterLockedSection();
        }

        /* access modifiers changed from: package-private */
        public void stackSupervisorRemoveUser(int userId) {
            synchronized (this.mService) {
                try {
                    ActivityManagerService.boostPriorityForLockedSection();
                    this.mService.mStackSupervisor.removeUserLocked(userId);
                } catch (Throwable th) {
                    while (true) {
                        ActivityManagerService.resetPriorityAfterLockedSection();
                        throw th;
                    }
                }
            }
            ActivityManagerService.resetPriorityAfterLockedSection();
        }

        /* access modifiers changed from: protected */
        public boolean stackSupervisorSwitchUser(int userId, UserState uss) {
            boolean switchUserLocked;
            synchronized (this.mService) {
                try {
                    ActivityManagerService.boostPriorityForLockedSection();
                    switchUserLocked = this.mService.mStackSupervisor.switchUserLocked(userId, uss);
                } catch (Throwable th) {
                    while (true) {
                        ActivityManagerService.resetPriorityAfterLockedSection();
                        throw th;
                    }
                }
            }
            ActivityManagerService.resetPriorityAfterLockedSection();
            return switchUserLocked;
        }

        /* access modifiers changed from: protected */
        public void stackSupervisorResumeFocusedStackTopActivity() {
            synchronized (this.mService) {
                try {
                    ActivityManagerService.boostPriorityForLockedSection();
                    this.mService.mStackSupervisor.resumeFocusedStackTopActivityLocked();
                } catch (Throwable th) {
                    while (true) {
                        ActivityManagerService.resetPriorityAfterLockedSection();
                        throw th;
                    }
                }
            }
            ActivityManagerService.resetPriorityAfterLockedSection();
        }

        /* access modifiers changed from: protected */
        public void clearAllLockedTasks(String reason) {
            synchronized (this.mService) {
                try {
                    ActivityManagerService.boostPriorityForLockedSection();
                    this.mService.getLockTaskController().clearLockedTasks(reason);
                } catch (Throwable th) {
                    while (true) {
                        ActivityManagerService.resetPriorityAfterLockedSection();
                        throw th;
                    }
                }
            }
            ActivityManagerService.resetPriorityAfterLockedSection();
        }

        /* access modifiers changed from: protected */
        public boolean isCallerRecents(int callingUid) {
            return this.mService.getRecentTasks().isCallerRecents(callingUid);
        }

        /* access modifiers changed from: private */
        public boolean shouldSkipKeyguard(UserInfo first, UserInfo second) {
            return this.mService.mHwAMSEx.isHiddenSpaceSwitch(first, second) && getWindowManager().isKeyguardLocked();
        }

        /* access modifiers changed from: package-private */
        public void cleanAppForHiddenSpace() {
            this.mService.mHwAMSEx.cleanAppForHiddenSpace();
        }
    }

    private static class UserProgressListener extends IProgressListener.Stub {
        private volatile long mUnlockStarted;

        private UserProgressListener() {
        }

        public void onStarted(int id, Bundle extras) throws RemoteException {
            Slog.d("ActivityManager", "Started unlocking user " + id);
            this.mUnlockStarted = SystemClock.uptimeMillis();
        }

        public void onProgress(int id, int progress, Bundle extras) throws RemoteException {
            Slog.d("ActivityManager", "Unlocking user " + id + " progress " + progress);
        }

        public void onFinished(int id, Bundle extras) throws RemoteException {
            long unlockTime = SystemClock.uptimeMillis() - this.mUnlockStarted;
            if (id == 0) {
                new TimingsTraceLog("SystemServerTiming", 524288).logDuration("SystemUserUnlock", unlockTime);
                return;
            }
            Slog.d("ActivityManager", "Unlocking user " + id + " took " + unlockTime + " ms");
        }
    }

    UserController(ActivityManagerService service) {
        this(new Injector(service));
    }

    @VisibleForTesting
    UserController(Injector injector) {
        this.isColdStart = false;
        this.misHiddenSpaceSwitch = false;
        this.mHaveTryCloneProUserUnlock = false;
        this.mLock = new Object();
        this.mIsSupportISec = SystemProperties.getBoolean("ro.config.support_iudf", false);
        this.mCurrentUserId = 0;
        this.mTargetUserId = -10000;
        this.mStartedUsers = new SparseArray<>();
        this.mUserLru = new ArrayList<>();
        this.mStartedUserArray = new int[]{0};
        this.mCurrentProfileIds = new int[0];
        this.mUserProfileGroupIds = new SparseIntArray();
        this.mUserSwitchObservers = new RemoteCallbackList<>();
        this.mUserSwitchUiEnabled = true;
        this.mInjector = injector;
        this.mHandler = this.mInjector.getHandler(this);
        this.mUiHandler = this.mInjector.getUiHandler(this);
        UserState uss = new UserState(UserHandle.SYSTEM);
        uss.mUnlockProgress.addListener(new UserProgressListener());
        this.mStartedUsers.put(0, uss);
        this.mUserLru.add(0);
        this.mLockPatternUtils = this.mInjector.getLockPatternUtils();
        updateStartedUserArrayLU();
    }

    /* access modifiers changed from: package-private */
    public void finishUserSwitch(UserState uss) {
        long startedTime = SystemClock.elapsedRealtime();
        finishUserBoot(uss);
        startProfiles();
        synchronized (this.mLock) {
            stopRunningUsersLU(this.mMaxRunningUsers);
        }
        Slog.i("ActivityManager", "_StartUser finishUserSwitch userid:" + uss.mHandle.getIdentifier() + " cost " + (SystemClock.elapsedRealtime() - startedTime) + " ms");
    }

    /* access modifiers changed from: package-private */
    public List<Integer> getRunningUsersLU() {
        ArrayList<Integer> runningUsers = new ArrayList<>();
        Iterator<Integer> it = this.mUserLru.iterator();
        while (it.hasNext()) {
            Integer userId = it.next();
            UserState uss = this.mStartedUsers.get(userId.intValue());
            if (!(uss == null || uss.state == 4 || uss.state == 5)) {
                if (userId.intValue() != 0 || !UserInfo.isSystemOnly(userId.intValue())) {
                    runningUsers.add(userId);
                }
            }
        }
        return runningUsers;
    }

    /* access modifiers changed from: package-private */
    public void stopRunningUsersLU(int maxRunningUsers) {
        List<Integer> currentlyRunning = getRunningUsersLU();
        Iterator<Integer> iterator = currentlyRunning.iterator();
        while (currentlyRunning.size() > maxRunningUsers && iterator.hasNext()) {
            Integer userId = iterator.next();
            if (!(userId.intValue() == 0 || userId.intValue() == this.mCurrentUserId || stopUsersLU(userId.intValue(), false, null) != 0)) {
                iterator.remove();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean canStartMoreUsers() {
        boolean z;
        synchronized (this.mLock) {
            z = getRunningUsersLU().size() < this.mMaxRunningUsers;
        }
        return z;
    }

    private void finishUserBoot(UserState uss) {
        finishUserBoot(uss, null);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0038, code lost:
        if (r2.setState(0, 1) == false) goto L_0x00c8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x003a, code lost:
        r1.mInjector.getUserManagerInternal().setUserState(r15, r2.state);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0045, code lost:
        if (r15 != 0) goto L_0x0086;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x004d, code lost:
        if (r1.mInjector.isRuntimeRestarted() != false) goto L_0x0086;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0055, code lost:
        if (r1.mInjector.isFirstBootOrUpgrade() != false) goto L_0x0086;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0057, code lost:
        r0 = (int) (android.os.SystemClock.elapsedRealtime() / 1000);
        com.android.internal.logging.MetricsLogger.histogram(r1.mInjector.getContext(), "framework_locked_boot_completed", r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x006e, code lost:
        if (r0 <= START_USER_SWITCH_FG_MSG) goto L_0x0086;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0070, code lost:
        android.util.Slog.wtf("SystemServerTiming", "finishUserBoot took too long. uptimeSeconds=" + r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0086, code lost:
        r1.mHandler.sendMessage(r1.mHandler.obtainMessage(110, r15, 0));
        r0 = new android.content.Intent("android.intent.action.LOCKED_BOOT_COMPLETED", null);
        r0.putExtra("android.intent.extra.user_handle", r15);
        r0.addFlags(150994944);
        r18 = r15;
        r1.mInjector.broadcastIntent(r0, null, r21, 0, null, null, new java.lang.String[]{"android.permission.RECEIVE_BOOT_COMPLETED"}, -1, null, true, false, com.android.server.am.ActivityManagerService.MY_PID, 1000, r18);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x00c8, code lost:
        r18 = r15;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x00ca, code lost:
        r4 = r18;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x00d6, code lost:
        if (r1.mInjector.getUserManager().isManagedProfile(r4) != false) goto L_0x00e9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x00e2, code lost:
        if (r1.mInjector.getUserManager().isClonedProfile(r4) == false) goto L_0x00e5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x00e5, code lost:
        maybeUnlockUser(r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x00e9, code lost:
        r0 = r1.mInjector.getUserManager().getProfileParent(r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x00f3, code lost:
        if (r0 == null) goto L_0x0126;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x00fb, code lost:
        if (android.os.storage.StorageManager.isUserKeyUnlocked(r0.id) == false) goto L_0x0126;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x00fd, code lost:
        android.util.Slog.d("ActivityManager", "User " + r4 + " (parent " + r0.id + "): attempting unlock because parent is unlocked");
        maybeUnlockUser(r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x0126, code lost:
        if (r0 != null) goto L_0x012b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x0128, code lost:
        r3 = "<null>";
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x012b, code lost:
        r3 = java.lang.String.valueOf(r0.id);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x0131, code lost:
        android.util.Slog.d("ActivityManager", "User " + r4 + " (parent " + r3 + "): delaying unlock because parent is locked");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x0155, code lost:
        return;
     */
    private void finishUserBoot(UserState uss, IIntentReceiver resultTo) {
        UserState userState = uss;
        int userId = userState.mHandle.getIdentifier();
        Slog.d("ActivityManager", "Finishing user boot " + userId);
        synchronized (this.mLock) {
            try {
                if (this.mStartedUsers.get(userId) != userState) {
                    try {
                    } catch (Throwable th) {
                        th = th;
                        int i = userId;
                        while (true) {
                            try {
                                break;
                            } catch (Throwable th2) {
                                th = th2;
                            }
                        }
                        throw th;
                    }
                }
            } catch (Throwable th3) {
                th = th3;
                int i2 = userId;
                while (true) {
                    break;
                }
                throw th;
            }
        }
    }

    private void finishUserUnlocking(UserState uss) {
        int userId = uss.mHandle.getIdentifier();
        if (StorageManager.isUserKeyUnlocked(userId)) {
            synchronized (this.mLock) {
                if (this.mStartedUsers.get(userId) == uss) {
                    if (uss.state == 1) {
                        uss.mUnlockProgress.start();
                        uss.mUnlockProgress.setProgress(5, this.mInjector.getContext().getString(17039579));
                        FgThread.getHandler().post(new Runnable(userId, uss) {
                            private final /* synthetic */ int f$1;
                            private final /* synthetic */ UserState f$2;

                            {
                                this.f$1 = r2;
                                this.f$2 = r3;
                            }

                            public final void run() {
                                UserController.lambda$finishUserUnlocking$0(UserController.this, this.f$1, this.f$2);
                            }
                        });
                    }
                }
            }
        }
    }

    public static /* synthetic */ void lambda$finishUserUnlocking$0(UserController userController, int userId, UserState uss) {
        if (!StorageManager.isUserKeyUnlocked(userId)) {
            Slog.w("ActivityManager", "User key got locked unexpectedly, leaving user locked.");
            return;
        }
        userController.mInjector.getUserManager().onBeforeUnlockUser(userId);
        synchronized (userController.mLock) {
            if (uss.setState(1, 2)) {
                userController.mInjector.getUserManagerInternal().setUserState(userId, uss.state);
                uss.mUnlockProgress.setProgress(20);
                userController.mHandler.obtainMessage(100, userId, 0, uss).sendToTarget();
            }
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0033, code lost:
        r1.mInjector.getUserManagerInternal().setUserState(r15, r2.state);
        r2.mUnlockProgress.finish();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0043, code lost:
        if (r15 != 0) goto L_0x004c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0045, code lost:
        r1.mInjector.startPersistentApps(262144);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x004c, code lost:
        r1.mInjector.installEncryptionUnawareProviders(r15);
        r0 = new android.content.Intent("android.intent.action.USER_UNLOCKED");
        r0.putExtra("android.intent.extra.user_handle", r15);
        r0.addFlags(1342177280);
        r19 = r15;
        r1.mInjector.broadcastIntent(r0, null, null, 0, null, null, null, -1, null, false, false, com.android.server.am.ActivityManagerService.MY_PID, 1000, r19);
        r4 = r19;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x008b, code lost:
        if (getUserInfo(r4).isManagedProfile() == false) goto L_0x00d3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x008d, code lost:
        r3 = r1.mInjector.getUserManager().getProfileParent(r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0097, code lost:
        if (r3 == null) goto L_0x00d3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0099, code lost:
        r5 = new android.content.Intent("android.intent.action.MANAGED_PROFILE_UNLOCKED");
        r5.putExtra("android.intent.extra.USER", android.os.UserHandle.of(r4));
        r5.addFlags(1342177280);
        r1.mInjector.broadcastIntent(r5, null, null, 0, null, null, null, -1, null, false, false, com.android.server.am.ActivityManagerService.MY_PID, 1000, r3.id);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x00d3, code lost:
        r3 = getUserInfo(r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x00df, code lost:
        if (java.util.Objects.equals(r3.lastLoggedInFingerprint, android.os.Build.FINGERPRINT) != false) goto L_0x010b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x00e1, code lost:
        r6 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x00e6, code lost:
        if (r3.isManagedProfile() != false) goto L_0x00f0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x00ec, code lost:
        if (r3.isClonedProfile() == false) goto L_0x00ff;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x00f2, code lost:
        if (r2.tokenProvided == false) goto L_0x00fe;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x00fa, code lost:
        if (r1.mLockPatternUtils.isSeparateProfileChallengeEnabled(r4) != false) goto L_0x00ff;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x00fe, code lost:
        r6 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x00ff, code lost:
        r1.mInjector.sendPreBootBroadcast(r4, r6, new com.android.server.am.$$Lambda$UserController$d0zeElfogOIugnQQLWhCzumk53k(r1, r2));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x010b, code lost:
        finishUserUnlockedCompleted(r35);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x010e, code lost:
        return;
     */
    public void finishUserUnlocked(UserState uss) {
        UserState userState = uss;
        int userId = userState.mHandle.getIdentifier();
        if (StorageManager.isUserKeyUnlocked(userId)) {
            synchronized (this.mLock) {
                try {
                    if (this.mStartedUsers.get(userState.mHandle.getIdentifier()) != userState) {
                        try {
                        } catch (Throwable th) {
                            th = th;
                            int i = userId;
                            while (true) {
                                try {
                                    break;
                                } catch (Throwable th2) {
                                    th = th2;
                                }
                            }
                            throw th;
                        }
                    } else if (!userState.setState(2, 3)) {
                    }
                } catch (Throwable th3) {
                    th = th3;
                    int i2 = userId;
                    while (true) {
                        break;
                    }
                    throw th;
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0023, code lost:
        r0 = getUserInfo(r15);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0027, code lost:
        if (r0 != null) goto L_0x002a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0029, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x002e, code lost:
        if (android.os.storage.StorageManager.isUserKeyUnlocked(r15) != false) goto L_0x0031;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0030, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0031, code lost:
        r1.mInjector.getUserManager().onUserLoggedIn(r15);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x003e, code lost:
        if (r0.isInitialized() != false) goto L_0x008b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0040, code lost:
        if (r15 == 0) goto L_0x008b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0042, code lost:
        android.util.Slog.d("ActivityManager", "Initializing user #" + r15);
        r14 = new android.content.Intent("android.intent.action.USER_INITIALIZE");
        r14.addFlags(285212672);
        r19 = r14;
        r20 = r15;
        r1.mInjector.broadcastIntent(r14, null, new com.android.server.am.UserController.AnonymousClass1(r1), 0, null, null, null, -1, null, true, false, com.android.server.am.ActivityManagerService.MY_PID, 1000, r20);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x008b, code lost:
        r20 = r15;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x008d, code lost:
        r4 = new java.lang.StringBuilder();
        r4.append("Sending BOOT_COMPLETE user #");
        r15 = r20;
        r4.append(r15);
        android.util.Slog.i("ActivityManager", r4.toString());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x00a5, code lost:
        if (r15 != 0) goto L_0x00ca;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x00ad, code lost:
        if (r1.mInjector.isRuntimeRestarted() != false) goto L_0x00ca;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x00b5, code lost:
        if (r1.mInjector.isFirstBootOrUpgrade() != false) goto L_0x00ca;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x00b7, code lost:
        com.android.internal.logging.MetricsLogger.histogram(r1.mInjector.getContext(), "framework_boot_completed", (int) (android.os.SystemClock.elapsedRealtime() / 1000));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x00ca, code lost:
        r14 = new android.content.Intent("android.intent.action.BOOT_COMPLETED", null);
        r14.putExtra("android.intent.extra.user_handle", r15);
        r14.addFlags(150994944);
        r19 = r14;
        r1.mInjector.broadcastIntent(r14, null, new com.android.server.am.UserController.AnonymousClass2(r1), 0, null, null, new java.lang.String[]{"android.permission.RECEIVE_BOOT_COMPLETED"}, -1, null, true, false, com.android.server.am.ActivityManagerService.MY_PID, 1000, r15);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x0106, code lost:
        return;
     */
    public void finishUserUnlockedCompleted(UserState uss) {
        UserState userState = uss;
        int userId = userState.mHandle.getIdentifier();
        synchronized (this.mLock) {
            try {
                if (this.mStartedUsers.get(userState.mHandle.getIdentifier()) != userState) {
                    try {
                    } catch (Throwable th) {
                        userInfo = th;
                        int i = userId;
                        while (true) {
                            try {
                                break;
                            } catch (Throwable th2) {
                                userInfo = th2;
                            }
                        }
                        throw userInfo;
                    }
                }
            } catch (Throwable th3) {
                userInfo = th3;
                int i2 = userId;
                while (true) {
                    break;
                }
                throw userInfo;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public int restartUser(int userId, final boolean foreground) {
        return stopUser(userId, true, new IStopUserCallback.Stub() {
            public void userStopped(int userId) {
                UserController.this.mHandler.post(new Runnable(userId, foreground) {
                    private final /* synthetic */ int f$1;
                    private final /* synthetic */ boolean f$2;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                    }

                    public final void run() {
                        UserController.this.startUser(this.f$1, this.f$2);
                    }
                });
            }

            public void userStopAborted(int userId) {
            }
        });
    }

    /* access modifiers changed from: package-private */
    public int stopUser(int userId, boolean force, IStopUserCallback callback) {
        int stopUsersLU;
        if (this.mInjector.checkCallingPermission("android.permission.INTERACT_ACROSS_USERS_FULL") != 0) {
            String msg = "Permission Denial: switchUser() from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + " requires " + "android.permission.INTERACT_ACROSS_USERS_FULL";
            Slog.w("ActivityManager", msg);
            throw new SecurityException(msg);
        } else if (userId < 0 || userId == 0) {
            throw new IllegalArgumentException("Can't stop system user " + userId);
        } else {
            enforceShellRestriction("no_debugging_features", userId);
            synchronized (this.mLock) {
                stopUsersLU = stopUsersLU(userId, force, callback);
            }
            return stopUsersLU;
        }
    }

    private int stopUsersLU(int userId, boolean force, IStopUserCallback callback) {
        if (userId == 0) {
            return -3;
        }
        if (isCurrentUserLU(userId)) {
            return -2;
        }
        int[] usersToStop = getUsersToStopLU(userId);
        for (int relatedUserId : usersToStop) {
            if (relatedUserId == 0 || isCurrentUserLU(relatedUserId)) {
                if (ActivityManagerDebugConfig.DEBUG_MU) {
                    Slog.i("ActivityManager", "stopUsersLocked cannot stop related user " + relatedUserId);
                }
                if (!force) {
                    return -4;
                }
                Slog.i("ActivityManager", "Force stop user " + userId + ". Related users will not be stopped");
                stopSingleUserLU(userId, callback);
                return 0;
            }
        }
        if (ActivityManagerDebugConfig.DEBUG_MU != 0) {
            Slog.i("ActivityManager", "stopUsersLocked usersToStop=" + Arrays.toString(usersToStop));
        }
        int length = usersToStop.length;
        for (int i = 0; i < length; i++) {
            int userIdToStop = usersToStop[i];
            stopSingleUserLU(userIdToStop, userIdToStop == userId ? callback : null);
        }
        return 0;
    }

    private void stopSingleUserLU(int userId, IStopUserCallback callback) {
        if (ActivityManagerDebugConfig.DEBUG_MU) {
            Slog.i("ActivityManager", "stopSingleUserLocked userId=" + userId);
        }
        UserState uss = this.mStartedUsers.get(userId);
        if (uss == null) {
            if (callback != null) {
                this.mHandler.post(new Runnable(callback, userId) {
                    private final /* synthetic */ IStopUserCallback f$0;
                    private final /* synthetic */ int f$1;

                    {
                        this.f$0 = r1;
                        this.f$1 = r2;
                    }

                    public final void run() {
                        UserController.lambda$stopSingleUserLU$2(this.f$0, this.f$1);
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
            updateStartedUserArrayLU();
            this.mHandler.post(new Runnable(userId, uss) {
                private final /* synthetic */ int f$1;
                private final /* synthetic */ UserState f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                public final void run() {
                    UserController.lambda$stopSingleUserLU$3(UserController.this, this.f$1, this.f$2);
                }
            });
        }
    }

    static /* synthetic */ void lambda$stopSingleUserLU$2(IStopUserCallback callback, int userId) {
        try {
            callback.userStopped(userId);
        } catch (RemoteException e) {
        }
    }

    public static /* synthetic */ void lambda$stopSingleUserLU$3(UserController userController, int userId, UserState uss) {
        UserController userController2 = userController;
        final int i = userId;
        Intent stoppingIntent = new Intent("android.intent.action.USER_STOPPING");
        stoppingIntent.addFlags(1073741824);
        stoppingIntent.putExtra("android.intent.extra.user_handle", i);
        stoppingIntent.putExtra("android.intent.extra.SHUTDOWN_USERSPACE_ONLY", true);
        final UserState userState = uss;
        IIntentReceiver stoppingReceiver = new IIntentReceiver.Stub() {
            public void performReceive(Intent intent, int resultCode, String data, Bundle extras, boolean ordered, boolean sticky, int sendingUser) {
                UserController.this.mHandler.post(new Runnable(i, userState) {
                    private final /* synthetic */ int f$1;
                    private final /* synthetic */ UserState f$2;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                    }

                    public final void run() {
                        UserController.this.finishUserStopping(this.f$1, this.f$2);
                    }
                });
            }
        };
        userController2.mInjector.clearBroadcastQueueForUser(i);
        userController2.mInjector.broadcastIntent(stoppingIntent, null, stoppingReceiver, 0, null, null, new String[]{"android.permission.INTERACT_ACROSS_USERS"}, -1, null, true, false, ActivityManagerService.MY_PID, 1000, -1);
    }

    /* access modifiers changed from: package-private */
    public void finishUserStopping(int userId, UserState uss) {
        int i = userId;
        final UserState userState = uss;
        Intent shutdownIntent = new Intent("android.intent.action.ACTION_SHUTDOWN");
        shutdownIntent.putExtra("android.intent.extra.SHUTDOWN_USERSPACE_ONLY", true);
        IIntentReceiver shutdownReceiver = new IIntentReceiver.Stub() {
            public void performReceive(Intent intent, int resultCode, String data, Bundle extras, boolean ordered, boolean sticky, int sendingUser) {
                UserController.this.mHandler.post(new Runnable() {
                    public void run() {
                        UserController.this.finishUserStopped(userState);
                    }
                });
            }
        };
        synchronized (this.mLock) {
            try {
                if (userState.state != 4) {
                    try {
                    } catch (Throwable th) {
                        th = th;
                        Intent intent = shutdownIntent;
                        while (true) {
                            try {
                                break;
                            } catch (Throwable th2) {
                                th = th2;
                            }
                        }
                        throw th;
                    }
                } else {
                    userState.setState(5);
                    this.mInjector.getUserManagerInternal().setUserState(i, userState.state);
                    this.mInjector.batteryStatsServiceNoteEvent(16391, Integer.toString(userId), i);
                    this.mInjector.getSystemServiceManager().stopUser(i);
                    Intent intent2 = shutdownIntent;
                    this.mInjector.broadcastIntent(shutdownIntent, null, shutdownReceiver, 0, null, null, null, -1, null, true, false, ActivityManagerService.MY_PID, 1000, userId);
                }
            } catch (Throwable th3) {
                th = th3;
                Intent intent3 = shutdownIntent;
                while (true) {
                    break;
                }
                throw th;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void finishUserStopped(UserState uss) {
        ArrayList<IStopUserCallback> callbacks;
        boolean stopped;
        int userId = uss.mHandle.getIdentifier();
        synchronized (this.mLock) {
            callbacks = new ArrayList<>(uss.mStopCallbacks);
            if (this.mStartedUsers.get(userId) == uss) {
                if (uss.state == 5) {
                    stopped = true;
                    this.mStartedUsers.remove(userId);
                    this.mUserLru.remove(Integer.valueOf(userId));
                    updateStartedUserArrayLU();
                }
            }
            stopped = false;
        }
        boolean stopped2 = stopped;
        if (stopped2) {
            this.mInjector.getUserManagerInternal().removeUserState(userId);
            this.mInjector.activityManagerOnUserStopped(userId);
            forceStopUser(userId, "finish user");
        }
        for (int i = 0; i < callbacks.size(); i++) {
            if (stopped2) {
                try {
                    callbacks.get(i).userStopped(userId);
                } catch (RemoteException e) {
                }
            } else {
                callbacks.get(i).userStopAborted(userId);
            }
        }
        if (stopped2) {
            this.mInjector.systemServiceManagerCleanupUser(userId);
            this.mInjector.stackSupervisorRemoveUser(userId);
            if (getUserInfo(userId).isEphemeral()) {
                this.mInjector.getUserManager().removeUserEvenWhenDisallowed(userId);
            }
            FgThread.getHandler().post(new Runnable(userId) {
                private final /* synthetic */ int f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    UserController.lambda$finishUserStopped$4(UserController.this, this.f$1);
                }
            });
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0019, code lost:
        if (r0 == null) goto L_0x0023;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x001f, code lost:
        if (r0.isHwHiddenSpace() == false) goto L_0x0023;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0021, code lost:
        r1 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0023, code lost:
        r1 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0024, code lost:
        if (r1 != false) goto L_0x0040;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0028, code lost:
        if (r4.mIsSupportISec != false) goto L_0x0032;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x002a, code lost:
        r4.getStorageManager().lockUserKey(r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0032, code lost:
        r4.getStorageManager().lockUserKeyISec(r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x003a, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x003f, code lost:
        throw r2.rethrowAsRuntimeException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0040, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0015, code lost:
        r0 = r4.getUserInfo(r5);
     */
    public static /* synthetic */ void lambda$finishUserStopped$4(UserController userController, int userId) {
        synchronized (userController.mLock) {
            if (userController.mStartedUsers.get(userId) != null) {
                Slog.w("ActivityManager", "User was restarted, skipping key eviction");
            }
        }
    }

    private int[] getUsersToStopLU(int userId) {
        int startedUsersSize = this.mStartedUsers.size();
        IntArray userIds = new IntArray();
        userIds.add(userId);
        int userGroupId = this.mUserProfileGroupIds.get(userId, -10000);
        for (int i = 0; i < startedUsersSize; i++) {
            int startedUserId = this.mStartedUsers.valueAt(i).mHandle.getIdentifier();
            boolean sameUserId = true;
            boolean sameGroup = userGroupId != -10000 && userGroupId == this.mUserProfileGroupIds.get(startedUserId, -10000);
            if (startedUserId != userId) {
                sameUserId = false;
            }
            if (sameGroup && !sameUserId) {
                userIds.add(startedUserId);
            }
        }
        return userIds.toArray();
    }

    private void forceStopUser(int userId, String reason) {
        int i = userId;
        this.mInjector.activityManagerForceStopPackage(i, reason);
        Intent intent = new Intent("android.intent.action.USER_STOPPED");
        intent.addFlags(1342177280);
        intent.putExtra("android.intent.extra.user_handle", i);
        this.mInjector.broadcastIntent(intent, null, null, 0, null, null, null, -1, null, false, false, ActivityManagerService.MY_PID, 1000, -1);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0039, code lost:
        r1 = getUserInfo(r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0041, code lost:
        if (r1.isEphemeral() == false) goto L_0x004e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0043, code lost:
        ((android.os.UserManagerInternal) com.android.server.LocalServices.getService(android.os.UserManagerInternal.class)).onEphemeralUserStop(r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0052, code lost:
        if (r1.isGuest() != false) goto L_0x005a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0058, code lost:
        if (r1.isEphemeral() == false) goto L_0x0063;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x005a, code lost:
        r2 = r4.mLock;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x005c, code lost:
        monitor-enter(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:?, code lost:
        stopUsersLU(r5, true, null);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0062, code lost:
        monitor-exit(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0063, code lost:
        return;
     */
    private void stopGuestOrEphemeralUserIfBackground(int oldUserId) {
        if (ActivityManagerDebugConfig.DEBUG_MU) {
            Slog.i("ActivityManager", "Stop guest or ephemeral user if background: " + oldUserId);
        }
        synchronized (this.mLock) {
            UserState oldUss = this.mStartedUsers.get(oldUserId);
            if (!(oldUserId == 0 || oldUserId == this.mCurrentUserId || oldUss == null || oldUss.state == 4)) {
                if (oldUss.state == 5) {
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void scheduleStartProfiles() {
        FgThread.getHandler().post(new Runnable() {
            public final void run() {
                UserController.lambda$scheduleStartProfiles$5(UserController.this);
            }
        });
    }

    public static /* synthetic */ void lambda$scheduleStartProfiles$5(UserController userController) {
        if (!userController.mHandler.hasMessages(40)) {
            userController.mHandler.sendMessageDelayed(userController.mHandler.obtainMessage(40), 1000);
        }
    }

    /* access modifiers changed from: package-private */
    public void startProfiles() {
        int currentUserId = getCurrentUserId();
        if (ActivityManagerDebugConfig.DEBUG_MU) {
            Slog.i("ActivityManager", "startProfilesLocked");
        }
        List<UserInfo> profiles = this.mInjector.getUserManager().getProfiles(currentUserId, false);
        List<UserInfo> profilesToStart = new ArrayList<>(profiles.size());
        for (UserInfo user : profiles) {
            if ((user.flags & 16) == 16 && user.id != currentUserId && !user.isQuietModeEnabled()) {
                profilesToStart.add(user);
            } else if (user.id != currentUserId && user.isClonedProfile()) {
                Slog.i("ActivityManager", "startProfilesLocked clone profile: " + user);
                profilesToStart.add(user);
            }
        }
        int profilesToStartSize = profilesToStart.size();
        int i = 0;
        while (i < profilesToStartSize) {
            startUser(profilesToStart.get(i).id, false);
            i++;
        }
        if (i < profilesToStartSize) {
            Slog.w("ActivityManager", "More profiles than MAX_RUNNING_USERS");
        }
    }

    private IStorageManager getStorageManager() {
        return IStorageManager.Stub.asInterface(ServiceManager.getService("mount"));
    }

    /* access modifiers changed from: package-private */
    public boolean startUser(int userId, boolean foreground) {
        return startUser(userId, foreground, null);
    }

    private void setMultiDpi(WindowManagerService wms, int userId) {
        int dpi = SystemProperties.getInt("persist.sys.dpi", 0);
        int width = SystemProperties.getInt("persist.sys.rog.width", 0);
        int realdpi = SystemProperties.getInt("persist.sys.realdpi", 0);
        if (width > 0) {
            dpi = SystemProperties.getInt("persist.sys.realdpi", SystemProperties.getInt("persist.sys.dpi", SystemProperties.getInt("ro.sf.real_lcd_density", SystemProperties.getInt("ro.sf.lcd_density", 0))));
        }
        if (wms != null && dpi > 0) {
            Slog.i("ActivityManager", "set multi dpi for user :" + userId + ", sys.dpi:" + dpi + ", readdpi:" + realdpi + ", width:" + width);
            wms.setForcedDisplayDensityForUser(0, dpi, userId);
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:100:?, code lost:
        r1.mInjector.updateUserConfiguration();
        updateCurrentProfileIds();
        r1.mInjector.getWindowManager().setCurrentUser(r15, getCurrentProfileIds());
        android.hwtheme.HwThemeManager.linkDataSkinDirAsUser(r50);
        r1.mInjector.reportCurWakefulnessUsageEvent();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:101:0x01f9, code lost:
        if (r1.mUserSwitchUiEnabled == false) goto L_0x0240;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:103:0x0201, code lost:
        if (com.android.server.am.UserController.Injector.access$200(r1.mInjector, r6, r7) != false) goto L_0x0240;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:104:0x0203, code lost:
        r1.mInjector.getWindowManager().setSwitchingUser(true);
        r1.mInjector.getWindowManager().lockNow(null);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:111:?, code lost:
        r2 = java.lang.Integer.valueOf(r1.mCurrentUserId);
        updateCurrentProfileIds();
        r1.mInjector.getWindowManager().setCurrentProfileIds(getCurrentProfileIds());
        r4 = r1.mLock;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:112:0x0234, code lost:
        monitor-enter(r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:114:?, code lost:
        r1.mUserLru.remove(r2);
        r1.mUserLru.add(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:115:0x023f, code lost:
        monitor-exit(r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:119:0x0243, code lost:
        if (r5.state != 4) goto L_0x0261;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:121:?, code lost:
        r5.setState(r5.lastState);
        r1.mInjector.getUserManagerInternal().setUserState(r15, r5.state);
        r2 = r1.mLock;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:122:0x0257, code lost:
        monitor-enter(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:124:?, code lost:
        updateStartedUserArrayLU();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:125:0x025b, code lost:
        monitor-exit(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:126:0x025c, code lost:
        r3 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:134:0x0264, code lost:
        if (r5.state != 5) goto L_0x0281;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:137:?, code lost:
        r5.setState(0);
        r1.mInjector.getUserManagerInternal().setUserState(r15, r5.state);
        r2 = r1.mLock;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:138:0x0277, code lost:
        monitor-enter(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:140:?, code lost:
        updateStartedUserArrayLU();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:141:0x027b, code lost:
        monitor-exit(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:142:0x027c, code lost:
        r3 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:147:0x0281, code lost:
        r0 = r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:150:0x0284, code lost:
        if (r5.state != 0) goto L_0x02a0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:152:?, code lost:
        r1.mInjector.getUserManager().onBeforeStartUser(r15);
        r22 = r6;
        r1.mHandler.sendMessage(r1.mHandler.obtainMessage(50, r15, 0));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:153:0x02a0, code lost:
        r22 = r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:154:0x02a2, code lost:
        if (r14 == false) goto L_0x02da;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:155:0x02a4, code lost:
        r1.mHandler.sendMessage(r1.mHandler.obtainMessage(60, r15, r8));
        r1.mHandler.removeMessages(10);
        r1.mHandler.removeMessages(30);
        r1.mHandler.sendMessage(r1.mHandler.obtainMessage(10, r8, r15, r5));
        r23 = r5;
        r1.mHandler.sendMessageDelayed(r1.mHandler.obtainMessage(30, r8, r15, r5), 3000);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:156:0x02da, code lost:
        r23 = r5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:157:0x02dc, code lost:
        if (r0 == false) goto L_0x033f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:159:?, code lost:
        r6 = new android.content.Intent("android.intent.action.USER_STARTED");
        r6.addFlags(1342177280);
        r6.putExtra("android.intent.extra.user_handle", r15);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:160:0x0306, code lost:
        r29 = r23;
        r23 = r6;
        r30 = r7;
        r31 = r8;
        r32 = r9;
        r18 = r11;
        r17 = r14;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:162:?, code lost:
        r1.mInjector.broadcastIntent(r6, null, null, 0, null, null, null, -1, null, false, false, com.android.server.am.ActivityManagerService.MY_PID, 1000, r50);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:163:0x0331, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:164:0x0332, code lost:
        r6 = r50;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:165:0x0335, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:166:0x0336, code lost:
        r18 = r11;
        r17 = r14;
        r6 = r50;
        r8 = r9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:167:0x033f, code lost:
        r30 = r7;
        r31 = r8;
        r32 = r9;
        r18 = r11;
        r17 = r14;
        r29 = r23;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:168:0x034b, code lost:
        if (r17 == false) goto L_0x035c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:169:0x034d, code lost:
        r6 = r50;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:171:?, code lost:
        moveUserToForeground(r29, r31, r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:172:0x0357, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:174:0x035c, code lost:
        r6 = r50;
        r7 = r31;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:176:?, code lost:
        finishUserBoot(r29);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:177:0x0365, code lost:
        if (r0 == false) goto L_0x03a4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:179:?, code lost:
        r2 = new android.content.Intent("android.intent.action.USER_STARTING");
        r2.addFlags(1073741824);
        r2.putExtra("android.intent.extra.user_handle", r6);
        r1.mInjector.broadcastIntent(r2, null, new com.android.server.am.UserController.AnonymousClass6(r1), 0, null, null, new java.lang.String[]{"android.permission.INTERACT_ACROSS_USERS"}, -1, null, true, false, com.android.server.am.ActivityManagerService.MY_PID, 1000, -1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:181:?, code lost:
        r1.isColdStart = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:182:0x03a6, code lost:
        android.os.Binder.restoreCallingIdentity(r32);
        android.util.Slog.i("ActivityManager", "_StartUser startUser userid:" + r6 + " cost " + (android.os.SystemClock.elapsedRealtime() - r18) + " ms");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:183:0x03d6, code lost:
        return true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:184:0x03d7, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:185:0x03d8, code lost:
        r8 = r32;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:186:0x03dc, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:187:0x03dd, code lost:
        r22 = r6;
        r30 = r7;
        r7 = r8;
        r8 = r9;
        r18 = r11;
        r17 = r14;
        r6 = r15;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:191:?, code lost:
        throw r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:192:0x03ea, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:204:0x041c, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:81:0x0197, code lost:
        if (getUserInfo(r0.intValue()).isClonedProfile() == false) goto L_0x01a7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:87:0x01b2, code lost:
        r5 = r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:88:0x01b3, code lost:
        if (r13 == null) goto L_0x01ba;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:90:?, code lost:
        r5.mUnlockProgress.addListener(r13);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:91:0x01ba, code lost:
        if (r21 == false) goto L_0x01c7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:92:0x01bc, code lost:
        r1.mInjector.getUserManagerInternal().setUserState(r15, r5.state);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:93:0x01c7, code lost:
        if (r14 == false) goto L_0x021b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:94:0x01c9, code lost:
        r1.mInjector.reportGlobalUsageEventLocked(16);
        r2 = r1.mLock;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:95:0x01d2, code lost:
        monitor-enter(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:97:?, code lost:
        r1.mCurrentUserId = r15;
        r1.mTargetUserId = -10000;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:98:0x01d9, code lost:
        monitor-exit(r2);
     */
    public boolean startUser(int userId, boolean foreground, IProgressListener unlockListener) {
        long ident;
        boolean updateUmState;
        boolean needStart;
        long ident2;
        long ident3;
        boolean needStart2;
        int i = userId;
        boolean z = foreground;
        IProgressListener iProgressListener = unlockListener;
        if (this.mInjector.checkCallingPermission("android.permission.INTERACT_ACROSS_USERS_FULL") == 0) {
            long startedTime = SystemClock.elapsedRealtime();
            this.SwitchUser_Time = startedTime;
            Slog.i("ActivityManager", "Starting userid:" + i + " fg:" + z);
            long ident4 = Binder.clearCallingIdentity();
            int oldUserId = getCurrentUserId();
            if (oldUserId == i) {
                Binder.restoreCallingIdentity(ident4);
                return true;
            }
            if (z) {
                try {
                    this.mInjector.clearAllLockedTasks("startUser");
                } catch (Throwable th) {
                    th = th;
                    ident = ident4;
                    long j = startedTime;
                    boolean z2 = z;
                    int i2 = i;
                }
            }
            try {
                UserInfo userInfo = getUserInfo(userId);
                if (userInfo == null) {
                    Slog.w("ActivityManager", "No user info for user #" + i);
                    Binder.restoreCallingIdentity(ident4);
                    return false;
                }
                if (z) {
                    if (userInfo.isManagedProfile()) {
                        Slog.w("ActivityManager", "Cannot switch to User #" + i + ": not a full user");
                        Binder.restoreCallingIdentity(ident4);
                        return false;
                    }
                }
                setMultiDpi(this.mInjector.getWindowManager(), i);
                UserInfo lastUserInfo = getUserInfo(this.mCurrentUserId);
                if (z) {
                    if (this.mUserSwitchUiEnabled && !this.mInjector.shouldSkipKeyguard(lastUserInfo, userInfo)) {
                        this.mInjector.getWindowManager().startFreezingScreen(17432717, 17432716);
                    }
                }
                synchronized (this.mLock) {
                    try {
                        UserState uss = this.mStartedUsers.get(i);
                        if (uss == null) {
                            try {
                                needStart2 = false;
                                try {
                                    uss = new UserState(UserHandle.of(userId));
                                    updateUmState = false;
                                    try {
                                        uss.mUnlockProgress.addListener(new UserProgressListener());
                                        this.mStartedUsers.put(i, uss);
                                        updateStartedUserArrayLU();
                                        needStart = true;
                                        updateUmState = true;
                                    } catch (Throwable th2) {
                                        th = th2;
                                        ident = ident4;
                                        boolean z3 = z;
                                        int i3 = i;
                                        while (true) {
                                            try {
                                                break;
                                            } catch (Throwable th3) {
                                                th = th3;
                                            }
                                        }
                                        throw th;
                                    }
                                } catch (Throwable th4) {
                                    th = th4;
                                    UserInfo userInfo2 = lastUserInfo;
                                    UserInfo userInfo3 = userInfo;
                                    int i4 = oldUserId;
                                    ident = ident4;
                                    long j2 = startedTime;
                                    boolean z4 = z;
                                    int i5 = i;
                                    while (true) {
                                        break;
                                    }
                                    throw th;
                                }
                            } catch (Throwable th5) {
                                th = th5;
                                UserInfo userInfo4 = lastUserInfo;
                                UserInfo userInfo5 = userInfo;
                                int i6 = oldUserId;
                                ident = ident4;
                                long j3 = startedTime;
                                boolean z5 = z;
                                int i7 = i;
                                while (true) {
                                    break;
                                }
                                throw th;
                            }
                        } else {
                            needStart2 = false;
                            updateUmState = false;
                            try {
                                if (uss.state == 5) {
                                    if (!isCallingOnHandlerThread()) {
                                        Slog.i("ActivityManager", "User #" + i + " is shutting down - will start after full stop");
                                        this.mHandler.post(new Runnable(i, z, iProgressListener) {
                                            private final /* synthetic */ int f$1;
                                            private final /* synthetic */ boolean f$2;
                                            private final /* synthetic */ IProgressListener f$3;

                                            {
                                                this.f$1 = r2;
                                                this.f$2 = r3;
                                                this.f$3 = r4;
                                            }

                                            public final void run() {
                                                UserController.this.startUser(this.f$1, this.f$2, this.f$3);
                                            }
                                        });
                                        Binder.restoreCallingIdentity(ident4);
                                        return true;
                                    }
                                }
                                needStart = false;
                            } catch (Throwable th6) {
                                th = th6;
                                UserInfo userInfo6 = lastUserInfo;
                                UserInfo userInfo7 = userInfo;
                                int i8 = oldUserId;
                                ident2 = ident4;
                                long j4 = startedTime;
                                boolean z6 = z;
                                int i9 = i;
                                while (true) {
                                    break;
                                }
                                throw th;
                            }
                        }
                        try {
                            Integer userIdInt = Integer.valueOf(userId);
                            if (getUserInfo(userIdInt.intValue()) != null) {
                                try {
                                } catch (Throwable th7) {
                                    th = th7;
                                    UserInfo userInfo8 = lastUserInfo;
                                    UserInfo userInfo9 = userInfo;
                                    int i10 = oldUserId;
                                    ident = ident4;
                                    long j5 = startedTime;
                                    boolean z7 = z;
                                    int i11 = i;
                                    while (true) {
                                        break;
                                    }
                                    throw th;
                                }
                            }
                            this.mUserLru.remove(userIdInt);
                            this.mUserLru.add(userIdInt);
                        } catch (Throwable th8) {
                            th = th8;
                            UserInfo userInfo10 = lastUserInfo;
                            UserInfo userInfo11 = userInfo;
                            int i12 = oldUserId;
                            ident2 = ident4;
                            long j6 = startedTime;
                            boolean z8 = z;
                            int i13 = i;
                            while (true) {
                                break;
                            }
                            throw th;
                        }
                    } catch (Throwable th9) {
                        th = th9;
                        UserInfo userInfo12 = lastUserInfo;
                        UserInfo userInfo13 = userInfo;
                        int i14 = oldUserId;
                        ident = ident4;
                        long j7 = startedTime;
                        boolean z9 = z;
                        int i15 = i;
                        while (true) {
                            break;
                        }
                        throw th;
                    }
                }
            } catch (Throwable th10) {
                th = th10;
                ident = ident4;
                long j8 = startedTime;
                boolean z10 = z;
                int i16 = i;
                Binder.restoreCallingIdentity(ident);
                throw th;
            }
        } else {
            boolean z11 = z;
            int i17 = i;
            String msg = "Permission Denial: switchUser() from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + " requires " + "android.permission.INTERACT_ACROSS_USERS_FULL";
            Slog.w("ActivityManager", msg);
            throw new SecurityException(msg);
        }
        ident = ident3;
        Binder.restoreCallingIdentity(ident);
        throw th;
    }

    private boolean isCallingOnHandlerThread() {
        return Looper.myLooper() == this.mHandler.getLooper();
    }

    /* access modifiers changed from: package-private */
    public void startUserInForeground(int targetUserId) {
        if (!startUser(targetUserId, true)) {
            this.mInjector.getWindowManager().setSwitchingUser(false);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean unlockUser(int userId, byte[] token, byte[] secret, IProgressListener listener) {
        if (this.mInjector.checkCallingPermission("android.permission.INTERACT_ACROSS_USERS_FULL") == 0) {
            long binderToken = Binder.clearCallingIdentity();
            try {
                return unlockUserCleared(userId, token, secret, listener);
            } finally {
                Binder.restoreCallingIdentity(binderToken);
            }
        } else {
            String msg = "Permission Denial: unlockUser() from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + " requires " + "android.permission.INTERACT_ACROSS_USERS_FULL";
            Slog.w("ActivityManager", msg);
            throw new SecurityException(msg);
        }
    }

    private boolean maybeUnlockUser(int userId) {
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

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0031, code lost:
        android.util.Slog.i("ActivityManager", "ClonedProfile user unlock, set mHaveTryCloneProUserUnlock true!");
        r8.mHaveTryCloneProUserUnlock = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0069, code lost:
        if (r13.isClonedProfile() != false) goto L_0x0031;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x002f, code lost:
        if (r13.isClonedProfile() != false) goto L_0x0031;
     */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x00d6  */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x00e4 A[SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x00ff  */
    /* JADX WARNING: Removed duplicated region for block: B:58:0x0104  */
    public boolean unlockUserCleared(int userId, byte[] token, byte[] secret, IProgressListener listener) {
        boolean z;
        ISDCardCryptedHelper helper;
        UserState uss;
        UserState uss2;
        int[] userIds;
        int[] userIds2;
        boolean isSuccess;
        int i = userId;
        byte[] bArr = token;
        byte[] bArr2 = secret;
        IProgressListener iProgressListener = listener;
        UserInfo userInfo = getUserInfo(userId);
        IStorageManager storageManager = getStorageManager();
        if (!StorageManager.isUserKeyUnlocked(userId)) {
            try {
                if (!this.mIsSupportISec) {
                    storageManager.unlockUserKey(i, userInfo.serialNumber, bArr, bArr2);
                } else {
                    storageManager.unlockUserKeyISec(i, userInfo.serialNumber, bArr, bArr2);
                }
                if (userInfo != null) {
                }
            } catch (RemoteException | RuntimeException e) {
                Slog.w("ActivityManager", "Failed to unlock: " + e.getMessage() + " ,SupportISec: " + this.mIsSupportISec);
                if (userInfo != null) {
                }
            } catch (Throwable th) {
                if (userInfo != null && userInfo.isClonedProfile()) {
                    Slog.i("ActivityManager", "ClonedProfile user unlock, set mHaveTryCloneProUserUnlock true!");
                    this.mHaveTryCloneProUserUnlock = true;
                }
                throw th;
            }
        } else if (this.mIsSupportISec) {
            if (bArr == null && bArr2 == null) {
                Slog.w("ActivityManager", "is SupportISec,Failed to unlockUserScreenISec: token is null  And secret is null");
            } else {
                try {
                    isSuccess = storageManager.setScreenStateFlag(i, userInfo.serialNumber, 2);
                } catch (RemoteException | RuntimeException e2) {
                    Slog.w("ActivityManager", "is SupportISec,Failed to setScreenStateFlag: " + e2.getMessage());
                    isSuccess = false;
                }
                if (isSuccess) {
                    final IStorageManager iStorageManager = storageManager;
                    final int i2 = i;
                    AnonymousClass7 r15 = r1;
                    final UserInfo userInfo2 = userInfo;
                    boolean z2 = isSuccess;
                    Handler handler = this.mHandler;
                    final byte[] bArr3 = bArr;
                    z = true;
                    final byte[] bArr4 = bArr2;
                    AnonymousClass7 r1 = new Runnable() {
                        public void run() {
                            try {
                                iStorageManager.unlockUserScreenISec(i2, userInfo2.serialNumber, bArr3, bArr4, 1);
                            } catch (RemoteException | RuntimeException e) {
                                Slog.w("ActivityManager", "is SupportISec,Failed to unlockUserScreenISec: " + e.getMessage());
                            }
                        }
                    };
                    handler.post(r15);
                    helper = HwServiceFactory.getSDCardCryptedHelper();
                    if (helper != null) {
                        UserInfo info = getUserInfo(userId);
                        if (info != null) {
                            helper.unlockKey(i, info.serialNumber, bArr, bArr2);
                        }
                    }
                    synchronized (this.mLock) {
                        uss = this.mStartedUsers.get(i);
                        if (uss != null) {
                            uss.mUnlockProgress.addListener(iProgressListener);
                            uss.tokenProvided = bArr != null ? z : false;
                        }
                    }
                    uss2 = uss;
                    if (uss2 != null) {
                        notifyFinished(i, iProgressListener);
                        return false;
                    }
                    int i3 = 0;
                    finishUserUnlocking(uss2);
                    synchronized (this.mLock) {
                        userIds = new int[this.mStartedUsers.size()];
                        for (int i4 = 0; i4 < userIds.length; i4++) {
                            userIds[i4] = this.mStartedUsers.keyAt(i4);
                        }
                    }
                    int length = userIds.length;
                    while (i3 < length) {
                        int testUserId = userIds[i3];
                        UserInfo parent = this.mInjector.getUserManager().getProfileParent(testUserId);
                        if (parent == null || parent.id != i || testUserId == i) {
                            userIds2 = userIds;
                        } else {
                            StringBuilder sb = new StringBuilder();
                            userIds2 = userIds;
                            sb.append("User ");
                            sb.append(testUserId);
                            sb.append(" (parent ");
                            sb.append(parent.id);
                            sb.append("): attempting unlock because parent was just unlocked");
                            Slog.d("ActivityManager", sb.toString());
                            maybeUnlockUser(testUserId);
                        }
                        i3++;
                        userIds = userIds2;
                    }
                    return z;
                }
            }
        }
        z = true;
        helper = HwServiceFactory.getSDCardCryptedHelper();
        if (helper != null) {
        }
        synchronized (this.mLock) {
        }
        uss2 = uss;
        if (uss2 != null) {
        }
    }

    /* access modifiers changed from: package-private */
    public boolean switchUser(int targetUserId) {
        enforceShellRestriction("no_debugging_features", targetUserId);
        int currentUserId = getCurrentUserId();
        UserInfo targetUserInfo = getUserInfo(targetUserId);
        if (targetUserId == currentUserId) {
            Slog.i("ActivityManager", "user #" + targetUserId + " is already the current user");
            return true;
        } else if (targetUserInfo == null) {
            Slog.w("ActivityManager", "No user info for user #" + targetUserId);
            return false;
        } else if (!targetUserInfo.supportsSwitchTo()) {
            Slog.w("ActivityManager", "Cannot switch to User #" + targetUserId + ": not supported");
            return false;
        } else if (targetUserInfo.isManagedProfile()) {
            Slog.w("ActivityManager", "Cannot switch to User #" + targetUserId + ": not a full user");
            return false;
        } else {
            synchronized (this.mLock) {
                this.mTargetUserId = targetUserId;
            }
            UserInfo currentUserInfo = getUserInfo(this.mCurrentUserId);
            boolean isHiddenSpaceSwitch = this.mInjector.mService.mHwAMSEx.isHiddenSpaceSwitch(currentUserInfo, targetUserInfo);
            this.misHiddenSpaceSwitch = isHiddenSpaceSwitch;
            if (!this.mUserSwitchUiEnabled || isHiddenSpaceSwitch) {
                if (isHiddenSpaceSwitch) {
                    long ident = Binder.clearCallingIdentity();
                    try {
                        this.mInjector.cleanAppForHiddenSpace();
                    } finally {
                        Binder.restoreCallingIdentity(ident);
                    }
                }
                this.mHandler.removeMessages(START_USER_SWITCH_FG_MSG);
                this.mHandler.sendMessage(this.mHandler.obtainMessage(START_USER_SWITCH_FG_MSG, targetUserId, 0));
            } else {
                Pair<UserInfo, UserInfo> userNames = new Pair<>(currentUserInfo, targetUserInfo);
                this.mUiHandler.removeMessages(1000);
                this.mUiHandler.sendMessage(this.mHandler.obtainMessage(1000, userNames));
            }
            return true;
        }
    }

    /* access modifiers changed from: package-private */
    public void showUserSwitchDialog(Pair<UserInfo, UserInfo> fromToUserPair) {
        this.mInjector.showUserSwitchingDialog((UserInfo) fromToUserPair.first, (UserInfo) fromToUserPair.second, getSwitchingFromSystemUserMessage(), getSwitchingToSystemUserMessage());
    }

    private void dispatchForegroundProfileChanged(int userId) {
        int observerCount = this.mUserSwitchObservers.beginBroadcast();
        for (int i = 0; i < observerCount; i++) {
            try {
                this.mUserSwitchObservers.getBroadcastItem(i).onForegroundProfileSwitch(userId);
            } catch (RemoteException e) {
            }
        }
        this.mUserSwitchObservers.finishBroadcast();
    }

    /* access modifiers changed from: package-private */
    public void dispatchUserSwitchComplete(int userId) {
        long startedTime = SystemClock.elapsedRealtime();
        this.mInjector.getWindowManager().setSwitchingUser(false);
        int observerCount = this.mUserSwitchObservers.beginBroadcast();
        for (int i = 0; i < observerCount; i++) {
            try {
                this.mUserSwitchObservers.getBroadcastItem(i).onUserSwitchComplete(userId);
            } catch (RemoteException e) {
            }
        }
        this.mUserSwitchObservers.finishBroadcast();
        if (this.misHiddenSpaceSwitch) {
            this.SwitchUser_Time = SystemClock.elapsedRealtime() - this.SwitchUser_Time;
            Context context = this.mInjector.getContext();
            Flog.bdReport(context, 530, "{isColdStart:" + this.isColdStart + ",SwitchUser_Time:" + this.SwitchUser_Time + "ms}");
        }
        Slog.i("ActivityManager", "_StartUser dispatchUserSwitchComplete userid:" + userId + " cost " + (SystemClock.elapsedRealtime() - startedTime) + " ms");
    }

    private void dispatchLockedBootComplete(int userId) {
        int observerCount = this.mUserSwitchObservers.beginBroadcast();
        for (int i = 0; i < observerCount; i++) {
            try {
                this.mUserSwitchObservers.getBroadcastItem(i).onLockedBootComplete(userId);
            } catch (RemoteException e) {
            }
        }
        this.mUserSwitchObservers.finishBroadcast();
    }

    private void stopBackgroundUsersIfEnforced(int oldUserId) {
        if (oldUserId != 0 && hasUserRestriction("no_run_in_background", oldUserId)) {
            synchronized (this.mLock) {
                if (ActivityManagerDebugConfig.DEBUG_MU) {
                    Slog.i("ActivityManager", "stopBackgroundUsersIfEnforced stopping " + oldUserId + " and related users");
                }
                stopUsersLU(oldUserId, false, null);
            }
        }
    }

    private void timeoutUserSwitch(UserState uss, int oldUserId, int newUserId) {
        synchronized (this.mLock) {
            Slog.e("ActivityManager", "User switch timeout: from " + oldUserId + " to " + newUserId);
            this.mTimeoutUserSwitchCallbacks = this.mCurWaitingUserSwitchCallbacks;
            this.mHandler.removeMessages(USER_SWITCH_CALLBACKS_TIMEOUT_MSG);
            sendContinueUserSwitchLU(uss, oldUserId, newUserId);
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(USER_SWITCH_CALLBACKS_TIMEOUT_MSG, oldUserId, newUserId), 5000);
        }
    }

    private void timeoutUserSwitchCallbacks(int oldUserId, int newUserId) {
        synchronized (this.mLock) {
            if (this.mTimeoutUserSwitchCallbacks != null && !this.mTimeoutUserSwitchCallbacks.isEmpty()) {
                Slog.wtf("ActivityManager", "User switch timeout: from " + oldUserId + " to " + newUserId + ". Observers that didn't respond: " + this.mTimeoutUserSwitchCallbacks);
                this.mTimeoutUserSwitchCallbacks = null;
            }
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x00a0, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x00bb, code lost:
        r0 = th;
     */
    public void dispatchUserSwitch(UserState uss, int oldUserId, int newUserId) {
        ArraySet<String> observerCount;
        int observerCount2;
        ArraySet<String> curWaitingUserSwitchCallbacks;
        int observerCount3;
        int observerCount4;
        int i = newUserId;
        StringBuilder sb = new StringBuilder();
        sb.append("Dispatch onUserSwitching oldUser #");
        int i2 = oldUserId;
        sb.append(i2);
        sb.append(" newUser #");
        sb.append(i);
        Slog.d("ActivityManager", sb.toString());
        int observerCount5 = this.mUserSwitchObservers.beginBroadcast();
        if (observerCount5 > 0) {
            ArraySet<String> observerCount6 = new ArraySet<>();
            synchronized (this.mLock) {
                try {
                    uss.switching = true;
                    this.mCurWaitingUserSwitchCallbacks = observerCount6;
                } finally {
                    th = th;
                    while (true) {
                    }
                }
            }
            final AtomicInteger waitingCallbacksCount = new AtomicInteger(observerCount5);
            long dispatchStartedTime = SystemClock.elapsedRealtime();
            int i3 = 0;
            while (true) {
                observerCount5 = i3;
                if (observerCount5 >= observerCount6) {
                    break;
                }
                try {
                    String name = "#" + observerCount5 + " " + this.mUserSwitchObservers.getBroadcastCookie(observerCount5);
                    synchronized (this.mLock) {
                        try {
                            observerCount6.add(name);
                        } catch (RemoteException e) {
                        } catch (Throwable th) {
                            th = th;
                            String str = name;
                            curWaitingUserSwitchCallbacks = observerCount6;
                            observerCount3 = observerCount6;
                            observerCount4 = observerCount5;
                            while (true) {
                                break;
                            }
                            throw th;
                        }
                    }
                    r1 = r1;
                    final long j = dispatchStartedTime;
                    final String str2 = name;
                    final int i4 = observerCount6;
                    String str3 = name;
                    final ArraySet<String> arraySet = observerCount6;
                    observerCount3 = observerCount6;
                    observerCount4 = observerCount5;
                    final int i5 = i;
                    final UserState userState = uss;
                    curWaitingUserSwitchCallbacks = observerCount6;
                    final int i6 = i2;
                    AnonymousClass8 r1 = new IRemoteCallback.Stub() {
                        public void sendResult(Bundle data) throws RemoteException {
                            synchronized (UserController.this.mLock) {
                                long delay = SystemClock.elapsedRealtime() - j;
                                if (delay > 3000) {
                                    Slog.e("ActivityManager", "User switch timeout: observer " + str2 + " sent result after " + delay + " ms");
                                }
                                Slog.d("ActivityManager", "_StartUser User switch done: observer " + str2 + " sent result after " + delay + " ms, total:" + i4);
                                arraySet.remove(str2);
                                if (waitingCallbacksCount.decrementAndGet() == 0 && arraySet == UserController.this.mCurWaitingUserSwitchCallbacks) {
                                    Slog.i("ActivityManager", "_StartUser dispatchUserSwitch userid:" + i5 + " cost " + (SystemClock.elapsedRealtime() - j) + " ms");
                                    UserController.this.sendContinueUserSwitchLU(userState, i6, i5);
                                }
                            }
                        }
                    };
                    this.mUserSwitchObservers.getBroadcastItem(observerCount4).onUserSwitching(i, r1);
                } catch (RemoteException e2) {
                    curWaitingUserSwitchCallbacks = observerCount6;
                }
                i3 = observerCount4 + 1;
                UserState userState2 = uss;
                observerCount5 = observerCount3;
                observerCount6 = curWaitingUserSwitchCallbacks;
            }
        } else {
            synchronized (this.mLock) {
                sendContinueUserSwitchLU(uss, oldUserId, newUserId);
            }
        }
        this.mUserSwitchObservers.finishBroadcast();
    }

    /* access modifiers changed from: package-private */
    public void sendContinueUserSwitchLU(UserState uss, int oldUserId, int newUserId) {
        this.mCurWaitingUserSwitchCallbacks = null;
        this.mHandler.removeMessages(30);
        this.mHandler.sendMessage(this.mHandler.obtainMessage(20, oldUserId, newUserId, uss));
    }

    /* access modifiers changed from: package-private */
    public void continueUserSwitch(UserState uss, int oldUserId, int newUserId) {
        Slog.d("ActivityManager", "Continue user switch oldUser #" + oldUserId + ", newUser #" + newUserId);
        if (this.mUserSwitchUiEnabled) {
            this.mInjector.getWindowManager().stopFreezingScreen();
        }
        uss.switching = false;
        this.mHandler.removeMessages(80);
        this.mHandler.sendMessage(this.mHandler.obtainMessage(80, newUserId, 0));
        stopGuestOrEphemeralUserIfBackground(oldUserId);
        stopBackgroundUsersIfEnforced(oldUserId);
    }

    private void moveUserToForeground(UserState uss, int oldUserId, int newUserId) {
        boolean homeInFront = this.mInjector.stackSupervisorSwitchUser(newUserId, uss);
        HwThemeManager.updateConfiguration(true);
        ContentResolver cr = this.mInjector.getContext().getContentResolver();
        Configuration config = new Configuration();
        HwThemeManager.retrieveSimpleUIConfig(cr, config, newUserId);
        config.fontScale = Settings.System.getFloatForUser(cr, "font_scale", config.fontScale, newUserId);
        this.mInjector.updatePersistentConfiguration(config);
        if (homeInFront) {
            this.mInjector.startHomeActivity(newUserId, "moveUserToForeground");
        } else {
            this.mInjector.stackSupervisorResumeFocusedStackTopActivity();
        }
        EventLogTags.writeAmSwitchUser(newUserId);
        sendUserSwitchBroadcasts(oldUserId, newUserId);
    }

    /* access modifiers changed from: package-private */
    public void sendUserSwitchBroadcasts(int oldUserId, int newUserId) {
        int i = oldUserId;
        int i2 = newUserId;
        long ident = Binder.clearCallingIdentity();
        if (i >= 0) {
            try {
                List<UserInfo> profiles = this.mInjector.getUserManager().getProfiles(i, false);
                int count = profiles.size();
                for (int i3 = 0; i3 < count; i3++) {
                    int profileUserId = profiles.get(i3).id;
                    Intent intent = new Intent("android.intent.action.USER_BACKGROUND");
                    intent.addFlags(1342177280);
                    intent.putExtra("android.intent.extra.user_handle", profileUserId);
                    Intent intent2 = intent;
                    this.mInjector.broadcastIntent(intent, null, null, 0, null, null, null, -1, null, false, false, ActivityManagerService.MY_PID, 1000, profileUserId);
                }
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(ident);
                throw th;
            }
        }
        if (i2 >= 0) {
            List<UserInfo> profiles2 = this.mInjector.getUserManager().getProfiles(i2, false);
            int count2 = profiles2.size();
            for (int i4 = 0; i4 < count2; i4++) {
                int profileUserId2 = profiles2.get(i4).id;
                Intent intent3 = new Intent("android.intent.action.USER_FOREGROUND");
                intent3.addFlags(1342177280);
                intent3.putExtra("android.intent.extra.user_handle", profileUserId2);
                Intent intent4 = intent3;
                this.mInjector.broadcastIntent(intent3, null, null, 0, null, null, null, -1, null, false, false, ActivityManagerService.MY_PID, 1000, profileUserId2);
            }
            Intent intent5 = new Intent("android.intent.action.USER_SWITCHED");
            intent5.addFlags(1342177280);
            intent5.putExtra("android.intent.extra.user_handle", i2);
            this.mInjector.broadcastIntent(intent5, null, null, 0, null, null, new String[]{"android.permission.MANAGE_USERS"}, -1, null, false, false, ActivityManagerService.MY_PID, 1000, -1);
        }
        Binder.restoreCallingIdentity(ident);
    }

    /* access modifiers changed from: package-private */
    public int handleIncomingUser(int callingPid, int callingUid, int userId, boolean allowAll, int allowMode, String name, String callerPackage) {
        boolean allow;
        int i = callingUid;
        int i2 = userId;
        int i3 = allowMode;
        String str = callerPackage;
        int callingUserId = UserHandle.getUserId(callingUid);
        if (callingUserId == i2 || this.mInjector.getUserManagerInternal().isSameGroupForClone(callingUserId, i2)) {
            return i2;
        }
        int targetUserId = unsafeConvertIncomingUser(i2);
        if (!(i == 0 || i == 1000)) {
            if (this.mInjector.isCallerRecents(i) && callingUserId == getCurrentUserId() && isSameProfileGroup(callingUserId, targetUserId)) {
                allow = true;
            } else if (this.mInjector.checkComponentPermission("android.permission.INTERACT_ACROSS_USERS_FULL", callingPid, i, -1, true) == 0) {
                allow = true;
            } else if (i3 == 2) {
                allow = false;
            } else if (this.mInjector.checkComponentPermission("android.permission.INTERACT_ACROSS_USERS", callingPid, i, -1, true) != 0) {
                allow = false;
            } else if (i3 == 0) {
                allow = true;
            } else if (i3 == 1) {
                allow = isSameProfileGroup(callingUserId, targetUserId);
            } else {
                String str2 = name;
                throw new IllegalArgumentException("Unknown mode: " + i3);
            }
            if (!allow) {
                if (i2 == -3) {
                    targetUserId = callingUserId;
                } else {
                    StringBuilder builder = new StringBuilder(128);
                    builder.append("Permission Denial: ");
                    builder.append(name);
                    if (str != null) {
                        builder.append(" from ");
                        builder.append(str);
                    }
                    builder.append(" asks to run as user ");
                    builder.append(i2);
                    builder.append(" but is calling from user ");
                    builder.append(UserHandle.getUserId(callingUid));
                    builder.append("; this requires ");
                    builder.append("android.permission.INTERACT_ACROSS_USERS_FULL");
                    if (i3 != 2) {
                        builder.append(" or ");
                        builder.append("android.permission.INTERACT_ACROSS_USERS");
                    }
                    String msg = builder.toString();
                    Slog.w("ActivityManager", msg);
                    throw new SecurityException(msg);
                }
            }
        }
        String str3 = name;
        if (!allowAll) {
            ensureNotSpecialUser(targetUserId);
        }
        if (i != 2000 || targetUserId < 0 || !hasUserRestriction("no_debugging_features", targetUserId)) {
            return targetUserId;
        }
        throw new SecurityException("Shell does not have permission to access user " + targetUserId + "\n " + Debug.getCallers(3));
    }

    /* access modifiers changed from: package-private */
    public int unsafeConvertIncomingUser(int userId) {
        if (userId == -2 || userId == -3) {
            return getCurrentUserId();
        }
        return userId;
    }

    /* access modifiers changed from: package-private */
    public void ensureNotSpecialUser(int userId) {
        if (userId < 0) {
            throw new IllegalArgumentException("Call does not support special user #" + userId);
        }
    }

    /* access modifiers changed from: package-private */
    public void registerUserSwitchObserver(IUserSwitchObserver observer, String name) {
        Preconditions.checkNotNull(name, "Observer name cannot be null");
        if (this.mInjector.checkCallingPermission("android.permission.INTERACT_ACROSS_USERS_FULL") == 0) {
            this.mUserSwitchObservers.register(observer, name);
            return;
        }
        String msg = "Permission Denial: registerUserSwitchObserver() from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + " requires " + "android.permission.INTERACT_ACROSS_USERS_FULL";
        Slog.w("ActivityManager", msg);
        throw new SecurityException(msg);
    }

    /* access modifiers changed from: package-private */
    public void sendForegroundProfileChanged(int userId) {
        this.mHandler.removeMessages(70);
        this.mHandler.obtainMessage(70, userId, 0).sendToTarget();
    }

    /* access modifiers changed from: package-private */
    public void unregisterUserSwitchObserver(IUserSwitchObserver observer) {
        this.mUserSwitchObservers.unregister(observer);
    }

    /* access modifiers changed from: package-private */
    public UserState getStartedUserState(int userId) {
        UserState userState;
        synchronized (this.mLock) {
            userState = this.mStartedUsers.get(userId);
        }
        return userState;
    }

    /* access modifiers changed from: package-private */
    public boolean hasStartedUserState(int userId) {
        return this.mStartedUsers.get(userId) != null;
    }

    private void updateStartedUserArrayLU() {
        int num = 0;
        for (int i = 0; i < this.mStartedUsers.size(); i++) {
            UserState uss = this.mStartedUsers.valueAt(i);
            if (!(uss.state == 4 || uss.state == 5)) {
                num++;
            }
        }
        this.mStartedUserArray = new int[num];
        int num2 = 0;
        for (int i2 = 0; i2 < this.mStartedUsers.size(); i2++) {
            UserState uss2 = this.mStartedUsers.valueAt(i2);
            if (!(uss2.state == 4 || uss2.state == 5)) {
                this.mStartedUserArray[num2] = this.mStartedUsers.keyAt(i2);
                num2++;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void sendBootCompleted(IIntentReceiver resultTo) {
        SparseArray<UserState> startedUsers;
        synchronized (this.mLock) {
            startedUsers = this.mStartedUsers.clone();
        }
        for (int i = 0; i < startedUsers.size(); i++) {
            finishUserBoot(startedUsers.valueAt(i), resultTo);
        }
    }

    /* access modifiers changed from: package-private */
    public void onSystemReady() {
        updateCurrentProfileIds();
        this.mInjector.reportCurWakefulnessUsageEvent();
    }

    private void updateCurrentProfileIds() {
        List<UserInfo> profiles = this.mInjector.getUserManager().getProfiles(getCurrentUserId(), false);
        int[] currentProfileIds = new int[profiles.size()];
        for (int i = 0; i < currentProfileIds.length; i++) {
            currentProfileIds[i] = profiles.get(i).id;
        }
        List<UserInfo> users = this.mInjector.getUserManager().getUsers(false);
        synchronized (this.mLock) {
            this.mCurrentProfileIds = currentProfileIds;
            this.mUserProfileGroupIds.clear();
            for (int i2 = 0; i2 < users.size(); i2++) {
                UserInfo user = users.get(i2);
                if (user.profileGroupId != -10000) {
                    this.mUserProfileGroupIds.put(user.id, user.profileGroupId);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public int[] getStartedUserArray() {
        int[] iArr;
        synchronized (this.mLock) {
            iArr = this.mStartedUserArray;
        }
        return iArr;
    }

    /* access modifiers changed from: package-private */
    public boolean isUserRunning(int userId, int flags) {
        UserState state = getStartedUserState(userId);
        boolean z = false;
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
                case 4:
                case 5:
                    return StorageManager.isUserKeyUnlocked(userId);
                default:
                    return false;
            }
        } else if ((flags & 4) != 0) {
            switch (state.state) {
                case 3:
                    return true;
                case 4:
                case 5:
                    return StorageManager.isUserKeyUnlocked(userId);
                default:
                    return false;
            }
        } else {
            if (!(state.state == 4 || state.state == 5)) {
                z = true;
            }
            return z;
        }
    }

    /* access modifiers changed from: package-private */
    public UserInfo getCurrentUser() {
        UserInfo currentUserLU;
        if (this.mInjector.checkCallingPermission("android.permission.INTERACT_ACROSS_USERS") != 0 && this.mInjector.checkCallingPermission("android.permission.INTERACT_ACROSS_USERS_FULL") != 0) {
            String msg = "Permission Denial: getCurrentUser() from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + " requires " + "android.permission.INTERACT_ACROSS_USERS";
            Slog.w("ActivityManager", msg);
            throw new SecurityException(msg);
        } else if (this.mTargetUserId == -10000) {
            return getUserInfo(this.mCurrentUserId);
        } else {
            synchronized (this.mLock) {
                currentUserLU = getCurrentUserLU();
            }
            return currentUserLU;
        }
    }

    /* access modifiers changed from: package-private */
    public UserInfo getCurrentUserLU() {
        return getUserInfo(this.mTargetUserId != -10000 ? this.mTargetUserId : this.mCurrentUserId);
    }

    /* access modifiers changed from: package-private */
    public int getCurrentOrTargetUserId() {
        int i;
        synchronized (this.mLock) {
            i = this.mTargetUserId != -10000 ? this.mTargetUserId : this.mCurrentUserId;
        }
        return i;
    }

    /* access modifiers changed from: package-private */
    public int getCurrentOrTargetUserIdLU() {
        return this.mTargetUserId != -10000 ? this.mTargetUserId : this.mCurrentUserId;
    }

    /* access modifiers changed from: package-private */
    public int getCurrentUserIdLU() {
        return this.mCurrentUserId;
    }

    /* access modifiers changed from: package-private */
    public int getCurrentUserId() {
        int i;
        synchronized (this.mLock) {
            i = this.mCurrentUserId;
        }
        return i;
    }

    private boolean isCurrentUserLU(int userId) {
        return userId == getCurrentOrTargetUserIdLU();
    }

    /* access modifiers changed from: package-private */
    public int[] getUsers() {
        UserManagerService ums = this.mInjector.getUserManager();
        if (ums != null) {
            return ums.getUserIds();
        }
        return new int[]{0};
    }

    /* access modifiers changed from: package-private */
    public UserInfo getUserInfo(int userId) {
        return this.mInjector.getUserManager().getUserInfo(userId);
    }

    /* access modifiers changed from: package-private */
    public int[] getUserIds() {
        return this.mInjector.getUserManager().getUserIds();
    }

    /* access modifiers changed from: package-private */
    public int[] expandUserId(int userId) {
        if (userId == -1) {
            return getUsers();
        }
        return new int[]{userId};
    }

    /* access modifiers changed from: package-private */
    public boolean exists(int userId) {
        return this.mInjector.getUserManager().exists(userId);
    }

    /* access modifiers changed from: package-private */
    public void enforceShellRestriction(String restriction, int userHandle) {
        if (Binder.getCallingUid() != 2000) {
            return;
        }
        if (userHandle < 0 || hasUserRestriction(restriction, userHandle)) {
            throw new SecurityException("Shell does not have permission to access user " + userHandle);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean hasUserRestriction(String restriction, int userId) {
        return this.mInjector.getUserManager().hasUserRestriction(restriction, userId);
    }

    /* access modifiers changed from: package-private */
    public Set<Integer> getProfileIds(int userId) {
        Set<Integer> userIds = new HashSet<>();
        for (UserInfo user : this.mInjector.getUserManager().getProfiles(userId, false)) {
            userIds.add(Integer.valueOf(user.id));
        }
        return userIds;
    }

    /* access modifiers changed from: package-private */
    public boolean isSameProfileGroup(int callingUserId, int targetUserId) {
        boolean z = true;
        if (callingUserId == targetUserId) {
            return true;
        }
        synchronized (this.mLock) {
            int callingProfile = this.mUserProfileGroupIds.get(callingUserId, -10000);
            int targetProfile = this.mUserProfileGroupIds.get(targetUserId, -10000);
            if (callingProfile == -10000 || callingProfile != targetProfile) {
                z = false;
            }
        }
        return z;
    }

    /* access modifiers changed from: package-private */
    public boolean isUserOrItsParentRunning(int userId) {
        synchronized (this.mLock) {
            if (isUserRunning(userId, 0)) {
                return true;
            }
            int parentUserId = this.mUserProfileGroupIds.get(userId, -10000);
            if (parentUserId == -10000) {
                return false;
            }
            boolean isUserRunning = isUserRunning(parentUserId, 0);
            return isUserRunning;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isCurrentProfile(int userId) {
        boolean contains;
        synchronized (this.mLock) {
            contains = ArrayUtils.contains(this.mCurrentProfileIds, userId);
        }
        return contains;
    }

    /* access modifiers changed from: package-private */
    public int[] getCurrentProfileIds() {
        int[] iArr;
        synchronized (this.mLock) {
            iArr = this.mCurrentProfileIds;
        }
        return iArr;
    }

    /* access modifiers changed from: package-private */
    public void onUserRemoved(int userId) {
        synchronized (this.mLock) {
            for (int i = this.mUserProfileGroupIds.size() - 1; i >= 0; i--) {
                if (this.mUserProfileGroupIds.keyAt(i) == userId || this.mUserProfileGroupIds.valueAt(i) == userId) {
                    this.mUserProfileGroupIds.removeAt(i);
                }
            }
            this.mCurrentProfileIds = ArrayUtils.removeInt(this.mCurrentProfileIds, userId);
        }
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0017, code lost:
        return false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0018, code lost:
        r0 = r3.mInjector.getKeyguardManager();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0022, code lost:
        if (r0.isDeviceLocked(r4) == false) goto L_0x002c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0028, code lost:
        if (r0.isDeviceSecure(r4) == false) goto L_0x002c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x002a, code lost:
        r2 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x002c, code lost:
        return r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0015, code lost:
        if (r3.mLockPatternUtils.isSeparateProfileChallengeEnabled(r4) != false) goto L_0x0018;
     */
    public boolean shouldConfirmCredentials(int userId) {
        synchronized (this.mLock) {
            boolean z = false;
            if (this.mStartedUsers.get(userId) == null) {
                return false;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isLockScreenDisabled(int userId) {
        return this.mLockPatternUtils.isLockScreenDisabled(userId);
    }

    /* access modifiers changed from: package-private */
    public void setSwitchingFromSystemUserMessage(String switchingFromSystemUserMessage) {
        synchronized (this.mLock) {
            this.mSwitchingFromSystemUserMessage = switchingFromSystemUserMessage;
        }
    }

    /* access modifiers changed from: package-private */
    public void setSwitchingToSystemUserMessage(String switchingToSystemUserMessage) {
        synchronized (this.mLock) {
            this.mSwitchingToSystemUserMessage = switchingToSystemUserMessage;
        }
    }

    private String getSwitchingFromSystemUserMessage() {
        String str;
        synchronized (this.mLock) {
            str = this.mSwitchingFromSystemUserMessage;
        }
        return str;
    }

    private String getSwitchingToSystemUserMessage() {
        String str;
        synchronized (this.mLock) {
            str = this.mSwitchingToSystemUserMessage;
        }
        return str;
    }

    /* access modifiers changed from: package-private */
    public void writeToProto(ProtoOutputStream proto, long fieldId) {
        synchronized (this.mLock) {
            long token = proto.start(fieldId);
            for (int i = 0; i < this.mStartedUsers.size(); i++) {
                UserState uss = this.mStartedUsers.valueAt(i);
                long uToken = proto.start(2246267895809L);
                proto.write(1120986464257L, uss.mHandle.getIdentifier());
                uss.writeToProto(proto, 1146756268034L);
                proto.end(uToken);
            }
            for (int write : this.mStartedUserArray) {
                proto.write(2220498092034L, write);
            }
            for (int i2 = 0; i2 < this.mUserLru.size(); i2++) {
                proto.write(2220498092035L, this.mUserLru.get(i2).intValue());
            }
            if (this.mUserProfileGroupIds.size() > 0) {
                for (int i3 = 0; i3 < this.mUserProfileGroupIds.size(); i3++) {
                    long uToken2 = proto.start(2246267895812L);
                    proto.write(1120986464257L, this.mUserProfileGroupIds.keyAt(i3));
                    proto.write(1120986464258L, this.mUserProfileGroupIds.valueAt(i3));
                    proto.end(uToken2);
                }
            }
            proto.end(token);
        }
    }

    /* access modifiers changed from: package-private */
    public void dump(PrintWriter pw, boolean dumpAll) {
        synchronized (this.mLock) {
            pw.println("  mStartedUsers:");
            for (int i = 0; i < this.mStartedUsers.size(); i++) {
                UserState uss = this.mStartedUsers.valueAt(i);
                pw.print("    User #");
                pw.print(uss.mHandle.getIdentifier());
                pw.print(": ");
                uss.dump(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS, pw);
            }
            pw.print("  mStartedUserArray: [");
            for (int i2 = 0; i2 < this.mStartedUserArray.length; i2++) {
                if (i2 > 0) {
                    pw.print(", ");
                }
                pw.print(this.mStartedUserArray[i2]);
            }
            pw.println("]");
            pw.print("  mUserLru: [");
            for (int i3 = 0; i3 < this.mUserLru.size(); i3++) {
                if (i3 > 0) {
                    pw.print(", ");
                }
                pw.print(this.mUserLru.get(i3));
            }
            pw.println("]");
            if (this.mUserProfileGroupIds.size() > 0) {
                pw.println("  mUserProfileGroupIds:");
                for (int i4 = 0; i4 < this.mUserProfileGroupIds.size(); i4++) {
                    pw.print("    User #");
                    pw.print(this.mUserProfileGroupIds.keyAt(i4));
                    pw.print(" -> profile #");
                    pw.println(this.mUserProfileGroupIds.valueAt(i4));
                }
            }
        }
    }

    public boolean handleMessage(Message msg) {
        long startedTime = SystemClock.elapsedRealtime();
        switch (msg.what) {
            case 10:
                dispatchUserSwitch((UserState) msg.obj, msg.arg1, msg.arg2);
                break;
            case 20:
                continueUserSwitch((UserState) msg.obj, msg.arg1, msg.arg2);
                break;
            case 30:
                timeoutUserSwitch((UserState) msg.obj, msg.arg1, msg.arg2);
                break;
            case 40:
                startProfiles();
                break;
            case 50:
                this.mInjector.batteryStatsServiceNoteEvent(32775, Integer.toString(msg.arg1), msg.arg1);
                this.mInjector.getSystemServiceManager().startUser(msg.arg1);
                Slog.i("ActivityManager", "_StartUser Handle SYSTEM_USER_START_MSG userid:" + msg.arg1 + " cost " + (SystemClock.elapsedRealtime() - startedTime) + " ms");
                break;
            case 60:
                this.mInjector.batteryStatsServiceNoteEvent(16392, Integer.toString(msg.arg2), msg.arg2);
                this.mInjector.batteryStatsServiceNoteEvent(32776, Integer.toString(msg.arg1), msg.arg1);
                this.mInjector.getSystemServiceManager().switchUser(msg.arg1);
                Slog.i("ActivityManager", "_StartUser Handle SYSTEM_USER_CURRENT_MSG userid:" + msg.arg1 + " cost " + (SystemClock.elapsedRealtime() - startedTime) + " ms");
                break;
            case 70:
                dispatchForegroundProfileChanged(msg.arg1);
                break;
            case 80:
                dispatchUserSwitchComplete(msg.arg1);
                break;
            case USER_SWITCH_CALLBACKS_TIMEOUT_MSG /*90*/:
                timeoutUserSwitchCallbacks(msg.arg1, msg.arg2);
                break;
            case 100:
                int userId = msg.arg1;
                this.mInjector.getSystemServiceManager().unlockUser(userId);
                FgThread.getHandler().post(new Runnable(userId) {
                    private final /* synthetic */ int f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void run() {
                        UserController.this.mInjector.loadUserRecents(this.f$1);
                    }
                });
                finishUserUnlocked((UserState) msg.obj);
                Slog.i("ActivityManager", "_StartUser Handle SYSTEM_USER_UNLOCK_MSG userid:" + msg.arg1 + " cost " + (SystemClock.elapsedRealtime() - startedTime) + " ms");
                break;
            case 110:
                dispatchLockedBootComplete(msg.arg1);
                break;
            case START_USER_SWITCH_FG_MSG /*120*/:
                startUserInForeground(msg.arg1);
                break;
            case 1000:
                showUserSwitchDialog((Pair) msg.obj);
                break;
        }
        return false;
    }
}
