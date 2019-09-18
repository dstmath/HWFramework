package android.icu.impl;

import android.icu.impl.PropsVectors;

public class PVecToTrieCompactHandler implements PropsVectors.CompactHandler {
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
        if (rowIndex <= 65535) {
            IntTrieBuilder intTrieBuilder = new IntTrieBuilder(null, 100000, this.initialValue, this.initialValue, false);
            this.builder = intTrieBuilder;
            return;
        }
        throw new IndexOutOfBoundsException();
    }
}
