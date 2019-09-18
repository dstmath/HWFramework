package java.util.zip;

import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class InflaterInputStream extends FilterInputStream {
    private byte[] b;
    protected byte[] buf;
    protected boolean closed;
    protected Inflater inf;
    protected int len;
    private boolean reachEOF;
    private byte[] singleByteBuf;

    private void ensureOpen() throws IOException {
        if (this.closed) {
            throw new IOException("Stream closed");
        }
    }

    public InflaterInputStream(InputStream in, Inflater inf2, int size) {
        super(in);
        this.closed = false;
        this.reachEOF = false;
        this.singleByteBuf = new byte[1];
        this.b = new byte[512];
        if (in == null || inf2 == null) {
            throw new NullPointerException();
        } else if (size > 0) {
            this.inf = inf2;
            this.buf = new byte[size];
        } else {
            throw new IllegalArgumentException("buffer size <= 0");
        }
    }

    public InflaterInputStream(InputStream in, Inflater inf2) {
        this(in, inf2, 512);
    }

    public InflaterInputStream(InputStream in) {
        this(in, new Inflater());
    }

    public int read() throws IOException {
        ensureOpen();
        if (read(this.singleByteBuf, 0, 1) == -1) {
            return -1;
        }
        return Byte.toUnsignedInt(this.singleByteBuf[0]);
    }

    public int read(byte[] b2, int off, int len2) throws IOException {
        ensureOpen();
        if (b2 == null) {
            throw new NullPointerException();
        } else if (off < 0 || len2 < 0 || len2 > b2.length - off) {
            throw new IndexOutOfBoundsException();
        } else if (len2 == 0) {
            return 0;
        } else {
            while (true) {
                try {
                    int inflate = this.inf.inflate(b2, off, len2);
                    int n = inflate;
                    if (inflate != 0) {
                        if (this.inf.finished()) {
                            this.reachEOF = true;
                        }
                        return n;
                    } else if (this.inf.finished()) {
                        break;
                    } else if (this.inf.needsDictionary()) {
                        break;
                    } else if (this.inf.needsInput()) {
                        fill();
                    }
                } catch (DataFormatException e) {
                    String s = e.getMessage();
                    throw new ZipException(s != null ? s : "Invalid ZLIB data format");
                }
            }
            this.reachEOF = true;
            return -1;
        }
    }

    public int available() throws IOException {
        ensureOpen();
        if (this.reachEOF) {
            return 0;
        }
        return 1;
    }

    public long skip(long n) throws IOException {
        if (n >= 0) {
            ensureOpen();
            int max = (int) Math.min(n, 2147483647L);
            int total = 0;
            while (true) {
                if (total >= max) {
                    break;
                }
                int len2 = max - total;
                if (len2 > this.b.length) {
                    len2 = this.b.length;
                }
                int len3 = read(this.b, 0, len2);
                if (len3 == -1) {
                    this.reachEOF = true;
                    break;
                }
                total += len3;
            }
            return (long) total;
        }
        throw new IllegalArgumentException("negative skip length");
    }

    public void close() throws IOException {
        if (!this.closed) {
            this.inf.end();
            this.in.close();
            this.closed = true;
        }
    }

    /* access modifiers changed from: protected */
    public void fill() throws IOException {
        ensureOpen();
        this.len = this.in.read(this.buf, 0, this.buf.length);
        if (this.len != -1) {
            this.inf.setInput(this.buf, 0, this.len);
            return;
        }
        throw new EOFException("Unexpected end of ZLIB input stream");
    }

    public boolean markSupported() {
        return false;
    }

    public synchronized void mark(int readlimit) {
    }

    public synchronized void reset() throws IOException {
        throw new IOException("mark/reset not supported");
    }
}
