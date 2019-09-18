package org.bouncycastle.math.ec;

import java.math.BigInteger;

public class MontgomeryLadderMultiplier extends AbstractECMultiplier {
    /* access modifiers changed from: protected */
    public ECPoint multiplyPositive(ECPoint eCPoint, BigInteger bigInteger) {
        ECPoint[] eCPointArr = {eCPoint.getCurve().getInfinity(), eCPoint};
        int bitLength = bigInteger.bitLength();
        while (true) {
            bitLength--;
            if (bitLength < 0) {
                return eCPointArr[0];
            }
            int testBit = bigInteger.testBit(bitLength);
            int i = 1 - testBit;
            eCPointArr[i] = eCPointArr[i].add(eCPointArr[testBit]);
            eCPointArr[testBit] = eCPointArr[testBit].twice();
        }
    }
}
