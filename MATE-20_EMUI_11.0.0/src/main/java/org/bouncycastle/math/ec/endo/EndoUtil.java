package org.bouncycastle.math.ec.endo;

import java.math.BigInteger;
import org.bouncycastle.math.ec.ECConstants;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.math.ec.PreCompCallback;
import org.bouncycastle.math.ec.PreCompInfo;

public abstract class EndoUtil {
    public static final String PRECOMP_NAME = "bc_endo";

    private static BigInteger calculateB(BigInteger bigInteger, BigInteger bigInteger2, int i) {
        boolean z = bigInteger2.signum() < 0;
        BigInteger multiply = bigInteger.multiply(bigInteger2.abs());
        boolean testBit = multiply.testBit(i - 1);
        BigInteger shiftRight = multiply.shiftRight(i);
        if (testBit) {
            shiftRight = shiftRight.add(ECConstants.ONE);
        }
        return z ? shiftRight.negate() : shiftRight;
    }

    public static BigInteger[] decomposeScalar(ScalarSplitParameters scalarSplitParameters, BigInteger bigInteger) {
        int bits = scalarSplitParameters.getBits();
        BigInteger calculateB = calculateB(bigInteger, scalarSplitParameters.getG1(), bits);
        BigInteger calculateB2 = calculateB(bigInteger, scalarSplitParameters.getG2(), bits);
        return new BigInteger[]{bigInteger.subtract(calculateB.multiply(scalarSplitParameters.getV1A()).add(calculateB2.multiply(scalarSplitParameters.getV2A()))), calculateB.multiply(scalarSplitParameters.getV1B()).add(calculateB2.multiply(scalarSplitParameters.getV2B())).negate()};
    }

    public static ECPoint mapPoint(final ECEndomorphism eCEndomorphism, final ECPoint eCPoint) {
        return ((EndoPreCompInfo) eCPoint.getCurve().precompute(eCPoint, PRECOMP_NAME, new PreCompCallback() {
            /* class org.bouncycastle.math.ec.endo.EndoUtil.AnonymousClass1 */

            private boolean checkExisting(EndoPreCompInfo endoPreCompInfo, ECEndomorphism eCEndomorphism) {
                return (endoPreCompInfo == null || endoPreCompInfo.getEndomorphism() != eCEndomorphism || endoPreCompInfo.getMappedPoint() == null) ? false : true;
            }

            @Override // org.bouncycastle.math.ec.PreCompCallback
            public PreCompInfo precompute(PreCompInfo preCompInfo) {
                EndoPreCompInfo endoPreCompInfo = preCompInfo instanceof EndoPreCompInfo ? (EndoPreCompInfo) preCompInfo : null;
                if (checkExisting(endoPreCompInfo, eCEndomorphism)) {
                    return endoPreCompInfo;
                }
                ECPoint map = eCEndomorphism.getPointMap().map(eCPoint);
                EndoPreCompInfo endoPreCompInfo2 = new EndoPreCompInfo();
                endoPreCompInfo2.setEndomorphism(eCEndomorphism);
                endoPreCompInfo2.setMappedPoint(map);
                return endoPreCompInfo2;
            }
        })).getMappedPoint();
    }
}
