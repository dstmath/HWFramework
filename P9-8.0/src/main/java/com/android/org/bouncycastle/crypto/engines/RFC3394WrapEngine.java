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

    public RFC3394WrapEngine(BlockCipher engine) {
        this(engine, false);
    }

    public RFC3394WrapEngine(BlockCipher engine, boolean useReverseDirection) {
        this.iv = new byte[]{(byte) -90, (byte) -90, (byte) -90, (byte) -90, (byte) -90, (byte) -90, (byte) -90, (byte) -90};
        this.engine = engine;
        this.wrapCipherMode = !useReverseDirection;
    }

    public void init(boolean forWrapping, CipherParameters param) {
        this.forWrapping = forWrapping;
        if (param instanceof ParametersWithRandom) {
            param = ((ParametersWithRandom) param).getParameters();
        }
        if (param instanceof KeyParameter) {
            this.param = (KeyParameter) param;
        } else if (param instanceof ParametersWithIV) {
            this.iv = ((ParametersWithIV) param).getIV();
            this.param = (KeyParameter) ((ParametersWithIV) param).getParameters();
            if (this.iv.length != 8) {
                throw new IllegalArgumentException("IV not equal to 8");
            }
        }
    }

    public String getAlgorithmName() {
        return this.engine.getAlgorithmName();
    }

    public byte[] wrap(byte[] in, int inOff, int inLen) {
        if (this.forWrapping) {
            int n = inLen / 8;
            if (n * 8 != inLen) {
                throw new DataLengthException("wrap data must be a multiple of 8 bytes");
            }
            byte[] block = new byte[(this.iv.length + inLen)];
            byte[] buf = new byte[(this.iv.length + 8)];
            System.arraycopy(this.iv, 0, block, 0, this.iv.length);
            System.arraycopy(in, inOff, block, this.iv.length, inLen);
            this.engine.init(this.wrapCipherMode, this.param);
            for (int j = 0; j != 6; j++) {
                for (int i = 1; i <= n; i++) {
                    System.arraycopy(block, 0, buf, 0, this.iv.length);
                    System.arraycopy(block, i * 8, buf, this.iv.length, 8);
                    this.engine.processBlock(buf, 0, buf, 0);
                    int t = (n * j) + i;
                    int k = 1;
                    while (t != 0) {
                        int length = this.iv.length - k;
                        buf[length] = (byte) (buf[length] ^ ((byte) t));
                        t >>>= 8;
                        k++;
                    }
                    System.arraycopy(buf, 0, block, 0, 8);
                    System.arraycopy(buf, 8, block, i * 8, 8);
                }
            }
            return block;
        }
        throw new IllegalStateException("not set for wrapping");
    }

    public byte[] unwrap(byte[] in, int inOff, int inLen) throws InvalidCipherTextException {
        if (this.forWrapping) {
            throw new IllegalStateException("not set for unwrapping");
        }
        int n = inLen / 8;
        if (n * 8 != inLen) {
            throw new InvalidCipherTextException("unwrap data must be a multiple of 8 bytes");
        }
        byte[] block = new byte[(inLen - this.iv.length)];
        byte[] a = new byte[this.iv.length];
        byte[] buf = new byte[(this.iv.length + 8)];
        System.arraycopy(in, inOff, a, 0, this.iv.length);
        System.arraycopy(in, this.iv.length + inOff, block, 0, inLen - this.iv.length);
        this.engine.init(this.wrapCipherMode ^ 1, this.param);
        n--;
        for (int j = 5; j >= 0; j--) {
            for (int i = n; i >= 1; i--) {
                System.arraycopy(a, 0, buf, 0, this.iv.length);
                System.arraycopy(block, (i - 1) * 8, buf, this.iv.length, 8);
                int t = (n * j) + i;
                int k = 1;
                while (t != 0) {
                    int length = this.iv.length - k;
                    buf[length] = (byte) (buf[length] ^ ((byte) t));
                    t >>>= 8;
                    k++;
                }
                this.engine.processBlock(buf, 0, buf, 0);
                System.arraycopy(buf, 0, a, 0, 8);
                System.arraycopy(buf, 8, block, (i - 1) * 8, 8);
            }
        }
        if (Arrays.constantTimeAreEqual(a, this.iv)) {
            return block;
        }
        throw new InvalidCipherTextException("checksum failed");
    }
}
