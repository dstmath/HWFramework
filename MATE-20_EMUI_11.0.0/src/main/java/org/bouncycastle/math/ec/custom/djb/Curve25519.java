package org.bouncycastle.math.ec.custom.djb;

import java.math.BigInteger;
import org.bouncycastle.math.ec.AbstractECLookupTable;
import org.bouncycastle.math.ec.ECConstants;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECFieldElement;
import org.bouncycastle.math.ec.ECLookupTable;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.math.raw.Nat256;
import org.bouncycastle.util.encoders.Hex;

public class Curve25519 extends ECCurve.AbstractFp {
    private static final ECFieldElement[] CURVE25519_AFFINE_ZS = {new Curve25519FieldElement(ECConstants.ONE), new Curve25519FieldElement(C_a)};
    private static final int CURVE25519_DEFAULT_COORDS = 4;
    private static final BigInteger C_a = new BigInteger(1, Hex.decodeStrict("2AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA984914A144"));
    private static final BigInteger C_b = new BigInteger(1, Hex.decodeStrict("7B425ED097B425ED097B425ED097B425ED097B425ED097B4260B5E9C7710C864"));
    public static final BigInteger q = Curve25519FieldElement.Q;
    protected Curve25519Point infinity = new Curve25519Point(this, null, null);

    public Curve25519() {
        super(q);
        this.a = fromBigInteger(C_a);
        this.b = fromBigInteger(C_b);
        this.order = new BigInteger(1, Hex.decodeStrict("1000000000000000000000000000000014DEF9DEA2F79CD65812631A5CF5D3ED"));
        this.cofactor = BigInteger.valueOf(8);
        this.coord = 4;
    }

    /* access modifiers changed from: protected */
    @Override // org.bouncycastle.math.ec.ECCurve
    public ECCurve cloneCurve() {
        return new Curve25519();
    }

    @Override // org.bouncycastle.math.ec.ECCurve
    public ECLookupTable createCacheSafeLookupTable(ECPoint[] eCPointArr, int i, final int i2) {
        final int[] iArr = new int[(i2 * 8 * 2)];
        int i3 = 0;
        for (int i4 = 0; i4 < i2; i4++) {
            ECPoint eCPoint = eCPointArr[i + i4];
            Nat256.copy(((Curve25519FieldElement) eCPoint.getRawXCoord()).x, 0, iArr, i3);
            int i5 = i3 + 8;
            Nat256.copy(((Curve25519FieldElement) eCPoint.getRawYCoord()).x, 0, iArr, i5);
            i3 = i5 + 8;
        }
        return new AbstractECLookupTable() {
            /* class org.bouncycastle.math.ec.custom.djb.Curve25519.AnonymousClass1 */

            private ECPoint createPoint(int[] iArr, int[] iArr2) {
                return Curve25519.this.createRawPoint(new Curve25519FieldElement(iArr), new Curve25519FieldElement(iArr2), Curve25519.CURVE25519_AFFINE_ZS);
            }

            @Override // org.bouncycastle.math.ec.ECLookupTable
            public int getSize() {
                return i2;
            }

            @Override // org.bouncycastle.math.ec.ECLookupTable
            public ECPoint lookup(int i) {
                int[] create = Nat256.create();
                int[] create2 = Nat256.create();
                int i2 = 0;
                for (int i3 = 0; i3 < i2; i3++) {
                    int i4 = ((i3 ^ i) - 1) >> 31;
                    for (int i5 = 0; i5 < 8; i5++) {
                        int i6 = create[i5];
                        int[] iArr = iArr;
                        create[i5] = i6 ^ (iArr[i2 + i5] & i4);
                        create2[i5] = create2[i5] ^ (iArr[(i2 + 8) + i5] & i4);
                    }
                    i2 += 16;
                }
                return createPoint(create, create2);
            }

            @Override // org.bouncycastle.math.ec.AbstractECLookupTable, org.bouncycastle.math.ec.ECLookupTable
            public ECPoint lookupVar(int i) {
                int[] create = Nat256.create();
                int[] create2 = Nat256.create();
                int i2 = i * 8 * 2;
                for (int i3 = 0; i3 < 8; i3++) {
                    int[] iArr = iArr;
                    create[i3] = iArr[i2 + i3];
                    create2[i3] = iArr[i2 + 8 + i3];
                }
                return createPoint(create, create2);
            }
        };
    }

    /* access modifiers changed from: protected */
    @Override // org.bouncycastle.math.ec.ECCurve
    public ECPoint createRawPoint(ECFieldElement eCFieldElement, ECFieldElement eCFieldElement2) {
        return new Curve25519Point(this, eCFieldElement, eCFieldElement2);
    }

    /* access modifiers changed from: protected */
    @Override // org.bouncycastle.math.ec.ECCurve
    public ECPoint createRawPoint(ECFieldElement eCFieldElement, ECFieldElement eCFieldElement2, ECFieldElement[] eCFieldElementArr) {
        return new Curve25519Point(this, eCFieldElement, eCFieldElement2, eCFieldElementArr);
    }

    @Override // org.bouncycastle.math.ec.ECCurve
    public ECFieldElement fromBigInteger(BigInteger bigInteger) {
        return new Curve25519FieldElement(bigInteger);
    }

    @Override // org.bouncycastle.math.ec.ECCurve
    public int getFieldSize() {
        return q.bitLength();
    }

    @Override // org.bouncycastle.math.ec.ECCurve
    public ECPoint getInfinity() {
        return this.infinity;
    }

    public BigInteger getQ() {
        return q;
    }

    @Override // org.bouncycastle.math.ec.ECCurve
    public boolean supportsCoordinateSystem(int i) {
        return i == 4;
    }
}
