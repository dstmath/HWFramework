package java.io;

public class PushbackReader extends FilterReader {
    private char[] buf;
    private int pos;

    public PushbackReader(Reader in, int size) {
        super(in);
        if (size <= 0) {
            throw new IllegalArgumentException("size <= 0");
        }
        this.buf = new char[size];
        this.pos = size;
    }

    public PushbackReader(Reader in) {
        this(in, 1);
    }

    private void ensureOpen() throws IOException {
        if (this.buf == null) {
            throw new IOException("Stream closed");
        }
    }

    public int read() throws IOException {
        synchronized (this.lock) {
            ensureOpen();
            if (this.pos < this.buf.length) {
                char[] cArr = this.buf;
                int i = this.pos;
                this.pos = i + 1;
                char c = cArr[i];
                return c;
            }
            int read = super.read();
            return read;
        }
    }

    /* JADX WARNING: Missing block: B:36:0x004f, code:
            return r0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int read(char[] cbuf, int off, int len) throws IOException {
        synchronized (this.lock) {
            ensureOpen();
            if (len > 0) {
                int avail = this.buf.length - this.pos;
                if (avail > 0) {
                    if (len < avail) {
                        avail = len;
                    }
                    System.arraycopy(this.buf, this.pos, cbuf, off, avail);
                    this.pos += avail;
                    off += avail;
                    len -= avail;
                }
                if (len > 0) {
                    len = super.read(cbuf, off, len);
                    if (len != -1) {
                        int i = avail + len;
                        return i;
                    } else if (avail == 0) {
                        avail = -1;
                    }
                } else {
                    return avail;
                }
            } else if (len < 0) {
                try {
                    throw new IndexOutOfBoundsException();
                } catch (ArrayIndexOutOfBoundsException e) {
                    throw new IndexOutOfBoundsException();
                }
            } else {
                if (off >= 0) {
                    if (off <= cbuf.length) {
                        return 0;
                    }
                }
                throw new IndexOutOfBoundsException();
            }
        }
    }

    public void unread(int c) throws IOException {
        synchronized (this.lock) {
            ensureOpen();
            if (this.pos == 0) {
                throw new IOException("Pushback buffer overflow");
            }
            char[] cArr = this.buf;
            int i = this.pos - 1;
            this.pos = i;
            cArr[i] = (char) c;
        }
    }

    public void unread(char[] cbuf, int off, int len) throws IOException {
        synchronized (this.lock) {
            ensureOpen();
            if (len > this.pos) {
                throw new IOException("Pushback buffer overflow");
            }
            this.pos -= len;
            System.arraycopy(cbuf, off, this.buf, this.pos, len);
        }
    }

    public void unread(char[] cbuf) throws IOException {
        unread(cbuf, 0, cbuf.length);
    }

    public boolean ready() throws IOException {
        boolean ready;
        synchronized (this.lock) {
            ensureOpen();
            ready = this.pos >= this.buf.length ? super.ready() : true;
        }
        return ready;
    }

    public void mark(int readAheadLimit) throws IOException {
        throw new IOException("mark/reset not supported");
    }

    public void reset() throws IOException {
        throw new IOException("mark/reset not supported");
    }

    public boolean markSupported() {
        return false;
    }

    public void close() throws IOException {
        super.close();
        this.buf = null;
    }

    public long skip(long n) throws IOException {
        if (n < 0) {
            throw new IllegalArgumentException("skip value is negative");
        }
        synchronized (this.lock) {
            ensureOpen();
            int avail = this.buf.length - this.pos;
            if (avail > 0) {
                if (n <= ((long) avail)) {
                    this.pos = (int) (((long) this.pos) + n);
                    return n;
                }
                this.pos = this.buf.length;
                n -= (long) avail;
            }
            long skip = ((long) avail) + super.skip(n);
            return skip;
        }
    }
}
