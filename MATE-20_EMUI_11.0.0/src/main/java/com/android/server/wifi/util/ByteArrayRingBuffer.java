package com.android.server.wifi.util;

import java.util.ArrayList;

public class ByteArrayRingBuffer {
    private ArrayList<byte[]> mArrayList;
    private int mBytesUsed;
    private int mMaxBytes;

    public ByteArrayRingBuffer(int maxBytes) {
        if (maxBytes >= 1) {
            this.mArrayList = new ArrayList<>();
            this.mMaxBytes = maxBytes;
            this.mBytesUsed = 0;
            return;
        }
        throw new IllegalArgumentException();
    }

    public boolean appendBuffer(byte[] newData) {
        pruneToSize(this.mMaxBytes - newData.length);
        if (this.mBytesUsed + newData.length > this.mMaxBytes) {
            return false;
        }
        this.mArrayList.add(newData);
        this.mBytesUsed += newData.length;
        return true;
    }

    public byte[] getBuffer(int i) {
        return this.mArrayList.get(i);
    }

    public int getNumBuffers() {
        return this.mArrayList.size();
    }

    public void resize(int maxBytes) {
        pruneToSize(maxBytes);
        this.mMaxBytes = maxBytes;
    }

    private void pruneToSize(int sizeBytes) {
        int newBytesUsed = this.mBytesUsed;
        int i = 0;
        while (i < this.mArrayList.size() && newBytesUsed > sizeBytes) {
            newBytesUsed -= this.mArrayList.get(i).length;
            i++;
        }
        this.mArrayList.subList(0, i).clear();
        this.mBytesUsed = newBytesUsed;
    }
}
