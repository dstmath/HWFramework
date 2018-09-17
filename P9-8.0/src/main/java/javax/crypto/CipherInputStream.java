package javax.crypto;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class CipherInputStream extends FilterInputStream {
    private Cipher cipher;
    private boolean closed;
    private boolean done;
    private byte[] ibuffer;
    private InputStream input;
    private byte[] obuffer;
    private int ofinish;
    private int ostart;

    /* JADX WARNING: Removed duplicated region for block: B:18:0x0049 A:{Splitter: B:11:0x002f, ExcHandler: javax.crypto.IllegalBlockSizeException (r7_0 'e' java.lang.Throwable)} */
    /* JADX WARNING: Missing block: B:18:0x0049, code:
            r7 = move-exception;
     */
    /* JADX WARNING: Missing block: B:19:0x004a, code:
            r11.obuffer = null;
     */
    /* JADX WARNING: Missing block: B:20:0x0051, code:
            throw new java.io.IOException(r7);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
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
            } catch (Throwable e) {
            } catch (ShortBufferException e2) {
                this.obuffer = null;
                throw new IllegalStateException("ShortBufferException is not expected", e2);
            }
        }
        try {
            this.ofinish = this.cipher.update(this.ibuffer, 0, readin, this.obuffer, 0);
        } catch (IllegalStateException e3) {
            this.obuffer = null;
            throw e3;
        } catch (ShortBufferException e22) {
            this.obuffer = null;
            throw new IllegalStateException("ShortBufferException is not expected", e22);
        }
        return this.ofinish;
    }

    public CipherInputStream(InputStream is, Cipher c) {
        super(is);
        this.ibuffer = new byte[512];
        this.done = false;
        this.ostart = 0;
        this.ofinish = 0;
        this.closed = false;
        this.input = is;
        this.cipher = c;
    }

    protected CipherInputStream(InputStream is) {
        super(is);
        this.ibuffer = new byte[512];
        this.done = false;
        this.ostart = 0;
        this.ofinish = 0;
        this.closed = false;
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
        return bArr[i2] & 255;
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

    /* JADX WARNING: Removed duplicated region for block: B:9:0x001c A:{Splitter: B:5:0x0012, ExcHandler: javax.crypto.BadPaddingException (r0_0 'ex' java.lang.Throwable)} */
    /* JADX WARNING: Missing block: B:9:0x001c, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:11:0x001f, code:
            if ((r0 instanceof javax.crypto.AEADBadTagException) != false) goto L_0x0021;
     */
    /* JADX WARNING: Missing block: B:13:0x0026, code:
            throw new java.io.IOException(r0);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void close() throws IOException {
        if (!this.closed) {
            this.closed = true;
            this.input.close();
            if (!this.done) {
                try {
                    this.cipher.doFinal();
                } catch (Throwable ex) {
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
