package com.android.server.am;

import android.app.ActivityManager;
import android.app.ActivityManagerInternal;
import android.app.ActivityOptions;
import android.app.AppOpsManager;
import android.app.ProfilerInfo;
import android.app.ResultInfo;
import android.app.WaitResult;
import android.app.WindowConfiguration;
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
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.UserInfo;
import android.content.res.Configuration;
import android.freeform.HwFreeFormManager;
import android.freeform.HwFreeFormUtils;
import android.graphics.Rect;
import android.hardware.display.DisplayManager;
import android.hardware.display.DisplayManagerInternal;
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
import android.os.SystemProperties;
import android.os.Trace;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.WorkSource;
import android.service.voice.IVoiceInteractionSession;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.EventLog;
import android.util.Flog;
import android.util.HwPCUtils;
import android.util.IntArray;
import android.util.Jlog;
import android.util.MergedConfiguration;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.util.TimeUtils;
import android.util.proto.ProtoOutputStream;
import android.view.Display;
import android.view.IApplicationToken;
import android.vrsystem.IVRSystemServiceManager;
import android.widget.Toast;
import android.zrhung.IZrHung;
import android.zrhung.ZrHungData;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.content.ReferrerIntent;
import com.android.internal.os.BackgroundThread;
import com.android.internal.os.TransferPipe;
import com.android.internal.os.logging.MetricsLoggerWrapper;
import com.android.internal.util.ArrayUtils;
import com.android.server.HwServiceExFactory;
import com.android.server.HwServiceFactory;
import com.android.server.LocalServices;
import com.android.server.NetworkManagementService;
import com.android.server.SmartShrinker;
import com.android.server.UiModeManagerService;
import com.android.server.UiThread;
import com.android.server.am.ActivityStack;
import com.android.server.am.RecentTasks;
import com.android.server.job.controllers.JobStatus;
import com.android.server.os.HwBootFail;
import com.android.server.pm.DumpState;
import com.android.server.pm.PackageManagerService;
import com.android.server.wm.ConfigurationContainer;
import com.android.server.wm.PinnedStackWindowController;
import com.android.server.wm.WindowManagerService;
import com.huawei.pgmng.log.LogPower;
import com.huawei.server.am.IHwActivityStackSupervisorEx;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class ActivityStackSupervisor extends AbsActivityStackSupervisor implements DisplayManager.DisplayListener, RecentTasks.Callbacks {
    private static final ArrayMap<String, String> ACTION_TO_RUNTIME_PERMISSION = new ArrayMap<>();
    private static final int ACTIVITY_RESTRICTION_APPOP = 2;
    private static final int ACTIVITY_RESTRICTION_NONE = 0;
    private static final int ACTIVITY_RESTRICTION_PERMISSION = 1;
    static final boolean CREATE_IF_NEEDED = true;
    static final boolean DEFER_RESUME = true;
    static final int HANDLE_DISPLAY_ADDED = 105;
    static final int HANDLE_DISPLAY_CHANGED = 106;
    static final int HANDLE_DISPLAY_REMOVED = 107;
    static final int IDLE_NOW_MSG = 101;
    static final int IDLE_TIMEOUT = 10000;
    static final int IDLE_TIMEOUT_MSG = 100;
    static final boolean IS_DEBUG_VERSION;
    private static final boolean IS_DOCOMO = SystemProperties.get("ro.product.custom", BluetoothManagerService.DEFAULT_PACKAGE_NAME).contains("docomo");
    static final int LAUNCH_TASK_BEHIND_COMPLETE = 112;
    static final int LAUNCH_TIMEOUT = 10000;
    static final int LAUNCH_TIMEOUT_MSG = 104;
    static final int MATCH_TASK_IN_STACKS_ONLY = 0;
    static final int MATCH_TASK_IN_STACKS_OR_RECENT_TASKS = 1;
    static final int MATCH_TASK_IN_STACKS_OR_RECENT_TASKS_AND_RESTORE = 2;
    private static final int MAX_TASK_IDS_PER_USER = 100000;
    static final boolean ON_TOP = true;
    static final boolean PAUSE_IMMEDIATELY = true;
    static final boolean PRESERVE_WINDOWS = true;
    static final boolean REMOVE_FROM_RECENTS = true;
    static final int REPORT_MULTI_WINDOW_MODE_CHANGED_MSG = 114;
    static final int REPORT_PIP_MODE_CHANGED_MSG = 115;
    static final int RESUME_TOP_ACTIVITY_MSG = 102;
    static final int SLEEP_TIMEOUT = 5000;
    static final int SLEEP_TIMEOUT_MSG = 103;
    private static final String TAG = "ActivityManager";
    private static final String TAG_FOCUS = "ActivityManager";
    private static final String TAG_IDLE = "ActivityManager";
    static final String TAG_KEYGUARD = "ActivityManager_keyguard";
    private static final String TAG_PAUSE = "ActivityManager";
    private static final String TAG_RECENTS = "ActivityManager";
    private static final String TAG_RELEASE = "ActivityManager";
    private static final String TAG_STACK = "ActivityManager";
    private static final String TAG_STATES = "ActivityManager";
    private static final String TAG_SWITCH = "ActivityManager";
    static final String TAG_TASKS = "ActivityManager";
    static final boolean VALIDATE_WAKE_LOCK_CALLER = false;
    private static final String VIRTUAL_DISPLAY_BASE_NAME = "ActivityViewVirtualDisplay";
    boolean inResumeTopActivity;
    final ArrayList<ActivityRecord> mActivitiesWaitingForVisibleActivity = new ArrayList<>();
    protected final SparseArray<ActivityDisplay> mActivityDisplays = new SparseArray<>();
    String mActivityLaunchTrack = BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
    private ActivityMetricsLogger mActivityMetricsLogger;
    private boolean mAllowDockedStackResize = true;
    boolean mAppVisibilitiesChangedSinceLastPause;
    private final SparseIntArray mCurTaskIdForUser = new SparseIntArray(20);
    int mCurrentUser;
    int mDefaultMinSizeOfResizeableTask = -1;
    private int mDeferResumeCount;
    private final SparseArray<IntArray> mDisplayAccessUIDs = new SparseArray<>();
    DisplayManager mDisplayManager;
    private DisplayManagerInternal mDisplayManagerInternal;
    private boolean mDockedStackResizing;
    final ArrayList<ActivityRecord> mFinishingActivities = new ArrayList<>();
    ActivityStack mFocusedStack;
    PowerManager.WakeLock mGoingToSleep;
    final ArrayList<ActivityRecord> mGoingToSleepActivities = new ArrayList<>();
    final ActivityStackSupervisorHandler mHandler;
    private boolean mHasPendingDockedBounds;
    ActivityStack mHomeStack;
    protected IHwActivityStackSupervisorEx mHwActivityStackSupervisorEx;
    private boolean mInitialized;
    boolean mIsDockMinimized;
    private KeyguardController mKeyguardController;
    private ActivityStack mLastFocusedStack;
    private LaunchParamsController mLaunchParamsController;
    private LaunchTimeTracker mLaunchTimeTracker = new LaunchTimeTracker();
    PowerManager.WakeLock mLaunchingActivity;
    final Looper mLooper;
    final ArrayList<ActivityRecord> mMultiWindowModeChangedActivities = new ArrayList<>();
    final ArrayList<ActivityRecord> mNoAnimActivities = new ArrayList<>();
    private Rect mPendingDockedBounds;
    private Rect mPendingTempDockedTaskBounds;
    private Rect mPendingTempDockedTaskInsetBounds;
    private Rect mPendingTempOtherTaskBounds;
    private Rect mPendingTempOtherTaskInsetBounds;
    final ArrayList<ActivityRecord> mPipModeChangedActivities = new ArrayList<>();
    Rect mPipModeChangedTargetStackBounds;
    private boolean mPowerHintSent;
    private PowerManager mPowerManager;
    RecentTasks mRecentTasks;
    private final ArraySet<Integer> mResizingTasksDuringAnimation = new ArraySet<>();
    private RunningTasks mRunningTasks;
    final ActivityManagerService mService;
    final ArrayList<ActivityManagerInternal.SleepToken> mSleepTokens = new ArrayList<>();
    final ArrayList<UserState> mStartingUsers = new ArrayList<>();
    final ArrayList<ActivityRecord> mStoppingActivities = new ArrayList<>();
    private boolean mTaskLayersChanged = true;
    private final ArrayList<ActivityRecord> mTmpActivityList = new ArrayList<>();
    private final FindTaskResult mTmpFindTaskResult = new FindTaskResult();
    private final ActivityOptions mTmpOptions = ActivityOptions.makeBasic();
    private SparseIntArray mTmpOrderedDisplayIds = new SparseIntArray();
    boolean mUserLeaving = false;
    SparseIntArray mUserStackInFront = new SparseIntArray(2);
    private IVRSystemServiceManager mVrMananger;
    final ArrayList<WaitResult> mWaitingActivityLaunched = new ArrayList<>();
    private final ArrayList<WaitInfo> mWaitingForActivityVisible = new ArrayList<>();
    WindowManagerService mWindowManager;
    private final Rect tempRect = new Rect();

    protected final class ActivityStackSupervisorHandler extends Handler {
        public ActivityStackSupervisorHandler(Looper looper) {
            super(looper);
        }

        /* access modifiers changed from: package-private */
        public void activityIdleInternal(ActivityRecord r, boolean processPausingActivities) {
            synchronized (ActivityStackSupervisor.this.mService) {
                try {
                    ActivityManagerService.boostPriorityForLockedSection();
                    ActivityStackSupervisor.this.activityIdleInternalLocked(r != null ? r.appToken : null, true, processPausingActivities, null);
                } catch (Throwable th) {
                    while (true) {
                        ActivityManagerService.resetPriorityAfterLockedSection();
                        throw th;
                    }
                }
            }
            ActivityManagerService.resetPriorityAfterLockedSection();
        }

        public void handleMessage(Message msg) {
            ActivityRecord r;
            int i = msg.what;
            if (i == 112) {
                synchronized (ActivityStackSupervisor.this.mService) {
                    try {
                        ActivityManagerService.boostPriorityForLockedSection();
                        ActivityRecord r2 = ActivityRecord.forTokenLocked((IBinder) msg.obj);
                        if (r2 != null) {
                            ActivityStackSupervisor.this.handleLaunchTaskBehindCompleteLocked(r2);
                        }
                    } catch (Throwable th) {
                        while (true) {
                            ActivityManagerService.resetPriorityAfterLockedSection();
                            throw th;
                        }
                    }
                }
                ActivityManagerService.resetPriorityAfterLockedSection();
            } else if (i != 10001) {
                switch (i) {
                    case 100:
                        if (ActivityManagerDebugConfig.DEBUG_IDLE) {
                            Slog.d(ActivityManagerService.TAG, "handleMessage: IDLE_TIMEOUT_MSG: r=" + msg.obj);
                        }
                        activityIdleInternal((ActivityRecord) msg.obj, true);
                        return;
                    case 101:
                        if (ActivityManagerDebugConfig.DEBUG_IDLE) {
                            Slog.d(ActivityManagerService.TAG, "handleMessage: IDLE_NOW_MSG: r=" + msg.obj);
                        }
                        activityIdleInternal((ActivityRecord) msg.obj, false);
                        return;
                    case 102:
                        synchronized (ActivityStackSupervisor.this.mService) {
                            try {
                                ActivityManagerService.boostPriorityForLockedSection();
                                ActivityStackSupervisor.this.resumeFocusedStackTopActivityLocked();
                            } catch (Throwable th2) {
                                while (true) {
                                    ActivityManagerService.resetPriorityAfterLockedSection();
                                    throw th2;
                                    break;
                                }
                            }
                        }
                        ActivityManagerService.resetPriorityAfterLockedSection();
                        return;
                    case 103:
                        synchronized (ActivityStackSupervisor.this.mService) {
                            try {
                                ActivityManagerService.boostPriorityForLockedSection();
                                if (ActivityStackSupervisor.this.mService.isSleepingOrShuttingDownLocked()) {
                                    Slog.w(ActivityManagerService.TAG, "Sleep timeout!  Sleeping now.");
                                    ActivityStackSupervisor.this.checkReadyForSleepLocked(false);
                                }
                            } catch (Throwable th3) {
                                while (true) {
                                    ActivityManagerService.resetPriorityAfterLockedSection();
                                    throw th3;
                                    break;
                                }
                            }
                        }
                        ActivityManagerService.resetPriorityAfterLockedSection();
                        return;
                    case 104:
                        String packageName = null;
                        synchronized (ActivityStackSupervisor.this.mService) {
                            try {
                                ActivityManagerService.boostPriorityForLockedSection();
                                ActivityRecord r3 = ActivityStackSupervisor.this.mFocusedStack.topRunningActivityLocked();
                                if (r3 == null) {
                                    Slog.w(ActivityManagerService.TAG, "Launch timeout,null top activity");
                                } else {
                                    packageName = r3.packageName;
                                }
                                if (ActivityStackSupervisor.this.mLaunchingActivity.isHeld()) {
                                    Slog.w(ActivityManagerService.TAG, "Launch timeout has expired, giving up wake lock!");
                                    ActivityStackSupervisor.this.mLaunchingActivity.release();
                                }
                            } catch (Throwable th4) {
                                while (true) {
                                    ActivityManagerService.resetPriorityAfterLockedSection();
                                    throw th4;
                                    break;
                                }
                            }
                        }
                        ActivityManagerService.resetPriorityAfterLockedSection();
                        IZrHung appBF = HwFrameworkFactory.getZrHung("appeye_bootfail");
                        if (appBF != null) {
                            ZrHungData arg = new ZrHungData();
                            arg.putString("packageName", packageName);
                            appBF.sendEvent(arg);
                            return;
                        }
                        return;
                    case 105:
                        ActivityStackSupervisor.this.handleDisplayAdded(msg.arg1);
                        return;
                    case 106:
                        ActivityStackSupervisor.this.handleDisplayChanged(msg.arg1);
                        return;
                    case 107:
                        ActivityStackSupervisor.this.handleDisplayRemoved(msg.arg1);
                        return;
                    default:
                        switch (i) {
                            case 114:
                                synchronized (ActivityStackSupervisor.this.mService) {
                                    try {
                                        ActivityManagerService.boostPriorityForLockedSection();
                                        int i2 = ActivityStackSupervisor.this.mMultiWindowModeChangedActivities.size() - 1;
                                        while (i2 >= 0) {
                                            Flog.i(101, "schedule multiwindow mode change callback for " + r);
                                            r.updateMultiWindowMode();
                                            i2 += -1;
                                        }
                                    } catch (Throwable th5) {
                                        while (true) {
                                            ActivityManagerService.resetPriorityAfterLockedSection();
                                            throw th5;
                                            break;
                                        }
                                    }
                                }
                                ActivityManagerService.resetPriorityAfterLockedSection();
                                return;
                            case 115:
                                synchronized (ActivityStackSupervisor.this.mService) {
                                    try {
                                        ActivityManagerService.boostPriorityForLockedSection();
                                        int i3 = ActivityStackSupervisor.this.mPipModeChangedActivities.size() - 1;
                                        while (true) {
                                            int i4 = i3;
                                            if (i4 >= 0) {
                                                ActivityStackSupervisor.this.mPipModeChangedActivities.remove(i4).updatePictureInPictureMode(ActivityStackSupervisor.this.mPipModeChangedTargetStackBounds, false);
                                                i3 = i4 - 1;
                                            }
                                        }
                                    } catch (Throwable th6) {
                                        while (true) {
                                            ActivityManagerService.resetPriorityAfterLockedSection();
                                            throw th6;
                                            break;
                                        }
                                    }
                                }
                                ActivityManagerService.resetPriorityAfterLockedSection();
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

    @Retention(RetentionPolicy.SOURCE)
    public @interface AnyTaskForIdMatchTaskMode {
    }

    static class FindTaskResult {
        boolean matchedByRootAffinity;
        ActivityRecord r;

        FindTaskResult() {
        }
    }

    static class PendingActivityLaunch {
        final ProcessRecord callerApp;
        final ActivityRecord r;
        final ActivityRecord sourceRecord;
        final ActivityStack stack;
        final int startFlags;

        PendingActivityLaunch(ActivityRecord _r, ActivityRecord _sourceRecord, int _startFlags, ActivityStack _stack, ProcessRecord _callerApp) {
            this.r = _r;
            this.sourceRecord = _sourceRecord;
            this.startFlags = _startFlags;
            this.stack = _stack;
            this.callerApp = _callerApp;
        }

        /* access modifiers changed from: package-private */
        public void sendErrorResult(String message) {
            try {
                if (this.callerApp.thread != null) {
                    this.callerApp.thread.scheduleCrash(message);
                }
            } catch (RemoteException e) {
                Slog.e(ActivityManagerService.TAG, "Exception scheduling crash of failed activity launcher sourceRecord=" + this.sourceRecord, e);
            }
        }
    }

    private final class SleepTokenImpl extends ActivityManagerInternal.SleepToken {
        private final long mAcquireTime = SystemClock.uptimeMillis();
        /* access modifiers changed from: private */
        public final int mDisplayId;
        private final String mTag;

        public SleepTokenImpl(String tag, int displayId) {
            this.mTag = tag;
            this.mDisplayId = displayId;
        }

        public void release() {
            synchronized (ActivityStackSupervisor.this.mService) {
                try {
                    ActivityManagerService.boostPriorityForLockedSection();
                    ActivityStackSupervisor.this.removeSleepTokenLocked(this);
                } catch (Throwable th) {
                    while (true) {
                        ActivityManagerService.resetPriorityAfterLockedSection();
                        throw th;
                    }
                }
            }
            ActivityManagerService.resetPriorityAfterLockedSection();
        }

        public String toString() {
            return "{\"" + this.mTag + "\", display " + this.mDisplayId + ", acquire at " + TimeUtils.formatUptime(this.mAcquireTime) + "}";
        }
    }

    static class WaitInfo {
        /* access modifiers changed from: private */
        public final WaitResult mResult;
        private final ComponentName mTargetComponent;

        public WaitInfo(ComponentName targetComponent, WaitResult result) {
            this.mTargetComponent = targetComponent;
            this.mResult = result;
        }

        public boolean matches(ComponentName targetComponent) {
            return this.mTargetComponent == null || this.mTargetComponent.equals(targetComponent);
        }

        public WaitResult getResult() {
            return this.mResult;
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

    static {
        boolean z = true;
        if (SystemProperties.getInt("ro.logsystem.usertype", 1) != 3) {
            z = false;
        }
        IS_DEBUG_VERSION = z;
        ACTION_TO_RUNTIME_PERMISSION.put("android.media.action.IMAGE_CAPTURE", "android.permission.CAMERA");
        ACTION_TO_RUNTIME_PERMISSION.put("android.media.action.VIDEO_CAPTURE", "android.permission.CAMERA");
        ACTION_TO_RUNTIME_PERMISSION.put("android.intent.action.CALL", "android.permission.CALL_PHONE");
    }

    /* access modifiers changed from: protected */
    public int getChildCount() {
        return this.mActivityDisplays.size();
    }

    /* access modifiers changed from: protected */
    public ActivityDisplay getChildAt(int index) {
        return this.mActivityDisplays.valueAt(index);
    }

    /* access modifiers changed from: protected */
    public ConfigurationContainer getParent() {
        return null;
    }

    /* access modifiers changed from: package-private */
    public Configuration getDisplayOverrideConfiguration(int displayId) {
        ActivityDisplay activityDisplay = getActivityDisplayOrCreateLocked(displayId);
        if (activityDisplay != null) {
            return activityDisplay.getOverrideConfiguration();
        }
        throw new IllegalArgumentException("No display found with id: " + displayId);
    }

    /* access modifiers changed from: package-private */
    public void setDisplayOverrideConfiguration(Configuration overrideConfiguration, int displayId) {
        ActivityDisplay activityDisplay = getActivityDisplayOrCreateLocked(displayId);
        if (activityDisplay != null) {
            activityDisplay.onOverrideConfigurationChanged(overrideConfiguration);
            return;
        }
        throw new IllegalArgumentException("No display found with id: " + displayId);
    }

    /* access modifiers changed from: package-private */
    public boolean canPlaceEntityOnDisplay(int displayId, boolean resizeable, int callingPid, int callingUid, ActivityInfo activityInfo) {
        if (displayId == 0) {
            return true;
        }
        if (!this.mService.mSupportsMultiDisplay) {
            return false;
        }
        if ((resizeable || displayConfigMatchesGlobal(displayId)) && isCallerAllowedToLaunchOnDisplay(callingPid, callingUid, displayId, activityInfo)) {
            return true;
        }
        return false;
    }

    private boolean displayConfigMatchesGlobal(int displayId) {
        if (displayId == 0) {
            return true;
        }
        if (displayId == -1) {
            return false;
        }
        ActivityDisplay targetDisplay = getActivityDisplayOrCreateLocked(displayId);
        if (targetDisplay != null) {
            return getConfiguration().equals(targetDisplay.getConfiguration());
        }
        throw new IllegalArgumentException("No display found with id: " + displayId);
    }

    public ActivityStackSupervisor(ActivityManagerService service, Looper looper) {
        this.mService = service;
        this.mLooper = looper;
        this.mHandler = new ActivityStackSupervisorHandler(looper);
        this.mHwActivityStackSupervisorEx = HwServiceExFactory.getHwActivityStackSupervisorEx(this.mService);
        this.mVrMananger = HwFrameworkFactory.getVRSystemServiceManager();
    }

    public void initialize() {
        if (!this.mInitialized) {
            this.mInitialized = true;
            this.mRunningTasks = createRunningTasks();
            this.mActivityMetricsLogger = new ActivityMetricsLogger(this, this.mService.mContext, this.mHandler.getLooper());
            this.mKeyguardController = new KeyguardController(this.mService, this);
            this.mLaunchParamsController = new LaunchParamsController(this.mService);
            this.mLaunchParamsController.registerDefaultModifiers(this);
        }
    }

    public ActivityMetricsLogger getActivityMetricsLogger() {
        return this.mActivityMetricsLogger;
    }

    /* access modifiers changed from: package-private */
    public LaunchTimeTracker getLaunchTimeTracker() {
        return this.mLaunchTimeTracker;
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
        this.mPowerManager = (PowerManager) this.mService.mContext.getSystemService("power");
        this.mGoingToSleep = this.mPowerManager.newWakeLock(1, "ActivityManager-Sleep");
        this.mLaunchingActivity = this.mPowerManager.newWakeLock(1, "*launch*");
        this.mLaunchingActivity.setReferenceCounted(false);
    }

    /* access modifiers changed from: package-private */
    public void setWindowManager(WindowManagerService wm) {
        synchronized (this.mService) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                this.mWindowManager = wm;
                getKeyguardController().setWindowManager(wm);
                this.mDisplayManager = (DisplayManager) this.mService.mContext.getSystemService("display");
                this.mDisplayManager.registerDisplayListener(this, null);
                this.mDisplayManagerInternal = (DisplayManagerInternal) LocalServices.getService(DisplayManagerInternal.class);
                Display[] displays = this.mDisplayManager.getDisplays();
                for (int displayNdx = displays.length - 1; displayNdx >= 0; displayNdx--) {
                    Display display = displays[displayNdx];
                    ActivityDisplay activityDisplay = new ActivityDisplay(this, display);
                    this.mActivityDisplays.put(display.getDisplayId(), activityDisplay);
                    calculateDefaultMinimalSizeOfResizeableTasks(activityDisplay);
                }
                ActivityStack orCreateStack = getDefaultDisplay().getOrCreateStack(1, 2, true);
                this.mLastFocusedStack = orCreateStack;
                this.mFocusedStack = orCreateStack;
                this.mHomeStack = orCreateStack;
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
    public ActivityStack getFocusedStack() {
        return this.mFocusedStack;
    }

    /* access modifiers changed from: package-private */
    public boolean isFocusable(ConfigurationContainer container, boolean alwaysFocusable) {
        boolean z = false;
        if (container.inSplitScreenPrimaryWindowingMode() && this.mIsDockMinimized) {
            return false;
        }
        if (container.getWindowConfiguration().canReceiveKeys() || alwaysFocusable) {
            z = true;
        }
        return z;
    }

    /* access modifiers changed from: package-private */
    public ActivityStack getLastStack() {
        return this.mLastFocusedStack;
    }

    /* access modifiers changed from: package-private */
    public boolean isFocusedStack(ActivityStack stack) {
        return stack != null && stack == this.mFocusedStack;
    }

    /* access modifiers changed from: package-private */
    public void setFocusStackUnchecked(String reason, ActivityStack focusCandidate) {
        int i;
        if (!focusCandidate.isFocusable()) {
            focusCandidate = getNextFocusableStackLocked(focusCandidate, false);
        }
        if (focusCandidate != this.mFocusedStack) {
            this.mLastFocusedStack = this.mFocusedStack;
            this.mFocusedStack = focusCandidate;
            if (this.mFocusedStack != null && HwFreeFormUtils.isFreeFormEnable() && this.mFocusedStack.isActivityTypeHome()) {
                HwFreeFormManager.getInstance(this.mService.mContext).removeFloatListView();
                ActivityStack freeFormStack = getStack(5, 1);
                if (freeFormStack != null) {
                    freeFormStack.setFreeFormStackVisible(false);
                }
            }
            if (this.mLastFocusedStack != null && this.mVrMananger.isVRDynamicStack(this.mLastFocusedStack.getStackId())) {
                this.mLastFocusedStack.makeStackVisible(false);
            }
            if (this.mFocusedStack != null && this.mFocusedStack.inFreeformWindowingMode()) {
                this.mFocusedStack.setFreeFormStackVisible(true);
            }
            int i2 = this.mCurrentUser;
            int i3 = -1;
            if (this.mFocusedStack == null) {
                i = -1;
            } else {
                i = this.mFocusedStack.getStackId();
            }
            if (this.mLastFocusedStack != null) {
                i3 = this.mLastFocusedStack.getStackId();
            }
            EventLogTags.writeAmFocusedStack(i2, i, i3, reason);
        }
        ActivityRecord r = topRunningActivityLocked();
        if ((this.mService.mBooting || !this.mService.mBooted) && r != null && r.idle) {
            checkFinishBootingLocked();
        }
    }

    /* access modifiers changed from: package-private */
    public void moveHomeStackToFront(String reason) {
        this.mHomeStack.moveToFront(reason);
    }

    /* access modifiers changed from: package-private */
    public void moveRecentsStackToFront(String reason) {
        ActivityStack recentsStack = getDefaultDisplay().getStack(0, 3);
        if (recentsStack != null) {
            recentsStack.moveToFront(reason);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean moveHomeStackTaskToTop(String reason) {
        this.mHomeStack.moveHomeStackTaskToTop();
        ActivityRecord top = getHomeActivity();
        if (top == null) {
            return false;
        }
        moveFocusableActivityStackToFrontLocked(top, reason);
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean resumeHomeStackTask(ActivityRecord prev, String reason) {
        if (!this.mService.mBooting && !this.mService.mBooted) {
            return false;
        }
        this.mHomeStack.moveHomeStackTaskToTop();
        ActivityRecord r = getHomeActivity();
        String myReason = reason + " resumeHomeStackTask";
        if (r == null || r.finishing) {
            return this.mService.startHomeActivityLocked(this.mCurrentUser, myReason);
        }
        moveFocusableActivityStackToFrontLocked(r, myReason);
        return resumeFocusedStackTopActivityLocked(this.mHomeStack, prev, null);
    }

    /* access modifiers changed from: package-private */
    public TaskRecord anyTaskForIdLocked(int id) {
        return anyTaskForIdLocked(id, 2);
    }

    /* access modifiers changed from: package-private */
    public TaskRecord anyTaskForIdLocked(int id, int matchMode) {
        return anyTaskForIdLocked(id, matchMode, null, false);
    }

    /* access modifiers changed from: package-private */
    public TaskRecord anyTaskForIdLocked(int id, int matchMode, ActivityOptions aOptions, boolean onTop) {
        TaskRecord task;
        int i = id;
        int i2 = matchMode;
        ActivityOptions activityOptions = aOptions;
        boolean z = onTop;
        if (i2 == 2 || activityOptions == null) {
            int numDisplays = this.mActivityDisplays.size();
            int displayNdx = 0;
            while (true) {
                int displayNdx2 = displayNdx;
                if (displayNdx2 < numDisplays) {
                    ActivityDisplay display = this.mActivityDisplays.valueAt(displayNdx2);
                    int stackNdx = display.getChildCount() - 1;
                    while (true) {
                        int stackNdx2 = stackNdx;
                        if (stackNdx2 < 0) {
                            break;
                        }
                        ActivityStack stack = display.getChildAt(stackNdx2);
                        TaskRecord task2 = stack.taskForIdLocked(i);
                        if (task2 == null) {
                            stackNdx = stackNdx2 - 1;
                        } else {
                            if (activityOptions != null) {
                                ActivityStack launchStack = getLaunchStack(null, activityOptions, task2, z);
                                if (!(launchStack == null || stack == launchStack)) {
                                    ActivityStack activityStack = launchStack;
                                    task = task2;
                                    ActivityStack activityStack2 = stack;
                                    task2.reparent(launchStack, z, z ? 0 : 2, true, true, "anyTaskForIdLocked");
                                    return task;
                                }
                            }
                            task = task2;
                            ActivityStack activityStack3 = stack;
                            return task;
                        }
                    }
                } else if (i2 == 0) {
                    return null;
                } else {
                    if (ActivityManagerDebugConfig.DEBUG_RECENTS) {
                        Slog.v(ActivityManagerService.TAG, "Looking for task id=" + i + " in recents");
                    }
                    TaskRecord task3 = this.mRecentTasks.getTask(i);
                    if (task3 == null) {
                        if (ActivityManagerDebugConfig.DEBUG_RECENTS) {
                            Slog.d(ActivityManagerService.TAG, "\tDidn't find task id=" + i + " in recents");
                        }
                        return null;
                    } else if (i2 == 1) {
                        return task3;
                    } else {
                        if (!restoreRecentTaskLocked(task3, activityOptions, z)) {
                            if (ActivityManagerDebugConfig.DEBUG_RECENTS) {
                                Slog.w(ActivityManagerService.TAG, "Couldn't restore task id=" + i + " found in recents");
                            }
                            return null;
                        }
                        if (ActivityManagerDebugConfig.DEBUG_RECENTS) {
                            Slog.w(ActivityManagerService.TAG, "Restored task id=" + i + " from in recents");
                        }
                        return task3;
                    }
                }
                displayNdx = displayNdx2 + 1;
            }
        } else {
            throw new IllegalArgumentException("Should not specify activity options for non-restore lookup");
        }
    }

    /* access modifiers changed from: package-private */
    public ActivityRecord isInAnyStackLocked(IBinder token) {
        int numDisplays = this.mActivityDisplays.size();
        for (int displayNdx = 0; displayNdx < numDisplays; displayNdx++) {
            ActivityDisplay display = this.mActivityDisplays.valueAt(displayNdx);
            for (int stackNdx = display.getChildCount() - 1; stackNdx >= 0; stackNdx--) {
                ActivityRecord r = display.getChildAt(stackNdx).isInStackLocked(token);
                if (r != null) {
                    return r;
                }
            }
        }
        return null;
    }

    private boolean taskTopActivityIsUser(TaskRecord task, int userId) {
        ActivityRecord activityRecord = task.getTopActivity();
        ActivityRecord resultTo = activityRecord != null ? activityRecord.resultTo : null;
        return (activityRecord != null && activityRecord.userId == userId) || (resultTo != null && resultTo.userId == userId);
    }

    /* access modifiers changed from: package-private */
    public void lockAllProfileTasks(int userId) {
        this.mWindowManager.deferSurfaceLayout();
        try {
            for (int displayNdx = this.mActivityDisplays.size() - 1; displayNdx >= 0; displayNdx--) {
                ActivityDisplay display = this.mActivityDisplays.valueAt(displayNdx);
                for (int stackNdx = display.getChildCount() - 1; stackNdx >= 0; stackNdx--) {
                    List<TaskRecord> tasks = display.getChildAt(stackNdx).getAllTasks();
                    for (int taskNdx = tasks.size() - 1; taskNdx >= 0; taskNdx--) {
                        TaskRecord task = tasks.get(taskNdx);
                        if (taskTopActivityIsUser(task, userId)) {
                            this.mService.mTaskChangeNotificationController.notifyTaskProfileLocked(task.taskId, userId);
                        }
                    }
                }
            }
        } finally {
            this.mWindowManager.continueSurfaceLayout();
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
            if (this.mRecentTasks.containsTaskId(candidateTaskId, userId) || anyTaskForIdLocked(candidateTaskId, 1) != null) {
                candidateTaskId = nextTaskIdForUser(candidateTaskId, userId);
            } else {
                this.mCurTaskIdForUser.put(userId, candidateTaskId);
                return candidateTaskId;
            }
        } while (candidateTaskId != currentTaskId);
        throw new IllegalStateException("Cannot get an available task id. Reached limit of 100000 running tasks per user.");
    }

    /* access modifiers changed from: package-private */
    public ActivityRecord getResumedActivityLocked() {
        ActivityStack stack = this.mFocusedStack;
        if (stack == null) {
            return null;
        }
        ActivityRecord resumedActivity = stack.getResumedActivity();
        if (resumedActivity == null || resumedActivity.app == null) {
            resumedActivity = stack.mPausingActivity;
            if (resumedActivity == null || resumedActivity.app == null) {
                resumedActivity = stack.topRunningActivityLocked();
            }
        }
        return resumedActivity;
    }

    /* access modifiers changed from: package-private */
    public boolean attachApplicationLocked(ProcessRecord app) throws RemoteException {
        String processName = app.processName;
        boolean didSomething = false;
        for (int displayNdx = this.mActivityDisplays.size() - 1; displayNdx >= 0; displayNdx--) {
            ActivityDisplay display = this.mActivityDisplays.valueAt(displayNdx);
            for (int stackNdx = display.getChildCount() - 1; stackNdx >= 0; stackNdx--) {
                ActivityStack stack = display.getChildAt(stackNdx);
                if (isFocusedStack(stack)) {
                    stack.getAllRunningVisibleActivitiesLocked(this.mTmpActivityList);
                    ActivityRecord top = stack.topRunningActivityLocked();
                    if (top != null && !this.mTmpActivityList.contains(top)) {
                        this.mTmpActivityList.add(top);
                        Slog.d(ActivityManagerService.TAG, "attachApplicationLocked add top running activity: " + top);
                    }
                    int size = this.mTmpActivityList.size();
                    boolean didSomething2 = didSomething;
                    for (int i = 0; i < size; i++) {
                        ActivityRecord activity = this.mTmpActivityList.get(i);
                        if (activity.app == null && app.uid == activity.info.applicationInfo.uid && processName.equals(activity.processName)) {
                            try {
                                if (realStartActivityLocked(activity, app, top == activity, true)) {
                                    didSomething2 = true;
                                }
                            } catch (RemoteException e) {
                                Slog.w(ActivityManagerService.TAG, "Exception in new application when starting activity " + top.intent.getComponent().flattenToShortString(), e);
                                throw e;
                            }
                        }
                    }
                    this.mTmpActivityList.clear();
                    didSomething = didSomething2;
                }
            }
        }
        if (!didSomething) {
            ensureActivitiesVisibleLocked(null, 0, false);
        }
        return didSomething;
    }

    /* access modifiers changed from: package-private */
    public boolean allResumedActivitiesIdle() {
        for (int displayNdx = this.mActivityDisplays.size() - 1; displayNdx >= 0; displayNdx--) {
            ActivityDisplay display = this.mActivityDisplays.valueAt(displayNdx);
            for (int stackNdx = display.getChildCount() - 1; stackNdx >= 0; stackNdx--) {
                ActivityStack stack = display.getChildAt(stackNdx);
                if (isFocusedStack(stack) && stack.numActivities() != 0) {
                    ActivityRecord resumedActivity = stack.getResumedActivity();
                    if (resumedActivity == null || !resumedActivity.idle) {
                        if (ActivityManagerDebugConfig.DEBUG_STATES) {
                            Slog.d(ActivityManagerService.TAG, "allResumedActivitiesIdle: stack=" + stack.mStackId + " " + resumedActivity + " not idle");
                        }
                        return false;
                    }
                }
            }
        }
        sendPowerHintForLaunchEndIfNeeded();
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean allResumedActivitiesComplete() {
        for (int displayNdx = this.mActivityDisplays.size() - 1; displayNdx >= 0; displayNdx--) {
            ActivityDisplay display = this.mActivityDisplays.valueAt(displayNdx);
            for (int stackNdx = display.getChildCount() - 1; stackNdx >= 0; stackNdx--) {
                ActivityStack stack = display.getChildAt(stackNdx);
                if (isFocusedStack(stack)) {
                    ActivityRecord r = stack.getResumedActivity();
                    if (r != null && !r.isState(ActivityStack.ActivityState.RESUMED)) {
                        return false;
                    }
                }
            }
        }
        if (ActivityManagerDebugConfig.DEBUG_STACK != 0) {
            Slog.d(ActivityManagerService.TAG, "allResumedActivitiesComplete: mLastFocusedStack changing from=" + this.mLastFocusedStack + " to=" + this.mFocusedStack);
        }
        this.mLastFocusedStack = this.mFocusedStack;
        return true;
    }

    private boolean allResumedActivitiesVisible() {
        boolean foundResumed = false;
        for (int displayNdx = this.mActivityDisplays.size() - 1; displayNdx >= 0; displayNdx--) {
            ActivityDisplay display = this.mActivityDisplays.valueAt(displayNdx);
            for (int stackNdx = display.getChildCount() - 1; stackNdx >= 0; stackNdx--) {
                ActivityRecord r = display.getChildAt(stackNdx).getResumedActivity();
                if (r != null) {
                    if (!r.nowVisible || this.mActivitiesWaitingForVisibleActivity.contains(r)) {
                        return false;
                    }
                    foundResumed = true;
                }
            }
        }
        return foundResumed;
    }

    /* access modifiers changed from: package-private */
    public boolean pauseBackStacks(boolean userLeaving, ActivityRecord resuming, boolean dontWait) {
        boolean someActivityPaused = false;
        for (int displayNdx = this.mActivityDisplays.size() - 1; displayNdx >= 0; displayNdx--) {
            ActivityDisplay display = this.mActivityDisplays.valueAt(displayNdx);
            for (int stackNdx = display.getChildCount() - 1; stackNdx >= 0; stackNdx--) {
                ActivityStack stack = display.getChildAt(stackNdx);
                if (!isFocusedStack(stack) && stack.getResumedActivity() != null && !keepStackResumed(stack) && !this.mHwActivityStackSupervisorEx.shouldKeepResumedIfFreeFormExist(stack)) {
                    if (ActivityManagerDebugConfig.DEBUG_STATES) {
                        Slog.d(ActivityManagerService.TAG, "pauseBackStacks: stack=" + stack + " mResumedActivity=" + stack.getResumedActivity());
                    }
                    someActivityPaused |= stack.startPausingLocked(userLeaving, false, resuming, dontWait);
                }
            }
        }
        return someActivityPaused;
    }

    /* access modifiers changed from: package-private */
    public boolean allPausedActivitiesComplete() {
        boolean pausing = true;
        for (int displayNdx = this.mActivityDisplays.size() - 1; displayNdx >= 0; displayNdx--) {
            ActivityDisplay display = this.mActivityDisplays.valueAt(displayNdx);
            for (int stackNdx = display.getChildCount() - 1; stackNdx >= 0; stackNdx--) {
                ActivityRecord r = display.getChildAt(stackNdx).mPausingActivity;
                if (r != null && !r.isState(ActivityStack.ActivityState.PAUSED, ActivityStack.ActivityState.STOPPED, ActivityStack.ActivityState.STOPPING)) {
                    if (!ActivityManagerDebugConfig.DEBUG_STATES) {
                        return false;
                    }
                    Slog.d(ActivityManagerService.TAG, "allPausedActivitiesComplete: r=" + r + " state=" + r.getState());
                    pausing = false;
                }
            }
        }
        return pausing;
    }

    /* access modifiers changed from: package-private */
    public void cancelInitializingActivities() {
        for (int displayNdx = this.mActivityDisplays.size() - 1; displayNdx >= 0; displayNdx--) {
            ActivityDisplay display = this.mActivityDisplays.valueAt(displayNdx);
            for (int stackNdx = display.getChildCount() - 1; stackNdx >= 0; stackNdx--) {
                display.getChildAt(stackNdx).cancelInitializingActivities();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void waitActivityVisible(ComponentName name, WaitResult result) {
        this.mWaitingForActivityVisible.add(new WaitInfo(name, result));
    }

    /* access modifiers changed from: package-private */
    public void cleanupActivity(ActivityRecord r) {
        this.mFinishingActivities.remove(r);
        this.mActivitiesWaitingForVisibleActivity.remove(r);
        boolean changed = false;
        for (int i = this.mWaitingForActivityVisible.size() - 1; i >= 0; i--) {
            if (this.mWaitingForActivityVisible.get(i).matches(r.realActivity)) {
                WaitInfo w = this.mWaitingForActivityVisible.remove(i);
                changed = true;
                w.mResult.who = new ComponentName(r.info.packageName, r.info.name);
                w.mResult.totalTime = SystemClock.uptimeMillis() - w.mResult.thisTime;
                w.mResult.thisTime = w.mResult.totalTime;
            }
        }
        for (int i2 = this.mWaitingActivityLaunched.size() - 1; i2 >= 0; i2--) {
            ComponentName cn = this.mWaitingActivityLaunched.get(i2).origin;
            Intent oriIntent = r.intent;
            if (!(cn == null || oriIntent == null || !cn.equals(oriIntent.getComponent()))) {
                WaitResult w2 = this.mWaitingActivityLaunched.remove(i2);
                changed = true;
                w2.who = new ComponentName(r.info.packageName, r.info.name);
                w2.totalTime = SystemClock.uptimeMillis() - w2.thisTime;
                w2.thisTime = w2.totalTime;
            }
        }
        if (changed) {
            Flog.i(101, " cleanupActivity notify r = " + r);
            this.mService.notifyAll();
        }
    }

    /* access modifiers changed from: package-private */
    public void reportActivityVisibleLocked(ActivityRecord r) {
        sendWaitingVisibleReportLocked(r);
    }

    /* access modifiers changed from: package-private */
    public void sendWaitingVisibleReportLocked(ActivityRecord r) {
        boolean changed = false;
        for (int i = this.mWaitingForActivityVisible.size() - 1; i >= 0; i--) {
            WaitInfo w = this.mWaitingForActivityVisible.get(i);
            if (w.matches(r.realActivity)) {
                WaitResult result = w.getResult();
                changed = true;
                result.timeout = false;
                result.who = w.getComponent();
                result.totalTime = SystemClock.uptimeMillis() - result.thisTime;
                result.thisTime = result.totalTime;
                this.mWaitingForActivityVisible.remove(w);
            }
        }
        if (changed) {
            Flog.i(101, "waited activity visible, r=" + r);
            this.mService.notifyAll();
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
                            w.who = r.realActivity;
                        }
                    }
                }
                if (changed) {
                    Flog.i(101, " reportTaskToFrontNoLaunch notify r = " + r);
                    this.mService.notifyAll();
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void reportActivityLaunchedLocked(boolean timeout, ActivityRecord r, long thisTime, long totalTime) {
        boolean changed = false;
        for (int i = this.mWaitingActivityLaunched.size() - 1; i >= 0; i--) {
            WaitResult w = this.mWaitingActivityLaunched.remove(i);
            if (w.who == null) {
                changed = true;
                w.timeout = timeout;
                if (r != null) {
                    w.who = new ComponentName(r.info.packageName, r.info.name);
                }
                w.thisTime = thisTime;
                w.totalTime = totalTime;
            }
        }
        if (changed) {
            Flog.i(101, "waited activity launched, r= " + r);
            this.mService.notifyAll();
        }
    }

    /* access modifiers changed from: package-private */
    public ActivityRecord topRunningActivityLocked() {
        return topRunningActivityLocked(false);
    }

    /* access modifiers changed from: package-private */
    public ActivityRecord topRunningActivityLocked(boolean considerKeyguardState) {
        ActivityStack focusedStack = this.mFocusedStack;
        ActivityRecord r = focusedStack.topRunningActivityLocked();
        if (r != null && isValidTopRunningActivity(r, considerKeyguardState)) {
            return r;
        }
        this.mWindowManager.getDisplaysInFocusOrder(this.mTmpOrderedDisplayIds);
        for (int i = this.mTmpOrderedDisplayIds.size() - 1; i >= 0; i--) {
            int displayId = this.mTmpOrderedDisplayIds.get(i);
            ActivityDisplay display = this.mActivityDisplays.get(displayId);
            if (this.mVrMananger.isVRDeviceConnected() && !HwPCUtils.isValidExtDisplayId(displayId) && !this.mVrMananger.isValidVRDisplayId(displayId)) {
                Slog.i(ActivityManagerService.TAG, "topRunningActivityLocked is not ValidExtDisplayId ,displayId = " + displayId);
            } else if (display == null) {
                continue;
            } else {
                ActivityStack topStack = display.getTopStack();
                if (!(topStack == null || !topStack.isFocusable() || topStack == focusedStack)) {
                    ActivityRecord topActivity = topStack.topRunningActivityLocked();
                    if (topActivity != null && isValidTopRunningActivity(topActivity, considerKeyguardState)) {
                        return topActivity;
                    }
                }
            }
        }
        return null;
    }

    private boolean isValidTopRunningActivity(ActivityRecord record, boolean considerKeyguardState) {
        if (considerKeyguardState && getKeyguardController().isKeyguardLocked()) {
            return record.canShowWhenLocked();
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void getRunningTasks(int maxNum, List<ActivityManager.RunningTaskInfo> list, @WindowConfiguration.ActivityType int ignoreActivityType, @WindowConfiguration.WindowingMode int ignoreWindowingMode, int callingUid, boolean allowed) {
        this.mRunningTasks.getTasks(maxNum, list, ignoreActivityType, ignoreWindowingMode, this.mActivityDisplays, callingUid, allowed);
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
            if (!aInfo.processName.equals("system")) {
                if ((startFlags & 2) != 0) {
                    this.mService.setDebugApp(aInfo.processName, true, false);
                }
                if ((startFlags & 8) != 0) {
                    this.mService.setNativeDebuggingAppLocked(aInfo.applicationInfo, aInfo.processName);
                }
                if ((startFlags & 4) != 0) {
                    this.mService.setTrackAllocationApp(aInfo.applicationInfo, aInfo.processName);
                }
                if (profilerInfo != null) {
                    this.mService.setProfileApp(aInfo.applicationInfo, aInfo.processName, profilerInfo);
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
        try {
            Trace.traceBegin(64, "resolveIntent");
            int modifiedFlags = flags | 65536 | 1024;
            if (intent.isWebIntent() || (intent.getFlags() & 2048) != 0) {
                modifiedFlags |= DumpState.DUMP_VOLUMES;
            }
            int modifiedFlags2 = modifiedFlags;
            long token = Binder.clearCallingIdentity();
            try {
                ResolveInfo resolveIntent = this.mService.getPackageManagerInternalLocked().resolveIntent(intent, resolvedType, modifiedFlags2, userId, true, filterCallingUid);
                Binder.restoreCallingIdentity(token);
                Trace.traceEnd(64);
                return resolveIntent;
            } catch (Throwable th) {
                th = th;
                Trace.traceEnd(64);
                throw th;
            }
        } catch (Throwable th2) {
            th = th2;
            Trace.traceEnd(64);
            throw th;
        }
    }

    /* access modifiers changed from: package-private */
    public ActivityInfo resolveActivity(Intent intent, String resolvedType, int startFlags, ProfilerInfo profilerInfo, int userId, int filterCallingUid) {
        return resolveActivity(intent, resolveIntent(intent, resolvedType, userId, 0, filterCallingUid), startFlags, profilerInfo);
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:203:0x0450, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:204:0x0451, code lost:
        r6 = r2;
        r2 = r7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:205:0x0454, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:206:0x0455, code lost:
        r6 = r2;
        r2 = r7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:221:0x0484, code lost:
        android.util.Slog.e(com.android.server.am.ActivityManagerService.TAG, "Second failure launching " + r6.intent.getComponent().flattenToShortString() + ", giving up", r0);
        r1.mService.appDiedLocked(r3);
        r5 = r32;
        r2.requestFinishActivityLocked(r6.appToken, 0, null, "2nd-crash", false);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:222:0x04bb, code lost:
        endDeferResume();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:223:0x04c0, code lost:
        return false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:224:0x04c1, code lost:
        r5 = r32;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:226:?, code lost:
        r6.launchFailed = true;
        r3.activities.remove(r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:227:0x04cb, code lost:
        throw r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x00aa, code lost:
        if (r2.appInfo.uid != r15) goto L_0x00ac;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:67:0x0143, code lost:
        if (r13.getLockTaskModeState() != 1) goto L_0x0148;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:120:0x0270  */
    /* JADX WARNING: Removed duplicated region for block: B:140:0x030a A[SYNTHETIC, Splitter:B:140:0x030a] */
    /* JADX WARNING: Removed duplicated region for block: B:146:0x0323 A[SYNTHETIC, Splitter:B:146:0x0323] */
    /* JADX WARNING: Removed duplicated region for block: B:151:0x033b A[SYNTHETIC, Splitter:B:151:0x033b] */
    /* JADX WARNING: Removed duplicated region for block: B:178:0x03b8  */
    /* JADX WARNING: Removed duplicated region for block: B:180:0x03d5  */
    /* JADX WARNING: Removed duplicated region for block: B:185:0x03f8  */
    /* JADX WARNING: Removed duplicated region for block: B:189:0x0421  */
    /* JADX WARNING: Removed duplicated region for block: B:192:0x042e  */
    /* JADX WARNING: Removed duplicated region for block: B:205:0x0454 A[ExcHandler: all (th java.lang.Throwable), Splitter:B:129:0x02db] */
    /* JADX WARNING: Removed duplicated region for block: B:211:0x0467  */
    /* JADX WARNING: Removed duplicated region for block: B:221:0x0484 A[Catch:{ all -> 0x04cc }] */
    /* JADX WARNING: Removed duplicated region for block: B:224:0x04c1  */
    /* JADX WARNING: Removed duplicated region for block: B:73:0x014c  */
    public final boolean realStartActivityLocked(ActivityRecord r, ProcessRecord app, boolean andResume, boolean checkConfig) throws RemoteException {
        LockTaskController lockTaskController;
        ActivityRecord activityRecord;
        ActivityStack stack;
        ProfilerInfo profilerInfo;
        MergedConfiguration mergedConfiguration;
        ClientTransaction clientTransaction;
        ActivityStack stack2;
        ActivityLifecycleItem lifecycleItem;
        ActivityStack stack3;
        ActivityRecord activityRecord2 = r;
        ProcessRecord processRecord = app;
        boolean z = andResume;
        if (!allPausedActivitiesComplete()) {
            if (ActivityManagerDebugConfig.DEBUG_SWITCH || ActivityManagerDebugConfig.DEBUG_PAUSE || ActivityManagerDebugConfig.DEBUG_STATES) {
                Slog.v(ActivityManagerService.TAG, "realStartActivityLocked: Skipping start of r=" + activityRecord2 + " some activities pausing...");
            }
            return false;
        }
        TaskRecord task = r.getTask();
        if (task == null) {
            Slog.e(ActivityManagerService.TAG, " null task for ActivityRecord: " + activityRecord2);
            return false;
        }
        ActivityStack stack4 = task.getStack();
        beginDeferResume();
        activityRecord2.startFreezingScreenLocked(processRecord, 0);
        r.startLaunchTickingLocked();
        r.setProcess(app);
        if (getKeyguardController().isKeyguardLocked()) {
            try {
                r.notifyUnknownVisibilityLaunched();
            } catch (Throwable th) {
                profilerInfo = th;
                TaskRecord taskRecord = task;
                ActivityRecord activityRecord3 = activityRecord2;
                ActivityStack activityStack = stack4;
            }
        }
        if (checkConfig) {
            ensureVisibilityAndConfig(activityRecord2, r.getDisplayId(), false, true);
        }
        try {
            if (r.getStack().checkKeyguardVisibility(activityRecord2, true, true)) {
                activityRecord2.setVisibility(true);
            }
            int applicationInfoUid = activityRecord2.info.applicationInfo != null ? activityRecord2.info.applicationInfo.uid : -1;
            if (activityRecord2.userId == processRecord.userId) {
            }
            Slog.wtf(ActivityManagerService.TAG, "User ID for activity changing for " + activityRecord2 + " appInfo.uid=" + activityRecord2.appInfo.uid + " info.ai.uid=" + applicationInfoUid + " old=" + activityRecord2.app + " new=" + processRecord);
            processRecord.waitingToKill = null;
            activityRecord2.launchCount++;
            activityRecord2.lastLaunchTime = SystemClock.uptimeMillis();
            if (ActivityManagerDebugConfig.DEBUG_ALL) {
                Slog.v(ActivityManagerService.TAG, "Launching: " + activityRecord2);
            }
            if (processRecord.activities.indexOf(activityRecord2) < 0) {
                processRecord.activities.add(activityRecord2);
            }
            this.mService.updateLruProcessLocked(processRecord, true, null);
            this.mService.updateOomAdjLocked();
            LockTaskController lockTaskController2 = this.mService.getLockTaskController();
            if (task.mLockTaskAuth != 2) {
                if (task.mLockTaskAuth != 4) {
                    if (task.mLockTaskAuth == 3) {
                    }
                    if (processRecord.thread == null) {
                        List<ResultInfo> results = null;
                        List<ReferrerIntent> newIntents = null;
                        if (z) {
                            try {
                                results = activityRecord2.results;
                                newIntents = activityRecord2.newIntents;
                            } catch (RemoteException e) {
                                e = e;
                                TaskRecord taskRecord2 = task;
                                lockTaskController = lockTaskController2;
                                int i = applicationInfoUid;
                                activityRecord = activityRecord2;
                                stack = stack4;
                                try {
                                    if (!activityRecord.launchFailed) {
                                    }
                                } catch (Throwable th2) {
                                    profilerInfo = th2;
                                    endDeferResume();
                                    throw profilerInfo;
                                }
                            }
                        }
                        List<ResultInfo> results2 = results;
                        if (ActivityManagerDebugConfig.DEBUG_SWITCH) {
                            Slog.v(ActivityManagerService.TAG, "Launching: " + activityRecord2 + " icicle=" + activityRecord2.icicle + " with results=" + results2 + " newIntents=" + newIntents + " andResume=" + z);
                        }
                        EventLog.writeEvent(EventLogTags.AM_RESTART_ACTIVITY, new Object[]{Integer.valueOf(activityRecord2.userId), Integer.valueOf(System.identityHashCode(r)), Integer.valueOf(task.taskId), activityRecord2.shortComponentName});
                        if (r.isActivityTypeHome()) {
                            this.mService.mHomeProcess = task.mActivities.get(0).app;
                            this.mService.mHwAMSEx.reportHomeProcess(this.mService.mHomeProcess);
                        }
                        this.mService.notifyPackageUse(activityRecord2.intent.getComponent().getPackageName(), 0);
                        activityRecord2.sleeping = false;
                        activityRecord2.forceNewConfig = false;
                        this.mService.getAppWarningsLocked().onStartActivity(activityRecord2);
                        this.mService.showAskCompatModeDialogLocked(activityRecord2);
                        activityRecord2.compat = this.mService.compatibilityInfoForPackageLocked(activityRecord2.info.applicationInfo);
                        if (this.mService.mProfileApp != null) {
                            if (this.mService.mProfileApp.equals(processRecord.processName) && (this.mService.mProfileProc == null || this.mService.mProfileProc == processRecord)) {
                                this.mService.mProfileProc = processRecord;
                                ProfilerInfo profilerInfoSvc = this.mService.mProfilerInfo;
                                if (!(profilerInfoSvc == null || profilerInfoSvc.profileFile == null)) {
                                    if (profilerInfoSvc.profileFd != null) {
                                        try {
                                            profilerInfoSvc.profileFd = profilerInfoSvc.profileFd.dup();
                                        } catch (IOException e2) {
                                            profilerInfoSvc.closeFd();
                                        }
                                    }
                                    profilerInfo = new ProfilerInfo(profilerInfoSvc);
                                    processRecord.hasShownUi = true;
                                    processRecord.pendingUiClean = true;
                                    processRecord.forceProcessStateUpTo(this.mService.mTopProcessState);
                                    this.mActivityLaunchTrack = "launchActivity";
                                    if (Jlog.isPerfTest()) {
                                        Jlog.i(3036, Jlog.getMessage("ActivityStackSupervisor", "realStartActivityLocked", Intent.toPkgClsString(activityRecord2.realActivity)));
                                    }
                                    lockTaskController = lockTaskController2;
                                    this.mWindowManager.prepareForForceRotation(activityRecord2.appToken.asBinder(), activityRecord2.info.packageName, processRecord.pid, activityRecord2.info.processName);
                                    mergedConfiguration = new MergedConfiguration(this.mService.getGlobalConfiguration(), r.getMergedOverrideConfiguration());
                                    activityRecord2.setLastReportedConfiguration(mergedConfiguration);
                                    logIfTransactionTooLarge(activityRecord2.intent, activityRecord2.icicle);
                                    clientTransaction = ClientTransaction.obtain(processRecord.thread, activityRecord2.appToken);
                                    MergedConfiguration mergedConfiguration2 = mergedConfiguration;
                                    TaskRecord taskRecord3 = task;
                                    try {
                                        int i2 = applicationInfoUid;
                                        stack2 = stack4;
                                    } catch (RemoteException e3) {
                                        e = e3;
                                        activityRecord = activityRecord2;
                                        stack = stack4;
                                        int i3 = applicationInfoUid;
                                        if (!activityRecord.launchFailed) {
                                        }
                                    } catch (Throwable th3) {
                                    }
                                    try {
                                        clientTransaction.addCallback(LaunchActivityItem.obtain(new Intent(activityRecord2.intent), System.identityHashCode(r), activityRecord2.info, mergedConfiguration.getGlobalConfiguration(), mergedConfiguration.getOverrideConfiguration(), activityRecord2.compat, activityRecord2.launchedFromPackage, task.voiceInteractor, processRecord.repProcState, activityRecord2.icicle, activityRecord2.persistentState, results2, newIntents, this.mService.isNextTransitionForward(), profilerInfo));
                                        if (!z) {
                                            try {
                                                lifecycleItem = ResumeActivityItem.obtain(this.mService.isNextTransitionForward());
                                            } catch (RemoteException e4) {
                                                e = e4;
                                                stack = stack2;
                                                activityRecord = r;
                                                if (!activityRecord.launchFailed) {
                                                }
                                            } catch (Throwable th4) {
                                                profilerInfo = th4;
                                                ActivityStack activityStack2 = stack2;
                                                ActivityRecord activityRecord4 = r;
                                                endDeferResume();
                                                throw profilerInfo;
                                            }
                                        } else {
                                            lifecycleItem = PauseActivityItem.obtain();
                                        }
                                        clientTransaction.setLifecycleStateRequest(lifecycleItem);
                                        this.mService.getLifecycleManager().scheduleTransaction(clientTransaction);
                                        if ((processRecord.info.privateFlags & 2) != 0) {
                                            try {
                                                if (this.mService.mHasHeavyWeightFeature && processRecord.processName.equals(processRecord.info.packageName)) {
                                                    if (this.mService.mHeavyWeightProcess != null) {
                                                        if (this.mService.mHeavyWeightProcess != processRecord) {
                                                            Slog.w(ActivityManagerService.TAG, "Starting new heavy weight process " + processRecord + " when already running " + this.mService.mHeavyWeightProcess);
                                                        }
                                                    }
                                                    this.mService.mHeavyWeightProcess = processRecord;
                                                    Message msg = this.mService.mHandler.obtainMessage(24);
                                                    activityRecord = r;
                                                    try {
                                                        msg.obj = activityRecord;
                                                        this.mService.mHandler.sendMessage(msg);
                                                        endDeferResume();
                                                        activityRecord.launchFailed = false;
                                                        stack3 = stack2;
                                                        if (stack3.updateLRUListLocked(activityRecord)) {
                                                            Slog.w(ActivityManagerService.TAG, "Activity " + activityRecord + " being launched, but already in LRU list");
                                                        }
                                                        if (z || !readyToResume()) {
                                                            if (ActivityManagerDebugConfig.DEBUG_STATES) {
                                                                Slog.v(ActivityManagerService.TAG, "Moving to PAUSED: " + activityRecord + " (starting in paused state)");
                                                            }
                                                            activityRecord.setState(ActivityStack.ActivityState.PAUSED, "realStartActivityLocked");
                                                        } else {
                                                            this.mActivityLaunchTrack += " minmalResume";
                                                            stack3.minimalResumeActivityLocked(activityRecord);
                                                        }
                                                        if (isFocusedStack(stack3)) {
                                                            this.mService.getActivityStartController().startSetupActivity();
                                                        }
                                                        if (activityRecord.app != null) {
                                                            this.mService.mServices.updateServiceConnectionActivitiesLocked(activityRecord.app);
                                                        }
                                                        return true;
                                                    } catch (RemoteException e5) {
                                                        e = e5;
                                                    } catch (Throwable th5) {
                                                        profilerInfo = th5;
                                                        endDeferResume();
                                                        throw profilerInfo;
                                                    }
                                                }
                                            } catch (RemoteException e6) {
                                                e = e6;
                                                activityRecord = r;
                                                stack = stack2;
                                                if (!activityRecord.launchFailed) {
                                                }
                                            } catch (Throwable th6) {
                                                profilerInfo = th6;
                                                ActivityRecord activityRecord5 = r;
                                                endDeferResume();
                                                throw profilerInfo;
                                            }
                                        }
                                        activityRecord = r;
                                        endDeferResume();
                                        activityRecord.launchFailed = false;
                                        stack3 = stack2;
                                        if (stack3.updateLRUListLocked(activityRecord)) {
                                        }
                                        if (z) {
                                        }
                                        if (ActivityManagerDebugConfig.DEBUG_STATES) {
                                        }
                                        activityRecord.setState(ActivityStack.ActivityState.PAUSED, "realStartActivityLocked");
                                        if (isFocusedStack(stack3)) {
                                        }
                                        if (activityRecord.app != null) {
                                        }
                                        return true;
                                    } catch (RemoteException e7) {
                                        e = e7;
                                        stack = stack2;
                                        activityRecord = r;
                                        if (!activityRecord.launchFailed) {
                                        }
                                    } catch (Throwable th7) {
                                        profilerInfo = th7;
                                        ActivityStack activityStack3 = stack2;
                                        ActivityRecord activityRecord6 = r;
                                        endDeferResume();
                                        throw profilerInfo;
                                    }
                                }
                            }
                        }
                        profilerInfo = null;
                        processRecord.hasShownUi = true;
                        processRecord.pendingUiClean = true;
                        processRecord.forceProcessStateUpTo(this.mService.mTopProcessState);
                        this.mActivityLaunchTrack = "launchActivity";
                        if (Jlog.isPerfTest()) {
                        }
                        lockTaskController = lockTaskController2;
                        try {
                            this.mWindowManager.prepareForForceRotation(activityRecord2.appToken.asBinder(), activityRecord2.info.packageName, processRecord.pid, activityRecord2.info.processName);
                            mergedConfiguration = new MergedConfiguration(this.mService.getGlobalConfiguration(), r.getMergedOverrideConfiguration());
                            activityRecord2.setLastReportedConfiguration(mergedConfiguration);
                            logIfTransactionTooLarge(activityRecord2.intent, activityRecord2.icicle);
                            clientTransaction = ClientTransaction.obtain(processRecord.thread, activityRecord2.appToken);
                            MergedConfiguration mergedConfiguration22 = mergedConfiguration;
                            TaskRecord taskRecord32 = task;
                            int i22 = applicationInfoUid;
                            stack2 = stack4;
                        } catch (RemoteException e8) {
                            e = e8;
                            TaskRecord taskRecord4 = task;
                            int i4 = applicationInfoUid;
                            activityRecord = activityRecord2;
                            stack = stack4;
                            if (!activityRecord.launchFailed) {
                            }
                        }
                        try {
                            clientTransaction.addCallback(LaunchActivityItem.obtain(new Intent(activityRecord2.intent), System.identityHashCode(r), activityRecord2.info, mergedConfiguration.getGlobalConfiguration(), mergedConfiguration.getOverrideConfiguration(), activityRecord2.compat, activityRecord2.launchedFromPackage, task.voiceInteractor, processRecord.repProcState, activityRecord2.icicle, activityRecord2.persistentState, results2, newIntents, this.mService.isNextTransitionForward(), profilerInfo));
                            if (!z) {
                            }
                            clientTransaction.setLifecycleStateRequest(lifecycleItem);
                            this.mService.getLifecycleManager().scheduleTransaction(clientTransaction);
                            if ((processRecord.info.privateFlags & 2) != 0) {
                            }
                            activityRecord = r;
                            endDeferResume();
                            activityRecord.launchFailed = false;
                            stack3 = stack2;
                            if (stack3.updateLRUListLocked(activityRecord)) {
                            }
                            if (z) {
                            }
                            if (ActivityManagerDebugConfig.DEBUG_STATES) {
                            }
                            activityRecord.setState(ActivityStack.ActivityState.PAUSED, "realStartActivityLocked");
                            if (isFocusedStack(stack3)) {
                            }
                            if (activityRecord.app != null) {
                            }
                            return true;
                        } catch (RemoteException e9) {
                            e = e9;
                            activityRecord = activityRecord2;
                            stack = stack2;
                            if (!activityRecord.launchFailed) {
                            }
                        } catch (Throwable th8) {
                            profilerInfo = th8;
                            ActivityRecord activityRecord7 = activityRecord2;
                            ActivityStack activityStack4 = stack2;
                            endDeferResume();
                            throw profilerInfo;
                        }
                    } else {
                        lockTaskController = lockTaskController2;
                        int i5 = applicationInfoUid;
                        activityRecord = activityRecord2;
                        stack = stack4;
                        try {
                            throw new RemoteException();
                        } catch (RemoteException e10) {
                            e = e10;
                            if (!activityRecord.launchFailed) {
                            }
                        }
                    }
                }
            }
            lockTaskController2.startLockTaskMode(task, false, 0);
            try {
                if (processRecord.thread == null) {
                }
            } catch (RemoteException e11) {
                e = e11;
                TaskRecord taskRecord5 = task;
                lockTaskController = lockTaskController2;
                int i6 = applicationInfoUid;
                activityRecord = activityRecord2;
                stack = stack4;
                if (!activityRecord.launchFailed) {
                }
            }
        } catch (Throwable th9) {
            profilerInfo = th9;
            TaskRecord taskRecord6 = task;
            ActivityRecord activityRecord8 = activityRecord2;
            ActivityStack activityStack5 = stack4;
            endDeferResume();
            throw profilerInfo;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean ensureVisibilityAndConfig(ActivityRecord starting, int displayId, boolean markFrozenIfConfigChanged, boolean deferResume) {
        IApplicationToken.Stub stub = null;
        ensureActivitiesVisibleLocked(null, 0, false, false);
        WindowManagerService windowManagerService = this.mWindowManager;
        Configuration displayOverrideConfiguration = getDisplayOverrideConfiguration(displayId);
        if (starting != null && starting.mayFreezeScreenLocked(starting.app)) {
            stub = starting.appToken;
        }
        Configuration config = windowManagerService.updateOrientationFromAppTokens(displayOverrideConfiguration, stub, displayId, true);
        if (!(starting == null || !markFrozenIfConfigChanged || config == null)) {
            starting.frozenBeforeDestroy = true;
        }
        return this.mService.updateDisplayOverrideConfigurationLocked(config, starting, deferResume, displayId);
    }

    private void logIfTransactionTooLarge(Intent intent, Bundle icicle) {
        int extrasSize = 0;
        if (intent != null) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                extrasSize = extras.getSize();
            }
        }
        int icicleSize = icicle == null ? 0 : icicle.getSize();
        if (extrasSize + icicleSize > 200000) {
            Slog.e(ActivityManagerService.TAG, "Transaction too large, intent: " + intent + ", extras size: " + extrasSize + ", icicle size: " + icicleSize);
        }
    }

    /* access modifiers changed from: protected */
    public void handlePCWindowStateChanged() {
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x00ca A[SYNTHETIC, Splitter:B:29:0x00ca] */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x0113  */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x013a  */
    public void startSpecificActivityLocked(ActivityRecord r, boolean andResume, boolean checkConfig) {
        ActivityRecord activityRecord = r;
        boolean isStartingLauncher = true;
        ProcessRecord app = this.mService.getProcessRecordLocked(activityRecord.processName, activityRecord.info.applicationInfo.uid, true);
        getLaunchTimeTracker().setLaunchTime(activityRecord);
        if (activityRecord.app != null && this.mService.mSystemReady) {
            this.mService.getDAMonitor().noteActivityDisplayedStart(activityRecord.shortComponentName, activityRecord.app.uid, activityRecord.app.pid);
        }
        if (app == null || app.thread == null) {
            boolean z = andResume;
            boolean z2 = checkConfig;
        } else {
            try {
                if ((activityRecord.info.flags & 1) == 0 || !PackageManagerService.PLATFORM_PACKAGE_NAME.equals(activityRecord.info.packageName)) {
                    app.addPackage(activityRecord.info.packageName, activityRecord.info.applicationInfo.longVersionCode, this.mService.mProcessStats);
                }
                try {
                    realStartActivityLocked(activityRecord, app, andResume, checkConfig);
                    return;
                } catch (RemoteException e) {
                    e = e;
                    Slog.w(ActivityManagerService.TAG, "Exception when starting activity " + activityRecord.intent.getComponent().flattenToShortString(), e);
                    Flog.i(101, "start process for launching activity: " + activityRecord);
                    isStartingLauncher = false;
                    if (isStartingLauncher) {
                    }
                    if (!this.mService.mUserController.hasStartedUserState(activityRecord.userId)) {
                    }
                }
            } catch (RemoteException e2) {
                e = e2;
                boolean z3 = andResume;
                boolean z4 = checkConfig;
                Slog.w(ActivityManagerService.TAG, "Exception when starting activity " + activityRecord.intent.getComponent().flattenToShortString(), e);
                Flog.i(101, "start process for launching activity: " + activityRecord);
                isStartingLauncher = false;
                if (isStartingLauncher) {
                }
                if (!this.mService.mUserController.hasStartedUserState(activityRecord.userId)) {
                }
            }
        }
        Flog.i(101, "start process for launching activity: " + activityRecord);
        if (activityRecord.intent.getComponent() == null || !"com.huawei.android.launcher".equals(activityRecord.intent.getComponent().getPackageName())) {
            isStartingLauncher = false;
        }
        if (isStartingLauncher) {
            try {
                this.mService.getPackageManagerInternalLocked().checkPackageStartable(activityRecord.intent.getComponent().getPackageName(), UserHandle.getUserId(activityRecord.info.applicationInfo.uid));
            } catch (SecurityException e3) {
                Slog.i(ActivityManagerService.TAG, "skip launch freezen hwLauncher for uid: " + activityRecord.info.applicationInfo.uid);
                return;
            }
        }
        if (!this.mService.mUserController.hasStartedUserState(activityRecord.userId)) {
            Slog.w(ActivityManagerService.TAG, "skip launch r : " + activityRecord + ": user " + activityRecord.userId + " is stopped");
            return;
        }
        if (!startProcessOnExtDisplay(r)) {
            this.mService.mHwAMSEx.setHbsMiniAppUid(activityRecord.info.applicationInfo, activityRecord.intent);
            this.mService.startProcessLocked(activityRecord.processName, activityRecord.info.applicationInfo, true, 0, "activity", activityRecord.intent.getComponent(), false, false, true);
        }
    }

    /* access modifiers changed from: protected */
    public boolean startProcessOnExtDisplay(ActivityRecord r) {
        return false;
    }

    /* access modifiers changed from: package-private */
    public void sendPowerHintForLaunchStartIfNeeded(boolean forceSend, ActivityRecord targetActivity) {
        boolean sendHint = forceSend;
        if (!sendHint) {
            ActivityRecord resumedActivity = getResumedActivityLocked();
            sendHint = resumedActivity == null || resumedActivity.app == null || !resumedActivity.app.equals(targetActivity.app);
        }
        if (sendHint && this.mService.mLocalPowerManager != null) {
            this.mService.mLocalPowerManager.powerHint(8, 1);
            this.mPowerHintSent = true;
        }
    }

    /* access modifiers changed from: package-private */
    public void sendPowerHintForLaunchEndIfNeeded() {
        if (this.mPowerHintSent && this.mService.mLocalPowerManager != null) {
            this.mService.mLocalPowerManager.powerHint(8, 0);
            this.mPowerHintSent = false;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean checkStartAnyActivityPermission(Intent intent, ActivityInfo aInfo, String resultWho, int requestCode, int callingPid, int callingUid, String callingPackage, boolean ignoreTargetSecurity, boolean launchingInTask, ProcessRecord callerApp, ActivityRecord resultRecord, ActivityStack resultStack) {
        String msg;
        ActivityInfo activityInfo = aInfo;
        int i = callingPid;
        int i2 = callingUid;
        ProcessRecord processRecord = callerApp;
        boolean isCallerRecents = this.mService.getRecentTasks() != null && this.mService.getRecentTasks().isCallerRecents(i2);
        if (this.mService.checkPermission("android.permission.START_ANY_ACTIVITY", i, i2) == 0 || (isCallerRecents && launchingInTask)) {
            String str = callingPackage;
            return true;
        }
        String str2 = callingPackage;
        int componentRestriction = getComponentRestrictionForCallingPackage(activityInfo, str2, i, i2, ignoreTargetSecurity);
        int actionRestriction = getActionRestrictionForCallingPackage(intent.getAction(), str2, i, i2);
        if (componentRestriction == 1 || actionRestriction == 1) {
            if (resultRecord != null) {
                resultStack.sendActivityResultLocked(-1, resultRecord, resultWho, requestCode, 0, null);
            }
            if (actionRestriction == 1) {
                msg = "Permission Denial: starting " + intent.toString() + " from " + processRecord + " (pid=" + i + ", uid=" + i2 + ") with revoked permission " + ACTION_TO_RUNTIME_PERMISSION.get(intent.getAction());
            } else if (!activityInfo.exported) {
                msg = "Permission Denial: starting " + intent.toString() + " from " + processRecord + " (pid=" + i + ", uid=" + i2 + ") not exported from uid " + activityInfo.applicationInfo.uid;
            } else {
                msg = "Permission Denial: starting " + intent.toString() + " from " + processRecord + " (pid=" + i + ", uid=" + i2 + ") requires " + activityInfo.permission;
            }
            Slog.w(ActivityManagerService.TAG, msg);
            throw new SecurityException(msg);
        } else if (actionRestriction == 2) {
            Slog.w(ActivityManagerService.TAG, "Appop Denial: starting " + intent.toString() + " from " + processRecord + " (pid=" + i + ", uid=" + i2 + ") requires " + AppOpsManager.permissionToOp(ACTION_TO_RUNTIME_PERMISSION.get(intent.getAction())));
            return false;
        } else if (componentRestriction != 2) {
            return true;
        } else {
            Slog.w(ActivityManagerService.TAG, "Appop Denial: starting " + intent.toString() + " from " + processRecord + " (pid=" + i + ", uid=" + i2 + ") requires appop " + AppOpsManager.permissionToOp(activityInfo.permission));
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isCallerAllowedToLaunchOnDisplay(int callingPid, int callingUid, int launchDisplayId, ActivityInfo aInfo) {
        if (ActivityManagerDebugConfig.DEBUG_TASKS) {
            Slog.d(ActivityManagerService.TAG, "Launch on display check: displayId=" + launchDisplayId + " callingPid=" + callingPid + " callingUid=" + callingUid);
        }
        if (callingPid == -1 && callingUid == -1) {
            if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                Slog.d(ActivityManagerService.TAG, "Launch on display check: no caller info, skip check");
            }
            return true;
        }
        ActivityDisplay activityDisplay = getActivityDisplayOrCreateLocked(launchDisplayId);
        if (activityDisplay == null) {
            Slog.w(ActivityManagerService.TAG, "Launch on display check: display not found");
            return false;
        } else if (this.mService.checkPermission("android.permission.INTERNAL_SYSTEM_WINDOW", callingPid, callingUid) == 0) {
            if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                Slog.d(ActivityManagerService.TAG, "Launch on display check: allow launch any on display");
            }
            return true;
        } else {
            boolean uidPresentOnDisplay = activityDisplay.isUidPresent(callingUid);
            int displayOwnerUid = activityDisplay.mDisplay.getOwnerUid();
            if (activityDisplay.mDisplay.getType() == 5 && displayOwnerUid != 1000 && displayOwnerUid != aInfo.applicationInfo.uid && !HwPCUtils.isPcCastModeInServer()) {
                if ((aInfo.flags & Integer.MIN_VALUE) == 0) {
                    if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                        Slog.d(ActivityManagerService.TAG, "Launch on display check: disallow launch on virtual display for not-embedded activity.");
                    }
                    return false;
                } else if (this.mService.checkPermission("android.permission.ACTIVITY_EMBEDDING", callingPid, callingUid) == -1 && !uidPresentOnDisplay) {
                    if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                        Slog.d(ActivityManagerService.TAG, "Launch on display check: disallow activity embedding without permission.");
                    }
                    return false;
                }
            }
            if (!activityDisplay.isPrivate()) {
                if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                    Slog.d(ActivityManagerService.TAG, "Launch on display check: allow launch on public display");
                }
                return true;
            } else if (displayOwnerUid == callingUid) {
                if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                    Slog.d(ActivityManagerService.TAG, "Launch on display check: allow launch for owner of the display");
                }
                return true;
            } else if (uidPresentOnDisplay) {
                if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                    Slog.d(ActivityManagerService.TAG, "Launch on display check: allow launch for caller present on the display");
                }
                return true;
            } else {
                Slog.w(ActivityManagerService.TAG, "Launch on display check: denied");
                return false;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void updateUIDsPresentOnDisplay() {
        this.mDisplayAccessUIDs.clear();
        for (int displayNdx = this.mActivityDisplays.size() - 1; displayNdx >= 0; displayNdx--) {
            ActivityDisplay activityDisplay = this.mActivityDisplays.valueAt(displayNdx);
            if (activityDisplay.isPrivate()) {
                this.mDisplayAccessUIDs.append(activityDisplay.mDisplayId, activityDisplay.getPresentUIDs());
            }
        }
        this.mDisplayManagerInternal.setDisplayAccessUIDs(this.mDisplayAccessUIDs);
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
        if (!ignoreTargetSecurity) {
            if (this.mService.checkComponentPermission(activityInfo.permission, callingPid, callingUid, activityInfo.applicationInfo.uid, activityInfo.exported) == -1) {
                return 1;
            }
        }
        if (activityInfo.permission == null) {
            return 0;
        }
        int opCode = AppOpsManager.permissionToOpCode(activityInfo.permission);
        if (opCode == -1 || this.mService.mAppOpsService.noteOperation(opCode, callingUid, callingPackage) == 0 || ignoreTargetSecurity) {
            return 0;
        }
        return 2;
    }

    private int getActionRestrictionForCallingPackage(String action, String callingPackage, int callingPid, int callingUid) {
        if (action == null) {
            return 0;
        }
        String permission = ACTION_TO_RUNTIME_PERMISSION.get(action);
        if (permission == null) {
            return 0;
        }
        try {
            if (!ArrayUtils.contains(this.mService.mContext.getPackageManager().getPackageInfo(callingPackage, 4096).requestedPermissions, permission)) {
                return 0;
            }
            if (this.mService.checkPermission(permission, callingPid, callingUid) == -1) {
                return 1;
            }
            int opCode = AppOpsManager.permissionToOpCode(permission);
            if (opCode == -1 || this.mService.mAppOpsService.noteOperation(opCode, callingUid, callingPackage) == 0) {
                return 0;
            }
            return 2;
        } catch (PackageManager.NameNotFoundException e) {
            Slog.i(ActivityManagerService.TAG, "Cannot find package info for " + callingPackage);
            return 0;
        }
    }

    /* access modifiers changed from: package-private */
    public void setLaunchSource(int uid) {
        this.mLaunchingActivity.setWorkSource(new WorkSource(uid));
    }

    /* access modifiers changed from: package-private */
    public void acquireLaunchWakelock() {
        this.mLaunchingActivity.acquire();
        if (!this.mHandler.hasMessages(104)) {
            this.mHandler.sendEmptyMessageDelayed(104, JobStatus.DEFAULT_TRIGGER_UPDATE_DELAY);
        }
    }

    @GuardedBy("mService")
    private boolean checkFinishBootingLocked() {
        boolean booting = this.mService.mBooting;
        boolean enableScreen = false;
        this.mService.mBooting = false;
        if (!this.mService.mBooted) {
            this.mService.mBooted = true;
            enableScreen = true;
        }
        if (booting || enableScreen) {
            this.mService.postFinishBooting(booting, enableScreen);
        }
        return booting;
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r12v1, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v3, resolved type: com.android.server.am.ActivityRecord} */
    /* access modifiers changed from: package-private */
    /* JADX WARNING: Multi-variable type inference failed */
    @GuardedBy("mService")
    public final ActivityRecord activityIdleInternalLocked(IBinder token, boolean fromTimeout, boolean processPausingActivities, Configuration config) {
        ArrayList<ActivityRecord> finishes;
        boolean z;
        ActivityRecord r;
        ArrayList<ActivityRecord> finishes2;
        Configuration configuration = config;
        if (ActivityManagerDebugConfig.DEBUG_ALL) {
            Slog.v(ActivityManagerService.TAG, "Activity idle: " + token);
        } else {
            IBinder iBinder = token;
        }
        ArrayList<UserState> startingUsers = null;
        boolean booting = false;
        boolean activityRemoved = false;
        ActivityRecord r2 = ActivityRecord.forTokenLocked(token);
        if (r2 != null) {
            if (ActivityManagerDebugConfig.DEBUG_IDLE) {
                Slog.d(ActivityManagerService.TAG, "activityIdleInternalLocked: Callers=" + Debug.getCallers(4));
            }
            this.mHandler.removeMessages(100, r2);
            r2.finishLaunchTickingLocked();
            if (fromTimeout) {
                r = r2;
                finishes = null;
                z = true;
                reportActivityLaunchedLocked(fromTimeout, r2, -1, -1);
            } else {
                r = r2;
                finishes = null;
                z = true;
            }
            if (configuration != null) {
                r.setLastReportedGlobalConfiguration(configuration);
            }
            r.idle = z;
            if (r.app != null && r.app.foregroundActivities) {
                this.mService.mHwAMSEx.noteActivityStart(r.app.info.packageName, r.app.processName, r.realActivity != null ? r.realActivity.getClassName() : BluetoothManagerService.DEFAULT_PACKAGE_NAME, r.app.pid, r.app.uid, false);
            }
            if (isFocusedStack(r.getStack()) || fromTimeout) {
                booting = checkFinishBootingLocked();
            }
        } else {
            r = r2;
            finishes = null;
            z = true;
        }
        if (allResumedActivitiesIdle()) {
            if (r != null) {
                this.mService.scheduleAppGcsLocked();
            }
            if (this.mLaunchingActivity.isHeld()) {
                this.mHandler.removeMessages(104);
                this.mLaunchingActivity.release();
            }
            ensureActivitiesVisibleLocked(null, 0, false);
        }
        ArrayList<ActivityRecord> stops = processStoppingActivitiesLocked(r, z, processPausingActivities);
        int NS = stops != null ? stops.size() : 0;
        int size = this.mFinishingActivities.size();
        int NF = size;
        if (size > 0) {
            finishes2 = new ArrayList<>(this.mFinishingActivities);
            this.mFinishingActivities.clear();
        } else {
            finishes2 = finishes;
        }
        if (this.mStartingUsers.size() > 0) {
            startingUsers = new ArrayList<>(this.mStartingUsers);
            this.mStartingUsers.clear();
        }
        for (int i = 0; i < NS; i++) {
            r = stops.get(i);
            ActivityStack stack = r.getStack();
            if (stack != null) {
                if (r.finishing) {
                    stack.finishCurrentActivityLocked(r, 0, false, "activityIdleInternalLocked");
                } else {
                    stack.stopActivityLocked(r);
                }
            }
        }
        for (int i2 = 0; i2 < NF; i2++) {
            r = finishes2.get(i2);
            ActivityStack stack2 = r.getStack();
            if (stack2 != null) {
                activityRemoved |= stack2.destroyActivityLocked(r, z, "finish-idle");
            }
        }
        if (!booting && startingUsers != null) {
            for (int i3 = 0; i3 < startingUsers.size(); i3++) {
                this.mService.mUserController.finishUserSwitch(startingUsers.get(i3));
            }
        }
        this.mService.trimApplications();
        if (activityRemoved) {
            resumeFocusedStackTopActivityLocked();
        }
        return r;
    }

    /* access modifiers changed from: package-private */
    public boolean handleAppDiedLocked(ProcessRecord app) {
        boolean hasVisibleActivities = false;
        for (int displayNdx = this.mActivityDisplays.size() - 1; displayNdx >= 0; displayNdx--) {
            ActivityDisplay display = this.mActivityDisplays.valueAt(displayNdx);
            for (int stackNdx = display.getChildCount() - 1; stackNdx >= 0; stackNdx--) {
                hasVisibleActivities |= display.getChildAt(stackNdx).handleAppDiedLocked(app);
            }
        }
        return hasVisibleActivities;
    }

    /* access modifiers changed from: package-private */
    public void closeSystemDialogsLocked() {
        for (int displayNdx = this.mActivityDisplays.size() - 1; displayNdx >= 0; displayNdx--) {
            ActivityDisplay display = this.mActivityDisplays.valueAt(displayNdx);
            for (int stackNdx = display.getChildCount() - 1; stackNdx >= 0; stackNdx--) {
                display.getChildAt(stackNdx).closeSystemDialogsLocked();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void removeUserLocked(int userId) {
        this.mUserStackInFront.delete(userId);
    }

    /* access modifiers changed from: package-private */
    public void updateUserStackLocked(int userId, ActivityStack stack) {
        if (userId != this.mCurrentUser) {
            this.mUserStackInFront.put(userId, stack != null ? stack.getStackId() : this.mHomeStack.mStackId);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean finishDisabledPackageActivitiesLocked(String packageName, Set<String> filterByClasses, boolean doit, boolean evenPersistent, int userId) {
        boolean didSomething = false;
        for (int displayNdx = this.mActivityDisplays.size() - 1; displayNdx >= 0; displayNdx--) {
            ActivityDisplay display = this.mActivityDisplays.valueAt(displayNdx);
            for (int stackNdx = display.getChildCount() - 1; stackNdx >= 0; stackNdx--) {
                if (display.getChildAt(stackNdx).finishDisabledPackageActivitiesLocked(packageName, filterByClasses, doit, evenPersistent, userId)) {
                    didSomething = true;
                }
            }
        }
        return didSomething;
    }

    /* access modifiers changed from: package-private */
    public void updatePreviousProcessLocked(ActivityRecord r) {
        ProcessRecord fgApp = null;
        for (int displayNdx = this.mActivityDisplays.size() - 1; displayNdx >= 0; displayNdx--) {
            ActivityDisplay display = this.mActivityDisplays.valueAt(displayNdx);
            int stackNdx = display.getChildCount() - 1;
            while (true) {
                if (stackNdx < 0) {
                    break;
                }
                ActivityStack stack = display.getChildAt(stackNdx);
                if (isFocusedStack(stack)) {
                    ActivityRecord resumedActivity = stack.getResumedActivity();
                    if (resumedActivity != null) {
                        fgApp = resumedActivity.app;
                    } else if (stack.mPausingActivity != null) {
                        fgApp = stack.mPausingActivity.app;
                    }
                } else {
                    stackNdx--;
                }
            }
        }
        if (r.app != null && fgApp != null && r.app != fgApp && r.lastVisibleTime > this.mService.mPreviousProcessVisibleTime && r.app != this.mService.mHomeProcess) {
            this.mService.mPreviousProcess = r.app;
            this.mService.mPreviousProcessVisibleTime = r.lastVisibleTime;
            this.mService.mHwAMSEx.reportPreviousInfo(12, r.app);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean resumeFocusedStackTopActivityLocked() {
        return resumeFocusedStackTopActivityLocked(null, null, null);
    }

    /* access modifiers changed from: package-private */
    public boolean resumeFocusedStackTopActivityLocked(ActivityStack targetStack, ActivityRecord target, ActivityOptions targetOptions) {
        if (!readyToResume()) {
            if (ActivityManagerDebugConfig.DEBUG_STACK) {
                Slog.d(ActivityManagerService.TAG, "It is not ready to resume");
            }
            return false;
        } else if (targetStack != null && isFocusedStack(targetStack) && !resumeAppLockActivityIfNeeded(targetStack, targetOptions)) {
            return targetStack.resumeTopActivityUncheckedLocked(target, targetOptions);
        } else {
            ActivityRecord r = this.mFocusedStack.topRunningActivityLocked();
            if (r == null || !r.isState(ActivityStack.ActivityState.RESUMED)) {
                if (!resumeAppLockActivityIfNeeded(this.mFocusedStack, targetOptions)) {
                    this.mFocusedStack.resumeTopActivityUncheckedLocked(null, null);
                }
            } else if (r.isState(ActivityStack.ActivityState.RESUMED)) {
                if (HwPCUtils.isPcCastModeInServer()) {
                    this.mFocusedStack.resumeTopActivityUncheckedLocked(null, null);
                } else {
                    this.mFocusedStack.executeAppTransition(targetOptions);
                }
            }
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public void updateActivityApplicationInfoLocked(ApplicationInfo aInfo) {
        for (int displayNdx = this.mActivityDisplays.size() - 1; displayNdx >= 0; displayNdx--) {
            ActivityDisplay display = this.mActivityDisplays.valueAt(displayNdx);
            for (int stackNdx = display.getChildCount() - 1; stackNdx >= 0; stackNdx--) {
                display.getChildAt(stackNdx).updateActivityApplicationInfoLocked(aInfo);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public TaskRecord finishTopCrashedActivitiesLocked(ProcessRecord app, String reason) {
        TaskRecord finishedTask = null;
        ActivityStack focusedStack = getFocusedStack();
        for (int displayNdx = this.mActivityDisplays.size() - 1; displayNdx >= 0; displayNdx--) {
            ActivityDisplay display = this.mActivityDisplays.valueAt(displayNdx);
            for (int stackNdx = 0; stackNdx < display.getChildCount(); stackNdx++) {
                ActivityStack stack = display.getChildAt(stackNdx);
                TaskRecord t = stack.finishTopCrashedActivityLocked(app, reason);
                if (stack == focusedStack || finishedTask == null) {
                    finishedTask = t;
                }
            }
        }
        return finishedTask;
    }

    /* access modifiers changed from: package-private */
    public void finishVoiceTask(IVoiceInteractionSession session) {
        for (int displayNdx = this.mActivityDisplays.size() - 1; displayNdx >= 0; displayNdx--) {
            ActivityDisplay display = this.mActivityDisplays.valueAt(displayNdx);
            int numStacks = display.getChildCount();
            for (int stackNdx = 0; stackNdx < numStacks; stackNdx++) {
                display.getChildAt(stackNdx).finishVoiceTask(session);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void findTaskToMoveToFront(TaskRecord task, int flags, ActivityOptions options, String reason, boolean forceNonResizeable) {
        AppTimeTracker appTimeTracker;
        TaskRecord taskRecord = task;
        ActivityOptions activityOptions = options;
        ActivityStack currentStack = task.getStack();
        if (currentStack == null) {
            Slog.e(ActivityManagerService.TAG, "findTaskToMoveToFront: can't move task=" + taskRecord + " to front. Stack is null");
            return;
        }
        if ((flags & 2) == 0) {
            this.mUserLeaving = true;
        }
        ActivityRecord prev = topRunningActivityLocked();
        if ((flags & 1) != 0 || (prev != null && prev.isActivityTypeRecents())) {
            moveHomeStackToFront("findTaskToMoveToFront");
        }
        if (task.isResizeable() && canUseActivityOptionsLaunchBounds(activityOptions)) {
            Rect bounds = options.getLaunchBounds();
            taskRecord.updateOverrideConfiguration(bounds);
            ActivityStack stack = getLaunchStack(null, activityOptions, taskRecord, true);
            if (stack != currentStack) {
                taskRecord.reparent(stack, true, 1, false, true, "findTaskToMoveToFront");
                stack = currentStack;
            }
            if (stack.resizeStackWithLaunchBounds()) {
                ActivityStack activityStack = stack;
                resizeStackLocked(stack, bounds, null, null, false, true, false);
            } else {
                task.resizeWindowContainer();
            }
        }
        ActivityRecord r = task.getTopActivity();
        if (r == null) {
            appTimeTracker = null;
        } else {
            appTimeTracker = r.appTimeTracker;
        }
        currentStack.moveTaskToFrontLocked(taskRecord, false, activityOptions, appTimeTracker, reason);
        if (ActivityManagerDebugConfig.DEBUG_STACK) {
            Slog.d(ActivityManagerService.TAG, "findTaskToMoveToFront: moved to front of stack=" + currentStack);
        }
        handleNonResizableTaskIfNeeded(taskRecord, 0, 0, currentStack, forceNonResizeable);
    }

    /* access modifiers changed from: package-private */
    public boolean canUseActivityOptionsLaunchBounds(ActivityOptions options) {
        boolean z = false;
        if (options == null || options.getLaunchBounds() == null) {
            return false;
        }
        if ((this.mService.mSupportsPictureInPicture && options.getLaunchWindowingMode() == 2) || this.mService.mSupportsFreeformWindowManagement) {
            z = true;
        }
        return z;
    }

    /* access modifiers changed from: package-private */
    public LaunchParamsController getLaunchParamsController() {
        return this.mLaunchParamsController;
    }

    /* access modifiers changed from: protected */
    public <T extends ActivityStack> T getStack(int stackId) {
        for (int i = this.mActivityDisplays.size() - 1; i >= 0; i--) {
            T stack = this.mActivityDisplays.valueAt(i).getStack(stackId);
            if (stack != null) {
                return stack;
            }
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public <T extends ActivityStack> T getStack(int windowingMode, int activityType) {
        for (int i = this.mActivityDisplays.size() - 1; i >= 0; i--) {
            T stack = this.mActivityDisplays.valueAt(i).getStack(windowingMode, activityType);
            if (stack != null) {
                return stack;
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public int resolveActivityType(ActivityRecord r, ActivityOptions options, TaskRecord task) {
        int activityType = r != null ? r.getActivityType() : 0;
        if (activityType == 0 && task != null) {
            activityType = task.getActivityType();
        }
        if (activityType != 0) {
            return activityType;
        }
        if (options != null) {
            activityType = options.getLaunchActivityType();
        }
        return activityType != 0 ? activityType : 1;
    }

    /* access modifiers changed from: package-private */
    public <T extends ActivityStack> T getLaunchStack(ActivityRecord r, ActivityOptions options, TaskRecord candidateTask, boolean onTop) {
        return getLaunchStack(r, options, candidateTask, onTop, -1);
    }

    /* access modifiers changed from: package-private */
    public <T extends ActivityStack> T getLaunchStack(ActivityRecord r, ActivityOptions options, TaskRecord candidateTask, boolean onTop, int candidateDisplayId) {
        boolean z;
        ActivityDisplay display;
        ActivityRecord activityRecord = r;
        ActivityOptions activityOptions = options;
        TaskRecord taskRecord = candidateTask;
        int taskId = -1;
        int displayId = -1;
        if (activityOptions != null) {
            taskId = options.getLaunchTaskId();
            displayId = options.getLaunchDisplayId();
        }
        int taskId2 = taskId;
        if (taskId2 != -1) {
            activityOptions.setLaunchTaskId(-1);
            z = onTop;
            TaskRecord task = anyTaskForIdLocked(taskId2, 2, activityOptions, z);
            activityOptions.setLaunchTaskId(taskId2);
            if (task != null) {
                return task.getStack();
            }
        } else {
            z = onTop;
        }
        int activityType = resolveActivityType(r, options, candidateTask);
        T stack = null;
        if (displayId == -1) {
            displayId = candidateDisplayId;
        }
        int displayId2 = displayId;
        if (displayId2 != -1 && canLaunchOnDisplay(activityRecord, displayId2)) {
            if (activityRecord != null) {
                stack = getValidLaunchStackOnDisplay(displayId2, activityRecord);
                if (stack != null) {
                    return stack;
                }
            }
            T stack2 = stack;
            ActivityDisplay display2 = getActivityDisplayOrCreateLocked(displayId2);
            if (display2 != null) {
                T stack3 = display2.getOrCreateStack(activityRecord, activityOptions, taskRecord, activityType, z);
                if (stack3 != null) {
                    return stack3;
                }
            }
        }
        T stack4 = null;
        ActivityDisplay display3 = null;
        if (taskRecord != null) {
            stack4 = candidateTask.getStack();
        }
        if (stack4 == null && activityRecord != null) {
            stack4 = r.getStack();
        }
        T stack5 = stack4;
        if (stack5 != null) {
            display3 = stack5.getDisplay();
            if (display3 != null && canLaunchOnDisplay(activityRecord, display3.mDisplayId)) {
                int windowingMode = display3.resolveWindowingMode(activityRecord, activityOptions, taskRecord, activityType);
                if (stack5.isCompatible(windowingMode, activityType)) {
                    return stack5;
                }
                if (windowingMode == 4 && display3.getSplitScreenPrimaryStack() == stack5 && taskRecord == stack5.topTask()) {
                    return stack5;
                }
            }
        }
        if (display3 == null || !canLaunchOnDisplay(activityRecord, display3.mDisplayId) || !(activityType == 1 || activityType == 0)) {
            display = getDefaultDisplay();
        } else {
            display = display3;
        }
        return display.getOrCreateStack(activityRecord, activityOptions, taskRecord, activityType, z);
    }

    private boolean canLaunchOnDisplay(ActivityRecord r, int displayId) {
        if (r == null) {
            return true;
        }
        return r.canBeLaunchedOnDisplay(displayId);
    }

    /* access modifiers changed from: protected */
    public ActivityStack getValidLaunchStackOnDisplay(int displayId, ActivityRecord r) {
        ActivityDisplay activityDisplay = getActivityDisplayOrCreateLocked(displayId);
        if (activityDisplay == null) {
            throw new IllegalArgumentException("Display with displayId=" + displayId + " not found.");
        } else if (!r.canBeLaunchedOnDisplay(displayId)) {
            return null;
        } else {
            for (int i = activityDisplay.getChildCount() - 1; i >= 0; i--) {
                ActivityStack stack = activityDisplay.getChildAt(i);
                if (isValidLaunchStack(stack, displayId, r)) {
                    return stack;
                }
            }
            if (displayId != 0) {
                return activityDisplay.createStack(r.getWindowingMode(), r.getActivityType(), true);
            }
            Slog.w(ActivityManagerService.TAG, "getValidLaunchStackOnDisplay: can't launch on displayId " + displayId);
            return null;
        }
    }

    private boolean isValidLaunchStack(ActivityStack stack, int displayId, ActivityRecord r) {
        switch (stack.getActivityType()) {
            case 2:
                return r.isActivityTypeHome();
            case 3:
                return r.isActivityTypeRecents();
            case 4:
                return r.isActivityTypeAssistant();
            default:
                switch (stack.getWindowingMode()) {
                    case 1:
                        return true;
                    case 2:
                        return r.supportsPictureInPicture();
                    case 3:
                        return r.supportsSplitScreenWindowingMode();
                    case 4:
                        return r.supportsSplitScreenWindowingMode();
                    case 5:
                        return r.supportsFreeform();
                    default:
                        if (HwPCUtils.isPcDynamicStack(stack.getStackId()) && this.mService.mSupportsMultiDisplay && HwPCUtils.isPcCastModeInServer()) {
                            return true;
                        }
                        if (!stack.isOnHomeDisplay()) {
                            return r.canBeLaunchedOnDisplay(displayId);
                        }
                        Slog.e(ActivityManagerService.TAG, "isValidLaunchStack: Unexpected stack=" + stack);
                        return false;
                }
        }
    }

    /* access modifiers changed from: package-private */
    public ActivityStack getNextFocusableStackLocked(ActivityStack currentFocus, boolean ignoreCurrent) {
        this.mWindowManager.getDisplaysInFocusOrder(this.mTmpOrderedDisplayIds);
        int currentWindowingMode = 0;
        boolean isFreeFormStack = currentFocus != null ? currentFocus.inFreeformWindowingMode() : false;
        if (currentFocus != null) {
            currentWindowingMode = currentFocus.getWindowingMode();
        }
        ActivityStack candidate = null;
        this.mHwActivityStackSupervisorEx.adjustFocusDisplayOrder(this.mTmpOrderedDisplayIds, HwPCUtils.getPCDisplayID());
        for (int i = this.mTmpOrderedDisplayIds.size() - 1; i >= 0; i--) {
            int displayId = this.mTmpOrderedDisplayIds.get(i);
            if (this.mVrMananger.allowDisplayFocusByID(displayId)) {
                ActivityDisplay display = getActivityDisplayOrCreateLocked(displayId);
                if (display == null) {
                    continue;
                } else {
                    for (int j = display.getChildCount() - 1; j >= 0; j--) {
                        ActivityStack stack = display.getChildAt(j);
                        if ((!ignoreCurrent || stack != currentFocus) && stack.isFocusable() && (stack.shouldBeVisible(null) || isFreeFormStack || this.mVrMananger.isVRDynamicStack(stack.getStackId()))) {
                            if (currentWindowingMode == 4 && candidate == null && stack.inSplitScreenPrimaryWindowingMode()) {
                                candidate = stack;
                            } else if (candidate == null || !stack.inSplitScreenSecondaryWindowingMode()) {
                                return stack;
                            } else {
                                return candidate;
                            }
                        }
                    }
                    continue;
                }
            }
        }
        return candidate;
    }

    /* access modifiers changed from: package-private */
    public ActivityStack getNextValidLaunchStackLocked(ActivityRecord r, int currentFocus) {
        this.mWindowManager.getDisplaysInFocusOrder(this.mTmpOrderedDisplayIds);
        for (int i = this.mTmpOrderedDisplayIds.size() - 1; i >= 0; i--) {
            int displayId = this.mTmpOrderedDisplayIds.get(i);
            if (displayId != currentFocus) {
                ActivityStack stack = getValidLaunchStackOnDisplay(displayId, r);
                if (stack != null) {
                    return stack;
                }
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public ActivityRecord getHomeActivity() {
        return getHomeActivityForUser(this.mCurrentUser);
    }

    /* access modifiers changed from: package-private */
    public ActivityRecord getHomeActivityForUser(int userId) {
        ArrayList<TaskRecord> tasks = this.mHomeStack.getAllTasks();
        for (int taskNdx = tasks.size() - 1; taskNdx >= 0; taskNdx--) {
            TaskRecord task = tasks.get(taskNdx);
            if (task.isActivityTypeHome()) {
                ArrayList<ActivityRecord> activities = task.mActivities;
                for (int activityNdx = activities.size() - 1; activityNdx >= 0; activityNdx--) {
                    ActivityRecord r = activities.get(activityNdx);
                    if (r.isActivityTypeHome() && (userId == -1 || r.userId == userId)) {
                        return r;
                    }
                }
                continue;
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public void resizeStackLocked(ActivityStack stack, Rect bounds, Rect tempTaskBounds, Rect tempTaskInsetBounds, boolean preserveWindows, boolean allowResizeInDockedMode, boolean deferResume) {
        ActivityStack activityStack = stack;
        if (activityStack.inSplitScreenPrimaryWindowingMode()) {
            resizeDockedStackLocked(bounds, tempTaskBounds, tempTaskInsetBounds, null, null, preserveWindows, deferResume);
            return;
        }
        boolean splitScreenActive = getDefaultDisplay().hasSplitScreenPrimaryStack();
        if (allowResizeInDockedMode || activityStack.getWindowConfiguration().tasksAreFloating() || !splitScreenActive) {
            Trace.traceBegin(64, "am.resizeStack_" + activityStack.mStackId);
            this.mWindowManager.deferSurfaceLayout();
            try {
                if (activityStack.affectedBySplitScreenResize()) {
                    if (bounds == null && activityStack.inSplitScreenWindowingMode()) {
                        activityStack.setWindowingMode(1);
                    } else if (splitScreenActive) {
                        activityStack.setWindowingMode(4);
                    }
                }
                activityStack.resize(bounds, tempTaskBounds, tempTaskInsetBounds);
                if (!deferResume) {
                    try {
                        activityStack.ensureVisibleActivitiesConfigurationLocked(activityStack.topRunningActivityLocked(), preserveWindows);
                    } catch (Throwable th) {
                        th = th;
                    }
                } else {
                    boolean z = preserveWindows;
                }
                this.mWindowManager.continueSurfaceLayout();
                Trace.traceEnd(64);
            } catch (Throwable th2) {
                th = th2;
                boolean z2 = preserveWindows;
                this.mWindowManager.continueSurfaceLayout();
                Trace.traceEnd(64);
                throw th;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void deferUpdateRecentsHomeStackBounds() {
        deferUpdateBounds(3);
        deferUpdateBounds(2);
    }

    /* access modifiers changed from: package-private */
    public void deferUpdateBounds(int activityType) {
        ActivityStack stack = getStack(0, activityType);
        if (stack != null) {
            stack.deferUpdateBounds();
        }
    }

    /* access modifiers changed from: package-private */
    public void continueUpdateRecentsHomeStackBounds() {
        continueUpdateBounds(3);
        continueUpdateBounds(2);
    }

    /* access modifiers changed from: package-private */
    public void continueUpdateBounds(int activityType) {
        ActivityStack stack = getStack(0, activityType);
        if (stack != null) {
            stack.continueUpdateBounds();
        }
    }

    /* access modifiers changed from: package-private */
    public void notifyAppTransitionDone() {
        continueUpdateRecentsHomeStackBounds();
        for (int i = this.mResizingTasksDuringAnimation.size() - 1; i >= 0; i--) {
            TaskRecord task = anyTaskForIdLocked(this.mResizingTasksDuringAnimation.valueAt(i).intValue(), 0);
            if (task != null) {
                try {
                    task.setTaskDockedResizing(false);
                } catch (IllegalArgumentException e) {
                }
            }
        }
        this.mResizingTasksDuringAnimation.clear();
    }

    /* access modifiers changed from: private */
    public void moveTasksToFullscreenStackInSurfaceTransaction(ActivityStack fromStack, int toDisplayId, boolean onTop) {
        ActivityDisplay toDisplay;
        ActivityStack activityStack = fromStack;
        this.mWindowManager.deferSurfaceLayout();
        try {
            int windowingMode = fromStack.getWindowingMode();
            boolean inPinnedWindowingMode = windowingMode == 2;
            ActivityDisplay toDisplay2 = getActivityDisplay(toDisplayId);
            if (windowingMode == 3) {
                toDisplay2.onExitingSplitScreenMode();
                int i = toDisplay2.getChildCount() - 1;
                while (true) {
                    int i2 = i;
                    if (i2 < 0) {
                        break;
                    }
                    ActivityStack otherStack = toDisplay2.getChildAt(i2);
                    if (otherStack.inSplitScreenSecondaryWindowingMode()) {
                        ActivityStack activityStack2 = otherStack;
                        resizeStackLocked(otherStack, null, null, null, true, true, true);
                    }
                    i = i2 - 1;
                }
                this.mAllowDockedStackResize = false;
                Flog.i(101, "The dock stack was dismissed! With fromStack = " + activityStack);
                activityStack.mWindowContainerController.resetBounds();
            }
            boolean schedulePictureInPictureModeChange = inPinnedWindowingMode;
            ArrayList<TaskRecord> tasks = fromStack.getAllTasks();
            if (!tasks.isEmpty()) {
                this.mTmpOptions.setLaunchWindowingMode(1);
                int size = tasks.size();
                int i3 = 0;
                while (true) {
                    int i4 = i3;
                    if (i4 >= size) {
                        break;
                    }
                    TaskRecord task = tasks.get(i4);
                    TaskRecord task2 = task;
                    int i5 = i4;
                    ActivityStack toStack = toDisplay2.getOrCreateStack(null, this.mTmpOptions, task, task.getActivityType(), onTop);
                    if (onTop) {
                        toDisplay = toDisplay2;
                        task2.reparent(toStack, true, 0, i5 == size + -1, true, schedulePictureInPictureModeChange, "moveTasksToFullscreenStack - onTop");
                        TaskRecord task3 = task2;
                        MetricsLoggerWrapper.logPictureInPictureFullScreen(this.mService.mContext, task3.effectiveUid, task3.realActivity.flattenToString());
                    } else {
                        toDisplay = toDisplay2;
                        task2.reparent(toStack, true, 2, false, true, schedulePictureInPictureModeChange, "moveTasksToFullscreenStack - NOT_onTop");
                    }
                    int i6 = toDisplayId;
                    i3 = i5 + 1;
                    toDisplay2 = toDisplay;
                }
            }
            ensureActivitiesVisibleLocked(null, 0, true);
            resumeFocusedStackTopActivityLocked();
        } finally {
            this.mAllowDockedStackResize = true;
            this.mWindowManager.continueSurfaceLayout();
        }
    }

    /* access modifiers changed from: package-private */
    public void moveTasksToFullscreenStackLocked(ActivityStack fromStack, boolean onTop) {
        moveTasksToFullscreenStackLocked(fromStack, 0, onTop);
    }

    /* access modifiers changed from: package-private */
    public void moveTasksToFullscreenStackLocked(ActivityStack fromStack, int toDisplayId, boolean onTop) {
        this.mWindowManager.inSurfaceTransaction(new Runnable(fromStack, toDisplayId, onTop) {
            private final /* synthetic */ ActivityStack f$1;
            private final /* synthetic */ int f$2;
            private final /* synthetic */ boolean f$3;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
            }

            public final void run() {
                ActivityStackSupervisor.this.moveTasksToFullscreenStackInSurfaceTransaction(this.f$1, this.f$2, this.f$3);
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

    /* JADX WARNING: Removed duplicated region for block: B:48:0x00f2  */
    /* JADX WARNING: Removed duplicated region for block: B:52:0x00fa  */
    private void resizeDockedStackLocked(Rect dockedBounds, Rect tempDockedTaskBounds, Rect tempDockedTaskInsetBounds, Rect tempOtherTaskBounds, Rect tempOtherTaskInsetBounds, boolean preserveWindows, boolean deferResume) {
        ActivityDisplay display;
        Rect otherTaskRect;
        int i;
        Rect rect = dockedBounds;
        if (this.mAllowDockedStackResize) {
            ActivityStack stack = getDefaultDisplay().getSplitScreenPrimaryStack();
            if (stack == null) {
                Slog.w(ActivityManagerService.TAG, "resizeDockedStackLocked: docked stack not found");
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
                    stack.resize(rect, tempDockedTaskBounds, tempDockedTaskInsetBounds);
                    if (stack.getWindowingMode() != 1) {
                        if (rect != null || stack.isAttached()) {
                            ActivityDisplay display2 = getDefaultDisplay();
                            Rect otherTaskRect2 = new Rect();
                            int i2 = display2.getChildCount() - 1;
                            while (true) {
                                int i3 = i2;
                                if (i3 < 0) {
                                    break;
                                }
                                ActivityStack current = display2.getChildAt(i3);
                                if (current.getWindowingMode() != 3) {
                                    if (current.affectedBySplitScreenResize()) {
                                        if (!this.mDockedStackResizing || current.isTopActivityVisible()) {
                                            current.setWindowingMode(4);
                                            Rect rect2 = tempOtherTaskBounds;
                                            current.getStackDockedModeBounds(rect2, this.tempRect, otherTaskRect2, true);
                                            Rect rect3 = !this.tempRect.isEmpty() ? this.tempRect : null;
                                            Rect rect4 = !otherTaskRect2.isEmpty() ? otherTaskRect2 : rect2;
                                            ActivityStack activityStack = current;
                                            Rect rect5 = rect3;
                                            ActivityStack activityStack2 = current;
                                            Rect rect6 = rect4;
                                            i = i3;
                                            otherTaskRect = otherTaskRect2;
                                            display = display2;
                                            resizeStackLocked(activityStack, rect5, rect6, tempOtherTaskInsetBounds, preserveWindows, true, deferResume);
                                            i2 = i - 1;
                                            Rect rect7 = tempDockedTaskInsetBounds;
                                            otherTaskRect2 = otherTaskRect;
                                            display2 = display;
                                        }
                                    }
                                }
                                i = i3;
                                otherTaskRect = otherTaskRect2;
                                display = display2;
                                i2 = i - 1;
                                Rect rect72 = tempDockedTaskInsetBounds;
                                otherTaskRect2 = otherTaskRect;
                                display2 = display;
                            }
                            if (deferResume) {
                                try {
                                    stack.ensureVisibleActivitiesConfigurationLocked(r, preserveWindows);
                                } catch (Throwable th) {
                                    th = th;
                                }
                            } else {
                                boolean z = preserveWindows;
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
                } catch (Throwable th2) {
                    th = th2;
                    boolean z2 = preserveWindows;
                    this.mAllowDockedStackResize = true;
                    this.mWindowManager.continueSurfaceLayout();
                    Trace.traceEnd(64);
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                Rect rect8 = tempDockedTaskBounds;
                boolean z22 = preserveWindows;
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
            PinnedActivityStack stack = getDefaultDisplay().getPinnedStack();
            if (stack == null) {
                Slog.w(ActivityManagerService.TAG, "resizePinnedStackLocked: pinned stack not found");
            } else if (!((PinnedStackWindowController) stack.getWindowContainerController()).pinnedStackResizeDisallowed()) {
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
            HwPCUtils.log(ActivityManagerService.TAG, "ignore pinned stack in pad pc mode");
        }
    }

    /* access modifiers changed from: private */
    public void removeStackInSurfaceTransaction(ActivityStack stack) {
        ArrayList<TaskRecord> tasks = stack.getAllTasks();
        if (stack.getWindowingMode() == 2) {
            PinnedActivityStack pinnedStack = (PinnedActivityStack) stack;
            pinnedStack.mForceHidden = true;
            pinnedStack.ensureActivitiesVisibleLocked(null, 0, true);
            pinnedStack.mForceHidden = false;
            activityIdleInternalLocked(null, false, true, null);
            moveTasksToFullscreenStackLocked(pinnedStack, false);
            return;
        }
        for (int i = tasks.size() - 1; i >= 0; i--) {
            removeTaskByIdLocked(tasks.get(i).taskId, true, true, "remove-stack");
        }
    }

    /* access modifiers changed from: package-private */
    public void removeStack(ActivityStack stack) {
        this.mWindowManager.inSurfaceTransaction(new Runnable(stack) {
            private final /* synthetic */ ActivityStack f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                ActivityStackSupervisor.this.removeStackInSurfaceTransaction(this.f$1);
            }
        });
    }

    /* access modifiers changed from: package-private */
    public void removeStacksInWindowingModes(int... windowingModes) {
        for (int i = this.mActivityDisplays.size() - 1; i >= 0; i--) {
            this.mActivityDisplays.valueAt(i).removeStacksInWindowingModes(windowingModes);
        }
    }

    /* access modifiers changed from: package-private */
    public void removeStacksWithActivityTypes(int... activityTypes) {
        for (int i = this.mActivityDisplays.size() - 1; i >= 0; i--) {
            this.mActivityDisplays.valueAt(i).removeStacksWithActivityTypes(activityTypes);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean removeTaskByIdLocked(int taskId, boolean killProcess, boolean removeFromRecents, String reason) {
        return removeTaskByIdLocked(taskId, killProcess, removeFromRecents, false, reason);
    }

    /* access modifiers changed from: package-private */
    public boolean removeTaskByIdLocked(int taskId, boolean killProcess, boolean removeFromRecents, boolean pauseImmediately, String reason) {
        TaskRecord tr = anyTaskForIdLocked(taskId, 1);
        if (tr != null) {
            if (!(tr.getBaseIntent() == null || tr.getBaseIntent().getComponent() == null)) {
                String packageName = tr.getBaseIntent().getComponent().getPackageName();
                if (HwDeviceManager.disallowOp(3, packageName)) {
                    Slog.i(ActivityManagerService.TAG, "[" + packageName + "] is Persistent app,won't be killed");
                    UiThread.getHandler().post(new Runnable() {
                        public void run() {
                            Context context = ActivityStackSupervisor.this.mService.mUiContext;
                            if (context != null) {
                                Toast toast = Toast.makeText(context, context.getString(33686141), 0);
                                toast.getWindowParams().privateFlags |= 16;
                                toast.show();
                            }
                        }
                    });
                    return false;
                } else if (HwPCUtils.isPcCastModeInServer()) {
                    if (HwPCUtils.enabledInPad() && "com.android.incallui".equals(packageName)) {
                        Slog.i(ActivityManagerService.TAG, "[" + packageName + "] is Service app,won't be killed");
                        return false;
                    } else if (!killProcess && "com.chinamworld.main".equals(packageName)) {
                        Slog.i(ActivityManagerService.TAG, "[" + packageName + "] remove task and kill process in pc mode");
                        tr.removeTaskActivitiesLocked(pauseImmediately, reason);
                        cleanUpRemovedTaskLocked(tr, true, removeFromRecents);
                        if (tr.isPersistable) {
                            this.mService.notifyTaskPersisterLocked(null, true);
                        }
                        return true;
                    }
                }
            }
            if (HwPCUtils.enabledInPad() && HwPCUtils.isPcCastModeInServer() && tr.getStack() != null) {
                tr.getStack().resetOtherStacksVisible(true);
            }
            tr.removeTaskActivitiesLocked(pauseImmediately, reason);
            cleanUpRemovedTaskLocked(tr, killProcess, removeFromRecents);
            this.mService.getLockTaskController().clearLockedTask(tr);
            if (tr.isPersistable) {
                this.mService.notifyTaskPersisterLocked(null, true);
            }
            return true;
        }
        Slog.w(ActivityManagerService.TAG, "Request to remove task ignored for non-existent task " + taskId);
        return false;
    }

    /* access modifiers changed from: package-private */
    public void cleanUpRemovedTaskLocked(TaskRecord tr, boolean killProcess, boolean removeFromRecents) {
        TaskRecord taskRecord = tr;
        if (removeFromRecents) {
            this.mRecentTasks.remove(taskRecord);
        }
        ComponentName component = tr.getBaseIntent().getComponent();
        if (component == null) {
            Slog.w(ActivityManagerService.TAG, "No component for base intent of task: " + taskRecord);
            return;
        }
        this.mService.mServices.cleanUpRemovedTaskLocked(taskRecord, component, new Intent(tr.getBaseIntent()));
        if (killProcess) {
            String pkg = component.getPackageName();
            if (!shouldNotKillProcWhenRemoveTask(pkg)) {
                ArrayList arrayList = new ArrayList();
                ArrayMap<String, SparseArray<ProcessRecord>> pmap = this.mService.mProcessNames.getMap();
                List<String> superWhiteListApp = HwDeviceManager.getList(22);
                int i = 0;
                while (i < pmap.size()) {
                    SparseArray<ProcessRecord> uids = pmap.valueAt(i);
                    int j = 0;
                    while (j < uids.size()) {
                        ProcessRecord proc = uids.valueAt(j);
                        if (superWhiteListApp != null && superWhiteListApp.contains(proc.info.packageName)) {
                            Slog.i(ActivityManagerService.TAG, "[" + proc.info.packageName + "] is super-whitelist app,won't be killed by remove task");
                        } else if (proc.userId == taskRecord.userId && proc != this.mService.mHomeProcess && proc.pkgList.containsKey(pkg)) {
                            int k = 0;
                            while (k < proc.activities.size()) {
                                TaskRecord otherTask = proc.activities.get(k).getTask();
                                if (otherTask == null || taskRecord.taskId == otherTask.taskId || !otherTask.inRecents) {
                                    k++;
                                    taskRecord = tr;
                                } else {
                                    return;
                                }
                            }
                            if (proc.foregroundServices == 0) {
                                arrayList.add(proc);
                            } else {
                                return;
                            }
                        }
                        j++;
                        taskRecord = tr;
                    }
                    i++;
                    taskRecord = tr;
                }
                int i2 = 0;
                while (true) {
                    int i3 = i2;
                    if (i3 < arrayList.size()) {
                        ProcessRecord pr = (ProcessRecord) arrayList.get(i3);
                        if (pr != null) {
                            if (pr.curAdj >= 900 && (pr.info.flags & 1) != 0 && (pr.info.hwFlags & DumpState.DUMP_HANDLE) == 0 && notKillProcessWhenRemoveTask(pr)) {
                                Slog.d(ActivityManagerService.TAG, " the process " + pr.processName + " adj >= " + 900);
                                try {
                                    SmartShrinker.reclaim(pr.pid, 4);
                                    if (pr.thread != null) {
                                        pr.thread.scheduleTrimMemory(80);
                                    }
                                } catch (RemoteException e) {
                                }
                                pr.trimMemoryLevel = 80;
                            } else if (pr.setSchedGroup != 0 || !pr.curReceivers.isEmpty()) {
                                pr.waitingToKill = "remove task";
                            } else {
                                pr.kill("remove task", true);
                            }
                        }
                        i2 = i3 + 1;
                    } else {
                        return;
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean restoreRecentTaskLocked(TaskRecord task, ActivityOptions aOptions, boolean onTop) {
        ActivityStack stack = getLaunchStack(null, aOptions, task, onTop);
        ActivityStack currentStack = task.getStack();
        if (currentStack != null) {
            if (currentStack == stack) {
                return true;
            }
            currentStack.removeTask(task, "restoreRecentTaskLocked", 1);
        }
        stack.addTask(task, onTop, "restoreRecentTask");
        task.createWindowContainer(onTop, true);
        if (ActivityManagerDebugConfig.DEBUG_RECENTS) {
            Slog.v(ActivityManagerService.TAG, "Added restored task=" + task + " to stack=" + stack);
        }
        ArrayList<ActivityRecord> activities = task.mActivities;
        for (int activityNdx = activities.size() - 1; activityNdx >= 0; activityNdx--) {
            activities.get(activityNdx).createWindowContainer(activities.get(activityNdx).info.navigationHide);
        }
        return true;
    }

    public void onRecentTaskAdded(TaskRecord task) {
        task.touchActiveTime();
    }

    public void onRecentTaskRemoved(TaskRecord task, boolean wasTrimmed) {
        if (wasTrimmed) {
            removeTaskByIdLocked(task.taskId, false, false, false, "recent-task-trimmed");
        }
        task.removedFromRecents();
    }

    /* access modifiers changed from: package-private */
    public void moveStackToDisplayLocked(int stackId, int displayId, boolean onTop) {
        ActivityDisplay activityDisplay = getActivityDisplayOrCreateLocked(displayId);
        if (activityDisplay != null) {
            ActivityStack stack = getStack(stackId);
            if (stack != null) {
                ActivityDisplay currentDisplay = stack.getDisplay();
                if (currentDisplay == null) {
                    throw new IllegalStateException("moveStackToDisplayLocked: Stack with stack=" + stack + " is not attached to any display.");
                } else if (currentDisplay.mDisplayId != displayId) {
                    stack.reparent(activityDisplay, onTop);
                } else {
                    throw new IllegalArgumentException("Trying to move stack=" + stack + " to its current displayId=" + displayId);
                }
            } else {
                throw new IllegalArgumentException("moveStackToDisplayLocked: Unknown stackId=" + stackId);
            }
        } else {
            throw new IllegalArgumentException("moveStackToDisplayLocked: Unknown displayId=" + displayId);
        }
    }

    /* access modifiers changed from: package-private */
    public ActivityStack getReparentTargetStack(TaskRecord task, ActivityStack stack, boolean toTop) {
        ActivityStack prevStack = task.getStack();
        int stackId = stack.mStackId;
        boolean inMultiWindowMode = stack.inMultiWindowMode();
        if (prevStack != null && prevStack.mStackId == stackId) {
            Slog.w(ActivityManagerService.TAG, "Can not reparent to same stack, task=" + task + " already in stackId=" + stackId);
            return prevStack;
        } else if (inMultiWindowMode && !this.mService.mSupportsMultiWindow) {
            throw new IllegalArgumentException("Device doesn't support multi-window, can not reparent task=" + task + " to stack=" + stack);
        } else if (stack.mDisplayId != 0 && !this.mService.mSupportsMultiDisplay) {
            throw new IllegalArgumentException("Device doesn't support multi-display, can not reparent task=" + task + " to stackId=" + stackId);
        } else if (stack.getWindowingMode() != 5 || this.mService.mSupportsFreeformWindowManagement) {
            if (inMultiWindowMode && !task.isResizeable()) {
                Slog.w(ActivityManagerService.TAG, "Can not move unresizeable task=" + task + " to multi-window stack=" + stack + " Moving to a fullscreen stack instead.");
                if (prevStack != null) {
                    return prevStack;
                }
                stack = stack.getDisplay().createStack(1, stack.getActivityType(), toTop);
            }
            return stack;
        } else {
            throw new IllegalArgumentException("Device doesn't support freeform, can not reparent task=" + task);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean moveTopStackActivityToPinnedStackLocked(int stackId, Rect destBounds) {
        if (!HwPCUtils.enabledInPad() || !HwPCUtils.isPcCastModeInServer()) {
            ActivityStack stack = getStack(stackId);
            if (stack != null) {
                ActivityRecord r = stack.topRunningActivityLocked();
                if (r == null) {
                    Slog.w(ActivityManagerService.TAG, "moveTopStackActivityToPinnedStackLocked: No top running activity in stack=" + stack);
                    return false;
                } else if (this.mService.mForceResizableActivities || r.supportsPictureInPicture()) {
                    moveActivityToPinnedStackLocked(r, null, 0.0f, "moveTopActivityToPinnedStack");
                    return true;
                } else {
                    Slog.w(ActivityManagerService.TAG, "moveTopStackActivityToPinnedStackLocked: Picture-In-Picture not supported for  r=" + r);
                    return false;
                }
            } else {
                throw new IllegalArgumentException("moveTopStackActivityToPinnedStackLocked: Unknown stackId=" + stackId);
            }
        } else {
            HwPCUtils.log(ActivityManagerService.TAG, "ignore moveTopStackActivityToPinnedStackLocked in pad pc mode");
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public void moveActivityToPinnedStackLocked(ActivityRecord r, Rect sourceHintBounds, float aspectRatio, String reason) {
        PinnedActivityStack stack;
        Rect destBounds;
        PinnedActivityStack stack2;
        Rect destBounds2;
        ActivityRecord activityRecord = r;
        this.mWindowManager.deferSurfaceLayout();
        try {
            ActivityDisplay display = r.getStack().getDisplay();
            try {
                PinnedActivityStack stack3 = display.getPinnedStack();
                if (stack3 != null) {
                    moveTasksToFullscreenStackLocked(stack3, false);
                }
                stack = (PinnedActivityStack) display.getOrCreateStack(2, r.getActivityType(), true);
                try {
                    destBounds = stack.getDefaultPictureInPictureBounds(aspectRatio);
                } catch (Throwable th) {
                    th = th;
                    Rect rect = sourceHintBounds;
                    PinnedActivityStack pinnedActivityStack = stack;
                    this.mWindowManager.continueSurfaceLayout();
                    throw th;
                }
            } catch (Throwable th2) {
                th = th2;
                Rect rect2 = sourceHintBounds;
                this.mWindowManager.continueSurfaceLayout();
                throw th;
            }
            try {
                TaskRecord task = r.getTask();
                TaskRecord task2 = task;
                resizeStackLocked(stack, task.getOverrideBounds(), null, null, false, true, false);
                if (task2.mActivities.size() == 1) {
                    destBounds2 = destBounds;
                    stack2 = stack;
                    try {
                        task2.reparent((ActivityStack) stack, true, 0, false, true, false, reason);
                    } catch (Throwable th3) {
                        th = th3;
                        Rect rect3 = sourceHintBounds;
                        this.mWindowManager.continueSurfaceLayout();
                        throw th;
                    }
                } else {
                    destBounds2 = destBounds;
                    stack2 = stack;
                    TaskRecord newTask = task2.getStack().createTaskRecord(getNextTaskIdForUserLocked(activityRecord.userId), activityRecord.info, activityRecord.intent, null, null, true);
                    activityRecord.reparent(newTask, HwBootFail.STAGE_BOOT_SUCCESS, "moveActivityToStack");
                    newTask.reparent((ActivityStack) stack2, true, 0, false, true, false, reason);
                }
                activityRecord.supportsEnterPipOnTaskSwitch = false;
                this.mWindowManager.continueSurfaceLayout();
                stack2.animateResizePinnedStack(sourceHintBounds, destBounds2, -1, true);
                ensureActivitiesVisibleLocked(null, 0, false);
                resumeFocusedStackTopActivityLocked();
                this.mService.mTaskChangeNotificationController.notifyActivityPinned(activityRecord);
                LogPower.push(NetworkManagementService.NetdResponseCode.DnsProxyQueryResult, activityRecord.packageName);
            } catch (Throwable th4) {
                th = th4;
                Rect rect4 = sourceHintBounds;
                Rect rect5 = destBounds;
                PinnedActivityStack pinnedActivityStack2 = stack;
                this.mWindowManager.continueSurfaceLayout();
                throw th;
            }
        } catch (Throwable th5) {
            th = th5;
            Rect rect6 = sourceHintBounds;
            this.mWindowManager.continueSurfaceLayout();
            throw th;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean moveFocusableActivityStackToFrontLocked(ActivityRecord r, String reason) {
        if (r == null || !r.isFocusable()) {
            if (ActivityManagerDebugConfig.DEBUG_FOCUS) {
                Slog.d(ActivityManagerService.TAG, "moveActivityStackToFront: unfocusable r=" + r);
            }
            return false;
        }
        TaskRecord task = r.getTask();
        ActivityStack stack = r.getStack();
        if (stack == null) {
            Slog.w(ActivityManagerService.TAG, "moveActivityStackToFront: invalid task or stack: r=" + r + " task=" + task);
            return false;
        } else if (stack == this.mFocusedStack && stack.topRunningActivityLocked() == r) {
            if (ActivityManagerDebugConfig.DEBUG_FOCUS) {
                Slog.d(ActivityManagerService.TAG, "moveActivityStackToFront: already on top, r=" + r);
            }
            return false;
        } else {
            if (ActivityManagerDebugConfig.DEBUG_FOCUS) {
                Slog.d(ActivityManagerService.TAG, "moveActivityStackToFront: r=" + r);
            }
            stack.moveToFront(reason, task);
            if (IS_DEBUG_VERSION) {
                ArrayMap<String, Object> params = new ArrayMap<>();
                params.put("checkType", "FocusWindowNullScene");
                params.put("looper", BackgroundThread.getHandler().getLooper());
                if (r != null) {
                    params.put("focusedActivityName", r.toString());
                }
                params.put("windowManager", this.mWindowManager);
                if (HwServiceFactory.getWinFreezeScreenMonitor() != null) {
                    HwServiceFactory.getWinFreezeScreenMonitor().checkFreezeScreen(params);
                }
            }
            return true;
        }
    }

    /* access modifiers changed from: package-private */
    public ActivityRecord findTaskLocked(ActivityRecord r, int displayId) {
        this.mTmpFindTaskResult.r = null;
        this.mTmpFindTaskResult.matchedByRootAffinity = false;
        ActivityRecord affinityMatch = null;
        if (ActivityManagerDebugConfig.DEBUG_TASKS) {
            Slog.d(ActivityManagerService.TAG, "Looking for task of " + r);
        }
        for (int displayNdx = this.mActivityDisplays.size() - 1; displayNdx >= 0; displayNdx--) {
            ActivityDisplay display = this.mActivityDisplays.valueAt(displayNdx);
            for (int stackNdx = display.getChildCount() - 1; stackNdx >= 0; stackNdx--) {
                ActivityStack stack = display.getChildAt(stackNdx);
                if (r.hasCompatibleActivityType(stack)) {
                    stack.findTaskLocked(r, this.mTmpFindTaskResult);
                    if (this.mTmpFindTaskResult.r == null) {
                        continue;
                    } else if (!this.mTmpFindTaskResult.matchedByRootAffinity) {
                        return this.mTmpFindTaskResult.r;
                    } else {
                        if (this.mTmpFindTaskResult.r.getDisplayId() == displayId) {
                            affinityMatch = this.mTmpFindTaskResult.r;
                        } else if (ActivityManagerDebugConfig.DEBUG_TASKS && this.mTmpFindTaskResult.matchedByRootAffinity) {
                            Slog.d(ActivityManagerService.TAG, "Skipping match on different display " + this.mTmpFindTaskResult.r.getDisplayId() + " " + displayId);
                        }
                    }
                } else if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                    Slog.d(ActivityManagerService.TAG, "Skipping stack: (mismatch activity/stack) " + stack);
                }
            }
        }
        if (ActivityManagerDebugConfig.DEBUG_TASKS != 0 && affinityMatch == null) {
            Slog.d(ActivityManagerService.TAG, "No task found");
        }
        return affinityMatch;
    }

    /* access modifiers changed from: package-private */
    public ActivityRecord findActivityLocked(Intent intent, ActivityInfo info, boolean compareIntentFilters) {
        for (int displayNdx = this.mActivityDisplays.size() - 1; displayNdx >= 0; displayNdx--) {
            ActivityDisplay display = this.mActivityDisplays.valueAt(displayNdx);
            for (int stackNdx = display.getChildCount() - 1; stackNdx >= 0; stackNdx--) {
                ActivityRecord ar = display.getChildAt(stackNdx).findActivityLocked(intent, info, compareIntentFilters);
                if (ar != null) {
                    return ar;
                }
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public boolean hasAwakeDisplay() {
        for (int displayNdx = this.mActivityDisplays.size() - 1; displayNdx >= 0; displayNdx--) {
            if (!this.mActivityDisplays.valueAt(displayNdx).shouldSleep()) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public void goingToSleepLocked() {
        scheduleSleepTimeout();
        if (!this.mGoingToSleep.isHeld()) {
            this.mGoingToSleep.acquire();
            if (this.mLaunchingActivity.isHeld()) {
                this.mLaunchingActivity.release();
                this.mService.mHandler.removeMessages(104);
            }
        }
        applySleepTokensLocked(false);
        checkReadyForSleepLocked(true);
    }

    /* access modifiers changed from: package-private */
    public void prepareForShutdownLocked() {
        for (int i = 0; i < this.mActivityDisplays.size(); i++) {
            createSleepTokenLocked("shutdown", this.mActivityDisplays.keyAt(i));
        }
    }

    /* access modifiers changed from: package-private */
    public boolean shutdownLocked(int timeout) {
        goingToSleepLocked();
        boolean timedout = false;
        long endTime = System.currentTimeMillis() + ((long) timeout);
        while (true) {
            if (putStacksToSleepLocked(true, true)) {
                break;
            }
            long timeRemaining = endTime - System.currentTimeMillis();
            if (timeRemaining <= 0) {
                Slog.w(ActivityManagerService.TAG, "Activity manager shutdown timed out");
                timedout = true;
                break;
            }
            try {
                this.mService.wait(timeRemaining);
            } catch (InterruptedException e) {
            }
        }
        checkReadyForSleepLocked(false);
        return timedout;
    }

    /* access modifiers changed from: package-private */
    public void comeOutOfSleepIfNeededLocked() {
        removeSleepTimeouts();
        if (this.mGoingToSleep.isHeld()) {
            this.mGoingToSleep.release();
        }
    }

    /* access modifiers changed from: package-private */
    public void applySleepTokensLocked(boolean applyToStacks) {
        for (int displayNdx = this.mActivityDisplays.size() - 1; displayNdx >= 0; displayNdx--) {
            ActivityDisplay display = this.mActivityDisplays.valueAt(displayNdx);
            boolean displayShouldSleep = display.shouldSleep();
            if (displayShouldSleep != display.isSleeping()) {
                display.setIsSleeping(displayShouldSleep);
                if (applyToStacks) {
                    for (int stackNdx = display.getChildCount() - 1; stackNdx >= 0; stackNdx--) {
                        try {
                            ActivityStack stack = display.getChildAt(stackNdx);
                            if (displayShouldSleep) {
                                stack.goToSleepIfPossible(false);
                            } else {
                                stack.awakeFromSleepingLocked();
                                if (isFocusedStack(stack) && !getKeyguardController().isKeyguardOrAodShowing(display.mDisplayId)) {
                                    this.mActivityLaunchTrack = "outofsleep";
                                    this.inResumeTopActivity = false;
                                    resumeFocusedStackTopActivityLocked();
                                } else if (stack.inMultiWindowMode() && (stack.getWindowingMode() == 3 || stack.getWindowingMode() == 4)) {
                                    resumeAppLockActivityIfNeeded(stack, null);
                                }
                            }
                        } catch (IndexOutOfBoundsException e) {
                            Slog.e(ActivityManagerService.TAG, "display getChild error, index:" + stackNdx);
                        }
                    }
                    if (!displayShouldSleep && !this.mGoingToSleepActivities.isEmpty()) {
                        Iterator<ActivityRecord> it = this.mGoingToSleepActivities.iterator();
                        while (it.hasNext()) {
                            if (it.next().getDisplayId() == display.mDisplayId) {
                                it.remove();
                            }
                        }
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void activitySleptLocked(ActivityRecord r) {
        this.mGoingToSleepActivities.remove(r);
        ActivityStack s = r.getStack();
        if (s != null) {
            s.checkReadyForSleep();
        } else {
            checkReadyForSleepLocked(true);
        }
    }

    /* access modifiers changed from: package-private */
    public void checkReadyForSleepLocked(boolean allowDelay) {
        if (this.mService.isSleepingOrShuttingDownLocked() && putStacksToSleepLocked(allowDelay, false)) {
            sendPowerHintForLaunchEndIfNeeded();
            removeSleepTimeouts();
            if (this.mGoingToSleep.isHeld()) {
                this.mGoingToSleep.release();
            }
            if (this.mService.mShuttingDown) {
                this.mService.notifyAll();
            }
        }
    }

    private boolean putStacksToSleepLocked(boolean allowDelay, boolean shuttingDown) {
        boolean allSleep = true;
        for (int displayNdx = this.mActivityDisplays.size() - 1; displayNdx >= 0; displayNdx--) {
            ActivityDisplay display = this.mActivityDisplays.valueAt(displayNdx);
            for (int stackNdx = display.getChildCount() - 1; stackNdx >= 0; stackNdx--) {
                try {
                    ActivityStack stack = display.getChildAt(stackNdx);
                    if (allowDelay) {
                        allSleep &= stack.goToSleepIfPossible(shuttingDown);
                    } else {
                        stack.goToSleep();
                    }
                } catch (IndexOutOfBoundsException e) {
                    Slog.e(ActivityManagerService.TAG, "putStacksToSleepLocked display getChild error, index:" + stackNdx);
                }
            }
        }
        return allSleep;
    }

    /* access modifiers changed from: package-private */
    public boolean reportResumedActivityLocked(ActivityRecord r) {
        this.mStoppingActivities.remove(r);
        if (isFocusedStack(r.getStack())) {
            this.mService.updateUsageStats(r, true);
        }
        if (!allResumedActivitiesComplete()) {
            return false;
        }
        ensureActivitiesVisibleLocked(null, 0, false);
        this.mWindowManager.executeAppTransition();
        return true;
    }

    /* access modifiers changed from: package-private */
    public void handleAppCrashLocked(ProcessRecord app) {
        for (int displayNdx = this.mActivityDisplays.size() - 1; displayNdx >= 0; displayNdx--) {
            ActivityDisplay display = this.mActivityDisplays.valueAt(displayNdx);
            for (int stackNdx = display.getChildCount() - 1; stackNdx >= 0; stackNdx--) {
                display.getChildAt(stackNdx).handleAppCrashLocked(app);
            }
        }
    }

    /* access modifiers changed from: private */
    public void handleLaunchTaskBehindCompleteLocked(ActivityRecord r) {
        TaskRecord task = r.getTask();
        ActivityStack stack = task.getStack();
        r.mLaunchTaskBehind = false;
        this.mRecentTasks.add(task);
        this.mService.mTaskChangeNotificationController.notifyTaskStackChanged();
        r.setVisibility(false);
        ActivityRecord top = stack.getTopActivity();
        if (top != null) {
            top.getTask().touchActiveTime();
        }
    }

    /* access modifiers changed from: package-private */
    public void scheduleLaunchTaskBehindComplete(IBinder token) {
        this.mHandler.obtainMessage(112, token).sendToTarget();
    }

    /* access modifiers changed from: package-private */
    public void ensureActivitiesVisibleLocked(ActivityRecord starting, int configChanges, boolean preserveWindows) {
        ensureActivitiesVisibleLocked(starting, configChanges, preserveWindows, true);
    }

    /* access modifiers changed from: package-private */
    public void ensureActivitiesVisibleLocked(ActivityRecord starting, int configChanges, boolean preserveWindows, boolean notifyClients) {
        getKeyguardController().beginActivityVisibilityUpdate();
        try {
            for (int displayNdx = this.mActivityDisplays.size() - 1; displayNdx >= 0; displayNdx--) {
                ActivityDisplay display = this.mActivityDisplays.valueAt(displayNdx);
                for (int stackNdx = display.getChildCount() - 1; stackNdx >= 0; stackNdx--) {
                    display.getChildAt(stackNdx).ensureActivitiesVisibleLocked(starting, configChanges, preserveWindows, notifyClients);
                }
            }
        } catch (IndexOutOfBoundsException e) {
            Slog.e(ActivityManagerService.TAG, "ensureActivitiesVisibleLocked has Exception : IndexOutOfBoundsException");
        } catch (Throwable th) {
            getKeyguardController().endActivityVisibilityUpdate();
            throw th;
        }
        getKeyguardController().endActivityVisibilityUpdate();
    }

    /* access modifiers changed from: package-private */
    public void addStartingWindowsForVisibleActivities(boolean taskSwitch) {
        for (int displayNdx = this.mActivityDisplays.size() - 1; displayNdx >= 0; displayNdx--) {
            ActivityDisplay display = this.mActivityDisplays.valueAt(displayNdx);
            for (int stackNdx = display.getChildCount() - 1; stackNdx >= 0; stackNdx--) {
                ActivityStack stack = display.getChildAt(stackNdx);
                if (!stack.inFreeformWindowingMode()) {
                    stack.addStartingWindowsForVisibleActivities(taskSwitch);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void invalidateTaskLayers() {
        this.mTaskLayersChanged = true;
    }

    /* access modifiers changed from: package-private */
    public void rankTaskLayersIfNeeded() {
        if (this.mTaskLayersChanged) {
            this.mTaskLayersChanged = false;
            for (int displayNdx = 0; displayNdx < this.mActivityDisplays.size(); displayNdx++) {
                ActivityDisplay display = this.mActivityDisplays.valueAt(displayNdx);
                int baseLayer = 0;
                for (int stackNdx = display.getChildCount() - 1; stackNdx >= 0; stackNdx--) {
                    baseLayer += display.getChildAt(stackNdx).rankTaskLayers(baseLayer);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void clearOtherAppTimeTrackers(AppTimeTracker except) {
        for (int displayNdx = this.mActivityDisplays.size() - 1; displayNdx >= 0; displayNdx--) {
            ActivityDisplay display = this.mActivityDisplays.valueAt(displayNdx);
            for (int stackNdx = display.getChildCount() - 1; stackNdx >= 0; stackNdx--) {
                display.getChildAt(stackNdx).clearOtherAppTimeTrackers(except);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void scheduleDestroyAllActivities(ProcessRecord app, String reason) {
        for (int displayNdx = this.mActivityDisplays.size() - 1; displayNdx >= 0; displayNdx--) {
            ActivityDisplay display = this.mActivityDisplays.valueAt(displayNdx);
            for (int stackNdx = display.getChildCount() - 1; stackNdx >= 0; stackNdx--) {
                display.getChildAt(stackNdx).scheduleDestroyActivities(app, reason);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void releaseSomeActivitiesLocked(ProcessRecord app, String reason) {
        ArraySet<TaskRecord> tasks = null;
        if (ActivityManagerDebugConfig.DEBUG_RELEASE) {
            Slog.d(ActivityManagerService.TAG, "Trying to release some activities in " + app);
        }
        TaskRecord firstTask = null;
        for (int i = 0; i < app.activities.size(); i++) {
            ActivityRecord r = app.activities.get(i);
            if (r.finishing || r.isState(ActivityStack.ActivityState.DESTROYING, ActivityStack.ActivityState.DESTROYED)) {
                if (ActivityManagerDebugConfig.DEBUG_RELEASE) {
                    Slog.d(ActivityManagerService.TAG, "Abort release; already destroying: " + r);
                }
                return;
            }
            if (!r.visible && r.stopped && r.haveState && !r.isState(ActivityStack.ActivityState.RESUMED, ActivityStack.ActivityState.PAUSING, ActivityStack.ActivityState.PAUSED, ActivityStack.ActivityState.STOPPING)) {
                TaskRecord task = r.getTask();
                if (task != null) {
                    if (ActivityManagerDebugConfig.DEBUG_RELEASE) {
                        Slog.d(ActivityManagerService.TAG, "Collecting release task " + task + " from " + r);
                    }
                    if (firstTask == null) {
                        firstTask = task;
                    } else if (firstTask != task) {
                        if (tasks == null) {
                            tasks = new ArraySet<>();
                            tasks.add(firstTask);
                        }
                        tasks.add(task);
                    }
                }
            } else if (ActivityManagerDebugConfig.DEBUG_RELEASE) {
                Slog.d(ActivityManagerService.TAG, "Not releasing in-use activity: " + r);
            }
        }
        if (tasks == null) {
            if (ActivityManagerDebugConfig.DEBUG_RELEASE) {
                Slog.d(ActivityManagerService.TAG, "Didn't find two or more tasks to release");
            }
            return;
        }
        int numDisplays = this.mActivityDisplays.size();
        for (int displayNdx = 0; displayNdx < numDisplays; displayNdx++) {
            ActivityDisplay display = this.mActivityDisplays.valueAt(displayNdx);
            int stackCount = display.getChildCount();
            int stackNdx = 0;
            while (stackNdx < stackCount) {
                if (display.getChildAt(stackNdx).releaseSomeActivitiesLocked(app, tasks, reason) <= 0) {
                    stackNdx++;
                } else {
                    return;
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean switchUserLocked(int userId, UserState uss) {
        int focusStackId = this.mFocusedStack.getStackId();
        ActivityStack dockedStack = getDefaultDisplay().getSplitScreenPrimaryStack();
        if (dockedStack != null) {
            moveTasksToFullscreenStackLocked(dockedStack, this.mFocusedStack == dockedStack);
        }
        removeStacksInWindowingModes(2);
        this.mUserStackInFront.put(this.mCurrentUser, focusStackId);
        int restoreStackId = this.mUserStackInFront.get(userId, this.mHomeStack.mStackId);
        this.mCurrentUser = userId;
        this.mStartingUsers.add(uss);
        for (int displayNdx = this.mActivityDisplays.size() - 1; displayNdx >= 0; displayNdx--) {
            ActivityDisplay display = this.mActivityDisplays.valueAt(displayNdx);
            for (int stackNdx = display.getChildCount() - 1; stackNdx >= 0; stackNdx--) {
                ActivityStack stack = display.getChildAt(stackNdx);
                stack.switchUserLocked(userId);
                TaskRecord task = stack.topTask();
                if (task != null) {
                    stack.positionChildWindowContainerAtTop(task);
                }
            }
        }
        ActivityStack stack2 = getStack(restoreStackId);
        if (stack2 == null) {
            stack2 = this.mHomeStack;
        }
        boolean homeInFront = stack2.isActivityTypeHome();
        if (stack2.isOnHomeDisplay()) {
            stack2.moveToFront("switchUserOnHomeDisplay");
        } else {
            resumeHomeStackTask(null, "switchUserOnOtherDisplay");
        }
        return homeInFront;
    }

    /* access modifiers changed from: package-private */
    public boolean isCurrentProfileLocked(int userId) {
        if (userId == this.mCurrentUser) {
            return true;
        }
        return this.mService.mUserController.isCurrentProfile(userId);
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
        boolean nowVisible = allResumedActivitiesVisible();
        for (int activityNdx = this.mStoppingActivities.size() - 1; activityNdx >= 0; activityNdx--) {
            ActivityRecord s = this.mStoppingActivities.get(activityNdx);
            boolean waitingVisible = this.mActivitiesWaitingForVisibleActivity.contains(s);
            if (ActivityManagerDebugConfig.DEBUG_STATES) {
                Slog.v(ActivityManagerService.TAG, "Stopping " + s + ": nowVisible=" + nowVisible + " waitingVisible=" + waitingVisible + " finishing=" + s.finishing);
            }
            if (waitingVisible && nowVisible) {
                this.mActivitiesWaitingForVisibleActivity.remove(s);
                waitingVisible = false;
                if (s.finishing) {
                    if (ActivityManagerDebugConfig.DEBUG_STATES) {
                        Slog.v(ActivityManagerService.TAG, "Before stopping, can hide: " + s);
                    }
                    s.setVisibility(false);
                }
            }
            if (remove) {
                ActivityStack stack = s.getStack();
                if (stack != null) {
                    shouldSleepOrShutDown = stack.shouldSleepOrShutDownActivities();
                } else {
                    shouldSleepOrShutDown = this.mService.isSleepingOrShuttingDownLocked();
                }
                if (!waitingVisible || shouldSleepOrShutDown) {
                    if (processPausingActivities || !s.isState(ActivityStack.ActivityState.PAUSING)) {
                        if (ActivityManagerDebugConfig.DEBUG_STATES) {
                            Slog.v(ActivityManagerService.TAG, "Ready to stop: " + s);
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

    /* access modifiers changed from: package-private */
    public void validateTopActivitiesLocked() {
        for (int displayNdx = this.mActivityDisplays.size() - 1; displayNdx >= 0; displayNdx--) {
            ActivityDisplay display = this.mActivityDisplays.valueAt(displayNdx);
            for (int stackNdx = display.getChildCount() - 1; stackNdx >= 0; stackNdx--) {
                ActivityStack stack = display.getChildAt(stackNdx);
                ActivityRecord r = stack.topRunningActivityLocked();
                ActivityStack.ActivityState state = r == null ? ActivityStack.ActivityState.DESTROYED : r.getState();
                if (!isFocusedStack(stack)) {
                    ActivityRecord resumed = stack.getResumedActivity();
                    if (resumed != null && resumed == r) {
                        Slog.e(ActivityManagerService.TAG, "validateTop...: back stack has resumed activity r=" + r + " state=" + state);
                    }
                    if (r != null && (state == ActivityStack.ActivityState.INITIALIZING || state == ActivityStack.ActivityState.RESUMED)) {
                        Slog.e(ActivityManagerService.TAG, "validateTop...: activity in back resumed r=" + r + " state=" + state);
                    }
                } else if (r == null) {
                    Slog.e(ActivityManagerService.TAG, "validateTop...: null top activity, stack=" + stack);
                } else {
                    ActivityRecord pausing = stack.mPausingActivity;
                    if (pausing != null && pausing == r) {
                        Slog.e(ActivityManagerService.TAG, "validateTop...: top stack has pausing activity r=" + r + " state=" + state);
                    }
                    if (!(state == ActivityStack.ActivityState.INITIALIZING || state == ActivityStack.ActivityState.RESUMED)) {
                        Slog.e(ActivityManagerService.TAG, "validateTop...: activity in front not resumed r=" + r + " state=" + state);
                    }
                }
            }
        }
    }

    public void dumpDisplays(PrintWriter pw) {
        ActivityDisplay display;
        for (int i = this.mActivityDisplays.size() - 1; i >= 0; i += -1) {
            pw.print("[id:" + this.mActivityDisplays.valueAt(i).mDisplayId + " stacks:");
            display.dumpStacks(pw);
            pw.print("]");
        }
    }

    public void dump(PrintWriter pw, String prefix) {
        pw.print(prefix);
        pw.print("mFocusedStack=" + this.mFocusedStack);
        pw.print(" mLastFocusedStack=");
        pw.println(this.mLastFocusedStack);
        pw.print(prefix);
        pw.println("mCurTaskIdForUser=" + this.mCurTaskIdForUser);
        pw.print(prefix);
        pw.println("mUserStackInFront=" + this.mUserStackInFront);
        for (int i = this.mActivityDisplays.size() + -1; i >= 0; i--) {
            this.mActivityDisplays.valueAt(i).dump(pw, prefix);
        }
        if (!this.mWaitingForActivityVisible.isEmpty()) {
            pw.print(prefix);
            pw.println("mWaitingForActivityVisible=");
            for (int i2 = 0; i2 < this.mWaitingForActivityVisible.size(); i2++) {
                pw.print(prefix);
                pw.print(prefix);
                this.mWaitingForActivityVisible.get(i2).dump(pw, prefix);
            }
        }
        pw.print(prefix);
        pw.print("isHomeRecentsComponent=");
        if (!IS_DOCOMO) {
            pw.print(this.mRecentTasks.isRecentsComponentHomeActivity(this.mCurrentUser));
        }
        getKeyguardController().dump(pw, prefix);
        this.mService.getLockTaskController().dump(pw, prefix);
    }

    public void writeToProto(ProtoOutputStream proto, long fieldId) {
        long token = proto.start(fieldId);
        super.writeToProto(proto, 1146756268033L, false);
        for (int displayNdx = 0; displayNdx < this.mActivityDisplays.size(); displayNdx++) {
            this.mActivityDisplays.valueAt(displayNdx).writeToProto(proto, 2246267895810L);
        }
        getKeyguardController().writeToProto(proto, 1146756268035L);
        if (this.mFocusedStack != null) {
            proto.write(1120986464260L, this.mFocusedStack.mStackId);
            ActivityRecord focusedActivity = getResumedActivityLocked();
            if (focusedActivity != null) {
                focusedActivity.writeIdentifierToProto(proto, 1146756268037L);
            }
        } else {
            proto.write(1120986464260L, -1);
        }
        proto.write(1133871366150L, this.mRecentTasks.isRecentsComponentHomeActivity(this.mCurrentUser));
        proto.end(token);
    }

    /* access modifiers changed from: package-private */
    public void dumpDisplayConfigs(PrintWriter pw, String prefix) {
        pw.print(prefix);
        pw.println("Display override configurations:");
        int displayCount = this.mActivityDisplays.size();
        for (int i = 0; i < displayCount; i++) {
            ActivityDisplay activityDisplay = this.mActivityDisplays.valueAt(i);
            pw.print(prefix);
            pw.print("  ");
            pw.print(activityDisplay.mDisplayId);
            pw.print(": ");
            pw.println(activityDisplay.getOverrideConfiguration());
        }
    }

    /* access modifiers changed from: package-private */
    public ArrayList<ActivityRecord> getDumpActivitiesLocked(String name, boolean dumpVisibleStacksOnly, boolean dumpFocusedStackOnly) {
        if (dumpFocusedStackOnly) {
            return this.mFocusedStack.getDumpActivitiesLocked(name);
        }
        ArrayList<ActivityRecord> activities = new ArrayList<>();
        int numDisplays = this.mActivityDisplays.size();
        for (int displayNdx = 0; displayNdx < numDisplays; displayNdx++) {
            ActivityDisplay display = this.mActivityDisplays.valueAt(displayNdx);
            for (int stackNdx = display.getChildCount() - 1; stackNdx >= 0; stackNdx--) {
                ActivityStack stack = display.getChildAt(stackNdx);
                if (!dumpVisibleStacksOnly || stack.shouldBeVisible(null)) {
                    activities.addAll(stack.getDumpActivitiesLocked(name));
                }
            }
        }
        return activities;
    }

    static boolean printThisActivity(PrintWriter pw, ActivityRecord activity, String dumpPackage, boolean needSep, String prefix) {
        if (activity == null || (dumpPackage != null && !dumpPackage.equals(activity.packageName))) {
            return false;
        }
        if (needSep) {
            pw.println();
        }
        pw.print(prefix);
        pw.println(activity);
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean dumpActivitiesLocked(FileDescriptor fd, PrintWriter pw, boolean dumpAll, boolean dumpClient, String dumpPackage) {
        PrintWriter printWriter = pw;
        String str = dumpPackage;
        int displayNdx = 0;
        boolean printed = false;
        boolean needSep = false;
        while (true) {
            int displayNdx2 = displayNdx;
            if (displayNdx2 < this.mActivityDisplays.size()) {
                ActivityDisplay activityDisplay = this.mActivityDisplays.valueAt(displayNdx2);
                printWriter.print("Display #");
                printWriter.print(activityDisplay.mDisplayId);
                printWriter.println(" (activities from top to bottom):");
                ActivityDisplay display = this.mActivityDisplays.valueAt(displayNdx2);
                int stackNdx = display.getChildCount() - 1;
                while (true) {
                    int stackNdx2 = stackNdx;
                    if (stackNdx2 < 0) {
                        break;
                    }
                    ActivityStack stack = display.getChildAt(stackNdx2);
                    pw.println();
                    printWriter.println("  Stack #" + stack.mStackId + ": type=" + WindowConfiguration.activityTypeToString(stack.getActivityType()) + " mode=" + WindowConfiguration.windowingModeToString(stack.getWindowingMode()));
                    StringBuilder sb = new StringBuilder();
                    sb.append("  isSleeping=");
                    sb.append(stack.shouldSleepActivities());
                    printWriter.println(sb.toString());
                    printWriter.println("  mBounds=" + stack.getOverrideBounds());
                    int stackNdx3 = stackNdx2;
                    ActivityDisplay display2 = display;
                    ActivityDisplay activityDisplay2 = activityDisplay;
                    int displayNdx3 = displayNdx2;
                    boolean printed2 = dumpHistoryList(fd, printWriter, stack.mLRUActivities, "    ", "Run", false, !dumpAll, false, str, true, "    Running activities (most recent first):", null) | printed | stack.dumpActivitiesLocked(fd, printWriter, dumpAll, dumpClient, str, needSep);
                    boolean needSep2 = printed2;
                    ActivityStack stack2 = stack;
                    if (printThisActivity(printWriter, stack2.mPausingActivity, str, needSep2, "    mPausingActivity: ")) {
                        printed2 = true;
                        needSep2 = false;
                    }
                    if (printThisActivity(printWriter, stack2.getResumedActivity(), str, needSep2, "    mResumedActivity: ")) {
                        printed2 = true;
                        needSep2 = false;
                    }
                    if (dumpAll) {
                        if (printThisActivity(printWriter, stack2.mLastPausedActivity, str, needSep2, "    mLastPausedActivity: ")) {
                            printed2 = true;
                            needSep2 = true;
                        }
                        printed2 |= printThisActivity(printWriter, stack2.mLastNoHistoryActivity, str, needSep2, "    mLastNoHistoryActivity: ");
                    }
                    printed = printed2;
                    needSep = printed;
                    stackNdx = stackNdx3 - 1;
                    activityDisplay = activityDisplay2;
                    displayNdx2 = displayNdx3;
                    display = display2;
                }
                displayNdx = displayNdx2 + 1;
            } else {
                return dumpHistoryList(fd, printWriter, this.mGoingToSleepActivities, "  ", "Sleep", false, !dumpAll, false, str, true, "  Activities waiting to sleep:", null) | printed | dumpHistoryList(fd, printWriter, this.mFinishingActivities, "  ", "Fin", false, !dumpAll, false, str, true, "  Activities waiting to finish:", null) | dumpHistoryList(fd, printWriter, this.mStoppingActivities, "  ", "Stop", false, !dumpAll, false, str, true, "  Activities waiting to stop:", null) | dumpHistoryList(fd, printWriter, this.mActivitiesWaitingForVisibleActivity, "  ", "Wait", false, !dumpAll, false, str, true, "  Activities waiting for another to become visible:", null);
            }
        }
    }

    static boolean dumpHistoryList(FileDescriptor fd, PrintWriter pw, List<ActivityRecord> list, String prefix, String label, boolean complete, boolean brief, boolean client, String dumpPackage, boolean needNL, String header, TaskRecord lastTask) {
        TaskRecord lastTask2;
        TransferPipe tp;
        PrintWriter printWriter = pw;
        String str = prefix;
        String str2 = dumpPackage;
        boolean printed = false;
        int i = list.size() - 1;
        boolean needNL2 = needNL;
        String innerPrefix = null;
        String[] args = null;
        String header2 = header;
        TaskRecord lastTask3 = lastTask;
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
                    full = true;
                }
                if (needNL2) {
                    printWriter.println(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
                    needNL2 = false;
                }
                if (header2 != null) {
                    printWriter.println(header2);
                    header2 = null;
                }
                String header3 = header2;
                if (lastTask3 != r.getTask()) {
                    lastTask3 = r.getTask();
                    printWriter.print(str);
                    printWriter.print(full ? "* " : "  ");
                    printWriter.println(lastTask3);
                    if (full) {
                        lastTask3.dump(printWriter, str + "  ");
                    } else if (complete && lastTask3.intent != null) {
                        printWriter.print(str);
                        printWriter.print("  ");
                        printWriter.println(lastTask3.intent.toInsecureStringWithClip());
                    }
                }
                printWriter.print(str);
                printWriter.print(full ? "  * " : "    ");
                printWriter.print(label);
                printWriter.print(" #");
                printWriter.print(i);
                printWriter.print(": ");
                printWriter.println(r);
                if (full) {
                    r.dump(printWriter, innerPrefix);
                } else if (complete) {
                    printWriter.print(innerPrefix);
                    printWriter.println(r.intent.toInsecureString());
                    if (r.app != null) {
                        printWriter.print(innerPrefix);
                        printWriter.println(r.app);
                    }
                }
                if (!client || r.app == null || r.app.thread == null) {
                    lastTask2 = lastTask3;
                    FileDescriptor fileDescriptor = fd;
                } else {
                    pw.flush();
                    try {
                        TransferPipe tp2 = new TransferPipe();
                        try {
                            tp = tp2;
                            try {
                                r.app.thread.dumpActivity(tp.getWriteFd(), r.appToken, innerPrefix, args);
                                lastTask2 = lastTask3;
                            } catch (Throwable th) {
                                th = th;
                                TaskRecord taskRecord = lastTask3;
                                FileDescriptor fileDescriptor2 = fd;
                                tp.kill();
                                throw th;
                            }
                            try {
                                tp.go(fd, 2000);
                                try {
                                    tp.kill();
                                } catch (IOException e) {
                                    e = e;
                                    printWriter.println(innerPrefix + "Failure while dumping the activity: " + e);
                                    needNL2 = true;
                                    header2 = header3;
                                    i--;
                                    lastTask3 = lastTask2;
                                    str = prefix;
                                    str2 = dumpPackage;
                                } catch (RemoteException e2) {
                                    printWriter.println(innerPrefix + "Got a RemoteException while dumping the activity");
                                    needNL2 = true;
                                    header2 = header3;
                                    i--;
                                    lastTask3 = lastTask2;
                                    str = prefix;
                                    str2 = dumpPackage;
                                }
                                needNL2 = true;
                            } catch (Throwable th2) {
                                th = th2;
                                tp.kill();
                                throw th;
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            TaskRecord taskRecord2 = lastTask3;
                            tp = tp2;
                            FileDescriptor fileDescriptor3 = fd;
                            tp.kill();
                            throw th;
                        }
                    } catch (IOException e3) {
                        e = e3;
                        lastTask2 = lastTask3;
                        FileDescriptor fileDescriptor4 = fd;
                        printWriter.println(innerPrefix + "Failure while dumping the activity: " + e);
                        needNL2 = true;
                        header2 = header3;
                        i--;
                        lastTask3 = lastTask2;
                        str = prefix;
                        str2 = dumpPackage;
                    } catch (RemoteException e4) {
                        lastTask2 = lastTask3;
                        FileDescriptor fileDescriptor5 = fd;
                        printWriter.println(innerPrefix + "Got a RemoteException while dumping the activity");
                        needNL2 = true;
                        header2 = header3;
                        i--;
                        lastTask3 = lastTask2;
                        str = prefix;
                        str2 = dumpPackage;
                    }
                }
                header2 = header3;
            } else {
                lastTask2 = lastTask3;
                FileDescriptor fileDescriptor6 = fd;
            }
            i--;
            lastTask3 = lastTask2;
            str = prefix;
            str2 = dumpPackage;
        }
        List<ActivityRecord> list2 = list;
        TaskRecord taskRecord3 = lastTask3;
        FileDescriptor fileDescriptor7 = fd;
        return printed;
    }

    /* access modifiers changed from: package-private */
    public void scheduleIdleTimeoutLocked(ActivityRecord next) {
        if (ActivityManagerDebugConfig.DEBUG_IDLE) {
            Slog.d(ActivityManagerService.TAG, "scheduleIdleTimeoutLocked: Callers=" + Debug.getCallers(4));
        }
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(100, next), JobStatus.DEFAULT_TRIGGER_UPDATE_DELAY);
    }

    /* access modifiers changed from: package-private */
    public final void scheduleIdleLocked() {
        this.mHandler.sendEmptyMessage(101);
    }

    /* access modifiers changed from: package-private */
    public void removeTimeoutsForActivityLocked(ActivityRecord r) {
        if (ActivityManagerDebugConfig.DEBUG_IDLE) {
            Slog.d(ActivityManagerService.TAG, "removeTimeoutsForActivity: Callers=" + Debug.getCallers(4));
        }
        this.mHandler.removeMessages(100, r);
    }

    /* access modifiers changed from: package-private */
    public final void scheduleResumeTopActivities() {
        if (!this.mHandler.hasMessages(102)) {
            this.mHandler.sendEmptyMessage(102);
        }
    }

    /* access modifiers changed from: package-private */
    public void removeSleepTimeouts() {
        this.mHandler.removeMessages(103);
    }

    /* access modifiers changed from: package-private */
    public final void scheduleSleepTimeout() {
        removeSleepTimeouts();
        this.mHandler.sendEmptyMessageDelayed(103, 5000);
    }

    public void onDisplayAdded(int displayId) {
        Slog.v(ActivityManagerService.TAG, "Display added displayId=" + displayId);
        this.mHandler.sendMessage(this.mHandler.obtainMessage(105, displayId, 0));
    }

    public void onDisplayRemoved(int displayId) {
        Slog.v(ActivityManagerService.TAG, "Display removed displayId=" + displayId);
        this.mHandler.sendMessage(this.mHandler.obtainMessage(107, displayId, 0));
    }

    public void onDisplayChanged(int displayId) {
        if (ActivityManagerDebugConfig.DEBUG_STACK) {
            Slog.v(ActivityManagerService.TAG, "Display changed displayId=" + displayId);
        }
        this.mHandler.sendMessage(this.mHandler.obtainMessage(106, displayId, 0));
    }

    /* access modifiers changed from: private */
    public void handleDisplayAdded(int displayId) {
        synchronized (this.mService) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                getActivityDisplayOrCreateLocked(displayId);
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
    public boolean isDisplayAdded(int displayId) {
        return getActivityDisplayOrCreateLocked(displayId) != null;
    }

    /* access modifiers changed from: package-private */
    public ActivityDisplay getActivityDisplay(int displayId) {
        return this.mActivityDisplays.get(displayId);
    }

    /* access modifiers changed from: package-private */
    public ActivityDisplay getDefaultDisplay() {
        return this.mActivityDisplays.get(0);
    }

    /* access modifiers changed from: package-private */
    public ActivityDisplay getActivityDisplayOrCreateLocked(int displayId) {
        ActivityDisplay activityDisplay = this.mActivityDisplays.get(displayId);
        if (activityDisplay != null) {
            return activityDisplay;
        }
        if (this.mDisplayManager == null) {
            return null;
        }
        Display display = this.mDisplayManager.getDisplay(displayId);
        if (display == null) {
            Slog.i(ActivityManagerService.TAG, "getActivityDisplayOrCreateLocked return null, displayId:" + displayId);
            return null;
        }
        ActivityDisplay activityDisplay2 = new ActivityDisplay(this, display);
        attachDisplay(activityDisplay2);
        calculateDefaultMinimalSizeOfResizeableTasks(activityDisplay2);
        this.mWindowManager.onDisplayAdded(displayId);
        return activityDisplay2;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void attachDisplay(ActivityDisplay display) {
        this.mActivityDisplays.put(display.mDisplayId, display);
    }

    public void reCalculateDefaultMinimalSizeOfResizeableTasks() {
        calculateDefaultMinimalSizeOfResizeableTasks(getActivityDisplayOrCreateLocked(0));
    }

    private void calculateDefaultMinimalSizeOfResizeableTasks(ActivityDisplay display) {
        this.mDefaultMinSizeOfResizeableTask = this.mService.mContext.getResources().getDimensionPixelSize(17105017);
    }

    /* access modifiers changed from: protected */
    public void handleDisplayRemoved(int displayId) {
        if (displayId != 0) {
            synchronized (this.mService) {
                try {
                    ActivityManagerService.boostPriorityForLockedSection();
                    ActivityDisplay activityDisplay = this.mActivityDisplays.get(displayId);
                    if (activityDisplay == null) {
                        ActivityManagerService.resetPriorityAfterLockedSection();
                        return;
                    }
                    activityDisplay.remove();
                    releaseSleepTokens(activityDisplay);
                    this.mActivityDisplays.remove(displayId);
                    ActivityManagerService.resetPriorityAfterLockedSection();
                } catch (Throwable th) {
                    while (true) {
                        ActivityManagerService.resetPriorityAfterLockedSection();
                        throw th;
                    }
                }
            }
        } else {
            throw new IllegalArgumentException("Can't remove the primary display.");
        }
    }

    /* access modifiers changed from: private */
    public void handleDisplayChanged(int displayId) {
        synchronized (this.mService) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                ActivityDisplay activityDisplay = this.mActivityDisplays.get(displayId);
                if (activityDisplay != null) {
                    if (displayId != 0) {
                        int displayState = activityDisplay.mDisplay.getState();
                        if (displayState == 1 && activityDisplay.mOffToken == null) {
                            activityDisplay.mOffToken = this.mService.acquireSleepToken("Display-off", displayId);
                        } else if (displayState == 2 && activityDisplay.mOffToken != null) {
                            activityDisplay.mOffToken.release();
                            activityDisplay.mOffToken = null;
                        }
                    }
                    activityDisplay.updateBounds();
                }
                this.mWindowManager.onDisplayChanged(displayId);
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
    public ActivityManagerInternal.SleepToken createSleepTokenLocked(String tag, int displayId) {
        ActivityDisplay display = this.mActivityDisplays.get(displayId);
        if (display != null) {
            SleepTokenImpl token = new SleepTokenImpl(tag, displayId);
            this.mSleepTokens.add(token);
            if (ActivityManagerDebugConfig.DEBUG_KEYGUARD) {
                Slog.v(TAG_KEYGUARD, "add sleepToken:" + token, new Exception());
            } else {
                Flog.i(107, "add sleepToken:" + token);
            }
            display.mAllSleepTokens.add(token);
            return token;
        }
        throw new IllegalArgumentException("Invalid display: " + displayId);
    }

    /* access modifiers changed from: private */
    public void removeSleepTokenLocked(SleepTokenImpl token) {
        this.mSleepTokens.remove(token);
        if (ActivityManagerDebugConfig.DEBUG_KEYGUARD) {
            Slog.v(TAG_KEYGUARD, "remove sleepToken:" + token, new Exception());
        }
        ActivityDisplay display = this.mActivityDisplays.get(token.mDisplayId);
        if (display != null) {
            display.mAllSleepTokens.remove(token);
            Flog.i(107, "remove sleepToken:" + token);
            if (!display.mAllSleepTokens.isEmpty()) {
                return;
            }
            if (!HwPCUtils.enabledInPad() || !HwPCUtils.isPcCastModeInServer() || this.mService.mHwAMSEx == null) {
                this.mService.updateSleepIfNeededLocked();
            } else if (this.mService.mHwAMSEx.canUpdateSleepForPCMode()) {
                this.mService.updateSleepIfNeededLocked();
            }
        }
    }

    private void releaseSleepTokens(ActivityDisplay display) {
        if (!display.mAllSleepTokens.isEmpty()) {
            Iterator<ActivityManagerInternal.SleepToken> it = display.mAllSleepTokens.iterator();
            while (it.hasNext()) {
                this.mSleepTokens.remove(it.next());
            }
            Flog.i(107, "clear sleepToken");
            display.mAllSleepTokens.clear();
            this.mService.updateSleepIfNeededLocked();
        }
    }

    private ActivityManager.StackInfo getStackInfo(ActivityStack stack) {
        String str;
        ActivityDisplay display = this.mActivityDisplays.get(stack.mDisplayId);
        ActivityManager.StackInfo info = new ActivityManager.StackInfo();
        stack.getWindowContainerBounds(info.bounds);
        if (HwPCUtils.isExtDynamicStack(stack.getStackId())) {
            info.displayId = stack.mDisplayId;
        } else {
            info.displayId = 0;
        }
        info.stackId = stack.mStackId;
        info.userId = stack.mCurrentUser;
        ComponentName componentName = null;
        info.visible = stack.shouldBeVisible(null);
        info.position = display != null ? display.getIndexOf(stack) : 0;
        info.configuration.setTo(stack.getConfiguration());
        ArrayList<TaskRecord> tasks = stack.getAllTasks();
        int numTasks = tasks.size();
        int[] taskIds = new int[numTasks];
        String[] taskNames = new String[numTasks];
        Rect[] taskBounds = new Rect[numTasks];
        int[] taskUserIds = new int[numTasks];
        for (int i = 0; i < numTasks; i++) {
            TaskRecord task = tasks.get(i);
            taskIds[i] = task.taskId;
            if (task.origActivity != null) {
                str = task.origActivity.flattenToString();
            } else if (task.realActivity != null) {
                str = task.realActivity.flattenToString();
            } else if (task.getTopActivity() != null) {
                str = task.getTopActivity().packageName;
            } else {
                str = UiModeManagerService.Shell.NIGHT_MODE_STR_UNKNOWN;
            }
            taskNames[i] = str;
            taskBounds[i] = new Rect();
            task.getWindowContainerBounds(taskBounds[i]);
            taskUserIds[i] = task.userId;
        }
        info.taskIds = taskIds;
        info.taskNames = taskNames;
        info.taskBounds = taskBounds;
        info.taskUserIds = taskUserIds;
        ActivityRecord top = stack.topRunningActivityLocked();
        if (top != null) {
            componentName = top.intent.getComponent();
        }
        info.topActivity = componentName;
        return info;
    }

    /* access modifiers changed from: package-private */
    public ActivityManager.StackInfo getStackInfo(int stackId) {
        ActivityStack stack = getStack(stackId);
        if (stack != null) {
            return getStackInfo(stack);
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public ActivityManager.StackInfo getStackInfo(int windowingMode, int activityType) {
        ActivityStack stack = getStack(windowingMode, activityType);
        if (stack != null) {
            return getStackInfo(stack);
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public ArrayList<ActivityManager.StackInfo> getAllStackInfosLocked() {
        ArrayList<ActivityManager.StackInfo> list = new ArrayList<>();
        for (int displayNdx = 0; displayNdx < this.mActivityDisplays.size(); displayNdx++) {
            ActivityDisplay display = this.mActivityDisplays.valueAt(displayNdx);
            for (int stackNdx = display.getChildCount() - 1; stackNdx >= 0; stackNdx--) {
                list.add(getStackInfo(display.getChildAt(stackNdx)));
            }
        }
        return list;
    }

    /* access modifiers changed from: package-private */
    public void handleNonResizableTaskIfNeeded(TaskRecord task, int preferredWindowingMode, int preferredDisplayId, ActivityStack actualStack) {
        handleNonResizableTaskIfNeeded(task, preferredWindowingMode, preferredDisplayId, actualStack, false);
    }

    /* access modifiers changed from: package-private */
    public void handleNonResizableTaskIfNeeded(TaskRecord task, int preferredWindowingMode, int preferredDisplayId, ActivityStack actualStack, boolean forceNonResizable) {
        boolean z = false;
        boolean isSecondaryDisplayPreferred = (preferredDisplayId == 0 || preferredDisplayId == -1) ? false : true;
        if (task != null) {
            if (this.mVrMananger.isVRDeviceConnected() || this.mVrMananger.isValidVRDisplayId(preferredDisplayId)) {
                Slog.i(ActivityManagerService.TAG, "handleNonResizableTaskIfNeeded preferredStackId = " + preferredDisplayId);
                return;
            }
            if ((actualStack != null && actualStack.getDisplay().hasSplitScreenPrimaryStack()) || preferredWindowingMode == 3 || isSecondaryDisplayPreferred) {
                int reason = 2;
                if (task.isActivityTypeStandardOrUndefined() || getConfiguration().extraConfig.getConfigItem(2) == 2) {
                    if (isSecondaryDisplayPreferred) {
                        int actualDisplayId = task.getStack().mDisplayId;
                        if (task.canBeLaunchedOnDisplay(actualDisplayId)) {
                            if (!HwPCUtils.isPcDynamicStack(task.getStack().mStackId)) {
                                this.mService.setTaskWindowingMode(task.taskId, 4, true);
                            }
                            if (preferredDisplayId != actualDisplayId) {
                                this.mService.mTaskChangeNotificationController.notifyActivityLaunchOnSecondaryDisplayFailed();
                                return;
                            }
                        } else {
                            throw new IllegalStateException("Task resolved to incompatible display");
                        }
                    }
                    ActivityRecord topActivity = task.getTopActivity();
                    boolean isInMultiWinBlackList = false;
                    if (!(this.mService.mCustAms == null || topActivity == null)) {
                        isInMultiWinBlackList = this.mService.mCustAms.isInMultiWinBlackList(topActivity.appInfo.packageName, this.mService.mContext.getContentResolver());
                    }
                    if (!task.supportsSplitScreenWindowingMode() || forceNonResizable || ((topActivity != null && "com.huawei.systemmanager".equals(topActivity.appInfo.packageName)) || isInMultiWinBlackList)) {
                        if (!HwPCUtils.isPcDynamicStack(topActivity.getStackId())) {
                            this.mService.mTaskChangeNotificationController.notifyActivityDismissingDockedStack();
                            uploadUnSupportSplitScreenAppPackageName(topActivity.appInfo.packageName);
                        }
                        ActivityStack dockedStack = task.getStack().getDisplay().getSplitScreenPrimaryStack();
                        if (dockedStack != null) {
                            if (actualStack == dockedStack) {
                                z = true;
                            }
                            moveTasksToFullscreenStackLocked(dockedStack, z);
                        }
                    } else if (topActivity != null && topActivity.isNonResizableOrForcedResizable() && !topActivity.noDisplay && !HwPCUtils.isExtDynamicStack(topActivity.getStackId())) {
                        String packageName = topActivity.appInfo.packageName;
                        if (!isSecondaryDisplayPreferred) {
                            reason = 1;
                        }
                        if ((1 & topActivity.appInfo.flags) == 0) {
                            this.mService.mTaskChangeNotificationController.notifyActivityForcedResizable(task.taskId, reason, packageName);
                        }
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void activityRelaunchedLocked(IBinder token) {
        this.mWindowManager.notifyAppRelaunchingFinished(token);
        ActivityRecord r = ActivityRecord.isInStackLocked(token);
        if (r != null && r.getStack().shouldSleepOrShutDownActivities()) {
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
            Slog.e(ActivityManagerService.TAG, "stack state exception!");
        }
    }

    /* access modifiers changed from: package-private */
    public void scheduleUpdateMultiWindowMode(TaskRecord task) {
        if (!task.getStack().deferScheduleMultiWindowModeChanged()) {
            for (int i = task.mActivities.size() - 1; i >= 0; i--) {
                ActivityRecord r = task.mActivities.get(i);
                if (!(r.app == null || r.app.thread == null)) {
                    Flog.i(101, "add r " + r + " into list of multiwindow activities");
                    this.mMultiWindowModeChangedActivities.add(r);
                }
            }
            if (!this.mHandler.hasMessages(114)) {
                this.mHandler.sendEmptyMessage(114);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void scheduleUpdatePictureInPictureModeIfNeeded(TaskRecord task, ActivityStack prevStack) {
        ActivityStack stack = task.getStack();
        if (prevStack != null && prevStack != stack && (prevStack.inPinnedWindowingMode() || stack.inPinnedWindowingMode())) {
            scheduleUpdatePictureInPictureModeIfNeeded(task, stack.getOverrideBounds());
        }
    }

    /* access modifiers changed from: package-private */
    public void scheduleUpdatePictureInPictureModeIfNeeded(TaskRecord task, Rect targetStackBounds) {
        for (int i = task.mActivities.size() - 1; i >= 0; i--) {
            ActivityRecord r = task.mActivities.get(i);
            if (!(r.app == null || r.app.thread == null)) {
                this.mPipModeChangedActivities.add(r);
                this.mMultiWindowModeChangedActivities.remove(r);
            }
        }
        this.mPipModeChangedTargetStackBounds = targetStackBounds;
        if (!this.mHandler.hasMessages(115)) {
            this.mHandler.sendEmptyMessage(115);
        }
    }

    /* access modifiers changed from: package-private */
    public void updatePictureInPictureMode(TaskRecord task, Rect targetStackBounds, boolean forceUpdate) {
        this.mHandler.removeMessages(115);
        for (int i = task.mActivities.size() - 1; i >= 0; i--) {
            ActivityRecord r = task.mActivities.get(i);
            if (!(r.app == null || r.app.thread == null)) {
                r.updatePictureInPictureMode(targetStackBounds, forceUpdate);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setDockedStackMinimized(boolean minimized) {
        this.mIsDockMinimized = minimized;
        if (this.mIsDockMinimized) {
            ActivityStack current = getFocusedStack();
            if (current.inSplitScreenPrimaryWindowingMode()) {
                current.adjustFocusToNextFocusableStack("setDockedStackMinimized");
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void wakeUp(String reason) {
        PowerManager powerManager = this.mPowerManager;
        long uptimeMillis = SystemClock.uptimeMillis();
        powerManager.wakeUp(uptimeMillis, "android.server.am:TURN_ON:" + reason);
    }

    private void beginDeferResume() {
        this.mDeferResumeCount++;
    }

    private void endDeferResume() {
        this.mDeferResumeCount--;
    }

    private boolean readyToResume() {
        return this.mDeferResumeCount == 0;
    }

    /* access modifiers changed from: package-private */
    public ActivityStack findStackBehind(ActivityStack stack) {
        ActivityDisplay display = this.mActivityDisplays.get(0);
        if (display == null) {
            return null;
        }
        for (int i = display.getChildCount() - 1; i >= 0; i--) {
            if (display.getChildAt(i) == stack && i > 0) {
                return display.getChildAt(i - 1);
            }
        }
        throw new IllegalStateException("Failed to find a stack behind stack=" + stack + " in=" + display);
    }

    /* access modifiers changed from: package-private */
    public void setResizingDuringAnimation(TaskRecord task) {
        this.mResizingTasksDuringAnimation.add(Integer.valueOf(task.taskId));
        task.setTaskDockedResizing(true);
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:105:0x0237 A[Catch:{ all -> 0x0243 }] */
    public int startActivityFromRecents(int callingPid, int callingUid, int taskId, SafeActivityOptions options) {
        ActivityOptions activityOptions;
        boolean z;
        int i;
        int i2;
        int windowingMode;
        String callingPackage;
        Intent intent;
        int userId;
        int taskCallingUid;
        TaskRecord task;
        TaskRecord task2;
        int windowingMode2;
        ActivityRecord targetActivity;
        int i3 = taskId;
        SafeActivityOptions safeActivityOptions = options;
        TaskRecord task3 = null;
        int activityType = 0;
        int windowingMode3 = 0;
        if (safeActivityOptions != null) {
            activityOptions = safeActivityOptions.getOptions(this);
        } else {
            activityOptions = null;
        }
        ActivityOptions activityOptions2 = activityOptions;
        if (activityOptions2 != null) {
            activityType = activityOptions2.getLaunchActivityType();
            windowingMode3 = activityOptions2.getLaunchWindowingMode();
        }
        int activityType2 = activityType;
        int windowingMode4 = windowingMode3;
        if (activityType2 == 2 || activityType2 == 3) {
            int i4 = activityType2;
            ActivityOptions activityOptions3 = activityOptions2;
            throw new IllegalArgumentException("startActivityFromRecents: Task " + i3 + " can't be launch in the home/recents stack.");
        }
        this.mWindowManager.deferSurfaceLayout();
        if (windowingMode4 == 3) {
            try {
                this.mWindowManager.setDockedStackCreateState(activityOptions2.getSplitScreenCreateMode(), null);
                deferUpdateRecentsHomeStackBounds();
                this.mWindowManager.prepareAppTransition(19, false);
            } catch (Throwable th) {
                th = th;
                i2 = 3;
                windowingMode = windowingMode4;
                int i5 = activityType2;
                ActivityOptions activityOptions4 = activityOptions2;
                z = false;
                i = 4;
            }
        }
        task3 = anyTaskForIdLocked(i3, 2, activityOptions2, true);
        if (task3 != null) {
            if (windowingMode4 != 3) {
                moveHomeStackToFront("startActivityFromRecents");
            }
            try {
                if (!this.mService.mUserController.shouldConfirmCredentials(task3.userId)) {
                    if (task3.getRootActivity() != null) {
                        targetActivity = task3.getTopActivity();
                        sendPowerHintForLaunchStartIfNeeded(true, targetActivity);
                        Flog.i(101, "task.userId =" + task3.userId + ", task.taskId = " + task3.taskId + ", task.getRootActivity() = " + task3.getRootActivity() + ", task.getTopActivity() = " + task3.getTopActivity());
                        this.mHwActivityStackSupervisorEx.handleFreeFormWindow(task3);
                        this.mActivityMetricsLogger.notifyActivityLaunching();
                        this.mService.moveTaskToFrontLocked(task3.taskId, 0, safeActivityOptions, true);
                        this.mActivityMetricsLogger.notifyActivityLaunched(2, targetActivity);
                        this.mService.getActivityStartController().postStartActivityProcessingForLastStarter(task3.getTopActivity(), 2, task3.getStack());
                        if (HwPCUtils.isPcCastModeInServer()) {
                            int launchDisplayId = 0;
                            if (!(activityOptions2 == null || activityOptions2.getLaunchDisplayId() == -1)) {
                                launchDisplayId = activityOptions2.getLaunchDisplayId();
                            }
                            if (!(task3.getStack() == null || task3.getStack().mDisplayId == launchDisplayId)) {
                                showToast(launchDisplayId);
                            }
                        }
                        if (windowingMode4 == 3 && task3 != null) {
                            try {
                                setResizingDuringAnimation(task3);
                                if (task3.getStack().getDisplay().getTopStackInWindowingMode(4).isActivityTypeHome()) {
                                    moveHomeStackToFront("startActivityFromRecents: homeVisibleInSplitScreen");
                                    this.mWindowManager.checkSplitScreenMinimizedChanged(false);
                                }
                            } catch (Throwable th2) {
                                this.mWindowManager.continueSurfaceLayout();
                                throw th2;
                            }
                        }
                        this.mWindowManager.continueSurfaceLayout();
                        return 2;
                    }
                }
                callingPackage = task3.mCallingPackage;
                intent = task3.intent;
                intent.addFlags(DumpState.DUMP_DEXOPT);
                userId = task3.userId;
                taskCallingUid = task3.mCallingUid;
                if (task3.inFreeformWindowingMode()) {
                    this.mRecentTasks.remove(task3);
                    task3.removedFromRecents();
                    task = null;
                } else {
                    task = task3;
                }
            } catch (Throwable th3) {
                th = th3;
                i2 = 3;
                windowingMode = windowingMode4;
                int i6 = activityType2;
                ActivityOptions activityOptions5 = activityOptions2;
                z = false;
                i = 4;
                try {
                    setResizingDuringAnimation(task3);
                    if (task3.getStack().getDisplay().getTopStackInWindowingMode(i).isActivityTypeHome()) {
                    }
                    this.mWindowManager.continueSurfaceLayout();
                    throw th;
                } catch (Throwable th4) {
                    this.mWindowManager.continueSurfaceLayout();
                    throw th4;
                }
            }
            try {
                task2 = task;
                windowingMode2 = windowingMode4;
                int i7 = activityType2;
                ActivityOptions activityOptions6 = activityOptions2;
            } catch (Throwable th5) {
                th = th5;
                task3 = task;
                i2 = 3;
                windowingMode = windowingMode4;
                int i8 = activityType2;
                ActivityOptions activityOptions7 = activityOptions2;
                z = false;
                i = 4;
                setResizingDuringAnimation(task3);
                if (task3.getStack().getDisplay().getTopStackInWindowingMode(i).isActivityTypeHome()) {
                }
                this.mWindowManager.continueSurfaceLayout();
                throw th;
            }
            try {
                int startActivityInPackage = this.mService.getActivityStartController().startActivityInPackage(taskCallingUid, callingPid, callingUid, callingPackage, intent, null, null, null, 0, 0, options, userId, task2, "startActivityFromRecents", false);
                if (windowingMode2 == 3) {
                    TaskRecord task4 = task2;
                    if (task4 != null) {
                        try {
                            setResizingDuringAnimation(task4);
                            if (task4.getStack().getDisplay().getTopStackInWindowingMode(4).isActivityTypeHome()) {
                                moveHomeStackToFront("startActivityFromRecents: homeVisibleInSplitScreen");
                                this.mWindowManager.checkSplitScreenMinimizedChanged(false);
                            }
                        } catch (Throwable th6) {
                            this.mWindowManager.continueSurfaceLayout();
                            throw th6;
                        }
                    }
                }
                this.mWindowManager.continueSurfaceLayout();
                return startActivityInPackage;
            } catch (Throwable th7) {
                th = th7;
                task3 = task2;
                windowingMode = windowingMode2;
                i2 = 3;
                i = 4;
                z = false;
                if (windowingMode == i2 && task3 != null) {
                    setResizingDuringAnimation(task3);
                    if (task3.getStack().getDisplay().getTopStackInWindowingMode(i).isActivityTypeHome()) {
                        moveHomeStackToFront("startActivityFromRecents: homeVisibleInSplitScreen");
                        this.mWindowManager.checkSplitScreenMinimizedChanged(z);
                    }
                }
                this.mWindowManager.continueSurfaceLayout();
                throw th;
            }
        } else {
            i2 = 3;
            windowingMode = windowingMode4;
            int i9 = activityType2;
            ActivityOptions activityOptions8 = activityOptions2;
            z = false;
            i = 4;
            try {
                continueUpdateRecentsHomeStackBounds();
                this.mWindowManager.executeAppTransition();
                throw new IllegalArgumentException("startActivityFromRecents: Task " + i3 + " not found.");
            } catch (Throwable th8) {
                th = th8;
                setResizingDuringAnimation(task3);
                if (task3.getStack().getDisplay().getTopStackInWindowingMode(i).isActivityTypeHome()) {
                }
                this.mWindowManager.continueSurfaceLayout();
                throw th;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public List<IBinder> getTopVisibleActivities() {
        ArrayList<IBinder> topActivityTokens = new ArrayList<>();
        for (int i = this.mActivityDisplays.size() - 1; i >= 0; i--) {
            ActivityDisplay display = this.mActivityDisplays.valueAt(i);
            for (int j = display.getChildCount() - 1; j >= 0; j--) {
                ActivityStack stack = display.getChildAt(j);
                if (stack.shouldBeVisible(null)) {
                    ActivityRecord top = stack.getTopActivity();
                    if (top != null) {
                        if (stack == this.mFocusedStack) {
                            topActivityTokens.add(0, top.appToken);
                        } else {
                            topActivityTokens.add(top.appToken);
                        }
                    }
                }
            }
        }
        return topActivityTokens;
    }

    /* access modifiers changed from: protected */
    public void showToast(int displayId) {
    }

    /* access modifiers changed from: protected */
    public boolean notKillProcessWhenRemoveTask(ProcessRecord processRecord) {
        return true;
    }
}
