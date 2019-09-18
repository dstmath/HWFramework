package org.bouncycastle.crypto.tls;

import java.io.IOException;
import java.io.OutputStream;

public class ByteQueueOutputStream extends OutputStream {
    private ByteQueue buffer = new ByteQueue();

    public ByteQueue getBuffer() {
        return this.buffer;
    }

    public void write(int i) throws IOException {
        this.buffer.addData(new byte[]{(byte) i}, 0, 1);
    }

    public void write(byte[] bArr, int i, int i2) throws IOException {
        this.buffer.addData(bArr, i, i2);
    }
}
