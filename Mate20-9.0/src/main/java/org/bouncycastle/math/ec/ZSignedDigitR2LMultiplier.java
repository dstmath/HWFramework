package org.bouncycastle.math.ec;

import java.math.BigInteger;

public class ZSignedDigitR2LMultiplier extends AbstractECMultiplier {
    /* access modifiers changed from: protected */
    public ECPoint multiplyPositive(ECPoint eCPoint, BigInteger bigInteger) {
        ECPoint infinity = eCPoint.getCurve().getInfinity();
        int bitLength = bigInteger.bitLength();
        int lowestSetBit = bigInteger.getLowestSetBit();
        ECPoint timesPow2 = eCPoint.timesPow2(lowestSetBit);
        while (true) {
            lowestSetBit++;
            if (lowestSetBit >= bitLength) {
                return infinity.add(timesPow2);
            }
            infinity = infinity.add(bigInteger.testBit(lowestSetBit) ? timesPow2 : timesPow2.negate());
            timesPow2 = timesPow2.twice();
        }
    }
}
