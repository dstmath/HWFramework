package java.lang;

import sun.misc.FloatConsts;
import sun.misc.FloatingDecimal;

public final class Float extends Number implements Comparable<Float> {
    public static final int BYTES = 4;
    public static final int MAX_EXPONENT = 127;
    public static final float MAX_VALUE = Float.MAX_VALUE;
    public static final int MIN_EXPONENT = -126;
    public static final float MIN_NORMAL = Float.MIN_NORMAL;
    public static final float MIN_VALUE = Float.MIN_VALUE;
    public static final float NEGATIVE_INFINITY = Float.NEGATIVE_INFINITY;
    public static final float NaN = Float.NaN;
    public static final float POSITIVE_INFINITY = Float.POSITIVE_INFINITY;
    public static final int SIZE = 32;
    public static final Class<Float> TYPE = float[].class.getComponentType();
    private static final long serialVersionUID = -2671257302660747028L;
    private final float value;

    public static native int floatToRawIntBits(float f);

    public static native float intBitsToFloat(int i);

    public static String toString(float f) {
        return FloatingDecimal.toJavaFormatString(f);
    }

    public static String toHexString(float f) {
        if (Math.abs(f) >= Float.MIN_NORMAL || f == 0.0f) {
            return Double.toHexString((double) f);
        }
        return Double.toHexString(Math.scalb((double) f, -896)).replaceFirst("p-1022$", "p-126");
    }

    public static Float valueOf(String s) throws NumberFormatException {
        return new Float(parseFloat(s));
    }

    public static Float valueOf(float f) {
        return new Float(f);
    }

    public static float parseFloat(String s) throws NumberFormatException {
        return FloatingDecimal.parseFloat(s);
    }

    public static boolean isNaN(float v) {
        return v != v;
    }

    public static boolean isInfinite(float v) {
        return v == Float.POSITIVE_INFINITY || v == Float.NEGATIVE_INFINITY;
    }

    public static boolean isFinite(float f) {
        return Math.abs(f) <= Float.MAX_VALUE;
    }

    public Float(float value) {
        this.value = value;
    }

    public Float(double value) {
        this.value = (float) value;
    }

    public Float(String s) throws NumberFormatException {
        this.value = parseFloat(s);
    }

    public boolean isNaN() {
        return isNaN(this.value);
    }

    public boolean isInfinite() {
        return isInfinite(this.value);
    }

    public String toString() {
        return toString(this.value);
    }

    public byte byteValue() {
        return (byte) ((int) this.value);
    }

    public short shortValue() {
        return (short) ((int) this.value);
    }

    public int intValue() {
        return (int) this.value;
    }

    public long longValue() {
        return (long) this.value;
    }

    public float floatValue() {
        return this.value;
    }

    public double doubleValue() {
        return (double) this.value;
    }

    public int hashCode() {
        return hashCode(this.value);
    }

    public static int hashCode(float value) {
        return floatToIntBits(value);
    }

    public boolean equals(Object obj) {
        if ((obj instanceof Float) && floatToIntBits(((Float) obj).value) == floatToIntBits(this.value)) {
            return true;
        }
        return false;
    }

    public static int floatToIntBits(float value) {
        int result = floatToRawIntBits(value);
        if ((result & FloatConsts.EXP_BIT_MASK) != FloatConsts.EXP_BIT_MASK || (FloatConsts.SIGNIF_BIT_MASK & result) == 0) {
            return result;
        }
        return 2143289344;
    }

    public int compareTo(Float anotherFloat) {
        return compare(this.value, anotherFloat.value);
    }

    public static int compare(float f1, float f2) {
        int i = -1;
        if (f1 < f2) {
            return -1;
        }
        if (f1 > f2) {
            return 1;
        }
        int thisBits = floatToIntBits(f1);
        int anotherBits = floatToIntBits(f2);
        if (thisBits == anotherBits) {
            i = 0;
        } else if (thisBits >= anotherBits) {
            i = 1;
        }
        return i;
    }

    public static float sum(float a, float b) {
        return a + b;
    }

    public static float max(float a, float b) {
        return Math.max(a, b);
    }

    public static float min(float a, float b) {
        return Math.min(a, b);
    }
}
