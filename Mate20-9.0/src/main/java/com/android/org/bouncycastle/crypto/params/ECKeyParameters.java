package com.android.org.bouncycastle.crypto.params;

public class ECKeyParameters extends AsymmetricKeyParameter {
    ECDomainParameters params;

    protected ECKeyParameters(boolean isPrivate, ECDomainParameters params2) {
        super(isPrivate);
        this.params = params2;
    }

    public ECDomainParameters getParameters() {
        return this.params;
    }
}
