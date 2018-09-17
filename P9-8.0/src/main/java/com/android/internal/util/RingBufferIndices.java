package com.android.internal.util;

public class RingBufferIndices {
    private final int mCapacity;
    private int mSize;
    private int mStart;

    public RingBufferIndices(int capacity) {
        this.mCapacity = capacity;
    }

    public int add() {
        int pos;
        if (this.mSize < this.mCapacity) {
            pos = this.mSize;
            this.mSize++;
            return pos;
        }
        pos = this.mStart;
        this.mStart++;
        if (this.mStart == this.mCapacity) {
            this.mStart = 0;
        }
        return pos;
    }

    public void clear() {
        this.mStart = 0;
        this.mSize = 0;
    }

    public int size() {
        return this.mSize;
    }

    public int indexOf(int pos) {
        int index = this.mStart + pos;
        if (index >= this.mCapacity) {
            return index - this.mCapacity;
        }
        return index;
    }
}
