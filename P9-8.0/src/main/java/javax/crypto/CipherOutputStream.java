package javax.crypto;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class CipherOutputStream extends FilterOutputStream {
    private Cipher cipher;
    private boolean closed;
    private byte[] ibuffer;
    private byte[] obuffer;
    private OutputStream output;

    public CipherOutputStream(OutputStream os, Cipher c) {
        super(os);
        this.ibuffer = new byte[1];
        this.closed = false;
        this.output = os;
        this.cipher = c;
    }

    protected CipherOutputStream(OutputStream os) {
        super(os);
        this.ibuffer = new byte[1];
        this.closed = false;
        this.output = os;
        this.cipher = new NullCipher();
    }

    public void write(int b) throws IOException {
        this.ibuffer[0] = (byte) b;
        this.obuffer = this.cipher.update(this.ibuffer, 0, 1);
        if (this.obuffer != null) {
            this.output.write(this.obuffer);
            this.obuffer = null;
        }
    }

    public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    public void write(byte[] b, int off, int len) throws IOException {
        this.obuffer = this.cipher.update(b, off, len);
        if (this.obuffer != null) {
            this.output.write(this.obuffer);
            this.obuffer = null;
        }
    }

    public void flush() throws IOException {
        if (this.obuffer != null) {
            this.output.write(this.obuffer);
            this.obuffer = null;
        }
        this.output.flush();
    }

    /* JADX WARNING: Removed duplicated region for block: B:10:0x0019 A:{Splitter: B:4:0x0008, ExcHandler: javax.crypto.IllegalBlockSizeException (r0_0 'e' java.lang.Throwable)} */
    /* JADX WARNING: Missing block: B:10:0x0019, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:11:0x001a, code:
            r3.obuffer = null;
     */
    /* JADX WARNING: Missing block: B:12:0x0022, code:
            throw new java.io.IOException(r0);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void close() throws IOException {
        if (!this.closed) {
            this.closed = true;
            try {
                this.obuffer = this.cipher.doFinal();
                try {
                    flush();
                } catch (IOException e) {
                }
                this.out.close();
            } catch (Throwable e2) {
            }
        }
    }
}
