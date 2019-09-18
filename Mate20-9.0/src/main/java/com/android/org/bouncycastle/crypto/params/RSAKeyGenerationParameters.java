package com.android.org.bouncycastle.crypto.params;

import com.android.org.bouncycastle.crypto.KeyGenerationParameters;
import java.math.BigInteger;
import java.security.SecureRandom;

public class RSAKeyGenerationParameters extends KeyGenerationParameters {
    private int certainty;
    private BigInteger publicExponent;

    public RSAKeyGenerationParameters(BigInteger publicExponent2, SecureRandom random, int strength, int certainty2) {
        super(random, strength);
        if (strength < 12) {
            throw new IllegalArgumentException("key strength too small");
        } else if (publicExponent2.testBit(0)) {
            this.publicExponent = publicExponent2;
            this.certainty = certainty2;
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
