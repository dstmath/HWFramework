package com.android.server;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.WorkSource;
import com.android.server.AlarmManagerService;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public abstract class AbsAlarmManagerService extends SystemService {
    public AbsAlarmManagerService(Context context) {
        super(context);
    }

    /* access modifiers changed from: protected */
    public void adjustAlarmLocked(AlarmManagerService.Alarm a) {
    }

    /* access modifiers changed from: protected */
    public long checkHasHwRTCAlarmLock(String packageName) {
        return -1;
    }

    /* access modifiers changed from: protected */
    public void setHwAirPlaneStatePropLock() {
    }

    /* access modifiers changed from: protected */
    public void adjustHwRTCAlarmLock(boolean deskClockTime, boolean bootOnTime, int typeState) {
    }

    /* access modifiers changed from: protected */
    public void printHwWakeupBoot(PrintWriter pw) {
    }

    /* access modifiers changed from: protected */
    public void hwRemoveRtcAlarm(AlarmManagerService.Alarm alarm, boolean cancel) {
    }

    /* access modifiers changed from: protected */
    public void hwSetRtcAlarm(AlarmManagerService.Alarm alarm) {
    }

    /* access modifiers changed from: protected */
    public void hwRemoveAnywayRtcAlarm(PendingIntent operation) {
    }

    /* access modifiers changed from: protected */
    public void hwAddFirstFlagForRtcAlarm(AlarmManagerService.Alarm alarm, Intent backgroundIntent) {
    }

    /* access modifiers changed from: protected */
    public void hwRecordFirstTime() {
    }

    /* access modifiers changed from: protected */
    public void hwRecordTimeChangeRTC(long nowRTC, long nowELAPSED, long lastTimeChangeClockTime, long expectedClockTime) {
    }

    /* access modifiers changed from: protected */
    public boolean isContainsAppUidInWorksource(WorkSource worksource, String packageName) {
        return false;
    }

    public void removePackageAlarm(String pkg, List<String> list, int targetUid) {
    }

    /* access modifiers changed from: protected */
    public void setHwRTCAlarmLock() {
    }

    /* access modifiers changed from: protected */
    public void modifyAlarmIfOverload(AlarmManagerService.Alarm alarm) {
    }

    /* access modifiers changed from: protected */
    public void reportWakeupAlarms(ArrayList<AlarmManagerService.Alarm> arrayList) {
    }

    /* access modifiers changed from: protected */
    public boolean isAwareAlarmManagerEnabled() {
        return false;
    }
}
