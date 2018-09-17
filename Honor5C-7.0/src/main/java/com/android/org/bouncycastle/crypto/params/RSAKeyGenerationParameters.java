package com.android.org.bouncycastle.crypto.params;

import com.android.org.bouncycastle.crypto.KeyGenerationParameters;
import java.math.BigInteger;
import java.security.SecureRandom;

public class RSAKeyGenerationParameters extends KeyGenerationParameters {
    private int certainty;
    private BigInteger publicExponent;

    public RSAKeyGenerationParameters(BigInteger publicExponent, SecureRandom random, int strength, int certainty) {
        super(random, strength);
        if (strength < 12) {
            throw new IllegalArgumentException("key strength too small");
        } else if (publicExponent.testBit(0)) {
            this.publicExponent = publicExponent;
            this.certainty = certainty;
        } else {
            throw new IllegalArgumentException("public exponent cannot be even");
        }
    }

    public BigInteger getPublicExponent() {
        return this.publicExponent;
    }

    public int getCertainty() {
        return this.certainty;
    }
}
