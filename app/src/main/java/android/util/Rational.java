package android.util;

import com.android.internal.os.HwBootFail;
import com.android.internal.util.Preconditions;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;

public final class Rational extends Number implements Comparable<Rational> {
    public static final Rational NEGATIVE_INFINITY = null;
    public static final Rational NaN = null;
    public static final Rational POSITIVE_INFINITY = null;
    public static final Rational ZERO = null;
    private static final long serialVersionUID = 1;
    private final int mDenominator;
    private final int mNumerator;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.util.Rational.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.util.Rational.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.util.Rational.<clinit>():void");
    }

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
            return HwBootFail.STAGE_BOOT_SUCCESS;
        }
        if (isNegInf()) {
            return RtlSpacingHelper.UNDEFINED;
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
