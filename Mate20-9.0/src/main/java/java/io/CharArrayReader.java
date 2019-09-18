package java.io;

public class CharArrayReader extends Reader {
    protected char[] buf;
    protected int count;
    protected int markedPos = 0;
    protected int pos;

    public CharArrayReader(char[] buf2) {
        this.buf = buf2;
        this.pos = 0;
        this.count = buf2.length;
    }

    public CharArrayReader(char[] buf2, int offset, int length) {
        if (offset < 0 || offset > buf2.length || length < 0 || offset + length < 0) {
            throw new IllegalArgumentException();
        }
        this.buf = buf2;
        this.pos = offset;
        this.count = Math.min(offset + length, buf2.length);
        this.markedPos = offset;
    }

    private void ensureOpen() throws IOException {
        if (this.buf == null) {
            throw new IOException("Stream closed");
        }
    }

    public int read() throws IOException {
        synchronized (this.lock) {
            ensureOpen();
            if (this.pos >= this.count) {
                return -1;
            }
            char[] cArr = this.buf;
            int i = this.pos;
            this.pos = i + 1;
            char c = cArr[i];
            return c;
        }
    }

    public int read(char[] b, int off, int len) throws IOException {
        synchronized (this.lock) {
            ensureOpen();
            if (off < 0 || off > b.length || len < 0 || off + len > b.length || off + len < 0) {
                throw new IndexOutOfBoundsException();
            } else if (len == 0) {
                return 0;
            } else {
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
                System.arraycopy((Object) this.buf, this.pos, (Object) b, off, len);
                this.pos += len;
                return len;
            }
        }
    }

    public long skip(long n) throws IOException {
        synchronized (this.lock) {
            ensureOpen();
            long avail = (long) (this.count - this.pos);
            if (n > avail) {
                n = avail;
            }
            if (n < 0) {
                return 0;
            }
            this.pos = (int) (((long) this.pos) + n);
            return n;
        }
    }

    public boolean ready() throws IOException {
        boolean z;
        synchronized (this.lock) {
            ensureOpen();
            z = this.count - this.pos > 0;
        }
        return z;
    }

    public boolean markSupported() {
        return true;
    }

    public void mark(int readAheadLimit) throws IOException {
        synchronized (this.lock) {
            ensureOpen();
            this.markedPos = this.pos;
        }
    }

    public void reset() throws IOException {
        synchronized (this.lock) {
            ensureOpen();
            this.pos = this.markedPos;
        }
    }

    public void close() {
        this.buf = null;
    }
}
