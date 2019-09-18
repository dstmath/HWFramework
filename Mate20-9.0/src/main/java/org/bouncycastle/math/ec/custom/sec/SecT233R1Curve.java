package org.bouncycastle.math.ec.custom.sec;

import java.math.BigInteger;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECFieldElement;
import org.bouncycastle.math.ec.ECLookupTable;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.math.raw.Nat256;
import org.bouncycastle.util.encoders.Hex;

public class SecT233R1Curve extends ECCurve.AbstractF2m {
    private static final int SecT233R1_DEFAULT_COORDS = 6;
    protected SecT233R1Point infinity = new SecT233R1Point(this, null, null);

    public SecT233R1Curve() {
        super(233, 74, 0, 0);
        this.a = fromBigInteger(BigInteger.valueOf(1));
        this.b = fromBigInteger(new BigInteger(1, Hex.decode("0066647EDE6C332C7F8C0923BB58213B333B20E9CE4281FE115F7D8F90AD")));
        this.order = new BigInteger(1, Hex.decode("01000000000000000000000000000013E974E72F8A6922031D2603CFE0D7"));
        this.cofactor = BigInteger.valueOf(2);
        this.coord = 6;
    }

    /* access modifiers changed from: protected */
    public ECCurve cloneCurve() {
        return new SecT233R1Curve();
    }

    public ECLookupTable createCacheSafeLookupTable(ECPoint[] eCPointArr, int i, final int i2) {
        final long[] jArr = new long[(i2 * 4 * 2)];
        int i3 = 0;
        for (int i4 = 0; i4 < i2; i4++) {
            ECPoint eCPoint = eCPointArr[i + i4];
            Nat256.copy64(((SecT233FieldElement) eCPoint.getRawXCoord()).x, 0, jArr, i3);
            int i5 = i3 + 4;
            Nat256.copy64(((SecT233FieldElement) eCPoint.getRawYCoord()).x, 0, jArr, i5);
            i3 = i5 + 4;
        }
        return new ECLookupTable() {
            public int getSize() {
                return i2;
            }

            public ECPoint lookup(int i) {
                long[] create64 = Nat256.create64();
                long[] create642 = Nat256.create64();
                int i2 = 0;
                for (int i3 = 0; i3 < i2; i3++) {
                    long j = (long) (((i3 ^ i) - 1) >> 31);
                    for (int i4 = 0; i4 < 4; i4++) {
                        create64[i4] = create64[i4] ^ (jArr[i2 + i4] & j);
                        create642[i4] = create642[i4] ^ (jArr[(i2 + 4) + i4] & j);
                    }
                    i2 += 8;
                }
                return SecT233R1Curve.this.createRawPoint(new SecT233FieldElement(create64), new SecT233FieldElement(create642), false);
            }
        };
    }

    /* access modifiers changed from: protected */
    public ECPoint createRawPoint(ECFieldElement eCFieldElement, ECFieldElement eCFieldElement2, boolean z) {
        return new SecT233R1Point(this, eCFieldElement, eCFieldElement2, z);
    }

    /* access modifiers changed from: protected */
    public ECPoint createRawPoint(ECFieldElement eCFieldElement, ECFieldElement eCFieldElement2, ECFieldElement[] eCFieldElementArr, boolean z) {
        SecT233R1Point secT233R1Point = new SecT233R1Point(this, eCFieldElement, eCFieldElement2, eCFieldElementArr, z);
        return secT233R1Point;
    }

    public ECFieldElement fromBigInteger(BigInteger bigInteger) {
        return new SecT233FieldElement(bigInteger);
    }

    public int getFieldSize() {
        return 233;
    }

    public ECPoint getInfinity() {
        return this.infinity;
    }

    public int getK1() {
        return 74;
    }

    public int getK2() {
        return 0;
    }

    public int getK3() {
        return 0;
    }

    public int getM() {
        return 233;
    }

    public boolean isKoblitz() {
        return false;
    }

    public boolean isTrinomial() {
        return true;
    }

    public boolean supportsCoordinateSystem(int i) {
        return i == 6;
    }
}
