package com.android.org.bouncycastle.crypto.engines;

import com.android.org.bouncycastle.crypto.BlockCipher;
import com.android.org.bouncycastle.crypto.CipherParameters;
import com.android.org.bouncycastle.crypto.DataLengthException;
import com.android.org.bouncycastle.crypto.InvalidCipherTextException;
import com.android.org.bouncycastle.crypto.Wrapper;
import com.android.org.bouncycastle.crypto.params.KeyParameter;
import com.android.org.bouncycastle.crypto.params.ParametersWithIV;
import com.android.org.bouncycastle.crypto.params.ParametersWithRandom;
import com.android.org.bouncycastle.util.Arrays;

public class RFC3394WrapEngine implements Wrapper {
    private BlockCipher engine;
    private boolean forWrapping;
    private byte[] iv;
    private KeyParameter param;
    private boolean wrapCipherMode;

    public RFC3394WrapEngine(BlockCipher engine2) {
        this(engine2, false);
    }

    public RFC3394WrapEngine(BlockCipher engine2, boolean useReverseDirection) {
        this.iv = new byte[]{-90, -90, -90, -90, -90, -90, -90, -90};
        this.engine = engine2;
        this.wrapCipherMode = !useReverseDirection;
    }

    public void init(boolean forWrapping2, CipherParameters param2) {
        this.forWrapping = forWrapping2;
        if (param2 instanceof ParametersWithRandom) {
            param2 = ((ParametersWithRandom) param2).getParameters();
        }
        if (param2 instanceof KeyParameter) {
            this.param = (KeyParameter) param2;
        } else if (param2 instanceof ParametersWithIV) {
            this.iv = ((ParametersWithIV) param2).getIV();
            this.param = (KeyParameter) ((ParametersWithIV) param2).getParameters();
            if (this.iv.length != 8) {
                throw new IllegalArgumentException("IV not equal to 8");
            }
        }
    }

    public String getAlgorithmName() {
        return this.engine.getAlgorithmName();
    }

    public byte[] wrap(byte[] in, int inOff, int inLen) {
        int i = inLen;
        if (this.forWrapping) {
            int n = i / 8;
            if (n * 8 == i) {
                byte[] block = new byte[(this.iv.length + i)];
                byte[] buf = new byte[(this.iv.length + 8)];
                System.arraycopy(this.iv, 0, block, 0, this.iv.length);
                System.arraycopy(in, inOff, block, this.iv.length, i);
                this.engine.init(this.wrapCipherMode, this.param);
                for (int j = 0; j != 6; j++) {
                    for (int i2 = 1; i2 <= n; i2++) {
                        System.arraycopy(block, 0, buf, 0, this.iv.length);
                        System.arraycopy(block, 8 * i2, buf, this.iv.length, 8);
                        this.engine.processBlock(buf, 0, buf, 0);
                        int t = (n * j) + i2;
                        int k = 1;
                        while (t != 0) {
                            int length = this.iv.length - k;
                            buf[length] = (byte) (buf[length] ^ ((byte) t));
                            t >>>= 8;
                            k++;
                        }
                        System.arraycopy(buf, 0, block, 0, 8);
                        System.arraycopy(buf, 8, block, 8 * i2, 8);
                    }
                }
                return block;
            }
            byte[] bArr = in;
            int i3 = inOff;
            throw new DataLengthException("wrap data must be a multiple of 8 bytes");
        }
        byte[] bArr2 = in;
        int i4 = inOff;
        throw new IllegalStateException("not set for wrapping");
    }

    public byte[] unwrap(byte[] in, int inOff, int inLen) throws InvalidCipherTextException {
        byte[] bArr = in;
        int i = inOff;
        int i2 = inLen;
        if (!this.forWrapping) {
            int n = i2 / 8;
            if (n * 8 == i2) {
                byte[] block = new byte[(i2 - this.iv.length)];
                byte[] a = new byte[this.iv.length];
                int i3 = 8;
                byte[] buf = new byte[(this.iv.length + 8)];
                System.arraycopy(bArr, i, a, 0, this.iv.length);
                System.arraycopy(bArr, this.iv.length + i, block, 0, i2 - this.iv.length);
                int i4 = 1;
                this.engine.init(!this.wrapCipherMode, this.param);
                int n2 = n - 1;
                int j = 5;
                while (j >= 0) {
                    int i5 = n2;
                    while (i5 >= i4) {
                        System.arraycopy(a, 0, buf, 0, this.iv.length);
                        System.arraycopy(block, (i5 - 1) * i3, buf, this.iv.length, i3);
                        int t = (n2 * j) + i5;
                        int k = i4;
                        while (t != 0) {
                            int length = this.iv.length - k;
                            buf[length] = (byte) (buf[length] ^ ((byte) t));
                            t >>>= 8;
                            k++;
                        }
                        this.engine.processBlock(buf, 0, buf, 0);
                        i3 = 8;
                        System.arraycopy(buf, 0, a, 0, 8);
                        System.arraycopy(buf, 8, block, (i5 - 1) * 8, 8);
                        i5--;
                        i4 = 1;
                    }
                    j--;
                    i4 = 1;
                }
                if (Arrays.constantTimeAreEqual(a, this.iv)) {
                    return block;
                }
                throw new InvalidCipherTextException("checksum failed");
            }
            throw new InvalidCipherTextException("unwrap data must be a multiple of 8 bytes");
        }
        throw new IllegalStateException("not set for unwrapping");
    }
}
