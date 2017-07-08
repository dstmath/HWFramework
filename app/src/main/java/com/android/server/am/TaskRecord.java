package com.android.server.am;

import android.app.ActivityManager;
import android.app.ActivityManager.StackId;
import android.app.ActivityManager.TaskDescription;
import android.app.ActivityManager.TaskThumbnail;
import android.app.ActivityManager.TaskThumbnailInfo;
import android.app.ActivityOptions;
import android.app.AppGlobals;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Debug;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.os.UserHandle;
import android.provider.Settings.Secure;
import android.service.voice.IVoiceInteractionSession;
import android.util.Flog;
import android.util.Slog;
import com.android.internal.app.IVoiceInteractor;
import com.android.internal.util.XmlUtils;
import com.android.server.HwServiceFactory;
import com.android.server.wm.WindowManagerService;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
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
    private static final String ATTR_FIRSTACTIVETIME = "first_active_time";
    private static final String ATTR_LASTACTIVETIME = "last_active_time";
    private static final String ATTR_LASTDESCRIPTION = "last_description";
    private static final String ATTR_LASTTIMEMOVED = "last_time_moved";
    private static final String ATTR_MIN_HEIGHT = "min_height";
    private static final String ATTR_MIN_WIDTH = "min_width";
    private static final String ATTR_NEVERRELINQUISH = "never_relinquish_identity";
    private static final String ATTR_NEXT_AFFILIATION = "next_affiliation";
    private static final String ATTR_NON_FULLSCREEN_BOUNDS = "non_fullscreen_bounds";
    private static final String ATTR_ORIGACTIVITY = "orig_activity";
    private static final String ATTR_PREV_AFFILIATION = "prev_affiliation";
    private static final String ATTR_PRIVILEGED = "privileged";
    static final String ATTR_REALACTIVITY = "real_activity";
    static final String ATTR_REALACTIVITY_SUSPENDED = "real_activity_suspended";
    private static final String ATTR_RESIZE_MODE = "resize_mode";
    private static final String ATTR_ROOTHASRESET = "root_has_reset";
    private static final String ATTR_ROOT_AFFINITY = "root_affinity";
    static final String ATTR_TASKID = "task_id";
    private static final String ATTR_TASKTYPE = "task_type";
    static final String ATTR_TASK_AFFILIATION = "task_affiliation";
    private static final String ATTR_TASK_AFFILIATION_COLOR = "task_affiliation_color";
    private static final String ATTR_USERID = "user_id";
    private static final String ATTR_USER_SETUP_COMPLETE = "user_setup_complete";
    static final int INVALID_MIN_SIZE = -1;
    static final int INVALID_TASK_ID = -1;
    static final int LOCK_TASK_AUTH_DONT_LOCK = 0;
    static final int LOCK_TASK_AUTH_LAUNCHABLE = 2;
    static final int LOCK_TASK_AUTH_LAUNCHABLE_PRIV = 4;
    static final int LOCK_TASK_AUTH_PINNABLE = 1;
    static final int LOCK_TASK_AUTH_WHITELISTED = 3;
    private static final String TAG = null;
    private static final String TAG_ACTIVITY = "activity";
    private static final String TAG_ADD_REMOVE = null;
    private static final String TAG_AFFINITYINTENT = "affinity_intent";
    private static final String TAG_INTENT = "intent";
    private static final String TAG_LOCKTASK = null;
    private static final String TAG_RECENTS = null;
    private static final String TAG_TASKS = null;
    private static final String TASK_THUMBNAIL_SUFFIX = "_task_thumbnail";
    String affinity;
    Intent affinityIntent;
    boolean askedCompatMode;
    boolean autoRemoveRecents;
    int effectiveUid;
    long firstActiveTime;
    boolean hasBeenVisible;
    boolean inRecents;
    Intent intent;
    boolean isAvailable;
    boolean isLaunching;
    boolean isPersistable;
    long lastActiveTime;
    CharSequence lastDescription;
    TaskDescription lastTaskDescription;
    final ArrayList<ActivityRecord> mActivities;
    int mAffiliatedTaskColor;
    int mAffiliatedTaskId;
    Rect mBounds;
    String mCallingPackage;
    int mCallingUid;
    private final String mFilename;
    boolean mFullscreen;
    Rect mLastNonFullscreenBounds;
    private Bitmap mLastThumbnail;
    private final File mLastThumbnailFile;
    private TaskThumbnailInfo mLastThumbnailInfo;
    long mLastTimeMoved;
    int mLayerRank;
    int mLockTaskAuth;
    int mLockTaskMode;
    int mLockTaskUid;
    int mMinHeight;
    int mMinWidth;
    boolean mNeverRelinquishIdentity;
    TaskRecord mNextAffiliate;
    int mNextAffiliateTaskId;
    Configuration mOverrideConfig;
    TaskRecord mPrevAffiliate;
    int mPrevAffiliateTaskId;
    private boolean mPrivileged;
    int mResizeMode;
    private boolean mReuseTask;
    final ActivityManagerService mService;
    private int mTaskToReturnTo;
    boolean mTemporarilyUnresizable;
    private final Rect mTmpNonDecorBounds;
    private final Rect mTmpRect;
    private final Rect mTmpRect2;
    private final Rect mTmpStableBounds;
    boolean mUserSetupComplete;
    int maxRecents;
    int multiLaunchId;
    int numFullscreen;
    ComponentName origActivity;
    ComponentName realActivity;
    boolean realActivitySuspended;
    String rootAffinity;
    boolean rootWasReset;
    ActivityStack stack;
    String stringName;
    final int taskId;
    int taskType;
    int userId;
    final IVoiceInteractor voiceInteractor;
    final IVoiceInteractionSession voiceSession;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.am.TaskRecord.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.am.TaskRecord.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.am.TaskRecord.<clinit>():void");
    }

    public TaskRecord(ActivityManagerService service, int _taskId, ActivityInfo info, Intent _intent, IVoiceInteractionSession _voiceSession, IVoiceInteractor _voiceInteractor) {
        this.mLockTaskAuth = LOCK_TASK_AUTH_PINNABLE;
        this.mLockTaskUid = INVALID_TASK_ID;
        this.lastTaskDescription = new TaskDescription();
        this.isPersistable = false;
        this.mLastTimeMoved = System.currentTimeMillis();
        this.mTaskToReturnTo = LOCK_TASK_AUTH_DONT_LOCK;
        this.mNeverRelinquishIdentity = true;
        this.mReuseTask = false;
        this.mPrevAffiliateTaskId = INVALID_TASK_ID;
        this.mNextAffiliateTaskId = INVALID_TASK_ID;
        this.mFullscreen = true;
        this.mBounds = null;
        this.mTmpStableBounds = new Rect();
        this.mTmpNonDecorBounds = new Rect();
        this.mTmpRect = new Rect();
        this.mTmpRect2 = new Rect();
        this.mLastNonFullscreenBounds = null;
        this.mLayerRank = INVALID_TASK_ID;
        this.mOverrideConfig = Configuration.EMPTY;
        this.mService = service;
        this.mFilename = String.valueOf(_taskId) + TASK_THUMBNAIL_SUFFIX + ".png";
        this.userId = UserHandle.getUserId(info.applicationInfo.uid);
        this.mLastThumbnailFile = new File(TaskPersister.getUserImagesDir(this.userId), this.mFilename);
        this.mLastThumbnailInfo = new TaskThumbnailInfo();
        this.taskId = _taskId;
        this.mAffiliatedTaskId = _taskId;
        this.voiceSession = _voiceSession;
        this.voiceInteractor = _voiceInteractor;
        this.isAvailable = true;
        this.mActivities = new ArrayList();
        this.mCallingUid = info.applicationInfo.uid;
        this.mCallingPackage = info.packageName;
        setIntent(_intent, info);
        setMinDimensions(info);
        touchActiveTime();
    }

    public TaskRecord(ActivityManagerService service, int _taskId, ActivityInfo info, Intent _intent, TaskDescription _taskDescription, TaskThumbnailInfo thumbnailInfo) {
        this.mLockTaskAuth = LOCK_TASK_AUTH_PINNABLE;
        this.mLockTaskUid = INVALID_TASK_ID;
        this.lastTaskDescription = new TaskDescription();
        this.isPersistable = false;
        this.mLastTimeMoved = System.currentTimeMillis();
        this.mTaskToReturnTo = LOCK_TASK_AUTH_DONT_LOCK;
        this.mNeverRelinquishIdentity = true;
        this.mReuseTask = false;
        this.mPrevAffiliateTaskId = INVALID_TASK_ID;
        this.mNextAffiliateTaskId = INVALID_TASK_ID;
        this.mFullscreen = true;
        this.mBounds = null;
        this.mTmpStableBounds = new Rect();
        this.mTmpNonDecorBounds = new Rect();
        this.mTmpRect = new Rect();
        this.mTmpRect2 = new Rect();
        this.mLastNonFullscreenBounds = null;
        this.mLayerRank = INVALID_TASK_ID;
        this.mOverrideConfig = Configuration.EMPTY;
        this.mService = service;
        this.mFilename = String.valueOf(_taskId) + TASK_THUMBNAIL_SUFFIX + ".png";
        this.userId = UserHandle.getUserId(info.applicationInfo.uid);
        this.mLastThumbnailFile = new File(TaskPersister.getUserImagesDir(this.userId), this.mFilename);
        this.mLastThumbnailInfo = thumbnailInfo;
        this.taskId = _taskId;
        this.mAffiliatedTaskId = _taskId;
        this.voiceSession = null;
        this.voiceInteractor = null;
        this.isAvailable = true;
        this.mActivities = new ArrayList();
        this.mCallingUid = info.applicationInfo.uid;
        this.mCallingPackage = info.packageName;
        setIntent(_intent, info);
        setMinDimensions(info);
        this.taskType = LOCK_TASK_AUTH_DONT_LOCK;
        this.isPersistable = true;
        this.maxRecents = Math.min(Math.max(info.maxRecents, LOCK_TASK_AUTH_PINNABLE), ActivityManager.getMaxAppRecentsLimitStatic());
        this.taskType = LOCK_TASK_AUTH_DONT_LOCK;
        this.mTaskToReturnTo = LOCK_TASK_AUTH_PINNABLE;
        updateMultiLaunchId(_intent);
        this.lastTaskDescription = _taskDescription;
        touchActiveTime();
    }

    public TaskRecord(ActivityManagerService service, int _taskId, Intent _intent, Intent _affinityIntent, String _affinity, String _rootAffinity, ComponentName _realActivity, ComponentName _origActivity, boolean _rootWasReset, boolean _autoRemoveRecents, boolean _askedCompatMode, int _taskType, int _userId, int _effectiveUid, String _lastDescription, ArrayList<ActivityRecord> activities, long _firstActiveTime, long _lastActiveTime, long lastTimeMoved, boolean neverRelinquishIdentity, TaskDescription _lastTaskDescription, TaskThumbnailInfo lastThumbnailInfo, int taskAffiliation, int prevTaskId, int nextTaskId, int taskAffiliationColor, int callingUid, String callingPackage, int resizeMode, boolean privileged, boolean _realActivitySuspended, boolean userSetupComplete, int minWidth, int minHeight) {
        this.mLockTaskAuth = LOCK_TASK_AUTH_PINNABLE;
        this.mLockTaskUid = INVALID_TASK_ID;
        this.lastTaskDescription = new TaskDescription();
        this.isPersistable = false;
        this.mLastTimeMoved = System.currentTimeMillis();
        this.mTaskToReturnTo = LOCK_TASK_AUTH_DONT_LOCK;
        this.mNeverRelinquishIdentity = true;
        this.mReuseTask = false;
        this.mPrevAffiliateTaskId = INVALID_TASK_ID;
        this.mNextAffiliateTaskId = INVALID_TASK_ID;
        this.mFullscreen = true;
        this.mBounds = null;
        this.mTmpStableBounds = new Rect();
        this.mTmpNonDecorBounds = new Rect();
        this.mTmpRect = new Rect();
        this.mTmpRect2 = new Rect();
        this.mLastNonFullscreenBounds = null;
        this.mLayerRank = INVALID_TASK_ID;
        this.mOverrideConfig = Configuration.EMPTY;
        this.mService = service;
        this.mFilename = String.valueOf(_taskId) + TASK_THUMBNAIL_SUFFIX + ".png";
        this.mLastThumbnailFile = new File(TaskPersister.getUserImagesDir(_userId), this.mFilename);
        this.mLastThumbnailInfo = lastThumbnailInfo;
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
        this.taskType = _taskType;
        this.mTaskToReturnTo = LOCK_TASK_AUTH_PINNABLE;
        this.userId = _userId;
        updateMultiLaunchId(_intent);
        this.mUserSetupComplete = userSetupComplete;
        this.effectiveUid = _effectiveUid;
        this.firstActiveTime = _firstActiveTime;
        this.lastActiveTime = _lastActiveTime;
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
        this.mPrivileged = privileged;
        this.mMinWidth = minWidth;
        this.mMinHeight = minHeight;
    }

    void touchActiveTime() {
        this.lastActiveTime = System.currentTimeMillis();
        if (this.firstActiveTime == 0) {
            this.firstActiveTime = this.lastActiveTime;
        }
    }

    long getInactiveDuration() {
        return System.currentTimeMillis() - this.lastActiveTime;
    }

    void setIntent(ActivityRecord r) {
        this.mCallingUid = r.launchedFromUid;
        this.mCallingPackage = r.launchedFromPackage;
        setIntent(r.intent, r.info);
    }

    private void setIntent(Intent _intent, ActivityInfo info) {
        boolean z;
        boolean z2 = true;
        if (this.intent == null) {
            if ((info.flags & DumpState.DUMP_PREFERRED) == 0) {
                z = true;
            } else {
                z = false;
            }
            this.mNeverRelinquishIdentity = z;
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
                Intent _intent2 = new Intent(_intent);
                _intent2.setSelector(null);
                _intent2.setSourceBounds(null);
                _intent = _intent2;
            }
            if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                Slog.v(TAG_TASKS, "Setting Intent of " + this + " to " + _intent);
            }
            this.intent = _intent;
            this.realActivity = _intent != null ? _intent.getComponent() : null;
            this.origActivity = null;
        } else {
            ComponentName targetComponent = new ComponentName(info.packageName, info.targetActivity);
            if (_intent != null) {
                Intent targetIntent = new Intent(_intent);
                targetIntent.setComponent(targetComponent);
                targetIntent.setSelector(null);
                targetIntent.setSourceBounds(null);
                if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                    Slog.v(TAG_TASKS, "Setting Intent of " + this + " to target " + targetIntent);
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
        int intentFlags = this.intent == null ? LOCK_TASK_AUTH_DONT_LOCK : this.intent.getFlags();
        if ((2097152 & intentFlags) != 0) {
            this.rootWasReset = true;
        }
        this.userId = UserHandle.getUserId(info.applicationInfo.uid);
        updateMultiLaunchId(_intent);
        if (Secure.getIntForUser(this.mService.mContext.getContentResolver(), ATTR_USER_SETUP_COMPLETE, LOCK_TASK_AUTH_DONT_LOCK, this.userId) != 0) {
            z = true;
        } else {
            z = false;
        }
        this.mUserSetupComplete = z;
        if ((info.flags & DumpState.DUMP_PREFERRED_XML) != 0) {
            this.autoRemoveRecents = true;
        } else if ((532480 & intentFlags) != DumpState.DUMP_FROZEN) {
            this.autoRemoveRecents = false;
        } else if (info.documentLaunchMode != 0) {
            this.autoRemoveRecents = false;
        } else {
            this.autoRemoveRecents = true;
        }
        this.mResizeMode = info.resizeMode;
        this.mLockTaskMode = info.lockTaskLaunchMode;
        if ((info.applicationInfo.privateFlags & 8) == 0) {
            z2 = false;
        }
        this.mPrivileged = z2;
        setLockTaskAuth();
    }

    private void setMinDimensions(ActivityInfo info) {
        if (info == null || info.windowLayout == null) {
            this.mMinWidth = INVALID_TASK_ID;
            this.mMinHeight = INVALID_TASK_ID;
            return;
        }
        this.mMinWidth = info.windowLayout.minWidth;
        this.mMinHeight = info.windowLayout.minHeight;
    }

    boolean isSameIntentFilter(ActivityRecord r) {
        Intent intent = new Intent(r.intent);
        intent.setComponent(r.realActivity);
        return this.intent.filterEquals(intent);
    }

    void setTaskToReturnTo(int taskToReturnTo) {
        if (taskToReturnTo == LOCK_TASK_AUTH_LAUNCHABLE) {
            taskToReturnTo = LOCK_TASK_AUTH_PINNABLE;
        }
        this.mTaskToReturnTo = taskToReturnTo;
    }

    int getTaskToReturnTo() {
        return this.mTaskToReturnTo;
    }

    void setPrevAffiliate(TaskRecord prevAffiliate) {
        this.mPrevAffiliate = prevAffiliate;
        this.mPrevAffiliateTaskId = prevAffiliate == null ? INVALID_TASK_ID : prevAffiliate.taskId;
    }

    void setNextAffiliate(TaskRecord nextAffiliate) {
        this.mNextAffiliate = nextAffiliate;
        this.mNextAffiliateTaskId = nextAffiliate == null ? INVALID_TASK_ID : nextAffiliate.taskId;
    }

    void closeRecentsChain() {
        if (this.mPrevAffiliate != null) {
            this.mPrevAffiliate.setNextAffiliate(this.mNextAffiliate);
        }
        if (this.mNextAffiliate != null) {
            this.mNextAffiliate.setPrevAffiliate(this.mPrevAffiliate);
        }
        setPrevAffiliate(null);
        setNextAffiliate(null);
    }

    void removedFromRecents() {
        disposeThumbnail();
        closeRecentsChain();
        if (this.inRecents) {
            this.inRecents = false;
            this.mService.notifyTaskPersisterLocked(this, false);
        }
    }

    void setTaskToAffiliateWith(TaskRecord taskToAffiliateWith) {
        closeRecentsChain();
        this.mAffiliatedTaskId = taskToAffiliateWith.mAffiliatedTaskId;
        this.mAffiliatedTaskColor = taskToAffiliateWith.mAffiliatedTaskColor;
        while (taskToAffiliateWith.mNextAffiliate != null) {
            TaskRecord nextRecents = taskToAffiliateWith.mNextAffiliate;
            if (nextRecents.mAffiliatedTaskId != this.mAffiliatedTaskId) {
                Slog.e(TAG, "setTaskToAffiliateWith: nextRecents=" + nextRecents + " affilTaskId=" + nextRecents.mAffiliatedTaskId + " should be " + this.mAffiliatedTaskId);
                if (nextRecents.mPrevAffiliate == taskToAffiliateWith) {
                    nextRecents.setPrevAffiliate(null);
                }
                taskToAffiliateWith.setNextAffiliate(null);
                taskToAffiliateWith.setNextAffiliate(this);
                setPrevAffiliate(taskToAffiliateWith);
                setNextAffiliate(null);
            }
            taskToAffiliateWith = nextRecents;
        }
        taskToAffiliateWith.setNextAffiliate(this);
        setPrevAffiliate(taskToAffiliateWith);
        setNextAffiliate(null);
    }

    boolean setLastThumbnailLocked(Bitmap thumbnail) {
        Configuration serviceConfig = this.mService.mConfiguration;
        int taskWidth = LOCK_TASK_AUTH_DONT_LOCK;
        int taskHeight = LOCK_TASK_AUTH_DONT_LOCK;
        if (this.mBounds != null) {
            taskWidth = this.mBounds.width();
            taskHeight = this.mBounds.height();
        } else if (this.stack != null) {
            Point displaySize = new Point();
            this.stack.getDisplaySize(displaySize);
            taskWidth = displaySize.x;
            taskHeight = displaySize.y;
        } else {
            Slog.e(TAG, "setLastThumbnailLocked() called on Task without stack");
        }
        return setLastThumbnailLocked(thumbnail, taskWidth, taskHeight, serviceConfig.orientation);
    }

    private Bitmap rotateThumbnailWhenLand(Bitmap thumbnail, int screenOrientation) {
        if (thumbnail == null || screenOrientation != LOCK_TASK_AUTH_LAUNCHABLE) {
            return thumbnail;
        }
        Matrix m = new Matrix();
        m.postRotate(270.0f);
        return Bitmap.createBitmap(thumbnail, LOCK_TASK_AUTH_DONT_LOCK, LOCK_TASK_AUTH_DONT_LOCK, thumbnail.getWidth(), thumbnail.getHeight(), m, true);
    }

    private boolean setLastThumbnailLocked(Bitmap thumbnail, int taskWidth, int taskHeight, int screenOrientation) {
        if (this.mLastThumbnail == thumbnail) {
            return false;
        }
        thumbnail = rotateThumbnailWhenLand(thumbnail, screenOrientation);
        this.mLastThumbnail = thumbnail;
        this.mLastThumbnailInfo.taskWidth = taskWidth;
        this.mLastThumbnailInfo.taskHeight = taskHeight;
        this.mLastThumbnailInfo.screenOrientation = screenOrientation;
        if (thumbnail != null && !thumbnail.isRecycled()) {
            this.mService.mRecentTasks.saveImage(thumbnail, this.mLastThumbnailFile.getAbsolutePath());
        } else if (this.mLastThumbnailFile != null) {
            this.mLastThumbnailFile.delete();
        }
        return true;
    }

    void getLastThumbnail(TaskThumbnail thumbs) {
        thumbs.mainThumbnail = this.mLastThumbnail;
        thumbs.thumbnailInfo = this.mLastThumbnailInfo;
        thumbs.thumbnailFileDescriptor = null;
        if (this.mLastThumbnail == null || this.mLastThumbnail.isRecycled()) {
            thumbs.mainThumbnail = this.mService.mRecentTasks.getImageFromWriteQueue(this.mLastThumbnailFile.getAbsolutePath());
        }
        if ((thumbs.mainThumbnail == null || (thumbs.mainThumbnail != null && thumbs.mainThumbnail.isRecycled())) && this.mLastThumbnailFile.exists()) {
            try {
                thumbs.thumbnailFileDescriptor = ParcelFileDescriptor.open(this.mLastThumbnailFile, 268435456);
            } catch (IOException e) {
            }
        }
    }

    void freeLastThumbnail() {
        this.mLastThumbnail = null;
    }

    void disposeThumbnail() {
        this.mLastThumbnailInfo.reset();
        this.mLastThumbnail = null;
        this.lastDescription = null;
    }

    Intent getBaseIntent() {
        return this.intent != null ? this.intent : this.affinityIntent;
    }

    ActivityRecord getRootActivity() {
        for (int i = LOCK_TASK_AUTH_DONT_LOCK; i < this.mActivities.size(); i += LOCK_TASK_AUTH_PINNABLE) {
            ActivityRecord r = (ActivityRecord) this.mActivities.get(i);
            if (!r.finishing) {
                return r;
            }
        }
        return null;
    }

    ActivityRecord getTopActivity() {
        for (int i = this.mActivities.size() + INVALID_TASK_ID; i >= 0; i += INVALID_TASK_ID) {
            ActivityRecord r = (ActivityRecord) this.mActivities.get(i);
            if (!r.finishing) {
                return r;
            }
        }
        return null;
    }

    ActivityRecord topRunningActivityLocked() {
        if (this.stack != null) {
            for (int activityNdx = this.mActivities.size() + INVALID_TASK_ID; activityNdx >= 0; activityNdx += INVALID_TASK_ID) {
                ActivityRecord r = (ActivityRecord) this.mActivities.get(activityNdx);
                if (!r.finishing && this.stack.okToShowLocked(r)) {
                    return r;
                }
            }
        }
        return null;
    }

    ActivityRecord topRunningActivityWithStartingWindowLocked() {
        if (this.stack != null) {
            for (int activityNdx = this.mActivities.size() + INVALID_TASK_ID; activityNdx >= 0; activityNdx += INVALID_TASK_ID) {
                ActivityRecord r = (ActivityRecord) this.mActivities.get(activityNdx);
                if (r.mStartingWindowState == LOCK_TASK_AUTH_PINNABLE && !r.finishing && this.stack.okToShowLocked(r)) {
                    return r;
                }
            }
        }
        return null;
    }

    void setFrontOfTask() {
        setFrontOfTask(null);
    }

    void setFrontOfTask(ActivityRecord newTop) {
        boolean foundFront = newTop != null;
        int numActivities = this.mActivities.size();
        for (int activityNdx = LOCK_TASK_AUTH_DONT_LOCK; activityNdx < numActivities; activityNdx += LOCK_TASK_AUTH_PINNABLE) {
            ActivityRecord r = (ActivityRecord) this.mActivities.get(activityNdx);
            if (foundFront || r.finishing) {
                r.frontOfTask = false;
            } else {
                r.frontOfTask = true;
                foundFront = true;
            }
        }
        if (!foundFront && numActivities > 0) {
            ((ActivityRecord) this.mActivities.get(LOCK_TASK_AUTH_DONT_LOCK)).frontOfTask = true;
        }
        if (newTop != null) {
            newTop.frontOfTask = true;
        }
    }

    final void moveActivityToFrontLocked(ActivityRecord newTop) {
        if (ActivityManagerDebugConfig.DEBUG_ADD_REMOVE) {
            Slog.i(TAG_ADD_REMOVE, "Removing and adding activity " + newTop + " to stack at top callers=" + Debug.getCallers(LOCK_TASK_AUTH_LAUNCHABLE_PRIV));
        }
        this.mActivities.remove(newTop);
        this.mActivities.add(newTop);
        updateEffectiveIntent();
        setFrontOfTask(newTop);
    }

    void addActivityAtBottom(ActivityRecord r) {
        addActivityAtIndex(LOCK_TASK_AUTH_DONT_LOCK, r);
    }

    void addActivityToTop(ActivityRecord r) {
        addActivityAtIndex(this.mActivities.size(), r);
    }

    void addActivityAtIndex(int index, ActivityRecord r) {
        if (!this.mActivities.remove(r) && r.fullscreen) {
            this.numFullscreen += LOCK_TASK_AUTH_PINNABLE;
        }
        if (this.mActivities.isEmpty()) {
            this.taskType = r.mActivityType;
            this.isPersistable = r.isPersistable();
            this.mCallingUid = r.launchedFromUid;
            this.mCallingPackage = r.launchedFromPackage;
            this.maxRecents = Math.min(Math.max(r.info.maxRecents, LOCK_TASK_AUTH_PINNABLE), ActivityManager.getMaxAppRecentsLimitStatic());
        } else {
            r.mActivityType = this.taskType;
        }
        int size = this.mActivities.size();
        if (index == size && size > 0 && ((ActivityRecord) this.mActivities.get(size + INVALID_TASK_ID)).mTaskOverlay) {
            index += INVALID_TASK_ID;
        }
        this.mActivities.add(index, r);
        updateEffectiveIntent();
        if (r.isPersistable()) {
            this.mService.notifyTaskPersisterLocked(this, false);
        }
    }

    boolean removeActivity(ActivityRecord r) {
        boolean z = false;
        if (this.mActivities.remove(r) && r.fullscreen) {
            this.numFullscreen += INVALID_TASK_ID;
        }
        if (r.isPersistable()) {
            this.mService.notifyTaskPersisterLocked(this, false);
        }
        if (this.stack != null && this.stack.mStackId == LOCK_TASK_AUTH_LAUNCHABLE_PRIV) {
            this.mService.notifyTaskStackChangedLocked();
        }
        if (this.mActivities.isEmpty()) {
            if (!this.mReuseTask) {
                z = true;
            }
            return z;
        }
        updateEffectiveIntent();
        return false;
    }

    boolean autoRemoveFromRecents() {
        if (this.autoRemoveRecents) {
            return true;
        }
        return this.mActivities.isEmpty() && !this.hasBeenVisible;
    }

    final void performClearTaskAtIndexLocked(int activityNdx) {
        int numActivities = this.mActivities.size();
        while (activityNdx < numActivities) {
            ActivityRecord r = (ActivityRecord) this.mActivities.get(activityNdx);
            if (!r.finishing) {
                if (this.stack == null) {
                    r.takeFromHistory();
                    this.mActivities.remove(activityNdx);
                    activityNdx += INVALID_TASK_ID;
                    numActivities += INVALID_TASK_ID;
                } else if (this.stack.finishActivityLocked(r, LOCK_TASK_AUTH_DONT_LOCK, null, "clear-task-index", false)) {
                    activityNdx += INVALID_TASK_ID;
                    numActivities += INVALID_TASK_ID;
                }
            }
            activityNdx += LOCK_TASK_AUTH_PINNABLE;
        }
    }

    final void performClearTaskLocked() {
        this.mReuseTask = true;
        performClearTaskAtIndexLocked(LOCK_TASK_AUTH_DONT_LOCK);
        this.mReuseTask = false;
    }

    ActivityRecord performClearTaskForReuseLocked(ActivityRecord newR, int launchFlags) {
        this.mReuseTask = true;
        ActivityRecord result = performClearTaskLocked(newR, launchFlags);
        this.mReuseTask = false;
        return result;
    }

    final ActivityRecord performClearTaskLocked(ActivityRecord newR, int launchFlags) {
        int numActivities = this.mActivities.size();
        for (int activityNdx = numActivities + INVALID_TASK_ID; activityNdx >= 0; activityNdx += INVALID_TASK_ID) {
            ActivityRecord r = (ActivityRecord) this.mActivities.get(activityNdx);
            if (!r.finishing && r.realActivity.equals(newR.realActivity)) {
                ActivityRecord ret = r;
                activityNdx += LOCK_TASK_AUTH_PINNABLE;
                while (activityNdx < numActivities) {
                    r = (ActivityRecord) this.mActivities.get(activityNdx);
                    if (!r.finishing) {
                        ActivityOptions opts = r.takeOptionsLocked();
                        if (opts != null) {
                            ret.updateOptionsLocked(opts);
                        }
                        if (this.stack != null && this.stack.finishActivityLocked(r, LOCK_TASK_AUTH_DONT_LOCK, null, "clear-task-stack", false)) {
                            activityNdx += INVALID_TASK_ID;
                            numActivities += INVALID_TASK_ID;
                        }
                    }
                    activityNdx += LOCK_TASK_AUTH_PINNABLE;
                }
                if (ret.launchMode != 0 || (536870912 & launchFlags) != 0 || ret.finishing) {
                    return ret;
                }
                if (this.stack != null) {
                    this.stack.finishActivityLocked(ret, LOCK_TASK_AUTH_DONT_LOCK, null, "clear-task-top", false);
                }
                return null;
            }
        }
        return null;
    }

    public TaskThumbnail getTaskThumbnailLocked() {
        if (this.stack != null) {
            ActivityRecord resumedActivity = this.stack.mResumedActivity;
            if (resumedActivity != null && resumedActivity.task == this) {
                setLastThumbnailLocked(this.stack.screenshotActivitiesLocked(resumedActivity));
            }
        }
        TaskThumbnail taskThumbnail = new TaskThumbnail();
        getLastThumbnail(taskThumbnail);
        return taskThumbnail;
    }

    public void removeTaskActivitiesLocked() {
        performClearTaskAtIndexLocked(LOCK_TASK_AUTH_DONT_LOCK);
    }

    String lockTaskAuthToString() {
        switch (this.mLockTaskAuth) {
            case LOCK_TASK_AUTH_DONT_LOCK /*0*/:
                return "LOCK_TASK_AUTH_DONT_LOCK";
            case LOCK_TASK_AUTH_PINNABLE /*1*/:
                return "LOCK_TASK_AUTH_PINNABLE";
            case LOCK_TASK_AUTH_LAUNCHABLE /*2*/:
                return "LOCK_TASK_AUTH_LAUNCHABLE";
            case LOCK_TASK_AUTH_WHITELISTED /*3*/:
                return "LOCK_TASK_AUTH_WHITELISTED";
            case LOCK_TASK_AUTH_LAUNCHABLE_PRIV /*4*/:
                return "LOCK_TASK_AUTH_LAUNCHABLE_PRIV";
            default:
                return "unknown=" + this.mLockTaskAuth;
        }
    }

    void setLockTaskAuth() {
        int i = LOCK_TASK_AUTH_PINNABLE;
        if (!this.mPrivileged && (this.mLockTaskMode == LOCK_TASK_AUTH_LAUNCHABLE || this.mLockTaskMode == LOCK_TASK_AUTH_PINNABLE)) {
            this.mLockTaskMode = LOCK_TASK_AUTH_DONT_LOCK;
        }
        switch (this.mLockTaskMode) {
            case LOCK_TASK_AUTH_DONT_LOCK /*0*/:
                if (isLockTaskWhitelistedLocked()) {
                    i = LOCK_TASK_AUTH_WHITELISTED;
                }
                this.mLockTaskAuth = i;
                break;
            case LOCK_TASK_AUTH_PINNABLE /*1*/:
                this.mLockTaskAuth = LOCK_TASK_AUTH_DONT_LOCK;
                break;
            case LOCK_TASK_AUTH_LAUNCHABLE /*2*/:
                this.mLockTaskAuth = LOCK_TASK_AUTH_LAUNCHABLE_PRIV;
                break;
            case LOCK_TASK_AUTH_WHITELISTED /*3*/:
                if (isLockTaskWhitelistedLocked()) {
                    i = LOCK_TASK_AUTH_LAUNCHABLE;
                }
                this.mLockTaskAuth = i;
                break;
        }
        if (ActivityManagerDebugConfig.DEBUG_LOCKTASK) {
            Slog.d(TAG_LOCKTASK, "setLockTaskAuth: task=" + this + " mLockTaskAuth=" + lockTaskAuthToString());
        }
    }

    boolean isLockTaskWhitelistedLocked() {
        String pkg = null;
        if (this.realActivity != null) {
            pkg = this.realActivity.getPackageName();
        }
        if (pkg == null) {
            return false;
        }
        String[] packages = (String[]) this.mService.mLockTaskPackages.get(this.userId);
        if (packages == null) {
            return false;
        }
        for (int i = packages.length + INVALID_TASK_ID; i >= 0; i += INVALID_TASK_ID) {
            if (pkg.equals(packages[i])) {
                return true;
            }
        }
        return false;
    }

    boolean isHomeTask() {
        return this.taskType == LOCK_TASK_AUTH_PINNABLE;
    }

    boolean isRecentsTask() {
        return this.taskType == LOCK_TASK_AUTH_LAUNCHABLE;
    }

    boolean isApplicationTask() {
        return this.taskType == 0;
    }

    boolean isOverHomeStack() {
        return this.mTaskToReturnTo == LOCK_TASK_AUTH_PINNABLE || this.mTaskToReturnTo == LOCK_TASK_AUTH_LAUNCHABLE;
    }

    boolean isResizeable() {
        if (isHomeTask()) {
            return false;
        }
        if ((this.mService.mForceResizableActivities || ActivityInfo.isResizeableMode(this.mResizeMode)) && !this.mTemporarilyUnresizable) {
            return true;
        }
        return false;
    }

    boolean inCropWindowsResizeMode() {
        return !isResizeable() && this.mResizeMode == LOCK_TASK_AUTH_PINNABLE;
    }

    boolean canGoInDockedStack() {
        return !isResizeable() ? inCropWindowsResizeMode() : true;
    }

    final ActivityRecord findActivityInHistoryLocked(ActivityRecord r) {
        ComponentName realActivity = r.realActivity;
        for (int activityNdx = this.mActivities.size() + INVALID_TASK_ID; activityNdx >= 0; activityNdx += INVALID_TASK_ID) {
            ActivityRecord candidate = (ActivityRecord) this.mActivities.get(activityNdx);
            if (!candidate.finishing && candidate.realActivity.equals(realActivity)) {
                return candidate;
            }
        }
        return null;
    }

    void updateTaskDescription() {
        int numActivities = this.mActivities.size();
        boolean relinquish = numActivities == 0 ? false : (((ActivityRecord) this.mActivities.get(LOCK_TASK_AUTH_DONT_LOCK)).info.flags & DumpState.DUMP_PREFERRED) != 0;
        int activityNdx = Math.min(numActivities, LOCK_TASK_AUTH_PINNABLE);
        while (activityNdx < numActivities) {
            ActivityRecord r = (ActivityRecord) this.mActivities.get(activityNdx);
            if (!relinquish || (r.info.flags & DumpState.DUMP_PREFERRED) != 0) {
                if (r.intent != null && (r.intent.getFlags() & DumpState.DUMP_FROZEN) != 0) {
                    break;
                }
                activityNdx += LOCK_TASK_AUTH_PINNABLE;
            } else {
                activityNdx += LOCK_TASK_AUTH_PINNABLE;
                break;
            }
        }
        if (activityNdx > 0) {
            String label = null;
            String iconFilename = null;
            int colorPrimary = LOCK_TASK_AUTH_DONT_LOCK;
            int colorBackground = LOCK_TASK_AUTH_DONT_LOCK;
            for (activityNdx += INVALID_TASK_ID; activityNdx >= 0; activityNdx += INVALID_TASK_ID) {
                r = (ActivityRecord) this.mActivities.get(activityNdx);
                if (r.taskDescription != null) {
                    if (label == null) {
                        label = r.taskDescription.getLabel();
                    }
                    if (iconFilename == null) {
                        iconFilename = r.taskDescription.getIconFilename();
                    }
                    if (colorPrimary == 0) {
                        colorPrimary = r.taskDescription.getPrimaryColor();
                    }
                    if (colorBackground == 0) {
                        colorBackground = r.taskDescription.getBackgroundColor();
                    }
                }
            }
            this.lastTaskDescription = new TaskDescription(label, null, iconFilename, colorPrimary, colorBackground);
            if (this.taskId == this.mAffiliatedTaskId) {
                this.mAffiliatedTaskColor = this.lastTaskDescription.getPrimaryColor();
            }
        }
    }

    int findEffectiveRootIndex() {
        int effectiveNdx = LOCK_TASK_AUTH_DONT_LOCK;
        int topActivityNdx = this.mActivities.size() + INVALID_TASK_ID;
        for (int activityNdx = LOCK_TASK_AUTH_DONT_LOCK; activityNdx <= topActivityNdx; activityNdx += LOCK_TASK_AUTH_PINNABLE) {
            ActivityRecord r = (ActivityRecord) this.mActivities.get(activityNdx);
            if (!r.finishing) {
                effectiveNdx = activityNdx;
                if ((r.info.flags & DumpState.DUMP_PREFERRED) == 0) {
                    break;
                }
            }
        }
        return effectiveNdx;
    }

    void updateEffectiveIntent() {
        setIntent((ActivityRecord) this.mActivities.get(findEffectiveRootIndex()));
    }

    void saveToXml(XmlSerializer out) throws IOException, XmlPullParserException {
        if (ActivityManagerDebugConfig.DEBUG_RECENTS) {
            Slog.i(TAG_RECENTS, "Saving task=" + this);
        }
        out.attribute(null, ATTR_TASKID, String.valueOf(this.taskId));
        if (this.realActivity != null) {
            out.attribute(null, ATTR_REALACTIVITY, this.realActivity.flattenToShortString());
        }
        out.attribute(null, ATTR_REALACTIVITY_SUSPENDED, String.valueOf(this.realActivitySuspended));
        if (this.origActivity != null) {
            out.attribute(null, ATTR_ORIGACTIVITY, this.origActivity.flattenToShortString());
        }
        if (this.affinity != null) {
            out.attribute(null, ATTR_AFFINITY, this.affinity);
            if (!this.affinity.equals(this.rootAffinity)) {
                out.attribute(null, ATTR_ROOT_AFFINITY, this.rootAffinity != null ? this.rootAffinity : "@");
            }
        } else if (this.rootAffinity != null) {
            out.attribute(null, ATTR_ROOT_AFFINITY, this.rootAffinity != null ? this.rootAffinity : "@");
        }
        out.attribute(null, ATTR_ROOTHASRESET, String.valueOf(this.rootWasReset));
        out.attribute(null, ATTR_AUTOREMOVERECENTS, String.valueOf(this.autoRemoveRecents));
        out.attribute(null, ATTR_ASKEDCOMPATMODE, String.valueOf(this.askedCompatMode));
        out.attribute(null, ATTR_USERID, String.valueOf(this.userId));
        out.attribute(null, ATTR_USER_SETUP_COMPLETE, String.valueOf(this.mUserSetupComplete));
        out.attribute(null, ATTR_EFFECTIVE_UID, String.valueOf(this.effectiveUid));
        out.attribute(null, ATTR_TASKTYPE, String.valueOf(this.taskType));
        out.attribute(null, ATTR_FIRSTACTIVETIME, String.valueOf(this.firstActiveTime));
        out.attribute(null, ATTR_LASTACTIVETIME, String.valueOf(this.lastActiveTime));
        out.attribute(null, ATTR_LASTTIMEMOVED, String.valueOf(this.mLastTimeMoved));
        out.attribute(null, ATTR_NEVERRELINQUISH, String.valueOf(this.mNeverRelinquishIdentity));
        if (this.lastDescription != null) {
            out.attribute(null, ATTR_LASTDESCRIPTION, this.lastDescription.toString());
        }
        if (this.lastTaskDescription != null) {
            this.lastTaskDescription.saveToXml(out);
        }
        this.mLastThumbnailInfo.saveToXml(out);
        out.attribute(null, ATTR_TASK_AFFILIATION_COLOR, String.valueOf(this.mAffiliatedTaskColor));
        out.attribute(null, ATTR_TASK_AFFILIATION, String.valueOf(this.mAffiliatedTaskId));
        out.attribute(null, ATTR_PREV_AFFILIATION, String.valueOf(this.mPrevAffiliateTaskId));
        out.attribute(null, ATTR_NEXT_AFFILIATION, String.valueOf(this.mNextAffiliateTaskId));
        out.attribute(null, ATTR_CALLING_UID, String.valueOf(this.mCallingUid));
        out.attribute(null, ATTR_CALLING_PACKAGE, this.mCallingPackage == null ? "" : this.mCallingPackage);
        out.attribute(null, ATTR_RESIZE_MODE, String.valueOf(this.mResizeMode));
        out.attribute(null, ATTR_PRIVILEGED, String.valueOf(this.mPrivileged));
        if (this.mLastNonFullscreenBounds != null) {
            out.attribute(null, ATTR_NON_FULLSCREEN_BOUNDS, this.mLastNonFullscreenBounds.flattenToString());
        }
        out.attribute(null, ATTR_MIN_WIDTH, String.valueOf(this.mMinWidth));
        out.attribute(null, ATTR_MIN_HEIGHT, String.valueOf(this.mMinHeight));
        if (this.affinityIntent != null) {
            out.startTag(null, TAG_AFFINITYINTENT);
            this.affinityIntent.saveToXml(out);
            out.endTag(null, TAG_AFFINITYINTENT);
        }
        out.startTag(null, TAG_INTENT);
        this.intent.saveToXml(out);
        out.endTag(null, TAG_INTENT);
        ArrayList<ActivityRecord> activities = this.mActivities;
        int numActivities = activities.size();
        int activityNdx = LOCK_TASK_AUTH_DONT_LOCK;
        while (activityNdx < numActivities) {
            ActivityRecord r = (ActivityRecord) activities.get(activityNdx);
            if (r.info.persistableMode != 0 && r.isPersistable()) {
                if (((r.intent.getFlags() & DumpState.DUMP_FROZEN) | DumpState.DUMP_PREFERRED_XML) != DumpState.DUMP_FROZEN || activityNdx <= 0) {
                    out.startTag(null, TAG_ACTIVITY);
                    r.saveToXml(out);
                    out.endTag(null, TAG_ACTIVITY);
                    activityNdx += LOCK_TASK_AUTH_PINNABLE;
                } else {
                    return;
                }
            }
            return;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    static TaskRecord restoreFromXml(XmlPullParser in, ActivityStackSupervisor stackSupervisor) throws IOException, XmlPullParserException {
        Intent intent = null;
        Intent intent2 = null;
        ArrayList<ActivityRecord> activities = new ArrayList();
        ComponentName realActivity = null;
        boolean realActivitySuspended = false;
        ComponentName origActivity = null;
        String affinity = null;
        String rootAffinity = null;
        boolean hasRootAffinity = false;
        boolean rootHasReset = false;
        boolean autoRemoveRecents = false;
        boolean askedCompatMode = false;
        int taskType = LOCK_TASK_AUTH_DONT_LOCK;
        int userId = LOCK_TASK_AUTH_DONT_LOCK;
        boolean userSetupComplete = true;
        int effectiveUid = INVALID_TASK_ID;
        String lastDescription = null;
        long firstActiveTime = -1;
        long lastActiveTime = -1;
        long lastTimeOnTop = 0;
        boolean neverRelinquishIdentity = true;
        int taskId = INVALID_TASK_ID;
        int outerDepth = in.getDepth();
        TaskDescription taskDescription = new TaskDescription();
        TaskThumbnailInfo thumbnailInfo = new TaskThumbnailInfo();
        int taskAffiliation = INVALID_TASK_ID;
        int taskAffiliationColor = LOCK_TASK_AUTH_DONT_LOCK;
        int prevTaskId = INVALID_TASK_ID;
        int nextTaskId = INVALID_TASK_ID;
        int callingUid = INVALID_TASK_ID;
        String callingPackage = "";
        int resizeMode = LOCK_TASK_AUTH_LAUNCHABLE_PRIV;
        boolean privileged = false;
        Rect bounds = null;
        int minWidth = INVALID_TASK_ID;
        int minHeight = INVALID_TASK_ID;
        for (int attrNdx = in.getAttributeCount() + INVALID_TASK_ID; attrNdx >= 0; attrNdx += INVALID_TASK_ID) {
            String attrName = in.getAttributeName(attrNdx);
            String attrValue = in.getAttributeValue(attrNdx);
            if (ATTR_TASKID.equals(attrName)) {
                if (taskId == INVALID_TASK_ID) {
                    taskId = Integer.parseInt(attrValue);
                }
            } else if (ATTR_REALACTIVITY.equals(attrName)) {
                realActivity = ComponentName.unflattenFromString(attrValue);
            } else if (ATTR_REALACTIVITY_SUSPENDED.equals(attrName)) {
                realActivitySuspended = Boolean.valueOf(attrValue).booleanValue();
            } else if (ATTR_ORIGACTIVITY.equals(attrName)) {
                origActivity = ComponentName.unflattenFromString(attrValue);
            } else if (ATTR_AFFINITY.equals(attrName)) {
                affinity = attrValue;
            } else if (ATTR_ROOT_AFFINITY.equals(attrName)) {
                rootAffinity = attrValue;
                hasRootAffinity = true;
            } else if (ATTR_ROOTHASRESET.equals(attrName)) {
                rootHasReset = Boolean.valueOf(attrValue).booleanValue();
            } else if (ATTR_AUTOREMOVERECENTS.equals(attrName)) {
                autoRemoveRecents = Boolean.valueOf(attrValue).booleanValue();
            } else if (ATTR_ASKEDCOMPATMODE.equals(attrName)) {
                askedCompatMode = Boolean.valueOf(attrValue).booleanValue();
            } else if (ATTR_USERID.equals(attrName)) {
                userId = Integer.parseInt(attrValue);
            } else if (ATTR_USER_SETUP_COMPLETE.equals(attrName)) {
                userSetupComplete = Boolean.valueOf(attrValue).booleanValue();
            } else if (ATTR_EFFECTIVE_UID.equals(attrName)) {
                effectiveUid = Integer.parseInt(attrValue);
            } else if (ATTR_TASKTYPE.equals(attrName)) {
                taskType = Integer.parseInt(attrValue);
            } else if (ATTR_FIRSTACTIVETIME.equals(attrName)) {
                firstActiveTime = Long.valueOf(attrValue).longValue();
            } else if (ATTR_LASTACTIVETIME.equals(attrName)) {
                lastActiveTime = Long.valueOf(attrValue).longValue();
            } else if (ATTR_LASTDESCRIPTION.equals(attrName)) {
                lastDescription = attrValue;
            } else if (ATTR_LASTTIMEMOVED.equals(attrName)) {
                lastTimeOnTop = Long.valueOf(attrValue).longValue();
            } else if (ATTR_NEVERRELINQUISH.equals(attrName)) {
                neverRelinquishIdentity = Boolean.valueOf(attrValue).booleanValue();
            } else {
                if (attrName.startsWith("task_thumbnailinfo_")) {
                    thumbnailInfo.restoreFromXml(attrName, attrValue);
                } else {
                    if (attrName.startsWith("task_description_")) {
                        taskDescription.restoreFromXml(attrName, attrValue);
                    } else if (ATTR_TASK_AFFILIATION.equals(attrName)) {
                        taskAffiliation = Integer.parseInt(attrValue);
                    } else if (ATTR_PREV_AFFILIATION.equals(attrName)) {
                        prevTaskId = Integer.parseInt(attrValue);
                    } else if (ATTR_NEXT_AFFILIATION.equals(attrName)) {
                        nextTaskId = Integer.parseInt(attrValue);
                    } else if (ATTR_TASK_AFFILIATION_COLOR.equals(attrName)) {
                        taskAffiliationColor = Integer.parseInt(attrValue);
                    } else if (ATTR_CALLING_UID.equals(attrName)) {
                        callingUid = Integer.parseInt(attrValue);
                    } else if (ATTR_CALLING_PACKAGE.equals(attrName)) {
                        callingPackage = attrValue;
                    } else if (ATTR_RESIZE_MODE.equals(attrName)) {
                        resizeMode = Integer.parseInt(attrValue);
                        if (resizeMode == LOCK_TASK_AUTH_PINNABLE) {
                            resizeMode = LOCK_TASK_AUTH_LAUNCHABLE_PRIV;
                        }
                    } else if (ATTR_PRIVILEGED.equals(attrName)) {
                        privileged = Boolean.valueOf(attrValue).booleanValue();
                    } else if (ATTR_NON_FULLSCREEN_BOUNDS.equals(attrName)) {
                        bounds = Rect.unflattenFromString(attrValue);
                    } else if (ATTR_MIN_WIDTH.equals(attrName)) {
                        minWidth = Integer.parseInt(attrValue);
                    } else if (ATTR_MIN_HEIGHT.equals(attrName)) {
                        minHeight = Integer.parseInt(attrValue);
                    } else {
                        Slog.w(TAG, "TaskRecord: Unknown attribute=" + attrName);
                    }
                }
            }
        }
        while (true) {
            int event = in.next();
            if (event == LOCK_TASK_AUTH_PINNABLE || (event == LOCK_TASK_AUTH_WHITELISTED && in.getDepth() < outerDepth)) {
                if (!hasRootAffinity) {
                    rootAffinity = affinity;
                } else if ("@".equals(rootAffinity)) {
                    rootAffinity = null;
                }
            } else if (event == LOCK_TASK_AUTH_LAUNCHABLE) {
                String name = in.getName();
                if (TAG_AFFINITYINTENT.equals(name)) {
                    intent2 = Intent.restoreFromXml(in);
                } else if (TAG_INTENT.equals(name)) {
                    intent = Intent.restoreFromXml(in);
                } else if (TAG_ACTIVITY.equals(name)) {
                    ActivityRecord activity = ActivityRecord.restoreFromXml(in, stackSupervisor);
                    if (activity != null) {
                        activities.add(activity);
                    }
                } else {
                    Slog.e(TAG, "restoreTask: Unexpected name=" + name);
                    XmlUtils.skipCurrentTag(in);
                }
            }
        }
        if (!hasRootAffinity) {
            rootAffinity = affinity;
        } else if ("@".equals(rootAffinity)) {
            rootAffinity = null;
        }
        if (effectiveUid <= 0) {
            Intent checkIntent;
            if (intent != null) {
                checkIntent = intent;
            } else {
                checkIntent = intent2;
            }
            effectiveUid = LOCK_TASK_AUTH_DONT_LOCK;
            if (checkIntent != null) {
                try {
                    ApplicationInfo ai = AppGlobals.getPackageManager().getApplicationInfo(checkIntent.getComponent().getPackageName(), 8704, userId);
                    if (ai != null) {
                        effectiveUid = ai.uid;
                    }
                } catch (RemoteException e) {
                }
            }
            Slog.w(TAG, "Updating task #" + taskId + " for " + checkIntent + ": effectiveUid=" + effectiveUid);
        }
        TaskRecord task = HwServiceFactory.createTaskRecord(stackSupervisor.mService, taskId, intent, intent2, affinity, rootAffinity, realActivity, origActivity, rootHasReset, autoRemoveRecents, askedCompatMode, taskType, userId, effectiveUid, lastDescription, activities, firstActiveTime, lastActiveTime, lastTimeOnTop, neverRelinquishIdentity, taskDescription, thumbnailInfo, taskAffiliation, prevTaskId, nextTaskId, taskAffiliationColor, callingUid, callingPackage, resizeMode, privileged, realActivitySuspended, userSetupComplete, minWidth, minHeight);
        task.updateOverrideConfiguration(bounds);
        for (int activityNdx = activities.size() + INVALID_TASK_ID; activityNdx >= 0; activityNdx += INVALID_TASK_ID) {
            ((ActivityRecord) activities.get(activityNdx)).task = task;
        }
        if (ActivityManagerDebugConfig.DEBUG_RECENTS) {
            Slog.d(TAG_RECENTS, "Restored task=" + task);
        }
        return task;
    }

    private void adjustForMinimalTaskDimensions(Rect bounds) {
        if (bounds != null) {
            int minWidth = this.mMinWidth;
            int minHeight = this.mMinHeight;
            if (this.stack.mStackId != LOCK_TASK_AUTH_LAUNCHABLE_PRIV) {
                if (minWidth == INVALID_TASK_ID) {
                    minWidth = this.mService.mStackSupervisor.mDefaultMinSizeOfResizeableTask;
                }
                if (minHeight == INVALID_TASK_ID) {
                    minHeight = this.mService.mStackSupervisor.mDefaultMinSizeOfResizeableTask;
                }
            }
            boolean adjustWidth = minWidth > bounds.width();
            boolean adjustHeight = minHeight > bounds.height();
            if (!adjustWidth ? adjustHeight : true) {
                if (adjustWidth) {
                    if (this.mBounds == null || bounds.right != this.mBounds.right) {
                        bounds.right = bounds.left + minWidth;
                    } else {
                        bounds.left = bounds.right - minWidth;
                    }
                }
                if (adjustHeight) {
                    if (this.mBounds == null || bounds.bottom != this.mBounds.bottom) {
                        bounds.bottom = bounds.top + minHeight;
                    } else {
                        bounds.top = bounds.bottom - minHeight;
                    }
                }
            }
        }
    }

    Configuration updateOverrideConfiguration(Rect bounds) {
        return updateOverrideConfiguration(bounds, null);
    }

    Configuration updateOverrideConfiguration(Rect bounds, Rect insetBounds) {
        boolean z = true;
        ActivityRecord topActivity = getTopActivity();
        if (Objects.equals(this.mBounds, bounds) && (topActivity == null || !this.mService.getPackageManagerInternalLocked().isInMWPortraitWhiteList(topActivity.packageName))) {
            return null;
        }
        boolean z2;
        Configuration configuration;
        Configuration oldConfig = this.mOverrideConfig;
        boolean oldFullscreen = this.mFullscreen;
        if (bounds == null) {
            z2 = true;
        } else {
            z2 = false;
        }
        this.mFullscreen = z2;
        if (this.mFullscreen) {
            if (this.mBounds != null && StackId.persistTaskBounds(this.stack.mStackId)) {
                this.mLastNonFullscreenBounds = this.mBounds;
            }
            this.mBounds = null;
            this.mOverrideConfig = Configuration.EMPTY;
        } else {
            this.mTmpRect.set(bounds);
            adjustForMinimalTaskDimensions(this.mTmpRect);
            if (this.mBounds == null) {
                this.mBounds = new Rect(this.mTmpRect);
            } else {
                this.mBounds.set(this.mTmpRect);
            }
            if (this.stack == null || StackId.persistTaskBounds(this.stack.mStackId)) {
                this.mLastNonFullscreenBounds = this.mBounds;
            }
            Rect rect = this.mTmpRect;
            if (this.mTmpRect.right != bounds.right) {
                z2 = true;
            } else {
                z2 = false;
            }
            if (this.mTmpRect.bottom == bounds.bottom) {
                z = false;
            }
            this.mOverrideConfig = calculateOverrideConfig(rect, insetBounds, z2, z);
        }
        if (this.mFullscreen != oldFullscreen) {
            this.mService.mStackSupervisor.scheduleReportMultiWindowModeChanged(this);
        }
        if (this.mOverrideConfig.equals(oldConfig)) {
            configuration = null;
        } else {
            configuration = this.mOverrideConfig;
        }
        return configuration;
    }

    private void subtractNonDecorInsets(Rect inOutBounds, Rect inInsetBounds, boolean overrideWidth, boolean overrideHeight) {
        this.mTmpRect2.set(inInsetBounds);
        this.mService.mWindowManager.subtractNonDecorInsets(this.mTmpRect2);
        inOutBounds.inset(this.mTmpRect2.left - inInsetBounds.left, this.mTmpRect2.top - inInsetBounds.top, overrideWidth ? LOCK_TASK_AUTH_DONT_LOCK : inInsetBounds.right - this.mTmpRect2.right, overrideHeight ? LOCK_TASK_AUTH_DONT_LOCK : inInsetBounds.bottom - this.mTmpRect2.bottom);
    }

    private void subtractStableInsets(Rect inOutBounds, Rect inInsetBounds, boolean overrideWidth, boolean overrideHeight) {
        this.mTmpRect2.set(inInsetBounds);
        this.mService.mWindowManager.subtractStableInsets(this.mTmpRect2);
        inOutBounds.inset(this.mTmpRect2.left - inInsetBounds.left, this.mTmpRect2.top - inInsetBounds.top, overrideWidth ? LOCK_TASK_AUTH_DONT_LOCK : inInsetBounds.right - this.mTmpRect2.right, overrideHeight ? LOCK_TASK_AUTH_DONT_LOCK : inInsetBounds.bottom - this.mTmpRect2.bottom);
    }

    private Configuration calculateOverrideConfig(Rect bounds, Rect insetBounds, boolean overrideWidth, boolean overrideHeight) {
        Rect rect;
        int i;
        this.mTmpNonDecorBounds.set(bounds);
        this.mTmpStableBounds.set(bounds);
        Rect rect2 = this.mTmpNonDecorBounds;
        if (insetBounds != null) {
            rect = insetBounds;
        } else {
            rect = bounds;
        }
        subtractNonDecorInsets(rect2, rect, overrideWidth, overrideHeight);
        rect2 = this.mTmpStableBounds;
        if (insetBounds != null) {
            rect = insetBounds;
        } else {
            rect = bounds;
        }
        subtractStableInsets(rect2, rect, overrideWidth, overrideHeight);
        Configuration serviceConfig = this.mService.mConfiguration;
        Configuration config = new Configuration(Configuration.EMPTY);
        float density = ((float) serviceConfig.densityDpi) * 0.00625f;
        config.screenWidthDp = Math.min((int) (((float) this.mTmpStableBounds.width()) / density), serviceConfig.screenWidthDp);
        config.screenHeightDp = Math.min((int) (((float) this.mTmpStableBounds.height()) / density), serviceConfig.screenHeightDp);
        if (config.screenWidthDp <= config.screenHeightDp) {
            i = LOCK_TASK_AUTH_PINNABLE;
        } else {
            i = LOCK_TASK_AUTH_LAUNCHABLE;
        }
        config.orientation = i;
        overrideConfigOrienForFreeForm(config);
        config.fontScale = serviceConfig.fontScale;
        int compatScreenWidthDp = (int) (((float) this.mTmpNonDecorBounds.width()) / density);
        int compatScreenHeightDp = (int) (((float) this.mTmpNonDecorBounds.height()) / density);
        config.screenLayout = Configuration.reduceScreenLayout(Configuration.resetScreenLayout(serviceConfig.screenLayout), Math.max(compatScreenHeightDp, compatScreenWidthDp), Math.min(compatScreenHeightDp, compatScreenWidthDp));
        WindowManagerService windowManagerService = this.mService.mWindowManager;
        if (insetBounds == null) {
            insetBounds = bounds;
        }
        config.smallestScreenWidthDp = windowManagerService.getSmallestWidthForTaskBounds(insetBounds);
        return config;
    }

    Configuration extractOverrideConfig(Configuration config) {
        Configuration extracted = new Configuration(Configuration.EMPTY);
        extracted.screenWidthDp = config.screenWidthDp;
        extracted.screenHeightDp = config.screenHeightDp;
        extracted.smallestScreenWidthDp = config.smallestScreenWidthDp;
        extracted.orientation = config.orientation;
        extracted.screenLayout = config.screenLayout;
        extracted.fontScale = config.fontScale;
        return extracted;
    }

    Rect updateOverrideConfigurationFromLaunchBounds() {
        Rect bounds = validateBounds(getLaunchBounds());
        updateOverrideConfiguration(bounds);
        if (bounds != null) {
            bounds.set(this.mBounds);
        }
        return bounds;
    }

    void sanitizeOverrideConfiguration(Configuration globalConfig) {
        if (!this.mFullscreen) {
            int overrideScreenLayout = this.mOverrideConfig.screenLayout;
            this.mOverrideConfig.screenLayout = (((globalConfig.screenLayout & -49) | (overrideScreenLayout & 48)) & -16) | (overrideScreenLayout & 15);
            this.mOverrideConfig.fontScale = globalConfig.fontScale;
        }
    }

    static Rect validateBounds(Rect bounds) {
        if (bounds == null || !bounds.isEmpty()) {
            return bounds;
        }
        Slog.wtf(TAG, "Received strange task bounds: " + bounds, new Throwable());
        return null;
    }

    void updateOverrideConfigurationForStack(ActivityStack inStack) {
        if (this.stack == null || this.stack != inStack) {
            if (inStack.mStackId != LOCK_TASK_AUTH_LAUNCHABLE) {
                updateOverrideConfiguration(inStack.mBounds);
            } else if (!isResizeable()) {
                throw new IllegalArgumentException("Can not position non-resizeable task=" + this + " in stack=" + inStack);
            } else if (this.mBounds == null) {
                if (this.mLastNonFullscreenBounds != null) {
                    updateOverrideConfiguration(this.mLastNonFullscreenBounds);
                } else {
                    inStack.layoutTaskInStack(this, null);
                }
            }
        }
    }

    int getLaunchStackId() {
        if (!isApplicationTask()) {
            return LOCK_TASK_AUTH_DONT_LOCK;
        }
        if (this.mBounds != null) {
            return LOCK_TASK_AUTH_LAUNCHABLE;
        }
        return LOCK_TASK_AUTH_PINNABLE;
    }

    Rect getLaunchBounds() {
        Rect rect = null;
        if (this.mService.mLockScreenShown == LOCK_TASK_AUTH_LAUNCHABLE || this.stack == null) {
            return null;
        }
        int stackId = this.stack.mStackId;
        if (stackId == 0 || stackId == LOCK_TASK_AUTH_PINNABLE || (stackId == LOCK_TASK_AUTH_WHITELISTED && !isResizeable())) {
            if (isResizeable()) {
                rect = this.stack.mBounds;
            }
            return rect;
        } else if (StackId.persistTaskBounds(stackId)) {
            return this.mLastNonFullscreenBounds;
        } else {
            return this.stack.mBounds;
        }
    }

    boolean canMatchRootAffinity() {
        return this.rootAffinity != null && (this.stack == null || this.stack.mStackId != LOCK_TASK_AUTH_LAUNCHABLE_PRIV);
    }

    void dump(PrintWriter pw, String prefix) {
        pw.print(prefix);
        pw.print("userId=");
        pw.print(this.userId);
        pw.print(" multiLaunchId=");
        pw.print(this.multiLaunchId);
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
            if (this.affinity == null || !this.affinity.equals(this.rootAffinity)) {
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
            StringBuilder sb = new StringBuilder(DumpState.DUMP_PACKAGES);
            sb.append(prefix);
            sb.append("intent={");
            this.intent.toShortString(sb, true, true, false, true);
            sb.append('}');
            pw.println(sb.toString());
        }
        if (this.affinityIntent != null) {
            sb = new StringBuilder(DumpState.DUMP_PACKAGES);
            sb.append(prefix);
            sb.append("affinityIntent={");
            this.affinityIntent.toShortString(sb, true, true, false, true);
            sb.append('}');
            pw.println(sb.toString());
        }
        if (this.origActivity != null) {
            pw.print(prefix);
            pw.print("origActivity=");
            pw.println(this.origActivity.flattenToShortString());
        }
        if (this.realActivity != null) {
            pw.print(prefix);
            pw.print("realActivity=");
            pw.println(this.realActivity.flattenToShortString());
        }
        if (!this.autoRemoveRecents && !this.isPersistable && this.taskType == 0 && this.mTaskToReturnTo == 0) {
            if (this.numFullscreen != 0) {
            }
            if (this.rootWasReset || this.mNeverRelinquishIdentity || this.mReuseTask || this.mLockTaskAuth != LOCK_TASK_AUTH_PINNABLE) {
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
            if (this.mAffiliatedTaskId == this.taskId && this.mPrevAffiliateTaskId == INVALID_TASK_ID && this.mPrevAffiliate == null && this.mNextAffiliateTaskId == INVALID_TASK_ID) {
                if (this.mNextAffiliate != null) {
                }
                pw.print(prefix);
                pw.print("Activities=");
                pw.println(this.mActivities);
                if (!(this.askedCompatMode && this.inRecents && this.isAvailable)) {
                    pw.print(prefix);
                    pw.print("askedCompatMode=");
                    pw.print(this.askedCompatMode);
                    pw.print(" inRecents=");
                    pw.print(this.inRecents);
                    pw.print(" isAvailable=");
                    pw.println(this.isAvailable);
                }
                pw.print(prefix);
                pw.print("lastThumbnail=");
                pw.print(this.mLastThumbnail);
                pw.print(" lastThumbnailFile=");
                pw.println(this.mLastThumbnailFile);
                if (this.lastDescription != null) {
                    pw.print(prefix);
                    pw.print("lastDescription=");
                    pw.println(this.lastDescription);
                }
                if (this.stack != null) {
                    pw.print(prefix);
                    pw.print("stackId=");
                    pw.println(this.stack.mStackId);
                }
                pw.print(prefix + "hasBeenVisible=" + this.hasBeenVisible);
                pw.print(" mResizeMode=" + ActivityInfo.resizeModeToString(this.mResizeMode));
                pw.print(" isResizeable=" + isResizeable());
                pw.print(" firstActiveTime=" + this.lastActiveTime);
                pw.print(" lastActiveTime=" + this.lastActiveTime);
                pw.println(" (inactive for " + (getInactiveDuration() / 1000) + "s)");
            }
            pw.print(prefix);
            pw.print("affiliation=");
            pw.print(this.mAffiliatedTaskId);
            pw.print(" prevAffiliation=");
            pw.print(this.mPrevAffiliateTaskId);
            pw.print(" (");
            if (this.mPrevAffiliate != null) {
                pw.print("null");
            } else {
                pw.print(Integer.toHexString(System.identityHashCode(this.mPrevAffiliate)));
            }
            pw.print(") nextAffiliation=");
            pw.print(this.mNextAffiliateTaskId);
            pw.print(" (");
            if (this.mNextAffiliate != null) {
                pw.print("null");
            } else {
                pw.print(Integer.toHexString(System.identityHashCode(this.mNextAffiliate)));
            }
            pw.println(")");
            pw.print(prefix);
            pw.print("Activities=");
            pw.println(this.mActivities);
            pw.print(prefix);
            pw.print("askedCompatMode=");
            pw.print(this.askedCompatMode);
            pw.print(" inRecents=");
            pw.print(this.inRecents);
            pw.print(" isAvailable=");
            pw.println(this.isAvailable);
            pw.print(prefix);
            pw.print("lastThumbnail=");
            pw.print(this.mLastThumbnail);
            pw.print(" lastThumbnailFile=");
            pw.println(this.mLastThumbnailFile);
            if (this.lastDescription != null) {
                pw.print(prefix);
                pw.print("lastDescription=");
                pw.println(this.lastDescription);
            }
            if (this.stack != null) {
                pw.print(prefix);
                pw.print("stackId=");
                pw.println(this.stack.mStackId);
            }
            pw.print(prefix + "hasBeenVisible=" + this.hasBeenVisible);
            pw.print(" mResizeMode=" + ActivityInfo.resizeModeToString(this.mResizeMode));
            pw.print(" isResizeable=" + isResizeable());
            pw.print(" firstActiveTime=" + this.lastActiveTime);
            pw.print(" lastActiveTime=" + this.lastActiveTime);
            pw.println(" (inactive for " + (getInactiveDuration() / 1000) + "s)");
        }
        pw.print(prefix);
        pw.print("autoRemoveRecents=");
        pw.print(this.autoRemoveRecents);
        pw.print(" isPersistable=");
        pw.print(this.isPersistable);
        pw.print(" numFullscreen=");
        pw.print(this.numFullscreen);
        pw.print(" taskType=");
        pw.print(this.taskType);
        pw.print(" mTaskToReturnTo=");
        pw.println(this.mTaskToReturnTo);
        pw.print(prefix);
        pw.print("rootWasReset=");
        pw.print(this.rootWasReset);
        pw.print(" mNeverRelinquishIdentity=");
        pw.print(this.mNeverRelinquishIdentity);
        pw.print(" mReuseTask=");
        pw.print(this.mReuseTask);
        pw.print(" mLockTaskAuth=");
        pw.println(lockTaskAuthToString());
        if (this.mNextAffiliate != null) {
            pw.print(prefix);
            pw.print("affiliation=");
            pw.print(this.mAffiliatedTaskId);
            pw.print(" prevAffiliation=");
            pw.print(this.mPrevAffiliateTaskId);
            pw.print(" (");
            if (this.mPrevAffiliate != null) {
                pw.print(Integer.toHexString(System.identityHashCode(this.mPrevAffiliate)));
            } else {
                pw.print("null");
            }
            pw.print(") nextAffiliation=");
            pw.print(this.mNextAffiliateTaskId);
            pw.print(" (");
            if (this.mNextAffiliate != null) {
                pw.print(Integer.toHexString(System.identityHashCode(this.mNextAffiliate)));
            } else {
                pw.print("null");
            }
            pw.println(")");
        }
        pw.print(prefix);
        pw.print("Activities=");
        pw.println(this.mActivities);
        pw.print(prefix);
        pw.print("askedCompatMode=");
        pw.print(this.askedCompatMode);
        pw.print(" inRecents=");
        pw.print(this.inRecents);
        pw.print(" isAvailable=");
        pw.println(this.isAvailable);
        pw.print(prefix);
        pw.print("lastThumbnail=");
        pw.print(this.mLastThumbnail);
        pw.print(" lastThumbnailFile=");
        pw.println(this.mLastThumbnailFile);
        if (this.lastDescription != null) {
            pw.print(prefix);
            pw.print("lastDescription=");
            pw.println(this.lastDescription);
        }
        if (this.stack != null) {
            pw.print(prefix);
            pw.print("stackId=");
            pw.println(this.stack.mStackId);
        }
        pw.print(prefix + "hasBeenVisible=" + this.hasBeenVisible);
        pw.print(" mResizeMode=" + ActivityInfo.resizeModeToString(this.mResizeMode));
        pw.print(" isResizeable=" + isResizeable());
        pw.print(" firstActiveTime=" + this.lastActiveTime);
        pw.print(" lastActiveTime=" + this.lastActiveTime);
        pw.println(" (inactive for " + (getInactiveDuration() / 1000) + "s)");
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(DumpState.DUMP_PACKAGES);
        if (this.stringName != null) {
            sb.append(this.stringName);
            sb.append(" U=");
            sb.append(this.userId);
            sb.append(" StackId=");
            sb.append(this.stack != null ? this.stack.mStackId : INVALID_TASK_ID);
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
        } else if (this.affinityIntent != null) {
            sb.append(" aI=");
            sb.append(this.affinityIntent.getComponent().flattenToShortString());
        } else {
            sb.append(" ??");
        }
        this.stringName = sb.toString();
        return toString();
    }

    private void updateMultiLaunchId(Intent intent) {
        int i = LOCK_TASK_AUTH_DONT_LOCK;
        if (intent != null) {
            if ((intent.getHwFlags() & LOCK_TASK_AUTH_PINNABLE) != 0) {
                i = LOCK_TASK_AUTH_PINNABLE;
            }
            this.multiLaunchId = i;
            Flog.i(HdmiCecKeycode.CEC_KEYCODE_SELECT_AV_INPUT_FUNCTION, "create new task record, multiLaunchId: " + this.multiLaunchId + ", intent = " + intent);
        }
    }
}
