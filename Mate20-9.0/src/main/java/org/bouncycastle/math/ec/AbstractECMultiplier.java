package org.bouncycastle.math.ec;

import java.math.BigInteger;

public abstract class AbstractECMultiplier implements ECMultiplier {
    /* access modifiers changed from: protected */
    public ECPoint checkResult(ECPoint eCPoint) {
        return ECAlgorithms.implCheckResult(eCPoint);
    }

    public ECPoint multiply(ECPoint eCPoint, BigInteger bigInteger) {
        int signum = bigInteger.signum();
        if (signum == 0 || eCPoint.isInfinity()) {
            return eCPoint.getCurve().getInfinity();
        }
        ECPoint multiplyPositive = multiplyPositive(eCPoint, bigInteger.abs());
        if (signum <= 0) {
            multiplyPositive = multiplyPositive.negate();
        }
        return checkResult(multiplyPositive);
    }

    /* access modifiers changed from: protected */
    public abstract ECPoint multiplyPositive(ECPoint eCPoint, BigInteger bigInteger);
}
