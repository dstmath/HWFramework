package org.bouncycastle.pqc.crypto.qtesla;

final class IntSlicer {
    private int base;
    private final int[] values;

    IntSlicer(int[] iArr, int i) {
        this.values = iArr;
        this.base = i;
    }

    /* access modifiers changed from: package-private */
    public final int at(int i) {
        return this.values[this.base + i];
    }

    /* access modifiers changed from: package-private */
    public final int at(int i, int i2) {
        this.values[this.base + i] = i2;
        return i2;
    }

    /* access modifiers changed from: package-private */
    public final int at(int i, long j) {
        int[] iArr = this.values;
        int i2 = this.base + i;
        int i3 = (int) j;
        iArr[i2] = i3;
        return i3;
    }

    /* access modifiers changed from: package-private */
    public final IntSlicer copy() {
        return new IntSlicer(this.values, this.base);
    }

    /* access modifiers changed from: package-private */
    public final IntSlicer from(int i) {
        return new IntSlicer(this.values, this.base + i);
    }

    /* access modifiers changed from: package-private */
    public final void incBase(int i) {
        this.base += i;
    }
}
