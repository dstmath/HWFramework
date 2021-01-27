package org.bouncycastle.crypto.modes;

import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bouncycastle.util.Arrays;

public class CBCBlockCipher implements BlockCipher {
    private byte[] IV;
    private int blockSize;
    private byte[] cbcNextV;
    private byte[] cbcV;
    private BlockCipher cipher = null;
    private boolean encrypting;

    public CBCBlockCipher(BlockCipher blockCipher) {
        this.cipher = blockCipher;
        this.blockSize = blockCipher.getBlockSize();
        int i = this.blockSize;
        this.IV = new byte[i];
        this.cbcV = new byte[i];
        this.cbcNextV = new byte[i];
    }

    private int decryptBlock(byte[] bArr, int i, byte[] bArr2, int i2) throws DataLengthException, IllegalStateException {
        int i3 = this.blockSize;
        if (i + i3 <= bArr.length) {
            System.arraycopy(bArr, i, this.cbcNextV, 0, i3);
            int processBlock = this.cipher.processBlock(bArr, i, bArr2, i2);
            for (int i4 = 0; i4 < this.blockSize; i4++) {
                int i5 = i2 + i4;
                bArr2[i5] = (byte) (bArr2[i5] ^ this.cbcV[i4]);
            }
            byte[] bArr3 = this.cbcV;
            this.cbcV = this.cbcNextV;
            this.cbcNextV = bArr3;
            return processBlock;
        }
        throw new DataLengthException("input buffer too short");
    }

    private int encryptBlock(byte[] bArr, int i, byte[] bArr2, int i2) throws DataLengthException, IllegalStateException {
        if (this.blockSize + i <= bArr.length) {
            for (int i3 = 0; i3 < this.blockSize; i3++) {
                byte[] bArr3 = this.cbcV;
                bArr3[i3] = (byte) (bArr3[i3] ^ bArr[i + i3]);
            }
            int processBlock = this.cipher.processBlock(this.cbcV, 0, bArr2, i2);
            byte[] bArr4 = this.cbcV;
            System.arraycopy(bArr2, i2, bArr4, 0, bArr4.length);
            return processBlock;
        }
        throw new DataLengthException("input buffer too short");
    }

    @Override // org.bouncycastle.crypto.BlockCipher
    public String getAlgorithmName() {
        return this.cipher.getAlgorithmName() + "/CBC";
    }

    @Override // org.bouncycastle.crypto.BlockCipher
    public int getBlockSize() {
        return this.cipher.getBlockSize();
    }

    public BlockCipher getUnderlyingCipher() {
        return this.cipher;
    }

    @Override // org.bouncycastle.crypto.BlockCipher
    public void init(boolean z, CipherParameters cipherParameters) throws IllegalArgumentException {
        BlockCipher blockCipher;
        boolean z2 = this.encrypting;
        this.encrypting = z;
        if (cipherParameters instanceof ParametersWithIV) {
            ParametersWithIV parametersWithIV = (ParametersWithIV) cipherParameters;
            byte[] iv = parametersWithIV.getIV();
            if (iv.length == this.blockSize) {
                System.arraycopy(iv, 0, this.IV, 0, iv.length);
                reset();
                if (parametersWithIV.getParameters() != null) {
                    blockCipher = this.cipher;
                    cipherParameters = parametersWithIV.getParameters();
                } else if (z2 != z) {
                    throw new IllegalArgumentException("cannot change encrypting state without providing key.");
                } else {
                    return;
                }
            } else {
                throw new IllegalArgumentException("initialisation vector must be the same length as block size");
            }
        } else {
            reset();
            if (cipherParameters != null) {
                blockCipher = this.cipher;
            } else if (z2 != z) {
                throw new IllegalArgumentException("cannot change encrypting state without providing key.");
            } else {
                return;
            }
        }
        blockCipher.init(z, cipherParameters);
    }

    @Override // org.bouncycastle.crypto.BlockCipher
    public int processBlock(byte[] bArr, int i, byte[] bArr2, int i2) throws DataLengthException, IllegalStateException {
        return this.encrypting ? encryptBlock(bArr, i, bArr2, i2) : decryptBlock(bArr, i, bArr2, i2);
    }

    @Override // org.bouncycastle.crypto.BlockCipher
    public void reset() {
        byte[] bArr = this.IV;
        System.arraycopy(bArr, 0, this.cbcV, 0, bArr.length);
        Arrays.fill(this.cbcNextV, (byte) 0);
        this.cipher.reset();
    }
}
