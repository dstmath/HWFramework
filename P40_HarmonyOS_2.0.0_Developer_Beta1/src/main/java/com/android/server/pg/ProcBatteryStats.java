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
    private boolean debugCommon = false;
    private boolean debugScreenon = false;
    private boolean debugWakelock = false;
    private final ArrayMap<String, Integer> mActionIdMap = new ArrayMap<>();
    private final BatteryStatsImpl.Clocks mClocks = new BatteryStatsImpl.SystemClocks();
    private Context mContext = null;
    private final BatteryStatsImpl.TimeBase mOnBatteryScreenOffTimeBase = new BatteryStatsImpl.TimeBase();
    private final BatteryStatsImpl.TimeBase mOnBatteryTimeBase = new BatteryStatsImpl.TimeBase();
    private final ArrayList<BatteryStatsImpl.StopwatchTimer> mPartialTimers = new ArrayList<>();
    private int mScreenState = 1;
    private SysEventsHandler mSysEventsHandler = null;
    private final ArrayMap<String, ProcWakeLockTime> mWakeLockTimeMap = new ArrayMap<>();

    public ProcBatteryStats(Context context) {
        this.mContext = context;
        initDebugSwitches();
        initTimeBases();
    }

    private void initDebugSwitches() {
        boolean z = false;
        int prop = SystemProperties.getInt("persist.procbatterystats.debug", 0);
        this.debugCommon = (prop & 1) != 0;
        this.debugWakelock = (prop & 2) != 0;
        if ((prop & 4) != 0) {
            z = true;
        }
        this.debugScreenon = z;
        Slog.d(TAG, "persist.procbatterystats.debug: " + prop);
    }

    private void initTimeBases() {
        long realtimeUs = SystemClock.elapsedRealtime() * 1000;
        long uptimeUs = SystemClock.uptimeMillis() * 1000;
        this.mOnBatteryTimeBase.init(uptimeUs, realtimeUs);
        this.mOnBatteryScreenOffTimeBase.init(uptimeUs, realtimeUs);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateTimeBases(boolean unplugged, boolean screenOff) {
        if (this.debugCommon) {
            Slog.d(TAG, "updateTimeBases, unplugged: " + unplugged + ", screenOff: " + screenOff);
        }
        long realtimeUs = SystemClock.elapsedRealtime() * 1000;
        long uptimeUs = 1000 * SystemClock.uptimeMillis();
        this.mOnBatteryTimeBase.setRunning(unplugged, uptimeUs, realtimeUs);
        synchronized (this.mWakeLockTimeMap) {
            this.mOnBatteryScreenOffTimeBase.setRunning(unplugged && screenOff, uptimeUs, realtimeUs);
        }
    }

    /* access modifiers changed from: private */
    public final class SysEventsHandler extends Handler {
        public SysEventsHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            boolean screenOff = ProcBatteryStats.this.mScreenState == 0;
            Intent intent = (Intent) msg.obj;
            int uid = -1;
            switch (msg.what) {
                case 1:
                    if (ProcBatteryStats.this.debugCommon) {
                        Slog.i(ProcBatteryStats.TAG, "SYS_EVENT_POWER_CONNECTED");
                    }
                    ProcBatteryStats.this.updateTimeBases(false, screenOff);
                    return;
                case 2:
                    if (ProcBatteryStats.this.debugCommon) {
                        Slog.i(ProcBatteryStats.TAG, "SYS_EVENT_POWER_DISCONNECTED");
                    }
                    ProcBatteryStats.this.updateTimeBases(true, screenOff);
                    return;
                case 3:
                    int userId = intent.getIntExtra("android.intent.extra.user_handle", -1);
                    ProcBatteryStats.this.removeWlByUserId(userId);
                    if (ProcBatteryStats.this.debugCommon) {
                        Slog.i(ProcBatteryStats.TAG, "SYS_EVENT_USER_REMOVED, userId: " + userId);
                        return;
                    }
                    return;
                case 4:
                    Bundle intentExtras = intent.getExtras();
                    if (intentExtras != null) {
                        uid = intentExtras.getInt("android.intent.extra.UID");
                    }
                    ProcBatteryStats.this.removeWlByUid(uid);
                    if (ProcBatteryStats.this.debugCommon) {
                        Slog.i(ProcBatteryStats.TAG, "SYS_EVENT_UID_REMOVED, uid: " + uid);
                        return;
                    }
                    return;
                case 5:
                    ProcBatteryStats.this.noteScreenState(1);
                    if (ProcBatteryStats.this.debugCommon) {
                        Slog.i(ProcBatteryStats.TAG, "SYS_EVENT_SCREEN_ON");
                        return;
                    }
                    return;
                case 6:
                    ProcBatteryStats.this.noteScreenState(0);
                    if (ProcBatteryStats.this.debugCommon) {
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

    public void onSystemReady() {
        startSystemEventHandleThread();
        Slog.i(TAG, "ProcBatteryStats--systemReady");
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        if (code == 101) {
            data.enforceInterface(DESCRIPTOR);
            List<String> stats = getPowerStats(data.readInt());
            reply.writeNoException();
            reply.writeStringList(stats);
            return true;
        } else if (code != 102) {
            return false;
        } else {
            data.enforceInterface(DESCRIPTOR);
            noteResetAllProcInfo();
            reply.writeNoException();
            return true;
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
            /* class com.android.server.pg.ProcBatteryStats.AnonymousClass1 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                if (intent != null) {
                    Message msg = ProcBatteryStats.this.mSysEventsHandler.obtainMessage(((Integer) ProcBatteryStats.this.mActionIdMap.get(intent.getAction())).intValue());
                    msg.obj = intent;
                    ProcBatteryStats.this.mSysEventsHandler.sendMessageDelayed(msg, 0);
                } else if (ProcBatteryStats.this.debugCommon) {
                    Slog.d(ProcBatteryStats.TAG, "null intent");
                }
            }
        }, UserHandle.ALL, filter, null, null);
    }

    public void noteResetAllProcInfo() {
        Slog.d(TAG, "noteResetAllProcInfo");
        initTimeBases();
        resetWakeLockTime();
    }

    public List<String> getPowerStats(int type) {
        if (this.debugCommon) {
            Slog.d(TAG, "getPowerStats, type: " + type);
        }
        if (type == 0) {
            return getWakeLockStatsList();
        }
        Slog.w(TAG, "unexpected Type: " + type);
        return null;
    }

    public void noteScreenState(int state) {
        if (this.debugScreenon) {
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

    /* access modifiers changed from: private */
    public final class WakeLock {
        long mCounter;
        BatteryStatsImpl.StopwatchTimer mTimerPartial;

        private WakeLock() {
            this.mCounter = 0;
            this.mTimerPartial = new BatteryStatsImpl.StopwatchTimer(ProcBatteryStats.this.mClocks, (BatteryStatsImpl.Uid) null, 0, ProcBatteryStats.this.mPartialTimers, ProcBatteryStats.this.mOnBatteryScreenOffTimeBase);
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void startRunning(long realtime) {
            this.mTimerPartial.startRunningLocked(realtime);
            this.mCounter++;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void stopRunning(long realtime) {
            this.mTimerPartial.stopRunningLocked(realtime);
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private long getTotalTime(long realtime) {
            return this.mTimerPartial.getTotalTimeLocked(1000 * realtime, 0);
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private BatteryStatsImpl.StopwatchTimer getStopwatchTimer() {
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

    /* access modifiers changed from: private */
    public final class ProcWakeLockTime {
        String mProcName;
        int mUid;
        OverflowArrayMap<WakeLock> mWakelockStats = new OverflowArrayMap<WakeLock>() {
            /* class com.android.server.pg.ProcBatteryStats.ProcWakeLockTime.AnonymousClass1 */

            @Override // com.android.server.pg.ProcBatteryStats.OverflowArrayMap
            public WakeLock instantiateObject() {
                return new WakeLock();
            }
        };

        public ProcWakeLockTime(String name, int uid) {
            this.mProcName = name;
            this.mUid = uid;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private OverflowArrayMap<WakeLock> getWakeLockStats() {
            return this.mWakelockStats;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private boolean reset() {
            if (ProcBatteryStats.this.debugWakelock) {
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

    public void processWakeLock(int event, String tag, WorkSource ws, String pkgName, int uid) {
        int i;
        String pkgName2;
        ArrayMap<String, ProcWakeLockTime> arrayMap;
        Throwable th;
        String wsName;
        if (this.debugWakelock) {
            StringBuilder sb = new StringBuilder();
            sb.append("processWlInfo, event: ");
            sb.append(event);
            sb.append(", tag: ");
            sb.append(tag);
            sb.append(", ws: ");
            sb.append(ws);
            sb.append(", pkgName: ");
            sb.append(pkgName);
            sb.append(", uid: ");
            i = uid;
            sb.append(i);
            Slog.d(TAG, sb.toString());
        } else {
            i = uid;
        }
        long realtime = SystemClock.elapsedRealtime();
        if (pkgName == null) {
            Slog.w(TAG, "name null");
            pkgName2 = "";
        } else {
            pkgName2 = pkgName;
        }
        ArrayMap<String, ProcWakeLockTime> arrayMap2 = this.mWakeLockTimeMap;
        synchronized (arrayMap2) {
            if (ws == null) {
                arrayMap = arrayMap2;
                try {
                    updateWlTimer(event, tag, pkgName2, uid, realtime);
                } catch (Throwable th2) {
                    th = th2;
                    throw th;
                }
            } else {
                arrayMap = arrayMap2;
                int length = ws.size();
                int uid2 = i;
                for (int i2 = 0; i2 < length; i2++) {
                    try {
                        uid2 = ws.get(i2);
                        String wsName2 = ws.getName(i2);
                        if (wsName2 == null) {
                            wsName = "";
                        } else if (wsName2.indexOf(58) > 0) {
                            wsName = wsName2.substring(0, wsName2.indexOf(58));
                        } else {
                            wsName = wsName2;
                        }
                        updateWlTimer(event, tag, wsName, uid2, realtime);
                    } catch (Throwable th3) {
                        th = th3;
                        throw th;
                    }
                }
                i = uid2;
            }
            try {
            } catch (Throwable th4) {
                th = th4;
                throw th;
            }
        }
    }

    private void updateWlTimer(int event, String tag, String name, int uid, long realtime) {
        WakeLock wl;
        OverflowArrayMap<WakeLock> wakelockStats = getProcWakeLockTime(name, uid).getWakeLockStats();
        if (event == 160) {
            WakeLock wl2 = wakelockStats.startObject(tag);
            if (wl2 == null) {
                return;
            }
            if (!wl2.getStopwatchTimer().isRunningLocked()) {
                if (this.debugWakelock) {
                    Slog.d(TAG, "startRunWlTimer, name: " + name + ", uid: " + uid + ", tag: " + tag);
                }
                wl2.startRunning(realtime);
                return;
            }
            Slog.w(TAG, "timer is running , not start, name: " + name + ", uid: " + uid + ", tag: " + tag);
        } else if (event == 161 && (wl = wakelockStats.stopObject(tag)) != null) {
            if (wl.getStopwatchTimer().isRunningLocked()) {
                if (this.debugWakelock) {
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
            return this.mWakeLockTimeMap.get(key);
        }
        ProcWakeLockTime pwt = new ProcWakeLockTime(procName, uid);
        this.mWakeLockTimeMap.put(key, pwt);
        if (!this.debugWakelock) {
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
    public void getWakeLockBatteryStats(List<String> list) {
        long realtime = SystemClock.elapsedRealtime();
        synchronized (this.mWakeLockTimeMap) {
            try {
                int size = this.mWakeLockTimeMap.size();
                if (this.debugWakelock) {
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
                    if (totalTimeMs >= 100) {
                        if (this.debugWakelock) {
                            Slog.d(TAG, "big time, procName: " + pwt.mProcName + ", uid: " + pwt.mUid + ", timeMs: " + totalTimeMs);
                        }
                        sb.append(pwt.mProcName);
                        sb.append('%');
                        sb.append(pwt.mUid);
                        sb.append('%');
                        sb.append(totalTimeMs);
                        list.add(sb.toString());
                    } else if (this.debugWakelock) {
                        Slog.d(TAG, "small time, procName: " + pwt.mProcName + ", uid: " + pwt.mUid + ", timeMs: " + totalTimeMs);
                    }
                }
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
                        sb.append("".equals(procName) ? "NULL" : procName);
                        sb.append(" tag=");
                        sb.append(tag);
                        stats.add(sb.toString());
                        if (procBatteryStats.debugWakelock) {
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
    /* access modifiers changed from: public */
    private void removeWlByUid(int uid) {
        synchronized (this.mWakeLockTimeMap) {
            for (int i = this.mWakeLockTimeMap.size() - 1; i >= 0; i--) {
                if (this.mWakeLockTimeMap.valueAt(i).mUid == uid) {
                    this.mWakeLockTimeMap.removeAt(i);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void removeWlByUserId(int userId) {
        synchronized (this.mWakeLockTimeMap) {
            for (int i = this.mWakeLockTimeMap.size() - 1; i >= 0; i--) {
                if (userId == UserHandle.getUserId(this.mWakeLockTimeMap.valueAt(i).mUid)) {
                    this.mWakeLockTimeMap.removeAt(i);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public static abstract class OverflowArrayMap<T> {
        private static final String OVERFLOW_NAME = "*overflow*";
        private static final String OVERFLOW_WEIXIN = "WakerLock:overflow";
        ArrayMap<String, MutableInt> mActiveOverflow;
        ArrayMap<String, MutableInt> mActiveOverflowWeixin;
        T mCurOverflow;
        T mCurOverflowWeixin;
        final ArrayMap<String, T> mMap = new ArrayMap<>();
        int mNumOfNameStartWithWakelock = 0;

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
                this.mNumOfNameStartWithWakelock++;
            }
        }

        public void cleanup() {
            ArrayMap<String, MutableInt> arrayMap = this.mActiveOverflowWeixin;
            if (arrayMap != null && arrayMap.size() == 0) {
                this.mActiveOverflowWeixin = null;
            }
            if (this.mActiveOverflowWeixin == null) {
                if (this.mMap.containsKey(OVERFLOW_WEIXIN)) {
                    Slog.wtf(ProcBatteryStats.TAG, "Cleaning up with no active overflow weixin, but have overflow entry " + ((Object) this.mMap.get(OVERFLOW_WEIXIN)));
                    this.mMap.remove(OVERFLOW_WEIXIN);
                }
                this.mCurOverflowWeixin = null;
            } else if (this.mCurOverflowWeixin == null || !this.mMap.containsKey(OVERFLOW_WEIXIN)) {
                Slog.wtf(ProcBatteryStats.TAG, "Cleaning up with active overflow weixin, but no overflow entry: cur=" + ((Object) this.mCurOverflowWeixin) + " map=" + ((Object) this.mMap.get(OVERFLOW_WEIXIN)));
            }
            ArrayMap<String, MutableInt> arrayMap2 = this.mActiveOverflow;
            if (arrayMap2 != null && arrayMap2.size() == 0) {
                this.mActiveOverflow = null;
            }
            if (this.mActiveOverflow == null) {
                if (this.mMap.containsKey(OVERFLOW_NAME)) {
                    Slog.wtf(ProcBatteryStats.TAG, "Cleaning up with no active overflow, but have overflow entry " + ((Object) this.mMap.get(OVERFLOW_NAME)));
                    this.mMap.remove(OVERFLOW_NAME);
                }
                this.mCurOverflow = null;
            } else if (this.mCurOverflow == null || !this.mMap.containsKey(OVERFLOW_NAME)) {
                Slog.wtf(ProcBatteryStats.TAG, "Cleaning up with active overflow, but no overflow entry: cur=" + ((Object) this.mCurOverflow) + " map=" + ((Object) this.mMap.get(OVERFLOW_NAME)));
            }
        }

        public T startObject(String name) {
            MutableInt over;
            MutableInt over2;
            if (name == null) {
                name = "";
            }
            T obj = this.mMap.get(name);
            if (obj != null) {
                return obj;
            }
            ArrayMap<String, MutableInt> arrayMap = this.mActiveOverflowWeixin;
            if (arrayMap == null || (over2 = arrayMap.get(name)) == null) {
                if (name.startsWith("WakerLock:")) {
                    this.mNumOfNameStartWithWakelock++;
                    if (this.mNumOfNameStartWithWakelock > ProcBatteryStats.MAX_WAKERLOCKS_WEIXIN) {
                        T obj2 = this.mCurOverflowWeixin;
                        if (obj2 == null) {
                            T instantiateObject = instantiateObject();
                            this.mCurOverflowWeixin = instantiateObject;
                            obj2 = instantiateObject;
                            this.mMap.put(OVERFLOW_WEIXIN, obj2);
                        }
                        if (this.mActiveOverflowWeixin == null) {
                            this.mActiveOverflowWeixin = new ArrayMap<>();
                        }
                        this.mActiveOverflowWeixin.put(name, new MutableInt(1));
                        return obj2;
                    }
                }
                ArrayMap<String, MutableInt> arrayMap2 = this.mActiveOverflow;
                if (arrayMap2 != null && (over = arrayMap2.get(name)) != null) {
                    T obj3 = this.mCurOverflow;
                    if (obj3 == null) {
                        Slog.wtf(ProcBatteryStats.TAG, "Have active overflow " + name + " but null overflow");
                        T instantiateObject2 = instantiateObject();
                        this.mCurOverflow = instantiateObject2;
                        obj3 = instantiateObject2;
                        this.mMap.put(OVERFLOW_NAME, obj3);
                    }
                    over.value++;
                    return obj3;
                } else if (this.mMap.size() >= 100) {
                    Slog.i(ProcBatteryStats.TAG, "wakelocks more than 100, name: " + name);
                    T obj4 = this.mCurOverflow;
                    if (obj4 == null) {
                        T instantiateObject3 = instantiateObject();
                        this.mCurOverflow = instantiateObject3;
                        obj4 = instantiateObject3;
                        this.mMap.put(OVERFLOW_NAME, obj4);
                    }
                    if (this.mActiveOverflow == null) {
                        this.mActiveOverflow = new ArrayMap<>();
                    }
                    this.mActiveOverflow.put(name, new MutableInt(1));
                    return obj4;
                } else {
                    T obj5 = instantiateObject();
                    this.mMap.put(name, obj5);
                    return obj5;
                }
            } else {
                T obj6 = this.mCurOverflowWeixin;
                if (obj6 == null) {
                    Slog.wtf(ProcBatteryStats.TAG, "Have active overflow " + name + " but null overflow weixin");
                    T instantiateObject4 = instantiateObject();
                    this.mCurOverflowWeixin = instantiateObject4;
                    obj6 = instantiateObject4;
                    this.mMap.put(OVERFLOW_WEIXIN, obj6);
                }
                over2.value++;
                return obj6;
            }
        }

        public T stopObject(String name) {
            MutableInt over;
            T obj;
            MutableInt over2;
            T obj2;
            if (name == null) {
                name = "";
            }
            T obj3 = this.mMap.get(name);
            if (obj3 != null) {
                return obj3;
            }
            ArrayMap<String, MutableInt> arrayMap = this.mActiveOverflowWeixin;
            if (arrayMap == null || (over2 = arrayMap.get(name)) == null || (obj2 = this.mCurOverflowWeixin) == null) {
                ArrayMap<String, MutableInt> arrayMap2 = this.mActiveOverflow;
                if (arrayMap2 == null || (over = arrayMap2.get(name)) == null || (obj = this.mCurOverflow) == null) {
                    Slog.wtf(ProcBatteryStats.TAG, "Unable to find object for " + name + " mapsize=" + this.mMap.size() + " activeoverflow=" + this.mActiveOverflow + " curoverflow=" + ((Object) this.mCurOverflow));
                    return null;
                }
                over.value--;
                if (over.value <= 0) {
                    this.mActiveOverflow.remove(name);
                }
                return obj;
            }
            over2.value--;
            if (over2.value <= 0) {
                this.mActiveOverflowWeixin.remove(name);
            }
            return obj2;
        }
    }
}
