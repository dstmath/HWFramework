package java.io;

import java.util.Arrays;
import sun.util.logging.PlatformLogger;

public class ByteArrayOutputStream extends OutputStream {
    protected byte[] buf;
    protected int count;

    public ByteArrayOutputStream() {
        this(32);
    }

    public ByteArrayOutputStream(int size) {
        if (size < 0) {
            throw new IllegalArgumentException("Negative initial size: " + size);
        }
        this.buf = new byte[size];
    }

    private void ensureCapacity(int minCapacity) {
        if (minCapacity - this.buf.length > 0) {
            grow(minCapacity);
        }
    }

    private void grow(int minCapacity) {
        int newCapacity = this.buf.length << 1;
        if (newCapacity - minCapacity < 0) {
            newCapacity = minCapacity;
        }
        if (newCapacity < 0) {
            if (minCapacity < 0) {
                throw new OutOfMemoryError();
            }
            newCapacity = PlatformLogger.OFF;
        }
        this.buf = Arrays.copyOf(this.buf, newCapacity);
    }

    public synchronized void write(int b) {
        ensureCapacity(this.count + 1);
        this.buf[this.count] = (byte) b;
        this.count++;
    }

    public synchronized void write(byte[] b, int off, int len) {
        if (off >= 0) {
            if (off <= b.length && len >= 0) {
                if ((off + len) - b.length <= 0) {
                    ensureCapacity(this.count + len);
                    System.arraycopy(b, off, this.buf, this.count, len);
                    this.count += len;
                }
            }
        }
        throw new IndexOutOfBoundsException();
    }

    public synchronized void writeTo(OutputStream out) throws IOException {
        out.write(this.buf, 0, this.count);
    }

    public synchronized void reset() {
        this.count = 0;
    }

    public synchronized byte[] toByteArray() {
        return Arrays.copyOf(this.buf, this.count);
    }

    public synchronized int size() {
        return this.count;
    }

    public synchronized String toString() {
        return new String(this.buf, 0, this.count);
    }

    public synchronized String toString(String charsetName) throws UnsupportedEncodingException {
        return new String(this.buf, 0, this.count, charsetName);
    }

    @Deprecated
    public synchronized String toString(int hibyte) {
        return new String(this.buf, hibyte, 0, this.count);
    }

    public void close() throws IOException {
    }
}
