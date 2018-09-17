package java.security.spec;

import java.math.BigInteger;

public class DSAPrivateKeySpec implements KeySpec {
    private BigInteger g;
    private BigInteger p;
    private BigInteger q;
    private BigInteger x;

    public DSAPrivateKeySpec(BigInteger x, BigInteger p, BigInteger q, BigInteger g) {
        this.x = x;
        this.p = p;
        this.q = q;
        this.g = g;
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
