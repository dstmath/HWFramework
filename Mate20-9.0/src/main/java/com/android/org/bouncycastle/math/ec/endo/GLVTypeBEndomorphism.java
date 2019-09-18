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

    public GLVTypeBEndomorphism(ECCurve curve2, GLVTypeBParameters parameters2) {
        this.curve = curve2;
        this.parameters = parameters2;
        this.pointMap = new ScaleXPointMap(curve2.fromBigInteger(parameters2.getBeta()));
    }

    public BigInteger[] decomposeScalar(BigInteger k) {
        int bits = this.parameters.getBits();
        BigInteger b1 = calculateB(k, this.parameters.getG1(), bits);
        BigInteger b2 = calculateB(k, this.parameters.getG2(), bits);
        GLVTypeBParameters p = this.parameters;
        return new BigInteger[]{k.subtract(b1.multiply(p.getV1A()).add(b2.multiply(p.getV2A()))), b1.multiply(p.getV1B()).add(b2.multiply(p.getV2B())).negate()};
    }

    public ECPointMap getPointMap() {
        return this.pointMap;
    }

    public boolean hasEfficientPointMap() {
        return true;
    }

    /* access modifiers changed from: protected */
    public BigInteger calculateB(BigInteger k, BigInteger g, int t) {
        boolean negative = g.signum() < 0;
        BigInteger b = k.multiply(g.abs());
        boolean extra = b.testBit(t - 1);
        BigInteger b2 = b.shiftRight(t);
        if (extra) {
            b2 = b2.add(ECConstants.ONE);
        }
        return negative ? b2.negate() : b2;
    }
}
