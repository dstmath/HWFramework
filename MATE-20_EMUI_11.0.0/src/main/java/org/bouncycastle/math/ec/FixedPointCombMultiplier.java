package org.bouncycastle.math.ec;

import java.math.BigInteger;
import org.bouncycastle.math.raw.Nat;

public class FixedPointCombMultiplier extends AbstractECMultiplier {
    /* access modifiers changed from: protected */
    @Override // org.bouncycastle.math.ec.AbstractECMultiplier
    public ECPoint multiplyPositive(ECPoint eCPoint, BigInteger bigInteger) {
        ECCurve curve = eCPoint.getCurve();
        int combSize = FixedPointUtil.getCombSize(curve);
        if (bigInteger.bitLength() <= combSize) {
            FixedPointPreCompInfo precompute = FixedPointUtil.precompute(eCPoint);
            ECLookupTable lookupTable = precompute.getLookupTable();
            int width = precompute.getWidth();
            int i = ((combSize + width) - 1) / width;
            ECPoint infinity = curve.getInfinity();
            int i2 = width * i;
            int[] fromBigInteger = Nat.fromBigInteger(i2, bigInteger);
            int i3 = i2 - 1;
            ECPoint eCPoint2 = infinity;
            for (int i4 = 0; i4 < i; i4++) {
                int i5 = 0;
                for (int i6 = i3 - i4; i6 >= 0; i6 -= i) {
                    int i7 = fromBigInteger[i6 >>> 5] >>> (i6 & 31);
                    i5 = ((i5 ^ (i7 >>> 1)) << 1) ^ i7;
                }
                eCPoint2 = eCPoint2.twicePlus(lookupTable.lookup(i5));
            }
            return eCPoint2.add(precompute.getOffset());
        }
        throw new IllegalStateException("fixed-point comb doesn't support scalars larger than the curve order");
    }
}
