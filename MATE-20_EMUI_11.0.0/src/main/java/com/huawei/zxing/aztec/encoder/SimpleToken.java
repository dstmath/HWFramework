package com.huawei.zxing.aztec.encoder;

import com.huawei.zxing.common.BitArray;

/* access modifiers changed from: package-private */
public final class SimpleToken extends Token {
    private final short bitCount;
    private final short value;

    SimpleToken(Token previous, int value2, int bitCount2) {
        super(previous);
        this.value = (short) value2;
        this.bitCount = (short) bitCount2;
    }

    /* access modifiers changed from: package-private */
    @Override // com.huawei.zxing.aztec.encoder.Token
    public void appendTo(BitArray bitArray, byte[] text) {
        bitArray.appendBits(this.value, this.bitCount);
    }

    public String toString() {
        short s = this.value;
        short s2 = this.bitCount;
        int value2 = (s & ((1 << s2) - 1)) | (1 << s2);
        return '<' + Integer.toBinaryString((1 << this.bitCount) | value2).substring(1) + '>';
    }
}
