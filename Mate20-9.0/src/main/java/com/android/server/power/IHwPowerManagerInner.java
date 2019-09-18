package com.android.server.power;

public interface IHwPowerManagerInner {
    HwPowerDAMonitorProxy getPowerMonitor();

    void sendNoUserActivityNotification(int i);
}
