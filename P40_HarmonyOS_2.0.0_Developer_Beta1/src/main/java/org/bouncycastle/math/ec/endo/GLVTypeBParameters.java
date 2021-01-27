package org.bouncycastle.math.ec.endo;

import java.math.BigInteger;

public class GLVTypeBParameters {
    protected final BigInteger beta;
    protected final BigInteger lambda;
    protected final ScalarSplitParameters splitParams;

    public GLVTypeBParameters(BigInteger bigInteger, BigInteger bigInteger2, ScalarSplitParameters scalarSplitParameters) {
        this.beta = bigInteger;
        this.lambda = bigInteger2;
        this.splitParams = scalarSplitParameters;
    }

    public GLVTypeBParameters(BigInteger bigInteger, BigInteger bigInteger2, BigInteger[] bigIntegerArr, BigInteger[] bigIntegerArr2, BigInteger bigInteger3, BigInteger bigInteger4, int i) {
        this.beta = bigInteger;
        this.lambda = bigInteger2;
        this.splitParams = new ScalarSplitParameters(bigIntegerArr, bigIntegerArr2, bigInteger3, bigInteger4, i);
    }

    public BigInteger getBeta() {
        return this.beta;
    }

    public int getBits() {
        return getSplitParams().getBits();
    }

    public BigInteger getG1() {
        return getSplitParams().getG1();
    }

    public BigInteger getG2() {
        return getSplitParams().getG2();
    }

    public BigInteger getLambda() {
        return this.lambda;
    }

    public ScalarSplitParameters getSplitParams() {
        return this.splitParams;
    }

    public BigInteger getV1A() {
        return getSplitParams().getV1A();
    }

    public BigInteger getV1B() {
        return getSplitParams().getV1B();
    }

    public BigInteger getV2A() {
        return getSplitParams().getV2A();
    }

    public BigInteger getV2B() {
        return getSplitParams().getV2B();
    }
}
