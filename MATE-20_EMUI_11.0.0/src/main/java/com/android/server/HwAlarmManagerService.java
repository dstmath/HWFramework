package com.android.server;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.WorkSource;
import android.provider.Settings;
import android.rms.HwSysResManager;
import android.rms.iaware.AwareConstant;
import android.rms.iaware.CollectData;
import android.text.TextUtils;
import android.util.Flog;
import com.android.server.AlarmManagerServiceExt;
import com.huawei.android.app.ActivityManagerEx;
import com.huawei.android.app.PendingIntentEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.os.WorkSourceEx;
import com.huawei.android.util.SlogEx;
import com.huawei.hiai.awareness.AwarenessInnerConstants;
import com.huawei.server.HwPartIawareUtil;
import huawei.android.app.IHwAlarmManagerEx;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class HwAlarmManagerService extends AlarmManagerServiceExt {
    private static final String ACTION_ALARM_WAKEUP = "com.android.deskclock.ALARM_ALERT";
    private static final String ALARM_ID = "intent.extra.alarm_id";
    private static final String ALARM_WHEN = "intent.extra.alarm_when";
    private static final boolean DEBUG_SHB = SystemPropertiesEx.getBoolean("persist.sys.shb.debug", false);
    private static final String DEFAULT_PACKAGENAME = "android";
    private static final String DESKCLOCK_PACKAGENAME_NEW = "com.huawei.deskclock";
    private static final String DESKCLOCK_PACKAGENAME_OLD = "com.android.deskclock";
    private static final String HWAIRPLANESTATE_PROPERTY = "persist.sys.hwairplanestate";
    private static final int INVALID_DATA = -1;
    private static final long INVALID_TIME = -1;
    private static final String IS_OUT_OF_DATA_ALARM = "is_out_of_data_alarm";
    private static final String IS_OWNER_ALARM = "is_owner_alarm";
    private static final boolean IS_POWEROFF_ALARM_ENABLED = "true".equals(SystemPropertiesEx.get("ro.poweroff_alarm", "true"));
    private static final String KEY_BOOT_MDM = "boot_alarm_mdm";
    private static final String KEY_PRESIST_SHUT_ALARM = "persist.sys.shut_alarm";
    private static final String LAST_TIME_CHANGED_RTC = "last_time_changed_rtc";
    private static final int LENGTH_OF_SHUT_ALARM_INFO = 2;
    private static final long MAX_OFF_SET_LOWER = 5000;
    private static final long MAX_OFF_SET_UPPER = 30000;
    private static final int MILLITOSECOND = 1000;
    private static final int NONE_LISTVIEW = 1;
    private static final int ONE_BOOT_LISTVIEW = 5;
    private static final int ONE_DESKCLOCK_LISTVIEW = 4;
    private static final int ONE_TWO_BOOT_LISTVIEW = 7;
    private static final int ONE_TWO_DESKCLOCK_LISTVIEW = 6;
    private static final int ONLY_ONE_PACKAGE = 1;
    private static final int PACKAGE_WITHOUT_ACTION = 1;
    private static final String REMOVE_POWEROFF_ALARM_ANYWAY = "remove_poweroff_alarm_anyway";
    private static final String SAVE_TO_REGISTER = "save_to_register";
    private static final String SETTINGS_PACKAGENAME = "com.android.providers.settings";
    private static final String TAG = "HwAlarmManagerService";
    private static final int TWO_BOOT_DLISTVIEW = 3;
    private static final int TWO_DESKCLOCK_DLISTVIEW = 2;
    private static Map<String, AlarmManagerServiceExt.AlarmEx> sHwWakeupBoot = new HashMap();
    private static String sPropHwRegisterName = "error";
    private static String sTimeInRegister = null;
    private Context mContext;
    private String mDeskClockName = "";
    private long mFirstElapsed;
    private long mFirstRTC;
    private boolean mIsAdjustedRTCAlarm = false;
    private boolean mIsDeskClockSetAlarm = false;
    private boolean mIsFirstPowerOffAlarm = true;
    private PendingIntent mPendingAlarm = null;
    private AlarmManagerServiceExt.AlarmEx mPresentationAlarm = null;
    private SmartHeartBeatDummy mSmartHeartBeat;

    public HwAlarmManagerService(Context context) {
        super(context);
        this.mSmartHeartBeat = SmartHeartBeat.getInstance(context, this);
        this.mContext = context;
    }

    /* JADX DEBUG: Multi-variable search result rejected for r2v0, resolved type: com.android.server.HwAlarmManagerService */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r0v0, types: [android.os.IBinder, com.android.server.HwAlarmManagerService$HwBinderService] */
    /* JADX WARNING: Unknown variable types count: 1 */
    public void onStart() {
        publishBinderService("hwAlarmService", new HwBinderService());
    }

    public void onBootPhase(int phase) {
        int alarmId;
        long alarmWhen;
        if (phase == PHASE_BOOT_COMPLETED) {
            getDeskClockName();
            if (IS_POWEROFF_ALARM_ENABLED && !this.mIsDeskClockSetAlarm && ActivityManagerEx.getCurrentUser() == 0) {
                int alarmId2 = -1;
                String[] shutAlarmInfos = SystemPropertiesEx.get(KEY_PRESIST_SHUT_ALARM, "none").split(" ");
                if (shutAlarmInfos.length == 2) {
                    try {
                        alarmId2 = Integer.parseInt(shutAlarmInfos[0]);
                        alarmWhen = Long.parseLong(shutAlarmInfos[1]);
                        alarmId = alarmId2;
                    } catch (NumberFormatException e) {
                        SlogEx.e(TAG, "NumberFormatException : " + e);
                        alarmId = alarmId2;
                        alarmWhen = 0;
                    }
                } else {
                    alarmId = -1;
                    alarmWhen = 0;
                }
                SlogEx.i(TAG, "boot completed, alarmId:" + alarmId + " alarmWhen:" + alarmWhen);
                if (alarmWhen != 0) {
                    Intent intent = new Intent(ACTION_ALARM_WAKEUP);
                    intent.addFlags(16777216);
                    intent.putExtra(ALARM_ID, alarmId);
                    SlogEx.i(TAG, "putExtra alarmWhen " + alarmWhen);
                    intent.putExtra(ALARM_WHEN, alarmWhen);
                    intent.putExtra(IS_OWNER_ALARM, true);
                    if (alarmWhen < System.currentTimeMillis()) {
                        intent.putExtra(IS_OUT_OF_DATA_ALARM, true);
                        SlogEx.i(TAG, "put is_out_of_data_alarm true");
                    }
                    synchronized (getLock()) {
                        try {
                            this.mPendingAlarm = PendingIntent.getBroadcast(this.mContext, 0, intent, 134217728);
                            setImpl(0, alarmWhen, 0, 0, this.mPendingAlarm, null, null, 9, null, new AlarmManager.AlarmClockInfo(alarmWhen, this.mPendingAlarm), 1000, DEFAULT_PACKAGENAME);
                        } catch (Throwable th) {
                            th = th;
                            throw th;
                        }
                    }
                }
            }
        }
    }

    protected class HwBinderService extends IHwAlarmManagerEx.Stub {
        protected HwBinderService() {
        }

        public void setAlarmsPending(List<String> pkgList, List<String> actionList, boolean isPending, int type) {
            if (Binder.getCallingUid() != 1000) {
                SlogEx.e(HwAlarmManagerService.TAG, "SmartHeartBeat:setAlarmsPending, permission not allowed. uid = " + Binder.getCallingUid());
            } else if (pkgList == null || pkgList.size() <= 0) {
                SlogEx.i(HwAlarmManagerService.TAG, "SmartHeartBeat:setAlarmsPending, pkgList=" + pkgList);
            } else {
                if (HwAlarmManagerService.DEBUG_SHB) {
                    SlogEx.i(HwAlarmManagerService.TAG, "SmartHeartBeat:setAlarmsPending, isPending=" + isPending + ", type=" + type);
                }
                HwAlarmManagerService.this.mSmartHeartBeat.setAlarmsPending(pkgList, actionList, isPending, type);
            }
        }

        public void removeAllPendingAlarms() {
            if (Binder.getCallingUid() != 1000) {
                SlogEx.e(HwAlarmManagerService.TAG, "SmartHeartBeat:removeAllPendingAlarms, permission not allowed. uid = " + Binder.getCallingUid());
                return;
            }
            if (HwAlarmManagerService.DEBUG_SHB) {
                SlogEx.i(HwAlarmManagerService.TAG, "SmartHeartBeat:remove all pending alarms");
            }
            HwAlarmManagerService.this.mSmartHeartBeat.removeAllPendingAlarms();
        }

        public void setAlarmsAdjust(List<String> pkgList, List<String> actionList, boolean isAdjust, int type, long interval, int mode) {
            if (Binder.getCallingUid() != 1000) {
                SlogEx.e(HwAlarmManagerService.TAG, "SmartHeartBeat:setAlarmsAdjust, uid: " + Binder.getCallingUid());
                return;
            }
            if (HwAlarmManagerService.DEBUG_SHB) {
                SlogEx.i(HwAlarmManagerService.TAG, "SmartHeartBeat:setAlarmsAdjust " + isAdjust);
            }
            HwAlarmManagerService.this.mSmartHeartBeat.setAlarmsAdjust(pkgList, actionList, isAdjust, type, interval, mode);
        }

        public void removeAllAdjustAlarms() {
            if (Binder.getCallingUid() != 1000) {
                SlogEx.e(HwAlarmManagerService.TAG, "SmartHeartBeat:removeAllAdjustAlarms, uid: " + Binder.getCallingUid());
                return;
            }
            if (HwAlarmManagerService.DEBUG_SHB) {
                SlogEx.i(HwAlarmManagerService.TAG, "SmartHeartBeat:remove all adjust alarms.");
            }
            HwAlarmManagerService.this.mSmartHeartBeat.removeAllAdjustAlarms();
        }

        /* access modifiers changed from: protected */
        public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            if (HwAlarmManagerService.this.getContext().checkCallingOrSelfPermission("android.permission.DUMP") != 0) {
                pw.println("Permission Denial: can't dump HwAlarmManagerService from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
                return;
            }
            HwAlarmManagerService.this.mSmartHeartBeat.dump(pw);
        }
    }

    /* access modifiers changed from: protected */
    public void adjustAlarmLocked(AlarmManagerServiceExt.AlarmEx a) {
        this.mSmartHeartBeat.adjustAlarmIfNeeded(a);
    }

    private void printDebugLog(String s) {
        if (DEBUG_SHB) {
            SlogEx.i(TAG, s);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0037, code lost:
        r0.add(r2);
        removeAlarmBatches(r1);
     */
    private List<AlarmManagerServiceExt.BatchEx> removeBatchesOfPkgOrProcLocked(boolean isByPkgs, List<String> pkgList, List<String> procList) {
        List<AlarmManagerServiceExt.BatchEx> batches = new ArrayList<>();
        int i = getAlarmBatchesSize() - 1;
        while (i >= 0) {
            AlarmManagerServiceExt.BatchEx b = getAlarmBatches(i);
            Iterator it = b.alarmsEx.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                AlarmManagerServiceExt.AlarmEx a = (AlarmManagerServiceExt.AlarmEx) it.next();
                if (!isByPkgs || (!pkgList.contains("*") && !pkgList.contains(a.getPkgName()))) {
                    if (!isByPkgs && pkgList.contains(a.getPkgName()) && a.getProcName() != null && procList != null && procList.contains(a.getProcName())) {
                        batches.add(b);
                        removeAlarmBatches(i);
                        printDebugLog("SmartHeartBeat:rebatchPkgAlarmsLocked by proc " + a.getProcName() + " alarm:" + a);
                        break;
                    }
                    SlogEx.w(TAG, "removeBatchesOfPkgOrProcLocked do nothing ");
                }
            }
            i--;
        }
        return batches;
    }

    private void reAddPendingNonWakeupAlarmsOfPkgOrProcLocked(boolean isByPkgs, List<String> pkgList, List<String> procList, long nowElapsed) {
        boolean isContain;
        for (int i = getPendingNonWakeupAlarmsSize() - 1; i >= 0; i--) {
            AlarmManagerServiceExt.AlarmEx a = getPendingNonWakeupAlarms(i);
            if (isByPkgs) {
                isContain = pkgList.contains(a.getPkgName());
            } else {
                isContain = pkgList.contains(a.getPkgName()) && a.getProcName() != null && procList != null && procList.contains(a.getProcName());
                if (DEBUG_SHB) {
                    SlogEx.i(TAG, "SmartHeartBeat:rebatchPkgAlarmsLocked by pkg " + a.getPkgName() + " proc " + a.getProcName() + " alarm:" + a + " isContain:" + isContain);
                }
            }
            if (isContain && this.mSmartHeartBeat.shouldPendingAlarm(a)) {
                removePendingNonWakeupAlarms(i);
                printDebugLog("readd PendingNonWakeupAlarms of " + a.getPkgName() + " " + a);
                reAddAlarmLocked(a, nowElapsed, true);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void rebatchPkgAlarmsLocked(boolean isByPkgs, List<String> pkgList, List<String> procList) {
        if (!(pkgList == null || pkgList.isEmpty())) {
            if (DEBUG_SHB) {
                SlogEx.i(TAG, "SmartHeartBeat:rebatchPkgAlarmsLocked, isByPkgs: " + isByPkgs + " pkgList: " + pkgList + " procList: " + procList);
            }
            AlarmManagerServiceExt.AlarmEx oldPendingIdleUntil = getPendingIdleUntil();
            List<AlarmManagerServiceExt.BatchEx> batches = removeBatchesOfPkgOrProcLocked(isByPkgs, pkgList, procList);
            long nowElapsed = SystemClock.elapsedRealtime();
            reAddPendingNonWakeupAlarmsOfPkgOrProcLocked(isByPkgs, pkgList, procList, nowElapsed);
            if (batches.size() != 0) {
                setIsCancelRemoveAction(true);
                for (AlarmManagerServiceExt.BatchEx batch : batches) {
                    int batchSize = batch.size();
                    for (int i = 0; i < batchSize; i++) {
                        reAddAlarmLocked(batch.get(i), nowElapsed, true);
                    }
                }
                setIsCancelRemoveAction(false);
                AlarmManagerServiceExt.AlarmEx newPendingIdleUntil = getPendingIdleUntil();
                if (oldPendingIdleUntil != null && !oldPendingIdleUntil.isAlarmEquals(newPendingIdleUntil)) {
                    SlogEx.w(TAG, "pkg Rebatching: idle until changed from " + oldPendingIdleUntil + " to " + newPendingIdleUntil);
                    if (newPendingIdleUntil == null) {
                        restorePendingWhileIdleAlarmsLocked();
                    }
                }
                rescheduleKernelAlarmsLocked();
                updateNextAlarmClockLocked();
                if (DEBUG_SHB) {
                    SlogEx.i(TAG, "SmartHeartBeat:rebatchPkgAlarmsLocked end");
                }
            }
        }
    }

    private String getDeskClockName() {
        if ("".equals(this.mDeskClockName)) {
            this.mDeskClockName = getSystemAppForDeskClock(DESKCLOCK_PACKAGENAME_NEW, DESKCLOCK_PACKAGENAME_OLD);
        }
        return this.mDeskClockName;
    }

    private String getSystemAppForDeskClock(String packageName1, String packageName2) {
        if (isSystemApp(packageName1)) {
            return packageName1;
        }
        if (isSystemApp(packageName2)) {
            return packageName2;
        }
        return "";
    }

    private boolean isSystemApp(String packageName) {
        PackageManager packageManager = this.mContext.getPackageManager();
        if (packageManager == null) {
            return false;
        }
        try {
            ApplicationInfo appInfo = packageManager.getApplicationInfo(packageName, 0);
            if (!(appInfo == null || (appInfo.flags & 1) == 0)) {
                SlogEx.i(TAG, packageName + " is SystemApp.");
                return true;
            }
        } catch (PackageManager.NameNotFoundException e) {
            SlogEx.i(TAG, packageName + " not found.");
        }
        return false;
    }

    private void decideRtcPrioritySet(AlarmManagerServiceExt.AlarmEx decidingAlarm) {
        String mayInvolvedPackageName;
        String targetPackageName = decidingAlarm.getOperation().getTargetPackage();
        if (DEFAULT_PACKAGENAME.equals(targetPackageName)) {
            targetPackageName = getDeskClockName();
        }
        if (getDeskClockName().equals(targetPackageName)) {
            mayInvolvedPackageName = SETTINGS_PACKAGENAME;
        } else if (SETTINGS_PACKAGENAME.equals(targetPackageName)) {
            mayInvolvedPackageName = getDeskClockName();
        } else {
            SlogEx.w(TAG, "decideRtcPrioritySet--packagename error, return directly");
            return;
        }
        if (sHwWakeupBoot.containsKey(targetPackageName)) {
            sHwWakeupBoot.remove(targetPackageName);
        }
        SlogEx.i(TAG, "decideRtcPrioritySet--put " + targetPackageName + ", to map");
        sHwWakeupBoot.put(targetPackageName, decidingAlarm);
        AlarmManagerServiceExt.AlarmEx mayInvolvedAlarm = null;
        if (mayInvolvedPackageName != null && sHwWakeupBoot.containsKey(mayInvolvedPackageName)) {
            mayInvolvedAlarm = sHwWakeupBoot.get(mayInvolvedPackageName);
        }
        AlarmManagerServiceExt.AlarmEx decideResultAlarm = null;
        if (mayInvolvedAlarm == null) {
            decideResultAlarm = decidingAlarm;
        } else if (decidingAlarm.getWhen() > mayInvolvedAlarm.getWhen()) {
            decideResultAlarm = mayInvolvedAlarm;
        } else if (decidingAlarm.getWhen() < mayInvolvedAlarm.getWhen()) {
            decideResultAlarm = decidingAlarm;
        } else if (getDeskClockName().equals(decidingAlarm.getOperation().getTargetPackage())) {
            decideResultAlarm = decidingAlarm;
        } else if (getDeskClockName().equals(mayInvolvedAlarm.getOperation().getTargetPackage())) {
            decideResultAlarm = mayInvolvedAlarm;
        }
        if (decideResultAlarm != null) {
            SlogEx.i(TAG, "have calculate RTC result and will set to lock");
            hwSetRtcLocked(decideResultAlarm);
        }
    }

    private void decideRtcPriorityRemove(PendingIntent operation) {
        String mayInvolvedPackageName;
        String targetPackageName = operation.getTargetPackage();
        if (DEFAULT_PACKAGENAME.equals(targetPackageName)) {
            targetPackageName = getDeskClockName();
        }
        if (getDeskClockName().equals(targetPackageName)) {
            mayInvolvedPackageName = SETTINGS_PACKAGENAME;
        } else if (SETTINGS_PACKAGENAME.equals(targetPackageName)) {
            mayInvolvedPackageName = getDeskClockName();
        } else {
            SlogEx.w(TAG, "decideRtcPriorityRemove--packagename error, return directly");
            return;
        }
        if (sHwWakeupBoot.isEmpty()) {
            SlogEx.w(TAG, "decideRtcPriorityRemove--sHwWakeupBoot is empty, return directly");
            hwRemoveRtcLocked();
            return;
        }
        if (sHwWakeupBoot.containsKey(targetPackageName)) {
            SlogEx.w(TAG, "decideRtcPriorityRemove--remove " + targetPackageName);
            sHwWakeupBoot.remove(targetPackageName);
        }
        if (mayInvolvedPackageName == null || !sHwWakeupBoot.containsKey(mayInvolvedPackageName)) {
            sHwWakeupBoot.clear();
            SlogEx.i(TAG, "sHwWakeupBoot have clear");
            hwRemoveRtcLocked();
            return;
        }
        AlarmManagerServiceExt.AlarmEx decideResultAlarm = sHwWakeupBoot.get(mayInvolvedPackageName);
        if (decideResultAlarm != null) {
            SlogEx.i(TAG, "decideRtcPriorityRemove, hwSetRtcLocked");
            hwSetRtcLocked(decideResultAlarm);
        }
    }

    private boolean isSettings(AlarmManagerServiceExt.AlarmEx a) {
        if (!SETTINGS_PACKAGENAME.equals(a.getOperation().getTargetPackage()) || !isOperationHasExtra(a.getOperation(), SAVE_TO_REGISTER, false)) {
            return false;
        }
        return true;
    }

    private boolean isDeskClock(AlarmManagerServiceExt.AlarmEx a) {
        if (!getDeskClockName().equals(a.getOperation().getTargetPackage()) || !isOperationHasExtra(a.getOperation(), IS_OWNER_ALARM, false)) {
            return DEFAULT_PACKAGENAME.equals(a.getOperation().getTargetPackage()) && isOperationHasExtra(a.getOperation(), IS_OWNER_ALARM, false);
        }
        return true;
    }

    private boolean isDeleteDeskClock(AlarmManagerServiceExt.AlarmEx a) {
        if (!getDeskClockName().equals(a.getOperation().getTargetPackage()) || !isOperationHasExtra(a.getOperation(), REMOVE_POWEROFF_ALARM_ANYWAY, false) || ActivityManagerEx.getCurrentUser() != 0) {
            return false;
        }
        return true;
    }

    private void saveDeskClock(AlarmManagerServiceExt.AlarmEx a) {
        if (getDeskClockName().equals(a.getOperation().getTargetPackage()) && isOperationHasExtra(a.getOperation(), IS_OWNER_ALARM, false)) {
            this.mIsDeskClockSetAlarm = true;
            synchronized (getLock()) {
                if (this.mPendingAlarm != null) {
                    SlogEx.i(TAG, "set deskclock after boot, remove Shutdown alarm from framework");
                    removeLocked(this.mPendingAlarm, null);
                    this.mPendingAlarm = null;
                }
            }
            long alarmWhen = a.getWhen();
            int alarmId = resetIntExtraCallingIdentity(a.getOperation(), ALARM_ID, -1);
            if (alarmId != -1) {
                SlogEx.i(TAG, "set shutdownAlarm " + alarmId + ", " + alarmWhen);
                StringBuilder sb = new StringBuilder();
                sb.append(alarmId);
                sb.append(" ");
                sb.append(alarmWhen);
                SystemPropertiesEx.set(KEY_PRESIST_SHUT_ALARM, sb.toString());
            }
        }
    }

    private void removeDeskClock(AlarmManagerServiceExt.AlarmEx a) {
        if (isDeskClock(a) || isDeleteDeskClock(a)) {
            setShutDownAlarmNone();
        }
    }

    /* access modifiers changed from: protected */
    public void removeDeskClockFromFWK(PendingIntent operation) {
        if (operation != null && isOperationHasExtra(operation, REMOVE_POWEROFF_ALARM_ANYWAY, false) && ActivityManagerEx.getCurrentUser() == 0) {
            synchronized (getLock()) {
                SlogEx.i(TAG, "no deskClock alarm enable, remove Shutdown alarm from framework");
                if (this.mPendingAlarm != null) {
                    removeLocked(this.mPendingAlarm, null);
                    this.mPendingAlarm = null;
                }
            }
            setShutDownAlarmNone();
        }
    }

    private void setShutDownAlarmNone() {
        SlogEx.i(TAG, "remove shutdownAlarm : none");
        SystemPropertiesEx.set(KEY_PRESIST_SHUT_ALARM, "none");
    }

    private void hwSetRtcLocked(AlarmManagerServiceExt.AlarmEx alarm) {
        long alarmSeconds;
        long alarmSeconds2;
        long alarmWhen = alarm.getWhen();
        if (System.currentTimeMillis() > alarmWhen) {
            SlogEx.i(TAG, "hwSetRtcLocked--missed alarm, not set RTC to driver");
            return;
        }
        if (alarmWhen < 0) {
            alarmSeconds2 = 0;
            alarmSeconds = 0;
        } else {
            alarmSeconds2 = alarmWhen / 1000;
            alarmSeconds = 1000 * (alarmWhen % 1000) * 1000;
        }
        SlogEx.i(TAG, "set RTC alarm Locked, time = " + getTimeString(alarmWhen));
        sTimeInRegister = getTimeString(alarmWhen);
        sPropHwRegisterName = alarm.getOperation().getTargetPackage();
        hwSetClockRTC(alarmSeconds2, alarmSeconds);
    }

    private void hwRemoveRtcLocked() {
        SlogEx.i(TAG, "remove RTC alarm time Locked");
        sTimeInRegister = "None";
        sPropHwRegisterName = "error";
        hwSetClockRTC(0, 0);
    }

    private String getTimeString(long milliSec) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(milliSec)).toString();
    }

    /* access modifiers changed from: protected */
    public void printHwWakeupBoot(PrintWriter pw) {
        Map<String, AlarmManagerServiceExt.AlarmEx> map;
        if (!this.mIsAdjustedRTCAlarm && (map = sHwWakeupBoot) != null && map.size() > 0) {
            pw.println();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            pw.print("HW WakeupBoot MAP: ");
            pw.println();
            pw.print("Register time:");
            pw.print(sTimeInRegister);
            pw.println();
            if (sHwWakeupBoot.size() > 0) {
                for (Map.Entry<String, AlarmManagerServiceExt.AlarmEx> entry : sHwWakeupBoot.entrySet()) {
                    pw.print("packageName=");
                    pw.print(entry.getKey());
                    pw.print("  time=");
                    pw.print(sdf.format(new Date(entry.getValue().getWhen())));
                    pw.println();
                }
            }
        }
    }

    private boolean isPresentationAlarm(AlarmManagerServiceExt.AlarmEx alarm) {
        if (!DEFAULT_PACKAGENAME.equals(alarm.getOperation().getTargetPackage()) || !isOperationHasExtra(alarm.getOperation(), KEY_BOOT_MDM, false)) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public void hwRemoveRtcAlarm(AlarmManagerServiceExt.AlarmEx alarm, boolean isCancel) {
        if (alarm != null) {
            reportAlarmForAware(alarm, isCancel ? 1 : 2);
            if (alarm.getOperation() != null) {
                if (isPresentationAlarm(alarm)) {
                    this.mPresentationAlarm = null;
                    SlogEx.i(TAG, "hwRemoveRtcAlarm MDM");
                }
                if (IS_POWEROFF_ALARM_ENABLED && alarm.getType() == 0) {
                    removeDeskClock(alarm);
                    if (!this.mIsAdjustedRTCAlarm) {
                        if (isSettings(alarm) || isDeskClock(alarm) || isDeleteDeskClock(alarm)) {
                            decideRtcPriorityRemove(alarm.getOperation());
                        }
                    }
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void hwSetRtcAlarm(AlarmManagerServiceExt.AlarmEx alarm) {
        if (alarm != null) {
            reportAlarmForAware(alarm, 0);
            if (alarm.getOperation() != null) {
                if (isPresentationAlarm(alarm)) {
                    this.mPresentationAlarm = alarm;
                    SlogEx.i(TAG, "hwSetRtcAlarm MDM");
                }
                if (IS_POWEROFF_ALARM_ENABLED && alarm.getType() == 0) {
                    saveDeskClock(alarm);
                    if (!this.mIsAdjustedRTCAlarm) {
                        if (isSettings(alarm) || isDeskClock(alarm)) {
                            decideRtcPrioritySet(alarm);
                        }
                    }
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void hwAddFirstFlagForRtcAlarm(AlarmManagerServiceExt.AlarmEx alarm, Intent backgroundIntent) {
        if (alarm != null && alarm.getOperation() != null && isDeskClock(alarm)) {
            if (!this.mIsFirstPowerOffAlarm || !"RTC".equals(SystemPropertiesEx.get("persist.sys.powerup_reason", "NORMAL"))) {
                setIntentInfo(backgroundIntent, false);
                return;
            }
            setIntentInfo(backgroundIntent, true);
            this.mIsFirstPowerOffAlarm = false;
        }
    }

    private void setIntentInfo(Intent backgroundIntent, boolean isFirstPowerOffAlarm) {
        SlogEx.i(TAG, "FLAG_IS_FIRST_POWER_OFF_ALARM :" + isFirstPowerOffAlarm);
        backgroundIntent.putExtra("FLAG_IS_FIRST_POWER_OFF_ALARM", isFirstPowerOffAlarm);
    }

    /* access modifiers changed from: protected */
    public long checkHasHwRTCAlarmLock(String packageName) {
        AlarmManagerServiceExt.AlarmEx rtcAlarm = null;
        long time = INVALID_TIME;
        synchronized (getLock()) {
            if (packageName != null) {
                try {
                    if (sHwWakeupBoot.containsKey(packageName)) {
                        rtcAlarm = sHwWakeupBoot.get(packageName);
                    }
                } catch (Throwable th) {
                    throw th;
                }
            }
            if (rtcAlarm != null) {
                if (rtcAlarm.getOperation() != null) {
                    if (isSettings(rtcAlarm) || isDeskClock(rtcAlarm)) {
                        time = rtcAlarm.getWhen();
                    }
                    return time;
                }
            }
            return INVALID_TIME;
        }
    }

    /* access modifiers changed from: protected */
    public void adjustHwRTCAlarmLock(boolean isDeskClock, boolean isBoot, int typeState) {
        SlogEx.i(TAG, "adjust RTC Alarm");
        synchronized (getLock()) {
            this.mIsAdjustedRTCAlarm = true;
            switch (typeState) {
                case 1:
                    clearRtcAlarm(typeState);
                    break;
                case 2:
                case 3:
                case 6:
                case 7:
                    if (!isDeskClock && !isBoot) {
                        clearRtcAlarm(typeState);
                        break;
                    } else {
                        if (!isDeskClock || isBoot) {
                            if (!isDeskClock && isBoot) {
                                SlogEx.i(TAG, "typeState:" + typeState + ", user cancel deskClockTime RTC alarm");
                                decideRtcPriorityRemove(sHwWakeupBoot.get(getDeskClockName()).getOperation());
                                break;
                            }
                        } else {
                            SlogEx.i(TAG, "typeState:" + typeState + ", user cancel bootOnTime RTC alarm");
                            decideRtcPriorityRemove(sHwWakeupBoot.get(SETTINGS_PACKAGENAME).getOperation());
                        }
                        break;
                    }
                case 4:
                    if (!isDeskClock) {
                        clearRtcAlarm(typeState);
                        break;
                    }
                    break;
                case 5:
                    if (!isBoot) {
                        clearRtcAlarm(typeState);
                        break;
                    }
                    break;
            }
        }
    }

    private void clearRtcAlarm(int typeState) {
        sHwWakeupBoot.clear();
        SlogEx.i(TAG, "typeState:" + typeState + ", user cancel RTC alarm");
        hwRemoveRtcLocked();
    }

    private void setHwAirPlaneStatePropLock() {
        SystemPropertiesEx.set(HWAIRPLANESTATE_PROPERTY, sPropHwRegisterName);
        SlogEx.i(TAG, "hw airplane prop locked = " + sPropHwRegisterName);
    }

    /* access modifiers changed from: protected */
    public void setHwRTCAlarmLock() {
        AlarmManagerServiceExt.AlarmEx optAlarm2;
        AlarmManagerServiceExt.AlarmEx alarmEx = this.mPresentationAlarm;
        if (alarmEx != null) {
            hwSetRtcLocked(alarmEx);
            setHwAirPlaneStatePropLock();
            return;
        }
        long deskTime = checkHasHwRTCAlarmLock(getDeskClockName());
        long settingsTime = checkHasHwRTCAlarmLock(SETTINGS_PACKAGENAME);
        if (deskTime == INVALID_TIME && settingsTime == INVALID_TIME) {
            SlogEx.i(TAG, "setHwRTCAlarmLock-- not set RTC to driver");
            setHwAirPlaneStatePropLock();
            return;
        }
        if (deskTime == INVALID_TIME) {
            optAlarm2 = sHwWakeupBoot.get(SETTINGS_PACKAGENAME);
        } else if (settingsTime == INVALID_TIME) {
            optAlarm2 = sHwWakeupBoot.get(getDeskClockName());
        } else {
            AlarmManagerServiceExt.AlarmEx optAlarm1 = sHwWakeupBoot.get(getDeskClockName());
            optAlarm2 = sHwWakeupBoot.get(SETTINGS_PACKAGENAME);
            if (optAlarm1.getWhen() <= optAlarm2.getWhen()) {
                optAlarm2 = optAlarm1;
            }
        }
        hwSetRtcLocked(optAlarm2);
        setHwAirPlaneStatePropLock();
    }

    /* access modifiers changed from: protected */
    public void hwRecordFirstTime() {
        this.mFirstRTC = System.currentTimeMillis();
        this.mFirstElapsed = SystemClock.elapsedRealtime();
        if (Settings.Global.getLong(getContext().getContentResolver(), LAST_TIME_CHANGED_RTC, 0) == 0) {
            Flog.i(500, "hwRecordFirstTime init for TimeKeeper at " + this.mFirstRTC);
            Settings.Global.putLong(getContext().getContentResolver(), LAST_TIME_CHANGED_RTC, this.mFirstRTC);
        }
    }

    /* access modifiers changed from: protected */
    public void hwRecordTimeChangeRTC(long nowRTC, long nowElapsed, long lastTimeChangeClockTime, long expectedClockTime) {
        long maxOffset;
        long expectedRTC;
        if (lastTimeChangeClockTime == 0) {
            expectedRTC = this.mFirstRTC + (nowElapsed - this.mFirstElapsed);
            maxOffset = MAX_OFF_SET_UPPER;
        } else {
            expectedRTC = expectedClockTime;
            maxOffset = MAX_OFF_SET_LOWER;
        }
        long offset = nowRTC - expectedRTC;
        if (offset < (-maxOffset) || nowRTC > maxOffset) {
            Flog.i(500, "hwRecordTimeChangeRTC for TimeKeeper at " + nowRTC + ", offset=" + offset);
            Settings.Global.putLong(getContext().getContentResolver(), LAST_TIME_CHANGED_RTC, nowRTC);
        }
    }

    private boolean isOperationHasExtra(PendingIntent operation, String extra, boolean isExtraValue) {
        long identity = Binder.clearCallingIdentity();
        boolean isExtra = false;
        try {
            Intent intent = PendingIntentEx.getIntent(operation);
            if (intent == null) {
                Binder.restoreCallingIdentity(identity);
                return false;
            }
            isExtra = intent.getBooleanExtra(extra, isExtraValue);
            Binder.restoreCallingIdentity(identity);
            return isExtra;
        } catch (Exception e) {
            SlogEx.e(TAG, "getBooleanExtra error, extra:" + extra);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
            throw th;
        }
    }

    private int resetIntExtraCallingIdentity(PendingIntent operation, String extra, int value) {
        long identity = Binder.clearCallingIdentity();
        int extraRes = -1;
        try {
            Intent intent = PendingIntentEx.getIntent(operation);
            if (intent == null) {
                Binder.restoreCallingIdentity(identity);
                return -1;
            }
            extraRes = intent.getIntExtra(extra, value);
            Binder.restoreCallingIdentity(identity);
            return extraRes;
        } catch (Exception e) {
            SlogEx.e(TAG, "getIntExtra error, extra:" + extra);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
            throw th;
        }
    }

    /* access modifiers changed from: protected */
    public boolean isContainsAppUidInWorksource(WorkSource workSource, String packageName) {
        if (workSource == null || TextUtils.isEmpty(packageName)) {
            return false;
        }
        try {
            if (!containsUidInWorksource(workSource, getContext().getPackageManager().getApplicationInfo(packageName, 1).uid)) {
                return false;
            }
            SlogEx.i(TAG, "isContainsAppUidInWorksource-->worksource contains app's.uid");
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private boolean containsUidInWorksource(WorkSource workSource, int uid) {
        if (workSource == null || WorkSourceEx.size(workSource) <= 0) {
            return false;
        }
        int length = WorkSourceEx.size(workSource);
        for (int i = 0; i < length; i++) {
            if (uid == WorkSourceEx.get(workSource, i)) {
                return true;
            }
        }
        return false;
    }

    public void removePackageAlarm(final String pkg, final List<String> tags, final int targetUid) {
        postHandler(new Runnable() {
            /* class com.android.server.HwAlarmManagerService.AnonymousClass1 */

            @Override // java.lang.Runnable
            public void run() {
                synchronized (HwAlarmManagerService.this.getLock()) {
                    HwAlarmManagerService.this.removeByTagLocked(pkg, tags, targetUid);
                }
            }
        });
    }

    private boolean removeSingleTagLocked(String packageName, String tag, int targetUid) {
        boolean didRemove = false;
        for (int i = getAlarmBatchesSize() - 1; i >= 0; i--) {
            AlarmManagerServiceExt.BatchEx b = getAlarmBatches(i);
            didRemove |= removeFromBatch(b, packageName, tag, targetUid);
            if (b.size() == 0) {
                removeAlarmBatches(i);
            }
        }
        for (int i2 = getPendingWhileIdleAlarmsSize() - 1; i2 >= 0; i2--) {
            AlarmManagerServiceExt.AlarmEx a = getPendingWhileIdleAlarms(i2);
            if (a.matches(packageName) && targetUid == a.getCreatorUid()) {
                removePendingWhileIdleAlarms(i2);
                decrementAlarmCount(a.getUid(), 1);
                Flog.i(500, "remove pending idle:" + a + " by pkg:" + packageName + ", tag:" + tag + ", uid:" + targetUid);
            }
        }
        return didRemove;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void removeByTagLocked(String packageName, List<String> tags, int targetUid) {
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
                if (DEBUG_BATCH) {
                    SlogEx.v(TAG, "remove(package) changed bounds; rebatching");
                }
                rebatchAllAlarmsLocked(true);
                rescheduleKernelAlarmsLocked();
                updateNextAlarmClockLocked();
            }
        }
    }

    private boolean canRemove(AlarmManagerServiceExt.AlarmEx alarm, String tag) {
        if (tag == null) {
            return true;
        }
        String alarmTag = AlarmManagerServiceExt.AlarmEx.makeTag(alarm.getOperation(), alarm.getListenerTag(), alarm.getType());
        if (alarmTag == null) {
            return false;
        }
        String[] splits = alarmTag.split(AwarenessInnerConstants.COLON_KEY);
        if (splits.length > 1 && splits[1].equals(tag)) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean removeFromBatch(AlarmManagerServiceExt.BatchEx batch, String packageName, String tag, int targetUid) {
        if (packageName == null) {
            return false;
        }
        boolean didRemove = false;
        long newStart = 0;
        long newEnd = Long.MAX_VALUE;
        int newFlags = 0;
        int i = 1;
        for (int i2 = batch.alarmsEx.size() - 1; i2 >= 0; i2--) {
            AlarmManagerServiceExt.AlarmEx alarm = (AlarmManagerServiceExt.AlarmEx) batch.alarmsEx.get(i2);
            if (!alarm.matches(packageName) || targetUid != alarm.getCreatorUid()) {
                if (alarm.getWhenElapsed() > newStart) {
                    newStart = alarm.getWhenElapsed();
                }
                if (alarm.getMaxWhenElapsed() < newEnd) {
                    newEnd = alarm.getMaxWhenElapsed();
                }
                newFlags |= alarm.getFlags();
            } else if (canRemove(alarm, tag)) {
                batch.alarmsEx.remove(i2);
                decrementAlarmCount(alarm.getUid(), i);
                didRemove = true;
                Flog.i(500, "remove " + alarm + " by pkg:" + packageName + ", tag:" + tag + ", targetUid: " + targetUid);
                i = 1;
                hwRemoveRtcAlarm(alarm, true);
                if (alarm.getAlarmClock() != null) {
                    setNextAlarmClockMayChange(true);
                }
            }
        }
        if (didRemove) {
            batch.setStart(newStart);
            batch.setEnd(newEnd);
            batch.setFlags(newFlags);
        }
        return didRemove;
    }

    private void reportAlarmForAware(AlarmManagerServiceExt.AlarmEx alarm, int operation) {
        HwSysResManager resManager;
        if (alarm != null && alarm.getPkgName() != null && alarm.getStatsTag() != null && (resManager = HwSysResManager.getInstance()) != null && resManager.isResourceNeeded(AwareConstant.ResourceType.getReousrceId(AwareConstant.ResourceType.RESOURCE_APPASSOC))) {
            Bundle bundleArgs = new Bundle();
            bundleArgs.putString("pkgname", alarm.getPkgName());
            bundleArgs.putString("statstag", alarm.getStatsTag());
            bundleArgs.putInt("relationType", 22);
            bundleArgs.putInt("alarm_operation", operation);
            bundleArgs.putInt("tgtUid", alarm.getCreatorUid());
            CollectData data = new CollectData(AwareConstant.ResourceType.getReousrceId(AwareConstant.ResourceType.RESOURCE_APPASSOC), System.currentTimeMillis(), bundleArgs);
            long id = Binder.clearCallingIdentity();
            resManager.reportData(data);
            Binder.restoreCallingIdentity(id);
        }
    }

    /* access modifiers changed from: protected */
    public void modifyAlarmIfOverload(AlarmManagerServiceExt.AlarmEx alarmEx) {
        HwPartIawareUtil.modifyAlarmIfOverload(alarmEx);
    }

    /* access modifiers changed from: protected */
    public void reportWakeupAlarms(ArrayList<AlarmManagerServiceExt.AlarmEx> alarmsEx) {
        HwPartIawareUtil.reportWakeupAlarms(alarmsEx);
    }

    /* access modifiers changed from: protected */
    public boolean isAwareAlarmManagerEnabled() {
        return HwPartIawareUtil.isAwareAlarmManagerEnabled();
    }

    /* access modifiers changed from: protected */
    public int getWakeUpNumImpl(int uid, String pkg) {
        int wakeUpNumImplEx;
        synchronized (getLock()) {
            wakeUpNumImplEx = getWakeUpNumImplEx(uid, pkg);
        }
        return wakeUpNumImplEx;
    }

    public void setAlarmExemption(List<String> pkgWithActions, int type) {
        if (pkgWithActions == null || pkgWithActions.isEmpty()) {
            SlogEx.w(TAG, "setAlarmExemption, pkgWithActions is null");
        } else if (pkgWithActions.size() == 1) {
            String[] arrays = pkgWithActions.get(0).split(AwarenessInnerConstants.COMMA_KEY);
            String pkg = arrays[0];
            List<String> actions = new ArrayList<>();
            if (arrays.length > 1) {
                for (String action : arrays[1].split("\\|")) {
                    actions.add(action);
                }
                this.mSmartHeartBeat.setAlarmExemption(Arrays.asList(pkg), actions, type);
                return;
            }
            this.mSmartHeartBeat.setAlarmExemption(Arrays.asList(pkg), (List) null, type);
        } else {
            this.mSmartHeartBeat.setAlarmExemption(pkgWithActions, (List) null, type);
        }
    }

    public static class HwAlarm {
        private AlarmManagerServiceExt.AlarmEx mAlarm = null;

        HwAlarm(AlarmManagerServiceExt.AlarmEx alarm) {
            this.mAlarm = alarm;
        }

        public int getType() {
            AlarmManagerServiceExt.AlarmEx alarmEx = this.mAlarm;
            if (alarmEx == null) {
                return -1;
            }
            return alarmEx.getType();
        }

        public int getUid() {
            AlarmManagerServiceExt.AlarmEx alarmEx = this.mAlarm;
            if (alarmEx == null) {
                return -1;
            }
            return alarmEx.getUid();
        }

        public String getPkgName() {
            AlarmManagerServiceExt.AlarmEx alarmEx = this.mAlarm;
            if (alarmEx == null) {
                return null;
            }
            return alarmEx.getPkgName();
        }

        public boolean getWakeup() {
            AlarmManagerServiceExt.AlarmEx alarmEx = this.mAlarm;
            if (alarmEx == null) {
                return false;
            }
            return alarmEx.getWakeup();
        }

        public String getStatsTag() {
            AlarmManagerServiceExt.AlarmEx alarmEx = this.mAlarm;
            if (alarmEx == null) {
                return null;
            }
            return alarmEx.getStatsTag();
        }

        public long getWhenElapsed() {
            AlarmManagerServiceExt.AlarmEx alarmEx = this.mAlarm;
            if (alarmEx == null) {
                return 0;
            }
            return alarmEx.getWhenElapsed();
        }

        public long getWindowLength() {
            AlarmManagerServiceExt.AlarmEx alarmEx = this.mAlarm;
            if (alarmEx == null) {
                return 0;
            }
            return alarmEx.getWindowLength();
        }

        public long getRepeatInterval() {
            AlarmManagerServiceExt.AlarmEx alarmEx = this.mAlarm;
            if (alarmEx == null) {
                return 0;
            }
            return alarmEx.getRepeatInterval();
        }

        public long getMaxWhenElapsed() {
            AlarmManagerServiceExt.AlarmEx alarmEx = this.mAlarm;
            if (alarmEx == null) {
                return 0;
            }
            return alarmEx.getMaxWhenElapsed();
        }

        public void setWhenElapsed(long whenElapsed) {
            AlarmManagerServiceExt.AlarmEx alarmEx = this.mAlarm;
            if (alarmEx != null) {
                alarmEx.setWhenElapsed(whenElapsed);
            }
        }

        public void setMaxWhenElapsed(long maxWhenElapsed) {
            AlarmManagerServiceExt.AlarmEx alarmEx = this.mAlarm;
            if (alarmEx != null) {
                alarmEx.setMaxWhenElapsed(maxWhenElapsed);
            }
        }

        public void setWakeup(boolean isWakeup) {
            AlarmManagerServiceExt.AlarmEx alarmEx = this.mAlarm;
            if (alarmEx != null) {
                alarmEx.setWakeup(isWakeup);
            }
        }
    }
}
