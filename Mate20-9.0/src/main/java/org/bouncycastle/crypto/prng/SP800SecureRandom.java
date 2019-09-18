package org.bouncycastle.crypto.prng;

import java.security.SecureRandom;
import org.bouncycastle.crypto.prng.drbg.SP80090DRBG;

public class SP800SecureRandom extends SecureRandom {
    private SP80090DRBG drbg;
    private final DRBGProvider drbgProvider;
    private final EntropySource entropySource;
    private final boolean predictionResistant;
    private final SecureRandom randomSource;

    SP800SecureRandom(SecureRandom secureRandom, EntropySource entropySource2, DRBGProvider dRBGProvider, boolean z) {
        this.randomSource = secureRandom;
        this.entropySource = entropySource2;
        this.drbgProvider = dRBGProvider;
        this.predictionResistant = z;
    }

    public byte[] generateSeed(int i) {
        return EntropyUtil.generateSeed(this.entropySource, i);
    }

    public void nextBytes(byte[] bArr) {
        synchronized (this) {
            if (this.drbg == null) {
                this.drbg = this.drbgProvider.get(this.entropySource);
            }
            if (this.drbg.generate(bArr, null, this.predictionResistant) < 0) {
                this.drbg.reseed(null);
                this.drbg.generate(bArr, null, this.predictionResistant);
            }
        }
    }

    public void reseed(byte[] bArr) {
        synchronized (this) {
            if (this.drbg == null) {
                this.drbg = this.drbgProvider.get(this.entropySource);
            }
            this.drbg.reseed(bArr);
        }
    }

    public void setSeed(long j) {
        synchronized (this) {
            if (this.randomSource != null) {
                this.randomSource.setSeed(j);
            }
        }
    }

    public void setSeed(byte[] bArr) {
        synchronized (this) {
            if (this.randomSource != null) {
                this.randomSource.setSeed(bArr);
            }
        }
    }
}
