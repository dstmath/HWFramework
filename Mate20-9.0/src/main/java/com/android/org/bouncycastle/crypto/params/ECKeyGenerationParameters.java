package com.android.org.bouncycastle.crypto.params;

import com.android.org.bouncycastle.crypto.KeyGenerationParameters;
import java.security.SecureRandom;

public class ECKeyGenerationParameters extends KeyGenerationParameters {
    private ECDomainParameters domainParams;

    public ECKeyGenerationParameters(ECDomainParameters domainParams2, SecureRandom random) {
        super(random, domainParams2.getN().bitLength());
        this.domainParams = domainParams2;
    }

    public ECDomainParameters getDomainParameters() {
        return this.domainParams;
    }
}
