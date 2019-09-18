package javax.crypto.spec;

import java.math.BigInteger;
import java.security.spec.KeySpec;

public class DHPublicKeySpec implements KeySpec {
    private BigInteger g;
    private BigInteger p;
    private BigInteger y;

    public DHPublicKeySpec(BigInteger y2, BigInteger p2, BigInteger g2) {
        this.y = y2;
        this.p = p2;
        this.g = g2;
    }

    public BigInteger getY() {
        return this.y;
    }

    public BigInteger getP() {
        return this.p;
    }

    public BigInteger getG() {
        return this.g;
    }
}
