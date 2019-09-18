package com.android.org.bouncycastle.crypto.params;

import com.android.org.bouncycastle.crypto.KeyGenerationParameters;
import java.security.SecureRandom;

public class DSAKeyGenerationParameters extends KeyGenerationParameters {
    private DSAParameters params;

    public DSAKeyGenerationParameters(SecureRandom random, DSAParameters params2) {
        super(random, params2.getP().bitLength() - 1);
        this.params = params2;
    }

    public DSAParameters getParameters() {
        return this.params;
    }
}
