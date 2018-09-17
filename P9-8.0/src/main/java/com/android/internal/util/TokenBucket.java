package com.android.internal.util;

import android.os.SystemClock;

public class TokenBucket {
    private int mAvailable;
    private final int mCapacity;
    private final int mFillDelta;
    private long mLastFill;

    public TokenBucket(int deltaMs, int capacity, int tokens) {
        this.mFillDelta = Preconditions.checkArgumentPositive(deltaMs, "deltaMs must be strictly positive");
        this.mCapacity = Preconditions.checkArgumentPositive(capacity, "capacity must be strictly positive");
        this.mAvailable = Math.min(Preconditions.checkArgumentNonnegative(tokens), this.mCapacity);
        this.mLastFill = scaledTime();
    }

    public TokenBucket(int deltaMs, int capacity) {
        this(deltaMs, capacity, capacity);
    }

    public void reset(int tokens) {
        Preconditions.checkArgumentNonnegative(tokens);
        this.mAvailable = Math.min(tokens, this.mCapacity);
        this.mLastFill = scaledTime();
    }

    public int capacity() {
        return this.mCapacity;
    }

    public int available() {
        fill();
        return this.mAvailable;
    }

    public boolean has() {
        fill();
        if (this.mAvailable > 0) {
            return true;
        }
        return false;
    }

    public boolean get() {
        return get(1) == 1;
    }

    public int get(int n) {
        fill();
        if (n <= 0) {
            return 0;
        }
        if (n > this.mAvailable) {
            int got = this.mAvailable;
            this.mAvailable = 0;
            return got;
        }
        this.mAvailable -= n;
        return n;
    }

    private void fill() {
        long now = scaledTime();
        this.mAvailable = Math.min(this.mCapacity, this.mAvailable + ((int) (now - this.mLastFill)));
        this.mLastFill = now;
    }

    private long scaledTime() {
        return SystemClock.elapsedRealtime() / ((long) this.mFillDelta);
    }
}
