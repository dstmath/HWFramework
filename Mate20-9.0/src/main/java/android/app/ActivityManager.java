package android.app;

import android.annotation.SystemApi;
import android.app.IActivityManager;
import android.app.IAppTask;
import android.app.IUidObserver;
import android.app.job.JobInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.ConfigurationInfo;
import android.content.pm.IPackageDataObserver;
import android.content.pm.PackageManager;
import android.content.pm.ParceledListSlice;
import android.content.pm.UserInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.GraphicBuffer;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.WorkSource;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.util.Singleton;
import android.util.Size;
import com.android.internal.R;
import com.android.internal.os.RoSystemProperties;
import com.android.internal.os.TransferPipe;
import com.android.internal.util.FastPrintWriter;
import com.android.internal.util.MemInfoReader;
import com.android.server.LocalServices;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;
import org.xmlpull.v1.XmlSerializer;

public class ActivityManager {
    public static final String ACTION_REPORT_HEAP_LIMIT = "android.app.action.REPORT_HEAP_LIMIT";
    public static final int APP_START_MODE_DELAYED = 1;
    public static final int APP_START_MODE_DELAYED_RIGID = 2;
    public static final int APP_START_MODE_DISABLED = 3;
    public static final int APP_START_MODE_NORMAL = 0;
    public static final int ASSIST_CONTEXT_AUTOFILL = 2;
    public static final int ASSIST_CONTEXT_BASIC = 0;
    public static final int ASSIST_CONTEXT_FULL = 1;
    public static final int BROADCAST_FAILED_USER_STOPPED = -2;
    public static final int BROADCAST_STICKY_CANT_HAVE_PERMISSION = -1;
    public static final int BROADCAST_SUCCESS = 0;
    public static final int BUGREPORT_OPTION_FULL = 0;
    public static final int BUGREPORT_OPTION_INTERACTIVE = 1;
    public static final int BUGREPORT_OPTION_REMOTE = 2;
    public static final int BUGREPORT_OPTION_TELEPHONY = 4;
    public static final int BUGREPORT_OPTION_WEAR = 3;
    public static final int BUGREPORT_OPTION_WIFI = 5;
    public static final int COMPAT_MODE_ALWAYS = -1;
    public static final int COMPAT_MODE_DISABLED = 0;
    public static final int COMPAT_MODE_ENABLED = 1;
    public static final int COMPAT_MODE_NEVER = -2;
    public static final int COMPAT_MODE_TOGGLE = 2;
    public static final int COMPAT_MODE_UNKNOWN = -3;
    private static final boolean DEVELOPMENT_FORCE_LOW_RAM = SystemProperties.getBoolean("debug.force_low_ram", false);
    private static final int FIRST_START_FATAL_ERROR_CODE = -100;
    private static final int FIRST_START_NON_FATAL_ERROR_CODE = 100;
    private static final int FIRST_START_SUCCESS_CODE = 0;
    public static final int FLAG_AND_LOCKED = 2;
    public static final int FLAG_AND_UNLOCKED = 4;
    public static final int FLAG_AND_UNLOCKING_OR_UNLOCKED = 8;
    public static final int FLAG_OR_STOPPED = 1;
    private static final Singleton<IActivityManager> IActivityManagerSingleton = new Singleton<IActivityManager>() {
        /* access modifiers changed from: protected */
        public IActivityManager create() {
            return IActivityManager.Stub.asInterface(ServiceManager.getService(Context.ACTIVITY_SERVICE));
        }
    };
    public static final int INTENT_SENDER_ACTIVITY = 2;
    public static final int INTENT_SENDER_ACTIVITY_RESULT = 3;
    public static final int INTENT_SENDER_BROADCAST = 1;
    public static final int INTENT_SENDER_FOREGROUND_SERVICE = 5;
    public static final int INTENT_SENDER_SERVICE = 4;
    private static final int LAST_START_FATAL_ERROR_CODE = -1;
    private static final int LAST_START_NON_FATAL_ERROR_CODE = 199;
    private static final int LAST_START_SUCCESS_CODE = 99;
    public static final int LOCK_TASK_MODE_LOCKED = 1;
    public static final int LOCK_TASK_MODE_NONE = 0;
    public static final int LOCK_TASK_MODE_PINNED = 2;
    public static final int MAX_PROCESS_STATE = 19;
    public static final String META_HOME_ALTERNATE = "android.app.home.alternate";
    public static final int MIN_PROCESS_STATE = 0;
    public static final int MOVE_TASK_NO_USER_ACTION = 2;
    public static final int MOVE_TASK_WITH_HOME = 1;
    public static final int PROCESS_STATE_BACKUP = 8;
    public static final int PROCESS_STATE_BOUND_FOREGROUND_SERVICE = 4;
    public static final int PROCESS_STATE_CACHED_ACTIVITY = 15;
    public static final int PROCESS_STATE_CACHED_ACTIVITY_CLIENT = 16;
    public static final int PROCESS_STATE_CACHED_EMPTY = 18;
    public static final int PROCESS_STATE_CACHED_RECENT = 17;
    public static final int PROCESS_STATE_FOREGROUND_SERVICE = 3;
    public static final int PROCESS_STATE_HEAVY_WEIGHT = 12;
    public static final int PROCESS_STATE_HOME = 13;
    public static final int PROCESS_STATE_IMPORTANT_BACKGROUND = 6;
    public static final int PROCESS_STATE_IMPORTANT_FOREGROUND = 5;
    public static final int PROCESS_STATE_LAST_ACTIVITY = 14;
    public static final int PROCESS_STATE_NONEXISTENT = 19;
    public static final int PROCESS_STATE_PERSISTENT = 0;
    public static final int PROCESS_STATE_PERSISTENT_UI = 1;
    public static final int PROCESS_STATE_RECEIVER = 10;
    public static final int PROCESS_STATE_SERVICE = 9;
    public static final int PROCESS_STATE_TOP = 2;
    public static final int PROCESS_STATE_TOP_SLEEPING = 11;
    public static final int PROCESS_STATE_TRANSIENT_BACKGROUND = 7;
    public static final int PROCESS_STATE_UNKNOWN = -1;
    public static final int RECENT_IGNORE_UNAVAILABLE = 2;
    public static final int RECENT_WITH_EXCLUDED = 1;
    public static final int RESIZE_MODE_FORCED = 2;
    public static final int RESIZE_MODE_PRESERVE_WINDOW = 1;
    public static final int RESIZE_MODE_SYSTEM = 0;
    public static final int RESIZE_MODE_SYSTEM_SCREEN_ROTATION = 1;
    public static final int RESIZE_MODE_USER = 1;
    public static final int RESIZE_MODE_USER_FORCED = 3;
    public static final int SPLIT_SCREEN_CREATE_MODE_BOTTOM_OR_RIGHT = 1;
    public static final int SPLIT_SCREEN_CREATE_MODE_TOP_OR_LEFT = 0;
    public static final int START_ABORTED = 102;
    public static final int START_ASSISTANT_HIDDEN_SESSION = -90;
    public static final int START_ASSISTANT_NOT_ACTIVE_SESSION = -89;
    public static final int START_CANCELED = -96;
    public static final int START_CLASS_NOT_FOUND = -92;
    public static final int START_DELIVERED_TO_TOP = 3;
    public static final int START_FLAG_DEBUG = 2;
    public static final int START_FLAG_NATIVE_DEBUGGING = 8;
    public static final int START_FLAG_ONLY_IF_NEEDED = 1;
    public static final int START_FLAG_TRACK_ALLOCATION = 4;
    public static final int START_FORWARD_AND_REQUEST_CONFLICT = -93;
    public static final int START_INTENT_NOT_RESOLVED = -91;
    public static final int START_NOT_ACTIVITY = -95;
    public static final int START_NOT_CURRENT_USER_ACTIVITY = -98;
    public static final int START_NOT_VOICE_COMPATIBLE = -97;
    public static final int START_PERMISSION_DENIED = -94;
    public static final int START_RETURN_INTENT_TO_CALLER = 1;
    public static final int START_RETURN_LOCK_TASK_MODE_VIOLATION = 101;
    public static final int START_SUCCESS = 0;
    public static final int START_SWITCHES_CANCELED = 100;
    public static final int START_TASK_TO_FRONT = 2;
    public static final int START_VOICE_HIDDEN_SESSION = -100;
    public static final int START_VOICE_NOT_ACTIVE_SESSION = -99;
    /* access modifiers changed from: private */
    public static String TAG = "ActivityManager";
    public static final int UID_OBSERVER_ACTIVE = 8;
    public static final int UID_OBSERVER_CACHED = 16;
    public static final int UID_OBSERVER_GONE = 2;
    public static final int UID_OBSERVER_IDLE = 4;
    public static final int UID_OBSERVER_PROCSTATE = 1;
    public static final int USER_OP_ERROR_IS_SYSTEM = -3;
    public static final int USER_OP_ERROR_RELATED_USERS_CANNOT_STOP = -4;
    public static final int USER_OP_IS_CURRENT = -2;
    public static final int USER_OP_SUCCESS = 0;
    public static final int USER_OP_UNKNOWN_USER = -1;
    private static int gMaxRecentTasks = -1;
    private static volatile boolean sSystemReady = false;
    Point mAppTaskThumbnailSize;
    private final Context mContext;
    final ArrayMap<OnUidImportanceListener, UidObserver> mImportanceListeners = new ArrayMap<>();

    public static class AppTask {
        private IAppTask mAppTaskImpl;

        public AppTask(IAppTask task) {
            this.mAppTaskImpl = task;
        }

        public void finishAndRemoveTask() {
            try {
                this.mAppTaskImpl.finishAndRemoveTask();
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }

        public RecentTaskInfo getTaskInfo() {
            try {
                return this.mAppTaskImpl.getTaskInfo();
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }

        public void moveToFront() {
            try {
                this.mAppTaskImpl.moveToFront();
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }

        public void startActivity(Context context, Intent intent, Bundle options) {
            ActivityThread thread = ActivityThread.currentActivityThread();
            thread.getInstrumentation().execStartActivityFromAppTask(context, thread.getApplicationThread(), this.mAppTaskImpl, intent, options);
        }

        public void setExcludeFromRecents(boolean exclude) {
            try {
                this.mAppTaskImpl.setExcludeFromRecents(exclude);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface BugreportMode {
    }

    public static class MemoryInfo implements Parcelable {
        public static final Parcelable.Creator<MemoryInfo> CREATOR = new Parcelable.Creator<MemoryInfo>() {
            public MemoryInfo createFromParcel(Parcel source) {
                return new MemoryInfo(source);
            }

            public MemoryInfo[] newArray(int size) {
                return new MemoryInfo[size];
            }
        };
        public long availMem;
        public long foregroundAppThreshold;
        public long hiddenAppThreshold;
        public boolean lowMemory;
        public long secondaryServerThreshold;
        public long threshold;
        public long totalMem;
        public long visibleAppThreshold;

        public MemoryInfo() {
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeLong(this.availMem);
            dest.writeLong(this.totalMem);
            dest.writeLong(this.threshold);
            dest.writeInt(this.lowMemory ? 1 : 0);
            dest.writeLong(this.hiddenAppThreshold);
            dest.writeLong(this.secondaryServerThreshold);
            dest.writeLong(this.visibleAppThreshold);
            dest.writeLong(this.foregroundAppThreshold);
        }

        public void readFromParcel(Parcel source) {
            this.availMem = source.readLong();
            this.totalMem = source.readLong();
            this.threshold = source.readLong();
            this.lowMemory = source.readInt() != 0;
            this.hiddenAppThreshold = source.readLong();
            this.secondaryServerThreshold = source.readLong();
            this.visibleAppThreshold = source.readLong();
            this.foregroundAppThreshold = source.readLong();
        }

        private MemoryInfo(Parcel source) {
            readFromParcel(source);
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface MoveTaskFlags {
    }

    @SystemApi
    public interface OnUidImportanceListener {
        void onUidImportance(int i, int i2);
    }

    public static class ProcessErrorStateInfo implements Parcelable {
        public static final int CRASHED = 1;
        public static final Parcelable.Creator<ProcessErrorStateInfo> CREATOR = new Parcelable.Creator<ProcessErrorStateInfo>() {
            public ProcessErrorStateInfo createFromParcel(Parcel source) {
                return new ProcessErrorStateInfo(source);
            }

            public ProcessErrorStateInfo[] newArray(int size) {
                return new ProcessErrorStateInfo[size];
            }
        };
        public static final int NOT_RESPONDING = 2;
        public static final int NO_ERROR = 0;
        public int condition;
        public byte[] crashData;
        public String longMsg;
        public int pid;
        public String processName;
        public String shortMsg;
        public String stackTrace;
        public String tag;
        public int uid;

        public ProcessErrorStateInfo() {
            this.crashData = null;
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.condition);
            dest.writeString(this.processName);
            dest.writeInt(this.pid);
            dest.writeInt(this.uid);
            dest.writeString(this.tag);
            dest.writeString(this.shortMsg);
            dest.writeString(this.longMsg);
            dest.writeString(this.stackTrace);
        }

        public void readFromParcel(Parcel source) {
            this.condition = source.readInt();
            this.processName = source.readString();
            this.pid = source.readInt();
            this.uid = source.readInt();
            this.tag = source.readString();
            this.shortMsg = source.readString();
            this.longMsg = source.readString();
            this.stackTrace = source.readString();
        }

        private ProcessErrorStateInfo(Parcel source) {
            this.crashData = null;
            readFromParcel(source);
        }
    }

    public static class RecentTaskInfo implements Parcelable {
        public static final Parcelable.Creator<RecentTaskInfo> CREATOR = new Parcelable.Creator<RecentTaskInfo>() {
            public RecentTaskInfo createFromParcel(Parcel source) {
                return new RecentTaskInfo(source);
            }

            public RecentTaskInfo[] newArray(int size) {
                return new RecentTaskInfo[size];
            }
        };
        public int affiliatedTaskColor;
        public int affiliatedTaskId;
        public ComponentName baseActivity;
        public Intent baseIntent;
        public Rect bounds;
        public final Configuration configuration;
        public CharSequence description;
        public long firstActiveTime;
        public int id;
        public long lastActiveTime;
        public int numActivities;
        public ComponentName origActivity;
        public int persistentId;
        public ComponentName realActivity;
        public int resizeMode;
        public int stackId;
        public boolean supportsSplitScreenMultiWindow;
        public TaskDescription taskDescription;
        public ComponentName topActivity;
        public int userId;

        public RecentTaskInfo() {
            this.configuration = new Configuration();
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.id);
            dest.writeInt(this.persistentId);
            if (this.baseIntent != null) {
                dest.writeInt(1);
                this.baseIntent.writeToParcel(dest, 0);
            } else {
                dest.writeInt(0);
            }
            ComponentName.writeToParcel(this.origActivity, dest);
            ComponentName.writeToParcel(this.realActivity, dest);
            TextUtils.writeToParcel(this.description, dest, 1);
            if (this.taskDescription != null) {
                dest.writeInt(1);
                this.taskDescription.writeToParcel(dest, 0);
            } else {
                dest.writeInt(0);
            }
            dest.writeInt(this.stackId);
            dest.writeInt(this.userId);
            dest.writeLong(this.lastActiveTime);
            dest.writeInt(this.affiliatedTaskId);
            dest.writeInt(this.affiliatedTaskColor);
            ComponentName.writeToParcel(this.baseActivity, dest);
            ComponentName.writeToParcel(this.topActivity, dest);
            dest.writeInt(this.numActivities);
            if (this.bounds != null) {
                dest.writeInt(1);
                this.bounds.writeToParcel(dest, 0);
            } else {
                dest.writeInt(0);
            }
            dest.writeInt(this.supportsSplitScreenMultiWindow ? 1 : 0);
            dest.writeInt(this.resizeMode);
            this.configuration.writeToParcel(dest, flags);
        }

        public void readFromParcel(Parcel source) {
            this.id = source.readInt();
            this.persistentId = source.readInt();
            Rect rect = null;
            this.baseIntent = source.readInt() > 0 ? Intent.CREATOR.createFromParcel(source) : null;
            this.origActivity = ComponentName.readFromParcel(source);
            this.realActivity = ComponentName.readFromParcel(source);
            this.description = (CharSequence) TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(source);
            this.taskDescription = source.readInt() > 0 ? TaskDescription.CREATOR.createFromParcel(source) : null;
            this.stackId = source.readInt();
            this.userId = source.readInt();
            this.lastActiveTime = source.readLong();
            this.affiliatedTaskId = source.readInt();
            this.affiliatedTaskColor = source.readInt();
            this.baseActivity = ComponentName.readFromParcel(source);
            this.topActivity = ComponentName.readFromParcel(source);
            this.numActivities = source.readInt();
            if (source.readInt() > 0) {
                rect = Rect.CREATOR.createFromParcel(source);
            }
            this.bounds = rect;
            boolean z = true;
            if (source.readInt() != 1) {
                z = false;
            }
            this.supportsSplitScreenMultiWindow = z;
            this.resizeMode = source.readInt();
            this.configuration.readFromParcel(source);
        }

        private RecentTaskInfo(Parcel source) {
            this.configuration = new Configuration();
            readFromParcel(source);
        }
    }

    public static class RunningAppProcessInfo implements Parcelable {
        public static final Parcelable.Creator<RunningAppProcessInfo> CREATOR = new Parcelable.Creator<RunningAppProcessInfo>() {
            public RunningAppProcessInfo createFromParcel(Parcel source) {
                return new RunningAppProcessInfo(source);
            }

            public RunningAppProcessInfo[] newArray(int size) {
                return new RunningAppProcessInfo[size];
            }
        };
        public static final int FLAG_CANT_SAVE_STATE = 1;
        public static final int FLAG_HAS_ACTIVITIES = 4;
        public static final int FLAG_PERSISTENT = 2;
        public static final int IMPORTANCE_BACKGROUND = 400;
        public static final int IMPORTANCE_CACHED = 400;
        public static final int IMPORTANCE_CANT_SAVE_STATE = 350;
        public static final int IMPORTANCE_CANT_SAVE_STATE_PRE_26 = 170;
        @Deprecated
        public static final int IMPORTANCE_EMPTY = 500;
        public static final int IMPORTANCE_FOREGROUND = 100;
        public static final int IMPORTANCE_FOREGROUND_SERVICE = 125;
        public static final int IMPORTANCE_GONE = 1000;
        public static final int IMPORTANCE_PERCEPTIBLE = 230;
        public static final int IMPORTANCE_PERCEPTIBLE_PRE_26 = 130;
        public static final int IMPORTANCE_SERVICE = 300;
        public static final int IMPORTANCE_TOP_SLEEPING = 325;
        @Deprecated
        public static final int IMPORTANCE_TOP_SLEEPING_PRE_28 = 150;
        public static final int IMPORTANCE_VISIBLE = 200;
        public static final int REASON_PROVIDER_IN_USE = 1;
        public static final int REASON_SERVICE_IN_USE = 2;
        public static final int REASON_UNKNOWN = 0;
        public int flags;
        public int importance;
        public int importanceReasonCode;
        public ComponentName importanceReasonComponent;
        public int importanceReasonImportance;
        public int importanceReasonPid;
        public int lastTrimLevel;
        public int lru;
        public int pid;
        public String[] pkgList;
        public String processName;
        public int processState;
        public int uid;

        @Retention(RetentionPolicy.SOURCE)
        public @interface Importance {
        }

        public static int procStateToImportance(int procState) {
            if (procState == 19) {
                return 1000;
            }
            if (procState >= 13) {
                return 400;
            }
            if (procState == 12) {
                return IMPORTANCE_CANT_SAVE_STATE;
            }
            if (procState >= 11) {
                return 325;
            }
            if (procState >= 9) {
                return 300;
            }
            if (procState >= 7) {
                return 230;
            }
            if (procState >= 5) {
                return 200;
            }
            if (procState >= 3) {
                return 125;
            }
            return 100;
        }

        public static int procStateToImportanceForClient(int procState, Context clientContext) {
            return procStateToImportanceForTargetSdk(procState, clientContext.getApplicationInfo().targetSdkVersion);
        }

        public static int procStateToImportanceForTargetSdk(int procState, int targetSdkVersion) {
            int importance2 = procStateToImportance(procState);
            if (targetSdkVersion < 26) {
                if (importance2 == 230) {
                    return 130;
                }
                if (importance2 == 325) {
                    return 150;
                }
                if (importance2 == 350) {
                    return 170;
                }
            }
            return importance2;
        }

        public static int importanceToProcState(int importance2) {
            if (importance2 == 1000) {
                return 19;
            }
            if (importance2 >= 400) {
                return 13;
            }
            if (importance2 >= 350) {
                return 12;
            }
            if (importance2 >= 325) {
                return 11;
            }
            if (importance2 >= 300) {
                return 9;
            }
            if (importance2 >= 230) {
                return 7;
            }
            if (importance2 >= 200 || importance2 >= 150) {
                return 5;
            }
            if (importance2 >= 125) {
                return 3;
            }
            return 2;
        }

        public RunningAppProcessInfo() {
            this.importance = 100;
            this.importanceReasonCode = 0;
            this.processState = 5;
        }

        public RunningAppProcessInfo(String pProcessName, int pPid, String[] pArr) {
            this.processName = pProcessName;
            this.pid = pPid;
            this.pkgList = pArr;
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags2) {
            dest.writeString(this.processName);
            dest.writeInt(this.pid);
            dest.writeInt(this.uid);
            dest.writeStringArray(this.pkgList);
            dest.writeInt(this.flags);
            dest.writeInt(this.lastTrimLevel);
            dest.writeInt(this.importance);
            dest.writeInt(this.lru);
            dest.writeInt(this.importanceReasonCode);
            dest.writeInt(this.importanceReasonPid);
            ComponentName.writeToParcel(this.importanceReasonComponent, dest);
            dest.writeInt(this.importanceReasonImportance);
            dest.writeInt(this.processState);
        }

        public void readFromParcel(Parcel source) {
            this.processName = source.readString();
            this.pid = source.readInt();
            this.uid = source.readInt();
            this.pkgList = source.readStringArray();
            this.flags = source.readInt();
            this.lastTrimLevel = source.readInt();
            this.importance = source.readInt();
            this.lru = source.readInt();
            this.importanceReasonCode = source.readInt();
            this.importanceReasonPid = source.readInt();
            this.importanceReasonComponent = ComponentName.readFromParcel(source);
            this.importanceReasonImportance = source.readInt();
            this.processState = source.readInt();
        }

        private RunningAppProcessInfo(Parcel source) {
            readFromParcel(source);
        }
    }

    public static class RunningServiceInfo implements Parcelable {
        public static final Parcelable.Creator<RunningServiceInfo> CREATOR = new Parcelable.Creator<RunningServiceInfo>() {
            public RunningServiceInfo createFromParcel(Parcel source) {
                return new RunningServiceInfo(source);
            }

            public RunningServiceInfo[] newArray(int size) {
                return new RunningServiceInfo[size];
            }
        };
        public static final int FLAG_FOREGROUND = 2;
        public static final int FLAG_PERSISTENT_PROCESS = 8;
        public static final int FLAG_STARTED = 1;
        public static final int FLAG_SYSTEM_PROCESS = 4;
        public long activeSince;
        public int clientCount;
        public int clientLabel;
        public String clientPackage;
        public int crashCount;
        public int flags;
        public boolean foreground;
        public long lastActivityTime;
        public int pid;
        public String process;
        public long restarting;
        public ComponentName service;
        public boolean started;
        public int uid;

        public RunningServiceInfo() {
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags2) {
            ComponentName.writeToParcel(this.service, dest);
            dest.writeInt(this.pid);
            dest.writeInt(this.uid);
            dest.writeString(this.process);
            dest.writeInt(this.foreground ? 1 : 0);
            dest.writeLong(this.activeSince);
            dest.writeInt(this.started ? 1 : 0);
            dest.writeInt(this.clientCount);
            dest.writeInt(this.crashCount);
            dest.writeLong(this.lastActivityTime);
            dest.writeLong(this.restarting);
            dest.writeInt(this.flags);
            dest.writeString(this.clientPackage);
            dest.writeInt(this.clientLabel);
        }

        public void readFromParcel(Parcel source) {
            this.service = ComponentName.readFromParcel(source);
            this.pid = source.readInt();
            this.uid = source.readInt();
            this.process = source.readString();
            boolean z = false;
            this.foreground = source.readInt() != 0;
            this.activeSince = source.readLong();
            if (source.readInt() != 0) {
                z = true;
            }
            this.started = z;
            this.clientCount = source.readInt();
            this.crashCount = source.readInt();
            this.lastActivityTime = source.readLong();
            this.restarting = source.readLong();
            this.flags = source.readInt();
            this.clientPackage = source.readString();
            this.clientLabel = source.readInt();
        }

        private RunningServiceInfo(Parcel source) {
            readFromParcel(source);
        }
    }

    public static class RunningTaskInfo implements Parcelable {
        public static final Parcelable.Creator<RunningTaskInfo> CREATOR = new Parcelable.Creator<RunningTaskInfo>() {
            public RunningTaskInfo createFromParcel(Parcel source) {
                return new RunningTaskInfo(source);
            }

            public RunningTaskInfo[] newArray(int size) {
                return new RunningTaskInfo[size];
            }
        };
        public ComponentName baseActivity;
        public final Configuration configuration;
        public CharSequence description;
        public int id;
        public long lastActiveTime;
        public int numActivities;
        public int numRunning;
        public int resizeMode;
        public int stackId;
        public boolean supportsSplitScreenMultiWindow;
        public Bitmap thumbnail;
        public ComponentName topActivity;

        public RunningTaskInfo() {
            this.configuration = new Configuration();
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.id);
            dest.writeInt(this.stackId);
            ComponentName.writeToParcel(this.baseActivity, dest);
            ComponentName.writeToParcel(this.topActivity, dest);
            if (this.thumbnail != null) {
                dest.writeInt(1);
                this.thumbnail.writeToParcel(dest, 0);
            } else {
                dest.writeInt(0);
            }
            TextUtils.writeToParcel(this.description, dest, 1);
            dest.writeInt(this.numActivities);
            dest.writeInt(this.numRunning);
            dest.writeInt(this.supportsSplitScreenMultiWindow ? 1 : 0);
            dest.writeInt(this.resizeMode);
            this.configuration.writeToParcel(dest, flags);
        }

        public void readFromParcel(Parcel source) {
            this.id = source.readInt();
            this.stackId = source.readInt();
            this.baseActivity = ComponentName.readFromParcel(source);
            this.topActivity = ComponentName.readFromParcel(source);
            if (source.readInt() != 0) {
                this.thumbnail = Bitmap.CREATOR.createFromParcel(source);
            } else {
                this.thumbnail = null;
            }
            this.description = (CharSequence) TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(source);
            this.numActivities = source.readInt();
            this.numRunning = source.readInt();
            this.supportsSplitScreenMultiWindow = source.readInt() != 0;
            this.resizeMode = source.readInt();
            this.configuration.readFromParcel(source);
        }

        private RunningTaskInfo(Parcel source) {
            this.configuration = new Configuration();
            readFromParcel(source);
        }
    }

    public static class StackId {
        public static final int INVALID_STACK_ID = -1;

        private StackId() {
        }
    }

    public static class StackInfo implements Parcelable {
        public static final Parcelable.Creator<StackInfo> CREATOR = new Parcelable.Creator<StackInfo>() {
            public StackInfo createFromParcel(Parcel source) {
                return new StackInfo(source);
            }

            public StackInfo[] newArray(int size) {
                return new StackInfo[size];
            }
        };
        public Rect bounds;
        public final Configuration configuration;
        public int displayId;
        public int position;
        public int stackId;
        public Rect[] taskBounds;
        public int[] taskIds;
        public String[] taskNames;
        public int[] taskUserIds;
        public ComponentName topActivity;
        public int userId;
        public boolean visible;

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.stackId);
            dest.writeInt(this.bounds.left);
            dest.writeInt(this.bounds.top);
            dest.writeInt(this.bounds.right);
            dest.writeInt(this.bounds.bottom);
            dest.writeIntArray(this.taskIds);
            dest.writeStringArray(this.taskNames);
            int boundsCount = this.taskBounds == null ? 0 : this.taskBounds.length;
            dest.writeInt(boundsCount);
            for (int i = 0; i < boundsCount; i++) {
                dest.writeInt(this.taskBounds[i].left);
                dest.writeInt(this.taskBounds[i].top);
                dest.writeInt(this.taskBounds[i].right);
                dest.writeInt(this.taskBounds[i].bottom);
            }
            dest.writeIntArray(this.taskUserIds);
            dest.writeInt(this.displayId);
            dest.writeInt(this.userId);
            dest.writeInt(this.visible ? 1 : 0);
            dest.writeInt(this.position);
            if (this.topActivity != null) {
                dest.writeInt(1);
                this.topActivity.writeToParcel(dest, 0);
            } else {
                dest.writeInt(0);
            }
            this.configuration.writeToParcel(dest, flags);
        }

        public void readFromParcel(Parcel source) {
            this.stackId = source.readInt();
            this.bounds = new Rect(source.readInt(), source.readInt(), source.readInt(), source.readInt());
            this.taskIds = source.createIntArray();
            this.taskNames = source.createStringArray();
            int boundsCount = source.readInt();
            boolean z = false;
            if (boundsCount > 0) {
                this.taskBounds = new Rect[boundsCount];
                for (int i = 0; i < boundsCount; i++) {
                    this.taskBounds[i] = new Rect();
                    this.taskBounds[i].set(source.readInt(), source.readInt(), source.readInt(), source.readInt());
                }
            } else {
                this.taskBounds = null;
            }
            this.taskUserIds = source.createIntArray();
            this.displayId = source.readInt();
            this.userId = source.readInt();
            if (source.readInt() > 0) {
                z = true;
            }
            this.visible = z;
            this.position = source.readInt();
            if (source.readInt() > 0) {
                this.topActivity = ComponentName.readFromParcel(source);
            }
            this.configuration.readFromParcel(source);
        }

        public StackInfo() {
            this.bounds = new Rect();
            this.configuration = new Configuration();
        }

        private StackInfo(Parcel source) {
            this.bounds = new Rect();
            this.configuration = new Configuration();
            readFromParcel(source);
        }

        public String toString(String prefix) {
            StringBuilder sb = new StringBuilder(256);
            sb.append(prefix);
            sb.append("Stack id=");
            sb.append(this.stackId);
            sb.append(" bounds=");
            sb.append(this.bounds.toShortString());
            sb.append(" displayId=");
            sb.append(this.displayId);
            sb.append(" userId=");
            sb.append(this.userId);
            sb.append("\n");
            sb.append(" configuration=");
            sb.append(this.configuration);
            sb.append("\n");
            String prefix2 = prefix + "  ";
            for (int i = 0; i < this.taskIds.length; i++) {
                sb.append(prefix2);
                sb.append("taskId=");
                sb.append(this.taskIds[i]);
                sb.append(": ");
                sb.append(this.taskNames[i]);
                if (this.taskBounds != null) {
                    sb.append(" bounds=");
                    sb.append(this.taskBounds[i].toShortString());
                }
                sb.append(" userId=");
                sb.append(this.taskUserIds[i]);
                sb.append(" visible=");
                sb.append(this.visible);
                if (this.topActivity != null) {
                    sb.append(" topActivity=");
                    sb.append(this.topActivity);
                }
                sb.append("\n");
            }
            return sb.toString();
        }

        public String toString() {
            return toString("");
        }
    }

    public static class TaskDescription implements Parcelable {
        private static final String ATTR_TASKDESCRIPTIONCOLOR_BACKGROUND = "task_description_colorBackground";
        private static final String ATTR_TASKDESCRIPTIONCOLOR_PRIMARY = "task_description_color";
        private static final String ATTR_TASKDESCRIPTIONICON_FILENAME = "task_description_icon_filename";
        private static final String ATTR_TASKDESCRIPTIONICON_RESOURCE = "task_description_icon_resource";
        private static final String ATTR_TASKDESCRIPTIONLABEL = "task_description_label";
        public static final String ATTR_TASKDESCRIPTION_PREFIX = "task_description_";
        public static final Parcelable.Creator<TaskDescription> CREATOR = new Parcelable.Creator<TaskDescription>() {
            public TaskDescription createFromParcel(Parcel source) {
                return new TaskDescription(source);
            }

            public TaskDescription[] newArray(int size) {
                return new TaskDescription[size];
            }
        };
        private int mColorBackground;
        private int mColorPrimary;
        private Bitmap mIcon;
        private String mIconFilename;
        private int mIconRes;
        private String mLabel;
        private int mNavigationBarColor;
        private int mStatusBarColor;

        @Deprecated
        public TaskDescription(String label, Bitmap icon, int colorPrimary) {
            this(label, icon, 0, null, colorPrimary, 0, 0, 0);
            if (colorPrimary != 0 && Color.alpha(colorPrimary) != 255) {
                throw new RuntimeException("A TaskDescription's primary color should be opaque");
            }
        }

        public TaskDescription(String label, int iconRes, int colorPrimary) {
            this(label, null, iconRes, null, colorPrimary, 0, 0, 0);
            if (colorPrimary != 0 && Color.alpha(colorPrimary) != 255) {
                throw new RuntimeException("A TaskDescription's primary color should be opaque");
            }
        }

        @Deprecated
        public TaskDescription(String label, Bitmap icon) {
            this(label, icon, 0, null, 0, 0, 0, 0);
        }

        public TaskDescription(String label, int iconRes) {
            this(label, null, iconRes, null, 0, 0, 0, 0);
        }

        public TaskDescription(String label) {
            this(label, null, 0, null, 0, 0, 0, 0);
        }

        public TaskDescription() {
            this(null, null, 0, null, 0, 0, 0, 0);
        }

        public TaskDescription(String label, Bitmap bitmap, int iconRes, String iconFilename, int colorPrimary, int colorBackground, int statusBarColor, int navigationBarColor) {
            this.mLabel = label;
            this.mIcon = bitmap;
            this.mIconRes = iconRes;
            this.mIconFilename = iconFilename;
            this.mColorPrimary = colorPrimary;
            this.mColorBackground = colorBackground;
            this.mStatusBarColor = statusBarColor;
            this.mNavigationBarColor = navigationBarColor;
        }

        public TaskDescription(TaskDescription td) {
            copyFrom(td);
        }

        public void copyFrom(TaskDescription other) {
            this.mLabel = other.mLabel;
            this.mIcon = other.mIcon;
            this.mIconRes = other.mIconRes;
            this.mIconFilename = other.mIconFilename;
            this.mColorPrimary = other.mColorPrimary;
            this.mColorBackground = other.mColorBackground;
            this.mStatusBarColor = other.mStatusBarColor;
            this.mNavigationBarColor = other.mNavigationBarColor;
        }

        public void copyFromPreserveHiddenFields(TaskDescription other) {
            this.mLabel = other.mLabel;
            this.mIcon = other.mIcon;
            this.mIconRes = other.mIconRes;
            this.mIconFilename = other.mIconFilename;
            this.mColorPrimary = other.mColorPrimary;
            if (other.mColorBackground != 0) {
                this.mColorBackground = other.mColorBackground;
            }
            if (other.mStatusBarColor != 0) {
                this.mStatusBarColor = other.mStatusBarColor;
            }
            if (other.mNavigationBarColor != 0) {
                this.mNavigationBarColor = other.mNavigationBarColor;
            }
        }

        private TaskDescription(Parcel source) {
            readFromParcel(source);
        }

        public void setLabel(String label) {
            this.mLabel = label;
        }

        public void setPrimaryColor(int primaryColor) {
            if (primaryColor == 0 || Color.alpha(primaryColor) == 255) {
                this.mColorPrimary = primaryColor;
                return;
            }
            throw new RuntimeException("A TaskDescription's primary color should be opaque");
        }

        public void setBackgroundColor(int backgroundColor) {
            if (backgroundColor == 0 || Color.alpha(backgroundColor) == 255) {
                this.mColorBackground = backgroundColor;
                return;
            }
            throw new RuntimeException("A TaskDescription's background color should be opaque");
        }

        public void setStatusBarColor(int statusBarColor) {
            this.mStatusBarColor = statusBarColor;
        }

        public void setNavigationBarColor(int navigationBarColor) {
            this.mNavigationBarColor = navigationBarColor;
        }

        public void setIcon(Bitmap icon) {
            this.mIcon = icon;
        }

        public void setIcon(int iconRes) {
            this.mIconRes = iconRes;
        }

        public void setIconFilename(String iconFilename) {
            this.mIconFilename = iconFilename;
            this.mIcon = null;
        }

        public String getLabel() {
            return this.mLabel;
        }

        public Bitmap getIcon() {
            if (this.mIcon != null) {
                return this.mIcon;
            }
            return loadTaskDescriptionIcon(this.mIconFilename, UserHandle.myUserId());
        }

        public int getIconResource() {
            return this.mIconRes;
        }

        public String getIconFilename() {
            return this.mIconFilename;
        }

        public Bitmap getInMemoryIcon() {
            return this.mIcon;
        }

        public static Bitmap loadTaskDescriptionIcon(String iconFilename, int userId) {
            if (iconFilename == null) {
                return null;
            }
            try {
                return ActivityManager.getService().getTaskDescriptionIcon(iconFilename, userId);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }

        public int getPrimaryColor() {
            return this.mColorPrimary;
        }

        public int getBackgroundColor() {
            return this.mColorBackground;
        }

        public int getStatusBarColor() {
            return this.mStatusBarColor;
        }

        public int getNavigationBarColor() {
            return this.mNavigationBarColor;
        }

        public void saveToXml(XmlSerializer out) throws IOException {
            if (this.mLabel != null) {
                out.attribute(null, ATTR_TASKDESCRIPTIONLABEL, this.mLabel);
            }
            if (this.mColorPrimary != 0) {
                out.attribute(null, ATTR_TASKDESCRIPTIONCOLOR_PRIMARY, Integer.toHexString(this.mColorPrimary));
            }
            if (this.mColorBackground != 0) {
                out.attribute(null, ATTR_TASKDESCRIPTIONCOLOR_BACKGROUND, Integer.toHexString(this.mColorBackground));
            }
            if (this.mIconFilename != null) {
                out.attribute(null, ATTR_TASKDESCRIPTIONICON_FILENAME, this.mIconFilename);
            }
            if (this.mIconRes != 0) {
                out.attribute(null, ATTR_TASKDESCRIPTIONICON_RESOURCE, Integer.toString(this.mIconRes));
            }
        }

        public void restoreFromXml(String attrName, String attrValue) {
            if (ATTR_TASKDESCRIPTIONLABEL.equals(attrName)) {
                setLabel(attrValue);
            } else if (ATTR_TASKDESCRIPTIONCOLOR_PRIMARY.equals(attrName)) {
                setPrimaryColor((int) Long.parseLong(attrValue, 16));
            } else if (ATTR_TASKDESCRIPTIONCOLOR_BACKGROUND.equals(attrName)) {
                setBackgroundColor((int) Long.parseLong(attrValue, 16));
            } else if (ATTR_TASKDESCRIPTIONICON_FILENAME.equals(attrName)) {
                setIconFilename(attrValue);
            } else if (ATTR_TASKDESCRIPTIONICON_RESOURCE.equals(attrName)) {
                setIcon(Integer.parseInt(attrValue, 10));
            }
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            if (this.mLabel == null) {
                dest.writeInt(0);
            } else {
                dest.writeInt(1);
                dest.writeString(this.mLabel);
            }
            if (this.mIcon == null) {
                dest.writeInt(0);
            } else {
                dest.writeInt(1);
                this.mIcon.writeToParcel(dest, 0);
            }
            dest.writeInt(this.mIconRes);
            dest.writeInt(this.mColorPrimary);
            dest.writeInt(this.mColorBackground);
            dest.writeInt(this.mStatusBarColor);
            dest.writeInt(this.mNavigationBarColor);
            if (this.mIconFilename == null) {
                dest.writeInt(0);
                return;
            }
            dest.writeInt(1);
            dest.writeString(this.mIconFilename);
        }

        public void readFromParcel(Parcel source) {
            String str = null;
            this.mLabel = source.readInt() > 0 ? source.readString() : null;
            this.mIcon = source.readInt() > 0 ? Bitmap.CREATOR.createFromParcel(source) : null;
            this.mIconRes = source.readInt();
            this.mColorPrimary = source.readInt();
            this.mColorBackground = source.readInt();
            this.mStatusBarColor = source.readInt();
            this.mNavigationBarColor = source.readInt();
            if (source.readInt() > 0) {
                str = source.readString();
            }
            this.mIconFilename = str;
        }

        public String toString() {
            return "TaskDescription Label: " + this.mLabel + " Icon: " + this.mIcon + " IconRes: " + this.mIconRes + " IconFilename: " + this.mIconFilename + " colorPrimary: " + this.mColorPrimary + " colorBackground: " + this.mColorBackground + " statusBarColor: " + this.mColorBackground + " navigationBarColor: " + this.mNavigationBarColor;
        }
    }

    public static class TaskSnapshot implements Parcelable {
        public static final Parcelable.Creator<TaskSnapshot> CREATOR = new Parcelable.Creator<TaskSnapshot>() {
            public TaskSnapshot createFromParcel(Parcel source) {
                return new TaskSnapshot(source);
            }

            public TaskSnapshot[] newArray(int size) {
                return new TaskSnapshot[size];
            }
        };
        private final Rect mContentInsets;
        private final boolean mIsRealSnapshot;
        private final boolean mIsTranslucent;
        private final int mOrientation;
        private final boolean mReducedResolution;
        private final float mScale;
        private final GraphicBuffer mSnapshot;
        private final int mSystemUiVisibility;
        private final int mWindowingMode;

        public TaskSnapshot(GraphicBuffer snapshot, int orientation, Rect contentInsets, boolean reducedResolution, float scale, boolean isRealSnapshot, int windowingMode, int systemUiVisibility, boolean isTranslucent) {
            this.mSnapshot = snapshot;
            this.mOrientation = orientation;
            this.mContentInsets = new Rect(contentInsets);
            this.mReducedResolution = reducedResolution;
            this.mScale = scale;
            this.mIsRealSnapshot = isRealSnapshot;
            this.mWindowingMode = windowingMode;
            this.mSystemUiVisibility = systemUiVisibility;
            this.mIsTranslucent = isTranslucent;
        }

        private TaskSnapshot(Parcel source) {
            this.mSnapshot = (GraphicBuffer) source.readParcelable(null);
            this.mOrientation = source.readInt();
            this.mContentInsets = (Rect) source.readParcelable(null);
            this.mReducedResolution = source.readBoolean();
            this.mScale = source.readFloat();
            this.mIsRealSnapshot = source.readBoolean();
            this.mWindowingMode = source.readInt();
            this.mSystemUiVisibility = source.readInt();
            this.mIsTranslucent = source.readBoolean();
        }

        public GraphicBuffer getSnapshot() {
            if (this.mSnapshot == null || !this.mSnapshot.isDestroyed()) {
                return this.mSnapshot;
            }
            Log.w(ActivityManager.TAG, "native graphic buff is destroyed");
            return null;
        }

        public int getOrientation() {
            return this.mOrientation;
        }

        public Rect getContentInsets() {
            return this.mContentInsets;
        }

        public boolean isReducedResolution() {
            return this.mReducedResolution;
        }

        public boolean isRealSnapshot() {
            return this.mIsRealSnapshot;
        }

        public boolean isTranslucent() {
            return this.mIsTranslucent;
        }

        public int getWindowingMode() {
            return this.mWindowingMode;
        }

        public int getSystemUiVisibility() {
            return this.mSystemUiVisibility;
        }

        public float getScale() {
            return this.mScale;
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            if (this.mSnapshot == null || !this.mSnapshot.isDestroyed()) {
                dest.writeParcelable(this.mSnapshot, 0);
            } else {
                Log.w(ActivityManager.TAG, "writeParcelable null");
                dest.writeParcelable(null, 0);
            }
            dest.writeInt(this.mOrientation);
            dest.writeParcelable(this.mContentInsets, 0);
            dest.writeBoolean(this.mReducedResolution);
            dest.writeFloat(this.mScale);
            dest.writeBoolean(this.mIsRealSnapshot);
            dest.writeInt(this.mWindowingMode);
            dest.writeInt(this.mSystemUiVisibility);
            dest.writeBoolean(this.mIsTranslucent);
        }

        public String toString() {
            int height = 0;
            int width = this.mSnapshot != null ? this.mSnapshot.getWidth() : 0;
            if (this.mSnapshot != null) {
                height = this.mSnapshot.getHeight();
            }
            return "TaskSnapshot{mSnapshot=" + this.mSnapshot + " (" + width + "x" + height + ") mOrientation=" + this.mOrientation + " mContentInsets=" + this.mContentInsets.toShortString() + " mReducedResolution=" + this.mReducedResolution + " mScale=" + this.mScale + " mIsRealSnapshot=" + this.mIsRealSnapshot + " mWindowingMode=" + this.mWindowingMode + " mSystemUiVisibility=" + this.mSystemUiVisibility + " mIsTranslucent=" + this.mIsTranslucent;
        }
    }

    static final class UidObserver extends IUidObserver.Stub {
        final Context mContext;
        final OnUidImportanceListener mListener;

        UidObserver(OnUidImportanceListener listener, Context clientContext) {
            this.mListener = listener;
            this.mContext = clientContext;
        }

        public void onUidStateChanged(int uid, int procState, long procStateSeq) {
            this.mListener.onUidImportance(uid, RunningAppProcessInfo.procStateToImportanceForClient(procState, this.mContext));
        }

        public void onUidGone(int uid, boolean disabled) {
            this.mListener.onUidImportance(uid, 1000);
        }

        public void onUidActive(int uid) {
        }

        public void onUidIdle(int uid, boolean disabled) {
        }

        public void onUidCachedChanged(int uid, boolean cached) {
        }
    }

    public static final int processStateAmToProto(int amInt) {
        switch (amInt) {
            case -1:
                return AppProtoEnums.PROCESS_STATE_UNKNOWN;
            case 0:
                return 1000;
            case 1:
                return 1001;
            case 2:
                return 1002;
            case 3:
                return 1003;
            case 4:
                return 1004;
            case 5:
                return 1005;
            case 6:
                return 1006;
            case 7:
                return 1007;
            case 8:
                return 1008;
            case 9:
                return 1009;
            case 10:
                return 1010;
            case 11:
                return 1011;
            case 12:
                return 1012;
            case 13:
                return AppProtoEnums.PROCESS_STATE_HOME;
            case 14:
                return AppProtoEnums.PROCESS_STATE_LAST_ACTIVITY;
            case 15:
                return 1015;
            case 16:
                return AppProtoEnums.PROCESS_STATE_CACHED_ACTIVITY_CLIENT;
            case 17:
                return AppProtoEnums.PROCESS_STATE_CACHED_RECENT;
            case 18:
                return AppProtoEnums.PROCESS_STATE_CACHED_EMPTY;
            case 19:
                return AppProtoEnums.PROCESS_STATE_NONEXISTENT;
            default:
                return AppProtoEnums.PROCESS_STATE_UNKNOWN_TO_PROTO;
        }
    }

    public static final boolean isProcStateBackground(int procState) {
        return procState >= 7;
    }

    ActivityManager(Context context, Handler handler) {
        this.mContext = context;
    }

    public static final boolean isStartResultSuccessful(int result) {
        return result >= 0 && result <= 99;
    }

    public static final boolean isStartResultFatalError(int result) {
        return -100 <= result && result <= -1;
    }

    public int getFrontActivityScreenCompatMode() {
        try {
            return getService().getFrontActivityScreenCompatMode();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setFrontActivityScreenCompatMode(int mode) {
        try {
            getService().setFrontActivityScreenCompatMode(mode);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int getPackageScreenCompatMode(String packageName) {
        try {
            return getService().getPackageScreenCompatMode(packageName);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setPackageScreenCompatMode(String packageName, int mode) {
        try {
            getService().setPackageScreenCompatMode(packageName, mode);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean getPackageAskScreenCompat(String packageName) {
        try {
            return getService().getPackageAskScreenCompat(packageName);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setPackageAskScreenCompat(String packageName, boolean ask) {
        try {
            getService().setPackageAskScreenCompat(packageName, ask);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int getMemoryClass() {
        return staticGetMemoryClass();
    }

    public static int staticGetMemoryClass() {
        String vmHeapSize = SystemProperties.get("dalvik.vm.heapgrowthlimit", "");
        if (vmHeapSize == null || "".equals(vmHeapSize)) {
            return staticGetLargeMemoryClass();
        }
        return Integer.parseInt(vmHeapSize.substring(0, vmHeapSize.length() - 1));
    }

    public int getLargeMemoryClass() {
        return staticGetLargeMemoryClass();
    }

    public static int staticGetLargeMemoryClass() {
        String vmHeapSize = SystemProperties.get("dalvik.vm.heapsize", "16m");
        return Integer.parseInt(vmHeapSize.substring(0, vmHeapSize.length() - 1));
    }

    public boolean isLowRamDevice() {
        return isLowRamDeviceStatic();
    }

    public static boolean isLowRamDeviceStatic() {
        return RoSystemProperties.CONFIG_LOW_RAM || (Build.IS_DEBUGGABLE && DEVELOPMENT_FORCE_LOW_RAM);
    }

    public static boolean isSmallBatteryDevice() {
        return RoSystemProperties.CONFIG_SMALL_BATTERY;
    }

    public static boolean isHighEndGfx() {
        return !isLowRamDeviceStatic() && !RoSystemProperties.CONFIG_AVOID_GFX_ACCEL && !Resources.getSystem().getBoolean(R.bool.config_avoidGfxAccel);
    }

    public long getTotalRam() {
        MemInfoReader memreader = new MemInfoReader();
        memreader.readMemInfo();
        return memreader.getTotalSize();
    }

    public static int getMaxRecentTasksStatic() {
        if (gMaxRecentTasks >= 0) {
            return gMaxRecentTasks;
        }
        int i = isLowRamDeviceStatic() ? 36 : 48;
        gMaxRecentTasks = i;
        return i;
    }

    public static int getDefaultAppRecentsLimitStatic() {
        return getMaxRecentTasksStatic() / 6;
    }

    public static int getMaxAppRecentsLimitStatic() {
        return getMaxRecentTasksStatic() / 2;
    }

    public static boolean supportsMultiWindow(Context context) {
        return (!isLowRamDeviceStatic() || context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_WATCH)) && Resources.getSystem().getBoolean(R.bool.config_supportsMultiWindow);
    }

    public static boolean supportsSplitScreenMultiWindow(Context context) {
        return supportsMultiWindow(context) && Resources.getSystem().getBoolean(R.bool.config_supportsSplitScreenMultiWindow);
    }

    @Deprecated
    public static int getMaxNumPictureInPictureActions() {
        return 3;
    }

    @Deprecated
    public List<RecentTaskInfo> getRecentTasks(int maxNum, int flags) throws SecurityException {
        if (maxNum >= 0) {
            try {
                return getService().getRecentTasks(maxNum, flags, this.mContext.getUserId()).getList();
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        } else {
            throw new IllegalArgumentException("The requested number of tasks should be >= 0");
        }
    }

    public List<AppTask> getAppTasks() {
        ArrayList<AppTask> tasks = new ArrayList<>();
        try {
            List<IBinder> appTasks = getService().getAppTasks(this.mContext.getPackageName());
            int numAppTasks = appTasks.size();
            for (int i = 0; i < numAppTasks; i++) {
                tasks.add(new AppTask(IAppTask.Stub.asInterface(appTasks.get(i))));
            }
            return tasks;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public Size getAppTaskThumbnailSize() {
        Size size;
        synchronized (this) {
            ensureAppTaskThumbnailSizeLocked();
            size = new Size(this.mAppTaskThumbnailSize.x, this.mAppTaskThumbnailSize.y);
        }
        return size;
    }

    private void ensureAppTaskThumbnailSizeLocked() {
        if (this.mAppTaskThumbnailSize == null) {
            try {
                this.mAppTaskThumbnailSize = getService().getAppTaskThumbnailSize();
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public int addAppTask(Activity activity, Intent intent, TaskDescription description, Bitmap thumbnail) {
        Point size;
        float scale;
        synchronized (this) {
            ensureAppTaskThumbnailSizeLocked();
            size = this.mAppTaskThumbnailSize;
        }
        int tw = thumbnail.getWidth();
        int th = thumbnail.getHeight();
        if (!(tw == size.x && th == size.y)) {
            Bitmap bm = Bitmap.createBitmap(size.x, size.y, thumbnail.getConfig());
            float dx = 0.0f;
            if (size.x * tw > size.y * th) {
                scale = ((float) size.x) / ((float) th);
                dx = (((float) size.y) - (((float) tw) * scale)) * 0.5f;
            } else {
                scale = ((float) size.y) / ((float) tw);
                float dy = (((float) size.x) - (((float) th) * scale)) * 0.5f;
            }
            Matrix matrix = new Matrix();
            matrix.setScale(scale, scale);
            matrix.postTranslate((float) ((int) (0.5f + dx)), 0.0f);
            Canvas canvas = new Canvas(bm);
            canvas.drawBitmap(thumbnail, matrix, null);
            canvas.setBitmap(null);
            thumbnail = bm;
        }
        if (description == null) {
            description = new TaskDescription();
        }
        try {
            return getService().addAppTask(activity.getActivityToken(), intent, description, thumbnail);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Deprecated
    public List<RunningTaskInfo> getRunningTasks(int maxNum) throws SecurityException {
        try {
            return getService().getTasks(maxNum);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setTaskWindowingMode(int taskId, int windowingMode, boolean toTop) throws SecurityException {
        try {
            getService().setTaskWindowingMode(taskId, windowingMode, toTop);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setTaskWindowingModeSplitScreenPrimary(int taskId, int createMode, boolean toTop, boolean animate, Rect initialBounds, boolean showRecents) throws SecurityException {
        try {
            getService().setTaskWindowingModeSplitScreenPrimary(taskId, createMode, toTop, animate, initialBounds, showRecents);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void resizeStack(int stackId, Rect bounds) throws SecurityException {
        try {
            getService().resizeStack(stackId, bounds, false, false, false, -1);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void removeStacksInWindowingModes(int[] windowingModes) throws SecurityException {
        try {
            getService().removeStacksInWindowingModes(windowingModes);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void removeStacksWithActivityTypes(int[] activityTypes) throws SecurityException {
        try {
            getService().removeStacksWithActivityTypes(activityTypes);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void moveTaskToFront(int taskId, int flags) {
        moveTaskToFront(taskId, flags, null);
    }

    public void moveTaskToFront(int taskId, int flags, Bundle options) {
        try {
            getService().moveTaskToFront(taskId, flags, options);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Deprecated
    public List<RunningServiceInfo> getRunningServices(int maxNum) throws SecurityException {
        try {
            return getService().getServices(maxNum, 0);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public PendingIntent getRunningServiceControlPanel(ComponentName service) throws SecurityException {
        try {
            return getService().getRunningServiceControlPanel(service);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void getMemoryInfo(MemoryInfo outInfo) {
        try {
            getService().getMemoryInfo(outInfo);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean clearApplicationUserData(String packageName, IPackageDataObserver observer) {
        try {
            return getService().clearApplicationUserData(packageName, false, observer, this.mContext.getUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean clearApplicationUserData() {
        return clearApplicationUserData(this.mContext.getPackageName(), null);
    }

    public ParceledListSlice<GrantedUriPermission> getGrantedUriPermissions(String packageName) {
        try {
            return getService().getGrantedUriPermissions(packageName, this.mContext.getUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void clearGrantedUriPermissions(String packageName) {
        try {
            getService().clearGrantedUriPermissions(packageName, this.mContext.getUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public List<ProcessErrorStateInfo> getProcessesInErrorState() {
        try {
            return getService().getProcessesInErrorState();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public List<ApplicationInfo> getRunningExternalApplications() {
        try {
            return getService().getRunningExternalApplications();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isBackgroundRestricted() {
        try {
            return getService().isBackgroundRestricted(this.mContext.getOpPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean setProcessMemoryTrimLevel(String process, int userId, int level) {
        try {
            return getService().setProcessMemoryTrimLevel(process, userId, level);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public List<RunningAppProcessInfo> getRunningAppProcesses() {
        try {
            return getService().getRunningAppProcesses();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @SystemApi
    public int getPackageImportance(String packageName) {
        try {
            return RunningAppProcessInfo.procStateToImportanceForClient(getService().getPackageProcessState(packageName, this.mContext.getOpPackageName()), this.mContext);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @SystemApi
    public int getUidImportance(int uid) {
        try {
            return RunningAppProcessInfo.procStateToImportanceForClient(getService().getUidProcessState(uid, this.mContext.getOpPackageName()), this.mContext);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @SystemApi
    public void addOnUidImportanceListener(OnUidImportanceListener listener, int importanceCutpoint) {
        synchronized (this) {
            if (!this.mImportanceListeners.containsKey(listener)) {
                UidObserver observer = new UidObserver(listener, this.mContext);
                try {
                    getService().registerUidObserver(observer, 3, RunningAppProcessInfo.importanceToProcState(importanceCutpoint), this.mContext.getOpPackageName());
                    this.mImportanceListeners.put(listener, observer);
                } catch (RemoteException e) {
                    throw e.rethrowFromSystemServer();
                }
            } else {
                throw new IllegalArgumentException("Listener already registered: " + listener);
            }
        }
    }

    @SystemApi
    public void removeOnUidImportanceListener(OnUidImportanceListener listener) {
        synchronized (this) {
            UidObserver observer = this.mImportanceListeners.remove(listener);
            if (observer != null) {
                try {
                    getService().unregisterUidObserver(observer);
                } catch (RemoteException e) {
                    throw e.rethrowFromSystemServer();
                }
            } else {
                throw new IllegalArgumentException("Listener not registered: " + listener);
            }
        }
    }

    public static void getMyMemoryState(RunningAppProcessInfo outState) {
        try {
            getService().getMyMemoryState(outState);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public Debug.MemoryInfo[] getProcessMemoryInfo(int[] pids) {
        try {
            return getService().getProcessMemoryInfo(pids);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Deprecated
    public void restartPackage(String packageName) {
        killBackgroundProcesses(packageName);
    }

    public void killBackgroundProcesses(String packageName) {
        try {
            getService().killBackgroundProcesses(packageName, this.mContext.getUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @SystemApi
    public void killUid(int uid, String reason) {
        try {
            getService().killUid(UserHandle.getAppId(uid), UserHandle.getUserId(uid), reason);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void forceStopPackageAsUser(String packageName, int userId) {
        try {
            getService().forceStopPackage(packageName, userId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @SystemApi
    public void forceStopPackage(String packageName) {
        forceStopPackageAsUser(packageName, this.mContext.getUserId());
    }

    public ConfigurationInfo getDeviceConfigurationInfo() {
        try {
            return getService().getDeviceConfigurationInfo();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int getLauncherLargeIconDensity() {
        Resources res = this.mContext.getResources();
        int density = res.getDisplayMetrics().densityDpi;
        if (res.getConfiguration().smallestScreenWidthDp < 600) {
            return density;
        }
        if (density == 120) {
            return 160;
        }
        if (density == 160) {
            return 240;
        }
        if (density == 213 || density == 240) {
            return 320;
        }
        if (density == 320) {
            return 480;
        }
        if (density != 480) {
            return (int) ((((float) density) * 1.5f) + 0.5f);
        }
        return 640;
    }

    public int getLauncherLargeIconSize() {
        return getLauncherLargeIconSizeInner(this.mContext);
    }

    static int getLauncherLargeIconSizeInner(Context context) {
        Resources res = context.getResources();
        int size = res.getDimensionPixelSize(17104896);
        if (res.getConfiguration().smallestScreenWidthDp < 600) {
            return size;
        }
        int density = res.getDisplayMetrics().densityDpi;
        if (density == 120) {
            return (size * 160) / 120;
        }
        if (density == 160) {
            return (size * 240) / 160;
        }
        if (density == 213) {
            return (size * 320) / 240;
        }
        if (density == 240) {
            return (size * 320) / 240;
        }
        if (density == 320) {
            return (size * 480) / 320;
        }
        if (density != 480) {
            return (int) ((((float) size) * 1.5f) + 0.5f);
        }
        return ((size * 320) * 2) / 480;
    }

    public static boolean isUserAMonkey() {
        try {
            return getService().isUserAMonkey();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public static boolean isRunningInTestHarness() {
        return SystemProperties.getBoolean("ro.test_harness", false);
    }

    public void alwaysShowUnsupportedCompileSdkWarning(ComponentName activity) {
        try {
            getService().alwaysShowUnsupportedCompileSdkWarning(activity);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public static int checkComponentPermission(String permission, int uid, int owningUid, boolean exported) {
        int appId = UserHandle.getAppId(uid);
        if (appId == 0 || appId == 1000) {
            return 0;
        }
        if (UserHandle.isIsolated(uid)) {
            return -1;
        }
        if (owningUid >= 0 && UserHandle.isSameApp(uid, owningUid)) {
            return 0;
        }
        if (!exported) {
            return -1;
        }
        if (permission == null) {
            return 0;
        }
        try {
            return AppGlobals.getPackageManager().checkUidPermission(permission, uid);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public static int checkUidPermission(String permission, int uid) {
        try {
            return AppGlobals.getPackageManager().checkUidPermission(permission, uid);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public static int handleIncomingUser(int callingPid, int callingUid, int userId, boolean allowAll, boolean requireFull, String name, String callerPackage) {
        if (UserHandle.getUserId(callingUid) == userId) {
            return userId;
        }
        try {
            return getService().handleIncomingUser(callingPid, callingUid, userId, allowAll, requireFull, name, callerPackage);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @SystemApi
    public static int getCurrentUser() {
        try {
            UserInfo ui = getService().getCurrentUser();
            if (ui != null) {
                return ui.id;
            }
            return 0;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean switchUser(int userid) {
        try {
            return getService().switchUser(userid);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public static void logoutCurrentUser() {
        int currentUser = getCurrentUser();
        if (currentUser != 0) {
            try {
                getService().switchUser(0);
                getService().stopUser(currentUser, false, null);
            } catch (RemoteException e) {
                e.rethrowFromSystemServer();
            }
        }
    }

    public boolean isUserRunning(int userId) {
        try {
            return getService().isUserRunning(userId, 0);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isVrModePackageEnabled(ComponentName component) {
        try {
            return getService().isVrModePackageEnabled(component);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void dumpPackageState(FileDescriptor fd, String packageName) {
        dumpPackageStateStatic(fd, packageName);
    }

    public static void dumpPackageStateStatic(FileDescriptor fd, String packageName) {
        PrintWriter pw = new FastPrintWriter(new FileOutputStream(fd));
        dumpService(pw, fd, "package", new String[]{packageName});
        pw.println();
        dumpService(pw, fd, Context.ACTIVITY_SERVICE, new String[]{"-a", "package", packageName});
        pw.println();
        dumpService(pw, fd, "meminfo", new String[]{"--local", "--package", packageName});
        pw.println();
        dumpService(pw, fd, "procstats", new String[]{packageName});
        pw.println();
        dumpService(pw, fd, Context.USAGE_STATS_SERVICE, new String[]{packageName});
        pw.println();
        dumpService(pw, fd, "batterystats", new String[]{packageName});
        pw.flush();
    }

    public static boolean isSystemReady() {
        if (!sSystemReady) {
            if (ActivityThread.isSystem()) {
                sSystemReady = ((ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class)).isSystemReady();
            } else {
                sSystemReady = true;
            }
        }
        return sSystemReady;
    }

    public static void broadcastStickyIntent(Intent intent, int userId) {
        broadcastStickyIntent(intent, -1, userId);
    }

    public static void broadcastStickyIntent(Intent intent, int appOp, int userId) {
        try {
            getService().broadcastIntent(null, intent, null, null, -1, null, null, null, appOp, null, false, true, userId);
        } catch (RemoteException e) {
            Log.e(TAG, "broadcastStickyIntent()");
        }
    }

    public static void noteWakeupAlarm(PendingIntent ps, WorkSource workSource, int sourceUid, String sourcePkg, String tag) {
        try {
            getService().noteWakeupAlarm(ps != null ? ps.getTarget() : null, workSource, sourceUid, sourcePkg, tag);
        } catch (RemoteException e) {
            Log.e(TAG, "noteWakeupAlarm()");
        }
    }

    public static void noteAlarmStart(PendingIntent ps, WorkSource workSource, int sourceUid, String tag) {
        try {
            getService().noteAlarmStart(ps != null ? ps.getTarget() : null, workSource, sourceUid, tag);
        } catch (RemoteException e) {
            Log.e(TAG, "noteAlarmStart()");
        }
    }

    public static void noteAlarmFinish(PendingIntent ps, WorkSource workSource, int sourceUid, String tag) {
        try {
            getService().noteAlarmFinish(ps != null ? ps.getTarget() : null, workSource, sourceUid, tag);
        } catch (RemoteException ex) {
            String str = TAG;
            Log.e(str, "noteAlarmFinish fail for " + ex.getMessage());
        }
    }

    public static IActivityManager getService() {
        return (IActivityManager) IActivityManagerSingleton.get();
    }

    private static void dumpService(PrintWriter pw, FileDescriptor fd, String name, String[] args) {
        pw.print("DUMP OF SERVICE ");
        pw.print(name);
        pw.println(":");
        IBinder service = ServiceManager.checkService(name);
        if (service == null) {
            pw.println("  (Service not found)");
            pw.flush();
            return;
        }
        pw.flush();
        if (service instanceof Binder) {
            try {
                service.dump(fd, args);
            } catch (Throwable e) {
                pw.println("Failure dumping service:");
                e.printStackTrace(pw);
                pw.flush();
            }
        } else {
            TransferPipe tp = null;
            try {
                pw.flush();
                tp = new TransferPipe();
                tp.setBufferPrefix("  ");
                service.dumpAsync(tp.getWriteFd().getFileDescriptor(), args);
                tp.go(fd, JobInfo.MIN_BACKOFF_MILLIS);
            } catch (Throwable e2) {
                if (tp != null) {
                    tp.kill();
                }
                pw.println("Failure dumping service:");
                e2.printStackTrace(pw);
            }
        }
    }

    public void setWatchHeapLimit(long pssSize) {
        try {
            getService().setDumpHeapDebugLimit(null, 0, pssSize, this.mContext.getPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void clearWatchHeapLimit() {
        try {
            getService().setDumpHeapDebugLimit(null, 0, 0, null);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Deprecated
    public boolean isInLockTaskMode() {
        return getLockTaskModeState() != 0;
    }

    public int getLockTaskModeState() {
        try {
            return getService().getLockTaskModeState();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public static void setVrThread(int tid) {
        try {
            getService().setVrThread(tid);
        } catch (RemoteException e) {
        }
    }

    public static void setPersistentVrThread(int tid) {
        try {
            getService().setPersistentVrThread(tid);
        } catch (RemoteException e) {
            Log.e(TAG, "setPersistentVrThread()");
        }
    }
}
