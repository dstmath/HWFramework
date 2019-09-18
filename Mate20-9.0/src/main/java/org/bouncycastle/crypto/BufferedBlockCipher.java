package org.bouncycastle.crypto;

public class BufferedBlockCipher {
    protected byte[] buf;
    protected int bufOff;
    protected BlockCipher cipher;
    protected boolean forEncryption;
    protected boolean partialBlockOkay;
    protected boolean pgpCFB;

    protected BufferedBlockCipher() {
    }

    public BufferedBlockCipher(BlockCipher blockCipher) {
        this.cipher = blockCipher;
        this.buf = new byte[blockCipher.getBlockSize()];
        boolean z = false;
        this.bufOff = 0;
        String algorithmName = blockCipher.getAlgorithmName();
        int indexOf = algorithmName.indexOf(47) + 1;
        this.pgpCFB = indexOf > 0 && algorithmName.startsWith("PGP", indexOf);
        if (this.pgpCFB || (blockCipher instanceof StreamCipher)) {
            this.partialBlockOkay = true;
            return;
        }
        if (indexOf > 0 && algorithmName.startsWith("OpenPGP", indexOf)) {
            z = true;
        }
        this.partialBlockOkay = z;
    }

    public int doFinal(byte[] bArr, int i) throws DataLengthException, IllegalStateException, InvalidCipherTextException {
        int i2;
        try {
            if (this.bufOff + i <= bArr.length) {
                if (this.bufOff == 0) {
                    i2 = 0;
                } else if (this.partialBlockOkay) {
                    this.cipher.processBlock(this.buf, 0, this.buf, 0);
                    i2 = this.bufOff;
                    this.bufOff = 0;
                    System.arraycopy(this.buf, 0, bArr, i, i2);
                } else {
                    throw new DataLengthException("data not block size aligned");
                }
                return i2;
            }
            throw new OutputLengthException("output buffer too short for doFinal()");
        } finally {
            reset();
        }
    }

    public int getBlockSize() {
        return this.cipher.getBlockSize();
    }

    public int getOutputSize(int i) {
        return i + this.bufOff;
    }

    public BlockCipher getUnderlyingCipher() {
        return this.cipher;
    }

    public int getUpdateOutputSize(int i) {
        int i2;
        int i3;
        int i4 = i + this.bufOff;
        if (!this.pgpCFB) {
            i3 = this.buf.length;
        } else if (this.forEncryption) {
            i2 = (i4 % this.buf.length) - (this.cipher.getBlockSize() + 2);
            return i4 - i2;
        } else {
            i3 = this.buf.length;
        }
        i2 = i4 % i3;
        return i4 - i2;
    }

    public void init(boolean z, CipherParameters cipherParameters) throws IllegalArgumentException {
        this.forEncryption = z;
        reset();
        this.cipher.init(z, cipherParameters);
    }

    public int processByte(byte b, byte[] bArr, int i) throws DataLengthException, IllegalStateException {
        byte[] bArr2 = this.buf;
        int i2 = this.bufOff;
        this.bufOff = i2 + 1;
        bArr2[i2] = b;
        if (this.bufOff != this.buf.length) {
            return 0;
        }
        int processBlock = this.cipher.processBlock(this.buf, 0, bArr, i);
        this.bufOff = 0;
        return processBlock;
    }

    public int processBytes(byte[] bArr, int i, int i2, byte[] bArr2, int i3) throws DataLengthException, IllegalStateException {
        int i4;
        if (i2 >= 0) {
            int blockSize = getBlockSize();
            int updateOutputSize = getUpdateOutputSize(i2);
            if (updateOutputSize <= 0 || updateOutputSize + i3 <= bArr2.length) {
                int length = this.buf.length - this.bufOff;
                if (i2 > length) {
                    System.arraycopy(bArr, i, this.buf, this.bufOff, length);
                    i4 = this.cipher.processBlock(this.buf, 0, bArr2, i3) + 0;
                    this.bufOff = 0;
                    i2 -= length;
                    i += length;
                    while (i2 > this.buf.length) {
                        i4 += this.cipher.processBlock(bArr, i, bArr2, i3 + i4);
                        i2 -= blockSize;
                        i += blockSize;
                    }
                } else {
                    i4 = 0;
                }
                System.arraycopy(bArr, i, this.buf, this.bufOff, i2);
                this.bufOff += i2;
                if (this.bufOff != this.buf.length) {
                    return i4;
                }
                int processBlock = i4 + this.cipher.processBlock(this.buf, 0, bArr2, i3 + i4);
                this.bufOff = 0;
                return processBlock;
            }
            throw new OutputLengthException("output buffer too short");
        }
        throw new IllegalArgumentException("Can't have a negative input length!");
    }

    public void reset() {
        for (int i = 0; i < this.buf.length; i++) {
            this.buf[i] = 0;
        }
        this.bufOff = 0;
        this.cipher.reset();
    }
}
