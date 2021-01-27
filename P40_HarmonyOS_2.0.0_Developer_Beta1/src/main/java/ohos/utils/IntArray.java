package ohos.utils;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class IntArray {
    public static final int ERROR_INDEX = -1;
    private static final int MIN_CAPACITY = 32;
    private static final int SIZE_OF_INT = 4;
    private int[] array;
    private int arraySize;

    public IntArray() {
        this(32);
    }

    public IntArray(int i) {
        this.array = new int[i];
    }

    public IntArray(int i, int i2) {
        this(i);
        Arrays.fill(this.array, i2);
        this.arraySize = i;
    }

    public IntArray(IntArray intArray) {
        Objects.requireNonNull(intArray, String.format(Locale.ENGLISH, "input IntArray is null", new Object[0]));
        this.arraySize = intArray.arraySize;
        int i = this.arraySize;
        this.array = new int[i];
        System.arraycopy(intArray.array, 0, this.array, 0, i);
    }

    public IntArray(int... iArr) {
        this(iArr, 0, iArr.length);
    }

    public IntArray(int[] iArr, int i, int i2) {
        this(i2);
        if (iArr == null) {
            throw new IllegalArgumentException(String.format(Locale.ENGLISH, "input IntArray is null", new Object[0]));
        } else if (i < 0 || i2 < 0 || (i + i2) - 1 >= iArr.length) {
            throw new ArrayIndexOutOfBoundsException(String.format(Locale.ENGLISH, "Start from %d Index %d out of bounds for length %d", Integer.valueOf(i), Integer.valueOf(i + i2), Integer.valueOf(this.arraySize)));
        } else {
            this.arraySize = i2;
            System.arraycopy(iArr, i, this.array, 0, i2);
        }
    }

    public IntArray(List<Integer> list) {
        int i = 0;
        Objects.requireNonNull(list, String.format(Locale.ENGLISH, "intList is null", new Object[0]));
        this.arraySize = list.size();
        this.array = new int[this.arraySize];
        for (Integer num : list) {
            this.array[i] = num.intValue();
            i++;
        }
    }

    public int hashCode() {
        return ((Arrays.hashCode(this.array) + 31) * 31) + this.arraySize;
    }

    public boolean equals(int[] iArr) {
        return equals(new IntArray(iArr));
    }

    public boolean equals(Object obj) {
        Objects.requireNonNull(obj, String.format(Locale.ENGLISH, "input IntArray is null", new Object[0]));
        if (this == obj) {
            return true;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        IntArray intArray = (IntArray) obj;
        if (this.arraySize != intArray.arraySize) {
            return false;
        }
        for (int i = 0; i < this.arraySize; i++) {
            if (this.array[i] != intArray.array[i]) {
                return false;
            }
        }
        return true;
    }

    public Object clone() {
        return new IntArray(this);
    }

    public String toString() {
        return toString(", ");
    }

    public String toString(String str) {
        int i = this.arraySize;
        if (i == 0) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder(i * 4);
        sb.append("[");
        for (int i2 = 0; i2 < this.arraySize; i2++) {
            sb.append(this.array[i2]);
            sb.append(str);
        }
        sb.delete(sb.length() - str.length(), sb.length());
        sb.append("]");
        return sb.toString();
    }

    public int getCapacity() {
        return this.array.length;
    }

    private void ensureCapacity(int i) {
        int[] iArr = this.array;
        if (i >= iArr.length) {
            resize(Math.max(32, iArr.length << 1));
        }
    }

    private void resize(int i) {
        int i2 = this.arraySize;
        if (i <= i2) {
            this.arraySize = i;
            return;
        }
        int[] iArr = this.array;
        if (i > iArr.length) {
            int[] iArr2 = new int[i];
            System.arraycopy(iArr, 0, iArr2, 0, i2);
            this.array = iArr2;
        }
    }

    public void insert(int i, int i2) {
        checkIndex(i);
        ensureCapacity(this.arraySize + 1);
        int[] iArr = this.array;
        System.arraycopy(iArr, i, iArr, i + 1, this.arraySize - i);
        this.arraySize++;
        this.array[i] = i2;
    }

    private void checkIndex(int i) {
        if (i >= this.arraySize || i < 0) {
            throw new ArrayIndexOutOfBoundsException(String.format(Locale.ENGLISH, "Index %d out of bounds for length %d", Integer.valueOf(i), Integer.valueOf(this.arraySize)));
        }
    }

    public int getAt(int i) {
        checkIndex(i);
        return this.array[i];
    }

    public void setAt(int i, int i2) {
        checkIndex(i);
        this.array[i] = i2;
    }

    public int indexOf(int i) {
        for (int i2 = 0; i2 < this.arraySize; i2++) {
            if (this.array[i2] == i) {
                return i2;
            }
        }
        return -1;
    }

    public int lastIndexOf(int i) {
        for (int i2 = this.arraySize - 1; i2 >= 0; i2--) {
            if (this.array[i2] == i) {
                return i2;
            }
        }
        return -1;
    }

    public void clear() {
        Arrays.fill(this.array, 0);
        this.arraySize = 0;
    }

    public int size() {
        return this.arraySize;
    }

    public boolean empty() {
        return this.arraySize == 0;
    }

    public void pushBack(int i) {
        int i2 = this.arraySize;
        int[] iArr = this.array;
        if (i2 == iArr.length) {
            resize(Math.max(32, iArr.length << 1));
        }
        int[] iArr2 = this.array;
        int i3 = this.arraySize;
        this.arraySize = i3 + 1;
        iArr2[i3] = i;
    }

    public int popBack() {
        checkIndex(0);
        int[] iArr = this.array;
        int i = this.arraySize - 1;
        this.arraySize = i;
        return iArr[i];
    }

    public int back() {
        checkIndex(0);
        return this.array[this.arraySize - 1];
    }

    public void swap(IntArray intArray) {
        if (intArray != this) {
            int[] iArr = intArray.array;
            int i = intArray.arraySize;
            intArray.arraySize = this.arraySize;
            intArray.array = this.array;
            this.array = iArr;
            this.arraySize = i;
        }
    }

    public void append(IntArray intArray) {
        ensureCapacity(this.arraySize + intArray.arraySize);
        for (int i = 0; i < intArray.size(); i++) {
            int[] iArr = this.array;
            int i2 = this.arraySize;
            this.arraySize = i2 + 1;
            iArr[i2] = intArray.getAt(i);
        }
    }

    public Integer[] toIntegerArray() {
        Integer[] numArr = new Integer[this.arraySize];
        for (int i = 0; i < this.arraySize; i++) {
            numArr[i] = Integer.valueOf(this.array[i]);
        }
        return numArr;
    }

    public void deleteFirstValue(int i) {
        for (int i2 = 0; i2 < this.arraySize; i2++) {
            if (this.array[i2] == i) {
                deleteIndex(i2);
                return;
            }
        }
    }

    public void deleteIndex(int i) {
        checkIndex(i);
        while (true) {
            int i2 = this.arraySize;
            if (i < i2 - 1) {
                int[] iArr = this.array;
                int i3 = i + 1;
                iArr[i] = iArr[i3];
                i = i3;
            } else {
                this.arraySize = i2 - 1;
                return;
            }
        }
    }

    public void deleteRangeIn(int i, int i2) {
        int i3;
        if (i < 0 || (i3 = i2 - i) < 0 || i2 >= this.arraySize) {
            throw new ArrayIndexOutOfBoundsException(String.format(Locale.ENGLISH, "Index low: %d high %d out of bounds for length %d", Integer.valueOf(i), Integer.valueOf(i2), Integer.valueOf(this.arraySize)));
        }
        int i4 = i;
        while (true) {
            int i5 = this.arraySize;
            int i6 = i3 + 1;
            if (i4 < i5 - i6) {
                int[] iArr = this.array;
                iArr[i4] = iArr[((i4 + i2) - i) + 1];
                i4++;
            } else {
                this.arraySize = i5 - i6;
                return;
            }
        }
    }

    public void sortIns() {
        Arrays.sort(this.array, 0, this.arraySize);
    }

    public int binarySearchInInsArray(int i) {
        return Arrays.binarySearch(this.array, i);
    }
}
