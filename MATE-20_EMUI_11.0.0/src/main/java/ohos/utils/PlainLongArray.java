package ohos.utils;

import java.util.Arrays;
import java.util.Optional;

public class PlainLongArray implements Cloneable {
    private static final int DEFAULT_INIT_CAP = 16;
    public static final int INVALID_INDEX = -1;
    private int capacity;
    private int[] keys;
    private int size;
    private long[] values;

    public PlainLongArray() {
        this(16);
    }

    public PlainLongArray(int i) {
        if (i > 0) {
            this.keys = new int[i];
            this.values = new long[i];
            this.capacity = i;
            return;
        }
        throw new IllegalArgumentException("Invalid Capacity, " + i + " <= 0");
    }

    private void adjust(int i, boolean z) {
        int i2;
        int i3;
        if (!z) {
            if (i < 0 || i > (i3 = this.size)) {
                throw new IndexOutOfBoundsException("Backward at:" + String.valueOf(i));
            }
            int i4 = i3 + 1;
            int i5 = this.capacity;
            if (i4 <= i5) {
                if (i < i3) {
                    int[] iArr = this.keys;
                    int i6 = i + 1;
                    System.arraycopy(iArr, i, iArr, i6, i3 - i);
                    long[] jArr = this.values;
                    System.arraycopy(jArr, i, jArr, i6, this.size - i);
                }
                this.size++;
                return;
            }
            int[] iArr2 = new int[(i5 << 1)];
            long[] jArr2 = new long[(i5 << 1)];
            int i7 = i3 - i;
            System.arraycopy(this.keys, 0, iArr2, 0, i);
            int i8 = i + 1;
            System.arraycopy(this.keys, i, iArr2, i8, i7);
            System.arraycopy(this.values, 0, jArr2, 0, i);
            System.arraycopy(this.values, i, jArr2, i8, i7);
            this.keys = iArr2;
            this.values = jArr2;
            this.capacity <<= 1;
            this.size++;
        } else if (i < 0 || i >= (i2 = this.size)) {
            throw new IndexOutOfBoundsException("Forward at:" + String.valueOf(i));
        } else {
            int[] iArr3 = this.keys;
            int i9 = i + 1;
            System.arraycopy(iArr3, i9, iArr3, i, i2 - i9);
            long[] jArr3 = this.values;
            System.arraycopy(jArr3, i9, jArr3, i, this.size - i9);
            this.size--;
        }
    }

    public int locate(int i) {
        return Arrays.binarySearch(this.keys, 0, this.size, i);
    }

    public void clear() {
        this.size = 0;
    }

    public void append(int i, long j) {
        if (isEmpty() || i > this.keys[this.size - 1]) {
            int i2 = this.size;
            adjust(i2, false);
            this.keys[i2] = i;
            this.values[i2] = j;
            return;
        }
        put(i, j);
    }

    @Override // java.lang.Object
    public PlainLongArray clone() {
        PlainLongArray plainLongArray = new PlainLongArray(this.capacity);
        plainLongArray.keys = (int[]) this.keys.clone();
        plainLongArray.values = (long[]) this.values.clone();
        plainLongArray.size = this.size;
        return plainLongArray;
    }

    public Optional<Long> remove(int i) {
        int locate = locate(i);
        if (locate < 0) {
            return Optional.empty();
        }
        long j = this.values[locate];
        adjust(locate, true);
        return Optional.of(Long.valueOf(j));
    }

    public Optional<Long> removeAt(int i) {
        if (i < 0 || i >= this.size) {
            throw new IndexOutOfBoundsException(String.valueOf(i));
        }
        long j = this.values[i];
        adjust(i, true);
        return Optional.of(Long.valueOf(j));
    }

    public Optional<Long> get(int i) {
        int locate = locate(i);
        if (locate < 0) {
            return Optional.empty();
        }
        return Optional.of(Long.valueOf(this.values[locate]));
    }

    public long get(int i, long j) {
        int locate = locate(i);
        if (locate < 0) {
            return j;
        }
        return this.values[locate];
    }

    public int indexOfKey(int i) {
        int locate = locate(i);
        if (locate < 0) {
            return -1;
        }
        return locate;
    }

    public int indexOfValue(long j) {
        for (int i = 0; i < this.size; i++) {
            if (this.values[i] == j) {
                return i;
            }
        }
        return -1;
    }

    public int keyAt(int i) {
        if (i >= 0 && i < this.size) {
            return this.keys[i];
        }
        throw new IndexOutOfBoundsException(String.valueOf(i));
    }

    public long valueAt(int i) {
        if (i >= 0 && i < this.size) {
            return this.values[i];
        }
        throw new IndexOutOfBoundsException(String.valueOf(i));
    }

    public void put(int i, long j) {
        int locate = locate(i);
        if (locate < 0) {
            locate = ~locate;
            adjust(locate, false);
        }
        this.keys[locate] = i;
        this.values[locate] = j;
    }

    public int size() {
        return this.size;
    }

    public boolean isEmpty() {
        return this.size == 0;
    }

    public boolean contains(int i) {
        return locate(i) >= 0;
    }

    @Override // java.lang.Object
    public String toString() {
        if (this.size == 0) {
            return "{}";
        }
        StringBuilder sb = new StringBuilder("{");
        for (int i = 0; i < this.size; i++) {
            String format = String.format("\"%s\":%d", Integer.valueOf(this.keys[i]), Long.valueOf(this.values[i]));
            if (i == 0) {
                sb.append(format);
            } else {
                sb.append(",");
                sb.append(format);
            }
        }
        sb.append("}");
        return sb.toString();
    }
}
