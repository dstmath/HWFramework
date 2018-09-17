package android.util;

import com.android.internal.util.ArrayUtils;
import libcore.util.EmptyArray;

public class LongArray implements Cloneable {
    private static final int MIN_CAPACITY_INCREMENT = 12;
    private int mSize;
    private long[] mValues;

    public LongArray() {
        this(10);
    }

    public LongArray(int initialCapacity) {
        if (initialCapacity == 0) {
            this.mValues = EmptyArray.LONG;
        } else {
            this.mValues = ArrayUtils.newUnpaddedLongArray(initialCapacity);
        }
        this.mSize = 0;
    }

    public void add(long value) {
        add(this.mSize, value);
    }

    public void add(int index, long value) {
        if (index < 0 || index > this.mSize) {
            throw new IndexOutOfBoundsException();
        }
        ensureCapacity(1);
        if (this.mSize - index != 0) {
            System.arraycopy(this.mValues, index, this.mValues, index + 1, this.mSize - index);
        }
        this.mValues[index] = value;
        this.mSize++;
    }

    public void addAll(LongArray values) {
        int count = values.mSize;
        ensureCapacity(count);
        System.arraycopy(values.mValues, 0, this.mValues, this.mSize, count);
        this.mSize += count;
    }

    private void ensureCapacity(int count) {
        int currentSize = this.mSize;
        int minCapacity = currentSize + count;
        if (minCapacity >= this.mValues.length) {
            int targetCap = currentSize + (currentSize < 6 ? 12 : currentSize >> 1);
            long[] newValues = ArrayUtils.newUnpaddedLongArray(targetCap > minCapacity ? targetCap : minCapacity);
            System.arraycopy(this.mValues, 0, newValues, 0, currentSize);
            this.mValues = newValues;
        }
    }

    public void clear() {
        this.mSize = 0;
    }

    public LongArray clone() {
        LongArray longArray = null;
        try {
            longArray = (LongArray) super.clone();
            longArray.mValues = (long[]) this.mValues.clone();
            return longArray;
        } catch (CloneNotSupportedException e) {
            return longArray;
        }
    }

    public long get(int index) {
        if (index < this.mSize) {
            return this.mValues[index];
        }
        throw new ArrayIndexOutOfBoundsException(this.mSize, index);
    }

    public int indexOf(long value) {
        int n = this.mSize;
        for (int i = 0; i < n; i++) {
            if (this.mValues[i] == value) {
                return i;
            }
        }
        return -1;
    }

    public void remove(int index) {
        if (index >= this.mSize) {
            throw new ArrayIndexOutOfBoundsException(this.mSize, index);
        }
        System.arraycopy(this.mValues, index + 1, this.mValues, index, (this.mSize - index) - 1);
        this.mSize--;
    }

    public int size() {
        return this.mSize;
    }
}
