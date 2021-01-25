package android.text;

import android.net.wifi.WifiEnterpriseConfig;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.GrowingArrayUtils;
import java.io.PrintStream;
import libcore.util.EmptyArray;

class PackedObjectVector<E> {
    private int mColumns;
    private int mRowGapLength = this.mRows;
    private int mRowGapStart = 0;
    private int mRows = 0;
    private Object[] mValues = EmptyArray.OBJECT;

    public PackedObjectVector(int columns) {
        this.mColumns = columns;
    }

    public E getValue(int row, int column) {
        if (row >= this.mRowGapStart) {
            row += this.mRowGapLength;
        }
        return (E) this.mValues[(this.mColumns * row) + column];
    }

    public void setValue(int row, int column, E value) {
        if (row >= this.mRowGapStart) {
            row += this.mRowGapLength;
        }
        this.mValues[(this.mColumns * row) + column] = value;
    }

    public void insertAt(int row, E[] values) {
        moveRowGapTo(row);
        if (this.mRowGapLength == 0) {
            growBuffer();
        }
        this.mRowGapStart++;
        this.mRowGapLength--;
        if (values == null) {
            for (int i = 0; i < this.mColumns; i++) {
                setValue(row, i, null);
            }
            return;
        }
        for (int i2 = 0; i2 < this.mColumns; i2++) {
            setValue(row, i2, values[i2]);
        }
    }

    public void deleteAt(int row, int count) {
        moveRowGapTo(row + count);
        this.mRowGapStart -= count;
        this.mRowGapLength += count;
        int i = this.mRowGapLength;
        size();
    }

    public int size() {
        return this.mRows - this.mRowGapLength;
    }

    public int width() {
        return this.mColumns;
    }

    private void growBuffer() {
        Object[] newvalues = ArrayUtils.newUnpaddedObjectArray(GrowingArrayUtils.growSize(size()) * this.mColumns);
        int length = newvalues.length;
        int i = this.mColumns;
        int newsize = length / i;
        int i2 = this.mRows;
        int i3 = this.mRowGapStart;
        int after = i2 - (this.mRowGapLength + i3);
        System.arraycopy(this.mValues, 0, newvalues, 0, i * i3);
        Object[] objArr = this.mValues;
        int i4 = this.mColumns;
        System.arraycopy(objArr, (this.mRows - after) * i4, newvalues, (newsize - after) * i4, i4 * after);
        this.mRowGapLength += newsize - this.mRows;
        this.mRows = newsize;
        this.mValues = newvalues;
    }

    private void moveRowGapTo(int where) {
        int i = this.mRowGapStart;
        if (where != i) {
            if (where > i) {
                int i2 = this.mRowGapLength;
                int moving = (where + i2) - (i + i2);
                int i3 = i + i2;
                while (true) {
                    int i4 = this.mRowGapStart;
                    int i5 = this.mRowGapLength;
                    if (i3 >= i4 + i5 + moving) {
                        break;
                    }
                    int destrow = (i3 - (i5 + i4)) + i4;
                    int j = 0;
                    while (true) {
                        int i6 = this.mColumns;
                        if (j >= i6) {
                            break;
                        }
                        Object[] objArr = this.mValues;
                        objArr[(i6 * destrow) + j] = objArr[(i3 * i6) + j];
                        j++;
                    }
                    i3++;
                }
            } else {
                int moving2 = i - where;
                for (int i7 = (where + moving2) - 1; i7 >= where; i7--) {
                    int destrow2 = (((i7 - where) + this.mRowGapStart) + this.mRowGapLength) - moving2;
                    int j2 = 0;
                    while (true) {
                        int i8 = this.mColumns;
                        if (j2 >= i8) {
                            break;
                        }
                        Object[] objArr2 = this.mValues;
                        objArr2[(i8 * destrow2) + j2] = objArr2[(i7 * i8) + j2];
                        j2++;
                    }
                }
            }
            this.mRowGapStart = where;
        }
    }

    public void dump() {
        for (int i = 0; i < this.mRows; i++) {
            int j = 0;
            while (true) {
                int i2 = this.mColumns;
                if (j >= i2) {
                    break;
                }
                Object val = this.mValues[(i2 * i) + j];
                int i3 = this.mRowGapStart;
                if (i < i3 || i >= i3 + this.mRowGapLength) {
                    PrintStream printStream = System.out;
                    printStream.print(val + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
                } else {
                    PrintStream printStream2 = System.out;
                    printStream2.print("(" + val + ") ");
                }
                j++;
            }
            System.out.print(" << \n");
        }
        System.out.print("-----\n\n");
    }
}
