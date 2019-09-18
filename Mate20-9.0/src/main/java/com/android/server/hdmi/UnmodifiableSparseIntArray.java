package com.android.server.hdmi;

import android.util.SparseIntArray;

final class UnmodifiableSparseIntArray {
    private static final String TAG = "ImmutableSparseIntArray";
    private final SparseIntArray mArray;

    public UnmodifiableSparseIntArray(SparseIntArray array) {
        this.mArray = array;
    }

    public int size() {
        return this.mArray.size();
    }

    public int get(int key) {
        return this.mArray.get(key);
    }

    public int get(int key, int valueIfKeyNotFound) {
        return this.mArray.get(key, valueIfKeyNotFound);
    }

    public int keyAt(int index) {
        return this.mArray.keyAt(index);
    }

    public int valueAt(int index) {
        return this.mArray.valueAt(index);
    }

    public int indexOfValue(int value) {
        return this.mArray.indexOfValue(value);
    }

    public String toString() {
        return this.mArray.toString();
    }
}
