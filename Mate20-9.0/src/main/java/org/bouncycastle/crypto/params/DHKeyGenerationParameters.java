package org.bouncycastle.crypto.params;

import java.security.SecureRandom;
import org.bouncycastle.crypto.KeyGenerationParameters;

public class DHKeyGenerationParameters extends KeyGenerationParameters {
    private DHParameters params;

    public DHKeyGenerationParameters(SecureRandom secureRandom, DHParameters dHParameters) {
        super(secureRandom, getStrength(dHParameters));
        this.params = dHParameters;
    }

    static int getStrength(DHParameters dHParameters) {
        return dHParameters.getL() != 0 ? dHParameters.getL() : dHParameters.getP().bitLength();
    }

    public DHParameters getParameters() {
        return this.params;
    }
}
