package com.android.server.am;

import android.app.ActivityManager.RunningTaskInfo;
import android.app.ActivityManager.StackId;
import android.app.ActivityOptions;
import android.app.AppGlobals;
import android.app.IActivityController;
import android.app.IApplicationThread;
import android.app.ResultInfo;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ActivityInfo.WindowLayout;
import android.content.pm.ApplicationInfo;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.Trace;
import android.os.UserHandle;
import android.service.voice.IVoiceInteractionSession;
import android.util.ArraySet;
import android.util.BoostFramework;
import android.util.EventLog;
import android.util.Flog;
import android.util.HwPCUtils;
import android.util.HwSlog;
import android.util.IntArray;
import android.util.Jlog;
import android.util.Log;
import android.util.Slog;
import android.util.SparseArray;
import com.android.internal.app.IVoiceInteractor;
import com.android.internal.os.BatteryStatsImpl;
import com.android.internal.os.BatteryStatsImpl.Uid.Proc;
import com.android.server.HwServiceFactory;
import com.android.server.Watchdog;
import com.android.server.am.ActivityStackSupervisor.ActivityContainer;
import com.android.server.connectivity.LingerMonitor;
import com.android.server.job.controllers.JobStatus;
import com.android.server.os.HwBootFail;
import com.android.server.wm.StackWindowController;
import com.android.server.wm.StackWindowListener;
import com.android.server.wm.WindowManagerService;
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
    private static final /* synthetic */ int[] -com-android-server-am-ActivityStack$ActivityStateSwitchesValues = null;
    private static final long ACTIVITY_INACTIVE_RESET_TIME = 0;
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
    static final int RELEASE_BACKGROUND_RESOURCES_TIMEOUT_MSG = 107;
    protected static final int REMOVE_TASK_MODE_DESTROYING = 0;
    static final int REMOVE_TASK_MODE_MOVING = 1;
    static final int REMOVE_TASK_MODE_MOVING_TO_TOP = 2;
    private static final String SETTINGS_DASHBROAED_ACTIVITY_NAME = "com.android.settings.Settings$AppAndNotificationDashboardActivity";
    private static final boolean SHOW_APP_STARTING_PREVIEW = true;
    static final int STACK_INVISIBLE = 0;
    static final int STACK_VISIBLE = 1;
    static final int STACK_VISIBLE_ACTIVITY_BEHIND = 2;
    private static final int STOP_TIMEOUT = 10000;
    static final int STOP_TIMEOUT_MSG = 104;
    protected static final String TAG = "ActivityManager";
    private static final String TAG_ADD_REMOVE = (TAG + ActivityManagerDebugConfig.POSTFIX_ADD_REMOVE);
    private static final String TAG_APP = (TAG + ActivityManagerDebugConfig.POSTFIX_APP);
    private static final String TAG_CLEANUP = (TAG + ActivityManagerDebugConfig.POSTFIX_CLEANUP);
    private static final String TAG_CONTAINERS = (TAG + ActivityManagerDebugConfig.POSTFIX_CONTAINERS);
    private static final String TAG_PAUSE = (TAG + ActivityManagerDebugConfig.POSTFIX_PAUSE);
    private static final String TAG_RELEASE = (TAG + ActivityManagerDebugConfig.POSTFIX_RELEASE);
    private static final String TAG_RESULTS = (TAG + ActivityManagerDebugConfig.POSTFIX_RESULTS);
    private static final String TAG_SAVED_STATE = (TAG + ActivityManagerDebugConfig.POSTFIX_SAVED_STATE);
    private static final String TAG_STACK = (TAG + ActivityManagerDebugConfig.POSTFIX_STACK);
    private static final String TAG_STATES = (TAG + ActivityManagerDebugConfig.POSTFIX_STATES);
    private static final String TAG_SWITCH = (TAG + ActivityManagerDebugConfig.POSTFIX_SWITCH);
    private static final String TAG_TASKS = (TAG + ActivityManagerDebugConfig.POSTFIX_TASKS);
    private static final String TAG_TRANSITION = (TAG + ActivityManagerDebugConfig.POSTFIX_TRANSITION);
    private static final String TAG_USER_LEAVING = (TAG + ActivityManagerDebugConfig.POSTFIX_USER_LEAVING);
    private static final String TAG_VISIBILITY = (TAG + ActivityManagerDebugConfig.POSTFIX_VISIBILITY);
    private static final long TRANSLUCENT_CONVERSION_TIMEOUT = 2000;
    static final int TRANSLUCENT_TIMEOUT_MSG = 106;
    protected static final boolean VALIDATE_TOKENS = false;
    public int[] aBoostParamVal;
    public int aBoostTimeOut = 0;
    public int[] lBoostCpuParamVal;
    public int lBoostTimeOut = 0;
    final ActivityContainer mActivityContainer;
    Rect mBounds = null;
    boolean mConfigWillChange;
    int mCurrentUser;
    private final Rect mDeferredBounds = new Rect();
    private final Rect mDeferredTaskBounds = new Rect();
    private final Rect mDeferredTaskInsetBounds = new Rect();
    int mDisplayId;
    private boolean mFirstDockedWithoutRecent = true;
    boolean mForceHidden = false;
    boolean mFullscreen = true;
    long mFullyDrawnStartTime = 0;
    final Handler mHandler;
    public boolean mIsAnimationBoostEnabled = false;
    public boolean mIsPerfBoostEnabled = false;
    final ArrayList<ActivityRecord> mLRUActivities = new ArrayList();
    ActivityRecord mLastNoHistoryActivity = null;
    ActivityRecord mLastPausedActivity = null;
    long mLaunchStartTime = 0;
    final ArrayList<ActivityRecord> mNoAnimActivities = new ArrayList();
    ActivityRecord mPausingActivity = null;
    public BoostFramework mPerf = null;
    public BoostFramework mPerfBoost = null;
    private final RecentTasks mRecentTasks;
    ActivityRecord mResumedActivity = null;
    final ActivityManagerService mService;
    final int mStackId;
    protected final ActivityStackSupervisor mStackSupervisor;
    ArrayList<ActivityStack> mStacks;
    protected ArrayList<TaskRecord> mTaskHistory = new ArrayList();
    private final LaunchingTaskPositioner mTaskPositioner;
    private final SparseArray<Rect> mTmpBounds = new SparseArray();
    private final SparseArray<Configuration> mTmpConfigs = new SparseArray();
    private final SparseArray<Rect> mTmpInsetBounds = new SparseArray();
    private final Rect mTmpRect2 = new Rect();
    private boolean mTopActivityOccludesKeyguard;
    private ActivityRecord mTopDismissingKeyguardActivity;
    ActivityRecord mTranslucentActivityWaiting = null;
    ArrayList<ActivityRecord> mUndrawnActivitiesBelowTopTranslucent = new ArrayList();
    private boolean mUpdateBoundsDeferred;
    private boolean mUpdateBoundsDeferredCalled;
    T mWindowContainerController;
    private final WindowManagerService mWindowManager;
    String mshortComponentName = "";

    private class ActivityStackHandler extends Handler {
        ActivityStackHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            IBinder iBinder = null;
            ActivityRecord r;
            switch (msg.what) {
                case 101:
                    r = msg.obj;
                    Slog.w(ActivityStack.TAG, "Activity pause timeout for " + r);
                    synchronized (ActivityStack.this.mService) {
                        try {
                            ActivityManagerService.boostPriorityForLockedSection();
                            if (r.app != null) {
                                ActivityStack.this.mService.logAppTooSlow(r.app, r.pauseTime, "pausing " + r);
                            }
                            ActivityStack.this.activityPausedLocked(r.appToken, true);
                        } finally {
                            ActivityManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                    return;
                case 102:
                    r = (ActivityRecord) msg.obj;
                    Slog.w(ActivityStack.TAG, "Activity destroy timeout for " + r);
                    synchronized (ActivityStack.this.mService) {
                        try {
                            ActivityManagerService.boostPriorityForLockedSection();
                            ActivityStack activityStack = ActivityStack.this;
                            if (r != null) {
                                iBinder = r.appToken;
                            }
                            activityStack.activityDestroyedLocked(iBinder, "destroyTimeout");
                        } finally {
                            ActivityManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                    return;
                case 103:
                    r = (ActivityRecord) msg.obj;
                    synchronized (ActivityStack.this.mService) {
                        try {
                            ActivityManagerService.boostPriorityForLockedSection();
                            if (r.continueLaunchTickingLocked()) {
                                ActivityStack.this.mService.logAppTooSlow(r.app, r.launchTickTime, "launching " + r);
                            }
                        } finally {
                            ActivityManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                    return;
                case 104:
                    r = (ActivityRecord) msg.obj;
                    Slog.w(ActivityStack.TAG, "Activity stop timeout for " + r);
                    synchronized (ActivityStack.this.mService) {
                        try {
                            ActivityManagerService.boostPriorityForLockedSection();
                            if (r.isInHistory()) {
                                r.activityStoppedLocked(null, null, null);
                            }
                        } finally {
                            ActivityManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                    return;
                case 105:
                    ScheduleDestroyArgs args = msg.obj;
                    synchronized (ActivityStack.this.mService) {
                        try {
                            ActivityManagerService.boostPriorityForLockedSection();
                            ActivityStack.this.destroyActivitiesLocked(args.mOwner, args.mReason);
                        } finally {
                            ActivityManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                    return;
                case 106:
                    synchronized (ActivityStack.this.mService) {
                        try {
                            ActivityManagerService.boostPriorityForLockedSection();
                            ActivityStack.this.notifyActivityDrawnLocked(null);
                        } finally {
                            ActivityManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                    return;
                case 107:
                    synchronized (ActivityStack.this.mService) {
                        try {
                            ActivityManagerService.boostPriorityForLockedSection();
                            r = ActivityStack.this.getVisibleBehindActivity();
                            Slog.e(ActivityStack.TAG, "Timeout waiting for cancelVisibleBehind player=" + r);
                            if (r != null) {
                                ActivityStack.this.mService.killAppAtUsersRequest(r.app, null);
                            }
                        } finally {
                            ActivityManagerService.resetPriorityAfterLockedSection();
                        }
                    }
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

    private static /* synthetic */ int[] -getcom-android-server-am-ActivityStack$ActivityStateSwitchesValues() {
        if (-com-android-server-am-ActivityStack$ActivityStateSwitchesValues != null) {
            return -com-android-server-am-ActivityStack$ActivityStateSwitchesValues;
        }
        int[] iArr = new int[ActivityState.values().length];
        try {
            iArr[ActivityState.DESTROYED.ordinal()] = 7;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[ActivityState.DESTROYING.ordinal()] = 8;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[ActivityState.FINISHING.ordinal()] = 9;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[ActivityState.INITIALIZING.ordinal()] = 1;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[ActivityState.PAUSED.ordinal()] = 2;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[ActivityState.PAUSING.ordinal()] = 3;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[ActivityState.RESUMED.ordinal()] = 4;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[ActivityState.STOPPED.ordinal()] = 5;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[ActivityState.STOPPING.ordinal()] = 6;
        } catch (NoSuchFieldError e9) {
        }
        -com-android-server-am-ActivityStack$ActivityStateSwitchesValues = iArr;
        return iArr;
    }

    protected int getChildCount() {
        return this.mTaskHistory.size();
    }

    protected ConfigurationContainer getChildAt(int index) {
        return (ConfigurationContainer) this.mTaskHistory.get(index);
    }

    protected ConfigurationContainer getParent() {
        return this.mActivityContainer.mActivityDisplay;
    }

    void onParentChanged() {
        super.onParentChanged();
        this.mStackSupervisor.updateUIDsPresentOnDisplay();
    }

    int numActivities() {
        int count = 0;
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            count += ((TaskRecord) this.mTaskHistory.get(taskNdx)).mActivities.size();
        }
        return count;
    }

    public ActivityStack(ActivityContainer activityContainer, RecentTasks recentTasks, boolean onTop) {
        LaunchingTaskPositioner launchingTaskPositioner;
        Rect rect = null;
        this.mActivityContainer = activityContainer;
        this.mStackSupervisor = activityContainer.getOuter();
        this.mService = this.mStackSupervisor.mService;
        this.mHandler = new ActivityStackHandler(this.mService.mHandler.getLooper());
        this.mWindowManager = this.mService.mWindowManager;
        this.mStackId = activityContainer.mStackId;
        this.mCurrentUser = this.mService.mUserController.getCurrentUserIdLocked();
        this.mRecentTasks = recentTasks;
        if (this.mStackId == 2) {
            launchingTaskPositioner = new LaunchingTaskPositioner();
        } else {
            launchingTaskPositioner = null;
        }
        this.mTaskPositioner = launchingTaskPositioner;
        ActivityDisplay display = this.mActivityContainer.mActivityDisplay;
        this.mTmpRect2.setEmpty();
        this.mWindowContainerController = createStackWindowController(display.mDisplayId, onTop, this.mTmpRect2);
        activityContainer.mStack = this;
        this.mStackSupervisor.mActivityContainers.put(this.mStackId, activityContainer);
        if (!this.mTmpRect2.isEmpty()) {
            rect = this.mTmpRect2;
        }
        postAddToDisplay(display, rect, onTop);
        this.mIsAnimationBoostEnabled = this.mService.mContext.getResources().getBoolean(17956957);
        if (this.mIsAnimationBoostEnabled) {
            this.aBoostTimeOut = this.mService.mContext.getResources().getInteger(17694725);
            this.aBoostParamVal = this.mService.mContext.getResources().getIntArray(17235975);
        }
        this.mIsPerfBoostEnabled = this.mService.mContext.getResources().getBoolean(17956944);
        if (this.mIsPerfBoostEnabled) {
            this.lBoostTimeOut = this.mService.mContext.getResources().getInteger(17694724);
            this.lBoostCpuParamVal = this.mService.mContext.getResources().getIntArray(17235974);
        }
    }

    T createStackWindowController(int displayId, boolean onTop, Rect outBounds) {
        return new StackWindowController(this.mStackId, this, displayId, onTop, outBounds);
    }

    T getWindowContainerController() {
        return this.mWindowContainerController;
    }

    void reparent(ActivityDisplay activityDisplay, boolean onTop) {
        removeFromDisplay();
        this.mTmpRect2.setEmpty();
        postAddToDisplay(activityDisplay, this.mTmpRect2.isEmpty() ? null : this.mTmpRect2, onTop);
        adjustFocusToNextFocusableStackLocked("reparent", true);
        this.mStackSupervisor.resumeFocusedStackTopActivityLocked();
        this.mStackSupervisor.ensureActivitiesVisibleLocked(null, 0, false);
        this.mWindowContainerController.reparent(activityDisplay.mDisplayId, this.mTmpRect2, onTop);
    }

    private void postAddToDisplay(ActivityDisplay activityDisplay, Rect bounds, boolean onTop) {
        this.mDisplayId = activityDisplay.mDisplayId;
        this.mStacks = activityDisplay.mStacks;
        this.mBounds = bounds != null ? new Rect(bounds) : null;
        this.mFullscreen = this.mBounds == null;
        if (this.mTaskPositioner != null) {
            this.mTaskPositioner.setDisplay(activityDisplay.mDisplay);
            this.mTaskPositioner.configure(this.mBounds);
        }
        onParentChanged();
        activityDisplay.attachStack(this, findStackInsertIndex(onTop));
        if (this.mStackId == 3) {
            this.mStackSupervisor.resizeDockedStackLocked(this.mBounds, null, null, null, null, true);
        }
    }

    private void removeFromDisplay() {
        this.mDisplayId = -1;
        this.mStacks = null;
        if (this.mTaskPositioner != null) {
            this.mTaskPositioner.reset();
        }
        if (this.mStackId == 3) {
            this.mStackSupervisor.resizeDockedStackLocked(null, null, null, null, null, true);
        }
    }

    void remove() {
        removeFromDisplay();
        this.mStackSupervisor.deleteActivityContainerRecord(this.mStackId);
        this.mWindowContainerController.removeContainer();
        this.mWindowContainerController = null;
        onParentChanged();
    }

    void getDisplaySize(Point out) {
        this.mActivityContainer.mActivityDisplay.mDisplay.getSize(out);
    }

    void getStackDockedModeBounds(Rect currentTempTaskBounds, Rect outStackBounds, Rect outTempTaskBounds, boolean ignoreVisibility) {
        this.mWindowContainerController.getStackDockedModeBounds(currentTempTaskBounds, outStackBounds, outTempTaskBounds, ignoreVisibility);
    }

    void prepareFreezingTaskBounds() {
        this.mWindowContainerController.prepareFreezingTaskBounds();
    }

    void getWindowContainerBounds(Rect outBounds) {
        if (this.mWindowContainerController != null) {
            this.mWindowContainerController.getBounds(outBounds);
        } else {
            outBounds.setEmpty();
        }
    }

    void getBoundsForNewConfiguration(Rect outBounds) {
        this.mWindowContainerController.getBoundsForNewConfiguration(outBounds);
    }

    void positionChildWindowContainerAtTop(TaskRecord child) {
        this.mWindowContainerController.positionChildAtTop(child.getWindowContainerController(), true);
    }

    boolean deferScheduleMultiWindowModeChanged() {
        return false;
    }

    void deferUpdateBounds() {
        if (!this.mUpdateBoundsDeferred) {
            this.mUpdateBoundsDeferred = true;
            this.mUpdateBoundsDeferredCalled = false;
        }
    }

    void continueUpdateBounds() {
        Rect rect = null;
        boolean wasDeferred = this.mUpdateBoundsDeferred;
        this.mUpdateBoundsDeferred = false;
        if (wasDeferred && this.mUpdateBoundsDeferredCalled) {
            Rect rect2 = this.mDeferredBounds.isEmpty() ? null : this.mDeferredBounds;
            Rect rect3 = this.mDeferredTaskBounds.isEmpty() ? null : this.mDeferredTaskBounds;
            if (!this.mDeferredTaskInsetBounds.isEmpty()) {
                rect = this.mDeferredTaskInsetBounds;
            }
            resize(rect2, rect3, rect);
        }
    }

    boolean updateBoundsAllowed(Rect bounds, Rect tempTaskBounds, Rect tempTaskInsetBounds) {
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

    void setBounds(Rect bounds) {
        Rect rect = null;
        if (!this.mFullscreen) {
            rect = new Rect(bounds);
        }
        this.mBounds = rect;
        if (this.mTaskPositioner != null) {
            this.mTaskPositioner.configure(bounds);
        }
    }

    ActivityRecord topRunningActivityLocked() {
        return topRunningActivityLocked(false);
    }

    private ActivityRecord topRunningActivityLocked(boolean focusableOnly) {
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            ActivityRecord r = ((TaskRecord) this.mTaskHistory.get(taskNdx)).topRunningActivityLocked();
            if (r != null && (!focusableOnly || r.isFocusable())) {
                return r;
            }
        }
        return null;
    }

    ActivityRecord topRunningNonOverlayTaskActivity() {
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            ArrayList<ActivityRecord> activities = ((TaskRecord) this.mTaskHistory.get(taskNdx)).mActivities;
            for (int activityNdx = activities.size() - 1; activityNdx >= 0; activityNdx--) {
                ActivityRecord r = (ActivityRecord) activities.get(activityNdx);
                if (!r.finishing && (r.mTaskOverlay ^ 1) != 0) {
                    return r;
                }
            }
        }
        return null;
    }

    ActivityRecord topRunningNonDelayedActivityLocked(ActivityRecord notTop) {
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            ArrayList<ActivityRecord> activities = ((TaskRecord) this.mTaskHistory.get(taskNdx)).mActivities;
            for (int activityNdx = activities.size() - 1; activityNdx >= 0; activityNdx--) {
                ActivityRecord r = (ActivityRecord) activities.get(activityNdx);
                if (!r.finishing && (r.delayedResume ^ 1) != 0 && r != notTop && r.okToShowLocked()) {
                    return r;
                }
            }
        }
        return null;
    }

    final ActivityRecord topRunningActivityLocked(IBinder token, int taskId) {
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            TaskRecord task = (TaskRecord) this.mTaskHistory.get(taskNdx);
            if (task.taskId != taskId) {
                ArrayList<ActivityRecord> activities = task.mActivities;
                for (int i = activities.size() - 1; i >= 0; i--) {
                    ActivityRecord r = (ActivityRecord) activities.get(i);
                    if (!r.finishing && token != r.appToken && r.okToShowLocked()) {
                        return r;
                    }
                }
                continue;
            }
        }
        return null;
    }

    final ActivityRecord topActivity() {
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            ArrayList<ActivityRecord> activities = ((TaskRecord) this.mTaskHistory.get(taskNdx)).mActivities;
            for (int activityNdx = activities.size() - 1; activityNdx >= 0; activityNdx--) {
                ActivityRecord r = (ActivityRecord) activities.get(activityNdx);
                if (!r.finishing) {
                    return r;
                }
            }
        }
        return null;
    }

    final TaskRecord topTask() {
        int size = this.mTaskHistory.size();
        if (size > 0) {
            return (TaskRecord) this.mTaskHistory.get(size - 1);
        }
        return null;
    }

    TaskRecord taskForIdLocked(int id) {
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            TaskRecord task = (TaskRecord) this.mTaskHistory.get(taskNdx);
            if (task.taskId == id) {
                return task;
            }
        }
        return null;
    }

    ActivityRecord isInStackLocked(IBinder token) {
        return isInStackLocked(ActivityRecord.forTokenLocked(token));
    }

    ActivityRecord isInStackLocked(ActivityRecord r) {
        if (r == null) {
            return null;
        }
        TaskRecord task = r.getTask();
        ActivityStack stack = r.getStack();
        if (stack == null || !task.mActivities.contains(r) || !this.mTaskHistory.contains(task)) {
            return null;
        }
        if (stack != this) {
            Slog.w(TAG, "Illegal state! task does not point to stack it is in.");
        }
        return r;
    }

    boolean isInStackLocked(TaskRecord task) {
        return this.mTaskHistory.contains(task);
    }

    boolean isUidPresent(int uid) {
        for (TaskRecord task : this.mTaskHistory) {
            for (ActivityRecord r : task.mActivities) {
                if (r.getUid() == uid) {
                    return true;
                }
            }
        }
        return false;
    }

    void getPresentUIDs(IntArray presentUIDs) {
        for (TaskRecord task : this.mTaskHistory) {
            for (ActivityRecord r : task.mActivities) {
                presentUIDs.add(r.getUid());
            }
        }
    }

    final void removeActivitiesFromLRUListLocked(TaskRecord task) {
        for (ActivityRecord r : task.mActivities) {
            this.mLRUActivities.remove(r);
        }
    }

    final boolean updateLRUListLocked(ActivityRecord r) {
        boolean hadit = this.mLRUActivities.remove(r);
        this.mLRUActivities.add(r);
        return hadit;
    }

    final boolean isHomeStack() {
        return this.mStackId == 0;
    }

    final boolean isRecentsStack() {
        return this.mStackId == 5;
    }

    final boolean isHomeOrRecentsStack() {
        return StackId.isHomeOrRecentsStack(this.mStackId);
    }

    final boolean isDockedStack() {
        return this.mStackId == 3;
    }

    final boolean isPinnedStack() {
        return this.mStackId == 4;
    }

    final boolean isAssistantStack() {
        return this.mStackId == 6;
    }

    final boolean isOnHomeDisplay() {
        if (isAttached() && this.mActivityContainer.mActivityDisplay.mDisplayId == 0) {
            return true;
        }
        return false;
    }

    void moveToFront(String reason) {
        moveToFront(reason, null);
    }

    protected void moveToFront(String reason, TaskRecord task) {
        if (isAttached()) {
            this.mStacks.remove(this);
            this.mStacks.add(findStackInsertIndex(true), this);
            this.mStackSupervisor.setFocusStackUnchecked(reason, this);
            if (task != null) {
                insertTaskAtTop(task, null);
                return;
            }
            task = topTask();
            if (task != null) {
                this.mWindowContainerController.positionChildAtTop(task.getWindowContainerController(), true);
            }
        }
    }

    private void moveToBack(TaskRecord task) {
        if (isAttached()) {
            this.mStacks.remove(this);
            this.mStacks.add(0, this);
            if (task != null) {
                this.mTaskHistory.remove(task);
                this.mTaskHistory.add(0, task);
                updateTaskMovement(task, false);
                this.mWindowContainerController.positionChildAtBottom(task.getWindowContainerController());
            }
        }
    }

    private int findStackInsertIndex(boolean onTop) {
        if (!onTop) {
            return 0;
        }
        int addIndex = this.mStacks.size();
        if (addIndex > 0) {
            ActivityStack topStack = (ActivityStack) this.mStacks.get(addIndex - 1);
            if (StackId.isAlwaysOnTop(topStack.mStackId) && topStack != this) {
                addIndex--;
            }
        }
        return addIndex;
    }

    boolean isFocusable() {
        if (StackId.canReceiveKeys(this.mStackId)) {
            return true;
        }
        ActivityRecord r = topRunningActivityLocked();
        return r != null ? r.isFocusable() : false;
    }

    final boolean isAttached() {
        return this.mStacks != null;
    }

    /* JADX WARNING: Missing block: B:37:0x00ee, code:
            if (((r17.mStackSupervisor.isCurrentProfileLocked(r7.userId) ? r17.mStackSupervisor.isCurrentProfileLocked(r13) : 0) ^ 1) == 0) goto L_0x00f0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void findTaskLocked(ActivityRecord target, FindTaskResult result) {
        Intent intent = target.intent;
        ActivityInfo info = target.info;
        ComponentName cls = intent.getComponent();
        if (info.targetActivity != null) {
            cls = new ComponentName(info.packageName, info.targetActivity);
        }
        int userId = UserHandle.getUserId(info.applicationInfo.uid);
        boolean isDocument = (intent != null ? 1 : 0) & intent.isDocument();
        Object documentData = isDocument ? intent.getData() : null;
        if (ActivityManagerDebugConfig.DEBUG_TASKS) {
            Slog.d(TAG_TASKS, "Looking for task of " + target + " in " + this);
        }
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            TaskRecord task = (TaskRecord) this.mTaskHistory.get(taskNdx);
            if (task.voiceSession != null) {
                if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                    Slog.d(TAG_TASKS, "Skipping " + task + ": voice session");
                }
            } else if (task.userId == userId) {
                ActivityRecord r = task.getTopActivity();
                if (!(r == null || r.finishing)) {
                    if (r.userId != userId) {
                    }
                    if (r.launchMode != 3) {
                        if (r.mActivityType == target.mActivityType) {
                            Intent taskIntent = task.intent;
                            Intent affinityIntent = task.affinityIntent;
                            boolean taskIsDocument;
                            Object taskDocumentData;
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
                            if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                                Slog.d(TAG_TASKS, "Comparing existing cls=" + taskIntent.getComponent().flattenToShortString() + "/aff=" + r.getTask().rootAffinity + " to new cls=" + intent.getComponent().flattenToShortString() + "/aff=" + info.taskAffinity);
                            }
                            if (taskIntent != null && taskIntent.getComponent() != null && taskIntent.getComponent().compareTo(cls) == 0 && Objects.equals(documentData, taskDocumentData)) {
                                if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                                    Slog.d(TAG_TASKS, "Found matching class!");
                                }
                                if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                                    Slog.d(TAG_TASKS, "For Intent " + intent + " bringing to top: " + r.intent);
                                }
                                result.r = r;
                                result.matchedByRootAffinity = false;
                                return;
                            } else if (affinityIntent != null && affinityIntent.getComponent() != null && affinityIntent.getComponent().compareTo(cls) == 0 && Objects.equals(documentData, taskDocumentData)) {
                                if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                                    Slog.d(TAG_TASKS, "Found matching class!");
                                }
                                if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                                    Slog.d(TAG_TASKS, "For Intent " + intent + " bringing to top: " + r.intent);
                                }
                                result.r = r;
                                result.matchedByRootAffinity = false;
                                return;
                            } else if (isDocument || (taskIsDocument ^ 1) == 0 || result.r != null || task.rootAffinity == null) {
                                if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                                    Slog.d(TAG_TASKS, "Not a match: " + task);
                                }
                            } else if (task.rootAffinity.equals(target.taskAffinity)) {
                                if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                                    Slog.d(TAG_TASKS, "Found matching affinity candidate!");
                                }
                                result.r = r;
                                result.matchedByRootAffinity = true;
                            }
                        } else if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                            Slog.d(TAG_TASKS, "Skipping " + task + ": mismatch activity type");
                        }
                    }
                }
                if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                    Slog.d(TAG_TASKS, "Skipping " + task + ": mismatch root " + r);
                }
            } else if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                Slog.d(TAG_TASKS, "Skipping " + task + ": different user");
            }
        }
    }

    ActivityRecord findActivityLocked(Intent intent, ActivityInfo info, boolean compareIntentFilters) {
        ComponentName cls = intent.getComponent();
        if (info.targetActivity != null) {
            cls = new ComponentName(info.packageName, info.targetActivity);
        }
        int userId = UserHandle.getUserId(info.applicationInfo.uid);
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            ArrayList<ActivityRecord> activities = ((TaskRecord) this.mTaskHistory.get(taskNdx)).mActivities;
            for (int activityNdx = activities.size() - 1; activityNdx >= 0; activityNdx--) {
                ActivityRecord r = (ActivityRecord) activities.get(activityNdx);
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

    final void switchUserLocked(int userId) {
        if (this.mCurrentUser != userId) {
            this.mCurrentUser = userId;
            int index = this.mTaskHistory.size();
            int i = 0;
            while (i < index) {
                TaskRecord task = (TaskRecord) this.mTaskHistory.get(i);
                ensureActivitiesVisibleLockedForSwitchUser(task);
                if (task.okToShowLocked()) {
                    if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                        Slog.d(TAG_TASKS, "switchUserLocked: stack=" + getStackId() + " moving " + task + " to top");
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

    void ensureActivitiesVisibleLockedForSwitchUser(TaskRecord task) {
        if (!this.mStackSupervisor.isCurrentProfileLocked(task.userId)) {
            ActivityRecord top = task.getTopActivity();
            if (top != null && top != task.topRunningActivityLocked() && top.visible) {
                if (top.state == ActivityState.STOPPING || top.state == ActivityState.STOPPED) {
                    Flog.i(101, "Making invisible for switch user:  top: " + top + ", finishing: " + top.finishing + " state: " + top.state);
                    try {
                        top.setVisible(false);
                        switch (-getcom-android-server-am-ActivityStack$ActivityStateSwitchesValues()[top.state.ordinal()]) {
                            case 5:
                            case 6:
                                if (top.app != null && top.app.thread != null) {
                                    top.app.thread.scheduleWindowVisibility(top.appToken, false);
                                    return;
                                }
                                return;
                            default:
                                return;
                        }
                    } catch (Exception e) {
                        Slog.w(TAG, "for switch user Exception thrown making hidden: " + top.intent.getComponent(), e);
                    }
                }
            }
        }
    }

    void minimalResumeActivityLocked(ActivityRecord r) {
        if (ActivityManagerDebugConfig.DEBUG_STATES) {
            Slog.v(TAG_STATES, "Moving to RESUMED: " + r + " (starting new instance)" + " callers=" + Debug.getCallers(5));
        }
        if (!this.mService.mActivityStarter.mCurActivityPkName.equals(r.packageName)) {
            Jlog.d(142, r.packageName, r.app.pid, "");
            LogPower.push(113, r.packageName);
            this.mService.mActivityStarter.mCurActivityPkName = r.packageName;
        }
        setResumedActivityLocked(r, "minimalResumeActivityLocked");
        r.completeResumeLocked();
        setLaunchTime(r);
        if (ActivityManagerDebugConfig.DEBUG_SAVED_STATE) {
            Slog.i(TAG_SAVED_STATE, "Launch completed; removing icicle of " + r.icicle);
        }
    }

    void addRecentActivityLocked(ActivityRecord r) {
        if (r != null) {
            TaskRecord task = r.getTask();
            this.mRecentTasks.addLocked(task);
            task.touchActiveTime();
        }
    }

    private void startLaunchTraces(String packageName) {
        if (this.mFullyDrawnStartTime != 0) {
            Trace.asyncTraceEnd(64, "drawing", 0);
        }
        Trace.asyncTraceBegin(64, "launching: " + packageName, 0);
        Trace.asyncTraceBegin(64, "drawing", 0);
    }

    private void stopFullyDrawnTraceIfNeeded() {
        if (this.mFullyDrawnStartTime != 0 && this.mLaunchStartTime == 0) {
            Trace.asyncTraceEnd(64, "drawing", 0);
            this.mFullyDrawnStartTime = 0;
        }
    }

    void setLaunchTime(ActivityRecord r) {
        long uptimeMillis;
        if (r.displayStartTime == 0) {
            uptimeMillis = SystemClock.uptimeMillis();
            r.displayStartTime = uptimeMillis;
            r.fullyDrawnStartTime = uptimeMillis;
            this.mshortComponentName = r.shortComponentName;
            Jlog.d(43, r.shortComponentName, "");
            if (r.task != null) {
                r.task.isLaunching = true;
            }
            if (this.mLaunchStartTime == 0) {
                startLaunchTraces(r.packageName);
                uptimeMillis = r.displayStartTime;
                this.mFullyDrawnStartTime = uptimeMillis;
                this.mLaunchStartTime = uptimeMillis;
            }
        } else if (this.mLaunchStartTime == 0) {
            startLaunchTraces(r.packageName);
            uptimeMillis = SystemClock.uptimeMillis();
            this.mFullyDrawnStartTime = uptimeMillis;
            this.mLaunchStartTime = uptimeMillis;
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

    void awakeFromSleepingLocked() {
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            ArrayList<ActivityRecord> activities = ((TaskRecord) this.mTaskHistory.get(taskNdx)).mActivities;
            for (int activityNdx = activities.size() - 1; activityNdx >= 0; activityNdx--) {
                ((ActivityRecord) activities.get(activityNdx)).setSleeping(false);
            }
        }
        if (this.mPausingActivity != null) {
            Flog.i(101, "Previously pausing activity " + this.mPausingActivity.shortComponentName + " state : " + this.mPausingActivity.state);
            activityPausedLocked(this.mPausingActivity.appToken, true);
        }
    }

    void updateActivityApplicationInfoLocked(ApplicationInfo aInfo) {
        if (aInfo != null) {
            String packageName = aInfo.packageName;
            int userId = UserHandle.getUserId(aInfo.uid);
            for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
                List<ActivityRecord> activities = ((TaskRecord) this.mTaskHistory.get(taskNdx)).mActivities;
                for (int activityNdx = activities.size() - 1; activityNdx >= 0; activityNdx--) {
                    ActivityRecord ar = (ActivityRecord) activities.get(activityNdx);
                    if (userId == ar.userId && packageName.equals(ar.packageName)) {
                        ar.info.applicationInfo = aInfo;
                    }
                }
            }
        }
    }

    boolean checkReadyForSleepLocked() {
        if (this.mResumedActivity != null) {
            if (ActivityManagerDebugConfig.DEBUG_PAUSE) {
                Slog.v(TAG_PAUSE, "Sleep needs to pause " + this.mResumedActivity);
            }
            if (ActivityManagerDebugConfig.DEBUG_USER_LEAVING) {
                Slog.v(TAG_USER_LEAVING, "Sleep => pause with userLeaving=false");
            }
            if (this.mStackSupervisor.inResumeTopActivity) {
                if (ActivityManagerDebugConfig.DEBUG_PAUSE) {
                    Slog.v(TAG_PAUSE, "In the middle of resuming top activity " + this.mResumedActivity);
                }
                return true;
            }
            startPausingLocked(false, true, null, false);
            return true;
        } else if (this.mPausingActivity != null) {
            if (ActivityManagerDebugConfig.DEBUG_PAUSE) {
                Slog.v(TAG_PAUSE, "Sleep still waiting to pause " + this.mPausingActivity);
            }
            return true;
        } else if (!hasVisibleBehindActivity()) {
            return false;
        } else {
            ActivityRecord r = getVisibleBehindActivity();
            this.mStackSupervisor.mStoppingActivities.add(r);
            if (ActivityManagerDebugConfig.DEBUG_STATES) {
                Slog.v(TAG_STATES, "Sleep still waiting to stop visible behind " + r);
            }
            return true;
        }
    }

    void goToSleep() {
        ensureActivitiesVisibleLocked(null, 0, false);
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            ArrayList<ActivityRecord> activities = ((TaskRecord) this.mTaskHistory.get(taskNdx)).mActivities;
            for (int activityNdx = activities.size() - 1; activityNdx >= 0; activityNdx--) {
                ActivityRecord r = (ActivityRecord) activities.get(activityNdx);
                if (r.state == ActivityState.STOPPING || r.state == ActivityState.STOPPED || r.state == ActivityState.PAUSED || r.state == ActivityState.PAUSING) {
                    r.setSleeping(true);
                }
            }
        }
    }

    private void schedulePauseTimeout(ActivityRecord r) {
        Message msg = this.mHandler.obtainMessage(101);
        msg.obj = r;
        r.pauseTime = SystemClock.uptimeMillis();
        this.mHandler.sendMessageDelayed(msg, 500);
        if (ActivityManagerDebugConfig.DEBUG_PAUSE) {
            Slog.v(TAG_PAUSE, "Waiting for pause to complete...");
        }
    }

    final boolean startPausingLocked(boolean userLeaving, boolean uiSleeping, ActivityRecord resuming, boolean pauseImmediately) {
        if (this.mPausingActivity != null) {
            Slog.wtf(TAG, "Going to pause when pause is already pending for " + this.mPausingActivity + " state=" + this.mPausingActivity.state);
            if (!this.mService.isSleepingLocked()) {
                completePauseLocked(false, resuming);
            }
        }
        ActivityRecord prev = this.mResumedActivity;
        if (prev == null) {
            if (resuming == null) {
                Slog.wtf(TAG, "Trying to pause when nothing is resumed");
                this.mStackSupervisor.resumeFocusedStackTopActivityLocked();
            }
            return false;
        }
        if (this.mActivityContainer.mParentActivity == null) {
            this.mStackSupervisor.pauseChildStacks(prev, userLeaving, uiSleeping, resuming, pauseImmediately);
        }
        if (ActivityManagerDebugConfig.DEBUG_STATES) {
            Slog.v(TAG_STATES, "Moving to PAUSING: " + prev);
        }
        Flog.i(101, "Moving to PAUSING: " + prev + " in stack " + this.mStackId);
        this.mResumedActivity = null;
        this.mPausingActivity = prev;
        this.mLastPausedActivity = prev;
        ActivityRecord activityRecord = ((prev.intent.getFlags() & 1073741824) == 0 && (prev.info.flags & 128) == 0) ? null : prev;
        this.mLastNoHistoryActivity = activityRecord;
        prev.state = ActivityState.PAUSING;
        prev.getTask().touchActiveTime();
        clearLaunchTime(prev);
        ActivityRecord next = this.mStackSupervisor.topRunningActivityLocked();
        if (prev.app != this.mService.mHomeProcess && this.mService.mHasRecents && (next == null || next.noDisplay || next.getTask() != prev.getTask() || uiSleeping)) {
            prev.mUpdateTaskThumbnailWhenHidden = true;
        }
        stopFullyDrawnTraceIfNeeded();
        this.mService.updateCpuStats();
        if (prev.app == null || prev.app.thread == null) {
            Flog.i(101, "Clear pausing activity " + this.mPausingActivity + " in stack " + this.mStackId + " for tha app is not ready");
            this.mPausingActivity = null;
            this.mLastPausedActivity = null;
            this.mLastNoHistoryActivity = null;
        } else {
            if (ActivityManagerDebugConfig.DEBUG_PAUSE) {
                Slog.v(TAG_PAUSE, "Enqueueing pending pause: " + prev);
            }
            try {
                EventLog.writeEvent(EventLogTags.AM_PAUSE_ACTIVITY, new Object[]{Integer.valueOf(prev.userId), Integer.valueOf(System.identityHashCode(prev)), prev.shortComponentName});
                this.mService.updateUsageStats(prev, false);
                if (Jlog.isPerfTest()) {
                    Jlog.i(2024, Intent.toPkgClsString(prev.realActivity, "who"));
                }
                prev.app.thread.schedulePauseActivity(prev.appToken, prev.finishing, userLeaving, prev.configChangeFlags, pauseImmediately);
            } catch (Exception e) {
                Slog.w(TAG, "Exception thrown during pause", e);
                this.mPausingActivity = null;
                this.mLastPausedActivity = null;
                this.mLastNoHistoryActivity = null;
            }
        }
        if (!(uiSleeping || (this.mService.isSleepingOrShuttingDownLocked() ^ 1) == 0)) {
            this.mStackSupervisor.acquireLaunchWakelock();
        }
        if (this.mPausingActivity != null) {
            if (!uiSleeping) {
                prev.pauseKeyDispatchingLocked();
            } else if (ActivityManagerDebugConfig.DEBUG_PAUSE) {
                Slog.v(TAG_PAUSE, "Key dispatch not paused for screen off");
            }
            if (pauseImmediately) {
                completePauseLocked(false, resuming);
                return false;
            }
            schedulePauseTimeout(prev);
            return true;
        }
        if (ActivityManagerDebugConfig.DEBUG_PAUSE) {
            Slog.v(TAG_PAUSE, "Activity not running, resuming next.");
        }
        if (resuming == null) {
            this.mStackSupervisor.mActivityLaunchTrack = " activityNotRunning";
            this.mStackSupervisor.resumeFocusedStackTopActivityLocked();
        }
        return false;
    }

    final void activityPausedLocked(IBinder token, boolean timeout) {
        if (ActivityManagerDebugConfig.DEBUG_PAUSE) {
            Slog.v(TAG_PAUSE, "Activity paused: token=" + token + ", timeout=" + timeout);
        }
        ActivityRecord r = isInStackLocked(token);
        if (r != null) {
            if (Jlog.isPerfTest()) {
                Jlog.i(2028, Intent.toPkgClsString(r.realActivity, "who"));
            }
            this.mHandler.removeMessages(101, r);
            if (this.mPausingActivity == r) {
                Flog.i(101, "Moving to PAUSED: " + r + (timeout ? " (due to timeout)" : " (pause complete)") + " in stack " + this.mStackId);
                this.mService.mWindowManager.deferSurfaceLayout();
                try {
                    completePauseLocked(true, null);
                    return;
                } finally {
                    this.mService.mWindowManager.continueSurfaceLayout();
                }
            } else {
                Flog.i(101, "FAILED to PAUSED: " + r + " state " + r.state + " in stack " + this.mStackId);
                Object[] objArr = new Object[4];
                objArr[0] = Integer.valueOf(r.userId);
                objArr[1] = Integer.valueOf(System.identityHashCode(r));
                objArr[2] = r.shortComponentName;
                objArr[3] = this.mPausingActivity != null ? this.mPausingActivity.shortComponentName : "(none)";
                EventLog.writeEvent(EventLogTags.AM_FAILED_TO_PAUSE, objArr);
                if (r.state == ActivityState.PAUSING) {
                    r.state = ActivityState.PAUSED;
                    if (r.finishing) {
                        Flog.i(101, "Executing finish of failed to pause activity: " + r);
                        finishCurrentActivityLocked(r, 2, false);
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
            Slog.v(TAG_PAUSE, "Complete pause: " + prev);
        }
        if (prev != null) {
            setSoundEffectState(false, prev.packageName, false, null);
        }
        if (prev != null) {
            boolean wasStopping = prev.state == ActivityState.STOPPING;
            prev.state = ActivityState.PAUSED;
            if (prev.finishing) {
                if (ActivityManagerDebugConfig.DEBUG_PAUSE) {
                    Slog.v(TAG_PAUSE, "Executing finish of activity: " + prev);
                }
                if (prev.info != null && prev.task.isOverHomeStack() && prev.frontOfTask) {
                    this.mService.getRecordCust().appExitRecord(prev.info.packageName, "finish");
                }
                prev = finishCurrentActivityLocked(prev, 2, false);
            } else if (prev.app != null) {
                if (ActivityManagerDebugConfig.DEBUG_PAUSE) {
                    Slog.v(TAG_PAUSE, "Enqueue pending stop if needed: " + prev + " wasStopping=" + wasStopping + " visible=" + prev.visible);
                }
                if (this.mStackSupervisor.mActivitiesWaitingForVisibleActivity.remove(prev) && (ActivityManagerDebugConfig.DEBUG_SWITCH || ActivityManagerDebugConfig.DEBUG_PAUSE)) {
                    Slog.v(TAG_PAUSE, "Complete pause, no longer waiting: " + prev);
                }
                if (prev.deferRelaunchUntilPaused) {
                    Slog.v(TAG_PAUSE, "Re-launching after pause: " + prev);
                    prev.relaunchActivityLocked(false, prev.preserveWindowOnDeferredRelaunch);
                } else if (wasStopping) {
                    prev.state = ActivityState.STOPPING;
                } else if (!(prev.visible || (hasVisibleBehindActivity() ^ 1) == 0) || this.mService.isSleepingOrShuttingDownLocked()) {
                    prev.setDeferHidingClient(false);
                    addToStopping(prev, true, false);
                }
            } else {
                if (ActivityManagerDebugConfig.DEBUG_PAUSE) {
                    Slog.v(TAG_PAUSE, "App died during pause, not stopping: " + prev);
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
            if (this.mService.isSleepingOrShuttingDownLocked()) {
                this.mStackSupervisor.checkReadyForSleepLocked();
                ActivityRecord top = topStack.topRunningActivityLocked();
                if (top == null || !(prev == null || top == prev)) {
                    this.mStackSupervisor.mActivityLaunchTrack = "sleepingNoMoreActivityRun";
                    this.mStackSupervisor.resumeFocusedStackTopActivityLocked();
                }
            } else {
                this.mStackSupervisor.mActivityLaunchTrack = "activityPaused";
                this.mStackSupervisor.resumeFocusedStackTopActivityLocked(topStack, prev, null);
            }
        }
        if (prev != null) {
            prev.resumeKeyDispatchingLocked();
            if (prev.app != null && prev.cpuTimeAtResume > 0 && this.mService.mBatteryStatsService.isOnBattery()) {
                long diff = this.mService.mProcessCpuTracker.getCpuTimeForPid(prev.app.pid) - prev.cpuTimeAtResume;
                if (diff > 0) {
                    BatteryStatsImpl bsi = this.mService.mBatteryStatsService.getActiveStatistics();
                    synchronized (bsi) {
                        Proc ps = bsi.getProcessStatsLocked(prev.info.applicationInfo.uid, prev.info.packageName);
                        if (ps != null) {
                            ps.addForegroundTimeLocked(diff);
                        }
                    }
                }
            }
            prev.cpuTimeAtResume = 0;
        }
        if (this.mStackSupervisor.mAppVisibilitiesChangedSinceLastPause || this.mService.mStackSupervisor.getStack(4) != null) {
            this.mService.mTaskChangeNotificationController.notifyTaskStackChanged();
            this.mStackSupervisor.mAppVisibilitiesChangedSinceLastPause = false;
        }
        this.mStackSupervisor.ensureActivitiesVisibleLocked(resuming, 0, false);
    }

    void addToStopping(ActivityRecord r, boolean scheduleIdle, boolean idleDelayed) {
        if (!this.mStackSupervisor.mStoppingActivities.contains(r)) {
            this.mStackSupervisor.mStoppingActivities.add(r);
        }
        boolean forceIdle = this.mStackSupervisor.mStoppingActivities.size() <= 3 ? r.frontOfTask && this.mTaskHistory.size() <= 1 : true;
        if (scheduleIdle || forceIdle) {
            if (ActivityManagerDebugConfig.DEBUG_PAUSE) {
                Slog.v(TAG_PAUSE, "Scheduling idle now: forceIdle=" + forceIdle + "immediate=" + (idleDelayed ^ 1));
            }
            if (idleDelayed) {
                this.mStackSupervisor.scheduleIdleTimeoutLocked(r);
                return;
            } else {
                this.mStackSupervisor.scheduleIdleLocked();
                return;
            }
        }
        this.mStackSupervisor.checkReadyForSleepLocked();
    }

    ActivityRecord findNextTranslucentActivity(ActivityRecord r) {
        TaskRecord task = r.getTask();
        if (task == null) {
            return null;
        }
        ActivityStack stack = task.getStack();
        if (stack == null) {
            return null;
        }
        int taskNdx = stack.mTaskHistory.indexOf(task);
        int activityNdx = task.mActivities.indexOf(r) + 1;
        int numStacks = this.mStacks.size();
        for (int stackNdx = this.mStacks.indexOf(stack); stackNdx < numStacks; stackNdx++) {
            ActivityStack historyStack = (ActivityStack) this.mStacks.get(stackNdx);
            ArrayList<TaskRecord> tasks = historyStack.mTaskHistory;
            int numTasks = tasks.size();
            for (taskNdx = 
/*
Method generation error in method: com.android.server.am.ActivityStack.findNextTranslucentActivity(com.android.server.am.ActivityRecord):com.android.server.am.ActivityRecord, dex: 
jadx.core.utils.exceptions.CodegenException: Error generate insn: PHI: (r11_1 'taskNdx' int) = (r11_0 'taskNdx' int), (r11_4 'taskNdx' int) binds: {(r11_0 'taskNdx' int)=B:8:0x0010, (r11_4 'taskNdx' int)=B:26:0x0069} in method: com.android.server.am.ActivityStack.findNextTranslucentActivity(com.android.server.am.ActivityRecord):com.android.server.am.ActivityRecord, dex: 
	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:228)
	at jadx.core.codegen.RegionGen.makeLoop(RegionGen.java:183)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:61)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:87)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:53)
	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:93)
	at jadx.core.codegen.RegionGen.makeLoop(RegionGen.java:189)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:61)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:87)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:53)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:87)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:53)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:87)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:53)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:87)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:53)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:87)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:53)
	at jadx.core.codegen.MethodGen.addInstructions(MethodGen.java:173)
	at jadx.core.codegen.ClassGen.addMethod(ClassGen.java:322)
	at jadx.core.codegen.ClassGen.addMethods(ClassGen.java:260)
	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:222)
	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:112)
	at jadx.core.codegen.ClassGen.makeClass(ClassGen.java:78)
	at jadx.core.codegen.CodeGen.visit(CodeGen.java:10)
	at jadx.core.ProcessClass.process(ProcessClass.java:38)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
Caused by: jadx.core.utils.exceptions.CodegenException: PHI can be used only in fallback mode
	at jadx.core.codegen.InsnGen.fallbackOnlyInsn(InsnGen.java:539)
	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:511)
	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:222)
	... 28 more

*/

    private boolean hasFullscreenTask() {
        for (int i = this.mTaskHistory.size() - 1; i >= 0; i--) {
            if (((TaskRecord) this.mTaskHistory.get(i)).mFullscreen) {
                return true;
            }
        }
        return false;
    }

    private boolean isStackTranslucent(ActivityRecord starting, int stackBehindId) {
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            TaskRecord task = (TaskRecord) this.mTaskHistory.get(taskNdx);
            ArrayList<ActivityRecord> activities = task.mActivities;
            for (int activityNdx = activities.size() - 1; activityNdx >= 0; activityNdx--) {
                ActivityRecord r = (ActivityRecord) activities.get(activityNdx);
                if (!r.finishing && (r.visible || r == starting)) {
                    if (r.fullscreen) {
                        return false;
                    }
                    if (!(isHomeOrRecentsStack() || !r.frontOfTask || !task.isOverHomeStack() || (StackId.isHomeOrRecentsStack(stackBehindId) ^ 1) == 0 || (isAssistantStack() ^ 1) == 0)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    boolean isVisible() {
        if (this.mWindowContainerController == null || !this.mWindowContainerController.isVisible()) {
            return false;
        }
        return this.mForceHidden ^ 1;
    }

    protected int shouldBeVisible(ActivityRecord starting) {
        if (!isAttached() || this.mForceHidden) {
            return 0;
        }
        if (this.mStackSupervisor.isFrontStackOnDisplay(this) || this.mStackSupervisor.isFocusedStack(this)) {
            return 1;
        }
        int stackIndex = this.mStacks.indexOf(this);
        if (stackIndex == this.mStacks.size() - 1) {
            Slog.wtf(TAG, "Stack=" + this + " isn't front stack but is at the top of the stack list");
            return 0;
        }
        ActivityStack topStack = getTopStackOnDisplay();
        int topStackId = topStack.mStackId;
        if (StackId.isBackdropToTranslucentActivity(this.mStackId) && hasVisibleBehindActivity() && StackId.isHomeOrRecentsStack(topStackId) && (topStack.topActivity() == null || (topStack.topActivity().fullscreen ^ 1) != 0)) {
            return 2;
        }
        if (this.mStackId != 3) {
            if (this.mStackId == 0) {
                int dockedStackIndex = this.mStacks.indexOf(this.mStackSupervisor.getStack(3));
                if (dockedStackIndex > stackIndex && stackIndex != dockedStackIndex - 1) {
                    int behindDockedStackIndex = dockedStackIndex - 1;
                    boolean shouldSkipActivityInFinishing = false;
                    while (behindDockedStackIndex >= 0 && ((ActivityStack) this.mStacks.get(behindDockedStackIndex)).mStackId == 1 && ((ActivityStack) this.mStacks.get(behindDockedStackIndex)).topActivity() == null) {
                        shouldSkipActivityInFinishing = true;
                        behindDockedStackIndex--;
                    }
                    if (behindDockedStackIndex == stackIndex && shouldSkipActivityInFinishing) {
                        return 1;
                    }
                    return 0;
                }
            }
            int stackBehindTopIndex = this.mStacks.indexOf(topStack) - 1;
            while (stackBehindTopIndex >= 0 && ((ActivityStack) this.mStacks.get(stackBehindTopIndex)).topRunningActivityLocked() == null) {
                stackBehindTopIndex--;
            }
            int stackBehindTopId = stackBehindTopIndex >= 0 ? ((ActivityStack) this.mStacks.get(stackBehindTopIndex)).mStackId : -1;
            if ((topStackId == 3 || topStackId == 4) && (stackIndex == stackBehindTopIndex || (stackBehindTopId == 3 && stackIndex == stackBehindTopIndex - 1))) {
                int realStackBehindTopIndex = this.mStacks.indexOf(topStack) - 1;
                if (!this.mFirstDockedWithoutRecent || realStackBehindTopIndex < 0 || this.mStackId != 1 || ((ActivityStack) this.mStacks.get(realStackBehindTopIndex)).mStackId != 5 || ((ActivityStack) this.mStacks.get(realStackBehindTopIndex)).topActivity() != null) {
                    return 1;
                }
                Slog.i(TAG, "First enter multiwindow without recent, make fullsreen stack invisible");
                this.mFirstDockedWithoutRecent = false;
                return 0;
            } else if (topStackId == 4 && StackId.isBackdropToTranslucentActivity(stackBehindTopId) && stackBehindTopIndex >= 0 && ((ActivityStack) this.mStacks.get(stackBehindTopIndex)).isStackTranslucent(starting, 0) && stackIndex == stackBehindTopIndex - 1) {
                return 1;
            } else {
                if (StackId.isBackdropToTranslucentActivity(topStackId) && topStack.isStackTranslucent(starting, stackBehindTopId)) {
                    if (stackIndex == stackBehindTopIndex) {
                        return 1;
                    }
                    if (stackBehindTopIndex >= 0 && ((stackBehindTopId == 3 || stackBehindTopId == 4) && stackIndex == stackBehindTopIndex - 1)) {
                        return 1;
                    }
                }
                if (StackId.isStaticStack(this.mStackId)) {
                    return 0;
                }
                for (int i = stackIndex + 1; i < this.mStacks.size(); i++) {
                    ActivityStack stack = (ActivityStack) this.mStacks.get(i);
                    if (stack.mFullscreen || (stack.hasFullscreenTask() ^ 1) == 0) {
                        if (!StackId.isDynamicStacksVisibleBehindAllowed(stack.mStackId)) {
                            return 0;
                        }
                        if (!stack.isStackTranslucent(starting, -1)) {
                            return 0;
                        }
                    }
                }
                return 1;
            }
        } else if (!topStack.isAssistantStack()) {
            return 1;
        } else {
            int i2;
            if (topStack.isStackTranslucent(starting, 3)) {
                i2 = 1;
            } else {
                i2 = 0;
            }
            return i2;
        }
    }

    final int rankTaskLayers(int baseLayer) {
        int taskNdx = this.mTaskHistory.size() - 1;
        int layer = 0;
        while (taskNdx >= 0) {
            int layer2;
            TaskRecord task = (TaskRecord) this.mTaskHistory.get(taskNdx);
            ActivityRecord r = task.topRunningActivityLocked();
            if (r == null || r.finishing || (r.visible ^ 1) != 0) {
                task.mLayerRank = -1;
                layer2 = layer;
            } else {
                layer2 = layer + 1;
                task.mLayerRank = baseLayer + layer;
            }
            taskNdx--;
            layer = layer2;
        }
        return layer;
    }

    final void ensureActivitiesVisibleLocked(ActivityRecord starting, int configChanges, boolean preserveWindows) {
        this.mTopActivityOccludesKeyguard = false;
        this.mTopDismissingKeyguardActivity = null;
        this.mStackSupervisor.mKeyguardController.beginActivityVisibilityUpdate();
        try {
            ActivityRecord top = topRunningActivityLocked();
            if (ActivityManagerDebugConfig.DEBUG_VISIBILITY) {
                Slog.v(TAG_VISIBILITY, "ensureActivitiesVisible behind " + top + " configChanges=0x" + Integer.toHexString(configChanges));
            }
            if (top != null) {
                checkTranslucentActivityWaiting(top);
            }
            boolean aboveTop = top != null;
            int stackVisibility = shouldBeVisible(starting);
            boolean stackInvisible = stackVisibility != 1;
            boolean stackVisibleBehind = stackVisibility == 2;
            boolean behindFullscreenActivity = stackInvisible;
            boolean resumeNextActivity = this.mStackSupervisor.isFocusedStack(this) ? isInStackLocked(starting) == null : false;
            boolean behindTranslucentActivity = false;
            ActivityRecord visibleBehind = getVisibleBehindActivity();
            for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
                TaskRecord task = (TaskRecord) this.mTaskHistory.get(taskNdx);
                ArrayList<ActivityRecord> activities = task.mActivities;
                int activitiesSize = activities.size();
                int activityNdx = activities.size() - 1;
                while (activityNdx >= 0) {
                    if (activityNdx < activitiesSize) {
                        ActivityRecord r = (ActivityRecord) activities.get(activityNdx);
                        if (!r.finishing) {
                            boolean isTop = r == top;
                            if (!aboveTop || (isTop ^ 1) == 0) {
                                aboveTop = false;
                                boolean visibleIgnoringKeyguard = r.shouldBeVisibleIgnoringKeyguard(behindTranslucentActivity, stackVisibleBehind, visibleBehind, behindFullscreenActivity);
                                r.visibleIgnoringKeyguard = visibleIgnoringKeyguard;
                                boolean reallyVisible = checkKeyguardVisibility(r, visibleIgnoringKeyguard, isTop);
                                if (visibleIgnoringKeyguard) {
                                    behindFullscreenActivity = updateBehindFullscreen(stackInvisible, behindFullscreenActivity, task, r);
                                    if (behindFullscreenActivity && (r.fullscreen ^ 1) != 0) {
                                        behindTranslucentActivity = true;
                                    }
                                }
                                if (reallyVisible) {
                                    if (ActivityManagerDebugConfig.DEBUG_VISIBILITY) {
                                        Slog.v(TAG_VISIBILITY, "Make visible? " + r + " finishing=" + r.finishing + " state=" + r.state);
                                    }
                                    if (r != starting) {
                                        r.ensureActivityConfigurationLocked(0, preserveWindows);
                                    }
                                    if (r.app == null || r.app.thread == null) {
                                        if (makeVisibleAndRestartIfNeeded(starting, configChanges, isTop, resumeNextActivity, r)) {
                                            if (activityNdx >= activities.size()) {
                                                activityNdx = activities.size() - 1;
                                            } else {
                                                resumeNextActivity = false;
                                            }
                                        }
                                    } else if (r.visible) {
                                        if (ActivityManagerDebugConfig.DEBUG_VISIBILITY) {
                                            Slog.v(TAG_VISIBILITY, "Skipping: already visible at " + r);
                                        }
                                        if (r.handleAlreadyVisible()) {
                                            resumeNextActivity = false;
                                        }
                                    } else {
                                        r.makeVisibleIfNeeded(starting);
                                    }
                                    configChanges |= r.configChangeFlags;
                                } else {
                                    if ((r.state == ActivityState.RESUMED) || ActivityManagerDebugConfig.DEBUG_VISIBILITY) {
                                        StringBuilder builder = new StringBuilder();
                                        builder.append("Make invisible? ");
                                        builder.append(r);
                                        builder.append(", finishing = ");
                                        builder.append(r.finishing);
                                        builder.append(", stackInvisible = ");
                                        builder.append(stackInvisible);
                                        builder.append(", behindTranslucentActivity = ");
                                        builder.append(behindTranslucentActivity);
                                        builder.append(", stackVisibleBehind = ");
                                        builder.append(stackVisibleBehind);
                                        builder.append(", behindFullscreenActivity = ");
                                        builder.append(behindFullscreenActivity);
                                        builder.append(", visibleBehind = ");
                                        builder.append(visibleBehind);
                                        builder.append(", r.mLaunchTaskBehind = ");
                                        builder.append(r.mLaunchTaskBehind);
                                        builder.append(", keyguardShowing = ");
                                        builder.append(this.mStackSupervisor.mKeyguardController.isKeyguardShowing());
                                        builder.append(", keyguardLocked = ");
                                        builder.append(this.mStackSupervisor.mKeyguardController.isKeyguardLocked());
                                        builder.append(", visibleIgnoringKeyguard = ");
                                        builder.append(visibleIgnoringKeyguard);
                                        Flog.i(106, builder.toString());
                                    }
                                    makeInvisible(r, visibleBehind);
                                }
                            }
                        } else if (r.mUpdateTaskThumbnailWhenHidden) {
                            r.updateThumbnailLocked(r.screenshotActivityLocked(), null);
                            r.mUpdateTaskThumbnailWhenHidden = false;
                        }
                    }
                    activityNdx--;
                }
                if (this.mStackId == 2) {
                    if (stackVisibility == 0) {
                        behindFullscreenActivity = true;
                    } else {
                        behindFullscreenActivity = false;
                    }
                } else if (this.mStackId == 0) {
                    if (task.isHomeTask()) {
                        if (ActivityManagerDebugConfig.DEBUG_VISIBILITY) {
                            Slog.v(TAG_VISIBILITY, "Home task: at " + task + " stackInvisible=" + stackInvisible + " behindFullscreenActivity=" + behindFullscreenActivity);
                        }
                        if (task.getTopActivity() != null) {
                            behindFullscreenActivity = true;
                        }
                    } else if (task.isRecentsTask() && task.getTaskToReturnTo() == 0) {
                        if (ActivityManagerDebugConfig.DEBUG_VISIBILITY) {
                            Slog.v(TAG_VISIBILITY, "Recents task returning to app: at " + task + " stackInvisible=" + stackInvisible + " behindFullscreenActivity=" + behindFullscreenActivity);
                        }
                        behindFullscreenActivity = true;
                    }
                }
            }
            if (this.mTranslucentActivityWaiting != null && this.mUndrawnActivitiesBelowTopTranslucent.isEmpty()) {
                notifyActivityDrawnLocked(null);
            }
            this.mStackSupervisor.mKeyguardController.endActivityVisibilityUpdate();
        } catch (Throwable th) {
            this.mStackSupervisor.mKeyguardController.endActivityVisibilityUpdate();
        }
    }

    void addStartingWindowsForVisibleActivities(boolean taskSwitch) {
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            ((TaskRecord) this.mTaskHistory.get(taskNdx)).addStartingWindowsForVisibleActivities(taskSwitch);
        }
    }

    boolean topActivityOccludesKeyguard() {
        return this.mTopActivityOccludesKeyguard;
    }

    ActivityRecord getTopDismissingKeyguardActivity() {
        return this.mTopDismissingKeyguardActivity;
    }

    boolean checkKeyguardVisibility(ActivityRecord r, boolean shouldBeVisible, boolean isTop) {
        boolean z = false;
        ActivityStack stack = r.getStack();
        if (stack == null) {
            Slog.v(TAG_VISIBILITY, "checkKeyguardVisibility return: stack is null");
            return shouldBeVisible;
        }
        boolean isInPinnedStack = stack.getStackId() == 4;
        boolean keyguardShowing = this.mStackSupervisor.mKeyguardController.isKeyguardShowing();
        boolean keyguardLocked = this.mStackSupervisor.mKeyguardController.isKeyguardLocked();
        boolean showWhenLocked = r.hasShowWhenLockedWindows() ? isInPinnedStack ^ 1 : false;
        boolean dismissKeyguard = r.hasDismissKeyguardWindows();
        if (shouldBeVisible) {
            boolean canShowWithKeyguard;
            if (dismissKeyguard && this.mTopDismissingKeyguardActivity == null) {
                this.mTopDismissingKeyguardActivity = r;
            }
            if (isTop) {
                this.mTopActivityOccludesKeyguard |= showWhenLocked;
            }
            if (canShowWithInsecureKeyguard()) {
                canShowWithKeyguard = this.mStackSupervisor.mKeyguardController.canDismissKeyguard();
            } else {
                canShowWithKeyguard = false;
            }
            if (canShowWithKeyguard) {
                return true;
            }
        }
        if (keyguardShowing) {
            if (shouldBeVisible) {
                z = this.mStackSupervisor.mKeyguardController.canShowActivityWhileKeyguardShowing(r, dismissKeyguard);
            }
            return z;
        } else if (!keyguardLocked) {
            return shouldBeVisible;
        } else {
            if (shouldBeVisible) {
                z = this.mStackSupervisor.mKeyguardController.canShowWhileOccluded(dismissKeyguard, showWhenLocked);
            }
            return z;
        }
    }

    private boolean canShowWithInsecureKeyguard() {
        ActivityDisplay activityDisplay = this.mActivityContainer.mActivityDisplay;
        if (activityDisplay == null) {
            throw new IllegalStateException("Stack is not attached to any display, stackId=" + this.mStackId);
        } else if ((activityDisplay.mDisplay.getFlags() & 32) != 0) {
            return true;
        } else {
            return false;
        }
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
        if (isTop || (r.visible ^ 1) != 0) {
            if (ActivityManagerDebugConfig.DEBUG_VISIBILITY) {
                Slog.v(TAG_VISIBILITY, "Start and freeze screen for " + r);
            }
            if (r != starting) {
                r.startFreezingScreenLocked(r.app, configChanges);
            }
            if (!r.visible || r.mLaunchTaskBehind) {
                if (ActivityManagerDebugConfig.DEBUG_VISIBILITY) {
                    Slog.v(TAG_VISIBILITY, "Starting and making visible: " + r);
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

    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void makeInvisible(ActivityRecord r, ActivityRecord visibleBehind) {
        if (r.visible) {
            Flog.i(106, "Making invisible: " + r + " " + r.state);
            try {
                boolean canEnterPictureInPicture = r.checkEnterPictureInPictureState("makeInvisible", true, true);
                boolean deferHidingClient = (!canEnterPictureInPicture || r.state == ActivityState.STOPPING) ? false : r.state != ActivityState.STOPPED;
                r.setDeferHidingClient(deferHidingClient);
                r.setVisible(false);
                switch (-getcom-android-server-am-ActivityStack$ActivityStateSwitchesValues()[r.state.ordinal()]) {
                    case 1:
                    case 2:
                    case 3:
                    case 4:
                        if (visibleBehind != r) {
                            addToStopping(r, true, canEnterPictureInPicture);
                            break;
                        } else {
                            releaseBackgroundResources(r);
                            break;
                        }
                    case 5:
                    case 6:
                        if (!(r.app == null || r.app.thread == null)) {
                            if (ActivityManagerDebugConfig.DEBUG_VISIBILITY) {
                                Slog.v(TAG_VISIBILITY, "Scheduling invisibility: " + r);
                            }
                            r.app.thread.scheduleWindowVisibility(r.appToken, false);
                        }
                        r.supportsPictureInPictureWhilePausing = false;
                        break;
                }
            } catch (Exception e) {
                Slog.w(TAG, "Exception thrown making hidden: " + r.intent.getComponent(), e);
            }
            return;
        }
        if (ActivityManagerDebugConfig.DEBUG_VISIBILITY) {
            Slog.v(TAG_VISIBILITY, "Already invisible: " + r);
        }
    }

    private boolean updateBehindFullscreen(boolean stackInvisible, boolean behindFullscreenActivity, TaskRecord task, ActivityRecord r) {
        if (r.fullscreen) {
            if (ActivityManagerDebugConfig.DEBUG_VISIBILITY) {
                Slog.v(TAG_VISIBILITY, "Fullscreen: at " + r + " stackInvisible=" + stackInvisible + " behindFullscreenActivity=" + behindFullscreenActivity);
            }
            return true;
        } else if (isHomeOrRecentsStack() || !r.frontOfTask || !task.isOverHomeStack()) {
            return behindFullscreenActivity;
        } else {
            if (ActivityManagerDebugConfig.DEBUG_VISIBILITY) {
                Slog.v(TAG_VISIBILITY, "Showing home: at " + r + " stackInvisible=" + stackInvisible + " behindFullscreenActivity=" + behindFullscreenActivity);
            }
            return true;
        }
    }

    void convertActivityToTranslucent(ActivityRecord r) {
        this.mTranslucentActivityWaiting = r;
        this.mUndrawnActivitiesBelowTopTranslucent.clear();
        this.mHandler.sendEmptyMessageDelayed(106, TRANSLUCENT_CONVERSION_TIMEOUT);
    }

    void clearOtherAppTimeTrackers(AppTimeTracker except) {
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            ArrayList<ActivityRecord> activities = ((TaskRecord) this.mTaskHistory.get(taskNdx)).mActivities;
            for (int activityNdx = activities.size() - 1; activityNdx >= 0; activityNdx--) {
                ActivityRecord r = (ActivityRecord) activities.get(activityNdx);
                if (r.appTimeTracker != except) {
                    r.appTimeTracker = null;
                }
            }
        }
    }

    void notifyActivityDrawnLocked(ActivityRecord r) {
        boolean z = false;
        this.mActivityContainer.setDrawn();
        if (r == null || (this.mUndrawnActivitiesBelowTopTranslucent.remove(r) && this.mUndrawnActivitiesBelowTopTranslucent.isEmpty())) {
            ActivityRecord waitingActivity = this.mTranslucentActivityWaiting;
            this.mTranslucentActivityWaiting = null;
            this.mUndrawnActivitiesBelowTopTranslucent.clear();
            this.mHandler.removeMessages(106);
            if (waitingActivity != null) {
                this.mWindowManager.setWindowOpaque(waitingActivity.appToken, false);
                if (waitingActivity.app != null && waitingActivity.app.thread != null) {
                    try {
                        IApplicationThread iApplicationThread = waitingActivity.app.thread;
                        IBinder iBinder = waitingActivity.appToken;
                        if (r != null) {
                            z = true;
                        }
                        iApplicationThread.scheduleTranslucentConversionComplete(iBinder, z);
                    } catch (RemoteException e) {
                    }
                }
            }
        }
    }

    void cancelInitializingActivities() {
        ActivityRecord topActivity = topRunningActivityLocked();
        boolean aboveTop = true;
        boolean behindFullscreenActivity = false;
        if (shouldBeVisible(null) == 0) {
            aboveTop = false;
            behindFullscreenActivity = true;
        }
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            ArrayList<ActivityRecord> activities = ((TaskRecord) this.mTaskHistory.get(taskNdx)).mActivities;
            for (int activityNdx = activities.size() - 1; activityNdx >= 0; activityNdx--) {
                int i;
                ActivityRecord r = (ActivityRecord) activities.get(activityNdx);
                if (aboveTop) {
                    if (r == topActivity) {
                        aboveTop = false;
                    }
                    i = r.fullscreen;
                } else {
                    r.removeOrphanedStartingWindow(behindFullscreenActivity);
                    i = r.fullscreen;
                }
                behindFullscreenActivity |= i;
            }
        }
    }

    boolean resumeTopActivityUncheckedLocked(ActivityRecord prev, ActivityOptions options) {
        if (this.mStackSupervisor.inResumeTopActivity) {
            return false;
        }
        boolean result = false;
        try {
            this.mStackSupervisor.inResumeTopActivity = true;
            result = resumeTopActivityInnerLocked(prev, options);
            this.mStackSupervisor.checkReadyForSleepLocked();
            return result;
        } finally {
            this.mStackSupervisor.inResumeTopActivity = false;
        }
    }

    void setResumedActivityLocked(ActivityRecord r, String reason) {
        this.mResumedActivity = r;
        r.state = ActivityState.RESUMED;
        this.mService.setResumedActivityUncheckLocked(r, reason);
        TaskRecord task = r.getTask();
        task.touchActiveTime();
        this.mRecentTasks.addLocked(task);
    }

    private boolean resumeTopActivityInnerLocked(ActivityRecord prev, ActivityOptions options) {
        if (!this.mService.mBooting && (this.mService.mBooted ^ 1) != 0) {
            return false;
        }
        ActivityRecord next = topRunningActivityLocked(true);
        boolean hasRunningActivity = next != null;
        if (HwPCUtils.enabledInPad() && HwPCUtils.isPcCastModeInServer() && hasRunningActivity && next.task != null) {
            ActivityManagerService activityManagerService = this.mService;
            if (ActivityManagerService.isInCallActivity(next)) {
                next.task.activityResumedInTop();
            }
        }
        ActivityRecord parent = this.mActivityContainer.mParentActivity;
        boolean isParentNotResumed = (parent == null || parent.state == ActivityState.RESUMED) ? false : true;
        if (hasRunningActivity && (isParentNotResumed || (this.mActivityContainer.isAttachedLocked() ^ 1) != 0)) {
            return false;
        }
        this.mStackSupervisor.cancelInitializingActivities();
        boolean userLeaving = this.mStackSupervisor.mUserLeaving;
        this.mStackSupervisor.mUserLeaving = false;
        if (hasRunningActivity) {
            next.delayedResume = false;
            if (this.mResumedActivity == next && next.state == ActivityState.RESUMED && this.mStackSupervisor.allResumedActivitiesComplete()) {
                if (HwPCUtils.isPcCastModeInServer()) {
                    HwPCUtils.log(TAG, "resumeTopActivityInnerLocked");
                } else {
                    executeAppTransition(options);
                    Flog.i(101, "resumeTopActivityLocked: Top activity resumed " + next);
                    if (ActivityManagerDebugConfig.DEBUG_STACK) {
                        this.mStackSupervisor.validateTopActivitiesLocked();
                    }
                    return false;
                }
            }
            TaskRecord nextTask = next.getTask();
            TaskRecord prevTask = prev != null ? prev.getTask() : null;
            if (prevTask != null && prevTask.getStack() == this && prevTask.isOverHomeStack() && prev.finishing && prev.frontOfTask) {
                if (ActivityManagerDebugConfig.DEBUG_STACK) {
                    this.mStackSupervisor.validateTopActivitiesLocked();
                }
                if (prevTask == nextTask) {
                    prevTask.setFrontOfTask();
                } else if (prevTask != topTask()) {
                    ((TaskRecord) this.mTaskHistory.get(this.mTaskHistory.indexOf(prevTask) + 1)).setTaskToReturnTo(1);
                } else if (!isOnHomeDisplay()) {
                    return false;
                } else {
                    if (!isHomeStack()) {
                        boolean resumeHomeStackTask;
                        Flog.i(101, "resumeTopActivityLocked: Launching home next");
                        if (isOnHomeDisplay()) {
                            resumeHomeStackTask = this.mStackSupervisor.resumeHomeStackTask(prev, "prevFinished");
                        } else {
                            resumeHomeStackTask = false;
                        }
                        return resumeHomeStackTask;
                    }
                }
            }
            if (this.mService.isSleepingOrShuttingDownLocked() && this.mLastPausedActivity == next && this.mStackSupervisor.allPausedActivitiesComplete()) {
                executeAppTransition(options);
                Flog.i(101, "resumeTopActivityLocked: Going to sleep and all paused");
                if (ActivityManagerDebugConfig.DEBUG_STACK) {
                    this.mStackSupervisor.validateTopActivitiesLocked();
                }
                return false;
            } else if (this.mService.mUserController.hasStartedUserState(next.userId)) {
                this.mStackSupervisor.mStoppingActivities.remove(next);
                this.mStackSupervisor.mGoingToSleepActivities.remove(next);
                next.sleeping = false;
                this.mStackSupervisor.mActivitiesWaitingForVisibleActivity.remove(next);
                if (ActivityManagerDebugConfig.DEBUG_SWITCH) {
                    Slog.v(TAG_SWITCH, "Resuming " + next);
                }
                if (this.mStackSupervisor.allPausedActivitiesComplete()) {
                    this.mStackSupervisor.setLaunchSource(next.info.applicationInfo.uid);
                    int lastResumedCanPip = 0;
                    ActivityStack lastFocusedStack = this.mStackSupervisor.getLastStack();
                    if (!(lastFocusedStack == null || lastFocusedStack == this)) {
                        ActivityRecord lastResumed = lastFocusedStack.mResumedActivity;
                        lastResumedCanPip = lastResumed != null ? lastResumed.checkEnterPictureInPictureState("resumeTopActivity", true, userLeaving) : 0;
                    }
                    int resumeWhilePausing;
                    if ((next.info.flags & 16384) != 0) {
                        resumeWhilePausing = lastResumedCanPip ^ 1;
                    } else {
                        resumeWhilePausing = 0;
                    }
                    boolean pausing = this.mStackSupervisor.pauseBackStacks(userLeaving, next, false);
                    if (this.mResumedActivity != null) {
                        if (ActivityManagerDebugConfig.DEBUG_STATES) {
                            Slog.d(TAG_STATES, "resumeTopActivityLocked: Pausing " + this.mResumedActivity);
                        }
                        Flog.i(101, "resumeTopActivityLocked: Pausing " + this.mResumedActivity + " in stack " + this.mStackId);
                        if (!HwPCUtils.isPcCastModeInServer() || (SETTINGS_DASHBROAED_ACTIVITY_NAME.equals(this.mResumedActivity.info.name) && (HwPCUtils.isPcDynamicStack(this.mStackId) ^ 1) != 0)) {
                            pausing |= startPausingLocked(userLeaving, false, next, false);
                        }
                    }
                    if (pausing && (resumeWhilePausing ^ 1) != 0) {
                        Flog.i(101, "resumeTopActivityLocked: Skip resume: need to start pausing");
                        if (!(next.app == null || next.app.thread == null)) {
                            this.mService.updateLruProcessLocked(next.app, true, null);
                        }
                        if (ActivityManagerDebugConfig.DEBUG_STACK) {
                            this.mStackSupervisor.validateTopActivitiesLocked();
                        }
                        return true;
                    } else if (this.mResumedActivity == next && next.state == ActivityState.RESUMED && this.mStackSupervisor.allResumedActivitiesComplete()) {
                        executeAppTransition(options);
                        if (ActivityManagerDebugConfig.DEBUG_STATES) {
                            Slog.d(TAG_STATES, "resumeTopActivityLocked: Top activity resumed (dontWaitForPause) " + next);
                        }
                        if (ActivityManagerDebugConfig.DEBUG_STACK) {
                            this.mStackSupervisor.validateTopActivitiesLocked();
                        }
                        return true;
                    } else {
                        if (!(!this.mService.isSleepingLocked() || this.mLastNoHistoryActivity == null || (this.mLastNoHistoryActivity.finishing ^ 1) == 0)) {
                            if (ActivityManagerDebugConfig.DEBUG_STATES) {
                                Slog.d(TAG_STATES, "no-history finish of " + this.mLastNoHistoryActivity + " on new resume");
                            }
                            requestFinishActivityLocked(this.mLastNoHistoryActivity.appToken, 0, null, "resume-no-history", false);
                            this.mLastNoHistoryActivity = null;
                        }
                        if (!(prev == null || prev == next)) {
                            if (!this.mStackSupervisor.mActivitiesWaitingForVisibleActivity.contains(prev) && next != null && (next.nowVisible ^ 1) != 0) {
                                this.mStackSupervisor.mActivitiesWaitingForVisibleActivity.add(prev);
                                if (ActivityManagerDebugConfig.DEBUG_SWITCH) {
                                    Slog.v(TAG_SWITCH, "Resuming top, waiting visible to hide: " + prev);
                                }
                            } else if (prev.finishing) {
                                prev.setVisibility(false);
                                if (ActivityManagerDebugConfig.DEBUG_SWITCH) {
                                    Slog.v(TAG_SWITCH, "Not waiting for visible to hide: " + prev + ", waitingVisible=" + this.mStackSupervisor.mActivitiesWaitingForVisibleActivity.contains(prev) + ", nowVisible=" + next.nowVisible);
                                }
                            } else if (ActivityManagerDebugConfig.DEBUG_SWITCH) {
                                Slog.v(TAG_SWITCH, "Previous already visible but still waiting to hide: " + prev + ", waitingVisible=" + this.mStackSupervisor.mActivitiesWaitingForVisibleActivity.contains(prev) + ", nowVisible=" + next.nowVisible);
                            }
                        }
                        try {
                            AppGlobals.getPackageManager().setPackageStoppedState(next.packageName, false, next.userId);
                        } catch (RemoteException e) {
                        } catch (IllegalArgumentException e2) {
                            Slog.w(TAG, "Failed trying to unstop package " + next.packageName + ": " + e2);
                        }
                        boolean anim = true;
                        if (this.mIsAnimationBoostEnabled && this.mPerf == null) {
                            this.mPerf = new BoostFramework();
                        }
                        WindowManagerService windowManagerService;
                        int i;
                        if (prev == null) {
                            if (ActivityManagerDebugConfig.DEBUG_TRANSITION) {
                                Slog.v(TAG_TRANSITION, "Prepare open transition: no previous");
                            }
                            if (this.mNoAnimActivities.contains(next)) {
                                anim = false;
                                this.mWindowManager.prepareAppTransition(0, false);
                            } else {
                                this.mWindowManager.prepareAppTransition(6, false);
                            }
                        } else if (prev.finishing) {
                            if (ActivityManagerDebugConfig.DEBUG_TRANSITION) {
                                Slog.v(TAG_TRANSITION, "Prepare close transition: prev=" + prev);
                            }
                            if (this.mNoAnimActivities.contains(prev)) {
                                anim = false;
                                this.mWindowManager.prepareAppTransition(0, false);
                            } else {
                                windowManagerService = this.mWindowManager;
                                if (prev.getTask() == next.getTask()) {
                                    i = 7;
                                } else {
                                    i = 9;
                                }
                                windowManagerService.prepareAppTransition(i, false);
                                if (!(prev.task == next.task || this.mPerf == null)) {
                                    this.mPerf.perfLockAcquire(this.aBoostTimeOut, this.aBoostParamVal);
                                }
                            }
                            prev.setVisibility(false);
                        } else {
                            if (ActivityManagerDebugConfig.DEBUG_TRANSITION) {
                                Slog.v(TAG_TRANSITION, "Prepare open transition: prev=" + prev);
                            }
                            if (this.mNoAnimActivities.contains(next)) {
                                anim = false;
                                this.mWindowManager.prepareAppTransition(0, false);
                            } else {
                                windowManagerService = this.mWindowManager;
                                if (prev.getTask() == next.getTask()) {
                                    i = 6;
                                } else if (next.mLaunchTaskBehind) {
                                    i = 16;
                                } else {
                                    i = 8;
                                }
                                windowManagerService.prepareAppTransition(i, false);
                                if (!(prev.task == next.task || this.mPerf == null)) {
                                    this.mPerf.perfLockAcquire(this.aBoostTimeOut, this.aBoostParamVal);
                                }
                            }
                        }
                        Bundle resumeAnimOptions = null;
                        if (anim) {
                            ActivityOptions opts = next.getOptionsForTargetActivityLocked();
                            if (opts != null) {
                                resumeAnimOptions = opts.toBundle();
                            }
                            next.applyOptionsLocked();
                        } else {
                            next.clearOptionsLocked();
                        }
                        setKeepPortraitFR();
                        ActivityStack lastStack = this.mStackSupervisor.getLastStack();
                        if (next.app == null || next.app.thread == null) {
                            if (next.hasBeenLaunched) {
                                next.showStartingWindow(null, false, false);
                                if (ActivityManagerDebugConfig.DEBUG_SWITCH) {
                                    Slog.v(TAG_SWITCH, "Restarting: " + next);
                                }
                            } else {
                                next.hasBeenLaunched = true;
                            }
                            if (ActivityManagerDebugConfig.DEBUG_STATES) {
                                Slog.d(TAG_STATES, "resumeTopActivityLocked: Restarting " + next);
                            }
                            this.mStackSupervisor.startSpecificActivityLocked(next, true, true);
                        } else {
                            if (ActivityManagerDebugConfig.DEBUG_SWITCH) {
                                Slog.v(TAG_SWITCH, "Resume running: " + next + " stopped=" + next.stopped + " visible=" + next.visible);
                            }
                            int lastActivityTranslucent;
                            if (lastStack == null) {
                                lastActivityTranslucent = 0;
                            } else if (!lastStack.mFullscreen) {
                                lastActivityTranslucent = 1;
                            } else if (lastStack.mLastPausedActivity != null) {
                                lastActivityTranslucent = lastStack.mLastPausedActivity.fullscreen ^ 1;
                            } else {
                                lastActivityTranslucent = 0;
                            }
                            if (!(next.visible && !next.stopped && lastActivityTranslucent == 0)) {
                                next.setVisibility(true);
                            }
                            next.startLaunchTickingLocked();
                            ActivityRecord lastResumedActivity = lastStack == null ? null : lastStack.mResumedActivity;
                            ActivityState lastState = next.state;
                            this.mService.updateCpuStats();
                            if (ActivityManagerDebugConfig.DEBUG_STATES) {
                                Slog.v(TAG_STATES, "Moving to RESUMED: " + next + " (in existing)");
                            }
                            setResumedActivityLocked(next, "resumeTopActivityInnerLocked");
                            if (!this.mService.mActivityStarter.mCurActivityPkName.equals(next.packageName)) {
                                Jlog.warmLaunchingAppBegin(next.packageName);
                                Jlog.d(142, next.packageName, next.app.pid, "");
                                LogPower.push(113, next.packageName);
                                this.mService.mActivityStarter.mCurActivityPkName = next.packageName;
                            }
                            this.mService.updateLruProcessLocked(next.app, true, null);
                            updateLRUListLocked(next);
                            this.mService.updateOomAdjLocked();
                            boolean notUpdated = true;
                            if (this.mStackSupervisor.isFocusedStack(this)) {
                                Configuration config = this.mWindowManager.updateOrientationFromAppTokens(this.mStackSupervisor.getDisplayOverrideConfiguration(this.mDisplayId), next.mayFreezeScreenLocked(next.app) ? next.appToken : null, this.mDisplayId);
                                if (config != null) {
                                    next.frozenBeforeDestroy = true;
                                }
                                notUpdated = this.mService.updateDisplayOverrideConfigurationLocked(config, next, false, this.mDisplayId) ^ 1;
                            }
                            if (notUpdated) {
                                ActivityRecord nextNext = topRunningActivityLocked();
                                if (ActivityManagerDebugConfig.DEBUG_SWITCH || ActivityManagerDebugConfig.DEBUG_STATES) {
                                    Slog.i(TAG_STATES, "Activity config changed during resume: " + next + ", new next: " + nextNext);
                                }
                                if (nextNext != next) {
                                    this.mStackSupervisor.scheduleResumeTopActivities();
                                }
                                this.mService.setFocusedActivityLockedForNavi(next);
                                if (!next.visible || next.stopped) {
                                    next.setVisibility(true);
                                }
                                next.completeResumeLocked();
                                if (ActivityManagerDebugConfig.DEBUG_STACK) {
                                    this.mStackSupervisor.validateTopActivitiesLocked();
                                }
                                return true;
                            }
                            try {
                                ArrayList<ResultInfo> a = next.results;
                                if (a != null) {
                                    int N = a.size();
                                    if (!next.finishing && N > 0) {
                                        if (ActivityManagerDebugConfig.DEBUG_RESULTS) {
                                            Slog.v(TAG_RESULTS, "Delivering results to " + next + ": " + a);
                                        }
                                        next.app.thread.scheduleSendResult(next.appToken, a);
                                    }
                                }
                                if (next.newIntents != null) {
                                    next.app.thread.scheduleNewIntent(next.newIntents, next.appToken, false);
                                }
                                next.notifyAppResumed(next.stopped);
                                EventLog.writeEvent(EventLogTags.AM_RESUME_ACTIVITY, new Object[]{Integer.valueOf(next.userId), Integer.valueOf(System.identityHashCode(next)), Integer.valueOf(next.getTask().taskId), next.shortComponentName});
                                next.sleeping = false;
                                this.mService.showUnsupportedZoomDialogIfNeededLocked(next);
                                this.mService.showAskCompatModeDialogLocked(next);
                                next.app.pendingUiClean = true;
                                next.app.forceProcessStateUpTo(this.mService.mTopProcessState);
                                next.clearOptionsLocked();
                                resumeCustomActivity(next);
                                if (Jlog.isPerfTest()) {
                                    Jlog.i(2042, Intent.toPkgClsString(next.realActivity, "who"));
                                }
                                next.app.thread.scheduleResumeActivity(next.appToken, next.app.repProcState, this.mService.isNextTransitionForward(), resumeAnimOptions);
                                if (ActivityManagerDebugConfig.DEBUG_STATES) {
                                    Slog.d(TAG_STATES, "resumeTopActivityLocked: Resumed " + next);
                                }
                                try {
                                    ActivityStackSupervisor activityStackSupervisor = this.mStackSupervisor;
                                    activityStackSupervisor.mActivityLaunchTrack += " resumeTopComplete";
                                    next.completeResumeLocked();
                                    if (Jlog.isUBMEnable()) {
                                        StringBuilder append = new StringBuilder().append("AS#").append(next.intent.getComponent().flattenToShortString()).append("(").append(next.app.pid).append(",");
                                        String flattenToShortString = (prev == null || prev.intent == null) ? "null" : prev.intent.getComponent().flattenToShortString();
                                        append = append.append(flattenToShortString).append(",");
                                        Object valueOf = (prev == null || prev.app == null) ? "unknow" : Integer.valueOf(prev.app.pid);
                                        Jlog.d(273, append.append(valueOf).append(")").toString());
                                    }
                                } catch (Exception e3) {
                                    Slog.w(TAG, "Exception thrown during resume of " + next, e3);
                                    requestFinishActivityLocked(next.appToken, 0, null, "resume-exception", true);
                                    if (ActivityManagerDebugConfig.DEBUG_STACK) {
                                        this.mStackSupervisor.validateTopActivitiesLocked();
                                    }
                                    return true;
                                }
                            } catch (Exception e4) {
                                if (ActivityManagerDebugConfig.DEBUG_STATES) {
                                    Slog.v(TAG_STATES, "Resume failed; resetting state to " + lastState + ": " + next);
                                }
                                next.state = lastState;
                                if (lastStack != null) {
                                    lastStack.mResumedActivity = lastResumedActivity;
                                }
                                Slog.i(TAG, "Restarting because process died: " + next);
                                if (!next.hasBeenLaunched) {
                                    next.hasBeenLaunched = true;
                                } else if (lastStack != null && this.mStackSupervisor.isFrontStackOnDisplay(lastStack)) {
                                    next.showStartingWindow(null, false, false);
                                }
                                this.mStackSupervisor.startSpecificActivityLocked(next, true, false);
                                if (ActivityManagerDebugConfig.DEBUG_STACK) {
                                    this.mStackSupervisor.validateTopActivitiesLocked();
                                }
                                return true;
                            }
                        }
                        if (ActivityManagerDebugConfig.DEBUG_STACK) {
                            this.mStackSupervisor.validateTopActivitiesLocked();
                        }
                        return true;
                    }
                }
                Flog.i(101, "resumeTopActivityLocked: Skip resume: some activity pausing.");
                if (ActivityManagerDebugConfig.DEBUG_STACK) {
                    this.mStackSupervisor.validateTopActivitiesLocked();
                }
                return false;
            } else {
                Slog.w(TAG, "Skipping resume of top activity " + next + ": user " + next.userId + " is stopped");
                if (ActivityManagerDebugConfig.DEBUG_STACK) {
                    this.mStackSupervisor.validateTopActivitiesLocked();
                }
                return false;
            }
        }
        return resumeTopActivityInNextFocusableStack(prev, options, "noMoreActivities");
    }

    private boolean resumeTopActivityInNextFocusableStack(ActivityRecord prev, ActivityOptions options, String reason) {
        if (!(this.mFullscreen && (isOnHomeDisplay() ^ 1) == 0) && adjustFocusToNextFocusableStackLocked(reason)) {
            Flog.i(101, "resume focused stack in multiwindow");
            return this.mStackSupervisor.resumeFocusedStackTopActivityLocked(this.mStackSupervisor.getFocusedStack(), prev, null);
        }
        boolean resumeHomeStackTask;
        ActivityOptions.abort(options);
        if (ActivityManagerDebugConfig.DEBUG_STATES) {
            Slog.d(TAG_STATES, "resumeTopActivityInNextFocusableStack: " + reason + ", go home");
        }
        if (ActivityManagerDebugConfig.DEBUG_STACK) {
            this.mStackSupervisor.validateTopActivitiesLocked();
        }
        Jlog.d(24, "JL_LAUNCHER_STARTUP");
        if (isOnHomeDisplay()) {
            resumeHomeStackTask = this.mStackSupervisor.resumeHomeStackTask(prev, reason);
        } else {
            resumeHomeStackTask = false;
        }
        return resumeHomeStackTask;
    }

    private TaskRecord getNextTask(TaskRecord targetTask) {
        int index = this.mTaskHistory.indexOf(targetTask);
        if (index >= 0) {
            int numTasks = this.mTaskHistory.size();
            for (int i = index + 1; i < numTasks; i++) {
                TaskRecord task = (TaskRecord) this.mTaskHistory.get(i);
                if (task.userId == targetTask.userId || (this.mStackSupervisor.isCurrentProfileLocked(task.userId) && this.mStackSupervisor.isCurrentProfileLocked(targetTask.userId))) {
                    return task;
                }
            }
        }
        return null;
    }

    int getAdjustedPositionForTask(TaskRecord task, int suggestedPosition, ActivityRecord starting) {
        int maxPosition = this.mTaskHistory.size();
        if ((starting != null && starting.okToShowLocked()) || (starting == null && task.okToShowLocked())) {
            return Math.min(suggestedPosition, maxPosition);
        }
        while (maxPosition > 0) {
            TaskRecord tmpTask = (TaskRecord) this.mTaskHistory.get(maxPosition - 1);
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
            return;
        }
        position = getAdjustedPositionForTask(task, position, null);
        this.mTaskHistory.remove(task);
        this.mTaskHistory.add(position, task);
        this.mWindowContainerController.positionChildAt(task.getWindowContainerController(), position, task.mBounds, task.getOverrideConfiguration());
        updateTaskMovement(task, true);
    }

    protected void insertTaskAtTop(TaskRecord task, ActivityRecord starting) {
        updateTaskReturnToForTopInsertion(task);
        this.mTaskHistory.remove(task);
        this.mTaskHistory.add(getAdjustedPositionForTask(task, this.mTaskHistory.size(), starting), task);
        updateTaskMovement(task, true);
        if (this.mStackId == 0 && "com.huawei.android.launcher".equals(task.affinity)) {
            ActivityStack topStack = getTopStackOnDisplay();
            int topStackId = topStack.mStackId;
            if (topStackId == 1 && (topStack.topActivity().fullscreen ^ 1) != 0) {
                Slog.i(TAG, "Do not position child when not fullscreen acvivity back to home");
                return;
            } else if (topStackId == 4) {
                int stackBehindTopIndex = this.mStacks.indexOf(topStack) - 1;
                while (stackBehindTopIndex >= 0 && ((ActivityStack) this.mStacks.get(stackBehindTopIndex)).topRunningActivityLocked() == null) {
                    stackBehindTopIndex--;
                }
                if ((stackBehindTopIndex >= 0 ? ((ActivityStack) this.mStacks.get(stackBehindTopIndex)).mStackId : -1) == 1 && stackBehindTopIndex >= 0 && (((ActivityStack) this.mStacks.get(stackBehindTopIndex)).topActivity().fullscreen ^ 1) != 0) {
                    Slog.i(TAG, "Do not position child when not fullscreen acvivity back to home in PIP mode");
                    return;
                }
            }
        }
        this.mWindowContainerController.positionChildAtTop(task.getWindowContainerController(), true);
    }

    private void updateTaskReturnToForTopInsertion(TaskRecord task) {
        boolean isLastTaskOverHome = false;
        if (task.isOverHomeStack() || task.isOverAssistantStack()) {
            TaskRecord nextTask = getNextTask(task);
            if (nextTask == null || nextTask.topRunningActivityLocked() == null) {
                isLastTaskOverHome = true;
            } else {
                Flog.i(101, "insertTaskAtTop, adjustNextTask: " + nextTask + ", nextTask ReturnTo: " + nextTask.getTaskToReturnTo() + ", current task: " + task + ", ReturnTo=" + task.getTaskToReturnTo());
                nextTask.setTaskToReturnTo(task.getTaskToReturnTo());
            }
        }
        if (isOnHomeDisplay()) {
            ActivityStack lastStack = this.mStackSupervisor.getLastStack();
            if (lastStack != null) {
                if (lastStack.isAssistantStack()) {
                    task.setTaskToReturnTo(3);
                    return;
                }
                boolean fromHomeOrRecents = lastStack.isHomeOrRecentsStack();
                TaskRecord topTask = lastStack.topTask();
                if (!isHomeOrRecentsStack() && (fromHomeOrRecents || topTask() != task)) {
                    int returnToType = isLastTaskOverHome ? task.getTaskToReturnTo() : 0;
                    if (fromHomeOrRecents && StackId.allowTopTaskToReturnHome(this.mStackId)) {
                        returnToType = topTask == null ? 1 : topTask.taskType;
                    }
                    task.setTaskToReturnTo(returnToType);
                }
                return;
            }
            return;
        }
        task.setTaskToReturnTo(0);
    }

    final void startActivityLocked(ActivityRecord r, ActivityRecord focusedTopActivity, boolean newTask, boolean keepCurTransition, ActivityOptions options) {
        TaskRecord rTask = r.getTask();
        int taskId = rTask.taskId;
        if (!r.mLaunchTaskBehind && (taskForIdLocked(taskId) == null || newTask)) {
            insertTaskAtTop(rTask, r);
        }
        TaskRecord task = null;
        if (!newTask) {
            boolean startIt = true;
            for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
                task = (TaskRecord) this.mTaskHistory.get(taskNdx);
                if (task.getTopActivity() != null) {
                    if (task == rTask) {
                        if (!startIt) {
                            if (ActivityManagerDebugConfig.DEBUG_ADD_REMOVE) {
                                Slog.i(TAG, "Adding activity " + r + " to task " + task, new RuntimeException("here").fillInStackTrace());
                            }
                            r.createWindowContainer(r.info.navigationHide);
                            ActivityOptions.abort(options);
                            return;
                        }
                    } else if (task.numFullscreen > 0) {
                        Flog.i(101, "starting r: " + r + " blocked by task: " + task);
                        startIt = false;
                    }
                }
            }
        }
        TaskRecord activityTask = r.getTask();
        if (task == activityTask && this.mTaskHistory.indexOf(task) != this.mTaskHistory.size() - 1) {
            this.mStackSupervisor.mUserLeaving = false;
            if (ActivityManagerDebugConfig.DEBUG_USER_LEAVING) {
                Slog.v(TAG_USER_LEAVING, "startActivity() behind front, mUserLeaving=false");
            }
        }
        task = activityTask;
        if (ActivityManagerDebugConfig.DEBUG_ADD_REMOVE) {
            Slog.i(TAG, "Adding activity " + r + " to stack to task " + activityTask, new RuntimeException("here").fillInStackTrace());
        }
        if (r.getWindowContainerController() == null) {
            r.createWindowContainer(r.info.navigationHide);
        }
        activityTask.setFrontOfTask();
        if (this.mIsPerfBoostEnabled && this.mPerfBoost == null) {
            this.mPerfBoost = new BoostFramework();
        }
        if (this.mPerfBoost != null) {
            this.mPerfBoost.perfLockAcquire(this.lBoostTimeOut, this.lBoostCpuParamVal);
        }
        if (!isHomeOrRecentsStack() || numActivities() > 0) {
            if (ActivityManagerDebugConfig.DEBUG_TRANSITION) {
                Slog.v(TAG_TRANSITION, "Prepare open transition: starting " + r);
            }
            if ((r.intent.getFlags() & 65536) != 0) {
                this.mWindowManager.prepareAppTransition(0, keepCurTransition);
                this.mNoAnimActivities.add(r);
            } else {
                int transit = 6;
                if (newTask) {
                    if (r.mLaunchTaskBehind) {
                        transit = 16;
                    } else {
                        if (!(focusedTopActivity == null || focusedTopActivity.getStack().getStackId() == 4)) {
                            focusedTopActivity.supportsPictureInPictureWhilePausing = true;
                        }
                        transit = 8;
                    }
                }
                this.mWindowManager.prepareAppTransition(transit, keepCurTransition);
                this.mNoAnimActivities.remove(r);
            }
            boolean doShow = true;
            if (newTask) {
                if ((r.intent.getFlags() & DumpState.DUMP_COMPILER_STATS) != 0) {
                    resetTaskIfNeededLocked(r, r);
                    doShow = topRunningNonDelayedActivityLocked(null) == r;
                }
            } else if (options != null && options.getAnimationType() == 5) {
                doShow = false;
            }
            Flog.i(301, "startActivityLocked doShow= " + doShow + " mLaunchTaskBehind= " + r.mLaunchTaskBehind);
            if (r.mLaunchTaskBehind) {
                r.setVisibility(true);
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
                if (isSplitActivity(r.intent)) {
                    this.mWindowManager.setSplittable(true);
                } else if (this.mWindowManager.isSplitMode()) {
                    this.mWindowManager.setSplittable(false);
                }
                r.showStartingWindow(prev, newTask, isTaskSwitch(r, focusedTopActivity));
            }
            this.mWindowManager.prepareForForceRotation(r.appToken.asBinder(), r.packageName, r.shortComponentName);
        } else {
            ActivityOptions.abort(options);
        }
    }

    private boolean isTaskSwitch(ActivityRecord r, ActivityRecord topFocusedActivity) {
        return (topFocusedActivity == null || r.getTask() == topFocusedActivity.getTask()) ? false : true;
    }

    private ActivityOptions resetTargetTaskIfNeededLocked(TaskRecord task, boolean forceReset) {
        ActivityOptions topOptions = null;
        int replyChainEnd = -1;
        boolean canMoveOptions = true;
        ArrayList<ActivityRecord> activities = task.mActivities;
        int numActivities = activities.size();
        int rootActivityNdx = task.findEffectiveRootIndex();
        for (int i = numActivities - 1; i > rootActivityNdx; i--) {
            ActivityRecord target = (ActivityRecord) activities.get(i);
            if (target.frontOfTask) {
                break;
            }
            int flags = target.info.flags;
            boolean finishOnTaskLaunch = (flags & 2) != 0;
            boolean allowTaskReparenting = (flags & 64) != 0;
            boolean clearWhenTaskReset = (target.intent.getFlags() & DumpState.DUMP_FROZEN) != 0;
            boolean noOptions;
            int srcPos;
            ActivityRecord p;
            if (!finishOnTaskLaunch && (clearWhenTaskReset ^ 1) != 0 && target.resultTo != null) {
                Flog.i(105, "ResetTask:Keeping the end of the reply chain, target= " + target.task + " targetI=" + i + " replyChainEnd=" + replyChainEnd);
                if (replyChainEnd < 0) {
                    replyChainEnd = i;
                }
            } else if (!finishOnTaskLaunch && (clearWhenTaskReset ^ 1) != 0 && allowTaskReparenting && target.taskAffinity != null && (target.taskAffinity.equals(task.affinity) ^ 1) != 0) {
                TaskRecord targetTask;
                ActivityRecord bottom = (this.mTaskHistory.isEmpty() || (((TaskRecord) this.mTaskHistory.get(0)).mActivities.isEmpty() ^ 1) == 0) ? null : (ActivityRecord) ((TaskRecord) this.mTaskHistory.get(0)).mActivities.get(0);
                if (bottom == null || target.taskAffinity == null || !target.taskAffinity.equals(bottom.getTask().affinity)) {
                    targetTask = createTaskRecord(this.mStackSupervisor.getNextTaskIdForUserLocked(target.userId), target.info, null, null, null, false, target.mActivityType);
                    targetTask.affinityIntent = target.intent;
                    Flog.i(105, "ResetTask:Start pushing activity " + target + " out to new task " + targetTask);
                } else {
                    targetTask = bottom.getTask();
                    Flog.i(105, "ResetTask:Start pushing activity " + target + " out to bottom task " + targetTask);
                }
                noOptions = canMoveOptions;
                for (srcPos = replyChainEnd < 0 ? i : replyChainEnd; srcPos >= i; srcPos--) {
                    p = (ActivityRecord) activities.get(srcPos);
                    if (!p.finishing) {
                        canMoveOptions = false;
                        if (noOptions && topOptions == null) {
                            topOptions = p.takeOptionsLocked();
                            if (topOptions != null) {
                                noOptions = false;
                            }
                        }
                        if (ActivityManagerDebugConfig.DEBUG_ADD_REMOVE) {
                            Slog.i(TAG_ADD_REMOVE, "Removing activity " + p + " from task=" + task + " adding to task=" + targetTask + " Callers=" + Debug.getCallers(4));
                        }
                        Flog.i(105, "ResetTask:Pushing next activity " + p + " out to target's task " + target);
                        p.reparent(targetTask, 0, "resetTargetTaskIfNeeded");
                    }
                }
                this.mWindowContainerController.positionChildAtBottom(targetTask.getWindowContainerController());
                replyChainEnd = -1;
            } else if (forceReset || finishOnTaskLaunch || clearWhenTaskReset) {
                int end;
                if (clearWhenTaskReset) {
                    end = activities.size() - 1;
                } else if (replyChainEnd < 0) {
                    end = i;
                } else {
                    end = replyChainEnd;
                }
                noOptions = canMoveOptions;
                srcPos = i;
                while (srcPos <= end) {
                    p = (ActivityRecord) activities.get(srcPos);
                    if (!p.finishing) {
                        canMoveOptions = false;
                        if (noOptions && topOptions == null) {
                            topOptions = p.takeOptionsLocked();
                            if (topOptions != null) {
                                noOptions = false;
                            }
                        }
                        Flog.i(105, "resetTaskIntendedTask: calling finishActivity on " + p);
                        if (finishActivityLocked(p, 0, null, "reset-task", false)) {
                            end--;
                            srcPos--;
                        }
                    }
                    srcPos++;
                }
                replyChainEnd = -1;
            } else {
                replyChainEnd = -1;
            }
        }
        return topOptions;
    }

    private int resetAffinityTaskIfNeededLocked(TaskRecord affinityTask, TaskRecord task, boolean topTaskIsHigher, boolean forceReset, int taskInsertionPoint) {
        int replyChainEnd = -1;
        int taskId = task.taskId;
        String taskAffinity = task.affinity;
        ArrayList<ActivityRecord> activities = affinityTask.mActivities;
        int numActivities = activities.size();
        int rootActivityNdx = affinityTask.findEffectiveRootIndex();
        for (int i = numActivities - 1; i > rootActivityNdx; i--) {
            ActivityRecord target = (ActivityRecord) activities.get(i);
            if (target.frontOfTask) {
                break;
            }
            int flags = target.info.flags;
            boolean finishOnTaskLaunch = (flags & 2) != 0;
            boolean allowTaskReparenting = (flags & 64) != 0;
            if (target.resultTo != null) {
                Flog.i(105, "ResetTaskAffinity:Keeping the end of the reply chain, target= " + target.task + " targetI=" + i + " replyChainEnd=" + replyChainEnd);
                if (replyChainEnd < 0) {
                    replyChainEnd = i;
                }
            } else if (topTaskIsHigher && allowTaskReparenting && taskAffinity != null) {
                if (taskAffinity.equals(target.taskAffinity)) {
                    int start;
                    int srcPos;
                    ActivityRecord p;
                    if (forceReset || finishOnTaskLaunch) {
                        start = replyChainEnd >= 0 ? replyChainEnd : i;
                        if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                            Slog.v(TAG_TASKS, "Finishing task at index " + start + " to " + i);
                        }
                        for (srcPos = start; srcPos >= i; srcPos--) {
                            p = (ActivityRecord) activities.get(srcPos);
                            if (!p.finishing) {
                                Flog.i(105, "ResetTaskAffinity:finishActivity pos:  " + srcPos + " acitivity: " + p);
                                finishActivityLocked(p, 0, null, "move-affinity", false);
                            }
                        }
                    } else {
                        if (taskInsertionPoint < 0) {
                            taskInsertionPoint = task.mActivities.size();
                        }
                        start = replyChainEnd >= 0 ? replyChainEnd : i;
                        if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                            Slog.v(TAG_TASKS, "Reparenting from task=" + affinityTask + ":" + start + "-" + i + " to task=" + task + ":" + taskInsertionPoint);
                        }
                        for (srcPos = start; srcPos >= i; srcPos--) {
                            p = (ActivityRecord) activities.get(srcPos);
                            p.reparent(task, taskInsertionPoint, "resetAffinityTaskIfNeededLocked");
                            if (ActivityManagerDebugConfig.DEBUG_ADD_REMOVE) {
                                Slog.i(TAG_ADD_REMOVE, "Removing and adding activity " + p + " to stack at " + task + " callers=" + Debug.getCallers(3));
                            }
                            Flog.i(105, "ResetTaskAffinity:Pulling activity " + p + " from " + srcPos + " in to resetting task " + task);
                        }
                        this.mWindowContainerController.positionChildAtTop(task.getWindowContainerController(), true);
                        if (target.info.launchMode == 1) {
                            ArrayList<ActivityRecord> taskActivities = task.mActivities;
                            int targetNdx = taskActivities.indexOf(target);
                            if (targetNdx > 0) {
                                p = (ActivityRecord) taskActivities.get(targetNdx - 1);
                                if (p.intent.getComponent().equals(target.intent.getComponent())) {
                                    Flog.i(105, "ResetTaskAffinity:Drop singleTop activity " + p + " for target " + target);
                                    finishActivityLocked(p, 0, null, "replace", false);
                                }
                            }
                        }
                    }
                    replyChainEnd = -1;
                }
            }
        }
        return taskInsertionPoint;
    }

    final ActivityRecord resetTaskIfNeededLocked(ActivityRecord taskTop, ActivityRecord newActivity) {
        boolean forceReset = (newActivity.info.flags & 4) != 0;
        TaskRecord task = taskTop.getTask();
        boolean taskFound = false;
        ActivityOptions topOptions = null;
        int reparentInsertionPoint = -1;
        for (int i = this.mTaskHistory.size() - 1; i >= 0; i--) {
            TaskRecord targetTask = (TaskRecord) this.mTaskHistory.get(i);
            if (targetTask == task) {
                topOptions = resetTargetTaskIfNeededLocked(task, forceReset);
                taskFound = true;
            } else {
                reparentInsertionPoint = resetAffinityTaskIfNeededLocked(targetTask, task, taskFound, forceReset, reparentInsertionPoint);
            }
        }
        int taskNdx = this.mTaskHistory.indexOf(task);
        if (taskNdx >= 0) {
            while (true) {
                int taskNdx2 = taskNdx - 1;
                taskTop = ((TaskRecord) this.mTaskHistory.get(taskNdx)).getTopActivity();
                if (taskTop != null || taskNdx2 < 0) {
                } else {
                    taskNdx = taskNdx2;
                }
            }
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

    void sendActivityResultLocked(int callingUid, ActivityRecord r, String resultWho, int requestCode, int resultCode, Intent data) {
        if (callingUid > 0) {
            this.mService.grantUriPermissionFromIntentLocked(callingUid, r.packageName, data, r.getUriPermissionsLocked(), r.userId);
        }
        if (ActivityManagerDebugConfig.DEBUG_RESULTS) {
            Slog.v(TAG, "Send activity result to " + r + " : who=" + resultWho + " req=" + requestCode + " res=" + resultCode + " data=" + data);
        }
        if (!(this.mResumedActivity != r || r.app == null || r.app.thread == null)) {
            try {
                ArrayList<ResultInfo> list = new ArrayList();
                list.add(new ResultInfo(resultWho, requestCode, resultCode, data));
                r.app.thread.scheduleSendResult(r.appToken, list);
                return;
            } catch (Exception e) {
                Slog.w(TAG, "Exception thrown sending result to " + r, e);
            }
        }
        r.addResultLocked(null, resultWho, requestCode, resultCode, data);
    }

    boolean isATopFinishingTask(TaskRecord task) {
        for (int i = this.mTaskHistory.size() - 1; i >= 0; i--) {
            TaskRecord current = (TaskRecord) this.mTaskHistory.get(i);
            if (current.topRunningActivityLocked() != null) {
                return false;
            }
            if (current == task) {
                return true;
            }
        }
        return false;
    }

    private void adjustFocusedActivityStackLocked(ActivityRecord r, String reason) {
        if (r != null && (this.mStackSupervisor.isFocusedStack(this) ^ 1) == 0 && (this.mResumedActivity == r || this.mResumedActivity == null)) {
            ActivityRecord next = topRunningActivityLocked();
            String myReason = reason + " adjustFocus";
            if (next != r) {
                if (next == null || !StackId.keepFocusInStackIfPossible(this.mStackId) || !isFocusable()) {
                    if (r != null) {
                        TaskRecord task = r.getTask();
                        if (task == null) {
                            throw new IllegalStateException("activity no longer associated with task:" + r);
                        }
                        boolean isAssistantOrOverAssistant;
                        if (task.getStack().isAssistantStack()) {
                            isAssistantOrOverAssistant = true;
                        } else {
                            isAssistantOrOverAssistant = task.isOverAssistantStack();
                        }
                        if (r.frontOfTask && isATopFinishingTask(task) && (task.isOverHomeStack() || isAssistantOrOverAssistant)) {
                            if ((!this.mFullscreen || isAssistantOrOverAssistant) && adjustFocusToNextFocusableStackLocked(myReason)) {
                                Flog.i(101, "adjustFocus in multiwindow reason=" + reason);
                                return;
                            } else if (task.isOverHomeStack() && this.mStackSupervisor.moveHomeStackTaskToTop(myReason)) {
                                return;
                            }
                        }
                    }
                }
                return;
            }
            this.mStackSupervisor.moveFocusableActivityStackToFrontLocked(this.mStackSupervisor.topRunningActivityLocked(), myReason);
        }
    }

    protected boolean adjustFocusToNextFocusableStackLocked(String reason) {
        return adjustFocusToNextFocusableStackLocked(reason, false);
    }

    private boolean adjustFocusToNextFocusableStackLocked(String reason, boolean allowFocusSelf) {
        ActivityStack activityStack = null;
        ActivityStackSupervisor activityStackSupervisor = this.mStackSupervisor;
        if (!allowFocusSelf) {
            activityStack = this;
        }
        ActivityStack stack = activityStackSupervisor.getNextFocusableStackLocked(activityStack);
        String myReason = reason + " adjustFocusToNextFocusableStack";
        if (stack == null) {
            return false;
        }
        ActivityRecord top = stack.topRunningActivityLocked();
        if (stack.isHomeOrRecentsStack() && (top == null || (top.visible ^ 1) != 0)) {
            return this.mStackSupervisor.moveHomeStackTaskToTop(reason);
        }
        stack.moveToFront(myReason);
        return true;
    }

    final void stopActivityLocked(ActivityRecord r) {
        if (ActivityManagerDebugConfig.DEBUG_SWITCH) {
            Slog.d(TAG_SWITCH, "Stopping: " + r);
        }
        if (!(((r.intent.getFlags() & 1073741824) == 0 && (r.info.flags & 128) == 0) || r.finishing)) {
            if (!this.mService.isSleepingLocked()) {
                if (ActivityManagerDebugConfig.DEBUG_STATES) {
                    Slog.d(TAG_STATES, "no-history finish of " + r);
                }
                if (requestFinishActivityLocked(r.appToken, 0, null, "stop-no-history", false)) {
                    r.resumeKeyDispatchingLocked();
                    return;
                }
            } else if (ActivityManagerDebugConfig.DEBUG_STATES) {
                Slog.d(TAG_STATES, "Not finishing noHistory " + r + " on stop because we're just sleeping");
            }
        }
        if (!(r.app == null || r.app.thread == null)) {
            adjustFocusedActivityStackLocked(r, "stopActivity");
            r.resumeKeyDispatchingLocked();
            try {
                r.stopped = false;
                if (ActivityManagerDebugConfig.DEBUG_STATES || HwSlog.HW_DEBUG_STATES) {
                    Slog.v(TAG_STATES, "Moving to STOPPING: " + r + " (stop requested)");
                }
                r.state = ActivityState.STOPPING;
                if (ActivityManagerDebugConfig.DEBUG_VISIBILITY) {
                    Slog.v(TAG_VISIBILITY, "Stopping visible=" + r.visible + " for " + r);
                }
                if (!r.visible) {
                    r.setVisible(false);
                }
                EventLogTags.writeAmStopActivity(r.userId, System.identityHashCode(r), r.shortComponentName);
                this.mService.notifyActivityState(r, ActivityState.STOPPED);
                r.app.thread.scheduleStopActivity(r.appToken, r.visible, r.configChangeFlags);
                if (this.mService.isSleepingOrShuttingDownLocked()) {
                    r.setSleeping(true);
                }
                this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(104, r), JobStatus.DEFAULT_TRIGGER_UPDATE_DELAY);
            } catch (Exception e) {
                Slog.w(TAG, "Exception thrown during pause", e);
                r.stopped = true;
                if (ActivityManagerDebugConfig.DEBUG_STATES) {
                    Slog.v(TAG_STATES, "Stop failed; moving to STOPPED: " + r);
                }
                r.state = ActivityState.STOPPED;
                if (r.deferRelaunchUntilPaused) {
                    destroyActivityLocked(r, true, "stop-except");
                }
            }
        }
    }

    final boolean requestFinishActivityLocked(IBinder token, int resultCode, Intent resultData, String reason, boolean oomAdj) {
        ActivityRecord r = isInStackLocked(token);
        if (ActivityManagerDebugConfig.DEBUG_RESULTS || ActivityManagerDebugConfig.DEBUG_STATES || HwSlog.HW_DEBUG_STATES) {
            Slog.v(TAG_STATES, "Finishing activity token=" + token + " r=" + ", result=" + resultCode + ", data=" + resultData + ", reason=" + reason);
        }
        if (r == null) {
            return false;
        }
        finishActivityLocked(r, resultCode, resultData, reason, oomAdj);
        return true;
    }

    final void finishSubActivityLocked(ActivityRecord self, String resultWho, int requestCode) {
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            ArrayList<ActivityRecord> activities = ((TaskRecord) this.mTaskHistory.get(taskNdx)).mActivities;
            for (int activityNdx = activities.size() - 1; activityNdx >= 0; activityNdx--) {
                ActivityRecord r = (ActivityRecord) activities.get(activityNdx);
                if (r.resultTo == self && r.requestCode == requestCode && ((r.resultWho == null && resultWho == null) || (r.resultWho != null && r.resultWho.equals(resultWho)))) {
                    finishActivityLocked(r, 0, null, "request-sub", false);
                }
            }
        }
        this.mService.updateOomAdjLocked();
    }

    final TaskRecord finishTopRunningActivityLocked(ProcessRecord app, String reason) {
        ActivityRecord r = topRunningActivityLocked();
        if (r == null || r.app != app) {
            return null;
        }
        Slog.w(TAG, "  Force finishing activity " + r.intent.getComponent().flattenToShortString());
        TaskRecord finishedTask = r.getTask();
        int taskNdx = this.mTaskHistory.indexOf(finishedTask);
        TaskRecord task = finishedTask;
        int activityNdx = finishedTask.mActivities.indexOf(r);
        finishActivityLocked(r, 0, null, reason, false);
        activityNdx--;
        if (activityNdx < 0) {
            while (true) {
                taskNdx--;
                if (taskNdx >= 0) {
                    activityNdx = ((TaskRecord) this.mTaskHistory.get(taskNdx)).mActivities.size() - 1;
                    if (activityNdx >= 0) {
                        break;
                    }
                } else {
                    break;
                }
            }
        }
        if (activityNdx >= 0 && taskNdx < this.mTaskHistory.size() && activityNdx < ((TaskRecord) this.mTaskHistory.get(taskNdx)).mActivities.size()) {
            r = (ActivityRecord) ((TaskRecord) this.mTaskHistory.get(taskNdx)).mActivities.get(activityNdx);
            if ((r.state == ActivityState.RESUMED || r.state == ActivityState.PAUSING || r.state == ActivityState.PAUSED) && !(r.isHomeActivity() && this.mService.mHomeProcess == r.app)) {
                Slog.w(TAG, "  Force finishing activity " + r.intent.getComponent().flattenToShortString());
                finishActivityLocked(r, 0, null, reason, false);
            }
        }
        return finishedTask;
    }

    final void finishVoiceTask(IVoiceInteractionSession session) {
        IBinder sessionBinder = session.asBinder();
        boolean didOne = false;
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            TaskRecord tr = (TaskRecord) this.mTaskHistory.get(taskNdx);
            int activityNdx;
            ActivityRecord r;
            if (tr.voiceSession == null || tr.voiceSession.asBinder() != sessionBinder) {
                for (activityNdx = tr.mActivities.size() - 1; activityNdx >= 0; activityNdx--) {
                    r = (ActivityRecord) tr.mActivities.get(activityNdx);
                    if (r.voiceSession != null && r.voiceSession.asBinder() == sessionBinder) {
                        r.clearVoiceSessionLocked();
                        try {
                            r.app.thread.scheduleLocalVoiceInteractionStarted(r.appToken, null);
                        } catch (RemoteException e) {
                        }
                        this.mService.finishRunningVoiceLocked();
                        break;
                    }
                }
            } else {
                for (activityNdx = tr.mActivities.size() - 1; activityNdx >= 0; activityNdx--) {
                    r = (ActivityRecord) tr.mActivities.get(activityNdx);
                    if (!r.finishing) {
                        finishActivityLocked(r, 0, null, "finish-voice", false);
                        didOne = true;
                    }
                }
            }
        }
        if (didOne) {
            this.mService.updateOomAdjLocked();
        }
    }

    final boolean finishActivityAffinityLocked(ActivityRecord r) {
        ArrayList<ActivityRecord> activities = r.getTask().mActivities;
        for (int index = activities.indexOf(r); index >= 0; index--) {
            ActivityRecord cur = (ActivityRecord) activities.get(index);
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
                Slog.v(TAG_RESULTS, "Adding result to " + resultTo + " who=" + r.resultWho + " req=" + r.requestCode + " res=" + resultCode + " data=" + resultData);
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
            Slog.v(TAG_RESULTS, "No result destination from " + r);
        }
        r.results = null;
        r.pendingResults = null;
        r.newIntents = null;
        r.icicle = null;
    }

    final boolean finishActivityLocked(ActivityRecord r, int resultCode, Intent resultData, String reason, boolean oomAdj) {
        return finishActivityLocked(r, resultCode, resultData, reason, oomAdj, false);
    }

    final boolean finishActivityLocked(ActivityRecord r, int resultCode, Intent resultData, String reason, boolean oomAdj, boolean pauseImmediately) {
        if (r.finishing) {
            Slog.w(TAG, "Duplicate finish request for " + r);
            return false;
        }
        this.mWindowManager.deferSurfaceLayout();
        try {
            r.makeFinishingLocked();
            TaskRecord task = r.getTask();
            if (task == null) {
                Slog.w(TAG, "finishActivityLocked: r.getTask is null!");
                return false;
            }
            EventLog.writeEvent(EventLogTags.AM_FINISH_ACTIVITY, new Object[]{Integer.valueOf(r.userId), Integer.valueOf(System.identityHashCode(r)), Integer.valueOf(task.taskId), r.shortComponentName, reason});
            ArrayList<ActivityRecord> activities = task.mActivities;
            int index = activities.indexOf(r);
            if (index < activities.size() - 1) {
                task.setFrontOfTask();
                if ((r.intent.getFlags() & DumpState.DUMP_FROZEN) != 0) {
                    ((ActivityRecord) activities.get(index + 1)).intent.addFlags(DumpState.DUMP_FROZEN);
                }
            }
            r.pauseKeyDispatchingLocked();
            adjustFocusedActivityStackLocked(r, "finishActivity");
            finishActivityResultsLocked(r, resultCode, resultData);
            boolean endTask = index <= 0;
            int transit = endTask ? 9 : 7;
            if (this.mResumedActivity == r) {
                if (ActivityManagerDebugConfig.DEBUG_VISIBILITY || ActivityManagerDebugConfig.DEBUG_TRANSITION) {
                    Slog.v(TAG_TRANSITION, "Prepare close transition: finishing " + r);
                }
                if (endTask) {
                    this.mService.mTaskChangeNotificationController.notifyTaskRemovalStarted(task.taskId);
                }
                this.mWindowManager.prepareAppTransition(transit, false);
                r.setVisibility(false);
                if (this.mPausingActivity == null) {
                    if (ActivityManagerDebugConfig.DEBUG_PAUSE) {
                        Slog.v(TAG_PAUSE, "Finish needs to pause: " + r);
                    }
                    if (ActivityManagerDebugConfig.DEBUG_USER_LEAVING) {
                        Slog.v(TAG_USER_LEAVING, "finish() => pause with userLeaving=false");
                    }
                    startPausingLocked(false, false, null, pauseImmediately);
                }
                if (endTask) {
                    this.mStackSupervisor.removeLockedTaskLocked(task);
                }
            } else if (r.state != ActivityState.PAUSING) {
                int finishMode;
                if (ActivityManagerDebugConfig.DEBUG_PAUSE) {
                    Slog.v(TAG_PAUSE, "Finish not pausing: " + r);
                }
                if (r.visible && (r.isSplitMode() ^ 1) != 0) {
                    prepareActivityHideTransitionAnimation(r, transit);
                }
                if (r.visible || r.nowVisible) {
                    finishMode = 2;
                } else {
                    finishMode = 1;
                }
                boolean removedActivity = finishCurrentActivityLocked(r, finishMode, oomAdj) == null;
                if (task.onlyHasTaskOverlayActivities(true)) {
                    for (ActivityRecord taskOverlay : task.mActivities) {
                        if (taskOverlay.mTaskOverlay) {
                            prepareActivityHideTransitionAnimation(taskOverlay, transit);
                        }
                    }
                }
                this.mWindowManager.continueSurfaceLayout();
                return removedActivity;
            } else if (ActivityManagerDebugConfig.DEBUG_PAUSE) {
                Slog.v(TAG_PAUSE, "Finish waiting for pause of: " + r);
            }
            this.mWindowManager.continueSurfaceLayout();
            return false;
        } finally {
            this.mWindowManager.continueSurfaceLayout();
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

    final ActivityRecord finishCurrentActivityLocked(ActivityRecord r, int mode, boolean oomAdj) {
        ActivityRecord next = this.mStackSupervisor.topRunningActivityLocked();
        if (mode != 2 || (!(r.visible || r.nowVisible) || next == null || (next.nowVisible && !(HwPCUtils.isPcCastModeInServer() && HwPCUtils.isPcDynamicStack(r.getStackId()))))) {
            this.mStackSupervisor.mStoppingActivities.remove(r);
            this.mStackSupervisor.mGoingToSleepActivities.remove(r);
            this.mStackSupervisor.mActivitiesWaitingForVisibleActivity.remove(r);
            if (this.mResumedActivity == r) {
                this.mResumedActivity = null;
            }
            ActivityState prevState = r.state;
            if (ActivityManagerDebugConfig.DEBUG_STATES || HwSlog.HW_DEBUG_STATES) {
                Slog.v(TAG_STATES, "Moving to FINISHING: " + r);
            }
            r.state = ActivityState.FINISHING;
            boolean finishingActivityInNonFocusedStack = (r.getStack() == this.mStackSupervisor.getFocusedStack() || prevState != ActivityState.PAUSED) ? false : mode == 2;
            if (mode == 0 || ((prevState == ActivityState.PAUSED && (mode == 1 || this.mStackId == 4)) || finishingActivityInNonFocusedStack || prevState == ActivityState.STOPPING || prevState == ActivityState.STOPPED || prevState == ActivityState.INITIALIZING)) {
                r.makeFinishingLocked();
                boolean activityRemoved = destroyActivityLocked(r, true, "finish-imm");
                if (finishingActivityInNonFocusedStack) {
                    this.mStackSupervisor.ensureActivitiesVisibleLocked(null, 0, false);
                    Flog.i(101, "Moving to FINISHING r=" + r + " in multiwindow, destroy returned removed=" + activityRemoved);
                }
                if (activityRemoved) {
                    this.mStackSupervisor.mActivityLaunchTrack = "finishImmAtivityRemoved";
                    this.mStackSupervisor.resumeFocusedStackTopActivityLocked();
                }
                if (ActivityManagerDebugConfig.DEBUG_CONTAINERS) {
                    Slog.d(TAG_CONTAINERS, "destroyActivityLocked: finishCurrentActivityLocked r=" + r + " destroy returned removed=" + activityRemoved);
                }
                if (activityRemoved) {
                    r = null;
                }
                return r;
            }
            if (ActivityManagerDebugConfig.DEBUG_ALL) {
                Slog.v(TAG, "Enqueueing pending finish: " + r);
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
        r.state = ActivityState.STOPPING;
        if (oomAdj) {
            this.mService.updateOomAdjLocked();
        }
        return r;
    }

    void finishAllActivitiesLocked(boolean immediately) {
        boolean noActivitiesInStack = true;
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            ArrayList<ActivityRecord> activities = ((TaskRecord) this.mTaskHistory.get(taskNdx)).mActivities;
            for (int activityNdx = activities.size() - 1; activityNdx >= 0; activityNdx--) {
                ActivityRecord r = (ActivityRecord) activities.get(activityNdx);
                noActivitiesInStack = false;
                if (!r.finishing || (immediately ^ 1) == 0) {
                    Slog.d(TAG, "finishAllActivitiesLocked: finishing " + r + " immediately");
                    finishCurrentActivityLocked(r, 0, false);
                }
            }
        }
        if (noActivitiesInStack) {
            this.mActivityContainer.onTaskListEmptyLocked();
        }
    }

    final boolean shouldUpRecreateTaskLocked(ActivityRecord srec, String destAffinity) {
        if (srec == null || srec.getTask().affinity == null || (srec.getTask().affinity.equals(destAffinity) ^ 1) != 0) {
            return true;
        }
        TaskRecord task = srec.getTask();
        if (srec.frontOfTask && task != null && task.getBaseIntent() != null && task.getBaseIntent().isDocument()) {
            if (task.getTaskToReturnTo() != 0) {
                return true;
            }
            int taskIdx = this.mTaskHistory.indexOf(task);
            if (taskIdx <= 0) {
                Slog.w(TAG, "shouldUpRecreateTask: task not in history for " + srec);
                return false;
            } else if (taskIdx == 0) {
                return true;
            } else {
                if (!task.affinity.equals(((TaskRecord) this.mTaskHistory.get(taskIdx)).affinity)) {
                    return true;
                }
            }
        }
        return false;
    }

    final boolean navigateUpToLocked(ActivityRecord srec, Intent destIntent, int resultCode, Intent resultData) {
        TaskRecord task = srec.getTask();
        ArrayList<ActivityRecord> activities = task.mActivities;
        int start = activities.indexOf(srec);
        if (!this.mTaskHistory.contains(task) || start < 0) {
            return false;
        }
        int i;
        int finishTo = start - 1;
        ActivityRecord parent = finishTo < 0 ? null : (ActivityRecord) activities.get(finishTo);
        boolean foundParentInTask = false;
        ComponentName dest = destIntent.getComponent();
        if (start > 0 && dest != null) {
            for (i = finishTo; i >= 0; i--) {
                ActivityRecord r = (ActivityRecord) activities.get(i);
                if (r.info.packageName.equals(dest.getPackageName()) && r.info.name.equals(dest.getClassName())) {
                    finishTo = i;
                    parent = r;
                    foundParentInTask = true;
                    break;
                }
            }
        }
        IActivityController controller = this.mService.mController;
        if (controller != null) {
            ActivityRecord next = topRunningActivityLocked(srec.appToken, 0);
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
        for (i = start; i > finishTo; i--) {
            requestFinishActivityLocked(((ActivityRecord) activities.get(i)).appToken, resultCode, resultData, "navigate-up", true);
            resultCode = 0;
            resultData = null;
        }
        if (parent != null && foundParentInTask) {
            int parentLaunchMode = parent.info.launchMode;
            int destIntentFlags = destIntent.getFlags();
            if (parentLaunchMode == 3 || parentLaunchMode == 2 || parentLaunchMode == 1 || (67108864 & destIntentFlags) != 0) {
                parent.deliverNewIntentLocked(srec.info.applicationInfo.uid, destIntent, srec.packageName);
            } else {
                try {
                    foundParentInTask = this.mService.mActivityStarter.startActivityLocked(srec.app.thread, destIntent, null, null, AppGlobals.getPackageManager().getActivityInfo(destIntent.getComponent(), 0, srec.userId), null, null, null, parent.appToken, null, 0, -1, parent.launchedFromUid, parent.launchedFromPackage, -1, parent.launchedFromUid, 0, null, false, true, null, null, null, "navigateUpTo") == 0;
                } catch (RemoteException e2) {
                    foundParentInTask = false;
                }
                requestFinishActivityLocked(parent.appToken, resultCode, resultData, "navigate-top", true);
            }
        }
        Binder.restoreCallingIdentity(origId);
        return foundParentInTask;
    }

    void onActivityRemovedFromStack(ActivityRecord r) {
        if (this.mResumedActivity == r) {
            this.mResumedActivity = null;
        }
        if (this.mPausingActivity == r) {
            Flog.i(101, "Remove the pausingActivity " + this.mPausingActivity + " in stack " + this.mStackId);
            this.mPausingActivity = null;
        }
        removeTimeoutsForActivityLocked(r);
    }

    private void cleanUpActivityLocked(ActivityRecord r, boolean cleanServices, boolean setState) {
        onActivityRemovedFromStack(r);
        r.deferRelaunchUntilPaused = false;
        r.frozenBeforeDestroy = false;
        if (setState) {
            if (ActivityManagerDebugConfig.DEBUG_STATES || HwSlog.HW_DEBUG_STATES) {
                Slog.v(TAG_STATES, "Moving to DESTROYED: " + r + " (cleaning up)");
            }
            r.state = ActivityState.DESTROYED;
            if (ActivityManagerDebugConfig.DEBUG_APP) {
                Slog.v(TAG_APP, "Clearing app during cleanUp for activity " + r);
            }
            r.app = null;
        }
        this.mStackSupervisor.cleanupActivity(r);
        if (r.finishing && r.pendingResults != null) {
            for (WeakReference<PendingIntentRecord> apr : r.pendingResults) {
                PendingIntentRecord rec = (PendingIntentRecord) apr.get();
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
        if (getVisibleBehindActivity() == r) {
            this.mStackSupervisor.requestVisibleBehindLocked(r, false);
        }
        this.mWindowManager.notifyAppRelaunchesCleared(r.appToken);
    }

    void removeTimeoutsForActivityLocked(ActivityRecord r) {
        if (r != null) {
            this.mStackSupervisor.removeTimeoutsForActivityLocked(r);
            this.mHandler.removeMessages(101, r);
            this.mHandler.removeMessages(104, r);
            this.mHandler.removeMessages(102, r);
            r.finishLaunchTickingLocked();
        }
    }

    private void removeActivityFromHistoryLocked(ActivityRecord r, String reason) {
        this.mStackSupervisor.removeChildActivityContainers(r);
        finishActivityResultsLocked(r, 0, null);
        r.makeFinishingLocked();
        if (ActivityManagerDebugConfig.DEBUG_ADD_REMOVE) {
            Slog.i(TAG_ADD_REMOVE, "Removing activity " + r + " from stack callers=" + Debug.getCallers(5));
        }
        r.takeFromHistory();
        removeTimeoutsForActivityLocked(r);
        if (ActivityManagerDebugConfig.DEBUG_STATES || HwSlog.HW_DEBUG_STATES) {
            Slog.v(TAG_STATES, "Moving to DESTROYED: " + r + " (removed from history)");
        }
        r.state = ActivityState.DESTROYED;
        if (ActivityManagerDebugConfig.DEBUG_APP) {
            Slog.v(TAG_APP, "Clearing app during remove for activity " + r);
        }
        r.app = null;
        r.removeWindowContainer();
        TaskRecord task = r.getTask();
        boolean lastActivity = task != null ? task.removeActivity(r) : false;
        boolean onlyHasTaskOverlays = task != null ? task.onlyHasTaskOverlayActivities(false) : false;
        if (lastActivity || onlyHasTaskOverlays) {
            if (ActivityManagerDebugConfig.DEBUG_STACK) {
                Slog.i(TAG_STACK, "removeActivityFromHistoryLocked: last activity removed from " + this + " onlyHasTaskOverlays=" + onlyHasTaskOverlays);
            }
            if (this.mStackSupervisor.isFocusedStack(this) && task == topTask() && task.isOverHomeStack()) {
                this.mStackSupervisor.moveHomeStackTaskToTop(reason);
            }
            if (onlyHasTaskOverlays) {
                this.mStackSupervisor.removeTaskByIdLocked(task.taskId, false, false, true);
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
                this.mService.mServices.removeConnectionLocked((ConnectionRecord) it.next(), null, r);
            }
            r.connections = null;
        }
    }

    final void scheduleDestroyActivities(ProcessRecord owner, String reason) {
        Message msg = this.mHandler.obtainMessage(105);
        msg.obj = new ScheduleDestroyArgs(owner, reason);
        this.mHandler.sendMessage(msg);
    }

    private void destroyActivitiesLocked(ProcessRecord owner, String reason) {
        boolean lastIsOpaque = false;
        boolean activityRemoved = false;
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            ArrayList<ActivityRecord> activities = ((TaskRecord) this.mTaskHistory.get(taskNdx)).mActivities;
            for (int activityNdx = activities.size() - 1; activityNdx >= 0; activityNdx--) {
                ActivityRecord r = (ActivityRecord) activities.get(activityNdx);
                if (!r.finishing) {
                    if (r.fullscreen) {
                        lastIsOpaque = true;
                    }
                    if ((owner == null || r.app == owner) && lastIsOpaque && r.isDestroyable()) {
                        if (ActivityManagerDebugConfig.DEBUG_SWITCH) {
                            Slog.v(TAG_SWITCH, "Destroying " + r + " in state " + r.state + " resumed=" + this.mResumedActivity + " pausing=" + this.mPausingActivity + " for reason " + reason);
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

    final boolean safelyDestroyActivityLocked(ActivityRecord r, String reason) {
        if (!r.isDestroyable()) {
            return false;
        }
        if (ActivityManagerDebugConfig.DEBUG_SWITCH) {
            Slog.v(TAG_SWITCH, "Destroying " + r + " in state " + r.state + " resumed=" + this.mResumedActivity + " pausing=" + this.mPausingActivity + " for reason " + reason);
        }
        return destroyActivityLocked(r, true, reason);
    }

    final int releaseSomeActivitiesLocked(ProcessRecord app, ArraySet<TaskRecord> tasks, String reason) {
        if (ActivityManagerDebugConfig.DEBUG_RELEASE) {
            Slog.d(TAG_RELEASE, "Trying to release some activities in " + app);
        }
        int maxTasks = tasks.size() / 4;
        if (maxTasks < 1) {
            maxTasks = 1;
        }
        int numReleased = 0;
        int taskNdx = 0;
        while (taskNdx < this.mTaskHistory.size() && maxTasks > 0) {
            TaskRecord task = (TaskRecord) this.mTaskHistory.get(taskNdx);
            if (tasks.contains(task)) {
                if (ActivityManagerDebugConfig.DEBUG_RELEASE) {
                    Slog.d(TAG_RELEASE, "Looking for activities to release in " + task);
                }
                int curNum = 0;
                ArrayList<ActivityRecord> activities = task.mActivities;
                int actNdx = 0;
                while (actNdx < activities.size()) {
                    ActivityRecord activity = (ActivityRecord) activities.get(actNdx);
                    if (activity.app == app && activity.isDestroyable()) {
                        if (ActivityManagerDebugConfig.DEBUG_RELEASE) {
                            Slog.v(TAG_RELEASE, "Destroying " + activity + " in state " + activity.state + " resumed=" + this.mResumedActivity + " pausing=" + this.mPausingActivity + " for reason " + reason);
                        }
                        destroyActivityLocked(activity, true, reason);
                        if (activities.get(actNdx) != activity) {
                            actNdx--;
                        }
                        curNum++;
                    }
                    actNdx++;
                }
                if (curNum > 0) {
                    numReleased += curNum;
                    maxTasks--;
                    if (this.mTaskHistory.get(taskNdx) != task) {
                        taskNdx--;
                    }
                }
            }
            taskNdx++;
        }
        if (ActivityManagerDebugConfig.DEBUG_RELEASE) {
            Slog.d(TAG_RELEASE, "Done releasing: did " + numReleased + " activities");
        }
        return numReleased;
    }

    final boolean destroyActivityLocked(ActivityRecord r, boolean removeFromApp, String reason) {
        if (ActivityManagerDebugConfig.DEBUG_SWITCH || ActivityManagerDebugConfig.DEBUG_CLEANUP) {
            Slog.v(TAG_SWITCH, "Removing activity from " + reason + ": token=" + r + ", app=" + (r.app != null ? r.app.processName : "(null)"));
        }
        EventLog.writeEvent(EventLogTags.AM_DESTROY_ACTIVITY, new Object[]{Integer.valueOf(r.userId), Integer.valueOf(System.identityHashCode(r)), Integer.valueOf(r.getTask().taskId), r.shortComponentName, reason});
        this.mService.notifyActivityState(r, ActivityState.DESTROYED);
        boolean removedFromHistory = false;
        cleanUpActivityLocked(r, false, false);
        if (ActivityManagerDebugConfig.DEBUG_SWITCH) {
            Slog.i(TAG_SWITCH, "Activity has been cleaned up!");
        }
        if (r.app != null) {
            this.mService.recognizeFakeActivity(r.shortComponentName, r.app.pid, r.app.uid);
        }
        boolean hadApp = r.app != null;
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
                    Slog.i(TAG_SWITCH, "Destroying: " + r);
                }
                r.app.thread.scheduleDestroyActivity(r.appToken, r.finishing, r.configChangeFlags);
            } catch (Exception e) {
                if (r.finishing) {
                    removeActivityFromHistoryLocked(r, reason + " exceptionInScheduleDestroy");
                    removedFromHistory = true;
                    skipDestroy = true;
                }
            }
            r.nowVisible = false;
            int isMovingtoDestroying = r.finishing ? skipDestroy ^ 1 : 0;
            Flog.i(101, "Moving to" + (isMovingtoDestroying != 0 ? " DESTROYING: " : " DESTROYED: ") + r + " in stack " + this.mStackId);
            if (isMovingtoDestroying != 0) {
                if (ActivityManagerDebugConfig.DEBUG_STATES) {
                    Slog.v(TAG_STATES, "Moving to DESTROYING: " + r + " (destroy requested)");
                }
                r.state = ActivityState.DESTROYING;
                this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(102, r), JobStatus.DEFAULT_TRIGGER_UPDATE_DELAY);
            } else {
                if (ActivityManagerDebugConfig.DEBUG_STATES || HwSlog.HW_DEBUG_STATES) {
                    Slog.v(TAG_STATES, "Moving to DESTROYED: " + r + " (destroy skipped)");
                }
                r.state = ActivityState.DESTROYED;
                if (ActivityManagerDebugConfig.DEBUG_APP) {
                    Slog.v(TAG_APP, "Clearing app during destroy for activity " + r);
                }
                r.app = null;
            }
        } else if (r.finishing) {
            removeActivityFromHistoryLocked(r, reason + " hadNoApp");
            removedFromHistory = true;
        } else {
            if (ActivityManagerDebugConfig.DEBUG_STATES || HwSlog.HW_DEBUG_STATES) {
                Slog.v(TAG_STATES, "Moving to DESTROYED: " + r + " (no app)");
            }
            r.state = ActivityState.DESTROYED;
            if (ActivityManagerDebugConfig.DEBUG_APP) {
                Slog.v(TAG_APP, "Clearing app during destroy for activity " + r);
            }
            r.app = null;
        }
        r.configChangeFlags = 0;
        if (!this.mLRUActivities.remove(r) && hadApp) {
            Slog.w(TAG, "Activity " + r + " being finished, but not in LRU list");
        }
        return removedFromHistory;
    }

    final void activityDestroyedLocked(IBinder token, String reason) {
        long origId = Binder.clearCallingIdentity();
        try {
            ActivityRecord r = ActivityRecord.forTokenLocked(token);
            if (r != null) {
                this.mHandler.removeMessages(102, r);
            }
            if (ActivityManagerDebugConfig.DEBUG_CONTAINERS) {
                Slog.d(TAG_CONTAINERS, "activityDestroyedLocked: r=" + r);
            }
            if (isInStackLocked(r) != null && r.state == ActivityState.DESTROYING) {
                cleanUpActivityLocked(r, true, false);
                removeActivityFromHistoryLocked(r, reason);
            }
            this.mStackSupervisor.mActivityLaunchTrack = "activityDestroyed";
            this.mStackSupervisor.resumeFocusedStackTopActivityLocked();
        } finally {
            Binder.restoreCallingIdentity(origId);
        }
    }

    void releaseBackgroundResources(ActivityRecord r) {
        if (hasVisibleBehindActivity() && (this.mHandler.hasMessages(107) ^ 1) != 0 && (r != topRunningActivityLocked() || shouldBeVisible(null) != 1)) {
            if (ActivityManagerDebugConfig.DEBUG_STATES) {
                Slog.d(TAG_STATES, "releaseBackgroundResources activtyDisplay=" + this.mActivityContainer.mActivityDisplay + " visibleBehind=" + r + " app=" + r.app + " thread=" + r.app.thread);
            }
            if (r == null || r.app == null || r.app.thread == null) {
                Slog.e(TAG, "releaseBackgroundResources: activity " + r + " no longer running");
                backgroundResourcesReleased();
            }
            try {
                r.app.thread.scheduleCancelVisibleBehind(r.appToken);
            } catch (RemoteException e) {
            }
            this.mHandler.sendEmptyMessageDelayed(107, 500);
        }
    }

    final void backgroundResourcesReleased() {
        this.mHandler.removeMessages(107);
        ActivityRecord r = getVisibleBehindActivity();
        if (r != null) {
            this.mStackSupervisor.mStoppingActivities.add(r);
            setVisibleBehindActivity(null);
            this.mStackSupervisor.scheduleIdleTimeoutLocked(null);
        }
        this.mStackSupervisor.resumeFocusedStackTopActivityLocked();
    }

    boolean hasVisibleBehindActivity() {
        return isAttached() ? this.mActivityContainer.mActivityDisplay.hasVisibleBehindActivity() : false;
    }

    void setVisibleBehindActivity(ActivityRecord r) {
        if (isAttached()) {
            this.mActivityContainer.mActivityDisplay.setVisibleBehindActivity(r);
        }
    }

    ActivityRecord getVisibleBehindActivity() {
        return isAttached() ? this.mActivityContainer.mActivityDisplay.mVisibleBehindActivity : null;
    }

    private void removeHistoryRecordsForAppLocked(ArrayList<ActivityRecord> list, ProcessRecord app, String listName) {
        int i = list.size();
        if (ActivityManagerDebugConfig.DEBUG_CLEANUP) {
            Slog.v(TAG_CLEANUP, "Removing app " + app + " from list " + listName + " with " + i + " entries");
        }
        while (i > 0) {
            i--;
            ActivityRecord r = (ActivityRecord) list.get(i);
            if (ActivityManagerDebugConfig.DEBUG_CLEANUP) {
                Slog.v(TAG_CLEANUP, "Record #" + i + " " + r);
            }
            if (r.app == app) {
                if (ActivityManagerDebugConfig.DEBUG_CLEANUP) {
                    Slog.v(TAG_CLEANUP, "---> REMOVING this entry!");
                }
                list.remove(i);
                removeTimeoutsForActivityLocked(r);
            }
        }
    }

    private boolean removeHistoryRecordsForAppLocked(ProcessRecord app) {
        removeHistoryRecordsForAppLocked(this.mLRUActivities, app, "mLRUActivities");
        removeHistoryRecordsForAppLocked(this.mStackSupervisor.mStoppingActivities, app, "mStoppingActivities");
        removeHistoryRecordsForAppLocked(this.mStackSupervisor.mGoingToSleepActivities, app, "mGoingToSleepActivities");
        removeHistoryRecordsForAppLocked(this.mStackSupervisor.mActivitiesWaitingForVisibleActivity, app, "mActivitiesWaitingForVisibleActivity");
        removeHistoryRecordsForAppLocked(this.mStackSupervisor.mFinishingActivities, app, "mFinishingActivities");
        boolean hasVisibleActivities = false;
        int i = numActivities();
        if (ActivityManagerDebugConfig.DEBUG_CLEANUP) {
            Slog.v(TAG_CLEANUP, "Removing app " + app + " from history with " + i + " entries");
        }
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            ArrayList<ActivityRecord> activities = ((TaskRecord) this.mTaskHistory.get(taskNdx)).mActivities;
            for (int activityNdx = activities.size() - 1; activityNdx >= 0; activityNdx--) {
                if (activityNdx < activities.size()) {
                    ActivityRecord r = (ActivityRecord) activities.get(activityNdx);
                    i--;
                    if (ActivityManagerDebugConfig.DEBUG_CLEANUP) {
                        Slog.v(TAG_CLEANUP, "Record #" + i + " " + r + ": app=" + r.app);
                    }
                    if (r.app == app) {
                        boolean remove;
                        if (r.visible) {
                            hasVisibleActivities = true;
                        }
                        if ((!r.haveState && (r.stateNotNeeded ^ 1) != 0) || r.finishing) {
                            remove = true;
                        } else if (!r.visible && r.launchCount > 2 && r.lastLaunchTime > SystemClock.uptimeMillis() - LingerMonitor.DEFAULT_NOTIFICATION_RATE_LIMIT_MILLIS) {
                            remove = true;
                        } else if (r.launchCount <= 5 || r.lastLaunchTime <= SystemClock.uptimeMillis() - TRANSLUCENT_CONVERSION_TIMEOUT) {
                            remove = false;
                        } else {
                            remove = true;
                            Slog.v(TAG_CLEANUP, "too many launcher times, remove : " + r);
                        }
                        if (remove) {
                            if (ActivityManagerDebugConfig.DEBUG_ADD_REMOVE || ActivityManagerDebugConfig.DEBUG_CLEANUP) {
                                Slog.i(TAG_ADD_REMOVE, "Removing activity " + r + " from stack at " + i + ": haveState=" + r.haveState + " stateNotNeeded=" + r.stateNotNeeded + " finishing=" + r.finishing + " state=" + r.state + " callers=" + Debug.getCallers(5));
                            }
                            if (!r.finishing) {
                                Slog.w(TAG, "Force removing " + r + ": app died, no saved state");
                                EventLog.writeEvent(EventLogTags.AM_FINISH_ACTIVITY, new Object[]{Integer.valueOf(r.userId), Integer.valueOf(System.identityHashCode(r)), Integer.valueOf(r.getTask().taskId), r.shortComponentName, "proc died without state saved"});
                                if (r.state == ActivityState.RESUMED) {
                                    this.mService.updateUsageStats(r, false);
                                }
                            }
                        } else {
                            if (ActivityManagerDebugConfig.DEBUG_ALL) {
                                Slog.v(TAG, "Keeping entry, setting app to null");
                            }
                            if (ActivityManagerDebugConfig.DEBUG_APP) {
                                Slog.v(TAG_APP, "Clearing app during removeHistory for activity " + r);
                            }
                            r.app = null;
                            r.nowVisible = r.visible;
                            if (!r.haveState) {
                                if (ActivityManagerDebugConfig.DEBUG_SAVED_STATE) {
                                    Slog.i(TAG_SAVED_STATE, "App died, clearing saved state of " + r);
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
        }
        return hasVisibleActivities;
    }

    private void updateTransitLocked(int transit, ActivityOptions options) {
        if (options != null) {
            ActivityRecord r = topRunningActivityLocked();
            if (r == null || r.state == ActivityState.RESUMED) {
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

    void moveHomeStackTaskToTop() {
        int top = this.mTaskHistory.size() - 1;
        for (int taskNdx = top; taskNdx >= 0; taskNdx--) {
            TaskRecord task = (TaskRecord) this.mTaskHistory.get(taskNdx);
            if (task.taskType == 1) {
                if (ActivityManagerDebugConfig.DEBUG_TASKS || ActivityManagerDebugConfig.DEBUG_STACK) {
                    Slog.d(TAG_STACK, "moveHomeStackTaskToTop: moving " + task);
                }
                this.mTaskHistory.remove(taskNdx);
                this.mTaskHistory.add(top, task);
                updateTaskMovement(task, true);
                return;
            }
        }
    }

    protected void moveTaskToFrontLocked(TaskRecord tr, boolean noAnimation, ActivityOptions options, AppTimeTracker timeTracker, String reason) {
        if (ActivityManagerDebugConfig.DEBUG_SWITCH) {
            Slog.v(TAG_SWITCH, "moveTaskToFront: " + tr);
        }
        ActivityStack topStack = getTopStackOnDisplay();
        ActivityRecord topActivity = topStack != null ? topStack.topActivity() : null;
        int numTasks = this.mTaskHistory.size();
        int index = this.mTaskHistory.indexOf(tr);
        if (numTasks == 0 || index < 0) {
            if (noAnimation) {
                ActivityOptions.abort(options);
            } else {
                updateTransitLocked(10, options);
            }
            return;
        }
        if (timeTracker != null) {
            for (int i = tr.mActivities.size() - 1; i >= 0; i--) {
                ((ActivityRecord) tr.mActivities.get(i)).appTimeTracker = timeTracker;
            }
        }
        insertTaskAtTop(tr, null);
        ActivityRecord top = tr.getTopActivity();
        if (top == null || (top.okToShowLocked() ^ 1) != 0) {
            addRecentActivityLocked(top);
            ActivityOptions.abort(options);
            return;
        }
        Flog.i(101, "moveTaskToFront: moving tr=" + tr + " reason=" + reason);
        ActivityRecord r = topRunningActivityLocked();
        this.mStackSupervisor.moveFocusableActivityStackToFrontLocked(r, reason);
        if (ActivityManagerDebugConfig.DEBUG_TRANSITION) {
            Slog.v(TAG_TRANSITION, "Prepare to front transition: task=" + tr);
        }
        if (noAnimation) {
            this.mWindowManager.prepareAppTransition(0, false);
            if (r != null) {
                this.mNoAnimActivities.add(r);
            }
            ActivityOptions.abort(options);
        } else {
            updateTransitLocked(10, options);
        }
        if (!(topActivity == null || topActivity.getStack() == null || topActivity.getStack().getStackId() == 4)) {
            topActivity.supportsPictureInPictureWhilePausing = true;
        }
        this.mStackSupervisor.mActivityLaunchTrack = "taskMove";
        this.mStackSupervisor.resumeFocusedStackTopActivityLocked();
        EventLog.writeEvent(EventLogTags.AM_TASK_TO_FRONT, new Object[]{Integer.valueOf(tr.userId), Integer.valueOf(tr.taskId)});
        this.mService.mTaskChangeNotificationController.notifyTaskMovedToFront(tr.taskId);
    }

    protected boolean moveTaskToBackLocked(int taskId) {
        TaskRecord tr = taskForIdLocked(taskId);
        if (tr == null) {
            Slog.i(TAG, "moveTaskToBack: bad taskId=" + taskId);
            return false;
        }
        Slog.i(TAG, "moveTaskToBack: " + tr);
        if (this.mStackSupervisor.isLockedTask(tr)) {
            this.mStackSupervisor.showLockTaskToast();
            return false;
        }
        if (this.mStackSupervisor.isFrontStackOnDisplay(this) && this.mService.mController != null) {
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
            Slog.v(TAG, "Prepare to back transition: task=" + taskId);
        }
        if (this.mStackId == 0 && topTask().isHomeTask()) {
            ActivityRecord visibleBehind = null;
            ActivityStack fullscreenStack = this.mStackSupervisor.getStack(1);
            ActivityStack assistantStack = this.mStackSupervisor.getStack(6);
            if (fullscreenStack != null && fullscreenStack.hasVisibleBehindActivity()) {
                visibleBehind = fullscreenStack.getVisibleBehindActivity();
            } else if (assistantStack != null && assistantStack.hasVisibleBehindActivity()) {
                visibleBehind = assistantStack.getVisibleBehindActivity();
            }
            if (visibleBehind != null) {
                this.mStackSupervisor.moveFocusableActivityStackToFrontLocked(visibleBehind, "moveTaskToBack");
                this.mStackSupervisor.resumeFocusedStackTopActivityLocked();
                return true;
            }
        }
        boolean prevIsHome = false;
        boolean canGoHome = !tr.isHomeTask() ? tr.isOverHomeStack() : false;
        if (canGoHome) {
            TaskRecord nextTask = getNextTask(tr);
            if (nextTask != null) {
                nextTask.setTaskToReturnTo(tr.getTaskToReturnTo());
            } else {
                prevIsHome = true;
            }
        }
        if (this.mTaskHistory.indexOf(tr) != 0) {
            this.mTaskHistory.remove(tr);
            this.mTaskHistory.add(0, tr);
            updateTaskMovement(tr, false);
            this.mWindowManager.prepareAppTransition(11, false);
            this.mWindowContainerController.positionChildAtBottom(tr.getWindowContainerController());
        }
        if (this.mStackId == 4) {
            this.mStackSupervisor.removeStackLocked(4);
            return true;
        }
        TaskRecord task;
        int numTasks = this.mTaskHistory.size();
        TaskRecord topTask = (TaskRecord) this.mTaskHistory.get(numTasks - 1);
        if (!(canGoHome || topTask == null || topTask.topRunningActivityLocked() != null || !topTask.isOverHomeStack() || topTask.mActivities == null || topTask.mActivities.size() == 0)) {
            canGoHome = true;
            Flog.i(101, "the top invalidate Task: " + topTask + " is overHome!");
        }
        int firstValidateTask = -1;
        boolean hasHomeTask = false;
        boolean noValidateTask = false;
        for (int taskNdx = 1; taskNdx < numTasks; taskNdx++) {
            if (taskNdx >= this.mTaskHistory.size()) {
                Flog.i(101, "moveTast Error becuase of out of bounds!");
                break;
            }
            task = (TaskRecord) this.mTaskHistory.get(taskNdx);
            if (task.topRunningActivityLocked() == null || !this.mStackSupervisor.isCurrentProfileLocked(task.userId)) {
                Flog.i(101, "inValidate task: " + task);
            } else {
                if (firstValidateTask < 0) {
                    firstValidateTask = taskNdx;
                }
                if (task.isOverHomeStack()) {
                    hasHomeTask = true;
                    break;
                }
            }
        }
        if (!hasHomeTask) {
            if (firstValidateTask <= 0 || firstValidateTask >= this.mTaskHistory.size()) {
                Flog.i(101, "there is no validate task, should go home!");
                noValidateTask = true;
            } else {
                Flog.i(101, "set first validate task!");
                ((TaskRecord) this.mTaskHistory.get(firstValidateTask)).setTaskToReturnTo(1);
            }
        }
        task = this.mResumedActivity != null ? this.mResumedActivity.getTask() : null;
        Flog.i(101, "moveTaskToBack, setTaskToReturnTo: taskToReturnTo=" + tr.getTaskToReturnTo() + "numTasks=" + numTasks + ",isOnHomeDisplay=" + isOnHomeDisplay() + ",isOverHomeStack=" + tr.isOverHomeStack() + ", prevIsHome=" + prevIsHome);
        if (!prevIsHome && ((task != tr || !canGoHome) && ((numTasks > 1 && !noValidateTask) || !isOnHomeDisplay()))) {
            this.mStackSupervisor.mActivityLaunchTrack = " taskToBack";
            this.mStackSupervisor.resumeFocusedStackTopActivityLocked();
            return true;
        } else if (this.mService.mBooting || (this.mService.mBooted ^ 1) == 0) {
            tr.setTaskToReturnTo(0);
            Flog.i(101, "moveTaskToBack, resuming home stack");
            return this.mStackSupervisor.resumeHomeStackTask(null, "moveTaskToBack");
        } else {
            Flog.i(101, "Can't move task to back while boot isn't completed");
            return false;
        }
    }

    private ActivityStack getTopStackOnDisplay() {
        ArrayList<ActivityStack> stacks = this.mActivityContainer.mActivityDisplay.mStacks;
        return stacks.isEmpty() ? null : (ActivityStack) stacks.get(stacks.size() - 1);
    }

    static void logStartActivity(int tag, ActivityRecord r, TaskRecord task) {
        Uri data = r.intent.getData();
        String strData = data != null ? data.toSafeString() : null;
        EventLog.writeEvent(tag, new Object[]{Integer.valueOf(r.userId), Integer.valueOf(System.identityHashCode(r)), Integer.valueOf(task.taskId), r.shortComponentName, r.intent.getAction(), r.intent.getType(), strData, Integer.valueOf(r.intent.getFlags())});
    }

    void ensureVisibleActivitiesConfigurationLocked(ActivityRecord start, boolean preserveWindow) {
        if (start != null && (start.visible ^ 1) == 0) {
            boolean behindFullscreen = false;
            boolean updatedConfig = false;
            for (int taskIndex = this.mTaskHistory.indexOf(start.getTask()); taskIndex >= 0; taskIndex--) {
                TaskRecord task = (TaskRecord) this.mTaskHistory.get(taskIndex);
                ArrayList<ActivityRecord> activities = task.mActivities;
                int activityIndex = start.getTask() == task ? activities.indexOf(start) : activities.size() - 1;
                while (activityIndex >= 0) {
                    ActivityRecord r = (ActivityRecord) activities.get(activityIndex);
                    updatedConfig |= r.ensureActivityConfigurationLocked(0, preserveWindow);
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

    void resize(Rect bounds, Rect tempTaskBounds, Rect tempTaskInsetBounds) {
        bounds = TaskRecord.validateBounds(bounds);
        if (updateBoundsAllowed(bounds, tempTaskBounds, tempTaskInsetBounds)) {
            Rect taskBounds = tempTaskBounds != null ? tempTaskBounds : bounds;
            Rect insetBounds = tempTaskInsetBounds != null ? tempTaskInsetBounds : taskBounds;
            this.mTmpBounds.clear();
            this.mTmpConfigs.clear();
            this.mTmpInsetBounds.clear();
            for (int i = this.mTaskHistory.size() - 1; i >= 0; i--) {
                TaskRecord task = (TaskRecord) this.mTaskHistory.get(i);
                if (task.isResizeable()) {
                    if (this.mStackId == 2 || HwPCUtils.isPcDynamicStack(this.mStackId)) {
                        this.mTmpRect2.set(task.mBounds);
                        fitWithinBounds(this.mTmpRect2, bounds);
                        task.updateOverrideConfiguration(this.mTmpRect2);
                    } else {
                        task.updateOverrideConfiguration(taskBounds, insetBounds);
                    }
                }
                this.mTmpConfigs.put(task.taskId, task.getOverrideConfiguration());
                this.mTmpBounds.put(task.taskId, task.mBounds);
                if (tempTaskInsetBounds != null) {
                    this.mTmpInsetBounds.put(task.taskId, tempTaskInsetBounds);
                }
            }
            this.mFullscreen = this.mWindowContainerController.resize(bounds, this.mTmpConfigs, this.mTmpBounds, this.mTmpInsetBounds);
            setBounds(bounds);
        }
    }

    private static void fitWithinBounds(Rect bounds, Rect stackBounds) {
        if (stackBounds != null && !stackBounds.contains(bounds)) {
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

    boolean willActivityBeVisibleLocked(IBinder token) {
        ActivityRecord r;
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            ArrayList<ActivityRecord> activities = ((TaskRecord) this.mTaskHistory.get(taskNdx)).mActivities;
            for (int activityNdx = activities.size() - 1; activityNdx >= 0; activityNdx--) {
                r = (ActivityRecord) activities.get(activityNdx);
                if (r.appToken == token) {
                    return true;
                }
                if (r.fullscreen && (r.finishing ^ 1) != 0) {
                    return false;
                }
            }
        }
        r = ActivityRecord.forTokenLocked(token);
        if (r == null) {
            return false;
        }
        if (r.finishing) {
            Slog.e(TAG, "willActivityBeVisibleLocked: Returning false, would have returned true for r=" + r);
        }
        return r.finishing ^ 1;
    }

    void closeSystemDialogsLocked() {
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            ArrayList<ActivityRecord> activities = ((TaskRecord) this.mTaskHistory.get(taskNdx)).mActivities;
            for (int activityNdx = activities.size() - 1; activityNdx >= 0; activityNdx--) {
                ActivityRecord r = (ActivityRecord) activities.get(activityNdx);
                if ((r.info.flags & 256) != 0) {
                    finishActivityLocked(r, 0, null, "close-sys", true);
                }
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:37:0x00a5  */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x006e  */
    /* JADX WARNING: Missing block: B:10:0x0040, code:
            if (r18.contains(r2.realActivity.getClassName()) == false) goto L_0x0042;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    boolean finishDisabledPackageActivitiesLocked(String packageName, Set<String> filterByClasses, boolean doit, boolean evenPersistent, int userId) {
        boolean didSomething = false;
        TaskRecord lastTask = null;
        ComponentName homeActivity = null;
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            ArrayList<ActivityRecord> activities = ((TaskRecord) this.mTaskHistory.get(taskNdx)).mActivities;
            int numActivities = activities.size();
            int activityNdx = 0;
            while (activityNdx < numActivities) {
                try {
                    boolean sameComponent;
                    ActivityRecord r = (ActivityRecord) activities.get(activityNdx);
                    if (r.packageName.equals(packageName)) {
                        if (filterByClasses != null) {
                        }
                        sameComponent = true;
                        if ((userId == -1 || r.userId == userId) && ((sameComponent || r.getTask() == lastTask) && (r.app == null || evenPersistent || (r.app.persistent ^ 1) != 0))) {
                            if (!doit) {
                                if (r.isHomeActivity()) {
                                    if (homeActivity == null || !homeActivity.equals(r.realActivity)) {
                                        homeActivity = r.realActivity;
                                    } else {
                                        Slog.i(TAG, "Skip force-stop again " + r);
                                    }
                                }
                                didSomething = true;
                                Slog.i(TAG, "  Force finishing activity " + r);
                                if (sameComponent) {
                                    if (r.app != null) {
                                        r.app.removed = true;
                                    }
                                    r.app = null;
                                }
                                lastTask = r.getTask();
                                if (finishActivityLocked(r, 0, null, "force-stop", true)) {
                                    numActivities--;
                                    activityNdx--;
                                }
                            } else if (!r.finishing) {
                                return true;
                            }
                        }
                        activityNdx++;
                    }
                    sameComponent = packageName == null && r.userId == userId;
                    if (!doit) {
                    }
                } catch (IndexOutOfBoundsException e) {
                    Slog.e(TAG, "IndexOutOfBoundsException: Index: +" + activityNdx + ", Size: " + activities.size());
                }
                activityNdx++;
            }
        }
        return didSomething;
    }

    void getTasksLocked(List<RunningTaskInfo> list, int callingUid, boolean allowed) {
        boolean focusedStack = this.mStackSupervisor.getFocusedStack() == this;
        boolean topTask = true;
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            TaskRecord task = (TaskRecord) this.mTaskHistory.get(taskNdx);
            if (task.getTopActivity() != null) {
                ActivityRecord r = null;
                ActivityRecord top = null;
                int numActivities = 0;
                int numRunning = 0;
                ArrayList<ActivityRecord> activities = task.mActivities;
                if (allowed || (task.isHomeTask() ^ 1) == 0 || task.effectiveUid == callingUid) {
                    for (int activityNdx = activities.size() - 1; activityNdx >= 0; activityNdx--) {
                        ActivityRecord tmp = (ActivityRecord) activities.get(activityNdx);
                        if (!tmp.finishing) {
                            r = tmp;
                            if (top == null || top.state == ActivityState.INITIALIZING) {
                                top = tmp;
                                numRunning = 0;
                                numActivities = 0;
                            }
                            numActivities++;
                            if (!(tmp.app == null || tmp.app.thread == null)) {
                                numRunning++;
                            }
                            if (ActivityManagerDebugConfig.DEBUG_ALL) {
                                Slog.v(TAG, tmp.intent.getComponent().flattenToShortString() + ": task=" + tmp.getTask());
                            }
                        }
                    }
                    RunningTaskInfo ci = new RunningTaskInfo();
                    ci.id = task.taskId;
                    ci.stackId = this.mStackId;
                    ci.baseActivity = r.intent.getComponent();
                    ci.topActivity = top.intent.getComponent();
                    ci.lastActiveTime = task.lastActiveTime;
                    if (focusedStack && topTask) {
                        ci.lastActiveTime = System.currentTimeMillis();
                        topTask = false;
                    }
                    if (top.getTask() != null) {
                        ci.description = top.getTask().lastDescription;
                    }
                    ci.numActivities = numActivities;
                    ci.numRunning = numRunning;
                    ci.supportsSplitScreenMultiWindow = task.supportsSplitScreen();
                    ci.resizeMode = task.mResizeMode;
                    list.add(ci);
                }
            }
        }
    }

    void unhandledBackLocked() {
        int top = this.mTaskHistory.size() - 1;
        if (ActivityManagerDebugConfig.DEBUG_SWITCH) {
            Slog.d(TAG_SWITCH, "Performing unhandledBack(): top activity at " + top);
        }
        if (top >= 0) {
            ArrayList<ActivityRecord> activities = ((TaskRecord) this.mTaskHistory.get(top)).mActivities;
            int activityTop = activities.size() - 1;
            if (activityTop >= 0) {
                finishActivityLocked((ActivityRecord) activities.get(activityTop), 0, null, "unhandled-back", true);
            }
        }
    }

    boolean handleAppDiedLocked(ProcessRecord app) {
        if (this.mPausingActivity != null && this.mPausingActivity.app == app) {
            if (ActivityManagerDebugConfig.DEBUG_PAUSE || ActivityManagerDebugConfig.DEBUG_CLEANUP) {
                Slog.v(TAG_PAUSE, "App died while pausing: " + this.mPausingActivity);
            }
            this.mPausingActivity = null;
        }
        if (this.mLastPausedActivity != null && this.mLastPausedActivity.app == app) {
            this.mLastPausedActivity = null;
            this.mLastNoHistoryActivity = null;
        }
        return removeHistoryRecordsForAppLocked(app);
    }

    void handleAppCrashLocked(ProcessRecord app) {
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            ArrayList<ActivityRecord> activities = ((TaskRecord) this.mTaskHistory.get(taskNdx)).mActivities;
            for (int activityNdx = activities.size() - 1; activityNdx >= 0; activityNdx--) {
                ActivityRecord activityRecord = null;
                if (activityNdx < activities.size()) {
                    activityRecord = (ActivityRecord) activities.get(activityNdx);
                }
                if (activityRecord != null && activityRecord.app == app) {
                    Slog.w(TAG, "  Force finishing activity " + activityRecord.intent.getComponent().flattenToShortString());
                    activityRecord.app = null;
                    finishCurrentActivityLocked(activityRecord, 0, false);
                }
            }
        }
    }

    boolean dumpActivitiesLocked(FileDescriptor fd, PrintWriter pw, boolean dumpAll, boolean dumpClient, String dumpPackage, boolean needSep, String header) {
        boolean printed = false;
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            TaskRecord task = (TaskRecord) this.mTaskHistory.get(taskNdx);
            boolean z = dumpAll ^ 1;
            String str = "    Task id #" + task.taskId + "\n" + "    mFullscreen=" + task.mFullscreen + "\n" + "    mBounds=" + task.mBounds + "\n" + "    mMinWidth=" + task.mMinWidth + "\n" + "    mMinHeight=" + task.mMinHeight + "\n" + "    mLastNonFullscreenBounds=" + task.mLastNonFullscreenBounds;
            printed |= ActivityStackSupervisor.dumpHistoryList(fd, pw, ((TaskRecord) this.mTaskHistory.get(taskNdx)).mActivities, "    ", "Hist", true, z, dumpClient, dumpPackage, needSep, header, str);
            if (printed) {
                header = null;
            }
        }
        return printed;
    }

    ArrayList<ActivityRecord> getDumpActivitiesLocked(String name) {
        ArrayList<ActivityRecord> activities = new ArrayList();
        int taskNdx;
        if ("all".equals(name)) {
            for (taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
                activities.addAll(((TaskRecord) this.mTaskHistory.get(taskNdx)).mActivities);
            }
        } else if ("top".equals(name)) {
            int top = this.mTaskHistory.size() - 1;
            if (top >= 0) {
                ArrayList<ActivityRecord> list = ((TaskRecord) this.mTaskHistory.get(top)).mActivities;
                int listTop = list.size() - 1;
                if (listTop >= 0) {
                    activities.add((ActivityRecord) list.get(listTop));
                }
            }
        } else {
            ItemMatcher matcher = new ItemMatcher();
            matcher.build(name);
            for (taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
                for (ActivityRecord r1 : ((TaskRecord) this.mTaskHistory.get(taskNdx)).mActivities) {
                    if (matcher.match(r1, r1.intent.getComponent())) {
                        activities.add(r1);
                    }
                }
            }
        }
        return activities;
    }

    ActivityRecord restartPackage(String packageName) {
        ActivityRecord starting = topRunningActivityLocked();
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            ArrayList<ActivityRecord> activities = ((TaskRecord) this.mTaskHistory.get(taskNdx)).mActivities;
            for (int activityNdx = activities.size() - 1; activityNdx >= 0; activityNdx--) {
                ActivityRecord a = (ActivityRecord) activities.get(activityNdx);
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

    void removeTask(TaskRecord task, String reason, int mode) {
        for (ActivityRecord record : task.mActivities) {
            onActivityRemovedFromStack(record);
        }
        int taskNdx = this.mTaskHistory.indexOf(task);
        int topTaskNdx = this.mTaskHistory.size() - 1;
        if (task.isOverHomeStack() && taskNdx < topTaskNdx) {
            TaskRecord nextTask = (TaskRecord) this.mTaskHistory.get(taskNdx + 1);
            if (!(nextTask.isOverHomeStack() || (nextTask.isOverAssistantStack() ^ 1) == 0)) {
                Flog.i(101, "removeTask, setTaskToReturnTo: HOME");
                nextTask.setTaskToReturnTo(1);
            }
        }
        Flog.i(101, "Task removed: " + task + ", reason: " + reason);
        this.mTaskHistory.remove(task);
        removeActivitiesFromLRUListLocked(task);
        updateTaskMovement(task, true);
        if (mode == 0 && task.mActivities.isEmpty()) {
            boolean isVoiceSession = task.voiceSession != null;
            if (isVoiceSession) {
                try {
                    task.voiceSession.taskFinished(task.intent, task.taskId);
                } catch (RemoteException e) {
                }
            }
            if (task.autoRemoveFromRecents() || isVoiceSession) {
                this.mRecentTasks.remove(task);
                task.removedFromRecents();
            }
            task.removeWindowContainer();
        }
        if (this.mTaskHistory.isEmpty()) {
            if (ActivityManagerDebugConfig.DEBUG_STACK) {
                Slog.i(TAG_STACK, "removeTask: removing stack=" + this);
            }
            Flog.i(101, "removeTask: removing stack=" + this);
            if (isOnHomeDisplay() && mode != 2 && this.mStackSupervisor.isFocusedStack(this)) {
                String myReason = reason + " leftTaskHistoryEmpty";
                if (this.mFullscreen || (adjustFocusToNextFocusableStackLocked(myReason) ^ 1) != 0) {
                    this.mStackSupervisor.moveHomeStackToFront(myReason);
                }
            }
            if (this.mStacks != null) {
                this.mStacks.remove(this);
                this.mStacks.add(0, this);
            }
            if (!isHomeOrRecentsStack()) {
                this.mActivityContainer.onTaskListEmptyLocked();
            }
        }
        task.setStack(null);
        if (this.mStackId == 4) {
            this.mService.mTaskChangeNotificationController.notifyActivityUnpinned();
        }
    }

    TaskRecord createTaskRecord(int taskId, ActivityInfo info, Intent intent, IVoiceInteractionSession voiceSession, IVoiceInteractor voiceInteractor, boolean toTop, int type) {
        TaskRecord task = HwServiceFactory.createTaskRecord(this.mService, taskId, info, intent, voiceSession, voiceInteractor, type);
        addTask(task, toTop, "createTaskRecord");
        boolean isLockscreenShown = this.mService.mStackSupervisor.mKeyguardController.isKeyguardShowing();
        if (!(layoutTaskInStack(task, info.windowLayout) || this.mBounds == null || !task.isResizeable() || (isLockscreenShown ^ 1) == 0)) {
            task.updateOverrideConfiguration(this.mBounds);
        }
        task.createWindowContainer(toTop, (info.flags & 1024) != 0);
        return task;
    }

    boolean layoutTaskInStack(TaskRecord task, WindowLayout windowLayout) {
        if (this.mTaskPositioner == null) {
            return false;
        }
        this.mTaskPositioner.updateDefaultBounds(task, this.mTaskHistory, windowLayout);
        return true;
    }

    ArrayList<TaskRecord> getAllTasks() {
        return new ArrayList(this.mTaskHistory);
    }

    void addTask(TaskRecord task, boolean toTop, String reason) {
        addTask(task, toTop ? HwBootFail.STAGE_BOOT_SUCCESS : 0, true, reason);
        if (toTop) {
            this.mWindowContainerController.positionChildAtTop(task.getWindowContainerController(), true);
        }
    }

    void addTask(TaskRecord task, int position, boolean schedulePictureInPictureModeChange, String reason) {
        this.mTaskHistory.remove(task);
        position = getAdjustedPositionForTask(task, position, null);
        boolean toTop = position >= this.mTaskHistory.size();
        ActivityStack prevStack = preAddTask(task, reason, toTop);
        this.mTaskHistory.add(position, task);
        task.setStack(this);
        if (toTop) {
            updateTaskReturnToForTopInsertion(task);
        }
        updateTaskMovement(task, toTop);
        postAddTask(task, prevStack, schedulePictureInPictureModeChange);
    }

    void positionChildAt(TaskRecord task, int index) {
        if (task.getStack() != this) {
            throw new IllegalArgumentException("AS.positionChildAt: task=" + task + " is not a child of stack=" + this + " current parent=" + task.getStack());
        }
        task.updateOverrideConfigurationForStack(this);
        ActivityRecord topRunningActivity = task.topRunningActivityLocked();
        boolean wasResumed = topRunningActivity == task.getStack().mResumedActivity;
        insertTaskAtPosition(task, index);
        task.setStack(this);
        postAddTask(task, null, true);
        if (wasResumed) {
            if (this.mResumedActivity != null) {
                Log.wtf(TAG, "mResumedActivity was already set when moving mResumedActivity from other stack to this stack mResumedActivity=" + this.mResumedActivity + " other mResumedActivity=" + topRunningActivity);
            }
            this.mResumedActivity = topRunningActivity;
        }
        ensureActivitiesVisibleLocked(null, 0, false);
        this.mStackSupervisor.resumeFocusedStackTopActivityLocked();
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

    void moveToFrontAndResumeStateIfNeeded(ActivityRecord r, boolean moveToFront, boolean setResume, boolean setPause, String reason) {
        if (moveToFront) {
            if (setResume) {
                this.mResumedActivity = r;
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
        return "ActivityStack{" + Integer.toHexString(System.identityHashCode(this)) + " stackId=" + this.mStackId + ", " + this.mTaskHistory.size() + " tasks}";
    }

    void onLockTaskPackagesUpdatedLocked() {
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            ((TaskRecord) this.mTaskHistory.get(taskNdx)).setLockTaskAuth();
        }
    }

    void executeAppTransition(ActivityOptions options) {
        this.mWindowManager.executeAppTransition();
        this.mNoAnimActivities.clear();
        ActivityOptions.abort(options);
    }
}
