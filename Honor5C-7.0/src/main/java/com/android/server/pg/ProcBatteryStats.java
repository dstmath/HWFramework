package com.android.server.pg;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
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
import com.android.server.power.AbsPowerManagerService;
import java.util.ArrayList;
import java.util.List;

public class ProcBatteryStats {
    private static final String DESCRIPTOR = "com.huawei.pgmng.api.IPGManager";
    private static final int GET_POWER_STATS_TRANSACTION = 101;
    private static final int MAX_WAKELOCKS_PER_UID = 100;
    private static final int MAX_WAKERLOCKS_WEIXIN = 60;
    private static final int NOTE_RESET_ALL_INFO_TRANSACTION = 102;
    private static final int NOTE_SCREEN_STATE_TRANSACTION = 103;
    private static final int POWER_SOURCE_TYPE_WAKELOCK = 0;
    private static final int SCREEN_STATE_OFF = 0;
    private static final int SCREEN_STATE_ON = 1;
    private static final int STATS_TYPE = 0;
    private static final int SYS_EVENT_POWER_CONNECTED = 1;
    private static final int SYS_EVENT_POWER_DISCONNECTED = 2;
    private static final String TAG = "ProcBatteryStats";
    private boolean DEBUG_COMMON;
    private boolean DEBUG_REALNAME;
    private boolean DEBUG_SCREENON;
    private boolean DEBUG_WAKELOCK;
    private final ArrayMap<String, Integer> mActionIdMap;
    private Clocks mClocks;
    private Context mContext;
    private final TimeBase mOnBatteryScreenOffTimeBase;
    private final TimeBase mOnBatteryTimeBase;
    private final ArrayList<StopwatchTimer> mPartialTimers;
    private PackageManager mPm;
    private int mScreenState;
    private SysEventsHandler mSysEventsHandler;
    private final ArrayMap<String, ProcWakeLockTime> mWakeLockTimeMap;

    private static abstract class OverflowArrayMap<T> {
        private static final String OVERFLOW_NAME = "*overflow*";
        private static final String OVERFLOW_WEIXIN = "WakerLock:overflow";
        int M;
        ArrayMap<String, MutableInt> mActiveOverflow;
        ArrayMap<String, MutableInt> mActiveOverflowWeixin;
        T mCurOverflow;
        T mCurOverflowWeixin;
        final ArrayMap<String, T> mMap;

        public abstract T instantiateObject();

        public OverflowArrayMap() {
            this.M = ProcBatteryStats.STATS_TYPE;
            this.mMap = new ArrayMap();
        }

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
                this.M += ProcBatteryStats.SYS_EVENT_POWER_CONNECTED;
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
                    over.value += ProcBatteryStats.SYS_EVENT_POWER_CONNECTED;
                    return obj;
                }
            }
            if (name.startsWith("WakerLock:")) {
                this.M += ProcBatteryStats.SYS_EVENT_POWER_CONNECTED;
                if (this.M > ProcBatteryStats.MAX_WAKERLOCKS_WEIXIN) {
                    obj = this.mCurOverflowWeixin;
                    if (obj == null) {
                        obj = instantiateObject();
                        this.mCurOverflowWeixin = obj;
                        this.mMap.put(OVERFLOW_WEIXIN, obj);
                    }
                    if (this.mActiveOverflowWeixin == null) {
                        this.mActiveOverflowWeixin = new ArrayMap();
                    }
                    this.mActiveOverflowWeixin.put(name, new MutableInt(ProcBatteryStats.SYS_EVENT_POWER_CONNECTED));
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
                    over.value += ProcBatteryStats.SYS_EVENT_POWER_CONNECTED;
                    return obj;
                }
            }
            if (this.mMap.size() >= ProcBatteryStats.MAX_WAKELOCKS_PER_UID) {
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
                this.mActiveOverflow.put(name, new MutableInt(ProcBatteryStats.SYS_EVENT_POWER_CONNECTED));
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
        OverflowArrayMap<WakeLock> mWakelockStats;

        public ProcWakeLockTime(String name, int uid) {
            this.mWakelockStats = new OverflowArrayMap<WakeLock>() {
                public WakeLock instantiateObject() {
                    return new WakeLock(null);
                }
            };
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
            if (active) {
                return false;
            }
            return true;
        }
    }

    private final class SysEventsHandler extends Handler {
        public SysEventsHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            boolean screenOff = ProcBatteryStats.this.mScreenState == 0;
            switch (msg.what) {
                case ProcBatteryStats.SYS_EVENT_POWER_CONNECTED /*1*/:
                    if (ProcBatteryStats.this.DEBUG_COMMON) {
                        Slog.i(ProcBatteryStats.TAG, "SYS_EVENT_POWER_CONNECTED");
                    }
                    ProcBatteryStats.this.updateTimeBases(false, screenOff);
                case ProcBatteryStats.SYS_EVENT_POWER_DISCONNECTED /*2*/:
                    if (ProcBatteryStats.this.DEBUG_COMMON) {
                        Slog.i(ProcBatteryStats.TAG, "SYS_EVENT_POWER_DISCONNECTED");
                    }
                    ProcBatteryStats.this.updateTimeBases(true, screenOff);
                default:
                    Slog.w(ProcBatteryStats.TAG, "unexpected sysEvent: " + msg.what);
            }
        }
    }

    private final class WakeLock {
        long mCounter;
        StopwatchTimer mTimerPartial;

        private WakeLock() {
            this.mCounter = 0;
            this.mTimerPartial = new StopwatchTimer(ProcBatteryStats.this.mClocks, null, ProcBatteryStats.STATS_TYPE, ProcBatteryStats.this.mPartialTimers, ProcBatteryStats.this.mOnBatteryScreenOffTimeBase);
        }

        private void startRunning(long realtime) {
            this.mTimerPartial.startRunningLocked(realtime);
            this.mCounter++;
        }

        private void stopRunning(long realtime) {
            this.mTimerPartial.stopRunningLocked(realtime);
        }

        private long getTotalTime(long realtime) {
            return this.mTimerPartial.getTotalTimeLocked(1000 * realtime, ProcBatteryStats.STATS_TYPE);
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
        this.mSysEventsHandler = null;
        this.mContext = null;
        this.mPm = null;
        this.mOnBatteryScreenOffTimeBase = new TimeBase();
        this.mOnBatteryTimeBase = new TimeBase();
        this.mPartialTimers = new ArrayList();
        this.mActionIdMap = new ArrayMap();
        this.mWakeLockTimeMap = new ArrayMap();
        this.mClocks = new SystemClocks();
        this.mScreenState = SYS_EVENT_POWER_CONNECTED;
        this.DEBUG_WAKELOCK = false;
        this.DEBUG_SCREENON = false;
        this.DEBUG_REALNAME = false;
        this.DEBUG_COMMON = false;
        this.mContext = context;
        initDebugSwitches();
        initTimeBases();
    }

    private void initDebugSwitches() {
        boolean z;
        boolean z2 = true;
        int prop = SystemProperties.getInt("persist.procbatterystats.debug", STATS_TYPE);
        if ((prop & SYS_EVENT_POWER_CONNECTED) != 0) {
            z = true;
        } else {
            z = false;
        }
        this.DEBUG_COMMON = z;
        if ((prop & SYS_EVENT_POWER_DISCONNECTED) != 0) {
            z = true;
        } else {
            z = false;
        }
        this.DEBUG_WAKELOCK = z;
        if ((prop & 4) != 0) {
            z = true;
        } else {
            z = false;
        }
        this.DEBUG_SCREENON = z;
        if ((prop & 8) == 0) {
            z2 = false;
        }
        this.DEBUG_REALNAME = z2;
        Slog.d(TAG, "persist.procbatterystats.debug: " + prop);
    }

    private void initTimeBases() {
        long realtimeUs = SystemClock.elapsedRealtime() * 1000;
        long uptimeUs = SystemClock.uptimeMillis() * 1000;
        this.mOnBatteryTimeBase.init(uptimeUs, realtimeUs);
        this.mOnBatteryScreenOffTimeBase.init(uptimeUs, realtimeUs);
    }

    private void updateTimeBases(boolean unplugged, boolean screenOff) {
        boolean z;
        if (this.DEBUG_COMMON) {
            Slog.d(TAG, "updateTimeBases, unplugged: " + unplugged + ", screenOff: " + screenOff);
        }
        long realtimeUs = SystemClock.elapsedRealtime() * 1000;
        long uptimeUs = SystemClock.uptimeMillis() * 1000;
        this.mOnBatteryTimeBase.setRunning(unplugged, uptimeUs, realtimeUs);
        TimeBase timeBase = this.mOnBatteryScreenOffTimeBase;
        if (unplugged) {
            z = screenOff;
        } else {
            z = false;
        }
        timeBase.setRunning(z, uptimeUs, realtimeUs);
    }

    public void onSystemReady() {
        this.mPm = this.mContext.getPackageManager();
        startSystemEventHandleThread();
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        switch (code) {
            case GET_POWER_STATS_TRANSACTION /*101*/:
                data.enforceInterface(DESCRIPTOR);
                List<String> stats = getPowerStats(data.readInt());
                reply.writeNoException();
                reply.writeStringList(stats);
                return true;
            case NOTE_RESET_ALL_INFO_TRANSACTION /*102*/:
                data.enforceInterface(DESCRIPTOR);
                noteResetAllProcInfo();
                reply.writeNoException();
                return true;
            case NOTE_SCREEN_STATE_TRANSACTION /*103*/:
                data.enforceInterface(DESCRIPTOR);
                noteScreenState(data.readInt());
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
        this.mActionIdMap.put("android.intent.action.ACTION_POWER_CONNECTED", Integer.valueOf(SYS_EVENT_POWER_CONNECTED));
        this.mActionIdMap.put("android.intent.action.ACTION_POWER_DISCONNECTED", Integer.valueOf(SYS_EVENT_POWER_DISCONNECTED));
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.ACTION_POWER_CONNECTED");
        filter.addAction("android.intent.action.ACTION_POWER_DISCONNECTED");
        this.mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent == null) {
                    if (ProcBatteryStats.this.DEBUG_COMMON) {
                        Slog.d(ProcBatteryStats.TAG, "null intent");
                    }
                    return;
                }
                ProcBatteryStats.this.mSysEventsHandler.sendMessageDelayed(ProcBatteryStats.this.mSysEventsHandler.obtainMessage(((Integer) ProcBatteryStats.this.mActionIdMap.get(intent.getAction())).intValue()), 0);
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
            if (state == SYS_EVENT_POWER_CONNECTED) {
                updateTimeBases(this.mOnBatteryTimeBase.isRunning(), false);
            } else if (state == 0) {
                updateTimeBases(this.mOnBatteryTimeBase.isRunning(), true);
            }
        }
    }

    private String getRealPkgName(String name, int uid) {
        if (this.DEBUG_REALNAME) {
            Slog.d(TAG, "getRealPkgName, name: " + name + ", uid: " + uid);
        }
        if (uid <= 0 || uid > 19999 || name == null || this.mPm == null) {
            if (this.DEBUG_REALNAME) {
                Slog.d(TAG, "need distribute");
            }
            return null;
        } else if (uid >= AbsPowerManagerService.MIN_COVER_SCREEN_OFF_TIMEOUT) {
            String[] packages = this.mPm.getPackagesForUid(uid);
            if (packages == null || packages.length == 0) {
                Slog.w(TAG, "empty packages");
                return null;
            }
            int i = packages.length - 1;
            while (i > 0 && !name.equals(packages[i])) {
                i--;
            }
            return packages[i];
        } else if (name.indexOf(47) >= 0 || -1 == name.indexOf(46)) {
            if (this.DEBUG_REALNAME) {
                Slog.d(TAG, "invalid name: " + name);
            }
            return null;
        } else {
            String realName = name;
            try {
                if (name.indexOf(58) > 0) {
                    realName = name.substring(STATS_TYPE, name.indexOf(58));
                }
                if (this.DEBUG_REALNAME) {
                    Slog.d(TAG, "realName: " + realName);
                }
                this.mPm.getApplicationInfo(realName, STATS_TYPE);
                return realName;
            } catch (StringIndexOutOfBoundsException e) {
                return null;
            } catch (NameNotFoundException e2) {
                return null;
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
            String realName;
            if (ws == null) {
                if (!this.mWakeLockTimeMap.containsKey(pkgName)) {
                    uid = UserHandle.getAppId(uid);
                    realName = getRealPkgName(pkgName, uid);
                    if (realName != null) {
                        pkgName = realName;
                    }
                }
                updateWlTimer(event, tag, pkgName, uid, realtime);
            } else {
                int length = ws.size();
                for (int i = STATS_TYPE; i < length; i += SYS_EVENT_POWER_CONNECTED) {
                    uid = UserHandle.getAppId(ws.get(i));
                    realName = getRealPkgName(pkgName, uid);
                    if (realName != null) {
                        pkgName = realName;
                    }
                    updateWlTimer(event, tag, pkgName, uid, realtime);
                }
            }
        }
    }

    private void updateWlTimer(int event, String tag, String name, int uid, long realtime) {
        OverflowArrayMap<WakeLock> wakelockStats = getProcWakeLockTime(name, uid).getWakeLockStats();
        WakeLock wl;
        if (event == HdmiCecKeycode.UI_SOUND_PRESENTATION_SELECT_AUDIO_AUTO_EQUALIZER) {
            wl = (WakeLock) wakelockStats.startObject(tag);
            if (wl != null) {
                if (this.DEBUG_WAKELOCK) {
                    Slog.d(TAG, "startRunWlTimer, name: " + name + ", uid: " + uid + ", tag: " + tag);
                }
                wl.startRunning(realtime);
            }
        } else if (event == 161) {
            wl = (WakeLock) wakelockStats.stopObject(tag);
            if (wl != null) {
                if (this.DEBUG_WAKELOCK) {
                    Slog.d(TAG, "stopRunWlTimer, name: " + name + ", uid: " + uid + ", tag: " + tag);
                }
                wl.stopRunning(realtime);
            }
        }
    }

    private ProcWakeLockTime getProcWakeLockTime(String realName, int uid) {
        if (this.mWakeLockTimeMap.containsKey(realName)) {
            return (ProcWakeLockTime) this.mWakeLockTimeMap.get(realName);
        }
        ProcWakeLockTime pwt = new ProcWakeLockTime(realName, uid);
        this.mWakeLockTimeMap.put(realName, pwt);
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

    private List<String> getWakeLockStatsList() {
        List<String> stats = new ArrayList();
        long realtime = SystemClock.elapsedRealtime();
        synchronized (this.mWakeLockTimeMap) {
            for (int i = this.mWakeLockTimeMap.size() - 1; i >= 0; i--) {
                String procName = (String) this.mWakeLockTimeMap.keyAt(i);
                ProcWakeLockTime pwt = (ProcWakeLockTime) this.mWakeLockTimeMap.valueAt(i);
                ArrayMap<String, WakeLock> wakeStats = pwt.getWakeLockStats().getMap();
                for (int iw = wakeStats.size() - 1; iw >= 0; iw--) {
                    String tag = (String) wakeStats.keyAt(iw);
                    long totalTimeMs = ((WakeLock) wakeStats.valueAt(iw)).getTotalTime(realtime) / 1000;
                    if (totalTimeMs > 0) {
                        StringBuilder sb = new StringBuilder(DumpState.DUMP_PACKAGES);
                        sb.append("uid=");
                        sb.append(pwt.mUid);
                        sb.append(" prevent_time=");
                        sb.append(totalTimeMs);
                        sb.append(" ws_name=");
                        sb.append(procName);
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
}
