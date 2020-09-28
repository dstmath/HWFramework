package com.huawei.zxing.pdf417.decoder.ec;

/* access modifiers changed from: package-private */
public final class ModulusPoly {
    private final int[] coefficients;
    private final ModulusGF field;

    ModulusPoly(ModulusGF field2, int[] coefficients2) {
        if (coefficients2.length != 0) {
            this.field = field2;
            int coefficientsLength = coefficients2.length;
            if (coefficientsLength <= 1 || coefficients2[0] != 0) {
                this.coefficients = coefficients2;
                return;
            }
            int firstNonZero = 1;
            while (firstNonZero < coefficientsLength && coefficients2[firstNonZero] == 0) {
                firstNonZero++;
            }
            if (firstNonZero == coefficientsLength) {
                this.coefficients = field2.getZero().coefficients;
                return;
            }
            this.coefficients = new int[(coefficientsLength - firstNonZero)];
            int[] iArr = this.coefficients;
            System.arraycopy(coefficients2, firstNonZero, iArr, 0, iArr.length);
            return;
        }
        throw new IllegalArgumentException();
    }

    /* access modifiers changed from: package-private */
    public int[] getCoefficients() {
        return this.coefficients;
    }

    /* access modifiers changed from: package-private */
    public int getDegree() {
        return this.coefficients.length - 1;
    }

    /* access modifiers changed from: package-private */
    public boolean isZero() {
        return this.coefficients[0] == 0;
    }

    /* access modifiers changed from: package-private */
    public int getCoefficient(int degree) {
        int[] iArr = this.coefficients;
        return iArr[(iArr.length - 1) - degree];
    }

    /* access modifiers changed from: package-private */
    public int evaluateAt(int a) {
        if (a == 0) {
            return getCoefficient(0);
        }
        int[] iArr = this.coefficients;
        int size = iArr.length;
        if (a == 1) {
            int result = 0;
            for (int coefficient : iArr) {
                result = this.field.add(result, coefficient);
            }
            return result;
        }
        int result2 = iArr[0];
        for (int i = 1; i < size; i++) {
            ModulusGF modulusGF = this.field;
            result2 = modulusGF.add(modulusGF.multiply(a, result2), this.coefficients[i]);
        }
        return result2;
    }

    /* access modifiers changed from: package-private */
    public ModulusPoly add(ModulusPoly other) {
        if (!this.field.equals(other.field)) {
            throw new IllegalArgumentException("ModulusPolys do not have same ModulusGF field");
        } else if (isZero()) {
            return other;
        } else {
            if (other.isZero()) {
                return this;
            }
            int[] smallerCoefficients = this.coefficients;
            int[] largerCoefficients = other.coefficients;
            if (smallerCoefficients.length > largerCoefficients.length) {
                smallerCoefficients = largerCoefficients;
                largerCoefficients = smallerCoefficients;
            }
            int[] sumDiff = new int[largerCoefficients.length];
            int lengthDiff = largerCoefficients.length - smallerCoefficients.length;
            System.arraycopy(largerCoefficients, 0, sumDiff, 0, lengthDiff);
            for (int i = lengthDiff; i < largerCoefficients.length; i++) {
                sumDiff[i] = this.field.add(smallerCoefficients[i - lengthDiff], largerCoefficients[i]);
            }
            return new ModulusPoly(this.field, sumDiff);
        }
    }

    /* access modifiers changed from: package-private */
    public ModulusPoly subtract(ModulusPoly other) {
        if (!this.field.equals(other.field)) {
            throw new IllegalArgumentException("ModulusPolys do not have same ModulusGF field");
        } else if (other.isZero()) {
            return this;
        } else {
            return add(other.negative());
        }
    }

    /* access modifiers changed from: package-private */
    public ModulusPoly multiply(ModulusPoly other) {
        if (!this.field.equals(other.field)) {
            throw new IllegalArgumentException("ModulusPolys do not have same ModulusGF field");
        } else if (isZero() || other.isZero()) {
            return this.field.getZero();
        } else {
            int[] aCoefficients = this.coefficients;
            int aLength = aCoefficients.length;
            int[] bCoefficients = other.coefficients;
            int bLength = bCoefficients.length;
            int[] product = new int[((aLength + bLength) - 1)];
            for (int i = 0; i < aLength; i++) {
                int aCoeff = aCoefficients[i];
                for (int j = 0; j < bLength; j++) {
                    ModulusGF modulusGF = this.field;
                    product[i + j] = modulusGF.add(product[i + j], modulusGF.multiply(aCoeff, bCoefficients[j]));
                }
            }
            return new ModulusPoly(this.field, product);
        }
    }

    /* access modifiers changed from: package-private */
    public ModulusPoly negative() {
        int size = this.coefficients.length;
        int[] negativeCoefficients = new int[size];
        for (int i = 0; i < size; i++) {
            negativeCoefficients[i] = this.field.subtract(0, this.coefficients[i]);
        }
        return new ModulusPoly(this.field, negativeCoefficients);
    }

    /* access modifiers changed from: package-private */
    public ModulusPoly multiply(int scalar) {
        if (scalar == 0) {
            return this.field.getZero();
        }
        if (scalar == 1) {
            return this;
        }
        int size = this.coefficients.length;
        int[] product = new int[size];
        for (int i = 0; i < size; i++) {
            product[i] = this.field.multiply(this.coefficients[i], scalar);
        }
        return new ModulusPoly(this.field, product);
    }

    /* access modifiers changed from: package-private */
    public ModulusPoly multiplyByMonomial(int degree, int coefficient) {
        if (degree < 0) {
            throw new IllegalArgumentException();
        } else if (coefficient == 0) {
            return this.field.getZero();
        } else {
            int size = this.coefficients.length;
            int[] product = new int[(size + degree)];
            for (int i = 0; i < size; i++) {
                product[i] = this.field.multiply(this.coefficients[i], coefficient);
            }
            return new ModulusPoly(this.field, product);
        }
    }

    /* access modifiers changed from: package-private */
    public ModulusPoly[] divide(ModulusPoly other) {
        if (!this.field.equals(other.field)) {
            throw new IllegalArgumentException("ModulusPolys do not have same ModulusGF field");
        } else if (!other.isZero()) {
            ModulusPoly quotient = this.field.getZero();
            ModulusPoly remainder = this;
            int inverseDenominatorLeadingTerm = this.field.inverse(other.getCoefficient(other.getDegree()));
            while (remainder.getDegree() >= other.getDegree() && !remainder.isZero()) {
                int degreeDifference = remainder.getDegree() - other.getDegree();
                int scale = this.field.multiply(remainder.getCoefficient(remainder.getDegree()), inverseDenominatorLeadingTerm);
                ModulusPoly term = other.multiplyByMonomial(degreeDifference, scale);
                quotient = quotient.add(this.field.buildMonomial(degreeDifference, scale));
                remainder = remainder.subtract(term);
            }
            return new ModulusPoly[]{quotient, remainder};
        } else {
            throw new IllegalArgumentException("Divide by 0");
        }
    }

    public String toString() {
        StringBuilder result = new StringBuilder(getDegree() * 8);
        for (int degree = getDegree(); degree >= 0; degree--) {
            int coefficient = getCoefficient(degree);
            if (coefficient != 0) {
                if (coefficient < 0) {
                    result.append(" - ");
                    coefficient = -coefficient;
                } else if (result.length() > 0) {
                    result.append(" + ");
                }
                if (degree == 0 || coefficient != 1) {
                    result.append(coefficient);
                }
                if (degree != 0) {
                    if (degree == 1) {
                        result.append('x');
                    } else {
                        result.append("x^");
                        result.append(degree);
                    }
                }
            }
        }
        return result.toString();
    }
}
