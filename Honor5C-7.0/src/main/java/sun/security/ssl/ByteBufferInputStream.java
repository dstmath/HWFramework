package sun.security.ssl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

class ByteBufferInputStream extends InputStream {
    ByteBuffer bb;

    ByteBufferInputStream(ByteBuffer bb) {
        this.bb = bb;
    }

    public int read() throws IOException {
        if (this.bb == null) {
            throw new IOException("read on a closed InputStream");
        } else if (this.bb.remaining() == 0) {
            return -1;
        } else {
            return this.bb.get();
        }
    }

    public int read(byte[] b) throws IOException {
        if (this.bb != null) {
            return read(b, 0, b.length);
        }
        throw new IOException("read on a closed InputStream");
    }

    public int read(byte[] b, int off, int len) throws IOException {
        if (this.bb == null) {
            throw new IOException("read on a closed InputStream");
        } else if (b == null) {
            throw new NullPointerException();
        } else if (off < 0 || len < 0 || len > b.length - off) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return 0;
        } else {
            int length = Math.min(this.bb.remaining(), len);
            if (length == 0) {
                return -1;
            }
            this.bb.get(b, off, length);
            return length;
        }
    }

    public long skip(long n) throws IOException {
        if (this.bb == null) {
            throw new IOException("skip on a closed InputStream");
        } else if (n <= 0) {
            return 0;
        } else {
            int nInt = (int) n;
            this.bb.position(this.bb.position() + Math.min(this.bb.remaining(), nInt));
            return (long) nInt;
        }
    }

    public int available() throws IOException {
        if (this.bb != null) {
            return this.bb.remaining();
        }
        throw new IOException("available on a closed InputStream");
    }

    public void close() throws IOException {
        this.bb = null;
    }

    public synchronized void mark(int readlimit) {
    }

    public synchronized void reset() throws IOException {
        throw new IOException("mark/reset not supported");
    }

    public boolean markSupported() {
        return false;
    }
}
