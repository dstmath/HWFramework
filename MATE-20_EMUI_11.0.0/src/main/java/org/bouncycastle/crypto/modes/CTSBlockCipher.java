package org.bouncycastle.crypto.modes;

import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.BufferedBlockCipher;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.OutputLengthException;
import org.bouncycastle.crypto.StreamBlockCipher;

public class CTSBlockCipher extends BufferedBlockCipher {
    private int blockSize;

    public CTSBlockCipher(BlockCipher blockCipher) {
        if (!(blockCipher instanceof StreamBlockCipher)) {
            this.cipher = blockCipher;
            this.blockSize = blockCipher.getBlockSize();
            this.buf = new byte[(this.blockSize * 2)];
            this.bufOff = 0;
            return;
        }
        throw new IllegalArgumentException("CTSBlockCipher can only accept ECB, or CBC ciphers");
    }

    @Override // org.bouncycastle.crypto.BufferedBlockCipher
    public int doFinal(byte[] bArr, int i) throws DataLengthException, IllegalStateException, InvalidCipherTextException {
        if (this.bufOff + i <= bArr.length) {
            int blockSize2 = this.cipher.getBlockSize();
            int i2 = this.bufOff - blockSize2;
            byte[] bArr2 = new byte[blockSize2];
            if (this.forEncryption) {
                if (this.bufOff >= blockSize2) {
                    this.cipher.processBlock(this.buf, 0, bArr2, 0);
                    if (this.bufOff > blockSize2) {
                        for (int i3 = this.bufOff; i3 != this.buf.length; i3++) {
                            this.buf[i3] = bArr2[i3 - blockSize2];
                        }
                        for (int i4 = blockSize2; i4 != this.bufOff; i4++) {
                            byte[] bArr3 = this.buf;
                            bArr3[i4] = (byte) (bArr3[i4] ^ bArr2[i4 - blockSize2]);
                        }
                        (this.cipher instanceof CBCBlockCipher ? ((CBCBlockCipher) this.cipher).getUnderlyingCipher() : this.cipher).processBlock(this.buf, blockSize2, bArr, i);
                        System.arraycopy(bArr2, 0, bArr, i + blockSize2, i2);
                        int i5 = this.bufOff;
                        reset();
                        return i5;
                    }
                } else {
                    throw new DataLengthException("need at least one block of input for CTS");
                }
            } else if (this.bufOff >= blockSize2) {
                byte[] bArr4 = new byte[blockSize2];
                if (this.bufOff > blockSize2) {
                    (this.cipher instanceof CBCBlockCipher ? ((CBCBlockCipher) this.cipher).getUnderlyingCipher() : this.cipher).processBlock(this.buf, 0, bArr2, 0);
                    for (int i6 = blockSize2; i6 != this.bufOff; i6++) {
                        int i7 = i6 - blockSize2;
                        bArr4[i7] = (byte) (bArr2[i7] ^ this.buf[i6]);
                    }
                    System.arraycopy(this.buf, blockSize2, bArr2, 0, i2);
                    this.cipher.processBlock(bArr2, 0, bArr, i);
                    System.arraycopy(bArr4, 0, bArr, i + blockSize2, i2);
                    int i52 = this.bufOff;
                    reset();
                    return i52;
                }
                this.cipher.processBlock(this.buf, 0, bArr2, 0);
            } else {
                throw new DataLengthException("need at least one block of input for CTS");
            }
            System.arraycopy(bArr2, 0, bArr, i, blockSize2);
            int i522 = this.bufOff;
            reset();
            return i522;
        }
        throw new OutputLengthException("output buffer to small in doFinal");
    }

    @Override // org.bouncycastle.crypto.BufferedBlockCipher
    public int getOutputSize(int i) {
        return i + this.bufOff;
    }

    @Override // org.bouncycastle.crypto.BufferedBlockCipher
    public int getUpdateOutputSize(int i) {
        int i2 = i + this.bufOff;
        int length = i2 % this.buf.length;
        return length == 0 ? i2 - this.buf.length : i2 - length;
    }

    @Override // org.bouncycastle.crypto.BufferedBlockCipher
    public int processByte(byte b, byte[] bArr, int i) throws DataLengthException, IllegalStateException {
        int i2;
        if (this.bufOff == this.buf.length) {
            i2 = this.cipher.processBlock(this.buf, 0, bArr, i);
            System.arraycopy(this.buf, this.blockSize, this.buf, 0, this.blockSize);
            this.bufOff = this.blockSize;
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
            int blockSize2 = getBlockSize();
            int updateOutputSize = getUpdateOutputSize(i2);
            if (updateOutputSize <= 0 || updateOutputSize + i3 <= bArr2.length) {
                int length = this.buf.length - this.bufOff;
                int i4 = 0;
                if (i2 > length) {
                    System.arraycopy(bArr, i, this.buf, this.bufOff, length);
                    int processBlock = this.cipher.processBlock(this.buf, 0, bArr2, i3) + 0;
                    System.arraycopy(this.buf, blockSize2, this.buf, 0, blockSize2);
                    this.bufOff = blockSize2;
                    i2 -= length;
                    i += length;
                    while (i2 > blockSize2) {
                        System.arraycopy(bArr, i, this.buf, this.bufOff, blockSize2);
                        processBlock += this.cipher.processBlock(this.buf, 0, bArr2, i3 + processBlock);
                        System.arraycopy(this.buf, blockSize2, this.buf, 0, blockSize2);
                        i2 -= blockSize2;
                        i += blockSize2;
                    }
                    i4 = processBlock;
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
