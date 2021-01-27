package ohos.utils;

import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

public class LongPlainArray<E> implements Cloneable {
    private static final int DEFAULT_INIT_CAP = 16;
    public static final int INVALID_INDEX = -1;
    private int capacity;
    private long[] keys;
    private int size;
    private Object[] values;

    public LongPlainArray() {
        this(16);
    }

    public LongPlainArray(int i) {
        if (i > 0) {
            this.keys = new long[i];
            this.values = new Object[i];
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
                    long[] jArr = this.keys;
                    int i6 = i + 1;
                    System.arraycopy(jArr, i, jArr, i6, i3 - i);
                    Object[] objArr = this.values;
                    System.arraycopy(objArr, i, objArr, i6, this.size - i);
                }
                this.size++;
                return;
            }
            long[] jArr2 = new long[(i5 << 1)];
            Object[] objArr2 = new Object[(i5 << 1)];
            int i7 = i3 - i;
            System.arraycopy(this.keys, 0, jArr2, 0, i);
            int i8 = i + 1;
            System.arraycopy(this.keys, i, jArr2, i8, i7);
            System.arraycopy(this.values, 0, objArr2, 0, i);
            System.arraycopy(this.values, i, objArr2, i8, i7);
            this.keys = jArr2;
            this.values = objArr2;
            this.capacity <<= 1;
            this.size++;
        } else if (i < 0 || i >= (i2 = this.size)) {
            throw new IndexOutOfBoundsException("Forward at:" + String.valueOf(i));
        } else {
            long[] jArr3 = this.keys;
            int i9 = i + 1;
            System.arraycopy(jArr3, i9, jArr3, i, i2 - i9);
            Object[] objArr3 = this.values;
            System.arraycopy(objArr3, i9, objArr3, i, this.size - i9);
            Object[] objArr4 = this.values;
            int i10 = this.size;
            objArr4[i10 - 1] = null;
            this.size = i10 - 1;
        }
    }

    public int locate(long j) {
        return Arrays.binarySearch(this.keys, 0, this.size, j);
    }

    public int indexOfKey(long j) {
        int locate = locate(j);
        if (locate < 0) {
            return -1;
        }
        return locate;
    }

    public int indexOfValue(E e) {
        Objects.requireNonNull(e, "value is null");
        int i = 0;
        while (true) {
            Object[] objArr = this.values;
            if (i >= objArr.length) {
                return -1;
            }
            if (e.equals(objArr[i])) {
                return i;
            }
            i++;
        }
    }

    public long keyAt(int i) {
        if (i >= 0 && i < this.size) {
            return this.keys[i];
        }
        throw new IndexOutOfBoundsException(String.valueOf(i));
    }

    public E valueAt(int i) {
        if (i >= 0 && i < this.size) {
            return (E) this.values[i];
        }
        throw new IndexOutOfBoundsException(String.valueOf(i));
    }

    public Optional<E> get(long j) {
        int indexOfKey = indexOfKey(j);
        if (indexOfKey == -1) {
            return Optional.empty();
        }
        return Optional.of(this.values[indexOfKey]);
    }

    public E get(long j, E e) {
        int indexOfKey = indexOfKey(j);
        return indexOfKey == -1 ? e : (E) this.values[indexOfKey];
    }

    public void put(long j, E e) {
        Objects.requireNonNull(e, "value is null.");
        int locate = locate(j);
        if (locate > 0) {
            this.keys[locate] = j;
            this.values[locate] = e;
            return;
        }
        int i = ~locate;
        adjust(i, false);
        this.keys[i] = j;
        this.values[i] = e;
    }

    public void setValueAt(int i, E e) {
        Objects.requireNonNull(e, "value is null.");
        if (i < 0 || i >= this.size) {
            throw new IndexOutOfBoundsException(String.valueOf(i));
        }
        this.values[i] = e;
    }

    public void append(long j, E e) {
        Objects.requireNonNull(e, "value is null");
        if (isEmpty() || j > this.keys[this.size - 1]) {
            int i = this.size;
            adjust(i, false);
            this.keys[i] = j;
            this.values[i] = e;
            return;
        }
        put(j, e);
    }

    public Optional<E> remove(long j) {
        int indexOfKey = indexOfKey(j);
        if (indexOfKey == -1) {
            return Optional.empty();
        }
        return Optional.of(removeAt(indexOfKey));
    }

    public E removeAt(int i) {
        if (i < 0 || i >= this.size) {
            throw new IndexOutOfBoundsException("Index:" + i);
        }
        E e = (E) this.values[i];
        adjust(i, true);
        return e;
    }

    @Override // java.lang.Object
    public LongPlainArray<E> clone() {
        LongPlainArray<E> longPlainArray = new LongPlainArray<>(this.capacity);
        longPlainArray.keys = (long[]) this.keys.clone();
        longPlainArray.values = (Object[]) this.values.clone();
        longPlainArray.size = this.size;
        return longPlainArray;
    }

    public boolean contains(long j) {
        return locate(j) >= 0;
    }

    public void clear() {
        for (int i = 0; i < this.size; i++) {
            this.values[i] = null;
        }
        this.size = 0;
    }

    public boolean isEmpty() {
        return this.size == 0;
    }

    public int size() {
        return this.size;
    }

    @Override // java.lang.Object
    public String toString() {
        if (this.size == 0) {
            return "{}";
        }
        StringBuilder sb = new StringBuilder("{");
        for (int i = 0; i < this.size; i++) {
            String format = String.format(Locale.ENGLISH, "\"%s\":\"%s\"", Long.valueOf(this.keys[i]), this.values[i]);
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
