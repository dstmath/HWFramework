package com.android.org.bouncycastle.math.ec;

import java.math.BigInteger;

public class WNafL2RMultiplier extends AbstractECMultiplier {
    protected ECPoint multiplyPositive(ECPoint p, BigInteger k) {
        int wi;
        int digit;
        int width = Math.max(2, Math.min(16, getWindowSize(k.bitLength())));
        WNafPreCompInfo wnafPreCompInfo = WNafUtil.precompute(p, width, true);
        ECPoint[] preComp = wnafPreCompInfo.getPreComp();
        ECPoint[] preCompNeg = wnafPreCompInfo.getPreCompNeg();
        int[] wnaf = WNafUtil.generateCompactWindowNaf(width, k);
        ECPoint R = p.getCurve().getInfinity();
        int i = wnaf.length;
        if (i > 1) {
            i--;
            wi = wnaf[i];
            digit = wi >> 16;
            int zeroes = wi & 65535;
            int n = Math.abs(digit);
            ECPoint[] table = digit < 0 ? preCompNeg : preComp;
            if ((n << 2) < (1 << width)) {
                int highest = LongArray.bitLengths[n];
                int scale = width - highest;
                R = table[((1 << (width - 1)) - 1) >>> 1].add(table[(((n ^ (1 << (highest - 1))) << scale) + 1) >>> 1]);
                zeroes -= scale;
            } else {
                R = table[n >>> 1];
            }
            R = R.timesPow2(zeroes);
        }
        while (i > 0) {
            i--;
            wi = wnaf[i];
            digit = wi >> 16;
            R = R.twicePlus((digit < 0 ? preCompNeg : preComp)[Math.abs(digit) >>> 1]).timesPow2(wi & 65535);
        }
        return R;
    }

    protected int getWindowSize(int bits) {
        return WNafUtil.getWindowSize(bits);
    }
}
