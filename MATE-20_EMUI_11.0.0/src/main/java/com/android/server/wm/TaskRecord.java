package com.android.server.wm;

import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.app.ActivityTaskManager;
import android.app.AppGlobals;
import android.app.TaskInfo;
import android.app.WindowConfiguration;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Debug;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Trace;
import android.os.UserHandle;
import android.provider.Settings;
import android.service.voice.IVoiceInteractionSession;
import android.util.EventLog;
import android.util.Flog;
import android.util.HwMwUtils;
import android.util.HwPCUtils;
import android.util.Slog;
import android.util.proto.ProtoOutputStream;
import android.view.DisplayInfo;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.app.IVoiceInteractor;
import com.android.internal.util.XmlUtils;
import com.android.server.HwServiceExFactory;
import com.android.server.HwServiceFactory;
import com.android.server.wm.ActivityRecord;
import com.android.server.wm.ActivityStack;
import com.android.server.wm.WindowManagerService;
import com.huawei.server.wm.IHwTaskRecordEx;
import huawei.cust.HwCustUtils;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class TaskRecord extends AbsTaskRecord {
    private static final String ATTR_AFFINITY = "affinity";
    private static final String ATTR_ASKEDCOMPATMODE = "asked_compat_mode";
    private static final String ATTR_AUTOREMOVERECENTS = "auto_remove_recents";
    private static final String ATTR_CALLING_PACKAGE = "calling_package";
    private static final String ATTR_CALLING_UID = "calling_uid";
    private static final String ATTR_EFFECTIVE_UID = "effective_uid";
    private static final String ATTR_LASTDESCRIPTION = "last_description";
    private static final String ATTR_LASTTIMEMOVED = "last_time_moved";
    private static final String ATTR_MIN_HEIGHT = "min_height";
    private static final String ATTR_MIN_WIDTH = "min_width";
    private static final String ATTR_NEVERRELINQUISH = "never_relinquish_identity";
    private static final String ATTR_NEXT_AFFILIATION = "next_affiliation";
    private static final String ATTR_NON_FULLSCREEN_BOUNDS = "non_fullscreen_bounds";
    private static final String ATTR_ORIGACTIVITY = "orig_activity";
    private static final String ATTR_PERSIST_TASK_VERSION = "persist_task_version";
    private static final String ATTR_PREV_AFFILIATION = "prev_affiliation";
    private static final String ATTR_REALACTIVITY = "real_activity";
    private static final String ATTR_REALACTIVITY_SUSPENDED = "real_activity_suspended";
    private static final String ATTR_RESIZE_MODE = "resize_mode";
    private static final String ATTR_ROOTHASRESET = "root_has_reset";
    private static final String ATTR_ROOT_AFFINITY = "root_affinity";
    private static final String ATTR_SUPPORTS_PICTURE_IN_PICTURE = "supports_picture_in_picture";
    private static final String ATTR_TASKID = "task_id";
    @Deprecated
    private static final String ATTR_TASKTYPE = "task_type";
    private static final String ATTR_TASK_AFFILIATION = "task_affiliation";
    private static final String ATTR_TASK_AFFILIATION_COLOR = "task_affiliation_color";
    private static final String ATTR_USERID = "user_id";
    private static final String ATTR_USER_SETUP_COMPLETE = "user_setup_complete";
    private static final int INVALID_MIN_SIZE = -1;
    static final int LOCK_TASK_AUTH_DONT_LOCK = 0;
    static final int LOCK_TASK_AUTH_LAUNCHABLE = 2;
    static final int LOCK_TASK_AUTH_LAUNCHABLE_PRIV = 4;
    static final int LOCK_TASK_AUTH_PINNABLE = 1;
    static final int LOCK_TASK_AUTH_WHITELISTED = 3;
    private static final int PERSIST_TASK_VERSION = 1;
    static final int REPARENT_KEEP_STACK_AT_FRONT = 1;
    static final int REPARENT_LEAVE_STACK_IN_PLACE = 2;
    static final int REPARENT_MOVE_STACK_TO_FRONT = 0;
    private static final String SYS_HW_MULTIWIN_FOR_CAMERA = "sys.hw_multiwin_for_camera";
    private static final String TAG = "ActivityTaskManager";
    private static final String TAG_ACTIVITY = "activity";
    private static final String TAG_ADD_REMOVE = "ActivityTaskManager";
    private static final String TAG_AFFINITYINTENT = "affinity_intent";
    private static final String TAG_INTENT = "intent";
    private static final String TAG_LOCKTASK = "ActivityTaskManager";
    private static final String TAG_RECENTS = "ActivityTaskManager";
    private static final String TAG_TASKS = "ActivityTaskManager";
    private static TaskRecordFactory sTaskRecordFactory;
    String affinity;
    Intent affinityIntent;
    boolean askedCompatMode;
    boolean autoRemoveRecents;
    int effectiveUid;
    boolean hasBeenVisible;
    boolean inRecents;
    Intent intent;
    boolean isAvailable;
    boolean isPersistable;
    long lastActiveTime;
    CharSequence lastDescription;
    ActivityManager.TaskDescription lastTaskDescription;
    final ArrayList<ActivityRecord> mActivities;
    int mAffiliatedTaskColor;
    int mAffiliatedTaskId;
    String mCallingPackage;
    int mCallingUid;
    int mDefaultMinSize;
    final Rect mDisplayedBounds;
    HwCustTaskRecord mHwCustTaskRecord;
    IHwTaskRecordEx mHwTaskRecordEx;
    boolean mIsReparenting;
    Rect mLastNonFullscreenBounds;
    long mLastTimeMoved;
    int mLayerRank;
    int mLockTaskAuth;
    int mLockTaskUid;
    int mMinHeight;
    int mMinWidth;
    private boolean mNeverRelinquishIdentity;
    TaskRecord mNextAffiliate;
    int mNextAffiliateTaskId;
    TaskRecord mPrevAffiliate;
    int mPrevAffiliateTaskId;
    int mResizeMode;
    final TaskActivitiesReport mReuseActivitiesReport;
    private boolean mReuseTask;
    ActivityInfo mRootActivityInfo;
    private WindowProcessController mRootProcess;
    final ActivityTaskManagerService mService;
    ActivityStack mStack;
    private boolean mSupportsPictureInPicture;
    Task mTask;
    private final Rect mTmpBounds;
    private Configuration mTmpConfig;
    private final Rect mTmpInsets;
    private final Rect mTmpNonDecorBounds;
    private final Rect mTmpStableBounds;
    boolean mUserSetupComplete;
    int maxRecents;
    int numFullscreen;
    ComponentName origActivity;
    ComponentName realActivity;
    boolean realActivitySuspended;
    String rootAffinity;
    boolean rootWasReset;
    String stringName;
    final int taskId;
    int userId;
    final IVoiceInteractor voiceInteractor;
    final IVoiceInteractionSession voiceSession;

    @Retention(RetentionPolicy.SOURCE)
    @interface ReparentMoveStackMode {
    }

    public TaskRecord(ActivityTaskManagerService service, int _taskId, ActivityInfo info, Intent _intent, IVoiceInteractionSession _voiceSession, IVoiceInteractor _voiceInteractor) {
        this.mLockTaskAuth = 1;
        this.mLockTaskUid = -1;
        this.lastTaskDescription = new ActivityManager.TaskDescription();
        this.isPersistable = false;
        this.mLastTimeMoved = System.currentTimeMillis();
        this.mNeverRelinquishIdentity = true;
        this.mReuseTask = false;
        this.mPrevAffiliateTaskId = -1;
        this.mNextAffiliateTaskId = -1;
        this.mTmpStableBounds = new Rect();
        this.mTmpNonDecorBounds = new Rect();
        this.mTmpBounds = new Rect();
        this.mTmpInsets = new Rect();
        this.mLastNonFullscreenBounds = null;
        this.mLayerRank = -1;
        this.mDisplayedBounds = new Rect();
        this.mTmpConfig = new Configuration();
        this.mReuseActivitiesReport = new TaskActivitiesReport();
        this.mHwTaskRecordEx = null;
        this.mHwCustTaskRecord = (HwCustTaskRecord) HwCustUtils.createObj(HwCustTaskRecord.class, new Object[0]);
        this.mService = service;
        this.userId = UserHandle.getUserId(info.applicationInfo.uid);
        this.taskId = _taskId;
        this.lastActiveTime = SystemClock.elapsedRealtime();
        this.mAffiliatedTaskId = _taskId;
        this.voiceSession = _voiceSession;
        this.voiceInteractor = _voiceInteractor;
        this.isAvailable = true;
        this.mActivities = new ArrayList<>();
        this.mCallingUid = info.applicationInfo.uid;
        this.mCallingPackage = info.packageName;
        setIntent(_intent, info);
        setMinDimensions(info);
        touchActiveTime();
        this.mService.getTaskChangeNotificationController().notifyTaskCreated(_taskId, this.realActivity);
        this.mHwTaskRecordEx = HwServiceExFactory.getHwTaskRecordEx();
    }

    public TaskRecord(ActivityTaskManagerService service, int _taskId, ActivityInfo info, Intent _intent, ActivityManager.TaskDescription _taskDescription) {
        this.mLockTaskAuth = 1;
        this.mLockTaskUid = -1;
        this.lastTaskDescription = new ActivityManager.TaskDescription();
        this.isPersistable = false;
        this.mLastTimeMoved = System.currentTimeMillis();
        this.mNeverRelinquishIdentity = true;
        this.mReuseTask = false;
        this.mPrevAffiliateTaskId = -1;
        this.mNextAffiliateTaskId = -1;
        this.mTmpStableBounds = new Rect();
        this.mTmpNonDecorBounds = new Rect();
        this.mTmpBounds = new Rect();
        this.mTmpInsets = new Rect();
        this.mLastNonFullscreenBounds = null;
        this.mLayerRank = -1;
        this.mDisplayedBounds = new Rect();
        this.mTmpConfig = new Configuration();
        this.mReuseActivitiesReport = new TaskActivitiesReport();
        this.mHwTaskRecordEx = null;
        this.mHwCustTaskRecord = (HwCustTaskRecord) HwCustUtils.createObj(HwCustTaskRecord.class, new Object[0]);
        this.mService = service;
        this.userId = UserHandle.getUserId(info.applicationInfo.uid);
        this.taskId = _taskId;
        this.lastActiveTime = SystemClock.elapsedRealtime();
        this.mAffiliatedTaskId = _taskId;
        this.voiceSession = null;
        this.voiceInteractor = null;
        this.isAvailable = true;
        this.mActivities = new ArrayList<>();
        this.mCallingUid = info.applicationInfo.uid;
        this.mCallingPackage = info.packageName;
        setIntent(_intent, info);
        setMinDimensions(info);
        this.isPersistable = true;
        this.maxRecents = Math.min(Math.max(info.maxRecents, 1), ActivityTaskManager.getMaxAppRecentsLimitStatic());
        this.lastTaskDescription = _taskDescription;
        touchActiveTime();
        this.mService.getTaskChangeNotificationController().notifyTaskCreated(_taskId, this.realActivity);
        this.mHwTaskRecordEx = HwServiceExFactory.getHwTaskRecordEx();
    }

    public TaskRecord(ActivityTaskManagerService service, int _taskId, Intent _intent, Intent _affinityIntent, String _affinity, String _rootAffinity, ComponentName _realActivity, ComponentName _origActivity, boolean _rootWasReset, boolean _autoRemoveRecents, boolean _askedCompatMode, int _userId, int _effectiveUid, String _lastDescription, ArrayList<ActivityRecord> activities, long lastTimeMoved, boolean neverRelinquishIdentity, ActivityManager.TaskDescription _lastTaskDescription, int taskAffiliation, int prevTaskId, int nextTaskId, int taskAffiliationColor, int callingUid, String callingPackage, int resizeMode, boolean supportsPictureInPicture, boolean _realActivitySuspended, boolean userSetupComplete, int minWidth, int minHeight) {
        this.mLockTaskAuth = 1;
        this.mLockTaskUid = -1;
        this.lastTaskDescription = new ActivityManager.TaskDescription();
        this.isPersistable = false;
        this.mLastTimeMoved = System.currentTimeMillis();
        this.mNeverRelinquishIdentity = true;
        this.mReuseTask = false;
        this.mPrevAffiliateTaskId = -1;
        this.mNextAffiliateTaskId = -1;
        this.mTmpStableBounds = new Rect();
        this.mTmpNonDecorBounds = new Rect();
        this.mTmpBounds = new Rect();
        this.mTmpInsets = new Rect();
        this.mLastNonFullscreenBounds = null;
        this.mLayerRank = -1;
        this.mDisplayedBounds = new Rect();
        this.mTmpConfig = new Configuration();
        this.mReuseActivitiesReport = new TaskActivitiesReport();
        this.mHwTaskRecordEx = null;
        this.mHwCustTaskRecord = (HwCustTaskRecord) HwCustUtils.createObj(HwCustTaskRecord.class, new Object[0]);
        this.mService = service;
        this.taskId = _taskId;
        this.intent = _intent;
        this.affinityIntent = _affinityIntent;
        this.affinity = _affinity;
        this.rootAffinity = _rootAffinity;
        this.voiceSession = null;
        this.voiceInteractor = null;
        this.realActivity = _realActivity;
        this.realActivitySuspended = _realActivitySuspended;
        this.origActivity = _origActivity;
        this.rootWasReset = _rootWasReset;
        this.isAvailable = true;
        this.autoRemoveRecents = _autoRemoveRecents;
        this.askedCompatMode = _askedCompatMode;
        this.userId = _userId;
        this.mUserSetupComplete = userSetupComplete;
        this.effectiveUid = _effectiveUid;
        this.lastActiveTime = SystemClock.elapsedRealtime();
        this.lastDescription = _lastDescription;
        this.mActivities = activities;
        this.mLastTimeMoved = lastTimeMoved;
        this.mNeverRelinquishIdentity = neverRelinquishIdentity;
        this.lastTaskDescription = _lastTaskDescription;
        this.mAffiliatedTaskId = taskAffiliation;
        this.mAffiliatedTaskColor = taskAffiliationColor;
        this.mPrevAffiliateTaskId = prevTaskId;
        this.mNextAffiliateTaskId = nextTaskId;
        this.mCallingUid = callingUid;
        this.mCallingPackage = callingPackage;
        this.mResizeMode = resizeMode;
        this.mSupportsPictureInPicture = supportsPictureInPicture;
        this.mMinWidth = minWidth;
        this.mMinHeight = minHeight;
        this.mService.getTaskChangeNotificationController().notifyTaskCreated(_taskId, this.realActivity);
        this.mHwTaskRecordEx = HwServiceExFactory.getHwTaskRecordEx();
    }

    /* access modifiers changed from: package-private */
    public Task getTask() {
        return this.mTask;
    }

    /* access modifiers changed from: package-private */
    public void createTask(boolean onTop, boolean showForAllUsers) {
        if (this.mTask == null) {
            updateOverrideConfigurationFromLaunchBounds();
            TaskStack stack = getStack().getTaskStack();
            if (stack != null) {
                EventLog.writeEvent(31001, Integer.valueOf(this.taskId), Integer.valueOf(stack.mStackId));
                this.mTask = new Task(this.taskId, stack, this.userId, this.mService.mWindowManager, this.mResizeMode, this.mSupportsPictureInPicture, this.lastTaskDescription, this);
                int position = onTop ? Integer.MAX_VALUE : Integer.MIN_VALUE;
                if (!this.mDisplayedBounds.isEmpty()) {
                    this.mTask.setOverrideDisplayedBounds(this.mDisplayedBounds);
                }
                stack.addTask(this.mTask, position, showForAllUsers, onTop);
                return;
            }
            throw new IllegalArgumentException("TaskRecord: invalid stack=" + this.mStack);
        }
        throw new IllegalArgumentException("mTask=" + this.mTask + " already created for task=" + this);
    }

    /* access modifiers changed from: package-private */
    public void setTask(Task task) {
        this.mTask = task;
    }

    /* access modifiers changed from: package-private */
    public void cleanUpResourcesForDestroy() {
        if (this.mActivities.isEmpty()) {
            saveLaunchingStateIfNeeded();
            boolean isVoiceSession = this.voiceSession != null;
            if (isVoiceSession) {
                try {
                    this.voiceSession.taskFinished(this.intent, this.taskId);
                } catch (RemoteException e) {
                }
            }
            if (autoRemoveFromRecents() || isVoiceSession) {
                this.mService.mStackSupervisor.mRecentTasks.remove(this);
            }
            removeWindowContainer();
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void removeWindowContainer() {
        this.mService.getLockTaskController().clearLockedTask(this);
        Task task = this.mTask;
        if (task != null) {
            task.removeIfPossible();
            this.mTask = null;
            if (!getWindowConfiguration().persistTaskBounds()) {
                updateOverrideConfiguration(null);
            }
            this.mService.getTaskChangeNotificationController().notifyTaskRemoved(this.taskId);
        }
    }

    public void onSnapshotChanged(ActivityManager.TaskSnapshot snapshot) {
        this.mService.getTaskChangeNotificationController().notifyTaskSnapshotChanged(this.taskId, snapshot);
    }

    /* access modifiers changed from: package-private */
    public void setResizeMode(int resizeMode) {
        if (this.mResizeMode != resizeMode) {
            this.mResizeMode = resizeMode;
            this.mTask.setResizeable(resizeMode);
            this.mService.mRootActivityContainer.ensureActivitiesVisible(null, 0, false);
            this.mService.mRootActivityContainer.resumeFocusedStacksTopActivities();
        }
    }

    /* access modifiers changed from: package-private */
    public void setTaskDockedResizing(boolean resizing) {
        Task task = this.mTask;
        if (task == null) {
            Slog.w("WindowManager", "setTaskDockedResizing: taskId " + this.taskId + " not found.");
            return;
        }
        task.setTaskDockedResizing(resizing);
    }

    public void requestResize(Rect bounds, int resizeMode) {
        this.mService.resizeTask(this.taskId, bounds, resizeMode);
    }

    /* access modifiers changed from: package-private */
    public boolean resize(Rect bounds, int resizeMode, boolean preserveWindow, boolean deferResume) {
        ActivityRecord r;
        this.mService.mWindowManager.deferSurfaceLayout();
        try {
            if (isResizeable() || HwPCUtils.isHiCarCastMode()) {
                boolean forced = (resizeMode & 2) != 0;
                if (equivalentRequestedOverrideBounds(bounds) && !forced) {
                    this.mService.mWindowManager.continueSurfaceLayout();
                    return true;
                } else if (this.mTask == null) {
                    updateOverrideConfiguration(bounds);
                    if (!inFreeformWindowingMode()) {
                        this.mService.mStackSupervisor.restoreRecentTaskLocked(this, null, false);
                    }
                    this.mService.mWindowManager.continueSurfaceLayout();
                    return true;
                } else if (canResizeToBounds(bounds) || HwPCUtils.isHiCarCastMode()) {
                    Trace.traceBegin(64, "am.resizeTask_" + this.taskId);
                    boolean kept = true;
                    if (updateOverrideConfiguration(bounds) && (r = topRunningActivityLocked()) != null && !deferResume) {
                        kept = r.ensureActivityConfiguration(0, preserveWindow);
                        this.mService.mRootActivityContainer.ensureActivitiesVisible(r, 0, preserveWindow);
                        if (!kept) {
                            this.mService.mRootActivityContainer.resumeFocusedStacksTopActivities();
                        }
                    }
                    this.mTask.resize(kept, forced);
                    saveLaunchingStateIfNeeded();
                    Trace.traceEnd(64);
                    this.mService.mWindowManager.continueSurfaceLayout();
                    return kept;
                } else {
                    throw new IllegalArgumentException("resizeTask: Can not resize task=" + this + " to bounds=" + bounds + " resizeMode=" + this.mResizeMode);
                }
            } else {
                Slog.w("ActivityTaskManager", "resizeTask: task " + this + " not resizeable.");
                return true;
            }
        } finally {
            this.mService.mWindowManager.continueSurfaceLayout();
        }
    }

    /* access modifiers changed from: package-private */
    public void resizeWindowContainer() {
        this.mTask.resize(false, false);
    }

    /* access modifiers changed from: package-private */
    public void getWindowContainerBounds(Rect bounds) {
        Task task = this.mTask;
        if (task != null) {
            task.getBounds(bounds);
        } else {
            bounds.setEmpty();
        }
    }

    /* access modifiers changed from: package-private */
    public boolean reparent(ActivityStack preferredStack, boolean toTop, int moveStackMode, boolean animate, boolean deferResume, String reason) {
        return reparent(preferredStack, toTop ? Integer.MAX_VALUE : 0, moveStackMode, animate, deferResume, true, reason);
    }

    /* access modifiers changed from: package-private */
    public boolean reparent(ActivityStack preferredStack, boolean toTop, int moveStackMode, boolean animate, boolean deferResume, boolean schedulePictureInPictureModeChange, String reason) {
        return reparent(preferredStack, toTop ? Integer.MAX_VALUE : 0, moveStackMode, animate, deferResume, schedulePictureInPictureModeChange, reason);
    }

    /* access modifiers changed from: package-private */
    public boolean reparent(ActivityStack preferredStack, int position, int moveStackMode, boolean animate, boolean deferResume, String reason) {
        return reparent(preferredStack, position, moveStackMode, animate, deferResume, true, reason);
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:102:0x019e A[Catch:{ all -> 0x0329 }] */
    /* JADX WARNING: Removed duplicated region for block: B:118:0x01be A[Catch:{ all -> 0x0329 }] */
    /* JADX WARNING: Removed duplicated region for block: B:119:0x01c0 A[Catch:{ all -> 0x0329 }] */
    /* JADX WARNING: Removed duplicated region for block: B:122:0x01ca  */
    /* JADX WARNING: Removed duplicated region for block: B:125:0x01d1 A[SYNTHETIC, Splitter:B:125:0x01d1] */
    /* JADX WARNING: Removed duplicated region for block: B:129:0x01de  */
    /* JADX WARNING: Removed duplicated region for block: B:135:0x0204 A[Catch:{ all -> 0x01f7 }] */
    /* JADX WARNING: Removed duplicated region for block: B:137:0x0213 A[Catch:{ all -> 0x01f7 }] */
    /* JADX WARNING: Removed duplicated region for block: B:156:0x024b  */
    /* JADX WARNING: Removed duplicated region for block: B:157:0x024d  */
    /* JADX WARNING: Removed duplicated region for block: B:193:0x02c2  */
    /* JADX WARNING: Removed duplicated region for block: B:198:0x02cf  */
    /* JADX WARNING: Removed duplicated region for block: B:200:0x02d3  */
    /* JADX WARNING: Removed duplicated region for block: B:204:0x02e3  */
    /* JADX WARNING: Removed duplicated region for block: B:207:0x02f3 A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:208:0x02f6 A[ORIG_RETURN, RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:214:0x030a  */
    /* JADX WARNING: Removed duplicated region for block: B:62:0x010f A[SYNTHETIC, Splitter:B:62:0x010f] */
    /* JADX WARNING: Removed duplicated region for block: B:71:0x012b A[SYNTHETIC, Splitter:B:71:0x012b] */
    /* JADX WARNING: Removed duplicated region for block: B:87:0x015a A[SYNTHETIC, Splitter:B:87:0x015a] */
    public boolean reparent(ActivityStack preferredStack, int position, int moveStackMode, boolean animate, boolean deferResume, boolean schedulePictureInPictureModeChange, String reason) {
        WindowManagerService windowManager;
        Throwable th;
        boolean isClearBounds;
        ActivityRecord r;
        boolean wasPaused;
        boolean wasFront;
        int toStackWindowingMode;
        boolean z;
        boolean moveStackToFront;
        WindowManagerService windowManager2;
        ActivityStackSupervisor supervisor;
        ActivityRecord topActivity;
        int i;
        int position2;
        int i2;
        ActivityStackSupervisor supervisor2 = this.mService.mStackSupervisor;
        RootActivityContainer root = this.mService.mRootActivityContainer;
        WindowManagerService windowManager3 = this.mService.mWindowManager;
        ActivityStack sourceStack = getStack();
        ActivityStack toStack = supervisor2.getReparentTargetStack(this, preferredStack, position == Integer.MAX_VALUE);
        if (toStack == sourceStack || !canBeLaunchedOnDisplay(toStack.mDisplayId)) {
            return false;
        }
        if (!(!(position == Integer.MAX_VALUE) || toStack.getResumedActivity() == null || toStack.topRunningActivityLocked() == null)) {
            toStack.startPausingLocked(false, false, null, false);
        }
        int toStackWindowingMode2 = toStack.getWindowingMode();
        ActivityRecord topActivity2 = getTopActivity();
        boolean mightReplaceWindow = topActivity2 != null && replaceWindowsOnTaskMove(getWindowingMode(), toStackWindowingMode2);
        if (mightReplaceWindow) {
            windowManager3.setWillReplaceWindow(topActivity2.appToken, animate);
        }
        windowManager3.deferSurfaceLayout();
        boolean kept = true;
        try {
            if (sourceStack.getWindowingMode() == 103) {
                try {
                    if (sourceStack.getWindowingMode() != toStackWindowingMode2) {
                        isClearBounds = true;
                        if (!HwMwUtils.ENABLED && isClearBounds) {
                            HwMwUtils.performPolicy(13, new Object[]{false, true, Integer.valueOf(toStackWindowingMode2), Integer.valueOf(this.taskId), Integer.valueOf(sourceStack.getStackId()), Integer.valueOf(toStack.getStackId())});
                        }
                        r = topRunningActivityLocked();
                        boolean wasFocused = r == null && root.isTopDisplayFocusedStack(sourceStack) && topRunningActivityLocked() == r;
                        boolean wasResumed = r == null && sourceStack.getResumedActivity() == r;
                        if (r != null) {
                            try {
                                if (sourceStack.mPausingActivity == r) {
                                    wasPaused = true;
                                    if (r != null) {
                                        try {
                                            if (sourceStack.isTopStackOnDisplay() && sourceStack.topRunningActivityLocked() == r) {
                                                wasFront = true;
                                                int position3 = toStack.getAdjustedPositionForTask(this, position, null);
                                                if (inHwFreeFormWindowingMode()) {
                                                    try {
                                                        if (!toStack.inHwFreeFormWindowingMode()) {
                                                            List<TaskRecord> removeTasks = new ArrayList<>(1);
                                                            removeTasks.add(this);
                                                            toStackWindowingMode = toStackWindowingMode2;
                                                            try {
                                                                this.mService.mHwATMSEx.dispatchFreeformBallLifeState(removeTasks, "remove");
                                                                if (this.mTask != null) {
                                                                    this.mTask.reparent(toStack.getTaskStack(), position3, moveStackMode == 0);
                                                                    if (moveStackMode != 0) {
                                                                        z = true;
                                                                        if (moveStackMode == 1) {
                                                                            if (!wasFocused) {
                                                                                if (wasFront) {
                                                                                }
                                                                            }
                                                                        }
                                                                        moveStackToFront = false;
                                                                        sourceStack.removeTask(this, reason, !moveStackToFront ? 2 : z);
                                                                        toStack.addTask(this, position3, false, reason);
                                                                        if (schedulePictureInPictureModeChange) {
                                                                            supervisor2.scheduleUpdatePictureInPictureModeIfNeeded(this, sourceStack);
                                                                        }
                                                                        if (this.voiceSession != null) {
                                                                            try {
                                                                                this.voiceSession.taskStarted(this.intent, this.taskId);
                                                                            } catch (RemoteException e) {
                                                                            }
                                                                        }
                                                                        if (r == null) {
                                                                            position2 = toStackWindowingMode;
                                                                            i = true;
                                                                            supervisor = supervisor2;
                                                                            topActivity = topActivity2;
                                                                            windowManager2 = windowManager3;
                                                                            try {
                                                                                toStack.moveToFrontAndResumeStateIfNeeded(r, moveStackToFront, wasResumed, wasPaused, reason);
                                                                            } catch (Throwable th2) {
                                                                                th = th2;
                                                                                windowManager = windowManager2;
                                                                                windowManager.continueSurfaceLayout();
                                                                                throw th;
                                                                            }
                                                                        } else {
                                                                            windowManager2 = windowManager3;
                                                                            position2 = toStackWindowingMode;
                                                                            i = true;
                                                                            supervisor = supervisor2;
                                                                            topActivity = topActivity2;
                                                                        }
                                                                        if (!animate) {
                                                                            this.mService.mStackSupervisor.mNoAnimActivities.add(topActivity);
                                                                        }
                                                                        toStack.prepareFreezingTaskBounds();
                                                                        boolean toStackSplitScreenPrimary = (position2 != 3 || WindowConfiguration.isHwSplitScreenPrimaryWindowingMode(position2)) ? i : false;
                                                                        Rect configBounds = getRequestedOverrideBounds();
                                                                        if ((position2 != i || position2 == 4 || WindowConfiguration.isHwMultiStackWindowingMode(position2)) && !Objects.equals(configBounds, toStack.getRequestedOverrideBounds())) {
                                                                            kept = resize(toStack.getRequestedOverrideBounds(), 0, mightReplaceWindow ? i : false, deferResume);
                                                                        } else if (position2 == 5) {
                                                                            Rect bounds = getLaunchBounds();
                                                                            if (bounds == null) {
                                                                                this.mService.mStackSupervisor.getLaunchParamsController().layoutTask(this, null);
                                                                                bounds = configBounds;
                                                                            }
                                                                            if (!bounds.isEmpty()) {
                                                                                kept = resize(bounds, 2, !mightReplaceWindow ? i : false, deferResume);
                                                                            }
                                                                        } else if (toStackSplitScreenPrimary || position2 == 2) {
                                                                            if (toStackSplitScreenPrimary && moveStackMode == i) {
                                                                                try {
                                                                                    if (!reason.contains("swapDockedAndFullscreenStack")) {
                                                                                        this.mService.mStackSupervisor.moveRecentsStackToFront(reason);
                                                                                    }
                                                                                } catch (Throwable th3) {
                                                                                    th = th3;
                                                                                    windowManager = windowManager2;
                                                                                    windowManager.continueSurfaceLayout();
                                                                                    throw th;
                                                                                }
                                                                            }
                                                                            try {
                                                                                kept = resize(toStack.getRequestedOverrideBounds(), 0, !mightReplaceWindow ? i : false, deferResume);
                                                                            } catch (Throwable th4) {
                                                                                th = th4;
                                                                                windowManager = windowManager2;
                                                                                windowManager.continueSurfaceLayout();
                                                                                throw th;
                                                                            }
                                                                        }
                                                                        windowManager2.continueSurfaceLayout();
                                                                        if (!mightReplaceWindow) {
                                                                            windowManager2.scheduleClearWillReplaceWindows(topActivity.appToken, !kept);
                                                                        }
                                                                        if (deferResume) {
                                                                            i2 = 0;
                                                                            root.ensureActivitiesVisible(null, 0, !mightReplaceWindow);
                                                                            root.resumeFocusedStacksTopActivities();
                                                                        } else {
                                                                            i2 = 0;
                                                                        }
                                                                        supervisor.handleNonResizableTaskIfNeeded(this, preferredStack.getWindowingMode(), i2, toStack);
                                                                        return preferredStack != toStack;
                                                                    }
                                                                    z = true;
                                                                    moveStackToFront = z;
                                                                    sourceStack.removeTask(this, reason, !moveStackToFront ? 2 : z);
                                                                    toStack.addTask(this, position3, false, reason);
                                                                    if (schedulePictureInPictureModeChange) {
                                                                    }
                                                                    if (this.voiceSession != null) {
                                                                    }
                                                                    if (r == null) {
                                                                    }
                                                                    if (!animate) {
                                                                    }
                                                                    try {
                                                                        toStack.prepareFreezingTaskBounds();
                                                                        if (position2 != 3) {
                                                                        }
                                                                        Rect configBounds2 = getRequestedOverrideBounds();
                                                                        if (position2 != i) {
                                                                        }
                                                                        kept = resize(toStack.getRequestedOverrideBounds(), 0, mightReplaceWindow ? i : false, deferResume);
                                                                        windowManager2.continueSurfaceLayout();
                                                                        if (!mightReplaceWindow) {
                                                                        }
                                                                        if (deferResume) {
                                                                        }
                                                                        supervisor.handleNonResizableTaskIfNeeded(this, preferredStack.getWindowingMode(), i2, toStack);
                                                                        if (preferredStack != toStack) {
                                                                        }
                                                                    } catch (Throwable th5) {
                                                                        th = th5;
                                                                        windowManager = windowManager2;
                                                                        windowManager.continueSurfaceLayout();
                                                                        throw th;
                                                                    }
                                                                } else {
                                                                    windowManager = windowManager3;
                                                                    try {
                                                                        Slog.e("WindowManager", "Reparent fail: task is null.");
                                                                        windowManager.continueSurfaceLayout();
                                                                        return false;
                                                                    } catch (Throwable th6) {
                                                                        th = th6;
                                                                        windowManager.continueSurfaceLayout();
                                                                        throw th;
                                                                    }
                                                                }
                                                            } catch (Throwable th7) {
                                                                th = th7;
                                                                windowManager = windowManager3;
                                                            }
                                                        }
                                                    } catch (Throwable th8) {
                                                        th = th8;
                                                        windowManager = windowManager3;
                                                        windowManager.continueSurfaceLayout();
                                                        throw th;
                                                    }
                                                }
                                                toStackWindowingMode = toStackWindowingMode2;
                                                if (this.mTask != null) {
                                                }
                                            }
                                        } catch (Throwable th9) {
                                            th = th9;
                                            windowManager = windowManager3;
                                            windowManager.continueSurfaceLayout();
                                            throw th;
                                        }
                                    }
                                    wasFront = false;
                                    int position32 = toStack.getAdjustedPositionForTask(this, position, null);
                                    if (inHwFreeFormWindowingMode()) {
                                    }
                                    toStackWindowingMode = toStackWindowingMode2;
                                    try {
                                        if (this.mTask != null) {
                                        }
                                    } catch (Throwable th10) {
                                        th = th10;
                                        windowManager = windowManager3;
                                        windowManager.continueSurfaceLayout();
                                        throw th;
                                    }
                                }
                            } catch (Throwable th11) {
                                th = th11;
                                windowManager = windowManager3;
                                windowManager.continueSurfaceLayout();
                                throw th;
                            }
                        }
                        wasPaused = false;
                        if (r != null) {
                        }
                        wasFront = false;
                        int position322 = toStack.getAdjustedPositionForTask(this, position, null);
                        try {
                            if (inHwFreeFormWindowingMode()) {
                            }
                            toStackWindowingMode = toStackWindowingMode2;
                            if (this.mTask != null) {
                            }
                        } catch (Throwable th12) {
                            th = th12;
                            windowManager = windowManager3;
                            windowManager.continueSurfaceLayout();
                            throw th;
                        }
                    }
                } catch (Throwable th13) {
                    th = th13;
                    windowManager = windowManager3;
                    windowManager.continueSurfaceLayout();
                    throw th;
                }
            }
            isClearBounds = false;
            if (!HwMwUtils.ENABLED) {
            }
            r = topRunningActivityLocked();
            if (r == null) {
            }
            if (r == null) {
            }
            if (r != null) {
            }
            wasPaused = false;
            if (r != null) {
            }
            wasFront = false;
            try {
                int position3222 = toStack.getAdjustedPositionForTask(this, position, null);
                if (inHwFreeFormWindowingMode()) {
                }
                toStackWindowingMode = toStackWindowingMode2;
                if (this.mTask != null) {
                }
            } catch (Throwable th14) {
                th = th14;
                windowManager = windowManager3;
                windowManager.continueSurfaceLayout();
                throw th;
            }
        } catch (Throwable th15) {
            th = th15;
            windowManager = windowManager3;
            windowManager.continueSurfaceLayout();
            throw th;
        }
    }

    private static boolean replaceWindowsOnTaskMove(int sourceWindowingMode, int targetWindowingMode) {
        return sourceWindowingMode == 5 || targetWindowingMode == 5;
    }

    /* access modifiers changed from: package-private */
    public void cancelWindowTransition() {
        Task task = this.mTask;
        if (task == null) {
            Slog.w("WindowManager", "cancelWindowTransition: taskId " + this.taskId + " not found.");
            return;
        }
        task.cancelTaskWindowTransition();
    }

    /* access modifiers changed from: package-private */
    public ActivityManager.TaskSnapshot getSnapshot(boolean reducedResolution, boolean restoreFromDisk) {
        return this.mService.mWindowManager.getTaskSnapshot(this.taskId, this.userId, reducedResolution, restoreFromDisk);
    }

    /* access modifiers changed from: package-private */
    public void touchActiveTime() {
        this.lastActiveTime = SystemClock.elapsedRealtime();
    }

    /* access modifiers changed from: package-private */
    public long getInactiveDuration() {
        return SystemClock.elapsedRealtime() - this.lastActiveTime;
    }

    /* access modifiers changed from: package-private */
    public void setIntent(ActivityRecord r) {
        this.mCallingUid = r.launchedFromUid;
        this.mCallingPackage = r.launchedFromPackage;
        setIntent(r.intent, r.info);
        setLockTaskAuth(r);
    }

    private void setIntent(Intent _intent, ActivityInfo info) {
        if (this.intent == null) {
            this.mNeverRelinquishIdentity = (info.flags & 4096) == 0;
        } else if (this.mNeverRelinquishIdentity) {
            return;
        }
        this.affinity = info.taskAffinity;
        if (this.intent == null) {
            this.rootAffinity = this.affinity;
        }
        this.effectiveUid = info.applicationInfo.uid;
        this.stringName = null;
        if (info.targetActivity == null) {
            if (!(_intent == null || (_intent.getSelector() == null && _intent.getSourceBounds() == null))) {
                _intent = new Intent(_intent);
                _intent.setSelector(null);
                _intent.setSourceBounds(null);
            }
            if (ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
                Slog.v("ActivityTaskManager", "Setting Intent of " + this + " to " + _intent);
            }
            this.intent = _intent;
            this.realActivity = _intent != null ? _intent.getComponent() : null;
            this.origActivity = null;
        } else {
            ComponentName targetComponent = new ComponentName(info.packageName, info.targetActivity);
            if (_intent != null) {
                Intent targetIntent = new Intent(_intent);
                targetIntent.setSelector(null);
                targetIntent.setSourceBounds(null);
                if (ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
                    Slog.v("ActivityTaskManager", "Setting Intent of " + this + " to target " + targetIntent);
                }
                this.intent = targetIntent;
                this.realActivity = targetComponent;
                this.origActivity = _intent.getComponent();
            } else {
                this.intent = null;
                this.realActivity = targetComponent;
                this.origActivity = new ComponentName(info.packageName, info.name);
            }
        }
        Intent intent2 = this.intent;
        int intentFlags = intent2 == null ? 0 : intent2.getFlags();
        if ((2097152 & intentFlags) != 0) {
            this.rootWasReset = true;
        }
        this.userId = UserHandle.getUserId(info.applicationInfo.uid);
        this.mUserSetupComplete = Settings.Secure.getIntForUser(this.mService.mContext.getContentResolver(), ATTR_USER_SETUP_COMPLETE, 0, this.userId) != 0;
        if ((info.flags & 8192) != 0) {
            this.autoRemoveRecents = true;
        } else if ((532480 & intentFlags) != 524288) {
            this.autoRemoveRecents = false;
        } else if (info.documentLaunchMode != 0) {
            this.autoRemoveRecents = false;
        } else {
            this.autoRemoveRecents = true;
        }
        this.mResizeMode = info.resizeMode;
        this.mSupportsPictureInPicture = info.supportsPictureInPicture();
    }

    private void setMinDimensions(ActivityInfo info) {
        if (info == null || info.windowLayout == null) {
            this.mMinWidth = -1;
            this.mMinHeight = -1;
            return;
        }
        this.mMinWidth = info.windowLayout.minWidth;
        this.mMinHeight = info.windowLayout.minHeight;
    }

    /* access modifiers changed from: package-private */
    public boolean isSameIntentFilter(ActivityRecord r) {
        Intent intent2;
        Intent intent3 = new Intent(r.intent);
        if (Objects.equals(this.realActivity, r.mActivityComponent) && (intent2 = this.intent) != null) {
            intent3.setComponent(intent2.getComponent());
        }
        return intent3.filterEquals(this.intent);
    }

    /* access modifiers changed from: package-private */
    public boolean returnsToHomeStack() {
        Intent intent2 = this.intent;
        return intent2 != null && (intent2.getFlags() & 268451840) == 268451840;
    }

    /* access modifiers changed from: package-private */
    public void setPrevAffiliate(TaskRecord prevAffiliate) {
        this.mPrevAffiliate = prevAffiliate;
        this.mPrevAffiliateTaskId = prevAffiliate == null ? -1 : prevAffiliate.taskId;
    }

    /* access modifiers changed from: package-private */
    public void setNextAffiliate(TaskRecord nextAffiliate) {
        this.mNextAffiliate = nextAffiliate;
        this.mNextAffiliateTaskId = nextAffiliate == null ? -1 : nextAffiliate.taskId;
    }

    /* access modifiers changed from: package-private */
    public <T extends ActivityStack> T getStack() {
        return (T) this.mStack;
    }

    /* access modifiers changed from: package-private */
    public void setStack(ActivityStack stack) {
        if (stack == null || stack.isInStackLocked(this)) {
            ActivityStack oldStack = this.mStack;
            this.mStack = stack;
            if (oldStack != this.mStack) {
                for (int i = getChildCount() - 1; i >= 0; i--) {
                    ActivityRecord activity = getChildAt(i);
                    if (oldStack != null) {
                        oldStack.onActivityRemovedFromStack(activity);
                    }
                    if (this.mStack != null) {
                        stack.onActivityAddedToStack(activity);
                    }
                }
            }
            onParentChanged();
            return;
        }
        throw new IllegalStateException("Task must be added as a Stack child first.");
    }

    /* access modifiers changed from: package-private */
    public int getStackId() {
        ActivityStack activityStack = this.mStack;
        if (activityStack != null) {
            return activityStack.mStackId;
        }
        return -1;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.wm.ConfigurationContainer
    public int getChildCount() {
        return this.mActivities.size();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.wm.ConfigurationContainer
    public ActivityRecord getChildAt(int index) {
        return this.mActivities.get(index);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.wm.ConfigurationContainer
    public ConfigurationContainer getParent() {
        return this.mStack;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.wm.ConfigurationContainer
    public void onParentChanged() {
        super.onParentChanged();
        this.mService.mRootActivityContainer.updateUIDsPresentOnDisplay();
    }

    private void closeRecentsChain() {
        TaskRecord taskRecord = this.mPrevAffiliate;
        if (taskRecord != null) {
            taskRecord.setNextAffiliate(this.mNextAffiliate);
        }
        TaskRecord taskRecord2 = this.mNextAffiliate;
        if (taskRecord2 != null) {
            taskRecord2.setPrevAffiliate(this.mPrevAffiliate);
        }
        setPrevAffiliate(null);
        setNextAffiliate(null);
    }

    /* access modifiers changed from: package-private */
    public void removedFromRecents() {
        closeRecentsChain();
        if (this.inRecents) {
            this.inRecents = false;
            this.mService.notifyTaskPersisterLocked(this, false);
        }
        clearRootProcess();
        this.mService.mWindowManager.notifyTaskRemovedFromRecents(this.taskId, this.userId);
    }

    /* access modifiers changed from: package-private */
    public void setTaskToAffiliateWith(TaskRecord taskToAffiliateWith) {
        closeRecentsChain();
        this.mAffiliatedTaskId = taskToAffiliateWith.mAffiliatedTaskId;
        this.mAffiliatedTaskColor = taskToAffiliateWith.mAffiliatedTaskColor;
        while (true) {
            if (taskToAffiliateWith.mNextAffiliate == null) {
                break;
            }
            TaskRecord nextRecents = taskToAffiliateWith.mNextAffiliate;
            if (nextRecents.mAffiliatedTaskId != this.mAffiliatedTaskId) {
                Slog.e("ActivityTaskManager", "setTaskToAffiliateWith: nextRecents=" + nextRecents + " affilTaskId=" + nextRecents.mAffiliatedTaskId + " should be " + this.mAffiliatedTaskId);
                if (nextRecents.mPrevAffiliate == taskToAffiliateWith) {
                    nextRecents.setPrevAffiliate(null);
                }
                taskToAffiliateWith.setNextAffiliate(null);
            } else {
                taskToAffiliateWith = nextRecents;
            }
        }
        taskToAffiliateWith.setNextAffiliate(this);
        setPrevAffiliate(taskToAffiliateWith);
        setNextAffiliate(null);
    }

    /* access modifiers changed from: package-private */
    public Intent getBaseIntent() {
        Intent intent2 = this.intent;
        return intent2 != null ? intent2 : this.affinityIntent;
    }

    /* access modifiers changed from: package-private */
    public ActivityRecord getRootActivity() {
        for (int i = 0; i < this.mActivities.size(); i++) {
            ActivityRecord r = this.mActivities.get(i);
            if (!r.finishing) {
                return r;
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public ActivityRecord getTopActivity() {
        return getTopActivity(true);
    }

    /* access modifiers changed from: package-private */
    public ActivityRecord getTopActivity(boolean includeOverlays) {
        for (int i = this.mActivities.size() - 1; i >= 0; i--) {
            ActivityRecord r = this.mActivities.get(i);
            if (!r.finishing && (includeOverlays || !r.mTaskOverlay)) {
                return r;
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public ActivityRecord topRunningActivityLocked() {
        if (this.mStack == null) {
            return null;
        }
        for (int activityNdx = this.mActivities.size() - 1; activityNdx >= 0; activityNdx--) {
            ActivityRecord r = this.mActivities.get(activityNdx);
            if (!r.finishing && r.okToShowLocked()) {
                return r;
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public boolean isVisible() {
        for (int i = this.mActivities.size() - 1; i >= 0; i--) {
            if (this.mActivities.get(i).visible) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean containsAppUid(int uid) {
        for (int i = this.mActivities.size() - 1; i >= 0; i--) {
            if (this.mActivities.get(i).getUid() == uid) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public void getAllRunningVisibleActivitiesLocked(ArrayList<ActivityRecord> outActivities) {
        if (this.mStack != null) {
            for (int activityNdx = this.mActivities.size() - 1; activityNdx >= 0; activityNdx--) {
                ActivityRecord r = this.mActivities.get(activityNdx);
                if (!r.finishing && r.okToShowLocked() && r.visibleIgnoringKeyguard) {
                    outActivities.add(r);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public ActivityRecord topRunningActivityWithStartingWindowLocked() {
        if (this.mStack == null) {
            return null;
        }
        for (int activityNdx = this.mActivities.size() - 1; activityNdx >= 0; activityNdx--) {
            ActivityRecord r = this.mActivities.get(activityNdx);
            if (r.mStartingWindowState == 1 && !r.finishing && r.okToShowLocked()) {
                return r;
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public void getNumRunningActivities(TaskActivitiesReport reportOut) {
        reportOut.reset();
        for (int i = this.mActivities.size() - 1; i >= 0; i--) {
            ActivityRecord r = this.mActivities.get(i);
            if (!r.finishing) {
                reportOut.base = r;
                reportOut.numActivities++;
                if (reportOut.top == null || reportOut.top.isState(ActivityStack.ActivityState.INITIALIZING)) {
                    reportOut.top = r;
                    reportOut.numRunning = 0;
                }
                if (r.attachedToProcess()) {
                    reportOut.numRunning++;
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean okToShowLocked() {
        return this.mService.mStackSupervisor.isCurrentProfileLocked(this.userId) || topRunningActivityLocked() != null;
    }

    /* access modifiers changed from: package-private */
    public final void setFrontOfTask() {
        boolean foundFront = false;
        int numActivities = this.mActivities.size();
        for (int activityNdx = 0; activityNdx < numActivities; activityNdx++) {
            ActivityRecord r = this.mActivities.get(activityNdx);
            if (foundFront || r.finishing) {
                r.frontOfTask = false;
            } else {
                r.frontOfTask = true;
                foundFront = true;
            }
        }
        if (!foundFront && numActivities > 0) {
            this.mActivities.get(0).frontOfTask = true;
        }
    }

    /* access modifiers changed from: package-private */
    public final void moveActivityToFrontLocked(ActivityRecord newTop) {
        if (ActivityTaskManagerDebugConfig.DEBUG_ADD_REMOVE) {
            Slog.i("ActivityTaskManager", "Removing and adding activity " + newTop + " to stack at top callers=" + Debug.getCallers(4));
        }
        this.mActivities.remove(newTop);
        this.mActivities.add(newTop);
        this.mTask.positionChildAtTop(newTop.mAppWindowToken);
        updateEffectiveIntent();
        setFrontOfTask();
    }

    /* access modifiers changed from: package-private */
    public void addActivityToTop(ActivityRecord r) {
        addActivityAtIndex(this.mActivities.size(), r);
    }

    @Override // com.android.server.wm.ConfigurationContainer
    public int getActivityType() {
        int applicationType = super.getActivityType();
        if (applicationType != 0 || this.mActivities.isEmpty()) {
            return applicationType;
        }
        return this.mActivities.get(0).getActivityType();
    }

    /* access modifiers changed from: package-private */
    public void addActivityAtIndex(int index, ActivityRecord r) {
        TaskRecord task = r.getTaskRecord();
        if (task == null || task == this) {
            r.setTask(this);
            if (!this.mActivities.remove(r) && r.fullscreen) {
                this.numFullscreen++;
            }
            if (this.mActivities.isEmpty()) {
                if (r.getActivityType() == 0) {
                    r.setActivityType(1);
                }
                setActivityType(r.getActivityType());
                this.isPersistable = r.isPersistable();
                this.mCallingUid = r.launchedFromUid;
                this.mCallingPackage = r.launchedFromPackage;
                this.maxRecents = Math.min(Math.max(r.info.maxRecents, 1), ActivityTaskManager.getMaxAppRecentsLimitStatic());
            } else {
                r.setActivityType(getActivityType());
            }
            int size = this.mActivities.size();
            if (index == size && size > 0 && this.mActivities.get(size - 1).mTaskOverlay) {
                index--;
            }
            int index2 = Math.min(size, index);
            this.mActivities.add(index2, r);
            updateEffectiveIntent();
            if (r.isPersistable()) {
                this.mService.notifyTaskPersisterLocked(this, false);
            }
            if (r.mAppWindowToken != null) {
                this.mTask.positionChildAt(r.mAppWindowToken, index2);
            }
            this.mService.mRootActivityContainer.updateUIDsPresentOnDisplay();
            return;
        }
        throw new IllegalArgumentException("Can not add r= to task=" + this + " current parent=" + task);
    }

    /* access modifiers changed from: package-private */
    public boolean removeActivity(ActivityRecord r) {
        return removeActivity(r, false);
    }

    /* access modifiers changed from: package-private */
    public boolean removeActivity(ActivityRecord r, boolean reparenting) {
        if (r.getTaskRecord() == this) {
            r.setTask(null, reparenting);
            if (this.mActivities.remove(r) && r.fullscreen) {
                this.numFullscreen--;
            }
            if (r.isPersistable()) {
                this.mService.notifyTaskPersisterLocked(this, false);
            }
            if (inPinnedWindowingMode()) {
                this.mService.getTaskChangeNotificationController().notifyTaskStackChanged();
            }
            if (this.mActivities.isEmpty()) {
                return !this.mReuseTask;
            }
            updateEffectiveIntent();
            return false;
        }
        throw new IllegalArgumentException("Activity=" + r + " does not belong to task=" + this);
    }

    /* access modifiers changed from: package-private */
    public boolean onlyHasTaskOverlayActivities(boolean excludeFinishing) {
        int count = 0;
        for (int i = this.mActivities.size() - 1; i >= 0; i--) {
            ActivityRecord r = this.mActivities.get(i);
            if (!excludeFinishing || !r.finishing) {
                if (!r.mTaskOverlay) {
                    return false;
                }
                count++;
            }
        }
        return count > 0;
    }

    /* access modifiers changed from: package-private */
    public boolean autoRemoveFromRecents() {
        return this.autoRemoveRecents || (this.mActivities.isEmpty() && !this.hasBeenVisible);
    }

    /* access modifiers changed from: package-private */
    public final void performClearTaskAtIndexLocked(int activityNdx, boolean pauseImmediately, String reason) {
        int numActivities = this.mActivities.size();
        while (activityNdx < numActivities) {
            try {
                ActivityRecord r = this.mActivities.get(activityNdx);
                if (!r.finishing) {
                    ActivityStack activityStack = this.mStack;
                    if (activityStack == null) {
                        r.takeFromHistory();
                        this.mActivities.remove(activityNdx);
                        activityNdx--;
                        numActivities--;
                    } else if (activityStack.finishActivityLocked(r, 0, null, reason, false, pauseImmediately)) {
                        activityNdx--;
                        numActivities--;
                    }
                }
            } catch (IndexOutOfBoundsException e) {
                Slog.e("ActivityTaskManager", "performClearTaskAtIndexLocked: IndexOutOfBoundsException!");
            }
            activityNdx++;
        }
    }

    /* access modifiers changed from: package-private */
    public void performClearTaskLocked() {
        this.mReuseTask = true;
        performClearTaskAtIndexLocked(0, false, "clear-task-all");
        this.mReuseTask = false;
    }

    /* access modifiers changed from: package-private */
    public ActivityRecord performClearTaskForReuseLocked(ActivityRecord newR, int launchFlags) {
        this.mReuseTask = true;
        ActivityRecord result = performClearTaskLocked(newR, launchFlags);
        this.mReuseTask = false;
        return result;
    }

    /* access modifiers changed from: package-private */
    public final ActivityRecord performClearTaskLocked(ActivityRecord newR, int launchFlags) {
        ActivityStack activityStack;
        int numActivities = this.mActivities.size();
        int activityNdx = numActivities - 1;
        while (activityNdx >= 0) {
            ActivityRecord r = this.mActivities.get(activityNdx);
            if (!r.finishing && r.mActivityComponent.equals(newR.mActivityComponent)) {
                while (true) {
                    activityNdx++;
                    if (activityNdx >= numActivities) {
                        break;
                    }
                    ActivityRecord r2 = this.mActivities.get(activityNdx);
                    if (!r2.finishing) {
                        ActivityOptions opts = r2.takeOptionsLocked(false);
                        if (opts != null) {
                            r.updateOptionsLocked(opts);
                        }
                        if ((!HwMwUtils.ENABLED || !r2.inHwMagicWindowingMode() || !HwMwUtils.performPolicy(7, new Object[]{r.appToken, Integer.valueOf(newR.intent.getFlags()), r2.appToken}).getBoolean("RESULT_CLEAR_TASK", false)) && (activityStack = this.mStack) != null && activityStack.finishActivityLocked(r2, 0, null, "clear-task-stack", false)) {
                            activityNdx--;
                            numActivities--;
                        }
                    }
                }
                if (r.launchMode != 0 || (launchFlags & 536870912) != 0 || ActivityStarter.isDocumentLaunchesIntoExisting(launchFlags) || r.finishing) {
                    return r;
                }
                ActivityStack activityStack2 = this.mStack;
                if (activityStack2 != null) {
                    activityStack2.finishActivityLocked(r, 0, null, "clear-task-top", false);
                }
                return null;
            }
            activityNdx--;
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public void removeTaskActivitiesLocked(boolean pauseImmediately, String reason) {
        if (this.mStack != null && getChildCount() == 0) {
            this.mStack.removeTask(this, reason, 0);
        }
        performClearTaskAtIndexLocked(0, pauseImmediately, reason);
    }

    /* access modifiers changed from: package-private */
    public String lockTaskAuthToString() {
        int i = this.mLockTaskAuth;
        if (i == 0) {
            return "LOCK_TASK_AUTH_DONT_LOCK";
        }
        if (i == 1) {
            return "LOCK_TASK_AUTH_PINNABLE";
        }
        if (i == 2) {
            return "LOCK_TASK_AUTH_LAUNCHABLE";
        }
        if (i == 3) {
            return "LOCK_TASK_AUTH_WHITELISTED";
        }
        if (i == 4) {
            return "LOCK_TASK_AUTH_LAUNCHABLE_PRIV";
        }
        return "unknown=" + this.mLockTaskAuth;
    }

    /* access modifiers changed from: package-private */
    public void setLockTaskAuth() {
        setLockTaskAuth(getRootActivity());
    }

    private void setLockTaskAuth(ActivityRecord r) {
        int i = 1;
        if (r == null) {
            this.mLockTaskAuth = 1;
            return;
        }
        ComponentName componentName = this.realActivity;
        String pkg = componentName != null ? componentName.getPackageName() : null;
        LockTaskController lockTaskController = this.mService.getLockTaskController();
        int i2 = r.lockTaskLaunchMode;
        if (i2 == 0) {
            if (lockTaskController.isPackageWhitelisted(this.userId, pkg)) {
                i = 3;
            }
            this.mLockTaskAuth = i;
        } else if (i2 == 1) {
            this.mLockTaskAuth = 0;
        } else if (i2 == 2) {
            this.mLockTaskAuth = 4;
        } else if (i2 == 3) {
            if (lockTaskController.isPackageWhitelisted(this.userId, pkg)) {
                i = 2;
            }
            this.mLockTaskAuth = i;
        }
        if (ActivityTaskManagerDebugConfig.DEBUG_LOCKTASK) {
            Slog.d("ActivityTaskManager", "setLockTaskAuth: task=" + this + " mLockTaskAuth=" + lockTaskAuthToString());
        }
    }

    /* access modifiers changed from: protected */
    public boolean isResizeable(boolean checkSupportsPip) {
        if (!this.mService.mForceResizableActivities) {
            IHwActivityTaskManagerServiceEx iHwActivityTaskManagerServiceEx = this.mService.mHwATMSEx;
            ComponentName componentName = this.realActivity;
            if (!iHwActivityTaskManagerServiceEx.isResizableApp(componentName == null ? null : componentName.getPackageName(), this.mResizeMode) && (!checkSupportsPip || !this.mSupportsPictureInPicture)) {
                return false;
            }
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean isResizeable() {
        return isResizeable(true);
    }

    /* access modifiers changed from: package-private */
    public boolean isSupportsHwFreeformWindowingMode() {
        if (this.mService.mSupportsFreeformWindowManagement) {
            if (!this.mService.mForceResizableActivities) {
                IHwActivityTaskManagerServiceEx iHwActivityTaskManagerServiceEx = this.mService.mHwATMSEx;
                ComponentName componentName = this.realActivity;
                if (!iHwActivityTaskManagerServiceEx.isResizableApp(componentName == null ? null : componentName.getPackageName(), this.mResizeMode) || ActivityInfo.isPreserveOrientationMode(this.mResizeMode)) {
                }
            }
            return true;
        }
        return false;
    }

    @Override // com.android.server.wm.ConfigurationContainer
    public boolean supportsSplitScreenWindowingMode() {
        if (!super.supportsSplitScreenWindowingMode() || !this.mService.mSupportsSplitScreenMultiWindow) {
            return false;
        }
        if (!this.mService.mForceResizableActivities) {
            if (!isResizeable(false)) {
                return false;
            }
            IHwActivityTaskManagerServiceEx iHwActivityTaskManagerServiceEx = this.mService.mHwATMSEx;
            ComponentName componentName = this.realActivity;
            if (iHwActivityTaskManagerServiceEx.isHwFreeFormOnlyApp(componentName == null ? null : componentName.getPackageName()) || ActivityInfo.isPreserveOrientationMode(this.mResizeMode)) {
                return false;
            }
        }
        if (!isTopInSplitAndNonResizable()) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean canBeLaunchedOnDisplay(int displayId) {
        return this.mService.mStackSupervisor.canPlaceEntityOnDisplay(displayId, -1, -1, null);
    }

    private boolean canResizeToBounds(Rect bounds) {
        if (bounds == null || !inFreeformWindowingMode()) {
            return true;
        }
        boolean landscape = bounds.width() > bounds.height();
        Rect configBounds = getRequestedOverrideBounds();
        int i = this.mResizeMode;
        if (i == 7) {
            if (configBounds.isEmpty()) {
                return true;
            }
            if (landscape == (configBounds.width() > configBounds.height())) {
                return true;
            }
            return false;
        } else if ((i != 6 || !landscape) && (this.mResizeMode != 5 || landscape)) {
            return true;
        } else {
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isClearingToReuseTask() {
        return this.mReuseTask;
    }

    /* access modifiers changed from: package-private */
    public final ActivityRecord findActivityInHistoryLocked(ActivityRecord r) {
        ComponentName realActivity2 = r.mActivityComponent;
        for (int activityNdx = this.mActivities.size() - 1; activityNdx >= 0; activityNdx--) {
            ActivityRecord candidate = this.mActivities.get(activityNdx);
            if (!candidate.finishing && candidate.mActivityComponent.equals(realActivity2)) {
                return candidate;
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public void updateTaskDescription() {
        int numActivities = this.mActivities.size();
        boolean relinquish = false;
        if (!(numActivities == 0 || (this.mActivities.get(0).info.flags & 4096) == 0)) {
            relinquish = true;
        }
        int activityNdx = Math.min(numActivities, 1);
        while (true) {
            if (activityNdx >= numActivities) {
                break;
            }
            ActivityRecord r = this.mActivities.get(activityNdx);
            if (relinquish && (r.info.flags & 4096) == 0) {
                activityNdx++;
                break;
            }
            if (!(r.intent == null || (r.intent.getFlags() & 524288) == 0)) {
                break;
            }
            activityNdx++;
        }
        if (activityNdx > 0) {
            String label = null;
            String iconFilename = null;
            int iconResource = -1;
            int colorPrimary = 0;
            int colorBackground = 0;
            int statusBarColor = 0;
            int navigationBarColor = 0;
            boolean statusBarContrastWhenTransparent = false;
            boolean navigationBarContrastWhenTransparent = false;
            boolean topActivity = true;
            for (int activityNdx2 = activityNdx - 1; activityNdx2 >= 0; activityNdx2--) {
                ActivityRecord r2 = this.mActivities.get(activityNdx2);
                if (!r2.mTaskOverlay) {
                    if (r2.taskDescription != null) {
                        if (label == null) {
                            label = r2.taskDescription.getLabel();
                        }
                        if (iconResource == -1) {
                            iconResource = r2.taskDescription.getIconResource();
                        }
                        if (iconFilename == null) {
                            iconFilename = r2.taskDescription.getIconFilename();
                        }
                        if (colorPrimary == 0) {
                            colorPrimary = r2.taskDescription.getPrimaryColor();
                        }
                        if (topActivity) {
                            colorBackground = r2.taskDescription.getBackgroundColor();
                            statusBarColor = r2.taskDescription.getStatusBarColor();
                            navigationBarColor = r2.taskDescription.getNavigationBarColor();
                            statusBarContrastWhenTransparent = r2.taskDescription.getEnsureStatusBarContrastWhenTransparent();
                            navigationBarContrastWhenTransparent = r2.taskDescription.getEnsureNavigationBarContrastWhenTransparent();
                        }
                    }
                    topActivity = false;
                }
            }
            this.lastTaskDescription = new ActivityManager.TaskDescription(label, null, iconResource, iconFilename, colorPrimary, colorBackground, statusBarColor, navigationBarColor, statusBarContrastWhenTransparent, navigationBarContrastWhenTransparent);
            Task task = this.mTask;
            if (task != null) {
                task.setTaskDescription(this.lastTaskDescription);
            }
            if (this.taskId == this.mAffiliatedTaskId) {
                this.mAffiliatedTaskColor = this.lastTaskDescription.getPrimaryColor();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public int findEffectiveRootIndex() {
        int effectiveNdx = 0;
        int topActivityNdx = this.mActivities.size() - 1;
        for (int activityNdx = 0; activityNdx <= topActivityNdx; activityNdx++) {
            ActivityRecord r = this.mActivities.get(activityNdx);
            if (!r.finishing) {
                effectiveNdx = activityNdx;
                if ((r.info.flags & 4096) == 0) {
                    break;
                }
            }
        }
        return effectiveNdx;
    }

    /* access modifiers changed from: package-private */
    public void updateEffectiveIntent() {
        setIntent(this.mActivities.get(findEffectiveRootIndex()));
        updateTaskDescription();
    }

    /* access modifiers changed from: package-private */
    public void adjustForMinimalTaskDimensions(Rect bounds, Rect previousBounds) {
        if (bounds != null) {
            int minWidth = this.mMinWidth;
            int minHeight = this.mMinHeight;
            if (!inPinnedWindowingMode() && this.mStack != null) {
                int defaultMinSizeDp = this.mService.mRootActivityContainer.mDefaultMinSizeOfResizeableTaskDp;
                ActivityDisplay display = this.mService.mRootActivityContainer.getActivityDisplay(this.mStack.mDisplayId);
                if (display != null) {
                    int i = (int) (((float) defaultMinSizeDp) * (((float) display.getConfiguration().densityDpi) / 160.0f));
                    if (minWidth == -1) {
                        minWidth = this.mDefaultMinSize;
                    }
                    if (minHeight == -1) {
                        minHeight = this.mDefaultMinSize;
                    }
                } else {
                    return;
                }
            }
            boolean adjustHeight = true;
            boolean adjustWidth = minWidth > bounds.width();
            if (minHeight <= bounds.height()) {
                adjustHeight = false;
            }
            if (adjustWidth || adjustHeight) {
                if (adjustWidth) {
                    if (previousBounds.isEmpty() || bounds.right != previousBounds.right) {
                        bounds.right = bounds.left + minWidth;
                    } else {
                        bounds.left = bounds.right - minWidth;
                    }
                }
                if (!adjustHeight) {
                    return;
                }
                if (previousBounds.isEmpty() || bounds.bottom != previousBounds.bottom) {
                    bounds.bottom = bounds.top + minHeight;
                } else {
                    bounds.top = bounds.bottom - minHeight;
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean updateOverrideConfiguration(Rect bounds) {
        return updateOverrideConfiguration(bounds, null);
    }

    /* access modifiers changed from: package-private */
    public void setLastNonFullscreenBounds(Rect bounds) {
        Rect rect = this.mLastNonFullscreenBounds;
        if (rect == null) {
            this.mLastNonFullscreenBounds = new Rect(bounds);
        } else {
            rect.set(bounds);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean updateOverrideConfiguration(Rect bounds, Rect insetBounds) {
        ActivityStack activityStack;
        boolean hasSetDisplayedBounds = insetBounds != null && !insetBounds.isEmpty();
        if (hasSetDisplayedBounds) {
            setDisplayedBounds(bounds);
        } else {
            setDisplayedBounds(null);
        }
        Rect steadyBounds = hasSetDisplayedBounds ? insetBounds : bounds;
        if (equivalentRequestedOverrideBounds(steadyBounds) && ((activityStack = this.mStack) == null || !HwPCUtils.isExtDynamicStack(activityStack.getStackId()))) {
            return false;
        }
        this.mTmpConfig.setTo(getResolvedOverrideConfiguration());
        setBounds(steadyBounds);
        updateHwOverrideConfiguration(bounds);
        updateHwPCMultiCastOverrideConfiguration(bounds);
        return true ^ this.mTmpConfig.equals(getResolvedOverrideConfiguration());
    }

    /* access modifiers changed from: package-private */
    public void onActivityStateChanged(ActivityRecord record, ActivityStack.ActivityState state, String reason) {
        ActivityStack parent = getStack();
        if (parent != null) {
            parent.onActivityStateChanged(record, state, reason);
        }
    }

    @Override // com.android.server.wm.ConfigurationContainer
    public void onConfigurationChanged(Configuration newParentConfig) {
        ComponentName componentName;
        ActivityStack activityStack;
        Rect rect;
        boolean prevPersistTaskBounds = getWindowConfiguration().persistTaskBounds();
        boolean nextPersistTaskBounds = getRequestedOverrideConfiguration().windowConfiguration.persistTaskBounds() || newParentConfig.windowConfiguration.persistTaskBounds();
        if (!prevPersistTaskBounds && nextPersistTaskBounds && (rect = this.mLastNonFullscreenBounds) != null && !rect.isEmpty()) {
            getRequestedOverrideConfiguration().windowConfiguration.setBounds(this.mLastNonFullscreenBounds);
        }
        boolean wasInMultiWindowMode = inMultiWindowMode();
        int prevWindowMode = getWindowingMode();
        super.onConfigurationChanged(newParentConfig);
        if (wasInMultiWindowMode != inMultiWindowMode()) {
            this.mService.mStackSupervisor.scheduleUpdateMultiWindowMode(this);
        }
        if (inHwFreeFormWindowingMode() && this.realActivity != null && (activityStack = this.mStack) != null && activityStack.getDisplay() != null && this.mStack.getDisplay().mDisplayContent != null) {
            this.mService.mHwATMSEx.updateCameraRotatio(this.realActivity.getPackageName(), this.mStack.getDisplay().mDisplayContent.getRotation(), getConfiguration().windowConfiguration.getRotation());
        } else if (!(inHwFreeFormWindowingMode() || (componentName = this.realActivity) == null || componentName.getPackageName() == null)) {
            if (SystemProperties.get(SYS_HW_MULTIWIN_FOR_CAMERA, "").contains(this.realActivity.getPackageName())) {
                this.mService.mHwATMSEx.getPackageNameRotations().clear();
                SystemProperties.set(SYS_HW_MULTIWIN_FOR_CAMERA, "-1");
                Slog.d("ActivityTaskManager", "set sys.hw_multiwin_for_camera= -1");
            } else if (this.mService.mHwATMSEx.getPackageNameRotations().containsKey(this.realActivity.getPackageName())) {
                this.mService.mHwATMSEx.getPackageNameRotations().remove(this.realActivity.getPackageName());
            }
        }
        if (WindowConfiguration.isHwPCFreeFormWindowingMode(prevWindowMode) && !WindowConfiguration.isHwPCFreeFormWindowingMode(getWindowingMode())) {
            this.mService.mHwATMSEx.ensureTaskRemovedForPCCast(this.taskId);
        }
        if (getWindowConfiguration().persistTaskBounds()) {
            Rect currentBounds = getRequestedOverrideBounds();
            if (!currentBounds.isEmpty()) {
                setLastNonFullscreenBounds(currentBounds);
            }
        }
        saveLaunchingStateIfNeeded();
    }

    /* access modifiers changed from: package-private */
    public void saveLaunchingStateIfNeeded() {
        if (this.hasBeenVisible) {
            int windowingMode = getWindowingMode();
            if (windowingMode == 1 || windowingMode == 5) {
                this.mService.mStackSupervisor.mLaunchParamsPersister.saveTask(this);
            }
        }
    }

    private static void fitWithinBounds(Rect bounds, Rect stackBounds, int overlapPxX, int overlapPxY) {
        if (stackBounds != null && !stackBounds.isEmpty() && !stackBounds.contains(bounds)) {
            int horizontalDiff = 0;
            int overlapLR = Math.min(overlapPxX, bounds.width());
            if (bounds.right < stackBounds.left + overlapLR) {
                horizontalDiff = overlapLR - (bounds.right - stackBounds.left);
            } else if (bounds.left > stackBounds.right - overlapLR) {
                horizontalDiff = -(overlapLR - (stackBounds.right - bounds.left));
            }
            int verticalDiff = 0;
            int overlapTB = Math.min(overlapPxY, bounds.width());
            if (bounds.bottom < stackBounds.top + overlapTB) {
                verticalDiff = overlapTB - (bounds.bottom - stackBounds.top);
            } else if (bounds.top > stackBounds.bottom - overlapTB) {
                verticalDiff = -(overlapTB - (stackBounds.bottom - bounds.top));
            }
            bounds.offset(horizontalDiff, verticalDiff);
        }
    }

    /* access modifiers changed from: package-private */
    public void setDisplayedBounds(Rect bounds) {
        if (bounds == null) {
            this.mDisplayedBounds.setEmpty();
        } else {
            this.mDisplayedBounds.set(bounds);
        }
        Task task = this.mTask;
        if (task != null) {
            task.setOverrideDisplayedBounds(this.mDisplayedBounds.isEmpty() ? null : this.mDisplayedBounds);
        }
    }

    /* access modifiers changed from: package-private */
    public Rect getDisplayedBounds() {
        return this.mDisplayedBounds;
    }

    /* access modifiers changed from: package-private */
    public boolean hasDisplayedBounds() {
        return !this.mDisplayedBounds.isEmpty();
    }

    static void intersectWithInsetsIfFits(Rect inOutBounds, Rect intersectBounds, Rect intersectInsets) {
        if (inOutBounds.right <= intersectBounds.right) {
            inOutBounds.right = Math.min(intersectBounds.right - intersectInsets.right, inOutBounds.right);
        }
        if (inOutBounds.bottom <= intersectBounds.bottom) {
            inOutBounds.bottom = Math.min(intersectBounds.bottom - intersectInsets.bottom, inOutBounds.bottom);
        }
        if (inOutBounds.left >= intersectBounds.left) {
            inOutBounds.left = Math.max(intersectBounds.left + intersectInsets.left, inOutBounds.left);
        }
        if (inOutBounds.top >= intersectBounds.top) {
            inOutBounds.top = Math.max(intersectBounds.top + intersectInsets.top, inOutBounds.top);
        }
    }

    private void calculateInsetFrames(Rect outNonDecorBounds, Rect outStableBounds, Rect bounds, DisplayInfo displayInfo) {
        DisplayPolicy policy;
        outNonDecorBounds.set(bounds);
        outStableBounds.set(bounds);
        if (getStack() != null && getStack().getDisplay() != null && !inHwFreeFormWindowingMode() && (policy = getStack().getDisplay().mDisplayContent.getDisplayPolicy()) != null) {
            this.mTmpBounds.set(0, 0, displayInfo.logicalWidth, displayInfo.logicalHeight);
            policy.getNonDecorInsetsLw(displayInfo.rotation, displayInfo.logicalWidth, displayInfo.logicalHeight, displayInfo.displayCutout, this.mTmpInsets);
            intersectWithInsetsIfFits(outNonDecorBounds, this.mTmpBounds, this.mTmpInsets);
            policy.convertNonDecorInsetsToStableInsets(this.mTmpInsets, displayInfo.rotation);
            intersectWithInsetsIfFits(outStableBounds, this.mTmpBounds, this.mTmpInsets);
        }
    }

    private int getSmallestScreenWidthDpForDockedBounds(Rect bounds) {
        DisplayContent dc = this.mStack.getDisplay().mDisplayContent;
        if (dc != null) {
            return dc.getDockedDividerController().getSmallestWidthDpForBounds(bounds);
        }
        return 0;
    }

    /* access modifiers changed from: package-private */
    public void computeConfigResourceOverrides(Configuration inOutConfig, Configuration parentConfig) {
        computeConfigResourceOverrides(inOutConfig, parentConfig, null);
    }

    /* access modifiers changed from: package-private */
    public void computeConfigResourceOverrides(Configuration inOutConfig, Configuration parentConfig, ActivityRecord.CompatDisplayInsets compatInsets) {
        int i;
        int i2;
        Rect parentAppBounds;
        int windowingMode = inOutConfig.windowConfiguration.getWindowingMode();
        if (windowingMode == 0) {
            windowingMode = parentConfig.windowConfiguration.getWindowingMode();
        }
        float density = (float) inOutConfig.densityDpi;
        if (density == 0.0f) {
            density = (float) parentConfig.densityDpi;
        }
        float density2 = density * 0.00625f;
        Rect bounds = inOutConfig.windowConfiguration.getBounds();
        Rect outAppBounds = inOutConfig.windowConfiguration.getAppBounds();
        if (outAppBounds == null || outAppBounds.isEmpty()) {
            inOutConfig.windowConfiguration.setAppBounds(bounds);
            outAppBounds = inOutConfig.windowConfiguration.getAppBounds();
        }
        boolean insideParentBounds = compatInsets == null && !WindowConfiguration.isHwFreeFormWindowingMode(windowingMode);
        if (!(!insideParentBounds || windowingMode == 5 || windowingMode == 11 || windowingMode == 12 || (parentAppBounds = parentConfig.windowConfiguration.getAppBounds()) == null || parentAppBounds.isEmpty())) {
            outAppBounds.intersect(parentAppBounds);
        }
        if (inOutConfig.screenWidthDp == 0 || inOutConfig.screenHeightDp == 0) {
            if (!insideParentBounds || this.mStack == null) {
                int rotation = parentConfig.windowConfiguration.getRotation();
                if (rotation == -1 || compatInsets == null) {
                    this.mTmpNonDecorBounds.set(outAppBounds);
                    this.mTmpStableBounds.set(outAppBounds);
                } else {
                    this.mTmpNonDecorBounds.set(bounds);
                    this.mTmpStableBounds.set(bounds);
                    compatInsets.getDisplayBoundsByRotation(this.mTmpBounds, rotation);
                    intersectWithInsetsIfFits(this.mTmpNonDecorBounds, this.mTmpBounds, compatInsets.mNonDecorInsets[rotation]);
                    intersectWithInsetsIfFits(this.mTmpStableBounds, this.mTmpBounds, compatInsets.mStableInsets[rotation]);
                    outAppBounds.set(this.mTmpNonDecorBounds);
                }
            } else {
                DisplayInfo di = new DisplayInfo();
                this.mStack.getDisplay().mDisplay.getDisplayInfo(di);
                calculateInsetFrames(this.mTmpNonDecorBounds, this.mTmpStableBounds, bounds, di);
            }
            if (inOutConfig.screenWidthDp == 0) {
                int overrideScreenWidthDp = (int) (((float) this.mTmpStableBounds.width()) / density2);
                if (insideParentBounds) {
                    i2 = Math.min(overrideScreenWidthDp, parentConfig.screenWidthDp);
                } else {
                    i2 = overrideScreenWidthDp;
                }
                inOutConfig.screenWidthDp = i2;
            }
            if (inOutConfig.screenHeightDp == 0) {
                int overrideScreenHeightDp = (int) (((float) this.mTmpStableBounds.height()) / density2);
                if (insideParentBounds) {
                    i = Math.min(overrideScreenHeightDp, parentConfig.screenHeightDp);
                } else {
                    i = overrideScreenHeightDp;
                }
                inOutConfig.screenHeightDp = i;
            }
            if (inOutConfig.smallestScreenWidthDp == 0) {
                if (WindowConfiguration.isFloating(windowingMode) || WindowConfiguration.isHwMultiStackWindowingMode(windowingMode)) {
                    inOutConfig.smallestScreenWidthDp = (int) (((float) Math.min(bounds.width(), bounds.height())) / density2);
                } else if (WindowConfiguration.isSplitScreenWindowingMode(windowingMode)) {
                    inOutConfig.smallestScreenWidthDp = getSmallestScreenWidthDpForDockedBounds(bounds);
                }
            }
        }
        if (inOutConfig.orientation == 0) {
            inOutConfig.orientation = inOutConfig.screenWidthDp <= inOutConfig.screenHeightDp ? 1 : 2;
            if (inHwMagicWindowingMode()) {
                HwMwUtils.performPolicy(80, new Object[]{inOutConfig, Float.valueOf(density2), this});
            }
            if (WindowConfiguration.isHwMultiStackWindowingMode(windowingMode)) {
                if (inOutConfig.orientation == 1) {
                    inOutConfig.windowConfiguration.setRotation(0);
                } else {
                    inOutConfig.windowConfiguration.setRotation(1);
                }
            }
            if (WindowConfiguration.isHwMagicWindowingMode(windowingMode) && inOutConfig.orientation == 1) {
                inOutConfig.windowConfiguration.setRotation(0);
            }
        }
        if (inOutConfig.screenLayout == 0) {
            int compatScreenWidthDp = (int) (((float) this.mTmpNonDecorBounds.width()) / density2);
            int compatScreenHeightDp = (int) (((float) this.mTmpNonDecorBounds.height()) / density2);
            inOutConfig.screenLayout = Configuration.reduceScreenLayout(parentConfig.screenLayout & 63, Math.max(compatScreenHeightDp, compatScreenWidthDp), Math.min(compatScreenHeightDp, compatScreenWidthDp));
        }
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.ConfigurationContainer
    public void resolveOverrideConfiguration(Configuration newParentConfig) {
        this.mTmpBounds.set(getResolvedOverrideConfiguration().windowConfiguration.getBounds());
        super.resolveOverrideConfiguration(newParentConfig);
        int windowingMode = getRequestedOverrideConfiguration().windowConfiguration.getWindowingMode();
        if (windowingMode == 0) {
            windowingMode = newParentConfig.windowConfiguration.getWindowingMode();
        }
        Rect outOverrideBounds = getResolvedOverrideConfiguration().windowConfiguration.getBounds();
        if (windowingMode == 1) {
            computeFullscreenBounds(outOverrideBounds, null, newParentConfig.windowConfiguration.getBounds(), newParentConfig.orientation);
        }
        if (!outOverrideBounds.isEmpty()) {
            if (!inCoordinationWindowingMode() && !inHwMultiStackWindowingMode()) {
                adjustForMinimalTaskDimensions(outOverrideBounds, this.mTmpBounds);
            }
            if (windowingMode == 5) {
                float density = ((float) newParentConfig.densityDpi) / 160.0f;
                Rect parentBounds = new Rect(newParentConfig.windowConfiguration.getBounds());
                ActivityDisplay display = this.mStack.getDisplay();
                if (!(display == null || display.mDisplayContent == null)) {
                    Rect stableBounds = new Rect();
                    display.mDisplayContent.getStableRect(stableBounds);
                    parentBounds.intersect(stableBounds);
                }
                fitWithinBounds(outOverrideBounds, parentBounds, (int) (48.0f * density), (int) (32.0f * density));
                int offsetTop = parentBounds.top - outOverrideBounds.top;
                if (offsetTop > 0) {
                    outOverrideBounds.offset(0, offsetTop);
                }
            }
            computeConfigResourceOverrides(getResolvedOverrideConfiguration(), newParentConfig);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean handlesOrientationChangeFromDescendant() {
        Task task = this.mTask;
        return (task == null || task.getParent() == null || !this.mTask.getParent().handlesOrientationChangeFromDescendant()) ? false : true;
    }

    /* access modifiers changed from: package-private */
    public void computeFullscreenBounds(Rect outBounds, ActivityRecord refActivity, Rect parentBounds, int parentOrientation) {
        outBounds.setEmpty();
        if (!handlesOrientationChangeFromDescendant()) {
            if (refActivity == null) {
                refActivity = getTopActivity(false);
            }
            int overrideOrientation = getRequestedOverrideConfiguration().orientation;
            int forcedOrientation = (overrideOrientation != 0 || refActivity == null) ? overrideOrientation : refActivity.getRequestedConfigurationOrientation();
            if (forcedOrientation != 0 && forcedOrientation != parentOrientation) {
                HwCustTaskRecord hwCustTaskRecord = this.mHwCustTaskRecord;
                if (hwCustTaskRecord == null || !hwCustTaskRecord.isForcedToFullScreen(this.realActivity, forcedOrientation, parentOrientation)) {
                    int parentWidth = parentBounds.width();
                    int parentHeight = parentBounds.height();
                    float aspect = ((float) parentHeight) / ((float) parentWidth);
                    if (forcedOrientation == 2) {
                        int height = (int) (((float) parentWidth) / aspect);
                        int top = parentBounds.centerY() - (height / 2);
                        outBounds.set(parentBounds.left, top, parentBounds.right, top + height);
                        return;
                    }
                    int width = (int) (((float) parentHeight) * aspect);
                    int left = parentBounds.centerX() - (width / 2);
                    outBounds.set(left, parentBounds.top, left + width, parentBounds.bottom);
                    return;
                }
                Slog.w("ActivityTaskManager", "computeFullscreenBounds force to full screen.");
            }
        }
    }

    /* access modifiers changed from: package-private */
    public Rect updateOverrideConfigurationFromLaunchBounds() {
        Rect bounds = getLaunchBounds();
        updateOverrideConfiguration(bounds);
        if (bounds != null && !bounds.isEmpty()) {
            bounds.set(getRequestedOverrideBounds());
        }
        return bounds;
    }

    /* access modifiers changed from: package-private */
    public void updateOverrideConfigurationForStack(ActivityStack inStack) {
        ActivityStack activityStack = this.mStack;
        if (activityStack != null && activityStack == inStack) {
            return;
        }
        if (!inStack.inFreeformWindowingMode()) {
            updateOverrideConfiguration(inStack.getRequestedOverrideBounds());
        } else if (!isResizeable()) {
            throw new IllegalArgumentException("Can not position non-resizeable task=" + this + " in stack=" + inStack);
        } else if (matchParentBounds()) {
            Rect rect = this.mLastNonFullscreenBounds;
            if (rect != null) {
                updateOverrideConfiguration(rect);
            } else {
                this.mService.mStackSupervisor.getLaunchParamsController().layoutTask(this, null);
            }
        }
    }

    /* access modifiers changed from: protected */
    public Rect getLaunchBounds() {
        if (this.mStack == null) {
            return null;
        }
        int windowingMode = getWindowingMode();
        if (!isActivityTypeStandardOrUndefined() || windowingMode == 1 || (windowingMode == 3 && !isResizeable())) {
            if (isResizeable()) {
                return this.mStack.getRequestedOverrideBounds();
            }
            return null;
        } else if (!getWindowConfiguration().persistTaskBounds()) {
            return this.mStack.getRequestedOverrideBounds();
        } else {
            return this.mLastNonFullscreenBounds;
        }
    }

    /* access modifiers changed from: package-private */
    public void addStartingWindowsForVisibleActivities(boolean taskSwitch) {
        for (int activityNdx = this.mActivities.size() - 1; activityNdx >= 0; activityNdx--) {
            ActivityRecord r = this.mActivities.get(activityNdx);
            if (r.visible) {
                Flog.i(301, "keyguardGoingAway--->>>showStartingWindow for r:" + r);
                r.showStartingWindow(null, false, taskSwitch);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setRootProcess(WindowProcessController proc) {
        clearRootProcess();
        Intent intent2 = this.intent;
        if (intent2 != null && (intent2.getFlags() & 8388608) == 0) {
            this.mRootProcess = proc;
            this.mRootProcess.addRecentTask(this);
        }
    }

    /* access modifiers changed from: package-private */
    public void clearRootProcess() {
        WindowProcessController windowProcessController = this.mRootProcess;
        if (windowProcessController != null) {
            windowProcessController.removeRecentTask(this);
            this.mRootProcess = null;
        }
    }

    /* access modifiers changed from: package-private */
    public void clearAllPendingOptions() {
        for (int i = getChildCount() - 1; i >= 0; i--) {
            getChildAt(i).clearOptionsLocked(false);
        }
    }

    /* access modifiers changed from: package-private */
    public void fillTaskInfo(TaskInfo info) {
        ComponentName componentName;
        getNumRunningActivities(this.mReuseActivitiesReport);
        info.userId = this.userId;
        info.stackId = getStackId();
        info.taskId = this.taskId;
        ComponentName componentName2 = null;
        info.combinedTaskIds = this.mStack == null ? null : this.mService.mHwATMSEx.getCombinedSplitScreenTaskIds(this.mStack);
        boolean z = false;
        if (inHwFreeFormWindowingMode()) {
            info.windowMode = getWindowingMode();
        } else {
            ActivityStack activityStack = this.mStack;
            info.windowMode = activityStack == null ? 0 : activityStack.getWindowingMode();
        }
        ActivityStack activityStack2 = this.mStack;
        info.bounds = activityStack2 == null ? null : new Rect(activityStack2.getBounds());
        ActivityStack activityStack3 = this.mStack;
        if (!(activityStack3 == null || activityStack3.getTaskStack() == null)) {
            info.stackScale = this.mStack.getTaskStack().mHwStackScale;
        }
        ActivityStack activityStack4 = this.mStack;
        info.displayId = activityStack4 == null ? -1 : activityStack4.mDisplayId;
        info.isRunning = getTopActivity() != null;
        info.baseIntent = new Intent(getBaseIntent());
        if (this.mReuseActivitiesReport.base != null) {
            componentName = this.mReuseActivitiesReport.base.intent.getComponent();
        } else {
            componentName = null;
        }
        info.baseActivity = componentName;
        if (this.mReuseActivitiesReport.top != null) {
            componentName2 = this.mReuseActivitiesReport.top.mActivityComponent;
        }
        info.topActivity = componentName2;
        info.origActivity = this.origActivity;
        info.realActivity = this.realActivity;
        info.numActivities = this.mReuseActivitiesReport.numActivities;
        info.lastActiveTime = this.lastActiveTime;
        info.taskDescription = new ActivityManager.TaskDescription(this.lastTaskDescription);
        if (supportsSplitScreenWindowingMode() && !this.mService.mHwATMSEx.isPadCastStack(this.mStack)) {
            z = true;
        }
        info.supportsSplitScreenMultiWindow = z;
        info.isSupportsHwFreeForm = isSupportsHwFreeformWindowingMode();
        info.resizeMode = this.mResizeMode;
        info.configuration.setTo(getConfiguration());
        this.mHwTaskRecordEx.updateMagicWindowTaskInfo(this, info);
        if (this.mStack == null && inHwSplitScreenWindowingMode()) {
            info.configuration.windowConfiguration.setWindowingMode(1);
        }
    }

    /* access modifiers changed from: package-private */
    public ActivityManager.RunningTaskInfo getTaskInfo() {
        ActivityManager.RunningTaskInfo info = new ActivityManager.RunningTaskInfo();
        fillTaskInfo(info);
        return info;
    }

    /* access modifiers changed from: package-private */
    public void dump(PrintWriter pw, String prefix) {
        pw.print(prefix);
        pw.print("userId=");
        pw.print(this.userId);
        pw.print(" effectiveUid=");
        UserHandle.formatUid(pw, this.effectiveUid);
        pw.print(" mCallingUid=");
        UserHandle.formatUid(pw, this.mCallingUid);
        pw.print(" mUserSetupComplete=");
        pw.print(this.mUserSetupComplete);
        pw.print(" mCallingPackage=");
        pw.println(this.mCallingPackage);
        if (!(this.affinity == null && this.rootAffinity == null)) {
            pw.print(prefix);
            pw.print("affinity=");
            pw.print(this.affinity);
            String str = this.affinity;
            if (str == null || !str.equals(this.rootAffinity)) {
                pw.print(" root=");
                pw.println(this.rootAffinity);
            } else {
                pw.println();
            }
        }
        if (!(this.voiceSession == null && this.voiceInteractor == null)) {
            pw.print(prefix);
            pw.print("VOICE: session=0x");
            pw.print(Integer.toHexString(System.identityHashCode(this.voiceSession)));
            pw.print(" interactor=0x");
            pw.println(Integer.toHexString(System.identityHashCode(this.voiceInteractor)));
        }
        if (this.intent != null) {
            StringBuilder sb = new StringBuilder(128);
            sb.append(prefix);
            sb.append("intent={");
            this.intent.toShortString(sb, true, true, false, true);
            sb.append('}');
            pw.println(sb.toString());
        }
        if (this.affinityIntent != null) {
            StringBuilder sb2 = new StringBuilder(128);
            sb2.append(prefix);
            sb2.append("affinityIntent={");
            this.affinityIntent.toShortString(sb2, true, true, false, true);
            sb2.append('}');
            pw.println(sb2.toString());
        }
        if (this.origActivity != null) {
            pw.print(prefix);
            pw.print("origActivity=");
            pw.println(this.origActivity.flattenToShortString());
        }
        if (this.realActivity != null) {
            pw.print(prefix);
            pw.print("mActivityComponent=");
            pw.println(this.realActivity.flattenToShortString());
        }
        if (this.autoRemoveRecents || this.isPersistable || !isActivityTypeStandard() || this.numFullscreen != 0) {
            pw.print(prefix);
            pw.print("autoRemoveRecents=");
            pw.print(this.autoRemoveRecents);
            pw.print(" isPersistable=");
            pw.print(this.isPersistable);
            pw.print(" numFullscreen=");
            pw.print(this.numFullscreen);
            pw.print(" activityType=");
            pw.println(getActivityType());
        }
        if (this.rootWasReset || this.mNeverRelinquishIdentity || this.mReuseTask || this.mLockTaskAuth != 1) {
            pw.print(prefix);
            pw.print("rootWasReset=");
            pw.print(this.rootWasReset);
            pw.print(" mNeverRelinquishIdentity=");
            pw.print(this.mNeverRelinquishIdentity);
            pw.print(" mReuseTask=");
            pw.print(this.mReuseTask);
            pw.print(" mLockTaskAuth=");
            pw.println(lockTaskAuthToString());
        }
        if (!(this.mAffiliatedTaskId == this.taskId && this.mPrevAffiliateTaskId == -1 && this.mPrevAffiliate == null && this.mNextAffiliateTaskId == -1 && this.mNextAffiliate == null)) {
            pw.print(prefix);
            pw.print("affiliation=");
            pw.print(this.mAffiliatedTaskId);
            pw.print(" prevAffiliation=");
            pw.print(this.mPrevAffiliateTaskId);
            pw.print(" (");
            TaskRecord taskRecord = this.mPrevAffiliate;
            if (taskRecord == null) {
                pw.print("null");
            } else {
                pw.print(Integer.toHexString(System.identityHashCode(taskRecord)));
            }
            pw.print(") nextAffiliation=");
            pw.print(this.mNextAffiliateTaskId);
            pw.print(" (");
            TaskRecord taskRecord2 = this.mNextAffiliate;
            if (taskRecord2 == null) {
                pw.print("null");
            } else {
                pw.print(Integer.toHexString(System.identityHashCode(taskRecord2)));
            }
            pw.println(")");
        }
        pw.print(prefix);
        pw.print("Activities=");
        pw.println(this.mActivities);
        if (!this.askedCompatMode || !this.inRecents || !this.isAvailable) {
            pw.print(prefix);
            pw.print("askedCompatMode=");
            pw.print(this.askedCompatMode);
            pw.print(" inRecents=");
            pw.print(this.inRecents);
            pw.print(" isAvailable=");
            pw.println(this.isAvailable);
        }
        if (this.lastDescription != null) {
            pw.print(prefix);
            pw.print("lastDescription=");
            pw.println(this.lastDescription);
        }
        if (this.mRootProcess != null) {
            pw.print(prefix);
            pw.print("mRootProcess=");
            pw.println(this.mRootProcess);
        }
        pw.print(prefix);
        pw.print("stackId=");
        pw.println(getStackId());
        pw.print(prefix + "hasBeenVisible=" + this.hasBeenVisible);
        StringBuilder sb3 = new StringBuilder();
        sb3.append(" mResizeMode=");
        sb3.append(ActivityInfo.resizeModeToString(this.mResizeMode));
        pw.print(sb3.toString());
        pw.print(" mSupportsPictureInPicture=" + this.mSupportsPictureInPicture);
        pw.print(" isResizeable=" + isResizeable());
        pw.print(" lastActiveTime=" + this.lastActiveTime);
        pw.println(" (inactive for " + (getInactiveDuration() / 1000) + "s)");
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(128);
        String str = this.stringName;
        if (str != null) {
            sb.append(str);
            sb.append(" U=");
            sb.append(this.userId);
            sb.append(" StackId=");
            sb.append(getStackId());
            sb.append(" sz=");
            sb.append(this.mActivities.size());
            sb.append('}');
            return sb.toString();
        }
        sb.append("TaskRecord{");
        sb.append(Integer.toHexString(System.identityHashCode(this)));
        sb.append(" #");
        sb.append(this.taskId);
        if (this.affinity != null) {
            sb.append(" A=");
            sb.append(this.affinity);
        } else if (this.intent != null) {
            sb.append(" I=");
            sb.append(this.intent.getComponent().flattenToShortString());
        } else {
            Intent intent2 = this.affinityIntent;
            if (intent2 == null || intent2.getComponent() == null) {
                sb.append(" ??");
            } else {
                sb.append(" aI=");
                sb.append(this.affinityIntent.getComponent().flattenToShortString());
            }
        }
        this.stringName = sb.toString();
        return toString();
    }

    @Override // com.android.server.wm.ConfigurationContainer
    public void writeToProto(ProtoOutputStream proto, long fieldId, int logLevel) {
        if (logLevel != 2 || isVisible()) {
            long token = proto.start(fieldId);
            super.writeToProto(proto, 1146756268033L, logLevel);
            proto.write(1120986464258L, this.taskId);
            for (int i = this.mActivities.size() - 1; i >= 0; i--) {
                this.mActivities.get(i).writeToProto(proto, 2246267895811L);
            }
            proto.write(1120986464260L, this.mStack.mStackId);
            Rect rect = this.mLastNonFullscreenBounds;
            if (rect != null) {
                rect.writeToProto(proto, 1146756268037L);
            }
            ComponentName componentName = this.realActivity;
            if (componentName != null) {
                proto.write(1138166333446L, componentName.flattenToShortString());
            }
            ComponentName componentName2 = this.origActivity;
            if (componentName2 != null) {
                proto.write(1138166333447L, componentName2.flattenToShortString());
            }
            proto.write(1120986464264L, getActivityType());
            proto.write(1120986464265L, this.mResizeMode);
            proto.write(1133871366154L, matchParentBounds());
            if (!matchParentBounds()) {
                getRequestedOverrideBounds().writeToProto(proto, 1146756268043L);
            }
            proto.write(1120986464268L, this.mMinWidth);
            proto.write(1120986464269L, this.mMinHeight);
            proto.end(token);
        }
    }

    /* access modifiers changed from: package-private */
    public static class TaskActivitiesReport {
        ActivityRecord base;
        int numActivities;
        int numRunning;
        ActivityRecord top;

        TaskActivitiesReport() {
        }

        /* access modifiers changed from: package-private */
        public void reset() {
            this.numActivities = 0;
            this.numRunning = 0;
            this.base = null;
            this.top = null;
        }
    }

    /* access modifiers changed from: package-private */
    public void saveToXml(XmlSerializer out) throws IOException, XmlPullParserException {
        if (ActivityTaskManagerDebugConfig.DEBUG_RECENTS) {
            Slog.i("ActivityTaskManager", "Saving task=" + this);
        }
        out.attribute(null, ATTR_TASKID, String.valueOf(this.taskId));
        ComponentName componentName = this.realActivity;
        if (componentName != null) {
            out.attribute(null, ATTR_REALACTIVITY, componentName.flattenToShortString());
        }
        out.attribute(null, ATTR_REALACTIVITY_SUSPENDED, String.valueOf(this.realActivitySuspended));
        ComponentName componentName2 = this.origActivity;
        if (componentName2 != null) {
            out.attribute(null, ATTR_ORIGACTIVITY, componentName2.flattenToShortString());
        }
        String str = this.affinity;
        if (str != null) {
            out.attribute(null, ATTR_AFFINITY, str);
            if (!this.affinity.equals(this.rootAffinity)) {
                String str2 = this.rootAffinity;
                if (str2 == null) {
                    str2 = "@";
                }
                out.attribute(null, ATTR_ROOT_AFFINITY, str2);
            }
        } else {
            String str3 = this.rootAffinity;
            if (str3 != null) {
                if (str3 == null) {
                    str3 = "@";
                }
                out.attribute(null, ATTR_ROOT_AFFINITY, str3);
            }
        }
        out.attribute(null, ATTR_ROOTHASRESET, String.valueOf(this.rootWasReset));
        out.attribute(null, ATTR_AUTOREMOVERECENTS, String.valueOf(this.autoRemoveRecents));
        out.attribute(null, ATTR_ASKEDCOMPATMODE, String.valueOf(this.askedCompatMode));
        out.attribute(null, ATTR_USERID, String.valueOf(this.userId));
        out.attribute(null, ATTR_USER_SETUP_COMPLETE, String.valueOf(this.mUserSetupComplete));
        out.attribute(null, ATTR_EFFECTIVE_UID, String.valueOf(this.effectiveUid));
        out.attribute(null, ATTR_LASTTIMEMOVED, String.valueOf(this.mLastTimeMoved));
        out.attribute(null, ATTR_NEVERRELINQUISH, String.valueOf(this.mNeverRelinquishIdentity));
        CharSequence charSequence = this.lastDescription;
        if (charSequence != null) {
            out.attribute(null, ATTR_LASTDESCRIPTION, charSequence.toString());
        }
        ActivityManager.TaskDescription taskDescription = this.lastTaskDescription;
        if (taskDescription != null) {
            taskDescription.saveToXml(out);
        }
        out.attribute(null, ATTR_TASK_AFFILIATION_COLOR, String.valueOf(this.mAffiliatedTaskColor));
        out.attribute(null, ATTR_TASK_AFFILIATION, String.valueOf(this.mAffiliatedTaskId));
        out.attribute(null, ATTR_PREV_AFFILIATION, String.valueOf(this.mPrevAffiliateTaskId));
        out.attribute(null, ATTR_NEXT_AFFILIATION, String.valueOf(this.mNextAffiliateTaskId));
        out.attribute(null, ATTR_CALLING_UID, String.valueOf(this.mCallingUid));
        String str4 = this.mCallingPackage;
        if (str4 == null) {
            str4 = "";
        }
        out.attribute(null, ATTR_CALLING_PACKAGE, str4);
        out.attribute(null, ATTR_RESIZE_MODE, String.valueOf(this.mResizeMode));
        out.attribute(null, ATTR_SUPPORTS_PICTURE_IN_PICTURE, String.valueOf(this.mSupportsPictureInPicture));
        Rect rect = this.mLastNonFullscreenBounds;
        if (rect != null) {
            out.attribute(null, ATTR_NON_FULLSCREEN_BOUNDS, rect.flattenToString());
        }
        out.attribute(null, ATTR_MIN_WIDTH, String.valueOf(this.mMinWidth));
        out.attribute(null, ATTR_MIN_HEIGHT, String.valueOf(this.mMinHeight));
        out.attribute(null, ATTR_PERSIST_TASK_VERSION, String.valueOf(1));
        if (this.affinityIntent != null) {
            out.startTag(null, TAG_AFFINITYINTENT);
            this.affinityIntent.saveToXml(out);
            out.endTag(null, TAG_AFFINITYINTENT);
        }
        if (this.intent != null) {
            out.startTag(null, TAG_INTENT);
            this.intent.saveToXml(out);
            out.endTag(null, TAG_INTENT);
        }
        ArrayList<ActivityRecord> activities = this.mActivities;
        int numActivities = activities.size();
        for (int activityNdx = 0; activityNdx < numActivities; activityNdx++) {
            ActivityRecord r = activities.get(activityNdx);
            if (r.info.persistableMode == 0 || !r.isPersistable()) {
                return;
            }
            if (((r.intent.getFlags() & 524288) | 8192) != 524288 || activityNdx <= 0) {
                out.startTag(null, TAG_ACTIVITY);
                r.saveToXml(out);
                out.endTag(null, TAG_ACTIVITY);
            } else {
                return;
            }
        }
    }

    @VisibleForTesting
    static TaskRecordFactory getTaskRecordFactory() {
        if (sTaskRecordFactory == null) {
            setTaskRecordFactory(new TaskRecordFactory());
        }
        return sTaskRecordFactory;
    }

    static void setTaskRecordFactory(TaskRecordFactory factory) {
        sTaskRecordFactory = factory;
    }

    static TaskRecord create(ActivityTaskManagerService service, int taskId2, ActivityInfo info, Intent intent2, IVoiceInteractionSession voiceSession2, IVoiceInteractor voiceInteractor2) {
        return getTaskRecordFactory().create(service, taskId2, info, intent2, voiceSession2, voiceInteractor2);
    }

    static TaskRecord create(ActivityTaskManagerService service, int taskId2, ActivityInfo info, Intent intent2, ActivityManager.TaskDescription taskDescription) {
        return getTaskRecordFactory().create(service, taskId2, info, intent2, taskDescription);
    }

    static TaskRecord restoreFromXml(XmlPullParser in, ActivityStackSupervisor stackSupervisor) throws IOException, XmlPullParserException {
        return getTaskRecordFactory().restoreFromXml(in, stackSupervisor);
    }

    /* access modifiers changed from: package-private */
    public static class TaskRecordFactory {
        TaskRecordFactory() {
        }

        /* access modifiers changed from: package-private */
        public TaskRecord create(ActivityTaskManagerService service, int taskId, ActivityInfo info, Intent intent, IVoiceInteractionSession voiceSession, IVoiceInteractor voiceInteractor) {
            return HwServiceFactory.createTaskRecord(service, taskId, info, intent, voiceSession, voiceInteractor);
        }

        /* access modifiers changed from: package-private */
        public TaskRecord create(ActivityTaskManagerService service, int taskId, ActivityInfo info, Intent intent, ActivityManager.TaskDescription taskDescription) {
            return HwServiceFactory.createTaskRecord(service, taskId, info, intent, taskDescription);
        }

        /* access modifiers changed from: package-private */
        public TaskRecord create(ActivityTaskManagerService service, int taskId, Intent intent, Intent affinityIntent, String affinity, String rootAffinity, ComponentName realActivity, ComponentName origActivity, boolean rootWasReset, boolean autoRemoveRecents, boolean askedCompatMode, int userId, int effectiveUid, String lastDescription, ArrayList<ActivityRecord> activities, long lastTimeMoved, boolean neverRelinquishIdentity, ActivityManager.TaskDescription lastTaskDescription, int taskAffiliation, int prevTaskId, int nextTaskId, int taskAffiliationColor, int callingUid, String callingPackage, int resizeMode, boolean supportsPictureInPicture, boolean realActivitySuspended, boolean userSetupComplete, int minWidth, int minHeight) {
            return HwServiceFactory.createTaskRecord(service, taskId, intent, affinityIntent, affinity, rootAffinity, realActivity, origActivity, rootWasReset, autoRemoveRecents, askedCompatMode, userId, effectiveUid, lastDescription, activities, lastTimeMoved, neverRelinquishIdentity, lastTaskDescription, taskAffiliation, prevTaskId, nextTaskId, taskAffiliationColor, callingUid, callingPackage, resizeMode, supportsPictureInPicture, realActivitySuspended, userSetupComplete, minWidth, minHeight);
        }

        /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
        /* access modifiers changed from: package-private */
        /* JADX WARNING: Removed duplicated region for block: B:191:0x0474 A[LOOP:2: B:190:0x0472->B:191:0x0474, LOOP_END] */
        /* JADX WARNING: Removed duplicated region for block: B:194:0x0484  */
        public TaskRecord restoreFromXml(XmlPullParser in, ActivityStackSupervisor stackSupervisor) throws IOException, XmlPullParserException {
            String rootAffinity;
            int effectiveUid;
            boolean supportsPictureInPicture;
            int resizeMode;
            int activityNdx;
            char c;
            Intent intent;
            ActivityManager.TaskDescription taskDescription;
            Intent intent2 = null;
            ArrayList<ActivityRecord> activities = new ArrayList<>();
            String rootAffinity2 = null;
            int effectiveUid2 = -1;
            int outerDepth = in.getDepth();
            ActivityManager.TaskDescription taskDescription2 = new ActivityManager.TaskDescription();
            boolean supportsPictureInPicture2 = false;
            ComponentName realActivity = null;
            boolean realActivitySuspended = false;
            ComponentName origActivity = null;
            String affinity = null;
            boolean hasRootAffinity = false;
            boolean rootHasReset = false;
            boolean autoRemoveRecents = false;
            boolean askedCompatMode = false;
            int taskType = 0;
            int userId = 0;
            String lastDescription = null;
            long lastTimeOnTop = 0;
            int taskId = -1;
            int taskAffiliation = -1;
            int taskAffiliationColor = 0;
            int prevTaskId = -1;
            int nextTaskId = -1;
            int callingUid = -1;
            String callingPackage = "";
            int resizeMode2 = 4;
            Rect lastNonFullscreenBounds = null;
            int minWidth = -1;
            int minHeight = -1;
            int persistTaskVersion = 0;
            int attrNdx = in.getAttributeCount() - 1;
            boolean userSetupComplete = true;
            boolean neverRelinquishIdentity = true;
            while (attrNdx >= 0) {
                String attrName = in.getAttributeName(attrNdx);
                String attrValue = in.getAttributeValue(attrNdx);
                switch (attrName.hashCode()) {
                    case -2134816935:
                        if (attrName.equals(TaskRecord.ATTR_ASKEDCOMPATMODE)) {
                            c = '\b';
                            break;
                        }
                        c = 65535;
                        break;
                    case -1556983798:
                        if (attrName.equals(TaskRecord.ATTR_LASTTIMEMOVED)) {
                            c = 14;
                            break;
                        }
                        c = 65535;
                        break;
                    case -1537240555:
                        if (attrName.equals(TaskRecord.ATTR_TASKID)) {
                            c = 0;
                            break;
                        }
                        c = 65535;
                        break;
                    case -1494902876:
                        if (attrName.equals(TaskRecord.ATTR_NEXT_AFFILIATION)) {
                            c = 18;
                            break;
                        }
                        c = 65535;
                        break;
                    case -1292777190:
                        if (attrName.equals(TaskRecord.ATTR_TASK_AFFILIATION_COLOR)) {
                            c = 19;
                            break;
                        }
                        c = 65535;
                        break;
                    case -1138503444:
                        if (attrName.equals(TaskRecord.ATTR_REALACTIVITY_SUSPENDED)) {
                            c = 2;
                            break;
                        }
                        c = 65535;
                        break;
                    case -1124927690:
                        if (attrName.equals(TaskRecord.ATTR_TASK_AFFILIATION)) {
                            c = 16;
                            break;
                        }
                        c = 65535;
                        break;
                    case -974080081:
                        if (attrName.equals(TaskRecord.ATTR_USER_SETUP_COMPLETE)) {
                            c = '\n';
                            break;
                        }
                        c = 65535;
                        break;
                    case -929566280:
                        if (attrName.equals(TaskRecord.ATTR_EFFECTIVE_UID)) {
                            c = 11;
                            break;
                        }
                        c = 65535;
                        break;
                    case -865458610:
                        if (attrName.equals(TaskRecord.ATTR_RESIZE_MODE)) {
                            c = 22;
                            break;
                        }
                        c = 65535;
                        break;
                    case -826243148:
                        if (attrName.equals(TaskRecord.ATTR_MIN_HEIGHT)) {
                            c = 26;
                            break;
                        }
                        c = 65535;
                        break;
                    case -707249465:
                        if (attrName.equals(TaskRecord.ATTR_NON_FULLSCREEN_BOUNDS)) {
                            c = 24;
                            break;
                        }
                        c = 65535;
                        break;
                    case -705269939:
                        if (attrName.equals(TaskRecord.ATTR_ORIGACTIVITY)) {
                            c = 3;
                            break;
                        }
                        c = 65535;
                        break;
                    case -502399667:
                        if (attrName.equals(TaskRecord.ATTR_AUTOREMOVERECENTS)) {
                            c = 7;
                            break;
                        }
                        c = 65535;
                        break;
                    case -360792224:
                        if (attrName.equals(TaskRecord.ATTR_SUPPORTS_PICTURE_IN_PICTURE)) {
                            c = 23;
                            break;
                        }
                        c = 65535;
                        break;
                    case -162744347:
                        if (attrName.equals(TaskRecord.ATTR_ROOT_AFFINITY)) {
                            c = 5;
                            break;
                        }
                        c = 65535;
                        break;
                    case -147132913:
                        if (attrName.equals(TaskRecord.ATTR_USERID)) {
                            c = '\t';
                            break;
                        }
                        c = 65535;
                        break;
                    case -132216235:
                        if (attrName.equals(TaskRecord.ATTR_CALLING_UID)) {
                            c = 20;
                            break;
                        }
                        c = 65535;
                        break;
                    case 180927924:
                        if (attrName.equals(TaskRecord.ATTR_TASKTYPE)) {
                            c = '\f';
                            break;
                        }
                        c = 65535;
                        break;
                    case 331206372:
                        if (attrName.equals(TaskRecord.ATTR_PREV_AFFILIATION)) {
                            c = 17;
                            break;
                        }
                        c = 65535;
                        break;
                    case 541503897:
                        if (attrName.equals(TaskRecord.ATTR_MIN_WIDTH)) {
                            c = 25;
                            break;
                        }
                        c = 65535;
                        break;
                    case 605497640:
                        if (attrName.equals(TaskRecord.ATTR_AFFINITY)) {
                            c = 4;
                            break;
                        }
                        c = 65535;
                        break;
                    case 869221331:
                        if (attrName.equals(TaskRecord.ATTR_LASTDESCRIPTION)) {
                            c = '\r';
                            break;
                        }
                        c = 65535;
                        break;
                    case 1007873193:
                        if (attrName.equals(TaskRecord.ATTR_PERSIST_TASK_VERSION)) {
                            c = 27;
                            break;
                        }
                        c = 65535;
                        break;
                    case 1081438155:
                        if (attrName.equals(TaskRecord.ATTR_CALLING_PACKAGE)) {
                            c = 21;
                            break;
                        }
                        c = 65535;
                        break;
                    case 1457608782:
                        if (attrName.equals(TaskRecord.ATTR_NEVERRELINQUISH)) {
                            c = 15;
                            break;
                        }
                        c = 65535;
                        break;
                    case 1539554448:
                        if (attrName.equals(TaskRecord.ATTR_REALACTIVITY)) {
                            c = 1;
                            break;
                        }
                        c = 65535;
                        break;
                    case 2023391309:
                        if (attrName.equals(TaskRecord.ATTR_ROOTHASRESET)) {
                            c = 6;
                            break;
                        }
                        c = 65535;
                        break;
                    default:
                        c = 65535;
                        break;
                }
                switch (c) {
                    case 0:
                        if (taskId == -1) {
                            taskId = Integer.parseInt(attrValue);
                        }
                        intent = intent2;
                        taskDescription = taskDescription2;
                        break;
                    case 1:
                        realActivity = ComponentName.unflattenFromString(attrValue);
                        intent = intent2;
                        taskDescription = taskDescription2;
                        break;
                    case 2:
                        realActivitySuspended = Boolean.valueOf(attrValue).booleanValue();
                        intent = intent2;
                        taskDescription = taskDescription2;
                        break;
                    case 3:
                        origActivity = ComponentName.unflattenFromString(attrValue);
                        intent = intent2;
                        taskDescription = taskDescription2;
                        break;
                    case 4:
                        affinity = attrValue;
                        intent = intent2;
                        taskDescription = taskDescription2;
                        break;
                    case 5:
                        rootAffinity2 = attrValue;
                        intent = intent2;
                        hasRootAffinity = true;
                        taskDescription = taskDescription2;
                        break;
                    case 6:
                        rootHasReset = Boolean.parseBoolean(attrValue);
                        intent = intent2;
                        taskDescription = taskDescription2;
                        break;
                    case 7:
                        autoRemoveRecents = Boolean.parseBoolean(attrValue);
                        intent = intent2;
                        taskDescription = taskDescription2;
                        break;
                    case '\b':
                        askedCompatMode = Boolean.parseBoolean(attrValue);
                        intent = intent2;
                        taskDescription = taskDescription2;
                        break;
                    case '\t':
                        userId = Integer.parseInt(attrValue);
                        intent = intent2;
                        taskDescription = taskDescription2;
                        break;
                    case '\n':
                        userSetupComplete = Boolean.parseBoolean(attrValue);
                        intent = intent2;
                        taskDescription = taskDescription2;
                        break;
                    case WindowManagerService.H.WINDOW_FREEZE_TIMEOUT /* 11 */:
                        intent = intent2;
                        effectiveUid2 = Integer.parseInt(attrValue);
                        taskDescription = taskDescription2;
                        break;
                    case '\f':
                        intent = intent2;
                        taskType = Integer.parseInt(attrValue);
                        taskDescription = taskDescription2;
                        break;
                    case '\r':
                        lastDescription = attrValue;
                        intent = intent2;
                        taskDescription = taskDescription2;
                        break;
                    case WindowManagerService.H.PERSIST_ANIMATION_SCALE /* 14 */:
                        lastTimeOnTop = Long.parseLong(attrValue);
                        intent = intent2;
                        taskDescription = taskDescription2;
                        break;
                    case WindowManagerService.H.FORCE_GC /* 15 */:
                        neverRelinquishIdentity = Boolean.parseBoolean(attrValue);
                        intent = intent2;
                        taskDescription = taskDescription2;
                        break;
                    case WindowManagerService.H.ENABLE_SCREEN /* 16 */:
                        taskAffiliation = Integer.parseInt(attrValue);
                        intent = intent2;
                        taskDescription = taskDescription2;
                        break;
                    case WindowManagerService.H.APP_FREEZE_TIMEOUT /* 17 */:
                        prevTaskId = Integer.parseInt(attrValue);
                        intent = intent2;
                        taskDescription = taskDescription2;
                        break;
                    case WindowManagerService.H.SEND_NEW_CONFIGURATION /* 18 */:
                        nextTaskId = Integer.parseInt(attrValue);
                        intent = intent2;
                        taskDescription = taskDescription2;
                        break;
                    case WindowManagerService.H.REPORT_WINDOWS_CHANGE /* 19 */:
                        taskAffiliationColor = Integer.parseInt(attrValue);
                        intent = intent2;
                        taskDescription = taskDescription2;
                        break;
                    case 20:
                        callingUid = Integer.parseInt(attrValue);
                        intent = intent2;
                        taskDescription = taskDescription2;
                        break;
                    case 21:
                        callingPackage = attrValue;
                        intent = intent2;
                        taskDescription = taskDescription2;
                        break;
                    case WindowManagerService.H.REPORT_HARD_KEYBOARD_STATUS_CHANGE /* 22 */:
                        resizeMode2 = Integer.parseInt(attrValue);
                        intent = intent2;
                        taskDescription = taskDescription2;
                        break;
                    case WindowManagerService.H.BOOT_TIMEOUT /* 23 */:
                        intent = intent2;
                        supportsPictureInPicture2 = Boolean.parseBoolean(attrValue);
                        taskDescription = taskDescription2;
                        break;
                    case WindowManagerService.H.WAITING_FOR_DRAWN_TIMEOUT /* 24 */:
                        intent = intent2;
                        lastNonFullscreenBounds = Rect.unflattenFromString(attrValue);
                        taskDescription = taskDescription2;
                        break;
                    case WindowManagerService.H.SHOW_STRICT_MODE_VIOLATION /* 25 */:
                        minWidth = Integer.parseInt(attrValue);
                        intent = intent2;
                        taskDescription = taskDescription2;
                        break;
                    case 26:
                        minHeight = Integer.parseInt(attrValue);
                        intent = intent2;
                        taskDescription = taskDescription2;
                        break;
                    case 27:
                        intent = intent2;
                        persistTaskVersion = Integer.parseInt(attrValue);
                        taskDescription = taskDescription2;
                        break;
                    default:
                        if (attrName.startsWith("task_description_")) {
                            taskDescription = taskDescription2;
                            taskDescription.restoreFromXml(attrName, attrValue);
                            intent = intent2;
                            break;
                        } else {
                            taskDescription = taskDescription2;
                            intent = intent2;
                            Slog.w("ActivityTaskManager", "TaskRecord: Unknown attribute=" + attrName);
                            break;
                        }
                }
                attrNdx--;
                taskDescription2 = taskDescription;
                intent2 = intent;
            }
            Intent intent3 = intent2;
            Intent affinityIntent = null;
            while (true) {
                int event = in.next();
                if (event != 1) {
                    if (event != 3 || in.getDepth() >= outerDepth) {
                        if (event == 2) {
                            String name = in.getName();
                            if (TaskRecord.TAG_AFFINITYINTENT.equals(name)) {
                                affinityIntent = Intent.restoreFromXml(in);
                            } else if (TaskRecord.TAG_INTENT.equals(name)) {
                                intent3 = Intent.restoreFromXml(in);
                            } else if (TaskRecord.TAG_ACTIVITY.equals(name)) {
                                ActivityRecord activity = ActivityRecord.restoreFromXml(in, stackSupervisor);
                                if (activity != null) {
                                    activities.add(activity);
                                }
                            } else {
                                handleUnknownTag(name, in);
                            }
                        }
                    }
                }
            }
            if (!hasRootAffinity) {
                rootAffinity = affinity;
            } else if ("@".equals(rootAffinity2)) {
                rootAffinity = null;
            } else {
                rootAffinity = rootAffinity2;
            }
            if (effectiveUid2 <= 0) {
                Intent checkIntent = intent3 != null ? intent3 : affinityIntent;
                int effectiveUid3 = 0;
                if (checkIntent != null) {
                    try {
                        try {
                            ApplicationInfo ai = AppGlobals.getPackageManager().getApplicationInfo(checkIntent.getComponent().getPackageName(), 8704, userId);
                            if (ai != null) {
                                effectiveUid3 = ai.uid;
                            }
                        } catch (RemoteException e) {
                        }
                    } catch (RemoteException e2) {
                    }
                }
                Slog.w("ActivityTaskManager", "Updating task #" + taskId + " for " + checkIntent + ": effectiveUid=" + effectiveUid3);
                effectiveUid = effectiveUid3;
            } else {
                effectiveUid = effectiveUid2;
            }
            if (persistTaskVersion < 1) {
                if (taskType == 1 && resizeMode2 == 2) {
                    resizeMode = 1;
                    supportsPictureInPicture = supportsPictureInPicture2;
                    TaskRecord task = create(stackSupervisor.mService, taskId, intent3, affinityIntent, affinity, rootAffinity, realActivity, origActivity, rootHasReset, autoRemoveRecents, askedCompatMode, userId, effectiveUid, lastDescription, activities, lastTimeOnTop, neverRelinquishIdentity, taskDescription2, taskAffiliation, prevTaskId, nextTaskId, taskAffiliationColor, callingUid, callingPackage, resizeMode, supportsPictureInPicture, realActivitySuspended, userSetupComplete, minWidth, minHeight);
                    task.mLastNonFullscreenBounds = lastNonFullscreenBounds;
                    task.setBounds(lastNonFullscreenBounds);
                    for (activityNdx = activities.size() - 1; activityNdx >= 0; activityNdx--) {
                        activities.get(activityNdx).setTask(task);
                    }
                    if (ActivityTaskManagerDebugConfig.DEBUG_RECENTS) {
                        Slog.d("ActivityTaskManager", "Restored task=" + task);
                    }
                    return task;
                }
            } else if (resizeMode2 == 3) {
                resizeMode = 2;
                supportsPictureInPicture = true;
                TaskRecord task2 = create(stackSupervisor.mService, taskId, intent3, affinityIntent, affinity, rootAffinity, realActivity, origActivity, rootHasReset, autoRemoveRecents, askedCompatMode, userId, effectiveUid, lastDescription, activities, lastTimeOnTop, neverRelinquishIdentity, taskDescription2, taskAffiliation, prevTaskId, nextTaskId, taskAffiliationColor, callingUid, callingPackage, resizeMode, supportsPictureInPicture, realActivitySuspended, userSetupComplete, minWidth, minHeight);
                task2.mLastNonFullscreenBounds = lastNonFullscreenBounds;
                task2.setBounds(lastNonFullscreenBounds);
                while (activityNdx >= 0) {
                }
                if (ActivityTaskManagerDebugConfig.DEBUG_RECENTS) {
                }
                return task2;
            }
            resizeMode = resizeMode2;
            supportsPictureInPicture = supportsPictureInPicture2;
            TaskRecord task22 = create(stackSupervisor.mService, taskId, intent3, affinityIntent, affinity, rootAffinity, realActivity, origActivity, rootHasReset, autoRemoveRecents, askedCompatMode, userId, effectiveUid, lastDescription, activities, lastTimeOnTop, neverRelinquishIdentity, taskDescription2, taskAffiliation, prevTaskId, nextTaskId, taskAffiliationColor, callingUid, callingPackage, resizeMode, supportsPictureInPicture, realActivitySuspended, userSetupComplete, minWidth, minHeight);
            task22.mLastNonFullscreenBounds = lastNonFullscreenBounds;
            task22.setBounds(lastNonFullscreenBounds);
            while (activityNdx >= 0) {
            }
            if (ActivityTaskManagerDebugConfig.DEBUG_RECENTS) {
            }
            return task22;
        }

        /* access modifiers changed from: package-private */
        public void handleUnknownTag(String name, XmlPullParser in) throws IOException, XmlPullParserException {
            Slog.e("ActivityTaskManager", "restoreTask: Unexpected name=" + name);
            XmlUtils.skipCurrentTag(in);
        }
    }

    private boolean isTopInSplitAndNonResizable() {
        ActivityRecord topActivity = getTopActivity();
        return topActivity != null && topActivity.isSplitMode() && topActivity.isNonResizableOrForcedResizable() && !topActivity.isState(ActivityStack.ActivityState.STOPPED);
    }

    /* access modifiers changed from: protected */
    public void updateHwOverrideConfiguration(Rect bounds) {
    }

    /* access modifiers changed from: protected */
    public void updateHwPCMultiCastOverrideConfiguration(Rect bounds) {
    }

    /* access modifiers changed from: package-private */
    public void activityResumedInTop() {
    }
}
