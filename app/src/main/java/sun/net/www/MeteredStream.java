package sun.net.www;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import sun.net.ProgressSource;
import sun.net.www.http.ChunkedInputStream;

public class MeteredStream extends FilterInputStream {
    protected boolean closed;
    protected long count;
    protected long expected;
    protected int markLimit;
    protected long markedCount;
    protected ProgressSource pi;

    public MeteredStream(InputStream is, ProgressSource pi, long expected) {
        super(is);
        this.closed = false;
        this.count = 0;
        this.markedCount = 0;
        this.markLimit = -1;
        this.pi = pi;
        this.expected = expected;
        if (pi != null) {
            pi.updateProgress(0, expected);
        }
    }

    private final void justRead(long n) throws IOException {
        if (n == -1) {
            if (!isMarked()) {
                close();
            }
            return;
        }
        this.count += n;
        if (this.count - this.markedCount > ((long) this.markLimit)) {
            this.markLimit = -1;
        }
        if (this.pi != null) {
            this.pi.updateProgress(this.count, this.expected);
        }
        if (!isMarked() && this.expected > 0 && this.count >= this.expected) {
            close();
        }
    }

    private boolean isMarked() {
        if (this.markLimit >= 0 && this.count - this.markedCount <= ((long) this.markLimit)) {
            return true;
        }
        return false;
    }

    public synchronized int read() throws IOException {
        if (this.closed) {
            return -1;
        }
        int c = this.in.read();
        if (c != -1) {
            justRead(1);
        } else {
            justRead((long) c);
        }
        return c;
    }

    public synchronized int read(byte[] b, int off, int len) throws IOException {
        if (this.closed) {
            return -1;
        }
        int n = this.in.read(b, off, len);
        justRead((long) n);
        return n;
    }

    public synchronized long skip(long n) throws IOException {
        if (this.closed) {
            return 0;
        }
        if (this.in instanceof ChunkedInputStream) {
            n = this.in.skip(n);
        } else {
            n = this.in.skip(n > this.expected - this.count ? this.expected - this.count : n);
        }
        justRead(n);
        return n;
    }

    public void close() throws IOException {
        if (!this.closed) {
            if (this.pi != null) {
                this.pi.finishTracking();
            }
            this.closed = true;
            this.in.close();
        }
    }

    public synchronized int available() throws IOException {
        return this.closed ? 0 : this.in.available();
    }

    public synchronized void mark(int readLimit) {
        if (!this.closed) {
            super.mark(readLimit);
            this.markedCount = this.count;
            this.markLimit = readLimit;
        }
    }

    public synchronized void reset() throws IOException {
        if (!this.closed) {
            if (isMarked()) {
                this.count = this.markedCount;
                super.reset();
                return;
            }
            throw new IOException("Resetting to an invalid mark");
        }
    }

    public boolean markSupported() {
        if (this.closed) {
            return false;
        }
        return super.markSupported();
    }

    protected void finalize() throws Throwable {
        try {
            close();
            if (this.pi != null) {
                this.pi.close();
            }
            super.finalize();
        } catch (Throwable th) {
            super.finalize();
        }
    }
}
