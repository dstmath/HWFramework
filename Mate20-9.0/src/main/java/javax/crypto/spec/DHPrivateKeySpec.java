package javax.crypto.spec;

import java.math.BigInteger;
import java.security.spec.KeySpec;

public class DHPrivateKeySpec implements KeySpec {
    private BigInteger g;
    private BigInteger p;
    private BigInteger x;

    public DHPrivateKeySpec(BigInteger x2, BigInteger p2, BigInteger g2) {
        this.x = x2;
        this.p = p2;
        this.g = g2;
    }

    public BigInteger getX() {
        return this.x;
    }

    public BigInteger getP() {
        return this.p;
    }

    public BigInteger getG() {
        return this.g;
    }
}
