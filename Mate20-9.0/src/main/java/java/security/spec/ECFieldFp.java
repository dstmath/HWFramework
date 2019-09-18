package java.security.spec;

import java.math.BigInteger;

public class ECFieldFp implements ECField {
    private BigInteger p;

    public ECFieldFp(BigInteger p2) {
        if (p2.signum() == 1) {
            this.p = p2;
            return;
        }
        throw new IllegalArgumentException("p is not positive");
    }

    public int getFieldSize() {
        return this.p.bitLength();
    }

    public BigInteger getP() {
        return this.p;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof ECFieldFp) {
            return this.p.equals(((ECFieldFp) obj).p);
        }
        return false;
    }

    public int hashCode() {
        return this.p.hashCode();
    }
}
