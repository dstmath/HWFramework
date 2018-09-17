package com.android.server.rms;

public interface IHwIpcMonitor {
    boolean action();

    boolean action(Object obj);

    void doMonitor();

    String getMonitorName();
}
