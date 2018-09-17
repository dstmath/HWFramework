package android.icu.impl;

import android.icu.impl.PropsVectors.CompactHandler;
import android.icu.text.DateTimePatternGenerator;

public class PVecToTrieCompactHandler implements CompactHandler {
    public IntTrieBuilder builder;
    public int initialValue;

    public void setRowIndexForErrorValue(int rowIndex) {
    }

    public void setRowIndexForInitialValue(int rowIndex) {
        this.initialValue = rowIndex;
    }

    public void setRowIndexForRange(int start, int end, int rowIndex) {
        this.builder.setRange(start, end + 1, rowIndex, true);
    }

    public void startRealValues(int rowIndex) {
        if (rowIndex > DateTimePatternGenerator.MATCH_ALL_FIELDS_LENGTH) {
            throw new IndexOutOfBoundsException();
        }
        this.builder = new IntTrieBuilder(null, 100000, this.initialValue, this.initialValue, false);
    }
}
