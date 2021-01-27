package com.android.server.wm;

import android.app.ActivityOptions;
import android.app.AppGlobals;
import android.app.IActivityController;
import android.app.IApplicationThread;
import android.app.RemoteAction;
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
import android.content.ComponentName;
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
import android.util.HwLog;
import android.util.HwMwUtils;
import android.util.HwPCUtils;
import android.util.HwSlog;
import android.util.IntArray;
import android.util.Jlog;
import android.util.Log;
import android.util.Slog;
import android.util.proto.ProtoOutputStream;
import android.view.IApplicationToken;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.app.IVoiceInteractor;
import com.android.internal.util.function.pooled.PooledLambda;
import com.android.server.HwServiceExFactory;
import com.android.server.LocalServices;
import com.android.server.Watchdog;
import com.android.server.am.ActivityManagerService;
import com.android.server.am.AppTimeTracker;
import com.android.server.am.EventLogTags;
import com.android.server.am.PendingIntentRecord;
import com.android.server.wm.RootActivityContainer;
import com.android.server.wm.WindowManagerService;
import com.huawei.pgmng.log.LogPower;
import com.huawei.server.wm.IHwActivityStackEx;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class ActivityStack extends AbsActivityStack {
    static final int DESTROY_ACTIVITIES_MSG = 105;
    private static final int DESTROY_TIMEOUT = 10000;
    static final int DESTROY_TIMEOUT_MSG = 102;
    static final int FINISH_AFTER_PAUSE = 1;
    static final int FINISH_AFTER_VISIBLE = 2;
    static final int FINISH_IMMEDIATELY = 0;
    private static final String INCALL_ACTIVITY_NAME_A = "com.android.incallui.InCallActivity";
    private static final String INCALL_ACTIVITY_NAME_Z = "com.huawei.ohos.call.base.InCallAbilityShellActivity";
    static final int LAUNCH_TICK = 500;
    static final int LAUNCH_TICK_MSG = 103;
    private static final int MAX_STOPPING_TO_FORCE = 3;
    private static final String MEETIME_INCALLUI_ACTIVITY_A = "com.huawei.hicallmanager.InCallActivity";
    private static final String MEETIME_INCALLUI_ACTIVITY_Z = "com.huawei.ohos.meetime.hicallui.base.HiCallAbilityShellActivity";
    private static final HashSet<String> PACKAGE_NAMES_TO_NOTIFY_RESUMED = new HashSet<String>() {
        /* class com.android.server.wm.ActivityStack.AnonymousClass1 */

        {
            add(ActivityStack.INCALL_ACTIVITY_NAME_A);
            add(ActivityStack.INCALL_ACTIVITY_NAME_Z);
            add(ActivityStack.MEETIME_INCALLUI_ACTIVITY_A);
            add(ActivityStack.MEETIME_INCALLUI_ACTIVITY_Z);
        }
    };
    private static final int PAUSE_TIMEOUT = 500;
    static final int PAUSE_TIMEOUT_MSG = 101;
    @VisibleForTesting
    protected static final int REMOVE_TASK_MODE_DESTROYING = 0;
    static final int REMOVE_TASK_MODE_MOVING = 1;
    static final int REMOVE_TASK_MODE_MOVING_TO_TOP = 2;
    private static final String SETTINGS_DASHBROAED_ACTIVITY_NAME = "com.android.settings.Settings$AppAndNotificationDashboardActivity";
    private static final boolean SHOW_APP_STARTING_PREVIEW = true;
    static final int STACK_VISIBILITY_INVISIBLE = 2;
    static final int STACK_VISIBILITY_VISIBLE = 0;
    static final int STACK_VISIBILITY_VISIBLE_BEHIND_TRANSLUCENT = 1;
    private static final int STOP_TIMEOUT = 11000;
    static final int STOP_TIMEOUT_MSG = 104;
    protected static final String TAG = "ActivityTaskManager";
    private static final String TAG_ADD_REMOVE = "ActivityTaskManager";
    private static final String TAG_APP = "ActivityTaskManager";
    private static final String TAG_CLEANUP = "ActivityTaskManager";
    private static final String TAG_CONTAINERS = "ActivityTaskManager";
    private static final String TAG_PAUSE = "ActivityTaskManager";
    private static final String TAG_RELEASE = "ActivityTaskManager";
    private static final String TAG_RESULTS = "ActivityTaskManager";
    private static final String TAG_SAVED_STATE = "ActivityTaskManager";
    private static final String TAG_STACK = "ActivityTaskManager";
    private static final String TAG_STATES = "ActivityTaskManager";
    private static final String TAG_SWITCH = "ActivityTaskManager";
    private static final String TAG_TASKS = "ActivityTaskManager";
    private static final String TAG_TRANSITION = "ActivityTaskManager";
    private static final String TAG_USER_LEAVING = "ActivityTaskManager";
    private static final String TAG_VISIBILITY = ("ActivityTaskManager" + ActivityTaskManagerDebugConfig.POSTFIX_VISIBILITY);
    private static final long TRANSLUCENT_CONVERSION_TIMEOUT = 2000;
    static final int TRANSLUCENT_TIMEOUT_MSG = 106;
    private boolean isPendingShow = false;
    boolean mConfigWillChange;
    protected String mCurrentPkgUnderFreeForm = "";
    protected long mCurrentTime;
    int mCurrentUser;
    private final Rect mDeferredBounds = new Rect();
    private final Rect mDeferredDisplayedBounds = new Rect();
    private int mDisplayHeight;
    public int mDisplayId;
    private int mDisplayWidth;
    protected final ArrayList<ActivityRecord> mFIFOActivities = new ArrayList<>();
    boolean mForceHidden = false;
    final Handler mHandler;
    protected IHwActivityStackEx mHwActivityStackEx;
    boolean mInResumeTopActivity = false;
    boolean mIsForceFocusable = true;
    protected boolean mIsFreeFormStackVisible = false;
    boolean mIsInDefaultPos = true;
    boolean mIsTempStack = false;
    protected final ArrayList<ActivityRecord> mLRUActivities = new ArrayList<>();
    ActivityRecord mLastNoHistoryActivity = null;
    ActivityRecord mLastPausedActivity = null;
    ActivityRecord mPausingActivity = null;
    private int mRestoreOverrideWindowingMode = 0;
    ActivityRecord mResumedActivity = null;
    protected final RootActivityContainer mRootActivityContainer;
    final ActivityTaskManagerService mService;
    String mShortComponentName = "";
    final int mStackId;
    protected final ActivityStackSupervisor mStackSupervisor;
    protected ArrayList<TaskRecord> mTaskHistory = new ArrayList<>();
    TaskStack mTaskStack;
    private final ArrayList<ActivityRecord> mTmpActivities = new ArrayList<>();
    private final ActivityOptions mTmpOptions = ActivityOptions.makeBasic();
    private final Rect mTmpRect = new Rect();
    private final Rect mTmpRect2 = new Rect();
    private boolean mTopActivityOccludesKeyguard;
    private ActivityRecord mTopDismissingKeyguardActivity;
    ActivityRecord mTranslucentActivityWaiting = null;
    ArrayList<ActivityRecord> mUndrawnActivitiesBelowTopTranslucent = new ArrayList<>();
    private boolean mUpdateBoundsDeferred;
    private boolean mUpdateBoundsDeferredCalled;
    private boolean mUpdateDisplayedBoundsDeferredCalled;
    final WindowManagerService mWindowManager;

    public enum ActivityState {
        INITIALIZING,
        RESUMED,
        PAUSING,
        PAUSED,
        STOPPING,
        STOPPED,
        FINISHING,
        DESTROYING,
        DESTROYED,
        RESTARTING_PROCESS
    }

    @interface StackVisibility {
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.wm.ConfigurationContainer
    public int getChildCount() {
        return this.mTaskHistory.size();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.wm.ConfigurationContainer
    public TaskRecord getChildAt(int index) {
        return this.mTaskHistory.get(index);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.wm.ConfigurationContainer
    public ActivityDisplay getParent() {
        return getDisplay();
    }

    /* access modifiers changed from: package-private */
    public void setParent(ActivityDisplay parent) {
        if (getParent() != parent) {
            this.mDisplayId = parent.mDisplayId;
            onParentChanged();
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.wm.ConfigurationContainer
    public void onParentChanged() {
        ActivityDisplay display = getParent();
        if (display != null) {
            getConfiguration().windowConfiguration.setRotation(display.getWindowConfiguration().getRotation());
        }
        super.onParentChanged();
        if (display != null && inSplitScreenPrimaryWindowingMode()) {
            getStackDockedModeBounds(null, null, this.mTmpRect, this.mTmpRect2);
            this.mStackSupervisor.resizeDockedStackLocked(getRequestedOverrideBounds(), this.mTmpRect, this.mTmpRect2, null, null, true);
        }
        this.mRootActivityContainer.updateUIDsPresentOnDisplay();
    }

    private static class ScheduleDestroyArgs {
        final WindowProcessController mOwner;
        final String mReason;

        ScheduleDestroyArgs(WindowProcessController owner, String reason) {
            this.mOwner = owner;
            this.mReason = reason;
        }
    }

    private class ActivityStackHandler extends Handler {
        ActivityStackHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            IApplicationToken.Stub stub = null;
            switch (msg.what) {
                case ActivityStack.PAUSE_TIMEOUT_MSG /* 101 */:
                    ActivityRecord r = (ActivityRecord) msg.obj;
                    Slog.w("ActivityTaskManager", "Activity pause timeout for " + r);
                    synchronized (ActivityStack.this.mService.mGlobalLock) {
                        try {
                            WindowManagerService.boostPriorityForLockedSection();
                            if (r.hasProcess()) {
                                ActivityTaskManagerService activityTaskManagerService = ActivityStack.this.mService;
                                WindowProcessController windowProcessController = r.app;
                                long j = r.pauseTime;
                                activityTaskManagerService.logAppTooSlow(windowProcessController, j, "pausing " + r);
                            }
                            ActivityStack.this.activityPausedLocked(r.appToken, true);
                        } finally {
                            WindowManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                    return;
                case 102:
                    ActivityRecord r2 = (ActivityRecord) msg.obj;
                    Slog.w("ActivityTaskManager", "Activity destroy timeout for " + r2);
                    synchronized (ActivityStack.this.mService.mGlobalLock) {
                        try {
                            WindowManagerService.boostPriorityForLockedSection();
                            ActivityStack activityStack = ActivityStack.this;
                            if (r2 != null) {
                                stub = r2.appToken;
                            }
                            activityStack.activityDestroyedLocked((IBinder) stub, "destroyTimeout");
                        } finally {
                            WindowManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                    return;
                case 103:
                    ActivityRecord r3 = (ActivityRecord) msg.obj;
                    synchronized (ActivityStack.this.mService.mGlobalLock) {
                        try {
                            WindowManagerService.boostPriorityForLockedSection();
                            if (r3.continueLaunchTickingLocked()) {
                                ActivityTaskManagerService activityTaskManagerService2 = ActivityStack.this.mService;
                                WindowProcessController windowProcessController2 = r3.app;
                                long j2 = r3.launchTickTime;
                                activityTaskManagerService2.logAppTooSlow(windowProcessController2, j2, "launching " + r3);
                            }
                        } finally {
                            WindowManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                    return;
                case 104:
                    ActivityRecord r4 = (ActivityRecord) msg.obj;
                    Slog.w("ActivityTaskManager", "Activity stop timeout for " + r4);
                    synchronized (ActivityStack.this.mService.mGlobalLock) {
                        try {
                            WindowManagerService.boostPriorityForLockedSection();
                            if (r4.isInHistory()) {
                                r4.activityStoppedLocked(null, null, null);
                            }
                        } finally {
                            WindowManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                    return;
                case 105:
                    ScheduleDestroyArgs args = (ScheduleDestroyArgs) msg.obj;
                    synchronized (ActivityStack.this.mService.mGlobalLock) {
                        try {
                            WindowManagerService.boostPriorityForLockedSection();
                            ActivityStack.this.destroyActivitiesLocked(args.mOwner, args.mReason);
                        } finally {
                            WindowManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                    return;
                case ActivityStack.TRANSLUCENT_TIMEOUT_MSG /* 106 */:
                    synchronized (ActivityStack.this.mService.mGlobalLock) {
                        try {
                            WindowManagerService.boostPriorityForLockedSection();
                            ActivityStack.this.notifyActivityDrawnLocked(null);
                        } finally {
                            WindowManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                    return;
                default:
                    return;
            }
        }
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
        this.mStackSupervisor = supervisor;
        this.mService = supervisor.mService;
        this.mRootActivityContainer = this.mService.mRootActivityContainer;
        this.mHandler = new ActivityStackHandler(supervisor.mLooper);
        this.mWindowManager = this.mService.mWindowManager;
        this.mStackId = stackId;
        this.mCurrentUser = this.mService.mAmInternal.getCurrentUserId();
        this.mDisplayId = display.mDisplayId;
        setActivityType(activityType);
        createTaskStack(display.mDisplayId, onTop, this.mTmpRect2);
        setWindowingMode(windowingMode, false, false, false, false, true);
        this.mService.mHwATMSEx.setAlwaysOnTopOnly(display, this, true, false);
        display.addChild(this, onTop ? Integer.MAX_VALUE : Integer.MIN_VALUE);
        this.mHwActivityStackEx = HwServiceExFactory.getHwActivityStackEx(this, this.mService);
        this.mDisplayWidth = display.getWindowConfiguration().getBounds().width();
        this.mDisplayHeight = display.getWindowConfiguration().getBounds().height();
    }

    /* access modifiers changed from: package-private */
    public void createTaskStack(int displayId, boolean onTop, Rect outBounds) {
        DisplayContent dc = this.mWindowManager.mRoot.getDisplayContent(displayId);
        if (dc != null) {
            this.mTaskStack = new TaskStack(this.mWindowManager, this.mStackId, this);
            dc.setStackOnDisplay(this.mStackId, onTop, this.mTaskStack);
            if (this.mTaskStack.matchParentBounds()) {
                outBounds.setEmpty();
            } else {
                this.mTaskStack.getRawBounds(outBounds);
            }
        } else {
            throw new IllegalArgumentException("Trying to add stackId=" + this.mStackId + " to unknown displayId=" + displayId);
        }
    }

    /* access modifiers changed from: package-private */
    public TaskStack getTaskStack() {
        return this.mTaskStack;
    }

    /* access modifiers changed from: package-private */
    public void onActivityStateChanged(ActivityRecord record, ActivityState state, String reason) {
        if (record == this.mResumedActivity && state != ActivityState.RESUMED) {
            setResumedActivity(null, reason + " - onActivityStateChanged");
        }
        if (state == ActivityState.RESUMED) {
            if (ActivityTaskManagerDebugConfig.DEBUG_STACK) {
                Slog.v("ActivityTaskManager", "set resumed activity to:" + record + " reason:" + reason);
            }
            setResumedActivity(record, reason + " - onActivityStateChanged");
            ActivityRecord topResumed = this.mRootActivityContainer.getTopResumedActivity();
            if (record == topResumed) {
                this.mService.setResumedActivityUncheckLocked(record, reason);
            } else {
                switchFocusedStackIfNeed(record, topResumed);
            }
            this.mStackSupervisor.mRecentTasks.add(record.getTaskRecord());
        }
    }

    private void switchFocusedStackIfNeed(ActivityRecord current, ActivityRecord topResumed) {
        if (!(current == null || topResumed == null || current.getDisplay() != topResumed.getDisplay() || current.inMultiWindowMode())) {
            if (!(topResumed.inHwFreeFormWindowingMode() || topResumed.inFreeformWindowingMode())) {
                return;
            }
            if (current.appInfo == null || !current.appInfo.isSystemApp() || topResumed.inHwTvFreeFormWindowingMode()) {
                ActivityDisplay display = getDisplay();
                for (int stackNdx = display.getChildCount() - 1; stackNdx >= 0; stackNdx--) {
                    ActivityStack stack = display.getChildAt(stackNdx);
                    if (!stack.inHwFreeFormWindowingMode() && !display.isFreeformStackOnTop(stack) && stack.topRunningActivityLocked(true) != null) {
                        if (stack == this) {
                            this.mService.mH.postAtFrontOfQueue(new Runnable(display) {
                                /* class com.android.server.wm.$$Lambda$ActivityStack$Koaxb3rFgaJas1RluQdiCmviDKI */
                                private final /* synthetic */ ActivityDisplay f$1;

                                {
                                    this.f$1 = r2;
                                }

                                @Override // java.lang.Runnable
                                public final void run() {
                                    ActivityStack.this.lambda$switchFocusedStackIfNeed$0$ActivityStack(this.f$1);
                                }
                            });
                            return;
                        } else {
                            return;
                        }
                    }
                }
            }
        }
    }

    public /* synthetic */ void lambda$switchFocusedStackIfNeed$0$ActivityStack(ActivityDisplay display) {
        synchronized (this.mService.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                int index = display.getChildCount() - 1;
                while (true) {
                    if (index < 0) {
                        break;
                    }
                    ActivityStack activityStack = display.getChildAt(index);
                    if (!activityStack.inHwFreeFormWindowingMode() && !display.isFreeformStackOnTop(activityStack)) {
                        if (activityStack.topRunningActivityLocked(true) != null) {
                            if (activityStack.mStackId == this.mStackId) {
                                display.switchFocusedStack(activityStack, "switchFocusedStack");
                            }
                        }
                    }
                    index--;
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    /* JADX INFO: Multiple debug info for r9v5 'newBounds'  android.graphics.Rect: [D('prevScreenW' int), D('newBounds' android.graphics.Rect)] */
    /* JADX WARNING: Code restructure failed: missing block: B:63:0x016c, code lost:
        if (r19 == false) goto L_0x018d;
     */
    @Override // com.android.server.wm.ConfigurationContainer
    public void onConfigurationChanged(Configuration newParentConfig) {
        boolean prevIsAlwaysOnTop;
        int oldDisplayHeight;
        ActivityDisplay display;
        char c;
        int prevScreenW;
        int prevDensity;
        int prevRotation;
        int prevRotation2;
        int prevDensity2;
        Rect newBounds;
        ActivityDisplay display2;
        boolean z;
        ActivityDisplay display3;
        TaskRecord topTask;
        boolean hasNewOverrideBounds;
        int i;
        int prevWindowingMode = getWindowingMode();
        boolean prevIsAlwaysOnTop2 = isAlwaysOnTop();
        int prevRotation3 = getWindowConfiguration().getRotation();
        int prevDensity3 = getConfiguration().densityDpi;
        int prevScreenW2 = getConfiguration().screenWidthDp;
        int prevScreenH = getConfiguration().screenHeightDp;
        Rect newBounds2 = this.mTmpRect;
        getBounds(newBounds2);
        Rect oldStackBounds = new Rect(newBounds2);
        int oldDiplayWidth = this.mDisplayWidth;
        int oldDisplayHeight2 = this.mDisplayHeight;
        super.onConfigurationChanged(newParentConfig);
        if (!(prevScreenW2 == getConfiguration().screenWidthDp && prevScreenH == getConfiguration().screenHeightDp && prevDensity3 == getConfiguration().densityDpi)) {
            this.mDisplayWidth = newParentConfig.windowConfiguration.getBounds().width();
            this.mDisplayHeight = newParentConfig.windowConfiguration.getBounds().height();
        }
        ActivityDisplay display4 = getDisplay();
        if (display4 == null) {
            return;
        }
        if (getTaskStack() != null) {
            boolean windowingModeChanged = prevWindowingMode != getWindowingMode();
            if (!windowingModeChanged || topRunningActivityLocked() == null || !topRunningActivityLocked().nowVisible) {
                oldDisplayHeight = oldDisplayHeight2;
                prevIsAlwaysOnTop = prevIsAlwaysOnTop2;
            } else if (!WindowConfiguration.isHwMultiWindowingMode(prevWindowingMode) && prevWindowingMode != 1) {
                oldDisplayHeight = oldDisplayHeight2;
                prevIsAlwaysOnTop = prevIsAlwaysOnTop2;
            } else if (inHwMultiWindowingMode() || getWindowingMode() == 1) {
                if (!((WindowConfiguration.isHwMagicWindowingMode(prevWindowingMode) && getWindowingMode() == 1) || (prevWindowingMode == 1 && inHwMagicWindowingMode()))) {
                    oldDisplayHeight = oldDisplayHeight2;
                    prevIsAlwaysOnTop = prevIsAlwaysOnTop2;
                    this.mService.mHwATMSEx.reportAppWindowMode(1103, topRunningActivityLocked(), getWindowingMode(), "windowingModeChanged");
                } else {
                    oldDisplayHeight = oldDisplayHeight2;
                    prevIsAlwaysOnTop = prevIsAlwaysOnTop2;
                }
            } else {
                oldDisplayHeight = oldDisplayHeight2;
                prevIsAlwaysOnTop = prevIsAlwaysOnTop2;
            }
            int overrideWindowingMode = getRequestedOverrideWindowingMode();
            boolean hasNewOverrideBounds2 = false;
            boolean isHwMultiStackMode = WindowConfiguration.isHwMultiStackWindowingMode(overrideWindowingMode);
            if (overrideWindowingMode == 2) {
                hasNewOverrideBounds2 = getTaskStack().calculatePinnedBoundsForConfigChange(newBounds2);
                display = display4;
                prevScreenW = prevScreenW2;
                prevDensity = prevDensity3;
                prevRotation = prevRotation3;
                prevDensity2 = 3;
                prevRotation2 = 4;
                c = 1;
                newBounds = newBounds2;
            } else if (!matchParentBounds() || isHwMultiStackMode) {
                int newRotation = getWindowConfiguration().getRotation();
                boolean rotationChanged = prevRotation3 != newRotation;
                if (!rotationChanged || isHwMultiStackMode) {
                    hasNewOverrideBounds = false;
                } else {
                    display4.mDisplayContent.rotateBounds(newParentConfig.windowConfiguration.getBounds(), prevRotation3, newRotation, newBounds2);
                    hasNewOverrideBounds = true;
                }
                if (overrideWindowingMode != 3) {
                    i = 4;
                    if (overrideWindowingMode != 4) {
                    }
                } else {
                    i = 4;
                }
                if (!(!rotationChanged && !windowingModeChanged && prevDensity3 == getConfiguration().densityDpi && prevScreenW2 == getConfiguration().screenWidthDp && prevScreenH == getConfiguration().screenHeightDp)) {
                    if (isHwMultiStackMode) {
                        prevScreenW = prevScreenW2;
                        prevDensity = prevDensity3;
                        c = 1;
                        prevDensity2 = 3;
                        prevRotation = prevRotation3;
                        display = display4;
                        prevRotation2 = i;
                        newBounds = newBounds2;
                        this.mService.mHwATMSEx.calcHwMultiWindowStackBoundsForConfigChange(this, newBounds2, oldStackBounds, oldDiplayWidth, oldDisplayHeight, this.mDisplayWidth, this.mDisplayHeight, windowingModeChanged);
                        Slog.d("ActivityTaskManager", "config change for " + toShortString() + ", new stack bounds: " + newBounds);
                    } else {
                        display = display4;
                        prevScreenW = prevScreenW2;
                        prevDensity = prevDensity3;
                        prevRotation = prevRotation3;
                        c = 1;
                        prevDensity2 = 3;
                        prevRotation2 = i;
                        newBounds = newBounds2;
                        getTaskStack().calculateDockedBoundsForConfigChange(newParentConfig, newBounds);
                    }
                    hasNewOverrideBounds2 = true;
                }
                display = display4;
                prevScreenW = prevScreenW2;
                prevDensity = prevDensity3;
                prevRotation = prevRotation3;
                hasNewOverrideBounds2 = hasNewOverrideBounds;
                c = 1;
                prevDensity2 = 3;
                prevRotation2 = i;
                newBounds = newBounds2;
            } else {
                display = display4;
                prevScreenW = prevScreenW2;
                prevDensity = prevDensity3;
                prevRotation = prevRotation3;
                prevDensity2 = 3;
                prevRotation2 = 4;
                c = 1;
                newBounds = newBounds2;
            }
            if (windowingModeChanged) {
                if (overrideWindowingMode == prevDensity2) {
                    getStackDockedModeBounds(null, null, newBounds, this.mTmpRect2);
                    setTaskDisplayedBounds(null);
                    setTaskBounds(newBounds);
                    setBounds(newBounds);
                    newBounds.set(newBounds);
                    display2 = display;
                } else if (overrideWindowingMode == prevRotation2) {
                    Rect dockedBounds = display.getSplitScreenPrimaryStack().getBounds();
                    display2 = display;
                    if (display2.mDisplayContent.getDockedDividerController().isMinimizedDock() && (topTask = display2.getSplitScreenPrimaryStack().topTask()) != null) {
                        dockedBounds = topTask.getBounds();
                    }
                    getStackDockedModeBounds(dockedBounds, null, newBounds, this.mTmpRect2);
                    hasNewOverrideBounds2 = true;
                } else {
                    display2 = display;
                }
                display2.onStackWindowingModeChanged(this);
            } else {
                display2 = display;
            }
            if (!hasNewOverrideBounds2) {
                z = false;
                display3 = display2;
            } else if (prevWindowingMode != 103 || !isHwMultiStackMode) {
                z = false;
                display3 = display2;
                this.mRootActivityContainer.resizeStack(this, new Rect(newBounds), null, null, true, true, true);
            } else {
                Object[] objArr = new Object[2];
                objArr[0] = Integer.valueOf(this.mStackId);
                objArr[c] = new Rect(newBounds);
                HwMwUtils.performPolicy(17, objArr);
                z = false;
                display3 = display2;
            }
            if (prevIsAlwaysOnTop != isAlwaysOnTop() && !inHwFreeFormWindowingMode() && !inFreeformWindowingMode()) {
                display3.positionChildAtTop(this, z);
            }
        }
    }

    @Override // com.android.server.wm.ConfigurationContainer
    public void setWindowingMode(int windowingMode) {
        setWindowingMode(windowingMode, false, false, false, false, false);
    }

    private static boolean isTransientWindowingMode(int windowingMode) {
        return windowingMode == 3 || windowingMode == 4;
    }

    /* JADX INFO: finally extract failed */
    /* access modifiers changed from: package-private */
    public void setWindowingMode(int preferredWindowingMode, boolean animate, boolean showRecents, boolean enteringSplitScreenMode, boolean deferEnsuringVisibility, boolean creating) {
        int currentMode = getWindowingMode();
        boolean setAlwaysOnTopOnly = WindowConfiguration.isHwSplitScreenWindowingMode(currentMode) && WindowConfiguration.isHwFreeFormWindowingMode(preferredWindowingMode);
        boolean isWindowingModeChanged = currentMode != preferredWindowingMode;
        if (isWindowingModeChanged && currentMode == 102) {
            this.mService.mHwATMSEx.recordHwFreeFormBounds(topTask(), true);
        }
        if (inHwFreeFormWindowingMode() && !WindowConfiguration.isHwFreeFormWindowingMode(preferredWindowingMode)) {
            List<TaskRecord> removeTasks = new ArrayList<>(1);
            removeTasks.add(topTask());
            this.mService.mHwATMSEx.dispatchFreeformBallLifeState(removeTasks, "remove");
        }
        this.mWindowManager.inSurfaceTransaction(new Runnable(preferredWindowingMode, animate, showRecents, enteringSplitScreenMode, deferEnsuringVisibility, creating) {
            /* class com.android.server.wm.$$Lambda$ActivityStack$FkaZkaRIeozTqSdHkmYZNbNtF1I */
            private final /* synthetic */ int f$1;
            private final /* synthetic */ boolean f$2;
            private final /* synthetic */ boolean f$3;
            private final /* synthetic */ boolean f$4;
            private final /* synthetic */ boolean f$5;
            private final /* synthetic */ boolean f$6;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
                this.f$4 = r5;
                this.f$5 = r6;
                this.f$6 = r7;
            }

            @Override // java.lang.Runnable
            public final void run() {
                ActivityStack.this.lambda$setWindowingMode$1$ActivityStack(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6);
            }
        });
        if (setAlwaysOnTopOnly) {
            setAlwaysOnTopOnly(inHwFreeFormWindowingMode());
        } else {
            setAlwaysOnTop(inHwFreeFormWindowingMode());
        }
        boolean hasHwFreeFormMode = WindowConfiguration.isHwFreeFormWindowingMode(currentMode) || WindowConfiguration.isHwFreeFormWindowingMode(preferredWindowingMode);
        if (isWindowingModeChanged && hasHwFreeFormMode) {
            synchronized (this.mService.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    Iterator<TaskRecord> it = this.mTaskHistory.iterator();
                    while (it.hasNext()) {
                        this.mService.notifyTaskPersisterLocked(it.next(), false);
                    }
                } catch (Throwable th) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
            WindowManagerService.resetPriorityAfterLockedSection();
            this.mService.getTaskChangeNotificationController().notifyTaskStackChanged();
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: setWindowingModeInSurfaceTransaction */
    public void lambda$setWindowingMode$1$ActivityStack(int preferredWindowingMode, boolean animate, boolean showRecents, boolean enteringSplitScreenMode, boolean deferEnsuringVisibility, boolean creating) {
        int windowingMode;
        int likelyResolvedMode;
        Throwable th;
        Throwable th2;
        int likelyResolvedMode2;
        WindowState primaryWindow;
        int currentMode = getWindowingMode();
        int currentOverrideMode = getRequestedOverrideWindowingMode();
        ActivityDisplay display = getDisplay();
        if (display != null) {
            TaskRecord topTask = topTask();
            ActivityStack splitScreenStack = display.getSplitScreenPrimaryStack();
            if (!(currentMode != 3 || preferredWindowingMode == 3 || topTask == null || topTask.mTask == null || (primaryWindow = topTask.mTask.getTopVisibleAppMainWindow()) == null)) {
                primaryWindow.removeBackgroundSurfaceLocked();
            }
            if (HwMwUtils.ENABLED && this.mRestoreOverrideWindowingMode == 103 && preferredWindowingMode != 103) {
                this.mRestoreOverrideWindowingMode = 1;
            }
            int windowingMode2 = preferredWindowingMode;
            if (preferredWindowingMode == 0 && isTransientWindowingMode(currentMode)) {
                windowingMode2 = this.mRestoreOverrideWindowingMode;
            }
            this.mTmpOptions.setLaunchWindowingMode(windowingMode2);
            if (!creating && preferredWindowingMode != 12) {
                windowingMode2 = display.validateWindowingMode(windowingMode2, null, topTask, getActivityType());
            }
            if (splitScreenStack == this && windowingMode2 == 4) {
                windowingMode = this.mRestoreOverrideWindowingMode;
            } else {
                windowingMode = windowingMode2;
            }
            boolean alreadyInSplitScreenMode = display.hasSplitScreenPrimaryStack();
            boolean sendNonResizeableNotification = (enteringSplitScreenMode || preferredWindowingMode == 11 || preferredWindowingMode == 12) ? false : true;
            if (alreadyInSplitScreenMode && windowingMode == 1 && sendNonResizeableNotification && isActivityTypeStandardOrUndefined()) {
                if ((preferredWindowingMode == 3 || preferredWindowingMode == 4) || creating) {
                    this.mService.getTaskChangeNotificationController().notifyActivityDismissingDockedStack();
                    ActivityStack primarySplitStack = display.getSplitScreenPrimaryStack();
                    primarySplitStack.lambda$setWindowingMode$1$ActivityStack(0, false, false, false, true, primarySplitStack == this ? creating : false);
                }
            }
            if (display.hasCoordinationPrimaryStack() && windowingMode == 1 && creating) {
                Slog.v("ActivityTaskManager", "exit coordination mode for launching fullscreen window mode");
                this.mService.mHwATMSEx.exitCoordinationMode(false, true);
            }
            if (currentMode == windowingMode) {
                getRequestedOverrideConfiguration().windowConfiguration.setWindowingMode(windowingMode);
                return;
            }
            WindowManagerService wm = this.mService.mWindowManager;
            ActivityRecord topActivity = getTopActivity();
            if (windowingMode == 0) {
                ConfigurationContainer parent = getParent();
                if (parent != null) {
                    likelyResolvedMode2 = parent.getWindowingMode();
                } else {
                    likelyResolvedMode2 = 1;
                }
                likelyResolvedMode = likelyResolvedMode2;
            } else {
                likelyResolvedMode = windowingMode;
            }
            if (sendNonResizeableNotification && likelyResolvedMode != 1 && likelyResolvedMode != 103 && topActivity != null) {
                if (topActivity.isNonResizableOrForcedResizable() && !topActivity.noDisplay && topTask != null) {
                    this.mService.getTaskChangeNotificationController().notifyActivityForcedResizable(topTask.taskId, 1, topActivity.appInfo.packageName);
                }
            }
            wm.deferSurfaceLayout();
            if (!animate && topActivity != null) {
                try {
                    this.mStackSupervisor.mNoAnimActivities.add(topActivity);
                } catch (Throwable th3) {
                    th = th3;
                }
            }
            try {
                super.setWindowingMode(windowingMode);
                windowingMode = getWindowingMode();
                if (creating) {
                    if (showRecents && !alreadyInSplitScreenMode) {
                        try {
                            if (this.mDisplayId == 0 && windowingMode == 3) {
                                try {
                                    display.getOrCreateStack(4, 3, true).moveToFront("setWindowingMode");
                                    this.mService.mWindowManager.showRecentApps();
                                    wm.continueSurfaceLayout();
                                } catch (Throwable th4) {
                                    th2 = th4;
                                }
                            }
                        } catch (Throwable th5) {
                            th2 = th5;
                            wm.continueSurfaceLayout();
                            throw th2;
                        }
                    }
                    wm.continueSurfaceLayout();
                } else if (windowingMode == 2 || currentMode == 2) {
                    throw new IllegalArgumentException("Changing pinned windowing mode not currently supported");
                } else if (windowingMode != 3 || splitScreenStack == null) {
                    if (isTransientWindowingMode(windowingMode) && !isTransientWindowingMode(currentMode)) {
                        this.mRestoreOverrideWindowingMode = currentOverrideMode;
                    }
                    this.mTmpRect2.setEmpty();
                    if (windowingMode != 1) {
                        if (this.mTaskStack.matchParentBounds()) {
                            this.mTmpRect2.setEmpty();
                        } else {
                            this.mTaskStack.getRawBounds(this.mTmpRect2);
                        }
                    }
                    if (!Objects.equals(getRequestedOverrideBounds(), this.mTmpRect2)) {
                        resize(this.mTmpRect2, null, null);
                    }
                    if (showRecents && !alreadyInSplitScreenMode) {
                        try {
                            if (this.mDisplayId == 0 && windowingMode == 3) {
                                display.getOrCreateStack(4, 3, true).moveToFront("setWindowingMode");
                                this.mService.mWindowManager.showRecentApps();
                            }
                        } catch (Throwable th6) {
                            wm.continueSurfaceLayout();
                            throw th6;
                        }
                    }
                    wm.continueSurfaceLayout();
                    if (!deferEnsuringVisibility) {
                        this.mRootActivityContainer.ensureActivitiesVisible(null, 0, true);
                        this.mRootActivityContainer.resumeFocusedStacksTopActivities();
                    }
                } else {
                    try {
                        throw new IllegalArgumentException("Setting primary split-screen windowing mode while there is already one isn't currently supported");
                    } catch (Throwable th7) {
                        th = th7;
                        if (showRecents && !alreadyInSplitScreenMode) {
                            try {
                                if (this.mDisplayId == 0 && windowingMode == 3) {
                                    display.getOrCreateStack(4, 3, true).moveToFront("setWindowingMode");
                                    this.mService.mWindowManager.showRecentApps();
                                }
                            } catch (Throwable th8) {
                                wm.continueSurfaceLayout();
                                throw th8;
                            }
                        }
                        wm.continueSurfaceLayout();
                        throw th;
                    }
                }
            } catch (Throwable th9) {
                th = th9;
                display.getOrCreateStack(4, 3, true).moveToFront("setWindowingMode");
                this.mService.mWindowManager.showRecentApps();
                wm.continueSurfaceLayout();
                throw th;
            }
        }
    }

    @Override // com.android.server.wm.ConfigurationContainer
    public boolean isCompatible(int windowingMode, int activityType) {
        if (activityType == 0) {
            activityType = 1;
        }
        return super.isCompatible(windowingMode, activityType);
    }

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x001f: APUT  
      (r1v3 java.lang.Object[])
      (1 ??[boolean, int, float, short, byte, char])
      (wrap: java.lang.Integer : 0x001b: INVOKE  (r3v3 java.lang.Integer) = (r3v2 int) type: STATIC call: java.lang.Integer.valueOf(int):java.lang.Integer)
     */
    /* access modifiers changed from: package-private */
    public void reparent(ActivityDisplay activityDisplay, boolean onTop, boolean displayRemoved) {
        if (HwMwUtils.ENABLED) {
            Object[] objArr = new Object[2];
            objArr[0] = Integer.valueOf(getStackId());
            objArr[1] = Integer.valueOf(activityDisplay != null ? activityDisplay.mDisplayId : -1);
            HwMwUtils.performPolicy(22, objArr);
        }
        removeFromDisplay();
        this.mTmpRect2.setEmpty();
        TaskStack taskStack = this.mTaskStack;
        if (taskStack == null) {
            Log.w("ActivityTaskManager", "Task stack is not valid when reparenting.");
        } else {
            taskStack.reparent(activityDisplay.mDisplayId, this.mTmpRect2, onTop);
        }
        setBounds(this.mTmpRect2.isEmpty() ? null : this.mTmpRect2);
        activityDisplay.addChild(this, onTop ? Integer.MAX_VALUE : Integer.MIN_VALUE);
        if (!displayRemoved) {
            postReparent();
        }
    }

    /* access modifiers changed from: package-private */
    public void postReparent() {
        adjustFocusToNextFocusableStack("reparent", true);
        this.mRootActivityContainer.resumeFocusedStacksTopActivities();
        this.mRootActivityContainer.ensureActivitiesVisible(null, 0, false);
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
        TaskStack taskStack = this.mTaskStack;
        if (taskStack != null) {
            taskStack.removeIfPossible();
            this.mTaskStack = null;
        }
        onParentChanged();
    }

    /* access modifiers changed from: package-private */
    public ActivityDisplay getDisplay() {
        return this.mRootActivityContainer.getActivityDisplay(this.mDisplayId);
    }

    /* access modifiers changed from: package-private */
    public void getStackDockedModeBounds(Rect dockedBounds, Rect currentTempTaskBounds, Rect outStackBounds, Rect outTempTaskBounds) {
        TaskStack taskStack = this.mTaskStack;
        if (taskStack != null) {
            taskStack.getStackDockedModeBoundsLocked(getParent().getConfiguration(), dockedBounds, currentTempTaskBounds, outStackBounds, outTempTaskBounds);
            return;
        }
        outStackBounds.setEmpty();
        outTempTaskBounds.setEmpty();
    }

    /* access modifiers changed from: package-private */
    public void prepareFreezingTaskBounds() {
        TaskStack taskStack = this.mTaskStack;
        if (taskStack != null) {
            taskStack.prepareFreezingTaskBounds();
        }
    }

    /* access modifiers changed from: package-private */
    public void getWindowContainerBounds(Rect outBounds) {
        TaskStack taskStack = this.mTaskStack;
        if (taskStack != null) {
            taskStack.getBounds(outBounds);
        } else {
            outBounds.setEmpty();
        }
    }

    /* access modifiers changed from: package-private */
    public void positionChildWindowContainerAtTop(TaskRecord child) {
        TaskStack taskStack = this.mTaskStack;
        if (taskStack != null) {
            taskStack.positionChildAtTop(child.getTask(), true);
        }
    }

    /* access modifiers changed from: package-private */
    public void positionChildWindowContainerAtBottom(TaskRecord child) {
        boolean z = true;
        ActivityStack nextFocusableStack = getDisplay().getNextFocusableStack(child.getStack(), true);
        TaskStack taskStack = this.mTaskStack;
        if (taskStack != null) {
            Task task = child.getTask();
            if (nextFocusableStack != null) {
                z = false;
            }
            taskStack.positionChildAtBottom(task, z);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean deferScheduleMultiWindowModeChanged() {
        if (!inPinnedWindowingMode() || getTaskStack() == null) {
            return false;
        }
        return getTaskStack().deferScheduleMultiWindowModeChanged();
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
        if (this.mUpdateBoundsDeferred) {
            this.mUpdateBoundsDeferred = false;
            if (this.mUpdateBoundsDeferredCalled) {
                setTaskBounds(this.mDeferredBounds);
                setBounds(this.mDeferredBounds);
            }
            if (this.mUpdateDisplayedBoundsDeferredCalled) {
                setTaskDisplayedBounds(this.mDeferredDisplayedBounds);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean updateBoundsAllowed(Rect bounds) {
        if (!this.mUpdateBoundsDeferred) {
            return true;
        }
        if (bounds != null) {
            this.mDeferredBounds.set(bounds);
        } else {
            this.mDeferredBounds.setEmpty();
        }
        this.mUpdateBoundsDeferredCalled = true;
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean updateDisplayedBoundsAllowed(Rect bounds) {
        if (!this.mUpdateBoundsDeferred) {
            return true;
        }
        if (bounds != null) {
            this.mDeferredDisplayedBounds.set(bounds);
        } else {
            this.mDeferredDisplayedBounds.setEmpty();
        }
        this.mUpdateDisplayedBoundsDeferredCalled = true;
        return false;
    }

    @Override // com.android.server.wm.ConfigurationContainer
    public int setBounds(Rect bounds) {
        if (ActivityTaskManagerDebugConfig.DEBUG_HWFREEFORM && inMultiWindowMode()) {
            Slog.w("ActivityTaskManager", "setBounds " + toString() + " MultiWindowMode bounds = " + bounds);
        }
        return super.setBounds(!inMultiWindowMode() ? null : bounds);
    }

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

    public ActivityRecord topRunningActivityLocked(boolean focusableOnly) {
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
                if (!(r.finishing || r.mTaskOverlay)) {
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
                if (!(r.finishing || r.delayedResume || r == notTop || !r.okToShowLocked())) {
                    return r;
                }
            }
        }
        return null;
    }

    public final ActivityRecord topRunningActivityLocked(IBinder token, int taskId) {
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            TaskRecord task = this.mTaskHistory.get(taskNdx);
            if (task.taskId != taskId) {
                ArrayList<ActivityRecord> activities = task.mActivities;
                for (int i = activities.size() - 1; i >= 0; i--) {
                    ActivityRecord r = activities.get(i);
                    if (!(r.finishing || token == r.appToken || !r.okToShowLocked())) {
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
        TaskRecord task = r.getTaskRecord();
        ActivityStack stack = r.getActivityStack();
        if (stack == null || !task.mActivities.contains(r) || !this.mTaskHistory.contains(task)) {
            return null;
        }
        if (stack != this) {
            Slog.w("ActivityTaskManager", "Illegal state! task does not point to stack it is in.");
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
    public boolean isSingleTaskInstance() {
        ActivityDisplay display = getDisplay();
        return display != null && display.isSingleTaskInstance();
    }

    /* access modifiers changed from: package-private */
    public final void removeActivitiesFromLRUListLocked(TaskRecord task) {
        Iterator<ActivityRecord> it = task.mActivities.iterator();
        while (it.hasNext()) {
            ActivityRecord r = it.next();
            this.mLRUActivities.remove(r);
            this.mFIFOActivities.remove(r);
        }
    }

    /* access modifiers changed from: package-private */
    public final boolean updateLRUListLocked(ActivityRecord r) {
        boolean hadit = this.mLRUActivities.remove(r);
        this.mLRUActivities.add(r);
        if (!this.mFIFOActivities.contains(r)) {
            this.mFIFOActivities.add(r);
        }
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
        ActivityStack topFullScreenStack;
        ActivityStack primarySplitScreenStack;
        if (isAttached()) {
            ActivityDisplay display = getDisplay();
            if (inSplitScreenSecondaryWindowingMode() && (topFullScreenStack = display.getTopStackInWindowingMode(1)) != null && (primarySplitScreenStack = display.getSplitScreenPrimaryStack()) != null && display.getIndexOf(topFullScreenStack) > display.getIndexOf(primarySplitScreenStack)) {
                primarySplitScreenStack.moveToFront(reason + " splitScreenToTop");
            }
            if (!isActivityTypeHome() && returnsToHomeStack()) {
                display.moveHomeStackToFront(reason + " returnToHome");
            }
            boolean z = false;
            boolean movingTask = task != null;
            if (movingTask && inHwFreeFormWindowingMode() && !isAlwaysOnTop()) {
                setAlwaysOnTopOnly(true);
            }
            if (HwPCUtils.isHiCarCastMode() && HwPCUtils.isValidExtDisplayId(display.mDisplayId)) {
                List<String> winList = this.mService.mWindowManager.mHwWMSEx.getCarFocusList();
                winList.add("CarNavigationBar_port");
                winList.add("CarNavigationBar_horizontal");
            }
            if (!movingTask) {
                z = true;
            }
            display.positionChildAtTop(this, z, reason);
            if (movingTask) {
                insertTaskAtTop(task, null);
            }
            this.mHwActivityStackEx.moveToFrontEx(reason, task);
        }
    }

    /* access modifiers changed from: package-private */
    public void moveToBack(String reason, TaskRecord task) {
        if (isAttached()) {
            ActivityStack targetStack = this.mStackSupervisor.getTargetSplitTopStack(this);
            boolean isFullScreen = true;
            if (targetStack != null) {
                WindowManagerService windowManagerService = this.mWindowManager;
                windowManagerService.mShouldResetTime = true;
                windowManagerService.startFreezingScreen(0, 0);
                getDisplay().positionChildAtTop(targetStack, false);
                if (HwMwUtils.ENABLED && HwMwUtils.isInSuitableScene(true)) {
                    isFullScreen = !this.mService.mHwATMSEx.isSwitchToMagicWin(inSplitScreenPrimaryWindowingMode() ? this.mStackId : targetStack.getStackId(), false, getConfiguration().orientation);
                }
                if (getWindowingMode() != 3 && isFullScreen) {
                    targetStack.setWindowingMode(1);
                }
            }
            if (getWindowingMode() == 3 && isFullScreen) {
                setWindowingMode(0);
            }
            if (targetStack != null) {
                this.mWindowManager.stopFreezingScreen();
            }
            getDisplay().positionChildAtBottom(this, reason);
            if (inHwSplitScreenWindowingMode() && (!HwMwUtils.ENABLED || !HwMwUtils.performPolicy(16, new Object[]{Integer.valueOf(this.mStackId)}).getBoolean("RESULT_HWMULTISTACK", false))) {
                setWindowingMode(1, false, false, false, true, false);
            }
            if (targetStack != null) {
                targetStack.moveToFront(reason);
            }
            if (task != null) {
                insertTaskAtBottom(task);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isFocusable() {
        boolean z = false;
        if (!this.mIsForceFocusable) {
            return false;
        }
        ActivityRecord r = topRunningActivityLocked();
        RootActivityContainer rootActivityContainer = this.mRootActivityContainer;
        if (r != null && r.isFocusable()) {
            z = true;
        }
        return rootActivityContainer.isFocusable(this, z);
    }

    /* access modifiers changed from: package-private */
    public boolean isFocusableAndVisible() {
        return isFocusable() && shouldBeVisible(null);
    }

    /* access modifiers changed from: package-private */
    public final boolean isAttached() {
        ActivityDisplay display = getDisplay();
        return display != null && !display.isRemoved();
    }

    /* access modifiers changed from: package-private */
    public void findTaskLocked(ActivityRecord target, RootActivityContainer.FindTaskResult result) {
        ActivityInfo info;
        int taskNdx;
        int userId;
        boolean z;
        boolean taskIsDocument;
        Uri taskDocumentData;
        Intent intent = target.intent;
        ActivityInfo info2 = target.info;
        ComponentName cls = intent.getComponent();
        if (info2.targetActivity != null) {
            cls = new ComponentName(info2.packageName, info2.targetActivity);
        }
        int userId2 = UserHandle.getUserId(info2.applicationInfo.uid);
        boolean z2 = true;
        boolean isDocument = intent.isDocument() & true;
        Uri documentData = isDocument ? intent.getData() : null;
        if (ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
            Slog.d("ActivityTaskManager", "Looking for task of " + target + " in " + this);
        }
        int taskNdx2 = this.mTaskHistory.size() - 1;
        while (taskNdx2 >= 0) {
            TaskRecord task = this.mTaskHistory.get(taskNdx2);
            if (this.mService.mHwATMSEx.isDelayRemoveTask(task.taskId)) {
                Slog.e("ActivityTaskManager", "Skipping " + task + ": delay remove");
                info = info2;
                userId = userId2;
                z = z2;
                taskNdx = taskNdx2;
            } else if (task.voiceSession != null) {
                if (ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
                    Slog.d("ActivityTaskManager", "Skipping " + task + ": voice session");
                }
                info = info2;
                userId = userId2;
                z = z2;
                taskNdx = taskNdx2;
            } else if (task.userId != userId2) {
                if (ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
                    Slog.d("ActivityTaskManager", "Skipping " + task + ": different user");
                }
                info = info2;
                userId = userId2;
                z = z2;
                taskNdx = taskNdx2;
            } else {
                ActivityRecord r = task.getTopActivity(false);
                if (r == null || r.finishing) {
                    info = info2;
                    userId = userId2;
                    z = z2;
                    taskNdx = taskNdx2;
                } else if ((r.mUserId != userId2 && (!this.mStackSupervisor.isCurrentProfileLocked(r.mUserId) || !this.mStackSupervisor.isCurrentProfileLocked(userId2))) || r.launchMode == 3) {
                    info = info2;
                    userId = userId2;
                    taskNdx = taskNdx2;
                    z = true;
                } else if (!r.hasCompatibleActivityType(target)) {
                    if (ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
                        Slog.d("ActivityTaskManager", "Skipping " + task + ": mismatch activity type");
                    }
                    info = info2;
                    userId = userId2;
                    taskNdx = taskNdx2;
                    z = true;
                } else if (this.mRootActivityContainer.getHwRootActivityContainerEx().checkWindowModeForAppLock(target, r)) {
                    info = info2;
                    userId = userId2;
                    taskNdx = taskNdx2;
                    z = true;
                } else {
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
                    if (ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
                        userId = userId2;
                        StringBuilder sb = new StringBuilder();
                        sb.append("Comparing existing cls=");
                        sb.append(task.realActivity != null ? task.realActivity.flattenToShortString() : "");
                        sb.append("/aff=");
                        taskNdx = taskNdx2;
                        sb.append(r.getTaskRecord().rootAffinity);
                        sb.append(" to new cls=");
                        sb.append(intent.getComponent().flattenToShortString());
                        sb.append("/aff=");
                        sb.append(info2.taskAffinity);
                        Slog.d("ActivityTaskManager", sb.toString());
                    } else {
                        userId = userId2;
                        taskNdx = taskNdx2;
                    }
                    info = info2;
                    if (task.realActivity == null || task.realActivity.compareTo(cls) != 0 || !Objects.equals(documentData, taskDocumentData)) {
                        if (affinityIntent == null || affinityIntent.getComponent() == null) {
                            z = true;
                        } else if (affinityIntent.getComponent().compareTo(cls) != 0) {
                            z = true;
                        } else if (Objects.equals(documentData, taskDocumentData)) {
                            if (ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
                                Slog.d("ActivityTaskManager", "Found matching class!");
                            }
                            if (ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
                                Slog.d("ActivityTaskManager", "For Intent " + intent + " bringing to top: " + r.intent);
                            }
                            result.mRecord = r;
                            result.mIdealMatch = true;
                            return;
                        } else {
                            z = true;
                        }
                        if (this.mService.mHwATMSEx.shouldReuseActivity(target, r)) {
                            if (ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
                                Slog.d("ActivityTaskManager", "Found matching special candidate!");
                            }
                            result.mRecord = r;
                            result.mIdealMatch = false;
                        } else if (isDocument || taskIsDocument || result.mRecord != null || task.rootAffinity == null) {
                            if (ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
                                Slog.d("ActivityTaskManager", "Not a match: " + task);
                            }
                        } else if (task.rootAffinity.equals(target.taskAffinity)) {
                            if (ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
                                Slog.d("ActivityTaskManager", "Found matching affinity candidate!");
                            }
                            result.mRecord = r;
                            result.mIdealMatch = false;
                        }
                    } else {
                        if (ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
                            Slog.d("ActivityTaskManager", "Found matching class!");
                        }
                        if (ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
                            Slog.d("ActivityTaskManager", "For Intent " + intent + " bringing to top: " + r.intent);
                        }
                        result.mRecord = r;
                        result.mIdealMatch = true;
                        return;
                    }
                }
                if (ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
                    Slog.d("ActivityTaskManager", "Skipping " + task + ": mismatch root " + r);
                }
            }
            taskNdx2 = taskNdx - 1;
            z2 = z;
            userId2 = userId;
            info2 = info;
        }
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
                if (r.okToShowLocked() && !r.finishing && r.mUserId == userId) {
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
                    if (ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
                        Slog.d("ActivityTaskManager", "switchUser: stack=" + getStackId() + " moving " + task + " to top");
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
        ActivityRecord top;
        if (!this.mStackSupervisor.isCurrentProfileLocked(task.userId) && (top = task.getTopActivity()) != null && top != task.topRunningActivityLocked() && top.visible && top.isState(ActivityState.STOPPING, ActivityState.STOPPED)) {
            Flog.i((int) PAUSE_TIMEOUT_MSG, "Making invisible for switch user:  top: " + top + ", finishing: " + top.finishing + " state: " + top.getState());
            try {
                top.setVisible(false);
                int i = AnonymousClass2.$SwitchMap$com$android$server$wm$ActivityStack$ActivityState[top.getState().ordinal()];
                if (i != 1 && i != 2) {
                    return;
                }
                if (top.attachedToProcess()) {
                    this.mService.getLifecycleManager().scheduleTransaction(top.app.getThread(), (IBinder) top.appToken, (ClientTransactionItem) WindowVisibilityItem.obtain(false));
                }
            } catch (Exception e) {
                Slog.w("ActivityTaskManager", "for switch user Exception thrown making hidden: " + top.intent.getComponent(), e);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void minimalResumeActivityLocked(ActivityRecord r) {
        if (ActivityTaskManagerDebugConfig.DEBUG_STATES) {
            Slog.v("ActivityTaskManager", "Moving to RESUMED: " + r + " (starting new instance) callers=" + Debug.getCallers(5));
        }
        if (!this.mService.getActivityStartController().mCurActivityPkName.equals(r.packageName)) {
            LogPower.push(113, r.packageName);
            this.mService.getActivityStartController().mCurActivityPkName = r.packageName;
        }
        HwLog.dubaie("DUBAI_TAG_FOREGROUND", "mode=" + r.getWindowingMode() + " name=" + r.packageName);
        r.setState(ActivityState.RESUMED, "minimalResumeActivityLocked");
        r.completeResumeLocked();
        if (PACKAGE_NAMES_TO_NOTIFY_RESUMED.contains(r.info.name)) {
            r.notifyAppResumed(true);
        }
        if (r.app != null) {
            this.mService.mHwATMSEx.noteActivityDisplayed(r.shortComponentName, r.app.mUid, r.app.mPid, true);
        }
        if (ActivityTaskManagerDebugConfig.DEBUG_SAVED_STATE) {
            Slog.i("ActivityTaskManager", "Launch completed; removing icicle of " + r.icicle);
        }
    }

    private void clearLaunchTime(ActivityRecord r) {
        if (!this.mStackSupervisor.mWaitingActivityLaunched.isEmpty()) {
            this.mStackSupervisor.removeTimeoutsForActivityLocked(r);
            this.mStackSupervisor.scheduleIdleTimeoutLocked(r);
        }
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
            Flog.i((int) PAUSE_TIMEOUT_MSG, "Previously pausing activity " + this.mPausingActivity.shortComponentName + " state : " + this.mPausingActivity.getState());
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
                    if (userId == ar.mUserId && packageName.equals(ar.packageName)) {
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
            if (ActivityTaskManagerDebugConfig.DEBUG_PAUSE) {
                Slog.v("ActivityTaskManager", "Sleep still need to pause " + this.mResumedActivity);
            }
            if (ActivityTaskManagerDebugConfig.DEBUG_USER_LEAVING) {
                Slog.v("ActivityTaskManager", "Sleep => pause with userLeaving=false");
            }
            startPausingLocked(false, true, null, false);
            shouldSleep = false;
        } else if (this.mPausingActivity != null) {
            if (ActivityTaskManagerDebugConfig.DEBUG_PAUSE) {
                Slog.v("ActivityTaskManager", "Sleep still waiting to pause " + this.mPausingActivity);
            }
            shouldSleep = false;
        }
        if (!shuttingDown) {
            if (containsActivityFromStack(this.mStackSupervisor.mStoppingActivities)) {
                if (ActivityTaskManagerDebugConfig.DEBUG_PAUSE) {
                    Slog.v("ActivityTaskManager", "Sleep still need to stop " + this.mStackSupervisor.mStoppingActivities.size() + " activities");
                }
                this.mStackSupervisor.scheduleIdleLocked();
                shouldSleep = false;
            }
            if (containsActivityFromStack(this.mStackSupervisor.mGoingToSleepActivities)) {
                if (ActivityTaskManagerDebugConfig.DEBUG_PAUSE) {
                    Slog.v("ActivityTaskManager", "Sleep still need to sleep " + this.mStackSupervisor.mGoingToSleepActivities.size() + " activities");
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
            if (r.getActivityStack() == this) {
                return true;
            }
        }
        return false;
    }

    private void schedulePauseTimeout(ActivityRecord r) {
        Message msg = this.mHandler.obtainMessage(PAUSE_TIMEOUT_MSG);
        msg.obj = r;
        r.pauseTime = SystemClock.uptimeMillis();
        this.mHandler.sendMessageDelayed(msg, 500);
        if (ActivityTaskManagerDebugConfig.DEBUG_PAUSE) {
            Slog.v("ActivityTaskManager", "Waiting for pause to complete...");
        }
    }

    /* access modifiers changed from: package-private */
    public final boolean startPausingLocked(boolean userLeaving, boolean uiSleeping, ActivityRecord resuming, boolean pauseImmediately) {
        if (this.mPausingActivity != null) {
            Slog.wtf("ActivityTaskManager", "Going to pause when pause is already pending for " + this.mPausingActivity + " state=" + this.mPausingActivity.getState());
            if (!shouldSleepActivities()) {
                completePauseLocked(false, resuming);
            }
        }
        ActivityRecord prev = this.mResumedActivity;
        if (prev == null) {
            if (resuming == null) {
                Slog.wtf("ActivityTaskManager", "Trying to pause when nothing is resumed");
                this.mRootActivityContainer.resumeFocusedStacksTopActivities();
            }
            return false;
        } else if (prev == resuming) {
            Slog.wtf("ActivityTaskManager", "Trying to pause activity that is in process of being resumed");
            return false;
        } else {
            if (ActivityTaskManagerDebugConfig.DEBUG_STATES) {
                Slog.v("ActivityTaskManager", "Moving to PAUSING: " + prev + " in stack " + this.mStackId, new Exception());
            } else {
                Flog.i((int) PAUSE_TIMEOUT_MSG, "Moving to PAUSING: " + prev + " in stack " + this.mStackId);
            }
            this.mPausingActivity = prev;
            this.mLastPausedActivity = prev;
            this.mLastNoHistoryActivity = ((prev.intent.getFlags() & 1073741824) == 0 && (prev.info.flags & 128) == 0) ? null : prev;
            prev.setState(ActivityState.PAUSING, "startPausingLocked");
            prev.getTaskRecord().touchActiveTime();
            clearLaunchTime(prev);
            this.mService.updateCpuStats();
            if (prev.attachedToProcess()) {
                if (ActivityTaskManagerDebugConfig.DEBUG_PAUSE) {
                    Slog.v("ActivityTaskManager", "Enqueueing pending pause: " + prev);
                }
                try {
                    int i = prev.mUserId;
                    int identityHashCode = System.identityHashCode(prev);
                    String str = prev.shortComponentName;
                    EventLogTags.writeAmPauseActivity(i, identityHashCode, str, "userLeaving=" + userLeaving);
                    this.mService.getLifecycleManager().scheduleTransaction(prev.app.getThread(), (IBinder) prev.appToken, (ActivityLifecycleItem) PauseActivityItem.obtain(prev.finishing, userLeaving, prev.configChangeFlags, pauseImmediately));
                } catch (Exception e) {
                    Slog.w("ActivityTaskManager", "Exception thrown during pause", e);
                    this.mPausingActivity = null;
                    this.mLastPausedActivity = null;
                    this.mLastNoHistoryActivity = null;
                }
            } else {
                Flog.i((int) PAUSE_TIMEOUT_MSG, "Clear pausing activity " + this.mPausingActivity + " in stack " + this.mStackId + " for tha app is not ready");
                this.mPausingActivity = null;
                this.mLastPausedActivity = null;
                this.mLastNoHistoryActivity = null;
            }
            if (!uiSleeping && !this.mService.isSleepingOrShuttingDownLocked()) {
                try {
                    this.mStackSupervisor.acquireLaunchWakelock();
                } catch (IllegalStateException e2) {
                    Slog.w("ActivityTaskManager", "Exception thrown when run performance script", e2);
                }
            }
            if (this.mPausingActivity != null) {
                if (!uiSleeping) {
                    prev.pauseKeyDispatchingLocked();
                } else if (ActivityTaskManagerDebugConfig.DEBUG_PAUSE) {
                    Slog.v("ActivityTaskManager", "Key dispatch not paused for screen off");
                }
                if (pauseImmediately) {
                    completePauseLocked(false, resuming);
                    return false;
                }
                schedulePauseTimeout(prev);
                return true;
            }
            if (ActivityTaskManagerDebugConfig.DEBUG_PAUSE) {
                Slog.v("ActivityTaskManager", "Activity not running, resuming next.");
            }
            if (resuming == null) {
                this.mStackSupervisor.mActivityLaunchTrack = " activityNotRunning";
                this.mRootActivityContainer.resumeFocusedStacksTopActivities();
            }
            return false;
        }
    }

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x00a0: APUT  (r5v4 java.lang.Object[]), (3 ??[int, float, short, byte, char]), (r8v1 java.lang.String) */
    /* access modifiers changed from: package-private */
    public final void activityPausedLocked(IBinder token, boolean timeout) {
        if (ActivityTaskManagerDebugConfig.DEBUG_PAUSE) {
            Slog.v("ActivityTaskManager", "Activity paused: token=" + token + ", timeout=" + timeout);
        }
        ActivityRecord r = isInStackLocked(token);
        if (r != null) {
            this.mHandler.removeMessages(PAUSE_TIMEOUT_MSG, r);
            if (this.mPausingActivity == r) {
                StringBuilder sb = new StringBuilder();
                sb.append("Moving to PAUSED: ");
                sb.append(r);
                sb.append(timeout ? " (due to timeout)" : " (pause complete)");
                sb.append(" in stack ");
                sb.append(this.mStackId);
                Flog.i((int) PAUSE_TIMEOUT_MSG, sb.toString());
                this.mService.mWindowManager.deferSurfaceLayout();
                try {
                    completePauseLocked(true, null);
                    return;
                } finally {
                    this.mService.mWindowManager.continueSurfaceLayout();
                }
            } else {
                Object[] objArr = new Object[4];
                objArr[0] = Integer.valueOf(r.mUserId);
                objArr[1] = Integer.valueOf(System.identityHashCode(r));
                objArr[2] = r.shortComponentName;
                ActivityRecord activityRecord = this.mPausingActivity;
                objArr[3] = activityRecord != null ? activityRecord.shortComponentName : "(none)";
                EventLog.writeEvent(30012, objArr);
                if (r.isState(ActivityState.PAUSING)) {
                    r.setState(ActivityState.PAUSED, "activityPausedLocked");
                    if (r.finishing) {
                        Flog.i((int) PAUSE_TIMEOUT_MSG, "Executing finish of failed to pause activity: " + r);
                        finishCurrentActivityLocked(r, 2, false, "activityPausedLocked");
                    } else {
                        Flog.i((int) PAUSE_TIMEOUT_MSG, "Not process of failed to pause activity: " + r);
                        r.resumeKeyDispatchingLocked();
                    }
                }
            }
        } else {
            ActivityRecord record = ActivityRecord.forTokenLocked(token);
            if (record != null) {
                Flog.i((int) PAUSE_TIMEOUT_MSG, "FAILED to find record " + record + " in stack " + this.mStackId + " while pausing " + this.mPausingActivity);
            }
        }
        this.mRootActivityContainer.ensureActivitiesVisible(null, 0, false);
    }

    private void completePauseLocked(boolean resumeNext, ActivityRecord resuming) {
        ActivityRecord prev = this.mPausingActivity;
        if (ActivityTaskManagerDebugConfig.DEBUG_PAUSE) {
            Slog.v("ActivityTaskManager", "Complete pause: " + prev);
        }
        if (prev != null) {
            prev.setWillCloseOrEnterPip(false);
            boolean wasStopping = prev.isState(ActivityState.STOPPING);
            prev.setState(ActivityState.PAUSED, "completePausedLocked");
            if (prev.finishing) {
                if (ActivityTaskManagerDebugConfig.DEBUG_PAUSE) {
                    Slog.v("ActivityTaskManager", "Executing finish of activity: " + prev);
                }
                prev = finishCurrentActivityLocked(prev, 2, false, "completePausedLocked");
            } else if (prev.hasProcess()) {
                if (ActivityTaskManagerDebugConfig.DEBUG_PAUSE) {
                    Slog.v("ActivityTaskManager", "Enqueue pending stop if needed: " + prev + " wasStopping=" + wasStopping + " visible=" + prev.visible);
                }
                if (prev.deferRelaunchUntilPaused) {
                    Slog.v("ActivityTaskManager", "Re-launching after pause: " + prev);
                    prev.relaunchActivityLocked(false, prev.preserveWindowOnDeferredRelaunch);
                } else if (wasStopping) {
                    prev.setState(ActivityState.STOPPING, "completePausedLocked");
                } else if (!prev.visible || shouldSleepOrShutDownActivities()) {
                    prev.setDeferHidingClient(false);
                    addToStopping(prev, true, false, "completePauseLocked");
                }
            } else {
                if (ActivityTaskManagerDebugConfig.DEBUG_PAUSE) {
                    Slog.v("ActivityTaskManager", "App died during pause, not stopping: " + prev);
                }
                prev = null;
            }
            if (prev != null) {
                prev.stopFreezingScreenLocked(true);
            }
            this.mPausingActivity = null;
        }
        if (resumeNext) {
            ActivityStack topStack = this.mRootActivityContainer.getTopDisplayFocusedStack();
            if (!topStack.shouldSleepOrShutDownActivities()) {
                this.mStackSupervisor.mActivityLaunchTrack = "activityPaused";
                this.mRootActivityContainer.resumeFocusedStacksTopActivities(topStack, prev, null);
            } else {
                checkReadyForSleep();
                ActivityRecord top = topStack.topRunningActivityLocked();
                if (top == null || !(prev == null || top == prev)) {
                    this.mStackSupervisor.mActivityLaunchTrack = "sleepingNoMoreActivityRun";
                    this.mRootActivityContainer.resumeFocusedStacksTopActivities();
                }
            }
        }
        if (prev != null) {
            prev.resumeKeyDispatchingLocked();
            if (prev.hasProcess() && prev.cpuTimeAtResume > 0) {
                long diff = prev.app.getCpuTime() - prev.cpuTimeAtResume;
                if (diff > 0) {
                    this.mService.mH.post(PooledLambda.obtainRunnable($$Lambda$1636dquQO0UvkFayOGf_gceB4iw.INSTANCE, this.mService.mAmInternal, prev.info.packageName, Integer.valueOf(prev.info.applicationInfo.uid), Long.valueOf(diff)));
                }
            }
            prev.cpuTimeAtResume = 0;
        }
        if (this.mStackSupervisor.mAppVisibilitiesChangedSinceLastPause || (getDisplay() != null && getDisplay().hasPinnedStack())) {
            this.mService.getTaskChangeNotificationController().notifyTaskStackChanged();
            this.mStackSupervisor.mAppVisibilitiesChangedSinceLastPause = false;
        }
        this.mRootActivityContainer.ensureActivitiesVisible(resuming, 0, false);
        if (getDisplay() == null) {
            Slog.i("ActivityTaskManager", "getDisplay() == null, DisplayId: " + this.mDisplayId + "  StackId: " + this.mStackId);
        }
    }

    private void addToStopping(ActivityRecord r, boolean scheduleIdle, boolean idleDelayed, String reason) {
        boolean forceIdle = false;
        if (!this.mStackSupervisor.mStoppingActivities.contains(r)) {
            EventLog.writeEvent(30066, Integer.valueOf(r.mUserId), Integer.valueOf(System.identityHashCode(r)), r.shortComponentName, reason);
            this.mStackSupervisor.mStoppingActivities.add(r);
        }
        if (this.mStackSupervisor.mStoppingActivities.size() > 3 || (r.frontOfTask && this.mTaskHistory.size() <= 1)) {
            forceIdle = true;
        }
        if (scheduleIdle || forceIdle) {
            if (ActivityTaskManagerDebugConfig.DEBUG_PAUSE) {
                StringBuilder sb = new StringBuilder();
                sb.append("Scheduling idle now: forceIdle=");
                sb.append(forceIdle);
                sb.append("immediate=");
                sb.append(!idleDelayed);
                Slog.v("ActivityTaskManager", sb.toString());
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
                    if (ActivityTaskManagerDebugConfig.DEBUG_VISIBILITY) {
                        Slog.v(TAG_VISIBILITY, "It is in finishing activity now");
                    }
                } else if (r.visibleIgnoringKeyguard || r == starting) {
                    if (r.fullscreen || r.hasWallpaper) {
                        if (ActivityTaskManagerDebugConfig.DEBUG_VISIBILITY) {
                            Slog.v(TAG_VISIBILITY, "Stack has at least one fullscreen activity -> untranslucent");
                        }
                        return false;
                    } else if (HwMwUtils.ENABLED && r.inHwMagicWindowingMode() && r.getActivityStack() != null && r.getActivityStack().isFocusable() && r.visible) {
                        return false;
                    }
                } else if (ActivityTaskManagerDebugConfig.DEBUG_VISIBILITY) {
                    Slog.v(TAG_VISIBILITY, "It is not the currently starting activity");
                }
            }
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean isTopStackOnDisplay() {
        ActivityDisplay display = getDisplay();
        return display != null && display.isTopStack(this);
    }

    /* access modifiers changed from: package-private */
    public boolean isFocusedStackOnDisplay() {
        ActivityDisplay display = getDisplay();
        return display != null && this == display.getFocusedStack();
    }

    /* access modifiers changed from: package-private */
    public boolean isTopActivityVisible() {
        ActivityRecord topActivity = getTopActivity();
        return topActivity != null && topActivity.visible;
    }

    /* access modifiers changed from: protected */
    public boolean shouldBeVisible(ActivityRecord starting) {
        return getVisibility(starting) != 2;
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:195:0x02e4, code lost:
        r14 = r23;
     */
    /* JADX WARNING: Removed duplicated region for block: B:212:0x0203 A[SYNTHETIC] */
    @StackVisibility
    public int getVisibility(ActivityRecord starting) {
        boolean gotOpaqueSplitScreenPrimary;
        boolean gotSplitScreenStack;
        boolean gotTranslucentFullscreen;
        boolean gotTranslucentSplitScreenPrimary;
        boolean shouldBeVisible;
        int i;
        int i2;
        boolean shouldBeVisible2;
        int i3;
        int i4;
        ActivityRecord top;
        if (!isAttached() || this.mForceHidden) {
            return 2;
        }
        ActivityDisplay display = getDisplay();
        boolean gotOpaqueSplitScreenSecondary = false;
        boolean otherRect = false;
        boolean gotTranslucentFullscreen2 = false;
        boolean gotTranslucentSplitScreenSecondary = false;
        boolean gotHwSplitScreenPrimary = false;
        boolean gotHwSplitScreenSecondary = false;
        boolean gotHwTvSplitPrimary = false;
        boolean gotHwTvSplitSecondary = false;
        boolean shouldBeVisible3 = true;
        int windowingMode = getWindowingMode();
        boolean isAssistantType = isActivityTypeAssistant();
        if (ActivityTaskManagerDebugConfig.DEBUG_VISIBILITY) {
            String str = TAG_VISIBILITY;
            gotSplitScreenStack = false;
            StringBuilder sb = new StringBuilder();
            gotOpaqueSplitScreenPrimary = false;
            sb.append("Current stack: ");
            sb.append(toShortString());
            Slog.v(str, sb.toString());
        } else {
            gotSplitScreenStack = false;
            gotOpaqueSplitScreenPrimary = false;
        }
        int i5 = display.getChildCount() - 1;
        while (true) {
            if (i5 < 0) {
                gotTranslucentFullscreen = otherRect;
                gotTranslucentSplitScreenPrimary = gotTranslucentFullscreen2;
                shouldBeVisible = shouldBeVisible3;
                i = 0;
                i2 = 2;
                break;
            }
            ActivityStack other = display.getChildAt(i5);
            boolean hasRunningActivities = other.topRunningActivityLocked() != null;
            if (other != this) {
                if (!hasRunningActivities) {
                    shouldBeVisible = shouldBeVisible3;
                } else {
                    int otherWindowingMode = other.getWindowingMode();
                    if (ActivityTaskManagerDebugConfig.DEBUG_VISIBILITY) {
                        String str2 = TAG_VISIBILITY;
                        shouldBeVisible = shouldBeVisible3;
                        StringBuilder sb2 = new StringBuilder();
                        gotTranslucentFullscreen = otherRect;
                        sb2.append("Other stack:");
                        sb2.append(other.toShortString());
                        Slog.v(str2, sb2.toString());
                    } else {
                        gotTranslucentFullscreen = otherRect;
                        shouldBeVisible = shouldBeVisible3;
                    }
                    if (windowingMode == 105 && otherWindowingMode == 105) {
                        Rect rect = getBounds();
                        Rect otherRect2 = other.getBounds();
                        gotTranslucentSplitScreenPrimary = gotTranslucentFullscreen2;
                        if (rect.left != otherRect2.left || rect.top != otherRect2.top) {
                            i = 0;
                        } else if (other.isStackTranslucent(starting)) {
                            if (ActivityTaskManagerDebugConfig.DEBUG_VISIBILITY) {
                                Slog.v(TAG_VISIBILITY, "hwPCMultiDisplay is behind a translucent fullscreen stack");
                            }
                            otherRect = true;
                            gotTranslucentFullscreen2 = gotTranslucentSplitScreenPrimary;
                        } else {
                            shouldBeVisible2 = false;
                            if (ActivityTaskManagerDebugConfig.DEBUG_VISIBILITY) {
                                Slog.v(TAG_VISIBILITY, "not top activity getVisibility, Current stack: " + toShortString() + ", shouldBeVisible false");
                            }
                            i = 0;
                            i2 = 2;
                        }
                    } else {
                        gotTranslucentSplitScreenPrimary = gotTranslucentFullscreen2;
                        if (inHwPCMultiStackWindowingMode() && isAlwaysOnTop()) {
                            return 0;
                        }
                        i = 0;
                    }
                    if (otherWindowingMode == 1 || otherWindowingMode == 103) {
                        int activityType = other.getActivityType();
                        if (windowingMode == 3) {
                            if (activityType != 2) {
                                if (activityType == 4 && this.mWindowManager.getRecentsAnimationController() != null) {
                                    i2 = 2;
                                    break;
                                }
                            } else {
                                i2 = 2;
                                break;
                            }
                        }
                        if (inHwMultiStackWindowingMode()) {
                            if (activityType == 2) {
                                return 2;
                            }
                            if (activityType == 4 && this.mWindowManager.getRecentsAnimationController() != null) {
                                return 2;
                            }
                        }
                        if (!other.isStackTranslucent(starting)) {
                            return 2;
                        }
                        if (ActivityTaskManagerDebugConfig.DEBUG_VISIBILITY) {
                            Slog.v(TAG_VISIBILITY, "It is behind a translucent fullscreen stack");
                        }
                        otherRect = true;
                        gotTranslucentFullscreen2 = gotTranslucentSplitScreenPrimary;
                    } else {
                        if ((otherWindowingMode == 3 || otherWindowingMode == 11) && !gotOpaqueSplitScreenPrimary) {
                            gotSplitScreenStack = true;
                            gotHwSplitScreenPrimary = true;
                            boolean gotTranslucentSplitScreenPrimary2 = other.isStackTranslucent(starting);
                            gotOpaqueSplitScreenPrimary = !gotTranslucentSplitScreenPrimary2;
                            if ((windowingMode == 3 || windowingMode == 11) && gotOpaqueSplitScreenPrimary) {
                                if (!ActivityTaskManagerDebugConfig.DEBUG_VISIBILITY) {
                                    return 2;
                                }
                                Slog.v(TAG_VISIBILITY, "It is behind another opaque stack in ssp mode -> invisible");
                                return 2;
                            } else if ((windowingMode == 12 && gotOpaqueSplitScreenPrimary && (top = topRunningActivityLocked()) != null && top.shortComponentName != null && !top.shortComponentName.contains("CollaborationActivity")) || inHwMultiStackWindowingMode()) {
                                return 2;
                            } else {
                                gotTranslucentSplitScreenPrimary = gotTranslucentSplitScreenPrimary2;
                            }
                        } else if ((otherWindowingMode == 4 || otherWindowingMode == 12) && !gotOpaqueSplitScreenSecondary) {
                            gotSplitScreenStack = true;
                            gotHwSplitScreenSecondary = true;
                            gotTranslucentSplitScreenSecondary = other.isStackTranslucent(starting);
                            gotOpaqueSplitScreenSecondary = !gotTranslucentSplitScreenSecondary;
                            if (!(windowingMode == 4 || windowingMode == 12)) {
                                if (windowingMode != 11) {
                                    i4 = 2;
                                } else if (other.getActivityType() != 2 || getDisplay() == null) {
                                    i4 = 2;
                                } else if (getDisplay().getFocusedStack() == null) {
                                    i4 = 2;
                                } else if (!getDisplay().getFocusedStack().isActivityTypeHome()) {
                                    i4 = 2;
                                }
                                if (inHwMultiStackWindowingMode()) {
                                    return i4;
                                }
                            }
                            if (!gotOpaqueSplitScreenSecondary) {
                                i4 = 2;
                                if (inHwMultiStackWindowingMode()) {
                                }
                            } else if (!ActivityTaskManagerDebugConfig.DEBUG_VISIBILITY) {
                                return 2;
                            } else {
                                Slog.v(TAG_VISIBILITY, "It is behind another opaque stack in sss mode -> invisible");
                                return 2;
                            }
                        } else if (other.inHwSplitScreenPrimaryWindowingMode() && !gotHwSplitScreenPrimary) {
                            gotHwSplitScreenPrimary = true;
                            if (inHwSplitScreenPrimaryWindowingMode()) {
                                return 2;
                            }
                        } else if (other.inHwSplitScreenSecondaryWindowingMode() && !gotHwSplitScreenSecondary) {
                            gotHwSplitScreenSecondary = true;
                            if (inHwSplitScreenSecondaryWindowingMode()) {
                                return 2;
                            }
                        } else if (other.inHwTvSplitPrimaryWindowingMode() && !gotHwTvSplitPrimary) {
                            gotHwTvSplitPrimary = true;
                            if (inHwTvSplitPrimaryWindowingMode()) {
                                return 2;
                            }
                        } else if (other.inHwTvSplitSecondaryWindowingMode() && !gotHwTvSplitSecondary) {
                            gotHwTvSplitSecondary = true;
                            if (inHwTvSplitSecondaryWindowingMode()) {
                                return 2;
                            }
                        }
                        if (gotHwTvSplitPrimary || gotHwTvSplitSecondary) {
                            i3 = 1;
                            if (windowingMode == 1) {
                                return 2;
                            }
                        } else {
                            i3 = 1;
                        }
                        if (gotHwSplitScreenPrimary && gotHwSplitScreenSecondary && (windowingMode == i3 || windowingMode == 103 || windowingMode == 102)) {
                            return 2;
                        }
                        if (!gotOpaqueSplitScreenPrimary || !gotOpaqueSplitScreenSecondary) {
                            if (!isAssistantType || !gotSplitScreenStack) {
                                otherRect = gotTranslucentFullscreen;
                                gotTranslucentFullscreen2 = gotTranslucentSplitScreenPrimary;
                            } else if (!ActivityTaskManagerDebugConfig.DEBUG_VISIBILITY) {
                                return 2;
                            } else {
                                Slog.v(TAG_VISIBILITY, "Assistant stack can't be visible behind split-screen -> invisible");
                                return 2;
                            }
                        } else if (!ActivityTaskManagerDebugConfig.DEBUG_VISIBILITY) {
                            return 2;
                        } else {
                            Slog.v(TAG_VISIBILITY, "It is in ssw mode -> invisible");
                            return 2;
                        }
                    }
                }
                i5--;
                display = display;
                shouldBeVisible3 = shouldBeVisible;
            } else if (windowingMode != 3 && this.mService.mSkipShowLauncher) {
                return 2;
            } else {
                if (ActivityTaskManagerDebugConfig.DEBUG_VISIBILITY) {
                    Slog.v(TAG_VISIBILITY, "No other stack occluding -> visible");
                }
                shouldBeVisible2 = hasRunningActivities || isInStackLocked(starting) != null || isActivityTypeHome();
                gotTranslucentFullscreen = otherRect;
                gotTranslucentSplitScreenPrimary = gotTranslucentFullscreen2;
                i = 0;
                i2 = 2;
            }
        }
        if (!shouldBeVisible2) {
            return i2;
        }
        if (windowingMode != 1) {
            if (windowingMode != 3) {
                if (windowingMode == 4 && gotTranslucentSplitScreenSecondary) {
                    return 1;
                }
            } else if (gotTranslucentSplitScreenPrimary) {
                return 1;
            }
        } else if (gotTranslucentSplitScreenPrimary || gotTranslucentSplitScreenSecondary) {
            return 1;
        }
        if (gotTranslucentFullscreen) {
            return 1;
        }
        return i;
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

    /* JADX INFO: Multiple debug info for r11v11 'activityNdx'  int: [D('top' com.android.server.wm.ActivityRecord), D('activityNdx' int)] */
    /* access modifiers changed from: package-private */
    public final void ensureActivitiesVisibleLocked(ActivityRecord starting, int configChanges, boolean preserveWindows, boolean notifyClients) {
        Throwable th;
        TaskRecord task;
        int taskNdx;
        ActivityRecord top;
        int i;
        int activityNdx;
        boolean behindFullscreenActivity;
        boolean reallyVisible;
        boolean reallyVisible2;
        boolean aboveTop;
        int i2;
        boolean aboveTop2;
        int i3 = false;
        this.mTopActivityOccludesKeyguard = false;
        this.mTopDismissingKeyguardActivity = null;
        this.mStackSupervisor.getKeyguardController().beginActivityVisibilityUpdate();
        try {
            ActivityRecord top2 = topRunningActivityLocked();
            if (ActivityTaskManagerDebugConfig.DEBUG_VISIBILITY) {
                Slog.v(TAG_VISIBILITY, "ensureActivitiesVisible behind " + top2 + " configChanges=0x" + Integer.toHexString(configChanges));
            }
            if (top2 != null) {
                checkTranslucentActivityWaiting(top2);
            }
            boolean aboveTop3 = top2 != null;
            boolean stackShouldBeVisible = shouldBeVisible(starting);
            boolean behindFullscreenActivity2 = !stackShouldBeVisible;
            boolean isBehindSplit = false;
            boolean resumeNextActivity = isFocusable() && isInStackLocked(starting) == null;
            int[] activityPositions = {0, 0};
            int taskNdx2 = this.mTaskHistory.size() - 1;
            int configChanges2 = configChanges;
            while (taskNdx2 >= 0) {
                try {
                    TaskRecord task2 = this.mTaskHistory.get(taskNdx2);
                    ArrayList<ActivityRecord> activities = task2.mActivities;
                    int configChanges3 = configChanges2;
                    int configChanges4 = activities.size() - 1;
                    boolean resumeNextActivity2 = resumeNextActivity;
                    while (configChanges4 >= 0) {
                        try {
                            ActivityRecord r = activities.get(configChanges4);
                            if (!r.finishing) {
                                boolean isTop = r == top2;
                                if (!aboveTop3 || isTop) {
                                    boolean visibleIgnoringKeyguard = r.shouldBeVisibleIgnoringKeyguard(behindFullscreenActivity2);
                                    boolean reallyVisible3 = r.shouldBeVisible(behindFullscreenActivity2);
                                    if (visibleIgnoringKeyguard) {
                                        behindFullscreenActivity = updateBehindFullscreen(!stackShouldBeVisible, behindFullscreenActivity2, r);
                                    } else {
                                        behindFullscreenActivity = behindFullscreenActivity2;
                                    }
                                    if (!isBehindSplit || !reallyVisible3 || !r.isShowInSplit() || r.isSplitBaseActivity()) {
                                        reallyVisible = reallyVisible3;
                                    } else {
                                        reallyVisible = false;
                                    }
                                    boolean isBehindSplit2 = isBehindSplitActivity(r, isTop, false);
                                    if (!r.inHwMagicWindowingMode() || !HwMwUtils.ENABLED || !stackShouldBeVisible || top2 == null) {
                                        aboveTop = false;
                                        reallyVisible2 = reallyVisible;
                                    } else {
                                        aboveTop = false;
                                        reallyVisible2 = HwMwUtils.performPolicy(3, new Object[]{top2.appToken, r.appToken, Boolean.valueOf(reallyVisible), activityPositions}).getBoolean("BUNDLE_RESULT_UPDATE_VISIBILITY", reallyVisible);
                                    }
                                    if (reallyVisible2) {
                                        try {
                                            if (ActivityTaskManagerDebugConfig.DEBUG_VISIBILITY) {
                                                Slog.v(TAG_VISIBILITY, "Make visible? " + r + " finishing=" + r.finishing + " state=" + r.getState());
                                            }
                                            if (r == starting || !notifyClients) {
                                                i2 = 0;
                                            } else {
                                                i2 = 0;
                                                r.ensureActivityConfiguration(0, preserveWindows, true);
                                            }
                                            if (!r.attachedToProcess()) {
                                                aboveTop2 = aboveTop;
                                                i = i2;
                                                top = top2;
                                                activityNdx = configChanges4;
                                                taskNdx = taskNdx2;
                                                if (makeVisibleAndRestartIfNeeded(starting, configChanges3, isTop, resumeNextActivity2, r)) {
                                                    if (activityNdx >= activities.size()) {
                                                        activityNdx = activities.size() - 1;
                                                    } else {
                                                        resumeNextActivity2 = false;
                                                    }
                                                }
                                            } else {
                                                taskNdx = taskNdx2;
                                                top = top2;
                                                aboveTop2 = aboveTop;
                                                activityNdx = configChanges4;
                                                i = i2;
                                                if (r.visible) {
                                                    if (ActivityTaskManagerDebugConfig.DEBUG_VISIBILITY) {
                                                        Slog.v(TAG_VISIBILITY, "Skipping: already visible at " + r);
                                                    }
                                                    if (r.mClientVisibilityDeferred && notifyClients) {
                                                        r.makeClientVisible();
                                                    }
                                                    if (r.handleAlreadyVisible()) {
                                                        resumeNextActivity2 = false;
                                                    }
                                                    if (notifyClients) {
                                                        if (!this.mStackSupervisor.getKeyguardController().isKeyguardShowing(this.mDisplayId != -1 ? this.mDisplayId : i)) {
                                                            r.makeActiveIfNeeded(starting);
                                                        }
                                                    }
                                                } else {
                                                    if (!this.mService.getActivityStartController().mCurActivityPkName.equals(r.appInfo.packageName)) {
                                                        LogPower.push(148, "visible", r.appInfo.packageName);
                                                    }
                                                    r.makeVisibleIfNeeded(starting, notifyClients);
                                                }
                                            }
                                            configChanges3 |= r.configChangeFlags;
                                            behindFullscreenActivity2 = behindFullscreenActivity;
                                            aboveTop3 = aboveTop2;
                                            isBehindSplit = isBehindSplit2;
                                        } catch (Throwable th2) {
                                            th = th2;
                                            this.mStackSupervisor.getKeyguardController().endActivityVisibilityUpdate();
                                            throw th;
                                        }
                                    } else {
                                        taskNdx = taskNdx2;
                                        top = top2;
                                        i = 0;
                                        activityNdx = configChanges4;
                                        if (ActivityTaskManagerDebugConfig.DEBUG_VISIBILITY || r.isState(ActivityState.RESUMED)) {
                                            String str = TAG_VISIBILITY;
                                            StringBuilder sb = new StringBuilder();
                                            sb.append("Make invisible? ");
                                            sb.append(r);
                                            sb.append(" finishing=");
                                            sb.append(r.finishing);
                                            sb.append(" state=");
                                            sb.append(r.getState());
                                            sb.append(" stackShouldBeVisible=");
                                            sb.append(stackShouldBeVisible);
                                            sb.append(" behindFullscreenActivity=");
                                            sb.append(behindFullscreenActivity);
                                            sb.append(" mLaunchTaskBehind=");
                                            sb.append(r.mLaunchTaskBehind);
                                            sb.append(" keyguardShowing = ");
                                            sb.append(this.mStackSupervisor.getKeyguardController().isKeyguardShowing(this.mDisplayId != -1 ? this.mDisplayId : 0));
                                            sb.append(" keyguardLocked = ");
                                            sb.append(this.mStackSupervisor.getKeyguardController().isKeyguardLocked());
                                            sb.append(" r.visibleIgnoringKeyguard = ");
                                            sb.append(r.visibleIgnoringKeyguard);
                                            Slog.v(str, sb.toString());
                                        }
                                        makeInvisible(r);
                                        behindFullscreenActivity2 = behindFullscreenActivity;
                                        aboveTop3 = aboveTop;
                                        isBehindSplit = isBehindSplit2;
                                    }
                                    configChanges4 = activityNdx - 1;
                                    task2 = task2;
                                    i3 = i;
                                    top2 = top;
                                    taskNdx2 = taskNdx;
                                }
                            }
                            taskNdx = taskNdx2;
                            top = top2;
                            i = 0;
                            activityNdx = configChanges4;
                            configChanges4 = activityNdx - 1;
                            task2 = task2;
                            i3 = i;
                            top2 = top;
                            taskNdx2 = taskNdx;
                        } catch (Throwable th3) {
                            th = th3;
                            this.mStackSupervisor.getKeyguardController().endActivityVisibilityUpdate();
                            throw th;
                        }
                    }
                    if (getWindowingMode() == 5) {
                        behindFullscreenActivity2 = !stackShouldBeVisible ? true : i3;
                    } else if (isActivityTypeHome()) {
                        if (ActivityTaskManagerDebugConfig.DEBUG_VISIBILITY) {
                            String str2 = TAG_VISIBILITY;
                            StringBuilder sb2 = new StringBuilder();
                            sb2.append("Home task: at ");
                            task = task2;
                            sb2.append(task);
                            sb2.append(" stackShouldBeVisible=");
                            sb2.append(stackShouldBeVisible);
                            sb2.append(" behindFullscreenActivity=");
                            sb2.append(behindFullscreenActivity2);
                            Slog.v(str2, sb2.toString());
                        } else {
                            task = task2;
                        }
                        if (task.getTopActivity() != null) {
                            behindFullscreenActivity2 = true;
                        }
                    }
                    taskNdx2--;
                    resumeNextActivity = resumeNextActivity2;
                    configChanges2 = configChanges3;
                    i3 = i3;
                    top2 = top2;
                } catch (Throwable th4) {
                    th = th4;
                    this.mStackSupervisor.getKeyguardController().endActivityVisibilityUpdate();
                    throw th;
                }
            }
            if (this.mTranslucentActivityWaiting != null && this.mUndrawnActivitiesBelowTopTranslucent.isEmpty()) {
                notifyActivityDrawnLocked(null);
            }
            this.mStackSupervisor.getKeyguardController().endActivityVisibilityUpdate();
        } catch (Throwable th5) {
            th = th5;
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

    @Override // com.android.server.wm.ConfigurationContainer
    public boolean supportsSplitScreenWindowingMode() {
        TaskRecord topTask = topTask();
        return super.supportsSplitScreenWindowingMode() && (topTask == null || topTask.supportsSplitScreenWindowingMode());
    }

    /* access modifiers changed from: package-private */
    public boolean affectedBySplitScreenResize() {
        int windowingMode;
        if (!supportsSplitScreenWindowingMode() || (windowingMode = getWindowingMode()) == 5 || windowingMode == 2) {
            return false;
        }
        return true;
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
        int displayId = this.mDisplayId;
        if (displayId == -1) {
            displayId = 0;
        }
        boolean keyguardOrAodShowing = this.mStackSupervisor.getKeyguardController().isKeyguardOrAodShowing(displayId);
        boolean keyguardLocked = this.mStackSupervisor.getKeyguardController().isKeyguardLocked();
        boolean showWhenLocked = r.canShowWhenLocked();
        boolean dismissKeyguard = r.mAppWindowToken != null && r.mAppWindowToken.containsDismissKeyguardWindow();
        if (keyguardLocked && ((showWhenLocked || dismissKeyguard) && !this.mService.mHwATMSEx.isAllowToStartActivity(this.mService.mContext, this.mService.mContext.getPackageName(), r.info, keyguardLocked, ((ActivityTaskManagerInternal) LocalServices.getService(ActivityTaskManagerInternal.class)).getLastResumedActivity()))) {
            showWhenLocked = false;
            dismissKeyguard = false;
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
        if (HwPCUtils.isHiCarCastMode() && HwPCUtils.isValidExtDisplayId(displayId)) {
            HwPCUtils.log("ActivityTaskManager", "Ignore keyguard status in HiCar mode. ShouldBeVisible: " + shouldBeVisible);
            return shouldBeVisible;
        } else if (keyguardOrAodShowing) {
            if (!shouldBeVisible || !this.mStackSupervisor.getKeyguardController().canShowActivityWhileKeyguardShowing(r, dismissKeyguard)) {
                return false;
            }
            return true;
        } else if (!keyguardLocked) {
            return shouldBeVisible;
        } else {
            if (!shouldBeVisible || !this.mStackSupervisor.getKeyguardController().canShowWhileOccluded(dismissKeyguard, showWhenLocked)) {
                return false;
            }
            return true;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean canShowWithInsecureKeyguard() {
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
            this.mHandler.removeMessages(TRANSLUCENT_TIMEOUT_MSG);
        }
    }

    private boolean makeVisibleAndRestartIfNeeded(ActivityRecord starting, int configChanges, boolean isTop, boolean andResume, ActivityRecord r) {
        boolean z = false;
        if (isTop || !r.visible) {
            if (ActivityTaskManagerDebugConfig.DEBUG_VISIBILITY) {
                Slog.v(TAG_VISIBILITY, "Start and freeze screen for " + r);
            }
            if (r != starting) {
                r.startFreezingScreenLocked(r.app, configChanges);
            }
            if (!r.visible || r.mLaunchTaskBehind) {
                if (ActivityTaskManagerDebugConfig.DEBUG_VISIBILITY) {
                    Slog.v(TAG_VISIBILITY, "Starting and making visible: " + r);
                }
                r.setVisible(true);
            }
            if (r != starting) {
                ActivityStackSupervisor activityStackSupervisor = this.mStackSupervisor;
                if (andResume && !r.mLaunchTaskBehind) {
                    z = true;
                }
                activityStackSupervisor.startSpecificActivityLocked(r, z, true);
                return true;
            }
        }
        return false;
    }

    private void makeInvisible(ActivityRecord r) {
        if (r.visible) {
            Flog.i((int) TRANSLUCENT_TIMEOUT_MSG, "Making invisible: " + r + " " + r.getState());
            try {
                boolean canEnterPictureInPicture = r.checkEnterPictureInPictureState("makeInvisible", true) && (!HwPCUtils.isPcCastModeInServer() || !HwPCUtils.isPcDynamicStack(r.getStackId()));
                r.setDeferHidingClient(canEnterPictureInPicture && !r.isState(ActivityState.STOPPING, ActivityState.STOPPED, ActivityState.PAUSED));
                r.setVisible(false);
                switch (r.getState()) {
                    case STOPPING:
                    case STOPPED:
                        if (r.attachedToProcess()) {
                            if (ActivityTaskManagerDebugConfig.DEBUG_VISIBILITY) {
                                String str = TAG_VISIBILITY;
                                Slog.v(str, "Scheduling invisibility: " + r);
                            }
                            this.mService.getLifecycleManager().scheduleTransaction(r.app.getThread(), (IBinder) r.appToken, (ClientTransactionItem) WindowVisibilityItem.obtain(false));
                            if (r.isState(ActivityState.STOPPED) && r.mAppWindowToken != null) {
                                r.mAppWindowToken.notifyAppStopped();
                            }
                        }
                        r.supportsEnterPipOnTaskSwitch = false;
                        return;
                    case INITIALIZING:
                    case RESUMED:
                    case PAUSING:
                    case PAUSED:
                        addToStopping(r, true, canEnterPictureInPicture, "makeInvisible");
                        return;
                    default:
                        return;
                }
            } catch (Exception e) {
                Slog.w("ActivityTaskManager", "Exception thrown making hidden: " + r.intent.getComponent(), e);
            }
        } else if (ActivityTaskManagerDebugConfig.DEBUG_VISIBILITY) {
            String str2 = TAG_VISIBILITY;
            Slog.v(str2, "Already invisible: " + r);
        }
    }

    private boolean updateBehindFullscreen(boolean stackInvisible, boolean behindFullscreenActivity, ActivityRecord r) {
        if (!r.fullscreen) {
            return behindFullscreenActivity;
        }
        if (ActivityTaskManagerDebugConfig.DEBUG_VISIBILITY) {
            String str = TAG_VISIBILITY;
            Slog.v(str, "Fullscreen: at " + r + " stackInvisible=" + stackInvisible + " behindFullscreenActivity=" + behindFullscreenActivity);
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public void convertActivityToTranslucent(ActivityRecord r) {
        this.mTranslucentActivityWaiting = r;
        this.mUndrawnActivitiesBelowTopTranslucent.clear();
        this.mHandler.sendEmptyMessageDelayed(TRANSLUCENT_TIMEOUT_MSG, TRANSLUCENT_CONVERSION_TIMEOUT);
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
            this.mHandler.removeMessages(TRANSLUCENT_TIMEOUT_MSG);
            if (waitingActivity != null) {
                boolean z = false;
                this.mWindowManager.setWindowOpaque(waitingActivity.appToken, false);
                if (waitingActivity.attachedToProcess()) {
                    try {
                        IApplicationThread thread = waitingActivity.app.getThread();
                        IApplicationToken.Stub stub = waitingActivity.appToken;
                        if (r != null) {
                            z = true;
                        }
                        thread.scheduleTranslucentConversionComplete(stub, z);
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

    /* JADX INFO: finally extract failed */
    /* access modifiers changed from: package-private */
    @GuardedBy({"mService"})
    public boolean resumeTopActivityUncheckedLocked(ActivityRecord prev, ActivityOptions options) {
        if (this.mInResumeTopActivity) {
            if (ActivityTaskManagerDebugConfig.DEBUG_SWITCH) {
                Flog.i((int) PAUSE_TIMEOUT_MSG, "It is now in resume top activity");
            }
            return false;
        }
        try {
            this.mInResumeTopActivity = true;
            boolean result = resumeTopActivityInnerLocked(prev, options);
            ActivityRecord next = topRunningActivityLocked(true);
            if (next == null || !next.canTurnScreenOn()) {
                checkReadyForSleep();
            }
            this.mInResumeTopActivity = false;
            return result;
        } catch (Throwable th) {
            this.mInResumeTopActivity = false;
            throw th;
        }
    }

    public ActivityRecord getResumedActivity() {
        return this.mResumedActivity;
    }

    private void setResumedActivity(ActivityRecord r, String reason) {
        if (this.mResumedActivity != r) {
            if (ActivityTaskManagerDebugConfig.DEBUG_STACK) {
                Slog.d("ActivityTaskManager", "setResumedActivity stack:" + this + " + from: " + this.mResumedActivity + " to:" + r + " reason:" + reason);
            }
            this.mResumedActivity = r;
            this.mStackSupervisor.updateTopResumedActivityIfNeeded();
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:251:0x04f6  */
    /* JADX WARNING: Removed duplicated region for block: B:252:0x04fa  */
    /* JADX WARNING: Removed duplicated region for block: B:255:0x050d  */
    /* JADX WARNING: Removed duplicated region for block: B:337:0x07b9  */
    /* JADX WARNING: Removed duplicated region for block: B:338:0x07d6  */
    /* JADX WARNING: Removed duplicated region for block: B:341:0x07df  */
    /* JADX WARNING: Removed duplicated region for block: B:344:0x07fc  */
    /* JADX WARNING: Removed duplicated region for block: B:345:0x0801  */
    /* JADX WARNING: Removed duplicated region for block: B:353:0x0819  */
    @GuardedBy({"mService"})
    private boolean resumeTopActivityInnerLocked(ActivityRecord prev, ActivityOptions options) {
        boolean forceResume;
        boolean lastResumedCanPip;
        boolean userLeaving;
        boolean pausing;
        ActivityStack lastFocusedStack;
        String str;
        ActivityRecord activityRecord;
        boolean anim;
        ActivityRecord lastResumedActivity;
        ActivityState lastState;
        boolean notUpdated;
        ActivityRecord lastResumedActivity2;
        ActivityState lastState2;
        ActivityStack lastFocusedStack2;
        ActivityState lastState3;
        boolean z;
        ActivityRecord activityRecord2;
        boolean z2;
        int i;
        ActivityRecord activityRecord3;
        boolean z3;
        if (this.mService.isBooting() || this.mService.isBooted()) {
            ActivityRecord next = topRunningActivityLocked(true);
            boolean hasRunningActivity = next != null;
            if (hasRunningActivity && !isAttached()) {
                return false;
            }
            if (HwPCUtils.enabledInPad() && HwPCUtils.isPcCastModeInServer() && hasRunningActivity && next.task != null) {
                ActivityTaskManagerService activityTaskManagerService = this.mService;
                if (ActivityTaskManagerService.isTimerAlertActivity(next)) {
                    next.task.activityResumedInTop();
                }
            }
            this.mRootActivityContainer.cancelInitializingActivities();
            boolean userLeaving2 = this.mStackSupervisor.mUserLeaving;
            this.mStackSupervisor.mUserLeaving = false;
            if (!hasRunningActivity) {
                Flog.i((int) PAUSE_TIMEOUT_MSG, "No activities left in the stack: " + this);
                return resumeNextFocusableActivityWhenStackIsEmpty(prev, options);
            }
            next.delayedResume = false;
            ActivityDisplay display = getDisplay();
            if (this.mResumedActivity != next || !next.isState(ActivityState.RESUMED) || !display.allResumedActivitiesComplete()) {
                forceResume = false;
            } else {
                boolean forceResume2 = !next.visible && this.mStackSupervisor.mStoppingActivities.contains(next) && !shouldSleepOrShutDownActivities();
                if (!forceResume2) {
                    executeAppTransition(options);
                    if (ActivityTaskManagerDebugConfig.DEBUG_STATES) {
                        Slog.d("ActivityTaskManager", "resumeTopActivityLocked: Top activity resumed " + next);
                    } else {
                        Flog.i((int) PAUSE_TIMEOUT_MSG, "Top activity resumed " + next);
                    }
                    return false;
                }
                Slog.i("ActivityTaskManager", "Force to resuming invisible and resumed activity " + next);
                forceResume = forceResume2;
            }
            if (!next.canResumeByCompat()) {
                return false;
            }
            if (shouldSleepOrShutDownActivities() && this.mLastPausedActivity == next && this.mRootActivityContainer.allPausedActivitiesComplete()) {
                boolean nothingToResume = true;
                if (!this.mService.mShuttingDown) {
                    boolean canShowWhenLocked = !this.mTopActivityOccludesKeyguard && next.canShowWhenLocked();
                    boolean mayDismissKeyguard = (this.mTopDismissingKeyguardActivity == next || next.mAppWindowToken == null || !next.mAppWindowToken.containsDismissKeyguardWindow()) ? false : true;
                    if (canShowWhenLocked || mayDismissKeyguard) {
                        ensureActivitiesVisibleLocked(null, 0, false);
                        nothingToResume = shouldSleepActivities();
                    }
                }
                if (nothingToResume) {
                    executeAppTransition(options);
                    if (ActivityTaskManagerDebugConfig.DEBUG_STATES) {
                        Slog.d("ActivityTaskManager", "resumeTopActivityLocked: Going to sleep and all paused");
                    }
                    return false;
                }
            }
            if (!this.mService.mAmInternal.hasStartedUserState(next.mUserId)) {
                Slog.w("ActivityTaskManager", "Skipping resume of top activity " + next + ": user " + next.mUserId + " is stopped");
                return false;
            }
            if (!HwPCUtils.isHiCarCastMode()) {
                this.mStackSupervisor.mStoppingActivities.remove(next);
                this.mStackSupervisor.mGoingToSleepActivities.remove(next);
                next.sleeping = false;
                if (ActivityTaskManagerDebugConfig.DEBUG_SWITCH) {
                    Slog.v("ActivityTaskManager", "Resuming " + next, new Exception());
                }
                if (!this.mRootActivityContainer.allPausedActivitiesComplete()) {
                    if (ActivityTaskManagerDebugConfig.DEBUG_SWITCH || ActivityTaskManagerDebugConfig.DEBUG_PAUSE || ActivityTaskManagerDebugConfig.DEBUG_STATES) {
                        Slog.v("ActivityTaskManager", "resumeTopActivityLocked: Skip resume: some activity pausing.");
                    }
                    return false;
                }
            } else {
                if (ActivityTaskManagerDebugConfig.DEBUG_SWITCH) {
                    Slog.v("ActivityTaskManager", "Resuming " + next, new Exception());
                }
                if (!this.mRootActivityContainer.allPausedActivitiesComplete()) {
                    if (ActivityTaskManagerDebugConfig.DEBUG_SWITCH || ActivityTaskManagerDebugConfig.DEBUG_PAUSE || ActivityTaskManagerDebugConfig.DEBUG_STATES) {
                        Slog.v("ActivityTaskManager", "resumeTopActivityLocked: Skip resume: some activity pausing.");
                    }
                    return false;
                }
                this.mStackSupervisor.mStoppingActivities.remove(next);
                this.mStackSupervisor.mGoingToSleepActivities.remove(next);
                next.sleeping = false;
            }
            this.mStackSupervisor.setLaunchSource(next.info.applicationInfo.uid);
            ActivityRecord lastResumed = null;
            ActivityStack lastFocusedStack3 = display.getLastFocusedStack();
            if (lastFocusedStack3 == null || lastFocusedStack3 == this) {
                userLeaving = userLeaving2;
                lastResumedCanPip = false;
            } else {
                lastResumed = lastFocusedStack3.mResumedActivity;
                if (userLeaving2 && inMultiWindowMode() && lastFocusedStack3.shouldBeVisible(next)) {
                    if (ActivityTaskManagerDebugConfig.DEBUG_USER_LEAVING) {
                        Slog.i("ActivityTaskManager", "Overriding userLeaving to false next=" + next + " lastResumed=" + lastResumed);
                    }
                    userLeaving2 = false;
                }
                userLeaving = userLeaving2;
                lastResumedCanPip = lastResumed != null && lastResumed.checkEnterPictureInPictureState("resumeTopActivity", userLeaving2);
            }
            boolean resumeWhilePausing = (next.info.flags & 16384) != 0 && !lastResumedCanPip;
            boolean pausing2 = getDisplay().pauseBackStacks(userLeaving, next, false, prev);
            ActivityRecord activityRecord4 = this.mResumedActivity;
            if (activityRecord4 == null || this.mHwActivityStackEx.shouldSkipPausing(activityRecord4, next, this.mStackId) || forceResume) {
                pausing = pausing2;
            } else {
                if (ActivityTaskManagerDebugConfig.DEBUG_STATES) {
                    Slog.d("ActivityTaskManager", "resumeTopActivityLocked: Pausing " + this.mResumedActivity);
                }
                Flog.i((int) PAUSE_TIMEOUT_MSG, "Start pausing " + this.mResumedActivity + " in stack " + this.mStackId);
                pausing = pausing2 | startPausingLocked(userLeaving, false, next, false);
            }
            if (pausing && !resumeWhilePausing) {
                if (ActivityTaskManagerDebugConfig.DEBUG_SWITCH || ActivityTaskManagerDebugConfig.DEBUG_STATES) {
                    Slog.v("ActivityTaskManager", "resumeTopActivityLocked: Skip resume: need to start pausing");
                } else {
                    Flog.i((int) PAUSE_TIMEOUT_MSG, "Skip resume: need to wait pause finished");
                }
                if (next.attachedToProcess()) {
                    z3 = true;
                    next.app.updateProcessInfo(false, true, false);
                } else {
                    z3 = true;
                }
                if (lastResumed != null) {
                    lastResumed.setWillCloseOrEnterPip(z3);
                }
                return z3;
            } else if (this.mResumedActivity != next || !next.isState(ActivityState.RESUMED) || !display.allResumedActivitiesComplete() || forceResume) {
                if (!shouldSleepActivities() || (activityRecord3 = this.mLastNoHistoryActivity) == null || activityRecord3.finishing) {
                    lastFocusedStack = lastFocusedStack3;
                    str = "ActivityTaskManager";
                    activityRecord = null;
                } else {
                    if (ActivityTaskManagerDebugConfig.DEBUG_STATES) {
                        Slog.d("ActivityTaskManager", "no-history finish of " + this.mLastNoHistoryActivity + " on new resume");
                    }
                    lastFocusedStack = lastFocusedStack3;
                    str = "ActivityTaskManager";
                    requestFinishActivityLocked(this.mLastNoHistoryActivity.appToken, 0, null, "resume-no-history", false);
                    activityRecord = null;
                    this.mLastNoHistoryActivity = null;
                }
                if (!(prev == null || prev == next || !next.nowVisible)) {
                    if (prev.finishing) {
                        prev.setVisibility(false);
                        if (ActivityTaskManagerDebugConfig.DEBUG_SWITCH) {
                            Slog.v(str, "Not waiting for visible to hide: " + prev + ", nowVisible=" + next.nowVisible);
                        }
                    } else if (ActivityTaskManagerDebugConfig.DEBUG_SWITCH) {
                        Slog.v(str, "Previous already visible but still waiting to hide: " + prev + ", nowVisible=" + next.nowVisible);
                    }
                }
                try {
                    AppGlobals.getPackageManager().setPackageStoppedState(next.packageName, false, next.mUserId);
                } catch (RemoteException e) {
                } catch (IllegalArgumentException e2) {
                    Slog.w(str, "Failed trying to unstop package " + next.packageName + ": " + e2);
                }
                boolean anim2 = true;
                DisplayContent dc = getDisplay().mDisplayContent;
                int i2 = 6;
                if (prev != null) {
                    if (prev.finishing) {
                        if (ActivityTaskManagerDebugConfig.DEBUG_TRANSITION) {
                            Slog.v(str, "Prepare close transition: prev=" + prev);
                        }
                        if (this.mStackSupervisor.mNoAnimActivities.contains(prev)) {
                            anim2 = false;
                            dc.prepareAppTransition(0, false);
                            z2 = false;
                        } else if (this.mService.mWindowManager.getRecentsAnimationController() == null || prev.task == null || !this.mService.mWindowManager.getRecentsAnimationController().isAnimatingTask(prev.task.mTask)) {
                            if (prev.getTaskRecord() == next.getTaskRecord()) {
                                i = 7;
                            } else {
                                i = 9;
                            }
                            z2 = false;
                            dc.prepareAppTransition(i, false);
                        } else {
                            dc.prepareAppTransition(0, false);
                            Slog.d(str, "Skip TRANSIT_ACTIVITY_CLOSE or TRANSIT_TASK_CLOSE during recents animation");
                            z2 = false;
                        }
                        prev.setVisibility(z2);
                        anim = anim2;
                    } else {
                        if (ActivityTaskManagerDebugConfig.DEBUG_TRANSITION) {
                            Slog.v(str, "Prepare open transition: prev=" + prev);
                        }
                        if (this.mStackSupervisor.mNoAnimActivities.contains(next)) {
                            dc.prepareAppTransition(0, false);
                            anim = false;
                        } else {
                            if (prev.getTaskRecord() != next.getTaskRecord()) {
                                if (next.mLaunchTaskBehind) {
                                    i2 = 16;
                                } else {
                                    i2 = 8;
                                }
                            }
                            dc.prepareAppTransition(i2, false);
                        }
                    }
                    if (anim) {
                        next.applyOptionsLocked();
                    } else {
                        next.clearOptionsLocked();
                    }
                    setKeepPortraitFR();
                    this.mStackSupervisor.mNoAnimActivities.clear();
                    if (next.attachedToProcess()) {
                        if (ActivityTaskManagerDebugConfig.DEBUG_SWITCH) {
                            Slog.v(str, "Resume running: " + next + " stopped=" + next.stopped + " visible=" + next.visible);
                        }
                        boolean lastActivityTranslucent = lastFocusedStack != null && (lastFocusedStack.inMultiWindowMode() || ((activityRecord2 = lastFocusedStack.mLastPausedActivity) != null && !activityRecord2.fullscreen));
                        if (!next.visible || next.stopped || lastActivityTranslucent) {
                            next.setVisibility(true);
                        }
                        next.startLaunchTickingLocked();
                        ActivityRecord lastResumedActivity3 = lastFocusedStack == null ? activityRecord : lastFocusedStack.mResumedActivity;
                        ActivityState lastState4 = next.getState();
                        this.mService.updateCpuStats();
                        if (ActivityTaskManagerDebugConfig.DEBUG_STATES) {
                            Slog.v(str, "Moving to RESUMED: " + next + " (in existing)");
                        }
                        next.setState(ActivityState.RESUMED, "resumeTopActivityInnerLocked");
                        if (!this.mService.getActivityStartController().mCurActivityPkName.equals(next.packageName)) {
                            lastState = lastState4;
                            lastResumedActivity = lastResumedActivity3;
                            Jlog.d(142, next.packageName, next.app.mPid, "");
                            Jlog.warmLaunchingAppBegin(next.packageName, next.launchedFromPackage);
                            LogPower.push(113, next.packageName);
                            this.mService.getActivityStartController().mCurActivityPkName = next.packageName;
                        } else {
                            lastState = lastState4;
                            lastResumedActivity = lastResumedActivity3;
                        }
                        HwLog.dubaie("DUBAI_TAG_FOREGROUND", "mode=" + next.getWindowingMode() + " name=" + next.packageName);
                        next.app.updateProcessInfo(false, true, true);
                        updateLRUListLocked(next);
                        if (shouldBeVisible(next)) {
                            notUpdated = !this.mRootActivityContainer.ensureVisibilityAndConfig(next, this.mDisplayId, true, false);
                        } else {
                            notUpdated = true;
                        }
                        if (notUpdated) {
                            ActivityRecord nextNext = topRunningActivityLocked();
                            if (ActivityTaskManagerDebugConfig.DEBUG_SWITCH || ActivityTaskManagerDebugConfig.DEBUG_STATES) {
                                Slog.i(str, "Activity config changed during resume: " + next + ", new next: " + nextNext);
                            }
                            if (nextNext != next) {
                                this.mStackSupervisor.scheduleResumeTopActivities();
                            }
                            if (!next.visible || next.stopped) {
                                next.setVisibility(true);
                                if (PACKAGE_NAMES_TO_NOTIFY_RESUMED.contains(next.info.name)) {
                                    next.notifyAppResumed(next.stopped);
                                }
                            }
                            next.completeResumeLocked();
                            return true;
                        }
                        try {
                            ClientTransaction transaction = ClientTransaction.obtain(next.app.getThread(), next.appToken);
                            ArrayList<ResultInfo> a = next.results;
                            if (a != null) {
                                try {
                                    int N = a.size();
                                    if (!next.finishing && N > 0) {
                                        if (ActivityTaskManagerDebugConfig.DEBUG_RESULTS) {
                                            Slog.v(str, "Delivering results to " + next + ": " + a);
                                        }
                                        transaction.addCallback(ActivityResultItem.obtain(a));
                                    }
                                } catch (Exception e3) {
                                    lastState2 = lastState;
                                    lastResumedActivity2 = lastResumedActivity;
                                    lastFocusedStack2 = lastFocusedStack;
                                    if (!ActivityTaskManagerDebugConfig.DEBUG_STATES) {
                                    }
                                    next.setState(lastState3, "resumeTopActivityInnerLocked");
                                    if (lastResumedActivity2 != null) {
                                    }
                                    Slog.i(str, "Restarting because process died: " + next);
                                    if (next.hasBeenLaunched) {
                                    }
                                    this.mStackSupervisor.startSpecificActivityLocked(next, true, z);
                                    return true;
                                }
                            }
                            if (next.newIntents != null) {
                                transaction.addCallback(NewIntentItem.obtain(next.newIntents, true));
                            }
                            next.notifyAppResumed(next.stopped);
                            EventLog.writeEvent(30007, Integer.valueOf(next.mUserId), Integer.valueOf(System.identityHashCode(next)), Integer.valueOf(next.getTaskRecord().taskId), next.shortComponentName);
                            next.sleeping = false;
                            this.mService.getAppWarningsLocked().onResumeActivity(next);
                            next.app.setPendingUiCleanAndForceProcessStateUpTo(this.mService.mTopProcessState);
                            next.clearOptionsLocked();
                            this.mService.mHwATMSEx.customActivityResuming(next.packageName);
                            transaction.setLifecycleStateRequest(ResumeActivityItem.obtain(next.app.getReportedProcState(), getDisplay().mDisplayContent.isNextTransitionForward()));
                            this.mService.getLifecycleManager().scheduleTransaction(transaction);
                            if (ActivityTaskManagerDebugConfig.DEBUG_STATES) {
                                Slog.d(str, "resumeTopActivityLocked: Resumed " + next);
                            }
                            try {
                                StringBuilder sb = new StringBuilder();
                                ActivityStackSupervisor activityStackSupervisor = this.mStackSupervisor;
                                sb.append(activityStackSupervisor.mActivityLaunchTrack);
                                sb.append(" resumeTopComplete");
                                activityStackSupervisor.mActivityLaunchTrack = sb.toString();
                                next.completeResumeLocked();
                                return true;
                            } catch (Exception e4) {
                                Slog.w(str, "Exception thrown during resume of " + next, e4);
                                requestFinishActivityLocked(next.appToken, 0, null, "resume-exception", true);
                                return true;
                            }
                        } catch (Exception e5) {
                            lastState2 = lastState;
                            lastResumedActivity2 = lastResumedActivity;
                            lastFocusedStack2 = lastFocusedStack;
                            if (!ActivityTaskManagerDebugConfig.DEBUG_STATES) {
                                StringBuilder sb2 = new StringBuilder();
                                sb2.append("Resume failed; resetting state to ");
                                lastState3 = lastState2;
                                sb2.append(lastState3);
                                sb2.append(": ");
                                sb2.append(next);
                                Slog.v(str, sb2.toString());
                            } else {
                                lastState3 = lastState2;
                            }
                            next.setState(lastState3, "resumeTopActivityInnerLocked");
                            if (lastResumedActivity2 != null) {
                                lastResumedActivity2.setState(ActivityState.RESUMED, "resumeTopActivityInnerLocked");
                            }
                            Slog.i(str, "Restarting because process died: " + next);
                            if (next.hasBeenLaunched) {
                                next.hasBeenLaunched = true;
                                z = false;
                            } else if (lastFocusedStack2 == null) {
                                z = false;
                            } else if (lastFocusedStack2.isTopStackOnDisplay()) {
                                z = false;
                                next.showStartingWindow(null, false, false);
                            } else {
                                z = false;
                            }
                            this.mStackSupervisor.startSpecificActivityLocked(next, true, z);
                            return true;
                        }
                    } else {
                        if (!next.hasBeenLaunched) {
                            next.hasBeenLaunched = true;
                        } else {
                            Flog.i(301, "resumeTopActivity--->>>showStartingWindow for r:" + next);
                            next.showStartingWindow(null, false, false);
                            if (ActivityTaskManagerDebugConfig.DEBUG_SWITCH) {
                                Slog.v(str, "Restarting: " + next);
                            }
                        }
                        if (ActivityTaskManagerDebugConfig.DEBUG_STATES) {
                            Slog.d(str, "No process,need to restart " + next);
                        }
                        this.mStackSupervisor.startSpecificActivityLocked(next, true, true);
                        return true;
                    }
                } else {
                    if (ActivityTaskManagerDebugConfig.DEBUG_TRANSITION) {
                        Slog.v(str, "Prepare open transition: no previous");
                    }
                    if (this.mStackSupervisor.mNoAnimActivities.contains(next)) {
                        dc.prepareAppTransition(0, false);
                        anim = false;
                        if (anim) {
                        }
                        setKeepPortraitFR();
                        this.mStackSupervisor.mNoAnimActivities.clear();
                        if (next.attachedToProcess()) {
                        }
                    } else {
                        dc.prepareAppTransition(6, false);
                    }
                }
                anim = true;
                if (anim) {
                }
                setKeepPortraitFR();
                this.mStackSupervisor.mNoAnimActivities.clear();
                if (next.attachedToProcess()) {
                }
            } else {
                executeAppTransition(options);
                if (!ActivityTaskManagerDebugConfig.DEBUG_STATES) {
                    return true;
                }
                Slog.d("ActivityTaskManager", "resumeTopActivityLocked: Top activity resumed (dontWaitForPause) " + next);
                return true;
            }
        } else {
            Flog.i((int) PAUSE_TIMEOUT_MSG, "It is not ready yet");
            return false;
        }
    }

    private boolean resumeNextFocusableActivityWhenStackIsEmpty(ActivityRecord prev, ActivityOptions options) {
        ActivityStack nextFocusedStack;
        if (!isActivityTypeHome() && (nextFocusedStack = adjustFocusToNextFocusableStack("noMoreActivities")) != null) {
            return this.mRootActivityContainer.resumeFocusedStacksTopActivities(nextFocusedStack, prev, null);
        }
        ActivityOptions.abort(options);
        if (ActivityTaskManagerDebugConfig.DEBUG_STATES) {
            Slog.d("ActivityTaskManager", "resumeNextFocusableActivityWhenStackIsEmpty: noMoreActivities, go home");
        }
        return this.mRootActivityContainer.resumeHomeActivity(prev, "noMoreActivities", this.mDisplayId);
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
            TaskStack taskStack = this.mTaskStack;
            if (taskStack != null) {
                taskStack.positionChildAt(task.getTask(), position2);
            }
            updateTaskMovement(task, true);
        }
    }

    private void insertTaskAtTop(TaskRecord task, ActivityRecord starting) {
        if (this.mTaskHistory.size() > 0) {
            this.mTaskHistory.remove(task);
        }
        this.mTaskHistory.add(getAdjustedPositionForTask(task, this.mTaskHistory.size(), starting), task);
        updateTaskMovement(task, true);
        positionChildWindowContainerAtTop(task);
    }

    private void insertTaskAtBottom(TaskRecord task) {
        if (this.mTaskHistory.size() > 0) {
            this.mTaskHistory.remove(task);
        }
        this.mTaskHistory.add(getAdjustedPositionForTask(task, 0, null), task);
        updateTaskMovement(task, true);
        positionChildWindowContainerAtBottom(task);
    }

    /* access modifiers changed from: package-private */
    public void startActivityLocked(ActivityRecord r, ActivityRecord focusedTopActivity, boolean newTask, boolean keepCurTransition, ActivityOptions options) {
        TaskRecord rTask = r.getTaskRecord();
        int taskId = rTask.taskId;
        if (!r.mLaunchTaskBehind && (taskForIdLocked(taskId) == null || newTask)) {
            insertTaskAtTop(rTask, r);
        }
        TaskRecord task = null;
        if (!newTask) {
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
                            if (ActivityTaskManagerDebugConfig.DEBUG_ADD_REMOVE) {
                                Slog.i("ActivityTaskManager", "Adding activity " + r + " to task " + task, new RuntimeException("here").fillInStackTrace());
                            }
                            r.createAppWindowToken(r.info.navigationHide);
                            ActivityOptions.abort(options);
                            return;
                        }
                    } else if (task.numFullscreen > 0) {
                        Flog.i((int) PAUSE_TIMEOUT_MSG, "starting r: " + r + " blocked by task: " + task);
                        startIt = false;
                    }
                }
                taskNdx--;
            }
        }
        TaskRecord activityTask = r.getTaskRecord();
        if (task == activityTask && this.mTaskHistory.indexOf(task) != this.mTaskHistory.size() - 1) {
            this.mStackSupervisor.mUserLeaving = false;
            if (ActivityTaskManagerDebugConfig.DEBUG_USER_LEAVING) {
                Slog.v("ActivityTaskManager", "startActivity() behind front, mUserLeaving=false");
            }
        }
        if (ActivityTaskManagerDebugConfig.DEBUG_ADD_REMOVE) {
            Slog.i("ActivityTaskManager", "Adding activity " + r + " to stack to task " + activityTask, new RuntimeException("here").fillInStackTrace());
        }
        if (r.mAppWindowToken == null) {
            r.createAppWindowToken(r.info.navigationHide);
        }
        activityTask.setFrontOfTask();
        if (!isHomeOrRecentsStack() || numActivities() > 0) {
            DisplayContent dc = getDisplay().mDisplayContent;
            if (ActivityTaskManagerDebugConfig.DEBUG_TRANSITION) {
                Slog.v("ActivityTaskManager", "Prepare open transition: starting " + r);
            }
            if ((r.intent.getFlags() & 65536) != 0) {
                dc.prepareAppTransition(0, keepCurTransition);
                this.mStackSupervisor.mNoAnimActivities.add(r);
            } else {
                int transit = 6;
                if (newTask) {
                    if (r.mLaunchTaskBehind) {
                        transit = 16;
                    } else {
                        if (canEnterPipOnTaskSwitch(focusedTopActivity, null, r, options)) {
                            focusedTopActivity.supportsEnterPipOnTaskSwitch = true;
                        }
                        transit = 8;
                    }
                }
                if (transit == 6 && this.mService.mWindowManager.getRecentsAnimationController() != null && this.mService.mWindowManager.getRecentsAnimationController().isAnimatingTask(activityTask.mTask)) {
                    transit = 0;
                    Slog.d("ActivityTaskManager", "Skip TRANSIT_ACTIVITY_OPEN during recents animation");
                }
                dc.prepareAppTransition(transit, keepCurTransition);
                this.mStackSupervisor.mNoAnimActivities.remove(r);
            }
            boolean doShow = true;
            if (newTask) {
                if ((r.intent.getFlags() & 2097152) != 0) {
                    resetTaskIfNeededLocked(r, r);
                    doShow = topRunningNonDelayedActivityLocked(null) == r;
                }
            } else if (options != null && options.getAnimationType() == 5) {
                doShow = false;
            }
            if ((r.intent.getHwFlags() & 32768) != 0) {
                doShow = false;
            }
            if (r.mLaunchTaskBehind) {
                r.setVisibility(true);
                ensureActivitiesVisibleLocked(null, 0, false);
            } else if (doShow) {
                TaskRecord prevTask = r.getTaskRecord();
                ActivityRecord prev = prevTask.topRunningActivityWithStartingWindowLocked();
                if (prev != null) {
                    if (prev.getTaskRecord() != prevTask) {
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
                Flog.i(301, "startActivityLocked--->>>showStartingWindow for r:" + r);
                r.showStartingWindow(prev, newTask, isTaskSwitch(r, focusedTopActivity));
            }
        } else {
            ActivityOptions.abort(options);
        }
    }

    private boolean canEnterPipOnTaskSwitch(ActivityRecord pipCandidate, TaskRecord toFrontTask, ActivityRecord toFrontActivity, ActivityOptions opts) {
        if ((opts != null && opts.disallowEnterPictureInPictureWhileLaunching()) || pipCandidate == null || pipCandidate.inPinnedWindowingMode()) {
            return false;
        }
        ActivityStack targetStack = toFrontTask != null ? toFrontTask.getStack() : toFrontActivity.getActivityStack();
        if (targetStack == null || !targetStack.isActivityTypeAssistant()) {
            return true;
        }
        return false;
    }

    private boolean isTaskSwitch(ActivityRecord r, ActivityRecord topFocusedActivity) {
        return (topFocusedActivity == null || r.getTaskRecord() == topFocusedActivity.getTaskRecord()) ? false : true;
    }

    /* JADX INFO: Multiple debug info for r10v6 'target'  com.android.server.wm.ActivityRecord: [D('target' com.android.server.wm.ActivityRecord), D('numActivities' int)] */
    private ActivityOptions resetTargetTaskIfNeededLocked(TaskRecord task, boolean forceReset) {
        int numActivities;
        boolean z;
        int end;
        boolean noOptions;
        ActivityOptions topOptions;
        ActivityRecord target;
        TaskRecord targetTask;
        boolean canMoveOptions;
        ArrayList<ActivityRecord> activities = task.mActivities;
        int numActivities2 = activities.size();
        int rootActivityNdx = task.findEffectiveRootIndex();
        ActivityOptions topOptions2 = null;
        int replyChainEnd = -1;
        boolean canMoveOptions2 = true;
        int i = numActivities2 - 1;
        while (true) {
            if (i <= rootActivityNdx) {
                break;
            }
            ActivityRecord target2 = activities.get(i);
            if (target2.frontOfTask) {
                break;
            }
            int flags = target2.info.flags;
            boolean finishOnTaskLaunch = (flags & 2) != 0;
            boolean allowTaskReparenting = (flags & 64) != 0;
            boolean clearWhenTaskReset = (target2.intent.getFlags() & 524288) != 0;
            if (finishOnTaskLaunch || clearWhenTaskReset || target2.resultTo == null) {
                if (finishOnTaskLaunch || clearWhenTaskReset || !allowTaskReparenting || target2.taskAffinity == null) {
                    numActivities = numActivities2;
                    z = false;
                } else if (!target2.taskAffinity.equals(task.affinity)) {
                    ActivityRecord bottom = (this.mTaskHistory.isEmpty() || this.mTaskHistory.get(0).mActivities.isEmpty()) ? null : this.mTaskHistory.get(0).mActivities.get(0);
                    if (bottom == null || target2.taskAffinity == null || !target2.taskAffinity.equals(bottom.getTaskRecord().affinity)) {
                        numActivities = numActivities2;
                        target = target2;
                        targetTask = createTaskRecord(this.mStackSupervisor.getNextTaskIdForUserLocked(target2.mUserId), target2.info, null, null, null, false);
                        targetTask.affinityIntent = target.intent;
                        Flog.i(105, "ResetTask:Start pushing activity " + target + " out to new task " + targetTask);
                    } else {
                        targetTask = bottom.getTaskRecord();
                        Flog.i(105, "ResetTask:Start pushing activity " + target2 + " out to bottom task " + targetTask);
                        numActivities = numActivities2;
                        target = target2;
                    }
                    boolean noOptions2 = canMoveOptions2;
                    int srcPos = replyChainEnd < 0 ? i : replyChainEnd;
                    while (srcPos >= i) {
                        ActivityRecord p = activities.get(srcPos);
                        if (p.finishing) {
                            canMoveOptions = canMoveOptions2;
                        } else {
                            canMoveOptions = false;
                            if (noOptions2 && topOptions2 == null) {
                                topOptions2 = p.takeOptionsLocked(false);
                                if (topOptions2 != null) {
                                    noOptions2 = false;
                                }
                            }
                            if (ActivityTaskManagerDebugConfig.DEBUG_ADD_REMOVE) {
                                Slog.i("ActivityTaskManager", "Removing activity " + p + " from task=" + task + " adding to task=" + targetTask + " Callers=" + Debug.getCallers(4));
                            }
                            Flog.i(105, "ResetTask:Pushing next activity " + p + " out to target's task " + target);
                            p.reparent(targetTask, 0, "resetTargetTaskIfNeeded");
                        }
                        srcPos--;
                        canMoveOptions2 = canMoveOptions;
                    }
                    positionChildWindowContainerAtBottom(targetTask);
                    replyChainEnd = -1;
                } else {
                    numActivities = numActivities2;
                    z = false;
                }
                if (forceReset || finishOnTaskLaunch || clearWhenTaskReset) {
                    if (clearWhenTaskReset) {
                        end = activities.size() - 1;
                    } else if (replyChainEnd < 0) {
                        end = i;
                    } else {
                        end = replyChainEnd;
                    }
                    boolean noOptions3 = canMoveOptions2;
                    int end2 = end;
                    boolean canMoveOptions3 = canMoveOptions2;
                    int srcPos2 = i;
                    ActivityOptions topOptions3 = topOptions2;
                    while (srcPos2 <= end2) {
                        ActivityRecord p2 = activities.get(srcPos2);
                        if (!p2.finishing) {
                            if (!noOptions3 || topOptions3 != null) {
                                noOptions = noOptions3;
                                topOptions = topOptions3;
                            } else {
                                ActivityOptions topOptions4 = p2.takeOptionsLocked(z);
                                if (topOptions4 != null) {
                                    noOptions = false;
                                    topOptions = topOptions4;
                                } else {
                                    noOptions = noOptions3;
                                    topOptions = topOptions4;
                                }
                            }
                            Flog.i(105, "resetTaskIntendedTask: calling finishActivity on " + p2);
                            if (finishActivityLocked(p2, 0, null, "reset-task", false)) {
                                end2--;
                                srcPos2--;
                                canMoveOptions3 = false;
                                topOptions3 = topOptions;
                                noOptions3 = noOptions;
                            } else {
                                canMoveOptions3 = false;
                                topOptions3 = topOptions;
                                noOptions3 = noOptions;
                            }
                        }
                        srcPos2++;
                    }
                    canMoveOptions2 = canMoveOptions3;
                    replyChainEnd = -1;
                    topOptions2 = topOptions3;
                } else {
                    replyChainEnd = -1;
                }
            } else {
                Flog.i(105, "ResetTask:Keeping the end of the reply chain, target= " + target2.task + " targetI=" + i + " replyChainEnd=" + replyChainEnd);
                if (replyChainEnd < 0) {
                    replyChainEnd = i;
                    numActivities = numActivities2;
                } else {
                    numActivities = numActivities2;
                }
            }
            i--;
            numActivities2 = numActivities;
        }
        return topOptions2;
    }

    private int resetAffinityTaskIfNeededLocked(TaskRecord affinityTask, TaskRecord task, boolean topTaskIsHigher, boolean forceReset, int taskInsertionPoint) {
        String taskAffinity;
        int taskId;
        ArrayList<ActivityRecord> taskActivities;
        int targetNdx;
        int taskInsertionPoint2;
        TaskRecord taskRecord = affinityTask;
        int taskId2 = task.taskId;
        String taskAffinity2 = task.affinity;
        ArrayList<ActivityRecord> activities = taskRecord.mActivities;
        int numActivities = activities.size();
        int rootActivityNdx = affinityTask.findEffectiveRootIndex();
        int i = numActivities - 1;
        int replyChainEnd = -1;
        int taskInsertionPoint3 = taskInsertionPoint;
        while (true) {
            if (i <= rootActivityNdx) {
                break;
            }
            ActivityRecord target = activities.get(i);
            if (target.frontOfTask) {
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
                    taskId = taskId2;
                    taskAffinity = taskAffinity2;
                } else {
                    taskId = taskId2;
                    taskAffinity = taskAffinity2;
                }
            } else if (!topTaskIsHigher || !allowTaskReparenting || taskAffinity2 == null) {
                taskId = taskId2;
                taskAffinity = taskAffinity2;
            } else if (taskAffinity2.equals(target.taskAffinity)) {
                if (forceReset) {
                    taskId = taskId2;
                    taskAffinity = taskAffinity2;
                } else if (finishOnTaskLaunch) {
                    taskId = taskId2;
                    taskAffinity = taskAffinity2;
                } else {
                    if (taskInsertionPoint3 < 0) {
                        taskInsertionPoint3 = task.mActivities.size();
                    }
                    int start = replyChainEnd >= 0 ? replyChainEnd : i;
                    if (ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
                        taskId = taskId2;
                        StringBuilder sb = new StringBuilder();
                        taskAffinity = taskAffinity2;
                        sb.append("Reparenting from task=");
                        sb.append(taskRecord);
                        sb.append(":");
                        sb.append(start);
                        sb.append("-");
                        sb.append(i);
                        sb.append(" to task=");
                        sb.append(task);
                        sb.append(":");
                        sb.append(taskInsertionPoint3);
                        Slog.v("ActivityTaskManager", sb.toString());
                    } else {
                        taskId = taskId2;
                        taskAffinity = taskAffinity2;
                    }
                    int srcPos = start;
                    while (srcPos >= i) {
                        ActivityRecord p = activities.get(srcPos);
                        p.reparent(task, taskInsertionPoint3, "resetAffinityTaskIfNeededLocked");
                        if (ActivityTaskManagerDebugConfig.DEBUG_ADD_REMOVE) {
                            StringBuilder sb2 = new StringBuilder();
                            taskInsertionPoint2 = taskInsertionPoint3;
                            sb2.append("Removing and adding activity ");
                            sb2.append(p);
                            sb2.append(" to stack at ");
                            sb2.append(task);
                            sb2.append(" callers=");
                            sb2.append(Debug.getCallers(3));
                            Slog.i("ActivityTaskManager", sb2.toString());
                        } else {
                            taskInsertionPoint2 = taskInsertionPoint3;
                        }
                        Flog.i(105, "ResetTaskAffinity:Pulling activity " + p + " from " + srcPos + " in to resetting task " + task);
                        srcPos += -1;
                        taskInsertionPoint3 = taskInsertionPoint2;
                    }
                    positionChildWindowContainerAtTop(task);
                    if (target.info.launchMode == 1 && (targetNdx = (taskActivities = task.mActivities).indexOf(target)) > 0) {
                        ActivityRecord p2 = taskActivities.get(targetNdx - 1);
                        if (p2.intent.getComponent().equals(target.intent.getComponent())) {
                            finishActivityLocked(p2, 0, null, "replace", false);
                        }
                    }
                    taskInsertionPoint3 = taskInsertionPoint3;
                    replyChainEnd = -1;
                }
                int start2 = replyChainEnd >= 0 ? replyChainEnd : i;
                if (ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
                    Slog.v("ActivityTaskManager", "Finishing task at index " + start2 + " to " + i);
                }
                for (int srcPos2 = start2; srcPos2 >= i; srcPos2--) {
                    ActivityRecord p3 = activities.get(srcPos2);
                    if (!p3.finishing) {
                        Flog.i(105, "ResetTaskAffinity:finishActivity pos:  " + srcPos2 + " acitivity: " + p3);
                        finishActivityLocked(p3, 0, null, "move-affinity", false);
                    }
                }
                replyChainEnd = -1;
            } else {
                taskId = taskId2;
                taskAffinity = taskAffinity2;
            }
            i--;
            taskRecord = affinityTask;
            taskId2 = taskId;
            taskAffinity2 = taskAffinity;
        }
        return taskInsertionPoint3;
    }

    /* access modifiers changed from: package-private */
    public final ActivityRecord resetTaskIfNeededLocked(ActivityRecord taskTop, ActivityRecord newActivity) {
        boolean forceReset = (newActivity.info.flags & 4) != 0;
        TaskRecord task = taskTop.getTaskRecord();
        boolean taskFound = false;
        ActivityOptions topOptions = null;
        int reparentInsertionPoint = -1;
        for (int i = this.mTaskHistory.size() - 1; i >= 0; i--) {
            TaskRecord targetTask = this.mTaskHistory.get(i);
            if (targetTask == task) {
                taskFound = true;
                topOptions = resetTargetTaskIfNeededLocked(task, forceReset);
            } else {
                reparentInsertionPoint = resetAffinityTaskIfNeededLocked(targetTask, task, taskFound, forceReset, reparentInsertionPoint);
            }
        }
        int taskNdx = this.mTaskHistory.indexOf(task);
        if (taskNdx >= 0) {
            while (true) {
                int taskNdx2 = taskNdx - 1;
                taskTop = this.mTaskHistory.get(taskNdx).getTopActivity();
                if (taskTop != null || taskNdx2 < 0) {
                    break;
                }
                taskNdx = taskNdx2;
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

    /* access modifiers changed from: package-private */
    public void sendActivityResultLocked(int callingUid, ActivityRecord r, String resultWho, int requestCode, int resultCode, Intent data) {
        if (callingUid > 0) {
            this.mService.mUgmInternal.grantUriPermissionFromIntent(callingUid, r.packageName, data, r.getUriPermissionsLocked(), r.mUserId);
        }
        if (ActivityTaskManagerDebugConfig.DEBUG_RESULTS) {
            Slog.v("ActivityTaskManager", "Send activity result to " + r + " : who=" + resultWho + " req=" + requestCode + " res=" + resultCode + " data=" + data);
        }
        if (this.mResumedActivity == r && r.attachedToProcess()) {
            try {
                ArrayList<ResultInfo> list = new ArrayList<>();
                list.add(new ResultInfo(resultWho, requestCode, resultCode, data));
                this.mService.getLifecycleManager().scheduleTransaction(r.app.getThread(), (IBinder) r.appToken, (ClientTransactionItem) ActivityResultItem.obtain(list));
                return;
            } catch (Exception e) {
                Slog.w("ActivityTaskManager", "Exception thrown sending result to " + r, e);
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
        if (r != null && this.mRootActivityContainer.isFocusedStack(this)) {
            ActivityRecord activityRecord = this.mResumedActivity;
            if (activityRecord == r || activityRecord == null) {
                ActivityRecord next = topRunningActivityLocked();
                String myReason = reason + " adjustFocus";
                if (next == r) {
                    ActivityRecord myTop = getDisplay().topRunningActivity();
                    ActivityRecord top = myTop != null ? myTop : this.mRootActivityContainer.topRunningActivity();
                    if (top == r) {
                        Slog.d("ActivityTaskManager", "Ignore adjust for same activity when stop r:" + r);
                    } else if (top != null) {
                        top.moveFocusableActivityToTop(myReason);
                    }
                } else if (next != null && isFocusable()) {
                } else {
                    if (r.getTaskRecord() != null) {
                        ActivityStack nextFocusableStack = adjustFocusToNextFocusableStack(myReason);
                        if (nextFocusableStack != null) {
                            ActivityRecord top2 = nextFocusableStack.topRunningActivityLocked();
                            if (top2 != null && top2 == this.mRootActivityContainer.getTopResumedActivity()) {
                                this.mService.setResumedActivityUncheckLocked(top2, reason);
                                return;
                            }
                            return;
                        }
                        getDisplay().moveHomeActivityToTop(myReason);
                        return;
                    }
                    throw new IllegalStateException("activity no longer associated with task:" + r);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public ActivityStack adjustFocusToNextFocusableStack(String reason) {
        return adjustFocusToNextFocusableStack(reason, false);
    }

    private ActivityStack adjustFocusToNextFocusableStack(String reason, boolean allowFocusSelf) {
        ActivityStack stack = this.mRootActivityContainer.getNextFocusableStack(this, !allowFocusSelf);
        String myReason = reason + " adjustFocusToNextFocusableStack";
        if (stack == null) {
            return null;
        }
        if (!allowFocusSelf && stack.inHwTvFreeFormWindowingMode()) {
            ActivityStack tempStack = null;
            ActivityDisplay display = getDisplay();
            int stackNdx = display.getChildCount() - 1;
            while (true) {
                if (stackNdx < 0) {
                    break;
                }
                ActivityStack activityStack = display.getChildAt(stackNdx);
                if (activityStack.getWindowingMode() == 1) {
                    tempStack = activityStack;
                    break;
                }
                stackNdx--;
            }
            if (tempStack != null) {
                stack = tempStack;
            }
        }
        ActivityRecord top = stack.topRunningActivityLocked();
        ActivityStack targetPrimaryStack = checkAdjustToPrimarySplitScreenStack(stack, top);
        if (targetPrimaryStack != null) {
            Slog.w("ActivityTaskManager", "adjustFocusToNextFocusableStack to primary split screen stack");
            return targetPrimaryStack;
        } else if (!stack.isActivityTypeHome() || (top != null && top.visible)) {
            stack.moveToFront(myReason);
            return stack;
        } else {
            stack.getDisplay().moveHomeActivityToTop(reason);
            return stack;
        }
    }

    private ActivityStack checkAdjustToPrimarySplitScreenStack(ActivityStack targetStack, ActivityRecord targetActivityRecord) {
        ActivityStack targetTemp;
        ActivityStack targetTemp2;
        if (getWindowingMode() == 4 && getActivityType() == 1) {
            if (((targetActivityRecord != null && targetActivityRecord.toString().contains("splitscreen.SplitScreenAppActivity")) || targetStack.getActivityType() == 3) && (targetTemp2 = primarySplitScreenStackToFullScreen(null)) != null) {
                return targetTemp2;
            }
            if (targetStack.getWindowingMode() == 3 && (targetTemp = primarySplitScreenStackToFullScreen(targetStack)) != null) {
                return targetTemp;
            }
        }
        return null;
    }

    private ActivityStack primarySplitScreenStackToFullScreen(ActivityStack topPrimaryStack) {
        if (topPrimaryStack == null) {
            topPrimaryStack = getDisplay().getTopStackInWindowingMode(3);
        }
        if (topPrimaryStack == null) {
            return null;
        }
        WindowManagerService windowManagerService = this.mWindowManager;
        windowManagerService.mShouldResetTime = true;
        windowManagerService.startFreezingScreen(0, 0);
        topPrimaryStack.moveToFront("adjustFocusedToSplitPrimaryStack");
        if (!this.mService.mHwATMSEx.isSwitchToMagicWin(topPrimaryStack.getStackId(), false, getConfiguration().orientation)) {
            topPrimaryStack.setWindowingMode(1);
        }
        this.mWindowManager.stopFreezingScreen();
        return topPrimaryStack;
    }

    /* access modifiers changed from: package-private */
    public final void stopActivityLocked(ActivityRecord r) {
        if (ActivityTaskManagerDebugConfig.DEBUG_SWITCH) {
            Slog.d("ActivityTaskManager", "Stopping: " + r);
        }
        if (!((r.intent.getFlags() & 1073741824) == 0 && (r.info.flags & 128) == 0) && !r.finishing) {
            if (!shouldSleepActivities()) {
                if (ActivityTaskManagerDebugConfig.DEBUG_STATES) {
                    Slog.d("ActivityTaskManager", "no-history finish of " + r);
                }
                if (requestFinishActivityLocked(r.appToken, 0, null, "stop-no-history", false)) {
                    r.resumeKeyDispatchingLocked();
                    return;
                }
            } else if (ActivityTaskManagerDebugConfig.DEBUG_STATES) {
                Slog.d("ActivityTaskManager", "Not finishing noHistory " + r + " on stop because we're just sleeping");
            }
        }
        if (r.attachedToProcess()) {
            adjustFocusedActivityStack(r, "stopActivity");
            r.resumeKeyDispatchingLocked();
            try {
                r.stopped = false;
                if (ActivityTaskManagerDebugConfig.DEBUG_STATES) {
                    Slog.v("ActivityTaskManager", "Moving to STOPPING: " + r + " (stop requested)");
                }
                r.setState(ActivityState.STOPPING, "stopActivityLocked");
                if (ActivityTaskManagerDebugConfig.DEBUG_VISIBILITY) {
                    String str = TAG_VISIBILITY;
                    Slog.v(str, "Stopping visible=" + r.visible + " for " + r);
                }
                if (!r.visible) {
                    r.setVisible(false);
                }
                EventLogTags.writeAmStopActivity(r.mUserId, System.identityHashCode(r), r.shortComponentName);
                this.mService.getLifecycleManager().scheduleTransaction(r.app.getThread(), (IBinder) r.appToken, (ActivityLifecycleItem) StopActivityItem.obtain(r.visible, r.configChangeFlags));
                if (shouldSleepOrShutDownActivities()) {
                    r.setSleeping(true);
                }
                this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(104, r), 11000);
            } catch (Exception e) {
                Slog.w("ActivityTaskManager", "Exception thrown during pause", e);
                r.stopped = true;
                if (ActivityTaskManagerDebugConfig.DEBUG_STATES) {
                    Slog.v("ActivityTaskManager", "Stop failed; moving to STOPPED: " + r);
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
        if (ActivityTaskManagerDebugConfig.DEBUG_RESULTS || ActivityTaskManagerDebugConfig.DEBUG_STATES || HwSlog.HW_DEBUG_STATES) {
            Slog.v("ActivityTaskManager", "Finishing activity token=" + token + " r=, result=" + resultCode + ", data=" + resultData + ", reason=" + reason);
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
        this.mService.updateOomAdj();
    }

    /* access modifiers changed from: package-private */
    public final TaskRecord finishTopCrashedActivityLocked(WindowProcessController app, String reason) {
        ActivityRecord r = topRunningActivityLocked();
        if (r == null) {
            return null;
        }
        if (r.app != app) {
            return null;
        }
        Slog.w("ActivityTaskManager", "  finishTopCrashedActivityLocked Force finishing activity " + r.intent.getComponent().flattenToShortString());
        TaskRecord finishedTask = r.getTaskRecord();
        int taskNdx = this.mTaskHistory.indexOf(finishedTask);
        int activityNdx = finishedTask.mActivities.indexOf(r);
        getDisplay().mDisplayContent.prepareAppTransition(26, false);
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
                Slog.w("ActivityTaskManager", "  finishTopCrashedActivityLocked non_home Force finishing activity " + r2.intent.getComponent().flattenToShortString());
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
                            r.app.getThread().scheduleLocalVoiceInteractionStarted(r.appToken, (IVoiceInteractor) null);
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
            this.mService.updateOomAdj();
        }
    }

    /* access modifiers changed from: package-private */
    public final boolean finishActivityAffinityLocked(ActivityRecord r) {
        ArrayList<ActivityRecord> activities = r.getTaskRecord().mActivities;
        for (int index = activities.indexOf(r); index >= 0; index--) {
            ActivityRecord cur = activities.get(index);
            if (!Objects.equals(cur.taskAffinity, r.taskAffinity)) {
                return true;
            }
            finishActivityLocked(cur, 0, null, "request-affinity", true);
        }
        return true;
    }

    private void finishActivityResultsLocked(ActivityRecord r, int resultCode, Intent resultData) {
        ActivityRecord resultTo = r.resultTo;
        if (resultTo != null) {
            if (ActivityTaskManagerDebugConfig.DEBUG_RESULTS) {
                Slog.v("ActivityTaskManager", "Adding result to " + resultTo + " who=" + r.resultWho + " req=" + r.requestCode + " res=" + resultCode + " data=" + resultData);
            }
            if (!(resultTo.mUserId == r.mUserId || resultData == null)) {
                resultData.prepareToLeaveUser(r.mUserId);
            }
            if (r.info.applicationInfo.uid > 0) {
                this.mService.mUgmInternal.grantUriPermissionFromIntent(r.info.applicationInfo.uid, resultTo.packageName, resultData, resultTo.getUriPermissionsLocked(), resultTo.mUserId);
            }
            resultTo.addResultLocked(r, r.resultWho, r.requestCode, resultCode, resultData);
            r.resultTo = null;
        } else if (ActivityTaskManagerDebugConfig.DEBUG_RESULTS) {
            Slog.v("ActivityTaskManager", "No result destination from " + r);
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
    public final boolean finishActivityLocked(ActivityRecord r, int resultCode, Intent resultData, String reason, boolean oomAdj, boolean pauseImmediately) {
        Throwable th;
        boolean removedActivity = false;
        if (r.finishing) {
            Slog.w("ActivityTaskManager", "Duplicate finish request for " + r);
            return false;
        }
        int finishMode = 2;
        if (HwMwUtils.ENABLED) {
            HwMwUtils.performPolicy(52, new Object[]{r.appToken, false, null, r.getRequestedOverrideBounds(), reason});
        }
        this.mWindowManager.deferSurfaceLayout();
        try {
            r.makeFinishingLocked();
            TaskRecord task = r.getTaskRecord();
            if (task == null) {
                Slog.w("ActivityTaskManager", "finishActivityLocked: r.getTask is null!");
                this.mWindowManager.continueSurfaceLayout();
                return false;
            }
            EventLog.writeEvent(30001, Integer.valueOf(r.mUserId), Integer.valueOf(System.identityHashCode(r)), Integer.valueOf(task.taskId), r.shortComponentName, reason);
            ArrayList<ActivityRecord> activities = task.mActivities;
            int index = activities.indexOf(r);
            if (index < activities.size() - 1) {
                task.setFrontOfTask();
                if ((r.intent.getFlags() & 524288) != 0) {
                    activities.get(index + 1).intent.addFlags(524288);
                }
            }
            r.pauseKeyDispatchingLocked();
            adjustFocusedActivityStack(r, "finishActivity");
            finishActivityResultsLocked(r, resultCode, resultData);
            boolean endTask = index <= 0 && !task.isClearingToReuseTask();
            int transit = endTask ? 9 : 7;
            if (this.mResumedActivity == r) {
                try {
                    if (ActivityTaskManagerDebugConfig.DEBUG_VISIBILITY || ActivityTaskManagerDebugConfig.DEBUG_TRANSITION) {
                        Slog.v("ActivityTaskManager", "Prepare close transition: finishing " + r);
                    }
                    if (endTask) {
                        this.mService.getTaskChangeNotificationController().notifyTaskRemovalStarted(task.getTaskInfo());
                    }
                    getDisplay().mDisplayContent.prepareAppTransition(transit, false);
                    r.setVisibility(false);
                    if (this.mPausingActivity == null) {
                        if (ActivityTaskManagerDebugConfig.DEBUG_PAUSE) {
                            Slog.v("ActivityTaskManager", "Finish needs to pause: " + r);
                        }
                        if (ActivityTaskManagerDebugConfig.DEBUG_USER_LEAVING) {
                            Slog.v("ActivityTaskManager", "finish() => pause with userLeaving=false");
                        }
                        startPausingLocked(false, false, null, pauseImmediately);
                    }
                    if (endTask) {
                        this.mService.getLockTaskController().clearLockedTask(task);
                    }
                } catch (Throwable th2) {
                    th = th2;
                    this.mWindowManager.continueSurfaceLayout();
                    throw th;
                }
            } else if (!r.isState(ActivityState.PAUSING)) {
                if (ActivityTaskManagerDebugConfig.DEBUG_PAUSE) {
                    Slog.v("ActivityTaskManager", "Finish not pausing: " + r);
                }
                if (HwMwUtils.ENABLED && r.inHwMagicWindowingMode() && transit == 9) {
                    transit = 7;
                }
                if (r.visible && !r.isSplitMode()) {
                    prepareActivityHideTransitionAnimation(r, transit);
                }
                if (!r.visible) {
                    if (!r.nowVisible) {
                        finishMode = 1;
                    }
                }
                try {
                    if (finishCurrentActivityLocked(r, finishMode, oomAdj, "finishActivityLocked") == null) {
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
                } catch (Throwable th3) {
                    th = th3;
                    this.mWindowManager.continueSurfaceLayout();
                    throw th;
                }
            } else if (ActivityTaskManagerDebugConfig.DEBUG_PAUSE) {
                Slog.v("ActivityTaskManager", "Finish waiting for pause of: " + r);
            }
            this.mWindowManager.continueSurfaceLayout();
            return false;
        } catch (Throwable th4) {
            th = th4;
            this.mWindowManager.continueSurfaceLayout();
            throw th;
        }
    }

    private void prepareActivityHideTransitionAnimation(ActivityRecord r, int transit) {
        DisplayContent dc = getDisplay().mDisplayContent;
        dc.prepareAppTransition(transit, false);
        r.setVisibility(false);
        dc.executeAppTransition();
    }

    /* access modifiers changed from: package-private */
    public final ActivityRecord finishCurrentActivityLocked(ActivityRecord r, int mode, boolean oomAdj, String reason) {
        ActivityDisplay display = getDisplay();
        ActivityRecord next = display.topRunningActivity(true);
        boolean isFloating = r.getConfiguration().windowConfiguration.tasksAreFloating();
        if (mode != 2 || ((!r.visible && !r.nowVisible) || ((next == null || ((next.nowVisible && (!next.inHwFreeFormWindowingMode() || r.inHwFreeFormWindowingMode())) || isFloating)) && (!HwPCUtils.isPcCastModeInServer() || !HwPCUtils.isPcDynamicStack(r.getStackId()))))) {
            this.mStackSupervisor.mStoppingActivities.remove(r);
            this.mStackSupervisor.mGoingToSleepActivities.remove(r);
            ActivityState prevState = r.getState();
            if (ActivityTaskManagerDebugConfig.DEBUG_STATES) {
                Slog.v("ActivityTaskManager", "Moving to FINISHING: " + r);
            }
            r.setState(ActivityState.FINISHING, "finishCurrentActivityLocked");
            boolean finishingInNonFocusedStackOrNoRunning = mode == 2 && prevState == ActivityState.PAUSED && ((r.getActivityStack() != display.getFocusedStack()) || (next == null && display.topRunningActivity() == null && display.getHomeStack() == null) || r.inSplitScreenPrimaryWindowingMode());
            boolean finishingLastActivityInFreeformStack = prevState == ActivityState.PAUSED && mode == 2 && inFreeformWindowingMode() && !(next != null && next.inFreeformWindowingMode());
            if (mode == 0 || ((prevState == ActivityState.PAUSED && (mode == 1 || inPinnedWindowingMode())) || finishingInNonFocusedStackOrNoRunning || finishingLastActivityInFreeformStack || prevState == ActivityState.STOPPING || prevState == ActivityState.STOPPED || prevState == ActivityState.INITIALIZING || isFinishImmediatelyInSplit(r, next))) {
                r.makeFinishingLocked();
                boolean activityRemoved = destroyActivityLocked(r, true, "finish-imm:" + reason);
                if (finishingInNonFocusedStackOrNoRunning) {
                    int i = this.mDisplayId;
                    if (i != -1) {
                        this.mRootActivityContainer.ensureVisibilityAndConfig(next, i, false, true);
                        Flog.i((int) PAUSE_TIMEOUT_MSG, "Moving to FINISHING r=" + r + " destroy returned removed=" + activityRemoved);
                    }
                }
                if (activityRemoved) {
                    this.mStackSupervisor.mActivityLaunchTrack = "finishImmAtivityRemoved";
                    this.mRootActivityContainer.resumeFocusedStacksTopActivities();
                }
                if (ActivityTaskManagerDebugConfig.DEBUG_CONTAINERS) {
                    Slog.d("ActivityTaskManager", "destroyActivityLocked: finishCurrentActivityLocked r=" + r + " destroy returned removed=" + activityRemoved);
                }
                if (activityRemoved) {
                    return null;
                }
                return r;
            }
            if (ActivityTaskManagerDebugConfig.DEBUG_ALL) {
                Slog.v("ActivityTaskManager", "Enqueueing pending finish: " + r);
            }
            this.mStackSupervisor.mFinishingActivities.add(r);
            r.resumeKeyDispatchingLocked();
            this.mStackSupervisor.mActivityLaunchTrack = "enqueueFinishResume";
            this.mRootActivityContainer.resumeFocusedStacksTopActivities();
            if (r.isState(ActivityState.RESUMED) && this.mPausingActivity != null) {
                startPausingLocked(false, false, next, false);
            }
            return r;
        }
        if (!this.mStackSupervisor.mStoppingActivities.contains(r)) {
            addToStopping(r, false, false, "finishCurrentActivityLocked");
        }
        Flog.i((int) PAUSE_TIMEOUT_MSG, "Moving to STOPPING: " + r + " (finish requested)");
        r.setState(ActivityState.STOPPING, "finishCurrentActivityLocked");
        if (oomAdj) {
            this.mService.updateOomAdj();
        }
        return r;
    }

    private boolean isFinishImmediatelyInSplit(ActivityRecord r, ActivityRecord next) {
        DisplayContent dc;
        if (!r.isSplitMode() || next == null || !next.nowVisible || (dc = this.mWindowManager.mRoot.getDisplayContent(r.getDisplayId())) == null || dc.mInputMethodWindow == null) {
            return false;
        }
        return !dc.mInputMethodWindow.isVisible();
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
                    Slog.d("ActivityTaskManager", "finishAllActivitiesLocked: finishing " + r + " immediately");
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
        int index;
        ActivityDisplay display = getDisplay();
        if (display == null || (index = display.getIndexOf(this)) == 0) {
            return false;
        }
        return display.getChildAt(index - 1).isActivityTypeStandard();
    }

    /* access modifiers changed from: package-private */
    public boolean shouldUpRecreateTaskLocked(ActivityRecord srec, String destAffinity) {
        if (srec == null || srec.getTaskRecord().affinity == null || !srec.getTaskRecord().affinity.equals(destAffinity)) {
            return true;
        }
        TaskRecord task = srec.getTaskRecord();
        if (srec.frontOfTask && task.getBaseIntent() != null && task.getBaseIntent().isDocument()) {
            if (!inFrontOfStandardStack()) {
                return true;
            }
            int taskIdx = this.mTaskHistory.indexOf(task);
            if (taskIdx <= 0) {
                Slog.w("ActivityTaskManager", "shouldUpRecreateTask: task not in history for " + srec);
                return false;
            } else if (!task.affinity.equals(this.mTaskHistory.get(taskIdx).affinity)) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x0093 A[RETURN] */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x009f A[LOOP:1: B:35:0x009d->B:36:0x009f, LOOP_END] */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x00e9  */
    public final boolean navigateUpToLocked(ActivityRecord srec, Intent destIntent, int resultCode, Intent resultData) {
        boolean foundParentInTask;
        ActivityRecord parent;
        int finishTo;
        IActivityController controller;
        int i;
        int parentLaunchMode;
        int callingUid;
        ActivityRecord next;
        boolean resumeOK;
        if (!srec.attachedToProcess()) {
            return false;
        }
        TaskRecord task = srec.getTaskRecord();
        ArrayList<ActivityRecord> activities = task.mActivities;
        int start = activities.indexOf(srec);
        if (!this.mTaskHistory.contains(task) || start < 0) {
            return false;
        }
        int finishTo2 = start - 1;
        ActivityRecord parent2 = finishTo2 < 0 ? null : activities.get(finishTo2);
        ComponentName dest = destIntent.getComponent();
        if (start > 0 && dest != null) {
            int i2 = finishTo2;
            while (true) {
                if (i2 < 0) {
                    break;
                }
                ActivityRecord r = activities.get(i2);
                if (r.info.packageName.equals(dest.getPackageName()) && r.info.name.equals(dest.getClassName())) {
                    finishTo = i2;
                    parent = r;
                    foundParentInTask = true;
                    break;
                }
                i2--;
            }
            controller = this.mService.mController;
            if (!(controller == null || (next = topRunningActivityLocked(srec.appToken, 0)) == null)) {
                resumeOK = true;
                resumeOK = controller.activityResuming(next.packageName);
                if (!resumeOK) {
                    return false;
                }
            }
            long origId = Binder.clearCallingIdentity();
            i = start;
            int resultCode2 = resultCode;
            Intent resultData2 = resultData;
            while (i > finishTo) {
                requestFinishActivityLocked(activities.get(i).appToken, resultCode2, resultData2, "navigate-up", true);
                resultCode2 = 0;
                resultData2 = null;
                i--;
                finishTo = finishTo;
                controller = controller;
            }
            if (parent != null && foundParentInTask) {
                int callingUid2 = srec.info.applicationInfo.uid;
                parentLaunchMode = parent.info.launchMode;
                int destIntentFlags = destIntent.getFlags();
                if (!(parentLaunchMode == 3 || parentLaunchMode == 2)) {
                    boolean foundParentInTask2 = true;
                    if (parentLaunchMode != 1) {
                        if ((destIntentFlags & 67108864) != 0) {
                            callingUid = callingUid2;
                            parent.deliverNewIntentLocked(callingUid, destIntent, srec.packageName);
                        } else {
                            try {
                                if (this.mService.getActivityStartController().obtainStarter(destIntent, "navigateUpTo").setCaller(srec.app.getThread()).setActivityInfo(AppGlobals.getPackageManager().getActivityInfo(destIntent.getComponent(), 1024, srec.mUserId)).setResultTo(parent.appToken).setCallingPid(-1).setCallingUid(callingUid2).setCallingPackage(srec.packageName).setRealCallingPid(-1).setRealCallingUid(callingUid2).setComponentSpecified(true).execute() != 0) {
                                    foundParentInTask2 = false;
                                }
                                foundParentInTask = foundParentInTask2;
                            } catch (RemoteException e) {
                                foundParentInTask = false;
                            }
                            requestFinishActivityLocked(parent.appToken, resultCode2, resultData2, "navigate-top", true);
                        }
                    }
                }
                callingUid = callingUid2;
                parent.deliverNewIntentLocked(callingUid, destIntent, srec.packageName);
            }
            Binder.restoreCallingIdentity(origId);
            return foundParentInTask;
        }
        finishTo = finishTo2;
        parent = parent2;
        foundParentInTask = false;
        controller = this.mService.mController;
        resumeOK = true;
        try {
            resumeOK = controller.activityResuming(next.packageName);
        } catch (RemoteException e2) {
            this.mService.mController = null;
            Watchdog.getInstance().setActivityController((IActivityController) null);
        }
        if (!resumeOK) {
        }
        long origId2 = Binder.clearCallingIdentity();
        i = start;
        int resultCode22 = resultCode;
        Intent resultData22 = resultData;
        while (i > finishTo) {
        }
        int callingUid22 = srec.info.applicationInfo.uid;
        parentLaunchMode = parent.info.launchMode;
        int destIntentFlags2 = destIntent.getFlags();
        boolean foundParentInTask22 = true;
        if (parentLaunchMode != 1) {
        }
        callingUid = callingUid22;
        parent.deliverNewIntentLocked(callingUid, destIntent, srec.packageName);
        Binder.restoreCallingIdentity(origId2);
        return foundParentInTask;
    }

    /* access modifiers changed from: package-private */
    public void onActivityRemovedFromStack(ActivityRecord r) {
        removeTimeoutsForActivityLocked(r);
        ActivityRecord activityRecord = this.mResumedActivity;
        if (activityRecord != null && activityRecord == r) {
            setResumedActivity(null, "onActivityRemovedFromStack");
        }
        ActivityRecord activityRecord2 = this.mPausingActivity;
        if (activityRecord2 != null && activityRecord2 == r) {
            Flog.i((int) PAUSE_TIMEOUT_MSG, "Remove the pausingActivity " + this.mPausingActivity + " in stack " + this.mStackId);
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
            if (ActivityTaskManagerDebugConfig.DEBUG_STATES) {
                Slog.v("ActivityTaskManager", "Moving to DESTROYED: " + r + " (cleaning up)");
            }
            r.setState(ActivityState.DESTROYED, "cleanupActivityLocked");
            if (ActivityTaskManagerDebugConfig.DEBUG_APP) {
                Slog.v("ActivityTaskManager", "Clearing app during cleanUp for activity " + r);
            }
            r.app = null;
        }
        this.mStackSupervisor.cleanupActivity(r);
        if (r.finishing && r.pendingResults != null) {
            Iterator<WeakReference<PendingIntentRecord>> it = r.pendingResults.iterator();
            while (it.hasNext()) {
                PendingIntentRecord rec = it.next().get();
                if (rec != null) {
                    this.mService.mPendingIntentController.cancelIntentSender(rec, false);
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

    private void removeTimeoutsForActivityLocked(ActivityRecord r) {
        if (r != null) {
            this.mStackSupervisor.removeTimeoutsForActivityLocked(r);
            this.mHandler.removeMessages(PAUSE_TIMEOUT_MSG, r);
            this.mHandler.removeMessages(104, r);
            this.mHandler.removeMessages(102, r);
            r.finishLaunchTickingLocked();
        }
    }

    private void removeActivityFromHistoryLocked(ActivityRecord r, String reason) {
        finishActivityResultsLocked(r, 0, null);
        r.makeFinishingLocked();
        if (ActivityTaskManagerDebugConfig.DEBUG_ADD_REMOVE) {
            Slog.i("ActivityTaskManager", "Removing activity " + r + " from stack callers=" + Debug.getCallers(5));
        }
        r.takeFromHistory();
        removeTimeoutsForActivityLocked(r);
        if (ActivityTaskManagerDebugConfig.DEBUG_STATES || HwSlog.HW_DEBUG_STATES) {
            Slog.v("ActivityTaskManager", "Moving to DESTROYED: " + r + " (removed from history)");
        }
        r.setState(ActivityState.DESTROYED, "removeActivityFromHistoryLocked");
        if (ActivityTaskManagerDebugConfig.DEBUG_APP) {
            Slog.v("ActivityTaskManager", "Clearing app during remove for activity " + r);
        }
        r.app = null;
        r.removeWindowContainer();
        TaskRecord task = r.getTaskRecord();
        boolean lastActivity = task != null ? task.removeActivity(r) : false;
        boolean onlyHasTaskOverlays = task != null ? task.onlyHasTaskOverlayActivities(false) : false;
        if (lastActivity || onlyHasTaskOverlays) {
            if (ActivityTaskManagerDebugConfig.DEBUG_STACK) {
                Slog.i("ActivityTaskManager", "removeActivityFromHistoryLocked: last activity removed from " + this + " onlyHasTaskOverlays=" + onlyHasTaskOverlays);
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
        if (r.mServiceConnectionsHolder != null) {
            r.mServiceConnectionsHolder.disconnectActivityFromServices();
        }
    }

    /* access modifiers changed from: package-private */
    public final void scheduleDestroyActivities(WindowProcessController owner, String reason) {
        Message msg = this.mHandler.obtainMessage(105);
        msg.obj = new ScheduleDestroyArgs(owner, reason);
        this.mHandler.sendMessage(msg);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void destroyActivitiesLocked(WindowProcessController owner, String reason) {
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
                        if (ActivityTaskManagerDebugConfig.DEBUG_SWITCH) {
                            Slog.v("ActivityTaskManager", "Destroying " + r + " in state " + r.getState() + " resumed=" + this.mResumedActivity + " pausing=" + this.mPausingActivity + " for reason " + reason);
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
            this.mRootActivityContainer.resumeFocusedStacksTopActivities();
        }
    }

    /* access modifiers changed from: package-private */
    public final boolean safelyDestroyActivityLocked(ActivityRecord r, String reason) {
        if (!r.isDestroyable()) {
            return false;
        }
        if (ActivityTaskManagerDebugConfig.DEBUG_SWITCH) {
            Slog.v("ActivityTaskManager", "Destroying " + r + " in state " + r.getState() + " resumed=" + this.mResumedActivity + " pausing=" + this.mPausingActivity + " for reason " + reason);
        }
        return destroyActivityLocked(r, true, reason);
    }

    /* access modifiers changed from: package-private */
    public final int releaseSomeActivitiesLocked(WindowProcessController app, ArraySet<TaskRecord> tasks, String reason) {
        if (ActivityTaskManagerDebugConfig.DEBUG_RELEASE) {
            Slog.d("ActivityTaskManager", "Trying to release some activities in " + app);
        }
        int maxTasks = tasks.size() / 4;
        if (maxTasks < 1) {
            maxTasks = 1;
        }
        int numReleased = 0;
        int taskNdx = 0;
        while (taskNdx < this.mTaskHistory.size() && maxTasks > 0) {
            TaskRecord task = this.mTaskHistory.get(taskNdx);
            if (tasks.contains(task)) {
                if (ActivityTaskManagerDebugConfig.DEBUG_RELEASE) {
                    Slog.d("ActivityTaskManager", "Looking for activities to release in " + task);
                }
                int curNum = 0;
                ArrayList<ActivityRecord> activities = task.mActivities;
                int actNdx = 0;
                while (actNdx < activities.size()) {
                    ActivityRecord activity = activities.get(actNdx);
                    if (activity.app == app && activity.isDestroyable()) {
                        if (ActivityTaskManagerDebugConfig.DEBUG_RELEASE) {
                            Slog.v("ActivityTaskManager", "Destroying " + activity + " in state " + activity.getState() + " resumed=" + this.mResumedActivity + " pausing=" + this.mPausingActivity + " for reason " + reason);
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
        if (ActivityTaskManagerDebugConfig.DEBUG_RELEASE) {
            Slog.d("ActivityTaskManager", "Done releasing: did " + numReleased + " activities");
        }
        return numReleased;
    }

    /* JADX INFO: Multiple debug info for r0v24 boolean: [D('e' java.lang.Exception), D('wasNowVisible' boolean)] */
    /* access modifiers changed from: package-private */
    public final boolean destroyActivityLocked(ActivityRecord r, boolean removeFromApp, String reason) {
        if (ActivityTaskManagerDebugConfig.DEBUG_SWITCH || ActivityTaskManagerDebugConfig.DEBUG_CLEANUP) {
            StringBuilder sb = new StringBuilder();
            sb.append("Removing activity from ");
            sb.append(reason);
            sb.append(": token=");
            sb.append(r);
            sb.append(", app=");
            sb.append(r.hasProcess() ? r.app.mName : "(null)");
            Slog.v("ActivityTaskManager", sb.toString());
        }
        if (r.isState(ActivityState.DESTROYING, ActivityState.DESTROYED)) {
            if (ActivityTaskManagerDebugConfig.DEBUG_STATES) {
                Slog.v("ActivityTaskManager", "activity " + r + " already destroying.skipping request with reason:" + reason);
            }
            return false;
        }
        this.mService.mHwATMSEx.removeFreeformBallWhenDestroy(r, r.getTaskRecord());
        EventLog.writeEvent(30018, Integer.valueOf(r.mUserId), Integer.valueOf(System.identityHashCode(r)), Integer.valueOf(r.getTaskRecord().taskId), r.shortComponentName, reason);
        this.mService.notifyActivityState(r, ActivityState.DESTROYED);
        boolean removedFromHistory = false;
        cleanUpActivityLocked(r, false, false);
        if (ActivityTaskManagerDebugConfig.DEBUG_SWITCH) {
            Slog.i("ActivityTaskManager", "Activity has been cleaned up!");
        }
        if (r.app != null) {
            this.mService.mAtmDAProxy.recognizeFakeActivity(r.shortComponentName, r.app.mPid, r.app.mUserId);
        }
        boolean hadApp = r.hasProcess();
        if (hadApp) {
            if (removeFromApp) {
                r.app.removeActivity(r);
                if (!r.app.hasActivities()) {
                    this.mService.clearHeavyWeightProcessIfEquals(r.app);
                }
                if (!r.app.hasActivities()) {
                    r.app.updateProcessInfo(true, false, true);
                }
            }
            boolean skipDestroy = false;
            try {
                if (ActivityTaskManagerDebugConfig.DEBUG_SWITCH) {
                    Slog.i("ActivityTaskManager", "Destroying: " + r);
                }
                this.mService.getLifecycleManager().scheduleTransaction(r.app.getThread(), (IBinder) r.appToken, (ActivityLifecycleItem) DestroyActivityItem.obtain(r.finishing, r.configChangeFlags));
            } catch (Exception e) {
                if (r.finishing) {
                    removeActivityFromHistoryLocked(r, reason + " exceptionInScheduleDestroy");
                    removedFromHistory = true;
                    skipDestroy = true;
                }
            }
            boolean wasNowVisible = r.nowVisible;
            r.nowVisible = false;
            if (wasNowVisible) {
                this.mService.mHwATMSEx.reportAppWindowVisibleOrGone(r);
            }
            if (!r.finishing || skipDestroy) {
                if (ActivityTaskManagerDebugConfig.DEBUG_STATES) {
                    Slog.v("ActivityTaskManager", "Moving to DESTROYED: " + r + " (destroy skipped) in stack " + this.mStackId);
                }
                r.setState(ActivityState.DESTROYED, "destroyActivityLocked. not finishing or skipping destroy");
                if (ActivityTaskManagerDebugConfig.DEBUG_APP) {
                    Slog.v("ActivityTaskManager", "Clearing app during destroy for activity " + r);
                }
                r.app = null;
            } else {
                if (ActivityTaskManagerDebugConfig.DEBUG_STATES) {
                    Slog.v("ActivityTaskManager", "Moving to DESTROYING: " + r + " (destroy requested) in stack " + this.mStackId);
                }
                r.setState(ActivityState.DESTROYING, "destroyActivityLocked. finishing and not skipping destroy");
                this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(102, r), 10000);
            }
        } else if (r.finishing) {
            removeActivityFromHistoryLocked(r, reason + " hadNoApp");
            removedFromHistory = true;
        } else {
            if (ActivityTaskManagerDebugConfig.DEBUG_STATES) {
                Slog.v("ActivityTaskManager", "Moving to DESTROYED: " + r + " (no app)");
            }
            r.setState(ActivityState.DESTROYED, "destroyActivityLocked. not finishing and had no app");
            if (ActivityTaskManagerDebugConfig.DEBUG_APP) {
                Slog.v("ActivityTaskManager", "Clearing app during destroy for activity " + r);
            }
            r.app = null;
        }
        r.configChangeFlags = 0;
        if (!this.mLRUActivities.remove(r) && hadApp) {
            Slog.w("ActivityTaskManager", "Activity " + r + " being finished, but not in LRU list");
        }
        this.mFIFOActivities.remove(r);
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
        if (ActivityTaskManagerDebugConfig.DEBUG_CONTAINERS) {
            Slog.d("ActivityTaskManager", "activityDestroyedLocked: r=" + record);
        }
        if (isInStackLocked(record) != null && record.isState(ActivityState.DESTROYING, ActivityState.DESTROYED)) {
            cleanUpActivityLocked(record, true, false);
            removeActivityFromHistoryLocked(record, reason);
        }
        this.mStackSupervisor.mActivityLaunchTrack = "activityDestroyed";
        this.mRootActivityContainer.resumeFocusedStacksTopActivities();
    }

    private void removeHistoryRecordsForAppLocked(ArrayList<ActivityRecord> list, WindowProcessController app, String listName) {
        int i = list.size();
        if (ActivityTaskManagerDebugConfig.DEBUG_CLEANUP) {
            Slog.v("ActivityTaskManager", "Removing app " + app + " from list " + listName + " with " + i + " entries");
        }
        while (i > 0) {
            i--;
            ActivityRecord r = list.get(i);
            if (ActivityTaskManagerDebugConfig.DEBUG_CLEANUP) {
                Slog.v("ActivityTaskManager", "Record #" + i + " " + r);
            }
            if (r.app == app) {
                if (ActivityTaskManagerDebugConfig.DEBUG_CLEANUP) {
                    Slog.v("ActivityTaskManager", "---> REMOVING this entry!");
                }
                list.remove(i);
                removeTimeoutsForActivityLocked(r);
            }
        }
    }

    private boolean removeHistoryRecordsForAppLocked(WindowProcessController app) {
        boolean remove;
        removeHistoryRecordsForAppLocked(this.mLRUActivities, app, "mLRUActivities");
        removeFIFORecordsForAppLocked(app);
        removeHistoryRecordsForAppLocked(this.mStackSupervisor.mStoppingActivities, app, "mStoppingActivities");
        removeHistoryRecordsForAppLocked(this.mStackSupervisor.mGoingToSleepActivities, app, "mGoingToSleepActivities");
        removeHistoryRecordsForAppLocked(this.mStackSupervisor.mFinishingActivities, app, "mFinishingActivities");
        boolean isProcessRemoved = app.isRemoved();
        if (isProcessRemoved) {
            app.makeFinishingForProcessRemoved();
        }
        boolean hasVisibleActivities = false;
        int i = numActivities();
        if (ActivityTaskManagerDebugConfig.DEBUG_CLEANUP) {
            Slog.v("ActivityTaskManager", "Removing app " + app + " from history with " + i + " entries");
        }
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            ArrayList<ActivityRecord> activities = this.mTaskHistory.get(taskNdx).mActivities;
            this.mTmpActivities.clear();
            this.mTmpActivities.addAll(activities);
            while (!this.mTmpActivities.isEmpty()) {
                int targetIndex = this.mTmpActivities.size() - 1;
                ActivityRecord r = this.mTmpActivities.remove(targetIndex);
                if (ActivityTaskManagerDebugConfig.DEBUG_CLEANUP) {
                    Slog.v("ActivityTaskManager", "Record #" + targetIndex + " " + r + ": app=" + r.app);
                }
                if (r.app == app) {
                    if (r.visible) {
                        hasVisibleActivities = true;
                    }
                    if ((r.mRelaunchReason == 1 || r.mRelaunchReason == 2) && r.launchCount < 3 && !r.finishing) {
                        remove = false;
                    } else if (!(r.haveState || r.stateNotNeeded || r.isState(ActivityState.RESTARTING_PROCESS)) || r.finishing) {
                        remove = true;
                    } else if (!r.visible && r.launchCount > 2 && r.lastLaunchTime > SystemClock.uptimeMillis() - 60000) {
                        remove = true;
                    } else if (r.launchCount <= 5 || !r.visible) {
                        remove = false;
                    } else {
                        Slog.v("ActivityTaskManager", "too many launcher times, remove : " + r);
                        remove = true;
                    }
                    if (remove) {
                        if (ActivityTaskManagerDebugConfig.DEBUG_ADD_REMOVE || ActivityTaskManagerDebugConfig.DEBUG_CLEANUP) {
                            Slog.i("ActivityTaskManager", "Removing activity " + r + " from stack at " + i + ": haveState=" + r.haveState + " stateNotNeeded=" + r.stateNotNeeded + " finishing=" + r.finishing + " state=" + r.getState() + " callers=" + Debug.getCallers(5));
                        }
                        if (!r.finishing || isProcessRemoved) {
                            Slog.w("ActivityTaskManager", "Force removing " + r + ": app died, no saved state");
                            EventLog.writeEvent(30001, Integer.valueOf(r.mUserId), Integer.valueOf(System.identityHashCode(r)), Integer.valueOf(r.getTaskRecord().taskId), r.shortComponentName, "proc died without state saved");
                        }
                    } else {
                        if (ActivityTaskManagerDebugConfig.DEBUG_ALL) {
                            Slog.v("ActivityTaskManager", "Keeping entry, setting app to null");
                        }
                        if (ActivityTaskManagerDebugConfig.DEBUG_APP) {
                            Slog.v("ActivityTaskManager", "Clearing app during removeHistory for activity " + r);
                        }
                        r.app = null;
                        r.nowVisible = r.visible;
                        if (!r.haveState) {
                            if (ActivityTaskManagerDebugConfig.DEBUG_SAVED_STATE) {
                                Slog.i("ActivityTaskManager", "App died, clearing saved state of " + r);
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
        getDisplay().mDisplayContent.prepareAppTransition(transit, false);
    }

    private void updateTaskMovement(TaskRecord task, boolean toFront) {
        if (task.isPersistable) {
            task.mLastTimeMoved = System.currentTimeMillis();
            if (!toFront) {
                task.mLastTimeMoved *= -1;
            }
        }
        this.mRootActivityContainer.invalidateTaskLayers();
    }

    /* JADX INFO: finally extract failed */
    /* access modifiers changed from: protected */
    public void moveTaskToFrontLocked(TaskRecord tr, boolean noAnimation, ActivityOptions options, AppTimeTracker timeTracker, String reason) {
        boolean noAnimation2;
        int i;
        ActivityDisplay ad;
        int i2;
        ActivityDisplay ad2;
        int i3;
        ActivityDisplay ad3;
        int i4;
        ActivityDisplay ad4;
        ActivityRecord top;
        Flog.i((int) PAUSE_TIMEOUT_MSG, "moveTaskToFront: " + tr + ", reason: " + reason);
        this.mHwActivityStackEx.moveTaskToFrontEx(tr);
        if (tr == null || tr.getStack() == null || !tr.inHwFreeFormWindowingMode() || !tr.getStack().isAlwaysOnTop() || (top = tr.getTopActivity()) == null || !top.nowVisible) {
            noAnimation2 = noAnimation;
        } else {
            noAnimation2 = true;
        }
        ActivityStack topStack = getDisplay().getTopStack();
        ActivityRecord topActivity = topStack != null ? topStack.getTopActivity() : null;
        int numTasks = this.mTaskHistory.size();
        int index = this.mTaskHistory.indexOf(tr);
        if (numTasks != 0 && index >= 0) {
            if (timeTracker != null) {
                for (int i5 = tr.mActivities.size() - 1; i5 >= 0; i5--) {
                    tr.mActivities.get(i5).appTimeTracker = timeTracker;
                }
            }
            try {
                getDisplay().deferUpdateImeTarget();
                if (tr.getTask() == null || tr.getTask().getParent() != null) {
                    insertTaskAtTop(tr, null);
                    ActivityRecord top2 = tr.getTopActivity();
                    if (top2 == null || !top2.okToShowLocked()) {
                        if (top2 != null) {
                            this.mStackSupervisor.mRecentTasks.add(top2.getTaskRecord());
                        }
                        ActivityOptions.abort(options);
                        ActivityDisplay activityDisplay = getDisplay();
                        if (activityDisplay != null) {
                            activityDisplay.continueUpdateImeTarget();
                            return;
                        }
                        Slog.e("ActivityTaskManager", "activityDisplay is null for " + this.mDisplayId);
                        if (!(HwPCUtils.isPcCastModeInServer() || (i2 = this.mDisplayId) == -1 || (ad2 = this.mRootActivityContainer.getActivityDisplayOrCreate(i2)) == null)) {
                            ad2.continueUpdateImeTarget();
                            return;
                        }
                        return;
                    }
                    ActivityRecord r = topRunningActivityLocked();
                    if (r != null) {
                        r.moveFocusableActivityToTop(reason);
                    }
                    if (ActivityTaskManagerDebugConfig.DEBUG_TRANSITION) {
                        Slog.v("ActivityTaskManager", "Prepare to front transition: task=" + tr);
                    }
                    if (noAnimation2) {
                        getDisplay().mDisplayContent.prepareAppTransition(0, false);
                        if (r != null) {
                            this.mStackSupervisor.mNoAnimActivities.add(r);
                        }
                        ActivityOptions.abort(options);
                    } else {
                        updateTransitLocked(10, options);
                    }
                    if (canEnterPipOnTaskSwitch(topActivity, tr, null, options)) {
                        topActivity.supportsEnterPipOnTaskSwitch = true;
                    }
                    this.mStackSupervisor.mActivityLaunchTrack = "taskMove";
                    this.mRootActivityContainer.resumeFocusedStacksTopActivities();
                    EventLog.writeEvent(30002, Integer.valueOf(tr.userId), Integer.valueOf(tr.taskId));
                    this.mService.getTaskChangeNotificationController().notifyTaskMovedToFront(tr.getTaskInfo());
                    ActivityDisplay activityDisplay2 = getDisplay();
                    if (activityDisplay2 != null) {
                        activityDisplay2.continueUpdateImeTarget();
                        return;
                    }
                    Slog.e("ActivityTaskManager", "activityDisplay is null for " + this.mDisplayId);
                    if (!(HwPCUtils.isPcCastModeInServer() || (i3 = this.mDisplayId) == -1 || (ad3 = this.mRootActivityContainer.getActivityDisplayOrCreate(i3)) == null)) {
                        ad3.continueUpdateImeTarget();
                        return;
                    }
                    return;
                }
                Flog.i((int) PAUSE_TIMEOUT_MSG, "Parent is null, do not move task!");
                ActivityDisplay activityDisplay3 = getDisplay();
                if (activityDisplay3 != null) {
                    activityDisplay3.continueUpdateImeTarget();
                    return;
                }
                Slog.e("ActivityTaskManager", "activityDisplay is null for " + this.mDisplayId);
                if (!(HwPCUtils.isPcCastModeInServer() || (i4 = this.mDisplayId) == -1 || (ad4 = this.mRootActivityContainer.getActivityDisplayOrCreate(i4)) == null)) {
                    ad4.continueUpdateImeTarget();
                }
            } catch (Throwable th) {
                ActivityDisplay activityDisplay4 = getDisplay();
                if (activityDisplay4 == null) {
                    Slog.e("ActivityTaskManager", "activityDisplay is null for " + this.mDisplayId);
                    if (!(HwPCUtils.isPcCastModeInServer() || (i = this.mDisplayId) == -1 || (ad = this.mRootActivityContainer.getActivityDisplayOrCreate(i)) == null)) {
                        ad.continueUpdateImeTarget();
                    }
                } else {
                    activityDisplay4.continueUpdateImeTarget();
                }
                throw th;
            }
        } else if (noAnimation2) {
            ActivityOptions.abort(options);
        } else {
            updateTransitLocked(10, options);
        }
    }

    /* access modifiers changed from: protected */
    public boolean moveTaskToBackLocked(int taskId) {
        TaskStack taskStack;
        TaskStack taskStack2;
        ActivityStack stack;
        if (this.mHwActivityStackEx.moveTaskToBackEx(taskId)) {
            return true;
        }
        TaskRecord tr = taskForIdLocked(taskId);
        if (tr == null) {
            Slog.i("ActivityTaskManager", "moveTaskToBack: bad taskId=" + taskId);
            return false;
        } else if (!HwFreeFormUtils.isFreeFormEnable() || !tr.inFreeformWindowingMode() || (stack = tr.getStack()) == null) {
            Slog.i("ActivityTaskManager", "moveTaskToBack: " + tr);
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
                        Watchdog.getInstance().setActivityController((IActivityController) null);
                    }
                    if (!moveOK) {
                        return false;
                    }
                }
            }
            if (ActivityTaskManagerDebugConfig.DEBUG_TRANSITION) {
                Slog.v("ActivityTaskManager", "Prepare to back transition: task=" + taskId);
            }
            if (this.mTaskHistory.size() > 0) {
                this.mTaskHistory.remove(tr);
            }
            this.mTaskHistory.add(0, tr);
            updateTaskMovement(tr, false);
            boolean isNeedFlagSetFalse = false;
            if (inHwFreeFormWindowingMode() && isAlwaysOnTop()) {
                super.setAlwaysOnTop(false);
                if (getWindowingMode() == 102 && (taskStack2 = this.mTaskStack) != null && !taskStack2.inHwFreeFormMoveBackOrCloseState()) {
                    this.mTaskStack.setInHwFreeFormMoveBackOrCloseState(true);
                    isNeedFlagSetFalse = true;
                }
            }
            getDisplay().mDisplayContent.prepareAppTransition(11, false);
            moveToBack("moveTaskToBackLocked", tr);
            if (getWindowingMode() == 102 && (taskStack = this.mTaskStack) != null && isNeedFlagSetFalse) {
                taskStack.setInHwFreeFormMoveBackOrCloseState(false);
            }
            if (inPinnedWindowingMode()) {
                this.mStackSupervisor.removeStack(this);
                return true;
            }
            ActivityRecord topActivity = getDisplay().topRunningActivity();
            ActivityStack topStack = topActivity.getActivityStack();
            if (!(topStack == null || topStack == this || (!topActivity.isState(ActivityState.RESUMED) && (!inHwFreeFormWindowingMode() || topStack.getWindowingMode() != 2)))) {
                this.mRootActivityContainer.ensureVisibilityAndConfig(null, getDisplay().mDisplayId, false, false);
                if (!topStack.inHwTvFreeFormWindowingMode()) {
                    this.mService.setResumedActivityUncheckLocked(topActivity, "moveTaskToBackLocked");
                }
            }
            this.mRootActivityContainer.resumeFocusedStacksTopActivities();
            return true;
        } else {
            HwFreeFormUtils.log("ActivityTaskManager", "moveTaskToBack finish freeform task" + tr);
            stack.setFreeFormStackVisible(false);
            stack.setCurrentPkgUnderFreeForm("");
            this.mStackSupervisor.removeTaskByIdLocked(taskId, false, false, "moveTaskToBack finish freeform task");
            stack.finishAllActivitiesLocked(true);
            return false;
        }
    }

    static void logStartActivity(int tag, ActivityRecord r, TaskRecord task) {
        String strData;
        Uri data = r.intent.getData();
        if (data == null || (r.intent.getHwFlags() & 16) != 0) {
            strData = null;
        } else {
            strData = data.toSafeString();
        }
        EventLog.writeEvent(tag, Integer.valueOf(r.mUserId), Integer.valueOf(System.identityHashCode(r)), Integer.valueOf(task.taskId), r.shortComponentName, r.intent.getAction(), r.intent.getType(), strData, Integer.valueOf(r.intent.getFlags()));
    }

    /* access modifiers changed from: package-private */
    public void ensureVisibleActivitiesConfigurationLocked(ActivityRecord start, boolean preserveWindow) {
        if (start != null && start.visible) {
            boolean behindFullscreen = false;
            boolean updatedConfig = false;
            for (int taskIndex = this.mTaskHistory.indexOf(start.getTaskRecord()); taskIndex >= 0; taskIndex--) {
                TaskRecord task = this.mTaskHistory.get(taskIndex);
                ArrayList<ActivityRecord> activities = task.mActivities;
                int activityIndex = start.getTaskRecord() == task ? activities.indexOf(start) : activities.size() - 1;
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
                this.mRootActivityContainer.resumeFocusedStacksTopActivities();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void requestResize(Rect bounds) {
        this.mService.resizeStack(this.mStackId, bounds, true, false, false, -1);
    }

    /* access modifiers changed from: package-private */
    public void resize(Rect bounds, Rect tempTaskBounds, Rect tempTaskInsetBounds) {
        if (updateBoundsAllowed(bounds)) {
            Rect taskBounds = tempTaskBounds != null ? tempTaskBounds : bounds;
            for (int i = this.mTaskHistory.size() - 1; i >= 0; i--) {
                TaskRecord task = this.mTaskHistory.get(i);
                if (task.isResizeable() || CoordinationModeUtils.getInstance(this.mService.mContext).isEnterOrExitCoordinationMode() || this.mService.mWindowManager.getDefaultDisplayContentLocked().getCoordinationPrimaryStackIgnoringVisibility() != null) {
                    task.updateOverrideConfiguration(taskBounds, tempTaskInsetBounds);
                }
            }
            setBounds(bounds);
        }
    }

    /* access modifiers changed from: package-private */
    public void onPipAnimationEndResize() {
        TaskStack taskStack = this.mTaskStack;
        if (taskStack != null) {
            taskStack.onPipAnimationEndResize();
        }
    }

    /* access modifiers changed from: package-private */
    public void setTaskBounds(Rect bounds) {
        if (updateBoundsAllowed(bounds)) {
            for (int i = this.mTaskHistory.size() - 1; i >= 0; i--) {
                TaskRecord task = this.mTaskHistory.get(i);
                if (task.isResizeable()) {
                    task.setBounds(bounds);
                } else {
                    task.setBounds(null);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setTaskDisplayedBounds(Rect bounds) {
        if (updateDisplayedBoundsAllowed(bounds)) {
            for (int i = this.mTaskHistory.size() - 1; i >= 0; i--) {
                TaskRecord task = this.mTaskHistory.get(i);
                if (bounds == null || bounds.isEmpty()) {
                    task.setDisplayedBounds(null);
                } else if (task.isResizeable()) {
                    task.setDisplayedBounds(bounds);
                }
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
            Slog.e("ActivityTaskManager", "willActivityBeVisibleLocked: Returning false, would have returned true for r=" + r2);
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
        boolean didSomething = false;
        TaskRecord lastTask = null;
        ComponentName homeActivity = null;
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            ArrayList<ActivityRecord> activities = this.mTaskHistory.get(taskNdx).mActivities;
            this.mTmpActivities.clear();
            this.mTmpActivities.addAll(activities);
            while (!this.mTmpActivities.isEmpty()) {
                boolean sameComponent = false;
                ActivityRecord r = this.mTmpActivities.remove(0);
                if ((r.packageName.equals(packageName) && (filterByClasses == null || filterByClasses.contains(r.mActivityComponent.getClassName()))) || (packageName == null && r.mUserId == userId)) {
                    sameComponent = true;
                }
                if ((userId == -1 || r.mUserId == userId) && ((sameComponent || r.getTaskRecord() == lastTask) && (r.app == null || evenPersistent || !r.app.isPersistent()))) {
                    if (doit) {
                        if (r.isActivityTypeHome()) {
                            if (homeActivity == null || !homeActivity.equals(r.mActivityComponent)) {
                                homeActivity = r.mActivityComponent;
                            } else {
                                Slog.i("ActivityTaskManager", "Skip force-stop again " + r);
                            }
                        }
                        Slog.i("ActivityTaskManager", " finishDisabledPackageActivitiesLocked Force finishing activity " + r);
                        TaskRecord lastTask2 = r.getTaskRecord();
                        finishActivityLocked(r, 0, null, "force-stop", true);
                        homeActivity = homeActivity;
                        didSomething = true;
                        lastTask = lastTask2;
                    } else if (!r.finishing) {
                        return true;
                    }
                }
            }
        }
        return didSomething;
    }

    /* access modifiers changed from: package-private */
    public void getRunningTasks(List<TaskRecord> tasksOut, @WindowConfiguration.ActivityType int ignoreActivityType, @WindowConfiguration.WindowingMode int ignoreWindowingMode, int callingUid, boolean allowed) {
        boolean focusedStack = this.mRootActivityContainer.getTopDisplayFocusedStack() == this;
        boolean topTask = true;
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            TaskRecord task = this.mTaskHistory.get(taskNdx);
            if (task.getTopActivity() != null && ((allowed || task.isActivityTypeHome() || task.effectiveUid == callingUid) && ((ignoreActivityType == 0 || task.getActivityType() != ignoreActivityType) && (ignoreWindowingMode == 0 || task.getWindowingMode() != ignoreWindowingMode)))) {
                if (focusedStack && topTask) {
                    task.lastActiveTime = SystemClock.elapsedRealtime();
                    topTask = false;
                }
                tasksOut.add(task);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void unhandledBackLocked() {
        int top = this.mTaskHistory.size() - 1;
        if (ActivityTaskManagerDebugConfig.DEBUG_SWITCH) {
            Slog.d("ActivityTaskManager", "Performing unhandledBack(): top activity at " + top);
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
    public boolean handleAppDiedLocked(WindowProcessController app) {
        ActivityRecord activityRecord = this.mPausingActivity;
        if (activityRecord != null && activityRecord.app == app) {
            if (ActivityTaskManagerDebugConfig.DEBUG_PAUSE || ActivityTaskManagerDebugConfig.DEBUG_CLEANUP) {
                Slog.v("ActivityTaskManager", "App died while pausing: " + this.mPausingActivity);
            }
            this.mPausingActivity = null;
        }
        ActivityRecord activityRecord2 = this.mLastPausedActivity;
        if (activityRecord2 != null && activityRecord2.app == app) {
            this.mLastPausedActivity = null;
            this.mLastNoHistoryActivity = null;
        }
        return removeHistoryRecordsForAppLocked(app);
    }

    /* access modifiers changed from: package-private */
    public void handleAppCrash(WindowProcessController app) {
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            ArrayList<ActivityRecord> activities = this.mTaskHistory.get(taskNdx).mActivities;
            for (int activityNdx = activities.size() - 1; activityNdx >= 0; activityNdx--) {
                ActivityRecord r = null;
                if (activityNdx < activities.size()) {
                    r = activities.get(activityNdx);
                }
                if (r != null && r.app == app) {
                    Slog.w("ActivityTaskManager", "  handleAppCrashLocked Force finishing activity " + r.intent.getComponent().flattenToShortString());
                    r.app = null;
                    getDisplay().mDisplayContent.prepareAppTransition(26, false);
                    finishCurrentActivityLocked(r, 0, false, "handleAppCrashedLocked");
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean dump(FileDescriptor fd, PrintWriter pw, boolean dumpAll, boolean dumpClient, String dumpPackage, boolean needSep) {
        pw.println("  Stack #" + this.mStackId + ": type=" + WindowConfiguration.activityTypeToString(getActivityType()) + " mode=" + WindowConfiguration.windowingModeToString(getWindowingMode()));
        StringBuilder sb = new StringBuilder();
        sb.append("  isSleeping=");
        sb.append(shouldSleepActivities());
        pw.println(sb.toString());
        pw.println("  mBounds=" + getRequestedOverrideBounds());
        boolean printed = ActivityStackSupervisor.dumpHistoryList(fd, pw, this.mLRUActivities, "    ", "Run", false, dumpAll ^ true, false, dumpPackage, true, "    Running activities (most recent first):", null) | dumpActivitiesLocked(fd, pw, dumpAll, dumpClient, dumpPackage, needSep);
        boolean needSep2 = printed;
        if (ActivityStackSupervisor.printThisActivity(pw, this.mPausingActivity, dumpPackage, needSep2, "    mPausingActivity: ")) {
            printed = true;
            needSep2 = false;
        }
        if (ActivityStackSupervisor.printThisActivity(pw, getResumedActivity(), dumpPackage, needSep2, "    mResumedActivity: ")) {
            printed = true;
            needSep2 = false;
        }
        if (!dumpAll) {
            return printed;
        }
        if (ActivityStackSupervisor.printThisActivity(pw, this.mLastPausedActivity, dumpPackage, needSep2, "    mLastPausedActivity: ")) {
            printed = true;
            needSep2 = true;
        }
        return printed | ActivityStackSupervisor.printThisActivity(pw, this.mLastNoHistoryActivity, dumpPackage, needSep2, "    mLastNoHistoryActivity: ");
    }

    /* access modifiers changed from: package-private */
    public boolean dumpActivitiesLocked(FileDescriptor fd, PrintWriter pw, boolean dumpAll, boolean dumpClient, String dumpPackage, boolean needSep) {
        if (this.mTaskHistory.isEmpty()) {
            return false;
        }
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx += -1) {
            TaskRecord task = this.mTaskHistory.get(taskNdx);
            if (needSep) {
                pw.println("");
            }
            pw.println("    Task id #" + task.taskId);
            pw.println("    mBounds=" + task.getRequestedOverrideBounds());
            pw.println("    mMinWidth=" + task.mMinWidth);
            pw.println("    mMinHeight=" + task.mMinHeight);
            pw.println("    mLastNonFullscreenBounds=" + task.mLastNonFullscreenBounds);
            pw.println("    * " + task);
            task.dump(pw, "      ");
            ActivityStackSupervisor.dumpHistoryList(fd, pw, this.mTaskHistory.get(taskNdx).mActivities, "    ", "Hist", true, dumpAll ^ true, dumpClient, dumpPackage, false, null, task);
        }
        return true;
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
        Flog.i((int) PAUSE_TIMEOUT_MSG, "Task removed: " + task + ", reason: " + reason + ", mode: " + mode);
        if (task != null && task.inHwPCMultiStackWindowingMode()) {
            Slog.i("ActivityTaskManager", "notify pc to close window when removeTask");
            this.mService.getTaskChangeNotificationController().notifyTaskRemovalStarted(task.getTaskInfo());
        }
        if (this.mTaskHistory.remove(task)) {
            EventLog.writeEvent(30061, Integer.valueOf(task.taskId), Integer.valueOf(getStackId()));
        }
        removeActivitiesFromLRUListLocked(task);
        updateTaskMovement(task, true);
        if (mode == 0) {
            task.cleanUpResourcesForDestroy();
        }
        if (this.mTaskHistory.isEmpty() && !reason.contains("swapDockedAndFullscreenStack")) {
            if (ActivityTaskManagerDebugConfig.DEBUG_STACK) {
                Slog.i("ActivityTaskManager", "removeTask: removing stack=" + this);
            }
            if (mode != 2 && this.mRootActivityContainer.isTopDisplayFocusedStack(this)) {
                String myReason = reason + " leftTaskHistoryEmpty";
                if (!inMultiWindowMode() || adjustFocusToNextFocusableStack(myReason) == null) {
                    getDisplay().moveHomeStackToFront(myReason);
                }
            }
            if (isAttached()) {
                getDisplay().positionChildAtBottom(this);
            }
            if (!isActivityTypeHome() || !isAttached()) {
                remove();
            }
        }
        TaskRecord lastResumedTask = null;
        task.setStack(null);
        if (HwMwUtils.ENABLED && mode == 0 && (task.inSplitScreenPrimaryWindowingMode() || task.inHwMagicWindowingMode())) {
            HwMwUtils.performPolicy(15, new Object[]{task});
        }
        if (inPinnedWindowingMode()) {
            this.mService.getTaskChangeNotificationController().notifyActivityUnpinned();
            LogPower.push(223);
            if (this.mService.mLastResumedActivity != null) {
                lastResumedTask = this.mService.mLastResumedActivity.getTaskRecord();
            }
            if (task.equals(lastResumedTask) && this.mRootActivityContainer.getTopResumedActivity() != null) {
                this.mService.setResumedActivityUncheckLocked(this.mRootActivityContainer.getTopResumedActivity(), "PIP removeTask");
                Slog.i("ActivityTaskManager", "removeTask: setLastResumeActivity to : " + this.mService.mLastResumedActivity);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public TaskRecord createTaskRecord(int taskId, ActivityInfo info, Intent intent, IVoiceInteractionSession voiceSession, IVoiceInteractor voiceInteractor, boolean toTop) {
        return createTaskRecord(taskId, info, intent, voiceSession, voiceInteractor, toTop, null, null, null);
    }

    /* access modifiers changed from: package-private */
    public TaskRecord createTaskRecord(int taskId, ActivityInfo info, Intent intent, IVoiceInteractionSession voiceSession, IVoiceInteractor voiceInteractor, boolean toTop, ActivityRecord activity, ActivityRecord source, ActivityOptions options) {
        TaskRecord task = TaskRecord.create(this.mService, taskId, info, intent, voiceSession, voiceInteractor);
        addTask(task, toTop, "createTaskRecord");
        int displayId = this.mDisplayId;
        boolean z = false;
        if (displayId == -1) {
            displayId = 0;
        }
        boolean isLockscreenShown = this.mService.mStackSupervisor.getKeyguardController().isKeyguardOrAodShowing(displayId);
        if (!this.mStackSupervisor.getLaunchParamsController().layoutTask(task, info.windowLayout, activity, source, options) && !matchParentBounds() && task.isResizeable() && !isLockscreenShown) {
            task.updateOverrideConfiguration(getRequestedOverrideBounds());
        }
        if ((info.flags & 1024) != 0) {
            z = true;
        }
        task.createTask(toTop, z);
        return task;
    }

    /* access modifiers changed from: package-private */
    public ArrayList<TaskRecord> getAllTasks() {
        return new ArrayList<>(this.mTaskHistory);
    }

    /* access modifiers changed from: package-private */
    public void addTask(TaskRecord task, boolean toTop, String reason) {
        addTask(task, toTop ? Integer.MAX_VALUE : 0, true, reason);
        if (toTop) {
            positionChildWindowContainerAtTop(task);
        }
    }

    /* access modifiers changed from: package-private */
    public void addTask(TaskRecord task, int position, boolean schedulePictureInPictureModeChange, String reason) {
        this.mTaskHistory.remove(task);
        if (!isSingleTaskInstance() || this.mTaskHistory.isEmpty()) {
            int position2 = getAdjustedPositionForTask(task, position, null);
            boolean toTop = position2 >= this.mTaskHistory.size();
            ActivityStack prevStack = preAddTask(task, reason, toTop);
            this.mTaskHistory.add(position2, task);
            task.setStack(this);
            updateTaskMovement(task, toTop);
            postAddTask(task, prevStack, schedulePictureInPictureModeChange);
            return;
        }
        throw new IllegalStateException("Can only have one child on stack=" + this);
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
                    Log.wtf("ActivityTaskManager", "mResumedActivity was already set when moving mResumedActivity from other stack to this stack mResumedActivity=" + this.mResumedActivity + " other mResumedActivity=" + topRunningActivity);
                }
                topRunningActivity.setState(ActivityState.RESUMED, "positionChildAt");
            }
            ensureActivitiesVisibleLocked(null, 0, false);
            this.mRootActivityContainer.resumeFocusedStacksTopActivities();
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

    public void setAlwaysOnTopOnly(boolean alwaysOnTop) {
        if (super.isAlwaysOnTop() != alwaysOnTop) {
            Log.i("ActivityTaskManager", "setAlwaysOnTopOnly alwaysOnTop = " + alwaysOnTop + " for stackId = " + this.mStackId);
            super.setAlwaysOnTop(alwaysOnTop);
        }
    }

    @Override // com.android.server.wm.ConfigurationContainer
    public void setAlwaysOnTop(boolean alwaysOnTop) {
        if (isAlwaysOnTop() != alwaysOnTop) {
            super.setAlwaysOnTop(alwaysOnTop);
            ActivityDisplay display = getDisplay();
            if (display != null) {
                if (inHwFreeFormWindowingMode() && !alwaysOnTop) {
                    display.positionChildAtBottom(this);
                } else if (!alwaysOnTop) {
                } else {
                    if (inHwFreeFormWindowingMode()) {
                        display.positionChildAtTop(this, false, "HwFreeformSetAlwaysOnTop");
                    } else {
                        display.positionChildAtTop(this, false);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void moveToFrontAndResumeStateIfNeeded(ActivityRecord r, boolean moveToFront, boolean setResume, boolean setPause, String reason) {
        if (moveToFront) {
            ActivityState origState = r.getState();
            if (setResume) {
                r.setState(ActivityState.RESUMED, "moveToFrontAndResumeStateIfNeeded");
                updateLRUListLocked(r);
            }
            if (setPause) {
                this.mPausingActivity = r;
                schedulePauseTimeout(r);
            }
            moveToFront(reason);
            if (origState == ActivityState.RESUMED && r == this.mRootActivityContainer.getTopResumedActivity()) {
                this.mService.setResumedActivityUncheckLocked(r, reason);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public Rect getDefaultPictureInPictureBounds(float aspectRatio) {
        if (getTaskStack() == null) {
            return null;
        }
        return getTaskStack().getPictureInPictureBounds(aspectRatio, null);
    }

    /* access modifiers changed from: package-private */
    public void animateResizePinnedStack(Rect sourceHintBounds, Rect toBounds, int animationDuration, boolean fromFullscreen) {
        if (inPinnedWindowingMode()) {
            if (skipResizeAnimation(toBounds == null)) {
                if (!this.mService.mHwATMSEx.isSwitchToMagicWin(this.mStackId, false, getConfiguration().orientation)) {
                    this.mService.moveTasksToFullscreenStack(this.mStackId, true);
                }
            } else if (getTaskStack() != null) {
                getTaskStack().animateResizePinnedStack(toBounds, sourceHintBounds, animationDuration, fromFullscreen);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void getAnimationOrCurrentBounds(Rect outBounds) {
        TaskStack stack = getTaskStack();
        if (stack == null) {
            outBounds.setEmpty();
        } else {
            stack.getAnimationOrCurrentBounds(outBounds);
        }
    }

    private boolean skipResizeAnimation(boolean toFullscreen) {
        if (!toFullscreen) {
            return false;
        }
        Configuration parentConfig = getParent().getConfiguration();
        ActivityRecord top = topRunningNonOverlayTaskActivity();
        if (top == null || top.isConfigurationCompatible(parentConfig)) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public void setPictureInPictureAspectRatio(float aspectRatio) {
        if (getTaskStack() != null) {
            getTaskStack().setPictureInPictureAspectRatio(aspectRatio);
        }
    }

    /* access modifiers changed from: package-private */
    public void setPictureInPictureActions(List<RemoteAction> actions) {
        if (getTaskStack() != null) {
            getTaskStack().setPictureInPictureActions(actions);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isAnimatingBoundsToFullscreen() {
        if (getTaskStack() == null) {
            return false;
        }
        return getTaskStack().isAnimatingBoundsToFullscreen();
    }

    public void updatePictureInPictureModeForPinnedStackAnimation(Rect targetStackBounds, boolean forceUpdate) {
        synchronized (this.mService.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (isAttached()) {
                    ArrayList<TaskRecord> tasks = getAllTasks();
                    for (int i = 0; i < tasks.size(); i++) {
                        this.mStackSupervisor.updatePictureInPictureMode(tasks.get(i), targetStackBounds, forceUpdate);
                    }
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
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
        getDisplay().mDisplayContent.executeAppTransition();
        ActivityOptions.abort(options);
    }

    /* access modifiers changed from: package-private */
    public boolean shouldSleepActivities() {
        ActivityDisplay display = getDisplay();
        if (!isFocusedStackOnDisplay() || !this.mStackSupervisor.getKeyguardController().isKeyguardGoingAway()) {
            return display != null ? display.isSleeping() : this.mService.isSleepingLocked();
        }
        if (!ActivityTaskManagerDebugConfig.DEBUG_KEYGUARD) {
            return false;
        }
        Flog.i((int) WindowManagerService.H.UNFREEZE_FOLD_ROTATION, "Skip sleeping activities for keyguard is in the process of going away");
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean shouldSleepOrShutDownActivities() {
        return shouldSleepActivities() || this.mService.mShuttingDown;
    }

    @Override // com.android.server.wm.ConfigurationContainer
    public void writeToProto(ProtoOutputStream proto, long fieldId, int logLevel) {
        long token = proto.start(fieldId);
        super.writeToProto(proto, 1146756268033L, logLevel);
        proto.write(1120986464258L, this.mStackId);
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            this.mTaskHistory.get(taskNdx).writeToProto(proto, 2246267895811L, logLevel);
        }
        ActivityRecord activityRecord = this.mResumedActivity;
        if (activityRecord != null) {
            activityRecord.writeIdentifierToProto(proto, 1146756268036L);
        }
        proto.write(1120986464261L, this.mDisplayId);
        if (!matchParentBounds()) {
            getRequestedOverrideBounds().writeToProto(proto, 1146756268039L);
        }
        proto.write(1133871366150L, matchParentBounds());
        proto.end(token);
    }

    public void setFreeFormStackVisible(boolean visible) {
        if (visible && !this.mIsFreeFormStackVisible) {
            this.mCurrentTime = System.currentTimeMillis();
        } else if (!visible && this.mIsFreeFormStackVisible && this.mCurrentTime > 0 && getTopActivity() != null) {
            Flog.bdReport(991311066, "{pkg:" + getTopActivity().packageName + ",currentTime:" + (System.currentTimeMillis() - this.mCurrentTime) + "}");
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

    public void setPendingShow(boolean value) {
        this.isPendingShow = value;
    }

    public boolean isPendingShow() {
        return this.isPendingShow;
    }

    private boolean isBehindSplitActivity(ActivityRecord r, boolean isTop, boolean aboveTop) {
        if (r == null) {
            return false;
        }
        if (ActivityTaskManagerDebugConfig.DEBUG_VISIBILITY) {
            String str = TAG_VISIBILITY;
            Slog.i(str, "AS isBehindSplitActivity: " + r + " isSplitMode=" + r.isSplitMode() + " isSplitBaseActivity=" + r.isSplitBaseActivity() + " isTop=" + isTop + " aboveTop=" + aboveTop);
        }
        if (!r.isShowInSplit() || r.isSplitBaseActivity()) {
            return false;
        }
        return true;
    }

    private void removeFIFORecordsForAppLocked(WindowProcessController app) {
        int i = this.mFIFOActivities.size();
        if (ActivityTaskManagerDebugConfig.DEBUG_VISIBILITY) {
            Slog.v(TAG_VISIBILITY, "Removing app " + app + " from list mFIFOActivities with " + i + " entries");
        }
        while (i > 0) {
            i--;
            ActivityRecord r = this.mFIFOActivities.get(i);
            if (ActivityTaskManagerDebugConfig.DEBUG_VISIBILITY) {
                Slog.v(TAG_VISIBILITY, "Record #" + i + " " + r);
            }
            if (r.app == app) {
                if (ActivityTaskManagerDebugConfig.DEBUG_VISIBILITY) {
                    Slog.v(TAG_VISIBILITY, "---> REMOVING this entry!");
                }
                this.mFIFOActivities.remove(i);
            }
        }
    }

    public void updateSplitFullScreenState(boolean fullscreenState) {
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            ArrayList<ActivityRecord> activities = this.mTaskHistory.get(taskNdx).mActivities;
            for (int activityNdx = activities.size() - 1; activityNdx >= 0; activityNdx--) {
                ActivityRecord r = activities.get(activityNdx);
                if (!r.finishing) {
                    r.updateSplitFullScreenState(fullscreenState);
                }
            }
        }
    }
}
