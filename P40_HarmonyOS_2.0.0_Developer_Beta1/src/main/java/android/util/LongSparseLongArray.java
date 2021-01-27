package android.util;

import android.annotation.UnsupportedAppUsage;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.GrowingArrayUtils;
import libcore.util.EmptyArray;

public class LongSparseLongArray implements Cloneable {
    @UnsupportedAppUsage(maxTargetSdk = 28)
    private long[] mKeys;
    @UnsupportedAppUsage(maxTargetSdk = 28)
    private int mSize;
    @UnsupportedAppUsage(maxTargetSdk = 28)
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

    @Override // java.lang.Object
    public LongSparseLongArray clone() {
        LongSparseLongArray clone = null;
        try {
            clone = (LongSparseLongArray) super.clone();
            clone.mKeys = (long[]) this.mKeys.clone();
            clone.mValues = (long[]) this.mValues.clone();
            return clone;
        } catch (CloneNotSupportedException e) {
            return clone;
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
        long[] jArr = this.mKeys;
        System.arraycopy(jArr, index + 1, jArr, index, this.mSize - (index + 1));
        long[] jArr2 = this.mValues;
        System.arraycopy(jArr2, index + 1, jArr2, index, this.mSize - (index + 1));
        this.mSize--;
    }

    public void put(long key, long value) {
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

    public long keyAt(int index) {
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
