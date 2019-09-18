package java.security.spec;

import java.math.BigInteger;

public class DSAPublicKeySpec implements KeySpec {
    private BigInteger g;
    private BigInteger p;
    private BigInteger q;
    private BigInteger y;

    public DSAPublicKeySpec(BigInteger y2, BigInteger p2, BigInteger q2, BigInteger g2) {
        this.y = y2;
        this.p = p2;
        this.q = q2;
        this.g = g2;
    }

    public BigInteger getY() {
        return this.y;
    }

    public BigInteger getP() {
        return this.p;
    }

    public BigInteger getQ() {
        return this.q;
    }

    public BigInteger getG() {
        return this.g;
    }
}
