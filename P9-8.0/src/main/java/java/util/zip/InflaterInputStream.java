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

    public InflaterInputStream(InputStream in, Inflater inf, int size) {
        super(in);
        this.closed = false;
        this.reachEOF = false;
        this.singleByteBuf = new byte[1];
        this.b = new byte[512];
        if (in == null || inf == null) {
            throw new NullPointerException();
        } else if (size <= 0) {
            throw new IllegalArgumentException("buffer size <= 0");
        } else {
            this.inf = inf;
            this.buf = new byte[size];
        }
    }

    public InflaterInputStream(InputStream in, Inflater inf) {
        this(in, inf, 512);
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

    public int read(byte[] b, int off, int len) throws IOException {
        ensureOpen();
        if (b == null) {
            throw new NullPointerException();
        } else if (off < 0 || len < 0 || len > b.length - off) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return 0;
        } else {
            while (true) {
                int n = this.inf.inflate(b, off, len);
                if (n != 0) {
                    if (this.inf.finished()) {
                        this.reachEOF = true;
                    }
                    return n;
                } else if (this.inf.finished() || this.inf.needsDictionary()) {
                    this.reachEOF = true;
                } else {
                    try {
                        if (this.inf.needsInput()) {
                            fill();
                        }
                    } catch (DataFormatException e) {
                        String s = e.getMessage();
                        if (s == null) {
                            s = "Invalid ZLIB data format";
                        }
                        throw new ZipException(s);
                    }
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
        if (n < 0) {
            throw new IllegalArgumentException("negative skip length");
        }
        ensureOpen();
        int max = (int) Math.min(n, 2147483647L);
        int total = 0;
        while (total < max) {
            int len = max - total;
            if (len > this.b.length) {
                len = this.b.length;
            }
            len = read(this.b, 0, len);
            if (len == -1) {
                this.reachEOF = true;
                break;
            }
            total += len;
        }
        return (long) total;
    }

    public void close() throws IOException {
        if (!this.closed) {
            this.inf.end();
            this.in.close();
            this.closed = true;
        }
    }

    protected void fill() throws IOException {
        ensureOpen();
        this.len = this.in.read(this.buf, 0, this.buf.length);
        if (this.len == -1) {
            throw new EOFException("Unexpected end of ZLIB input stream");
        }
        this.inf.setInput(this.buf, 0, this.len);
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
