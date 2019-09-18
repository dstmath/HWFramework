package org.bouncycastle.math.ec;

import java.math.BigInteger;

public class ZSignedDigitL2RMultiplier extends AbstractECMultiplier {
    /* access modifiers changed from: protected */
    public ECPoint multiplyPositive(ECPoint eCPoint, BigInteger bigInteger) {
        ECPoint normalize = eCPoint.normalize();
        ECPoint negate = normalize.negate();
        int bitLength = bigInteger.bitLength();
        int lowestSetBit = bigInteger.getLowestSetBit();
        ECPoint eCPoint2 = normalize;
        while (true) {
            bitLength--;
            if (bitLength <= lowestSetBit) {
                return eCPoint2.timesPow2(lowestSetBit);
            }
            eCPoint2 = eCPoint2.twicePlus(bigInteger.testBit(bitLength) ? normalize : negate);
        }
    }
}
