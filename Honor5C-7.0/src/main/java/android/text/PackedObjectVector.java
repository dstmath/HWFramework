package android.text;

import com.android.internal.util.ArrayUtils;
import com.android.internal.util.GrowingArrayUtils;
import libcore.util.EmptyArray;

class PackedObjectVector<E> {
    private int mColumns;
    private int mRowGapLength;
    private int mRowGapStart;
    private int mRows;
    private Object[] mValues;

    public PackedObjectVector(int columns) {
        this.mColumns = columns;
        this.mValues = EmptyArray.OBJECT;
        this.mRows = 0;
        this.mRowGapStart = 0;
        this.mRowGapLength = this.mRows;
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
        int i;
        if (values == null) {
            for (i = 0; i < this.mColumns; i++) {
                setValue(row, i, null);
            }
            return;
        }
        for (i = 0; i < this.mColumns; i++) {
            setValue(row, i, values[i]);
        }
    }

    public void deleteAt(int row, int count) {
        moveRowGapTo(row + count);
        this.mRowGapStart -= count;
        this.mRowGapLength += count;
        if (this.mRowGapLength <= size() * 2) {
        }
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
            int moving;
            int i;
            int destrow;
            int j;
            if (where > this.mRowGapStart) {
                moving = (this.mRowGapLength + where) - (this.mRowGapStart + this.mRowGapLength);
                for (i = this.mRowGapStart + this.mRowGapLength; i < (this.mRowGapStart + this.mRowGapLength) + moving; i++) {
                    destrow = (i - (this.mRowGapStart + this.mRowGapLength)) + this.mRowGapStart;
                    for (j = 0; j < this.mColumns; j++) {
                        this.mValues[(this.mColumns * destrow) + j] = this.mValues[(this.mColumns * i) + j];
                    }
                }
            } else {
                moving = this.mRowGapStart - where;
                for (i = (where + moving) - 1; i >= where; i--) {
                    destrow = (((i - where) + this.mRowGapStart) + this.mRowGapLength) - moving;
                    for (j = 0; j < this.mColumns; j++) {
                        this.mValues[(this.mColumns * destrow) + j] = this.mValues[(this.mColumns * i) + j];
                    }
                }
            }
            this.mRowGapStart = where;
        }
    }

    public void dump() {
        int i = 0;
        while (i < this.mRows) {
            for (int j = 0; j < this.mColumns; j++) {
                Object val = this.mValues[(this.mColumns * i) + j];
                if (i < this.mRowGapStart || i >= this.mRowGapStart + this.mRowGapLength) {
                    System.out.print(val + " ");
                } else {
                    System.out.print("(" + val + ") ");
                }
            }
            System.out.print(" << \n");
            i++;
        }
        System.out.print("-----\n\n");
    }
}
