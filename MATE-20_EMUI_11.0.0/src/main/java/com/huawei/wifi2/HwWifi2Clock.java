package com.huawei.wifi2;

import android.os.SystemClock;

public class HwWifi2Clock {
    public long getWallClockMillis() {
        return System.currentTimeMillis();
    }

    public long getElapsedSinceBootMillis() {
        return SystemClock.elapsedRealtime();
    }

    public long getElapsedSinceBootNanos() {
        return SystemClock.elapsedRealtimeNanos();
    }

    public long getUptimeSinceBootMillis() {
        return SystemClock.uptimeMillis();
    }
}
