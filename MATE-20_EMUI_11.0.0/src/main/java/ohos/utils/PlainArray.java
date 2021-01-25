package ohos.utils;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

public class PlainArray<E> implements Cloneable {
    private static final int INIT_CAP = 16;
    public static final int INVALID_INDEX = -1;
    private int capacity;
    private int[] keys;
    private int size;
    private Object[] values;

    public PlainArray() {
        this(16);
    }

    public PlainArray(int i) {
        if (i > 0) {
            this.keys = new int[i];
            this.values = new Object[i];
            this.capacity = i;
            return;
        }
        throw new IllegalArgumentException("Invalid Capacity, " + i + " <= 0");
    }

    @Override // java.lang.Object
    public PlainArray<E> clone() {
        PlainArray<E> plainArray = new PlainArray<>(this.capacity);
        plainArray.keys = (int[]) this.keys.clone();
        plainArray.values = (Object[]) this.values.clone();
        plainArray.size = this.size;
        return plainArray;
    }

    private void adjust(int i, boolean z) {
        if (!z) {
            adjustBackward(i);
        } else {
            adjustForward(i, 1);
        }
    }

    private void adjustBackward(int i) {
        int i2;
        if (i < 0 || i > (i2 = this.size)) {
            throw new IndexOutOfBoundsException("Backward at:" + String.valueOf(i));
        }
        int i3 = i2 + 1;
        int i4 = this.capacity;
        if (i3 <= i4) {
            if (i < i2) {
                int[] iArr = this.keys;
                int i5 = i + 1;
                System.arraycopy(iArr, i, iArr, i5, i2 - i);
                Object[] objArr = this.values;
                System.arraycopy(objArr, i, objArr, i5, this.size - i);
            }
            this.size++;
            return;
        }
        int[] iArr2 = new int[(i4 << 1)];
        Object[] objArr2 = new Object[(i4 << 1)];
        int i6 = i2 - i;
        System.arraycopy(this.keys, 0, iArr2, 0, i);
        int i7 = i + 1;
        System.arraycopy(this.keys, i, iArr2, i7, i6);
        System.arraycopy(this.values, 0, objArr2, 0, i);
        System.arraycopy(this.values, i, objArr2, i7, i6);
        this.keys = iArr2;
        this.values = objArr2;
        this.capacity <<= 1;
        this.size++;
    }

    private void adjustForward(int i, int i2) {
        int i3;
        if (i < 0 || i >= (i3 = this.size)) {
            throw new IndexOutOfBoundsException("Forward at:" + String.valueOf(i));
        }
        int[] iArr = this.keys;
        int i4 = i + i2;
        System.arraycopy(iArr, i4, iArr, i, i3 - i4);
        Object[] objArr = this.values;
        System.arraycopy(objArr, i4, objArr, i, this.size - i4);
        for (int i5 = 0; i5 < i2; i5++) {
            Object[] objArr2 = this.values;
            int i6 = this.size;
            objArr2[i6 - 1] = null;
            this.size = i6 - 1;
        }
    }

    public int locate(int i) {
        return Arrays.binarySearch(this.keys, 0, this.size, i);
    }

    public void put(int i, E e) {
        Objects.requireNonNull(e, "value is null");
        int locate = locate(i);
        if (locate < 0) {
            locate = ~locate;
            adjust(locate, false);
        }
        this.keys[locate] = i;
        this.values[locate] = e;
    }

    public void setValueAt(int i, E e) {
        Objects.requireNonNull(e, "value is null");
        if (i < 0 || i >= this.size) {
            throw new IndexOutOfBoundsException(String.valueOf(i));
        }
        this.values[i] = e;
    }

    public void append(int i, E e) {
        Objects.requireNonNull(e, "value is null");
        if (isEmpty() || i > this.keys[this.size - 1]) {
            int i2 = this.size;
            adjust(i2, false);
            this.keys[i2] = i;
            this.values[i2] = e;
            return;
        }
        put(i, e);
    }

    public void clear() {
        for (int i = 0; i < this.size; i++) {
            this.values[i] = null;
        }
        this.size = 0;
    }

    public int keyAt(int i) {
        if (i >= 0 && i < this.size) {
            return this.keys[i];
        }
        throw new IndexOutOfBoundsException(String.valueOf(i));
    }

    public Optional<E> remove(int i) {
        int locate = locate(i);
        if (locate < 0) {
            return Optional.empty();
        }
        Object obj = this.values[locate];
        adjust(locate, true);
        return Optional.of(obj);
    }

    public Optional<E> removeAt(int i) {
        if (i < 0 || i >= this.size) {
            throw new IndexOutOfBoundsException(String.valueOf(i));
        }
        Object obj = this.values[i];
        adjust(i, true);
        return Optional.of(obj);
    }

    public int removeBatchAt(int i, int i2) {
        int i3;
        if (i < 0 || i >= (i3 = this.size)) {
            throw new IndexOutOfBoundsException(String.valueOf(i));
        } else if (i2 >= 1) {
            if (i3 - (i + i2) < 0) {
                i2 = i3 - i;
            }
            adjustForward(i, i2);
            return i2;
        } else {
            throw new IllegalArgumentException("batchSize should not be less than 1");
        }
    }

    public Optional<E> get(int i) {
        int locate = locate(i);
        if (locate < 0) {
            return Optional.empty();
        }
        return Optional.of(this.values[locate]);
    }

    public E get(int i, E e) {
        int locate = locate(i);
        return locate < 0 ? e : (E) this.values[locate];
    }

    public E valueAt(int i) {
        if (i >= 0 && i < this.size) {
            return (E) this.values[i];
        }
        throw new IndexOutOfBoundsException(String.valueOf(i));
    }

    public int indexOfKey(int i) {
        int locate = locate(i);
        if (locate < 0) {
            return -1;
        }
        return locate;
    }

    public int indexOfValue(E e) {
        Objects.requireNonNull(e, "value is null");
        for (int i = 0; i < this.size; i++) {
            if (e.equals(this.values[i])) {
                return i;
            }
        }
        return -1;
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
            String format = String.format("\"%s\":\"%s\"", Integer.valueOf(this.keys[i]), this.values[i]);
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
