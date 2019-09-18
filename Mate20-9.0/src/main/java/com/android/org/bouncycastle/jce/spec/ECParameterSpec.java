package com.android.org.bouncycastle.jce.spec;

import com.android.org.bouncycastle.math.ec.ECCurve;
import com.android.org.bouncycastle.math.ec.ECPoint;
import java.math.BigInteger;
import java.security.spec.AlgorithmParameterSpec;

public class ECParameterSpec implements AlgorithmParameterSpec {
    private ECPoint G;
    private ECCurve curve;
    private BigInteger h;
    private BigInteger n;
    private byte[] seed;

    public ECParameterSpec(ECCurve curve2, ECPoint G2, BigInteger n2) {
        this.curve = curve2;
        this.G = G2.normalize();
        this.n = n2;
        this.h = BigInteger.valueOf(1);
        this.seed = null;
    }

    public ECParameterSpec(ECCurve curve2, ECPoint G2, BigInteger n2, BigInteger h2) {
        this.curve = curve2;
        this.G = G2.normalize();
        this.n = n2;
        this.h = h2;
        this.seed = null;
    }

    public ECParameterSpec(ECCurve curve2, ECPoint G2, BigInteger n2, BigInteger h2, byte[] seed2) {
        this.curve = curve2;
        this.G = G2.normalize();
        this.n = n2;
        this.h = h2;
        this.seed = seed2;
    }

    public ECCurve getCurve() {
        return this.curve;
    }

    public ECPoint getG() {
        return this.G;
    }

    public BigInteger getN() {
        return this.n;
    }

    public BigInteger getH() {
        return this.h;
    }

    public byte[] getSeed() {
        return this.seed;
    }

    public boolean equals(Object o) {
        boolean z = false;
        if (!(o instanceof ECParameterSpec)) {
            return false;
        }
        ECParameterSpec other = (ECParameterSpec) o;
        if (getCurve().equals(other.getCurve()) && getG().equals(other.getG())) {
            z = true;
        }
        return z;
    }

    public int hashCode() {
        return getCurve().hashCode() ^ getG().hashCode();
    }
}
