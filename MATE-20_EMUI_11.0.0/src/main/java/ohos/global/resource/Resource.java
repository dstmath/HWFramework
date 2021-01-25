package ohos.global.resource;

import java.io.IOException;
import java.io.InputStream;

public abstract class Resource extends InputStream {
    @Override // java.io.InputStream
    public abstract int available() throws IOException;

    @Override // java.io.InputStream, java.io.Closeable, java.lang.AutoCloseable
    public abstract void close() throws IOException;

    @Override // java.io.InputStream
    public abstract int read(byte[] bArr, int i, int i2) throws IOException, NullPointerException, IndexOutOfBoundsException;

    @Override // java.io.InputStream
    public int read() throws IOException {
        byte[] bArr = new byte[1];
        if (read(bArr, 0, 1) == -1) {
            return -1;
        }
        return bArr[0];
    }
}
