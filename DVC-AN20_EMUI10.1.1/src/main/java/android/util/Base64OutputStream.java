package android.util;

import android.annotation.UnsupportedAppUsage;
import android.util.Base64;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class Base64OutputStream extends FilterOutputStream {
    private static byte[] EMPTY = new byte[0];
    private int bpos;
    private byte[] buffer;
    private final Base64.Coder coder;
    private final int flags;

    public Base64OutputStream(OutputStream out, int flags2) {
        this(out, flags2, true);
    }

    @UnsupportedAppUsage
    public Base64OutputStream(OutputStream out, int flags2, boolean encode) {
        super(out);
        this.buffer = null;
        this.bpos = 0;
        this.flags = flags2;
        if (encode) {
            this.coder = new Base64.Encoder(flags2, null);
        } else {
            this.coder = new Base64.Decoder(flags2, null);
        }
    }

    @Override // java.io.OutputStream, java.io.FilterOutputStream
    public void write(int b) throws IOException {
        if (this.buffer == null) {
            this.buffer = new byte[1024];
        }
        int i = this.bpos;
        byte[] bArr = this.buffer;
        if (i >= bArr.length) {
            internalWrite(bArr, 0, i, false);
            this.bpos = 0;
        }
        byte[] bArr2 = this.buffer;
        int i2 = this.bpos;
        this.bpos = i2 + 1;
        bArr2[i2] = (byte) b;
    }

    private void flushBuffer() throws IOException {
        int i = this.bpos;
        if (i > 0) {
            internalWrite(this.buffer, 0, i, false);
            this.bpos = 0;
        }
    }

    @Override // java.io.OutputStream, java.io.FilterOutputStream
    public void write(byte[] b, int off, int len) throws IOException {
        if (len > 0) {
            flushBuffer();
            internalWrite(b, off, len, false);
        }
    }

    @Override // java.io.OutputStream, java.io.Closeable, java.io.FilterOutputStream, java.lang.AutoCloseable
    public void close() throws IOException {
        IOException thrown = null;
        try {
            flushBuffer();
            internalWrite(EMPTY, 0, 0, true);
        } catch (IOException e) {
            thrown = e;
        }
        try {
            if ((this.flags & 16) == 0) {
                this.out.close();
            } else {
                this.out.flush();
            }
        } catch (IOException e2) {
            if (thrown == null) {
                thrown = e2;
            } else {
                thrown.addSuppressed(e2);
            }
        }
        if (thrown != null) {
            throw thrown;
        }
    }

    private void internalWrite(byte[] b, int off, int len, boolean finish) throws IOException {
        Base64.Coder coder2 = this.coder;
        coder2.output = embiggen(coder2.output, this.coder.maxOutputSize(len));
        if (this.coder.process(b, off, len, finish)) {
            this.out.write(this.coder.output, 0, this.coder.op);
            return;
        }
        throw new Base64DataException("bad base-64");
    }

    private byte[] embiggen(byte[] b, int len) {
        if (b == null || b.length < len) {
            return new byte[len];
        }
        return b;
    }
}
