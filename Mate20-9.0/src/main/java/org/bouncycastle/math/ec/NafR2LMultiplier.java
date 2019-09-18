package org.bouncycastle.math.ec;

import java.math.BigInteger;

public class NafR2LMultiplier extends AbstractECMultiplier {
    /* access modifiers changed from: protected */
    public ECPoint multiplyPositive(ECPoint eCPoint, BigInteger bigInteger) {
        int[] generateCompactNaf = WNafUtil.generateCompactNaf(bigInteger);
        int i = 0;
        ECPoint infinity = eCPoint.getCurve().getInfinity();
        ECPoint eCPoint2 = eCPoint;
        int i2 = 0;
        while (i < generateCompactNaf.length) {
            int i3 = generateCompactNaf[i];
            int i4 = i3 >> 16;
            eCPoint2 = eCPoint2.timesPow2(i2 + (i3 & 65535));
            infinity = infinity.add(i4 < 0 ? eCPoint2.negate() : eCPoint2);
            i++;
            i2 = 1;
        }
        return infinity;
    }
}
