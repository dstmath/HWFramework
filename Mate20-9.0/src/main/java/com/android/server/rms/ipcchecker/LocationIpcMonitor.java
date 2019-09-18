package com.android.server.rms.ipcchecker;

public class LocationIpcMonitor extends HwIpcMonitorImpl {
    private static final String TAG = "RMS.LocationIpcMonitor";

    private LocationIpcMonitor(Object object, String name) {
        super(object, name);
    }

    public static LocationIpcMonitor getInstance(Object object, String name) {
        return new LocationIpcMonitor(object, name);
    }

    public boolean action() {
        return super.action();
    }
}
