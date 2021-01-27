package android.util;

import android.annotation.UnsupportedAppUsage;
import com.android.internal.util.Preconditions;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;

public final class Rational extends Number implements Comparable<Rational> {
    public static final Rational NEGATIVE_INFINITY = new Rational(-1, 0);
    public static final Rational NaN = new Rational(0, 0);
    public static final Rational POSITIVE_INFINITY = new Rational(1, 0);
    public static final Rational ZERO = new Rational(0, 1);
    private static final long serialVersionUID = 1;
    @UnsupportedAppUsage
    private final int mDenominator;
    @UnsupportedAppUsage
    private final int mNumerator;

    public Rational(int numerator, int denominator) {
        if (denominator < 0) {
            numerator = -numerator;
            denominator = -denominator;
        }
        if (denominator == 0 && numerator > 0) {
            this.mNumerator = 1;
            this.mDenominator = 0;
        } else if (denominator == 0 && numerator < 0) {
            this.mNumerator = -1;
            this.mDenominator = 0;
        } else if (denominator == 0 && numerator == 0) {
            this.mNumerator = 0;
            this.mDenominator = 0;
        } else if (numerator == 0) {
            this.mNumerator = 0;
            this.mDenominator = 1;
        } else {
            int gcd = gcd(numerator, denominator);
            this.mNumerator = numerator / gcd;
            this.mDenominator = denominator / gcd;
        }
    }

    public int getNumerator() {
        return this.mNumerator;
    }

    public int getDenominator() {
        return this.mDenominator;
    }

    public boolean isNaN() {
        return this.mDenominator == 0 && this.mNumerator == 0;
    }

    public boolean isInfinite() {
        return this.mNumerator != 0 && this.mDenominator == 0;
    }

    public boolean isFinite() {
        return this.mDenominator != 0;
    }

    public boolean isZero() {
        return isFinite() && this.mNumerator == 0;
    }

    private boolean isPosInf() {
        return this.mDenominator == 0 && this.mNumerator > 0;
    }

    private boolean isNegInf() {
        return this.mDenominator == 0 && this.mNumerator < 0;
    }

    @Override // java.lang.Object
    public boolean equals(Object obj) {
        return (obj instanceof Rational) && equals((Rational) obj);
    }

    private boolean equals(Rational other) {
        return this.mNumerator == other.mNumerator && this.mDenominator == other.mDenominator;
    }

    @Override // java.lang.Object
    public String toString() {
        if (isNaN()) {
            return "NaN";
        }
        if (isPosInf()) {
            return "Infinity";
        }
        if (isNegInf()) {
            return "-Infinity";
        }
        return this.mNumerator + "/" + this.mDenominator;
    }

    public float toFloat() {
        return floatValue();
    }

    @Override // java.lang.Object
    public int hashCode() {
        int i = this.mNumerator;
        return this.mDenominator ^ ((i >>> 16) | (i << 16));
    }

    public static int gcd(int numerator, int denominator) {
        int a = numerator;
        int b = denominator;
        while (b != 0) {
            b = a % b;
            a = b;
        }
        return Math.abs(a);
    }

    @Override // java.lang.Number
    public double doubleValue() {
        return ((double) this.mNumerator) / ((double) this.mDenominator);
    }

    @Override // java.lang.Number
    public float floatValue() {
        return ((float) this.mNumerator) / ((float) this.mDenominator);
    }

    @Override // java.lang.Number
    public int intValue() {
        if (isPosInf()) {
            return Integer.MAX_VALUE;
        }
        if (isNegInf()) {
            return Integer.MIN_VALUE;
        }
        if (isNaN()) {
            return 0;
        }
        return this.mNumerator / this.mDenominator;
    }

    @Override // java.lang.Number
    public long longValue() {
        if (isPosInf()) {
            return Long.MAX_VALUE;
        }
        if (isNegInf()) {
            return Long.MIN_VALUE;
        }
        if (isNaN()) {
            return 0;
        }
        return (long) (this.mNumerator / this.mDenominator);
    }

    @Override // java.lang.Number
    public short shortValue() {
        return (short) intValue();
    }

    public int compareTo(Rational another) {
        Preconditions.checkNotNull(another, "another must not be null");
        if (equals(another)) {
            return 0;
        }
        if (isNaN()) {
            return 1;
        }
        if (another.isNaN()) {
            return -1;
        }
        if (isPosInf() || another.isNegInf()) {
            return 1;
        }
        if (isNegInf() || another.isPosInf()) {
            return -1;
        }
        long thisNumerator = ((long) this.mNumerator) * ((long) another.mDenominator);
        long otherNumerator = ((long) another.mNumerator) * ((long) this.mDenominator);
        if (thisNumerator < otherNumerator) {
            return -1;
        }
        if (thisNumerator > otherNumerator) {
            return 1;
        }
        return 0;
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        int i = this.mNumerator;
        if (i == 0) {
            int i2 = this.mDenominator;
            if (i2 != 1 && i2 != 0) {
                throw new InvalidObjectException("Rational must be deserialized from a reduced form for zero values");
            }
            return;
        }
        int i3 = this.mDenominator;
        if (i3 == 0) {
            if (i != 1 && i != -1) {
                throw new InvalidObjectException("Rational must be deserialized from a reduced form for infinity values");
            }
        } else if (gcd(i, i3) > 1) {
            throw new InvalidObjectException("Rational must be deserialized from a reduced form for finite values");
        }
    }

    private static NumberFormatException invalidRational(String s) {
        throw new NumberFormatException("Invalid Rational: \"" + s + "\"");
    }

    public static Rational parseRational(String string) throws NumberFormatException {
        Preconditions.checkNotNull(string, "string must not be null");
        if (string.equals("NaN")) {
            return NaN;
        }
        if (string.equals("Infinity")) {
            return POSITIVE_INFINITY;
        }
        if (string.equals("-Infinity")) {
            return NEGATIVE_INFINITY;
        }
        int sep_ix = string.indexOf(58);
        if (sep_ix < 0) {
            sep_ix = string.indexOf(47);
        }
        if (sep_ix >= 0) {
            try {
                return new Rational(Integer.parseInt(string.substring(0, sep_ix)), Integer.parseInt(string.substring(sep_ix + 1)));
            } catch (NumberFormatException e) {
                throw invalidRational(string);
            }
        } else {
            throw invalidRational(string);
        }
    }
}
