package org.bouncycastle.math.ec.endo;

import java.math.BigInteger;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECPointMap;
import org.bouncycastle.math.ec.ScaleYNegateXPointMap;

public class GLVTypeAEndomorphism implements GLVEndomorphism {
    protected final GLVTypeAParameters parameters;
    protected final ECPointMap pointMap;

    public GLVTypeAEndomorphism(ECCurve eCCurve, GLVTypeAParameters gLVTypeAParameters) {
        this.parameters = gLVTypeAParameters;
        this.pointMap = new ScaleYNegateXPointMap(eCCurve.fromBigInteger(gLVTypeAParameters.getI()));
    }

    @Override // org.bouncycastle.math.ec.endo.GLVEndomorphism
    public BigInteger[] decomposeScalar(BigInteger bigInteger) {
        return EndoUtil.decomposeScalar(this.parameters.getSplitParams(), bigInteger);
    }

    @Override // org.bouncycastle.math.ec.endo.ECEndomorphism
    public ECPointMap getPointMap() {
        return this.pointMap;
    }

    @Override // org.bouncycastle.math.ec.endo.ECEndomorphism
    public boolean hasEfficientPointMap() {
        return true;
    }
}
