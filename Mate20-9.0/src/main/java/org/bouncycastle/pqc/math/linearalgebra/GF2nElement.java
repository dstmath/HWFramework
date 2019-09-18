package org.bouncycastle.pqc.math.linearalgebra;

public abstract class GF2nElement implements GFElement {
    protected int mDegree;
    protected GF2nField mField;

    /* access modifiers changed from: package-private */
    public abstract void assignOne();

    /* access modifiers changed from: package-private */
    public abstract void assignZero();

    public abstract Object clone();

    public final GF2nElement convert(GF2nField gF2nField) {
        return this.mField.convert(this, gF2nField);
    }

    public final GF2nField getField() {
        return this.mField;
    }

    public abstract GF2nElement increase();

    public abstract void increaseThis();

    public abstract GF2nElement solveQuadraticEquation() throws RuntimeException;

    public abstract GF2nElement square();

    public abstract GF2nElement squareRoot();

    public abstract void squareRootThis();

    public abstract void squareThis();

    public final GFElement subtract(GFElement gFElement) {
        return add(gFElement);
    }

    public final void subtractFromThis(GFElement gFElement) {
        addToThis(gFElement);
    }

    /* access modifiers changed from: package-private */
    public abstract boolean testBit(int i);

    public abstract boolean testRightmostBit();

    public abstract int trace();
}
