package com.huawei.zxing.aztec.encoder;

import com.huawei.zxing.common.BitArray;

final class SimpleToken extends Token {
    private final short bitCount;
    private final short value;

    SimpleToken(Token previous, int value, int bitCount) {
        super(previous);
        this.value = (short) value;
        this.bitCount = (short) bitCount;
    }

    void appendTo(BitArray bitArray, byte[] text) {
        bitArray.appendBits(this.value, this.bitCount);
    }

    public String toString() {
        return '<' + Integer.toBinaryString((1 << this.bitCount) | ((this.value & ((1 << this.bitCount) - 1)) | (1 << this.bitCount))).substring(1) + '>';
    }
}
