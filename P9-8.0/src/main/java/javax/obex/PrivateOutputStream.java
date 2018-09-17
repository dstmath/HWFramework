package javax.obex;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public final class PrivateOutputStream extends OutputStream {
    private ByteArrayOutputStream mArray = new ByteArrayOutputStream();
    private int mMaxPacketSize;
    private boolean mOpen;
    private BaseStream mParent;

    public PrivateOutputStream(BaseStream p, int maxSize) {
        this.mParent = p;
        this.mMaxPacketSize = maxSize;
        this.mOpen = true;
    }

    public int size() {
        return this.mArray.size();
    }

    public synchronized void write(int b) throws IOException {
        ensureOpen();
        this.mParent.ensureNotDone();
        this.mArray.write(b);
        if (this.mArray.size() == this.mMaxPacketSize) {
            this.mParent.continueOperation(true, false);
        }
    }

    public void write(byte[] buffer) throws IOException {
        write(buffer, 0, buffer.length);
    }

    public synchronized void write(byte[] buffer, int offset, int count) throws IOException {
        int offset1 = offset;
        int remainLength = count;
        if (buffer == null) {
            throw new IOException("buffer is null");
        }
        if ((offset | count) >= 0) {
            if (count <= buffer.length - offset) {
                ensureOpen();
                this.mParent.ensureNotDone();
                while (this.mArray.size() + remainLength >= this.mMaxPacketSize) {
                    int bufferLeft = this.mMaxPacketSize - this.mArray.size();
                    this.mArray.write(buffer, offset1, bufferLeft);
                    offset1 += bufferLeft;
                    remainLength -= bufferLeft;
                    this.mParent.continueOperation(true, false);
                }
                if (remainLength > 0) {
                    this.mArray.write(buffer, offset1, remainLength);
                }
            }
        }
        throw new IndexOutOfBoundsException("index outof bound");
    }

    /* JADX WARNING: Missing block: B:8:0x0026, code:
            return r0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized byte[] readBytes(int size) {
        if (this.mArray.size() <= 0) {
            return null;
        }
        byte[] temp = this.mArray.toByteArray();
        this.mArray.reset();
        byte[] result = new byte[size];
        System.arraycopy(temp, 0, result, 0, size);
        if (temp.length != size) {
            this.mArray.write(temp, size, temp.length - size);
        }
    }

    private void ensureOpen() throws IOException {
        this.mParent.ensureOpen();
        if (!this.mOpen) {
            throw new IOException("Output stream is closed");
        }
    }

    public void close() throws IOException {
        this.mOpen = false;
        this.mParent.streamClosed(false);
    }

    public boolean isClosed() {
        return this.mOpen ^ 1;
    }
}
