package com.android.server.display;

import android.os.SystemClock;

public final class HwRingBuffer {
    private int mCapacity;
    private int mCount;
    private int mEnd;
    private float[] mRingLux = new float[this.mCapacity];
    private long[] mRingTime = new long[this.mCapacity];
    private int mStart;

    public HwRingBuffer(int size) {
        this.mCapacity = size;
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
        if (this.mCount == this.mCapacity) {
            int newSize = this.mCapacity * 2;
            float[] newRingLux = new float[newSize];
            long[] newRingTime = new long[newSize];
            int length = this.mCapacity - this.mStart;
            System.arraycopy(this.mRingLux, this.mStart, newRingLux, 0, length);
            System.arraycopy(this.mRingTime, this.mStart, newRingTime, 0, length);
            if (this.mStart != 0) {
                System.arraycopy(this.mRingLux, 0, newRingLux, length, this.mStart);
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
                if (next >= this.mCapacity) {
                    next -= this.mCapacity;
                }
                if (this.mRingTime[next] > horizon) {
                    break;
                }
                this.mStart = next;
                this.mCount--;
            }
            if (this.mRingTime[this.mStart] < horizon) {
                this.mRingTime[this.mStart] = horizon;
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
        for (int i = 0; i < this.mCount; i++) {
            long next = i + 1 < this.mCount ? getTime(i + 1) : SystemClock.uptimeMillis();
            if (i != 0) {
                buf.append(", ");
            }
            buf.append(getLux(i));
            buf.append(" / ");
            buf.append(next - getTime(i));
            buf.append("ms");
        }
        buf.append(']');
        return buf.toString();
    }

    public String toString(int n) {
        if (n > this.mCount) {
            n = this.mCount;
        }
        StringBuffer buf = new StringBuffer();
        buf.append('[');
        int i = this.mCount - n;
        while (i >= 0 && i < this.mCount) {
            if (i != this.mCount - n) {
                buf.append(", ");
            }
            buf.append(getLux(i));
            buf.append("/");
            buf.append(getTime(i));
            i++;
        }
        buf.append(']');
        return buf.toString();
    }

    private int offsetOf(int index) {
        if (index >= this.mCount || index < 0) {
            throw new ArrayIndexOutOfBoundsException(index);
        }
        index += this.mStart;
        if (index >= this.mCapacity) {
            return index - this.mCapacity;
        }
        return index;
    }
}
