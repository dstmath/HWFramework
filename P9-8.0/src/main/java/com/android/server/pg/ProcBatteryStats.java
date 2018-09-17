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
import com.android.internal.os.BatteryStatsImpl.Clocks;
import com.android.internal.os.BatteryStatsImpl.StopwatchTimer;
import com.android.internal.os.BatteryStatsImpl.SystemClocks;
import com.android.internal.os.BatteryStatsImpl.TimeBase;
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
    private boolean DEBUG_COMMON = false;
    private boolean DEBUG_SCREENON = false;
    private boolean DEBUG_WAKELOCK = false;
    private final ArrayMap<String, Integer> mActionIdMap = new ArrayMap();
    private Clocks mClocks = new SystemClocks();
    private Context mContext = null;
    private final TimeBase mOnBatteryScreenOffTimeBase = new TimeBase();
    private final TimeBase mOnBatteryTimeBase = new TimeBase();
    private final ArrayList<StopwatchTimer> mPartialTimers = new ArrayList();
    private int mScreenState = 1;
    private SysEventsHandler mSysEventsHandler = null;
    private final ArrayMap<String, ProcWakeLockTime> mWakeLockTimeMap = new ArrayMap();

    private static abstract class OverflowArrayMap<T> {
        private static final String OVERFLOW_NAME = "*overflow*";
        private static final String OVERFLOW_WEIXIN = "WakerLock:overflow";
        int M = 0;
        ArrayMap<String, MutableInt> mActiveOverflow;
        ArrayMap<String, MutableInt> mActiveOverflowWeixin;
        T mCurOverflow;
        T mCurOverflowWeixin;
        final ArrayMap<String, T> mMap = new ArrayMap();

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
                name = "";
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
            } else if (this.mCurOverflowWeixin == null || (this.mMap.containsKey(OVERFLOW_WEIXIN) ^ 1) != 0) {
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
            } else if (this.mCurOverflow == null || (this.mMap.containsKey(OVERFLOW_NAME) ^ 1) != 0) {
                Slog.wtf(ProcBatteryStats.TAG, "Cleaning up with active overflow, but no overflow entry: cur=" + this.mCurOverflow + " map=" + this.mMap.get(OVERFLOW_NAME));
            }
        }

        public T startObject(String name) {
            if (name == null) {
                name = "";
            }
            T obj = this.mMap.get(name);
            if (obj != null) {
                return obj;
            }
            MutableInt over;
            if (this.mActiveOverflowWeixin != null) {
                over = (MutableInt) this.mActiveOverflowWeixin.get(name);
                if (over != null) {
                    obj = this.mCurOverflowWeixin;
                    if (obj == null) {
                        Slog.wtf(ProcBatteryStats.TAG, "Have active overflow " + name + " but null overflow weixin");
                        obj = instantiateObject();
                        this.mCurOverflowWeixin = obj;
                        this.mMap.put(OVERFLOW_WEIXIN, obj);
                    }
                    over.value++;
                    return obj;
                }
            }
            if (name.startsWith("WakerLock:")) {
                this.M++;
                if (this.M > 60) {
                    obj = this.mCurOverflowWeixin;
                    if (obj == null) {
                        obj = instantiateObject();
                        this.mCurOverflowWeixin = obj;
                        this.mMap.put(OVERFLOW_WEIXIN, obj);
                    }
                    if (this.mActiveOverflowWeixin == null) {
                        this.mActiveOverflowWeixin = new ArrayMap();
                    }
                    this.mActiveOverflowWeixin.put(name, new MutableInt(1));
                    return obj;
                }
            }
            if (this.mActiveOverflow != null) {
                over = (MutableInt) this.mActiveOverflow.get(name);
                if (over != null) {
                    obj = this.mCurOverflow;
                    if (obj == null) {
                        Slog.wtf(ProcBatteryStats.TAG, "Have active overflow " + name + " but null overflow");
                        obj = instantiateObject();
                        this.mCurOverflow = obj;
                        this.mMap.put(OVERFLOW_NAME, obj);
                    }
                    over.value++;
                    return obj;
                }
            }
            if (this.mMap.size() >= 100) {
                Slog.i(ProcBatteryStats.TAG, "wakelocks more than 100, name: " + name);
                obj = this.mCurOverflow;
                if (obj == null) {
                    obj = instantiateObject();
                    this.mCurOverflow = obj;
                    this.mMap.put(OVERFLOW_NAME, obj);
                }
                if (this.mActiveOverflow == null) {
                    this.mActiveOverflow = new ArrayMap();
                }
                this.mActiveOverflow.put(name, new MutableInt(1));
                return obj;
            }
            obj = instantiateObject();
            this.mMap.put(name, obj);
            return obj;
        }

        public T stopObject(String name) {
            if (name == null) {
                name = "";
            }
            T obj = this.mMap.get(name);
            if (obj != null) {
                return obj;
            }
            MutableInt over;
            if (this.mActiveOverflowWeixin != null) {
                over = (MutableInt) this.mActiveOverflowWeixin.get(name);
                if (over != null) {
                    obj = this.mCurOverflowWeixin;
                    if (obj != null) {
                        over.value--;
                        if (over.value <= 0) {
                            this.mActiveOverflowWeixin.remove(name);
                        }
                        return obj;
                    }
                }
            }
            if (this.mActiveOverflow != null) {
                over = (MutableInt) this.mActiveOverflow.get(name);
                if (over != null) {
                    obj = this.mCurOverflow;
                    if (obj != null) {
                        over.value--;
                        if (over.value <= 0) {
                            this.mActiveOverflow.remove(name);
                        }
                        return obj;
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
                return new WakeLock(ProcBatteryStats.this, null);
            }
        };

        public ProcWakeLockTime(String name, int uid) {
            this.mProcName = name;
            this.mUid = uid;
        }

        private OverflowArrayMap<WakeLock> getWakeLockStats() {
            return this.mWakelockStats;
        }

        private boolean reset() {
            if (ProcBatteryStats.this.DEBUG_WAKELOCK) {
                Slog.d(ProcBatteryStats.TAG, "pwt, reset, mProcName: " + this.mProcName + ", mUid: " + this.mUid);
            }
            boolean active = false;
            ArrayMap<String, WakeLock> wakeStats = this.mWakelockStats.getMap();
            for (int iw = wakeStats.size() - 1; iw >= 0; iw--) {
                if (((WakeLock) wakeStats.valueAt(iw)).reset()) {
                    wakeStats.removeAt(iw);
                } else {
                    active = true;
                }
            }
            this.mWakelockStats.cleanup();
            return active ^ 1;
        }
    }

    private final class SysEventsHandler extends Handler {
        public SysEventsHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            boolean screenOff = ProcBatteryStats.this.mScreenState == 0;
            Intent intent = msg.obj;
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
                    int uid = intentExtras != null ? intentExtras.getInt("android.intent.extra.UID") : -1;
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
        StopwatchTimer mTimerPartial;

        /* synthetic */ WakeLock(ProcBatteryStats this$0, WakeLock -this1) {
            this();
        }

        private WakeLock() {
            this.mCounter = 0;
            this.mTimerPartial = new StopwatchTimer(ProcBatteryStats.this.mClocks, null, 0, ProcBatteryStats.this.mPartialTimers, ProcBatteryStats.this.mOnBatteryScreenOffTimeBase);
        }

        private void startRunning(long realtime) {
            this.mTimerPartial.startRunningLocked(realtime);
            this.mCounter++;
        }

        private void stopRunning(long realtime) {
            this.mTimerPartial.stopRunningLocked(realtime);
        }

        private long getTotalTime(long realtime) {
            return this.mTimerPartial.getTotalTimeLocked(1000 * realtime, 0);
        }

        private StopwatchTimer getStopwatchTimer() {
            return this.mTimerPartial;
        }

        boolean reset() {
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
        boolean z;
        boolean z2 = true;
        int prop = SystemProperties.getInt("persist.procbatterystats.debug", 0);
        if ((prop & 1) != 0) {
            z = true;
        } else {
            z = false;
        }
        this.DEBUG_COMMON = z;
        if ((prop & 2) != 0) {
            z = true;
        } else {
            z = false;
        }
        this.DEBUG_WAKELOCK = z;
        if ((prop & 4) == 0) {
            z2 = false;
        }
        this.DEBUG_SCREENON = z2;
        Slog.d(TAG, "persist.procbatterystats.debug: " + prop);
    }

    private void initTimeBases() {
        long realtimeUs = SystemClock.elapsedRealtime() * 1000;
        long uptimeUs = SystemClock.uptimeMillis() * 1000;
        this.mOnBatteryTimeBase.init(uptimeUs, realtimeUs);
        this.mOnBatteryScreenOffTimeBase.init(uptimeUs, realtimeUs);
    }

    private void updateTimeBases(boolean unplugged, boolean screenOff) {
        if (this.DEBUG_COMMON) {
            Slog.d(TAG, "updateTimeBases, unplugged: " + unplugged + ", screenOff: " + screenOff);
        }
        long realtimeUs = SystemClock.elapsedRealtime() * 1000;
        long uptimeUs = SystemClock.uptimeMillis() * 1000;
        this.mOnBatteryTimeBase.setRunning(unplugged, uptimeUs, realtimeUs);
        this.mOnBatteryScreenOffTimeBase.setRunning(unplugged ? screenOff : false, uptimeUs, realtimeUs);
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
        this.mActionIdMap.put("android.intent.action.ACTION_POWER_CONNECTED", Integer.valueOf(1));
        this.mActionIdMap.put("android.intent.action.ACTION_POWER_DISCONNECTED", Integer.valueOf(2));
        this.mActionIdMap.put("android.intent.action.USER_REMOVED", Integer.valueOf(3));
        this.mActionIdMap.put("android.intent.action.UID_REMOVED", Integer.valueOf(4));
        this.mActionIdMap.put("android.intent.action.SCREEN_ON", Integer.valueOf(5));
        this.mActionIdMap.put("android.intent.action.SCREEN_OFF", Integer.valueOf(6));
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.ACTION_POWER_CONNECTED");
        filter.addAction("android.intent.action.ACTION_POWER_DISCONNECTED");
        filter.addAction("android.intent.action.USER_REMOVED");
        filter.addAction("android.intent.action.UID_REMOVED");
        filter.addAction("android.intent.action.SCREEN_ON");
        filter.addAction("android.intent.action.SCREEN_OFF");
        this.mContext.registerReceiver(new BroadcastReceiver() {
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
        }, filter);
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
        if (this.DEBUG_WAKELOCK) {
            Slog.d(TAG, "processWlInfo, event: " + event + ", tag: " + tag + ", ws: " + ws + ", pkgName: " + pkgName + ", uid: " + uid);
        }
        long realtime = SystemClock.elapsedRealtime();
        if (pkgName == null) {
            pkgName = "";
            Slog.w(TAG, "name null");
        }
        synchronized (this.mWakeLockTimeMap) {
            if (ws == null) {
                updateWlTimer(event, tag, pkgName, uid, realtime);
            } else {
                int length = ws.size();
                for (int i = 0; i < length; i++) {
                    updateWlTimer(event, tag, "", ws.get(i), realtime);
                }
            }
        }
    }

    private void updateWlTimer(int event, String tag, String name, int uid, long realtime) {
        OverflowArrayMap<WakeLock> wakelockStats = getProcWakeLockTime(name, uid).getWakeLockStats();
        WakeLock wl;
        if (event == 160) {
            wl = (WakeLock) wakelockStats.startObject(tag);
            if (wl == null) {
                return;
            }
            if (wl.getStopwatchTimer().isRunningLocked()) {
                Slog.w(TAG, "timer is running , not start, name: " + name + ", uid: " + uid + ", tag: " + tag);
                return;
            }
            if (this.DEBUG_WAKELOCK) {
                Slog.d(TAG, "startRunWlTimer, name: " + name + ", uid: " + uid + ", tag: " + tag);
            }
            wl.startRunning(realtime);
        } else if (event == 161) {
            wl = (WakeLock) wakelockStats.stopObject(tag);
            if (wl == null) {
                return;
            }
            if (wl.getStopwatchTimer().isRunningLocked()) {
                if (this.DEBUG_WAKELOCK) {
                    Slog.d(TAG, "stopRunWlTimer, name: " + name + ", uid: " + uid + ", tag: " + tag);
                }
                wl.stopRunning(realtime);
                return;
            }
            Slog.w(TAG, "timer is not running , not stop, name: " + name + ", uid: " + uid + ", tag: " + tag);
        }
    }

    private ProcWakeLockTime getProcWakeLockTime(String procName, int uid) {
        String key = procName + uid;
        if (this.mWakeLockTimeMap.containsKey(key)) {
            return (ProcWakeLockTime) this.mWakeLockTimeMap.get(key);
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
                if (((ProcWakeLockTime) this.mWakeLockTimeMap.valueAt(i)).reset()) {
                    this.mWakeLockTimeMap.removeAt(i);
                }
            }
        }
    }

    protected void getWlBatteryStats(List<String> list) {
        long realtime = SystemClock.elapsedRealtime();
        int size = this.mWakeLockTimeMap.size();
        if (this.DEBUG_WAKELOCK) {
            Slog.d(TAG, "getWlBatteryStats, size: " + size);
        }
        synchronized (this.mWakeLockTimeMap) {
            for (int i = size - 1; i >= 0; i--) {
                long totalTimeMs = 0;
                StringBuilder sb = new StringBuilder(128);
                ProcWakeLockTime pwt = (ProcWakeLockTime) this.mWakeLockTimeMap.valueAt(i);
                ArrayMap<String, WakeLock> wakeStats = pwt.getWakeLockStats().getMap();
                for (int iw = wakeStats.size() - 1; iw >= 0; iw--) {
                    totalTimeMs += ((WakeLock) wakeStats.valueAt(iw)).getTotalTime(realtime) / 1000;
                }
                if (totalTimeMs >= 100) {
                    if (this.DEBUG_WAKELOCK) {
                        Slog.d(TAG, "big time, procName: " + pwt.mProcName + ", uid: " + pwt.mUid + ", timeMs: " + totalTimeMs);
                    }
                    sb.append(pwt.mProcName);
                    sb.append('%');
                    sb.append(pwt.mUid);
                    sb.append('%');
                    sb.append(totalTimeMs);
                    list.add(sb.toString());
                } else if (this.DEBUG_WAKELOCK) {
                    Slog.d(TAG, "small time, procName: " + pwt.mProcName + ", uid: " + pwt.mUid + ", timeMs: " + totalTimeMs);
                }
            }
        }
    }

    private List<String> getWakeLockStatsList() {
        List<String> stats = new ArrayList();
        long realtime = SystemClock.elapsedRealtime();
        synchronized (this.mWakeLockTimeMap) {
            for (int i = this.mWakeLockTimeMap.size() - 1; i >= 0; i--) {
                ProcWakeLockTime pwt = (ProcWakeLockTime) this.mWakeLockTimeMap.valueAt(i);
                String procName = pwt.mProcName;
                ArrayMap<String, WakeLock> wakeStats = pwt.getWakeLockStats().getMap();
                for (int iw = wakeStats.size() - 1; iw >= 0; iw--) {
                    String tag = (String) wakeStats.keyAt(iw);
                    long totalTimeMs = ((WakeLock) wakeStats.valueAt(iw)).getTotalTime(realtime) / 1000;
                    if (totalTimeMs > 0) {
                        String str;
                        StringBuilder sb = new StringBuilder(128);
                        sb.append("uid=");
                        sb.append(pwt.mUid);
                        sb.append(" prevent_time=");
                        sb.append(totalTimeMs);
                        sb.append(" ws_name=");
                        if ("".equals(procName)) {
                            str = "NULL";
                        } else {
                            str = procName;
                        }
                        sb.append(str);
                        sb.append(" tag=");
                        sb.append(tag);
                        stats.add(sb.toString());
                        if (this.DEBUG_WAKELOCK) {
                            Slog.d(TAG, sb.toString());
                        }
                    }
                }
            }
        }
        return stats;
    }

    private void removeWlByUid(int uid) {
        synchronized (this.mWakeLockTimeMap) {
            for (int i = this.mWakeLockTimeMap.size() - 1; i >= 0; i--) {
                if (((ProcWakeLockTime) this.mWakeLockTimeMap.valueAt(i)).mUid == uid) {
                    this.mWakeLockTimeMap.removeAt(i);
                }
            }
        }
    }

    private void removeWlByUserId(int userId) {
        synchronized (this.mWakeLockTimeMap) {
            for (int i = this.mWakeLockTimeMap.size() - 1; i >= 0; i--) {
                if (userId == UserHandle.getUserId(((ProcWakeLockTime) this.mWakeLockTimeMap.valueAt(i)).mUid)) {
                    this.mWakeLockTimeMap.removeAt(i);
                }
            }
        }
    }
}
