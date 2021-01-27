package com.android.server;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.WorkSource;
import com.android.server.AlarmManagerService;
import com.android.server.AlarmManagerServiceExt;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AlarmManagerServiceBridge extends AlarmManagerService {
    private AlarmManagerServiceExt mAlarmManagerServiceExt;

    public AlarmManagerServiceBridge(Context context) {
        super(context);
    }

    public void setAlarmManagerServiceExt(AlarmManagerServiceExt alarmManagerServiceExt) {
        this.mAlarmManagerServiceExt = alarmManagerServiceExt;
    }

    public void onStart() {
        AlarmManagerServiceBridge.super.onStart();
        AlarmManagerServiceExt alarmManagerServiceExt = this.mAlarmManagerServiceExt;
        if (alarmManagerServiceExt != null) {
            alarmManagerServiceExt.onStart();
        }
    }

    public void onBootPhase(int phase) {
        AlarmManagerServiceBridge.super.onBootPhase(phase);
        AlarmManagerServiceExt alarmManagerServiceExt = this.mAlarmManagerServiceExt;
        if (alarmManagerServiceExt != null) {
            alarmManagerServiceExt.onBootPhase(phase);
        }
    }

    /* access modifiers changed from: protected */
    public void adjustAlarmLocked(AlarmManagerService.Alarm alarm) {
        if (this.mAlarmManagerServiceExt != null) {
            AlarmManagerServiceExt.AlarmEx alarmEx = null;
            if (alarm != null) {
                alarmEx = new AlarmManagerServiceExt.AlarmEx();
                alarmEx.setAlarm(alarm);
            }
            this.mAlarmManagerServiceExt.adjustAlarmLocked(alarmEx);
        }
    }

    /* access modifiers changed from: protected */
    public long checkHasHwRTCAlarmLock(String packageName) {
        AlarmManagerServiceExt alarmManagerServiceExt = this.mAlarmManagerServiceExt;
        if (alarmManagerServiceExt != null) {
            return alarmManagerServiceExt.checkHasHwRTCAlarmLock(packageName);
        }
        return -1;
    }

    /* access modifiers changed from: protected */
    public void adjustHwRTCAlarmLock(boolean isDeskClock, boolean isBoot, int typeState) {
        AlarmManagerServiceExt alarmManagerServiceExt = this.mAlarmManagerServiceExt;
        if (alarmManagerServiceExt != null) {
            alarmManagerServiceExt.adjustHwRTCAlarmLock(isDeskClock, isBoot, typeState);
        }
    }

    /* access modifiers changed from: protected */
    public void printHwWakeupBoot(PrintWriter pw) {
        AlarmManagerServiceExt alarmManagerServiceExt = this.mAlarmManagerServiceExt;
        if (alarmManagerServiceExt != null) {
            alarmManagerServiceExt.printHwWakeupBoot(pw);
        }
    }

    /* access modifiers changed from: protected */
    public void hwRemoveRtcAlarm(AlarmManagerService.Alarm alarm, boolean isCancel) {
        if (this.mAlarmManagerServiceExt != null) {
            AlarmManagerServiceExt.AlarmEx alarmEx = null;
            if (alarm != null) {
                alarmEx = new AlarmManagerServiceExt.AlarmEx();
                alarmEx.setAlarm(alarm);
            }
            this.mAlarmManagerServiceExt.hwRemoveRtcAlarm(alarmEx, isCancel);
        }
    }

    /* access modifiers changed from: protected */
    public void hwSetRtcAlarm(AlarmManagerService.Alarm alarm) {
        if (this.mAlarmManagerServiceExt != null) {
            AlarmManagerServiceExt.AlarmEx alarmEx = null;
            if (alarm != null) {
                alarmEx = new AlarmManagerServiceExt.AlarmEx();
                alarmEx.setAlarm(alarm);
            }
            this.mAlarmManagerServiceExt.hwSetRtcAlarm(alarmEx);
        }
    }

    /* access modifiers changed from: protected */
    public void hwAddFirstFlagForRtcAlarm(AlarmManagerService.Alarm alarm, Intent backgroundIntent) {
        if (this.mAlarmManagerServiceExt != null) {
            AlarmManagerServiceExt.AlarmEx alarmEx = null;
            if (alarm != null) {
                alarmEx = new AlarmManagerServiceExt.AlarmEx();
                alarmEx.setAlarm(alarm);
            }
            this.mAlarmManagerServiceExt.hwAddFirstFlagForRtcAlarm(alarmEx, backgroundIntent);
        }
    }

    /* access modifiers changed from: protected */
    public void setHwRTCAlarmLock() {
        AlarmManagerServiceExt alarmManagerServiceExt = this.mAlarmManagerServiceExt;
        if (alarmManagerServiceExt != null) {
            alarmManagerServiceExt.setHwRTCAlarmLock();
        }
    }

    /* access modifiers changed from: protected */
    public void removeDeskClockFromFWK(PendingIntent operation) {
        AlarmManagerServiceExt alarmManagerServiceExt = this.mAlarmManagerServiceExt;
        if (alarmManagerServiceExt != null) {
            alarmManagerServiceExt.removeDeskClockFromFWK(operation);
        }
    }

    /* access modifiers changed from: protected */
    public void hwRecordFirstTime() {
        AlarmManagerServiceExt alarmManagerServiceExt = this.mAlarmManagerServiceExt;
        if (alarmManagerServiceExt != null) {
            alarmManagerServiceExt.hwRecordFirstTime();
        }
    }

    /* access modifiers changed from: protected */
    public void hwRecordTimeChangeRTC(long nowRTC, long nowELAPSED, long lastTimeChangeClockTime, long expectedClockTime) {
        AlarmManagerServiceExt alarmManagerServiceExt = this.mAlarmManagerServiceExt;
        if (alarmManagerServiceExt != null) {
            alarmManagerServiceExt.hwRecordTimeChangeRTC(nowRTC, nowELAPSED, lastTimeChangeClockTime, expectedClockTime);
        }
    }

    /* access modifiers changed from: protected */
    public boolean isContainsAppUidInWorksource(WorkSource worksource, String packageName) {
        AlarmManagerServiceExt alarmManagerServiceExt = this.mAlarmManagerServiceExt;
        if (alarmManagerServiceExt != null) {
            return alarmManagerServiceExt.isContainsAppUidInWorksource(worksource, packageName);
        }
        return false;
    }

    public void removePackageAlarm(String pkg, List<String> tags, int targetUid) {
        AlarmManagerServiceExt alarmManagerServiceExt = this.mAlarmManagerServiceExt;
        if (alarmManagerServiceExt != null) {
            alarmManagerServiceExt.removePackageAlarm(pkg, tags, targetUid);
        }
    }

    /* access modifiers changed from: protected */
    public void modifyAlarmIfOverload(AlarmManagerService.Alarm alarm) {
        if (this.mAlarmManagerServiceExt != null) {
            AlarmManagerServiceExt.AlarmEx alarmEx = null;
            if (alarm != null) {
                alarmEx = new AlarmManagerServiceExt.AlarmEx();
                alarmEx.setAlarm(alarm);
            }
            this.mAlarmManagerServiceExt.modifyAlarmIfOverload(alarmEx);
        }
    }

    /* access modifiers changed from: protected */
    public void reportWakeupAlarms(ArrayList<AlarmManagerService.Alarm> alarms) {
        if (this.mAlarmManagerServiceExt != null) {
            ArrayList<AlarmManagerServiceExt.AlarmEx> alarmsEx = new ArrayList<>();
            Iterator<AlarmManagerService.Alarm> it = alarms.iterator();
            while (it.hasNext()) {
                AlarmManagerServiceExt.AlarmEx alarmEx = new AlarmManagerServiceExt.AlarmEx();
                alarmEx.setAlarm(it.next());
                alarmsEx.add(alarmEx);
            }
            this.mAlarmManagerServiceExt.reportWakeupAlarms(alarmsEx);
        }
    }

    /* access modifiers changed from: protected */
    public boolean isAwareAlarmManagerEnabled() {
        AlarmManagerServiceExt alarmManagerServiceExt = this.mAlarmManagerServiceExt;
        if (alarmManagerServiceExt != null) {
            return alarmManagerServiceExt.isAwareAlarmManagerEnabled();
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public int getWakeUpNumImpl(int uid, String pkg) {
        AlarmManagerServiceExt alarmManagerServiceExt = this.mAlarmManagerServiceExt;
        if (alarmManagerServiceExt != null) {
            return alarmManagerServiceExt.getWakeUpNumImpl(uid, pkg);
        }
        return 0;
    }

    public void setAlarmExemption(List<String> pkgWithActions, int type) {
        AlarmManagerServiceExt alarmManagerServiceExt = this.mAlarmManagerServiceExt;
        if (alarmManagerServiceExt != null) {
            alarmManagerServiceExt.setAlarmExemption(pkgWithActions, type);
        }
    }
}
