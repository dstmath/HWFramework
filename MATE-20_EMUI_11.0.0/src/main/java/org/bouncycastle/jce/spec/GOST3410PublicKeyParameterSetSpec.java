package org.bouncycastle.jce.spec;

import java.math.BigInteger;

public class GOST3410PublicKeyParameterSetSpec {
    private BigInteger a;
    private BigInteger p;
    private BigInteger q;

    public GOST3410PublicKeyParameterSetSpec(BigInteger bigInteger, BigInteger bigInteger2, BigInteger bigInteger3) {
        this.p = bigInteger;
        this.q = bigInteger2;
        this.a = bigInteger3;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof GOST3410PublicKeyParameterSetSpec)) {
            return false;
        }
        GOST3410PublicKeyParameterSetSpec gOST3410PublicKeyParameterSetSpec = (GOST3410PublicKeyParameterSetSpec) obj;
        return this.a.equals(gOST3410PublicKeyParameterSetSpec.a) && this.p.equals(gOST3410PublicKeyParameterSetSpec.p) && this.q.equals(gOST3410PublicKeyParameterSetSpec.q);
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

    public int hashCode() {
        return (this.a.hashCode() ^ this.p.hashCode()) ^ this.q.hashCode();
    }
}
