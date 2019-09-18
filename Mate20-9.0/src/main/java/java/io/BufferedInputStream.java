package java.io;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

public class BufferedInputStream extends FilterInputStream {
    private static final int DEFAULT_BUFFER_SIZE = 8192;
    private static final int MAX_BUFFER_SIZE = 2147483639;
    private static final AtomicReferenceFieldUpdater<BufferedInputStream, byte[]> bufUpdater = AtomicReferenceFieldUpdater.newUpdater(BufferedInputStream.class, byte[].class, "buf");
    protected volatile byte[] buf;
    protected int count;
    protected int marklimit;
    protected int markpos;
    protected int pos;

    private InputStream getInIfOpen() throws IOException {
        InputStream input = this.in;
        if (input != null) {
            return input;
        }
        throw new IOException("Stream closed");
    }

    private byte[] getBufIfOpen() throws IOException {
        byte[] buffer = this.buf;
        if (buffer != null) {
            return buffer;
        }
        throw new IOException("Stream closed");
    }

    public BufferedInputStream(InputStream in) {
        this(in, 8192);
    }

    public BufferedInputStream(InputStream in, int size) {
        super(in);
        this.markpos = -1;
        if (size > 0) {
            this.buf = new byte[size];
            return;
        }
        throw new IllegalArgumentException("Buffer size <= 0");
    }

    private void fill() throws IOException {
        byte[] buffer = getBufIfOpen();
        if (this.markpos < 0) {
            this.pos = 0;
        } else if (this.pos >= buffer.length) {
            if (this.markpos > 0) {
                int sz = this.pos - this.markpos;
                System.arraycopy(buffer, this.markpos, buffer, 0, sz);
                this.pos = sz;
                this.markpos = 0;
            } else if (buffer.length >= this.marklimit) {
                this.markpos = -1;
                this.pos = 0;
            } else {
                int length = buffer.length;
                int i = MAX_BUFFER_SIZE;
                if (length < MAX_BUFFER_SIZE) {
                    if (this.pos <= MAX_BUFFER_SIZE - this.pos) {
                        i = this.pos * 2;
                    }
                    int nsz = i;
                    if (nsz > this.marklimit) {
                        nsz = this.marklimit;
                    }
                    byte[] nbuf = new byte[nsz];
                    System.arraycopy(buffer, 0, nbuf, 0, this.pos);
                    if (bufUpdater.compareAndSet(this, buffer, nbuf)) {
                        buffer = nbuf;
                    } else {
                        throw new IOException("Stream closed");
                    }
                } else {
                    throw new OutOfMemoryError("Required array size too large");
                }
            }
        }
        this.count = this.pos;
        int n = getInIfOpen().read(buffer, this.pos, buffer.length - this.pos);
        if (n > 0) {
            this.count = this.pos + n;
        }
    }

    public synchronized int read() throws IOException {
        if (this.pos >= this.count) {
            fill();
            if (this.pos >= this.count) {
                return -1;
            }
        }
        byte[] bufIfOpen = getBufIfOpen();
        int i = this.pos;
        this.pos = i + 1;
        return bufIfOpen[i] & Character.DIRECTIONALITY_UNDEFINED;
    }

    private int read1(byte[] b, int off, int len) throws IOException {
        int avail = this.count - this.pos;
        if (avail <= 0) {
            if (len >= getBufIfOpen().length && this.markpos < 0) {
                return getInIfOpen().read(b, off, len);
            }
            fill();
            int avail2 = this.count - this.pos;
            if (avail2 <= 0) {
                return -1;
            }
            avail = avail2;
        }
        int cnt = avail < len ? avail : len;
        System.arraycopy(getBufIfOpen(), this.pos, b, off, cnt);
        this.pos += cnt;
        return cnt;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0026, code lost:
        return r0 == 0 ? r1 : r0;
     */
    public synchronized int read(byte[] b, int off, int len) throws IOException {
        getBufIfOpen();
        if ((off | len | (off + len) | (b.length - (off + len))) >= 0) {
            int n = 0;
            if (len == 0) {
                return 0;
            }
            while (true) {
                int nread = read1(b, off + n, len - n);
                if (nread > 0) {
                    n += nread;
                    if (n >= len) {
                        return n;
                    }
                    InputStream input = this.in;
                    if (input != null && input.available() <= 0) {
                        return n;
                    }
                }
            }
        } else {
            throw new IndexOutOfBoundsException();
        }
    }

    public synchronized long skip(long n) throws IOException {
        getBufIfOpen();
        if (n <= 0) {
            return 0;
        }
        long avail = (long) (this.count - this.pos);
        if (avail <= 0) {
            if (this.markpos < 0) {
                return getInIfOpen().skip(n);
            }
            fill();
            avail = (long) (this.count - this.pos);
            if (avail <= 0) {
                return 0;
            }
        }
        long skipped = avail < n ? avail : n;
        this.pos = (int) (((long) this.pos) + skipped);
        return skipped;
    }

    public synchronized int available() throws IOException {
        int i;
        int n = this.count - this.pos;
        int avail = getInIfOpen().available();
        i = Integer.MAX_VALUE;
        if (n <= Integer.MAX_VALUE - avail) {
            i = n + avail;
        }
        return i;
    }

    public synchronized void mark(int readlimit) {
        this.marklimit = readlimit;
        this.markpos = this.pos;
    }

    public synchronized void reset() throws IOException {
        getBufIfOpen();
        if (this.markpos >= 0) {
            this.pos = this.markpos;
        } else {
            throw new IOException("Resetting to invalid mark");
        }
    }

    public boolean markSupported() {
        return true;
    }

    public void close() throws IOException {
        byte[] buffer;
        do {
            byte[] bArr = this.buf;
            buffer = bArr;
            if (bArr == null) {
                return;
            }
        } while (!bufUpdater.compareAndSet(this, buffer, null));
        InputStream input = this.in;
        this.in = null;
        if (input != null) {
            input.close();
        }
    }
}
