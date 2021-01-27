package android.util;

import com.android.internal.util.ArrayUtils;
import com.android.internal.util.GrowingArrayUtils;
import libcore.util.EmptyArray;

public class SparseLongArray implements Cloneable {
    private int[] mKeys;
    private int mSize;
    private long[] mValues;

    public SparseLongArray() {
        this(10);
    }

    public SparseLongArray(int initialCapacity) {
        if (initialCapacity == 0) {
            this.mKeys = EmptyArray.INT;
            this.mValues = EmptyArray.LONG;
        } else {
            this.mValues = ArrayUtils.newUnpaddedLongArray(initialCapacity);
            this.mKeys = new int[this.mValues.length];
        }
        this.mSize = 0;
    }

    @Override // java.lang.Object
    public SparseLongArray clone() {
        SparseLongArray clone = null;
        try {
            clone = (SparseLongArray) super.clone();
            clone.mKeys = (int[]) this.mKeys.clone();
            clone.mValues = (long[]) this.mValues.clone();
            return clone;
        } catch (CloneNotSupportedException e) {
            return clone;
        }
    }

    public long get(int key) {
        return get(key, 0);
    }

    public long get(int key, long valueIfKeyNotFound) {
        int i = ContainerHelpers.binarySearch(this.mKeys, this.mSize, key);
        if (i < 0) {
            return valueIfKeyNotFound;
        }
        return this.mValues[i];
    }

    public void delete(int key) {
        int i = ContainerHelpers.binarySearch(this.mKeys, this.mSize, key);
        if (i >= 0) {
            removeAt(i);
        }
    }

    public void removeAtRange(int index, int size) {
        int size2 = Math.min(size, this.mSize - index);
        int[] iArr = this.mKeys;
        System.arraycopy(iArr, index + size2, iArr, index, this.mSize - (index + size2));
        long[] jArr = this.mValues;
        System.arraycopy(jArr, index + size2, jArr, index, this.mSize - (index + size2));
        this.mSize -= size2;
    }

    public void removeAt(int index) {
        int[] iArr = this.mKeys;
        System.arraycopy(iArr, index + 1, iArr, index, this.mSize - (index + 1));
        long[] jArr = this.mValues;
        System.arraycopy(jArr, index + 1, jArr, index, this.mSize - (index + 1));
        this.mSize--;
    }

    public void put(int key, long value) {
        int i = ContainerHelpers.binarySearch(this.mKeys, this.mSize, key);
        if (i >= 0) {
            this.mValues[i] = value;
            return;
        }
        int i2 = ~i;
        this.mKeys = GrowingArrayUtils.insert(this.mKeys, this.mSize, i2, key);
        this.mValues = GrowingArrayUtils.insert(this.mValues, this.mSize, i2, value);
        this.mSize++;
    }

    public int size() {
        return this.mSize;
    }

    public int keyAt(int index) {
        if (index < this.mSize || !UtilConfig.sThrowExceptionForUpperArrayOutOfBounds) {
            return this.mKeys[index];
        }
        throw new ArrayIndexOutOfBoundsException(index);
    }

    public long valueAt(int index) {
        if (index < this.mSize || !UtilConfig.sThrowExceptionForUpperArrayOutOfBounds) {
            return this.mValues[index];
        }
        throw new ArrayIndexOutOfBoundsException(index);
    }

    public int indexOfKey(int key) {
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

    public void append(int key, long value) {
        int i = this.mSize;
        if (i == 0 || key > this.mKeys[i - 1]) {
            this.mKeys = GrowingArrayUtils.append(this.mKeys, this.mSize, key);
            this.mValues = GrowingArrayUtils.append(this.mValues, this.mSize, value);
            this.mSize++;
            return;
        }
        put(key, value);
    }

    @Override // java.lang.Object
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
