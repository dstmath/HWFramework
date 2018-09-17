package android.app;

import android.R;
import android.app.IActivityManager.Stub;
import android.app.job.JobInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.IIntentSender;
import android.content.Intent;
import android.content.UriPermission;
import android.content.pm.ApplicationInfo;
import android.content.pm.ConfigurationInfo;
import android.content.pm.IPackageDataObserver;
import android.content.pm.PackageManager;
import android.content.pm.ParceledListSlice;
import android.content.pm.UserInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.GraphicBuffer;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.camera2.params.TonemapCurve;
import android.net.ProxyInfo;
import android.os.BatteryStats;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.HwPCUtils;
import android.util.Log;
import android.util.Singleton;
import android.util.Size;
import com.android.internal.os.RoSystemProperties;
import com.android.internal.os.TransferPipe;
import com.android.internal.util.FastPrintWriter;
import com.android.server.LocalServices;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
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
    public static final int COMPAT_MODE_ALWAYS = -1;
    public static final int COMPAT_MODE_DISABLED = 0;
    public static final int COMPAT_MODE_ENABLED = 1;
    public static final int COMPAT_MODE_NEVER = -2;
    public static final int COMPAT_MODE_TOGGLE = 2;
    public static final int COMPAT_MODE_UNKNOWN = -3;
    public static final int DOCKED_STACK_CREATE_MODE_BOTTOM_OR_RIGHT = 1;
    public static final int DOCKED_STACK_CREATE_MODE_TOP_OR_LEFT = 0;
    public static final boolean ENABLE_TASK_SNAPSHOTS = SystemProperties.getBoolean("persist.enable_task_snapshots", false);
    private static final int FIRST_START_FATAL_ERROR_CODE = -100;
    private static final int FIRST_START_NON_FATAL_ERROR_CODE = 100;
    private static final int FIRST_START_SUCCESS_CODE = 0;
    public static final int FLAG_AND_LOCKED = 2;
    public static final int FLAG_AND_UNLOCKED = 4;
    public static final int FLAG_AND_UNLOCKING_OR_UNLOCKED = 8;
    public static final int FLAG_OR_STOPPED = 1;
    private static final Singleton<IActivityManager> IActivityManagerSingleton = new Singleton<IActivityManager>() {
        protected IActivityManager create() {
            return Stub.asInterface(ServiceManager.getService(Context.ACTIVITY_SERVICE));
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
    public static final int MAX_PROCESS_STATE = 18;
    public static final String META_HOME_ALTERNATE = "android.app.home.alternate";
    public static final int MIN_PROCESS_STATE = 0;
    public static final int MOVE_TASK_NO_USER_ACTION = 2;
    public static final int MOVE_TASK_WITH_HOME = 1;
    public static final int PROCESS_STATE_BACKUP = 9;
    public static final int PROCESS_STATE_BOUND_FOREGROUND_SERVICE = 3;
    public static final int PROCESS_STATE_CACHED_ACTIVITY = 15;
    public static final int PROCESS_STATE_CACHED_ACTIVITY_CLIENT = 16;
    public static final int PROCESS_STATE_CACHED_EMPTY = 17;
    public static final int PROCESS_STATE_FOREGROUND_SERVICE = 4;
    public static final int PROCESS_STATE_HEAVY_WEIGHT = 10;
    public static final int PROCESS_STATE_HOME = 13;
    public static final int PROCESS_STATE_IMPORTANT_BACKGROUND = 7;
    public static final int PROCESS_STATE_IMPORTANT_FOREGROUND = 6;
    public static final int PROCESS_STATE_LAST_ACTIVITY = 14;
    public static final int PROCESS_STATE_NONEXISTENT = 18;
    public static final int PROCESS_STATE_PERSISTENT = 0;
    public static final int PROCESS_STATE_PERSISTENT_UI = 1;
    public static final int PROCESS_STATE_RECEIVER = 12;
    public static final int PROCESS_STATE_SERVICE = 11;
    public static final int PROCESS_STATE_TOP = 2;
    public static final int PROCESS_STATE_TOP_SLEEPING = 5;
    public static final int PROCESS_STATE_TRANSIENT_BACKGROUND = 8;
    public static final int PROCESS_STATE_UNKNOWN = -1;
    public static final int RECENT_IGNORE_HOME_AND_RECENTS_STACK_TASKS = 8;
    public static final int RECENT_IGNORE_UNAVAILABLE = 2;
    public static final int RECENT_INCLUDE_PROFILES = 4;
    public static final int RECENT_INGORE_DOCKED_STACK_TOP_TASK = 16;
    public static final int RECENT_INGORE_PINNED_STACK_TASKS = 32;
    public static final int RECENT_WITH_EXCLUDED = 1;
    public static final int RESIZE_MODE_FORCED = 2;
    public static final int RESIZE_MODE_PRESERVE_WINDOW = 1;
    public static final int RESIZE_MODE_SYSTEM = 0;
    public static final int RESIZE_MODE_SYSTEM_SCREEN_ROTATION = 1;
    public static final int RESIZE_MODE_USER = 1;
    public static final int RESIZE_MODE_USER_FORCED = 3;
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
    private static String TAG = "ActivityManager";
    public static final int UID_OBSERVER_ACTIVE = 8;
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
    final ArrayMap<OnUidImportanceListener, UidObserver> mImportanceListeners = new ArrayMap();

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

    public static class MemoryInfo implements Parcelable {
        public static final Creator<MemoryInfo> CREATOR = new Creator<MemoryInfo>() {
            public MemoryInfo createFromParcel(Parcel source) {
                return new MemoryInfo(source, null);
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

        /* synthetic */ MemoryInfo(Parcel source, MemoryInfo -this1) {
            this(source);
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
            boolean z = false;
            this.availMem = source.readLong();
            this.totalMem = source.readLong();
            this.threshold = source.readLong();
            if (source.readInt() != 0) {
                z = true;
            }
            this.lowMemory = z;
            this.hiddenAppThreshold = source.readLong();
            this.secondaryServerThreshold = source.readLong();
            this.visibleAppThreshold = source.readLong();
            this.foregroundAppThreshold = source.readLong();
        }

        private MemoryInfo(Parcel source) {
            readFromParcel(source);
        }
    }

    public interface OnUidImportanceListener {
        void onUidImportance(int i, int i2);
    }

    public static class ProcessErrorStateInfo implements Parcelable {
        public static final int CRASHED = 1;
        public static final Creator<ProcessErrorStateInfo> CREATOR = new Creator<ProcessErrorStateInfo>() {
            public ProcessErrorStateInfo createFromParcel(Parcel source) {
                return new ProcessErrorStateInfo(source, null);
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

        /* synthetic */ ProcessErrorStateInfo(Parcel source, ProcessErrorStateInfo -this1) {
            this(source);
        }

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
        public static final Creator<RecentTaskInfo> CREATOR = new Creator<RecentTaskInfo>() {
            public RecentTaskInfo createFromParcel(Parcel source) {
                return new RecentTaskInfo(source, null);
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

        /* synthetic */ RecentTaskInfo(Parcel source, RecentTaskInfo -this1) {
            this(source);
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            int i = 1;
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
            dest.writeLong(this.firstActiveTime);
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
            if (!this.supportsSplitScreenMultiWindow) {
                i = 0;
            }
            dest.writeInt(i);
            dest.writeInt(this.resizeMode);
        }

        public void readFromParcel(Parcel source) {
            TaskDescription taskDescription;
            boolean z;
            this.id = source.readInt();
            this.persistentId = source.readInt();
            this.baseIntent = source.readInt() > 0 ? (Intent) Intent.CREATOR.createFromParcel(source) : null;
            this.origActivity = ComponentName.readFromParcel(source);
            this.realActivity = ComponentName.readFromParcel(source);
            this.description = (CharSequence) TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(source);
            if (source.readInt() > 0) {
                taskDescription = (TaskDescription) TaskDescription.CREATOR.createFromParcel(source);
            } else {
                taskDescription = null;
            }
            this.taskDescription = taskDescription;
            this.stackId = source.readInt();
            this.userId = source.readInt();
            this.firstActiveTime = source.readLong();
            this.lastActiveTime = source.readLong();
            this.affiliatedTaskId = source.readInt();
            this.affiliatedTaskColor = source.readInt();
            this.baseActivity = ComponentName.readFromParcel(source);
            this.topActivity = ComponentName.readFromParcel(source);
            this.numActivities = source.readInt();
            this.bounds = source.readInt() > 0 ? (Rect) Rect.CREATOR.createFromParcel(source) : null;
            if (source.readInt() == 1) {
                z = true;
            } else {
                z = false;
            }
            this.supportsSplitScreenMultiWindow = z;
            this.resizeMode = source.readInt();
        }

        private RecentTaskInfo(Parcel source) {
            readFromParcel(source);
        }
    }

    public static class RunningAppProcessInfo implements Parcelable {
        public static final Creator<RunningAppProcessInfo> CREATOR = new Creator<RunningAppProcessInfo>() {
            public RunningAppProcessInfo createFromParcel(Parcel source) {
                return new RunningAppProcessInfo(source, null);
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
        public static final int IMPORTANCE_CANT_SAVE_STATE = 270;
        public static final int IMPORTANCE_CANT_SAVE_STATE_PRE_26 = 170;
        @Deprecated
        public static final int IMPORTANCE_EMPTY = 500;
        public static final int IMPORTANCE_FOREGROUND = 100;
        public static final int IMPORTANCE_FOREGROUND_SERVICE = 125;
        public static final int IMPORTANCE_GONE = 1000;
        public static final int IMPORTANCE_PERCEPTIBLE = 230;
        public static final int IMPORTANCE_PERCEPTIBLE_PRE_26 = 130;
        public static final int IMPORTANCE_SERVICE = 300;
        public static final int IMPORTANCE_TOP_SLEEPING = 150;
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

        /* synthetic */ RunningAppProcessInfo(Parcel source, RunningAppProcessInfo -this1) {
            this(source);
        }

        public static int procStateToImportance(int procState) {
            if (procState == 18) {
                return 1000;
            }
            if (procState >= 13) {
                return 400;
            }
            if (procState >= 11) {
                return 300;
            }
            if (procState > 10) {
                return 270;
            }
            if (procState >= 8) {
                return 230;
            }
            if (procState >= 6) {
                return 200;
            }
            if (procState >= 5) {
                return 150;
            }
            if (procState >= 4) {
                return 125;
            }
            return 100;
        }

        public static int procStateToImportanceForClient(int procState, Context clientContext) {
            return procStateToImportanceForTargetSdk(procState, clientContext.getApplicationInfo().targetSdkVersion);
        }

        public static int procStateToImportanceForTargetSdk(int procState, int targetSdkVersion) {
            int importance = procStateToImportance(procState);
            if (targetSdkVersion < 26) {
                switch (importance) {
                    case 230:
                        return 130;
                    case 270:
                        return 170;
                }
            }
            return importance;
        }

        public static int importanceToProcState(int importance) {
            if (importance == 1000) {
                return 18;
            }
            if (importance >= 400) {
                return 13;
            }
            if (importance >= 300) {
                return 11;
            }
            if (importance > 270) {
                return 10;
            }
            if (importance >= 230) {
                return 8;
            }
            if (importance >= 200) {
                return 6;
            }
            if (importance >= 150) {
                return 5;
            }
            if (importance >= 125) {
                return 4;
            }
            return 3;
        }

        public RunningAppProcessInfo() {
            this.importance = 100;
            this.importanceReasonCode = 0;
            this.processState = 6;
        }

        public RunningAppProcessInfo(String pProcessName, int pPid, String[] pArr) {
            this.processName = pProcessName;
            this.pid = pPid;
            this.pkgList = pArr;
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
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
        public static final Creator<RunningServiceInfo> CREATOR = new Creator<RunningServiceInfo>() {
            public RunningServiceInfo createFromParcel(Parcel source) {
                return new RunningServiceInfo(source, null);
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

        /* synthetic */ RunningServiceInfo(Parcel source, RunningServiceInfo -this1) {
            this(source);
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            int i = 1;
            ComponentName.writeToParcel(this.service, dest);
            dest.writeInt(this.pid);
            dest.writeInt(this.uid);
            dest.writeString(this.process);
            dest.writeInt(this.foreground ? 1 : 0);
            dest.writeLong(this.activeSince);
            if (!this.started) {
                i = 0;
            }
            dest.writeInt(i);
            dest.writeInt(this.clientCount);
            dest.writeInt(this.crashCount);
            dest.writeLong(this.lastActivityTime);
            dest.writeLong(this.restarting);
            dest.writeInt(this.flags);
            dest.writeString(this.clientPackage);
            dest.writeInt(this.clientLabel);
        }

        public void readFromParcel(Parcel source) {
            boolean z = true;
            this.service = ComponentName.readFromParcel(source);
            this.pid = source.readInt();
            this.uid = source.readInt();
            this.process = source.readString();
            this.foreground = source.readInt() != 0;
            this.activeSince = source.readLong();
            if (source.readInt() == 0) {
                z = false;
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
        public static final Creator<RunningTaskInfo> CREATOR = new Creator<RunningTaskInfo>() {
            public RunningTaskInfo createFromParcel(Parcel source) {
                return new RunningTaskInfo(source, null);
            }

            public RunningTaskInfo[] newArray(int size) {
                return new RunningTaskInfo[size];
            }
        };
        public ComponentName baseActivity;
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

        /* synthetic */ RunningTaskInfo(Parcel source, RunningTaskInfo -this1) {
            this(source);
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            int i = 1;
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
            if (!this.supportsSplitScreenMultiWindow) {
                i = 0;
            }
            dest.writeInt(i);
            dest.writeInt(this.resizeMode);
        }

        public void readFromParcel(Parcel source) {
            boolean z;
            this.id = source.readInt();
            this.stackId = source.readInt();
            this.baseActivity = ComponentName.readFromParcel(source);
            this.topActivity = ComponentName.readFromParcel(source);
            if (source.readInt() != 0) {
                this.thumbnail = (Bitmap) Bitmap.CREATOR.createFromParcel(source);
            } else {
                this.thumbnail = null;
            }
            this.description = (CharSequence) TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(source);
            this.numActivities = source.readInt();
            this.numRunning = source.readInt();
            if (source.readInt() != 0) {
                z = true;
            } else {
                z = false;
            }
            this.supportsSplitScreenMultiWindow = z;
            this.resizeMode = source.readInt();
        }

        private RunningTaskInfo(Parcel source) {
            readFromParcel(source);
        }
    }

    public static class StackId {
        public static final int ASSISTANT_STACK_ID = 6;
        public static final int DOCKED_STACK_ID = 3;
        public static final int FIRST_DYNAMIC_STACK_ID = 7;
        public static final int FIRST_STATIC_STACK_ID = 0;
        public static final int FREEFORM_WORKSPACE_STACK_ID = 2;
        public static final int FULLSCREEN_WORKSPACE_STACK_ID = 1;
        public static final int HOME_STACK_ID = 0;
        public static final int INVALID_STACK_ID = -1;
        public static final int LAST_STATIC_STACK_ID = 6;
        public static final int PINNED_STACK_ID = 4;
        public static final int RECENTS_STACK_ID = 5;

        public static boolean isStaticStack(int stackId) {
            return stackId >= 0 && stackId <= 6;
        }

        public static boolean isDynamicStack(int stackId) {
            return stackId >= 7;
        }

        public static boolean hasWindowShadow(int stackId) {
            boolean z = true;
            if (HwPCUtils.isPcDynamicStack(stackId)) {
                return true;
            }
            if (!(stackId == 2 || stackId == 4)) {
                z = false;
            }
            return z;
        }

        public static boolean hasWindowDecor(int stackId) {
            boolean z = true;
            if (HwPCUtils.isPcDynamicStack(stackId)) {
                return true;
            }
            if (stackId != 2) {
                z = false;
            }
            return z;
        }

        public static boolean isTaskResizeAllowed(int stackId) {
            boolean z = true;
            if (HwPCUtils.isPcDynamicStack(stackId)) {
                return true;
            }
            if (stackId != 2) {
                z = false;
            }
            return z;
        }

        public static boolean persistTaskBounds(int stackId) {
            boolean z = true;
            if (HwPCUtils.isPcDynamicStack(stackId)) {
                return true;
            }
            if (stackId != 2) {
                z = false;
            }
            return z;
        }

        public static boolean isDynamicStacksVisibleBehindAllowed(int stackId) {
            return stackId == 4 || stackId == 6;
        }

        public static boolean keepFocusInStackIfPossible(int stackId) {
            boolean z = true;
            if (HwPCUtils.isPcDynamicStack(stackId)) {
                return true;
            }
            if (!(stackId == 2 || stackId == 3 || stackId == 4)) {
                z = false;
            }
            return z;
        }

        public static boolean isResizeableByDockedStack(int stackId) {
            if (!isStaticStack(stackId) || stackId == 3 || stackId == 4 || stackId == 6) {
                return false;
            }
            return true;
        }

        public static boolean isTaskResizeableByDockedStack(int stackId) {
            if (!isStaticStack(stackId) || stackId == 2 || stackId == 3 || stackId == 4 || stackId == 6) {
                return false;
            }
            return true;
        }

        public static boolean isStackAffectedByDragResizing(int stackId) {
            if (!isStaticStack(stackId) || stackId == 4 || stackId == 6) {
                return false;
            }
            return true;
        }

        public static boolean replaceWindowsOnTaskMove(int sourceStackId, int targetStackId) {
            if (sourceStackId == 2 || targetStackId == 2) {
                return true;
            }
            return false;
        }

        public static boolean tasksAreFloating(int stackId) {
            boolean z = true;
            if (HwPCUtils.isPcDynamicStack(stackId)) {
                return true;
            }
            if (!(stackId == 2 || stackId == 4)) {
                z = false;
            }
            return z;
        }

        public static boolean isBackdropToTranslucentActivity(int stackId) {
            boolean z = true;
            if (HwPCUtils.isPcDynamicStack(stackId)) {
                return true;
            }
            if (!(stackId == 1 || stackId == 6)) {
                z = false;
            }
            return z;
        }

        public static boolean useAnimationSpecForAppTransition(int stackId) {
            if (stackId == 2 || stackId == 1 || stackId == 6 || stackId == 3 || stackId == -1) {
                return true;
            }
            return false;
        }

        public static boolean canReceiveKeys(int stackId) {
            return stackId != 4;
        }

        public static boolean isAllowedOverLockscreen(int stackId) {
            if (stackId == 0 || stackId == 1 || stackId == 6) {
                return true;
            }
            return false;
        }

        public static boolean isAllowedToEnterPictureInPicture(int stackId) {
            if (stackId == 0 || stackId == 6 || stackId == 5) {
                return false;
            }
            return true;
        }

        public static boolean isAlwaysOnTop(int stackId) {
            return stackId == 4;
        }

        public static boolean allowTopTaskToReturnHome(int stackId) {
            return stackId != 4;
        }

        public static boolean resizeStackWithLaunchBounds(int stackId) {
            return stackId == 4;
        }

        public static boolean keepVisibleDeadAppWindowOnScreen(int stackId) {
            return stackId != 4;
        }

        public static boolean useWindowFrameForBackdrop(int stackId) {
            boolean z = true;
            if (HwPCUtils.isPcDynamicStack(stackId)) {
                return true;
            }
            if (!(stackId == 2 || stackId == 4)) {
                z = false;
            }
            return z;
        }

        public static boolean normallyFullscreenWindows(int stackId) {
            if (stackId == 4 || stackId == 2 || stackId == 3) {
                return false;
            }
            return true;
        }

        public static boolean isMultiWindowStack(int stackId) {
            boolean z = true;
            if (HwPCUtils.isPcDynamicStack(stackId)) {
                return true;
            }
            if (!(stackId == 4 || stackId == 2 || stackId == 3)) {
                z = false;
            }
            return z;
        }

        public static boolean isHomeOrRecentsStack(int stackId) {
            return stackId == 0 || stackId == 5;
        }

        @Deprecated
        public static boolean activitiesCanRequestVisibleBehind(int stackId) {
            if (stackId == 1 || stackId == 6) {
                return true;
            }
            return false;
        }

        public static boolean windowsAreScaleable(int stackId) {
            return stackId == 4;
        }

        public static boolean hasMovementAnimations(int stackId) {
            return stackId != 4;
        }

        public static boolean canSpecifyOrientation(int stackId) {
            if (stackId == 0 || stackId == 5 || stackId == 1 || stackId == 6) {
                return true;
            }
            return isDynamicStack(stackId);
        }
    }

    public static class StackInfo implements Parcelable {
        public static final Creator<StackInfo> CREATOR = new Creator<StackInfo>() {
            public StackInfo createFromParcel(Parcel source) {
                return new StackInfo(source, null);
            }

            public StackInfo[] newArray(int size) {
                return new StackInfo[size];
            }
        };
        public Rect bounds;
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

        /* synthetic */ StackInfo(Parcel source, StackInfo -this1) {
            this(source);
        }

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
                return;
            }
            dest.writeInt(0);
        }

        public void readFromParcel(Parcel source) {
            boolean z = false;
            this.stackId = source.readInt();
            this.bounds = new Rect(source.readInt(), source.readInt(), source.readInt(), source.readInt());
            this.taskIds = source.createIntArray();
            this.taskNames = source.createStringArray();
            int boundsCount = source.readInt();
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
        }

        public StackInfo() {
            this.bounds = new Rect();
        }

        private StackInfo(Parcel source) {
            this.bounds = new Rect();
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
            prefix = prefix + "  ";
            for (int i = 0; i < this.taskIds.length; i++) {
                sb.append(prefix);
                sb.append("taskId=");
                sb.append(this.taskIds[i]);
                sb.append(": ");
                sb.append(this.taskNames[i]);
                if (this.taskBounds != null) {
                    sb.append(" bounds=");
                    sb.append(this.taskBounds[i].toShortString());
                }
                sb.append(" userId=").append(this.taskUserIds[i]);
                sb.append(" visible=").append(this.visible);
                if (this.topActivity != null) {
                    sb.append(" topActivity=").append(this.topActivity);
                }
                sb.append("\n");
            }
            return sb.toString();
        }

        public String toString() {
            return toString(ProxyInfo.LOCAL_EXCL_LIST);
        }
    }

    public static class TaskDescription implements Parcelable {
        private static final String ATTR_TASKDESCRIPTIONCOLOR_BACKGROUND = "task_description_colorBackground";
        private static final String ATTR_TASKDESCRIPTIONCOLOR_PRIMARY = "task_description_color";
        private static final String ATTR_TASKDESCRIPTIONICONFILENAME = "task_description_icon_filename";
        private static final String ATTR_TASKDESCRIPTIONLABEL = "task_description_label";
        public static final String ATTR_TASKDESCRIPTION_PREFIX = "task_description_";
        public static final Creator<TaskDescription> CREATOR = new Creator<TaskDescription>() {
            public TaskDescription createFromParcel(Parcel source) {
                return new TaskDescription(source, null);
            }

            public TaskDescription[] newArray(int size) {
                return new TaskDescription[size];
            }
        };
        private int mColorBackground;
        private int mColorPrimary;
        private Bitmap mIcon;
        private String mIconFilename;
        private String mLabel;
        private int mNavigationBarColor;
        private int mStatusBarColor;

        public TaskDescription(String label, Bitmap icon, int colorPrimary) {
            this(label, icon, null, colorPrimary, 0, 0, 0);
            if (colorPrimary != 0 && Color.alpha(colorPrimary) != 255) {
                throw new RuntimeException("A TaskDescription's primary color should be opaque");
            }
        }

        public TaskDescription(String label, Bitmap icon) {
            this(label, icon, null, 0, 0, 0, 0);
        }

        public TaskDescription(String label) {
            this(label, null, null, 0, 0, 0, 0);
        }

        public TaskDescription() {
            this(null, null, null, 0, 0, 0, 0);
        }

        public TaskDescription(String label, Bitmap icon, String iconFilename, int colorPrimary, int colorBackground, int statusBarColor, int navigationBarColor) {
            this.mLabel = label;
            this.mIcon = icon;
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
            this.mIconFilename = other.mIconFilename;
            this.mColorPrimary = other.mColorPrimary;
            this.mColorBackground = other.mColorBackground;
            this.mStatusBarColor = other.mStatusBarColor;
            this.mNavigationBarColor = other.mNavigationBarColor;
        }

        public void copyFromPreserveHiddenFields(TaskDescription other) {
            this.mLabel = other.mLabel;
            this.mIcon = other.mIcon;
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
                out.attribute(null, ATTR_TASKDESCRIPTIONICONFILENAME, this.mIconFilename);
            }
        }

        public void restoreFromXml(String attrName, String attrValue) {
            if (ATTR_TASKDESCRIPTIONLABEL.equals(attrName)) {
                setLabel(attrValue);
            } else if (ATTR_TASKDESCRIPTIONCOLOR_PRIMARY.equals(attrName)) {
                setPrimaryColor((int) Long.parseLong(attrValue, 16));
            } else if (ATTR_TASKDESCRIPTIONCOLOR_BACKGROUND.equals(attrName)) {
                setBackgroundColor((int) Long.parseLong(attrValue, 16));
            } else if (ATTR_TASKDESCRIPTIONICONFILENAME.equals(attrName)) {
                setIconFilename(attrValue);
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
            Bitmap bitmap;
            String str = null;
            this.mLabel = source.readInt() > 0 ? source.readString() : null;
            if (source.readInt() > 0) {
                bitmap = (Bitmap) Bitmap.CREATOR.createFromParcel(source);
            } else {
                bitmap = null;
            }
            this.mIcon = bitmap;
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
            return "TaskDescription Label: " + this.mLabel + " Icon: " + this.mIcon + " IconFilename: " + this.mIconFilename + " colorPrimary: " + this.mColorPrimary + " colorBackground: " + this.mColorBackground + " statusBarColor: " + this.mColorBackground + " navigationBarColor: " + this.mNavigationBarColor;
        }
    }

    public static class TaskSnapshot implements Parcelable {
        public static final Creator<TaskSnapshot> CREATOR = new Creator<TaskSnapshot>() {
            public TaskSnapshot createFromParcel(Parcel source) {
                return new TaskSnapshot(source, null);
            }

            public TaskSnapshot[] newArray(int size) {
                return new TaskSnapshot[size];
            }
        };
        private final Rect mContentInsets;
        private final int mOrientation;
        private final boolean mReducedResolution;
        private final float mScale;
        private final GraphicBuffer mSnapshot;

        /* synthetic */ TaskSnapshot(Parcel source, TaskSnapshot -this1) {
            this(source);
        }

        public TaskSnapshot(GraphicBuffer snapshot, int orientation, Rect contentInsets, boolean reducedResolution, float scale) {
            this.mSnapshot = snapshot;
            this.mOrientation = orientation;
            this.mContentInsets = new Rect(contentInsets);
            this.mReducedResolution = reducedResolution;
            this.mScale = scale;
        }

        private TaskSnapshot(Parcel source) {
            this.mSnapshot = (GraphicBuffer) source.readParcelable(null);
            this.mOrientation = source.readInt();
            this.mContentInsets = (Rect) source.readParcelable(null);
            this.mReducedResolution = source.readBoolean();
            this.mScale = source.readFloat();
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
        }

        public String toString() {
            return "TaskSnapshot{mSnapshot=" + this.mSnapshot + " mOrientation=" + this.mOrientation + " mContentInsets=" + this.mContentInsets.toShortString() + " mReducedResolution=" + this.mReducedResolution + " mScale=" + this.mScale;
        }
    }

    public static class TaskThumbnail implements Parcelable {
        public static final Creator<TaskThumbnail> CREATOR = new Creator<TaskThumbnail>() {
            public TaskThumbnail createFromParcel(Parcel source) {
                return new TaskThumbnail(source, null);
            }

            public TaskThumbnail[] newArray(int size) {
                return new TaskThumbnail[size];
            }
        };
        public Bitmap mainThumbnail;
        public ParcelFileDescriptor thumbnailFileDescriptor;
        public TaskThumbnailInfo thumbnailInfo;

        /* synthetic */ TaskThumbnail(Parcel source, TaskThumbnail -this1) {
            this(source);
        }

        private TaskThumbnail(Parcel source) {
            readFromParcel(source);
        }

        public int describeContents() {
            if (this.thumbnailFileDescriptor != null) {
                return this.thumbnailFileDescriptor.describeContents();
            }
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            if (this.mainThumbnail == null || (this.mainThumbnail.isRecycled() ^ 1) == 0) {
                dest.writeInt(0);
            } else {
                dest.writeInt(1);
                this.mainThumbnail.writeToParcel(dest, flags);
            }
            if (this.thumbnailFileDescriptor != null) {
                dest.writeInt(1);
                this.thumbnailFileDescriptor.writeToParcel(dest, flags);
            } else {
                dest.writeInt(0);
            }
            if (this.thumbnailInfo != null) {
                dest.writeInt(1);
                this.thumbnailInfo.writeToParcel(dest, flags);
                return;
            }
            dest.writeInt(0);
        }

        public void readFromParcel(Parcel source) {
            if (source.readInt() != 0) {
                this.mainThumbnail = (Bitmap) Bitmap.CREATOR.createFromParcel(source);
            } else {
                this.mainThumbnail = null;
            }
            if (source.readInt() != 0) {
                this.thumbnailFileDescriptor = (ParcelFileDescriptor) ParcelFileDescriptor.CREATOR.createFromParcel(source);
            } else {
                this.thumbnailFileDescriptor = null;
            }
            if (source.readInt() != 0) {
                this.thumbnailInfo = (TaskThumbnailInfo) TaskThumbnailInfo.CREATOR.createFromParcel(source);
            } else {
                this.thumbnailInfo = null;
            }
        }
    }

    public static class TaskThumbnailInfo implements Parcelable {
        private static final String ATTR_SCREEN_ORIENTATION = "task_thumbnailinfo_screen_orientation";
        private static final String ATTR_TASK_HEIGHT = "task_thumbnailinfo_task_height";
        public static final String ATTR_TASK_THUMBNAILINFO_PREFIX = "task_thumbnailinfo_";
        private static final String ATTR_TASK_WIDTH = "task_thumbnailinfo_task_width";
        public static final Creator<TaskThumbnailInfo> CREATOR = new Creator<TaskThumbnailInfo>() {
            public TaskThumbnailInfo createFromParcel(Parcel source) {
                return new TaskThumbnailInfo(source, null);
            }

            public TaskThumbnailInfo[] newArray(int size) {
                return new TaskThumbnailInfo[size];
            }
        };
        public int screenOrientation;
        public int taskHeight;
        public int taskWidth;

        /* synthetic */ TaskThumbnailInfo(Parcel source, TaskThumbnailInfo -this1) {
            this(source);
        }

        public TaskThumbnailInfo() {
            this.screenOrientation = 0;
        }

        private TaskThumbnailInfo(Parcel source) {
            this.screenOrientation = 0;
            readFromParcel(source);
        }

        public void reset() {
            this.taskWidth = 0;
            this.taskHeight = 0;
            this.screenOrientation = 0;
        }

        public void copyFrom(TaskThumbnailInfo o) {
            this.taskWidth = o.taskWidth;
            this.taskHeight = o.taskHeight;
            this.screenOrientation = o.screenOrientation;
        }

        public void saveToXml(XmlSerializer out) throws IOException {
            out.attribute(null, ATTR_TASK_WIDTH, Integer.toString(this.taskWidth));
            out.attribute(null, ATTR_TASK_HEIGHT, Integer.toString(this.taskHeight));
            out.attribute(null, ATTR_SCREEN_ORIENTATION, Integer.toString(this.screenOrientation));
        }

        public void restoreFromXml(String attrName, String attrValue) {
            if (ATTR_TASK_WIDTH.equals(attrName)) {
                this.taskWidth = Integer.parseInt(attrValue);
            } else if (ATTR_TASK_HEIGHT.equals(attrName)) {
                this.taskHeight = Integer.parseInt(attrValue);
            } else if (ATTR_SCREEN_ORIENTATION.equals(attrName)) {
                this.screenOrientation = Integer.parseInt(attrValue);
            }
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.taskWidth);
            dest.writeInt(this.taskHeight);
            dest.writeInt(this.screenOrientation);
        }

        public void readFromParcel(Parcel source) {
            this.taskWidth = source.readInt();
            this.taskHeight = source.readInt();
            this.screenOrientation = source.readInt();
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
    }

    public static final boolean isProcStateBackground(int procState) {
        return procState >= 8;
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
        String vmHeapSize = SystemProperties.get("dalvik.vm.heapgrowthlimit", ProxyInfo.LOCAL_EXCL_LIST);
        if (vmHeapSize == null || (ProxyInfo.LOCAL_EXCL_LIST.equals(vmHeapSize) ^ 1) == 0) {
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
        return RoSystemProperties.CONFIG_LOW_RAM;
    }

    public static boolean isHighEndGfx() {
        if (isLowRamDeviceStatic()) {
            return false;
        }
        return Resources.getSystem().getBoolean(17956895) ^ 1;
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
        boolean isWatch = context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_WATCH);
        if (!isLowRamDeviceStatic() || isWatch) {
            return Resources.getSystem().getBoolean(17957025);
        }
        return false;
    }

    public static boolean supportsSplitScreenMultiWindow(Context context) {
        if (supportsMultiWindow(context)) {
            return Resources.getSystem().getBoolean(17957026);
        }
        return false;
    }

    @Deprecated
    public static int getMaxNumPictureInPictureActions() {
        return 3;
    }

    @Deprecated
    public List<RecentTaskInfo> getRecentTasks(int maxNum, int flags) throws SecurityException {
        try {
            return getService().getRecentTasks(maxNum, flags, UserHandle.myUserId()).getList();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public List<RecentTaskInfo> getRecentTasksForUser(int maxNum, int flags, int userId) throws SecurityException {
        try {
            return getService().getRecentTasks(maxNum, flags, userId).getList();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public List<AppTask> getAppTasks() {
        ArrayList<AppTask> tasks = new ArrayList();
        try {
            List<IBinder> appTasks = getService().getAppTasks(this.mContext.getPackageName());
            int numAppTasks = appTasks.size();
            for (int i = 0; i < numAppTasks; i++) {
                tasks.add(new AppTask(IAppTask.Stub.asInterface((IBinder) appTasks.get(i))));
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
        synchronized (this) {
            ensureAppTaskThumbnailSizeLocked();
            size = this.mAppTaskThumbnailSize;
        }
        int tw = thumbnail.getWidth();
        int th = thumbnail.getHeight();
        if (!(tw == size.x && th == size.y)) {
            float scale;
            Bitmap bm = Bitmap.createBitmap(size.x, size.y, thumbnail.getConfig());
            float dx = TonemapCurve.LEVEL_BLACK;
            if (size.x * tw > size.y * th) {
                scale = ((float) size.x) / ((float) th);
                dx = (((float) size.y) - (((float) tw) * scale)) * 0.5f;
            } else {
                scale = ((float) size.y) / ((float) tw);
                float dy = (((float) size.x) - (((float) th) * scale)) * 0.5f;
            }
            Matrix matrix = new Matrix();
            matrix.setScale(scale, scale);
            matrix.postTranslate((float) ((int) (0.5f + dx)), TonemapCurve.LEVEL_BLACK);
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
            return getService().getTasks(maxNum, 0);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean removeTask(int taskId) throws SecurityException {
        try {
            return getService().removeTask(taskId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public TaskThumbnail getTaskThumbnail(int id) throws SecurityException {
        try {
            return getService().getTaskThumbnail(id);
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
            return getService().clearApplicationUserData(packageName, observer, UserHandle.myUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean clearApplicationUserData() {
        return clearApplicationUserData(this.mContext.getPackageName(), null);
    }

    public ParceledListSlice<UriPermission> getGrantedUriPermissions(String packageName) {
        try {
            return getService().getGrantedUriPermissions(packageName, UserHandle.myUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void clearGrantedUriPermissions(String packageName) {
        try {
            getService().clearGrantedUriPermissions(packageName, UserHandle.myUserId());
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

    public int getPackageImportance(String packageName) {
        try {
            return RunningAppProcessInfo.procStateToImportanceForClient(getService().getPackageProcessState(packageName, this.mContext.getOpPackageName()), this.mContext);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int getUidImportance(int uid) {
        try {
            return RunningAppProcessInfo.procStateToImportanceForClient(getService().getUidProcessState(uid, this.mContext.getOpPackageName()), this.mContext);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void addOnUidImportanceListener(OnUidImportanceListener listener, int importanceCutpoint) {
        synchronized (this) {
            if (this.mImportanceListeners.containsKey(listener)) {
                throw new IllegalArgumentException("Listener already registered: " + listener);
            }
            UidObserver observer = new UidObserver(listener, this.mContext);
            try {
                getService().registerUidObserver(observer, 3, RunningAppProcessInfo.importanceToProcState(importanceCutpoint), this.mContext.getOpPackageName());
                this.mImportanceListeners.put(listener, observer);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void removeOnUidImportanceListener(OnUidImportanceListener listener) {
        synchronized (this) {
            UidObserver observer = (UidObserver) this.mImportanceListeners.remove(listener);
            if (observer == null) {
                throw new IllegalArgumentException("Listener not registered: " + listener);
            }
            try {
                getService().unregisterUidObserver(observer);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
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

    public android.os.Debug.MemoryInfo[] getProcessMemoryInfo(int[] pids) {
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
            getService().killBackgroundProcesses(packageName, UserHandle.myUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

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

    public void forceStopPackage(String packageName) {
        forceStopPackageAsUser(packageName, UserHandle.myUserId());
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
        switch (density) {
            case 120:
                return 160;
            case 160:
                return 240;
            case 213:
                return 320;
            case 240:
                return 320;
            case 320:
                return 480;
            case 480:
                return 640;
            default:
                return (int) ((((float) density) * 1.5f) + 0.5f);
        }
    }

    public int getLauncherLargeIconSize() {
        return getLauncherLargeIconSizeInner(this.mContext);
    }

    static int getLauncherLargeIconSizeInner(Context context) {
        Resources res = context.getResources();
        int size = res.getDimensionPixelSize(R.dimen.app_icon_size);
        if (res.getConfiguration().smallestScreenWidthDp < 600) {
            return size;
        }
        switch (res.getDisplayMetrics().densityDpi) {
            case 120:
                return (size * 160) / 120;
            case 160:
                return (size * 240) / 160;
            case 213:
                return (size * 320) / 240;
            case 240:
                return (size * 320) / 240;
            case 320:
                return (size * 480) / 320;
            case 480:
                return ((size * 320) * 2) / 480;
            default:
                return (int) ((((float) size) * 1.5f) + 0.5f);
        }
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

    public static int getCurrentUser() {
        try {
            UserInfo ui = getService().getCurrentUser();
            return ui != null ? ui.id : 0;
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
        dumpService(pw, fd, Context.USAGE_STATS_SERVICE, new String[]{"--packages", packageName});
        pw.println();
        dumpService(pw, fd, BatteryStats.SERVICE_NAME, new String[]{packageName});
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

    public static void noteWakeupAlarm(PendingIntent ps, int sourceUid, String sourcePkg, String tag) {
        IIntentSender iIntentSender = null;
        try {
            IActivityManager service = getService();
            if (ps != null) {
                iIntentSender = ps.getTarget();
            }
            service.noteWakeupAlarm(iIntentSender, sourceUid, sourcePkg, tag);
        } catch (RemoteException e) {
            Log.e(TAG, "noteWakeupAlarm()");
        }
    }

    public static void noteAlarmStart(PendingIntent ps, int sourceUid, String tag) {
        IIntentSender iIntentSender = null;
        try {
            IActivityManager service = getService();
            if (ps != null) {
                iIntentSender = ps.getTarget();
            }
            service.noteAlarmStart(iIntentSender, sourceUid, tag);
        } catch (RemoteException e) {
            Log.e(TAG, "noteAlarmStart()");
        }
    }

    public static void noteAlarmFinish(PendingIntent ps, int sourceUid, String tag) {
        IIntentSender iIntentSender = null;
        try {
            IActivityManager service = getService();
            if (ps != null) {
                iIntentSender = ps.getTarget();
            }
            service.noteAlarmFinish(iIntentSender, sourceUid, tag);
        } catch (RemoteException ex) {
            Log.e(TAG, "noteAlarmFinish fail for " + ex.getMessage());
        }
    }

    public static IActivityManager getService() {
        return (IActivityManager) IActivityManagerSingleton.get();
    }

    /* JADX WARNING: Removed duplicated region for block: B:13:0x0040  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static void dumpService(PrintWriter pw, FileDescriptor fd, String name, String[] args) {
        Throwable e;
        pw.print("DUMP OF SERVICE ");
        pw.print(name);
        pw.println(":");
        IBinder service = ServiceManager.checkService(name);
        if (service == null) {
            pw.println("  (Service not found)");
            return;
        }
        TransferPipe tp = null;
        try {
            pw.flush();
            TransferPipe tp2 = new TransferPipe();
            try {
                tp2.setBufferPrefix("  ");
                service.dumpAsync(tp2.getWriteFd().getFileDescriptor(), args);
                tp2.go(fd, JobInfo.MIN_BACKOFF_MILLIS);
                tp = tp2;
            } catch (Throwable th) {
                e = th;
                tp = tp2;
                if (tp != null) {
                }
                pw.println("Failure dumping service:");
                e.printStackTrace(pw);
            }
        } catch (Throwable th2) {
            e = th2;
            if (tp != null) {
                tp.kill();
            }
            pw.println("Failure dumping service:");
            e.printStackTrace(pw);
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

    public void startLockTaskMode(int taskId) {
        try {
            getService().startLockTaskModeById(taskId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void stopLockTaskMode() {
        try {
            getService().stopLockTaskMode();
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
