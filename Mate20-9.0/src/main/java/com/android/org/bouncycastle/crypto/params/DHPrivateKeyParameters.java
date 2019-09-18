package com.android.org.bouncycastle.crypto.params;

import java.math.BigInteger;

public class DHPrivateKeyParameters extends DHKeyParameters {
    private BigInteger x;

    public DHPrivateKeyParameters(BigInteger x2, DHParameters params) {
        super(true, params);
        this.x = x2;
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
        if (((DHPrivateKeyParameters) obj).getX().equals(this.x) && super.equals(obj)) {
            z = true;
        }
        return z;
    }
}
