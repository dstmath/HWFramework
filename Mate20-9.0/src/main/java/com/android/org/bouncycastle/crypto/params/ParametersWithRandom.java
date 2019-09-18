package com.android.org.bouncycastle.crypto.params;

import com.android.org.bouncycastle.crypto.CipherParameters;
import java.security.SecureRandom;

public class ParametersWithRandom implements CipherParameters {
    private CipherParameters parameters;
    private SecureRandom random;

    public ParametersWithRandom(CipherParameters parameters2, SecureRandom random2) {
        this.random = random2;
        this.parameters = parameters2;
    }

    public ParametersWithRandom(CipherParameters parameters2) {
        this(parameters2, new SecureRandom());
    }

    public SecureRandom getRandom() {
        return this.random;
    }

    public CipherParameters getParameters() {
        return this.parameters;
    }
}
