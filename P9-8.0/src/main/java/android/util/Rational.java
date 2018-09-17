package android.util;

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
    private final int mDenominator;
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

    public boolean equals(Object obj) {
        return obj instanceof Rational ? equals((Rational) obj) : false;
    }

    private boolean equals(Rational other) {
        return this.mNumerator == other.mNumerator && this.mDenominator == other.mDenominator;
    }

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

    public int hashCode() {
        return this.mDenominator ^ ((this.mNumerator << 16) | (this.mNumerator >>> 16));
    }

    public static int gcd(int numerator, int denominator) {
        int a = numerator;
        int b = denominator;
        while (b != 0) {
            int oldB = b;
            b = a % b;
            a = oldB;
        }
        return Math.abs(a);
    }

    public double doubleValue() {
        return ((double) this.mNumerator) / ((double) this.mDenominator);
    }

    public float floatValue() {
        return ((float) this.mNumerator) / ((float) this.mDenominator);
    }

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
        return thisNumerator > otherNumerator ? 1 : 0;
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        if (this.mNumerator == 0) {
            if (this.mDenominator != 1 && this.mDenominator != 0) {
                throw new InvalidObjectException("Rational must be deserialized from a reduced form for zero values");
            }
        } else if (this.mDenominator == 0) {
            if (this.mNumerator != 1 && this.mNumerator != -1) {
                throw new InvalidObjectException("Rational must be deserialized from a reduced form for infinity values");
            }
        } else if (gcd(this.mNumerator, this.mDenominator) > 1) {
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
        if (sep_ix < 0) {
            throw invalidRational(string);
        }
        try {
            return new Rational(Integer.parseInt(string.substring(0, sep_ix)), Integer.parseInt(string.substring(sep_ix + 1)));
        } catch (NumberFormatException e) {
            throw invalidRational(string);
        }
    }
}
