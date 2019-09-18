package com.android.org.bouncycastle.crypto;

import java.security.SecureRandom;

public class KeyGenerationParameters {
    private SecureRandom random;
    private int strength;

    public KeyGenerationParameters(SecureRandom random2, int strength2) {
        this.random = random2;
        this.strength = strength2;
    }

    public SecureRandom getRandom() {
        return this.random;
    }

    public int getStrength() {
        return this.strength;
    }
}
