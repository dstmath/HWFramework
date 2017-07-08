package com.android.org.bouncycastle.crypto;

import java.security.SecureRandom;

public class CipherKeyGenerator {
    protected SecureRandom random;
    protected int strength;

    public void init(KeyGenerationParameters param) {
        this.random = param.getRandom();
        this.strength = (param.getStrength() + 7) / 8;
    }

    public byte[] generateKey() {
        byte[] key = new byte[this.strength];
        this.random.nextBytes(key);
        return key;
    }
}
