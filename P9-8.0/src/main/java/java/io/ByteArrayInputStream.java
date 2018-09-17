package java.io;

public class ByteArrayInputStream extends InputStream {
    protected byte[] buf;
    protected int count;
    protected int mark = 0;
    protected int pos;

    public ByteArrayInputStream(byte[] buf) {
        this.buf = buf;
        this.pos = 0;
        this.count = buf.length;
    }

    public ByteArrayInputStream(byte[] buf, int offset, int length) {
        this.buf = buf;
        this.pos = offset;
        this.count = Math.min(offset + length, buf.length);
        this.mark = offset;
    }

    public synchronized int read() {
        int i;
        if (this.pos < this.count) {
            byte[] bArr = this.buf;
            int i2 = this.pos;
            this.pos = i2 + 1;
            i = bArr[i2] & 255;
        } else {
            i = -1;
        }
        return i;
    }

    public synchronized int read(byte[] b, int off, int len) {
        if (b == null) {
            throw new NullPointerException();
        } else if (off < 0 || len < 0 || len > b.length - off) {
            throw new IndexOutOfBoundsException();
        } else if (this.pos >= this.count) {
            return -1;
        } else {
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

    public synchronized long skip(long n) {
        long k;
        k = (long) (this.count - this.pos);
        if (n < k) {
            k = n < 0 ? 0 : n;
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
