package org.bouncycastle.crypto.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import org.bouncycastle.pqc.crypto.rainbow.util.GF2Field;
import org.bouncycastle.util.Strings;

/* access modifiers changed from: package-private */
public class SSHBuilder {
    private final ByteArrayOutputStream bos = new ByteArrayOutputStream();

    SSHBuilder() {
    }

    public byte[] getBytes() {
        return this.bos.toByteArray();
    }

    public byte[] getPaddedBytes() {
        return getPaddedBytes(8);
    }

    public byte[] getPaddedBytes(int i) {
        int size = this.bos.size() % i;
        if (size != 0) {
            int i2 = i - size;
            for (int i3 = 1; i3 <= i2; i3++) {
                this.bos.write(i3);
            }
        }
        return this.bos.toByteArray();
    }

    public void u32(int i) {
        this.bos.write((i >>> 24) & GF2Field.MASK);
        this.bos.write((i >>> 16) & GF2Field.MASK);
        this.bos.write((i >>> 8) & GF2Field.MASK);
        this.bos.write(i & GF2Field.MASK);
    }

    public void writeBigNum(BigInteger bigInteger) {
        writeBlock(bigInteger.toByteArray());
    }

    public void writeBlock(byte[] bArr) {
        u32(bArr.length);
        try {
            this.bos.write(bArr);
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public void writeBytes(byte[] bArr) {
        try {
            this.bos.write(bArr);
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public void writeString(String str) {
        writeBlock(Strings.toByteArray(str));
    }
}
