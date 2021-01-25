package ohos.utils;

import java.util.Objects;
import ohos.media.camera.mode.adapter.utils.constant.ConstantValue;

public final class RationalNumber extends Number implements Comparable<RationalNumber> {
    private static final long serialVersionUID = 1;
    private final int denominatorSize;
    private final int numeratorSize;

    public static int getCommonDivisor(int i, int i2) {
        int abs = Math.abs(i);
        int abs2 = Math.abs(i2);
        while (true) {
            abs = abs2;
            if (abs == 0) {
                return abs;
            }
            abs2 = abs % abs;
        }
    }

    public RationalNumber(int i, int i2) {
        if (i == 0 && i2 == 0) {
            this.numeratorSize = 0;
            this.denominatorSize = 0;
        } else if (i == 0 && i2 != 0) {
            this.numeratorSize = 0;
            this.denominatorSize = 1;
        } else if (i2 == 0 && i > 0) {
            this.numeratorSize = 1;
            this.denominatorSize = 0;
        } else if (i2 != 0 || i >= 0) {
            int commonDivisor = getCommonDivisor(i, i2);
            this.numeratorSize = i / commonDivisor;
            this.denominatorSize = i2 / commonDivisor;
        } else {
            this.numeratorSize = -1;
            this.denominatorSize = 0;
        }
    }

    @Override // java.lang.Object
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof RationalNumber)) {
            return false;
        }
        RationalNumber rationalNumber = (RationalNumber) obj;
        if (rationalNumber.getNumerator() == this.numeratorSize && rationalNumber.getDenominator() == this.denominatorSize) {
            return true;
        }
        return false;
    }

    @Override // java.lang.Object
    public int hashCode() {
        return Objects.hash(Integer.valueOf(this.numeratorSize), Integer.valueOf(this.denominatorSize));
    }

    @Override // java.lang.Object
    public String toString() {
        if (isNaN()) {
            return "NaN";
        }
        if (this.numeratorSize > 0 && this.denominatorSize == 0) {
            return "PositiveInfinity";
        }
        if (this.numeratorSize < 0 && this.denominatorSize == 0) {
            return "NegativeInfinity";
        }
        return this.numeratorSize + "/" + this.denominatorSize;
    }

    @Override // java.lang.Number
    public double doubleValue() {
        if (isPositiveInfinity()) {
            return Double.MAX_VALUE;
        }
        if (isNegativeInfinity()) {
            return Double.MIN_VALUE;
        }
        if (isNaN()) {
            return 0.0d;
        }
        return ((double) this.numeratorSize) / ((double) this.denominatorSize);
    }

    @Override // java.lang.Number
    public float floatValue() {
        if (isPositiveInfinity()) {
            return Float.MAX_VALUE;
        }
        if (isNegativeInfinity()) {
            return Float.MIN_VALUE;
        }
        if (isNaN()) {
            return ConstantValue.MIN_ZOOM_VALUE;
        }
        return ((float) this.numeratorSize) / ((float) this.denominatorSize);
    }

    @Override // java.lang.Number
    public int intValue() {
        if (isPositiveInfinity()) {
            return Integer.MAX_VALUE;
        }
        if (isNegativeInfinity()) {
            return Integer.MIN_VALUE;
        }
        if (isNaN()) {
            return 0;
        }
        return this.numeratorSize / this.denominatorSize;
    }

    @Override // java.lang.Number
    public long longValue() {
        if (isPositiveInfinity()) {
            return Long.MAX_VALUE;
        }
        if (isNegativeInfinity()) {
            return Long.MIN_VALUE;
        }
        if (isNaN()) {
            return 0;
        }
        return (long) (this.numeratorSize / this.denominatorSize);
    }

    public int getDenominator() {
        return this.denominatorSize;
    }

    public int getNumerator() {
        return this.numeratorSize;
    }

    public boolean isFinite() {
        return this.denominatorSize != 0;
    }

    public boolean isInfinite() {
        return this.denominatorSize == 0 && this.numeratorSize != 0;
    }

    public boolean isNaN() {
        return this.numeratorSize == 0 && this.denominatorSize == 0;
    }

    public boolean isZero() {
        return this.numeratorSize == 0 && this.denominatorSize != 0;
    }

    private boolean isPositiveInfinity() {
        return this.numeratorSize > 0 && this.denominatorSize == 0;
    }

    private boolean isNegativeInfinity() {
        return this.numeratorSize < 0 && this.denominatorSize == 0;
    }

    public static RationalNumber createRationalFromString(String str) throws NumberFormatException {
        Objects.requireNonNull(str, "Argument rationalString must not be null");
        if (str.equals("NaN")) {
            return new RationalNumber(0, 0);
        }
        if (str.equals("PositiveInfinity")) {
            return new RationalNumber(1, 0);
        }
        if (str.equals("NegativeInfinity")) {
            return new RationalNumber(-1, 0);
        }
        int indexOf = str.indexOf(47);
        if (indexOf >= 0) {
            try {
                return new RationalNumber(Integer.parseInt(str.substring(0, indexOf)), Integer.parseInt(str.substring(indexOf + 1)));
            } catch (NumberFormatException unused) {
                throw new NumberFormatException("Invalid RationalNum string: " + str);
            }
        } else {
            throw new NumberFormatException("Invalid RationalNum string: " + str);
        }
    }

    public int compareTo(RationalNumber rationalNumber) {
        Objects.requireNonNull(rationalNumber, "Argument another must not be null");
        if (equals(rationalNumber)) {
            return 0;
        }
        if (rationalNumber.isNaN()) {
            return -1;
        }
        if (isNaN()) {
            return 1;
        }
        if (isNegativeInfinity() && rationalNumber.isPositiveInfinity()) {
            return -1;
        }
        if (isPositiveInfinity() && rationalNumber.isNegativeInfinity()) {
            return 1;
        }
        int i = this.denominatorSize < 0 ? -this.numeratorSize : this.numeratorSize;
        int i2 = this.denominatorSize;
        if (i2 < 0) {
            i2 = -i2;
        }
        int i3 = rationalNumber.denominatorSize < 0 ? -rationalNumber.numeratorSize : rationalNumber.numeratorSize;
        int i4 = rationalNumber.denominatorSize;
        if (i4 < 0) {
            i4 = -i4;
        }
        int i5 = ((((long) i) * ((long) i4)) > (((long) i3) * ((long) i2)) ? 1 : ((((long) i) * ((long) i4)) == (((long) i3) * ((long) i2)) ? 0 : -1));
        if (i5 < 0) {
            return -1;
        }
        if (i5 > 0) {
            return 1;
        }
        return 0;
    }
}
