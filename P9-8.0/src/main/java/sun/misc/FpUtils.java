package sun.misc;

public class FpUtils {
    static final /* synthetic */ boolean -assertionsDisabled = (FpUtils.class.desiredAssertionStatus() ^ 1);
    static double twoToTheDoubleScaleDown = powerOfTwoD(-512);
    static double twoToTheDoubleScaleUp = powerOfTwoD(512);

    private FpUtils() {
    }

    public static int getExponent(double d) {
        return (int) (((Double.doubleToRawLongBits(d) & DoubleConsts.EXP_BIT_MASK) >> 52) - 1023);
    }

    public static int getExponent(float f) {
        return ((Float.floatToRawIntBits(f) & FloatConsts.EXP_BIT_MASK) >> 23) - 127;
    }

    static double powerOfTwoD(int n) {
        if (-assertionsDisabled || (n >= -1022 && n <= 1023)) {
            return Double.longBitsToDouble(((((long) n) + 1023) << 52) & DoubleConsts.EXP_BIT_MASK);
        }
        throw new AssertionError();
    }

    static float powerOfTwoF(int n) {
        if (-assertionsDisabled || (n >= -126 && n <= 127)) {
            return Float.intBitsToFloat(((n + 127) << 23) & FloatConsts.EXP_BIT_MASK);
        }
        throw new AssertionError();
    }

    public static double rawCopySign(double magnitude, double sign) {
        return Double.longBitsToDouble((Double.doubleToRawLongBits(sign) & Long.MIN_VALUE) | (Double.doubleToRawLongBits(magnitude) & Long.MAX_VALUE));
    }

    public static float rawCopySign(float magnitude, float sign) {
        return Float.intBitsToFloat((Float.floatToRawIntBits(sign) & Integer.MIN_VALUE) | (Float.floatToRawIntBits(magnitude) & Integer.MAX_VALUE));
    }

    public static boolean isFinite(double d) {
        return Math.abs(d) <= Double.MAX_VALUE;
    }

    public static boolean isFinite(float f) {
        return Math.abs(f) <= Float.MAX_VALUE;
    }

    public static boolean isInfinite(double d) {
        return Double.isInfinite(d);
    }

    public static boolean isInfinite(float f) {
        return Float.isInfinite(f);
    }

    public static boolean isNaN(double d) {
        return Double.isNaN(d);
    }

    public static boolean isNaN(float f) {
        return Float.isNaN(f);
    }

    public static boolean isUnordered(double arg1, double arg2) {
        return !isNaN(arg1) ? isNaN(arg2) : true;
    }

    public static boolean isUnordered(float arg1, float arg2) {
        return !isNaN(arg1) ? isNaN(arg2) : true;
    }

    public static int ilogb(double d) {
        int exponent = getExponent(d);
        switch (exponent) {
            case -1023:
                if (d == 0.0d) {
                    return -268435456;
                }
                long transducer = Double.doubleToRawLongBits(d) & DoubleConsts.SIGNIF_BIT_MASK;
                if (-assertionsDisabled || transducer != 0) {
                    while (transducer < 4503599627370496L) {
                        transducer *= 2;
                        exponent--;
                    }
                    exponent++;
                    if (-assertionsDisabled || (exponent >= DoubleConsts.MIN_SUB_EXPONENT && exponent < -1022)) {
                        return exponent;
                    }
                    throw new AssertionError();
                }
                throw new AssertionError();
            case 1024:
                if (isNaN(d)) {
                    return 1073741824;
                }
                return 268435456;
            default:
                if (-assertionsDisabled || (exponent >= -1022 && exponent <= 1023)) {
                    return exponent;
                }
                throw new AssertionError();
        }
    }

    public static int ilogb(float f) {
        int exponent = getExponent(f);
        switch (exponent) {
            case -127:
                if (f == 0.0f) {
                    return -268435456;
                }
                int transducer = Float.floatToRawIntBits(f) & FloatConsts.SIGNIF_BIT_MASK;
                if (-assertionsDisabled || transducer != 0) {
                    while (transducer < 8388608) {
                        transducer *= 2;
                        exponent--;
                    }
                    exponent++;
                    if (-assertionsDisabled || (exponent >= FloatConsts.MIN_SUB_EXPONENT && exponent < -126)) {
                        return exponent;
                    }
                    throw new AssertionError();
                }
                throw new AssertionError();
            case 128:
                if (isNaN(f)) {
                    return 1073741824;
                }
                return 268435456;
            default:
                if (-assertionsDisabled || (exponent >= -126 && exponent <= 127)) {
                    return exponent;
                }
                throw new AssertionError();
        }
    }

    public static double scalb(double d, int scale_factor) {
        int scale_increment;
        double exp_delta;
        if (scale_factor < 0) {
            scale_factor = Math.max(scale_factor, -2099);
            scale_increment = -512;
            exp_delta = twoToTheDoubleScaleDown;
        } else {
            scale_factor = Math.min(scale_factor, 2099);
            scale_increment = 512;
            exp_delta = twoToTheDoubleScaleUp;
        }
        int t = (scale_factor >> 8) >>> 23;
        int exp_adjust = ((scale_factor + t) & 511) - t;
        d *= powerOfTwoD(exp_adjust);
        for (scale_factor -= exp_adjust; scale_factor != 0; scale_factor -= scale_increment) {
            d *= exp_delta;
        }
        return d;
    }

    public static float scalb(float f, int scale_factor) {
        return (float) (((double) f) * powerOfTwoD(Math.max(Math.min(scale_factor, 278), -278)));
    }

    public static double nextAfter(double start, double direction) {
        long j = 1;
        if (isNaN(start) || isNaN(direction)) {
            return start + direction;
        }
        if (start == direction) {
            return direction;
        }
        long transducer = Double.doubleToRawLongBits(0.0d + start);
        if (direction > start) {
            if (transducer < 0) {
                j = -1;
            }
            transducer += j;
        } else {
            if (!-assertionsDisabled) {
                if ((direction < start ? 1 : null) == null) {
                    throw new AssertionError();
                }
            }
            if (transducer > 0) {
                transducer--;
            } else if (transducer < 0) {
                transducer++;
            } else {
                transducer = -9223372036854775807L;
            }
        }
        return Double.longBitsToDouble(transducer);
    }

    public static float nextAfter(float start, double direction) {
        int i = 1;
        if (isNaN(start) || isNaN(direction)) {
            return ((float) direction) + start;
        }
        if (((double) start) == direction) {
            return (float) direction;
        }
        int transducer = Float.floatToRawIntBits(0.0f + start);
        if (direction > ((double) start)) {
            if (transducer < 0) {
                i = -1;
            }
            transducer += i;
        } else {
            if (!-assertionsDisabled) {
                if (direction >= ((double) start)) {
                    i = 0;
                }
                if (i == 0) {
                    throw new AssertionError();
                }
            }
            if (transducer > 0) {
                transducer--;
            } else if (transducer < 0) {
                transducer++;
            } else {
                transducer = -2147483647;
            }
        }
        return Float.intBitsToFloat(transducer);
    }

    public static double nextUp(double d) {
        if (isNaN(d) || d == Double.POSITIVE_INFINITY) {
            return d;
        }
        d += 0.0d;
        return Double.longBitsToDouble((d >= 0.0d ? 1 : -1) + Double.doubleToRawLongBits(d));
    }

    public static float nextUp(float f) {
        if (isNaN(f) || f == Float.POSITIVE_INFINITY) {
            return f;
        }
        f += 0.0f;
        return Float.intBitsToFloat((f >= 0.0f ? 1 : -1) + Float.floatToRawIntBits(f));
    }

    public static double nextDown(double d) {
        if (isNaN(d) || d == Double.NEGATIVE_INFINITY) {
            return d;
        }
        if (d == 0.0d) {
            return -4.9E-324d;
        }
        return Double.longBitsToDouble((d > 0.0d ? -1 : 1) + Double.doubleToRawLongBits(d));
    }

    public static double nextDown(float f) {
        if (isNaN(f) || f == Float.NEGATIVE_INFINITY) {
            return (double) f;
        }
        if (f == 0.0f) {
            return -1.401298464324817E-45d;
        }
        return (double) Float.intBitsToFloat((f > 0.0f ? -1 : 1) + Float.floatToRawIntBits(f));
    }

    public static double copySign(double magnitude, double sign) {
        if (isNaN(sign)) {
            sign = 1.0d;
        }
        return rawCopySign(magnitude, sign);
    }

    public static float copySign(float magnitude, float sign) {
        if (isNaN(sign)) {
            sign = 1.0f;
        }
        return rawCopySign(magnitude, sign);
    }

    public static double ulp(double d) {
        int exp = getExponent(d);
        switch (exp) {
            case -1023:
                return Double.MIN_VALUE;
            case 1024:
                return Math.abs(d);
            default:
                if (-assertionsDisabled || (exp <= 1023 && exp >= -1022)) {
                    exp -= 52;
                    if (exp >= -1022) {
                        return powerOfTwoD(exp);
                    }
                    return Double.longBitsToDouble(1 << (exp + 1074));
                }
                throw new AssertionError();
        }
    }

    public static float ulp(float f) {
        int exp = getExponent(f);
        switch (exp) {
            case -127:
                return Float.MIN_VALUE;
            case 128:
                return Math.abs(f);
            default:
                if (-assertionsDisabled || (exp <= 127 && exp >= -126)) {
                    exp -= 23;
                    if (exp >= -126) {
                        return powerOfTwoF(exp);
                    }
                    return Float.intBitsToFloat(1 << (exp + 149));
                }
                throw new AssertionError();
        }
    }

    public static double signum(double d) {
        return (d == 0.0d || isNaN(d)) ? d : copySign(1.0d, d);
    }

    public static float signum(float f) {
        return (f == 0.0f || isNaN(f)) ? f : copySign(1.0f, f);
    }
}
