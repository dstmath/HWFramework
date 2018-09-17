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

    public ECParameterSpec(ECCurve curve, ECPoint G, BigInteger n) {
        this.curve = curve;
        this.G = G.normalize();
        this.n = n;
        this.h = BigInteger.valueOf(1);
        this.seed = null;
    }

    public ECParameterSpec(ECCurve curve, ECPoint G, BigInteger n, BigInteger h) {
        this.curve = curve;
        this.G = G.normalize();
        this.n = n;
        this.h = h;
        this.seed = null;
    }

    public ECParameterSpec(ECCurve curve, ECPoint G, BigInteger n, BigInteger h, byte[] seed) {
        this.curve = curve;
        this.G = G.normalize();
        this.n = n;
        this.h = h;
        this.seed = seed;
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
        if (getCurve().equals(other.getCurve())) {
            z = getG().equals(other.getG());
        }
        return z;
    }

    public int hashCode() {
        return getCurve().hashCode() ^ getG().hashCode();
    }
}
