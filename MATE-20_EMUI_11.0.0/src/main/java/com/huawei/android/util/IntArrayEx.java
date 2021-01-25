package com.huawei.android.util;

import android.util.IntArray;

public class IntArrayEx implements Cloneable {
    private IntArray mArray;

    public IntArrayEx(int initialCapacity) {
        this.mArray = new IntArray(initialCapacity);
    }

    public int get(int index) {
        return this.mArray.get(index);
    }

    public void add(int index, int value) {
        this.mArray.add(index, value);
    }

    public int size() {
        return this.mArray.size();
    }

    public void remove(int index) {
        this.mArray.remove(index);
    }

    public int binarySearch(int value) {
        return this.mArray.binarySearch(value);
    }

    public int indexOf(int value) {
        return this.mArray.indexOf(value);
    }

    public void clear() {
        this.mArray.clear();
    }

    public int[] toArray() {
        return this.mArray.toArray();
    }

    @Override // java.lang.Object
    public IntArrayEx clone() throws CloneNotSupportedException {
        IntArrayEx clone;
        Object superClone = super.clone();
        if (superClone instanceof IntArrayEx) {
            clone = (IntArrayEx) superClone;
        } else {
            clone = new IntArrayEx(0);
        }
        clone.mArray = this.mArray.clone();
        return clone;
    }
}
