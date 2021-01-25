package com.android.server;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.Slog;
import com.android.server.AlarmManagerService;
import com.android.server.AlarmManagerServiceExt;
import com.huawei.hiai.awareness.AwarenessInnerConstants;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class SmartHeartBeat extends SmartHeartBeatDummy {
    static final int ALARM_ADJUST_BACKWARD = 2;
    static final int ALARM_ADJUST_FORWARD = 0;
    static final int ALARM_ADJUST_MSG = 102;
    static final int ALARM_ADJUST_NEAR = 1;
    static final int ALARM_ADJUST_NONE = -1;
    static final int ALARM_EXEMPTION_MSG = 103;
    static final int ALARM_PENDING_MSG = 101;
    static final int ALL_ALARM_TYPE_BY_PKG = 0;
    static final int ALL_ALARM_TYPE_BY_PROC = 10;
    static final boolean DEBUG_HEART_BEAT = SystemProperties.getBoolean("persist.sys.shb.debug", false);
    private static final int DEFAULT_HASHMAP_SIZE = 16;
    private static final int DEFAULT_SIZE = 10;
    private static final Object LOCK = new Object();
    static final boolean SHB_MODULE_SWITCHER;
    static final String TAG = "SmartHeartBeat";
    private static AlarmManagerServiceExt sAlarmService;
    private static Context sContext;
    private static SmartHeartBeatDummy sInstance = null;
    private HeartBeatDatabase mDataMap;
    private Handler mHandler;
    private HandlerThread mThread = new HandlerThread("HeartBeatHandlerThread");

    static {
        boolean z = true;
        if (!SystemProperties.getBoolean("persist.sys.shb.switcher", true) || SystemProperties.getBoolean("ro.config.pg_disable_pg", false)) {
            z = false;
        }
        SHB_MODULE_SWITCHER = z;
    }

    /* access modifiers changed from: private */
    public enum ExemptionMode {
        SUBTYPE_ADD_PACKAGE_ALARM(1),
        SUBTYPE_REMOVE_PACKAGE_ALARM(2),
        SUBTYPE_ADD_PACKAGE_LIST_ALARM(3),
        SUBTYPE_REMOVE_PACKAGE_LIST_ALARM(4);
        
        private int value;

        private ExemptionMode(int value2) {
            this.value = value2;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private int getValue() {
            return this.value;
        }
    }

    /* access modifiers changed from: private */
    public class AdjustParameter {
        long mInterval;
        int mMode;
        int mType;

        AdjustParameter(int type, long interval, int mode) {
            this.mType = type;
            this.mInterval = interval;
            this.mMode = mode;
        }
    }

    private SmartHeartBeat() {
        this.mThread.start();
        this.mHandler = new HeartBeatHandler(this.mThread.getLooper());
        this.mDataMap = new HeartBeatDatabase();
        Slog.i(TAG, "SmartHeartBeat getInstance init success!");
    }

    public static synchronized SmartHeartBeatDummy getInstance(Context context, AlarmManagerServiceExt service) {
        SmartHeartBeatDummy smartHeartBeatDummy;
        synchronized (SmartHeartBeat.class) {
            if (sInstance == null) {
                if (SHB_MODULE_SWITCHER) {
                    sContext = context;
                    sAlarmService = service;
                    Slog.i(TAG, "getInstance new");
                    sInstance = new SmartHeartBeat();
                } else {
                    Slog.i(TAG, "SmartHeartBeat is turn off !");
                    sInstance = new SmartHeartBeatDummy();
                }
            }
            if (DEBUG_HEART_BEAT) {
                Slog.i(TAG, "getInstance return");
            }
            smartHeartBeatDummy = sInstance;
        }
        return smartHeartBeatDummy;
    }

    private boolean isPending(AlarmManagerService.Alarm alarm) {
        boolean pending = false;
        if (alarm == null) {
            Slog.d(TAG, "isPending, alarm is null");
            return false;
        }
        if (this.mDataMap.isPendingAlarm(alarm)) {
            pending = true;
        }
        if (DEBUG_HEART_BEAT) {
            Slog.d(TAG, "isPending, ret:" + pending + ", alarm:" + alarm);
        }
        return pending;
    }

    @Override // com.android.server.SmartHeartBeatDummy
    public boolean shouldPendingAlarm(AlarmManagerServiceExt.AlarmEx alarmEx) {
        boolean isPending;
        AlarmManagerService.Alarm alarm = null;
        if (alarmEx != null) {
            alarm = alarmEx.getAlarm();
        }
        synchronized (LOCK) {
            isPending = isPending(alarm);
        }
        return isPending;
    }

    @Override // com.android.server.SmartHeartBeatDummy
    public void setAlarmsPending(List<String> pkgList, List<String> actionList, boolean pending, int pendingType) {
        if (pendingType == 10) {
            setAlarmsPendingByPidsInner(pkgList, actionList, pending, pendingType);
        } else if (pendingType == 0) {
            setAlarmsPendingInner(pkgList, actionList, pending, pendingType);
        } else {
            Slog.w(TAG, "unknown pending " + pkgList + " by " + pendingType);
        }
    }

    private void setAlarmsPendingInner(List<String> pkgList, List<String> actionList, boolean pending, int pendingType) {
        if (pkgList == null || pkgList.size() <= 0) {
            Slog.d(TAG, "setAlarmsPending, pkgList=" + pkgList);
            return;
        }
        if (DEBUG_HEART_BEAT) {
            Slog.i(TAG, "setAlarmsPending, pkgList=" + pkgList + ", action=" + actionList + ", pending=" + pending + ", pendingType=" + pendingType);
        } else {
            Slog.d(TAG, "setAlarmsPending ...");
        }
        synchronized (LOCK) {
            for (String pkgName : pkgList) {
                if (pending) {
                    if (actionList != null) {
                        this.mDataMap.addPendingPackageForActions(pkgName, actionList);
                    } else {
                        this.mDataMap.addPendingPackage(pkgName);
                    }
                } else if (actionList != null) {
                    this.mDataMap.removePendingPackageForActions(pkgName, actionList);
                } else {
                    this.mDataMap.removePendingPackage(pkgName);
                }
            }
        }
        MessageInfo args = new MessageInfo();
        args.state = pending;
        args.byPkgs = true;
        args.pkgList = pkgList;
        Message msg = this.mHandler.obtainMessage();
        msg.what = 101;
        msg.obj = args;
        this.mHandler.sendMessage(msg);
    }

    private void setAlarmsPendingByPidsInner(List<String> pkgList, List<String> procList, boolean pending, int pendingType) {
        if (pkgList == null || pkgList.size() <= 0 || procList == null || procList.size() <= 0 || procList.size() != pkgList.size()) {
            Slog.w(TAG, "pending alarm , invaild para");
            return;
        }
        if (DEBUG_HEART_BEAT) {
            Slog.i(TAG, "pending alarm, pkgs=" + pkgList + ", procs=" + procList + ", pending=" + pending + ", pendingType=" + pendingType);
        } else {
            Slog.d(TAG, "pending alarm by p ...");
        }
        synchronized (LOCK) {
            int size = pkgList.size();
            for (int i = 0; i < size; i++) {
                String pkg = pkgList.get(i);
                String proc = procList.get(i);
                if (pending) {
                    this.mDataMap.addPendingProc(pkg, proc);
                } else {
                    this.mDataMap.removePendingProc(pkg, proc);
                }
            }
        }
        MessageInfo args = new MessageInfo();
        args.state = pending;
        args.byPkgs = false;
        args.pkgList = pkgList;
        args.procList = procList;
        Message msg = this.mHandler.obtainMessage();
        msg.what = 101;
        msg.obj = args;
        this.mHandler.sendMessage(msg);
    }

    @Override // com.android.server.SmartHeartBeatDummy
    public void removeAllPendingAlarms() {
        synchronized (LOCK) {
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

    private boolean isAdjustAlarm(AlarmManagerService.Alarm alarm) {
        String pkg = alarm.packageName;
        String action = getActionByClearCallingIdentity(alarm.operation);
        int type = alarm.type;
        if (this.mDataMap.adjustPackageForActions.containsAdjustKey(pkg, action)) {
            if (!DEBUG_HEART_BEAT) {
                return true;
            }
            Slog.d(TAG, "isAdjustAlarm: true,pkg: " + pkg + ",action: " + action);
            return true;
        } else if (type != 0 || alarm.alarmClock == null) {
            boolean isAdjustAlarm = this.mDataMap.adjustPkgsMap.containsAdjustKey(pkg);
            if (!isAdjustAlarm && alarm.procName != null) {
                isAdjustAlarm = this.mDataMap.isAdjustAlarmByProc(pkg, alarm.procName);
            }
            if (!isAdjustAlarm) {
                isAdjustAlarm = isAdjustBySystemUnify(isAdjustAlarm, alarm, action);
            }
            if (DEBUG_HEART_BEAT) {
                Slog.d(TAG, "isAdjustAlarm: " + isAdjustAlarm + ",pkg:" + pkg + ",proc:" + alarm.procName);
            }
            return isAdjustAlarm;
        } else if (!DEBUG_HEART_BEAT) {
            return false;
        } else {
            Slog.d(TAG, "isAdjustAlarm: false,pkg: " + pkg + ",action: " + action + ",not adjustalarm with AlarmClockInfo");
            return false;
        }
    }

    private boolean isAdjustBySystemUnify(boolean isAdjustAlarm, AlarmManagerService.Alarm alarm, String action) {
        String pkg = alarm.packageName;
        if ("android".equals(pkg) && action == null && alarm.listenerTag == null) {
            if (DEBUG_HEART_BEAT) {
                Slog.d(TAG, "pkg is android, action is null and listenerTag is null!");
            }
            return false;
        } else if (this.mDataMap.adjustPkgsMap.containsExemptionPkg(pkg) || this.mDataMap.adjustPackageForActions.containsExemptionPkgForAction(pkg, action) || this.mDataMap.adjustPackageForActions.containsExemptionPkgForAction(pkg, alarm.listenerTag)) {
            if (DEBUG_HEART_BEAT) {
                Slog.d(TAG, "isAdjustAlarm by exemption: true,pkg: " + pkg + ",action: " + action + ",listenerTag" + alarm.listenerTag);
            }
            return false;
        } else if (!this.mDataMap.adjustPkgsMap.containsAdjustKey("*")) {
            return isAdjustAlarm;
        } else {
            if (!DEBUG_HEART_BEAT) {
                return true;
            }
            Slog.d(TAG, "isAdjustAlarm: true, pkg: " + pkg + " because all pkg config adjust");
            return true;
        }
    }

    private long calAlarmWhenElapsed(AlarmManagerService.Alarm alarm) {
        long interval = 0;
        int mode = -1;
        String pkg = alarm.packageName;
        String action = getActionByClearCallingIdentity(alarm.operation);
        AdjustValue adjustValue = this.mDataMap.adjustPackageForActions.getAdjustValue(pkg, action);
        if (adjustValue != null) {
            interval = adjustValue.interval;
            mode = adjustValue.mode;
        }
        if (interval == 0) {
            interval = this.mDataMap.adjustPkgsMap.getAdjustPkgInterval(pkg);
            mode = this.mDataMap.adjustPkgsMap.getAdjustPkgMode(pkg);
        }
        if (interval == 0 && alarm.procName != null && this.mDataMap.isAdjustAlarmByProc(pkg, alarm.procName)) {
            interval = this.mDataMap.getAdjustProc(pkg).getAdjustProcInterval(alarm.procName);
            mode = this.mDataMap.getAdjustProc(pkg).getAdjustProcMode(alarm.procName);
            if (DEBUG_HEART_BEAT) {
                Slog.d(TAG, "calAlarmWhenElapsed, pkg:" + pkg + ",proc:" + alarm.procName + ",interval:" + interval + ",mode:" + mode);
            }
        }
        if (interval == 0) {
            interval = this.mDataMap.adjustPkgsMap.getAdjustPkgInterval("*");
            mode = this.mDataMap.adjustPkgsMap.getAdjustPkgMode("*");
        }
        if (interval == 0) {
            return alarm.whenElapsed;
        }
        long adjustWhenElapsed = calAdjustWhenElapsedByMode(alarm, mode, interval);
        if (DEBUG_HEART_BEAT) {
            Slog.d(TAG, "calAlarmWhenElapsed pkg: " + pkg + ",proc:" + alarm.procName + ", action: " + action + ", whenElapsed: " + alarm.whenElapsed + ", adjusted WhenElapsed: " + adjustWhenElapsed + ", interval: " + interval + ", mode: " + mode);
        }
        return adjustWhenElapsed;
    }

    private long calAdjustWhenElapsedByMode(AlarmManagerService.Alarm alarm, int mode, long interval) {
        String pkg = alarm.packageName;
        long whenElapsed = alarm.whenElapsed;
        long adjustWhenElapsed = alarm.whenElapsed;
        int type = alarm.type;
        if ((type == 0 || type == 1) && whenElapsed < 0) {
            Slog.d(TAG, "Maybe the whenElapsed time is incorrect: pkg=" + pkg + " whenElapsed = " + whenElapsed);
            whenElapsed = SystemClock.elapsedRealtime();
        }
        if (mode != 0 && mode != 1 && mode != 2) {
            return adjustWhenElapsed;
        }
        long adjustWhenElapsed2 = ((((interval / 2) * ((long) mode)) + whenElapsed) / interval) * interval;
        if (mode == 2) {
            return (mode == 2 && adjustWhenElapsed2 - whenElapsed == interval) ? whenElapsed : adjustWhenElapsed2;
        }
        if (adjustWhenElapsed2 < SystemClock.elapsedRealtime()) {
            return adjustWhenElapsed2 + interval;
        }
        return adjustWhenElapsed2;
    }

    private long getAdjustAlarmWhenElapsed(AlarmManagerService.Alarm alarm) {
        long val = isAdjustAlarm(alarm) ? calAlarmWhenElapsed(alarm) : alarm.whenElapsed;
        if (DEBUG_HEART_BEAT) {
            Slog.d(TAG, "getAdjustAlarmWhenElapsed, val: " + val + ", pkg: " + alarm.packageName + ", proc: " + alarm.procName + ", whenElapsed: " + alarm.whenElapsed + ", type: " + alarm.type);
        }
        return val;
    }

    /* access modifiers changed from: private */
    public static String getActionByClearCallingIdentity(PendingIntent operation) {
        Intent alarmIntent;
        long identity = Binder.clearCallingIdentity();
        String action = null;
        if (!(operation == null || (alarmIntent = operation.getIntent()) == null)) {
            action = alarmIntent.getAction();
        }
        Binder.restoreCallingIdentity(identity);
        return action;
    }

    @Override // com.android.server.SmartHeartBeatDummy
    public void setAlarmsAdjust(List<String> pkgList, List<String> actionList, boolean adjust, int type, long interval, int mode) {
        if (type == 10) {
            setAlarmsAdjustByPidsInner(pkgList, actionList, adjust, new AdjustParameter(type, interval, mode));
        } else if (type == 0) {
            setAlarmsAdjustInner(pkgList, actionList, adjust, new AdjustParameter(type, interval, mode));
        } else {
            Slog.w(TAG, "unknown adjust " + pkgList + " by " + type);
        }
    }

    private void setAlarmsAdjustInner(List<String> pkgList, List<String> actionList, boolean adjust, AdjustParameter parameter) {
        Iterator<String> it;
        if (pkgList == null || pkgList.size() <= 0) {
            Slog.i(TAG, "setAlarmsAdjust, pkgList: " + pkgList);
            return;
        }
        int type = parameter.mType;
        long interval = parameter.mInterval;
        int mode = parameter.mMode;
        if (DEBUG_HEART_BEAT) {
            Slog.i(TAG, "setAlarmsAdjust, pkgList=" + pkgList + ", action=" + actionList + ", adjust=" + adjust + ", type=" + type + ", interval=" + interval + ", mode=" + mode);
        } else {
            Slog.d(TAG, "setAlarmsAdjust ...");
        }
        synchronized (LOCK) {
            Iterator<String> it2 = pkgList.iterator();
            while (it2.hasNext()) {
                String pkg = it2.next();
                if (!adjust) {
                    it = it2;
                    if (actionList != null) {
                        this.mDataMap.adjustPackageForActions.removeAdjustPkg(pkg, actionList);
                        it2 = it;
                    } else {
                        this.mDataMap.adjustPkgsMap.removeAdjustPkg(pkg);
                    }
                } else if (interval <= 0) {
                    Slog.d(TAG, "setAlarmsAdjust interval must be greater than 0.");
                    return;
                } else if (actionList != null) {
                    this.mDataMap.adjustPackageForActions.putAdjustPkg(pkg, actionList, interval, mode);
                    it2 = it2;
                } else {
                    it = it2;
                    this.mDataMap.adjustPkgsMap.putAdjustPkg(pkg, interval, mode);
                }
                it2 = it;
            }
            MessageInfo args = new MessageInfo();
            args.byPkgs = true;
            args.pkgList = pkgList;
            Message msg = this.mHandler.obtainMessage();
            msg.what = 102;
            msg.obj = args;
            this.mHandler.sendMessage(msg);
        }
    }

    private void setAlarmsAdjustByPidsInner(List<String> pkgList, List<String> procList, boolean adjust, AdjustParameter parameter) {
        if (pkgList == null || pkgList.size() <= 0 || procList == null || procList.size() <= 0 || procList.size() != pkgList.size()) {
            Slog.w(TAG, "adjust alarm , invaild para");
            return;
        }
        int type = parameter.mType;
        long interval = parameter.mInterval;
        int mode = parameter.mMode;
        if (DEBUG_HEART_BEAT) {
            Slog.i(TAG, "adjust alarm by p, pkgs=" + pkgList + ", procs=" + procList + ", adjust=" + adjust + ", type=" + type + ", interval=" + interval + ", mode=" + mode);
        } else {
            Slog.d(TAG, "adjust alarm by p ...");
        }
        synchronized (LOCK) {
            int size = pkgList.size();
            for (int i = 0; i < size; i++) {
                String pkg = pkgList.get(i);
                String proc = procList.get(i);
                if (adjust) {
                    if (interval <= 0) {
                        Slog.d(TAG, "setAlarmsAdjust interval must be greater than 0.");
                        return;
                    }
                    this.mDataMap.getAdjustProc(pkg).putAdjustProc(proc, interval, mode);
                } else if (this.mDataMap.getAdjustProc(pkg).removeAdjustProc(proc) <= 0) {
                    this.mDataMap.removeAdjustProc(pkg);
                }
            }
            MessageInfo args = new MessageInfo();
            args.byPkgs = false;
            args.pkgList = pkgList;
            args.procList = procList;
            Message msg = this.mHandler.obtainMessage();
            msg.what = 102;
            msg.obj = args;
            this.mHandler.sendMessage(msg);
        }
    }

    @Override // com.android.server.SmartHeartBeatDummy
    public void removeAllAdjustAlarms() {
        Slog.i(TAG, "remove all adjust alarms.");
        synchronized (LOCK) {
            this.mDataMap.adjustPkgsMap.clearAdjustPkg();
            this.mDataMap.adjustPackageForActions.clearAdjustPkg();
            this.mDataMap.adjustProcMap.clear();
        }
    }

    @Override // com.android.server.SmartHeartBeatDummy
    public void adjustAlarmIfNeeded(AlarmManagerServiceExt.AlarmEx alarmEx) {
        if (alarmEx == null) {
            Slog.e(TAG, "adjustAlarmIfNeeded, alarm is null!");
            return;
        }
        AlarmManagerService.Alarm alarm = alarmEx.getAlarm();
        long now = SystemClock.elapsedRealtime();
        synchronized (LOCK) {
            long delay = 259200000;
            if (now < alarm.whenElapsed && alarm.whenElapsed - now > 259200000) {
                if (DEBUG_HEART_BEAT) {
                    Slog.i(TAG, "adjustAlarmIfNeeded, no need change alarm: " + alarm.packageName + ", whenElapsed: " + alarm.whenElapsed + ", now: " + now);
                }
            } else if (isPending(alarm)) {
                if (now >= alarm.whenElapsed) {
                    delay = 259200000 + (now - alarm.whenElapsed);
                }
                alarm.when += delay;
                alarm.whenElapsed += delay;
                alarm.maxWhenElapsed += delay;
                if (DEBUG_HEART_BEAT) {
                    Slog.i(TAG, "adjustAlarmIfNeeded, is pending alarm: " + alarm.packageName + ", tag: " + alarm.statsTag + ", whenElapsed: " + alarm.whenElapsed + ", now: " + now);
                }
            } else {
                long whenElapsed = getAdjustAlarmWhenElapsed(alarm);
                if (whenElapsed != alarm.whenElapsed) {
                    long delta = whenElapsed - alarm.whenElapsed;
                    alarm.whenElapsed += delta;
                    alarm.when += delta;
                    alarm.maxWhenElapsed = alarm.windowLength >= 0 ? alarm.windowLength + whenElapsed : whenElapsed;
                    if (DEBUG_HEART_BEAT) {
                        Slog.i(TAG, "adjustAlarmLocked, is unify alarm: " + alarm.packageName + ", tag: " + alarm.statsTag + ", whenElapsed: " + alarm.whenElapsed + ", now: " + now);
                    }
                }
            }
        }
    }

    @Override // com.android.server.SmartHeartBeatDummy
    public void setAlarmExemption(List<String> pkgs, List<String> actionList, int type) {
        if (type == ExemptionMode.SUBTYPE_ADD_PACKAGE_LIST_ALARM.getValue() || type == ExemptionMode.SUBTYPE_REMOVE_PACKAGE_LIST_ALARM.getValue()) {
            setAlarmExemptionInner(pkgs, type);
        } else if (type == ExemptionMode.SUBTYPE_ADD_PACKAGE_ALARM.getValue() || type == ExemptionMode.SUBTYPE_REMOVE_PACKAGE_ALARM.getValue()) {
            setAlarmExemptionByActionInner(pkgs, actionList, type);
        } else {
            Slog.w(TAG, "unknown Exemption " + pkgs + " by " + type);
        }
    }

    private void setAlarmExemptionInner(List<String> pkgs, int type) {
        boolean isSet = type == ExemptionMode.SUBTYPE_ADD_PACKAGE_LIST_ALARM.getValue();
        if (pkgs == null || pkgs.isEmpty()) {
            Slog.i(TAG, "setAlarmExemptionInner, pkgs: " + pkgs);
            return;
        }
        if (DEBUG_HEART_BEAT) {
            Slog.i(TAG, "setAlarmExemptionInner, pkgs = " + pkgs);
        } else {
            Slog.d(TAG, "setAlarmExemptionInner ...");
        }
        synchronized (LOCK) {
            for (String pkg : pkgs) {
                if (isSet) {
                    this.mDataMap.adjustPkgsMap.putExemptionPkg(pkg);
                    this.mDataMap.adjustPkgsMap.removeAdjustPkg(pkg);
                    this.mDataMap.adjustPackageForActions.removeAdjustPkg(pkg, null);
                } else {
                    this.mDataMap.adjustPkgsMap.removeExemptionPkg(pkg);
                }
            }
        }
        MessageInfo args = new MessageInfo();
        args.byPkgs = true;
        args.pkgList = pkgs;
        Message msg = this.mHandler.obtainMessage();
        msg.what = 103;
        msg.obj = args;
        this.mHandler.sendMessage(msg);
    }

    private void setAlarmExemptionByActionInner(List<String> pkgs, List<String> actionList, int type) {
        boolean isSet = type == ExemptionMode.SUBTYPE_ADD_PACKAGE_ALARM.getValue();
        if (pkgs == null || pkgs.isEmpty()) {
            Slog.i(TAG, "setAlarmExemptionByActionInner, pkgs: " + pkgs);
            return;
        }
        String pkg = pkgs.get(0);
        if (DEBUG_HEART_BEAT) {
            Slog.i(TAG, "setAlarmExemptionByActionInner, pkg = " + pkg + ", isSet = " + isSet);
            if (actionList == null || actionList.isEmpty()) {
                Slog.i(TAG, "setAlarmExemptionByActionInner, action is null");
            } else {
                Slog.i(TAG, "setAlarmExemptionByActionInner, actions = " + actionList);
            }
        } else {
            Slog.d(TAG, "setAlarmExemptionByActionInner ...");
        }
        synchronized (LOCK) {
            if (isSet) {
                if (actionList != null) {
                    if (!actionList.isEmpty()) {
                        this.mDataMap.adjustPackageForActions.putExemptionPkg(pkg, actionList);
                        this.mDataMap.adjustPackageForActions.removeAdjustPkg(pkg, actionList);
                    }
                }
                this.mDataMap.adjustPkgsMap.putExemptionPkg(pkg);
                this.mDataMap.adjustPkgsMap.removeAdjustPkg(pkg);
                this.mDataMap.adjustPackageForActions.removeAdjustPkg(pkg, null);
            } else {
                if (actionList != null) {
                    if (!actionList.isEmpty()) {
                        this.mDataMap.adjustPackageForActions.removeExemptionPkg(pkg, actionList);
                    }
                }
                this.mDataMap.adjustPkgsMap.removeExemptionPkg(pkg);
            }
        }
        MessageInfo args = new MessageInfo();
        args.byPkgs = true;
        args.pkgList = Arrays.asList(pkg);
        Message msg = this.mHandler.obtainMessage();
        msg.what = 103;
        msg.obj = args;
        this.mHandler.sendMessage(msg);
    }

    @Override // com.android.server.SmartHeartBeatDummy
    public void dump(PrintWriter pw) {
        synchronized (LOCK) {
            this.mDataMap.dump(pw);
        }
    }

    private static class HeartBeatHandler extends Handler {
        HeartBeatHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 101:
                    pendingAlarmMessage(msg);
                    return;
                case 102:
                    adjustAlarmMessage(msg);
                    return;
                case 103:
                    setExemptionAlarmMessage(msg);
                    return;
                default:
                    return;
            }
        }

        private void adjustAlarmMessage(Message msg) {
            MessageInfo args = (MessageInfo) msg.obj;
            if (args == null) {
                Slog.d(SmartHeartBeat.TAG, "adjust alarm message, args is null");
                return;
            }
            if (SmartHeartBeat.DEBUG_HEART_BEAT) {
                Slog.d(SmartHeartBeat.TAG, "adjust alarm message, byPkg: " + args.byPkgs + " pkg: " + args.pkgList + " proc: " + args.procList);
            }
            synchronized (SmartHeartBeat.sAlarmService.getLock()) {
                SmartHeartBeat.sAlarmService.rebatchPkgAlarmsLocked(args.byPkgs, args.pkgList, args.procList);
            }
        }

        private void pendingAlarmMessage(Message msg) {
            MessageInfo args = null;
            if (msg.obj instanceof MessageInfo) {
                args = (MessageInfo) msg.obj;
            }
            if (args == null) {
                Slog.d(SmartHeartBeat.TAG, "pendingAlarmMessage, args is null");
                return;
            }
            boolean pending = args.state;
            if (SmartHeartBeat.DEBUG_HEART_BEAT) {
                Slog.d(SmartHeartBeat.TAG, "pendingAlarmMessage, pending: " + pending + ", byPkgs: " + args.byPkgs + " pkg:" + args.pkgList);
            }
            synchronized (SmartHeartBeat.sAlarmService.getLock()) {
                if (args.pkgList == null) {
                    SmartHeartBeat.sAlarmService.rebatchAllAlarmsLocked(true);
                } else {
                    SmartHeartBeat.sAlarmService.rebatchPkgAlarmsLocked(args.byPkgs, args.pkgList, args.procList);
                }
            }
        }

        private void setExemptionAlarmMessage(Message msg) {
            MessageInfo args = (MessageInfo) msg.obj;
            if (args == null) {
                Slog.d(SmartHeartBeat.TAG, "setExemptionAlarmMessage, args is null");
                return;
            }
            if (SmartHeartBeat.DEBUG_HEART_BEAT) {
                Slog.d(SmartHeartBeat.TAG, "set Exemption alarm message, byPkg: " + args.byPkgs + " pkg: " + args.pkgList);
            }
            synchronized (SmartHeartBeat.sAlarmService.getLock()) {
                SmartHeartBeat.sAlarmService.rebatchPkgAlarmsLocked(args.byPkgs, args.pkgList, args.procList);
            }
        }
    }

    /* access modifiers changed from: private */
    public static class MessageInfo {
        boolean byPkgs;
        List<String> pkgList;
        List<String> procList;
        boolean state;

        private MessageInfo() {
        }
    }

    /* access modifiers changed from: private */
    public static class HeartBeatDatabase {
        final AdjustPackageForActions adjustPackageForActions;
        final AdjustPackage adjustPkgsMap;
        final HashMap<String, AdjustProc> adjustProcMap;
        private final Object lock;
        private final ArrayList<String> pendingMapByPkg;
        private final MultiMap<String, String> pendingMapByProc;
        private final MultiMap<String, String> pendingMapForActions;

        private HeartBeatDatabase() {
            this.adjustPkgsMap = new AdjustPackage();
            this.adjustProcMap = new HashMap<>(16);
            this.adjustPackageForActions = new AdjustPackageForActions();
            this.pendingMapByPkg = new ArrayList<>(10);
            this.pendingMapByProc = new MultiMap<>();
            this.pendingMapForActions = new MultiMap<>();
            this.lock = new Object();
        }

        /* access modifiers changed from: package-private */
        public void addPendingPackage(String pkgName) {
            if (!this.pendingMapByPkg.contains(pkgName)) {
                if (SmartHeartBeat.DEBUG_HEART_BEAT) {
                    Slog.d(SmartHeartBeat.TAG, "addPendingPackage, pkgName: " + pkgName);
                }
                this.pendingMapByPkg.add(pkgName);
            }
        }

        /* access modifiers changed from: package-private */
        public void addPendingProc(String pkg, String proc) {
            this.pendingMapByProc.put(pkg, proc);
            if (SmartHeartBeat.DEBUG_HEART_BEAT) {
                Slog.d(SmartHeartBeat.TAG, "addPendingProc, pkg: " + pkg + " proc: " + proc);
            }
        }

        /* access modifiers changed from: package-private */
        public void addPendingPackageForActions(String pkgName, List<String> actionList) {
            if (SmartHeartBeat.DEBUG_HEART_BEAT) {
                Slog.d(SmartHeartBeat.TAG, "addPendingPackageForActions, pkgName: " + pkgName + ", actionList: " + actionList);
            }
            for (String action : actionList) {
                this.pendingMapForActions.put(pkgName, action);
            }
        }

        /* access modifiers changed from: package-private */
        public void removePendingPackage(String pkgName) {
            this.pendingMapByPkg.remove(pkgName);
            if (SmartHeartBeat.DEBUG_HEART_BEAT) {
                Slog.d(SmartHeartBeat.TAG, "removePendingPackage, pkgName: " + pkgName);
            }
        }

        /* access modifiers changed from: package-private */
        public void removePendingProc(String pkg, String proc) {
            this.pendingMapByProc.remove(pkg, proc);
            if (SmartHeartBeat.DEBUG_HEART_BEAT) {
                Slog.d(SmartHeartBeat.TAG, "removePendingProc, pkg: " + pkg + " proc: " + proc);
            }
        }

        /* access modifiers changed from: package-private */
        public void removePendingPackageForActions(String pkgName, List<String> actionList) {
            if (SmartHeartBeat.DEBUG_HEART_BEAT) {
                Slog.d(SmartHeartBeat.TAG, "removePendingPackageForActions, pkgName: " + pkgName + ", actionList: " + actionList);
            }
            for (String action : actionList) {
                this.pendingMapForActions.remove(pkgName, action);
            }
        }

        /* access modifiers changed from: package-private */
        public void removeAllPendingPackages() {
            this.pendingMapByPkg.clear();
            this.pendingMapForActions.clear();
            this.pendingMapByProc.clear();
            if (SmartHeartBeat.DEBUG_HEART_BEAT) {
                Slog.d(SmartHeartBeat.TAG, "removeAllPendingPackages");
            }
        }

        /* access modifiers changed from: package-private */
        public boolean isPendingAlarmByProc(String pkg, String proc) {
            List<String> procs = this.pendingMapByProc.getAll(pkg);
            return procs != null && procs.contains(proc);
        }

        /* access modifiers changed from: package-private */
        public boolean isAdjustAlarmByProc(String pkg, String proc) {
            AdjustProc adjustProc = this.adjustProcMap.get(pkg);
            if (adjustProc != null) {
                return adjustProc.containsAdjustKey(proc);
            }
            return false;
        }

        /* access modifiers changed from: package-private */
        public boolean isPendingAlarm(AlarmManagerService.Alarm alarm) {
            String pkgName = alarm.packageName;
            String action = SmartHeartBeat.getActionByClearCallingIdentity(alarm.operation);
            List<String> actionList = this.pendingMapForActions.getAll(pkgName);
            if (actionList.size() > 0 && action != null && actionList.contains(action)) {
                if (SmartHeartBeat.DEBUG_HEART_BEAT) {
                    Slog.d(SmartHeartBeat.TAG, "isPendingAlarm, true, pkg:" + pkgName + ", action:" + action);
                }
                return true;
            } else if (alarm.type == 0 && alarm.alarmClock != null) {
                Slog.d(SmartHeartBeat.TAG, "isPendingAlarm, false, pkg:" + pkgName + ", action:" + action + ",not pending alarm with AlarmClockInfo");
                return false;
            } else if (this.pendingMapByPkg.contains(pkgName)) {
                if (SmartHeartBeat.DEBUG_HEART_BEAT) {
                    Slog.d(SmartHeartBeat.TAG, "isPendingAlarm, true, pkg:" + pkgName);
                }
                return true;
            } else if (alarm.procName == null || !isPendingAlarmByProc(pkgName, alarm.procName)) {
                return false;
            } else {
                if (SmartHeartBeat.DEBUG_HEART_BEAT) {
                    Slog.d(SmartHeartBeat.TAG, "isPendingAlarm, true, pkg:" + pkgName + " proc:" + alarm.procName);
                }
                return true;
            }
        }

        /* access modifiers changed from: package-private */
        public AdjustProc getAdjustProc(String pkgName) {
            AdjustProc ap = this.adjustProcMap.get(pkgName);
            if (ap != null) {
                return ap;
            }
            AdjustProc adjustProc = new AdjustProc();
            this.adjustProcMap.put(pkgName, adjustProc);
            return adjustProc;
        }

        /* access modifiers changed from: package-private */
        public void removeAdjustProc(String pkgName) {
            this.adjustProcMap.remove(pkgName);
            if (SmartHeartBeat.DEBUG_HEART_BEAT) {
                Slog.d(SmartHeartBeat.TAG, "removeAdjustProc, pkg:" + pkgName);
            }
        }

        /* access modifiers changed from: package-private */
        public void dump(PrintWriter pw) {
            synchronized (this.lock) {
                pw.println("=======dumping shb bg");
                pw.println("===pending pkg:" + this.pendingMapByPkg);
                pw.println();
                pw.println("===pending proc:");
                this.pendingMapByProc.dump(pw);
                pw.println();
                pw.println("===pending actions:");
                this.pendingMapForActions.dump(pw);
                pw.println();
                pw.println("===adjust pkg:");
                this.adjustPkgsMap.dump(pw);
                pw.println();
                pw.println("===adjust actions:");
                this.adjustPackageForActions.dump(pw);
                pw.println("===adjust proc:");
                synchronized (this.lock) {
                    for (Map.Entry<String, AdjustProc> entry : this.adjustProcMap.entrySet()) {
                        pw.print("{" + ((Object) entry.getKey()) + AwarenessInnerConstants.COLON_KEY);
                        entry.getValue().dump(pw);
                        pw.println("}");
                    }
                }
                pw.println();
                pw.println("=======dumping shb end");
            }
        }

        /* access modifiers changed from: private */
        public class AdjustPackage {
            private final HashMap<String, AdjustValue> adjustMap;
            private final ArrayList<String> exemptionPkg;

            private AdjustPackage() {
                this.adjustMap = new HashMap<>(16);
                this.exemptionPkg = new ArrayList<>(10);
            }

            /* access modifiers changed from: package-private */
            public void clearAdjustPkg() {
                synchronized (HeartBeatDatabase.this.lock) {
                    this.adjustMap.clear();
                }
            }

            /* access modifiers changed from: package-private */
            public void clearExemptionPkg() {
                synchronized (HeartBeatDatabase.this.lock) {
                    this.exemptionPkg.clear();
                }
            }

            /* access modifiers changed from: package-private */
            public void putAdjustPkg(String pkg, long interval, int mode) {
                synchronized (HeartBeatDatabase.this.lock) {
                    this.adjustMap.put(pkg, new AdjustValue(interval, mode));
                }
            }

            /* access modifiers changed from: package-private */
            public void putExemptionPkg(String pkg) {
                synchronized (HeartBeatDatabase.this.lock) {
                    if (!this.exemptionPkg.contains(pkg)) {
                        this.exemptionPkg.add(pkg);
                    }
                }
            }

            /* access modifiers changed from: package-private */
            public long getAdjustPkgInterval(String pkg) {
                long interval;
                synchronized (HeartBeatDatabase.this.lock) {
                    AdjustValue pkgValue = this.adjustMap.get(pkg);
                    interval = pkgValue == null ? 0 : pkgValue.getInterval();
                }
                return interval;
            }

            /* access modifiers changed from: package-private */
            public int getAdjustPkgMode(String pkg) {
                int mode;
                synchronized (HeartBeatDatabase.this.lock) {
                    AdjustValue pkgValue = this.adjustMap.get(pkg);
                    mode = pkgValue == null ? 0 : pkgValue.getMode();
                }
                return mode;
            }

            /* access modifiers changed from: package-private */
            public void removeAdjustPkg(String pkg) {
                synchronized (HeartBeatDatabase.this.lock) {
                    this.adjustMap.remove(pkg);
                }
            }

            /* access modifiers changed from: package-private */
            public void removeExemptionPkg(String pkg) {
                synchronized (HeartBeatDatabase.this.lock) {
                    this.exemptionPkg.remove(pkg);
                }
            }

            /* access modifiers changed from: package-private */
            public boolean containsAdjustKey(String pkg) {
                boolean containsKey;
                synchronized (HeartBeatDatabase.this.lock) {
                    containsKey = this.adjustMap.containsKey(pkg);
                }
                return containsKey;
            }

            /* access modifiers changed from: package-private */
            public boolean containsExemptionPkg(String pkg) {
                boolean contains;
                synchronized (HeartBeatDatabase.this.lock) {
                    contains = this.exemptionPkg.contains(pkg);
                }
                return contains;
            }

            /* access modifiers changed from: package-private */
            public void dump(PrintWriter pw) {
                synchronized (HeartBeatDatabase.this.lock) {
                    for (Map.Entry<String, AdjustValue> entry : this.adjustMap.entrySet()) {
                        pw.print(((Object) entry.getKey()) + AwarenessInnerConstants.COLON_KEY);
                        pw.println(entry.getValue());
                    }
                }
            }
        }

        /* access modifiers changed from: private */
        public class AdjustProc {
            private final HashMap<String, AdjustValue> adjustMap = new HashMap<>(16);

            AdjustProc() {
            }

            /* access modifiers changed from: package-private */
            public void clearAdjustProc() {
                synchronized (HeartBeatDatabase.this.lock) {
                    this.adjustMap.clear();
                }
            }

            /* access modifiers changed from: package-private */
            public void putAdjustProc(String proc, long interval, int mode) {
                synchronized (HeartBeatDatabase.this.lock) {
                    this.adjustMap.put(proc, new AdjustValue(interval, mode));
                }
            }

            /* access modifiers changed from: package-private */
            public long getAdjustProcInterval(String proc) {
                long interval;
                synchronized (HeartBeatDatabase.this.lock) {
                    AdjustValue procValue = this.adjustMap.get(proc);
                    interval = procValue == null ? 0 : procValue.getInterval();
                }
                return interval;
            }

            /* access modifiers changed from: package-private */
            public int getAdjustProcMode(String proc) {
                int mode;
                synchronized (HeartBeatDatabase.this.lock) {
                    AdjustValue procValue = this.adjustMap.get(proc);
                    mode = procValue == null ? 0 : procValue.getMode();
                }
                return mode;
            }

            /* access modifiers changed from: package-private */
            public int removeAdjustProc(String proc) {
                int size;
                synchronized (HeartBeatDatabase.this.lock) {
                    this.adjustMap.remove(proc);
                    size = this.adjustMap.size();
                }
                return size;
            }

            /* access modifiers changed from: package-private */
            public boolean containsAdjustKey(String proc) {
                boolean containsKey;
                synchronized (HeartBeatDatabase.this.lock) {
                    containsKey = this.adjustMap.containsKey(proc);
                }
                return containsKey;
            }

            /* access modifiers changed from: package-private */
            public void dump(PrintWriter pw) {
                synchronized (HeartBeatDatabase.this.lock) {
                    for (Map.Entry<String, AdjustValue> entry : this.adjustMap.entrySet()) {
                        pw.print("[" + ((Object) entry.getKey()) + AwarenessInnerConstants.COLON_KEY);
                        StringBuilder sb = new StringBuilder();
                        sb.append(entry.getValue());
                        sb.append("]");
                        pw.print(sb.toString());
                    }
                }
            }
        }

        /* access modifiers changed from: private */
        public class AdjustPackageForActions {
            private final HashMap<String, List<String>> mExemptionPkgMap;
            private final HashMap<String, HashMap<String, AdjustValue>> mPkgMap;

            private AdjustPackageForActions() {
                this.mPkgMap = new HashMap<>(16);
                this.mExemptionPkgMap = new HashMap<>(16);
            }

            /* access modifiers changed from: package-private */
            public void clearAdjustPkg() {
                synchronized (HeartBeatDatabase.this.lock) {
                    this.mPkgMap.clear();
                }
            }

            /* access modifiers changed from: package-private */
            public void clearExemptionPkg() {
                synchronized (HeartBeatDatabase.this.lock) {
                    this.mExemptionPkgMap.clear();
                }
            }

            /* access modifiers changed from: package-private */
            public void putAdjustPkg(String pkg, List<String> actions, long interval, int mode) {
                synchronized (HeartBeatDatabase.this.lock) {
                    HashMap<String, AdjustValue> actionMap = this.mPkgMap.get(pkg);
                    if (actionMap == null) {
                        actionMap = new HashMap<>(16);
                        this.mPkgMap.put(pkg, actionMap);
                    }
                    for (String action : actions) {
                        actionMap.put(action, new AdjustValue(interval, mode));
                    }
                }
            }

            /* access modifiers changed from: package-private */
            public void putExemptionPkg(String pkg, List<String> actions) {
                synchronized (HeartBeatDatabase.this.lock) {
                    List<String> actionMap = this.mExemptionPkgMap.get(pkg);
                    if (actionMap == null) {
                        actionMap = new ArrayList();
                        this.mExemptionPkgMap.put(pkg, actionMap);
                    }
                    for (String action : actions) {
                        if (!actionMap.contains(action)) {
                            actionMap.add(action);
                        }
                    }
                }
            }

            /* access modifiers changed from: package-private */
            public AdjustValue getAdjustValue(String pkg, String action) {
                synchronized (HeartBeatDatabase.this.lock) {
                    HashMap<String, AdjustValue> actionMap = this.mPkgMap.get(pkg);
                    if (actionMap == null) {
                        return null;
                    }
                    return actionMap.get(action);
                }
            }

            /* access modifiers changed from: package-private */
            public void removeAdjustPkg(String pkg, List<String> actions) {
                synchronized (HeartBeatDatabase.this.lock) {
                    HashMap<String, AdjustValue> actionMap = this.mPkgMap.get(pkg);
                    if (actionMap != null) {
                        if (actions == null) {
                            this.mPkgMap.remove(pkg);
                            return;
                        }
                        for (String action : actions) {
                            actionMap.remove(action);
                        }
                        if (actionMap.isEmpty()) {
                            this.mPkgMap.remove(pkg);
                        }
                    }
                }
            }

            /* access modifiers changed from: package-private */
            public void removeExemptionPkg(String pkg, List<String> actions) {
                synchronized (HeartBeatDatabase.this.lock) {
                    List<String> actionMap = this.mExemptionPkgMap.get(pkg);
                    if (actionMap != null) {
                        for (String action : actions) {
                            actionMap.remove(action);
                        }
                        if (actionMap.size() == 0) {
                            this.mExemptionPkgMap.remove(pkg);
                        }
                    }
                }
            }

            /* access modifiers changed from: package-private */
            public boolean containsAdjustKey(String pkg, String action) {
                synchronized (HeartBeatDatabase.this.lock) {
                    HashMap<String, AdjustValue> actionMap = this.mPkgMap.get(pkg);
                    if (actionMap == null) {
                        return false;
                    }
                    return actionMap.containsKey(action);
                }
            }

            /* access modifiers changed from: package-private */
            public boolean containsExemptionPkgForAction(String pkg, String action) {
                synchronized (HeartBeatDatabase.this.lock) {
                    List<String> actionMap = this.mExemptionPkgMap.get(pkg);
                    if (actionMap == null) {
                        return false;
                    }
                    return actionMap.contains(action);
                }
            }

            /* access modifiers changed from: package-private */
            public void dump(PrintWriter pw) {
                synchronized (HeartBeatDatabase.this.lock) {
                    for (Map.Entry<String, HashMap<String, AdjustValue>> entry : this.mPkgMap.entrySet()) {
                        pw.println(((Object) entry.getKey()) + AwarenessInnerConstants.COLON_KEY);
                        for (Map.Entry<String, AdjustValue> entry2 : entry.getValue().entrySet()) {
                            pw.print("    " + ((Object) entry2.getKey()) + AwarenessInnerConstants.COLON_KEY);
                            pw.println(entry2.getValue());
                        }
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public static class AdjustValue {
        private long interval = 0;
        private int mode = 0;

        AdjustValue(long theInterval, int theMode) {
            this.interval = theInterval;
            this.mode = theMode;
        }

        /* access modifiers changed from: package-private */
        public long getInterval() {
            return this.interval;
        }

        /* access modifiers changed from: package-private */
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

    /* access modifiers changed from: private */
    public static class MultiMap<K, V> {
        private HashMap<K, List<V>> store;

        private MultiMap() {
            this.store = new HashMap<>(16);
        }

        /* access modifiers changed from: package-private */
        public List<V> getAll(K key) {
            List<V> values = this.store.get(key);
            return values != null ? values : Collections.emptyList();
        }

        /* access modifiers changed from: package-private */
        public void put(K key, V val) {
            List<V> curVals = this.store.get(key);
            if (curVals == null) {
                curVals = new ArrayList(3);
                this.store.put(key, curVals);
            }
            if (!curVals.contains(val)) {
                curVals.add(val);
            }
        }

        /* access modifiers changed from: package-private */
        public void remove(K key, V val) {
            List<V> curVals = this.store.get(key);
            if (curVals != null) {
                curVals.remove(val);
                if (curVals.isEmpty()) {
                    this.store.remove(key);
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void removeAll(K key) {
            this.store.remove(key);
        }

        /* access modifiers changed from: package-private */
        public void clear() {
            this.store.clear();
        }

        /* access modifiers changed from: package-private */
        public void dump(PrintWriter pw) {
            for (Map.Entry<K, List<V>> entry : this.store.entrySet()) {
                pw.print(((Object) entry.getKey()) + AwarenessInnerConstants.COLON_KEY);
                pw.println(entry.getValue());
            }
        }
    }
}
