package java.io;

public class ByteArrayInputStream extends InputStream {
    protected byte[] buf;
    protected int count;
    protected int mark = 0;
    protected int pos;

    public ByteArrayInputStream(byte[] buf2) {
        this.buf = buf2;
        this.pos = 0;
        this.count = buf2.length;
    }

    public ByteArrayInputStream(byte[] buf2, int offset, int length) {
        this.buf = buf2;
        this.pos = offset;
        this.count = Math.min(offset + length, buf2.length);
        this.mark = offset;
    }

    public synchronized int read() {
        byte b;
        if (this.pos < this.count) {
            byte[] bArr = this.buf;
            int i = this.pos;
            this.pos = i + 1;
            b = bArr[i] & Character.DIRECTIONALITY_UNDEFINED;
        } else {
            b = -1;
        }
        return b;
    }

    public synchronized int read(byte[] b, int off, int len) {
        if (b != null) {
            if (off >= 0 && len >= 0) {
                if (len <= b.length - off) {
                    if (this.pos >= this.count) {
                        return -1;
                    }
                    int avail = this.count - this.pos;
                    if (len > avail) {
                        len = avail;
                    }
                    if (len <= 0) {
                        return 0;
                    }
                    System.arraycopy(this.buf, this.pos, b, off, len);
                    this.pos += len;
                    return len;
                }
            }
            throw new IndexOutOfBoundsException();
        }
        throw new NullPointerException();
    }

    public synchronized long skip(long n) {
        long k;
        k = (long) (this.count - this.pos);
        if (n < k) {
            long j = 0;
            if (n >= 0) {
                j = n;
            }
            k = j;
        }
        this.pos = (int) (((long) this.pos) + k);
        return k;
    }

    public synchronized int available() {
        return this.count - this.pos;
    }

    public boolean markSupported() {
        return true;
    }

    public void mark(int readAheadLimit) {
        this.mark = this.pos;
    }

    public synchronized void reset() {
        this.pos = this.mark;
    }

    public void close() throws IOException {
    }
}
