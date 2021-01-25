package com.huawei.zxing.aztec.encoder;

import com.huawei.zxing.common.BitArray;

abstract class Token {
    static final Token EMPTY = new SimpleToken(null, 0, 0);
    private final Token previous;

    /* access modifiers changed from: package-private */
    public abstract void appendTo(BitArray bitArray, byte[] bArr);

    Token(Token previous2) {
        this.previous = previous2;
    }

    /* access modifiers changed from: package-private */
    public final Token getPrevious() {
        return this.previous;
    }

    /* access modifiers changed from: package-private */
    public final Token add(int value, int bitCount) {
        return new SimpleToken(this, value, bitCount);
    }

    /* access modifiers changed from: package-private */
    public final Token addBinaryShift(int start, int byteCount) {
        return new BinaryShiftToken(this, start, byteCount);
    }
}
