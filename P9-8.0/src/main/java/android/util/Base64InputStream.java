package android.util;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class Base64InputStream extends FilterInputStream {
    private static final int BUFFER_SIZE = 2048;
    private static byte[] EMPTY = new byte[0];
    private final Coder coder;
    private boolean eof;
    private byte[] inputBuffer;
    private int outputEnd;
    private int outputStart;

    public Base64InputStream(InputStream in, int flags) {
        this(in, flags, false);
    }

    public Base64InputStream(InputStream in, int flags, boolean encode) {
        super(in);
        this.eof = false;
        this.inputBuffer = new byte[2048];
        if (encode) {
            this.coder = new Encoder(flags, null);
        } else {
            this.coder = new Decoder(flags, null);
        }
        this.coder.output = new byte[this.coder.maxOutputSize(2048)];
        this.outputStart = 0;
        this.outputEnd = 0;
    }

    public boolean markSupported() {
        return false;
    }

    public void mark(int readlimit) {
        throw new UnsupportedOperationException();
    }

    public void reset() {
        throw new UnsupportedOperationException();
    }

    public void close() throws IOException {
        this.in.close();
        this.inputBuffer = null;
    }

    public int available() {
        return this.outputEnd - this.outputStart;
    }

    public long skip(long n) throws IOException {
        if (this.outputStart >= this.outputEnd) {
            refill();
        }
        if (this.outputStart >= this.outputEnd) {
            return 0;
        }
        long bytes = Math.min(n, (long) (this.outputEnd - this.outputStart));
        this.outputStart = (int) (((long) this.outputStart) + bytes);
        return bytes;
    }

    public int read() throws IOException {
        if (this.outputStart >= this.outputEnd) {
            refill();
        }
        if (this.outputStart >= this.outputEnd) {
            return -1;
        }
        byte[] bArr = this.coder.output;
        int i = this.outputStart;
        this.outputStart = i + 1;
        return bArr[i] & 255;
    }

    public int read(byte[] b, int off, int len) throws IOException {
        if (this.outputStart >= this.outputEnd) {
            refill();
        }
        if (this.outputStart >= this.outputEnd) {
            return -1;
        }
        int bytes = Math.min(len, this.outputEnd - this.outputStart);
        System.arraycopy(this.coder.output, this.outputStart, b, off, bytes);
        this.outputStart += bytes;
        return bytes;
    }

    private void refill() throws IOException {
        if (!this.eof) {
            boolean success;
            int bytesRead = this.in.read(this.inputBuffer);
            if (bytesRead == -1) {
                this.eof = true;
                success = this.coder.process(EMPTY, 0, 0, true);
            } else {
                success = this.coder.process(this.inputBuffer, 0, bytesRead, false);
            }
            if (success) {
                this.outputEnd = this.coder.op;
                this.outputStart = 0;
                return;
            }
            throw new Base64DataException("bad base-64");
        }
    }
}
