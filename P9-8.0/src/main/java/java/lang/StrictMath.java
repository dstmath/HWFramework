package java.lang;

import java.util.Random;
import sun.misc.DoubleConsts;

public final class StrictMath {
    static final /* synthetic */ boolean -assertionsDisabled = (StrictMath.class.desiredAssertionStatus() ^ 1);
    public static final double E = 2.718281828459045d;
    public static final double PI = 3.141592653589793d;

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

    public static native double cos(double d);

    public static native double cosh(double d);

    public static native double exp(double d);

    public static native double expm1(double d);

    public static native double hypot(double d, double d2);

    public static native double log(double d);

    public static native double log10(double d);

    public static native double log1p(double d);

    public static native double pow(double d, double d2);

    public static native double sin(double d);

    public static native double sinh(double d);

    public static native double sqrt(double d);

    public static native double tan(double d);

    public static native double tanh(double d);

    private StrictMath() {
    }

    public static double toRadians(double angdeg) {
        return (angdeg / 180.0d) * 3.141592653589793d;
    }

    public static double toDegrees(double angrad) {
        return (180.0d * angrad) / 3.141592653589793d;
    }

    public static double ceil(double a) {
        return floorOrCeil(a, -0.0d, 1.0d, 1.0d);
    }

    public static double floor(double a) {
        return floorOrCeil(a, -1.0d, 0.0d, -1.0d);
    }

    private static double floorOrCeil(double a, double negativeBoundary, double positiveBoundary, double sign) {
        int exponent = Math.getExponent(a);
        if (exponent < 0) {
            if (a != 0.0d) {
                a = a < 0.0d ? negativeBoundary : positiveBoundary;
            }
            return a;
        } else if (exponent >= 52) {
            return a;
        } else {
            if (-assertionsDisabled || (exponent >= 0 && exponent <= 51)) {
                long doppel = Double.doubleToRawLongBits(a);
                long mask = DoubleConsts.SIGNIF_BIT_MASK >> exponent;
                if ((mask & doppel) == 0) {
                    return a;
                }
                double result = Double.longBitsToDouble((~mask) & doppel);
                if (sign * a > 0.0d) {
                    result += sign;
                }
                return result;
            }
            throw new AssertionError();
        }
    }

    public static double rint(double a) {
        double sign = Math.copySign(1.0d, a);
        a = Math.abs(a);
        if (a < 4.503599627370496E15d) {
            a = (4.503599627370496E15d + a) - 4.503599627370496E15d;
        }
        return sign * a;
    }

    public static int round(float a) {
        return Math.round(a);
    }

    public static long round(double a) {
        return Math.round(a);
    }

    public static double random() {
        return RandomNumberGeneratorHolder.randomNumberGenerator.nextDouble();
    }

    public static int addExact(int x, int y) {
        return Math.addExact(x, y);
    }

    public static long addExact(long x, long y) {
        return Math.addExact(x, y);
    }

    public static int subtractExact(int x, int y) {
        return Math.subtractExact(x, y);
    }

    public static long subtractExact(long x, long y) {
        return Math.subtractExact(x, y);
    }

    public static int multiplyExact(int x, int y) {
        return Math.multiplyExact(x, y);
    }

    public static long multiplyExact(long x, long y) {
        return Math.multiplyExact(x, y);
    }

    public static int toIntExact(long value) {
        return Math.toIntExact(value);
    }

    public static int floorDiv(int x, int y) {
        return Math.floorDiv(x, y);
    }

    public static long floorDiv(long x, long y) {
        return Math.floorDiv(x, y);
    }

    public static int floorMod(int x, int y) {
        return Math.floorMod(x, y);
    }

    public static long floorMod(long x, long y) {
        return Math.floorMod(x, y);
    }

    public static int abs(int a) {
        return Math.abs(a);
    }

    public static long abs(long a) {
        return Math.abs(a);
    }

    public static float abs(float a) {
        return Math.abs(a);
    }

    public static double abs(double a) {
        return Math.abs(a);
    }

    public static int max(int a, int b) {
        return Math.max(a, b);
    }

    public static long max(long a, long b) {
        return Math.max(a, b);
    }

    public static float max(float a, float b) {
        return Math.max(a, b);
    }

    public static double max(double a, double b) {
        return Math.max(a, b);
    }

    public static int min(int a, int b) {
        return Math.min(a, b);
    }

    public static long min(long a, long b) {
        return Math.min(a, b);
    }

    public static float min(float a, float b) {
        return Math.min(a, b);
    }

    public static double min(double a, double b) {
        return Math.min(a, b);
    }

    public static double ulp(double d) {
        return Math.ulp(d);
    }

    public static float ulp(float f) {
        return Math.ulp(f);
    }

    public static double signum(double d) {
        return Math.signum(d);
    }

    public static float signum(float f) {
        return Math.signum(f);
    }

    public static double copySign(double magnitude, double sign) {
        if (Double.isNaN(sign)) {
            sign = 1.0d;
        }
        return Math.copySign(magnitude, sign);
    }

    public static float copySign(float magnitude, float sign) {
        if (Float.isNaN(sign)) {
            sign = 1.0f;
        }
        return Math.copySign(magnitude, sign);
    }

    public static int getExponent(float f) {
        return Math.getExponent(f);
    }

    public static int getExponent(double d) {
        return Math.getExponent(d);
    }

    public static double nextAfter(double start, double direction) {
        return Math.nextAfter(start, direction);
    }

    public static float nextAfter(float start, double direction) {
        return Math.nextAfter(start, direction);
    }

    public static double nextUp(double d) {
        return Math.nextUp(d);
    }

    public static float nextUp(float f) {
        return Math.nextUp(f);
    }

    public static double nextDown(double d) {
        return Math.nextDown(d);
    }

    public static float nextDown(float f) {
        return Math.nextDown(f);
    }

    public static double scalb(double d, int scaleFactor) {
        return Math.scalb(d, scaleFactor);
    }

    public static float scalb(float f, int scaleFactor) {
        return Math.scalb(f, scaleFactor);
    }
}
