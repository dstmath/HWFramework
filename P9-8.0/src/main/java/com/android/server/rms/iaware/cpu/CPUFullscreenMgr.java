package com.android.server.rms.iaware.cpu;

import android.rms.iaware.AwareLog;

public class CPUFullscreenMgr {
    private static final String TAG = "CPUFullscreenMgr";
    private static CPUFullscreenMgr sInstance;
    private static Object sLock = new Object();
    private CPUFreqInteractive mCPUFreqInteractive;

    private CPUFullscreenMgr() {
    }

    public static CPUFullscreenMgr getInstance() {
        CPUFullscreenMgr cPUFullscreenMgr;
        synchronized (sLock) {
            if (sInstance == null) {
                sInstance = new CPUFullscreenMgr();
            }
            cPUFullscreenMgr = sInstance;
        }
        return cPUFullscreenMgr;
    }

    public void fullscreenChange(int pid, boolean isFullScreen) {
        AwareLog.d(TAG, "fullscreenChange pid = " + pid + " isFullScreen = " + isFullScreen);
        if (this.mCPUFreqInteractive != null) {
            this.mCPUFreqInteractive.fullscreenChange(pid, isFullScreen);
        }
    }

    public void setInteractiveInstance(CPUFreqInteractive instance) {
        this.mCPUFreqInteractive = instance;
    }

    public void onProcessDied(int pid) {
        if (this.mCPUFreqInteractive != null) {
            this.mCPUFreqInteractive.doDied(pid);
        }
    }
}
