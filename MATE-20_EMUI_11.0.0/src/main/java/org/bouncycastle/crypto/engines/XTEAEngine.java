package org.bouncycastle.crypto.engines;

import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.OutputLengthException;
import org.bouncycastle.crypto.params.KeyParameter;

public class XTEAEngine implements BlockCipher {
    private static final int block_size = 8;
    private static final int delta = -1640531527;
    private static final int rounds = 32;
    private int[] _S = new int[4];
    private boolean _forEncryption;
    private boolean _initialised = false;
    private int[] _sum0 = new int[32];
    private int[] _sum1 = new int[32];

    private int bytesToInt(byte[] bArr, int i) {
        int i2 = i + 1;
        int i3 = i2 + 1;
        int i4 = (bArr[i] << 24) | ((bArr[i2] & 255) << 16);
        return (bArr[i3 + 1] & 255) | i4 | ((bArr[i3] & 255) << 8);
    }

    private int decryptBlock(byte[] bArr, int i, byte[] bArr2, int i2) {
        int bytesToInt = bytesToInt(bArr, i);
        int bytesToInt2 = bytesToInt(bArr, i + 4);
        for (int i3 = 31; i3 >= 0; i3--) {
            bytesToInt2 -= (((bytesToInt << 4) ^ (bytesToInt >>> 5)) + bytesToInt) ^ this._sum1[i3];
            bytesToInt -= (((bytesToInt2 << 4) ^ (bytesToInt2 >>> 5)) + bytesToInt2) ^ this._sum0[i3];
        }
        unpackInt(bytesToInt, bArr2, i2);
        unpackInt(bytesToInt2, bArr2, i2 + 4);
        return 8;
    }

    private int encryptBlock(byte[] bArr, int i, byte[] bArr2, int i2) {
        int bytesToInt = bytesToInt(bArr, i);
        int bytesToInt2 = bytesToInt(bArr, i + 4);
        for (int i3 = 0; i3 < 32; i3++) {
            bytesToInt += (((bytesToInt2 << 4) ^ (bytesToInt2 >>> 5)) + bytesToInt2) ^ this._sum0[i3];
            bytesToInt2 += (((bytesToInt << 4) ^ (bytesToInt >>> 5)) + bytesToInt) ^ this._sum1[i3];
        }
        unpackInt(bytesToInt, bArr2, i2);
        unpackInt(bytesToInt2, bArr2, i2 + 4);
        return 8;
    }

    private void setKey(byte[] bArr) {
        if (bArr.length == 16) {
            int i = 0;
            int i2 = 0;
            while (i < 4) {
                this._S[i] = bytesToInt(bArr, i2);
                i++;
                i2 += 4;
            }
            int i3 = 0;
            for (int i4 = 0; i4 < 32; i4++) {
                int[] iArr = this._sum0;
                int[] iArr2 = this._S;
                iArr[i4] = iArr2[i3 & 3] + i3;
                i3 -= 1640531527;
                this._sum1[i4] = iArr2[(i3 >>> 11) & 3] + i3;
            }
            return;
        }
        throw new IllegalArgumentException("Key size must be 128 bits.");
    }

    private void unpackInt(int i, byte[] bArr, int i2) {
        int i3 = i2 + 1;
        bArr[i2] = (byte) (i >>> 24);
        int i4 = i3 + 1;
        bArr[i3] = (byte) (i >>> 16);
        bArr[i4] = (byte) (i >>> 8);
        bArr[i4 + 1] = (byte) i;
    }

    @Override // org.bouncycastle.crypto.BlockCipher
    public String getAlgorithmName() {
        return "XTEA";
    }

    @Override // org.bouncycastle.crypto.BlockCipher
    public int getBlockSize() {
        return 8;
    }

    @Override // org.bouncycastle.crypto.BlockCipher
    public void init(boolean z, CipherParameters cipherParameters) {
        if (cipherParameters instanceof KeyParameter) {
            this._forEncryption = z;
            this._initialised = true;
            setKey(((KeyParameter) cipherParameters).getKey());
            return;
        }
        throw new IllegalArgumentException("invalid parameter passed to TEA init - " + cipherParameters.getClass().getName());
    }

    @Override // org.bouncycastle.crypto.BlockCipher
    public int processBlock(byte[] bArr, int i, byte[] bArr2, int i2) {
        if (!this._initialised) {
            throw new IllegalStateException(getAlgorithmName() + " not initialised");
        } else if (i + 8 > bArr.length) {
            throw new DataLengthException("input buffer too short");
        } else if (i2 + 8 <= bArr2.length) {
            return this._forEncryption ? encryptBlock(bArr, i, bArr2, i2) : decryptBlock(bArr, i, bArr2, i2);
        } else {
            throw new OutputLengthException("output buffer too short");
        }
    }

    @Override // org.bouncycastle.crypto.BlockCipher
    public void reset() {
    }
}
