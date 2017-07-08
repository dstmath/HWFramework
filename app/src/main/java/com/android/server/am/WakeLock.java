package com.android.server.am;

/* compiled from: HwPowerInfoService */
class WakeLock {
    String mWakeLockName;
    int mWakeLockPID;

    WakeLock() {
        this.mWakeLockPID = 0;
        this.mWakeLockName = null;
    }
}
