package com.android.server;

import android.app.PendingIntent;
import android.content.Context;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.Slog;
import com.android.server.AlarmManagerService.Alarm;
import com.android.server.wifipro.WifiProCommonUtils;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

public class SmartHeartBeat extends SmartHeartBeatDummy {
    static final int ALARM_ADJUST_BACKWARD = 2;
    static final int ALARM_ADJUST_FORWARD = 0;
    static final int ALARM_ADJUST_NEAR = 1;
    static final int ALARM_ADJUST_NONE = -1;
    static final int ALARM_ADJUST_PACKAGE_MSG = 102;
    static final int ALARM_PENDING_PACKAGE_MSG = 101;
    static final int ALL_ALARM_TYPE = 0;
    static final boolean DEBUG_HEART_BEAT = SystemProperties.getBoolean("persist.sys.shb.debug", false);
    static final int NONE_WAKEUP_ALARM_TYPE = 2;
    static final boolean SHB_MODULE_SWITCHER = SystemProperties.getBoolean("persist.sys.shb.switcher", true);
    static final String TAG = "SmartHeartBeat";
    static final int WAKEUP_ALARM_TYPE = 1;
    private static HwAlarmManagerService mAlarmService;
    private static Context mContext;
    private static SmartHeartBeatDummy mInstance = null;
    private static final Object mLock = new Object();
    private HeartBeatDatabase mDataMap;
    private Handler mHandler;
    private HandlerThread mThread = new HandlerThread("HeartBeatHandlerThread");

    private static class AdjustValue {
        private long interval = 0;
        private int mode = 0;

        AdjustValue(long theInterval, int theMode) {
            this.interval = theInterval;
            this.mode = theMode;
        }

        public long getInterval() {
            return this.interval;
        }

        public int getMode() {
            return this.mode;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder(128);
            sb.append(this.interval);
            sb.append(" ");
            sb.append(this.mode);
            return sb.toString();
        }
    }

    private static class HeartBeatDatabase {
        public final AdjustPackageForActions adjustPackageForActions;
        private Object lock;
        public final AdjustPackage noneWakeupAdjustPkgsMap;
        private final MultiMap<String, Integer> pendingMap;
        private final MultiMap<String, String> pendingMapForActions;
        public final AdjustPackage wakeupAdjustPkgsMap;

        private class AdjustPackage {
            private final HashMap<String, AdjustValue> adjustMap;

            /* synthetic */ AdjustPackage(HeartBeatDatabase this$1, AdjustPackage -this1) {
                this();
            }

            private AdjustPackage() {
                this.adjustMap = new HashMap();
            }

            public void clearAdjustPkg() {
                synchronized (HeartBeatDatabase.this.lock) {
                    this.adjustMap.clear();
                }
            }

            public void putAdjustPkg(String pkg, long interval, int mode) {
                synchronized (HeartBeatDatabase.this.lock) {
                    this.adjustMap.put(pkg, new AdjustValue(interval, mode));
                }
            }

            public long getAdjustPkgInterval(String pkg) {
                long interval;
                synchronized (HeartBeatDatabase.this.lock) {
                    AdjustValue pkgValue = (AdjustValue) this.adjustMap.get(pkg);
                    interval = pkgValue == null ? 0 : pkgValue.getInterval();
                }
                return interval;
            }

            public int getAdjustPkgMode(String pkg) {
                int mode;
                synchronized (HeartBeatDatabase.this.lock) {
                    AdjustValue pkgValue = (AdjustValue) this.adjustMap.get(pkg);
                    mode = pkgValue == null ? 0 : pkgValue.getMode();
                }
                return mode;
            }

            public void removeAdjustPkg(String pkg) {
                synchronized (HeartBeatDatabase.this.lock) {
                    this.adjustMap.remove(pkg);
                }
            }

            public boolean containsAdjustKey(String pkg) {
                boolean containsKey;
                synchronized (HeartBeatDatabase.this.lock) {
                    containsKey = this.adjustMap.containsKey(pkg);
                }
                return containsKey;
            }

            public void dump(PrintWriter pw) {
                synchronized (HeartBeatDatabase.this.lock) {
                    for (Entry entry : this.adjustMap.entrySet()) {
                        pw.print(entry.getKey() + ":");
                        pw.println(entry.getValue());
                    }
                }
            }
        }

        private class AdjustPackageForActions {
            private final HashMap<String, HashMap<String, AdjustValue>> mPkgMap;

            /* synthetic */ AdjustPackageForActions(HeartBeatDatabase this$1, AdjustPackageForActions -this1) {
                this();
            }

            private AdjustPackageForActions() {
                this.mPkgMap = new HashMap();
            }

            public void clearAdjustPkg() {
                synchronized (HeartBeatDatabase.this.lock) {
                    this.mPkgMap.clear();
                }
            }

            public void putAdjustPkg(String pkg, List<String> actions, long interval, int mode) {
                synchronized (HeartBeatDatabase.this.lock) {
                    HashMap<String, AdjustValue> actionMap = (HashMap) this.mPkgMap.get(pkg);
                    if (actionMap == null) {
                        actionMap = new HashMap();
                        this.mPkgMap.put(pkg, actionMap);
                    }
                    if (actionMap != null) {
                        int listSize = actions.size();
                        for (int i = 0; i < listSize; i++) {
                            actionMap.put((String) actions.get(i), new AdjustValue(interval, mode));
                        }
                    }
                }
            }

            public AdjustValue getAdjustValue(String pkg, String action) {
                synchronized (HeartBeatDatabase.this.lock) {
                    HashMap<String, AdjustValue> actionMap = (HashMap) this.mPkgMap.get(pkg);
                    if (actionMap != null) {
                        AdjustValue adjustValue = (AdjustValue) actionMap.get(action);
                        return adjustValue;
                    }
                    return null;
                }
            }

            public void removeAdjustPkg(String pkg, List<String> actions) {
                synchronized (HeartBeatDatabase.this.lock) {
                    HashMap<String, AdjustValue> actionMap = (HashMap) this.mPkgMap.get(pkg);
                    if (actionMap != null) {
                        int listSize = actions.size();
                        for (int i = 0; i < listSize; i++) {
                            actionMap.remove(actions.get(i));
                        }
                        if (actionMap.isEmpty()) {
                            this.mPkgMap.remove(pkg);
                        }
                    }
                }
            }

            public boolean containsAdjustKey(String pkg, String action) {
                synchronized (HeartBeatDatabase.this.lock) {
                    HashMap<String, AdjustValue> actionMap = (HashMap) this.mPkgMap.get(pkg);
                    if (actionMap != null) {
                        boolean containsKey = actionMap.containsKey(action);
                        return containsKey;
                    }
                    return false;
                }
            }

            public void dump(PrintWriter pw) {
                synchronized (HeartBeatDatabase.this.lock) {
                    for (Entry entry : this.mPkgMap.entrySet()) {
                        pw.println(entry.getKey() + ":");
                        for (Entry entry2 : ((HashMap) entry.getValue()).entrySet()) {
                            pw.print("    " + entry2.getKey() + ":");
                            pw.println(entry2.getValue());
                        }
                    }
                }
            }
        }

        /* synthetic */ HeartBeatDatabase(HeartBeatDatabase -this0) {
            this();
        }

        private HeartBeatDatabase() {
            this.pendingMap = new MultiMap();
            this.pendingMapForActions = new MultiMap();
            this.wakeupAdjustPkgsMap = new AdjustPackage(this, null);
            this.noneWakeupAdjustPkgsMap = new AdjustPackage(this, null);
            this.adjustPackageForActions = new AdjustPackageForActions(this, null);
            this.lock = new Object();
        }

        public void addPendingPackage(String pkgName, int type) {
            this.pendingMap.put(pkgName, Integer.valueOf(type));
            if (SmartHeartBeat.DEBUG_HEART_BEAT) {
                Slog.d(SmartHeartBeat.TAG, "addPendingPackage, pkgName: " + pkgName + ", type: " + type);
            }
        }

        public void addPendingPackageForActions(String pkgName, List<String> actionList) {
            if (SmartHeartBeat.DEBUG_HEART_BEAT) {
                Slog.d(SmartHeartBeat.TAG, "addPendingPackageForActions, pkgName: " + pkgName + ", actionList: " + actionList);
            }
            int listSize = actionList.size();
            for (int i = 0; i < listSize; i++) {
                this.pendingMapForActions.put(pkgName, (String) actionList.get(i));
            }
        }

        public void removePendingPackage(String pkgName, int type) {
            this.pendingMap.remove(pkgName, Integer.valueOf(type));
            if (SmartHeartBeat.DEBUG_HEART_BEAT) {
                Slog.d(SmartHeartBeat.TAG, "removePendingPackage, pkgName: " + pkgName + ", type: " + type);
            }
        }

        public void removePendingPackageForActions(String pkgName, List<String> actionList) {
            if (SmartHeartBeat.DEBUG_HEART_BEAT) {
                Slog.d(SmartHeartBeat.TAG, "removePendingPackageForActions, pkgName: " + pkgName + ", actionList: " + actionList);
            }
            int listSize = actionList.size();
            for (int i = 0; i < listSize; i++) {
                this.pendingMapForActions.remove(pkgName, (String) actionList.get(i));
            }
        }

        public void removeAllPendingPackages() {
            this.pendingMap.clear();
            this.pendingMapForActions.clear();
            if (SmartHeartBeat.DEBUG_HEART_BEAT) {
                Slog.d(SmartHeartBeat.TAG, "removeAllPendingPackages");
            }
        }

        public boolean isPendingPackage(Alarm alarm) {
            String pkgName = alarm.packageName;
            String action = SmartHeartBeat.getActionByClearCallingIdentity(alarm.operation);
            List<String> actionList = this.pendingMapForActions.getAll(pkgName);
            if (actionList.size() > 0 && action != null && actionList.contains(action)) {
                if (SmartHeartBeat.DEBUG_HEART_BEAT) {
                    Slog.d(SmartHeartBeat.TAG, "isPendingPackage, true, pkg:" + pkgName + ", action:" + action);
                }
                return true;
            } else if (alarm.type != 0 || alarm.alarmClock == null) {
                List<Integer> pendingTypeList = this.pendingMap.getAll(pkgName);
                if (pendingTypeList.size() > 0) {
                    for (Integer alarmType : pendingTypeList) {
                        if (alarmType.intValue() == alarm.type) {
                            if (SmartHeartBeat.DEBUG_HEART_BEAT) {
                                Slog.d(SmartHeartBeat.TAG, "isPendingPackage, true, pkg:" + pkgName);
                            }
                            return true;
                        }
                    }
                }
                return false;
            } else {
                Slog.d(SmartHeartBeat.TAG, "isPendingPackage, false, pkg:" + pkgName + ", action:" + action + ",not pending alarm with AlarmClockInfo");
                return false;
            }
        }

        public void dump(PrintWriter pw) {
            synchronized (this.lock) {
                pw.println("=======dumping shb bg");
                pw.println("===pending pkg:");
                this.pendingMap.dump(pw);
                pw.println();
                pw.println("===pending actions:");
                this.pendingMapForActions.dump(pw);
                pw.println();
                pw.println("===adjust pkg wakeup:");
                this.wakeupAdjustPkgsMap.dump(pw);
                pw.println();
                pw.println("===adjust pkg none_wakeup:");
                this.noneWakeupAdjustPkgsMap.dump(pw);
                pw.println();
                pw.println("===adjust actions:");
                this.adjustPackageForActions.dump(pw);
                pw.println("=======dumping shb end");
            }
        }
    }

    private static class HeartBeatHandler extends Handler {
        public HeartBeatHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 101:
                    pendingPackageMessage(msg);
                    return;
                case 102:
                    adjustPackageMessage(msg);
                    return;
                default:
                    return;
            }
        }

        private void adjustPackageMessage(Message msg) {
            List<String> pkgList = msg.obj;
            if (SmartHeartBeat.DEBUG_HEART_BEAT) {
                Slog.d(SmartHeartBeat.TAG, "adjustPackageMessage, pkgList: " + pkgList);
            }
            synchronized (SmartHeartBeat.mAlarmService.mLock) {
                SmartHeartBeat.mAlarmService.rebatchPkgAlarmsLocked(pkgList);
            }
        }

        private void pendingPackageMessage(Message msg) {
            MessageInfo args = msg.obj;
            if (args == null) {
                Slog.d(SmartHeartBeat.TAG, "pendingPackageMessage, null == args");
                return;
            }
            boolean pending = args.state;
            if (SmartHeartBeat.DEBUG_HEART_BEAT) {
                Slog.d(SmartHeartBeat.TAG, "pendingPackageMessage, pending: " + pending + ", pkg:" + args.pkgList);
            }
            synchronized (SmartHeartBeat.mAlarmService.mLock) {
                if (args.pkgList == null) {
                    SmartHeartBeat.mAlarmService.rebatchAllAlarmsLocked(true);
                } else {
                    SmartHeartBeat.mAlarmService.rebatchPkgAlarmsLocked(args.pkgList);
                }
            }
        }
    }

    private static class MessageInfo {
        List<String> pkgList;
        boolean state;

        /* synthetic */ MessageInfo(MessageInfo -this0) {
            this();
        }

        private MessageInfo() {
        }
    }

    private static class MultiMap<K, V> {
        private HashMap<K, List<V>> store;

        /* synthetic */ MultiMap(MultiMap -this0) {
            this();
        }

        private MultiMap() {
            this.store = new HashMap();
        }

        List<V> getAll(K key) {
            List<V> values = (List) this.store.get(key);
            return values != null ? values : Collections.emptyList();
        }

        void put(K key, V val) {
            List<V> curVals = (List) this.store.get(key);
            if (curVals == null) {
                curVals = new ArrayList(3);
                this.store.put(key, curVals);
            }
            if (!curVals.contains(val)) {
                curVals.add(val);
            }
        }

        void remove(K key, V val) {
            List<V> curVals = (List) this.store.get(key);
            if (curVals != null) {
                curVals.remove(val);
                if (curVals.isEmpty()) {
                    this.store.remove(key);
                }
            }
        }

        void removeAll(K key) {
            this.store.remove(key);
        }

        void clear() {
            this.store.clear();
        }

        void dump(PrintWriter pw) {
            for (Entry entry : this.store.entrySet()) {
                pw.print(entry.getKey() + ":");
                pw.println(entry.getValue());
            }
        }
    }

    public static synchronized SmartHeartBeatDummy getInstance(Context context, HwAlarmManagerService service) {
        SmartHeartBeatDummy smartHeartBeatDummy;
        synchronized (SmartHeartBeat.class) {
            if (mInstance == null) {
                if (SHB_MODULE_SWITCHER) {
                    mContext = context;
                    mAlarmService = service;
                    Slog.i(TAG, "getInstance new");
                    mInstance = new SmartHeartBeat();
                } else {
                    Slog.i(TAG, "SmartHeartBeat is turn off !");
                    mInstance = new SmartHeartBeatDummy();
                }
            }
            if (DEBUG_HEART_BEAT) {
                Slog.i(TAG, "getInstance return");
            }
            smartHeartBeatDummy = mInstance;
        }
        return smartHeartBeatDummy;
    }

    private SmartHeartBeat() {
        this.mThread.start();
        this.mHandler = new HeartBeatHandler(this.mThread.getLooper());
        this.mDataMap = new HeartBeatDatabase();
        Slog.i(TAG, "SmartHeartBeat getInstance init success!");
    }

    protected void finalize() {
        this.mThread.quit();
        Slog.i(TAG, "SmartHeartBeat destructor !");
        try {
            super.finalize();
        } catch (Throwable th) {
        }
    }

    private boolean isPending(Alarm alarm) {
        boolean pending = false;
        if (alarm == null) {
            Slog.d(TAG, "isPending, null == alarm");
            return false;
        }
        if (this.mDataMap.isPendingPackage(alarm)) {
            pending = true;
        }
        if (DEBUG_HEART_BEAT) {
            Slog.d(TAG, "isPending, ret:" + pending + ", alarm:" + alarm);
        }
        return pending;
    }

    public boolean shouldPendingAlarm(Alarm alarm) {
        boolean isPending;
        synchronized (mLock) {
            isPending = isPending(alarm);
        }
        return isPending;
    }

    public void setAlarmsPending(List<String> pkgList, List<String> actionList, boolean pending, int pendingType) {
        if (pkgList == null || pkgList.size() <= 0) {
            Slog.d(TAG, "setAlarmsPending, pkgList=" + pkgList);
            return;
        }
        if (DEBUG_HEART_BEAT) {
            Slog.i(TAG, "setAlarmsPending, pkgList=" + pkgList + ", action=" + actionList + ", pending=" + pending + ", pendingType=" + pendingType);
        } else {
            Slog.d(TAG, "setAlarmsPending ...");
        }
        synchronized (mLock) {
            for (String pkgName : pkgList) {
                if (pending) {
                    if (actionList != null) {
                        this.mDataMap.addPendingPackageForActions(pkgName, actionList);
                    } else {
                        if (pendingType == 0 || 1 == pendingType) {
                            this.mDataMap.addPendingPackage(pkgName, 0);
                            this.mDataMap.addPendingPackage(pkgName, 2);
                        }
                        if (pendingType == 0 || 2 == pendingType) {
                            this.mDataMap.addPendingPackage(pkgName, 1);
                            this.mDataMap.addPendingPackage(pkgName, 3);
                        }
                    }
                } else if (actionList != null) {
                    this.mDataMap.removePendingPackageForActions(pkgName, actionList);
                } else {
                    if (pendingType == 0 || 1 == pendingType) {
                        this.mDataMap.removePendingPackage(pkgName, 0);
                        this.mDataMap.removePendingPackage(pkgName, 2);
                    }
                    if (pendingType == 0 || 2 == pendingType) {
                        this.mDataMap.removePendingPackage(pkgName, 1);
                        this.mDataMap.removePendingPackage(pkgName, 3);
                    }
                }
            }
        }
        MessageInfo args = new MessageInfo();
        args.state = pending;
        args.pkgList = pkgList;
        Message msg = this.mHandler.obtainMessage();
        msg.what = 101;
        msg.obj = args;
        this.mHandler.sendMessage(msg);
    }

    public void removeAllPendingAlarms() {
        synchronized (mLock) {
            this.mDataMap.removeAllPendingPackages();
        }
        MessageInfo args = new MessageInfo();
        args.state = true;
        args.pkgList = null;
        Message msg = this.mHandler.obtainMessage();
        msg.what = 101;
        msg.obj = args;
        this.mHandler.sendMessage(msg);
        Slog.i(TAG, "remove all pending alarms");
    }

    private boolean isAdjustAlarm(Alarm a) {
        String pkg = a.packageName;
        String action = getActionByClearCallingIdentity(a.operation);
        int type = a.type;
        if (this.mDataMap.adjustPackageForActions.containsAdjustKey(pkg, action)) {
            if (DEBUG_HEART_BEAT) {
                Slog.d(TAG, "isAdjustAlarm: true,pkg: " + pkg + ",action: " + action);
            }
            return true;
        } else if (type != 0 || a.alarmClock == null) {
            boolean isAdjustAlarm;
            if (type == 0 || 2 == type) {
                isAdjustAlarm = this.mDataMap.wakeupAdjustPkgsMap.containsAdjustKey(pkg);
            } else if (1 == type || 3 == type) {
                isAdjustAlarm = this.mDataMap.noneWakeupAdjustPkgsMap.containsAdjustKey(pkg);
            } else {
                if (DEBUG_HEART_BEAT) {
                    Slog.d(TAG, "isAdjustAlarm1: " + false);
                }
                return false;
            }
            if (DEBUG_HEART_BEAT) {
                Slog.d(TAG, "isAdjustAlarm: " + isAdjustAlarm + ",pkg:" + pkg);
            }
            return isAdjustAlarm;
        } else {
            if (DEBUG_HEART_BEAT) {
                Slog.d(TAG, "isAdjustAlarm: false,pkg: " + pkg + ",action: " + action + ",not adjustalarm with AlarmClockInfo");
            }
            return false;
        }
    }

    private long calAlarmWhenElapsed(Alarm a) {
        long interval = 0;
        int mode = -1;
        long adjustWhenElapsed = a.whenElapsed;
        String pkg = a.packageName;
        String action = getActionByClearCallingIdentity(a.operation);
        int type = a.type;
        long whenElapsed = a.whenElapsed;
        AdjustValue adjustValue = this.mDataMap.adjustPackageForActions.getAdjustValue(pkg, action);
        if (adjustValue != null) {
            interval = adjustValue.interval;
            mode = adjustValue.mode;
        }
        if (interval == 0) {
            if (type == 0 || 2 == type) {
                interval = this.mDataMap.wakeupAdjustPkgsMap.getAdjustPkgInterval(pkg);
                mode = this.mDataMap.wakeupAdjustPkgsMap.getAdjustPkgMode(pkg);
            } else if (1 != type && 3 != type) {
                return adjustWhenElapsed;
            } else {
                interval = this.mDataMap.noneWakeupAdjustPkgsMap.getAdjustPkgInterval(pkg);
                mode = this.mDataMap.noneWakeupAdjustPkgsMap.getAdjustPkgMode(pkg);
            }
        }
        if (0 == interval) {
            return adjustWhenElapsed;
        }
        if ((type == 0 || 1 == type) && 0 > whenElapsed) {
            Slog.d(TAG, "Maybe the whenElapsed time is incorrect: pkg=" + pkg + " whenElapsed = " + whenElapsed);
            whenElapsed = SystemClock.elapsedRealtime();
        }
        switch (mode) {
            case 0:
            case 1:
            case 2:
                adjustWhenElapsed = ((((interval / 2) * ((long) mode)) + whenElapsed) / interval) * interval;
                if (mode == 2) {
                    if (2 == mode && adjustWhenElapsed - whenElapsed == interval) {
                        adjustWhenElapsed = whenElapsed;
                        break;
                    }
                } else if (adjustWhenElapsed < SystemClock.elapsedRealtime()) {
                    adjustWhenElapsed += interval;
                    break;
                }
                break;
        }
        if (DEBUG_HEART_BEAT) {
            Slog.d(TAG, "calAlarmWhenElapsed pkg: " + pkg + ", action: " + action + ", whenElapsed: " + whenElapsed + ", adjusted WhenElapsed: " + adjustWhenElapsed + ", interval: " + interval + ", mode: " + mode);
        }
        return adjustWhenElapsed;
    }

    private long getAdjustAlarmWhenElapsed(Alarm a) {
        long val = isAdjustAlarm(a) ? calAlarmWhenElapsed(a) : a.whenElapsed;
        if (DEBUG_HEART_BEAT) {
            Slog.d(TAG, "getAdjustAlarmWhenElapsed, val: " + val + ", pkg: " + a.packageName + ", whenElapsed: " + a.whenElapsed + ", type: " + a.type);
        }
        return val;
    }

    private static String getActionByClearCallingIdentity(PendingIntent operation) {
        long identity = Binder.clearCallingIdentity();
        String action = null;
        if (operation != null) {
            try {
                action = operation.getIntent().getAction();
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identity);
                return action;
            }
        }
        Binder.restoreCallingIdentity(identity);
        return action;
    }

    public void setAlarmsAdjust(List<String> pkgList, List<String> actionList, boolean adjust, int type, long interval, int mode) {
        if (pkgList == null || pkgList.size() <= 0) {
            Slog.i(TAG, "setAlarmsAdjust, pkgList: " + pkgList);
            return;
        }
        if (DEBUG_HEART_BEAT) {
            Slog.i(TAG, "setAlarmsAdjust, pkgList=" + pkgList + ", action=" + actionList + ", adjust=" + adjust + ", type=" + type + ", interval=" + interval + ", mode=" + mode);
        } else {
            Slog.d(TAG, "setAlarmsAdjust ...");
        }
        synchronized (mLock) {
            for (String pkg : pkgList) {
                if (adjust) {
                    if (interval <= 0) {
                        Slog.d(TAG, "setAlarmsAdjust interval must be greater than 0.");
                        return;
                    } else if (actionList != null) {
                        this.mDataMap.adjustPackageForActions.putAdjustPkg(pkg, actionList, interval, mode);
                    } else {
                        if (type == 0 || 1 == type) {
                            this.mDataMap.wakeupAdjustPkgsMap.putAdjustPkg(pkg, interval, mode);
                        }
                        if (type == 0 || 2 == type) {
                            this.mDataMap.noneWakeupAdjustPkgsMap.putAdjustPkg(pkg, interval, mode);
                        }
                    }
                } else if (actionList != null) {
                    this.mDataMap.adjustPackageForActions.removeAdjustPkg(pkg, actionList);
                } else {
                    if (type == 0 || 1 == type) {
                        this.mDataMap.wakeupAdjustPkgsMap.removeAdjustPkg(pkg);
                    }
                    if (type == 0 || 2 == type) {
                        this.mDataMap.noneWakeupAdjustPkgsMap.removeAdjustPkg(pkg);
                    }
                }
            }
            Message msg = this.mHandler.obtainMessage();
            msg.what = 102;
            msg.obj = pkgList;
            this.mHandler.sendMessage(msg);
        }
    }

    public void removeAllAdjustAlarms() {
        Slog.i(TAG, "remove all adjust alarms.");
        synchronized (mLock) {
            this.mDataMap.wakeupAdjustPkgsMap.clearAdjustPkg();
            this.mDataMap.noneWakeupAdjustPkgsMap.clearAdjustPkg();
            this.mDataMap.adjustPackageForActions.clearAdjustPkg();
        }
    }

    /* JADX WARNING: Missing block: B:15:0x005c, code:
            return;
     */
    /* JADX WARNING: Missing block: B:26:0x00c1, code:
            return;
     */
    /* JADX WARNING: Missing block: B:38:0x0133, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void adjustAlarmIfNeeded(Alarm a) {
        if (a == null) {
            Slog.e(TAG, "adjustAlarmIfNeeded, alarm is null!");
            return;
        }
        long now = SystemClock.elapsedRealtime();
        synchronized (mLock) {
            if (now >= a.whenElapsed || a.whenElapsed - now <= WifiProCommonUtils.TRUSTED_DAYS_MS) {
                if (isPending(a)) {
                    long delay;
                    if (now < a.whenElapsed) {
                        delay = WifiProCommonUtils.TRUSTED_DAYS_MS;
                    } else {
                        delay = (now - a.whenElapsed) + WifiProCommonUtils.TRUSTED_DAYS_MS;
                    }
                    a.when += delay;
                    a.whenElapsed += delay;
                    a.maxWhenElapsed += delay;
                    if (DEBUG_HEART_BEAT) {
                        Slog.i(TAG, "adjustAlarmIfNeeded, is pending alarm: " + a.packageName + ", tag: " + a.statsTag + ", whenElapsed: " + a.whenElapsed + ", now: " + now);
                    }
                } else {
                    long whenElapsed = getAdjustAlarmWhenElapsed(a);
                    if (whenElapsed != a.whenElapsed) {
                        long delta = whenElapsed - a.whenElapsed;
                        a.whenElapsed += delta;
                        a.when += delta;
                        a.maxWhenElapsed = a.whenElapsed > a.maxWhenElapsed ? a.whenElapsed : a.maxWhenElapsed;
                        if (DEBUG_HEART_BEAT) {
                            Slog.i(TAG, "adjustAlarmLocked, is unify alarm: " + a.packageName + ", tag: " + a.statsTag + ", whenElapsed: " + a.whenElapsed + ", now: " + now);
                        }
                    }
                }
            } else if (DEBUG_HEART_BEAT) {
                Slog.i(TAG, "adjustAlarmIfNeeded, no need change alarm: " + a.packageName + ", whenElapsed: " + a.whenElapsed + ", now: " + now);
            }
        }
    }

    public void dump(PrintWriter pw) {
        synchronized (mLock) {
            this.mDataMap.dump(pw);
        }
    }
}
