package ohos.utils;

import java.util.Arrays;
import java.util.Optional;

public class PlainBooleanArray implements Cloneable {
    private static final int DEFAULT_INIT_CAP = 16;
    public static final int INVALID_INDEX = -1;
    private int capacity;
    private int[] keys;
    private int size;
    private boolean[] values;

    public PlainBooleanArray() {
        this(16);
    }

    public PlainBooleanArray(int i) {
        if (i > 0) {
            this.keys = new int[i];
            this.values = new boolean[i];
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
                    boolean[] zArr = this.values;
                    System.arraycopy(zArr, i, zArr, i6, this.size - i);
                }
                this.size++;
                return;
            }
            int[] iArr2 = new int[(i5 << 1)];
            boolean[] zArr2 = new boolean[(i5 << 1)];
            int i7 = i3 - i;
            System.arraycopy(this.keys, 0, iArr2, 0, i);
            int i8 = i + 1;
            System.arraycopy(this.keys, i, iArr2, i8, i7);
            System.arraycopy(this.values, 0, zArr2, 0, i);
            System.arraycopy(this.values, i, zArr2, i8, i7);
            this.keys = iArr2;
            this.values = zArr2;
            this.capacity <<= 1;
            this.size++;
        } else if (i < 0 || i >= (i2 = this.size)) {
            throw new IndexOutOfBoundsException("Forward at:" + String.valueOf(i));
        } else {
            int[] iArr3 = this.keys;
            int i9 = i + 1;
            System.arraycopy(iArr3, i9, iArr3, i, i2 - i9);
            boolean[] zArr3 = this.values;
            System.arraycopy(zArr3, i9, zArr3, i, this.size - i9);
            this.size--;
        }
    }

    public int locate(int i) {
        return Arrays.binarySearch(this.keys, 0, this.size, i);
    }

    public void append(int i, boolean z) {
        if (isEmpty() || i > this.keys[this.size - 1]) {
            int i2 = this.size;
            adjust(i2, false);
            this.keys[i2] = i;
            this.values[i2] = z;
            return;
        }
        put(i, z);
    }

    public void clear() {
        this.size = 0;
    }

    @Override // java.lang.Object
    public PlainBooleanArray clone() {
        PlainBooleanArray plainBooleanArray = new PlainBooleanArray(this.capacity);
        plainBooleanArray.keys = (int[]) this.keys.clone();
        plainBooleanArray.values = (boolean[]) this.values.clone();
        plainBooleanArray.size = this.size;
        return plainBooleanArray;
    }

    public Optional<Boolean> get(int i) {
        int locate = locate(i);
        if (locate < 0) {
            return Optional.empty();
        }
        return Optional.of(Boolean.valueOf(this.values[locate]));
    }

    public boolean get(int i, boolean z) {
        int locate = locate(i);
        if (locate < 0) {
            return z;
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

    public int indexOfValue(boolean z) {
        for (int i = 0; i < this.size; i++) {
            if (this.values[i] == z) {
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

    public boolean valueAt(int i) {
        if (i >= 0 && i < this.size) {
            return this.values[i];
        }
        throw new IndexOutOfBoundsException(String.valueOf(i));
    }

    public void put(int i, boolean z) {
        int locate = locate(i);
        if (locate < 0) {
            locate = ~locate;
            adjust(locate, false);
        }
        this.keys[locate] = i;
        this.values[locate] = z;
    }

    public void setValueAt(int i, boolean z) {
        if (i < 0 || i >= this.size) {
            throw new IndexOutOfBoundsException(String.valueOf(i));
        }
        this.values[i] = z;
    }

    public Optional<Boolean> remove(int i) {
        int locate = locate(i);
        if (locate < 0) {
            return Optional.empty();
        }
        boolean z = this.values[locate];
        adjust(locate, true);
        return Optional.of(Boolean.valueOf(z));
    }

    public Optional<Boolean> removeAt(int i) {
        if (i < 0 || i >= this.size) {
            throw new IndexOutOfBoundsException(String.valueOf(i));
        }
        boolean z = this.values[i];
        adjust(i, true);
        return Optional.of(Boolean.valueOf(z));
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
            String format = String.format("\"%s\":%s", Integer.valueOf(this.keys[i]), Boolean.valueOf(this.values[i]));
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
