package sun.misc;

import java.lang.reflect.Modifier;
import java.util.regex.Pattern;
import sun.util.logging.PlatformLogger;

public class FpUtils {
    static final /* synthetic */ boolean -assertionsDisabled = false;
    static double twoToTheDoubleScaleDown;
    static double twoToTheDoubleScaleUp;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.misc.FpUtils.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.misc.FpUtils.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.misc.FpUtils.<clinit>():void");
    }

    private FpUtils() {
    }

    public static int getExponent(double d) {
        return (int) (((Double.doubleToRawLongBits(d) & DoubleConsts.EXP_BIT_MASK) >> 52) - 1023);
    }

    public static int getExponent(float f) {
        return ((Float.floatToRawIntBits(f) & FloatConsts.EXP_BIT_MASK) >> 23) - 127;
    }

    static double powerOfTwoD(int n) {
        Object obj = null;
        if (!-assertionsDisabled) {
            if (n >= DoubleConsts.MIN_EXPONENT && n <= DoubleConsts.MAX_EXPONENT) {
                obj = 1;
            }
            if (obj == null) {
                throw new AssertionError();
            }
        }
        return Double.longBitsToDouble(((((long) n) + 1023) << 52) & DoubleConsts.EXP_BIT_MASK);
    }

    static float powerOfTwoF(int n) {
        Object obj = null;
        if (!-assertionsDisabled) {
            if (n >= FloatConsts.MIN_EXPONENT && n <= FloatConsts.MAX_EXPONENT) {
                obj = 1;
            }
            if (obj == null) {
                throw new AssertionError();
            }
        }
        return Float.intBitsToFloat(((n + FloatConsts.MAX_EXPONENT) << 23) & FloatConsts.EXP_BIT_MASK);
    }

    public static double rawCopySign(double magnitude, double sign) {
        return Double.longBitsToDouble((Double.doubleToRawLongBits(sign) & Long.MIN_VALUE) | (Double.doubleToRawLongBits(magnitude) & Long.MAX_VALUE));
    }

    public static float rawCopySign(float magnitude, float sign) {
        return Float.intBitsToFloat((Float.floatToRawIntBits(sign) & PlatformLogger.ALL) | (Float.floatToRawIntBits(magnitude) & PlatformLogger.OFF));
    }

    public static boolean isFinite(double d) {
        return Math.abs(d) <= DoubleConsts.MAX_VALUE;
    }

    public static boolean isFinite(float f) {
        return Math.abs(f) <= FloatConsts.MAX_VALUE;
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
        Object obj = 1;
        Object obj2 = null;
        int exponent = getExponent(d);
        switch (exponent) {
            case -1023:
                if (d == 0.0d) {
                    return -268435456;
                }
                long transducer = Double.doubleToRawLongBits(d) & DoubleConsts.SIGNIF_BIT_MASK;
                if (!-assertionsDisabled) {
                    if ((transducer != 0 ? 1 : null) == null) {
                        throw new AssertionError();
                    }
                }
                while (transducer < 4503599627370496L) {
                    transducer *= 2;
                    exponent--;
                }
                exponent++;
                if (!-assertionsDisabled) {
                    if (exponent >= DoubleConsts.MIN_SUB_EXPONENT && exponent < DoubleConsts.MIN_EXPONENT) {
                        obj2 = 1;
                    }
                    if (obj2 == null) {
                        throw new AssertionError();
                    }
                }
                return exponent;
            case Record.maxExpansion /*1024*/:
                if (isNaN(d)) {
                    return 1073741824;
                }
                return 268435456;
            default:
                if (!-assertionsDisabled) {
                    if (exponent < DoubleConsts.MIN_EXPONENT) {
                        obj = null;
                    } else if (exponent > DoubleConsts.MAX_EXPONENT) {
                        obj = null;
                    }
                    if (obj == null) {
                        throw new AssertionError();
                    }
                }
                return exponent;
        }
    }

    public static int ilogb(float f) {
        Object obj = 1;
        Object obj2 = null;
        int exponent = getExponent(f);
        switch (exponent) {
            case -127:
                if (f == 0.0f) {
                    return -268435456;
                }
                int transducer = Float.floatToRawIntBits(f) & FloatConsts.SIGNIF_BIT_MASK;
                if (!-assertionsDisabled) {
                    if ((transducer != 0 ? 1 : null) == null) {
                        throw new AssertionError();
                    }
                }
                while (transducer < 8388608) {
                    transducer *= 2;
                    exponent--;
                }
                exponent++;
                if (!-assertionsDisabled) {
                    if (exponent >= FloatConsts.MIN_SUB_EXPONENT && exponent < FloatConsts.MIN_EXPONENT) {
                        obj2 = 1;
                    }
                    if (obj2 == null) {
                        throw new AssertionError();
                    }
                }
                return exponent;
            case Pattern.CANON_EQ /*128*/:
                if (isNaN(f)) {
                    return 1073741824;
                }
                return 268435456;
            default:
                if (!-assertionsDisabled) {
                    if (exponent < FloatConsts.MIN_EXPONENT) {
                        obj = null;
                    } else if (exponent > FloatConsts.MAX_EXPONENT) {
                        obj = null;
                    }
                    if (obj == null) {
                        throw new AssertionError();
                    }
                }
                return exponent;
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
            scale_increment = Modifier.INTERFACE;
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
        if (isNaN(d) || d == DoubleConsts.POSITIVE_INFINITY) {
            return d;
        }
        d += 0.0d;
        return Double.longBitsToDouble((d >= 0.0d ? 1 : -1) + Double.doubleToRawLongBits(d));
    }

    public static float nextUp(float f) {
        if (isNaN(f) || f == FloatConsts.POSITIVE_INFINITY) {
            return f;
        }
        f += 0.0f;
        return Float.intBitsToFloat((f >= 0.0f ? 1 : -1) + Float.floatToRawIntBits(f));
    }

    public static double nextDown(double d) {
        if (isNaN(d) || d == DoubleConsts.NEGATIVE_INFINITY) {
            return d;
        }
        if (d == 0.0d) {
            return -4.9E-324d;
        }
        return Double.longBitsToDouble((d > 0.0d ? -1 : 1) + Double.doubleToRawLongBits(d));
    }

    public static double nextDown(float f) {
        if (isNaN(f) || f == FloatConsts.NEGATIVE_INFINITY) {
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
        Object obj = null;
        int exp = getExponent(d);
        switch (exp) {
            case -1023:
                return DoubleConsts.MIN_VALUE;
            case Record.maxExpansion /*1024*/:
                return Math.abs(d);
            default:
                if (!-assertionsDisabled) {
                    if (exp <= DoubleConsts.MAX_EXPONENT && exp >= DoubleConsts.MIN_EXPONENT) {
                        obj = 1;
                    }
                    if (obj == null) {
                        throw new AssertionError();
                    }
                }
                exp -= 52;
                if (exp >= DoubleConsts.MIN_EXPONENT) {
                    return powerOfTwoD(exp);
                }
                return Double.longBitsToDouble(1 << (exp + 1074));
        }
    }

    public static float ulp(float f) {
        int i = 0;
        int exp = getExponent(f);
        switch (exp) {
            case -127:
                return FloatConsts.MIN_VALUE;
            case Pattern.CANON_EQ /*128*/:
                return Math.abs(f);
            default:
                if (!-assertionsDisabled) {
                    if (exp <= FloatConsts.MAX_EXPONENT && exp >= FloatConsts.MIN_EXPONENT) {
                        i = 1;
                    }
                    if (i == 0) {
                        throw new AssertionError();
                    }
                }
                exp -= 23;
                if (exp >= FloatConsts.MIN_EXPONENT) {
                    return powerOfTwoF(exp);
                }
                return Float.intBitsToFloat(1 << (exp + 149));
        }
    }

    public static double signum(double d) {
        return (d == 0.0d || isNaN(d)) ? d : copySign(1.0d, d);
    }

    public static float signum(float f) {
        return (f == 0.0f || isNaN(f)) ? f : copySign(1.0f, f);
    }
}
