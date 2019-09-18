package com.android.server.am;

import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.app.AppGlobals;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.res.Configuration;
import android.freeform.HwFreeFormUtils;
import android.graphics.Rect;
import android.os.Debug;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.Trace;
import android.os.UserHandle;
import android.provider.Settings;
import android.service.voice.IVoiceInteractionSession;
import android.util.HwPCUtils;
import android.util.Slog;
import android.util.proto.ProtoOutputStream;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.app.IVoiceInteractor;
import com.android.internal.util.XmlUtils;
import com.android.server.HwServiceExFactory;
import com.android.server.HwServiceFactory;
import com.android.server.am.ActivityStack;
import com.android.server.backup.internal.BackupHandler;
import com.android.server.os.HwBootFail;
import com.android.server.pm.DumpState;
import com.android.server.wm.AppWindowContainerController;
import com.android.server.wm.ConfigurationContainer;
import com.android.server.wm.TaskWindowContainerController;
import com.android.server.wm.TaskWindowContainerListener;
import com.android.server.wm.WindowManagerService;
import com.huawei.server.am.IHwTaskRecordEx;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Objects;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class TaskRecord extends AbsTaskRecord implements TaskWindowContainerListener {
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
    static final int INVALID_TASK_ID = -1;
    private static final int LAND_ROTATE_VALUE = 270;
    static final int LOCK_TASK_AUTH_DONT_LOCK = 0;
    static final int LOCK_TASK_AUTH_LAUNCHABLE = 2;
    static final int LOCK_TASK_AUTH_LAUNCHABLE_PRIV = 4;
    static final int LOCK_TASK_AUTH_PINNABLE = 1;
    static final int LOCK_TASK_AUTH_WHITELISTED = 3;
    private static final int PERSIST_TASK_VERSION = 1;
    static final int REPARENT_KEEP_STACK_AT_FRONT = 1;
    static final int REPARENT_LEAVE_STACK_IN_PLACE = 2;
    static final int REPARENT_MOVE_STACK_TO_FRONT = 0;
    private static final String TAG = "ActivityManager";
    private static final String TAG_ACTIVITY = "activity";
    private static final String TAG_ADD_REMOVE = "ActivityManager";
    private static final String TAG_AFFINITYINTENT = "affinity_intent";
    private static final String TAG_INTENT = "intent";
    private static final String TAG_LOCKTASK = "ActivityManager";
    private static final String TAG_RECENTS = "ActivityManager";
    private static final String TAG_TASKS = "ActivityManager";
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
    boolean isLaunching;
    boolean isPersistable = false;
    long lastActiveTime;
    CharSequence lastDescription;
    ActivityManager.TaskDescription lastTaskDescription = new ActivityManager.TaskDescription();
    final ArrayList<ActivityRecord> mActivities;
    int mAffiliatedTaskColor;
    int mAffiliatedTaskId;
    String mCallingPackage;
    int mCallingUid;
    int mDefaultMinSize;
    public IHwTaskRecordEx mHwTaskRecordEx = null;
    Rect mLastNonFullscreenBounds = null;
    long mLastTimeMoved = System.currentTimeMillis();
    int mLayerRank = -1;
    int mLockTaskAuth = 1;
    int mLockTaskUid = -1;
    int mMinHeight;
    int mMinWidth;
    private boolean mNeverRelinquishIdentity = true;
    TaskRecord mNextAffiliate;
    int mNextAffiliateTaskId = -1;
    TaskRecord mPrevAffiliate;
    int mPrevAffiliateTaskId = -1;
    int mResizeMode;
    private boolean mReuseTask = false;
    protected ActivityInfo mRootActivityInfo;
    private ProcessRecord mRootProcess;
    final ActivityManagerService mService;
    protected ActivityStack mStack;
    private boolean mSupportsPictureInPicture;
    private Configuration mTmpConfig = new Configuration();
    private final Rect mTmpNonDecorBounds = new Rect();
    protected final Rect mTmpRect = new Rect();
    private final Rect mTmpStableBounds = new Rect();
    boolean mUserSetupComplete;
    private TaskWindowContainerController mWindowContainerController;
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

    static class TaskActivitiesReport {
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

    static class TaskRecordFactory {
        TaskRecordFactory() {
        }

        /* access modifiers changed from: package-private */
        public TaskRecord create(ActivityManagerService service, int taskId, ActivityInfo info, Intent intent, IVoiceInteractionSession voiceSession, IVoiceInteractor voiceInteractor) {
            return HwServiceFactory.createTaskRecord(service, taskId, info, intent, voiceSession, voiceInteractor);
        }

        /* access modifiers changed from: package-private */
        public TaskRecord create(ActivityManagerService service, int taskId, ActivityInfo info, Intent intent, ActivityManager.TaskDescription taskDescription) {
            return HwServiceFactory.createTaskRecord(service, taskId, info, intent, taskDescription);
        }

        /* access modifiers changed from: package-private */
        public TaskRecord create(ActivityManagerService service, int taskId, Intent intent, Intent affinityIntent, String affinity, String rootAffinity, ComponentName realActivity, ComponentName origActivity, boolean rootWasReset, boolean autoRemoveRecents, boolean askedCompatMode, int userId, int effectiveUid, String lastDescription, ArrayList<ActivityRecord> activities, long lastTimeMoved, boolean neverRelinquishIdentity, ActivityManager.TaskDescription lastTaskDescription, int taskAffiliation, int prevTaskId, int nextTaskId, int taskAffiliationColor, int callingUid, String callingPackage, int resizeMode, boolean supportsPictureInPicture, boolean realActivitySuspended, boolean userSetupComplete, int minWidth, int minHeight) {
            return HwServiceFactory.createTaskRecord(service, taskId, intent, affinityIntent, affinity, rootAffinity, realActivity, origActivity, rootWasReset, autoRemoveRecents, askedCompatMode, userId, effectiveUid, lastDescription, activities, lastTimeMoved, neverRelinquishIdentity, lastTaskDescription, taskAffiliation, prevTaskId, nextTaskId, taskAffiliationColor, callingUid, callingPackage, resizeMode, supportsPictureInPicture, realActivitySuspended, userSetupComplete, minWidth, minHeight);
        }

        /* access modifiers changed from: package-private */
        /* JADX WARNING: Can't fix incorrect switch cases order */
        /* JADX WARNING: Removed duplicated region for block: B:158:0x033c  */
        /* JADX WARNING: Removed duplicated region for block: B:171:0x038b  */
        /* JADX WARNING: Removed duplicated region for block: B:174:0x0390 A[ADDED_TO_REGION] */
        /* JADX WARNING: Removed duplicated region for block: B:178:0x0397  */
        /* JADX WARNING: Removed duplicated region for block: B:184:0x0405 A[LOOP:2: B:183:0x0403->B:184:0x0405, LOOP_END] */
        /* JADX WARNING: Removed duplicated region for block: B:187:0x0415  */
        public TaskRecord restoreFromXml(XmlPullParser in, ActivityStackSupervisor stackSupervisor) throws IOException, XmlPullParserException {
            int event;
            String rootAffinity;
            int effectiveUid;
            boolean supportsPictureInPicture;
            int resizeMode;
            int activityNdx;
            String rootAffinity2;
            char c;
            ActivityManager.TaskDescription taskDescription;
            XmlPullParser xmlPullParser = in;
            ArrayList<ActivityRecord> activities = new ArrayList<>();
            String rootAffinity3 = null;
            int effectiveUid2 = -1;
            Intent affinityIntent = null;
            int outerDepth = in.getDepth();
            Intent intent = null;
            ActivityManager.TaskDescription taskDescription2 = new ActivityManager.TaskDescription();
            boolean supportsPictureInPicture2 = false;
            boolean userSetupComplete = true;
            int attrNdx = in.getAttributeCount() - 1;
            int taskAffiliation = -1;
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
            int taskAffiliationColor = 0;
            int prevTaskId = -1;
            int nextTaskId = -1;
            int callingUid = -1;
            String callingPackage = BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
            int resizeMode2 = 4;
            Rect lastNonFullscreenBounds = null;
            int minWidth = -1;
            int minHeight = -1;
            int persistTaskVersion = 0;
            boolean userSetupComplete2 = true;
            boolean neverRelinquishIdentity = true;
            while (true) {
                int attrNdx2 = attrNdx;
                if (attrNdx2 >= 0) {
                    String attrName = xmlPullParser.getAttributeName(attrNdx2);
                    String attrValue = xmlPullParser.getAttributeValue(attrNdx2);
                    switch (attrName.hashCode()) {
                        case -2134816935:
                            if (attrName.equals(TaskRecord.ATTR_ASKEDCOMPATMODE)) {
                                c = 8;
                                break;
                            }
                        case -1556983798:
                            if (attrName.equals(TaskRecord.ATTR_LASTTIMEMOVED)) {
                                c = 14;
                                break;
                            }
                        case -1537240555:
                            if (attrName.equals(TaskRecord.ATTR_TASKID)) {
                                c = 0;
                                break;
                            }
                        case -1494902876:
                            if (attrName.equals(TaskRecord.ATTR_NEXT_AFFILIATION)) {
                                c = 18;
                                break;
                            }
                        case -1292777190:
                            if (attrName.equals(TaskRecord.ATTR_TASK_AFFILIATION_COLOR)) {
                                c = 19;
                                break;
                            }
                        case -1138503444:
                            if (attrName.equals(TaskRecord.ATTR_REALACTIVITY_SUSPENDED)) {
                                c = 2;
                                break;
                            }
                        case -1124927690:
                            if (attrName.equals(TaskRecord.ATTR_TASK_AFFILIATION)) {
                                c = 16;
                                break;
                            }
                        case -974080081:
                            if (attrName.equals(TaskRecord.ATTR_USER_SETUP_COMPLETE)) {
                                c = 10;
                                break;
                            }
                        case -929566280:
                            if (attrName.equals(TaskRecord.ATTR_EFFECTIVE_UID)) {
                                c = 11;
                                break;
                            }
                        case -865458610:
                            if (attrName.equals(TaskRecord.ATTR_RESIZE_MODE)) {
                                c = 22;
                                break;
                            }
                        case -826243148:
                            if (attrName.equals(TaskRecord.ATTR_MIN_HEIGHT)) {
                                c = 26;
                                break;
                            }
                        case -707249465:
                            if (attrName.equals(TaskRecord.ATTR_NON_FULLSCREEN_BOUNDS)) {
                                c = 24;
                                break;
                            }
                        case -705269939:
                            if (attrName.equals(TaskRecord.ATTR_ORIGACTIVITY)) {
                                c = 3;
                                break;
                            }
                        case -502399667:
                            if (attrName.equals(TaskRecord.ATTR_AUTOREMOVERECENTS)) {
                                c = 7;
                                break;
                            }
                        case -360792224:
                            if (attrName.equals(TaskRecord.ATTR_SUPPORTS_PICTURE_IN_PICTURE)) {
                                c = 23;
                                break;
                            }
                        case -162744347:
                            if (attrName.equals(TaskRecord.ATTR_ROOT_AFFINITY)) {
                                c = 5;
                                break;
                            }
                        case -147132913:
                            if (attrName.equals(TaskRecord.ATTR_USERID)) {
                                c = 9;
                                break;
                            }
                        case -132216235:
                            if (attrName.equals(TaskRecord.ATTR_CALLING_UID)) {
                                c = 20;
                                break;
                            }
                        case 180927924:
                            if (attrName.equals(TaskRecord.ATTR_TASKTYPE)) {
                                c = 12;
                                break;
                            }
                        case 331206372:
                            if (attrName.equals(TaskRecord.ATTR_PREV_AFFILIATION)) {
                                c = 17;
                                break;
                            }
                        case 541503897:
                            if (attrName.equals(TaskRecord.ATTR_MIN_WIDTH)) {
                                c = 25;
                                break;
                            }
                        case 605497640:
                            if (attrName.equals(TaskRecord.ATTR_AFFINITY)) {
                                c = 4;
                                break;
                            }
                        case 869221331:
                            if (attrName.equals(TaskRecord.ATTR_LASTDESCRIPTION)) {
                                c = 13;
                                break;
                            }
                        case 1007873193:
                            if (attrName.equals(TaskRecord.ATTR_PERSIST_TASK_VERSION)) {
                                c = 27;
                                break;
                            }
                        case 1081438155:
                            if (attrName.equals(TaskRecord.ATTR_CALLING_PACKAGE)) {
                                c = 21;
                                break;
                            }
                        case 1457608782:
                            if (attrName.equals(TaskRecord.ATTR_NEVERRELINQUISH)) {
                                c = 15;
                                break;
                            }
                        case 1539554448:
                            if (attrName.equals(TaskRecord.ATTR_REALACTIVITY)) {
                                c = userSetupComplete;
                                break;
                            }
                        case 2023391309:
                            if (attrName.equals(TaskRecord.ATTR_ROOTHASRESET)) {
                                c = 6;
                                break;
                            }
                        default:
                            c = 65535;
                            break;
                    }
                    switch (c) {
                        case 0:
                            if (taskId == -1) {
                                taskId = Integer.parseInt(attrValue);
                                break;
                            }
                            break;
                        case 1:
                            realActivity = ComponentName.unflattenFromString(attrValue);
                            break;
                        case 2:
                            realActivitySuspended = Boolean.valueOf(attrValue).booleanValue();
                            break;
                        case 3:
                            origActivity = ComponentName.unflattenFromString(attrValue);
                            break;
                        case 4:
                            affinity = attrValue;
                            break;
                        case 5:
                            rootAffinity3 = attrValue;
                            hasRootAffinity = true;
                            break;
                        case 6:
                            rootHasReset = Boolean.parseBoolean(attrValue);
                            break;
                        case 7:
                            autoRemoveRecents = Boolean.parseBoolean(attrValue);
                            break;
                        case 8:
                            askedCompatMode = Boolean.parseBoolean(attrValue);
                            break;
                        case 9:
                            userId = Integer.parseInt(attrValue);
                            break;
                        case 10:
                            userSetupComplete2 = Boolean.parseBoolean(attrValue);
                            break;
                        case 11:
                            effectiveUid2 = Integer.parseInt(attrValue);
                            break;
                        case 12:
                            taskType = Integer.parseInt(attrValue);
                            break;
                        case 13:
                            lastDescription = attrValue;
                            break;
                        case 14:
                            lastTimeOnTop = Long.parseLong(attrValue);
                            break;
                        case 15:
                            neverRelinquishIdentity = Boolean.parseBoolean(attrValue);
                            break;
                        case 16:
                            taskAffiliation = Integer.parseInt(attrValue);
                            break;
                        case 17:
                            prevTaskId = Integer.parseInt(attrValue);
                            break;
                        case 18:
                            nextTaskId = Integer.parseInt(attrValue);
                            break;
                        case WindowManagerService.H.REPORT_WINDOWS_CHANGE /*19*/:
                            taskAffiliationColor = Integer.parseInt(attrValue);
                            break;
                        case 20:
                            callingUid = Integer.parseInt(attrValue);
                            break;
                        case BackupHandler.MSG_OP_COMPLETE /*21*/:
                            callingPackage = attrValue;
                            break;
                        case WindowManagerService.H.REPORT_HARD_KEYBOARD_STATUS_CHANGE /*22*/:
                            resizeMode2 = Integer.parseInt(attrValue);
                            break;
                        case WindowManagerService.H.BOOT_TIMEOUT /*23*/:
                            supportsPictureInPicture2 = Boolean.parseBoolean(attrValue);
                            break;
                        case 24:
                            lastNonFullscreenBounds = Rect.unflattenFromString(attrValue);
                            break;
                        case WindowManagerService.H.SHOW_STRICT_MODE_VIOLATION /*25*/:
                            minWidth = Integer.parseInt(attrValue);
                            break;
                        case WindowManagerService.H.DO_ANIMATION_CALLBACK /*26*/:
                            minHeight = Integer.parseInt(attrValue);
                            break;
                        case 27:
                            persistTaskVersion = Integer.parseInt(attrValue);
                            break;
                        default:
                            if (attrName.startsWith("task_description_")) {
                                ActivityManager.TaskDescription taskDescription3 = taskDescription2;
                                taskDescription3.restoreFromXml(attrName, attrValue);
                                taskDescription = taskDescription3;
                                break;
                            } else {
                                StringBuilder sb = new StringBuilder();
                                taskDescription = taskDescription2;
                                sb.append("TaskRecord: Unknown attribute=");
                                sb.append(attrName);
                                Slog.w(ActivityManagerService.TAG, sb.toString());
                                continue;
                            }
                    }
                    taskDescription = taskDescription2;
                    attrNdx = attrNdx2 - 1;
                    taskDescription2 = taskDescription;
                    userSetupComplete = true;
                } else {
                    ActivityManager.TaskDescription taskDescription4 = taskDescription2;
                    while (true) {
                        int next = in.next();
                        event = next;
                        if (next != 1 && (event != 3 || in.getDepth() >= outerDepth)) {
                            if (event == 2) {
                                String name = in.getName();
                                if (TaskRecord.TAG_AFFINITYINTENT.equals(name)) {
                                    affinityIntent = Intent.restoreFromXml(in);
                                } else if ("intent".equals(name)) {
                                    intent = Intent.restoreFromXml(in);
                                } else if (TaskRecord.TAG_ACTIVITY.equals(name)) {
                                    ActivityRecord activity = ActivityRecord.restoreFromXml(in, stackSupervisor);
                                    if (activity != null) {
                                        activities.add(activity);
                                    }
                                } else {
                                    handleUnknownTag(name, xmlPullParser);
                                }
                            }
                        }
                    }
                    if (!hasRootAffinity) {
                        rootAffinity2 = affinity;
                    } else if ("@".equals(rootAffinity3)) {
                        rootAffinity2 = null;
                    } else {
                        rootAffinity = rootAffinity3;
                        if (effectiveUid2 > 0) {
                            Intent checkIntent = intent != null ? intent : affinityIntent;
                            int effectiveUid3 = 0;
                            if (checkIntent != null) {
                                try {
                                    ApplicationInfo ai = AppGlobals.getPackageManager().getApplicationInfo(checkIntent.getComponent().getPackageName(), 8704, userId);
                                    if (ai != null) {
                                        effectiveUid3 = ai.uid;
                                    }
                                } catch (RemoteException e) {
                                }
                            }
                            Slog.w(ActivityManagerService.TAG, "Updating task #" + taskId + " for " + checkIntent + ": effectiveUid=" + effectiveUid3);
                            effectiveUid = effectiveUid3;
                        } else {
                            effectiveUid = effectiveUid2;
                        }
                        if (persistTaskVersion >= 1) {
                            if (taskType == 1 && resizeMode2 == 2) {
                                resizeMode2 = 1;
                            }
                        } else if (resizeMode2 == 3) {
                            resizeMode = 2;
                            supportsPictureInPicture = true;
                            int i = taskType;
                            Rect lastNonFullscreenBounds2 = lastNonFullscreenBounds;
                            int i2 = persistTaskVersion;
                            int i3 = taskId;
                            int i4 = event;
                            TaskRecord task = create(stackSupervisor.mService, taskId, intent, affinityIntent, affinity, rootAffinity, realActivity, origActivity, rootHasReset, autoRemoveRecents, askedCompatMode, userId, effectiveUid, lastDescription, activities, lastTimeOnTop, neverRelinquishIdentity, taskDescription4, taskAffiliation, prevTaskId, nextTaskId, taskAffiliationColor, callingUid, callingPackage, resizeMode, supportsPictureInPicture, realActivitySuspended, userSetupComplete2, minWidth, minHeight);
                            task.mLastNonFullscreenBounds = lastNonFullscreenBounds2;
                            task.setBounds(lastNonFullscreenBounds2);
                            for (activityNdx = activities.size() - 1; activityNdx >= 0; activityNdx--) {
                                activities.get(activityNdx).setTask(task);
                            }
                            if (ActivityManagerDebugConfig.DEBUG_RECENTS != 0) {
                                Slog.d(ActivityManagerService.TAG, "Restored task=" + task);
                            }
                            return task;
                        }
                        resizeMode = resizeMode2;
                        supportsPictureInPicture = supportsPictureInPicture2;
                        int i5 = taskType;
                        Rect lastNonFullscreenBounds22 = lastNonFullscreenBounds;
                        int i22 = persistTaskVersion;
                        int i32 = taskId;
                        int i42 = event;
                        TaskRecord task2 = create(stackSupervisor.mService, taskId, intent, affinityIntent, affinity, rootAffinity, realActivity, origActivity, rootHasReset, autoRemoveRecents, askedCompatMode, userId, effectiveUid, lastDescription, activities, lastTimeOnTop, neverRelinquishIdentity, taskDescription4, taskAffiliation, prevTaskId, nextTaskId, taskAffiliationColor, callingUid, callingPackage, resizeMode, supportsPictureInPicture, realActivitySuspended, userSetupComplete2, minWidth, minHeight);
                        task2.mLastNonFullscreenBounds = lastNonFullscreenBounds22;
                        task2.setBounds(lastNonFullscreenBounds22);
                        while (activityNdx >= 0) {
                        }
                        if (ActivityManagerDebugConfig.DEBUG_RECENTS != 0) {
                        }
                        return task2;
                    }
                    rootAffinity = rootAffinity2;
                    if (effectiveUid2 > 0) {
                    }
                    if (persistTaskVersion >= 1) {
                    }
                    resizeMode = resizeMode2;
                    supportsPictureInPicture = supportsPictureInPicture2;
                    int i52 = taskType;
                    Rect lastNonFullscreenBounds222 = lastNonFullscreenBounds;
                    int i222 = persistTaskVersion;
                    int i322 = taskId;
                    int i422 = event;
                    TaskRecord task22 = create(stackSupervisor.mService, taskId, intent, affinityIntent, affinity, rootAffinity, realActivity, origActivity, rootHasReset, autoRemoveRecents, askedCompatMode, userId, effectiveUid, lastDescription, activities, lastTimeOnTop, neverRelinquishIdentity, taskDescription4, taskAffiliation, prevTaskId, nextTaskId, taskAffiliationColor, callingUid, callingPackage, resizeMode, supportsPictureInPicture, realActivitySuspended, userSetupComplete2, minWidth, minHeight);
                    task22.mLastNonFullscreenBounds = lastNonFullscreenBounds222;
                    task22.setBounds(lastNonFullscreenBounds222);
                    while (activityNdx >= 0) {
                    }
                    if (ActivityManagerDebugConfig.DEBUG_RECENTS != 0) {
                    }
                    return task22;
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void handleUnknownTag(String name, XmlPullParser in) throws IOException, XmlPullParserException {
            Slog.e(ActivityManagerService.TAG, "restoreTask: Unexpected name=" + name);
            XmlUtils.skipCurrentTag(in);
        }
    }

    public TaskRecord(ActivityManagerService service, int _taskId, ActivityInfo info, Intent _intent, IVoiceInteractionSession _voiceSession, IVoiceInteractor _voiceInteractor) {
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
        this.mService.mTaskChangeNotificationController.notifyTaskCreated(_taskId, this.realActivity);
        this.mHwTaskRecordEx = HwServiceExFactory.getHwTaskRecordEx();
    }

    public TaskRecord(ActivityManagerService service, int _taskId, ActivityInfo info, Intent _intent, ActivityManager.TaskDescription _taskDescription) {
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
        this.maxRecents = Math.min(Math.max(info.maxRecents, 1), ActivityManager.getMaxAppRecentsLimitStatic());
        this.lastTaskDescription = _taskDescription;
        touchActiveTime();
        this.mService.mTaskChangeNotificationController.notifyTaskCreated(_taskId, this.realActivity);
    }

    public TaskRecord(ActivityManagerService service, int _taskId, Intent _intent, Intent _affinityIntent, String _affinity, String _rootAffinity, ComponentName _realActivity, ComponentName _origActivity, boolean _rootWasReset, boolean _autoRemoveRecents, boolean _askedCompatMode, int _userId, int _effectiveUid, String _lastDescription, ArrayList<ActivityRecord> activities, long lastTimeMoved, boolean neverRelinquishIdentity, ActivityManager.TaskDescription _lastTaskDescription, int taskAffiliation, int prevTaskId, int nextTaskId, int taskAffiliationColor, int callingUid, String callingPackage, int resizeMode, boolean supportsPictureInPicture, boolean _realActivitySuspended, boolean userSetupComplete, int minWidth, int minHeight) {
        int i = _taskId;
        this.mService = service;
        this.taskId = i;
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
        this.mService.mTaskChangeNotificationController.notifyTaskCreated(i, this.realActivity);
    }

    /* access modifiers changed from: package-private */
    public TaskWindowContainerController getWindowContainerController() {
        return this.mWindowContainerController;
    }

    /* access modifiers changed from: package-private */
    public void createWindowContainer(boolean onTop, boolean showForAllUsers) {
        if (this.mWindowContainerController == null) {
            TaskWindowContainerController taskWindowContainerController = new TaskWindowContainerController(this.taskId, this, getStack().getWindowContainerController(), this.userId, updateOverrideConfigurationFromLaunchBounds(), this.mResizeMode, this.mSupportsPictureInPicture, onTop, showForAllUsers, this.lastTaskDescription);
            setWindowContainerController(taskWindowContainerController);
            return;
        }
        throw new IllegalArgumentException("Window container=" + this.mWindowContainerController + " already created for task=" + this);
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public void setWindowContainerController(TaskWindowContainerController controller) {
        if (this.mWindowContainerController == null) {
            this.mWindowContainerController = controller;
            return;
        }
        throw new IllegalArgumentException("Window container=" + this.mWindowContainerController + " already created for task=" + this);
    }

    /* access modifiers changed from: package-private */
    public void removeWindowContainer() {
        this.mService.getLockTaskController().clearLockedTask(this);
        this.mWindowContainerController.removeContainer();
        if (!getWindowConfiguration().persistTaskBounds()) {
            updateOverrideConfiguration(null);
        }
        this.mService.mTaskChangeNotificationController.notifyTaskRemoved(this.taskId);
        this.mWindowContainerController = null;
    }

    public void onSnapshotChanged(ActivityManager.TaskSnapshot snapshot) {
        this.mService.mTaskChangeNotificationController.notifyTaskSnapshotChanged(this.taskId, snapshot);
    }

    /* access modifiers changed from: package-private */
    public void setResizeMode(int resizeMode) {
        if (this.mResizeMode != resizeMode) {
            this.mResizeMode = resizeMode;
            if (this.mWindowContainerController != null) {
                this.mWindowContainerController.setResizeable(resizeMode);
            }
            this.mService.mStackSupervisor.ensureActivitiesVisibleLocked(null, 0, false);
            this.mService.mStackSupervisor.resumeFocusedStackTopActivityLocked();
        }
    }

    /* access modifiers changed from: package-private */
    public void setTaskDockedResizing(boolean resizing) {
        if (this.mWindowContainerController != null) {
            this.mWindowContainerController.setTaskDockedResizing(resizing);
        }
    }

    public void requestResize(Rect bounds, int resizeMode) {
        this.mService.resizeTask(this.taskId, bounds, resizeMode);
    }

    /* access modifiers changed from: package-private */
    public boolean resize(Rect bounds, int resizeMode, boolean preserveWindow, boolean deferResume) {
        this.mService.mWindowManager.deferSurfaceLayout();
        if (!isResizeable()) {
            Slog.w(ActivityManagerService.TAG, "resizeTask: task " + this + " not resizeable.");
            this.mService.mWindowManager.continueSurfaceLayout();
            return true;
        }
        boolean forced = (resizeMode & 2) != 0;
        try {
            if (equivalentOverrideBounds(bounds) && !forced) {
                return true;
            }
            if (this.mWindowContainerController == null) {
                updateOverrideConfiguration(bounds);
                if (!inFreeformWindowingMode()) {
                    this.mService.mStackSupervisor.restoreRecentTaskLocked(this, null, false);
                }
                this.mService.mWindowManager.continueSurfaceLayout();
                return true;
            } else if (canResizeToBounds(bounds)) {
                Trace.traceBegin(64, "am.resizeTask_" + this.taskId);
                boolean kept = true;
                if (updateOverrideConfiguration(bounds)) {
                    ActivityRecord r = topRunningActivityLocked();
                    if (r != null && !deferResume) {
                        kept = r.ensureActivityConfiguration(0, preserveWindow);
                        this.mService.mStackSupervisor.ensureActivitiesVisibleLocked(r, 0, false);
                        if (!kept) {
                            this.mService.mStackSupervisor.resumeFocusedStackTopActivityLocked();
                        }
                    }
                }
                this.mWindowContainerController.resize(kept, forced);
                Trace.traceEnd(64);
                this.mService.mWindowManager.continueSurfaceLayout();
                return kept;
            } else {
                throw new IllegalArgumentException("resizeTask: Can not resize task=" + this + " to bounds=" + bounds + " resizeMode=" + this.mResizeMode);
            }
        } finally {
            this.mService.mWindowManager.continueSurfaceLayout();
        }
    }

    /* access modifiers changed from: package-private */
    public void resizeWindowContainer() {
        if (this.mWindowContainerController != null) {
            this.mWindowContainerController.resize(false, false);
        }
    }

    /* access modifiers changed from: package-private */
    public void getWindowContainerBounds(Rect bounds) {
        if (this.mWindowContainerController != null) {
            this.mWindowContainerController.getBounds(bounds);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean reparent(ActivityStack preferredStack, boolean toTop, int moveStackMode, boolean animate, boolean deferResume, String reason) {
        return reparent(preferredStack, toTop ? HwBootFail.STAGE_BOOT_SUCCESS : 0, moveStackMode, animate, deferResume, true, reason);
    }

    /* access modifiers changed from: package-private */
    public boolean reparent(ActivityStack preferredStack, boolean toTop, int moveStackMode, boolean animate, boolean deferResume, boolean schedulePictureInPictureModeChange, String reason) {
        return reparent(preferredStack, toTop ? HwBootFail.STAGE_BOOT_SUCCESS : 0, moveStackMode, animate, deferResume, schedulePictureInPictureModeChange, reason);
    }

    /* access modifiers changed from: package-private */
    public boolean reparent(ActivityStack preferredStack, int position, int moveStackMode, boolean animate, boolean deferResume, String reason) {
        return reparent(preferredStack, position, moveStackMode, animate, deferResume, true, reason);
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:103:0x012e  */
    /* JADX WARNING: Removed duplicated region for block: B:105:0x0138 A[SYNTHETIC, Splitter:B:105:0x0138] */
    /* JADX WARNING: Removed duplicated region for block: B:113:0x014f  */
    /* JADX WARNING: Removed duplicated region for block: B:118:0x0159  */
    /* JADX WARNING: Removed duplicated region for block: B:119:0x015b  */
    /* JADX WARNING: Removed duplicated region for block: B:123:0x0162 A[Catch:{ all -> 0x0206 }] */
    /* JADX WARNING: Removed duplicated region for block: B:129:0x0175 A[Catch:{ all -> 0x0206 }] */
    /* JADX WARNING: Removed duplicated region for block: B:130:0x0176 A[Catch:{ all -> 0x0206 }] */
    /* JADX WARNING: Removed duplicated region for block: B:142:0x01a0 A[Catch:{ all -> 0x0206 }] */
    /* JADX WARNING: Removed duplicated region for block: B:143:0x01a2 A[Catch:{ all -> 0x0206 }] */
    /* JADX WARNING: Removed duplicated region for block: B:163:0x01dd  */
    /* JADX WARNING: Removed duplicated region for block: B:169:0x01e9  */
    /* JADX WARNING: Removed duplicated region for block: B:173:0x01f7  */
    /* JADX WARNING: Removed duplicated region for block: B:176:0x0203  */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x008f A[SYNTHETIC, Splitter:B:41:0x008f] */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x00a7 A[SYNTHETIC, Splitter:B:50:0x00a7] */
    /* JADX WARNING: Removed duplicated region for block: B:66:0x00d3  */
    /* JADX WARNING: Removed duplicated region for block: B:67:0x00d5  */
    /* JADX WARNING: Removed duplicated region for block: B:71:0x00db A[Catch:{ all -> 0x020f }] */
    /* JADX WARNING: Removed duplicated region for block: B:77:0x00e5 A[Catch:{ all -> 0x020f }] */
    /* JADX WARNING: Removed duplicated region for block: B:81:0x00eb A[Catch:{ all -> 0x020f }] */
    /* JADX WARNING: Removed duplicated region for block: B:82:0x00ee A[Catch:{ all -> 0x020f }] */
    /* JADX WARNING: Removed duplicated region for block: B:85:0x00f9 A[SYNTHETIC, Splitter:B:85:0x00f9] */
    /* JADX WARNING: Removed duplicated region for block: B:95:0x010f A[SYNTHETIC, Splitter:B:95:0x010f] */
    /* JADX WARNING: Removed duplicated region for block: B:99:0x011c  */
    public boolean reparent(ActivityStack preferredStack, int position, int moveStackMode, boolean animate, boolean deferResume, boolean schedulePictureInPictureModeChange, String reason) {
        boolean z;
        ActivityRecord topActivity;
        boolean z2;
        int toStackWindowingMode;
        boolean z3;
        int position2;
        boolean z4;
        boolean z5;
        boolean z6;
        ActivityStack toStack;
        ActivityRecord topActivity2;
        int toStackWindowingMode2;
        boolean z7;
        ActivityStack activityStack;
        boolean z8;
        ActivityStack activityStack2 = preferredStack;
        int i = position;
        int i2 = moveStackMode;
        boolean z9 = animate;
        boolean z10 = deferResume;
        String str = reason;
        ActivityStackSupervisor supervisor = this.mService.mStackSupervisor;
        WindowManagerService windowManager = this.mService.mWindowManager;
        ActivityStack sourceStack = getStack();
        ActivityStack toStack2 = supervisor.getReparentTargetStack(this, activityStack2, i == Integer.MAX_VALUE);
        if (toStack2 == sourceStack || !canBeLaunchedOnDisplay(toStack2.mDisplayId)) {
            return false;
        }
        int toStackWindowingMode3 = toStack2.getWindowingMode();
        ActivityRecord topActivity3 = getTopActivity();
        boolean mightReplaceWindow = topActivity3 != null && replaceWindowsOnTaskMove(getWindowingMode(), toStackWindowingMode3);
        if (mightReplaceWindow) {
            windowManager.setWillReplaceWindow(topActivity3.appToken, z9);
        }
        windowManager.deferSurfaceLayout();
        boolean kept = true;
        try {
            ActivityRecord r = topRunningActivityLocked();
            if (r != null) {
                try {
                    if (supervisor.isFocusedStack(sourceStack) && topRunningActivityLocked() == r) {
                        z = true;
                        boolean wasFocused = z;
                        topActivity = topActivity3;
                        boolean wasResumed = r == null && sourceStack.getResumedActivity() == r;
                        if (r != null) {
                            try {
                                if (sourceStack.mPausingActivity == r) {
                                    z2 = true;
                                    toStackWindowingMode = toStackWindowingMode3;
                                    boolean wasPaused = z2;
                                    if (r != null) {
                                        try {
                                            if (sourceStack.isTopStackOnDisplay() && sourceStack.topRunningActivityLocked() == r) {
                                                z3 = true;
                                                boolean wasFront = z3;
                                                position2 = toStack2.getAdjustedPositionForTask(this, i, null);
                                                this.mWindowContainerController.reparent(toStack2.getWindowContainerController(), position2, i2 != 0);
                                                if (i2 == 0) {
                                                    z4 = true;
                                                    if (i2 == 1) {
                                                        if (!wasFocused) {
                                                            if (wasFront) {
                                                            }
                                                        }
                                                    }
                                                    z5 = false;
                                                    boolean moveStackToFront = z5;
                                                    sourceStack.removeTask(this, str, moveStackToFront ? 2 : z4);
                                                    toStack2.addTask(this, position2, false, str);
                                                    if (schedulePictureInPictureModeChange) {
                                                        try {
                                                            supervisor.scheduleUpdatePictureInPictureModeIfNeeded(this, sourceStack);
                                                        } catch (Throwable th) {
                                                            th = th;
                                                            int i3 = position2;
                                                            ActivityStack activityStack3 = toStack2;
                                                            ActivityStack activityStack4 = sourceStack;
                                                        }
                                                    }
                                                    if (this.voiceSession != null) {
                                                        try {
                                                            this.voiceSession.taskStarted(this.intent, this.taskId);
                                                        } catch (RemoteException e) {
                                                        }
                                                    }
                                                    if (r != null) {
                                                        toStack = toStack2;
                                                        ActivityRecord activityRecord = r;
                                                        int i4 = position2;
                                                        z6 = true;
                                                        ActivityStack activityStack5 = sourceStack;
                                                        try {
                                                            toStack2.moveToFrontAndResumeStateIfNeeded(r, moveStackToFront, wasResumed, wasPaused, str);
                                                        } catch (Throwable th2) {
                                                            th = th2;
                                                        }
                                                    } else {
                                                        toStack = toStack2;
                                                        ActivityRecord activityRecord2 = r;
                                                        ActivityStack activityStack6 = sourceStack;
                                                        z6 = true;
                                                    }
                                                    if (!z9) {
                                                        try {
                                                            topActivity2 = topActivity;
                                                            try {
                                                                this.mService.mStackSupervisor.mNoAnimActivities.add(topActivity2);
                                                            } catch (Throwable th3) {
                                                                th = th3;
                                                            }
                                                        } catch (Throwable th4) {
                                                            th = th4;
                                                            ActivityRecord activityRecord3 = topActivity;
                                                            int i5 = toStackWindowingMode;
                                                            ActivityStack activityStack7 = preferredStack;
                                                            windowManager.continueSurfaceLayout();
                                                            throw th;
                                                        }
                                                    } else {
                                                        topActivity2 = topActivity;
                                                    }
                                                    toStack.prepareFreezingTaskBounds();
                                                    toStackWindowingMode2 = toStackWindowingMode;
                                                    boolean toStackSplitScreenPrimary = toStackWindowingMode2 == 3 ? z6 : false;
                                                    Rect configBounds = getOverrideBounds();
                                                    if ((toStackWindowingMode2 != z6 || toStackWindowingMode2 == 4) && !Objects.equals(configBounds, toStack.getOverrideBounds())) {
                                                        Rect overrideBounds = toStack.getOverrideBounds();
                                                        if (mightReplaceWindow) {
                                                            z6 = false;
                                                        }
                                                        kept = resize(overrideBounds, 0, z6, z10);
                                                    } else if (toStackWindowingMode2 == 5) {
                                                        Rect bounds = getLaunchBounds();
                                                        if (bounds != null) {
                                                            if (!bounds.isEmpty()) {
                                                                kept = resize(bounds, 2, mightReplaceWindow, z10);
                                                            }
                                                        }
                                                        this.mService.mStackSupervisor.getLaunchParamsController().layoutTask(this, null);
                                                        bounds = configBounds;
                                                        kept = resize(bounds, 2, mightReplaceWindow, z10);
                                                    } else if (toStackSplitScreenPrimary || toStackWindowingMode2 == 2) {
                                                        if (toStackSplitScreenPrimary) {
                                                            z8 = true;
                                                            if (i2 == 1 && !str.contains("swapDockedAndFullscreenStack")) {
                                                                this.mService.mStackSupervisor.moveRecentsStackToFront(str);
                                                            }
                                                        } else {
                                                            z8 = true;
                                                        }
                                                        kept = resize(toStack.getOverrideBounds(), 0, !mightReplaceWindow ? z8 : false, z10);
                                                    }
                                                    windowManager.continueSurfaceLayout();
                                                    if (mightReplaceWindow) {
                                                        windowManager.scheduleClearWillReplaceWindows(topActivity2.appToken, !kept);
                                                    }
                                                    if (!z10) {
                                                        z7 = false;
                                                        supervisor.ensureActivitiesVisibleLocked(null, 0, !mightReplaceWindow);
                                                        supervisor.resumeFocusedStackTopActivityLocked();
                                                    } else {
                                                        z7 = false;
                                                    }
                                                    activityStack = preferredStack;
                                                    supervisor.handleNonResizableTaskIfNeeded(this, preferredStack.getWindowingMode(), z7 ? 1 : 0, toStack);
                                                    if (activityStack == toStack) {
                                                        z7 = true;
                                                    }
                                                    return z7;
                                                }
                                                z4 = true;
                                                z5 = z4;
                                                boolean moveStackToFront2 = z5;
                                                sourceStack.removeTask(this, str, moveStackToFront2 ? 2 : z4);
                                                toStack2.addTask(this, position2, false, str);
                                                if (schedulePictureInPictureModeChange) {
                                                }
                                                if (this.voiceSession != null) {
                                                }
                                                if (r != null) {
                                                }
                                                if (!z9) {
                                                }
                                                toStack.prepareFreezingTaskBounds();
                                                toStackWindowingMode2 = toStackWindowingMode;
                                                if (toStackWindowingMode2 == 3) {
                                                }
                                                try {
                                                    Rect configBounds2 = getOverrideBounds();
                                                    if (toStackWindowingMode2 != z6) {
                                                    }
                                                    Rect overrideBounds2 = toStack.getOverrideBounds();
                                                    if (mightReplaceWindow) {
                                                    }
                                                    kept = resize(overrideBounds2, 0, z6, z10);
                                                    windowManager.continueSurfaceLayout();
                                                    if (mightReplaceWindow) {
                                                    }
                                                    if (!z10) {
                                                    }
                                                    activityStack = preferredStack;
                                                    supervisor.handleNonResizableTaskIfNeeded(this, preferredStack.getWindowingMode(), z7 ? 1 : 0, toStack);
                                                    if (activityStack == toStack) {
                                                    }
                                                    return z7;
                                                } catch (Throwable th5) {
                                                    th = th5;
                                                    ActivityStack activityStack8 = preferredStack;
                                                    windowManager.continueSurfaceLayout();
                                                    throw th;
                                                }
                                            }
                                        } catch (Throwable th6) {
                                            th = th6;
                                            ActivityStack activityStack9 = activityStack2;
                                            int i6 = i;
                                            ActivityStack activityStack10 = toStack2;
                                            ActivityStack activityStack11 = sourceStack;
                                            ActivityRecord activityRecord4 = topActivity;
                                            int i7 = toStackWindowingMode;
                                            windowManager.continueSurfaceLayout();
                                            throw th;
                                        }
                                    }
                                    z3 = false;
                                    boolean wasFront2 = z3;
                                    position2 = toStack2.getAdjustedPositionForTask(this, i, null);
                                    try {
                                        this.mWindowContainerController.reparent(toStack2.getWindowContainerController(), position2, i2 != 0);
                                        if (i2 == 0) {
                                        }
                                        z5 = z4;
                                        boolean moveStackToFront22 = z5;
                                        sourceStack.removeTask(this, str, moveStackToFront22 ? 2 : z4);
                                        toStack2.addTask(this, position2, false, str);
                                        if (schedulePictureInPictureModeChange) {
                                        }
                                        if (this.voiceSession != null) {
                                        }
                                        if (r != null) {
                                        }
                                        if (!z9) {
                                        }
                                    } catch (Throwable th7) {
                                        th = th7;
                                        int i8 = position2;
                                        ActivityStack activityStack12 = toStack2;
                                        ActivityStack activityStack13 = sourceStack;
                                        ActivityRecord activityRecord5 = topActivity;
                                        int i9 = toStackWindowingMode;
                                        ActivityStack activityStack14 = preferredStack;
                                        windowManager.continueSurfaceLayout();
                                        throw th;
                                    }
                                    try {
                                        toStack.prepareFreezingTaskBounds();
                                        toStackWindowingMode2 = toStackWindowingMode;
                                        if (toStackWindowingMode2 == 3) {
                                        }
                                        Rect configBounds22 = getOverrideBounds();
                                        if (toStackWindowingMode2 != z6) {
                                        }
                                        Rect overrideBounds22 = toStack.getOverrideBounds();
                                        if (mightReplaceWindow) {
                                        }
                                        kept = resize(overrideBounds22, 0, z6, z10);
                                        windowManager.continueSurfaceLayout();
                                        if (mightReplaceWindow) {
                                        }
                                        if (!z10) {
                                        }
                                        activityStack = preferredStack;
                                        supervisor.handleNonResizableTaskIfNeeded(this, preferredStack.getWindowingMode(), z7 ? 1 : 0, toStack);
                                        if (activityStack == toStack) {
                                        }
                                        return z7;
                                    } catch (Throwable th8) {
                                        th = th8;
                                        int i10 = toStackWindowingMode;
                                        ActivityStack activityStack15 = preferredStack;
                                        windowManager.continueSurfaceLayout();
                                        throw th;
                                    }
                                }
                            } catch (Throwable th9) {
                                th = th9;
                                ActivityStack activityStack16 = activityStack2;
                                int i11 = i;
                                ActivityStack activityStack17 = toStack2;
                                int i12 = toStackWindowingMode3;
                                ActivityStack activityStack18 = sourceStack;
                                ActivityRecord activityRecord6 = topActivity;
                                windowManager.continueSurfaceLayout();
                                throw th;
                            }
                        }
                        z2 = false;
                        toStackWindowingMode = toStackWindowingMode3;
                        boolean wasPaused2 = z2;
                        if (r != null) {
                        }
                        z3 = false;
                        boolean wasFront22 = z3;
                        position2 = toStack2.getAdjustedPositionForTask(this, i, null);
                        try {
                            this.mWindowContainerController.reparent(toStack2.getWindowContainerController(), position2, i2 != 0);
                            if (i2 == 0) {
                            }
                            z5 = z4;
                            boolean moveStackToFront222 = z5;
                            sourceStack.removeTask(this, str, moveStackToFront222 ? 2 : z4);
                            toStack2.addTask(this, position2, false, str);
                            if (schedulePictureInPictureModeChange) {
                            }
                            if (this.voiceSession != null) {
                            }
                            if (r != null) {
                            }
                            if (!z9) {
                            }
                            toStack.prepareFreezingTaskBounds();
                            toStackWindowingMode2 = toStackWindowingMode;
                            if (toStackWindowingMode2 == 3) {
                            }
                            Rect configBounds222 = getOverrideBounds();
                            if (toStackWindowingMode2 != z6) {
                            }
                            Rect overrideBounds222 = toStack.getOverrideBounds();
                            if (mightReplaceWindow) {
                            }
                            kept = resize(overrideBounds222, 0, z6, z10);
                            windowManager.continueSurfaceLayout();
                            if (mightReplaceWindow) {
                            }
                            if (!z10) {
                            }
                            activityStack = preferredStack;
                            supervisor.handleNonResizableTaskIfNeeded(this, preferredStack.getWindowingMode(), z7 ? 1 : 0, toStack);
                            if (activityStack == toStack) {
                            }
                            return z7;
                        } catch (Throwable th10) {
                            th = th10;
                            ActivityStack activityStack19 = activityStack2;
                            int i13 = position2;
                            ActivityStack activityStack20 = toStack2;
                            ActivityStack activityStack21 = sourceStack;
                            ActivityRecord activityRecord7 = topActivity;
                            int i14 = toStackWindowingMode;
                            windowManager.continueSurfaceLayout();
                            throw th;
                        }
                    }
                } catch (Throwable th11) {
                    th = th11;
                    ActivityStack activityStack22 = activityStack2;
                    int i15 = i;
                    ActivityStack activityStack23 = toStack2;
                    ActivityRecord activityRecord8 = topActivity3;
                    int i16 = toStackWindowingMode3;
                    ActivityStack activityStack24 = sourceStack;
                    windowManager.continueSurfaceLayout();
                    throw th;
                }
            }
            z = false;
            boolean wasFocused2 = z;
            topActivity = topActivity3;
            boolean wasResumed2 = r == null && sourceStack.getResumedActivity() == r;
            if (r != null) {
            }
            z2 = false;
            toStackWindowingMode = toStackWindowingMode3;
            boolean wasPaused22 = z2;
            if (r != null) {
            }
            z3 = false;
            boolean wasFront222 = z3;
            try {
                position2 = toStack2.getAdjustedPositionForTask(this, i, null);
                this.mWindowContainerController.reparent(toStack2.getWindowContainerController(), position2, i2 != 0);
                if (i2 == 0) {
                }
                z5 = z4;
                boolean moveStackToFront2222 = z5;
                sourceStack.removeTask(this, str, moveStackToFront2222 ? 2 : z4);
                toStack2.addTask(this, position2, false, str);
                if (schedulePictureInPictureModeChange) {
                }
                if (this.voiceSession != null) {
                }
                if (r != null) {
                }
                if (!z9) {
                }
                toStack.prepareFreezingTaskBounds();
                toStackWindowingMode2 = toStackWindowingMode;
                if (toStackWindowingMode2 == 3) {
                }
                Rect configBounds2222 = getOverrideBounds();
                if (toStackWindowingMode2 != z6) {
                }
                Rect overrideBounds2222 = toStack.getOverrideBounds();
                if (mightReplaceWindow) {
                }
                kept = resize(overrideBounds2222, 0, z6, z10);
                windowManager.continueSurfaceLayout();
                if (mightReplaceWindow) {
                }
                if (!z10) {
                }
                activityStack = preferredStack;
                supervisor.handleNonResizableTaskIfNeeded(this, preferredStack.getWindowingMode(), z7 ? 1 : 0, toStack);
                if (activityStack == toStack) {
                }
                return z7;
            } catch (Throwable th12) {
                th = th12;
                ActivityStack activityStack25 = activityStack2;
                ActivityStack activityStack26 = toStack2;
                ActivityStack activityStack27 = sourceStack;
                ActivityRecord activityRecord9 = topActivity;
                int i17 = toStackWindowingMode;
                int i18 = i;
                windowManager.continueSurfaceLayout();
                throw th;
            }
        } catch (Throwable th13) {
            th = th13;
            ActivityStack activityStack28 = activityStack2;
            ActivityStack activityStack29 = toStack2;
            ActivityRecord activityRecord10 = topActivity3;
            int i19 = toStackWindowingMode3;
            ActivityStack activityStack30 = sourceStack;
            int i20 = i;
            windowManager.continueSurfaceLayout();
            throw th;
        }
        ActivityStack activityStack82 = preferredStack;
        windowManager.continueSurfaceLayout();
        throw th;
        ActivityStack activityStack822 = preferredStack;
        windowManager.continueSurfaceLayout();
        throw th;
    }

    private static boolean replaceWindowsOnTaskMove(int sourceWindowingMode, int targetWindowingMode) {
        return sourceWindowingMode == 5 || targetWindowingMode == 5;
    }

    /* access modifiers changed from: package-private */
    public void cancelWindowTransition() {
        if (this.mWindowContainerController != null) {
            this.mWindowContainerController.cancelWindowTransition();
        }
    }

    /* access modifiers changed from: package-private */
    public ActivityManager.TaskSnapshot getSnapshot(boolean reducedResolution) {
        return this.mService.mWindowManager.getTaskSnapshot(this.taskId, this.userId, reducedResolution);
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
            if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                Slog.v(ActivityManagerService.TAG, "Setting Intent of " + this + " to " + _intent);
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
                    Slog.v(ActivityManagerService.TAG, "Setting Intent of " + this + " to target " + targetIntent);
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
        int intentFlags = this.intent == null ? 0 : this.intent.getFlags();
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
        Intent intent2 = new Intent(r.intent);
        intent2.setComponent(r.realActivity);
        return intent2.filterEquals(this.intent);
    }

    /* access modifiers changed from: package-private */
    public boolean returnsToHomeStack() {
        return this.intent != null && (this.intent.getFlags() & 268451840) == 268451840;
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
        return this.mStack;
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
        if (this.mStack != null) {
            return this.mStack.mStackId;
        }
        return -1;
    }

    /* access modifiers changed from: protected */
    public int getChildCount() {
        return this.mActivities.size();
    }

    /* access modifiers changed from: protected */
    public ActivityRecord getChildAt(int index) {
        return this.mActivities.get(index);
    }

    /* access modifiers changed from: protected */
    public ConfigurationContainer getParent() {
        return this.mStack;
    }

    /* access modifiers changed from: protected */
    public void onParentChanged() {
        super.onParentChanged();
        this.mService.mStackSupervisor.updateUIDsPresentOnDisplay();
    }

    private void closeRecentsChain() {
        if (this.mPrevAffiliate != null) {
            this.mPrevAffiliate.setNextAffiliate(this.mNextAffiliate);
        }
        if (this.mNextAffiliate != null) {
            this.mNextAffiliate.setPrevAffiliate(this.mPrevAffiliate);
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
                Slog.e(ActivityManagerService.TAG, "setTaskToAffiliateWith: nextRecents=" + nextRecents + " affilTaskId=" + nextRecents.mAffiliatedTaskId + " should be " + this.mAffiliatedTaskId);
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
        return this.intent != null ? this.intent : this.affinityIntent;
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
        if (this.mStack != null) {
            for (int activityNdx = this.mActivities.size() - 1; activityNdx >= 0; activityNdx--) {
                ActivityRecord r = this.mActivities.get(activityNdx);
                if (!r.finishing && r.okToShowLocked()) {
                    return r;
                }
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
        if (this.mStack != null) {
            for (int activityNdx = this.mActivities.size() - 1; activityNdx >= 0; activityNdx--) {
                ActivityRecord r = this.mActivities.get(activityNdx);
                if (r.mStartingWindowState == 1 && !r.finishing && r.okToShowLocked()) {
                    return r;
                }
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
                if (!(r.app == null || r.app.thread == null)) {
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
        int numActivities = this.mActivities.size();
        boolean foundFront = false;
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
        if (ActivityManagerDebugConfig.DEBUG_ADD_REMOVE) {
            Slog.i(ActivityManagerService.TAG, "Removing and adding activity " + newTop + " to stack at top callers=" + Debug.getCallers(4));
        }
        this.mActivities.remove(newTop);
        this.mActivities.add(newTop);
        this.mWindowContainerController.positionChildAtTop(newTop.mWindowContainerController);
        updateEffectiveIntent();
        setFrontOfTask();
    }

    /* access modifiers changed from: package-private */
    public void addActivityAtBottom(ActivityRecord r) {
        addActivityAtIndex(0, r);
    }

    /* access modifiers changed from: package-private */
    public void addActivityToTop(ActivityRecord r) {
        addActivityAtIndex(this.mActivities.size(), r);
    }

    public int getActivityType() {
        int applicationType = super.getActivityType();
        if (applicationType != 0 || this.mActivities.isEmpty()) {
            return applicationType;
        }
        return this.mActivities.get(0).getActivityType();
    }

    /* access modifiers changed from: package-private */
    public void addActivityAtIndex(int index, ActivityRecord r) {
        TaskRecord task = r.getTask();
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
                this.maxRecents = Math.min(Math.max(r.info.maxRecents, 1), ActivityManager.getMaxAppRecentsLimitStatic());
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
            updateOverrideConfigurationFromLaunchBounds();
            AppWindowContainerController appController = r.getWindowContainerController();
            if (appController != null) {
                this.mWindowContainerController.positionChildAt(appController, index2);
            }
            this.mService.mStackSupervisor.updateUIDsPresentOnDisplay();
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
        if (r.getTask() == this) {
            r.setTask(null, reparenting);
            if (this.mActivities.remove(r) && r.fullscreen) {
                this.numFullscreen--;
            }
            if (r.isPersistable()) {
                this.mService.notifyTaskPersisterLocked(this, false);
            }
            if (inPinnedWindowingMode()) {
                this.mService.mTaskChangeNotificationController.notifyTaskStackChanged();
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
        boolean z = true;
        for (int i = this.mActivities.size() - 1; i >= 0; i--) {
            ActivityRecord r = this.mActivities.get(i);
            if (!excludeFinishing || !r.finishing) {
                if (!r.mTaskOverlay) {
                    return false;
                }
                count++;
            }
        }
        if (count <= 0) {
            z = false;
        }
        return z;
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
                    if (this.mStack == null) {
                        r.takeFromHistory();
                        this.mActivities.remove(activityNdx);
                        activityNdx--;
                        numActivities--;
                    } else if (this.mStack.finishActivityLocked(r, 0, null, reason, false, pauseImmediately)) {
                        activityNdx--;
                        numActivities--;
                    }
                }
            } catch (IndexOutOfBoundsException e) {
                Slog.e(ActivityManagerService.TAG, "performClearTaskAtIndexLocked: IndexOutOfBoundsException!");
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
        int activityNdx;
        int numActivities = this.mActivities.size();
        int activityNdx2 = numActivities - 1;
        while (activityNdx >= 0) {
            ActivityRecord r = this.mActivities.get(activityNdx);
            if (!r.finishing && r.realActivity.equals(newR.realActivity)) {
                ActivityRecord ret = r;
                while (true) {
                    activityNdx++;
                    if (activityNdx >= numActivities) {
                        break;
                    }
                    ActivityRecord r2 = this.mActivities.get(activityNdx);
                    if (!r2.finishing) {
                        ActivityOptions opts = r2.takeOptionsLocked();
                        if (opts != null) {
                            ret.updateOptionsLocked(opts);
                        }
                        if (this.mStack != null && this.mStack.finishActivityLocked(r2, 0, null, "clear-task-stack", false)) {
                            activityNdx--;
                            numActivities--;
                        }
                    }
                }
                if (ret.launchMode != 0 || (536870912 & launchFlags) != 0 || ActivityStarter.isDocumentLaunchesIntoExisting(launchFlags) || ret.finishing) {
                    return ret;
                }
                if (this.mStack != null) {
                    this.mStack.finishActivityLocked(ret, 0, null, "clear-task-top", false);
                }
                return null;
            }
            activityNdx2 = activityNdx - 1;
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public void removeTaskActivitiesLocked(boolean pauseImmediately, String reason) {
        performClearTaskAtIndexLocked(0, pauseImmediately, reason);
    }

    /* access modifiers changed from: package-private */
    public String lockTaskAuthToString() {
        switch (this.mLockTaskAuth) {
            case 0:
                return "LOCK_TASK_AUTH_DONT_LOCK";
            case 1:
                return "LOCK_TASK_AUTH_PINNABLE";
            case 2:
                return "LOCK_TASK_AUTH_LAUNCHABLE";
            case 3:
                return "LOCK_TASK_AUTH_WHITELISTED";
            case 4:
                return "LOCK_TASK_AUTH_LAUNCHABLE_PRIV";
            default:
                return "unknown=" + this.mLockTaskAuth;
        }
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
        String pkg = this.realActivity != null ? this.realActivity.getPackageName() : null;
        LockTaskController lockTaskController = this.mService.getLockTaskController();
        switch (r.lockTaskLaunchMode) {
            case 0:
                if (lockTaskController.isPackageWhitelisted(this.userId, pkg)) {
                    i = 3;
                }
                this.mLockTaskAuth = i;
                break;
            case 1:
                this.mLockTaskAuth = 0;
                break;
            case 2:
                this.mLockTaskAuth = 4;
                break;
            case 3:
                if (lockTaskController.isPackageWhitelisted(this.userId, pkg)) {
                    i = 2;
                }
                this.mLockTaskAuth = i;
                break;
        }
        if (ActivityManagerDebugConfig.DEBUG_LOCKTASK) {
            Slog.d(ActivityManagerService.TAG, "setLockTaskAuth: task=" + this + " mLockTaskAuth=" + lockTaskAuthToString());
        }
    }

    /* access modifiers changed from: protected */
    public boolean isResizeable(boolean checkSupportsPip) {
        return this.mService.mForceResizableActivities || ActivityInfo.isResizeableMode(this.mResizeMode) || (checkSupportsPip && this.mSupportsPictureInPicture);
    }

    /* access modifiers changed from: package-private */
    public boolean isResizeable() {
        return isResizeable(true);
    }

    public boolean supportsSplitScreenWindowingMode() {
        if (!super.supportsSplitScreenWindowingMode() || !this.mService.mSupportsSplitScreenMultiWindow) {
            return false;
        }
        if (this.mService.mForceResizableActivities || (isResizeable(false) && !ActivityInfo.isPreserveOrientationMode(this.mResizeMode))) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean canBeLaunchedOnDisplay(int displayId) {
        return this.mService.mStackSupervisor.canPlaceEntityOnDisplay(displayId, isResizeable(false), -1, -1, null);
    }

    private boolean canResizeToBounds(Rect bounds) {
        boolean z = true;
        if (bounds == null || !inFreeformWindowingMode()) {
            return true;
        }
        boolean landscape = bounds.width() > bounds.height();
        Rect configBounds = getOverrideBounds();
        if (this.mResizeMode == 7) {
            if (!configBounds.isEmpty()) {
                if (landscape != (configBounds.width() > configBounds.height())) {
                    z = false;
                }
            }
            return z;
        }
        if ((this.mResizeMode == 6 && landscape) || (this.mResizeMode == 5 && !landscape)) {
            z = false;
        }
        return z;
    }

    /* access modifiers changed from: package-private */
    public boolean isClearingToReuseTask() {
        return this.mReuseTask;
    }

    /* access modifiers changed from: package-private */
    public final ActivityRecord findActivityInHistoryLocked(ActivityRecord r) {
        ComponentName realActivity2 = r.realActivity;
        for (int activityNdx = this.mActivities.size() - 1; activityNdx >= 0; activityNdx--) {
            ActivityRecord candidate = this.mActivities.get(activityNdx);
            if (!candidate.finishing && candidate.realActivity.equals(realActivity2)) {
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
            if (!relinquish || (r.info.flags & 4096) != 0) {
                if (r.intent != null && (r.intent.getFlags() & DumpState.DUMP_FROZEN) != 0) {
                    break;
                }
                activityNdx++;
            } else {
                activityNdx++;
                break;
            }
        }
        if (activityNdx > 0) {
            String label = null;
            String iconFilename = null;
            int iconResource = -1;
            int colorPrimary = 0;
            int colorBackground = 0;
            int statusBarColor = 0;
            int navigationBarColor = 0;
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
                        }
                    }
                    topActivity = false;
                }
            }
            int i = numActivities;
            ActivityManager.TaskDescription taskDescription = r4;
            ActivityManager.TaskDescription taskDescription2 = new ActivityManager.TaskDescription(label, null, iconResource, iconFilename, colorPrimary, colorBackground, statusBarColor, navigationBarColor);
            this.lastTaskDescription = taskDescription;
            if (this.mWindowContainerController != null) {
                this.mWindowContainerController.setTaskDescription(this.lastTaskDescription);
            }
            if (this.taskId == this.mAffiliatedTaskId) {
                this.mAffiliatedTaskColor = this.lastTaskDescription.getPrimaryColor();
                return;
            }
            return;
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

    /* access modifiers changed from: protected */
    public void adjustForMinimalTaskDimensions(Rect bounds) {
        if (bounds != null) {
            int minWidth = this.mMinWidth;
            int minHeight = this.mMinHeight;
            if (!inPinnedWindowingMode()) {
                if (minWidth == -1) {
                    minWidth = this.mDefaultMinSize;
                }
                if (minHeight == -1) {
                    minHeight = this.mDefaultMinSize;
                }
                if (HwFreeFormUtils.isFreeFormEnable() && inFreeformWindowingMode() && this.mMinWidth < 0 && this.mMinHeight < 0) {
                    minWidth = HwFreeFormUtils.getFreeformMinLength();
                    minHeight = HwFreeFormUtils.getFreeformMinLength();
                }
            }
            boolean adjustHeight = false;
            boolean adjustWidth = minWidth > bounds.width();
            if (minHeight > bounds.height()) {
                adjustHeight = true;
            }
            if (adjustWidth || adjustHeight) {
                Rect configBounds = getOverrideBounds();
                if (adjustWidth) {
                    if (configBounds.isEmpty() || bounds.right != configBounds.right) {
                        bounds.right = bounds.left + minWidth;
                    } else {
                        bounds.left = bounds.right - minWidth;
                    }
                }
                if (adjustHeight) {
                    if (configBounds.isEmpty() || bounds.bottom != configBounds.bottom) {
                        bounds.bottom = bounds.top + minHeight;
                    } else {
                        bounds.top = bounds.bottom - minHeight;
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public Configuration computeNewOverrideConfigurationForBounds(Rect bounds, Rect insetBounds) {
        Configuration newOverrideConfig = new Configuration();
        if (bounds != null) {
            newOverrideConfig.setTo(getOverrideConfiguration());
            this.mTmpRect.set(bounds);
            if (!inCoordinationPrimaryWindowingMode() && !inCoordinationSecondaryWindowingMode()) {
                adjustForMinimalTaskDimensions(this.mTmpRect);
            }
            computeOverrideConfiguration(newOverrideConfig, this.mTmpRect, insetBounds, this.mTmpRect.right != bounds.right, this.mTmpRect.bottom != bounds.bottom);
        }
        return newOverrideConfig;
    }

    /* access modifiers changed from: package-private */
    public boolean updateOverrideConfiguration(Rect bounds) {
        return updateOverrideConfiguration(bounds, null);
    }

    /* access modifiers changed from: protected */
    public void updateHwOverrideConfiguration(Rect bounds) {
    }

    /* access modifiers changed from: package-private */
    public void activityResumedInTop() {
    }

    /* access modifiers changed from: package-private */
    public boolean updateOverrideConfiguration(Rect bounds, Rect insetBounds) {
        if (equivalentOverrideBounds(bounds) && (this.mStack == null || !HwPCUtils.isExtDynamicStack(this.mStack.getStackId()))) {
            return false;
        }
        Rect currentBounds = getOverrideBounds();
        this.mTmpConfig.setTo(getOverrideConfiguration());
        Configuration newConfig = getOverrideConfiguration();
        boolean matchParentBounds = bounds == null || bounds.isEmpty();
        boolean persistBounds = getWindowConfiguration().persistTaskBounds();
        if (matchParentBounds) {
            if (!currentBounds.isEmpty() && persistBounds) {
                this.mLastNonFullscreenBounds = currentBounds;
            }
            setBounds(null);
            newConfig.unset();
        } else {
            this.mTmpRect.set(bounds);
            if (!inCoordinationPrimaryWindowingMode() && !inCoordinationSecondaryWindowingMode()) {
                adjustForMinimalTaskDimensions(this.mTmpRect);
            }
            setBounds(this.mTmpRect);
            if (this.mStack == null || persistBounds) {
                this.mLastNonFullscreenBounds = getOverrideBounds();
            }
            computeOverrideConfiguration(newConfig, this.mTmpRect, insetBounds, this.mTmpRect.right != bounds.right, this.mTmpRect.bottom != bounds.bottom);
            if (this.mRootActivityInfo != null && isMaximizedPortraitAppOnPCMode(this.mRootActivityInfo.packageName)) {
                newConfig.orientation = 1;
                this.mTmpConfig.orientation = 1;
            }
        }
        onOverrideConfigurationChanged(newConfig);
        updateHwOverrideConfiguration(bounds);
        return !this.mTmpConfig.equals(newConfig);
    }

    /* access modifiers changed from: package-private */
    public void onActivityStateChanged(ActivityRecord record, ActivityStack.ActivityState state, String reason) {
        ActivityStack parent = getStack();
        if (parent != null) {
            parent.onActivityStateChanged(record, state, reason);
        }
    }

    public void onConfigurationChanged(Configuration newParentConfig) {
        boolean wasInMultiWindowMode = inMultiWindowMode();
        super.onConfigurationChanged(newParentConfig);
        if (wasInMultiWindowMode != inMultiWindowMode()) {
            this.mService.mStackSupervisor.scheduleUpdateMultiWindowMode(this);
        }
    }

    /* access modifiers changed from: package-private */
    public void computeOverrideConfiguration(Configuration config, Rect bounds, Rect insetBounds, boolean overrideWidth, boolean overrideHeight) {
        int i;
        Configuration configuration = config;
        Rect rect = bounds;
        if (getParent() == null) {
            Slog.w(ActivityManagerService.TAG, "computeOverrideConfiguration: getParent return null!");
            return;
        }
        this.mTmpNonDecorBounds.set(rect);
        this.mTmpStableBounds.set(rect);
        config.unset();
        Configuration parentConfig = getParent().getConfiguration();
        float density = ((float) parentConfig.densityDpi) * 0.00625f;
        if (this.mStack != null) {
            this.mStack.getWindowContainerController().adjustConfigurationForBounds(rect, insetBounds, this.mTmpNonDecorBounds, this.mTmpStableBounds, overrideWidth, overrideHeight, density, configuration, parentConfig, getWindowingMode());
            if (configuration.screenWidthDp <= configuration.screenHeightDp) {
                i = 1;
            } else {
                i = 2;
            }
            configuration.orientation = i;
            overrideConfigOrienForFreeForm(config);
            int compatScreenWidthDp = (int) (((float) this.mTmpNonDecorBounds.width()) / density);
            int compatScreenHeightDp = (int) (((float) this.mTmpNonDecorBounds.height()) / density);
            configuration.screenLayout = Configuration.reduceScreenLayout(36, Math.max(compatScreenHeightDp, compatScreenWidthDp), Math.min(compatScreenHeightDp, compatScreenWidthDp));
            return;
        }
        throw new IllegalArgumentException("Expected stack when calculating override config");
    }

    /* access modifiers changed from: package-private */
    public Rect updateOverrideConfigurationFromLaunchBounds() {
        Rect bounds = getLaunchBounds();
        updateOverrideConfiguration(bounds);
        if (bounds != null && !bounds.isEmpty()) {
            bounds.set(getOverrideBounds());
        }
        return bounds;
    }

    /* access modifiers changed from: package-private */
    public void updateOverrideConfigurationForStack(ActivityStack inStack) {
        if (this.mStack == null || this.mStack != inStack) {
            if (!inStack.inFreeformWindowingMode()) {
                updateOverrideConfiguration(inStack.getOverrideBounds());
            } else if (!isResizeable()) {
                throw new IllegalArgumentException("Can not position non-resizeable task=" + this + " in stack=" + inStack);
            } else if (matchParentBounds()) {
                if (this.mLastNonFullscreenBounds != null) {
                    updateOverrideConfiguration(this.mLastNonFullscreenBounds);
                } else {
                    this.mService.mStackSupervisor.getLaunchParamsController().layoutTask(this, null);
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public Rect getLaunchBounds() {
        Rect rect = null;
        if (this.mStack == null) {
            return null;
        }
        int windowingMode = getWindowingMode();
        if (!isActivityTypeStandardOrUndefined() || windowingMode == 1 || (windowingMode == 3 && !isResizeable())) {
            if (isResizeable()) {
                rect = this.mStack.getOverrideBounds();
            }
            return rect;
        } else if (!getWindowConfiguration().persistTaskBounds()) {
            return this.mStack.getOverrideBounds();
        } else {
            return this.mLastNonFullscreenBounds;
        }
    }

    /* access modifiers changed from: package-private */
    public void addStartingWindowsForVisibleActivities(boolean taskSwitch) {
        for (int activityNdx = this.mActivities.size() - 1; activityNdx >= 0; activityNdx--) {
            ActivityRecord r = this.mActivities.get(activityNdx);
            if (r.visible) {
                r.showStartingWindow(null, false, taskSwitch);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setRootProcess(ProcessRecord proc) {
        clearRootProcess();
        if (this.intent != null && (this.intent.getFlags() & DumpState.DUMP_VOLUMES) == 0) {
            this.mRootProcess = proc;
            proc.recentTasks.add(this);
        }
    }

    /* access modifiers changed from: package-private */
    public void clearRootProcess() {
        if (this.mRootProcess != null) {
            this.mRootProcess.recentTasks.remove(this);
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
            pw.print("realActivity=");
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
            if (this.mPrevAffiliate == null) {
                pw.print("null");
            } else {
                pw.print(Integer.toHexString(System.identityHashCode(this.mPrevAffiliate)));
            }
            pw.print(") nextAffiliation=");
            pw.print(this.mNextAffiliateTaskId);
            pw.print(" (");
            if (this.mNextAffiliate == null) {
                pw.print("null");
            } else {
                pw.print(Integer.toHexString(System.identityHashCode(this.mNextAffiliate)));
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
        if (this.stringName != null) {
            sb.append(this.stringName);
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
        } else if (this.affinityIntent == null || this.affinityIntent.getComponent() == null) {
            sb.append(" ??");
        } else {
            sb.append(" aI=");
            sb.append(this.affinityIntent.getComponent().flattenToShortString());
        }
        this.stringName = sb.toString();
        return toString();
    }

    public void writeToProto(ProtoOutputStream proto, long fieldId) {
        long token = proto.start(fieldId);
        super.writeToProto(proto, 1146756268033L, false);
        proto.write(1120986464258L, this.taskId);
        for (int i = this.mActivities.size() - 1; i >= 0; i--) {
            this.mActivities.get(i).writeToProto(proto, 2246267895811L);
        }
        proto.write(1120986464260L, this.mStack.mStackId);
        if (this.mLastNonFullscreenBounds != null) {
            this.mLastNonFullscreenBounds.writeToProto(proto, 1146756268037L);
        }
        if (this.realActivity != null) {
            proto.write(1138166333446L, this.realActivity.flattenToShortString());
        }
        if (this.origActivity != null) {
            proto.write(1138166333447L, this.origActivity.flattenToShortString());
        }
        proto.write(1120986464264L, getActivityType());
        proto.write(1120986464265L, this.mResizeMode);
        proto.write(1133871366154L, matchParentBounds());
        if (!matchParentBounds()) {
            getOverrideBounds().writeToProto(proto, 1146756268043L);
        }
        proto.write(1120986464268L, this.mMinWidth);
        proto.write(1120986464269L, this.mMinHeight);
        proto.end(token);
    }

    /* access modifiers changed from: package-private */
    public void saveToXml(XmlSerializer out) throws IOException, XmlPullParserException {
        if (ActivityManagerDebugConfig.DEBUG_RECENTS) {
            Slog.i(ActivityManagerService.TAG, "Saving task=" + this);
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
        out.attribute(null, ATTR_LASTTIMEMOVED, String.valueOf(this.mLastTimeMoved));
        out.attribute(null, ATTR_NEVERRELINQUISH, String.valueOf(this.mNeverRelinquishIdentity));
        if (this.lastDescription != null) {
            out.attribute(null, ATTR_LASTDESCRIPTION, this.lastDescription.toString());
        }
        if (this.lastTaskDescription != null) {
            this.lastTaskDescription.saveToXml(out);
        }
        out.attribute(null, ATTR_TASK_AFFILIATION_COLOR, String.valueOf(this.mAffiliatedTaskColor));
        out.attribute(null, ATTR_TASK_AFFILIATION, String.valueOf(this.mAffiliatedTaskId));
        out.attribute(null, ATTR_PREV_AFFILIATION, String.valueOf(this.mPrevAffiliateTaskId));
        out.attribute(null, ATTR_NEXT_AFFILIATION, String.valueOf(this.mNextAffiliateTaskId));
        out.attribute(null, ATTR_CALLING_UID, String.valueOf(this.mCallingUid));
        out.attribute(null, ATTR_CALLING_PACKAGE, this.mCallingPackage == null ? BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS : this.mCallingPackage);
        out.attribute(null, ATTR_RESIZE_MODE, String.valueOf(this.mResizeMode));
        out.attribute(null, ATTR_SUPPORTS_PICTURE_IN_PICTURE, String.valueOf(this.mSupportsPictureInPicture));
        if (this.mLastNonFullscreenBounds != null) {
            out.attribute(null, ATTR_NON_FULLSCREEN_BOUNDS, this.mLastNonFullscreenBounds.flattenToString());
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
            out.startTag(null, "intent");
            this.intent.saveToXml(out);
            out.endTag(null, "intent");
        }
        ArrayList<ActivityRecord> activities = this.mActivities;
        int numActivities = activities.size();
        int activityNdx = 0;
        while (activityNdx < numActivities) {
            ActivityRecord r = activities.get(activityNdx);
            if (r.info.persistableMode != 0 && r.isPersistable()) {
                if (((r.intent.getFlags() & DumpState.DUMP_FROZEN) | 8192) != 524288 || activityNdx <= 0) {
                    out.startTag(null, TAG_ACTIVITY);
                    r.saveToXml(out);
                    out.endTag(null, TAG_ACTIVITY);
                    activityNdx++;
                } else {
                    return;
                }
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

    static TaskRecord create(ActivityManagerService service, int taskId2, ActivityInfo info, Intent intent2, IVoiceInteractionSession voiceSession2, IVoiceInteractor voiceInteractor2) {
        return getTaskRecordFactory().create(service, taskId2, info, intent2, voiceSession2, voiceInteractor2);
    }

    static TaskRecord create(ActivityManagerService service, int taskId2, ActivityInfo info, Intent intent2, ActivityManager.TaskDescription taskDescription) {
        return getTaskRecordFactory().create(service, taskId2, info, intent2, taskDescription);
    }

    static TaskRecord restoreFromXml(XmlPullParser in, ActivityStackSupervisor stackSupervisor) throws IOException, XmlPullParserException {
        return getTaskRecordFactory().restoreFromXml(in, stackSupervisor);
    }
}
