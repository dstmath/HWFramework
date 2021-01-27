package org.bouncycastle.pqc.math.linearalgebra;

import java.security.SecureRandom;

public class PolynomialGF2mSmallM {
    public static final char RANDOM_IRREDUCIBLE_POLYNOMIAL = 'I';
    private int[] coefficients;
    private int degree;
    private GF2mField field;

    public PolynomialGF2mSmallM(GF2mField gF2mField) {
        this.field = gF2mField;
        this.degree = -1;
        this.coefficients = new int[1];
    }

    public PolynomialGF2mSmallM(GF2mField gF2mField, int i) {
        this.field = gF2mField;
        this.degree = i;
        this.coefficients = new int[(i + 1)];
        this.coefficients[i] = 1;
    }

    public PolynomialGF2mSmallM(GF2mField gF2mField, int i, char c, SecureRandom secureRandom) {
        this.field = gF2mField;
        if (c == 'I') {
            this.coefficients = createRandomIrreduciblePolynomial(i, secureRandom);
            computeDegree();
            return;
        }
        throw new IllegalArgumentException(" Error: type " + c + " is not defined for GF2smallmPolynomial");
    }

    public PolynomialGF2mSmallM(GF2mField gF2mField, byte[] bArr) {
        this.field = gF2mField;
        int i = 8;
        int i2 = 1;
        while (gF2mField.getDegree() > i) {
            i2++;
            i += 8;
        }
        if (bArr.length % i2 == 0) {
            this.coefficients = new int[(bArr.length / i2)];
            int i3 = 0;
            int i4 = 0;
            while (true) {
                int[] iArr = this.coefficients;
                if (i3 < iArr.length) {
                    int i5 = i4;
                    int i6 = 0;
                    while (i6 < i) {
                        int[] iArr2 = this.coefficients;
                        iArr2[i3] = ((bArr[i5] & 255) << i6) ^ iArr2[i3];
                        i6 += 8;
                        i5++;
                    }
                    if (this.field.isElementOfThisField(this.coefficients[i3])) {
                        i3++;
                        i4 = i5;
                    } else {
                        throw new IllegalArgumentException(" Error: byte array is not encoded polynomial over given finite field GF2m");
                    }
                } else if (iArr.length == 1 || iArr[iArr.length - 1] != 0) {
                    computeDegree();
                    return;
                } else {
                    throw new IllegalArgumentException(" Error: byte array is not encoded polynomial over given finite field GF2m");
                }
            }
        } else {
            throw new IllegalArgumentException(" Error: byte array is not encoded polynomial over given finite field GF2m");
        }
    }

    public PolynomialGF2mSmallM(GF2mField gF2mField, int[] iArr) {
        this.field = gF2mField;
        this.coefficients = normalForm(iArr);
        computeDegree();
    }

    public PolynomialGF2mSmallM(GF2mVector gF2mVector) {
        this(gF2mVector.getField(), gF2mVector.getIntArrayForm());
    }

    public PolynomialGF2mSmallM(PolynomialGF2mSmallM polynomialGF2mSmallM) {
        this.field = polynomialGF2mSmallM.field;
        this.degree = polynomialGF2mSmallM.degree;
        this.coefficients = IntUtils.clone(polynomialGF2mSmallM.coefficients);
    }

    private int[] add(int[] iArr, int[] iArr2) {
        int[] iArr3;
        if (iArr.length < iArr2.length) {
            iArr3 = new int[iArr2.length];
            System.arraycopy(iArr2, 0, iArr3, 0, iArr2.length);
        } else {
            iArr3 = new int[iArr.length];
            System.arraycopy(iArr, 0, iArr3, 0, iArr.length);
            iArr = iArr2;
        }
        for (int length = iArr.length - 1; length >= 0; length--) {
            iArr3[length] = this.field.add(iArr3[length], iArr[length]);
        }
        return iArr3;
    }

    private static int computeDegree(int[] iArr) {
        int length = iArr.length - 1;
        while (length >= 0 && iArr[length] == 0) {
            length--;
        }
        return length;
    }

    private void computeDegree() {
        int length = this.coefficients.length;
        do {
            this.degree = length - 1;
            length = this.degree;
            if (length < 0) {
                return;
            }
        } while (this.coefficients[length] == 0);
    }

    private int[] createRandomIrreduciblePolynomial(int i, SecureRandom secureRandom) {
        int[] iArr = new int[(i + 1)];
        iArr[i] = 1;
        iArr[0] = this.field.getRandomNonZeroElement(secureRandom);
        for (int i2 = 1; i2 < i; i2++) {
            iArr[i2] = this.field.getRandomElement(secureRandom);
        }
        while (!isIrreducible(iArr)) {
            int nextInt = RandUtils.nextInt(secureRandom, i);
            if (nextInt == 0) {
                iArr[0] = this.field.getRandomNonZeroElement(secureRandom);
            } else {
                iArr[nextInt] = this.field.getRandomElement(secureRandom);
            }
        }
        return iArr;
    }

    private int[][] div(int[] iArr, int[] iArr2) {
        int computeDegree = computeDegree(iArr2);
        int computeDegree2 = computeDegree(iArr) + 1;
        if (computeDegree != -1) {
            int[][] iArr3 = {new int[1], new int[computeDegree2]};
            int inverse = this.field.inverse(headCoefficient(iArr2));
            iArr3[0][0] = 0;
            System.arraycopy(iArr, 0, iArr3[1], 0, iArr3[1].length);
            while (computeDegree <= computeDegree(iArr3[1])) {
                int[] iArr4 = {this.field.mult(headCoefficient(iArr3[1]), inverse)};
                int[] multWithElement = multWithElement(iArr2, iArr4[0]);
                int computeDegree3 = computeDegree(iArr3[1]) - computeDegree;
                int[] multWithMonomial = multWithMonomial(multWithElement, computeDegree3);
                iArr3[0] = add(multWithMonomial(iArr4, computeDegree3), iArr3[0]);
                iArr3[1] = add(multWithMonomial, iArr3[1]);
            }
            return iArr3;
        }
        throw new ArithmeticException("Division by zero.");
    }

    private int[] gcd(int[] iArr, int[] iArr2) {
        if (computeDegree(iArr) == -1) {
            return iArr2;
        }
        while (computeDegree(iArr2) != -1) {
            int[] mod = mod(iArr, iArr2);
            int[] iArr3 = new int[iArr2.length];
            System.arraycopy(iArr2, 0, iArr3, 0, iArr3.length);
            iArr2 = new int[mod.length];
            System.arraycopy(mod, 0, iArr2, 0, iArr2.length);
            iArr = iArr3;
        }
        return multWithElement(iArr, this.field.inverse(headCoefficient(iArr)));
    }

    private static int headCoefficient(int[] iArr) {
        int computeDegree = computeDegree(iArr);
        if (computeDegree == -1) {
            return 0;
        }
        return iArr[computeDegree];
    }

    private static boolean isEqual(int[] iArr, int[] iArr2) {
        int computeDegree = computeDegree(iArr);
        if (computeDegree != computeDegree(iArr2)) {
            return false;
        }
        for (int i = 0; i <= computeDegree; i++) {
            if (iArr[i] != iArr2[i]) {
                return false;
            }
        }
        return true;
    }

    private boolean isIrreducible(int[] iArr) {
        if (iArr[0] == 0) {
            return false;
        }
        int computeDegree = computeDegree(iArr) >> 1;
        int[] iArr2 = {0, 1};
        int degree2 = this.field.getDegree();
        int[] iArr3 = {0, 1};
        for (int i = 0; i < computeDegree; i++) {
            for (int i2 = degree2 - 1; i2 >= 0; i2--) {
                iArr3 = modMultiply(iArr3, iArr3, iArr);
            }
            iArr3 = normalForm(iArr3);
            if (computeDegree(gcd(add(iArr3, iArr2), iArr)) != 0) {
                return false;
            }
        }
        return true;
    }

    private int[] mod(int[] iArr, int[] iArr2) {
        int computeDegree = computeDegree(iArr2);
        if (computeDegree != -1) {
            int[] iArr3 = new int[iArr.length];
            int inverse = this.field.inverse(headCoefficient(iArr2));
            System.arraycopy(iArr, 0, iArr3, 0, iArr3.length);
            while (computeDegree <= computeDegree(iArr3)) {
                iArr3 = add(multWithElement(multWithMonomial(iArr2, computeDegree(iArr3) - computeDegree), this.field.mult(headCoefficient(iArr3), inverse)), iArr3);
            }
            return iArr3;
        }
        throw new ArithmeticException("Division by zero");
    }

    private int[] modDiv(int[] iArr, int[] iArr2, int[] iArr3) {
        int[] normalForm = normalForm(iArr3);
        int[] mod = mod(iArr2, iArr3);
        int[] iArr4 = {0};
        int[] mod2 = mod(iArr, iArr3);
        while (computeDegree(mod) != -1) {
            int[][] div = div(normalForm, mod);
            int[] normalForm2 = normalForm(mod);
            int[] normalForm3 = normalForm(div[1]);
            int[] add = add(iArr4, modMultiply(div[0], mod2, iArr3));
            iArr4 = normalForm(mod2);
            mod2 = normalForm(add);
            normalForm = normalForm2;
            mod = normalForm3;
        }
        return multWithElement(iArr4, this.field.inverse(headCoefficient(normalForm)));
    }

    private int[] modMultiply(int[] iArr, int[] iArr2, int[] iArr3) {
        return mod(multiply(iArr, iArr2), iArr3);
    }

    private int[] multWithElement(int[] iArr, int i) {
        int computeDegree = computeDegree(iArr);
        if (computeDegree == -1 || i == 0) {
            return new int[1];
        }
        if (i == 1) {
            return IntUtils.clone(iArr);
        }
        int[] iArr2 = new int[(computeDegree + 1)];
        while (computeDegree >= 0) {
            iArr2[computeDegree] = this.field.mult(iArr[computeDegree], i);
            computeDegree--;
        }
        return iArr2;
    }

    private static int[] multWithMonomial(int[] iArr, int i) {
        int computeDegree = computeDegree(iArr);
        if (computeDegree == -1) {
            return new int[1];
        }
        int[] iArr2 = new int[(computeDegree + i + 1)];
        System.arraycopy(iArr, 0, iArr2, i, computeDegree + 1);
        return iArr2;
    }

    private int[] multiply(int[] iArr, int[] iArr2) {
        if (computeDegree(iArr) < computeDegree(iArr2)) {
            iArr2 = iArr;
            iArr = iArr2;
        }
        int[] normalForm = normalForm(iArr);
        int[] normalForm2 = normalForm(iArr2);
        if (normalForm2.length == 1) {
            return multWithElement(normalForm, normalForm2[0]);
        }
        int length = normalForm.length;
        int length2 = normalForm2.length;
        int[] iArr3 = new int[((length + length2) - 1)];
        if (length2 != length) {
            int[] iArr4 = new int[length2];
            int[] iArr5 = new int[(length - length2)];
            System.arraycopy(normalForm, 0, iArr4, 0, iArr4.length);
            System.arraycopy(normalForm, length2, iArr5, 0, iArr5.length);
            return add(multiply(iArr4, normalForm2), multWithMonomial(multiply(iArr5, normalForm2), length2));
        }
        int i = (length + 1) >>> 1;
        int i2 = length - i;
        int[] iArr6 = new int[i];
        int[] iArr7 = new int[i];
        int[] iArr8 = new int[i2];
        int[] iArr9 = new int[i2];
        System.arraycopy(normalForm, 0, iArr6, 0, iArr6.length);
        System.arraycopy(normalForm, i, iArr8, 0, iArr8.length);
        System.arraycopy(normalForm2, 0, iArr7, 0, iArr7.length);
        System.arraycopy(normalForm2, i, iArr9, 0, iArr9.length);
        int[] add = add(iArr6, iArr8);
        int[] add2 = add(iArr7, iArr9);
        int[] multiply = multiply(iArr6, iArr7);
        int[] multiply2 = multiply(add, add2);
        int[] multiply3 = multiply(iArr8, iArr9);
        return add(multWithMonomial(add(add(add(multiply2, multiply), multiply3), multWithMonomial(multiply3, i)), i), multiply);
    }

    private static int[] normalForm(int[] iArr) {
        int computeDegree = computeDegree(iArr);
        if (computeDegree == -1) {
            return new int[1];
        }
        int i = computeDegree + 1;
        if (iArr.length == i) {
            return IntUtils.clone(iArr);
        }
        int[] iArr2 = new int[i];
        System.arraycopy(iArr, 0, iArr2, 0, i);
        return iArr2;
    }

    public PolynomialGF2mSmallM add(PolynomialGF2mSmallM polynomialGF2mSmallM) {
        return new PolynomialGF2mSmallM(this.field, add(this.coefficients, polynomialGF2mSmallM.coefficients));
    }

    public PolynomialGF2mSmallM addMonomial(int i) {
        int[] iArr = new int[(i + 1)];
        iArr[i] = 1;
        return new PolynomialGF2mSmallM(this.field, add(this.coefficients, iArr));
    }

    public void addToThis(PolynomialGF2mSmallM polynomialGF2mSmallM) {
        this.coefficients = add(this.coefficients, polynomialGF2mSmallM.coefficients);
        computeDegree();
    }

    public PolynomialGF2mSmallM[] div(PolynomialGF2mSmallM polynomialGF2mSmallM) {
        int[][] div = div(this.coefficients, polynomialGF2mSmallM.coefficients);
        return new PolynomialGF2mSmallM[]{new PolynomialGF2mSmallM(this.field, div[0]), new PolynomialGF2mSmallM(this.field, div[1])};
    }

    public boolean equals(Object obj) {
        if (obj != null && (obj instanceof PolynomialGF2mSmallM)) {
            PolynomialGF2mSmallM polynomialGF2mSmallM = (PolynomialGF2mSmallM) obj;
            if (this.field.equals(polynomialGF2mSmallM.field) && this.degree == polynomialGF2mSmallM.degree && isEqual(this.coefficients, polynomialGF2mSmallM.coefficients)) {
                return true;
            }
        }
        return false;
    }

    public int evaluateAt(int i) {
        int[] iArr = this.coefficients;
        int i2 = this.degree;
        int i3 = iArr[i2];
        for (int i4 = i2 - 1; i4 >= 0; i4--) {
            i3 = this.field.mult(i3, i) ^ this.coefficients[i4];
        }
        return i3;
    }

    public PolynomialGF2mSmallM gcd(PolynomialGF2mSmallM polynomialGF2mSmallM) {
        return new PolynomialGF2mSmallM(this.field, gcd(this.coefficients, polynomialGF2mSmallM.coefficients));
    }

    public int getCoefficient(int i) {
        if (i < 0 || i > this.degree) {
            return 0;
        }
        return this.coefficients[i];
    }

    public int getDegree() {
        int[] iArr = this.coefficients;
        int length = iArr.length - 1;
        if (iArr[length] == 0) {
            return -1;
        }
        return length;
    }

    public byte[] getEncoded() {
        int i = 8;
        int i2 = 1;
        while (this.field.getDegree() > i) {
            i2++;
            i += 8;
        }
        byte[] bArr = new byte[(this.coefficients.length * i2)];
        int i3 = 0;
        int i4 = 0;
        while (i3 < this.coefficients.length) {
            int i5 = i4;
            int i6 = 0;
            while (i6 < i) {
                bArr[i5] = (byte) (this.coefficients[i3] >>> i6);
                i6 += 8;
                i5++;
            }
            i3++;
            i4 = i5;
        }
        return bArr;
    }

    public int getHeadCoefficient() {
        int i = this.degree;
        if (i == -1) {
            return 0;
        }
        return this.coefficients[i];
    }

    public int hashCode() {
        int hashCode = this.field.hashCode();
        int i = 0;
        while (true) {
            int[] iArr = this.coefficients;
            if (i >= iArr.length) {
                return hashCode;
            }
            hashCode = (hashCode * 31) + iArr[i];
            i++;
        }
    }

    public PolynomialGF2mSmallM mod(PolynomialGF2mSmallM polynomialGF2mSmallM) {
        return new PolynomialGF2mSmallM(this.field, mod(this.coefficients, polynomialGF2mSmallM.coefficients));
    }

    public PolynomialGF2mSmallM modDiv(PolynomialGF2mSmallM polynomialGF2mSmallM, PolynomialGF2mSmallM polynomialGF2mSmallM2) {
        return new PolynomialGF2mSmallM(this.field, modDiv(this.coefficients, polynomialGF2mSmallM.coefficients, polynomialGF2mSmallM2.coefficients));
    }

    public PolynomialGF2mSmallM modInverse(PolynomialGF2mSmallM polynomialGF2mSmallM) {
        return new PolynomialGF2mSmallM(this.field, modDiv(new int[]{1}, this.coefficients, polynomialGF2mSmallM.coefficients));
    }

    public PolynomialGF2mSmallM modMultiply(PolynomialGF2mSmallM polynomialGF2mSmallM, PolynomialGF2mSmallM polynomialGF2mSmallM2) {
        return new PolynomialGF2mSmallM(this.field, modMultiply(this.coefficients, polynomialGF2mSmallM.coefficients, polynomialGF2mSmallM2.coefficients));
    }

    public PolynomialGF2mSmallM[] modPolynomialToFracton(PolynomialGF2mSmallM polynomialGF2mSmallM) {
        int i = polynomialGF2mSmallM.degree >> 1;
        int[] normalForm = normalForm(polynomialGF2mSmallM.coefficients);
        int[] mod = mod(this.coefficients, polynomialGF2mSmallM.coefficients);
        int[] iArr = {0};
        int[] iArr2 = {1};
        while (computeDegree(mod) > i) {
            int[][] div = div(normalForm, mod);
            int[] iArr3 = div[1];
            int[] add = add(iArr, modMultiply(div[0], iArr2, polynomialGF2mSmallM.coefficients));
            iArr = iArr2;
            iArr2 = add;
            normalForm = mod;
            mod = iArr3;
        }
        return new PolynomialGF2mSmallM[]{new PolynomialGF2mSmallM(this.field, mod), new PolynomialGF2mSmallM(this.field, iArr2)};
    }

    public PolynomialGF2mSmallM modSquareMatrix(PolynomialGF2mSmallM[] polynomialGF2mSmallMArr) {
        int length = polynomialGF2mSmallMArr.length;
        int[] iArr = new int[length];
        int[] iArr2 = new int[length];
        int i = 0;
        while (true) {
            int[] iArr3 = this.coefficients;
            if (i >= iArr3.length) {
                break;
            }
            iArr2[i] = this.field.mult(iArr3[i], iArr3[i]);
            i++;
        }
        for (int i2 = 0; i2 < length; i2++) {
            for (int i3 = 0; i3 < length; i3++) {
                if (i2 < polynomialGF2mSmallMArr[i3].coefficients.length) {
                    iArr[i2] = this.field.add(iArr[i2], this.field.mult(polynomialGF2mSmallMArr[i3].coefficients[i2], iArr2[i3]));
                }
            }
        }
        return new PolynomialGF2mSmallM(this.field, iArr);
    }

    public PolynomialGF2mSmallM modSquareRoot(PolynomialGF2mSmallM polynomialGF2mSmallM) {
        int[] clone = IntUtils.clone(this.coefficients);
        while (true) {
            int[] modMultiply = modMultiply(clone, clone, polynomialGF2mSmallM.coefficients);
            if (isEqual(modMultiply, this.coefficients)) {
                return new PolynomialGF2mSmallM(this.field, clone);
            }
            clone = normalForm(modMultiply);
        }
    }

    public PolynomialGF2mSmallM modSquareRootMatrix(PolynomialGF2mSmallM[] polynomialGF2mSmallMArr) {
        int length = polynomialGF2mSmallMArr.length;
        int[] iArr = new int[length];
        for (int i = 0; i < length; i++) {
            for (int i2 = 0; i2 < length; i2++) {
                if (i < polynomialGF2mSmallMArr[i2].coefficients.length) {
                    int[] iArr2 = this.coefficients;
                    if (i2 < iArr2.length) {
                        iArr[i] = this.field.add(iArr[i], this.field.mult(polynomialGF2mSmallMArr[i2].coefficients[i], iArr2[i2]));
                    }
                }
            }
        }
        for (int i3 = 0; i3 < length; i3++) {
            iArr[i3] = this.field.sqRoot(iArr[i3]);
        }
        return new PolynomialGF2mSmallM(this.field, iArr);
    }

    public void multThisWithElement(int i) {
        if (this.field.isElementOfThisField(i)) {
            this.coefficients = multWithElement(this.coefficients, i);
            computeDegree();
            return;
        }
        throw new ArithmeticException("Not an element of the finite field this polynomial is defined over.");
    }

    public PolynomialGF2mSmallM multWithElement(int i) {
        if (this.field.isElementOfThisField(i)) {
            return new PolynomialGF2mSmallM(this.field, multWithElement(this.coefficients, i));
        }
        throw new ArithmeticException("Not an element of the finite field this polynomial is defined over.");
    }

    public PolynomialGF2mSmallM multWithMonomial(int i) {
        return new PolynomialGF2mSmallM(this.field, multWithMonomial(this.coefficients, i));
    }

    public PolynomialGF2mSmallM multiply(PolynomialGF2mSmallM polynomialGF2mSmallM) {
        return new PolynomialGF2mSmallM(this.field, multiply(this.coefficients, polynomialGF2mSmallM.coefficients));
    }

    public String toString() {
        String str = " Polynomial over " + this.field.toString() + ": \n";
        for (int i = 0; i < this.coefficients.length; i++) {
            str = str + this.field.elementToStr(this.coefficients[i]) + "Y^" + i + "+";
        }
        return str + ";";
    }
}
