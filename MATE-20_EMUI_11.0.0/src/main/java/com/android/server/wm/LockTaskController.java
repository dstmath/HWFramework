package com.android.server.wm;

import android.app.admin.IDevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Debug;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Settings;
import android.telecom.TelecomManager;
import android.util.EventLog;
import android.util.Pair;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseIntArray;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.policy.IKeyguardDismissCallback;
import com.android.internal.statusbar.IStatusBarService;
import com.android.internal.widget.LockPatternUtils;
import com.android.server.LocalServices;
import com.android.server.statusbar.StatusBarManagerInternal;
import com.android.server.wm.LockTaskController;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;

public class LockTaskController {
    private static final String LOCK_TASK_TAG = "Lock-to-App";
    private static final SparseArray<Pair<Integer, Integer>> STATUS_BAR_FLAG_MAP_LOCKED = new SparseArray<>();
    @VisibleForTesting
    static final int STATUS_BAR_MASK_LOCKED = 61210624;
    @VisibleForTesting
    static final int STATUS_BAR_MASK_PINNED = 43974656;
    private static final String TAG = "ActivityTaskManager";
    private static final String TAG_LOCKTASK = "ActivityTaskManager";
    private final Context mContext;
    @VisibleForTesting
    IDevicePolicyManager mDevicePolicyManager;
    private final Handler mHandler;
    @VisibleForTesting
    LockPatternUtils mLockPatternUtils;
    private final SparseIntArray mLockTaskFeatures = new SparseIntArray();
    private int mLockTaskModeState = 0;
    private final ArrayList<TaskRecord> mLockTaskModeTasks = new ArrayList<>();
    private final SparseArray<String[]> mLockTaskPackages = new SparseArray<>();
    private int mPendingDisableFromDismiss = -10000;
    @VisibleForTesting
    IStatusBarService mStatusBarService;
    private final ActivityStackSupervisor mSupervisor;
    @VisibleForTesting
    TelecomManager mTelecomManager;
    private final IBinder mToken = new LockTaskToken();
    @VisibleForTesting
    WindowManagerService mWindowManager;

    static {
        STATUS_BAR_FLAG_MAP_LOCKED.append(1, new Pair<>(8388608, 2));
        STATUS_BAR_FLAG_MAP_LOCKED.append(2, new Pair<>(393216, 4));
        STATUS_BAR_FLAG_MAP_LOCKED.append(4, new Pair<>(2097152, 0));
        STATUS_BAR_FLAG_MAP_LOCKED.append(8, new Pair<>(16777216, 0));
        STATUS_BAR_FLAG_MAP_LOCKED.append(16, new Pair<>(0, 8));
    }

    LockTaskController(Context context, ActivityStackSupervisor supervisor, Handler handler) {
        this.mContext = context;
        this.mSupervisor = supervisor;
        this.mHandler = handler;
    }

    /* access modifiers changed from: package-private */
    public void setWindowManager(WindowManagerService windowManager) {
        this.mWindowManager = windowManager;
    }

    /* access modifiers changed from: package-private */
    public int getLockTaskModeState() {
        return this.mLockTaskModeState;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public boolean isTaskLocked(TaskRecord task) {
        return this.mLockTaskModeTasks.contains(task);
    }

    private boolean isRootTask(TaskRecord task) {
        return this.mLockTaskModeTasks.indexOf(task) == 0;
    }

    /* access modifiers changed from: package-private */
    public boolean activityBlockedFromFinish(ActivityRecord activity) {
        TaskRecord task = activity.getTaskRecord();
        if (activity != task.getRootActivity() || activity != task.getTopActivity() || task.mLockTaskAuth == 4 || !isRootTask(task)) {
            return false;
        }
        Slog.i("ActivityTaskManager", "Not finishing task in lock task mode");
        showLockTaskToast();
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean canMoveTaskToBack(TaskRecord task) {
        if (!isRootTask(task)) {
            return true;
        }
        showLockTaskToast();
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean isTaskWhitelisted(TaskRecord task) {
        int i = task.mLockTaskAuth;
        if (i == 2 || i == 3 || i == 4) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean isLockTaskModeViolation(TaskRecord task) {
        return isLockTaskModeViolation(task, false);
    }

    /* access modifiers changed from: package-private */
    public boolean isLockTaskModeViolation(TaskRecord task, boolean isNewClearTask) {
        if (!isLockTaskModeViolationInternal(task, isNewClearTask)) {
            return false;
        }
        showLockTaskToast();
        return true;
    }

    /* access modifiers changed from: package-private */
    public TaskRecord getRootTask() {
        if (this.mLockTaskModeTasks.isEmpty()) {
            return null;
        }
        return this.mLockTaskModeTasks.get(0);
    }

    private boolean isLockTaskModeViolationInternal(TaskRecord task, boolean isNewClearTask) {
        if (isTaskLocked(task) && !isNewClearTask) {
            return false;
        }
        if (task.isActivityTypeRecents() && isRecentsAllowed(task.userId)) {
            return false;
        }
        if ((!isKeyguardAllowed(task.userId) || !isEmergencyCallTask(task)) && !isTaskWhitelisted(task) && !this.mLockTaskModeTasks.isEmpty()) {
            return true;
        }
        return false;
    }

    private boolean isRecentsAllowed(int userId) {
        return (getLockTaskFeaturesForUser(userId) & 8) != 0;
    }

    private boolean isKeyguardAllowed(int userId) {
        return (getLockTaskFeaturesForUser(userId) & 32) != 0;
    }

    private boolean isEmergencyCallTask(TaskRecord task) {
        Intent intent = task.intent;
        if (intent == null) {
            return false;
        }
        if (TelecomManager.EMERGENCY_DIALER_COMPONENT.equals(intent.getComponent()) || "android.intent.action.CALL_EMERGENCY".equals(intent.getAction())) {
            return true;
        }
        TelecomManager tm = getTelecomManager();
        String dialerPackage = tm != null ? tm.getSystemDialerPackage() : null;
        if (dialerPackage == null || !dialerPackage.equals(intent.getComponent().getPackageName())) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public void stopLockTaskMode(TaskRecord task, boolean isSystemCaller, int callingUid) {
        int i = this.mLockTaskModeState;
        if (i != 0) {
            if (isSystemCaller) {
                if (i == 2) {
                    clearLockedTasks("stopAppPinning");
                    return;
                }
                Slog.e("ActivityTaskManager", "Attempted to stop LockTask with isSystemCaller=true");
                showLockTaskToast();
            } else if (task == null) {
                throw new IllegalArgumentException("can't stop LockTask for null task");
            } else if (callingUid == task.mLockTaskUid || (task.mLockTaskUid == 0 && callingUid == task.effectiveUid)) {
                clearLockedTask(task);
            } else {
                throw new SecurityException("Invalid uid, expected " + task.mLockTaskUid + " callingUid=" + callingUid + " effectiveUid=" + task.effectiveUid);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void clearLockedTasks(String reason) {
        if (ActivityTaskManagerDebugConfig.DEBUG_LOCKTASK) {
            Slog.i("ActivityTaskManager", "clearLockedTasks: " + reason);
        }
        if (!this.mLockTaskModeTasks.isEmpty()) {
            clearLockedTask(this.mLockTaskModeTasks.get(0));
        }
    }

    /* access modifiers changed from: package-private */
    public void clearLockedTask(TaskRecord task) {
        if (!(task == null || this.mLockTaskModeTasks.isEmpty())) {
            if (task == this.mLockTaskModeTasks.get(0)) {
                for (int taskNdx = this.mLockTaskModeTasks.size() - 1; taskNdx > 0; taskNdx--) {
                    clearLockedTask(this.mLockTaskModeTasks.get(taskNdx));
                }
            }
            removeLockedTask(task);
            if (!this.mLockTaskModeTasks.isEmpty()) {
                task.performClearTaskLocked();
                this.mSupervisor.mRootActivityContainer.resumeFocusedStacksTopActivities();
            }
        }
    }

    private void removeLockedTask(TaskRecord task) {
        if (this.mLockTaskModeTasks.remove(task)) {
            if (ActivityTaskManagerDebugConfig.DEBUG_LOCKTASK) {
                Slog.d("ActivityTaskManager", "removeLockedTask: removed " + task);
            }
            if (this.mLockTaskModeTasks.isEmpty()) {
                if (ActivityTaskManagerDebugConfig.DEBUG_LOCKTASK) {
                    Slog.d("ActivityTaskManager", "removeLockedTask: task=" + task + " last task, reverting locktask mode. Callers=" + Debug.getCallers(3));
                }
                this.mHandler.post(new Runnable(task) {
                    /* class com.android.server.wm.$$Lambda$LockTaskController$2elXHbo9dze0DvBeuUaZ338FJqE */
                    private final /* synthetic */ TaskRecord f$1;

                    {
                        this.f$1 = r2;
                    }

                    @Override // java.lang.Runnable
                    public final void run() {
                        LockTaskController.this.lambda$removeLockedTask$0$LockTaskController(this.f$1);
                    }
                });
            }
        }
    }

    public /* synthetic */ void lambda$removeLockedTask$0$LockTaskController(TaskRecord task) {
        performStopLockTask(task.userId);
    }

    private void performStopLockTask(int userId) {
        try {
            setStatusBarState(0, userId);
            setKeyguardState(0, userId);
            if (this.mLockTaskModeState == 2) {
                lockKeyguardIfNeeded();
            }
            if (getDevicePolicyManager() != null) {
                getDevicePolicyManager().notifyLockTaskModeChanged(false, (String) null, userId);
            }
            if (this.mLockTaskModeState == 2) {
                getStatusBarService().showPinningEnterExitToast(false);
            }
            this.mWindowManager.onLockTaskStateChanged(0);
            this.mLockTaskModeState = 0;
        } catch (RemoteException ex) {
            throw new RuntimeException(ex);
        } catch (Throwable th) {
            this.mLockTaskModeState = 0;
            throw th;
        }
    }

    /* access modifiers changed from: package-private */
    public void showLockTaskToast() {
        if (this.mLockTaskModeState == 2) {
            try {
                getStatusBarService().showPinningEscapeToast();
            } catch (RemoteException e) {
                Slog.e("ActivityTaskManager", "Failed to send pinning escape toast", e);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void startLockTaskMode(TaskRecord task, boolean isSystemCaller, int callingUid) {
        if (!isSystemCaller) {
            task.mLockTaskUid = callingUid;
            if (task.mLockTaskAuth == 1) {
                if (ActivityTaskManagerDebugConfig.DEBUG_LOCKTASK) {
                    Slog.w("ActivityTaskManager", "Mode default, asking user");
                }
                StatusBarManagerInternal statusBarManager = (StatusBarManagerInternal) LocalServices.getService(StatusBarManagerInternal.class);
                if (statusBarManager != null) {
                    statusBarManager.showScreenPinningRequest(task.taskId);
                    return;
                }
                return;
            }
        }
        if (ActivityTaskManagerDebugConfig.DEBUG_LOCKTASK) {
            Slog.w("ActivityTaskManager", isSystemCaller ? "Locking pinned" : "Locking fully");
        }
        setLockTaskMode(task, isSystemCaller ? 2 : 1, "startLockTask", true);
    }

    private void setLockTaskMode(TaskRecord task, int lockTaskModeState, String reason, boolean andResume) {
        if (task.mLockTaskAuth == 0) {
            if (ActivityTaskManagerDebugConfig.DEBUG_LOCKTASK) {
                Slog.w("ActivityTaskManager", "setLockTaskMode: Can't lock due to auth");
            }
        } else if (isLockTaskModeViolation(task)) {
            Slog.e("ActivityTaskManager", "setLockTaskMode: Attempt to start an unauthorized lock task.");
        } else {
            Intent taskIntent = task.intent;
            if (this.mLockTaskModeTasks.isEmpty() && taskIntent != null) {
                this.mSupervisor.mRecentTasks.onLockTaskModeStateChanged(lockTaskModeState, task.userId);
                this.mLockTaskModeState = lockTaskModeState;
                this.mHandler.post(new Runnable(taskIntent, task, lockTaskModeState) {
                    /* class com.android.server.wm.$$Lambda$LockTaskController$9_wIEcqJktdkdI2IOf8QHYvHoks */
                    private final /* synthetic */ Intent f$1;
                    private final /* synthetic */ TaskRecord f$2;
                    private final /* synthetic */ int f$3;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                        this.f$3 = r4;
                    }

                    @Override // java.lang.Runnable
                    public final void run() {
                        LockTaskController.this.lambda$setLockTaskMode$1$LockTaskController(this.f$1, this.f$2, this.f$3);
                    }
                });
            }
            if (ActivityTaskManagerDebugConfig.DEBUG_LOCKTASK) {
                Slog.w("ActivityTaskManager", "setLockTaskMode: Locking to " + task + " Callers=" + Debug.getCallers(4));
            }
            if (!this.mLockTaskModeTasks.contains(task)) {
                this.mLockTaskModeTasks.add(task);
            }
            if (task.mLockTaskUid == -1) {
                task.mLockTaskUid = task.effectiveUid;
            }
            if (andResume) {
                this.mSupervisor.findTaskToMoveToFront(task, 0, null, reason, lockTaskModeState != 0);
                this.mSupervisor.mRootActivityContainer.resumeFocusedStacksTopActivities();
                ActivityStack stack = task.getStack();
                if (stack != null) {
                    stack.getDisplay().mDisplayContent.executeAppTransition();
                }
            } else if (lockTaskModeState != 0) {
                this.mSupervisor.handleNonResizableTaskIfNeeded(task, 0, 0, task.getStack(), true);
            }
        }
    }

    public /* synthetic */ void lambda$setLockTaskMode$1$LockTaskController(Intent taskIntent, TaskRecord task, int lockTaskModeState) {
        performStartLockTask(taskIntent.getComponent().getPackageName(), task.userId, lockTaskModeState);
    }

    private void performStartLockTask(String packageName, int userId, int lockTaskModeState) {
        if (lockTaskModeState == 2) {
            try {
                getStatusBarService().showPinningEnterExitToast(true);
            } catch (RemoteException ex) {
                throw new RuntimeException(ex);
            }
        }
        this.mWindowManager.onLockTaskStateChanged(lockTaskModeState);
        setStatusBarState(lockTaskModeState, userId);
        setKeyguardState(lockTaskModeState, userId);
        if (getDevicePolicyManager() != null) {
            getDevicePolicyManager().notifyLockTaskModeChanged(true, packageName, userId);
        }
    }

    /* access modifiers changed from: package-private */
    public void updateLockTaskPackages(int userId, String[] packages) {
        this.mLockTaskPackages.put(userId, packages);
        boolean taskChanged = false;
        int taskNdx = this.mLockTaskModeTasks.size() - 1;
        while (true) {
            boolean isWhitelisted = false;
            if (taskNdx < 0) {
                break;
            }
            TaskRecord lockedTask = this.mLockTaskModeTasks.get(taskNdx);
            boolean wasWhitelisted = lockedTask.mLockTaskAuth == 2 || lockedTask.mLockTaskAuth == 3;
            lockedTask.setLockTaskAuth();
            if (lockedTask.mLockTaskAuth == 2 || lockedTask.mLockTaskAuth == 3) {
                isWhitelisted = true;
            }
            if (this.mLockTaskModeState == 1 && lockedTask.userId == userId && wasWhitelisted && !isWhitelisted) {
                if (ActivityTaskManagerDebugConfig.DEBUG_LOCKTASK) {
                    Slog.d("ActivityTaskManager", "onLockTaskPackagesUpdated: removing " + lockedTask + " mLockTaskAuth()=" + lockedTask.lockTaskAuthToString());
                }
                removeLockedTask(lockedTask);
                lockedTask.performClearTaskLocked();
                taskChanged = true;
            }
            taskNdx--;
        }
        for (int displayNdx = this.mSupervisor.mRootActivityContainer.getChildCount() - 1; displayNdx >= 0; displayNdx--) {
            this.mSupervisor.mRootActivityContainer.getChildAt(displayNdx).onLockTaskPackagesUpdated();
        }
        ActivityRecord r = this.mSupervisor.mRootActivityContainer.topRunningActivity();
        TaskRecord task = r != null ? r.getTaskRecord() : null;
        if (this.mLockTaskModeTasks.isEmpty() && task != null && task.mLockTaskAuth == 2) {
            if (ActivityTaskManagerDebugConfig.DEBUG_LOCKTASK) {
                Slog.d("ActivityTaskManager", "onLockTaskPackagesUpdated: starting new locktask task=" + task);
            }
            setLockTaskMode(task, 1, "package updated", false);
            taskChanged = true;
        }
        if (taskChanged) {
            this.mSupervisor.mRootActivityContainer.resumeFocusedStacksTopActivities();
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isPackageWhitelisted(int userId, String pkg) {
        String[] whitelist;
        if (pkg == null || (whitelist = this.mLockTaskPackages.get(userId)) == null) {
            return false;
        }
        for (String whitelistedPkg : whitelist) {
            if (pkg.equals(whitelistedPkg)) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public void updateLockTaskFeatures(int userId, int flags) {
        if (flags != getLockTaskFeaturesForUser(userId)) {
            this.mLockTaskFeatures.put(userId, flags);
            if (!this.mLockTaskModeTasks.isEmpty() && userId == this.mLockTaskModeTasks.get(0).userId) {
                this.mHandler.post(new Runnable(userId) {
                    /* class com.android.server.wm.$$Lambda$LockTaskController$nuVptnoYwaF1CYydSggC_oxSSSc */
                    private final /* synthetic */ int f$1;

                    {
                        this.f$1 = r2;
                    }

                    @Override // java.lang.Runnable
                    public final void run() {
                        LockTaskController.this.lambda$updateLockTaskFeatures$2$LockTaskController(this.f$1);
                    }
                });
            }
        }
    }

    public /* synthetic */ void lambda$updateLockTaskFeatures$2$LockTaskController(int userId) {
        int i = this.mLockTaskModeState;
        if (i == 1) {
            setStatusBarState(i, userId);
            setKeyguardState(this.mLockTaskModeState, userId);
        }
    }

    private void setStatusBarState(int lockTaskModeState, int userId) {
        IStatusBarService statusBar = getStatusBarService();
        if (statusBar == null) {
            Slog.e("ActivityTaskManager", "Can't find StatusBarService");
            return;
        }
        int flags1 = 0;
        int flags2 = 0;
        if (lockTaskModeState == 2) {
            flags1 = STATUS_BAR_MASK_PINNED;
        } else if (lockTaskModeState == 1) {
            Pair<Integer, Integer> statusBarFlags = getStatusBarDisableFlags(getLockTaskFeaturesForUser(userId));
            flags1 = ((Integer) statusBarFlags.first).intValue();
            flags2 = ((Integer) statusBarFlags.second).intValue();
        }
        try {
            statusBar.disable(flags1, this.mToken, this.mContext.getPackageName());
            statusBar.disable2(flags2, this.mToken, this.mContext.getPackageName());
        } catch (RemoteException e) {
            Slog.e("ActivityTaskManager", "Failed to set status bar flags", e);
        }
    }

    private void setKeyguardState(int lockTaskModeState, final int userId) {
        this.mPendingDisableFromDismiss = -10000;
        if (lockTaskModeState == 0) {
            this.mWindowManager.reenableKeyguard(this.mToken, userId);
        } else if (lockTaskModeState != 1) {
            this.mWindowManager.disableKeyguard(this.mToken, LOCK_TASK_TAG, userId);
        } else if (isKeyguardAllowed(userId)) {
            this.mWindowManager.reenableKeyguard(this.mToken, userId);
        } else if (!this.mWindowManager.isKeyguardLocked() || this.mWindowManager.isKeyguardSecure(userId)) {
            this.mWindowManager.disableKeyguard(this.mToken, LOCK_TASK_TAG, userId);
        } else {
            this.mPendingDisableFromDismiss = userId;
            this.mWindowManager.dismissKeyguard(new IKeyguardDismissCallback.Stub() {
                /* class com.android.server.wm.LockTaskController.AnonymousClass1 */

                public void onDismissError() throws RemoteException {
                    Slog.i("ActivityTaskManager", "setKeyguardState: failed to dismiss keyguard");
                }

                public void onDismissSucceeded() throws RemoteException {
                    LockTaskController.this.mHandler.post(new Runnable(userId) {
                        /* class com.android.server.wm.$$Lambda$LockTaskController$1$WwLdnVMTh3BcztLd26dCnk4GjpA */
                        private final /* synthetic */ int f$1;

                        {
                            this.f$1 = r2;
                        }

                        @Override // java.lang.Runnable
                        public final void run() {
                            LockTaskController.AnonymousClass1.this.lambda$onDismissSucceeded$0$LockTaskController$1(this.f$1);
                        }
                    });
                }

                public /* synthetic */ void lambda$onDismissSucceeded$0$LockTaskController$1(int userId) {
                    if (LockTaskController.this.mPendingDisableFromDismiss == userId) {
                        LockTaskController.this.mWindowManager.disableKeyguard(LockTaskController.this.mToken, LockTaskController.LOCK_TASK_TAG, userId);
                        LockTaskController.this.mPendingDisableFromDismiss = -10000;
                    }
                }

                public void onDismissCancelled() throws RemoteException {
                    Slog.i("ActivityTaskManager", "setKeyguardState: dismiss cancelled");
                }
            }, null);
        }
    }

    private void lockKeyguardIfNeeded() {
        if (shouldLockKeyguard()) {
            this.mWindowManager.lockNow(null);
            this.mWindowManager.dismissKeyguard(null, null);
            getLockPatternUtils().requireCredentialEntry(-1);
        }
    }

    private boolean shouldLockKeyguard() {
        try {
            return Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "lock_to_app_exit_locked", -2) != 0;
        } catch (Settings.SettingNotFoundException e) {
            EventLog.writeEvent(1397638484, "127605586", -1, "");
            return getLockPatternUtils().isSecure(-2);
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public Pair<Integer, Integer> getStatusBarDisableFlags(int lockTaskFlags) {
        int flags1 = 67043328;
        int flags2 = 31;
        for (int i = STATUS_BAR_FLAG_MAP_LOCKED.size() - 1; i >= 0; i--) {
            Pair<Integer, Integer> statusBarFlags = STATUS_BAR_FLAG_MAP_LOCKED.valueAt(i);
            if ((STATUS_BAR_FLAG_MAP_LOCKED.keyAt(i) & lockTaskFlags) != 0) {
                flags1 &= ~((Integer) statusBarFlags.first).intValue();
                flags2 &= ~((Integer) statusBarFlags.second).intValue();
            }
        }
        return new Pair<>(Integer.valueOf(flags1 & STATUS_BAR_MASK_LOCKED), Integer.valueOf(flags2));
    }

    private int getLockTaskFeaturesForUser(int userId) {
        return this.mLockTaskFeatures.get(userId, 0);
    }

    private IStatusBarService getStatusBarService() {
        if (this.mStatusBarService == null) {
            this.mStatusBarService = IStatusBarService.Stub.asInterface(ServiceManager.checkService("statusbar"));
            if (this.mStatusBarService == null) {
                Slog.w("StatusBarManager", "warning: no STATUS_BAR_SERVICE");
            }
        }
        return this.mStatusBarService;
    }

    private IDevicePolicyManager getDevicePolicyManager() {
        if (this.mDevicePolicyManager == null) {
            this.mDevicePolicyManager = IDevicePolicyManager.Stub.asInterface(ServiceManager.checkService("device_policy"));
            if (this.mDevicePolicyManager == null) {
                Slog.w("ActivityTaskManager", "warning: no DEVICE_POLICY_SERVICE");
            }
        }
        return this.mDevicePolicyManager;
    }

    private LockPatternUtils getLockPatternUtils() {
        LockPatternUtils lockPatternUtils = this.mLockPatternUtils;
        if (lockPatternUtils == null) {
            return new LockPatternUtils(this.mContext);
        }
        return lockPatternUtils;
    }

    private TelecomManager getTelecomManager() {
        TelecomManager telecomManager = this.mTelecomManager;
        if (telecomManager == null) {
            return (TelecomManager) this.mContext.getSystemService(TelecomManager.class);
        }
        return telecomManager;
    }

    public void dump(PrintWriter pw, String prefix) {
        pw.println(prefix + "LockTaskController");
        String prefix2 = prefix + "  ";
        pw.println(prefix2 + "mLockTaskModeState=" + lockTaskModeToString());
        StringBuilder sb = new StringBuilder();
        sb.append(prefix2);
        sb.append("mLockTaskModeTasks=");
        pw.println(sb.toString());
        for (int i = 0; i < this.mLockTaskModeTasks.size(); i++) {
            pw.println(prefix2 + "  #" + i + " " + this.mLockTaskModeTasks.get(i));
        }
        pw.println(prefix2 + "mLockTaskPackages (userId:packages)=");
        for (int i2 = 0; i2 < this.mLockTaskPackages.size(); i2++) {
            pw.println(prefix2 + "  u" + this.mLockTaskPackages.keyAt(i2) + ":" + Arrays.toString(this.mLockTaskPackages.valueAt(i2)));
        }
    }

    private String lockTaskModeToString() {
        int i = this.mLockTaskModeState;
        if (i == 0) {
            return "NONE";
        }
        if (i == 1) {
            return "LOCKED";
        }
        if (i == 2) {
            return "PINNED";
        }
        return "unknown=" + this.mLockTaskModeState;
    }

    static class LockTaskToken extends Binder {
        private LockTaskToken() {
        }
    }
}
