package org.bouncycastle.crypto;

import java.security.SecureRandom;

public class KeyGenerationParameters {
    private SecureRandom random;
    private int strength;

    public KeyGenerationParameters(SecureRandom secureRandom, int i) {
        this.random = secureRandom;
        this.strength = i;
    }

    public SecureRandom getRandom() {
        return this.random;
    }

    public int getStrength() {
        return this.strength;
    }
}
