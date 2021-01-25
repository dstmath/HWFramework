package org.bouncycastle.math.ec;

import java.math.BigInteger;
import org.bouncycastle.crypto.digests.Blake2xsDigest;

public class NafR2LMultiplier extends AbstractECMultiplier {
    /* access modifiers changed from: protected */
    @Override // org.bouncycastle.math.ec.AbstractECMultiplier
    public ECPoint multiplyPositive(ECPoint eCPoint, BigInteger bigInteger) {
        int[] generateCompactNaf = WNafUtil.generateCompactNaf(bigInteger);
        ECPoint infinity = eCPoint.getCurve().getInfinity();
        int i = 0;
        ECPoint eCPoint2 = eCPoint;
        int i2 = 0;
        while (i < generateCompactNaf.length) {
            int i3 = generateCompactNaf[i];
            int i4 = i3 >> 16;
            eCPoint2 = eCPoint2.timesPow2(i2 + (i3 & Blake2xsDigest.UNKNOWN_DIGEST_LENGTH));
            infinity = infinity.add(i4 < 0 ? eCPoint2.negate() : eCPoint2);
            i++;
            i2 = 1;
        }
        return infinity;
    }
}
