package com.android.server;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.WorkSource;
import com.android.server.AlarmManagerService.Alarm;
import java.io.PrintWriter;
import java.util.List;

public abstract class AbsAlarmManagerService extends SystemService {
    public AbsAlarmManagerService(Context context) {
        super(context);
    }

    protected void adjustAlarmLocked(Alarm a) {
    }

    protected long checkHasHwRTCAlarmLock(String packageName) {
        return -1;
    }

    protected void setHwAirPlaneStatePropLock() {
    }

    protected void adjustHwRTCAlarmLock(boolean deskClockTime, boolean bootOnTime, int typeState) {
    }

    protected void printHwWakeupBoot(PrintWriter pw) {
    }

    protected void hwRemoveRtcAlarm(Alarm alarm, boolean cancel) {
    }

    protected void hwSetRtcAlarm(Alarm alarm) {
    }

    protected void hwRemoveAnywayRtcAlarm(PendingIntent operation) {
    }

    protected void hwAddFirstFlagForRtcAlarm(Alarm alarm, Intent backgroundIntent) {
    }

    protected void hwRecordFirstTime() {
    }

    protected void hwRecordTimeChangeRTC(long nowRTC, long nowELAPSED, long lastTimeChangeClockTime, long expectedClockTime) {
    }

    protected boolean isContainsAppUidInWorksource(WorkSource worksource, String packageName) {
        return false;
    }

    public void removePackageAlarm(String pkg, List<String> list) {
    }

    protected void setHwRTCAlarmLock() {
    }
}
