package android.text;

import com.android.internal.util.ArrayUtils;
import libcore.util.EmptyArray;

public final class AutoGrowArray {
    private static final int MAX_CAPACITY_TO_BE_KEPT = 10000;
    private static final int MIN_CAPACITY_INCREMENT = 12;

    public static class ByteArray {
        private int mSize;
        private byte[] mValues;

        public ByteArray() {
            this(10);
        }

        public ByteArray(int initialCapacity) {
            if (initialCapacity == 0) {
                this.mValues = EmptyArray.BYTE;
            } else {
                this.mValues = ArrayUtils.newUnpaddedByteArray(initialCapacity);
            }
            this.mSize = 0;
        }

        public void resize(int newSize) {
            if (newSize > this.mValues.length) {
                ensureCapacity(newSize - this.mSize);
            }
            this.mSize = newSize;
        }

        public void append(byte value) {
            ensureCapacity(1);
            byte[] bArr = this.mValues;
            int i = this.mSize;
            this.mSize = i + 1;
            bArr[i] = value;
        }

        private void ensureCapacity(int count) {
            int requestedSize = this.mSize + count;
            if (requestedSize >= this.mValues.length) {
                byte[] newValues = ArrayUtils.newUnpaddedByteArray(AutoGrowArray.computeNewCapacity(this.mSize, requestedSize));
                System.arraycopy(this.mValues, 0, newValues, 0, this.mSize);
                this.mValues = newValues;
            }
        }

        public void clear() {
            this.mSize = 0;
        }

        public void clearWithReleasingLargeArray() {
            clear();
            if (this.mValues.length > 10000) {
                this.mValues = EmptyArray.BYTE;
            }
        }

        public byte get(int index) {
            return this.mValues[index];
        }

        public void set(int index, byte value) {
            this.mValues[index] = value;
        }

        public int size() {
            return this.mSize;
        }

        public byte[] getRawArray() {
            return this.mValues;
        }
    }

    public static class FloatArray {
        private int mSize;
        private float[] mValues;

        public FloatArray() {
            this(10);
        }

        public FloatArray(int initialCapacity) {
            if (initialCapacity == 0) {
                this.mValues = EmptyArray.FLOAT;
            } else {
                this.mValues = ArrayUtils.newUnpaddedFloatArray(initialCapacity);
            }
            this.mSize = 0;
        }

        public void resize(int newSize) {
            if (newSize > this.mValues.length) {
                ensureCapacity(newSize - this.mSize);
            }
            this.mSize = newSize;
        }

        public void append(float value) {
            ensureCapacity(1);
            float[] fArr = this.mValues;
            int i = this.mSize;
            this.mSize = i + 1;
            fArr[i] = value;
        }

        private void ensureCapacity(int count) {
            int requestedSize = this.mSize + count;
            if (requestedSize >= this.mValues.length) {
                float[] newValues = ArrayUtils.newUnpaddedFloatArray(AutoGrowArray.computeNewCapacity(this.mSize, requestedSize));
                System.arraycopy(this.mValues, 0, newValues, 0, this.mSize);
                this.mValues = newValues;
            }
        }

        public void clear() {
            this.mSize = 0;
        }

        public void clearWithReleasingLargeArray() {
            clear();
            if (this.mValues.length > 10000) {
                this.mValues = EmptyArray.FLOAT;
            }
        }

        public float get(int index) {
            return this.mValues[index];
        }

        public void set(int index, float value) {
            this.mValues[index] = value;
        }

        public int size() {
            return this.mSize;
        }

        public float[] getRawArray() {
            return this.mValues;
        }
    }

    public static class IntArray {
        private int mSize;
        private int[] mValues;

        public IntArray() {
            this(10);
        }

        public IntArray(int initialCapacity) {
            if (initialCapacity == 0) {
                this.mValues = EmptyArray.INT;
            } else {
                this.mValues = ArrayUtils.newUnpaddedIntArray(initialCapacity);
            }
            this.mSize = 0;
        }

        public void resize(int newSize) {
            if (newSize > this.mValues.length) {
                ensureCapacity(newSize - this.mSize);
            }
            this.mSize = newSize;
        }

        public void append(int value) {
            ensureCapacity(1);
            int[] iArr = this.mValues;
            int i = this.mSize;
            this.mSize = i + 1;
            iArr[i] = value;
        }

        private void ensureCapacity(int count) {
            int requestedSize = this.mSize + count;
            if (requestedSize >= this.mValues.length) {
                int[] newValues = ArrayUtils.newUnpaddedIntArray(AutoGrowArray.computeNewCapacity(this.mSize, requestedSize));
                System.arraycopy(this.mValues, 0, newValues, 0, this.mSize);
                this.mValues = newValues;
            }
        }

        public void clear() {
            this.mSize = 0;
        }

        public void clearWithReleasingLargeArray() {
            clear();
            if (this.mValues.length > 10000) {
                this.mValues = EmptyArray.INT;
            }
        }

        public int get(int index) {
            return this.mValues[index];
        }

        public void set(int index, int value) {
            this.mValues[index] = value;
        }

        public int size() {
            return this.mSize;
        }

        public int[] getRawArray() {
            return this.mValues;
        }
    }

    /* access modifiers changed from: private */
    public static int computeNewCapacity(int currentSize, int requested) {
        int targetCapacity = (currentSize < 6 ? 12 : currentSize >> 1) + currentSize;
        return targetCapacity > requested ? targetCapacity : requested;
    }
}
