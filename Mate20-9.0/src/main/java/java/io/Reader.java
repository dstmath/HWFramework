package java.io;

import java.nio.CharBuffer;

public abstract class Reader implements Readable, Closeable {
    private static final int maxSkipBufferSize = 8192;
    protected Object lock;
    private char[] skipBuffer;

    public abstract void close() throws IOException;

    public abstract int read(char[] cArr, int i, int i2) throws IOException;

    protected Reader() {
        this.skipBuffer = null;
        this.lock = this;
    }

    protected Reader(Object lock2) {
        this.skipBuffer = null;
        if (lock2 != null) {
            this.lock = lock2;
            return;
        }
        throw new NullPointerException();
    }

    public int read(CharBuffer target) throws IOException {
        int len = target.remaining();
        char[] cbuf = new char[len];
        int n = read(cbuf, 0, len);
        if (n > 0) {
            target.put(cbuf, 0, n);
        }
        return n;
    }

    public int read() throws IOException {
        char[] cb = new char[1];
        if (read(cb, 0, 1) == -1) {
            return -1;
        }
        return cb[0];
    }

    public int read(char[] cbuf) throws IOException {
        return read(cbuf, 0, cbuf.length);
    }

    public long skip(long n) throws IOException {
        long j;
        if (n >= 0) {
            int nn = (int) Math.min(n, 8192);
            synchronized (this.lock) {
                if (this.skipBuffer == null || this.skipBuffer.length < nn) {
                    this.skipBuffer = new char[nn];
                }
                long r = n;
                while (true) {
                    if (r <= 0) {
                        break;
                    }
                    int nc = read(this.skipBuffer, 0, (int) Math.min(r, (long) nn));
                    if (nc == -1) {
                        break;
                    }
                    r -= (long) nc;
                }
                j = n - r;
            }
            return j;
        }
        throw new IllegalArgumentException("skip value is negative");
    }

    public boolean ready() throws IOException {
        return false;
    }

    public boolean markSupported() {
        return false;
    }

    public void mark(int readAheadLimit) throws IOException {
        throw new IOException("mark() not supported");
    }

    public void reset() throws IOException {
        throw new IOException("reset() not supported");
    }
}
