package ohos.utils;

import java.util.Arrays;
import java.util.Optional;

public class PlainIntArray implements Cloneable {
    private static final int DEFAULT_INIT_CAP = 16;
    public static final int INVALID_INDEX = -1;
    private int capacity;
    private int[] keys;
    private int size;
    private int[] values;

    public PlainIntArray() {
        this(16);
    }

    public PlainIntArray(int i) {
        if (i > 0) {
            this.keys = new int[i];
            this.values = new int[i];
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
                    int[] iArr2 = this.values;
                    System.arraycopy(iArr2, i, iArr2, i6, this.size - i);
                }
                this.size++;
                return;
            }
            int[] iArr3 = new int[(i5 << 1)];
            int[] iArr4 = new int[(i5 << 1)];
            int i7 = i3 - i;
            System.arraycopy(this.keys, 0, iArr3, 0, i);
            int i8 = i + 1;
            System.arraycopy(this.keys, i, iArr3, i8, i7);
            System.arraycopy(this.values, 0, iArr4, 0, i);
            System.arraycopy(this.values, i, iArr4, i8, i7);
            this.keys = iArr3;
            this.values = iArr4;
            this.capacity <<= 1;
            this.size++;
        } else if (i < 0 || i >= (i2 = this.size)) {
            throw new IndexOutOfBoundsException("Forward at:" + String.valueOf(i));
        } else {
            int[] iArr5 = this.keys;
            int i9 = i + 1;
            System.arraycopy(iArr5, i9, iArr5, i, i2 - i9);
            int[] iArr6 = this.values;
            System.arraycopy(iArr6, i9, iArr6, i, this.size - i9);
            this.size--;
        }
    }

    public int locate(int i) {
        return Arrays.binarySearch(this.keys, 0, this.size, i);
    }

    public void append(int i, int i2) {
        if (isEmpty() || i > this.keys[this.size - 1]) {
            int i3 = this.size;
            adjust(i3, false);
            this.keys[i3] = i;
            this.values[i3] = i2;
            return;
        }
        put(i, i2);
    }

    public void clear() {
        this.size = 0;
    }

    @Override // java.lang.Object
    public PlainIntArray clone() {
        PlainIntArray plainIntArray = new PlainIntArray(this.capacity);
        plainIntArray.keys = (int[]) this.keys.clone();
        plainIntArray.values = (int[]) this.values.clone();
        plainIntArray.size = this.size;
        return plainIntArray;
    }

    public Optional<Integer> remove(int i) {
        int locate = locate(i);
        if (locate < 0) {
            return Optional.empty();
        }
        int i2 = this.values[locate];
        adjust(locate, true);
        return Optional.of(Integer.valueOf(i2));
    }

    public Optional<Integer> removeAt(int i) {
        if (i < 0 || i >= this.size) {
            throw new IndexOutOfBoundsException(String.valueOf(i));
        }
        int i2 = this.values[i];
        adjust(i, true);
        return Optional.of(Integer.valueOf(i2));
    }

    public Optional<Integer> get(int i) {
        int locate = locate(i);
        if (locate < 0) {
            return Optional.empty();
        }
        return Optional.of(Integer.valueOf(this.values[locate]));
    }

    public int get(int i, int i2) {
        int locate = locate(i);
        if (locate < 0) {
            return i2;
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

    public int indexOfValue(int i) {
        for (int i2 = 0; i2 < this.size; i2++) {
            if (this.values[i2] == i) {
                return i2;
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

    public int valueAt(int i) {
        if (i >= 0 && i < this.size) {
            return this.values[i];
        }
        throw new IndexOutOfBoundsException(String.valueOf(i));
    }

    public void put(int i, int i2) {
        int locate = locate(i);
        if (locate < 0) {
            locate = ~locate;
            adjust(locate, false);
        }
        this.keys[locate] = i;
        this.values[locate] = i2;
    }

    public void setValueAt(int i, int i2) {
        if (i < 0 || i >= this.size) {
            throw new IndexOutOfBoundsException(String.valueOf(i));
        }
        this.values[i] = i2;
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
            String format = String.format("\"%s\":%d", Integer.valueOf(this.keys[i]), Integer.valueOf(this.values[i]));
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
