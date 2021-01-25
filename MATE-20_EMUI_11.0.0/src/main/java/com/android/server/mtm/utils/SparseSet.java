package com.android.server.mtm.utils;

import com.huawei.android.util.IntArrayEx;
import java.util.Collection;

public class SparseSet implements Cloneable {
    private static final int DEFAULT_CAPACITY = 10;
    private IntArrayEx mValues;

    public SparseSet() {
        this(10);
    }

    public SparseSet(int initialCapacity) {
        this.mValues = new IntArrayEx(initialCapacity);
    }

    @Override // java.lang.Object
    public SparseSet clone() {
        SparseSet clone = null;
        try {
            clone = (SparseSet) super.clone();
            clone.mValues = this.mValues.clone();
            return clone;
        } catch (CloneNotSupportedException e) {
            return clone;
        }
    }

    public boolean contains(int val) {
        if (this.mValues.binarySearch(val) < 0) {
            return false;
        }
        return true;
    }

    public void remove(int val) {
        int i = this.mValues.binarySearch(val);
        if (i >= 0) {
            this.mValues.remove(i);
        }
    }

    public void removeAt(int index) {
        this.mValues.remove(index);
    }

    public void add(int val) {
        int i = this.mValues.binarySearch(val);
        if (i < 0) {
            this.mValues.add(~i, val);
        }
    }

    public void addAll(SparseSet other) {
        for (int i = other.size() - 1; i >= 0; i--) {
            add(other.keyAt(i));
        }
    }

    public void addAll(Collection<Integer> other) {
        for (Integer item : other) {
            add(item.intValue());
        }
    }

    public int size() {
        return this.mValues.size();
    }

    public boolean isEmpty() {
        return this.mValues.size() == 0;
    }

    public int indexOfKey(int val) {
        return this.mValues.indexOf(val);
    }

    public int keyAt(int index) {
        return this.mValues.get(index);
    }

    public void clear() {
        this.mValues.clear();
    }

    public int[] copyKeys() {
        return this.mValues.toArray();
    }

    @Override // java.lang.Object
    public String toString() {
        if (size() <= 0) {
            return "{}";
        }
        StringBuilder buffer = new StringBuilder();
        buffer.append('{');
        for (int i = 0; i < size(); i++) {
            if (i > 0) {
                buffer.append(", ");
            }
            buffer.append(keyAt(i));
        }
        buffer.append('}');
        return buffer.toString();
    }
}
