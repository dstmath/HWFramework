package com.android.server.rms.ipcchecker;

public class GeneralIpcMonitor extends HwIpcMonitorImpl {
    private static final String TAG = "RMS.GeneralIpcMonitor";
    private static GeneralIpcMonitor mGeneralIpcMonitor;

    private GeneralIpcMonitor() {
    }

    public static synchronized GeneralIpcMonitor getInstance() {
        GeneralIpcMonitor generalIpcMonitor;
        synchronized (GeneralIpcMonitor.class) {
            if (mGeneralIpcMonitor == null) {
                mGeneralIpcMonitor = new GeneralIpcMonitor();
            }
            generalIpcMonitor = mGeneralIpcMonitor;
        }
        return generalIpcMonitor;
    }

    @Override // com.android.server.rms.ipcchecker.HwIpcMonitorImpl
    public boolean action(Object lock) {
        this.mLock = lock;
        return super.action();
    }
}
