package org.bouncycastle.math.ec;

import java.math.BigInteger;

public class MontgomeryLadderMultiplier extends AbstractECMultiplier {
    /* JADX WARN: Type inference failed for: r3v0, types: [boolean] */
    /* access modifiers changed from: protected */
    /* JADX WARNING: Unknown variable types count: 1 */
    @Override // org.bouncycastle.math.ec.AbstractECMultiplier
    public ECPoint multiplyPositive(ECPoint eCPoint, BigInteger bigInteger) {
        ECPoint[] eCPointArr = {eCPoint.getCurve().getInfinity(), eCPoint};
        int bitLength = bigInteger.bitLength();
        while (true) {
            bitLength--;
            if (bitLength < 0) {
                return eCPointArr[0];
            }
            ?? testBit = bigInteger.testBit(bitLength);
            int i = 1 - (testBit == true ? 1 : 0);
            eCPointArr[i] = eCPointArr[i].add(eCPointArr[testBit == true ? 1 : 0]);
            eCPointArr[testBit] = eCPointArr[testBit].twice();
        }
    }
}
