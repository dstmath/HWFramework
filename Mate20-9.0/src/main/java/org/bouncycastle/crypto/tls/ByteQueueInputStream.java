package org.bouncycastle.crypto.tls;

import java.io.InputStream;

public class ByteQueueInputStream extends InputStream {
    private ByteQueue buffer = new ByteQueue();

    public void addBytes(byte[] bArr) {
        this.buffer.addData(bArr, 0, bArr.length);
    }

    public int available() {
        return this.buffer.available();
    }

    public void close() {
    }

    public int peek(byte[] bArr) {
        int min = Math.min(this.buffer.available(), bArr.length);
        this.buffer.read(bArr, 0, min, 0);
        return min;
    }

    public int read() {
        if (this.buffer.available() == 0) {
            return -1;
        }
        return this.buffer.removeData(1, 0)[0] & 255;
    }

    public int read(byte[] bArr) {
        return read(bArr, 0, bArr.length);
    }

    public int read(byte[] bArr, int i, int i2) {
        int min = Math.min(this.buffer.available(), i2);
        this.buffer.removeData(bArr, i, min, 0);
        return min;
    }

    public long skip(long j) {
        int min = Math.min((int) j, this.buffer.available());
        this.buffer.removeData(min);
        return (long) min;
    }
}
