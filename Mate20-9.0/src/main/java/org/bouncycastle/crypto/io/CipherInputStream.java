package org.bouncycastle.crypto.io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.bouncycastle.crypto.BufferedBlockCipher;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.SkippingCipher;
import org.bouncycastle.crypto.StreamCipher;
import org.bouncycastle.crypto.modes.AEADBlockCipher;
import org.bouncycastle.util.Arrays;

public class CipherInputStream extends FilterInputStream {
    private static final int INPUT_BUF_SIZE = 2048;
    private AEADBlockCipher aeadBlockCipher;
    private byte[] buf;
    private int bufOff;
    private BufferedBlockCipher bufferedBlockCipher;
    private boolean finalized;
    private byte[] inBuf;
    private byte[] markBuf;
    private int markBufOff;
    private long markPosition;
    private int maxBuf;
    private SkippingCipher skippingCipher;
    private StreamCipher streamCipher;

    public CipherInputStream(InputStream inputStream, BufferedBlockCipher bufferedBlockCipher2) {
        this(inputStream, bufferedBlockCipher2, 2048);
    }

    public CipherInputStream(InputStream inputStream, BufferedBlockCipher bufferedBlockCipher2, int i) {
        super(inputStream);
        this.bufferedBlockCipher = bufferedBlockCipher2;
        this.inBuf = new byte[i];
        this.skippingCipher = bufferedBlockCipher2 instanceof SkippingCipher ? (SkippingCipher) bufferedBlockCipher2 : null;
    }

    public CipherInputStream(InputStream inputStream, StreamCipher streamCipher2) {
        this(inputStream, streamCipher2, 2048);
    }

    public CipherInputStream(InputStream inputStream, StreamCipher streamCipher2, int i) {
        super(inputStream);
        this.streamCipher = streamCipher2;
        this.inBuf = new byte[i];
        this.skippingCipher = streamCipher2 instanceof SkippingCipher ? (SkippingCipher) streamCipher2 : null;
    }

    public CipherInputStream(InputStream inputStream, AEADBlockCipher aEADBlockCipher) {
        this(inputStream, aEADBlockCipher, 2048);
    }

    public CipherInputStream(InputStream inputStream, AEADBlockCipher aEADBlockCipher, int i) {
        super(inputStream);
        this.aeadBlockCipher = aEADBlockCipher;
        this.inBuf = new byte[i];
        this.skippingCipher = aEADBlockCipher instanceof SkippingCipher ? (SkippingCipher) aEADBlockCipher : null;
    }

    private void ensureCapacity(int i, boolean z) {
        if (z) {
            if (this.bufferedBlockCipher != null) {
                i = this.bufferedBlockCipher.getOutputSize(i);
            } else if (this.aeadBlockCipher != null) {
                i = this.aeadBlockCipher.getOutputSize(i);
            }
        } else if (this.bufferedBlockCipher != null) {
            i = this.bufferedBlockCipher.getUpdateOutputSize(i);
        } else if (this.aeadBlockCipher != null) {
            i = this.aeadBlockCipher.getUpdateOutputSize(i);
        }
        if (this.buf == null || this.buf.length < i) {
            this.buf = new byte[i];
        }
    }

    private void finaliseCipher() throws IOException {
        int doFinal;
        try {
            this.finalized = true;
            ensureCapacity(0, true);
            if (this.bufferedBlockCipher != null) {
                doFinal = this.bufferedBlockCipher.doFinal(this.buf, 0);
            } else if (this.aeadBlockCipher != null) {
                doFinal = this.aeadBlockCipher.doFinal(this.buf, 0);
            } else {
                this.maxBuf = 0;
                return;
            }
            this.maxBuf = doFinal;
        } catch (InvalidCipherTextException e) {
            throw new InvalidCipherTextIOException("Error finalising cipher", e);
        } catch (Exception e2) {
            throw new IOException("Error finalising cipher " + e2);
        }
    }

    private int nextChunk() throws IOException {
        if (this.finalized) {
            return -1;
        }
        this.bufOff = 0;
        this.maxBuf = 0;
        while (this.maxBuf == 0) {
            int read = this.in.read(this.inBuf);
            if (read == -1) {
                finaliseCipher();
                if (this.maxBuf == 0) {
                    return -1;
                }
                return this.maxBuf;
            }
            try {
                ensureCapacity(read, false);
                if (this.bufferedBlockCipher != null) {
                    read = this.bufferedBlockCipher.processBytes(this.inBuf, 0, read, this.buf, 0);
                } else if (this.aeadBlockCipher != null) {
                    read = this.aeadBlockCipher.processBytes(this.inBuf, 0, read, this.buf, 0);
                } else {
                    this.streamCipher.processBytes(this.inBuf, 0, read, this.buf, 0);
                }
                this.maxBuf = read;
            } catch (Exception e) {
                throw new CipherIOException("Error processing stream ", e);
            }
        }
        return this.maxBuf;
    }

    public int available() throws IOException {
        return this.maxBuf - this.bufOff;
    }

    public void close() throws IOException {
        try {
            this.in.close();
            this.bufOff = 0;
            this.maxBuf = 0;
            this.markBufOff = 0;
            this.markPosition = 0;
            if (this.markBuf != null) {
                Arrays.fill(this.markBuf, (byte) 0);
                this.markBuf = null;
            }
            if (this.buf != null) {
                Arrays.fill(this.buf, (byte) 0);
                this.buf = null;
            }
            Arrays.fill(this.inBuf, (byte) 0);
        } finally {
            if (!this.finalized) {
                finaliseCipher();
            }
        }
    }

    public void mark(int i) {
        this.in.mark(i);
        if (this.skippingCipher != null) {
            this.markPosition = this.skippingCipher.getPosition();
        }
        if (this.buf != null) {
            this.markBuf = new byte[this.buf.length];
            System.arraycopy(this.buf, 0, this.markBuf, 0, this.buf.length);
        }
        this.markBufOff = this.bufOff;
    }

    public boolean markSupported() {
        if (this.skippingCipher != null) {
            return this.in.markSupported();
        }
        return false;
    }

    public int read() throws IOException {
        if (this.bufOff >= this.maxBuf && nextChunk() < 0) {
            return -1;
        }
        byte[] bArr = this.buf;
        int i = this.bufOff;
        this.bufOff = i + 1;
        return bArr[i] & 255;
    }

    public int read(byte[] bArr) throws IOException {
        return read(bArr, 0, bArr.length);
    }

    public int read(byte[] bArr, int i, int i2) throws IOException {
        if (this.bufOff >= this.maxBuf && nextChunk() < 0) {
            return -1;
        }
        int min = Math.min(i2, available());
        System.arraycopy(this.buf, this.bufOff, bArr, i, min);
        this.bufOff += min;
        return min;
    }

    public void reset() throws IOException {
        if (this.skippingCipher != null) {
            this.in.reset();
            this.skippingCipher.seekTo(this.markPosition);
            if (this.markBuf != null) {
                this.buf = this.markBuf;
            }
            this.bufOff = this.markBufOff;
            return;
        }
        throw new IOException("cipher must implement SkippingCipher to be used with reset()");
    }

    public long skip(long j) throws IOException {
        if (j <= 0) {
            return 0;
        }
        if (this.skippingCipher != null) {
            long available = (long) available();
            if (j <= available) {
                this.bufOff = (int) (((long) this.bufOff) + j);
                return j;
            }
            this.bufOff = this.maxBuf;
            long skip = this.in.skip(j - available);
            if (skip == this.skippingCipher.skip(skip)) {
                return skip + available;
            }
            throw new IOException("Unable to skip cipher " + skip + " bytes.");
        }
        int min = (int) Math.min(j, (long) available());
        this.bufOff += min;
        return (long) min;
    }
}
