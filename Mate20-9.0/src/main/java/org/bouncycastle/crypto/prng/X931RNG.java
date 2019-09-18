package org.bouncycastle.crypto.prng;

import org.bouncycastle.crypto.BlockCipher;

public class X931RNG {
    private static final int BLOCK128_MAX_BITS_REQUEST = 262144;
    private static final long BLOCK128_RESEED_MAX = 8388608;
    private static final int BLOCK64_MAX_BITS_REQUEST = 4096;
    private static final long BLOCK64_RESEED_MAX = 32768;
    private final byte[] DT;
    private final byte[] I;
    private final byte[] R;
    private byte[] V;
    private final BlockCipher engine;
    private final EntropySource entropySource;
    private long reseedCounter = 1;

    public X931RNG(BlockCipher blockCipher, byte[] bArr, EntropySource entropySource2) {
        this.engine = blockCipher;
        this.entropySource = entropySource2;
        this.DT = new byte[blockCipher.getBlockSize()];
        System.arraycopy(bArr, 0, this.DT, 0, this.DT.length);
        this.I = new byte[blockCipher.getBlockSize()];
        this.R = new byte[blockCipher.getBlockSize()];
    }

    private void increment(byte[] bArr) {
        int length = bArr.length - 1;
        while (length >= 0) {
            byte b = (byte) (bArr[length] + 1);
            bArr[length] = b;
            if (b == 0) {
                length--;
            } else {
                return;
            }
        }
    }

    private static boolean isTooLarge(byte[] bArr, int i) {
        return bArr != null && bArr.length > i;
    }

    private void process(byte[] bArr, byte[] bArr2, byte[] bArr3) {
        for (int i = 0; i != bArr.length; i++) {
            bArr[i] = (byte) (bArr2[i] ^ bArr3[i]);
        }
        this.engine.processBlock(bArr, 0, bArr, 0);
    }

    /* access modifiers changed from: package-private */
    public int generate(byte[] bArr, boolean z) {
        if (this.R.length == 8) {
            if (this.reseedCounter > BLOCK64_RESEED_MAX) {
                return -1;
            }
            if (isTooLarge(bArr, 512)) {
                throw new IllegalArgumentException("Number of bits per request limited to 4096");
            }
        } else if (this.reseedCounter > BLOCK128_RESEED_MAX) {
            return -1;
        } else {
            if (isTooLarge(bArr, 32768)) {
                throw new IllegalArgumentException("Number of bits per request limited to 262144");
            }
        }
        if (z || this.V == null) {
            this.V = this.entropySource.getEntropy();
            if (this.V.length != this.engine.getBlockSize()) {
                throw new IllegalStateException("Insufficient entropy returned");
            }
        }
        int length = bArr.length / this.R.length;
        for (int i = 0; i < length; i++) {
            this.engine.processBlock(this.DT, 0, this.I, 0);
            process(this.R, this.I, this.V);
            process(this.V, this.R, this.I);
            System.arraycopy(this.R, 0, bArr, this.R.length * i, this.R.length);
            increment(this.DT);
        }
        int length2 = bArr.length - (this.R.length * length);
        if (length2 > 0) {
            this.engine.processBlock(this.DT, 0, this.I, 0);
            process(this.R, this.I, this.V);
            process(this.V, this.R, this.I);
            System.arraycopy(this.R, 0, bArr, length * this.R.length, length2);
            increment(this.DT);
        }
        this.reseedCounter++;
        return bArr.length;
    }

    /* access modifiers changed from: package-private */
    public EntropySource getEntropySource() {
        return this.entropySource;
    }

    /* access modifiers changed from: package-private */
    public void reseed() {
        this.V = this.entropySource.getEntropy();
        if (this.V.length == this.engine.getBlockSize()) {
            this.reseedCounter = 1;
            return;
        }
        throw new IllegalStateException("Insufficient entropy returned");
    }
}
