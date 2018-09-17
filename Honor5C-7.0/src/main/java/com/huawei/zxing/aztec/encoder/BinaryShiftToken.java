package com.huawei.zxing.aztec.encoder;

import com.huawei.zxing.common.BitArray;

final class BinaryShiftToken extends Token {
    private final short binaryShiftByteCount;
    private final short binaryShiftStart;

    BinaryShiftToken(Token previous, int binaryShiftStart, int binaryShiftByteCount) {
        super(previous);
        this.binaryShiftStart = (short) binaryShiftStart;
        this.binaryShiftByteCount = (short) binaryShiftByteCount;
    }

    public void appendTo(BitArray bitArray, byte[] text) {
        short i = (short) 0;
        while (i < this.binaryShiftByteCount) {
            if (i == (short) 0 || (i == (short) 31 && this.binaryShiftByteCount <= (short) 62)) {
                bitArray.appendBits(31, 5);
                if (this.binaryShiftByteCount > (short) 62) {
                    bitArray.appendBits(this.binaryShiftByteCount - 31, 16);
                } else if (i == (short) 0) {
                    bitArray.appendBits(Math.min(this.binaryShiftByteCount, 31), 5);
                } else {
                    bitArray.appendBits(this.binaryShiftByteCount - 31, 5);
                }
            }
            bitArray.appendBits(text[this.binaryShiftStart + i], 8);
            i++;
        }
    }

    public String toString() {
        return "<" + this.binaryShiftStart + "::" + ((this.binaryShiftStart + this.binaryShiftByteCount) - 1) + '>';
    }
}
