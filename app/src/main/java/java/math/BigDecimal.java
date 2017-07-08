package java.math;

import android.icu.util.AnnualTimeZoneRule;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import javax.xml.datatype.DatatypeConstants;
import libcore.math.MathUtils;
import org.w3c.dom.traversal.NodeFilter;

public class BigDecimal extends Number implements Comparable<BigDecimal>, Serializable {
    private static final /* synthetic */ int[] -java-math-RoundingModeSwitchesValues = null;
    private static final BigDecimal[] BI_SCALED_BY_ZERO = null;
    private static final int BI_SCALED_BY_ZERO_LENGTH = 11;
    private static final char[] CH_ZEROS = null;
    private static final BigInteger[] FIVE_POW = null;
    private static final double LOG10_2 = 0.3010299956639812d;
    private static final long[] LONG_FIVE_POW = null;
    private static final int[] LONG_FIVE_POW_BIT_LENGTH = null;
    private static final int[] LONG_POWERS_OF_TEN_BIT_LENGTH = null;
    public static final BigDecimal ONE = null;
    public static final int ROUND_CEILING = 2;
    public static final int ROUND_DOWN = 1;
    public static final int ROUND_FLOOR = 3;
    public static final int ROUND_HALF_DOWN = 5;
    public static final int ROUND_HALF_EVEN = 6;
    public static final int ROUND_HALF_UP = 4;
    public static final int ROUND_UNNECESSARY = 7;
    public static final int ROUND_UP = 0;
    public static final BigDecimal TEN = null;
    private static final BigInteger[] TEN_POW = null;
    public static final BigDecimal ZERO = null;
    private static final BigDecimal[] ZERO_SCALED_BY = null;
    private static final long serialVersionUID = 6108874887143696463L;
    private transient int bitLength;
    private transient int hashCode;
    private BigInteger intVal;
    private transient int precision;
    private int scale;
    private transient long smallValue;
    private transient String toStringImage;

    private static /* synthetic */ int[] -getjava-math-RoundingModeSwitchesValues() {
        if (-java-math-RoundingModeSwitchesValues != null) {
            return -java-math-RoundingModeSwitchesValues;
        }
        int[] iArr = new int[RoundingMode.values().length];
        try {
            iArr[RoundingMode.CEILING.ordinal()] = ROUND_DOWN;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[RoundingMode.DOWN.ordinal()] = ROUND_CEILING;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[RoundingMode.FLOOR.ordinal()] = ROUND_FLOOR;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[RoundingMode.HALF_DOWN.ordinal()] = ROUND_HALF_UP;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[RoundingMode.HALF_EVEN.ordinal()] = ROUND_HALF_DOWN;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[RoundingMode.HALF_UP.ordinal()] = ROUND_HALF_EVEN;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[RoundingMode.UNNECESSARY.ordinal()] = ROUND_UNNECESSARY;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[RoundingMode.UP.ordinal()] = 8;
        } catch (NoSuchFieldError e8) {
        }
        -java-math-RoundingModeSwitchesValues = iArr;
        return iArr;
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.math.BigDecimal.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.math.BigDecimal.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: java.math.BigDecimal.<clinit>():void");
    }

    private static int bitLength(int r1) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.math.BigDecimal.bitLength(int):int
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 7 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: java.math.BigDecimal.bitLength(int):int");
    }

    private static int bitLength(long r1) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.math.BigDecimal.bitLength(long):int
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-long
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 7 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: java.math.BigDecimal.bitLength(long):int");
    }

    private BigDecimal(long smallValue, int scale) {
        this.toStringImage = null;
        this.hashCode = ROUND_UP;
        this.precision = ROUND_UP;
        this.smallValue = smallValue;
        this.scale = scale;
        this.bitLength = bitLength(smallValue);
    }

    private BigDecimal(int smallValue, int scale) {
        this.toStringImage = null;
        this.hashCode = ROUND_UP;
        this.precision = ROUND_UP;
        this.smallValue = (long) smallValue;
        this.scale = scale;
        this.bitLength = bitLength(smallValue);
    }

    public BigDecimal(char[] in, int offset, int len) {
        this.toStringImage = null;
        this.hashCode = ROUND_UP;
        this.precision = ROUND_UP;
        int begin = offset;
        int last = offset + (len - 1);
        if (in == null) {
            throw new NullPointerException("in == null");
        } else if (last >= in.length || offset < 0 || len <= 0 || last < 0) {
            throw new NumberFormatException("Bad offset/length: offset=" + offset + " len=" + len + " in.length=" + in.length);
        } else {
            int offset2;
            StringBuilder unscaledBuffer = new StringBuilder(len);
            if (offset <= last && in[offset] == '+') {
                offset += ROUND_DOWN;
                begin += ROUND_DOWN;
            }
            int counter = ROUND_UP;
            boolean wasNonZero = false;
            while (offset <= last && in[offset] != '.' && in[offset] != 'e' && in[offset] != 'E') {
                if (!wasNonZero) {
                    if (in[offset] == '0') {
                        counter += ROUND_DOWN;
                    } else {
                        wasNonZero = true;
                    }
                }
                offset += ROUND_DOWN;
            }
            unscaledBuffer.append(in, begin, offset - begin);
            int bufLength = (offset - begin) + ROUND_UP;
            if (offset > last || in[offset] != '.') {
                this.scale = ROUND_UP;
            } else {
                offset += ROUND_DOWN;
                begin = offset;
                offset2 = offset;
                while (offset2 <= last && in[offset2] != 'e' && in[offset2] != 'E') {
                    if (!wasNonZero) {
                        if (in[offset2] == '0') {
                            counter += ROUND_DOWN;
                        } else {
                            wasNonZero = true;
                        }
                    }
                    offset2 += ROUND_DOWN;
                }
                this.scale = offset2 - offset;
                bufLength += this.scale;
                unscaledBuffer.append(in, offset, this.scale);
                offset = offset2;
            }
            if (offset <= last && (in[offset] == 'e' || in[offset] == 'E')) {
                offset += ROUND_DOWN;
                begin = offset;
                if (offset <= last && in[offset] == '+') {
                    offset2 = offset + ROUND_DOWN;
                    if (offset2 > last || in[offset2] == '-') {
                    } else {
                        begin = offset + ROUND_DOWN;
                        offset = offset2;
                    }
                }
                long newScale = ((long) this.scale) - ((long) Integer.parseInt(String.valueOf(in, begin, (last + ROUND_DOWN) - begin)));
                this.scale = (int) newScale;
                if (newScale != ((long) this.scale)) {
                    throw new NumberFormatException("Scale out of range");
                }
            }
            if (bufLength < 19) {
                this.smallValue = Long.parseLong(unscaledBuffer.toString());
                this.bitLength = bitLength(this.smallValue);
                return;
            }
            setUnscaledValue(new BigInteger(unscaledBuffer.toString()));
        }
    }

    public BigDecimal(char[] in, int offset, int len, MathContext mc) {
        this(in, offset, len);
        inplaceRound(mc);
    }

    public BigDecimal(char[] in) {
        this(in, (int) ROUND_UP, in.length);
    }

    public BigDecimal(char[] in, MathContext mc) {
        this(in, (int) ROUND_UP, in.length);
        inplaceRound(mc);
    }

    public BigDecimal(String val) {
        this(val.toCharArray(), (int) ROUND_UP, val.length());
    }

    public BigDecimal(String val, MathContext mc) {
        this(val.toCharArray(), (int) ROUND_UP, val.length());
        inplaceRound(mc);
    }

    public BigDecimal(double val) {
        this.toStringImage = null;
        this.hashCode = ROUND_UP;
        this.precision = ROUND_UP;
        if (Double.isInfinite(val) || Double.isNaN(val)) {
            throw new NumberFormatException("Infinity or NaN: " + val);
        }
        long mantissa;
        long bits = Double.doubleToLongBits(val);
        this.scale = 1075 - ((int) ((bits >> 52) & 2047));
        if (this.scale == 1075) {
            mantissa = (4503599627370495L & bits) << ROUND_DOWN;
        } else {
            mantissa = (4503599627370495L & bits) | 4503599627370496L;
        }
        if (mantissa == 0) {
            this.scale = ROUND_UP;
            this.precision = ROUND_DOWN;
        }
        if (this.scale > 0) {
            int trailingZeros = Math.min(this.scale, Long.numberOfTrailingZeros(mantissa));
            mantissa >>>= trailingZeros;
            this.scale -= trailingZeros;
        }
        if ((bits >> 63) != 0) {
            mantissa = -mantissa;
        }
        int mantissaBits = bitLength(mantissa);
        if (this.scale < 0) {
            this.bitLength = mantissaBits == 0 ? ROUND_UP : mantissaBits - this.scale;
            if (this.bitLength < 64) {
                this.smallValue = mantissa << (-this.scale);
            } else {
                BigInt bi = new BigInt();
                bi.putLongInt(mantissa);
                bi.shift(-this.scale);
                this.intVal = new BigInteger(bi);
            }
            this.scale = ROUND_UP;
        } else if (this.scale <= 0) {
            this.smallValue = mantissa;
            this.bitLength = mantissaBits;
        } else if (this.scale >= LONG_FIVE_POW.length || LONG_FIVE_POW_BIT_LENGTH[this.scale] + mantissaBits >= 64) {
            setUnscaledValue(Multiplication.multiplyByFivePow(BigInteger.valueOf(mantissa), this.scale));
        } else {
            this.smallValue = LONG_FIVE_POW[this.scale] * mantissa;
            this.bitLength = bitLength(this.smallValue);
        }
    }

    public BigDecimal(double val, MathContext mc) {
        this(val);
        inplaceRound(mc);
    }

    public BigDecimal(BigInteger val) {
        this(val, (int) ROUND_UP);
    }

    public BigDecimal(BigInteger val, MathContext mc) {
        this(val);
        inplaceRound(mc);
    }

    public BigDecimal(BigInteger unscaledVal, int scale) {
        this.toStringImage = null;
        this.hashCode = ROUND_UP;
        this.precision = ROUND_UP;
        if (unscaledVal == null) {
            throw new NullPointerException("unscaledVal == null");
        }
        this.scale = scale;
        setUnscaledValue(unscaledVal);
    }

    public BigDecimal(BigInteger unscaledVal, int scale, MathContext mc) {
        this(unscaledVal, scale);
        inplaceRound(mc);
    }

    public BigDecimal(int val) {
        this(val, (int) ROUND_UP);
    }

    public BigDecimal(int val, MathContext mc) {
        this(val, (int) ROUND_UP);
        inplaceRound(mc);
    }

    public BigDecimal(long val) {
        this(val, (int) ROUND_UP);
    }

    public BigDecimal(long val, MathContext mc) {
        this(val);
        inplaceRound(mc);
    }

    public static BigDecimal valueOf(long unscaledVal, int scale) {
        if (scale == 0) {
            return valueOf(unscaledVal);
        }
        if (unscaledVal != 0 || scale < 0 || scale >= ZERO_SCALED_BY.length) {
            return new BigDecimal(unscaledVal, scale);
        }
        return ZERO_SCALED_BY[scale];
    }

    public static BigDecimal valueOf(long unscaledVal) {
        if (unscaledVal < 0 || unscaledVal >= 11) {
            return new BigDecimal(unscaledVal, (int) ROUND_UP);
        }
        return BI_SCALED_BY_ZERO[(int) unscaledVal];
    }

    public static BigDecimal valueOf(double val) {
        if (!Double.isInfinite(val) && !Double.isNaN(val)) {
            return new BigDecimal(Double.toString(val));
        }
        throw new NumberFormatException("Infinity or NaN: " + val);
    }

    public BigDecimal add(BigDecimal augend) {
        int diffScale = this.scale - augend.scale;
        if (isZero()) {
            if (diffScale <= 0) {
                return augend;
            }
            if (augend.isZero()) {
                return this;
            }
        } else if (augend.isZero() && diffScale >= 0) {
            return this;
        }
        if (diffScale == 0) {
            if (Math.max(this.bitLength, augend.bitLength) + ROUND_DOWN < 64) {
                return valueOf(this.smallValue + augend.smallValue, this.scale);
            }
            return new BigDecimal(getUnscaledValue().add(augend.getUnscaledValue()), this.scale);
        } else if (diffScale > 0) {
            return addAndMult10(this, augend, diffScale);
        } else {
            return addAndMult10(augend, this, -diffScale);
        }
    }

    private static BigDecimal addAndMult10(BigDecimal thisValue, BigDecimal augend, int diffScale) {
        if (diffScale < MathUtils.LONG_POWERS_OF_TEN.length && Math.max(thisValue.bitLength, augend.bitLength + LONG_POWERS_OF_TEN_BIT_LENGTH[diffScale]) + ROUND_DOWN < 64) {
            return valueOf(thisValue.smallValue + (augend.smallValue * MathUtils.LONG_POWERS_OF_TEN[diffScale]), thisValue.scale);
        }
        BigInt bi = Multiplication.multiplyByTenPow(augend.getUnscaledValue(), (long) diffScale).getBigInt();
        bi.add(thisValue.getUnscaledValue().getBigInt());
        return new BigDecimal(new BigInteger(bi), thisValue.scale);
    }

    public BigDecimal add(BigDecimal augend, MathContext mc) {
        long diffScale = ((long) this.scale) - ((long) augend.scale);
        if (augend.isZero() || isZero() || mc.getPrecision() == 0) {
            return add(augend).round(mc);
        }
        BigDecimal larger;
        BigDecimal smaller;
        if (((long) approxPrecision()) < diffScale - 1) {
            larger = augend;
            smaller = this;
        } else if (((long) augend.approxPrecision()) >= (-diffScale) - 1) {
            return add(augend).round(mc);
        } else {
            larger = this;
            smaller = augend;
        }
        if (mc.getPrecision() >= larger.approxPrecision()) {
            return add(augend).round(mc);
        }
        BigInteger tempBI;
        int largerSignum = larger.signum();
        if (largerSignum == smaller.signum()) {
            tempBI = Multiplication.multiplyByPositiveInt(larger.getUnscaledValue(), 10).add(BigInteger.valueOf((long) largerSignum));
        } else {
            tempBI = Multiplication.multiplyByPositiveInt(larger.getUnscaledValue().subtract(BigInteger.valueOf((long) largerSignum)), 10).add(BigInteger.valueOf((long) (largerSignum * 9)));
        }
        return new BigDecimal(tempBI, larger.scale + ROUND_DOWN).round(mc);
    }

    public BigDecimal subtract(BigDecimal subtrahend) {
        int diffScale = this.scale - subtrahend.scale;
        if (isZero()) {
            if (diffScale <= 0) {
                return subtrahend.negate();
            }
            if (subtrahend.isZero()) {
                return this;
            }
        } else if (subtrahend.isZero() && diffScale >= 0) {
            return this;
        }
        if (diffScale == 0) {
            if (Math.max(this.bitLength, subtrahend.bitLength) + ROUND_DOWN < 64) {
                return valueOf(this.smallValue - subtrahend.smallValue, this.scale);
            }
            return new BigDecimal(getUnscaledValue().subtract(subtrahend.getUnscaledValue()), this.scale);
        } else if (diffScale <= 0) {
            diffScale = -diffScale;
            if (diffScale >= MathUtils.LONG_POWERS_OF_TEN.length || Math.max(this.bitLength + LONG_POWERS_OF_TEN_BIT_LENGTH[diffScale], subtrahend.bitLength) + ROUND_DOWN >= 64) {
                return new BigDecimal(Multiplication.multiplyByTenPow(getUnscaledValue(), (long) diffScale).subtract(subtrahend.getUnscaledValue()), subtrahend.scale);
            }
            return valueOf((this.smallValue * MathUtils.LONG_POWERS_OF_TEN[diffScale]) - subtrahend.smallValue, subtrahend.scale);
        } else if (diffScale >= MathUtils.LONG_POWERS_OF_TEN.length || Math.max(this.bitLength, subtrahend.bitLength + LONG_POWERS_OF_TEN_BIT_LENGTH[diffScale]) + ROUND_DOWN >= 64) {
            return new BigDecimal(getUnscaledValue().subtract(Multiplication.multiplyByTenPow(subtrahend.getUnscaledValue(), (long) diffScale)), this.scale);
        } else {
            return valueOf(this.smallValue - (subtrahend.smallValue * MathUtils.LONG_POWERS_OF_TEN[diffScale]), this.scale);
        }
    }

    public BigDecimal subtract(BigDecimal subtrahend, MathContext mc) {
        long diffScale = ((long) subtrahend.scale) - ((long) this.scale);
        if (subtrahend.isZero() || isZero() || mc.getPrecision() == 0) {
            return subtract(subtrahend).round(mc);
        }
        if (((long) subtrahend.approxPrecision()) >= diffScale - 1 || mc.getPrecision() >= approxPrecision()) {
            return subtract(subtrahend).round(mc);
        }
        BigInteger tempBI;
        int thisSignum = signum();
        if (thisSignum != subtrahend.signum()) {
            tempBI = Multiplication.multiplyByPositiveInt(getUnscaledValue(), 10).add(BigInteger.valueOf((long) thisSignum));
        } else {
            tempBI = Multiplication.multiplyByPositiveInt(getUnscaledValue().subtract(BigInteger.valueOf((long) thisSignum)), 10).add(BigInteger.valueOf((long) (thisSignum * 9)));
        }
        return new BigDecimal(tempBI, this.scale + ROUND_DOWN).round(mc);
    }

    public BigDecimal multiply(BigDecimal multiplicand) {
        long newScale = ((long) this.scale) + ((long) multiplicand.scale);
        if (isZero() || multiplicand.isZero()) {
            return zeroScaledBy(newScale);
        }
        if (this.bitLength + multiplicand.bitLength < 64) {
            return valueOf(this.smallValue * multiplicand.smallValue, safeLongToInt(newScale));
        }
        return new BigDecimal(getUnscaledValue().multiply(multiplicand.getUnscaledValue()), safeLongToInt(newScale));
    }

    public BigDecimal multiply(BigDecimal multiplicand, MathContext mc) {
        BigDecimal result = multiply(multiplicand);
        result.inplaceRound(mc);
        return result;
    }

    public BigDecimal divide(BigDecimal divisor, int scale, int roundingMode) {
        return divide(divisor, scale, RoundingMode.valueOf(roundingMode));
    }

    public BigDecimal divide(BigDecimal divisor, int scale, RoundingMode roundingMode) {
        if (roundingMode == null) {
            throw new NullPointerException("roundingMode == null");
        } else if (divisor.isZero()) {
            throw new ArithmeticException("Division by zero");
        } else {
            long diffScale = (((long) this.scale) - ((long) divisor.scale)) - ((long) scale);
            if (bitLength(diffScale) > 32) {
                throw new ArithmeticException("Unable to perform divisor / dividend scaling: the difference in scale is too big (" + diffScale + ")");
            }
            if (this.bitLength < 64 && divisor.bitLength < 64) {
                if (diffScale == 0) {
                    return dividePrimitiveLongs(this.smallValue, divisor.smallValue, scale, roundingMode);
                }
                if (diffScale > 0) {
                    if (diffScale < ((long) MathUtils.LONG_POWERS_OF_TEN.length) && divisor.bitLength + LONG_POWERS_OF_TEN_BIT_LENGTH[(int) diffScale] < 64) {
                        return dividePrimitiveLongs(this.smallValue, divisor.smallValue * MathUtils.LONG_POWERS_OF_TEN[(int) diffScale], scale, roundingMode);
                    }
                } else if ((-diffScale) < ((long) MathUtils.LONG_POWERS_OF_TEN.length) && this.bitLength + LONG_POWERS_OF_TEN_BIT_LENGTH[(int) (-diffScale)] < 64) {
                    return dividePrimitiveLongs(this.smallValue * MathUtils.LONG_POWERS_OF_TEN[(int) (-diffScale)], divisor.smallValue, scale, roundingMode);
                }
            }
            BigInteger scaledDividend = getUnscaledValue();
            BigInteger scaledDivisor = divisor.getUnscaledValue();
            if (diffScale > 0) {
                scaledDivisor = Multiplication.multiplyByTenPow(scaledDivisor, (long) ((int) diffScale));
            } else if (diffScale < 0) {
                scaledDividend = Multiplication.multiplyByTenPow(scaledDividend, (long) ((int) (-diffScale)));
            }
            return divideBigIntegers(scaledDividend, scaledDivisor, scale, roundingMode);
        }
    }

    private static BigDecimal divideBigIntegers(BigInteger scaledDividend, BigInteger scaledDivisor, int scale, RoundingMode roundingMode) {
        BigInteger[] quotAndRem = scaledDividend.divideAndRemainder(scaledDivisor);
        BigInteger quotient = quotAndRem[ROUND_UP];
        BigInteger remainder = quotAndRem[ROUND_DOWN];
        if (remainder.signum() == 0) {
            return new BigDecimal(quotient, scale);
        }
        int compRem;
        int sign = scaledDividend.signum() * scaledDivisor.signum();
        if (scaledDivisor.bitLength() < 63) {
            compRem = roundingBehavior(quotient.testBit(ROUND_UP) ? ROUND_DOWN : ROUND_UP, (longCompareTo(Math.abs(remainder.longValue()) * 2, Math.abs(scaledDivisor.longValue())) + ROUND_HALF_DOWN) * sign, roundingMode);
        } else {
            compRem = roundingBehavior(quotient.testBit(ROUND_UP) ? ROUND_DOWN : ROUND_UP, (remainder.abs().shiftLeftOneBit().compareTo(scaledDivisor.abs()) + ROUND_HALF_DOWN) * sign, roundingMode);
        }
        if (compRem == 0) {
            return new BigDecimal(quotient, scale);
        }
        if (quotient.bitLength() < 63) {
            return valueOf(quotient.longValue() + ((long) compRem), scale);
        }
        return new BigDecimal(quotient.add(BigInteger.valueOf((long) compRem)), scale);
    }

    private static BigDecimal dividePrimitiveLongs(long scaledDividend, long scaledDivisor, int scale, RoundingMode roundingMode) {
        long quotient = scaledDividend / scaledDivisor;
        long remainder = scaledDividend % scaledDivisor;
        int sign = Long.signum(scaledDividend) * Long.signum(scaledDivisor);
        if (remainder != 0) {
            quotient += (long) roundingBehavior(((int) quotient) & ROUND_DOWN, (longCompareTo(Math.abs(remainder) * 2, Math.abs(scaledDivisor)) + ROUND_HALF_DOWN) * sign, roundingMode);
        }
        return valueOf(quotient, scale);
    }

    public BigDecimal divide(BigDecimal divisor, int roundingMode) {
        return divide(divisor, this.scale, RoundingMode.valueOf(roundingMode));
    }

    public BigDecimal divide(BigDecimal divisor, RoundingMode roundingMode) {
        return divide(divisor, this.scale, roundingMode);
    }

    public BigDecimal divide(BigDecimal divisor) {
        BigInteger p = getUnscaledValue();
        BigInteger q = divisor.getUnscaledValue();
        long diffScale = ((long) this.scale) - ((long) divisor.scale);
        int l = ROUND_UP;
        int i = ROUND_DOWN;
        int lastPow = FIVE_POW.length - 1;
        if (divisor.isZero()) {
            throw new ArithmeticException("Division by zero");
        } else if (p.signum() == 0) {
            return zeroScaledBy(diffScale);
        } else {
            BigInteger gcd = p.gcd(q);
            p = p.divide(gcd);
            q = q.divide(gcd);
            int k = q.getLowestSetBit();
            q = q.shiftRight(k);
            while (true) {
                BigInteger[] quotAndRem = q.divideAndRemainder(FIVE_POW[i]);
                if (quotAndRem[ROUND_DOWN].signum() == 0) {
                    l += i;
                    if (i < lastPow) {
                        i += ROUND_DOWN;
                    }
                    q = quotAndRem[ROUND_UP];
                } else if (i == ROUND_DOWN) {
                    break;
                } else {
                    i = ROUND_DOWN;
                }
            }
            if (q.abs().equals(BigInteger.ONE)) {
                if (q.signum() < 0) {
                    p = p.negate();
                }
                int newScale = safeLongToInt(((long) Math.max(k, l)) + diffScale);
                i = k - l;
                if (i > 0) {
                    p = Multiplication.multiplyByFivePow(p, i);
                } else {
                    p = p.shiftLeft(-i);
                }
                return new BigDecimal(p, newScale);
            }
            throw new ArithmeticException("Non-terminating decimal expansion; no exact representable decimal result");
        }
    }

    public BigDecimal divide(BigDecimal divisor, MathContext mc) {
        long approxPrecision = (long) approxPrecision();
        long trailingZeros = ((((long) mc.getPrecision()) + 2) + ((long) divisor.approxPrecision())) - r0;
        long diffScale = ((long) this.scale) - ((long) divisor.scale);
        long newScale = diffScale;
        int i = ROUND_DOWN;
        int lastPow = TEN_POW.length - 1;
        BigInteger[] quotAndRem = new BigInteger[ROUND_DOWN];
        quotAndRem[ROUND_UP] = getUnscaledValue();
        if (mc.getPrecision() == 0 || isZero() || divisor.isZero()) {
            return divide(divisor);
        }
        if (trailingZeros > 0) {
            quotAndRem[ROUND_UP] = getUnscaledValue().multiply(Multiplication.powerOf10(trailingZeros));
            newScale = diffScale + trailingZeros;
        }
        quotAndRem = quotAndRem[ROUND_UP].divideAndRemainder(divisor.getUnscaledValue());
        BigInteger integerQuot = quotAndRem[ROUND_UP];
        if (quotAndRem[ROUND_DOWN].signum() != 0) {
            integerQuot = integerQuot.multiply(BigInteger.TEN).add(BigInteger.valueOf((long) (quotAndRem[ROUND_UP].signum() * (quotAndRem[ROUND_DOWN].shiftLeftOneBit().compareTo(divisor.getUnscaledValue()) + ROUND_HALF_DOWN))));
            newScale++;
        } else {
            while (!integerQuot.testBit(ROUND_UP)) {
                quotAndRem = integerQuot.divideAndRemainder(TEN_POW[i]);
                if (quotAndRem[ROUND_DOWN].signum() == 0 && newScale - ((long) i) >= diffScale) {
                    newScale -= (long) i;
                    if (i < lastPow) {
                        i += ROUND_DOWN;
                    }
                    integerQuot = quotAndRem[ROUND_UP];
                } else if (i == ROUND_DOWN) {
                    break;
                } else {
                    i = ROUND_DOWN;
                }
            }
        }
        return new BigDecimal(integerQuot, safeLongToInt(newScale), mc);
    }

    public BigDecimal divideToIntegralValue(BigDecimal divisor) {
        new BigInteger[ROUND_DOWN][ROUND_UP] = getUnscaledValue();
        long newScale = ((long) this.scale) - ((long) divisor.scale);
        long tempScale = 0;
        int i = ROUND_DOWN;
        int lastPow = TEN_POW.length - 1;
        if (divisor.isZero()) {
            throw new ArithmeticException("Division by zero");
        }
        BigInteger integralValue;
        if (((long) divisor.approxPrecision()) + newScale > ((long) approxPrecision()) + 1 || isZero()) {
            integralValue = BigInteger.ZERO;
        } else if (newScale == 0) {
            integralValue = getUnscaledValue().divide(divisor.getUnscaledValue());
        } else if (newScale > 0) {
            BigInteger powerOfTen = Multiplication.powerOf10(newScale);
            integralValue = getUnscaledValue().divide(divisor.getUnscaledValue().multiply(powerOfTen)).multiply(powerOfTen);
        } else {
            integralValue = getUnscaledValue().multiply(Multiplication.powerOf10(-newScale)).divide(divisor.getUnscaledValue());
            while (!integralValue.testBit(ROUND_UP)) {
                BigInteger[] quotAndRem = integralValue.divideAndRemainder(TEN_POW[i]);
                if (quotAndRem[ROUND_DOWN].signum() == 0 && tempScale - ((long) i) >= newScale) {
                    tempScale -= (long) i;
                    if (i < lastPow) {
                        i += ROUND_DOWN;
                    }
                    integralValue = quotAndRem[ROUND_UP];
                } else if (i == ROUND_DOWN) {
                    break;
                } else {
                    i = ROUND_DOWN;
                }
            }
            newScale = tempScale;
        }
        if (integralValue.signum() == 0) {
            return zeroScaledBy(newScale);
        }
        return new BigDecimal(integralValue, safeLongToInt(newScale));
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public BigDecimal divideToIntegralValue(BigDecimal divisor, MathContext mc) {
        int mcPrecision = mc.getPrecision();
        int diffPrecision = precision() - divisor.precision();
        int lastPow = TEN_POW.length - 1;
        long diffScale = ((long) this.scale) - ((long) divisor.scale);
        long newScale = diffScale;
        long quotPrecision = (((long) diffPrecision) - diffScale) + 1;
        BigInteger[] quotAndRem = new BigInteger[ROUND_CEILING];
        if (mcPrecision == 0 || isZero() || divisor.isZero()) {
            return divideToIntegralValue(divisor);
        }
        if (quotPrecision <= 0) {
            quotAndRem[ROUND_UP] = BigInteger.ZERO;
        } else if (diffScale == 0) {
            quotAndRem[ROUND_UP] = getUnscaledValue().divide(divisor.getUnscaledValue());
        } else if (diffScale > 0) {
            quotAndRem[ROUND_UP] = getUnscaledValue().divide(divisor.getUnscaledValue().multiply(Multiplication.powerOf10(diffScale)));
            newScale = Math.min(diffScale, Math.max((((long) mcPrecision) - quotPrecision) + 1, 0));
            quotAndRem[ROUND_UP] = quotAndRem[ROUND_UP].multiply(Multiplication.powerOf10(newScale));
        } else {
            long exp = Math.min(-diffScale, Math.max(((long) mcPrecision) - ((long) diffPrecision), 0));
            quotAndRem = getUnscaledValue().multiply(Multiplication.powerOf10(exp)).divideAndRemainder(divisor.getUnscaledValue());
            newScale = diffScale + exp;
            exp = -newScale;
            if (quotAndRem[ROUND_DOWN].signum() != 0 && exp > 0) {
                long precision = (long) new BigDecimal(quotAndRem[ROUND_DOWN]).precision();
                long compRemDiv = (r0 + exp) - ((long) divisor.precision());
                if (compRemDiv == 0) {
                    quotAndRem[ROUND_DOWN] = quotAndRem[ROUND_DOWN].multiply(Multiplication.powerOf10(exp)).divide(divisor.getUnscaledValue());
                    compRemDiv = (long) Math.abs(quotAndRem[ROUND_DOWN].signum());
                }
                if (compRemDiv > 0) {
                    throw new ArithmeticException("Division impossible");
                }
            }
        }
        if (quotAndRem[ROUND_UP].signum() == 0) {
            return zeroScaledBy(diffScale);
        }
        BigInteger strippedBI = quotAndRem[ROUND_UP];
        BigDecimal integralValue = new BigDecimal(quotAndRem[ROUND_UP]);
        long resultPrecision = (long) integralValue.precision();
        int i = ROUND_DOWN;
        while (!strippedBI.testBit(ROUND_UP)) {
            quotAndRem = strippedBI.divideAndRemainder(TEN_POW[i]);
            if (quotAndRem[ROUND_DOWN].signum() == 0) {
                if (resultPrecision - ((long) i) < ((long) mcPrecision)) {
                }
                resultPrecision -= (long) i;
                newScale -= (long) i;
                if (i < lastPow) {
                    i += ROUND_DOWN;
                }
                strippedBI = quotAndRem[ROUND_UP];
            }
            if (i == ROUND_DOWN) {
                break;
            }
            i = ROUND_DOWN;
        }
        if (resultPrecision > ((long) mcPrecision)) {
            throw new ArithmeticException("Division impossible");
        }
        integralValue.scale = safeLongToInt(newScale);
        integralValue.setUnscaledValue(strippedBI);
        return integralValue;
    }

    public BigDecimal remainder(BigDecimal divisor) {
        return divideAndRemainder(divisor)[ROUND_DOWN];
    }

    public BigDecimal remainder(BigDecimal divisor, MathContext mc) {
        return divideAndRemainder(divisor, mc)[ROUND_DOWN];
    }

    public BigDecimal[] divideAndRemainder(BigDecimal divisor) {
        BigDecimal[] quotAndRem = new BigDecimal[ROUND_CEILING];
        quotAndRem[ROUND_UP] = divideToIntegralValue(divisor);
        quotAndRem[ROUND_DOWN] = subtract(quotAndRem[ROUND_UP].multiply(divisor));
        return quotAndRem;
    }

    public BigDecimal[] divideAndRemainder(BigDecimal divisor, MathContext mc) {
        BigDecimal[] quotAndRem = new BigDecimal[ROUND_CEILING];
        quotAndRem[ROUND_UP] = divideToIntegralValue(divisor, mc);
        quotAndRem[ROUND_DOWN] = subtract(quotAndRem[ROUND_UP].multiply(divisor));
        return quotAndRem;
    }

    public BigDecimal pow(int n) {
        if (n == 0) {
            return ONE;
        }
        if (n < 0 || n > 999999999) {
            throw new ArithmeticException("Invalid operation");
        }
        BigDecimal zeroScaledBy;
        long newScale = ((long) this.scale) * ((long) n);
        if (isZero()) {
            zeroScaledBy = zeroScaledBy(newScale);
        } else {
            zeroScaledBy = new BigDecimal(getUnscaledValue().pow(n), safeLongToInt(newScale));
        }
        return zeroScaledBy;
    }

    public BigDecimal pow(int n, MathContext mc) {
        int m = Math.abs(n);
        int mcPrecision = mc.getPrecision();
        int elength = ((int) Math.log10((double) m)) + ROUND_DOWN;
        MathContext newPrecision = mc;
        if (n == 0 || (isZero() && n > 0)) {
            return pow(n);
        }
        if (m > 999999999 || ((mcPrecision == 0 && n < 0) || (mcPrecision > 0 && elength > mcPrecision))) {
            throw new ArithmeticException("Invalid operation");
        }
        if (mcPrecision > 0) {
            newPrecision = new MathContext((mcPrecision + elength) + ROUND_DOWN, mc.getRoundingMode());
        }
        BigDecimal accum = round(newPrecision);
        for (int oneBitMask = Integer.highestOneBit(m) >> ROUND_DOWN; oneBitMask > 0; oneBitMask >>= ROUND_DOWN) {
            accum = accum.multiply(accum, newPrecision);
            if ((m & oneBitMask) == oneBitMask) {
                accum = accum.multiply(this, newPrecision);
            }
        }
        if (n < 0) {
            accum = ONE.divide(accum, newPrecision);
        }
        accum.inplaceRound(mc);
        return accum;
    }

    public BigDecimal abs() {
        return signum() < 0 ? negate() : this;
    }

    public BigDecimal abs(MathContext mc) {
        BigDecimal result = signum() < 0 ? negate() : new BigDecimal(getUnscaledValue(), this.scale);
        result.inplaceRound(mc);
        return result;
    }

    public BigDecimal negate() {
        if (this.bitLength < 63 || (this.bitLength == 63 && this.smallValue != Long.MIN_VALUE)) {
            return valueOf(-this.smallValue, this.scale);
        }
        return new BigDecimal(getUnscaledValue().negate(), this.scale);
    }

    public BigDecimal negate(MathContext mc) {
        BigDecimal result = negate();
        result.inplaceRound(mc);
        return result;
    }

    public BigDecimal plus() {
        return this;
    }

    public BigDecimal plus(MathContext mc) {
        return round(mc);
    }

    public int signum() {
        if (this.bitLength < 64) {
            return Long.signum(this.smallValue);
        }
        return getUnscaledValue().signum();
    }

    private boolean isZero() {
        return this.bitLength == 0 && this.smallValue != -1;
    }

    public int scale() {
        return this.scale;
    }

    public int precision() {
        if (this.precision != 0) {
            return this.precision;
        }
        if (this.bitLength == 0) {
            this.precision = ROUND_DOWN;
        } else if (this.bitLength < 64) {
            this.precision = decimalDigitsInLong(this.smallValue);
        } else {
            int decimalDigits = ((int) (((double) (this.bitLength - 1)) * LOG10_2)) + ROUND_DOWN;
            if (getUnscaledValue().divide(Multiplication.powerOf10((long) decimalDigits)).signum() != 0) {
                decimalDigits += ROUND_DOWN;
            }
            this.precision = decimalDigits;
        }
        return this.precision;
    }

    private int decimalDigitsInLong(long value) {
        if (value == Long.MIN_VALUE) {
            return 19;
        }
        int index = Arrays.binarySearch(MathUtils.LONG_POWERS_OF_TEN, Math.abs(value));
        return index < 0 ? (-index) - 1 : index + ROUND_DOWN;
    }

    public BigInteger unscaledValue() {
        return getUnscaledValue();
    }

    public BigDecimal round(MathContext mc) {
        BigDecimal thisBD = new BigDecimal(getUnscaledValue(), this.scale);
        thisBD.inplaceRound(mc);
        return thisBD;
    }

    public BigDecimal setScale(int newScale, RoundingMode roundingMode) {
        if (roundingMode == null) {
            throw new NullPointerException("roundingMode == null");
        }
        long diffScale = ((long) newScale) - ((long) this.scale);
        if (diffScale == 0) {
            return this;
        }
        if (diffScale > 0) {
            if (diffScale >= ((long) MathUtils.LONG_POWERS_OF_TEN.length) || this.bitLength + LONG_POWERS_OF_TEN_BIT_LENGTH[(int) diffScale] >= 64) {
                return new BigDecimal(Multiplication.multiplyByTenPow(getUnscaledValue(), (long) ((int) diffScale)), newScale);
            }
            return valueOf(this.smallValue * MathUtils.LONG_POWERS_OF_TEN[(int) diffScale], newScale);
        } else if (this.bitLength >= 64 || (-diffScale) >= ((long) MathUtils.LONG_POWERS_OF_TEN.length)) {
            return divideBigIntegers(getUnscaledValue(), Multiplication.powerOf10(-diffScale), newScale, roundingMode);
        } else {
            return dividePrimitiveLongs(this.smallValue, MathUtils.LONG_POWERS_OF_TEN[(int) (-diffScale)], newScale, roundingMode);
        }
    }

    public BigDecimal setScale(int newScale, int roundingMode) {
        return setScale(newScale, RoundingMode.valueOf(roundingMode));
    }

    public BigDecimal setScale(int newScale) {
        return setScale(newScale, RoundingMode.UNNECESSARY);
    }

    public BigDecimal movePointLeft(int n) {
        return movePoint(((long) this.scale) + ((long) n));
    }

    private BigDecimal movePoint(long newScale) {
        if (isZero()) {
            return zeroScaledBy(Math.max(newScale, 0));
        }
        if (newScale >= 0) {
            if (this.bitLength < 64) {
                return valueOf(this.smallValue, safeLongToInt(newScale));
            }
            return new BigDecimal(getUnscaledValue(), safeLongToInt(newScale));
        } else if ((-newScale) >= ((long) MathUtils.LONG_POWERS_OF_TEN.length) || this.bitLength + LONG_POWERS_OF_TEN_BIT_LENGTH[(int) (-newScale)] >= 64) {
            return new BigDecimal(Multiplication.multiplyByTenPow(getUnscaledValue(), (long) safeLongToInt(-newScale)), (int) ROUND_UP);
        } else {
            return valueOf(this.smallValue * MathUtils.LONG_POWERS_OF_TEN[(int) (-newScale)], ROUND_UP);
        }
    }

    public BigDecimal movePointRight(int n) {
        return movePoint(((long) this.scale) - ((long) n));
    }

    public BigDecimal scaleByPowerOfTen(int n) {
        long newScale = ((long) this.scale) - ((long) n);
        if (this.bitLength >= 64) {
            return new BigDecimal(getUnscaledValue(), safeLongToInt(newScale));
        }
        if (this.smallValue == 0) {
            return zeroScaledBy(newScale);
        }
        return valueOf(this.smallValue, safeLongToInt(newScale));
    }

    public BigDecimal stripTrailingZeros() {
        int i = ROUND_DOWN;
        int lastPow = TEN_POW.length - 1;
        long newScale = (long) this.scale;
        if (isZero()) {
            return this;
        }
        BigInteger strippedBI = getUnscaledValue();
        while (!strippedBI.testBit(ROUND_UP)) {
            BigInteger[] quotAndRem = strippedBI.divideAndRemainder(TEN_POW[i]);
            if (quotAndRem[ROUND_DOWN].signum() == 0) {
                newScale -= (long) i;
                if (i < lastPow) {
                    i += ROUND_DOWN;
                }
                strippedBI = quotAndRem[ROUND_UP];
            } else if (i == ROUND_DOWN) {
                break;
            } else {
                i = ROUND_DOWN;
            }
        }
        return new BigDecimal(strippedBI, safeLongToInt(newScale));
    }

    public int compareTo(BigDecimal val) {
        int thisSign = signum();
        int valueSign = val.signum();
        if (thisSign == valueSign) {
            if (this.scale != val.scale || this.bitLength >= 64 || val.bitLength >= 64) {
                long diffScale = ((long) this.scale) - ((long) val.scale);
                int diffPrecision = approxPrecision() - val.approxPrecision();
                if (((long) diffPrecision) > 1 + diffScale) {
                    return thisSign;
                }
                if (((long) diffPrecision) < diffScale - 1) {
                    return -thisSign;
                }
                BigInteger thisUnscaled = getUnscaledValue();
                BigInteger valUnscaled = val.getUnscaledValue();
                if (diffScale < 0) {
                    thisUnscaled = thisUnscaled.multiply(Multiplication.powerOf10(-diffScale));
                } else if (diffScale > 0) {
                    valUnscaled = valUnscaled.multiply(Multiplication.powerOf10(diffScale));
                }
                return thisUnscaled.compareTo(valUnscaled);
            }
            int i = this.smallValue < val.smallValue ? -1 : this.smallValue > val.smallValue ? ROUND_DOWN : ROUND_UP;
            return i;
        } else if (thisSign < valueSign) {
            return -1;
        } else {
            return ROUND_DOWN;
        }
    }

    public boolean equals(Object x) {
        boolean z = true;
        if (this == x) {
            return true;
        }
        if (!(x instanceof BigDecimal)) {
            return false;
        }
        BigDecimal x1 = (BigDecimal) x;
        if (x1.scale != this.scale || x1.bitLength != this.bitLength) {
            z = false;
        } else if (this.bitLength >= 64) {
            z = x1.intVal.equals(this.intVal);
        } else if (x1.smallValue != this.smallValue) {
            z = false;
        }
        return z;
    }

    public BigDecimal min(BigDecimal val) {
        return compareTo(val) <= 0 ? this : val;
    }

    public BigDecimal max(BigDecimal val) {
        return compareTo(val) >= 0 ? this : val;
    }

    public int hashCode() {
        if (this.hashCode != 0) {
            return this.hashCode;
        }
        if (this.bitLength < 64) {
            this.hashCode = (int) (this.smallValue & -1);
            this.hashCode = (this.hashCode * 33) + ((int) ((this.smallValue >> 32) & -1));
            this.hashCode = (this.hashCode * 17) + this.scale;
            return this.hashCode;
        }
        this.hashCode = (this.intVal.hashCode() * 17) + this.scale;
        return this.hashCode;
    }

    public String toString() {
        if (this.toStringImage != null) {
            return this.toStringImage;
        }
        if (this.bitLength < 32) {
            this.toStringImage = Conversion.toDecimalScaledString(this.smallValue, this.scale);
            return this.toStringImage;
        }
        String intString = getUnscaledValue().toString();
        if (this.scale == 0) {
            return intString;
        }
        int begin = getUnscaledValue().signum() < 0 ? ROUND_CEILING : ROUND_DOWN;
        int end = intString.length();
        long exponent = ((-((long) this.scale)) + ((long) end)) - ((long) begin);
        StringBuilder result = new StringBuilder();
        result.append(intString);
        if (this.scale <= 0 || exponent < -6) {
            if (end - begin >= ROUND_DOWN) {
                result.insert(begin, '.');
                end += ROUND_DOWN;
            }
            result.insert(end, 'E');
            if (exponent > 0) {
                end += ROUND_DOWN;
                result.insert(end, '+');
            }
            result.insert(end + ROUND_DOWN, Long.toString(exponent));
        } else if (exponent >= 0) {
            result.insert(end - this.scale, '.');
        } else {
            result.insert(begin - 1, "0.");
            result.insert(begin + ROUND_DOWN, CH_ZEROS, ROUND_UP, (-((int) exponent)) - 1);
        }
        this.toStringImage = result.toString();
        return this.toStringImage;
    }

    public String toEngineeringString() {
        String intString = getUnscaledValue().toString();
        if (this.scale == 0) {
            return intString;
        }
        int begin = getUnscaledValue().signum() < 0 ? ROUND_CEILING : ROUND_DOWN;
        int end = intString.length();
        long exponent = ((-((long) this.scale)) + ((long) end)) - ((long) begin);
        StringBuilder result = new StringBuilder(intString);
        if (this.scale <= 0 || exponent < -6) {
            int delta = end - begin;
            int rem = (int) (exponent % 3);
            if (rem != 0) {
                if (getUnscaledValue().signum() == 0) {
                    rem = rem < 0 ? -rem : 3 - rem;
                    exponent += (long) rem;
                } else {
                    if (rem < 0) {
                        rem += ROUND_FLOOR;
                    }
                    exponent -= (long) rem;
                    begin += rem;
                }
                if (delta < ROUND_FLOOR) {
                    int i = rem - delta;
                    int end2 = end;
                    while (i > 0) {
                        end = end2 + ROUND_DOWN;
                        result.insert(end2, '0');
                        i--;
                        end2 = end;
                    }
                    end = end2;
                }
            }
            if (end - begin >= ROUND_DOWN) {
                result.insert(begin, '.');
                end += ROUND_DOWN;
            }
            if (exponent != 0) {
                result.insert(end, 'E');
                if (exponent > 0) {
                    end += ROUND_DOWN;
                    result.insert(end, '+');
                }
                result.insert(end + ROUND_DOWN, Long.toString(exponent));
            }
        } else if (exponent >= 0) {
            result.insert(end - this.scale, '.');
        } else {
            result.insert(begin - 1, "0.");
            result.insert(begin + ROUND_DOWN, CH_ZEROS, ROUND_UP, (-((int) exponent)) - 1);
        }
        return result.toString();
    }

    public String toPlainString() {
        String intStr = getUnscaledValue().toString();
        if (this.scale == 0 || (isZero() && this.scale < 0)) {
            return intStr;
        }
        int begin = signum() < 0 ? ROUND_DOWN : ROUND_UP;
        int delta = this.scale;
        StringBuilder result = new StringBuilder((intStr.length() + ROUND_DOWN) + Math.abs(this.scale));
        if (begin == ROUND_DOWN) {
            result.append('-');
        }
        if (this.scale > 0) {
            delta -= intStr.length() - begin;
            if (delta >= 0) {
                result.append("0.");
                while (delta > CH_ZEROS.length) {
                    result.append(CH_ZEROS);
                    delta -= CH_ZEROS.length;
                }
                result.append(CH_ZEROS, ROUND_UP, delta);
                result.append(intStr.substring(begin));
            } else {
                delta = begin - delta;
                result.append(intStr.substring(begin, delta));
                result.append('.');
                result.append(intStr.substring(delta));
            }
        } else {
            result.append(intStr.substring(begin));
            while (delta < (-CH_ZEROS.length)) {
                result.append(CH_ZEROS);
                delta += CH_ZEROS.length;
            }
            result.append(CH_ZEROS, ROUND_UP, -delta);
        }
        return result.toString();
    }

    public BigInteger toBigInteger() {
        if (this.scale == 0 || isZero()) {
            return getUnscaledValue();
        }
        if (this.scale < 0) {
            return getUnscaledValue().multiply(Multiplication.powerOf10(-((long) this.scale)));
        }
        return getUnscaledValue().divide(Multiplication.powerOf10((long) this.scale));
    }

    public BigInteger toBigIntegerExact() {
        if (this.scale == 0 || isZero()) {
            return getUnscaledValue();
        }
        if (this.scale < 0) {
            return getUnscaledValue().multiply(Multiplication.powerOf10(-((long) this.scale)));
        }
        if (this.scale > approxPrecision() || this.scale > getUnscaledValue().getLowestSetBit()) {
            throw new ArithmeticException("Rounding necessary");
        }
        BigInteger[] integerAndFraction = getUnscaledValue().divideAndRemainder(Multiplication.powerOf10((long) this.scale));
        if (integerAndFraction[ROUND_DOWN].signum() == 0) {
            return integerAndFraction[ROUND_UP];
        }
        throw new ArithmeticException("Rounding necessary");
    }

    public long longValue() {
        return (this.scale <= -64 || this.scale > approxPrecision()) ? 0 : toBigInteger().longValue();
    }

    public long longValueExact() {
        return valueExact(64);
    }

    public int intValue() {
        return (this.scale <= -32 || this.scale > approxPrecision()) ? ROUND_UP : toBigInteger().intValue();
    }

    public int intValueExact() {
        return (int) valueExact(32);
    }

    public short shortValueExact() {
        return (short) ((int) valueExact(16));
    }

    public byte byteValueExact() {
        return (byte) ((int) valueExact(8));
    }

    public float floatValue() {
        float floatResult = (float) signum();
        long powerOfTwo = ((long) this.bitLength) - ((long) (((double) this.scale) / LOG10_2));
        if (powerOfTwo < -149 || floatResult == 0.0f) {
            return floatResult * 0.0f;
        }
        if (powerOfTwo > 129) {
            return floatResult * Float.POSITIVE_INFINITY;
        }
        return (float) doubleValue();
    }

    public double doubleValue() {
        int sign = signum();
        int exponent = 1076;
        long powerOfTwo = ((long) this.bitLength) - ((long) (((double) this.scale) / LOG10_2));
        if (powerOfTwo < -1074 || sign == 0) {
            return ((double) sign) * 0.0d;
        } else if (powerOfTwo > 1025) {
            return ((double) sign) * Double.POSITIVE_INFINITY;
        } else {
            long bits;
            long tempBits;
            BigInteger mantissa = getUnscaledValue().abs();
            if (this.scale <= 0) {
                mantissa = mantissa.multiply(Multiplication.powerOf10((long) (-this.scale)));
            } else {
                BigInteger powerOfTen = Multiplication.powerOf10((long) this.scale);
                int k = 100 - ((int) powerOfTwo);
                if (k > 0) {
                    mantissa = mantissa.shiftLeft(k);
                    exponent = 1076 - k;
                }
                BigInteger[] quotAndRem = mantissa.divideAndRemainder(powerOfTen);
                int compRem = quotAndRem[ROUND_DOWN].shiftLeftOneBit().compareTo(powerOfTen);
                mantissa = quotAndRem[ROUND_UP].shiftLeft(ROUND_CEILING).add(BigInteger.valueOf((long) ((((compRem + ROUND_FLOOR) * compRem) / ROUND_CEILING) + ROUND_DOWN)));
                exponent -= 2;
            }
            int lowestSetBit = mantissa.getLowestSetBit();
            int discardedSize = mantissa.bitLength() - 54;
            if (discardedSize > 0) {
                bits = mantissa.shiftRight(discardedSize).longValue();
                tempBits = bits;
                if ((1 & bits) != 1 || lowestSetBit >= discardedSize) {
                    if ((3 & bits) == 3) {
                    }
                }
                bits += 2;
            } else {
                bits = mantissa.longValue() << (-discardedSize);
                tempBits = bits;
                if ((3 & bits) == 3) {
                    bits += 2;
                }
            }
            if ((18014398509481984L & bits) == 0) {
                bits >>= ROUND_DOWN;
                exponent += discardedSize;
            } else {
                bits >>= ROUND_CEILING;
                exponent += discardedSize + ROUND_DOWN;
            }
            if (exponent > 2046) {
                return ((double) sign) * Double.POSITIVE_INFINITY;
            }
            if (exponent <= 0) {
                if (exponent < -53) {
                    return ((double) sign) * 0.0d;
                }
                bits = tempBits >> ROUND_DOWN;
                tempBits = bits & (-1 >>> (exponent + 63));
                bits >>= -exponent;
                if ((3 & bits) == 3 || ((1 & bits) == 1 && tempBits != 0 && lowestSetBit < discardedSize)) {
                    bits++;
                }
                exponent = ROUND_UP;
                bits >>= ROUND_DOWN;
            }
            return Double.longBitsToDouble(((((long) sign) & Long.MIN_VALUE) | (((long) exponent) << 52)) | (4503599627370495L & bits));
        }
    }

    public BigDecimal ulp() {
        return valueOf(1, this.scale);
    }

    private void inplaceRound(MathContext mc) {
        int mcPrecision = mc.getPrecision();
        if (approxPrecision() >= mcPrecision && mcPrecision != 0) {
            int discardedPrecision = precision() - mcPrecision;
            if (discardedPrecision > 0) {
                if (this.bitLength < 64) {
                    smallRound(mc, discardedPrecision);
                    return;
                }
                BigInteger sizeOfFraction = Multiplication.powerOf10((long) discardedPrecision);
                BigInteger[] integerAndFraction = getUnscaledValue().divideAndRemainder(sizeOfFraction);
                long newScale = ((long) this.scale) - ((long) discardedPrecision);
                if (integerAndFraction[ROUND_DOWN].signum() != 0) {
                    int compRem = roundingBehavior(integerAndFraction[ROUND_UP].testBit(ROUND_UP) ? ROUND_DOWN : ROUND_UP, integerAndFraction[ROUND_DOWN].signum() * (integerAndFraction[ROUND_DOWN].abs().shiftLeftOneBit().compareTo(sizeOfFraction) + ROUND_HALF_DOWN), mc.getRoundingMode());
                    if (compRem != 0) {
                        integerAndFraction[ROUND_UP] = integerAndFraction[ROUND_UP].add(BigInteger.valueOf((long) compRem));
                    }
                    if (new BigDecimal(integerAndFraction[ROUND_UP]).precision() > mcPrecision) {
                        integerAndFraction[ROUND_UP] = integerAndFraction[ROUND_UP].divide(BigInteger.TEN);
                        newScale--;
                    }
                }
                this.scale = safeLongToInt(newScale);
                this.precision = mcPrecision;
                setUnscaledValue(integerAndFraction[ROUND_UP]);
            }
        }
    }

    private static int longCompareTo(long value1, long value2) {
        if (value1 > value2) {
            return ROUND_DOWN;
        }
        return value1 < value2 ? -1 : ROUND_UP;
    }

    private void smallRound(MathContext mc, int discardedPrecision) {
        long sizeOfFraction = MathUtils.LONG_POWERS_OF_TEN[discardedPrecision];
        long newScale = ((long) this.scale) - ((long) discardedPrecision);
        long unscaledVal = this.smallValue;
        long integer = unscaledVal / sizeOfFraction;
        long fraction = unscaledVal % sizeOfFraction;
        if (fraction != 0) {
            integer += (long) roundingBehavior(((int) integer) & ROUND_DOWN, Long.signum(fraction) * (longCompareTo(Math.abs(fraction) * 2, sizeOfFraction) + ROUND_HALF_DOWN), mc.getRoundingMode());
            if (Math.log10((double) Math.abs(integer)) >= ((double) mc.getPrecision())) {
                integer /= 10;
                newScale--;
            }
        }
        this.scale = safeLongToInt(newScale);
        this.precision = mc.getPrecision();
        this.smallValue = integer;
        this.bitLength = bitLength(integer);
        this.intVal = null;
    }

    private static int roundingBehavior(int parityBit, int fraction, RoundingMode roundingMode) {
        switch (-getjava-math-RoundingModeSwitchesValues()[roundingMode.ordinal()]) {
            case ROUND_DOWN /*1*/:
                return Math.max(Integer.signum(fraction), ROUND_UP);
            case ROUND_FLOOR /*3*/:
                return Math.min(Integer.signum(fraction), ROUND_UP);
            case ROUND_HALF_UP /*4*/:
                if (Math.abs(fraction) > ROUND_HALF_DOWN) {
                    return Integer.signum(fraction);
                }
                return ROUND_UP;
            case ROUND_HALF_DOWN /*5*/:
                if (Math.abs(fraction) + parityBit > ROUND_HALF_DOWN) {
                    return Integer.signum(fraction);
                }
                return ROUND_UP;
            case ROUND_HALF_EVEN /*6*/:
                if (Math.abs(fraction) >= ROUND_HALF_DOWN) {
                    return Integer.signum(fraction);
                }
                return ROUND_UP;
            case ROUND_UNNECESSARY /*7*/:
                if (fraction == 0) {
                    return ROUND_UP;
                }
                throw new ArithmeticException("Rounding necessary");
            case NodeFilter.SHOW_CDATA_SECTION /*8*/:
                return Integer.signum(fraction);
            default:
                return ROUND_UP;
        }
    }

    private long valueExact(int bitLengthOfType) {
        BigInteger bigInteger = toBigIntegerExact();
        if (bigInteger.bitLength() < bitLengthOfType) {
            return bigInteger.longValue();
        }
        throw new ArithmeticException("Rounding necessary");
    }

    private int approxPrecision() {
        if (this.precision > 0) {
            return this.precision;
        }
        return ((int) (((double) (this.bitLength - 1)) * LOG10_2)) + ROUND_DOWN;
    }

    private static int safeLongToInt(long longValue) {
        if (longValue >= -2147483648L && longValue <= 2147483647L) {
            return (int) longValue;
        }
        throw new ArithmeticException("Out of int range: " + longValue);
    }

    private static BigDecimal zeroScaledBy(long longScale) {
        if (longScale == ((long) ((int) longScale))) {
            return valueOf(0, (int) longScale);
        }
        if (longScale >= 0) {
            return new BigDecimal((int) ROUND_UP, (int) AnnualTimeZoneRule.MAX_YEAR);
        }
        return new BigDecimal((int) ROUND_UP, (int) DatatypeConstants.FIELD_UNDEFINED);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.bitLength = this.intVal.bitLength();
        if (this.bitLength < 64) {
            this.smallValue = this.intVal.longValue();
        }
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        getUnscaledValue();
        out.defaultWriteObject();
    }

    private BigInteger getUnscaledValue() {
        if (this.intVal == null) {
            this.intVal = BigInteger.valueOf(this.smallValue);
        }
        return this.intVal;
    }

    private void setUnscaledValue(BigInteger unscaledValue) {
        this.intVal = unscaledValue;
        this.bitLength = unscaledValue.bitLength();
        if (this.bitLength < 64) {
            this.smallValue = unscaledValue.longValue();
        }
    }
}
