package com.android.server;

import com.android.server.AlarmManagerService.Alarm;
import java.io.PrintWriter;
import java.util.List;

public class SmartHeartBeatDummy {
    public void setAlarmsPending(List<String> list, List<String> list2, boolean pending, int pendingType) {
    }

    public void removeAllPendingAlarms() {
    }

    public void setAlarmsAdjust(List<String> list, List<String> list2, boolean adjust, int type, long interval, int mode) {
    }

    public void removeAllAdjustAlarms() {
    }

    public void adjustAlarmIfNeeded(Alarm a) {
    }

    public boolean shouldPendingAlarm(Alarm a) {
        return false;
    }

    public void dump(PrintWriter pw) {
    }
}
