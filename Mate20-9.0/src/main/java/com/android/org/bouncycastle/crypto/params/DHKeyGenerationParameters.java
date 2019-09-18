package com.android.org.bouncycastle.crypto.params;

import com.android.org.bouncycastle.crypto.KeyGenerationParameters;
import java.security.SecureRandom;

public class DHKeyGenerationParameters extends KeyGenerationParameters {
    private DHParameters params;

    public DHKeyGenerationParameters(SecureRandom random, DHParameters params2) {
        super(random, getStrength(params2));
        this.params = params2;
    }

    public DHParameters getParameters() {
        return this.params;
    }

    static int getStrength(DHParameters params2) {
        return params2.getL() != 0 ? params2.getL() : params2.getP().bitLength();
    }
}
