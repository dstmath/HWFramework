package javax.crypto;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Modifier;

public class CipherInputStream extends FilterInputStream {
    private Cipher cipher;
    private boolean done;
    private byte[] ibuffer;
    private InputStream input;
    private byte[] obuffer;
    private int ofinish;
    private int ostart;

    private int getMoreData() throws IOException {
        if (this.done) {
            return -1;
        }
        int readin = this.input.read(this.ibuffer);
        if (readin == -1) {
            this.done = true;
            try {
                this.obuffer = this.cipher.doFinal();
            } catch (IllegalBlockSizeException e) {
                this.obuffer = null;
            } catch (BadPaddingException e2) {
                this.obuffer = null;
            }
            if (this.obuffer == null) {
                return -1;
            }
            this.ostart = 0;
            this.ofinish = this.obuffer.length;
            return this.ofinish;
        }
        try {
            this.obuffer = this.cipher.update(this.ibuffer, 0, readin);
        } catch (IllegalStateException e3) {
            this.obuffer = null;
        }
        this.ostart = 0;
        if (this.obuffer == null) {
            this.ofinish = 0;
        } else {
            this.ofinish = this.obuffer.length;
        }
        return this.ofinish;
    }

    public CipherInputStream(InputStream is, Cipher c) {
        super(is);
        this.ibuffer = new byte[Modifier.INTERFACE];
        this.done = false;
        this.ostart = 0;
        this.ofinish = 0;
        this.input = is;
        this.cipher = c;
    }

    protected CipherInputStream(InputStream is) {
        super(is);
        this.ibuffer = new byte[Modifier.INTERFACE];
        this.done = false;
        this.ostart = 0;
        this.ofinish = 0;
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

    public void close() throws IOException {
        this.input.close();
        try {
            this.cipher.doFinal();
        } catch (BadPaddingException e) {
        } catch (IllegalBlockSizeException e2) {
        }
        this.ostart = 0;
        this.ofinish = 0;
    }

    public boolean markSupported() {
        return false;
    }
}
