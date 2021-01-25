package android.util;

import android.util.Base64;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class Base64InputStream extends FilterInputStream {
    private static final int BUFFER_SIZE = 2048;
    private static byte[] EMPTY = new byte[0];
    private final Base64.Coder coder;
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
            this.coder = new Base64.Encoder(flags, null);
        } else {
            this.coder = new Base64.Decoder(flags, null);
        }
        Base64.Coder coder2 = this.coder;
        coder2.output = new byte[coder2.maxOutputSize(2048)];
        this.outputStart = 0;
        this.outputEnd = 0;
    }

    @Override // java.io.FilterInputStream, java.io.InputStream
    public boolean markSupported() {
        return false;
    }

    @Override // java.io.FilterInputStream, java.io.InputStream
    public void mark(int readlimit) {
        throw new UnsupportedOperationException();
    }

    @Override // java.io.FilterInputStream, java.io.InputStream
    public void reset() {
        throw new UnsupportedOperationException();
    }

    @Override // java.io.FilterInputStream, java.io.Closeable, java.lang.AutoCloseable, java.io.InputStream
    public void close() throws IOException {
        this.in.close();
        this.inputBuffer = null;
    }

    @Override // java.io.FilterInputStream, java.io.InputStream
    public int available() {
        return this.outputEnd - this.outputStart;
    }

    @Override // java.io.FilterInputStream, java.io.InputStream
    public long skip(long n) throws IOException {
        if (this.outputStart >= this.outputEnd) {
            refill();
        }
        int i = this.outputStart;
        int i2 = this.outputEnd;
        if (i >= i2) {
            return 0;
        }
        long bytes = Math.min(n, (long) (i2 - i));
        this.outputStart = (int) (((long) this.outputStart) + bytes);
        return bytes;
    }

    @Override // java.io.FilterInputStream, java.io.InputStream
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

    @Override // java.io.FilterInputStream, java.io.InputStream
    public int read(byte[] b, int off, int len) throws IOException {
        if (this.outputStart >= this.outputEnd) {
            refill();
        }
        int i = this.outputStart;
        int i2 = this.outputEnd;
        if (i >= i2) {
            return -1;
        }
        int bytes = Math.min(len, i2 - i);
        System.arraycopy(this.coder.output, this.outputStart, b, off, bytes);
        this.outputStart += bytes;
        return bytes;
    }

    private void refill() throws IOException {
        boolean success;
        if (!this.eof) {
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
