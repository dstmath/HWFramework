package org.bouncycastle.crypto.io;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.bouncycastle.crypto.BufferedBlockCipher;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.StreamCipher;
import org.bouncycastle.crypto.modes.AEADBlockCipher;

public class CipherOutputStream extends FilterOutputStream {
    private AEADBlockCipher aeadBlockCipher;
    private byte[] buf;
    private BufferedBlockCipher bufferedBlockCipher;
    private final byte[] oneByte = new byte[1];
    private StreamCipher streamCipher;

    public CipherOutputStream(OutputStream outputStream, BufferedBlockCipher bufferedBlockCipher2) {
        super(outputStream);
        this.bufferedBlockCipher = bufferedBlockCipher2;
    }

    public CipherOutputStream(OutputStream outputStream, StreamCipher streamCipher2) {
        super(outputStream);
        this.streamCipher = streamCipher2;
    }

    public CipherOutputStream(OutputStream outputStream, AEADBlockCipher aEADBlockCipher) {
        super(outputStream);
        this.aeadBlockCipher = aEADBlockCipher;
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

    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0055, code lost:
        if (r1 != null) goto L_0x0058;
     */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x005b A[RETURN] */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x005c  */
    public void close() throws IOException {
        IOException iOException;
        IOException e;
        int doFinal;
        OutputStream outputStream;
        byte[] bArr;
        ensureCapacity(0, true);
        try {
            if (this.bufferedBlockCipher != null) {
                doFinal = this.bufferedBlockCipher.doFinal(this.buf, 0);
                if (doFinal != 0) {
                    outputStream = this.out;
                    bArr = this.buf;
                }
                iOException = null;
                flush();
                this.out.close();
                e = iOException;
                if (e != null) {
                    throw e;
                }
                return;
            }
            if (this.aeadBlockCipher != null) {
                doFinal = this.aeadBlockCipher.doFinal(this.buf, 0);
                if (doFinal != 0) {
                    outputStream = this.out;
                    bArr = this.buf;
                }
            } else if (this.streamCipher != null) {
                this.streamCipher.reset();
            }
            iOException = null;
            flush();
            this.out.close();
            e = iOException;
            if (e != null) {
            }
            outputStream.write(bArr, 0, doFinal);
            iOException = null;
        } catch (InvalidCipherTextException e2) {
            iOException = new InvalidCipherTextIOException("Error finalising cipher data", e2);
        } catch (Exception e3) {
            iOException = new CipherIOException("Error closing stream: ", e3);
        }
        try {
            flush();
            this.out.close();
        } catch (IOException e4) {
            e = e4;
        }
        e = iOException;
        if (e != null) {
        }
    }

    public void flush() throws IOException {
        this.out.flush();
    }

    public void write(int i) throws IOException {
        byte b = (byte) i;
        this.oneByte[0] = b;
        if (this.streamCipher != null) {
            this.out.write(this.streamCipher.returnByte(b));
        } else {
            write(this.oneByte, 0, 1);
        }
    }

    public void write(byte[] bArr) throws IOException {
        write(bArr, 0, bArr.length);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:3:0x0014, code lost:
        if (r9 != 0) goto L_0x0016;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x002e, code lost:
        if (r9 != 0) goto L_0x0016;
     */
    public void write(byte[] bArr, int i, int i2) throws IOException {
        int processBytes;
        ensureCapacity(i2, false);
        if (this.bufferedBlockCipher != null) {
            processBytes = this.bufferedBlockCipher.processBytes(bArr, i, i2, this.buf, 0);
        } else {
            if (this.aeadBlockCipher != null) {
                processBytes = this.aeadBlockCipher.processBytes(bArr, i, i2, this.buf, 0);
            } else {
                this.streamCipher.processBytes(bArr, i, i2, this.buf, 0);
                this.out.write(this.buf, 0, i2);
            }
            return;
        }
        this.out.write(this.buf, 0, processBytes);
    }
}
