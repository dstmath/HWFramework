package java.io;

public class PushbackInputStream extends FilterInputStream {
    protected byte[] buf;
    protected int pos;

    private void ensureOpen() throws IOException {
        if (this.in == null) {
            throw new IOException("Stream closed");
        }
    }

    public PushbackInputStream(InputStream in, int size) {
        super(in);
        if (size > 0) {
            this.buf = new byte[size];
            this.pos = size;
            return;
        }
        throw new IllegalArgumentException("size <= 0");
    }

    public PushbackInputStream(InputStream in) {
        this(in, 1);
    }

    public int read() throws IOException {
        ensureOpen();
        if (this.pos >= this.buf.length) {
            return super.read();
        }
        byte[] bArr = this.buf;
        int i = this.pos;
        this.pos = i + 1;
        return bArr[i] & Character.DIRECTIONALITY_UNDEFINED;
    }

    public int read(byte[] b, int off, int len) throws IOException {
        ensureOpen();
        if (b == null) {
            throw new NullPointerException();
        } else if (off < 0 || len < 0 || len > b.length - off) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return 0;
        } else {
            int avail = this.buf.length - this.pos;
            if (avail > 0) {
                if (len < avail) {
                    avail = len;
                }
                System.arraycopy(this.buf, this.pos, b, off, avail);
                this.pos += avail;
                off += avail;
                len -= avail;
            }
            if (len <= 0) {
                return avail;
            }
            int len2 = super.read(b, off, len);
            int i = -1;
            if (len2 != -1) {
                return avail + len2;
            }
            if (avail != 0) {
                i = avail;
            }
            return i;
        }
    }

    public void unread(int b) throws IOException {
        ensureOpen();
        if (this.pos != 0) {
            byte[] bArr = this.buf;
            int i = this.pos - 1;
            this.pos = i;
            bArr[i] = (byte) b;
            return;
        }
        throw new IOException("Push back buffer is full");
    }

    public void unread(byte[] b, int off, int len) throws IOException {
        ensureOpen();
        if (len <= this.pos) {
            this.pos -= len;
            System.arraycopy(b, off, this.buf, this.pos, len);
            return;
        }
        throw new IOException("Push back buffer is full");
    }

    public void unread(byte[] b) throws IOException {
        unread(b, 0, b.length);
    }

    public int available() throws IOException {
        ensureOpen();
        int n = this.buf.length - this.pos;
        int avail = super.available();
        if (n > Integer.MAX_VALUE - avail) {
            return Integer.MAX_VALUE;
        }
        return n + avail;
    }

    public long skip(long n) throws IOException {
        ensureOpen();
        if (n <= 0) {
            return 0;
        }
        long pskip = (long) (this.buf.length - this.pos);
        if (pskip > 0) {
            if (n < pskip) {
                pskip = n;
            }
            this.pos = (int) (((long) this.pos) + pskip);
            n -= pskip;
        }
        if (n > 0) {
            pskip += super.skip(n);
        }
        return pskip;
    }

    public boolean markSupported() {
        return false;
    }

    public synchronized void mark(int readlimit) {
    }

    public synchronized void reset() throws IOException {
        throw new IOException("mark/reset not supported");
    }

    public synchronized void close() throws IOException {
        if (this.in != null) {
            this.in.close();
            this.in = null;
            this.buf = null;
        }
    }
}
