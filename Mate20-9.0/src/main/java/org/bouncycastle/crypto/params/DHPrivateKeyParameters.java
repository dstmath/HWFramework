package org.bouncycastle.crypto.params;

import java.math.BigInteger;

public class DHPrivateKeyParameters extends DHKeyParameters {
    private BigInteger x;

    public DHPrivateKeyParameters(BigInteger bigInteger, DHParameters dHParameters) {
        super(true, dHParameters);
        this.x = bigInteger;
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

    public BigInteger getX() {
        return this.x;
    }

    public int hashCode() {
        return this.x.hashCode() ^ super.hashCode();
    }
}
