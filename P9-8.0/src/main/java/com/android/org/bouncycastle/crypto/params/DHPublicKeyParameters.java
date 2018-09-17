package com.android.org.bouncycastle.crypto.params;

import java.math.BigInteger;

public class DHPublicKeyParameters extends DHKeyParameters {
    private static final BigInteger ONE = BigInteger.valueOf(1);
    private static final BigInteger TWO = BigInteger.valueOf(2);
    private BigInteger y;

    public DHPublicKeyParameters(BigInteger y, DHParameters params) {
        super(false, params);
        this.y = validate(y, params);
    }

    private BigInteger validate(BigInteger y, DHParameters dhParams) {
        if (y == null) {
            throw new NullPointerException("y value cannot be null");
        } else if (y.compareTo(TWO) < 0 || y.compareTo(dhParams.getP().subtract(TWO)) > 0) {
            throw new IllegalArgumentException("invalid DH public key");
        } else if (dhParams.getQ() == null || ONE.equals(y.modPow(dhParams.getQ(), dhParams.getP()))) {
            return y;
        } else {
            throw new IllegalArgumentException("Y value does not appear to be in correct group");
        }
    }

    public BigInteger getY() {
        return this.y;
    }

    public int hashCode() {
        return this.y.hashCode() ^ super.hashCode();
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (!(obj instanceof DHPublicKeyParameters)) {
            return false;
        }
        if (((DHPublicKeyParameters) obj).getY().equals(this.y)) {
            z = super.equals(obj);
        }
        return z;
    }
}
