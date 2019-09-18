package com.android.server.rms.ipcchecker;

public class BackupIpcMonitor extends HwIpcMonitorImpl {
    private static final String TAG = "RMS.BackupIpcMonitor";

    private BackupIpcMonitor(Object object, String name) {
        super(object, name);
    }

    public static BackupIpcMonitor getInstance(Object object, String name) {
        return new BackupIpcMonitor(object, name);
    }

    public boolean action() {
        return super.action();
    }
}
