package org.bouncycastle.math.ec;

import java.math.BigInteger;

public class DoubleAddMultiplier extends AbstractECMultiplier {
    /* access modifiers changed from: protected */
    @Override // org.bouncycastle.math.ec.AbstractECMultiplier
    public ECPoint multiplyPositive(ECPoint eCPoint, BigInteger bigInteger) {
        ECPoint[] eCPointArr = {eCPoint.getCurve().getInfinity(), eCPoint};
        int bitLength = bigInteger.bitLength();
        for (int i = 0; i < bitLength; i++) {
            boolean testBit = bigInteger.testBit(i);
            int i2 = 1 - (testBit ? 1 : 0);
            eCPointArr[i2] = eCPointArr[i2].twicePlus(eCPointArr[testBit ? 1 : 0]);
        }
        return eCPointArr[0];
    }
}
