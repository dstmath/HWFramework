package java.util.zip;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class DeflaterInputStream extends FilterInputStream {
    protected final byte[] buf;
    protected final Deflater def;
    private byte[] rbuf;
    private boolean reachEOF;
    private boolean usesDefaultDeflater;

    private void ensureOpen() throws IOException {
        if (this.in == null) {
            throw new IOException("Stream closed");
        }
    }

    public DeflaterInputStream(InputStream in) {
        this(in, new Deflater());
        this.usesDefaultDeflater = true;
    }

    public DeflaterInputStream(InputStream in, Deflater defl) {
        this(in, defl, 512);
    }

    public DeflaterInputStream(InputStream in, Deflater defl, int bufLen) {
        super(in);
        this.rbuf = new byte[1];
        this.usesDefaultDeflater = false;
        this.reachEOF = false;
        if (in == null) {
            throw new NullPointerException("Null input");
        } else if (defl == null) {
            throw new NullPointerException("Null deflater");
        } else if (bufLen < 1) {
            throw new IllegalArgumentException("Buffer size < 1");
        } else {
            this.def = defl;
            this.buf = new byte[bufLen];
        }
    }

    public void close() throws IOException {
        if (this.in != null) {
            try {
                if (this.usesDefaultDeflater) {
                    this.def.end();
                }
                this.in.close();
            } finally {
                this.in = null;
            }
        }
    }

    public int read() throws IOException {
        if (read(this.rbuf, 0, 1) <= 0) {
            return -1;
        }
        return this.rbuf[0] & 255;
    }

    public int read(byte[] b, int off, int len) throws IOException {
        ensureOpen();
        if (b == null) {
            throw new NullPointerException("Null buffer for read");
        } else if (off < 0 || len < 0 || len > b.length - off) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return 0;
        } else {
            int cnt = 0;
            while (len > 0 && (this.def.finished() ^ 1) != 0) {
                int n;
                if (this.def.needsInput()) {
                    n = this.in.read(this.buf, 0, this.buf.length);
                    if (n < 0) {
                        this.def.finish();
                    } else if (n > 0) {
                        this.def.setInput(this.buf, 0, n);
                    }
                }
                n = this.def.deflate(b, off, len);
                cnt += n;
                off += n;
                len -= n;
            }
            if (this.def.finished()) {
                this.reachEOF = true;
                if (cnt == 0) {
                    cnt = -1;
                }
            }
            return cnt;
        }
    }

    public long skip(long n) throws IOException {
        if (n < 0) {
            throw new IllegalArgumentException("negative skip length");
        }
        ensureOpen();
        if (this.rbuf.length < 512) {
            this.rbuf = new byte[512];
        }
        int total = (int) Math.min(n, 2147483647L);
        long cnt = 0;
        while (total > 0) {
            int len = read(this.rbuf, 0, total <= this.rbuf.length ? total : this.rbuf.length);
            if (len < 0) {
                break;
            }
            cnt += (long) len;
            total -= len;
        }
        return cnt;
    }

    public int available() throws IOException {
        ensureOpen();
        if (this.reachEOF) {
            return 0;
        }
        return 1;
    }

    public boolean markSupported() {
        return false;
    }

    public void mark(int limit) {
    }

    public void reset() throws IOException {
        throw new IOException("mark/reset not supported");
    }
}
