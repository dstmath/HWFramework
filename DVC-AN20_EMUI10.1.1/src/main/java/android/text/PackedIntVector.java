package android.text;

import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.GrowingArrayUtils;

@VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
public class PackedIntVector {
    private final int mColumns;
    private int mRowGapLength = this.mRows;
    private int mRowGapStart = 0;
    private int mRows = 0;
    private int[] mValueGap;
    private int[] mValues = null;

    public PackedIntVector(int columns) {
        this.mColumns = columns;
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
            if (values == null) {
                for (int i = this.mColumns - 1; i >= 0; i--) {
                    setValueInternal(row, i, 0);
                }
                return;
            }
            for (int i2 = this.mColumns - 1; i2 >= 0; i2--) {
                setValueInternal(row, i2, values[i2]);
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
        int[] iArr = this.mValues;
        if (iArr != null) {
            System.arraycopy(iArr, 0, newvalues, 0, columns * rowgapstart);
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
            if (where > valuegap[column]) {
                for (int i = valuegap[column]; i < where; i++) {
                    int i2 = (i * columns) + column;
                    values[i2] = values[i2] + valuegap[column + columns];
                }
            } else {
                for (int i3 = where; i3 < valuegap[column]; i3++) {
                    int i4 = (i3 * columns) + column;
                    values[i4] = values[i4] - valuegap[column + columns];
                }
            }
            valuegap[column] = where;
        }
    }

    private final void moveRowGapTo(int where) {
        int i = this.mRowGapStart;
        if (where != i) {
            if (where > i) {
                int i2 = this.mRowGapLength;
                int moving = (where + i2) - (i + i2);
                int columns = this.mColumns;
                int[] valuegap = this.mValueGap;
                int[] values = this.mValues;
                int gapend = i + i2;
                for (int i3 = gapend; i3 < gapend + moving; i3++) {
                    int destrow = (i3 - gapend) + this.mRowGapStart;
                    for (int j = 0; j < columns; j++) {
                        int val = values[(i3 * columns) + j];
                        if (i3 >= valuegap[j]) {
                            val += valuegap[j + columns];
                        }
                        if (destrow >= valuegap[j]) {
                            val -= valuegap[j + columns];
                        }
                        values[(destrow * columns) + j] = val;
                    }
                }
            } else {
                int moving2 = i - where;
                int columns2 = this.mColumns;
                int[] valuegap2 = this.mValueGap;
                int[] values2 = this.mValues;
                int gapend2 = i + this.mRowGapLength;
                for (int i4 = (where + moving2) - 1; i4 >= where; i4--) {
                    int destrow2 = ((i4 - where) + gapend2) - moving2;
                    for (int j2 = 0; j2 < columns2; j2++) {
                        int val2 = values2[(i4 * columns2) + j2];
                        if (i4 >= valuegap2[j2]) {
                            val2 += valuegap2[j2 + columns2];
                        }
                        if (destrow2 >= valuegap2[j2]) {
                            val2 -= valuegap2[j2 + columns2];
                        }
                        values2[(destrow2 * columns2) + j2] = val2;
                    }
                }
            }
            this.mRowGapStart = where;
        }
    }
}
