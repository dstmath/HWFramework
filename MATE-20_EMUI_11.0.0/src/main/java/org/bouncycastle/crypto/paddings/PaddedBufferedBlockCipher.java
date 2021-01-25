package org.bouncycastle.crypto.paddings;

import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.BufferedBlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.OutputLengthException;
import org.bouncycastle.crypto.params.ParametersWithRandom;

public class PaddedBufferedBlockCipher extends BufferedBlockCipher {
    BlockCipherPadding padding;

    public PaddedBufferedBlockCipher(BlockCipher blockCipher) {
        this(blockCipher, new PKCS7Padding());
    }

    public PaddedBufferedBlockCipher(BlockCipher blockCipher, BlockCipherPadding blockCipherPadding) {
        this.cipher = blockCipher;
        this.padding = blockCipherPadding;
        this.buf = new byte[blockCipher.getBlockSize()];
        this.bufOff = 0;
    }

    @Override // org.bouncycastle.crypto.BufferedBlockCipher
    public int doFinal(byte[] bArr, int i) throws DataLengthException, IllegalStateException, InvalidCipherTextException {
        int padCount;
        int i2;
        int blockSize = this.cipher.getBlockSize();
        if (this.forEncryption) {
            if (this.bufOff != blockSize) {
                i2 = 0;
            } else if ((blockSize * 2) + i <= bArr.length) {
                i2 = this.cipher.processBlock(this.buf, 0, bArr, i);
                this.bufOff = 0;
            } else {
                reset();
                throw new OutputLengthException("output buffer too short");
            }
            this.padding.addPadding(this.buf, this.bufOff);
            padCount = i2 + this.cipher.processBlock(this.buf, 0, bArr, i + i2);
        } else if (this.bufOff == blockSize) {
            int processBlock = this.cipher.processBlock(this.buf, 0, this.buf, 0);
            this.bufOff = 0;
            try {
                padCount = processBlock - this.padding.padCount(this.buf);
                System.arraycopy(this.buf, 0, bArr, i, padCount);
            } catch (Throwable th) {
                reset();
                throw th;
            }
        } else {
            reset();
            throw new DataLengthException("last block incomplete in decryption");
        }
        reset();
        return padCount;
    }

    @Override // org.bouncycastle.crypto.BufferedBlockCipher
    public int getOutputSize(int i) {
        int length;
        int i2 = i + this.bufOff;
        int length2 = i2 % this.buf.length;
        if (length2 != 0) {
            i2 -= length2;
            length = this.buf.length;
        } else if (!this.forEncryption) {
            return i2;
        } else {
            length = this.buf.length;
        }
        return i2 + length;
    }

    @Override // org.bouncycastle.crypto.BufferedBlockCipher
    public int getUpdateOutputSize(int i) {
        int i2 = i + this.bufOff;
        int length = i2 % this.buf.length;
        return length == 0 ? Math.max(0, i2 - this.buf.length) : i2 - length;
    }

    @Override // org.bouncycastle.crypto.BufferedBlockCipher
    public void init(boolean z, CipherParameters cipherParameters) throws IllegalArgumentException {
        BlockCipher blockCipher;
        this.forEncryption = z;
        reset();
        if (cipherParameters instanceof ParametersWithRandom) {
            ParametersWithRandom parametersWithRandom = (ParametersWithRandom) cipherParameters;
            this.padding.init(parametersWithRandom.getRandom());
            blockCipher = this.cipher;
            cipherParameters = parametersWithRandom.getParameters();
        } else {
            this.padding.init(null);
            blockCipher = this.cipher;
        }
        blockCipher.init(z, cipherParameters);
    }

    @Override // org.bouncycastle.crypto.BufferedBlockCipher
    public int processByte(byte b, byte[] bArr, int i) throws DataLengthException, IllegalStateException {
        int i2;
        if (this.bufOff == this.buf.length) {
            i2 = this.cipher.processBlock(this.buf, 0, bArr, i);
            this.bufOff = 0;
        } else {
            i2 = 0;
        }
        byte[] bArr2 = this.buf;
        int i3 = this.bufOff;
        this.bufOff = i3 + 1;
        bArr2[i3] = b;
        return i2;
    }

    @Override // org.bouncycastle.crypto.BufferedBlockCipher
    public int processBytes(byte[] bArr, int i, int i2, byte[] bArr2, int i3) throws DataLengthException, IllegalStateException {
        if (i2 >= 0) {
            int blockSize = getBlockSize();
            int updateOutputSize = getUpdateOutputSize(i2);
            if (updateOutputSize <= 0 || updateOutputSize + i3 <= bArr2.length) {
                int length = this.buf.length - this.bufOff;
                int i4 = 0;
                if (i2 > length) {
                    System.arraycopy(bArr, i, this.buf, this.bufOff, length);
                    this.bufOff = 0;
                    i2 -= length;
                    i += length;
                    i4 = this.cipher.processBlock(this.buf, 0, bArr2, i3) + 0;
                    while (i2 > this.buf.length) {
                        i4 += this.cipher.processBlock(bArr, i, bArr2, i3 + i4);
                        i2 -= blockSize;
                        i += blockSize;
                    }
                }
                System.arraycopy(bArr, i, this.buf, this.bufOff, i2);
                this.bufOff += i2;
                return i4;
            }
            throw new OutputLengthException("output buffer too short");
        }
        throw new IllegalArgumentException("Can't have a negative input length!");
    }
}
