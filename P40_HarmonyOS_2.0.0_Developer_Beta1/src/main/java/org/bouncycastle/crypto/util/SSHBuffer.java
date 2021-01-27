package org.bouncycastle.crypto.util;

import java.math.BigInteger;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.Strings;

/* access modifiers changed from: package-private */
public class SSHBuffer {
    private final byte[] buffer;
    private int pos = 0;

    public SSHBuffer(byte[] bArr) {
        this.buffer = bArr;
    }

    public SSHBuffer(byte[] bArr, byte[] bArr2) {
        this.buffer = bArr2;
        for (int i = 0; i != bArr.length; i++) {
            if (bArr[i] != bArr2[i]) {
                throw new IllegalArgumentException("magic-number incorrect");
            }
        }
        this.pos += bArr.length;
    }

    public byte[] getBuffer() {
        return Arrays.clone(this.buffer);
    }

    public boolean hasRemaining() {
        return this.pos < this.buffer.length;
    }

    public BigInteger readBigNumPositive() {
        int readU32 = readU32();
        int i = this.pos;
        int i2 = i + readU32;
        byte[] bArr = this.buffer;
        if (i2 <= bArr.length) {
            this.pos = readU32 + i;
            return new BigInteger(1, Arrays.copyOfRange(bArr, i, this.pos));
        }
        throw new IllegalArgumentException("not enough data for big num");
    }

    public byte[] readBlock() {
        int readU32 = readU32();
        if (readU32 == 0) {
            return new byte[0];
        }
        int i = this.pos;
        byte[] bArr = this.buffer;
        if (i <= bArr.length - readU32) {
            this.pos = readU32 + i;
            return Arrays.copyOfRange(bArr, i, this.pos);
        }
        throw new IllegalArgumentException("not enough data for block");
    }

    public byte[] readPaddedBlock() {
        return readPaddedBlock(8);
    }

    public byte[] readPaddedBlock(int i) {
        int i2;
        int readU32 = readU32();
        if (readU32 == 0) {
            return new byte[0];
        }
        int i3 = this.pos;
        byte[] bArr = this.buffer;
        if (i3 > bArr.length - readU32) {
            throw new IllegalArgumentException("not enough data for block");
        } else if (readU32 % i == 0) {
            this.pos = i3 + readU32;
            int i4 = this.pos;
            if (readU32 > 0 && (i2 = bArr[i4 - 1] & 255) > 0 && i2 < i) {
                i4 -= i2;
                int i5 = 1;
                int i6 = i4;
                while (i5 <= i2) {
                    if (i5 == (this.buffer[i6] & 255)) {
                        i5++;
                        i6++;
                    } else {
                        throw new IllegalArgumentException("incorrect padding");
                    }
                }
            }
            return Arrays.copyOfRange(this.buffer, i3, i4);
        } else {
            throw new IllegalArgumentException("missing padding");
        }
    }

    public String readString() {
        return Strings.fromByteArray(readBlock());
    }

    public int readU32() {
        int i = this.pos;
        byte[] bArr = this.buffer;
        if (i <= bArr.length - 4) {
            this.pos = i + 1;
            int i2 = this.pos;
            this.pos = i2 + 1;
            int i3 = ((bArr[i] & 255) << 24) | ((bArr[i2] & 255) << 16);
            int i4 = this.pos;
            this.pos = i4 + 1;
            int i5 = i3 | ((bArr[i4] & 255) << 8);
            int i6 = this.pos;
            this.pos = i6 + 1;
            return i5 | (bArr[i6] & 255);
        }
        throw new IllegalArgumentException("4 bytes for U32 exceeds buffer.");
    }

    public void skipBlock() {
        int readU32 = readU32();
        int i = this.pos;
        if (i <= this.buffer.length - readU32) {
            this.pos = i + readU32;
            return;
        }
        throw new IllegalArgumentException("not enough data for block");
    }
}
