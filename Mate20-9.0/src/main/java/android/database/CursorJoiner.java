package android.database;

import java.util.Iterator;

public final class CursorJoiner implements Iterator<Result>, Iterable<Result> {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private int[] mColumnsLeft;
    private int[] mColumnsRight;
    private Result mCompareResult;
    private boolean mCompareResultIsValid;
    private Cursor mCursorLeft;
    private Cursor mCursorRight;
    private String[] mValues;

    public enum Result {
        RIGHT,
        LEFT,
        BOTH
    }

    public CursorJoiner(Cursor cursorLeft, String[] columnNamesLeft, Cursor cursorRight, String[] columnNamesRight) {
        if (columnNamesLeft.length == columnNamesRight.length) {
            this.mCursorLeft = cursorLeft;
            this.mCursorRight = cursorRight;
            this.mCursorLeft.moveToFirst();
            this.mCursorRight.moveToFirst();
            this.mCompareResultIsValid = false;
            this.mColumnsLeft = buildColumnIndiciesArray(cursorLeft, columnNamesLeft);
            this.mColumnsRight = buildColumnIndiciesArray(cursorRight, columnNamesRight);
            this.mValues = new String[(this.mColumnsLeft.length * 2)];
            return;
        }
        throw new IllegalArgumentException("you must have the same number of columns on the left and right, " + columnNamesLeft.length + " != " + columnNamesRight.length);
    }

    public Iterator<Result> iterator() {
        return this;
    }

    private int[] buildColumnIndiciesArray(Cursor cursor, String[] columnNames) {
        int[] columns = new int[columnNames.length];
        for (int i = 0; i < columnNames.length; i++) {
            columns[i] = cursor.getColumnIndexOrThrow(columnNames[i]);
        }
        return columns;
    }

    public boolean hasNext() {
        boolean z = true;
        if (this.mCompareResultIsValid) {
            switch (this.mCompareResult) {
                case BOTH:
                    if (this.mCursorLeft.isLast() && this.mCursorRight.isLast()) {
                        z = false;
                    }
                    return z;
                case LEFT:
                    if (this.mCursorLeft.isLast() && this.mCursorRight.isAfterLast()) {
                        z = false;
                    }
                    return z;
                case RIGHT:
                    if (this.mCursorLeft.isAfterLast() && this.mCursorRight.isLast()) {
                        z = false;
                    }
                    return z;
                default:
                    throw new IllegalStateException("bad value for mCompareResult, " + this.mCompareResult);
            }
        } else {
            if (this.mCursorLeft.isAfterLast() && this.mCursorRight.isAfterLast()) {
                z = false;
            }
            return z;
        }
    }

    public Result next() {
        if (hasNext()) {
            incrementCursors();
            boolean hasLeft = !this.mCursorLeft.isAfterLast();
            boolean hasRight = !this.mCursorRight.isAfterLast();
            if (hasLeft && hasRight) {
                populateValues(this.mValues, this.mCursorLeft, this.mColumnsLeft, 0);
                populateValues(this.mValues, this.mCursorRight, this.mColumnsRight, 1);
                switch (compareStrings(this.mValues)) {
                    case -1:
                        this.mCompareResult = Result.LEFT;
                        break;
                    case 0:
                        this.mCompareResult = Result.BOTH;
                        break;
                    case 1:
                        this.mCompareResult = Result.RIGHT;
                        break;
                }
            } else if (hasLeft) {
                this.mCompareResult = Result.LEFT;
            } else {
                this.mCompareResult = Result.RIGHT;
            }
            this.mCompareResultIsValid = true;
            return this.mCompareResult;
        }
        throw new IllegalStateException("you must only call next() when hasNext() is true");
    }

    public void remove() {
        throw new UnsupportedOperationException("not implemented");
    }

    private static void populateValues(String[] values, Cursor cursor, int[] columnIndicies, int startingIndex) {
        for (int i = 0; i < columnIndicies.length; i++) {
            values[(i * 2) + startingIndex] = cursor.getString(columnIndicies[i]);
        }
    }

    private void incrementCursors() {
        if (this.mCompareResultIsValid) {
            switch (this.mCompareResult) {
                case BOTH:
                    this.mCursorLeft.moveToNext();
                    this.mCursorRight.moveToNext();
                    break;
                case LEFT:
                    this.mCursorLeft.moveToNext();
                    break;
                case RIGHT:
                    this.mCursorRight.moveToNext();
                    break;
            }
            this.mCompareResultIsValid = false;
        }
    }

    private static int compareStrings(String... values) {
        if (values.length % 2 == 0) {
            for (int index = 0; index < values.length; index += 2) {
                int i = -1;
                if (values[index] == null) {
                    if (values[index + 1] != null) {
                        return -1;
                    }
                } else if (values[index + 1] == null) {
                    return 1;
                } else {
                    int comp = values[index].compareTo(values[index + 1]);
                    if (comp != 0) {
                        if (comp >= 0) {
                            i = 1;
                        }
                        return i;
                    }
                }
            }
            return 0;
        }
        throw new IllegalArgumentException("you must specify an even number of values");
    }
}
