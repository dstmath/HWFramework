package com.android.org.bouncycastle.math.ec.custom.sec;

import com.android.org.bouncycastle.math.ec.ECConstants;
import com.android.org.bouncycastle.math.ec.ECCurve;
import com.android.org.bouncycastle.math.ec.ECFieldElement;
import com.android.org.bouncycastle.math.ec.ECPoint;
import com.android.org.bouncycastle.util.encoders.Hex;
import java.math.BigInteger;

public class SecP224K1Curve extends ECCurve.AbstractFp {
    private static final int SECP224K1_DEFAULT_COORDS = 2;
    public static final BigInteger q = new BigInteger(1, Hex.decode("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEFFFFE56D"));
    protected SecP224K1Point infinity = new SecP224K1Point(this, null, null);

    public SecP224K1Curve() {
        super(q);
        this.a = fromBigInteger(ECConstants.ZERO);
        this.b = fromBigInteger(BigInteger.valueOf(5));
        this.order = new BigInteger(1, Hex.decode("010000000000000000000000000001DCE8D2EC6184CAF0A971769FB1F7"));
        this.cofactor = BigInteger.valueOf(1);
        this.coord = 2;
    }

    /* access modifiers changed from: protected */
    public ECCurve cloneCurve() {
        return new SecP224K1Curve();
    }

    public boolean supportsCoordinateSystem(int coord) {
        if (coord != 2) {
            return false;
        }
        return true;
    }

    public BigInteger getQ() {
        return q;
    }

    public int getFieldSize() {
        return q.bitLength();
    }

    public ECFieldElement fromBigInteger(BigInteger x) {
        return new SecP224K1FieldElement(x);
    }

    /* access modifiers changed from: protected */
    public ECPoint createRawPoint(ECFieldElement x, ECFieldElement y, boolean withCompression) {
        return new SecP224K1Point(this, x, y, withCompression);
    }

    /* access modifiers changed from: protected */
    public ECPoint createRawPoint(ECFieldElement x, ECFieldElement y, ECFieldElement[] zs, boolean withCompression) {
        SecP224K1Point secP224K1Point = new SecP224K1Point(this, x, y, zs, withCompression);
        return secP224K1Point;
    }

    public ECPoint getInfinity() {
        return this.infinity;
    }
}
