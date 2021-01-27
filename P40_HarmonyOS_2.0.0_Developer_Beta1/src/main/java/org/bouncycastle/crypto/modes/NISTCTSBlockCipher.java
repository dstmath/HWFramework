package org.bouncycastle.crypto.modes;

import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.BufferedBlockCipher;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.OutputLengthException;

public class NISTCTSBlockCipher extends BufferedBlockCipher {
    public static final int CS1 = 1;
    public static final int CS2 = 2;
    public static final int CS3 = 3;
    private final int blockSize;
    private final int type;

    public NISTCTSBlockCipher(int i, BlockCipher blockCipher) {
        this.type = i;
        this.cipher = new CBCBlockCipher(blockCipher);
        this.blockSize = blockCipher.getBlockSize();
        this.buf = new byte[(this.blockSize * 2)];
        this.bufOff = 0;
    }

    @Override // org.bouncycastle.crypto.BufferedBlockCipher
    public int doFinal(byte[] bArr, int i) throws DataLengthException, IllegalStateException, InvalidCipherTextException {
        byte[] bArr2;
        if (this.bufOff + i <= bArr.length) {
            int blockSize2 = this.cipher.getBlockSize();
            int i2 = this.bufOff - blockSize2;
            byte[] bArr3 = new byte[blockSize2];
            if (this.forEncryption) {
                if (this.bufOff >= blockSize2) {
                    if (this.bufOff > blockSize2) {
                        bArr2 = new byte[blockSize2];
                        int i3 = this.type;
                        if (i3 == 2 || i3 == 3) {
                            this.cipher.processBlock(this.buf, 0, bArr3, 0);
                            System.arraycopy(this.buf, blockSize2, bArr2, 0, i2);
                            this.cipher.processBlock(bArr2, 0, bArr2, 0);
                            if (this.type == 2 && i2 == blockSize2) {
                                System.arraycopy(bArr3, 0, bArr, i, blockSize2);
                            } else {
                                System.arraycopy(bArr2, 0, bArr, i, blockSize2);
                                System.arraycopy(bArr3, 0, bArr, i + blockSize2, i2);
                                int i4 = this.bufOff;
                                reset();
                                return i4;
                            }
                        } else {
                            System.arraycopy(this.buf, 0, bArr3, 0, blockSize2);
                            this.cipher.processBlock(bArr3, 0, bArr3, 0);
                            System.arraycopy(bArr3, 0, bArr, i, i2);
                            System.arraycopy(this.buf, this.bufOff - i2, bArr2, 0, i2);
                            this.cipher.processBlock(bArr2, 0, bArr2, 0);
                            System.arraycopy(bArr2, 0, bArr, i + i2, blockSize2);
                            int i42 = this.bufOff;
                            reset();
                            return i42;
                        }
                    }
                    this.cipher.processBlock(this.buf, 0, bArr3, 0);
                    System.arraycopy(bArr3, 0, bArr, i, blockSize2);
                    int i422 = this.bufOff;
                    reset();
                    return i422;
                }
                throw new DataLengthException("need at least one block of input for NISTCTS");
            } else if (this.bufOff >= blockSize2) {
                bArr2 = new byte[blockSize2];
                if (this.bufOff > blockSize2) {
                    int i5 = this.type;
                    if (i5 == 3 || (i5 == 2 && (this.buf.length - this.bufOff) % blockSize2 != 0)) {
                        (this.cipher instanceof CBCBlockCipher ? ((CBCBlockCipher) this.cipher).getUnderlyingCipher() : this.cipher).processBlock(this.buf, 0, bArr3, 0);
                        for (int i6 = blockSize2; i6 != this.bufOff; i6++) {
                            int i7 = i6 - blockSize2;
                            bArr2[i7] = (byte) (bArr3[i7] ^ this.buf[i6]);
                        }
                        System.arraycopy(this.buf, blockSize2, bArr3, 0, i2);
                        this.cipher.processBlock(bArr3, 0, bArr, i);
                    } else {
                        ((CBCBlockCipher) this.cipher).getUnderlyingCipher().processBlock(this.buf, this.bufOff - blockSize2, bArr2, 0);
                        System.arraycopy(this.buf, 0, bArr3, 0, blockSize2);
                        if (i2 != blockSize2) {
                            System.arraycopy(bArr2, i2, bArr3, i2, blockSize2 - i2);
                        }
                        this.cipher.processBlock(bArr3, 0, bArr3, 0);
                        System.arraycopy(bArr3, 0, bArr, i, blockSize2);
                        for (int i8 = 0; i8 != i2; i8++) {
                            bArr2[i8] = (byte) (bArr2[i8] ^ this.buf[i8]);
                        }
                    }
                }
                this.cipher.processBlock(this.buf, 0, bArr3, 0);
                System.arraycopy(bArr3, 0, bArr, i, blockSize2);
                int i4222 = this.bufOff;
                reset();
                return i4222;
            } else {
                throw new DataLengthException("need at least one block of input for CTS");
            }
            System.arraycopy(bArr2, 0, bArr, i + blockSize2, i2);
            int i42222 = this.bufOff;
            reset();
            return i42222;
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
