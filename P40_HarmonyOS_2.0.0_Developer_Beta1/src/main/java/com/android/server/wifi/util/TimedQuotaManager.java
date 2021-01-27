package com.android.server.wifi.util;

import com.android.server.wifi.Clock;

public class TimedQuotaManager {
    private final Clock mClock;
    private long mConsumedQuota = 0;
    private long mLastPeriod = 0;
    private final long mPeriodMillis;
    private final long mQuota;
    private final long mStartTimeMillis;

    public TimedQuotaManager(Clock clock, long quota, long periodMillis) {
        this.mClock = clock;
        this.mQuota = quota;
        this.mPeriodMillis = periodMillis;
        this.mStartTimeMillis = clock.getElapsedSinceBootMillis();
    }

    public boolean requestQuota() {
        long currentPeriod = getCurrentPeriod();
        if (this.mLastPeriod < currentPeriod) {
            this.mLastPeriod = currentPeriod;
            this.mConsumedQuota = 0;
        }
        long j = this.mConsumedQuota;
        if (j >= this.mQuota) {
            return false;
        }
        this.mConsumedQuota = j + 1;
        return true;
    }

    private long getCurrentPeriod() {
        return (this.mClock.getElapsedSinceBootMillis() - this.mStartTimeMillis) / this.mPeriodMillis;
    }

    public String toString() {
        return "TimedQuotaManager{mQuota=" + this.mQuota + ", mPeriodMillis=" + this.mPeriodMillis + ", mStartTimeMillis=" + this.mStartTimeMillis + ", mLastPeriod=" + this.mLastPeriod + ", mConsumedQuota=" + this.mConsumedQuota + '}';
    }
}
