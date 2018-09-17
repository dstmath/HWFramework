package com.android.server.wifi;

import android.os.SystemClock;

public class Clock {
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
