package org.bouncycastle.crypto.modes;

import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.BufferedBlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.OutputLengthException;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bouncycastle.util.Pack;

public class KXTSBlockCipher extends BufferedBlockCipher {
    private static final long RED_POLY_128 = 135;
    private static final long RED_POLY_256 = 1061;
    private static final long RED_POLY_512 = 293;
    private final int blockSize;
    private int counter = -1;
    private final long reductionPolynomial = getReductionPolynomial(this.blockSize);
    private final long[] tw_current = new long[(this.blockSize >>> 3)];
    private final long[] tw_init = new long[(this.blockSize >>> 3)];

    public KXTSBlockCipher(BlockCipher blockCipher) {
        this.cipher = blockCipher;
        this.blockSize = blockCipher.getBlockSize();
    }

    private static void GF_double(long j, long[] jArr) {
        long j2 = 0;
        int i = 0;
        while (i < jArr.length) {
            long j3 = jArr[i];
            jArr[i] = j2 ^ (j3 << 1);
            i++;
            j2 = j3 >>> 63;
        }
        jArr[0] = (j & (-j2)) ^ jArr[0];
    }

    protected static long getReductionPolynomial(int i) {
        if (i == 16) {
            return RED_POLY_128;
        }
        if (i == 32) {
            return RED_POLY_256;
        }
        if (i == 64) {
            return RED_POLY_512;
        }
        throw new IllegalArgumentException("Only 128, 256, and 512 -bit block sizes supported");
    }

    private void processBlock(byte[] bArr, int i, byte[] bArr2, int i2) {
        if (this.counter != -1) {
            this.counter++;
            GF_double(this.reductionPolynomial, this.tw_current);
            byte[] bArr3 = new byte[this.blockSize];
            Pack.longToLittleEndian(this.tw_current, bArr3, 0);
            byte[] bArr4 = new byte[this.blockSize];
            System.arraycopy(bArr3, 0, bArr4, 0, this.blockSize);
            for (int i3 = 0; i3 < this.blockSize; i3++) {
                bArr4[i3] = (byte) (bArr4[i3] ^ bArr[i + i3]);
            }
            this.cipher.processBlock(bArr4, 0, bArr4, 0);
            for (int i4 = 0; i4 < this.blockSize; i4++) {
                bArr2[i2 + i4] = (byte) (bArr4[i4] ^ bArr3[i4]);
            }
            return;
        }
        throw new IllegalStateException("Attempt to process too many blocks");
    }

    public int doFinal(byte[] bArr, int i) {
        reset();
        return 0;
    }

    public int getOutputSize(int i) {
        return i;
    }

    public int getUpdateOutputSize(int i) {
        return i;
    }

    public void init(boolean z, CipherParameters cipherParameters) {
        if (cipherParameters instanceof ParametersWithIV) {
            ParametersWithIV parametersWithIV = (ParametersWithIV) cipherParameters;
            CipherParameters parameters = parametersWithIV.getParameters();
            byte[] iv = parametersWithIV.getIV();
            if (iv.length == this.blockSize) {
                byte[] bArr = new byte[this.blockSize];
                System.arraycopy(iv, 0, bArr, 0, this.blockSize);
                this.cipher.init(true, parameters);
                this.cipher.processBlock(bArr, 0, bArr, 0);
                this.cipher.init(z, parameters);
                Pack.littleEndianToLong(bArr, 0, this.tw_init);
                System.arraycopy(this.tw_init, 0, this.tw_current, 0, this.tw_init.length);
                this.counter = 0;
                return;
            }
            throw new IllegalArgumentException("Currently only support IVs of exactly one block");
        }
        throw new IllegalArgumentException("Invalid parameters passed");
    }

    public int processByte(byte b, byte[] bArr, int i) {
        throw new IllegalStateException("unsupported operation");
    }

    public int processBytes(byte[] bArr, int i, int i2, byte[] bArr2, int i3) {
        if (bArr.length - i < i2) {
            throw new DataLengthException("Input buffer too short");
        } else if (bArr2.length - i < i2) {
            throw new OutputLengthException("Output buffer too short");
        } else if (i2 % this.blockSize == 0) {
            int i4 = 0;
            while (i4 < i2) {
                processBlock(bArr, i + i4, bArr2, i3 + i4);
                i4 += this.blockSize;
            }
            return i2;
        } else {
            throw new IllegalArgumentException("Partial blocks not supported");
        }
    }

    public void reset() {
        this.cipher.reset();
        System.arraycopy(this.tw_init, 0, this.tw_current, 0, this.tw_init.length);
        this.counter = 0;
    }
}
