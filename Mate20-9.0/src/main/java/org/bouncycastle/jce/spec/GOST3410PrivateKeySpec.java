package org.bouncycastle.jce.spec;

import java.math.BigInteger;
import java.security.spec.KeySpec;

public class GOST3410PrivateKeySpec implements KeySpec {
    private BigInteger a;
    private BigInteger p;
    private BigInteger q;
    private BigInteger x;

    public GOST3410PrivateKeySpec(BigInteger bigInteger, BigInteger bigInteger2, BigInteger bigInteger3, BigInteger bigInteger4) {
        this.x = bigInteger;
        this.p = bigInteger2;
        this.q = bigInteger3;
        this.a = bigInteger4;
    }

    public BigInteger getA() {
        return this.a;
    }

    public BigInteger getP() {
        return this.p;
    }

    public BigInteger getQ() {
        return this.q;
    }

    public BigInteger getX() {
        return this.x;
    }
}
