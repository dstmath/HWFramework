package com.android.org.bouncycastle.crypto.params;

import java.math.BigInteger;

public class ECPrivateKeyParameters extends ECKeyParameters {
    BigInteger d;

    public ECPrivateKeyParameters(BigInteger d2, ECDomainParameters params) {
        super(true, params);
        this.d = d2;
    }

    public BigInteger getD() {
        return this.d;
    }
}
