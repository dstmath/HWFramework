package com.android.server.am;

import android.app.ActivityManagerInternal;
import android.app.ActivityOptions;
import android.app.AppGlobals;
import android.app.IActivityController;
import android.app.IApplicationThread;
import android.app.ResultInfo;
import android.app.WindowConfiguration;
import android.app.servertransaction.ActivityLifecycleItem;
import android.app.servertransaction.ActivityResultItem;
import android.app.servertransaction.ClientTransaction;
import android.app.servertransaction.ClientTransactionItem;
import android.app.servertransaction.DestroyActivityItem;
import android.app.servertransaction.NewIntentItem;
import android.app.servertransaction.PauseActivityItem;
import android.app.servertransaction.ResumeActivityItem;
import android.app.servertransaction.StopActivityItem;
import android.app.servertransaction.WindowVisibilityItem;
import android.common.HwFrameworkFactory;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.res.Configuration;
import android.freeform.HwFreeFormUtils;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Binder;
import android.os.Debug;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.UserHandle;
import android.service.voice.IVoiceInteractionSession;
import android.util.ArraySet;
import android.util.CoordinationModeUtils;
import android.util.EventLog;
import android.util.Flog;
import android.util.HwPCUtils;
import android.util.HwSlog;
import android.util.IntArray;
import android.util.Jlog;
import android.util.Log;
import android.util.Slog;
import android.util.SparseArray;
import android.util.proto.ProtoOutputStream;
import android.view.IApplicationToken;
import android.vrsystem.IVRSystemServiceManager;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.app.IVoiceInteractor;
import com.android.internal.os.BatteryStatsImpl;
import com.android.server.LocalServices;
import com.android.server.NetworkManagementService;
import com.android.server.Watchdog;
import com.android.server.am.ActivityManagerService;
import com.android.server.am.ActivityStackSupervisor;
import com.android.server.job.controllers.JobStatus;
import com.android.server.os.HwBootFail;
import com.android.server.pm.DumpState;
import com.android.server.wm.ConfigurationContainer;
import com.android.server.wm.StackWindowController;
import com.android.server.wm.StackWindowListener;
import com.android.server.wm.WindowManagerService;
import com.huawei.android.audio.HwAudioServiceManager;
import com.huawei.pgmng.log.LogPower;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class ActivityStack<T extends StackWindowController> extends AbsActivityStack implements StackWindowListener {
    static final int DESTROY_ACTIVITIES_MSG = 105;
    private static final int DESTROY_TIMEOUT = 10000;
    static final int DESTROY_TIMEOUT_MSG = 102;
    static final int FINISH_AFTER_PAUSE = 1;
    static final int FINISH_AFTER_VISIBLE = 2;
    static final int FINISH_IMMEDIATELY = 0;
    private static final int FIT_WITHIN_BOUNDS_DIVIDER = 3;
    static final int LAUNCH_TICK = 500;
    static final int LAUNCH_TICK_MSG = 103;
    private static final int MAX_STOPPING_TO_FORCE = 3;
    private static final int PAUSE_TIMEOUT = 500;
    static final int PAUSE_TIMEOUT_MSG = 101;
    @VisibleForTesting
    protected static final int REMOVE_TASK_MODE_DESTROYING = 0;
    static final int REMOVE_TASK_MODE_MOVING = 1;
    static final int REMOVE_TASK_MODE_MOVING_TO_TOP = 2;
    private static final boolean SHOW_APP_STARTING_PREVIEW = true;
    private static final int STOP_TIMEOUT = 11000;
    static final int STOP_TIMEOUT_MSG = 104;
    protected static final String TAG = "ActivityManager";
    private static final String TAG_ADD_REMOVE = "ActivityManager";
    private static final String TAG_APP = "ActivityManager";
    private static final String TAG_CLEANUP = "ActivityManager";
    private static final String TAG_CONTAINERS = "ActivityManager";
    private static final String TAG_PAUSE = "ActivityManager";
    private static final String TAG_RELEASE = "ActivityManager";
    private static final String TAG_RESULTS = "ActivityManager";
    private static final String TAG_SAVED_STATE = "ActivityManager";
    private static final String TAG_STACK = "ActivityManager";
    private static final String TAG_STATES = "ActivityManager";
    private static final String TAG_SWITCH = "ActivityManager";
    private static final String TAG_TASKS = "ActivityManager";
    private static final String TAG_TRANSITION = "ActivityManager";
    private static final String TAG_USER_LEAVING = "ActivityManager";
    private static final String TAG_VISIBILITY = (ActivityManagerService.TAG + ActivityManagerDebugConfig.POSTFIX_VISIBILITY);
    private static final long TRANSLUCENT_CONVERSION_TIMEOUT = 2000;
    static final int TRANSLUCENT_TIMEOUT_MSG = 106;
    protected static final boolean VALIDATE_TOKENS = false;
    boolean mConfigWillChange;
    protected String mCurrentPkgUnderFreeForm;
    protected long mCurrentTime;
    int mCurrentUser;
    private final Rect mDeferredBounds;
    private final Rect mDeferredTaskBounds;
    private final Rect mDeferredTaskInsetBounds;
    int mDisplayId;
    boolean mForceHidden;
    final Handler mHandler;
    protected boolean mIsFreeFormStackVisible;
    final ArrayList<ActivityRecord> mLRUActivities = new ArrayList<>();
    ActivityRecord mLastNoHistoryActivity;
    ActivityRecord mLastPausedActivity;
    ActivityRecord mPausingActivity;
    ActivityRecord mResumedActivity;
    final ActivityManagerService mService;
    final int mStackId;
    protected final ActivityStackSupervisor mStackSupervisor;
    protected ArrayList<TaskRecord> mTaskHistory = new ArrayList<>();
    private final ArrayList<ActivityRecord> mTmpActivities;
    private final SparseArray<Rect> mTmpBounds;
    private final SparseArray<Rect> mTmpInsetBounds;
    private final ActivityOptions mTmpOptions;
    private final Rect mTmpRect2;
    private boolean mTopActivityOccludesKeyguard;
    private ActivityRecord mTopDismissingKeyguardActivity;
    ActivityRecord mTranslucentActivityWaiting;
    ArrayList<ActivityRecord> mUndrawnActivitiesBelowTopTranslucent;
    private boolean mUpdateBoundsDeferred;
    private boolean mUpdateBoundsDeferredCalled;
    private IVRSystemServiceManager mVrMananger;
    T mWindowContainerController;
    private final WindowManagerService mWindowManager;
    String mshortComponentName;

    private class ActivityStackHandler extends Handler {
        ActivityStackHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            IApplicationToken.Stub stub = null;
            switch (msg.what) {
                case 101:
                    ActivityRecord r = (ActivityRecord) msg.obj;
                    Slog.w(ActivityManagerService.TAG, "Activity pause timeout for " + r);
                    synchronized (ActivityStack.this.mService) {
                        try {
                            ActivityManagerService.boostPriorityForLockedSection();
                            if (r.app != null) {
                                ActivityManagerService activityManagerService = ActivityStack.this.mService;
                                ProcessRecord processRecord = r.app;
                                long j = r.pauseTime;
                                activityManagerService.logAppTooSlow(processRecord, j, "pausing " + r);
                            }
                            ActivityStack.this.activityPausedLocked(r.appToken, true);
                        } catch (Throwable th) {
                            while (true) {
                                ActivityManagerService.resetPriorityAfterLockedSection();
                                throw th;
                                break;
                            }
                        }
                    }
                    ActivityManagerService.resetPriorityAfterLockedSection();
                    return;
                case 102:
                    ActivityRecord r2 = (ActivityRecord) msg.obj;
                    Slog.w(ActivityManagerService.TAG, "Activity destroy timeout for " + r2);
                    synchronized (ActivityStack.this.mService) {
                        try {
                            ActivityManagerService.boostPriorityForLockedSection();
                            ActivityStack activityStack = ActivityStack.this;
                            if (r2 != null) {
                                stub = r2.appToken;
                            }
                            activityStack.activityDestroyedLocked((IBinder) stub, "destroyTimeout");
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
                    ActivityRecord r3 = (ActivityRecord) msg.obj;
                    synchronized (ActivityStack.this.mService) {
                        try {
                            ActivityManagerService.boostPriorityForLockedSection();
                            if (r3.continueLaunchTickingLocked()) {
                                ActivityManagerService activityManagerService2 = ActivityStack.this.mService;
                                ProcessRecord processRecord2 = r3.app;
                                long j2 = r3.launchTickTime;
                                activityManagerService2.logAppTooSlow(processRecord2, j2, "launching " + r3);
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
                    ActivityRecord r4 = (ActivityRecord) msg.obj;
                    Slog.w(ActivityManagerService.TAG, "Activity stop timeout for " + r4);
                    synchronized (ActivityStack.this.mService) {
                        try {
                            ActivityManagerService.boostPriorityForLockedSection();
                            if (r4.isInHistory()) {
                                r4.activityStoppedLocked(null, null, null);
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
                    return;
                case 105:
                    ScheduleDestroyArgs args = (ScheduleDestroyArgs) msg.obj;
                    synchronized (ActivityStack.this.mService) {
                        try {
                            ActivityManagerService.boostPriorityForLockedSection();
                            ActivityStack.this.destroyActivitiesLocked(args.mOwner, args.mReason);
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
                case 106:
                    synchronized (ActivityStack.this.mService) {
                        try {
                            ActivityManagerService.boostPriorityForLockedSection();
                            ActivityStack.this.notifyActivityDrawnLocked(null);
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
    }

    enum ActivityState {
        INITIALIZING,
        RESUMED,
        PAUSING,
        PAUSED,
        STOPPING,
        STOPPED,
        FINISHING,
        DESTROYING,
        DESTROYED
    }

    private static class ScheduleDestroyArgs {
        final ProcessRecord mOwner;
        final String mReason;

        ScheduleDestroyArgs(ProcessRecord owner, String reason) {
            this.mOwner = owner;
            this.mReason = reason;
        }
    }

    /* access modifiers changed from: protected */
    public int getChildCount() {
        return this.mTaskHistory.size();
    }

    /* access modifiers changed from: protected */
    public ConfigurationContainer getChildAt(int index) {
        return this.mTaskHistory.get(index);
    }

    /* access modifiers changed from: protected */
    public ConfigurationContainer getParent() {
        return getDisplay();
    }

    /* access modifiers changed from: protected */
    public void onParentChanged() {
        super.onParentChanged();
        this.mStackSupervisor.updateUIDsPresentOnDisplay();
    }

    /* access modifiers changed from: package-private */
    public int numActivities() {
        int count = 0;
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            count += this.mTaskHistory.get(taskNdx).mActivities.size();
        }
        return count;
    }

    public ActivityStack(ActivityDisplay display, int stackId, ActivityStackSupervisor supervisor, int windowingMode, int activityType, boolean onTop) {
        Rect rect = null;
        this.mPausingActivity = null;
        this.mLastPausedActivity = null;
        this.mLastNoHistoryActivity = null;
        this.mResumedActivity = null;
        this.mTranslucentActivityWaiting = null;
        this.mUndrawnActivitiesBelowTopTranslucent = new ArrayList<>();
        this.mForceHidden = false;
        this.mDeferredBounds = new Rect();
        this.mDeferredTaskBounds = new Rect();
        this.mDeferredTaskInsetBounds = new Rect();
        this.mTmpBounds = new SparseArray<>();
        this.mTmpInsetBounds = new SparseArray<>();
        this.mTmpRect2 = new Rect();
        this.mTmpOptions = ActivityOptions.makeBasic();
        this.mTmpActivities = new ArrayList<>();
        this.mshortComponentName = BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
        this.mIsFreeFormStackVisible = false;
        this.mCurrentPkgUnderFreeForm = BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
        this.mStackSupervisor = supervisor;
        this.mService = supervisor.mService;
        this.mHandler = new ActivityStackHandler(this.mService.mHandler.getLooper());
        this.mWindowManager = this.mService.mWindowManager;
        this.mStackId = stackId;
        this.mCurrentUser = this.mService.mUserController.getCurrentUserId();
        this.mTmpRect2.setEmpty();
        this.mDisplayId = display.mDisplayId;
        setActivityType(activityType);
        setWindowingMode(windowingMode);
        this.mWindowContainerController = createStackWindowController(display.mDisplayId, onTop, this.mTmpRect2);
        postAddToDisplay(display, !this.mTmpRect2.isEmpty() ? this.mTmpRect2 : rect, onTop);
        this.mVrMananger = HwFrameworkFactory.getVRSystemServiceManager();
    }

    /* access modifiers changed from: package-private */
    public T createStackWindowController(int displayId, boolean onTop, Rect outBounds) {
        StackWindowController stackWindowController = new StackWindowController(this.mStackId, this, displayId, onTop, outBounds, this.mStackSupervisor.mWindowManager);
        return stackWindowController;
    }

    /* access modifiers changed from: package-private */
    public T getWindowContainerController() {
        return this.mWindowContainerController;
    }

    /* access modifiers changed from: package-private */
    public void onActivityStateChanged(ActivityRecord record, ActivityState state, String reason) {
        if (record == this.mResumedActivity && state != ActivityState.RESUMED) {
            setResumedActivity(null, reason + " - onActivityStateChanged");
        }
        if (state == ActivityState.RESUMED) {
            if (ActivityManagerDebugConfig.DEBUG_STACK) {
                Slog.v(ActivityManagerService.TAG, "set resumed activity to:" + record + " reason:" + reason);
            }
            setResumedActivity(record, reason + " - onActivityStateChanged");
            this.mService.setResumedActivityUncheckLocked(record, reason);
            this.mStackSupervisor.mRecentTasks.add(record.getTask());
        }
    }

    public void onConfigurationChanged(Configuration newParentConfig) {
        int prevWindowingMode = getWindowingMode();
        super.onConfigurationChanged(newParentConfig);
        ActivityDisplay display = getDisplay();
        if (display != null && prevWindowingMode != getWindowingMode()) {
            display.onStackWindowingModeChanged(this);
        }
    }

    public void setWindowingMode(int windowingMode) {
        setWindowingMode(windowingMode, false, false, false, false);
    }

    /* access modifiers changed from: package-private */
    public void setWindowingMode(int preferredWindowingMode, boolean animate, boolean showRecents, boolean enteringSplitScreenMode, boolean deferEnsuringVisibility) {
        int i = preferredWindowingMode;
        boolean creating = this.mWindowContainerController == null;
        int currentMode = getWindowingMode();
        ActivityDisplay display = getDisplay();
        TaskRecord topTask = topTask();
        ActivityStack splitScreenStack = display.getSplitScreenPrimaryStack();
        this.mTmpOptions.setLaunchWindowingMode(i);
        int windowingMode = (creating || i == 12) ? i : display.resolveWindowingMode(null, this.mTmpOptions, topTask, getActivityType());
        if (splitScreenStack == this && windowingMode == 4) {
            windowingMode = 1;
        }
        boolean alreadyInSplitScreenMode = display.hasSplitScreenPrimaryStack();
        boolean sendNonResizeableNotification = (enteringSplitScreenMode || i == 11 || i == 12) ? false : true;
        if (alreadyInSplitScreenMode && windowingMode == 1 && sendNonResizeableNotification && isActivityTypeStandardOrUndefined()) {
            if ((i == 3 || i == 4) || creating) {
                this.mService.mTaskChangeNotificationController.notifyActivityDismissingDockedStack();
                display.getSplitScreenPrimaryStack().setWindowingMode(1, false, false, false, true);
            }
        }
        boolean alreadyInCoordinationMode = display.hasCoordinationPrimaryStack();
        if (alreadyInCoordinationMode && windowingMode == 1 && creating) {
            Slog.v(ActivityManagerService.TAG, "exit coordination mode for launching fullscreen window mode");
            this.mService.mHwAMSEx.exitCoordinationModeInner(false, true);
        }
        if (currentMode != windowingMode) {
            WindowManagerService wm = this.mService.mWindowManager;
            ActivityRecord topActivity = getTopActivity();
            if (!sendNonResizeableNotification || windowingMode == 1 || topActivity == null || !topActivity.isNonResizableOrForcedResizable() || topActivity.noDisplay) {
                boolean z = alreadyInCoordinationMode;
            } else {
                boolean z2 = sendNonResizeableNotification;
                boolean z3 = alreadyInCoordinationMode;
                this.mService.mTaskChangeNotificationController.notifyActivityForcedResizable(topTask.taskId, 1, topActivity.appInfo.packageName);
            }
            wm.deferSurfaceLayout();
            if (!animate && topActivity != null) {
                try {
                    this.mStackSupervisor.mNoAnimActivities.add(topActivity);
                } catch (Throwable th) {
                    wm.continueSurfaceLayout();
                    throw th;
                }
            }
            super.setWindowingMode(windowingMode);
            if (creating) {
                if (showRecents && !alreadyInSplitScreenMode) {
                    try {
                        if (this.mDisplayId == 0 && windowingMode == 3) {
                            display.getOrCreateStack(4, 3, true).moveToFront("setWindowingMode");
                            this.mService.mWindowManager.showRecentApps();
                        }
                    } catch (Throwable th2) {
                        wm.continueSurfaceLayout();
                        throw th2;
                    }
                }
                wm.continueSurfaceLayout();
            } else if (windowingMode == 2 || currentMode == 2) {
                throw new IllegalArgumentException("Changing pinned windowing mode not currently supported");
            } else if (windowingMode != 3 || splitScreenStack == null) {
                this.mTmpRect2.setEmpty();
                if (windowingMode != 1) {
                    this.mWindowContainerController.getRawBounds(this.mTmpRect2);
                    if (windowingMode == 5 && topTask != null) {
                        Rect bounds = topTask().getLaunchBounds();
                        if (bounds != null) {
                            this.mTmpRect2.set(bounds);
                        }
                    }
                }
                if (!Objects.equals(getOverrideBounds(), this.mTmpRect2) || (currentMode == 5 && windowingMode == 1)) {
                    resize(this.mTmpRect2, null, null);
                }
                if (showRecents && !alreadyInSplitScreenMode) {
                    try {
                        if (this.mDisplayId == 0 && windowingMode == 3) {
                            display.getOrCreateStack(4, 3, true).moveToFront("setWindowingMode");
                            this.mService.mWindowManager.showRecentApps();
                        }
                    } catch (Throwable th3) {
                        wm.continueSurfaceLayout();
                        throw th3;
                    }
                }
                wm.continueSurfaceLayout();
                if (!deferEnsuringVisibility) {
                    this.mStackSupervisor.ensureActivitiesVisibleLocked(null, 0, true);
                    this.mStackSupervisor.resumeFocusedStackTopActivityLocked();
                }
            } else {
                throw new IllegalArgumentException("Setting primary split-screen windowing mode while there is already one isn't currently supported");
            }
        }
    }

    public boolean isCompatible(int windowingMode, int activityType) {
        if (activityType == 0) {
            activityType = 1;
        }
        ActivityDisplay display = getDisplay();
        if (display != null && activityType == 1 && windowingMode == 0) {
            windowingMode = display.getWindowingMode();
        }
        return super.isCompatible(windowingMode, activityType);
    }

    /* access modifiers changed from: package-private */
    public void reparent(ActivityDisplay activityDisplay, boolean onTop) {
        removeFromDisplay();
        this.mTmpRect2.setEmpty();
        this.mWindowContainerController.reparent(activityDisplay.mDisplayId, this.mTmpRect2, onTop);
        postAddToDisplay(activityDisplay, this.mTmpRect2.isEmpty() ? null : this.mTmpRect2, onTop);
        adjustFocusToNextFocusableStack("reparent", true);
        this.mStackSupervisor.resumeFocusedStackTopActivityLocked();
        this.mStackSupervisor.ensureActivitiesVisibleLocked(null, 0, false);
    }

    private void postAddToDisplay(ActivityDisplay activityDisplay, Rect bounds, boolean onTop) {
        this.mDisplayId = activityDisplay.mDisplayId;
        setBounds(bounds);
        onParentChanged();
        activityDisplay.addChild(this, onTop ? HwBootFail.STAGE_BOOT_SUCCESS : Integer.MIN_VALUE);
        if (inSplitScreenPrimaryWindowingMode()) {
            this.mStackSupervisor.resizeDockedStackLocked(getOverrideBounds(), null, null, null, null, true);
        }
    }

    private void removeFromDisplay() {
        ActivityDisplay display = getDisplay();
        if (display != null) {
            display.removeChild(this);
        }
        this.mDisplayId = -1;
    }

    /* access modifiers changed from: package-private */
    public void remove() {
        removeFromDisplay();
        this.mWindowContainerController.removeContainer();
        this.mWindowContainerController = null;
        onParentChanged();
    }

    /* access modifiers changed from: package-private */
    public ActivityDisplay getDisplay() {
        return this.mStackSupervisor.getActivityDisplay(this.mDisplayId);
    }

    /* access modifiers changed from: package-private */
    public void getStackDockedModeBounds(Rect currentTempTaskBounds, Rect outStackBounds, Rect outTempTaskBounds, boolean ignoreVisibility) {
        this.mWindowContainerController.getStackDockedModeBounds(currentTempTaskBounds, outStackBounds, outTempTaskBounds, ignoreVisibility);
    }

    /* access modifiers changed from: package-private */
    public void prepareFreezingTaskBounds() {
        this.mWindowContainerController.prepareFreezingTaskBounds();
    }

    /* access modifiers changed from: package-private */
    public void getWindowContainerBounds(Rect outBounds) {
        if (this.mWindowContainerController != null) {
            this.mWindowContainerController.getBounds(outBounds);
        } else {
            outBounds.setEmpty();
        }
    }

    /* access modifiers changed from: package-private */
    public void getBoundsForNewConfiguration(Rect outBounds) {
        this.mWindowContainerController.getBoundsForNewConfiguration(outBounds);
    }

    /* access modifiers changed from: package-private */
    public void positionChildWindowContainerAtTop(TaskRecord child) {
        this.mWindowContainerController.positionChildAtTop(child.getWindowContainerController(), true);
    }

    /* access modifiers changed from: package-private */
    public boolean deferScheduleMultiWindowModeChanged() {
        return false;
    }

    /* access modifiers changed from: package-private */
    public void deferUpdateBounds() {
        if (!this.mUpdateBoundsDeferred) {
            this.mUpdateBoundsDeferred = true;
            this.mUpdateBoundsDeferredCalled = false;
        }
    }

    /* access modifiers changed from: package-private */
    public void continueUpdateBounds() {
        Rect rect;
        Rect rect2;
        boolean wasDeferred = this.mUpdateBoundsDeferred;
        this.mUpdateBoundsDeferred = false;
        if (wasDeferred && this.mUpdateBoundsDeferredCalled) {
            Rect rect3 = null;
            if (this.mDeferredBounds.isEmpty()) {
                rect = null;
            } else {
                rect = this.mDeferredBounds;
            }
            if (this.mDeferredTaskBounds.isEmpty()) {
                rect2 = null;
            } else {
                rect2 = this.mDeferredTaskBounds;
            }
            if (!this.mDeferredTaskInsetBounds.isEmpty()) {
                rect3 = this.mDeferredTaskInsetBounds;
            }
            resize(rect, rect2, rect3);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean updateBoundsAllowed(Rect bounds, Rect tempTaskBounds, Rect tempTaskInsetBounds) {
        if (!this.mUpdateBoundsDeferred) {
            return true;
        }
        if (bounds != null) {
            this.mDeferredBounds.set(bounds);
        } else {
            this.mDeferredBounds.setEmpty();
        }
        if (tempTaskBounds != null) {
            this.mDeferredTaskBounds.set(tempTaskBounds);
        } else {
            this.mDeferredTaskBounds.setEmpty();
        }
        if (tempTaskInsetBounds != null) {
            this.mDeferredTaskInsetBounds.set(tempTaskInsetBounds);
        } else {
            this.mDeferredTaskInsetBounds.setEmpty();
        }
        this.mUpdateBoundsDeferredCalled = true;
        return false;
    }

    public int setBounds(Rect bounds) {
        return super.setBounds(!inMultiWindowMode() ? null : bounds);
    }

    /* access modifiers changed from: package-private */
    public ActivityRecord topRunningActivityLocked() {
        return topRunningActivityLocked(false);
    }

    /* access modifiers changed from: package-private */
    public void getAllRunningVisibleActivitiesLocked(ArrayList<ActivityRecord> outActivities) {
        outActivities.clear();
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            this.mTaskHistory.get(taskNdx).getAllRunningVisibleActivitiesLocked(outActivities);
        }
    }

    private ActivityRecord topRunningActivityLocked(boolean focusableOnly) {
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            ActivityRecord r = this.mTaskHistory.get(taskNdx).topRunningActivityLocked();
            if (r != null && (!focusableOnly || r.isFocusable())) {
                return r;
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public ActivityRecord topRunningNonOverlayTaskActivity() {
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            ArrayList<ActivityRecord> activities = this.mTaskHistory.get(taskNdx).mActivities;
            for (int activityNdx = activities.size() - 1; activityNdx >= 0; activityNdx--) {
                ActivityRecord r = activities.get(activityNdx);
                if (!r.finishing && !r.mTaskOverlay) {
                    return r;
                }
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public ActivityRecord topRunningNonDelayedActivityLocked(ActivityRecord notTop) {
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            ArrayList<ActivityRecord> activities = this.mTaskHistory.get(taskNdx).mActivities;
            for (int activityNdx = activities.size() - 1; activityNdx >= 0; activityNdx--) {
                ActivityRecord r = activities.get(activityNdx);
                if (!r.finishing && !r.delayedResume && r != notTop && r.okToShowLocked()) {
                    return r;
                }
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public final ActivityRecord topRunningActivityLocked(IBinder token, int taskId) {
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            TaskRecord task = this.mTaskHistory.get(taskNdx);
            if (task.taskId != taskId) {
                ArrayList<ActivityRecord> activities = task.mActivities;
                for (int i = activities.size() - 1; i >= 0; i--) {
                    ActivityRecord r = activities.get(i);
                    if (!r.finishing && token != r.appToken && r.okToShowLocked()) {
                        return r;
                    }
                }
                continue;
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public ActivityRecord getTopActivity() {
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            ActivityRecord r = this.mTaskHistory.get(taskNdx).getTopActivity();
            if (r != null) {
                return r;
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public final TaskRecord topTask() {
        int size = this.mTaskHistory.size();
        if (size > 0) {
            return this.mTaskHistory.get(size - 1);
        }
        return null;
    }

    private TaskRecord bottomTask() {
        if (this.mTaskHistory.isEmpty()) {
            return null;
        }
        return this.mTaskHistory.get(0);
    }

    /* access modifiers changed from: package-private */
    public TaskRecord taskForIdLocked(int id) {
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            TaskRecord task = this.mTaskHistory.get(taskNdx);
            if (task.taskId == id) {
                return task;
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public ActivityRecord isInStackLocked(IBinder token) {
        return isInStackLocked(ActivityRecord.forTokenLocked(token));
    }

    /* access modifiers changed from: package-private */
    public ActivityRecord isInStackLocked(ActivityRecord r) {
        if (r == null) {
            return null;
        }
        TaskRecord task = r.getTask();
        ActivityStack stack = r.getStack();
        if (stack == null || !task.mActivities.contains(r) || !this.mTaskHistory.contains(task)) {
            return null;
        }
        if (stack != this) {
            Slog.w(ActivityManagerService.TAG, "Illegal state! task does not point to stack it is in.");
        }
        return r;
    }

    /* access modifiers changed from: package-private */
    public boolean isInStackLocked(TaskRecord task) {
        return this.mTaskHistory.contains(task);
    }

    /* access modifiers changed from: package-private */
    public boolean isUidPresent(int uid) {
        Iterator<TaskRecord> it = this.mTaskHistory.iterator();
        while (it.hasNext()) {
            Iterator<ActivityRecord> it2 = it.next().mActivities.iterator();
            while (true) {
                if (it2.hasNext()) {
                    if (it2.next().getUid() == uid) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public void getPresentUIDs(IntArray presentUIDs) {
        Iterator<TaskRecord> it = this.mTaskHistory.iterator();
        while (it.hasNext()) {
            Iterator<ActivityRecord> it2 = it.next().mActivities.iterator();
            while (it2.hasNext()) {
                presentUIDs.add(it2.next().getUid());
            }
        }
    }

    /* access modifiers changed from: package-private */
    public final void removeActivitiesFromLRUListLocked(TaskRecord task) {
        Iterator<ActivityRecord> it = task.mActivities.iterator();
        while (it.hasNext()) {
            this.mLRUActivities.remove(it.next());
        }
    }

    /* access modifiers changed from: package-private */
    public final boolean updateLRUListLocked(ActivityRecord r) {
        boolean hadit = this.mLRUActivities.remove(r);
        this.mLRUActivities.add(r);
        return hadit;
    }

    /* access modifiers changed from: package-private */
    public final boolean isHomeOrRecentsStack() {
        return isActivityTypeHome() || isActivityTypeRecents();
    }

    /* access modifiers changed from: package-private */
    public final boolean isOnHomeDisplay() {
        return this.mDisplayId == 0;
    }

    private boolean returnsToHomeStack() {
        if (inMultiWindowMode() || this.mTaskHistory.isEmpty() || !this.mTaskHistory.get(0).returnsToHomeStack()) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public void moveToFront(String reason) {
        moveToFront(reason, null);
    }

    /* access modifiers changed from: protected */
    public void moveToFront(String reason, TaskRecord task) {
        if (isAttached()) {
            ActivityDisplay display = getDisplay();
            if (inSplitScreenSecondaryWindowingMode()) {
                ActivityStack topFullScreenStack = display.getTopStackInWindowingMode(1);
                if (topFullScreenStack != null) {
                    ActivityStack primarySplitScreenStack = display.getSplitScreenPrimaryStack();
                    if (primarySplitScreenStack != null && display.getIndexOf(topFullScreenStack) > display.getIndexOf(primarySplitScreenStack)) {
                        primarySplitScreenStack.moveToFront(reason + " splitScreenToTop");
                    }
                }
            }
            if (!isActivityTypeHome() && returnsToHomeStack()) {
                ActivityStackSupervisor activityStackSupervisor = this.mStackSupervisor;
                activityStackSupervisor.moveHomeStackToFront(reason + " returnToHome");
            }
            display.positionChildAtTop(this);
            this.mStackSupervisor.setFocusStackUnchecked(reason, this);
            if (task != null) {
                insertTaskAtTop(task, null);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void moveToBack(String reason, TaskRecord task) {
        if (isAttached()) {
            ActivityStack targetStack = this.mStackSupervisor.getTargetSplitTopStack(this);
            if (targetStack != null) {
                this.mWindowManager.mShouldResetTime = true;
                this.mWindowManager.startFreezingScreen(0, 0);
                getDisplay().positionChildAtTop(targetStack);
                if (getWindowingMode() != 3) {
                    targetStack.setWindowingMode(1);
                }
            }
            if (getWindowingMode() == 3) {
                setWindowingMode(1);
            }
            if (targetStack != null) {
                this.mWindowManager.stopFreezingScreen();
            }
            getDisplay().positionChildAtBottom(this);
            this.mStackSupervisor.setFocusStackUnchecked(reason, targetStack != null ? targetStack : getDisplay().getTopStack());
            if (task != null) {
                insertTaskAtBottom(task);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isFocusable() {
        ActivityRecord r = topRunningActivityLocked();
        return this.mStackSupervisor.isFocusable(this, r != null && r.isFocusable());
    }

    /* access modifiers changed from: package-private */
    public final boolean isAttached() {
        return getParent() != null;
    }

    /* access modifiers changed from: package-private */
    public void findTaskLocked(ActivityRecord target, ActivityStackSupervisor.FindTaskResult result) {
        ActivityInfo info;
        int userId;
        ActivityInfo info2;
        int userId2;
        Uri taskDocumentData;
        boolean taskIsDocument;
        Uri taskDocumentData2;
        ActivityStack activityStack = this;
        ActivityRecord activityRecord = target;
        ActivityStackSupervisor.FindTaskResult findTaskResult = result;
        Intent intent = activityRecord.intent;
        ActivityInfo info3 = activityRecord.info;
        ComponentName cls = intent.getComponent();
        if (info3.targetActivity != null) {
            cls = new ComponentName(info3.packageName, info3.targetActivity);
        }
        int userId3 = UserHandle.getUserId(info3.applicationInfo.uid);
        boolean z = false;
        boolean isDocument = (intent != null) & intent.isDocument();
        Uri documentData = isDocument ? intent.getData() : null;
        if (ActivityManagerDebugConfig.DEBUG_TASKS) {
            Slog.d(ActivityManagerService.TAG, "Looking for task of " + activityRecord + " in " + activityStack);
        }
        int taskNdx = activityStack.mTaskHistory.size() - 1;
        while (taskNdx >= 0) {
            TaskRecord task = activityStack.mTaskHistory.get(taskNdx);
            if (task.voiceSession != null) {
                if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                    Slog.d(ActivityManagerService.TAG, "Skipping " + task + ": voice session");
                }
            } else if (task.userId == userId3) {
                ActivityRecord r = task.getTopActivity(z);
                if (r == null || r.finishing) {
                    info2 = info3;
                    userId2 = userId3;
                } else if ((r.userId != userId3 && (!activityStack.mStackSupervisor.isCurrentProfileLocked(r.userId) || !activityStack.mStackSupervisor.isCurrentProfileLocked(userId3))) || r.launchMode == 3) {
                    info2 = info3;
                    userId2 = userId3;
                } else if (r.hasCompatibleActivityType(activityRecord)) {
                    Intent taskIntent = task.intent;
                    Intent affinityIntent = task.affinityIntent;
                    if (taskIntent != null && taskIntent.isDocument()) {
                        taskIsDocument = true;
                        taskDocumentData = taskIntent.getData();
                    } else if (affinityIntent == null || !affinityIntent.isDocument()) {
                        taskIsDocument = false;
                        taskDocumentData = null;
                    } else {
                        taskIsDocument = true;
                        taskDocumentData = affinityIntent.getData();
                    }
                    Uri taskDocumentData3 = taskDocumentData;
                    if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                        StringBuilder sb = new StringBuilder();
                        userId = userId3;
                        sb.append("Comparing existing cls=");
                        sb.append(taskIntent.getComponent().flattenToShortString());
                        sb.append("/aff=");
                        sb.append(r.getTask().rootAffinity);
                        sb.append(" to new cls=");
                        sb.append(intent.getComponent().flattenToShortString());
                        sb.append("/aff=");
                        sb.append(info3.taskAffinity);
                        Slog.d(ActivityManagerService.TAG, sb.toString());
                    } else {
                        userId = userId3;
                    }
                    if (taskIntent == null || taskIntent.getComponent() == null || taskIntent.getComponent().compareTo(cls) != 0) {
                        info = info3;
                        taskDocumentData2 = taskDocumentData3;
                    } else {
                        taskDocumentData2 = taskDocumentData3;
                        if (Objects.equals(documentData, taskDocumentData2)) {
                            if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                                StringBuilder sb2 = new StringBuilder();
                                ActivityInfo activityInfo = info3;
                                sb2.append("Found matching taskIntent for ");
                                sb2.append(intent);
                                sb2.append(" bringing to top: ");
                                sb2.append(r.intent);
                                Slog.d(ActivityManagerService.TAG, sb2.toString());
                            }
                            findTaskResult.r = r;
                            findTaskResult.matchedByRootAffinity = false;
                            return;
                        }
                        info = info3;
                    }
                    if (affinityIntent != null && affinityIntent.getComponent() != null && affinityIntent.getComponent().compareTo(cls) == 0 && Objects.equals(documentData, taskDocumentData2)) {
                        if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                            Slog.d(ActivityManagerService.TAG, "Found matching affinityIntent For " + intent + " bringing to top: " + r.intent);
                        }
                        findTaskResult.r = r;
                        findTaskResult.matchedByRootAffinity = false;
                        return;
                    } else if (isDocument || taskIsDocument || findTaskResult.r != null || task.rootAffinity == null) {
                        if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                            Slog.d(ActivityManagerService.TAG, "Not a match: " + task);
                        }
                        taskNdx--;
                        userId3 = userId;
                        info3 = info;
                        activityStack = this;
                        z = false;
                    } else {
                        if (task.rootAffinity.equals(activityRecord.taskAffinity)) {
                            if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                                Slog.d(ActivityManagerService.TAG, "Found matching affinity candidate!");
                            }
                            findTaskResult.r = r;
                            findTaskResult.matchedByRootAffinity = true;
                        } else if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                            Slog.d(ActivityManagerService.TAG, "Not found matching affinity candidate!");
                        }
                        taskNdx--;
                        userId3 = userId;
                        info3 = info;
                        activityStack = this;
                        z = false;
                    }
                } else if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                    Slog.d(ActivityManagerService.TAG, "Skipping " + task + ": mismatch activity type");
                }
                if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                    Slog.d(ActivityManagerService.TAG, "Skipping " + task + ": mismatch root " + r);
                }
                taskNdx--;
                userId3 = userId;
                info3 = info;
                activityStack = this;
                z = false;
            } else if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                Slog.d(ActivityManagerService.TAG, "Skipping " + task + ": different user");
            }
            info = info3;
            userId = userId3;
            taskNdx--;
            userId3 = userId;
            info3 = info;
            activityStack = this;
            z = false;
        }
        int i = userId3;
    }

    /* access modifiers changed from: package-private */
    public ActivityRecord findActivityLocked(Intent intent, ActivityInfo info, boolean compareIntentFilters) {
        ComponentName cls = intent.getComponent();
        if (info.targetActivity != null) {
            cls = new ComponentName(info.packageName, info.targetActivity);
        }
        int userId = UserHandle.getUserId(info.applicationInfo.uid);
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            ArrayList<ActivityRecord> activities = this.mTaskHistory.get(taskNdx).mActivities;
            for (int activityNdx = activities.size() - 1; activityNdx >= 0; activityNdx--) {
                ActivityRecord r = activities.get(activityNdx);
                if (r.okToShowLocked() && !r.finishing && r.userId == userId) {
                    if (compareIntentFilters) {
                        if (r.intent.filterEquals(intent)) {
                            return r;
                        }
                    } else if (r.intent.getComponent().equals(cls)) {
                        return r;
                    }
                }
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public final void switchUserLocked(int userId) {
        if (this.mCurrentUser != userId) {
            this.mCurrentUser = userId;
            int index = this.mTaskHistory.size();
            int i = 0;
            while (i < index) {
                TaskRecord task = this.mTaskHistory.get(i);
                ensureActivitiesVisibleLockedForSwitchUser(task);
                if (task.okToShowLocked()) {
                    if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                        Slog.d(ActivityManagerService.TAG, "switchUserLocked: stack=" + getStackId() + " moving " + task + " to top");
                    }
                    this.mTaskHistory.remove(i);
                    this.mTaskHistory.add(task);
                    index--;
                } else {
                    i++;
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void ensureActivitiesVisibleLockedForSwitchUser(TaskRecord task) {
        if (!this.mStackSupervisor.isCurrentProfileLocked(task.userId)) {
            ActivityRecord top = task.getTopActivity();
            if (top != null && top != task.topRunningActivityLocked() && top.visible && top.isState(ActivityState.STOPPING, ActivityState.STOPPED)) {
                Flog.i(101, "Making invisible for switch user:  top: " + top + ", finishing: " + top.finishing + " state: " + top.getState());
                try {
                    top.setVisible(false);
                    switch (top.getState()) {
                        case STOPPING:
                        case STOPPED:
                            if (top.app != null && top.app.thread != null) {
                                this.mService.getLifecycleManager().scheduleTransaction(top.app.thread, (IBinder) top.appToken, (ClientTransactionItem) WindowVisibilityItem.obtain(false));
                                return;
                            }
                            return;
                        default:
                            return;
                    }
                } catch (Exception e) {
                    Slog.w(ActivityManagerService.TAG, "for switch user Exception thrown making hidden: " + top.intent.getComponent(), e);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void minimalResumeActivityLocked(ActivityRecord r) {
        if (ActivityManagerDebugConfig.DEBUG_STATES) {
            Slog.v(ActivityManagerService.TAG, "Moving to RESUMED: " + r + " (starting new instance) callers=" + Debug.getCallers(5));
        }
        if (!this.mService.getActivityStartController().mCurActivityPkName.equals(r.packageName)) {
            Jlog.d(142, r.packageName, r.app.pid, BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
            LogPower.push(113, r.packageName);
            this.mService.getActivityStartController().mCurActivityPkName = r.packageName;
        }
        r.setState(ActivityState.RESUMED, "minimalResumeActivityLocked");
        r.completeResumeLocked();
        if ("com.android.incallui.InCallActivity".equals(r.info.name) && (!r.visible || r.stopped || this.mWindowManager.isKeyguardOccluded())) {
            r.notifyAppResumed(true);
        }
        this.mStackSupervisor.getLaunchTimeTracker().setLaunchTime(r);
        if (r.app != null && this.mService.mSystemReady) {
            this.mService.getDAMonitor().noteActivityDisplayedStart(r.shortComponentName, r.app.uid, r.app.pid);
        }
        if (ActivityManagerDebugConfig.DEBUG_SAVED_STATE) {
            Slog.i(ActivityManagerService.TAG, "Launch completed; removing icicle of " + r.icicle);
        }
    }

    private void clearLaunchTime(ActivityRecord r) {
        if (this.mStackSupervisor.mWaitingActivityLaunched.isEmpty()) {
            r.fullyDrawnStartTime = 0;
            r.displayStartTime = 0;
            if (r.task != null) {
                r.task.isLaunching = false;
                return;
            }
            return;
        }
        this.mStackSupervisor.removeTimeoutsForActivityLocked(r);
        this.mStackSupervisor.scheduleIdleTimeoutLocked(r);
    }

    /* access modifiers changed from: package-private */
    public void awakeFromSleepingLocked() {
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            ArrayList<ActivityRecord> activities = this.mTaskHistory.get(taskNdx).mActivities;
            for (int activityNdx = activities.size() - 1; activityNdx >= 0; activityNdx--) {
                activities.get(activityNdx).setSleeping(false);
            }
        }
        if (this.mPausingActivity != null) {
            Flog.i(101, "Previously pausing activity " + this.mPausingActivity.shortComponentName + " state : " + this.mPausingActivity.getState());
            activityPausedLocked(this.mPausingActivity.appToken, true);
        }
    }

    /* access modifiers changed from: package-private */
    public void updateActivityApplicationInfoLocked(ApplicationInfo aInfo) {
        if (aInfo != null) {
            String packageName = aInfo.packageName;
            int userId = UserHandle.getUserId(aInfo.uid);
            for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
                List<ActivityRecord> activities = this.mTaskHistory.get(taskNdx).mActivities;
                for (int activityNdx = activities.size() - 1; activityNdx >= 0; activityNdx--) {
                    ActivityRecord ar = activities.get(activityNdx);
                    if (userId == ar.userId && packageName.equals(ar.packageName)) {
                        ar.updateApplicationInfo(aInfo);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void checkReadyForSleep() {
        if (shouldSleepActivities() && goToSleepIfPossible(false)) {
            this.mStackSupervisor.checkReadyForSleepLocked(true);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean goToSleepIfPossible(boolean shuttingDown) {
        boolean shouldSleep = true;
        if (this.mResumedActivity != null) {
            if (ActivityManagerDebugConfig.DEBUG_PAUSE) {
                Slog.v(ActivityManagerService.TAG, "Sleep still need to pause " + this.mResumedActivity);
            }
            if (ActivityManagerDebugConfig.DEBUG_USER_LEAVING) {
                Slog.v(ActivityManagerService.TAG, "Sleep => pause with userLeaving=false");
            }
            startPausingLocked(false, true, null, false);
            shouldSleep = false;
        } else if (this.mPausingActivity != null) {
            if (ActivityManagerDebugConfig.DEBUG_PAUSE) {
                Slog.v(ActivityManagerService.TAG, "Sleep still waiting to pause " + this.mPausingActivity);
            }
            shouldSleep = false;
        }
        if (!shuttingDown) {
            if (containsActivityFromStack(this.mStackSupervisor.mStoppingActivities)) {
                if (ActivityManagerDebugConfig.DEBUG_PAUSE) {
                    Slog.v(ActivityManagerService.TAG, "Sleep still need to stop " + this.mStackSupervisor.mStoppingActivities.size() + " activities");
                }
                this.mStackSupervisor.scheduleIdleLocked();
                shouldSleep = false;
            }
            if (containsActivityFromStack(this.mStackSupervisor.mGoingToSleepActivities)) {
                if (ActivityManagerDebugConfig.DEBUG_PAUSE) {
                    Slog.v(ActivityManagerService.TAG, "Sleep still need to sleep " + this.mStackSupervisor.mGoingToSleepActivities.size() + " activities");
                }
                shouldSleep = false;
            }
        }
        if (shouldSleep) {
            goToSleep();
        }
        return shouldSleep;
    }

    /* access modifiers changed from: package-private */
    public void goToSleep() {
        ensureActivitiesVisibleLocked(null, 0, false);
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            ArrayList<ActivityRecord> activities = this.mTaskHistory.get(taskNdx).mActivities;
            for (int activityNdx = activities.size() - 1; activityNdx >= 0; activityNdx--) {
                ActivityRecord r = activities.get(activityNdx);
                if (r.isState(ActivityState.STOPPING, ActivityState.STOPPED, ActivityState.PAUSED, ActivityState.PAUSING)) {
                    r.setSleeping(true);
                }
            }
        }
    }

    private boolean containsActivityFromStack(List<ActivityRecord> rs) {
        for (ActivityRecord r : rs) {
            if (r.getStack() == this) {
                return true;
            }
        }
        return false;
    }

    private void schedulePauseTimeout(ActivityRecord r) {
        Message msg = this.mHandler.obtainMessage(101);
        msg.obj = r;
        r.pauseTime = SystemClock.uptimeMillis();
        this.mHandler.sendMessageDelayed(msg, 500);
        if (ActivityManagerDebugConfig.DEBUG_PAUSE) {
            Slog.v(ActivityManagerService.TAG, "Waiting for pause to complete...");
        }
    }

    /* access modifiers changed from: package-private */
    public final boolean startPausingLocked(boolean userLeaving, boolean uiSleeping, ActivityRecord resuming, boolean pauseImmediately) {
        if (this.mPausingActivity != null) {
            Slog.wtf(ActivityManagerService.TAG, "Going to pause when pause is already pending for " + this.mPausingActivity + " state=" + this.mPausingActivity.getState());
            if (!shouldSleepActivities()) {
                completePauseLocked(false, resuming);
            }
        }
        ActivityRecord prev = this.mResumedActivity;
        if (prev == null) {
            if (resuming == null) {
                Slog.wtf(ActivityManagerService.TAG, "Trying to pause when nothing is resumed");
                this.mStackSupervisor.resumeFocusedStackTopActivityLocked();
            }
            return false;
        } else if (prev == resuming) {
            Slog.wtf(ActivityManagerService.TAG, "Trying to pause activity that is in process of being resumed");
            return false;
        } else {
            if (ActivityManagerDebugConfig.DEBUG_STATES) {
                Slog.v(ActivityManagerService.TAG, "Moving to PAUSING: " + prev + " in stack " + this.mStackId, new Exception());
            } else {
                Flog.i(101, "Moving to PAUSING: " + prev + " in stack " + this.mStackId);
            }
            this.mPausingActivity = prev;
            this.mLastPausedActivity = prev;
            this.mLastNoHistoryActivity = ((prev.intent.getFlags() & 1073741824) == 0 && (prev.info.flags & 128) == 0) ? null : prev;
            prev.setState(ActivityState.PAUSING, "startPausingLocked");
            prev.getTask().touchActiveTime();
            clearLaunchTime(prev);
            this.mStackSupervisor.getLaunchTimeTracker().stopFullyDrawnTraceIfNeeded(getWindowingMode());
            this.mService.updateCpuStats();
            if (prev.app == null || prev.app.thread == null) {
                Flog.i(101, "Clear pausing activity " + this.mPausingActivity + " in stack " + this.mStackId + " for tha app is not ready");
                this.mPausingActivity = null;
                this.mLastPausedActivity = null;
                this.mLastNoHistoryActivity = null;
            } else {
                if (ActivityManagerDebugConfig.DEBUG_PAUSE) {
                    Slog.v(ActivityManagerService.TAG, "Enqueueing pending pause: " + prev);
                }
                try {
                    int i = prev.userId;
                    int identityHashCode = System.identityHashCode(prev);
                    String str = prev.shortComponentName;
                    EventLogTags.writeAmPauseActivity(i, identityHashCode, str, "userLeaving=" + userLeaving);
                    this.mService.updateUsageStats(prev, false);
                    if (Jlog.isPerfTest()) {
                        Jlog.i(3024, Jlog.getMessage("ActivityStack", "startPausingLocked", Intent.toPkgClsString(prev.realActivity, "who")));
                    }
                    this.mService.getLifecycleManager().scheduleTransaction(prev.app.thread, (IBinder) prev.appToken, (ActivityLifecycleItem) PauseActivityItem.obtain(prev.finishing, userLeaving, prev.configChangeFlags, pauseImmediately));
                } catch (Exception e) {
                    Slog.w(ActivityManagerService.TAG, "Exception thrown during pause", e);
                    this.mPausingActivity = null;
                    this.mLastPausedActivity = null;
                    this.mLastNoHistoryActivity = null;
                }
            }
            if (!uiSleeping && !this.mService.isSleepingOrShuttingDownLocked()) {
                this.mStackSupervisor.acquireLaunchWakelock();
            }
            if (this.mPausingActivity != null) {
                if (!uiSleeping) {
                    prev.pauseKeyDispatchingLocked();
                } else if (ActivityManagerDebugConfig.DEBUG_PAUSE) {
                    Slog.v(ActivityManagerService.TAG, "Key dispatch not paused for screen off");
                }
                if (pauseImmediately) {
                    completePauseLocked(false, resuming);
                    return false;
                }
                schedulePauseTimeout(prev);
                return true;
            }
            if (ActivityManagerDebugConfig.DEBUG_PAUSE) {
                Slog.v(ActivityManagerService.TAG, "Activity not running, resuming next.");
            }
            if (resuming == null) {
                this.mStackSupervisor.mActivityLaunchTrack = " activityNotRunning";
                this.mStackSupervisor.resumeFocusedStackTopActivityLocked();
            }
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public final void activityPausedLocked(IBinder token, boolean timeout) {
        String str;
        if (ActivityManagerDebugConfig.DEBUG_PAUSE) {
            Slog.v(ActivityManagerService.TAG, "Activity paused: token=" + token + ", timeout=" + timeout);
        }
        ActivityRecord r = isInStackLocked(token);
        if (r != null) {
            if (Jlog.isPerfTest()) {
                Jlog.i(3029, Jlog.getMessage("ActivityStack", "activityPausedLocked", Intent.toPkgClsString(r.realActivity, "who")));
            }
            this.mHandler.removeMessages(101, r);
            if (this.mPausingActivity == r) {
                StringBuilder sb = new StringBuilder();
                sb.append("Moving to PAUSED: ");
                sb.append(r);
                sb.append(timeout ? " (due to timeout)" : " (pause complete)");
                sb.append(" in stack ");
                sb.append(this.mStackId);
                Flog.i(101, sb.toString());
                this.mService.mWindowManager.deferSurfaceLayout();
                try {
                    completePauseLocked(true, null);
                    return;
                } finally {
                    str = "activitypause";
                    this.mService.mWindowManager.mAppTransitTrack = str;
                    this.mService.mWindowManager.continueSurfaceLayout();
                }
            } else {
                Object[] objArr = new Object[4];
                objArr[0] = Integer.valueOf(r.userId);
                objArr[1] = Integer.valueOf(System.identityHashCode(r));
                objArr[2] = r.shortComponentName;
                objArr[3] = this.mPausingActivity != null ? this.mPausingActivity.shortComponentName : "(none)";
                EventLog.writeEvent(EventLogTags.AM_FAILED_TO_PAUSE, objArr);
                if (r.isState(ActivityState.PAUSING)) {
                    r.setState(ActivityState.PAUSED, "activityPausedLocked");
                    if (r.finishing) {
                        Flog.i(101, "Executing finish of failed to pause activity: " + r);
                        finishCurrentActivityLocked(r, 2, false, "activityPausedLocked");
                    } else {
                        Flog.i(101, "Not process of failed to pause activity: " + r);
                    }
                }
            }
        } else {
            ActivityRecord record = ActivityRecord.forTokenLocked(token);
            if (record != null) {
                Flog.i(101, "FAILED to find record " + record + " in stack " + this.mStackId + " while pausing " + this.mPausingActivity);
            }
        }
        this.mStackSupervisor.ensureActivitiesVisibleLocked(null, 0, false);
    }

    private void completePauseLocked(boolean resumeNext, ActivityRecord resuming) {
        ActivityRecord prev = this.mPausingActivity;
        if (ActivityManagerDebugConfig.DEBUG_PAUSE) {
            Slog.v(ActivityManagerService.TAG, "Complete pause: " + prev);
        }
        if (prev != null) {
            HwAudioServiceManager.setSoundEffectState(false, prev.packageName, false, null);
        }
        if (prev != null) {
            prev.setWillCloseOrEnterPip(false);
            boolean wasStopping = prev.isState(ActivityState.STOPPING);
            prev.setState(ActivityState.PAUSED, "completePausedLocked");
            if (prev.finishing) {
                if (ActivityManagerDebugConfig.DEBUG_PAUSE) {
                    Slog.v(ActivityManagerService.TAG, "Executing finish of activity: " + prev);
                }
                prev = finishCurrentActivityLocked(prev, 2, false, "completedPausedLocked");
            } else if (prev.app != null) {
                if (ActivityManagerDebugConfig.DEBUG_PAUSE) {
                    Slog.v(ActivityManagerService.TAG, "Enqueue pending stop if needed: " + prev + " wasStopping=" + wasStopping + " visible=" + prev.visible);
                }
                if (this.mStackSupervisor.mActivitiesWaitingForVisibleActivity.remove(prev) && (ActivityManagerDebugConfig.DEBUG_SWITCH || ActivityManagerDebugConfig.DEBUG_PAUSE)) {
                    Slog.v(ActivityManagerService.TAG, "Complete pause, no longer waiting: " + prev);
                }
                if (prev.deferRelaunchUntilPaused) {
                    Slog.v(ActivityManagerService.TAG, "Re-launching after pause: " + prev);
                    prev.relaunchActivityLocked(false, prev.preserveWindowOnDeferredRelaunch);
                } else if (wasStopping) {
                    prev.setState(ActivityState.STOPPING, "completePausedLocked");
                } else if (!prev.visible || shouldSleepOrShutDownActivities()) {
                    prev.setDeferHidingClient(false);
                    addToStopping(prev, true, false);
                }
            } else {
                if (ActivityManagerDebugConfig.DEBUG_PAUSE) {
                    Slog.v(ActivityManagerService.TAG, "App died during pause, not stopping: " + prev);
                }
                prev = null;
            }
            if (prev != null) {
                prev.stopFreezingScreenLocked(true);
            }
            this.mPausingActivity = null;
        }
        if (resumeNext) {
            ActivityStack topStack = this.mStackSupervisor.getFocusedStack();
            if (!topStack.shouldSleepOrShutDownActivities()) {
                this.mStackSupervisor.mActivityLaunchTrack = "activityPaused";
                this.mStackSupervisor.resumeFocusedStackTopActivityLocked(topStack, prev, null);
            } else {
                checkReadyForSleep();
                ActivityRecord top = topStack.topRunningActivityLocked();
                if (top == null || !(prev == null || top == prev)) {
                    this.mStackSupervisor.mActivityLaunchTrack = "sleepingNoMoreActivityRun";
                    this.mStackSupervisor.resumeFocusedStackTopActivityLocked();
                }
            }
        }
        if (prev != null) {
            prev.resumeKeyDispatchingLocked();
            if (prev.app != null && prev.cpuTimeAtResume > 0 && this.mService.mBatteryStatsService.isOnBattery()) {
                long diff = this.mService.mProcessCpuTracker.getCpuTimeForPid(prev.app.pid) - prev.cpuTimeAtResume;
                if (diff > 0) {
                    BatteryStatsImpl bsi = this.mService.mBatteryStatsService.getActiveStatistics();
                    synchronized (bsi) {
                        BatteryStatsImpl.Uid.Proc ps = bsi.getProcessStatsLocked(prev.info.applicationInfo.uid, prev.info.packageName);
                        if (ps != null) {
                            ps.addForegroundTimeLocked(diff);
                        }
                    }
                }
            }
            prev.cpuTimeAtResume = 0;
        }
        if (this.mStackSupervisor.mAppVisibilitiesChangedSinceLastPause || (getDisplay() != null && getDisplay().hasPinnedStack())) {
            this.mService.mTaskChangeNotificationController.notifyTaskStackChanged();
            this.mStackSupervisor.mAppVisibilitiesChangedSinceLastPause = false;
        }
        this.mStackSupervisor.ensureActivitiesVisibleLocked(resuming, 0, false);
        if (getDisplay() == null) {
            Slog.i(ActivityManagerService.TAG, "getDisplay() == null, DisplayId: " + this.mDisplayId + "  StackId: " + this.mStackId);
        }
    }

    /* access modifiers changed from: package-private */
    public void addToStopping(ActivityRecord r, boolean scheduleIdle, boolean idleDelayed) {
        if (!this.mStackSupervisor.mStoppingActivities.contains(r)) {
            this.mStackSupervisor.mStoppingActivities.add(r);
        }
        boolean z = true;
        if (this.mStackSupervisor.mStoppingActivities.size() <= 3 && (!r.frontOfTask || this.mTaskHistory.size() > 1)) {
            z = false;
        }
        boolean forceIdle = z;
        if (scheduleIdle || forceIdle) {
            if (ActivityManagerDebugConfig.DEBUG_PAUSE) {
                Slog.v(ActivityManagerService.TAG, "Scheduling idle now: forceIdle=" + forceIdle + "immediate=" + (!idleDelayed));
            }
            if (!idleDelayed) {
                this.mStackSupervisor.scheduleIdleLocked();
            } else {
                this.mStackSupervisor.scheduleIdleTimeoutLocked(r);
            }
        } else {
            checkReadyForSleep();
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public boolean isStackTranslucent(ActivityRecord starting) {
        if (!isAttached() || this.mForceHidden) {
            return true;
        }
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            ArrayList<ActivityRecord> activities = this.mTaskHistory.get(taskNdx).mActivities;
            for (int activityNdx = activities.size() - 1; activityNdx >= 0; activityNdx--) {
                ActivityRecord r = activities.get(activityNdx);
                if (r.finishing) {
                    if (ActivityManagerDebugConfig.DEBUG_VISIBILITY) {
                        Slog.v(TAG_VISIBILITY, "It is in finishing activity now");
                    }
                } else if (r.visibleIgnoringKeyguard || r == starting) {
                    if (r.fullscreen || r.hasWallpaper) {
                        if (ActivityManagerDebugConfig.DEBUG_VISIBILITY) {
                            Slog.v(TAG_VISIBILITY, "Stack has at least one fullscreen activity -> untranslucent");
                        }
                        return false;
                    }
                } else if (ActivityManagerDebugConfig.DEBUG_VISIBILITY) {
                    Slog.v(TAG_VISIBILITY, "It is not the currently starting activity");
                }
            }
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean isTopStackOnDisplay() {
        if (getDisplay() == null) {
            return false;
        }
        return getDisplay().isTopStack(this);
    }

    /* access modifiers changed from: package-private */
    public boolean isTopActivityVisible() {
        ActivityRecord topActivity = getTopActivity();
        return topActivity != null && topActivity.visible;
    }

    /* access modifiers changed from: protected */
    public boolean shouldBeVisible(ActivityRecord starting) {
        ActivityRecord activityRecord = starting;
        boolean z = false;
        if (!isAttached() || this.mForceHidden) {
            return false;
        }
        if (this.mStackSupervisor.isFocusedStack(this)) {
            if (ActivityManagerDebugConfig.DEBUG_VISIBILITY) {
                Slog.v(TAG_VISIBILITY, "It is the focusedStack -> visible");
            }
            return true;
        } else if (topRunningActivityLocked() == null && isInStackLocked(starting) == null && !isTopStackOnDisplay()) {
            if (ActivityManagerDebugConfig.DEBUG_VISIBILITY) {
                Slog.v(TAG_VISIBILITY, "No running activities -> invisible");
            }
            return false;
        } else {
            ActivityDisplay display = getDisplay();
            boolean gotSplitScreenStack = false;
            boolean gotOpaqueSplitScreenPrimary = false;
            boolean gotOpaqueSplitScreenSecondary = false;
            int windowingMode = getWindowingMode();
            boolean isAssistantType = isActivityTypeAssistant();
            if (ActivityManagerDebugConfig.DEBUG_VISIBILITY) {
                Slog.v(TAG_VISIBILITY, "Current stack windowingMode:" + windowingMode + " activityType:" + getActivityType());
            }
            int i = display.getChildCount() - 1;
            while (i >= 0) {
                ActivityStack other = display.getChildAt(i);
                if (other != this) {
                    int otherWindowingMode = other.getWindowingMode();
                    if (ActivityManagerDebugConfig.DEBUG_VISIBILITY) {
                        Slog.v(TAG_VISIBILITY, "Other stack:" + other.toShortString());
                    }
                    if (otherWindowingMode == 1) {
                        int activityType = other.getActivityType();
                        if (windowingMode == 3 && (activityType == 2 || (activityType == 4 && this.mWindowManager.getRecentsAnimationController() != null))) {
                            return true;
                        }
                        if (!other.isStackTranslucent(activityRecord)) {
                            return false;
                        }
                        if (ActivityManagerDebugConfig.DEBUG_VISIBILITY) {
                            Slog.v(TAG_VISIBILITY, "It is behind a translucent fullscreen stack");
                        }
                    } else {
                        if ((otherWindowingMode == 3 || otherWindowingMode == 11) && !gotOpaqueSplitScreenPrimary) {
                            gotSplitScreenStack = true;
                            gotOpaqueSplitScreenPrimary = !other.isStackTranslucent(activityRecord);
                            if (windowingMode == 1) {
                                ActivityStack currentFocusStack = this.mStackSupervisor.getFocusedStack();
                                if (currentFocusStack != null && currentFocusStack.inSplitScreenWindowingMode()) {
                                    if (ActivityManagerDebugConfig.DEBUG_VISIBILITY) {
                                        Slog.v(TAG_VISIBILITY, "current is in split or exiting split mode,not show fullscreen stack");
                                    }
                                    return false;
                                }
                            }
                            if ((windowingMode == 3 || windowingMode == 11) && gotOpaqueSplitScreenPrimary) {
                                if (ActivityManagerDebugConfig.DEBUG_VISIBILITY) {
                                    Slog.v(TAG_VISIBILITY, "It is behind another opaque stack in ssp mode -> invisible");
                                }
                                return false;
                            }
                        } else if ((otherWindowingMode == 4 || otherWindowingMode == 12) && !gotOpaqueSplitScreenSecondary) {
                            gotSplitScreenStack = true;
                            gotOpaqueSplitScreenSecondary = !other.isStackTranslucent(activityRecord);
                            if ((windowingMode == 4 || windowingMode == 12) && gotOpaqueSplitScreenSecondary) {
                                if (ActivityManagerDebugConfig.DEBUG_VISIBILITY) {
                                    Slog.v(TAG_VISIBILITY, "It is behind another opaque stack in sss mode -> invisible");
                                }
                                return false;
                            }
                        }
                        if (gotOpaqueSplitScreenPrimary && gotOpaqueSplitScreenSecondary) {
                            if (ActivityManagerDebugConfig.DEBUG_VISIBILITY) {
                                Slog.v(TAG_VISIBILITY, "It is in ssw mode -> invisible");
                            }
                            return false;
                        } else if (isAssistantType && gotSplitScreenStack) {
                            if (ActivityManagerDebugConfig.DEBUG_VISIBILITY) {
                                Slog.v(TAG_VISIBILITY, "Assistant stack can't be visible behind split-screen -> invisible");
                            }
                            return false;
                        }
                    }
                    i--;
                    z = false;
                } else if (windowingMode != 3 && this.mService.mSkipShowLauncher) {
                    return z;
                } else {
                    if (ActivityManagerDebugConfig.DEBUG_VISIBILITY) {
                        Slog.v(TAG_VISIBILITY, "No other stack occluding -> visible");
                    }
                    return true;
                }
            }
            return true;
        }
    }

    /* access modifiers changed from: package-private */
    public final int rankTaskLayers(int baseLayer) {
        int layer = 0;
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            TaskRecord task = this.mTaskHistory.get(taskNdx);
            ActivityRecord r = task.topRunningActivityLocked();
            if (r == null || r.finishing || !r.visible) {
                task.mLayerRank = -1;
            } else {
                task.mLayerRank = layer + baseLayer;
                layer++;
            }
        }
        return layer;
    }

    /* access modifiers changed from: package-private */
    public final void ensureActivitiesVisibleLocked(ActivityRecord starting, int configChanges, boolean preserveWindows) {
        ensureActivitiesVisibleLocked(starting, configChanges, preserveWindows, true);
    }

    /* access modifiers changed from: package-private */
    public final void ensureActivitiesVisibleLocked(ActivityRecord starting, int configChanges, boolean preserveWindows, boolean notifyClients) {
        boolean z;
        TaskRecord task;
        ArrayList<ActivityRecord> activities;
        boolean behindFullscreenActivity;
        boolean visibleIgnoringKeyguard;
        ActivityRecord r;
        int activityNdx;
        ActivityRecord activityRecord = starting;
        boolean z2 = notifyClients;
        boolean z3 = false;
        this.mTopActivityOccludesKeyguard = false;
        this.mTopDismissingKeyguardActivity = null;
        this.mStackSupervisor.getKeyguardController().beginActivityVisibilityUpdate();
        try {
            ActivityRecord top = topRunningActivityLocked();
            if (ActivityManagerDebugConfig.DEBUG_VISIBILITY) {
                Slog.v(TAG_VISIBILITY, "ensureActivitiesVisible behind " + top + " configChanges=0x" + Integer.toHexString(configChanges));
            }
            if (top != null) {
                checkTranslucentActivityWaiting(top);
            }
            boolean z4 = true;
            boolean aboveTop = top != null;
            boolean stackShouldBeVisible = shouldBeVisible(starting);
            boolean behindFullscreenActivity2 = !stackShouldBeVisible;
            boolean resumeNextActivity = this.mStackSupervisor.isFocusedStack(this) && isInStackLocked(starting) == null;
            boolean isTopNotPinnedStack = isAttached() && getDisplay().isTopNotPinnedStack(this);
            int taskNdx = this.mTaskHistory.size() - 1;
            int configChanges2 = configChanges;
            while (true) {
                int taskNdx2 = taskNdx;
                if (taskNdx2 < 0) {
                    break;
                }
                try {
                    TaskRecord task2 = this.mTaskHistory.get(taskNdx2);
                    ArrayList<ActivityRecord> activities2 = task2.mActivities;
                    int activityNdx2 = activities2.size() - 1;
                    boolean resumeNextActivity2 = resumeNextActivity;
                    int configChanges3 = configChanges2;
                    while (true) {
                        int activityNdx3 = activityNdx2;
                        if (activityNdx3 < 0) {
                            break;
                        }
                        try {
                            ActivityRecord r2 = activities2.get(activityNdx3);
                            if (!r2.finishing) {
                                boolean isTop = r2 == top ? z4 : false;
                                if (!aboveTop || isTop) {
                                    boolean visibleIgnoringKeyguard2 = r2.shouldBeVisibleIgnoringKeyguard(behindFullscreenActivity2);
                                    r2.visibleIgnoringKeyguard = visibleIgnoringKeyguard2;
                                    boolean reallyVisible = checkKeyguardVisibility(r2, visibleIgnoringKeyguard2, (!isTop || !isTopNotPinnedStack) ? false : z4);
                                    if (visibleIgnoringKeyguard2) {
                                        if (stackShouldBeVisible) {
                                            z4 = false;
                                        }
                                        behindFullscreenActivity = updateBehindFullscreen(z4, behindFullscreenActivity2, r2);
                                    } else {
                                        behindFullscreenActivity = behindFullscreenActivity2;
                                    }
                                    if (reallyVisible) {
                                        if (ActivityManagerDebugConfig.DEBUG_VISIBILITY) {
                                            String str = TAG_VISIBILITY;
                                            boolean z5 = reallyVisible;
                                            StringBuilder sb = new StringBuilder();
                                            visibleIgnoringKeyguard = visibleIgnoringKeyguard2;
                                            sb.append("Make visible? ");
                                            sb.append(r2);
                                            sb.append(" finishing=");
                                            sb.append(r2.finishing);
                                            sb.append(" state=");
                                            sb.append(r2.getState());
                                            Slog.v(str, sb.toString());
                                        } else {
                                            visibleIgnoringKeyguard = visibleIgnoringKeyguard2;
                                        }
                                        if (r2 == activityRecord || !z2) {
                                            boolean z6 = preserveWindows;
                                        } else {
                                            r2.ensureActivityConfiguration(0, preserveWindows, true);
                                        }
                                        if (r2.app != null) {
                                            if (r2.app.thread != null) {
                                                if (r2.visible) {
                                                    if (ActivityManagerDebugConfig.DEBUG_VISIBILITY) {
                                                        Slog.v(TAG_VISIBILITY, "Skipping: already visible at " + r2);
                                                    }
                                                    if (r2.mClientVisibilityDeferred && z2) {
                                                        r2.makeClientVisible();
                                                    }
                                                    if (r2.handleAlreadyVisible()) {
                                                        resumeNextActivity2 = false;
                                                        r = r2;
                                                        activities = activities2;
                                                        task = task2;
                                                        boolean z7 = visibleIgnoringKeyguard;
                                                        z = false;
                                                        configChanges3 |= r.configChangeFlags;
                                                    }
                                                } else {
                                                    if (!this.mService.getActivityStartController().mCurActivityPkName.equals(r2.appInfo.packageName)) {
                                                        LogPower.push(148, "visible", r2.appInfo.packageName);
                                                    }
                                                    r2.makeVisibleIfNeeded(activityRecord, z2);
                                                }
                                                r = r2;
                                                activities = activities2;
                                                activityNdx = activityNdx3;
                                                task = task2;
                                                boolean z8 = visibleIgnoringKeyguard;
                                                z = false;
                                                activityNdx3 = activityNdx;
                                                configChanges3 |= r.configChangeFlags;
                                            }
                                        }
                                        z = false;
                                        r = r2;
                                        boolean z9 = visibleIgnoringKeyguard;
                                        activities = activities2;
                                        activityNdx = activityNdx3;
                                        task = task2;
                                        if (makeVisibleAndRestartIfNeeded(activityRecord, configChanges3, isTop, resumeNextActivity2, r)) {
                                            if (activityNdx >= activities.size()) {
                                                activityNdx3 = activities.size() - 1;
                                                configChanges3 |= r.configChangeFlags;
                                            } else {
                                                resumeNextActivity2 = false;
                                            }
                                        }
                                        activityNdx3 = activityNdx;
                                        configChanges3 |= r.configChangeFlags;
                                    } else {
                                        boolean z10 = visibleIgnoringKeyguard2;
                                        ActivityRecord r3 = r2;
                                        activities = activities2;
                                        int activityNdx4 = activityNdx3;
                                        task = task2;
                                        z = false;
                                        if (ActivityManagerDebugConfig.DEBUG_VISIBILITY || r3.isState(ActivityState.RESUMED)) {
                                            String str2 = TAG_VISIBILITY;
                                            StringBuilder sb2 = new StringBuilder();
                                            sb2.append("Make invisible? ");
                                            sb2.append(r3);
                                            sb2.append(" finishing=");
                                            sb2.append(r3.finishing);
                                            sb2.append(" state=");
                                            sb2.append(r3.getState());
                                            sb2.append(" stackShouldBeVisible=");
                                            sb2.append(stackShouldBeVisible);
                                            sb2.append(" behindFullscreenActivity=");
                                            sb2.append(behindFullscreenActivity);
                                            sb2.append(" mLaunchTaskBehind=");
                                            sb2.append(r3.mLaunchTaskBehind);
                                            sb2.append(" keyguardShowing = ");
                                            sb2.append(this.mStackSupervisor.getKeyguardController().isKeyguardShowing(this.mDisplayId != -1 ? this.mDisplayId : 0));
                                            sb2.append(" keyguardLocked = ");
                                            sb2.append(this.mStackSupervisor.getKeyguardController().isKeyguardLocked());
                                            sb2.append(" r.visibleIgnoringKeyguard = ");
                                            sb2.append(r3.visibleIgnoringKeyguard);
                                            Slog.v(str2, sb2.toString());
                                        }
                                        makeInvisible(r3);
                                        activityNdx3 = activityNdx4;
                                    }
                                    behindFullscreenActivity2 = behindFullscreenActivity;
                                    aboveTop = false;
                                    activityNdx2 = activityNdx3 - 1;
                                    activities2 = activities;
                                    task2 = task;
                                    z3 = z;
                                    activityRecord = starting;
                                    z2 = notifyClients;
                                    z4 = true;
                                }
                            }
                            activities = activities2;
                            task = task2;
                            z = false;
                            activityNdx2 = activityNdx3 - 1;
                            activities2 = activities;
                            task2 = task;
                            z3 = z;
                            activityRecord = starting;
                            z2 = notifyClients;
                            z4 = true;
                        } catch (Throwable th) {
                            th = th;
                            this.mStackSupervisor.getKeyguardController().endActivityVisibilityUpdate();
                            throw th;
                        }
                    }
                    boolean z11 = z3;
                    ArrayList<ActivityRecord> arrayList = activities2;
                    TaskRecord task3 = task2;
                    if (getWindowingMode() == 5) {
                        behindFullscreenActivity2 = !stackShouldBeVisible ? true : z11;
                    } else if (isActivityTypeHome()) {
                        if (ActivityManagerDebugConfig.DEBUG_VISIBILITY) {
                            Slog.v(TAG_VISIBILITY, "Home task: at " + task3 + " stackShouldBeVisible=" + stackShouldBeVisible + " behindFullscreenActivity=" + behindFullscreenActivity2);
                        }
                        if (task3.getTopActivity() != null) {
                            behindFullscreenActivity2 = true;
                        }
                    }
                    taskNdx = taskNdx2 - 1;
                    resumeNextActivity = resumeNextActivity2;
                    configChanges2 = configChanges3;
                    z3 = z11;
                    activityRecord = starting;
                    z2 = notifyClients;
                    z4 = true;
                } catch (Throwable th2) {
                    th = th2;
                    int i = configChanges2;
                    this.mStackSupervisor.getKeyguardController().endActivityVisibilityUpdate();
                    throw th;
                }
            }
            if (this.mTranslucentActivityWaiting != null && this.mUndrawnActivitiesBelowTopTranslucent.isEmpty()) {
                notifyActivityDrawnLocked(null);
            }
            this.mStackSupervisor.getKeyguardController().endActivityVisibilityUpdate();
        } catch (Throwable th3) {
            th = th3;
            int i2 = configChanges;
            this.mStackSupervisor.getKeyguardController().endActivityVisibilityUpdate();
            throw th;
        }
    }

    /* access modifiers changed from: package-private */
    public void addStartingWindowsForVisibleActivities(boolean taskSwitch) {
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            this.mTaskHistory.get(taskNdx).addStartingWindowsForVisibleActivities(taskSwitch);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean topActivityOccludesKeyguard() {
        return this.mTopActivityOccludesKeyguard;
    }

    /* access modifiers changed from: package-private */
    public boolean resizeStackWithLaunchBounds() {
        return inPinnedWindowingMode();
    }

    public boolean supportsSplitScreenWindowingMode() {
        TaskRecord topTask = topTask();
        return super.supportsSplitScreenWindowingMode() && (topTask == null || topTask.supportsSplitScreenWindowingMode());
    }

    /* access modifiers changed from: package-private */
    public boolean affectedBySplitScreenResize() {
        boolean z = false;
        if (!supportsSplitScreenWindowingMode()) {
            return false;
        }
        int windowingMode = getWindowingMode();
        if (!(windowingMode == 5 || windowingMode == 2)) {
            z = true;
        }
        return z;
    }

    /* access modifiers changed from: package-private */
    public ActivityRecord getTopDismissingKeyguardActivity() {
        return this.mTopDismissingKeyguardActivity;
    }

    /* access modifiers changed from: package-private */
    public boolean checkKeyguardVisibility(ActivityRecord r, boolean shouldBeVisible, boolean isTop) {
        if (!shouldBeVisible) {
            return shouldBeVisible;
        }
        boolean z = false;
        boolean keyguardOrAodShowing = this.mStackSupervisor.getKeyguardController().isKeyguardOrAodShowing(this.mDisplayId != -1 ? this.mDisplayId : 0);
        boolean keyguardLocked = this.mStackSupervisor.getKeyguardController().isKeyguardLocked();
        boolean showWhenLocked = r.canShowWhenLocked();
        boolean dismissKeyguard = r.hasDismissKeyguardWindows();
        if (keyguardLocked && (showWhenLocked || dismissKeyguard)) {
            if (!this.mService.mHwAMSEx.isAllowToStartActivity(this.mService.mContext, this.mService.mContext.getPackageName(), r.info, keyguardLocked, ((ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class)).getLastResumedActivity())) {
                showWhenLocked = false;
                dismissKeyguard = false;
            }
        }
        if (shouldBeVisible) {
            if (dismissKeyguard && this.mTopDismissingKeyguardActivity == null) {
                this.mTopDismissingKeyguardActivity = r;
            }
            if (isTop) {
                this.mTopActivityOccludesKeyguard |= showWhenLocked;
            }
            if (canShowWithInsecureKeyguard() && this.mStackSupervisor.getKeyguardController().canDismissKeyguard()) {
                return true;
            }
        }
        if (keyguardOrAodShowing) {
            if (shouldBeVisible && this.mStackSupervisor.getKeyguardController().canShowActivityWhileKeyguardShowing(r, dismissKeyguard)) {
                z = true;
            }
            return z;
        } else if (!keyguardLocked) {
            return shouldBeVisible;
        } else {
            if (shouldBeVisible && this.mStackSupervisor.getKeyguardController().canShowWhileOccluded(dismissKeyguard, showWhenLocked)) {
                z = true;
            }
            return z;
        }
    }

    private boolean canShowWithInsecureKeyguard() {
        ActivityDisplay activityDisplay = getDisplay();
        if (activityDisplay != null) {
            return (activityDisplay.mDisplay.getFlags() & 32) != 0;
        }
        throw new IllegalStateException("Stack is not attached to any display, stackId=" + this.mStackId);
    }

    private void checkTranslucentActivityWaiting(ActivityRecord top) {
        if (this.mTranslucentActivityWaiting != top) {
            this.mUndrawnActivitiesBelowTopTranslucent.clear();
            if (this.mTranslucentActivityWaiting != null) {
                notifyActivityDrawnLocked(null);
                this.mTranslucentActivityWaiting = null;
            }
            this.mHandler.removeMessages(106);
        }
    }

    private boolean makeVisibleAndRestartIfNeeded(ActivityRecord starting, int configChanges, boolean isTop, boolean andResume, ActivityRecord r) {
        if (isTop || !r.visible) {
            if (ActivityManagerDebugConfig.DEBUG_VISIBILITY) {
                String str = TAG_VISIBILITY;
                Slog.v(str, "Start and freeze screen for " + r);
            }
            if (r != starting) {
                r.startFreezingScreenLocked(r.app, configChanges);
            }
            if (!r.visible || r.mLaunchTaskBehind) {
                if (ActivityManagerDebugConfig.DEBUG_VISIBILITY) {
                    String str2 = TAG_VISIBILITY;
                    Slog.v(str2, "Starting and making visible: " + r);
                }
                r.setVisible(true);
            }
            if (r != starting) {
                this.mStackSupervisor.startSpecificActivityLocked(r, andResume, false);
                return true;
            }
        }
        return false;
    }

    private void makeInvisible(ActivityRecord r) {
        if (!r.visible) {
            if (ActivityManagerDebugConfig.DEBUG_VISIBILITY) {
                String str = TAG_VISIBILITY;
                Slog.v(str, "Already invisible: " + r);
            }
            return;
        }
        Flog.i(106, "Making invisible: " + r + " " + r.getState());
        try {
            boolean canEnterPictureInPicture = r.checkEnterPictureInPictureState("makeInvisible", true);
            r.setDeferHidingClient(canEnterPictureInPicture && !r.isState(ActivityState.STOPPING, ActivityState.STOPPED, ActivityState.PAUSED));
            r.setVisible(false);
            switch (r.getState()) {
                case STOPPING:
                case STOPPED:
                    if (!(r.app == null || r.app.thread == null)) {
                        if (ActivityManagerDebugConfig.DEBUG_VISIBILITY) {
                            String str2 = TAG_VISIBILITY;
                            Slog.v(str2, "Scheduling invisibility: " + r);
                        }
                        this.mService.getLifecycleManager().scheduleTransaction(r.app.thread, (IBinder) r.appToken, (ClientTransactionItem) WindowVisibilityItem.obtain(false));
                        if (r.mWindowContainerController != null) {
                            r.mWindowContainerController.notifyAppStopped();
                        }
                    }
                    r.supportsEnterPipOnTaskSwitch = false;
                    break;
                case INITIALIZING:
                case RESUMED:
                case PAUSING:
                case PAUSED:
                    addToStopping(r, true, canEnterPictureInPicture);
                    break;
            }
        } catch (Exception e) {
            Slog.w(ActivityManagerService.TAG, "Exception thrown making hidden: " + r.intent.getComponent(), e);
        }
    }

    private boolean updateBehindFullscreen(boolean stackInvisible, boolean behindFullscreenActivity, ActivityRecord r) {
        if (!r.fullscreen) {
            return behindFullscreenActivity;
        }
        if (ActivityManagerDebugConfig.DEBUG_VISIBILITY) {
            String str = TAG_VISIBILITY;
            Slog.v(str, "Fullscreen: at " + r + " stackInvisible=" + stackInvisible + " behindFullscreenActivity=" + behindFullscreenActivity);
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public void convertActivityToTranslucent(ActivityRecord r) {
        this.mTranslucentActivityWaiting = r;
        this.mUndrawnActivitiesBelowTopTranslucent.clear();
        this.mHandler.sendEmptyMessageDelayed(106, TRANSLUCENT_CONVERSION_TIMEOUT);
    }

    /* access modifiers changed from: package-private */
    public void clearOtherAppTimeTrackers(AppTimeTracker except) {
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            ArrayList<ActivityRecord> activities = this.mTaskHistory.get(taskNdx).mActivities;
            for (int activityNdx = activities.size() - 1; activityNdx >= 0; activityNdx--) {
                ActivityRecord r = activities.get(activityNdx);
                if (r.appTimeTracker != except) {
                    r.appTimeTracker = null;
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void notifyActivityDrawnLocked(ActivityRecord r) {
        if (r == null || (this.mUndrawnActivitiesBelowTopTranslucent.remove(r) && this.mUndrawnActivitiesBelowTopTranslucent.isEmpty())) {
            ActivityRecord waitingActivity = this.mTranslucentActivityWaiting;
            this.mTranslucentActivityWaiting = null;
            this.mUndrawnActivitiesBelowTopTranslucent.clear();
            this.mHandler.removeMessages(106);
            if (waitingActivity != null) {
                boolean z = false;
                this.mWindowManager.setWindowOpaque(waitingActivity.appToken, false);
                if (waitingActivity.app != null && waitingActivity.app.thread != null) {
                    try {
                        IApplicationThread iApplicationThread = waitingActivity.app.thread;
                        IApplicationToken.Stub stub = waitingActivity.appToken;
                        if (r != null) {
                            z = true;
                        }
                        iApplicationThread.scheduleTranslucentConversionComplete(stub, z);
                    } catch (RemoteException e) {
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void cancelInitializingActivities() {
        boolean z;
        ActivityRecord topActivity = topRunningActivityLocked();
        boolean aboveTop = true;
        boolean behindFullscreenActivity = false;
        if (!shouldBeVisible(null)) {
            aboveTop = false;
            behindFullscreenActivity = true;
        }
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            ArrayList<ActivityRecord> activities = this.mTaskHistory.get(taskNdx).mActivities;
            for (int activityNdx = activities.size() - 1; activityNdx >= 0; activityNdx--) {
                ActivityRecord r = activities.get(activityNdx);
                if (aboveTop) {
                    if (r == topActivity) {
                        aboveTop = false;
                    }
                    z = r.fullscreen;
                } else {
                    r.removeOrphanedStartingWindow(behindFullscreenActivity);
                    z = r.fullscreen;
                }
                behindFullscreenActivity |= z;
            }
        }
    }

    /* access modifiers changed from: package-private */
    @GuardedBy("mService")
    public boolean resumeTopActivityUncheckedLocked(ActivityRecord prev, ActivityOptions options) {
        if (this.mStackSupervisor.inResumeTopActivity) {
            Flog.i(101, "It is now in resume top activity");
            return false;
        }
        try {
            this.mStackSupervisor.inResumeTopActivity = true;
            boolean result = resumeTopActivityInnerLocked(prev, options);
            ActivityRecord next = topRunningActivityLocked(true);
            if (next == null || !next.canTurnScreenOn()) {
                checkReadyForSleep();
            }
            return result;
        } finally {
            this.mStackSupervisor.inResumeTopActivity = false;
        }
    }

    /* access modifiers changed from: protected */
    public ActivityRecord getResumedActivity() {
        return this.mResumedActivity;
    }

    private void setResumedActivity(ActivityRecord r, String reason) {
        if (this.mResumedActivity != r) {
            if (ActivityManagerDebugConfig.DEBUG_STACK) {
                Slog.d(ActivityManagerService.TAG, "setResumedActivity stack:" + this + " + from: " + this.mResumedActivity + " to:" + r + " reason:" + reason);
            }
            this.mResumedActivity = r;
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v2, resolved type: com.android.server.am.ActivityRecord} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v54, resolved type: com.android.server.am.ActivityRecord} */
    /* JADX WARNING: type inference failed for: r4v55 */
    /* JADX WARNING: Code restructure failed: missing block: B:218:0x04b8, code lost:
        if (r21 == false) goto L_0x04c3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x004f, code lost:
        if (com.android.server.am.ActivityManagerService.isInCallActivity(r12) != false) goto L_0x0051;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:264:0x05b6, code lost:
        return true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:285:0x0606, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:286:0x0607, code lost:
        r10 = r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:288:0x060b, code lost:
        r22 = r4;
        r10 = r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:314:?, code lost:
        r0 = new java.lang.StringBuilder();
        r1 = r7.mStackSupervisor;
        r0.append(r1.mActivityLaunchTrack);
        r0.append(" resumeTopComplete");
        r1.mActivityLaunchTrack = r0.toString();
        r12.completeResumeLocked();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:316:0x06ef, code lost:
        if (android.util.Jlog.isUBMEnable() == false) goto L_0x075a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:317:0x06f1, code lost:
        r1 = new java.lang.StringBuilder();
        r1.append("AS#");
        r1.append(r12.intent.getComponent().flattenToShortString());
        r1.append("(");
        r1.append(r12.app.pid);
        r1.append(",");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:318:0x071b, code lost:
        if (r8 == null) goto L_0x072d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:320:0x071f, code lost:
        if (r8.intent != null) goto L_0x0722;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:321:0x0722, code lost:
        r2 = r8.intent.getComponent().flattenToShortString();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:322:0x072d, code lost:
        r2 = "null";
     */
    /* JADX WARNING: Code restructure failed: missing block: B:323:0x0730, code lost:
        r1.append(r2);
        r1.append(",");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:324:0x0738, code lost:
        if (r8 == null) goto L_0x0748;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:326:0x073c, code lost:
        if (r8.app != null) goto L_0x073f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:327:0x073f, code lost:
        r2 = java.lang.Integer.valueOf(r8.app.pid);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:328:0x0748, code lost:
        r2 = "unknow";
     */
    /* JADX WARNING: Code restructure failed: missing block: B:329:0x074b, code lost:
        r1.append(r2);
        r1.append(")");
        android.util.Jlog.d(273, r1.toString());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:330:0x075a, code lost:
        r10 = r23;
        r1 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:331:0x0760, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:332:0x0761, code lost:
        android.util.Slog.w(com.android.server.am.ActivityManagerService.TAG, "Exception thrown during resume of " + r12, r0);
        r10 = r23;
        requestFinishActivityLocked(r12.appToken, 0, null, "resume-exception", true);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:333:0x0787, code lost:
        if (com.android.server.am.ActivityManagerDebugConfig.DEBUG_STACK != false) goto L_0x0789;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:334:0x0789, code lost:
        r7.mStackSupervisor.validateTopActivitiesLocked();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:336:0x078f, code lost:
        return true;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Multi-variable type inference failed */
    /* JADX WARNING: Removed duplicated region for block: B:285:0x0606 A[ExcHandler: all (th java.lang.Throwable), Splitter:B:268:0x05c5] */
    /* JADX WARNING: Removed duplicated region for block: B:293:0x0618 A[SYNTHETIC, Splitter:B:293:0x0618] */
    /* JADX WARNING: Removed duplicated region for block: B:302:0x068b  */
    /* JADX WARNING: Removed duplicated region for block: B:308:0x06b7  */
    /* JADX WARNING: Removed duplicated region for block: B:346:0x07a1 A[Catch:{ all -> 0x080d, all -> 0x0811 }] */
    /* JADX WARNING: Removed duplicated region for block: B:349:0x07c7 A[Catch:{ all -> 0x080d, all -> 0x0811 }] */
    /* JADX WARNING: Removed duplicated region for block: B:352:0x07e9 A[Catch:{ all -> 0x080d, all -> 0x0811 }] */
    /* JADX WARNING: Removed duplicated region for block: B:353:0x07ed A[ADDED_TO_REGION, Catch:{ all -> 0x080d, all -> 0x0811 }] */
    /* JADX WARNING: Removed duplicated region for block: B:359:0x0805 A[Catch:{ all -> 0x080d, all -> 0x0811 }] */
    @GuardedBy("mService")
    private boolean resumeTopActivityInnerLocked(ActivityRecord prev, ActivityOptions options) {
        ProcessRecord processRecord;
        boolean z;
        boolean notUpdated;
        ActivityStack lastStack;
        ActivityStack lastStack2;
        int i;
        ActivityRecord activityRecord = prev;
        ActivityOptions activityOptions = options;
        if (this.mService.mBooting || this.mService.mBooted) {
            ActivityRecord next = topRunningActivityLocked(true);
            boolean hasRunningActivity = next != null;
            if (hasRunningActivity && !isAttached()) {
                return false;
            }
            if (HwPCUtils.enabledInPad() && HwPCUtils.isPcCastModeInServer() && hasRunningActivity && next.task != null) {
                ActivityManagerService activityManagerService = this.mService;
                if (!ActivityManagerService.isTimerAlertActivity(next)) {
                    ActivityManagerService activityManagerService2 = this.mService;
                }
                next.task.activityResumedInTop();
            }
            this.mStackSupervisor.cancelInitializingActivities();
            boolean userLeaving = this.mStackSupervisor.mUserLeaving;
            this.mStackSupervisor.mUserLeaving = false;
            if (!hasRunningActivity) {
                Flog.i(101, "No activities left in the stack: " + this);
                return resumeTopActivityInNextFocusableStack(activityRecord, activityOptions, "noMoreActivities");
            }
            next.delayedResume = false;
            if (this.mResumedActivity == next && next.isState(ActivityState.RESUMED) && this.mStackSupervisor.allResumedActivitiesComplete()) {
                executeAppTransition(activityOptions);
                Flog.i(101, "Top activity resumed " + next);
                if (ActivityManagerDebugConfig.DEBUG_STACK) {
                    this.mStackSupervisor.validateTopActivitiesLocked();
                }
                return false;
            } else if (shouldSleepOrShutDownActivities() && this.mLastPausedActivity == next && this.mStackSupervisor.allPausedActivitiesComplete()) {
                executeAppTransition(activityOptions);
                Flog.i(101, "Going to sleep and all paused");
                if (ActivityManagerDebugConfig.DEBUG_STACK) {
                    this.mStackSupervisor.validateTopActivitiesLocked();
                }
                return false;
            } else if (!this.mService.mUserController.hasStartedUserState(next.userId)) {
                Slog.w(ActivityManagerService.TAG, "Skipping resume of top activity " + next + ": user " + next.userId + " is stopped");
                if (ActivityManagerDebugConfig.DEBUG_STACK) {
                    this.mStackSupervisor.validateTopActivitiesLocked();
                }
                return false;
            } else {
                this.mStackSupervisor.mStoppingActivities.remove(next);
                this.mStackSupervisor.mGoingToSleepActivities.remove(next);
                next.sleeping = false;
                this.mStackSupervisor.mActivitiesWaitingForVisibleActivity.remove(next);
                if (ActivityManagerDebugConfig.DEBUG_SWITCH) {
                    Slog.v(ActivityManagerService.TAG, "Resuming " + next, new Exception());
                }
                if (!this.mStackSupervisor.allPausedActivitiesComplete()) {
                    Flog.i(101, "Skip resume: some activity is pausing.");
                    if (ActivityManagerDebugConfig.DEBUG_STACK) {
                        this.mStackSupervisor.validateTopActivitiesLocked();
                    }
                    return false;
                }
                this.mStackSupervisor.setLaunchSource(next.info.applicationInfo.uid);
                boolean lastResumedCanPip = false;
                ActivityRecord lastResumed = null;
                ActivityStack lastFocusedStack = this.mStackSupervisor.getLastStack();
                if (!(lastFocusedStack == null || lastFocusedStack == this)) {
                    lastResumed = lastFocusedStack.mResumedActivity;
                    if (userLeaving && inMultiWindowMode() && lastFocusedStack.shouldBeVisible(next)) {
                        if (ActivityManagerDebugConfig.DEBUG_USER_LEAVING) {
                            Slog.i(ActivityManagerService.TAG, "Overriding userLeaving to false next=" + next + " lastResumed=" + lastResumed);
                        }
                        userLeaving = false;
                    }
                    lastResumedCanPip = lastResumed != null && lastResumed.checkEnterPictureInPictureState("resumeTopActivity", userLeaving);
                }
                boolean userLeaving2 = userLeaving;
                ActivityRecord lastResumed2 = lastResumed;
                boolean resumeWhilePausing = (next.info.flags & 16384) != 0 && !lastResumedCanPip;
                boolean pausing = this.mStackSupervisor.pauseBackStacks(userLeaving2, next, false);
                if (this.mResumedActivity != null) {
                    Flog.i(101, "Start pausing " + this.mResumedActivity + " in stack " + this.mStackId);
                    pausing |= startPausingLocked(userLeaving2, false, next, false);
                }
                if (pausing && !resumeWhilePausing) {
                    Flog.i(101, "Skip resume: need to wait pause finished");
                    if (!(next.app == null || next.app.thread == null)) {
                        this.mService.updateLruProcessLocked(next.app, true, null);
                    }
                    if (ActivityManagerDebugConfig.DEBUG_STACK) {
                        this.mStackSupervisor.validateTopActivitiesLocked();
                    }
                    if (lastResumed2 != null) {
                        lastResumed2.setWillCloseOrEnterPip(true);
                    }
                    return true;
                } else if (this.mResumedActivity != next || !next.isState(ActivityState.RESUMED) || !this.mStackSupervisor.allResumedActivitiesComplete()) {
                    if (!shouldSleepActivities() || this.mLastNoHistoryActivity == null || this.mLastNoHistoryActivity.finishing) {
                        processRecord = null;
                        ActivityRecord activityRecord2 = lastResumed2;
                        boolean z2 = userLeaving2;
                    } else {
                        if (ActivityManagerDebugConfig.DEBUG_STATES) {
                            Slog.d(ActivityManagerService.TAG, "no-history finish of " + this.mLastNoHistoryActivity + " on new resume");
                        }
                        processRecord = null;
                        ActivityRecord activityRecord3 = lastResumed2;
                        boolean z3 = userLeaving2;
                        requestFinishActivityLocked(this.mLastNoHistoryActivity.appToken, 0, null, "resume-no-history", false);
                        this.mLastNoHistoryActivity = null;
                    }
                    if (!(activityRecord == null || activityRecord == next)) {
                        if (!this.mStackSupervisor.mActivitiesWaitingForVisibleActivity.contains(activityRecord) && next != null && !next.nowVisible) {
                            this.mStackSupervisor.mActivitiesWaitingForVisibleActivity.add(activityRecord);
                            if (ActivityManagerDebugConfig.DEBUG_SWITCH) {
                                Slog.v(ActivityManagerService.TAG, "Resuming top, waiting visible to hide: " + activityRecord);
                            }
                        } else if (activityRecord.finishing) {
                            activityRecord.setVisibility(false);
                            if (ActivityManagerDebugConfig.DEBUG_SWITCH) {
                                Slog.v(ActivityManagerService.TAG, "Not waiting for visible to hide: " + activityRecord + ", waitingVisible=" + this.mStackSupervisor.mActivitiesWaitingForVisibleActivity.contains(activityRecord) + ", nowVisible=" + next.nowVisible);
                            }
                        } else if (ActivityManagerDebugConfig.DEBUG_SWITCH) {
                            Slog.v(ActivityManagerService.TAG, "Previous already visible but still waiting to hide: " + activityRecord + ", waitingVisible=" + this.mStackSupervisor.mActivitiesWaitingForVisibleActivity.contains(activityRecord) + ", nowVisible=" + next.nowVisible);
                        }
                    }
                    try {
                        AppGlobals.getPackageManager().setPackageStoppedState(next.packageName, false, next.userId);
                    } catch (RemoteException e) {
                    } catch (IllegalArgumentException e2) {
                        Slog.w(ActivityManagerService.TAG, "Failed trying to unstop package " + next.packageName + ": " + e2);
                    }
                    boolean anim = true;
                    int i2 = 6;
                    if (activityRecord == null) {
                        if (ActivityManagerDebugConfig.DEBUG_TRANSITION) {
                            Slog.v(ActivityManagerService.TAG, "Prepare open transition: no previous");
                        }
                        if (this.mStackSupervisor.mNoAnimActivities.contains(next)) {
                            anim = false;
                            this.mWindowManager.prepareAppTransition(0, false);
                        } else {
                            this.mWindowManager.prepareAppTransition(6, false);
                        }
                    } else if (activityRecord.finishing) {
                        if (ActivityManagerDebugConfig.DEBUG_TRANSITION) {
                            Slog.v(ActivityManagerService.TAG, "Prepare close transition: prev=" + activityRecord);
                        }
                        if (this.mStackSupervisor.mNoAnimActivities.contains(activityRecord)) {
                            anim = false;
                            this.mWindowManager.prepareAppTransition(0, false);
                        } else {
                            WindowManagerService windowManagerService = this.mWindowManager;
                            if (prev.getTask() == next.getTask()) {
                                i = 7;
                            } else {
                                i = 9;
                            }
                            windowManagerService.prepareAppTransition(i, false);
                        }
                        activityRecord.setVisibility(false);
                    } else {
                        if (ActivityManagerDebugConfig.DEBUG_TRANSITION) {
                            Slog.v(ActivityManagerService.TAG, "Prepare open transition: prev=" + activityRecord);
                        }
                        if (this.mStackSupervisor.mNoAnimActivities.contains(next)) {
                            anim = false;
                            this.mWindowManager.prepareAppTransition(0, false);
                        } else {
                            WindowManagerService windowManagerService2 = this.mWindowManager;
                            if (prev.getTask() != next.getTask()) {
                                if (next.mLaunchTaskBehind) {
                                    i2 = 16;
                                } else {
                                    i2 = 8;
                                }
                            }
                            windowManagerService2.prepareAppTransition(i2, false);
                        }
                    }
                    if (anim) {
                        next.applyOptionsLocked();
                    } else {
                        next.clearOptionsLocked();
                    }
                    setKeepPortraitFR();
                    this.mStackSupervisor.mNoAnimActivities.clear();
                    ActivityStack lastStack3 = this.mStackSupervisor.getLastStack();
                    if (next.app == null || next.app.thread == null) {
                        if (!next.hasBeenLaunched) {
                            next.hasBeenLaunched = true;
                        } else {
                            next.showStartingWindow(null, false, false);
                            if (ActivityManagerDebugConfig.DEBUG_SWITCH) {
                                Slog.v(ActivityManagerService.TAG, "Restarting: " + next);
                            }
                        }
                        if (ActivityManagerDebugConfig.DEBUG_STATES) {
                            Slog.d(ActivityManagerService.TAG, "No process,need to restart " + next);
                        }
                        z = true;
                        this.mStackSupervisor.startSpecificActivityLocked(next, true, true);
                    } else {
                        if (ActivityManagerDebugConfig.DEBUG_SWITCH) {
                            Slog.v(ActivityManagerService.TAG, "Resume running: " + next + " stopped=" + next.stopped + " visible=" + next.visible);
                        }
                        boolean lastActivityTranslucent = lastStack3 != null && (lastStack3.inMultiWindowMode() || (lastStack3.mLastPausedActivity != null && !lastStack3.mLastPausedActivity.fullscreen));
                        synchronized (this.mWindowManager.getWindowManagerLock()) {
                            if (next.visible) {
                                try {
                                    if (!next.stopped) {
                                    }
                                } catch (Throwable th) {
                                    transaction = th;
                                    ActivityStack activityStack = lastStack3;
                                    throw transaction;
                                }
                            }
                            next.setVisibility(true);
                            next.startLaunchTickingLocked();
                            ActivityRecord lastResumedActivity = lastStack3 == null ? processRecord : lastStack3.mResumedActivity;
                            ActivityState lastState = next.getState();
                            this.mService.updateCpuStats();
                            if (ActivityManagerDebugConfig.DEBUG_STATES) {
                                Slog.v(ActivityManagerService.TAG, "Moving to RESUMED: " + next + " (in existing)");
                            }
                            try {
                                next.setState(ActivityState.RESUMED, "resumeTopActivityInnerLocked");
                                if (!this.mService.getActivityStartController().mCurActivityPkName.equals(next.packageName)) {
                                    Jlog.warmLaunchingAppBegin(next.packageName);
                                    Jlog.d(142, next.packageName, next.app.pid, BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
                                    LogPower.push(113, next.packageName);
                                    this.mService.getActivityStartController().mCurActivityPkName = next.packageName;
                                }
                                this.mService.updateLruProcessLocked(next.app, true, processRecord);
                                updateLRUListLocked(next);
                                this.mService.updateOomAdjLocked();
                                if (this.mStackSupervisor.isFocusedStack(this)) {
                                    notUpdated = !this.mStackSupervisor.ensureVisibilityAndConfig(next, this.mDisplayId, true, false);
                                } else {
                                    notUpdated = true;
                                }
                                if (notUpdated) {
                                    ActivityRecord nextNext = topRunningActivityLocked();
                                    if (ActivityManagerDebugConfig.DEBUG_SWITCH || ActivityManagerDebugConfig.DEBUG_STATES) {
                                        Slog.i(ActivityManagerService.TAG, "Activity config changed during resume: " + next + ", new next: " + nextNext);
                                    }
                                    if (nextNext != next) {
                                        this.mStackSupervisor.scheduleResumeTopActivities();
                                    }
                                    if (!next.visible || next.stopped) {
                                        next.setVisibility(true);
                                        if ("com.android.incallui.InCallActivity".equals(next.info.name)) {
                                            next.notifyAppResumed(next.stopped);
                                        }
                                    }
                                    next.completeResumeLocked();
                                    if (ActivityManagerDebugConfig.DEBUG_STACK) {
                                        this.mStackSupervisor.validateTopActivitiesLocked();
                                    }
                                } else {
                                    try {
                                        ClientTransaction transaction = ClientTransaction.obtain(next.app.thread, next.appToken);
                                        ArrayList<ResultInfo> a = next.results;
                                        if (a != null) {
                                            try {
                                                int N = a.size();
                                                if (!next.finishing && N > 0) {
                                                    if (ActivityManagerDebugConfig.DEBUG_RESULTS) {
                                                        boolean z4 = notUpdated;
                                                        StringBuilder sb = new StringBuilder();
                                                        lastStack2 = lastStack3;
                                                        try {
                                                            sb.append("Delivering results to ");
                                                            sb.append(next);
                                                            sb.append(": ");
                                                            sb.append(a);
                                                            Slog.v(ActivityManagerService.TAG, sb.toString());
                                                        } catch (Exception e3) {
                                                            lastStack = lastStack2;
                                                        } catch (Throwable th2) {
                                                            transaction = th2;
                                                            ActivityStack activityStack2 = lastStack2;
                                                            throw transaction;
                                                        }
                                                    } else {
                                                        lastStack2 = lastStack3;
                                                    }
                                                    transaction.addCallback(ActivityResultItem.obtain(a));
                                                    if (next.newIntents != null) {
                                                        transaction.addCallback(NewIntentItem.obtain(next.newIntents, false));
                                                    }
                                                    next.notifyAppResumed(next.stopped);
                                                    EventLog.writeEvent(EventLogTags.AM_RESUME_ACTIVITY, new Object[]{Integer.valueOf(next.userId), Integer.valueOf(System.identityHashCode(next)), Integer.valueOf(next.getTask().taskId), next.shortComponentName});
                                                    next.sleeping = false;
                                                    this.mService.getAppWarningsLocked().onResumeActivity(next);
                                                    this.mService.showAskCompatModeDialogLocked(next);
                                                    next.app.pendingUiClean = true;
                                                    next.app.forceProcessStateUpTo(this.mService.mTopProcessState);
                                                    next.clearOptionsLocked();
                                                    resumeCustomActivity(next);
                                                    if (Jlog.isPerfTest()) {
                                                        Jlog.i(3044, Intent.toPkgClsString(next.realActivity, "who"));
                                                    }
                                                    transaction.setLifecycleStateRequest(ResumeActivityItem.obtain(next.app.repProcState, this.mService.isNextTransitionForward()));
                                                    this.mService.getLifecycleManager().scheduleTransaction(transaction);
                                                    if (ActivityManagerDebugConfig.DEBUG_STATES) {
                                                        Slog.d(ActivityManagerService.TAG, "resumeTopActivityLocked: Resumed " + next);
                                                    }
                                                    try {
                                                    } catch (Throwable th3) {
                                                        transaction = th3;
                                                        ActivityStack activityStack3 = lastStack2;
                                                        throw transaction;
                                                    }
                                                }
                                            } catch (Exception e4) {
                                                lastStack = lastStack3;
                                                if (ActivityManagerDebugConfig.DEBUG_STATES) {
                                                }
                                                next.setState(lastState, "resumeTopActivityInnerLocked");
                                                if (lastResumedActivity != null) {
                                                }
                                                Slog.i(ActivityManagerService.TAG, "Restarting because process died: " + next);
                                                if (next.hasBeenLaunched) {
                                                }
                                                this.mStackSupervisor.startSpecificActivityLocked(next, true, false);
                                                if (ActivityManagerDebugConfig.DEBUG_STACK) {
                                                }
                                                return true;
                                            } catch (Throwable th4) {
                                            }
                                        }
                                        lastStack2 = lastStack3;
                                        try {
                                            if (next.newIntents != null) {
                                            }
                                            next.notifyAppResumed(next.stopped);
                                            EventLog.writeEvent(EventLogTags.AM_RESUME_ACTIVITY, new Object[]{Integer.valueOf(next.userId), Integer.valueOf(System.identityHashCode(next)), Integer.valueOf(next.getTask().taskId), next.shortComponentName});
                                            next.sleeping = false;
                                            this.mService.getAppWarningsLocked().onResumeActivity(next);
                                            this.mService.showAskCompatModeDialogLocked(next);
                                            next.app.pendingUiClean = true;
                                            next.app.forceProcessStateUpTo(this.mService.mTopProcessState);
                                            next.clearOptionsLocked();
                                            resumeCustomActivity(next);
                                            if (Jlog.isPerfTest()) {
                                            }
                                            transaction.setLifecycleStateRequest(ResumeActivityItem.obtain(next.app.repProcState, this.mService.isNextTransitionForward()));
                                            this.mService.getLifecycleManager().scheduleTransaction(transaction);
                                            if (ActivityManagerDebugConfig.DEBUG_STATES) {
                                            }
                                        } catch (Exception e5) {
                                            lastStack = lastStack2;
                                            if (ActivityManagerDebugConfig.DEBUG_STATES) {
                                            }
                                            next.setState(lastState, "resumeTopActivityInnerLocked");
                                            if (lastResumedActivity != null) {
                                            }
                                            Slog.i(ActivityManagerService.TAG, "Restarting because process died: " + next);
                                            if (next.hasBeenLaunched) {
                                            }
                                            this.mStackSupervisor.startSpecificActivityLocked(next, true, false);
                                            if (ActivityManagerDebugConfig.DEBUG_STACK) {
                                            }
                                            return true;
                                        }
                                    } catch (Exception e6) {
                                        boolean z5 = notUpdated;
                                        lastStack = lastStack3;
                                        if (ActivityManagerDebugConfig.DEBUG_STATES) {
                                            Slog.v(ActivityManagerService.TAG, "Resume failed; resetting state to " + lastState + ": " + next);
                                        }
                                        next.setState(lastState, "resumeTopActivityInnerLocked");
                                        if (lastResumedActivity != null) {
                                            lastResumedActivity.setState(ActivityState.RESUMED, "resumeTopActivityInnerLocked");
                                        }
                                        Slog.i(ActivityManagerService.TAG, "Restarting because process died: " + next);
                                        if (next.hasBeenLaunched) {
                                            next.hasBeenLaunched = true;
                                        } else if (lastStack != null && lastStack.isTopStackOnDisplay()) {
                                            next.showStartingWindow(null, false, false);
                                        }
                                        this.mStackSupervisor.startSpecificActivityLocked(next, true, false);
                                        if (ActivityManagerDebugConfig.DEBUG_STACK) {
                                            this.mStackSupervisor.validateTopActivitiesLocked();
                                        }
                                        return true;
                                    }
                                }
                            } catch (Throwable th5) {
                                transaction = th5;
                                throw transaction;
                            }
                        }
                    }
                    if (ActivityManagerDebugConfig.DEBUG_STACK) {
                        this.mStackSupervisor.validateTopActivitiesLocked();
                    }
                    return z;
                } else {
                    executeAppTransition(activityOptions);
                    if (ActivityManagerDebugConfig.DEBUG_STATES) {
                        Slog.d(ActivityManagerService.TAG, "Top activity resumed (dontWaitForPause) " + next);
                    }
                    if (ActivityManagerDebugConfig.DEBUG_STACK) {
                        this.mStackSupervisor.validateTopActivitiesLocked();
                    }
                    return true;
                }
            }
        } else {
            Flog.i(101, "It is not ready yet");
            return false;
        }
    }

    private boolean resumeTopActivityInNextFocusableStack(ActivityRecord prev, ActivityOptions options, String reason) {
        boolean z = false;
        if (adjustFocusToNextFocusableStack(reason)) {
            ActivityStack nextFocusableStack = this.mStackSupervisor.getFocusedStack();
            if (!(nextFocusableStack == null || nextFocusableStack.topRunningActivityLocked(true) == null)) {
                this.mStackSupervisor.inResumeTopActivity = false;
                Flog.i(101, "adjust focus to next focusable stack: " + nextFocusableStack);
            }
            return this.mStackSupervisor.resumeFocusedStackTopActivityLocked(nextFocusableStack, prev, null);
        }
        ActivityOptions.abort(options);
        if (ActivityManagerDebugConfig.DEBUG_STATES) {
            Slog.d(ActivityManagerService.TAG, "resumeTopActivityInNextFocusableStack: " + reason + ", go home");
        }
        if (ActivityManagerDebugConfig.DEBUG_STACK) {
            this.mStackSupervisor.validateTopActivitiesLocked();
        }
        Jlog.d(24, "JL_LAUNCHER_STARTUP");
        if (isOnHomeDisplay() && this.mStackSupervisor.resumeHomeStackTask(prev, reason)) {
            z = true;
        }
        return z;
    }

    private TaskRecord getNextTask(TaskRecord targetTask) {
        int index = this.mTaskHistory.indexOf(targetTask);
        if (index >= 0) {
            int numTasks = this.mTaskHistory.size();
            for (int i = index + 1; i < numTasks; i++) {
                TaskRecord task = this.mTaskHistory.get(i);
                if (task.userId == targetTask.userId || (this.mStackSupervisor.isCurrentProfileLocked(task.userId) && this.mStackSupervisor.isCurrentProfileLocked(targetTask.userId))) {
                    return task;
                }
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public int getAdjustedPositionForTask(TaskRecord task, int suggestedPosition, ActivityRecord starting) {
        int maxPosition = this.mTaskHistory.size();
        if ((starting != null && starting.okToShowLocked()) || (starting == null && task.okToShowLocked())) {
            return Math.min(suggestedPosition, maxPosition);
        }
        while (maxPosition > 0) {
            TaskRecord tmpTask = this.mTaskHistory.get(maxPosition - 1);
            if (!this.mStackSupervisor.isCurrentProfileLocked(tmpTask.userId) || tmpTask.topRunningActivityLocked() == null) {
                break;
            }
            maxPosition--;
        }
        return Math.min(suggestedPosition, maxPosition);
    }

    private void insertTaskAtPosition(TaskRecord task, int position) {
        if (position >= this.mTaskHistory.size()) {
            insertTaskAtTop(task, null);
        } else if (position <= 0) {
            insertTaskAtBottom(task);
        } else {
            int position2 = getAdjustedPositionForTask(task, position, null);
            this.mTaskHistory.remove(task);
            this.mTaskHistory.add(position2, task);
            this.mWindowContainerController.positionChildAt(task.getWindowContainerController(), position2);
            updateTaskMovement(task, true);
        }
    }

    private void insertTaskAtTop(TaskRecord task, ActivityRecord starting) {
        this.mTaskHistory.remove(task);
        this.mTaskHistory.add(getAdjustedPositionForTask(task, this.mTaskHistory.size(), starting), task);
        updateTaskMovement(task, true);
        this.mWindowContainerController.positionChildAtTop(task.getWindowContainerController(), true);
    }

    private void insertTaskAtBottom(TaskRecord task) {
        this.mTaskHistory.remove(task);
        this.mTaskHistory.add(getAdjustedPositionForTask(task, 0, null), task);
        updateTaskMovement(task, true);
        this.mWindowContainerController.positionChildAtBottom(task.getWindowContainerController(), true);
    }

    /* access modifiers changed from: package-private */
    public void startActivityLocked(ActivityRecord r, ActivityRecord focusedTopActivity, boolean newTask, boolean keepCurTransition, ActivityOptions options) {
        ActivityRecord activityRecord = r;
        ActivityRecord activityRecord2 = focusedTopActivity;
        boolean z = newTask;
        boolean z2 = keepCurTransition;
        ActivityOptions activityOptions = options;
        TaskRecord rTask = r.getTask();
        int taskId = rTask.taskId;
        if (!activityRecord.mLaunchTaskBehind && (taskForIdLocked(taskId) == null || z)) {
            insertTaskAtTop(rTask, activityRecord);
        }
        TaskRecord task = null;
        if (!z) {
            boolean startIt = true;
            int taskNdx = this.mTaskHistory.size() - 1;
            while (true) {
                if (taskNdx < 0) {
                    break;
                }
                task = this.mTaskHistory.get(taskNdx);
                if (task.getTopActivity() != null) {
                    if (task == rTask) {
                        if (!startIt) {
                            if (ActivityManagerDebugConfig.DEBUG_ADD_REMOVE) {
                                Slog.i(ActivityManagerService.TAG, "Adding activity " + activityRecord + " to task " + task, new RuntimeException("here").fillInStackTrace());
                            }
                            activityRecord.createWindowContainer(activityRecord.info.navigationHide);
                            ActivityOptions.abort(options);
                            return;
                        }
                    } else if (task.numFullscreen > 0) {
                        Flog.i(101, "starting r: " + activityRecord + " blocked by task: " + task);
                        startIt = false;
                    }
                }
                taskNdx--;
            }
        }
        TaskRecord activityTask = r.getTask();
        if (task == activityTask && this.mTaskHistory.indexOf(task) != this.mTaskHistory.size() - 1) {
            this.mStackSupervisor.mUserLeaving = false;
            if (ActivityManagerDebugConfig.DEBUG_USER_LEAVING) {
                Slog.v(ActivityManagerService.TAG, "startActivity() behind front, mUserLeaving=false");
            }
        }
        TaskRecord task2 = activityTask;
        if (ActivityManagerDebugConfig.DEBUG_ADD_REMOVE) {
            Slog.i(ActivityManagerService.TAG, "Adding activity " + activityRecord + " to stack to task " + task2, new RuntimeException("here").fillInStackTrace());
        }
        if (r.getWindowContainerController() == null) {
            activityRecord.createWindowContainer(activityRecord.info.navigationHide);
        }
        task2.setFrontOfTask();
        if (!isHomeOrRecentsStack() || numActivities() > 0) {
            if (ActivityManagerDebugConfig.DEBUG_TRANSITION) {
                Slog.v(ActivityManagerService.TAG, "Prepare open transition: starting " + activityRecord);
            }
            if ((activityRecord.intent.getFlags() & 65536) != 0) {
                this.mWindowManager.prepareAppTransition(0, z2);
                this.mStackSupervisor.mNoAnimActivities.add(activityRecord);
            } else {
                int transit = 6;
                if (z) {
                    if (activityRecord.mLaunchTaskBehind) {
                        transit = 16;
                    } else {
                        if (canEnterPipOnTaskSwitch(activityRecord2, null, activityRecord, activityOptions)) {
                            activityRecord2.supportsEnterPipOnTaskSwitch = true;
                        }
                        transit = 8;
                    }
                }
                this.mWindowManager.prepareAppTransition(transit, z2);
                this.mStackSupervisor.mNoAnimActivities.remove(activityRecord);
            }
            boolean doShow = true;
            if (z) {
                if ((activityRecord.intent.getFlags() & DumpState.DUMP_COMPILER_STATS) != 0) {
                    resetTaskIfNeededLocked(activityRecord, activityRecord);
                    doShow = topRunningNonDelayedActivityLocked(null) == activityRecord;
                }
            } else if (activityOptions != null && options.getAnimationType() == 5) {
                doShow = false;
            }
            if ((activityRecord.intent.getHwFlags() & 8192) != 0) {
                doShow = false;
            }
            Flog.i(301, "startActivityLocked doShow= " + doShow + " mLaunchTaskBehind= " + activityRecord.mLaunchTaskBehind);
            if (activityRecord.mLaunchTaskBehind) {
                activityRecord.setVisibility(true);
                ensureActivitiesVisibleLocked(null, 0, false);
            } else if (doShow) {
                TaskRecord prevTask = r.getTask();
                ActivityRecord prev = prevTask.topRunningActivityWithStartingWindowLocked();
                if (prev != null) {
                    if (prev.getTask() != prevTask) {
                        prev = null;
                    } else if (prev.nowVisible) {
                        prev = null;
                    }
                }
                if (isSplitActivity(activityRecord.intent)) {
                    this.mWindowManager.setSplittable(true);
                } else if (this.mWindowManager.isSplitMode()) {
                    this.mWindowManager.setSplittable(false);
                }
                activityRecord.showStartingWindow(prev, z, isTaskSwitch(r, focusedTopActivity));
            }
        } else {
            ActivityOptions.abort(options);
        }
    }

    private boolean canEnterPipOnTaskSwitch(ActivityRecord pipCandidate, TaskRecord toFrontTask, ActivityRecord toFrontActivity, ActivityOptions opts) {
        if ((opts != null && opts.disallowEnterPictureInPictureWhileLaunching()) || pipCandidate == null || pipCandidate.inPinnedWindowingMode()) {
            return false;
        }
        ActivityStack targetStack = toFrontTask != null ? toFrontTask.getStack() : toFrontActivity.getStack();
        if (targetStack == null || !targetStack.isActivityTypeAssistant()) {
            return true;
        }
        return false;
    }

    private boolean isTaskSwitch(ActivityRecord r, ActivityRecord topFocusedActivity) {
        return (topFocusedActivity == null || r.getTask() == topFocusedActivity.getTask()) ? false : true;
    }

    private ActivityOptions resetTargetTaskIfNeededLocked(TaskRecord task, boolean forceReset) {
        int numActivities;
        int i;
        int end;
        ActivityRecord target;
        TaskRecord targetTask;
        int start;
        TaskRecord taskRecord = task;
        ArrayList<ActivityRecord> activities = taskRecord.mActivities;
        int numActivities2 = activities.size();
        int rootActivityNdx = task.findEffectiveRootIndex();
        int i2 = numActivities2 - 1;
        ActivityOptions topOptions = null;
        int replyChainEnd = -1;
        boolean canMoveOptions = true;
        while (true) {
            int i3 = i2;
            if (i3 <= rootActivityNdx) {
                break;
            }
            ActivityRecord target2 = activities.get(i3);
            if (target2.frontOfTask) {
                int i4 = numActivities2;
                break;
            }
            int flags = target2.info.flags;
            boolean finishOnTaskLaunch = (flags & 2) != 0;
            boolean allowTaskReparenting = (flags & 64) != 0;
            boolean clearWhenTaskReset = (target2.intent.getFlags() & DumpState.DUMP_FROZEN) != 0;
            if (finishOnTaskLaunch || clearWhenTaskReset || target2.resultTo == null) {
                if (finishOnTaskLaunch || clearWhenTaskReset || !allowTaskReparenting || target2.taskAffinity == null || target2.taskAffinity.equals(taskRecord.affinity)) {
                    numActivities = numActivities2;
                    ActivityRecord activityRecord = target2;
                    if (forceReset || finishOnTaskLaunch || clearWhenTaskReset) {
                        if (clearWhenTaskReset) {
                            end = activities.size() - 1;
                        } else if (replyChainEnd < 0) {
                            end = i3;
                        } else {
                            end = replyChainEnd;
                        }
                        boolean noOptions = canMoveOptions;
                        ActivityOptions topOptions2 = topOptions;
                        int end2 = end;
                        int end3 = i3;
                        while (true) {
                            int srcPos = end3;
                            if (srcPos > end2) {
                                break;
                            }
                            ActivityRecord p = activities.get(srcPos);
                            if (!p.finishing) {
                                canMoveOptions = false;
                                if (noOptions && topOptions2 == null) {
                                    topOptions2 = p.takeOptionsLocked();
                                    if (topOptions2 != null) {
                                        noOptions = false;
                                    }
                                }
                                boolean noOptions2 = noOptions;
                                ActivityOptions topOptions3 = topOptions2;
                                Flog.i(105, "resetTaskIntendedTask: calling finishActivity on " + p);
                                ActivityRecord activityRecord2 = p;
                                int srcPos2 = srcPos;
                                if (finishActivityLocked(p, 0, null, "reset-task", false)) {
                                    end2--;
                                    srcPos = srcPos2 - 1;
                                    topOptions2 = topOptions3;
                                    noOptions = noOptions2;
                                } else {
                                    topOptions2 = topOptions3;
                                    noOptions = noOptions2;
                                    srcPos = srcPos2;
                                }
                            }
                            end3 = srcPos + 1;
                        }
                        replyChainEnd = -1;
                        topOptions = topOptions2;
                    } else {
                        i = -1;
                    }
                } else {
                    ActivityRecord bottom = (this.mTaskHistory.isEmpty() || this.mTaskHistory.get(0).mActivities.isEmpty()) ? null : this.mTaskHistory.get(0).mActivities.get(0);
                    if (bottom == null || target2.taskAffinity == null || !target2.taskAffinity.equals(bottom.getTask().affinity)) {
                        ActivityRecord activityRecord3 = bottom;
                        int i5 = flags;
                        numActivities = numActivities2;
                        target = target2;
                        targetTask = createTaskRecord(this.mStackSupervisor.getNextTaskIdForUserLocked(target2.userId), target2.info, null, null, null, false);
                        targetTask.affinityIntent = target.intent;
                        Flog.i(105, "ResetTask:Start pushing activity " + target + " out to new task " + targetTask);
                    } else {
                        targetTask = bottom.getTask();
                        Flog.i(105, "ResetTask:Start pushing activity " + target2 + " out to bottom task " + targetTask);
                        ActivityRecord activityRecord4 = bottom;
                        int i6 = flags;
                        numActivities = numActivities2;
                        target = target2;
                    }
                    boolean noOptions3 = canMoveOptions;
                    int start2 = replyChainEnd < 0 ? i3 : replyChainEnd;
                    boolean noOptions4 = noOptions3;
                    int srcPos3 = start2;
                    while (srcPos3 >= i3) {
                        ActivityRecord p2 = activities.get(srcPos3);
                        if (p2.finishing) {
                            start = start2;
                        } else {
                            if (noOptions4 && topOptions == null) {
                                topOptions = p2.takeOptionsLocked();
                                if (topOptions != null) {
                                    noOptions4 = false;
                                }
                            }
                            if (ActivityManagerDebugConfig.DEBUG_ADD_REMOVE) {
                                StringBuilder sb = new StringBuilder();
                                start = start2;
                                sb.append("Removing activity ");
                                sb.append(p2);
                                sb.append(" from task=");
                                sb.append(taskRecord);
                                sb.append(" adding to task=");
                                sb.append(targetTask);
                                sb.append(" Callers=");
                                sb.append(Debug.getCallers(4));
                                Slog.i(ActivityManagerService.TAG, sb.toString());
                            } else {
                                start = start2;
                            }
                            Flog.i(105, "ResetTask:Pushing next activity " + p2 + " out to target's task " + target);
                            p2.reparent(targetTask, 0, "resetTargetTaskIfNeeded");
                            canMoveOptions = false;
                        }
                        srcPos3--;
                        start2 = start;
                    }
                    this.mWindowContainerController.positionChildAtBottom(targetTask.getWindowContainerController(), false);
                    i = -1;
                }
                replyChainEnd = i;
            } else {
                Flog.i(105, "ResetTask:Keeping the end of the reply chain, target= " + target2.task + " targetI=" + i3 + " replyChainEnd=" + replyChainEnd);
                if (replyChainEnd < 0) {
                    replyChainEnd = i3;
                }
                numActivities = numActivities2;
            }
            i2 = i3 - 1;
            numActivities2 = numActivities;
        }
        return topOptions;
    }

    private int resetAffinityTaskIfNeededLocked(TaskRecord affinityTask, TaskRecord task, boolean topTaskIsHigher, boolean forceReset, int taskInsertionPoint) {
        int rootActivityNdx;
        int numActivities;
        String taskAffinity;
        int taskId;
        int taskInsertionPoint2;
        ActivityStack activityStack;
        int taskId2;
        String taskAffinity2;
        int numActivities2;
        int rootActivityNdx2;
        int taskInsertionPoint3;
        TaskRecord taskRecord = affinityTask;
        TaskRecord taskRecord2 = task;
        int taskId3 = taskRecord2.taskId;
        String taskAffinity3 = taskRecord2.affinity;
        ArrayList<ActivityRecord> activities = taskRecord.mActivities;
        int numActivities3 = activities.size();
        int rootActivityNdx3 = affinityTask.findEffectiveRootIndex();
        int i = numActivities3 - 1;
        int replyChainEnd = -1;
        int taskInsertionPoint4 = taskInsertionPoint;
        while (true) {
            if (i <= rootActivityNdx3) {
                int i2 = taskId3;
                String str = taskAffinity3;
                int i3 = numActivities3;
                int i4 = rootActivityNdx3;
                break;
            }
            ActivityRecord target = activities.get(i);
            if (target.frontOfTask) {
                int i5 = taskId3;
                String str2 = taskAffinity3;
                int i6 = numActivities3;
                int i7 = rootActivityNdx3;
                break;
            }
            int flags = target.info.flags;
            boolean allowTaskReparenting = false;
            boolean finishOnTaskLaunch = (flags & 2) != 0;
            if ((flags & 64) != 0) {
                allowTaskReparenting = true;
            }
            if (target.resultTo != null) {
                Flog.i(105, "ResetTaskAffinity:Keeping the end of the reply chain, target= " + target.task + " targetI=" + i + " replyChainEnd=" + replyChainEnd);
                if (replyChainEnd < 0) {
                    replyChainEnd = i;
                }
                taskId = taskId3;
                taskAffinity = taskAffinity3;
                numActivities = numActivities3;
                rootActivityNdx = rootActivityNdx3;
            } else if (!topTaskIsHigher || !allowTaskReparenting || taskAffinity3 == null || !taskAffinity3.equals(target.taskAffinity)) {
                taskId = taskId3;
                taskAffinity = taskAffinity3;
                numActivities = numActivities3;
                rootActivityNdx = rootActivityNdx3;
            } else {
                if (forceReset) {
                    activityStack = this;
                    taskId2 = taskId3;
                    taskAffinity2 = taskAffinity3;
                    numActivities2 = numActivities3;
                    rootActivityNdx2 = rootActivityNdx3;
                } else if (finishOnTaskLaunch) {
                    activityStack = this;
                    taskId2 = taskId3;
                    taskAffinity2 = taskAffinity3;
                    numActivities2 = numActivities3;
                    rootActivityNdx2 = rootActivityNdx3;
                } else {
                    if (taskInsertionPoint4 < 0) {
                        taskId = taskId3;
                        taskInsertionPoint4 = taskRecord2.mActivities.size();
                    } else {
                        taskId = taskId3;
                    }
                    int start = replyChainEnd >= 0 ? replyChainEnd : i;
                    if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                        taskAffinity = taskAffinity3;
                        numActivities = numActivities3;
                        StringBuilder sb = new StringBuilder();
                        rootActivityNdx = rootActivityNdx3;
                        sb.append("Reparenting from task=");
                        sb.append(taskRecord);
                        sb.append(":");
                        sb.append(start);
                        sb.append("-");
                        sb.append(i);
                        sb.append(" to task=");
                        sb.append(taskRecord2);
                        sb.append(":");
                        sb.append(taskInsertionPoint4);
                        Slog.v(ActivityManagerService.TAG, sb.toString());
                    } else {
                        taskAffinity = taskAffinity3;
                        numActivities = numActivities3;
                        rootActivityNdx = rootActivityNdx3;
                    }
                    int srcPos = start;
                    while (srcPos >= i) {
                        ActivityRecord p = activities.get(srcPos);
                        p.reparent(taskRecord2, taskInsertionPoint4, "resetAffinityTaskIfNeededLocked");
                        if (ActivityManagerDebugConfig.DEBUG_ADD_REMOVE) {
                            StringBuilder sb2 = new StringBuilder();
                            taskInsertionPoint3 = taskInsertionPoint4;
                            sb2.append("Removing and adding activity ");
                            sb2.append(p);
                            sb2.append(" to stack at ");
                            sb2.append(taskRecord2);
                            sb2.append(" callers=");
                            sb2.append(Debug.getCallers(3));
                            Slog.i(ActivityManagerService.TAG, sb2.toString());
                        } else {
                            taskInsertionPoint3 = taskInsertionPoint4;
                        }
                        Flog.i(105, "ResetTaskAffinity:Pulling activity " + p + " from " + srcPos + " in to resetting task " + taskRecord2);
                        srcPos += -1;
                        taskInsertionPoint4 = taskInsertionPoint3;
                        TaskRecord taskRecord3 = affinityTask;
                    }
                    taskInsertionPoint2 = taskInsertionPoint4;
                    this.mWindowContainerController.positionChildAtTop(task.getWindowContainerController(), true);
                    if (target.info.launchMode == 1) {
                        ArrayList<ActivityRecord> taskActivities = taskRecord2.mActivities;
                        if (taskActivities.indexOf(target) > 0) {
                            ActivityRecord p2 = taskActivities.get(targetNdx - 1);
                            if (p2.intent.getComponent().equals(target.intent.getComponent())) {
                                Flog.i(105, "ResetTaskAffinity:Drop singleTop activity " + p2 + " for target " + target);
                                finishActivityLocked(p2, 0, null, "replace", false);
                            }
                        }
                    }
                    replyChainEnd = -1;
                    taskInsertionPoint4 = taskInsertionPoint2;
                }
                int start2 = replyChainEnd >= 0 ? replyChainEnd : i;
                if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                    Slog.v(ActivityManagerService.TAG, "Finishing task at index " + start2 + " to " + i);
                }
                for (int srcPos2 = start2; srcPos2 >= i; srcPos2--) {
                    ActivityRecord p3 = activities.get(srcPos2);
                    if (!p3.finishing) {
                        Flog.i(105, "ResetTaskAffinity:finishActivity pos:  " + srcPos2 + " acitivity: " + p3);
                        activityStack.finishActivityLocked(p3, 0, null, "move-affinity", false);
                    }
                }
                taskInsertionPoint2 = taskInsertionPoint4;
                replyChainEnd = -1;
                taskInsertionPoint4 = taskInsertionPoint2;
            }
            i--;
            taskId3 = taskId;
            taskAffinity3 = taskAffinity;
            numActivities3 = numActivities;
            rootActivityNdx3 = rootActivityNdx;
            taskRecord = affinityTask;
            taskRecord2 = task;
        }
        return taskInsertionPoint4;
    }

    /* access modifiers changed from: package-private */
    public final ActivityRecord resetTaskIfNeededLocked(ActivityRecord taskTop, ActivityRecord newActivity) {
        int taskNdx;
        boolean forceReset = (newActivity.info.flags & 4) != 0;
        TaskRecord task = taskTop.getTask();
        int i = this.mTaskHistory.size() - 1;
        boolean taskFound = false;
        ActivityOptions topOptions = null;
        int reparentInsertionPoint = -1;
        while (true) {
            int i2 = i;
            if (i2 < 0) {
                break;
            }
            TaskRecord targetTask = this.mTaskHistory.get(i2);
            if (targetTask == task) {
                topOptions = resetTargetTaskIfNeededLocked(task, forceReset);
                taskFound = true;
            } else {
                reparentInsertionPoint = resetAffinityTaskIfNeededLocked(targetTask, task, taskFound, forceReset, reparentInsertionPoint);
            }
            i = i2 - 1;
        }
        int taskNdx2 = this.mTaskHistory.indexOf(task);
        if (taskNdx2 >= 0) {
            while (true) {
                taskNdx = taskNdx2 - 1;
                taskTop = this.mTaskHistory.get(taskNdx2).getTopActivity();
                if (taskTop != null || taskNdx < 0) {
                    int i3 = taskNdx;
                } else {
                    taskNdx2 = taskNdx;
                }
            }
            int i32 = taskNdx;
        }
        if (topOptions != null) {
            if (taskTop != null) {
                taskTop.updateOptionsLocked(topOptions);
            } else {
                topOptions.abort();
            }
        }
        return taskTop;
    }

    /* access modifiers changed from: package-private */
    public void sendActivityResultLocked(int callingUid, ActivityRecord r, String resultWho, int requestCode, int resultCode, Intent data) {
        if (callingUid > 0) {
            this.mService.grantUriPermissionFromIntentLocked(callingUid, r.packageName, data, r.getUriPermissionsLocked(), r.userId);
        }
        if (ActivityManagerDebugConfig.DEBUG_RESULTS) {
            Slog.v(ActivityManagerService.TAG, "Send activity result to " + r + " : who=" + resultWho + " req=" + requestCode + " res=" + resultCode + " data=" + data);
        }
        if (!(this.mResumedActivity != r || r.app == null || r.app.thread == null)) {
            try {
                ArrayList<ResultInfo> list = new ArrayList<>();
                list.add(new ResultInfo(resultWho, requestCode, resultCode, data));
                this.mService.getLifecycleManager().scheduleTransaction(r.app.thread, (IBinder) r.appToken, (ClientTransactionItem) ActivityResultItem.obtain(list));
                return;
            } catch (Exception e) {
                Slog.w(ActivityManagerService.TAG, "Exception thrown sending result to " + r, e);
            }
        }
        r.addResultLocked(null, resultWho, requestCode, resultCode, data);
    }

    private boolean isATopFinishingTask(TaskRecord task) {
        for (int i = this.mTaskHistory.size() - 1; i >= 0; i--) {
            TaskRecord current = this.mTaskHistory.get(i);
            if (current.topRunningActivityLocked() != null) {
                return false;
            }
            if (current == task) {
                return true;
            }
        }
        return false;
    }

    private void adjustFocusedActivityStack(ActivityRecord r, String reason) {
        if (r != null && this.mStackSupervisor.isFocusedStack(this) && (this.mResumedActivity == r || this.mResumedActivity == null)) {
            ActivityRecord next = topRunningActivityLocked();
            String myReason = reason + " adjustFocus";
            if (next == r) {
                this.mStackSupervisor.moveFocusableActivityStackToFrontLocked(this.mStackSupervisor.topRunningActivityLocked(), myReason);
            } else if (next != null && isFocusable()) {
            } else {
                if (r.getTask() == null) {
                    throw new IllegalStateException("activity no longer associated with task:" + r);
                } else if (!adjustFocusToNextFocusableStack(myReason)) {
                    this.mStackSupervisor.moveHomeStackTaskToTop(myReason);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean adjustFocusToNextFocusableStack(String reason) {
        return adjustFocusToNextFocusableStack(reason, false);
    }

    private boolean adjustFocusToNextFocusableStack(String reason, boolean allowFocusSelf) {
        ActivityStack stack = this.mStackSupervisor.getNextFocusableStackLocked(this, !allowFocusSelf);
        String myReason = reason + " adjustFocusToNextFocusableStack";
        if (stack == null) {
            return false;
        }
        ActivityRecord top = stack.topRunningActivityLocked();
        if (checkAdjustToPrimarySplitScreenStack(stack, top)) {
            Slog.w(ActivityManagerService.TAG, "adjustFocusToNextFocusableStack to primary split screen stack");
            return true;
        } else if (stack.isActivityTypeHome() && (top == null || !top.visible)) {
            return this.mStackSupervisor.moveHomeStackTaskToTop(reason);
        } else {
            stack.moveToFront(myReason);
            return true;
        }
    }

    private boolean checkAdjustToPrimarySplitScreenStack(ActivityStack targetStack, ActivityRecord targetActivityRecord) {
        if (getWindowingMode() == 4 && getActivityType() == 1) {
            if (((targetActivityRecord != null && targetActivityRecord.toString().contains("splitscreen.SplitScreenAppActivity")) || targetStack.getActivityType() == 3) && primarySplitScreenStackToFullScreen(null)) {
                return true;
            }
            if (targetStack.getWindowingMode() == 3 && primarySplitScreenStackToFullScreen(targetStack)) {
                return true;
            }
        }
        return false;
    }

    private boolean primarySplitScreenStackToFullScreen(ActivityStack topPrimaryStack) {
        if (topPrimaryStack == null) {
            topPrimaryStack = getDisplay().getTopStackInWindowingMode(3);
        }
        if (topPrimaryStack == null) {
            return false;
        }
        this.mWindowManager.mShouldResetTime = true;
        this.mWindowManager.startFreezingScreen(0, 0);
        topPrimaryStack.moveToFront("adjustFocusedToSplitPrimaryStack");
        topPrimaryStack.setWindowingMode(1);
        this.mWindowManager.stopFreezingScreen();
        return true;
    }

    /* access modifiers changed from: package-private */
    public final void stopActivityLocked(ActivityRecord r) {
        if (ActivityManagerDebugConfig.DEBUG_SWITCH) {
            Slog.d(ActivityManagerService.TAG, "Stopping: " + r);
        }
        if (!((r.intent.getFlags() & 1073741824) == 0 && (r.info.flags & 128) == 0) && !r.finishing) {
            if (!shouldSleepActivities()) {
                if (ActivityManagerDebugConfig.DEBUG_STATES) {
                    Slog.d(ActivityManagerService.TAG, "no-history finish of " + r);
                }
                if (requestFinishActivityLocked(r.appToken, 0, null, "stop-no-history", false)) {
                    r.resumeKeyDispatchingLocked();
                    return;
                }
            } else if (ActivityManagerDebugConfig.DEBUG_STATES) {
                Slog.d(ActivityManagerService.TAG, "Not finishing noHistory " + r + " on stop because we're just sleeping");
            }
        }
        if (!(r.app == null || r.app.thread == null)) {
            adjustFocusedActivityStack(r, "stopActivity");
            r.resumeKeyDispatchingLocked();
            try {
                r.stopped = false;
                if (ActivityManagerDebugConfig.DEBUG_STATES) {
                    Slog.v(ActivityManagerService.TAG, "Moving to STOPPING: " + r + " (stop requested)");
                }
                r.setState(ActivityState.STOPPING, "stopActivityLocked");
                if (ActivityManagerDebugConfig.DEBUG_VISIBILITY) {
                    String str = TAG_VISIBILITY;
                    Slog.v(str, "Stopping visible=" + r.visible + " for " + r);
                }
                if (!r.visible) {
                    r.setVisible(false);
                }
                EventLogTags.writeAmStopActivity(r.userId, System.identityHashCode(r), r.shortComponentName);
                this.mService.getLifecycleManager().scheduleTransaction(r.app.thread, (IBinder) r.appToken, (ActivityLifecycleItem) StopActivityItem.obtain(r.visible, r.configChangeFlags));
                this.mService.notifyActivityState(r, ActivityState.STOPPED);
                if (shouldSleepOrShutDownActivities()) {
                    r.setSleeping(true);
                }
                this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(104, r), 11000);
            } catch (Exception e) {
                Slog.w(ActivityManagerService.TAG, "Exception thrown during pause", e);
                r.stopped = true;
                if (ActivityManagerDebugConfig.DEBUG_STATES) {
                    Slog.v(ActivityManagerService.TAG, "Stop failed; moving to STOPPED: " + r);
                }
                r.setState(ActivityState.STOPPED, "stopActivityLocked");
                if (r.deferRelaunchUntilPaused) {
                    destroyActivityLocked(r, true, "stop-except");
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public final boolean requestFinishActivityLocked(IBinder token, int resultCode, Intent resultData, String reason, boolean oomAdj) {
        ActivityRecord r = isInStackLocked(token);
        if (ActivityManagerDebugConfig.DEBUG_RESULTS || ActivityManagerDebugConfig.DEBUG_STATES || HwSlog.HW_DEBUG_STATES) {
            Slog.v(ActivityManagerService.TAG, "Finishing activity token=" + token + " r=, result=" + resultCode + ", data=" + resultData + ", reason=" + reason);
        }
        if (r == null) {
            return false;
        }
        finishActivityLocked(r, resultCode, resultData, reason, oomAdj);
        return true;
    }

    /* access modifiers changed from: package-private */
    public final void finishSubActivityLocked(ActivityRecord self, String resultWho, int requestCode) {
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            ArrayList<ActivityRecord> activities = this.mTaskHistory.get(taskNdx).mActivities;
            for (int activityNdx = activities.size() - 1; activityNdx >= 0; activityNdx--) {
                ActivityRecord r = activities.get(activityNdx);
                if (r.resultTo == self && r.requestCode == requestCode && ((r.resultWho == null && resultWho == null) || (r.resultWho != null && r.resultWho.equals(resultWho)))) {
                    finishActivityLocked(r, 0, null, "request-sub", false);
                }
            }
        }
        this.mService.updateOomAdjLocked();
    }

    /* access modifiers changed from: package-private */
    public final TaskRecord finishTopCrashedActivityLocked(ProcessRecord app, String reason) {
        ActivityRecord r = topRunningActivityLocked();
        if (r == null || r.app != app) {
            return null;
        }
        Slog.w(ActivityManagerService.TAG, "  finishTopCrashedActivityLocked Force finishing activity " + r.intent.getComponent().flattenToShortString());
        TaskRecord finishedTask = r.getTask();
        int taskNdx = this.mTaskHistory.indexOf(finishedTask);
        int activityNdx = finishedTask.mActivities.indexOf(r);
        this.mWindowManager.prepareAppTransition(26, false);
        finishActivityLocked(r, 0, null, reason, false);
        int activityNdx2 = activityNdx - 1;
        if (activityNdx2 < 0) {
            do {
                taskNdx--;
                if (taskNdx < 0) {
                    break;
                }
                activityNdx2 = this.mTaskHistory.get(taskNdx).mActivities.size() - 1;
            } while (activityNdx2 < 0);
        }
        if (activityNdx2 >= 0 && taskNdx < this.mTaskHistory.size() && activityNdx2 < this.mTaskHistory.get(taskNdx).mActivities.size()) {
            ActivityRecord r2 = this.mTaskHistory.get(taskNdx).mActivities.get(activityNdx2);
            if (r2.isState(ActivityState.RESUMED, ActivityState.PAUSING, ActivityState.PAUSED) && (!r2.isActivityTypeHome() || this.mService.mHomeProcess != r2.app)) {
                Slog.w(ActivityManagerService.TAG, "  finishTopCrashedActivityLocked non_home Force finishing activity " + r2.intent.getComponent().flattenToShortString());
                finishActivityLocked(r2, 0, null, reason, false);
            }
        }
        return finishedTask;
    }

    /* access modifiers changed from: package-private */
    public final void finishVoiceTask(IVoiceInteractionSession session) {
        IBinder sessionBinder = session.asBinder();
        boolean didOne = false;
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            TaskRecord tr = this.mTaskHistory.get(taskNdx);
            if (tr.voiceSession == null || tr.voiceSession.asBinder() != sessionBinder) {
                int activityNdx = tr.mActivities.size() - 1;
                while (true) {
                    if (activityNdx < 0) {
                        break;
                    }
                    ActivityRecord r = tr.mActivities.get(activityNdx);
                    if (r.voiceSession != null && r.voiceSession.asBinder() == sessionBinder) {
                        r.clearVoiceSessionLocked();
                        try {
                            r.app.thread.scheduleLocalVoiceInteractionStarted(r.appToken, null);
                        } catch (RemoteException e) {
                        }
                        this.mService.finishRunningVoiceLocked();
                        break;
                    }
                    activityNdx--;
                }
            } else {
                for (int activityNdx2 = tr.mActivities.size() - 1; activityNdx2 >= 0; activityNdx2--) {
                    ActivityRecord r2 = tr.mActivities.get(activityNdx2);
                    if (!r2.finishing) {
                        finishActivityLocked(r2, 0, null, "finish-voice", false);
                        didOne = true;
                    }
                }
            }
        }
        if (didOne) {
            this.mService.updateOomAdjLocked();
        }
    }

    /* access modifiers changed from: package-private */
    public final boolean finishActivityAffinityLocked(ActivityRecord r) {
        ArrayList<ActivityRecord> activities = r.getTask().mActivities;
        for (int index = activities.indexOf(r); index >= 0; index--) {
            ActivityRecord cur = activities.get(index);
            if (!Objects.equals(cur.taskAffinity, r.taskAffinity)) {
                break;
            }
            finishActivityLocked(cur, 0, null, "request-affinity", true);
        }
        return true;
    }

    private void finishActivityResultsLocked(ActivityRecord r, int resultCode, Intent resultData) {
        ActivityRecord resultTo = r.resultTo;
        if (resultTo != null) {
            if (ActivityManagerDebugConfig.DEBUG_RESULTS) {
                Slog.v(ActivityManagerService.TAG, "Adding result to " + resultTo + " who=" + r.resultWho + " req=" + r.requestCode + " res=" + resultCode + " data=" + resultData);
            }
            if (!(resultTo.userId == r.userId || resultData == null)) {
                resultData.prepareToLeaveUser(r.userId);
            }
            if (r.info.applicationInfo.uid > 0) {
                this.mService.grantUriPermissionFromIntentLocked(r.info.applicationInfo.uid, resultTo.packageName, resultData, resultTo.getUriPermissionsLocked(), resultTo.userId);
            }
            resultTo.addResultLocked(r, r.resultWho, r.requestCode, resultCode, resultData);
            r.resultTo = null;
        } else if (ActivityManagerDebugConfig.DEBUG_RESULTS) {
            Slog.v(ActivityManagerService.TAG, "No result destination from " + r);
        }
        r.results = null;
        r.pendingResults = null;
        r.newIntents = null;
        r.icicle = null;
    }

    /* access modifiers changed from: package-private */
    public final boolean finishActivityLocked(ActivityRecord r, int resultCode, Intent resultData, String reason, boolean oomAdj) {
        return finishActivityLocked(r, resultCode, resultData, reason, oomAdj, false);
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x00b5, code lost:
        if (com.android.server.am.ActivityManagerDebugConfig.DEBUG_TRANSITION != false) goto L_0x00b7;
     */
    public final boolean finishActivityLocked(ActivityRecord r, int resultCode, Intent resultData, String reason, boolean oomAdj, boolean pauseImmediately) {
        ActivityRecord activityRecord = r;
        boolean removedActivity = false;
        if (activityRecord.finishing) {
            Slog.w(ActivityManagerService.TAG, "Duplicate finish request for " + activityRecord);
            return false;
        }
        this.mWindowManager.deferSurfaceLayout();
        try {
            r.makeFinishingLocked();
            TaskRecord task = r.getTask();
            if (task == null) {
                Slog.w(ActivityManagerService.TAG, "finishActivityLocked: r.getTask is null!");
                this.mWindowManager.continueSurfaceLayout();
                return false;
            }
            int finishMode = 2;
            EventLog.writeEvent(EventLogTags.AM_FINISH_ACTIVITY, new Object[]{Integer.valueOf(activityRecord.userId), Integer.valueOf(System.identityHashCode(r)), Integer.valueOf(task.taskId), activityRecord.shortComponentName, reason});
            ArrayList<ActivityRecord> activities = task.mActivities;
            int index = activities.indexOf(activityRecord);
            if (index < activities.size() - 1) {
                task.setFrontOfTask();
                if ((activityRecord.intent.getFlags() & DumpState.DUMP_FROZEN) != 0) {
                    activities.get(index + 1).intent.addFlags(DumpState.DUMP_FROZEN);
                }
            }
            r.pauseKeyDispatchingLocked();
            adjustFocusedActivityStack(activityRecord, "finishActivity");
            finishActivityResultsLocked(r, resultCode, resultData);
            boolean endTask = index <= 0 && !task.isClearingToReuseTask();
            int transit = endTask ? 9 : 7;
            if (this.mResumedActivity == activityRecord) {
                if (!ActivityManagerDebugConfig.DEBUG_VISIBILITY) {
                }
                Slog.v(ActivityManagerService.TAG, "Prepare close transition: finishing " + activityRecord);
                if (endTask) {
                    this.mService.mTaskChangeNotificationController.notifyTaskRemovalStarted(task.taskId);
                }
                try {
                    this.mWindowManager.prepareAppTransition(transit, false);
                    activityRecord.setVisibility(false);
                    if (this.mPausingActivity == null) {
                        if (ActivityManagerDebugConfig.DEBUG_PAUSE) {
                            Slog.v(ActivityManagerService.TAG, "Finish needs to pause: " + activityRecord);
                        }
                        if (ActivityManagerDebugConfig.DEBUG_USER_LEAVING) {
                            Slog.v(ActivityManagerService.TAG, "finish() => pause with userLeaving=false");
                        }
                        startPausingLocked(false, false, null, pauseImmediately);
                    } else {
                        boolean z = pauseImmediately;
                    }
                    if (endTask) {
                        this.mService.getLockTaskController().clearLockedTask(task);
                    }
                    boolean z2 = oomAdj;
                } catch (Throwable th) {
                    th = th;
                    boolean z3 = oomAdj;
                    this.mWindowManager.continueSurfaceLayout();
                    throw th;
                }
            } else {
                boolean z4 = pauseImmediately;
                if (!activityRecord.isState(ActivityState.PAUSING)) {
                    if (ActivityManagerDebugConfig.DEBUG_PAUSE) {
                        Slog.v(ActivityManagerService.TAG, "Finish not pausing: " + activityRecord);
                    }
                    if (activityRecord.visible && !r.isSplitMode()) {
                        prepareActivityHideTransitionAnimation(activityRecord, transit);
                    }
                    if (!activityRecord.visible) {
                        if (!activityRecord.nowVisible) {
                            finishMode = 1;
                        }
                    }
                    try {
                        if (finishCurrentActivityLocked(activityRecord, finishMode, oomAdj, "finishActivityLocked") == null) {
                            removedActivity = true;
                        }
                        if (task.onlyHasTaskOverlayActivities(true)) {
                            Iterator<ActivityRecord> it = task.mActivities.iterator();
                            while (it.hasNext()) {
                                ActivityRecord taskOverlay = it.next();
                                if (taskOverlay.mTaskOverlay) {
                                    prepareActivityHideTransitionAnimation(taskOverlay, transit);
                                }
                            }
                        }
                        this.mWindowManager.continueSurfaceLayout();
                        return removedActivity;
                    } catch (Throwable th2) {
                        th = th2;
                        this.mWindowManager.continueSurfaceLayout();
                        throw th;
                    }
                } else {
                    boolean z5 = oomAdj;
                    if (ActivityManagerDebugConfig.DEBUG_PAUSE) {
                        Slog.v(ActivityManagerService.TAG, "Finish waiting for pause of: " + activityRecord);
                    }
                }
            }
            this.mWindowManager.continueSurfaceLayout();
            return false;
        } catch (Throwable th3) {
            th = th3;
            boolean z6 = oomAdj;
            boolean z7 = pauseImmediately;
            this.mWindowManager.continueSurfaceLayout();
            throw th;
        }
    }

    private void prepareActivityHideTransitionAnimation(ActivityRecord r, int transit) {
        this.mWindowManager.prepareAppTransition(transit, false);
        r.setVisibility(false);
        this.mWindowManager.executeAppTransition();
        if (!this.mStackSupervisor.mActivitiesWaitingForVisibleActivity.contains(r)) {
            this.mStackSupervisor.mActivitiesWaitingForVisibleActivity.add(r);
        }
    }

    /* access modifiers changed from: package-private */
    public final ActivityRecord finishCurrentActivityLocked(ActivityRecord r, int mode, boolean oomAdj, String reason) {
        ActivityRecord next = this.mStackSupervisor.topRunningActivityLocked(true);
        if (mode != 2 || ((!r.visible && !r.nowVisible) || next == null || (next.nowVisible && (!HwPCUtils.isPcCastModeInServer() || !HwPCUtils.isPcDynamicStack(r.getStackId()))))) {
            this.mStackSupervisor.mStoppingActivities.remove(r);
            this.mStackSupervisor.mGoingToSleepActivities.remove(r);
            this.mStackSupervisor.mActivitiesWaitingForVisibleActivity.remove(r);
            ActivityState prevState = r.getState();
            if (ActivityManagerDebugConfig.DEBUG_STATES) {
                Slog.v(ActivityManagerService.TAG, "Moving to FINISHING: " + r);
            }
            r.setState(ActivityState.FINISHING, "finishCurrentActivityLocked");
            boolean finishingActivityInNonFocusedStack = r.getStack() != this.mStackSupervisor.getFocusedStack() && prevState == ActivityState.PAUSED && mode == 2;
            boolean finishingLastActivityInFreeformStack = prevState == ActivityState.PAUSED && mode == 2 && inFreeformWindowingMode();
            if (mode == 0 || ((prevState == ActivityState.PAUSED && (mode == 1 || inPinnedWindowingMode())) || finishingActivityInNonFocusedStack || finishingLastActivityInFreeformStack || prevState == ActivityState.STOPPING || prevState == ActivityState.STOPPED || prevState == ActivityState.INITIALIZING)) {
                r.makeFinishingLocked();
                boolean activityRemoved = destroyActivityLocked(r, true, "finish-imm:" + reason);
                if (finishingActivityInNonFocusedStack && this.mDisplayId != -1) {
                    this.mStackSupervisor.ensureVisibilityAndConfig(next, this.mDisplayId, false, true);
                    Flog.i(101, "Moving to FINISHING r=" + r + " destroy returned removed=" + activityRemoved);
                }
                if (activityRemoved) {
                    this.mStackSupervisor.mActivityLaunchTrack = "finishImmAtivityRemoved";
                    this.mStackSupervisor.resumeFocusedStackTopActivityLocked();
                }
                if (ActivityManagerDebugConfig.DEBUG_CONTAINERS) {
                    Slog.d(ActivityManagerService.TAG, "destroyActivityLocked: finishCurrentActivityLocked r=" + r + " destroy returned removed=" + activityRemoved);
                }
                return activityRemoved ? null : r;
            }
            if (ActivityManagerDebugConfig.DEBUG_ALL) {
                Slog.v(ActivityManagerService.TAG, "Enqueueing pending finish: " + r);
            }
            this.mStackSupervisor.mFinishingActivities.add(r);
            r.resumeKeyDispatchingLocked();
            this.mStackSupervisor.mActivityLaunchTrack = "enqueueFinishResume";
            this.mStackSupervisor.resumeFocusedStackTopActivityLocked();
            return r;
        }
        if (!this.mStackSupervisor.mStoppingActivities.contains(r)) {
            addToStopping(r, false, false);
        }
        Flog.i(101, "Moving to STOPPING: " + r + " (finish requested)");
        r.setState(ActivityState.STOPPING, "finishCurrentActivityLocked");
        if (oomAdj) {
            this.mService.updateOomAdjLocked();
        }
        return r;
    }

    /* access modifiers changed from: package-private */
    public void finishAllActivitiesLocked(boolean immediately) {
        boolean noActivitiesInStack = true;
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            ArrayList<ActivityRecord> activities = this.mTaskHistory.get(taskNdx).mActivities;
            for (int activityNdx = activities.size() - 1; activityNdx >= 0; activityNdx--) {
                ActivityRecord r = activities.get(activityNdx);
                noActivitiesInStack = false;
                if (!r.finishing || immediately) {
                    Slog.d(ActivityManagerService.TAG, "finishAllActivitiesLocked: finishing " + r + " immediately");
                    finishCurrentActivityLocked(r, 0, false, "finishAllActivitiesLocked");
                }
            }
        }
        if (noActivitiesInStack) {
            remove();
        }
    }

    /* access modifiers changed from: package-private */
    public boolean inFrontOfStandardStack() {
        ActivityDisplay display = getDisplay();
        if (display == null) {
            return false;
        }
        int index = display.getIndexOf(this);
        if (index == 0) {
            return false;
        }
        return display.getChildAt(index - 1).isActivityTypeStandard();
    }

    /* access modifiers changed from: package-private */
    public boolean shouldUpRecreateTaskLocked(ActivityRecord srec, String destAffinity) {
        if (srec == null || srec.getTask().affinity == null || !srec.getTask().affinity.equals(destAffinity)) {
            return true;
        }
        TaskRecord task = srec.getTask();
        if (srec.frontOfTask && task.getBaseIntent() != null && task.getBaseIntent().isDocument()) {
            if (!inFrontOfStandardStack()) {
                return true;
            }
            int taskIdx = this.mTaskHistory.indexOf(task);
            if (taskIdx <= 0) {
                Slog.w(ActivityManagerService.TAG, "shouldUpRecreateTask: task not in history for " + srec);
                return false;
            } else if (!task.affinity.equals(this.mTaskHistory.get(taskIdx).affinity)) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public final boolean navigateUpToLocked(ActivityRecord srec, Intent destIntent, int resultCode, Intent resultData) {
        boolean z;
        ActivityRecord activityRecord = srec;
        Intent intent = destIntent;
        TaskRecord task = srec.getTask();
        ArrayList<ActivityRecord> activities = task.mActivities;
        int start = activities.indexOf(activityRecord);
        if (!this.mTaskHistory.contains(task) || start < 0) {
            return false;
        }
        int finishTo = start - 1;
        ActivityRecord parent = finishTo < 0 ? null : activities.get(finishTo);
        boolean foundParentInTask = false;
        ComponentName dest = destIntent.getComponent();
        if (start > 0 && dest != null) {
            int i = finishTo;
            while (true) {
                if (i < 0) {
                    break;
                }
                ActivityRecord r = activities.get(i);
                if (r.info.packageName.equals(dest.getPackageName()) && r.info.name.equals(dest.getClassName())) {
                    finishTo = i;
                    parent = r;
                    foundParentInTask = true;
                    break;
                }
                i--;
            }
        }
        int finishTo2 = finishTo;
        ActivityRecord parent2 = parent;
        boolean foundParentInTask2 = foundParentInTask;
        IActivityController controller = this.mService.mController;
        if (controller != null) {
            ActivityRecord next = topRunningActivityLocked(activityRecord.appToken, 0);
            if (next != null) {
                boolean resumeOK = true;
                try {
                    resumeOK = controller.activityResuming(next.packageName);
                } catch (RemoteException e) {
                    this.mService.mController = null;
                    Watchdog.getInstance().setActivityController(null);
                }
                if (!resumeOK) {
                    return false;
                }
            }
        }
        long origId = Binder.clearCallingIdentity();
        int resultCode2 = resultCode;
        Intent resultData2 = resultData;
        int i2 = start;
        while (i2 > finishTo2) {
            ActivityRecord r2 = activities.get(i2);
            ActivityRecord activityRecord2 = r2;
            requestFinishActivityLocked(r2.appToken, resultCode2, resultData2, "navigate-up", true);
            resultCode2 = 0;
            resultData2 = null;
            i2--;
            origId = origId;
            controller = controller;
            finishTo2 = finishTo2;
            dest = dest;
        }
        int i3 = finishTo2;
        ComponentName componentName = dest;
        long origId2 = origId;
        if (parent2 != null && foundParentInTask2) {
            int parentLaunchMode = parent2.info.launchMode;
            int destIntentFlags = destIntent.getFlags();
            if (!(parentLaunchMode == 3 || parentLaunchMode == 2)) {
                boolean z2 = true;
                if (parentLaunchMode != 1) {
                    if ((destIntentFlags & 67108864) != 0) {
                        int i4 = parentLaunchMode;
                        parent2.deliverNewIntentLocked(activityRecord.info.applicationInfo.uid, intent, activityRecord.packageName);
                    } else {
                        try {
                            if (this.mService.getActivityStartController().obtainStarter(intent, "navigateUpTo").setCaller(activityRecord.app.thread).setActivityInfo(AppGlobals.getPackageManager().getActivityInfo(destIntent.getComponent(), 1024, activityRecord.userId)).setResultTo(parent2.appToken).setCallingPid(-1).setCallingUid(parent2.launchedFromUid).setCallingPackage(parent2.launchedFromPackage).setRealCallingPid(-1).setRealCallingUid(parent2.launchedFromUid).setComponentSpecified(true).execute() != 0) {
                                z2 = false;
                            }
                            z = z2;
                        } catch (RemoteException e2) {
                            z = false;
                        }
                        foundParentInTask2 = z;
                        int i5 = parentLaunchMode;
                        requestFinishActivityLocked(parent2.appToken, resultCode2, resultData2, "navigate-top", true);
                    }
                }
            }
            int i6 = parentLaunchMode;
            parent2.deliverNewIntentLocked(activityRecord.info.applicationInfo.uid, intent, activityRecord.packageName);
        }
        Binder.restoreCallingIdentity(origId2);
        return foundParentInTask2;
    }

    /* access modifiers changed from: package-private */
    public void onActivityRemovedFromStack(ActivityRecord r) {
        removeTimeoutsForActivityLocked(r);
        if (this.mResumedActivity != null && this.mResumedActivity == r) {
            setResumedActivity(null, "onActivityRemovedFromStack");
        }
        if (this.mPausingActivity != null && this.mPausingActivity == r) {
            Flog.i(101, "Remove the pausingActivity " + this.mPausingActivity + " in stack " + this.mStackId);
            this.mPausingActivity = null;
        }
    }

    /* access modifiers changed from: package-private */
    public void onActivityAddedToStack(ActivityRecord r) {
        if (r.getState() == ActivityState.RESUMED) {
            setResumedActivity(r, "onActivityAddedToStack");
        }
    }

    private void cleanUpActivityLocked(ActivityRecord r, boolean cleanServices, boolean setState) {
        onActivityRemovedFromStack(r);
        r.deferRelaunchUntilPaused = false;
        r.frozenBeforeDestroy = false;
        if (setState) {
            if (ActivityManagerDebugConfig.DEBUG_STATES) {
                Slog.v(ActivityManagerService.TAG, "Moving to DESTROYED: " + r + " (cleaning up)");
            }
            r.setState(ActivityState.DESTROYED, "cleanupActivityLocked");
            if (ActivityManagerDebugConfig.DEBUG_APP) {
                Slog.v(ActivityManagerService.TAG, "Clearing app during cleanUp for activity " + r);
            }
            r.app = null;
        }
        this.mStackSupervisor.cleanupActivity(r);
        if (r.finishing && r.pendingResults != null) {
            Iterator<WeakReference<PendingIntentRecord>> it = r.pendingResults.iterator();
            while (it.hasNext()) {
                PendingIntentRecord rec = (PendingIntentRecord) it.next().get();
                if (rec != null) {
                    this.mService.cancelIntentSenderLocked(rec, false);
                }
            }
            r.pendingResults = null;
        }
        if (cleanServices) {
            cleanUpActivityServicesLocked(r);
        }
        removeTimeoutsForActivityLocked(r);
        this.mWindowManager.notifyAppRelaunchesCleared(r.appToken);
    }

    /* access modifiers changed from: package-private */
    public void removeTimeoutsForActivityLocked(ActivityRecord r) {
        if (r != null) {
            this.mStackSupervisor.removeTimeoutsForActivityLocked(r);
            this.mHandler.removeMessages(101, r);
            this.mHandler.removeMessages(104, r);
            this.mHandler.removeMessages(102, r);
            r.finishLaunchTickingLocked();
        }
    }

    private void removeActivityFromHistoryLocked(ActivityRecord r, String reason) {
        finishActivityResultsLocked(r, 0, null);
        r.makeFinishingLocked();
        if (ActivityManagerDebugConfig.DEBUG_ADD_REMOVE) {
            Slog.i(ActivityManagerService.TAG, "Removing activity " + r + " from stack callers=" + Debug.getCallers(5));
        }
        r.takeFromHistory();
        removeTimeoutsForActivityLocked(r);
        if (ActivityManagerDebugConfig.DEBUG_STATES || HwSlog.HW_DEBUG_STATES) {
            Slog.v(ActivityManagerService.TAG, "Moving to DESTROYED: " + r + " (removed from history)");
        }
        r.setState(ActivityState.DESTROYED, "removeActivityFromHistoryLocked");
        if (ActivityManagerDebugConfig.DEBUG_APP) {
            Slog.v(ActivityManagerService.TAG, "Clearing app during remove for activity " + r);
        }
        r.app = null;
        r.removeWindowContainer();
        TaskRecord task = r.getTask();
        boolean lastActivity = task != null ? task.removeActivity(r) : false;
        boolean onlyHasTaskOverlays = task != null ? task.onlyHasTaskOverlayActivities(false) : false;
        if (lastActivity || onlyHasTaskOverlays) {
            if (ActivityManagerDebugConfig.DEBUG_STACK) {
                Slog.i(ActivityManagerService.TAG, "removeActivityFromHistoryLocked: last activity removed from " + this + " onlyHasTaskOverlays=" + onlyHasTaskOverlays);
            }
            if (onlyHasTaskOverlays) {
                this.mStackSupervisor.removeTaskByIdLocked(task.taskId, false, false, true, reason);
            }
            if (lastActivity) {
                removeTask(task, reason, 0);
            }
        }
        cleanUpActivityServicesLocked(r);
        r.removeUriPermissionsLocked();
    }

    private void cleanUpActivityServicesLocked(ActivityRecord r) {
        if (r.connections != null) {
            Iterator<ConnectionRecord> it = r.connections.iterator();
            while (it.hasNext()) {
                this.mService.mServices.removeConnectionLocked(it.next(), null, r);
            }
            r.connections = null;
        }
    }

    /* access modifiers changed from: package-private */
    public final void scheduleDestroyActivities(ProcessRecord owner, String reason) {
        Message msg = this.mHandler.obtainMessage(105);
        msg.obj = new ScheduleDestroyArgs(owner, reason);
        this.mHandler.sendMessage(msg);
    }

    /* access modifiers changed from: private */
    public void destroyActivitiesLocked(ProcessRecord owner, String reason) {
        boolean lastIsOpaque = false;
        boolean activityRemoved = false;
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            ArrayList<ActivityRecord> activities = this.mTaskHistory.get(taskNdx).mActivities;
            for (int activityNdx = activities.size() - 1; activityNdx >= 0; activityNdx--) {
                ActivityRecord r = activities.get(activityNdx);
                if (!r.finishing) {
                    if (r.fullscreen) {
                        lastIsOpaque = true;
                    }
                    if ((owner == null || r.app == owner) && lastIsOpaque && r.isDestroyable()) {
                        if (ActivityManagerDebugConfig.DEBUG_SWITCH) {
                            Slog.v(ActivityManagerService.TAG, "Destroying " + r + " in state " + r.getState() + " resumed=" + this.mResumedActivity + " pausing=" + this.mPausingActivity + " for reason " + reason);
                        }
                        if (destroyActivityLocked(r, true, reason)) {
                            activityRemoved = true;
                        }
                    }
                }
            }
        }
        if (activityRemoved) {
            this.mStackSupervisor.mActivityLaunchTrack = "destroyedAtivityRemoved";
            this.mStackSupervisor.resumeFocusedStackTopActivityLocked();
        }
    }

    /* access modifiers changed from: package-private */
    public final boolean safelyDestroyActivityLocked(ActivityRecord r, String reason) {
        if (!r.isDestroyable()) {
            return false;
        }
        if (ActivityManagerDebugConfig.DEBUG_SWITCH) {
            Slog.v(ActivityManagerService.TAG, "Destroying " + r + " in state " + r.getState() + " resumed=" + this.mResumedActivity + " pausing=" + this.mPausingActivity + " for reason " + reason);
        }
        return destroyActivityLocked(r, true, reason);
    }

    /* access modifiers changed from: package-private */
    public final int releaseSomeActivitiesLocked(ProcessRecord app, ArraySet<TaskRecord> tasks, String reason) {
        ProcessRecord processRecord = app;
        String str = reason;
        if (ActivityManagerDebugConfig.DEBUG_RELEASE) {
            Slog.d(ActivityManagerService.TAG, "Trying to release some activities in " + processRecord);
        }
        int maxTasks = tasks.size() / 4;
        if (maxTasks < 1) {
            maxTasks = 1;
        }
        int numReleased = 0;
        int maxTasks2 = maxTasks;
        int taskNdx = 0;
        while (taskNdx < this.mTaskHistory.size() && maxTasks2 > 0) {
            TaskRecord task = this.mTaskHistory.get(taskNdx);
            if (tasks.contains(task)) {
                if (ActivityManagerDebugConfig.DEBUG_RELEASE) {
                    Slog.d(ActivityManagerService.TAG, "Looking for activities to release in " + task);
                }
                ArrayList<ActivityRecord> activities = task.mActivities;
                int curNum = 0;
                int actNdx = 0;
                while (actNdx < activities.size()) {
                    ActivityRecord activity = activities.get(actNdx);
                    if (activity.app == processRecord && activity.isDestroyable()) {
                        if (ActivityManagerDebugConfig.DEBUG_RELEASE) {
                            Slog.v(ActivityManagerService.TAG, "Destroying " + activity + " in state " + activity.getState() + " resumed=" + this.mResumedActivity + " pausing=" + this.mPausingActivity + " for reason " + str);
                        }
                        destroyActivityLocked(activity, true, str);
                        if (activities.get(actNdx) != activity) {
                            actNdx--;
                        }
                        curNum++;
                    }
                    actNdx++;
                }
                if (curNum > 0) {
                    numReleased += curNum;
                    maxTasks2--;
                    if (this.mTaskHistory.get(taskNdx) != task) {
                        taskNdx--;
                    }
                }
            }
            taskNdx++;
        }
        ArraySet<TaskRecord> arraySet = tasks;
        if (ActivityManagerDebugConfig.DEBUG_RELEASE) {
            Slog.d(ActivityManagerService.TAG, "Done releasing: did " + numReleased + " activities");
        }
        return numReleased;
    }

    /* access modifiers changed from: package-private */
    public final boolean destroyActivityLocked(ActivityRecord r, boolean removeFromApp, String reason) {
        if (ActivityManagerDebugConfig.DEBUG_SWITCH || ActivityManagerDebugConfig.DEBUG_CLEANUP) {
            StringBuilder sb = new StringBuilder();
            sb.append("Removing activity from ");
            sb.append(reason);
            sb.append(": token=");
            sb.append(r);
            sb.append(", app=");
            sb.append(r.app != null ? r.app.processName : "(null)");
            Slog.v(ActivityManagerService.TAG, sb.toString());
        }
        if (r.isState(ActivityState.DESTROYING, ActivityState.DESTROYED)) {
            if (ActivityManagerDebugConfig.DEBUG_STATES) {
                Slog.v(ActivityManagerService.TAG, "activity " + r + " already destroying.skipping request with reason:" + reason);
            }
            return false;
        }
        boolean z = true;
        EventLog.writeEvent(EventLogTags.AM_DESTROY_ACTIVITY, new Object[]{Integer.valueOf(r.userId), Integer.valueOf(System.identityHashCode(r)), Integer.valueOf(r.getTask().taskId), r.shortComponentName, reason});
        this.mService.notifyActivityState(r, ActivityState.DESTROYED);
        boolean removedFromHistory = false;
        cleanUpActivityLocked(r, false, false);
        if (ActivityManagerDebugConfig.DEBUG_SWITCH) {
            Slog.i(ActivityManagerService.TAG, "Activity has been cleaned up!");
        }
        if (r.app != null) {
            this.mService.recognizeFakeActivity(r.shortComponentName, r.app.pid, r.app.uid);
        }
        if (r.app == null) {
            z = false;
        }
        boolean hadApp = z;
        if (hadApp) {
            if (removeFromApp) {
                r.app.activities.remove(r);
                if (this.mService.mHeavyWeightProcess == r.app && r.app.activities.size() <= 0) {
                    this.mService.mHeavyWeightProcess = null;
                    this.mService.mHandler.sendEmptyMessage(25);
                }
                if (r.app.activities.isEmpty()) {
                    this.mService.mServices.updateServiceConnectionActivitiesLocked(r.app);
                    this.mService.updateLruProcessLocked(r.app, false, null);
                    this.mService.updateOomAdjLocked();
                }
            }
            boolean skipDestroy = false;
            try {
                if (ActivityManagerDebugConfig.DEBUG_SWITCH) {
                    Slog.i(ActivityManagerService.TAG, "Destroying: " + r);
                }
                this.mService.getLifecycleManager().scheduleTransaction(r.app.thread, (IBinder) r.appToken, (ActivityLifecycleItem) DestroyActivityItem.obtain(r.finishing, r.configChangeFlags));
            } catch (Exception e) {
                if (r.finishing) {
                    removeActivityFromHistoryLocked(r, reason + " exceptionInScheduleDestroy");
                    removedFromHistory = true;
                    skipDestroy = true;
                }
            }
            r.nowVisible = false;
            if (!r.finishing || skipDestroy) {
                if (ActivityManagerDebugConfig.DEBUG_STATES) {
                    Slog.v(ActivityManagerService.TAG, "Moving to DESTROYED: " + r + " (destroy skipped) in stack " + this.mStackId);
                }
                r.setState(ActivityState.DESTROYED, "destroyActivityLocked. not finishing or skipping destroy");
                if (ActivityManagerDebugConfig.DEBUG_APP) {
                    Slog.v(ActivityManagerService.TAG, "Clearing app during destroy for activity " + r);
                }
                r.app = null;
            } else {
                if (ActivityManagerDebugConfig.DEBUG_STATES) {
                    Slog.v(ActivityManagerService.TAG, "Moving to DESTROYING: " + r + " (destroy requested) in stack " + this.mStackId);
                }
                r.setState(ActivityState.DESTROYING, "destroyActivityLocked. finishing and not skipping destroy");
                this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(102, r), JobStatus.DEFAULT_TRIGGER_UPDATE_DELAY);
            }
        } else if (r.finishing) {
            removeActivityFromHistoryLocked(r, reason + " hadNoApp");
            removedFromHistory = true;
        } else {
            if (ActivityManagerDebugConfig.DEBUG_STATES) {
                Slog.v(ActivityManagerService.TAG, "Moving to DESTROYED: " + r + " (no app)");
            }
            r.setState(ActivityState.DESTROYED, "destroyActivityLocked. not finishing and had no app");
            if (ActivityManagerDebugConfig.DEBUG_APP) {
                Slog.v(ActivityManagerService.TAG, "Clearing app during destroy for activity " + r);
            }
            r.app = null;
        }
        r.configChangeFlags = 0;
        if (!this.mLRUActivities.remove(r) && hadApp) {
            Slog.w(ActivityManagerService.TAG, "Activity " + r + " being finished, but not in LRU list");
        }
        return removedFromHistory;
    }

    /* access modifiers changed from: package-private */
    public final void activityDestroyedLocked(IBinder token, String reason) {
        long origId = Binder.clearCallingIdentity();
        try {
            activityDestroyedLocked(ActivityRecord.forTokenLocked(token), reason);
        } finally {
            Binder.restoreCallingIdentity(origId);
        }
    }

    /* access modifiers changed from: package-private */
    public final void activityDestroyedLocked(ActivityRecord record, String reason) {
        if (record != null) {
            this.mHandler.removeMessages(102, record);
        }
        if (ActivityManagerDebugConfig.DEBUG_CONTAINERS) {
            Slog.d(ActivityManagerService.TAG, "activityDestroyedLocked: r=" + record);
        }
        if (isInStackLocked(record) != null && record.isState(ActivityState.DESTROYING, ActivityState.DESTROYED)) {
            cleanUpActivityLocked(record, true, false);
            removeActivityFromHistoryLocked(record, reason);
        }
        this.mStackSupervisor.mActivityLaunchTrack = "activityDestroyed";
        this.mStackSupervisor.resumeFocusedStackTopActivityLocked();
    }

    private void removeHistoryRecordsForAppLocked(ArrayList<ActivityRecord> list, ProcessRecord app, String listName) {
        int i = list.size();
        if (ActivityManagerDebugConfig.DEBUG_CLEANUP) {
            Slog.v(ActivityManagerService.TAG, "Removing app " + app + " from list " + listName + " with " + i + " entries");
        }
        while (i > 0) {
            i--;
            ActivityRecord r = list.get(i);
            if (ActivityManagerDebugConfig.DEBUG_CLEANUP) {
                Slog.v(ActivityManagerService.TAG, "Record #" + i + " " + r);
            }
            if (r.app == app) {
                if (ActivityManagerDebugConfig.DEBUG_CLEANUP) {
                    Slog.v(ActivityManagerService.TAG, "---> REMOVING this entry!");
                }
                list.remove(i);
                removeTimeoutsForActivityLocked(r);
            }
        }
    }

    private boolean removeHistoryRecordsForAppLocked(ProcessRecord app) {
        boolean remove;
        ProcessRecord processRecord = app;
        removeHistoryRecordsForAppLocked(this.mLRUActivities, processRecord, "mLRUActivities");
        removeHistoryRecordsForAppLocked(this.mStackSupervisor.mStoppingActivities, processRecord, "mStoppingActivities");
        removeHistoryRecordsForAppLocked(this.mStackSupervisor.mGoingToSleepActivities, processRecord, "mGoingToSleepActivities");
        removeHistoryRecordsForAppLocked(this.mStackSupervisor.mActivitiesWaitingForVisibleActivity, processRecord, "mActivitiesWaitingForVisibleActivity");
        removeHistoryRecordsForAppLocked(this.mStackSupervisor.mFinishingActivities, processRecord, "mFinishingActivities");
        boolean hasVisibleActivities = false;
        int i = numActivities();
        if (ActivityManagerDebugConfig.DEBUG_CLEANUP) {
            Slog.v(ActivityManagerService.TAG, "Removing app " + processRecord + " from history with " + i + " entries");
        }
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            ArrayList<ActivityRecord> activities = this.mTaskHistory.get(taskNdx).mActivities;
            this.mTmpActivities.clear();
            this.mTmpActivities.addAll(activities);
            while (!this.mTmpActivities.isEmpty()) {
                ActivityRecord r = this.mTmpActivities.remove(this.mTmpActivities.size() - 1);
                if (ActivityManagerDebugConfig.DEBUG_CLEANUP) {
                    Slog.v(ActivityManagerService.TAG, "Record #" + targetIndex + " " + r + ": app=" + r.app);
                }
                if (r.app == processRecord) {
                    if (r.visible) {
                        hasVisibleActivities = true;
                    }
                    if ((!r.haveState && !r.stateNotNeeded) || r.finishing) {
                        remove = true;
                    } else if (!r.visible && r.launchCount > 2 && r.lastLaunchTime > SystemClock.uptimeMillis() - 60000) {
                        remove = true;
                    } else if (r.launchCount <= 5 || r.lastLaunchTime <= SystemClock.uptimeMillis() - TRANSLUCENT_CONVERSION_TIMEOUT) {
                        remove = false;
                    } else {
                        remove = true;
                        Slog.v(ActivityManagerService.TAG, "too many launcher times, remove : " + r);
                    }
                    if (remove) {
                        if (ActivityManagerDebugConfig.DEBUG_ADD_REMOVE || ActivityManagerDebugConfig.DEBUG_CLEANUP) {
                            Slog.i(ActivityManagerService.TAG, "Removing activity " + r + " from stack at " + i + ": haveState=" + r.haveState + " stateNotNeeded=" + r.stateNotNeeded + " finishing=" + r.finishing + " state=" + r.getState() + " callers=" + Debug.getCallers(5));
                        }
                        if (!r.finishing) {
                            Slog.w(ActivityManagerService.TAG, "Force removing " + r + ": app died, no saved state");
                            EventLog.writeEvent(EventLogTags.AM_FINISH_ACTIVITY, new Object[]{Integer.valueOf(r.userId), Integer.valueOf(System.identityHashCode(r)), Integer.valueOf(r.getTask().taskId), r.shortComponentName, "proc died without state saved"});
                            if (r.getState() == ActivityState.RESUMED) {
                                this.mService.updateUsageStats(r, false);
                            }
                        }
                    } else {
                        if (ActivityManagerDebugConfig.DEBUG_ALL) {
                            Slog.v(ActivityManagerService.TAG, "Keeping entry, setting app to null");
                        }
                        if (ActivityManagerDebugConfig.DEBUG_APP) {
                            Slog.v(ActivityManagerService.TAG, "Clearing app during removeHistory for activity " + r);
                        }
                        r.app = null;
                        r.nowVisible = r.visible;
                        if (!r.haveState) {
                            if (ActivityManagerDebugConfig.DEBUG_SAVED_STATE) {
                                Slog.i(ActivityManagerService.TAG, "App died, clearing saved state of " + r);
                            }
                            r.icicle = null;
                        }
                    }
                    cleanUpActivityLocked(r, true, true);
                    if (remove) {
                        removeActivityFromHistoryLocked(r, "appDied");
                    }
                }
            }
        }
        return hasVisibleActivities;
    }

    private void updateTransitLocked(int transit, ActivityOptions options) {
        if (options != null) {
            ActivityRecord r = topRunningActivityLocked();
            if (r == null || r.isState(ActivityState.RESUMED)) {
                ActivityOptions.abort(options);
            } else {
                r.updateOptionsLocked(options);
            }
        }
        this.mWindowManager.prepareAppTransition(transit, false);
    }

    private void updateTaskMovement(TaskRecord task, boolean toFront) {
        if (task.isPersistable) {
            task.mLastTimeMoved = System.currentTimeMillis();
            if (!toFront) {
                task.mLastTimeMoved *= -1;
            }
        }
        this.mStackSupervisor.invalidateTaskLayers();
    }

    /* access modifiers changed from: package-private */
    public void moveHomeStackTaskToTop() {
        if (isActivityTypeHome()) {
            int top = this.mTaskHistory.size() - 1;
            if (top >= 0) {
                TaskRecord task = this.mTaskHistory.get(top);
                if (ActivityManagerDebugConfig.DEBUG_TASKS || ActivityManagerDebugConfig.DEBUG_STACK) {
                    Slog.d(ActivityManagerService.TAG, "moveHomeStackTaskToTop: moving " + task);
                }
                this.mTaskHistory.remove(top);
                this.mTaskHistory.add(top, task);
                updateTaskMovement(task, true);
                return;
            }
            return;
        }
        throw new IllegalStateException("Calling moveHomeStackTaskToTop() on non-home stack: " + this);
    }

    /* access modifiers changed from: protected */
    public void moveTaskToFrontLocked(TaskRecord tr, boolean noAnimation, ActivityOptions options, AppTimeTracker timeTracker, String reason) {
        String str;
        TaskRecord taskRecord = tr;
        ActivityOptions activityOptions = options;
        AppTimeTracker appTimeTracker = timeTracker;
        Flog.i(101, "moveTaskToFront: " + taskRecord + ", reason: " + str);
        ActivityStack topStack = getDisplay().getTopStack();
        ActivityRecord topActivity = topStack != null ? topStack.getTopActivity() : null;
        int numTasks = this.mTaskHistory.size();
        int index = this.mTaskHistory.indexOf(taskRecord);
        if (numTasks == 0 || index < 0) {
            if (noAnimation) {
                ActivityOptions.abort(options);
            } else {
                updateTransitLocked(10, activityOptions);
            }
            return;
        }
        if (appTimeTracker != null) {
            for (int i = taskRecord.mActivities.size() - 1; i >= 0; i--) {
                taskRecord.mActivities.get(i).appTimeTracker = appTimeTracker;
            }
        }
        ActivityDisplay activityDisplay = getDisplay();
        try {
            activityDisplay.deferUpdateImeTarget();
            insertTaskAtTop(taskRecord, null);
            ActivityRecord top = tr.getTopActivity();
            if (top != null) {
                if (top.okToShowLocked()) {
                    ActivityRecord r = topRunningActivityLocked();
                    this.mStackSupervisor.moveFocusableActivityStackToFrontLocked(r, str);
                    if (ActivityManagerDebugConfig.DEBUG_TRANSITION) {
                        Slog.v(ActivityManagerService.TAG, "Prepare to front transition: task=" + taskRecord);
                    }
                    if (noAnimation) {
                        this.mWindowManager.prepareAppTransition(0, false);
                        if (r != null) {
                            this.mStackSupervisor.mNoAnimActivities.add(r);
                        }
                        ActivityOptions.abort(options);
                    } else {
                        updateTransitLocked(10, activityOptions);
                    }
                    if (canEnterPipOnTaskSwitch(topActivity, taskRecord, null, activityOptions)) {
                        topActivity.supportsEnterPipOnTaskSwitch = true;
                    }
                    this.mStackSupervisor.mActivityLaunchTrack = "taskMove";
                    this.mStackSupervisor.resumeFocusedStackTopActivityLocked();
                    EventLog.writeEvent(EventLogTags.AM_TASK_TO_FRONT, new Object[]{Integer.valueOf(taskRecord.userId), Integer.valueOf(taskRecord.taskId)});
                    this.mService.mTaskChangeNotificationController.notifyTaskMovedToFront(taskRecord.taskId);
                    activityDisplay.continueUpdateImeTarget();
                    return;
                }
            }
            if (top != null) {
                this.mStackSupervisor.mRecentTasks.add(top.getTask());
            }
            ActivityOptions.abort(options);
        } finally {
            activityDisplay.continueUpdateImeTarget();
        }
    }

    /* access modifiers changed from: protected */
    public boolean moveTaskToBackLocked(int taskId) {
        TaskRecord tr = taskForIdLocked(taskId);
        if (tr == null) {
            Slog.i(ActivityManagerService.TAG, "moveTaskToBack: bad taskId=" + taskId);
            return false;
        }
        Slog.i(ActivityManagerService.TAG, "moveTaskToBack: " + tr);
        if (!this.mService.getLockTaskController().canMoveTaskToBack(tr)) {
            return false;
        }
        if (isTopStackOnDisplay() && this.mService.mController != null) {
            ActivityRecord next = topRunningActivityLocked(null, taskId);
            if (next == null) {
                next = topRunningActivityLocked(null, 0);
            }
            if (next != null) {
                boolean moveOK = true;
                try {
                    moveOK = this.mService.mController.activityResuming(next.packageName);
                } catch (RemoteException e) {
                    this.mService.mController = null;
                    Watchdog.getInstance().setActivityController(null);
                }
                if (!moveOK) {
                    return false;
                }
            }
        }
        if (ActivityManagerDebugConfig.DEBUG_TRANSITION) {
            Slog.v(ActivityManagerService.TAG, "Prepare to back transition: task=" + taskId);
        }
        this.mTaskHistory.remove(tr);
        this.mTaskHistory.add(0, tr);
        updateTaskMovement(tr, false);
        this.mWindowManager.prepareAppTransition(11, false);
        moveToBack("moveTaskToBackLocked", tr);
        if (inPinnedWindowingMode()) {
            this.mStackSupervisor.removeStack(this);
            return true;
        }
        this.mStackSupervisor.resumeFocusedStackTopActivityLocked();
        return true;
    }

    static void logStartActivity(int tag, ActivityRecord r, TaskRecord task) {
        Uri data = r.intent.getData();
        EventLog.writeEvent(tag, new Object[]{Integer.valueOf(r.userId), Integer.valueOf(System.identityHashCode(r)), Integer.valueOf(task.taskId), r.shortComponentName, r.intent.getAction(), r.intent.getType(), data != null ? data.toSafeString() : null, Integer.valueOf(r.intent.getFlags())});
    }

    /* access modifiers changed from: package-private */
    public void ensureVisibleActivitiesConfigurationLocked(ActivityRecord start, boolean preserveWindow) {
        if (start != null && start.visible) {
            boolean behindFullscreen = false;
            boolean updatedConfig = false;
            for (int taskIndex = this.mTaskHistory.indexOf(start.getTask()); taskIndex >= 0; taskIndex--) {
                TaskRecord task = this.mTaskHistory.get(taskIndex);
                ArrayList<ActivityRecord> activities = task.mActivities;
                int activityIndex = start.getTask() == task ? activities.indexOf(start) : activities.size() - 1;
                while (true) {
                    if (activityIndex < 0) {
                        break;
                    }
                    ActivityRecord r = activities.get(activityIndex);
                    updatedConfig |= r.ensureActivityConfiguration(0, preserveWindow);
                    if (r.fullscreen) {
                        behindFullscreen = true;
                        break;
                    }
                    activityIndex--;
                }
                if (behindFullscreen) {
                    break;
                }
            }
            if (updatedConfig) {
                this.mStackSupervisor.resumeFocusedStackTopActivityLocked();
            }
        }
    }

    public void requestResize(Rect bounds) {
        this.mService.resizeStack(this.mStackId, bounds, true, false, false, -1);
    }

    /* access modifiers changed from: package-private */
    public void resize(Rect bounds, Rect tempTaskBounds, Rect tempTaskInsetBounds) {
        if (updateBoundsAllowed(bounds, tempTaskBounds, tempTaskInsetBounds)) {
            Rect taskBounds = tempTaskBounds != null ? tempTaskBounds : bounds;
            Rect insetBounds = tempTaskInsetBounds != null ? tempTaskInsetBounds : taskBounds;
            this.mTmpBounds.clear();
            this.mTmpInsetBounds.clear();
            synchronized (this.mWindowManager.getWindowManagerLock()) {
                for (int i = this.mTaskHistory.size() - 1; i >= 0; i--) {
                    TaskRecord task = this.mTaskHistory.get(i);
                    if (task.isResizeable() || CoordinationModeUtils.getInstance(this.mService.mContext).isEnterOrExitCoordinationMode() || this.mService.mWindowManager.getDefaultDisplayContentLocked().getCoordinationPrimaryStackIgnoringVisibility() != null) {
                        if (!inFreeformWindowingMode()) {
                            if (!HwPCUtils.isExtDynamicStack(this.mStackId)) {
                                task.updateOverrideConfiguration(taskBounds, insetBounds);
                            }
                        }
                        this.mTmpRect2.set(task.getOverrideBounds());
                        fitWithinBounds(this.mTmpRect2, bounds);
                        task.updateOverrideConfiguration(this.mTmpRect2);
                    }
                    this.mTmpBounds.put(task.taskId, task.getOverrideBounds());
                    if (tempTaskInsetBounds != null) {
                        this.mTmpInsetBounds.put(task.taskId, tempTaskInsetBounds);
                    }
                }
                this.mWindowContainerController.resize(bounds, this.mTmpBounds, this.mTmpInsetBounds);
                setBounds(bounds);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void onPipAnimationEndResize() {
        this.mWindowContainerController.onPipAnimationEndResize();
    }

    private static void fitWithinBounds(Rect bounds, Rect stackBounds) {
        if (stackBounds != null && !stackBounds.isEmpty() && !stackBounds.contains(bounds)) {
            if (bounds.left < stackBounds.left || bounds.right > stackBounds.right) {
                int maxRight = stackBounds.right - (stackBounds.width() / 3);
                int horizontalDiff = stackBounds.left - bounds.left;
                if ((horizontalDiff < 0 && bounds.left >= maxRight) || bounds.left + horizontalDiff >= maxRight) {
                    horizontalDiff = maxRight - bounds.left;
                }
                bounds.left += horizontalDiff;
                bounds.right += horizontalDiff;
            }
            if (bounds.top < stackBounds.top || bounds.bottom > stackBounds.bottom) {
                int maxBottom = stackBounds.bottom - (stackBounds.height() / 3);
                int verticalDiff = stackBounds.top - bounds.top;
                if ((verticalDiff < 0 && bounds.top >= maxBottom) || bounds.top + verticalDiff >= maxBottom) {
                    verticalDiff = maxBottom - bounds.top;
                }
                bounds.top += verticalDiff;
                bounds.bottom += verticalDiff;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean willActivityBeVisibleLocked(IBinder token) {
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            ArrayList<ActivityRecord> activities = this.mTaskHistory.get(taskNdx).mActivities;
            for (int activityNdx = activities.size() - 1; activityNdx >= 0; activityNdx--) {
                ActivityRecord r = activities.get(activityNdx);
                if (r.appToken == token) {
                    return true;
                }
                if (r.fullscreen && !r.finishing) {
                    return false;
                }
            }
        }
        ActivityRecord r2 = ActivityRecord.forTokenLocked(token);
        if (r2 == null) {
            return false;
        }
        if (r2.finishing) {
            Slog.e(ActivityManagerService.TAG, "willActivityBeVisibleLocked: Returning false, would have returned true for r=" + r2);
        }
        return true ^ r2.finishing;
    }

    /* access modifiers changed from: package-private */
    public void closeSystemDialogsLocked() {
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            ArrayList<ActivityRecord> activities = this.mTaskHistory.get(taskNdx).mActivities;
            for (int activityNdx = activities.size() - 1; activityNdx >= 0; activityNdx--) {
                ActivityRecord r = activities.get(activityNdx);
                if ((r.info.flags & 256) != 0) {
                    finishActivityLocked(r, 0, null, "close-sys", true);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean finishDisabledPackageActivitiesLocked(String packageName, Set<String> filterByClasses, boolean doit, boolean evenPersistent, int userId) {
        String str = packageName;
        Set<String> set = filterByClasses;
        int i = userId;
        boolean didSomething = false;
        TaskRecord lastTask = null;
        ComponentName homeActivity = null;
        int taskNdx = this.mTaskHistory.size() - 1;
        while (true) {
            int taskNdx2 = taskNdx;
            if (taskNdx2 < 0) {
                return didSomething;
            }
            ArrayList<ActivityRecord> activities = this.mTaskHistory.get(taskNdx2).mActivities;
            this.mTmpActivities.clear();
            this.mTmpActivities.addAll(activities);
            while (!this.mTmpActivities.isEmpty()) {
                boolean z = false;
                ActivityRecord r = this.mTmpActivities.remove(0);
                if ((r.packageName.equals(str) && (set == null || set.contains(r.realActivity.getClassName()))) || (str == null && r.userId == i)) {
                    z = true;
                }
                boolean sameComponent = z;
                if ((i == -1 || r.userId == i) && ((sameComponent || r.getTask() == lastTask) && (r.app == null || evenPersistent || !r.app.persistent))) {
                    if (doit) {
                        if (r.isActivityTypeHome()) {
                            if (homeActivity == null || !homeActivity.equals(r.realActivity)) {
                                homeActivity = r.realActivity;
                            } else {
                                Slog.i(ActivityManagerService.TAG, "Skip force-stop again " + r);
                            }
                        }
                        ComponentName homeActivity2 = homeActivity;
                        Slog.i(ActivityManagerService.TAG, "  finishDisabledPackageActivitiesLocked Force finishing activity " + r);
                        if (sameComponent) {
                            if (r.app != null) {
                                r.app.removed = true;
                            }
                            r.app = null;
                        }
                        TaskRecord lastTask2 = r.getTask();
                        finishActivityLocked(r, 0, null, "force-stop", true);
                        homeActivity = homeActivity2;
                        didSomething = true;
                        lastTask = lastTask2;
                    } else if (!r.finishing) {
                        return true;
                    }
                }
            }
            taskNdx = taskNdx2 - 1;
        }
    }

    /* access modifiers changed from: package-private */
    public void getRunningTasks(List<TaskRecord> tasksOut, @WindowConfiguration.ActivityType int ignoreActivityType, @WindowConfiguration.WindowingMode int ignoreWindowingMode, int callingUid, boolean allowed) {
        boolean focusedStack = this.mStackSupervisor.getFocusedStack() == this;
        boolean topTask = true;
        int taskNdx = this.mTaskHistory.size() - 1;
        while (true) {
            int taskNdx2 = taskNdx;
            if (taskNdx2 >= 0) {
                TaskRecord task = this.mTaskHistory.get(taskNdx2);
                if (task.getTopActivity() != null && ((allowed || task.isActivityTypeHome() || task.effectiveUid == callingUid) && ((ignoreActivityType == 0 || task.getActivityType() != ignoreActivityType) && (ignoreWindowingMode == 0 || task.getWindowingMode() != ignoreWindowingMode)))) {
                    if (focusedStack && topTask) {
                        task.lastActiveTime = SystemClock.elapsedRealtime();
                        topTask = false;
                    }
                    tasksOut.add(task);
                }
                taskNdx = taskNdx2 - 1;
            } else {
                return;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void unhandledBackLocked() {
        int top = this.mTaskHistory.size() - 1;
        if (ActivityManagerDebugConfig.DEBUG_SWITCH) {
            Slog.d(ActivityManagerService.TAG, "Performing unhandledBack(): top activity at " + top);
        }
        if (top >= 0) {
            ArrayList<ActivityRecord> activities = this.mTaskHistory.get(top).mActivities;
            int activityTop = activities.size() - 1;
            if (activityTop >= 0) {
                finishActivityLocked(activities.get(activityTop), 0, null, "unhandled-back", true);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean handleAppDiedLocked(ProcessRecord app) {
        if (this.mPausingActivity != null && this.mPausingActivity.app == app) {
            if (ActivityManagerDebugConfig.DEBUG_PAUSE || ActivityManagerDebugConfig.DEBUG_CLEANUP) {
                Slog.v(ActivityManagerService.TAG, "App died while pausing: " + this.mPausingActivity);
            }
            this.mPausingActivity = null;
        }
        if (this.mLastPausedActivity != null && this.mLastPausedActivity.app == app) {
            this.mLastPausedActivity = null;
            this.mLastNoHistoryActivity = null;
        }
        return removeHistoryRecordsForAppLocked(app);
    }

    /* access modifiers changed from: package-private */
    public void handleAppCrashLocked(ProcessRecord app) {
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            ArrayList<ActivityRecord> activities = this.mTaskHistory.get(taskNdx).mActivities;
            for (int activityNdx = activities.size() - 1; activityNdx >= 0; activityNdx--) {
                ActivityRecord r = null;
                if (activityNdx < activities.size()) {
                    r = activities.get(activityNdx);
                }
                if (r != null && r.app == app) {
                    Slog.w(ActivityManagerService.TAG, "  handleAppCrashLocked Force finishing activity " + r.intent.getComponent().flattenToShortString());
                    r.app = null;
                    this.mWindowManager.prepareAppTransition(26, false);
                    finishCurrentActivityLocked(r, 0, false, "handleAppCrashedLocked");
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean dumpActivitiesLocked(FileDescriptor fd, PrintWriter pw, boolean dumpAll, boolean dumpClient, String dumpPackage, boolean needSep) {
        PrintWriter printWriter = pw;
        if (this.mTaskHistory.isEmpty()) {
            return false;
        }
        int taskNdx = this.mTaskHistory.size() - 1;
        while (true) {
            int taskNdx2 = taskNdx;
            if (taskNdx2 < 0) {
                return true;
            }
            TaskRecord task = this.mTaskHistory.get(taskNdx2);
            if (needSep) {
                printWriter.println(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
            }
            printWriter.println("    Task id #" + task.taskId);
            printWriter.println("    mBounds=" + task.getOverrideBounds());
            printWriter.println("    mMinWidth=" + task.mMinWidth);
            printWriter.println("    mMinHeight=" + task.mMinHeight);
            printWriter.println("    mLastNonFullscreenBounds=" + task.mLastNonFullscreenBounds);
            printWriter.println("    * " + task);
            task.dump(printWriter, "      ");
            ActivityStackSupervisor.dumpHistoryList(fd, printWriter, this.mTaskHistory.get(taskNdx2).mActivities, "    ", "Hist", true, dumpAll ^ true, dumpClient, dumpPackage, false, null, task);
            taskNdx = taskNdx2 + -1;
        }
    }

    /* access modifiers changed from: package-private */
    public ArrayList<ActivityRecord> getDumpActivitiesLocked(String name) {
        ArrayList<ActivityRecord> activities = new ArrayList<>();
        if ("all".equals(name)) {
            for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
                activities.addAll(this.mTaskHistory.get(taskNdx).mActivities);
            }
        } else if ("top".equals(name)) {
            int top = this.mTaskHistory.size() - 1;
            if (top >= 0) {
                ArrayList<ActivityRecord> list = this.mTaskHistory.get(top).mActivities;
                int listTop = list.size() - 1;
                if (listTop >= 0) {
                    activities.add(list.get(listTop));
                }
            }
        } else {
            ActivityManagerService.ItemMatcher matcher = new ActivityManagerService.ItemMatcher();
            matcher.build(name);
            for (int taskNdx2 = this.mTaskHistory.size() - 1; taskNdx2 >= 0; taskNdx2--) {
                Iterator<ActivityRecord> it = this.mTaskHistory.get(taskNdx2).mActivities.iterator();
                while (it.hasNext()) {
                    ActivityRecord r1 = it.next();
                    if (matcher.match(r1, r1.intent.getComponent())) {
                        activities.add(r1);
                    }
                }
            }
        }
        return activities;
    }

    /* access modifiers changed from: package-private */
    public ActivityRecord restartPackage(String packageName) {
        ActivityRecord starting = topRunningActivityLocked();
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            ArrayList<ActivityRecord> activities = this.mTaskHistory.get(taskNdx).mActivities;
            for (int activityNdx = activities.size() - 1; activityNdx >= 0; activityNdx--) {
                ActivityRecord a = activities.get(activityNdx);
                if (a.info.packageName.equals(packageName)) {
                    a.forceNewConfig = true;
                    if (starting != null && a == starting && a.visible) {
                        a.startFreezingScreenLocked(starting.app, 256);
                    }
                }
            }
        }
        return starting;
    }

    /* access modifiers changed from: package-private */
    public void removeTask(TaskRecord task, String reason, int mode) {
        Iterator<ActivityRecord> it = task.mActivities.iterator();
        while (it.hasNext()) {
            onActivityRemovedFromStack(it.next());
        }
        Flog.i(101, "Task removed: " + task + ", reason: " + reason + ", mode: " + mode);
        boolean isVoiceSession = false;
        if (this.mTaskHistory.remove(task)) {
            EventLog.writeEvent(EventLogTags.AM_REMOVE_TASK, new Object[]{Integer.valueOf(task.taskId), Integer.valueOf(getStackId())});
        }
        removeActivitiesFromLRUListLocked(task);
        updateTaskMovement(task, true);
        if (mode == 0 && task.mActivities.isEmpty()) {
            if (task.voiceSession != null) {
                isVoiceSession = true;
            }
            if (isVoiceSession) {
                try {
                    task.voiceSession.taskFinished(task.intent, task.taskId);
                } catch (RemoteException e) {
                }
            }
            if (task.autoRemoveFromRecents() || isVoiceSession) {
                this.mStackSupervisor.mRecentTasks.remove(task);
            }
            task.removeWindowContainer();
        }
        if (this.mTaskHistory.isEmpty() && !reason.contains("swapDockedAndFullscreenStack")) {
            Flog.i(101, "removeTask: removing stack=" + this);
            if (isOnHomeDisplay() && mode != 2 && this.mStackSupervisor.isFocusedStack(this)) {
                String myReason = reason + " leftTaskHistoryEmpty";
                if (!inMultiWindowMode() || !adjustFocusToNextFocusableStack(myReason)) {
                    this.mStackSupervisor.moveHomeStackToFront(myReason);
                }
            }
            if (isAttached()) {
                getDisplay().positionChildAtBottom(this);
            }
            if (!isActivityTypeHome()) {
                remove();
            }
        }
        task.setStack(null);
        if (inPinnedWindowingMode()) {
            this.mService.mTaskChangeNotificationController.notifyActivityUnpinned();
            LogPower.push(NetworkManagementService.NetdResponseCode.ClatdStatusResult);
        }
    }

    /* access modifiers changed from: package-private */
    public TaskRecord createTaskRecord(int taskId, ActivityInfo info, Intent intent, IVoiceInteractionSession voiceSession, IVoiceInteractor voiceInteractor, boolean toTop) {
        return createTaskRecord(taskId, info, intent, voiceSession, voiceInteractor, toTop, null, null, null);
    }

    /* access modifiers changed from: package-private */
    public TaskRecord createTaskRecord(int taskId, ActivityInfo info, Intent intent, IVoiceInteractionSession voiceSession, IVoiceInteractor voiceInteractor, boolean toTop, ActivityRecord activity, ActivityRecord source, ActivityOptions options) {
        ActivityInfo activityInfo = info;
        boolean z = toTop;
        TaskRecord task = TaskRecord.create(this.mService, taskId, activityInfo, intent, voiceSession, voiceInteractor);
        addTask(task, z, "createTaskRecord");
        boolean z2 = false;
        boolean isLockscreenShown = this.mService.mStackSupervisor.getKeyguardController().isKeyguardOrAodShowing(this.mDisplayId != -1 ? this.mDisplayId : 0);
        if (!this.mStackSupervisor.getLaunchParamsController().layoutTask(task, activityInfo.windowLayout, activity, source, options) && !matchParentBounds() && task.isResizeable() && !isLockscreenShown) {
            task.updateOverrideConfiguration(getOverrideBounds());
        }
        if ((activityInfo.flags & 1024) != 0) {
            z2 = true;
        }
        task.createWindowContainer(z, z2);
        return task;
    }

    /* access modifiers changed from: package-private */
    public ArrayList<TaskRecord> getAllTasks() {
        return new ArrayList<>(this.mTaskHistory);
    }

    /* access modifiers changed from: package-private */
    public void addTask(TaskRecord task, boolean toTop, String reason) {
        addTask(task, toTop ? HwBootFail.STAGE_BOOT_SUCCESS : 0, true, reason);
        if (toTop) {
            this.mWindowContainerController.positionChildAtTop(task.getWindowContainerController(), true);
        }
    }

    /* access modifiers changed from: package-private */
    public void addTask(TaskRecord task, int position, boolean schedulePictureInPictureModeChange, String reason) {
        this.mTaskHistory.remove(task);
        int position2 = getAdjustedPositionForTask(task, position, null);
        boolean toTop = position2 >= this.mTaskHistory.size();
        ActivityStack prevStack = preAddTask(task, reason, toTop);
        this.mTaskHistory.add(position2, task);
        task.setStack(this);
        updateTaskMovement(task, toTop);
        postAddTask(task, prevStack, schedulePictureInPictureModeChange);
    }

    /* access modifiers changed from: package-private */
    public void positionChildAt(TaskRecord task, int index) {
        if (task.getStack() == this) {
            task.updateOverrideConfigurationForStack(this);
            ActivityRecord topRunningActivity = task.topRunningActivityLocked();
            boolean wasResumed = topRunningActivity == task.getStack().mResumedActivity;
            insertTaskAtPosition(task, index);
            task.setStack(this);
            postAddTask(task, null, true);
            if (wasResumed) {
                if (this.mResumedActivity != null) {
                    Log.wtf(ActivityManagerService.TAG, "mResumedActivity was already set when moving mResumedActivity from other stack to this stack mResumedActivity=" + this.mResumedActivity + " other mResumedActivity=" + topRunningActivity);
                }
                topRunningActivity.setState(ActivityState.RESUMED, "positionChildAt");
            }
            ensureActivitiesVisibleLocked(null, 0, false);
            this.mStackSupervisor.resumeFocusedStackTopActivityLocked();
            return;
        }
        throw new IllegalArgumentException("AS.positionChildAt: task=" + task + " is not a child of stack=" + this + " current parent=" + task.getStack());
    }

    private ActivityStack preAddTask(TaskRecord task, String reason, boolean toTop) {
        ActivityStack prevStack = task.getStack();
        if (!(prevStack == null || prevStack == this)) {
            prevStack.removeTask(task, reason, toTop ? 2 : 1);
        }
        return prevStack;
    }

    private void postAddTask(TaskRecord task, ActivityStack prevStack, boolean schedulePictureInPictureModeChange) {
        if (schedulePictureInPictureModeChange && prevStack != null) {
            this.mStackSupervisor.scheduleUpdatePictureInPictureModeIfNeeded(task, prevStack);
        } else if (task.voiceSession != null) {
            try {
                task.voiceSession.taskStarted(task.intent, task.taskId);
            } catch (RemoteException e) {
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void moveToFrontAndResumeStateIfNeeded(ActivityRecord r, boolean moveToFront, boolean setResume, boolean setPause, String reason) {
        if (moveToFront) {
            if (setResume) {
                r.setState(ActivityState.RESUMED, "moveToFrontAndResumeStateIfNeeded");
                updateLRUListLocked(r);
            }
            if (setPause) {
                this.mPausingActivity = r;
                schedulePauseTimeout(r);
            }
            moveToFront(reason);
        }
    }

    public int getStackId() {
        return this.mStackId;
    }

    public String toString() {
        return "ActivityStack{" + Integer.toHexString(System.identityHashCode(this)) + " stackId=" + this.mStackId + " type=" + WindowConfiguration.activityTypeToString(getActivityType()) + " mode=" + WindowConfiguration.windowingModeToString(getWindowingMode()) + " visible=" + shouldBeVisible(null) + " translucent=" + isStackTranslucent(null) + ", " + this.mTaskHistory.size() + " tasks}";
    }

    public String toShortString() {
        return "ActivityStack{" + Integer.toHexString(System.identityHashCode(this)) + " stackId=" + this.mStackId + " type=" + WindowConfiguration.activityTypeToString(getActivityType()) + " mode=" + WindowConfiguration.windowingModeToString(getWindowingMode()) + ", " + this.mTaskHistory.size() + " tasks}";
    }

    /* access modifiers changed from: package-private */
    public void onLockTaskPackagesUpdated() {
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            this.mTaskHistory.get(taskNdx).setLockTaskAuth();
        }
    }

    /* access modifiers changed from: package-private */
    public void executeAppTransition(ActivityOptions options) {
        this.mWindowManager.executeAppTransition();
        ActivityOptions.abort(options);
    }

    /* access modifiers changed from: package-private */
    public boolean shouldSleepActivities() {
        ActivityDisplay display = getDisplay();
        if (this.mStackSupervisor.getFocusedStack() != this || !this.mStackSupervisor.getKeyguardController().isKeyguardGoingAway()) {
            return display != null ? display.isSleeping() : this.mService.isSleepingLocked();
        }
        if (ActivityManagerDebugConfig.DEBUG_KEYGUARD) {
            Flog.i(107, "Skip sleeping activities for keyguard is in the process of going away");
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean shouldSleepOrShutDownActivities() {
        return shouldSleepActivities() || this.mService.isShuttingDownLocked();
    }

    public void writeToProto(ProtoOutputStream proto, long fieldId) {
        long token = proto.start(fieldId);
        super.writeToProto(proto, 1146756268033L, false);
        proto.write(1120986464258L, this.mStackId);
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            this.mTaskHistory.get(taskNdx).writeToProto(proto, 2246267895811L);
        }
        if (this.mResumedActivity != null) {
            this.mResumedActivity.writeIdentifierToProto(proto, 1146756268036L);
        }
        proto.write(1120986464261L, this.mDisplayId);
        if (!matchParentBounds()) {
            getOverrideBounds().writeToProto(proto, 1146756268039L);
        }
        proto.write(1133871366150L, matchParentBounds());
        proto.end(token);
    }

    /* access modifiers changed from: protected */
    public void resetOtherStacksVisible(boolean visible) {
    }

    public void setFreeFormStackVisible(boolean visible) {
        if (visible && !this.mIsFreeFormStackVisible) {
            this.mCurrentTime = System.currentTimeMillis();
        } else if (!visible && this.mIsFreeFormStackVisible && this.mCurrentTime > 0 && getTopActivity() != null) {
            Context context = this.mService.mContext;
            Flog.bdReport(context, 10066, "{ pkg:" + getTopActivity().packageName + ",currentTime:" + (System.currentTimeMillis() - this.mCurrentTime) + "}");
            this.mCurrentTime = 0;
        }
        HwFreeFormUtils.setFreeFormStackVisible(visible);
        this.mIsFreeFormStackVisible = visible;
    }

    public boolean getFreeFormStackVisible() {
        return this.mIsFreeFormStackVisible;
    }

    public void setCurrentPkgUnderFreeForm(String pgkName) {
        this.mCurrentPkgUnderFreeForm = pgkName;
    }

    public String getCurrentPkgUnderFreeForm() {
        return this.mCurrentPkgUnderFreeForm;
    }

    /* access modifiers changed from: package-private */
    public ArrayList<TaskRecord> getTaskHistory() {
        return this.mTaskHistory;
    }
}
