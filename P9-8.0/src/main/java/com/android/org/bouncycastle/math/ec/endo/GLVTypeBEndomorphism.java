package com.android.org.bouncycastle.math.ec.endo;

import com.android.org.bouncycastle.math.ec.ECConstants;
import com.android.org.bouncycastle.math.ec.ECCurve;
import com.android.org.bouncycastle.math.ec.ECPointMap;
import com.android.org.bouncycastle.math.ec.ScaleXPointMap;
import java.math.BigInteger;

public class GLVTypeBEndomorphism implements GLVEndomorphism {
    protected final ECCurve curve;
    protected final GLVTypeBParameters parameters;
    protected final ECPointMap pointMap;

    public GLVTypeBEndomorphism(ECCurve curve, GLVTypeBParameters parameters) {
        this.curve = curve;
        this.parameters = parameters;
        this.pointMap = new ScaleXPointMap(curve.fromBigInteger(parameters.getBeta()));
    }

    public BigInteger[] decomposeScalar(BigInteger k) {
        int bits = this.parameters.getBits();
        BigInteger b1 = calculateB(k, this.parameters.getG1(), bits);
        BigInteger b2 = calculateB(k, this.parameters.getG2(), bits);
        GLVTypeBParameters p = this.parameters;
        BigInteger a = k.subtract(b1.multiply(p.getV1A()).add(b2.multiply(p.getV2A())));
        BigInteger b = b1.multiply(p.getV1B()).add(b2.multiply(p.getV2B())).negate();
        return new BigInteger[]{a, b};
    }

    public ECPointMap getPointMap() {
        return this.pointMap;
    }

    public boolean hasEfficientPointMap() {
        return true;
    }

    protected BigInteger calculateB(BigInteger k, BigInteger g, int t) {
        boolean negative = g.signum() < 0;
        BigInteger b = k.multiply(g.abs());
        boolean extra = b.testBit(t - 1);
        b = b.shiftRight(t);
        if (extra) {
            b = b.add(ECConstants.ONE);
        }
        return negative ? b.negate() : b;
    }
}
