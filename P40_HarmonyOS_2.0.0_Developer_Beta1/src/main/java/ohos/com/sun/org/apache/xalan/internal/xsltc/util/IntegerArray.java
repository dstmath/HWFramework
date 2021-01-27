package ohos.com.sun.org.apache.xalan.internal.xsltc.util;

import java.io.PrintStream;

public final class IntegerArray {
    private static final int InitialSize = 32;
    private int[] _array;
    private int _free;
    private int _size;

    public IntegerArray() {
        this(32);
    }

    public IntegerArray(int i) {
        this._free = 0;
        this._size = i;
        this._array = new int[i];
    }

    public IntegerArray(int[] iArr) {
        this(iArr.length);
        int[] iArr2 = this._array;
        int i = this._size;
        this._free = i;
        System.arraycopy(iArr, 0, iArr2, 0, i);
    }

    public void clear() {
        this._free = 0;
    }

    public Object clone() {
        int i = this._free;
        if (i <= 0) {
            i = 1;
        }
        IntegerArray integerArray = new IntegerArray(i);
        System.arraycopy(this._array, 0, integerArray._array, 0, this._free);
        integerArray._free = this._free;
        return integerArray;
    }

    public int[] toIntArray() {
        int[] iArr = new int[cardinality()];
        System.arraycopy(this._array, 0, iArr, 0, cardinality());
        return iArr;
    }

    public final int at(int i) {
        return this._array[i];
    }

    public final void set(int i, int i2) {
        this._array[i] = i2;
    }

    public int indexOf(int i) {
        for (int i2 = 0; i2 < this._free; i2++) {
            if (i == this._array[i2]) {
                return i2;
            }
        }
        return -1;
    }

    public final void add(int i) {
        int i2 = this._free;
        int i3 = this._size;
        if (i2 == i3) {
            growArray(i3 * 2);
        }
        int[] iArr = this._array;
        int i4 = this._free;
        this._free = i4 + 1;
        iArr[i4] = i;
    }

    public void addNew(int i) {
        for (int i2 = 0; i2 < this._free; i2++) {
            if (this._array[i2] == i) {
                return;
            }
        }
        add(i);
    }

    public void reverse() {
        int i = this._free - 1;
        for (int i2 = 0; i2 < i; i2++) {
            int[] iArr = this._array;
            int i3 = iArr[i2];
            iArr[i2] = iArr[i];
            iArr[i] = i3;
            i--;
        }
    }

    public void merge(IntegerArray integerArray) {
        int i = this._free + integerArray._free;
        int[] iArr = new int[i];
        int i2 = 0;
        int i3 = 0;
        int i4 = 0;
        while (i2 < this._free && i3 < integerArray._free) {
            int i5 = this._array[i2];
            int i6 = integerArray._array[i3];
            if (i5 < i6) {
                iArr[i4] = i5;
                i2++;
            } else {
                if (i5 > i6) {
                    iArr[i4] = i6;
                } else {
                    iArr[i4] = i5;
                    i2++;
                }
                i3++;
            }
            i4++;
        }
        if (i2 >= this._free) {
            while (i3 < integerArray._free) {
                iArr[i4] = integerArray._array[i3];
                i4++;
                i3++;
            }
        } else {
            while (i2 < this._free) {
                iArr[i4] = this._array[i2];
                i4++;
                i2++;
            }
        }
        this._array = iArr;
        this._size = i;
        this._free = i;
    }

    public void sort() {
        quicksort(this._array, 0, this._free - 1);
    }

    private static void quicksort(int[] iArr, int i, int i2) {
        if (i < i2) {
            int partition = partition(iArr, i, i2);
            quicksort(iArr, i, partition);
            quicksort(iArr, partition + 1, i2);
        }
    }

    private static int partition(int[] iArr, int i, int i2) {
        int i3 = iArr[(i + i2) >>> 1];
        int i4 = i - 1;
        int i5 = i2 + 1;
        while (true) {
            i5--;
            if (i3 >= iArr[i5]) {
                do {
                    i4++;
                } while (i3 > iArr[i4]);
                if (i4 >= i5) {
                    return i5;
                }
                int i6 = iArr[i4];
                iArr[i4] = iArr[i5];
                iArr[i5] = i6;
            }
        }
    }

    private void growArray(int i) {
        this._size = i;
        int[] iArr = new int[i];
        System.arraycopy(this._array, 0, iArr, 0, this._free);
        this._array = iArr;
    }

    public int popLast() {
        int[] iArr = this._array;
        int i = this._free - 1;
        this._free = i;
        return iArr[i];
    }

    public int last() {
        return this._array[this._free - 1];
    }

    public void setLast(int i) {
        this._array[this._free - 1] = i;
    }

    public void pop() {
        this._free--;
    }

    public void pop(int i) {
        this._free -= i;
    }

    public final int cardinality() {
        return this._free;
    }

    public void print(PrintStream printStream) {
        if (this._free > 0) {
            int i = 0;
            while (true) {
                int i2 = this._free;
                if (i < i2 - 1) {
                    printStream.print(this._array[i]);
                    printStream.print(' ');
                    i++;
                } else {
                    printStream.println(this._array[i2 - 1]);
                    return;
                }
            }
        } else {
            printStream.println("IntegerArray: empty");
        }
    }
}
