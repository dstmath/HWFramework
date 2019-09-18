package com.android.server.security.trustcircle.task;

import java.util.Timer;

public class HwSecurityTimer extends Timer {
    private static HwSecurityTimer gTimer = null;
    private static Object mLock = new Object();

    private HwSecurityTimer() {
    }

    public static HwSecurityTimer getInstance() {
        HwSecurityTimer hwSecurityTimer;
        synchronized (mLock) {
            if (gTimer == null) {
                gTimer = new HwSecurityTimer();
            }
            hwSecurityTimer = gTimer;
        }
        return hwSecurityTimer;
    }

    public static void destroyInstance() {
        synchronized (mLock) {
            if (gTimer != null) {
                gTimer = null;
            }
        }
    }
}
