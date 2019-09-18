package com.android.server.pg;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.WorkSource;
import android.util.ArrayMap;
import android.util.MutableInt;
import android.util.Slog;
import com.android.internal.os.BatteryStatsImpl;
import java.util.ArrayList;
import java.util.List;

public class ProcBatteryStats {
    private static final String DESCRIPTOR = "com.huawei.pgmng.api.IPGManager";
    private static final int GET_POWER_STATS_TRANSACTION = 101;
    private static final int MAX_WAKELOCKS_PER_UID = 100;
    private static final int MAX_WAKERLOCKS_WEIXIN = 60;
    private static final int NOTE_RESET_ALL_INFO_TRANSACTION = 102;
    private static final int POWER_SOURCE_TYPE_WAKELOCK = 0;
    private static final int SCREEN_STATE_OFF = 0;
    private static final int SCREEN_STATE_ON = 1;
    private static final int STATS_TYPE = 0;
    private static final int SYS_EVENT_POWER_CONNECTED = 1;
    private static final int SYS_EVENT_POWER_DISCONNECTED = 2;
    private static final int SYS_EVENT_SCREEN_OFF = 6;
    private static final int SYS_EVENT_SCREEN_ON = 5;
    private static final int SYS_EVENT_UID_REMOVED = 4;
    private static final int SYS_EVENT_USER_REMOVED = 3;
    private static final String TAG = "ProcBatteryStats";
    /* access modifiers changed from: private */
    public boolean DEBUG_COMMON = false;
    private boolean DEBUG_SCREENON = false;
    /* access modifiers changed from: private */
    public boolean DEBUG_WAKELOCK = false;
    /* access modifiers changed from: private */
    public final ArrayMap<String, Integer> mActionIdMap = new ArrayMap<>();
    /* access modifiers changed from: private */
    public BatteryStatsImpl.Clocks mClocks = new BatteryStatsImpl.SystemClocks();
    private Context mContext = null;
    /* access modifiers changed from: private */
    public final BatteryStatsImpl.TimeBase mOnBatteryScreenOffTimeBase = new BatteryStatsImpl.TimeBase();
    private final BatteryStatsImpl.TimeBase mOnBatteryTimeBase = new BatteryStatsImpl.TimeBase();
    /* access modifiers changed from: private */
    public final ArrayList<BatteryStatsImpl.StopwatchTimer> mPartialTimers = new ArrayList<>();
    /* access modifiers changed from: private */
    public int mScreenState = 1;
    /* access modifiers changed from: private */
    public SysEventsHandler mSysEventsHandler = null;
    private final ArrayMap<String, ProcWakeLockTime> mWakeLockTimeMap = new ArrayMap<>();

    private static abstract class OverflowArrayMap<T> {
        private static final String OVERFLOW_NAME = "*overflow*";
        private static final String OVERFLOW_WEIXIN = "WakerLock:overflow";
        int M = 0;
        ArrayMap<String, MutableInt> mActiveOverflow;
        ArrayMap<String, MutableInt> mActiveOverflowWeixin;
        T mCurOverflow;
        T mCurOverflowWeixin;
        final ArrayMap<String, T> mMap = new ArrayMap<>();

        public abstract T instantiateObject();

        public ArrayMap<String, T> getMap() {
            return this.mMap;
        }

        public void clear() {
            this.mMap.clear();
            this.mCurOverflow = null;
            this.mActiveOverflow = null;
            this.mCurOverflowWeixin = null;
            this.mActiveOverflowWeixin = null;
        }

        public void add(String name, T obj) {
            if (name == null) {
                name = BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
            }
            this.mMap.put(name, obj);
            if (OVERFLOW_NAME.equals(name)) {
                this.mCurOverflow = obj;
            } else if (OVERFLOW_WEIXIN.equals(name)) {
                this.mCurOverflowWeixin = obj;
            }
            if (name.startsWith("WakerLock:")) {
                this.M++;
            }
        }

        public void cleanup() {
            if (this.mActiveOverflowWeixin != null && this.mActiveOverflowWeixin.size() == 0) {
                this.mActiveOverflowWeixin = null;
            }
            if (this.mActiveOverflowWeixin == null) {
                if (this.mMap.containsKey(OVERFLOW_WEIXIN)) {
                    Slog.wtf(ProcBatteryStats.TAG, "Cleaning up with no active overflow weixin, but have overflow entry " + this.mMap.get(OVERFLOW_WEIXIN));
                    this.mMap.remove(OVERFLOW_WEIXIN);
                }
                this.mCurOverflowWeixin = null;
            } else if (this.mCurOverflowWeixin == null || !this.mMap.containsKey(OVERFLOW_WEIXIN)) {
                Slog.wtf(ProcBatteryStats.TAG, "Cleaning up with active overflow weixin, but no overflow entry: cur=" + this.mCurOverflowWeixin + " map=" + this.mMap.get(OVERFLOW_WEIXIN));
            }
            if (this.mActiveOverflow != null && this.mActiveOverflow.size() == 0) {
                this.mActiveOverflow = null;
            }
            if (this.mActiveOverflow == null) {
                if (this.mMap.containsKey(OVERFLOW_NAME)) {
                    Slog.wtf(ProcBatteryStats.TAG, "Cleaning up with no active overflow, but have overflow entry " + this.mMap.get(OVERFLOW_NAME));
                    this.mMap.remove(OVERFLOW_NAME);
                }
                this.mCurOverflow = null;
            } else if (this.mCurOverflow == null || !this.mMap.containsKey(OVERFLOW_NAME)) {
                Slog.wtf(ProcBatteryStats.TAG, "Cleaning up with active overflow, but no overflow entry: cur=" + this.mCurOverflow + " map=" + this.mMap.get(OVERFLOW_NAME));
            }
        }

        public T startObject(String name) {
            if (name == null) {
                name = BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
            }
            T obj = this.mMap.get(name);
            if (obj != null) {
                return obj;
            }
            if (this.mActiveOverflowWeixin != null) {
                MutableInt over = this.mActiveOverflowWeixin.get(name);
                if (over != null) {
                    T obj2 = this.mCurOverflowWeixin;
                    if (obj2 == null) {
                        Slog.wtf(ProcBatteryStats.TAG, "Have active overflow " + name + " but null overflow weixin");
                        T instantiateObject = instantiateObject();
                        this.mCurOverflowWeixin = instantiateObject;
                        obj2 = instantiateObject;
                        this.mMap.put(OVERFLOW_WEIXIN, obj2);
                    }
                    over.value++;
                    return obj2;
                }
            }
            if (name.startsWith("WakerLock:")) {
                this.M++;
                if (this.M > 60) {
                    T obj3 = this.mCurOverflowWeixin;
                    if (obj3 == null) {
                        T instantiateObject2 = instantiateObject();
                        this.mCurOverflowWeixin = instantiateObject2;
                        obj3 = instantiateObject2;
                        this.mMap.put(OVERFLOW_WEIXIN, obj3);
                    }
                    if (this.mActiveOverflowWeixin == null) {
                        this.mActiveOverflowWeixin = new ArrayMap<>();
                    }
                    this.mActiveOverflowWeixin.put(name, new MutableInt(1));
                    return obj3;
                }
            }
            if (this.mActiveOverflow != null) {
                MutableInt over2 = this.mActiveOverflow.get(name);
                if (over2 != null) {
                    T obj4 = this.mCurOverflow;
                    if (obj4 == null) {
                        Slog.wtf(ProcBatteryStats.TAG, "Have active overflow " + name + " but null overflow");
                        T instantiateObject3 = instantiateObject();
                        this.mCurOverflow = instantiateObject3;
                        obj4 = instantiateObject3;
                        this.mMap.put(OVERFLOW_NAME, obj4);
                    }
                    over2.value++;
                    return obj4;
                }
            }
            if (this.mMap.size() >= 100) {
                Slog.i(ProcBatteryStats.TAG, "wakelocks more than 100, name: " + name);
                T obj5 = this.mCurOverflow;
                if (obj5 == null) {
                    T instantiateObject4 = instantiateObject();
                    this.mCurOverflow = instantiateObject4;
                    obj5 = instantiateObject4;
                    this.mMap.put(OVERFLOW_NAME, obj5);
                }
                if (this.mActiveOverflow == null) {
                    this.mActiveOverflow = new ArrayMap<>();
                }
                this.mActiveOverflow.put(name, new MutableInt(1));
                return obj5;
            }
            T obj6 = instantiateObject();
            this.mMap.put(name, obj6);
            return obj6;
        }

        public T stopObject(String name) {
            if (name == null) {
                name = BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
            }
            T obj = this.mMap.get(name);
            if (obj != null) {
                return obj;
            }
            if (this.mActiveOverflowWeixin != null) {
                MutableInt over = this.mActiveOverflowWeixin.get(name);
                if (over != null) {
                    T obj2 = this.mCurOverflowWeixin;
                    if (obj2 != null) {
                        over.value--;
                        if (over.value <= 0) {
                            this.mActiveOverflowWeixin.remove(name);
                        }
                        return obj2;
                    }
                }
            }
            if (this.mActiveOverflow != null) {
                MutableInt over2 = this.mActiveOverflow.get(name);
                if (over2 != null) {
                    T obj3 = this.mCurOverflow;
                    if (obj3 != null) {
                        over2.value--;
                        if (over2.value <= 0) {
                            this.mActiveOverflow.remove(name);
                        }
                        return obj3;
                    }
                }
            }
            Slog.wtf(ProcBatteryStats.TAG, "Unable to find object for " + name + " mapsize=" + this.mMap.size() + " activeoverflow=" + this.mActiveOverflow + " curoverflow=" + this.mCurOverflow);
            return null;
        }
    }

    private final class ProcWakeLockTime {
        String mProcName;
        int mUid;
        OverflowArrayMap<WakeLock> mWakelockStats = new OverflowArrayMap<WakeLock>() {
            public WakeLock instantiateObject() {
                return new WakeLock();
            }
        };

        public ProcWakeLockTime(String name, int uid) {
            this.mProcName = name;
            this.mUid = uid;
        }

        /* access modifiers changed from: private */
        public OverflowArrayMap<WakeLock> getWakeLockStats() {
            return this.mWakelockStats;
        }

        /* access modifiers changed from: private */
        public boolean reset() {
            if (ProcBatteryStats.this.DEBUG_WAKELOCK) {
                Slog.d(ProcBatteryStats.TAG, "pwt, reset, mProcName: " + this.mProcName + ", mUid: " + this.mUid);
            }
            boolean active = false;
            ArrayMap<String, WakeLock> wakeStats = this.mWakelockStats.getMap();
            for (int iw = wakeStats.size() - 1; iw >= 0; iw--) {
                if (wakeStats.valueAt(iw).reset()) {
                    wakeStats.removeAt(iw);
                } else {
                    active = true;
                }
            }
            this.mWakelockStats.cleanup();
            if (!active) {
                return true;
            }
            return false;
        }
    }

    private final class SysEventsHandler extends Handler {
        public SysEventsHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            boolean screenOff = ProcBatteryStats.this.mScreenState == 0;
            Intent intent = (Intent) msg.obj;
            int i = -1;
            switch (msg.what) {
                case 1:
                    if (ProcBatteryStats.this.DEBUG_COMMON) {
                        Slog.i(ProcBatteryStats.TAG, "SYS_EVENT_POWER_CONNECTED");
                    }
                    ProcBatteryStats.this.updateTimeBases(false, screenOff);
                    return;
                case 2:
                    if (ProcBatteryStats.this.DEBUG_COMMON) {
                        Slog.i(ProcBatteryStats.TAG, "SYS_EVENT_POWER_DISCONNECTED");
                    }
                    ProcBatteryStats.this.updateTimeBases(true, screenOff);
                    return;
                case 3:
                    int userId = intent.getIntExtra("android.intent.extra.user_handle", -1);
                    ProcBatteryStats.this.removeWlByUserId(userId);
                    if (ProcBatteryStats.this.DEBUG_COMMON) {
                        Slog.i(ProcBatteryStats.TAG, "SYS_EVENT_USER_REMOVED, userId: " + userId);
                        return;
                    }
                    return;
                case 4:
                    Bundle intentExtras = intent.getExtras();
                    if (intentExtras != null) {
                        i = intentExtras.getInt("android.intent.extra.UID");
                    }
                    int uid = i;
                    ProcBatteryStats.this.removeWlByUid(uid);
                    if (ProcBatteryStats.this.DEBUG_COMMON) {
                        Slog.i(ProcBatteryStats.TAG, "SYS_EVENT_UID_REMOVED, uid: " + uid);
                        return;
                    }
                    return;
                case 5:
                    ProcBatteryStats.this.noteScreenState(1);
                    if (ProcBatteryStats.this.DEBUG_COMMON) {
                        Slog.i(ProcBatteryStats.TAG, "SYS_EVENT_SCREEN_ON");
                        return;
                    }
                    return;
                case 6:
                    ProcBatteryStats.this.noteScreenState(0);
                    if (ProcBatteryStats.this.DEBUG_COMMON) {
                        Slog.i(ProcBatteryStats.TAG, "SYS_EVENT_SCREEN_OFF");
                        return;
                    }
                    return;
                default:
                    Slog.w(ProcBatteryStats.TAG, "unexpected sysEvent: " + msg.what);
                    return;
            }
        }
    }

    private final class WakeLock {
        long mCounter;
        BatteryStatsImpl.StopwatchTimer mTimerPartial;

        private WakeLock() {
            this.mCounter = 0;
            BatteryStatsImpl.StopwatchTimer stopwatchTimer = new BatteryStatsImpl.StopwatchTimer(ProcBatteryStats.this.mClocks, null, 0, ProcBatteryStats.this.mPartialTimers, ProcBatteryStats.this.mOnBatteryScreenOffTimeBase);
            this.mTimerPartial = stopwatchTimer;
        }

        /* access modifiers changed from: private */
        public void startRunning(long realtime) {
            this.mTimerPartial.startRunningLocked(realtime);
            this.mCounter++;
        }

        /* access modifiers changed from: private */
        public void stopRunning(long realtime) {
            this.mTimerPartial.stopRunningLocked(realtime);
        }

        /* access modifiers changed from: private */
        public long getTotalTime(long realtime) {
            return this.mTimerPartial.getTotalTimeLocked(1000 * realtime, 0);
        }

        /* access modifiers changed from: private */
        public BatteryStatsImpl.StopwatchTimer getStopwatchTimer() {
            return this.mTimerPartial;
        }

        /* access modifiers changed from: package-private */
        public boolean reset() {
            this.mCounter = 0;
            if (!this.mTimerPartial.reset(false)) {
                return false;
            }
            this.mTimerPartial.detach();
            return true;
        }
    }

    public ProcBatteryStats(Context context) {
        this.mContext = context;
        initDebugSwitches();
        initTimeBases();
    }

    private void initDebugSwitches() {
        boolean z = false;
        int prop = SystemProperties.getInt("persist.procbatterystats.debug", 0);
        this.DEBUG_COMMON = (prop & 1) != 0;
        this.DEBUG_WAKELOCK = (prop & 2) != 0;
        if ((prop & 4) != 0) {
            z = true;
        }
        this.DEBUG_SCREENON = z;
        Slog.d(TAG, "persist.procbatterystats.debug: " + prop);
    }

    private void initTimeBases() {
        long realtimeUs = SystemClock.elapsedRealtime() * 1000;
        long uptimeUs = SystemClock.uptimeMillis() * 1000;
        this.mOnBatteryTimeBase.init(uptimeUs, realtimeUs);
        this.mOnBatteryScreenOffTimeBase.init(uptimeUs, realtimeUs);
    }

    /* access modifiers changed from: private */
    public void updateTimeBases(boolean unplugged, boolean screenOff) {
        if (this.DEBUG_COMMON) {
            Slog.d(TAG, "updateTimeBases, unplugged: " + unplugged + ", screenOff: " + screenOff);
        }
        long realtimeUs = SystemClock.elapsedRealtime() * 1000;
        long uptimeUs = 1000 * SystemClock.uptimeMillis();
        this.mOnBatteryTimeBase.setRunning(unplugged, uptimeUs, realtimeUs);
        synchronized (this.mWakeLockTimeMap) {
            this.mOnBatteryScreenOffTimeBase.setRunning(unplugged && screenOff, uptimeUs, realtimeUs);
        }
    }

    public void onSystemReady() {
        startSystemEventHandleThread();
        Slog.i(TAG, "ProcBatteryStats--systemReady");
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        switch (code) {
            case 101:
                data.enforceInterface(DESCRIPTOR);
                List<String> stats = getPowerStats(data.readInt());
                reply.writeNoException();
                reply.writeStringList(stats);
                return true;
            case 102:
                data.enforceInterface(DESCRIPTOR);
                noteResetAllProcInfo();
                reply.writeNoException();
                return true;
            default:
                return false;
        }
    }

    private void startSystemEventHandleThread() {
        HandlerThread sysHandlerthread = new HandlerThread("PgmsEventsHandler", 10);
        sysHandlerthread.start();
        this.mSysEventsHandler = new SysEventsHandler(sysHandlerthread.getLooper());
        registerBroadcast();
    }

    private void registerBroadcast() {
        if (this.mContext == null) {
            Slog.w(TAG, "null mContext!");
            return;
        }
        Slog.i(TAG, "ProcBatteryStats--registerBroadcast");
        this.mActionIdMap.put("android.intent.action.ACTION_POWER_CONNECTED", 1);
        this.mActionIdMap.put("android.intent.action.ACTION_POWER_DISCONNECTED", 2);
        this.mActionIdMap.put("android.intent.action.USER_REMOVED", 3);
        this.mActionIdMap.put("android.intent.action.UID_REMOVED", 4);
        this.mActionIdMap.put("android.intent.action.SCREEN_ON", 5);
        this.mActionIdMap.put("android.intent.action.SCREEN_OFF", 6);
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.ACTION_POWER_CONNECTED");
        filter.addAction("android.intent.action.ACTION_POWER_DISCONNECTED");
        filter.addAction("android.intent.action.USER_REMOVED");
        filter.addAction("android.intent.action.UID_REMOVED");
        filter.addAction("android.intent.action.SCREEN_ON");
        filter.addAction("android.intent.action.SCREEN_OFF");
        this.mContext.registerReceiverAsUser(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent == null) {
                    if (ProcBatteryStats.this.DEBUG_COMMON) {
                        Slog.d(ProcBatteryStats.TAG, "null intent");
                    }
                    return;
                }
                Message msg = ProcBatteryStats.this.mSysEventsHandler.obtainMessage(((Integer) ProcBatteryStats.this.mActionIdMap.get(intent.getAction())).intValue());
                msg.obj = intent;
                ProcBatteryStats.this.mSysEventsHandler.sendMessageDelayed(msg, 0);
            }
        }, UserHandle.ALL, filter, null, null);
    }

    public void noteResetAllProcInfo() {
        Slog.d(TAG, "noteResetAllProcInfo");
        initTimeBases();
        resetWakeLockTime();
    }

    public List<String> getPowerStats(int type) {
        if (this.DEBUG_COMMON) {
            Slog.d(TAG, "getPowerStats, type: " + type);
        }
        if (type == 0) {
            return getWakeLockStatsList();
        }
        Slog.w(TAG, "unexpected Type: " + type);
        return null;
    }

    public void noteScreenState(int state) {
        if (this.DEBUG_SCREENON) {
            Slog.d(TAG, "screenState, " + this.mScreenState + " -> " + state);
        }
        if (this.mScreenState != state) {
            this.mScreenState = state;
            if (state == 1) {
                updateTimeBases(this.mOnBatteryTimeBase.isRunning(), false);
            } else if (state == 0) {
                updateTimeBases(this.mOnBatteryTimeBase.isRunning(), true);
            }
        }
    }

    public void processWakeLock(int event, String tag, WorkSource ws, String pkgName, int uid) {
        int uid2;
        String str;
        int i;
        ArrayMap<String, ProcWakeLockTime> arrayMap;
        WorkSource workSource = ws;
        String pkgName2 = pkgName;
        if (this.DEBUG_WAKELOCK) {
            StringBuilder sb = new StringBuilder();
            sb.append("processWlInfo, event: ");
            i = event;
            sb.append(i);
            sb.append(", tag: ");
            str = tag;
            sb.append(str);
            sb.append(", ws: ");
            sb.append(workSource);
            sb.append(", pkgName: ");
            sb.append(pkgName2);
            sb.append(", uid: ");
            uid2 = uid;
            sb.append(uid2);
            Slog.d(TAG, sb.toString());
        } else {
            i = event;
            str = tag;
            uid2 = uid;
        }
        long realtime = SystemClock.elapsedRealtime();
        if (pkgName2 == null) {
            pkgName2 = BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
            Slog.w(TAG, "name null");
        }
        String pkgName3 = pkgName2;
        ArrayMap<String, ProcWakeLockTime> arrayMap2 = this.mWakeLockTimeMap;
        synchronized (arrayMap2) {
            if (workSource == null) {
                arrayMap = arrayMap2;
                try {
                    updateWlTimer(i, str, pkgName3, uid2, realtime);
                } catch (Throwable th) {
                    th = th;
                    int i2 = uid2;
                    throw th;
                }
            } else {
                arrayMap = arrayMap2;
                int length = ws.size();
                int i3 = 0;
                int i4 = 0;
                while (true) {
                    int i5 = i4;
                    if (i5 < length) {
                        int uid3 = workSource.get(i5);
                        try {
                            String wsName = workSource.getName(i5);
                            if (wsName == null) {
                                wsName = BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
                            } else if (wsName.indexOf(58) > 0) {
                                wsName = wsName.substring(i3, wsName.indexOf(58));
                            }
                            int i6 = i3;
                            int i7 = i5;
                            updateWlTimer(i, str, wsName, uid3, realtime);
                            i4 = i7 + 1;
                            uid2 = uid3;
                            i3 = i6;
                        } catch (Throwable th2) {
                            th = th2;
                            throw th;
                        }
                    }
                }
            }
        }
    }

    private void updateWlTimer(int event, String tag, String name, int uid, long realtime) {
        OverflowArrayMap<WakeLock> wakelockStats = getProcWakeLockTime(name, uid).getWakeLockStats();
        if (event == 160) {
            WakeLock wl = wakelockStats.startObject(tag);
            if (wl == null) {
                return;
            }
            if (!wl.getStopwatchTimer().isRunningLocked()) {
                if (this.DEBUG_WAKELOCK) {
                    Slog.d(TAG, "startRunWlTimer, name: " + name + ", uid: " + uid + ", tag: " + tag);
                }
                wl.startRunning(realtime);
                return;
            }
            Slog.w(TAG, "timer is running , not start, name: " + name + ", uid: " + uid + ", tag: " + tag);
        } else if (event == 161) {
            WakeLock wl2 = wakelockStats.stopObject(tag);
            if (wl2 == null) {
                return;
            }
            if (wl2.getStopwatchTimer().isRunningLocked()) {
                if (this.DEBUG_WAKELOCK) {
                    Slog.d(TAG, "stopRunWlTimer, name: " + name + ", uid: " + uid + ", tag: " + tag);
                }
                wl2.stopRunning(realtime);
                return;
            }
            Slog.w(TAG, "timer is not running , not stop, name: " + name + ", uid: " + uid + ", tag: " + tag);
        }
    }

    private ProcWakeLockTime getProcWakeLockTime(String procName, int uid) {
        String key = procName + uid;
        if (this.mWakeLockTimeMap.containsKey(key)) {
            return this.mWakeLockTimeMap.get(key);
        }
        ProcWakeLockTime pwt = new ProcWakeLockTime(procName, uid);
        this.mWakeLockTimeMap.put(key, pwt);
        if (!this.DEBUG_WAKELOCK) {
            return pwt;
        }
        Slog.d(TAG, "getProcWakeLockTime, procName: " + procName + ", uid: " + uid + ", size: " + this.mWakeLockTimeMap.size());
        return pwt;
    }

    private void resetWakeLockTime() {
        synchronized (this.mWakeLockTimeMap) {
            for (int i = this.mWakeLockTimeMap.size() - 1; i >= 0; i--) {
                if (this.mWakeLockTimeMap.valueAt(i).reset()) {
                    this.mWakeLockTimeMap.removeAt(i);
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void getWlBatteryStats(List<String> list) {
        long realtime = SystemClock.elapsedRealtime();
        synchronized (this.mWakeLockTimeMap) {
            try {
                int size = this.mWakeLockTimeMap.size();
                if (this.DEBUG_WAKELOCK) {
                    Slog.d(TAG, "getWlBatteryStats, size: " + size);
                }
                for (int i = size - 1; i >= 0; i--) {
                    long totalTimeMs = 0;
                    StringBuilder sb = new StringBuilder(128);
                    ProcWakeLockTime pwt = this.mWakeLockTimeMap.valueAt(i);
                    ArrayMap<String, WakeLock> wakeStats = pwt.getWakeLockStats().getMap();
                    for (int iw = wakeStats.size() - 1; iw >= 0; iw--) {
                        totalTimeMs += wakeStats.valueAt(iw).getTotalTime(realtime) / 1000;
                    }
                    if (totalTimeMs < 100) {
                        if (this.DEBUG_WAKELOCK) {
                            Slog.d(TAG, "small time, procName: " + pwt.mProcName + ", uid: " + pwt.mUid + ", timeMs: " + totalTimeMs);
                        }
                        List<String> list2 = list;
                    } else {
                        if (this.DEBUG_WAKELOCK) {
                            Slog.d(TAG, "big time, procName: " + pwt.mProcName + ", uid: " + pwt.mUid + ", timeMs: " + totalTimeMs);
                        }
                        sb.append(pwt.mProcName);
                        sb.append('%');
                        sb.append(pwt.mUid);
                        sb.append('%');
                        sb.append(totalTimeMs);
                        list.add(sb.toString());
                    }
                }
                List<String> list3 = list;
            } catch (Throwable th) {
                th = th;
                throw th;
            }
        }
    }

    private List<String> getWakeLockStatsList() {
        ProcBatteryStats procBatteryStats = this;
        List<String> stats = new ArrayList<>();
        long realtime = SystemClock.elapsedRealtime();
        synchronized (procBatteryStats.mWakeLockTimeMap) {
            int i = procBatteryStats.mWakeLockTimeMap.size() - 1;
            while (i >= 0) {
                ProcWakeLockTime pwt = procBatteryStats.mWakeLockTimeMap.valueAt(i);
                String procName = pwt.mProcName;
                ArrayMap<String, WakeLock> wakeStats = pwt.getWakeLockStats().getMap();
                int iw = wakeStats.size() - 1;
                while (iw >= 0) {
                    String tag = wakeStats.keyAt(iw);
                    long totalTimeMs = wakeStats.valueAt(iw).getTotalTime(realtime) / 1000;
                    if (totalTimeMs > 0) {
                        StringBuilder sb = new StringBuilder(128);
                        sb.append("uid=");
                        sb.append(pwt.mUid);
                        sb.append(" prevent_time=");
                        sb.append(totalTimeMs);
                        sb.append(" ws_name=");
                        sb.append(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS.equals(procName) ? BluetoothManagerService.DEFAULT_PACKAGE_NAME : procName);
                        sb.append(" tag=");
                        sb.append(tag);
                        stats.add(sb.toString());
                        if (procBatteryStats.DEBUG_WAKELOCK) {
                            Slog.d(TAG, sb.toString());
                        }
                    }
                    iw--;
                    procBatteryStats = this;
                }
                i--;
                procBatteryStats = this;
            }
        }
        return stats;
    }

    /* access modifiers changed from: private */
    public void removeWlByUid(int uid) {
        synchronized (this.mWakeLockTimeMap) {
            for (int i = this.mWakeLockTimeMap.size() - 1; i >= 0; i--) {
                if (this.mWakeLockTimeMap.valueAt(i).mUid == uid) {
                    this.mWakeLockTimeMap.removeAt(i);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void removeWlByUserId(int userId) {
        synchronized (this.mWakeLockTimeMap) {
            for (int i = this.mWakeLockTimeMap.size() - 1; i >= 0; i--) {
                if (userId == UserHandle.getUserId(this.mWakeLockTimeMap.valueAt(i).mUid)) {
                    this.mWakeLockTimeMap.removeAt(i);
                }
            }
        }
    }
}
