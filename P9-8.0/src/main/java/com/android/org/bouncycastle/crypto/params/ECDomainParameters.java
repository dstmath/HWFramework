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

    public ECDomainParameters(ECCurve curve, ECPoint G, BigInteger n) {
        this(curve, G, n, ONE, null);
    }

    public ECDomainParameters(ECCurve curve, ECPoint G, BigInteger n, BigInteger h) {
        this(curve, G, n, h, null);
    }

    public ECDomainParameters(ECCurve curve, ECPoint G, BigInteger n, BigInteger h, byte[] seed) {
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
        return Arrays.clone(this.seed);
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ECDomainParameters)) {
            return false;
        }
        ECDomainParameters other = (ECDomainParameters) obj;
        if (this.curve.equals(other.curve) && this.G.equals(other.G) && this.n.equals(other.n)) {
            z = this.h.equals(other.h);
        }
        return z;
    }

    public int hashCode() {
        return (((((this.curve.hashCode() * 37) ^ this.G.hashCode()) * 37) ^ this.n.hashCode()) * 37) ^ this.h.hashCode();
    }
}
