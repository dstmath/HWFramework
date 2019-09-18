package org.bouncycastle.math.ec.custom.sec;

import java.math.BigInteger;
import org.bouncycastle.crypto.tls.CipherSuite;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECFieldElement;
import org.bouncycastle.math.ec.ECLookupTable;
import org.bouncycastle.math.ec.ECMultiplier;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.math.ec.WTauNafMultiplier;
import org.bouncycastle.math.raw.Nat256;
import org.bouncycastle.util.encoders.Hex;

public class SecT239K1Curve extends ECCurve.AbstractF2m {
    private static final int SecT239K1_DEFAULT_COORDS = 6;
    protected SecT239K1Point infinity = new SecT239K1Point(this, null, null);

    public SecT239K1Curve() {
        super(239, CipherSuite.TLS_DHE_RSA_WITH_AES_128_GCM_SHA256, 0, 0);
        this.a = fromBigInteger(BigInteger.valueOf(0));
        this.b = fromBigInteger(BigInteger.valueOf(1));
        this.order = new BigInteger(1, Hex.decode("2000000000000000000000000000005A79FEC67CB6E91F1C1DA800E478A5"));
        this.cofactor = BigInteger.valueOf(4);
        this.coord = 6;
    }

    /* access modifiers changed from: protected */
    public ECCurve cloneCurve() {
        return new SecT239K1Curve();
    }

    public ECLookupTable createCacheSafeLookupTable(ECPoint[] eCPointArr, int i, final int i2) {
        final long[] jArr = new long[(i2 * 4 * 2)];
        int i3 = 0;
        for (int i4 = 0; i4 < i2; i4++) {
            ECPoint eCPoint = eCPointArr[i + i4];
            Nat256.copy64(((SecT239FieldElement) eCPoint.getRawXCoord()).x, 0, jArr, i3);
            int i5 = i3 + 4;
            Nat256.copy64(((SecT239FieldElement) eCPoint.getRawYCoord()).x, 0, jArr, i5);
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
                return SecT239K1Curve.this.createRawPoint(new SecT239FieldElement(create64), new SecT239FieldElement(create642), false);
            }
        };
    }

    /* access modifiers changed from: protected */
    public ECMultiplier createDefaultMultiplier() {
        return new WTauNafMultiplier();
    }

    /* access modifiers changed from: protected */
    public ECPoint createRawPoint(ECFieldElement eCFieldElement, ECFieldElement eCFieldElement2, boolean z) {
        return new SecT239K1Point(this, eCFieldElement, eCFieldElement2, z);
    }

    /* access modifiers changed from: protected */
    public ECPoint createRawPoint(ECFieldElement eCFieldElement, ECFieldElement eCFieldElement2, ECFieldElement[] eCFieldElementArr, boolean z) {
        SecT239K1Point secT239K1Point = new SecT239K1Point(this, eCFieldElement, eCFieldElement2, eCFieldElementArr, z);
        return secT239K1Point;
    }

    public ECFieldElement fromBigInteger(BigInteger bigInteger) {
        return new SecT239FieldElement(bigInteger);
    }

    public int getFieldSize() {
        return 239;
    }

    public ECPoint getInfinity() {
        return this.infinity;
    }

    public int getK1() {
        return CipherSuite.TLS_DHE_RSA_WITH_AES_128_GCM_SHA256;
    }

    public int getK2() {
        return 0;
    }

    public int getK3() {
        return 0;
    }

    public int getM() {
        return 239;
    }

    public boolean isKoblitz() {
        return true;
    }

    public boolean isTrinomial() {
        return true;
    }

    public boolean supportsCoordinateSystem(int i) {
        return i == 6;
    }
}
