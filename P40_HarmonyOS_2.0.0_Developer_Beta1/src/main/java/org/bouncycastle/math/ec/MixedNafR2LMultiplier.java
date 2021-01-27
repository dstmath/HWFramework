package org.bouncycastle.math.ec;

import java.math.BigInteger;
import org.bouncycastle.crypto.digests.Blake2xsDigest;

public class MixedNafR2LMultiplier extends AbstractECMultiplier {
    protected int additionCoord;
    protected int doublingCoord;

    public MixedNafR2LMultiplier() {
        this(2, 4);
    }

    public MixedNafR2LMultiplier(int i, int i2) {
        this.additionCoord = i;
        this.doublingCoord = i2;
    }

    /* access modifiers changed from: protected */
    public ECCurve configureCurve(ECCurve eCCurve, int i) {
        if (eCCurve.getCoordinateSystem() == i) {
            return eCCurve;
        }
        if (eCCurve.supportsCoordinateSystem(i)) {
            return eCCurve.configure().setCoordinateSystem(i).create();
        }
        throw new IllegalArgumentException("Coordinate system " + i + " not supported by this curve");
    }

    /* access modifiers changed from: protected */
    @Override // org.bouncycastle.math.ec.AbstractECMultiplier
    public ECPoint multiplyPositive(ECPoint eCPoint, BigInteger bigInteger) {
        ECCurve curve = eCPoint.getCurve();
        ECCurve configureCurve = configureCurve(curve, this.additionCoord);
        ECCurve configureCurve2 = configureCurve(curve, this.doublingCoord);
        int[] generateCompactNaf = WNafUtil.generateCompactNaf(bigInteger);
        ECPoint infinity = configureCurve.getInfinity();
        ECPoint importPoint = configureCurve2.importPoint(eCPoint);
        int i = 0;
        ECPoint eCPoint2 = importPoint;
        int i2 = 0;
        while (i < generateCompactNaf.length) {
            int i3 = generateCompactNaf[i];
            int i4 = i3 >> 16;
            eCPoint2 = eCPoint2.timesPow2(i2 + (i3 & Blake2xsDigest.UNKNOWN_DIGEST_LENGTH));
            ECPoint importPoint2 = configureCurve.importPoint(eCPoint2);
            if (i4 < 0) {
                importPoint2 = importPoint2.negate();
            }
            infinity = infinity.add(importPoint2);
            i++;
            i2 = 1;
        }
        return curve.importPoint(infinity);
    }
}
