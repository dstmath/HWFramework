package java.lang;

import java.util.Random;
import sun.misc.DoubleConsts;
import sun.misc.FloatConsts;

public final class Math {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    public static final double E = 2.718281828459045d;
    public static final double PI = 3.141592653589793d;
    private static long negativeZeroDoubleBits = Double.doubleToRawLongBits(-0.0d);
    private static long negativeZeroFloatBits = ((long) Float.floatToRawIntBits(-0.0f));
    static double twoToTheDoubleScaleDown = powerOfTwoD(-512);
    static double twoToTheDoubleScaleUp = powerOfTwoD(512);

    private static final class RandomNumberGeneratorHolder {
        static final Random randomNumberGenerator = new Random();

        private RandomNumberGeneratorHolder() {
        }
    }

    public static native double IEEEremainder(double d, double d2);

    public static native double acos(double d);

    public static native double asin(double d);

    public static native double atan(double d);

    public static native double atan2(double d, double d2);

    public static native double cbrt(double d);

    public static native double ceil(double d);

    public static native double cos(double d);

    public static native double cosh(double d);

    public static native double exp(double d);

    public static native double expm1(double d);

    public static native double floor(double d);

    public static native double hypot(double d, double d2);

    public static native double log(double d);

    public static native double log10(double d);

    public static native double log1p(double d);

    public static native double pow(double d, double d2);

    public static native double rint(double d);

    public static native double sin(double d);

    public static native double sinh(double d);

    public static native double sqrt(double d);

    public static native double tan(double d);

    public static native double tanh(double d);

    private Math() {
    }

    public static double toRadians(double angdeg) {
        return (angdeg / 180.0d) * 3.141592653589793d;
    }

    public static double toDegrees(double angrad) {
        return (180.0d * angrad) / 3.141592653589793d;
    }

    public static int round(float a) {
        int intBits = Float.floatToRawIntBits(a);
        int shift = 149 - ((2139095040 & intBits) >> 23);
        if ((shift & -32) != 0) {
            return (int) a;
        }
        int r = (8388607 & intBits) | 8388608;
        if (intBits < 0) {
            r = -r;
        }
        return ((r >> shift) + 1) >> 1;
    }

    public static long round(double a) {
        long longBits = Double.doubleToRawLongBits(a);
        long shift = 1074 - ((DoubleConsts.EXP_BIT_MASK & longBits) >> 52);
        if ((-64 & shift) != 0) {
            return (long) a;
        }
        long r = (DoubleConsts.SIGNIF_BIT_MASK & longBits) | 4503599627370496L;
        if (longBits < 0) {
            r = -r;
        }
        return ((r >> ((int) shift)) + 1) >> 1;
    }

    public static double random() {
        return RandomNumberGeneratorHolder.randomNumberGenerator.nextDouble();
    }

    public static void setRandomSeedInternal(long seed) {
        RandomNumberGeneratorHolder.randomNumberGenerator.setSeed(seed);
    }

    public static int randomIntInternal() {
        return RandomNumberGeneratorHolder.randomNumberGenerator.nextInt();
    }

    public static long randomLongInternal() {
        return RandomNumberGeneratorHolder.randomNumberGenerator.nextLong();
    }

    public static int addExact(int x, int y) {
        int r = x + y;
        if (((x ^ r) & (y ^ r)) >= 0) {
            return r;
        }
        throw new ArithmeticException("integer overflow");
    }

    public static long addExact(long x, long y) {
        long r = x + y;
        if (((x ^ r) & (y ^ r)) >= 0) {
            return r;
        }
        throw new ArithmeticException("long overflow");
    }

    public static int subtractExact(int x, int y) {
        int r = x - y;
        if (((x ^ y) & (x ^ r)) >= 0) {
            return r;
        }
        throw new ArithmeticException("integer overflow");
    }

    public static long subtractExact(long x, long y) {
        long r = x - y;
        if (((x ^ y) & (x ^ r)) >= 0) {
            return r;
        }
        throw new ArithmeticException("long overflow");
    }

    public static int multiplyExact(int x, int y) {
        long r = ((long) x) * ((long) y);
        if (((long) ((int) r)) == r) {
            return (int) r;
        }
        throw new ArithmeticException("integer overflow");
    }

    public static long multiplyExact(long x, long y) {
        long r = x * y;
        if (((abs(x) | abs(y)) >>> 31) == 0 || ((y == 0 || r / y == x) && (x != Long.MIN_VALUE || y != -1))) {
            return r;
        }
        throw new ArithmeticException("long overflow");
    }

    public static int incrementExact(int a) {
        if (a != Integer.MAX_VALUE) {
            return a + 1;
        }
        throw new ArithmeticException("integer overflow");
    }

    public static long incrementExact(long a) {
        if (a != Long.MAX_VALUE) {
            return 1 + a;
        }
        throw new ArithmeticException("long overflow");
    }

    public static int decrementExact(int a) {
        if (a != Integer.MIN_VALUE) {
            return a - 1;
        }
        throw new ArithmeticException("integer overflow");
    }

    public static long decrementExact(long a) {
        if (a != Long.MIN_VALUE) {
            return a - 1;
        }
        throw new ArithmeticException("long overflow");
    }

    public static int negateExact(int a) {
        if (a != Integer.MIN_VALUE) {
            return -a;
        }
        throw new ArithmeticException("integer overflow");
    }

    public static long negateExact(long a) {
        if (a != Long.MIN_VALUE) {
            return -a;
        }
        throw new ArithmeticException("long overflow");
    }

    public static int toIntExact(long value) {
        if (((long) ((int) value)) == value) {
            return (int) value;
        }
        throw new ArithmeticException("integer overflow");
    }

    public static int floorDiv(int x, int y) {
        int r = x / y;
        if ((x ^ y) >= 0 || r * y == x) {
            return r;
        }
        return r - 1;
    }

    public static long floorDiv(long x, long y) {
        long r = x / y;
        if ((x ^ y) >= 0 || r * y == x) {
            return r;
        }
        return r - 1;
    }

    public static int floorMod(int x, int y) {
        return x - (floorDiv(x, y) * y);
    }

    public static long floorMod(long x, long y) {
        return x - (floorDiv(x, y) * y);
    }

    public static int abs(int a) {
        return a < 0 ? -a : a;
    }

    public static long abs(long a) {
        return a < 0 ? -a : a;
    }

    public static float abs(float a) {
        return Float.intBitsToFloat(Float.floatToRawIntBits(a) & Integer.MAX_VALUE);
    }

    public static double abs(double a) {
        return Double.longBitsToDouble(Double.doubleToRawLongBits(a) & Long.MAX_VALUE);
    }

    public static int max(int a, int b) {
        return a >= b ? a : b;
    }

    public static long max(long a, long b) {
        return a >= b ? a : b;
    }

    public static float max(float a, float b) {
        if (a != a) {
            return a;
        }
        if (a == 0.0f && b == 0.0f && ((long) Float.floatToRawIntBits(a)) == negativeZeroFloatBits) {
            return b;
        }
        return a >= b ? a : b;
    }

    public static double max(double a, double b) {
        if (a != a) {
            return a;
        }
        if (a == 0.0d && b == 0.0d && Double.doubleToRawLongBits(a) == negativeZeroDoubleBits) {
            return b;
        }
        return a >= b ? a : b;
    }

    public static int min(int a, int b) {
        return a <= b ? a : b;
    }

    public static long min(long a, long b) {
        return a <= b ? a : b;
    }

    public static float min(float a, float b) {
        if (a != a) {
            return a;
        }
        if (a == 0.0f && b == 0.0f && ((long) Float.floatToRawIntBits(b)) == negativeZeroFloatBits) {
            return b;
        }
        return a <= b ? a : b;
    }

    public static double min(double a, double b) {
        if (a != a) {
            return a;
        }
        if (a == 0.0d && b == 0.0d && Double.doubleToRawLongBits(b) == negativeZeroDoubleBits) {
            return b;
        }
        return a <= b ? a : b;
    }

    public static double ulp(double d) {
        int exp = getExponent(d);
        if (exp == -1023) {
            return Double.MIN_VALUE;
        }
        if (exp == 1024) {
            return abs(d);
        }
        int exp2 = exp - 52;
        if (exp2 >= -1022) {
            return powerOfTwoD(exp2);
        }
        return Double.longBitsToDouble(1 << (exp2 + 1074));
    }

    public static float ulp(float f) {
        int exp = getExponent(f);
        if (exp == -127) {
            return Float.MIN_VALUE;
        }
        if (exp == 128) {
            return abs(f);
        }
        int exp2 = exp - 23;
        if (exp2 >= -126) {
            return powerOfTwoF(exp2);
        }
        return Float.intBitsToFloat(1 << (exp2 + 149));
    }

    public static double signum(double d) {
        return (d == 0.0d || Double.isNaN(d)) ? d : copySign(1.0d, d);
    }

    public static float signum(float f) {
        return (f == 0.0f || Float.isNaN(f)) ? f : copySign(1.0f, f);
    }

    public static double copySign(double magnitude, double sign) {
        return Double.longBitsToDouble((Double.doubleToRawLongBits(sign) & Long.MIN_VALUE) | (Double.doubleToRawLongBits(magnitude) & Long.MAX_VALUE));
    }

    public static float copySign(float magnitude, float sign) {
        return Float.intBitsToFloat((Float.floatToRawIntBits(sign) & Integer.MIN_VALUE) | (Float.floatToRawIntBits(magnitude) & Integer.MAX_VALUE));
    }

    public static int getExponent(float f) {
        return ((Float.floatToRawIntBits(f) & FloatConsts.EXP_BIT_MASK) >> 23) - 127;
    }

    public static int getExponent(double d) {
        return (int) (((Double.doubleToRawLongBits(d) & DoubleConsts.EXP_BIT_MASK) >> 52) - 1023);
    }

    public static double nextAfter(double start, double direction) {
        long transducer;
        if (Double.isNaN(start) || Double.isNaN(direction)) {
            return start + direction;
        }
        if (start == direction) {
            return direction;
        }
        long transducer2 = Double.doubleToRawLongBits(0.0d + start);
        long j = 1;
        if (direction > start) {
            if (transducer2 < 0) {
                j = -1;
            }
            transducer = transducer2 + j;
        } else if (transducer2 > 0) {
            transducer = transducer2 - 1;
        } else if (transducer2 < 0) {
            transducer = transducer2 + 1;
        } else {
            transducer = -9223372036854775807L;
        }
        return Double.longBitsToDouble(transducer);
    }

    public static float nextAfter(float start, double direction) {
        int transducer;
        if (Float.isNaN(start) || Double.isNaN(direction)) {
            return ((float) direction) + start;
        }
        if (((double) start) == direction) {
            return (float) direction;
        }
        int transducer2 = Float.floatToRawIntBits(0.0f + start);
        int i = -1;
        if (direction > ((double) start)) {
            if (transducer2 >= 0) {
                i = 1;
            }
            transducer = transducer2 + i;
        } else if (transducer2 > 0) {
            transducer = transducer2 - 1;
        } else if (transducer2 < 0) {
            transducer = transducer2 + 1;
        } else {
            transducer = -2147483647;
        }
        return Float.intBitsToFloat(transducer);
    }

    public static double nextUp(double d) {
        if (Double.isNaN(d) || d == Double.POSITIVE_INFINITY) {
            return d;
        }
        double d2 = d + 0.0d;
        return Double.longBitsToDouble(Double.doubleToRawLongBits(d2) + (d2 >= 0.0d ? 1 : -1));
    }

    public static float nextUp(float f) {
        if (Float.isNaN(f) || f == Float.POSITIVE_INFINITY) {
            return f;
        }
        float f2 = f + 0.0f;
        return Float.intBitsToFloat(Float.floatToRawIntBits(f2) + (f2 >= 0.0f ? 1 : -1));
    }

    public static double nextDown(double d) {
        if (Double.isNaN(d) || d == Double.NEGATIVE_INFINITY) {
            return d;
        }
        if (d == 0.0d) {
            return -4.9E-324d;
        }
        return Double.longBitsToDouble(Double.doubleToRawLongBits(d) + (d > 0.0d ? -1 : 1));
    }

    public static float nextDown(float f) {
        if (Float.isNaN(f) || f == Float.NEGATIVE_INFINITY) {
            return f;
        }
        if (f == 0.0f) {
            return -1.4E-45f;
        }
        return Float.intBitsToFloat(Float.floatToRawIntBits(f) + (f > 0.0f ? -1 : 1));
    }

    public static double scalb(double d, int scaleFactor) {
        int scaleFactor2;
        double exp_delta;
        int scale_increment;
        if (scaleFactor < 0) {
            scaleFactor2 = max(scaleFactor, -2099);
            scale_increment = -512;
            exp_delta = twoToTheDoubleScaleDown;
        } else {
            scaleFactor2 = min(scaleFactor, 2099);
            scale_increment = 512;
            exp_delta = twoToTheDoubleScaleUp;
        }
        int t = (scaleFactor2 >> 8) >>> 23;
        int exp_adjust = ((scaleFactor2 + t) & 511) - t;
        double d2 = d * powerOfTwoD(exp_adjust);
        for (int scaleFactor3 = scaleFactor2 - exp_adjust; scaleFactor3 != 0; scaleFactor3 -= scale_increment) {
            d2 *= exp_delta;
        }
        return d2;
    }

    public static float scalb(float f, int scaleFactor) {
        return (float) (((double) f) * powerOfTwoD(max(min(scaleFactor, 278), -278)));
    }

    static double powerOfTwoD(int n) {
        return Double.longBitsToDouble(((((long) n) + 1023) << 52) & DoubleConsts.EXP_BIT_MASK);
    }

    static float powerOfTwoF(int n) {
        return Float.intBitsToFloat(((n + 127) << 23) & FloatConsts.EXP_BIT_MASK);
    }
}
