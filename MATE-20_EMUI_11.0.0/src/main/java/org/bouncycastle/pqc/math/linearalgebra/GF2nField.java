package org.bouncycastle.pqc.math.linearalgebra;

import java.security.SecureRandom;
import java.util.Vector;

public abstract class GF2nField {
    protected GF2Polynomial fieldPolynomial;
    protected Vector fields;
    protected int mDegree;
    protected Vector matrices;
    protected final SecureRandom random;

    protected GF2nField(SecureRandom secureRandom) {
        this.random = secureRandom;
    }

    /* access modifiers changed from: protected */
    public abstract void computeCOBMatrix(GF2nField gF2nField);

    /* access modifiers changed from: protected */
    public abstract void computeFieldPolynomial();

    public final GF2nElement convert(GF2nElement gF2nElement, GF2nField gF2nField) throws RuntimeException {
        if (gF2nField == this || this.fieldPolynomial.equals(gF2nField.fieldPolynomial)) {
            return (GF2nElement) gF2nElement.clone();
        }
        if (this.mDegree == gF2nField.mDegree) {
            int indexOf = this.fields.indexOf(gF2nField);
            if (indexOf == -1) {
                computeCOBMatrix(gF2nField);
                indexOf = this.fields.indexOf(gF2nField);
            }
            GF2Polynomial[] gF2PolynomialArr = (GF2Polynomial[]) this.matrices.elementAt(indexOf);
            GF2nElement gF2nElement2 = (GF2nElement) gF2nElement.clone();
            if (gF2nElement2 instanceof GF2nONBElement) {
                ((GF2nONBElement) gF2nElement2).reverseOrder();
            }
            GF2Polynomial gF2Polynomial = new GF2Polynomial(this.mDegree, gF2nElement2.toFlexiBigInt());
            gF2Polynomial.expandN(this.mDegree);
            GF2Polynomial gF2Polynomial2 = new GF2Polynomial(this.mDegree);
            for (int i = 0; i < this.mDegree; i++) {
                if (gF2Polynomial.vectorMult(gF2PolynomialArr[i])) {
                    gF2Polynomial2.setBit((this.mDegree - 1) - i);
                }
            }
            if (gF2nField instanceof GF2nPolynomialField) {
                return new GF2nPolynomialElement((GF2nPolynomialField) gF2nField, gF2Polynomial2);
            }
            if (gF2nField instanceof GF2nONBField) {
                GF2nONBElement gF2nONBElement = new GF2nONBElement((GF2nONBField) gF2nField, gF2Polynomial2.toFlexiBigInt());
                gF2nONBElement.reverseOrder();
                return gF2nONBElement;
            }
            throw new RuntimeException("GF2nField.convert: B1 must be an instance of GF2nPolynomialField or GF2nONBField!");
        }
        throw new RuntimeException("GF2nField.convert: B1 has a different degree and thus cannot be coverted to!");
    }

    public final boolean equals(Object obj) {
        if (obj == null || !(obj instanceof GF2nField)) {
            return false;
        }
        GF2nField gF2nField = (GF2nField) obj;
        if (gF2nField.mDegree != this.mDegree || !this.fieldPolynomial.equals(gF2nField.fieldPolynomial)) {
            return false;
        }
        if (!(this instanceof GF2nPolynomialField) || (gF2nField instanceof GF2nPolynomialField)) {
            return !(this instanceof GF2nONBField) || (gF2nField instanceof GF2nONBField);
        }
        return false;
    }

    public final int getDegree() {
        return this.mDegree;
    }

    public final GF2Polynomial getFieldPolynomial() {
        if (this.fieldPolynomial == null) {
            computeFieldPolynomial();
        }
        return new GF2Polynomial(this.fieldPolynomial);
    }

    /* access modifiers changed from: protected */
    public abstract GF2nElement getRandomRoot(GF2Polynomial gF2Polynomial);

    public int hashCode() {
        return this.mDegree + this.fieldPolynomial.hashCode();
    }

    /* access modifiers changed from: protected */
    public final GF2Polynomial[] invertMatrix(GF2Polynomial[] gF2PolynomialArr) {
        GF2Polynomial[] gF2PolynomialArr2 = new GF2Polynomial[gF2PolynomialArr.length];
        GF2Polynomial[] gF2PolynomialArr3 = new GF2Polynomial[gF2PolynomialArr.length];
        int i = 0;
        for (int i2 = 0; i2 < this.mDegree; i2++) {
            gF2PolynomialArr2[i2] = new GF2Polynomial(gF2PolynomialArr[i2]);
            gF2PolynomialArr3[i2] = new GF2Polynomial(this.mDegree);
            gF2PolynomialArr3[i2].setBit((this.mDegree - 1) - i2);
        }
        while (true) {
            int i3 = this.mDegree;
            if (i < i3 - 1) {
                int i4 = i;
                while (true) {
                    int i5 = this.mDegree;
                    if (i4 >= i5 || gF2PolynomialArr2[i4].testBit((i5 - 1) - i)) {
                        break;
                    }
                    i4++;
                }
                if (i4 < this.mDegree) {
                    if (i != i4) {
                        GF2Polynomial gF2Polynomial = gF2PolynomialArr2[i];
                        gF2PolynomialArr2[i] = gF2PolynomialArr2[i4];
                        gF2PolynomialArr2[i4] = gF2Polynomial;
                        GF2Polynomial gF2Polynomial2 = gF2PolynomialArr3[i];
                        gF2PolynomialArr3[i] = gF2PolynomialArr3[i4];
                        gF2PolynomialArr3[i4] = gF2Polynomial2;
                    }
                    int i6 = i + 1;
                    int i7 = i6;
                    while (true) {
                        int i8 = this.mDegree;
                        if (i7 >= i8) {
                            break;
                        }
                        if (gF2PolynomialArr2[i7].testBit((i8 - 1) - i)) {
                            gF2PolynomialArr2[i7].addToThis(gF2PolynomialArr2[i]);
                            gF2PolynomialArr3[i7].addToThis(gF2PolynomialArr3[i]);
                        }
                        i7++;
                    }
                    i = i6;
                } else {
                    throw new RuntimeException("GF2nField.invertMatrix: Matrix cannot be inverted!");
                }
            } else {
                for (int i9 = i3 - 1; i9 > 0; i9--) {
                    for (int i10 = i9 - 1; i10 >= 0; i10--) {
                        if (gF2PolynomialArr2[i10].testBit((this.mDegree - 1) - i9)) {
                            gF2PolynomialArr2[i10].addToThis(gF2PolynomialArr2[i9]);
                            gF2PolynomialArr3[i10].addToThis(gF2PolynomialArr3[i9]);
                        }
                    }
                }
                return gF2PolynomialArr3;
            }
        }
    }
}
