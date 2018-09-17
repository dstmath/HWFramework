package android.text;

import com.android.internal.util.ArrayUtils;
import com.android.internal.util.GrowingArrayUtils;

class PackedIntVector {
    private final int mColumns;
    private int mRowGapLength;
    private int mRowGapStart;
    private int mRows;
    private int[] mValueGap;
    private int[] mValues;

    public PackedIntVector(int columns) {
        this.mColumns = columns;
        this.mRows = 0;
        this.mRowGapStart = 0;
        this.mRowGapLength = this.mRows;
        this.mValues = null;
        this.mValueGap = new int[(columns * 2)];
    }

    public int getValue(int row, int column) {
        int columns = this.mColumns;
        if ((row | column) < 0 || row >= size() || column >= columns) {
            throw new IndexOutOfBoundsException(row + ", " + column);
        }
        if (row >= this.mRowGapStart) {
            row += this.mRowGapLength;
        }
        int value = this.mValues[(row * columns) + column];
        int[] valuegap = this.mValueGap;
        if (row >= valuegap[column]) {
            return value + valuegap[column + columns];
        }
        return value;
    }

    public void setValue(int row, int column, int value) {
        if ((row | column) < 0 || row >= size() || column >= this.mColumns) {
            throw new IndexOutOfBoundsException(row + ", " + column);
        }
        if (row >= this.mRowGapStart) {
            row += this.mRowGapLength;
        }
        int[] valuegap = this.mValueGap;
        if (row >= valuegap[column]) {
            value -= valuegap[this.mColumns + column];
        }
        this.mValues[(this.mColumns * row) + column] = value;
    }

    private void setValueInternal(int row, int column, int value) {
        if (row >= this.mRowGapStart) {
            row += this.mRowGapLength;
        }
        int[] valuegap = this.mValueGap;
        if (row >= valuegap[column]) {
            value -= valuegap[this.mColumns + column];
        }
        this.mValues[(this.mColumns * row) + column] = value;
    }

    public void adjustValuesBelow(int startRow, int column, int delta) {
        if ((startRow | column) < 0 || startRow > size() || column >= width()) {
            throw new IndexOutOfBoundsException(startRow + ", " + column);
        }
        if (startRow >= this.mRowGapStart) {
            startRow += this.mRowGapLength;
        }
        moveValueGapTo(column, startRow);
        int[] iArr = this.mValueGap;
        int i = this.mColumns + column;
        iArr[i] = iArr[i] + delta;
    }

    public void insertAt(int row, int[] values) {
        if (row < 0 || row > size()) {
            throw new IndexOutOfBoundsException("row " + row);
        } else if (values == null || values.length >= width()) {
            moveRowGapTo(row);
            if (this.mRowGapLength == 0) {
                growBuffer();
            }
            this.mRowGapStart++;
            this.mRowGapLength--;
            int i;
            if (values == null) {
                for (i = this.mColumns - 1; i >= 0; i--) {
                    setValueInternal(row, i, 0);
                }
                return;
            }
            for (i = this.mColumns - 1; i >= 0; i--) {
                setValueInternal(row, i, values[i]);
            }
        } else {
            throw new IndexOutOfBoundsException("value count " + values.length);
        }
    }

    public void deleteAt(int row, int count) {
        if ((row | count) < 0 || row + count > size()) {
            throw new IndexOutOfBoundsException(row + ", " + count);
        }
        moveRowGapTo(row + count);
        this.mRowGapStart -= count;
        this.mRowGapLength += count;
    }

    public int size() {
        return this.mRows - this.mRowGapLength;
    }

    public int width() {
        return this.mColumns;
    }

    private final void growBuffer() {
        int columns = this.mColumns;
        int[] newvalues = ArrayUtils.newUnpaddedIntArray(GrowingArrayUtils.growSize(size()) * columns);
        int newsize = newvalues.length / columns;
        int[] valuegap = this.mValueGap;
        int rowgapstart = this.mRowGapStart;
        int after = this.mRows - (this.mRowGapLength + rowgapstart);
        if (this.mValues != null) {
            System.arraycopy(this.mValues, 0, newvalues, 0, columns * rowgapstart);
            System.arraycopy(this.mValues, (this.mRows - after) * columns, newvalues, (newsize - after) * columns, after * columns);
        }
        for (int i = 0; i < columns; i++) {
            if (valuegap[i] >= rowgapstart) {
                valuegap[i] = valuegap[i] + (newsize - this.mRows);
                if (valuegap[i] < rowgapstart) {
                    valuegap[i] = rowgapstart;
                }
            }
        }
        this.mRowGapLength += newsize - this.mRows;
        this.mRows = newsize;
        this.mValues = newvalues;
    }

    private final void moveValueGapTo(int column, int where) {
        int[] valuegap = this.mValueGap;
        int[] values = this.mValues;
        int columns = this.mColumns;
        if (where != valuegap[column]) {
            int i;
            int i2;
            if (where > valuegap[column]) {
                for (i = valuegap[column]; i < where; i++) {
                    i2 = (i * columns) + column;
                    values[i2] = values[i2] + valuegap[column + columns];
                }
            } else {
                for (i = where; i < valuegap[column]; i++) {
                    i2 = (i * columns) + column;
                    values[i2] = values[i2] - valuegap[column + columns];
                }
            }
            valuegap[column] = where;
        }
    }

    private final void moveRowGapTo(int where) {
        if (where != this.mRowGapStart) {
            int moving;
            int columns;
            int[] valuegap;
            int[] values;
            int gapend;
            int i;
            int destrow;
            int j;
            int val;
            if (where > this.mRowGapStart) {
                moving = (this.mRowGapLength + where) - (this.mRowGapStart + this.mRowGapLength);
                columns = this.mColumns;
                valuegap = this.mValueGap;
                values = this.mValues;
                gapend = this.mRowGapStart + this.mRowGapLength;
                for (i = gapend; i < gapend + moving; i++) {
                    destrow = (i - gapend) + this.mRowGapStart;
                    for (j = 0; j < columns; j++) {
                        val = values[(i * columns) + j];
                        if (i >= valuegap[j]) {
                            val += valuegap[j + columns];
                        }
                        if (destrow >= valuegap[j]) {
                            val -= valuegap[j + columns];
                        }
                        values[(destrow * columns) + j] = val;
                    }
                }
            } else {
                moving = this.mRowGapStart - where;
                columns = this.mColumns;
                valuegap = this.mValueGap;
                values = this.mValues;
                gapend = this.mRowGapStart + this.mRowGapLength;
                for (i = (where + moving) - 1; i >= where; i--) {
                    destrow = ((i - where) + gapend) - moving;
                    for (j = 0; j < columns; j++) {
                        val = values[(i * columns) + j];
                        if (i >= valuegap[j]) {
                            val += valuegap[j + columns];
                        }
                        if (destrow >= valuegap[j]) {
                            val -= valuegap[j + columns];
                        }
                        values[(destrow * columns) + j] = val;
                    }
                }
            }
            this.mRowGapStart = where;
        }
    }
}
