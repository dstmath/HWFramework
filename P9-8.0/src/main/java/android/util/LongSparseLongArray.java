package android.util;

import com.android.internal.util.ArrayUtils;
import com.android.internal.util.GrowingArrayUtils;
import libcore.util.EmptyArray;

public class LongSparseLongArray implements Cloneable {
    private long[] mKeys;
    private int mSize;
    private long[] mValues;

    public LongSparseLongArray() {
        this(10);
    }

    public LongSparseLongArray(int initialCapacity) {
        if (initialCapacity == 0) {
            this.mKeys = EmptyArray.LONG;
            this.mValues = EmptyArray.LONG;
        } else {
            this.mKeys = ArrayUtils.newUnpaddedLongArray(initialCapacity);
            this.mValues = new long[this.mKeys.length];
        }
        this.mSize = 0;
    }

    public LongSparseLongArray clone() {
        LongSparseLongArray longSparseLongArray = null;
        try {
            longSparseLongArray = (LongSparseLongArray) super.clone();
            longSparseLongArray.mKeys = (long[]) this.mKeys.clone();
            longSparseLongArray.mValues = (long[]) this.mValues.clone();
            return longSparseLongArray;
        } catch (CloneNotSupportedException e) {
            return longSparseLongArray;
        }
    }

    public long get(long key) {
        return get(key, 0);
    }

    public long get(long key, long valueIfKeyNotFound) {
        int i = ContainerHelpers.binarySearch(this.mKeys, this.mSize, key);
        if (i < 0) {
            return valueIfKeyNotFound;
        }
        return this.mValues[i];
    }

    public void delete(long key) {
        int i = ContainerHelpers.binarySearch(this.mKeys, this.mSize, key);
        if (i >= 0) {
            removeAt(i);
        }
    }

    public void removeAt(int index) {
        System.arraycopy(this.mKeys, index + 1, this.mKeys, index, this.mSize - (index + 1));
        System.arraycopy(this.mValues, index + 1, this.mValues, index, this.mSize - (index + 1));
        this.mSize--;
    }

    public void put(long key, long value) {
        int i = ContainerHelpers.binarySearch(this.mKeys, this.mSize, key);
        if (i >= 0) {
            this.mValues[i] = value;
            return;
        }
        i = ~i;
        this.mKeys = GrowingArrayUtils.insert(this.mKeys, this.mSize, i, key);
        this.mValues = GrowingArrayUtils.insert(this.mValues, this.mSize, i, value);
        this.mSize++;
    }

    public int size() {
        return this.mSize;
    }

    public long keyAt(int index) {
        return this.mKeys[index];
    }

    public long valueAt(int index) {
        return this.mValues[index];
    }

    public int indexOfKey(long key) {
        return ContainerHelpers.binarySearch(this.mKeys, this.mSize, key);
    }

    public int indexOfValue(long value) {
        for (int i = 0; i < this.mSize; i++) {
            if (this.mValues[i] == value) {
                return i;
            }
        }
        return -1;
    }

    public void clear() {
        this.mSize = 0;
    }

    public void append(long key, long value) {
        if (this.mSize == 0 || key > this.mKeys[this.mSize - 1]) {
            this.mKeys = GrowingArrayUtils.append(this.mKeys, this.mSize, key);
            this.mValues = GrowingArrayUtils.append(this.mValues, this.mSize, value);
            this.mSize++;
            return;
        }
        put(key, value);
    }

    public String toString() {
        if (size() <= 0) {
            return "{}";
        }
        StringBuilder buffer = new StringBuilder(this.mSize * 28);
        buffer.append('{');
        for (int i = 0; i < this.mSize; i++) {
            if (i > 0) {
                buffer.append(", ");
            }
            buffer.append(keyAt(i));
            buffer.append('=');
            buffer.append(valueAt(i));
        }
        buffer.append('}');
        return buffer.toString();
    }
}
