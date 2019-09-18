package org.bouncycastle.crypto.params;

import java.math.BigInteger;

public class ElGamalPublicKeyParameters extends ElGamalKeyParameters {
    private BigInteger y;

    public ElGamalPublicKeyParameters(BigInteger bigInteger, ElGamalParameters elGamalParameters) {
        super(false, elGamalParameters);
        this.y = bigInteger;
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (!(obj instanceof ElGamalPublicKeyParameters)) {
            return false;
        }
        if (((ElGamalPublicKeyParameters) obj).getY().equals(this.y) && super.equals(obj)) {
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
