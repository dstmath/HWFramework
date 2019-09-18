package java.security.spec;

import java.math.BigInteger;

public class DSAPrivateKeySpec implements KeySpec {
    private BigInteger g;
    private BigInteger p;
    private BigInteger q;
    private BigInteger x;

    public DSAPrivateKeySpec(BigInteger x2, BigInteger p2, BigInteger q2, BigInteger g2) {
        this.x = x2;
        this.p = p2;
        this.q = q2;
        this.g = g2;
    }

    public BigInteger getX() {
        return this.x;
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
