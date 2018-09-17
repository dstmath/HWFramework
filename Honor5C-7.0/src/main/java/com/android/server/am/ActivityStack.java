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
import android.graphics.Bitmap;
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
import android.os.PersistableBundle;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.Trace;
import android.os.UserHandle;
import android.service.voice.IVoiceInteractionSession;
import android.util.ArraySet;
import android.util.BoostFramework;
import android.util.EventLog;
import android.util.Flog;
import android.util.HwSlog;
import android.util.Jlog;
import android.util.Log;
import android.util.Slog;
import com.android.internal.app.IVoiceInteractor;
import com.android.internal.os.BatteryStatsImpl;
import com.android.internal.os.BatteryStatsImpl.Uid.Proc;
import com.android.server.HwServiceFactory;
import com.android.server.Watchdog;
import com.android.server.am.ActivityStackSupervisor.ActivityContainer;
import com.android.server.job.controllers.JobStatus;
import com.android.server.wm.TaskGroup;
import com.android.server.wm.WindowManagerService;
import com.android.server.wm.WindowManagerService.H;
import com.huawei.pgmng.log.LogPower;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class ActivityStack extends AbsActivityStack {
    private static final /* synthetic */ int[] -com-android-server-am-ActivityStack$ActivityStateSwitchesValues = null;
    static final long ACTIVITY_INACTIVE_RESET_TIME = 0;
    static final int DESTROY_ACTIVITIES_MSG = 105;
    static final int DESTROY_TIMEOUT = 10000;
    static final int DESTROY_TIMEOUT_MSG = 102;
    static final int FINISH_AFTER_PAUSE = 1;
    static final int FINISH_AFTER_VISIBLE = 2;
    static final int FINISH_IMMEDIATELY = 0;
    static final int LAUNCH_TICK = 500;
    static final int LAUNCH_TICK_MSG = 103;
    private static final int MAX_STOPPING_TO_FORCE = 3;
    static final int PAUSE_TIMEOUT = 500;
    static final int PAUSE_TIMEOUT_MSG = 101;
    static final int RELEASE_BACKGROUND_RESOURCES_TIMEOUT_MSG = 107;
    static final int REMOVE_TASK_MODE_DESTROYING = 0;
    static final int REMOVE_TASK_MODE_MOVING = 1;
    static final int REMOVE_TASK_MODE_MOVING_TO_TOP = 2;
    static final boolean SHOW_APP_STARTING_PREVIEW = true;
    static final int STACK_INVISIBLE = 0;
    static final int STACK_VISIBLE = 1;
    static final int STACK_VISIBLE_ACTIVITY_BEHIND = 2;
    static final long START_WARN_TIME = 5000;
    static final int STOP_TIMEOUT = 10000;
    static final int STOP_TIMEOUT_MSG = 104;
    protected static final String TAG = null;
    private static final String TAG_ADD_REMOVE = null;
    private static final String TAG_APP = null;
    private static final String TAG_CLEANUP = null;
    private static final String TAG_CONFIGURATION = null;
    private static final String TAG_CONTAINERS = null;
    private static final String TAG_PAUSE = null;
    private static final String TAG_RELEASE = null;
    private static final String TAG_RESULTS = null;
    private static final String TAG_SAVED_STATE = null;
    private static final String TAG_SCREENSHOTS = null;
    private static final String TAG_STACK = null;
    private static final String TAG_STATES = null;
    private static final String TAG_SWITCH = null;
    private static final String TAG_TASKS = null;
    private static final String TAG_TRANSITION = null;
    private static final String TAG_USER_LEAVING = null;
    private static final String TAG_VISIBILITY = null;
    static final long TRANSLUCENT_CONVERSION_TIMEOUT = 2000;
    static final int TRANSLUCENT_TIMEOUT_MSG = 106;
    protected static final boolean VALIDATE_TOKENS = false;
    public int[] aBoostParamVal;
    public int aBoostTimeOut;
    public int[] lBoostCpuParamVal;
    public int lBoostTimeOut;
    final ActivityContainer mActivityContainer;
    Rect mBounds;
    boolean mConfigWillChange;
    int mCurrentUser;
    final Rect mDeferredBounds;
    final Rect mDeferredTaskBounds;
    final Rect mDeferredTaskInsetBounds;
    int mDisplayId;
    boolean mFullscreen;
    long mFullyDrawnStartTime;
    final Handler mHandler;
    public boolean mIsAnimationBoostEnabled;
    public boolean mIsPerfBoostEnabled;
    final ArrayList<ActivityRecord> mLRUActivities;
    ActivityRecord mLastNoHistoryActivity;
    ActivityRecord mLastPausedActivity;
    ActivityRecord mLastStartedActivity;
    long mLaunchStartTime;
    final ArrayList<ActivityRecord> mNoAnimActivities;
    ActivityRecord mPausingActivity;
    public BoostFramework mPerf;
    public BoostFramework mPerfBoost;
    private final RecentTasks mRecentTasks;
    ActivityRecord mResumedActivity;
    Boolean mRuningBackground;
    final ActivityManagerService mService;
    final int mStackId;
    final ActivityStackSupervisor mStackSupervisor;
    ArrayList<ActivityStack> mStacks;
    protected ArrayList<TaskRecord> mTaskHistory;
    private final LaunchingTaskPositioner mTaskPositioner;
    ActivityRecord mTranslucentActivityWaiting;
    private ArrayList<ActivityRecord> mUndrawnActivitiesBelowTopTranslucent;
    boolean mUpdateBoundsDeferred;
    boolean mUpdateBoundsDeferredCalled;
    final ArrayList<TaskGroup> mValidateAppTokens;
    final WindowManagerService mWindowManager;
    String mshortComponentName;

    final class ActivityStackHandler extends Handler {
        ActivityStackHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            IBinder iBinder = null;
            ActivityRecord r;
            switch (msg.what) {
                case ActivityStack.PAUSE_TIMEOUT_MSG /*101*/:
                    r = msg.obj;
                    Slog.w(ActivityStack.TAG, "Activity pause timeout for " + r);
                    synchronized (ActivityStack.this.mService) {
                        try {
                            ActivityManagerService.boostPriorityForLockedSection();
                            if (r.app != null) {
                                ActivityStack.this.mService.logAppTooSlow(r.app, r.pauseTime, "pausing " + r);
                            }
                            ActivityStack.this.activityPausedLocked(r.appToken, ActivityStack.SHOW_APP_STARTING_PREVIEW);
                        } finally {
                            ActivityManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                case ActivityStack.DESTROY_TIMEOUT_MSG /*102*/:
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
                case ActivityStack.LAUNCH_TICK_MSG /*103*/:
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
                case ActivityStack.STOP_TIMEOUT_MSG /*104*/:
                    r = (ActivityRecord) msg.obj;
                    Slog.w(ActivityStack.TAG, "Activity stop timeout for " + r);
                    synchronized (ActivityStack.this.mService) {
                        try {
                            ActivityManagerService.boostPriorityForLockedSection();
                            if (r.isInHistory()) {
                                ActivityStack.this.activityStoppedLocked(r, null, null, null);
                            }
                        } finally {
                            ActivityManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                case ActivityStack.DESTROY_ACTIVITIES_MSG /*105*/:
                    ScheduleDestroyArgs args = msg.obj;
                    synchronized (ActivityStack.this.mService) {
                        try {
                            ActivityManagerService.boostPriorityForLockedSection();
                            ActivityStack.this.destroyActivitiesLocked(args.mOwner, args.mReason);
                        } finally {
                            ActivityManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                case ActivityStack.TRANSLUCENT_TIMEOUT_MSG /*106*/:
                    synchronized (ActivityStack.this.mService) {
                        try {
                            ActivityManagerService.boostPriorityForLockedSection();
                            ActivityStack.this.notifyActivityDrawnLocked(null);
                        } finally {
                            ActivityManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                case ActivityStack.RELEASE_BACKGROUND_RESOURCES_TIMEOUT_MSG /*107*/:
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
                default:
            }
        }
    }

    enum ActivityState {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.am.ActivityStack.ActivityState.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.am.ActivityStack.ActivityState.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.am.ActivityStack.ActivityState.<clinit>():void");
        }
    }

    static class ScheduleDestroyArgs {
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
            iArr[ActivityState.INITIALIZING.ordinal()] = STACK_VISIBLE;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[ActivityState.PAUSED.ordinal()] = STACK_VISIBLE_ACTIVITY_BEHIND;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[ActivityState.PAUSING.ordinal()] = MAX_STOPPING_TO_FORCE;
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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.am.ActivityStack.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.am.ActivityStack.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.am.ActivityStack.<clinit>():void");
    }

    boolean ensureActivityConfigurationLocked(com.android.server.am.ActivityRecord r1, int r2, boolean r3) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.am.ActivityStack.ensureActivityConfigurationLocked(com.android.server.am.ActivityRecord, int, boolean):boolean
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 7 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.am.ActivityStack.ensureActivityConfigurationLocked(com.android.server.am.ActivityRecord, int, boolean):boolean");
    }

    int numActivities() {
        int count = STACK_INVISIBLE;
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            count += ((TaskRecord) this.mTaskHistory.get(taskNdx)).mActivities.size();
        }
        return count;
    }

    public ActivityStack(ActivityContainer activityContainer, RecentTasks recentTasks) {
        LaunchingTaskPositioner launchingTaskPositioner = null;
        this.mPerf = null;
        this.mIsAnimationBoostEnabled = false;
        this.aBoostTimeOut = STACK_INVISIBLE;
        this.mPerfBoost = null;
        this.mIsPerfBoostEnabled = false;
        this.lBoostTimeOut = STACK_INVISIBLE;
        this.mTaskHistory = new ArrayList();
        this.mValidateAppTokens = new ArrayList();
        this.mLRUActivities = new ArrayList();
        this.mNoAnimActivities = new ArrayList();
        this.mPausingActivity = null;
        this.mLastPausedActivity = null;
        this.mLastNoHistoryActivity = null;
        this.mResumedActivity = null;
        this.mRuningBackground = Boolean.valueOf(false);
        this.mLastStartedActivity = null;
        this.mTranslucentActivityWaiting = null;
        this.mUndrawnActivitiesBelowTopTranslucent = new ArrayList();
        this.mFullscreen = SHOW_APP_STARTING_PREVIEW;
        this.mBounds = null;
        this.mDeferredBounds = new Rect();
        this.mDeferredTaskBounds = new Rect();
        this.mDeferredTaskInsetBounds = new Rect();
        this.mLaunchStartTime = ACTIVITY_INACTIVE_RESET_TIME;
        this.mFullyDrawnStartTime = ACTIVITY_INACTIVE_RESET_TIME;
        this.mshortComponentName = "";
        this.mActivityContainer = activityContainer;
        this.mStackSupervisor = activityContainer.getOuter();
        this.mService = this.mStackSupervisor.mService;
        this.mHandler = new ActivityStackHandler(this.mService.mHandler.getLooper());
        this.mWindowManager = this.mService.mWindowManager;
        this.mStackId = activityContainer.mStackId;
        this.mCurrentUser = this.mService.mUserController.getCurrentUserIdLocked();
        this.mRecentTasks = recentTasks;
        if (this.mStackId == STACK_VISIBLE_ACTIVITY_BEHIND) {
            launchingTaskPositioner = new LaunchingTaskPositioner();
        }
        this.mTaskPositioner = launchingTaskPositioner;
        this.mIsAnimationBoostEnabled = this.mService.mContext.getResources().getBoolean(17957047);
        if (this.mIsAnimationBoostEnabled) {
            this.aBoostTimeOut = this.mService.mContext.getResources().getInteger(17694927);
            this.aBoostParamVal = this.mService.mContext.getResources().getIntArray(17236060);
        }
        this.mIsPerfBoostEnabled = this.mService.mContext.getResources().getBoolean(17957045);
        if (this.mIsPerfBoostEnabled) {
            this.lBoostTimeOut = this.mService.mContext.getResources().getInteger(17694925);
            this.lBoostCpuParamVal = this.mService.mContext.getResources().getIntArray(17236058);
        }
    }

    void attachDisplay(ActivityDisplay activityDisplay, boolean onTop) {
        this.mDisplayId = activityDisplay.mDisplayId;
        this.mStacks = activityDisplay.mStacks;
        this.mBounds = this.mWindowManager.attachStack(this.mStackId, activityDisplay.mDisplayId, onTop);
        this.mFullscreen = this.mBounds == null ? SHOW_APP_STARTING_PREVIEW : false;
        if (this.mTaskPositioner != null) {
            this.mTaskPositioner.setDisplay(activityDisplay.mDisplay);
            this.mTaskPositioner.configure(this.mBounds);
        }
        if (this.mStackId == MAX_STOPPING_TO_FORCE) {
            this.mStackSupervisor.resizeDockedStackLocked(this.mBounds, null, null, null, null, SHOW_APP_STARTING_PREVIEW);
        }
    }

    void detachDisplay() {
        this.mDisplayId = -1;
        this.mStacks = null;
        if (this.mTaskPositioner != null) {
            this.mTaskPositioner.reset();
        }
        this.mWindowManager.detachStack(this.mStackId);
        if (this.mStackId == MAX_STOPPING_TO_FORCE) {
            this.mStackSupervisor.resizeDockedStackLocked(null, null, null, null, null, SHOW_APP_STARTING_PREVIEW);
        }
    }

    public void getDisplaySize(Point out) {
        this.mActivityContainer.mActivityDisplay.mDisplay.getSize(out);
    }

    void deferUpdateBounds() {
        if (!this.mUpdateBoundsDeferred) {
            this.mUpdateBoundsDeferred = SHOW_APP_STARTING_PREVIEW;
            this.mUpdateBoundsDeferredCalled = false;
        }
    }

    void continueUpdateBounds() {
        Rect rect = null;
        boolean wasDeferred = this.mUpdateBoundsDeferred;
        this.mUpdateBoundsDeferred = false;
        if (wasDeferred && this.mUpdateBoundsDeferredCalled) {
            ActivityStackSupervisor activityStackSupervisor = this.mStackSupervisor;
            Rect rect2 = this.mDeferredBounds.isEmpty() ? null : this.mDeferredBounds;
            Rect rect3 = this.mDeferredTaskBounds.isEmpty() ? null : this.mDeferredTaskBounds;
            if (!this.mDeferredTaskInsetBounds.isEmpty()) {
                rect = this.mDeferredTaskInsetBounds;
            }
            activityStackSupervisor.resizeStackUncheckedLocked(this, rect2, rect3, rect);
        }
    }

    boolean updateBoundsAllowed(Rect bounds, Rect tempTaskBounds, Rect tempTaskInsetBounds) {
        if (!this.mUpdateBoundsDeferred) {
            return SHOW_APP_STARTING_PREVIEW;
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
        this.mUpdateBoundsDeferredCalled = SHOW_APP_STARTING_PREVIEW;
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

    boolean okToShowLocked(ActivityRecord r) {
        return this.mStackSupervisor.okToShowLocked(r);
    }

    final ActivityRecord topRunningActivityLocked() {
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            ActivityRecord r = ((TaskRecord) this.mTaskHistory.get(taskNdx)).topRunningActivityLocked();
            if (r != null) {
                return r;
            }
        }
        return null;
    }

    final ActivityRecord topRunningNonDelayedActivityLocked(ActivityRecord notTop) {
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            ArrayList<ActivityRecord> activities = ((TaskRecord) this.mTaskHistory.get(taskNdx)).mActivities;
            for (int activityNdx = activities.size() - 1; activityNdx >= 0; activityNdx--) {
                ActivityRecord r = (ActivityRecord) activities.get(activityNdx);
                if (!r.finishing && !r.delayedResume && r != notTop && okToShowLocked(r)) {
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
                    if (!r.finishing && token != r.appToken && okToShowLocked(r)) {
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
        TaskRecord task = r.task;
        if (task == null || task.stack == null || !task.mActivities.contains(r) || !this.mTaskHistory.contains(task)) {
            return null;
        }
        if (task.stack != this) {
            Slog.w(TAG, "Illegal state! task does not point to stack it is in.");
        }
        return r;
    }

    final boolean updateLRUListLocked(ActivityRecord r) {
        boolean hadit = this.mLRUActivities.remove(r);
        this.mLRUActivities.add(r);
        return hadit;
    }

    final boolean isHomeStack() {
        return this.mStackId == 0 ? SHOW_APP_STARTING_PREVIEW : false;
    }

    final boolean isDockedStack() {
        return this.mStackId == MAX_STOPPING_TO_FORCE ? SHOW_APP_STARTING_PREVIEW : false;
    }

    final boolean isPinnedStack() {
        return this.mStackId == 4 ? SHOW_APP_STARTING_PREVIEW : false;
    }

    final boolean isOnHomeDisplay() {
        if (isAttached() && this.mActivityContainer.mActivityDisplay.mDisplayId == 0) {
            return SHOW_APP_STARTING_PREVIEW;
        }
        return false;
    }

    void moveToFront(String reason) {
        moveToFront(reason, null);
    }

    void moveToFront(String reason, TaskRecord task) {
        if (isAttached()) {
            this.mStacks.remove(this);
            int addIndex = this.mStacks.size();
            if (addIndex > 0) {
                ActivityStack topStack = (ActivityStack) this.mStacks.get(addIndex - 1);
                if (StackId.isAlwaysOnTop(topStack.mStackId) && topStack != this) {
                    addIndex--;
                }
            }
            this.mStacks.add(addIndex, this);
            if (isOnHomeDisplay()) {
                this.mStackSupervisor.setFocusStackUnchecked(reason, this);
            }
            if (task != null) {
                insertTaskAtTop(task, null);
            } else {
                task = topTask();
            }
            if (task != null) {
                this.mWindowManager.moveTaskToTop(task.taskId);
            }
        }
    }

    boolean isFocusable() {
        if (StackId.canReceiveKeys(this.mStackId)) {
            return SHOW_APP_STARTING_PREVIEW;
        }
        ActivityRecord r = topRunningActivityLocked();
        return r != null ? r.isFocusable() : false;
    }

    final boolean isAttached() {
        return this.mStacks != null ? SHOW_APP_STARTING_PREVIEW : false;
    }

    void findTaskLocked(ActivityRecord target, FindTaskResult result) {
        Intent intent = target.intent;
        ActivityInfo info = target.info;
        ComponentName cls = intent.getComponent();
        if (info.targetActivity != null) {
            cls = new ComponentName(info.packageName, info.targetActivity);
        }
        int userId = UserHandle.getUserId(info.applicationInfo.uid);
        boolean isDocument = (intent != null ? STACK_VISIBLE : STACK_INVISIBLE) & intent.isDocument();
        Object data = isDocument ? intent.getData() : null;
        if (ActivityManagerDebugConfig.DEBUG_TASKS) {
            Slog.d(TAG_TASKS, "Looking for task of " + target + " in " + this);
        }
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            TaskRecord task = (TaskRecord) this.mTaskHistory.get(taskNdx);
            if (task.voiceSession != null) {
                if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                    Slog.d(TAG_TASKS, "Skipping " + task + ": voice session");
                }
            } else if (task.userId == userId && task.multiLaunchId == target.multiLaunchId) {
                ActivityRecord r = task.getTopActivity();
                if (r == null || r.finishing || r.userId != userId || r.launchMode == MAX_STOPPING_TO_FORCE) {
                    if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                        Slog.d(TAG_TASKS, "Skipping " + task + ": mismatch root " + r);
                    }
                } else if (r.mActivityType == target.mActivityType) {
                    Intent taskIntent = task.intent;
                    Intent affinityIntent = task.affinityIntent;
                    boolean taskIsDocument;
                    Object data2;
                    if (taskIntent != null && taskIntent.isDocument()) {
                        taskIsDocument = SHOW_APP_STARTING_PREVIEW;
                        data2 = taskIntent.getData();
                    } else if (affinityIntent == null || !affinityIntent.isDocument()) {
                        taskIsDocument = false;
                        data2 = null;
                    } else {
                        taskIsDocument = SHOW_APP_STARTING_PREVIEW;
                        data2 = affinityIntent.getData();
                    }
                    if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                        Slog.d(TAG_TASKS, "Comparing existing cls=" + taskIntent.getComponent().flattenToShortString() + "/aff=" + r.task.rootAffinity + " to new cls=" + intent.getComponent().flattenToShortString() + "/aff=" + info.taskAffinity);
                    }
                    if (taskIntent != null && taskIntent.getComponent() != null && taskIntent.getComponent().compareTo(cls) == 0 && Objects.equals(data, r9)) {
                        if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                            Slog.d(TAG_TASKS, "Found matching class!");
                        }
                        if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                            Slog.d(TAG_TASKS, "For Intent " + intent + " bringing to top: " + r.intent);
                        }
                        result.r = r;
                        result.matchedByRootAffinity = false;
                        return;
                    } else if (affinityIntent != null && affinityIntent.getComponent() != null && affinityIntent.getComponent().compareTo(cls) == 0 && Objects.equals(data, r9)) {
                        if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                            Slog.d(TAG_TASKS, "Found matching class!");
                        }
                        if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                            Slog.d(TAG_TASKS, "For Intent " + intent + " bringing to top: " + r.intent);
                        }
                        result.r = r;
                        result.matchedByRootAffinity = false;
                        return;
                    } else if (isDocument || taskIsDocument || result.r != null || !task.canMatchRootAffinity()) {
                        if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                            Slog.d(TAG_TASKS, "Not a match: " + task);
                        }
                    } else if (task.rootAffinity.equals(target.taskAffinity)) {
                        if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                            Slog.d(TAG_TASKS, "Found matching affinity candidate!");
                        }
                        result.r = r;
                        result.matchedByRootAffinity = SHOW_APP_STARTING_PREVIEW;
                    }
                } else if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                    Slog.d(TAG_TASKS, "Skipping " + task + ": mismatch activity type");
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
            TaskRecord task = (TaskRecord) this.mTaskHistory.get(taskNdx);
            boolean notCurrentUserTask = this.mStackSupervisor.isCurrentProfileLocked(task.userId) ? false : SHOW_APP_STARTING_PREVIEW;
            ArrayList<ActivityRecord> activities = task.mActivities;
            for (int activityNdx = activities.size() - 1; activityNdx >= 0; activityNdx--) {
                ActivityRecord r = (ActivityRecord) activities.get(activityNdx);
                if (!(notCurrentUserTask && (r.info.flags & DumpState.DUMP_PROVIDERS) == 0) && !r.finishing && r.userId == userId && (r.intent.getHwFlags() & STACK_VISIBLE) == (intent.getHwFlags() & STACK_VISIBLE)) {
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
            int i = STACK_INVISIBLE;
            while (i < index) {
                TaskRecord task = (TaskRecord) this.mTaskHistory.get(i);
                ensureActivitiesVisibleLockedForSwitchUser(task);
                if (this.mStackSupervisor.isCurrentProfileLocked(task.userId) || task.topRunningActivityLocked() != null) {
                    if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                        Slog.d(TAG_TASKS, "switchUserLocked: stack=" + getStackId() + " moving " + task + " to top");
                    }
                    this.mTaskHistory.remove(i);
                    this.mTaskHistory.add(task);
                    index--;
                } else {
                    i += STACK_VISIBLE;
                }
            }
        }
    }

    void ensureActivitiesVisibleLockedForSwitchUser(TaskRecord task) {
        if (!this.mStackSupervisor.isCurrentProfileLocked(task.userId)) {
            ActivityRecord top = task.getTopActivity();
            if (top != null && top != task.topRunningActivityLocked() && top.visible) {
                if (top.state == ActivityState.STOPPING || top.state == ActivityState.STOPPED) {
                    Flog.i(PAUSE_TIMEOUT_MSG, "Making invisible for switch user:  top: " + top + ", finishing: " + top.finishing + " state: " + top.state);
                    try {
                        setVisible(top, false);
                        switch (-getcom-android-server-am-ActivityStack$ActivityStateSwitchesValues()[top.state.ordinal()]) {
                            case H.ADD_STARTING /*5*/:
                            case H.REMOVE_STARTING /*6*/:
                                if (top.app != null && top.app.thread != null) {
                                    top.app.thread.scheduleWindowVisibility(top.appToken, false);
                                }
                            default:
                        }
                    } catch (Exception e) {
                        Slog.w(TAG, "for switch user Exception thrown making hidden: " + top.intent.getComponent(), e);
                    }
                }
            }
        }
    }

    void minimalResumeActivityLocked(ActivityRecord r) {
        r.state = ActivityState.RESUMED;
        if (ActivityManagerDebugConfig.DEBUG_STATES) {
            Slog.v(TAG_STATES, "Moving to RESUMED: " + r + " (starting new instance)" + " callers=" + Debug.getCallers(5));
        }
        if (!this.mService.mActivityStarter.mCurActivityPkName.equals(r.packageName)) {
            Jlog.d(142, r.packageName, r.app.pid, "");
            LogPower.push(HdmiCecKeycode.CEC_KEYCODE_F1_BLUE, r.packageName);
            if (this.mRuningBackground.booleanValue()) {
                LogPower.push(HdmiCecKeycode.CEC_KEYCODE_F2_RED, this.mService.mActivityStarter.mCurActivityPkName);
                this.mRuningBackground = Boolean.valueOf(false);
            }
            this.mService.mActivityStarter.mCurActivityPkName = r.packageName;
        }
        this.mResumedActivity = r;
        r.task.touchActiveTime();
        this.mRecentTasks.addLocked(r.task);
        completeResumeLocked(r);
        this.mStackSupervisor.checkReadyForSleepLocked();
        setLaunchTime(r);
        if (ActivityManagerDebugConfig.DEBUG_SAVED_STATE) {
            Slog.i(TAG_SAVED_STATE, "Launch completed; removing icicle of " + r.icicle);
        }
    }

    void addRecentActivityLocked(ActivityRecord r) {
        if (r != null) {
            this.mRecentTasks.addLocked(r.task);
            r.task.touchActiveTime();
        }
    }

    private void startLaunchTraces(String packageName) {
        if (this.mFullyDrawnStartTime != ACTIVITY_INACTIVE_RESET_TIME) {
            Trace.asyncTraceEnd(64, "drawing", STACK_INVISIBLE);
        }
        Trace.asyncTraceBegin(64, "launching: " + packageName, STACK_INVISIBLE);
        Trace.asyncTraceBegin(64, "drawing", STACK_INVISIBLE);
    }

    private void stopFullyDrawnTraceIfNeeded() {
        if (this.mFullyDrawnStartTime != ACTIVITY_INACTIVE_RESET_TIME && this.mLaunchStartTime == ACTIVITY_INACTIVE_RESET_TIME) {
            Trace.asyncTraceEnd(64, "drawing", STACK_INVISIBLE);
            this.mFullyDrawnStartTime = ACTIVITY_INACTIVE_RESET_TIME;
        }
    }

    void setLaunchTime(ActivityRecord r) {
        long uptimeMillis;
        if (r.displayStartTime == ACTIVITY_INACTIVE_RESET_TIME) {
            uptimeMillis = SystemClock.uptimeMillis();
            r.displayStartTime = uptimeMillis;
            r.fullyDrawnStartTime = uptimeMillis;
            this.mshortComponentName = r.shortComponentName;
            Jlog.d(43, r.shortComponentName, "");
            if (r.task != null) {
                r.task.isLaunching = SHOW_APP_STARTING_PREVIEW;
            }
            if (this.mLaunchStartTime == ACTIVITY_INACTIVE_RESET_TIME) {
                startLaunchTraces(r.packageName);
                uptimeMillis = r.displayStartTime;
                this.mFullyDrawnStartTime = uptimeMillis;
                this.mLaunchStartTime = uptimeMillis;
            }
        } else if (this.mLaunchStartTime == ACTIVITY_INACTIVE_RESET_TIME) {
            startLaunchTraces(r.packageName);
            uptimeMillis = SystemClock.uptimeMillis();
            this.mFullyDrawnStartTime = uptimeMillis;
            this.mLaunchStartTime = uptimeMillis;
        }
    }

    void clearLaunchTime(ActivityRecord r) {
        if (this.mStackSupervisor.mWaitingActivityLaunched.isEmpty()) {
            r.fullyDrawnStartTime = ACTIVITY_INACTIVE_RESET_TIME;
            r.displayStartTime = ACTIVITY_INACTIVE_RESET_TIME;
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
            Slog.d(TAG, "awakeFromSleepingLocked: previously pausing activity didn't pause");
            activityPausedLocked(this.mPausingActivity.appToken, SHOW_APP_STARTING_PREVIEW);
        }
    }

    void updateActivityApplicationInfoLocked(ApplicationInfo aInfo) {
        if (aInfo != null) {
            String packageName = aInfo.packageName;
            for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
                List<ActivityRecord> activities = ((TaskRecord) this.mTaskHistory.get(taskNdx)).mActivities;
                for (int activityNdx = activities.size() - 1; activityNdx >= 0; activityNdx--) {
                    if (packageName.equals(((ActivityRecord) activities.get(activityNdx)).packageName)) {
                        ((ActivityRecord) activities.get(activityNdx)).info.applicationInfo = aInfo;
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
            startPausingLocked(false, SHOW_APP_STARTING_PREVIEW, false, false);
            return SHOW_APP_STARTING_PREVIEW;
        } else if (this.mPausingActivity != null) {
            if (ActivityManagerDebugConfig.DEBUG_PAUSE) {
                Slog.v(TAG_PAUSE, "Sleep still waiting to pause " + this.mPausingActivity);
            }
            return SHOW_APP_STARTING_PREVIEW;
        } else if (!hasVisibleBehindActivity()) {
            return false;
        } else {
            ActivityRecord r = getVisibleBehindActivity();
            this.mStackSupervisor.mStoppingActivities.add(r);
            if (ActivityManagerDebugConfig.DEBUG_STATES) {
                Slog.v(TAG_STATES, "Sleep still waiting to stop visible behind " + r);
            }
            return SHOW_APP_STARTING_PREVIEW;
        }
    }

    void goToSleep() {
        ensureActivitiesVisibleLocked(null, STACK_INVISIBLE, false);
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            ArrayList<ActivityRecord> activities = ((TaskRecord) this.mTaskHistory.get(taskNdx)).mActivities;
            for (int activityNdx = activities.size() - 1; activityNdx >= 0; activityNdx--) {
                ActivityRecord r = (ActivityRecord) activities.get(activityNdx);
                if (!(r.state == ActivityState.STOPPING || r.state == ActivityState.STOPPED || r.state == ActivityState.PAUSED)) {
                    if (r.state != ActivityState.PAUSING) {
                    }
                }
                r.setSleeping(SHOW_APP_STARTING_PREVIEW);
            }
        }
    }

    public final Bitmap screenshotActivitiesLocked(ActivityRecord who) {
        if (ActivityManagerDebugConfig.DEBUG_SCREENSHOTS) {
            Slog.d(TAG_SCREENSHOTS, "screenshotActivitiesLocked: " + who);
        }
        if (who.noDisplay) {
            if (ActivityManagerDebugConfig.DEBUG_SCREENSHOTS) {
                Slog.d(TAG_SCREENSHOTS, "\tNo display");
            }
            return null;
        } else if (isHomeStack()) {
            if (ActivityManagerDebugConfig.DEBUG_SCREENSHOTS) {
                Slog.d(TAG_SCREENSHOTS, "\tHome stack");
            }
            return null;
        } else {
            int w = this.mService.mThumbnailWidth;
            int h = this.mService.mThumbnailHeight;
            if (w > 0) {
                if (ActivityManagerDebugConfig.DEBUG_SCREENSHOTS) {
                    Slog.d(TAG_SCREENSHOTS, "\tTaking screenshot");
                }
                return this.mWindowManager.screenshotApplications(who.appToken, STACK_INVISIBLE, -1, -1, this.mService.mFullscreenThumbnailScale);
            }
            Slog.e(TAG, "Invalid thumbnail dimensions: " + w + "x" + h);
            return null;
        }
    }

    final boolean startPausingLocked(boolean userLeaving, boolean uiSleeping, boolean resuming, boolean dontWait) {
        if (this.mPausingActivity != null) {
            Slog.wtf(TAG, "Going to pause when pause is already pending for " + this.mPausingActivity + " state=" + this.mPausingActivity.state);
            if (!this.mService.isSleepingLocked()) {
                completePauseLocked(false);
            }
        }
        ActivityRecord prev = this.mResumedActivity;
        if (prev == null) {
            if (!resuming) {
                Slog.wtf(TAG, "Trying to pause when nothing is resumed");
                this.mStackSupervisor.resumeFocusedStackTopActivityLocked();
            }
            return false;
        }
        if (this.mActivityContainer.mParentActivity == null) {
            this.mStackSupervisor.pauseChildStacks(prev, userLeaving, uiSleeping, resuming, dontWait);
        }
        if (ActivityManagerDebugConfig.DEBUG_STATES) {
            Slog.v(TAG_STATES, "Moving to PAUSING: " + prev);
        }
        Flog.i(PAUSE_TIMEOUT_MSG, "Start pausing: " + prev);
        this.mResumedActivity = null;
        this.mPausingActivity = prev;
        this.mLastPausedActivity = prev;
        ActivityRecord activityRecord = ((prev.intent.getFlags() & 1073741824) == 0 && (prev.info.flags & DumpState.DUMP_PACKAGES) == 0) ? null : prev;
        this.mLastNoHistoryActivity = activityRecord;
        prev.state = ActivityState.PAUSING;
        prev.task.touchActiveTime();
        clearLaunchTime(prev);
        ActivityRecord next = this.mStackSupervisor.topRunningActivityLocked();
        if (prev.app != this.mService.mHomeProcess && this.mService.mHasRecents) {
            if (!(next == null || next.noDisplay || next.task != prev.task)) {
                if (uiSleeping) {
                }
            }
            prev.mUpdateTaskThumbnailWhenHidden = SHOW_APP_STARTING_PREVIEW;
        }
        stopFullyDrawnTraceIfNeeded();
        this.mService.updateCpuStats();
        if (prev.app == null || prev.app.thread == null) {
            this.mPausingActivity = null;
            this.mLastPausedActivity = null;
            this.mLastNoHistoryActivity = null;
        } else {
            if (ActivityManagerDebugConfig.DEBUG_PAUSE) {
                Slog.v(TAG_PAUSE, "Enqueueing pending pause: " + prev);
            }
            try {
                Object[] objArr = new Object[MAX_STOPPING_TO_FORCE];
                objArr[STACK_INVISIBLE] = Integer.valueOf(prev.userId);
                objArr[STACK_VISIBLE] = Integer.valueOf(System.identityHashCode(prev));
                objArr[STACK_VISIBLE_ACTIVITY_BEHIND] = prev.shortComponentName;
                EventLog.writeEvent(EventLogTags.AM_PAUSE_ACTIVITY, objArr);
                this.mService.updateUsageStats(prev, false);
                if (Jlog.isPerfTest()) {
                    Jlog.i(2024, Intent.toPkgClsString(prev.realActivity, "who"));
                }
                prev.app.thread.schedulePauseActivity(prev.appToken, prev.finishing, userLeaving, prev.configChangeFlags, dontWait);
            } catch (Exception e) {
                Slog.w(TAG, "Exception thrown during pause", e);
                this.mPausingActivity = null;
                this.mLastPausedActivity = null;
                this.mLastNoHistoryActivity = null;
            }
        }
        if (!(uiSleeping || this.mService.isSleepingOrShuttingDownLocked())) {
            this.mStackSupervisor.acquireLaunchWakelock();
        }
        if (this.mPausingActivity != null) {
            if (!uiSleeping) {
                prev.pauseKeyDispatchingLocked();
            } else if (ActivityManagerDebugConfig.DEBUG_PAUSE) {
                Slog.v(TAG_PAUSE, "Key dispatch not paused for screen off");
            }
            if (dontWait) {
                completePauseLocked(false);
                return false;
            }
            Message msg = this.mHandler.obtainMessage(PAUSE_TIMEOUT_MSG);
            msg.obj = prev;
            prev.pauseTime = SystemClock.uptimeMillis();
            this.mHandler.sendMessageDelayed(msg, 500);
            if (ActivityManagerDebugConfig.DEBUG_PAUSE) {
                Slog.v(TAG_PAUSE, "Waiting for pause to complete...");
            }
            return SHOW_APP_STARTING_PREVIEW;
        }
        if (ActivityManagerDebugConfig.DEBUG_PAUSE) {
            Slog.v(TAG_PAUSE, "Activity not running, resuming next.");
        }
        if (!resuming) {
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
            this.mHandler.removeMessages(PAUSE_TIMEOUT_MSG, r);
            if (this.mPausingActivity == r) {
                Flog.i(PAUSE_TIMEOUT_MSG, "Moving to PAUSED: " + r + (timeout ? " (due to timeout)" : " (pause complete)"));
                completePauseLocked(SHOW_APP_STARTING_PREVIEW);
                return;
            }
            Object[] objArr = new Object[4];
            objArr[STACK_INVISIBLE] = Integer.valueOf(r.userId);
            objArr[STACK_VISIBLE] = Integer.valueOf(System.identityHashCode(r));
            objArr[STACK_VISIBLE_ACTIVITY_BEHIND] = r.shortComponentName;
            objArr[MAX_STOPPING_TO_FORCE] = this.mPausingActivity != null ? this.mPausingActivity.shortComponentName : "(none)";
            EventLog.writeEvent(EventLogTags.AM_FAILED_TO_PAUSE, objArr);
            if (r.state == ActivityState.PAUSING) {
                r.state = ActivityState.PAUSED;
                if (r.finishing) {
                    Flog.i(PAUSE_TIMEOUT_MSG, "Executing finish of failed to pause activity: " + r);
                    finishCurrentActivityLocked(r, STACK_VISIBLE_ACTIVITY_BEHIND, false);
                } else {
                    Flog.i(PAUSE_TIMEOUT_MSG, "Not process of failed to pause activity: " + r);
                }
            }
        }
        this.mStackSupervisor.ensureActivitiesVisibleLocked(null, STACK_INVISIBLE, false);
    }

    final void activityResumedLocked(IBinder token) {
        ActivityRecord r = ActivityRecord.forTokenLocked(token);
        if (ActivityManagerDebugConfig.DEBUG_SAVED_STATE) {
            Slog.i(TAG_STATES, "Resumed activity; dropping state of: " + r);
        }
        r.icicle = null;
        r.haveState = false;
    }

    final void activityStoppedLocked(ActivityRecord r, Bundle icicle, PersistableBundle persistentState, CharSequence description) {
        if (r.state != ActivityState.STOPPING) {
            Slog.i(TAG, "Activity reported stop, but no longer stopping: " + r + " state:" + r.state + " Callers=" + Debug.getCallers(4));
            this.mHandler.removeMessages(STOP_TIMEOUT_MSG, r);
            return;
        }
        if (persistentState != null) {
            r.persistentState = persistentState;
            this.mService.notifyTaskPersisterLocked(r.task, false);
        }
        if (ActivityManagerDebugConfig.DEBUG_SAVED_STATE) {
            Slog.i(TAG_SAVED_STATE, "Saving icicle of " + r + ": " + icicle);
        }
        if (icicle != null) {
            r.icicle = icicle;
            r.haveState = SHOW_APP_STARTING_PREVIEW;
            r.launchCount = STACK_INVISIBLE;
            r.updateThumbnailLocked(null, description);
        }
        if (!r.stopped) {
            if (ActivityManagerDebugConfig.DEBUG_STATES) {
                Slog.v(TAG_STATES, "Moving to STOPPED: " + r + " (stop complete)");
            }
            this.mHandler.removeMessages(STOP_TIMEOUT_MSG, r);
            r.stopped = SHOW_APP_STARTING_PREVIEW;
            r.state = ActivityState.STOPPED;
            this.mWindowManager.notifyAppStopped(r.appToken, SHOW_APP_STARTING_PREVIEW);
            if (getVisibleBehindActivity() == r) {
                this.mStackSupervisor.requestVisibleBehindLocked(r, false);
            }
            if (r.finishing) {
                r.clearOptionsLocked();
            } else if (r.deferRelaunchUntilPaused) {
                destroyActivityLocked(r, SHOW_APP_STARTING_PREVIEW, "stop-config");
                this.mStackSupervisor.mActivityLaunchTrack = " stop-config";
                this.mStackSupervisor.resumeFocusedStackTopActivityLocked();
            } else {
                this.mStackSupervisor.updatePreviousProcessLocked(r);
            }
        }
    }

    private void completePauseLocked(boolean resumeNext) {
        ActivityRecord prev = this.mPausingActivity;
        if (ActivityManagerDebugConfig.DEBUG_PAUSE) {
            Slog.v(TAG_PAUSE, "Complete pause: " + prev);
        }
        if (prev != null) {
            setSoundEffectState(false, prev.packageName, false, null);
        }
        this.mRuningBackground = Boolean.valueOf(SHOW_APP_STARTING_PREVIEW);
        if (prev != null) {
            boolean wasStopping = prev.state == ActivityState.STOPPING ? SHOW_APP_STARTING_PREVIEW : false;
            prev.state = ActivityState.PAUSED;
            if (prev.finishing) {
                if (ActivityManagerDebugConfig.DEBUG_PAUSE) {
                    Slog.v(TAG_PAUSE, "Executing finish of activity: " + prev);
                }
                if (prev.info != null && prev.task.isOverHomeStack() && prev.frontOfTask) {
                    this.mService.getRecordCust().appExitRecord(prev.info.packageName, "finish");
                }
                prev = finishCurrentActivityLocked(prev, STACK_VISIBLE_ACTIVITY_BEHIND, false);
                this.mRuningBackground = Boolean.valueOf(false);
            } else if (prev.app != null) {
                if (ActivityManagerDebugConfig.DEBUG_PAUSE) {
                    Slog.v(TAG_PAUSE, "Enqueue pending stop if needed: " + prev + " wasStopping=" + wasStopping + " visible=" + prev.visible);
                }
                if (this.mStackSupervisor.mWaitingVisibleActivities.remove(prev) && (ActivityManagerDebugConfig.DEBUG_SWITCH || ActivityManagerDebugConfig.DEBUG_PAUSE)) {
                    Slog.v(TAG_PAUSE, "Complete pause, no longer waiting: " + prev);
                }
                if (prev.deferRelaunchUntilPaused) {
                    if (ActivityManagerDebugConfig.DEBUG_PAUSE) {
                        Slog.v(TAG_PAUSE, "Re-launching after pause: " + prev);
                    }
                    relaunchActivityLocked(prev, prev.configChangeFlags, false, prev.preserveWindowOnDeferredRelaunch);
                } else if (wasStopping) {
                    prev.state = ActivityState.STOPPING;
                } else if (!(prev.visible || hasVisibleBehindActivity()) || this.mService.isSleepingOrShuttingDownLocked()) {
                    addToStopping(prev, SHOW_APP_STARTING_PREVIEW);
                }
            } else {
                if (ActivityManagerDebugConfig.DEBUG_PAUSE) {
                    Slog.v(TAG_PAUSE, "App died during pause, not stopping: " + prev);
                }
                prev = null;
            }
            if (prev != null) {
                prev.stopFreezingScreenLocked(SHOW_APP_STARTING_PREVIEW);
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
            if (prev.app != null && prev.cpuTimeAtResume > ACTIVITY_INACTIVE_RESET_TIME && this.mService.mBatteryStatsService.isOnBattery()) {
                long diff = this.mService.mProcessCpuTracker.getCpuTimeForPid(prev.app.pid) - prev.cpuTimeAtResume;
                if (diff > ACTIVITY_INACTIVE_RESET_TIME) {
                    BatteryStatsImpl bsi = this.mService.mBatteryStatsService.getActiveStatistics();
                    synchronized (bsi) {
                        Proc ps = bsi.getProcessStatsLocked(prev.info.applicationInfo.uid, prev.info.packageName);
                        if (ps != null) {
                            ps.addForegroundTimeLocked(diff);
                        }
                    }
                }
            }
            prev.cpuTimeAtResume = ACTIVITY_INACTIVE_RESET_TIME;
        }
        if (this.mStackSupervisor.mAppVisibilitiesChangedSinceLastPause) {
            this.mService.notifyTaskStackChangedLocked();
            this.mStackSupervisor.mAppVisibilitiesChangedSinceLastPause = false;
        }
        this.mStackSupervisor.ensureActivitiesVisibleLocked(null, STACK_INVISIBLE, false);
    }

    private void addToStopping(ActivityRecord r, boolean immediate) {
        if (!this.mStackSupervisor.mStoppingActivities.contains(r)) {
            this.mStackSupervisor.mStoppingActivities.add(r);
        }
        boolean forceIdle = this.mStackSupervisor.mStoppingActivities.size() <= MAX_STOPPING_TO_FORCE ? (!r.frontOfTask || this.mTaskHistory.size() > STACK_VISIBLE) ? false : SHOW_APP_STARTING_PREVIEW : SHOW_APP_STARTING_PREVIEW;
        if (immediate || forceIdle) {
            if (ActivityManagerDebugConfig.DEBUG_PAUSE) {
                Slog.v(TAG_PAUSE, "Scheduling idle now: forceIdle=" + forceIdle + "immediate=" + immediate);
            }
            this.mStackSupervisor.scheduleIdleLocked();
            return;
        }
        this.mStackSupervisor.checkReadyForSleepLocked();
    }

    private void completeResumeLocked(ActivityRecord next) {
        next.visible = SHOW_APP_STARTING_PREVIEW;
        next.idle = false;
        next.results = null;
        next.newIntents = null;
        next.stopped = false;
        if (next.isHomeActivity()) {
            ProcessRecord app = ((ActivityRecord) next.task.mActivities.get(STACK_INVISIBLE)).app;
            if (!(app == null || app == this.mService.mHomeProcess)) {
                this.mService.mHomeProcess = app;
                this.mService.reportHomeProcess(this.mService.mHomeProcess);
            }
        }
        if (next.nowVisible) {
            this.mStackSupervisor.reportActivityVisibleLocked(next);
            this.mStackSupervisor.notifyActivityDrawnForKeyguard();
        }
        this.mStackSupervisor.scheduleIdleTimeoutLocked(next);
        this.mStackSupervisor.reportResumedActivityLocked(next);
        this.mService.setFocusedActivityLockedForNavi(next);
        next.resumeKeyDispatchingLocked();
        this.mNoAnimActivities.clear();
        setSoundEffectState(false, next.packageName, SHOW_APP_STARTING_PREVIEW, null);
        if (next.app != null) {
            next.cpuTimeAtResume = this.mService.mProcessCpuTracker.getCpuTimeForPid(next.app.pid);
        } else {
            next.cpuTimeAtResume = ACTIVITY_INACTIVE_RESET_TIME;
        }
        next.returningOptions = null;
        if (getVisibleBehindActivity() == next) {
            setVisibleBehindActivity(null);
        }
        Flog.i(PAUSE_TIMEOUT_MSG, "completedResumed: " + next + ", launchTrack: " + this.mStackSupervisor.mActivityLaunchTrack);
        this.mStackSupervisor.mActivityLaunchTrack = "";
    }

    private void setVisible(ActivityRecord r, boolean visible) {
        r.visible = visible;
        if (!visible && r.mUpdateTaskThumbnailWhenHidden) {
            r.updateThumbnailLocked(r.task.stack.screenshotActivitiesLocked(r), null);
            r.mUpdateTaskThumbnailWhenHidden = false;
        }
        this.mWindowManager.setAppVisibility(r.appToken, visible);
        ArrayList<ActivityContainer> containers = r.mChildContainers;
        for (int containerNdx = containers.size() - 1; containerNdx >= 0; containerNdx--) {
            ((ActivityContainer) containers.get(containerNdx)).setVisible(visible);
        }
        this.mStackSupervisor.mAppVisibilitiesChangedSinceLastPause = SHOW_APP_STARTING_PREVIEW;
    }

    ActivityRecord findNextTranslucentActivity(ActivityRecord r) {
        TaskRecord task = r.task;
        if (task == null) {
            return null;
        }
        ActivityStack stack = task.stack;
        if (stack == null) {
            return null;
        }
        int taskNdx = stack.mTaskHistory.indexOf(task);
        int activityNdx = task.mActivities.indexOf(r) + STACK_VISIBLE;
        int numStacks = this.mStacks.size();
        for (int stackNdx = this.mStacks.indexOf(stack); stackNdx < numStacks; stackNdx += STACK_VISIBLE) {
            ActivityStack historyStack = (ActivityStack) this.mStacks.get(stackNdx);
            ArrayList<TaskRecord> tasks = historyStack.mTaskHistory;
            int numTasks = tasks.size();
            for (taskNdx = 
            /* Method generation error in method: com.android.server.am.ActivityStack.findNextTranslucentActivity(com.android.server.am.ActivityRecord):com.android.server.am.ActivityRecord
jadx.core.utils.exceptions.CodegenException: Error generate insn: PHI: (r11_1 'taskNdx' int) = (r11_0 'taskNdx' int), (r11_4 'taskNdx' int) binds: {(r11_4 'taskNdx' int)=B:26:0x0065, (r11_0 'taskNdx' int)=B:8:0x000c} in method: com.android.server.am.ActivityStack.findNextTranslucentActivity(com.android.server.am.ActivityRecord):com.android.server.am.ActivityRecord
	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:225)
	at jadx.core.codegen.RegionGen.makeLoop(RegionGen.java:184)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:61)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:87)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:53)
	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:93)
	at jadx.core.codegen.RegionGen.makeLoop(RegionGen.java:190)
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
	at jadx.core.codegen.MethodGen.addInstructions(MethodGen.java:177)
	at jadx.core.codegen.ClassGen.addMethod(ClassGen.java:324)
	at jadx.core.codegen.ClassGen.addMethods(ClassGen.java:263)
	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:226)
	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:116)
	at jadx.core.codegen.ClassGen.makeClass(ClassGen.java:81)
	at jadx.core.codegen.CodeGen.visit(CodeGen.java:19)
	at jadx.core.ProcessClass.process(ProcessClass.java:43)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.CodegenException: Unknown instruction: PHI in method: com.android.server.am.ActivityStack.findNextTranslucentActivity(com.android.server.am.ActivityRecord):com.android.server.am.ActivityRecord
	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:512)
	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:219)
	... 28 more
 */

            ActivityStack getNextFocusableStackLocked() {
                ArrayList<ActivityStack> stacks = this.mStacks;
                ActivityRecord parent = this.mActivityContainer.mParentActivity;
                if (parent != null) {
                    stacks = parent.task.stack.mStacks;
                }
                if (stacks != null) {
                    for (int i = stacks.size() - 1; i >= 0; i--) {
                        ActivityStack stack = (ActivityStack) stacks.get(i);
                        if (stack != this && stack.isFocusable() && stack.getStackVisibilityLocked(null) != 0) {
                            return stack;
                        }
                    }
                }
                return null;
            }

            private boolean hasFullscreenTask() {
                for (int i = this.mTaskHistory.size() - 1; i >= 0; i--) {
                    if (((TaskRecord) this.mTaskHistory.get(i)).mFullscreen) {
                        return SHOW_APP_STARTING_PREVIEW;
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
                            if (!isHomeStack() && r.frontOfTask && task.isOverHomeStack() && stackBehindId != 0) {
                                return false;
                            }
                        }
                    }
                }
                return SHOW_APP_STARTING_PREVIEW;
            }

            int getStackVisibilityLocked(ActivityRecord starting) {
                if (!isAttached()) {
                    return STACK_INVISIBLE;
                }
                if (this.mStackSupervisor.isFrontStack(this) || this.mStackSupervisor.isFocusedStack(this)) {
                    return STACK_VISIBLE;
                }
                int stackIndex = this.mStacks.indexOf(this);
                if (stackIndex == this.mStacks.size() - 1) {
                    Slog.wtf(TAG, "Stack=" + this + " isn't front stack but is at the top of the stack list");
                    return STACK_INVISIBLE;
                }
                if ((this.mService.mLockScreenShown == STACK_VISIBLE_ACTIVITY_BEHIND ? SHOW_APP_STARTING_PREVIEW : false) && !StackId.isAllowedOverLockscreen(this.mStackId)) {
                    return STACK_INVISIBLE;
                }
                ActivityStack focusedStack = this.mStackSupervisor.getFocusedStack();
                int focusedStackId = focusedStack.mStackId;
                if (this.mStackId == STACK_VISIBLE && hasVisibleBehindActivity() && focusedStackId == 0 && !focusedStack.topActivity().fullscreen) {
                    return STACK_VISIBLE_ACTIVITY_BEHIND;
                }
                if (this.mStackId == MAX_STOPPING_TO_FORCE) {
                    int i;
                    ActivityRecord r = focusedStack.topRunningActivityLocked();
                    TaskRecord task = r != null ? r.task : null;
                    if (task == null || task.canGoInDockedStack() || task.isHomeTask()) {
                        i = STACK_VISIBLE;
                    } else {
                        i = STACK_INVISIBLE;
                    }
                    return i;
                }
                int stackBehindFocusedIndex = this.mStacks.indexOf(focusedStack) - 1;
                while (stackBehindFocusedIndex >= 0 && ((ActivityStack) this.mStacks.get(stackBehindFocusedIndex)).topRunningActivityLocked() == null) {
                    stackBehindFocusedIndex--;
                }
                if ((focusedStackId == MAX_STOPPING_TO_FORCE || focusedStackId == 4) && stackIndex == stackBehindFocusedIndex) {
                    return STACK_VISIBLE;
                }
                int stackBehindFocusedId = stackBehindFocusedIndex >= 0 ? ((ActivityStack) this.mStacks.get(stackBehindFocusedIndex)).mStackId : -1;
                if (focusedStackId == STACK_VISIBLE && focusedStack.isStackTranslucent(starting, stackBehindFocusedId)) {
                    if (stackIndex == stackBehindFocusedIndex) {
                        return STACK_VISIBLE;
                    }
                    if (stackBehindFocusedIndex >= 0 && ((stackBehindFocusedId == MAX_STOPPING_TO_FORCE || stackBehindFocusedId == 4) && stackIndex == stackBehindFocusedIndex - 1)) {
                        return STACK_VISIBLE;
                    }
                }
                if (StackId.isStaticStack(this.mStackId)) {
                    return STACK_INVISIBLE;
                }
                for (int i2 = stackIndex + STACK_VISIBLE; i2 < this.mStacks.size(); i2 += STACK_VISIBLE) {
                    ActivityStack stack = (ActivityStack) this.mStacks.get(i2);
                    if (stack.mFullscreen || stack.hasFullscreenTask()) {
                        if (!StackId.isDynamicStacksVisibleBehindAllowed(stack.mStackId)) {
                            return STACK_INVISIBLE;
                        }
                        if (!stack.isStackTranslucent(starting, -1)) {
                            return STACK_INVISIBLE;
                        }
                    }
                }
                return STACK_VISIBLE;
            }

            final int rankTaskLayers(int baseLayer) {
                int taskNdx = this.mTaskHistory.size() - 1;
                int layer = STACK_INVISIBLE;
                while (taskNdx >= 0) {
                    int layer2;
                    TaskRecord task = (TaskRecord) this.mTaskHistory.get(taskNdx);
                    ActivityRecord r = task.topRunningActivityLocked();
                    if (r == null || r.finishing || !r.visible) {
                        task.mLayerRank = -1;
                        layer2 = layer;
                    } else {
                        layer2 = layer + STACK_VISIBLE;
                        task.mLayerRank = baseLayer + layer;
                    }
                    taskNdx--;
                    layer = layer2;
                }
                return layer;
            }

            final void ensureActivitiesVisibleLocked(ActivityRecord starting, int configChanges, boolean preserveWindows) {
                ActivityRecord top = topRunningActivityLocked();
                if (ActivityManagerDebugConfig.DEBUG_VISIBILITY) {
                    Slog.v(TAG_VISIBILITY, "ensureActivitiesVisible behind " + top + " configChanges=0x" + Integer.toHexString(configChanges));
                }
                if (top != null) {
                    checkTranslucentActivityWaiting(top);
                }
                boolean aboveTop = top != null ? SHOW_APP_STARTING_PREVIEW : false;
                int stackVisibility = getStackVisibilityLocked(starting);
                boolean stackInvisible = stackVisibility != STACK_VISIBLE ? SHOW_APP_STARTING_PREVIEW : false;
                boolean stackVisibleBehind = stackVisibility == STACK_VISIBLE_ACTIVITY_BEHIND ? SHOW_APP_STARTING_PREVIEW : false;
                boolean behindFullscreenActivity = stackInvisible;
                boolean resumeNextActivity = this.mStackSupervisor.isFocusedStack(this) ? isInStackLocked(starting) == null ? SHOW_APP_STARTING_PREVIEW : false : false;
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
                                boolean isTop = r == top ? SHOW_APP_STARTING_PREVIEW : false;
                                if (!aboveTop || isTop) {
                                    aboveTop = false;
                                    if (shouldBeVisible(r, behindTranslucentActivity, stackVisibleBehind, visibleBehind, behindFullscreenActivity)) {
                                        if (ActivityManagerDebugConfig.DEBUG_VISIBILITY) {
                                            Slog.v(TAG_VISIBILITY, "Make visible? " + r + " finishing=" + r.finishing + " state=" + r.state);
                                        }
                                        if (r != starting) {
                                            ensureActivityConfigurationLocked(r, STACK_INVISIBLE, preserveWindows);
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
                                            if (handleAlreadyVisible(r)) {
                                                resumeNextActivity = false;
                                            }
                                        } else {
                                            makeVisibleIfNeeded(starting, r);
                                        }
                                        configChanges |= r.configChangeFlags;
                                        behindFullscreenActivity = updateBehindFullscreen(stackInvisible, behindFullscreenActivity, task, r);
                                        if (behindFullscreenActivity && !r.fullscreen) {
                                            behindTranslucentActivity = SHOW_APP_STARTING_PREVIEW;
                                        }
                                    } else {
                                        if (ActivityManagerDebugConfig.DEBUG_VISIBILITY) {
                                            Slog.v(TAG_VISIBILITY, "Make invisible? " + r + " finishing=" + r.finishing + " state=" + r.state + " stackInvisible=" + stackInvisible + " behindFullscreenActivity=" + behindFullscreenActivity + " mLaunchTaskBehind=" + r.mLaunchTaskBehind);
                                        }
                                        makeInvisible(r, visibleBehind);
                                    }
                                }
                            } else if (r.mUpdateTaskThumbnailWhenHidden) {
                                r.updateThumbnailLocked(screenshotActivitiesLocked(r), null);
                                r.mUpdateTaskThumbnailWhenHidden = false;
                            }
                        }
                        activityNdx--;
                    }
                    if (this.mStackId == STACK_VISIBLE_ACTIVITY_BEHIND) {
                        if (stackVisibility == 0) {
                            behindFullscreenActivity = SHOW_APP_STARTING_PREVIEW;
                        } else {
                            behindFullscreenActivity = false;
                        }
                    } else if (this.mStackId == 0) {
                        if (task.isHomeTask()) {
                            if (ActivityManagerDebugConfig.DEBUG_VISIBILITY) {
                                Slog.v(TAG_VISIBILITY, "Home task: at " + task + " stackInvisible=" + stackInvisible + " behindFullscreenActivity=" + behindFullscreenActivity);
                            }
                            behindFullscreenActivity = SHOW_APP_STARTING_PREVIEW;
                        } else if (task.isRecentsTask() && task.getTaskToReturnTo() == 0) {
                            if (ActivityManagerDebugConfig.DEBUG_VISIBILITY) {
                                Slog.v(TAG_VISIBILITY, "Recents task returning to app: at " + task + " stackInvisible=" + stackInvisible + " behindFullscreenActivity=" + behindFullscreenActivity);
                            }
                            behindFullscreenActivity = SHOW_APP_STARTING_PREVIEW;
                        }
                    }
                }
                if (this.mTranslucentActivityWaiting != null && this.mUndrawnActivitiesBelowTopTranslucent.isEmpty()) {
                    notifyActivityDrawnLocked(null);
                }
            }

            private boolean shouldBeVisible(ActivityRecord r, boolean behindTranslucentActivity, boolean stackVisibleBehind, ActivityRecord visibleBehind, boolean behindFullscreenActivity) {
                if (!okToShowLocked(r)) {
                    return false;
                }
                boolean activityVisibleBehind = ((behindTranslucentActivity || stackVisibleBehind) && visibleBehind == r) ? SHOW_APP_STARTING_PREVIEW : false;
                boolean z = (!behindFullscreenActivity || r.mLaunchTaskBehind) ? SHOW_APP_STARTING_PREVIEW : activityVisibleBehind;
                if (this.mService.mSupportsLeanbackOnly && z && r.isRecentsActivity()) {
                    z = this.mStackSupervisor.getStack(MAX_STOPPING_TO_FORCE) == null ? this.mStackSupervisor.isFocusedStack(this) : SHOW_APP_STARTING_PREVIEW;
                }
                return z;
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
                if (isTop || !r.visible) {
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
                        setVisible(r, SHOW_APP_STARTING_PREVIEW);
                    }
                    if (r != starting) {
                        this.mStackSupervisor.startSpecificActivityLocked(r, andResume, false);
                        return SHOW_APP_STARTING_PREVIEW;
                    }
                }
                return false;
            }

            /* JADX WARNING: inconsistent code. */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            private void makeInvisible(ActivityRecord r, ActivityRecord visibleBehind) {
                if (r.visible) {
                    if (ActivityManagerDebugConfig.DEBUG_VISIBILITY) {
                        Slog.v(TAG_VISIBILITY, "Making invisible: " + r + " " + r.state);
                    }
                    try {
                        setVisible(r, false);
                        switch (-getcom-android-server-am-ActivityStack$ActivityStateSwitchesValues()[r.state.ordinal()]) {
                            case STACK_VISIBLE /*1*/:
                            case STACK_VISIBLE_ACTIVITY_BEHIND /*2*/:
                            case MAX_STOPPING_TO_FORCE /*3*/:
                            case H.DO_TRAVERSAL /*4*/:
                                if (visibleBehind != r) {
                                    addToStopping(r, SHOW_APP_STARTING_PREVIEW);
                                    break;
                                } else {
                                    releaseBackgroundResources(r);
                                    break;
                                }
                            case H.ADD_STARTING /*5*/:
                            case H.REMOVE_STARTING /*6*/:
                                if (!(r.app == null || r.app.thread == null)) {
                                    if (ActivityManagerDebugConfig.DEBUG_VISIBILITY) {
                                        Slog.v(TAG_VISIBILITY, "Scheduling invisibility: " + r);
                                    }
                                    r.app.thread.scheduleWindowVisibility(r.appToken, false);
                                    break;
                                }
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
                    return SHOW_APP_STARTING_PREVIEW;
                } else if (isHomeStack() || !r.frontOfTask || !task.isOverHomeStack()) {
                    return behindFullscreenActivity;
                } else {
                    if (ActivityManagerDebugConfig.DEBUG_VISIBILITY) {
                        Slog.v(TAG_VISIBILITY, "Showing home: at " + r + " stackInvisible=" + stackInvisible + " behindFullscreenActivity=" + behindFullscreenActivity);
                    }
                    return SHOW_APP_STARTING_PREVIEW;
                }
            }

            private void makeVisibleIfNeeded(ActivityRecord starting, ActivityRecord r) {
                if (r.state == ActivityState.RESUMED || r == starting) {
                    if (ActivityManagerDebugConfig.DEBUG_VISIBILITY) {
                        Slog.d(TAG_VISIBILITY, "Not making visible, r=" + r + " state=" + r.state + " starting=" + starting);
                    }
                    return;
                }
                if (ActivityManagerDebugConfig.DEBUG_VISIBILITY) {
                    Slog.v(TAG_VISIBILITY, "Making visible and scheduling visibility: " + r);
                }
                try {
                    if (this.mTranslucentActivityWaiting != null) {
                        r.updateOptionsLocked(r.returningOptions);
                        this.mUndrawnActivitiesBelowTopTranslucent.add(r);
                    }
                    setVisible(r, SHOW_APP_STARTING_PREVIEW);
                    r.sleeping = false;
                    r.app.pendingUiClean = SHOW_APP_STARTING_PREVIEW;
                    r.app.thread.scheduleWindowVisibility(r.appToken, SHOW_APP_STARTING_PREVIEW);
                    this.mStackSupervisor.mStoppingActivities.remove(r);
                    this.mStackSupervisor.mGoingToSleepActivities.remove(r);
                } catch (Exception e) {
                    Slog.w(TAG, "Exception thrown making visibile: " + r.intent.getComponent(), e);
                }
                handleAlreadyVisible(r);
            }

            private boolean handleAlreadyVisible(ActivityRecord r) {
                r.stopFreezingScreenLocked(false);
                try {
                    if (r.returningOptions != null) {
                        r.app.thread.scheduleOnNewActivityOptions(r.appToken, r.returningOptions);
                    }
                } catch (RemoteException e) {
                }
                if (r.state == ActivityState.RESUMED) {
                    return SHOW_APP_STARTING_PREVIEW;
                }
                return false;
            }

            void convertActivityToTranslucent(ActivityRecord r) {
                this.mTranslucentActivityWaiting = r;
                this.mUndrawnActivitiesBelowTopTranslucent.clear();
                this.mHandler.sendEmptyMessageDelayed(TRANSLUCENT_TIMEOUT_MSG, TRANSLUCENT_CONVERSION_TIMEOUT);
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
                    this.mHandler.removeMessages(TRANSLUCENT_TIMEOUT_MSG);
                    if (waitingActivity != null) {
                        this.mWindowManager.setWindowOpaque(waitingActivity.appToken, false);
                        if (waitingActivity.app != null && waitingActivity.app.thread != null) {
                            try {
                                IApplicationThread iApplicationThread = waitingActivity.app.thread;
                                IBinder iBinder = waitingActivity.appToken;
                                if (r != null) {
                                    z = SHOW_APP_STARTING_PREVIEW;
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
                boolean aboveTop = SHOW_APP_STARTING_PREVIEW;
                boolean behindFullscreenActivity = false;
                if (getStackVisibilityLocked(null) == 0) {
                    aboveTop = false;
                    behindFullscreenActivity = SHOW_APP_STARTING_PREVIEW;
                }
                for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
                    ArrayList<ActivityRecord> activities = ((TaskRecord) this.mTaskHistory.get(taskNdx)).mActivities;
                    for (int activityNdx = activities.size() - 1; activityNdx >= 0; activityNdx--) {
                        ActivityRecord r = (ActivityRecord) activities.get(activityNdx);
                        if (aboveTop) {
                            if (r == topActivity) {
                                aboveTop = false;
                            }
                            behindFullscreenActivity |= r.fullscreen;
                        } else {
                            if (r.state == ActivityState.INITIALIZING && r.mStartingWindowState == STACK_VISIBLE && behindFullscreenActivity) {
                                Slog.w(TAG_VISIBILITY, "Found orphaned starting window " + r);
                                r.mStartingWindowState = STACK_VISIBLE_ACTIVITY_BEHIND;
                                this.mWindowManager.removeAppStartingWindow(r.appToken);
                            }
                            behindFullscreenActivity |= r.fullscreen;
                        }
                    }
                }
            }

            boolean resumeTopActivityUncheckedLocked(ActivityRecord prev, ActivityOptions options) {
                if (this.mStackSupervisor.inResumeTopActivity) {
                    return false;
                }
                boolean z = false;
                try {
                    this.mStackSupervisor.inResumeTopActivity = SHOW_APP_STARTING_PREVIEW;
                    if (this.mService.mLockScreenShown == STACK_VISIBLE) {
                        this.mService.mLockScreenShown = STACK_INVISIBLE;
                        this.mService.updateSleepIfNeededLocked();
                    }
                    z = resumeTopActivityInnerLocked(prev, options);
                    return z;
                } finally {
                    this.mStackSupervisor.inResumeTopActivity = false;
                }
            }

            private boolean resumeTopActivityInnerLocked(ActivityRecord prev, ActivityOptions options) {
                if (ActivityManagerDebugConfig.DEBUG_LOCKSCREEN) {
                    this.mService.logLockScreen("");
                }
                if (!this.mService.mBooting && !this.mService.mBooted) {
                    return false;
                }
                ActivityRecord parent = this.mActivityContainer.mParentActivity;
                if ((parent != null && parent.state != ActivityState.RESUMED) || !this.mActivityContainer.isAttachedLocked()) {
                    return false;
                }
                this.mStackSupervisor.cancelInitializingActivities();
                ActivityRecord next = topRunningActivityLocked();
                boolean userLeaving = this.mStackSupervisor.mUserLeaving;
                this.mStackSupervisor.mUserLeaving = false;
                TaskRecord taskRecord = prev != null ? prev.task : null;
                int returnTaskType;
                boolean resumeHomeStackTask;
                if (next == null) {
                    String reason = "noMoreActivities";
                    this.mStackSupervisor.mActivityLaunchTrack = "noMoreActivities";
                    returnTaskType = (taskRecord == null || !taskRecord.isOverHomeStack()) ? STACK_VISIBLE : taskRecord.getTaskToReturnTo();
                    if (!this.mFullscreen) {
                        if (adjustFocusToNextFocusableStackLocked(returnTaskType, "noMoreActivities")) {
                            return this.mStackSupervisor.resumeFocusedStackTopActivityLocked(this.mStackSupervisor.getFocusedStack(), prev, null);
                        }
                    }
                    ActivityOptions.abort(options);
                    if (ActivityManagerDebugConfig.DEBUG_STATES) {
                        Slog.d(TAG_STATES, "resumeTopActivityLocked: No more activities go home");
                    }
                    if (ActivityManagerDebugConfig.DEBUG_STACK) {
                        this.mStackSupervisor.validateTopActivitiesLocked();
                    }
                    Jlog.d(24, "JL_LAUNCHER_STARTUP");
                    if (isOnHomeDisplay()) {
                        resumeHomeStackTask = this.mStackSupervisor.resumeHomeStackTask(returnTaskType, prev, "noMoreActivities");
                    } else {
                        resumeHomeStackTask = false;
                    }
                    return resumeHomeStackTask;
                }
                next.delayedResume = false;
                if (this.mResumedActivity == next && next.state == ActivityState.RESUMED && this.mStackSupervisor.allResumedActivitiesComplete()) {
                    this.mWindowManager.executeAppTransition();
                    this.mNoAnimActivities.clear();
                    ActivityOptions.abort(options);
                    Flog.i(PAUSE_TIMEOUT_MSG, "resumeTopActivityLocked: Top activity resumed " + next);
                    if (ActivityManagerDebugConfig.DEBUG_STACK) {
                        this.mStackSupervisor.validateTopActivitiesLocked();
                    }
                    return false;
                }
                TaskRecord nextTask = next.task;
                if (taskRecord != null && taskRecord.stack == this && taskRecord.isOverHomeStack() && prev.finishing && prev.frontOfTask) {
                    if (ActivityManagerDebugConfig.DEBUG_STACK) {
                        this.mStackSupervisor.validateTopActivitiesLocked();
                    }
                    if (taskRecord == nextTask) {
                        taskRecord.setFrontOfTask();
                    } else if (taskRecord != topTask()) {
                        ((TaskRecord) this.mTaskHistory.get(this.mTaskHistory.indexOf(taskRecord) + STACK_VISIBLE)).setTaskToReturnTo(STACK_VISIBLE);
                    } else if (!isOnHomeDisplay()) {
                        return false;
                    } else {
                        if (!isHomeStack()) {
                            Flog.i(PAUSE_TIMEOUT_MSG, "resumeTopActivityLocked: Launching home next");
                            returnTaskType = (taskRecord == null || !taskRecord.isOverHomeStack()) ? STACK_VISIBLE : taskRecord.getTaskToReturnTo();
                            if (isOnHomeDisplay()) {
                                resumeHomeStackTask = this.mStackSupervisor.resumeHomeStackTask(returnTaskType, prev, "prevFinished");
                            } else {
                                resumeHomeStackTask = false;
                            }
                            return resumeHomeStackTask;
                        }
                    }
                }
                if (this.mService.isSleepingOrShuttingDownLocked() && this.mLastPausedActivity == next && this.mStackSupervisor.allPausedActivitiesComplete()) {
                    this.mWindowManager.executeAppTransition();
                    this.mNoAnimActivities.clear();
                    ActivityOptions.abort(options);
                    Flog.i(PAUSE_TIMEOUT_MSG, "resumeTopActivityLocked: Going to sleep and all paused");
                    if (ActivityManagerDebugConfig.DEBUG_STACK) {
                        this.mStackSupervisor.validateTopActivitiesLocked();
                    }
                    return false;
                } else if (this.mService.mUserController.hasStartedUserState(next.userId)) {
                    this.mStackSupervisor.mStoppingActivities.remove(next);
                    this.mStackSupervisor.mGoingToSleepActivities.remove(next);
                    next.sleeping = false;
                    if (this.mStackSupervisor.mWaitingVisibleActivities.contains(next)) {
                        this.mStackSupervisor.mWaitingVisibleActivities.remove(next);
                    }
                    if (ActivityManagerDebugConfig.DEBUG_SWITCH) {
                        Slog.v(TAG_SWITCH, "Resuming " + next);
                    }
                    if (this.mStackSupervisor.allPausedActivitiesComplete()) {
                        this.mStackSupervisor.setLaunchSource(next.info.applicationInfo.uid);
                        boolean dontWaitForPause = (next.info.flags & DumpState.DUMP_KEYSETS) != 0 ? SHOW_APP_STARTING_PREVIEW : false;
                        boolean pausing = this.mStackSupervisor.pauseBackStacks(userLeaving, SHOW_APP_STARTING_PREVIEW, dontWaitForPause);
                        if (this.mResumedActivity != null) {
                            if (ActivityManagerDebugConfig.DEBUG_STATES) {
                                Slog.d(TAG_STATES, "resumeTopActivityLocked: Pausing " + this.mResumedActivity);
                            }
                            Flog.i(PAUSE_TIMEOUT_MSG, "resumeTopActivityLocked: Pausing " + this.mResumedActivity);
                            pausing |= startPausingLocked(userLeaving, false, SHOW_APP_STARTING_PREVIEW, dontWaitForPause);
                        }
                        if (pausing) {
                            Flog.i(PAUSE_TIMEOUT_MSG, "resumeTopActivityLocked: Skip resume: need to start pausing");
                            if (!(next.app == null || next.app.thread == null)) {
                                this.mService.updateLruProcessLocked(next.app, SHOW_APP_STARTING_PREVIEW, null);
                            }
                            if (ActivityManagerDebugConfig.DEBUG_STACK) {
                                this.mStackSupervisor.validateTopActivitiesLocked();
                            }
                            return SHOW_APP_STARTING_PREVIEW;
                        } else if (this.mResumedActivity == next && next.state == ActivityState.RESUMED && this.mStackSupervisor.allResumedActivitiesComplete()) {
                            this.mWindowManager.executeAppTransition();
                            this.mNoAnimActivities.clear();
                            ActivityOptions.abort(options);
                            if (ActivityManagerDebugConfig.DEBUG_STATES) {
                                Slog.d(TAG_STATES, "resumeTopActivityLocked: Top activity resumed (dontWaitForPause) " + next);
                            }
                            if (ActivityManagerDebugConfig.DEBUG_STACK) {
                                this.mStackSupervisor.validateTopActivitiesLocked();
                            }
                            return SHOW_APP_STARTING_PREVIEW;
                        } else {
                            if (!(!this.mService.isSleepingLocked() || this.mLastNoHistoryActivity == null || this.mLastNoHistoryActivity.finishing)) {
                                if (ActivityManagerDebugConfig.DEBUG_STATES) {
                                    Slog.d(TAG_STATES, "no-history finish of " + this.mLastNoHistoryActivity + " on new resume");
                                }
                                requestFinishActivityLocked(this.mLastNoHistoryActivity.appToken, STACK_INVISIBLE, null, "resume-no-history", false);
                                this.mLastNoHistoryActivity = null;
                            }
                            if (!(prev == null || prev == next)) {
                                if (!this.mStackSupervisor.mWaitingVisibleActivities.contains(prev) && next != null && !next.nowVisible) {
                                    this.mStackSupervisor.mWaitingVisibleActivities.add(prev);
                                    if (ActivityManagerDebugConfig.DEBUG_SWITCH) {
                                        Slog.v(TAG_SWITCH, "Resuming top, waiting visible to hide: " + prev);
                                    }
                                } else if (prev.finishing) {
                                    if (this.mStackSupervisor.mWaitingVisibleActivities.contains(prev)) {
                                        this.mStackSupervisor.mWaitingVisibleActivities.add(prev);
                                    }
                                    this.mWindowManager.setAppVisibility(prev.appToken, false);
                                    if (ActivityManagerDebugConfig.DEBUG_SWITCH) {
                                        Slog.v(TAG_SWITCH, "Not waiting for visible to hide: " + prev + ", waitingVisible=" + this.mStackSupervisor.mWaitingVisibleActivities.contains(prev) + ", nowVisible=" + next.nowVisible);
                                    }
                                } else if (ActivityManagerDebugConfig.DEBUG_SWITCH) {
                                    Slog.v(TAG_SWITCH, "Previous already visible but still waiting to hide: " + prev + ", waitingVisible=" + this.mStackSupervisor.mWaitingVisibleActivities.contains(prev) + ", nowVisible=" + next.nowVisible);
                                }
                            }
                            try {
                                AppGlobals.getPackageManager().setPackageStoppedState(next.packageName, false, next.userId);
                            } catch (RemoteException e) {
                            } catch (IllegalArgumentException e2) {
                                Slog.w(TAG, "Failed trying to unstop package " + next.packageName + ": " + e2);
                            }
                            boolean anim = SHOW_APP_STARTING_PREVIEW;
                            if (this.mIsAnimationBoostEnabled && this.mPerf == null) {
                                this.mPerf = new BoostFramework();
                            }
                            if (prev == null) {
                                if (ActivityManagerDebugConfig.DEBUG_TRANSITION) {
                                    Slog.v(TAG_TRANSITION, "Prepare open transition: no previous");
                                }
                                if (this.mNoAnimActivities.contains(next)) {
                                    anim = false;
                                    this.mWindowManager.prepareAppTransition(STACK_INVISIBLE, false);
                                } else {
                                    this.mWindowManager.prepareAppTransition(6, false);
                                }
                            } else if (prev.finishing) {
                                if (ActivityManagerDebugConfig.DEBUG_TRANSITION) {
                                    Slog.v(TAG_TRANSITION, "Prepare close transition: prev=" + prev);
                                }
                                if (this.mNoAnimActivities.contains(prev)) {
                                    anim = false;
                                    this.mWindowManager.prepareAppTransition(STACK_INVISIBLE, false);
                                } else {
                                    r3 = this.mWindowManager;
                                    if (prev.task == next.task) {
                                        r2 = 7;
                                    } else {
                                        r2 = 9;
                                    }
                                    r3.prepareAppTransition(r2, false);
                                    if (!(prev.task == next.task || this.mPerf == null)) {
                                        this.mPerf.perfLockAcquire(this.aBoostTimeOut, this.aBoostParamVal);
                                    }
                                }
                                this.mWindowManager.setAppVisibility(prev.appToken, false);
                            } else {
                                if (ActivityManagerDebugConfig.DEBUG_TRANSITION) {
                                    Slog.v(TAG_TRANSITION, "Prepare open transition: prev=" + prev);
                                }
                                if (this.mNoAnimActivities.contains(next)) {
                                    anim = false;
                                    this.mWindowManager.prepareAppTransition(STACK_INVISIBLE, false);
                                } else {
                                    r3 = this.mWindowManager;
                                    if (prev.task == next.task) {
                                        r2 = 6;
                                    } else if (next.mLaunchTaskBehind) {
                                        r2 = 16;
                                    } else {
                                        r2 = 8;
                                    }
                                    r3.prepareAppTransition(r2, false);
                                    if (!(prev.task == next.task || this.mPerf == null)) {
                                        this.mPerf.perfLockAcquire(this.aBoostTimeOut, this.aBoostParamVal);
                                    }
                                }
                            }
                            Bundle bundle = null;
                            if (anim) {
                                ActivityOptions opts = next.getOptionsForTargetActivityLocked();
                                if (opts != null) {
                                    bundle = opts.toBundle();
                                }
                                next.applyOptionsLocked();
                            } else {
                                next.clearOptionsLocked();
                            }
                            ActivityStack lastStack = this.mStackSupervisor.getLastStack();
                            if (next.app == null || next.app.thread == null) {
                                if (next.hasBeenLaunched) {
                                    next.showStartingWindow(null, SHOW_APP_STARTING_PREVIEW);
                                    if (ActivityManagerDebugConfig.DEBUG_SWITCH) {
                                        Slog.v(TAG_SWITCH, "Restarting: " + next);
                                    }
                                } else {
                                    next.hasBeenLaunched = SHOW_APP_STARTING_PREVIEW;
                                }
                                if (ActivityManagerDebugConfig.DEBUG_STATES) {
                                    Slog.d(TAG_STATES, "resumeTopActivityLocked: Restarting " + next);
                                }
                                this.mStackSupervisor.startSpecificActivityLocked(next, SHOW_APP_STARTING_PREVIEW, SHOW_APP_STARTING_PREVIEW);
                            } else {
                                if (ActivityManagerDebugConfig.DEBUG_SWITCH) {
                                    Slog.v(TAG_SWITCH, "Resume running: " + next + " stopped=" + next.stopped + " visible=" + next.visible);
                                }
                                boolean lastActivityTranslucent = lastStack != null ? lastStack.mFullscreen ? lastStack.mLastPausedActivity != null ? lastStack.mLastPausedActivity.fullscreen ? false : SHOW_APP_STARTING_PREVIEW : false : SHOW_APP_STARTING_PREVIEW : false;
                                if (!next.visible || next.stopped || lastActivityTranslucent) {
                                    this.mWindowManager.setAppVisibility(next.appToken, SHOW_APP_STARTING_PREVIEW);
                                }
                                next.startLaunchTickingLocked();
                                ActivityRecord activityRecord = lastStack == null ? null : lastStack.mResumedActivity;
                                ActivityState lastState = next.state;
                                this.mService.updateCpuStats();
                                if (ActivityManagerDebugConfig.DEBUG_STATES) {
                                    Slog.v(TAG_STATES, "Moving to RESUMED: " + next + " (in existing)");
                                }
                                next.state = ActivityState.RESUMED;
                                if (!this.mService.mActivityStarter.mCurActivityPkName.equals(next.packageName)) {
                                    Jlog.warmLaunchingAppBegin(next.packageName);
                                    Jlog.d(142, next.packageName, next.app.pid, "");
                                    LogPower.push(HdmiCecKeycode.CEC_KEYCODE_F1_BLUE, next.packageName);
                                    if (this.mRuningBackground.booleanValue()) {
                                        LogPower.push(HdmiCecKeycode.CEC_KEYCODE_F2_RED, this.mService.mActivityStarter.mCurActivityPkName);
                                        this.mRuningBackground = Boolean.valueOf(false);
                                    }
                                    this.mService.mActivityStarter.mCurActivityPkName = next.packageName;
                                }
                                this.mResumedActivity = next;
                                next.task.touchActiveTime();
                                this.mRecentTasks.addLocked(next.task);
                                this.mService.updateLruProcessLocked(next.app, SHOW_APP_STARTING_PREVIEW, null);
                                updateLRUListLocked(next);
                                this.mService.updateOomAdjLocked();
                                boolean notUpdated = SHOW_APP_STARTING_PREVIEW;
                                if (this.mStackSupervisor.isFocusedStack(this)) {
                                    Configuration config = this.mWindowManager.updateOrientationFromAppTokens(this.mService.mConfiguration, next.mayFreezeScreenLocked(next.app) ? next.appToken : null);
                                    if (config != null) {
                                        next.frozenBeforeDestroy = SHOW_APP_STARTING_PREVIEW;
                                    }
                                    notUpdated = this.mService.updateConfigurationLocked(config, next, false) ? false : SHOW_APP_STARTING_PREVIEW;
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
                                    this.mService.setFocusedActivityLocked(next, "resumeTopActivityInnerLocked adjustTopFocus");
                                    if (this.mStackSupervisor.reportResumedActivityLocked(next)) {
                                        Flog.i(PAUSE_TIMEOUT_MSG, "resumeTopActivityLocked, report resumed activity: " + next);
                                        this.mNoAnimActivities.clear();
                                        if (ActivityManagerDebugConfig.DEBUG_STACK) {
                                            this.mStackSupervisor.validateTopActivitiesLocked();
                                        }
                                        return SHOW_APP_STARTING_PREVIEW;
                                    }
                                    if (ActivityManagerDebugConfig.DEBUG_STACK) {
                                        this.mStackSupervisor.validateTopActivitiesLocked();
                                    }
                                    return false;
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
                                        next.app.thread.scheduleNewIntent(next.newIntents, next.appToken);
                                    }
                                    this.mWindowManager.notifyAppStopped(next.appToken, false);
                                    EventLog.writeEvent(EventLogTags.AM_RESUME_ACTIVITY, new Object[]{Integer.valueOf(next.userId), Integer.valueOf(System.identityHashCode(next)), Integer.valueOf(next.task.taskId), next.shortComponentName});
                                    next.sleeping = false;
                                    this.mService.showUnsupportedZoomDialogIfNeededLocked(next);
                                    this.mService.showAskCompatModeDialogLocked(next);
                                    next.app.pendingUiClean = SHOW_APP_STARTING_PREVIEW;
                                    next.app.forceProcessStateUpTo(this.mService.mTopProcessState);
                                    next.clearOptionsLocked();
                                    if (this.mService.mCustomController != null) {
                                        this.mService.customActivityResuming(next.packageName);
                                    }
                                    if (Jlog.isPerfTest()) {
                                        Jlog.i(2042, Intent.toPkgClsString(next.realActivity, "who"));
                                    }
                                    next.app.thread.scheduleResumeActivity(next.appToken, next.app.repProcState, this.mService.isNextTransitionForward(), bundle);
                                    this.mStackSupervisor.checkReadyForSleepLocked();
                                    if (ActivityManagerDebugConfig.DEBUG_STATES) {
                                        Slog.d(TAG_STATES, "resumeTopActivityLocked: Resumed " + next);
                                    }
                                    try {
                                        ActivityStackSupervisor activityStackSupervisor = this.mStackSupervisor;
                                        activityStackSupervisor.mActivityLaunchTrack += " resumeTopComplete";
                                        completeResumeLocked(next);
                                        if (Jlog.isUBMEnable()) {
                                            StringBuilder append = new StringBuilder().append("AS#").append(next.intent.getComponent().flattenToShortString()).append("(").append(next.app.pid).append(",");
                                            String flattenToShortString = (prev == null || prev.intent == null) ? "null" : prev.intent.getComponent().flattenToShortString();
                                            append = append.append(flattenToShortString).append(",");
                                            Object valueOf = (prev == null || prev.app == null) ? "unknow" : Integer.valueOf(prev.app.pid);
                                            Jlog.d(273, append.append(valueOf).append(")").toString());
                                        }
                                    } catch (Exception e3) {
                                        Slog.w(TAG, "Exception thrown during resume of " + next, e3);
                                        requestFinishActivityLocked(next.appToken, STACK_INVISIBLE, null, "resume-exception", SHOW_APP_STARTING_PREVIEW);
                                        if (ActivityManagerDebugConfig.DEBUG_STACK) {
                                            this.mStackSupervisor.validateTopActivitiesLocked();
                                        }
                                        return SHOW_APP_STARTING_PREVIEW;
                                    }
                                } catch (Exception e4) {
                                    if (ActivityManagerDebugConfig.DEBUG_STATES) {
                                        Slog.v(TAG_STATES, "Resume failed; resetting state to " + lastState + ": " + next);
                                    }
                                    next.state = lastState;
                                    if (lastStack != null) {
                                        lastStack.mResumedActivity = activityRecord;
                                    }
                                    Slog.i(TAG, "Restarting because process died: " + next);
                                    if (!next.hasBeenLaunched) {
                                        next.hasBeenLaunched = SHOW_APP_STARTING_PREVIEW;
                                    } else if (lastStack != null && this.mStackSupervisor.isFrontStack(lastStack)) {
                                        next.showStartingWindow(null, SHOW_APP_STARTING_PREVIEW);
                                    }
                                    this.mStackSupervisor.startSpecificActivityLocked(next, SHOW_APP_STARTING_PREVIEW, false);
                                    if (ActivityManagerDebugConfig.DEBUG_STACK) {
                                        this.mStackSupervisor.validateTopActivitiesLocked();
                                    }
                                    return SHOW_APP_STARTING_PREVIEW;
                                }
                            }
                            if (ActivityManagerDebugConfig.DEBUG_STACK) {
                                this.mStackSupervisor.validateTopActivitiesLocked();
                            }
                            return SHOW_APP_STARTING_PREVIEW;
                        }
                    }
                    Flog.i(PAUSE_TIMEOUT_MSG, "resumeTopActivityLocked: Skip resume: some activity pausing.");
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

            private TaskRecord getNextTask(TaskRecord targetTask) {
                int index = this.mTaskHistory.indexOf(targetTask);
                if (index >= 0) {
                    int numTasks = this.mTaskHistory.size();
                    for (int i = index + STACK_VISIBLE; i < numTasks; i += STACK_VISIBLE) {
                        TaskRecord task = (TaskRecord) this.mTaskHistory.get(i);
                        if (task.userId == targetTask.userId) {
                            return task;
                        }
                    }
                }
                return null;
            }

            /* JADX WARNING: inconsistent code. */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            private void insertTaskAtPosition(TaskRecord task, int position) {
                if (position >= this.mTaskHistory.size()) {
                    insertTaskAtTop(task, null);
                    return;
                }
                int maxPosition = this.mTaskHistory.size();
                if (!this.mStackSupervisor.isCurrentProfileLocked(task.userId) && task.topRunningActivityLocked() == null) {
                    while (maxPosition > 0) {
                        TaskRecord tmpTask = (TaskRecord) this.mTaskHistory.get(maxPosition - 1);
                        if (this.mStackSupervisor.isCurrentProfileLocked(tmpTask.userId) && tmpTask.topRunningActivityLocked() != null) {
                            maxPosition--;
                        }
                    }
                }
                position = Math.min(position, maxPosition);
                this.mTaskHistory.remove(task);
                this.mTaskHistory.add(position, task);
                updateTaskMovement(task, SHOW_APP_STARTING_PREVIEW);
            }

            protected void insertTaskAtTop(TaskRecord task, ActivityRecord newActivity) {
                boolean isLastTaskOverHome = false;
                if (task.isOverHomeStack()) {
                    TaskRecord nextTask = getNextTask(task);
                    if (nextTask == null || nextTask.topRunningActivityLocked() == null) {
                        isLastTaskOverHome = SHOW_APP_STARTING_PREVIEW;
                    } else {
                        Flog.i(PAUSE_TIMEOUT_MSG, "insertTaskAtTop, adjustNextTask: " + nextTask + ", nextTask ReturnTo: " + nextTask.getTaskToReturnTo() + ", current task: " + task + ", ReturnTo=" + task.getTaskToReturnTo());
                        nextTask.setTaskToReturnTo(task.getTaskToReturnTo());
                    }
                }
                if (isOnHomeDisplay()) {
                    ActivityStack lastStack = this.mStackSupervisor.getLastStack();
                    boolean fromHome = lastStack.isHomeStack();
                    Flog.i(PAUSE_TIMEOUT_MSG, "insertTaskAtTop, setTaskToReturnTo: fromHome=" + fromHome + ",lastStackTop=" + lastStack.topTask() + ", isHomeStack=" + isHomeStack() + ", topTask=" + topTask() + ", task=" + task);
                    if (!isHomeStack() && (fromHome || topTask() != task)) {
                        int returnToType = isLastTaskOverHome ? task.getTaskToReturnTo() : STACK_INVISIBLE;
                        if (fromHome && StackId.allowTopTaskToReturnHome(this.mStackId)) {
                            returnToType = lastStack.topTask() == null ? STACK_VISIBLE : lastStack.topTask().taskType;
                        }
                        task.setTaskToReturnTo(returnToType);
                    }
                } else {
                    task.setTaskToReturnTo(STACK_INVISIBLE);
                }
                this.mTaskHistory.remove(task);
                int taskNdx = this.mTaskHistory.size();
                boolean notShownWhenLocked = (newActivity == null || (newActivity.info.flags & DumpState.DUMP_PROVIDERS) != 0) ? (newActivity == null && task.topRunningActivityLocked() == null) ? SHOW_APP_STARTING_PREVIEW : false : SHOW_APP_STARTING_PREVIEW;
                if (!this.mStackSupervisor.isCurrentProfileLocked(task.userId) && notShownWhenLocked) {
                    TaskRecord tmpTask;
                    do {
                        taskNdx--;
                        if (taskNdx < 0) {
                            break;
                        }
                        tmpTask = (TaskRecord) this.mTaskHistory.get(taskNdx);
                        if (!this.mStackSupervisor.isCurrentProfileLocked(tmpTask.userId)) {
                            break;
                        }
                    } while (tmpTask.topRunningActivityLocked() != null);
                    taskNdx += STACK_VISIBLE;
                }
                this.mTaskHistory.add(taskNdx, task);
                updateTaskMovement(task, SHOW_APP_STARTING_PREVIEW);
            }

            final void startActivityLocked(ActivityRecord r, boolean newTask, boolean keepCurTransition, ActivityOptions options) {
                TaskRecord rTask = r.task;
                int taskId = rTask.taskId;
                if (!r.mLaunchTaskBehind && (taskForIdLocked(taskId) == null || newTask)) {
                    insertTaskAtTop(rTask, r);
                    this.mWindowManager.moveTaskToTop(taskId);
                }
                TaskRecord taskRecord = null;
                if (!newTask) {
                    boolean startIt = SHOW_APP_STARTING_PREVIEW;
                    for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
                        taskRecord = (TaskRecord) this.mTaskHistory.get(taskNdx);
                        if (taskRecord.getTopActivity() != null) {
                            if (taskRecord == r.task) {
                                if (!startIt) {
                                    if (ActivityManagerDebugConfig.DEBUG_ADD_REMOVE) {
                                        Slog.i(TAG, "Adding activity " + r + " to task " + taskRecord, new RuntimeException("here").fillInStackTrace());
                                    }
                                    taskRecord.addActivityToTop(r);
                                    r.putInHistory();
                                    addConfigOverride(r, taskRecord);
                                    ActivityOptions.abort(options);
                                    return;
                                }
                            } else if (taskRecord.numFullscreen > 0) {
                                Flog.i(PAUSE_TIMEOUT_MSG, "starting r: " + r + " blocked by task: " + taskRecord);
                                startIt = false;
                            }
                        }
                    }
                }
                if (taskRecord == r.task && this.mTaskHistory.indexOf(taskRecord) != this.mTaskHistory.size() - 1) {
                    this.mStackSupervisor.mUserLeaving = false;
                    if (ActivityManagerDebugConfig.DEBUG_USER_LEAVING) {
                        Slog.v(TAG_USER_LEAVING, "startActivity() behind front, mUserLeaving=false");
                    }
                }
                taskRecord = r.task;
                if (ActivityManagerDebugConfig.DEBUG_ADD_REMOVE) {
                    Slog.i(TAG, "Adding activity " + r + " to stack to task " + taskRecord, new RuntimeException("here").fillInStackTrace());
                }
                taskRecord.addActivityToTop(r);
                taskRecord.setFrontOfTask();
                r.putInHistory();
                if (this.mIsPerfBoostEnabled && this.mPerfBoost == null) {
                    this.mPerfBoost = new BoostFramework();
                }
                if (this.mPerfBoost != null) {
                    this.mPerfBoost.perfLockAcquire(this.lBoostTimeOut, this.lBoostCpuParamVal);
                }
                if (!isHomeStack() || numActivities() > 0) {
                    boolean showStartingIcon = newTask;
                    ProcessRecord proc = r.app;
                    if (proc == null) {
                        proc = (ProcessRecord) this.mService.mProcessNames.get(r.processName, r.info.applicationInfo.uid + r.info.applicationInfo.euid);
                    }
                    if (proc == null || proc.thread == null) {
                        showStartingIcon = SHOW_APP_STARTING_PREVIEW;
                    }
                    if (ActivityManagerDebugConfig.DEBUG_TRANSITION) {
                        Slog.v(TAG_TRANSITION, "Prepare open transition: starting " + r);
                    }
                    if ((r.intent.getFlags() & DumpState.DUMP_INSTALLS) != 0) {
                        this.mWindowManager.prepareAppTransition(STACK_INVISIBLE, keepCurTransition);
                        this.mNoAnimActivities.add(r);
                    } else {
                        int i;
                        WindowManagerService windowManagerService = this.mWindowManager;
                        if (!newTask) {
                            i = 6;
                        } else if (r.mLaunchTaskBehind) {
                            i = 16;
                        } else {
                            i = 8;
                        }
                        windowManagerService.prepareAppTransition(i, keepCurTransition);
                        this.mNoAnimActivities.remove(r);
                    }
                    addConfigOverride(r, taskRecord);
                    boolean doShow = SHOW_APP_STARTING_PREVIEW;
                    if (newTask) {
                        if ((r.intent.getFlags() & 2097152) != 0) {
                            resetTaskIfNeededLocked(r, r);
                            doShow = topRunningNonDelayedActivityLocked(null) == r ? SHOW_APP_STARTING_PREVIEW : false;
                        }
                    } else if (options != null && options.getAnimationType() == 5) {
                        doShow = false;
                    }
                    Flog.i(301, "startActivityLocked doShow= " + doShow + " mLaunchTaskBehind= " + r.mLaunchTaskBehind);
                    if (r.mLaunchTaskBehind) {
                        this.mWindowManager.setAppVisibility(r.appToken, SHOW_APP_STARTING_PREVIEW);
                        ensureActivitiesVisibleLocked(null, STACK_INVISIBLE, false);
                    } else if (doShow) {
                        ActivityRecord prev = r.task.topRunningActivityWithStartingWindowLocked();
                        if (prev != null) {
                            if (prev.task != r.task) {
                                prev = null;
                            } else if (prev.nowVisible) {
                                prev = null;
                            }
                        }
                        if (isSplitActivity(r.intent)) {
                            this.mWindowManager.setSplittable(SHOW_APP_STARTING_PREVIEW);
                        } else if (this.mWindowManager.isSplitMode()) {
                            this.mWindowManager.setSplittable(false);
                        }
                        r.showStartingWindow(prev, showStartingIcon);
                    }
                } else {
                    addConfigOverride(r, taskRecord);
                    ActivityOptions.abort(options);
                }
            }

            final void validateAppTokensLocked() {
                this.mValidateAppTokens.clear();
                this.mValidateAppTokens.ensureCapacity(numActivities());
                int numTasks = this.mTaskHistory.size();
                for (int taskNdx = STACK_INVISIBLE; taskNdx < numTasks; taskNdx += STACK_VISIBLE) {
                    TaskRecord task = (TaskRecord) this.mTaskHistory.get(taskNdx);
                    ArrayList<ActivityRecord> activities = task.mActivities;
                    if (!activities.isEmpty()) {
                        TaskGroup group = new TaskGroup();
                        group.taskId = task.taskId;
                        this.mValidateAppTokens.add(group);
                        int numActivities = activities.size();
                        for (int activityNdx = STACK_INVISIBLE; activityNdx < numActivities; activityNdx += STACK_VISIBLE) {
                            group.tokens.add(((ActivityRecord) activities.get(activityNdx)).appToken);
                        }
                    }
                }
                this.mWindowManager.validateAppTokens(this.mStackId, this.mValidateAppTokens);
            }

            final ActivityOptions resetTargetTaskIfNeededLocked(TaskRecord task, boolean forceReset) {
                ActivityOptions topOptions = null;
                int replyChainEnd = -1;
                boolean canMoveOptions = SHOW_APP_STARTING_PREVIEW;
                ArrayList<ActivityRecord> activities = task.mActivities;
                int numActivities = activities.size();
                int rootActivityNdx = task.findEffectiveRootIndex();
                for (int i = numActivities - 1; i > rootActivityNdx; i--) {
                    ActivityRecord target = (ActivityRecord) activities.get(i);
                    if (target.frontOfTask) {
                        break;
                    }
                    int flags = target.info.flags;
                    boolean finishOnTaskLaunch = (flags & STACK_VISIBLE_ACTIVITY_BEHIND) != 0 ? SHOW_APP_STARTING_PREVIEW : false;
                    boolean allowTaskReparenting = (flags & 64) != 0 ? SHOW_APP_STARTING_PREVIEW : false;
                    boolean clearWhenTaskReset = (target.intent.getFlags() & DumpState.DUMP_FROZEN) != 0 ? SHOW_APP_STARTING_PREVIEW : false;
                    if (!finishOnTaskLaunch && !clearWhenTaskReset && target.resultTo != null) {
                        Flog.i(DESTROY_ACTIVITIES_MSG, "ResetTask:Keeping the end of the reply chain, target= " + target.task + " targetI=" + i + " replyChainEnd=" + replyChainEnd);
                        if (replyChainEnd < 0) {
                            replyChainEnd = i;
                        }
                    } else if (!finishOnTaskLaunch && !clearWhenTaskReset && allowTaskReparenting && target.taskAffinity != null && !target.taskAffinity.equals(task.affinity)) {
                        ActivityRecord bottom;
                        TaskRecord targetTask;
                        int start;
                        if (this.mTaskHistory.isEmpty() || ((TaskRecord) this.mTaskHistory.get(STACK_INVISIBLE)).mActivities.isEmpty()) {
                            bottom = null;
                        } else {
                            bottom = (ActivityRecord) ((TaskRecord) this.mTaskHistory.get(STACK_INVISIBLE)).mActivities.get(STACK_INVISIBLE);
                        }
                        if (bottom == null || target.taskAffinity == null || !target.taskAffinity.equals(bottom.task.affinity)) {
                            targetTask = createTaskRecord(this.mStackSupervisor.getNextTaskIdForUserLocked(target.userId), target.info, null, null, null, false);
                            targetTask.affinityIntent = target.intent;
                            Flog.i(DESTROY_ACTIVITIES_MSG, "ResetTask:Start pushing activity " + target + " out to new task " + target.task);
                        } else {
                            targetTask = bottom.task;
                            Flog.i(DESTROY_ACTIVITIES_MSG, "ResetTask:Start pushing activity " + target + " out to bottom task " + bottom.task);
                        }
                        setAppTask(target, targetTask);
                        noOptions = canMoveOptions;
                        if (replyChainEnd < 0) {
                            start = i;
                        } else {
                            start = replyChainEnd;
                        }
                        for (srcPos = start; srcPos >= i; srcPos--) {
                            p = (ActivityRecord) activities.get(srcPos);
                            if (!p.finishing) {
                                canMoveOptions = false;
                                if (noOptions && r27 == null) {
                                    topOptions = p.takeOptionsLocked();
                                    if (topOptions != null) {
                                        noOptions = false;
                                    }
                                }
                                if (ActivityManagerDebugConfig.DEBUG_ADD_REMOVE) {
                                    Slog.i(TAG_ADD_REMOVE, "Removing activity " + p + " from task=" + task + " adding to task=" + targetTask + " Callers=" + Debug.getCallers(4));
                                }
                                Flog.i(DESTROY_ACTIVITIES_MSG, "ResetTask:Pushing next activity " + srcPos + ": " + p + " out to target's task " + targetTask);
                                p.setTask(targetTask, null);
                                targetTask.addActivityAtBottom(p);
                                setAppTask(p, targetTask);
                            }
                        }
                        this.mWindowManager.moveTaskToBottom(targetTask.taskId);
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
                                if (noOptions && r27 == null) {
                                    topOptions = p.takeOptionsLocked();
                                    if (topOptions != null) {
                                        noOptions = false;
                                    }
                                }
                                Flog.i(DESTROY_ACTIVITIES_MSG, "resetTaskIntendedTask: calling finishActivity on " + p);
                                if (finishActivityLocked(p, STACK_INVISIBLE, null, "reset-task", false)) {
                                    end--;
                                    srcPos--;
                                }
                            }
                            srcPos += STACK_VISIBLE;
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
                    boolean finishOnTaskLaunch = (flags & STACK_VISIBLE_ACTIVITY_BEHIND) != 0 ? SHOW_APP_STARTING_PREVIEW : false;
                    boolean allowTaskReparenting = (flags & 64) != 0 ? SHOW_APP_STARTING_PREVIEW : false;
                    if (target.resultTo != null) {
                        Flog.i(DESTROY_ACTIVITIES_MSG, "ResetTaskAffinity:Keeping the end of the reply chain, target= " + target.task + " targetI=" + i + " replyChainEnd=" + replyChainEnd);
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
                                        Flog.i(DESTROY_ACTIVITIES_MSG, "ResetTaskAffinity:finishActivity pos:  " + srcPos + " acitivity: " + p);
                                        finishActivityLocked(p, STACK_INVISIBLE, null, "move-affinity", false);
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
                                    p.setTask(task, null);
                                    task.addActivityAtIndex(taskInsertionPoint, p);
                                    if (ActivityManagerDebugConfig.DEBUG_ADD_REMOVE) {
                                        Slog.i(TAG_ADD_REMOVE, "Removing and adding activity " + p + " to stack at " + task + " callers=" + Debug.getCallers(MAX_STOPPING_TO_FORCE));
                                    }
                                    Flog.i(DESTROY_ACTIVITIES_MSG, "ResetTaskAffinity:Pulling activity " + p + " from " + srcPos + " in to resetting task " + task);
                                    setAppTask(p, task);
                                }
                                this.mWindowManager.moveTaskToTop(taskId);
                                if (target.info.launchMode == STACK_VISIBLE) {
                                    ArrayList<ActivityRecord> taskActivities = task.mActivities;
                                    int targetNdx = taskActivities.indexOf(target);
                                    if (targetNdx > 0) {
                                        p = (ActivityRecord) taskActivities.get(targetNdx - 1);
                                        if (p.intent.getComponent().equals(target.intent.getComponent())) {
                                            Flog.i(DESTROY_ACTIVITIES_MSG, "ResetTaskAffinity:Drop singleTop activity " + p + " for target " + target);
                                            finishActivityLocked(p, STACK_INVISIBLE, null, "replace", false);
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

            /* JADX WARNING: inconsistent code. */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            final ActivityRecord resetTaskIfNeededLocked(ActivityRecord taskTop, ActivityRecord newActivity) {
                boolean forceReset = (newActivity.info.flags & 4) != 0 ? SHOW_APP_STARTING_PREVIEW : false;
                TaskRecord task = taskTop.task;
                boolean taskFound = false;
                ActivityOptions topOptions = null;
                int reparentInsertionPoint = -1;
                for (int i = this.mTaskHistory.size() - 1; i >= 0; i--) {
                    TaskRecord targetTask = (TaskRecord) this.mTaskHistory.get(i);
                    if (targetTask == task) {
                        topOptions = resetTargetTaskIfNeededLocked(task, forceReset);
                        taskFound = SHOW_APP_STARTING_PREVIEW;
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

            private void adjustFocusedActivityLocked(ActivityRecord r, String reason) {
                if (this.mStackSupervisor.isFocusedStack(this) && this.mService.mFocusedActivity == r) {
                    ActivityRecord next = topRunningActivityLocked();
                    String myReason = reason + " adjustFocus";
                    if (next != r) {
                        if (next != null && StackId.keepFocusInStackIfPossible(this.mStackId) && isFocusable()) {
                            this.mService.setFocusedActivityLocked(next, myReason);
                            return;
                        }
                        TaskRecord task = r.task;
                        if (r.frontOfTask && task == topTask() && task.isOverHomeStack()) {
                            int taskToReturnTo = task.getTaskToReturnTo();
                            if ((!this.mFullscreen && adjustFocusToNextFocusableStackLocked(taskToReturnTo, myReason)) || this.mStackSupervisor.moveHomeStackTaskToTop(taskToReturnTo, myReason)) {
                                return;
                            }
                        }
                    }
                    this.mService.setFocusedActivityLocked(this.mStackSupervisor.topRunningActivityLocked(), myReason);
                }
            }

            private boolean adjustFocusToNextFocusableStackLocked(int taskToReturnTo, String reason) {
                ActivityStack stack = getNextFocusableStackLocked();
                String myReason = reason + " adjustFocusToNextFocusableStack";
                if (stack == null) {
                    return false;
                }
                ActivityRecord top = stack.topRunningActivityLocked();
                if (!stack.isHomeStack() || (top != null && top.visible)) {
                    return this.mService.setFocusedActivityLocked(top, myReason);
                }
                return this.mStackSupervisor.moveHomeStackTaskToTop(taskToReturnTo, reason);
            }

            final void stopActivityLocked(ActivityRecord r) {
                if (ActivityManagerDebugConfig.DEBUG_SWITCH) {
                    Slog.d(TAG_SWITCH, "Stopping: " + r);
                }
                if (!(((r.intent.getFlags() & 1073741824) == 0 && (r.info.flags & DumpState.DUMP_PACKAGES) == 0) || r.finishing)) {
                    if (!this.mService.isSleepingLocked()) {
                        if (ActivityManagerDebugConfig.DEBUG_STATES) {
                            Slog.d(TAG_STATES, "no-history finish of " + r);
                        }
                        if (requestFinishActivityLocked(r.appToken, STACK_INVISIBLE, null, "stop-no-history", false)) {
                            adjustFocusedActivityLocked(r, "stopActivityFinished");
                            r.resumeKeyDispatchingLocked();
                            return;
                        }
                    } else if (ActivityManagerDebugConfig.DEBUG_STATES) {
                        Slog.d(TAG_STATES, "Not finishing noHistory " + r + " on stop because we're just sleeping");
                    }
                }
                if (!(r.app == null || r.app.thread == null)) {
                    adjustFocusedActivityLocked(r, "stopActivity");
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
                            this.mWindowManager.setAppVisibility(r.appToken, false);
                        }
                        EventLogTags.writeAmStopActivity(r.userId, System.identityHashCode(r), r.shortComponentName);
                        r.app.thread.scheduleStopActivity(r.appToken, r.visible, r.configChangeFlags);
                        if (this.mService.isSleepingOrShuttingDownLocked()) {
                            r.setSleeping(SHOW_APP_STARTING_PREVIEW);
                        }
                        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(STOP_TIMEOUT_MSG, r), JobStatus.DEFAULT_TRIGGER_UPDATE_DELAY);
                    } catch (Exception e) {
                        Slog.w(TAG, "Exception thrown during pause", e);
                        r.stopped = SHOW_APP_STARTING_PREVIEW;
                        if (ActivityManagerDebugConfig.DEBUG_STATES) {
                            Slog.v(TAG_STATES, "Stop failed; moving to STOPPED: " + r);
                        }
                        r.state = ActivityState.STOPPED;
                        if (r.deferRelaunchUntilPaused) {
                            destroyActivityLocked(r, SHOW_APP_STARTING_PREVIEW, "stop-except");
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
                return SHOW_APP_STARTING_PREVIEW;
            }

            final void finishSubActivityLocked(ActivityRecord self, String resultWho, int requestCode) {
                for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
                    ArrayList<ActivityRecord> activities = ((TaskRecord) this.mTaskHistory.get(taskNdx)).mActivities;
                    for (int activityNdx = activities.size() - 1; activityNdx >= 0; activityNdx--) {
                        ActivityRecord r = (ActivityRecord) activities.get(activityNdx);
                        if (r.resultTo == self && r.requestCode == requestCode) {
                            if (!(r.resultWho == null && resultWho == null)) {
                                if (r.resultWho != null && r.resultWho.equals(resultWho)) {
                                }
                            }
                            finishActivityLocked(r, STACK_INVISIBLE, null, "request-sub", false);
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
                int taskNdx = this.mTaskHistory.indexOf(r.task);
                int activityNdx = r.task.mActivities.indexOf(r);
                finishActivityLocked(r, STACK_INVISIBLE, null, reason, false);
                TaskRecord finishedTask = r.task;
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
                    if (!(r.state == ActivityState.RESUMED || r.state == ActivityState.PAUSING)) {
                        if (r.state == ActivityState.PAUSED) {
                        }
                    }
                    if (!(r.isHomeActivity() && this.mService.mHomeProcess == r.app)) {
                        Slog.w(TAG, "  Force finishing activity " + r.intent.getComponent().flattenToShortString());
                        finishActivityLocked(r, STACK_INVISIBLE, null, reason, false);
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
                                finishActivityLocked(r, STACK_INVISIBLE, null, "finish-voice", false);
                                didOne = SHOW_APP_STARTING_PREVIEW;
                            }
                        }
                    }
                }
                if (didOne) {
                    this.mService.updateOomAdjLocked();
                }
            }

            final boolean finishActivityAffinityLocked(ActivityRecord r) {
                ArrayList<ActivityRecord> activities = r.task.mActivities;
                for (int index = activities.indexOf(r); index >= 0; index--) {
                    ActivityRecord cur = (ActivityRecord) activities.get(index);
                    if (!Objects.equals(cur.taskAffinity, r.taskAffinity)) {
                        break;
                    }
                    finishActivityLocked(cur, STACK_INVISIBLE, null, "request-affinity", SHOW_APP_STARTING_PREVIEW);
                }
                return SHOW_APP_STARTING_PREVIEW;
            }

            final void finishActivityResultsLocked(ActivityRecord r, int resultCode, Intent resultData) {
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
                if (r.finishing) {
                    Slog.w(TAG, "Duplicate finish request for " + r);
                    return false;
                }
                r.makeFinishingLocked();
                TaskRecord task = r.task;
                EventLog.writeEvent(EventLogTags.AM_FINISH_ACTIVITY, new Object[]{Integer.valueOf(r.userId), Integer.valueOf(System.identityHashCode(r)), Integer.valueOf(task.taskId), r.shortComponentName, reason});
                ArrayList<ActivityRecord> activities = task.mActivities;
                int index = activities.indexOf(r);
                if (index < activities.size() - 1) {
                    task.setFrontOfTask();
                    if ((r.intent.getFlags() & DumpState.DUMP_FROZEN) != 0) {
                        ((ActivityRecord) activities.get(index + STACK_VISIBLE)).intent.addFlags(DumpState.DUMP_FROZEN);
                    }
                }
                r.pauseKeyDispatchingLocked();
                adjustFocusedActivityLocked(r, "finishActivity");
                finishActivityResultsLocked(r, resultCode, resultData);
                boolean endTask = index <= 0 ? SHOW_APP_STARTING_PREVIEW : false;
                int transit = endTask ? 9 : 7;
                if (this.mResumedActivity == r) {
                    if (ActivityManagerDebugConfig.DEBUG_VISIBILITY || ActivityManagerDebugConfig.DEBUG_TRANSITION) {
                        Slog.v(TAG_TRANSITION, "Prepare close transition: finishing " + r);
                    }
                    this.mWindowManager.prepareAppTransition(transit, false);
                    this.mWindowManager.setAppVisibility(r.appToken, false);
                    if (this.mPausingActivity == null) {
                        if (ActivityManagerDebugConfig.DEBUG_PAUSE) {
                            Slog.v(TAG_PAUSE, "Finish needs to pause: " + r);
                        }
                        if (ActivityManagerDebugConfig.DEBUG_USER_LEAVING) {
                            Slog.v(TAG_USER_LEAVING, "finish() => pause with userLeaving=false");
                        }
                        startPausingLocked(false, false, false, false);
                    }
                    if (endTask) {
                        this.mStackSupervisor.removeLockedTaskLocked(task);
                    }
                } else if (r.state != ActivityState.PAUSING) {
                    if (ActivityManagerDebugConfig.DEBUG_PAUSE) {
                        Slog.v(TAG_PAUSE, "Finish not pausing: " + r);
                    }
                    if (r.visible) {
                        this.mWindowManager.prepareAppTransition(transit, false);
                        this.mWindowManager.setAppVisibility(r.appToken, false);
                        this.mWindowManager.executeAppTransition();
                        if (!this.mStackSupervisor.mWaitingVisibleActivities.contains(r)) {
                            this.mStackSupervisor.mWaitingVisibleActivities.add(r);
                        }
                    }
                    int i = (r.visible || r.nowVisible) ? STACK_VISIBLE_ACTIVITY_BEHIND : STACK_VISIBLE;
                    return finishCurrentActivityLocked(r, i, oomAdj) == null ? SHOW_APP_STARTING_PREVIEW : false;
                } else if (ActivityManagerDebugConfig.DEBUG_PAUSE) {
                    Slog.v(TAG_PAUSE, "Finish waiting for pause of: " + r);
                }
                return false;
            }

            final ActivityRecord finishCurrentActivityLocked(ActivityRecord r, int mode, boolean oomAdj) {
                ActivityRecord next = this.mStackSupervisor.topRunningActivityLocked();
                if (mode != STACK_VISIBLE_ACTIVITY_BEHIND || (!(r.visible || r.nowVisible) || next == null || next.nowVisible)) {
                    this.mStackSupervisor.mStoppingActivities.remove(r);
                    this.mStackSupervisor.mGoingToSleepActivities.remove(r);
                    this.mStackSupervisor.mWaitingVisibleActivities.remove(r);
                    if (this.mResumedActivity == r) {
                        this.mResumedActivity = null;
                    }
                    ActivityState prevState = r.state;
                    if (ActivityManagerDebugConfig.DEBUG_STATES || HwSlog.HW_DEBUG_STATES) {
                        Slog.v(TAG_STATES, "Moving to FINISHING: " + r);
                    }
                    r.state = ActivityState.FINISHING;
                    if (mode == 0 || ((prevState == ActivityState.PAUSED && (mode == STACK_VISIBLE || ((mode == STACK_VISIBLE_ACTIVITY_BEHIND && !this.mFullscreen) || this.mStackId == 4))) || prevState == ActivityState.STOPPED || prevState == ActivityState.INITIALIZING)) {
                        r.makeFinishingLocked();
                        boolean activityRemoved = destroyActivityLocked(r, SHOW_APP_STARTING_PREVIEW, "finish-imm");
                        if (prevState == ActivityState.PAUSED && mode == STACK_VISIBLE_ACTIVITY_BEHIND && !this.mFullscreen) {
                            this.mStackSupervisor.ensureActivitiesVisibleLocked(null, STACK_INVISIBLE, false);
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
                    addToStopping(r, false);
                }
                Flog.i(PAUSE_TIMEOUT_MSG, "Moving to STOPPING: " + r + " (finish requested)");
                r.state = ActivityState.STOPPING;
                if (oomAdj) {
                    this.mService.updateOomAdjLocked();
                }
                return r;
            }

            void finishAllActivitiesLocked(boolean immediately) {
                boolean noActivitiesInStack = SHOW_APP_STARTING_PREVIEW;
                for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
                    ArrayList<ActivityRecord> activities = ((TaskRecord) this.mTaskHistory.get(taskNdx)).mActivities;
                    for (int activityNdx = activities.size() - 1; activityNdx >= 0; activityNdx--) {
                        ActivityRecord r = (ActivityRecord) activities.get(activityNdx);
                        noActivitiesInStack = false;
                        if (!r.finishing || immediately) {
                            Slog.d(TAG, "finishAllActivitiesLocked: finishing " + r + " immediately");
                            finishCurrentActivityLocked(r, STACK_INVISIBLE, false);
                        }
                    }
                }
                if (noActivitiesInStack) {
                    this.mActivityContainer.onTaskListEmptyLocked();
                }
            }

            final boolean shouldUpRecreateTaskLocked(ActivityRecord srec, String destAffinity) {
                if (srec == null || srec.task.affinity == null || !srec.task.affinity.equals(destAffinity)) {
                    return SHOW_APP_STARTING_PREVIEW;
                }
                if (srec.frontOfTask && srec.task != null && srec.task.getBaseIntent() != null && srec.task.getBaseIntent().isDocument()) {
                    if (srec.task.getTaskToReturnTo() != 0) {
                        return SHOW_APP_STARTING_PREVIEW;
                    }
                    int taskIdx = this.mTaskHistory.indexOf(srec.task);
                    if (taskIdx <= 0) {
                        Slog.w(TAG, "shouldUpRecreateTask: task not in history for " + srec);
                        return false;
                    } else if (taskIdx == 0) {
                        return SHOW_APP_STARTING_PREVIEW;
                    } else {
                        if (!srec.task.affinity.equals(((TaskRecord) this.mTaskHistory.get(taskIdx)).affinity)) {
                            return SHOW_APP_STARTING_PREVIEW;
                        }
                    }
                }
                return false;
            }

            final boolean navigateUpToLocked(ActivityRecord srec, Intent destIntent, int resultCode, Intent resultData) {
                TaskRecord task = srec.task;
                ArrayList<ActivityRecord> activities = task.mActivities;
                int start = activities.indexOf(srec);
                if (!this.mTaskHistory.contains(task) || start < 0) {
                    return false;
                }
                int i;
                int finishTo = start - 1;
                ActivityRecord activityRecord = finishTo < 0 ? null : (ActivityRecord) activities.get(finishTo);
                boolean foundParentInTask = false;
                ComponentName dest = destIntent.getComponent();
                if (start > 0 && dest != null) {
                    for (i = finishTo; i >= 0; i--) {
                        ActivityRecord r = (ActivityRecord) activities.get(i);
                        if (r.info.packageName.equals(dest.getPackageName()) && r.info.name.equals(dest.getClassName())) {
                            finishTo = i;
                            activityRecord = r;
                            foundParentInTask = SHOW_APP_STARTING_PREVIEW;
                            break;
                        }
                    }
                }
                IActivityController controller = this.mService.mController;
                if (controller != null) {
                    ActivityRecord next = topRunningActivityLocked(srec.appToken, STACK_INVISIBLE);
                    if (next != null) {
                        boolean resumeOK = SHOW_APP_STARTING_PREVIEW;
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
                    requestFinishActivityLocked(((ActivityRecord) activities.get(i)).appToken, resultCode, resultData, "navigate-up", SHOW_APP_STARTING_PREVIEW);
                    resultCode = STACK_INVISIBLE;
                    resultData = null;
                }
                if (activityRecord != null && foundParentInTask) {
                    int parentLaunchMode = activityRecord.info.launchMode;
                    int destIntentFlags = destIntent.getFlags();
                    if (parentLaunchMode == MAX_STOPPING_TO_FORCE || parentLaunchMode == STACK_VISIBLE_ACTIVITY_BEHIND || parentLaunchMode == STACK_VISIBLE || (67108864 & destIntentFlags) != 0) {
                        activityRecord.deliverNewIntentLocked(srec.info.applicationInfo.uid, destIntent, srec.packageName);
                    } else {
                        try {
                            foundParentInTask = this.mService.mActivityStarter.startActivityLocked(srec.app.thread, destIntent, null, null, AppGlobals.getPackageManager().getActivityInfo(destIntent.getComponent(), STACK_INVISIBLE, srec.userId), null, null, null, activityRecord.appToken, null, STACK_INVISIBLE, -1, activityRecord.launchedFromUid, activityRecord.launchedFromPackage, -1, activityRecord.launchedFromUid, STACK_INVISIBLE, null, false, SHOW_APP_STARTING_PREVIEW, null, null, null) == 0 ? SHOW_APP_STARTING_PREVIEW : false;
                        } catch (RemoteException e2) {
                            foundParentInTask = false;
                        }
                        requestFinishActivityLocked(activityRecord.appToken, resultCode, resultData, "navigate-top", SHOW_APP_STARTING_PREVIEW);
                    }
                }
                Binder.restoreCallingIdentity(origId);
                return foundParentInTask;
            }

            final void cleanUpActivityLocked(ActivityRecord r, boolean cleanServices, boolean setState) {
                if (this.mResumedActivity == r) {
                    this.mResumedActivity = null;
                }
                if (this.mPausingActivity == r) {
                    this.mPausingActivity = null;
                }
                this.mService.resetFocusedActivityIfNeededLocked(r);
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
                this.mStackSupervisor.mFinishingActivities.remove(r);
                this.mStackSupervisor.mWaitingVisibleActivities.remove(r);
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
            }

            private void removeTimeoutsForActivityLocked(ActivityRecord r) {
                this.mStackSupervisor.removeTimeoutsForActivityLocked(r);
                this.mHandler.removeMessages(PAUSE_TIMEOUT_MSG, r);
                this.mHandler.removeMessages(STOP_TIMEOUT_MSG, r);
                this.mHandler.removeMessages(DESTROY_TIMEOUT_MSG, r);
                r.finishLaunchTickingLocked();
            }

            private void removeActivityFromHistoryLocked(ActivityRecord r, TaskRecord oldTop, String reason) {
                this.mStackSupervisor.removeChildActivityContainers(r);
                finishActivityResultsLocked(r, STACK_INVISIBLE, null);
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
                this.mWindowManager.removeAppToken(r.appToken);
                TaskRecord task = r.task;
                TaskRecord topTask = oldTop != null ? oldTop : topTask();
                if (task != null && task.removeActivity(r)) {
                    if (ActivityManagerDebugConfig.DEBUG_STACK) {
                        Slog.i(TAG_STACK, "removeActivityFromHistoryLocked: last activity removed from " + this);
                    }
                    if (this.mStackSupervisor.isFocusedStack(this) && task == topTask && task.isOverHomeStack()) {
                        this.mStackSupervisor.moveHomeStackTaskToTop(task.getTaskToReturnTo(), reason);
                    }
                    removeTask(task, reason);
                }
                cleanUpActivityServicesLocked(r);
                r.removeUriPermissionsLocked();
            }

            final void cleanUpActivityServicesLocked(ActivityRecord r) {
                if (r.connections != null) {
                    Iterator<ConnectionRecord> it = r.connections.iterator();
                    while (it.hasNext()) {
                        this.mService.mServices.removeConnectionLocked((ConnectionRecord) it.next(), null, r);
                    }
                    r.connections = null;
                }
            }

            final void scheduleDestroyActivities(ProcessRecord owner, String reason) {
                Message msg = this.mHandler.obtainMessage(DESTROY_ACTIVITIES_MSG);
                msg.obj = new ScheduleDestroyArgs(owner, reason);
                this.mHandler.sendMessage(msg);
            }

            final void destroyActivitiesLocked(ProcessRecord owner, String reason) {
                boolean lastIsOpaque = false;
                boolean activityRemoved = false;
                for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
                    ArrayList<ActivityRecord> activities = ((TaskRecord) this.mTaskHistory.get(taskNdx)).mActivities;
                    for (int activityNdx = activities.size() - 1; activityNdx >= 0; activityNdx--) {
                        ActivityRecord r = (ActivityRecord) activities.get(activityNdx);
                        if (!r.finishing) {
                            if (r.fullscreen) {
                                lastIsOpaque = SHOW_APP_STARTING_PREVIEW;
                            }
                            if ((owner == null || r.app == owner) && lastIsOpaque && r.isDestroyable()) {
                                if (ActivityManagerDebugConfig.DEBUG_SWITCH) {
                                    Slog.v(TAG_SWITCH, "Destroying " + r + " in state " + r.state + " resumed=" + this.mResumedActivity + " pausing=" + this.mPausingActivity + " for reason " + reason);
                                }
                                if (destroyActivityLocked(r, SHOW_APP_STARTING_PREVIEW, reason)) {
                                    activityRemoved = SHOW_APP_STARTING_PREVIEW;
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
                return destroyActivityLocked(r, SHOW_APP_STARTING_PREVIEW, reason);
            }

            final int releaseSomeActivitiesLocked(ProcessRecord app, ArraySet<TaskRecord> tasks, String reason) {
                if (ActivityManagerDebugConfig.DEBUG_RELEASE) {
                    Slog.d(TAG_RELEASE, "Trying to release some activities in " + app);
                }
                int maxTasks = tasks.size() / 4;
                if (maxTasks < STACK_VISIBLE) {
                    maxTasks = STACK_VISIBLE;
                }
                int numReleased = STACK_INVISIBLE;
                int taskNdx = STACK_INVISIBLE;
                while (taskNdx < this.mTaskHistory.size() && maxTasks > 0) {
                    TaskRecord task = (TaskRecord) this.mTaskHistory.get(taskNdx);
                    if (tasks.contains(task)) {
                        if (ActivityManagerDebugConfig.DEBUG_RELEASE) {
                            Slog.d(TAG_RELEASE, "Looking for activities to release in " + task);
                        }
                        int curNum = STACK_INVISIBLE;
                        ArrayList<ActivityRecord> activities = task.mActivities;
                        int actNdx = STACK_INVISIBLE;
                        while (actNdx < activities.size()) {
                            ActivityRecord activity = (ActivityRecord) activities.get(actNdx);
                            if (activity.app == app && activity.isDestroyable()) {
                                if (ActivityManagerDebugConfig.DEBUG_RELEASE) {
                                    Slog.v(TAG_RELEASE, "Destroying " + activity + " in state " + activity.state + " resumed=" + this.mResumedActivity + " pausing=" + this.mPausingActivity + " for reason " + reason);
                                }
                                destroyActivityLocked(activity, SHOW_APP_STARTING_PREVIEW, reason);
                                if (activities.get(actNdx) != activity) {
                                    actNdx--;
                                }
                                curNum += STACK_VISIBLE;
                            }
                            actNdx += STACK_VISIBLE;
                        }
                        if (curNum > 0) {
                            numReleased += curNum;
                            maxTasks--;
                            if (this.mTaskHistory.get(taskNdx) != task) {
                                taskNdx--;
                            }
                        }
                    }
                    taskNdx += STACK_VISIBLE;
                }
                if (ActivityManagerDebugConfig.DEBUG_RELEASE) {
                    Slog.d(TAG_RELEASE, "Done releasing: did " + numReleased + " activities");
                }
                return numReleased;
            }

            final boolean destroyActivityLocked(ActivityRecord r, boolean removeFromApp, String reason) {
                boolean hadApp = SHOW_APP_STARTING_PREVIEW;
                if (ActivityManagerDebugConfig.DEBUG_SWITCH || ActivityManagerDebugConfig.DEBUG_CLEANUP) {
                    Slog.v(TAG_SWITCH, "Removing activity from " + reason + ": token=" + r + ", app=" + (r.app != null ? r.app.processName : "(null)"));
                }
                EventLog.writeEvent(EventLogTags.AM_DESTROY_ACTIVITY, new Object[]{Integer.valueOf(r.userId), Integer.valueOf(System.identityHashCode(r)), Integer.valueOf(r.task.taskId), r.shortComponentName, reason});
                boolean removedFromHistory = false;
                TaskRecord topTask = topTask();
                cleanUpActivityLocked(r, false, false);
                if (ActivityManagerDebugConfig.DEBUG_SWITCH) {
                    Slog.i(TAG_SWITCH, "Activity has been cleaned up!");
                }
                if (r.app == null) {
                    hadApp = false;
                }
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
                            removeActivityFromHistoryLocked(r, topTask, reason + " exceptionInScheduleDestroy");
                            removedFromHistory = SHOW_APP_STARTING_PREVIEW;
                            skipDestroy = SHOW_APP_STARTING_PREVIEW;
                        }
                    }
                    r.nowVisible = false;
                    if (!r.finishing || skipDestroy) {
                        if (ActivityManagerDebugConfig.DEBUG_STATES || HwSlog.HW_DEBUG_STATES) {
                            Slog.v(TAG_STATES, "Moving to DESTROYED: " + r + " (destroy skipped)");
                        }
                        r.state = ActivityState.DESTROYED;
                        if (ActivityManagerDebugConfig.DEBUG_APP) {
                            Slog.v(TAG_APP, "Clearing app during destroy for activity " + r);
                        }
                        r.app = null;
                    } else {
                        if (ActivityManagerDebugConfig.DEBUG_STATES) {
                            Slog.v(TAG_STATES, "Moving to DESTROYING: " + r + " (destroy requested)");
                        }
                        r.state = ActivityState.DESTROYING;
                        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(DESTROY_TIMEOUT_MSG, r), JobStatus.DEFAULT_TRIGGER_UPDATE_DELAY);
                    }
                } else if (r.finishing) {
                    removeActivityFromHistoryLocked(r, topTask, reason + " hadNoApp");
                    removedFromHistory = SHOW_APP_STARTING_PREVIEW;
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
                r.configChangeFlags = STACK_INVISIBLE;
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
                        this.mHandler.removeMessages(DESTROY_TIMEOUT_MSG, r);
                    }
                    if (ActivityManagerDebugConfig.DEBUG_CONTAINERS) {
                        Slog.d(TAG_CONTAINERS, "activityDestroyedLocked: r=" + r);
                    }
                    if (isInStackLocked(r) != null && r.state == ActivityState.DESTROYING) {
                        cleanUpActivityLocked(r, SHOW_APP_STARTING_PREVIEW, false);
                        removeActivityFromHistoryLocked(r, null, reason);
                    }
                    this.mStackSupervisor.mActivityLaunchTrack = "activityDestroyed";
                    this.mStackSupervisor.resumeFocusedStackTopActivityLocked();
                } finally {
                    Binder.restoreCallingIdentity(origId);
                }
            }

            void releaseBackgroundResources(ActivityRecord r) {
                if (hasVisibleBehindActivity() && !this.mHandler.hasMessages(RELEASE_BACKGROUND_RESOURCES_TIMEOUT_MSG) && (r != topRunningActivityLocked() || getStackVisibilityLocked(null) != STACK_VISIBLE)) {
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
                    this.mHandler.sendEmptyMessageDelayed(RELEASE_BACKGROUND_RESOURCES_TIMEOUT_MSG, 500);
                }
            }

            final void backgroundResourcesReleased() {
                this.mHandler.removeMessages(RELEASE_BACKGROUND_RESOURCES_TIMEOUT_MSG);
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

            boolean removeHistoryRecordsForAppLocked(ProcessRecord app) {
                removeHistoryRecordsForAppLocked(this.mLRUActivities, app, "mLRUActivities");
                removeHistoryRecordsForAppLocked(this.mStackSupervisor.mStoppingActivities, app, "mStoppingActivities");
                removeHistoryRecordsForAppLocked(this.mStackSupervisor.mGoingToSleepActivities, app, "mGoingToSleepActivities");
                removeHistoryRecordsForAppLocked(this.mStackSupervisor.mWaitingVisibleActivities, app, "mWaitingVisibleActivities");
                removeHistoryRecordsForAppLocked(this.mStackSupervisor.mFinishingActivities, app, "mFinishingActivities");
                boolean hasVisibleActivities = false;
                int i = numActivities();
                if (ActivityManagerDebugConfig.DEBUG_CLEANUP) {
                    Slog.v(TAG_CLEANUP, "Removing app " + app + " from history with " + i + " entries");
                }
                for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
                    ArrayList<ActivityRecord> activities = ((TaskRecord) this.mTaskHistory.get(taskNdx)).mActivities;
                    for (int activityNdx = activities.size() - 1; activityNdx >= 0; activityNdx--) {
                        ActivityRecord r = (ActivityRecord) activities.get(activityNdx);
                        i--;
                        if (ActivityManagerDebugConfig.DEBUG_CLEANUP) {
                            Slog.v(TAG_CLEANUP, "Record #" + i + " " + r + ": app=" + r.app);
                        }
                        if (r.app == app) {
                            boolean remove;
                            if (r.visible) {
                                hasVisibleActivities = SHOW_APP_STARTING_PREVIEW;
                            }
                            if ((!r.haveState && !r.stateNotNeeded) || r.finishing) {
                                remove = SHOW_APP_STARTING_PREVIEW;
                            } else if (!r.visible && r.launchCount > STACK_VISIBLE_ACTIVITY_BEHIND && r.lastLaunchTime > SystemClock.uptimeMillis() - 60000) {
                                remove = SHOW_APP_STARTING_PREVIEW;
                            } else if (r.launchCount <= 5 || r.lastLaunchTime <= SystemClock.uptimeMillis() - TRANSLUCENT_CONVERSION_TIMEOUT) {
                                remove = false;
                            } else {
                                remove = SHOW_APP_STARTING_PREVIEW;
                                Slog.v(TAG_CLEANUP, "too many launcher times, remove : " + r);
                            }
                            if (remove) {
                                if (ActivityManagerDebugConfig.DEBUG_ADD_REMOVE || ActivityManagerDebugConfig.DEBUG_CLEANUP) {
                                    Slog.i(TAG_ADD_REMOVE, "Removing activity " + r + " from stack at " + i + ": haveState=" + r.haveState + " stateNotNeeded=" + r.stateNotNeeded + " finishing=" + r.finishing + " state=" + r.state + " callers=" + Debug.getCallers(5));
                                }
                                if (!r.finishing) {
                                    Slog.w(TAG, "Force removing " + r + ": app died, no saved state");
                                    EventLog.writeEvent(EventLogTags.AM_FINISH_ACTIVITY, new Object[]{Integer.valueOf(r.userId), Integer.valueOf(System.identityHashCode(r)), Integer.valueOf(r.task.taskId), r.shortComponentName, "proc died without state saved"});
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
                            cleanUpActivityLocked(r, SHOW_APP_STARTING_PREVIEW, SHOW_APP_STARTING_PREVIEW);
                            if (remove) {
                                removeActivityFromHistoryLocked(r, null, "appDied");
                            }
                        }
                    }
                }
                return hasVisibleActivities;
            }

            final void updateTransitLocked(int transit, ActivityOptions options) {
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

            void updateTaskMovement(TaskRecord task, boolean toFront) {
                if (task.isPersistable) {
                    task.mLastTimeMoved = System.currentTimeMillis();
                    if (!toFront) {
                        task.mLastTimeMoved *= -1;
                    }
                }
                this.mStackSupervisor.invalidateTaskLayers();
            }

            void moveHomeStackTaskToTop(int homeStackTaskType) {
                int top = this.mTaskHistory.size() - 1;
                for (int taskNdx = top; taskNdx >= 0; taskNdx--) {
                    TaskRecord task = (TaskRecord) this.mTaskHistory.get(taskNdx);
                    if (task.taskType == homeStackTaskType) {
                        if (ActivityManagerDebugConfig.DEBUG_TASKS || ActivityManagerDebugConfig.DEBUG_STACK) {
                            Slog.d(TAG_STACK, "moveHomeStackTaskToTop: moving " + task);
                        }
                        this.mTaskHistory.remove(taskNdx);
                        this.mTaskHistory.add(top, task);
                        updateTaskMovement(task, SHOW_APP_STARTING_PREVIEW);
                        return;
                    }
                }
            }

            final void moveTaskToFrontLocked(TaskRecord tr, boolean noAnimation, ActivityOptions options, AppTimeTracker timeTracker, String reason) {
                if (ActivityManagerDebugConfig.DEBUG_SWITCH) {
                    Slog.v(TAG_SWITCH, "moveTaskToFront: " + tr);
                }
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
                if (okToShowLocked(top)) {
                    Flog.i(PAUSE_TIMEOUT_MSG, "moveTaskToFront: moving tr=" + tr + " reason=" + reason);
                    ActivityRecord r = topRunningActivityLocked();
                    this.mService.setFocusedActivityLocked(r, reason);
                    if (ActivityManagerDebugConfig.DEBUG_TRANSITION) {
                        Slog.v(TAG_TRANSITION, "Prepare to front transition: task=" + tr);
                    }
                    if (noAnimation) {
                        this.mWindowManager.prepareAppTransition(STACK_INVISIBLE, false);
                        if (r != null) {
                            this.mNoAnimActivities.add(r);
                        }
                        ActivityOptions.abort(options);
                    } else {
                        updateTransitLocked(10, options);
                    }
                    this.mStackSupervisor.mActivityLaunchTrack = "taskMove";
                    this.mStackSupervisor.resumeFocusedStackTopActivityLocked();
                    Object[] objArr = new Object[STACK_VISIBLE_ACTIVITY_BEHIND];
                    objArr[STACK_INVISIBLE] = Integer.valueOf(tr.userId);
                    objArr[STACK_VISIBLE] = Integer.valueOf(tr.taskId);
                    EventLog.writeEvent(EventLogTags.AM_TASK_TO_FRONT, objArr);
                    return;
                }
                addRecentActivityLocked(top);
                ActivityOptions.abort(options);
            }

            final boolean moveTaskToBackLocked(int taskId) {
                TaskRecord tr = taskForIdLocked(taskId);
                if (tr == null) {
                    Slog.i(TAG, "moveTaskToBack: bad taskId=" + taskId);
                    return false;
                }
                TaskRecord task;
                Slog.i(TAG, "moveTaskToBack: " + tr);
                this.mStackSupervisor.removeLockedTaskLocked(tr);
                if (this.mStackSupervisor.isFrontStack(this)) {
                    if (this.mService.mController != null) {
                        ActivityRecord next = topRunningActivityLocked(null, taskId);
                        if (next == null) {
                            next = topRunningActivityLocked(null, STACK_INVISIBLE);
                        }
                        if (next != null) {
                            boolean moveOK = SHOW_APP_STARTING_PREVIEW;
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
                }
                if (ActivityManagerDebugConfig.DEBUG_TRANSITION) {
                    Slog.v(TAG, "Prepare to back transition: task=" + taskId);
                }
                if (this.mStackId == 0 && topTask().isHomeTask()) {
                    ActivityStack fullscreenStack = this.mStackSupervisor.getStack(STACK_VISIBLE);
                    if (fullscreenStack != null && fullscreenStack.hasVisibleBehindActivity()) {
                        this.mService.setFocusedActivityLocked(fullscreenStack.getVisibleBehindActivity(), "moveTaskToBack");
                        this.mStackSupervisor.resumeFocusedStackTopActivityLocked();
                        return SHOW_APP_STARTING_PREVIEW;
                    }
                }
                boolean prevIsHome = false;
                boolean isOverHomeStack = !tr.isHomeTask() ? tr.isOverHomeStack() : false;
                if (isOverHomeStack) {
                    TaskRecord nextTask = getNextTask(tr);
                    if (nextTask != null) {
                        nextTask.setTaskToReturnTo(tr.getTaskToReturnTo());
                    } else {
                        prevIsHome = SHOW_APP_STARTING_PREVIEW;
                    }
                }
                this.mTaskHistory.remove(tr);
                this.mTaskHistory.add(STACK_INVISIBLE, tr);
                updateTaskMovement(tr, false);
                int numTasks = this.mTaskHistory.size();
                TaskRecord topTask = (TaskRecord) this.mTaskHistory.get(numTasks - 1);
                if (!(isOverHomeStack || topTask == null || topTask.topRunningActivityLocked() != null || !topTask.isOverHomeStack() || topTask.mActivities == null)) {
                    if (topTask.mActivities.size() != 0) {
                        isOverHomeStack = SHOW_APP_STARTING_PREVIEW;
                        Flog.i(PAUSE_TIMEOUT_MSG, "the top invalidate Task: " + topTask + " is overHome!");
                    }
                }
                int firstValidateTask = -1;
                boolean hasHomeTask = false;
                boolean noValidateTask = false;
                for (int taskNdx = STACK_VISIBLE; taskNdx < numTasks; taskNdx += STACK_VISIBLE) {
                    if (taskNdx >= this.mTaskHistory.size()) {
                        Flog.i(PAUSE_TIMEOUT_MSG, "moveTast Error becuase of out of bounds!");
                        break;
                    }
                    task = (TaskRecord) this.mTaskHistory.get(taskNdx);
                    if (task.topRunningActivityLocked() == null || tr.userId != task.userId) {
                        Flog.i(PAUSE_TIMEOUT_MSG, "inValidate task: " + task);
                    } else {
                        if (firstValidateTask < 0) {
                            firstValidateTask = taskNdx;
                        }
                        if (task.isOverHomeStack()) {
                            hasHomeTask = SHOW_APP_STARTING_PREVIEW;
                            break;
                        }
                    }
                }
                if (!hasHomeTask) {
                    if (firstValidateTask > 0) {
                        if (firstValidateTask < this.mTaskHistory.size()) {
                            Flog.i(PAUSE_TIMEOUT_MSG, "set first validate task!");
                            ((TaskRecord) this.mTaskHistory.get(firstValidateTask)).setTaskToReturnTo(STACK_VISIBLE);
                        }
                    }
                    Flog.i(PAUSE_TIMEOUT_MSG, "there is no validate task, should go home!");
                    noValidateTask = SHOW_APP_STARTING_PREVIEW;
                }
                this.mWindowManager.prepareAppTransition(11, false);
                this.mWindowManager.moveTaskToBottom(taskId);
                task = this.mResumedActivity != null ? this.mResumedActivity.task : null;
                Flog.i(PAUSE_TIMEOUT_MSG, "moveTaskToBack, setTaskToReturnTo: taskToReturnTo=" + tr.getTaskToReturnTo() + "numTasks=" + numTasks + ",isOnHomeDisplay=" + isOnHomeDisplay() + ",isOverHomeStack=" + tr.isOverHomeStack() + ", prevIsHome=" + prevIsHome);
                if (prevIsHome || ((task == tr && r4) || ((numTasks <= STACK_VISIBLE || noValidateTask) && isOnHomeDisplay()))) {
                    if (!this.mService.mBooting) {
                        if (!this.mService.mBooted) {
                            Flog.i(PAUSE_TIMEOUT_MSG, "Can't move task to back while boot isn't completed");
                            return false;
                        }
                    }
                    int taskToReturnTo = tr.getTaskToReturnTo();
                    tr.setTaskToReturnTo(STACK_INVISIBLE);
                    Flog.i(PAUSE_TIMEOUT_MSG, "moveTaskToBack, resuming home stack");
                    return this.mStackSupervisor.resumeHomeStackTask(taskToReturnTo, null, "moveTaskToBack");
                }
                this.mStackSupervisor.mActivityLaunchTrack = " taskToBack";
                this.mStackSupervisor.resumeFocusedStackTopActivityLocked();
                return SHOW_APP_STARTING_PREVIEW;
            }

            static final void logStartActivity(int tag, ActivityRecord r, TaskRecord task) {
                Uri data = r.intent.getData();
                String toSafeString = data != null ? data.toSafeString() : null;
                EventLog.writeEvent(tag, new Object[]{Integer.valueOf(r.userId), Integer.valueOf(System.identityHashCode(r)), Integer.valueOf(task.taskId), r.shortComponentName, r.intent.getAction(), r.intent.getType(), toSafeString, Integer.valueOf(r.intent.getFlags())});
            }

            void ensureVisibleActivitiesConfigurationLocked(ActivityRecord start, boolean preserveWindow) {
                if (start != null && start.visible) {
                    boolean behindFullscreen = false;
                    boolean updatedConfig = false;
                    for (int taskIndex = this.mTaskHistory.indexOf(start.task); taskIndex >= 0; taskIndex--) {
                        TaskRecord task = (TaskRecord) this.mTaskHistory.get(taskIndex);
                        ArrayList<ActivityRecord> activities = task.mActivities;
                        int activityIndex = start.task == task ? activities.indexOf(start) : activities.size() - 1;
                        while (activityIndex >= 0) {
                            ActivityRecord r = (ActivityRecord) activities.get(activityIndex);
                            updatedConfig |= ensureActivityConfigurationLocked(r, STACK_INVISIBLE, preserveWindow);
                            if (r.fullscreen) {
                                behindFullscreen = SHOW_APP_STARTING_PREVIEW;
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

            private int getTaskConfigurationChanges(ActivityRecord record, Configuration taskConfig, Configuration oldTaskOverride) {
                if (Configuration.EMPTY.equals(oldTaskOverride) && !Configuration.EMPTY.equals(taskConfig)) {
                    oldTaskOverride = record.task.extractOverrideConfig(record.configuration);
                }
                if (Configuration.EMPTY.equals(taskConfig) && !Configuration.EMPTY.equals(oldTaskOverride)) {
                    taskConfig = record.task.extractOverrideConfig(record.configuration);
                }
                int taskChanges = oldTaskOverride.diff(taskConfig);
                if ((taskChanges & DumpState.DUMP_PROVIDERS) != 0) {
                    boolean crosses;
                    if (record.crossesHorizontalSizeThreshold(oldTaskOverride.screenWidthDp, taskConfig.screenWidthDp)) {
                        crosses = SHOW_APP_STARTING_PREVIEW;
                    } else {
                        crosses = record.crossesVerticalSizeThreshold(oldTaskOverride.screenHeightDp, taskConfig.screenHeightDp);
                    }
                    if (!crosses) {
                        taskChanges &= -1025;
                    }
                }
                if (!((taskChanges & DumpState.DUMP_VERIFIERS) == 0 || record.crossesSmallestSizeThreshold(oldTaskOverride.smallestScreenWidthDp, taskConfig.smallestScreenWidthDp))) {
                    taskChanges &= -2049;
                }
                return catchConfigChangesFromUnset(taskConfig, oldTaskOverride, taskChanges);
            }

            private static int catchConfigChangesFromUnset(Configuration taskConfig, Configuration oldTaskOverride, int taskChanges) {
                if (taskChanges != 0) {
                    return taskChanges;
                }
                int oldWidth;
                int newWidth;
                int oldSmallest;
                int newSmallest;
                int oldLayout;
                int newLayout;
                if (oldTaskOverride.orientation != taskConfig.orientation) {
                    taskChanges |= DumpState.DUMP_PACKAGES;
                }
                int oldHeight = oldTaskOverride.screenHeightDp;
                int newHeight = taskConfig.screenHeightDp;
                if (oldHeight != 0 || newHeight == 0) {
                    if (oldHeight != 0 && newHeight == 0) {
                    }
                    oldWidth = oldTaskOverride.screenWidthDp;
                    newWidth = taskConfig.screenWidthDp;
                    if (oldWidth != 0 || newWidth == 0) {
                        if (oldWidth != 0 && newWidth == 0) {
                        }
                        oldSmallest = oldTaskOverride.smallestScreenWidthDp;
                        newSmallest = taskConfig.smallestScreenWidthDp;
                        if (oldSmallest != 0 || newSmallest == 0) {
                            if (oldSmallest != 0 && newSmallest == 0) {
                            }
                            oldLayout = oldTaskOverride.screenLayout;
                            newLayout = taskConfig.screenLayout;
                            if (oldLayout != 0 || newLayout == 0) {
                                if (oldLayout == 0 && newLayout == 0) {
                                }
                            }
                            return taskChanges | DumpState.DUMP_SHARED_USERS;
                        }
                        taskChanges |= DumpState.DUMP_VERIFIERS;
                        oldLayout = oldTaskOverride.screenLayout;
                        newLayout = taskConfig.screenLayout;
                        return oldLayout == 0 ? taskChanges : taskChanges;
                    }
                    taskChanges |= DumpState.DUMP_PROVIDERS;
                    oldSmallest = oldTaskOverride.smallestScreenWidthDp;
                    newSmallest = taskConfig.smallestScreenWidthDp;
                    taskChanges |= DumpState.DUMP_VERIFIERS;
                    oldLayout = oldTaskOverride.screenLayout;
                    newLayout = taskConfig.screenLayout;
                    if (oldLayout == 0) {
                    }
                }
                taskChanges |= DumpState.DUMP_PROVIDERS;
                oldWidth = oldTaskOverride.screenWidthDp;
                newWidth = taskConfig.screenWidthDp;
                taskChanges |= DumpState.DUMP_PROVIDERS;
                oldSmallest = oldTaskOverride.smallestScreenWidthDp;
                newSmallest = taskConfig.smallestScreenWidthDp;
                taskChanges |= DumpState.DUMP_VERIFIERS;
                oldLayout = oldTaskOverride.screenLayout;
                newLayout = taskConfig.screenLayout;
                if (oldLayout == 0) {
                }
            }

            private static boolean isResizeOnlyChange(int change) {
                return (change & -3457) == 0 ? SHOW_APP_STARTING_PREVIEW : false;
            }

            private void relaunchActivityLocked(ActivityRecord r, int changes, boolean andResume, boolean preserveWindow) {
                if (this.mService.mSuppressResizeConfigChanges && preserveWindow) {
                    r.configChangeFlags = STACK_INVISIBLE;
                    return;
                }
                int i;
                List list = null;
                List newIntents = null;
                if (andResume) {
                    list = r.results;
                    newIntents = r.newIntents;
                }
                if (ActivityManagerDebugConfig.DEBUG_SWITCH) {
                    Slog.v(TAG_SWITCH, "Relaunching: " + r + " with results=" + list + " newIntents=" + newIntents + " andResume=" + andResume + " preserveWindow=" + preserveWindow);
                }
                if (andResume) {
                    i = EventLogTags.AM_RELAUNCH_RESUME_ACTIVITY;
                } else {
                    i = EventLogTags.AM_RELAUNCH_ACTIVITY;
                }
                EventLog.writeEvent(i, new Object[]{Integer.valueOf(r.userId), Integer.valueOf(System.identityHashCode(r)), Integer.valueOf(r.task.taskId), r.shortComponentName});
                r.startFreezingScreenLocked(r.app, STACK_INVISIBLE);
                this.mStackSupervisor.removeChildActivityContainers(r);
                try {
                    if (ActivityManagerDebugConfig.DEBUG_SWITCH || ActivityManagerDebugConfig.DEBUG_STATES) {
                        Slog.i(TAG_SWITCH, "Moving to " + (andResume ? "RESUMED" : "PAUSED") + " Relaunching " + r + " callers=" + Debug.getCallers(6));
                    }
                    r.forceNewConfig = false;
                    this.mStackSupervisor.activityRelaunchingLocked(r);
                    r.app.thread.scheduleRelaunchActivity(r.appToken, list, newIntents, changes, andResume ? false : SHOW_APP_STARTING_PREVIEW, new Configuration(this.mService.mConfiguration), new Configuration(r.task.mOverrideConfig), preserveWindow);
                } catch (RemoteException e) {
                    if (ActivityManagerDebugConfig.DEBUG_SWITCH || ActivityManagerDebugConfig.DEBUG_STATES) {
                        Slog.i(TAG_SWITCH, "Relaunch failed", e);
                    }
                }
                if (andResume) {
                    if (ActivityManagerDebugConfig.DEBUG_STATES) {
                        Slog.d(TAG_STATES, "Resumed after relaunch " + r);
                    }
                    r.state = ActivityState.RESUMED;
                    if (!r.visible || r.stopped) {
                        this.mWindowManager.setAppVisibility(r.appToken, SHOW_APP_STARTING_PREVIEW);
                        completeResumeLocked(r);
                    } else {
                        r.results = null;
                        r.newIntents = null;
                    }
                    this.mService.showUnsupportedZoomDialogIfNeededLocked(r);
                    this.mService.showAskCompatModeDialogLocked(r);
                } else {
                    this.mHandler.removeMessages(PAUSE_TIMEOUT_MSG, r);
                    r.state = ActivityState.PAUSED;
                }
                r.configChangeFlags = STACK_INVISIBLE;
                r.deferRelaunchUntilPaused = false;
                r.preserveWindowOnDeferredRelaunch = false;
            }

            boolean willActivityBeVisibleLocked(IBinder token) {
                ActivityRecord r;
                for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
                    ArrayList<ActivityRecord> activities = ((TaskRecord) this.mTaskHistory.get(taskNdx)).mActivities;
                    for (int activityNdx = activities.size() - 1; activityNdx >= 0; activityNdx--) {
                        r = (ActivityRecord) activities.get(activityNdx);
                        if (r.appToken == token) {
                            return SHOW_APP_STARTING_PREVIEW;
                        }
                        if (r.fullscreen && !r.finishing) {
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
                return r.finishing ? false : SHOW_APP_STARTING_PREVIEW;
            }

            void closeSystemDialogsLocked() {
                for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
                    ArrayList<ActivityRecord> activities = ((TaskRecord) this.mTaskHistory.get(taskNdx)).mActivities;
                    for (int activityNdx = activities.size() - 1; activityNdx >= 0; activityNdx--) {
                        ActivityRecord r = (ActivityRecord) activities.get(activityNdx);
                        if ((r.info.flags & DumpState.DUMP_SHARED_USERS) != 0) {
                            finishActivityLocked(r, STACK_INVISIBLE, null, "close-sys", SHOW_APP_STARTING_PREVIEW);
                        }
                    }
                }
            }

            /* JADX WARNING: inconsistent code. */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            boolean finishDisabledPackageActivitiesLocked(String packageName, Set<String> filterByClasses, boolean doit, boolean evenPersistent, int userId) {
                boolean didSomething = false;
                TaskRecord lastTask = null;
                ComponentName homeActivity = null;
                for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
                    ArrayList<ActivityRecord> activities = ((TaskRecord) this.mTaskHistory.get(taskNdx)).mActivities;
                    int numActivities = activities.size();
                    int activityNdx = STACK_INVISIBLE;
                    while (activityNdx < numActivities) {
                        try {
                            boolean sameComponent;
                            ActivityRecord r = (ActivityRecord) activities.get(activityNdx);
                            if (r.packageName.equals(packageName)) {
                                if (filterByClasses != null) {
                                }
                                sameComponent = SHOW_APP_STARTING_PREVIEW;
                                if ((userId == -1 || r.userId == userId) && ((sameComponent || r.task == r12) && (r.app == null || evenPersistent || !r.app.persistent))) {
                                    if (!doit) {
                                        if (r.isHomeActivity()) {
                                            if (homeActivity == null && homeActivity.equals(r.realActivity)) {
                                                Slog.i(TAG, "Skip force-stop again " + r);
                                            } else {
                                                homeActivity = r.realActivity;
                                            }
                                        }
                                        didSomething = SHOW_APP_STARTING_PREVIEW;
                                        Slog.i(TAG, "  Force finishing activity " + r);
                                        if (sameComponent) {
                                            if (r.app != null) {
                                                r.app.removed = SHOW_APP_STARTING_PREVIEW;
                                            }
                                            r.app = null;
                                        }
                                        lastTask = r.task;
                                        if (finishActivityLocked(r, STACK_INVISIBLE, null, "force-stop", SHOW_APP_STARTING_PREVIEW)) {
                                            numActivities--;
                                            activityNdx--;
                                        }
                                    } else if (!r.finishing) {
                                        return SHOW_APP_STARTING_PREVIEW;
                                    }
                                }
                                activityNdx += STACK_VISIBLE;
                            }
                            sameComponent = (packageName == null && r.userId == userId) ? SHOW_APP_STARTING_PREVIEW : false;
                            if (!doit) {
                                if (r.isHomeActivity()) {
                                    if (homeActivity == null) {
                                    }
                                    homeActivity = r.realActivity;
                                }
                                didSomething = SHOW_APP_STARTING_PREVIEW;
                                Slog.i(TAG, "  Force finishing activity " + r);
                                if (sameComponent) {
                                    if (r.app != null) {
                                        r.app.removed = SHOW_APP_STARTING_PREVIEW;
                                    }
                                    r.app = null;
                                }
                                lastTask = r.task;
                                if (finishActivityLocked(r, STACK_INVISIBLE, null, "force-stop", SHOW_APP_STARTING_PREVIEW)) {
                                    numActivities--;
                                    activityNdx--;
                                }
                            } else if (!r.finishing) {
                                return SHOW_APP_STARTING_PREVIEW;
                            }
                        } catch (IndexOutOfBoundsException e) {
                            Slog.e(TAG, "IndexOutOfBoundsException: Index: +" + activityNdx + ", Size: " + activities.size());
                        }
                        activityNdx += STACK_VISIBLE;
                    }
                }
                return didSomething;
            }

            void getTasksLocked(List<RunningTaskInfo> list, int callingUid, boolean allowed) {
                boolean focusedStack = this.mStackSupervisor.getFocusedStack() == this ? SHOW_APP_STARTING_PREVIEW : false;
                boolean topTask = SHOW_APP_STARTING_PREVIEW;
                for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
                    TaskRecord task = (TaskRecord) this.mTaskHistory.get(taskNdx);
                    if (task.getTopActivity() != null) {
                        ActivityRecord r = null;
                        ActivityRecord top = null;
                        int numActivities = STACK_INVISIBLE;
                        int numRunning = STACK_INVISIBLE;
                        ArrayList<ActivityRecord> activities = task.mActivities;
                        if (allowed || task.isHomeTask() || task.effectiveUid == callingUid) {
                            for (int activityNdx = activities.size() - 1; activityNdx >= 0; activityNdx--) {
                                ActivityRecord tmp = (ActivityRecord) activities.get(activityNdx);
                                if (!tmp.finishing) {
                                    r = tmp;
                                    if (top == null || top.state == ActivityState.INITIALIZING) {
                                        top = tmp;
                                        numRunning = STACK_INVISIBLE;
                                        numActivities = STACK_INVISIBLE;
                                    }
                                    numActivities += STACK_VISIBLE;
                                    if (!(tmp.app == null || tmp.app.thread == null)) {
                                        numRunning += STACK_VISIBLE;
                                    }
                                    if (ActivityManagerDebugConfig.DEBUG_ALL) {
                                        Slog.v(TAG, tmp.intent.getComponent().flattenToShortString() + ": task=" + tmp.task);
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
                            if (top.task != null) {
                                ci.description = top.task.lastDescription;
                            }
                            ci.numActivities = numActivities;
                            ci.numRunning = numRunning;
                            ci.isDockable = task.canGoInDockedStack();
                            ci.resizeMode = task.mResizeMode;
                            list.add(ci);
                        }
                    }
                }
            }

            public void unhandledBackLocked() {
                int top = this.mTaskHistory.size() - 1;
                if (ActivityManagerDebugConfig.DEBUG_SWITCH) {
                    Slog.d(TAG_SWITCH, "Performing unhandledBack(): top activity at " + top);
                }
                if (top >= 0) {
                    ArrayList<ActivityRecord> activities = ((TaskRecord) this.mTaskHistory.get(top)).mActivities;
                    int activityTop = activities.size() - 1;
                    if (activityTop > 0) {
                        finishActivityLocked((ActivityRecord) activities.get(activityTop), STACK_INVISIBLE, null, "unhandled-back", SHOW_APP_STARTING_PREVIEW);
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
                            finishCurrentActivityLocked(activityRecord, STACK_INVISIBLE, false);
                        }
                    }
                }
            }

            boolean dumpActivitiesLocked(FileDescriptor fd, PrintWriter pw, boolean dumpAll, boolean dumpClient, String dumpPackage, boolean needSep, String header) {
                boolean printed = false;
                for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
                    TaskRecord task = (TaskRecord) this.mTaskHistory.get(taskNdx);
                    printed |= ActivityStackSupervisor.dumpHistoryList(fd, pw, ((TaskRecord) this.mTaskHistory.get(taskNdx)).mActivities, "    ", "Hist", SHOW_APP_STARTING_PREVIEW, dumpAll ? false : SHOW_APP_STARTING_PREVIEW, dumpClient, dumpPackage, needSep, header, "    Task id #" + task.taskId + "\n" + "    mFullscreen=" + task.mFullscreen + "\n" + "    mBounds=" + task.mBounds + "\n" + "    mMinWidth=" + task.mMinWidth + "\n" + "    mMinHeight=" + task.mMinHeight + "\n" + "    mLastNonFullscreenBounds=" + task.mLastNonFullscreenBounds);
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
                            a.forceNewConfig = SHOW_APP_STARTING_PREVIEW;
                            if (starting != null && a == starting && a.visible) {
                                a.startFreezingScreenLocked(starting.app, DumpState.DUMP_SHARED_USERS);
                            }
                        }
                    }
                }
                return starting;
            }

            void removeTask(TaskRecord task, String reason) {
                removeTask(task, reason, STACK_INVISIBLE);
            }

            void removeTask(TaskRecord task, String reason, int mode) {
                if (mode == 0) {
                    this.mStackSupervisor.removeLockedTaskLocked(task);
                    this.mWindowManager.removeTask(task.taskId);
                    if (!StackId.persistTaskBounds(this.mStackId)) {
                        task.updateOverrideConfiguration(null);
                    }
                }
                ActivityRecord r = this.mResumedActivity;
                if (r != null && r.task == task) {
                    this.mResumedActivity = null;
                }
                int taskNdx = this.mTaskHistory.indexOf(task);
                int topTaskNdx = this.mTaskHistory.size() - 1;
                if (task.isOverHomeStack() && taskNdx < topTaskNdx) {
                    TaskRecord nextTask = (TaskRecord) this.mTaskHistory.get(taskNdx + STACK_VISIBLE);
                    if (!nextTask.isOverHomeStack()) {
                        Flog.i(PAUSE_TIMEOUT_MSG, "removeTask, setTaskToReturnTo: HOME");
                        nextTask.setTaskToReturnTo(STACK_VISIBLE);
                    }
                }
                Flog.i(PAUSE_TIMEOUT_MSG, "Task removed: " + task + ", reason: " + reason);
                this.mTaskHistory.remove(task);
                updateTaskMovement(task, SHOW_APP_STARTING_PREVIEW);
                if (mode == 0 && task.mActivities.isEmpty()) {
                    boolean isVoiceSession = task.voiceSession != null ? SHOW_APP_STARTING_PREVIEW : false;
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
                }
                if (this.mTaskHistory.isEmpty()) {
                    if (ActivityManagerDebugConfig.DEBUG_STACK) {
                        Slog.i(TAG_STACK, "removeTask: removing stack=" + this);
                    }
                    Flog.i(PAUSE_TIMEOUT_MSG, "removeTask: removing stack=" + this);
                    if (isOnHomeDisplay() && mode != STACK_VISIBLE_ACTIVITY_BEHIND && this.mStackSupervisor.isFocusedStack(this)) {
                        String myReason = reason + " leftTaskHistoryEmpty";
                        if (this.mFullscreen || !adjustFocusToNextFocusableStackLocked(task.getTaskToReturnTo(), myReason)) {
                            this.mStackSupervisor.moveHomeStackToFront(myReason);
                        }
                    }
                    if (this.mStacks != null) {
                        this.mStacks.remove(this);
                        this.mStacks.add(STACK_INVISIBLE, this);
                    }
                    if (!isHomeStack()) {
                        this.mActivityContainer.onTaskListEmptyLocked();
                    }
                }
                task.stack = null;
            }

            TaskRecord createTaskRecord(int taskId, ActivityInfo info, Intent intent, IVoiceInteractionSession voiceSession, IVoiceInteractor voiceInteractor, boolean toTop) {
                TaskRecord task = HwServiceFactory.createTaskRecord(this.mService, taskId, info, intent, voiceSession, voiceInteractor);
                addTask(task, toTop, "createTaskRecord");
                boolean isLockscreenShown = this.mService.mLockScreenShown == STACK_VISIBLE_ACTIVITY_BEHIND ? SHOW_APP_STARTING_PREVIEW : false;
                if (!(layoutTaskInStack(task, info.windowLayout) || this.mBounds == null || !task.isResizeable() || isLockscreenShown)) {
                    task.updateOverrideConfiguration(this.mBounds);
                }
                return task;
            }

            boolean layoutTaskInStack(TaskRecord task, WindowLayout windowLayout) {
                if (this.mTaskPositioner == null) {
                    return false;
                }
                this.mTaskPositioner.updateDefaultBounds(task, this.mTaskHistory, windowLayout);
                return SHOW_APP_STARTING_PREVIEW;
            }

            ArrayList<TaskRecord> getAllTasks() {
                return new ArrayList(this.mTaskHistory);
            }

            void addTask(TaskRecord task, boolean toTop, String reason) {
                ActivityStack prevStack = preAddTask(task, reason, toTop);
                task.stack = this;
                if (toTop) {
                    insertTaskAtTop(task, null);
                } else {
                    this.mTaskHistory.add(STACK_INVISIBLE, task);
                    updateTaskMovement(task, false);
                }
                postAddTask(task, prevStack);
            }

            void positionTask(TaskRecord task, int position) {
                ActivityRecord topRunningActivity = task.topRunningActivityLocked();
                boolean wasResumed = topRunningActivity == task.stack.mResumedActivity ? SHOW_APP_STARTING_PREVIEW : false;
                ActivityStack prevStack = preAddTask(task, "positionTask", false);
                task.stack = this;
                insertTaskAtPosition(task, position);
                postAddTask(task, prevStack);
                if (wasResumed) {
                    if (this.mResumedActivity != null) {
                        Log.wtf(TAG, "mResumedActivity was already set when moving mResumedActivity from other stack to this stack mResumedActivity=" + this.mResumedActivity + " other mResumedActivity=" + topRunningActivity);
                    }
                    this.mResumedActivity = topRunningActivity;
                }
            }

            private ActivityStack preAddTask(TaskRecord task, String reason, boolean toTop) {
                ActivityStack prevStack = task.stack;
                if (!(prevStack == null || prevStack == this)) {
                    prevStack.removeTask(task, reason, toTop ? STACK_VISIBLE_ACTIVITY_BEHIND : STACK_VISIBLE);
                }
                return prevStack;
            }

            private void postAddTask(TaskRecord task, ActivityStack prevStack) {
                if (prevStack != null) {
                    this.mStackSupervisor.scheduleReportPictureInPictureModeChangedIfNeeded(task, prevStack);
                } else if (task.voiceSession != null) {
                    try {
                        task.voiceSession.taskStarted(task.intent, task.taskId);
                    } catch (RemoteException e) {
                    }
                }
            }

            void addConfigOverride(ActivityRecord r, TaskRecord task) {
                this.mWindowManager.addAppToken(task.mActivities.indexOf(r), r.appToken, r.task.taskId, this.mStackId, r.info.screenOrientation, r.fullscreen, (r.info.flags & DumpState.DUMP_PROVIDERS) != 0 ? SHOW_APP_STARTING_PREVIEW : false, r.userId, r.info.configChanges, task.voiceSession != null ? SHOW_APP_STARTING_PREVIEW : false, r.mLaunchTaskBehind, task.updateOverrideConfigurationFromLaunchBounds(), task.mOverrideConfig, task.mResizeMode, r.isAlwaysFocusable(), task.isHomeTask(), r.appInfo.targetSdkVersion, r.info.navigationHide);
                r.taskConfigOverride = task.mOverrideConfig;
            }

            void moveToFrontAndResumeStateIfNeeded(ActivityRecord r, boolean moveToFront, boolean setResume, String reason) {
                if (moveToFront) {
                    if (setResume) {
                        this.mResumedActivity = r;
                    }
                    moveToFront(reason);
                }
            }

            void moveActivityToStack(ActivityRecord r) {
                boolean wasFocused = false;
                ActivityStack prevStack = r.task.stack;
                if (prevStack.mStackId != this.mStackId) {
                    if (this.mStackSupervisor.isFocusedStack(prevStack) && this.mStackSupervisor.topRunningActivityLocked() == r) {
                        wasFocused = SHOW_APP_STARTING_PREVIEW;
                    }
                    boolean wasResumed = (wasFocused && prevStack.mResumedActivity == r) ? SHOW_APP_STARTING_PREVIEW : false;
                    TaskRecord task = createTaskRecord(this.mStackSupervisor.getNextTaskIdForUserLocked(r.userId), r.info, r.intent, null, null, SHOW_APP_STARTING_PREVIEW);
                    r.setTask(task, null);
                    task.addActivityToTop(r);
                    setAppTask(r, task);
                    this.mStackSupervisor.scheduleReportPictureInPictureModeChangedIfNeeded(task, prevStack);
                    moveToFrontAndResumeStateIfNeeded(r, wasFocused, wasResumed, "moveActivityToStack");
                    if (wasResumed) {
                        prevStack.mResumedActivity = null;
                    }
                }
            }

            private void setAppTask(ActivityRecord r, TaskRecord task) {
                this.mWindowManager.setAppTask(r.appToken, task.taskId, this.mStackId, task.updateOverrideConfigurationFromLaunchBounds(), task.mOverrideConfig, task.mResizeMode, task.isHomeTask());
                r.taskConfigOverride = task.mOverrideConfig;
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
        }
