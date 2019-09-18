package com.android.org.bouncycastle.crypto.params;

import com.android.org.bouncycastle.math.ec.ECConstants;
import com.android.org.bouncycastle.math.ec.ECCurve;
import com.android.org.bouncycastle.math.ec.ECPoint;
import com.android.org.bouncycastle.util.Arrays;
import java.math.BigInteger;

public class ECDomainParameters implements ECConstants {
    private ECPoint G;
    private ECCurve curve;
    private BigInteger h;
    private BigInteger n;
    private byte[] seed;

    public ECDomainParameters(ECCurve curve2, ECPoint G2, BigInteger n2) {
        this(curve2, G2, n2, ONE, null);
    }

    public ECDomainParameters(ECCurve curve2, ECPoint G2, BigInteger n2, BigInteger h2) {
        this(curve2, G2, n2, h2, null);
    }

    public ECDomainParameters(ECCurve curve2, ECPoint G2, BigInteger n2, BigInteger h2, byte[] seed2) {
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
        return Arrays.clone(this.seed);
    }

    public boolean equals(Object obj) {
        boolean z = true;
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ECDomainParameters)) {
            return false;
        }
        ECDomainParameters other = (ECDomainParameters) obj;
        if (!this.curve.equals(other.curve) || !this.G.equals(other.G) || !this.n.equals(other.n) || !this.h.equals(other.h)) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        return (((((this.curve.hashCode() * 37) ^ this.G.hashCode()) * 37) ^ this.n.hashCode()) * 37) ^ this.h.hashCode();
    }
}
