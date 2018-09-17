package com.android.server;

import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.AlarmManager.AlarmClockInfo;
import android.app.AppOpsManager;
import android.app.BroadcastOptions;
import android.app.IAlarmCompleteListener.Stub;
import android.app.IAlarmListener;
import android.app.IAlarmManager;
import android.app.IUidObserver;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.app.PendingIntent.OnFinished;
import android.common.HwFrameworkFactory;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.WorkSource;
import android.provider.Settings.Global;
import android.provider.Settings.System;
import android.rms.HwSysResource;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.ArrayMap;
import android.util.Flog;
import android.util.HwLog;
import android.util.KeyValueListParser;
import android.util.Log;
import android.util.PtmLog;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.util.SparseLongArray;
import android.util.TimeUtils;
import com.android.internal.util.LocalLog;
import com.android.server.am.HwActivityManagerServiceUtil;
import com.android.server.am.HwBroadcastRadarUtil;
import com.android.server.am.ProcessList;
import com.android.server.job.controllers.JobStatus;
import com.android.server.usage.UnixCalendar;
import com.android.server.wm.WindowManagerService.H;
import com.huawei.pgmng.log.LogPower;
import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;
import java.util.TreeSet;

public class AlarmManagerService extends AbsAlarmManagerService {
    static final int ALARM_EVENT = 1;
    static final String BAIDU_PUSHSERVICE_METHOD = "com.baidu.android.pushservice.action.METHOD";
    static final boolean DEBUG_ALARM_CLOCK = false;
    static final boolean DEBUG_BATCH = false;
    static final boolean DEBUG_LISTENER_CALLBACK = false;
    static final boolean DEBUG_TIMEZONE = false;
    static final boolean DEBUG_VALIDATE = false;
    private static final int ELAPSED_REALTIME_MASK = 8;
    private static final int ELAPSED_REALTIME_WAKEUP_MASK = 4;
    static final int IS_WAKEUP_MASK = 5;
    static final long MIN_FUZZABLE_INTERVAL = 10000;
    private static final Intent NEXT_ALARM_CLOCK_CHANGED_INTENT = null;
    static final int PRIO_NORMAL = 2;
    static final int PRIO_TICK = 0;
    static final int PRIO_WAKEUP = 1;
    static final boolean RECORD_ALARMS_IN_HISTORY = true;
    static final boolean RECORD_DEVICE_IDLE_ALARMS = false;
    private static final int RTC_MASK = 2;
    private static final int RTC_WAKEUP_MASK = 1;
    static final String TAG = "AlarmManager";
    static final String TIMEZONE_PROPERTY = "persist.sys.timezone";
    static final int TIME_CHANGED_MASK = 65536;
    static final int TYPE_NONWAKEUP_MASK = 1;
    static final boolean WAKEUP_STATS = false;
    static final boolean localLOGV = false;
    static final BatchTimeOrder sBatchOrder = null;
    static final IncreasingTimeOrder sIncreasingTimeOrder = null;
    final long RECENT_WAKEUP_PERIOD;
    final ArrayList<Batch> mAlarmBatches;
    final Comparator<Alarm> mAlarmDispatchComparator;
    private HwSysResource mAlarmResource;
    final ArrayList<IdleDispatchEntry> mAllowWhileIdleDispatches;
    long mAllowWhileIdleMinTime;
    AppOpsManager mAppOps;
    private final Intent mBackgroundIntent;
    int mBroadcastRefCount;
    final SparseArray<ArrayMap<String, BroadcastStats>> mBroadcastStats;
    boolean mCancelRemoveAction;
    ClockReceiver mClockReceiver;
    final Constants mConstants;
    int mCurrentSeq;
    PendingIntent mDateChangeSender;
    final DeliveryTracker mDeliveryTracker;
    int[] mDeviceIdleUserWhitelist;
    final AlarmHandler mHandler;
    private final SparseArray<AlarmClockInfo> mHandlerSparseAlarmClockArray;
    Bundle mIdleOptions;
    ArrayList<InFlight> mInFlight;
    boolean mInteractive;
    InteractiveStateReceiver mInteractiveStateReceiver;
    private boolean mIsScreenOn;
    long mLastAlarmDeliveryTime;
    final SparseLongArray mLastAllowWhileIdleDispatch;
    long mLastTimeChangeClockTime;
    long mLastTimeChangeRealtime;
    boolean mLastWakeLockUnimportantForLogging;
    private long mLastWakeup;
    private long mLastWakeupSet;
    com.android.server.DeviceIdleController.LocalService mLocalDeviceIdleController;
    protected Object mLock;
    final LocalLog mLog;
    long mMaxDelayTime;
    long mNativeData;
    private final SparseArray<AlarmClockInfo> mNextAlarmClockForUser;
    private boolean mNextAlarmClockMayChange;
    private long mNextNonWakeup;
    long mNextNonWakeupDeliveryTime;
    Alarm mNextWakeFromIdle;
    private long mNextWakeup;
    long mNonInteractiveStartTime;
    long mNonInteractiveTime;
    int mNumDelayedAlarms;
    int mNumTimeChanged;
    Alarm mPendingIdleUntil;
    ArrayList<Alarm> mPendingNonWakeupAlarms;
    private final SparseBooleanArray mPendingSendNextAlarmClockChangedForUser;
    ArrayList<Alarm> mPendingWhileIdleAlarms;
    final HashMap<String, PriorityClass> mPriorities;
    Random mRandom;
    final LinkedList<WakeupEvent> mRecentWakeups;
    private final IBinder mService;
    long mStartCurrentDelayTime;
    PendingIntent mTimeTickSender;
    private final SparseArray<AlarmClockInfo> mTmpSparseAlarmClockArray;
    long mTotalDelayTime;
    private UninstallReceiver mUninstallReceiver;
    WakeLock mWakeLock;

    public static class Alarm {
        public final AlarmClockInfo alarmClock;
        public int count;
        public final int creatorUid;
        public final int flags;
        public final IAlarmListener listener;
        public final String listenerTag;
        public long maxWhenElapsed;
        public final PendingIntent operation;
        public final long origWhen;
        public final String packageName;
        public PriorityClass priorityClass;
        public long repeatInterval;
        public final String statsTag;
        public final int type;
        public final int uid;
        public final boolean wakeup;
        public long when;
        public long whenElapsed;
        public long windowLength;
        public final WorkSource workSource;

        public Alarm(int _type, long _when, long _whenElapsed, long _windowLength, long _maxWhen, long _interval, PendingIntent _op, IAlarmListener _rec, String _listenerTag, WorkSource _ws, int _flags, AlarmClockInfo _info, int _uid, String _pkgName) {
            int creatorUid;
            this.type = _type;
            this.origWhen = _when;
            boolean z = _type != AlarmManagerService.RTC_MASK ? _type == 0 ? AlarmManagerService.RECORD_ALARMS_IN_HISTORY : AlarmManagerService.RECORD_DEVICE_IDLE_ALARMS : AlarmManagerService.RECORD_ALARMS_IN_HISTORY;
            this.wakeup = z;
            this.when = _when;
            this.whenElapsed = _whenElapsed;
            this.windowLength = _windowLength;
            this.maxWhenElapsed = _maxWhen;
            this.repeatInterval = _interval;
            this.operation = _op;
            this.listener = _rec;
            this.listenerTag = _listenerTag;
            this.statsTag = makeTag(_op, _listenerTag, _type);
            this.workSource = _ws;
            this.flags = _flags;
            this.alarmClock = _info;
            this.uid = _uid;
            this.packageName = _pkgName;
            if (this.operation != null) {
                creatorUid = this.operation.getCreatorUid();
            } else {
                creatorUid = this.uid;
            }
            this.creatorUid = creatorUid;
        }

        public static String makeTag(PendingIntent pi, String tag, int type) {
            String alarmString = (type == AlarmManagerService.RTC_MASK || type == 0) ? "*walarm*:" : "*alarm*:";
            return pi != null ? pi.getTag(alarmString) : alarmString + tag;
        }

        public WakeupEvent makeWakeupEvent(long nowRTC) {
            String -wrap6;
            int i = this.creatorUid;
            if (this.operation != null) {
                -wrap6 = AlarmManagerService.resetActionCallingIdentity(this.operation);
            } else {
                -wrap6 = "<listener>:" + this.listenerTag;
            }
            return new WakeupEvent(nowRTC, i, -wrap6);
        }

        public boolean matches(PendingIntent pi, IAlarmListener rec) {
            if (this.operation != null) {
                return this.operation.equals(pi);
            }
            return rec != null ? this.listener.asBinder().equals(rec.asBinder()) : AlarmManagerService.RECORD_DEVICE_IDLE_ALARMS;
        }

        public boolean matches(String packageName) {
            if (this.operation != null) {
                return packageName.equals(this.operation.getTargetPackage());
            }
            return packageName.equals(this.packageName);
        }

        public String toString() {
            StringBuilder sb = new StringBuilder(DumpState.DUMP_PACKAGES);
            sb.append("Alarm{");
            sb.append(Integer.toHexString(System.identityHashCode(this)));
            sb.append(" type ");
            sb.append(this.type);
            sb.append(" when ");
            sb.append(this.when);
            sb.append(" ");
            if (this.operation != null) {
                sb.append(this.operation.getTargetPackage());
            } else {
                sb.append(this.packageName);
            }
            sb.append('}');
            return sb.toString();
        }

        public void dump(PrintWriter pw, String prefix, long nowRTC, long nowELAPSED, SimpleDateFormat sdf) {
            boolean isRtc = (this.type == AlarmManagerService.TYPE_NONWAKEUP_MASK || this.type == 0) ? AlarmManagerService.RECORD_ALARMS_IN_HISTORY : AlarmManagerService.RECORD_DEVICE_IDLE_ALARMS;
            pw.print(prefix);
            pw.print("tag=");
            pw.println(this.statsTag);
            pw.print(prefix);
            pw.print("type=");
            pw.print(this.type);
            pw.print(" whenElapsed=");
            TimeUtils.formatDuration(this.whenElapsed, nowELAPSED, pw);
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
    }

    public class AlarmHandler extends Handler {
        public static final int ALARM_EVENT = 1;
        public static final int CLEAR_BAIDU_PUSHSERVICE = 6;
        public static final int LISTENER_TIMEOUT = 3;
        public static final int REPORT_ALARMS_ACTIVE = 4;
        public static final int SEND_NEXT_ALARM_CLOCK_CHANGED = 2;

        public void handleMessage(Message msg) {
            int i;
            switch (msg.what) {
                case ALARM_EVENT /*1*/:
                    ArrayList<Alarm> triggerList = new ArrayList();
                    synchronized (AlarmManagerService.this.mLock) {
                        AlarmManagerService.this.triggerAlarmsLocked(triggerList, SystemClock.elapsedRealtime(), System.currentTimeMillis());
                        AlarmManagerService.this.updateNextAlarmClockLocked();
                        break;
                    }
                    for (i = AlarmManagerService.PRIO_TICK; i < triggerList.size(); i += ALARM_EVENT) {
                        Alarm alarm = (Alarm) triggerList.get(i);
                        try {
                            alarm.operation.send();
                        } catch (CanceledException e) {
                            if (alarm.repeatInterval > 0) {
                                AlarmManagerService.this.removeImpl(alarm.operation);
                            }
                        }
                    }
                case SEND_NEXT_ALARM_CLOCK_CHANGED /*2*/:
                    AlarmManagerService.this.sendNextAlarmClockChanged();
                case LISTENER_TIMEOUT /*3*/:
                    AlarmManagerService.this.mDeliveryTracker.alarmTimedOut((IBinder) msg.obj);
                case REPORT_ALARMS_ACTIVE /*4*/:
                    if (AlarmManagerService.this.mLocalDeviceIdleController != null) {
                        AlarmManagerService.this.mLocalDeviceIdleController.setAlarmsActive(msg.arg1 != 0 ? AlarmManagerService.RECORD_ALARMS_IN_HISTORY : AlarmManagerService.RECORD_DEVICE_IDLE_ALARMS);
                    }
                case CLEAR_BAIDU_PUSHSERVICE /*6*/:
                    ArrayList<String> array = msg.getData().getStringArrayList("clearlist");
                    if (array != null && array.size() > 0) {
                        for (i = AlarmManagerService.PRIO_TICK; i < array.size(); i += ALARM_EVENT) {
                            AlarmManagerService.this.removeImpl((String) array.get(i), AlarmManagerService.BAIDU_PUSHSERVICE_METHOD);
                        }
                    }
                default:
            }
        }
    }

    private class AlarmThread extends Thread {
        public AlarmThread() {
            super(AlarmManagerService.TAG);
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void run() {
            ArrayList<Alarm> triggerList = new ArrayList();
            AlarmManagerService.this.hwRecordFirstTime();
            while (true) {
                int result = AlarmManagerService.this.waitForAlarm(AlarmManagerService.this.mNativeData);
                AlarmManagerService.this.mLastWakeup = SystemClock.elapsedRealtime();
                triggerList.clear();
                long nowRTC = System.currentTimeMillis();
                long nowELAPSED = SystemClock.elapsedRealtime();
                if ((AlarmManagerService.TIME_CHANGED_MASK & result) != 0) {
                    long lastTimeChangeClockTime;
                    long expectedClockTime;
                    synchronized (AlarmManagerService.this.mLock) {
                        lastTimeChangeClockTime = AlarmManagerService.this.mLastTimeChangeClockTime;
                        expectedClockTime = lastTimeChangeClockTime + (nowELAPSED - AlarmManagerService.this.mLastTimeChangeRealtime);
                    }
                    if (lastTimeChangeClockTime != 0 && nowRTC >= expectedClockTime - 500) {
                        if (nowRTC > 500 + expectedClockTime) {
                        }
                    }
                    Flog.i(SystemService.PHASE_SYSTEM_SERVICES_READY, "Time changed notification from kernel; rebatching");
                    AlarmManagerService.this.removeImpl(AlarmManagerService.this.mTimeTickSender);
                    AlarmManagerService.this.rebatchAllAlarms();
                    AlarmManagerService.this.mClockReceiver.scheduleTimeTickEvent();
                    synchronized (AlarmManagerService.this.mLock) {
                        AlarmManagerService alarmManagerService = AlarmManagerService.this;
                        alarmManagerService.mNumTimeChanged += AlarmManagerService.TYPE_NONWAKEUP_MASK;
                        AlarmManagerService.this.mLastTimeChangeClockTime = nowRTC;
                        AlarmManagerService.this.mLastTimeChangeRealtime = nowELAPSED;
                    }
                    Intent intent = new Intent("android.intent.action.TIME_SET");
                    intent.addFlags(872415232);
                    AlarmManagerService.this.getContext().sendBroadcastAsUser(intent, UserHandle.ALL);
                    AlarmManagerService.this.hwRecordTimeChangeRTC(nowRTC, nowELAPSED, lastTimeChangeClockTime, expectedClockTime);
                    result |= AlarmManagerService.IS_WAKEUP_MASK;
                }
                if (result != AlarmManagerService.TIME_CHANGED_MASK) {
                    synchronized (AlarmManagerService.this.mLock) {
                        if (AlarmManagerService.this.triggerAlarmsLocked(triggerList, nowELAPSED, nowRTC) || !AlarmManagerService.this.checkAllowNonWakeupDelayLocked(nowELAPSED)) {
                            AlarmManagerService.this.rescheduleKernelAlarmsLocked();
                            AlarmManagerService.this.updateNextAlarmClockLocked();
                            if (AlarmManagerService.this.mPendingNonWakeupAlarms.size() > 0) {
                                AlarmManagerService.this.calculateDeliveryPriorities(AlarmManagerService.this.mPendingNonWakeupAlarms);
                                triggerList.addAll(AlarmManagerService.this.mPendingNonWakeupAlarms);
                                Collections.sort(triggerList, AlarmManagerService.this.mAlarmDispatchComparator);
                                long thisDelayTime = nowELAPSED - AlarmManagerService.this.mStartCurrentDelayTime;
                                alarmManagerService = AlarmManagerService.this;
                                alarmManagerService.mTotalDelayTime += thisDelayTime;
                                if (AlarmManagerService.this.mMaxDelayTime < thisDelayTime) {
                                    AlarmManagerService.this.mMaxDelayTime = thisDelayTime;
                                }
                                AlarmManagerService.this.mPendingNonWakeupAlarms.clear();
                            }
                            AlarmManagerService.this.deliverAlarmsLocked(triggerList, nowELAPSED);
                        } else {
                            Flog.i(SystemService.PHASE_SYSTEM_SERVICES_READY, "there are no wakeup alarms and the screen is off, we can delay what we have so far until the future");
                            if (AlarmManagerService.this.mPendingNonWakeupAlarms.size() == 0) {
                                AlarmManagerService.this.mStartCurrentDelayTime = nowELAPSED;
                                AlarmManagerService.this.mNextNonWakeupDeliveryTime = ((AlarmManagerService.this.currentNonWakeupFuzzLocked(nowELAPSED) * 3) / 2) + nowELAPSED;
                            }
                            AlarmManagerService.this.mPendingNonWakeupAlarms.addAll(triggerList);
                            alarmManagerService = AlarmManagerService.this;
                            alarmManagerService.mNumDelayedAlarms += triggerList.size();
                            AlarmManagerService.this.rescheduleKernelAlarmsLocked();
                            AlarmManagerService.this.updateNextAlarmClockLocked();
                        }
                    }
                } else {
                    synchronized (AlarmManagerService.this.mLock) {
                        AlarmManagerService.this.rescheduleKernelAlarmsLocked();
                    }
                }
            }
        }
    }

    public final class Batch {
        public final ArrayList<Alarm> alarms;
        long end;
        int flags;
        boolean standalone;
        long start;

        Batch() {
            this.alarms = new ArrayList();
            this.start = 0;
            this.end = JobStatus.NO_LATEST_RUNTIME;
            this.flags = AlarmManagerService.PRIO_TICK;
        }

        Batch(Alarm seed) {
            this.alarms = new ArrayList();
            this.start = seed.whenElapsed;
            this.end = seed.maxWhenElapsed;
            this.flags = seed.flags;
            this.alarms.add(seed);
        }

        int size() {
            return this.alarms.size();
        }

        Alarm get(int index) {
            return (Alarm) this.alarms.get(index);
        }

        boolean canHold(long whenElapsed, long maxWhen) {
            return (this.end < whenElapsed || this.start > maxWhen) ? AlarmManagerService.RECORD_DEVICE_IDLE_ALARMS : AlarmManagerService.RECORD_ALARMS_IN_HISTORY;
        }

        boolean add(Alarm alarm) {
            boolean newStart = AlarmManagerService.RECORD_DEVICE_IDLE_ALARMS;
            int index = Collections.binarySearch(this.alarms, alarm, AlarmManagerService.sIncreasingTimeOrder);
            if (index < 0) {
                index = (0 - index) - 1;
            }
            this.alarms.add(index, alarm);
            if (alarm.whenElapsed > this.start) {
                this.start = alarm.whenElapsed;
                newStart = AlarmManagerService.RECORD_ALARMS_IN_HISTORY;
            }
            if (alarm.maxWhenElapsed < this.end) {
                this.end = alarm.maxWhenElapsed;
            }
            this.flags |= alarm.flags;
            return newStart;
        }

        boolean remove(PendingIntent operation, IAlarmListener listener) {
            if (operation == null && listener == null) {
                return AlarmManagerService.RECORD_DEVICE_IDLE_ALARMS;
            }
            boolean didRemove = AlarmManagerService.RECORD_DEVICE_IDLE_ALARMS;
            long newStart = 0;
            long newEnd = JobStatus.NO_LATEST_RUNTIME;
            int newFlags = AlarmManagerService.PRIO_TICK;
            int i = AlarmManagerService.PRIO_TICK;
            while (i < this.alarms.size()) {
                Alarm alarm = (Alarm) this.alarms.get(i);
                if (alarm.matches(operation, listener) || HwActivityManagerServiceUtil.isPendingIntentCanceled(alarm.operation)) {
                    this.alarms.remove(i);
                    didRemove = AlarmManagerService.RECORD_ALARMS_IN_HISTORY;
                    AlarmManagerService.this.hwRemoveRtcAlarm(alarm);
                    if (alarm.alarmClock != null) {
                        AlarmManagerService.this.mNextAlarmClockMayChange = AlarmManagerService.RECORD_ALARMS_IN_HISTORY;
                    }
                } else {
                    if (alarm.whenElapsed > newStart) {
                        newStart = alarm.whenElapsed;
                    }
                    if (alarm.maxWhenElapsed < newEnd) {
                        newEnd = alarm.maxWhenElapsed;
                    }
                    newFlags |= alarm.flags;
                    i += AlarmManagerService.TYPE_NONWAKEUP_MASK;
                }
            }
            if (didRemove) {
                this.start = newStart;
                this.end = newEnd;
                this.flags = newFlags;
            }
            return didRemove;
        }

        boolean remove(String packageName) {
            return remove(packageName, null);
        }

        boolean remove(String packageName, String action) {
            if (packageName == null) {
                return AlarmManagerService.RECORD_DEVICE_IDLE_ALARMS;
            }
            boolean didRemove = AlarmManagerService.RECORD_DEVICE_IDLE_ALARMS;
            long newStart = 0;
            long newEnd = JobStatus.NO_LATEST_RUNTIME;
            int newFlags = AlarmManagerService.PRIO_TICK;
            for (int i = this.alarms.size() - 1; i >= 0; i--) {
                Alarm alarm = (Alarm) this.alarms.get(i);
                if (alarm.matches(packageName)) {
                    if (action != null && alarm.operation != null && !action.equals(AlarmManagerService.resetActionCallingIdentity(alarm.operation))) {
                        break;
                    }
                    this.alarms.remove(i);
                    didRemove = AlarmManagerService.RECORD_ALARMS_IN_HISTORY;
                    if (action != null) {
                        Slog.d(AlarmManagerService.TAG, "remove package alarm " + packageName + "' (" + action + ")");
                    }
                    AlarmManagerService.this.hwRemoveRtcAlarm(alarm);
                    if (alarm.alarmClock != null) {
                        AlarmManagerService.this.mNextAlarmClockMayChange = AlarmManagerService.RECORD_ALARMS_IN_HISTORY;
                    }
                } else {
                    if (alarm.whenElapsed > newStart) {
                        newStart = alarm.whenElapsed;
                    }
                    if (alarm.maxWhenElapsed < newEnd) {
                        newEnd = alarm.maxWhenElapsed;
                    }
                    newFlags |= alarm.flags;
                }
            }
            if (didRemove) {
                this.start = newStart;
                this.end = newEnd;
                this.flags = newFlags;
            }
            return didRemove;
        }

        boolean removeForStopped(int uid) {
            boolean didRemove = AlarmManagerService.RECORD_DEVICE_IDLE_ALARMS;
            long newStart = 0;
            long newEnd = JobStatus.NO_LATEST_RUNTIME;
            int newFlags = AlarmManagerService.PRIO_TICK;
            int i = this.alarms.size() - 1;
            while (i >= 0) {
                Alarm alarm = (Alarm) this.alarms.get(i);
                try {
                    if (alarm.uid == uid && ActivityManagerNative.getDefault().getAppStartMode(uid, alarm.packageName) == AlarmManagerService.RTC_MASK) {
                        this.alarms.remove(i);
                        didRemove = AlarmManagerService.RECORD_ALARMS_IN_HISTORY;
                        if (alarm.alarmClock != null) {
                            AlarmManagerService.this.mNextAlarmClockMayChange = AlarmManagerService.RECORD_ALARMS_IN_HISTORY;
                        }
                        i--;
                    } else {
                        if (alarm.whenElapsed > newStart) {
                            newStart = alarm.whenElapsed;
                        }
                        if (alarm.maxWhenElapsed < newEnd) {
                            newEnd = alarm.maxWhenElapsed;
                        }
                        newFlags |= alarm.flags;
                        i--;
                    }
                } catch (RemoteException e) {
                }
            }
            if (didRemove) {
                this.start = newStart;
                this.end = newEnd;
                this.flags = newFlags;
            }
            return didRemove;
        }

        boolean remove(int userHandle) {
            boolean didRemove = AlarmManagerService.RECORD_DEVICE_IDLE_ALARMS;
            long newStart = 0;
            long newEnd = JobStatus.NO_LATEST_RUNTIME;
            int i = AlarmManagerService.PRIO_TICK;
            while (i < this.alarms.size()) {
                Alarm alarm = (Alarm) this.alarms.get(i);
                if (UserHandle.getUserId(alarm.creatorUid) == userHandle) {
                    this.alarms.remove(i);
                    didRemove = AlarmManagerService.RECORD_ALARMS_IN_HISTORY;
                    AlarmManagerService.this.hwRemoveRtcAlarm(alarm);
                    if (alarm.alarmClock != null) {
                        AlarmManagerService.this.mNextAlarmClockMayChange = AlarmManagerService.RECORD_ALARMS_IN_HISTORY;
                    }
                } else {
                    if (alarm.whenElapsed > newStart) {
                        newStart = alarm.whenElapsed;
                    }
                    if (alarm.maxWhenElapsed < newEnd) {
                        newEnd = alarm.maxWhenElapsed;
                    }
                    i += AlarmManagerService.TYPE_NONWAKEUP_MASK;
                }
            }
            if (didRemove) {
                this.start = newStart;
                this.end = newEnd;
            }
            return didRemove;
        }

        boolean hasPackage(String packageName) {
            int N = this.alarms.size();
            for (int i = AlarmManagerService.PRIO_TICK; i < N; i += AlarmManagerService.TYPE_NONWAKEUP_MASK) {
                if (((Alarm) this.alarms.get(i)).matches(packageName)) {
                    return AlarmManagerService.RECORD_ALARMS_IN_HISTORY;
                }
            }
            return AlarmManagerService.RECORD_DEVICE_IDLE_ALARMS;
        }

        boolean hasWakeups() {
            int N = this.alarms.size();
            for (int i = AlarmManagerService.PRIO_TICK; i < N; i += AlarmManagerService.TYPE_NONWAKEUP_MASK) {
                if ((((Alarm) this.alarms.get(i)).type & AlarmManagerService.TYPE_NONWAKEUP_MASK) == 0) {
                    return AlarmManagerService.RECORD_ALARMS_IN_HISTORY;
                }
            }
            return AlarmManagerService.RECORD_DEVICE_IDLE_ALARMS;
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
    }

    static class BatchTimeOrder implements Comparator<Batch> {
        BatchTimeOrder() {
        }

        public int compare(Batch b1, Batch b2) {
            if (!(b1 == null || b2 == null)) {
                long when1 = b1.start;
                long when2 = b2.start;
                if (when1 > when2) {
                    return AlarmManagerService.TYPE_NONWAKEUP_MASK;
                }
                if (when1 < when2) {
                    return -1;
                }
            }
            return AlarmManagerService.PRIO_TICK;
        }
    }

    static final class BroadcastStats {
        long aggregateTime;
        int count;
        final ArrayMap<String, FilterStats> filterStats;
        final String mPackageName;
        final int mUid;
        int nesting;
        int numWakeup;
        long startTime;

        BroadcastStats(int uid, String packageName) {
            this.filterStats = new ArrayMap();
            this.mUid = uid;
            this.mPackageName = packageName;
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
                scheduleTimeTickEvent();
            } else if (intent.getAction().equals("android.intent.action.DATE_CHANGED")) {
                Flog.i(SystemService.PHASE_SYSTEM_SERVICES_READY, "Received DATE_CHANGED alarm; rescheduling");
                AlarmManagerService.this.setKernelTimezone(AlarmManagerService.this.mNativeData, -(TimeZone.getTimeZone(SystemProperties.get(AlarmManagerService.TIMEZONE_PROPERTY)).getOffset(System.currentTimeMillis()) / 60000));
                scheduleDateChangedEvent();
            }
        }

        public void scheduleTimeTickEvent() {
            long currentTime = System.currentTimeMillis();
            long triggerAtTime = SystemClock.elapsedRealtime() + ((60000 * ((currentTime / 60000) + 1)) - currentTime);
            Flog.i(SystemService.PHASE_SYSTEM_SERVICES_READY, "scheduleTimeTickEvent triggerAtTime = " + triggerAtTime);
            AlarmManagerService.this.setImpl(3, triggerAtTime, 0, 0, AlarmManagerService.this.mTimeTickSender, null, "time_tick", AlarmManagerService.TYPE_NONWAKEUP_MASK, null, null, Process.myUid(), "android");
        }

        public void scheduleDateChangedEvent() {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(10, AlarmManagerService.PRIO_TICK);
            calendar.set(12, AlarmManagerService.PRIO_TICK);
            calendar.set(13, AlarmManagerService.PRIO_TICK);
            calendar.set(14, AlarmManagerService.PRIO_TICK);
            calendar.add(AlarmManagerService.IS_WAKEUP_MASK, AlarmManagerService.TYPE_NONWAKEUP_MASK);
            AlarmManagerService.this.setImpl(AlarmManagerService.TYPE_NONWAKEUP_MASK, calendar.getTimeInMillis(), 0, 0, AlarmManagerService.this.mDateChangeSender, null, null, AlarmManagerService.TYPE_NONWAKEUP_MASK, null, null, Process.myUid(), "android");
        }
    }

    private final class Constants extends ContentObserver {
        private static final long DEFAULT_ALLOW_WHILE_IDLE_LONG_TIME = 540000;
        private static final long DEFAULT_ALLOW_WHILE_IDLE_SHORT_TIME = 5000;
        private static final long DEFAULT_ALLOW_WHILE_IDLE_WHITELIST_DURATION = 10000;
        private static final long DEFAULT_LISTENER_TIMEOUT = 5000;
        private static final long DEFAULT_MIN_FUTURITY = 5000;
        private static final long DEFAULT_MIN_INTERVAL = 60000;
        private static final String KEY_ALLOW_WHILE_IDLE_LONG_TIME = "allow_while_idle_long_time";
        private static final String KEY_ALLOW_WHILE_IDLE_SHORT_TIME = "allow_while_idle_short_time";
        private static final String KEY_ALLOW_WHILE_IDLE_WHITELIST_DURATION = "allow_while_idle_whitelist_duration";
        private static final String KEY_LISTENER_TIMEOUT = "listener_timeout";
        private static final String KEY_MIN_FUTURITY = "min_futurity";
        private static final String KEY_MIN_INTERVAL = "min_interval";
        public long ALLOW_WHILE_IDLE_LONG_TIME;
        public long ALLOW_WHILE_IDLE_SHORT_TIME;
        public long ALLOW_WHILE_IDLE_WHITELIST_DURATION;
        public long LISTENER_TIMEOUT;
        public long MIN_FUTURITY;
        public long MIN_INTERVAL;
        private long mLastAllowWhileIdleWhitelistDuration;
        private final KeyValueListParser mParser;
        private ContentResolver mResolver;

        public Constants(Handler handler) {
            super(handler);
            this.MIN_FUTURITY = DEFAULT_MIN_FUTURITY;
            this.MIN_INTERVAL = DEFAULT_MIN_INTERVAL;
            this.ALLOW_WHILE_IDLE_SHORT_TIME = DEFAULT_MIN_FUTURITY;
            this.ALLOW_WHILE_IDLE_LONG_TIME = DEFAULT_ALLOW_WHILE_IDLE_LONG_TIME;
            this.ALLOW_WHILE_IDLE_WHITELIST_DURATION = DEFAULT_ALLOW_WHILE_IDLE_WHITELIST_DURATION;
            this.LISTENER_TIMEOUT = DEFAULT_MIN_FUTURITY;
            this.mParser = new KeyValueListParser(',');
            this.mLastAllowWhileIdleWhitelistDuration = -1;
            updateAllowWhileIdleMinTimeLocked();
            updateAllowWhileIdleWhitelistDurationLocked();
        }

        public void start(ContentResolver resolver) {
            this.mResolver = resolver;
            this.mResolver.registerContentObserver(Global.getUriFor("alarm_manager_constants"), AlarmManagerService.RECORD_DEVICE_IDLE_ALARMS, this);
            updateConstants();
        }

        public void updateAllowWhileIdleMinTimeLocked() {
            AlarmManagerService.this.mAllowWhileIdleMinTime = AlarmManagerService.this.mPendingIdleUntil != null ? this.ALLOW_WHILE_IDLE_LONG_TIME : this.ALLOW_WHILE_IDLE_SHORT_TIME;
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
                    this.mParser.setString(Global.getString(this.mResolver, "alarm_manager_constants"));
                } catch (IllegalArgumentException e) {
                    Slog.e(AlarmManagerService.TAG, "Bad device idle settings", e);
                }
                this.MIN_FUTURITY = this.mParser.getLong(KEY_MIN_FUTURITY, DEFAULT_MIN_FUTURITY);
                this.MIN_INTERVAL = this.mParser.getLong(KEY_MIN_INTERVAL, DEFAULT_MIN_INTERVAL);
                this.ALLOW_WHILE_IDLE_SHORT_TIME = this.mParser.getLong(KEY_ALLOW_WHILE_IDLE_SHORT_TIME, DEFAULT_MIN_FUTURITY);
                this.ALLOW_WHILE_IDLE_LONG_TIME = this.mParser.getLong(KEY_ALLOW_WHILE_IDLE_LONG_TIME, DEFAULT_ALLOW_WHILE_IDLE_LONG_TIME);
                this.ALLOW_WHILE_IDLE_WHITELIST_DURATION = this.mParser.getLong(KEY_ALLOW_WHILE_IDLE_WHITELIST_DURATION, DEFAULT_ALLOW_WHILE_IDLE_WHITELIST_DURATION);
                this.LISTENER_TIMEOUT = this.mParser.getLong(KEY_LISTENER_TIMEOUT, DEFAULT_MIN_FUTURITY);
                updateAllowWhileIdleMinTimeLocked();
                updateAllowWhileIdleWhitelistDurationLocked();
            }
        }

        void dump(PrintWriter pw) {
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
        }
    }

    class DeliveryTracker extends Stub implements OnFinished {
        DeliveryTracker() {
        }

        private InFlight removeLocked(PendingIntent pi, Intent intent) {
            for (int i = AlarmManagerService.PRIO_TICK; i < AlarmManagerService.this.mInFlight.size(); i += AlarmManagerService.TYPE_NONWAKEUP_MASK) {
                if (((InFlight) AlarmManagerService.this.mInFlight.get(i)).mPendingIntent == pi) {
                    return (InFlight) AlarmManagerService.this.mInFlight.remove(i);
                }
            }
            AlarmManagerService.this.mLog.w("No in-flight alarm for " + pi + " " + intent);
            return null;
        }

        private InFlight removeLocked(IBinder listener) {
            for (int i = AlarmManagerService.PRIO_TICK; i < AlarmManagerService.this.mInFlight.size(); i += AlarmManagerService.TYPE_NONWAKEUP_MASK) {
                if (((InFlight) AlarmManagerService.this.mInFlight.get(i)).mListener == listener) {
                    return (InFlight) AlarmManagerService.this.mInFlight.remove(i);
                }
            }
            AlarmManagerService.this.mLog.w("No in-flight alarm for listener " + listener);
            return null;
        }

        private void updateStatsLocked(InFlight inflight) {
            long nowELAPSED = SystemClock.elapsedRealtime();
            BroadcastStats bs = inflight.mBroadcastStats;
            bs.nesting--;
            if (bs.nesting <= 0) {
                bs.nesting = AlarmManagerService.PRIO_TICK;
                bs.aggregateTime += nowELAPSED - bs.startTime;
            }
            FilterStats fs = inflight.mFilterStats;
            fs.nesting--;
            if (fs.nesting <= 0) {
                fs.nesting = AlarmManagerService.PRIO_TICK;
                fs.aggregateTime += nowELAPSED - fs.startTime;
            }
            if (inflight.mWorkSource == null || inflight.mWorkSource.size() <= 0) {
                ActivityManagerNative.noteAlarmFinish(inflight.mPendingIntent, inflight.mUid, inflight.mTag);
                return;
            }
            for (int wi = AlarmManagerService.PRIO_TICK; wi < inflight.mWorkSource.size(); wi += AlarmManagerService.TYPE_NONWAKEUP_MASK) {
                ActivityManagerNative.noteAlarmFinish(inflight.mPendingIntent, inflight.mWorkSource.get(wi), inflight.mTag);
            }
        }

        private void updateTrackingLocked(InFlight inflight) {
            if (inflight != null) {
                updateStatsLocked(inflight);
            }
            AlarmManagerService alarmManagerService = AlarmManagerService.this;
            alarmManagerService.mBroadcastRefCount--;
            if (AlarmManagerService.this.mBroadcastRefCount == 0) {
                AlarmManagerService.this.mHandler.obtainMessage(AlarmManagerService.ELAPSED_REALTIME_WAKEUP_MASK, Integer.valueOf(AlarmManagerService.PRIO_TICK)).sendToTarget();
                AlarmManagerService.this.mWakeLock.release();
                if (AlarmManagerService.this.mInFlight.size() > 0) {
                    AlarmManagerService.this.mLog.w("Finished all dispatches with " + AlarmManagerService.this.mInFlight.size() + " remaining inflights");
                    for (int i = AlarmManagerService.PRIO_TICK; i < AlarmManagerService.this.mInFlight.size(); i += AlarmManagerService.TYPE_NONWAKEUP_MASK) {
                        AlarmManagerService.this.mLog.w("  Remaining #" + i + ": " + AlarmManagerService.this.mInFlight.get(i));
                    }
                    AlarmManagerService.this.mInFlight.clear();
                }
            } else if (AlarmManagerService.this.mInFlight.size() > 0) {
                InFlight inFlight = (InFlight) AlarmManagerService.this.mInFlight.get(AlarmManagerService.PRIO_TICK);
                AlarmManagerService.this.setWakelockWorkSource(inFlight.mPendingIntent, inFlight.mWorkSource, inFlight.mAlarmType, inFlight.mTag, -1, AlarmManagerService.RECORD_DEVICE_IDLE_ALARMS);
            } else {
                AlarmManagerService.this.mLog.w("Alarm wakelock still held but sent queue empty");
                AlarmManagerService.this.mWakeLock.setWorkSource(null);
            }
        }

        public void alarmComplete(IBinder who) {
            if (who == null) {
                Slog.w(AlarmManagerService.TAG, "Invalid alarmComplete: uid=" + Binder.getCallingUid() + " pid=" + Binder.getCallingPid());
                return;
            }
            long ident = Binder.clearCallingIdentity();
            try {
                synchronized (AlarmManagerService.this.mLock) {
                    AlarmManagerService.this.mHandler.removeMessages(3, who);
                    InFlight inflight = removeLocked(who);
                    if (inflight != null) {
                        updateTrackingLocked(inflight);
                    }
                }
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public void onSendFinished(PendingIntent pi, Intent intent, int resultCode, String resultData, Bundle resultExtras) {
            synchronized (AlarmManagerService.this.mLock) {
                updateTrackingLocked(removeLocked(pi, intent));
            }
        }

        public void alarmTimedOut(IBinder who) {
            synchronized (AlarmManagerService.this.mLock) {
                InFlight inflight = removeLocked(who);
                if (inflight != null) {
                    updateTrackingLocked(inflight);
                }
            }
        }

        public void deliverLocked(Alarm alarm, long nowELAPSED, boolean allowWhileIdle) {
            if (alarm.operation != null) {
                AlarmManagerService.this.hwAddFirstFlagForRtcAlarm(alarm, AlarmManagerService.this.mBackgroundIntent);
                try {
                    alarm.operation.send(AlarmManagerService.this.getContext(), AlarmManagerService.PRIO_TICK, AlarmManagerService.this.mBackgroundIntent.putExtra("android.intent.extra.ALARM_COUNT", alarm.count), AlarmManagerService.this.mDeliveryTracker, AlarmManagerService.this.mHandler, null, allowWhileIdle ? AlarmManagerService.this.mIdleOptions : null);
                    AlarmManagerService.this.hwRemoveRtcAlarm(alarm);
                } catch (CanceledException e) {
                    if (alarm.repeatInterval > 0) {
                        AlarmManagerService.this.removeImpl(alarm.operation);
                    }
                    return;
                }
            }
            try {
                alarm.listener.doAlarm(this);
                AlarmManagerService.this.mHandler.sendMessageDelayed(AlarmManagerService.this.mHandler.obtainMessage(3, alarm.listener.asBinder()), AlarmManagerService.this.mConstants.LISTENER_TIMEOUT);
            } catch (Exception e2) {
                return;
            }
            if (AlarmManagerService.this.mBroadcastRefCount == 0) {
                AlarmManagerService.this.setWakelockWorkSource(alarm.operation, alarm.workSource, alarm.type, alarm.statsTag, alarm.operation == null ? alarm.uid : -1, AlarmManagerService.RECORD_ALARMS_IN_HISTORY);
                AlarmManagerService.this.mWakeLock.acquire();
                AlarmManagerService.this.mHandler.obtainMessage(AlarmManagerService.ELAPSED_REALTIME_WAKEUP_MASK, Integer.valueOf(AlarmManagerService.TYPE_NONWAKEUP_MASK)).sendToTarget();
            }
            InFlight inflight = new InFlight(AlarmManagerService.this, alarm.operation, alarm.listener, alarm.workSource, alarm.uid, alarm.packageName, alarm.type, alarm.statsTag, nowELAPSED);
            AlarmManagerService.this.mInFlight.add(inflight);
            AlarmManagerService alarmManagerService = AlarmManagerService.this;
            alarmManagerService.mBroadcastRefCount += AlarmManagerService.TYPE_NONWAKEUP_MASK;
            if (allowWhileIdle) {
                AlarmManagerService.this.mLastAllowWhileIdleDispatch.put(alarm.uid, nowELAPSED);
            }
            BroadcastStats bs = inflight.mBroadcastStats;
            bs.count += AlarmManagerService.TYPE_NONWAKEUP_MASK;
            if (bs.nesting == 0) {
                bs.nesting = AlarmManagerService.TYPE_NONWAKEUP_MASK;
                bs.startTime = nowELAPSED;
            } else {
                bs.nesting += AlarmManagerService.TYPE_NONWAKEUP_MASK;
            }
            FilterStats fs = inflight.mFilterStats;
            fs.count += AlarmManagerService.TYPE_NONWAKEUP_MASK;
            if (fs.nesting == 0) {
                fs.nesting = AlarmManagerService.TYPE_NONWAKEUP_MASK;
                fs.startTime = nowELAPSED;
            } else {
                fs.nesting += AlarmManagerService.TYPE_NONWAKEUP_MASK;
            }
            if (alarm.type == AlarmManagerService.RTC_MASK || alarm.type == 0) {
                bs.numWakeup += AlarmManagerService.TYPE_NONWAKEUP_MASK;
                fs.numWakeup += AlarmManagerService.TYPE_NONWAKEUP_MASK;
                if (alarm.workSource == null || alarm.workSource.size() <= 0) {
                    ActivityManagerNative.noteWakeupAlarm(alarm.operation, alarm.uid, alarm.packageName, alarm.statsTag);
                } else {
                    for (int wi = AlarmManagerService.PRIO_TICK; wi < alarm.workSource.size(); wi += AlarmManagerService.TYPE_NONWAKEUP_MASK) {
                        String wsName = alarm.workSource.getName(wi);
                        PendingIntent pendingIntent = alarm.operation;
                        int i = alarm.workSource.get(wi);
                        if (wsName == null) {
                            wsName = alarm.packageName;
                        }
                        ActivityManagerNative.noteWakeupAlarm(pendingIntent, i, wsName, alarm.statsTag);
                    }
                }
            }
            String pkg = alarm.packageName;
            if (!(pkg == null || (AlarmManagerService.this.mInteractive && "android".equals(pkg)))) {
                String intentStr = "";
                if (alarm.operation != null) {
                    Intent alarmIntent = alarm.operation.getIntent();
                    if (alarmIntent != null) {
                        intentStr = alarmIntent.getAction();
                    }
                } else if (alarm.statsTag != null) {
                    intentStr = alarm.statsTag;
                }
                String valueOf = String.valueOf(alarm.type);
                String valueOf2 = String.valueOf(alarm.repeatInterval);
                String[] strArr = new String[AlarmManagerService.RTC_MASK];
                strArr[AlarmManagerService.PRIO_TICK] = String.valueOf(fs.count);
                strArr[AlarmManagerService.TYPE_NONWAKEUP_MASK] = intentStr;
                LogPower.push(121, pkg, valueOf, valueOf2, strArr);
            }
            if (!AlarmManagerService.this.mIsScreenOn) {
                AlarmManagerService.this.recordAlarmForPtm(alarm);
            }
        }
    }

    static final class FilterStats {
        long aggregateTime;
        int count;
        long lastTime;
        final BroadcastStats mBroadcastStats;
        final String mTag;
        int nesting;
        int numWakeup;
        long startTime;

        FilterStats(BroadcastStats broadcastStats, String tag) {
            this.mBroadcastStats = broadcastStats;
            this.mTag = tag;
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
        final WorkSource mWorkSource;

        InFlight(AlarmManagerService service, PendingIntent pendingIntent, IAlarmListener listener, WorkSource workSource, int uid, String alarmPkg, int alarmType, String tag, long nowELAPSED) {
            BroadcastStats -wrap0;
            IBinder iBinder = null;
            this.mPendingIntent = pendingIntent;
            if (listener != null) {
                iBinder = listener.asBinder();
            }
            this.mListener = iBinder;
            this.mWorkSource = workSource;
            this.mUid = uid;
            this.mTag = tag;
            if (pendingIntent != null) {
                -wrap0 = service.getStatsLocked(pendingIntent);
            } else {
                -wrap0 = service.getStatsLocked(uid, alarmPkg);
            }
            this.mBroadcastStats = -wrap0;
            FilterStats fs = (FilterStats) this.mBroadcastStats.filterStats.get(this.mTag);
            if (fs == null) {
                fs = new FilterStats(this.mBroadcastStats, this.mTag);
                this.mBroadcastStats.filterStats.put(this.mTag, fs);
            }
            fs.lastTime = nowELAPSED;
            this.mFilterStats = fs;
            this.mAlarmType = alarmType;
        }
    }

    public static class IncreasingTimeOrder implements Comparator<Alarm> {
        public int compare(Alarm a1, Alarm a2) {
            long when1 = a1.whenElapsed;
            long when2 = a2.whenElapsed;
            if (when1 > when2) {
                return AlarmManagerService.TYPE_NONWAKEUP_MASK;
            }
            if (when1 < when2) {
                return -1;
            }
            return AlarmManagerService.PRIO_TICK;
        }
    }

    class InteractiveStateReceiver extends BroadcastReceiver {
        public InteractiveStateReceiver() {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.SCREEN_OFF");
            filter.addAction("android.intent.action.SCREEN_ON");
            filter.setPriority(ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE);
            AlarmManagerService.this.getContext().registerReceiver(this, filter);
        }

        public void onReceive(Context context, Intent intent) {
            synchronized (AlarmManagerService.this.mLock) {
                AlarmManagerService.this.interactiveStateChangedLocked("android.intent.action.SCREEN_ON".equals(intent.getAction()));
                if ("android.intent.action.SCREEN_ON".equals(intent.getAction())) {
                    AlarmManagerService.this.mIsScreenOn = AlarmManagerService.RECORD_ALARMS_IN_HISTORY;
                } else if ("android.intent.action.SCREEN_OFF".equals(intent.getAction())) {
                    AlarmManagerService.this.mIsScreenOn = AlarmManagerService.RECORD_DEVICE_IDLE_ALARMS;
                }
            }
        }
    }

    public final class LocalService {
        public void setDeviceIdleUserWhitelist(int[] appids) {
            AlarmManagerService.this.setDeviceIdleUserWhitelistImpl(appids);
        }
    }

    final class PriorityClass {
        int priority;
        int seq;

        PriorityClass() {
            this.seq = AlarmManagerService.this.mCurrentSeq - 1;
            this.priority = AlarmManagerService.RTC_MASK;
        }
    }

    final class UidObserver extends IUidObserver.Stub {
        UidObserver() {
        }

        public void onUidStateChanged(int uid, int procState) throws RemoteException {
        }

        public void onUidGone(int uid) throws RemoteException {
        }

        public void onUidActive(int uid) throws RemoteException {
        }

        public void onUidIdle(int uid) throws RemoteException {
            synchronized (AlarmManagerService.this.mLock) {
                AlarmManagerService.this.removeForStoppedLocked(uid);
            }
        }
    }

    class UninstallReceiver extends BroadcastReceiver {
        public UninstallReceiver() {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.PACKAGE_REMOVED");
            filter.addAction("android.intent.action.PACKAGE_RESTARTED");
            filter.addAction("android.intent.action.QUERY_PACKAGE_RESTART");
            filter.addDataScheme(HwBroadcastRadarUtil.KEY_PACKAGE);
            AlarmManagerService.this.getContext().registerReceiver(this, filter);
            IntentFilter sdFilter = new IntentFilter();
            sdFilter.addAction("android.intent.action.EXTERNAL_APPLICATIONS_UNAVAILABLE");
            sdFilter.addAction("android.intent.action.USER_STOPPED");
            sdFilter.addAction("android.intent.action.UID_REMOVED");
            AlarmManagerService.this.getContext().registerReceiver(this, sdFilter);
        }

        public void onReceive(Context context, Intent intent) {
            int i = AlarmManagerService.PRIO_TICK;
            synchronized (AlarmManagerService.this.mLock) {
                String action = intent.getAction();
                String[] strArr = null;
                int length;
                if ("android.intent.action.QUERY_PACKAGE_RESTART".equals(action)) {
                    strArr = intent.getStringArrayExtra("android.intent.extra.PACKAGES");
                    length = strArr.length;
                    while (i < length) {
                        if (AlarmManagerService.this.lookForPackageLocked(strArr[i])) {
                            setResultCode(-1);
                            return;
                        }
                        i += AlarmManagerService.TYPE_NONWAKEUP_MASK;
                    }
                    return;
                }
                String pkg;
                if ("android.intent.action.EXTERNAL_APPLICATIONS_UNAVAILABLE".equals(action)) {
                    strArr = intent.getStringArrayExtra("android.intent.extra.changed_package_list");
                } else if ("android.intent.action.USER_STOPPED".equals(action)) {
                    int userHandle = intent.getIntExtra("android.intent.extra.user_handle", -1);
                    if (userHandle >= 0) {
                        AlarmManagerService.this.removeUserLocked(userHandle);
                    }
                } else if ("android.intent.action.UID_REMOVED".equals(action)) {
                    int uid = intent.getIntExtra("android.intent.extra.UID", -1);
                    if (uid >= 0) {
                        AlarmManagerService.this.mLastAllowWhileIdleDispatch.delete(uid);
                    }
                } else if ("android.intent.action.PACKAGE_REMOVED".equals(action) && intent.getBooleanExtra("android.intent.extra.REPLACING", AlarmManagerService.RECORD_DEVICE_IDLE_ALARMS)) {
                    return;
                } else {
                    Uri data = intent.getData();
                    if (data != null) {
                        pkg = data.getSchemeSpecificPart();
                        if (pkg != null) {
                            strArr = new String[AlarmManagerService.TYPE_NONWAKEUP_MASK];
                            strArr[AlarmManagerService.PRIO_TICK] = pkg;
                        }
                    }
                }
                if (strArr != null && strArr.length > 0) {
                    length = strArr.length;
                    while (i < length) {
                        pkg = strArr[i];
                        AlarmManagerService.this.removeLocked(pkg);
                        AlarmManagerService.this.mPriorities.remove(pkg);
                        for (int i2 = AlarmManagerService.this.mBroadcastStats.size() - 1; i2 >= 0; i2--) {
                            ArrayMap<String, BroadcastStats> uidStats = (ArrayMap) AlarmManagerService.this.mBroadcastStats.valueAt(i2);
                            if (!(uidStats == null || uidStats.remove(pkg) == null || uidStats.size() > 0)) {
                                AlarmManagerService.this.mBroadcastStats.removeAt(i2);
                            }
                        }
                        i += AlarmManagerService.TYPE_NONWAKEUP_MASK;
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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.AlarmManagerService.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.AlarmManagerService.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.AlarmManagerService.<clinit>():void");
    }

    private native void close(long j);

    private native long init();

    private native void set(long j, int i, long j2, long j3);

    private native int setKernelTime(long j, long j2);

    private native int setKernelTimezone(long j, int i);

    private native int waitForAlarm(long j);

    protected native void hwSetClockRTC(long j, long j2, long j3);

    void calculateDeliveryPriorities(ArrayList<Alarm> alarms) {
        int N = alarms.size();
        for (int i = PRIO_TICK; i < N; i += TYPE_NONWAKEUP_MASK) {
            int alarmPrio;
            String alarmPackage;
            Alarm a = (Alarm) alarms.get(i);
            if (a.operation != null && "android.intent.action.TIME_TICK".equals(resetActionCallingIdentity(a.operation))) {
                alarmPrio = PRIO_TICK;
            } else if (a.wakeup) {
                alarmPrio = TYPE_NONWAKEUP_MASK;
            } else {
                alarmPrio = RTC_MASK;
            }
            PriorityClass priorityClass = a.priorityClass;
            if (a.operation != null) {
                alarmPackage = a.operation.getCreatorPackage();
            } else {
                alarmPackage = a.packageName;
            }
            if (priorityClass == null) {
                priorityClass = (PriorityClass) this.mPriorities.get(alarmPackage);
            }
            if (priorityClass == null) {
                priorityClass = new PriorityClass();
                a.priorityClass = priorityClass;
                this.mPriorities.put(alarmPackage, priorityClass);
            }
            a.priorityClass = priorityClass;
            if (priorityClass.seq != this.mCurrentSeq) {
                priorityClass.priority = alarmPrio;
                priorityClass.seq = this.mCurrentSeq;
            } else if (alarmPrio < priorityClass.priority) {
                priorityClass.priority = alarmPrio;
            }
        }
    }

    public AlarmManagerService(Context context) {
        super(context);
        this.mBackgroundIntent = new Intent().addFlags(ELAPSED_REALTIME_WAKEUP_MASK);
        this.mLog = new LocalLog(TAG);
        this.mIsScreenOn = RECORD_ALARMS_IN_HISTORY;
        this.mLock = new Object();
        this.mBroadcastRefCount = PRIO_TICK;
        this.mPendingNonWakeupAlarms = new ArrayList();
        this.mInFlight = new ArrayList();
        this.mHandler = new AlarmHandler();
        this.mDeliveryTracker = new DeliveryTracker();
        this.mCancelRemoveAction = RECORD_DEVICE_IDLE_ALARMS;
        this.mInteractive = RECORD_ALARMS_IN_HISTORY;
        this.mDeviceIdleUserWhitelist = new int[PRIO_TICK];
        this.mLastAllowWhileIdleDispatch = new SparseLongArray();
        this.mAllowWhileIdleDispatches = new ArrayList();
        this.mNextAlarmClockForUser = new SparseArray();
        this.mTmpSparseAlarmClockArray = new SparseArray();
        this.mPendingSendNextAlarmClockChangedForUser = new SparseBooleanArray();
        this.mHandlerSparseAlarmClockArray = new SparseArray();
        this.mPriorities = new HashMap();
        this.mCurrentSeq = PRIO_TICK;
        this.mRecentWakeups = new LinkedList();
        this.RECENT_WAKEUP_PERIOD = UnixCalendar.DAY_IN_MILLIS;
        this.mAlarmDispatchComparator = new Comparator<Alarm>() {
            public int compare(Alarm lhs, Alarm rhs) {
                if (lhs.priorityClass.priority < rhs.priorityClass.priority) {
                    return -1;
                }
                if (lhs.priorityClass.priority > rhs.priorityClass.priority) {
                    return AlarmManagerService.TYPE_NONWAKEUP_MASK;
                }
                if (lhs.whenElapsed < rhs.whenElapsed) {
                    return -1;
                }
                if (lhs.whenElapsed > rhs.whenElapsed) {
                    return AlarmManagerService.TYPE_NONWAKEUP_MASK;
                }
                return AlarmManagerService.PRIO_TICK;
            }
        };
        this.mAlarmBatches = new ArrayList();
        this.mPendingIdleUntil = null;
        this.mNextWakeFromIdle = null;
        this.mPendingWhileIdleAlarms = new ArrayList();
        this.mBroadcastStats = new SparseArray();
        this.mNumDelayedAlarms = PRIO_TICK;
        this.mTotalDelayTime = 0;
        this.mMaxDelayTime = 0;
        this.mService = new IAlarmManager.Stub() {
            public void set(String callingPackage, int type, long triggerAtTime, long windowLength, long interval, int flags, PendingIntent operation, IAlarmListener directReceiver, String listenerTag, WorkSource workSource, AlarmClockInfo alarmClock) {
                int callingUid = Binder.getCallingUid();
                AlarmManagerService.this.mAppOps.checkPackage(callingUid, callingPackage);
                if (interval == 0 || directReceiver == null) {
                    if (workSource != null) {
                        AlarmManagerService.this.getContext().enforcePermission("android.permission.UPDATE_DEVICE_STATS", Binder.getCallingPid(), callingUid, "AlarmManager.set");
                    }
                    flags &= -11;
                    if (callingUid != 1000) {
                        flags &= -17;
                    }
                    if (windowLength == 0) {
                        flags |= AlarmManagerService.TYPE_NONWAKEUP_MASK;
                    }
                    if (alarmClock != null) {
                        flags |= 3;
                    } else {
                        if (workSource != null || (callingUid >= 10000 && Arrays.binarySearch(AlarmManagerService.this.mDeviceIdleUserWhitelist, UserHandle.getAppId(callingUid)) < 0)) {
                            if (!AlarmManagerService.this.isContainsAppUidInWorksource(workSource, "com.android.email")) {
                                if (!AlarmManagerService.this.isContainsAppUidInWorksource(workSource, "com.android.exchange")) {
                                    if (AlarmManagerService.this.isContainsAppUidInWorksource(workSource, "com.google.android.gm")) {
                                    }
                                }
                            }
                        }
                        flags = (flags | AlarmManagerService.ELAPSED_REALTIME_MASK) & -5;
                    }
                    AlarmManagerService.this.setImpl(type, triggerAtTime, windowLength, interval, operation, directReceiver, listenerTag, flags, workSource, alarmClock, callingUid, callingPackage);
                    return;
                }
                throw new IllegalArgumentException("Repeating alarms cannot use AlarmReceivers");
            }

            public boolean setTime(long millis) {
                boolean z = AlarmManagerService.RECORD_DEVICE_IDLE_ALARMS;
                AlarmManagerService.this.getContext().enforceCallingOrSelfPermission("android.permission.SET_TIME", "setTime");
                int uid = Binder.getCallingUid();
                Flog.i(SystemService.PHASE_SYSTEM_SERVICES_READY, "setTime,uid is " + uid + ",pid is " + Binder.getCallingPid());
                if (AlarmManagerService.this.mNativeData == 0) {
                    Slog.w(AlarmManagerService.TAG, "Not setting time since no alarm driver is available.");
                    return AlarmManagerService.RECORD_DEVICE_IDLE_ALARMS;
                }
                synchronized (AlarmManagerService.this.mLock) {
                    HwLog.bdate("BDAT_TAG_TIME_CHANGED", "delta=" + (millis - System.currentTimeMillis()));
                    if (AlarmManagerService.this.setKernelTime(AlarmManagerService.this.mNativeData, millis) == 0) {
                        z = AlarmManagerService.RECORD_ALARMS_IN_HISTORY;
                    }
                }
                return z;
            }

            public void setTimeZone(String tz) {
                AlarmManagerService.this.getContext().enforceCallingOrSelfPermission("android.permission.SET_TIME_ZONE", "setTimeZone");
                int uid = Binder.getCallingUid();
                Flog.i(SystemService.PHASE_SYSTEM_SERVICES_READY, "setTimeZoneImpl,uid is " + uid + ",pid is " + Binder.getCallingPid() + ",tz = " + tz);
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

            public AlarmClockInfo getNextAlarmClock(int userId) {
                return AlarmManagerService.this.getNextAlarmClockImpl(ActivityManager.handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), userId, AlarmManagerService.RECORD_DEVICE_IDLE_ALARMS, AlarmManagerService.RECORD_DEVICE_IDLE_ALARMS, "getNextAlarmClock", null));
            }

            protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
                if (AlarmManagerService.this.getContext().checkCallingOrSelfPermission("android.permission.DUMP") != 0) {
                    pw.println("Permission Denial: can't dump AlarmManager from from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
                } else {
                    AlarmManagerService.this.dumpImpl(pw);
                }
            }

            public void updateBlockedUids(int uid, boolean isBlocked) {
            }

            public int getWakeUpNum(int uid, String pkg) {
                if (ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE == Binder.getCallingUid()) {
                    return AlarmManagerService.this.getWakeUpNumImpl(uid, pkg);
                }
                Slog.i(AlarmManagerService.TAG, "getWakeUpNum: permission not allowed.");
                return AlarmManagerService.PRIO_TICK;
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
        };
        this.mConstants = new Constants(this.mHandler);
    }

    static long convertToElapsed(long when, int type) {
        boolean isRtc = RECORD_ALARMS_IN_HISTORY;
        if (!(type == TYPE_NONWAKEUP_MASK || type == 0)) {
            isRtc = RECORD_DEVICE_IDLE_ALARMS;
        }
        if (isRtc) {
            return when - (System.currentTimeMillis() - SystemClock.elapsedRealtime());
        }
        return when;
    }

    static long maxTriggerTime(long now, long triggerAtTime, long interval) {
        long futurity;
        if (interval == 0) {
            futurity = triggerAtTime - now;
        } else {
            futurity = interval;
        }
        if (futurity < MIN_FUZZABLE_INTERVAL) {
            futurity = 0;
        }
        return ((long) (((double) futurity) * 0.75d)) + triggerAtTime;
    }

    static boolean addBatchLocked(ArrayList<Batch> list, Batch newBatch) {
        int index = Collections.binarySearch(list, newBatch, sBatchOrder);
        if (index < 0) {
            index = (0 - index) - 1;
        }
        list.add(index, newBatch);
        if (index == 0) {
            return RECORD_ALARMS_IN_HISTORY;
        }
        return RECORD_DEVICE_IDLE_ALARMS;
    }

    int attemptCoalesceLocked(long whenElapsed, long maxWhen) {
        int N = this.mAlarmBatches.size();
        for (int i = PRIO_TICK; i < N; i += TYPE_NONWAKEUP_MASK) {
            Batch b = (Batch) this.mAlarmBatches.get(i);
            if ((b.flags & TYPE_NONWAKEUP_MASK) == 0 && b.canHold(whenElapsed, maxWhen)) {
                return i;
            }
        }
        return -1;
    }

    void rebatchAllAlarms() {
        synchronized (this.mLock) {
            rebatchAllAlarmsLocked(RECORD_ALARMS_IN_HISTORY);
        }
    }

    void rebatchAllAlarmsLocked(boolean doValidate) {
        ArrayList<Batch> oldSet = (ArrayList) this.mAlarmBatches.clone();
        this.mAlarmBatches.clear();
        Alarm oldPendingIdleUntil = this.mPendingIdleUntil;
        long nowElapsed = SystemClock.elapsedRealtime();
        int oldBatches = oldSet.size();
        this.mCancelRemoveAction = RECORD_ALARMS_IN_HISTORY;
        for (int batchNum = PRIO_TICK; batchNum < oldBatches; batchNum += TYPE_NONWAKEUP_MASK) {
            Batch batch = (Batch) oldSet.get(batchNum);
            int N = batch.size();
            for (int i = PRIO_TICK; i < N; i += TYPE_NONWAKEUP_MASK) {
                reAddAlarmLocked(batch.get(i), nowElapsed, doValidate);
            }
        }
        this.mCancelRemoveAction = RECORD_DEVICE_IDLE_ALARMS;
        if (!(oldPendingIdleUntil == null || oldPendingIdleUntil == this.mPendingIdleUntil)) {
            Slog.wtf(TAG, "Rebatching: idle until changed from " + oldPendingIdleUntil + " to " + this.mPendingIdleUntil);
            if (this.mPendingIdleUntil == null) {
                restorePendingWhileIdleAlarmsLocked();
            }
        }
        rescheduleKernelAlarmsLocked();
        updateNextAlarmClockLocked();
    }

    void reAddAlarmLocked(Alarm a, long nowElapsed, boolean doValidate) {
        long maxElapsed;
        a.when = a.origWhen;
        long whenElapsed = convertToElapsed(a.when, a.type);
        if (a.windowLength == 0) {
            maxElapsed = whenElapsed;
        } else if (a.windowLength > 0) {
            maxElapsed = whenElapsed + a.windowLength;
        } else {
            maxElapsed = maxTriggerTime(nowElapsed, whenElapsed, a.repeatInterval);
        }
        a.whenElapsed = whenElapsed;
        a.maxWhenElapsed = maxElapsed;
        setImplLocked(a, RECORD_ALARMS_IN_HISTORY, doValidate);
    }

    void restorePendingWhileIdleAlarmsLocked() {
        if (this.mPendingWhileIdleAlarms.size() > 0) {
            ArrayList<Alarm> alarms = this.mPendingWhileIdleAlarms;
            this.mPendingWhileIdleAlarms = new ArrayList();
            long nowElapsed = SystemClock.elapsedRealtime();
            for (int i = alarms.size() - 1; i >= 0; i--) {
                reAddAlarmLocked((Alarm) alarms.get(i), nowElapsed, RECORD_DEVICE_IDLE_ALARMS);
            }
        }
        this.mConstants.updateAllowWhileIdleMinTimeLocked();
        rescheduleKernelAlarmsLocked();
        updateNextAlarmClockLocked();
        try {
            this.mTimeTickSender.send();
        } catch (CanceledException e) {
        }
    }

    public void onStart() {
        this.mNativeData = init();
        this.mNextNonWakeup = 0;
        this.mNextWakeup = 0;
        Flog.d(SystemService.PHASE_SYSTEM_SERVICES_READY, "alarmmanagerservice onStart");
        setTimeZoneImpl(SystemProperties.get(TIMEZONE_PROPERTY));
        this.mWakeLock = ((PowerManager) getContext().getSystemService("power")).newWakeLock(TYPE_NONWAKEUP_MASK, "*alarm*");
        this.mTimeTickSender = PendingIntent.getBroadcastAsUser(getContext(), PRIO_TICK, new Intent("android.intent.action.TIME_TICK").addFlags(1342177280), PRIO_TICK, UserHandle.ALL);
        Intent intent = new Intent("android.intent.action.DATE_CHANGED");
        intent.addFlags(536870912);
        this.mDateChangeSender = PendingIntent.getBroadcastAsUser(getContext(), PRIO_TICK, intent, 67108864, UserHandle.ALL);
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
            ActivityManagerNative.getDefault().registerUidObserver(new UidObserver(), ELAPSED_REALTIME_WAKEUP_MASK);
        } catch (RemoteException e) {
        }
        publishBinderService("alarm", this.mService);
        publishLocalService(LocalService.class, new LocalService());
    }

    public void onBootPhase(int phase) {
        if (phase == SystemService.PHASE_SYSTEM_SERVICES_READY) {
            this.mConstants.start(getContext().getContentResolver());
            this.mAppOps = (AppOpsManager) getContext().getSystemService("appops");
            this.mLocalDeviceIdleController = (com.android.server.DeviceIdleController.LocalService) LocalServices.getService(com.android.server.DeviceIdleController.LocalService.class);
        }
    }

    protected void finalize() throws Throwable {
        try {
            close(this.mNativeData);
        } finally {
            super.finalize();
        }
    }

    void setTimeZoneImpl(String tz) {
        if (!TextUtils.isEmpty(tz)) {
            TimeZone zone = TimeZone.getTimeZone(tz);
            boolean timeZoneWasChanged = RECORD_DEVICE_IDLE_ALARMS;
            synchronized (this) {
                String current = SystemProperties.get(TIMEZONE_PROPERTY);
                if (current == null || !current.equals(zone.getID())) {
                    Flog.i(SystemService.PHASE_SYSTEM_SERVICES_READY, "timezone changed: " + current + ", new=" + zone.getID());
                    timeZoneWasChanged = RECORD_ALARMS_IN_HISTORY;
                    SystemProperties.set(TIMEZONE_PROPERTY, zone.getID());
                }
                setKernelTimezone(this.mNativeData, -(zone.getOffset(System.currentTimeMillis()) / 60000));
            }
            TimeZone.setDefault(null);
            if (timeZoneWasChanged) {
                Intent intent = new Intent("android.intent.action.TIMEZONE_CHANGED");
                intent.addFlags(536870912);
                intent.putExtra("time-zone", zone.getID());
                getContext().sendBroadcastAsUser(intent, UserHandle.ALL);
            }
        }
    }

    void removeImpl(String pkg, String action) {
        if (pkg != null && action != null) {
            synchronized (this.mLock) {
                removeLocked(pkg, action);
            }
        }
    }

    void removeImpl(PendingIntent operation) {
        if (operation != null) {
            synchronized (this.mLock) {
                removeLocked(operation, null);
            }
            removeDeskClockFromFWK(operation);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void setImpl(int type, long triggerAtTime, long windowLength, long interval, PendingIntent operation, IAlarmListener directReceiver, String listenerTag, int flags, WorkSource workSource, AlarmClockInfo alarmClock, int callingUid, String callingPackage) {
        if (!(operation == null && directReceiver == null) && (operation == null || directReceiver == null)) {
            if (windowLength > 43200000) {
                Slog.w(TAG, "Window length " + windowLength + "ms suspiciously long; limiting to 1 hour");
                windowLength = 3600000;
            }
            long minInterval = this.mConstants.MIN_INTERVAL;
            if (interval > 0 && interval < minInterval) {
                Slog.w(TAG, "Suspiciously short interval " + interval + " millis; expanding to " + (minInterval / 1000) + " seconds");
                interval = minInterval;
            }
            if (type < 0 || type > 3) {
                throw new IllegalArgumentException("Invalid alarm type " + type);
            }
            long maxElapsed;
            if (triggerAtTime < 0) {
                Slog.w(TAG, "Invalid alarm trigger time! " + triggerAtTime + " from uid=" + callingUid + " pid=" + ((long) Binder.getCallingPid()));
                triggerAtTime = 0;
            }
            long nowElapsed = SystemClock.elapsedRealtime();
            long nominalTrigger = convertToElapsed(triggerAtTime, type);
            long minTrigger = nowElapsed + this.mConstants.MIN_FUTURITY;
            long triggerElapsed = nominalTrigger > minTrigger ? nominalTrigger : minTrigger;
            if (windowLength == 0) {
                maxElapsed = triggerElapsed;
            } else if (windowLength < 0) {
                maxElapsed = maxTriggerTime(nowElapsed, triggerElapsed, interval);
                windowLength = maxElapsed - triggerElapsed;
            } else {
                maxElapsed = triggerElapsed + windowLength;
            }
            synchronized (this.mLock) {
                if (operation != null) {
                    int count = PRIO_TICK;
                    String pkg = operation.getTargetPackage();
                    loop0:
                    for (int i = PRIO_TICK; i < this.mAlarmBatches.size(); i += TYPE_NONWAKEUP_MASK) {
                        Batch batch = (Batch) this.mAlarmBatches.get(i);
                        int j = PRIO_TICK;
                        while (batch != null && j < batch.size()) {
                            Alarm alarm = batch.get(j);
                            if (!(alarm.operation == null || pkg == null)) {
                                if (pkg.equals("android")) {
                                    continue;
                                } else {
                                    if (pkg.equals(alarm.operation.getTargetPackage())) {
                                        count += TYPE_NONWAKEUP_MASK;
                                    }
                                }
                            }
                            j += TYPE_NONWAKEUP_MASK;
                        }
                    }
                    if (this.mAlarmResource == null) {
                        this.mAlarmResource = HwFrameworkFactory.getHwResource(13);
                    }
                    if (this.mAlarmResource != null && RTC_MASK == this.mAlarmResource.acquire(operation.getCreatorUid(), pkg, -1, count)) {
                        return;
                    }
                }
                if (type == 0 || RTC_MASK == type) {
                    Flog.i(SystemService.PHASE_SYSTEM_SERVICES_READY, "set(" + operation + ") : type=" + type + " triggerAtTime=" + triggerAtTime + " win=" + windowLength + " tElapsed=" + triggerElapsed + " maxElapsed=" + maxElapsed + " interval=" + interval + " flags=0x" + Integer.toHexString(flags));
                }
                setImplLocked(type, triggerAtTime, triggerElapsed, windowLength, maxElapsed, interval, operation, directReceiver, listenerTag, flags, RECORD_ALARMS_IN_HISTORY, workSource, alarmClock, callingUid, callingPackage);
                return;
            }
        }
        Slog.w(TAG, "Alarms must either supply a PendingIntent or an AlarmReceiver");
    }

    private void setImplLocked(int type, long when, long whenElapsed, long windowLength, long maxWhen, long interval, PendingIntent operation, IAlarmListener directReceiver, String listenerTag, int flags, boolean doValidate, WorkSource workSource, AlarmClockInfo alarmClock, int callingUid, String callingPackage) {
        Alarm a = new Alarm(type, when, whenElapsed, windowLength, maxWhen, interval, operation, directReceiver, listenerTag, workSource, flags, alarmClock, callingUid, callingPackage);
        try {
            if (ActivityManagerNative.getDefault().getAppStartMode(callingUid, callingPackage) == RTC_MASK) {
                Slog.w(TAG, "Not setting alarm from " + callingUid + ":" + a + " -- package not allowed to start");
                return;
            }
        } catch (RemoteException e) {
        }
        if (!this.mCancelRemoveAction) {
            removeLocked(operation, directReceiver);
        }
        setImplLocked(a, RECORD_DEVICE_IDLE_ALARMS, doValidate);
    }

    private void setImplLocked(Alarm a, boolean rebatching, boolean doValidate) {
        if ((a.flags & 16) != 0) {
            long j;
            if (this.mNextWakeFromIdle != null && a.whenElapsed > this.mNextWakeFromIdle.whenElapsed) {
                j = this.mNextWakeFromIdle.whenElapsed;
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
                j = a.whenElapsed;
                a.maxWhenElapsed = j;
                a.when = j;
            }
        } else if (this.mPendingIdleUntil != null && (a.flags & 14) == 0) {
            this.mPendingWhileIdleAlarms.add(a);
            return;
        }
        adjustAlarmLocked(a);
        int whichBatch = (a.flags & TYPE_NONWAKEUP_MASK) != 0 ? -1 : attemptCoalesceLocked(a.whenElapsed, a.maxWhenElapsed);
        if (whichBatch < 0) {
            addBatchLocked(this.mAlarmBatches, new Batch(a));
        } else {
            Batch batch = (Batch) this.mAlarmBatches.get(whichBatch);
            if (batch.add(a)) {
                this.mAlarmBatches.remove(whichBatch);
                addBatchLocked(this.mAlarmBatches, batch);
            }
        }
        if (a.alarmClock != null) {
            this.mNextAlarmClockMayChange = RECORD_ALARMS_IN_HISTORY;
        }
        boolean needRebatch = RECORD_DEVICE_IDLE_ALARMS;
        if ((a.flags & 16) != 0) {
            this.mPendingIdleUntil = a;
            this.mConstants.updateAllowWhileIdleMinTimeLocked();
            needRebatch = RECORD_ALARMS_IN_HISTORY;
        } else if ((a.flags & RTC_MASK) != 0 && (this.mNextWakeFromIdle == null || this.mNextWakeFromIdle.whenElapsed > a.whenElapsed)) {
            this.mNextWakeFromIdle = a;
            if (this.mPendingIdleUntil != null) {
                needRebatch = RECORD_ALARMS_IN_HISTORY;
            }
        }
        if (!rebatching) {
            if (needRebatch) {
                rebatchAllAlarmsLocked(RECORD_DEVICE_IDLE_ALARMS);
            }
            hwSetRtcAlarm(a);
            rescheduleKernelAlarmsLocked();
            updateNextAlarmClockLocked();
        }
    }

    private int getWakeUpNumImpl(int uid, String pkg) {
        int i;
        synchronized (this.mLock) {
            ArrayMap<String, BroadcastStats> uidStats = (ArrayMap) this.mBroadcastStats.get(uid);
            if (uidStats == null) {
                uidStats = new ArrayMap();
                this.mBroadcastStats.put(uid, uidStats);
            }
            BroadcastStats bs = (BroadcastStats) uidStats.get(pkg);
            if (bs == null) {
                bs = new BroadcastStats(uid, pkg);
                uidStats.put(pkg, bs);
            }
            i = bs.numWakeup;
        }
        return i;
    }

    void dumpImpl(PrintWriter pw) {
        synchronized (this.mLock) {
            int i;
            int iu;
            int is;
            pw.println("Current Alarm Manager state:");
            this.mConstants.dump(pw);
            pw.println();
            long nowRTC = System.currentTimeMillis();
            long nowELAPSED = SystemClock.elapsedRealtime();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            pw.print("  nowRTC=");
            pw.print(nowRTC);
            pw.print("=");
            pw.print(sdf.format(new Date(nowRTC)));
            pw.print(" nowELAPSED=");
            pw.print(nowELAPSED);
            pw.println();
            pw.print("  mLastTimeChangeClockTime=");
            pw.print(this.mLastTimeChangeClockTime);
            pw.print("=");
            pw.println(sdf.format(new Date(this.mLastTimeChangeClockTime)));
            pw.print("  mLastTimeChangeRealtime=");
            TimeUtils.formatDuration(this.mLastTimeChangeRealtime, pw);
            pw.println();
            if (!this.mInteractive) {
                pw.print("  Time since non-interactive: ");
                TimeUtils.formatDuration(nowELAPSED - this.mNonInteractiveStartTime, pw);
                pw.println();
                pw.print("  Max wakeup delay: ");
                TimeUtils.formatDuration(currentNonWakeupFuzzLocked(nowELAPSED), pw);
                pw.println();
                pw.print("  Time since last dispatch: ");
                TimeUtils.formatDuration(nowELAPSED - this.mLastAlarmDeliveryTime, pw);
                pw.println();
                pw.print("  Next non-wakeup delivery time: ");
                TimeUtils.formatDuration(nowELAPSED - this.mNextNonWakeupDeliveryTime, pw);
                pw.println();
            }
            long nextWakeupRTC = this.mNextWakeup + (nowRTC - nowELAPSED);
            long nextNonWakeupRTC = this.mNextNonWakeup + (nowRTC - nowELAPSED);
            pw.print("  Next non-wakeup alarm: ");
            TimeUtils.formatDuration(this.mNextNonWakeup, nowELAPSED, pw);
            pw.print(" = ");
            pw.println(sdf.format(new Date(nextNonWakeupRTC)));
            pw.print("  Next wakeup: ");
            TimeUtils.formatDuration(this.mNextWakeup, nowELAPSED, pw);
            pw.print(" = ");
            pw.println(sdf.format(new Date(nextWakeupRTC)));
            pw.print("  Last wakeup: ");
            TimeUtils.formatDuration(this.mLastWakeup, nowELAPSED, pw);
            pw.print(" set at ");
            TimeUtils.formatDuration(this.mLastWakeupSet, nowELAPSED, pw);
            pw.println();
            pw.print("  Num time change events: ");
            pw.println(this.mNumTimeChanged);
            pw.println("  mDeviceIdleUserWhitelist=" + Arrays.toString(this.mDeviceIdleUserWhitelist));
            pw.println();
            pw.println("  Next alarm clock information: ");
            TreeSet<Integer> users = new TreeSet();
            for (i = PRIO_TICK; i < this.mNextAlarmClockForUser.size(); i += TYPE_NONWAKEUP_MASK) {
                users.add(Integer.valueOf(this.mNextAlarmClockForUser.keyAt(i)));
            }
            for (i = PRIO_TICK; i < this.mPendingSendNextAlarmClockChangedForUser.size(); i += TYPE_NONWAKEUP_MASK) {
                users.add(Integer.valueOf(this.mPendingSendNextAlarmClockChangedForUser.keyAt(i)));
            }
            for (Integer intValue : users) {
                int user = intValue.intValue();
                AlarmClockInfo next = (AlarmClockInfo) this.mNextAlarmClockForUser.get(user);
                long time = next != null ? next.getTriggerTime() : 0;
                boolean pendingSend = this.mPendingSendNextAlarmClockChangedForUser.get(user);
                pw.print("    user:");
                pw.print(user);
                pw.print(" pendingSend:");
                pw.print(pendingSend);
                pw.print(" time:");
                pw.print(time);
                if (time > 0) {
                    pw.print(" = ");
                    pw.print(sdf.format(new Date(time)));
                    pw.print(" = ");
                    TimeUtils.formatDuration(time, nowRTC, pw);
                }
                pw.println();
            }
            if (this.mAlarmBatches.size() > 0) {
                pw.println();
                pw.print("  Pending alarm batches: ");
                pw.println(this.mAlarmBatches.size());
                for (Batch b : this.mAlarmBatches) {
                    pw.print(b);
                    pw.println(':');
                    dumpAlarmList(pw, b.alarms, "    ", nowELAPSED, nowRTC, sdf);
                }
            }
            if (this.mPendingIdleUntil != null || this.mPendingWhileIdleAlarms.size() > 0) {
                pw.println();
                pw.println("    Idle mode state:");
                pw.print("      Idling until: ");
                if (this.mPendingIdleUntil != null) {
                    pw.println(this.mPendingIdleUntil);
                    this.mPendingIdleUntil.dump(pw, "        ", nowRTC, nowELAPSED, sdf);
                } else {
                    pw.println("null");
                }
                pw.println("      Pending alarms:");
                dumpAlarmList(pw, this.mPendingWhileIdleAlarms, "      ", nowELAPSED, nowRTC, sdf);
            }
            if (this.mNextWakeFromIdle != null) {
                pw.println();
                pw.print("  Next wake from idle: ");
                pw.println(this.mNextWakeFromIdle);
                this.mNextWakeFromIdle.dump(pw, "    ", nowRTC, nowELAPSED, sdf);
            }
            pw.println();
            pw.print("  Past-due non-wakeup alarms: ");
            if (this.mPendingNonWakeupAlarms.size() > 0) {
                pw.println(this.mPendingNonWakeupAlarms.size());
                dumpAlarmList(pw, this.mPendingNonWakeupAlarms, "    ", nowELAPSED, nowRTC, sdf);
            } else {
                pw.println("(none)");
            }
            pw.print("    Number of delayed alarms: ");
            pw.print(this.mNumDelayedAlarms);
            pw.print(", total delay time: ");
            TimeUtils.formatDuration(this.mTotalDelayTime, pw);
            pw.println();
            pw.print("    Max delay time: ");
            TimeUtils.formatDuration(this.mMaxDelayTime, pw);
            pw.print(", max non-interactive time: ");
            TimeUtils.formatDuration(this.mNonInteractiveTime, pw);
            pw.println();
            pw.println();
            pw.print("  Broadcast ref count: ");
            pw.println(this.mBroadcastRefCount);
            pw.println();
            if (this.mInFlight.size() > 0) {
                pw.println("Outstanding deliveries:");
                for (i = PRIO_TICK; i < this.mInFlight.size(); i += TYPE_NONWAKEUP_MASK) {
                    if (this.mInFlight.get(i) != null) {
                        pw.print("   #");
                        pw.print(i);
                        pw.print(": ");
                        pw.print(this.mInFlight.get(i));
                        pw.print("##" + ((InFlight) this.mInFlight.get(i)).mUid);
                        pw.print("##" + ((InFlight) this.mInFlight.get(i)).mTag);
                        pw.print("\n");
                    }
                }
                pw.println();
            }
            pw.print("  mAllowWhileIdleMinTime=");
            TimeUtils.formatDuration(this.mAllowWhileIdleMinTime, pw);
            pw.println();
            if (this.mLastAllowWhileIdleDispatch.size() > 0) {
                pw.println("  Last allow while idle dispatch times:");
                for (i = PRIO_TICK; i < this.mLastAllowWhileIdleDispatch.size(); i += TYPE_NONWAKEUP_MASK) {
                    pw.print("  UID ");
                    UserHandle.formatUid(pw, this.mLastAllowWhileIdleDispatch.keyAt(i));
                    pw.print(": ");
                    TimeUtils.formatDuration(this.mLastAllowWhileIdleDispatch.valueAt(i), nowELAPSED, pw);
                    pw.println();
                }
            }
            pw.println();
            if (this.mLog.dump(pw, "  Recent problems", "    ")) {
                pw.println();
            }
            Object topFilters = new FilterStats[10];
            Comparator<FilterStats> anonymousClass3 = new Comparator<FilterStats>() {
                public int compare(FilterStats lhs, FilterStats rhs) {
                    if (lhs.aggregateTime < rhs.aggregateTime) {
                        return AlarmManagerService.TYPE_NONWAKEUP_MASK;
                    }
                    if (lhs.aggregateTime > rhs.aggregateTime) {
                        return -1;
                    }
                    return AlarmManagerService.PRIO_TICK;
                }
            };
            int len = PRIO_TICK;
            for (iu = PRIO_TICK; iu < this.mBroadcastStats.size(); iu += TYPE_NONWAKEUP_MASK) {
                int ip;
                ArrayMap<String, BroadcastStats> uidStats = (ArrayMap) this.mBroadcastStats.valueAt(iu);
                for (ip = PRIO_TICK; ip < uidStats.size(); ip += TYPE_NONWAKEUP_MASK) {
                    BroadcastStats bs = (BroadcastStats) uidStats.valueAt(ip);
                    for (is = PRIO_TICK; is < bs.filterStats.size(); is += TYPE_NONWAKEUP_MASK) {
                        FilterStats fs = (FilterStats) bs.filterStats.valueAt(is);
                        int pos = len > 0 ? Arrays.binarySearch(topFilters, PRIO_TICK, len, fs, anonymousClass3) : PRIO_TICK;
                        if (pos < 0) {
                            pos = (-pos) - 1;
                        }
                        if (pos < topFilters.length) {
                            int copylen = (topFilters.length - pos) - 1;
                            if (copylen > 0) {
                                System.arraycopy(topFilters, pos, topFilters, pos + TYPE_NONWAKEUP_MASK, copylen);
                            }
                            topFilters[pos] = fs;
                            if (len < topFilters.length) {
                                len += TYPE_NONWAKEUP_MASK;
                            }
                        }
                    }
                }
            }
            if (len > 0) {
                pw.println("  Top Alarms:");
                for (i = PRIO_TICK; i < len; i += TYPE_NONWAKEUP_MASK) {
                    fs = topFilters[i];
                    pw.print("    ");
                    if (fs.nesting > 0) {
                        pw.print("*ACTIVE* ");
                    }
                    TimeUtils.formatDuration(fs.aggregateTime, pw);
                    pw.print(" running, ");
                    pw.print(fs.numWakeup);
                    pw.print(" wakeups, ");
                    pw.print(fs.count);
                    pw.print(" alarms: ");
                    UserHandle.formatUid(pw, fs.mBroadcastStats.mUid);
                    pw.print(":");
                    pw.print(fs.mBroadcastStats.mPackageName);
                    pw.println();
                    pw.print("      ");
                    pw.print(fs.mTag);
                    pw.println();
                }
            }
            pw.println(" ");
            pw.println("  Alarm Stats:");
            ArrayList<FilterStats> tmpFilters = new ArrayList();
            for (iu = PRIO_TICK; iu < this.mBroadcastStats.size(); iu += TYPE_NONWAKEUP_MASK) {
                uidStats = (ArrayMap) this.mBroadcastStats.valueAt(iu);
                for (ip = PRIO_TICK; ip < uidStats.size(); ip += TYPE_NONWAKEUP_MASK) {
                    bs = (BroadcastStats) uidStats.valueAt(ip);
                    pw.print("  ");
                    if (bs.nesting > 0) {
                        pw.print("*ACTIVE* ");
                    }
                    UserHandle.formatUid(pw, bs.mUid);
                    pw.print(":");
                    pw.print(bs.mPackageName);
                    pw.print(" ");
                    TimeUtils.formatDuration(bs.aggregateTime, pw);
                    pw.print(" running, ");
                    pw.print(bs.numWakeup);
                    pw.println(" wakeups:");
                    tmpFilters.clear();
                    for (is = PRIO_TICK; is < bs.filterStats.size(); is += TYPE_NONWAKEUP_MASK) {
                        tmpFilters.add((FilterStats) bs.filterStats.valueAt(is));
                    }
                    Collections.sort(tmpFilters, anonymousClass3);
                    for (i = PRIO_TICK; i < tmpFilters.size(); i += TYPE_NONWAKEUP_MASK) {
                        fs = (FilterStats) tmpFilters.get(i);
                        pw.print("    ");
                        if (fs.nesting > 0) {
                            pw.print("*ACTIVE* ");
                        }
                        TimeUtils.formatDuration(fs.aggregateTime, pw);
                        pw.print(" ");
                        pw.print(fs.numWakeup);
                        pw.print(" wakes ");
                        pw.print(fs.count);
                        pw.print(" alarms, last ");
                        TimeUtils.formatDuration(fs.lastTime, nowELAPSED, pw);
                        pw.println(":");
                        pw.print("      ");
                        pw.print(fs.mTag);
                        pw.println();
                    }
                }
            }
            printHwWakeupBoot(pw);
        }
    }

    private void logBatchesLocked(SimpleDateFormat sdf) {
        ByteArrayOutputStream bs = new ByteArrayOutputStream(DumpState.DUMP_VERIFIERS);
        PrintWriter pw = new PrintWriter(bs);
        long nowRTC = System.currentTimeMillis();
        long nowELAPSED = SystemClock.elapsedRealtime();
        int NZ = this.mAlarmBatches.size();
        for (int iz = PRIO_TICK; iz < NZ; iz += TYPE_NONWAKEUP_MASK) {
            Batch bz = (Batch) this.mAlarmBatches.get(iz);
            pw.append("Batch ");
            pw.print(iz);
            pw.append(": ");
            pw.println(bz);
            dumpAlarmList(pw, bz.alarms, "  ", nowELAPSED, nowRTC, sdf);
            pw.flush();
            Slog.v(TAG, bs.toString());
            bs.reset();
        }
    }

    private boolean validateConsistencyLocked() {
        return RECORD_ALARMS_IN_HISTORY;
    }

    private Batch findFirstWakeupBatchLocked() {
        int N = this.mAlarmBatches.size();
        int i = PRIO_TICK;
        while (i < N) {
            try {
                Batch b = (Batch) this.mAlarmBatches.get(i);
                if (b.hasWakeups()) {
                    return b;
                }
                i += TYPE_NONWAKEUP_MASK;
            } catch (IndexOutOfBoundsException e) {
                Log.d(TAG, "Do nothing");
            }
        }
        return null;
    }

    long getNextWakeFromIdleTimeImpl() {
        long j;
        synchronized (this.mLock) {
            j = this.mNextWakeFromIdle != null ? this.mNextWakeFromIdle.whenElapsed : JobStatus.NO_LATEST_RUNTIME;
        }
        return j;
    }

    void setDeviceIdleUserWhitelistImpl(int[] appids) {
        synchronized (this.mLock) {
            this.mDeviceIdleUserWhitelist = appids;
        }
    }

    AlarmClockInfo getNextAlarmClockImpl(int userId) {
        AlarmClockInfo alarmClockInfo;
        synchronized (this.mLock) {
            alarmClockInfo = (AlarmClockInfo) this.mNextAlarmClockForUser.get(userId);
        }
        return alarmClockInfo;
    }

    void updateNextAlarmClockLocked() {
        if (this.mNextAlarmClockMayChange) {
            int i;
            int userId;
            this.mNextAlarmClockMayChange = RECORD_DEVICE_IDLE_ALARMS;
            SparseArray<AlarmClockInfo> nextForUser = this.mTmpSparseAlarmClockArray;
            nextForUser.clear();
            int N = this.mAlarmBatches.size();
            for (i = PRIO_TICK; i < N; i += TYPE_NONWAKEUP_MASK) {
                ArrayList<Alarm> alarms = ((Batch) this.mAlarmBatches.get(i)).alarms;
                int M = alarms.size();
                for (int j = PRIO_TICK; j < M; j += TYPE_NONWAKEUP_MASK) {
                    Alarm a = (Alarm) alarms.get(j);
                    if (a.alarmClock != null) {
                        userId = UserHandle.getUserId(a.uid);
                        AlarmClockInfo current = (AlarmClockInfo) this.mNextAlarmClockForUser.get(userId);
                        if (nextForUser.get(userId) == null) {
                            nextForUser.put(userId, a.alarmClock);
                        } else if (a.alarmClock.equals(current) && current.getTriggerTime() <= ((AlarmClockInfo) nextForUser.get(userId)).getTriggerTime()) {
                            nextForUser.put(userId, current);
                        }
                    }
                }
            }
            int NN = nextForUser.size();
            for (i = PRIO_TICK; i < NN; i += TYPE_NONWAKEUP_MASK) {
                AlarmClockInfo newAlarm = (AlarmClockInfo) nextForUser.valueAt(i);
                userId = nextForUser.keyAt(i);
                if (!newAlarm.equals((AlarmClockInfo) this.mNextAlarmClockForUser.get(userId))) {
                    updateNextAlarmInfoForUserLocked(userId, newAlarm);
                }
            }
            for (i = this.mNextAlarmClockForUser.size() - 1; i >= 0; i--) {
                userId = this.mNextAlarmClockForUser.keyAt(i);
                if (nextForUser.get(userId) == null) {
                    updateNextAlarmInfoForUserLocked(userId, null);
                }
            }
        }
    }

    private void updateNextAlarmInfoForUserLocked(int userId, AlarmClockInfo alarmClock) {
        if (alarmClock != null) {
            this.mNextAlarmClockForUser.put(userId, alarmClock);
        } else {
            this.mNextAlarmClockForUser.remove(userId);
        }
        this.mPendingSendNextAlarmClockChangedForUser.put(userId, RECORD_ALARMS_IN_HISTORY);
        this.mHandler.removeMessages(RTC_MASK);
        this.mHandler.sendEmptyMessage(RTC_MASK);
    }

    private void sendNextAlarmClockChanged() {
        int N;
        int i;
        SparseArray<AlarmClockInfo> pendingUsers = this.mHandlerSparseAlarmClockArray;
        pendingUsers.clear();
        synchronized (this.mLock) {
            N = this.mPendingSendNextAlarmClockChangedForUser.size();
            for (i = PRIO_TICK; i < N; i += TYPE_NONWAKEUP_MASK) {
                int userId = this.mPendingSendNextAlarmClockChangedForUser.keyAt(i);
                pendingUsers.append(userId, (AlarmClockInfo) this.mNextAlarmClockForUser.get(userId));
            }
            this.mPendingSendNextAlarmClockChangedForUser.clear();
        }
        N = pendingUsers.size();
        for (i = PRIO_TICK; i < N; i += TYPE_NONWAKEUP_MASK) {
            userId = pendingUsers.keyAt(i);
            System.putStringForUser(getContext().getContentResolver(), "next_alarm_formatted", formatNextAlarm(getContext(), (AlarmClockInfo) pendingUsers.valueAt(i), userId), userId);
            getContext().sendBroadcastAsUser(NEXT_ALARM_CLOCK_CHANGED_INTENT, new UserHandle(userId));
        }
    }

    private static String formatNextAlarm(Context context, AlarmClockInfo info, int userId) {
        String pattern = DateFormat.getBestDateTimePattern(Locale.getDefault(), DateFormat.is24HourFormat(context, userId) ? "EHm" : "Ehma");
        if (info == null) {
            return "";
        }
        return DateFormat.format(pattern, info.getTriggerTime()).toString();
    }

    void rescheduleKernelAlarmsLocked() {
        long nextNonWakeup = 0;
        if (this.mAlarmBatches.size() > 0) {
            Batch firstWakeup = findFirstWakeupBatchLocked();
            Batch firstBatch = (Batch) this.mAlarmBatches.get(PRIO_TICK);
            if (!(firstWakeup == null || this.mNextWakeup == firstWakeup.start)) {
                this.mNextWakeup = firstWakeup.start;
                this.mLastWakeupSet = SystemClock.elapsedRealtime();
                setLocked(RTC_MASK, firstWakeup.start);
            }
            if (firstBatch != firstWakeup) {
                nextNonWakeup = firstBatch.start;
            }
        }
        if (this.mPendingNonWakeupAlarms.size() > 0 && (nextNonWakeup == 0 || this.mNextNonWakeupDeliveryTime < nextNonWakeup)) {
            nextNonWakeup = this.mNextNonWakeupDeliveryTime;
        }
        if (nextNonWakeup != 0 && this.mNextNonWakeup != nextNonWakeup) {
            this.mNextNonWakeup = nextNonWakeup;
            setLocked(3, nextNonWakeup);
        }
    }

    void removeLocked(PendingIntent operation, IAlarmListener directReceiver) {
        int i;
        int didRemove = PRIO_TICK;
        for (i = this.mAlarmBatches.size() - 1; i >= 0; i--) {
            Batch b = (Batch) this.mAlarmBatches.get(i);
            if (b != null) {
                didRemove |= b.remove(operation, directReceiver);
                if (b.size() == 0) {
                    this.mAlarmBatches.remove(i);
                }
            }
        }
        for (i = this.mPendingWhileIdleAlarms.size() - 1; i >= 0; i--) {
            if (((Alarm) this.mPendingWhileIdleAlarms.get(i)).matches(operation, directReceiver)) {
                this.mPendingWhileIdleAlarms.remove(i);
            }
        }
        hwRemoveAnywayRtcAlarm(operation);
        if (didRemove != 0) {
            Flog.i(SystemService.PHASE_SYSTEM_SERVICES_READY, "remove(" + operation + ") changed bounds; rebatching");
            boolean restorePending = RECORD_DEVICE_IDLE_ALARMS;
            if (this.mPendingIdleUntil != null && this.mPendingIdleUntil.matches(operation, directReceiver)) {
                this.mPendingIdleUntil = null;
                restorePending = RECORD_ALARMS_IN_HISTORY;
            }
            if (this.mNextWakeFromIdle != null && this.mNextWakeFromIdle.matches(operation, directReceiver)) {
                this.mNextWakeFromIdle = null;
            }
            rebatchAllAlarmsLocked(RECORD_ALARMS_IN_HISTORY);
            if (restorePending) {
                restorePendingWhileIdleAlarmsLocked();
            }
            updateNextAlarmClockLocked();
        }
    }

    public void cleanupAlarmLocked(ArrayList<String> array) {
        if (array != null && array.size() != 0) {
            Message msg = Message.obtain();
            msg.what = 6;
            Bundle bundle = new Bundle();
            bundle.putStringArrayList("clearlist", array);
            msg.setData(bundle);
            this.mHandler.sendMessage(msg);
        }
    }

    void removeLocked(String packageName) {
        removeLocked(packageName, null);
    }

    void removeLocked(String packageName, String action) {
        int i;
        int didRemove = PRIO_TICK;
        for (i = this.mAlarmBatches.size() - 1; i >= 0; i--) {
            Batch b = (Batch) this.mAlarmBatches.get(i);
            if (action == null) {
                didRemove |= b.remove(packageName);
            } else {
                didRemove |= b.remove(packageName, action);
            }
            if (b.size() == 0) {
                this.mAlarmBatches.remove(i);
            }
        }
        for (i = this.mPendingWhileIdleAlarms.size() - 1; i >= 0; i--) {
            if (((Alarm) this.mPendingWhileIdleAlarms.get(i)).matches(packageName)) {
                this.mPendingWhileIdleAlarms.remove(i);
            }
        }
        if (didRemove != 0) {
            rebatchAllAlarmsLocked(RECORD_ALARMS_IN_HISTORY);
            rescheduleKernelAlarmsLocked();
            updateNextAlarmClockLocked();
        }
    }

    void removeForStoppedLocked(int uid) {
        int i;
        int didRemove = PRIO_TICK;
        for (i = this.mAlarmBatches.size() - 1; i >= 0; i--) {
            Batch b = (Batch) this.mAlarmBatches.get(i);
            didRemove |= b.removeForStopped(uid);
            if (b.size() == 0) {
                this.mAlarmBatches.remove(i);
            }
        }
        for (i = this.mPendingWhileIdleAlarms.size() - 1; i >= 0; i--) {
            Alarm a = (Alarm) this.mPendingWhileIdleAlarms.get(i);
            try {
                if (a.uid == uid && ActivityManagerNative.getDefault().getAppStartMode(uid, a.packageName) == RTC_MASK) {
                    this.mPendingWhileIdleAlarms.remove(i);
                }
            } catch (RemoteException e) {
            }
        }
        if (didRemove != 0) {
            rebatchAllAlarmsLocked(RECORD_ALARMS_IN_HISTORY);
            rescheduleKernelAlarmsLocked();
            updateNextAlarmClockLocked();
        }
    }

    void removeUserLocked(int userHandle) {
        int i;
        int didRemove = PRIO_TICK;
        for (i = this.mAlarmBatches.size() - 1; i >= 0; i--) {
            Batch b = (Batch) this.mAlarmBatches.get(i);
            didRemove |= b.remove(userHandle);
            if (b.size() == 0) {
                this.mAlarmBatches.remove(i);
            }
        }
        for (i = this.mPendingWhileIdleAlarms.size() - 1; i >= 0; i--) {
            if (UserHandle.getUserId(((Alarm) this.mPendingWhileIdleAlarms.get(i)).creatorUid) == userHandle) {
                this.mPendingWhileIdleAlarms.remove(i);
            }
        }
        for (i = this.mLastAllowWhileIdleDispatch.size() - 1; i >= 0; i--) {
            if (UserHandle.getUserId(this.mLastAllowWhileIdleDispatch.keyAt(i)) == userHandle) {
                this.mLastAllowWhileIdleDispatch.removeAt(i);
            }
        }
        if (didRemove != 0) {
            rebatchAllAlarmsLocked(RECORD_ALARMS_IN_HISTORY);
            rescheduleKernelAlarmsLocked();
            updateNextAlarmClockLocked();
        }
    }

    void interactiveStateChangedLocked(boolean interactive) {
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

    boolean lookForPackageLocked(String packageName) {
        int i;
        for (i = PRIO_TICK; i < this.mAlarmBatches.size(); i += TYPE_NONWAKEUP_MASK) {
            if (((Batch) this.mAlarmBatches.get(i)).hasPackage(packageName)) {
                return RECORD_ALARMS_IN_HISTORY;
            }
        }
        for (i = PRIO_TICK; i < this.mPendingWhileIdleAlarms.size(); i += TYPE_NONWAKEUP_MASK) {
            if (((Alarm) this.mPendingWhileIdleAlarms.get(i)).matches(packageName)) {
                return RECORD_ALARMS_IN_HISTORY;
            }
        }
        return RECORD_DEVICE_IDLE_ALARMS;
    }

    private void setLocked(int type, long when) {
        if (this.mNativeData != 0) {
            long alarmSeconds;
            long alarmNanoseconds;
            if (when < 0) {
                alarmSeconds = 0;
                alarmNanoseconds = 0;
            } else {
                alarmSeconds = when / 1000;
                alarmNanoseconds = ((when % 1000) * 1000) * 1000;
            }
            set(this.mNativeData, type, alarmSeconds, alarmNanoseconds);
            return;
        }
        Message msg = Message.obtain();
        msg.what = TYPE_NONWAKEUP_MASK;
        this.mHandler.removeMessages(TYPE_NONWAKEUP_MASK);
        this.mHandler.sendMessageAtTime(msg, when);
    }

    private static final void dumpAlarmList(PrintWriter pw, ArrayList<Alarm> list, String prefix, String label, long nowRTC, long nowELAPSED, SimpleDateFormat sdf) {
        for (int i = list.size() - 1; i >= 0; i--) {
            Alarm a = (Alarm) list.get(i);
            pw.print(prefix);
            pw.print(label);
            pw.print(" #");
            pw.print(i);
            pw.print(": ");
            pw.println(a);
            a.dump(pw, prefix + "  ", nowRTC, nowELAPSED, sdf);
        }
    }

    private static final String labelForType(int type) {
        switch (type) {
            case PRIO_TICK /*0*/:
                return "RTC_WAKEUP";
            case TYPE_NONWAKEUP_MASK /*1*/:
                return "RTC";
            case RTC_MASK /*2*/:
                return "ELAPSED_WAKEUP";
            case H.REPORT_LOSING_FOCUS /*3*/:
                return "ELAPSED";
            default:
                return "--unknown--";
        }
    }

    private static final void dumpAlarmList(PrintWriter pw, ArrayList<Alarm> list, String prefix, long nowELAPSED, long nowRTC, SimpleDateFormat sdf) {
        for (int i = list.size() - 1; i >= 0; i--) {
            Alarm a = (Alarm) list.get(i);
            String label = labelForType(a.type);
            pw.print(prefix);
            pw.print(label);
            pw.print(" #");
            pw.print(i);
            pw.print(": ");
            pw.println(a);
            a.dump(pw, prefix + "  ", nowRTC, nowELAPSED, sdf);
        }
    }

    boolean triggerAlarmsLocked(ArrayList<Alarm> triggerList, long nowELAPSED, long nowRTC) {
        boolean hasWakeup = RECORD_DEVICE_IDLE_ALARMS;
        while (this.mAlarmBatches.size() > 0) {
            Batch batch = (Batch) this.mAlarmBatches.get(PRIO_TICK);
            if (batch.start > nowELAPSED) {
                break;
            }
            int i;
            this.mAlarmBatches.remove(PRIO_TICK);
            int N = batch.size();
            for (i = PRIO_TICK; i < N; i += TYPE_NONWAKEUP_MASK) {
                Alarm alarm = batch.get(i);
                if ((alarm.flags & ELAPSED_REALTIME_WAKEUP_MASK) != 0) {
                    long minTime = this.mLastAllowWhileIdleDispatch.get(alarm.uid, 0) + this.mAllowWhileIdleMinTime;
                    if (nowELAPSED < minTime) {
                        alarm.whenElapsed = minTime;
                        if (alarm.maxWhenElapsed < minTime) {
                            alarm.maxWhenElapsed = minTime;
                        }
                        setImplLocked(alarm, RECORD_ALARMS_IN_HISTORY, RECORD_DEVICE_IDLE_ALARMS);
                    }
                }
                alarm.count = TYPE_NONWAKEUP_MASK;
                triggerList.add(alarm);
                if ((alarm.flags & RTC_MASK) != 0) {
                    EventLogTags.writeDeviceIdleWakeFromIdle(this.mPendingIdleUntil != null ? TYPE_NONWAKEUP_MASK : PRIO_TICK, alarm.statsTag);
                }
                if (this.mPendingIdleUntil == alarm) {
                    this.mPendingIdleUntil = null;
                    rebatchAllAlarmsLocked(RECORD_DEVICE_IDLE_ALARMS);
                    restorePendingWhileIdleAlarmsLocked();
                }
                if (this.mNextWakeFromIdle == alarm) {
                    this.mNextWakeFromIdle = null;
                    rebatchAllAlarmsLocked(RECORD_DEVICE_IDLE_ALARMS);
                }
                if (alarm.repeatInterval > 0) {
                    alarm.count = (int) (((long) alarm.count) + ((nowELAPSED - alarm.whenElapsed) / alarm.repeatInterval));
                    long delta = ((long) alarm.count) * alarm.repeatInterval;
                    long nextElapsed = alarm.whenElapsed + delta;
                    if (alarm.type == 0 || RTC_MASK == alarm.type) {
                        Flog.i(SystemService.PHASE_SYSTEM_SERVICES_READY, "set again repeat alarm: next ,nextElapsed = " + nextElapsed + ",repeatInterval = " + alarm.repeatInterval);
                    }
                    int i2 = alarm.type;
                    long j = nextElapsed;
                    setImplLocked(i2, alarm.when + delta, j, alarm.windowLength, maxTriggerTime(nowELAPSED, nextElapsed, alarm.repeatInterval), alarm.repeatInterval, alarm.operation, null, null, alarm.flags, RECORD_ALARMS_IN_HISTORY, alarm.workSource, alarm.alarmClock, alarm.uid, alarm.packageName);
                }
                if (alarm.wakeup) {
                    hasWakeup = RECORD_ALARMS_IN_HISTORY;
                }
                if (alarm.alarmClock != null) {
                    this.mNextAlarmClockMayChange = RECORD_ALARMS_IN_HISTORY;
                }
            }
        }
        this.mCurrentSeq += TYPE_NONWAKEUP_MASK;
        calculateDeliveryPriorities(triggerList);
        Collections.sort(triggerList, this.mAlarmDispatchComparator);
        ArrayList<Alarm> wakeupAlarms = new ArrayList();
        for (i = PRIO_TICK; i < triggerList.size(); i += TYPE_NONWAKEUP_MASK) {
            Alarm talarm = (Alarm) triggerList.get(i);
            if (talarm.type == 0 || RTC_MASK == talarm.type) {
                wakeupAlarms.add(talarm);
                if (talarm.operation == null) {
                    Log.w(TAG, "mIsScreenOn is: " + this.mIsScreenOn + ", WAKEUP alarm talarm.operation == null,package name is: " + talarm.packageName + " listenerTag is: " + talarm.listenerTag + " creator uid is: " + talarm.creatorUid);
                } else if (resetIntentCallingIdentity(talarm.operation) != null) {
                    Log.w(TAG, "mIsScreenOn is: " + this.mIsScreenOn + ", WAKEUP alarm trigger action = " + resetActionCallingIdentity(talarm.operation) + " package name is: " + talarm.packageName);
                } else {
                    Log.w(TAG, "mIsScreenOn is: " + this.mIsScreenOn + ", WAKEUP alarm intent == null and  package name is: " + talarm.packageName + " listenerTag is: " + talarm.listenerTag + " createor uid is: " + talarm.creatorUid);
                }
            }
        }
        if (wakeupAlarms.size() > 0) {
            Flog.i(SystemService.PHASE_SYSTEM_SERVICES_READY, "Alarm triggering (type 0 or 2): " + wakeupAlarms);
        }
        return hasWakeup;
    }

    void recordWakeupAlarms(ArrayList<Batch> batches, long nowELAPSED, long nowRTC) {
        int numBatches = batches.size();
        int nextBatch = PRIO_TICK;
        while (nextBatch < numBatches) {
            Batch b = (Batch) batches.get(nextBatch);
            if (b.start <= nowELAPSED) {
                int numAlarms = b.alarms.size();
                for (int nextAlarm = PRIO_TICK; nextAlarm < numAlarms; nextAlarm += TYPE_NONWAKEUP_MASK) {
                    this.mRecentWakeups.add(((Alarm) b.alarms.get(nextAlarm)).makeWakeupEvent(nowRTC));
                }
                nextBatch += TYPE_NONWAKEUP_MASK;
            } else {
                return;
            }
        }
    }

    long currentNonWakeupFuzzLocked(long nowELAPSED) {
        long timeSinceOn = nowELAPSED - this.mNonInteractiveStartTime;
        if (timeSinceOn < 300000) {
            return JobStatus.DEFAULT_TRIGGER_MAX_DELAY;
        }
        if (timeSinceOn < HwBroadcastRadarUtil.SYSTEM_BOOT_COMPLETED_TIME) {
            return 900000;
        }
        return 3600000;
    }

    static int fuzzForDuration(long duration) {
        if (duration < 900000) {
            return (int) duration;
        }
        if (duration < 5400000) {
            return 900000;
        }
        return ProcessList.PSS_MAX_INTERVAL;
    }

    boolean checkAllowNonWakeupDelayLocked(long nowELAPSED) {
        boolean z = RECORD_DEVICE_IDLE_ALARMS;
        if (this.mInteractive || this.mLastAlarmDeliveryTime <= 0) {
            return RECORD_DEVICE_IDLE_ALARMS;
        }
        if (this.mPendingNonWakeupAlarms.size() > 0 && this.mNextNonWakeupDeliveryTime < nowELAPSED) {
            return RECORD_DEVICE_IDLE_ALARMS;
        }
        if (nowELAPSED - this.mLastAlarmDeliveryTime <= currentNonWakeupFuzzLocked(nowELAPSED)) {
            z = RECORD_ALARMS_IN_HISTORY;
        }
        return z;
    }

    void deliverAlarmsLocked(ArrayList<Alarm> triggerList, long nowELAPSED) {
        this.mLastAlarmDeliveryTime = nowELAPSED;
        for (int i = PRIO_TICK; i < triggerList.size(); i += TYPE_NONWAKEUP_MASK) {
            Alarm alarm = (Alarm) triggerList.get(i);
            if (alarm == null) {
                HwLog.bdate("BDAT_TAG_ALARM_TRIGGER", "uid=0 name=NULL type=-1 tag=NULL size=" + triggerList.size());
            } else {
                boolean allowWhileIdle = (alarm.flags & ELAPSED_REALTIME_WAKEUP_MASK) != 0 ? RECORD_ALARMS_IN_HISTORY : RECORD_DEVICE_IDLE_ALARMS;
                try {
                    String packageName;
                    Flog.i(SystemService.PHASE_SYSTEM_SERVICES_READY, "sending alarm " + alarm + ",repeatInterval = " + alarm.repeatInterval + ",listenerTag =" + alarm.listenerTag);
                    if (alarm.operation == null) {
                        packageName = alarm.packageName;
                    } else {
                        packageName = alarm.operation.getTargetPackage();
                    }
                    HwLog.bdate("BDAT_TAG_ALARM_TRIGGER", "uid=" + alarm.uid + " name=" + packageName + " type=" + alarm.type + " tag=" + alarm.statsTag + " size=" + triggerList.size());
                    if (alarm.workSource == null || alarm.workSource.size() <= 0) {
                        ActivityManagerNative.noteAlarmStart(alarm.operation, alarm.uid, alarm.statsTag);
                    } else {
                        for (int wi = PRIO_TICK; wi < alarm.workSource.size(); wi += TYPE_NONWAKEUP_MASK) {
                            ActivityManagerNative.noteAlarmStart(alarm.operation, alarm.workSource.get(wi), alarm.statsTag);
                        }
                    }
                    this.mDeliveryTracker.deliverLocked(alarm, nowELAPSED, allowWhileIdle);
                } catch (RuntimeException e) {
                    Slog.w(TAG, "Failure sending alarm.", e);
                }
            }
        }
    }

    void setWakelockWorkSource(PendingIntent pi, WorkSource ws, int type, String tag, int knownUid, boolean first) {
        try {
            boolean unimportant = pi == this.mTimeTickSender ? RECORD_ALARMS_IN_HISTORY : RECORD_DEVICE_IDLE_ALARMS;
            this.mWakeLock.setUnimportantForLogging(unimportant);
            if (first || this.mLastWakeLockUnimportantForLogging) {
                this.mWakeLock.setHistoryTag(tag);
            } else {
                this.mWakeLock.setHistoryTag(null);
            }
            this.mLastWakeLockUnimportantForLogging = unimportant;
            if (ws != null) {
                this.mWakeLock.setWorkSource(ws);
                return;
            }
            int uid;
            if (knownUid >= 0) {
                uid = knownUid;
            } else {
                uid = ActivityManagerNative.getDefault().getUidForIntentSender(pi.getTarget());
            }
            if (uid >= 0) {
                this.mWakeLock.setWorkSource(new WorkSource(uid));
                return;
            }
            this.mWakeLock.setWorkSource(null);
        } catch (Exception e) {
        }
    }

    private final BroadcastStats getStatsLocked(PendingIntent pi) {
        return getStatsLocked(pi.getCreatorUid(), pi.getCreatorPackage());
    }

    private final BroadcastStats getStatsLocked(int uid, String pkgName) {
        ArrayMap<String, BroadcastStats> uidStats = (ArrayMap) this.mBroadcastStats.get(uid);
        if (uidStats == null) {
            uidStats = new ArrayMap();
            this.mBroadcastStats.put(uid, uidStats);
        }
        BroadcastStats bs = (BroadcastStats) uidStats.get(pkgName);
        if (bs != null) {
            return bs;
        }
        bs = new BroadcastStats(uid, pkgName);
        uidStats.put(pkgName, bs);
        return bs;
    }

    public ArrayList<Batch> getmAlarmBatches() {
        return this.mAlarmBatches;
    }

    protected void removeDeskClockFromFWK(PendingIntent operation) {
    }

    private static String resetActionCallingIdentity(PendingIntent operation) {
        long identity = Binder.clearCallingIdentity();
        String action = null;
        try {
            action = operation.getIntent().getAction();
            Binder.restoreCallingIdentity(identity);
            return action;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
            return action;
        }
    }

    private static Intent resetIntentCallingIdentity(PendingIntent operation) {
        long identity = Binder.clearCallingIdentity();
        Intent intent = null;
        try {
            intent = operation.getIntent();
            Binder.restoreCallingIdentity(identity);
            return intent;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
            return intent;
        }
    }

    private void recordAlarmForPtm(Alarm alarm) {
        String trigAction = null;
        if (alarm != null) {
            String typeStr;
            if (alarm.type == 0 || RTC_MASK == alarm.type) {
                typeStr = "wk";
            } else {
                typeStr = "unwk";
            }
            String pkgName = alarm.packageName != null ? alarm.packageName : "null";
            if (alarm.operation == null) {
                trigAction = alarm.statsTag;
            } else if (resetIntentCallingIdentity(alarm.operation) != null) {
                trigAction = resetActionCallingIdentity(alarm.operation);
            }
            if (trigAction == null) {
                trigAction = "null";
            }
            StringBuilder builder = new StringBuilder(200);
            builder.append("event=10,");
            builder.append("type=").append(typeStr).append(",");
            builder.append("pkg=").append(pkgName).append(",");
            builder.append("trigAction=").append(trigAction);
            String alarmStr = builder.toString();
            builder.setLength(PRIO_TICK);
            PtmLog.w(10, alarmStr);
        }
    }
}
