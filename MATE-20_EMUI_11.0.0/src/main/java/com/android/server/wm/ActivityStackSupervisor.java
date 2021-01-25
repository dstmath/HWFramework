package com.android.server.wm;

import android.app.ActivityOptions;
import android.app.AppOpsManager;
import android.app.ProfilerInfo;
import android.app.ResultInfo;
import android.app.WaitResult;
import android.app.servertransaction.ActivityLifecycleItem;
import android.app.servertransaction.ClientTransaction;
import android.app.servertransaction.LaunchActivityItem;
import android.app.servertransaction.PauseActivityItem;
import android.app.servertransaction.ResumeActivityItem;
import android.common.HwFrameworkFactory;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.UserInfo;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.hardware.display.DisplayManagerGlobal;
import android.hdm.HwDeviceManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.Trace;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.WorkSource;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.CoordinationModeUtils;
import android.util.EventLog;
import android.util.Flog;
import android.util.HwPCUtils;
import android.util.MergedConfiguration;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.widget.Toast;
import android.zrhung.IZrHung;
import android.zrhung.ZrHungData;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.content.ReferrerIntent;
import com.android.internal.os.TransferPipe;
import com.android.internal.os.logging.MetricsLoggerWrapper;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.function.pooled.PooledLambda;
import com.android.server.HwServiceExFactory;
import com.android.server.UiThread;
import com.android.server.am.AppTimeTracker;
import com.android.server.am.UserState;
import com.android.server.wm.ActivityStack;
import com.android.server.wm.ActivityTaskManagerService;
import com.android.server.wm.RecentTasks;
import com.android.server.wm.WindowManagerService;
import com.android.server.zrhung.IZRHungService;
import com.huawei.server.wm.IHwActivityStackSupervisorEx;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ActivityStackSupervisor extends AbsActivityStackSupervisor implements RecentTasks.Callbacks {
    private static final ArrayMap<String, String> ACTION_TO_RUNTIME_PERMISSION = new ArrayMap<>();
    private static final int ACTIVITY_RESTRICTION_APPOP = 2;
    private static final int ACTIVITY_RESTRICTION_NONE = 0;
    private static final int ACTIVITY_RESTRICTION_PERMISSION = 1;
    public static final boolean DEFER_RESUME = true;
    static final int IDLE_NOW_MSG = 201;
    static final int IDLE_TIMEOUT = 10000;
    static final int IDLE_TIMEOUT_MSG = 200;
    static final int LAUNCH_TASK_BEHIND_COMPLETE = 212;
    static final int LAUNCH_TIMEOUT = 10000;
    static final int LAUNCH_TIMEOUT_MSG = 204;
    static final int LAUNCH_TIMEOUT_RESUME = 5000;
    static final int LAUNCH_TIMEOUT_RESUME_MSG = 1200;
    private static final int MAX_TASK_IDS_PER_USER = 100000;
    public static final boolean ON_TOP = true;
    static final boolean PAUSE_IMMEDIATELY = true;
    public static final boolean PRESERVE_WINDOWS = true;
    public static final boolean REMOVE_FROM_RECENTS = true;
    static final int REPORT_HOME_CHANGED_MSG = 216;
    static final int REPORT_MULTI_WINDOW_MODE_CHANGED_MSG = 214;
    static final int REPORT_PIP_MODE_CHANGED_MSG = 215;
    static final int RESTART_ACTIVITY_PROCESS_TIMEOUT_MSG = 213;
    static final int RESUME_TOP_ACTIVITY_MSG = 202;
    static final int SLEEP_TIMEOUT = 5000;
    static final int SLEEP_TIMEOUT_MSG = 203;
    private static final String TAG = "ActivityTaskManager";
    private static final String TAG_IDLE = "ActivityTaskManager";
    static final String TAG_KEYGUARD = "ActivityTaskManager_keyguard";
    private static final String TAG_PAUSE = "ActivityTaskManager";
    private static final String TAG_RECENTS = "ActivityTaskManager";
    private static final String TAG_STACK = "ActivityTaskManager";
    private static final String TAG_SWITCH = "ActivityTaskManager";
    static final String TAG_TASKS = "ActivityTaskManager";
    static final int TOP_RESUMED_STATE_LOSS_TIMEOUT = 500;
    static final int TOP_RESUMED_STATE_LOSS_TIMEOUT_MSG = 217;
    static final boolean VALIDATE_WAKE_LOCK_CALLER = false;
    String mActivityLaunchTrack = "";
    private ActivityMetricsLogger mActivityMetricsLogger;
    private boolean mAllowDockedStackResize = true;
    boolean mAppVisibilitiesChangedSinceLastPause;
    private final SparseIntArray mCurTaskIdForUser = new SparseIntArray(20);
    private int mDeferResumeCount;
    private boolean mDockedStackResizing;
    final ArrayList<ActivityRecord> mFinishingActivities = new ArrayList<>();
    final ArrayList<ActivityRecord> mGoingToSleepActivities = new ArrayList<>();
    PowerManager.WakeLock mGoingToSleepWakeLock;
    final ActivityStackSupervisorHandler mHandler;
    private boolean mHasPendingDockedBounds;
    public IHwActivityStackSupervisorEx mHwActivityStackSupervisorEx;
    private boolean mInitialized;
    private KeyguardController mKeyguardController;
    private LaunchParamsController mLaunchParamsController;
    LaunchParamsPersister mLaunchParamsPersister;
    PowerManager.WakeLock mLaunchingActivityWakeLock;
    final Looper mLooper;
    final ArrayList<ActivityRecord> mMultiWindowModeChangedActivities = new ArrayList<>();
    final ArrayList<ActivityRecord> mNoAnimActivities = new ArrayList<>();
    private Rect mPendingDockedBounds;
    private Rect mPendingTempDockedTaskBounds;
    private Rect mPendingTempDockedTaskInsetBounds;
    private Rect mPendingTempOtherTaskBounds;
    private Rect mPendingTempOtherTaskInsetBounds;
    PersisterQueue mPersisterQueue;
    final ArrayList<ActivityRecord> mPipModeChangedActivities = new ArrayList<>();
    Rect mPipModeChangedTargetStackBounds;
    private PowerManager mPowerManager;
    RecentTasks mRecentTasks;
    private final ArraySet<Integer> mResizingTasksDuringAnimation = new ArraySet<>();
    RootActivityContainer mRootActivityContainer;
    RunningTasks mRunningTasks;
    final ActivityTaskManagerService mService;
    final ArrayList<UserState> mStartingUsers = new ArrayList<>();
    final ArrayList<ActivityRecord> mStoppingActivities = new ArrayList<>();
    private final ActivityOptions mTmpOptions = ActivityOptions.makeBasic();
    private ActivityRecord mTopResumedActivity;
    private boolean mTopResumedActivityWaitingForPrev;
    boolean mUserLeaving = false;
    final ArrayList<WaitResult> mWaitingActivityLaunched = new ArrayList<>();
    private final ArrayList<WaitInfo> mWaitingForActivityVisible = new ArrayList<>();
    WindowManagerService mWindowManager;
    private final Rect tempRect = new Rect();

    static {
        ACTION_TO_RUNTIME_PERMISSION.put("android.media.action.IMAGE_CAPTURE", "android.permission.CAMERA");
        ACTION_TO_RUNTIME_PERMISSION.put("android.media.action.VIDEO_CAPTURE", "android.permission.CAMERA");
        ACTION_TO_RUNTIME_PERMISSION.put("android.intent.action.CALL", "android.permission.CALL_PHONE");
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.wm.ConfigurationContainer
    public int getChildCount() {
        return 0;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.wm.ConfigurationContainer
    public ActivityDisplay getChildAt(int index) {
        return null;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.wm.ConfigurationContainer
    public ConfigurationContainer getParent() {
        return null;
    }

    /* access modifiers changed from: package-private */
    public boolean canPlaceEntityOnDisplay(int displayId, int callingPid, int callingUid, ActivityInfo activityInfo) {
        if (displayId == 0) {
            return true;
        }
        if (this.mService.mSupportsMultiDisplay && isCallerAllowedToLaunchOnDisplay(callingPid, callingUid, displayId, activityInfo)) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public static class PendingActivityLaunch {
        final WindowProcessController callerApp;
        final ActivityRecord r;
        final ActivityRecord sourceRecord;
        final ActivityStack stack;
        final int startFlags;

        PendingActivityLaunch(ActivityRecord _r, ActivityRecord _sourceRecord, int _startFlags, ActivityStack _stack, WindowProcessController app) {
            this.r = _r;
            this.sourceRecord = _sourceRecord;
            this.startFlags = _startFlags;
            this.stack = _stack;
            this.callerApp = app;
        }

        /* access modifiers changed from: package-private */
        public void sendErrorResult(String message) {
            try {
                if (this.callerApp != null && this.callerApp.hasThread()) {
                    this.callerApp.getThread().scheduleCrash(message);
                }
            } catch (RemoteException e) {
                Slog.e("ActivityTaskManager", "Exception scheduling crash of failed activity launcher sourceRecord=" + this.sourceRecord, e);
            }
        }
    }

    public ActivityStackSupervisor(ActivityTaskManagerService service, Looper looper) {
        this.mService = service;
        this.mLooper = looper;
        this.mHandler = new ActivityStackSupervisorHandler(looper);
        this.mHwActivityStackSupervisorEx = HwServiceExFactory.getHwActivityStackSupervisorEx(this.mService);
    }

    public void initialize() {
        if (!this.mInitialized) {
            this.mInitialized = true;
            this.mRunningTasks = createRunningTasks();
            this.mActivityMetricsLogger = new ActivityMetricsLogger(this, this.mService.mContext, this.mHandler.getLooper());
            this.mKeyguardController = new KeyguardController(this.mService, this);
            this.mPersisterQueue = new PersisterQueue();
            this.mLaunchParamsPersister = new LaunchParamsPersister(this.mPersisterQueue, this);
            this.mLaunchParamsController = new LaunchParamsController(this.mService, this.mLaunchParamsPersister);
            this.mLaunchParamsController.registerDefaultModifiers(this);
        }
    }

    /* access modifiers changed from: package-private */
    public void onSystemReady() {
        this.mLaunchParamsPersister.onSystemReady();
    }

    /* access modifiers changed from: package-private */
    public void onUserUnlocked(int userId) {
        this.mPersisterQueue.startPersisting();
        this.mLaunchParamsPersister.onUnlockUser(userId);
    }

    public ActivityMetricsLogger getActivityMetricsLogger() {
        return this.mActivityMetricsLogger;
    }

    public KeyguardController getKeyguardController() {
        return this.mKeyguardController;
    }

    /* access modifiers changed from: package-private */
    public void setRecentTasks(RecentTasks recentTasks) {
        this.mRecentTasks = recentTasks;
        this.mRecentTasks.registerCallback(this);
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public RunningTasks createRunningTasks() {
        return new RunningTasks();
    }

    /* access modifiers changed from: package-private */
    public void initPowerManagement() {
        this.mPowerManager = (PowerManager) this.mService.mContext.getSystemService(PowerManager.class);
        this.mGoingToSleepWakeLock = this.mPowerManager.newWakeLock(1, "ActivityManager-Sleep");
        this.mLaunchingActivityWakeLock = this.mPowerManager.newWakeLock(1, "*launch*");
        this.mLaunchingActivityWakeLock.setReferenceCounted(false);
    }

    /* access modifiers changed from: package-private */
    public void setWindowManager(WindowManagerService wm) {
        this.mWindowManager = wm;
        getKeyguardController().setWindowManager(wm);
    }

    /* access modifiers changed from: package-private */
    public void moveRecentsStackToFront(String reason) {
        ActivityStack recentsStack = this.mRootActivityContainer.getDefaultDisplay().getStack(0, 3);
        if (recentsStack != null) {
            recentsStack.moveToFront(reason);
        }
    }

    /* access modifiers changed from: package-private */
    public void setNextTaskIdForUserLocked(int taskId, int userId) {
        if (taskId > this.mCurTaskIdForUser.get(userId, -1)) {
            this.mCurTaskIdForUser.put(userId, taskId);
        }
    }

    static int nextTaskIdForUser(int taskId, int userId) {
        int nextTaskId = taskId + 1;
        if (nextTaskId == (userId + 1) * MAX_TASK_IDS_PER_USER) {
            return nextTaskId - MAX_TASK_IDS_PER_USER;
        }
        return nextTaskId;
    }

    /* access modifiers changed from: package-private */
    public int getNextTaskIdForUserLocked(int userId) {
        int currentTaskId = this.mCurTaskIdForUser.get(userId, MAX_TASK_IDS_PER_USER * userId);
        int candidateTaskId = nextTaskIdForUser(currentTaskId, userId);
        do {
            if (this.mRecentTasks.containsTaskId(candidateTaskId, userId) || this.mRootActivityContainer.anyTaskForId(candidateTaskId, 1) != null) {
                candidateTaskId = nextTaskIdForUser(candidateTaskId, userId);
            } else {
                this.mCurTaskIdForUser.put(userId, candidateTaskId);
                return candidateTaskId;
            }
        } while (candidateTaskId != currentTaskId);
        throw new IllegalStateException("Cannot get an available task id. Reached limit of 100000 running tasks per user.");
    }

    /* access modifiers changed from: package-private */
    public void waitActivityVisible(ComponentName name, WaitResult result, long startTimeMs) {
        this.mWaitingForActivityVisible.add(new WaitInfo(name, result, startTimeMs));
    }

    /* access modifiers changed from: package-private */
    public void cleanupActivity(ActivityRecord r) {
        this.mFinishingActivities.remove(r);
        stopWaitingForActivityVisible(r);
    }

    /* access modifiers changed from: package-private */
    public void stopWaitingForActivityVisible(ActivityRecord r) {
        boolean changed = false;
        for (int i = this.mWaitingForActivityVisible.size() - 1; i >= 0; i--) {
            WaitInfo w = this.mWaitingForActivityVisible.get(i);
            if (w.matches(r.mActivityComponent)) {
                WaitResult result = w.getResult();
                changed = true;
                result.timeout = false;
                result.who = w.getComponent();
                result.totalTime = SystemClock.uptimeMillis() - w.getStartTime();
                this.mWaitingForActivityVisible.remove(w);
            }
        }
        if (changed) {
            Flog.i(101, "waited activity visible, r=" + r);
            this.mService.mGlobalLock.notifyAll();
        }
    }

    /* access modifiers changed from: package-private */
    public void reportWaitingActivityLaunchedIfNeeded(ActivityRecord r, int result) {
        if (!this.mWaitingActivityLaunched.isEmpty()) {
            if (result == 3 || result == 2) {
                boolean changed = false;
                for (int i = this.mWaitingActivityLaunched.size() - 1; i >= 0; i--) {
                    WaitResult w = this.mWaitingActivityLaunched.remove(i);
                    if (w.who == null) {
                        changed = true;
                        w.result = result;
                        if (result == 3) {
                            w.who = r.mActivityComponent;
                        }
                    }
                }
                if (changed) {
                    Flog.i(101, " reportTaskToFrontNoLaunch notify r = " + r);
                    this.mService.mGlobalLock.notifyAll();
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void reportActivityLaunchedLocked(boolean timeout, ActivityRecord r, long totalTime, int launchState) {
        boolean changed = false;
        for (int i = this.mWaitingActivityLaunched.size() - 1; i >= 0; i--) {
            WaitResult w = this.mWaitingActivityLaunched.remove(i);
            if (w.who == null) {
                changed = true;
                w.timeout = timeout;
                if (r != null) {
                    w.who = new ComponentName(r.info.packageName, r.info.name);
                }
                w.totalTime = totalTime;
                w.launchState = launchState;
            }
        }
        if (changed) {
            Flog.i(101, "waited activity launched, r= " + r);
            this.mService.mGlobalLock.notifyAll();
        }
    }

    /* access modifiers changed from: protected */
    public boolean keepStackResumed(ActivityStack stack) {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean isStackInVisible(ActivityStack stack) {
        return false;
    }

    /* access modifiers changed from: package-private */
    public ActivityInfo resolveActivity(Intent intent, ResolveInfo rInfo, int startFlags, ProfilerInfo profilerInfo) {
        ActivityInfo aInfo = rInfo != null ? rInfo.activityInfo : null;
        if (aInfo != null) {
            intent.setComponent(new ComponentName(aInfo.applicationInfo.packageName, aInfo.name));
            if (!aInfo.processName.equals("system") && !((startFlags & 14) == 0 && profilerInfo == null)) {
                synchronized (this.mService.mGlobalLock) {
                    try {
                        WindowManagerService.boostPriorityForLockedSection();
                        this.mService.mH.sendMessage(PooledLambda.obtainMessage($$Lambda$8ew6SY_v_7ex9pwFGDswbkGWuXc.INSTANCE, this.mService.mAmInternal, aInfo, Integer.valueOf(startFlags), profilerInfo, this.mService.mGlobalLock));
                        try {
                            this.mService.mGlobalLock.wait();
                        } catch (InterruptedException e) {
                        }
                    } finally {
                        WindowManagerService.resetPriorityAfterLockedSection();
                    }
                }
            }
            String intentLaunchToken = intent.getLaunchToken();
            if (aInfo.launchToken == null && intentLaunchToken != null) {
                aInfo.launchToken = intentLaunchToken;
            }
        }
        return aInfo;
    }

    /* access modifiers changed from: package-private */
    public ResolveInfo resolveIntent(Intent intent, String resolvedType, int userId) {
        return resolveIntent(intent, resolvedType, userId, 0, Binder.getCallingUid());
    }

    /* access modifiers changed from: package-private */
    public ResolveInfo resolveIntent(Intent intent, String resolvedType, int userId, int flags, int filterCallingUid) {
        Throwable th;
        int modifiedFlags;
        try {
            Trace.traceBegin(64, "resolveIntent");
            int modifiedFlags2 = flags | 65536 | 1024;
            if (!intent.isWebIntent()) {
                if ((intent.getFlags() & 2048) == 0) {
                    modifiedFlags = modifiedFlags2;
                    long token = Binder.clearCallingIdentity();
                    ResolveInfo resolveIntent = this.mService.getPackageManagerInternalLocked().resolveIntent(intent, resolvedType, modifiedFlags, userId, true, filterCallingUid);
                    try {
                        Trace.traceEnd(64);
                        return resolveIntent;
                    } catch (Throwable th2) {
                        th = th2;
                        Trace.traceEnd(64);
                        throw th;
                    }
                }
            }
            modifiedFlags = modifiedFlags2 | 8388608;
            long token2 = Binder.clearCallingIdentity();
            try {
                ResolveInfo resolveIntent2 = this.mService.getPackageManagerInternalLocked().resolveIntent(intent, resolvedType, modifiedFlags, userId, true, filterCallingUid);
                Trace.traceEnd(64);
                return resolveIntent2;
            } finally {
                Binder.restoreCallingIdentity(token2);
            }
        } catch (Throwable th3) {
            th = th3;
            Trace.traceEnd(64);
            throw th;
        }
    }

    /* access modifiers changed from: package-private */
    public ActivityInfo resolveActivity(Intent intent, String resolvedType, int startFlags, ProfilerInfo profilerInfo, int userId, int filterCallingUid) {
        return resolveActivity(intent, resolveIntent(intent, resolvedType, userId, 0, filterCallingUid), startFlags, profilerInfo);
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:116:0x02fd, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:118:0x0302, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:119:0x0303, code lost:
        r6 = "ActivityTaskManager";
     */
    /* JADX WARNING: Code restructure failed: missing block: B:120:0x0305, code lost:
        r4 = r13;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:139:0x039e, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:140:0x039f, code lost:
        r4 = r13;
        r6 = "ActivityTaskManager";
     */
    /* JADX WARNING: Code restructure failed: missing block: B:141:0x03a4, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:163:0x03e1, code lost:
        android.util.Slog.e(r6, "Second failure launching " + r40.intent.getComponent().flattenToShortString() + ", giving up", r0);
        r41.appDied();
        r4.requestFinishActivityLocked(r40.appToken, 0, null, "2nd-crash", false);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:164:0x0414, code lost:
        endDeferResume();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:165:0x0419, code lost:
        return false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:166:0x041a, code lost:
        r40.launchFailed = true;
        r41.removeActivity(r40);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:167:0x0423, code lost:
        throw r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:94:0x0290, code lost:
        r0 = th;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:124:0x031b  */
    /* JADX WARNING: Removed duplicated region for block: B:131:0x0359  */
    /* JADX WARNING: Removed duplicated region for block: B:135:0x038a  */
    /* JADX WARNING: Removed duplicated region for block: B:138:0x0397  */
    /* JADX WARNING: Removed duplicated region for block: B:141:0x03a4 A[ExcHandler: all (th java.lang.Throwable), Splitter:B:86:0x0253] */
    /* JADX WARNING: Removed duplicated region for block: B:153:0x03c4  */
    /* JADX WARNING: Removed duplicated region for block: B:163:0x03e1 A[Catch:{ all -> 0x0424 }] */
    /* JADX WARNING: Removed duplicated region for block: B:166:0x041a  */
    /* JADX WARNING: Removed duplicated region for block: B:176:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x0081 A[SYNTHETIC, Splitter:B:28:0x0081] */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x008e A[Catch:{ all -> 0x0085 }] */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x00a1  */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x00aa  */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x00b1  */
    /* JADX WARNING: Removed duplicated region for block: B:49:0x010a  */
    /* JADX WARNING: Removed duplicated region for block: B:62:0x0144  */
    /* JADX WARNING: Removed duplicated region for block: B:94:0x0290 A[ExcHandler: all (th java.lang.Throwable), Splitter:B:92:0x027e] */
    public boolean realStartActivityLocked(ActivityRecord r, WindowProcessController proc, boolean andResume, boolean checkConfig) throws RemoteException {
        RemoteException e;
        boolean andResume2;
        int applicationInfoUid;
        LockTaskController lockTaskController;
        LockTaskController lockTaskController2;
        String str;
        ActivityStack stack;
        RemoteException e2;
        List<ReferrerIntent> newIntents;
        ActivityLifecycleItem lifecycleItem;
        if (!this.mRootActivityContainer.allPausedActivitiesComplete()) {
            if (ActivityTaskManagerDebugConfig.DEBUG_SWITCH || ActivityTaskManagerDebugConfig.DEBUG_PAUSE || ActivityTaskManagerDebugConfig.DEBUG_STATES) {
                Slog.v("ActivityTaskManager", "realStartActivityLocked: Skipping start of r=" + r + " some activities pausing...");
            }
            return false;
        }
        TaskRecord task = r.getTaskRecord();
        if (task == null) {
            Slog.e("ActivityTaskManager", " null task for ActivityRecord: " + r);
            return false;
        }
        ActivityStack stack2 = task.getStack();
        beginDeferResume();
        try {
            r.startFreezingScreenLocked(proc, 0);
            r.startLaunchTickingLocked();
            r.setProcess(proc);
            if (andResume) {
                try {
                    if (!r.canResumeByCompat()) {
                        andResume2 = false;
                        if (getKeyguardController().isKeyguardLocked()) {
                            try {
                                r.notifyUnknownVisibilityLaunched();
                            } catch (Throwable th) {
                                e = th;
                                endDeferResume();
                                throw e;
                            }
                        }
                        if (checkConfig) {
                            this.mRootActivityContainer.ensureVisibilityAndConfig(r, r.getDisplayId(), false, true);
                        }
                        if (r.getActivityStack().checkKeyguardVisibility(r, true, true)) {
                            r.setVisibility(true);
                        }
                        applicationInfoUid = r.info.applicationInfo == null ? r.info.applicationInfo.uid : -1;
                        if (!(r.mUserId == proc.mUserId && r.appInfo.uid == applicationInfoUid)) {
                            Slog.wtf("ActivityTaskManager", "User ID for activity changing for " + r + " appInfo.uid=" + r.appInfo.uid + " info.ai.uid=" + applicationInfoUid + " old=" + r.app + " new=" + proc);
                        }
                        r.launchCount++;
                        r.lastLaunchTime = SystemClock.uptimeMillis();
                        if (ActivityTaskManagerDebugConfig.DEBUG_ALL) {
                            Slog.v("ActivityTaskManager", "Launching: " + r);
                        }
                        proc.addActivityIfNeeded(r);
                        lockTaskController = this.mService.getLockTaskController();
                        if (task.mLockTaskAuth == 2 || task.mLockTaskAuth == 4 || (task.mLockTaskAuth == 3 && lockTaskController.getLockTaskModeState() == 1)) {
                            lockTaskController.startLockTaskMode(task, false, 0);
                        }
                        try {
                            if (!proc.hasThread()) {
                                List<ResultInfo> results = null;
                                if (andResume2) {
                                    try {
                                        results = r.results;
                                        newIntents = r.newIntents;
                                    } catch (RemoteException e3) {
                                        e2 = e3;
                                        lockTaskController2 = lockTaskController;
                                        str = "ActivityTaskManager";
                                        stack = stack2;
                                        try {
                                            if (!r.launchFailed) {
                                            }
                                        } catch (Throwable th2) {
                                            e = th2;
                                            endDeferResume();
                                            throw e;
                                        }
                                    }
                                } else {
                                    newIntents = null;
                                }
                                if (ActivityTaskManagerDebugConfig.DEBUG_SWITCH) {
                                    Slog.v("ActivityTaskManager", "Launching: " + r + " icicle=" + r.icicle + " with results=" + results + " newIntents=" + newIntents + " andResume=" + andResume2);
                                }
                                EventLog.writeEvent(30006, Integer.valueOf(r.mUserId), Integer.valueOf(System.identityHashCode(r)), Integer.valueOf(task.taskId), r.shortComponentName);
                                if (r.isActivityTypeHome()) {
                                    updateHomeProcess(task.mActivities.get(0).app);
                                }
                                this.mService.getPackageManagerInternalLocked().notifyPackageUse(r.intent.getComponent().getPackageName(), 0);
                                r.sleeping = false;
                                r.forceNewConfig = false;
                                this.mService.getAppWarningsLocked().onStartActivity(r);
                                r.compat = this.mService.compatibilityInfoForPackageLocked(r.info.applicationInfo);
                                MergedConfiguration mergedConfiguration = new MergedConfiguration(this.mService.getGlobalConfiguration(), r.getMergedOverrideConfiguration());
                                r.setLastReportedConfiguration(mergedConfiguration);
                                logIfTransactionTooLarge(r.intent, r.icicle);
                                ClientTransaction clientTransaction = ClientTransaction.obtain(proc.getThread(), r.appToken);
                                DisplayContent dc = r.getDisplay().mDisplayContent;
                                lockTaskController2 = lockTaskController;
                                try {
                                    try {
                                        try {
                                            try {
                                                clientTransaction.addCallback(LaunchActivityItem.obtain(new Intent(r.intent), System.identityHashCode(r), r.info, mergedConfiguration.getGlobalConfiguration(), mergedConfiguration.getOverrideConfiguration(), r.compat, r.launchedFromPackage, task.voiceInteractor, proc.getReportedProcState(), r.icicle, r.persistentState, results, newIntents, dc.isNextTransitionForward(), proc.createProfilerInfoIfNeeded(), r.assistToken));
                                                if (andResume2) {
                                                    try {
                                                        this.mService.mHwATMSEx.customActivityResuming(r.packageName);
                                                        lifecycleItem = ResumeActivityItem.obtain(dc.isNextTransitionForward());
                                                    } catch (RemoteException e4) {
                                                        e2 = e4;
                                                        stack = stack2;
                                                        str = "ActivityTaskManager";
                                                    } catch (Throwable th3) {
                                                    }
                                                } else {
                                                    lifecycleItem = PauseActivityItem.obtain();
                                                }
                                                clientTransaction.setLifecycleStateRequest(lifecycleItem);
                                                this.mService.getLifecycleManager().scheduleTransaction(clientTransaction);
                                                if ((proc.mInfo.privateFlags & 2) != 0) {
                                                    if (this.mService.mHasHeavyWeightFeature) {
                                                        if (proc.mName.equals(proc.mInfo.packageName)) {
                                                            if (this.mService.mHeavyWeightProcess == null || this.mService.mHeavyWeightProcess == proc) {
                                                                str = "ActivityTaskManager";
                                                            } else {
                                                                str = "ActivityTaskManager";
                                                                Slog.w(str, "Starting new heavy weight process " + proc + " when already running " + this.mService.mHeavyWeightProcess);
                                                            }
                                                            this.mService.setHeavyWeightProcess(r);
                                                        } else {
                                                            str = "ActivityTaskManager";
                                                        }
                                                        endDeferResume();
                                                        r.launchFailed = false;
                                                        if (stack2.updateLRUListLocked(r)) {
                                                            Slog.w(str, "Activity " + r + " being launched, but already in LRU list");
                                                        }
                                                        if (andResume2 || !readyToResume()) {
                                                            if (ActivityTaskManagerDebugConfig.DEBUG_STATES) {
                                                                Slog.v(str, "Moving to PAUSED: " + r + " (starting in paused state)");
                                                            }
                                                            r.setState(ActivityStack.ActivityState.PAUSED, "realStartActivityLocked");
                                                        } else {
                                                            this.mActivityLaunchTrack += " minmalResume";
                                                            stack2.minimalResumeActivityLocked(r);
                                                        }
                                                        proc.onStartActivity(this.mService.mTopProcessState, r.info);
                                                        if (this.mRootActivityContainer.isTopDisplayFocusedStack(stack2)) {
                                                            this.mService.getActivityStartController().startSetupActivity();
                                                        }
                                                        if (r.app != null) {
                                                            return true;
                                                        }
                                                        r.app.updateServiceConnectionActivities();
                                                        return true;
                                                    }
                                                }
                                                str = "ActivityTaskManager";
                                                endDeferResume();
                                                r.launchFailed = false;
                                                if (stack2.updateLRUListLocked(r)) {
                                                }
                                                if (andResume2) {
                                                }
                                                if (ActivityTaskManagerDebugConfig.DEBUG_STATES) {
                                                }
                                                r.setState(ActivityStack.ActivityState.PAUSED, "realStartActivityLocked");
                                                proc.onStartActivity(this.mService.mTopProcessState, r.info);
                                                if (this.mRootActivityContainer.isTopDisplayFocusedStack(stack2)) {
                                                }
                                                if (r.app != null) {
                                                }
                                            } catch (RemoteException e5) {
                                                e2 = e5;
                                                str = "ActivityTaskManager";
                                                stack = stack2;
                                                if (!r.launchFailed) {
                                                }
                                            } catch (Throwable th4) {
                                            }
                                        } catch (RemoteException e6) {
                                            e2 = e6;
                                            str = "ActivityTaskManager";
                                            stack = stack2;
                                            if (!r.launchFailed) {
                                            }
                                        } catch (Throwable th5) {
                                            e = th5;
                                            endDeferResume();
                                            throw e;
                                        }
                                    } catch (RemoteException e7) {
                                        e2 = e7;
                                        str = "ActivityTaskManager";
                                        stack = stack2;
                                        if (!r.launchFailed) {
                                        }
                                    }
                                } catch (RemoteException e8) {
                                    e2 = e8;
                                    str = "ActivityTaskManager";
                                    stack = stack2;
                                    if (!r.launchFailed) {
                                    }
                                }
                            } else {
                                lockTaskController2 = lockTaskController;
                                str = "ActivityTaskManager";
                                stack = stack2;
                                try {
                                    throw new RemoteException();
                                } catch (RemoteException e9) {
                                    e2 = e9;
                                    if (!r.launchFailed) {
                                    }
                                }
                            }
                        } catch (RemoteException e10) {
                            e2 = e10;
                            lockTaskController2 = lockTaskController;
                            str = "ActivityTaskManager";
                            stack = stack2;
                            if (!r.launchFailed) {
                            }
                        }
                    }
                } catch (Throwable th6) {
                    e = th6;
                    endDeferResume();
                    throw e;
                }
            }
            andResume2 = andResume;
            try {
                if (getKeyguardController().isKeyguardLocked()) {
                }
                if (checkConfig) {
                }
                if (r.getActivityStack().checkKeyguardVisibility(r, true, true)) {
                }
                if (r.info.applicationInfo == null) {
                }
                Slog.wtf("ActivityTaskManager", "User ID for activity changing for " + r + " appInfo.uid=" + r.appInfo.uid + " info.ai.uid=" + applicationInfoUid + " old=" + r.app + " new=" + proc);
                r.launchCount++;
                r.lastLaunchTime = SystemClock.uptimeMillis();
                if (ActivityTaskManagerDebugConfig.DEBUG_ALL) {
                }
                proc.addActivityIfNeeded(r);
                lockTaskController = this.mService.getLockTaskController();
                lockTaskController.startLockTaskMode(task, false, 0);
                if (!proc.hasThread()) {
                }
            } catch (Throwable th7) {
                e = th7;
                endDeferResume();
                throw e;
            }
        } catch (Throwable th8) {
            e = th8;
            endDeferResume();
            throw e;
        }
    }

    /* access modifiers changed from: package-private */
    public void updateHomeProcess(WindowProcessController app) {
        if (app != null && this.mService.mHomeProcess != app) {
            if (!this.mHandler.hasMessages(REPORT_HOME_CHANGED_MSG)) {
                this.mHandler.sendEmptyMessage(REPORT_HOME_CHANGED_MSG);
            }
            ActivityTaskManagerService activityTaskManagerService = this.mService;
            activityTaskManagerService.mHomeProcess = app;
            activityTaskManagerService.mHwATMSEx.reportHomeProcess(this.mService.mHomeProcess);
        }
    }

    private void logIfTransactionTooLarge(Intent intent, Bundle icicle) {
        Bundle extras;
        int extrasSize = 0;
        if (!(intent == null || (extras = intent.getExtras()) == null)) {
            extrasSize = extras.getSize();
        }
        int icicleSize = icicle == null ? 0 : icicle.getSize();
        if (extrasSize + icicleSize > 200000) {
            Slog.e("ActivityTaskManager", "Transaction too large, intent: " + intent + ", extras size: " + extrasSize + ", icicle size: " + icicleSize);
        }
    }

    /* access modifiers changed from: protected */
    public void handlePCWindowStateChanged() {
    }

    /* access modifiers changed from: package-private */
    public void startSpecificActivityLocked(ActivityRecord r, boolean andResume, boolean checkConfig) {
        WindowProcessController wpc = this.mService.getProcessController(r.processName, r.info.applicationInfo.uid);
        boolean knownToBeDead = false;
        boolean isStartingLauncher = true;
        if (r.app != null) {
            this.mService.mHwATMSEx.noteActivityDisplayed(r.shortComponentName, r.app.mUid, r.app.mPid, true);
        }
        this.mActivityMetricsLogger.setLaunchBeginLog(r);
        if (wpc != null && wpc.hasThread()) {
            try {
                realStartActivityLocked(r, wpc, andResume, checkConfig);
                return;
            } catch (RemoteException e) {
                Slog.w("ActivityTaskManager", "Exception when starting activity " + r.intent.getComponent().flattenToShortString(), e);
                knownToBeDead = true;
            }
        }
        Flog.i(101, "start process for launching activity: " + r);
        if (r.intent.getComponent() == null || !DisplayPolicy.LAUNCHER_PACKAGE_NAME.equals(r.intent.getComponent().getPackageName())) {
            isStartingLauncher = false;
        }
        if (isStartingLauncher) {
            try {
                this.mService.getPackageManagerInternalLocked().checkPackageStartable(r.intent.getComponent().getPackageName(), UserHandle.getUserId(r.info.applicationInfo.uid));
            } catch (SecurityException e2) {
                Slog.i("ActivityTaskManager", "skip launch freezen hwLauncher for uid: " + r.info.applicationInfo.uid);
                return;
            }
        }
        if (!this.mService.mAmInternal.hasStartedUserState(r.mUserId)) {
            Slog.w("ActivityTaskManager", "skip launch r : " + r + ": user " + r.mUserId + " is stopped");
            return;
        }
        if (getKeyguardController().isKeyguardLocked()) {
            r.notifyUnknownVisibilityLaunched();
        }
        try {
            if (Trace.isTagEnabled(64)) {
                Trace.traceBegin(64, "dispatchingStartProcess:" + r.processName);
            }
            this.mService.mH.sendMessage(PooledLambda.obtainMessage($$Lambda$3W4Y_XVQUddVKzLjibuHW7h0R1g.INSTANCE, this.mService.mAmInternal, r.processName, r.info.applicationInfo, Boolean.valueOf(knownToBeDead), "activity", r.intent.getComponent()));
        } finally {
            Trace.traceEnd(64);
        }
    }

    /* access modifiers changed from: protected */
    public boolean startProcessOnExtDisplay(ActivityRecord r) {
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean checkStartAnyActivityPermission(Intent intent, ActivityInfo aInfo, String resultWho, int requestCode, int callingPid, int callingUid, String callingPackage, boolean ignoreTargetSecurity, boolean launchingInTask, WindowProcessController callerApp, ActivityRecord resultRecord, ActivityStack resultStack) {
        String msg;
        boolean isCallerRecents = this.mService.getRecentTasks() != null && this.mService.getRecentTasks().isCallerRecents(callingUid);
        ActivityTaskManagerService activityTaskManagerService = this.mService;
        if (ActivityTaskManagerService.checkPermission("android.permission.START_ANY_ACTIVITY", callingPid, callingUid) == 0) {
            return true;
        }
        if (isCallerRecents && launchingInTask) {
            return true;
        }
        int componentRestriction = getComponentRestrictionForCallingPackage(aInfo, callingPackage, callingPid, callingUid, ignoreTargetSecurity);
        int actionRestriction = getActionRestrictionForCallingPackage(intent.getAction(), callingPackage, callingPid, callingUid);
        if (componentRestriction == 1 || actionRestriction == 1) {
            if (resultRecord != null) {
                resultStack.sendActivityResultLocked(-1, resultRecord, resultWho, requestCode, 0, null);
            }
            if (actionRestriction == 1) {
                msg = "Permission Denial: starting " + intent.toString() + " from " + callerApp + " (pid=" + callingPid + ", uid=" + callingUid + ") with revoked permission " + ACTION_TO_RUNTIME_PERMISSION.get(intent.getAction());
            } else if (!aInfo.exported) {
                msg = "Permission Denial: starting " + intent.toString() + " from " + callerApp + " (pid=" + callingPid + ", uid=" + callingUid + ") not exported from uid " + aInfo.applicationInfo.uid;
            } else {
                msg = "Permission Denial: starting " + intent.toString() + " from " + callerApp + " (pid=" + callingPid + ", uid=" + callingUid + ") requires " + aInfo.permission;
            }
            Slog.w("ActivityTaskManager", msg);
            throw new SecurityException(msg);
        } else if (actionRestriction == 2) {
            Slog.w("ActivityTaskManager", "Appop Denial: starting " + intent.toString() + " from " + callerApp + " (pid=" + callingPid + ", uid=" + callingUid + ") requires " + AppOpsManager.permissionToOp(ACTION_TO_RUNTIME_PERMISSION.get(intent.getAction())));
            return false;
        } else if (componentRestriction != 2) {
            return true;
        } else {
            Slog.w("ActivityTaskManager", "Appop Denial: starting " + intent.toString() + " from " + callerApp + " (pid=" + callingPid + ", uid=" + callingUid + ") requires appop " + AppOpsManager.permissionToOp(aInfo.permission));
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isCallerAllowedToLaunchOnDisplay(int callingPid, int callingUid, int launchDisplayId, ActivityInfo aInfo) {
        if (ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
            Slog.d("ActivityTaskManager", "Launch on display check: displayId=" + launchDisplayId + " callingPid=" + callingPid + " callingUid=" + callingUid);
        }
        if (callingPid == -1 && callingUid == -1) {
            if (ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
                Slog.d("ActivityTaskManager", "Launch on display check: no caller info, skip check");
            }
            return true;
        }
        ActivityDisplay activityDisplay = this.mRootActivityContainer.getActivityDisplayOrCreate(launchDisplayId);
        if (activityDisplay == null || activityDisplay.isRemoved()) {
            Slog.w("ActivityTaskManager", "Launch on display check: display not found");
            return false;
        }
        ActivityTaskManagerService activityTaskManagerService = this.mService;
        if (ActivityTaskManagerService.checkPermission("android.permission.INTERNAL_SYSTEM_WINDOW", callingPid, callingUid) == 0) {
            if (ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
                Slog.d("ActivityTaskManager", "Launch on display check: allow launch any on display");
            }
            return true;
        }
        boolean uidPresentOnDisplay = activityDisplay.isUidPresent(callingUid);
        int displayOwnerUid = activityDisplay.mDisplay.getOwnerUid();
        if (activityDisplay.mDisplay.getType() == 5 && displayOwnerUid != 1000 && displayOwnerUid != aInfo.applicationInfo.uid && !HwPCUtils.isPcCastModeInServer()) {
            if ((aInfo.flags & Integer.MIN_VALUE) == 0) {
                if (ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
                    Slog.d("ActivityTaskManager", "Launch on display check: disallow launch on virtual display for not-embedded activity.");
                }
                return false;
            }
            ActivityTaskManagerService activityTaskManagerService2 = this.mService;
            if (ActivityTaskManagerService.checkPermission("android.permission.ACTIVITY_EMBEDDING", callingPid, callingUid) == -1 && !uidPresentOnDisplay) {
                if (ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
                    Slog.d("ActivityTaskManager", "Launch on display check: disallow activity embedding without permission.");
                }
                return false;
            }
        }
        if (!activityDisplay.isPrivate()) {
            if (ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
                Slog.d("ActivityTaskManager", "Launch on display check: allow launch on public display");
            }
            return true;
        } else if (displayOwnerUid == callingUid) {
            if (ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
                Slog.d("ActivityTaskManager", "Launch on display check: allow launch for owner of the display");
            }
            return true;
        } else if (uidPresentOnDisplay) {
            if (ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
                Slog.d("ActivityTaskManager", "Launch on display check: allow launch for caller present on the display");
            }
            return true;
        } else if (!HwPCUtils.isHiCarCastMode() || (!DisplayManagerGlobal.getInstance().isUidPresentOnDisplay(callingUid, launchDisplayId) && !HwPCUtils.isValidExtDisplayId(launchDisplayId))) {
            Slog.w("ActivityTaskManager", "Launch on display check: denied");
            return false;
        } else {
            if (ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
                Slog.d("ActivityTaskManager", "Launch on display check: allow launch for hicar mode on the display");
            }
            return true;
        }
    }

    /* access modifiers changed from: package-private */
    public UserInfo getUserInfo(int userId) {
        long identity = Binder.clearCallingIdentity();
        try {
            return UserManager.get(this.mService.mContext).getUserInfo(userId);
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    private int getComponentRestrictionForCallingPackage(ActivityInfo activityInfo, String callingPackage, int callingPid, int callingUid, boolean ignoreTargetSecurity) {
        int opCode;
        if (!ignoreTargetSecurity) {
            ActivityTaskManagerService activityTaskManagerService = this.mService;
            if (ActivityTaskManagerService.checkComponentPermission(activityInfo.permission, callingPid, callingUid, activityInfo.applicationInfo.uid, activityInfo.exported) == -1) {
                return 1;
            }
        }
        if (activityInfo.permission == null || (opCode = AppOpsManager.permissionToOpCode(activityInfo.permission)) == -1 || this.mService.getAppOpsService().noteOperation(opCode, callingUid, callingPackage) == 0 || ignoreTargetSecurity) {
            return 0;
        }
        return 2;
    }

    private int getActionRestrictionForCallingPackage(String action, String callingPackage, int callingPid, int callingUid) {
        String permission;
        if (action == null || (permission = ACTION_TO_RUNTIME_PERMISSION.get(action)) == null) {
            return 0;
        }
        try {
            if (!ArrayUtils.contains(this.mService.mContext.getPackageManager().getPackageInfo(callingPackage, 4096).requestedPermissions, permission)) {
                return 0;
            }
            ActivityTaskManagerService activityTaskManagerService = this.mService;
            if (ActivityTaskManagerService.checkPermission(permission, callingPid, callingUid) == -1) {
                return 1;
            }
            int opCode = AppOpsManager.permissionToOpCode(permission);
            if (opCode == -1 || this.mService.getAppOpsService().noteOperation(opCode, callingUid, callingPackage) == 0) {
                return 0;
            }
            return 2;
        } catch (PackageManager.NameNotFoundException e) {
            Slog.i("ActivityTaskManager", "Cannot find package info for " + callingPackage);
            return 0;
        }
    }

    /* access modifiers changed from: package-private */
    public void setLaunchSource(int uid) {
        this.mLaunchingActivityWakeLock.setWorkSource(new WorkSource(uid));
    }

    /* access modifiers changed from: package-private */
    public void acquireLaunchWakelock() {
        this.mLaunchingActivityWakeLock.acquire();
        if (!this.mHandler.hasMessages(LAUNCH_TIMEOUT_MSG)) {
            this.mHandler.sendEmptyMessageDelayed(LAUNCH_TIMEOUT_MSG, 10000);
        }
        sendLaunchTimeOutResume();
    }

    /* access modifiers changed from: package-private */
    public void sendLaunchTimeOutResume() {
        ActivityStack mFocusedStack = this.mRootActivityContainer.getTopDisplayFocusedStack();
        if (mFocusedStack != null) {
            ActivityRecord next = mFocusedStack.topRunningActivityLocked(true);
            if (next == null) {
                Slog.w("ActivityTaskManager", "send launch timeout, null top activity");
            } else if (!next.isState(ActivityStack.ActivityState.RESUMED)) {
                this.mHandler.removeMessages(LAUNCH_TIMEOUT_RESUME_MSG);
                Message msg = Message.obtain();
                Bundle bundle = new Bundle();
                bundle.putString(IZRHungService.PARA_PACKAGENAME, next.packageName);
                msg.setData(bundle);
                msg.what = LAUNCH_TIMEOUT_RESUME_MSG;
                this.mHandler.sendMessageDelayed(msg, 5000);
            }
        }
    }

    @GuardedBy({"mService"})
    private boolean checkFinishBootingLocked() {
        boolean booting = this.mService.isBooting();
        boolean enableScreen = false;
        this.mService.setBooting(false);
        if (!this.mService.isBooted()) {
            this.mService.setBooted(true);
            enableScreen = true;
        }
        if (booting || enableScreen) {
            this.mService.postFinishBooting(booting, enableScreen);
        }
        return booting;
    }

    /* JADX DEBUG: Multi-variable search result rejected for r11v5, resolved type: com.android.server.wm.ActivityStack */
    /* JADX DEBUG: Multi-variable search result rejected for r1v7, resolved type: com.android.server.wm.RootActivityContainer */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r0v2, types: [boolean, int] */
    /* JADX WARN: Type inference failed for: r0v12 */
    /* JADX WARN: Type inference failed for: r0v18 */
    /* access modifiers changed from: package-private */
    /* JADX WARNING: Unknown variable types count: 1 */
    @GuardedBy({"mService"})
    public final ActivityRecord activityIdleInternalLocked(IBinder token, boolean fromTimeout, boolean processPausingActivities, Configuration config) {
        boolean z;
        ?? r0;
        int NS;
        Slog.v("ActivityTaskManager", "Activity idle: " + token);
        ArrayList<ActivityRecord> finishes = null;
        ArrayList<UserState> startingUsers = null;
        boolean booting = false;
        boolean activityRemoved = false;
        ActivityRecord r = ActivityRecord.forTokenLocked(token);
        if (r != null) {
            if (ActivityTaskManagerDebugConfig.DEBUG_IDLE) {
                Slog.d("ActivityTaskManager", "activityIdleInternalLocked: Callers=" + Debug.getCallers(4));
            }
            this.mHandler.removeMessages(IDLE_TIMEOUT_MSG, r);
            r.finishLaunchTickingLocked();
            if (fromTimeout) {
                z = true;
                reportActivityLaunchedLocked(fromTimeout, r, -1, -1);
            } else {
                z = true;
            }
            if (config != null) {
                r.setLastReportedGlobalConfiguration(config);
            }
            r.idle = z;
            if (r.app != null && r.app.hasForegroundActivities()) {
                this.mService.mHwATMSEx.noteActivityStart(r.appInfo.packageName, r.appInfo.processName, r.mActivityComponent != null ? r.mActivityComponent.getClassName() : "NULL", r.app.getPid(), r.appInfo.uid, false);
            }
            if ((this.mService.isBooting() && this.mRootActivityContainer.allResumedActivitiesIdle()) || fromTimeout) {
                booting = checkFinishBootingLocked();
            }
            r0 = 0;
            r.mRelaunchReason = 0;
        } else {
            z = true;
            r0 = 0;
        }
        if (this.mRootActivityContainer.allResumedActivitiesIdle()) {
            if (r != null) {
                this.mService.scheduleAppGcsLocked();
            }
            if (this.mLaunchingActivityWakeLock.isHeld()) {
                this.mHandler.removeMessages(LAUNCH_TIMEOUT_MSG);
                this.mHandler.removeMessages(LAUNCH_TIMEOUT_RESUME_MSG);
                this.mLaunchingActivityWakeLock.release();
            }
            this.mRootActivityContainer.ensureActivitiesVisible(null, r0, r0);
        }
        ArrayList<ActivityRecord> stops = processStoppingActivitiesLocked(r, z, processPausingActivities);
        if (stops != null) {
            NS = stops.size();
        } else {
            int i = r0 == true ? 1 : 0;
            Object[] objArr = r0 == true ? 1 : 0;
            Object[] objArr2 = r0 == true ? 1 : 0;
            NS = i;
        }
        int NF = this.mFinishingActivities.size();
        if (NF > 0) {
            finishes = new ArrayList<>(this.mFinishingActivities);
            this.mFinishingActivities.clear();
        }
        if (this.mStartingUsers.size() > 0) {
            startingUsers = new ArrayList<>(this.mStartingUsers);
            this.mStartingUsers.clear();
        }
        for (int i2 = 0; i2 < NS; i2++) {
            r = stops.get(i2);
            ActivityStack stack = r.getActivityStack();
            if (stack != 0) {
                if (r.finishing) {
                    int i3 = r0 == true ? 1 : 0;
                    int i4 = r0 == true ? 1 : 0;
                    int i5 = r0 == true ? 1 : 0;
                    stack.finishCurrentActivityLocked(r, i3, r0, "activityIdleInternalLocked");
                } else {
                    stack.stopActivityLocked(r);
                }
            }
        }
        for (int i6 = 0; i6 < NF; i6++) {
            r = finishes.get(i6);
            ActivityStack stack2 = r.getActivityStack();
            if (stack2 != null) {
                activityRemoved = stack2.destroyActivityLocked(r, z, "finish-idle") | activityRemoved;
            }
        }
        if (!booting && startingUsers != null) {
            for (int i7 = 0; i7 < startingUsers.size(); i7++) {
                this.mService.mAmInternal.finishUserSwitch(startingUsers.get(i7));
            }
        }
        this.mService.mH.post(new Runnable() {
            /* class com.android.server.wm.$$Lambda$ActivityStackSupervisor$28Zuzbi6usdgbDcOi8hrJg6nZO0 */

            @Override // java.lang.Runnable
            public final void run() {
                ActivityStackSupervisor.this.lambda$activityIdleInternalLocked$0$ActivityStackSupervisor();
            }
        });
        if (activityRemoved) {
            this.mRootActivityContainer.resumeFocusedStacksTopActivities();
        }
        return r;
    }

    public /* synthetic */ void lambda$activityIdleInternalLocked$0$ActivityStackSupervisor() {
        this.mService.mAmInternal.trimApplications();
    }

    /* access modifiers changed from: package-private */
    public void findTaskToMoveToFront(TaskRecord task, int flags, ActivityOptions options, String reason, boolean forceNonResizeable) {
        AppTimeTracker appTimeTracker;
        Rect bounds;
        ActivityStack currentStack = task.getStack();
        if (currentStack == null) {
            Slog.e("ActivityTaskManager", "findTaskToMoveToFront: can't move task=" + task + " to front. Stack is null");
            return;
        }
        if ((flags & 2) == 0) {
            this.mUserLeaving = true;
        }
        String reason2 = reason + " findTaskToMoveToFront";
        boolean reparented = false;
        if (!task.isResizeable() || !canUseActivityOptionsLaunchBounds(options)) {
            appTimeTracker = null;
        } else {
            Rect bounds2 = options.getLaunchBounds();
            task.updateOverrideConfiguration(bounds2);
            ActivityStack stack = this.mRootActivityContainer.getLaunchStack(null, options, task, true);
            if (stack != currentStack) {
                moveHomeStackToFrontIfNeeded(flags, stack.getDisplay(), reason2);
                bounds = bounds2;
                appTimeTracker = null;
                task.reparent(stack, true, 1, false, true, reason2);
                currentStack = stack;
                reparented = true;
            } else {
                bounds = bounds2;
                appTimeTracker = null;
            }
            if (stack.resizeStackWithLaunchBounds()) {
                this.mRootActivityContainer.resizeStack(stack, bounds, null, null, false, true, false);
            } else {
                task.resizeWindowContainer();
            }
        }
        if (!reparented) {
            moveHomeStackToFrontIfNeeded(flags, currentStack.getDisplay(), reason2);
        }
        ActivityRecord r = task.getTopActivity();
        if (r != null) {
            appTimeTracker = r.appTimeTracker;
        }
        currentStack.moveTaskToFrontLocked(task, false, options, appTimeTracker, reason2);
        if (ActivityTaskManagerDebugConfig.DEBUG_STACK) {
            Slog.d("ActivityTaskManager", "findTaskToMoveToFront: moved to front of stack=" + currentStack);
        }
        handleNonResizableTaskIfNeeded(task, 0, 0, currentStack, forceNonResizeable);
    }

    private void moveHomeStackToFrontIfNeeded(int flags, ActivityDisplay display, String reason) {
        ActivityStack focusedStack = display.getFocusedStack();
        if ((display.getWindowingMode() == 1 && (flags & 1) != 0) || (focusedStack != null && focusedStack.isActivityTypeRecents())) {
            display.moveHomeStackToFront(reason);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean canUseActivityOptionsLaunchBounds(ActivityOptions options) {
        if (options == null || options.getLaunchBounds() == null) {
            return false;
        }
        if ((!this.mService.mSupportsPictureInPicture || options.getLaunchWindowingMode() != 2) && !this.mService.mSupportsFreeformWindowManagement) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public LaunchParamsController getLaunchParamsController() {
        return this.mLaunchParamsController;
    }

    private void deferUpdateRecentsHomeStackBounds() {
        this.mRootActivityContainer.deferUpdateBounds(3);
        this.mRootActivityContainer.deferUpdateBounds(2);
    }

    private void continueUpdateRecentsHomeStackBounds() {
        this.mRootActivityContainer.continueUpdateBounds(3);
        this.mRootActivityContainer.continueUpdateBounds(2);
    }

    /* access modifiers changed from: package-private */
    public void notifyAppTransitionDone() {
        continueUpdateRecentsHomeStackBounds();
        for (int i = this.mResizingTasksDuringAnimation.size() - 1; i >= 0; i--) {
            TaskRecord task = this.mRootActivityContainer.anyTaskForId(this.mResizingTasksDuringAnimation.valueAt(i).intValue(), 0);
            if (task != null) {
                try {
                    task.setTaskDockedResizing(false);
                } catch (IllegalArgumentException e) {
                }
            }
        }
        this.mResizingTasksDuringAnimation.clear();
    }

    /* JADX INFO: Multiple debug info for r7v4 'task'  com.android.server.wm.TaskRecord: [D('i' int), D('task' com.android.server.wm.TaskRecord)] */
    /* access modifiers changed from: private */
    /* renamed from: moveTasksToFullscreenStackInSurfaceTransaction */
    public void lambda$moveTasksToFullscreenStackLocked$1$ActivityStackSupervisor(ActivityStack fromStack, int toDisplayId, boolean onTop) {
        Throwable th;
        ActivityDisplay toDisplay;
        ArrayList<TaskRecord> tasks;
        int size;
        int i;
        TaskRecord task;
        this.mWindowManager.deferSurfaceLayout();
        try {
            int windowingMode = fromStack.getWindowingMode();
            boolean inPinnedWindowingMode = windowingMode == 2;
            try {
                ActivityDisplay toDisplay2 = this.mRootActivityContainer.getActivityDisplay(toDisplayId);
                if (windowingMode == 3) {
                    toDisplay2.onExitingSplitScreenMode();
                    boolean isImeVisible = toDisplay2.mDisplayContent != null && toDisplay2.mDisplayContent.isImeVisible();
                    for (int i2 = toDisplay2.getChildCount() - 1; i2 >= 0; i2--) {
                        ActivityStack otherStack = toDisplay2.getChildAt(i2);
                        if (otherStack.inSplitScreenSecondaryWindowingMode()) {
                            if (isImeVisible && otherStack.getTaskStack() != null) {
                                otherStack.getTaskStack().clearAdjustedBounds();
                            }
                            if (!this.mService.mHwATMSEx.isSwitchToMagicWin(otherStack.getStackId(), false, getConfiguration().orientation)) {
                                otherStack.setWindowingMode(0);
                            }
                        }
                    }
                    this.mAllowDockedStackResize = false;
                }
                ArrayList<TaskRecord> tasks2 = fromStack.getAllTasks();
                if (!tasks2.isEmpty()) {
                    this.mTmpOptions.setLaunchWindowingMode(1);
                    int size2 = tasks2.size();
                    int i3 = 0;
                    while (i3 < size2) {
                        TaskRecord task2 = tasks2.get(i3);
                        ActivityStack toStack = toDisplay2.getOrCreateStack(null, this.mTmpOptions, task2, task2.getActivityType(), onTop);
                        if (onTop) {
                            i = i3;
                            size = size2;
                            tasks = tasks2;
                            toDisplay = toDisplay2;
                            task2.reparent(toStack, true, 0, i3 == size2 + -1, true, inPinnedWindowingMode, "moveTasksToFullscreenStack - onTop");
                            task = task2;
                            MetricsLoggerWrapper.logPictureInPictureFullScreen(this.mService.mContext, task.effectiveUid, task.realActivity.flattenToString());
                        } else {
                            i = i3;
                            size = size2;
                            tasks = tasks2;
                            toDisplay = toDisplay2;
                            task = task2;
                            task.reparent(toStack, true, 2, false, true, inPinnedWindowingMode, "moveTasksToFullscreenStack - NOT_onTop");
                        }
                        this.mService.mHwATMSEx.isSwitchToMagicWin(task.getStack().getStackId(), false, getConfiguration().orientation);
                        i3 = i + 1;
                        size2 = size;
                        tasks2 = tasks;
                        toDisplay2 = toDisplay;
                    }
                }
                this.mRootActivityContainer.ensureActivitiesVisible(null, 0, true);
                this.mRootActivityContainer.resumeFocusedStacksTopActivities();
                this.mAllowDockedStackResize = true;
                this.mWindowManager.continueSurfaceLayout();
            } catch (Throwable th2) {
                th = th2;
                this.mAllowDockedStackResize = true;
                this.mWindowManager.continueSurfaceLayout();
                throw th;
            }
        } catch (Throwable th3) {
            th = th3;
            this.mAllowDockedStackResize = true;
            this.mWindowManager.continueSurfaceLayout();
            throw th;
        }
    }

    /* access modifiers changed from: package-private */
    public void moveTasksToFullscreenStackLocked(ActivityStack fromStack, boolean onTop) {
        moveTasksToFullscreenStackLocked(fromStack, 0, onTop);
    }

    /* access modifiers changed from: package-private */
    public void moveTasksToFullscreenStackLocked(ActivityStack fromStack, int toDisplayId, boolean onTop) {
        this.mWindowManager.inSurfaceTransaction(new Runnable(fromStack, toDisplayId, onTop) {
            /* class com.android.server.wm.$$Lambda$ActivityStackSupervisor$PHIj4FpzoLIwUTmMRMOYA9us0rc */
            private final /* synthetic */ ActivityStack f$1;
            private final /* synthetic */ int f$2;
            private final /* synthetic */ boolean f$3;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
            }

            @Override // java.lang.Runnable
            public final void run() {
                ActivityStackSupervisor.this.lambda$moveTasksToFullscreenStackLocked$1$ActivityStackSupervisor(this.f$1, this.f$2, this.f$3);
            }
        });
    }

    /* access modifiers changed from: package-private */
    public void setSplitScreenResizing(boolean resizing) {
        if (resizing != this.mDockedStackResizing) {
            this.mDockedStackResizing = resizing;
            this.mWindowManager.setDockedStackResizing(resizing);
            if (!resizing && this.mHasPendingDockedBounds) {
                resizeDockedStackLocked(this.mPendingDockedBounds, this.mPendingTempDockedTaskBounds, this.mPendingTempDockedTaskInsetBounds, this.mPendingTempOtherTaskBounds, this.mPendingTempOtherTaskInsetBounds, true);
                this.mHasPendingDockedBounds = false;
                this.mPendingDockedBounds = null;
                this.mPendingTempDockedTaskBounds = null;
                this.mPendingTempDockedTaskInsetBounds = null;
                this.mPendingTempOtherTaskBounds = null;
                this.mPendingTempOtherTaskInsetBounds = null;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void resizeDockedStackLocked(Rect dockedBounds, Rect tempDockedTaskBounds, Rect tempDockedTaskInsetBounds, Rect tempOtherTaskBounds, Rect tempOtherTaskInsetBounds, boolean preserveWindows) {
        resizeDockedStackLocked(dockedBounds, tempDockedTaskBounds, tempDockedTaskInsetBounds, tempOtherTaskBounds, tempOtherTaskInsetBounds, preserveWindows, false);
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:47:0x00d6  */
    /* JADX WARNING: Removed duplicated region for block: B:51:0x00de  */
    public void resizeDockedStackLocked(Rect dockedBounds, Rect tempDockedTaskBounds, Rect tempDockedTaskInsetBounds, Rect tempOtherTaskBounds, Rect tempOtherTaskInsetBounds, boolean preserveWindows, boolean deferResume) {
        Throwable th;
        if (this.mAllowDockedStackResize) {
            ActivityStack stack = this.mRootActivityContainer.getDefaultDisplay().getSplitScreenPrimaryStack();
            if (stack == null) {
                Slog.w("ActivityTaskManager", "resizeDockedStackLocked: docked stack not found");
                return;
            }
            if (this.mDockedStackResizing) {
                this.mHasPendingDockedBounds = true;
                this.mPendingDockedBounds = Rect.copyOrNull(dockedBounds);
                this.mPendingTempDockedTaskBounds = Rect.copyOrNull(tempDockedTaskBounds);
                this.mPendingTempDockedTaskInsetBounds = Rect.copyOrNull(tempDockedTaskInsetBounds);
                this.mPendingTempOtherTaskBounds = Rect.copyOrNull(tempOtherTaskBounds);
                this.mPendingTempOtherTaskInsetBounds = Rect.copyOrNull(tempOtherTaskInsetBounds);
            }
            Trace.traceBegin(64, "am.resizeDockedStack");
            this.mWindowManager.deferSurfaceLayout();
            try {
                this.mAllowDockedStackResize = false;
                ActivityRecord r = stack.topRunningActivityLocked();
                try {
                    stack.resize(dockedBounds, tempDockedTaskBounds, tempDockedTaskInsetBounds);
                    if (stack.getWindowingMode() != 1) {
                        if (dockedBounds != null || stack.isAttached()) {
                            ActivityDisplay display = this.mRootActivityContainer.getDefaultDisplay();
                            Rect otherTaskRect = new Rect();
                            for (int i = display.getChildCount() - 1; i >= 0; i--) {
                                ActivityStack current = display.getChildAt(i);
                                if (current.inSplitScreenSecondaryWindowingMode()) {
                                    if (current.affectedBySplitScreenResize()) {
                                        if (!this.mDockedStackResizing || current.isTopActivityVisible()) {
                                            current.getStackDockedModeBounds(dockedBounds, tempOtherTaskBounds, this.tempRect, otherTaskRect);
                                            this.mRootActivityContainer.resizeStack(current, !this.tempRect.isEmpty() ? this.tempRect : null, !otherTaskRect.isEmpty() ? otherTaskRect : tempOtherTaskBounds, tempOtherTaskInsetBounds, preserveWindows, true, deferResume);
                                        }
                                    }
                                }
                            }
                            if (deferResume) {
                                try {
                                    stack.ensureVisibleActivitiesConfigurationLocked(r, preserveWindows);
                                } catch (Throwable th2) {
                                    th = th2;
                                }
                            }
                            this.mAllowDockedStackResize = true;
                            this.mWindowManager.continueSurfaceLayout();
                            Trace.traceEnd(64);
                        }
                    }
                    moveTasksToFullscreenStackLocked(stack, true);
                    r = null;
                    if (deferResume) {
                    }
                    this.mAllowDockedStackResize = true;
                    this.mWindowManager.continueSurfaceLayout();
                    Trace.traceEnd(64);
                } catch (Throwable th3) {
                    th = th3;
                    this.mAllowDockedStackResize = true;
                    this.mWindowManager.continueSurfaceLayout();
                    Trace.traceEnd(64);
                    throw th;
                }
            } catch (Throwable th4) {
                th = th4;
                this.mAllowDockedStackResize = true;
                this.mWindowManager.continueSurfaceLayout();
                Trace.traceEnd(64);
                throw th;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void resizePinnedStackLocked(Rect pinnedBounds, Rect tempPinnedTaskBounds) {
        if (!HwPCUtils.enabledInPad() || !HwPCUtils.isPcCastModeInServer()) {
            ActivityStack stack = this.mRootActivityContainer.getDefaultDisplay().getPinnedStack();
            if (stack == null) {
                Slog.w("ActivityTaskManager", "resizePinnedStackLocked: pinned stack not found");
            } else if (!stack.getTaskStack().pinnedStackResizeDisallowed()) {
                Trace.traceBegin(64, "am.resizePinnedStack");
                this.mWindowManager.deferSurfaceLayout();
                try {
                    ActivityRecord r = stack.topRunningActivityLocked();
                    Rect insetBounds = null;
                    if (tempPinnedTaskBounds != null && stack.isAnimatingBoundsToFullscreen()) {
                        insetBounds = this.tempRect;
                        insetBounds.top = 0;
                        insetBounds.left = 0;
                        insetBounds.right = tempPinnedTaskBounds.width();
                        insetBounds.bottom = tempPinnedTaskBounds.height();
                    }
                    if (pinnedBounds != null && tempPinnedTaskBounds == null) {
                        stack.onPipAnimationEndResize();
                    }
                    stack.resize(pinnedBounds, tempPinnedTaskBounds, insetBounds);
                    stack.ensureVisibleActivitiesConfigurationLocked(r, false);
                } finally {
                    this.mWindowManager.continueSurfaceLayout();
                    Trace.traceEnd(64);
                }
            }
        } else {
            HwPCUtils.log("ActivityTaskManager", "ignore pinned stack in pad pc mode");
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: removeStackInSurfaceTransaction */
    public void lambda$removeStack$2$ActivityStackSupervisor(ActivityStack stack) {
        ArrayList<TaskRecord> tasks = stack.getAllTasks();
        if (stack.getWindowingMode() == 2) {
            stack.mForceHidden = true;
            stack.ensureActivitiesVisibleLocked(null, 0, true);
            stack.mForceHidden = false;
            activityIdleInternalLocked(null, false, true, null);
            moveTasksToFullscreenStackLocked(stack, false);
            return;
        }
        for (int i = tasks.size() - 1; i >= 0; i--) {
            removeTaskByIdLocked(tasks.get(i).taskId, true, true, "remove-stack");
        }
    }

    /* access modifiers changed from: package-private */
    public void removeStack(ActivityStack stack) {
        this.mWindowManager.inSurfaceTransaction(new Runnable(stack) {
            /* class com.android.server.wm.$$Lambda$ActivityStackSupervisor$0u1RcpeZ6m0BHDGGv8EXroS3KyE */
            private final /* synthetic */ ActivityStack f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                ActivityStackSupervisor.this.lambda$removeStack$2$ActivityStackSupervisor(this.f$1);
            }
        });
    }

    /* access modifiers changed from: package-private */
    public boolean removeTaskByIdLocked(int taskId, boolean killProcess, boolean removeFromRecents, String reason) {
        return removeTaskByIdLocked(taskId, killProcess, removeFromRecents, false, reason);
    }

    /* access modifiers changed from: package-private */
    public boolean removeTaskByIdLocked(int taskId, boolean killProcess, boolean removeFromRecents, boolean pauseImmediately, String reason) {
        TaskRecord tr = this.mRootActivityContainer.anyTaskForId(taskId, 1);
        if (tr != null) {
            if (!(tr.getBaseIntent() == null || tr.getBaseIntent().getComponent() == null)) {
                String packageName = tr.getBaseIntent().getComponent().getPackageName();
                if (HwDeviceManager.disallowOp(3, packageName)) {
                    Slog.i("ActivityTaskManager", "[" + packageName + "] is Persistent app,won't be killed");
                    UiThread.getHandler().post(new Runnable() {
                        /* class com.android.server.wm.ActivityStackSupervisor.AnonymousClass1 */

                        @Override // java.lang.Runnable
                        public void run() {
                            Context context = ActivityStackSupervisor.this.mService.mUiContext;
                            if (context != null) {
                                Toast toast = Toast.makeText(context, context.getString(33686025), 0);
                                toast.getWindowParams().privateFlags |= 16;
                                toast.show();
                            }
                        }
                    });
                    return false;
                }
            }
            if (tr.getStack() != null) {
                tr.getStack().mHwActivityStackEx.resetOtherStacksVisible(true);
            }
            tr.removeTaskActivitiesLocked(pauseImmediately, reason);
            cleanUpRemovedTaskLocked(tr, killProcess, removeFromRecents);
            this.mService.getLockTaskController().clearLockedTask(tr);
            if (tr.isPersistable) {
                this.mService.notifyTaskPersisterLocked(null, true);
            }
            return true;
        }
        Slog.w("ActivityTaskManager", "Request to remove task ignored for non-existent task " + taskId);
        return false;
    }

    /* access modifiers changed from: package-private */
    public void cleanUpRemovedTaskLocked(TaskRecord tr, boolean killProcess, boolean removeFromRecents) {
        if (removeFromRecents) {
            this.mRecentTasks.remove(tr);
        }
        ComponentName component = tr.getBaseIntent().getComponent();
        if (component == null) {
            Slog.w("ActivityTaskManager", "No component for base intent of task: " + tr);
            return;
        }
        this.mService.mH.sendMessage(PooledLambda.obtainMessage($$Lambda$z5j5fiv3cZuY5AODkt3H3rhKimk.INSTANCE, this.mService.mAmInternal, Integer.valueOf(tr.userId), component, new Intent(tr.getBaseIntent())));
        if (killProcess) {
            String pkg = component.getPackageName();
            if (!shouldNotKillProcWhenRemoveTask(pkg)) {
                ArrayList<Object> procsToKill = new ArrayList<>();
                ArrayMap<String, SparseArray<WindowProcessController>> pmap = this.mService.mProcessNames.getMap();
                List<String> superWhiteListApp = HwDeviceManager.getList(22);
                for (int i = 0; i < pmap.size(); i++) {
                    SparseArray<WindowProcessController> uids = pmap.valueAt(i);
                    for (int j = 0; j < uids.size(); j++) {
                        WindowProcessController proc = uids.valueAt(j);
                        if (proc != null) {
                            if (superWhiteListApp != null && superWhiteListApp.contains(proc.mInfo.packageName)) {
                                Slog.i("ActivityTaskManager", "[" + proc.mInfo.packageName + "] is super-whitelist app,won't be killed by remove task");
                            } else if (proc.mUserId == tr.userId && proc != this.mService.mHomeProcess && proc.mPkgList.contains(pkg)) {
                                if (proc.shouldKillProcessForRemovedTask(tr) && !proc.hasForegroundServices()) {
                                    procsToKill.add(proc);
                                } else {
                                    return;
                                }
                            }
                        }
                    }
                }
                if (tr.inHwFreeFormWindowingMode()) {
                    Message m = Message.obtain();
                    ActivityTaskManagerService.H h = this.mService.mH;
                    m.what = 10087;
                    m.obj = procsToKill;
                    ActivityTaskManagerService.H h2 = this.mService.mH;
                    ActivityTaskManagerService.H h3 = this.mService.mH;
                    h2.sendMessageDelayed(m, 200);
                    return;
                }
                this.mService.mH.sendMessage(PooledLambda.obtainMessage($$Lambda$j9nJq2XXOKyN4f0dfDaTjqmQRvg.INSTANCE, this.mService.mAmInternal, procsToKill));
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean restoreRecentTaskLocked(TaskRecord task, ActivityOptions aOptions, boolean onTop) {
        ActivityStack stack = this.mRootActivityContainer.getLaunchStack(null, aOptions, task, onTop);
        ActivityStack currentStack = task.getStack();
        if (currentStack != null) {
            if (currentStack == stack) {
                return true;
            }
            currentStack.removeTask(task, "restoreRecentTaskLocked", 1);
        }
        stack.addTask(task, onTop, "restoreRecentTask");
        task.createTask(onTop, true);
        if (ActivityTaskManagerDebugConfig.DEBUG_RECENTS) {
            Slog.v("ActivityTaskManager", "Added restored task=" + task + " to stack=" + stack);
        }
        ArrayList<ActivityRecord> activities = task.mActivities;
        for (int activityNdx = activities.size() - 1; activityNdx >= 0; activityNdx--) {
            activities.get(activityNdx).createAppWindowToken(activities.get(activityNdx).info.navigationHide);
        }
        return true;
    }

    @Override // com.android.server.wm.RecentTasks.Callbacks
    public void onRecentTaskAdded(TaskRecord task) {
        task.touchActiveTime();
    }

    @Override // com.android.server.wm.RecentTasks.Callbacks
    public void onRecentTaskRemoved(TaskRecord task, boolean wasTrimmed, boolean killProcess) {
        if (wasTrimmed) {
            removeTaskByIdLocked(task.taskId, killProcess, false, false, "recent-task-trimmed");
        }
        task.removedFromRecents();
    }

    /* access modifiers changed from: package-private */
    public ActivityStack getReparentTargetStack(TaskRecord task, ActivityStack stack, boolean toTop) {
        ActivityStack prevStack = task.getStack();
        int stackId = stack.mStackId;
        boolean inMultiWindowMode = stack.inMultiWindowMode();
        if (prevStack != null && prevStack.mStackId == stackId) {
            Slog.w("ActivityTaskManager", "Can not reparent to same stack, task=" + task + " already in stackId=" + stackId);
            return prevStack;
        } else if (inMultiWindowMode && !this.mService.mSupportsMultiWindow) {
            throw new IllegalArgumentException("Device doesn't support multi-window, can not reparent task=" + task + " to stack=" + stack);
        } else if (stack.mDisplayId != 0 && !this.mService.mSupportsMultiDisplay) {
            throw new IllegalArgumentException("Device doesn't support multi-display, can not reparent task=" + task + " to stackId=" + stackId);
        } else if (stack.getWindowingMode() == 5 && !this.mService.mSupportsFreeformWindowManagement) {
            throw new IllegalArgumentException("Device doesn't support freeform, can not reparent task=" + task);
        } else if (!inMultiWindowMode || task.isResizeable()) {
            return stack;
        } else {
            Slog.w("ActivityTaskManager", "Can not move unresizeable task=" + task + " to multi-window stack=" + stack + " Moving to a fullscreen stack instead.");
            if (prevStack != null) {
                return prevStack;
            }
            return stack.getDisplay().createStack(1, stack.getActivityType(), toTop);
        }
    }

    /* access modifiers changed from: package-private */
    public void goingToSleepLocked() {
        scheduleSleepTimeout();
        if (!this.mGoingToSleepWakeLock.isHeld()) {
            this.mGoingToSleepWakeLock.acquire();
            if (this.mLaunchingActivityWakeLock.isHeld()) {
                this.mLaunchingActivityWakeLock.release();
                this.mHandler.removeMessages(LAUNCH_TIMEOUT_MSG);
                this.mHandler.removeMessages(LAUNCH_TIMEOUT_RESUME_MSG);
            }
        }
        this.mRootActivityContainer.applySleepTokens(false);
        checkReadyForSleepLocked(true);
        if (!this.mService.mShuttingDown) {
            this.mRootActivityContainer.getHwRootActivityContainerEx().checkStartAppLockActivity();
        }
    }

    /* access modifiers changed from: package-private */
    public boolean shutdownLocked(int timeout) {
        goingToSleepLocked();
        boolean timedout = false;
        long endTime = System.currentTimeMillis() + ((long) timeout);
        while (true) {
            if (this.mRootActivityContainer.putStacksToSleep(true, true)) {
                break;
            }
            long timeRemaining = endTime - System.currentTimeMillis();
            if (timeRemaining <= 0) {
                Slog.w("ActivityTaskManager", "Activity manager shutdown timed out");
                timedout = true;
                break;
            }
            try {
                this.mService.mGlobalLock.wait(timeRemaining);
            } catch (InterruptedException e) {
            }
        }
        checkReadyForSleepLocked(false);
        return timedout;
    }

    /* access modifiers changed from: package-private */
    public void comeOutOfSleepIfNeededLocked() {
        removeSleepTimeouts();
        if (this.mGoingToSleepWakeLock.isHeld()) {
            this.mGoingToSleepWakeLock.release();
        }
    }

    /* access modifiers changed from: package-private */
    public void activitySleptLocked(ActivityRecord r) {
        this.mGoingToSleepActivities.remove(r);
        ActivityStack s = r.getActivityStack();
        if (s != null) {
            s.checkReadyForSleep();
        } else {
            checkReadyForSleepLocked(true);
        }
    }

    /* access modifiers changed from: package-private */
    public void checkReadyForSleepLocked(boolean allowDelay) {
        if (this.mService.isSleepingOrShuttingDownLocked() && this.mRootActivityContainer.putStacksToSleep(allowDelay, false)) {
            this.mRootActivityContainer.sendPowerHintForLaunchEndIfNeeded();
            removeSleepTimeouts();
            if (this.mGoingToSleepWakeLock.isHeld()) {
                this.mGoingToSleepWakeLock.release();
            }
            if (this.mService.mShuttingDown) {
                this.mService.mGlobalLock.notifyAll();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean reportResumedActivityLocked(ActivityRecord r) {
        this.mStoppingActivities.remove(r);
        if (!r.getActivityStack().getDisplay().allResumedActivitiesComplete()) {
            return false;
        }
        this.mRootActivityContainer.ensureActivitiesVisible(null, 0, false);
        this.mRootActivityContainer.executeAppTransitionForAllDisplay();
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleLaunchTaskBehindCompleteLocked(ActivityRecord r) {
        TaskRecord task = r.getTaskRecord();
        ActivityStack stack = task.getStack();
        r.mLaunchTaskBehind = false;
        this.mRecentTasks.add(task);
        this.mService.getTaskChangeNotificationController().notifyTaskStackChanged();
        r.setVisibility(false);
        ActivityRecord top = stack.getTopActivity();
        if (top != null) {
            top.getTaskRecord().touchActiveTime();
        }
    }

    /* access modifiers changed from: package-private */
    public void scheduleLaunchTaskBehindComplete(IBinder token) {
        this.mHandler.obtainMessage(LAUNCH_TASK_BEHIND_COMPLETE, token).sendToTarget();
    }

    /* access modifiers changed from: package-private */
    public boolean isCurrentProfileLocked(int userId) {
        if (userId == this.mRootActivityContainer.mCurrentUser) {
            return true;
        }
        return this.mService.mAmInternal.isCurrentProfile(userId);
    }

    /* access modifiers changed from: package-private */
    public boolean isStoppingNoHistoryActivity() {
        Iterator<ActivityRecord> it = this.mStoppingActivities.iterator();
        while (it.hasNext()) {
            if (it.next().isNoHistory()) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public final ArrayList<ActivityRecord> processStoppingActivitiesLocked(ActivityRecord idleActivity, boolean remove, boolean processPausingActivities) {
        boolean shouldSleepOrShutDown;
        ArrayList<ActivityRecord> stops = null;
        boolean nowVisible = this.mRootActivityContainer.allResumedActivitiesVisible();
        for (int activityNdx = this.mStoppingActivities.size() - 1; activityNdx >= 0; activityNdx--) {
            ActivityRecord s = this.mStoppingActivities.get(activityNdx);
            boolean animating = s.mAppWindowToken.isSelfAnimating();
            if (ActivityTaskManagerDebugConfig.DEBUG_STATES || animating) {
                Slog.v("ActivityTaskManager", "Stopping " + s + ": nowVisible=" + nowVisible + " animating=" + animating + " finishing=" + s.finishing);
            }
            if (nowVisible && s.finishing) {
                if (ActivityTaskManagerDebugConfig.DEBUG_STATES) {
                    Slog.v("ActivityTaskManager", "Before stopping, can hide: " + s);
                }
                s.setVisibility(false);
            }
            if (remove) {
                ActivityStack stack = s.getActivityStack();
                if (stack != null) {
                    shouldSleepOrShutDown = stack.shouldSleepOrShutDownActivities();
                } else {
                    shouldSleepOrShutDown = this.mService.isSleepingOrShuttingDownLocked();
                }
                if (!animating || s.info.name.contains("splitscreen.SplitScreenAppActivity") || shouldSleepOrShutDown || HwPCUtils.isPcDynamicStack(s.getStackId())) {
                    if (processPausingActivities || !s.isState(ActivityStack.ActivityState.PAUSING)) {
                        if (ActivityTaskManagerDebugConfig.DEBUG_STATES) {
                            Slog.v("ActivityTaskManager", "Ready to stop: " + s);
                        }
                        if (stops == null) {
                            stops = new ArrayList<>();
                        }
                        stops.add(s);
                        this.mStoppingActivities.remove(activityNdx);
                    } else {
                        removeTimeoutsForActivityLocked(idleActivity);
                        scheduleIdleTimeoutLocked(idleActivity);
                    }
                }
            }
        }
        return stops;
    }

    public void dump(PrintWriter pw, String prefix) {
        pw.println();
        pw.println("ActivityStackSupervisor state:");
        this.mRootActivityContainer.dump(pw, prefix);
        pw.print(prefix);
        pw.println("mCurTaskIdForUser=" + this.mCurTaskIdForUser);
        pw.println(prefix + "mUserStackInFront=" + this.mRootActivityContainer.mUserStackInFront);
        if (!this.mWaitingForActivityVisible.isEmpty()) {
            pw.println(prefix + "mWaitingForActivityVisible=");
            for (int i = 0; i < this.mWaitingForActivityVisible.size(); i++) {
                pw.print(prefix + prefix);
                this.mWaitingForActivityVisible.get(i).dump(pw, prefix);
            }
        }
        pw.print(prefix);
        pw.print("isHomeRecentsComponent=");
        pw.print(this.mRecentTasks.isRecentsComponentHomeActivity(this.mRootActivityContainer.mCurrentUser));
        getKeyguardController().dump(pw, prefix);
        this.mService.getLockTaskController().dump(pw, prefix);
    }

    static boolean printThisActivity(PrintWriter pw, ActivityRecord activity, String dumpPackage, boolean needSep, String prefix) {
        if (activity == null) {
            return false;
        }
        if (dumpPackage != null && !dumpPackage.equals(activity.packageName)) {
            return false;
        }
        if (needSep) {
            pw.println();
        }
        pw.print(prefix);
        pw.println(activity);
        return true;
    }

    /* JADX INFO: Multiple debug info for r4v9 'lastTask'  com.android.server.wm.TaskRecord: [D('lastTask' com.android.server.wm.TaskRecord), D('tp' com.android.internal.os.TransferPipe)] */
    static boolean dumpHistoryList(FileDescriptor fd, PrintWriter pw, List<ActivityRecord> list, String prefix, String label, boolean complete, boolean brief, boolean client, String dumpPackage, boolean needNL, String header, TaskRecord lastTask) {
        String header2;
        TaskRecord lastTask2;
        IOException e;
        TaskRecord lastTask3;
        Throwable th;
        String str = prefix;
        String str2 = dumpPackage;
        boolean printed = false;
        boolean z = true;
        int i = list.size() - 1;
        boolean needNL2 = needNL;
        String innerPrefix = null;
        String[] args = null;
        String header3 = header;
        TaskRecord lastTask4 = lastTask;
        while (i >= 0) {
            ActivityRecord r = list.get(i);
            if (str2 == null || str2.equals(r.packageName)) {
                boolean full = false;
                if (innerPrefix == null) {
                    innerPrefix = str + "      ";
                    args = new String[0];
                }
                printed = true;
                if (!brief && (complete || !r.isInHistory())) {
                    full = z;
                }
                if (needNL2) {
                    pw.println("");
                    needNL2 = false;
                }
                if (header3 != null) {
                    pw.println(header3);
                    header2 = null;
                } else {
                    header2 = header3;
                }
                if (lastTask4 != r.getTaskRecord()) {
                    lastTask4 = r.getTaskRecord();
                    pw.print(str);
                    pw.print(full ? "* " : "  ");
                    pw.println(lastTask4);
                    if (full) {
                        lastTask4.dump(pw, str + "  ");
                    } else if (!(!complete || lastTask4 == null || lastTask4.intent == null)) {
                        pw.print(str);
                        pw.print("  ");
                        pw.println(lastTask4.intent.toInsecureStringWithClip());
                    }
                }
                pw.print(str);
                pw.print(full ? "  * " : "    ");
                pw.print(label);
                pw.print(" #");
                pw.print(i);
                pw.print(": ");
                pw.println(r);
                if (full) {
                    r.dump(pw, innerPrefix);
                } else if (complete) {
                    pw.print(innerPrefix);
                    pw.println(r.intent.toInsecureString());
                    if (r.app != null) {
                        pw.print(innerPrefix);
                        pw.println(r.app);
                    }
                }
                if (!client || !r.attachedToProcess()) {
                    lastTask4 = lastTask4;
                    header3 = header2;
                } else {
                    pw.flush();
                    try {
                        lastTask2 = new TransferPipe();
                        try {
                            r.app.getThread().dumpActivity(lastTask2.getWriteFd(), r.appToken, innerPrefix, args);
                            lastTask3 = lastTask2;
                            lastTask2 = lastTask4;
                            try {
                                lastTask3.go(fd, 2000);
                                try {
                                    lastTask3.kill();
                                } catch (IOException e2) {
                                    e = e2;
                                    pw.println(innerPrefix + "Failure while dumping the activity: " + e);
                                    lastTask4 = lastTask2;
                                    needNL2 = true;
                                    header3 = header2;
                                    i--;
                                    str = prefix;
                                    str2 = dumpPackage;
                                    z = true;
                                } catch (RemoteException e3) {
                                    pw.println(innerPrefix + "Got a RemoteException while dumping the activity");
                                    lastTask4 = lastTask2;
                                    needNL2 = true;
                                    header3 = header2;
                                    i--;
                                    str = prefix;
                                    str2 = dumpPackage;
                                    z = true;
                                }
                            } catch (Throwable th2) {
                                th = th2;
                                lastTask3.kill();
                                throw th;
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            lastTask3 = lastTask2;
                            lastTask3.kill();
                            throw th;
                        }
                    } catch (IOException e4) {
                        e = e4;
                        lastTask2 = lastTask4;
                        pw.println(innerPrefix + "Failure while dumping the activity: " + e);
                        lastTask4 = lastTask2;
                        needNL2 = true;
                        header3 = header2;
                        i--;
                        str = prefix;
                        str2 = dumpPackage;
                        z = true;
                    } catch (RemoteException e5) {
                        lastTask2 = lastTask4;
                        pw.println(innerPrefix + "Got a RemoteException while dumping the activity");
                        lastTask4 = lastTask2;
                        needNL2 = true;
                        header3 = header2;
                        i--;
                        str = prefix;
                        str2 = dumpPackage;
                        z = true;
                    }
                    lastTask4 = lastTask2;
                    needNL2 = true;
                    header3 = header2;
                }
            }
            i--;
            str = prefix;
            str2 = dumpPackage;
            z = true;
        }
        return printed;
    }

    /* access modifiers changed from: package-private */
    public void scheduleIdleTimeoutLocked(ActivityRecord next) {
        if (ActivityTaskManagerDebugConfig.DEBUG_IDLE) {
            Slog.d("ActivityTaskManager", "scheduleIdleTimeoutLocked: Callers=" + Debug.getCallers(4));
        }
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(IDLE_TIMEOUT_MSG, next), 10000);
    }

    /* access modifiers changed from: package-private */
    public final void scheduleIdleLocked() {
        this.mHandler.sendEmptyMessage(IDLE_NOW_MSG);
    }

    /* access modifiers changed from: package-private */
    public void updateTopResumedActivityIfNeeded() {
        ActivityRecord prevTopActivity = this.mTopResumedActivity;
        ActivityStack topStack = this.mRootActivityContainer.getTopDisplayFocusedStack();
        if (topStack != null && topStack.mResumedActivity != prevTopActivity) {
            if ((prevTopActivity != null && !this.mTopResumedActivityWaitingForPrev) && prevTopActivity.scheduleTopResumedActivityChanged(false)) {
                scheduleTopResumedStateLossTimeout(prevTopActivity);
                this.mTopResumedActivityWaitingForPrev = true;
            }
            this.mTopResumedActivity = topStack.mResumedActivity;
            scheduleTopResumedActivityStateIfNeeded();
        }
    }

    private void scheduleTopResumedActivityStateIfNeeded() {
        ActivityRecord activityRecord = this.mTopResumedActivity;
        if (activityRecord != null && !this.mTopResumedActivityWaitingForPrev) {
            activityRecord.scheduleTopResumedActivityChanged(true);
        }
    }

    private void scheduleTopResumedStateLossTimeout(ActivityRecord r) {
        Message msg = this.mHandler.obtainMessage(TOP_RESUMED_STATE_LOSS_TIMEOUT_MSG);
        msg.obj = r;
        r.topResumedStateLossTime = SystemClock.uptimeMillis();
        this.mHandler.sendMessageDelayed(msg, 500);
        if (ActivityTaskManagerDebugConfig.DEBUG_STATES) {
            Slog.v("ActivityTaskManager", "Waiting for top state to be released by " + r);
        }
    }

    /* access modifiers changed from: package-private */
    public void handleTopResumedStateReleased(boolean timeout) {
        if (ActivityTaskManagerDebugConfig.DEBUG_STATES) {
            StringBuilder sb = new StringBuilder();
            sb.append("Top resumed state released ");
            sb.append(timeout ? " (due to timeout)" : " (transition complete)");
            Slog.v("ActivityTaskManager", sb.toString());
        }
        this.mHandler.removeMessages(TOP_RESUMED_STATE_LOSS_TIMEOUT_MSG);
        if (this.mTopResumedActivityWaitingForPrev) {
            this.mTopResumedActivityWaitingForPrev = false;
            scheduleTopResumedActivityStateIfNeeded();
        }
    }

    /* access modifiers changed from: package-private */
    public void removeTimeoutsForActivityLocked(ActivityRecord r) {
        if (ActivityTaskManagerDebugConfig.DEBUG_IDLE) {
            Slog.d("ActivityTaskManager", "removeTimeoutsForActivity: Callers=" + Debug.getCallers(4));
        }
        this.mHandler.removeMessages(IDLE_TIMEOUT_MSG, r);
    }

    /* access modifiers changed from: package-private */
    public final void scheduleResumeTopActivities() {
        if (!this.mHandler.hasMessages(RESUME_TOP_ACTIVITY_MSG)) {
            this.mHandler.sendEmptyMessage(RESUME_TOP_ACTIVITY_MSG);
        }
    }

    /* access modifiers changed from: package-private */
    public void removeSleepTimeouts() {
        this.mHandler.removeMessages(SLEEP_TIMEOUT_MSG);
    }

    /* access modifiers changed from: package-private */
    public final void scheduleSleepTimeout() {
        removeSleepTimeouts();
        this.mHandler.sendEmptyMessageDelayed(SLEEP_TIMEOUT_MSG, 5000);
    }

    public void reCalculateDefaultMinimalSizeOfResizeableTasks() {
    }

    /* access modifiers changed from: package-private */
    public void removeRestartTimeouts(ActivityRecord r) {
        this.mHandler.removeMessages(RESTART_ACTIVITY_PROCESS_TIMEOUT_MSG, r);
    }

    /* access modifiers changed from: package-private */
    public final void scheduleRestartTimeout(ActivityRecord r) {
        removeRestartTimeouts(r);
        ActivityStackSupervisorHandler activityStackSupervisorHandler = this.mHandler;
        activityStackSupervisorHandler.sendMessageDelayed(activityStackSupervisorHandler.obtainMessage(RESTART_ACTIVITY_PROCESS_TIMEOUT_MSG, r), 2000);
    }

    /* access modifiers changed from: package-private */
    public void handleNonResizableTaskIfNeeded(TaskRecord task, int preferredWindowingMode, int preferredDisplayId, ActivityStack actualStack) {
        handleNonResizableTaskIfNeeded(task, preferredWindowingMode, preferredDisplayId, actualStack, false);
    }

    /* access modifiers changed from: package-private */
    public boolean checkNeedFullScreenDisplay(TaskRecord task) {
        ActivityRecord top;
        if (task == null || !task.inHwMultiStackWindowingMode() || task.getStack() == null || (top = task.getTopActivity()) == null || !"com.huawei.camera".equals(top.appInfo.packageName)) {
            return false;
        }
        task.getStack().setWindowingMode(1);
        return true;
    }

    /* access modifiers changed from: package-private */
    public void handleNonResizableTaskIfNeeded(TaskRecord task, int preferredWindowingMode, int preferredDisplayId, ActivityStack actualStack, boolean forceNonResizable) {
        boolean singleTaskInstance = false;
        boolean isSecondaryDisplayPreferred = (preferredDisplayId == 0 || preferredDisplayId == -1) ? false : true;
        if (task != null && !this.mService.mVrMananger.isVrCaredDisplay(preferredDisplayId) && !checkNeedFullScreenDisplay(task)) {
            if (!(actualStack != null && actualStack.getDisplay().hasSplitScreenPrimaryStack()) && preferredWindowingMode != 3 && !isSecondaryDisplayPreferred) {
                return;
            }
            if (!task.isActivityTypeStandardOrUndefined() && getConfiguration().extraConfig.getConfigItem(2) != 2) {
                return;
            }
            if (isSecondaryDisplayPreferred) {
                int actualDisplayId = task.getStack().mDisplayId;
                if (task.canBeLaunchedOnDisplay(actualDisplayId)) {
                    ActivityDisplay preferredDisplay = this.mRootActivityContainer.getActivityDisplay(preferredDisplayId);
                    if (preferredDisplay != null && preferredDisplay.isSingleTaskInstance()) {
                        singleTaskInstance = true;
                    }
                    if (preferredDisplayId != actualDisplayId) {
                        if (singleTaskInstance) {
                            this.mService.getTaskChangeNotificationController().notifyActivityLaunchOnSecondaryDisplayRerouted(task.getTaskInfo(), preferredDisplayId);
                            return;
                        }
                        Slog.w("ActivityTaskManager", "Failed to put " + task + " on display " + preferredDisplayId);
                        this.mService.getTaskChangeNotificationController().notifyActivityLaunchOnSecondaryDisplayFailed(task.getTaskInfo(), preferredDisplayId);
                    } else if (!forceNonResizable) {
                        handleForcedResizableTaskIfNeeded(task, 2);
                    }
                } else {
                    throw new IllegalStateException("Task resolved to incompatible display");
                }
            } else {
                ActivityRecord topActivity = task.getTopActivity();
                if (topActivity == null || topActivity.appInfo == null || (task.supportsSplitScreenWindowingMode() && !forceNonResizable && !"com.huawei.systemmanager".equals(topActivity.appInfo.packageName) && 0 == 0)) {
                    handleForcedResizableTaskIfNeeded(task, 1);
                    return;
                }
                uploadUnSupportSplitScreenAppPackageName(topActivity.appInfo.packageName);
                ActivityStack dockedStack = task.getStack().getDisplay().getSplitScreenPrimaryStack();
                if (dockedStack != null) {
                    if (task.getTopActivity() == null || !HwPCUtils.isPcDynamicStack(task.getTopActivity().getStackId())) {
                        this.mService.getTaskChangeNotificationController().notifyActivityDismissingDockedStack();
                    }
                    if (actualStack == dockedStack) {
                        singleTaskInstance = true;
                    }
                    moveTasksToFullscreenStackLocked(dockedStack, singleTaskInstance);
                }
            }
        }
    }

    private void handleForcedResizableTaskIfNeeded(TaskRecord task, int reason) {
        ActivityRecord topActivity = task.getTopActivity();
        if (topActivity != null && !topActivity.noDisplay && topActivity.isNonResizableOrForcedResizable() && !HwPCUtils.isExtDynamicStack(topActivity.getStackId()) && (topActivity.appInfo.flags & 1) == 0) {
            this.mService.getTaskChangeNotificationController().notifyActivityForcedResizable(task.taskId, reason, topActivity.appInfo.packageName);
        }
    }

    /* access modifiers changed from: package-private */
    public void activityRelaunchedLocked(IBinder token) {
        this.mWindowManager.notifyAppRelaunchingFinished(token);
        ActivityRecord r = ActivityRecord.isInStackLocked(token);
        if (r != null && r.getActivityStack().shouldSleepOrShutDownActivities()) {
            r.setSleeping(true, true);
        }
    }

    /* access modifiers changed from: package-private */
    public void activityRelaunchingLocked(ActivityRecord r) {
        this.mWindowManager.notifyAppRelaunching(r.appToken);
    }

    /* access modifiers changed from: package-private */
    public void logStackState() {
        try {
            this.mActivityMetricsLogger.logWindowState();
        } catch (Exception e) {
            Slog.e("ActivityTaskManager", "stack state exception!");
        }
    }

    /* access modifiers changed from: package-private */
    public void scheduleUpdateMultiWindowMode(TaskRecord task) {
        if (!(task.getStack() == null || task.getStack().deferScheduleMultiWindowModeChanged())) {
            for (int i = task.mActivities.size() - 1; i >= 0; i--) {
                ActivityRecord r = task.mActivities.get(i);
                if (r.attachedToProcess()) {
                    Flog.i(101, "add r " + r + " into list of multiwindow activities");
                    this.mMultiWindowModeChangedActivities.add(r);
                }
            }
            if (!this.mHandler.hasMessages(REPORT_MULTI_WINDOW_MODE_CHANGED_MSG)) {
                this.mHandler.sendEmptyMessage(REPORT_MULTI_WINDOW_MODE_CHANGED_MSG);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void scheduleUpdatePictureInPictureModeIfNeeded(TaskRecord task, ActivityStack prevStack) {
        ActivityStack stack = task.getStack();
        if (prevStack != null && prevStack != stack) {
            if (prevStack.inPinnedWindowingMode() || stack.inPinnedWindowingMode()) {
                scheduleUpdatePictureInPictureModeIfNeeded(task, stack.getRequestedOverrideBounds());
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void scheduleUpdatePictureInPictureModeIfNeeded(TaskRecord task, Rect targetStackBounds) {
        for (int i = task.mActivities.size() - 1; i >= 0; i--) {
            ActivityRecord r = task.mActivities.get(i);
            if (r.attachedToProcess()) {
                this.mPipModeChangedActivities.add(r);
                this.mMultiWindowModeChangedActivities.remove(r);
            }
        }
        this.mPipModeChangedTargetStackBounds = targetStackBounds;
        if (!this.mHandler.hasMessages(REPORT_PIP_MODE_CHANGED_MSG)) {
            this.mHandler.sendEmptyMessage(REPORT_PIP_MODE_CHANGED_MSG);
        }
    }

    /* access modifiers changed from: package-private */
    public void updatePictureInPictureMode(TaskRecord task, Rect targetStackBounds, boolean forceUpdate) {
        this.mHandler.removeMessages(REPORT_PIP_MODE_CHANGED_MSG);
        for (int i = task.mActivities.size() - 1; i >= 0; i--) {
            ActivityRecord r = task.mActivities.get(i);
            if (r.attachedToProcess()) {
                r.updatePictureInPictureMode(targetStackBounds, forceUpdate);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void wakeUp(String reason) {
        PowerManager powerManager = this.mPowerManager;
        long uptimeMillis = SystemClock.uptimeMillis();
        powerManager.wakeUp(uptimeMillis, 2, "android.server.am:TURN_ON:" + reason);
    }

    /* access modifiers changed from: package-private */
    public void beginDeferResume() {
        this.mDeferResumeCount++;
    }

    /* access modifiers changed from: package-private */
    public void endDeferResume() {
        this.mDeferResumeCount--;
    }

    /* access modifiers changed from: package-private */
    public boolean readyToResume() {
        return this.mDeferResumeCount == 0;
    }

    /* access modifiers changed from: protected */
    public final class ActivityStackSupervisorHandler extends Handler {
        public ActivityStackSupervisorHandler(Looper looper) {
            super(looper);
        }

        /* access modifiers changed from: package-private */
        public void activityIdleInternal(ActivityRecord r, boolean processPausingActivities) {
            synchronized (ActivityStackSupervisor.this.mService.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    ActivityStackSupervisor.this.activityIdleInternalLocked(r != null ? r.appToken : null, true, processPausingActivities, null);
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        /* JADX INFO: finally extract failed */
        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == ActivityStackSupervisor.LAUNCH_TIMEOUT_RESUME_MSG) {
                String lastResumedPackageName = null;
                String lastResumedProcessName = null;
                String shortComponentName = null;
                int lastResumedPid = 0;
                synchronized (ActivityStackSupervisor.this.mService.mGlobalLock) {
                    try {
                        WindowManagerService.boostPriorityForLockedSection();
                        ActivityRecord record = ActivityStackSupervisor.this.mService.mLastResumedActivity;
                        if (!(record == null || record.app == null)) {
                            lastResumedPackageName = record.packageName;
                            lastResumedProcessName = record.processName;
                            shortComponentName = record.shortComponentName;
                            lastResumedPid = record.app.getPid();
                        }
                    } catch (Throwable th) {
                        WindowManagerService.resetPriorityAfterLockedSection();
                        throw th;
                    }
                }
                WindowManagerService.resetPriorityAfterLockedSection();
                String packageName = msg.getData().getString(IZRHungService.PARA_PACKAGENAME);
                if (packageName == null || !packageName.equals(lastResumedPackageName)) {
                    Slog.d("ActivityTaskManager", "launch timeout event not send due to cur " + packageName + "not mismatch focusapp " + lastResumedPackageName);
                    return;
                }
                IZrHung appResume = HwFrameworkFactory.getZrHung("appeye_resume");
                if (appResume != null) {
                    ZrHungData arg = new ZrHungData();
                    arg.putString(IZRHungService.PARA_PACKAGENAME, lastResumedPackageName);
                    arg.putString(IZRHungService.PARA_PROCNAME, lastResumedProcessName);
                    arg.putString("activityName", shortComponentName);
                    arg.putInt(IZRHungService.PARAM_PID, lastResumedPid);
                    appResume.sendEvent(arg);
                    return;
                }
                Slog.e("ActivityTaskManager", "appResume is null");
            } else if (i != 10001) {
                switch (i) {
                    case ActivityStackSupervisor.IDLE_TIMEOUT_MSG /* 200 */:
                        if (ActivityTaskManagerDebugConfig.DEBUG_IDLE) {
                            Slog.d("ActivityTaskManager", "handleMessage: IDLE_TIMEOUT_MSG: r=" + msg.obj);
                        }
                        activityIdleInternal((ActivityRecord) msg.obj, true);
                        return;
                    case ActivityStackSupervisor.IDLE_NOW_MSG /* 201 */:
                        if (ActivityTaskManagerDebugConfig.DEBUG_IDLE) {
                            Slog.d("ActivityTaskManager", "handleMessage: IDLE_NOW_MSG: r=" + msg.obj);
                        }
                        activityIdleInternal((ActivityRecord) msg.obj, false);
                        return;
                    case ActivityStackSupervisor.RESUME_TOP_ACTIVITY_MSG /* 202 */:
                        synchronized (ActivityStackSupervisor.this.mService.mGlobalLock) {
                            try {
                                WindowManagerService.boostPriorityForLockedSection();
                                ActivityStackSupervisor.this.mRootActivityContainer.resumeFocusedStacksTopActivities();
                            } finally {
                                WindowManagerService.resetPriorityAfterLockedSection();
                            }
                        }
                        return;
                    case ActivityStackSupervisor.SLEEP_TIMEOUT_MSG /* 203 */:
                        synchronized (ActivityStackSupervisor.this.mService.mGlobalLock) {
                            try {
                                WindowManagerService.boostPriorityForLockedSection();
                                if (ActivityStackSupervisor.this.mService.isSleepingOrShuttingDownLocked()) {
                                    Slog.w("ActivityTaskManager", "Sleep timeout!  Sleeping now.");
                                    ActivityStackSupervisor.this.checkReadyForSleepLocked(false);
                                }
                            } finally {
                                WindowManagerService.resetPriorityAfterLockedSection();
                            }
                        }
                        return;
                    case ActivityStackSupervisor.LAUNCH_TIMEOUT_MSG /* 204 */:
                        String packageName2 = null;
                        ActivityStack mFocusedStack = ActivityStackSupervisor.this.mRootActivityContainer.getTopDisplayFocusedStack();
                        synchronized (ActivityStackSupervisor.this.mService.mGlobalLock) {
                            try {
                                WindowManagerService.boostPriorityForLockedSection();
                                if (mFocusedStack != null) {
                                    ActivityRecord record2 = mFocusedStack.topRunningActivityLocked();
                                    if (record2 == null) {
                                        Slog.w("ActivityTaskManager", "Launch timeout,null top activity");
                                    } else {
                                        packageName2 = record2.packageName;
                                    }
                                } else {
                                    Slog.w("ActivityTaskManager", "mFocusedStack is null!");
                                }
                                if (ActivityStackSupervisor.this.mLaunchingActivityWakeLock.isHeld()) {
                                    Slog.w("ActivityTaskManager", "Launch timeout has expired, giving up wake lock!");
                                    ActivityStackSupervisor.this.mLaunchingActivityWakeLock.release();
                                }
                            } catch (Throwable th2) {
                                WindowManagerService.resetPriorityAfterLockedSection();
                                throw th2;
                            }
                        }
                        WindowManagerService.resetPriorityAfterLockedSection();
                        IZrHung appBF = HwFrameworkFactory.getZrHung("appeye_bootfail");
                        if (appBF != null) {
                            ZrHungData arg2 = new ZrHungData();
                            arg2.putString(IZRHungService.PARA_PACKAGENAME, packageName2);
                            appBF.sendEvent(arg2);
                            Slog.e("ActivityTaskManager", "app boot failure event send");
                            return;
                        }
                        return;
                    default:
                        switch (i) {
                            case ActivityStackSupervisor.LAUNCH_TASK_BEHIND_COMPLETE /* 212 */:
                                synchronized (ActivityStackSupervisor.this.mService.mGlobalLock) {
                                    try {
                                        WindowManagerService.boostPriorityForLockedSection();
                                        ActivityRecord r = ActivityRecord.forTokenLocked((IBinder) msg.obj);
                                        if (r != null) {
                                            ActivityStackSupervisor.this.handleLaunchTaskBehindCompleteLocked(r);
                                        }
                                    } finally {
                                        WindowManagerService.resetPriorityAfterLockedSection();
                                    }
                                }
                                return;
                            case ActivityStackSupervisor.RESTART_ACTIVITY_PROCESS_TIMEOUT_MSG /* 213 */:
                                ActivityRecord r2 = (ActivityRecord) msg.obj;
                                String processName = null;
                                int uid = 0;
                                synchronized (ActivityStackSupervisor.this.mService.mGlobalLock) {
                                    try {
                                        WindowManagerService.boostPriorityForLockedSection();
                                        if (r2.attachedToProcess() && r2.isState(ActivityStack.ActivityState.RESTARTING_PROCESS)) {
                                            processName = r2.app.mName;
                                            uid = r2.app.mUid;
                                        }
                                    } finally {
                                        WindowManagerService.resetPriorityAfterLockedSection();
                                    }
                                }
                                if (processName != null) {
                                    ActivityStackSupervisor.this.mService.mAmInternal.killProcess(processName, uid, "restartActivityProcessTimeout");
                                    return;
                                }
                                return;
                            case ActivityStackSupervisor.REPORT_MULTI_WINDOW_MODE_CHANGED_MSG /* 214 */:
                                synchronized (ActivityStackSupervisor.this.mService.mGlobalLock) {
                                    try {
                                        WindowManagerService.boostPriorityForLockedSection();
                                        for (int i2 = ActivityStackSupervisor.this.mMultiWindowModeChangedActivities.size() - 1; i2 >= 0; i2 += -1) {
                                            ActivityRecord r3 = ActivityStackSupervisor.this.mMultiWindowModeChangedActivities.remove(i2);
                                            Flog.i(101, "schedule multiwindow mode change callback for " + r3);
                                            r3.updateMultiWindowMode();
                                        }
                                    } finally {
                                        WindowManagerService.resetPriorityAfterLockedSection();
                                    }
                                }
                                return;
                            case ActivityStackSupervisor.REPORT_PIP_MODE_CHANGED_MSG /* 215 */:
                                synchronized (ActivityStackSupervisor.this.mService.mGlobalLock) {
                                    try {
                                        WindowManagerService.boostPriorityForLockedSection();
                                        for (int i3 = ActivityStackSupervisor.this.mPipModeChangedActivities.size() - 1; i3 >= 0; i3--) {
                                            ActivityStackSupervisor.this.mPipModeChangedActivities.remove(i3).updatePictureInPictureMode(ActivityStackSupervisor.this.mPipModeChangedTargetStackBounds, false);
                                        }
                                    } finally {
                                        WindowManagerService.resetPriorityAfterLockedSection();
                                    }
                                }
                                return;
                            case ActivityStackSupervisor.REPORT_HOME_CHANGED_MSG /* 216 */:
                                synchronized (ActivityStackSupervisor.this.mService.mGlobalLock) {
                                    try {
                                        WindowManagerService.boostPriorityForLockedSection();
                                        ActivityStackSupervisor.this.mHandler.removeMessages(ActivityStackSupervisor.REPORT_HOME_CHANGED_MSG);
                                        ActivityStackSupervisor.this.mRootActivityContainer.startHomeOnEmptyDisplays("homeChanged");
                                    } finally {
                                        WindowManagerService.resetPriorityAfterLockedSection();
                                    }
                                }
                                return;
                            case ActivityStackSupervisor.TOP_RESUMED_STATE_LOSS_TIMEOUT_MSG /* 217 */:
                                ActivityRecord r4 = (ActivityRecord) msg.obj;
                                Slog.w("ActivityTaskManager", "Activity top resumed state loss timeout for " + r4);
                                synchronized (ActivityStackSupervisor.this.mService.mGlobalLock) {
                                    try {
                                        WindowManagerService.boostPriorityForLockedSection();
                                        if (r4.hasProcess()) {
                                            ActivityStackSupervisor.this.mService.logAppTooSlow(r4.app, r4.topResumedStateLossTime, "top state loss for " + r4);
                                        }
                                    } catch (Throwable th3) {
                                        WindowManagerService.resetPriorityAfterLockedSection();
                                        throw th3;
                                    }
                                }
                                WindowManagerService.resetPriorityAfterLockedSection();
                                ActivityStackSupervisor.this.handleTopResumedStateReleased(true);
                                return;
                            default:
                                return;
                        }
                }
            } else {
                ActivityStackSupervisor.this.handlePCWindowStateChanged();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setResizingDuringAnimation(TaskRecord task) {
        this.mResizingTasksDuringAnimation.add(Integer.valueOf(task.taskId));
        task.setTaskDockedResizing(true);
    }

    private void updateWindowingModeToUndefined(int taskId, ActivityOptions activityOptions) {
        if (activityOptions != null && activityOptions.getLaunchWindowingMode() == 102) {
            TaskRecord task = this.mRootActivityContainer.anyTaskForId(taskId, 0);
            ActivityRecord top = task != null ? task.getTopActivity() : null;
            if (top != null && top.appInfo != null && "com.huawei.camera".equals(top.appInfo.packageName)) {
                activityOptions.setLaunchWindowingMode(0);
            }
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:168:0x03f1 A[Catch:{ all -> 0x03fa }] */
    public int startActivityFromRecents(int callingPid, int callingUid, int taskId, SafeActivityOptions options) {
        ActivityOptions activityOptions;
        int windowingMode;
        int activityType;
        String str;
        int i;
        int i2;
        int windowingMode2;
        Throwable th;
        ActivityDisplay display;
        ActivityStack oldStack;
        ActivityOptions activityOptions2;
        String str2;
        int i3;
        int i4;
        boolean z;
        String callingPackage;
        Intent intent;
        int userId;
        ActivityOptions activityOptions3;
        String str3;
        int i5;
        int i6;
        boolean z2;
        int i7;
        ActivityRecord targetActivity;
        Throwable th2;
        TaskRecord task = null;
        Rect launchBounds = null;
        if (options != null) {
            activityOptions = options.getOptions(this);
        } else {
            activityOptions = null;
        }
        boolean z3 = false;
        if (activityOptions != null) {
            updateWindowingModeToUndefined(taskId, activityOptions);
            int activityType2 = activityOptions.getLaunchActivityType();
            int windowingMode3 = activityOptions.getLaunchWindowingMode();
            launchBounds = activityOptions.getLaunchBounds();
            if (activityOptions.getLaunchWindowingMode() == 102 && this.mService.mHwATMSEx.isInDisplaySurfaceScaled()) {
                activityOptions.setLaunchWindowingMode(0);
                Slog.i("ActivityTaskManager", "startActivityFromRecents: setLaunchWindowingMode " + activityOptions);
            }
            if (activityOptions.freezeRecentTasksReordering()) {
                if (this.mRecentTasks.isCallerRecents(callingUid)) {
                    this.mRecentTasks.setFreezeTaskListReordering();
                }
            }
            activityType = activityType2;
            windowingMode = windowingMode3;
        } else {
            activityType = 0;
            windowingMode = 0;
        }
        if (activityType == 2 || activityType == 3) {
            Slog.e("ActivityTaskManager", "startActivityFromRecents: Task " + taskId + " can't be launch for activity type " + activityType);
            throw new IllegalArgumentException("startActivityFromRecents: Task " + taskId + " can't be launch in the home/recents stack.");
        } else if (this.mWindowManager.getFoldDisplayMode() == 4) {
            return WindowManagerService.H.APP_TRANSITION_GETSPECSFUTURE_TIMEOUT;
        } else {
            if (CoordinationModeUtils.getInstance(this.mService.mContext).isExitingCoordinationMode()) {
                return WindowManagerService.H.APP_TRANSITION_GETSPECSFUTURE_TIMEOUT;
            }
            this.mWindowManager.deferSurfaceLayout();
            if (windowingMode == 3) {
                try {
                    try {
                        this.mWindowManager.setDockedStackCreateState(activityOptions.getSplitScreenCreateMode(), null);
                        deferUpdateRecentsHomeStackBounds();
                        this.mWindowManager.prepareAppTransition(19, false);
                    } catch (Throwable th3) {
                        th = th3;
                        str = "startActivityFromRecents: homeVisibleInSplitScreen";
                        windowingMode2 = windowingMode;
                        i2 = 3;
                        i = 4;
                    }
                } catch (Throwable th4) {
                    th = th4;
                    windowingMode2 = windowingMode;
                    i = 4;
                    str = "startActivityFromRecents: homeVisibleInSplitScreen";
                    i2 = 3;
                    try {
                        setResizingDuringAnimation(task);
                        display = task.getStack().getDisplay();
                        if (display.getTopStackInWindowingMode(i).isActivityTypeHome()) {
                        }
                        this.mWindowManager.continueSurfaceLayout();
                        throw th;
                    } catch (Throwable th5) {
                        this.mWindowManager.continueSurfaceLayout();
                        throw th5;
                    }
                }
            }
            try {
                TaskRecord oldTask = this.mRecentTasks.getTask(taskId);
                oldStack = oldTask != null ? oldTask.getStack() : null;
            } catch (Throwable th6) {
                th = th6;
                str = "startActivityFromRecents: homeVisibleInSplitScreen";
                windowingMode2 = windowingMode;
                i2 = 3;
                i = 4;
                setResizingDuringAnimation(task);
                display = task.getStack().getDisplay();
                if (display.getTopStackInWindowingMode(i).isActivityTypeHome()) {
                }
                this.mWindowManager.continueSurfaceLayout();
                throw th;
            }
            try {
                TaskRecord task2 = this.mRootActivityContainer.anyTaskForId(taskId, 2, activityOptions, true);
                if (task2 != null) {
                    if (launchBounds == null) {
                        try {
                            launchBounds = new Rect();
                        } catch (Throwable th7) {
                            th = th7;
                            str = "startActivityFromRecents: homeVisibleInSplitScreen";
                            task = task2;
                            windowingMode2 = windowingMode;
                            i2 = 3;
                            i = 4;
                            z3 = false;
                            setResizingDuringAnimation(task);
                            display = task.getStack().getDisplay();
                            if (display.getTopStackInWindowingMode(i).isActivityTypeHome()) {
                            }
                            this.mWindowManager.continueSurfaceLayout();
                            throw th;
                        }
                    }
                    try {
                        if (!(!launchBounds.isEmpty() || windowingMode != 102 || task2.realActivity == null || oldStack == null || task2.getStack() == null || oldStack == task2.getStack())) {
                            float adjustScale = this.mService.mHwATMSEx.getReusableHwFreeFormBounds(task2.realActivity.getPackageName(), task2.userId, launchBounds);
                            if (adjustScale > 0.0f && task2.getStack().getTaskStack() != null) {
                                task2.getStack().getTaskStack().mHwStackScale = adjustScale;
                            }
                            if (!launchBounds.isEmpty()) {
                                task2.getStack().resize(launchBounds, null, null);
                                this.mService.mHwATMSEx.updateDragFreeFormPos(task2.getStack());
                            }
                        }
                        if (windowingMode != 3 && !task2.inHwMultiStackWindowingMode()) {
                            this.mRootActivityContainer.getDefaultDisplay().moveHomeStackToFront("startActivityFromRecents");
                        }
                        if (!this.mService.mAmInternal.shouldConfirmCredentials(task2.userId)) {
                            try {
                                if (task2.getRootActivity() != null) {
                                    ActivityRecord targetActivity2 = task2.getTopActivity();
                                    this.mRootActivityContainer.sendPowerHintForLaunchStartIfNeeded(true, targetActivity2);
                                    Flog.i(101, "task.userId =" + task2.userId + ", task.taskId = " + task2.taskId + ", task.getRootActivity() = " + task2.getRootActivity() + ", task.getTopActivity() = " + task2.getTopActivity());
                                    this.mActivityMetricsLogger.notifyActivityLaunching(task2.intent);
                                    this.mHwActivityStackSupervisorEx.handleFreeFormWindow(task2);
                                    this.mHwActivityStackSupervisorEx.handlePCMultiDisplayWindow(task2, windowingMode);
                                    try {
                                        activityOptions3 = activityOptions;
                                        str3 = "startActivityFromRecents: homeVisibleInSplitScreen";
                                        try {
                                            this.mService.moveTaskToFrontLocked(null, null, task2.taskId, 0, options, true);
                                            targetActivity2.applyOptionsLocked();
                                            try {
                                                this.mActivityMetricsLogger.notifyActivityLaunched(2, targetActivity2);
                                                this.mService.getActivityStartController().postStartActivityProcessingForLastStarter(task2.getTopActivity(), 2, task2.getStack());
                                                if (windowingMode == 3) {
                                                    try {
                                                        setResizingDuringAnimation(task2);
                                                        ActivityDisplay display2 = task2.getStack().getDisplay();
                                                        if (display2.getTopStackInWindowingMode(4).isActivityTypeHome()) {
                                                            display2.moveHomeStackToFront(str3);
                                                            this.mWindowManager.checkSplitScreenMinimizedChanged(false);
                                                        }
                                                    } catch (Throwable th8) {
                                                        this.mWindowManager.continueSurfaceLayout();
                                                        throw th8;
                                                    }
                                                }
                                                this.mWindowManager.continueSurfaceLayout();
                                                return 2;
                                            } catch (Throwable th9) {
                                                th = th9;
                                                task = task2;
                                                str = str3;
                                                windowingMode2 = windowingMode;
                                                i2 = 3;
                                                i = 4;
                                                z3 = false;
                                                setResizingDuringAnimation(task);
                                                display = task.getStack().getDisplay();
                                                if (display.getTopStackInWindowingMode(i).isActivityTypeHome()) {
                                                }
                                                this.mWindowManager.continueSurfaceLayout();
                                                throw th;
                                            }
                                        } catch (Throwable th10) {
                                            th2 = th10;
                                            targetActivity = targetActivity2;
                                            i7 = 2;
                                            z2 = false;
                                            i6 = 4;
                                            i5 = 3;
                                            try {
                                                this.mActivityMetricsLogger.notifyActivityLaunched(i7, targetActivity);
                                                throw th2;
                                            } catch (Throwable th11) {
                                                th = th11;
                                                task = task2;
                                                str = str3;
                                                windowingMode2 = windowingMode;
                                                i = i6;
                                                i2 = i5;
                                                z3 = z2;
                                                setResizingDuringAnimation(task);
                                                display = task.getStack().getDisplay();
                                                if (display.getTopStackInWindowingMode(i).isActivityTypeHome()) {
                                                }
                                                this.mWindowManager.continueSurfaceLayout();
                                                throw th;
                                            }
                                        }
                                    } catch (Throwable th12) {
                                        th2 = th12;
                                        targetActivity = targetActivity2;
                                        activityOptions3 = activityOptions;
                                        i7 = 2;
                                        z2 = false;
                                        i5 = 3;
                                        str3 = "startActivityFromRecents: homeVisibleInSplitScreen";
                                        i6 = 4;
                                        this.mActivityMetricsLogger.notifyActivityLaunched(i7, targetActivity);
                                        throw th2;
                                    }
                                } else {
                                    activityOptions2 = activityOptions;
                                    z = false;
                                    i3 = 3;
                                    str2 = "startActivityFromRecents: homeVisibleInSplitScreen";
                                    i4 = 4;
                                }
                            } catch (Throwable th13) {
                                th = th13;
                                task = task2;
                                str = "startActivityFromRecents: homeVisibleInSplitScreen";
                                windowingMode2 = windowingMode;
                                i2 = 3;
                                i = 4;
                                z3 = false;
                                if (windowingMode2 == i2 && task != null) {
                                    setResizingDuringAnimation(task);
                                    display = task.getStack().getDisplay();
                                    if (display.getTopStackInWindowingMode(i).isActivityTypeHome()) {
                                        display.moveHomeStackToFront(str);
                                        this.mWindowManager.checkSplitScreenMinimizedChanged(z3);
                                    }
                                }
                                this.mWindowManager.continueSurfaceLayout();
                                throw th;
                            }
                        } else {
                            activityOptions2 = activityOptions;
                            z = false;
                            i3 = 3;
                            str2 = "startActivityFromRecents: homeVisibleInSplitScreen";
                            i4 = 4;
                        }
                        try {
                            callingPackage = task2.mCallingPackage;
                            try {
                                intent = task2.intent;
                                intent.addFlags(1048576);
                                userId = task2.userId;
                                windowingMode2 = windowingMode;
                            } catch (Throwable th14) {
                                th = th14;
                                task = task2;
                                str = str2;
                                windowingMode2 = windowingMode;
                                i = i4;
                                i2 = i3;
                                z3 = z;
                                setResizingDuringAnimation(task);
                                display = task.getStack().getDisplay();
                                if (display.getTopStackInWindowingMode(i).isActivityTypeHome()) {
                                }
                                this.mWindowManager.continueSurfaceLayout();
                                throw th;
                            }
                        } catch (Throwable th15) {
                            th = th15;
                            task = task2;
                            str = str2;
                            windowingMode2 = windowingMode;
                            i = i4;
                            i2 = i3;
                            z3 = z;
                            setResizingDuringAnimation(task);
                            display = task.getStack().getDisplay();
                            if (display.getTopStackInWindowingMode(i).isActivityTypeHome()) {
                            }
                            this.mWindowManager.continueSurfaceLayout();
                            throw th;
                        }
                        try {
                            Slog.i("ActivityTaskManager", "startActivityFromRecents: Task " + taskId + " in package.");
                            try {
                                int startActivityInPackage = this.mService.getActivityStartController().startActivityInPackage(task2.mCallingUid, callingPid, callingUid, callingPackage, intent, null, null, null, 0, 0, options, userId, task2, "startActivityFromRecents", false, null, false);
                                if (windowingMode2 == 3) {
                                    try {
                                        setResizingDuringAnimation(task2);
                                        ActivityDisplay display3 = task2.getStack().getDisplay();
                                        if (display3.getTopStackInWindowingMode(4).isActivityTypeHome()) {
                                            display3.moveHomeStackToFront(str2);
                                            this.mWindowManager.checkSplitScreenMinimizedChanged(false);
                                        }
                                    } catch (Throwable th16) {
                                        this.mWindowManager.continueSurfaceLayout();
                                        throw th16;
                                    }
                                }
                                this.mWindowManager.continueSurfaceLayout();
                                return startActivityInPackage;
                            } catch (Throwable th17) {
                                th = th17;
                                windowingMode2 = windowingMode2;
                                task = task2;
                                str = str2;
                                i2 = 3;
                                i = 4;
                                z3 = false;
                                setResizingDuringAnimation(task);
                                display = task.getStack().getDisplay();
                                if (display.getTopStackInWindowingMode(i).isActivityTypeHome()) {
                                }
                                this.mWindowManager.continueSurfaceLayout();
                                throw th;
                            }
                        } catch (Throwable th18) {
                            th = th18;
                            task = task2;
                            str = str2;
                            i = i4;
                            i2 = i3;
                            z3 = z;
                            setResizingDuringAnimation(task);
                            display = task.getStack().getDisplay();
                            if (display.getTopStackInWindowingMode(i).isActivityTypeHome()) {
                            }
                            this.mWindowManager.continueSurfaceLayout();
                            throw th;
                        }
                    } catch (Throwable th19) {
                        th = th19;
                        str = "startActivityFromRecents: homeVisibleInSplitScreen";
                        task = task2;
                        windowingMode2 = windowingMode;
                        i2 = 3;
                        i = 4;
                        z3 = false;
                        setResizingDuringAnimation(task);
                        display = task.getStack().getDisplay();
                        if (display.getTopStackInWindowingMode(i).isActivityTypeHome()) {
                        }
                        this.mWindowManager.continueSurfaceLayout();
                        throw th;
                    }
                } else {
                    task = task2;
                    windowingMode2 = windowingMode;
                    i = 4;
                    z3 = false;
                    str = "startActivityFromRecents: homeVisibleInSplitScreen";
                    i2 = 3;
                    try {
                        continueUpdateRecentsHomeStackBounds();
                        this.mWindowManager.executeAppTransition();
                        Slog.e("ActivityTaskManager", "startActivityFromRecents: Task " + taskId + " not found.");
                        throw new IllegalArgumentException("startActivityFromRecents: Task " + taskId + " not found.");
                    } catch (Throwable th20) {
                        th = th20;
                        setResizingDuringAnimation(task);
                        display = task.getStack().getDisplay();
                        if (display.getTopStackInWindowingMode(i).isActivityTypeHome()) {
                        }
                        this.mWindowManager.continueSurfaceLayout();
                        throw th;
                    }
                }
            } catch (Throwable th21) {
                th = th21;
                str = "startActivityFromRecents: homeVisibleInSplitScreen";
                windowingMode2 = windowingMode;
                i2 = 3;
                i = 4;
                z3 = false;
                setResizingDuringAnimation(task);
                display = task.getStack().getDisplay();
                if (display.getTopStackInWindowingMode(i).isActivityTypeHome()) {
                }
                this.mWindowManager.continueSurfaceLayout();
                throw th;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public static class WaitInfo {
        private final WaitResult mResult;
        private final long mStartTimeMs;
        private final ComponentName mTargetComponent;

        WaitInfo(ComponentName targetComponent, WaitResult result, long startTimeMs) {
            this.mTargetComponent = targetComponent;
            this.mResult = result;
            this.mStartTimeMs = startTimeMs;
        }

        public boolean matches(ComponentName targetComponent) {
            ComponentName componentName = this.mTargetComponent;
            return componentName == null || componentName.equals(targetComponent);
        }

        public WaitResult getResult() {
            return this.mResult;
        }

        public long getStartTime() {
            return this.mStartTimeMs;
        }

        public ComponentName getComponent() {
            return this.mTargetComponent;
        }

        public void dump(PrintWriter pw, String prefix) {
            pw.println(prefix + "WaitInfo:");
            pw.println(prefix + "  mTargetComponent=" + this.mTargetComponent);
            StringBuilder sb = new StringBuilder();
            sb.append(prefix);
            sb.append("  mResult=");
            pw.println(sb.toString());
            this.mResult.dump(pw, prefix);
        }
    }
}
