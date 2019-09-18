package android.text;

import android.net.wifi.WifiEnterpriseConfig;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.GrowingArrayUtils;
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
        return this.mValues[(this.mColumns * row) + column];
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
        int i = 0;
        if (values == null) {
            while (i < this.mColumns) {
                setValue(row, i, null);
                i++;
            }
            return;
        }
        while (i < this.mColumns) {
            setValue(row, i, values[i]);
            i++;
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
        int newsize = newvalues.length / this.mColumns;
        int after = this.mRows - (this.mRowGapStart + this.mRowGapLength);
        System.arraycopy(this.mValues, 0, newvalues, 0, this.mColumns * this.mRowGapStart);
        System.arraycopy(this.mValues, (this.mRows - after) * this.mColumns, newvalues, (newsize - after) * this.mColumns, this.mColumns * after);
        this.mRowGapLength += newsize - this.mRows;
        this.mRows = newsize;
        this.mValues = newvalues;
    }

    private void moveRowGapTo(int where) {
        if (where != this.mRowGapStart) {
            if (where > this.mRowGapStart) {
                int moving = (this.mRowGapLength + where) - (this.mRowGapStart + this.mRowGapLength);
                for (int i = this.mRowGapStart + this.mRowGapLength; i < this.mRowGapStart + this.mRowGapLength + moving; i++) {
                    int destrow = (i - (this.mRowGapStart + this.mRowGapLength)) + this.mRowGapStart;
                    for (int j = 0; j < this.mColumns; j++) {
                        this.mValues[(this.mColumns * destrow) + j] = this.mValues[(this.mColumns * i) + j];
                    }
                }
            } else {
                int moving2 = this.mRowGapStart - where;
                for (int i2 = (where + moving2) - 1; i2 >= where; i2--) {
                    int destrow2 = (((i2 - where) + this.mRowGapStart) + this.mRowGapLength) - moving2;
                    for (int j2 = 0; j2 < this.mColumns; j2++) {
                        this.mValues[(this.mColumns * destrow2) + j2] = this.mValues[(this.mColumns * i2) + j2];
                    }
                }
            }
            this.mRowGapStart = where;
        }
    }

    public void dump() {
        for (int i = 0; i < this.mRows; i++) {
            for (int j = 0; j < this.mColumns; j++) {
                Object val = this.mValues[(this.mColumns * i) + j];
                if (i < this.mRowGapStart || i >= this.mRowGapStart + this.mRowGapLength) {
                    System.out.print(val + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
                } else {
                    System.out.print("(" + val + ") ");
                }
            }
            System.out.print(" << \n");
        }
        System.out.print("-----\n\n");
    }
}
