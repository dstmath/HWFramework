package com.android.org.bouncycastle.crypto.modes;

import com.android.org.bouncycastle.crypto.BlockCipher;
import com.android.org.bouncycastle.crypto.CipherParameters;
import com.android.org.bouncycastle.crypto.DataLengthException;
import com.android.org.bouncycastle.crypto.params.ParametersWithIV;
import com.android.org.bouncycastle.util.Arrays;

public class CBCBlockCipher implements BlockCipher {
    private byte[] IV;
    private int blockSize;
    private byte[] cbcNextV;
    private byte[] cbcV;
    private BlockCipher cipher = null;
    private boolean encrypting;

    public CBCBlockCipher(BlockCipher cipher2) {
        this.cipher = cipher2;
        this.blockSize = cipher2.getBlockSize();
        this.IV = new byte[this.blockSize];
        this.cbcV = new byte[this.blockSize];
        this.cbcNextV = new byte[this.blockSize];
    }

    public BlockCipher getUnderlyingCipher() {
        return this.cipher;
    }

    public void init(boolean encrypting2, CipherParameters params) throws IllegalArgumentException {
        boolean oldEncrypting = this.encrypting;
        this.encrypting = encrypting2;
        if (params instanceof ParametersWithIV) {
            ParametersWithIV ivParam = (ParametersWithIV) params;
            byte[] iv = ivParam.getIV();
            if (iv.length == this.blockSize) {
                System.arraycopy(iv, 0, this.IV, 0, iv.length);
                reset();
                if (ivParam.getParameters() != null) {
                    this.cipher.init(encrypting2, ivParam.getParameters());
                } else if (oldEncrypting != encrypting2) {
                    throw new IllegalArgumentException("cannot change encrypting state without providing key.");
                }
            } else {
                throw new IllegalArgumentException("initialisation vector must be the same length as block size");
            }
        } else {
            reset();
            if (params != null) {
                this.cipher.init(encrypting2, params);
            } else if (oldEncrypting != encrypting2) {
                throw new IllegalArgumentException("cannot change encrypting state without providing key.");
            }
        }
    }

    public String getAlgorithmName() {
        return this.cipher.getAlgorithmName() + "/CBC";
    }

    public int getBlockSize() {
        return this.cipher.getBlockSize();
    }

    public int processBlock(byte[] in, int inOff, byte[] out, int outOff) throws DataLengthException, IllegalStateException {
        return this.encrypting ? encryptBlock(in, inOff, out, outOff) : decryptBlock(in, inOff, out, outOff);
    }

    public void reset() {
        System.arraycopy(this.IV, 0, this.cbcV, 0, this.IV.length);
        Arrays.fill(this.cbcNextV, (byte) 0);
        this.cipher.reset();
    }

    private int encryptBlock(byte[] in, int inOff, byte[] out, int outOff) throws DataLengthException, IllegalStateException {
        if (this.blockSize + inOff <= in.length) {
            for (int i = 0; i < this.blockSize; i++) {
                byte[] bArr = this.cbcV;
                bArr[i] = (byte) (bArr[i] ^ in[inOff + i]);
            }
            int length = this.cipher.processBlock(this.cbcV, 0, out, outOff);
            System.arraycopy(out, outOff, this.cbcV, 0, this.cbcV.length);
            return length;
        }
        throw new DataLengthException("input buffer too short");
    }

    private int decryptBlock(byte[] in, int inOff, byte[] out, int outOff) throws DataLengthException, IllegalStateException {
        if (this.blockSize + inOff <= in.length) {
            int i = 0;
            System.arraycopy(in, inOff, this.cbcNextV, 0, this.blockSize);
            int length = this.cipher.processBlock(in, inOff, out, outOff);
            while (true) {
                int i2 = i;
                if (i2 < this.blockSize) {
                    int i3 = outOff + i2;
                    out[i3] = (byte) (out[i3] ^ this.cbcV[i2]);
                    i = i2 + 1;
                } else {
                    byte[] tmp = this.cbcV;
                    this.cbcV = this.cbcNextV;
                    this.cbcNextV = tmp;
                    return length;
                }
            }
        } else {
            throw new DataLengthException("input buffer too short");
        }
    }
}
