package com.android.server.wifi;

import android.os.SystemClock;

public class Clock {
    public long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    public long elapsedRealtime() {
        return SystemClock.elapsedRealtime();
    }

    public long nanoTime() {
        return System.nanoTime();
    }

    public long elapsedRealtimeNanos() {
        return SystemClock.elapsedRealtimeNanos();
    }
}
