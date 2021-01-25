package javax.obex;

import java.io.IOException;
import java.io.InputStream;

public final class PrivateInputStream extends InputStream {
    private byte[] mData = new byte[0];
    private int mIndex = 0;
    private boolean mOpen = true;
    private BaseStream mParent;

    public PrivateInputStream(BaseStream p) {
        this.mParent = p;
    }

    @Override // java.io.InputStream
    public synchronized int available() throws IOException {
        ensureOpen();
        return this.mData.length - this.mIndex;
    }

    @Override // java.io.InputStream
    public synchronized int read() throws IOException {
        ensureOpen();
        while (this.mData.length == this.mIndex) {
            if (!this.mParent.continueOperation(true, true)) {
                return -1;
            }
        }
        byte[] bArr = this.mData;
        int i = this.mIndex;
        this.mIndex = i + 1;
        return bArr[i] & 255;
    }

    @Override // java.io.InputStream
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    @Override // java.io.InputStream
    public synchronized int read(byte[] b, int offset, int length) throws IOException {
        if (b != null) {
            if ((offset | length) >= 0) {
                if (length <= b.length - offset) {
                    ensureOpen();
                    int currentDataLength = this.mData.length - this.mIndex;
                    int remainReadLength = length;
                    int offset1 = offset;
                    int result = 0;
                    while (currentDataLength <= remainReadLength) {
                        System.arraycopy(this.mData, this.mIndex, b, offset1, currentDataLength);
                        this.mIndex += currentDataLength;
                        offset1 += currentDataLength;
                        result += currentDataLength;
                        remainReadLength -= currentDataLength;
                        if (!this.mParent.continueOperation(true, true)) {
                            return result == 0 ? -1 : result;
                        }
                        currentDataLength = this.mData.length - this.mIndex;
                    }
                    if (remainReadLength > 0) {
                        System.arraycopy(this.mData, this.mIndex, b, offset1, remainReadLength);
                        this.mIndex += remainReadLength;
                        result += remainReadLength;
                    }
                    return result;
                }
            }
            throw new ArrayIndexOutOfBoundsException("index outof bound");
        }
        throw new IOException("buffer is null");
    }

    public synchronized void writeBytes(byte[] body, int start) {
        byte[] temp = new byte[((body.length - start) + (this.mData.length - this.mIndex))];
        System.arraycopy(this.mData, this.mIndex, temp, 0, this.mData.length - this.mIndex);
        System.arraycopy(body, start, temp, this.mData.length - this.mIndex, body.length - start);
        this.mData = temp;
        this.mIndex = 0;
        notifyAll();
    }

    private void ensureOpen() throws IOException {
        this.mParent.ensureOpen();
        if (!this.mOpen) {
            throw new IOException("Input stream is closed");
        }
    }

    @Override // java.io.InputStream, java.io.Closeable, java.lang.AutoCloseable
    public void close() throws IOException {
        this.mOpen = false;
        this.mParent.streamClosed(true);
    }
}
