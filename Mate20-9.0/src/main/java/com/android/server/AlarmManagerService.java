package com.android.server;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.AppOpsManager;
import android.app.BroadcastOptions;
import android.app.IAlarmCompleteListener;
import android.app.IAlarmListener;
import android.app.IAlarmManager;
import android.app.IUidObserver;
import android.app.PendingIntent;
import android.app.usage.UsageStatsManagerInternal;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.ParcelableException;
import android.os.PowerManager;
import android.os.Process;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.ShellCallback;
import android.os.ShellCommand;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Trace;
import android.os.UserHandle;
import android.os.WorkSource;
import android.provider.Settings;
import android.system.Os;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Flog;
import android.util.HwLog;
import android.util.KeyValueListParser;
import android.util.Log;
import android.util.NtpTrustedTime;
import android.util.Pair;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.util.SparseLongArray;
import android.util.TimeUtils;
import android.util.proto.ProtoOutputStream;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.DumpUtils;
import com.android.internal.util.LocalLog;
import com.android.internal.util.StatLogger;
import com.android.server.AlarmManagerService;
import com.android.server.AppStateTracker;
import com.android.server.DeviceIdleController;
import com.android.server.am.HwActivityManagerServiceUtil;
import com.android.server.backup.BackupAgentTimeoutParameters;
import com.android.server.job.controllers.JobStatus;
import com.android.server.pm.PackageManagerService;
import com.android.server.usage.AppStandbyController;
import com.android.server.utils.PriorityDump;
import com.huawei.pgmng.log.LogPower;
import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.annotation.RCUnownedRef;
import java.text.SimpleDateFormat;
import java.time.DateTimeException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;
import java.util.TreeSet;
import java.util.function.Predicate;

public class AlarmManagerService extends AbsAlarmManagerService {
    static final int ACTIVE_INDEX = 0;
    static final int ALARM_EVENT = 1;
    static final boolean DEBUG_ALARM_CLOCK = false;
    static final boolean DEBUG_BATCH = false;
    static final boolean DEBUG_BG_LIMIT = false;
    static final boolean DEBUG_LISTENER_CALLBACK = false;
    static final boolean DEBUG_STANDBY = false;
    static final boolean DEBUG_TIMEZONE = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
    static final boolean DEBUG_VALIDATE = false;
    static final boolean DEBUG_WAKELOCK = false;
    private static final int ELAPSED_REALTIME_MASK = 8;
    private static final int ELAPSED_REALTIME_WAKEUP_MASK = 4;
    static final int FREQUENT_INDEX = 2;
    static final int IS_WAKEUP_MASK = 5;
    static final long MIN_FUZZABLE_INTERVAL = 10000;
    static final int NEVER_INDEX = 4;
    private static final Intent NEXT_ALARM_CLOCK_CHANGED_INTENT = new Intent("android.app.action.NEXT_ALARM_CLOCK_CHANGED").addFlags(553648128);
    static final int PRIO_NORMAL = 2;
    static final int PRIO_TICK = 0;
    static final int PRIO_WAKEUP = 1;
    static final int RARE_INDEX = 3;
    static final boolean RECORD_ALARMS_IN_HISTORY = true;
    static final boolean RECORD_DEVICE_IDLE_ALARMS = false;
    private static final int RTC_MASK = 2;
    private static final int RTC_WAKEUP_MASK = 1;
    private static final String SYSTEM_UI_SELF_PERMISSION = "android.permission.systemui.IDENTITY";
    static final String TAG = "AlarmManager";
    static final String TIMEZONE_PROPERTY = "persist.sys.timezone";
    static final int TIME_CHANGED_MASK = 65536;
    static final int TYPE_NONWAKEUP_MASK = 1;
    static final boolean WAKEUP_STATS = false;
    static final int WORKING_INDEX = 1;
    static final boolean localLOGV = false;
    static final BatchTimeOrder sBatchOrder = new BatchTimeOrder();
    static final IncreasingTimeOrder sIncreasingTimeOrder = new IncreasingTimeOrder();
    final long RECENT_WAKEUP_PERIOD = 86400000;
    final ArrayList<Batch> mAlarmBatches = new ArrayList<>();
    final Comparator<Alarm> mAlarmDispatchComparator = new Comparator<Alarm>() {
        public int compare(Alarm lhs, Alarm rhs) {
            if (lhs.priorityClass.priority < rhs.priorityClass.priority) {
                return -1;
            }
            if (lhs.priorityClass.priority > rhs.priorityClass.priority) {
                return 1;
            }
            if (lhs.whenElapsed < rhs.whenElapsed) {
                return -1;
            }
            if (lhs.whenElapsed > rhs.whenElapsed) {
                return 1;
            }
            return 0;
        }
    };
    final ArrayList<IdleDispatchEntry> mAllowWhileIdleDispatches = new ArrayList<>();
    AppOpsManager mAppOps;
    /* access modifiers changed from: private */
    public boolean mAppStandbyParole;
    /* access modifiers changed from: private */
    public AppStateTracker mAppStateTracker;
    /* access modifiers changed from: private */
    public final Intent mBackgroundIntent = new Intent().addFlags(4);
    int mBroadcastRefCount = 0;
    final SparseArray<ArrayMap<String, BroadcastStats>> mBroadcastStats = new SparseArray<>();
    boolean mCancelRemoveAction = false;
    ClockReceiver mClockReceiver;
    final Constants mConstants = new Constants(this.mHandler);
    int mCurrentSeq = 0;
    PendingIntent mDateChangeSender;
    final DeliveryTracker mDeliveryTracker = new DeliveryTracker();
    private final AppStateTracker.Listener mForceAppStandbyListener = new AppStateTracker.Listener() {
        public void unblockAllUnrestrictedAlarms() {
            synchronized (AlarmManagerService.this.mLock) {
                AlarmManagerService.this.sendAllUnrestrictedPendingBackgroundAlarmsLocked();
            }
        }

        public void unblockAlarmsForUid(int uid) {
            synchronized (AlarmManagerService.this.mLock) {
                AlarmManagerService.this.sendPendingBackgroundAlarmsLocked(uid, null);
            }
        }

        public void unblockAlarmsForUidPackage(int uid, String packageName) {
            synchronized (AlarmManagerService.this.mLock) {
                AlarmManagerService.this.sendPendingBackgroundAlarmsLocked(uid, packageName);
            }
        }

        public void onUidForeground(int uid, boolean foreground) {
            synchronized (AlarmManagerService.this.mLock) {
                if (foreground) {
                    try {
                        AlarmManagerService.this.mUseAllowWhileIdleShortTime.put(uid, true);
                    } catch (Throwable th) {
                        throw th;
                    }
                }
            }
        }
    };
    final AlarmHandler mHandler = new AlarmHandler();
    private final SparseArray<AlarmManager.AlarmClockInfo> mHandlerSparseAlarmClockArray = new SparseArray<>();
    Bundle mIdleOptions;
    ArrayList<InFlight> mInFlight = new ArrayList<>();
    boolean mInteractive = true;
    InteractiveStateReceiver mInteractiveStateReceiver;
    private final boolean mIsAlarmDataOnlyMode = SystemProperties.getBoolean("persist.sys.alarm_data_only", false);
    /* access modifiers changed from: private */
    public boolean mIsScreenOn = true;
    /* access modifiers changed from: private */
    public ArrayMap<Pair<String, Integer>, Long> mLastAlarmDeliveredForPackage = new ArrayMap<>();
    long mLastAlarmDeliveryTime;
    final SparseLongArray mLastAllowWhileIdleDispatch = new SparseLongArray();
    /* access modifiers changed from: private */
    public long mLastTickAdded;
    /* access modifiers changed from: private */
    public long mLastTickIssued;
    /* access modifiers changed from: private */
    public long mLastTickReceived;
    /* access modifiers changed from: private */
    public long mLastTickRemoved;
    /* access modifiers changed from: private */
    public long mLastTickSet;
    long mLastTimeChangeClockTime;
    long mLastTimeChangeRealtime;
    /* access modifiers changed from: private */
    public long mLastTrigger;
    boolean mLastWakeLockUnimportantForLogging;
    /* access modifiers changed from: private */
    public long mLastWakeup;
    private long mLastWakeupSet;
    /* access modifiers changed from: private */
    @GuardedBy("mLock")
    public int mListenerCount = 0;
    /* access modifiers changed from: private */
    @GuardedBy("mLock")
    public int mListenerFinishCount = 0;
    DeviceIdleController.LocalService mLocalDeviceIdleController;
    protected Object mLock = new Object();
    final LocalLog mLog = new LocalLog(TAG);
    long mMaxDelayTime = 0;
    long mNativeData;
    private final SparseArray<AlarmManager.AlarmClockInfo> mNextAlarmClockForUser = new SparseArray<>();
    protected boolean mNextAlarmClockMayChange;
    private long mNextNonWakeup;
    long mNextNonWakeupDeliveryTime;
    Alarm mNextWakeFromIdle = null;
    private long mNextWakeup;
    long mNonInteractiveStartTime;
    long mNonInteractiveTime;
    int mNumDelayedAlarms = 0;
    int mNumTimeChanged;
    SparseArray<ArrayList<Alarm>> mPendingBackgroundAlarms = new SparseArray<>();
    Alarm mPendingIdleUntil = null;
    ArrayList<Alarm> mPendingNonWakeupAlarms = new ArrayList<>();
    private final SparseBooleanArray mPendingSendNextAlarmClockChangedForUser = new SparseBooleanArray();
    ArrayList<Alarm> mPendingWhileIdleAlarms = new ArrayList<>();
    final HashMap<String, PriorityClass> mPriorities = new HashMap<>();
    Random mRandom;
    final LinkedList<WakeupEvent> mRecentWakeups = new LinkedList<>();
    /* access modifiers changed from: private */
    @GuardedBy("mLock")
    public int mSendCount = 0;
    /* access modifiers changed from: private */
    @GuardedBy("mLock")
    public int mSendFinishCount = 0;
    /* access modifiers changed from: private */
    public final IBinder mService = new IAlarmManager.Stub() {
        public void set(String callingPackage, int type, long triggerAtTime, long windowLength, long interval, int flags, PendingIntent operation, IAlarmListener directReceiver, String listenerTag, WorkSource workSource, AlarmManager.AlarmClockInfo alarmClock) {
            int flags2;
            int flags3;
            WorkSource workSource2 = workSource;
            int callingUid = Binder.getCallingUid();
            AlarmManagerService.this.mAppOps.checkPackage(callingUid, callingPackage);
            if (interval == 0 || directReceiver == null) {
                if (workSource2 != null) {
                    AlarmManagerService.this.getContext().enforcePermission("android.permission.UPDATE_DEVICE_STATS", Binder.getCallingPid(), callingUid, "AlarmManager.set");
                }
                int flags4 = flags & -11;
                if (callingUid != 1000) {
                    flags4 &= -17;
                }
                if (windowLength == 0) {
                    flags4 |= 1;
                }
                if (alarmClock != null) {
                    flags3 = flags4 | 3;
                } else if ((workSource2 != null || (callingUid >= 10000 && !UserHandle.isSameApp(callingUid, AlarmManagerService.this.mSystemUiUid) && (AlarmManagerService.this.mAppStateTracker == null || !AlarmManagerService.this.mAppStateTracker.isUidPowerSaveUserWhitelisted(callingUid)))) && !AlarmManagerService.this.isContainsAppUidInWorksource(workSource2, "com.android.email") && !AlarmManagerService.this.isContainsAppUidInWorksource(workSource2, "com.android.exchange") && !AlarmManagerService.this.isContainsAppUidInWorksource(workSource2, "com.google.android.gm")) {
                    flags2 = flags4;
                    AlarmManagerService.this.setImpl(type, triggerAtTime, windowLength, interval, operation, directReceiver, listenerTag, flags2, workSource2, alarmClock, callingUid, callingPackage);
                    return;
                } else {
                    flags3 = (flags4 | 8) & -5;
                }
                flags2 = flags3;
                AlarmManagerService.this.setImpl(type, triggerAtTime, windowLength, interval, operation, directReceiver, listenerTag, flags2, workSource2, alarmClock, callingUid, callingPackage);
                return;
            }
            throw new IllegalArgumentException("Repeating alarms cannot use AlarmReceivers");
        }

        public boolean setTime(long millis) {
            AlarmManagerService.this.getContext().enforceCallingOrSelfPermission("android.permission.SET_TIME", "setTime");
            int uid = Binder.getCallingUid();
            int pid = Binder.getCallingPid();
            Flog.i(500, "setTime,uid is " + uid + ",pid is " + pid);
            return AlarmManagerService.this.setTimeImpl(millis);
        }

        public void setTimeZone(String tz) {
            AlarmManagerService.this.getContext().enforceCallingOrSelfPermission("android.permission.SET_TIME_ZONE", "setTimeZone");
            int uid = Binder.getCallingUid();
            int pid = Binder.getCallingPid();
            Flog.i(500, "setTimeZoneImpl,uid is " + uid + ",pid is " + pid + ",tz = " + tz);
            long oldId = Binder.clearCallingIdentity();
            try {
                AlarmManagerService.this.setTimeZoneImpl(tz);
            } finally {
                Binder.restoreCallingIdentity(oldId);
            }
        }

        public void remove(PendingIntent operation, IAlarmListener listener) {
            if (operation == null && listener == null) {
                Slog.w(AlarmManagerService.TAG, "remove() with no intent or listener");
                return;
            }
            synchronized (AlarmManagerService.this.mLock) {
                AlarmManagerService.this.removeLocked(operation, listener);
            }
        }

        public long getNextWakeFromIdleTime() {
            return AlarmManagerService.this.getNextWakeFromIdleTimeImpl();
        }

        public AlarmManager.AlarmClockInfo getNextAlarmClock(int userId) {
            return AlarmManagerService.this.getNextAlarmClockImpl(ActivityManager.handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), userId, false, false, "getNextAlarmClock", null));
        }

        public long currentNetworkTimeMillis() {
            NtpTrustedTime time = NtpTrustedTime.getInstance(AlarmManagerService.this.getContext());
            if (time.hasCache()) {
                return time.currentTimeMillis();
            }
            throw new ParcelableException(new DateTimeException("Missing NTP fix"));
        }

        /* access modifiers changed from: protected */
        public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            if (DumpUtils.checkDumpAndUsageStatsPermission(AlarmManagerService.this.getContext(), AlarmManagerService.TAG, pw)) {
                if (args.length <= 0 || !PriorityDump.PROTO_ARG.equals(args[0])) {
                    AlarmManagerService.this.dumpImpl(pw);
                } else {
                    AlarmManagerService.this.dumpProto(fd);
                }
            }
        }

        /* JADX WARNING: type inference failed for: r1v1, types: [android.os.Binder] */
        /* JADX WARNING: Multi-variable type inference failed */
        public void onShellCommand(FileDescriptor in, FileDescriptor out, FileDescriptor err, String[] args, ShellCallback callback, ResultReceiver resultReceiver) {
            new ShellCmd().exec(this, in, out, err, args, callback, resultReceiver);
        }

        public void updateBlockedUids(int uid, boolean isBlocked) {
        }

        public int getWakeUpNum(int uid, String pkg) {
            if (1000 == Binder.getCallingUid()) {
                return AlarmManagerService.this.getWakeUpNumImpl(uid, pkg);
            }
            Slog.i(AlarmManagerService.TAG, "getWakeUpNum: permission not allowed.");
            return 0;
        }

        public long checkHasHwRTCAlarm(String packageName) {
            AlarmManagerService.this.getContext().enforceCallingOrSelfPermission("android.permission.SET_TIME", "checkHasHwRTCAlarm");
            return AlarmManagerService.this.checkHasHwRTCAlarmLock(packageName);
        }

        public void adjustHwRTCAlarm(boolean deskClockTime, boolean bootOnTime, int typeState) {
            AlarmManagerService.this.getContext().enforceCallingOrSelfPermission("android.permission.SET_TIME", "adjustHwRTCAlarm");
            Slog.i(AlarmManagerService.TAG, "adjustHwRTCAlarm : adjust RTC alarm");
            AlarmManagerService.this.adjustHwRTCAlarmLock(deskClockTime, bootOnTime, typeState);
        }

        public void setHwAirPlaneStateProp() {
            AlarmManagerService.this.getContext().enforceCallingOrSelfPermission("android.permission.SET_TIME", "setHwAirPlaneStateProp");
            Slog.i(AlarmManagerService.TAG, "setHwAirPlaneStateProp : set Prop Lock");
            AlarmManagerService.this.setHwAirPlaneStatePropLock();
        }

        public void setHwRTCAlarm() {
            AlarmManagerService.this.getContext().enforceCallingOrSelfPermission("android.permission.SET_TIME", "setHwRTCAlarm");
            Slog.i(AlarmManagerService.TAG, "setHwRTCAlarm : set RTC alarm");
            AlarmManagerService.this.setHwRTCAlarmLock();
        }
    };
    long mStartCurrentDelayTime;
    private final StatLogger mStatLogger = new StatLogger(new String[]{"REBATCH_ALL_ALARMS", "REORDER_ALARMS_FOR_STANDBY"});
    int mSystemUiUid;
    PendingIntent mTimeTickSender;
    private final SparseArray<AlarmManager.AlarmClockInfo> mTmpSparseAlarmClockArray = new SparseArray<>();
    long mTotalDelayTime = 0;
    private UninstallReceiver mUninstallReceiver;
    private UsageStatsManagerInternal mUsageStatsManagerInternal;
    final SparseBooleanArray mUseAllowWhileIdleShortTime = new SparseBooleanArray();
    PowerManager.WakeLock mWakeLock;

    @VisibleForTesting
    public static class Alarm {
        public final AlarmManager.AlarmClockInfo alarmClock;
        public int count;
        public final int creatorUid;
        public long expectedMaxWhenElapsed;
        public long expectedWhenElapsed;
        public final int flags;
        public final IAlarmListener listener;
        public final String listenerTag;
        public long maxWhenElapsed;
        public final PendingIntent operation;
        public final long origWhen;
        public final String packageName;
        public PriorityClass priorityClass;
        public long repeatInterval;
        public final String sourcePackage;
        public final String statsTag;
        public final int type;
        public final int uid;
        public boolean wakeup;
        public long when;
        public long whenElapsed;
        public long windowLength;
        public final WorkSource workSource;

        public Alarm(int _type, long _when, long _whenElapsed, long _windowLength, long _maxWhen, long _interval, PendingIntent _op, IAlarmListener _rec, String _listenerTag, WorkSource _ws, int _flags, AlarmManager.AlarmClockInfo _info, int _uid, String _pkgName) {
            int i = _type;
            long j = _when;
            long j2 = _whenElapsed;
            PendingIntent pendingIntent = _op;
            String str = _listenerTag;
            this.type = i;
            this.origWhen = j;
            this.wakeup = i == 2 || i == 0;
            this.when = j;
            this.whenElapsed = j2;
            this.expectedWhenElapsed = j2;
            this.windowLength = _windowLength;
            long clampPositive = AlarmManagerService.clampPositive(_maxWhen);
            this.expectedMaxWhenElapsed = clampPositive;
            this.maxWhenElapsed = clampPositive;
            this.repeatInterval = _interval;
            this.operation = pendingIntent;
            this.listener = _rec;
            this.listenerTag = str;
            this.statsTag = makeTag(pendingIntent, str, i);
            this.workSource = _ws;
            this.flags = _flags;
            this.alarmClock = _info;
            this.uid = _uid;
            this.packageName = _pkgName;
            this.sourcePackage = this.operation != null ? this.operation.getCreatorPackage() : this.packageName;
            this.creatorUid = this.operation != null ? this.operation.getCreatorUid() : this.uid;
        }

        public static String makeTag(PendingIntent pi, String tag, int type2) {
            String alarmString = (type2 == 2 || type2 == 0) ? "*walarm*:" : "*alarm*:";
            if (pi != null) {
                return pi.getTag(alarmString);
            }
            return alarmString + tag;
        }

        public WakeupEvent makeWakeupEvent(long nowRTC) {
            String str;
            int i = this.creatorUid;
            if (this.operation != null) {
                str = AlarmManagerService.resetActionCallingIdentity(this.operation);
            } else {
                str = "<listener>:" + this.listenerTag;
            }
            return new WakeupEvent(nowRTC, i, str);
        }

        public boolean matches(PendingIntent pi, IAlarmListener rec) {
            if (this.operation != null) {
                return this.operation.equals(pi);
            }
            return rec != null && this.listener.asBinder().equals(rec.asBinder());
        }

        public boolean matches(String packageName2) {
            return packageName2.equals(this.sourcePackage);
        }

        public String toString() {
            StringBuilder sb = new StringBuilder(128);
            sb.append("Alarm{");
            sb.append(Integer.toHexString(System.identityHashCode(this)));
            sb.append(" type ");
            sb.append(this.type);
            sb.append(" when ");
            sb.append(this.when);
            sb.append(" ");
            if (this.operation != null) {
                sb.append(this.operation);
            } else {
                sb.append(this.packageName);
            }
            sb.append(this.sourcePackage);
            sb.append('}');
            return sb.toString();
        }

        public void dump(PrintWriter pw, String prefix, long nowELAPSED, long nowRTC, SimpleDateFormat sdf) {
            boolean z = true;
            if (!(this.type == 1 || this.type == 0)) {
                z = false;
            }
            boolean isRtc = z;
            pw.print(prefix);
            pw.print("tag=");
            pw.println(this.statsTag);
            pw.print(prefix);
            pw.print("type=");
            pw.print(this.type);
            pw.print(prefix);
            pw.print("wakeup=");
            pw.print(this.wakeup);
            pw.print(" expectedWhenElapsed=");
            TimeUtils.formatDuration(this.expectedWhenElapsed, nowELAPSED, pw);
            pw.print(" expectedMaxWhenElapsed=");
            TimeUtils.formatDuration(this.expectedMaxWhenElapsed, nowELAPSED, pw);
            pw.print(" whenElapsed=");
            TimeUtils.formatDuration(this.whenElapsed, nowELAPSED, pw);
            pw.print(" maxWhenElapsed=");
            TimeUtils.formatDuration(this.maxWhenElapsed, nowELAPSED, pw);
            pw.print(" when=");
            if (isRtc) {
                pw.print(sdf.format(new Date(this.when)));
            } else {
                TimeUtils.formatDuration(this.when, nowELAPSED, pw);
            }
            pw.println();
            pw.print(prefix);
            pw.print("window=");
            TimeUtils.formatDuration(this.windowLength, pw);
            pw.print(" repeatInterval=");
            pw.print(this.repeatInterval);
            pw.print(" count=");
            pw.print(this.count);
            pw.print(" flags=0x");
            pw.println(Integer.toHexString(this.flags));
            if (this.alarmClock != null) {
                pw.print(prefix);
                pw.println("Alarm clock:");
                pw.print(prefix);
                pw.print("  triggerTime=");
                pw.println(sdf.format(new Date(this.alarmClock.getTriggerTime())));
                pw.print(prefix);
                pw.print("  showIntent=");
                pw.println(this.alarmClock.getShowIntent());
            }
            pw.print(prefix);
            pw.print("operation=");
            pw.println(this.operation);
            if (this.listener != null) {
                pw.print(prefix);
                pw.print("listener=");
                pw.println(this.listener.asBinder());
            }
        }

        public void writeToProto(ProtoOutputStream proto, long fieldId, long nowElapsed, long nowRTC) {
            long token = proto.start(fieldId);
            proto.write(1138166333441L, this.statsTag);
            proto.write(1159641169922L, this.type);
            proto.write(1112396529667L, this.whenElapsed - nowElapsed);
            proto.write(1112396529668L, this.windowLength);
            proto.write(1112396529669L, this.repeatInterval);
            proto.write(1120986464262L, this.count);
            proto.write(1120986464263L, this.flags);
            if (this.alarmClock != null) {
                this.alarmClock.writeToProto(proto, 1146756268040L);
            }
            if (this.operation != null) {
                this.operation.writeToProto(proto, 1146756268041L);
            }
            if (this.listener != null) {
                proto.write(1138166333450L, this.listener.asBinder().toString());
            }
            proto.end(token);
        }
    }

    public class AlarmHandler extends Handler {
        public static final int ALARM_EVENT = 1;
        public static final int APP_STANDBY_BUCKET_CHANGED = 5;
        public static final int APP_STANDBY_PAROLE_CHANGED = 6;
        public static final int LISTENER_TIMEOUT = 3;
        public static final int REMOVE_FOR_STOPPED = 7;
        public static final int REPORT_ALARMS_ACTIVE = 4;
        public static final int SEND_NEXT_ALARM_CLOCK_CHANGED = 2;

        public AlarmHandler() {
        }

        public void postRemoveForStopped(int uid) {
            obtainMessage(7, uid, 0).sendToTarget();
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v6, resolved type: boolean} */
        /* JADX WARNING: type inference failed for: r1v0 */
        /* JADX WARNING: type inference failed for: r1v2, types: [int] */
        /* JADX WARNING: type inference failed for: r1v7 */
        /* JADX WARNING: type inference failed for: r1v18 */
        /* JADX WARNING: Multi-variable type inference failed */
        public void handleMessage(Message msg) {
            ? r1 = 0;
            switch (msg.what) {
                case 1:
                    ArrayList<Alarm> triggerList = new ArrayList<>();
                    synchronized (AlarmManagerService.this.mLock) {
                        long nowRTC = System.currentTimeMillis();
                        AlarmManagerService.this.triggerAlarmsLocked(triggerList, SystemClock.elapsedRealtime(), nowRTC);
                        AlarmManagerService.this.updateNextAlarmClockLocked();
                    }
                    while (r1 < triggerList.size()) {
                        Alarm alarm = triggerList.get(r1);
                        try {
                            if (alarm.operation != null) {
                                alarm.operation.send();
                            }
                        } catch (PendingIntent.CanceledException e) {
                            if (alarm.repeatInterval > 0) {
                                AlarmManagerService.this.removeImpl(alarm.operation);
                            }
                        }
                        r1++;
                    }
                    return;
                case 2:
                    AlarmManagerService.this.sendNextAlarmClockChanged();
                    return;
                case 3:
                    AlarmManagerService.this.mDeliveryTracker.alarmTimedOut((IBinder) msg.obj);
                    return;
                case 4:
                    if (AlarmManagerService.this.mLocalDeviceIdleController != null) {
                        DeviceIdleController.LocalService localService = AlarmManagerService.this.mLocalDeviceIdleController;
                        if (msg.arg1 != 0) {
                            r1 = 1;
                        }
                        localService.setAlarmsActive(r1);
                        return;
                    }
                    return;
                case 5:
                    synchronized (AlarmManagerService.this.mLock) {
                        ArraySet<Pair<String, Integer>> filterPackages = new ArraySet<>();
                        filterPackages.add(Pair.create((String) msg.obj, Integer.valueOf(msg.arg1)));
                        if (AlarmManagerService.this.reorderAlarmsBasedOnStandbyBuckets(filterPackages)) {
                            AlarmManagerService.this.rescheduleKernelAlarmsLocked();
                            AlarmManagerService.this.updateNextAlarmClockLocked();
                        }
                    }
                    return;
                case 6:
                    synchronized (AlarmManagerService.this.mLock) {
                        boolean unused = AlarmManagerService.this.mAppStandbyParole = ((Boolean) msg.obj).booleanValue();
                        if (AlarmManagerService.this.reorderAlarmsBasedOnStandbyBuckets(null)) {
                            AlarmManagerService.this.rescheduleKernelAlarmsLocked();
                            AlarmManagerService.this.updateNextAlarmClockLocked();
                        }
                    }
                    return;
                case 7:
                    synchronized (AlarmManagerService.this.mLock) {
                        AlarmManagerService.this.removeForStoppedLocked(msg.arg1);
                    }
                    return;
                default:
                    return;
            }
        }
    }

    private class AlarmThread extends Thread {
        public AlarmThread() {
            super(AlarmManagerService.TAG);
        }

        /* JADX INFO: finally extract failed */
        /* JADX WARNING: Code restructure failed: missing block: B:96:0x0200, code lost:
            r0 = th;
         */
        public void run() {
            long lastTimeChangeClockTime;
            long expectedClockTime;
            ArrayList<Alarm> triggerList = new ArrayList<>();
            AlarmManagerService.this.hwRecordFirstTime();
            while (true) {
                int result = AlarmManagerService.this.waitForAlarm(AlarmManagerService.this.mNativeData);
                long nowRTC = System.currentTimeMillis();
                long nowELAPSED = SystemClock.elapsedRealtime();
                synchronized (AlarmManagerService.this.mLock) {
                    try {
                        long unused = AlarmManagerService.this.mLastWakeup = nowELAPSED;
                    } catch (Throwable th) {
                        th = th;
                        long j = nowELAPSED;
                        long j2 = nowRTC;
                        while (true) {
                            throw th;
                        }
                    }
                }
                triggerList.clear();
                if ((result & 65536) != 0) {
                    synchronized (AlarmManagerService.this.mLock) {
                        lastTimeChangeClockTime = AlarmManagerService.this.mLastTimeChangeClockTime;
                        expectedClockTime = lastTimeChangeClockTime + (nowELAPSED - AlarmManagerService.this.mLastTimeChangeRealtime);
                    }
                    if (lastTimeChangeClockTime == 0 || nowRTC < expectedClockTime - 1000 || nowRTC > expectedClockTime + 1000) {
                        Flog.i(500, "Time changed notification from kernel; rebatching");
                        AlarmManagerService.this.removeImpl(AlarmManagerService.this.mTimeTickSender);
                        AlarmManagerService.this.removeImpl(AlarmManagerService.this.mDateChangeSender);
                        AlarmManagerService.this.rebatchAllAlarms();
                        AlarmManagerService.this.mClockReceiver.scheduleTimeTickEvent();
                        AlarmManagerService.this.mClockReceiver.scheduleDateChangedEvent();
                        synchronized (AlarmManagerService.this.mLock) {
                            AlarmManagerService.this.mNumTimeChanged++;
                            AlarmManagerService.this.mLastTimeChangeClockTime = nowRTC;
                            AlarmManagerService.this.mLastTimeChangeRealtime = nowELAPSED;
                        }
                        Intent intent = new Intent("android.intent.action.TIME_SET");
                        intent.addFlags(891289600);
                        AlarmManagerService.this.getContext().sendBroadcastAsUser(intent, UserHandle.ALL);
                        AlarmManagerService.this.hwRecordTimeChangeRTC(nowRTC, nowELAPSED, lastTimeChangeClockTime, expectedClockTime);
                        result |= 5;
                    }
                }
                int result2 = result;
                if (result2 != 65536) {
                    synchronized (AlarmManagerService.this.mLock) {
                        try {
                            long unused2 = AlarmManagerService.this.mLastTrigger = nowELAPSED;
                            long nowELAPSED2 = nowELAPSED;
                            long j3 = nowRTC;
                            if (AlarmManagerService.this.triggerAlarmsLocked(triggerList, nowELAPSED, nowRTC) || !AlarmManagerService.this.checkAllowNonWakeupDelayLocked(nowELAPSED2)) {
                                if (AlarmManagerService.this.isAwareAlarmManagerEnabled() && (result2 & 5) != 0) {
                                    ArrayList<Alarm> wakeupList = new ArrayList<>();
                                    Iterator<Alarm> it = triggerList.iterator();
                                    while (it.hasNext()) {
                                        Alarm alarm = it.next();
                                        if (alarm.wakeup) {
                                            wakeupList.add(alarm);
                                        }
                                    }
                                    AlarmManagerService.this.reportWakeupAlarms(wakeupList);
                                }
                                if (AlarmManagerService.this.mPendingNonWakeupAlarms.size() > 0) {
                                    AlarmManagerService.this.calculateDeliveryPriorities(AlarmManagerService.this.mPendingNonWakeupAlarms);
                                    triggerList.addAll(AlarmManagerService.this.mPendingNonWakeupAlarms);
                                    Collections.sort(triggerList, AlarmManagerService.this.mAlarmDispatchComparator);
                                    long thisDelayTime = nowELAPSED2 - AlarmManagerService.this.mStartCurrentDelayTime;
                                    AlarmManagerService.this.mTotalDelayTime += thisDelayTime;
                                    if (AlarmManagerService.this.mMaxDelayTime < thisDelayTime) {
                                        AlarmManagerService.this.mMaxDelayTime = thisDelayTime;
                                    }
                                    AlarmManagerService.this.mPendingNonWakeupAlarms.clear();
                                }
                                ArraySet<Pair<String, Integer>> triggerPackages = new ArraySet<>();
                                for (int i = 0; i < triggerList.size(); i++) {
                                    Alarm a = triggerList.get(i);
                                    if (!AlarmManagerService.this.isExemptFromAppStandby(a)) {
                                        triggerPackages.add(Pair.create(a.sourcePackage, Integer.valueOf(UserHandle.getUserId(a.creatorUid))));
                                    }
                                }
                                AlarmManagerService.this.deliverAlarmsLocked(triggerList, nowELAPSED2);
                                AlarmManagerService.this.reorderAlarmsBasedOnStandbyBuckets(triggerPackages);
                                AlarmManagerService.this.rescheduleKernelAlarmsLocked();
                                AlarmManagerService.this.updateNextAlarmClockLocked();
                            } else {
                                Flog.i(500, "there are no wakeup alarms and the screen is off, we can delay what we have so far until the future");
                                if (AlarmManagerService.this.mPendingNonWakeupAlarms.size() == 0) {
                                    AlarmManagerService.this.mStartCurrentDelayTime = nowELAPSED2;
                                    AlarmManagerService.this.mNextNonWakeupDeliveryTime = ((AlarmManagerService.this.currentNonWakeupFuzzLocked(nowELAPSED2) * 3) / 2) + nowELAPSED2;
                                }
                                AlarmManagerService.this.mPendingNonWakeupAlarms.addAll(triggerList);
                                AlarmManagerService.this.mNumDelayedAlarms += triggerList.size();
                                AlarmManagerService.this.rescheduleKernelAlarmsLocked();
                                AlarmManagerService.this.updateNextAlarmClockLocked();
                            }
                        } catch (Throwable th2) {
                            th = th2;
                            throw th;
                        }
                    }
                } else {
                    long j4 = nowRTC;
                    synchronized (AlarmManagerService.this.mLock) {
                        AlarmManagerService.this.rescheduleKernelAlarmsLocked();
                    }
                }
            }
            while (true) {
            }
        }
    }

    final class AppStandbyTracker extends UsageStatsManagerInternal.AppIdleStateChangeListener {
        AppStandbyTracker() {
        }

        public void onAppIdleStateChanged(String packageName, int userId, boolean idle, int bucket, int reason) {
            AlarmManagerService.this.mHandler.removeMessages(5);
            AlarmManagerService.this.mHandler.obtainMessage(5, userId, -1, packageName).sendToTarget();
        }

        public void onParoleStateChanged(boolean isParoleOn) {
            AlarmManagerService.this.mHandler.removeMessages(5);
            AlarmManagerService.this.mHandler.removeMessages(6);
            AlarmManagerService.this.mHandler.obtainMessage(6, Boolean.valueOf(isParoleOn)).sendToTarget();
        }
    }

    public final class Batch {
        public final ArrayList<Alarm> alarms;
        long end;
        int flags;
        boolean standalone;
        long start;

        Batch() {
            this.alarms = new ArrayList<>();
            this.start = 0;
            this.end = JobStatus.NO_LATEST_RUNTIME;
            this.flags = 0;
        }

        Batch(Alarm seed) {
            this.alarms = new ArrayList<>();
            this.start = seed.whenElapsed;
            this.end = AlarmManagerService.clampPositive(seed.maxWhenElapsed);
            this.flags = seed.flags;
            this.alarms.add(seed);
            if (seed.operation == AlarmManagerService.this.mTimeTickSender) {
                long unused = AlarmManagerService.this.mLastTickAdded = System.currentTimeMillis();
            }
        }

        /* access modifiers changed from: package-private */
        public int size() {
            return this.alarms.size();
        }

        /* access modifiers changed from: package-private */
        public Alarm get(int index) {
            return this.alarms.get(index);
        }

        /* access modifiers changed from: package-private */
        public boolean canHold(long whenElapsed, long maxWhen) {
            return this.end >= whenElapsed && this.start <= maxWhen;
        }

        /* access modifiers changed from: package-private */
        public boolean add(Alarm alarm) {
            boolean newStart = false;
            int index = Collections.binarySearch(this.alarms, alarm, AlarmManagerService.sIncreasingTimeOrder);
            if (index < 0) {
                index = (0 - index) - 1;
            }
            this.alarms.add(index, alarm);
            if (alarm.operation == AlarmManagerService.this.mTimeTickSender) {
                long unused = AlarmManagerService.this.mLastTickAdded = System.currentTimeMillis();
            }
            if (alarm.whenElapsed > this.start) {
                this.start = alarm.whenElapsed;
                newStart = true;
            }
            if (alarm.maxWhenElapsed < this.end) {
                this.end = alarm.maxWhenElapsed;
            }
            this.flags |= alarm.flags;
            return newStart;
        }

        static /* synthetic */ boolean lambda$remove$0(Alarm alarm, Alarm a) {
            return a == alarm;
        }

        /* access modifiers changed from: package-private */
        public boolean remove(Alarm alarm) {
            return remove((Predicate<Alarm>) new Predicate() {
                public final boolean test(Object obj) {
                    return AlarmManagerService.Batch.lambda$remove$0(AlarmManagerService.Alarm.this, (AlarmManagerService.Alarm) obj);
                }
            });
        }

        /* access modifiers changed from: package-private */
        public boolean remove(Predicate<Alarm> predicate) {
            boolean didRemove = false;
            long newStart = 0;
            long newEnd = JobStatus.NO_LATEST_RUNTIME;
            int newFlags = 0;
            int i = 0;
            while (i < this.alarms.size()) {
                Alarm alarm = this.alarms.get(i);
                if (predicate.test(alarm)) {
                    this.alarms.remove(i);
                    didRemove = true;
                    AlarmManagerService.this.hwRemoveRtcAlarm(alarm, true);
                    if (alarm.alarmClock != null) {
                        AlarmManagerService.this.mNextAlarmClockMayChange = true;
                    }
                    if (alarm.operation == AlarmManagerService.this.mTimeTickSender) {
                        long unused = AlarmManagerService.this.mLastTickRemoved = System.currentTimeMillis();
                    }
                } else {
                    if (alarm.whenElapsed > newStart) {
                        newStart = alarm.whenElapsed;
                    }
                    if (alarm.maxWhenElapsed < newEnd) {
                        newEnd = alarm.maxWhenElapsed;
                    }
                    newFlags |= alarm.flags;
                    i++;
                }
            }
            if (didRemove) {
                this.start = newStart;
                this.end = newEnd;
                this.flags = newFlags;
            }
            return didRemove;
        }

        /* access modifiers changed from: package-private */
        public boolean hasPackage(String packageName) {
            int N = this.alarms.size();
            for (int i = 0; i < N; i++) {
                if (this.alarms.get(i).matches(packageName)) {
                    return true;
                }
            }
            return false;
        }

        /* access modifiers changed from: package-private */
        public boolean hasWakeups() {
            int N = this.alarms.size();
            for (int i = 0; i < N; i++) {
                if ((this.alarms.get(i).type & 1) == 0) {
                    return true;
                }
            }
            return false;
        }

        public String toString() {
            StringBuilder b = new StringBuilder(40);
            b.append("Batch{");
            b.append(Integer.toHexString(hashCode()));
            b.append(" num=");
            b.append(size());
            b.append(" start=");
            b.append(this.start);
            b.append(" end=");
            b.append(this.end);
            if (this.flags != 0) {
                b.append(" flgs=0x");
                b.append(Integer.toHexString(this.flags));
            }
            b.append('}');
            return b.toString();
        }

        public void writeToProto(ProtoOutputStream proto, long fieldId, long nowElapsed, long nowRTC) {
            ProtoOutputStream protoOutputStream = proto;
            long token = proto.start(fieldId);
            protoOutputStream.write(1112396529665L, this.start);
            protoOutputStream.write(1112396529666L, this.end);
            protoOutputStream.write(1120986464259L, this.flags);
            Iterator<Alarm> it = this.alarms.iterator();
            while (it.hasNext()) {
                it.next().writeToProto(protoOutputStream, 2246267895812L, nowElapsed, nowRTC);
            }
            protoOutputStream.end(token);
        }
    }

    static class BatchTimeOrder implements Comparator<Batch> {
        BatchTimeOrder() {
        }

        public int compare(Batch b1, Batch b2) {
            if (!(b1 == null || b2 == null)) {
                long when1 = b1.start;
                long when2 = b2.start;
                if (when1 > when2) {
                    return 1;
                }
                if (when1 < when2) {
                    return -1;
                }
            }
            return 0;
        }
    }

    static final class BroadcastStats {
        long aggregateTime;
        int count;
        final ArrayMap<String, FilterStats> filterStats = new ArrayMap<>();
        final String mPackageName;
        final int mUid;
        int nesting;
        int numWakeup;
        long startTime;

        BroadcastStats(int uid, String packageName) {
            this.mUid = uid;
            this.mPackageName = packageName;
        }

        public String toString() {
            return "BroadcastStats{uid=" + this.mUid + ", packageName=" + this.mPackageName + ", aggregateTime=" + this.aggregateTime + ", count=" + this.count + ", numWakeup=" + this.numWakeup + ", startTime=" + this.startTime + ", nesting=" + this.nesting + "}";
        }

        public void writeToProto(ProtoOutputStream proto, long fieldId) {
            long token = proto.start(fieldId);
            proto.write(1120986464257L, this.mUid);
            proto.write(1138166333442L, this.mPackageName);
            proto.write(1112396529667L, this.aggregateTime);
            proto.write(1120986464260L, this.count);
            proto.write(1120986464261L, this.numWakeup);
            proto.write(1112396529670L, this.startTime);
            proto.write(1120986464263L, this.nesting);
            proto.end(token);
        }
    }

    class ClockReceiver extends BroadcastReceiver {
        public ClockReceiver() {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.TIME_TICK");
            filter.addAction("android.intent.action.DATE_CHANGED");
            AlarmManagerService.this.getContext().registerReceiver(this, filter);
        }

        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.intent.action.TIME_TICK")) {
                Slog.v(AlarmManagerService.TAG, "Received TIME_TICK alarm; rescheduling");
                synchronized (AlarmManagerService.this.mLock) {
                    long unused = AlarmManagerService.this.mLastTickReceived = System.currentTimeMillis();
                }
                scheduleTimeTickEvent();
            } else if (intent.getAction().equals("android.intent.action.DATE_CHANGED")) {
                Flog.i(500, "Received DATE_CHANGED alarm; rescheduling");
                int unused2 = AlarmManagerService.this.setKernelTimezone(AlarmManagerService.this.mNativeData, -(TimeZone.getTimeZone(SystemProperties.get(AlarmManagerService.TIMEZONE_PROPERTY)).getOffset(System.currentTimeMillis()) / 60000));
                scheduleDateChangedEvent();
            }
        }

        public void scheduleTimeTickEvent() {
            long currentTime = System.currentTimeMillis();
            long triggerAtTime = SystemClock.elapsedRealtime() + ((60000 * ((currentTime / 60000) + 1)) - currentTime);
            Flog.i(500, "scheduleTimeTickEvent triggerAtTime = " + triggerAtTime);
            AlarmManagerService.this.setImpl(3, triggerAtTime, 0, 0, AlarmManagerService.this.mTimeTickSender, null, "time_tick", 1, null, null, Process.myUid(), PackageManagerService.PLATFORM_PACKAGE_NAME);
            synchronized (AlarmManagerService.this.mLock) {
                long unused = AlarmManagerService.this.mLastTickSet = currentTime;
            }
        }

        public void scheduleDateChangedEvent() {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(11, 0);
            calendar.set(12, 0);
            calendar.set(13, 0);
            calendar.set(14, 0);
            calendar.add(5, 1);
            AlarmManagerService.this.setImpl(1, calendar.getTimeInMillis(), 0, 0, AlarmManagerService.this.mDateChangeSender, null, null, 1, null, null, Process.myUid(), PackageManagerService.PLATFORM_PACKAGE_NAME);
        }
    }

    private final class Constants extends ContentObserver {
        private static final long DEFAULT_ALLOW_WHILE_IDLE_LONG_TIME = 540000;
        private static final long DEFAULT_ALLOW_WHILE_IDLE_SHORT_TIME = 5000;
        private static final long DEFAULT_ALLOW_WHILE_IDLE_WHITELIST_DURATION = 10000;
        private static final long DEFAULT_LISTENER_TIMEOUT = 5000;
        private static final long DEFAULT_MAX_INTERVAL = 31536000000L;
        private static final long DEFAULT_MIN_FUTURITY = 5000;
        private static final long DEFAULT_MIN_INTERVAL = 60000;
        private static final String KEY_ALLOW_WHILE_IDLE_LONG_TIME = "allow_while_idle_long_time";
        private static final String KEY_ALLOW_WHILE_IDLE_SHORT_TIME = "allow_while_idle_short_time";
        private static final String KEY_ALLOW_WHILE_IDLE_WHITELIST_DURATION = "allow_while_idle_whitelist_duration";
        private static final String KEY_LISTENER_TIMEOUT = "listener_timeout";
        private static final String KEY_MAX_INTERVAL = "max_interval";
        private static final String KEY_MIN_FUTURITY = "min_futurity";
        private static final String KEY_MIN_INTERVAL = "min_interval";
        public long ALLOW_WHILE_IDLE_LONG_TIME = DEFAULT_ALLOW_WHILE_IDLE_LONG_TIME;
        public long ALLOW_WHILE_IDLE_SHORT_TIME = 5000;
        public long ALLOW_WHILE_IDLE_WHITELIST_DURATION = 10000;
        public long[] APP_STANDBY_MIN_DELAYS = new long[this.DEFAULT_APP_STANDBY_DELAYS.length];
        private final long[] DEFAULT_APP_STANDBY_DELAYS = {0, 360000, 1800000, AppStandbyController.SettingsObserver.DEFAULT_SYSTEM_UPDATE_TIMEOUT, 864000000};
        private final String[] KEYS_APP_STANDBY_DELAY = {"standby_active_delay", "standby_working_delay", "standby_frequent_delay", "standby_rare_delay", "standby_never_delay"};
        public long LISTENER_TIMEOUT = 5000;
        public long MAX_INTERVAL = 31536000000L;
        public long MIN_FUTURITY = 5000;
        public long MIN_INTERVAL = 60000;
        private long mLastAllowWhileIdleWhitelistDuration = -1;
        private final KeyValueListParser mParser = new KeyValueListParser(',');
        private ContentResolver mResolver;

        public Constants(Handler handler) {
            super(handler);
            updateAllowWhileIdleWhitelistDurationLocked();
        }

        public void start(ContentResolver resolver) {
            this.mResolver = resolver;
            this.mResolver.registerContentObserver(Settings.Global.getUriFor("alarm_manager_constants"), false, this);
            updateConstants();
        }

        public void updateAllowWhileIdleWhitelistDurationLocked() {
            if (this.mLastAllowWhileIdleWhitelistDuration != this.ALLOW_WHILE_IDLE_WHITELIST_DURATION) {
                this.mLastAllowWhileIdleWhitelistDuration = this.ALLOW_WHILE_IDLE_WHITELIST_DURATION;
                BroadcastOptions opts = BroadcastOptions.makeBasic();
                opts.setTemporaryAppWhitelistDuration(this.ALLOW_WHILE_IDLE_WHITELIST_DURATION);
                AlarmManagerService.this.mIdleOptions = opts.toBundle();
            }
        }

        public void onChange(boolean selfChange, Uri uri) {
            updateConstants();
        }

        private void updateConstants() {
            synchronized (AlarmManagerService.this.mLock) {
                try {
                    this.mParser.setString(Settings.Global.getString(this.mResolver, "alarm_manager_constants"));
                } catch (IllegalArgumentException e) {
                    Slog.e(AlarmManagerService.TAG, "Bad alarm manager settings", e);
                }
                this.MIN_FUTURITY = this.mParser.getLong(KEY_MIN_FUTURITY, 5000);
                this.MIN_INTERVAL = this.mParser.getLong(KEY_MIN_INTERVAL, 60000);
                this.MAX_INTERVAL = this.mParser.getLong(KEY_MAX_INTERVAL, 31536000000L);
                this.ALLOW_WHILE_IDLE_SHORT_TIME = this.mParser.getLong(KEY_ALLOW_WHILE_IDLE_SHORT_TIME, 5000);
                this.ALLOW_WHILE_IDLE_LONG_TIME = this.mParser.getLong(KEY_ALLOW_WHILE_IDLE_LONG_TIME, DEFAULT_ALLOW_WHILE_IDLE_LONG_TIME);
                this.ALLOW_WHILE_IDLE_WHITELIST_DURATION = this.mParser.getLong(KEY_ALLOW_WHILE_IDLE_WHITELIST_DURATION, 10000);
                this.LISTENER_TIMEOUT = this.mParser.getLong(KEY_LISTENER_TIMEOUT, 5000);
                this.APP_STANDBY_MIN_DELAYS[0] = this.mParser.getDurationMillis(this.KEYS_APP_STANDBY_DELAY[0], this.DEFAULT_APP_STANDBY_DELAYS[0]);
                for (int i = 1; i < this.KEYS_APP_STANDBY_DELAY.length; i++) {
                    this.APP_STANDBY_MIN_DELAYS[i] = this.mParser.getDurationMillis(this.KEYS_APP_STANDBY_DELAY[i], Math.max(this.APP_STANDBY_MIN_DELAYS[i - 1], this.DEFAULT_APP_STANDBY_DELAYS[i]));
                }
                updateAllowWhileIdleWhitelistDurationLocked();
            }
        }

        /* access modifiers changed from: package-private */
        public void dump(PrintWriter pw) {
            pw.println("  Settings:");
            pw.print("    ");
            pw.print(KEY_MIN_FUTURITY);
            pw.print("=");
            TimeUtils.formatDuration(this.MIN_FUTURITY, pw);
            pw.println();
            pw.print("    ");
            pw.print(KEY_MIN_INTERVAL);
            pw.print("=");
            TimeUtils.formatDuration(this.MIN_INTERVAL, pw);
            pw.println();
            pw.print("    ");
            pw.print(KEY_MAX_INTERVAL);
            pw.print("=");
            TimeUtils.formatDuration(this.MAX_INTERVAL, pw);
            pw.println();
            pw.print("    ");
            pw.print(KEY_LISTENER_TIMEOUT);
            pw.print("=");
            TimeUtils.formatDuration(this.LISTENER_TIMEOUT, pw);
            pw.println();
            pw.print("    ");
            pw.print(KEY_ALLOW_WHILE_IDLE_SHORT_TIME);
            pw.print("=");
            TimeUtils.formatDuration(this.ALLOW_WHILE_IDLE_SHORT_TIME, pw);
            pw.println();
            pw.print("    ");
            pw.print(KEY_ALLOW_WHILE_IDLE_LONG_TIME);
            pw.print("=");
            TimeUtils.formatDuration(this.ALLOW_WHILE_IDLE_LONG_TIME, pw);
            pw.println();
            pw.print("    ");
            pw.print(KEY_ALLOW_WHILE_IDLE_WHITELIST_DURATION);
            pw.print("=");
            TimeUtils.formatDuration(this.ALLOW_WHILE_IDLE_WHITELIST_DURATION, pw);
            pw.println();
            for (int i = 0; i < this.KEYS_APP_STANDBY_DELAY.length; i++) {
                pw.print("    ");
                pw.print(this.KEYS_APP_STANDBY_DELAY[i]);
                pw.print("=");
                TimeUtils.formatDuration(this.APP_STANDBY_MIN_DELAYS[i], pw);
                pw.println();
            }
        }

        /* access modifiers changed from: package-private */
        public void dumpProto(ProtoOutputStream proto, long fieldId) {
            long token = proto.start(fieldId);
            proto.write(1112396529665L, this.MIN_FUTURITY);
            proto.write(1112396529666L, this.MIN_INTERVAL);
            proto.write(1112396529671L, this.MAX_INTERVAL);
            proto.write(1112396529667L, this.LISTENER_TIMEOUT);
            proto.write(1112396529668L, this.ALLOW_WHILE_IDLE_SHORT_TIME);
            proto.write(1112396529669L, this.ALLOW_WHILE_IDLE_LONG_TIME);
            proto.write(1112396529670L, this.ALLOW_WHILE_IDLE_WHITELIST_DURATION);
            proto.end(token);
        }
    }

    class DeliveryTracker extends IAlarmCompleteListener.Stub implements PendingIntent.OnFinished {
        DeliveryTracker() {
        }

        private InFlight removeLocked(PendingIntent pi, Intent intent) {
            for (int i = 0; i < AlarmManagerService.this.mInFlight.size(); i++) {
                if (AlarmManagerService.this.mInFlight.get(i).mPendingIntent == pi) {
                    return AlarmManagerService.this.mInFlight.remove(i);
                }
            }
            LocalLog localLog = AlarmManagerService.this.mLog;
            localLog.w("No in-flight alarm for " + pi + " " + intent);
            return null;
        }

        private InFlight removeLocked(IBinder listener) {
            for (int i = 0; i < AlarmManagerService.this.mInFlight.size(); i++) {
                if (AlarmManagerService.this.mInFlight.get(i).mListener == listener) {
                    return AlarmManagerService.this.mInFlight.remove(i);
                }
            }
            LocalLog localLog = AlarmManagerService.this.mLog;
            localLog.w("No in-flight alarm for listener " + listener);
            return null;
        }

        private void updateStatsLocked(InFlight inflight) {
            long nowELAPSED = SystemClock.elapsedRealtime();
            BroadcastStats bs = inflight.mBroadcastStats;
            bs.nesting--;
            if (bs.nesting <= 0) {
                bs.nesting = 0;
                bs.aggregateTime += nowELAPSED - bs.startTime;
            }
            FilterStats fs = inflight.mFilterStats;
            fs.nesting--;
            if (fs.nesting <= 0) {
                fs.nesting = 0;
                fs.aggregateTime += nowELAPSED - fs.startTime;
            }
            ActivityManager.noteAlarmFinish(inflight.mPendingIntent, inflight.mWorkSource, inflight.mUid, inflight.mTag);
        }

        private void updateTrackingLocked(InFlight inflight) {
            if (inflight != null) {
                updateStatsLocked(inflight);
            }
            AlarmManagerService alarmManagerService = AlarmManagerService.this;
            alarmManagerService.mBroadcastRefCount--;
            int i = 0;
            if (AlarmManagerService.this.mBroadcastRefCount == 0) {
                AlarmManagerService.this.mHandler.obtainMessage(4, 0).sendToTarget();
                AlarmManagerService.this.mWakeLock.release();
                if (AlarmManagerService.this.mInFlight.size() > 0) {
                    AlarmManagerService.this.mLog.w("Finished all dispatches with " + AlarmManagerService.this.mInFlight.size() + " remaining inflights");
                    while (true) {
                        int i2 = i;
                        if (i2 < AlarmManagerService.this.mInFlight.size()) {
                            AlarmManagerService.this.mLog.w("  Remaining #" + i2 + ": " + AlarmManagerService.this.mInFlight.get(i2));
                            i = i2 + 1;
                        } else {
                            AlarmManagerService.this.mInFlight.clear();
                            return;
                        }
                    }
                }
            } else if (AlarmManagerService.this.mInFlight.size() > 0) {
                InFlight inFlight = AlarmManagerService.this.mInFlight.get(0);
                AlarmManagerService.this.setWakelockWorkSource(inFlight.mPendingIntent, inFlight.mWorkSource, inFlight.mAlarmType, inFlight.mTag, -1, false);
            } else {
                AlarmManagerService.this.mLog.w("Alarm wakelock still held but sent queue empty");
                AlarmManagerService.this.mWakeLock.setWorkSource(null);
            }
        }

        public void alarmComplete(IBinder who) {
            if (who == null) {
                LocalLog localLog = AlarmManagerService.this.mLog;
                localLog.w("Invalid alarmComplete: uid=" + Binder.getCallingUid() + " pid=" + Binder.getCallingPid());
                return;
            }
            long ident = Binder.clearCallingIdentity();
            try {
                synchronized (AlarmManagerService.this.mLock) {
                    AlarmManagerService.this.mHandler.removeMessages(3, who);
                    InFlight inflight = removeLocked(who);
                    if (inflight != null) {
                        updateTrackingLocked(inflight);
                        int unused = AlarmManagerService.this.mListenerFinishCount = AlarmManagerService.this.mListenerFinishCount + 1;
                    }
                }
                Binder.restoreCallingIdentity(ident);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(ident);
                throw th;
            }
        }

        public void onSendFinished(PendingIntent pi, Intent intent, int resultCode, String resultData, Bundle resultExtras) {
            synchronized (AlarmManagerService.this.mLock) {
                int unused = AlarmManagerService.this.mSendFinishCount = AlarmManagerService.this.mSendFinishCount + 1;
                updateTrackingLocked(removeLocked(pi, intent));
            }
        }

        public void alarmTimedOut(IBinder who) {
            synchronized (AlarmManagerService.this.mLock) {
                InFlight inflight = removeLocked(who);
                if (inflight != null) {
                    updateTrackingLocked(inflight);
                    int unused = AlarmManagerService.this.mListenerFinishCount = AlarmManagerService.this.mListenerFinishCount + 1;
                } else {
                    LocalLog localLog = AlarmManagerService.this.mLog;
                    localLog.w("Spurious timeout of listener " + who);
                }
            }
        }

        @GuardedBy("mLock")
        public void deliverLocked(Alarm alarm, long nowELAPSED, boolean allowWhileIdle) {
            char c;
            int i;
            Alarm alarm2;
            Alarm alarm3 = alarm;
            long j = nowELAPSED;
            if (alarm3.operation != null) {
                AlarmManagerService.this.hwAddFirstFlagForRtcAlarm(alarm3, AlarmManagerService.this.mBackgroundIntent);
                AlarmManagerService.this.mBackgroundIntent.addHwFlags(2048);
                int unused = AlarmManagerService.this.mSendCount = AlarmManagerService.this.mSendCount + 1;
                if (alarm3.priorityClass.priority == 0) {
                    long unused2 = AlarmManagerService.this.mLastTickIssued = j;
                }
                try {
                    alarm3.operation.send(AlarmManagerService.this.getContext(), 0, AlarmManagerService.this.mBackgroundIntent.putExtra("android.intent.extra.ALARM_COUNT", alarm3.count), AlarmManagerService.this.mDeliveryTracker, AlarmManagerService.this.mHandler, null, allowWhileIdle ? AlarmManagerService.this.mIdleOptions : null);
                    AlarmManagerService.this.hwRemoveRtcAlarm(alarm3, false);
                } catch (PendingIntent.CanceledException e) {
                    if (alarm3.operation == AlarmManagerService.this.mTimeTickSender) {
                        Slog.wtf(AlarmManagerService.TAG, "mTimeTickSender canceled");
                    }
                    if (alarm3.repeatInterval > 0) {
                        AlarmManagerService.this.removeImpl(alarm3.operation);
                    }
                    int unused3 = AlarmManagerService.this.mSendFinishCount = AlarmManagerService.this.mSendFinishCount + 1;
                    return;
                }
            } else {
                int unused4 = AlarmManagerService.this.mListenerCount = AlarmManagerService.this.mListenerCount + 1;
                try {
                    alarm3.listener.doAlarm(this);
                    AlarmManagerService.this.mHandler.sendMessageDelayed(AlarmManagerService.this.mHandler.obtainMessage(3, alarm3.listener.asBinder()), AlarmManagerService.this.mConstants.LISTENER_TIMEOUT);
                } catch (Exception e2) {
                    int unused5 = AlarmManagerService.this.mListenerFinishCount = AlarmManagerService.this.mListenerFinishCount + 1;
                    return;
                }
            }
            if (AlarmManagerService.this.mBroadcastRefCount == 0) {
                AlarmManagerService.this.setWakelockWorkSource(alarm3.operation, alarm3.workSource, alarm3.type, alarm3.statsTag, alarm3.operation == null ? alarm3.uid : -1, true);
                AlarmManagerService.this.mWakeLock.acquire();
                AlarmManagerService.this.mHandler.obtainMessage(4, 1).sendToTarget();
            }
            InFlight inFlight = new InFlight(AlarmManagerService.this, alarm3.operation, alarm3.listener, alarm3.workSource, alarm3.uid, alarm3.packageName, alarm3.type, alarm3.statsTag, j);
            AlarmManagerService.this.mInFlight.add(inFlight);
            AlarmManagerService.this.mBroadcastRefCount++;
            if (allowWhileIdle) {
                i = 1;
                alarm2 = alarm;
                AlarmManagerService.this.mLastAllowWhileIdleDispatch.put(alarm2.creatorUid, j);
                if (AlarmManagerService.this.mAppStateTracker == null || AlarmManagerService.this.mAppStateTracker.isUidInForeground(alarm2.creatorUid)) {
                    c = 0;
                    AlarmManagerService.this.mUseAllowWhileIdleShortTime.put(alarm2.creatorUid, true);
                } else {
                    c = 0;
                    AlarmManagerService.this.mUseAllowWhileIdleShortTime.put(alarm2.creatorUid, false);
                }
            } else {
                i = 1;
                alarm2 = alarm;
                c = 0;
            }
            if (!AlarmManagerService.this.isExemptFromAppStandby(alarm2)) {
                AlarmManagerService.this.mLastAlarmDeliveredForPackage.put(Pair.create(alarm2.sourcePackage, Integer.valueOf(UserHandle.getUserId(alarm2.creatorUid))), Long.valueOf(nowELAPSED));
            }
            BroadcastStats bs = inFlight.mBroadcastStats;
            bs.count += i;
            if (bs.nesting == 0) {
                bs.nesting = i;
                bs.startTime = j;
            } else {
                bs.nesting += i;
            }
            FilterStats fs = inFlight.mFilterStats;
            fs.count += i;
            if (fs.nesting == 0) {
                fs.nesting = i;
                fs.startTime = j;
            } else {
                fs.nesting += i;
            }
            if (alarm2.type == 2 || alarm2.type == 0) {
                bs.numWakeup += i;
                fs.numWakeup += i;
                ActivityManager.noteWakeupAlarm(alarm2.operation, alarm2.workSource, alarm2.uid, alarm2.packageName, alarm2.statsTag);
            }
            String pkg = alarm2.packageName;
            if (pkg != null && (!AlarmManagerService.this.mInteractive || !PackageManagerService.PLATFORM_PACKAGE_NAME.equals(pkg))) {
                String intentStr = BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
                if (alarm2.operation != null) {
                    Intent alarmIntent = alarm2.operation.getIntent();
                    if (alarmIntent != null) {
                        intentStr = alarmIntent.getAction();
                    }
                } else if (alarm2.statsTag != null) {
                    intentStr = alarm2.statsTag;
                }
                String valueOf = String.valueOf(alarm2.type);
                String valueOf2 = String.valueOf(alarm2.repeatInterval);
                String[] strArr = new String[2];
                strArr[c] = String.valueOf(fs.count);
                strArr[i] = intentStr;
                LogPower.push(121, pkg, valueOf, valueOf2, strArr);
            }
        }
    }

    static final class FilterStats {
        long aggregateTime;
        int count;
        long lastTime;
        @RCUnownedRef
        final BroadcastStats mBroadcastStats;
        final String mTag;
        int nesting;
        int numWakeup;
        long startTime;

        FilterStats(BroadcastStats broadcastStats, String tag) {
            this.mBroadcastStats = broadcastStats;
            this.mTag = tag;
        }

        public String toString() {
            return "FilterStats{tag=" + this.mTag + ", lastTime=" + this.lastTime + ", aggregateTime=" + this.aggregateTime + ", count=" + this.count + ", numWakeup=" + this.numWakeup + ", startTime=" + this.startTime + ", nesting=" + this.nesting + "}";
        }

        public void writeToProto(ProtoOutputStream proto, long fieldId) {
            long token = proto.start(fieldId);
            proto.write(1138166333441L, this.mTag);
            proto.write(1112396529666L, this.lastTime);
            proto.write(1112396529667L, this.aggregateTime);
            proto.write(1120986464260L, this.count);
            proto.write(1120986464261L, this.numWakeup);
            proto.write(1112396529670L, this.startTime);
            proto.write(1120986464263L, this.nesting);
            proto.end(token);
        }
    }

    static final class IdleDispatchEntry {
        long argRealtime;
        long elapsedRealtime;
        String op;
        String pkg;
        String tag;
        int uid;

        IdleDispatchEntry() {
        }
    }

    static final class InFlight {
        final int mAlarmType;
        final BroadcastStats mBroadcastStats;
        final FilterStats mFilterStats;
        final IBinder mListener;
        final PendingIntent mPendingIntent;
        final String mTag;
        final int mUid;
        final long mWhenElapsed;
        final WorkSource mWorkSource;

        InFlight(AlarmManagerService service, PendingIntent pendingIntent, IAlarmListener listener, WorkSource workSource, int uid, String alarmPkg, int alarmType, String tag, long nowELAPSED) {
            BroadcastStats broadcastStats;
            this.mPendingIntent = pendingIntent;
            this.mWhenElapsed = nowELAPSED;
            this.mListener = listener != null ? listener.asBinder() : null;
            this.mWorkSource = workSource;
            this.mUid = uid;
            this.mTag = tag;
            if (pendingIntent != null) {
                broadcastStats = service.getStatsLocked(pendingIntent);
            } else {
                broadcastStats = service.getStatsLocked(uid, alarmPkg);
            }
            this.mBroadcastStats = broadcastStats;
            FilterStats fs = this.mBroadcastStats.filterStats.get(this.mTag);
            if (fs == null) {
                fs = new FilterStats(this.mBroadcastStats, this.mTag);
                this.mBroadcastStats.filterStats.put(this.mTag, fs);
            }
            fs.lastTime = nowELAPSED;
            this.mFilterStats = fs;
            this.mAlarmType = alarmType;
        }

        public String toString() {
            return "InFlight{pendingIntent=" + this.mPendingIntent + ", when=" + this.mWhenElapsed + ", workSource=" + this.mWorkSource + ", uid=" + this.mUid + ", tag=" + this.mTag + ", broadcastStats=" + this.mBroadcastStats + ", filterStats=" + this.mFilterStats + ", alarmType=" + this.mAlarmType + "}";
        }

        public void writeToProto(ProtoOutputStream proto, long fieldId) {
            long token = proto.start(fieldId);
            proto.write(1120986464257L, this.mUid);
            proto.write(1138166333442L, this.mTag);
            proto.write(1112396529667L, this.mWhenElapsed);
            proto.write(1159641169924L, this.mAlarmType);
            if (this.mPendingIntent != null) {
                this.mPendingIntent.writeToProto(proto, 1146756268037L);
            }
            if (this.mBroadcastStats != null) {
                this.mBroadcastStats.writeToProto(proto, 1146756268038L);
            }
            if (this.mFilterStats != null) {
                this.mFilterStats.writeToProto(proto, 1146756268039L);
            }
            if (this.mWorkSource != null) {
                this.mWorkSource.writeToProto(proto, 1146756268040L);
            }
            proto.end(token);
        }
    }

    public static class IncreasingTimeOrder implements Comparator<Alarm> {
        public int compare(Alarm a1, Alarm a2) {
            long when1 = a1.whenElapsed;
            long when2 = a2.whenElapsed;
            if (when1 > when2) {
                return 1;
            }
            if (when1 < when2) {
                return -1;
            }
            return 0;
        }
    }

    class InteractiveStateReceiver extends BroadcastReceiver {
        public InteractiveStateReceiver() {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.SCREEN_OFF");
            filter.addAction("android.intent.action.SCREEN_ON");
            filter.setPriority(1000);
            AlarmManagerService.this.getContext().registerReceiver(this, filter);
        }

        public void onReceive(Context context, Intent intent) {
            synchronized (AlarmManagerService.this.mLock) {
                AlarmManagerService.this.interactiveStateChangedLocked("android.intent.action.SCREEN_ON".equals(intent.getAction()));
                if ("android.intent.action.SCREEN_ON".equals(intent.getAction())) {
                    boolean unused = AlarmManagerService.this.mIsScreenOn = true;
                } else if ("android.intent.action.SCREEN_OFF".equals(intent.getAction())) {
                    boolean unused2 = AlarmManagerService.this.mIsScreenOn = false;
                }
            }
        }
    }

    private final class LocalService implements AlarmManagerInternal {
        private LocalService() {
        }

        public void removeAlarmsForUid(int uid) {
            synchronized (AlarmManagerService.this.mLock) {
                AlarmManagerService.this.removeLocked(uid);
            }
        }
    }

    final class PriorityClass {
        int priority = 2;
        int seq;

        PriorityClass() {
            this.seq = this$0.mCurrentSeq - 1;
        }
    }

    private class ShellCmd extends ShellCommand {
        private ShellCmd() {
        }

        /* access modifiers changed from: package-private */
        public IAlarmManager getBinderService() {
            return IAlarmManager.Stub.asInterface(AlarmManagerService.this.mService);
        }

        /* JADX WARNING: Removed duplicated region for block: B:18:0x0036 A[Catch:{ Exception -> 0x005d }] */
        /* JADX WARNING: Removed duplicated region for block: B:19:0x003b A[Catch:{ Exception -> 0x005d }] */
        /* JADX WARNING: Removed duplicated region for block: B:21:0x0047 A[Catch:{ Exception -> 0x005d }] */
        public int onCommand(String cmd) {
            boolean z;
            if (cmd == null) {
                return handleDefaultCommands(cmd);
            }
            PrintWriter pw = getOutPrintWriter();
            int i = -1;
            try {
                int hashCode = cmd.hashCode();
                if (hashCode == 1369384280) {
                    if (cmd.equals("set-time")) {
                        z = false;
                        switch (z) {
                            case false:
                                break;
                            case true:
                                break;
                        }
                    }
                } else if (hashCode == 2023087364) {
                    if (cmd.equals("set-timezone")) {
                        z = true;
                        switch (z) {
                            case false:
                                if (getBinderService().setTime(Long.parseLong(getNextArgRequired()))) {
                                    i = 0;
                                }
                                return i;
                            case true:
                                getBinderService().setTimeZone(getNextArgRequired());
                                return 0;
                            default:
                                return handleDefaultCommands(cmd);
                        }
                    }
                }
                z = true;
                switch (z) {
                    case false:
                        break;
                    case true:
                        break;
                }
            } catch (Exception e) {
                pw.println(e);
                return -1;
            }
        }

        public void onHelp() {
            PrintWriter pw = getOutPrintWriter();
            pw.println("Alarm manager service (alarm) commands:");
            pw.println("  help");
            pw.println("    Print this help text.");
            pw.println("  set-time TIME");
            pw.println("    Set the system clock time to TIME where TIME is milliseconds");
            pw.println("    since the Epoch.");
            pw.println("  set-timezone TZ");
            pw.println("    Set the system timezone to TZ where TZ is an Olson id.");
        }
    }

    interface Stats {
        public static final int REBATCH_ALL_ALARMS = 0;
        public static final int REORDER_ALARMS_FOR_STANDBY = 1;
    }

    final class UidObserver extends IUidObserver.Stub {
        UidObserver() {
        }

        public void onUidStateChanged(int uid, int procState, long procStateSeq) {
        }

        public void onUidGone(int uid, boolean disabled) {
            if (disabled) {
                AlarmManagerService.this.mHandler.postRemoveForStopped(uid);
            }
        }

        public void onUidActive(int uid) {
        }

        public void onUidIdle(int uid, boolean disabled) {
            if (disabled) {
                AlarmManagerService.this.mHandler.postRemoveForStopped(uid);
            }
        }

        public void onUidCachedChanged(int uid, boolean cached) {
        }
    }

    class UninstallReceiver extends BroadcastReceiver {
        public UninstallReceiver() {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.PACKAGE_REMOVED");
            filter.addAction("android.intent.action.PACKAGE_RESTARTED");
            filter.addAction("android.intent.action.QUERY_PACKAGE_RESTART");
            filter.addDataScheme("package");
            AlarmManagerService.this.getContext().registerReceiver(this, filter);
            IntentFilter sdFilter = new IntentFilter();
            sdFilter.addAction("android.intent.action.EXTERNAL_APPLICATIONS_UNAVAILABLE");
            sdFilter.addAction("android.intent.action.USER_STOPPED");
            sdFilter.addAction("android.intent.action.UID_REMOVED");
            AlarmManagerService.this.getContext().registerReceiver(this, sdFilter);
        }

        /* JADX WARNING: Code restructure failed: missing block: B:75:0x0196, code lost:
            return;
         */
        public void onReceive(Context context, Intent intent) {
            int uid = intent.getIntExtra("android.intent.extra.UID", -1);
            synchronized (AlarmManagerService.this.mLock) {
                String action = intent.getAction();
                String[] pkgList = null;
                Slog.v(AlarmManagerService.TAG, "UninstallReceiver onReceive action:" + action);
                int i = 0;
                if ("android.intent.action.QUERY_PACKAGE_RESTART".equals(action)) {
                    String[] pkgList2 = intent.getStringArrayExtra("android.intent.extra.PACKAGES");
                    int length = pkgList2.length;
                    while (i < length) {
                        if (AlarmManagerService.this.lookForPackageLocked(pkgList2[i])) {
                            setResultCode(-1);
                            return;
                        }
                        i++;
                    }
                    return;
                }
                if ("android.intent.action.EXTERNAL_APPLICATIONS_UNAVAILABLE".equals(action)) {
                    pkgList = intent.getStringArrayExtra("android.intent.extra.changed_package_list");
                } else if ("android.intent.action.USER_STOPPED".equals(action)) {
                    int userHandle = intent.getIntExtra("android.intent.extra.user_handle", -1);
                    if (userHandle >= 0) {
                        AlarmManagerService.this.removeUserLocked(userHandle);
                        for (int i2 = AlarmManagerService.this.mLastAlarmDeliveredForPackage.size() - 1; i2 >= 0; i2--) {
                            if (((Integer) ((Pair) AlarmManagerService.this.mLastAlarmDeliveredForPackage.keyAt(i2)).second).intValue() == userHandle) {
                                AlarmManagerService.this.mLastAlarmDeliveredForPackage.removeAt(i2);
                            }
                        }
                    }
                } else if ("android.intent.action.UID_REMOVED".equals(action)) {
                    if (uid >= 0) {
                        AlarmManagerService.this.mLastAllowWhileIdleDispatch.delete(uid);
                        AlarmManagerService.this.mUseAllowWhileIdleShortTime.delete(uid);
                    }
                } else if (!"android.intent.action.PACKAGE_REMOVED".equals(action) || !intent.getBooleanExtra("android.intent.extra.REPLACING", false)) {
                    Uri data = intent.getData();
                    if (data != null) {
                        String pkg = data.getSchemeSpecificPart();
                        if (pkg != null) {
                            pkgList = new String[]{pkg};
                        }
                    }
                } else {
                    return;
                }
                if (pkgList != null && pkgList.length > 0) {
                    for (int i3 = AlarmManagerService.this.mLastAlarmDeliveredForPackage.size() - 1; i3 >= 0; i3--) {
                        Pair<String, Integer> packageUser = (Pair) AlarmManagerService.this.mLastAlarmDeliveredForPackage.keyAt(i3);
                        if (ArrayUtils.contains(pkgList, (String) packageUser.first) && ((Integer) packageUser.second).intValue() == UserHandle.getUserId(uid)) {
                            AlarmManagerService.this.mLastAlarmDeliveredForPackage.removeAt(i3);
                        }
                    }
                    int i4 = pkgList.length;
                    while (i < i4) {
                        String pkg2 = pkgList[i];
                        if (uid == 1001) {
                            Slog.i(AlarmManagerService.TAG, "removeLocked pkg: " + pkg2 + ", uid:" + uid);
                            AlarmManagerService.this.removeLocked(uid, pkg2);
                        } else if (uid >= 0) {
                            AlarmManagerService.this.removeLocked(uid);
                        } else {
                            AlarmManagerService.this.removeLocked(pkg2);
                        }
                        AlarmManagerService.this.mPriorities.remove(pkg2);
                        for (int i5 = AlarmManagerService.this.mBroadcastStats.size() - 1; i5 >= 0; i5--) {
                            ArrayMap<String, BroadcastStats> uidStats = AlarmManagerService.this.mBroadcastStats.valueAt(i5);
                            if (!(uidStats == null || uidStats.remove(pkg2) == null || uidStats.size() > 0)) {
                                AlarmManagerService.this.mBroadcastStats.removeAt(i5);
                            }
                        }
                        i++;
                    }
                }
            }
        }
    }

    static final class WakeupEvent {
        public String action;
        public int uid;
        public long when;

        public WakeupEvent(long theTime, int theUid, String theAction) {
            this.when = theTime;
            this.uid = theUid;
            this.action = theAction;
        }
    }

    private native void close(long j);

    private native long init();

    private native int set(long j, int i, long j2, long j3);

    private native int setKernelTime(long j, long j2);

    /* access modifiers changed from: private */
    public native int setKernelTimezone(long j, int i);

    /* access modifiers changed from: private */
    public native int waitForAlarm(long j);

    /* access modifiers changed from: protected */
    public native void hwSetClockRTC(long j, long j2, long j3);

    /* access modifiers changed from: package-private */
    public void calculateDeliveryPriorities(ArrayList<Alarm> alarms) {
        int alarmPrio;
        int N = alarms.size();
        for (int i = 0; i < N; i++) {
            Alarm a = alarms.get(i);
            if (a.operation != null && "android.intent.action.TIME_TICK".equals(resetActionCallingIdentity(a.operation))) {
                alarmPrio = 0;
            } else if (a.wakeup != 0) {
                alarmPrio = 1;
            } else {
                alarmPrio = 2;
            }
            PriorityClass packagePrio = a.priorityClass;
            String alarmPackage = a.sourcePackage;
            if (packagePrio == null) {
                packagePrio = this.mPriorities.get(alarmPackage);
            }
            if (packagePrio == null) {
                PriorityClass priorityClass = new PriorityClass();
                a.priorityClass = priorityClass;
                packagePrio = priorityClass;
                this.mPriorities.put(alarmPackage, packagePrio);
            }
            a.priorityClass = packagePrio;
            if (packagePrio.seq != this.mCurrentSeq) {
                packagePrio.priority = alarmPrio;
                packagePrio.seq = this.mCurrentSeq;
            } else if (alarmPrio < packagePrio.priority) {
                packagePrio.priority = alarmPrio;
            }
        }
    }

    /* JADX WARNING: type inference failed for: r2v10, types: [com.android.server.AlarmManagerService$2, android.os.IBinder] */
    public AlarmManagerService(Context context) {
        super(context);
        publishLocalService(AlarmManagerInternal.class, new LocalService());
    }

    static long convertToElapsed(long when, int type) {
        boolean isRtc = true;
        if (!(type == 1 || type == 0)) {
            isRtc = false;
        }
        if (isRtc) {
            return when - (System.currentTimeMillis() - SystemClock.elapsedRealtime());
        }
        return when;
    }

    public static long maxTriggerTime(long now, long triggerAtTime, long interval) {
        long futurity;
        if (interval == 0) {
            futurity = triggerAtTime - now;
        } else {
            futurity = interval;
        }
        if (futurity < 10000) {
            futurity = 0;
        }
        return clampPositive(((long) (0.75d * ((double) futurity))) + triggerAtTime);
    }

    static boolean addBatchLocked(ArrayList<Batch> list, Batch newBatch) {
        int index = Collections.binarySearch(list, newBatch, sBatchOrder);
        if (index < 0) {
            index = (0 - index) - 1;
        }
        list.add(index, newBatch);
        if (index == 0) {
            return true;
        }
        return false;
    }

    private void insertAndBatchAlarmLocked(Alarm alarm) {
        int whichBatch;
        adjustAlarmLocked(alarm);
        if ((alarm.flags & 1) != 0) {
            whichBatch = -1;
        } else {
            whichBatch = attemptCoalesceLocked(alarm.whenElapsed, alarm.maxWhenElapsed);
        }
        if (whichBatch < 0) {
            addBatchLocked(this.mAlarmBatches, new Batch(alarm));
            return;
        }
        Batch batch = this.mAlarmBatches.get(whichBatch);
        if (batch.add(alarm)) {
            this.mAlarmBatches.remove(whichBatch);
            addBatchLocked(this.mAlarmBatches, batch);
        }
    }

    /* access modifiers changed from: package-private */
    public int attemptCoalesceLocked(long whenElapsed, long maxWhen) {
        int N = this.mAlarmBatches.size();
        for (int i = 0; i < N; i++) {
            Batch b = this.mAlarmBatches.get(i);
            if ((b.flags & 1) == 0 && b.canHold(whenElapsed, maxWhen)) {
                return i;
            }
        }
        return -1;
    }

    static int getAlarmCount(ArrayList<Batch> batches) {
        int ret = 0;
        int size = batches.size();
        for (int i = 0; i < size; i++) {
            ret += batches.get(i).size();
        }
        return ret;
    }

    /* access modifiers changed from: package-private */
    public boolean haveAlarmsTimeTickAlarm(ArrayList<Alarm> alarms) {
        if (alarms.size() == 0) {
            return false;
        }
        int batchSize = alarms.size();
        for (int j = 0; j < batchSize; j++) {
            if (alarms.get(j).operation == this.mTimeTickSender) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean haveBatchesTimeTickAlarm(ArrayList<Batch> batches) {
        int numBatches = batches.size();
        for (int i = 0; i < numBatches; i++) {
            if (haveAlarmsTimeTickAlarm(batches.get(i).alarms)) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public void rebatchAllAlarms() {
        synchronized (this.mLock) {
            rebatchAllAlarmsLocked(true);
        }
    }

    /* access modifiers changed from: package-private */
    public void rebatchAllAlarmsLocked(boolean doValidate) {
        long start = this.mStatLogger.getTime();
        int oldCount = getAlarmCount(this.mAlarmBatches) + ArrayUtils.size(this.mPendingWhileIdleAlarms);
        boolean oldHasTick = haveBatchesTimeTickAlarm(this.mAlarmBatches) || haveAlarmsTimeTickAlarm(this.mPendingWhileIdleAlarms);
        ArrayList<Batch> oldSet = (ArrayList) this.mAlarmBatches.clone();
        this.mAlarmBatches.clear();
        Alarm oldPendingIdleUntil = this.mPendingIdleUntil;
        long nowElapsed = SystemClock.elapsedRealtime();
        int oldBatches = oldSet.size();
        this.mCancelRemoveAction = true;
        for (int batchNum = 0; batchNum < oldBatches; batchNum++) {
            Batch batch = oldSet.get(batchNum);
            int N = batch.size();
            for (int i = 0; i < N; i++) {
                reAddAlarmLocked(batch.get(i), nowElapsed, doValidate);
            }
            boolean z = doValidate;
        }
        boolean z2 = doValidate;
        this.mCancelRemoveAction = false;
        if (!(oldPendingIdleUntil == null || oldPendingIdleUntil == this.mPendingIdleUntil)) {
            Slog.wtf(TAG, "Rebatching: idle until changed from " + oldPendingIdleUntil + " to " + this.mPendingIdleUntil);
            if (this.mPendingIdleUntil == null) {
                restorePendingWhileIdleAlarmsLocked();
            }
        }
        int newCount = getAlarmCount(this.mAlarmBatches) + ArrayUtils.size(this.mPendingWhileIdleAlarms);
        boolean newHasTick = haveBatchesTimeTickAlarm(this.mAlarmBatches) || haveAlarmsTimeTickAlarm(this.mPendingWhileIdleAlarms);
        if (oldCount != newCount) {
            Slog.wtf(TAG, "Rebatching: total count changed from " + oldCount + " to " + newCount);
        }
        if (oldHasTick != newHasTick) {
            Slog.wtf(TAG, "Rebatching: hasTick changed from " + oldHasTick + " to " + newHasTick);
        }
        rescheduleKernelAlarmsLocked();
        updateNextAlarmClockLocked();
        this.mStatLogger.logDurationStat(0, start);
    }

    /* access modifiers changed from: package-private */
    public boolean reorderAlarmsBasedOnStandbyBuckets(ArraySet<Pair<String, Integer>> targetPackages) {
        long start = this.mStatLogger.getTime();
        ArrayList<Alarm> rescheduledAlarms = new ArrayList<>();
        for (int batchIndex = this.mAlarmBatches.size() - 1; batchIndex >= 0; batchIndex--) {
            Batch batch = this.mAlarmBatches.get(batchIndex);
            for (int alarmIndex = batch.size() - 1; alarmIndex >= 0; alarmIndex--) {
                Alarm alarm = batch.get(alarmIndex);
                Pair<String, Integer> packageUser = Pair.create(alarm.sourcePackage, Integer.valueOf(UserHandle.getUserId(alarm.creatorUid)));
                if ((targetPackages == null || targetPackages.contains(packageUser)) && adjustDeliveryTimeBasedOnStandbyBucketLocked(alarm)) {
                    batch.remove(alarm);
                    rescheduledAlarms.add(alarm);
                }
            }
            if (batch.size() == 0) {
                this.mAlarmBatches.remove(batchIndex);
            }
        }
        for (int i = 0; i < rescheduledAlarms.size(); i++) {
            insertAndBatchAlarmLocked(rescheduledAlarms.get(i));
        }
        this.mStatLogger.logDurationStat(1, start);
        if (rescheduledAlarms.size() > 0) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public void reAddAlarmLocked(Alarm a, long nowElapsed, boolean doValidate) {
        long maxElapsed;
        a.when = a.origWhen;
        long whenElapsed = convertToElapsed(a.when, a.type);
        if (a.windowLength == 0) {
            maxElapsed = whenElapsed;
        } else if (a.windowLength > 0) {
            maxElapsed = clampPositive(a.windowLength + whenElapsed);
        } else {
            maxElapsed = maxTriggerTime(nowElapsed, whenElapsed, a.repeatInterval);
        }
        a.whenElapsed = whenElapsed;
        a.maxWhenElapsed = maxElapsed;
        setImplLocked(a, true, doValidate);
    }

    static long clampPositive(long val) {
        return val >= 0 ? val : JobStatus.NO_LATEST_RUNTIME;
    }

    /* access modifiers changed from: package-private */
    public void sendPendingBackgroundAlarmsLocked(int uid, String packageName) {
        ArrayList<Alarm> alarmsToDeliver;
        ArrayList<Alarm> alarmsForUid = this.mPendingBackgroundAlarms.get(uid);
        if (alarmsForUid != null && alarmsForUid.size() != 0) {
            if (packageName != null) {
                alarmsToDeliver = new ArrayList<>();
                for (int i = alarmsForUid.size() - 1; i >= 0; i--) {
                    if (alarmsForUid.get(i).matches(packageName)) {
                        alarmsToDeliver.add(alarmsForUid.remove(i));
                    }
                }
                if (alarmsForUid.size() == 0) {
                    this.mPendingBackgroundAlarms.remove(uid);
                }
            } else {
                alarmsToDeliver = alarmsForUid;
                this.mPendingBackgroundAlarms.remove(uid);
            }
            deliverPendingBackgroundAlarmsLocked(alarmsToDeliver, SystemClock.elapsedRealtime());
        }
    }

    /* access modifiers changed from: package-private */
    public void sendAllUnrestrictedPendingBackgroundAlarmsLocked() {
        ArrayList<Alarm> alarmsToDeliver = new ArrayList<>();
        findAllUnrestrictedPendingBackgroundAlarmsLockedInner(this.mPendingBackgroundAlarms, alarmsToDeliver, new Predicate() {
            public final boolean test(Object obj) {
                return AlarmManagerService.this.isBackgroundRestricted((AlarmManagerService.Alarm) obj);
            }
        });
        if (alarmsToDeliver.size() > 0) {
            deliverPendingBackgroundAlarmsLocked(alarmsToDeliver, SystemClock.elapsedRealtime());
        }
    }

    @VisibleForTesting
    static void findAllUnrestrictedPendingBackgroundAlarmsLockedInner(SparseArray<ArrayList<Alarm>> pendingAlarms, ArrayList<Alarm> unrestrictedAlarms, Predicate<Alarm> isBackgroundRestricted) {
        for (int uidIndex = pendingAlarms.size() - 1; uidIndex >= 0; uidIndex--) {
            int keyAt = pendingAlarms.keyAt(uidIndex);
            ArrayList<Alarm> alarmsForUid = pendingAlarms.valueAt(uidIndex);
            for (int alarmIndex = alarmsForUid.size() - 1; alarmIndex >= 0; alarmIndex--) {
                Alarm alarm = alarmsForUid.get(alarmIndex);
                if (!isBackgroundRestricted.test(alarm)) {
                    unrestrictedAlarms.add(alarm);
                    alarmsForUid.remove(alarmIndex);
                }
            }
            if (alarmsForUid.size() == 0) {
                pendingAlarms.removeAt(uidIndex);
            }
        }
    }

    private void deliverPendingBackgroundAlarmsLocked(ArrayList<Alarm> alarms, long nowELAPSED) {
        long j;
        AlarmManagerService alarmManagerService;
        int i;
        int N;
        AlarmManagerService alarmManagerService2 = this;
        ArrayList<Alarm> arrayList = alarms;
        long j2 = nowELAPSED;
        int N2 = alarms.size();
        boolean hasWakeup = false;
        int i2 = 0;
        while (true) {
            int i3 = i2;
            if (i3 >= N2) {
                break;
            }
            Alarm alarm = arrayList.get(i3);
            if (alarm.wakeup) {
                hasWakeup = true;
            }
            boolean hasWakeup2 = hasWakeup;
            alarm.count = 1;
            if (alarm.repeatInterval > 0) {
                alarm.count = (int) (((long) alarm.count) + ((j2 - alarm.expectedWhenElapsed) / alarm.repeatInterval));
                long delta = ((long) alarm.count) * alarm.repeatInterval;
                long nextElapsed = alarm.whenElapsed + delta;
                int i4 = alarm.type;
                long j3 = alarm.when + delta;
                long j4 = alarm.windowLength;
                long maxTriggerTime = maxTriggerTime(j2, nextElapsed, alarm.repeatInterval);
                long j5 = alarm.repeatInterval;
                PendingIntent pendingIntent = alarm.operation;
                int i5 = alarm.flags;
                WorkSource workSource = alarm.workSource;
                WorkSource workSource2 = workSource;
                long j6 = j3;
                long j7 = j5;
                long j8 = j4;
                Alarm alarm2 = alarm;
                N = N2;
                i = i3;
                alarmManagerService2.setImplLocked(i4, j6, nextElapsed, j8, maxTriggerTime, j7, pendingIntent, null, null, i5, true, workSource2, alarm.alarmClock, alarm.uid, alarm.packageName);
            } else {
                N = N2;
                i = i3;
            }
            i2 = i + 1;
            hasWakeup = hasWakeup2;
            N2 = N;
            j2 = nowELAPSED;
            arrayList = alarms;
            alarmManagerService2 = this;
        }
        if (!hasWakeup) {
            alarmManagerService = this;
            j = nowELAPSED;
            if (alarmManagerService.checkAllowNonWakeupDelayLocked(j)) {
                if (alarmManagerService.mPendingNonWakeupAlarms.size() == 0) {
                    alarmManagerService.mStartCurrentDelayTime = j;
                    alarmManagerService.mNextNonWakeupDeliveryTime = ((alarmManagerService.currentNonWakeupFuzzLocked(j) * 3) / 2) + j;
                }
                alarmManagerService.mPendingNonWakeupAlarms.addAll(alarms);
                alarmManagerService.mNumDelayedAlarms += alarms.size();
                return;
            }
        } else {
            alarmManagerService = this;
            j = nowELAPSED;
        }
        ArrayList<Alarm> arrayList2 = alarms;
        if (alarmManagerService.mPendingNonWakeupAlarms.size() > 0) {
            arrayList2.addAll(alarmManagerService.mPendingNonWakeupAlarms);
            long thisDelayTime = j - alarmManagerService.mStartCurrentDelayTime;
            alarmManagerService.mTotalDelayTime += thisDelayTime;
            if (alarmManagerService.mMaxDelayTime < thisDelayTime) {
                alarmManagerService.mMaxDelayTime = thisDelayTime;
            }
            alarmManagerService.mPendingNonWakeupAlarms.clear();
        }
        calculateDeliveryPriorities(alarms);
        Collections.sort(arrayList2, alarmManagerService.mAlarmDispatchComparator);
        deliverAlarmsLocked(alarms, nowELAPSED);
    }

    /* access modifiers changed from: package-private */
    public void restorePendingWhileIdleAlarmsLocked() {
        if (this.mPendingWhileIdleAlarms.size() > 0) {
            ArrayList<Alarm> alarms = this.mPendingWhileIdleAlarms;
            this.mPendingWhileIdleAlarms = new ArrayList<>();
            long nowElapsed = SystemClock.elapsedRealtime();
            for (int i = alarms.size() - 1; i >= 0; i--) {
                reAddAlarmLocked(alarms.get(i), nowElapsed, false);
            }
        }
        rescheduleKernelAlarmsLocked();
        updateNextAlarmClockLocked();
        try {
            this.mTimeTickSender.send();
        } catch (PendingIntent.CanceledException e) {
        }
    }

    public void onStart() {
        this.mNativeData = init();
        this.mNextNonWakeup = 0;
        this.mNextWakeup = 0;
        Flog.d(500, "alarmmanagerservice onStart");
        setTimeZoneImpl(SystemProperties.get(TIMEZONE_PROPERTY));
        if (this.mNativeData != 0) {
            long systemBuildTime = Environment.getRootDirectory().lastModified();
            if ("normal".equals(SystemProperties.get("ro.runmode", "normal")) && System.currentTimeMillis() < systemBuildTime) {
                Slog.i(TAG, "Current time only " + System.currentTimeMillis() + ", advancing to build time " + systemBuildTime);
                setKernelTime(this.mNativeData, systemBuildTime);
            }
        }
        PackageManager packMan = getContext().getPackageManager();
        try {
            ApplicationInfo sysUi = packMan.getApplicationInfo(packMan.getPermissionInfo(SYSTEM_UI_SELF_PERMISSION, 0).packageName, 0);
            if ((sysUi.privateFlags & 8) != 0) {
                this.mSystemUiUid = sysUi.uid;
            } else {
                Slog.e(TAG, "SysUI permission android.permission.systemui.IDENTITY defined by non-privileged app " + sysUi.packageName + " - ignoring");
            }
        } catch (PackageManager.NameNotFoundException e) {
        }
        if (this.mSystemUiUid <= 0) {
            Slog.wtf(TAG, "SysUI package not found!");
        }
        this.mWakeLock = ((PowerManager) getContext().getSystemService("power")).newWakeLock(1, "*alarm*");
        this.mTimeTickSender = PendingIntent.getBroadcastAsUser(getContext(), 0, new Intent("android.intent.action.TIME_TICK").addFlags(1344274432), 0, UserHandle.ALL);
        Intent intent = new Intent("android.intent.action.DATE_CHANGED");
        intent.addFlags(538968064);
        this.mDateChangeSender = PendingIntent.getBroadcastAsUser(getContext(), 0, intent, 67108864, UserHandle.ALL);
        this.mClockReceiver = new ClockReceiver();
        this.mClockReceiver.scheduleTimeTickEvent();
        this.mClockReceiver.scheduleDateChangedEvent();
        this.mInteractiveStateReceiver = new InteractiveStateReceiver();
        this.mUninstallReceiver = new UninstallReceiver();
        if (this.mNativeData != 0) {
            new AlarmThread().start();
        } else {
            Slog.w(TAG, "Failed to open alarm driver. Falling back to a handler.");
        }
        try {
            ActivityManager.getService().registerUidObserver(new UidObserver(), 14, -1, null);
        } catch (RemoteException e2) {
        }
        publishBinderService("alarm", this.mService);
    }

    public void onBootPhase(int phase) {
        if (phase == 500) {
            this.mConstants.start(getContext().getContentResolver());
            this.mAppOps = (AppOpsManager) getContext().getSystemService("appops");
            this.mLocalDeviceIdleController = (DeviceIdleController.LocalService) LocalServices.getService(DeviceIdleController.LocalService.class);
            this.mUsageStatsManagerInternal = (UsageStatsManagerInternal) LocalServices.getService(UsageStatsManagerInternal.class);
            this.mUsageStatsManagerInternal.addAppIdleStateChangeListener(new AppStandbyTracker());
            this.mAppStateTracker = (AppStateTracker) LocalServices.getService(AppStateTracker.class);
            this.mAppStateTracker.addListener(this.mForceAppStandbyListener);
        }
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        try {
            close(this.mNativeData);
        } finally {
            super.finalize();
        }
    }

    /* access modifiers changed from: package-private */
    public boolean setTimeImpl(long millis) {
        boolean z = false;
        if (this.mNativeData == 0) {
            Slog.w(TAG, "Not setting time since no alarm driver is available.");
            return false;
        }
        synchronized (this.mLock) {
            HwLog.dubaie("DUBAI_TAG_TIME_CHANGED", "delta=" + (millis - System.currentTimeMillis()));
            if (setKernelTime(this.mNativeData, millis) == 0) {
                z = true;
            }
        }
        return z;
    }

    /* access modifiers changed from: package-private */
    public void setTimeZoneImpl(String tz) {
        if (!TextUtils.isEmpty(tz)) {
            TimeZone zone = TimeZone.getTimeZone(tz);
            boolean timeZoneWasChanged = false;
            synchronized (this) {
                String current = SystemProperties.get(TIMEZONE_PROPERTY);
                if (current == null || !current.equals(zone.getID())) {
                    Flog.i(500, "timezone changed: " + current + ", new=" + zone.getID());
                    timeZoneWasChanged = true;
                    SystemProperties.set(TIMEZONE_PROPERTY, zone.getID());
                }
                int gmtOffset = zone.getOffset(System.currentTimeMillis());
                setKernelTimezone(this.mNativeData, -(gmtOffset / 60000));
                if (!TextUtils.isEmpty(current) && timeZoneWasChanged) {
                    int oldGmtOffset = TimeZone.getTimeZone(current).getOffset(System.currentTimeMillis());
                    HwLog.dubaie("DUBAI_TAG_TIMEZONE_CHANGED", "oldId=" + current + " oldGmtOffset=" + oldGmtOffset + " newId=" + zone.getID() + " newGmtOffset=" + gmtOffset);
                }
            }
            TimeZone.setDefault(null);
            if (timeZoneWasChanged) {
                Intent intent = new Intent("android.intent.action.TIMEZONE_CHANGED");
                intent.addFlags(555745280);
                intent.putExtra("time-zone", zone.getID());
                getContext().sendBroadcastAsUser(intent, UserHandle.ALL);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void removeImpl(PendingIntent operation) {
        if (operation != null) {
            synchronized (this.mLock) {
                removeLocked(operation, (IAlarmListener) null);
            }
            removeDeskClockFromFWK(operation);
        }
    }

    /* access modifiers changed from: package-private */
    public void setImpl(int type, long triggerAtTime, long windowLength, long interval, PendingIntent operation, IAlarmListener directReceiver, String listenerTag, int flags, WorkSource workSource, AlarmManager.AlarmClockInfo alarmClock, int callingUid, String callingPackage) {
        long triggerAtTime2;
        long minInterval;
        long windowLength2;
        long maxElapsed;
        Object obj;
        int i = type;
        long j = triggerAtTime;
        long windowLength3 = windowLength;
        long j2 = interval;
        PendingIntent pendingIntent = operation;
        if (!(pendingIntent == null && directReceiver == null) && (pendingIntent == null || directReceiver == null)) {
            if (windowLength3 > AppStandbyController.SettingsObserver.DEFAULT_NOTIFICATION_TIMEOUT) {
                Slog.w(TAG, "Window length " + windowLength3 + "ms suspiciously long; limiting to 1 hour");
                windowLength3 = AppStandbyController.SettingsObserver.DEFAULT_STRONG_USAGE_TIMEOUT;
            }
            long minInterval2 = this.mConstants.MIN_INTERVAL;
            if (j2 > 0 && j2 < minInterval2) {
                Slog.w(TAG, "Suspiciously short interval " + j2 + " millis; expanding to " + (minInterval2 / 1000) + " seconds");
                j2 = minInterval2;
            } else if (j2 > this.mConstants.MAX_INTERVAL) {
                Slog.w(TAG, "Suspiciously long interval " + j2 + " millis; clamping");
                j2 = this.mConstants.MAX_INTERVAL;
            }
            long interval2 = j2;
            if (i < 0 || i > 3) {
                long j3 = minInterval2;
                throw new IllegalArgumentException("Invalid alarm type " + type);
            }
            if (j < 0) {
                Slog.w(TAG, "Invalid alarm trigger time! " + j + " from uid=" + callingUid + " pid=" + ((long) Binder.getCallingPid()));
                triggerAtTime2 = 0;
            } else {
                int i2 = callingUid;
                triggerAtTime2 = triggerAtTime;
            }
            long nowElapsed = SystemClock.elapsedRealtime();
            long nominalTrigger = convertToElapsed(triggerAtTime2, i);
            long minTrigger = nowElapsed + this.mConstants.MIN_FUTURITY;
            long triggerElapsed = nominalTrigger > minTrigger ? nominalTrigger : minTrigger;
            if (windowLength3 == 0) {
                maxElapsed = triggerElapsed;
                windowLength2 = windowLength3;
                long j4 = minInterval2;
                minInterval = triggerElapsed;
            } else {
                if (windowLength3 < 0) {
                    maxElapsed = maxTriggerTime(nowElapsed, triggerElapsed, interval2);
                    long j5 = minInterval2;
                    minInterval = triggerElapsed;
                    windowLength3 = maxElapsed - minInterval;
                } else {
                    minInterval = triggerElapsed;
                    maxElapsed = minInterval + windowLength3;
                }
                windowLength2 = windowLength3;
            }
            long maxElapsed2 = maxElapsed;
            Object obj2 = this.mLock;
            synchronized (obj2) {
                if (i == 0 || 2 == i) {
                    try {
                        Flog.i(500, "set(" + pendingIntent + ") : type=" + i + " triggerAtTime=" + triggerAtTime2 + " win=" + windowLength2 + " tElapsed=" + minInterval + " maxElapsed=" + maxElapsed2 + " interval=" + interval2 + " flags=0x" + Integer.toHexString(flags));
                    } catch (Throwable th) {
                        th = th;
                        throw th;
                    }
                }
                long j6 = triggerAtTime2;
                obj = obj2;
                long j7 = minInterval;
                setImplLocked(i, triggerAtTime2, minInterval, windowLength2, maxElapsed2, interval2, operation, directReceiver, listenerTag, flags, true, workSource, alarmClock, callingUid, callingPackage);
                return;
            }
        }
        Slog.w(TAG, "Alarms must either supply a PendingIntent or an AlarmReceiver");
    }

    private void setImplLocked(int type, long when, long whenElapsed, long windowLength, long maxWhen, long interval, PendingIntent operation, IAlarmListener directReceiver, String listenerTag, int flags, boolean doValidate, WorkSource workSource, AlarmManager.AlarmClockInfo alarmClock, int callingUid, String callingPackage) {
        int i = callingUid;
        Alarm alarm = new Alarm(type, when, whenElapsed, windowLength, maxWhen, interval, operation, directReceiver, listenerTag, workSource, flags, alarmClock, callingUid, callingPackage);
        Alarm a = alarm;
        try {
            int i2 = callingUid;
            try {
                if (ActivityManager.getService().isAppStartModeDisabled(i2, callingPackage)) {
                    Slog.w(TAG, "Not setting alarm from " + i2 + ":" + a + " -- package not allowed to start");
                    return;
                }
            } catch (RemoteException e) {
            }
        } catch (RemoteException e2) {
            int i3 = callingUid;
            String str = callingPackage;
        }
        if (!this.mCancelRemoveAction) {
            removeLocked(operation, directReceiver);
        } else {
            PendingIntent pendingIntent = operation;
            IAlarmListener iAlarmListener = directReceiver;
        }
        setImplLocked(a, false, doValidate);
    }

    private long getMinDelayForBucketLocked(int bucket) {
        int index;
        if (bucket == 50) {
            index = 4;
        } else if (bucket > 30) {
            index = 3;
        } else if (bucket > 20) {
            index = 2;
        } else if (bucket > 10) {
            index = 1;
        } else {
            index = 0;
        }
        return this.mConstants.APP_STANDBY_MIN_DELAYS[index];
    }

    private boolean adjustDeliveryTimeBasedOnStandbyBucketLocked(Alarm alarm) {
        Alarm alarm2 = alarm;
        if (isExemptFromAppStandby(alarm)) {
            return false;
        }
        if (!this.mAppStandbyParole) {
            long oldWhenElapsed = alarm2.whenElapsed;
            long oldMaxWhenElapsed = alarm2.maxWhenElapsed;
            String sourcePackage = alarm2.sourcePackage;
            int sourceUserId = UserHandle.getUserId(alarm2.creatorUid);
            int standbyBucket = this.mUsageStatsManagerInternal.getAppStandbyBucket(sourcePackage, sourceUserId, SystemClock.elapsedRealtime());
            long lastElapsed = ((Long) this.mLastAlarmDeliveredForPackage.getOrDefault(Pair.create(sourcePackage, Integer.valueOf(sourceUserId)), 0L)).longValue();
            if (lastElapsed > 0) {
                long minElapsed = getMinDelayForBucketLocked(standbyBucket) + lastElapsed;
                if (alarm2.expectedWhenElapsed < minElapsed) {
                    Slog.i(TAG, "adjustDeliveryTimeBasedOnStandbyBucketLocked alarm:" + alarm2 + ", standbyBucket:" + standbyBucket + ", minElapsed:" + minElapsed);
                    alarm2.maxWhenElapsed = minElapsed;
                    alarm2.whenElapsed = minElapsed;
                } else {
                    alarm2.whenElapsed = alarm2.expectedWhenElapsed;
                    alarm2.maxWhenElapsed = alarm2.expectedMaxWhenElapsed;
                }
            }
            return (oldWhenElapsed == alarm2.whenElapsed && oldMaxWhenElapsed == alarm2.maxWhenElapsed) ? false : true;
        } else if (alarm2.whenElapsed <= alarm2.expectedWhenElapsed) {
            return false;
        } else {
            alarm2.whenElapsed = alarm2.expectedWhenElapsed;
            alarm2.maxWhenElapsed = alarm2.expectedMaxWhenElapsed;
            return true;
        }
    }

    private void setImplLocked(Alarm a, boolean rebatching, boolean doValidate) {
        if (!this.mIsAlarmDataOnlyMode && isAwareAlarmManagerEnabled()) {
            modifyAlarmIfOverload(a);
        }
        if ((a.flags & 16) != 0) {
            if (this.mNextWakeFromIdle != null && a.whenElapsed > this.mNextWakeFromIdle.whenElapsed) {
                long j = this.mNextWakeFromIdle.whenElapsed;
                a.maxWhenElapsed = j;
                a.whenElapsed = j;
                a.when = j;
            }
            int fuzz = fuzzForDuration(a.whenElapsed - SystemClock.elapsedRealtime());
            if (fuzz > 0) {
                if (this.mRandom == null) {
                    this.mRandom = new Random();
                }
                a.whenElapsed -= (long) this.mRandom.nextInt(fuzz);
                long j2 = a.whenElapsed;
                a.maxWhenElapsed = j2;
                a.when = j2;
            }
        } else if (this.mPendingIdleUntil != null && (a.flags & 14) == 0) {
            this.mPendingWhileIdleAlarms.add(a);
            return;
        }
        adjustDeliveryTimeBasedOnStandbyBucketLocked(a);
        insertAndBatchAlarmLocked(a);
        if (a.alarmClock != null) {
            this.mNextAlarmClockMayChange = true;
        }
        boolean needRebatch = false;
        if ((a.flags & 16) != 0) {
            if (!(this.mPendingIdleUntil == a || this.mPendingIdleUntil == null)) {
                Slog.wtfStack(TAG, "setImplLocked: idle until changed from " + this.mPendingIdleUntil + " to " + a);
            }
            this.mPendingIdleUntil = a;
            needRebatch = true;
        } else if ((a.flags & 2) != 0 && (this.mNextWakeFromIdle == null || this.mNextWakeFromIdle.whenElapsed > a.whenElapsed)) {
            this.mNextWakeFromIdle = a;
            if (this.mPendingIdleUntil != null) {
                needRebatch = true;
            }
        }
        if (!rebatching) {
            if (needRebatch) {
                rebatchAllAlarmsLocked(false);
            }
            hwSetRtcAlarm(a);
            rescheduleKernelAlarmsLocked();
            updateNextAlarmClockLocked();
        }
    }

    /* access modifiers changed from: private */
    public int getWakeUpNumImpl(int uid, String pkg) {
        int i;
        synchronized (this.mLock) {
            ArrayMap<String, BroadcastStats> uidStats = this.mBroadcastStats.get(uid);
            if (uidStats == null) {
                uidStats = new ArrayMap<>();
                this.mBroadcastStats.put(uid, uidStats);
            }
            BroadcastStats bs = uidStats.get(pkg);
            if (bs == null) {
                bs = new BroadcastStats(uid, pkg);
                uidStats.put(pkg, bs);
            }
            i = bs.numWakeup;
        }
        return i;
    }

    /* access modifiers changed from: package-private */
    public void dumpImpl(PrintWriter pw) {
        SimpleDateFormat sdf;
        int pos;
        ArrayMap<String, BroadcastStats> uidStats;
        BroadcastStats bs;
        int i;
        long nextWakeupRTC;
        Iterator<Integer> it;
        long nextWakeupRTC2;
        PrintWriter printWriter = pw;
        synchronized (this.mLock) {
            printWriter.println("Current Alarm Manager state:");
            this.mConstants.dump(printWriter);
            pw.println();
            if (this.mAppStateTracker != null) {
                this.mAppStateTracker.dump(printWriter, "  ");
                pw.println();
            }
            printWriter.println("  App Standby Parole: " + this.mAppStandbyParole);
            pw.println();
            long nowRTC = System.currentTimeMillis();
            long nowELAPSED = SystemClock.elapsedRealtime();
            long nowUPTIME = SystemClock.uptimeMillis();
            SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            printWriter.print("  nowRTC=");
            printWriter.print(nowRTC);
            printWriter.print("=");
            printWriter.print(sdf2.format(new Date(nowRTC)));
            printWriter.print(" nowELAPSED=");
            printWriter.print(nowELAPSED);
            pw.println();
            printWriter.print("  mLastTimeChangeClockTime=");
            printWriter.print(this.mLastTimeChangeClockTime);
            printWriter.print("=");
            printWriter.println(sdf2.format(new Date(this.mLastTimeChangeClockTime)));
            printWriter.print("  mLastTimeChangeRealtime=");
            printWriter.println(this.mLastTimeChangeRealtime);
            printWriter.print("  mLastTickIssued=");
            printWriter.println(sdf2.format(new Date(nowRTC - (nowELAPSED - this.mLastTickIssued))));
            printWriter.print("  mLastTickReceived=");
            printWriter.println(sdf2.format(new Date(this.mLastTickReceived)));
            printWriter.print("  mLastTickSet=");
            printWriter.println(sdf2.format(new Date(this.mLastTickSet)));
            printWriter.print("  mLastTickAdded=");
            printWriter.println(sdf2.format(new Date(this.mLastTickAdded)));
            printWriter.print("  mLastTickRemoved=");
            printWriter.println(sdf2.format(new Date(this.mLastTickRemoved)));
            SystemServiceManager ssm = (SystemServiceManager) LocalServices.getService(SystemServiceManager.class);
            if (ssm != null) {
                pw.println();
                printWriter.print("  RuntimeStarted=");
                printWriter.print(sdf2.format(new Date((nowRTC - nowELAPSED) + ssm.getRuntimeStartElapsedTime())));
                if (ssm.isRuntimeRestarted()) {
                    printWriter.print("  (Runtime restarted)");
                }
                pw.println();
                printWriter.print("  Runtime uptime (elapsed): ");
                TimeUtils.formatDuration(nowELAPSED, ssm.getRuntimeStartElapsedTime(), printWriter);
                pw.println();
                printWriter.print("  Runtime uptime (uptime): ");
                TimeUtils.formatDuration(nowUPTIME, ssm.getRuntimeStartUptime(), printWriter);
                pw.println();
            }
            pw.println();
            if (!this.mInteractive) {
                printWriter.print("  Time since non-interactive: ");
                TimeUtils.formatDuration(nowELAPSED - this.mNonInteractiveStartTime, printWriter);
                pw.println();
            }
            printWriter.print("  Max wakeup delay: ");
            TimeUtils.formatDuration(currentNonWakeupFuzzLocked(nowELAPSED), printWriter);
            pw.println();
            printWriter.print("  Time since last dispatch: ");
            TimeUtils.formatDuration(nowELAPSED - this.mLastAlarmDeliveryTime, printWriter);
            pw.println();
            printWriter.print("  Next non-wakeup delivery time: ");
            TimeUtils.formatDuration(nowELAPSED - this.mNextNonWakeupDeliveryTime, printWriter);
            pw.println();
            long nowRTC2 = nowUPTIME;
            long nowRTC3 = this.mNextWakeup + (nowRTC - nowELAPSED);
            long nowRTC4 = nowRTC;
            long nextNonWakeupRTC = this.mNextNonWakeup + (nowRTC - nowELAPSED);
            printWriter.print("  Next non-wakeup alarm: ");
            TimeUtils.formatDuration(this.mNextNonWakeup, nowELAPSED, printWriter);
            printWriter.print(" = ");
            printWriter.print(this.mNextNonWakeup);
            printWriter.print(" = ");
            printWriter.println(sdf2.format(new Date(nextNonWakeupRTC)));
            printWriter.print("  Next wakeup alarm: ");
            TimeUtils.formatDuration(this.mNextWakeup, nowELAPSED, printWriter);
            printWriter.print(" = ");
            printWriter.print(this.mNextWakeup);
            printWriter.print(" = ");
            printWriter.println(sdf2.format(new Date(nowRTC3)));
            printWriter.print("    set at ");
            TimeUtils.formatDuration(this.mLastWakeupSet, nowELAPSED, printWriter);
            pw.println();
            printWriter.print("  Last wakeup: ");
            TimeUtils.formatDuration(this.mLastWakeup, nowELAPSED, printWriter);
            printWriter.print(" = ");
            printWriter.println(this.mLastWakeup);
            printWriter.print("  Last trigger: ");
            TimeUtils.formatDuration(this.mLastTrigger, nowELAPSED, printWriter);
            printWriter.print(" = ");
            printWriter.println(this.mLastTrigger);
            printWriter.print("  Num time change events: ");
            printWriter.println(this.mNumTimeChanged);
            pw.println();
            printWriter.println("  Next alarm clock information: ");
            TreeSet<Integer> users = new TreeSet<>();
            for (int i2 = 0; i2 < this.mNextAlarmClockForUser.size(); i2++) {
                users.add(Integer.valueOf(this.mNextAlarmClockForUser.keyAt(i2)));
            }
            for (int i3 = 0; i3 < this.mPendingSendNextAlarmClockChangedForUser.size(); i3++) {
                users.add(Integer.valueOf(this.mPendingSendNextAlarmClockChangedForUser.keyAt(i3)));
            }
            Iterator<Integer> it2 = users.iterator();
            while (true) {
                long nextNonWakeupRTC2 = nextNonWakeupRTC;
                if (!it2.hasNext()) {
                    break;
                }
                int user = it2.next().intValue();
                AlarmManager.AlarmClockInfo next = this.mNextAlarmClockForUser.get(user);
                long time = next != null ? next.getTriggerTime() : 0;
                boolean pendingSend = this.mPendingSendNextAlarmClockChangedForUser.get(user);
                printWriter.print("    user:");
                printWriter.print(user);
                printWriter.print(" pendingSend:");
                printWriter.print(pendingSend);
                printWriter.print(" time:");
                long time2 = time;
                printWriter.print(time2);
                if (time2 > 0) {
                    it = it2;
                    printWriter.print(" = ");
                    printWriter.print(sdf2.format(new Date(time2)));
                    printWriter.print(" = ");
                    nextWakeupRTC = nowRTC3;
                    nextWakeupRTC2 = nowRTC4;
                    TimeUtils.formatDuration(time2, nextWakeupRTC2, printWriter);
                } else {
                    it = it2;
                    nextWakeupRTC = nowRTC3;
                    nextWakeupRTC2 = nowRTC4;
                }
                pw.println();
                nowRTC4 = nextWakeupRTC2;
                nextNonWakeupRTC = nextNonWakeupRTC2;
                it2 = it;
                nowRTC3 = nextWakeupRTC;
            }
            long nextWakeupRTC3 = nowRTC3;
            long nowRTC5 = nowRTC4;
            if (this.mAlarmBatches.size() > 0) {
                pw.println();
                printWriter.print("  Pending alarm batches: ");
                printWriter.println(this.mAlarmBatches.size());
                for (Iterator<Batch> it3 = this.mAlarmBatches.iterator(); it3.hasNext(); it3 = it3) {
                    Batch b = it3.next();
                    printWriter.print(b);
                    printWriter.println(':');
                    dumpAlarmList(printWriter, b.alarms, "    ", nowELAPSED, nowRTC5, sdf2);
                    nowRTC5 = nowRTC5;
                    users = users;
                    ssm = ssm;
                    nowRTC2 = nowRTC2;
                    nextWakeupRTC3 = nextWakeupRTC3;
                }
            }
            SystemServiceManager systemServiceManager = ssm;
            long j = nowRTC2;
            long j2 = nextWakeupRTC3;
            int i4 = 0;
            long nowRTC6 = nowRTC5;
            pw.println();
            printWriter.println("  Pending user blocked background alarms: ");
            boolean blocked = false;
            int i5 = 0;
            while (true) {
                int i6 = i5;
                if (i6 >= this.mPendingBackgroundAlarms.size()) {
                    break;
                }
                ArrayList<Alarm> blockedAlarms = this.mPendingBackgroundAlarms.valueAt(i6);
                if (blockedAlarms == null || blockedAlarms.size() <= 0) {
                    i = i6;
                } else {
                    blocked = true;
                    ArrayList<Alarm> arrayList = blockedAlarms;
                    i = i6;
                    dumpAlarmList(printWriter, blockedAlarms, "    ", nowELAPSED, nowRTC6, sdf2);
                }
                i5 = i + 1;
            }
            if (!blocked) {
                printWriter.println("    none");
            }
            printWriter.println("  mLastAlarmDeliveredForPackage:");
            for (int i7 = 0; i7 < this.mLastAlarmDeliveredForPackage.size(); i7++) {
                Pair<String, Integer> packageUser = this.mLastAlarmDeliveredForPackage.keyAt(i7);
                printWriter.print("    Package " + ((String) packageUser.first) + ", User " + packageUser.second + ":");
                TimeUtils.formatDuration(this.mLastAlarmDeliveredForPackage.valueAt(i7).longValue(), nowELAPSED, printWriter);
                pw.println();
            }
            pw.println();
            if (this.mPendingIdleUntil != null || this.mPendingWhileIdleAlarms.size() > 0) {
                pw.println();
                printWriter.println("    Idle mode state:");
                printWriter.print("      Idling until: ");
                if (this.mPendingIdleUntil != null) {
                    printWriter.println(this.mPendingIdleUntil);
                    this.mPendingIdleUntil.dump(printWriter, "        ", nowELAPSED, nowRTC6, sdf2);
                } else {
                    printWriter.println("null");
                }
                printWriter.println("      Pending alarms:");
                dumpAlarmList(printWriter, this.mPendingWhileIdleAlarms, "      ", nowELAPSED, nowRTC6, sdf2);
            }
            if (this.mNextWakeFromIdle != null) {
                pw.println();
                printWriter.print("  Next wake from idle: ");
                printWriter.println(this.mNextWakeFromIdle);
                this.mNextWakeFromIdle.dump(printWriter, "    ", nowELAPSED, nowRTC6, sdf2);
            }
            pw.println();
            printWriter.print("  Past-due non-wakeup alarms: ");
            if (this.mPendingNonWakeupAlarms.size() > 0) {
                printWriter.println(this.mPendingNonWakeupAlarms.size());
                dumpAlarmList(printWriter, this.mPendingNonWakeupAlarms, "    ", nowELAPSED, nowRTC6, sdf2);
            } else {
                printWriter.println("(none)");
            }
            printWriter.print("    Number of delayed alarms: ");
            printWriter.print(this.mNumDelayedAlarms);
            printWriter.print(", total delay time: ");
            TimeUtils.formatDuration(this.mTotalDelayTime, printWriter);
            pw.println();
            printWriter.print("    Max delay time: ");
            TimeUtils.formatDuration(this.mMaxDelayTime, printWriter);
            printWriter.print(", max non-interactive time: ");
            TimeUtils.formatDuration(this.mNonInteractiveTime, printWriter);
            pw.println();
            pw.println();
            printWriter.print("  Broadcast ref count: ");
            printWriter.println(this.mBroadcastRefCount);
            printWriter.print("  PendingIntent send count: ");
            printWriter.println(this.mSendCount);
            printWriter.print("  PendingIntent finish count: ");
            printWriter.println(this.mSendFinishCount);
            printWriter.print("  Listener send count: ");
            printWriter.println(this.mListenerCount);
            printWriter.print("  Listener finish count: ");
            printWriter.println(this.mListenerFinishCount);
            pw.println();
            if (this.mInFlight.size() > 0) {
                printWriter.println("Outstanding deliveries:");
                for (int i8 = 0; i8 < this.mInFlight.size(); i8++) {
                    if (this.mInFlight.get(i8) != null) {
                        printWriter.print("   #");
                        printWriter.print(i8);
                        printWriter.print(": ");
                        printWriter.print(this.mInFlight.get(i8));
                        printWriter.print("##" + this.mInFlight.get(i8).mUid);
                        printWriter.print("##" + this.mInFlight.get(i8).mTag);
                        printWriter.print("\n");
                    }
                }
                pw.println();
            }
            if (this.mLastAllowWhileIdleDispatch.size() > 0) {
                printWriter.println("  Last allow while idle dispatch times:");
                for (int i9 = 0; i9 < this.mLastAllowWhileIdleDispatch.size(); i9++) {
                    printWriter.print("    UID ");
                    int uid = this.mLastAllowWhileIdleDispatch.keyAt(i9);
                    UserHandle.formatUid(printWriter, uid);
                    printWriter.print(": ");
                    long lastTime = this.mLastAllowWhileIdleDispatch.valueAt(i9);
                    TimeUtils.formatDuration(lastTime, nowELAPSED, printWriter);
                    long minInterval = getWhileIdleMinIntervalLocked(uid);
                    printWriter.print("  Next allowed:");
                    TimeUtils.formatDuration(lastTime + minInterval, nowELAPSED, printWriter);
                    printWriter.print(" (");
                    TimeUtils.formatDuration(minInterval, 0, printWriter);
                    printWriter.print(")");
                    pw.println();
                }
            }
            printWriter.print("  mUseAllowWhileIdleShortTime: [");
            for (int i10 = 0; i10 < this.mUseAllowWhileIdleShortTime.size(); i10++) {
                if (this.mUseAllowWhileIdleShortTime.valueAt(i10)) {
                    UserHandle.formatUid(printWriter, this.mUseAllowWhileIdleShortTime.keyAt(i10));
                    printWriter.print(" ");
                }
            }
            printWriter.println("]");
            pw.println();
            if (this.mLog.dump(printWriter, "  Recent problems", "    ")) {
                pw.println();
            }
            FilterStats[] topFilters = new FilterStats[10];
            Comparator<FilterStats> comparator = new Comparator<FilterStats>() {
                public int compare(FilterStats lhs, FilterStats rhs) {
                    if (lhs.aggregateTime < rhs.aggregateTime) {
                        return 1;
                    }
                    if (lhs.aggregateTime > rhs.aggregateTime) {
                        return -1;
                    }
                    return 0;
                }
            };
            int len = 0;
            int iu = 0;
            while (iu < this.mBroadcastStats.size()) {
                ArrayMap<String, BroadcastStats> uidStats2 = this.mBroadcastStats.valueAt(iu);
                int len2 = len;
                int ip = i4;
                while (ip < uidStats2.size()) {
                    BroadcastStats bs2 = uidStats2.valueAt(ip);
                    int len3 = len2;
                    int is = i4;
                    while (is < bs2.filterStats.size()) {
                        FilterStats fs = bs2.filterStats.valueAt(is);
                        if (len3 > 0) {
                            sdf = sdf2;
                            pos = Arrays.binarySearch(topFilters, 0, len3, fs, comparator);
                        } else {
                            sdf = sdf2;
                            pos = 0;
                        }
                        int pos2 = pos;
                        if (pos2 < 0) {
                            uidStats = uidStats2;
                            pos2 = (-pos2) - 1;
                        } else {
                            uidStats = uidStats2;
                        }
                        if (pos2 < topFilters.length) {
                            int copylen = (topFilters.length - pos2) - 1;
                            if (copylen > 0) {
                                bs = bs2;
                                System.arraycopy(topFilters, pos2, topFilters, pos2 + 1, copylen);
                            } else {
                                bs = bs2;
                            }
                            topFilters[pos2] = fs;
                            if (len3 < topFilters.length) {
                                len3++;
                            }
                        } else {
                            bs = bs2;
                        }
                        is++;
                        sdf2 = sdf;
                        uidStats2 = uidStats;
                        bs2 = bs;
                    }
                    ArrayMap<String, BroadcastStats> arrayMap = uidStats2;
                    ip++;
                    len2 = len3;
                    i4 = 0;
                }
                iu++;
                len = len2;
                i4 = 0;
            }
            if (len > 0) {
                printWriter.println("  Top Alarms:");
                for (int i11 = 0; i11 < len; i11++) {
                    FilterStats fs2 = topFilters[i11];
                    printWriter.print("    ");
                    if (fs2.nesting > 0) {
                        printWriter.print("*ACTIVE* ");
                    }
                    TimeUtils.formatDuration(fs2.aggregateTime, printWriter);
                    printWriter.print(" running, ");
                    printWriter.print(fs2.numWakeup);
                    printWriter.print(" wakeups, ");
                    printWriter.print(fs2.count);
                    printWriter.print(" alarms: ");
                    UserHandle.formatUid(printWriter, fs2.mBroadcastStats.mUid);
                    printWriter.print(":");
                    printWriter.print(fs2.mBroadcastStats.mPackageName);
                    pw.println();
                    printWriter.print("      ");
                    printWriter.print(fs2.mTag);
                    pw.println();
                }
            }
            printWriter.println(" ");
            printWriter.println("  Alarm Stats:");
            ArrayList<FilterStats> tmpFilters = new ArrayList<>();
            for (int iu2 = 0; iu2 < this.mBroadcastStats.size(); iu2++) {
                ArrayMap<String, BroadcastStats> uidStats3 = this.mBroadcastStats.valueAt(iu2);
                int ip2 = 0;
                while (ip2 < uidStats3.size()) {
                    BroadcastStats bs3 = uidStats3.valueAt(ip2);
                    printWriter.print("  ");
                    if (bs3.nesting > 0) {
                        printWriter.print("*ACTIVE* ");
                    }
                    UserHandle.formatUid(printWriter, bs3.mUid);
                    printWriter.print(":");
                    printWriter.print(bs3.mPackageName);
                    printWriter.print(" ");
                    int len4 = len;
                    ArrayMap<String, BroadcastStats> uidStats4 = uidStats3;
                    TimeUtils.formatDuration(bs3.aggregateTime, printWriter);
                    printWriter.print(" running, ");
                    printWriter.print(bs3.numWakeup);
                    printWriter.println(" wakeups:");
                    tmpFilters.clear();
                    for (int is2 = 0; is2 < bs3.filterStats.size(); is2++) {
                        tmpFilters.add(bs3.filterStats.valueAt(is2));
                    }
                    Collections.sort(tmpFilters, comparator);
                    int i12 = 0;
                    while (i12 < tmpFilters.size()) {
                        FilterStats fs3 = tmpFilters.get(i12);
                        printWriter.print("    ");
                        if (fs3.nesting > 0) {
                            printWriter.print("*ACTIVE* ");
                        }
                        TimeUtils.formatDuration(fs3.aggregateTime, printWriter);
                        printWriter.print(" ");
                        printWriter.print(fs3.numWakeup);
                        printWriter.print(" wakes ");
                        printWriter.print(fs3.count);
                        printWriter.print(" alarms, last ");
                        TimeUtils.formatDuration(fs3.lastTime, nowELAPSED, printWriter);
                        printWriter.println(":");
                        printWriter.print("      ");
                        printWriter.print(fs3.mTag);
                        pw.println();
                        i12++;
                        topFilters = topFilters;
                        comparator = comparator;
                    }
                    Comparator<FilterStats> comparator2 = comparator;
                    ip2++;
                    len = len4;
                    uidStats3 = uidStats4;
                }
                Comparator<FilterStats> comparator3 = comparator;
                int i13 = len;
            }
            Comparator<FilterStats> comparator4 = comparator;
            int i14 = len;
            pw.println();
            this.mStatLogger.dump(printWriter, "  ");
            printHwWakeupBoot(pw);
        }
    }

    /* access modifiers changed from: package-private */
    public void dumpProto(FileDescriptor fd) {
        TreeSet<Integer> users;
        AlarmManagerService alarmManagerService = this;
        ProtoOutputStream proto = new ProtoOutputStream(fd);
        synchronized (alarmManagerService.mLock) {
            long nowRTC = System.currentTimeMillis();
            long nowElapsed = SystemClock.elapsedRealtime();
            proto.write(1112396529665L, nowRTC);
            proto.write(1112396529666L, nowElapsed);
            proto.write(1112396529667L, alarmManagerService.mLastTimeChangeClockTime);
            proto.write(1112396529668L, alarmManagerService.mLastTimeChangeRealtime);
            alarmManagerService.mConstants.dumpProto(proto, 1146756268037L);
            if (alarmManagerService.mAppStateTracker != null) {
                alarmManagerService.mAppStateTracker.dumpProto(proto, 1146756268038L);
            }
            proto.write(1133871366151L, alarmManagerService.mInteractive);
            if (!alarmManagerService.mInteractive) {
                proto.write(1112396529672L, nowElapsed - alarmManagerService.mNonInteractiveStartTime);
                proto.write(1112396529673L, alarmManagerService.currentNonWakeupFuzzLocked(nowElapsed));
                proto.write(1112396529674L, nowElapsed - alarmManagerService.mLastAlarmDeliveryTime);
                proto.write(1112396529675L, nowElapsed - alarmManagerService.mNextNonWakeupDeliveryTime);
            }
            proto.write(1112396529676L, alarmManagerService.mNextNonWakeup - nowElapsed);
            proto.write(1112396529677L, alarmManagerService.mNextWakeup - nowElapsed);
            proto.write(1112396529678L, nowElapsed - alarmManagerService.mLastWakeup);
            proto.write(1112396529679L, nowElapsed - alarmManagerService.mLastWakeupSet);
            proto.write(1112396529680L, alarmManagerService.mNumTimeChanged);
            TreeSet<Integer> users2 = new TreeSet<>();
            int nextAlarmClockForUserSize = alarmManagerService.mNextAlarmClockForUser.size();
            for (int i = 0; i < nextAlarmClockForUserSize; i++) {
                users2.add(Integer.valueOf(alarmManagerService.mNextAlarmClockForUser.keyAt(i)));
            }
            int pendingSendNextAlarmClockChangedForUserSize = alarmManagerService.mPendingSendNextAlarmClockChangedForUser.size();
            for (int i2 = 0; i2 < pendingSendNextAlarmClockChangedForUserSize; i2++) {
                users2.add(Integer.valueOf(alarmManagerService.mPendingSendNextAlarmClockChangedForUser.keyAt(i2)));
            }
            Iterator<Integer> it = users2.iterator();
            while (it.hasNext()) {
                int user = it.next().intValue();
                AlarmManager.AlarmClockInfo next = alarmManagerService.mNextAlarmClockForUser.get(user);
                long time = next != null ? next.getTriggerTime() : 0;
                boolean pendingSend = alarmManagerService.mPendingSendNextAlarmClockChangedForUser.get(user);
                long aToken = proto.start(2246267895826L);
                proto.write(1120986464257L, user);
                proto.write(1133871366146L, pendingSend);
                proto.write(1112396529667L, time);
                proto.end(aToken);
                it = it;
                nextAlarmClockForUserSize = nextAlarmClockForUserSize;
                nowRTC = nowRTC;
                FileDescriptor fileDescriptor = fd;
            }
            int nextAlarmClockForUserSize2 = nextAlarmClockForUserSize;
            long nowRTC2 = nowRTC;
            long j = 1120986464257L;
            Iterator<Batch> it2 = alarmManagerService.mAlarmBatches.iterator();
            while (it2.hasNext()) {
                it2.next().writeToProto(proto, 2246267895827L, nowElapsed, nowRTC2);
                j = j;
                nextAlarmClockForUserSize2 = nextAlarmClockForUserSize2;
                nowElapsed = nowElapsed;
                pendingSendNextAlarmClockChangedForUserSize = pendingSendNextAlarmClockChangedForUserSize;
            }
            long j2 = j;
            long nowElapsed2 = nowElapsed;
            int i3 = nextAlarmClockForUserSize2;
            for (int i4 = 0; i4 < alarmManagerService.mPendingBackgroundAlarms.size(); i4++) {
                ArrayList<Alarm> blockedAlarms = alarmManagerService.mPendingBackgroundAlarms.valueAt(i4);
                if (blockedAlarms != null) {
                    for (Iterator<Alarm> it3 = blockedAlarms.iterator(); it3.hasNext(); it3 = it3) {
                        it3.next().writeToProto(proto, 2246267895828L, nowElapsed2, nowRTC2);
                        blockedAlarms = blockedAlarms;
                    }
                }
            }
            if (alarmManagerService.mPendingIdleUntil != null) {
                alarmManagerService.mPendingIdleUntil.writeToProto(proto, 1146756268053L, nowElapsed2, nowRTC2);
            }
            Iterator<Alarm> it4 = alarmManagerService.mPendingWhileIdleAlarms.iterator();
            while (it4.hasNext()) {
                it4.next().writeToProto(proto, 2246267895830L, nowElapsed2, nowRTC2);
            }
            if (alarmManagerService.mNextWakeFromIdle != null) {
                alarmManagerService.mNextWakeFromIdle.writeToProto(proto, 1146756268055L, nowElapsed2, nowRTC2);
            }
            Iterator<Alarm> it5 = alarmManagerService.mPendingNonWakeupAlarms.iterator();
            while (it5.hasNext()) {
                it5.next().writeToProto(proto, 2246267895832L, nowElapsed2, nowRTC2);
            }
            proto.write(1120986464281L, alarmManagerService.mNumDelayedAlarms);
            proto.write(1112396529690L, alarmManagerService.mTotalDelayTime);
            proto.write(1112396529691L, alarmManagerService.mMaxDelayTime);
            proto.write(1112396529692L, alarmManagerService.mNonInteractiveTime);
            proto.write(1120986464285L, alarmManagerService.mBroadcastRefCount);
            proto.write(1120986464286L, alarmManagerService.mSendCount);
            proto.write(1120986464287L, alarmManagerService.mSendFinishCount);
            proto.write(1120986464288L, alarmManagerService.mListenerCount);
            proto.write(1120986464289L, alarmManagerService.mListenerFinishCount);
            Iterator<InFlight> it6 = alarmManagerService.mInFlight.iterator();
            while (it6.hasNext()) {
                it6.next().writeToProto(proto, 2246267895842L);
            }
            int i5 = 0;
            while (i5 < alarmManagerService.mLastAllowWhileIdleDispatch.size()) {
                long token = proto.start(2246267895844L);
                int uid = alarmManagerService.mLastAllowWhileIdleDispatch.keyAt(i5);
                long lastTime = alarmManagerService.mLastAllowWhileIdleDispatch.valueAt(i5);
                proto.write(j2, uid);
                proto.write(1112396529666L, lastTime);
                proto.write(1112396529667L, lastTime + alarmManagerService.getWhileIdleMinIntervalLocked(uid));
                proto.end(token);
                i5++;
                j2 = 1120986464257L;
            }
            for (int i6 = 0; i6 < alarmManagerService.mUseAllowWhileIdleShortTime.size(); i6++) {
                if (alarmManagerService.mUseAllowWhileIdleShortTime.valueAt(i6)) {
                    proto.write(2220498092067L, alarmManagerService.mUseAllowWhileIdleShortTime.keyAt(i6));
                }
            }
            alarmManagerService.mLog.writeToProto(proto, 1146756268069L);
            FilterStats[] topFilters = new FilterStats[10];
            Comparator<FilterStats> comparator = new Comparator<FilterStats>() {
                public int compare(FilterStats lhs, FilterStats rhs) {
                    if (lhs.aggregateTime < rhs.aggregateTime) {
                        return 1;
                    }
                    if (lhs.aggregateTime > rhs.aggregateTime) {
                        return -1;
                    }
                    return 0;
                }
            };
            int len = 0;
            int iu = 0;
            while (iu < alarmManagerService.mBroadcastStats.size()) {
                ArrayMap<String, BroadcastStats> uidStats = alarmManagerService.mBroadcastStats.valueAt(iu);
                int len2 = len;
                int ip = 0;
                while (ip < uidStats.size()) {
                    BroadcastStats bs = uidStats.valueAt(ip);
                    int len3 = len2;
                    int is = 0;
                    while (is < bs.filterStats.size()) {
                        FilterStats fs = bs.filterStats.valueAt(is);
                        int pos = len3 > 0 ? Arrays.binarySearch(topFilters, 0, len3, fs, comparator) : 0;
                        if (pos < 0) {
                            pos = (-pos) - 1;
                        }
                        if (pos < topFilters.length) {
                            int copylen = (topFilters.length - pos) - 1;
                            if (copylen > 0) {
                                users = users2;
                                System.arraycopy(topFilters, pos, topFilters, pos + 1, copylen);
                            } else {
                                users = users2;
                            }
                            topFilters[pos] = fs;
                            if (len3 < topFilters.length) {
                                len3++;
                            }
                        } else {
                            users = users2;
                        }
                        is++;
                        users2 = users;
                    }
                    ip++;
                    len2 = len3;
                }
                iu++;
                len = len2;
            }
            for (int i7 = 0; i7 < len; i7++) {
                long token2 = proto.start(2246267895846L);
                FilterStats fs2 = topFilters[i7];
                proto.write(1120986464257L, fs2.mBroadcastStats.mUid);
                proto.write(1138166333442L, fs2.mBroadcastStats.mPackageName);
                fs2.writeToProto(proto, 1146756268035L);
                proto.end(token2);
            }
            ArrayList<FilterStats> tmpFilters = new ArrayList<>();
            int iu2 = 0;
            while (iu2 < alarmManagerService.mBroadcastStats.size()) {
                ArrayMap<String, BroadcastStats> uidStats2 = alarmManagerService.mBroadcastStats.valueAt(iu2);
                int ip2 = 0;
                while (ip2 < uidStats2.size()) {
                    long token3 = proto.start(2246267895847L);
                    BroadcastStats bs2 = uidStats2.valueAt(ip2);
                    bs2.writeToProto(proto, 1146756268033L);
                    tmpFilters.clear();
                    for (int is2 = 0; is2 < bs2.filterStats.size(); is2++) {
                        tmpFilters.add(bs2.filterStats.valueAt(is2));
                    }
                    Collections.sort(tmpFilters, comparator);
                    Iterator<FilterStats> it7 = tmpFilters.iterator();
                    while (it7.hasNext()) {
                        it7.next().writeToProto(proto, 2246267895810L);
                        tmpFilters = tmpFilters;
                    }
                    proto.end(token3);
                    ip2++;
                    tmpFilters = tmpFilters;
                }
                iu2++;
                alarmManagerService = this;
            }
        }
        proto.flush();
    }

    private void logBatchesLocked(SimpleDateFormat sdf) {
        ByteArrayOutputStream bs = new ByteArrayOutputStream(2048);
        PrintWriter pw = new PrintWriter(bs);
        long nowRTC = System.currentTimeMillis();
        long nowELAPSED = SystemClock.elapsedRealtime();
        int NZ = this.mAlarmBatches.size();
        int iz = 0;
        while (true) {
            int iz2 = iz;
            if (iz2 < NZ) {
                Batch bz = this.mAlarmBatches.get(iz2);
                pw.append("Batch ");
                pw.print(iz2);
                pw.append(": ");
                pw.println(bz);
                Batch batch = bz;
                dumpAlarmList(pw, bz.alarms, "  ", nowELAPSED, nowRTC, sdf);
                pw.flush();
                Slog.v(TAG, bs.toString());
                bs.reset();
                iz = iz2 + 1;
            } else {
                return;
            }
        }
    }

    private boolean validateConsistencyLocked() {
        return true;
    }

    private Batch findFirstWakeupBatchLocked() {
        int N = this.mAlarmBatches.size();
        int i = 0;
        while (i < N) {
            try {
                Batch b = this.mAlarmBatches.get(i);
                if (b.hasWakeups()) {
                    return b;
                }
                i++;
            } catch (IndexOutOfBoundsException e) {
                Log.d(TAG, "Do nothing");
            }
        }
        return null;
    }

    private long findFirstWakeupEndLocked() {
        int N = this.mAlarmBatches.size();
        long lastWakeupEnd = -1;
        for (int i = 0; i < N; i++) {
            try {
                Batch b = this.mAlarmBatches.get(i);
                if (b.hasWakeups()) {
                    int size = b.alarms.size();
                    for (int j = 0; j < size; j++) {
                        Alarm a = b.alarms.get(j);
                        if (a.wakeup) {
                            if (lastWakeupEnd == -1) {
                                lastWakeupEnd = a.maxWhenElapsed;
                            } else if (a.whenElapsed > lastWakeupEnd) {
                                return lastWakeupEnd;
                            } else {
                                if (a.maxWhenElapsed < lastWakeupEnd) {
                                    lastWakeupEnd = a.maxWhenElapsed;
                                }
                            }
                        }
                    }
                    continue;
                } else {
                    continue;
                }
            } catch (IndexOutOfBoundsException e) {
                Log.d(TAG, "Do nothing");
            }
        }
        return lastWakeupEnd;
    }

    /* access modifiers changed from: package-private */
    public long getNextWakeFromIdleTimeImpl() {
        long j;
        synchronized (this.mLock) {
            j = this.mNextWakeFromIdle != null ? this.mNextWakeFromIdle.whenElapsed : JobStatus.NO_LATEST_RUNTIME;
        }
        return j;
    }

    /* access modifiers changed from: package-private */
    public AlarmManager.AlarmClockInfo getNextAlarmClockImpl(int userId) {
        AlarmManager.AlarmClockInfo alarmClockInfo;
        synchronized (this.mLock) {
            alarmClockInfo = this.mNextAlarmClockForUser.get(userId);
        }
        return alarmClockInfo;
    }

    /* access modifiers changed from: package-private */
    public void updateNextAlarmClockLocked() {
        if (this.mNextAlarmClockMayChange) {
            this.mNextAlarmClockMayChange = false;
            SparseArray<AlarmManager.AlarmClockInfo> nextForUser = this.mTmpSparseAlarmClockArray;
            nextForUser.clear();
            int N = this.mAlarmBatches.size();
            for (int i = 0; i < N; i++) {
                ArrayList<Alarm> alarms = this.mAlarmBatches.get(i).alarms;
                int M = alarms.size();
                for (int j = 0; j < M; j++) {
                    Alarm a = alarms.get(j);
                    if (a.alarmClock != null) {
                        int userId = UserHandle.getUserId(a.uid);
                        AlarmManager.AlarmClockInfo current = this.mNextAlarmClockForUser.get(userId);
                        if (nextForUser.get(userId) == null) {
                            nextForUser.put(userId, a.alarmClock);
                        } else if (a.alarmClock.equals(current) && current.getTriggerTime() <= nextForUser.get(userId).getTriggerTime()) {
                            nextForUser.put(userId, current);
                        }
                    }
                }
            }
            int i2 = nextForUser.size();
            for (int i3 = 0; i3 < i2; i3++) {
                AlarmManager.AlarmClockInfo newAlarm = nextForUser.valueAt(i3);
                int userId2 = nextForUser.keyAt(i3);
                if (!newAlarm.equals(this.mNextAlarmClockForUser.get(userId2))) {
                    updateNextAlarmInfoForUserLocked(userId2, newAlarm);
                }
            }
            for (int i4 = this.mNextAlarmClockForUser.size() - 1; i4 >= 0; i4--) {
                int userId3 = this.mNextAlarmClockForUser.keyAt(i4);
                if (nextForUser.get(userId3) == null) {
                    updateNextAlarmInfoForUserLocked(userId3, null);
                }
            }
        }
    }

    private void updateNextAlarmInfoForUserLocked(int userId, AlarmManager.AlarmClockInfo alarmClock) {
        if (alarmClock != null) {
            this.mNextAlarmClockForUser.put(userId, alarmClock);
        } else {
            this.mNextAlarmClockForUser.remove(userId);
        }
        this.mPendingSendNextAlarmClockChangedForUser.put(userId, true);
        this.mHandler.removeMessages(2);
        this.mHandler.sendEmptyMessage(2);
    }

    /* access modifiers changed from: private */
    public void sendNextAlarmClockChanged() {
        int userId;
        SparseArray<AlarmManager.AlarmClockInfo> pendingUsers = this.mHandlerSparseAlarmClockArray;
        pendingUsers.clear();
        synchronized (this.mLock) {
            int N = this.mPendingSendNextAlarmClockChangedForUser.size();
            userId = 0;
            for (int i = 0; i < N; i++) {
                int userId2 = this.mPendingSendNextAlarmClockChangedForUser.keyAt(i);
                pendingUsers.append(userId2, this.mNextAlarmClockForUser.get(userId2));
            }
            this.mPendingSendNextAlarmClockChangedForUser.clear();
        }
        int N2 = pendingUsers.size();
        while (true) {
            int i2 = userId;
            if (i2 < N2) {
                int userId3 = pendingUsers.keyAt(i2);
                Settings.System.putStringForUser(getContext().getContentResolver(), "next_alarm_formatted", formatNextAlarm(getContext(), pendingUsers.valueAt(i2), userId3), userId3);
                getContext().sendBroadcastAsUser(NEXT_ALARM_CLOCK_CHANGED_INTENT, new UserHandle(userId3));
                userId = i2 + 1;
            } else {
                return;
            }
        }
    }

    private static String formatNextAlarm(Context context, AlarmManager.AlarmClockInfo info, int userId) {
        String pattern = DateFormat.getBestDateTimePattern(Locale.getDefault(), DateFormat.is24HourFormat(context, userId) ? "EHm" : "Ehma");
        if (info == null) {
            return BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
        }
        return DateFormat.format(pattern, info.getTriggerTime()).toString();
    }

    /* access modifiers changed from: package-private */
    public void rescheduleKernelAlarmsLocked() {
        long nextNonWakeup = 0;
        if (this.mAlarmBatches.size() > 0) {
            Batch firstBatch = this.mAlarmBatches.get(0);
            if (this.mIsAlarmDataOnlyMode || !isAwareAlarmManagerEnabled()) {
                Batch firstWakeup = findFirstWakeupBatchLocked();
                if (firstWakeup != null) {
                    this.mNextWakeup = firstWakeup.start;
                    this.mLastWakeupSet = SystemClock.elapsedRealtime();
                    setLocked(2, firstWakeup.start);
                }
                if (firstBatch != firstWakeup) {
                    nextNonWakeup = firstBatch.start;
                }
            } else {
                long firstWakeupEnd = findFirstWakeupEndLocked();
                if (firstWakeupEnd != -1) {
                    this.mNextWakeup = firstWakeupEnd;
                    this.mLastWakeupSet = SystemClock.elapsedRealtime();
                    setLocked(2, firstWakeupEnd);
                }
                if (firstBatch.start != firstWakeupEnd) {
                    nextNonWakeup = firstBatch.start;
                }
            }
        }
        if (this.mPendingNonWakeupAlarms.size() > 0 && (nextNonWakeup == 0 || this.mNextNonWakeupDeliveryTime < nextNonWakeup)) {
            nextNonWakeup = this.mNextNonWakeupDeliveryTime;
        }
        if (nextNonWakeup != 0) {
            if (!"factory".equals(SystemProperties.get("ro.runmode", "normal")) || nextNonWakeup != this.mNextWakeup) {
                this.mNextNonWakeup = nextNonWakeup;
                setLocked(3, nextNonWakeup);
            } else {
                Flog.w(500, "no need set for the time had been set by type 2");
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void removeLocked(PendingIntent operation, IAlarmListener directReceiver) {
        if (operation != null || directReceiver != null) {
            boolean didRemove = false;
            Predicate<Alarm> whichAlarms = new Predicate(operation, directReceiver) {
                private final /* synthetic */ PendingIntent f$0;
                private final /* synthetic */ IAlarmListener f$1;

                {
                    this.f$0 = r1;
                    this.f$1 = r2;
                }

                public final boolean test(Object obj) {
                    return AlarmManagerService.lambda$removeLocked$0(this.f$0, this.f$1, (AlarmManagerService.Alarm) obj);
                }
            };
            for (int i = this.mAlarmBatches.size() - 1; i >= 0; i--) {
                Batch b = this.mAlarmBatches.get(i);
                if (b != null) {
                    didRemove |= b.remove(whichAlarms);
                    if (b.size() == 0) {
                        this.mAlarmBatches.remove(i);
                    }
                }
            }
            for (int i2 = this.mPendingWhileIdleAlarms.size() - 1; i2 >= 0; i2--) {
                if (this.mPendingWhileIdleAlarms.get(i2).matches(operation, directReceiver)) {
                    this.mPendingWhileIdleAlarms.remove(i2);
                }
            }
            for (int i3 = this.mPendingBackgroundAlarms.size() - 1; i3 >= 0; i3--) {
                ArrayList<Alarm> alarmsForUid = this.mPendingBackgroundAlarms.valueAt(i3);
                for (int j = alarmsForUid.size() - 1; j >= 0; j--) {
                    if (alarmsForUid.get(j).matches(operation, directReceiver)) {
                        alarmsForUid.remove(j);
                    }
                }
                if (alarmsForUid.size() == 0) {
                    this.mPendingBackgroundAlarms.removeAt(i3);
                }
            }
            hwRemoveAnywayRtcAlarm(operation);
            if (didRemove) {
                boolean restorePending = false;
                if (this.mPendingIdleUntil != null && this.mPendingIdleUntil.matches(operation, directReceiver)) {
                    this.mPendingIdleUntil = null;
                    restorePending = true;
                }
                if (this.mNextWakeFromIdle != null && this.mNextWakeFromIdle.matches(operation, directReceiver)) {
                    this.mNextWakeFromIdle = null;
                }
                rebatchAllAlarmsLocked(true);
                if (restorePending) {
                    restorePendingWhileIdleAlarmsLocked();
                }
                updateNextAlarmClockLocked();
            }
        }
    }

    static /* synthetic */ boolean lambda$removeLocked$0(PendingIntent operation, IAlarmListener directReceiver, Alarm a) {
        if (!a.matches(operation, directReceiver) && !HwActivityManagerServiceUtil.isPendingIntentCanceled(a.operation)) {
            return false;
        }
        if (operation != null && (a.packageName == null || !a.packageName.equals("com.google.android.gms"))) {
            Slog.i(TAG, "remove alarm:" + a + " according to operation:" + Integer.toHexString(System.identityHashCode(operation)));
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public void removeLocked(int uid) {
        if (uid == 1000) {
            Slog.wtf(TAG, "removeLocked: Shouldn't for UID=" + uid);
            return;
        }
        boolean didRemove = false;
        Predicate<Alarm> whichAlarms = new Predicate(uid) {
            private final /* synthetic */ int f$0;

            {
                this.f$0 = r1;
            }

            public final boolean test(Object obj) {
                return AlarmManagerService.lambda$removeLocked$1(this.f$0, (AlarmManagerService.Alarm) obj);
            }
        };
        for (int i = this.mAlarmBatches.size() - 1; i >= 0; i--) {
            Batch b = this.mAlarmBatches.get(i);
            didRemove |= b.remove(whichAlarms);
            if (b.size() == 0) {
                this.mAlarmBatches.remove(i);
            }
        }
        for (int i2 = this.mPendingWhileIdleAlarms.size() - 1; i2 >= 0; i2--) {
            if (this.mPendingWhileIdleAlarms.get(i2).uid == uid) {
                this.mPendingWhileIdleAlarms.remove(i2);
            }
        }
        for (int i3 = this.mPendingBackgroundAlarms.size() - 1; i3 >= 0; i3--) {
            ArrayList<Alarm> alarmsForUid = this.mPendingBackgroundAlarms.valueAt(i3);
            for (int j = alarmsForUid.size() - 1; j >= 0; j--) {
                if (alarmsForUid.get(j).uid == uid) {
                    alarmsForUid.remove(j);
                }
            }
            if (alarmsForUid.size() == 0) {
                this.mPendingBackgroundAlarms.removeAt(i3);
            }
        }
        if (didRemove) {
            rebatchAllAlarmsLocked(true);
            rescheduleKernelAlarmsLocked();
            updateNextAlarmClockLocked();
        }
    }

    static /* synthetic */ boolean lambda$removeLocked$1(int uid, Alarm a) {
        if (a.uid != uid) {
            return false;
        }
        Slog.i(TAG, "remove " + a + " according to uid:" + uid);
        return true;
    }

    /* access modifiers changed from: package-private */
    public void removeLocked(String packageName) {
        if (packageName != null && !packageName.equals(PackageManagerService.PLATFORM_PACKAGE_NAME)) {
            boolean didRemove = false;
            Predicate<Alarm> whichAlarms = new Predicate(packageName) {
                private final /* synthetic */ String f$0;

                {
                    this.f$0 = r1;
                }

                public final boolean test(Object obj) {
                    return AlarmManagerService.lambda$removeLocked$2(this.f$0, (AlarmManagerService.Alarm) obj);
                }
            };
            boolean oldHasTick = haveBatchesTimeTickAlarm(this.mAlarmBatches);
            for (int i = this.mAlarmBatches.size() - 1; i >= 0; i--) {
                Batch b = this.mAlarmBatches.get(i);
                didRemove |= b.remove(whichAlarms);
                if (b.size() == 0) {
                    this.mAlarmBatches.remove(i);
                }
            }
            if (oldHasTick != haveBatchesTimeTickAlarm(this.mAlarmBatches)) {
                Slog.wtf(TAG, "removeLocked: hasTick changed from " + oldHasTick + " to " + newHasTick);
            }
            for (int i2 = this.mPendingWhileIdleAlarms.size() - 1; i2 >= 0; i2--) {
                if (this.mPendingWhileIdleAlarms.get(i2).matches(packageName)) {
                    this.mPendingWhileIdleAlarms.remove(i2);
                }
            }
            for (int i3 = this.mPendingBackgroundAlarms.size() - 1; i3 >= 0; i3--) {
                ArrayList<Alarm> alarmsForUid = this.mPendingBackgroundAlarms.valueAt(i3);
                for (int j = alarmsForUid.size() - 1; j >= 0; j--) {
                    if (alarmsForUid.get(j).matches(packageName)) {
                        alarmsForUid.remove(j);
                    }
                }
                if (alarmsForUid.size() == 0) {
                    this.mPendingBackgroundAlarms.removeAt(i3);
                }
            }
            if (didRemove) {
                rebatchAllAlarmsLocked(true);
                rescheduleKernelAlarmsLocked();
                updateNextAlarmClockLocked();
            }
        }
    }

    static /* synthetic */ boolean lambda$removeLocked$2(String packageName, Alarm a) {
        if (!a.matches(packageName)) {
            return false;
        }
        Slog.i(TAG, "remove " + a + " according to packageName:" + packageName);
        return true;
    }

    /* access modifiers changed from: package-private */
    public void removeLocked(int uid, String packageName) {
        if (uid == 1000) {
            Slog.wtf(TAG, "removeLocked: Shouldn't for UID=" + uid);
        } else if (packageName != null && !packageName.equals(PackageManagerService.PLATFORM_PACKAGE_NAME)) {
            boolean didRemove = false;
            Predicate<Alarm> whichAlarms = new Predicate(uid, packageName) {
                private final /* synthetic */ int f$0;
                private final /* synthetic */ String f$1;

                {
                    this.f$0 = r1;
                    this.f$1 = r2;
                }

                public final boolean test(Object obj) {
                    return AlarmManagerService.lambda$removeLocked$3(this.f$0, this.f$1, (AlarmManagerService.Alarm) obj);
                }
            };
            for (int i = this.mAlarmBatches.size() - 1; i >= 0; i--) {
                Batch b = this.mAlarmBatches.get(i);
                didRemove |= b.remove(whichAlarms);
                if (b.size() == 0) {
                    this.mAlarmBatches.remove(i);
                }
            }
            for (int i2 = this.mPendingWhileIdleAlarms.size() - 1; i2 >= 0; i2--) {
                Alarm a = this.mPendingWhileIdleAlarms.get(i2);
                if (a.uid == uid && a.matches(packageName)) {
                    this.mPendingWhileIdleAlarms.remove(i2);
                }
            }
            for (int i3 = this.mPendingBackgroundAlarms.size() - 1; i3 >= 0; i3--) {
                ArrayList<Alarm> alarmsForUid = this.mPendingBackgroundAlarms.valueAt(i3);
                for (int j = alarmsForUid.size() - 1; j >= 0; j--) {
                    if (alarmsForUid.get(j).uid == uid && alarmsForUid.get(j).matches(packageName)) {
                        alarmsForUid.remove(j);
                    }
                }
                if (alarmsForUid.size() == 0) {
                    this.mPendingBackgroundAlarms.removeAt(i3);
                }
            }
            if (didRemove) {
                rebatchAllAlarmsLocked(true);
                rescheduleKernelAlarmsLocked();
                updateNextAlarmClockLocked();
            }
        }
    }

    static /* synthetic */ boolean lambda$removeLocked$3(int uid, String packageName, Alarm a) {
        if (a.uid != uid || !a.matches(packageName)) {
            return false;
        }
        Slog.i(TAG, "remove " + a + " according to " + packageName + " and " + uid);
        return true;
    }

    /* access modifiers changed from: package-private */
    public void removeForStoppedLocked(int uid) {
        if (uid == 1000) {
            Slog.wtf(TAG, "removeForStoppedLocked: Shouldn't for UID=" + uid);
            return;
        }
        boolean didRemove = false;
        Predicate<Alarm> whichAlarms = new Predicate(uid) {
            private final /* synthetic */ int f$0;

            {
                this.f$0 = r1;
            }

            public final boolean test(Object obj) {
                return AlarmManagerService.lambda$removeForStoppedLocked$4(this.f$0, (AlarmManagerService.Alarm) obj);
            }
        };
        for (int i = this.mAlarmBatches.size() - 1; i >= 0; i--) {
            Batch b = this.mAlarmBatches.get(i);
            didRemove |= b.remove(whichAlarms);
            if (b.size() == 0) {
                this.mAlarmBatches.remove(i);
            }
        }
        for (int i2 = this.mPendingWhileIdleAlarms.size() - 1; i2 >= 0; i2--) {
            if (this.mPendingWhileIdleAlarms.get(i2).uid == uid) {
                this.mPendingWhileIdleAlarms.remove(i2);
            }
        }
        for (int i3 = this.mPendingBackgroundAlarms.size() - 1; i3 >= 0; i3--) {
            if (this.mPendingBackgroundAlarms.keyAt(i3) == uid) {
                this.mPendingBackgroundAlarms.removeAt(i3);
            }
        }
        if (didRemove) {
            rebatchAllAlarmsLocked(true);
            rescheduleKernelAlarmsLocked();
            updateNextAlarmClockLocked();
        }
    }

    static /* synthetic */ boolean lambda$removeForStoppedLocked$4(int uid, Alarm a) {
        try {
            if (a.uid == uid && ActivityManager.getService().isAppStartModeDisabled(uid, a.packageName)) {
                Slog.i(TAG, "removeForStoppedLocked " + a + " according to uid:" + uid);
                return true;
            }
        } catch (RemoteException e) {
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public void removeUserLocked(int userHandle) {
        if (userHandle == 0) {
            Slog.wtf(TAG, "removeForStoppedLocked: Shouldn't for user=" + userHandle);
            return;
        }
        boolean didRemove = false;
        Predicate<Alarm> whichAlarms = new Predicate(userHandle) {
            private final /* synthetic */ int f$0;

            {
                this.f$0 = r1;
            }

            public final boolean test(Object obj) {
                return AlarmManagerService.lambda$removeUserLocked$5(this.f$0, (AlarmManagerService.Alarm) obj);
            }
        };
        for (int i = this.mAlarmBatches.size() - 1; i >= 0; i--) {
            Batch b = this.mAlarmBatches.get(i);
            didRemove |= b.remove(whichAlarms);
            if (b.size() == 0) {
                this.mAlarmBatches.remove(i);
            }
        }
        for (int i2 = this.mPendingWhileIdleAlarms.size() - 1; i2 >= 0; i2--) {
            if (UserHandle.getUserId(this.mPendingWhileIdleAlarms.get(i2).creatorUid) == userHandle) {
                this.mPendingWhileIdleAlarms.remove(i2);
            }
        }
        for (int i3 = this.mPendingBackgroundAlarms.size() - 1; i3 >= 0; i3--) {
            if (UserHandle.getUserId(this.mPendingBackgroundAlarms.keyAt(i3)) == userHandle) {
                this.mPendingBackgroundAlarms.removeAt(i3);
            }
        }
        for (int i4 = this.mLastAllowWhileIdleDispatch.size() - 1; i4 >= 0; i4--) {
            if (UserHandle.getUserId(this.mLastAllowWhileIdleDispatch.keyAt(i4)) == userHandle) {
                this.mLastAllowWhileIdleDispatch.removeAt(i4);
            }
        }
        if (didRemove) {
            rebatchAllAlarmsLocked(true);
            rescheduleKernelAlarmsLocked();
            updateNextAlarmClockLocked();
        }
    }

    static /* synthetic */ boolean lambda$removeUserLocked$5(int userHandle, Alarm a) {
        if (UserHandle.getUserId(a.creatorUid) != userHandle) {
            return false;
        }
        Slog.i(TAG, "remove " + a + " according to userHandle:" + userHandle);
        return true;
    }

    /* access modifiers changed from: package-private */
    public void interactiveStateChangedLocked(boolean interactive) {
        if (this.mInteractive != interactive) {
            this.mInteractive = interactive;
            long nowELAPSED = SystemClock.elapsedRealtime();
            if (interactive) {
                if (this.mPendingNonWakeupAlarms.size() > 0) {
                    long thisDelayTime = nowELAPSED - this.mStartCurrentDelayTime;
                    this.mTotalDelayTime += thisDelayTime;
                    if (this.mMaxDelayTime < thisDelayTime) {
                        this.mMaxDelayTime = thisDelayTime;
                    }
                    deliverAlarmsLocked(this.mPendingNonWakeupAlarms, nowELAPSED);
                    this.mPendingNonWakeupAlarms.clear();
                }
                if (this.mNonInteractiveStartTime > 0) {
                    long dur = nowELAPSED - this.mNonInteractiveStartTime;
                    if (dur > this.mNonInteractiveTime) {
                        this.mNonInteractiveTime = dur;
                        return;
                    }
                    return;
                }
                return;
            }
            this.mNonInteractiveStartTime = nowELAPSED;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean lookForPackageLocked(String packageName) {
        for (int i = 0; i < this.mAlarmBatches.size(); i++) {
            if (this.mAlarmBatches.get(i).hasPackage(packageName)) {
                return true;
            }
        }
        for (int i2 = 0; i2 < this.mPendingWhileIdleAlarms.size(); i2++) {
            if (this.mPendingWhileIdleAlarms.get(i2).matches(packageName)) {
                return true;
            }
        }
        return false;
    }

    private void setLocked(int type, long when) {
        long alarmSeconds;
        long alarmSeconds2;
        if (this.mNativeData != 0) {
            if (when < 0) {
                alarmSeconds = 0;
                alarmSeconds2 = 0;
            } else {
                alarmSeconds2 = 1000 * (when % 1000) * 1000;
                alarmSeconds = when / 1000;
            }
            if (set(this.mNativeData, type, alarmSeconds, alarmSeconds2) != 0) {
                long nowElapsed = SystemClock.elapsedRealtime();
                Slog.wtf(TAG, "Unable to set kernel alarm, now=" + nowElapsed + " type=" + type + " when=" + when + " @ (" + alarmSeconds + "," + alarmSeconds2 + "), ret = " + result + " = " + Os.strerror(result));
                return;
            }
            return;
        }
        Message msg = Message.obtain();
        msg.what = 1;
        this.mHandler.removeMessages(1);
        this.mHandler.sendMessageAtTime(msg, when);
    }

    private static final void dumpAlarmList(PrintWriter pw, ArrayList<Alarm> list, String prefix, String label, long nowELAPSED, long nowRTC, SimpleDateFormat sdf) {
        PrintWriter printWriter = pw;
        String str = prefix;
        int i = list.size() - 1;
        while (true) {
            int i2 = i;
            if (i2 >= 0) {
                Alarm a = list.get(i2);
                printWriter.print(str);
                printWriter.print(label);
                printWriter.print(" #");
                printWriter.print(i2);
                printWriter.print(": ");
                printWriter.println(a);
                a.dump(printWriter, str + "  ", nowELAPSED, nowRTC, sdf);
                i = i2 + -1;
            } else {
                ArrayList<Alarm> arrayList = list;
                String str2 = label;
                return;
            }
        }
    }

    private static final String labelForType(int type) {
        switch (type) {
            case 0:
                return "RTC_WAKEUP";
            case 1:
                return "RTC";
            case 2:
                return "ELAPSED_WAKEUP";
            case 3:
                return "ELAPSED";
            default:
                return "--unknown--";
        }
    }

    private static final void dumpAlarmList(PrintWriter pw, ArrayList<Alarm> list, String prefix, long nowELAPSED, long nowRTC, SimpleDateFormat sdf) {
        PrintWriter printWriter = pw;
        String str = prefix;
        int i = list.size() - 1;
        while (true) {
            int i2 = i;
            if (i2 >= 0) {
                Alarm a = list.get(i2);
                String label = labelForType(a.type);
                printWriter.print(str);
                printWriter.print(label);
                printWriter.print(" #");
                printWriter.print(i2);
                printWriter.print(": ");
                printWriter.println(a);
                a.dump(printWriter, str + "  ", nowELAPSED, nowRTC, sdf);
                i = i2 + -1;
            } else {
                ArrayList<Alarm> arrayList = list;
                return;
            }
        }
    }

    /* access modifiers changed from: private */
    public boolean isBackgroundRestricted(Alarm alarm) {
        boolean z = true;
        boolean exemptOnBatterySaver = (alarm.flags & 4) != 0;
        if (alarm.alarmClock != null) {
            return false;
        }
        if (alarm.operation != null) {
            if (alarm.operation.isActivity()) {
                return false;
            }
            if (alarm.operation.isForegroundService()) {
                exemptOnBatterySaver = true;
            }
        }
        String sourcePackage = alarm.sourcePackage;
        int sourceUid = alarm.creatorUid;
        if (this.mAppStateTracker == null || !this.mAppStateTracker.areAlarmsRestricted(sourceUid, sourcePackage, exemptOnBatterySaver)) {
            z = false;
        }
        return z;
    }

    private long getWhileIdleMinIntervalLocked(int uid) {
        boolean ebs = false;
        boolean dozing = this.mPendingIdleUntil != null;
        if (this.mAppStateTracker != null && this.mAppStateTracker.isForceAllAppsStandbyEnabled()) {
            ebs = true;
        }
        if (!dozing && !ebs) {
            return this.mConstants.ALLOW_WHILE_IDLE_SHORT_TIME;
        }
        if (dozing) {
            return this.mConstants.ALLOW_WHILE_IDLE_LONG_TIME;
        }
        if (this.mUseAllowWhileIdleShortTime.get(uid)) {
            return this.mConstants.ALLOW_WHILE_IDLE_SHORT_TIME;
        }
        return this.mConstants.ALLOW_WHILE_IDLE_LONG_TIME;
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x00ee, code lost:
        if (2 == r7.type) goto L_0x00f2;
     */
    public boolean triggerAlarmsLocked(ArrayList<Alarm> triggerList, long nowELAPSED, long nowRTC) {
        int N;
        Batch batch;
        int i;
        boolean z;
        AlarmManagerService alarmManagerService;
        Alarm alarm;
        AlarmManagerService alarmManagerService2 = this;
        ArrayList<Alarm> arrayList = triggerList;
        boolean z2 = false;
        boolean hasWakeup = false;
        while (true) {
            int i2 = 500;
            if (alarmManagerService2.mAlarmBatches.size() <= 0) {
                break;
            }
            Batch batch2 = alarmManagerService2.mAlarmBatches.get(z2 ? 1 : 0);
            if (batch2.start > nowELAPSED) {
                break;
            }
            alarmManagerService2.mAlarmBatches.remove(z2);
            int N2 = batch2.size();
            boolean hasWakeup2 = hasWakeup;
            int i3 = z2;
            while (true) {
                int i4 = i3;
                if (i4 >= N2) {
                    break;
                }
                Alarm alarm2 = batch2.get(i4);
                if ((alarm2.flags & 4) != 0) {
                    long lastTime = alarmManagerService2.mLastAllowWhileIdleDispatch.get(alarm2.creatorUid, -1);
                    long minTime = lastTime + alarmManagerService2.getWhileIdleMinIntervalLocked(alarm2.creatorUid);
                    if (lastTime >= 0 && nowELAPSED < minTime) {
                        alarm2.whenElapsed = minTime;
                        alarm2.expectedWhenElapsed = minTime;
                        if (alarm2.maxWhenElapsed < minTime) {
                            alarm2.maxWhenElapsed = minTime;
                        }
                        alarm2.expectedMaxWhenElapsed = alarm2.maxWhenElapsed;
                        alarmManagerService2.setImplLocked(alarm2, true, z2);
                        i = i4;
                        batch = batch2;
                        N = N2;
                        z = z2;
                        alarmManagerService = alarmManagerService2;
                        i3 = i + 1;
                        arrayList = triggerList;
                        alarmManagerService2 = alarmManagerService;
                        z2 = z;
                        batch2 = batch;
                        N2 = N;
                        i2 = 500;
                    }
                }
                if (alarmManagerService2.isBackgroundRestricted(alarm2)) {
                    ArrayList<Alarm> alarmsForUid = alarmManagerService2.mPendingBackgroundAlarms.get(alarm2.creatorUid);
                    if (alarmsForUid == null) {
                        alarmsForUid = new ArrayList<>();
                        alarmManagerService2.mPendingBackgroundAlarms.put(alarm2.creatorUid, alarmsForUid);
                    }
                    alarmsForUid.add(alarm2);
                    i = i4;
                    batch = batch2;
                    N = N2;
                    z = z2;
                    alarmManagerService = alarmManagerService2;
                    i3 = i + 1;
                    arrayList = triggerList;
                    alarmManagerService2 = alarmManagerService;
                    z2 = z;
                    batch2 = batch;
                    N2 = N;
                    i2 = 500;
                } else {
                    alarm2.count = 1;
                    arrayList.add(alarm2);
                    if ((alarm2.flags & 2) != 0) {
                        EventLogTags.writeDeviceIdleWakeFromIdle(alarmManagerService2.mPendingIdleUntil != null ? true : z2 ? 1 : 0, alarm2.statsTag);
                    }
                    if (alarmManagerService2.mPendingIdleUntil == alarm2) {
                        alarmManagerService2.mPendingIdleUntil = null;
                        alarmManagerService2.rebatchAllAlarmsLocked(z2);
                        restorePendingWhileIdleAlarmsLocked();
                    }
                    if (alarmManagerService2.mNextWakeFromIdle == alarm2) {
                        alarmManagerService2.mNextWakeFromIdle = null;
                        alarmManagerService2.rebatchAllAlarmsLocked(z2);
                    }
                    if (alarm2.repeatInterval > 0) {
                        alarm2.count = (int) (((long) alarm2.count) + ((nowELAPSED - alarm2.expectedWhenElapsed) / alarm2.repeatInterval));
                        long delta = ((long) alarm2.count) * alarm2.repeatInterval;
                        long nextElapsed = alarm2.whenElapsed + delta;
                        if (alarm2.type != 0) {
                        }
                        Flog.i(i2, "set again repeat alarm: next ,nextElapsed = " + nextElapsed + ",repeatInterval = " + alarm2.repeatInterval);
                        int i5 = alarm2.type;
                        int i6 = i5;
                        long nextElapsed2 = nextElapsed;
                        i = i4;
                        alarm = alarm2;
                        batch = batch2;
                        N = N2;
                        z = z2;
                        alarmManagerService2.setImplLocked(i6, alarm2.when + delta, nextElapsed2, alarm2.windowLength, maxTriggerTime(nowELAPSED, nextElapsed, alarm2.repeatInterval), alarm2.repeatInterval, alarm2.operation, null, null, alarm2.flags, true, alarm2.workSource, alarm2.alarmClock, alarm2.uid, alarm2.packageName);
                    } else {
                        i = i4;
                        alarm = alarm2;
                        batch = batch2;
                        N = N2;
                        z = z2;
                    }
                    Alarm alarm3 = alarm;
                    if (alarm3.wakeup) {
                        hasWakeup2 = true;
                    }
                    if (alarm3.alarmClock != null) {
                        alarmManagerService = this;
                        alarmManagerService.mNextAlarmClockMayChange = true;
                        i3 = i + 1;
                        arrayList = triggerList;
                        alarmManagerService2 = alarmManagerService;
                        z2 = z;
                        batch2 = batch;
                        N2 = N;
                        i2 = 500;
                    } else {
                        alarmManagerService = this;
                        i3 = i + 1;
                        arrayList = triggerList;
                        alarmManagerService2 = alarmManagerService;
                        z2 = z;
                        batch2 = batch;
                        N2 = N;
                        i2 = 500;
                    }
                }
            }
            boolean z3 = z2;
            AlarmManagerService alarmManagerService3 = alarmManagerService2;
            arrayList = triggerList;
            hasWakeup = hasWakeup2;
        }
        int i7 = z2;
        AlarmManagerService alarmManagerService4 = alarmManagerService2;
        alarmManagerService4.mCurrentSeq++;
        calculateDeliveryPriorities(triggerList);
        ArrayList<Alarm> arrayList2 = triggerList;
        Collections.sort(arrayList2, alarmManagerService4.mAlarmDispatchComparator);
        ArrayList<Alarm> wakeupAlarms = new ArrayList<>();
        while (true) {
            int i8 = i7;
            if (i8 >= triggerList.size()) {
                break;
            }
            Alarm talarm = arrayList2.get(i8);
            if (talarm.type != 0) {
                if (2 != talarm.type) {
                    i7 = i8 + 1;
                }
            }
            wakeupAlarms.add(talarm);
            StringBuilder alarmInfo = new StringBuilder();
            if (talarm.operation != null) {
                Intent intent = resetIntentCallingIdentity(talarm.operation);
                alarmInfo.append(" tag: ");
                alarmInfo.append(talarm.statsTag);
                alarmInfo.append(" window:");
                alarmInfo.append(talarm.windowLength);
                alarmInfo.append(" originWhen:");
                alarmInfo.append(convertToElapsed(talarm.when, talarm.type));
                alarmInfo.append(" when:");
                alarmInfo.append(talarm.whenElapsed);
                alarmInfo.append(" maxWhen:");
                alarmInfo.append(talarm.maxWhenElapsed);
                if (intent != null) {
                    String triggerAction = resetActionCallingIdentity(talarm.operation);
                    Log.w(TAG, "mIsScreenOn is: " + alarmManagerService4.mIsScreenOn + ", WAKEUP alarm trigger action = " + triggerAction + " package name is: " + talarm.packageName + alarmInfo.toString());
                } else {
                    Log.w(TAG, "mIsScreenOn is: " + alarmManagerService4.mIsScreenOn + ", WAKEUP alarm intent == null and  package name is: " + talarm.packageName + " listenerTag is: " + talarm.listenerTag + " createor uid is: " + talarm.creatorUid + alarmInfo.toString());
                }
            } else {
                Log.w(TAG, "mIsScreenOn is: " + alarmManagerService4.mIsScreenOn + ", WAKEUP alarm talarm.operation == null,package name is: " + talarm.packageName + " listenerTag is: " + talarm.listenerTag + " creator uid is: " + talarm.creatorUid + alarmInfo.toString());
            }
            i7 = i8 + 1;
        }
        if (wakeupAlarms.size() > 0) {
            Flog.i(500, "Alarm triggering (type 0 or 2): " + wakeupAlarms);
        }
        return hasWakeup;
    }

    /* access modifiers changed from: package-private */
    public void recordWakeupAlarms(ArrayList<Batch> batches, long nowELAPSED, long nowRTC) {
        int numBatches = batches.size();
        int nextBatch = 0;
        while (nextBatch < numBatches) {
            Batch b = batches.get(nextBatch);
            if (b.start <= nowELAPSED) {
                int numAlarms = b.alarms.size();
                for (int nextAlarm = 0; nextAlarm < numAlarms; nextAlarm++) {
                    this.mRecentWakeups.add(b.alarms.get(nextAlarm).makeWakeupEvent(nowRTC));
                }
                nextBatch++;
            } else {
                return;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public long currentNonWakeupFuzzLocked(long nowELAPSED) {
        long timeSinceOn = nowELAPSED - this.mNonInteractiveStartTime;
        if (timeSinceOn < BackupAgentTimeoutParameters.DEFAULT_FULL_BACKUP_AGENT_TIMEOUT_MILLIS) {
            return JobStatus.DEFAULT_TRIGGER_MAX_DELAY;
        }
        if (timeSinceOn < 1800000) {
            return 900000;
        }
        return AppStandbyController.SettingsObserver.DEFAULT_STRONG_USAGE_TIMEOUT;
    }

    static int fuzzForDuration(long duration) {
        if (duration < 900000) {
            return (int) duration;
        }
        if (duration < 5400000) {
            return 900000;
        }
        return 1800000;
    }

    /* access modifiers changed from: package-private */
    public boolean checkAllowNonWakeupDelayLocked(long nowELAPSED) {
        boolean z = false;
        if (this.mInteractive || this.mLastAlarmDeliveryTime <= 0) {
            return false;
        }
        if (this.mPendingNonWakeupAlarms.size() > 0 && this.mNextNonWakeupDeliveryTime < nowELAPSED) {
            return false;
        }
        if (nowELAPSED - this.mLastAlarmDeliveryTime <= currentNonWakeupFuzzLocked(nowELAPSED)) {
            z = true;
        }
        return z;
    }

    /* access modifiers changed from: package-private */
    public void deliverAlarmsLocked(ArrayList<Alarm> triggerList, long nowELAPSED) {
        String packageName;
        long j = nowELAPSED;
        this.mLastAlarmDeliveryTime = j;
        int i = 0;
        while (true) {
            int i2 = i;
            if (i2 < triggerList.size()) {
                Alarm alarm = triggerList.get(i2);
                if (alarm == null) {
                    HwLog.dubaie("DUBAI_TAG_ALARM_TRIGGER", "uid=0 name=NULL type=-1 tag=NULL size=" + triggerList.size() + " worksource=0");
                } else {
                    boolean allowWhileIdle = (alarm.flags & 4) != 0;
                    if (alarm.wakeup) {
                        Trace.traceBegin(131072, "Dispatch wakeup alarm to " + alarm.packageName);
                    } else {
                        Trace.traceBegin(131072, "Dispatch non-wakeup alarm to " + alarm.packageName);
                    }
                    try {
                        Flog.i(500, "sending alarm " + alarm + ",repeatInterval = " + alarm.repeatInterval + ",listenerTag =" + alarm.listenerTag);
                        if (alarm.operation == null) {
                            packageName = alarm.packageName;
                        } else {
                            packageName = alarm.operation.getTargetPackage();
                        }
                        int hasWorkSource = 0;
                        if (alarm.workSource != null && alarm.workSource.size() > 0) {
                            hasWorkSource = 1;
                        }
                        HwLog.dubaie("DUBAI_TAG_ALARM_TRIGGER", "uid=" + alarm.uid + " name=" + packageName + " type=" + alarm.type + " tag=" + alarm.statsTag + " size=" + triggerList.size() + " worksource=" + hasWorkSource);
                        if (hasWorkSource == 1) {
                            int wi = 0;
                            while (wi < alarm.workSource.size()) {
                                StringBuilder sb = new StringBuilder();
                                sb.append("uid=");
                                sb.append(alarm.workSource.get(wi));
                                sb.append(" name=");
                                sb.append(alarm.workSource.getName(wi));
                                sb.append(" tag=");
                                sb.append(alarm.statsTag);
                                sb.append(" finished=");
                                sb.append(wi == alarm.workSource.size() - 1 ? "1" : "0");
                                HwLog.dubaie("DUBAI_TAG_ALARM_WORKSOURCE", sb.toString());
                                wi++;
                            }
                        }
                        ActivityManager.noteAlarmStart(alarm.operation, alarm.workSource, alarm.uid, alarm.statsTag);
                        this.mDeliveryTracker.deliverLocked(alarm, j, allowWhileIdle);
                    } catch (RuntimeException e) {
                        Slog.w(TAG, "Failure sending alarm.", e);
                    }
                    Trace.traceEnd(131072);
                }
                i = i2 + 1;
            } else {
                ArrayList<Alarm> arrayList = triggerList;
                return;
            }
        }
    }

    /* access modifiers changed from: private */
    public boolean isExemptFromAppStandby(Alarm a) {
        return (a.alarmClock == null && !UserHandle.isCore(a.creatorUid) && (a.flags & 8) == 0) ? false : true;
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x0023 A[Catch:{ Exception -> 0x0048 }] */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x0029 A[Catch:{ Exception -> 0x0048 }] */
    public void setWakelockWorkSource(PendingIntent pi, WorkSource ws, int type, String tag, int knownUid, boolean first) {
        try {
            boolean unimportant = pi == this.mTimeTickSender;
            this.mWakeLock.setUnimportantForLogging(unimportant);
            if (!first) {
                if (!this.mLastWakeLockUnimportantForLogging) {
                    this.mWakeLock.setHistoryTag(null);
                    this.mLastWakeLockUnimportantForLogging = unimportant;
                    if (ws == null) {
                        this.mWakeLock.setWorkSource(ws);
                        return;
                    }
                    int uid = knownUid >= 0 ? knownUid : ActivityManager.getService().getUidForIntentSender(pi.getTarget());
                    if (uid >= 0) {
                        this.mWakeLock.setWorkSource(new WorkSource(uid));
                        return;
                    }
                    this.mWakeLock.setWorkSource(null);
                    return;
                }
            }
            this.mWakeLock.setHistoryTag(tag);
            this.mLastWakeLockUnimportantForLogging = unimportant;
            if (ws == null) {
            }
        } catch (Exception e) {
        }
    }

    /* access modifiers changed from: private */
    public final BroadcastStats getStatsLocked(PendingIntent pi) {
        return getStatsLocked(pi.getCreatorUid(), pi.getCreatorPackage());
    }

    /* access modifiers changed from: private */
    public final BroadcastStats getStatsLocked(int uid, String pkgName) {
        ArrayMap<String, BroadcastStats> uidStats = this.mBroadcastStats.get(uid);
        if (uidStats == null) {
            uidStats = new ArrayMap<>();
            this.mBroadcastStats.put(uid, uidStats);
        }
        BroadcastStats bs = uidStats.get(pkgName);
        if (bs != null) {
            return bs;
        }
        BroadcastStats bs2 = new BroadcastStats(uid, pkgName);
        uidStats.put(pkgName, bs2);
        return bs2;
    }

    public ArrayList<Batch> getmAlarmBatches() {
        return this.mAlarmBatches;
    }

    /* access modifiers changed from: protected */
    public void removeDeskClockFromFWK(PendingIntent operation) {
    }

    /* access modifiers changed from: private */
    public static String resetActionCallingIdentity(PendingIntent operation) {
        long identity = Binder.clearCallingIdentity();
        String action = null;
        try {
            action = operation.getIntent().getAction();
        } catch (Throwable th) {
        }
        Binder.restoreCallingIdentity(identity);
        return action;
    }

    private static Intent resetIntentCallingIdentity(PendingIntent operation) {
        long identity = Binder.clearCallingIdentity();
        Intent intent = null;
        try {
            intent = operation.getIntent();
        } catch (Throwable th) {
        }
        Binder.restoreCallingIdentity(identity);
        return intent;
    }
}
