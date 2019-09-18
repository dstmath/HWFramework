package org.bouncycastle.crypto.params;

import java.math.BigInteger;
import org.bouncycastle.math.ec.ECAlgorithms;
import org.bouncycastle.math.ec.ECConstants;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.util.Arrays;

public class ECDomainParameters implements ECConstants {
    private ECPoint G;
    private ECCurve curve;
    private BigInteger h;
    private BigInteger hInv;
    private BigInteger n;
    private byte[] seed;

    public ECDomainParameters(ECCurve eCCurve, ECPoint eCPoint, BigInteger bigInteger) {
        this(eCCurve, eCPoint, bigInteger, ONE, null);
    }

    public ECDomainParameters(ECCurve eCCurve, ECPoint eCPoint, BigInteger bigInteger, BigInteger bigInteger2) {
        this(eCCurve, eCPoint, bigInteger, bigInteger2, null);
    }

    public ECDomainParameters(ECCurve eCCurve, ECPoint eCPoint, BigInteger bigInteger, BigInteger bigInteger2, byte[] bArr) {
        this.hInv = null;
        if (eCCurve == null) {
            throw new NullPointerException("curve");
        } else if (bigInteger != null) {
            this.curve = eCCurve;
            this.G = validate(eCCurve, eCPoint);
            this.n = bigInteger;
            this.h = bigInteger2;
            this.seed = bArr;
        } else {
            throw new NullPointerException("n");
        }
    }

    static ECPoint validate(ECCurve eCCurve, ECPoint eCPoint) {
        if (eCPoint == null) {
            throw new IllegalArgumentException("point has null value");
        } else if (!eCPoint.isInfinity()) {
            ECPoint normalize = eCPoint.normalize();
            if (normalize.isValid()) {
                return ECAlgorithms.importPoint(eCCurve, normalize);
            }
            throw new IllegalArgumentException("point not on curve");
        } else {
            throw new IllegalArgumentException("point at infinity");
        }
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ECDomainParameters)) {
            return false;
        }
        ECDomainParameters eCDomainParameters = (ECDomainParameters) obj;
        return this.curve.equals(eCDomainParameters.curve) && this.G.equals(eCDomainParameters.G) && this.n.equals(eCDomainParameters.n) && this.h.equals(eCDomainParameters.h);
    }

    public ECCurve getCurve() {
        return this.curve;
    }

    public ECPoint getG() {
        return this.G;
    }

    public BigInteger getH() {
        return this.h;
    }

    public synchronized BigInteger getHInv() {
        if (this.hInv == null) {
            this.hInv = this.h.modInverse(this.n);
        }
        return this.hInv;
    }

    public BigInteger getN() {
        return this.n;
    }

    public byte[] getSeed() {
        return Arrays.clone(this.seed);
    }

    public int hashCode() {
        return (((((this.curve.hashCode() * 37) ^ this.G.hashCode()) * 37) ^ this.n.hashCode()) * 37) ^ this.h.hashCode();
    }
}
