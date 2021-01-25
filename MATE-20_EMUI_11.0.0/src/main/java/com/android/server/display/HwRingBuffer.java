package com.android.server.display;

import android.os.SystemClock;

public final class HwRingBuffer {
    private int mCapacity;
    private int mCount;
    private int mEnd;
    private float[] mRingLux;
    private long[] mRingTime;
    private int mStart;

    public HwRingBuffer(int size) {
        this.mCapacity = size;
        int i = this.mCapacity;
        this.mRingLux = new float[i];
        this.mRingTime = new long[i];
    }

    public float getLux(int index) {
        return this.mRingLux[offsetOf(index)];
    }

    public void putLux(int index, float lux) {
        this.mRingLux[offsetOf(index)] = lux;
    }

    public long getTime(int index) {
        return this.mRingTime[offsetOf(index)];
    }

    public void push(long time, float lux) {
        int next = this.mEnd;
        int i = this.mCount;
        int i2 = this.mCapacity;
        if (i == i2) {
            int newSize = i2 * 2;
            float[] newRingLux = new float[newSize];
            long[] newRingTime = new long[newSize];
            int i3 = this.mStart;
            int length = i2 - i3;
            System.arraycopy(this.mRingLux, i3, newRingLux, 0, length);
            System.arraycopy(this.mRingTime, this.mStart, newRingTime, 0, length);
            int i4 = this.mStart;
            if (i4 != 0) {
                System.arraycopy(this.mRingLux, 0, newRingLux, length, i4);
                System.arraycopy(this.mRingTime, 0, newRingTime, length, this.mStart);
            }
            this.mRingLux = newRingLux;
            this.mRingTime = newRingTime;
            next = this.mCapacity;
            this.mCapacity = newSize;
            this.mStart = 0;
        }
        this.mRingTime[next] = time;
        this.mRingLux[next] = lux;
        this.mEnd = next + 1;
        if (this.mEnd == this.mCapacity) {
            this.mEnd = 0;
        }
        this.mCount++;
    }

    public void prune(long horizon) {
        if (this.mCount != 0) {
            while (this.mCount > 1) {
                int next = this.mStart + 1;
                int i = this.mCapacity;
                if (next >= i) {
                    next -= i;
                }
                if (this.mRingTime[next] > horizon) {
                    break;
                }
                this.mStart = next;
                this.mCount--;
            }
            long[] jArr = this.mRingTime;
            int i2 = this.mStart;
            if (jArr[i2] < horizon) {
                jArr[i2] = horizon;
            }
        }
    }

    public int size() {
        return this.mCount;
    }

    public void clear() {
        this.mStart = 0;
        this.mEnd = 0;
        this.mCount = 0;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append('[');
        int i = 0;
        while (true) {
            int i2 = this.mCount;
            if (i < i2) {
                long next = i + 1 < i2 ? getTime(i + 1) : SystemClock.uptimeMillis();
                if (i != 0) {
                    buf.append(", ");
                }
                buf.append(getLux(i));
                buf.append(" / ");
                buf.append(next - getTime(i));
                buf.append("ms");
                i++;
            } else {
                buf.append(']');
                return buf.toString();
            }
        }
    }

    public String toString(int n) {
        if (n > this.mCount) {
            n = this.mCount;
        }
        StringBuffer buf = new StringBuffer();
        buf.append('[');
        for (int i = this.mCount - n; i >= 0; i++) {
            int i2 = this.mCount;
            if (i >= i2) {
                break;
            }
            if (i != i2 - n) {
                buf.append(", ");
            }
            buf.append(getLux(i));
            buf.append("/");
            buf.append(getTime(i));
        }
        buf.append(']');
        return buf.toString();
    }

    private int offsetOf(int index) {
        if (index >= this.mCount || index < 0) {
            throw new ArrayIndexOutOfBoundsException(index);
        }
        int index2 = index + this.mStart;
        int i = this.mCapacity;
        if (index2 >= i) {
            return index2 - i;
        }
        return index2;
    }
}
