package com.android.server.location;

class ExponentialBackOff {
    private static final int MULTIPLIER = 2;
    private long mCurrentIntervalMillis = (this.mInitIntervalMillis / 2);
    private final long mInitIntervalMillis;
    private final long mMaxIntervalMillis;

    ExponentialBackOff(long initIntervalMillis, long maxIntervalMillis) {
        this.mInitIntervalMillis = initIntervalMillis;
        this.mMaxIntervalMillis = maxIntervalMillis;
    }

    /* access modifiers changed from: package-private */
    public long nextBackoffMillis() {
        if (this.mCurrentIntervalMillis > this.mMaxIntervalMillis) {
            return this.mMaxIntervalMillis;
        }
        this.mCurrentIntervalMillis *= 2;
        return this.mCurrentIntervalMillis;
    }

    /* access modifiers changed from: package-private */
    public void reset() {
        this.mCurrentIntervalMillis = this.mInitIntervalMillis / 2;
    }
}
