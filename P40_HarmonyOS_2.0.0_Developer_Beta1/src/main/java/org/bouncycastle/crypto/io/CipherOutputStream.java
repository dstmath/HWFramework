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
    private final byte[] oneByte;
    private StreamCipher streamCipher;

    public CipherOutputStream(OutputStream outputStream, BufferedBlockCipher bufferedBlockCipher2) {
        super(outputStream);
        this.oneByte = new byte[1];
        this.bufferedBlockCipher = bufferedBlockCipher2;
    }

    public CipherOutputStream(OutputStream outputStream, StreamCipher streamCipher2) {
        super(outputStream);
        this.oneByte = new byte[1];
        this.streamCipher = streamCipher2;
    }

    public CipherOutputStream(OutputStream outputStream, AEADBlockCipher aEADBlockCipher) {
        super(outputStream);
        this.oneByte = new byte[1];
        this.aeadBlockCipher = aEADBlockCipher;
    }

    private void ensureCapacity(int i, boolean z) {
        if (z) {
            BufferedBlockCipher bufferedBlockCipher2 = this.bufferedBlockCipher;
            if (bufferedBlockCipher2 != null) {
                i = bufferedBlockCipher2.getOutputSize(i);
            } else {
                AEADBlockCipher aEADBlockCipher = this.aeadBlockCipher;
                if (aEADBlockCipher != null) {
                    i = aEADBlockCipher.getOutputSize(i);
                }
            }
        } else {
            BufferedBlockCipher bufferedBlockCipher3 = this.bufferedBlockCipher;
            if (bufferedBlockCipher3 != null) {
                i = bufferedBlockCipher3.getUpdateOutputSize(i);
            } else {
                AEADBlockCipher aEADBlockCipher2 = this.aeadBlockCipher;
                if (aEADBlockCipher2 != null) {
                    i = aEADBlockCipher2.getUpdateOutputSize(i);
                }
            }
        }
        byte[] bArr = this.buf;
        if (bArr == null || bArr.length < i) {
            this.buf = new byte[i];
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0055, code lost:
        if (r1 != null) goto L_0x0058;
     */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x005b A[RETURN] */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x005c  */
    @Override // java.io.FilterOutputStream, java.io.OutputStream, java.io.Closeable, java.lang.AutoCloseable
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

    @Override // java.io.FilterOutputStream, java.io.OutputStream, java.io.Flushable
    public void flush() throws IOException {
        this.out.flush();
    }

    @Override // java.io.FilterOutputStream, java.io.OutputStream
    public void write(int i) throws IOException {
        byte[] bArr = this.oneByte;
        byte b = (byte) i;
        bArr[0] = b;
        if (this.streamCipher != null) {
            this.out.write(this.streamCipher.returnByte(b));
        } else {
            write(bArr, 0, 1);
        }
    }

    @Override // java.io.FilterOutputStream, java.io.OutputStream
    public void write(byte[] bArr) throws IOException {
        write(bArr, 0, bArr.length);
    }

    @Override // java.io.FilterOutputStream, java.io.OutputStream
    public void write(byte[] bArr, int i, int i2) throws IOException {
        int processBytes;
        ensureCapacity(i2, false);
        BufferedBlockCipher bufferedBlockCipher2 = this.bufferedBlockCipher;
        if (bufferedBlockCipher2 != null) {
            processBytes = bufferedBlockCipher2.processBytes(bArr, i, i2, this.buf, 0);
            if (processBytes == 0) {
                return;
            }
        } else {
            AEADBlockCipher aEADBlockCipher = this.aeadBlockCipher;
            if (aEADBlockCipher != null) {
                processBytes = aEADBlockCipher.processBytes(bArr, i, i2, this.buf, 0);
                if (processBytes == 0) {
                    return;
                }
            } else {
                this.streamCipher.processBytes(bArr, i, i2, this.buf, 0);
                this.out.write(this.buf, 0, i2);
                return;
            }
        }
        this.out.write(this.buf, 0, processBytes);
    }
}
