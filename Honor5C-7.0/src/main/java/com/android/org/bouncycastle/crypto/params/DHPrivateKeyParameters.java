package com.android.org.bouncycastle.crypto.params;

import java.math.BigInteger;

public class DHPrivateKeyParameters extends DHKeyParameters {
    private BigInteger x;

    public DHPrivateKeyParameters(BigInteger x, DHParameters params) {
        super(true, params);
        this.x = x;
    }

    public BigInteger getX() {
        return this.x;
    }

    public int hashCode() {
        return this.x.hashCode() ^ super.hashCode();
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (!(obj instanceof DHPrivateKeyParameters)) {
            return false;
        }
        if (((DHPrivateKeyParameters) obj).getX().equals(this.x)) {
            z = super.equals(obj);
        }
        return z;
    }
}
