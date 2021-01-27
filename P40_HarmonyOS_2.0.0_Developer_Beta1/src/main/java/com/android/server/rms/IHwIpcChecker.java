package com.android.server.rms;

public interface IHwIpcChecker {
    void addMonitor(IHwIpcMonitor iHwIpcMonitor);

    IHwIpcMonitor getCurrentIpcMonitor();

    void scheduleCheckLocked();
}
