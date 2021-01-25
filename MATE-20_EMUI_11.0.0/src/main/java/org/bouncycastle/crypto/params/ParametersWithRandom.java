package org.bouncycastle.crypto.params;

import java.security.SecureRandom;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.CryptoServicesRegistrar;

public class ParametersWithRandom implements CipherParameters {
    private CipherParameters parameters;
    private SecureRandom random;

    public ParametersWithRandom(CipherParameters cipherParameters) {
        this(cipherParameters, CryptoServicesRegistrar.getSecureRandom());
    }

    public ParametersWithRandom(CipherParameters cipherParameters, SecureRandom secureRandom) {
        this.random = secureRandom;
        this.parameters = cipherParameters;
    }

    public CipherParameters getParameters() {
        return this.parameters;
    }

    public SecureRandom getRandom() {
        return this.random;
    }
}
