package com.huawei.zxing.aztec.encoder;

import com.huawei.zxing.common.BitArray;

/* access modifiers changed from: package-private */
public final class BinaryShiftToken extends Token {
    private final short binaryShiftByteCount;
    private final short binaryShiftStart;

    BinaryShiftToken(Token previous, int binaryShiftStart2, int binaryShiftByteCount2) {
        super(previous);
        this.binaryShiftStart = (short) binaryShiftStart2;
        this.binaryShiftByteCount = (short) binaryShiftByteCount2;
    }

    @Override // com.huawei.zxing.aztec.encoder.Token
    public void appendTo(BitArray bitArray, byte[] text) {
        int i = 0;
        while (true) {
            short s = this.binaryShiftByteCount;
            if (i < s) {
                if (i == 0 || (i == 31 && s <= 62)) {
                    bitArray.appendBits(31, 5);
                    short s2 = this.binaryShiftByteCount;
                    if (s2 > 62) {
                        bitArray.appendBits(s2 - 31, 16);
                    } else if (i == 0) {
                        bitArray.appendBits(Math.min((int) s2, 31), 5);
                    } else {
                        bitArray.appendBits(s2 - 31, 5);
                    }
                }
                bitArray.appendBits(text[this.binaryShiftStart + i], 8);
                i++;
            } else {
                return;
            }
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("<");
        sb.append((int) this.binaryShiftStart);
        sb.append("::");
        sb.append((this.binaryShiftStart + this.binaryShiftByteCount) - 1);
        sb.append('>');
        return sb.toString();
    }
}
