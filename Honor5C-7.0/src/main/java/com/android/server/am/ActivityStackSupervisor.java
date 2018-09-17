package com.android.server.am;

import android.app.ActivityManager.RunningTaskInfo;
import android.app.ActivityManager.StackId;
import android.app.ActivityManager.StackInfo;
import android.app.ActivityOptions;
import android.app.AppGlobals;
import android.app.AppOpsManager;
import android.app.HwCustNonHardwareAcceleratedPackagesManager;
import android.app.IActivityContainer;
import android.app.IActivityContainer.Stub;
import android.app.IActivityContainerCallback;
import android.app.IActivityManager.WaitResult;
import android.app.ProfilerInfo;
import android.app.admin.IDevicePolicyManager;
import android.common.HwFrameworkFactory;
import android.content.ComponentName;
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
import android.hardware.display.VirtualDisplay;
import android.hardware.input.InputManagerInternal;
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
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
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
import android.util.Jlog;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.Display;
import android.view.DisplayInfo;
import android.view.InputEvent;
import android.view.Surface;
import com.android.internal.os.TransferPipe;
import com.android.internal.statusbar.IStatusBarService;
import com.android.internal.util.ArrayUtils;
import com.android.internal.widget.LockPatternUtils;
import com.android.server.HwServiceFactory;
import com.android.server.LocalServices;
import com.android.server.job.controllers.JobStatus;
import com.android.server.usb.UsbAudioDevice;
import com.android.server.wm.WindowManagerService;
import com.android.server.wm.WindowManagerService.H;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class ActivityStackSupervisor extends AbsActivityStackSupervisor implements DisplayListener {
    private static final ArrayMap<String, String> ACTION_TO_RUNTIME_PERMISSION = null;
    private static final int ACTIVITY_RESTRICTION_APPOP = 2;
    private static final int ACTIVITY_RESTRICTION_NONE = 0;
    private static final int ACTIVITY_RESTRICTION_PERMISSION = 1;
    static final int CONTAINER_CALLBACK_TASK_LIST_EMPTY = 111;
    static final int CONTAINER_CALLBACK_VISIBILITY = 108;
    static final boolean CREATE_IF_NEEDED = true;
    static final boolean DEFER_RESUME = true;
    private static final int FIT_WITHIN_BOUNDS_DIVIDER = 3;
    static final boolean FORCE_FOCUS = true;
    static final int HANDLE_DISPLAY_ADDED = 105;
    static final int HANDLE_DISPLAY_CHANGED = 106;
    static final int HANDLE_DISPLAY_REMOVED = 107;
    static final int IDLE_NOW_MSG = 101;
    static final int IDLE_TIMEOUT = 10000;
    static final int IDLE_TIMEOUT_MSG = 100;
    static final int LAUNCH_TASK_BEHIND_COMPLETE = 112;
    static final int LAUNCH_TIMEOUT = 10000;
    static final int LAUNCH_TIMEOUT_MSG = 104;
    static final int LOCK_TASK_END_MSG = 110;
    static final int LOCK_TASK_START_MSG = 109;
    private static final String LOCK_TASK_TAG = "Lock-to-App";
    private static final int MAX_TASK_IDS_PER_USER = 100000;
    static final boolean MOVING = true;
    static final boolean ON_TOP = true;
    static final boolean PRESERVE_WINDOWS = true;
    static final int REPORT_MULTI_WINDOW_MODE_CHANGED_MSG = 114;
    static final int REPORT_PIP_MODE_CHANGED_MSG = 115;
    static final boolean RESTORE_FROM_RECENTS = true;
    static final int RESUME_TOP_ACTIVITY_MSG = 102;
    static final int SHOW_LOCK_TASK_ESCAPE_MESSAGE_MSG = 113;
    static final int SLEEP_TIMEOUT = 5000;
    static final int SLEEP_TIMEOUT_MSG = 103;
    private static final String TAG = null;
    private static final String TAG_CONTAINERS = null;
    private static final String TAG_IDLE = null;
    private static final String TAG_LOCKTASK = null;
    private static final String TAG_PAUSE = null;
    private static final String TAG_RECENTS = null;
    private static final String TAG_RELEASE = null;
    private static final String TAG_STACK = null;
    private static final String TAG_STATES = null;
    private static final String TAG_SWITCH = null;
    static final String TAG_TASKS = null;
    private static final String TAG_VISIBLE_BEHIND = null;
    static final boolean VALIDATE_WAKE_LOCK_CALLER = false;
    private static final String VIRTUAL_DISPLAY_BASE_NAME = "ActivityViewVirtualDisplay";
    boolean inResumeTopActivity;
    public int[] lBoostCpuParamVal;
    public int[] lBoostPackParamVal;
    public int lBoostTimeOut;
    public int lDisPackTimeOut;
    private SparseArray<ActivityContainer> mActivityContainers;
    protected final SparseArray<ActivityDisplay> mActivityDisplays;
    String mActivityLaunchTrack;
    final ActivityMetricsLogger mActivityMetricsLogger;
    private boolean mAllowDockedStackResize;
    private HwSysResource mAppResource;
    boolean mAppVisibilitiesChangedSinceLastPause;
    private final SparseIntArray mCurTaskIdForUser;
    int mCurrentUser;
    int mDefaultMinSizeOfResizeableTask;
    private IDevicePolicyManager mDevicePolicyManager;
    DisplayManager mDisplayManager;
    final ArrayList<ActivityRecord> mFinishingActivities;
    ActivityStack mFocusedStack;
    WakeLock mGoingToSleep;
    final ArrayList<ActivityRecord> mGoingToSleepActivities;
    final ActivityStackSupervisorHandler mHandler;
    ActivityStack mHomeStack;
    InputManagerInternal mInputManagerInternal;
    boolean mIsDockMinimized;
    public boolean mIsPerfBoostEnabled;
    public boolean mIsperfDisablepackingEnable;
    private ActivityStack mLastFocusedStack;
    WakeLock mLaunchingActivity;
    private int mLockTaskModeState;
    ArrayList<TaskRecord> mLockTaskModeTasks;
    private LockTaskNotify mLockTaskNotify;
    final ArrayList<ActivityRecord> mMultiWindowModeChangedActivities;
    private int mNextFreeStackId;
    public BoostFramework mPerfBoost;
    public BoostFramework mPerfPack;
    public BoostFramework mPerf_iop;
    final ArrayList<ActivityRecord> mPipModeChangedActivities;
    private RecentTasks mRecentTasks;
    private final ResizeDockedStackTimeout mResizeDockedStackTimeout;
    private final ArraySet<Integer> mResizingTasksDuringAnimation;
    final ActivityManagerService mService;
    boolean mSleepTimeout;
    final ArrayList<UserState> mStartingUsers;
    private IStatusBarService mStatusBarService;
    final ArrayList<ActivityRecord> mStoppingActivities;
    private boolean mTaskLayersChanged;
    private final SparseArray<Rect> mTmpBounds;
    private final SparseArray<Configuration> mTmpConfigs;
    private final FindTaskResult mTmpFindTaskResult;
    private final SparseArray<Rect> mTmpInsetBounds;
    private IBinder mToken;
    boolean mUserLeaving;
    SparseIntArray mUserStackInFront;
    final ArrayList<WaitResult> mWaitingActivityLaunched;
    final ArrayList<WaitResult> mWaitingActivityVisible;
    final ArrayList<ActivityRecord> mWaitingVisibleActivities;
    WindowManagerService mWindowManager;
    private final Rect tempRect;
    private final Rect tempRect2;

    public class ActivityContainer extends Stub {
        static final int CONTAINER_STATE_FINISHING = 2;
        static final int CONTAINER_STATE_HAS_SURFACE = 0;
        static final int CONTAINER_STATE_NO_SURFACE = 1;
        static final int FORCE_NEW_TASK_FLAGS = 402718720;
        ActivityDisplay mActivityDisplay;
        IActivityContainerCallback mCallback;
        int mContainerState;
        String mIdString;
        ActivityRecord mParentActivity;
        final ActivityStack mStack;
        final int mStackId;
        boolean mVisible;

        ActivityContainer(int stackId) {
            this.mCallback = null;
            this.mParentActivity = null;
            this.mVisible = ActivityStackSupervisor.RESTORE_FROM_RECENTS;
            this.mContainerState = CONTAINER_STATE_HAS_SURFACE;
            synchronized (ActivityStackSupervisor.this.mService) {
                try {
                    ActivityManagerService.boostPriorityForLockedSection();
                    this.mStackId = stackId;
                    this.mStack = HwServiceFactory.createActivityStack(this, ActivityStackSupervisor.this.mRecentTasks);
                    this.mIdString = "ActivtyContainer{" + this.mStackId + "}";
                    if (ActivityManagerDebugConfig.DEBUG_STACK) {
                        Slog.d(ActivityStackSupervisor.TAG_STACK, "Creating " + this);
                    }
                } finally {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        void attachToDisplayLocked(ActivityDisplay activityDisplay, boolean onTop) {
            if (ActivityManagerDebugConfig.DEBUG_STACK) {
                Slog.d(ActivityStackSupervisor.TAG_STACK, "attachToDisplayLocked: " + this + " to display=" + activityDisplay + " onTop=" + onTop);
            }
            this.mActivityDisplay = activityDisplay;
            this.mStack.attachDisplay(activityDisplay, onTop);
            activityDisplay.attachActivities(this.mStack, onTop);
        }

        public void attachToDisplay(int displayId) {
            synchronized (ActivityStackSupervisor.this.mService) {
                try {
                    ActivityManagerService.boostPriorityForLockedSection();
                    ActivityDisplay activityDisplay = (ActivityDisplay) ActivityStackSupervisor.this.mActivityDisplays.get(displayId);
                    if (activityDisplay == null) {
                        return;
                    }
                    attachToDisplayLocked(activityDisplay, ActivityStackSupervisor.RESTORE_FROM_RECENTS);
                    ActivityManagerService.resetPriorityAfterLockedSection();
                } finally {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        public int getDisplayId() {
            synchronized (ActivityStackSupervisor.this.mService) {
                try {
                    ActivityManagerService.boostPriorityForLockedSection();
                    if (this.mActivityDisplay != null) {
                        int i = this.mActivityDisplay.mDisplayId;
                        return i;
                    }
                    ActivityManagerService.resetPriorityAfterLockedSection();
                    return -1;
                } finally {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                }
            }
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
                        boolean injectInputEvent = ActivityStackSupervisor.this.mInputManagerInternal.injectInputEvent(event, this.mActivityDisplay.mDisplayId, CONTAINER_STATE_HAS_SURFACE);
                        ActivityManagerService.resetPriorityAfterLockedSection();
                        Binder.restoreCallingIdentity(origId);
                        return injectInputEvent;
                    }
                    ActivityManagerService.resetPriorityAfterLockedSection();
                    Binder.restoreCallingIdentity(origId);
                    return ActivityStackSupervisor.VALIDATE_WAKE_LOCK_CALLER;
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
                    if (this.mContainerState == CONTAINER_STATE_FINISHING) {
                        ActivityManagerService.resetPriorityAfterLockedSection();
                        return;
                    }
                    this.mContainerState = CONTAINER_STATE_FINISHING;
                    origId = Binder.clearCallingIdentity();
                    this.mStack.finishAllActivitiesLocked(ActivityStackSupervisor.VALIDATE_WAKE_LOCK_CALLER);
                    ActivityStackSupervisor.this.mService.mActivityStarter.removePendingActivityLaunchesLocked(this.mStack);
                    Binder.restoreCallingIdentity(origId);
                    ActivityManagerService.resetPriorityAfterLockedSection();
                } catch (Throwable th) {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        protected void detachLocked() {
            if (ActivityManagerDebugConfig.DEBUG_STACK) {
                Slog.d(ActivityStackSupervisor.TAG_STACK, "detachLocked: " + this + " from display=" + this.mActivityDisplay + " Callers=" + Debug.getCallers(CONTAINER_STATE_FINISHING));
            }
            if (this.mActivityDisplay != null) {
                this.mActivityDisplay.detachActivitiesLocked(this.mStack);
                this.mActivityDisplay = null;
                this.mStack.detachDisplay();
            }
        }

        public final int startActivity(Intent intent) {
            return ActivityStackSupervisor.this.mService.startActivity(intent, this);
        }

        public final int startActivityIntentSender(IIntentSender intentSender) throws TransactionTooLargeException {
            ActivityStackSupervisor.this.mService.enforceNotIsolatedCaller("ActivityContainer.startActivityIntentSender");
            if (intentSender instanceof PendingIntentRecord) {
                PendingIntentRecord pendingIntent = (PendingIntentRecord) intentSender;
                checkEmbeddedAllowedInner(ActivityStackSupervisor.this.mService.mUserController.handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), ActivityStackSupervisor.this.mCurrentUser, ActivityStackSupervisor.VALIDATE_WAKE_LOCK_CALLER, CONTAINER_STATE_FINISHING, "ActivityContainer", null), pendingIntent.key.requestIntent, pendingIntent.key.requestResolvedType);
                return pendingIntent.sendInner(CONTAINER_STATE_HAS_SURFACE, null, null, null, null, null, null, CONTAINER_STATE_HAS_SURFACE, FORCE_NEW_TASK_FLAGS, FORCE_NEW_TASK_FLAGS, null, this);
            }
            throw new IllegalArgumentException("Bad PendingIntent object");
        }

        void checkEmbeddedAllowedInner(int userId, Intent intent, String resolvedType) {
            ActivityInfo aInfo = ActivityStackSupervisor.this.resolveActivity(intent, resolvedType, CONTAINER_STATE_HAS_SURFACE, null, userId);
            if (aInfo != null && (aInfo.flags & UsbAudioDevice.kAudioDeviceMeta_Alsa) == 0) {
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
            return this.mActivityDisplay != null ? ActivityStackSupervisor.RESTORE_FROM_RECENTS : ActivityStackSupervisor.VALIDATE_WAKE_LOCK_CALLER;
        }

        void setVisible(boolean visible) {
            if (this.mVisible != visible) {
                this.mVisible = visible;
                if (this.mCallback != null) {
                    int i;
                    ActivityStackSupervisorHandler activityStackSupervisorHandler = ActivityStackSupervisor.this.mHandler;
                    if (visible) {
                        i = CONTAINER_STATE_NO_SURFACE;
                    } else {
                        i = CONTAINER_STATE_HAS_SURFACE;
                    }
                    activityStackSupervisorHandler.obtainMessage(ActivityStackSupervisor.CONTAINER_CALLBACK_VISIBILITY, i, CONTAINER_STATE_HAS_SURFACE, this).sendToTarget();
                }
            }
        }

        void setDrawn() {
        }

        boolean isEligibleForNewTasks() {
            return ActivityStackSupervisor.RESTORE_FROM_RECENTS;
        }

        void onTaskListEmptyLocked() {
            detachLocked();
            ActivityStackSupervisor.this.deleteActivityContainer(this);
            ActivityStackSupervisor.this.mHandler.obtainMessage(ActivityStackSupervisor.CONTAINER_CALLBACK_TASK_LIST_EMPTY, this).sendToTarget();
        }

        public String toString() {
            return this.mIdString + (this.mActivityDisplay == null ? "N" : "A");
        }
    }

    class ActivityDisplay {
        Display mDisplay;
        int mDisplayId;
        DisplayInfo mDisplayInfo;
        final ArrayList<ActivityStack> mStacks;
        ActivityRecord mVisibleBehindActivity;

        ActivityDisplay() {
            this.mDisplayInfo = new DisplayInfo();
            this.mStacks = new ArrayList();
        }

        ActivityDisplay(int displayId) {
            this.mDisplayInfo = new DisplayInfo();
            this.mStacks = new ArrayList();
            Display display = ActivityStackSupervisor.this.mDisplayManager.getDisplay(displayId);
            if (display != null) {
                init(display);
            }
        }

        void init(Display display) {
            this.mDisplay = display;
            this.mDisplayId = display.getDisplayId();
            this.mDisplay.getDisplayInfo(this.mDisplayInfo);
        }

        void attachActivities(ActivityStack stack, boolean onTop) {
            if (ActivityManagerDebugConfig.DEBUG_STACK) {
                Slog.v(ActivityStackSupervisor.TAG_STACK, "attachActivities: attaching " + stack + " to displayId=" + this.mDisplayId + " onTop=" + onTop);
            }
            if (onTop) {
                this.mStacks.add(stack);
            } else {
                this.mStacks.add(ActivityStackSupervisor.ACTIVITY_RESTRICTION_NONE, stack);
            }
        }

        void detachActivitiesLocked(ActivityStack stack) {
            if (ActivityManagerDebugConfig.DEBUG_STACK) {
                Slog.v(ActivityStackSupervisor.TAG_STACK, "detachActivitiesLocked: detaching " + stack + " from displayId=" + this.mDisplayId);
            }
            this.mStacks.remove(stack);
        }

        void setVisibleBehindActivity(ActivityRecord r) {
            this.mVisibleBehindActivity = r;
        }

        boolean hasVisibleBehindActivity() {
            return this.mVisibleBehindActivity != null ? ActivityStackSupervisor.RESTORE_FROM_RECENTS : ActivityStackSupervisor.VALIDATE_WAKE_LOCK_CALLER;
        }

        public String toString() {
            return "ActivityDisplay={" + this.mDisplayId + " numStacks=" + this.mStacks.size() + "}";
        }
    }

    private final class ActivityStackSupervisorHandler extends Handler {
        public ActivityStackSupervisorHandler(Looper looper) {
            super(looper);
        }

        void activityIdleInternal(ActivityRecord r) {
            IBinder iBinder = null;
            synchronized (ActivityStackSupervisor.this.mService) {
                try {
                    ActivityManagerService.boostPriorityForLockedSection();
                    ActivityStackSupervisor activityStackSupervisor = ActivityStackSupervisor.this;
                    if (r != null) {
                        iBinder = r.appToken;
                    }
                    activityStackSupervisor.activityIdleInternalLocked(iBinder, ActivityStackSupervisor.RESTORE_FROM_RECENTS, null);
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
                case ActivityStackSupervisor.IDLE_TIMEOUT_MSG /*100*/:
                    if (ActivityManagerDebugConfig.DEBUG_IDLE) {
                        Slog.d(ActivityStackSupervisor.TAG_IDLE, "handleMessage: IDLE_TIMEOUT_MSG: r=" + msg.obj);
                    }
                    if (!ActivityStackSupervisor.this.mService.mDidDexOpt) {
                        activityIdleInternal((ActivityRecord) msg.obj);
                        break;
                    }
                    ActivityStackSupervisor.this.mService.mDidDexOpt = ActivityStackSupervisor.VALIDATE_WAKE_LOCK_CALLER;
                    Message nmsg = ActivityStackSupervisor.this.mHandler.obtainMessage(ActivityStackSupervisor.IDLE_TIMEOUT_MSG);
                    nmsg.obj = msg.obj;
                    ActivityStackSupervisor.this.mHandler.sendMessageDelayed(nmsg, JobStatus.DEFAULT_TRIGGER_UPDATE_DELAY);
                case ActivityStackSupervisor.IDLE_NOW_MSG /*101*/:
                    if (ActivityManagerDebugConfig.DEBUG_IDLE) {
                        Slog.d(ActivityStackSupervisor.TAG_IDLE, "handleMessage: IDLE_NOW_MSG: r=" + msg.obj);
                    }
                    activityIdleInternal((ActivityRecord) msg.obj);
                    break;
                case ActivityStackSupervisor.RESUME_TOP_ACTIVITY_MSG /*102*/:
                    synchronized (ActivityStackSupervisor.this.mService) {
                        try {
                            ActivityManagerService.boostPriorityForLockedSection();
                            ActivityStackSupervisor.this.resumeFocusedStackTopActivityLocked();
                        } finally {
                            ActivityManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                    break;
                case ActivityStackSupervisor.SLEEP_TIMEOUT_MSG /*103*/:
                    synchronized (ActivityStackSupervisor.this.mService) {
                        try {
                            ActivityManagerService.boostPriorityForLockedSection();
                            if (ActivityStackSupervisor.this.mService.isSleepingOrShuttingDownLocked()) {
                                Slog.w(ActivityStackSupervisor.TAG, "Sleep timeout!  Sleeping now.");
                                ActivityStackSupervisor.this.mSleepTimeout = ActivityStackSupervisor.RESTORE_FROM_RECENTS;
                                ActivityStackSupervisor.this.checkReadyForSleepLocked();
                            }
                        } finally {
                            ActivityManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                    break;
                case ActivityStackSupervisor.LAUNCH_TIMEOUT_MSG /*104*/:
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
                    ActivityStackSupervisor.this.mService.mDidDexOpt = ActivityStackSupervisor.VALIDATE_WAKE_LOCK_CALLER;
                    ActivityStackSupervisor.this.mHandler.sendEmptyMessageDelayed(ActivityStackSupervisor.LAUNCH_TIMEOUT_MSG, JobStatus.DEFAULT_TRIGGER_UPDATE_DELAY);
                case ActivityStackSupervisor.HANDLE_DISPLAY_ADDED /*105*/:
                    ActivityStackSupervisor.this.handleDisplayAdded(msg.arg1);
                    break;
                case ActivityStackSupervisor.HANDLE_DISPLAY_CHANGED /*106*/:
                    ActivityStackSupervisor.this.handleDisplayChanged(msg.arg1);
                    break;
                case ActivityStackSupervisor.HANDLE_DISPLAY_REMOVED /*107*/:
                    ActivityStackSupervisor.this.handleDisplayRemoved(msg.arg1);
                    break;
                case ActivityStackSupervisor.CONTAINER_CALLBACK_VISIBILITY /*108*/:
                    container = msg.obj;
                    callback = container.mCallback;
                    if (callback != null) {
                        try {
                            callback.setVisible(container.asBinder(), msg.arg1 == ActivityStackSupervisor.ACTIVITY_RESTRICTION_PERMISSION ? ActivityStackSupervisor.RESTORE_FROM_RECENTS : ActivityStackSupervisor.VALIDATE_WAKE_LOCK_CALLER);
                            break;
                        } catch (RemoteException e) {
                            break;
                        }
                    }
                    break;
                case ActivityStackSupervisor.LOCK_TASK_START_MSG /*109*/:
                    try {
                        if (ActivityStackSupervisor.this.mLockTaskNotify == null) {
                            ActivityStackSupervisor.this.mLockTaskNotify = new LockTaskNotify(ActivityStackSupervisor.this.mService.mContext);
                        }
                        ActivityStackSupervisor.this.mLockTaskNotify.show(ActivityStackSupervisor.RESTORE_FROM_RECENTS);
                        ActivityStackSupervisor.this.mLockTaskModeState = msg.arg2;
                        if (ActivityStackSupervisor.this.getStatusBarService() != null) {
                            int flags = ActivityStackSupervisor.ACTIVITY_RESTRICTION_NONE;
                            if (ActivityStackSupervisor.this.mLockTaskModeState == ActivityStackSupervisor.ACTIVITY_RESTRICTION_PERMISSION) {
                                flags = 62849024;
                            } else if (ActivityStackSupervisor.this.mLockTaskModeState == ActivityStackSupervisor.ACTIVITY_RESTRICTION_APPOP) {
                                flags = 43974656;
                            }
                            ActivityStackSupervisor.this.getStatusBarService().disable(flags, ActivityStackSupervisor.this.mToken, ActivityStackSupervisor.this.mService.mContext.getPackageName());
                        }
                        ActivityStackSupervisor.this.mWindowManager.disableKeyguard(ActivityStackSupervisor.this.mToken, ActivityStackSupervisor.LOCK_TASK_TAG);
                        if (ActivityStackSupervisor.this.getDevicePolicyManager() != null) {
                            ActivityStackSupervisor.this.getDevicePolicyManager().notifyLockTaskModeChanged(ActivityStackSupervisor.RESTORE_FROM_RECENTS, (String) msg.obj, msg.arg1);
                            break;
                        }
                    } catch (RemoteException ex) {
                        throw new RuntimeException(ex);
                    }
                    break;
                case ActivityStackSupervisor.LOCK_TASK_END_MSG /*110*/:
                    try {
                        if (ActivityStackSupervisor.this.getStatusBarService() != null) {
                            ActivityStackSupervisor.this.getStatusBarService().disable(ActivityStackSupervisor.ACTIVITY_RESTRICTION_NONE, ActivityStackSupervisor.this.mToken, ActivityStackSupervisor.this.mService.mContext.getPackageName());
                        }
                        ActivityStackSupervisor.this.mWindowManager.reenableKeyguard(ActivityStackSupervisor.this.mToken);
                        if (ActivityStackSupervisor.this.getDevicePolicyManager() != null) {
                            ActivityStackSupervisor.this.getDevicePolicyManager().notifyLockTaskModeChanged(ActivityStackSupervisor.VALIDATE_WAKE_LOCK_CALLER, null, msg.arg1);
                        }
                        if (ActivityStackSupervisor.this.mLockTaskNotify == null) {
                            ActivityStackSupervisor.this.mLockTaskNotify = new LockTaskNotify(ActivityStackSupervisor.this.mService.mContext);
                        }
                        ActivityStackSupervisor.this.mLockTaskNotify.show(ActivityStackSupervisor.VALIDATE_WAKE_LOCK_CALLER);
                        try {
                            boolean shouldLockKeyguard = Secure.getInt(ActivityStackSupervisor.this.mService.mContext.getContentResolver(), "lock_to_app_exit_locked") != 0 ? ActivityStackSupervisor.RESTORE_FROM_RECENTS : ActivityStackSupervisor.VALIDATE_WAKE_LOCK_CALLER;
                            if (ActivityStackSupervisor.this.mLockTaskModeState == ActivityStackSupervisor.ACTIVITY_RESTRICTION_APPOP && shouldLockKeyguard) {
                                ActivityStackSupervisor.this.mWindowManager.lockNow(null);
                                ActivityStackSupervisor.this.mWindowManager.dismissKeyguard();
                                new LockPatternUtils(ActivityStackSupervisor.this.mService.mContext).requireCredentialEntry(-1);
                            }
                        } catch (SettingNotFoundException e2) {
                        }
                        ActivityStackSupervisor.this.mLockTaskModeState = ActivityStackSupervisor.ACTIVITY_RESTRICTION_NONE;
                        break;
                    } catch (RemoteException ex2) {
                        throw new RuntimeException(ex2);
                    } catch (Throwable th) {
                        ActivityStackSupervisor.this.mLockTaskModeState = ActivityStackSupervisor.ACTIVITY_RESTRICTION_NONE;
                    }
                case ActivityStackSupervisor.CONTAINER_CALLBACK_TASK_LIST_EMPTY /*111*/:
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
                case ActivityStackSupervisor.LAUNCH_TASK_BEHIND_COMPLETE /*112*/:
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
                case ActivityStackSupervisor.SHOW_LOCK_TASK_ESCAPE_MESSAGE_MSG /*113*/:
                    if (ActivityStackSupervisor.this.mLockTaskNotify == null) {
                        ActivityStackSupervisor.this.mLockTaskNotify = new LockTaskNotify(ActivityStackSupervisor.this.mService.mContext);
                    }
                    ActivityStackSupervisor.this.mLockTaskNotify.showToast(ActivityStackSupervisor.ACTIVITY_RESTRICTION_APPOP);
                    break;
                case ActivityStackSupervisor.REPORT_MULTI_WINDOW_MODE_CHANGED_MSG /*114*/:
                    synchronized (ActivityStackSupervisor.this.mService) {
                        try {
                            ActivityManagerService.boostPriorityForLockedSection();
                            for (i = ActivityStackSupervisor.this.mMultiWindowModeChangedActivities.size() - 1; i >= 0; i--) {
                                r = (ActivityRecord) ActivityStackSupervisor.this.mMultiWindowModeChangedActivities.remove(i);
                                Flog.i(ActivityStackSupervisor.IDLE_NOW_MSG, "schedule multiwindow mode change callback for " + r);
                                r.scheduleMultiWindowModeChanged();
                            }
                        } finally {
                            ActivityManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                    break;
                case ActivityStackSupervisor.REPORT_PIP_MODE_CHANGED_MSG /*115*/:
                    synchronized (ActivityStackSupervisor.this.mService) {
                        try {
                            ActivityManagerService.boostPriorityForLockedSection();
                            for (i = ActivityStackSupervisor.this.mPipModeChangedActivities.size() - 1; i >= 0; i--) {
                                ((ActivityRecord) ActivityStackSupervisor.this.mPipModeChangedActivities.remove(i)).schedulePictureInPictureModeChanged();
                            }
                        } finally {
                            ActivityManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                    break;
            }
        }
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
        boolean mDrawn;
        Surface mSurface;

        VirtualActivityContainer(ActivityRecord parent, IActivityContainerCallback callback) {
            super(ActivityStackSupervisor.this.getNextStackId());
            this.mDrawn = ActivityStackSupervisor.VALIDATE_WAKE_LOCK_CALLER;
            this.mParentActivity = parent;
            this.mCallback = callback;
            this.mContainerState = ActivityStackSupervisor.ACTIVITY_RESTRICTION_PERMISSION;
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
            if (this.mContainerState != ActivityStackSupervisor.ACTIVITY_RESTRICTION_APPOP) {
                VirtualActivityDisplay virtualActivityDisplay = this.mActivityDisplay;
                if (virtualActivityDisplay == null) {
                    virtualActivityDisplay = new VirtualActivityDisplay(width, height, density);
                    this.mActivityDisplay = virtualActivityDisplay;
                    ActivityStackSupervisor.this.mActivityDisplays.put(virtualActivityDisplay.mDisplayId, virtualActivityDisplay);
                    attachToDisplayLocked(virtualActivityDisplay, ActivityStackSupervisor.RESTORE_FROM_RECENTS);
                }
                if (this.mSurface != null) {
                    this.mSurface.release();
                }
                this.mSurface = surface;
                if (surface != null) {
                    ActivityStackSupervisor.this.resumeFocusedStackTopActivityLocked();
                } else {
                    this.mContainerState = ActivityStackSupervisor.ACTIVITY_RESTRICTION_PERMISSION;
                    ((VirtualActivityDisplay) this.mActivityDisplay).setSurface(null);
                    if (this.mStack.mPausingActivity == null && this.mStack.mResumedActivity != null) {
                        this.mStack.startPausingLocked(ActivityStackSupervisor.VALIDATE_WAKE_LOCK_CALLER, ActivityStackSupervisor.RESTORE_FROM_RECENTS, ActivityStackSupervisor.VALIDATE_WAKE_LOCK_CALLER, ActivityStackSupervisor.VALIDATE_WAKE_LOCK_CALLER);
                    }
                }
                setSurfaceIfReadyLocked();
                if (ActivityManagerDebugConfig.DEBUG_STACK) {
                    Slog.d(ActivityStackSupervisor.TAG_STACK, "setSurface: " + this + " to display=" + virtualActivityDisplay);
                }
            }
        }

        boolean isAttachedLocked() {
            return this.mSurface != null ? super.isAttachedLocked() : ActivityStackSupervisor.VALIDATE_WAKE_LOCK_CALLER;
        }

        void setDrawn() {
            synchronized (ActivityStackSupervisor.this.mService) {
                try {
                    ActivityManagerService.boostPriorityForLockedSection();
                    this.mDrawn = ActivityStackSupervisor.RESTORE_FROM_RECENTS;
                    setSurfaceIfReadyLocked();
                } finally {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        boolean isEligibleForNewTasks() {
            return ActivityStackSupervisor.VALIDATE_WAKE_LOCK_CALLER;
        }

        private void setSurfaceIfReadyLocked() {
            if (ActivityManagerDebugConfig.DEBUG_STACK) {
                Slog.v(ActivityStackSupervisor.TAG_STACK, "setSurfaceIfReadyLocked: mDrawn=" + this.mDrawn + " mContainerState=" + this.mContainerState + " mSurface=" + this.mSurface);
            }
            if (this.mDrawn && this.mSurface != null && this.mContainerState == ActivityStackSupervisor.ACTIVITY_RESTRICTION_PERMISSION) {
                ((VirtualActivityDisplay) this.mActivityDisplay).setSurface(this.mSurface);
                this.mContainerState = ActivityStackSupervisor.ACTIVITY_RESTRICTION_NONE;
            }
        }
    }

    class VirtualActivityDisplay extends ActivityDisplay {
        VirtualDisplay mVirtualDisplay;

        VirtualActivityDisplay(int width, int height, int density) {
            super();
            this.mVirtualDisplay = DisplayManagerGlobal.getInstance().createVirtualDisplay(ActivityStackSupervisor.this.mService.mContext, null, ActivityStackSupervisor.VIRTUAL_DISPLAY_BASE_NAME, width, height, density, null, 9, null, null);
            init(this.mVirtualDisplay.getDisplay());
            ActivityStackSupervisor.this.mWindowManager.handleDisplayAdded(this.mDisplayId);
        }

        void setSurface(Surface surface) {
            if (this.mVirtualDisplay != null) {
                this.mVirtualDisplay.setSurface(surface);
            }
        }

        void detachActivitiesLocked(ActivityStack stack) {
            super.detachActivitiesLocked(stack);
            if (this.mVirtualDisplay != null) {
                this.mVirtualDisplay.release();
                this.mVirtualDisplay = null;
            }
        }

        public String toString() {
            return "VirtualActivityDisplay={" + this.mDisplayId + "}";
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.am.ActivityStackSupervisor.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.am.ActivityStackSupervisor.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.am.ActivityStackSupervisor.<clinit>():void");
    }

    public ActivityStackSupervisor(ActivityManagerService service) {
        this.mPerf_iop = null;
        this.mPerfBoost = null;
        this.mPerfPack = null;
        this.mIsPerfBoostEnabled = VALIDATE_WAKE_LOCK_CALLER;
        this.mIsperfDisablepackingEnable = VALIDATE_WAKE_LOCK_CALLER;
        this.lBoostTimeOut = ACTIVITY_RESTRICTION_NONE;
        this.lDisPackTimeOut = ACTIVITY_RESTRICTION_NONE;
        this.mToken = new Binder();
        this.mNextFreeStackId = 5;
        this.mCurTaskIdForUser = new SparseIntArray(20);
        this.mWaitingVisibleActivities = new ArrayList();
        this.mWaitingActivityVisible = new ArrayList();
        this.mWaitingActivityLaunched = new ArrayList();
        this.mStoppingActivities = new ArrayList();
        this.mFinishingActivities = new ArrayList();
        this.mGoingToSleepActivities = new ArrayList();
        this.mMultiWindowModeChangedActivities = new ArrayList();
        this.mPipModeChangedActivities = new ArrayList();
        this.mStartingUsers = new ArrayList();
        this.mUserLeaving = VALIDATE_WAKE_LOCK_CALLER;
        this.mSleepTimeout = VALIDATE_WAKE_LOCK_CALLER;
        this.mUserStackInFront = new SparseIntArray(ACTIVITY_RESTRICTION_APPOP);
        this.mActivityContainers = new SparseArray();
        this.mActivityDisplays = new SparseArray();
        this.mLockTaskModeTasks = new ArrayList();
        this.tempRect = new Rect();
        this.tempRect2 = new Rect();
        this.mTmpConfigs = new SparseArray();
        this.mTmpBounds = new SparseArray();
        this.mTmpInsetBounds = new SparseArray();
        this.mDefaultMinSizeOfResizeableTask = -1;
        this.mTaskLayersChanged = RESTORE_FROM_RECENTS;
        this.mTmpFindTaskResult = new FindTaskResult();
        this.mResizingTasksDuringAnimation = new ArraySet();
        this.mAllowDockedStackResize = RESTORE_FROM_RECENTS;
        this.mActivityLaunchTrack = "";
        this.mService = service;
        this.mHandler = new ActivityStackSupervisorHandler(this.mService.mHandler.getLooper());
        this.mActivityMetricsLogger = new ActivityMetricsLogger(this, this.mService.mContext);
        this.mResizeDockedStackTimeout = new ResizeDockedStackTimeout(service, this, this.mHandler);
        this.mIsPerfBoostEnabled = this.mService.mContext.getResources().getBoolean(17957044);
        this.mIsperfDisablepackingEnable = this.mService.mContext.getResources().getBoolean(17957046);
        if (this.mIsPerfBoostEnabled) {
            this.lBoostTimeOut = this.mService.mContext.getResources().getInteger(17694924);
            this.lBoostCpuParamVal = this.mService.mContext.getResources().getIntArray(17236057);
        }
        if (this.mIsperfDisablepackingEnable) {
            this.lDisPackTimeOut = this.mService.mContext.getResources().getInteger(17694926);
            this.lBoostPackParamVal = this.mService.mContext.getResources().getIntArray(17236059);
        }
    }

    void setRecentTasks(RecentTasks recentTasks) {
        this.mRecentTasks = recentTasks;
    }

    void initPowerManagement() {
        PowerManager pm = (PowerManager) this.mService.mContext.getSystemService("power");
        this.mGoingToSleep = pm.newWakeLock(ACTIVITY_RESTRICTION_PERMISSION, "ActivityManager-Sleep");
        this.mLaunchingActivity = pm.newWakeLock(ACTIVITY_RESTRICTION_PERMISSION, "*launch*");
        this.mLaunchingActivity.setReferenceCounted(VALIDATE_WAKE_LOCK_CALLER);
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
                this.mDisplayManager = (DisplayManager) this.mService.mContext.getSystemService("display");
                this.mDisplayManager.registerDisplayListener(this, null);
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
                ActivityStack stack = getStack(ACTIVITY_RESTRICTION_NONE, RESTORE_FROM_RECENTS, RESTORE_FROM_RECENTS);
                this.mLastFocusedStack = stack;
                this.mFocusedStack = stack;
                this.mHomeStack = stack;
                this.mInputManagerInternal = (InputManagerInternal) LocalServices.getService(InputManagerInternal.class);
            } finally {
                ActivityManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    void notifyActivityDrawnForKeyguard() {
        if (ActivityManagerDebugConfig.DEBUG_LOCKSCREEN) {
            this.mService.logLockScreen("");
        }
        this.mWindowManager.notifyActivityDrawnForKeyguard();
    }

    ActivityStack getFocusedStack() {
        return this.mFocusedStack;
    }

    ActivityStack getLastStack() {
        return this.mLastFocusedStack;
    }

    boolean isFocusedStack(ActivityStack stack) {
        boolean z = VALIDATE_WAKE_LOCK_CALLER;
        if (stack == null) {
            return VALIDATE_WAKE_LOCK_CALLER;
        }
        ActivityRecord parent = stack.mActivityContainer.mParentActivity;
        if (parent != null) {
            stack = parent.task.stack;
        }
        if (stack == this.mFocusedStack) {
            z = RESTORE_FROM_RECENTS;
        }
        return z;
    }

    boolean isFrontStack(ActivityStack stack) {
        boolean z = VALIDATE_WAKE_LOCK_CALLER;
        if (stack == null) {
            return VALIDATE_WAKE_LOCK_CALLER;
        }
        ActivityRecord parent = stack.mActivityContainer.mParentActivity;
        if (parent != null) {
            stack = parent.task.stack;
        }
        if (stack == this.mHomeStack.mStacks.get(this.mHomeStack.mStacks.size() - 1)) {
            z = RESTORE_FROM_RECENTS;
        }
        return z;
    }

    void setFocusStackUnchecked(String reason, ActivityStack focusCandidate) {
        int i = -1;
        if (!focusCandidate.isFocusable()) {
            focusCandidate = focusCandidate.getNextFocusableStackLocked();
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
        if (!(this.mService.mDoingSetFocusedActivity || this.mService.mFocusedActivity == r)) {
            this.mService.setFocusedActivityLocked(r, reason + " setFocusStack");
        }
        if ((this.mService.mBooting || !this.mService.mBooted) && r != null && r.idle) {
            checkFinishBootingLocked();
        }
    }

    void moveHomeStackToFront(String reason) {
        this.mHomeStack.moveToFront(reason);
    }

    boolean moveHomeStackTaskToTop(int homeStackTaskType, String reason) {
        if (homeStackTaskType == ACTIVITY_RESTRICTION_APPOP) {
            this.mWindowManager.showRecentApps(VALIDATE_WAKE_LOCK_CALLER);
            return VALIDATE_WAKE_LOCK_CALLER;
        }
        this.mHomeStack.moveHomeStackTaskToTop(homeStackTaskType);
        ActivityRecord top = getHomeActivity();
        if (top == null) {
            return VALIDATE_WAKE_LOCK_CALLER;
        }
        this.mService.setFocusedActivityLocked(top, reason);
        return RESTORE_FROM_RECENTS;
    }

    boolean resumeHomeStackTask(int homeStackTaskType, ActivityRecord prev, String reason) {
        if (!this.mService.mBooting && !this.mService.mBooted) {
            return VALIDATE_WAKE_LOCK_CALLER;
        }
        if (homeStackTaskType == ACTIVITY_RESTRICTION_APPOP) {
            this.mWindowManager.showRecentApps(VALIDATE_WAKE_LOCK_CALLER);
            return VALIDATE_WAKE_LOCK_CALLER;
        }
        if (prev != null) {
            prev.task.setTaskToReturnTo(ACTIVITY_RESTRICTION_NONE);
        }
        this.mHomeStack.moveHomeStackTaskToTop(homeStackTaskType);
        ActivityRecord r = getHomeActivity();
        String myReason = reason + " resumeHomeStackTask";
        if (r == null || r.finishing) {
            return this.mService.startHomeActivityLocked(this.mCurrentUser, myReason);
        }
        this.mService.setFocusedActivityLocked(r, myReason);
        return resumeFocusedStackTopActivityLocked(this.mHomeStack, prev, null);
    }

    TaskRecord anyTaskForIdLocked(int id) {
        return anyTaskForIdLocked(id, RESTORE_FROM_RECENTS, -1);
    }

    TaskRecord anyTaskForIdLocked(int id, boolean restoreFromRecents, int stackId) {
        TaskRecord task;
        int numDisplays = this.mActivityDisplays.size();
        for (int displayNdx = ACTIVITY_RESTRICTION_NONE; displayNdx < numDisplays; displayNdx += ACTIVITY_RESTRICTION_PERMISSION) {
            ArrayList<ActivityStack> stacks = ((ActivityDisplay) this.mActivityDisplays.valueAt(displayNdx)).mStacks;
            for (int stackNdx = stacks.size() - 1; stackNdx >= 0; stackNdx--) {
                task = ((ActivityStack) stacks.get(stackNdx)).taskForIdLocked(id);
                if (task != null) {
                    return task;
                }
            }
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
        } else if (!restoreFromRecents) {
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

    ActivityRecord isInAnyStackLocked(IBinder token) {
        int numDisplays = this.mActivityDisplays.size();
        for (int displayNdx = ACTIVITY_RESTRICTION_NONE; displayNdx < numDisplays; displayNdx += ACTIVITY_RESTRICTION_PERMISSION) {
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

    boolean isUserLockedProfile(int userId) {
        if (!this.mService.mUserController.shouldConfirmCredentials(userId)) {
            return VALIDATE_WAKE_LOCK_CALLER;
        }
        ActivityStack[] activityStacks = new ActivityStack[FIT_WITHIN_BOUNDS_DIVIDER];
        activityStacks[ACTIVITY_RESTRICTION_NONE] = getStack(FIT_WITHIN_BOUNDS_DIVIDER);
        activityStacks[ACTIVITY_RESTRICTION_PERMISSION] = getStack(ACTIVITY_RESTRICTION_APPOP);
        activityStacks[ACTIVITY_RESTRICTION_APPOP] = getStack(ACTIVITY_RESTRICTION_PERMISSION);
        int length = activityStacks.length;
        for (int i = ACTIVITY_RESTRICTION_NONE; i < length; i += ACTIVITY_RESTRICTION_PERMISSION) {
            ActivityStack activityStack = activityStacks[i];
            if (!(activityStack == null || activityStack.topRunningActivityLocked() == null || activityStack.getStackVisibilityLocked(null) == 0 || (activityStack.isDockedStack() && this.mIsDockMinimized))) {
                if (activityStack.mStackId == ACTIVITY_RESTRICTION_APPOP) {
                    List<TaskRecord> tasks = activityStack.getAllTasks();
                    int size = tasks.size();
                    for (int i2 = ACTIVITY_RESTRICTION_NONE; i2 < size; i2 += ACTIVITY_RESTRICTION_PERMISSION) {
                        if (taskContainsActivityFromUser((TaskRecord) tasks.get(i2), userId)) {
                            return RESTORE_FROM_RECENTS;
                        }
                    }
                    continue;
                } else {
                    TaskRecord topTask = activityStack.topTask();
                    if (topTask != null && taskContainsActivityFromUser(topTask, userId)) {
                        return RESTORE_FROM_RECENTS;
                    }
                }
            }
        }
        return VALIDATE_WAKE_LOCK_CALLER;
    }

    private boolean taskContainsActivityFromUser(TaskRecord task, int userId) {
        for (int i = task.mActivities.size() - 1; i >= 0; i--) {
            if (((ActivityRecord) task.mActivities.get(i)).userId == userId) {
                return RESTORE_FROM_RECENTS;
            }
        }
        return VALIDATE_WAKE_LOCK_CALLER;
    }

    void setNextTaskIdForUserLocked(int taskId, int userId) {
        if (taskId > this.mCurTaskIdForUser.get(userId, -1)) {
            this.mCurTaskIdForUser.put(userId, taskId);
        }
    }

    int getNextTaskIdForUserLocked(int userId) {
        int currentTaskId = this.mCurTaskIdForUser.get(userId, userId * MAX_TASK_IDS_PER_USER);
        int candidateTaskId = currentTaskId;
        do {
            if (this.mRecentTasks.taskIdTakenForUserLocked(candidateTaskId, userId) || anyTaskForIdLocked(candidateTaskId, VALIDATE_WAKE_LOCK_CALLER, -1) != null) {
                candidateTaskId += ACTIVITY_RESTRICTION_PERMISSION;
                if (candidateTaskId == (userId + ACTIVITY_RESTRICTION_PERMISSION) * MAX_TASK_IDS_PER_USER) {
                    candidateTaskId -= MAX_TASK_IDS_PER_USER;
                    continue;
                }
            } else {
                this.mCurTaskIdForUser.put(userId, candidateTaskId);
                return candidateTaskId;
            }
        } while (candidateTaskId != currentTaskId);
        throw new IllegalStateException("Cannot get an available task id. Reached limit of 100000 running tasks per user.");
    }

    ActivityRecord resumedAppLocked() {
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
        boolean didSomething = VALIDATE_WAKE_LOCK_CALLER;
        for (int displayNdx = this.mActivityDisplays.size() - 1; displayNdx >= 0; displayNdx--) {
            ArrayList<ActivityStack> stacks = ((ActivityDisplay) this.mActivityDisplays.valueAt(displayNdx)).mStacks;
            for (int stackNdx = stacks.size() - 1; stackNdx >= 0; stackNdx--) {
                ActivityStack stack = (ActivityStack) stacks.get(stackNdx);
                if (isFocusedStack(stack)) {
                    ActivityRecord hr = stack.topRunningActivityLocked();
                    if (hr != null && hr.app == null && app.uid == hr.info.applicationInfo.uid && app.info.euid == hr.info.applicationInfo.euid && processName.equals(hr.processName)) {
                        try {
                            if (realStartActivityLocked(hr, app, RESTORE_FROM_RECENTS, RESTORE_FROM_RECENTS)) {
                                didSomething = RESTORE_FROM_RECENTS;
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
            ensureActivitiesVisibleLocked(null, ACTIVITY_RESTRICTION_NONE, VALIDATE_WAKE_LOCK_CALLER);
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
                    if (resumedActivity == null || !resumedActivity.idle) {
                        if (ActivityManagerDebugConfig.DEBUG_STATES) {
                            Slog.d(TAG_STATES, "allResumedActivitiesIdle: stack=" + stack.mStackId + " " + resumedActivity + " not idle");
                        }
                        return VALIDATE_WAKE_LOCK_CALLER;
                    }
                }
            }
        }
        return RESTORE_FROM_RECENTS;
    }

    boolean allResumedActivitiesComplete() {
        for (int displayNdx = this.mActivityDisplays.size() - 1; displayNdx >= 0; displayNdx--) {
            ArrayList<ActivityStack> stacks = ((ActivityDisplay) this.mActivityDisplays.valueAt(displayNdx)).mStacks;
            for (int stackNdx = stacks.size() - 1; stackNdx >= 0; stackNdx--) {
                ActivityStack stack = (ActivityStack) stacks.get(stackNdx);
                if (isFocusedStack(stack)) {
                    ActivityRecord r = stack.mResumedActivity;
                    if (!(r == null || r.state == ActivityState.RESUMED)) {
                        return VALIDATE_WAKE_LOCK_CALLER;
                    }
                }
            }
        }
        if (ActivityManagerDebugConfig.DEBUG_STACK) {
            Slog.d(TAG_STACK, "allResumedActivitiesComplete: mLastFocusedStack changing from=" + this.mLastFocusedStack + " to=" + this.mFocusedStack);
        }
        this.mLastFocusedStack = this.mFocusedStack;
        return RESTORE_FROM_RECENTS;
    }

    boolean allResumedActivitiesVisible() {
        boolean foundResumed = VALIDATE_WAKE_LOCK_CALLER;
        for (int displayNdx = this.mActivityDisplays.size() - 1; displayNdx >= 0; displayNdx--) {
            ArrayList<ActivityStack> stacks = ((ActivityDisplay) this.mActivityDisplays.valueAt(displayNdx)).mStacks;
            for (int stackNdx = stacks.size() - 1; stackNdx >= 0; stackNdx--) {
                ActivityRecord r = ((ActivityStack) stacks.get(stackNdx)).mResumedActivity;
                if (r != null) {
                    if (!r.nowVisible || this.mWaitingVisibleActivities.contains(r)) {
                        return VALIDATE_WAKE_LOCK_CALLER;
                    }
                    foundResumed = RESTORE_FROM_RECENTS;
                }
            }
        }
        return foundResumed;
    }

    boolean pauseBackStacks(boolean userLeaving, boolean resuming, boolean dontWait) {
        boolean someActivityPaused = VALIDATE_WAKE_LOCK_CALLER;
        for (int displayNdx = this.mActivityDisplays.size() - 1; displayNdx >= 0; displayNdx--) {
            ArrayList<ActivityStack> stacks = ((ActivityDisplay) this.mActivityDisplays.valueAt(displayNdx)).mStacks;
            for (int stackNdx = stacks.size() - 1; stackNdx >= 0; stackNdx--) {
                ActivityStack stack = (ActivityStack) stacks.get(stackNdx);
                if (!(isFocusedStack(stack) || stack.mResumedActivity == null)) {
                    if (ActivityManagerDebugConfig.DEBUG_STATES) {
                        Slog.d(TAG_STATES, "pauseBackStacks: stack=" + stack + " mResumedActivity=" + stack.mResumedActivity);
                    }
                    someActivityPaused |= stack.startPausingLocked(userLeaving, VALIDATE_WAKE_LOCK_CALLER, resuming, dontWait);
                }
            }
        }
        return someActivityPaused;
    }

    boolean allPausedActivitiesComplete() {
        boolean pausing = RESTORE_FROM_RECENTS;
        for (int displayNdx = this.mActivityDisplays.size() - 1; displayNdx >= 0; displayNdx--) {
            ArrayList<ActivityStack> stacks = ((ActivityDisplay) this.mActivityDisplays.valueAt(displayNdx)).mStacks;
            for (int stackNdx = stacks.size() - 1; stackNdx >= 0; stackNdx--) {
                ActivityRecord r = ((ActivityStack) stacks.get(stackNdx)).mPausingActivity;
                if (!(r == null || r.state == ActivityState.PAUSED || r.state == ActivityState.STOPPED || r.state == ActivityState.STOPPING)) {
                    if (!ActivityManagerDebugConfig.DEBUG_STATES) {
                        return VALIDATE_WAKE_LOCK_CALLER;
                    }
                    Slog.d(TAG_STATES, "allPausedActivitiesComplete: r=" + r + " state=" + r.state);
                    pausing = VALIDATE_WAKE_LOCK_CALLER;
                }
            }
        }
        return pausing;
    }

    void pauseChildStacks(ActivityRecord parent, boolean userLeaving, boolean uiSleeping, boolean resuming, boolean dontWait) {
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

    void reportActivityVisibleLocked(ActivityRecord r) {
        sendWaitingVisibleReportLocked(r);
    }

    void sendWaitingVisibleReportLocked(ActivityRecord r) {
        boolean changed = VALIDATE_WAKE_LOCK_CALLER;
        for (int i = this.mWaitingActivityVisible.size() - 1; i >= 0; i--) {
            WaitResult w = (WaitResult) this.mWaitingActivityVisible.get(i);
            if (w.who == null) {
                changed = RESTORE_FROM_RECENTS;
                w.timeout = VALIDATE_WAKE_LOCK_CALLER;
                if (r != null) {
                    w.who = new ComponentName(r.info.packageName, r.info.name);
                }
                w.totalTime = SystemClock.uptimeMillis() - w.thisTime;
                w.thisTime = w.totalTime;
            }
        }
        if (changed) {
            Flog.i(IDLE_NOW_MSG, "waited activity visible, r=" + r);
            this.mService.notifyAll();
        }
    }

    void reportTaskToFrontNoLaunch(ActivityRecord r) {
        boolean changed = VALIDATE_WAKE_LOCK_CALLER;
        for (int i = this.mWaitingActivityLaunched.size() - 1; i >= 0; i--) {
            WaitResult w = (WaitResult) this.mWaitingActivityLaunched.remove(i);
            if (w.who == null) {
                changed = RESTORE_FROM_RECENTS;
                w.result = ACTIVITY_RESTRICTION_APPOP;
            }
        }
        if (changed) {
            this.mService.notifyAll();
        }
    }

    void reportActivityLaunchedLocked(boolean timeout, ActivityRecord r, long thisTime, long totalTime) {
        boolean changed = VALIDATE_WAKE_LOCK_CALLER;
        for (int i = this.mWaitingActivityLaunched.size() - 1; i >= 0; i--) {
            WaitResult w = (WaitResult) this.mWaitingActivityLaunched.remove(i);
            if (w.who == null) {
                changed = RESTORE_FROM_RECENTS;
                w.timeout = timeout;
                if (r != null) {
                    w.who = new ComponentName(r.info.packageName, r.info.name);
                }
                w.thisTime = thisTime;
                w.totalTime = totalTime;
            }
        }
        if (changed) {
            Flog.i(IDLE_NOW_MSG, "waited activity launched, r= " + r);
            this.mService.notifyAll();
        }
    }

    ActivityRecord topRunningActivityLocked() {
        ActivityStack focusedStack = this.mFocusedStack;
        ActivityRecord r = focusedStack.topRunningActivityLocked();
        if (r != null) {
            return r;
        }
        ArrayList<ActivityStack> stacks = this.mHomeStack.mStacks;
        for (int stackNdx = stacks.size() - 1; stackNdx >= 0; stackNdx--) {
            ActivityStack stack = (ActivityStack) stacks.get(stackNdx);
            if (stack != focusedStack && isFrontStack(stack) && stack.isFocusable()) {
                r = stack.topRunningActivityLocked();
                if (r != null) {
                    return r;
                }
            }
        }
        return null;
    }

    void getTasksLocked(int maxNum, List<RunningTaskInfo> list, int callingUid, boolean allowed) {
        ArrayList<ArrayList<RunningTaskInfo>> runningTaskLists = new ArrayList();
        int numDisplays = this.mActivityDisplays.size();
        for (int displayNdx = ACTIVITY_RESTRICTION_NONE; displayNdx < numDisplays; displayNdx += ACTIVITY_RESTRICTION_PERMISSION) {
            int stackNdx;
            ArrayList<ActivityStack> stacks = ((ActivityDisplay) this.mActivityDisplays.valueAt(displayNdx)).mStacks;
            for (stackNdx = stacks.size() - 1; stackNdx >= 0; stackNdx--) {
                ActivityStack stack = (ActivityStack) stacks.get(stackNdx);
                ArrayList<RunningTaskInfo> stackTaskList = new ArrayList();
                runningTaskLists.add(stackTaskList);
                stack.getTasksLocked(stackTaskList, callingUid, allowed);
            }
        }
        while (maxNum > 0) {
            long mostRecentActiveTime = Long.MIN_VALUE;
            ArrayList selectedStackList = null;
            int numTaskLists = runningTaskLists.size();
            for (stackNdx = ACTIVITY_RESTRICTION_NONE; stackNdx < numTaskLists; stackNdx += ACTIVITY_RESTRICTION_PERMISSION) {
                stackTaskList = (ArrayList) runningTaskLists.get(stackNdx);
                if (!stackTaskList.isEmpty()) {
                    long lastActiveTime = ((RunningTaskInfo) stackTaskList.get(ACTIVITY_RESTRICTION_NONE)).lastActiveTime;
                    long currentTimeMillis = System.currentTimeMillis();
                    if (lastActiveTime > currentTimeMillis) {
                        ((RunningTaskInfo) stackTaskList.get(ACTIVITY_RESTRICTION_NONE)).lastActiveTime = currentTimeMillis;
                    } else if (lastActiveTime > mostRecentActiveTime) {
                        mostRecentActiveTime = lastActiveTime;
                        ArrayList<RunningTaskInfo> selectedStackList2 = stackTaskList;
                    }
                }
            }
            if (selectedStackList != null) {
                list.add((RunningTaskInfo) selectedStackList.remove(ACTIVITY_RESTRICTION_NONE));
                maxNum--;
            } else {
                return;
            }
        }
    }

    ActivityInfo resolveActivity(Intent intent, ResolveInfo rInfo, int startFlags, ProfilerInfo profilerInfo) {
        ActivityInfo aInfo = null;
        if (rInfo != null) {
            aInfo = rInfo.activityInfo;
        }
        if (aInfo != null) {
            intent.setComponent(new ComponentName(aInfo.applicationInfo.packageName, aInfo.name));
            if (!aInfo.processName.equals("system")) {
                if ((startFlags & ACTIVITY_RESTRICTION_APPOP) != 0) {
                    this.mService.setDebugApp(aInfo.processName, RESTORE_FROM_RECENTS, VALIDATE_WAKE_LOCK_CALLER);
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
        }
        return aInfo;
    }

    ResolveInfo resolveIntent(Intent intent, String resolvedType, int userId) {
        return resolveIntent(intent, resolvedType, userId, ACTIVITY_RESTRICTION_NONE);
    }

    ResolveInfo resolveIntent(Intent intent, String resolvedType, int userId, int flags) {
        try {
            return AppGlobals.getPackageManager().resolveIntent(intent, resolvedType, (DumpState.DUMP_INSTALLS | flags) | DumpState.DUMP_PROVIDERS, userId);
        } catch (RemoteException e) {
            return null;
        }
    }

    ActivityInfo resolveActivity(Intent intent, String resolvedType, int startFlags, ProfilerInfo profilerInfo, int userId) {
        return resolveActivity(intent, resolveIntent(intent, resolvedType, userId), startFlags, profilerInfo);
    }

    final boolean realStartActivityLocked(ActivityRecord r, ProcessRecord app, boolean andResume, boolean checkConfig) throws RemoteException {
        if (allPausedActivitiesComplete()) {
            if (andResume) {
                r.startFreezingScreenLocked(app, ACTIVITY_RESTRICTION_NONE);
                this.mWindowManager.setAppVisibility(r.appToken, RESTORE_FROM_RECENTS);
                r.startLaunchTickingLocked();
            }
            if (checkConfig) {
                this.mService.updateConfigurationLocked(this.mWindowManager.updateOrientationFromAppTokens(this.mService.mConfiguration, r.mayFreezeScreenLocked(app) ? r.appToken : null), r, VALIDATE_WAKE_LOCK_CALLER);
            }
            r.app = app;
            app.waitingToKill = null;
            r.launchCount += ACTIVITY_RESTRICTION_PERMISSION;
            r.lastLaunchTime = SystemClock.uptimeMillis();
            if (ActivityManagerDebugConfig.DEBUG_ALL) {
                Slog.v(TAG, "Launching: " + r);
            }
            if (app.activities.indexOf(r) < 0) {
                app.activities.add(r);
            }
            this.mService.updateLruProcessLocked(app, RESTORE_FROM_RECENTS, null);
            this.mService.updateOomAdjLocked();
            TaskRecord task = r.task;
            if (task.mLockTaskAuth == ACTIVITY_RESTRICTION_APPOP || task.mLockTaskAuth == 4) {
                setLockTaskModeLocked(task, ACTIVITY_RESTRICTION_PERMISSION, "mLockTaskAuth==LAUNCHABLE", VALIDATE_WAKE_LOCK_CALLER);
            }
            ActivityStack stack = task.stack;
            try {
                if (app.thread == null) {
                    throw new RemoteException();
                }
                ActivityInfo activityInfo;
                List list = null;
                List newIntents = null;
                if (andResume) {
                    list = r.results;
                    newIntents = r.newIntents;
                }
                if (ActivityManagerDebugConfig.DEBUG_SWITCH) {
                    Slog.v(TAG_SWITCH, "Launching: " + r + " icicle=" + r.icicle + " with results=" + list + " newIntents=" + newIntents + " andResume=" + andResume);
                }
                if (andResume) {
                    EventLog.writeEvent(EventLogTags.AM_RESTART_ACTIVITY, new Object[]{Integer.valueOf(r.userId), Integer.valueOf(System.identityHashCode(r)), Integer.valueOf(task.taskId), r.shortComponentName});
                }
                if (r.isHomeActivity()) {
                    this.mService.mHomeProcess = ((ActivityRecord) task.mActivities.get(ACTIVITY_RESTRICTION_NONE)).app;
                    this.mService.reportHomeProcess(this.mService.mHomeProcess);
                }
                this.mService.notifyPackageUse(r.intent.getComponent().getPackageName(), ACTIVITY_RESTRICTION_NONE);
                r.sleeping = VALIDATE_WAKE_LOCK_CALLER;
                r.forceNewConfig = VALIDATE_WAKE_LOCK_CALLER;
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
                        ProfilerInfo profilerInfo2 = new ProfilerInfo(profileFile, profileFd, this.mService.mSamplingInterval, this.mService.mAutoStopProfiler);
                    }
                }
                if (andResume) {
                    app.hasShownUi = RESTORE_FROM_RECENTS;
                    app.pendingUiClean = RESTORE_FROM_RECENTS;
                }
                app.forceProcessStateUpTo(this.mService.mTopProcessState);
                boolean forceHardAccel = HwCustNonHardwareAcceleratedPackagesManager.getDefault().shouldForceEnabled(r.info, app.instrumentationClass);
                if (forceHardAccel) {
                    activityInfo = r.info;
                    activityInfo.flags |= DumpState.DUMP_MESSAGES;
                }
                this.mActivityLaunchTrack = "launchActivity";
                Flog.i(IDLE_NOW_MSG, "launch r: " + r + ", uid = " + r.info.applicationInfo.uid + ", r.info.applicationInfo.euid  = " + r.info.applicationInfo.euid);
                if (Jlog.isPerfTest()) {
                    Jlog.i(2035, Intent.toPkgClsString(r.realActivity));
                }
                app.thread.scheduleLaunchActivity(new Intent(r.intent), r.appToken, System.identityHashCode(r), r.info, new Configuration(this.mService.mConfiguration), new Configuration(task.mOverrideConfig), r.compat, r.launchedFromPackage, task.voiceInteractor, app.repProcState, r.icicle, r.persistentState, list, newIntents, andResume ? VALIDATE_WAKE_LOCK_CALLER : RESTORE_FROM_RECENTS, this.mService.isNextTransitionForward(), profilerInfo);
                if (forceHardAccel) {
                    activityInfo = r.info;
                    activityInfo.flags &= -513;
                }
                if ((app.info.privateFlags & ACTIVITY_RESTRICTION_APPOP) != 0 && app.processName.equals(app.info.packageName)) {
                    if (!(this.mService.mHeavyWeightProcess == null || this.mService.mHeavyWeightProcess == app)) {
                        Slog.w(TAG, "Starting new heavy weight process " + app + " when already running " + this.mService.mHeavyWeightProcess);
                    }
                    this.mService.mHeavyWeightProcess = app;
                    Message msg = this.mService.mHandler.obtainMessage(24);
                    msg.obj = r;
                    this.mService.mHandler.sendMessage(msg);
                }
                r.launchFailed = VALIDATE_WAKE_LOCK_CALLER;
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
                return RESTORE_FROM_RECENTS;
            } catch (Throwable e3) {
                if (r.launchFailed) {
                    Slog.e(TAG, "Second failure launching " + r.intent.getComponent().flattenToShortString() + ", giving up", e3);
                    this.mService.appDiedLocked(app);
                    stack.requestFinishActivityLocked(r.appToken, ACTIVITY_RESTRICTION_NONE, null, "2nd-crash", VALIDATE_WAKE_LOCK_CALLER);
                    return VALIDATE_WAKE_LOCK_CALLER;
                }
                app.activities.remove(r);
                throw e3;
            }
        }
        if (ActivityManagerDebugConfig.DEBUG_SWITCH || ActivityManagerDebugConfig.DEBUG_PAUSE || ActivityManagerDebugConfig.DEBUG_STATES) {
            Slog.v(TAG_PAUSE, "realStartActivityLocked: Skipping start of r=" + r + " some activities pausing...");
        }
        return VALIDATE_WAKE_LOCK_CALLER;
    }

    void startSpecificActivityLocked(ActivityRecord r, boolean andResume, boolean checkConfig) {
        ProcessRecord app = this.mService.getProcessRecordLocked(r.processName, r.info.applicationInfo.uid + r.info.applicationInfo.euid, RESTORE_FROM_RECENTS);
        r.task.stack.setLaunchTime(r);
        if (!(app == null || app.thread == null)) {
            try {
                if ((r.info.flags & ACTIVITY_RESTRICTION_PERMISSION) == 0 || !"android".equals(r.info.packageName)) {
                    app.addPackage(r.info.packageName, r.info.applicationInfo.versionCode, this.mService.mProcessStats);
                }
                realStartActivityLocked(r, app, andResume, checkConfig);
                return;
            } catch (Throwable e) {
                Slog.w(TAG, "Exception when starting activity " + r.intent.getComponent().flattenToShortString(), e);
            }
        }
        Flog.i(IDLE_NOW_MSG, "mService.startProcessLocked for activity: " + r + ", appinfo euid: " + r.info.applicationInfo.euid);
        if (this.mAppResource == null) {
            this.mAppResource = HwFrameworkFactory.getHwResource(19);
        }
        if (!(this.mAppResource == null || r.launchedFromPackage == null)) {
            Set<String> categories = r.intent.getCategories();
            int launched = (categories == null || !categories.contains("android.intent.category.LAUNCHER")) ? ACTIVITY_RESTRICTION_NONE : ACTIVITY_RESTRICTION_PERMISSION;
            this.mAppResource.acquire(r.info.applicationInfo.uid, r.packageName, launched);
        }
        if (r.intent.getComponent() == null || !"com.huawei.android.launcher".equals(r.intent.getComponent().getPackageName()) || this.mService.mUserController.isUserRunningLocked(UserHandle.getUserId(r.info.applicationInfo.uid), 4)) {
            this.mService.startProcessLocked(r.processName, r.info.applicationInfo, RESTORE_FROM_RECENTS, ACTIVITY_RESTRICTION_NONE, "activity", r.intent.getComponent(), VALIDATE_WAKE_LOCK_CALLER, VALIDATE_WAKE_LOCK_CALLER, RESTORE_FROM_RECENTS);
        } else {
            Slog.i(TAG, "skip launch activity for uid: " + r.info.applicationInfo.uid);
        }
    }

    boolean checkStartAnyActivityPermission(Intent intent, ActivityInfo aInfo, String resultWho, int requestCode, int callingPid, int callingUid, String callingPackage, boolean ignoreTargetSecurity, ProcessRecord callerApp, ActivityRecord resultRecord, ActivityStack resultStack, ActivityOptions options) {
        if (this.mService.checkPermission("android.permission.START_ANY_ACTIVITY", callingPid, callingUid) == 0) {
            return RESTORE_FROM_RECENTS;
        }
        int componentRestriction = getComponentRestrictionForCallingPackage(aInfo, callingPackage, callingPid, callingUid, ignoreTargetSecurity);
        int actionRestriction = getActionRestrictionForCallingPackage(intent.getAction(), callingPackage, callingPid, callingUid);
        String msg;
        if (componentRestriction == ACTIVITY_RESTRICTION_PERMISSION || actionRestriction == ACTIVITY_RESTRICTION_PERMISSION) {
            if (resultRecord != null) {
                resultStack.sendActivityResultLocked(-1, resultRecord, resultWho, requestCode, ACTIVITY_RESTRICTION_NONE, null);
            }
            if (actionRestriction == ACTIVITY_RESTRICTION_PERMISSION) {
                msg = "Permission Denial: starting " + intent.toString() + " from " + callerApp + " (pid=" + callingPid + ", uid=" + callingUid + ")" + " with revoked permission " + ((String) ACTION_TO_RUNTIME_PERMISSION.get(intent.getAction()));
            } else if (aInfo.exported) {
                msg = "Permission Denial: starting " + intent.toString() + " from " + callerApp + " (pid=" + callingPid + ", uid=" + callingUid + ")" + " requires " + aInfo.permission;
            } else {
                msg = "Permission Denial: starting " + intent.toString() + " from " + callerApp + " (pid=" + callingPid + ", uid=" + callingUid + ")" + " not exported from uid " + aInfo.applicationInfo.uid;
            }
            Slog.w(TAG, msg);
            throw new SecurityException(msg);
        } else if (actionRestriction == ACTIVITY_RESTRICTION_APPOP) {
            Slog.w(TAG, "Appop Denial: starting " + intent.toString() + " from " + callerApp + " (pid=" + callingPid + ", uid=" + callingUid + ")" + " requires " + AppOpsManager.permissionToOp((String) ACTION_TO_RUNTIME_PERMISSION.get(intent.getAction())));
            return VALIDATE_WAKE_LOCK_CALLER;
        } else if (componentRestriction == ACTIVITY_RESTRICTION_APPOP) {
            Slog.w(TAG, "Appop Denial: starting " + intent.toString() + " from " + callerApp + " (pid=" + callingPid + ", uid=" + callingUid + ")" + " requires appop " + AppOpsManager.permissionToOp(aInfo.permission));
            return VALIDATE_WAKE_LOCK_CALLER;
        } else if (options == null || options.getLaunchTaskId() == -1 || this.mService.checkPermission("android.permission.START_TASKS_FROM_RECENTS", callingPid, callingUid) == 0) {
            return RESTORE_FROM_RECENTS;
        } else {
            msg = "Permission Denial: starting " + intent.toString() + " from " + callerApp + " (pid=" + callingPid + ", uid=" + callingUid + ") with launchTaskId=" + options.getLaunchTaskId();
            Slog.w(TAG, msg);
            throw new SecurityException(msg);
        }
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
                return ACTIVITY_RESTRICTION_PERMISSION;
            }
        }
        if (activityInfo.permission == null) {
            return ACTIVITY_RESTRICTION_NONE;
        }
        int opCode = AppOpsManager.permissionToOpCode(activityInfo.permission);
        if (opCode == -1 || this.mService.mAppOpsService.noteOperation(opCode, callingUid, callingPackage) == 0 || ignoreTargetSecurity) {
            return ACTIVITY_RESTRICTION_NONE;
        }
        return ACTIVITY_RESTRICTION_APPOP;
    }

    private int getActionRestrictionForCallingPackage(String action, String callingPackage, int callingPid, int callingUid) {
        if (action == null) {
            return ACTIVITY_RESTRICTION_NONE;
        }
        String permission = (String) ACTION_TO_RUNTIME_PERMISSION.get(action);
        if (permission == null) {
            return ACTIVITY_RESTRICTION_NONE;
        }
        try {
            if (!ArrayUtils.contains(this.mService.mContext.getPackageManager().getPackageInfo(callingPackage, DumpState.DUMP_PREFERRED).requestedPermissions, permission)) {
                return ACTIVITY_RESTRICTION_NONE;
            }
            if (this.mService.checkPermission(permission, callingPid, callingUid) == -1) {
                return ACTIVITY_RESTRICTION_PERMISSION;
            }
            int opCode = AppOpsManager.permissionToOpCode(permission);
            if (opCode == -1 || this.mService.mAppOpsService.noteOperation(opCode, callingUid, callingPackage) == 0) {
                return ACTIVITY_RESTRICTION_NONE;
            }
            return ACTIVITY_RESTRICTION_APPOP;
        } catch (NameNotFoundException e) {
            Slog.i(TAG, "Cannot find package info for " + callingPackage);
            return ACTIVITY_RESTRICTION_NONE;
        }
    }

    boolean moveActivityStackToFront(ActivityRecord r, String reason) {
        if (r == null) {
            return VALIDATE_WAKE_LOCK_CALLER;
        }
        TaskRecord task = r.task;
        if (task == null || task.stack == null) {
            Slog.w(TAG, "Can't move stack to front for r=" + r + " task=" + task);
            return VALIDATE_WAKE_LOCK_CALLER;
        }
        task.stack.moveToFront(reason, task);
        return RESTORE_FROM_RECENTS;
    }

    void setLaunchSource(int uid) {
        this.mLaunchingActivity.setWorkSource(new WorkSource(uid));
    }

    void acquireLaunchWakelock() {
        this.mLaunchingActivity.acquire();
        if (!this.mHandler.hasMessages(LAUNCH_TIMEOUT_MSG)) {
            this.mHandler.sendEmptyMessageDelayed(LAUNCH_TIMEOUT_MSG, JobStatus.DEFAULT_TRIGGER_UPDATE_DELAY);
        }
    }

    private boolean checkFinishBootingLocked() {
        boolean booting = this.mService.mBooting;
        boolean enableScreen = VALIDATE_WAKE_LOCK_CALLER;
        this.mService.mBooting = VALIDATE_WAKE_LOCK_CALLER;
        if (!this.mService.mBooted) {
            this.mService.mBooted = RESTORE_FROM_RECENTS;
            enableScreen = RESTORE_FROM_RECENTS;
        }
        if (booting || enableScreen) {
            this.mService.postFinishBooting(booting, enableScreen);
        }
        return booting;
    }

    final ActivityRecord activityIdleInternalLocked(IBinder token, boolean fromTimeout, Configuration config) {
        int i;
        if (ActivityManagerDebugConfig.DEBUG_ALL) {
            Slog.v(TAG, "Activity idle: " + token);
        }
        ArrayList arrayList = null;
        ArrayList arrayList2 = null;
        boolean booting = VALIDATE_WAKE_LOCK_CALLER;
        int activityRemoved = ACTIVITY_RESTRICTION_NONE;
        ActivityRecord r = ActivityRecord.forTokenLocked(token);
        if (r != null) {
            if (ActivityManagerDebugConfig.DEBUG_IDLE) {
                Slog.d(TAG_IDLE, "activityIdleInternalLocked: Callers=" + Debug.getCallers(4));
            }
            this.mHandler.removeMessages(IDLE_TIMEOUT_MSG, r);
            r.finishLaunchTickingLocked();
            if (fromTimeout) {
                reportActivityLaunchedLocked(fromTimeout, r, -1, -1);
            }
            if (config != null) {
                r.configuration = config;
            }
            r.idle = RESTORE_FROM_RECENTS;
            if (r.app != null && r.app.foregroundActivities) {
                this.mService.noteActivityStart(r.app.info.packageName, r.app.processName, r.app.pid, r.app.uid, VALIDATE_WAKE_LOCK_CALLER);
            }
            if (isFocusedStack(r.task.stack) || fromTimeout) {
                booting = checkFinishBootingLocked();
            }
        }
        if (allResumedActivitiesIdle()) {
            if (r != null) {
                this.mService.scheduleAppGcsLocked();
            }
            if (this.mLaunchingActivity.isHeld()) {
                this.mHandler.removeMessages(LAUNCH_TIMEOUT_MSG);
                this.mLaunchingActivity.release();
            }
            ensureActivitiesVisibleLocked(null, ACTIVITY_RESTRICTION_NONE, VALIDATE_WAKE_LOCK_CALLER);
        }
        ArrayList<ActivityRecord> stops = processStoppingActivitiesLocked(RESTORE_FROM_RECENTS);
        int NS = stops != null ? stops.size() : ACTIVITY_RESTRICTION_NONE;
        int NF = this.mFinishingActivities.size();
        if (NF > 0) {
            arrayList = new ArrayList(this.mFinishingActivities);
            this.mFinishingActivities.clear();
        }
        if (this.mStartingUsers.size() > 0) {
            ArrayList arrayList3 = new ArrayList(this.mStartingUsers);
            this.mStartingUsers.clear();
        }
        for (i = ACTIVITY_RESTRICTION_NONE; i < NS; i += ACTIVITY_RESTRICTION_PERMISSION) {
            r = (ActivityRecord) stops.get(i);
            ActivityStack stack = r.task.stack;
            if (stack != null) {
                if (r.finishing) {
                    stack.finishCurrentActivityLocked(r, ACTIVITY_RESTRICTION_NONE, VALIDATE_WAKE_LOCK_CALLER);
                } else {
                    stack.stopActivityLocked(r);
                }
            }
        }
        for (i = ACTIVITY_RESTRICTION_NONE; i < NF; i += ACTIVITY_RESTRICTION_PERMISSION) {
            r = (ActivityRecord) arrayList.get(i);
            stack = r.task.stack;
            if (stack != null) {
                activityRemoved |= stack.destroyActivityLocked(r, RESTORE_FROM_RECENTS, "finish-idle");
            }
        }
        if (!(booting || arrayList2 == null)) {
            for (i = ACTIVITY_RESTRICTION_NONE; i < arrayList2.size(); i += ACTIVITY_RESTRICTION_PERMISSION) {
                this.mService.mUserController.finishUserSwitch((UserState) arrayList2.get(i));
            }
        }
        this.mService.trimApplications();
        if (activityRemoved != 0) {
            resumeFocusedStackTopActivityLocked();
        }
        return r;
    }

    boolean handleAppDiedLocked(ProcessRecord app) {
        boolean hasVisibleActivities = VALIDATE_WAKE_LOCK_CALLER;
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
            this.mUserStackInFront.put(userId, stack != null ? stack.getStackId() : ACTIVITY_RESTRICTION_NONE);
        }
    }

    boolean finishDisabledPackageActivitiesLocked(String packageName, Set<String> filterByClasses, boolean doit, boolean evenPersistent, int userId) {
        boolean didSomething = VALIDATE_WAKE_LOCK_CALLER;
        for (int displayNdx = this.mActivityDisplays.size() - 1; displayNdx >= 0; displayNdx--) {
            ArrayList<ActivityStack> stacks = ((ActivityDisplay) this.mActivityDisplays.valueAt(displayNdx)).mStacks;
            for (int stackNdx = stacks.size() - 1; stackNdx >= 0; stackNdx--) {
                if (((ActivityStack) stacks.get(stackNdx)).finishDisabledPackageActivitiesLocked(packageName, filterByClasses, doit, evenPersistent, userId)) {
                    didSomething = RESTORE_FROM_RECENTS;
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
        if (r.app != null && r1 != null && r.app != r1 && r.lastVisibleTime > this.mService.mPreviousProcessVisibleTime && r.app != this.mService.mHomeProcess) {
            this.mService.mPreviousProcess = r.app;
            this.mService.mPreviousProcessVisibleTime = r.lastVisibleTime;
            this.mService.reportPreviousInfo(12, r.app);
        }
    }

    boolean resumeFocusedStackTopActivityLocked() {
        return resumeFocusedStackTopActivityLocked(null, null, null);
    }

    boolean resumeFocusedStackTopActivityLocked(ActivityStack targetStack, ActivityRecord target, ActivityOptions targetOptions) {
        if (targetStack != null && isFocusedStack(targetStack)) {
            return targetStack.resumeTopActivityUncheckedLocked(target, targetOptions);
        }
        ActivityRecord r = this.mFocusedStack.topRunningActivityLocked();
        if (r == null || r.state != ActivityState.RESUMED) {
            this.mFocusedStack.resumeTopActivityUncheckedLocked(null, null);
        }
        return VALIDATE_WAKE_LOCK_CALLER;
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
            for (int stackNdx = ACTIVITY_RESTRICTION_NONE; stackNdx < numStacks; stackNdx += ACTIVITY_RESTRICTION_PERMISSION) {
                ActivityStack stack = (ActivityStack) stacks.get(stackNdx);
                TaskRecord t = stack.finishTopRunningActivityLocked(app, reason);
                if (stack == focusedStack || r1 == null) {
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
            for (int stackNdx = ACTIVITY_RESTRICTION_NONE; stackNdx < numStacks; stackNdx += ACTIVITY_RESTRICTION_PERMISSION) {
                ((ActivityStack) stacks.get(stackNdx)).finishVoiceTask(session);
            }
        }
    }

    void findTaskToMoveToFrontLocked(TaskRecord task, int flags, ActivityOptions options, String reason, boolean forceNonResizeable) {
        if ((flags & ACTIVITY_RESTRICTION_APPOP) == 0) {
            this.mUserLeaving = RESTORE_FROM_RECENTS;
        }
        if ((flags & ACTIVITY_RESTRICTION_PERMISSION) != 0) {
            task.setTaskToReturnTo(ACTIVITY_RESTRICTION_PERMISSION);
        }
        if (task.stack == null) {
            Slog.e(TAG, "findTaskToMoveToFrontLocked: can't move task=" + task + " to front. Stack is null");
            return;
        }
        ActivityRecord top_activity = task.stack.topRunningActivityLocked();
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
                if (stackId != task.stack.mStackId) {
                    stackId = moveTaskToStackUncheckedLocked(task, stackId, RESTORE_FROM_RECENTS, VALIDATE_WAKE_LOCK_CALLER, reason).mStackId;
                }
                if (StackId.resizeStackWithLaunchBounds(stackId)) {
                    resizeStackLocked(stackId, bounds, null, null, VALIDATE_WAKE_LOCK_CALLER, RESTORE_FROM_RECENTS, VALIDATE_WAKE_LOCK_CALLER);
                } else {
                    this.mWindowManager.resizeTask(task.taskId, task.mBounds, task.mOverrideConfig, VALIDATE_WAKE_LOCK_CALLER, VALIDATE_WAKE_LOCK_CALLER);
                }
            }
        }
        ActivityRecord r = task.getTopActivity();
        task.stack.moveTaskToFrontLocked(task, VALIDATE_WAKE_LOCK_CALLER, options, r == null ? null : r.appTimeTracker, reason);
        if (ActivityManagerDebugConfig.DEBUG_STACK) {
            Slog.d(TAG_STACK, "findTaskToMoveToFront: moved to front of stack=" + task.stack);
        }
        handleNonResizableTaskIfNeeded(task, -1, task.stack.mStackId, forceNonResizeable);
    }

    boolean canUseActivityOptionsLaunchBounds(ActivityOptions options, int launchStackId) {
        if (options.getLaunchBounds() == null) {
            return VALIDATE_WAKE_LOCK_CALLER;
        }
        boolean z;
        if (this.mService.mSupportsPictureInPicture && launchStackId == 4) {
            z = RESTORE_FROM_RECENTS;
        } else {
            z = this.mService.mSupportsFreeformWindowManagement;
        }
        return z;
    }

    ActivityStack getStack(int stackId) {
        return getStack(stackId, VALIDATE_WAKE_LOCK_CALLER, VALIDATE_WAKE_LOCK_CALLER);
    }

    ActivityStack getStack(int stackId, boolean createStaticStackIfNeeded, boolean createOnTop) {
        ActivityContainer activityContainer = (ActivityContainer) this.mActivityContainers.get(stackId);
        if (activityContainer != null) {
            return activityContainer.mStack;
        }
        if (createStaticStackIfNeeded && StackId.isStaticStack(stackId)) {
            return createStackOnDisplay(stackId, ACTIVITY_RESTRICTION_NONE, createOnTop);
        }
        return null;
    }

    ArrayList<ActivityStack> getStacks() {
        ArrayList<ActivityStack> allStacks = new ArrayList();
        for (int displayNdx = this.mActivityDisplays.size() - 1; displayNdx >= 0; displayNdx--) {
            allStacks.addAll(((ActivityDisplay) this.mActivityDisplays.valueAt(displayNdx)).mStacks);
        }
        return allStacks;
    }

    IBinder getHomeActivityToken() {
        ActivityRecord homeActivity = getHomeActivity();
        if (homeActivity != null) {
            return homeActivity.appToken;
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
        if (stackId != FIT_WITHIN_BOUNDS_DIVIDER) {
            return (!StackId.isResizeableByDockedStack(stackId) || getStack(FIT_WITHIN_BOUNDS_DIVIDER) == null) ? VALIDATE_WAKE_LOCK_CALLER : RESTORE_FROM_RECENTS;
        } else {
            return RESTORE_FROM_RECENTS;
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

    void deleteActivityContainer(IActivityContainer container) {
        ActivityContainer activityContainer = (ActivityContainer) container;
        if (activityContainer != null) {
            if (ActivityManagerDebugConfig.DEBUG_CONTAINERS) {
                Slog.d(TAG_CONTAINERS, "deleteActivityContainer: callers=" + Debug.getCallers(4));
            }
            int stackId = activityContainer.mStackId;
            this.mActivityContainers.remove(stackId);
            this.mWindowManager.removeStack(stackId);
        }
    }

    void resizeStackLocked(int stackId, Rect bounds, Rect tempTaskBounds, Rect tempTaskInsetBounds, boolean preserveWindows, boolean allowResizeInDockedMode, boolean deferResume) {
        if (stackId == FIT_WITHIN_BOUNDS_DIVIDER) {
            resizeDockedStackLocked(bounds, tempTaskBounds, tempTaskInsetBounds, null, null, preserveWindows);
            return;
        }
        ActivityStack stack = getStack(stackId);
        if (stack == null) {
            Slog.w(TAG, "resizeStack: stackId " + stackId + " not found.");
        } else if (allowResizeInDockedMode || getStack(FIT_WITHIN_BOUNDS_DIVIDER) == null) {
            Trace.traceBegin(64, "am.resizeStack_" + stackId);
            this.mWindowManager.deferSurfaceLayout();
            try {
                resizeStackUncheckedLocked(stack, bounds, tempTaskBounds, tempTaskInsetBounds);
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
        continueUpdateBounds(ACTIVITY_RESTRICTION_NONE);
        for (int i = this.mResizingTasksDuringAnimation.size() - 1; i >= 0; i--) {
            int taskId = ((Integer) this.mResizingTasksDuringAnimation.valueAt(i)).intValue();
            if (anyTaskForIdLocked(taskId, VALIDATE_WAKE_LOCK_CALLER, -1) != null) {
                this.mWindowManager.setTaskDockedResizing(taskId, VALIDATE_WAKE_LOCK_CALLER);
            }
        }
        this.mResizingTasksDuringAnimation.clear();
    }

    void resizeStackUncheckedLocked(ActivityStack stack, Rect bounds, Rect tempTaskBounds, Rect tempTaskInsetBounds) {
        bounds = TaskRecord.validateBounds(bounds);
        if (stack.updateBoundsAllowed(bounds, tempTaskBounds, tempTaskInsetBounds)) {
            this.mTmpBounds.clear();
            this.mTmpConfigs.clear();
            this.mTmpInsetBounds.clear();
            ArrayList<TaskRecord> tasks = stack.getAllTasks();
            Rect taskBounds = tempTaskBounds != null ? tempTaskBounds : bounds;
            Rect insetBounds = tempTaskInsetBounds != null ? tempTaskInsetBounds : taskBounds;
            for (int i = tasks.size() - 1; i >= 0; i--) {
                TaskRecord task = (TaskRecord) tasks.get(i);
                if (task.isResizeable()) {
                    if (stack.mStackId == ACTIVITY_RESTRICTION_APPOP) {
                        this.tempRect2.set(task.mBounds);
                        fitWithinBounds(this.tempRect2, bounds);
                        task.updateOverrideConfiguration(this.tempRect2);
                    } else {
                        task.updateOverrideConfiguration(taskBounds, insetBounds);
                    }
                }
                this.mTmpConfigs.put(task.taskId, task.mOverrideConfig);
                this.mTmpBounds.put(task.taskId, task.mBounds);
                if (tempTaskInsetBounds != null) {
                    this.mTmpInsetBounds.put(task.taskId, tempTaskInsetBounds);
                }
            }
            this.mWindowManager.prepareFreezingTaskBounds(stack.mStackId);
            stack.mFullscreen = this.mWindowManager.resizeStack(stack.mStackId, bounds, this.mTmpConfigs, this.mTmpBounds, this.mTmpInsetBounds);
            stack.setBounds(bounds);
        }
    }

    void moveTasksToFullscreenStackLocked(int fromStackId, boolean onTop) {
        ActivityStack stack = getStack(fromStackId);
        if (stack != null) {
            int i;
            this.mWindowManager.deferSurfaceLayout();
            if (fromStackId == FIT_WITHIN_BOUNDS_DIVIDER) {
                i = ACTIVITY_RESTRICTION_NONE;
                while (i <= 4) {
                    try {
                        if (StackId.isResizeableByDockedStack(i) && getStack(i) != null) {
                            resizeStackLocked(i, null, null, null, RESTORE_FROM_RECENTS, RESTORE_FROM_RECENTS, RESTORE_FROM_RECENTS);
                        }
                        i += ACTIVITY_RESTRICTION_PERMISSION;
                    } catch (Throwable th) {
                        this.mAllowDockedStackResize = RESTORE_FROM_RECENTS;
                        this.mWindowManager.continueSurfaceLayout();
                    }
                }
                this.mAllowDockedStackResize = VALIDATE_WAKE_LOCK_CALLER;
                if (!onTop) {
                    Flog.i(IDLE_NOW_MSG, "The dock stack was dismissed");
                    resizeStackUncheckedLocked(stack, null, null, null);
                }
            }
            ArrayList<TaskRecord> tasks = stack.getAllTasks();
            int size = tasks.size();
            if (onTop) {
                for (i = ACTIVITY_RESTRICTION_NONE; i < size; i += ACTIVITY_RESTRICTION_PERMISSION) {
                    moveTaskToStackLocked(((TaskRecord) tasks.get(i)).taskId, ACTIVITY_RESTRICTION_PERMISSION, onTop, onTop, "moveTasksToFullscreenStack", RESTORE_FROM_RECENTS, RESTORE_FROM_RECENTS);
                }
                ensureActivitiesVisibleLocked(null, ACTIVITY_RESTRICTION_NONE, RESTORE_FROM_RECENTS);
                resumeFocusedStackTopActivityLocked();
            } else {
                for (i = size - 1; i >= 0; i--) {
                    positionTaskInStackLocked(((TaskRecord) tasks.get(i)).taskId, ACTIVITY_RESTRICTION_PERMISSION, ACTIVITY_RESTRICTION_NONE);
                }
            }
            this.mAllowDockedStackResize = RESTORE_FROM_RECENTS;
            this.mWindowManager.continueSurfaceLayout();
        }
    }

    void moveProfileTasksFromFreeformToFullscreenStackLocked(int userId) {
        ActivityStack stack = getStack(ACTIVITY_RESTRICTION_APPOP);
        if (stack != null) {
            this.mWindowManager.deferSurfaceLayout();
            try {
                ArrayList<TaskRecord> tasks = stack.getAllTasks();
                for (int i = tasks.size() - 1; i >= 0; i--) {
                    if (taskContainsActivityFromUser((TaskRecord) tasks.get(i), userId)) {
                        positionTaskInStackLocked(((TaskRecord) tasks.get(i)).taskId, ACTIVITY_RESTRICTION_PERMISSION, ACTIVITY_RESTRICTION_NONE);
                    }
                }
            } finally {
                this.mWindowManager.continueSurfaceLayout();
            }
        }
    }

    void resizeDockedStackLocked(Rect dockedBounds, Rect tempDockedTaskBounds, Rect tempDockedTaskInsetBounds, Rect tempOtherTaskBounds, Rect tempOtherTaskInsetBounds, boolean preserveWindows) {
        if (this.mAllowDockedStackResize) {
            ActivityStack stack = getStack(FIT_WITHIN_BOUNDS_DIVIDER);
            if (stack == null) {
                Slog.w(TAG, "resizeDockedStackLocked: docked stack not found");
                return;
            }
            Trace.traceBegin(64, "am.resizeDockedStack");
            this.mWindowManager.deferSurfaceLayout();
            try {
                this.mAllowDockedStackResize = VALIDATE_WAKE_LOCK_CALLER;
                ActivityRecord r = stack.topRunningActivityLocked();
                resizeStackUncheckedLocked(stack, dockedBounds, tempDockedTaskBounds, tempDockedTaskInsetBounds);
                if (stack.mFullscreen || (dockedBounds == null && !stack.isAttached())) {
                    moveTasksToFullscreenStackLocked(FIT_WITHIN_BOUNDS_DIVIDER, RESTORE_FROM_RECENTS);
                    r = null;
                } else {
                    this.mWindowManager.getStackDockedModeBounds(ACTIVITY_RESTRICTION_NONE, this.tempRect, RESTORE_FROM_RECENTS);
                    int i = ACTIVITY_RESTRICTION_NONE;
                    while (i <= 4) {
                        if (StackId.isResizeableByDockedStack(i) && getStack(i) != null) {
                            resizeStackLocked(i, this.tempRect, tempOtherTaskBounds, tempOtherTaskInsetBounds, preserveWindows, RESTORE_FROM_RECENTS, VALIDATE_WAKE_LOCK_CALLER);
                        }
                        i += ACTIVITY_RESTRICTION_PERMISSION;
                    }
                }
                stack.ensureVisibleActivitiesConfigurationLocked(r, preserveWindows);
                ResizeDockedStackTimeout resizeDockedStackTimeout = this.mResizeDockedStackTimeout;
                boolean z = (tempDockedTaskBounds == null && tempDockedTaskInsetBounds == null && tempOtherTaskBounds == null) ? tempOtherTaskInsetBounds != null ? RESTORE_FROM_RECENTS : VALIDATE_WAKE_LOCK_CALLER : RESTORE_FROM_RECENTS;
                resizeDockedStackTimeout.notifyResizing(dockedBounds, z);
            } finally {
                this.mAllowDockedStackResize = RESTORE_FROM_RECENTS;
                this.mWindowManager.continueSurfaceLayout();
                Trace.traceEnd(64);
            }
        }
    }

    void resizePinnedStackLocked(Rect pinnedBounds, Rect tempPinnedTaskBounds) {
        ActivityStack stack = getStack(4);
        if (stack == null) {
            Slog.w(TAG, "resizePinnedStackLocked: pinned stack not found");
            return;
        }
        Trace.traceBegin(64, "am.resizePinnedStack");
        this.mWindowManager.deferSurfaceLayout();
        try {
            ActivityRecord r = stack.topRunningActivityLocked();
            resizeStackUncheckedLocked(stack, pinnedBounds, tempPinnedTaskBounds, null);
            stack.ensureVisibleActivitiesConfigurationLocked(r, VALIDATE_WAKE_LOCK_CALLER);
        } finally {
            this.mWindowManager.continueSurfaceLayout();
            Trace.traceEnd(64);
        }
    }

    boolean resizeTaskLocked(TaskRecord task, Rect bounds, int resizeMode, boolean preserveWindow, boolean deferResume) {
        if (task.isResizeable()) {
            boolean forced = (resizeMode & ACTIVITY_RESTRICTION_APPOP) != 0 ? RESTORE_FROM_RECENTS : VALIDATE_WAKE_LOCK_CALLER;
            if (Objects.equals(task.mBounds, bounds) && !forced) {
                return RESTORE_FROM_RECENTS;
            }
            bounds = TaskRecord.validateBounds(bounds);
            if (this.mWindowManager.isValidTaskId(task.taskId)) {
                Trace.traceBegin(64, "am.resizeTask_" + task.taskId);
                Configuration overrideConfig = task.updateOverrideConfiguration(bounds);
                boolean z = RESTORE_FROM_RECENTS;
                if (overrideConfig != null) {
                    ActivityRecord r = task.topRunningActivityLocked();
                    if (r != null) {
                        z = task.stack.ensureActivityConfigurationLocked(r, ACTIVITY_RESTRICTION_NONE, preserveWindow);
                        if (!deferResume) {
                            ensureActivitiesVisibleLocked(r, ACTIVITY_RESTRICTION_NONE, VALIDATE_WAKE_LOCK_CALLER);
                            if (!z) {
                                resumeFocusedStackTopActivityLocked();
                            }
                        }
                    }
                }
                this.mWindowManager.resizeTask(task.taskId, task.mBounds, task.mOverrideConfig, z, forced);
                Trace.traceEnd(64);
                return z;
            }
            task.updateOverrideConfiguration(bounds);
            if (!(task.stack == null || task.stack.mStackId == ACTIVITY_RESTRICTION_APPOP)) {
                restoreRecentTaskLocked(task, ACTIVITY_RESTRICTION_APPOP);
            }
            return RESTORE_FROM_RECENTS;
        }
        Slog.w(TAG, "resizeTask: task " + task + " not resizeable.");
        return RESTORE_FROM_RECENTS;
    }

    ActivityStack createStackOnDisplay(int stackId, int displayId, boolean onTop) {
        ActivityDisplay activityDisplay = (ActivityDisplay) this.mActivityDisplays.get(displayId);
        if (activityDisplay == null) {
            return null;
        }
        ActivityContainer activityContainer = new ActivityContainer(stackId);
        this.mActivityContainers.put(stackId, activityContainer);
        activityContainer.attachToDisplayLocked(activityDisplay, onTop);
        return activityContainer.mStack;
    }

    int getNextStackId() {
        while (true) {
            if (this.mNextFreeStackId >= 5 && getStack(this.mNextFreeStackId) == null) {
                return this.mNextFreeStackId;
            }
            this.mNextFreeStackId += ACTIVITY_RESTRICTION_PERMISSION;
        }
    }

    private boolean restoreRecentTaskLocked(TaskRecord task, int stackId) {
        if (stackId == -1) {
            stackId = task.getLaunchStackId();
        } else if (stackId == FIT_WITHIN_BOUNDS_DIVIDER && !task.canGoInDockedStack()) {
            stackId = ACTIVITY_RESTRICTION_PERMISSION;
        } else if (stackId == ACTIVITY_RESTRICTION_APPOP && this.mService.mUserController.shouldConfirmCredentials(task.userId)) {
            stackId = ACTIVITY_RESTRICTION_PERMISSION;
        }
        if (task.stack != null) {
            if (task.stack.mStackId == stackId) {
                return RESTORE_FROM_RECENTS;
            }
            task.stack.removeTask(task, "restoreRecentTaskLocked", ACTIVITY_RESTRICTION_PERMISSION);
        }
        ActivityStack stack = getStack(stackId, RESTORE_FROM_RECENTS, VALIDATE_WAKE_LOCK_CALLER);
        if (stack == null) {
            if (ActivityManagerDebugConfig.DEBUG_RECENTS) {
                Slog.v(TAG_RECENTS, "Unable to find/create stack to restore recent task=" + task);
            }
            return VALIDATE_WAKE_LOCK_CALLER;
        }
        stack.addTask(task, VALIDATE_WAKE_LOCK_CALLER, "restoreRecentTask");
        if (ActivityManagerDebugConfig.DEBUG_RECENTS) {
            Slog.v(TAG_RECENTS, "Added restored task=" + task + " to stack=" + stack);
        }
        ArrayList<ActivityRecord> activities = task.mActivities;
        for (int activityNdx = activities.size() - 1; activityNdx >= 0; activityNdx--) {
            stack.addConfigOverride((ActivityRecord) activities.get(activityNdx), task);
        }
        return RESTORE_FROM_RECENTS;
    }

    ActivityStack moveTaskToStackUncheckedLocked(TaskRecord task, int stackId, boolean toTop, boolean forceFocus, String reason) {
        if (!StackId.isMultiWindowStack(stackId) || this.mService.mSupportsMultiWindow) {
            ActivityRecord r = task.topRunningActivityLocked();
            ActivityStack prevStack = task.stack;
            boolean wasFocused = (isFocusedStack(prevStack) && topRunningActivityLocked() == r) ? RESTORE_FROM_RECENTS : VALIDATE_WAKE_LOCK_CALLER;
            boolean wasResumed = prevStack.mResumedActivity == r ? RESTORE_FROM_RECENTS : VALIDATE_WAKE_LOCK_CALLER;
            boolean wasFront = isFrontStack(prevStack) ? prevStack.topRunningActivityLocked() == r ? RESTORE_FROM_RECENTS : VALIDATE_WAKE_LOCK_CALLER : VALIDATE_WAKE_LOCK_CALLER;
            if (stackId == FIT_WITHIN_BOUNDS_DIVIDER && !task.isResizeable()) {
                stackId = prevStack != null ? prevStack.mStackId : ACTIVITY_RESTRICTION_PERMISSION;
                Slog.w(TAG, "Can not move unresizeable task=" + task + " to docked stack. Moving to stackId=" + stackId + " instead.");
            }
            if (stackId == ACTIVITY_RESTRICTION_APPOP && this.mService.mUserController.shouldConfirmCredentials(task.userId)) {
                stackId = prevStack != null ? prevStack.mStackId : ACTIVITY_RESTRICTION_PERMISSION;
                Slog.w(TAG, "Can not move locked profile task=" + task + " to freeform stack. Moving to stackId=" + stackId + " instead.");
            }
            task.mTemporarilyUnresizable = RESTORE_FROM_RECENTS;
            ActivityStack stack = getStack(stackId, RESTORE_FROM_RECENTS, toTop);
            task.mTemporarilyUnresizable = VALIDATE_WAKE_LOCK_CALLER;
            this.mWindowManager.moveTaskToStack(task.taskId, stack.mStackId, toTop);
            stack.addTask(task, toTop, reason);
            if (forceFocus || wasFocused) {
                wasFront = RESTORE_FROM_RECENTS;
            }
            stack.moveToFrontAndResumeStateIfNeeded(r, wasFront, wasResumed, reason);
            return stack;
        }
        throw new IllegalStateException("moveTaskToStackUncheckedLocked: Device doesn't support multi-window task=" + task + " to stackId=" + stackId);
    }

    boolean moveTaskToStackLocked(int taskId, int stackId, boolean toTop, boolean forceFocus, String reason, boolean animate) {
        return moveTaskToStackLocked(taskId, stackId, toTop, forceFocus, reason, animate, VALIDATE_WAKE_LOCK_CALLER);
    }

    boolean moveTaskToStackLocked(int taskId, int stackId, boolean toTop, boolean forceFocus, String reason, boolean animate, boolean deferResume) {
        TaskRecord task = anyTaskForIdLocked(taskId);
        if (task == null) {
            Slog.w(TAG, "moveTaskToStack: no task for id=" + taskId);
            return VALIDATE_WAKE_LOCK_CALLER;
        } else if (task.stack != null && task.stack.mStackId == stackId) {
            Slog.i(TAG, "moveTaskToStack: taskId=" + taskId + " already in stackId=" + stackId);
            return RESTORE_FROM_RECENTS;
        } else if (stackId != ACTIVITY_RESTRICTION_APPOP || this.mService.mSupportsFreeformWindowManagement) {
            ActivityRecord topActivity = task.getTopActivity();
            boolean mightReplaceWindow = (!StackId.replaceWindowsOnTaskMove(task.stack != null ? task.stack.mStackId : -1, stackId) || topActivity == null) ? VALIDATE_WAKE_LOCK_CALLER : RESTORE_FROM_RECENTS;
            if (mightReplaceWindow) {
                this.mWindowManager.setReplacingWindow(topActivity.appToken, animate);
                Flog.i(IDLE_NOW_MSG, "moveTaskToStack: replace window for taskId=" + taskId + " appToken " + topActivity.appToken);
            }
            this.mWindowManager.deferSurfaceLayout();
            int preferredLaunchStackId = stackId;
            boolean kept = RESTORE_FROM_RECENTS;
            try {
                boolean z;
                ActivityStack stack = moveTaskToStackUncheckedLocked(task, stackId, toTop, forceFocus, reason + " moveTaskToStack");
                stackId = stack.mStackId;
                if (!animate) {
                    stack.mNoAnimActivities.add(topActivity);
                }
                this.mWindowManager.prepareFreezingTaskBounds(stack.mStackId);
                if (stackId == ACTIVITY_RESTRICTION_PERMISSION && task.mBounds != null) {
                    kept = resizeTaskLocked(task, stack.mBounds, ACTIVITY_RESTRICTION_NONE, mightReplaceWindow ? VALIDATE_WAKE_LOCK_CALLER : RESTORE_FROM_RECENTS, deferResume);
                } else if (stackId == ACTIVITY_RESTRICTION_APPOP) {
                    Rect bounds = task.getLaunchBounds();
                    if (bounds == null) {
                        stack.layoutTaskInStack(task, null);
                        bounds = task.mBounds;
                    }
                    kept = resizeTaskLocked(task, bounds, ACTIVITY_RESTRICTION_APPOP, mightReplaceWindow ? VALIDATE_WAKE_LOCK_CALLER : RESTORE_FROM_RECENTS, deferResume);
                } else if (stackId == FIT_WITHIN_BOUNDS_DIVIDER || stackId == 4) {
                    kept = resizeTaskLocked(task, stack.mBounds, ACTIVITY_RESTRICTION_NONE, mightReplaceWindow ? VALIDATE_WAKE_LOCK_CALLER : RESTORE_FROM_RECENTS, deferResume);
                }
                this.mWindowManager.continueSurfaceLayout();
                if (mightReplaceWindow) {
                    this.mWindowManager.scheduleClearReplacingWindowIfNeeded(topActivity.appToken, kept ? VALIDATE_WAKE_LOCK_CALLER : RESTORE_FROM_RECENTS);
                }
                if (!deferResume) {
                    ensureActivitiesVisibleLocked(null, ACTIVITY_RESTRICTION_NONE, mightReplaceWindow ? VALIDATE_WAKE_LOCK_CALLER : RESTORE_FROM_RECENTS);
                    resumeFocusedStackTopActivityLocked();
                }
                handleNonResizableTaskIfNeeded(task, preferredLaunchStackId, stackId);
                if (preferredLaunchStackId == stackId) {
                    z = RESTORE_FROM_RECENTS;
                } else {
                    z = VALIDATE_WAKE_LOCK_CALLER;
                }
                return z;
            } catch (Throwable th) {
                this.mWindowManager.continueSurfaceLayout();
            }
        } else {
            throw new IllegalArgumentException("moveTaskToStack:Attempt to move task " + taskId + " to unsupported freeform stack");
        }
    }

    boolean moveTopStackActivityToPinnedStackLocked(int stackId, Rect bounds) {
        ActivityStack stack = getStack(stackId, VALIDATE_WAKE_LOCK_CALLER, VALIDATE_WAKE_LOCK_CALLER);
        if (stack == null) {
            throw new IllegalArgumentException("moveTopStackActivityToPinnedStackLocked: Unknown stackId=" + stackId);
        }
        ActivityRecord r = stack.topRunningActivityLocked();
        if (r == null) {
            Slog.w(TAG, "moveTopStackActivityToPinnedStackLocked: No top running activity in stack=" + stack);
            return VALIDATE_WAKE_LOCK_CALLER;
        } else if (this.mService.mForceResizableActivities || r.supportsPictureInPicture()) {
            moveActivityToPinnedStackLocked(r, "moveTopActivityToPinnedStack", bounds);
            return RESTORE_FROM_RECENTS;
        } else {
            Slog.w(TAG, "moveTopStackActivityToPinnedStackLocked: Picture-In-Picture not supported for  r=" + r);
            return VALIDATE_WAKE_LOCK_CALLER;
        }
    }

    void moveActivityToPinnedStackLocked(ActivityRecord r, String reason, Rect bounds) {
        this.mWindowManager.deferSurfaceLayout();
        try {
            TaskRecord task = r.task;
            if (r == task.stack.getVisibleBehindActivity()) {
                requestVisibleBehindLocked(r, VALIDATE_WAKE_LOCK_CALLER);
            }
            ActivityStack stack = getStack(4, RESTORE_FROM_RECENTS, RESTORE_FROM_RECENTS);
            resizeStackLocked(4, task.mBounds, null, null, VALIDATE_WAKE_LOCK_CALLER, RESTORE_FROM_RECENTS, VALIDATE_WAKE_LOCK_CALLER);
            if (task.mActivities.size() == ACTIVITY_RESTRICTION_PERMISSION) {
                if (task.getTaskToReturnTo() == ACTIVITY_RESTRICTION_PERMISSION) {
                    moveHomeStackToFront(reason);
                }
                moveTaskToStackLocked(task.taskId, 4, RESTORE_FROM_RECENTS, RESTORE_FROM_RECENTS, reason, VALIDATE_WAKE_LOCK_CALLER);
            } else {
                stack.moveActivityToStack(r);
            }
            this.mWindowManager.continueSurfaceLayout();
            ensureActivitiesVisibleLocked(null, ACTIVITY_RESTRICTION_NONE, VALIDATE_WAKE_LOCK_CALLER);
            resumeFocusedStackTopActivityLocked();
            this.mWindowManager.animateResizePinnedStack(bounds, -1);
            this.mService.notifyActivityPinnedLocked();
        } catch (Throwable th) {
            this.mWindowManager.continueSurfaceLayout();
        }
    }

    void positionTaskInStackLocked(int taskId, int stackId, int position) {
        TaskRecord task = anyTaskForIdLocked(taskId);
        if (task == null) {
            Slog.w(TAG, "positionTaskInStackLocked: no task for id=" + taskId);
            return;
        }
        ActivityStack stack = getStack(stackId, RESTORE_FROM_RECENTS, VALIDATE_WAKE_LOCK_CALLER);
        task.updateOverrideConfigurationForStack(stack);
        this.mWindowManager.positionTaskInStack(taskId, stackId, position, task.mBounds, task.mOverrideConfig);
        stack.positionTask(task, position);
        stack.ensureActivitiesVisibleLocked(null, ACTIVITY_RESTRICTION_NONE, VALIDATE_WAKE_LOCK_CALLER);
        resumeFocusedStackTopActivityLocked();
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

    ActivityRecord findTaskLocked(ActivityRecord r) {
        this.mTmpFindTaskResult.r = null;
        this.mTmpFindTaskResult.matchedByRootAffinity = VALIDATE_WAKE_LOCK_CALLER;
        if (ActivityManagerDebugConfig.DEBUG_TASKS) {
            Slog.d(TAG_TASKS, "Looking for task of " + r);
        }
        for (int displayNdx = this.mActivityDisplays.size() - 1; displayNdx >= 0; displayNdx--) {
            ArrayList<ActivityStack> stacks = ((ActivityDisplay) this.mActivityDisplays.valueAt(displayNdx)).mStacks;
            for (int stackNdx = stacks.size() - 1; stackNdx >= 0; stackNdx--) {
                ActivityStack stack = (ActivityStack) stacks.get(stackNdx);
                if (r.isApplicationActivity() || stack.isHomeStack()) {
                    if (stack.mActivityContainer.isEligibleForNewTasks()) {
                        stack.findTaskLocked(r, this.mTmpFindTaskResult);
                        if (!(this.mTmpFindTaskResult.r == null || this.mTmpFindTaskResult.matchedByRootAffinity)) {
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
                        }
                    } else if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                        Slog.d(TAG_TASKS, "Skipping stack: (new task not allowed) " + stack);
                    }
                } else if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                    Slog.d(TAG_TASKS, "Skipping stack: (home activity) " + stack);
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
        if (ActivityManagerDebugConfig.DEBUG_TASKS && this.mTmpFindTaskResult.r == null) {
            Slog.d(TAG_TASKS, "No task found");
        }
        return this.mTmpFindTaskResult.r;
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
                this.mService.mHandler.removeMessages(LAUNCH_TIMEOUT_MSG);
            }
        }
        checkReadyForSleepLocked();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    boolean shutdownLocked(int timeout) {
        goingToSleepLocked();
        boolean timedout = VALIDATE_WAKE_LOCK_CALLER;
        long endTime = System.currentTimeMillis() + ((long) timeout);
        while (true) {
            boolean cantShutdown = VALIDATE_WAKE_LOCK_CALLER;
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
                break;
            }
            try {
                this.mService.wait(timeRemaining);
            } catch (InterruptedException e) {
            }
        }
        this.mSleepTimeout = RESTORE_FROM_RECENTS;
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
                boolean dontSleep = VALIDATE_WAKE_LOCK_CALLER;
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
                    dontSleep = RESTORE_FROM_RECENTS;
                }
                if (this.mGoingToSleepActivities.size() > 0) {
                    if (ActivityManagerDebugConfig.DEBUG_PAUSE) {
                        Slog.v(TAG_PAUSE, "Sleep still need to sleep " + this.mGoingToSleepActivities.size() + " activities");
                    }
                    dontSleep = RESTORE_FROM_RECENTS;
                }
                if (dontSleep) {
                    return;
                }
            }
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
        if (isFocusedStack(r.task.stack)) {
            this.mService.updateUsageStats(r, RESTORE_FROM_RECENTS);
        }
        if (!allResumedActivitiesComplete()) {
            return VALIDATE_WAKE_LOCK_CALLER;
        }
        ensureActivitiesVisibleLocked(null, ACTIVITY_RESTRICTION_NONE, VALIDATE_WAKE_LOCK_CALLER);
        this.mWindowManager.executeAppTransition();
        return RESTORE_FROM_RECENTS;
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
        ActivityStack stack = r.task.stack;
        if (stack == null) {
            if (ActivityManagerDebugConfig.DEBUG_VISIBLE_BEHIND) {
                Slog.d(TAG_VISIBLE_BEHIND, "requestVisibleBehind: r=" + r + " visible=" + visible + " stack is null");
            }
            return VALIDATE_WAKE_LOCK_CALLER;
        } else if (!visible || StackId.activitiesCanRequestVisibleBehind(stack.mStackId)) {
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
                return RESTORE_FROM_RECENTS;
            } else if (visible && top.fullscreen) {
                if (ActivityManagerDebugConfig.DEBUG_VISIBLE_BEHIND) {
                    Slog.d(TAG_VISIBLE_BEHIND, "requestVisibleBehind: returning top.fullscreen=" + top.fullscreen + " top.state=" + top.state + " top.app=" + top.app + " top.app.thread=" + top.app.thread);
                }
                return VALIDATE_WAKE_LOCK_CALLER;
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
                return RESTORE_FROM_RECENTS;
            } else {
                if (ActivityManagerDebugConfig.DEBUG_VISIBLE_BEHIND) {
                    Slog.d(TAG_VISIBLE_BEHIND, "requestVisibleBehind: returning visible=" + visible + " stack.getVisibleBehindActivity()=" + stack.getVisibleBehindActivity() + " r=" + r);
                }
                return VALIDATE_WAKE_LOCK_CALLER;
            }
        } else {
            if (ActivityManagerDebugConfig.DEBUG_VISIBLE_BEHIND) {
                Slog.d(TAG_VISIBLE_BEHIND, "requestVisibleBehind: r=" + r + " visible=" + visible + " stackId=" + stack.mStackId + " can't contain visible behind activities");
            }
            return VALIDATE_WAKE_LOCK_CALLER;
        }
    }

    void handleLaunchTaskBehindCompleteLocked(ActivityRecord r) {
        TaskRecord task = r.task;
        ActivityStack stack = task.stack;
        r.mLaunchTaskBehind = VALIDATE_WAKE_LOCK_CALLER;
        task.setLastThumbnailLocked(stack.screenshotActivitiesLocked(r));
        this.mRecentTasks.addLocked(task);
        this.mService.notifyTaskStackChangedLocked();
        this.mWindowManager.setAppVisibility(r.appToken, VALIDATE_WAKE_LOCK_CALLER);
        ActivityRecord top = stack.topActivity();
        if (top != null) {
            top.task.touchActiveTime();
        }
    }

    void scheduleLaunchTaskBehindComplete(IBinder token) {
        this.mHandler.obtainMessage(LAUNCH_TASK_BEHIND_COMPLETE, token).sendToTarget();
    }

    void ensureActivitiesVisibleLocked(ActivityRecord starting, int configChanges, boolean preserveWindows) {
        for (int displayNdx = this.mActivityDisplays.size() - 1; displayNdx >= 0; displayNdx--) {
            ArrayList<ActivityStack> stacks = ((ActivityDisplay) this.mActivityDisplays.valueAt(displayNdx)).mStacks;
            for (int stackNdx = stacks.size() - 1; stackNdx >= 0; stackNdx--) {
                ((ActivityStack) stacks.get(stackNdx)).ensureActivitiesVisibleLocked(starting, configChanges, preserveWindows);
            }
        }
    }

    void invalidateTaskLayers() {
        this.mTaskLayersChanged = RESTORE_FROM_RECENTS;
    }

    void rankTaskLayersIfNeeded() {
        if (this.mTaskLayersChanged) {
            this.mTaskLayersChanged = VALIDATE_WAKE_LOCK_CALLER;
            for (int displayNdx = ACTIVITY_RESTRICTION_NONE; displayNdx < this.mActivityDisplays.size(); displayNdx += ACTIVITY_RESTRICTION_PERMISSION) {
                ArrayList<ActivityStack> stacks = ((ActivityDisplay) this.mActivityDisplays.valueAt(displayNdx)).mStacks;
                int baseLayer = ACTIVITY_RESTRICTION_NONE;
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
            for (int stackNdx = ACTIVITY_RESTRICTION_NONE; stackNdx < numStacks; stackNdx += ACTIVITY_RESTRICTION_PERMISSION) {
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
        for (int i = ACTIVITY_RESTRICTION_NONE; i < app.activities.size(); i += ACTIVITY_RESTRICTION_PERMISSION) {
            ActivityRecord r = (ActivityRecord) app.activities.get(i);
            if (r.finishing || r.state == ActivityState.DESTROYING || r.state == ActivityState.DESTROYED) {
                if (ActivityManagerDebugConfig.DEBUG_RELEASE) {
                    Slog.d(TAG_RELEASE, "Abort release; already destroying: " + r);
                }
                return;
            }
            if (r.visible || !r.stopped || !r.haveState || r.state == ActivityState.RESUMED || r.state == ActivityState.PAUSING || r.state == ActivityState.PAUSED || r.state == ActivityState.STOPPING) {
                if (ActivityManagerDebugConfig.DEBUG_RELEASE) {
                    Slog.d(TAG_RELEASE, "Not releasing in-use activity: " + r);
                }
            } else if (r.task != null) {
                if (ActivityManagerDebugConfig.DEBUG_RELEASE) {
                    Slog.d(TAG_RELEASE, "Collecting release task " + r.task + " from " + r);
                }
                if (firstTask == null) {
                    firstTask = r.task;
                } else if (firstTask != r.task) {
                    if (tasks == null) {
                        tasks = new ArraySet();
                        tasks.add(firstTask);
                    }
                    tasks.add(r.task);
                }
            }
        }
        if (tasks == null) {
            if (ActivityManagerDebugConfig.DEBUG_RELEASE) {
                Slog.d(TAG_RELEASE, "Didn't find two or more tasks to release");
            }
            return;
        }
        int numDisplays = this.mActivityDisplays.size();
        for (int displayNdx = ACTIVITY_RESTRICTION_NONE; displayNdx < numDisplays; displayNdx += ACTIVITY_RESTRICTION_PERMISSION) {
            ArrayList<ActivityStack> stacks = ((ActivityDisplay) this.mActivityDisplays.valueAt(displayNdx)).mStacks;
            int stackNdx = ACTIVITY_RESTRICTION_NONE;
            while (stackNdx < stacks.size()) {
                if (((ActivityStack) stacks.get(stackNdx)).releaseSomeActivitiesLocked(app, tasks, reason) <= 0) {
                    stackNdx += ACTIVITY_RESTRICTION_PERMISSION;
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
        if (focusStackId == FIT_WITHIN_BOUNDS_DIVIDER) {
            z = RESTORE_FROM_RECENTS;
        } else {
            z = VALIDATE_WAKE_LOCK_CALLER;
        }
        moveTasksToFullscreenStackLocked(FIT_WITHIN_BOUNDS_DIVIDER, z);
        this.mUserStackInFront.put(this.mCurrentUser, focusStackId);
        int restoreStackId = this.mUserStackInFront.get(userId, ACTIVITY_RESTRICTION_NONE);
        this.mCurrentUser = userId;
        this.mStartingUsers.add(uss);
        for (int displayNdx = this.mActivityDisplays.size() - 1; displayNdx >= 0; displayNdx--) {
            ArrayList<ActivityStack> stacks = ((ActivityDisplay) this.mActivityDisplays.valueAt(displayNdx)).mStacks;
            for (int stackNdx = stacks.size() - 1; stackNdx >= 0; stackNdx--) {
                stack = (ActivityStack) stacks.get(stackNdx);
                stack.switchUserLocked(userId);
                TaskRecord task = stack.topTask();
                if (task != null) {
                    this.mWindowManager.moveTaskToTop(task.taskId);
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
            resumeHomeStackTask(ACTIVITY_RESTRICTION_PERMISSION, null, "switchUserOnOtherDisplay");
        }
        return homeInFront;
    }

    boolean isCurrentProfileLocked(int userId) {
        if (userId == this.mCurrentUser) {
            return RESTORE_FROM_RECENTS;
        }
        return this.mService.mUserController.isCurrentProfileLocked(userId);
    }

    boolean okToShowLocked(ActivityRecord r) {
        if (r == null) {
            return VALIDATE_WAKE_LOCK_CALLER;
        }
        if ((r.info.flags & DumpState.DUMP_PROVIDERS) != 0) {
            return RESTORE_FROM_RECENTS;
        }
        if (!isCurrentProfileLocked(r.userId)) {
            return VALIDATE_WAKE_LOCK_CALLER;
        }
        if (this.mService.mUserController.isUserStoppingOrShuttingDownLocked(r.userId)) {
            return VALIDATE_WAKE_LOCK_CALLER;
        }
        return RESTORE_FROM_RECENTS;
    }

    final ArrayList<ActivityRecord> processStoppingActivitiesLocked(boolean remove) {
        ArrayList<ActivityRecord> stops = null;
        boolean nowVisible = allResumedActivitiesVisible();
        for (int activityNdx = this.mStoppingActivities.size() - 1; activityNdx >= 0; activityNdx--) {
            ActivityRecord s = (ActivityRecord) this.mStoppingActivities.get(activityNdx);
            boolean waitingVisible = this.mWaitingVisibleActivities.contains(s);
            if (ActivityManagerDebugConfig.DEBUG_STATES) {
                Slog.v(TAG, "Stopping " + s + ": nowVisible=" + nowVisible + " waitingVisible=" + waitingVisible + " finishing=" + s.finishing);
            }
            if (waitingVisible && nowVisible) {
                this.mWaitingVisibleActivities.remove(s);
                if (s.finishing) {
                    if (ActivityManagerDebugConfig.DEBUG_STATES) {
                        Slog.v(TAG, "Before stopping, can hide: " + s);
                    }
                    this.mWindowManager.setAppVisibility(s.appToken, VALIDATE_WAKE_LOCK_CALLER);
                }
            }
            if ((!waitingVisible || this.mService.isSleepingOrShuttingDownLocked()) && remove) {
                if (ActivityManagerDebugConfig.DEBUG_STATES) {
                    Slog.v(TAG, "Ready to stop: " + s);
                }
                if (stops == null) {
                    stops = new ArrayList();
                }
                stops.add(s);
                this.mStoppingActivities.remove(activityNdx);
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
            case ACTIVITY_RESTRICTION_NONE /*0*/:
                return "NONE";
            case ACTIVITY_RESTRICTION_PERMISSION /*1*/:
                return "LOCKED";
            case ACTIVITY_RESTRICTION_APPOP /*2*/:
                return "PINNED";
            default:
                return "unknown=" + this.mLockTaskModeState;
        }
    }

    public void dump(PrintWriter pw, String prefix) {
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
            pw.println(" mLockTaskPackages (userId:packages)=");
            for (int i = ACTIVITY_RESTRICTION_NONE; i < packages.size(); i += ACTIVITY_RESTRICTION_PERMISSION) {
                pw.print(prefix);
                pw.print(prefix);
                pw.print(packages.keyAt(i));
                pw.print(":");
                pw.println(Arrays.toString((Object[]) packages.valueAt(i)));
            }
        }
        pw.println(" mLockTaskModeTasks" + this.mLockTaskModeTasks);
    }

    ArrayList<ActivityRecord> getDumpActivitiesLocked(String name) {
        return this.mFocusedStack.getDumpActivitiesLocked(name);
    }

    static boolean printThisActivity(PrintWriter pw, ActivityRecord activity, String dumpPackage, boolean needSep, String prefix) {
        if (activity == null || (dumpPackage != null && !dumpPackage.equals(activity.packageName))) {
            return VALIDATE_WAKE_LOCK_CALLER;
        }
        if (needSep) {
            pw.println();
        }
        pw.print(prefix);
        pw.println(activity);
        return RESTORE_FROM_RECENTS;
    }

    boolean dumpActivitiesLocked(FileDescriptor fd, PrintWriter pw, boolean dumpAll, boolean dumpClient, String dumpPackage) {
        int printed = ACTIVITY_RESTRICTION_NONE;
        boolean needSep = VALIDATE_WAKE_LOCK_CALLER;
        for (int displayNdx = ACTIVITY_RESTRICTION_NONE; displayNdx < this.mActivityDisplays.size(); displayNdx += ACTIVITY_RESTRICTION_PERMISSION) {
            ActivityDisplay activityDisplay = (ActivityDisplay) this.mActivityDisplays.valueAt(displayNdx);
            pw.print("Display #");
            pw.print(activityDisplay.mDisplayId);
            pw.println(" (activities from top to bottom):");
            ArrayList<ActivityStack> stacks = activityDisplay.mStacks;
            for (int stackNdx = stacks.size() - 1; stackNdx >= 0; stackNdx--) {
                ActivityStack stack = (ActivityStack) stacks.get(stackNdx);
                StringBuilder stringBuilder = new StringBuilder(DumpState.DUMP_PACKAGES);
                stringBuilder.append("  Stack #");
                stringBuilder.append(stack.mStackId);
                stringBuilder.append(":");
                stringBuilder.append("\n");
                stringBuilder.append("  mFullscreen=").append(stack.mFullscreen);
                stringBuilder.append("\n");
                stringBuilder.append("  mBounds=").append(stack.mBounds);
                printed = (printed | stack.dumpActivitiesLocked(fd, pw, dumpAll, dumpClient, dumpPackage, needSep, stringBuilder.toString())) | dumpHistoryList(fd, pw, stack.mLRUActivities, "    ", "Run", VALIDATE_WAKE_LOCK_CALLER, dumpAll ? VALIDATE_WAKE_LOCK_CALLER : RESTORE_FROM_RECENTS, VALIDATE_WAKE_LOCK_CALLER, dumpPackage, RESTORE_FROM_RECENTS, "    Running activities (most recent first):", null);
                needSep = printed;
                if (printThisActivity(pw, stack.mPausingActivity, dumpPackage, printed, "    mPausingActivity: ")) {
                    printed = ACTIVITY_RESTRICTION_PERMISSION;
                    needSep = VALIDATE_WAKE_LOCK_CALLER;
                }
                if (printThisActivity(pw, stack.mResumedActivity, dumpPackage, needSep, "    mResumedActivity: ")) {
                    printed = ACTIVITY_RESTRICTION_PERMISSION;
                    needSep = VALIDATE_WAKE_LOCK_CALLER;
                }
                if (dumpAll) {
                    if (printThisActivity(pw, stack.mLastPausedActivity, dumpPackage, needSep, "    mLastPausedActivity: ")) {
                        printed = ACTIVITY_RESTRICTION_PERMISSION;
                        needSep = RESTORE_FROM_RECENTS;
                    }
                    printed |= printThisActivity(pw, stack.mLastNoHistoryActivity, dumpPackage, needSep, "    mLastNoHistoryActivity: ");
                }
                needSep = printed;
            }
        }
        return ((((printed | dumpHistoryList(fd, pw, this.mFinishingActivities, "  ", "Fin", VALIDATE_WAKE_LOCK_CALLER, dumpAll ? VALIDATE_WAKE_LOCK_CALLER : RESTORE_FROM_RECENTS, VALIDATE_WAKE_LOCK_CALLER, dumpPackage, RESTORE_FROM_RECENTS, "  Activities waiting to finish:", null)) | dumpHistoryList(fd, pw, this.mStoppingActivities, "  ", "Stop", VALIDATE_WAKE_LOCK_CALLER, dumpAll ? VALIDATE_WAKE_LOCK_CALLER : RESTORE_FROM_RECENTS, VALIDATE_WAKE_LOCK_CALLER, dumpPackage, RESTORE_FROM_RECENTS, "  Activities waiting to stop:", null)) | dumpHistoryList(fd, pw, this.mWaitingVisibleActivities, "  ", "Wait", VALIDATE_WAKE_LOCK_CALLER, dumpAll ? VALIDATE_WAKE_LOCK_CALLER : RESTORE_FROM_RECENTS, VALIDATE_WAKE_LOCK_CALLER, dumpPackage, RESTORE_FROM_RECENTS, "  Activities waiting for another to become visible:", null)) | dumpHistoryList(fd, pw, this.mGoingToSleepActivities, "  ", "Sleep", VALIDATE_WAKE_LOCK_CALLER, dumpAll ? VALIDATE_WAKE_LOCK_CALLER : RESTORE_FROM_RECENTS, VALIDATE_WAKE_LOCK_CALLER, dumpPackage, RESTORE_FROM_RECENTS, "  Activities waiting to sleep:", null)) | dumpHistoryList(fd, pw, this.mGoingToSleepActivities, "  ", "Sleep", VALIDATE_WAKE_LOCK_CALLER, dumpAll ? VALIDATE_WAKE_LOCK_CALLER : RESTORE_FROM_RECENTS, VALIDATE_WAKE_LOCK_CALLER, dumpPackage, RESTORE_FROM_RECENTS, "  Activities waiting to sleep:", null);
    }

    static boolean dumpHistoryList(FileDescriptor fd, PrintWriter pw, List<ActivityRecord> list, String prefix, String label, boolean complete, boolean brief, boolean client, String dumpPackage, boolean needNL, String header1, String header2) {
        TaskRecord lastTask = null;
        String innerPrefix = null;
        String[] args = null;
        boolean printed = VALIDATE_WAKE_LOCK_CALLER;
        for (int i = list.size() - 1; i >= 0; i--) {
            ActivityRecord r = (ActivityRecord) list.get(i);
            if (dumpPackage != null) {
                if (!dumpPackage.equals(r.packageName)) {
                    continue;
                }
            }
            if (innerPrefix == null) {
                innerPrefix = prefix + "      ";
                args = new String[ACTIVITY_RESTRICTION_NONE];
            }
            printed = RESTORE_FROM_RECENTS;
            boolean full = (brief || (!complete && r.isInHistory())) ? VALIDATE_WAKE_LOCK_CALLER : RESTORE_FROM_RECENTS;
            if (needNL) {
                pw.println("");
                needNL = VALIDATE_WAKE_LOCK_CALLER;
            }
            if (header1 != null) {
                pw.println(header1);
                header1 = null;
            }
            if (header2 != null) {
                pw.println(header2);
                header2 = null;
            }
            if (lastTask != r.task) {
                lastTask = r.task;
                pw.print(prefix);
                pw.print(full ? "* " : "  ");
                pw.println(lastTask);
                if (full) {
                    lastTask.dump(pw, prefix + "  ");
                } else if (complete && lastTask.intent != null) {
                    pw.print(prefix);
                    pw.print("  ");
                    pw.println(lastTask.intent.toInsecureStringWithClip());
                }
            }
            pw.print(prefix);
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
            if (!(!client || r.app == null || r.app.thread == null)) {
                pw.flush();
                TransferPipe tp;
                try {
                    tp = new TransferPipe();
                    r.app.thread.dumpActivity(tp.getWriteFd().getFileDescriptor(), r.appToken, innerPrefix, args);
                    tp.go(fd, 2000);
                    tp.kill();
                } catch (IOException e) {
                    pw.println(innerPrefix + "Failure while dumping the activity: " + e);
                } catch (RemoteException e2) {
                    pw.println(innerPrefix + "Got a RemoteException while dumping the activity");
                } catch (Throwable th) {
                    tp.kill();
                }
                needNL = RESTORE_FROM_RECENTS;
            }
        }
        return printed;
    }

    void scheduleIdleTimeoutLocked(ActivityRecord next) {
        if (ActivityManagerDebugConfig.DEBUG_IDLE) {
            Slog.d(TAG_IDLE, "scheduleIdleTimeoutLocked: Callers=" + Debug.getCallers(4));
        }
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(IDLE_TIMEOUT_MSG, next), JobStatus.DEFAULT_TRIGGER_UPDATE_DELAY);
    }

    final void scheduleIdleLocked() {
        this.mHandler.sendEmptyMessage(IDLE_NOW_MSG);
    }

    void removeTimeoutsForActivityLocked(ActivityRecord r) {
        if (ActivityManagerDebugConfig.DEBUG_IDLE) {
            Slog.d(TAG_IDLE, "removeTimeoutsForActivity: Callers=" + Debug.getCallers(4));
        }
        this.mHandler.removeMessages(IDLE_TIMEOUT_MSG, r);
    }

    final void scheduleResumeTopActivities() {
        if (!this.mHandler.hasMessages(RESUME_TOP_ACTIVITY_MSG)) {
            this.mHandler.sendEmptyMessage(RESUME_TOP_ACTIVITY_MSG);
        }
    }

    void removeSleepTimeouts() {
        this.mSleepTimeout = VALIDATE_WAKE_LOCK_CALLER;
        this.mHandler.removeMessages(SLEEP_TIMEOUT_MSG);
    }

    final void scheduleSleepTimeout() {
        removeSleepTimeouts();
        this.mHandler.sendEmptyMessageDelayed(SLEEP_TIMEOUT_MSG, 5000);
    }

    public void onDisplayAdded(int displayId) {
        if (ActivityManagerDebugConfig.DEBUG_STACK) {
            Slog.v(TAG, "Display added displayId=" + displayId);
        }
        this.mHandler.sendMessage(this.mHandler.obtainMessage(HANDLE_DISPLAY_ADDED, displayId, ACTIVITY_RESTRICTION_NONE));
    }

    public void onDisplayRemoved(int displayId) {
        if (ActivityManagerDebugConfig.DEBUG_STACK) {
            Slog.v(TAG, "Display removed displayId=" + displayId);
        }
        this.mHandler.sendMessage(this.mHandler.obtainMessage(HANDLE_DISPLAY_REMOVED, displayId, ACTIVITY_RESTRICTION_NONE));
    }

    public void onDisplayChanged(int displayId) {
        if (ActivityManagerDebugConfig.DEBUG_STACK) {
            Slog.v(TAG, "Display changed displayId=" + displayId);
        }
        this.mHandler.sendMessage(this.mHandler.obtainMessage(HANDLE_DISPLAY_CHANGED, displayId, ACTIVITY_RESTRICTION_NONE));
    }

    private void handleDisplayAdded(int displayId) {
        synchronized (this.mService) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                boolean newDisplay = this.mActivityDisplays.get(displayId) == null ? RESTORE_FROM_RECENTS : VALIDATE_WAKE_LOCK_CALLER;
                if (newDisplay) {
                    ActivityDisplay activityDisplay = new ActivityDisplay(displayId);
                    if (activityDisplay.mDisplay == null) {
                        Slog.w(TAG, "Display " + displayId + " gone before initialization complete");
                        return;
                    }
                    this.mActivityDisplays.put(displayId, activityDisplay);
                    calculateDefaultMinimalSizeOfResizeableTasks(activityDisplay);
                }
                ActivityManagerService.resetPriorityAfterLockedSection();
                if (newDisplay) {
                    this.mWindowManager.onDisplayAdded(displayId);
                }
            } finally {
                ActivityManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    private void calculateDefaultMinimalSizeOfResizeableTasks(ActivityDisplay display) {
        this.mDefaultMinSizeOfResizeableTask = this.mService.mContext.getResources().getDimensionPixelSize(17105226);
    }

    private void handleDisplayRemoved(int displayId) {
        synchronized (this.mService) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                ActivityDisplay activityDisplay = (ActivityDisplay) this.mActivityDisplays.get(displayId);
                if (activityDisplay != null) {
                    ArrayList<ActivityStack> stacks = activityDisplay.mStacks;
                    for (int stackNdx = stacks.size() - 1; stackNdx >= 0; stackNdx--) {
                        ((ActivityStack) stacks.get(stackNdx)).mActivityContainer.detachLocked();
                    }
                    this.mActivityDisplays.remove(displayId);
                }
            } finally {
                ActivityManagerService.resetPriorityAfterLockedSection();
            }
        }
        this.mWindowManager.onDisplayRemoved(displayId);
    }

    private void handleDisplayChanged(int displayId) {
        synchronized (this.mService) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                if (((ActivityDisplay) this.mActivityDisplays.get(displayId)) != null) {
                }
            } finally {
                ActivityManagerService.resetPriorityAfterLockedSection();
            }
        }
        this.mWindowManager.onDisplayChanged(displayId);
    }

    private StackInfo getStackInfoLocked(ActivityStack stack) {
        int indexOf;
        ActivityDisplay display = (ActivityDisplay) this.mActivityDisplays.get(ACTIVITY_RESTRICTION_NONE);
        StackInfo info = new StackInfo();
        this.mWindowManager.getStackBounds(stack.mStackId, info.bounds);
        info.displayId = ACTIVITY_RESTRICTION_NONE;
        info.stackId = stack.mStackId;
        info.userId = stack.mCurrentUser;
        info.visible = stack.getStackVisibilityLocked(null) == ACTIVITY_RESTRICTION_PERMISSION ? RESTORE_FROM_RECENTS : VALIDATE_WAKE_LOCK_CALLER;
        if (display != null) {
            indexOf = display.mStacks.indexOf(stack);
        } else {
            indexOf = ACTIVITY_RESTRICTION_NONE;
        }
        info.position = indexOf;
        ArrayList<TaskRecord> tasks = stack.getAllTasks();
        int numTasks = tasks.size();
        int[] taskIds = new int[numTasks];
        String[] taskNames = new String[numTasks];
        Rect[] taskBounds = new Rect[numTasks];
        int[] taskUserIds = new int[numTasks];
        for (int i = ACTIVITY_RESTRICTION_NONE; i < numTasks; i += ACTIVITY_RESTRICTION_PERMISSION) {
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
                flattenToString = "unknown";
            }
            taskNames[i] = flattenToString;
            taskBounds[i] = new Rect();
            this.mWindowManager.getTaskBounds(task.taskId, taskBounds[i]);
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
        for (int displayNdx = ACTIVITY_RESTRICTION_NONE; displayNdx < this.mActivityDisplays.size(); displayNdx += ACTIVITY_RESTRICTION_PERMISSION) {
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
        return this.mLockTaskModeTasks.size() == ACTIVITY_RESTRICTION_PERMISSION ? this.mLockTaskModeTasks.contains(task) : VALIDATE_WAKE_LOCK_CALLER;
    }

    void removeLockedTaskLocked(TaskRecord task) {
        if (this.mLockTaskModeTasks.remove(task)) {
            if (ActivityManagerDebugConfig.DEBUG_LOCKTASK) {
                Slog.w(TAG_LOCKTASK, "removeLockedTaskLocked: removed " + task);
            }
            if (this.mLockTaskModeTasks.isEmpty()) {
                if (ActivityManagerDebugConfig.DEBUG_LOCKTASK) {
                    Slog.d(TAG_LOCKTASK, "removeLockedTask: task=" + task + " last task, reverting locktask mode. Callers=" + Debug.getCallers(FIT_WITHIN_BOUNDS_DIVIDER));
                }
                Message lockTaskMsg = Message.obtain();
                lockTaskMsg.arg1 = task.userId;
                lockTaskMsg.what = LOCK_TASK_END_MSG;
                this.mHandler.sendMessage(lockTaskMsg);
            }
        }
    }

    void handleNonResizableTaskIfNeeded(TaskRecord task, int preferredStackId, int actualStackId) {
        handleNonResizableTaskIfNeeded(task, preferredStackId, actualStackId, VALIDATE_WAKE_LOCK_CALLER);
    }

    void handleNonResizableTaskIfNeeded(TaskRecord task, int preferredStackId, int actualStackId, boolean forceNonResizable) {
        boolean z = VALIDATE_WAKE_LOCK_CALLER;
        if ((isStackDockedInEffect(actualStackId) || preferredStackId == FIT_WITHIN_BOUNDS_DIVIDER) && !task.isHomeTask()) {
            ActivityRecord topActivity = task.getTopActivity();
            if (!task.canGoInDockedStack() || forceNonResizable || (topActivity != null && isInMultiWinBlackList(topActivity.appInfo.packageName))) {
                this.mService.mHandler.sendEmptyMessage(68);
                if (actualStackId == FIT_WITHIN_BOUNDS_DIVIDER) {
                    z = RESTORE_FROM_RECENTS;
                }
                moveTasksToFullscreenStackLocked(FIT_WITHIN_BOUNDS_DIVIDER, z);
            } else if (!(topActivity == null || !topActivity.isNonResizableOrForced() || topActivity.noDisplay)) {
                String packageName = topActivity.appInfo.packageName;
                ApplicationInfo info = this.mService.getPackageManagerInternalLocked().getApplicationInfo(packageName, task.userId);
                if (info == null || (info.flags & ACTIVITY_RESTRICTION_PERMISSION) == 0) {
                    this.mService.mHandler.obtainMessage(67, task.taskId, ACTIVITY_RESTRICTION_NONE, packageName).sendToTarget();
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
            this.mHandler.sendEmptyMessage(SHOW_LOCK_TASK_ESCAPE_MESSAGE_MSG);
        }
    }

    void setLockTaskModeLocked(TaskRecord task, int lockTaskModeState, String reason, boolean andResume) {
        boolean z = RESTORE_FROM_RECENTS;
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
                lockTaskMsg.what = LOCK_TASK_START_MSG;
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
                if (lockTaskModeState == 0) {
                    z = VALIDATE_WAKE_LOCK_CALLER;
                }
                findTaskToMoveToFrontLocked(task, ACTIVITY_RESTRICTION_NONE, null, reason, z);
                resumeFocusedStackTopActivityLocked();
            } else if (lockTaskModeState != 0) {
                handleNonResizableTaskIfNeeded(task, -1, task.stack.mStackId, RESTORE_FROM_RECENTS);
            }
        }
    }

    boolean isLockTaskModeViolation(TaskRecord task) {
        return isLockTaskModeViolation(task, VALIDATE_WAKE_LOCK_CALLER);
    }

    boolean isLockTaskModeViolation(TaskRecord task, boolean isNewClearTask) {
        boolean z = VALIDATE_WAKE_LOCK_CALLER;
        if (getLockedTaskLocked() == task && !isNewClearTask) {
            return VALIDATE_WAKE_LOCK_CALLER;
        }
        int lockTaskAuth = task.mLockTaskAuth;
        switch (lockTaskAuth) {
            case ACTIVITY_RESTRICTION_NONE /*0*/:
                if (!this.mLockTaskModeTasks.isEmpty()) {
                    z = RESTORE_FROM_RECENTS;
                }
                return z;
            case ACTIVITY_RESTRICTION_PERMISSION /*1*/:
                if (!this.mLockTaskModeTasks.isEmpty()) {
                    z = RESTORE_FROM_RECENTS;
                }
                return z;
            case ACTIVITY_RESTRICTION_APPOP /*2*/:
            case FIT_WITHIN_BOUNDS_DIVIDER /*3*/:
            case H.DO_TRAVERSAL /*4*/:
                return VALIDATE_WAKE_LOCK_CALLER;
            default:
                Slog.w(TAG, "isLockTaskModeViolation: invalid lockTaskAuth value=" + lockTaskAuth);
                return RESTORE_FROM_RECENTS;
        }
    }

    void onLockTaskPackagesUpdatedLocked() {
        boolean didSomething = VALIDATE_WAKE_LOCK_CALLER;
        for (int taskNdx = this.mLockTaskModeTasks.size() - 1; taskNdx >= 0; taskNdx--) {
            TaskRecord lockedTask = (TaskRecord) this.mLockTaskModeTasks.get(taskNdx);
            boolean wasWhitelisted = lockedTask.mLockTaskAuth != ACTIVITY_RESTRICTION_APPOP ? lockedTask.mLockTaskAuth == FIT_WITHIN_BOUNDS_DIVIDER ? RESTORE_FROM_RECENTS : VALIDATE_WAKE_LOCK_CALLER : RESTORE_FROM_RECENTS;
            lockedTask.setLockTaskAuth();
            boolean isWhitelisted = lockedTask.mLockTaskAuth != ACTIVITY_RESTRICTION_APPOP ? lockedTask.mLockTaskAuth == FIT_WITHIN_BOUNDS_DIVIDER ? RESTORE_FROM_RECENTS : VALIDATE_WAKE_LOCK_CALLER : RESTORE_FROM_RECENTS;
            if (wasWhitelisted && !isWhitelisted) {
                if (ActivityManagerDebugConfig.DEBUG_LOCKTASK) {
                    Slog.d(TAG_LOCKTASK, "onLockTaskPackagesUpdated: removing " + lockedTask + " mLockTaskAuth=" + lockedTask.lockTaskAuthToString());
                }
                removeLockedTaskLocked(lockedTask);
                lockedTask.performClearTaskLocked();
                didSomething = RESTORE_FROM_RECENTS;
            }
        }
        for (int displayNdx = this.mActivityDisplays.size() - 1; displayNdx >= 0; displayNdx--) {
            ArrayList<ActivityStack> stacks = ((ActivityDisplay) this.mActivityDisplays.valueAt(displayNdx)).mStacks;
            for (int stackNdx = stacks.size() - 1; stackNdx >= 0; stackNdx--) {
                ((ActivityStack) stacks.get(stackNdx)).onLockTaskPackagesUpdatedLocked();
            }
        }
        ActivityRecord r = topRunningActivityLocked();
        TaskRecord taskRecord = r != null ? r.task : null;
        if (this.mLockTaskModeTasks.isEmpty() && taskRecord != null && taskRecord.mLockTaskAuth == ACTIVITY_RESTRICTION_APPOP) {
            if (ActivityManagerDebugConfig.DEBUG_LOCKTASK) {
                Slog.d(TAG_LOCKTASK, "onLockTaskPackagesUpdated: starting new locktask task=" + taskRecord);
            }
            setLockTaskModeLocked(taskRecord, ACTIVITY_RESTRICTION_PERMISSION, "package updated", VALIDATE_WAKE_LOCK_CALLER);
            didSomething = RESTORE_FROM_RECENTS;
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
    }

    void activityRelaunchingLocked(ActivityRecord r) {
        this.mWindowManager.notifyAppRelaunching(r.appToken);
    }

    void logStackState() {
        this.mActivityMetricsLogger.logWindowState();
    }

    void scheduleReportMultiWindowModeChanged(TaskRecord task) {
        for (int i = task.mActivities.size() - 1; i >= 0; i--) {
            ActivityRecord r = (ActivityRecord) task.mActivities.get(i);
            if (!(r.app == null || r.app.thread == null)) {
                Flog.i(IDLE_NOW_MSG, "add r " + r + " into list of multiwindow activities");
                this.mMultiWindowModeChangedActivities.add(r);
            }
        }
        if (!this.mHandler.hasMessages(REPORT_MULTI_WINDOW_MODE_CHANGED_MSG)) {
            this.mHandler.sendEmptyMessage(REPORT_MULTI_WINDOW_MODE_CHANGED_MSG);
        }
    }

    void scheduleReportPictureInPictureModeChangedIfNeeded(TaskRecord task, ActivityStack prevStack) {
        ActivityStack stack = task.stack;
        if (prevStack != null && prevStack != stack && (prevStack.mStackId == 4 || stack.mStackId == 4)) {
            for (int i = task.mActivities.size() - 1; i >= 0; i--) {
                ActivityRecord r = (ActivityRecord) task.mActivities.get(i);
                if (!(r.app == null || r.app.thread == null)) {
                    this.mPipModeChangedActivities.add(r);
                }
            }
            if (!this.mHandler.hasMessages(REPORT_PIP_MODE_CHANGED_MSG)) {
                this.mHandler.sendEmptyMessage(REPORT_PIP_MODE_CHANGED_MSG);
            }
        }
    }

    void setDockedStackMinimized(boolean minimized) {
        this.mIsDockMinimized = minimized;
        if (!minimized) {
            ActivityStack dockedStack = getStack(FIT_WITHIN_BOUNDS_DIVIDER);
            if (dockedStack != null) {
                ActivityRecord top = dockedStack.topRunningActivityLocked();
                if (top != null && this.mService.mUserController.shouldConfirmCredentials(top.userId)) {
                    this.mService.mActivityStarter.showConfirmDeviceCredential(top.userId);
                }
            }
        }
    }

    private static void fitWithinBounds(Rect bounds, Rect stackBounds) {
        if (stackBounds != null && !stackBounds.contains(bounds)) {
            if (bounds.left < stackBounds.left || bounds.right > stackBounds.right) {
                int maxRight = stackBounds.right - (stackBounds.width() / FIT_WITHIN_BOUNDS_DIVIDER);
                int horizontalDiff = stackBounds.left - bounds.left;
                if (horizontalDiff >= 0 || bounds.left < maxRight) {
                    if (bounds.left + horizontalDiff >= maxRight) {
                    }
                    bounds.left += horizontalDiff;
                    bounds.right += horizontalDiff;
                }
                horizontalDiff = maxRight - bounds.left;
                bounds.left += horizontalDiff;
                bounds.right += horizontalDiff;
            }
            if (bounds.top < stackBounds.top || bounds.bottom > stackBounds.bottom) {
                int maxBottom = stackBounds.bottom - (stackBounds.height() / FIT_WITHIN_BOUNDS_DIVIDER);
                int verticalDiff = stackBounds.top - bounds.top;
                if (verticalDiff >= 0 || bounds.top < maxBottom) {
                    if (bounds.top + verticalDiff >= maxBottom) {
                    }
                    bounds.top += verticalDiff;
                    bounds.bottom += verticalDiff;
                }
                verticalDiff = maxBottom - bounds.top;
                bounds.top += verticalDiff;
                bounds.bottom += verticalDiff;
            }
        }
    }

    ActivityStack findStackBehind(ActivityStack stack) {
        ActivityDisplay display = (ActivityDisplay) this.mActivityDisplays.get(ACTIVITY_RESTRICTION_NONE);
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

    private void setResizingDuringAnimation(int taskId) {
        this.mResizingTasksDuringAnimation.add(Integer.valueOf(taskId));
        this.mWindowManager.setTaskDockedResizing(taskId, RESTORE_FROM_RECENTS);
    }

    final int startActivityFromRecentsInner(int taskId, Bundle bOptions) {
        ActivityOptions activityOptions;
        if (bOptions != null) {
            ActivityOptions activityOptions2 = new ActivityOptions(bOptions);
        } else {
            activityOptions = null;
        }
        int launchStackId = activityOptions != null ? activityOptions.getLaunchStackId() : -1;
        if (launchStackId == 0) {
            throw new IllegalArgumentException("startActivityFromRecentsInner: Task " + taskId + " can't be launch in the home stack.");
        }
        if (launchStackId == FIT_WITHIN_BOUNDS_DIVIDER) {
            this.mWindowManager.setDockedStackCreateState(activityOptions.getDockCreateMode(), null);
            deferUpdateBounds(ACTIVITY_RESTRICTION_NONE);
            this.mWindowManager.prepareAppTransition(19, VALIDATE_WAKE_LOCK_CALLER);
        }
        TaskRecord task = anyTaskForIdLocked(taskId, RESTORE_FROM_RECENTS, launchStackId);
        if (task == null) {
            continueUpdateBounds(ACTIVITY_RESTRICTION_NONE);
            this.mWindowManager.executeAppTransition();
            throw new IllegalArgumentException("startActivityFromRecentsInner: Task " + taskId + " not found.");
        }
        ActivityStack focusedStack = getFocusedStack();
        ActivityRecord topActivity = focusedStack != null ? focusedStack.topActivity() : null;
        if (!(launchStackId == -1 || task.stack.mStackId == launchStackId)) {
            moveTaskToStackLocked(taskId, launchStackId, RESTORE_FROM_RECENTS, RESTORE_FROM_RECENTS, "startActivityFromRecents", RESTORE_FROM_RECENTS);
        }
        if (this.mService.mUserController.shouldConfirmCredentials(task.userId) || task.getRootActivity() == null) {
            int callingUid = task.mCallingUid;
            String callingPackage = task.mCallingPackage;
            Intent intent = task.intent;
            intent.addFlags(DumpState.DUMP_DEXOPT);
            int result = this.mService.startActivityInPackage(callingUid, callingPackage, intent, null, null, null, ACTIVITY_RESTRICTION_NONE, ACTIVITY_RESTRICTION_NONE, bOptions, task.userId, null, task);
            if (launchStackId == FIT_WITHIN_BOUNDS_DIVIDER) {
                setResizingDuringAnimation(task.taskId);
            }
            return result;
        }
        Flog.i(IDLE_NOW_MSG, "task.userId =" + task.userId + ", task.taskId = " + task.taskId + ", task.getRootActivity() = " + task.getRootActivity() + ", task.getTopActivity() = " + task.getTopActivity());
        this.mActivityMetricsLogger.notifyActivityLaunching();
        this.mService.moveTaskToFrontLocked(task.taskId, ACTIVITY_RESTRICTION_NONE, bOptions);
        this.mActivityMetricsLogger.notifyActivityLaunched(ACTIVITY_RESTRICTION_APPOP, task.getTopActivity());
        if (launchStackId == FIT_WITHIN_BOUNDS_DIVIDER) {
            setResizingDuringAnimation(taskId);
        }
        this.mService.mActivityStarter.postStartActivityUncheckedProcessing(task.getTopActivity(), ACTIVITY_RESTRICTION_APPOP, topActivity != null ? topActivity.task.stack.mStackId : -1, topActivity, task.stack);
        return ACTIVITY_RESTRICTION_APPOP;
    }

    public List<IBinder> getTopVisibleActivities() {
        ActivityDisplay display = (ActivityDisplay) this.mActivityDisplays.get(ACTIVITY_RESTRICTION_NONE);
        if (display == null) {
            return Collections.EMPTY_LIST;
        }
        ArrayList<IBinder> topActivityTokens = new ArrayList();
        ArrayList<ActivityStack> stacks = display.mStacks;
        for (int i = stacks.size() - 1; i >= 0; i--) {
            ActivityStack stack = (ActivityStack) stacks.get(i);
            if (stack.getStackVisibilityLocked(null) == ACTIVITY_RESTRICTION_PERMISSION) {
                ActivityRecord top = stack.topActivity();
                if (top != null) {
                    if (stack == this.mFocusedStack) {
                        topActivityTokens.add(ACTIVITY_RESTRICTION_NONE, top.appToken);
                    } else {
                        topActivityTokens.add(top.appToken);
                    }
                }
            }
        }
        return topActivityTokens;
    }
}
