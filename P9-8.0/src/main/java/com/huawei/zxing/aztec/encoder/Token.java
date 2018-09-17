package com.huawei.zxing.aztec.encoder;

import com.huawei.zxing.common.BitArray;

abstract class Token {
    static final Token EMPTY = new SimpleToken(null, 0, 0);
    private final Token previous;

    abstract void appendTo(BitArray bitArray, byte[] bArr);

    Token(Token previous) {
        this.previous = previous;
    }

    final Token getPrevious() {
        return this.previous;
    }

    final Token add(int value, int bitCount) {
        return new SimpleToken(this, value, bitCount);
    }

    final Token addBinaryShift(int start, int byteCount) {
        return new BinaryShiftToken(this, start, byteCount);
    }
}
