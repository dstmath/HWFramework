package org.bouncycastle.crypto.modes;

import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.StreamBlockCipher;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bouncycastle.util.Arrays;

public class CFBBlockCipher extends StreamBlockCipher {
    private byte[] IV;
    private int blockSize;
    private int byteCount;
    private byte[] cfbOutV;
    private byte[] cfbV;
    private BlockCipher cipher = null;
    private boolean encrypting;
    private byte[] inBuf;

    public CFBBlockCipher(BlockCipher blockCipher, int i) {
        super(blockCipher);
        if (i < 8 || i % 8 != 0) {
            throw new IllegalArgumentException("CFB" + i + " not supported");
        }
        this.cipher = blockCipher;
        this.blockSize = i / 8;
        this.IV = new byte[blockCipher.getBlockSize()];
        this.cfbV = new byte[blockCipher.getBlockSize()];
        this.cfbOutV = new byte[blockCipher.getBlockSize()];
        this.inBuf = new byte[this.blockSize];
    }

    private byte decryptByte(byte b) {
        if (this.byteCount == 0) {
            this.cipher.processBlock(this.cfbV, 0, this.cfbOutV, 0);
        }
        byte[] bArr = this.inBuf;
        int i = this.byteCount;
        bArr[i] = b;
        byte[] bArr2 = this.cfbOutV;
        this.byteCount = i + 1;
        byte b2 = (byte) (b ^ bArr2[i]);
        int i2 = this.byteCount;
        int i3 = this.blockSize;
        if (i2 == i3) {
            this.byteCount = 0;
            byte[] bArr3 = this.cfbV;
            System.arraycopy(bArr3, i3, bArr3, 0, bArr3.length - i3);
            byte[] bArr4 = this.inBuf;
            byte[] bArr5 = this.cfbV;
            int length = bArr5.length;
            int i4 = this.blockSize;
            System.arraycopy(bArr4, 0, bArr5, length - i4, i4);
        }
        return b2;
    }

    private byte encryptByte(byte b) {
        if (this.byteCount == 0) {
            this.cipher.processBlock(this.cfbV, 0, this.cfbOutV, 0);
        }
        byte[] bArr = this.cfbOutV;
        int i = this.byteCount;
        byte b2 = (byte) (b ^ bArr[i]);
        byte[] bArr2 = this.inBuf;
        this.byteCount = i + 1;
        bArr2[i] = b2;
        int i2 = this.byteCount;
        int i3 = this.blockSize;
        if (i2 == i3) {
            this.byteCount = 0;
            byte[] bArr3 = this.cfbV;
            System.arraycopy(bArr3, i3, bArr3, 0, bArr3.length - i3);
            byte[] bArr4 = this.inBuf;
            byte[] bArr5 = this.cfbV;
            int length = bArr5.length;
            int i4 = this.blockSize;
            System.arraycopy(bArr4, 0, bArr5, length - i4, i4);
        }
        return b2;
    }

    /* access modifiers changed from: protected */
    @Override // org.bouncycastle.crypto.StreamBlockCipher
    public byte calculateByte(byte b) throws DataLengthException, IllegalStateException {
        return this.encrypting ? encryptByte(b) : decryptByte(b);
    }

    public int decryptBlock(byte[] bArr, int i, byte[] bArr2, int i2) throws DataLengthException, IllegalStateException {
        processBytes(bArr, i, this.blockSize, bArr2, i2);
        return this.blockSize;
    }

    public int encryptBlock(byte[] bArr, int i, byte[] bArr2, int i2) throws DataLengthException, IllegalStateException {
        processBytes(bArr, i, this.blockSize, bArr2, i2);
        return this.blockSize;
    }

    @Override // org.bouncycastle.crypto.BlockCipher
    public String getAlgorithmName() {
        return this.cipher.getAlgorithmName() + "/CFB" + (this.blockSize * 8);
    }

    @Override // org.bouncycastle.crypto.BlockCipher
    public int getBlockSize() {
        return this.blockSize;
    }

    public byte[] getCurrentIV() {
        return Arrays.clone(this.cfbV);
    }

    @Override // org.bouncycastle.crypto.BlockCipher
    public void init(boolean z, CipherParameters cipherParameters) throws IllegalArgumentException {
        BlockCipher blockCipher;
        this.encrypting = z;
        if (cipherParameters instanceof ParametersWithIV) {
            ParametersWithIV parametersWithIV = (ParametersWithIV) cipherParameters;
            byte[] iv = parametersWithIV.getIV();
            int length = iv.length;
            byte[] bArr = this.IV;
            if (length < bArr.length) {
                System.arraycopy(iv, 0, bArr, bArr.length - iv.length, iv.length);
                int i = 0;
                while (true) {
                    byte[] bArr2 = this.IV;
                    if (i >= bArr2.length - iv.length) {
                        break;
                    }
                    bArr2[i] = 0;
                    i++;
                }
            } else {
                System.arraycopy(iv, 0, bArr, 0, bArr.length);
            }
            reset();
            if (parametersWithIV.getParameters() != null) {
                blockCipher = this.cipher;
                cipherParameters = parametersWithIV.getParameters();
            } else {
                return;
            }
        } else {
            reset();
            if (cipherParameters != null) {
                blockCipher = this.cipher;
            } else {
                return;
            }
        }
        blockCipher.init(true, cipherParameters);
    }

    @Override // org.bouncycastle.crypto.BlockCipher
    public int processBlock(byte[] bArr, int i, byte[] bArr2, int i2) throws DataLengthException, IllegalStateException {
        processBytes(bArr, i, this.blockSize, bArr2, i2);
        return this.blockSize;
    }

    @Override // org.bouncycastle.crypto.BlockCipher
    public void reset() {
        byte[] bArr = this.IV;
        System.arraycopy(bArr, 0, this.cfbV, 0, bArr.length);
        Arrays.fill(this.inBuf, (byte) 0);
        this.byteCount = 0;
        this.cipher.reset();
    }
}
