package android.util;

import android.net.wifi.WifiInfo;
import sun.misc.FloatingDecimal;

public final class Half extends Number implements Comparable<Half> {
    public static final short EPSILON = 5120;
    private static final int FP16_COMBINED = 32767;
    private static final int FP16_EXPONENT_BIAS = 15;
    private static final int FP16_EXPONENT_MASK = 31;
    private static final int FP16_EXPONENT_MAX = 31744;
    private static final int FP16_EXPONENT_SHIFT = 10;
    private static final int FP16_SIGNIFICAND_MASK = 1023;
    private static final int FP16_SIGN_MASK = 32768;
    private static final int FP16_SIGN_SHIFT = 15;
    private static final float FP32_DENORMAL_FLOAT = Float.intBitsToFloat(FP32_DENORMAL_MAGIC);
    private static final int FP32_DENORMAL_MAGIC = 1056964608;
    private static final int FP32_EXPONENT_BIAS = 127;
    private static final int FP32_EXPONENT_MASK = 255;
    private static final int FP32_EXPONENT_SHIFT = 23;
    private static final int FP32_SIGNIFICAND_MASK = 8388607;
    private static final int FP32_SIGN_SHIFT = 31;
    public static final short LOWEST_VALUE = -1025;
    public static final int MAX_EXPONENT = 15;
    public static final short MAX_VALUE = 31743;
    public static final int MIN_EXPONENT = -14;
    public static final short MIN_NORMAL = 1024;
    public static final short MIN_VALUE = 1;
    public static final short NEGATIVE_INFINITY = -1024;
    public static final short NEGATIVE_ZERO = Short.MIN_VALUE;
    public static final short NaN = 32256;
    public static final short POSITIVE_INFINITY = 31744;
    public static final short POSITIVE_ZERO = 0;
    public static final int SIZE = 16;
    private final short mValue;

    public Half(short value) {
        this.mValue = value;
    }

    public Half(float value) {
        this.mValue = toHalf(value);
    }

    public Half(double value) {
        this.mValue = toHalf((float) value);
    }

    public Half(String value) throws NumberFormatException {
        this.mValue = toHalf(Float.parseFloat(value));
    }

    public short halfValue() {
        return this.mValue;
    }

    public byte byteValue() {
        return (byte) ((int) toFloat(this.mValue));
    }

    public short shortValue() {
        return (short) ((int) toFloat(this.mValue));
    }

    public int intValue() {
        return (int) toFloat(this.mValue);
    }

    public long longValue() {
        return (long) toFloat(this.mValue);
    }

    public float floatValue() {
        return toFloat(this.mValue);
    }

    public double doubleValue() {
        return (double) toFloat(this.mValue);
    }

    public boolean isNaN() {
        return isNaN(this.mValue);
    }

    public boolean equals(Object o) {
        return (o instanceof Half) && halfToIntBits(((Half) o).mValue) == halfToIntBits(this.mValue);
    }

    public int hashCode() {
        return hashCode(this.mValue);
    }

    public String toString() {
        return toString(this.mValue);
    }

    public int compareTo(Half h) {
        return compare(this.mValue, h.mValue);
    }

    public static int hashCode(short h) {
        return halfToIntBits(h);
    }

    public static int compare(short x, short y) {
        int i = -1;
        if (less(x, y)) {
            return -1;
        }
        if (greater(x, y)) {
            return 1;
        }
        short s = x & Short.MAX_VALUE;
        short yBits = NaN;
        short xBits = s > FP16_EXPONENT_MAX ? 32256 : x;
        if ((y & Short.MAX_VALUE) <= FP16_EXPONENT_MAX) {
            yBits = y;
        }
        if (xBits == yBits) {
            i = 0;
        } else if (xBits >= yBits) {
            i = 1;
        }
        return i;
    }

    public static short halfToShortBits(short h) {
        return (h & Short.MAX_VALUE) > FP16_EXPONENT_MAX ? NaN : h;
    }

    public static int halfToIntBits(short h) {
        if ((h & Short.MAX_VALUE) > FP16_EXPONENT_MAX) {
            return 32256;
        }
        return 65535 & h;
    }

    public static int halfToRawIntBits(short h) {
        return 65535 & h;
    }

    public static short intBitsToHalf(int bits) {
        return (short) (65535 & bits);
    }

    public static short copySign(short magnitude, short sign) {
        return (short) ((32768 & sign) | (magnitude & Short.MAX_VALUE));
    }

    public static short abs(short h) {
        return (short) (h & Short.MAX_VALUE);
    }

    public static short round(short h) {
        int i = 65535;
        int bits = h & 65535;
        int e = bits & FP16_COMBINED;
        int result = bits;
        if (e < 15360) {
            int result2 = result & 32768;
            if (e < 14336) {
                i = 0;
            }
            result = result2 | (i & 15360);
        } else if (e < 25600) {
            int e2 = 25 - (e >> 10);
            result = (result + (1 << (e2 - 1))) & (~((1 << e2) - 1));
        }
        return (short) result;
    }

    public static short ceil(short h) {
        int bits = 65535 & h;
        int e = bits & FP16_COMBINED;
        int result = bits;
        int i = 1;
        if (e < 15360) {
            int result2 = result & 32768;
            int i2 = ~(bits >> 15);
            if (e == 0) {
                i = 0;
            }
            result = result2 | (15360 & (-(i & i2)));
        } else if (e < 25600) {
            int mask = (1 << (25 - (e >> 10))) - 1;
            result = (result + (mask & ((bits >> 15) - 1))) & (~mask);
        }
        return (short) result;
    }

    public static short floor(short h) {
        int i = 65535;
        int bits = h & 65535;
        int e = bits & FP16_COMBINED;
        int result = bits;
        if (e < 15360) {
            int result2 = result & 32768;
            if (bits <= 32768) {
                i = 0;
            }
            result = result2 | (i & 15360);
        } else if (e < 25600) {
            int mask = (1 << (25 - (e >> 10))) - 1;
            result = (result + ((-(bits >> 15)) & mask)) & (~mask);
        }
        return (short) result;
    }

    public static short trunc(short h) {
        int bits = 65535 & h;
        int e = bits & FP16_COMBINED;
        int result = bits;
        if (e < 15360) {
            result &= 32768;
        } else if (e < 25600) {
            result &= ~((1 << (25 - (e >> 10))) - 1);
        }
        return (short) result;
    }

    public static short min(short x, short y) {
        if ((x & Short.MAX_VALUE) > FP16_EXPONENT_MAX || (y & Short.MAX_VALUE) > FP16_EXPONENT_MAX) {
            return NaN;
        }
        if ((x & Short.MAX_VALUE) == 0 && (y & Short.MAX_VALUE) == 0) {
            return (x & NEGATIVE_ZERO) != 0 ? x : y;
        }
        return ((x & NEGATIVE_ZERO) != 0 ? 32768 - (x & 65535) : x & 65535) < ((y & NEGATIVE_ZERO) != 0 ? 32768 - (65535 & y) : y & 65535) ? x : y;
    }

    public static short max(short x, short y) {
        if ((x & Short.MAX_VALUE) > FP16_EXPONENT_MAX || (y & Short.MAX_VALUE) > FP16_EXPONENT_MAX) {
            return NaN;
        }
        if ((x & Short.MAX_VALUE) == 0 && (y & Short.MAX_VALUE) == 0) {
            return (x & NEGATIVE_ZERO) != 0 ? y : x;
        }
        return ((x & NEGATIVE_ZERO) != 0 ? 32768 - (x & 65535) : x & 65535) > ((y & NEGATIVE_ZERO) != 0 ? 32768 - (65535 & y) : y & 65535) ? x : y;
    }

    public static boolean less(short x, short y) {
        boolean z = false;
        if ((x & Short.MAX_VALUE) > FP16_EXPONENT_MAX || (y & Short.MAX_VALUE) > FP16_EXPONENT_MAX) {
            return false;
        }
        if (((x & NEGATIVE_ZERO) != 0 ? 32768 - (x & 65535) : x & 65535) < ((y & NEGATIVE_ZERO) != 0 ? 32768 - (65535 & y) : y & 65535)) {
            z = true;
        }
        return z;
    }

    public static boolean lessEquals(short x, short y) {
        boolean z = false;
        if ((x & Short.MAX_VALUE) > FP16_EXPONENT_MAX || (y & Short.MAX_VALUE) > FP16_EXPONENT_MAX) {
            return false;
        }
        if (((x & NEGATIVE_ZERO) != 0 ? 32768 - (x & 65535) : x & 65535) <= ((y & NEGATIVE_ZERO) != 0 ? 32768 - (65535 & y) : y & 65535)) {
            z = true;
        }
        return z;
    }

    public static boolean greater(short x, short y) {
        boolean z = false;
        if ((x & Short.MAX_VALUE) > FP16_EXPONENT_MAX || (y & Short.MAX_VALUE) > FP16_EXPONENT_MAX) {
            return false;
        }
        if (((x & NEGATIVE_ZERO) != 0 ? 32768 - (x & 65535) : x & 65535) > ((y & NEGATIVE_ZERO) != 0 ? 32768 - (65535 & y) : y & 65535)) {
            z = true;
        }
        return z;
    }

    public static boolean greaterEquals(short x, short y) {
        boolean z = false;
        if ((x & Short.MAX_VALUE) > FP16_EXPONENT_MAX || (y & Short.MAX_VALUE) > FP16_EXPONENT_MAX) {
            return false;
        }
        if (((x & NEGATIVE_ZERO) != 0 ? 32768 - (x & 65535) : x & 65535) >= ((y & NEGATIVE_ZERO) != 0 ? 32768 - (65535 & y) : y & 65535)) {
            z = true;
        }
        return z;
    }

    public static boolean equals(short x, short y) {
        boolean z = false;
        if ((x & Short.MAX_VALUE) > FP16_EXPONENT_MAX || (y & Short.MAX_VALUE) > FP16_EXPONENT_MAX) {
            return false;
        }
        if (x == y || ((x | y) & Short.MAX_VALUE) == 0) {
            z = true;
        }
        return z;
    }

    public static int getSign(short h) {
        return (32768 & h) == 0 ? 1 : -1;
    }

    public static int getExponent(short h) {
        return ((h >>> 10) & 31) - 15;
    }

    public static int getSignificand(short h) {
        return h & 1023;
    }

    public static boolean isInfinite(short h) {
        return (h & Short.MAX_VALUE) == FP16_EXPONENT_MAX;
    }

    public static boolean isNaN(short h) {
        return (h & Short.MAX_VALUE) > FP16_EXPONENT_MAX;
    }

    public static boolean isNormalized(short h) {
        return ((h & POSITIVE_INFINITY) == 0 || (h & POSITIVE_INFINITY) == FP16_EXPONENT_MAX) ? false : true;
    }

    public static float toFloat(short h) {
        int bits = 65535 & h;
        int s = 32768 & bits;
        int e = (bits >>> 10) & 31;
        int m = bits & 1023;
        int outE = 0;
        int outM = 0;
        if (e != 0) {
            outM = m << 13;
            if (e == 31) {
                outE = 255;
            } else {
                outE = (e - 15) + 127;
            }
        } else if (m != 0) {
            float o = Float.intBitsToFloat(FP32_DENORMAL_MAGIC + m) - FP32_DENORMAL_FLOAT;
            return s == 0 ? o : -o;
        }
        return Float.intBitsToFloat((s << 16) | (outE << 23) | outM);
    }

    public static short toHalf(float f) {
        int bits = Float.floatToRawIntBits(f);
        int s = bits >>> 31;
        int e = (bits >>> 23) & 255;
        int m = FP32_SIGNIFICAND_MASK & bits;
        int outE = 0;
        int outM = 0;
        if (e == 255) {
            outE = 31;
            outM = m != 0 ? 512 : 0;
        } else {
            int e2 = e + WifiInfo.INVALID_RSSI + 15;
            if (e2 >= 31) {
                outE = 49;
            } else if (e2 > 0) {
                outE = e2;
                outM = m >> 13;
                if ((m & 4096) != 0) {
                    return (short) ((s << 15) | (((outE << 10) | outM) + 1));
                }
            } else if (e2 >= -10) {
                int m2 = (8388608 | m) >> (1 - e2);
                if ((m2 & 4096) != 0) {
                    m2 += 8192;
                }
                outM = m2 >> 13;
            }
        }
        return (short) ((s << 15) | (outE << 10) | outM);
    }

    public static Half valueOf(short h) {
        return new Half(h);
    }

    public static Half valueOf(float f) {
        return new Half(f);
    }

    public static Half valueOf(String s) {
        return new Half(s);
    }

    public static short parseHalf(String s) throws NumberFormatException {
        return toHalf(FloatingDecimal.parseFloat(s));
    }

    public static String toString(short h) {
        return Float.toString(toFloat(h));
    }

    public static String toHexString(short h) {
        StringBuilder o = new StringBuilder();
        int bits = 65535 & h;
        int s = bits >>> 15;
        int e = (bits >>> 10) & 31;
        int m = bits & 1023;
        if (e != 31) {
            if (s == 1) {
                o.append('-');
            }
            if (e != 0) {
                o.append("0x1.");
                o.append(Integer.toHexString(m).replaceFirst("0{2,}$", ""));
                o.append('p');
                o.append(Integer.toString(e - 15));
            } else if (m == 0) {
                o.append("0x0.0p0");
            } else {
                o.append("0x0.");
                o.append(Integer.toHexString(m).replaceFirst("0{2,}$", ""));
                o.append("p-14");
            }
        } else if (m == 0) {
            if (s != 0) {
                o.append('-');
            }
            o.append("Infinity");
        } else {
            o.append("NaN");
        }
        return o.toString();
    }
}
