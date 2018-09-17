package com.android.server.hdmi;

import android.util.SparseArray;

final class UnmodifiableSparseArray<E> {
    private static final String TAG = "ImmutableSparseArray";
    private final SparseArray<E> mArray;

    public UnmodifiableSparseArray(SparseArray<E> array) {
        this.mArray = array;
    }

    public int size() {
        return this.mArray.size();
    }

    public E get(int key) {
        return this.mArray.get(key);
    }

    public E get(int key, E valueIfKeyNotFound) {
        return this.mArray.get(key, valueIfKeyNotFound);
    }

    public int keyAt(int index) {
        return this.mArray.keyAt(index);
    }

    public E valueAt(int index) {
        return this.mArray.valueAt(index);
    }

    public int indexOfValue(E value) {
        return this.mArray.indexOfValue(value);
    }

    public String toString() {
        return this.mArray.toString();
    }
}
