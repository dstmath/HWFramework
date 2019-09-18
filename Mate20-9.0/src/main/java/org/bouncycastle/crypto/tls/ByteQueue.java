package org.bouncycastle.crypto.tls;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ByteQueue {
    private static final int DEFAULT_CAPACITY = 1024;
    private int available;
    private byte[] databuf;
    private boolean readOnlyBuf;
    private int skipped;

    public ByteQueue() {
        this(1024);
    }

    public ByteQueue(int i) {
        this.skipped = 0;
        this.available = 0;
        this.readOnlyBuf = false;
        this.databuf = i == 0 ? TlsUtils.EMPTY_BYTES : new byte[i];
    }

    public ByteQueue(byte[] bArr, int i, int i2) {
        this.skipped = 0;
        this.available = 0;
        this.readOnlyBuf = false;
        this.databuf = bArr;
        this.skipped = i;
        this.available = i2;
        this.readOnlyBuf = true;
    }

    public static int nextTwoPow(int i) {
        int i2 = i | (i >> 1);
        int i3 = i2 | (i2 >> 2);
        int i4 = i3 | (i3 >> 4);
        int i5 = i4 | (i4 >> 8);
        return (i5 | (i5 >> 16)) + 1;
    }

    public void addData(byte[] bArr, int i, int i2) {
        if (!this.readOnlyBuf) {
            if (this.skipped + this.available + i2 > this.databuf.length) {
                int nextTwoPow = nextTwoPow(this.available + i2);
                if (nextTwoPow > this.databuf.length) {
                    byte[] bArr2 = new byte[nextTwoPow];
                    System.arraycopy(this.databuf, this.skipped, bArr2, 0, this.available);
                    this.databuf = bArr2;
                } else {
                    System.arraycopy(this.databuf, this.skipped, this.databuf, 0, this.available);
                }
                this.skipped = 0;
            }
            System.arraycopy(bArr, i, this.databuf, this.skipped + this.available, i2);
            this.available += i2;
            return;
        }
        throw new IllegalStateException("Cannot add data to read-only buffer");
    }

    public int available() {
        return this.available;
    }

    public void copyTo(OutputStream outputStream, int i) throws IOException {
        if (i <= this.available) {
            outputStream.write(this.databuf, this.skipped, i);
            return;
        }
        throw new IllegalStateException("Cannot copy " + i + " bytes, only got " + this.available);
    }

    public void read(byte[] bArr, int i, int i2, int i3) {
        if (bArr.length - i < i2) {
            throw new IllegalArgumentException("Buffer size of " + bArr.length + " is too small for a read of " + i2 + " bytes");
        } else if (this.available - i3 >= i2) {
            System.arraycopy(this.databuf, this.skipped + i3, bArr, i, i2);
        } else {
            throw new IllegalStateException("Not enough data to read");
        }
    }

    public ByteArrayInputStream readFrom(int i) {
        if (i <= this.available) {
            int i2 = this.skipped;
            this.available -= i;
            this.skipped += i;
            return new ByteArrayInputStream(this.databuf, i2, i);
        }
        throw new IllegalStateException("Cannot read " + i + " bytes, only got " + this.available);
    }

    public void removeData(int i) {
        if (i <= this.available) {
            this.available -= i;
            this.skipped += i;
            return;
        }
        throw new IllegalStateException("Cannot remove " + i + " bytes, only got " + this.available);
    }

    public void removeData(byte[] bArr, int i, int i2, int i3) {
        read(bArr, i, i2, i3);
        removeData(i3 + i2);
    }

    public byte[] removeData(int i, int i2) {
        byte[] bArr = new byte[i];
        removeData(bArr, 0, i, i2);
        return bArr;
    }

    public void shrink() {
        byte[] bArr;
        if (this.available == 0) {
            bArr = TlsUtils.EMPTY_BYTES;
        } else {
            int nextTwoPow = nextTwoPow(this.available);
            if (nextTwoPow < this.databuf.length) {
                bArr = new byte[nextTwoPow];
                System.arraycopy(this.databuf, this.skipped, bArr, 0, this.available);
            } else {
                return;
            }
        }
        this.databuf = bArr;
        this.skipped = 0;
    }
}
