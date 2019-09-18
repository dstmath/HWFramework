package org.bouncycastle.crypto.params;

import java.math.BigInteger;

public class DHPublicKeyParameters extends DHKeyParameters {
    private static final BigInteger ONE = BigInteger.valueOf(1);
    private static final BigInteger TWO = BigInteger.valueOf(2);
    private BigInteger y;

    public DHPublicKeyParameters(BigInteger bigInteger, DHParameters dHParameters) {
        super(false, dHParameters);
        this.y = validate(bigInteger, dHParameters);
    }

    private BigInteger validate(BigInteger bigInteger, DHParameters dHParameters) {
        if (bigInteger == null) {
            throw new NullPointerException("y value cannot be null");
        } else if (bigInteger.compareTo(TWO) < 0 || bigInteger.compareTo(dHParameters.getP().subtract(TWO)) > 0) {
            throw new IllegalArgumentException("invalid DH public key");
        } else if (dHParameters.getQ() == null || ONE.equals(bigInteger.modPow(dHParameters.getQ(), dHParameters.getP()))) {
            return bigInteger;
        } else {
            throw new IllegalArgumentException("Y value does not appear to be in correct group");
        }
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

    public BigInteger getY() {
        return this.y;
    }

    public int hashCode() {
        return this.y.hashCode() ^ super.hashCode();
    }
}
