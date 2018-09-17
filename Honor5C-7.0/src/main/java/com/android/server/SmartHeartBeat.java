package com.android.server;

import android.app.PendingIntent;
import android.content.Context;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Slog;
import com.android.server.AlarmManagerService.Alarm;
import com.android.server.security.securitydiagnose.HwSecDiagnoseConstant;
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
    static final boolean DEBUG_HEART_BEAT = false;
    static final int NONE_WAKEUP_ALARM_TYPE = 2;
    static final boolean SHB_MODULE_SWITCHER = false;
    static final String TAG = "SmartHeartBeat";
    static final int WAKEUP_ALARM_TYPE = 1;
    private static HwAlarmManagerService mAlarmService;
    private static Context mContext;
    private static SmartHeartBeatDummy mInstance;
    private static final Object mLock = null;
    private HeartBeatDatabase mDataMap;
    private Handler mHandler;
    private HandlerThread mThread;

    private static class AdjustValue {
        private long interval;
        private int mode;

        AdjustValue(long theInterval, int theMode) {
            this.interval = 0;
            this.mode = SmartHeartBeat.ALL_ALARM_TYPE;
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
            StringBuilder sb = new StringBuilder(HwSecDiagnoseConstant.BIT_VERIFYBOOT);
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
                    mode = pkgValue == null ? SmartHeartBeat.ALL_ALARM_TYPE : pkgValue.getMode();
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
                        for (int i = SmartHeartBeat.ALL_ALARM_TYPE; i < actions.size(); i += SmartHeartBeat.WAKEUP_ALARM_TYPE) {
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
                        for (int i = SmartHeartBeat.ALL_ALARM_TYPE; i < actions.size(); i += SmartHeartBeat.WAKEUP_ALARM_TYPE) {
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
                    return SmartHeartBeat.SHB_MODULE_SWITCHER;
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

        private HeartBeatDatabase() {
            this.pendingMap = new MultiMap();
            this.pendingMapForActions = new MultiMap();
            this.wakeupAdjustPkgsMap = new AdjustPackage();
            this.noneWakeupAdjustPkgsMap = new AdjustPackage();
            this.adjustPackageForActions = new AdjustPackageForActions();
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
            for (int i = SmartHeartBeat.ALL_ALARM_TYPE; i < actionList.size(); i += SmartHeartBeat.WAKEUP_ALARM_TYPE) {
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
            for (int i = SmartHeartBeat.ALL_ALARM_TYPE; i < actionList.size(); i += SmartHeartBeat.WAKEUP_ALARM_TYPE) {
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
                return SmartHeartBeat.SHB_MODULE_SWITCHER;
            } else {
                if (SmartHeartBeat.DEBUG_HEART_BEAT) {
                    Slog.d(SmartHeartBeat.TAG, "isPendingPackage, false, pkg:" + pkgName + ", action:" + action + ",not pending alarm with AlarmClockInfo");
                }
                return SmartHeartBeat.SHB_MODULE_SWITCHER;
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
                case SmartHeartBeat.ALARM_PENDING_PACKAGE_MSG /*101*/:
                    pendingPackageMessage(msg);
                case SmartHeartBeat.ALARM_ADJUST_PACKAGE_MSG /*102*/:
                    adjustPackageMessage(msg);
                default:
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

        private MessageInfo() {
        }
    }

    private static class MultiMap<K, V> {
        private HashMap<K, List<V>> store;

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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.SmartHeartBeat.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.SmartHeartBeat.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.SmartHeartBeat.<clinit>():void");
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
        this.mThread = new HandlerThread("HeartBeatHandlerThread");
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
        boolean pending = SHB_MODULE_SWITCHER;
        if (alarm == null) {
            Slog.d(TAG, "isPending, null == alarm");
            return SHB_MODULE_SWITCHER;
        }
        if (this.mDataMap.isPendingPackage(alarm)) {
            pending = true;
        }
        if (DEBUG_HEART_BEAT) {
            Slog.d(TAG, "isPending, ret:" + pending + ", alarm:" + alarm);
        }
        return pending;
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
                        if (pendingType == 0 || WAKEUP_ALARM_TYPE == pendingType) {
                            this.mDataMap.addPendingPackage(pkgName, ALL_ALARM_TYPE);
                            this.mDataMap.addPendingPackage(pkgName, NONE_WAKEUP_ALARM_TYPE);
                        }
                        if (pendingType == 0 || NONE_WAKEUP_ALARM_TYPE == pendingType) {
                            this.mDataMap.addPendingPackage(pkgName, WAKEUP_ALARM_TYPE);
                            this.mDataMap.addPendingPackage(pkgName, 3);
                        }
                    }
                } else if (actionList != null) {
                    this.mDataMap.removePendingPackageForActions(pkgName, actionList);
                } else {
                    if (pendingType == 0 || WAKEUP_ALARM_TYPE == pendingType) {
                        this.mDataMap.removePendingPackage(pkgName, ALL_ALARM_TYPE);
                        this.mDataMap.removePendingPackage(pkgName, NONE_WAKEUP_ALARM_TYPE);
                    }
                    if (pendingType == 0 || NONE_WAKEUP_ALARM_TYPE == pendingType) {
                        this.mDataMap.removePendingPackage(pkgName, WAKEUP_ALARM_TYPE);
                        this.mDataMap.removePendingPackage(pkgName, 3);
                    }
                }
            }
        }
        MessageInfo args = new MessageInfo();
        args.state = pending;
        args.pkgList = pkgList;
        Message msg = this.mHandler.obtainMessage();
        msg.what = ALARM_PENDING_PACKAGE_MSG;
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
        msg.what = ALARM_PENDING_PACKAGE_MSG;
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
            if (type == 0 || NONE_WAKEUP_ALARM_TYPE == type) {
                isAdjustAlarm = this.mDataMap.wakeupAdjustPkgsMap.containsAdjustKey(pkg);
            } else if (WAKEUP_ALARM_TYPE == type || 3 == type) {
                isAdjustAlarm = this.mDataMap.noneWakeupAdjustPkgsMap.containsAdjustKey(pkg);
            } else {
                if (DEBUG_HEART_BEAT) {
                    Slog.d(TAG, "isAdjustAlarm1: " + SHB_MODULE_SWITCHER);
                }
                return SHB_MODULE_SWITCHER;
            }
            if (DEBUG_HEART_BEAT) {
                Slog.d(TAG, "isAdjustAlarm: " + isAdjustAlarm + ",pkg:" + pkg);
            }
            return isAdjustAlarm;
        } else {
            if (DEBUG_HEART_BEAT) {
                Slog.d(TAG, "isAdjustAlarm: false,pkg: " + pkg + ",action: " + action + ",not adjustalarm with AlarmClockInfo");
            }
            return SHB_MODULE_SWITCHER;
        }
    }

    private long calAlarmWhenElapsed(Alarm a) {
        long interval = 0;
        int mode = ALARM_ADJUST_NONE;
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
            if (type == 0 || NONE_WAKEUP_ALARM_TYPE == type) {
                interval = this.mDataMap.wakeupAdjustPkgsMap.getAdjustPkgInterval(pkg);
                mode = this.mDataMap.wakeupAdjustPkgsMap.getAdjustPkgMode(pkg);
            } else if (WAKEUP_ALARM_TYPE != type && 3 != type) {
                return adjustWhenElapsed;
            } else {
                interval = this.mDataMap.noneWakeupAdjustPkgsMap.getAdjustPkgInterval(pkg);
                mode = this.mDataMap.noneWakeupAdjustPkgsMap.getAdjustPkgMode(pkg);
            }
        }
        if (0 == interval) {
            return adjustWhenElapsed;
        }
        if ((type == 0 || WAKEUP_ALARM_TYPE == type) && 0 > whenElapsed) {
            Slog.d(TAG, "Maybe the whenElapsed time is incorrect: pkg=" + pkg + " whenElapsed = " + whenElapsed);
            whenElapsed = SystemClock.elapsedRealtime();
        }
        switch (mode) {
            case ALL_ALARM_TYPE /*0*/:
            case WAKEUP_ALARM_TYPE /*1*/:
            case NONE_WAKEUP_ALARM_TYPE /*2*/:
                adjustWhenElapsed = ((((interval / 2) * ((long) mode)) + whenElapsed) / interval) * interval;
                if (mode == NONE_WAKEUP_ALARM_TYPE) {
                    if (NONE_WAKEUP_ALARM_TYPE == mode && adjustWhenElapsed - whenElapsed == interval) {
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
                        if (type == 0 || WAKEUP_ALARM_TYPE == type) {
                            this.mDataMap.wakeupAdjustPkgsMap.putAdjustPkg(pkg, interval, mode);
                        }
                        if (type == 0 || NONE_WAKEUP_ALARM_TYPE == type) {
                            this.mDataMap.noneWakeupAdjustPkgsMap.putAdjustPkg(pkg, interval, mode);
                        }
                    }
                } else if (actionList != null) {
                    this.mDataMap.adjustPackageForActions.removeAdjustPkg(pkg, actionList);
                } else {
                    if (type == 0 || WAKEUP_ALARM_TYPE == type) {
                        this.mDataMap.wakeupAdjustPkgsMap.removeAdjustPkg(pkg);
                    }
                    if (type == 0 || NONE_WAKEUP_ALARM_TYPE == type) {
                        this.mDataMap.noneWakeupAdjustPkgsMap.removeAdjustPkg(pkg);
                    }
                }
            }
            Message msg = this.mHandler.obtainMessage();
            msg.what = ALARM_ADJUST_PACKAGE_MSG;
            msg.obj = pkgList;
            this.mHandler.sendMessage(msg);
        }
    }

    public void removeAllAdjustAlarms() {
        Slog.i(TAG, "remove all adjust alarms.");
        this.mDataMap.wakeupAdjustPkgsMap.clearAdjustPkg();
        this.mDataMap.noneWakeupAdjustPkgsMap.clearAdjustPkg();
        this.mDataMap.adjustPackageForActions.clearAdjustPkg();
    }

    public void adjustAlarmIfNeeded(Alarm a) {
        if (a == null) {
            Slog.e(TAG, "adjustAlarmIfNeeded, alarm is null!");
            return;
        }
        long now = SystemClock.elapsedRealtime();
        synchronized (mLock) {
            if (!isPending(a)) {
                long whenElapsed = getAdjustAlarmWhenElapsed(a);
                if (whenElapsed != a.whenElapsed) {
                    long delta = whenElapsed - a.whenElapsed;
                    a.whenElapsed += delta;
                    a.when += delta;
                    a.maxWhenElapsed = Math.max(a.whenElapsed, a.maxWhenElapsed);
                    if (DEBUG_HEART_BEAT) {
                        Slog.i(TAG, "adjustAlarmLocked, is unify alarm: " + a.packageName + ", tag: " + a.statsTag + ", whenElapsed: " + a.whenElapsed + ", now: " + now);
                    }
                }
            } else if (now >= a.whenElapsed || a.whenElapsed - now <= WifiProCommonUtils.TRUSTED_DAYS_MS) {
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
                if (DEBUG_HEART_BEAT) {
                    Slog.i(TAG, "adjustAlarmIfNeeded, no need pending alarm: " + a.packageName + ", whenElapsed: " + a.whenElapsed + ", now: " + now);
                }
            }
        }
    }

    public void dump(PrintWriter pw) {
        synchronized (mLock) {
            this.mDataMap.dump(pw);
        }
    }
}
