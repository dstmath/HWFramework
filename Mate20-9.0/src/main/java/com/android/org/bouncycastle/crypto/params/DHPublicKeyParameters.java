package com.android.org.bouncycastle.crypto.params;

import java.math.BigInteger;

public class DHPublicKeyParameters extends DHKeyParameters {
    private static final BigInteger ONE = BigInteger.valueOf(1);
    private static final BigInteger TWO = BigInteger.valueOf(2);
    private BigInteger y;

    public DHPublicKeyParameters(BigInteger y2, DHParameters params) {
        super(false, params);
        this.y = validate(y2, params);
    }

    private BigInteger validate(BigInteger y2, DHParameters dhParams) {
        if (y2 == null) {
            throw new NullPointerException("y value cannot be null");
        } else if (y2.compareTo(TWO) < 0 || y2.compareTo(dhParams.getP().subtract(TWO)) > 0) {
            throw new IllegalArgumentException("invalid DH public key");
        } else if (dhParams.getQ() == null || ONE.equals(y2.modPow(dhParams.getQ(), dhParams.getP()))) {
            return y2;
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
        if (((DHPublicKeyParameters) obj).getY().equals(this.y) && super.equals(obj)) {
            z = true;
        }
        return z;
    }
}
