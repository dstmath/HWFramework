package java.io;

public class CharArrayReader extends Reader {
    protected char[] buf;
    protected int count;
    protected int markedPos = 0;
    protected int pos;

    public CharArrayReader(char[] buf) {
        this.buf = buf;
        this.pos = 0;
        this.count = buf.length;
    }

    public CharArrayReader(char[] buf, int offset, int length) {
        if (offset < 0 || offset > buf.length || length < 0 || offset + length < 0) {
            throw new IllegalArgumentException();
        }
        this.buf = buf;
        this.pos = offset;
        this.count = Math.min(offset + length, buf.length);
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
            if (off >= 0 && off <= b.length && len >= 0) {
                if (off + len <= b.length && off + len >= 0) {
                    if (len == 0) {
                        return 0;
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
            }
            throw new IndexOutOfBoundsException();
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
        boolean z = false;
        synchronized (this.lock) {
            ensureOpen();
            if (this.count - this.pos > 0) {
                z = true;
            }
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
