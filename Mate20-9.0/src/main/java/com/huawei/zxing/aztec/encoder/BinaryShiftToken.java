package com.huawei.zxing.aztec.encoder;

import com.huawei.zxing.common.BitArray;

final class BinaryShiftToken extends Token {
    private final short binaryShiftByteCount;
    private final short binaryShiftStart;

    BinaryShiftToken(Token previous, int binaryShiftStart2, int binaryShiftByteCount2) {
        super(previous);
        this.binaryShiftStart = (short) binaryShiftStart2;
        this.binaryShiftByteCount = (short) binaryShiftByteCount2;
    }

    public void appendTo(BitArray bitArray, byte[] text) {
        for (int i = 0; i < this.binaryShiftByteCount; i++) {
            if (i == 0 || (i == 31 && this.binaryShiftByteCount <= 62)) {
                bitArray.appendBits(31, 5);
                if (this.binaryShiftByteCount > 62) {
                    bitArray.appendBits(this.binaryShiftByteCount - 31, 16);
                } else if (i == 0) {
                    bitArray.appendBits(Math.min(this.binaryShiftByteCount, 31), 5);
                } else {
                    bitArray.appendBits(this.binaryShiftByteCount - 31, 5);
                }
            }
            bitArray.appendBits(text[this.binaryShiftStart + i], 8);
        }
    }

    public String toString() {
        return "<" + this.binaryShiftStart + "::" + ((this.binaryShiftStart + this.binaryShiftByteCount) - 1) + '>';
    }
}
