package com.android.org.bouncycastle.math.ec;

import java.math.BigInteger;

public class WNafL2RMultiplier extends AbstractECMultiplier {
    /* access modifiers changed from: protected */
    public ECPoint multiplyPositive(ECPoint p, BigInteger k) {
        ECPoint R;
        int width = Math.max(2, Math.min(16, getWindowSize(k.bitLength())));
        WNafPreCompInfo wnafPreCompInfo = WNafUtil.precompute(p, width, true);
        ECPoint[] preComp = wnafPreCompInfo.getPreComp();
        ECPoint[] preCompNeg = wnafPreCompInfo.getPreCompNeg();
        int[] wnaf = WNafUtil.generateCompactWindowNaf(width, k);
        ECPoint R2 = p.getCurve().getInfinity();
        int i = wnaf.length;
        if (i > 1) {
            i--;
            int wi = wnaf[i];
            int digit = wi >> 16;
            int zeroes = wi & 65535;
            int n = Math.abs(digit);
            ECPoint[] table = digit < 0 ? preCompNeg : preComp;
            if ((n << 2) < (1 << width)) {
                int scale = width - LongArray.bitLengths[n];
                int i2 = width;
                R = table[((1 << (width - 1)) - 1) >>> 1].add(table[(((n ^ (1 << (LongArray.bitLengths[n] - 1))) << scale) + 1) >>> 1]);
                zeroes -= scale;
            } else {
                R = table[n >>> 1];
            }
            R2 = R.timesPow2(zeroes);
        }
        while (i > 0) {
            i--;
            int wi2 = wnaf[i];
            int digit2 = wi2 >> 16;
            R2 = R2.twicePlus((digit2 < 0 ? preCompNeg : preComp)[Math.abs(digit2) >>> 1]).timesPow2(wi2 & 65535);
        }
        return R2;
    }

    /* access modifiers changed from: protected */
    public int getWindowSize(int bits) {
        return WNafUtil.getWindowSize(bits);
    }
}
