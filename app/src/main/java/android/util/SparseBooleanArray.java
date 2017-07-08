package android.util;

import com.android.internal.util.ArrayUtils;
import com.android.internal.util.GrowingArrayUtils;
import libcore.util.EmptyArray;

public class SparseBooleanArray implements Cloneable {
    private int[] mKeys;
    private int mSize;
    private boolean[] mValues;

    public void put(int r1, boolean r2) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.util.SparseBooleanArray.put(int, boolean):void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 5 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.util.SparseBooleanArray.put(int, boolean):void");
    }

    public SparseBooleanArray() {
        this(10);
    }

    public SparseBooleanArray(int initialCapacity) {
        if (initialCapacity == 0) {
            this.mKeys = EmptyArray.INT;
            this.mValues = EmptyArray.BOOLEAN;
        } else {
            this.mKeys = ArrayUtils.newUnpaddedIntArray(initialCapacity);
            this.mValues = new boolean[this.mKeys.length];
        }
        this.mSize = 0;
    }

    public SparseBooleanArray clone() {
        SparseBooleanArray sparseBooleanArray = null;
        try {
            sparseBooleanArray = (SparseBooleanArray) super.clone();
            sparseBooleanArray.mKeys = (int[]) this.mKeys.clone();
            sparseBooleanArray.mValues = (boolean[]) this.mValues.clone();
            return sparseBooleanArray;
        } catch (CloneNotSupportedException e) {
            return sparseBooleanArray;
        }
    }

    public boolean get(int key) {
        return get(key, false);
    }

    public boolean get(int key, boolean valueIfKeyNotFound) {
        int i = ContainerHelpers.binarySearch(this.mKeys, this.mSize, key);
        if (i < 0) {
            return valueIfKeyNotFound;
        }
        return this.mValues[i];
    }

    public void delete(int key) {
        int i = ContainerHelpers.binarySearch(this.mKeys, this.mSize, key);
        if (i >= 0) {
            System.arraycopy(this.mKeys, i + 1, this.mKeys, i, this.mSize - (i + 1));
            System.arraycopy(this.mValues, i + 1, this.mValues, i, this.mSize - (i + 1));
            this.mSize--;
        }
    }

    public void removeAt(int index) {
        System.arraycopy(this.mKeys, index + 1, this.mKeys, index, this.mSize - (index + 1));
        System.arraycopy(this.mValues, index + 1, this.mValues, index, this.mSize - (index + 1));
        this.mSize--;
    }

    public int size() {
        return this.mSize;
    }

    public int keyAt(int index) {
        return this.mKeys[index];
    }

    public boolean valueAt(int index) {
        return this.mValues[index];
    }

    public void setValueAt(int index, boolean value) {
        this.mValues[index] = value;
    }

    public void setKeyAt(int index, int key) {
        this.mKeys[index] = key;
    }

    public int indexOfKey(int key) {
        return ContainerHelpers.binarySearch(this.mKeys, this.mSize, key);
    }

    public int indexOfValue(boolean value) {
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

    public void append(int key, boolean value) {
        if (this.mSize == 0 || key > this.mKeys[this.mSize - 1]) {
            this.mKeys = GrowingArrayUtils.append(this.mKeys, this.mSize, key);
            this.mValues = GrowingArrayUtils.append(this.mValues, this.mSize, value);
            this.mSize++;
            return;
        }
        put(key, value);
    }

    public int hashCode() {
        int hashCode = this.mSize;
        for (int i = 0; i < this.mSize; i++) {
            hashCode = (this.mKeys[i] + (hashCode * 31)) | (this.mValues[i] ? 1 : 0);
        }
        return hashCode;
    }

    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (!(that instanceof SparseBooleanArray)) {
            return false;
        }
        SparseBooleanArray other = (SparseBooleanArray) that;
        if (this.mSize != other.mSize) {
            return false;
        }
        int i = 0;
        while (i < this.mSize) {
            if (this.mKeys[i] != other.mKeys[i] || this.mValues[i] != other.mValues[i]) {
                return false;
            }
            i++;
        }
        return true;
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
