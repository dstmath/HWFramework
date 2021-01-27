package com.android.server.rms.ipcchecker;

public class GeneralIpcMonitor extends HwIpcMonitorImpl {
    private static final String TAG = "RMS.GeneralIpcMonitor";
    private static GeneralIpcMonitor sGeneralIpcMonitor;

    private GeneralIpcMonitor() {
    }

    public static synchronized GeneralIpcMonitor getInstance() {
        GeneralIpcMonitor generalIpcMonitor;
        synchronized (GeneralIpcMonitor.class) {
            if (sGeneralIpcMonitor == null) {
                sGeneralIpcMonitor = new GeneralIpcMonitor();
            }
            generalIpcMonitor = sGeneralIpcMonitor;
        }
        return generalIpcMonitor;
    }

    @Override // com.android.server.rms.ipcchecker.HwIpcMonitorImpl
    public boolean action(Object lock) {
        this.mLock = lock;
        return super.action();
    }
}
