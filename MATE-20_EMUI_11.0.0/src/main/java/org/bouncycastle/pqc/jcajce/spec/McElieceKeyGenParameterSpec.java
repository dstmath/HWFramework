package org.bouncycastle.pqc.jcajce.spec;

import java.security.InvalidParameterException;
import java.security.spec.AlgorithmParameterSpec;
import org.bouncycastle.pqc.math.linearalgebra.PolynomialRingGF2;

public class McElieceKeyGenParameterSpec implements AlgorithmParameterSpec {
    public static final int DEFAULT_M = 11;
    public static final int DEFAULT_T = 50;
    private int fieldPoly;
    private int m;
    private int n;
    private int t;

    public McElieceKeyGenParameterSpec() {
        this(11, 50);
    }

    public McElieceKeyGenParameterSpec(int i) {
        if (i >= 1) {
            this.m = 0;
            this.n = 1;
            while (true) {
                int i2 = this.n;
                if (i2 < i) {
                    this.n = i2 << 1;
                    this.m++;
                } else {
                    this.t = i2 >>> 1;
                    int i3 = this.t;
                    int i4 = this.m;
                    this.t = i3 / i4;
                    this.fieldPoly = PolynomialRingGF2.getIrreduciblePolynomial(i4);
                    return;
                }
            }
        } else {
            throw new IllegalArgumentException("key size must be positive");
        }
    }

    public McElieceKeyGenParameterSpec(int i, int i2) throws InvalidParameterException {
        if (i < 1) {
            throw new IllegalArgumentException("m must be positive");
        } else if (i <= 32) {
            this.m = i;
            this.n = 1 << i;
            if (i2 < 0) {
                throw new IllegalArgumentException("t must be positive");
            } else if (i2 <= this.n) {
                this.t = i2;
                this.fieldPoly = PolynomialRingGF2.getIrreduciblePolynomial(i);
            } else {
                throw new IllegalArgumentException("t must be less than n = 2^m");
            }
        } else {
            throw new IllegalArgumentException("m is too large");
        }
    }

    public McElieceKeyGenParameterSpec(int i, int i2, int i3) {
        this.m = i;
        if (i < 1) {
            throw new IllegalArgumentException("m must be positive");
        } else if (i <= 32) {
            this.n = 1 << i;
            this.t = i2;
            if (i2 < 0) {
                throw new IllegalArgumentException("t must be positive");
            } else if (i2 > this.n) {
                throw new IllegalArgumentException("t must be less than n = 2^m");
            } else if (PolynomialRingGF2.degree(i3) != i || !PolynomialRingGF2.isIrreducible(i3)) {
                throw new IllegalArgumentException("polynomial is not a field polynomial for GF(2^m)");
            } else {
                this.fieldPoly = i3;
            }
        } else {
            throw new IllegalArgumentException(" m is too large");
        }
    }

    public int getFieldPoly() {
        return this.fieldPoly;
    }

    public int getM() {
        return this.m;
    }

    public int getN() {
        return this.n;
    }

    public int getT() {
        return this.t;
    }
}
