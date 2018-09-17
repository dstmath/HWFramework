package android.icu.impl;

import android.icu.impl.PropsVectors.CompactHandler;
import com.android.dex.DexFormat;

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
        if (rowIndex > DexFormat.MAX_TYPE_IDX) {
            throw new IndexOutOfBoundsException();
        }
        this.builder = new IntTrieBuilder(null, 100000, this.initialValue, this.initialValue, false);
    }
}
