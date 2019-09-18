package javax.crypto;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class CipherOutputStream extends FilterOutputStream {
    private Cipher cipher;
    private boolean closed = false;
    private byte[] ibuffer = new byte[1];
    private byte[] obuffer;
    private OutputStream output;

    public CipherOutputStream(OutputStream os, Cipher c) {
        super(os);
        this.output = os;
        this.cipher = c;
    }

    protected CipherOutputStream(OutputStream os) {
        super(os);
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
            } catch (BadPaddingException | IllegalBlockSizeException e2) {
                this.obuffer = null;
                throw new IOException((Throwable) e2);
            }
        }
    }
}
