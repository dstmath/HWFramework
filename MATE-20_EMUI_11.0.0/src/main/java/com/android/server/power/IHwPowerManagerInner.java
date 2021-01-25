package com.android.server.power;

public interface IHwPowerManagerInner {
    HwPowerDAMonitorProxy getPowerMonitor();

    void sendNoUserActivityNotification(int i);

    boolean shouldWakeUpWhenPluggedOrUnpluggedInner(boolean z, int i, boolean z2);
}
