package org.bouncycastle.crypto.prng;

import java.security.SecureRandom;
import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.CryptoServicesRegistrar;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.Pack;

public class X931SecureRandomBuilder {
    private byte[] dateTimeVector;
    private EntropySourceProvider entropySourceProvider;
    private SecureRandom random;

    public X931SecureRandomBuilder() {
        this(CryptoServicesRegistrar.getSecureRandom(), false);
    }

    public X931SecureRandomBuilder(SecureRandom secureRandom, boolean z) {
        this.random = secureRandom;
        this.entropySourceProvider = new BasicEntropySourceProvider(this.random, z);
    }

    public X931SecureRandomBuilder(EntropySourceProvider entropySourceProvider2) {
        this.random = null;
        this.entropySourceProvider = entropySourceProvider2;
    }

    public X931SecureRandom build(BlockCipher blockCipher, KeyParameter keyParameter, boolean z) {
        if (this.dateTimeVector == null) {
            this.dateTimeVector = new byte[blockCipher.getBlockSize()];
            Pack.longToBigEndian(System.currentTimeMillis(), this.dateTimeVector, 0);
        }
        blockCipher.init(true, keyParameter);
        return new X931SecureRandom(this.random, new X931RNG(blockCipher, this.dateTimeVector, this.entropySourceProvider.get(blockCipher.getBlockSize() * 8)), z);
    }

    public X931SecureRandomBuilder setDateTimeVector(byte[] bArr) {
        this.dateTimeVector = Arrays.clone(bArr);
        return this;
    }
}
