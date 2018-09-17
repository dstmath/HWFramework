package com.android.server.am;

import android.annotation.IntDef;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.ActivityManager.StackId;
import android.app.ActivityManager.StackInfo;
import android.app.ActivityOptions;
import android.app.AppOpsManager;
import android.app.HwCustNonHardwareAcceleratedPackagesManager;
import android.app.IActivityContainer.Stub;
import android.app.IActivityContainerCallback;
import android.app.ProfilerInfo;
import android.app.WaitResult;
import android.app.admin.IDevicePolicyManager;
import android.common.HwFrameworkFactory;
import android.content.ComponentName;
import android.content.Context;
import android.content.IIntentSender;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.pm.UserInfo;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.hardware.display.DisplayManager;
import android.hardware.display.DisplayManager.DisplayListener;
import android.hardware.display.DisplayManagerGlobal;
import android.hardware.display.DisplayManagerInternal;
import android.hardware.display.VirtualDisplay;
import android.hardware.input.InputManagerInternal;
import android.hdm.HwDeviceManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Trace;
import android.os.TransactionTooLargeException;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.WorkSource;
import android.provider.Settings.Secure;
import android.provider.Settings.SettingNotFoundException;
import android.rms.HwSysResource;
import android.service.voice.IVoiceInteractionSession;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.BoostFramework;
import android.util.EventLog;
import android.util.Flog;
import android.util.HwPCUtils;
import android.util.IntArray;
import android.util.Jlog;
import android.util.MergedConfiguration;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.Display;
import android.view.InputEvent;
import android.view.Surface;
import android.widget.Toast;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.os.BackgroundThread;
import com.android.internal.os.TransferPipe;
import com.android.internal.statusbar.IStatusBarService;
import com.android.internal.util.ArrayUtils;
import com.android.internal.widget.LockPatternUtils;
import com.android.server.HwServiceFactory;
import com.android.server.LocalServices;
import com.android.server.SmartShrinker;
import com.android.server.UiThread;
import com.android.server.job.controllers.JobStatus;
import com.android.server.os.HwBootFail;
import com.android.server.wm.PinnedStackWindowController;
import com.android.server.wm.WindowManagerService;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class ActivityStackSupervisor extends AbsActivityStackSupervisor implements DisplayListener {
    private static final ArrayMap<String, String> ACTION_TO_RUNTIME_PERMISSION = new ArrayMap();
    private static final int ACTIVITY_RESTRICTION_APPOP = 2;
    private static final int ACTIVITY_RESTRICTION_NONE = 0;
    private static final int ACTIVITY_RESTRICTION_PERMISSION = 1;
    static final int CONTAINER_CALLBACK_TASK_LIST_EMPTY = 111;
    static final int CONTAINER_CALLBACK_VISIBILITY = 108;
    static final boolean CREATE_IF_NEEDED = true;
    static final boolean DEFER_RESUME = true;
    static final boolean FORCE_FOCUS = true;
    static final int HANDLE_DISPLAY_ADDED = 105;
    static final int HANDLE_DISPLAY_CHANGED = 106;
    static final int HANDLE_DISPLAY_REMOVED = 107;
    static final int IDLE_NOW_MSG = 101;
    static final int IDLE_TIMEOUT = 10000;
    static final int IDLE_TIMEOUT_MSG = 100;
    static final boolean IS_DEBUG_VERSION;
    static final int LAUNCH_TASK_BEHIND_COMPLETE = 112;
    static final int LAUNCH_TIMEOUT = 10000;
    static final int LAUNCH_TIMEOUT_MSG = 104;
    static final int LOCK_TASK_END_MSG = 110;
    static final int LOCK_TASK_START_MSG = 109;
    private static final String LOCK_TASK_TAG = "Lock-to-App";
    static final int MATCH_TASK_IN_STACKS_ONLY = 0;
    static final int MATCH_TASK_IN_STACKS_OR_RECENT_TASKS = 1;
    static final int MATCH_TASK_IN_STACKS_OR_RECENT_TASKS_AND_RESTORE = 2;
    private static final int MAX_TASK_IDS_PER_USER = 100000;
    static final boolean MOVING = true;
    static final boolean ON_TOP = true;
    static final boolean PAUSE_IMMEDIATELY = true;
    static final boolean PRESERVE_WINDOWS = true;
    static final boolean REMOVE_FROM_RECENTS = true;
    static final int REPORT_MULTI_WINDOW_MODE_CHANGED_MSG = 114;
    static final int REPORT_PIP_MODE_CHANGED_MSG = 115;
    static final int RESUME_TOP_ACTIVITY_MSG = 102;
    static final int SHOW_LOCK_TASK_ESCAPE_MESSAGE_MSG = 113;
    static final int SLEEP_TIMEOUT = 5000;
    static final int SLEEP_TIMEOUT_MSG = 103;
    public static final String TAG = "ActivityManager";
    private static final String TAG_CONTAINERS = (TAG + ActivityManagerDebugConfig.POSTFIX_CONTAINERS);
    private static final String TAG_FOCUS = (TAG + ActivityManagerDebugConfig.POSTFIX_FOCUS);
    private static final String TAG_IDLE = (TAG + ActivityManagerDebugConfig.POSTFIX_IDLE);
    private static final String TAG_LOCKTASK = (TAG + ActivityManagerDebugConfig.POSTFIX_LOCKTASK);
    private static final String TAG_PAUSE = (TAG + ActivityManagerDebugConfig.POSTFIX_PAUSE);
    private static final String TAG_RECENTS = (TAG + ActivityManagerDebugConfig.POSTFIX_RECENTS);
    private static final String TAG_RELEASE = (TAG + ActivityManagerDebugConfig.POSTFIX_RELEASE);
    private static final String TAG_STACK = (TAG + ActivityManagerDebugConfig.POSTFIX_STACK);
    private static final String TAG_STATES = (TAG + ActivityManagerDebugConfig.POSTFIX_STATES);
    private static final String TAG_SWITCH = (TAG + ActivityManagerDebugConfig.POSTFIX_SWITCH);
    static final String TAG_TASKS = (TAG + ActivityManagerDebugConfig.POSTFIX_TASKS);
    private static final String TAG_VISIBLE_BEHIND = (TAG + ActivityManagerDebugConfig.POSTFIX_VISIBLE_BEHIND);
    static final boolean VALIDATE_WAKE_LOCK_CALLER = false;
    private static final String VIRTUAL_DISPLAY_BASE_NAME = "ActivityViewVirtualDisplay";
    boolean inResumeTopActivity;
    public int[] lBoostCpuParamVal;
    public int[] lBoostPackParamVal;
    public int lBoostTimeOut = 0;
    public int lDisPackTimeOut = 0;
    final ArrayList<ActivityRecord> mActivitiesWaitingForVisibleActivity = new ArrayList();
    SparseArray<ActivityContainer> mActivityContainers = new SparseArray();
    protected final SparseArray<ActivityDisplay> mActivityDisplays = new SparseArray();
    String mActivityLaunchTrack = "";
    final ActivityMetricsLogger mActivityMetricsLogger;
    private boolean mAllowDockedStackResize = true;
    private HwSysResource mAppResource;
    boolean mAppVisibilitiesChangedSinceLastPause;
    private final SparseIntArray mCurTaskIdForUser = new SparseIntArray(20);
    int mCurrentUser;
    int mDefaultMinSizeOfResizeableTask = -1;
    private IDevicePolicyManager mDevicePolicyManager;
    private final SparseArray<IntArray> mDisplayAccessUIDs = new SparseArray();
    DisplayManager mDisplayManager;
    private DisplayManagerInternal mDisplayManagerInternal;
    final ArrayList<ActivityRecord> mFinishingActivities = new ArrayList();
    ActivityStack mFocusedStack;
    WakeLock mGoingToSleep;
    final ArrayList<ActivityRecord> mGoingToSleepActivities = new ArrayList();
    final ActivityStackSupervisorHandler mHandler;
    ActivityStack mHomeStack;
    private InputManagerInternal mInputManagerInternal;
    boolean mIsDockMinimized;
    public boolean mIsPerfBoostEnabled = false;
    public boolean mIsperfDisablepackingEnable = false;
    final KeyguardController mKeyguardController;
    private ActivityStack mLastFocusedStack;
    WakeLock mLaunchingActivity;
    private int mLockTaskModeState;
    ArrayList<TaskRecord> mLockTaskModeTasks = new ArrayList();
    private LockTaskNotify mLockTaskNotify;
    final ArrayList<ActivityRecord> mMultiWindowModeChangedActivities = new ArrayList();
    private int mNextFreeStackId = 7;
    public BoostFramework mPerfBoost = null;
    public BoostFramework mPerfPack = null;
    public BoostFramework mPerf_iop = null;
    final ArrayList<ActivityRecord> mPipModeChangedActivities = new ArrayList();
    Rect mPipModeChangedTargetStackBounds;
    private RecentTasks mRecentTasks;
    private final ArraySet<Integer> mResizingTasksDuringAnimation = new ArraySet();
    final ActivityManagerService mService;
    boolean mSleepTimeout = false;
    final ArrayList<UserState> mStartingUsers = new ArrayList();
    private IStatusBarService mStatusBarService;
    final ArrayList<ActivityRecord> mStoppingActivities = new ArrayList();
    private boolean mTaskLayersChanged = true;
    private final FindTaskResult mTmpFindTaskResult = new FindTaskResult();
    private SparseIntArray mTmpOrderedDisplayIds = new SparseIntArray();
    private IBinder mToken = new Binder();
    boolean mUserLeaving = false;
    SparseIntArray mUserStackInFront = new SparseIntArray(2);
    final ArrayList<WaitResult> mWaitingActivityLaunched = new ArrayList();
    private final ArrayList<WaitInfo> mWaitingForActivityVisible = new ArrayList();
    WindowManagerService mWindowManager;
    private final Rect tempRect = new Rect();

    public class ActivityContainer extends Stub {
        static final int CONTAINER_STATE_FINISHING = 2;
        static final int CONTAINER_STATE_HAS_SURFACE = 0;
        static final int CONTAINER_STATE_NO_SURFACE = 1;
        static final int FORCE_NEW_TASK_FLAGS = 402718720;
        ActivityDisplay mActivityDisplay;
        IActivityContainerCallback mCallback = null;
        int mContainerState = 0;
        String mIdString;
        ActivityRecord mParentActivity = null;
        ActivityStack mStack;
        final int mStackId;
        boolean mVisible = true;

        ActivityContainer(int stackId, ActivityDisplay activityDisplay, boolean onTop) {
            synchronized (ActivityStackSupervisor.this.mService) {
                try {
                    ActivityManagerService.boostPriorityForLockedSection();
                    this.mStackId = stackId;
                    this.mActivityDisplay = activityDisplay;
                    this.mIdString = "ActivtyContainer{" + this.mStackId + "}";
                    createStack(stackId, onTop);
                    if (ActivityManagerDebugConfig.DEBUG_STACK) {
                        Slog.d(ActivityStackSupervisor.TAG_STACK, "Creating " + this);
                    }
                } finally {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        protected void createStack(int stackId, boolean onTop) {
            switch (stackId) {
                case 4:
                    PinnedActivityStack pinnedActivityStack = new PinnedActivityStack(this, ActivityStackSupervisor.this.mRecentTasks, onTop);
                    return;
                default:
                    this.mStack = HwServiceFactory.createActivityStack(this, ActivityStackSupervisor.this.mRecentTasks, onTop);
                    return;
            }
        }

        void addToDisplayLocked(ActivityDisplay activityDisplay) {
            if (ActivityManagerDebugConfig.DEBUG_STACK) {
                Slog.d(ActivityStackSupervisor.TAG_STACK, "addToDisplayLocked: " + this + " to display=" + activityDisplay);
            }
            if (this.mActivityDisplay != null) {
                throw new IllegalStateException("ActivityContainer is already attached, displayId=" + this.mActivityDisplay.mDisplayId);
            }
            this.mActivityDisplay = activityDisplay;
            this.mStack.reparent(activityDisplay, true);
        }

        public void addToDisplay(int displayId) {
            synchronized (ActivityStackSupervisor.this.mService) {
                try {
                    ActivityManagerService.boostPriorityForLockedSection();
                    ActivityDisplay activityDisplay = ActivityStackSupervisor.this.getActivityDisplayOrCreateLocked(displayId);
                    if (activityDisplay == null) {
                    } else {
                        addToDisplayLocked(activityDisplay);
                        ActivityManagerService.resetPriorityAfterLockedSection();
                    }
                } finally {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        public int getDisplayId() {
            int i;
            synchronized (ActivityStackSupervisor.this.mService) {
                try {
                    ActivityManagerService.boostPriorityForLockedSection();
                    if (this.mActivityDisplay != null) {
                        i = this.mActivityDisplay.mDisplayId;
                    } else {
                        ActivityManagerService.resetPriorityAfterLockedSection();
                        return -1;
                    }
                } finally {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                }
            }
            return i;
        }

        public int getStackId() {
            int i;
            synchronized (ActivityStackSupervisor.this.mService) {
                try {
                    ActivityManagerService.boostPriorityForLockedSection();
                    i = this.mStackId;
                } finally {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                }
            }
            return i;
        }

        public boolean injectEvent(InputEvent event) {
            long origId = Binder.clearCallingIdentity();
            try {
                synchronized (ActivityStackSupervisor.this.mService) {
                    ActivityManagerService.boostPriorityForLockedSection();
                    if (this.mActivityDisplay != null) {
                        boolean injectInputEvent = ActivityStackSupervisor.this.mInputManagerInternal.injectInputEvent(event, this.mActivityDisplay.mDisplayId, 0);
                        ActivityManagerService.resetPriorityAfterLockedSection();
                        Binder.restoreCallingIdentity(origId);
                        return injectInputEvent;
                    }
                    ActivityManagerService.resetPriorityAfterLockedSection();
                    Binder.restoreCallingIdentity(origId);
                    return false;
                }
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(origId);
            }
        }

        public void release() {
            synchronized (ActivityStackSupervisor.this.mService) {
                long origId;
                try {
                    ActivityManagerService.boostPriorityForLockedSection();
                    if (this.mContainerState == 2) {
                        ActivityManagerService.resetPriorityAfterLockedSection();
                        return;
                    }
                    this.mContainerState = 2;
                    origId = Binder.clearCallingIdentity();
                    this.mStack.finishAllActivitiesLocked(false);
                    ActivityStackSupervisor.this.mService.mActivityStarter.removePendingActivityLaunchesLocked(this.mStack);
                    Binder.restoreCallingIdentity(origId);
                    ActivityManagerService.resetPriorityAfterLockedSection();
                } catch (Throwable th) {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        void removeLocked() {
            if (ActivityManagerDebugConfig.DEBUG_STACK) {
                Slog.d(ActivityStackSupervisor.TAG_STACK, "removeLocked: " + this + " from display=" + this.mActivityDisplay + " Callers=" + Debug.getCallers(2));
            }
            if (this.mActivityDisplay != null) {
                removeFromDisplayLocked();
            }
            this.mStack.remove();
        }

        private void removeFromDisplayLocked() {
            if (ActivityManagerDebugConfig.DEBUG_STACK) {
                Slog.d(ActivityStackSupervisor.TAG_STACK, "removeFromDisplayLocked: " + this + " current displayId=" + this.mActivityDisplay.mDisplayId);
            }
            this.mActivityDisplay.detachStack(this.mStack);
            this.mActivityDisplay = null;
        }

        void moveToDisplayLocked(ActivityDisplay activityDisplay, boolean onTop) {
            if (ActivityManagerDebugConfig.DEBUG_STACK) {
                Slog.d(ActivityStackSupervisor.TAG_STACK, "moveToDisplayLocked: " + this + " from display=" + this.mActivityDisplay + " to display=" + activityDisplay + " Callers=" + Debug.getCallers(2));
            }
            removeFromDisplayLocked();
            this.mActivityDisplay = activityDisplay;
            this.mStack.reparent(activityDisplay, onTop);
        }

        public final int startActivity(Intent intent) {
            return ActivityStackSupervisor.this.mService.startActivity(intent, this);
        }

        public final int startActivityIntentSender(IIntentSender intentSender) throws TransactionTooLargeException {
            ActivityStackSupervisor.this.mService.enforceNotIsolatedCaller("ActivityContainer.startActivityIntentSender");
            if (intentSender instanceof PendingIntentRecord) {
                PendingIntentRecord pendingIntent = (PendingIntentRecord) intentSender;
                checkEmbeddedAllowedInner(ActivityStackSupervisor.this.mService.mUserController.handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), ActivityStackSupervisor.this.mCurrentUser, false, 2, "ActivityContainer", null), pendingIntent.key.requestIntent, pendingIntent.key.requestResolvedType);
                return pendingIntent.sendInner(0, null, null, null, null, null, null, null, 0, FORCE_NEW_TASK_FLAGS, FORCE_NEW_TASK_FLAGS, null, this);
            }
            throw new IllegalArgumentException("Bad PendingIntent object");
        }

        void checkEmbeddedAllowedInner(int userId, Intent intent, String resolvedType) {
            ActivityInfo aInfo = ActivityStackSupervisor.this.resolveActivity(intent, resolvedType, 0, null, userId);
            if (aInfo != null && (aInfo.flags & Integer.MIN_VALUE) == 0) {
                throw new SecurityException("Attempt to embed activity that has not set allowEmbedded=\"true\"");
            }
        }

        public IBinder asBinder() {
            return this;
        }

        public void setSurface(Surface surface, int width, int height, int density) {
            ActivityStackSupervisor.this.mService.enforceNotIsolatedCaller("ActivityContainer.attachToSurface");
        }

        ActivityStackSupervisor getOuter() {
            return ActivityStackSupervisor.this;
        }

        boolean isAttachedLocked() {
            return this.mActivityDisplay != null;
        }

        void setVisible(boolean visible) {
            if (this.mVisible != visible) {
                this.mVisible = visible;
                if (this.mCallback != null) {
                    int i;
                    ActivityStackSupervisorHandler activityStackSupervisorHandler = ActivityStackSupervisor.this.mHandler;
                    if (visible) {
                        i = 1;
                    } else {
                        i = 0;
                    }
                    activityStackSupervisorHandler.obtainMessage(108, i, 0, this).sendToTarget();
                }
            }
        }

        void setDrawn() {
        }

        boolean isEligibleForNewTasks() {
            return true;
        }

        void onTaskListEmptyLocked() {
            removeLocked();
            ActivityStackSupervisor.this.mHandler.obtainMessage(111, this).sendToTarget();
        }

        public String toString() {
            return this.mIdString + (this.mActivityDisplay == null ? "N" : "A");
        }
    }

    class ActivityDisplay extends ConfigurationContainer {
        Display mDisplay;
        private IntArray mDisplayAccessUIDs = new IntArray();
        int mDisplayId;
        final ArrayList<ActivityStack> mStacks = new ArrayList();
        ActivityRecord mVisibleBehindActivity;

        ActivityDisplay() {
        }

        ActivityDisplay(int displayId) {
            Display display = ActivityStackSupervisor.this.mDisplayManager.getDisplay(displayId);
            if (display != null) {
                init(display);
            }
        }

        void init(Display display) {
            this.mDisplay = display;
            this.mDisplayId = display.getDisplayId();
        }

        void attachStack(ActivityStack stack, int position) {
            if (ActivityManagerDebugConfig.DEBUG_STACK) {
                Slog.v(ActivityStackSupervisor.TAG_STACK, "attachStack: attaching " + stack + " to displayId=" + this.mDisplayId + " position=" + position);
            }
            this.mStacks.add(position, stack);
        }

        void detachStack(ActivityStack stack) {
            if (ActivityManagerDebugConfig.DEBUG_STACK) {
                Slog.v(ActivityStackSupervisor.TAG_STACK, "detachStack: detaching " + stack + " from displayId=" + this.mDisplayId);
            }
            this.mStacks.remove(stack);
        }

        void setVisibleBehindActivity(ActivityRecord r) {
            this.mVisibleBehindActivity = r;
        }

        boolean hasVisibleBehindActivity() {
            return this.mVisibleBehindActivity != null;
        }

        public String toString() {
            return "ActivityDisplay={" + this.mDisplayId + " numStacks=" + this.mStacks.size() + "}";
        }

        protected int getChildCount() {
            return this.mStacks.size();
        }

        protected ConfigurationContainer getChildAt(int index) {
            return (ConfigurationContainer) this.mStacks.get(index);
        }

        protected ConfigurationContainer getParent() {
            return ActivityStackSupervisor.this;
        }

        boolean isPrivate() {
            return (this.mDisplay.getFlags() & 4) != 0;
        }

        boolean isUidPresent(int uid) {
            for (ActivityStack stack : this.mStacks) {
                if (stack.isUidPresent(uid)) {
                    return true;
                }
            }
            return false;
        }

        private IntArray getPresentUIDs() {
            this.mDisplayAccessUIDs.clear();
            for (ActivityStack stack : this.mStacks) {
                stack.getPresentUIDs(this.mDisplayAccessUIDs);
            }
            return this.mDisplayAccessUIDs;
        }

        boolean shouldDestroyContentOnRemove() {
            return this.mDisplay.getRemoveMode() == 1;
        }
    }

    protected final class ActivityStackSupervisorHandler extends Handler {
        public ActivityStackSupervisorHandler(Looper looper) {
            super(looper);
        }

        void activityIdleInternal(ActivityRecord r, boolean processPausingActivities) {
            IBinder iBinder = null;
            synchronized (ActivityStackSupervisor.this.mService) {
                try {
                    ActivityManagerService.boostPriorityForLockedSection();
                    ActivityStackSupervisor activityStackSupervisor = ActivityStackSupervisor.this;
                    if (r != null) {
                        iBinder = r.appToken;
                    }
                    activityStackSupervisor.activityIdleInternalLocked(iBinder, true, processPausingActivities, null);
                } finally {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        public void handleMessage(Message msg) {
            ActivityContainer container;
            IActivityContainerCallback callback;
            ActivityRecord r;
            int i;
            switch (msg.what) {
                case 100:
                    if (ActivityManagerDebugConfig.DEBUG_IDLE) {
                        Slog.d(ActivityStackSupervisor.TAG_IDLE, "handleMessage: IDLE_TIMEOUT_MSG: r=" + msg.obj);
                    }
                    if (!ActivityStackSupervisor.this.mService.mDidDexOpt) {
                        activityIdleInternal((ActivityRecord) msg.obj, true);
                        break;
                    }
                    ActivityStackSupervisor.this.mService.mDidDexOpt = false;
                    Message nmsg = ActivityStackSupervisor.this.mHandler.obtainMessage(100);
                    nmsg.obj = msg.obj;
                    ActivityStackSupervisor.this.mHandler.sendMessageDelayed(nmsg, JobStatus.DEFAULT_TRIGGER_UPDATE_DELAY);
                    return;
                case 101:
                    if (ActivityManagerDebugConfig.DEBUG_IDLE) {
                        Slog.d(ActivityStackSupervisor.TAG_IDLE, "handleMessage: IDLE_NOW_MSG: r=" + msg.obj);
                    }
                    activityIdleInternal((ActivityRecord) msg.obj, false);
                    break;
                case 102:
                    synchronized (ActivityStackSupervisor.this.mService) {
                        try {
                            ActivityManagerService.boostPriorityForLockedSection();
                            ActivityStackSupervisor.this.resumeFocusedStackTopActivityLocked();
                        } finally {
                            ActivityManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                    break;
                case 103:
                    synchronized (ActivityStackSupervisor.this.mService) {
                        try {
                            ActivityManagerService.boostPriorityForLockedSection();
                            if (ActivityStackSupervisor.this.mService.isSleepingOrShuttingDownLocked()) {
                                Slog.w(ActivityStackSupervisor.TAG, "Sleep timeout!  Sleeping now.");
                                ActivityStackSupervisor.this.mSleepTimeout = true;
                                ActivityStackSupervisor.this.checkReadyForSleepLocked();
                            }
                        } finally {
                            ActivityManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                    break;
                case 104:
                    if (!ActivityStackSupervisor.this.mService.mDidDexOpt) {
                        synchronized (ActivityStackSupervisor.this.mService) {
                            try {
                                ActivityManagerService.boostPriorityForLockedSection();
                                if (ActivityStackSupervisor.this.mLaunchingActivity.isHeld()) {
                                    Slog.w(ActivityStackSupervisor.TAG, "Launch timeout has expired, giving up wake lock!");
                                    ActivityStackSupervisor.this.mLaunchingActivity.release();
                                }
                            } finally {
                                ActivityManagerService.resetPriorityAfterLockedSection();
                            }
                        }
                        break;
                    }
                    ActivityStackSupervisor.this.mService.mDidDexOpt = false;
                    ActivityStackSupervisor.this.mHandler.sendEmptyMessageDelayed(104, JobStatus.DEFAULT_TRIGGER_UPDATE_DELAY);
                    return;
                case 105:
                    ActivityStackSupervisor.this.handleDisplayAdded(msg.arg1);
                    break;
                case 106:
                    ActivityStackSupervisor.this.handleDisplayChanged(msg.arg1);
                    break;
                case 107:
                    ActivityStackSupervisor.this.handleDisplayRemoved(msg.arg1);
                    break;
                case 108:
                    container = msg.obj;
                    callback = container.mCallback;
                    if (callback != null) {
                        try {
                            callback.setVisible(container.asBinder(), msg.arg1 == 1);
                            break;
                        } catch (RemoteException e) {
                            break;
                        }
                    }
                    break;
                case 109:
                    try {
                        if (ActivityStackSupervisor.this.mLockTaskNotify == null) {
                            ActivityStackSupervisor.this.mLockTaskNotify = new LockTaskNotify(ActivityStackSupervisor.this.mService.mContext);
                        }
                        ActivityStackSupervisor.this.mLockTaskNotify.show(true);
                        ActivityStackSupervisor.this.mLockTaskModeState = msg.arg2;
                        if (ActivityStackSupervisor.this.getStatusBarService() != null) {
                            int flags = 0;
                            if (ActivityStackSupervisor.this.mLockTaskModeState == 1) {
                                flags = 62849024;
                            } else if (ActivityStackSupervisor.this.mLockTaskModeState == 2) {
                                flags = 43974656;
                            }
                            ActivityStackSupervisor.this.getStatusBarService().disable(flags, ActivityStackSupervisor.this.mToken, ActivityStackSupervisor.this.mService.mContext.getPackageName());
                        }
                        ActivityStackSupervisor.this.mWindowManager.disableKeyguard(ActivityStackSupervisor.this.mToken, ActivityStackSupervisor.LOCK_TASK_TAG);
                        if (ActivityStackSupervisor.this.getDevicePolicyManager() != null) {
                            ActivityStackSupervisor.this.getDevicePolicyManager().notifyLockTaskModeChanged(true, (String) msg.obj, msg.arg1);
                            break;
                        }
                    } catch (RemoteException ex) {
                        throw new RuntimeException(ex);
                    }
                    break;
                case 110:
                    try {
                        if (ActivityStackSupervisor.this.getStatusBarService() != null) {
                            ActivityStackSupervisor.this.getStatusBarService().disable(0, ActivityStackSupervisor.this.mToken, ActivityStackSupervisor.this.mService.mContext.getPackageName());
                        }
                        ActivityStackSupervisor.this.mWindowManager.reenableKeyguard(ActivityStackSupervisor.this.mToken);
                        if (ActivityStackSupervisor.this.getDevicePolicyManager() != null) {
                            ActivityStackSupervisor.this.getDevicePolicyManager().notifyLockTaskModeChanged(false, null, msg.arg1);
                        }
                        if (ActivityStackSupervisor.this.mLockTaskNotify == null) {
                            ActivityStackSupervisor.this.mLockTaskNotify = new LockTaskNotify(ActivityStackSupervisor.this.mService.mContext);
                        }
                        ActivityStackSupervisor.this.mLockTaskNotify.show(false);
                        try {
                            boolean shouldLockKeyguard = Secure.getInt(ActivityStackSupervisor.this.mService.mContext.getContentResolver(), "lock_to_app_exit_locked") != 0;
                            if (ActivityStackSupervisor.this.mLockTaskModeState == 2 && shouldLockKeyguard) {
                                ActivityStackSupervisor.this.mWindowManager.lockNow(null);
                                ActivityStackSupervisor.this.mWindowManager.dismissKeyguard(null);
                                new LockPatternUtils(ActivityStackSupervisor.this.mService.mContext).requireCredentialEntry(-1);
                            }
                        } catch (SettingNotFoundException e2) {
                        }
                        ActivityStackSupervisor.this.mLockTaskModeState = 0;
                        break;
                    } catch (RemoteException ex2) {
                        throw new RuntimeException(ex2);
                    } catch (Throwable th) {
                        ActivityStackSupervisor.this.mLockTaskModeState = 0;
                    }
                case 111:
                    container = (ActivityContainer) msg.obj;
                    callback = container.mCallback;
                    if (callback != null) {
                        try {
                            callback.onAllActivitiesComplete(container.asBinder());
                            break;
                        } catch (RemoteException e3) {
                            break;
                        }
                    }
                    break;
                case 112:
                    synchronized (ActivityStackSupervisor.this.mService) {
                        try {
                            ActivityManagerService.boostPriorityForLockedSection();
                            r = ActivityRecord.forTokenLocked((IBinder) msg.obj);
                            if (r != null) {
                                ActivityStackSupervisor.this.handleLaunchTaskBehindCompleteLocked(r);
                            }
                        } finally {
                            ActivityManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                    break;
                case 113:
                    if (ActivityStackSupervisor.this.mLockTaskNotify == null) {
                        ActivityStackSupervisor.this.mLockTaskNotify = new LockTaskNotify(ActivityStackSupervisor.this.mService.mContext);
                    }
                    ActivityStackSupervisor.this.mLockTaskNotify.showToast(2);
                    break;
                case 114:
                    synchronized (ActivityStackSupervisor.this.mService) {
                        try {
                            ActivityManagerService.boostPriorityForLockedSection();
                            for (i = ActivityStackSupervisor.this.mMultiWindowModeChangedActivities.size() - 1; i >= 0; i--) {
                                r = (ActivityRecord) ActivityStackSupervisor.this.mMultiWindowModeChangedActivities.remove(i);
                                Flog.i(101, "schedule multiwindow mode change callback for " + r);
                                r.updateMultiWindowMode();
                            }
                        } finally {
                            ActivityManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                    break;
                case 115:
                    synchronized (ActivityStackSupervisor.this.mService) {
                        try {
                            ActivityManagerService.boostPriorityForLockedSection();
                            for (i = ActivityStackSupervisor.this.mPipModeChangedActivities.size() - 1; i >= 0; i--) {
                                ((ActivityRecord) ActivityStackSupervisor.this.mPipModeChangedActivities.remove(i)).updatePictureInPictureMode(ActivityStackSupervisor.this.mPipModeChangedTargetStackBounds);
                            }
                        } finally {
                            ActivityManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                    break;
                case 10001:
                    ActivityStackSupervisor.this.handlePCWindowStateChanged();
                    break;
            }
        }
    }

    @IntDef({0, 1, 2})
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

        void sendErrorResult(String message) {
            try {
                if (this.callerApp.thread != null) {
                    this.callerApp.thread.scheduleCrash(message);
                }
            } catch (RemoteException e) {
                Slog.e(ActivityStackSupervisor.TAG, "Exception scheduling crash of failed activity launcher sourceRecord=" + this.sourceRecord, e);
            }
        }
    }

    private class VirtualActivityContainer extends ActivityContainer {
        boolean mDrawn = false;
        Surface mSurface;

        VirtualActivityContainer(ActivityRecord parent, IActivityContainerCallback callback) {
            super(ActivityStackSupervisor.this.getNextStackId(), parent.getStack().mActivityContainer.mActivityDisplay, true);
            this.mParentActivity = parent;
            this.mCallback = callback;
            this.mContainerState = 1;
            this.mIdString = "VirtualActivityContainer{" + this.mStackId + ", parent=" + this.mParentActivity + "}";
        }

        public void setSurface(Surface surface, int width, int height, int density) {
            super.setSurface(surface, width, height, density);
            synchronized (ActivityStackSupervisor.this.mService) {
                long origId;
                try {
                    ActivityManagerService.boostPriorityForLockedSection();
                    origId = Binder.clearCallingIdentity();
                    setSurfaceLocked(surface, width, height, density);
                    Binder.restoreCallingIdentity(origId);
                } catch (Throwable th) {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                }
            }
            ActivityManagerService.resetPriorityAfterLockedSection();
        }

        private void setSurfaceLocked(Surface surface, int width, int height, int density) {
            if (this.mContainerState != 2) {
                VirtualActivityDisplay virtualActivityDisplay = this.mActivityDisplay;
                if (virtualActivityDisplay == null) {
                    virtualActivityDisplay = new VirtualActivityDisplay(width, height, density);
                    this.mActivityDisplay = virtualActivityDisplay;
                    ActivityStackSupervisor.this.mActivityDisplays.put(virtualActivityDisplay.mDisplayId, virtualActivityDisplay);
                    addToDisplayLocked(virtualActivityDisplay);
                }
                if (this.mSurface != null) {
                    this.mSurface.release();
                }
                this.mSurface = surface;
                if (surface != null) {
                    ActivityStackSupervisor.this.resumeFocusedStackTopActivityLocked();
                } else {
                    this.mContainerState = 1;
                    ((VirtualActivityDisplay) this.mActivityDisplay).setSurface(null);
                    if (this.mStack.mPausingActivity == null && this.mStack.mResumedActivity != null) {
                        this.mStack.startPausingLocked(false, true, null, false);
                    }
                }
                setSurfaceIfReadyLocked();
                if (ActivityManagerDebugConfig.DEBUG_STACK) {
                    Slog.d(ActivityStackSupervisor.TAG_STACK, "setSurface: " + this + " to display=" + virtualActivityDisplay);
                }
            }
        }

        boolean isAttachedLocked() {
            return this.mSurface != null ? super.isAttachedLocked() : false;
        }

        void setDrawn() {
            synchronized (ActivityStackSupervisor.this.mService) {
                try {
                    ActivityManagerService.boostPriorityForLockedSection();
                    this.mDrawn = true;
                    setSurfaceIfReadyLocked();
                } finally {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        boolean isEligibleForNewTasks() {
            return false;
        }

        private void setSurfaceIfReadyLocked() {
            if (ActivityManagerDebugConfig.DEBUG_STACK) {
                Slog.v(ActivityStackSupervisor.TAG_STACK, "setSurfaceIfReadyLocked: mDrawn=" + this.mDrawn + " mContainerState=" + this.mContainerState + " mSurface=" + this.mSurface);
            }
            if (this.mDrawn && this.mSurface != null && this.mContainerState == 1) {
                ((VirtualActivityDisplay) this.mActivityDisplay).setSurface(this.mSurface);
                this.mContainerState = 0;
            }
        }
    }

    class VirtualActivityDisplay extends ActivityDisplay {
        VirtualDisplay mVirtualDisplay;

        VirtualActivityDisplay(int width, int height, int density) {
            super();
            this.mVirtualDisplay = DisplayManagerGlobal.getInstance().createVirtualDisplay(ActivityStackSupervisor.this.mService.mContext, null, ActivityStackSupervisor.VIRTUAL_DISPLAY_BASE_NAME, width, height, density, null, 9, null, null, null);
            init(this.mVirtualDisplay.getDisplay());
            ActivityStackSupervisor.this.mWindowManager.onDisplayAdded(this.mDisplayId);
        }

        void setSurface(Surface surface) {
            if (this.mVirtualDisplay != null) {
                this.mVirtualDisplay.setSurface(surface);
            }
        }

        void detachStack(ActivityStack stack) {
            super.detachStack(stack);
            if (this.mVirtualDisplay != null) {
                this.mVirtualDisplay.release();
                this.mVirtualDisplay = null;
            }
        }

        public String toString() {
            return "VirtualActivityDisplay={" + this.mDisplayId + "}";
        }
    }

    static class WaitInfo {
        private final WaitResult mResult;
        private final ComponentName mTargetComponent;

        public WaitInfo(ComponentName targetComponent, WaitResult result) {
            this.mTargetComponent = targetComponent;
            this.mResult = result;
        }

        public boolean matches(ComponentName targetComponent) {
            return this.mTargetComponent != null ? this.mTargetComponent.equals(targetComponent) : true;
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
            pw.println(prefix + "  mResult=");
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

    protected int getChildCount() {
        return this.mActivityDisplays.size();
    }

    protected ActivityDisplay getChildAt(int index) {
        return (ActivityDisplay) this.mActivityDisplays.valueAt(index);
    }

    protected ConfigurationContainer getParent() {
        return null;
    }

    Configuration getDisplayOverrideConfiguration(int displayId) {
        ActivityDisplay activityDisplay = getActivityDisplayOrCreateLocked(displayId);
        if (activityDisplay != null) {
            return activityDisplay.getOverrideConfiguration();
        }
        throw new IllegalArgumentException("No display found with id: " + displayId);
    }

    void setDisplayOverrideConfiguration(Configuration overrideConfiguration, int displayId) {
        ActivityDisplay activityDisplay = getActivityDisplayOrCreateLocked(displayId);
        if (activityDisplay == null) {
            throw new IllegalArgumentException("No display found with id: " + displayId);
        }
        activityDisplay.onOverrideConfigurationChanged(overrideConfiguration);
    }

    boolean canPlaceEntityOnDisplay(int displayId, boolean resizeable) {
        if (displayId == 0) {
            return true;
        }
        if (!this.mService.mSupportsMultiDisplay) {
            return false;
        }
        if (resizeable) {
            return true;
        }
        return displayConfigMatchesGlobal(displayId);
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
        this.mHandler = new ActivityStackSupervisorHandler(looper);
        this.mActivityMetricsLogger = new ActivityMetricsLogger(this, this.mService.mContext);
        this.mKeyguardController = new KeyguardController(service, this);
        this.mIsPerfBoostEnabled = this.mService.mContext.getResources().getBoolean(17956945);
        this.mIsperfDisablepackingEnable = this.mService.mContext.getResources().getBoolean(17956926);
        if (this.mIsPerfBoostEnabled) {
            this.lBoostTimeOut = this.mService.mContext.getResources().getInteger(17694926);
            this.lBoostCpuParamVal = this.mService.mContext.getResources().getIntArray(17236054);
        }
        if (this.mIsperfDisablepackingEnable) {
            this.lDisPackTimeOut = this.mService.mContext.getResources().getInteger(17694918);
            this.lBoostPackParamVal = this.mService.mContext.getResources().getIntArray(17236053);
        }
    }

    void setRecentTasks(RecentTasks recentTasks) {
        this.mRecentTasks = recentTasks;
    }

    void initPowerManagement() {
        PowerManager pm = (PowerManager) this.mService.mContext.getSystemService("power");
        this.mGoingToSleep = pm.newWakeLock(1, "ActivityManager-Sleep");
        this.mLaunchingActivity = pm.newWakeLock(1, "*launch*");
        this.mLaunchingActivity.setReferenceCounted(false);
    }

    private IStatusBarService getStatusBarService() {
        IStatusBarService iStatusBarService;
        synchronized (this.mService) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                if (this.mStatusBarService == null) {
                    this.mStatusBarService = IStatusBarService.Stub.asInterface(ServiceManager.checkService("statusbar"));
                    if (this.mStatusBarService == null) {
                        Slog.w("StatusBarManager", "warning: no STATUS_BAR_SERVICE");
                    }
                }
                iStatusBarService = this.mStatusBarService;
            } finally {
                ActivityManagerService.resetPriorityAfterLockedSection();
            }
        }
        return iStatusBarService;
    }

    private IDevicePolicyManager getDevicePolicyManager() {
        IDevicePolicyManager iDevicePolicyManager;
        synchronized (this.mService) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                if (this.mDevicePolicyManager == null) {
                    this.mDevicePolicyManager = IDevicePolicyManager.Stub.asInterface(ServiceManager.checkService("device_policy"));
                    if (this.mDevicePolicyManager == null) {
                        Slog.w(TAG, "warning: no DEVICE_POLICY_SERVICE");
                    }
                }
                iDevicePolicyManager = this.mDevicePolicyManager;
            } finally {
                ActivityManagerService.resetPriorityAfterLockedSection();
            }
        }
        return iDevicePolicyManager;
    }

    void setWindowManager(WindowManagerService wm) {
        synchronized (this.mService) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                this.mWindowManager = wm;
                this.mKeyguardController.setWindowManager(wm);
                this.mDisplayManager = (DisplayManager) this.mService.mContext.getSystemService("display");
                this.mDisplayManager.registerDisplayListener(this, null);
                this.mDisplayManagerInternal = (DisplayManagerInternal) LocalServices.getService(DisplayManagerInternal.class);
                Display[] displays = this.mDisplayManager.getDisplays();
                for (int displayNdx = displays.length - 1; displayNdx >= 0; displayNdx--) {
                    int displayId = displays[displayNdx].getDisplayId();
                    ActivityDisplay activityDisplay = new ActivityDisplay(displayId);
                    if (activityDisplay.mDisplay == null) {
                        throw new IllegalStateException("Default Display does not exist");
                    }
                    this.mActivityDisplays.put(displayId, activityDisplay);
                    calculateDefaultMinimalSizeOfResizeableTasks(activityDisplay);
                }
                ActivityStack stack = getStack(0, true, true);
                this.mLastFocusedStack = stack;
                this.mFocusedStack = stack;
                this.mHomeStack = stack;
                this.mInputManagerInternal = (InputManagerInternal) LocalServices.getService(InputManagerInternal.class);
            } finally {
                ActivityManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    ActivityStack getFocusedStack() {
        return this.mFocusedStack;
    }

    ActivityStack getLastStack() {
        return this.mLastFocusedStack;
    }

    boolean isFocusedStack(ActivityStack stack) {
        boolean z = false;
        if (stack == null) {
            return false;
        }
        ActivityRecord parent = stack.mActivityContainer.mParentActivity;
        if (parent != null) {
            stack = parent.getStack();
        }
        if (stack == this.mFocusedStack) {
            z = true;
        }
        return z;
    }

    boolean isFrontStackOnDisplay(ActivityStack stack) {
        if (stack.mActivityContainer.mActivityDisplay == null) {
            return false;
        }
        return isFrontOfStackList(stack, stack.mActivityContainer.mActivityDisplay.mStacks);
    }

    protected boolean isFrontOfStackList(ActivityStack stack, List<ActivityStack> stackList) {
        boolean z = false;
        if (stack == null) {
            return false;
        }
        ActivityRecord parent = stack.mActivityContainer.mParentActivity;
        if (parent != null) {
            stack = parent.getStack();
        }
        if (stack == stackList.get(stackList.size() - 1)) {
            z = true;
        }
        return z;
    }

    void setFocusStackUnchecked(String reason, ActivityStack focusCandidate) {
        int i = -1;
        if (!focusCandidate.isFocusable()) {
            focusCandidate = getNextFocusableStackLocked(focusCandidate);
        }
        if (focusCandidate != this.mFocusedStack) {
            this.mLastFocusedStack = this.mFocusedStack;
            this.mFocusedStack = focusCandidate;
            int i2 = this.mCurrentUser;
            int stackId = this.mFocusedStack == null ? -1 : this.mFocusedStack.getStackId();
            if (this.mLastFocusedStack != null) {
                i = this.mLastFocusedStack.getStackId();
            }
            EventLogTags.writeAmFocusedStack(i2, stackId, i, reason);
        }
        ActivityRecord r = topRunningActivityLocked();
        if ((this.mService.mBooting || (this.mService.mBooted ^ 1) != 0) && r != null && r.idle) {
            checkFinishBootingLocked();
        }
    }

    void moveHomeStackToFront(String reason) {
        this.mHomeStack.moveToFront(reason);
    }

    void moveRecentsStackToFront(String reason) {
        ActivityStack recentsStack = getStack(5);
        if (recentsStack != null) {
            recentsStack.moveToFront(reason);
        }
    }

    boolean moveHomeStackTaskToTop(String reason) {
        this.mHomeStack.moveHomeStackTaskToTop();
        ActivityRecord top = getHomeActivity();
        if (top == null) {
            return false;
        }
        moveFocusableActivityStackToFrontLocked(top, reason);
        return true;
    }

    boolean resumeHomeStackTask(ActivityRecord prev, String reason) {
        if (!this.mService.mBooting && (this.mService.mBooted ^ 1) != 0) {
            return false;
        }
        if (prev != null) {
            prev.getTask().setTaskToReturnTo(0);
        }
        this.mHomeStack.moveHomeStackTaskToTop();
        ActivityRecord r = getHomeActivity();
        String myReason = reason + " resumeHomeStackTask";
        if (r == null || (r.finishing ^ 1) == 0) {
            return this.mService.startHomeActivityLocked(this.mCurrentUser, myReason);
        }
        moveFocusableActivityStackToFrontLocked(r, myReason);
        return resumeFocusedStackTopActivityLocked(this.mHomeStack, prev, null);
    }

    TaskRecord anyTaskForIdLocked(int id) {
        return anyTaskForIdLocked(id, 2, -1);
    }

    TaskRecord anyTaskForIdLocked(int id, int matchMode, int stackId) {
        if (matchMode == 2 || stackId == -1) {
            TaskRecord task;
            int numDisplays = this.mActivityDisplays.size();
            for (int displayNdx = 0; displayNdx < numDisplays; displayNdx++) {
                ArrayList<ActivityStack> stacks = ((ActivityDisplay) this.mActivityDisplays.valueAt(displayNdx)).mStacks;
                for (int stackNdx = stacks.size() - 1; stackNdx >= 0; stackNdx--) {
                    task = ((ActivityStack) stacks.get(stackNdx)).taskForIdLocked(id);
                    if (task != null) {
                        return task;
                    }
                }
            }
            if (matchMode == 0) {
                return null;
            }
            if (ActivityManagerDebugConfig.DEBUG_RECENTS) {
                Slog.v(TAG_RECENTS, "Looking for task id=" + id + " in recents");
            }
            task = this.mRecentTasks.taskForIdLocked(id);
            if (task == null) {
                if (ActivityManagerDebugConfig.DEBUG_RECENTS) {
                    Slog.d(TAG_RECENTS, "\tDidn't find task id=" + id + " in recents");
                }
                return null;
            } else if (matchMode == 1) {
                return task;
            } else {
                if (restoreRecentTaskLocked(task, stackId)) {
                    if (ActivityManagerDebugConfig.DEBUG_RECENTS) {
                        Slog.w(TAG_RECENTS, "Restored task id=" + id + " from in recents");
                    }
                    return task;
                }
                if (ActivityManagerDebugConfig.DEBUG_RECENTS) {
                    Slog.w(TAG_RECENTS, "Couldn't restore task id=" + id + " found in recents");
                }
                return null;
            }
        }
        throw new IllegalArgumentException("Should not specify stackId for non-restore lookup");
    }

    ActivityRecord isInAnyStackLocked(IBinder token) {
        int numDisplays = this.mActivityDisplays.size();
        for (int displayNdx = 0; displayNdx < numDisplays; displayNdx++) {
            ArrayList<ActivityStack> stacks = ((ActivityDisplay) this.mActivityDisplays.valueAt(displayNdx)).mStacks;
            for (int stackNdx = stacks.size() - 1; stackNdx >= 0; stackNdx--) {
                ActivityRecord r = ((ActivityStack) stacks.get(stackNdx)).isInStackLocked(token);
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
        if (activityRecord != null && activityRecord.userId == userId) {
            return true;
        }
        if (resultTo == null || resultTo.userId != userId) {
            return false;
        }
        return true;
    }

    void lockAllProfileTasks(int userId) {
        this.mWindowManager.deferSurfaceLayout();
        try {
            List<ActivityStack> stacks = getStacks();
            for (int stackNdx = stacks.size() - 1; stackNdx >= 0; stackNdx--) {
                List<TaskRecord> tasks = ((ActivityStack) stacks.get(stackNdx)).getAllTasks();
                for (int taskNdx = tasks.size() - 1; taskNdx >= 0; taskNdx--) {
                    TaskRecord task = (TaskRecord) tasks.get(taskNdx);
                    if (taskTopActivityIsUser(task, userId)) {
                        this.mService.mTaskChangeNotificationController.notifyTaskProfileLocked(task.taskId, userId);
                    }
                }
            }
        } finally {
            this.mWindowManager.continueSurfaceLayout();
        }
    }

    void setNextTaskIdForUserLocked(int taskId, int userId) {
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

    int getNextTaskIdForUserLocked(int userId) {
        int currentTaskId = this.mCurTaskIdForUser.get(userId, MAX_TASK_IDS_PER_USER * userId);
        int candidateTaskId = nextTaskIdForUser(currentTaskId, userId);
        do {
            if (this.mRecentTasks.taskIdTakenForUserLocked(candidateTaskId, userId) || anyTaskForIdLocked(candidateTaskId, 1, -1) != null) {
                candidateTaskId = nextTaskIdForUser(candidateTaskId, userId);
            } else {
                this.mCurTaskIdForUser.put(userId, candidateTaskId);
                return candidateTaskId;
            }
        } while (candidateTaskId != currentTaskId);
        throw new IllegalStateException("Cannot get an available task id. Reached limit of 100000 running tasks per user.");
    }

    ActivityRecord getResumedActivityLocked() {
        ActivityStack stack = this.mFocusedStack;
        if (stack == null) {
            return null;
        }
        ActivityRecord resumedActivity = stack.mResumedActivity;
        if (resumedActivity == null || resumedActivity.app == null) {
            resumedActivity = stack.mPausingActivity;
            if (resumedActivity == null || resumedActivity.app == null) {
                resumedActivity = stack.topRunningActivityLocked();
            }
        }
        return resumedActivity;
    }

    boolean attachApplicationLocked(ProcessRecord app) throws RemoteException {
        String processName = app.processName;
        boolean didSomething = false;
        for (int displayNdx = this.mActivityDisplays.size() - 1; displayNdx >= 0; displayNdx--) {
            ArrayList<ActivityStack> stacks = ((ActivityDisplay) this.mActivityDisplays.valueAt(displayNdx)).mStacks;
            for (int stackNdx = stacks.size() - 1; stackNdx >= 0; stackNdx--) {
                ActivityStack stack = (ActivityStack) stacks.get(stackNdx);
                if (isFocusedStack(stack)) {
                    ActivityRecord hr = stack.topRunningActivityLocked();
                    if (hr != null && hr.app == null && app.uid == hr.info.applicationInfo.uid && processName.equals(hr.processName)) {
                        try {
                            app.addPackage(hr.info.packageName, hr.info.applicationInfo.versionCode, this.mService.mProcessStats);
                            if (realStartActivityLocked(hr, app, true, true)) {
                                didSomething = true;
                            }
                        } catch (RemoteException e) {
                            Slog.w(TAG, "Exception in new application when starting activity " + hr.intent.getComponent().flattenToShortString(), e);
                            throw e;
                        }
                    }
                }
            }
        }
        if (!didSomething) {
            ensureActivitiesVisibleLocked(null, 0, false);
        }
        return didSomething;
    }

    boolean allResumedActivitiesIdle() {
        for (int displayNdx = this.mActivityDisplays.size() - 1; displayNdx >= 0; displayNdx--) {
            ArrayList<ActivityStack> stacks = ((ActivityDisplay) this.mActivityDisplays.valueAt(displayNdx)).mStacks;
            for (int stackNdx = stacks.size() - 1; stackNdx >= 0; stackNdx--) {
                ActivityStack stack = (ActivityStack) stacks.get(stackNdx);
                if (isFocusedStack(stack) && stack.numActivities() != 0) {
                    ActivityRecord resumedActivity = stack.mResumedActivity;
                    if (resumedActivity == null || (resumedActivity.idle ^ 1) != 0) {
                        if (ActivityManagerDebugConfig.DEBUG_STATES) {
                            Slog.d(TAG_STATES, "allResumedActivitiesIdle: stack=" + stack.mStackId + " " + resumedActivity + " not idle");
                        }
                        return false;
                    }
                }
            }
        }
        this.mService.mActivityStarter.sendPowerHintForLaunchEndIfNeeded();
        return true;
    }

    boolean allResumedActivitiesComplete() {
        for (int displayNdx = this.mActivityDisplays.size() - 1; displayNdx >= 0; displayNdx--) {
            ArrayList<ActivityStack> stacks = ((ActivityDisplay) this.mActivityDisplays.valueAt(displayNdx)).mStacks;
            for (int stackNdx = stacks.size() - 1; stackNdx >= 0; stackNdx--) {
                ActivityStack stack = (ActivityStack) stacks.get(stackNdx);
                if (isFocusedStack(stack)) {
                    ActivityRecord r = stack.mResumedActivity;
                    if (!(r == null || r.state == ActivityState.RESUMED)) {
                        return false;
                    }
                }
            }
        }
        if (ActivityManagerDebugConfig.DEBUG_STACK) {
            Slog.d(TAG_STACK, "allResumedActivitiesComplete: mLastFocusedStack changing from=" + this.mLastFocusedStack + " to=" + this.mFocusedStack);
        }
        this.mLastFocusedStack = this.mFocusedStack;
        return true;
    }

    boolean allResumedActivitiesVisible() {
        boolean foundResumed = false;
        for (int displayNdx = this.mActivityDisplays.size() - 1; displayNdx >= 0; displayNdx--) {
            ArrayList<ActivityStack> stacks = ((ActivityDisplay) this.mActivityDisplays.valueAt(displayNdx)).mStacks;
            for (int stackNdx = stacks.size() - 1; stackNdx >= 0; stackNdx--) {
                ActivityRecord r = ((ActivityStack) stacks.get(stackNdx)).mResumedActivity;
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

    boolean pauseBackStacks(boolean userLeaving, ActivityRecord resuming, boolean dontWait) {
        boolean someActivityPaused = false;
        for (int displayNdx = this.mActivityDisplays.size() - 1; displayNdx >= 0; displayNdx--) {
            ArrayList<ActivityStack> stacks = ((ActivityDisplay) this.mActivityDisplays.valueAt(displayNdx)).mStacks;
            for (int stackNdx = stacks.size() - 1; stackNdx >= 0; stackNdx--) {
                ActivityStack stack = (ActivityStack) stacks.get(stackNdx);
                if (!(isFocusedStack(stack) || stack.mResumedActivity == null)) {
                    if (ActivityManagerDebugConfig.DEBUG_STATES) {
                        Slog.d(TAG_STATES, "pauseBackStacks: stack=" + stack + " mResumedActivity=" + stack.mResumedActivity);
                    }
                    someActivityPaused |= stack.startPausingLocked(userLeaving, false, resuming, dontWait);
                }
            }
        }
        return someActivityPaused;
    }

    boolean allPausedActivitiesComplete() {
        boolean pausing = true;
        for (int displayNdx = this.mActivityDisplays.size() - 1; displayNdx >= 0; displayNdx--) {
            ArrayList<ActivityStack> stacks = ((ActivityDisplay) this.mActivityDisplays.valueAt(displayNdx)).mStacks;
            for (int stackNdx = stacks.size() - 1; stackNdx >= 0; stackNdx--) {
                ActivityRecord r = ((ActivityStack) stacks.get(stackNdx)).mPausingActivity;
                if (!(r == null || r.state == ActivityState.PAUSED || r.state == ActivityState.STOPPED || r.state == ActivityState.STOPPING)) {
                    Slog.d(TAG_STATES, "allPausedActivitiesComplete: r=" + r + " state=" + r.state);
                    if (!ActivityManagerDebugConfig.DEBUG_STATES) {
                        return false;
                    }
                    pausing = false;
                }
            }
        }
        return pausing;
    }

    void pauseChildStacks(ActivityRecord parent, boolean userLeaving, boolean uiSleeping, ActivityRecord resuming, boolean dontWait) {
        for (int displayNdx = this.mActivityDisplays.size() - 1; displayNdx >= 0; displayNdx--) {
            ArrayList<ActivityStack> stacks = ((ActivityDisplay) this.mActivityDisplays.valueAt(displayNdx)).mStacks;
            for (int stackNdx = stacks.size() - 1; stackNdx >= 0; stackNdx--) {
                ActivityStack stack = (ActivityStack) stacks.get(stackNdx);
                if (stack.mResumedActivity != null && stack.mActivityContainer.mParentActivity == parent) {
                    stack.startPausingLocked(userLeaving, uiSleeping, resuming, dontWait);
                }
            }
        }
    }

    void cancelInitializingActivities() {
        for (int displayNdx = this.mActivityDisplays.size() - 1; displayNdx >= 0; displayNdx--) {
            ArrayList<ActivityStack> stacks = ((ActivityDisplay) this.mActivityDisplays.valueAt(displayNdx)).mStacks;
            for (int stackNdx = stacks.size() - 1; stackNdx >= 0; stackNdx--) {
                ((ActivityStack) stacks.get(stackNdx)).cancelInitializingActivities();
            }
        }
    }

    void waitActivityVisible(ComponentName name, WaitResult result) {
        this.mWaitingForActivityVisible.add(new WaitInfo(name, result));
    }

    void cleanupActivity(ActivityRecord r) {
        int i;
        this.mFinishingActivities.remove(r);
        this.mActivitiesWaitingForVisibleActivity.remove(r);
        boolean changed = false;
        for (i = this.mWaitingForActivityVisible.size() - 1; i >= 0; i--) {
            if (((WaitInfo) this.mWaitingForActivityVisible.get(i)).matches(r.realActivity)) {
                WaitInfo w = (WaitInfo) this.mWaitingForActivityVisible.remove(i);
                changed = true;
                w.mResult.who = new ComponentName(r.info.packageName, r.info.name);
                w.mResult.totalTime = SystemClock.uptimeMillis() - w.mResult.thisTime;
                w.mResult.thisTime = w.mResult.totalTime;
            }
        }
        for (i = this.mWaitingActivityLaunched.size() - 1; i >= 0; i--) {
            ComponentName cn = ((WaitResult) this.mWaitingActivityLaunched.get(i)).origin;
            Intent oriIntent = r.intent;
            if (!(cn == null || oriIntent == null || !cn.equals(oriIntent.getComponent()))) {
                WaitResult w2 = (WaitResult) this.mWaitingActivityLaunched.remove(i);
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

    void reportActivityVisibleLocked(ActivityRecord r) {
        sendWaitingVisibleReportLocked(r);
    }

    void sendWaitingVisibleReportLocked(ActivityRecord r) {
        boolean changed = false;
        for (int i = this.mWaitingForActivityVisible.size() - 1; i >= 0; i--) {
            WaitInfo w = (WaitInfo) this.mWaitingForActivityVisible.get(i);
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

    void reportTaskToFrontNoLaunch(ActivityRecord r) {
        boolean changed = false;
        for (int i = this.mWaitingActivityLaunched.size() - 1; i >= 0; i--) {
            WaitResult w = (WaitResult) this.mWaitingActivityLaunched.remove(i);
            if (w.who == null) {
                changed = true;
                w.result = 2;
            }
        }
        if (changed) {
            Flog.i(101, " reportTaskToFrontNoLaunch notify r = " + r);
            this.mService.notifyAll();
        }
    }

    void reportActivityLaunchedLocked(boolean timeout, ActivityRecord r, long thisTime, long totalTime) {
        boolean changed = false;
        for (int i = this.mWaitingActivityLaunched.size() - 1; i >= 0; i--) {
            WaitResult w = (WaitResult) this.mWaitingActivityLaunched.remove(i);
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

    ActivityRecord topRunningActivityLocked() {
        ActivityStack focusedStack = this.mFocusedStack;
        ActivityRecord r = focusedStack.topRunningActivityLocked();
        if (r != null) {
            return r;
        }
        this.mWindowManager.getDisplaysInFocusOrder(this.mTmpOrderedDisplayIds);
        for (int i = this.mTmpOrderedDisplayIds.size() - 1; i >= 0; i--) {
            List<ActivityStack> stacks = ((ActivityDisplay) this.mActivityDisplays.get(this.mTmpOrderedDisplayIds.get(i))).mStacks;
            if (stacks != null) {
                for (int j = stacks.size() - 1; j >= 0; j--) {
                    ActivityStack stack = (ActivityStack) stacks.get(j);
                    if (stack != focusedStack && isFrontStackOnDisplay(stack) && stack.isFocusable() && !isStackInVisible(stack)) {
                        r = stack.topRunningActivityLocked();
                        if (r != null) {
                            return r;
                        }
                    }
                }
                continue;
            }
        }
        return null;
    }

    protected boolean keepStackResumed(ActivityStack stack) {
        return false;
    }

    protected boolean isStackInVisible(ActivityStack stack) {
        return false;
    }

    void getTasksLocked(int maxNum, List<RunningTaskInfo> list, int callingUid, boolean allowed) {
        int stackNdx;
        ArrayList<RunningTaskInfo> stackTaskList;
        ArrayList<ArrayList<RunningTaskInfo>> runningTaskLists = new ArrayList();
        int numDisplays = this.mActivityDisplays.size();
        for (int displayNdx = 0; displayNdx < numDisplays; displayNdx++) {
            ArrayList<ActivityStack> stacks = ((ActivityDisplay) this.mActivityDisplays.valueAt(displayNdx)).mStacks;
            for (stackNdx = stacks.size() - 1; stackNdx >= 0; stackNdx--) {
                ActivityStack stack = (ActivityStack) stacks.get(stackNdx);
                stackTaskList = new ArrayList();
                runningTaskLists.add(stackTaskList);
                stack.getTasksLocked(stackTaskList, callingUid, allowed);
            }
        }
        while (maxNum > 0) {
            long mostRecentActiveTime = Long.MIN_VALUE;
            ArrayList selectedStackList = null;
            int numTaskLists = runningTaskLists.size();
            for (stackNdx = 0; stackNdx < numTaskLists; stackNdx++) {
                stackTaskList = (ArrayList) runningTaskLists.get(stackNdx);
                if (!stackTaskList.isEmpty()) {
                    long lastActiveTime = ((RunningTaskInfo) stackTaskList.get(0)).lastActiveTime;
                    long currentTimeMillis = System.currentTimeMillis();
                    if (lastActiveTime > currentTimeMillis) {
                        ((RunningTaskInfo) stackTaskList.get(0)).lastActiveTime = currentTimeMillis;
                    } else if (lastActiveTime > mostRecentActiveTime) {
                        mostRecentActiveTime = lastActiveTime;
                        ArrayList<RunningTaskInfo> selectedStackList2 = stackTaskList;
                    }
                }
            }
            if (selectedStackList2 != null) {
                list.add((RunningTaskInfo) selectedStackList2.remove(0));
                maxNum--;
            } else {
                return;
            }
        }
    }

    ActivityInfo resolveActivity(Intent intent, ResolveInfo rInfo, int startFlags, ProfilerInfo profilerInfo) {
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

    ResolveInfo resolveIntent(Intent intent, String resolvedType, int userId) {
        return resolveIntent(intent, resolvedType, userId, 0);
    }

    ResolveInfo resolveIntent(Intent intent, String resolvedType, int userId, int flags) {
        return this.mService.getPackageManagerInternalLocked().resolveIntent(intent, resolvedType, (8454144 | flags) | 1024, userId);
    }

    ActivityInfo resolveActivity(Intent intent, String resolvedType, int startFlags, ProfilerInfo profilerInfo, int userId) {
        return resolveActivity(intent, resolveIntent(intent, resolvedType, userId), startFlags, profilerInfo);
    }

    final boolean realStartActivityLocked(ActivityRecord r, ProcessRecord app, boolean andResume, boolean checkConfig) throws RemoteException {
        if (allPausedActivitiesComplete()) {
            r.startFreezingScreenLocked(app, 0);
            if (r.getStack().checkKeyguardVisibility(r, true, true)) {
                r.setVisibility(true);
            }
            r.startLaunchTickingLocked();
            if (checkConfig) {
                int displayId = r.getDisplayId();
                this.mService.updateDisplayOverrideConfigurationLocked(this.mWindowManager.updateOrientationFromAppTokens(getDisplayOverrideConfiguration(displayId), r.mayFreezeScreenLocked(app) ? r.appToken : null, displayId), r, true, displayId);
            }
            if (this.mKeyguardController.isKeyguardLocked()) {
                r.notifyUnknownVisibilityLaunched();
            }
            int applicationInfoUid = r.info.applicationInfo != null ? r.info.applicationInfo.uid : -1;
            if (!(r.userId == app.userId && r.appInfo.uid == applicationInfoUid)) {
                Slog.wtf(TAG, "User ID for activity changing for " + r + " appInfo.uid=" + r.appInfo.uid + " info.ai.uid=" + applicationInfoUid + " old=" + r.app + " new=" + app);
            }
            r.app = app;
            app.waitingToKill = null;
            r.launchCount++;
            r.lastLaunchTime = SystemClock.uptimeMillis();
            if (ActivityManagerDebugConfig.DEBUG_ALL) {
                Slog.v(TAG, "Launching: " + r);
            }
            if (app.activities.indexOf(r) < 0) {
                app.activities.add(r);
            }
            this.mService.updateLruProcessLocked(app, true, null);
            this.mService.updateOomAdjLocked();
            TaskRecord task = r.getTask();
            if (task == null) {
                Slog.e(TAG, " null task for ActivityRecord: " + r);
                return false;
            }
            if (task.mLockTaskAuth == 2 || task.mLockTaskAuth == 4) {
                setLockTaskModeLocked(task, 1, "mLockTaskAuth==LAUNCHABLE", false);
            }
            ActivityStack stack = task.getStack();
            try {
                if (app.thread == null) {
                    throw new RemoteException();
                }
                ComponentName componentName;
                ActivityInfo activityInfo;
                List results = null;
                List newIntents = null;
                if (andResume) {
                    results = r.results;
                    newIntents = r.newIntents;
                }
                if (ActivityManagerDebugConfig.DEBUG_SWITCH) {
                    Slog.v(TAG_SWITCH, "Launching: " + r + " icicle=" + r.icicle + " with results=" + results + " newIntents=" + newIntents + " andResume=" + andResume);
                }
                EventLog.writeEvent(EventLogTags.AM_RESTART_ACTIVITY, new Object[]{Integer.valueOf(r.userId), Integer.valueOf(System.identityHashCode(r)), Integer.valueOf(task.taskId), r.shortComponentName});
                if (r.isHomeActivity()) {
                    this.mService.mHomeProcess = ((ActivityRecord) task.mActivities.get(0)).app;
                    this.mService.reportHomeProcess(this.mService.mHomeProcess);
                }
                this.mService.notifyPackageUse(r.intent.getComponent().getPackageName(), 0);
                r.sleeping = false;
                r.forceNewConfig = false;
                this.mService.showUnsupportedZoomDialogIfNeededLocked(r);
                this.mService.showAskCompatModeDialogLocked(r);
                r.compat = this.mService.compatibilityInfoForPackageLocked(r.info.applicationInfo);
                ProfilerInfo profilerInfo = null;
                if (this.mService.mProfileApp != null && this.mService.mProfileApp.equals(app.processName) && (this.mService.mProfileProc == null || this.mService.mProfileProc == app)) {
                    this.mService.mProfileProc = app;
                    String profileFile = this.mService.mProfileFile;
                    if (profileFile != null) {
                        ParcelFileDescriptor profileFd = this.mService.mProfileFd;
                        if (profileFd != null) {
                            try {
                                profileFd = profileFd.dup();
                            } catch (IOException e) {
                                if (profileFd != null) {
                                    try {
                                        profileFd.close();
                                    } catch (IOException e2) {
                                    }
                                    profileFd = null;
                                }
                            }
                        }
                        profilerInfo = new ProfilerInfo(profileFile, profileFd, this.mService.mSamplingInterval, this.mService.mAutoStopProfiler, this.mService.mStreamingOutput);
                    }
                }
                app.hasShownUi = true;
                app.pendingUiClean = true;
                app.forceProcessStateUpTo(this.mService.mTopProcessState);
                HwCustNonHardwareAcceleratedPackagesManager hwCustNonHardwareAcceleratedPackagesManager = HwCustNonHardwareAcceleratedPackagesManager.getDefault();
                ActivityInfo activityInfo2 = r.info;
                if (app.instr == null) {
                    componentName = null;
                } else {
                    componentName = app.instr.mClass;
                }
                boolean forceHardAccel = hwCustNonHardwareAcceleratedPackagesManager.shouldForceEnabled(activityInfo2, componentName);
                if (forceHardAccel) {
                    activityInfo = r.info;
                    activityInfo.flags |= 512;
                }
                this.mActivityLaunchTrack = "launchActivity";
                Flog.i(101, "launch r: " + r + ", uid = " + r.info.applicationInfo.uid);
                if (Jlog.isPerfTest()) {
                    Jlog.i(2035, Intent.toPkgClsString(r.realActivity));
                }
                this.mWindowManager.prepareForForceRotation(r.appToken.asBinder(), r.info.packageName, app.pid, r.info.processName);
                MergedConfiguration mergedConfiguration = new MergedConfiguration(this.mService.getGlobalConfiguration(), r.getMergedOverrideConfiguration());
                r.setLastReportedConfiguration(mergedConfiguration);
                app.thread.scheduleLaunchActivity(new Intent(r.intent), r.appToken, System.identityHashCode(r), r.info, mergedConfiguration.getGlobalConfiguration(), mergedConfiguration.getOverrideConfiguration(), r.compat, r.launchedFromPackage, task.voiceInteractor, app.repProcState, r.icicle, r.persistentState, results, newIntents, andResume ^ 1, this.mService.isNextTransitionForward(), profilerInfo);
                if (forceHardAccel) {
                    activityInfo = r.info;
                    activityInfo.flags &= -513;
                }
                if ((app.info.privateFlags & 2) != 0 && app.processName.equals(app.info.packageName)) {
                    if (!(this.mService.mHeavyWeightProcess == null || this.mService.mHeavyWeightProcess == app)) {
                        Slog.w(TAG, "Starting new heavy weight process " + app + " when already running " + this.mService.mHeavyWeightProcess);
                    }
                    this.mService.mHeavyWeightProcess = app;
                    Message msg = this.mService.mHandler.obtainMessage(24);
                    msg.obj = r;
                    this.mService.mHandler.sendMessage(msg);
                }
                r.launchFailed = false;
                if (stack.updateLRUListLocked(r)) {
                    Slog.w(TAG, "Activity " + r + " being launched, but already in LRU list");
                }
                if (andResume) {
                    this.mActivityLaunchTrack += " minmalResume";
                    stack.minimalResumeActivityLocked(r);
                } else {
                    if (ActivityManagerDebugConfig.DEBUG_STATES) {
                        Slog.v(TAG_STATES, "Moving to PAUSED: " + r + " (starting in paused state)");
                    }
                    r.state = ActivityState.PAUSED;
                }
                if (isFocusedStack(stack)) {
                    this.mService.startSetupActivityLocked();
                }
                if (r.app != null) {
                    this.mService.mServices.updateServiceConnectionActivitiesLocked(r.app);
                }
                return true;
            } catch (Throwable e3) {
                if (r.launchFailed) {
                    Slog.e(TAG, "Second failure launching " + r.intent.getComponent().flattenToShortString() + ", giving up", e3);
                    this.mService.appDiedLocked(app);
                    stack.requestFinishActivityLocked(r.appToken, 0, null, "2nd-crash", false);
                    return false;
                }
                r.launchFailed = true;
                app.activities.remove(r);
                throw e3;
            }
        }
        if (ActivityManagerDebugConfig.DEBUG_SWITCH || ActivityManagerDebugConfig.DEBUG_PAUSE || ActivityManagerDebugConfig.DEBUG_STATES) {
            Slog.v(TAG_PAUSE, "realStartActivityLocked: Skipping start of r=" + r + " some activities pausing...");
        }
        return false;
    }

    protected void handlePCWindowStateChanged() {
    }

    void startSpecificActivityLocked(ActivityRecord r, boolean andResume, boolean checkConfig) {
        boolean isStartingLauncher;
        ProcessRecord app = this.mService.getProcessRecordLocked(r.processName, r.info.applicationInfo.uid, true);
        if (r.getStack() != null) {
            r.getStack().setLaunchTime(r);
        }
        if (!(app == null || app.thread == null)) {
            try {
                if ((r.info.flags & 1) == 0 || ("android".equals(r.info.packageName) ^ 1) != 0) {
                    app.addPackage(r.info.packageName, r.info.applicationInfo.versionCode, this.mService.mProcessStats);
                }
                realStartActivityLocked(r, app, andResume, checkConfig);
                return;
            } catch (Throwable e) {
                Slog.w(TAG, "Exception when starting activity " + r.intent.getComponent().flattenToShortString(), e);
            }
        }
        Flog.i(101, "mService.startProcessLocked for activity: " + r);
        if (this.mAppResource == null) {
            this.mAppResource = HwFrameworkFactory.getHwResource(18);
        }
        if (!(this.mAppResource == null || r.processName == null)) {
            Set<String> categories = r.intent.getCategories();
            int launched = (categories == null || !categories.contains("android.intent.category.LAUNCHER")) ? 0 : 1;
            this.mAppResource.acquire(r.info.applicationInfo.uid, r.processName, launched);
        }
        if (r.intent.getComponent() != null) {
            isStartingLauncher = "com.huawei.android.launcher".equals(r.intent.getComponent().getPackageName());
        } else {
            isStartingLauncher = false;
        }
        if (isStartingLauncher) {
            if (this.mService.mUserController.isUserRunningLocked(UserHandle.getUserId(r.info.applicationInfo.uid), 4)) {
                try {
                    this.mService.getPackageManagerInternalLocked().checkPackageStartable(r.intent.getComponent().getPackageName(), UserHandle.getUserId(r.info.applicationInfo.uid));
                } catch (SecurityException e2) {
                    Slog.i(TAG, "skip launch freezen hwLauncher for uid: " + r.info.applicationInfo.uid);
                    return;
                }
            }
            Slog.i(TAG, "skip launch hwLauncher for uid: " + r.info.applicationInfo.uid);
            return;
        }
        if (this.mService.mUserController.hasStartedUserState(r.userId)) {
            if (!startProcessOnExtDisplay(r)) {
                this.mService.startProcessLocked(r.processName, r.info.applicationInfo, true, 0, "activity", r.intent.getComponent(), false, false, true);
            }
            return;
        }
        Slog.w(TAG, "skip launch r : " + r + ": user " + r.userId + " is stopped");
    }

    protected boolean startProcessOnExtDisplay(ActivityRecord r) {
        return false;
    }

    boolean checkStartAnyActivityPermission(Intent intent, ActivityInfo aInfo, String resultWho, int requestCode, int callingPid, int callingUid, String callingPackage, boolean ignoreTargetSecurity, ProcessRecord callerApp, ActivityRecord resultRecord, ActivityStack resultStack, ActivityOptions options) {
        if (this.mService.checkPermission("android.permission.START_ANY_ACTIVITY", callingPid, callingUid) == 0) {
            return true;
        }
        int componentRestriction = getComponentRestrictionForCallingPackage(aInfo, callingPackage, callingPid, callingUid, ignoreTargetSecurity);
        int actionRestriction = getActionRestrictionForCallingPackage(intent.getAction(), callingPackage, callingPid, callingUid);
        String msg;
        if (componentRestriction == 1 || actionRestriction == 1) {
            if (resultRecord != null) {
                resultStack.sendActivityResultLocked(-1, resultRecord, resultWho, requestCode, 0, null);
            }
            if (actionRestriction == 1) {
                msg = "Permission Denial: starting " + intent.toString() + " from " + callerApp + " (pid=" + callingPid + ", uid=" + callingUid + ")" + " with revoked permission " + ((String) ACTION_TO_RUNTIME_PERMISSION.get(intent.getAction()));
            } else if (aInfo.exported) {
                msg = "Permission Denial: starting " + intent.toString() + " from " + callerApp + " (pid=" + callingPid + ", uid=" + callingUid + ")" + " requires " + aInfo.permission;
            } else {
                msg = "Permission Denial: starting " + intent.toString() + " from " + callerApp + " (pid=" + callingPid + ", uid=" + callingUid + ")" + " not exported from uid " + aInfo.applicationInfo.uid;
            }
            Slog.w(TAG, msg);
            throw new SecurityException(msg);
        } else if (actionRestriction == 2) {
            Slog.w(TAG, "Appop Denial: starting " + intent.toString() + " from " + callerApp + " (pid=" + callingPid + ", uid=" + callingUid + ")" + " requires " + AppOpsManager.permissionToOp((String) ACTION_TO_RUNTIME_PERMISSION.get(intent.getAction())));
            return false;
        } else if (componentRestriction == 2) {
            Slog.w(TAG, "Appop Denial: starting " + intent.toString() + " from " + callerApp + " (pid=" + callingPid + ", uid=" + callingUid + ")" + " requires appop " + AppOpsManager.permissionToOp(aInfo.permission));
            return false;
        } else {
            if (options != null) {
                if (options.getLaunchTaskId() == -1 || this.mService.checkPermission("android.permission.START_TASKS_FROM_RECENTS", callingPid, callingUid) == 0) {
                    int launchDisplayId = options.getLaunchDisplayId();
                    if (!(launchDisplayId == -1 || (isCallerAllowedToLaunchOnDisplay(callingPid, callingUid, launchDisplayId) ^ 1) == 0)) {
                        msg = "Permission Denial: starting " + intent.toString() + " from " + callerApp + " (pid=" + callingPid + ", uid=" + callingUid + ") with launchDisplayId=" + launchDisplayId;
                        Slog.w(TAG, msg);
                        throw new SecurityException(msg);
                    }
                }
                msg = "Permission Denial: starting " + intent.toString() + " from " + callerApp + " (pid=" + callingPid + ", uid=" + callingUid + ") with launchTaskId=" + options.getLaunchTaskId();
                Slog.w(TAG, msg);
                throw new SecurityException(msg);
            }
            return true;
        }
    }

    boolean isCallerAllowedToLaunchOnDisplay(int callingPid, int callingUid, int launchDisplayId) {
        if (ActivityManagerDebugConfig.DEBUG_TASKS) {
            Slog.d(TAG, "Launch on display check: displayId=" + launchDisplayId + " callingPid=" + callingPid + " callingUid=" + callingUid);
        }
        ActivityDisplay activityDisplay = getActivityDisplayOrCreateLocked(launchDisplayId);
        if (activityDisplay == null) {
            Slog.w(TAG, "Launch on display check: display not found");
            return false;
        } else if (this.mService.checkPermission("android.permission.INTERNAL_SYSTEM_WINDOW", callingPid, callingUid) == 0) {
            if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                Slog.d(TAG, "Launch on display check: allow launch any on display");
            }
            return true;
        } else if (activityDisplay.mDisplay.getType() == 5 && activityDisplay.mDisplay.getOwnerUid() != 1000) {
            if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                Slog.d(TAG, "Launch on display check: disallow launch on virtual display for not-embedded activity");
            }
            return false;
        } else if (!activityDisplay.isPrivate()) {
            if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                Slog.d(TAG, "Launch on display check: allow launch on public display");
            }
            return true;
        } else if (activityDisplay.mDisplay.getOwnerUid() == callingUid) {
            if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                Slog.d(TAG, "Launch on display check: allow launch for owner of the display");
            }
            return true;
        } else if (activityDisplay.isUidPresent(callingUid)) {
            if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                Slog.d(TAG, "Launch on display check: allow launch for caller present on the display");
            }
            return true;
        } else {
            Slog.w(TAG, "Launch on display check: denied");
            return false;
        }
    }

    void updateUIDsPresentOnDisplay() {
        this.mDisplayAccessUIDs.clear();
        for (int displayNdx = this.mActivityDisplays.size() - 1; displayNdx >= 0; displayNdx--) {
            ActivityDisplay activityDisplay = (ActivityDisplay) this.mActivityDisplays.valueAt(displayNdx);
            if (activityDisplay.isPrivate()) {
                this.mDisplayAccessUIDs.append(activityDisplay.mDisplayId, activityDisplay.getPresentUIDs());
            }
        }
        this.mDisplayManagerInternal.setDisplayAccessUIDs(this.mDisplayAccessUIDs);
    }

    UserInfo getUserInfo(int userId) {
        long identity = Binder.clearCallingIdentity();
        try {
            UserInfo userInfo = UserManager.get(this.mService.mContext).getUserInfo(userId);
            return userInfo;
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
        String permission = (String) ACTION_TO_RUNTIME_PERMISSION.get(action);
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
        } catch (NameNotFoundException e) {
            Slog.i(TAG, "Cannot find package info for " + callingPackage);
            return 0;
        }
    }

    void setLaunchSource(int uid) {
        this.mLaunchingActivity.setWorkSource(new WorkSource(uid));
    }

    void acquireLaunchWakelock() {
        this.mLaunchingActivity.acquire();
        if (!this.mHandler.hasMessages(104)) {
            this.mHandler.sendEmptyMessageDelayed(104, JobStatus.DEFAULT_TRIGGER_UPDATE_DELAY);
        }
    }

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

    final ActivityRecord activityIdleInternalLocked(IBinder token, boolean fromTimeout, boolean processPausingActivities, Configuration config) {
        int i;
        ActivityStack stack;
        if (ActivityManagerDebugConfig.DEBUG_ALL) {
            Slog.v(TAG, "Activity idle: " + token);
        }
        ArrayList finishes = null;
        ArrayList startingUsers = null;
        boolean booting = false;
        int activityRemoved = 0;
        ActivityRecord r = ActivityRecord.forTokenLocked(token);
        if (r != null) {
            if (ActivityManagerDebugConfig.DEBUG_IDLE) {
                Slog.d(TAG_IDLE, "activityIdleInternalLocked: Callers=" + Debug.getCallers(4));
            }
            this.mHandler.removeMessages(100, r);
            r.finishLaunchTickingLocked();
            if (fromTimeout) {
                reportActivityLaunchedLocked(fromTimeout, r, -1, -1);
            }
            if (config != null) {
                r.setLastReportedGlobalConfiguration(config);
            }
            r.idle = true;
            if (r.app != null && r.app.foregroundActivities) {
                this.mService.noteActivityStart(new AppInfo(r.app.info.packageName, r.app.processName, r.realActivity != null ? r.realActivity.getClassName() : "NULL", r.app.pid, r.app.uid), false);
            }
            if (isFocusedStack(r.getStack()) || fromTimeout) {
                booting = checkFinishBootingLocked();
            }
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
        ArrayList<ActivityRecord> stops = processStoppingActivitiesLocked(r, true, processPausingActivities);
        int NS = stops != null ? stops.size() : 0;
        int NF = this.mFinishingActivities.size();
        if (NF > 0) {
            finishes = new ArrayList(this.mFinishingActivities);
            this.mFinishingActivities.clear();
        }
        if (this.mStartingUsers.size() > 0) {
            ArrayList arrayList = new ArrayList(this.mStartingUsers);
            this.mStartingUsers.clear();
        }
        for (i = 0; i < NS; i++) {
            r = (ActivityRecord) stops.get(i);
            stack = r.getStack();
            if (stack != null) {
                if (r.finishing) {
                    stack.finishCurrentActivityLocked(r, 0, false);
                } else {
                    stack.stopActivityLocked(r);
                }
            }
        }
        for (i = 0; i < NF; i++) {
            r = (ActivityRecord) finishes.get(i);
            stack = r.getStack();
            if (stack != null) {
                activityRemoved |= stack.destroyActivityLocked(r, true, "finish-idle");
            }
        }
        if (!(booting || startingUsers == null)) {
            for (i = 0; i < startingUsers.size(); i++) {
                this.mService.mUserController.finishUserSwitch((UserState) startingUsers.get(i));
            }
        }
        this.mService.trimApplications();
        if (activityRemoved != 0) {
            resumeFocusedStackTopActivityLocked();
        }
        return r;
    }

    boolean handleAppDiedLocked(ProcessRecord app) {
        boolean hasVisibleActivities = false;
        for (int displayNdx = this.mActivityDisplays.size() - 1; displayNdx >= 0; displayNdx--) {
            ArrayList<ActivityStack> stacks = ((ActivityDisplay) this.mActivityDisplays.valueAt(displayNdx)).mStacks;
            for (int stackNdx = stacks.size() - 1; stackNdx >= 0; stackNdx--) {
                hasVisibleActivities |= ((ActivityStack) stacks.get(stackNdx)).handleAppDiedLocked(app);
            }
        }
        return hasVisibleActivities;
    }

    void closeSystemDialogsLocked() {
        for (int displayNdx = this.mActivityDisplays.size() - 1; displayNdx >= 0; displayNdx--) {
            ArrayList<ActivityStack> stacks = ((ActivityDisplay) this.mActivityDisplays.valueAt(displayNdx)).mStacks;
            for (int stackNdx = stacks.size() - 1; stackNdx >= 0; stackNdx--) {
                ((ActivityStack) stacks.get(stackNdx)).closeSystemDialogsLocked();
            }
        }
    }

    void removeUserLocked(int userId) {
        this.mUserStackInFront.delete(userId);
    }

    void updateUserStackLocked(int userId, ActivityStack stack) {
        if (userId != this.mCurrentUser) {
            this.mUserStackInFront.put(userId, stack != null ? stack.getStackId() : 0);
        }
    }

    boolean finishDisabledPackageActivitiesLocked(String packageName, Set<String> filterByClasses, boolean doit, boolean evenPersistent, int userId) {
        boolean didSomething = false;
        for (int displayNdx = this.mActivityDisplays.size() - 1; displayNdx >= 0; displayNdx--) {
            ArrayList<ActivityStack> stacks = ((ActivityDisplay) this.mActivityDisplays.valueAt(displayNdx)).mStacks;
            for (int stackNdx = stacks.size() - 1; stackNdx >= 0; stackNdx--) {
                if (((ActivityStack) stacks.get(stackNdx)).finishDisabledPackageActivitiesLocked(packageName, filterByClasses, doit, evenPersistent, userId)) {
                    didSomething = true;
                }
            }
        }
        return didSomething;
    }

    void updatePreviousProcessLocked(ActivityRecord r) {
        ProcessRecord fgApp = null;
        for (int displayNdx = this.mActivityDisplays.size() - 1; displayNdx >= 0; displayNdx--) {
            ArrayList<ActivityStack> stacks = ((ActivityDisplay) this.mActivityDisplays.valueAt(displayNdx)).mStacks;
            int stackNdx = stacks.size() - 1;
            while (stackNdx >= 0) {
                ActivityStack stack = (ActivityStack) stacks.get(stackNdx);
                if (isFocusedStack(stack)) {
                    if (stack.mResumedActivity != null) {
                        fgApp = stack.mResumedActivity.app;
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
            this.mService.reportPreviousInfo(12, r.app);
        }
    }

    boolean resumeFocusedStackTopActivityLocked() {
        return resumeFocusedStackTopActivityLocked(null, null, null);
    }

    boolean resumeFocusedStackTopActivityLocked(ActivityStack targetStack, ActivityRecord target, ActivityOptions targetOptions) {
        if (targetStack != null && isFocusedStack(targetStack) && !resumeAppLockActivityIfNeeded(targetStack, targetOptions)) {
            return targetStack.resumeTopActivityUncheckedLocked(target, targetOptions);
        }
        ActivityRecord r = this.mFocusedStack.topRunningActivityLocked();
        if (r == null || r.state != ActivityState.RESUMED) {
            if (!resumeAppLockActivityIfNeeded(this.mFocusedStack, targetOptions)) {
                this.mFocusedStack.resumeTopActivityUncheckedLocked(null, null);
            }
        } else if (r.state == ActivityState.RESUMED) {
            if (HwPCUtils.isPcCastModeInServer()) {
                this.mFocusedStack.resumeTopActivityUncheckedLocked(null, null);
            } else {
                this.mFocusedStack.executeAppTransition(targetOptions);
            }
        }
        return false;
    }

    void updateActivityApplicationInfoLocked(ApplicationInfo aInfo) {
        for (int displayNdx = this.mActivityDisplays.size() - 1; displayNdx >= 0; displayNdx--) {
            ArrayList<ActivityStack> stacks = ((ActivityDisplay) this.mActivityDisplays.valueAt(displayNdx)).mStacks;
            for (int stackNdx = stacks.size() - 1; stackNdx >= 0; stackNdx--) {
                ((ActivityStack) stacks.get(stackNdx)).updateActivityApplicationInfoLocked(aInfo);
            }
        }
    }

    TaskRecord finishTopRunningActivityLocked(ProcessRecord app, String reason) {
        TaskRecord finishedTask = null;
        ActivityStack focusedStack = getFocusedStack();
        for (int displayNdx = this.mActivityDisplays.size() - 1; displayNdx >= 0; displayNdx--) {
            ArrayList<ActivityStack> stacks = ((ActivityDisplay) this.mActivityDisplays.valueAt(displayNdx)).mStacks;
            int numStacks = stacks.size();
            for (int stackNdx = 0; stackNdx < numStacks; stackNdx++) {
                ActivityStack stack = (ActivityStack) stacks.get(stackNdx);
                TaskRecord t = stack.finishTopRunningActivityLocked(app, reason);
                if (stack == focusedStack || finishedTask == null) {
                    finishedTask = t;
                }
            }
        }
        return finishedTask;
    }

    void finishVoiceTask(IVoiceInteractionSession session) {
        for (int displayNdx = this.mActivityDisplays.size() - 1; displayNdx >= 0; displayNdx--) {
            ArrayList<ActivityStack> stacks = ((ActivityDisplay) this.mActivityDisplays.valueAt(displayNdx)).mStacks;
            int numStacks = stacks.size();
            for (int stackNdx = 0; stackNdx < numStacks; stackNdx++) {
                ((ActivityStack) stacks.get(stackNdx)).finishVoiceTask(session);
            }
        }
    }

    void findTaskToMoveToFrontLocked(TaskRecord task, int flags, ActivityOptions options, String reason, boolean forceNonResizeable) {
        if ((flags & 2) == 0) {
            this.mUserLeaving = true;
        }
        if ((flags & 1) != 0) {
            task.setTaskToReturnTo(1);
        }
        ActivityStack currentStack = task.getStack();
        if (currentStack == null) {
            Slog.e(TAG, "findTaskToMoveToFrontLocked: can't move task=" + task + " to front. Stack is null");
            return;
        }
        ActivityRecord top_activity = task.getStack().topRunningActivityLocked();
        if (top_activity != null && top_activity.state == ActivityState.DESTROYED) {
            acquireAppLaunchPerfLock();
        }
        if (task.isResizeable() && options != null) {
            int stackId = options.getLaunchStackId();
            if (canUseActivityOptionsLaunchBounds(options, stackId)) {
                Rect bounds = TaskRecord.validateBounds(options.getLaunchBounds());
                task.updateOverrideConfiguration(bounds);
                if (stackId == -1) {
                    stackId = task.getLaunchStackId();
                }
                if (stackId != currentStack.mStackId) {
                    task.reparent(stackId, true, 1, false, true, "findTaskToMoveToFrontLocked");
                    stackId = currentStack.mStackId;
                }
                if (StackId.resizeStackWithLaunchBounds(stackId)) {
                    resizeStackLocked(stackId, bounds, null, null, false, true, false);
                } else {
                    task.resizeWindowContainer();
                }
            }
        }
        ActivityRecord r = task.getTopActivity();
        currentStack.moveTaskToFrontLocked(task, false, options, r == null ? null : r.appTimeTracker, reason);
        if (ActivityManagerDebugConfig.DEBUG_STACK) {
            Slog.d(TAG_STACK, "findTaskToMoveToFront: moved to front of stack=" + currentStack);
        }
        handleNonResizableTaskIfNeeded(task, -1, 0, currentStack.mStackId, forceNonResizeable);
    }

    boolean canUseActivityOptionsLaunchBounds(ActivityOptions options, int launchStackId) {
        if (options.getLaunchBounds() == null) {
            return false;
        }
        boolean z;
        if (this.mService.mSupportsPictureInPicture && launchStackId == 4) {
            z = true;
        } else {
            z = this.mService.mSupportsFreeformWindowManagement;
        }
        return z;
    }

    protected <T extends ActivityStack> T getStack(int stackId) {
        return getStack(stackId, false, false);
    }

    protected <T extends ActivityStack> T getStack(int stackId, boolean createStaticStackIfNeeded, boolean createOnTop) {
        ActivityContainer activityContainer = (ActivityContainer) this.mActivityContainers.get(stackId);
        if (activityContainer != null) {
            return activityContainer.mStack;
        }
        if (!createStaticStackIfNeeded || (StackId.isStaticStack(stackId) ^ 1) != 0) {
            return null;
        }
        if (stackId == 3) {
            getStack(5, true, createOnTop);
        }
        return createStackOnDisplay(stackId, 0, createOnTop);
    }

    protected ActivityStack getValidLaunchStackOnDisplay(int displayId, ActivityRecord r) {
        ActivityDisplay activityDisplay = getActivityDisplayOrCreateLocked(displayId);
        if (activityDisplay == null) {
            throw new IllegalArgumentException("Display with displayId=" + displayId + " not found.");
        }
        for (int i = activityDisplay.mStacks.size() - 1; i >= 0; i--) {
            ActivityStack stack = (ActivityStack) activityDisplay.mStacks.get(i);
            if (this.mService.mActivityStarter.isValidLaunchStackId(stack.mStackId, displayId, r)) {
                return stack;
            }
        }
        if (displayId != 0) {
            int newDynamicStackId = getNextStackId();
            if (this.mService.mActivityStarter.isValidLaunchStackId(newDynamicStackId, displayId, r)) {
                return createStackOnDisplay(newDynamicStackId, displayId, true);
            }
        }
        Slog.w(TAG, "getValidLaunchStackOnDisplay: can't launch on displayId " + displayId);
        return null;
    }

    ArrayList<ActivityStack> getStacks() {
        ArrayList<ActivityStack> allStacks = new ArrayList();
        for (int displayNdx = this.mActivityDisplays.size() - 1; displayNdx >= 0; displayNdx--) {
            allStacks.addAll(((ActivityDisplay) this.mActivityDisplays.valueAt(displayNdx)).mStacks);
        }
        return allStacks;
    }

    ArrayList<ActivityStack> getStacksOnDefaultDisplay() {
        return ((ActivityDisplay) this.mActivityDisplays.valueAt(0)).mStacks;
    }

    ActivityStack getNextFocusableStackLocked(ActivityStack currentFocus) {
        this.mWindowManager.getDisplaysInFocusOrder(this.mTmpOrderedDisplayIds);
        for (int i = this.mTmpOrderedDisplayIds.size() - 1; i >= 0; i--) {
            List<ActivityStack> stacks = getActivityDisplayOrCreateLocked(this.mTmpOrderedDisplayIds.get(i)).mStacks;
            for (int j = stacks.size() - 1; j >= 0; j--) {
                ActivityStack stack = (ActivityStack) stacks.get(j);
                if (stack != currentFocus && stack.isFocusable() && stack.shouldBeVisible(null) != 0) {
                    return stack;
                }
            }
        }
        return null;
    }

    ActivityStack getNextValidLaunchStackLocked(ActivityRecord r, int currentFocus) {
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

    ActivityRecord getHomeActivity() {
        return getHomeActivityForUser(this.mCurrentUser);
    }

    ActivityRecord getHomeActivityForUser(int userId) {
        ArrayList<TaskRecord> tasks = this.mHomeStack.getAllTasks();
        for (int taskNdx = tasks.size() - 1; taskNdx >= 0; taskNdx--) {
            TaskRecord task = (TaskRecord) tasks.get(taskNdx);
            if (task.isHomeTask()) {
                ArrayList<ActivityRecord> activities = task.mActivities;
                for (int activityNdx = activities.size() - 1; activityNdx >= 0; activityNdx--) {
                    ActivityRecord r = (ActivityRecord) activities.get(activityNdx);
                    if (r.isHomeActivity() && (userId == -1 || r.userId == userId)) {
                        return r;
                    }
                }
                continue;
            }
        }
        return null;
    }

    boolean isStackDockedInEffect(int stackId) {
        if (stackId != 3) {
            return StackId.isResizeableByDockedStack(stackId) && getStack(3) != null;
        } else {
            return true;
        }
    }

    ActivityContainer createVirtualActivityContainer(ActivityRecord parentActivity, IActivityContainerCallback callback) {
        ActivityContainer activityContainer = new VirtualActivityContainer(parentActivity, callback);
        this.mActivityContainers.put(activityContainer.mStackId, activityContainer);
        if (ActivityManagerDebugConfig.DEBUG_CONTAINERS) {
            Slog.d(TAG_CONTAINERS, "createActivityContainer: " + activityContainer);
        }
        parentActivity.mChildContainers.add(activityContainer);
        return activityContainer;
    }

    void removeChildActivityContainers(ActivityRecord parentActivity) {
        ArrayList<ActivityContainer> childStacks = parentActivity.mChildContainers;
        for (int containerNdx = childStacks.size() - 1; containerNdx >= 0; containerNdx--) {
            ActivityContainer container = (ActivityContainer) childStacks.remove(containerNdx);
            if (ActivityManagerDebugConfig.DEBUG_CONTAINERS) {
                Slog.d(TAG_CONTAINERS, "removeChildActivityContainers: removing " + container);
            }
            container.release();
        }
    }

    void deleteActivityContainerRecord(int stackId) {
        if (ActivityManagerDebugConfig.DEBUG_CONTAINERS) {
            Slog.d(TAG_CONTAINERS, "deleteActivityContainerRecord: callers=" + Debug.getCallers(4));
        }
        this.mActivityContainers.remove(stackId);
    }

    void resizeStackLocked(int stackId, Rect bounds, Rect tempTaskBounds, Rect tempTaskInsetBounds, boolean preserveWindows, boolean allowResizeInDockedMode, boolean deferResume) {
        if (stackId == 3) {
            resizeDockedStackLocked(bounds, tempTaskBounds, tempTaskInsetBounds, null, null, preserveWindows, deferResume);
            return;
        }
        ActivityStack stack = getStack(stackId);
        if (stack == null) {
            Slog.w(TAG, "resizeStack: stackId " + stackId + " not found.");
        } else if (allowResizeInDockedMode || (StackId.tasksAreFloating(stackId) ^ 1) == 0 || getStack(3) == null) {
            Trace.traceBegin(64, "am.resizeStack_" + stackId);
            this.mWindowManager.deferSurfaceLayout();
            try {
                stack.resize(bounds, tempTaskBounds, tempTaskInsetBounds);
                if (!deferResume) {
                    stack.ensureVisibleActivitiesConfigurationLocked(stack.topRunningActivityLocked(), preserveWindows);
                }
                this.mWindowManager.continueSurfaceLayout();
                Trace.traceEnd(64);
            } catch (Throwable th) {
                this.mWindowManager.continueSurfaceLayout();
                Trace.traceEnd(64);
            }
        }
    }

    void deferUpdateBounds(int stackId) {
        ActivityStack stack = getStack(stackId);
        if (stack != null) {
            stack.deferUpdateBounds();
        }
    }

    void continueUpdateBounds(int stackId) {
        ActivityStack stack = getStack(stackId);
        if (stack != null) {
            stack.continueUpdateBounds();
        }
    }

    void notifyAppTransitionDone() {
        continueUpdateBounds(5);
        for (int i = this.mResizingTasksDuringAnimation.size() - 1; i >= 0; i--) {
            TaskRecord task = anyTaskForIdLocked(((Integer) this.mResizingTasksDuringAnimation.valueAt(i)).intValue(), 0, -1);
            if (task != null) {
                task.setTaskDockedResizing(false);
            }
        }
        this.mResizingTasksDuringAnimation.clear();
    }

    private void moveTasksToFullscreenStackInSurfaceTransaction(int fromStackId, boolean onTop) {
        ActivityStack stack = getStack(fromStackId);
        if (stack != null) {
            int i;
            this.mWindowManager.deferSurfaceLayout();
            if (fromStackId == 3) {
                i = 0;
                while (i <= 6) {
                    try {
                        if (StackId.isResizeableByDockedStack(i) && getStack(i) != null) {
                            resizeStackLocked(i, null, null, null, true, true, true);
                        }
                        i++;
                    } catch (Throwable th) {
                        this.mAllowDockedStackResize = true;
                        this.mWindowManager.continueSurfaceLayout();
                    }
                }
                this.mAllowDockedStackResize = false;
                Flog.i(101, "The dock stack was dismissed! With stack = " + stack);
                stack.mWindowContainerController.resetBounds();
            } else if (fromStackId == 4 && onTop) {
                MetricsLogger.action(this.mService.mContext, 820);
            }
            ActivityStack fullscreenStack = getStack(1);
            boolean isFullscreenStackVisible = fullscreenStack != null ? fullscreenStack.shouldBeVisible(null) == 1 : false;
            boolean schedulePictureInPictureModeChange = fromStackId == 4;
            ArrayList<TaskRecord> tasks = stack.getAllTasks();
            int size = tasks.size();
            if (onTop) {
                i = 0;
                while (i < size) {
                    TaskRecord task = (TaskRecord) tasks.get(i);
                    boolean isTopTask = i == size + -1;
                    if (fromStackId == 4) {
                        int i2 = (isFullscreenStackVisible && onTop) ? 0 : 1;
                        task.setTaskToReturnTo(i2);
                    }
                    task.reparent(1, true, 0, isTopTask, true, schedulePictureInPictureModeChange, "moveTasksToFullscreenStack - onTop");
                    i++;
                }
            } else {
                for (i = 0; i < size; i++) {
                    ((TaskRecord) tasks.get(i)).reparent(1, i, 2, false, true, schedulePictureInPictureModeChange, "moveTasksToFullscreenStack - NOT_onTop");
                }
            }
            ensureActivitiesVisibleLocked(null, 0, true);
            resumeFocusedStackTopActivityLocked();
            this.mAllowDockedStackResize = true;
            this.mWindowManager.continueSurfaceLayout();
        }
    }

    void moveTasksToFullscreenStackLocked(int fromStackId, boolean onTop) {
        this.mWindowManager.inSurfaceTransaction(new com.android.server.am.-$Lambda$wXoCvN1vCS9Im-C0Hwk121gFGr0.AnonymousClass1(onTop, fromStackId, this));
    }

    void resizeDockedStackLocked(Rect dockedBounds, Rect tempDockedTaskBounds, Rect tempDockedTaskInsetBounds, Rect tempOtherTaskBounds, Rect tempOtherTaskInsetBounds, boolean preserveWindows) {
        resizeDockedStackLocked(dockedBounds, tempDockedTaskBounds, tempDockedTaskInsetBounds, tempOtherTaskBounds, tempOtherTaskInsetBounds, preserveWindows, false);
    }

    void resizeDockedStackLocked(Rect dockedBounds, Rect tempDockedTaskBounds, Rect tempDockedTaskInsetBounds, Rect tempOtherTaskBounds, Rect tempOtherTaskInsetBounds, boolean preserveWindows, boolean deferResume) {
        if (this.mAllowDockedStackResize) {
            ActivityStack stack = getStack(3);
            if (stack == null) {
                Slog.w(TAG, "resizeDockedStackLocked: docked stack not found");
                return;
            }
            Trace.traceBegin(64, "am.resizeDockedStack");
            this.mWindowManager.deferSurfaceLayout();
            try {
                this.mAllowDockedStackResize = false;
                ActivityRecord r = stack.topRunningActivityLocked();
                stack.resize(dockedBounds, tempDockedTaskBounds, tempDockedTaskInsetBounds);
                if (stack.mFullscreen || (dockedBounds == null && (stack.isAttached() ^ 1) != 0)) {
                    moveTasksToFullscreenStackLocked(3, true);
                    r = null;
                } else {
                    Rect otherTaskRect = new Rect();
                    int i = 0;
                    while (i <= 6) {
                        ActivityStack current = getStack(i);
                        if (current != null && StackId.isResizeableByDockedStack(i)) {
                            Rect rect;
                            current.getStackDockedModeBounds(tempOtherTaskBounds, this.tempRect, otherTaskRect, true);
                            Rect rect2 = !this.tempRect.isEmpty() ? this.tempRect : null;
                            if (otherTaskRect.isEmpty()) {
                                rect = tempOtherTaskBounds;
                            } else {
                                rect = otherTaskRect;
                            }
                            resizeStackLocked(i, rect2, rect, tempOtherTaskInsetBounds, preserveWindows, true, deferResume);
                        }
                        i++;
                    }
                }
                if (!deferResume) {
                    stack.ensureVisibleActivitiesConfigurationLocked(r, preserveWindows);
                }
                this.mAllowDockedStackResize = true;
                this.mWindowManager.continueSurfaceLayout();
                Trace.traceEnd(64);
            } catch (Throwable th) {
                this.mAllowDockedStackResize = true;
                this.mWindowManager.continueSurfaceLayout();
                Trace.traceEnd(64);
            }
        }
    }

    void resizePinnedStackLocked(Rect pinnedBounds, Rect tempPinnedTaskBounds) {
        if (HwPCUtils.enabledInPad() && HwPCUtils.isPcCastModeInServer()) {
            HwPCUtils.log(TAG, "ignore pinned stack in pad pc mode");
            return;
        }
        PinnedActivityStack stack = (PinnedActivityStack) getStack(4);
        if (stack == null) {
            Slog.w(TAG, "resizePinnedStackLocked: pinned stack not found");
        } else if (!((PinnedStackWindowController) stack.getWindowContainerController()).pinnedStackResizeDisallowed()) {
            Trace.traceBegin(64, "am.resizePinnedStack");
            this.mWindowManager.deferSurfaceLayout();
            try {
                ActivityRecord r = stack.topRunningActivityLocked();
                Rect insetBounds = null;
                if (tempPinnedTaskBounds != null) {
                    insetBounds = this.tempRect;
                    insetBounds.top = 0;
                    insetBounds.left = 0;
                    insetBounds.right = tempPinnedTaskBounds.width();
                    insetBounds.bottom = tempPinnedTaskBounds.height();
                }
                stack.resize(pinnedBounds, tempPinnedTaskBounds, insetBounds);
                stack.ensureVisibleActivitiesConfigurationLocked(r, false);
            } finally {
                this.mWindowManager.continueSurfaceLayout();
                Trace.traceEnd(64);
            }
        }
    }

    ActivityStack createStackOnDisplay(int stackId, int displayId, boolean onTop) {
        ActivityDisplay activityDisplay = getActivityDisplayOrCreateLocked(displayId);
        if (activityDisplay == null) {
            return null;
        }
        return new ActivityContainer(stackId, activityDisplay, onTop).mStack;
    }

    /* renamed from: removeStackInSurfaceTransaction */
    void lambda$-com_android_server_am_ActivityStackSupervisor_138304(int stackId) {
        ActivityStack stack = getStack(stackId);
        if (stack != null) {
            ArrayList<TaskRecord> tasks = stack.getAllTasks();
            if (stack.getStackId() == 4) {
                PinnedActivityStack pinnedStack = (PinnedActivityStack) stack;
                pinnedStack.mForceHidden = true;
                pinnedStack.ensureActivitiesVisibleLocked(null, 0, true);
                pinnedStack.mForceHidden = false;
                activityIdleInternalLocked(null, false, true, null);
                moveTasksToFullscreenStackLocked(4, false);
            } else {
                for (int i = tasks.size() - 1; i >= 0; i--) {
                    removeTaskByIdLocked(((TaskRecord) tasks.get(i)).taskId, true, true);
                }
            }
        }
    }

    void removeStackLocked(int stackId) {
        this.mWindowManager.inSurfaceTransaction(new -$Lambda$wXoCvN1vCS9Im-C0Hwk121gFGr0(stackId, this));
    }

    boolean removeTaskByIdLocked(int taskId, boolean killProcess, boolean removeFromRecents) {
        return removeTaskByIdLocked(taskId, killProcess, removeFromRecents, false);
    }

    boolean removeTaskByIdLocked(int taskId, boolean killProcess, boolean removeFromRecents, boolean pauseImmediately) {
        TaskRecord tr = anyTaskForIdLocked(taskId, 1, -1);
        if (tr != null) {
            if (!(tr.getBaseIntent() == null || tr.getBaseIntent().getComponent() == null)) {
                String packageName = tr.getBaseIntent().getComponent().getPackageName();
                if (HwDeviceManager.disallowOp(3, packageName)) {
                    Slog.i(TAG, "[" + packageName + "] is Persistent app,won't be killed");
                    UiThread.getHandler().post(new Runnable() {
                        public void run() {
                            Context context = ActivityStackSupervisor.this.mService.mUiContext;
                            if (context != null) {
                                Toast.makeText(context, context.getString(33685975), 0).show();
                            }
                        }
                    });
                    return false;
                } else if (HwPCUtils.isPcCastModeInServer()) {
                    if (HwPCUtils.enabledInPad() && "com.android.incallui".equals(packageName)) {
                        Slog.i(TAG, "[" + packageName + "] is Service app,won't be killed");
                        return false;
                    } else if (!killProcess && "com.chinamworld.main".equals(packageName)) {
                        Slog.i(TAG, "[" + packageName + "] remove task and kill process in pc mode");
                        tr.removeTaskActivitiesLocked(pauseImmediately);
                        cleanUpRemovedTaskLocked(tr, true, removeFromRecents);
                        if (tr.isPersistable) {
                            this.mService.notifyTaskPersisterLocked(null, true);
                        }
                        return true;
                    }
                }
            }
            tr.removeTaskActivitiesLocked(pauseImmediately);
            cleanUpRemovedTaskLocked(tr, killProcess, removeFromRecents);
            if (tr.isPersistable) {
                this.mService.notifyTaskPersisterLocked(null, true);
            }
            return true;
        }
        Slog.w(TAG, "Request to remove task ignored for non-existent task " + taskId);
        return false;
    }

    void cleanUpRemovedTaskLocked(TaskRecord tr, boolean killProcess, boolean removeFromRecents) {
        if (removeFromRecents) {
            this.mRecentTasks.remove(tr);
            tr.removedFromRecents();
        }
        ComponentName component = tr.getBaseIntent().getComponent();
        if (component == null) {
            Slog.w(TAG, "No component for base intent of task: " + tr);
            return;
        }
        this.mService.mServices.cleanUpRemovedTaskLocked(tr, component, new Intent(tr.getBaseIntent()));
        if (killProcess) {
            String pkg = component.getPackageName();
            if (!shouldNotKillProcWhenRemoveTask(pkg)) {
                int i;
                if (this.mService.getRecordCust() != null) {
                    this.mService.getRecordCust().appExitRecord(pkg, "rkill");
                }
                ArrayList<ProcessRecord> procsToKill = new ArrayList();
                ArrayMap<String, SparseArray<ProcessRecord>> pmap = this.mService.mProcessNames.getMap();
                for (i = 0; i < pmap.size(); i++) {
                    SparseArray<ProcessRecord> uids = (SparseArray) pmap.valueAt(i);
                    for (int j = 0; j < uids.size(); j++) {
                        ProcessRecord proc = (ProcessRecord) uids.valueAt(j);
                        if (HwDeviceManager.disallowOp(22, proc.info.packageName)) {
                            Slog.i(TAG, "[" + proc.info.packageName + "] is super-whitelist app,won't be killed by remove task");
                        } else if (proc.userId == tr.userId && proc != this.mService.mHomeProcess && proc.pkgList.containsKey(pkg)) {
                            int k = 0;
                            while (k < proc.activities.size()) {
                                TaskRecord otherTask = ((ActivityRecord) proc.activities.get(k)).getTask();
                                if (otherTask == null || tr.taskId == otherTask.taskId || !otherTask.inRecents) {
                                    k++;
                                } else {
                                    return;
                                }
                            }
                            if (!proc.foregroundServices) {
                                procsToKill.add(proc);
                            } else {
                                return;
                            }
                        }
                    }
                }
                for (i = 0; i < procsToKill.size(); i++) {
                    ProcessRecord pr = (ProcessRecord) procsToKill.get(i);
                    if (pr != null) {
                        if (pr.curAdj >= 900 && (pr.info.flags & 1) != 0 && (pr.info.hwFlags & 33554432) == 0 && notKillProcessWhenRemoveTask()) {
                            Slog.d(TAG, " the process " + pr.processName + " adj >= " + 900);
                            try {
                                SmartShrinker.reclaim(pr.pid, 4);
                                if (pr.thread != null) {
                                    pr.thread.scheduleTrimMemory(80);
                                }
                            } catch (RemoteException e) {
                            }
                            pr.trimMemoryLevel = 80;
                        } else if (pr.setSchedGroup == 0 && pr.curReceivers.isEmpty()) {
                            pr.kill("remove task", true);
                        } else {
                            pr.waitingToKill = "remove task";
                        }
                    }
                }
            }
        }
    }

    int getNextStackId() {
        while (true) {
            if (this.mNextFreeStackId >= 7 && getStack(this.mNextFreeStackId) == null) {
                return this.mNextFreeStackId;
            }
            this.mNextFreeStackId++;
        }
    }

    protected boolean restoreRecentTaskLocked(TaskRecord task, int stackId) {
        if (!StackId.isStaticStack(stackId)) {
            stackId = task.getLaunchStackId();
        } else if (stackId == 3 && (task.supportsSplitScreen() ^ 1) != 0) {
            stackId = 1;
        }
        ActivityStack currentStack = task.getStack();
        if (currentStack != null) {
            if (currentStack.mStackId == stackId) {
                return true;
            }
            currentStack.removeTask(task, "restoreRecentTaskLocked", 1);
        }
        ActivityStack stack = getStack(stackId, true, false);
        if (stack == null) {
            if (ActivityManagerDebugConfig.DEBUG_RECENTS) {
                Slog.v(TAG_RECENTS, "Unable to find/create stack to restore recent task=" + task);
            }
            return false;
        } else if (getFocusedStack() == null || getFocusedStack().mStackId != 1) {
            stack.addTask(task, false, "restoreRecentTask");
            task.createWindowContainer(false, true);
            if (ActivityManagerDebugConfig.DEBUG_RECENTS) {
                Slog.v(TAG_RECENTS, "Added restored task=" + task + " to stack=" + stack);
            }
            ArrayList<ActivityRecord> activities = task.mActivities;
            for (int activityNdx = activities.size() - 1; activityNdx >= 0; activityNdx--) {
                ((ActivityRecord) activities.get(activityNdx)).createWindowContainer(((ActivityRecord) activities.get(activityNdx)).info.navigationHide);
            }
            return true;
        } else {
            Slog.v(TAG_RECENTS, "Skip restoring recent task=" + task + " to stack=" + stack);
            return true;
        }
    }

    void moveStackToDisplayLocked(int stackId, int displayId, boolean onTop) {
        ActivityDisplay activityDisplay = getActivityDisplayOrCreateLocked(displayId);
        if (activityDisplay == null) {
            throw new IllegalArgumentException("moveStackToDisplayLocked: Unknown displayId=" + displayId);
        }
        ActivityContainer activityContainer = (ActivityContainer) this.mActivityContainers.get(stackId);
        if (activityContainer == null) {
            throw new IllegalArgumentException("moveStackToDisplayLocked: Unknown stackId=" + stackId);
        } else if (!activityContainer.isAttachedLocked()) {
            throw new IllegalStateException("moveStackToDisplayLocked: Stack with stackId=" + stackId + " is not attached to any display.");
        } else if (activityContainer.getDisplayId() == displayId) {
            throw new IllegalArgumentException("Trying to move stackId=" + stackId + " to its current displayId=" + displayId);
        } else {
            activityContainer.moveToDisplayLocked(activityDisplay, onTop);
        }
    }

    ActivityStack getReparentTargetStack(TaskRecord task, int stackId, boolean toTop) {
        ActivityStack prevStack = task.getStack();
        if (prevStack != null && prevStack.mStackId == stackId) {
            Slog.w(TAG, "Can not reparent to same stack, task=" + task + " already in stackId=" + stackId);
            return prevStack;
        } else if (StackId.isMultiWindowStack(stackId) && (this.mService.mSupportsMultiWindow ^ 1) != 0) {
            throw new IllegalArgumentException("Device doesn't support multi-window, can not reparent task=" + task + " to stackId=" + stackId);
        } else if (StackId.isDynamicStack(stackId) && (this.mService.mSupportsMultiDisplay ^ 1) != 0) {
            throw new IllegalArgumentException("Device doesn't support multi-display, can not reparent task=" + task + " to stackId=" + stackId);
        } else if (stackId != 2 || (this.mService.mSupportsFreeformWindowManagement ^ 1) == 0) {
            if (stackId == 3 && (task.isResizeable() ^ 1) != 0) {
                stackId = prevStack != null ? prevStack.mStackId : 1;
                Slog.w(TAG, "Can not move unresizeable task=" + task + " to docked stack." + " Moving to stackId=" + stackId + " instead.");
            }
            try {
                task.mTemporarilyUnresizable = true;
                ActivityStack stack = getStack(stackId, true, toTop);
                return stack;
            } finally {
                task.mTemporarilyUnresizable = false;
            }
        } else {
            throw new IllegalArgumentException("Device doesn't support freeform, can not reparent task=" + task);
        }
    }

    boolean moveTopStackActivityToPinnedStackLocked(int stackId, Rect destBounds) {
        if (HwPCUtils.enabledInPad() && HwPCUtils.isPcCastModeInServer()) {
            HwPCUtils.log(TAG, "ignore moveTopStackActivityToPinnedStackLocked in pad pc mode");
            return false;
        }
        ActivityStack stack = getStack(stackId, false, false);
        if (stack == null) {
            throw new IllegalArgumentException("moveTopStackActivityToPinnedStackLocked: Unknown stackId=" + stackId);
        }
        ActivityRecord r = stack.topRunningActivityLocked();
        if (r == null) {
            Slog.w(TAG, "moveTopStackActivityToPinnedStackLocked: No top running activity in stack=" + stack);
            return false;
        } else if (this.mService.mForceResizableActivities || (r.supportsPictureInPicture() ^ 1) == 0) {
            moveActivityToPinnedStackLocked(r, null, 0.0f, true, "moveTopActivityToPinnedStack");
            return true;
        } else {
            Slog.w(TAG, "moveTopStackActivityToPinnedStackLocked: Picture-In-Picture not supported for  r=" + r);
            return false;
        }
    }

    void moveActivityToPinnedStackLocked(ActivityRecord r, Rect sourceHintBounds, float aspectRatio, boolean moveHomeStackToFront, String reason) {
        this.mWindowManager.deferSurfaceLayout();
        moveTasksToFullscreenStackLocked(4, false);
        PinnedActivityStack stack = (PinnedActivityStack) getStack(4, true, true);
        try {
            TaskRecord task = r.getTask();
            if (r == task.getStack().getVisibleBehindActivity()) {
                requestVisibleBehindLocked(r, false);
            }
            resizeStackLocked(4, task.mBounds, null, null, false, true, false);
            if (task.mActivities.size() == 1) {
                if (moveHomeStackToFront && task.getTaskToReturnTo() == 1 && (r.state == ActivityState.RESUMED || (r.supportsPictureInPictureWhilePausing ^ 1) != 0)) {
                    moveHomeStackToFront(reason);
                }
                task.reparent(4, true, 0, false, true, false, reason);
            } else {
                TaskRecord newTask = task.getStack().createTaskRecord(getNextTaskIdForUserLocked(r.userId), r.info, r.intent, null, null, true, r.mActivityType);
                r.reparent(newTask, HwBootFail.STAGE_BOOT_SUCCESS, "moveActivityToStack");
                newTask.reparent(4, true, 0, false, true, false, reason);
            }
            r.supportsPictureInPictureWhilePausing = false;
            stack.animateResizePinnedStack(sourceHintBounds, stack.getDefaultPictureInPictureBounds(aspectRatio), -1, true);
            ensureActivitiesVisibleLocked(null, 0, false);
            resumeFocusedStackTopActivityLocked();
            this.mService.mTaskChangeNotificationController.notifyActivityPinned(r.packageName, r.getTask().taskId);
        } finally {
            this.mWindowManager.continueSurfaceLayout();
        }
    }

    boolean moveFocusableActivityStackToFrontLocked(ActivityRecord r, String reason) {
        if (r == null || (r.isFocusable() ^ 1) != 0) {
            return false;
        }
        TaskRecord task = r.getTask();
        ActivityStack stack = r.getStack();
        if (stack == null) {
            Slog.w(TAG, "moveActivityStackToFront: invalid task or stack: r=" + r + " task=" + task);
            return false;
        } else if (stack == this.mFocusedStack && stack.topRunningActivityLocked() == r) {
            return false;
        } else {
            stack.moveToFront(reason, task);
            if (IS_DEBUG_VERSION) {
                ArrayMap<String, Object> params = new ArrayMap();
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

    void acquireAppLaunchPerfLock() {
        if (this.mIsperfDisablepackingEnable && this.mPerfPack == null) {
            this.mPerfPack = new BoostFramework();
        }
        if (this.mPerfPack != null) {
            this.mPerfPack.perfLockAcquire(this.lDisPackTimeOut, this.lBoostPackParamVal);
        }
        if (this.mIsPerfBoostEnabled && this.mPerfBoost == null) {
            this.mPerfBoost = new BoostFramework();
        }
        if (this.mPerfBoost != null) {
            this.mPerfBoost.perfLockAcquire(this.lBoostTimeOut, this.lBoostCpuParamVal);
        }
    }

    ActivityRecord findTaskLocked(ActivityRecord r, int displayId) {
        this.mTmpFindTaskResult.r = null;
        this.mTmpFindTaskResult.matchedByRootAffinity = false;
        ActivityRecord affinityMatch = null;
        if (ActivityManagerDebugConfig.DEBUG_TASKS) {
            Slog.d(TAG_TASKS, "Looking for task of " + r);
        }
        for (int displayNdx = this.mActivityDisplays.size() - 1; displayNdx >= 0; displayNdx--) {
            ArrayList<ActivityStack> stacks = ((ActivityDisplay) this.mActivityDisplays.valueAt(displayNdx)).mStacks;
            for (int stackNdx = stacks.size() - 1; stackNdx >= 0; stackNdx--) {
                ActivityStack stack = (ActivityStack) stacks.get(stackNdx);
                if (checkActivityBelongsInStack(r, stack)) {
                    if (stack.mActivityContainer.isEligibleForNewTasks()) {
                        stack.findTaskLocked(r, this.mTmpFindTaskResult);
                        if (this.mTmpFindTaskResult.r == null) {
                            continue;
                        } else if (!this.mTmpFindTaskResult.matchedByRootAffinity) {
                            if (this.mTmpFindTaskResult.r.state == ActivityState.DESTROYED) {
                                acquireAppLaunchPerfLock();
                                if (this.mIsPerfBoostEnabled && this.mPerf_iop == null) {
                                    this.mPerf_iop = new BoostFramework();
                                }
                                if (this.mPerf_iop != null) {
                                    this.mPerf_iop.perfIOPrefetchStart(-1, r.packageName);
                                }
                            }
                            return this.mTmpFindTaskResult.r;
                        } else if (this.mTmpFindTaskResult.r.getDisplayId() == displayId) {
                            affinityMatch = this.mTmpFindTaskResult.r;
                        }
                    } else if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                        Slog.d(TAG_TASKS, "Skipping stack: (new task not allowed) " + stack);
                    }
                } else if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                    Slog.d(TAG_TASKS, "Skipping stack: (mismatch activity/stack) " + stack);
                }
            }
        }
        acquireAppLaunchPerfLock();
        if (this.mIsPerfBoostEnabled && this.mPerf_iop == null) {
            this.mPerf_iop = new BoostFramework();
        }
        if (this.mPerf_iop != null) {
            this.mPerf_iop.perfIOPrefetchStart(-1, r.packageName);
        }
        if (ActivityManagerDebugConfig.DEBUG_TASKS && affinityMatch == null) {
            Slog.d(TAG_TASKS, "No task found");
        }
        return affinityMatch;
    }

    private boolean checkActivityBelongsInStack(ActivityRecord r, ActivityStack stack) {
        if (r.isHomeActivity()) {
            return stack.isHomeStack();
        }
        if (r.isRecentsActivity()) {
            return stack.isRecentsStack();
        }
        if (r.isAssistantActivity()) {
            return stack.isAssistantStack();
        }
        return true;
    }

    ActivityRecord findActivityLocked(Intent intent, ActivityInfo info, boolean compareIntentFilters) {
        for (int displayNdx = this.mActivityDisplays.size() - 1; displayNdx >= 0; displayNdx--) {
            ArrayList<ActivityStack> stacks = ((ActivityDisplay) this.mActivityDisplays.valueAt(displayNdx)).mStacks;
            for (int stackNdx = stacks.size() - 1; stackNdx >= 0; stackNdx--) {
                ActivityRecord ar = ((ActivityStack) stacks.get(stackNdx)).findActivityLocked(intent, info, compareIntentFilters);
                if (ar != null) {
                    return ar;
                }
            }
        }
        return null;
    }

    void goingToSleepLocked() {
        scheduleSleepTimeout();
        if (!this.mGoingToSleep.isHeld()) {
            this.mGoingToSleep.acquire();
            if (this.mLaunchingActivity.isHeld()) {
                this.mLaunchingActivity.release();
                this.mService.mHandler.removeMessages(104);
            }
        }
        checkReadyForSleepLocked();
    }

    boolean shutdownLocked(int timeout) {
        goingToSleepLocked();
        boolean timedout = false;
        long endTime = System.currentTimeMillis() + ((long) timeout);
        while (true) {
            boolean cantShutdown = false;
            for (int displayNdx = this.mActivityDisplays.size() - 1; displayNdx >= 0; displayNdx--) {
                ArrayList<ActivityStack> stacks = ((ActivityDisplay) this.mActivityDisplays.valueAt(displayNdx)).mStacks;
                for (int stackNdx = stacks.size() - 1; stackNdx >= 0; stackNdx--) {
                    cantShutdown |= ((ActivityStack) stacks.get(stackNdx)).checkReadyForSleepLocked();
                }
            }
            if (!cantShutdown) {
                break;
            }
            long timeRemaining = endTime - System.currentTimeMillis();
            if (timeRemaining <= 0) {
                Slog.w(TAG, "Activity manager shutdown timed out");
                timedout = true;
                break;
            }
            try {
                this.mService.wait(timeRemaining);
            } catch (InterruptedException e) {
            }
        }
        this.mSleepTimeout = true;
        checkReadyForSleepLocked();
        return timedout;
    }

    void comeOutOfSleepIfNeededLocked() {
        removeSleepTimeouts();
        if (this.mGoingToSleep.isHeld()) {
            this.mGoingToSleep.release();
        }
        for (int displayNdx = this.mActivityDisplays.size() - 1; displayNdx >= 0; displayNdx--) {
            ArrayList<ActivityStack> stacks = ((ActivityDisplay) this.mActivityDisplays.valueAt(displayNdx)).mStacks;
            for (int stackNdx = stacks.size() - 1; stackNdx >= 0; stackNdx--) {
                ActivityStack stack = (ActivityStack) stacks.get(stackNdx);
                stack.awakeFromSleepingLocked();
                if (isFocusedStack(stack)) {
                    this.mActivityLaunchTrack = "outofsleep";
                    resumeFocusedStackTopActivityLocked();
                } else if (!stack.mFullscreen && (stack.mStackId == 3 || stack.mStackId == 1)) {
                    resumeAppLockActivityIfNeeded(stack, null);
                }
            }
        }
        this.mGoingToSleepActivities.clear();
    }

    void activitySleptLocked(ActivityRecord r) {
        this.mGoingToSleepActivities.remove(r);
        checkReadyForSleepLocked();
    }

    void checkReadyForSleepLocked() {
        if (this.mService.isSleepingOrShuttingDownLocked()) {
            int displayNdx;
            ArrayList<ActivityStack> stacks;
            int stackNdx;
            if (!this.mSleepTimeout) {
                boolean dontSleep = false;
                for (displayNdx = this.mActivityDisplays.size() - 1; displayNdx >= 0; displayNdx--) {
                    stacks = ((ActivityDisplay) this.mActivityDisplays.valueAt(displayNdx)).mStacks;
                    for (stackNdx = stacks.size() - 1; stackNdx >= 0; stackNdx--) {
                        dontSleep |= ((ActivityStack) stacks.get(stackNdx)).checkReadyForSleepLocked();
                    }
                }
                if (this.mStoppingActivities.size() > 0) {
                    if (ActivityManagerDebugConfig.DEBUG_PAUSE) {
                        Slog.v(TAG_PAUSE, "Sleep still need to stop " + this.mStoppingActivities.size() + " activities");
                    }
                    scheduleIdleLocked();
                    dontSleep = true;
                }
                if (this.mGoingToSleepActivities.size() > 0) {
                    if (ActivityManagerDebugConfig.DEBUG_PAUSE) {
                        Slog.v(TAG_PAUSE, "Sleep still need to sleep " + this.mGoingToSleepActivities.size() + " activities");
                    }
                    dontSleep = true;
                }
                if (dontSleep) {
                    return;
                }
            }
            this.mService.mActivityStarter.sendPowerHintForLaunchEndIfNeeded();
            for (displayNdx = this.mActivityDisplays.size() - 1; displayNdx >= 0; displayNdx--) {
                stacks = ((ActivityDisplay) this.mActivityDisplays.valueAt(displayNdx)).mStacks;
                for (stackNdx = stacks.size() - 1; stackNdx >= 0; stackNdx--) {
                    ((ActivityStack) stacks.get(stackNdx)).goToSleep();
                }
            }
            removeSleepTimeouts();
            if (this.mGoingToSleep.isHeld()) {
                this.mGoingToSleep.release();
            }
            if (this.mService.mShuttingDown) {
                this.mService.notifyAll();
            }
        }
    }

    boolean reportResumedActivityLocked(ActivityRecord r) {
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

    void handleAppCrashLocked(ProcessRecord app) {
        for (int displayNdx = this.mActivityDisplays.size() - 1; displayNdx >= 0; displayNdx--) {
            ArrayList<ActivityStack> stacks = ((ActivityDisplay) this.mActivityDisplays.valueAt(displayNdx)).mStacks;
            for (int stackNdx = stacks.size() - 1; stackNdx >= 0; stackNdx--) {
                ((ActivityStack) stacks.get(stackNdx)).handleAppCrashLocked(app);
            }
        }
    }

    boolean requestVisibleBehindLocked(ActivityRecord r, boolean visible) {
        ActivityRecord activityRecord = null;
        ActivityStack stack = r.getStack();
        if (stack == null) {
            if (ActivityManagerDebugConfig.DEBUG_VISIBLE_BEHIND) {
                Slog.d(TAG_VISIBLE_BEHIND, "requestVisibleBehind: r=" + r + " visible=" + visible + " stack is null");
            }
            return false;
        } else if (!visible || (StackId.activitiesCanRequestVisibleBehind(stack.mStackId) ^ 1) == 0) {
            boolean isVisible = stack.hasVisibleBehindActivity();
            if (ActivityManagerDebugConfig.DEBUG_VISIBLE_BEHIND) {
                Slog.d(TAG_VISIBLE_BEHIND, "requestVisibleBehind r=" + r + " visible=" + visible + " isVisible=" + isVisible);
            }
            ActivityRecord top = topRunningActivityLocked();
            if (top == null || top == r || visible == isVisible) {
                if (ActivityManagerDebugConfig.DEBUG_VISIBLE_BEHIND) {
                    Slog.d(TAG_VISIBLE_BEHIND, "requestVisibleBehind: quick return");
                }
                if (!visible) {
                    r = null;
                }
                stack.setVisibleBehindActivity(r);
                return true;
            } else if (visible && top.fullscreen) {
                if (ActivityManagerDebugConfig.DEBUG_VISIBLE_BEHIND) {
                    Slog.d(TAG_VISIBLE_BEHIND, "requestVisibleBehind: returning top.fullscreen=" + top.fullscreen + " top.state=" + top.state + " top.app=" + top.app + " top.app.thread=" + top.app.thread);
                }
                return false;
            } else if (visible || stack.getVisibleBehindActivity() == r) {
                if (visible) {
                    activityRecord = r;
                }
                stack.setVisibleBehindActivity(activityRecord);
                if (!visible) {
                    ActivityRecord next = stack.findNextTranslucentActivity(r);
                    if (next != null && next.isHomeActivity()) {
                        this.mService.convertFromTranslucent(next.appToken);
                    }
                }
                if (!(top.app == null || top.app.thread == null)) {
                    try {
                        top.app.thread.scheduleBackgroundVisibleBehindChanged(top.appToken, visible);
                    } catch (RemoteException e) {
                    }
                }
                return true;
            } else {
                if (ActivityManagerDebugConfig.DEBUG_VISIBLE_BEHIND) {
                    Slog.d(TAG_VISIBLE_BEHIND, "requestVisibleBehind: returning visible=" + visible + " stack.getVisibleBehindActivity()=" + stack.getVisibleBehindActivity() + " r=" + r);
                }
                return false;
            }
        } else {
            if (ActivityManagerDebugConfig.DEBUG_VISIBLE_BEHIND) {
                Slog.d(TAG_VISIBLE_BEHIND, "requestVisibleBehind: r=" + r + " visible=" + visible + " stackId=" + stack.mStackId + " can't contain visible behind activities");
            }
            return false;
        }
    }

    private void handleLaunchTaskBehindCompleteLocked(ActivityRecord r) {
        TaskRecord task = r.getTask();
        ActivityStack stack = task.getStack();
        r.mLaunchTaskBehind = false;
        task.setLastThumbnailLocked(r.screenshotActivityLocked());
        this.mRecentTasks.addLocked(task);
        this.mService.mTaskChangeNotificationController.notifyTaskStackChanged();
        r.setVisibility(false);
        ActivityRecord top = stack.topActivity();
        if (top != null) {
            top.getTask().touchActiveTime();
        }
    }

    void scheduleLaunchTaskBehindComplete(IBinder token) {
        this.mHandler.obtainMessage(112, token).sendToTarget();
    }

    void ensureActivitiesVisibleLocked(ActivityRecord starting, int configChanges, boolean preserveWindows) {
        this.mKeyguardController.beginActivityVisibilityUpdate();
        try {
            for (int displayNdx = this.mActivityDisplays.size() - 1; displayNdx >= 0; displayNdx--) {
                ArrayList<ActivityStack> stacks = ((ActivityDisplay) this.mActivityDisplays.valueAt(displayNdx)).mStacks;
                for (int stackNdx = stacks.size() - 1; stackNdx >= 0; stackNdx--) {
                    ((ActivityStack) stacks.get(stackNdx)).ensureActivitiesVisibleLocked(starting, configChanges, preserveWindows);
                }
            }
        } catch (IndexOutOfBoundsException e) {
            Slog.e(TAG, "ensureActivitiesVisibleLocked has Exception : IndexOutOfBoundsException");
        } finally {
            this.mKeyguardController.endActivityVisibilityUpdate();
        }
    }

    void addStartingWindowsForVisibleActivities(boolean taskSwitch) {
        for (int displayNdx = this.mActivityDisplays.size() - 1; displayNdx >= 0; displayNdx--) {
            ArrayList<ActivityStack> stacks = ((ActivityDisplay) this.mActivityDisplays.valueAt(displayNdx)).mStacks;
            for (int stackNdx = stacks.size() - 1; stackNdx >= 0; stackNdx--) {
                ((ActivityStack) stacks.get(stackNdx)).addStartingWindowsForVisibleActivities(taskSwitch);
            }
        }
    }

    void invalidateTaskLayers() {
        this.mTaskLayersChanged = true;
    }

    void rankTaskLayersIfNeeded() {
        if (this.mTaskLayersChanged) {
            this.mTaskLayersChanged = false;
            for (int displayNdx = 0; displayNdx < this.mActivityDisplays.size(); displayNdx++) {
                ArrayList<ActivityStack> stacks = ((ActivityDisplay) this.mActivityDisplays.valueAt(displayNdx)).mStacks;
                int baseLayer = 0;
                for (int stackNdx = stacks.size() - 1; stackNdx >= 0; stackNdx--) {
                    baseLayer += ((ActivityStack) stacks.get(stackNdx)).rankTaskLayers(baseLayer);
                }
            }
        }
    }

    void clearOtherAppTimeTrackers(AppTimeTracker except) {
        for (int displayNdx = this.mActivityDisplays.size() - 1; displayNdx >= 0; displayNdx--) {
            ArrayList<ActivityStack> stacks = ((ActivityDisplay) this.mActivityDisplays.valueAt(displayNdx)).mStacks;
            for (int stackNdx = stacks.size() - 1; stackNdx >= 0; stackNdx--) {
                ((ActivityStack) stacks.get(stackNdx)).clearOtherAppTimeTrackers(except);
            }
        }
    }

    void scheduleDestroyAllActivities(ProcessRecord app, String reason) {
        for (int displayNdx = this.mActivityDisplays.size() - 1; displayNdx >= 0; displayNdx--) {
            ArrayList<ActivityStack> stacks = ((ActivityDisplay) this.mActivityDisplays.valueAt(displayNdx)).mStacks;
            int numStacks = stacks.size();
            for (int stackNdx = 0; stackNdx < numStacks; stackNdx++) {
                ((ActivityStack) stacks.get(stackNdx)).scheduleDestroyActivities(app, reason);
            }
        }
    }

    void releaseSomeActivitiesLocked(ProcessRecord app, String reason) {
        TaskRecord firstTask = null;
        ArraySet tasks = null;
        if (ActivityManagerDebugConfig.DEBUG_RELEASE) {
            Slog.d(TAG_RELEASE, "Trying to release some activities in " + app);
        }
        for (int i = 0; i < app.activities.size(); i++) {
            ActivityRecord r = (ActivityRecord) app.activities.get(i);
            if (r.finishing || r.state == ActivityState.DESTROYING || r.state == ActivityState.DESTROYED) {
                if (ActivityManagerDebugConfig.DEBUG_RELEASE) {
                    Slog.d(TAG_RELEASE, "Abort release; already destroying: " + r);
                }
                return;
            }
            if (!r.visible && (r.stopped ^ 1) == 0 && (r.haveState ^ 1) == 0 && r.state != ActivityState.RESUMED && r.state != ActivityState.PAUSING && r.state != ActivityState.PAUSED && r.state != ActivityState.STOPPING) {
                TaskRecord task = r.getTask();
                if (task != null) {
                    if (ActivityManagerDebugConfig.DEBUG_RELEASE) {
                        Slog.d(TAG_RELEASE, "Collecting release task " + task + " from " + r);
                    }
                    if (firstTask == null) {
                        firstTask = task;
                    } else if (firstTask != task) {
                        if (tasks == null) {
                            tasks = new ArraySet();
                            tasks.add(firstTask);
                        }
                        tasks.add(task);
                    }
                }
            } else if (ActivityManagerDebugConfig.DEBUG_RELEASE) {
                Slog.d(TAG_RELEASE, "Not releasing in-use activity: " + r);
            }
        }
        if (tasks == null) {
            if (ActivityManagerDebugConfig.DEBUG_RELEASE) {
                Slog.d(TAG_RELEASE, "Didn't find two or more tasks to release");
            }
            return;
        }
        int numDisplays = this.mActivityDisplays.size();
        for (int displayNdx = 0; displayNdx < numDisplays; displayNdx++) {
            ArrayList<ActivityStack> stacks = ((ActivityDisplay) this.mActivityDisplays.valueAt(displayNdx)).mStacks;
            int stackNdx = 0;
            while (stackNdx < stacks.size()) {
                if (((ActivityStack) stacks.get(stackNdx)).releaseSomeActivitiesLocked(app, tasks, reason) <= 0) {
                    stackNdx++;
                } else {
                    return;
                }
            }
        }
    }

    boolean switchUserLocked(int userId, UserState uss) {
        boolean z;
        ActivityStack stack;
        int focusStackId = this.mFocusedStack.getStackId();
        if (focusStackId == 3) {
            z = true;
        } else {
            z = false;
        }
        moveTasksToFullscreenStackLocked(3, z);
        removeStackLocked(4);
        this.mUserStackInFront.put(this.mCurrentUser, focusStackId);
        int restoreStackId = this.mUserStackInFront.get(userId, 0);
        this.mCurrentUser = userId;
        this.mStartingUsers.add(uss);
        for (int displayNdx = this.mActivityDisplays.size() - 1; displayNdx >= 0; displayNdx--) {
            ArrayList<ActivityStack> stacks = ((ActivityDisplay) this.mActivityDisplays.valueAt(displayNdx)).mStacks;
            for (int stackNdx = stacks.size() - 1; stackNdx >= 0; stackNdx--) {
                stack = (ActivityStack) stacks.get(stackNdx);
                stack.switchUserLocked(userId);
                TaskRecord task = stack.topTask();
                if (task != null) {
                    stack.positionChildWindowContainerAtTop(task);
                }
            }
        }
        stack = getStack(restoreStackId);
        if (stack == null) {
            stack = this.mHomeStack;
        }
        boolean homeInFront = stack.isHomeStack();
        if (stack.isOnHomeDisplay()) {
            stack.moveToFront("switchUserOnHomeDisplay");
        } else {
            resumeHomeStackTask(null, "switchUserOnOtherDisplay");
        }
        return homeInFront;
    }

    boolean isCurrentProfileLocked(int userId) {
        if (userId == this.mCurrentUser) {
            return true;
        }
        return this.mService.mUserController.isCurrentProfileLocked(userId);
    }

    boolean isStoppingNoHistoryActivity() {
        for (ActivityRecord record : this.mStoppingActivities) {
            if (record.isNoHistory()) {
                return true;
            }
        }
        return false;
    }

    final ArrayList<ActivityRecord> processStoppingActivitiesLocked(ActivityRecord idleActivity, boolean remove, boolean processPausingActivities) {
        ArrayList<ActivityRecord> stops = null;
        boolean nowVisible = allResumedActivitiesVisible();
        for (int activityNdx = this.mStoppingActivities.size() - 1; activityNdx >= 0; activityNdx--) {
            ActivityRecord s = (ActivityRecord) this.mStoppingActivities.get(activityNdx);
            boolean waitingVisible = this.mActivitiesWaitingForVisibleActivity.contains(s);
            if (ActivityManagerDebugConfig.DEBUG_STATES) {
                Slog.v(TAG, "Stopping " + s + ": nowVisible=" + nowVisible + " waitingVisible=" + waitingVisible + " finishing=" + s.finishing);
            }
            if (waitingVisible && nowVisible) {
                this.mActivitiesWaitingForVisibleActivity.remove(s);
                waitingVisible = false;
                if (s.finishing) {
                    if (ActivityManagerDebugConfig.DEBUG_STATES) {
                        Slog.v(TAG, "Before stopping, can hide: " + s);
                    }
                    s.setVisibility(false);
                }
            }
            if ((!waitingVisible || this.mService.isSleepingOrShuttingDownLocked()) && remove) {
                if (processPausingActivities || s.state != ActivityState.PAUSING) {
                    if (ActivityManagerDebugConfig.DEBUG_STATES) {
                        Slog.v(TAG, "Ready to stop: " + s);
                    }
                    if (stops == null) {
                        stops = new ArrayList();
                    }
                    stops.add(s);
                    this.mStoppingActivities.remove(activityNdx);
                } else {
                    removeTimeoutsForActivityLocked(idleActivity);
                    scheduleIdleTimeoutLocked(idleActivity);
                }
            }
        }
        return stops;
    }

    void validateTopActivitiesLocked() {
        for (int displayNdx = this.mActivityDisplays.size() - 1; displayNdx >= 0; displayNdx--) {
            ArrayList<ActivityStack> stacks = ((ActivityDisplay) this.mActivityDisplays.valueAt(displayNdx)).mStacks;
            for (int stackNdx = stacks.size() - 1; stackNdx >= 0; stackNdx--) {
                ActivityStack stack = (ActivityStack) stacks.get(stackNdx);
                ActivityRecord r = stack.topRunningActivityLocked();
                ActivityState state = r == null ? ActivityState.DESTROYED : r.state;
                if (!isFocusedStack(stack)) {
                    ActivityRecord resumed = stack.mResumedActivity;
                    if (resumed != null && resumed == r) {
                        Slog.e(TAG, "validateTop...: back stack has resumed activity r=" + r + " state=" + state);
                    }
                    if (r != null && (state == ActivityState.INITIALIZING || state == ActivityState.RESUMED)) {
                        Slog.e(TAG, "validateTop...: activity in back resumed r=" + r + " state=" + state);
                    }
                } else if (r == null) {
                    Slog.e(TAG, "validateTop...: null top activity, stack=" + stack);
                } else {
                    ActivityRecord pausing = stack.mPausingActivity;
                    if (pausing != null && pausing == r) {
                        Slog.e(TAG, "validateTop...: top stack has pausing activity r=" + r + " state=" + state);
                    }
                    if (!(state == ActivityState.INITIALIZING || state == ActivityState.RESUMED)) {
                        Slog.e(TAG, "validateTop...: activity in front not resumed r=" + r + " state=" + state);
                    }
                }
            }
        }
    }

    private String lockTaskModeToString() {
        switch (this.mLockTaskModeState) {
            case 0:
                return "NONE";
            case 1:
                return "LOCKED";
            case 2:
                return "PINNED";
            default:
                return "unknown=" + this.mLockTaskModeState;
        }
    }

    public void dump(PrintWriter pw, String prefix) {
        int i;
        pw.print(prefix);
        pw.print("mFocusedStack=" + this.mFocusedStack);
        pw.print(" mLastFocusedStack=");
        pw.println(this.mLastFocusedStack);
        pw.print(prefix);
        pw.println("mSleepTimeout=" + this.mSleepTimeout);
        pw.print(prefix);
        pw.println("mCurTaskIdForUser=" + this.mCurTaskIdForUser);
        pw.print(prefix);
        pw.println("mUserStackInFront=" + this.mUserStackInFront);
        pw.print(prefix);
        pw.println("mActivityContainers=" + this.mActivityContainers);
        pw.print(prefix);
        pw.print("mLockTaskModeState=" + lockTaskModeToString());
        SparseArray<String[]> packages = this.mService.mLockTaskPackages;
        if (packages.size() > 0) {
            pw.print(prefix);
            pw.println("mLockTaskPackages (userId:packages)=");
            for (i = 0; i < packages.size(); i++) {
                pw.print(prefix);
                pw.print(prefix);
                pw.print(packages.keyAt(i));
                pw.print(":");
                pw.println(Arrays.toString((Object[]) packages.valueAt(i)));
            }
        }
        if (!this.mWaitingForActivityVisible.isEmpty()) {
            pw.print(prefix);
            pw.println("mWaitingForActivityVisible=");
            for (i = 0; i < this.mWaitingForActivityVisible.size(); i++) {
                pw.print(prefix);
                pw.print(prefix);
                ((WaitInfo) this.mWaitingForActivityVisible.get(i)).dump(pw, prefix);
            }
        }
        pw.println(" mLockTaskModeTasks" + this.mLockTaskModeTasks);
        this.mKeyguardController.dump(pw, prefix);
    }

    void dumpDisplayConfigs(PrintWriter pw, String prefix) {
        pw.print(prefix);
        pw.println("Display override configurations:");
        int displayCount = this.mActivityDisplays.size();
        for (int i = 0; i < displayCount; i++) {
            ActivityDisplay activityDisplay = (ActivityDisplay) this.mActivityDisplays.valueAt(i);
            pw.print(prefix);
            pw.print("  ");
            pw.print(activityDisplay.mDisplayId);
            pw.print(": ");
            pw.println(activityDisplay.getOverrideConfiguration());
        }
    }

    ArrayList<ActivityRecord> getDumpActivitiesLocked(String name, boolean dumpVisibleStacksOnly, boolean dumpFocusedStackOnly) {
        if (dumpFocusedStackOnly) {
            return this.mFocusedStack.getDumpActivitiesLocked(name);
        }
        ArrayList<ActivityRecord> activities = new ArrayList();
        int numDisplays = this.mActivityDisplays.size();
        for (int displayNdx = 0; displayNdx < numDisplays; displayNdx++) {
            ArrayList<ActivityStack> stacks = ((ActivityDisplay) this.mActivityDisplays.valueAt(displayNdx)).mStacks;
            for (int stackNdx = stacks.size() - 1; stackNdx >= 0; stackNdx--) {
                ActivityStack stack = (ActivityStack) stacks.get(stackNdx);
                if (!dumpVisibleStacksOnly || stack.shouldBeVisible(null) == 1) {
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

    boolean dumpActivitiesLocked(FileDescriptor fd, PrintWriter pw, boolean dumpAll, boolean dumpClient, String dumpPackage) {
        boolean printed = false;
        boolean needSep = false;
        for (int displayNdx = 0; displayNdx < this.mActivityDisplays.size(); displayNdx++) {
            ActivityDisplay activityDisplay = (ActivityDisplay) this.mActivityDisplays.valueAt(displayNdx);
            pw.print("Display #");
            pw.print(activityDisplay.mDisplayId);
            pw.println(" (activities from top to bottom):");
            ArrayList<ActivityStack> stacks = activityDisplay.mStacks;
            for (int stackNdx = stacks.size() - 1; stackNdx >= 0; stackNdx--) {
                ActivityStack stack = (ActivityStack) stacks.get(stackNdx);
                StringBuilder stringBuilder = new StringBuilder(128);
                stringBuilder.append("  Stack #");
                stringBuilder.append(stack.mStackId);
                stringBuilder.append(":");
                stringBuilder.append("\n");
                stringBuilder.append("  mFullscreen=").append(stack.mFullscreen);
                stringBuilder.append("\n");
                stringBuilder.append("  mBounds=").append(stack.mBounds);
                boolean printedStackHeader = stack.dumpActivitiesLocked(fd, pw, dumpAll, dumpClient, dumpPackage, needSep, stringBuilder.toString());
                printed |= printedStackHeader;
                if (!printedStackHeader) {
                    pw.println();
                    pw.println(stringBuilder);
                }
                printed |= dumpHistoryList(fd, pw, stack.mLRUActivities, "    ", "Run", false, dumpAll ^ 1, false, dumpPackage, true, "    Running activities (most recent first):", null);
                needSep = printed;
                if (printThisActivity(pw, stack.mPausingActivity, dumpPackage, printed, "    mPausingActivity: ")) {
                    printed = true;
                    needSep = false;
                }
                if (printThisActivity(pw, stack.mResumedActivity, dumpPackage, needSep, "    mResumedActivity: ")) {
                    printed = true;
                    needSep = false;
                }
                if (dumpAll) {
                    int printed2;
                    if (printThisActivity(pw, stack.mLastPausedActivity, dumpPackage, needSep, "    mLastPausedActivity: ")) {
                        printed2 = 1;
                        needSep = true;
                    }
                    printed = printed2 | printThisActivity(pw, stack.mLastNoHistoryActivity, dumpPackage, needSep, "    mLastNoHistoryActivity: ");
                }
                needSep = printed;
            }
        }
        return ((((printed | dumpHistoryList(fd, pw, this.mFinishingActivities, "  ", "Fin", false, dumpAll ^ 1, false, dumpPackage, true, "  Activities waiting to finish:", null)) | dumpHistoryList(fd, pw, this.mStoppingActivities, "  ", "Stop", false, dumpAll ^ 1, false, dumpPackage, true, "  Activities waiting to stop:", null)) | dumpHistoryList(fd, pw, this.mActivitiesWaitingForVisibleActivity, "  ", "Wait", false, dumpAll ^ 1, false, dumpPackage, true, "  Activities waiting for another to become visible:", null)) | dumpHistoryList(fd, pw, this.mGoingToSleepActivities, "  ", "Sleep", false, dumpAll ^ 1, false, dumpPackage, true, "  Activities waiting to sleep:", null)) | dumpHistoryList(fd, pw, this.mGoingToSleepActivities, "  ", "Sleep", false, dumpAll ^ 1, false, dumpPackage, true, "  Activities waiting to sleep:", null);
    }

    static boolean dumpHistoryList(FileDescriptor fd, PrintWriter pw, List<ActivityRecord> list, String prefix, String label, boolean complete, boolean brief, boolean client, String dumpPackage, boolean needNL, String header1, String header2) {
        TaskRecord lastTask = null;
        String innerPrefix = null;
        String[] args = null;
        boolean printed = false;
        for (int i = list.size() - 1; i >= 0; i--) {
            ActivityRecord r = (ActivityRecord) list.get(i);
            if (dumpPackage != null) {
                if ((dumpPackage.equals(r.packageName) ^ 1) != 0) {
                    continue;
                }
            }
            if (innerPrefix == null) {
                innerPrefix = prefix + "      ";
                args = new String[0];
            }
            printed = true;
            int full = !brief ? !complete ? r.isInHistory() ^ 1 : 1 : 0;
            if (needNL) {
                pw.println("");
                needNL = false;
            }
            if (header1 != null) {
                pw.println(header1);
                header1 = null;
            }
            if (header2 != null) {
                pw.println(header2);
                header2 = null;
            }
            if (lastTask != r.getTask()) {
                lastTask = r.getTask();
                pw.print(prefix);
                pw.print(full != 0 ? "* " : "  ");
                pw.println(lastTask);
                if (full != 0) {
                    lastTask.dump(pw, prefix + "  ");
                } else if (complete && lastTask.intent != null) {
                    pw.print(prefix);
                    pw.print("  ");
                    pw.println(lastTask.intent.toInsecureStringWithClip());
                }
            }
            pw.print(prefix);
            pw.print(full != 0 ? "  * " : "    ");
            pw.print(label);
            pw.print(" #");
            pw.print(i);
            pw.print(": ");
            pw.println(r);
            if (full != 0) {
                r.dump(pw, innerPrefix);
            } else if (complete) {
                pw.print(innerPrefix);
                pw.println(r.intent.toInsecureString());
                if (r.app != null) {
                    pw.print(innerPrefix);
                    pw.println(r.app);
                }
            }
            if (!(!client || r.app == null || r.app.thread == null)) {
                pw.flush();
                TransferPipe tp;
                try {
                    tp = new TransferPipe();
                    r.app.thread.dumpActivity(tp.getWriteFd(), r.appToken, innerPrefix, args);
                    tp.go(fd, 2000);
                    tp.kill();
                } catch (IOException e) {
                    pw.println(innerPrefix + "Failure while dumping the activity: " + e);
                } catch (RemoteException e2) {
                    pw.println(innerPrefix + "Got a RemoteException while dumping the activity");
                } catch (Throwable th) {
                    tp.kill();
                }
                needNL = true;
            }
        }
        return printed;
    }

    void scheduleIdleTimeoutLocked(ActivityRecord next) {
        if (ActivityManagerDebugConfig.DEBUG_IDLE) {
            Slog.d(TAG_IDLE, "scheduleIdleTimeoutLocked: Callers=" + Debug.getCallers(4));
        }
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(100, next), JobStatus.DEFAULT_TRIGGER_UPDATE_DELAY);
    }

    final void scheduleIdleLocked() {
        this.mHandler.sendEmptyMessage(101);
    }

    void removeTimeoutsForActivityLocked(ActivityRecord r) {
        if (ActivityManagerDebugConfig.DEBUG_IDLE) {
            Slog.d(TAG_IDLE, "removeTimeoutsForActivity: Callers=" + Debug.getCallers(4));
        }
        this.mHandler.removeMessages(100, r);
    }

    final void scheduleResumeTopActivities() {
        if (!this.mHandler.hasMessages(102)) {
            this.mHandler.sendEmptyMessage(102);
        }
    }

    void removeSleepTimeouts() {
        this.mSleepTimeout = false;
        this.mHandler.removeMessages(103);
    }

    final void scheduleSleepTimeout() {
        removeSleepTimeouts();
        this.mHandler.sendEmptyMessageDelayed(103, 5000);
    }

    public void onDisplayAdded(int displayId) {
        if (ActivityManagerDebugConfig.DEBUG_STACK) {
            Slog.v(TAG, "Display added displayId=" + displayId);
        }
        this.mHandler.sendMessage(this.mHandler.obtainMessage(105, displayId, 0));
    }

    public void onDisplayRemoved(int displayId) {
        if (ActivityManagerDebugConfig.DEBUG_STACK) {
            Slog.v(TAG, "Display removed displayId=" + displayId);
        }
        this.mHandler.sendMessage(this.mHandler.obtainMessage(107, displayId, 0));
    }

    public void onDisplayChanged(int displayId) {
        if (ActivityManagerDebugConfig.DEBUG_STACK) {
            Slog.v(TAG, "Display changed displayId=" + displayId);
        }
        this.mHandler.sendMessage(this.mHandler.obtainMessage(106, displayId, 0));
    }

    private void handleDisplayAdded(int displayId) {
        synchronized (this.mService) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                getActivityDisplayOrCreateLocked(displayId);
            } finally {
                ActivityManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    boolean isDisplayAdded(int displayId) {
        return getActivityDisplayOrCreateLocked(displayId) != null;
    }

    private ActivityDisplay getActivityDisplayOrCreateLocked(int displayId) {
        ActivityDisplay activityDisplay = (ActivityDisplay) this.mActivityDisplays.get(displayId);
        if (activityDisplay != null) {
            return activityDisplay;
        }
        if (this.mDisplayManager == null || this.mDisplayManager.getDisplay(displayId) == null) {
            return null;
        }
        activityDisplay = new ActivityDisplay(displayId);
        if (activityDisplay.mDisplay == null) {
            Slog.w(TAG, "Display " + displayId + " gone before initialization complete");
            return null;
        }
        this.mActivityDisplays.put(displayId, activityDisplay);
        calculateDefaultMinimalSizeOfResizeableTasks(activityDisplay);
        this.mWindowManager.onDisplayAdded(displayId);
        return activityDisplay;
    }

    public void reCalculateDefaultMinimalSizeOfResizeableTasks() {
        calculateDefaultMinimalSizeOfResizeableTasks(new ActivityDisplay(0));
    }

    private void calculateDefaultMinimalSizeOfResizeableTasks(ActivityDisplay display) {
        this.mDefaultMinSizeOfResizeableTask = this.mService.mContext.getResources().getDimensionPixelSize(17104998);
    }

    protected void handleDisplayRemoved(int displayId) {
        if (displayId == 0) {
            throw new IllegalArgumentException("Can't remove the primary display.");
        }
        synchronized (this.mService) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                ActivityDisplay activityDisplay = (ActivityDisplay) this.mActivityDisplays.get(displayId);
                if (activityDisplay != null) {
                    boolean destroyContentOnRemoval = activityDisplay.shouldDestroyContentOnRemove();
                    ArrayList<ActivityStack> stacks = activityDisplay.mStacks;
                    while (!stacks.isEmpty()) {
                        ActivityStack stack = (ActivityStack) stacks.get(0);
                        if (destroyContentOnRemoval) {
                            moveStackToDisplayLocked(stack.mStackId, 0, false);
                            stack.finishAllActivitiesLocked(true);
                        } else {
                            moveTasksToFullscreenStackLocked(stack.getStackId(), true);
                        }
                    }
                    this.mActivityDisplays.remove(displayId);
                    this.mWindowManager.onDisplayRemoved(displayId);
                }
            } finally {
                ActivityManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    private void handleDisplayChanged(int displayId) {
        synchronized (this.mService) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                ActivityDisplay activityDisplay = (ActivityDisplay) this.mActivityDisplays.get(displayId);
                this.mWindowManager.onDisplayChanged(displayId);
            } finally {
                ActivityManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    private StackInfo getStackInfoLocked(ActivityStack stack) {
        int indexOf;
        ActivityDisplay display = (ActivityDisplay) this.mActivityDisplays.get(stack.mDisplayId);
        StackInfo info = new StackInfo();
        stack.getWindowContainerBounds(info.bounds);
        if (HwPCUtils.isPcDynamicStack(stack.getStackId())) {
            info.displayId = stack.mDisplayId;
        } else {
            info.displayId = 0;
        }
        info.stackId = stack.mStackId;
        info.userId = stack.mCurrentUser;
        info.visible = stack.shouldBeVisible(null) == 1;
        if (display != null) {
            indexOf = display.mStacks.indexOf(stack);
        } else {
            indexOf = 0;
        }
        info.position = indexOf;
        ArrayList<TaskRecord> tasks = stack.getAllTasks();
        int numTasks = tasks.size();
        int[] taskIds = new int[numTasks];
        String[] taskNames = new String[numTasks];
        Rect[] taskBounds = new Rect[numTasks];
        int[] taskUserIds = new int[numTasks];
        for (int i = 0; i < numTasks; i++) {
            String flattenToString;
            TaskRecord task = (TaskRecord) tasks.get(i);
            taskIds[i] = task.taskId;
            if (task.origActivity != null) {
                flattenToString = task.origActivity.flattenToString();
            } else if (task.realActivity != null) {
                flattenToString = task.realActivity.flattenToString();
            } else if (task.getTopActivity() != null) {
                flattenToString = task.getTopActivity().packageName;
            } else {
                flattenToString = Shell.NIGHT_MODE_STR_UNKNOWN;
            }
            taskNames[i] = flattenToString;
            taskBounds[i] = new Rect();
            task.getWindowContainerBounds(taskBounds[i]);
            taskUserIds[i] = task.userId;
        }
        info.taskIds = taskIds;
        info.taskNames = taskNames;
        info.taskBounds = taskBounds;
        info.taskUserIds = taskUserIds;
        ActivityRecord top = stack.topRunningActivityLocked();
        info.topActivity = top != null ? top.intent.getComponent() : null;
        return info;
    }

    StackInfo getStackInfoLocked(int stackId) {
        ActivityStack stack = getStack(stackId);
        if (stack != null) {
            return getStackInfoLocked(stack);
        }
        return null;
    }

    ArrayList<StackInfo> getAllStackInfosLocked() {
        ArrayList<StackInfo> list = new ArrayList();
        for (int displayNdx = 0; displayNdx < this.mActivityDisplays.size(); displayNdx++) {
            ArrayList<ActivityStack> stacks = ((ActivityDisplay) this.mActivityDisplays.valueAt(displayNdx)).mStacks;
            for (int ndx = stacks.size() - 1; ndx >= 0; ndx--) {
                list.add(getStackInfoLocked((ActivityStack) stacks.get(ndx)));
            }
        }
        return list;
    }

    TaskRecord getLockedTaskLocked() {
        int top = this.mLockTaskModeTasks.size() - 1;
        if (top >= 0) {
            return (TaskRecord) this.mLockTaskModeTasks.get(top);
        }
        return null;
    }

    boolean isLockedTask(TaskRecord task) {
        return this.mLockTaskModeTasks.contains(task);
    }

    boolean isLastLockedTask(TaskRecord task) {
        return this.mLockTaskModeTasks.size() == 1 ? this.mLockTaskModeTasks.contains(task) : false;
    }

    void removeLockedTaskLocked(TaskRecord task) {
        if (this.mLockTaskModeTasks.remove(task)) {
            if (ActivityManagerDebugConfig.DEBUG_LOCKTASK) {
                Slog.w(TAG_LOCKTASK, "removeLockedTaskLocked: removed " + task);
            }
            if (this.mLockTaskModeTasks.isEmpty()) {
                if (ActivityManagerDebugConfig.DEBUG_LOCKTASK) {
                    Slog.d(TAG_LOCKTASK, "removeLockedTask: task=" + task + " last task, reverting locktask mode. Callers=" + Debug.getCallers(3));
                }
                Message lockTaskMsg = Message.obtain();
                lockTaskMsg.arg1 = task.userId;
                lockTaskMsg.what = 110;
                this.mHandler.sendMessage(lockTaskMsg);
            }
        }
    }

    void handleNonResizableTaskIfNeeded(TaskRecord task, int preferredStackId, int preferredDisplayId, int actualStackId) {
        handleNonResizableTaskIfNeeded(task, preferredStackId, preferredDisplayId, actualStackId, false);
    }

    private void handleNonResizableTaskIfNeeded(TaskRecord task, int preferredStackId, int preferredDisplayId, int actualStackId, boolean forceNonResizable) {
        int isSecondaryDisplayPreferred;
        if (preferredDisplayId == 0 || preferredDisplayId == -1) {
            isSecondaryDisplayPreferred = StackId.isDynamicStack(preferredStackId);
        } else {
            isSecondaryDisplayPreferred = 1;
        }
        if (task != null) {
            if ((isStackDockedInEffect(actualStackId) || preferredStackId == 3 || (isSecondaryDisplayPreferred ^ 1) == 0) && !task.isHomeTask()) {
                boolean launchOnSecondaryDisplayFailed;
                if (isSecondaryDisplayPreferred != 0) {
                    int actualDisplayId = task.getStack().mDisplayId;
                    if (task.canBeLaunchedOnDisplay(actualDisplayId)) {
                        launchOnSecondaryDisplayFailed = actualDisplayId != 0 ? preferredDisplayId != -1 ? preferredDisplayId != actualDisplayId : false : true;
                    } else {
                        this.mService.moveTaskToStack(task.taskId, 1, true);
                        launchOnSecondaryDisplayFailed = true;
                    }
                } else {
                    launchOnSecondaryDisplayFailed = false;
                }
                ActivityRecord topActivity = task.getTopActivity();
                boolean isInMultiWinBlackList = false;
                if (!(this.mService.mCustAms == null || topActivity == null)) {
                    isInMultiWinBlackList = this.mService.mCustAms.isInMultiWinBlackList(topActivity.appInfo.packageName, this.mService.mContext.getContentResolver());
                }
                if (launchOnSecondaryDisplayFailed || (task.supportsSplitScreen() ^ 1) != 0 || forceNonResizable || ((topActivity != null && "com.huawei.systemmanager".equals(topActivity.appInfo.packageName)) || isInMultiWinBlackList)) {
                    boolean z;
                    if (launchOnSecondaryDisplayFailed) {
                        this.mService.mTaskChangeNotificationController.notifyActivityLaunchOnSecondaryDisplayFailed();
                    } else if (topActivity == null || (HwPCUtils.isPcDynamicStack(topActivity.getStackId()) ^ 1) != 0) {
                        this.mService.mTaskChangeNotificationController.notifyActivityDismissingDockedStack();
                    }
                    if (actualStackId == 3) {
                        z = true;
                    } else {
                        z = false;
                    }
                    moveTasksToFullscreenStackLocked(3, z);
                } else if (topActivity != null && topActivity.isNonResizableOrForcedResizable() && (topActivity.noDisplay ^ 1) != 0 && !HwPCUtils.isPcDynamicStack(topActivity.getStackId())) {
                    int reason;
                    String packageName = topActivity.appInfo.packageName;
                    if (isSecondaryDisplayPreferred != 0) {
                        reason = 2;
                    } else {
                        reason = 1;
                    }
                    ApplicationInfo info = this.mService.getPackageManagerInternalLocked().getApplicationInfo(packageName, 0, Process.myUid(), task.userId);
                    if (info == null || (info.flags & 1) == 0) {
                        this.mService.mTaskChangeNotificationController.notifyActivityForcedResizable(task.taskId, reason, packageName);
                    }
                }
            }
        }
    }

    void showLockTaskToast() {
        if (this.mLockTaskNotify != null) {
            this.mLockTaskNotify.showToast(this.mLockTaskModeState);
        }
    }

    void showLockTaskEscapeMessageLocked(TaskRecord task) {
        if (this.mLockTaskModeTasks.contains(task)) {
            this.mHandler.sendEmptyMessage(113);
        }
    }

    void setLockTaskModeLocked(TaskRecord task, int lockTaskModeState, String reason, boolean andResume) {
        if (task == null) {
            TaskRecord lockedTask = getLockedTaskLocked();
            if (lockedTask != null) {
                removeLockedTaskLocked(lockedTask);
                if (!this.mLockTaskModeTasks.isEmpty()) {
                    if (ActivityManagerDebugConfig.DEBUG_LOCKTASK) {
                        Slog.w(TAG_LOCKTASK, "setLockTaskModeLocked: Tasks remaining, can't unlock");
                    }
                    lockedTask.performClearTaskLocked();
                    resumeFocusedStackTopActivityLocked();
                    return;
                }
            }
            if (ActivityManagerDebugConfig.DEBUG_LOCKTASK) {
                Slog.w(TAG_LOCKTASK, "setLockTaskModeLocked: No tasks to unlock. Callers=" + Debug.getCallers(4));
            }
        } else if (task.mLockTaskAuth == 0) {
            if (ActivityManagerDebugConfig.DEBUG_LOCKTASK) {
                Slog.w(TAG_LOCKTASK, "setLockTaskModeLocked: Can't lock due to auth");
            }
        } else if (isLockTaskModeViolation(task)) {
            Slog.e(TAG_LOCKTASK, "setLockTaskMode: Attempt to start an unauthorized lock task.");
        } else {
            if (this.mLockTaskModeTasks.isEmpty()) {
                Message lockTaskMsg = Message.obtain();
                lockTaskMsg.obj = task.intent.getComponent().getPackageName();
                lockTaskMsg.arg1 = task.userId;
                lockTaskMsg.what = 109;
                lockTaskMsg.arg2 = lockTaskModeState;
                this.mHandler.sendMessage(lockTaskMsg);
            }
            if (ActivityManagerDebugConfig.DEBUG_LOCKTASK) {
                Slog.w(TAG_LOCKTASK, "setLockTaskModeLocked: Locking to " + task + " Callers=" + Debug.getCallers(4));
            }
            this.mLockTaskModeTasks.remove(task);
            this.mLockTaskModeTasks.add(task);
            if (task.mLockTaskUid == -1) {
                task.mLockTaskUid = task.effectiveUid;
            }
            if (andResume) {
                findTaskToMoveToFrontLocked(task, 0, null, reason, lockTaskModeState != 0);
                resumeFocusedStackTopActivityLocked();
                this.mWindowManager.executeAppTransition();
            } else if (lockTaskModeState != 0) {
                handleNonResizableTaskIfNeeded(task, -1, 0, task.getStackId(), true);
            }
        }
    }

    boolean isLockTaskModeViolation(TaskRecord task) {
        return isLockTaskModeViolation(task, false);
    }

    boolean isLockTaskModeViolation(TaskRecord task, boolean isNewClearTask) {
        if (getLockedTaskLocked() == task && (isNewClearTask ^ 1) != 0) {
            return false;
        }
        int lockTaskAuth = task.mLockTaskAuth;
        switch (lockTaskAuth) {
            case 0:
                return this.mLockTaskModeTasks.isEmpty() ^ 1;
            case 1:
                return this.mLockTaskModeTasks.isEmpty() ^ 1;
            case 2:
            case 3:
            case 4:
                return false;
            default:
                Slog.w(TAG, "isLockTaskModeViolation: invalid lockTaskAuth value=" + lockTaskAuth);
                return true;
        }
    }

    void onLockTaskPackagesUpdatedLocked() {
        boolean didSomething = false;
        for (int taskNdx = this.mLockTaskModeTasks.size() - 1; taskNdx >= 0; taskNdx--) {
            TaskRecord lockedTask = (TaskRecord) this.mLockTaskModeTasks.get(taskNdx);
            boolean wasWhitelisted = lockedTask.mLockTaskAuth != 2 ? lockedTask.mLockTaskAuth == 3 : true;
            lockedTask.setLockTaskAuth();
            boolean isWhitelisted = lockedTask.mLockTaskAuth != 2 ? lockedTask.mLockTaskAuth == 3 : true;
            if (wasWhitelisted && (isWhitelisted ^ 1) != 0) {
                if (ActivityManagerDebugConfig.DEBUG_LOCKTASK) {
                    Slog.d(TAG_LOCKTASK, "onLockTaskPackagesUpdated: removing " + lockedTask + " mLockTaskAuth=" + lockedTask.lockTaskAuthToString());
                }
                removeLockedTaskLocked(lockedTask);
                lockedTask.performClearTaskLocked();
                didSomething = true;
            }
        }
        for (int displayNdx = this.mActivityDisplays.size() - 1; displayNdx >= 0; displayNdx--) {
            ArrayList<ActivityStack> stacks = ((ActivityDisplay) this.mActivityDisplays.valueAt(displayNdx)).mStacks;
            for (int stackNdx = stacks.size() - 1; stackNdx >= 0; stackNdx--) {
                ((ActivityStack) stacks.get(stackNdx)).onLockTaskPackagesUpdatedLocked();
            }
        }
        ActivityRecord r = topRunningActivityLocked();
        TaskRecord task = r != null ? r.getTask() : null;
        if (this.mLockTaskModeTasks.isEmpty() && task != null && task.mLockTaskAuth == 2) {
            if (ActivityManagerDebugConfig.DEBUG_LOCKTASK) {
                Slog.d(TAG_LOCKTASK, "onLockTaskPackagesUpdated: starting new locktask task=" + task);
            }
            setLockTaskModeLocked(task, 1, "package updated", false);
            didSomething = true;
        }
        if (didSomething) {
            resumeFocusedStackTopActivityLocked();
        }
    }

    int getLockTaskModeState() {
        return this.mLockTaskModeState;
    }

    void activityRelaunchedLocked(IBinder token) {
        this.mWindowManager.notifyAppRelaunchingFinished(token);
        if (this.mService.isSleepingOrShuttingDownLocked()) {
            ActivityRecord r = ActivityRecord.isInStackLocked(token);
            if (r != null) {
                r.setSleeping(true, true);
            }
        }
    }

    void activityRelaunchingLocked(ActivityRecord r) {
        this.mWindowManager.notifyAppRelaunching(r.appToken);
    }

    void logStackState() {
        this.mActivityMetricsLogger.logWindowState();
    }

    void scheduleUpdateMultiWindowMode(TaskRecord task) {
        if (!task.getStack().deferScheduleMultiWindowModeChanged()) {
            for (int i = task.mActivities.size() - 1; i >= 0; i--) {
                ActivityRecord r = (ActivityRecord) task.mActivities.get(i);
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

    void scheduleUpdatePictureInPictureModeIfNeeded(TaskRecord task, ActivityStack prevStack) {
        ActivityStack stack = task.getStack();
        if (prevStack != null && prevStack != stack && (prevStack.mStackId == 4 || stack.mStackId == 4)) {
            scheduleUpdatePictureInPictureModeIfNeeded(task, stack.mBounds, false);
        }
    }

    void scheduleUpdatePictureInPictureModeIfNeeded(TaskRecord task, Rect targetStackBounds, boolean immediate) {
        int i;
        ActivityRecord r;
        if (immediate) {
            this.mHandler.removeMessages(115);
            for (i = task.mActivities.size() - 1; i >= 0; i--) {
                r = (ActivityRecord) task.mActivities.get(i);
                if (!(r.app == null || r.app.thread == null)) {
                    r.updatePictureInPictureMode(targetStackBounds);
                }
            }
            return;
        }
        for (i = task.mActivities.size() - 1; i >= 0; i--) {
            r = (ActivityRecord) task.mActivities.get(i);
            if (!(r.app == null || r.app.thread == null)) {
                this.mPipModeChangedActivities.add(r);
            }
        }
        this.mPipModeChangedTargetStackBounds = targetStackBounds;
        if (!this.mHandler.hasMessages(115)) {
            this.mHandler.sendEmptyMessage(115);
        }
    }

    void setDockedStackMinimized(boolean minimized) {
        this.mIsDockMinimized = minimized;
    }

    ActivityStack findStackBehind(ActivityStack stack) {
        ActivityDisplay display = (ActivityDisplay) this.mActivityDisplays.get(0);
        if (display == null) {
            return null;
        }
        ArrayList<ActivityStack> stacks = display.mStacks;
        int i = stacks.size() - 1;
        while (i >= 0) {
            if (stacks.get(i) == stack && i > 0) {
                return (ActivityStack) stacks.get(i - 1);
            }
            i--;
        }
        throw new IllegalStateException("Failed to find a stack behind stack=" + stack + " in=" + stacks);
    }

    private void setResizingDuringAnimation(TaskRecord task) {
        this.mResizingTasksDuringAnimation.add(Integer.valueOf(task.taskId));
        task.setTaskDockedResizing(true);
    }

    final int startActivityFromRecentsInner(int taskId, Bundle bOptions) {
        ActivityOptions activityOptions;
        if (bOptions != null) {
            ActivityOptions activityOptions2 = new ActivityOptions(bOptions);
        } else {
            activityOptions = null;
        }
        int launchStackId = activityOptions != null ? activityOptions.getLaunchStackId() : -1;
        if (StackId.isHomeOrRecentsStack(launchStackId)) {
            throw new IllegalArgumentException("startActivityFromRecentsInner: Task " + taskId + " can't be launch in the home/recents stack.");
        }
        this.mWindowManager.deferSurfaceLayout();
        if (launchStackId == 3) {
            try {
                this.mWindowManager.setDockedStackCreateState(activityOptions.getDockCreateMode(), null);
                deferUpdateBounds(5);
                this.mWindowManager.prepareAppTransition(19, false);
            } catch (Throwable th) {
                this.mWindowManager.continueSurfaceLayout();
            }
        }
        TaskRecord task = anyTaskForIdLocked(taskId, 2, launchStackId);
        if (task == null) {
            continueUpdateBounds(5);
            this.mWindowManager.executeAppTransition();
            throw new IllegalArgumentException("startActivityFromRecentsInner: Task " + taskId + " not found.");
        }
        ActivityStack focusedStack = getFocusedStack();
        ActivityRecord sourceRecord = focusedStack != null ? focusedStack.topActivity() : null;
        if (!(launchStackId == -1 || task.getStackId() == launchStackId)) {
            task.reparent(launchStackId, true, 0, true, true, "startActivityFromRecents");
        }
        if (this.mService.mUserController.shouldConfirmCredentials(task.userId) || task.getRootActivity() == null) {
            int callingUid = task.mCallingUid;
            String callingPackage = task.mCallingPackage;
            Intent intent = task.intent;
            intent.addFlags(DumpState.DUMP_DEXOPT);
            int result = this.mService.startActivityInPackage(callingUid, callingPackage, intent, null, null, null, 0, 0, bOptions, task.userId, null, task, "startActivityFromRecents");
            if (launchStackId == 3) {
                setResizingDuringAnimation(task);
            }
            this.mWindowManager.continueSurfaceLayout();
            return result;
        }
        this.mService.mActivityStarter.sendPowerHintForLaunchStartIfNeeded(true);
        Flog.i(101, "task.userId =" + task.userId + ", task.taskId = " + task.taskId + ", task.getRootActivity() = " + task.getRootActivity() + ", task.getTopActivity() = " + task.getTopActivity());
        this.mActivityMetricsLogger.notifyActivityLaunching();
        this.mService.moveTaskToFrontLocked(task.taskId, 0, bOptions, true);
        this.mActivityMetricsLogger.notifyActivityLaunched(2, task.getTopActivity());
        if (launchStackId == 3) {
            setResizingDuringAnimation(task);
        }
        this.mService.mActivityStarter.postStartActivityProcessing(task.getTopActivity(), 2, sourceRecord != null ? sourceRecord.getTask().getStackId() : -1, sourceRecord, task.getStack());
        if (HwPCUtils.isPcCastModeInServer()) {
            int launchDisplayId = 0;
            if (!(activityOptions == null || activityOptions.getLaunchDisplayId() == -1)) {
                launchDisplayId = activityOptions.getLaunchDisplayId();
            }
            if (!(task.getStack() == null || task.getStack().mDisplayId == launchDisplayId)) {
                showToast(launchDisplayId);
            }
        }
        this.mWindowManager.continueSurfaceLayout();
        return 2;
    }

    List<IBinder> getTopVisibleActivities() {
        ArrayList<IBinder> topActivityTokens = new ArrayList();
        for (int i = this.mActivityDisplays.size() - 1; i >= 0; i--) {
            ActivityDisplay display = (ActivityDisplay) this.mActivityDisplays.valueAt(i);
            for (int j = display.mStacks.size() - 1; j >= 0; j--) {
                ActivityStack stack = (ActivityStack) display.mStacks.get(j);
                if (stack.shouldBeVisible(null) == 1) {
                    ActivityRecord top = stack.topActivity();
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

    protected void showToast(int displayId) {
    }

    protected boolean notKillProcessWhenRemoveTask() {
        return true;
    }
}
