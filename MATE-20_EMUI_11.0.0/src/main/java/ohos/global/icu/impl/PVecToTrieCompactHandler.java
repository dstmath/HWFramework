package ohos.global.icu.impl;

import ohos.global.icu.impl.PropsVectors;

public class PVecToTrieCompactHandler implements PropsVectors.CompactHandler {
    public IntTrieBuilder builder;
    public int initialValue;

    @Override // ohos.global.icu.impl.PropsVectors.CompactHandler
    public void setRowIndexForErrorValue(int i) {
    }

    @Override // ohos.global.icu.impl.PropsVectors.CompactHandler
    public void setRowIndexForInitialValue(int i) {
        this.initialValue = i;
    }

    @Override // ohos.global.icu.impl.PropsVectors.CompactHandler
    public void setRowIndexForRange(int i, int i2, int i3) {
        this.builder.setRange(i, i2 + 1, i3, true);
    }

    @Override // ohos.global.icu.impl.PropsVectors.CompactHandler
    public void startRealValues(int i) {
        if (i <= 65535) {
            int i2 = this.initialValue;
            this.builder = new IntTrieBuilder(null, 100000, i2, i2, false);
            return;
        }
        throw new IndexOutOfBoundsException();
    }
}
