package org.bouncycastle.pqc.math.linearalgebra;

import java.security.SecureRandom;
import java.util.Random;
import java.util.Vector;

public class GF2nPolynomialField extends GF2nField {
    private boolean isPentanomial = false;
    private boolean isTrinomial = false;
    private int[] pc = new int[3];
    GF2Polynomial[] squaringMatrix;
    private int tc;

    public GF2nPolynomialField(int i, SecureRandom secureRandom) {
        super(secureRandom);
        if (i >= 3) {
            this.mDegree = i;
            computeFieldPolynomial();
            computeSquaringMatrix();
            this.fields = new Vector();
            this.matrices = new Vector();
            return;
        }
        throw new IllegalArgumentException("k must be at least 3");
    }

    public GF2nPolynomialField(int i, SecureRandom secureRandom, GF2Polynomial gF2Polynomial) throws RuntimeException {
        super(secureRandom);
        if (i < 3) {
            throw new IllegalArgumentException("degree must be at least 3");
        } else if (gF2Polynomial.getLength() != i + 1) {
            throw new RuntimeException();
        } else if (gF2Polynomial.isIrreducible()) {
            this.mDegree = i;
            this.fieldPolynomial = gF2Polynomial;
            computeSquaringMatrix();
            int i2 = 2;
            for (int i3 = 1; i3 < this.fieldPolynomial.getLength() - 1; i3++) {
                if (this.fieldPolynomial.testBit(i3)) {
                    i2++;
                    if (i2 == 3) {
                        this.tc = i3;
                    }
                    if (i2 <= 5) {
                        this.pc[i2 - 3] = i3;
                    }
                }
            }
            if (i2 == 3) {
                this.isTrinomial = true;
            }
            if (i2 == 5) {
                this.isPentanomial = true;
            }
            this.fields = new Vector();
            this.matrices = new Vector();
        } else {
            throw new RuntimeException();
        }
    }

    public GF2nPolynomialField(int i, SecureRandom secureRandom, boolean z) {
        super(secureRandom);
        if (i >= 3) {
            this.mDegree = i;
            if (z) {
                computeFieldPolynomial();
            } else {
                computeFieldPolynomial2();
            }
            computeSquaringMatrix();
            this.fields = new Vector();
            this.matrices = new Vector();
            return;
        }
        throw new IllegalArgumentException("k must be at least 3");
    }

    private void computeSquaringMatrix() {
        GF2Polynomial[] gF2PolynomialArr = new GF2Polynomial[(this.mDegree - 1)];
        this.squaringMatrix = new GF2Polynomial[this.mDegree];
        for (int i = 0; i < this.squaringMatrix.length; i++) {
            this.squaringMatrix[i] = new GF2Polynomial(this.mDegree, "ZERO");
        }
        for (int i2 = 0; i2 < this.mDegree - 1; i2++) {
            gF2PolynomialArr[i2] = new GF2Polynomial(1, "ONE").shiftLeft(this.mDegree + i2).remainder(this.fieldPolynomial);
        }
        for (int i3 = 1; i3 <= Math.abs(this.mDegree >> 1); i3++) {
            for (int i4 = 1; i4 <= this.mDegree; i4++) {
                if (gF2PolynomialArr[this.mDegree - (i3 << 1)].testBit(this.mDegree - i4)) {
                    this.squaringMatrix[i4 - 1].setBit(this.mDegree - i3);
                }
            }
        }
        for (int abs = Math.abs(this.mDegree >> 1) + 1; abs <= this.mDegree; abs++) {
            this.squaringMatrix[((abs << 1) - this.mDegree) - 1].setBit(this.mDegree - abs);
        }
    }

    private boolean testPentanomials() {
        this.fieldPolynomial = new GF2Polynomial(this.mDegree + 1);
        this.fieldPolynomial.setBit(0);
        this.fieldPolynomial.setBit(this.mDegree);
        boolean z = false;
        int i = 1;
        while (i <= this.mDegree - 3 && !z) {
            this.fieldPolynomial.setBit(i);
            int i2 = i + 1;
            boolean z2 = z;
            int i3 = i2;
            while (i3 <= this.mDegree - 2 && !z2) {
                this.fieldPolynomial.setBit(i3);
                int i4 = i3 + 1;
                boolean z3 = z2;
                for (int i5 = i4; i5 <= this.mDegree - 1 && !z3; i5++) {
                    this.fieldPolynomial.setBit(i5);
                    if ((((this.mDegree & 1) != 0) | ((i & 1) != 0) | ((i3 & 1) != 0)) || ((i5 & 1) != 0)) {
                        z3 = this.fieldPolynomial.isIrreducible();
                        if (z3) {
                            this.isPentanomial = true;
                            this.pc[0] = i;
                            this.pc[1] = i3;
                            this.pc[2] = i5;
                            return z3;
                        }
                    }
                    this.fieldPolynomial.resetBit(i5);
                }
                this.fieldPolynomial.resetBit(i3);
                i3 = i4;
                z2 = z3;
            }
            this.fieldPolynomial.resetBit(i);
            i = i2;
            z = z2;
        }
        return z;
    }

    private boolean testRandom() {
        this.fieldPolynomial = new GF2Polynomial(this.mDegree + 1);
        do {
            this.fieldPolynomial.randomize();
            this.fieldPolynomial.setBit(this.mDegree);
            this.fieldPolynomial.setBit(0);
        } while (!this.fieldPolynomial.isIrreducible());
        return true;
    }

    private boolean testTrinomials() {
        this.fieldPolynomial = new GF2Polynomial(this.mDegree + 1);
        boolean z = false;
        this.fieldPolynomial.setBit(0);
        this.fieldPolynomial.setBit(this.mDegree);
        for (int i = 1; i < this.mDegree && !z; i++) {
            this.fieldPolynomial.setBit(i);
            boolean isIrreducible = this.fieldPolynomial.isIrreducible();
            if (isIrreducible) {
                this.isTrinomial = true;
                this.tc = i;
                return isIrreducible;
            }
            this.fieldPolynomial.resetBit(i);
            z = this.fieldPolynomial.isIrreducible();
        }
        return z;
    }

    /* access modifiers changed from: protected */
    public void computeCOBMatrix(GF2nField gF2nField) {
        GF2nElement randomRoot;
        GF2nElement[] gF2nElementArr;
        if (this.mDegree == gF2nField.mDegree) {
            boolean z = gF2nField instanceof GF2nONBField;
            if (z) {
                gF2nField.computeCOBMatrix(this);
                return;
            }
            GF2Polynomial[] gF2PolynomialArr = new GF2Polynomial[this.mDegree];
            for (int i = 0; i < this.mDegree; i++) {
                gF2PolynomialArr[i] = new GF2Polynomial(this.mDegree);
            }
            do {
                randomRoot = gF2nField.getRandomRoot(this.fieldPolynomial);
            } while (randomRoot.isZero());
            if (randomRoot instanceof GF2nONBElement) {
                gF2nElementArr = new GF2nONBElement[this.mDegree];
                gF2nElementArr[this.mDegree - 1] = GF2nONBElement.ONE((GF2nONBField) gF2nField);
            } else {
                gF2nElementArr = new GF2nPolynomialElement[this.mDegree];
                gF2nElementArr[this.mDegree - 1] = GF2nPolynomialElement.ONE((GF2nPolynomialField) gF2nField);
            }
            gF2nElementArr[this.mDegree - 2] = randomRoot;
            for (int i2 = this.mDegree - 3; i2 >= 0; i2--) {
                gF2nElementArr[i2] = (GF2nElement) gF2nElementArr[i2 + 1].multiply(randomRoot);
            }
            if (z) {
                for (int i3 = 0; i3 < this.mDegree; i3++) {
                    for (int i4 = 0; i4 < this.mDegree; i4++) {
                        if (gF2nElementArr[i3].testBit((this.mDegree - i4) - 1)) {
                            gF2PolynomialArr[(this.mDegree - i4) - 1].setBit((this.mDegree - i3) - 1);
                        }
                    }
                }
            } else {
                for (int i5 = 0; i5 < this.mDegree; i5++) {
                    for (int i6 = 0; i6 < this.mDegree; i6++) {
                        if (gF2nElementArr[i5].testBit(i6)) {
                            gF2PolynomialArr[(this.mDegree - i6) - 1].setBit((this.mDegree - i5) - 1);
                        }
                    }
                }
            }
            this.fields.addElement(gF2nField);
            this.matrices.addElement(gF2PolynomialArr);
            gF2nField.fields.addElement(this);
            gF2nField.matrices.addElement(invertMatrix(gF2PolynomialArr));
            return;
        }
        throw new IllegalArgumentException("GF2nPolynomialField.computeCOBMatrix: B1 has a different degree and thus cannot be coverted to!");
    }

    /* access modifiers changed from: protected */
    public void computeFieldPolynomial() {
        if (!testTrinomials() && !testPentanomials()) {
            testRandom();
        }
    }

    /* access modifiers changed from: protected */
    public void computeFieldPolynomial2() {
        if (!testTrinomials() && !testPentanomials()) {
            testRandom();
        }
    }

    public int[] getPc() throws RuntimeException {
        if (this.isPentanomial) {
            int[] iArr = new int[3];
            System.arraycopy(this.pc, 0, iArr, 0, 3);
            return iArr;
        }
        throw new RuntimeException();
    }

    /* access modifiers changed from: protected */
    public GF2nElement getRandomRoot(GF2Polynomial gF2Polynomial) {
        GF2nPolynomial gcd;
        int degree;
        int degree2;
        GF2nPolynomial gF2nPolynomial = new GF2nPolynomial(gF2Polynomial, (GF2nField) this);
        while (gF2nPolynomial.getDegree() > 1) {
            while (true) {
                GF2nPolynomialElement gF2nPolynomialElement = new GF2nPolynomialElement(this, (Random) this.random);
                GF2nPolynomial gF2nPolynomial2 = new GF2nPolynomial(2, (GF2nElement) GF2nPolynomialElement.ZERO(this));
                gF2nPolynomial2.set(1, gF2nPolynomialElement);
                GF2nPolynomial gF2nPolynomial3 = new GF2nPolynomial(gF2nPolynomial2);
                for (int i = 1; i <= this.mDegree - 1; i++) {
                    gF2nPolynomial3 = gF2nPolynomial3.multiplyAndReduce(gF2nPolynomial3, gF2nPolynomial).add(gF2nPolynomial2);
                }
                gcd = gF2nPolynomial3.gcd(gF2nPolynomial);
                degree = gcd.getDegree();
                degree2 = gF2nPolynomial.getDegree();
                if (degree != 0 && degree != degree2) {
                    break;
                }
            }
            gF2nPolynomial = (degree << 1) > degree2 ? gF2nPolynomial.quotient(gcd) : new GF2nPolynomial(gcd);
        }
        return gF2nPolynomial.at(0);
    }

    public GF2Polynomial getSquaringVector(int i) {
        return new GF2Polynomial(this.squaringMatrix[i]);
    }

    public int getTc() throws RuntimeException {
        if (this.isTrinomial) {
            return this.tc;
        }
        throw new RuntimeException();
    }

    public boolean isPentanomial() {
        return this.isPentanomial;
    }

    public boolean isTrinomial() {
        return this.isTrinomial;
    }
}
