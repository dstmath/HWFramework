package javax.crypto;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class CipherInputStream extends FilterInputStream {
    private Cipher cipher;
    private boolean closed = false;
    private boolean done = false;
    private byte[] ibuffer = new byte[512];
    private InputStream input;
    private byte[] obuffer;
    private int ofinish = 0;
    private int ostart = 0;

    private int getMoreData() throws IOException {
        if (this.done) {
            return -1;
        }
        this.ofinish = 0;
        this.ostart = 0;
        int expectedOutputSize = this.cipher.getOutputSize(this.ibuffer.length);
        if (this.obuffer == null || expectedOutputSize > this.obuffer.length) {
            this.obuffer = new byte[expectedOutputSize];
        }
        int readin = this.input.read(this.ibuffer);
        if (readin == -1) {
            this.done = true;
            try {
                this.ofinish = this.cipher.doFinal(this.obuffer, 0);
            } catch (BadPaddingException | IllegalBlockSizeException e) {
                this.obuffer = null;
                throw new IOException((Throwable) e);
            } catch (ShortBufferException e2) {
                this.obuffer = null;
                throw new IllegalStateException("ShortBufferException is not expected", e2);
            }
        } else {
            try {
                this.ofinish = this.cipher.update(this.ibuffer, 0, readin, this.obuffer, 0);
            } catch (IllegalStateException e3) {
                this.obuffer = null;
                throw e3;
            } catch (ShortBufferException e4) {
                this.obuffer = null;
                throw new IllegalStateException("ShortBufferException is not expected", e4);
            }
        }
        return this.ofinish;
    }

    public CipherInputStream(InputStream is, Cipher c) {
        super(is);
        this.input = is;
        this.cipher = c;
    }

    protected CipherInputStream(InputStream is) {
        super(is);
        this.input = is;
        this.cipher = new NullCipher();
    }

    public int read() throws IOException {
        if (this.ostart >= this.ofinish) {
            int i = 0;
            while (i == 0) {
                i = getMoreData();
            }
            if (i == -1) {
                return -1;
            }
        }
        byte[] bArr = this.obuffer;
        int i2 = this.ostart;
        this.ostart = i2 + 1;
        return bArr[i2] & Character.DIRECTIONALITY_UNDEFINED;
    }

    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    public int read(byte[] b, int off, int len) throws IOException {
        if (this.ostart >= this.ofinish) {
            int i = 0;
            while (i == 0) {
                i = getMoreData();
            }
            if (i == -1) {
                return -1;
            }
        }
        if (len <= 0) {
            return 0;
        }
        int available = this.ofinish - this.ostart;
        if (len < available) {
            available = len;
        }
        if (b != null) {
            System.arraycopy(this.obuffer, this.ostart, b, off, available);
        }
        this.ostart += available;
        return available;
    }

    public long skip(long n) throws IOException {
        int available = this.ofinish - this.ostart;
        if (n > ((long) available)) {
            n = (long) available;
        }
        if (n < 0) {
            return 0;
        }
        this.ostart = (int) (((long) this.ostart) + n);
        return n;
    }

    public int available() throws IOException {
        return this.ofinish - this.ostart;
    }

    public void close() throws IOException {
        if (!this.closed) {
            this.closed = true;
            this.input.close();
            if (!this.done) {
                try {
                    this.cipher.doFinal();
                } catch (BadPaddingException | IllegalBlockSizeException ex) {
                    if (ex instanceof AEADBadTagException) {
                        throw new IOException((Throwable) ex);
                    }
                }
            }
            this.ostart = 0;
            this.ofinish = 0;
        }
    }

    public boolean markSupported() {
        return false;
    }
}
