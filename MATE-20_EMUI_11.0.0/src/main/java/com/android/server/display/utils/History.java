package com.android.server.display.utils;

import java.time.Clock;

public class History {
    private Clock mClock;
    private int mCount;
    private int mEnd;
    private int mSize;
    private int mStart;
    private long[] mTimes;
    private float[] mValues;

    public History(int size) {
        this(size, Clock.systemUTC());
    }

    public History(int size, Clock clock) {
        this.mSize = size;
        this.mCount = 0;
        this.mStart = 0;
        this.mEnd = 0;
        this.mTimes = new long[size];
        this.mValues = new float[size];
        this.mClock = clock;
    }

    public void add(float value) {
        this.mTimes[this.mEnd] = this.mClock.millis();
        this.mValues[this.mEnd] = value;
        int i = this.mCount;
        int i2 = this.mSize;
        if (i < i2) {
            this.mCount = i + 1;
        } else {
            this.mStart = (this.mStart + 1) % i2;
        }
        this.mEnd = (this.mEnd + 1) % this.mSize;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("[");
        for (int i = 0; i < this.mCount; i++) {
            int index = (this.mStart + i) % this.mSize;
            long time = this.mTimes[index];
            float value = this.mValues[index];
            sb.append(value + " @ " + time);
            if (i + 1 != this.mCount) {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }
}
