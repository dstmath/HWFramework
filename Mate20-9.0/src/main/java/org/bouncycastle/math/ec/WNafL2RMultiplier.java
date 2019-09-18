package org.bouncycastle.math.ec;

import java.math.BigInteger;

public class WNafL2RMultiplier extends AbstractECMultiplier {
    /* access modifiers changed from: protected */
    public int getWindowSize(int i) {
        return WNafUtil.getWindowSize(i);
    }

    /* access modifiers changed from: protected */
    public ECPoint multiplyPositive(ECPoint eCPoint, BigInteger bigInteger) {
        ECPoint eCPoint2;
        int max = Math.max(2, Math.min(16, getWindowSize(bigInteger.bitLength())));
        WNafPreCompInfo precompute = WNafUtil.precompute(eCPoint, max, true);
        ECPoint[] preComp = precompute.getPreComp();
        ECPoint[] preCompNeg = precompute.getPreCompNeg();
        int[] generateCompactWindowNaf = WNafUtil.generateCompactWindowNaf(max, bigInteger);
        ECPoint infinity = eCPoint.getCurve().getInfinity();
        int length = generateCompactWindowNaf.length;
        if (length > 1) {
            length--;
            int i = generateCompactWindowNaf[length];
            int i2 = i >> 16;
            int i3 = i & 65535;
            int abs = Math.abs(i2);
            ECPoint[] eCPointArr = i2 < 0 ? preCompNeg : preComp;
            if ((abs << 2) < (1 << max)) {
                byte b = LongArray.bitLengths[abs];
                int i4 = max - b;
                eCPoint2 = eCPointArr[((1 << (max - 1)) - 1) >>> 1].add(eCPointArr[(((abs ^ (1 << (b - 1))) << i4) + 1) >>> 1]);
                i3 -= i4;
            } else {
                eCPoint2 = eCPointArr[abs >>> 1];
            }
            infinity = eCPoint2.timesPow2(i3);
        }
        while (length > 0) {
            length--;
            int i5 = generateCompactWindowNaf[length];
            int i6 = i5 >> 16;
            infinity = infinity.twicePlus((i6 < 0 ? preCompNeg : preComp)[Math.abs(i6) >>> 1]).timesPow2(i5 & 65535);
        }
        return infinity;
    }
}
