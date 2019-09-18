package com.android.server;

import android.app.ActivityManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.WorkSource;
import android.provider.Settings;
import android.rms.HwSysResManager;
import android.rms.iaware.AwareConstant;
import android.rms.iaware.CollectData;
import android.text.TextUtils;
import android.util.Flog;
import android.util.Slog;
import com.android.server.AlarmManagerService;
import com.android.server.devicepolicy.plugins.SettingsMDMPlugin;
import com.android.server.hidata.appqoe.HwAPPQoEUtils;
import com.android.server.hidata.arbitration.HwArbitrationDEFS;
import com.android.server.rms.iaware.appmng.AwareWakeUpManager;
import com.android.server.rms.iaware.feature.AlarmManagerFeature;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import huawei.android.app.IHwAlarmManagerEx;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class HwAlarmManagerService extends AlarmManagerService {
    private static final String ACTION_ALARM_WAKEUP = "com.android.deskclock.ALARM_ALERT";
    private static final String ALARM_ID = "intent.extra.alarm_id";
    private static final String ALARM_WHEN = "intent.extra.alarm_when";
    /* access modifiers changed from: private */
    public static boolean DEBUG_SHB = false;
    private static final boolean DEBUG_ST = false;
    private static final String DESKCLOCK_PACKAGENAME = "com.android.deskclock";
    private static final String HWAIRPLANESTATE_PROPERTY = "persist.sys.hwairplanestate";
    private static final boolean IS_DEVICE_ENCRYPTED = "encrypted".equals(SystemProperties.get("ro.crypto.state", ""));
    private static final String IS_OUT_OF_DATA_ALARM = "is_out_of_data_alarm";
    private static final String IS_OWNER_ALARM = "is_owner_alarm";
    private static final boolean IS_POWEROFF_ALARM_ENABLED = "true".equals(SystemProperties.get("ro.poweroff_alarm", "true"));
    private static final String KEY_BOOT_MDM = "boot_alarm_mdm";
    private static final String LAST_TIME_CHANGED_RTC = "last_time_changed_rtc";
    private static final int NONE_LISTVIEW = 1;
    private static final int ONE_BOOT_LISTVIEW = 5;
    private static final int ONE_DESKCLOCK_LISTVIEW = 4;
    private static final int ONE_TWO_BOOT_LISTVIEW = 7;
    private static final int ONE_TWO_DESKCLOCK_LISTVIEW = 6;
    private static final String REMOVE_POWEROFF_ALARM_ANYWAY = "remove_poweroff_alarm_anyway";
    private static final String SAVE_TO_REGISTER = "save_to_register";
    private static final String SETTINGS_PACKAGENAME = "com.android.providers.settings";
    static final String TAG = "HwAlarmManagerService";
    private static final int TRIM_ALARM_POST_MSG_DELAY = 10;
    private static final int TWO_BOOT_DLISTVIEW = 3;
    private static final int TWO_DESKCLOCK_DLISTVIEW = 2;
    private static HashMap<String, AlarmManagerService.Alarm> mHwWakeupBoot = new HashMap<>();
    private static String mPropHwRegisterName = "error";
    private static String timeInRegister = null;
    private boolean hasDeskClocksetAlarm = false;
    private boolean isSetHwMDMAlarm = false;
    Context mContext;
    private long mFirstELAPSED;
    private long mFirstRTC;
    private boolean mHwAlarmLock = false;
    private AlarmManagerService.Alarm mHwMDMAlarm = null;
    private boolean mIsFirstPowerOffAlarm = true;
    private PendingIntent mPendingAlarm = null;
    /* access modifiers changed from: private */
    public SmartHeartBeatDummy mSmartHB = null;
    /* access modifiers changed from: private */
    public HashSet<String> mTrimAlarmPkg = null;

    public class HwAlarmHandler extends AlarmManagerService.AlarmHandler {
        public static final int TRIM_PKG_ALARM = 5;

        public HwAlarmHandler() {
            super(HwAlarmManagerService.this);
        }

        public void handleMessage(Message msg) {
            if (5 == msg.what) {
                HwAlarmManagerService.this.removeByPkg_hwHsm(HwAlarmManagerService.this.mTrimAlarmPkg);
                HwAlarmManagerService.this.mTrimAlarmPkg.clear();
            }
            HwAlarmManagerService.super.handleMessage(msg);
        }
    }

    protected class HwBinderService extends IHwAlarmManagerEx.Stub {
        protected HwBinderService() {
        }

        public void setAlarmsPending(List<String> pkgList, List<String> actionList, boolean pending, int type) {
            if (1000 != Binder.getCallingUid()) {
                Slog.e(HwAlarmManagerService.TAG, "SmartHeartBeat:setAlarmsPending, permission not allowed. uid = " + Binder.getCallingUid());
            } else if (pkgList == null || pkgList.size() <= 0) {
                Slog.i(HwAlarmManagerService.TAG, "SmartHeartBeat:setAlarmsPending, pkgList=" + pkgList);
            } else {
                if (HwAlarmManagerService.DEBUG_SHB) {
                    Slog.i(HwAlarmManagerService.TAG, "SmartHeartBeat:setAlarmsPending, pending=" + pending + ", type=" + type);
                }
                HwAlarmManagerService.this.mSmartHB.setAlarmsPending(pkgList, actionList, pending, type);
            }
        }

        public void removeAllPendingAlarms() {
            if (1000 != Binder.getCallingUid()) {
                Slog.e(HwAlarmManagerService.TAG, "SmartHeartBeat:removeAllPendingAlarms, permission not allowed. uid = " + Binder.getCallingUid());
                return;
            }
            if (HwAlarmManagerService.DEBUG_SHB) {
                Slog.i(HwAlarmManagerService.TAG, "SmartHeartBeat:remove all pending alarms");
            }
            HwAlarmManagerService.this.mSmartHB.removeAllPendingAlarms();
        }

        public void setAlarmsAdjust(List<String> pkgList, List<String> actionList, boolean adjust, int type, long interval, int mode) {
            boolean z;
            if (1000 != Binder.getCallingUid()) {
                Slog.e(HwAlarmManagerService.TAG, "SmartHeartBeat:setAlarmsAdjust, uid: " + Binder.getCallingUid());
                return;
            }
            if (HwAlarmManagerService.DEBUG_SHB) {
                StringBuilder sb = new StringBuilder();
                sb.append("SmartHeartBeat:setAlarmsAdjust ");
                z = adjust;
                sb.append(z);
                Slog.i(HwAlarmManagerService.TAG, sb.toString());
            } else {
                z = adjust;
            }
            HwAlarmManagerService.this.mSmartHB.setAlarmsAdjust(pkgList, actionList, z, type, interval, mode);
        }

        public void removeAllAdjustAlarms() {
            if (1000 != Binder.getCallingUid()) {
                Slog.e(HwAlarmManagerService.TAG, "SmartHeartBeat:removeAllAdjustAlarms, uid: " + Binder.getCallingUid());
                return;
            }
            if (HwAlarmManagerService.DEBUG_SHB) {
                Slog.i(HwAlarmManagerService.TAG, "SmartHeartBeat:remove all adjust alarms.");
            }
            HwAlarmManagerService.this.mSmartHB.removeAllAdjustAlarms();
        }

        /* access modifiers changed from: protected */
        public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            if (HwAlarmManagerService.this.getContext().checkCallingOrSelfPermission("android.permission.DUMP") != 0) {
                pw.println("Permission Denial: can't dump HwAlarmManagerService from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
                return;
            }
            HwAlarmManagerService.this.mSmartHB.dump(pw);
        }
    }

    public HwAlarmManagerService(Context context) {
        super(context);
        this.mSmartHB = SmartHeartBeat.getInstance(context, this);
        DEBUG_SHB = SmartHeartBeat.DEBUG_HEART_BEAT;
        this.mContext = context;
    }

    /* JADX WARNING: type inference failed for: r1v0, types: [android.os.IBinder, com.android.server.HwAlarmManagerService$HwBinderService] */
    public void onStart() {
        HwAlarmManagerService.super.onStart();
        publishBinderService("hwAlarmService", new HwBinderService());
    }

    public void onBootPhase(int phase) {
        Object obj;
        HwAlarmManagerService.super.onBootPhase(phase);
        if (phase == 1000 && IS_POWEROFF_ALARM_ENABLED && !this.hasDeskClocksetAlarm && ActivityManager.getCurrentUser() == 0) {
            int alarm_id = -1;
            long alarm_when = 0;
            String shutAlarm = SystemProperties.get("persist.sys.shut_alarm", "none");
            String[] s = shutAlarm.split(" ");
            if (s.length == 2) {
                try {
                    alarm_id = Integer.parseInt(s[0]);
                    alarm_when = Long.parseLong(s[1]);
                } catch (NumberFormatException e) {
                    Slog.e(TAG, "NumberFormatException : " + e);
                }
            }
            int alarm_id2 = alarm_id;
            long alarm_when2 = alarm_when;
            Slog.d(TAG, "boot completed, alarmId:" + alarm_id2 + " alarm_when:" + alarm_when2);
            if (0 != alarm_when2) {
                long now = System.currentTimeMillis();
                Intent intent = new Intent(ACTION_ALARM_WAKEUP);
                intent.addFlags(16777216);
                intent.putExtra(ALARM_ID, alarm_id2);
                Slog.d(TAG, "putExtra alarm_when " + alarm_when2);
                intent.putExtra(ALARM_WHEN, alarm_when2);
                intent.putExtra(IS_OWNER_ALARM, true);
                if (alarm_when2 < now) {
                    intent.putExtra(IS_OUT_OF_DATA_ALARM, true);
                    Slog.d(TAG, "put is_out_of_data_alarm true");
                }
                Object obj2 = this.mLock;
                synchronized (obj2) {
                    try {
                        this.mPendingAlarm = PendingIntent.getBroadcast(this.mContext, 0, intent, 134217728);
                        Intent intent2 = intent;
                        obj = obj2;
                        long j = alarm_when2;
                        int i = alarm_id2;
                        String[] strArr = s;
                        String str = shutAlarm;
                        setImpl(0, alarm_when2, 0, 0, this.mPendingAlarm, null, null, 9, null, null, 1000, "android");
                    } catch (Throwable th) {
                        th = th;
                        throw th;
                    }
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void adjustAlarmLocked(AlarmManagerService.Alarm a) {
        this.mSmartHB.adjustAlarmIfNeeded(a);
    }

    /* access modifiers changed from: protected */
    public void rebatchPkgAlarmsLocked(List<String> pkgList) {
        if (pkgList != null && !pkgList.isEmpty()) {
            if (DEBUG_SHB) {
                Slog.i(TAG, "SmartHeartBeat:rebatchPkgAlarmsLocked, pkgList: " + pkgList);
            }
            AlarmManagerService.Alarm oldPendingIdleUntil = this.mPendingIdleUntil;
            ArrayList<AlarmManagerService.Batch> batches = new ArrayList<>();
            int i = this.mAlarmBatches.size() - 1;
            while (true) {
                int j = 0;
                if (i < 0) {
                    break;
                }
                AlarmManagerService.Batch b = (AlarmManagerService.Batch) this.mAlarmBatches.get(i);
                int alarmSize = b.alarms.size();
                while (true) {
                    if (j >= alarmSize) {
                        break;
                    } else if (pkgList.contains(((AlarmManagerService.Alarm) b.alarms.get(j)).packageName)) {
                        batches.add(b);
                        this.mAlarmBatches.remove(i);
                        break;
                    } else {
                        j++;
                    }
                }
                i--;
            }
            long nowElapsed = SystemClock.elapsedRealtime();
            for (int i2 = this.mPendingNonWakeupAlarms.size() - 1; i2 >= 0; i2--) {
                AlarmManagerService.Alarm a = (AlarmManagerService.Alarm) this.mPendingNonWakeupAlarms.get(i2);
                if (pkgList.contains(a.packageName) && this.mSmartHB.shouldPendingAlarm(a)) {
                    this.mPendingNonWakeupAlarms.remove(i2);
                    if (DEBUG_SHB) {
                        Slog.i(TAG, "readd PendingNonWakeupAlarms of " + a.packageName + " " + a);
                    }
                    reAddAlarmLocked(a, nowElapsed, true);
                }
            }
            if (batches.size() != 0) {
                this.mCancelRemoveAction = true;
                int M = batches.size();
                for (int batchNum = 0; batchNum < M; batchNum++) {
                    AlarmManagerService.Batch batch = batches.get(batchNum);
                    int N = batch.size();
                    for (int i3 = 0; i3 < N; i3++) {
                        reAddAlarmLocked(batch.get(i3), nowElapsed, true);
                    }
                }
                this.mCancelRemoveAction = false;
                if (!(oldPendingIdleUntil == null || oldPendingIdleUntil == this.mPendingIdleUntil)) {
                    Slog.wtf(TAG, "pkg Rebatching: idle until changed from " + oldPendingIdleUntil + " to " + this.mPendingIdleUntil);
                    if (this.mPendingIdleUntil == null) {
                        restorePendingWhileIdleAlarmsLocked();
                    }
                }
                rescheduleKernelAlarmsLocked();
                updateNextAlarmClockLocked();
                if (DEBUG_SHB) {
                    Slog.i(TAG, "SmartHeartBeat:rebatchPkgAlarmsLocked end");
                }
            }
        }
    }

    public Object getHwAlarmHandler() {
        return new HwAlarmHandler();
    }

    public void postTrimAlarm(HashSet<String> pkgList) {
        if (pkgList != null && pkgList.size() != 0) {
        }
    }

    /* access modifiers changed from: private */
    public void removeByPkg_hwHsm(HashSet<String> sPkgs) {
        if (sPkgs != null) {
            try {
                if (sPkgs.size() != 0) {
                }
            } catch (Exception e) {
                Slog.e(TAG, "AlarmManagerService removeByPkg_hwHsm", e);
            }
        }
    }

    private void decideRtcPrioritySet(AlarmManagerService.Alarm decidingAlarm) {
        String mayInvolvedPackageName;
        if (decidingAlarm != null && decidingAlarm.operation != null) {
            String targetPackageName = decidingAlarm.operation.getTargetPackage();
            if (targetPackageName.equals("android")) {
                targetPackageName = DESKCLOCK_PACKAGENAME;
            }
            AlarmManagerService.Alarm mayInvolvedAlarm = null;
            AlarmManagerService.Alarm decideResultAlarm = null;
            if (DESKCLOCK_PACKAGENAME.equals(targetPackageName)) {
                mayInvolvedPackageName = SETTINGS_PACKAGENAME;
            } else if (SETTINGS_PACKAGENAME.equals(targetPackageName)) {
                mayInvolvedPackageName = DESKCLOCK_PACKAGENAME;
            } else {
                Slog.w(TAG, "decideRtcPrioritySet--packagename error, decideRtcPrioritySet 3, return directly");
                return;
            }
            if (mHwWakeupBoot.containsKey(targetPackageName)) {
                mHwWakeupBoot.remove(targetPackageName);
            }
            Slog.d(TAG, "decideRtcPrioritySet--put " + targetPackageName + ",to map");
            mHwWakeupBoot.put(targetPackageName, decidingAlarm);
            if (mHwWakeupBoot.containsKey(mayInvolvedPackageName)) {
                mayInvolvedAlarm = mHwWakeupBoot.get(mayInvolvedPackageName);
            }
            if (mayInvolvedAlarm == null) {
                decideResultAlarm = decidingAlarm;
            } else if (decidingAlarm.when > mayInvolvedAlarm.when) {
                decideResultAlarm = mayInvolvedAlarm;
            } else if (decidingAlarm.when < mayInvolvedAlarm.when) {
                decideResultAlarm = decidingAlarm;
            } else if (DESKCLOCK_PACKAGENAME.equals(decidingAlarm.operation.getTargetPackage())) {
                decideResultAlarm = decidingAlarm;
            } else if (DESKCLOCK_PACKAGENAME.equals(mayInvolvedAlarm.operation.getTargetPackage())) {
                decideResultAlarm = mayInvolvedAlarm;
            }
            if (decideResultAlarm != null) {
                Slog.d(TAG, "have calculate RTC result and will set to lock");
                hwSetRtcLocked(decideResultAlarm);
            }
        }
    }

    private boolean decideRtcPriorityEnable(AlarmManagerService.Alarm a) {
        if (a == null || a.operation == null) {
            return false;
        }
        boolean ret = false;
        boolean deskClockEnable = false;
        boolean settingProviderEnable = false;
        if (a.type == 0) {
            if (IS_POWEROFF_ALARM_ENABLED && a.operation.getTargetPackage().equals(DESKCLOCK_PACKAGENAME) && resetStrExtraCallingIdentity(a.operation, IS_OWNER_ALARM, true)) {
                deskClockEnable = true;
            }
            if (a.operation.getTargetPackage().equals(SETTINGS_PACKAGENAME) && resetStrExtraCallingIdentity(a.operation, SAVE_TO_REGISTER, false)) {
                settingProviderEnable = true;
            }
        } else {
            ret = false;
        }
        if (settingProviderEnable || deskClockEnable) {
            ret = true;
        }
        return ret;
    }

    private void decideRtcPriorityRemove(PendingIntent operation) {
        String mayInvolvedPackageName;
        if (operation != null) {
            String targetPackageName = operation.getTargetPackage();
            if (targetPackageName.equals("android")) {
                targetPackageName = DESKCLOCK_PACKAGENAME;
            }
            if (DESKCLOCK_PACKAGENAME.equals(targetPackageName)) {
                mayInvolvedPackageName = SETTINGS_PACKAGENAME;
            } else if (SETTINGS_PACKAGENAME.equals(targetPackageName)) {
                mayInvolvedPackageName = DESKCLOCK_PACKAGENAME;
            } else {
                Slog.w(TAG, "packagename error, decideRtcPriorityRemove, return directly");
                return;
            }
            if (mHwWakeupBoot.isEmpty()) {
                Slog.w(TAG, "error, mHwWakeupBoot is empty, return directly");
                hwRemoveRtcLocked();
                return;
            }
            if (mHwWakeupBoot.containsKey(targetPackageName)) {
                Slog.w(TAG, "decideRtcPriorityRemove--remove " + targetPackageName);
                mHwWakeupBoot.remove(targetPackageName);
            }
            if (mHwWakeupBoot.containsKey(mayInvolvedPackageName)) {
                AlarmManagerService.Alarm decideResultAlarm = mHwWakeupBoot.get(mayInvolvedPackageName);
                if (decideResultAlarm != null) {
                    Slog.d(TAG, "decideRtcPriorityRemove ,hwSetRtcLocked");
                    hwSetRtcLocked(decideResultAlarm);
                }
            } else {
                mHwWakeupBoot.clear();
                Slog.d(TAG, "mHwWakeupBoot have clear");
                hwRemoveRtcLocked();
            }
        }
    }

    private boolean isDeskClock(AlarmManagerService.Alarm a) {
        if (a == null || a.operation == null || !IS_POWEROFF_ALARM_ENABLED) {
            return false;
        }
        if (a.type == 0 && IS_POWEROFF_ALARM_ENABLED && a.operation.getTargetPackage().equals(DESKCLOCK_PACKAGENAME) && resetStrExtraCallingIdentity(a.operation, IS_OWNER_ALARM, false)) {
            return true;
        }
        if (a.type != 0 || !IS_POWEROFF_ALARM_ENABLED || !a.operation.getTargetPackage().equals("android") || !resetStrExtraCallingIdentity(a.operation, IS_OWNER_ALARM, false)) {
            return false;
        }
        return true;
    }

    private void saveDeskClock(AlarmManagerService.Alarm a) {
        if (a != null && a.operation != null && a.operation.getTargetPackage().equals(DESKCLOCK_PACKAGENAME) && resetStrExtraCallingIdentity(a.operation, IS_OWNER_ALARM, false)) {
            this.hasDeskClocksetAlarm = true;
            synchronized (this.mLock) {
                if (this.mPendingAlarm != null) {
                    Slog.d(TAG, "set deskclock after boot, remove Shutdown alarm from framework");
                    removeLocked(this.mPendingAlarm, null);
                    this.mPendingAlarm = null;
                }
            }
            long alarmWhen = a.when;
            int alarmId = resetIntExtraCallingIdentity(a.operation, ALARM_ID, -1);
            if (alarmId != -1) {
                Slog.d(TAG, "set shutdownAlarm " + alarmId + " " + alarmWhen);
                StringBuilder sb = new StringBuilder();
                sb.append(alarmId);
                sb.append(" ");
                sb.append(alarmWhen);
                SystemProperties.set("persist.sys.shut_alarm", sb.toString());
            }
        }
    }

    private void removeDeskClock(AlarmManagerService.Alarm a) {
        if (a != null && a.operation != null && IS_POWEROFF_ALARM_ENABLED) {
            if (a.operation.getTargetPackage().equals(DESKCLOCK_PACKAGENAME) && resetStrExtraCallingIdentity(a.operation, IS_OWNER_ALARM, false)) {
                Slog.d(TAG, "remove shutdownAlarm : none");
                SystemProperties.set("persist.sys.shut_alarm", "none");
            }
            if (a.operation.getTargetPackage().equals("android") && resetStrExtraCallingIdentity(a.operation, IS_OWNER_ALARM, false)) {
                Slog.d(TAG, "remove shutdownAlarm : none");
                SystemProperties.set("persist.sys.shut_alarm", "none");
            }
        }
    }

    /* access modifiers changed from: protected */
    public void removeDeskClockFromFWK(PendingIntent operation) {
        if (operation != null && resetStrExtraCallingIdentity(operation, REMOVE_POWEROFF_ALARM_ANYWAY, false) && ActivityManager.getCurrentUser() == 0) {
            synchronized (this.mLock) {
                Slog.d(TAG, "no alarm enable, so remove Shutdown Alarm from framework");
                if (this.mPendingAlarm != null) {
                    removeLocked(this.mPendingAlarm, null);
                    this.mPendingAlarm = null;
                }
            }
            Slog.d(TAG, "remove shutdownAlarm : none");
            SystemProperties.set("persist.sys.shut_alarm", "none");
        }
    }

    private void hwSetRtcLocked(AlarmManagerService.Alarm setAlarm) {
        long alarmNanoseconds;
        long alarmSeconds;
        if (setAlarm != null && setAlarm.operation != null) {
            long alarmWhen = setAlarm.when;
            String targetPackageName = setAlarm.operation.getTargetPackage();
            if (System.currentTimeMillis() > alarmWhen) {
                Slog.d(TAG, "hwSetRtcLocked--missed alarm, not set RTC to driver");
                return;
            }
            if (alarmWhen < 0) {
                alarmSeconds = 0;
                alarmNanoseconds = 0;
            } else {
                alarmNanoseconds = 1000 * (alarmWhen % 1000) * 1000;
                alarmSeconds = alarmWhen / 1000;
            }
            Slog.d(TAG, "set RTC alarm Locked, time = " + getTimeString(alarmWhen));
            timeInRegister = getTimeString(alarmWhen);
            mPropHwRegisterName = targetPackageName;
            hwSetClockRTC(this.mNativeData, alarmSeconds, alarmNanoseconds);
        }
    }

    private void hwRemoveRtcLocked() {
        Slog.d(TAG, "remove RTC alarm time Locked");
        timeInRegister = HwAPPQoEUtils.INVALID_STRING_VALUE;
        hwSetClockRTC(this.mNativeData, 0, 0);
    }

    private String getTimeString(long milliSec) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(milliSec)).toString();
    }

    /* access modifiers changed from: protected */
    public void printHwWakeupBoot(PrintWriter pw) {
        if (!(this.mHwAlarmLock || mHwWakeupBoot == null || mHwWakeupBoot.size() <= 0)) {
            pw.println();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            pw.print("HW WakeupBoot MAP: ");
            pw.println();
            pw.print("Register time:");
            pw.print(timeInRegister);
            pw.println();
            if (mHwWakeupBoot.size() > 0) {
                for (Map.Entry entry : mHwWakeupBoot.entrySet()) {
                    pw.print("packageName=");
                    pw.print((String) entry.getKey());
                    pw.print("  time=");
                    pw.print(sdf.format(new Date(((AlarmManagerService.Alarm) entry.getValue()).when)));
                    pw.println();
                }
            }
        }
    }

    private boolean isMDMAlarm(AlarmManagerService.Alarm alarm) {
        if (alarm == null || alarm.operation == null || !"android".equals(alarm.operation.getTargetPackage()) || !resetStrExtraCallingIdentity(alarm.operation, KEY_BOOT_MDM, false)) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public void hwRemoveRtcAlarm(AlarmManagerService.Alarm alarm, boolean cancel) {
        if (alarm != null) {
            if (isMDMAlarm(alarm)) {
                this.isSetHwMDMAlarm = false;
                this.mHwMDMAlarm = null;
                Slog.i(TAG, "hwRemoveRtcAlarm MDM");
            }
            reportAlarmForAware(alarm, cancel ? 1 : 2);
            if (isDeskClock(alarm)) {
                removeDeskClock(alarm);
            }
            if (!this.mHwAlarmLock) {
                if (decideRtcPriorityEnable(alarm) || isDeskClock(alarm)) {
                    decideRtcPriorityRemove(alarm.operation);
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void hwSetRtcAlarm(AlarmManagerService.Alarm alarm) {
        if (alarm != null) {
            if (alarm.listenerTag != null) {
                Slog.i(TAG, "hwSetAlarm listenerTag: " + alarm.listenerTag);
            }
            if (isMDMAlarm(alarm)) {
                this.isSetHwMDMAlarm = true;
                this.mHwMDMAlarm = alarm;
                Slog.i(TAG, "hwSetRtcAlarm MDM");
            }
            reportAlarmForAware(alarm, 0);
            if (isDeskClock(alarm)) {
                saveDeskClock(alarm);
            }
            if (!this.mHwAlarmLock) {
                if (decideRtcPriorityEnable(alarm) || isDeskClock(alarm)) {
                    decideRtcPrioritySet(alarm);
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void hwRemoveAnywayRtcAlarm(PendingIntent operation) {
        if (operation != null && !this.mHwAlarmLock && IS_POWEROFF_ALARM_ENABLED && operation.getTargetPackage().equals(DESKCLOCK_PACKAGENAME) && resetStrExtraCallingIdentity(operation, REMOVE_POWEROFF_ALARM_ANYWAY, false) && ActivityManager.getCurrentUser() == 0) {
            Slog.d(TAG, "DeskClock not receive boot broadcast,remove alarm anyway");
            decideRtcPriorityRemove(operation);
            Slog.d(TAG, "remove shutdownAlarm : none");
            SystemProperties.set("persist.sys.shut_alarm", "none");
        }
    }

    /* access modifiers changed from: protected */
    public void hwAddFirstFlagForRtcAlarm(AlarmManagerService.Alarm alarm, Intent backgroundIntent) {
        if (alarm != null && alarm.operation != null) {
            if (!decideRtcPriorityEnable(alarm) || !alarm.operation.getTargetPackage().equals(DESKCLOCK_PACKAGENAME)) {
                if (isDeskClock(alarm)) {
                    if (true != this.mIsFirstPowerOffAlarm || !"RTC".equals(SystemProperties.get("persist.sys.powerup_reason", SettingsMDMPlugin.CONFIG_NORMAL_VALUE))) {
                        Slog.d(TAG, "FLAG_IS_FIRST_POWER_OFF_ALARM : false");
                        backgroundIntent.putExtra("FLAG_IS_FIRST_POWER_OFF_ALARM", false);
                    } else {
                        Slog.d(TAG, "FLAG_IS_FIRST_POWER_OFF_ALARM : true");
                        backgroundIntent.putExtra("FLAG_IS_FIRST_POWER_OFF_ALARM", true);
                        this.mIsFirstPowerOffAlarm = false;
                    }
                }
            } else if (true != this.mIsFirstPowerOffAlarm || !"RTC".equals(SystemProperties.get("persist.sys.powerup_reason", SettingsMDMPlugin.CONFIG_NORMAL_VALUE))) {
                backgroundIntent.putExtra("FLAG_IS_FIRST_POWER_OFF_ALARM", false);
            } else {
                backgroundIntent.putExtra("FLAG_IS_FIRST_POWER_OFF_ALARM", true);
                this.mIsFirstPowerOffAlarm = false;
            }
        }
    }

    /* access modifiers changed from: protected */
    public long checkHasHwRTCAlarmLock(String packageName) {
        AlarmManagerService.Alarm rtcAlarm = null;
        long time = -1;
        synchronized (this.mLock) {
            if (packageName != null) {
                try {
                    if (mHwWakeupBoot.containsKey(packageName)) {
                        rtcAlarm = mHwWakeupBoot.get(packageName);
                    }
                } catch (Throwable th) {
                    throw th;
                }
            }
            if (rtcAlarm != null && (decideRtcPriorityEnable(rtcAlarm) || isDeskClock(rtcAlarm))) {
                time = rtcAlarm.when;
            }
        }
        return time;
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x009d, code lost:
        return;
     */
    public void adjustHwRTCAlarmLock(boolean deskClockTime, boolean bootOnTime, int typeState) {
        Slog.d(TAG, "adjust RTC Alarm");
        synchronized (this.mLock) {
            this.mHwAlarmLock = true;
            switch (typeState) {
                case 1:
                    mHwWakeupBoot.clear();
                    Slog.d(TAG, "NONE_LISTVIEW--user cancel bootOnTime and deskClockTime RTC alarm  ");
                    hwRemoveRtcLocked();
                    break;
                case 2:
                case 3:
                case 6:
                case 7:
                    if (!deskClockTime || !bootOnTime) {
                        if (deskClockTime || bootOnTime) {
                            if (!deskClockTime || bootOnTime) {
                                if (!deskClockTime && bootOnTime) {
                                    Slog.d(TAG, "user cancel deskClockTime RTC alarm");
                                    hwRemoveRtcAlarmWhenShut(mHwWakeupBoot.get(DESKCLOCK_PACKAGENAME));
                                    break;
                                }
                            } else {
                                Slog.d(TAG, "user cancel bootOnTime RTC alarm");
                                hwRemoveRtcAlarmWhenShut(mHwWakeupBoot.get(SETTINGS_PACKAGENAME));
                                break;
                            }
                        } else {
                            mHwWakeupBoot.clear();
                            Slog.d(TAG, "user cancel bootOnTime and deskClockTime RTC alarm  ");
                            hwRemoveRtcLocked();
                            break;
                        }
                    } else {
                        setHwAirPlaneStatePropLock();
                        return;
                    }
                case 4:
                    if (!deskClockTime) {
                        mHwWakeupBoot.clear();
                        Slog.d(TAG, "ONE_DESKCLOCK_LISTVIEW ---user cancel deskClockTime RTC alarm  ");
                        hwRemoveRtcLocked();
                        break;
                    }
                    break;
                case 5:
                    if (!bootOnTime) {
                        mHwWakeupBoot.clear();
                        Slog.d(TAG, "ONE_BOOT_LISTVIEW ---user cancel bootOnTime RTC alarm  ");
                        hwRemoveRtcLocked();
                        break;
                    }
                    break;
            }
        }
    }

    private void hwRemoveRtcAlarmWhenShut(AlarmManagerService.Alarm alarm) {
        if (alarm != null) {
            if (decideRtcPriorityEnable(alarm) || isDeskClock(alarm)) {
                Slog.d(TAG, "remove RTC alarm in shutdown view");
                decideRtcPriorityRemove(alarm.operation);
                setHwAirPlaneStatePropLock();
            }
        }
    }

    /* access modifiers changed from: protected */
    public void setHwAirPlaneStatePropLock() {
        SystemProperties.set(HWAIRPLANESTATE_PROPERTY, mPropHwRegisterName);
        Slog.d(TAG, "hw airplane prop locked = " + mPropHwRegisterName);
    }

    /* access modifiers changed from: protected */
    public void setHwRTCAlarmLock() {
        AlarmManagerService.Alarm rtcAlarm;
        if (this.isSetHwMDMAlarm) {
            hwSetRtcLocked(this.mHwMDMAlarm);
            return;
        }
        long deskTime = checkHasHwRTCAlarmLock(DESKCLOCK_PACKAGENAME);
        long settingsTime = checkHasHwRTCAlarmLock(SETTINGS_PACKAGENAME);
        if (deskTime == -1 && settingsTime == -1) {
            Slog.d(TAG, "setHwRTCAlarmLock-- not set RTC to driver");
            return;
        }
        if (deskTime == -1) {
            rtcAlarm = mHwWakeupBoot.get(SETTINGS_PACKAGENAME);
        } else if (settingsTime == -1) {
            rtcAlarm = mHwWakeupBoot.get(DESKCLOCK_PACKAGENAME);
        } else {
            AlarmManagerService.Alarm optAlarm1 = mHwWakeupBoot.get(DESKCLOCK_PACKAGENAME);
            AlarmManagerService.Alarm optAlarm2 = mHwWakeupBoot.get(SETTINGS_PACKAGENAME);
            if (optAlarm1.when <= optAlarm2.when) {
                rtcAlarm = optAlarm1;
            } else {
                rtcAlarm = optAlarm2;
            }
        }
        hwSetRtcLocked(rtcAlarm);
    }

    /* access modifiers changed from: protected */
    public void hwRecordFirstTime() {
        this.mFirstRTC = System.currentTimeMillis();
        this.mFirstELAPSED = SystemClock.elapsedRealtime();
        if (0 == Settings.Global.getLong(getContext().getContentResolver(), LAST_TIME_CHANGED_RTC, 0)) {
            Flog.i(500, "hwRecordFirstTime init for TimeKeeper at " + this.mFirstRTC);
            Settings.Global.putLong(getContext().getContentResolver(), LAST_TIME_CHANGED_RTC, this.mFirstRTC);
        }
    }

    /* access modifiers changed from: protected */
    public void hwRecordTimeChangeRTC(long nowRTC, long nowELAPSED, long lastTimeChangeClockTime, long expectedClockTime) {
        long maxOffset;
        long expectedRTC;
        long j = nowRTC;
        if (lastTimeChangeClockTime == 0) {
            expectedRTC = this.mFirstRTC + (nowELAPSED - this.mFirstELAPSED);
            maxOffset = HwArbitrationDEFS.DelayTimeMillisA;
        } else {
            expectedRTC = expectedClockTime;
            maxOffset = 5000;
        }
        long offset = j - expectedRTC;
        if (offset < (-maxOffset) || j > maxOffset) {
            Flog.i(500, "hwRecordTimeChangeRTC for TimeKeeper at " + j + ", offset=" + offset);
            Settings.Global.putLong(getContext().getContentResolver(), LAST_TIME_CHANGED_RTC, j);
        }
    }

    /* Debug info: failed to restart local var, previous not found, register: 4 */
    private boolean resetStrExtraCallingIdentity(PendingIntent operation, String extra, boolean value) {
        long identity = Binder.clearCallingIdentity();
        boolean extraRes = false;
        try {
            extraRes = operation.getIntent().getBooleanExtra(extra, value);
        } catch (Throwable th) {
        }
        Binder.restoreCallingIdentity(identity);
        return extraRes;
    }

    /* Debug info: failed to restart local var, previous not found, register: 4 */
    private int resetIntExtraCallingIdentity(PendingIntent operation, String extra, int value) {
        long identity = Binder.clearCallingIdentity();
        int extraRes = -1;
        try {
            extraRes = operation.getIntent().getIntExtra(extra, value);
        } catch (Throwable th) {
        }
        Binder.restoreCallingIdentity(identity);
        return extraRes;
    }

    /* access modifiers changed from: protected */
    public boolean isContainsAppUidInWorksource(WorkSource workSource, String packageName) {
        if (workSource == null || TextUtils.isEmpty(packageName)) {
            return false;
        }
        try {
            if (containsUidInWorksource(workSource, getContext().getPackageManager().getApplicationInfo(packageName, 1).uid)) {
                Slog.i(TAG, "isContainsAppUidInWorksource-->worksource contains app's.uid");
                return true;
            }
        } catch (PackageManager.NameNotFoundException e) {
            Slog.w(TAG, "isContainsAppUidInWorksource-->happend NameNotFoundException");
        } catch (Exception e2) {
            Slog.w(TAG, "isContainsAppUidInWorksource-->happend Exception");
        }
        return false;
    }

    private boolean containsUidInWorksource(WorkSource workSource, int uid) {
        if (workSource == null || workSource.size() <= 0) {
            return false;
        }
        int length = workSource.size();
        for (int i = 0; i < length; i++) {
            if (uid == workSource.get(i)) {
                return true;
            }
        }
        return false;
    }

    public void removePackageAlarm(final String pkg, final List<String> tags, final int targetUid) {
        if (this.mHandler != null) {
            this.mHandler.post(new Runnable() {
                public void run() {
                    synchronized (HwAlarmManagerService.this.mLock) {
                        HwAlarmManagerService.this.removeByTagLocked(pkg, tags, targetUid);
                    }
                }
            });
        }
    }

    private boolean removeSingleTagLocked(String packageName, String tag, int targetUid) {
        boolean didRemove = false;
        for (int i = this.mAlarmBatches.size() - 1; i >= 0; i--) {
            AlarmManagerService.Batch b = (AlarmManagerService.Batch) this.mAlarmBatches.get(i);
            didRemove |= removeFromBatch(b, packageName, tag, targetUid);
            if (b.size() == 0) {
                this.mAlarmBatches.remove(i);
            }
        }
        for (int i2 = this.mPendingWhileIdleAlarms.size() - 1; i2 >= 0; i2--) {
            AlarmManagerService.Alarm a = (AlarmManagerService.Alarm) this.mPendingWhileIdleAlarms.get(i2);
            if (a.matches(packageName) && targetUid == a.creatorUid) {
                this.mPendingWhileIdleAlarms.remove(i2);
            }
        }
        return didRemove;
    }

    /* access modifiers changed from: private */
    public void removeByTagLocked(String packageName, List<String> tags, int targetUid) {
        boolean didRemove = false;
        if (packageName != null) {
            if (tags == null) {
                didRemove = removeSingleTagLocked(packageName, null, targetUid);
            } else {
                for (String tag : tags) {
                    didRemove |= removeSingleTagLocked(packageName, tag, targetUid);
                }
            }
            if (didRemove) {
                rebatchAllAlarmsLocked(true);
                rescheduleKernelAlarmsLocked();
                updateNextAlarmClockLocked();
            }
        }
    }

    private boolean canRemove(AlarmManagerService.Alarm alarm, String tag) {
        if (tag == null) {
            return true;
        }
        String alarmTag = AlarmManagerService.Alarm.makeTag(alarm.operation, alarm.listenerTag, alarm.type);
        if (alarmTag == null) {
            return false;
        }
        String[] splits = alarmTag.split(":");
        if (splits.length > 1 && splits[1].equals(tag)) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean removeFromBatch(AlarmManagerService.Batch batch, String packageName, String tag, int targetUid) {
        AlarmManagerService.Batch batch2 = batch;
        String str = packageName;
        String str2 = tag;
        int i = targetUid;
        if (str == null) {
            return false;
        }
        boolean didRemove = false;
        long newStart = 0;
        long newEnd = Long.MAX_VALUE;
        int newFlags = 0;
        for (int i2 = batch2.alarms.size() - 1; i2 >= 0; i2--) {
            AlarmManagerService.Alarm alarm = (AlarmManagerService.Alarm) batch2.alarms.get(i2);
            if (!alarm.matches(str) || i != alarm.creatorUid) {
                if (alarm.whenElapsed > newStart) {
                    newStart = alarm.whenElapsed;
                }
                if (alarm.maxWhenElapsed < newEnd) {
                    newEnd = alarm.maxWhenElapsed;
                }
                newFlags |= alarm.flags;
            } else if (canRemove(alarm, str2)) {
                batch2.alarms.remove(i2);
                didRemove = true;
                if (str2 != null) {
                    Slog.d(TAG, "remove package alarm " + str + "' (" + str2 + ") ,targetUid: " + i);
                }
                hwRemoveRtcAlarm(alarm, true);
                if (alarm.alarmClock != null) {
                    this.mNextAlarmClockMayChange = true;
                }
            }
        }
        if (didRemove) {
            batch2.start = newStart;
            batch2.end = newEnd;
            batch2.flags = newFlags;
        }
        return didRemove;
    }

    private void reportAlarmForAware(AlarmManagerService.Alarm alarm, int operation) {
        if (alarm != null && alarm.packageName != null && alarm.statsTag != null) {
            HwSysResManager resManager = HwSysResManager.getInstance();
            if (resManager != null && resManager.isResourceNeeded(AwareConstant.ResourceType.getReousrceId(AwareConstant.ResourceType.RESOURCE_APPASSOC))) {
                Bundle bundleArgs = new Bundle();
                bundleArgs.putString(MemoryConstant.MEM_PREREAD_ITEM_NAME, alarm.packageName);
                bundleArgs.putString("statstag", alarm.statsTag);
                bundleArgs.putInt("relationType", 22);
                bundleArgs.putInt("alarm_operation", operation);
                bundleArgs.putInt("tgtUid", alarm.creatorUid);
                CollectData data = new CollectData(AwareConstant.ResourceType.getReousrceId(AwareConstant.ResourceType.RESOURCE_APPASSOC), System.currentTimeMillis(), bundleArgs);
                long id = Binder.clearCallingIdentity();
                resManager.reportData(data);
                Binder.restoreCallingIdentity(id);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void modifyAlarmIfOverload(AlarmManagerService.Alarm alarm) {
        AwareWakeUpManager.getInstance().modifyAlarmIfOverload(alarm);
    }

    /* access modifiers changed from: protected */
    public void reportWakeupAlarms(ArrayList<AlarmManagerService.Alarm> alarms) {
        AwareWakeUpManager.getInstance().reportWakeupAlarms(alarms);
    }

    /* access modifiers changed from: protected */
    public boolean isAwareAlarmManagerEnabled() {
        return AlarmManagerFeature.isEnable();
    }
}
