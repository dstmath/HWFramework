package com.android.server.display.utils;

public class RollingBuffer {
    private static final int INITIAL_SIZE = 50;
    private int mCount;
    private int mEnd;
    private int mSize = 50;
    private int mStart;
    private long[] mTimes = new long[50];
    private float[] mValues = new float[50];

    public RollingBuffer() {
        clear();
    }

    public void add(long time, float value) {
        if (this.mCount >= this.mSize) {
            expandBuffer();
        }
        long[] jArr = this.mTimes;
        int i = this.mEnd;
        jArr[i] = time;
        this.mValues[i] = value;
        this.mEnd = (i + 1) % this.mSize;
        this.mCount++;
    }

    public int size() {
        return this.mCount;
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public long getTime(int index) {
        return this.mTimes[offsetOf(index)];
    }

    public float getValue(int index) {
        return this.mValues[offsetOf(index)];
    }

    public void truncate(long minTime) {
        if (!isEmpty() && getTime(0) < minTime) {
            int index = getLatestIndexBefore(minTime);
            this.mStart = offsetOf(index);
            this.mCount -= index;
            this.mTimes[this.mStart] = minTime;
        }
    }

    public void clear() {
        this.mCount = 0;
        this.mStart = 0;
        this.mEnd = 0;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("[");
        for (int i = 0; i < this.mCount; i++) {
            int index = offsetOf(i);
            sb.append(this.mValues[index] + " @ " + this.mTimes[index]);
            if (i + 1 != this.mCount) {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    private int offsetOf(int index) {
        if (index >= 0 && index < this.mCount) {
            return (this.mStart + index) % this.mSize;
        }
        throw new ArrayIndexOutOfBoundsException("invalid index: " + index + ", mCount= " + this.mCount);
    }

    private void expandBuffer() {
        int size = this.mSize * 2;
        long[] times = new long[size];
        float[] values = new float[size];
        long[] jArr = this.mTimes;
        int i = this.mStart;
        System.arraycopy(jArr, i, times, 0, this.mCount - i);
        long[] jArr2 = this.mTimes;
        int i2 = this.mCount;
        int i3 = this.mStart;
        System.arraycopy(jArr2, 0, times, i2 - i3, i3);
        float[] fArr = this.mValues;
        int i4 = this.mStart;
        System.arraycopy(fArr, i4, values, 0, this.mCount - i4);
        float[] fArr2 = this.mValues;
        int i5 = this.mCount;
        int i6 = this.mStart;
        System.arraycopy(fArr2, 0, values, i5 - i6, i6);
        this.mSize = size;
        this.mStart = 0;
        this.mEnd = this.mCount;
        this.mTimes = times;
        this.mValues = values;
    }

    private int getLatestIndexBefore(long time) {
        int i = 1;
        while (true) {
            int i2 = this.mCount;
            if (i >= i2) {
                return i2 - 1;
            }
            if (this.mTimes[offsetOf(i)] > time) {
                return i - 1;
            }
            i++;
        }
    }
}
