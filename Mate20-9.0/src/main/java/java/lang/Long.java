package java.lang;

import java.math.BigInteger;
import sun.util.locale.LanguageTag;

public final class Long extends Number implements Comparable<Long> {
    public static final int BYTES = 8;
    public static final long MAX_VALUE = Long.MAX_VALUE;
    public static final long MIN_VALUE = Long.MIN_VALUE;
    public static final int SIZE = 64;
    public static final Class<Long> TYPE = Class.getPrimitiveClass("long");
    private static final long serialVersionUID = 4290774380558885855L;
    private final long value;

    private static class LongCache {
        static final Long[] cache = new Long[256];

        private LongCache() {
        }

        static {
            for (int i = 0; i < cache.length; i++) {
                cache[i] = new Long((long) (i - 128));
            }
        }
    }

    public static String toString(long i, int radix) {
        if (radix < 2 || radix > 36) {
            radix = 10;
        }
        if (radix == 10) {
            return toString(i);
        }
        char[] buf = new char[65];
        int charPos = 64;
        boolean negative = i < 0;
        if (!negative) {
            i = -i;
        }
        while (i <= ((long) (-radix))) {
            buf[charPos] = Integer.digits[(int) (-(i % ((long) radix)))];
            i /= (long) radix;
            charPos--;
        }
        buf[charPos] = Integer.digits[(int) (-i)];
        if (negative) {
            charPos--;
            buf[charPos] = '-';
        }
        return new String(buf, charPos, 65 - charPos);
    }

    public static String toUnsignedString(long i, int radix) {
        if (i >= 0) {
            return toString(i, radix);
        }
        if (radix == 2) {
            return toBinaryString(i);
        }
        if (radix == 4) {
            return toUnsignedString0(i, 2);
        }
        if (radix == 8) {
            return toOctalString(i);
        }
        if (radix == 10) {
            long quot = (i >>> 1) / 5;
            return toString(quot) + (i - (10 * quot));
        } else if (radix == 16) {
            return toHexString(i);
        } else {
            if (radix != 32) {
                return toUnsignedBigInteger(i).toString(radix);
            }
            return toUnsignedString0(i, 5);
        }
    }

    private static BigInteger toUnsignedBigInteger(long i) {
        if (i >= 0) {
            return BigInteger.valueOf(i);
        }
        return BigInteger.valueOf(Integer.toUnsignedLong((int) (i >>> 32))).shiftLeft(32).add(BigInteger.valueOf(Integer.toUnsignedLong((int) i)));
    }

    public static String toHexString(long i) {
        return toUnsignedString0(i, 4);
    }

    public static String toOctalString(long i) {
        return toUnsignedString0(i, 3);
    }

    public static String toBinaryString(long i) {
        return toUnsignedString0(i, 1);
    }

    static String toUnsignedString0(long val, int shift) {
        int chars = Math.max(((shift - 1) + (64 - numberOfLeadingZeros(val))) / shift, 1);
        char[] buf = new char[chars];
        formatUnsignedLong(val, shift, buf, 0, chars);
        return new String(buf);
    }

    static int formatUnsignedLong(long val, int shift, char[] buf, int offset, int len) {
        int charPos = len;
        int mask = (1 << shift) - 1;
        do {
            charPos--;
            buf[offset + charPos] = Integer.digits[((int) val) & mask];
            val >>>= shift;
            if (val == 0) {
                break;
            }
        } while (charPos > 0);
        return charPos;
    }

    public static String toString(long i) {
        if (i == Long.MIN_VALUE) {
            return "-9223372036854775808";
        }
        int size = i < 0 ? stringSize(-i) + 1 : stringSize(i);
        char[] buf = new char[size];
        getChars(i, size, buf);
        return new String(buf);
    }

    public static String toUnsignedString(long i) {
        return toUnsignedString(i, 10);
    }

    static void getChars(long i, int index, char[] buf) {
        int charPos = index;
        char sign = 0;
        if (i < 0) {
            sign = '-';
            i = -i;
        }
        while (i > 2147483647L) {
            long q = i / 100;
            int r = (int) (i - (((q << 6) + (q << 5)) + (q << 2)));
            i = q;
            int charPos2 = charPos - 1;
            buf[charPos2] = Integer.DigitOnes[r];
            charPos = charPos2 - 1;
            buf[charPos] = Integer.DigitTens[r];
        }
        int i2 = (int) i;
        while (i2 >= 65536) {
            int q2 = i2 / 100;
            int r2 = i2 - (((q2 << 6) + (q2 << 5)) + (q2 << 2));
            i2 = q2;
            int charPos3 = charPos - 1;
            buf[charPos3] = Integer.DigitOnes[r2];
            charPos = charPos3 - 1;
            buf[charPos] = Integer.DigitTens[r2];
        }
        do {
            int q22 = (52429 * i2) >>> 19;
            charPos--;
            buf[charPos] = Integer.digits[i2 - ((q22 << 3) + (q22 << 1))];
            i2 = q22;
        } while (i2 != 0);
        if (sign != 0) {
            buf[charPos - 1] = sign;
        }
    }

    static int stringSize(long x) {
        long p = 10;
        for (int i = 1; i < 19; i++) {
            if (x < p) {
                return i;
            }
            p *= 10;
        }
        return 19;
    }

    public static long parseLong(String s, int radix) throws NumberFormatException {
        if (s == null) {
            throw new NumberFormatException("null");
        } else if (radix < 2) {
            throw new NumberFormatException("radix " + radix + " less than Character.MIN_RADIX");
        } else if (radix <= 36) {
            long result = 0;
            boolean negative = false;
            int i = 0;
            int len = s.length();
            long limit = -9223372036854775807L;
            if (len > 0) {
                char firstChar = s.charAt(0);
                if (firstChar < '0') {
                    if (firstChar == '-') {
                        negative = true;
                        limit = Long.MIN_VALUE;
                    } else if (firstChar != '+') {
                        throw NumberFormatException.forInputString(s);
                    }
                    if (len != 1) {
                        i = 0 + 1;
                    } else {
                        throw NumberFormatException.forInputString(s);
                    }
                }
                long multmin = limit / ((long) radix);
                while (i < len) {
                    int i2 = i + 1;
                    int i3 = Character.digit(s.charAt(i), radix);
                    if (i3 < 0) {
                        throw NumberFormatException.forInputString(s);
                    } else if (result >= multmin) {
                        long result2 = result * ((long) radix);
                        if (result2 >= ((long) i3) + limit) {
                            result = result2 - ((long) i3);
                            i = i2;
                        } else {
                            throw NumberFormatException.forInputString(s);
                        }
                    } else {
                        throw NumberFormatException.forInputString(s);
                    }
                }
                return negative ? result : -result;
            }
            throw NumberFormatException.forInputString(s);
        } else {
            throw new NumberFormatException("radix " + radix + " greater than Character.MAX_RADIX");
        }
    }

    public static long parseLong(String s) throws NumberFormatException {
        return parseLong(s, 10);
    }

    public static long parseUnsignedLong(String s, int radix) throws NumberFormatException {
        if (s != null) {
            int len = s.length();
            if (len <= 0) {
                throw NumberFormatException.forInputString(s);
            } else if (s.charAt(0) == '-') {
                throw new NumberFormatException(String.format("Illegal leading minus sign on unsigned string %s.", s));
            } else if (len <= 12 || (radix == 10 && len <= 18)) {
                return parseLong(s, radix);
            } else {
                long first = parseLong(s.substring(0, len - 1), radix);
                int second = Character.digit(s.charAt(len - 1), radix);
                if (second >= 0) {
                    long result = (((long) radix) * first) + ((long) second);
                    if (compareUnsigned(result, first) >= 0) {
                        return result;
                    }
                    throw new NumberFormatException(String.format("String value %s exceeds range of unsigned long.", s));
                }
                throw new NumberFormatException("Bad digit at end of " + s);
            }
        } else {
            throw new NumberFormatException("null");
        }
    }

    public static long parseUnsignedLong(String s) throws NumberFormatException {
        return parseUnsignedLong(s, 10);
    }

    public static Long valueOf(String s, int radix) throws NumberFormatException {
        return valueOf(parseLong(s, radix));
    }

    public static Long valueOf(String s) throws NumberFormatException {
        return valueOf(parseLong(s, 10));
    }

    public static Long valueOf(long l) {
        if (l < -128 || l > 127) {
            return new Long(l);
        }
        return LongCache.cache[((int) l) + 128];
    }

    public static Long decode(String nm) throws NumberFormatException {
        String constant;
        int radix = 10;
        int index = 0;
        boolean negative = false;
        if (nm.length() != 0) {
            char firstChar = nm.charAt(0);
            if (firstChar == '-') {
                negative = true;
                index = 0 + 1;
            } else if (firstChar == '+') {
                index = 0 + 1;
            }
            if (nm.startsWith("0x", index) || nm.startsWith("0X", index)) {
                index += 2;
                radix = 16;
            } else if (nm.startsWith("#", index)) {
                index++;
                radix = 16;
            } else if (nm.startsWith("0", index) && nm.length() > 1 + index) {
                index++;
                radix = 8;
            }
            if (nm.startsWith(LanguageTag.SEP, index) || nm.startsWith("+", index)) {
                throw new NumberFormatException("Sign character in wrong position");
            }
            try {
                Long result = valueOf(nm.substring(index), radix);
                return negative ? valueOf(-result.longValue()) : result;
            } catch (NumberFormatException e) {
                if (negative) {
                    constant = LanguageTag.SEP + nm.substring(index);
                } else {
                    constant = nm.substring(index);
                }
                return valueOf(constant, radix);
            }
        } else {
            throw new NumberFormatException("Zero length string");
        }
    }

    public Long(long value2) {
        this.value = value2;
    }

    public Long(String s) throws NumberFormatException {
        this.value = parseLong(s, 10);
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
        return this.value;
    }

    public float floatValue() {
        return (float) this.value;
    }

    public double doubleValue() {
        return (double) this.value;
    }

    public String toString() {
        return toString(this.value);
    }

    public int hashCode() {
        return hashCode(this.value);
    }

    public static int hashCode(long value2) {
        return (int) ((value2 >>> 32) ^ value2);
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (!(obj instanceof Long)) {
            return false;
        }
        if (this.value == ((Long) obj).longValue()) {
            z = true;
        }
        return z;
    }

    public static Long getLong(String nm) {
        return getLong(nm, (Long) null);
    }

    public static Long getLong(String nm, long val) {
        Long result = getLong(nm, (Long) null);
        return result == null ? valueOf(val) : result;
    }

    public static Long getLong(String nm, Long val) {
        String v = null;
        try {
            v = System.getProperty(nm);
        } catch (IllegalArgumentException | NullPointerException e) {
        }
        if (v != null) {
            try {
                return decode(v);
            } catch (NumberFormatException e2) {
            }
        }
        return val;
    }

    public int compareTo(Long anotherLong) {
        return compare(this.value, anotherLong.value);
    }

    public static int compare(long x, long y) {
        if (x < y) {
            return -1;
        }
        return x == y ? 0 : 1;
    }

    public static int compareUnsigned(long x, long y) {
        return compare(x - Long.MIN_VALUE, Long.MIN_VALUE + y);
    }

    public static long divideUnsigned(long dividend, long divisor) {
        long j = 0;
        if (divisor < 0) {
            if (compareUnsigned(dividend, divisor) >= 0) {
                j = 1;
            }
            return j;
        } else if (dividend > 0) {
            return dividend / divisor;
        } else {
            return toUnsignedBigInteger(dividend).divide(toUnsignedBigInteger(divisor)).longValue();
        }
    }

    public static long remainderUnsigned(long dividend, long divisor) {
        if (dividend > 0 && divisor > 0) {
            return dividend % divisor;
        }
        if (compareUnsigned(dividend, divisor) < 0) {
            return dividend;
        }
        return toUnsignedBigInteger(dividend).remainder(toUnsignedBigInteger(divisor)).longValue();
    }

    public static long highestOneBit(long i) {
        long i2 = i | (i >> 1);
        long i3 = i2 | (i2 >> 2);
        long i4 = i3 | (i3 >> 4);
        long i5 = i4 | (i4 >> 8);
        long i6 = i5 | (i5 >> 16);
        long i7 = i6 | (i6 >> 32);
        return i7 - (i7 >>> 1);
    }

    public static long lowestOneBit(long i) {
        return (-i) & i;
    }

    public static int numberOfLeadingZeros(long i) {
        if (i == 0) {
            return 64;
        }
        int n = 1;
        int x = (int) (i >>> 32);
        if (x == 0) {
            n = 1 + 32;
            x = (int) i;
        }
        if ((x >>> 16) == 0) {
            n += 16;
            x <<= 16;
        }
        if ((x >>> 24) == 0) {
            n += 8;
            x <<= 8;
        }
        if ((x >>> 28) == 0) {
            n += 4;
            x <<= 4;
        }
        if ((x >>> 30) == 0) {
            n += 2;
            x <<= 2;
        }
        return n - (x >>> 31);
    }

    public static int numberOfTrailingZeros(long i) {
        int x;
        if (i == 0) {
            return 64;
        }
        int n = 63;
        int y = (int) i;
        if (y != 0) {
            n = 63 - 32;
            x = y;
        } else {
            x = (int) (i >>> 32);
        }
        int y2 = x << 16;
        if (y2 != 0) {
            n -= 16;
            x = y2;
        }
        int y3 = x << 8;
        if (y3 != 0) {
            n -= 8;
            x = y3;
        }
        int y4 = x << 4;
        if (y4 != 0) {
            n -= 4;
            x = y4;
        }
        int y5 = x << 2;
        if (y5 != 0) {
            n -= 2;
            x = y5;
        }
        return n - ((x << 1) >>> 31);
    }

    public static int bitCount(long i) {
        long i2 = i - ((i >>> 1) & 6148914691236517205L);
        long i3 = (i2 & 3689348814741910323L) + (3689348814741910323L & (i2 >>> 2));
        long i4 = ((i3 >>> 4) + i3) & 1085102592571150095L;
        long i5 = i4 + (i4 >>> 8);
        long i6 = i5 + (i5 >>> 16);
        return ((int) (i6 + (i6 >>> 32))) & 127;
    }

    public static long rotateLeft(long i, int distance) {
        return (i << distance) | (i >>> (-distance));
    }

    public static long rotateRight(long i, int distance) {
        return (i >>> distance) | (i << (-distance));
    }

    public static long reverse(long i) {
        long i2 = ((i & 6148914691236517205L) << 1) | (6148914691236517205L & (i >>> 1));
        long i3 = ((i2 & 3689348814741910323L) << 2) | (3689348814741910323L & (i2 >>> 2));
        long i4 = ((i3 & 1085102592571150095L) << 4) | (1085102592571150095L & (i3 >>> 4));
        long i5 = ((i4 & 71777214294589695L) << 8) | (71777214294589695L & (i4 >>> 8));
        return (i5 << 48) | ((i5 & 4294901760L) << 16) | (4294901760L & (i5 >>> 16)) | (i5 >>> 48);
    }

    public static int signum(long i) {
        return (int) ((i >> 63) | ((-i) >>> 63));
    }

    public static long reverseBytes(long i) {
        long i2 = ((i & 71777214294589695L) << 8) | (71777214294589695L & (i >>> 8));
        return (i2 << 48) | ((i2 & 4294901760L) << 16) | (4294901760L & (i2 >>> 16)) | (i2 >>> 48);
    }

    public static long sum(long a, long b) {
        return a + b;
    }

    public static long max(long a, long b) {
        return Math.max(a, b);
    }

    public static long min(long a, long b) {
        return Math.min(a, b);
    }
}
