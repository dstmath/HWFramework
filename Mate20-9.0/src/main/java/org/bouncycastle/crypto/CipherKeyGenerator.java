package org.bouncycastle.crypto;

import java.security.SecureRandom;

public class CipherKeyGenerator {
    protected SecureRandom random;
    protected int strength;

    public byte[] generateKey() {
        byte[] bArr = new byte[this.strength];
        this.random.nextBytes(bArr);
        return bArr;
    }

    public void init(KeyGenerationParameters keyGenerationParameters) {
        this.random = keyGenerationParameters.getRandom();
        this.strength = (keyGenerationParameters.getStrength() + 7) / 8;
    }
}
