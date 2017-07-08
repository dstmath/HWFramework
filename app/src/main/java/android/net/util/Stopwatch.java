package android.net.util;

import android.os.SystemClock;

public class Stopwatch {
    private long mStartTimeMs;
    private long mStopTimeMs;

    public boolean isStarted() {
        return this.mStartTimeMs > 0;
    }

    public boolean isStopped() {
        return this.mStopTimeMs > 0;
    }

    public boolean isRunning() {
        return isStarted() && !isStopped();
    }

    public Stopwatch start() {
        if (!isStarted()) {
            this.mStartTimeMs = SystemClock.elapsedRealtime();
        }
        return this;
    }

    public long stop() {
        if (isRunning()) {
            this.mStopTimeMs = SystemClock.elapsedRealtime();
        }
        return this.mStopTimeMs - this.mStartTimeMs;
    }

    public long lap() {
        if (isRunning()) {
            return SystemClock.elapsedRealtime() - this.mStartTimeMs;
        }
        return stop();
    }

    public void reset() {
        this.mStartTimeMs = 0;
        this.mStopTimeMs = 0;
    }
}
