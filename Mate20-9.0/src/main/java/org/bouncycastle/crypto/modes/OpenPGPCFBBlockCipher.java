package org.bouncycastle.crypto.modes;

import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.OutputLengthException;

public class OpenPGPCFBBlockCipher implements BlockCipher {
    private byte[] FR = new byte[this.blockSize];
    private byte[] FRE = new byte[this.blockSize];
    private byte[] IV = new byte[this.blockSize];
    private int blockSize;
    private BlockCipher cipher;
    private int count;
    private boolean forEncryption;

    public OpenPGPCFBBlockCipher(BlockCipher blockCipher) {
        this.cipher = blockCipher;
        this.blockSize = blockCipher.getBlockSize();
    }

    private int decryptBlock(byte[] bArr, int i, byte[] bArr2, int i2) throws DataLengthException, IllegalStateException {
        if (this.blockSize + i > bArr.length) {
            throw new DataLengthException("input buffer too short");
        } else if (this.blockSize + i2 <= bArr2.length) {
            int i3 = 2;
            if (this.count > this.blockSize) {
                byte b = bArr[i];
                this.FR[this.blockSize - 2] = b;
                bArr2[i2] = encryptByte(b, this.blockSize - 2);
                byte b2 = bArr[i + 1];
                this.FR[this.blockSize - 1] = b2;
                bArr2[i2 + 1] = encryptByte(b2, this.blockSize - 1);
                this.cipher.processBlock(this.FR, 0, this.FRE, 0);
                while (i3 < this.blockSize) {
                    byte b3 = bArr[i + i3];
                    int i4 = i3 - 2;
                    this.FR[i4] = b3;
                    bArr2[i2 + i3] = encryptByte(b3, i4);
                    i3++;
                }
            } else {
                if (this.count == 0) {
                    this.cipher.processBlock(this.FR, 0, this.FRE, 0);
                    for (int i5 = 0; i5 < this.blockSize; i5++) {
                        int i6 = i + i5;
                        this.FR[i5] = bArr[i6];
                        bArr2[i5] = encryptByte(bArr[i6], i5);
                    }
                } else if (this.count == this.blockSize) {
                    this.cipher.processBlock(this.FR, 0, this.FRE, 0);
                    byte b4 = bArr[i];
                    byte b5 = bArr[i + 1];
                    bArr2[i2] = encryptByte(b4, 0);
                    bArr2[i2 + 1] = encryptByte(b5, 1);
                    System.arraycopy(this.FR, 2, this.FR, 0, this.blockSize - 2);
                    this.FR[this.blockSize - 2] = b4;
                    this.FR[this.blockSize - 1] = b5;
                    this.cipher.processBlock(this.FR, 0, this.FRE, 0);
                    while (i3 < this.blockSize) {
                        byte b6 = bArr[i + i3];
                        int i7 = i3 - 2;
                        this.FR[i7] = b6;
                        bArr2[i2 + i3] = encryptByte(b6, i7);
                        i3++;
                    }
                }
                this.count += this.blockSize;
            }
            return this.blockSize;
        } else {
            throw new OutputLengthException("output buffer too short");
        }
    }

    private int encryptBlock(byte[] bArr, int i, byte[] bArr2, int i2) throws DataLengthException, IllegalStateException {
        if (this.blockSize + i > bArr.length) {
            throw new DataLengthException("input buffer too short");
        } else if (this.blockSize + i2 <= bArr2.length) {
            int i3 = 2;
            if (this.count > this.blockSize) {
                byte encryptByte = encryptByte(bArr[i], this.blockSize - 2);
                bArr2[i2] = encryptByte;
                this.FR[this.blockSize - 2] = encryptByte;
                byte encryptByte2 = encryptByte(bArr[i + 1], this.blockSize - 1);
                bArr2[i2 + 1] = encryptByte2;
                this.FR[this.blockSize - 1] = encryptByte2;
                this.cipher.processBlock(this.FR, 0, this.FRE, 0);
                while (i3 < this.blockSize) {
                    byte[] bArr3 = this.FR;
                    int i4 = i3 - 2;
                    byte encryptByte3 = encryptByte(bArr[i + i3], i4);
                    bArr2[i2 + i3] = encryptByte3;
                    bArr3[i4] = encryptByte3;
                    i3++;
                }
            } else {
                if (this.count == 0) {
                    this.cipher.processBlock(this.FR, 0, this.FRE, 0);
                    for (int i5 = 0; i5 < this.blockSize; i5++) {
                        byte[] bArr4 = this.FR;
                        byte encryptByte4 = encryptByte(bArr[i + i5], i5);
                        bArr2[i2 + i5] = encryptByte4;
                        bArr4[i5] = encryptByte4;
                    }
                } else if (this.count == this.blockSize) {
                    this.cipher.processBlock(this.FR, 0, this.FRE, 0);
                    bArr2[i2] = encryptByte(bArr[i], 0);
                    bArr2[i2 + 1] = encryptByte(bArr[i + 1], 1);
                    System.arraycopy(this.FR, 2, this.FR, 0, this.blockSize - 2);
                    System.arraycopy(bArr2, i2, this.FR, this.blockSize - 2, 2);
                    this.cipher.processBlock(this.FR, 0, this.FRE, 0);
                    while (i3 < this.blockSize) {
                        byte[] bArr5 = this.FR;
                        int i6 = i3 - 2;
                        byte encryptByte5 = encryptByte(bArr[i + i3], i6);
                        bArr2[i2 + i3] = encryptByte5;
                        bArr5[i6] = encryptByte5;
                        i3++;
                    }
                }
                this.count += this.blockSize;
            }
            return this.blockSize;
        } else {
            throw new OutputLengthException("output buffer too short");
        }
    }

    private byte encryptByte(byte b, int i) {
        return (byte) (b ^ this.FRE[i]);
    }

    public String getAlgorithmName() {
        return this.cipher.getAlgorithmName() + "/OpenPGPCFB";
    }

    public int getBlockSize() {
        return this.cipher.getBlockSize();
    }

    public BlockCipher getUnderlyingCipher() {
        return this.cipher;
    }

    public void init(boolean z, CipherParameters cipherParameters) throws IllegalArgumentException {
        this.forEncryption = z;
        reset();
        this.cipher.init(true, cipherParameters);
    }

    public int processBlock(byte[] bArr, int i, byte[] bArr2, int i2) throws DataLengthException, IllegalStateException {
        return this.forEncryption ? encryptBlock(bArr, i, bArr2, i2) : decryptBlock(bArr, i, bArr2, i2);
    }

    public void reset() {
        this.count = 0;
        System.arraycopy(this.IV, 0, this.FR, 0, this.FR.length);
        this.cipher.reset();
    }
}
