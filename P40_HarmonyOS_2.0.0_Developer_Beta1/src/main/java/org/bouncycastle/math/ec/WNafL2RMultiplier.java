package org.bouncycastle.math.ec;

import java.math.BigInteger;
import org.bouncycastle.crypto.digests.Blake2xsDigest;
import org.bouncycastle.util.Integers;

public class WNafL2RMultiplier extends AbstractECMultiplier {
    /* access modifiers changed from: protected */
    @Override // org.bouncycastle.math.ec.AbstractECMultiplier
    public ECPoint multiplyPositive(ECPoint eCPoint, BigInteger bigInteger) {
        ECPoint eCPoint2;
        WNafPreCompInfo precompute = WNafUtil.precompute(eCPoint, WNafUtil.getWindowSize(bigInteger.bitLength()), true);
        ECPoint[] preComp = precompute.getPreComp();
        ECPoint[] preCompNeg = precompute.getPreCompNeg();
        int width = precompute.getWidth();
        int[] generateCompactWindowNaf = WNafUtil.generateCompactWindowNaf(width, bigInteger);
        ECPoint infinity = eCPoint.getCurve().getInfinity();
        int length = generateCompactWindowNaf.length;
        if (length > 1) {
            length--;
            int i = generateCompactWindowNaf[length];
            int i2 = i >> 16;
            int i3 = i & Blake2xsDigest.UNKNOWN_DIGEST_LENGTH;
            int abs = Math.abs(i2);
            ECPoint[] eCPointArr = i2 < 0 ? preCompNeg : preComp;
            if ((abs << 2) < (1 << width)) {
                int numberOfLeadingZeros = 32 - Integers.numberOfLeadingZeros(abs);
                int i4 = width - numberOfLeadingZeros;
                eCPoint2 = eCPointArr[((1 << (width - 1)) - 1) >>> 1].add(eCPointArr[(((abs ^ (1 << (numberOfLeadingZeros - 1))) << i4) + 1) >>> 1]);
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
            infinity = infinity.twicePlus((i6 < 0 ? preCompNeg : preComp)[Math.abs(i6) >>> 1]).timesPow2(i5 & Blake2xsDigest.UNKNOWN_DIGEST_LENGTH);
        }
        return infinity;
    }
}
