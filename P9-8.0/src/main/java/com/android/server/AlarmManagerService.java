package com.android.server;

import android.app.ActivityManager;
import android.app.AlarmManager.AlarmClockInfo;
import android.app.AppOpsManager;
import android.app.BroadcastOptions;
import android.app.IAlarmCompleteListener;
import android.app.IAlarmListener;
import android.app.IAlarmManager.Stub;
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
import android.os.Environment;
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
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.util.SparseLongArray;
import android.util.TimeUtils;
import com.android.internal.util.DumpUtils;
import com.android.internal.util.LocalLog;
import com.android.server.am.HwActivityManagerServiceUtil;
import com.android.server.am.HwBroadcastRadarUtil;
import com.android.server.am.ProcessList;
import com.android.server.connectivity.LingerMonitor;
import com.android.server.job.controllers.JobStatus;
import com.android.server.usage.UnixCalendar;
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
    static final boolean DEBUG_TIMEZONE;
    static final boolean DEBUG_VALIDATE = false;
    static final boolean DEBUG_WAKELOCK = false;
    private static final int ELAPSED_REALTIME_MASK = 8;
    private static final int ELAPSED_REALTIME_WAKEUP_MASK = 4;
    static final int IS_WAKEUP_MASK = 5;
    static final long MIN_FUZZABLE_INTERVAL = 10000;
    private static final Intent NEXT_ALARM_CLOCK_CHANGED_INTENT = new Intent("android.app.action.NEXT_ALARM_CLOCK_CHANGED").addFlags(553648128);
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
    static final BatchTimeOrder sBatchOrder = new BatchTimeOrder();
    static final IncreasingTimeOrder sIncreasingTimeOrder = new IncreasingTimeOrder();
    final long RECENT_WAKEUP_PERIOD = UnixCalendar.DAY_IN_MILLIS;
    final ArrayList<Batch> mAlarmBatches = new ArrayList();
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
    private HwSysResource mAlarmResource;
    final ArrayList<IdleDispatchEntry> mAllowWhileIdleDispatches = new ArrayList();
    long mAllowWhileIdleMinTime;
    AppOpsManager mAppOps;
    private final Intent mBackgroundIntent = new Intent().addFlags(4);
    int mBroadcastRefCount = 0;
    final SparseArray<ArrayMap<String, BroadcastStats>> mBroadcastStats = new SparseArray();
    boolean mCancelRemoveAction = false;
    ClockReceiver mClockReceiver;
    final Constants mConstants = new Constants(this.mHandler);
    int mCurrentSeq = 0;
    PendingIntent mDateChangeSender;
    final DeliveryTracker mDeliveryTracker = new DeliveryTracker();
    int[] mDeviceIdleUserWhitelist = new int[0];
    final AlarmHandler mHandler = new AlarmHandler();
    private final SparseArray<AlarmClockInfo> mHandlerSparseAlarmClockArray = new SparseArray();
    Bundle mIdleOptions;
    ArrayList<InFlight> mInFlight = new ArrayList();
    boolean mInteractive = true;
    InteractiveStateReceiver mInteractiveStateReceiver;
    private boolean mIsScreenOn = true;
    long mLastAlarmDeliveryTime;
    final SparseLongArray mLastAllowWhileIdleDispatch = new SparseLongArray();
    long mLastTimeChangeClockTime;
    long mLastTimeChangeRealtime;
    boolean mLastWakeLockUnimportantForLogging;
    private long mLastWakeup;
    private long mLastWakeupSet;
    com.android.server.DeviceIdleController.LocalService mLocalDeviceIdleController;
    protected Object mLock = new Object();
    final LocalLog mLog = new LocalLog(TAG);
    long mMaxDelayTime = 0;
    long mNativeData;
    private final SparseArray<AlarmClockInfo> mNextAlarmClockForUser = new SparseArray();
    protected boolean mNextAlarmClockMayChange;
    private long mNextNonWakeup;
    long mNextNonWakeupDeliveryTime;
    Alarm mNextWakeFromIdle = null;
    private long mNextWakeup;
    long mNonInteractiveStartTime;
    long mNonInteractiveTime;
    int mNumDelayedAlarms = 0;
    int mNumTimeChanged;
    Alarm mPendingIdleUntil = null;
    ArrayList<Alarm> mPendingNonWakeupAlarms = new ArrayList();
    private final SparseBooleanArray mPendingSendNextAlarmClockChangedForUser = new SparseBooleanArray();
    ArrayList<Alarm> mPendingWhileIdleAlarms = new ArrayList();
    final HashMap<String, PriorityClass> mPriorities = new HashMap();
    Random mRandom;
    final LinkedList<WakeupEvent> mRecentWakeups = new LinkedList();
    private final IBinder mService = new Stub() {
        /* JADX WARNING: Missing block: B:28:0x00b7, code:
            if (r19.this$0.isContainsAppUidInWorksource(r32, "com.google.android.gm") != false) goto L_0x0087;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
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
                    flags |= 1;
                }
                if (alarmClock != null) {
                    flags |= 3;
                } else {
                    if (workSource != null || (callingUid >= 10000 && Arrays.binarySearch(AlarmManagerService.this.mDeviceIdleUserWhitelist, UserHandle.getAppId(callingUid)) < 0)) {
                        if (!AlarmManagerService.this.isContainsAppUidInWorksource(workSource, "com.android.email")) {
                            if (!AlarmManagerService.this.isContainsAppUidInWorksource(workSource, "com.android.exchange")) {
                            }
                        }
                    }
                    flags = (flags | 8) & -5;
                }
                AlarmManagerService.this.setImpl(type, triggerAtTime, windowLength, interval, operation, directReceiver, listenerTag, flags, workSource, alarmClock, callingUid, callingPackage);
                return;
            }
            throw new IllegalArgumentException("Repeating alarms cannot use AlarmReceivers");
        }

        public boolean setTime(long millis) {
            boolean z = false;
            AlarmManagerService.this.getContext().enforceCallingOrSelfPermission("android.permission.SET_TIME", "setTime");
            int uid = Binder.getCallingUid();
            Flog.i(500, "setTime,uid is " + uid + ",pid is " + Binder.getCallingPid());
            if (AlarmManagerService.this.mNativeData == 0) {
                Slog.w(AlarmManagerService.TAG, "Not setting time since no alarm driver is available.");
                return false;
            }
            synchronized (AlarmManagerService.this.mLock) {
                HwLog.dubaie("DUBAI_TAG_TIME_CHANGED", "delta=" + (millis - System.currentTimeMillis()));
                if (AlarmManagerService.this.setKernelTime(AlarmManagerService.this.mNativeData, millis) == 0) {
                    z = true;
                }
            }
            return z;
        }

        public void setTimeZone(String tz) {
            AlarmManagerService.this.getContext().enforceCallingOrSelfPermission("android.permission.SET_TIME_ZONE", "setTimeZone");
            int uid = Binder.getCallingUid();
            Flog.i(500, "setTimeZoneImpl,uid is " + uid + ",pid is " + Binder.getCallingPid() + ",tz = " + tz);
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
            return AlarmManagerService.this.getNextAlarmClockImpl(ActivityManager.handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), userId, false, false, "getNextAlarmClock", null));
        }

        protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            if (DumpUtils.checkDumpAndUsageStatsPermission(AlarmManagerService.this.getContext(), AlarmManagerService.TAG, pw)) {
                AlarmManagerService.this.dumpImpl(pw);
            }
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
    PendingIntent mTimeTickSender;
    private final SparseArray<AlarmClockInfo> mTmpSparseAlarmClockArray = new SparseArray();
    long mTotalDelayTime = 0;
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
            this.type = _type;
            this.origWhen = _when;
            boolean z = _type != 2 ? _type == 0 : true;
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
            this.creatorUid = this.operation != null ? this.operation.getCreatorUid() : this.uid;
        }

        public static String makeTag(PendingIntent pi, String tag, int type) {
            String alarmString = (type == 2 || type == 0) ? "*walarm*:" : "*alarm*:";
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
            return rec != null ? this.listener.asBinder().equals(rec.asBinder()) : false;
        }

        public boolean matches(String packageName) {
            if (this.operation != null) {
                return packageName.equals(this.operation.getTargetPackage());
            }
            return packageName.equals(this.packageName);
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
            sb.append('}');
            return sb.toString();
        }

        public void dump(PrintWriter pw, String prefix, long nowRTC, long nowELAPSED, SimpleDateFormat sdf) {
            boolean isRtc = this.type == 1 || this.type == 0;
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
                case 1:
                    ArrayList<Alarm> triggerList = new ArrayList();
                    synchronized (AlarmManagerService.this.mLock) {
                        AlarmManagerService.this.triggerAlarmsLocked(triggerList, SystemClock.elapsedRealtime(), System.currentTimeMillis());
                        AlarmManagerService.this.updateNextAlarmClockLocked();
                    }
                    for (i = 0; i < triggerList.size(); i++) {
                        Alarm alarm = (Alarm) triggerList.get(i);
                        try {
                            alarm.operation.send();
                        } catch (CanceledException e) {
                            if (alarm.repeatInterval > 0) {
                                AlarmManagerService.this.removeImpl(alarm.operation);
                            }
                        }
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
                        AlarmManagerService.this.mLocalDeviceIdleController.setAlarmsActive(msg.arg1 != 0);
                        return;
                    }
                    return;
                case 6:
                    ArrayList<String> array = msg.getData().getStringArrayList("clearlist");
                    if (array != null && array.size() > 0) {
                        for (i = 0; i < array.size(); i++) {
                            AlarmManagerService.this.removeImpl((String) array.get(i), AlarmManagerService.BAIDU_PUSHSERVICE_METHOD);
                        }
                        return;
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

        public void run() {
            ArrayList<Alarm> triggerList = new ArrayList();
            AlarmManagerService.this.hwRecordFirstTime();
            while (true) {
                AlarmManagerService alarmManagerService;
                int result = AlarmManagerService.this.waitForAlarm(AlarmManagerService.this.mNativeData);
                AlarmManagerService.this.mLastWakeup = SystemClock.elapsedRealtime();
                triggerList.clear();
                long nowRTC = System.currentTimeMillis();
                long nowELAPSED = SystemClock.elapsedRealtime();
                if ((65536 & result) != 0) {
                    long lastTimeChangeClockTime;
                    long expectedClockTime;
                    synchronized (AlarmManagerService.this.mLock) {
                        lastTimeChangeClockTime = AlarmManagerService.this.mLastTimeChangeClockTime;
                        expectedClockTime = lastTimeChangeClockTime + (nowELAPSED - AlarmManagerService.this.mLastTimeChangeRealtime);
                    }
                    if (lastTimeChangeClockTime == 0 || nowRTC < expectedClockTime - 1000 || nowRTC > 1000 + expectedClockTime) {
                        Flog.i(500, "Time changed notification from kernel; rebatching");
                        AlarmManagerService.this.removeImpl(AlarmManagerService.this.mTimeTickSender);
                        AlarmManagerService.this.removeImpl(AlarmManagerService.this.mDateChangeSender);
                        AlarmManagerService.this.rebatchAllAlarms();
                        AlarmManagerService.this.mClockReceiver.scheduleTimeTickEvent();
                        AlarmManagerService.this.mClockReceiver.scheduleDateChangedEvent();
                        synchronized (AlarmManagerService.this.mLock) {
                            alarmManagerService = AlarmManagerService.this;
                            alarmManagerService.mNumTimeChanged++;
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
                if (result != 65536) {
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
                            Flog.i(500, "there are no wakeup alarms and the screen is off, we can delay what we have so far until the future");
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
            this.flags = 0;
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
            return this.end >= whenElapsed && this.start <= maxWhen;
        }

        boolean add(Alarm alarm) {
            boolean newStart = false;
            int index = Collections.binarySearch(this.alarms, alarm, AlarmManagerService.sIncreasingTimeOrder);
            if (index < 0) {
                index = (0 - index) - 1;
            }
            this.alarms.add(index, alarm);
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

        boolean remove(PendingIntent operation, IAlarmListener listener) {
            if (operation == null && listener == null) {
                return false;
            }
            boolean didRemove = false;
            long newStart = 0;
            long newEnd = JobStatus.NO_LATEST_RUNTIME;
            int newFlags = 0;
            int i = 0;
            while (i < this.alarms.size()) {
                Alarm alarm = (Alarm) this.alarms.get(i);
                if (alarm.matches(operation, listener) || HwActivityManagerServiceUtil.isPendingIntentCanceled(alarm.operation)) {
                    this.alarms.remove(i);
                    didRemove = true;
                    AlarmManagerService.this.hwRemoveRtcAlarm(alarm, true);
                    if (alarm.alarmClock != null) {
                        AlarmManagerService.this.mNextAlarmClockMayChange = true;
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

        boolean remove(String packageName) {
            return remove(packageName, null);
        }

        boolean remove(String packageName, String action) {
            if (packageName == null) {
                return false;
            }
            boolean didRemove = false;
            long newStart = 0;
            long newEnd = JobStatus.NO_LATEST_RUNTIME;
            int newFlags = 0;
            for (int i = this.alarms.size() - 1; i >= 0; i--) {
                Alarm alarm = (Alarm) this.alarms.get(i);
                if (alarm.matches(packageName)) {
                    if (action != null && alarm.operation != null && (action.equals(AlarmManagerService.resetActionCallingIdentity(alarm.operation)) ^ 1) != 0) {
                        break;
                    }
                    this.alarms.remove(i);
                    didRemove = true;
                    if (action != null) {
                        Slog.d(AlarmManagerService.TAG, "remove package alarm " + packageName + "' (" + action + ")");
                    }
                    AlarmManagerService.this.hwRemoveRtcAlarm(alarm, true);
                    if (alarm.alarmClock != null) {
                        AlarmManagerService.this.mNextAlarmClockMayChange = true;
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
            boolean didRemove = false;
            long newStart = 0;
            long newEnd = JobStatus.NO_LATEST_RUNTIME;
            int newFlags = 0;
            int i = this.alarms.size() - 1;
            while (i >= 0) {
                Alarm alarm = (Alarm) this.alarms.get(i);
                try {
                    if (alarm.uid == uid && ActivityManager.getService().isAppStartModeDisabled(uid, alarm.packageName)) {
                        this.alarms.remove(i);
                        didRemove = true;
                        if (alarm.alarmClock != null) {
                            AlarmManagerService.this.mNextAlarmClockMayChange = true;
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
            boolean didRemove = false;
            long newStart = 0;
            long newEnd = JobStatus.NO_LATEST_RUNTIME;
            int i = 0;
            while (i < this.alarms.size()) {
                Alarm alarm = (Alarm) this.alarms.get(i);
                if (UserHandle.getUserId(alarm.creatorUid) == userHandle) {
                    this.alarms.remove(i);
                    didRemove = true;
                    AlarmManagerService.this.hwRemoveRtcAlarm(alarm, true);
                    if (alarm.alarmClock != null) {
                        AlarmManagerService.this.mNextAlarmClockMayChange = true;
                    }
                } else {
                    if (alarm.whenElapsed > newStart) {
                        newStart = alarm.whenElapsed;
                    }
                    if (alarm.maxWhenElapsed < newEnd) {
                        newEnd = alarm.maxWhenElapsed;
                    }
                    i++;
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
            for (int i = 0; i < N; i++) {
                if (((Alarm) this.alarms.get(i)).matches(packageName)) {
                    return true;
                }
            }
            return false;
        }

        boolean hasWakeups() {
            int N = this.alarms.size();
            for (int i = 0; i < N; i++) {
                if ((((Alarm) this.alarms.get(i)).type & 1) == 0) {
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
        final ArrayMap<String, FilterStats> filterStats = new ArrayMap();
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
                Flog.i(500, "Received DATE_CHANGED alarm; rescheduling");
                AlarmManagerService.this.setKernelTimezone(AlarmManagerService.this.mNativeData, -(TimeZone.getTimeZone(SystemProperties.get(AlarmManagerService.TIMEZONE_PROPERTY)).getOffset(System.currentTimeMillis()) / 60000));
                scheduleDateChangedEvent();
            }
        }

        public void scheduleTimeTickEvent() {
            long currentTime = System.currentTimeMillis();
            long triggerAtTime = SystemClock.elapsedRealtime() + ((LingerMonitor.DEFAULT_NOTIFICATION_RATE_LIMIT_MILLIS * ((currentTime / LingerMonitor.DEFAULT_NOTIFICATION_RATE_LIMIT_MILLIS) + 1)) - currentTime);
            Flog.i(500, "scheduleTimeTickEvent triggerAtTime = " + triggerAtTime);
            AlarmManagerService.this.setImpl(3, triggerAtTime, 0, 0, AlarmManagerService.this.mTimeTickSender, null, "time_tick", 1, null, null, Process.myUid(), "android");
        }

        public void scheduleDateChangedEvent() {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(11, 0);
            calendar.set(12, 0);
            calendar.set(13, 0);
            calendar.set(14, 0);
            calendar.add(5, 1);
            AlarmManagerService.this.setImpl(1, calendar.getTimeInMillis(), 0, 0, AlarmManagerService.this.mDateChangeSender, null, null, 1, null, null, Process.myUid(), "android");
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
        public long ALLOW_WHILE_IDLE_LONG_TIME = DEFAULT_ALLOW_WHILE_IDLE_LONG_TIME;
        public long ALLOW_WHILE_IDLE_SHORT_TIME = 5000;
        public long ALLOW_WHILE_IDLE_WHITELIST_DURATION = 10000;
        public long LISTENER_TIMEOUT = 5000;
        public long MIN_FUTURITY = 5000;
        public long MIN_INTERVAL = 60000;
        private long mLastAllowWhileIdleWhitelistDuration = -1;
        private final KeyValueListParser mParser = new KeyValueListParser(',');
        private ContentResolver mResolver;

        public Constants(Handler handler) {
            super(handler);
            updateAllowWhileIdleMinTimeLocked();
            updateAllowWhileIdleWhitelistDurationLocked();
        }

        public void start(ContentResolver resolver) {
            this.mResolver = resolver;
            this.mResolver.registerContentObserver(Global.getUriFor("alarm_manager_constants"), false, this);
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
                    Slog.e(AlarmManagerService.TAG, "Bad alarm manager settings", e);
                }
                this.MIN_FUTURITY = this.mParser.getLong(KEY_MIN_FUTURITY, 5000);
                this.MIN_INTERVAL = this.mParser.getLong(KEY_MIN_INTERVAL, 60000);
                this.ALLOW_WHILE_IDLE_SHORT_TIME = this.mParser.getLong(KEY_ALLOW_WHILE_IDLE_SHORT_TIME, 5000);
                this.ALLOW_WHILE_IDLE_LONG_TIME = this.mParser.getLong(KEY_ALLOW_WHILE_IDLE_LONG_TIME, DEFAULT_ALLOW_WHILE_IDLE_LONG_TIME);
                this.ALLOW_WHILE_IDLE_WHITELIST_DURATION = this.mParser.getLong(KEY_ALLOW_WHILE_IDLE_WHITELIST_DURATION, 10000);
                this.LISTENER_TIMEOUT = this.mParser.getLong(KEY_LISTENER_TIMEOUT, 5000);
                updateAllowWhileIdleMinTimeLocked();
                updateAllowWhileIdleWhitelistDurationLocked();
            }
            return;
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

    class DeliveryTracker extends IAlarmCompleteListener.Stub implements OnFinished {
        DeliveryTracker() {
        }

        private InFlight removeLocked(PendingIntent pi, Intent intent) {
            for (int i = 0; i < AlarmManagerService.this.mInFlight.size(); i++) {
                if (((InFlight) AlarmManagerService.this.mInFlight.get(i)).mPendingIntent == pi) {
                    return (InFlight) AlarmManagerService.this.mInFlight.remove(i);
                }
            }
            AlarmManagerService.this.mLog.w("No in-flight alarm for " + pi + " " + intent);
            return null;
        }

        private InFlight removeLocked(IBinder listener) {
            for (int i = 0; i < AlarmManagerService.this.mInFlight.size(); i++) {
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
                bs.nesting = 0;
                bs.aggregateTime += nowELAPSED - bs.startTime;
            }
            FilterStats fs = inflight.mFilterStats;
            fs.nesting--;
            if (fs.nesting <= 0) {
                fs.nesting = 0;
                fs.aggregateTime += nowELAPSED - fs.startTime;
            }
            if (inflight.mWorkSource == null || inflight.mWorkSource.size() <= 0) {
                ActivityManager.noteAlarmFinish(inflight.mPendingIntent, inflight.mUid, inflight.mTag);
                return;
            }
            for (int wi = 0; wi < inflight.mWorkSource.size(); wi++) {
                ActivityManager.noteAlarmFinish(inflight.mPendingIntent, inflight.mWorkSource.get(wi), inflight.mTag);
            }
        }

        private void updateTrackingLocked(InFlight inflight) {
            if (inflight != null) {
                updateStatsLocked(inflight);
            }
            AlarmManagerService alarmManagerService = AlarmManagerService.this;
            alarmManagerService.mBroadcastRefCount--;
            if (AlarmManagerService.this.mBroadcastRefCount == 0) {
                AlarmManagerService.this.mHandler.obtainMessage(4, Integer.valueOf(0)).sendToTarget();
                AlarmManagerService.this.mWakeLock.release();
                if (AlarmManagerService.this.mInFlight.size() > 0) {
                    AlarmManagerService.this.mLog.w("Finished all dispatches with " + AlarmManagerService.this.mInFlight.size() + " remaining inflights");
                    for (int i = 0; i < AlarmManagerService.this.mInFlight.size(); i++) {
                        AlarmManagerService.this.mLog.w("  Remaining #" + i + ": " + AlarmManagerService.this.mInFlight.get(i));
                    }
                    AlarmManagerService.this.mInFlight.clear();
                }
            } else if (AlarmManagerService.this.mInFlight.size() > 0) {
                InFlight inFlight = (InFlight) AlarmManagerService.this.mInFlight.get(0);
                AlarmManagerService.this.setWakelockWorkSource(inFlight.mPendingIntent, inFlight.mWorkSource, inFlight.mAlarmType, inFlight.mTag, -1, false);
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
                AlarmManagerService.this.mBackgroundIntent.addHwFlags(2048);
                try {
                    alarm.operation.send(AlarmManagerService.this.getContext(), 0, AlarmManagerService.this.mBackgroundIntent.putExtra("android.intent.extra.ALARM_COUNT", alarm.count), AlarmManagerService.this.mDeliveryTracker, AlarmManagerService.this.mHandler, null, allowWhileIdle ? AlarmManagerService.this.mIdleOptions : null);
                    AlarmManagerService.this.hwRemoveRtcAlarm(alarm, false);
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
                AlarmManagerService.this.setWakelockWorkSource(alarm.operation, alarm.workSource, alarm.type, alarm.statsTag, alarm.operation == null ? alarm.uid : -1, true);
                AlarmManagerService.this.mWakeLock.acquire();
                AlarmManagerService.this.mHandler.obtainMessage(4, Integer.valueOf(1)).sendToTarget();
            }
            InFlight inflight = new InFlight(AlarmManagerService.this, alarm.operation, alarm.listener, alarm.workSource, alarm.uid, alarm.packageName, alarm.type, alarm.statsTag, nowELAPSED);
            AlarmManagerService.this.mInFlight.add(inflight);
            AlarmManagerService alarmManagerService = AlarmManagerService.this;
            alarmManagerService.mBroadcastRefCount++;
            if (allowWhileIdle) {
                AlarmManagerService.this.mLastAllowWhileIdleDispatch.put(alarm.uid, nowELAPSED);
            }
            BroadcastStats bs = inflight.mBroadcastStats;
            bs.count++;
            if (bs.nesting == 0) {
                bs.nesting = 1;
                bs.startTime = nowELAPSED;
            } else {
                bs.nesting++;
            }
            FilterStats fs = inflight.mFilterStats;
            fs.count++;
            if (fs.nesting == 0) {
                fs.nesting = 1;
                fs.startTime = nowELAPSED;
            } else {
                fs.nesting++;
            }
            if (alarm.type == 2 || alarm.type == 0) {
                bs.numWakeup++;
                fs.numWakeup++;
                if (alarm.workSource == null || alarm.workSource.size() <= 0) {
                    ActivityManager.noteWakeupAlarm(alarm.operation, alarm.uid, alarm.packageName, alarm.statsTag);
                } else {
                    for (int wi = 0; wi < alarm.workSource.size(); wi++) {
                        String wsName = alarm.workSource.getName(wi);
                        PendingIntent pendingIntent = alarm.operation;
                        int i = alarm.workSource.get(wi);
                        if (wsName == null) {
                            wsName = alarm.packageName;
                        }
                        ActivityManager.noteWakeupAlarm(pendingIntent, i, wsName, alarm.statsTag);
                    }
                }
            }
            String pkg = alarm.packageName;
            if (!(pkg == null || (AlarmManagerService.this.mInteractive && ("android".equals(pkg) ^ 1) == 0))) {
                String intentStr = "";
                if (alarm.operation != null) {
                    Intent alarmIntent = alarm.operation.getIntent();
                    if (alarmIntent != null) {
                        intentStr = alarmIntent.getAction();
                    }
                } else if (alarm.statsTag != null) {
                    intentStr = alarm.statsTag;
                }
                LogPower.push(121, pkg, String.valueOf(alarm.type), String.valueOf(alarm.repeatInterval), new String[]{String.valueOf(fs.count), intentStr});
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

        public String toString() {
            return "FilterStats{tag=" + this.mTag + ", lastTime=" + this.lastTime + ", aggregateTime=" + this.aggregateTime + ", count=" + this.count + ", numWakeup=" + this.numWakeup + ", startTime=" + this.startTime + ", nesting=" + this.nesting + "}";
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

        public String toString() {
            return "InFlight{pendingIntent=" + this.mPendingIntent + ", workSource=" + this.mWorkSource + ", uid=" + this.mUid + ", tag=" + this.mTag + ", broadcastStats=" + this.mBroadcastStats + ", filterStats=" + this.mFilterStats + ", alarmType=" + this.mAlarmType + "}";
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
                    AlarmManagerService.this.mIsScreenOn = true;
                } else if ("android.intent.action.SCREEN_OFF".equals(intent.getAction())) {
                    AlarmManagerService.this.mIsScreenOn = false;
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
        int priority = 2;
        int seq;

        PriorityClass() {
            this.seq = AlarmManagerService.this.mCurrentSeq - 1;
        }
    }

    final class UidObserver extends IUidObserver.Stub {
        UidObserver() {
        }

        public void onUidStateChanged(int uid, int procState, long procStateSeq) throws RemoteException {
        }

        public void onUidGone(int uid, boolean disabled) throws RemoteException {
            if (disabled) {
                synchronized (AlarmManagerService.this.mLock) {
                    AlarmManagerService.this.removeForStoppedLocked(uid);
                }
            }
        }

        public void onUidActive(int uid) throws RemoteException {
        }

        public void onUidIdle(int uid, boolean disabled) throws RemoteException {
            if (disabled) {
                synchronized (AlarmManagerService.this.mLock) {
                    AlarmManagerService.this.removeForStoppedLocked(uid);
                }
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

        /* JADX WARNING: Missing block: B:62:0x0107, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onReceive(Context context, Intent intent) {
            synchronized (AlarmManagerService.this.mLock) {
                String action = intent.getAction();
                String[] pkgList = null;
                Slog.v(AlarmManagerService.TAG, "UninstallReceiver onReceive action:" + action);
                if ("android.intent.action.QUERY_PACKAGE_RESTART".equals(action)) {
                    for (String packageName : intent.getStringArrayExtra("android.intent.extra.PACKAGES")) {
                        if (AlarmManagerService.this.lookForPackageLocked(packageName)) {
                            setResultCode(-1);
                            return;
                        }
                    }
                    return;
                }
                if ("android.intent.action.EXTERNAL_APPLICATIONS_UNAVAILABLE".equals(action)) {
                    pkgList = intent.getStringArrayExtra("android.intent.extra.changed_package_list");
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
                } else if ("android.intent.action.PACKAGE_REMOVED".equals(action) && intent.getBooleanExtra("android.intent.extra.REPLACING", false)) {
                    return;
                } else {
                    Uri data = intent.getData();
                    if (!(data == null || data.getSchemeSpecificPart() == null)) {
                        pkgList = new String[]{data.getSchemeSpecificPart()};
                    }
                }
                if (pkgList != null && pkgList.length > 0) {
                    for (String pkg : pkgList) {
                        AlarmManagerService.this.removeLocked(pkg);
                        AlarmManagerService.this.mPriorities.remove(pkg);
                        for (int i = AlarmManagerService.this.mBroadcastStats.size() - 1; i >= 0; i--) {
                            ArrayMap<String, BroadcastStats> uidStats = (ArrayMap) AlarmManagerService.this.mBroadcastStats.valueAt(i);
                            if (!(uidStats == null || uidStats.remove(pkg) == null || uidStats.size() > 0)) {
                                AlarmManagerService.this.mBroadcastStats.removeAt(i);
                            }
                        }
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

    private native void set(long j, int i, long j2, long j3);

    private native int setKernelTime(long j, long j2);

    private native int setKernelTimezone(long j, int i);

    private native int waitForAlarm(long j);

    protected native void hwSetClockRTC(long j, long j2, long j3);

    static {
        boolean isLoggable = !Log.HWINFO ? Log.HWModuleLog ? Log.isLoggable(TAG, 4) : false : true;
        DEBUG_TIMEZONE = isLoggable;
    }

    void calculateDeliveryPriorities(ArrayList<Alarm> alarms) {
        int N = alarms.size();
        for (int i = 0; i < N; i++) {
            int alarmPrio;
            String alarmPackage;
            Alarm a = (Alarm) alarms.get(i);
            if (a.operation != null && "android.intent.action.TIME_TICK".equals(resetActionCallingIdentity(a.operation))) {
                alarmPrio = 0;
            } else if (a.wakeup) {
                alarmPrio = 1;
            } else {
                alarmPrio = 2;
            }
            PriorityClass packagePrio = a.priorityClass;
            if (a.operation != null) {
                alarmPackage = a.operation.getCreatorPackage();
            } else {
                alarmPackage = a.packageName;
            }
            if (packagePrio == null) {
                packagePrio = (PriorityClass) this.mPriorities.get(alarmPackage);
            }
            if (packagePrio == null) {
                packagePrio = new PriorityClass();
                a.priorityClass = packagePrio;
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

    public AlarmManagerService(Context context) {
        super(context);
    }

    static long convertToElapsed(long when, int type) {
        boolean isRtc = type == 1 || type == 0;
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
        if (futurity < 10000) {
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
            return true;
        }
        return false;
    }

    int attemptCoalesceLocked(long whenElapsed, long maxWhen) {
        int N = this.mAlarmBatches.size();
        for (int i = 0; i < N; i++) {
            Batch b = (Batch) this.mAlarmBatches.get(i);
            if ((b.flags & 1) == 0 && b.canHold(whenElapsed, maxWhen)) {
                return i;
            }
        }
        return -1;
    }

    void rebatchAllAlarms() {
        synchronized (this.mLock) {
            rebatchAllAlarmsLocked(true);
        }
    }

    void rebatchAllAlarmsLocked(boolean doValidate) {
        ArrayList<Batch> oldSet = (ArrayList) this.mAlarmBatches.clone();
        this.mAlarmBatches.clear();
        Alarm oldPendingIdleUntil = this.mPendingIdleUntil;
        long nowElapsed = SystemClock.elapsedRealtime();
        int oldBatches = oldSet.size();
        this.mCancelRemoveAction = true;
        for (int batchNum = 0; batchNum < oldBatches; batchNum++) {
            Batch batch = (Batch) oldSet.get(batchNum);
            int N = batch.size();
            for (int i = 0; i < N; i++) {
                reAddAlarmLocked(batch.get(i), nowElapsed, doValidate);
            }
        }
        this.mCancelRemoveAction = false;
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
        setImplLocked(a, true, doValidate);
    }

    void restorePendingWhileIdleAlarmsLocked() {
        if (this.mPendingWhileIdleAlarms.size() > 0) {
            ArrayList<Alarm> alarms = this.mPendingWhileIdleAlarms;
            this.mPendingWhileIdleAlarms = new ArrayList();
            long nowElapsed = SystemClock.elapsedRealtime();
            for (int i = alarms.size() - 1; i >= 0; i--) {
                reAddAlarmLocked((Alarm) alarms.get(i), nowElapsed, false);
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
        Flog.d(500, "alarmmanagerservice onStart");
        setTimeZoneImpl(SystemProperties.get(TIMEZONE_PROPERTY));
        if (this.mNativeData != 0) {
            long systemBuildTime = Environment.getRootDirectory().lastModified();
            if (System.currentTimeMillis() < systemBuildTime) {
                Slog.i(TAG, "Current time only " + System.currentTimeMillis() + ", advancing to build time " + systemBuildTime);
                setKernelTime(this.mNativeData, systemBuildTime);
            }
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
            ActivityManager.getService().registerUidObserver(new UidObserver(), 4, -1, null);
        } catch (RemoteException e) {
        }
        publishBinderService("alarm", this.mService);
        publishLocalService(LocalService.class, new LocalService());
    }

    public void onBootPhase(int phase) {
        if (phase == 500) {
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
            boolean timeZoneWasChanged = false;
            synchronized (this) {
                String current = SystemProperties.get(TIMEZONE_PROPERTY);
                if (current == null || (current.equals(zone.getID()) ^ 1) != 0) {
                    Flog.i(500, "timezone changed: " + current + ", new=" + zone.getID());
                    timeZoneWasChanged = true;
                    SystemProperties.set(TIMEZONE_PROPERTY, zone.getID());
                }
                setKernelTimezone(this.mNativeData, -(zone.getOffset(System.currentTimeMillis()) / 60000));
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
                    int count = 0;
                    String pkg = operation.getTargetPackage();
                    for (int i = 0; i < this.mAlarmBatches.size(); i++) {
                        Batch batch = (Batch) this.mAlarmBatches.get(i);
                        int j = 0;
                        while (batch != null && j < batch.size()) {
                            Alarm alarm = batch.get(j);
                            if (!(alarm.operation == null || pkg == null)) {
                                if ((pkg.equals("android") ^ 1) != 0) {
                                    if (pkg.equals(alarm.operation.getTargetPackage())) {
                                        count++;
                                    }
                                }
                            }
                            j++;
                        }
                    }
                    if (this.mAlarmResource == null) {
                        this.mAlarmResource = HwFrameworkFactory.getHwResource(13);
                    }
                    if (this.mAlarmResource != null && 2 == this.mAlarmResource.acquire(operation.getCreatorUid(), pkg, -1, count)) {
                        return;
                    }
                }
                if (type == 0 || 2 == type) {
                    Flog.i(500, "set(" + operation + ") : type=" + type + " triggerAtTime=" + triggerAtTime + " win=" + windowLength + " tElapsed=" + triggerElapsed + " maxElapsed=" + maxElapsed + " interval=" + interval + " flags=0x" + Integer.toHexString(flags));
                }
                setImplLocked(type, triggerAtTime, triggerElapsed, windowLength, maxElapsed, interval, operation, directReceiver, listenerTag, flags, true, workSource, alarmClock, callingUid, callingPackage);
                return;
            }
        }
        Slog.w(TAG, "Alarms must either supply a PendingIntent or an AlarmReceiver");
    }

    private void setImplLocked(int type, long when, long whenElapsed, long windowLength, long maxWhen, long interval, PendingIntent operation, IAlarmListener directReceiver, String listenerTag, int flags, boolean doValidate, WorkSource workSource, AlarmClockInfo alarmClock, int callingUid, String callingPackage) {
        Alarm a = new Alarm(type, when, whenElapsed, windowLength, maxWhen, interval, operation, directReceiver, listenerTag, workSource, flags, alarmClock, callingUid, callingPackage);
        try {
            if (ActivityManager.getService().isAppStartModeDisabled(callingUid, callingPackage)) {
                Slog.w(TAG, "Not setting alarm from " + callingUid + ":" + a + " -- package not allowed to start");
                return;
            }
        } catch (RemoteException e) {
        }
        if (!this.mCancelRemoveAction) {
            removeLocked(operation, directReceiver);
        }
        setImplLocked(a, false, doValidate);
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
        int whichBatch = (a.flags & 1) != 0 ? -1 : attemptCoalesceLocked(a.whenElapsed, a.maxWhenElapsed);
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
            this.mNextAlarmClockMayChange = true;
        }
        boolean needRebatch = false;
        if ((a.flags & 16) != 0) {
            if (!(this.mPendingIdleUntil == a || this.mPendingIdleUntil == null)) {
                Slog.wtfStack(TAG, "setImplLocked: idle until changed from " + this.mPendingIdleUntil + " to " + a);
            }
            this.mPendingIdleUntil = a;
            this.mConstants.updateAllowWhileIdleMinTimeLocked();
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
            ArrayMap<String, BroadcastStats> uidStats;
            int ip;
            BroadcastStats bs;
            int is;
            FilterStats fs;
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
            for (i = 0; i < this.mNextAlarmClockForUser.size(); i++) {
                users.add(Integer.valueOf(this.mNextAlarmClockForUser.keyAt(i)));
            }
            for (i = 0; i < this.mPendingSendNextAlarmClockChangedForUser.size(); i++) {
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
                for (i = 0; i < this.mInFlight.size(); i++) {
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
                for (i = 0; i < this.mLastAllowWhileIdleDispatch.size(); i++) {
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
                        return 1;
                    }
                    if (lhs.aggregateTime > rhs.aggregateTime) {
                        return -1;
                    }
                    return 0;
                }
            };
            int len = 0;
            for (iu = 0; iu < this.mBroadcastStats.size(); iu++) {
                uidStats = (ArrayMap) this.mBroadcastStats.valueAt(iu);
                for (ip = 0; ip < uidStats.size(); ip++) {
                    bs = (BroadcastStats) uidStats.valueAt(ip);
                    for (is = 0; is < bs.filterStats.size(); is++) {
                        fs = (FilterStats) bs.filterStats.valueAt(is);
                        int pos = len > 0 ? Arrays.binarySearch(topFilters, 0, len, fs, anonymousClass3) : 0;
                        if (pos < 0) {
                            pos = (-pos) - 1;
                        }
                        if (pos < topFilters.length) {
                            int copylen = (topFilters.length - pos) - 1;
                            if (copylen > 0) {
                                System.arraycopy(topFilters, pos, topFilters, pos + 1, copylen);
                            }
                            topFilters[pos] = fs;
                            if (len < topFilters.length) {
                                len++;
                            }
                        }
                    }
                }
            }
            if (len > 0) {
                pw.println("  Top Alarms:");
                for (i = 0; i < len; i++) {
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
            for (iu = 0; iu < this.mBroadcastStats.size(); iu++) {
                uidStats = (ArrayMap) this.mBroadcastStats.valueAt(iu);
                for (ip = 0; ip < uidStats.size(); ip++) {
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
                    for (is = 0; is < bs.filterStats.size(); is++) {
                        tmpFilters.add((FilterStats) bs.filterStats.valueAt(is));
                    }
                    Collections.sort(tmpFilters, anonymousClass3);
                    for (i = 0; i < tmpFilters.size(); i++) {
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
        ByteArrayOutputStream bs = new ByteArrayOutputStream(2048);
        PrintWriter pw = new PrintWriter(bs);
        long nowRTC = System.currentTimeMillis();
        long nowELAPSED = SystemClock.elapsedRealtime();
        int NZ = this.mAlarmBatches.size();
        for (int iz = 0; iz < NZ; iz++) {
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
        return true;
    }

    private Batch findFirstWakeupBatchLocked() {
        int N = this.mAlarmBatches.size();
        int i = 0;
        while (i < N) {
            try {
                Batch b = (Batch) this.mAlarmBatches.get(i);
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
            this.mNextAlarmClockMayChange = false;
            SparseArray<AlarmClockInfo> nextForUser = this.mTmpSparseAlarmClockArray;
            nextForUser.clear();
            int N = this.mAlarmBatches.size();
            for (i = 0; i < N; i++) {
                ArrayList<Alarm> alarms = ((Batch) this.mAlarmBatches.get(i)).alarms;
                int M = alarms.size();
                for (int j = 0; j < M; j++) {
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
            for (i = 0; i < NN; i++) {
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
        this.mPendingSendNextAlarmClockChangedForUser.put(userId, true);
        this.mHandler.removeMessages(2);
        this.mHandler.sendEmptyMessage(2);
    }

    private void sendNextAlarmClockChanged() {
        int N;
        int i;
        int userId;
        SparseArray<AlarmClockInfo> pendingUsers = this.mHandlerSparseAlarmClockArray;
        pendingUsers.clear();
        synchronized (this.mLock) {
            N = this.mPendingSendNextAlarmClockChangedForUser.size();
            for (i = 0; i < N; i++) {
                userId = this.mPendingSendNextAlarmClockChangedForUser.keyAt(i);
                pendingUsers.append(userId, (AlarmClockInfo) this.mNextAlarmClockForUser.get(userId));
            }
            this.mPendingSendNextAlarmClockChangedForUser.clear();
        }
        N = pendingUsers.size();
        for (i = 0; i < N; i++) {
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
            Batch firstBatch = (Batch) this.mAlarmBatches.get(0);
            if (!(firstWakeup == null || this.mNextWakeup == firstWakeup.start)) {
                this.mNextWakeup = firstWakeup.start;
                this.mLastWakeupSet = SystemClock.elapsedRealtime();
                setLocked(2, firstWakeup.start);
            }
            if (firstBatch != firstWakeup) {
                nextNonWakeup = firstBatch.start;
            }
        }
        if (this.mPendingNonWakeupAlarms.size() > 0 && (nextNonWakeup == 0 || this.mNextNonWakeupDeliveryTime < nextNonWakeup)) {
            nextNonWakeup = this.mNextNonWakeupDeliveryTime;
        }
        if (!(nextNonWakeup == 0 || this.mNextNonWakeup == nextNonWakeup)) {
            if ("factory".equals(SystemProperties.get("ro.runmode", "normal")) && nextNonWakeup == this.mNextWakeup) {
                Flog.w(500, "no need set for the time had been set by type 2");
            } else {
                this.mNextNonWakeup = nextNonWakeup;
                setLocked(3, nextNonWakeup);
            }
        }
    }

    void removeLocked(PendingIntent operation, IAlarmListener directReceiver) {
        int i;
        int didRemove = 0;
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
            Flog.i(500, "remove(" + operation + ") changed bounds; rebatching");
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
        int didRemove = 0;
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
            rebatchAllAlarmsLocked(true);
            rescheduleKernelAlarmsLocked();
            updateNextAlarmClockLocked();
        }
    }

    void removeForStoppedLocked(int uid) {
        int i;
        int didRemove = 0;
        for (i = this.mAlarmBatches.size() - 1; i >= 0; i--) {
            Batch b = (Batch) this.mAlarmBatches.get(i);
            didRemove |= b.removeForStopped(uid);
            if (b.size() == 0) {
                this.mAlarmBatches.remove(i);
            }
        }
        for (i = this.mPendingWhileIdleAlarms.size() - 1; i >= 0; i--) {
            if (((Alarm) this.mPendingWhileIdleAlarms.get(i)).uid == uid) {
                this.mPendingWhileIdleAlarms.remove(i);
            }
        }
        if (didRemove != 0) {
            rebatchAllAlarmsLocked(true);
            rescheduleKernelAlarmsLocked();
            updateNextAlarmClockLocked();
        }
    }

    void removeUserLocked(int userHandle) {
        int i;
        int didRemove = 0;
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
            rebatchAllAlarmsLocked(true);
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
        for (i = 0; i < this.mAlarmBatches.size(); i++) {
            if (((Batch) this.mAlarmBatches.get(i)).hasPackage(packageName)) {
                return true;
            }
        }
        for (i = 0; i < this.mPendingWhileIdleAlarms.size(); i++) {
            if (((Alarm) this.mPendingWhileIdleAlarms.get(i)).matches(packageName)) {
                return true;
            }
        }
        return false;
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
        msg.what = 1;
        this.mHandler.removeMessages(1);
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
        int i;
        boolean hasWakeup = false;
        while (this.mAlarmBatches.size() > 0) {
            Batch batch = (Batch) this.mAlarmBatches.get(0);
            if (batch.start > nowELAPSED) {
                break;
            }
            this.mAlarmBatches.remove(0);
            int N = batch.size();
            for (i = 0; i < N; i++) {
                Alarm alarm = batch.get(i);
                if ((alarm.flags & 4) != 0) {
                    long minTime = this.mLastAllowWhileIdleDispatch.get(alarm.uid, 0) + this.mAllowWhileIdleMinTime;
                    if (nowELAPSED < minTime) {
                        alarm.whenElapsed = minTime;
                        if (alarm.maxWhenElapsed < minTime) {
                            alarm.maxWhenElapsed = minTime;
                        }
                        setImplLocked(alarm, true, false);
                    }
                }
                alarm.count = 1;
                triggerList.add(alarm);
                if ((alarm.flags & 2) != 0) {
                    EventLogTags.writeDeviceIdleWakeFromIdle(this.mPendingIdleUntil != null ? 1 : 0, alarm.statsTag);
                }
                if (this.mPendingIdleUntil == alarm) {
                    this.mPendingIdleUntil = null;
                    rebatchAllAlarmsLocked(false);
                    restorePendingWhileIdleAlarmsLocked();
                }
                if (this.mNextWakeFromIdle == alarm) {
                    this.mNextWakeFromIdle = null;
                    rebatchAllAlarmsLocked(false);
                }
                if (alarm.repeatInterval > 0) {
                    alarm.count = (int) (((long) alarm.count) + ((nowELAPSED - alarm.whenElapsed) / alarm.repeatInterval));
                    long delta = ((long) alarm.count) * alarm.repeatInterval;
                    long nextElapsed = alarm.whenElapsed + delta;
                    if (alarm.type == 0 || 2 == alarm.type) {
                        Flog.i(500, "set again repeat alarm: next ,nextElapsed = " + nextElapsed + ",repeatInterval = " + alarm.repeatInterval);
                    }
                    setImplLocked(alarm.type, alarm.when + delta, nextElapsed, alarm.windowLength, maxTriggerTime(nowELAPSED, nextElapsed, alarm.repeatInterval), alarm.repeatInterval, alarm.operation, null, null, alarm.flags, true, alarm.workSource, alarm.alarmClock, alarm.uid, alarm.packageName);
                }
                if (alarm.wakeup) {
                    hasWakeup = true;
                }
                if (alarm.alarmClock != null) {
                    this.mNextAlarmClockMayChange = true;
                }
            }
        }
        this.mCurrentSeq++;
        calculateDeliveryPriorities(triggerList);
        Collections.sort(triggerList, this.mAlarmDispatchComparator);
        ArrayList<Alarm> wakeupAlarms = new ArrayList();
        for (i = 0; i < triggerList.size(); i++) {
            Alarm talarm = (Alarm) triggerList.get(i);
            if (talarm.type == 0 || 2 == talarm.type) {
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
            Flog.i(500, "Alarm triggering (type 0 or 2): " + wakeupAlarms);
        }
        return hasWakeup;
    }

    void recordWakeupAlarms(ArrayList<Batch> batches, long nowELAPSED, long nowRTC) {
        int numBatches = batches.size();
        int nextBatch = 0;
        while (nextBatch < numBatches) {
            Batch b = (Batch) batches.get(nextBatch);
            if (b.start <= nowELAPSED) {
                int numAlarms = b.alarms.size();
                for (int nextAlarm = 0; nextAlarm < numAlarms; nextAlarm++) {
                    this.mRecentWakeups.add(((Alarm) b.alarms.get(nextAlarm)).makeWakeupEvent(nowRTC));
                }
                nextBatch++;
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

    void deliverAlarmsLocked(ArrayList<Alarm> triggerList, long nowELAPSED) {
        this.mLastAlarmDeliveryTime = nowELAPSED;
        for (int i = 0; i < triggerList.size(); i++) {
            Alarm alarm = (Alarm) triggerList.get(i);
            if (alarm == null) {
                HwLog.dubaie("DUBAI_TAG_ALARM_TRIGGER", "uid=0 name=NULL type=-1 tag=NULL size=" + triggerList.size() + " worksource=0");
            } else {
                boolean allowWhileIdle = (alarm.flags & 4) != 0;
                try {
                    String packageName;
                    int wi;
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
                        wi = 0;
                        while (wi < alarm.workSource.size()) {
                            HwLog.dubaie("DUBAI_TAG_ALARM_WORKSOURCE", "uid=" + alarm.workSource.get(wi) + " name=" + alarm.workSource.getName(wi) + " tag=" + alarm.statsTag + " finished=" + (wi == alarm.workSource.size() + -1 ? "1" : "0"));
                            wi++;
                        }
                    }
                    if (alarm.workSource == null || alarm.workSource.size() <= 0) {
                        ActivityManager.noteAlarmStart(alarm.operation, alarm.uid, alarm.statsTag);
                    } else {
                        for (wi = 0; wi < alarm.workSource.size(); wi++) {
                            ActivityManager.noteAlarmStart(alarm.operation, alarm.workSource.get(wi), alarm.statsTag);
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
            boolean unimportant = pi == this.mTimeTickSender;
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
                uid = ActivityManager.getService().getUidForIntentSender(pi.getTarget());
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
}
