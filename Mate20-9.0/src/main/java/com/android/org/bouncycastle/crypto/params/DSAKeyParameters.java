package com.android.org.bouncycastle.crypto.params;

public class DSAKeyParameters extends AsymmetricKeyParameter {
    private DSAParameters params;

    public DSAKeyParameters(boolean isPrivate, DSAParameters params2) {
        super(isPrivate);
        this.params = params2;
    }

    public DSAParameters getParameters() {
        return this.params;
    }
}
