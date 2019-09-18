package com.android.org.bouncycastle.crypto.params;

import java.math.BigInteger;

public class DSAPublicKeyParameters extends DSAKeyParameters {
    private static final BigInteger ONE = BigInteger.valueOf(1);
    private static final BigInteger TWO = BigInteger.valueOf(2);
    private BigInteger y;

    public DSAPublicKeyParameters(BigInteger y2, DSAParameters params) {
        super(false, params);
        this.y = validate(y2, params);
    }

    private BigInteger validate(BigInteger y2, DSAParameters params) {
        if (params == null) {
            return y2;
        }
        if (TWO.compareTo(y2) <= 0 && params.getP().subtract(TWO).compareTo(y2) >= 0 && ONE.equals(y2.modPow(params.getQ(), params.getP()))) {
            return y2;
        }
        throw new IllegalArgumentException("y value does not appear to be in correct group");
    }

    public BigInteger getY() {
        return this.y;
    }
}
