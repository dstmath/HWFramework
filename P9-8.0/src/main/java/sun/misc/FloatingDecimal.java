package sun.misc;

import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import sun.util.locale.LanguageTag;

public class FloatingDecimal {
    static final /* synthetic */ boolean -assertionsDisabled = (FloatingDecimal.class.desiredAssertionStatus() ^ 1);
    static final ASCIIToBinaryConverter A2BC_NEGATIVE_INFINITY = new PreparedASCIIToBinaryBuffer(Double.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);
    static final ASCIIToBinaryConverter A2BC_NEGATIVE_ZERO = new PreparedASCIIToBinaryBuffer(-0.0d, -0.0f);
    static final ASCIIToBinaryConverter A2BC_NOT_A_NUMBER = new PreparedASCIIToBinaryBuffer(Double.NaN, Float.NaN);
    static final ASCIIToBinaryConverter A2BC_POSITIVE_INFINITY = new PreparedASCIIToBinaryBuffer(Double.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
    static final ASCIIToBinaryConverter A2BC_POSITIVE_ZERO = new PreparedASCIIToBinaryBuffer(0.0d, 0.0f);
    private static final BinaryToASCIIConverter B2AC_NEGATIVE_INFINITY = new ExceptionalBinaryToASCIIBuffer("-Infinity", true);
    private static final BinaryToASCIIConverter B2AC_NEGATIVE_ZERO = new BinaryToASCIIBuffer(true, new char[]{'0'});
    private static final BinaryToASCIIConverter B2AC_NOT_A_NUMBER = new ExceptionalBinaryToASCIIBuffer(NAN_REP, -assertionsDisabled);
    private static final BinaryToASCIIConverter B2AC_POSITIVE_INFINITY = new ExceptionalBinaryToASCIIBuffer(INFINITY_REP, -assertionsDisabled);
    private static final BinaryToASCIIConverter B2AC_POSITIVE_ZERO = new BinaryToASCIIBuffer(-assertionsDisabled, new char[]{'0'});
    static final int BIG_DECIMAL_EXPONENT = 324;
    static final long EXP_ONE = 4607182418800017408L;
    static final int EXP_SHIFT = 52;
    static final long FRACT_HOB = 4503599627370496L;
    private static final int INFINITY_LENGTH = INFINITY_REP.length();
    private static final String INFINITY_REP = "Infinity";
    static final int INT_DECIMAL_DIGITS = 9;
    static final int MAX_DECIMAL_DIGITS = 15;
    static final int MAX_DECIMAL_EXPONENT = 308;
    static final int MAX_NDIGITS = 1100;
    static final int MAX_SMALL_BIN_EXP = 62;
    static final int MIN_DECIMAL_EXPONENT = -324;
    static final int MIN_SMALL_BIN_EXP = -21;
    private static final int NAN_LENGTH = NAN_REP.length();
    private static final String NAN_REP = "NaN";
    static final int SINGLE_EXP_SHIFT = 23;
    static final int SINGLE_FRACT_HOB = 8388608;
    static final int SINGLE_MAX_DECIMAL_DIGITS = 7;
    static final int SINGLE_MAX_DECIMAL_EXPONENT = 38;
    static final int SINGLE_MAX_NDIGITS = 200;
    static final int SINGLE_MIN_DECIMAL_EXPONENT = -45;
    private static final ThreadLocal<BinaryToASCIIBuffer> threadLocalBinaryToASCIIBuffer = new ThreadLocal<BinaryToASCIIBuffer>() {
        protected BinaryToASCIIBuffer initialValue() {
            return new BinaryToASCIIBuffer();
        }
    };

    interface ASCIIToBinaryConverter {
        double doubleValue();

        float floatValue();
    }

    static class ASCIIToBinaryBuffer implements ASCIIToBinaryConverter {
        static final /* synthetic */ boolean -assertionsDisabled = (ASCIIToBinaryBuffer.class.desiredAssertionStatus() ^ 1);
        private static final double[] BIG_10_POW = new double[]{1.0E16d, 1.0E32d, 1.0E64d, 1.0E128d, 1.0E256d};
        private static final int MAX_SMALL_TEN = (SMALL_10_POW.length - 1);
        private static final int SINGLE_MAX_SMALL_TEN = (SINGLE_SMALL_10_POW.length - 1);
        private static final float[] SINGLE_SMALL_10_POW = new float[]{1.0f, 10.0f, 100.0f, 1000.0f, 10000.0f, 100000.0f, 1000000.0f, 1.0E7f, 1.0E8f, 1.0E9f, 1.0E10f};
        private static final double[] SMALL_10_POW = new double[]{1.0d, 10.0d, 100.0d, 1000.0d, 10000.0d, 100000.0d, 1000000.0d, 1.0E7d, 1.0E8d, 1.0E9d, 1.0E10d, 1.0E11d, 1.0E12d, 1.0E13d, 1.0E14d, 1.0E15d, 1.0E16d, 1.0E17d, 1.0E18d, 1.0E19d, 1.0E20d, 1.0E21d, 1.0E22d};
        private static final double[] TINY_10_POW = new double[]{1.0E-16d, 1.0E-32d, 1.0E-64d, 1.0E-128d, 1.0E-256d};
        int decExponent;
        char[] digits;
        boolean isNegative;
        int nDigits;

        ASCIIToBinaryBuffer(boolean negSign, int decExponent, char[] digits, int n) {
            this.isNegative = negSign;
            this.decExponent = decExponent;
            this.digits = digits;
            this.nDigits = n;
        }

        public double doubleValue() {
            int i;
            int kDigits = Math.min(this.nDigits, 16);
            int iValue = this.digits[0] - 48;
            int iDigits = Math.min(kDigits, 9);
            for (i = 1; i < iDigits; i++) {
                iValue = ((iValue * 10) + this.digits[i]) - 48;
            }
            long lValue = (long) iValue;
            for (i = iDigits; i < kDigits; i++) {
                lValue = (10 * lValue) + ((long) (this.digits[i] - 48));
            }
            double dValue = (double) lValue;
            int exp = this.decExponent - kDigits;
            if (this.nDigits <= 15) {
                double rValue;
                if (exp == 0 || dValue == 0.0d) {
                    if (this.isNegative) {
                        dValue = -dValue;
                    }
                    return dValue;
                } else if (exp >= 0) {
                    if (exp <= MAX_SMALL_TEN) {
                        rValue = dValue * SMALL_10_POW[exp];
                        if (this.isNegative) {
                            rValue = -rValue;
                        }
                        return rValue;
                    }
                    int slop = 15 - kDigits;
                    if (exp <= MAX_SMALL_TEN + slop) {
                        rValue = (dValue * SMALL_10_POW[slop]) * SMALL_10_POW[exp - slop];
                        if (this.isNegative) {
                            rValue = -rValue;
                        }
                        return rValue;
                    }
                } else if (exp >= (-MAX_SMALL_TEN)) {
                    rValue = dValue / SMALL_10_POW[-exp];
                    if (this.isNegative) {
                        rValue = -rValue;
                    }
                    return rValue;
                }
            }
            int j;
            double t;
            if (exp > 0) {
                if (this.decExponent > 309) {
                    return this.isNegative ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
                }
                if ((exp & 15) != 0) {
                    dValue *= SMALL_10_POW[exp & 15];
                }
                exp >>= 4;
                if (exp != 0) {
                    j = 0;
                    while (exp > 1) {
                        if ((exp & 1) != 0) {
                            dValue *= BIG_10_POW[j];
                        }
                        j++;
                        exp >>= 1;
                    }
                    t = dValue * BIG_10_POW[j];
                    if (Double.isInfinite(t)) {
                        if (Double.isInfinite((dValue / 2.0d) * BIG_10_POW[j])) {
                            return this.isNegative ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
                        }
                        t = Double.MAX_VALUE;
                    }
                    dValue = t;
                }
            } else if (exp < 0) {
                exp = -exp;
                if (this.decExponent < -325) {
                    return this.isNegative ? -0.0d : 0.0d;
                }
                if ((exp & 15) != 0) {
                    dValue /= SMALL_10_POW[exp & 15];
                }
                exp >>= 4;
                if (exp != 0) {
                    j = 0;
                    while (exp > 1) {
                        if ((exp & 1) != 0) {
                            dValue *= TINY_10_POW[j];
                        }
                        j++;
                        exp >>= 1;
                    }
                    t = dValue * TINY_10_POW[j];
                    if (t == 0.0d) {
                        if ((dValue * 2.0d) * TINY_10_POW[j] == 0.0d) {
                            return this.isNegative ? -0.0d : 0.0d;
                        }
                        t = Double.MIN_VALUE;
                    }
                    dValue = t;
                }
            }
            if (this.nDigits > FloatingDecimal.MAX_NDIGITS) {
                this.nDigits = 1101;
                this.digits[FloatingDecimal.MAX_NDIGITS] = '1';
            }
            FDBigInteger bigD0 = new FDBigInteger(lValue, this.digits, kDigits, this.nDigits);
            exp = this.decExponent - this.nDigits;
            long ieeeBits = Double.doubleToRawLongBits(dValue);
            int B5 = Math.max(0, -exp);
            int D5 = Math.max(0, exp);
            bigD0 = bigD0.multByPow52(D5, 0);
            bigD0.makeImmutable();
            FDBigInteger bigD = null;
            int prevD2 = 0;
            do {
                int hulpbias;
                boolean overvalue;
                FDBigInteger diff;
                int binexp = (int) (ieeeBits >>> FloatingDecimal.EXP_SHIFT);
                long bigBbits = ieeeBits & DoubleConsts.SIGNIF_BIT_MASK;
                if (binexp > 0) {
                    bigBbits |= FloatingDecimal.FRACT_HOB;
                } else if (-assertionsDisabled || bigBbits != 0) {
                    int shift = Long.numberOfLeadingZeros(bigBbits) - 11;
                    bigBbits <<= shift;
                    binexp = 1 - shift;
                } else {
                    throw new AssertionError(Long.valueOf(bigBbits));
                }
                binexp -= 1023;
                int lowOrderZeros = Long.numberOfTrailingZeros(bigBbits);
                bigBbits >>>= lowOrderZeros;
                int bigIntExp = (binexp - 52) + lowOrderZeros;
                int bigIntNBits = 53 - lowOrderZeros;
                int B2 = B5;
                int D2 = D5;
                if (bigIntExp >= 0) {
                    B2 = B5 + bigIntExp;
                } else {
                    D2 = D5 - bigIntExp;
                }
                int Ulp2 = B2;
                if (binexp <= -1023) {
                    hulpbias = (binexp + lowOrderZeros) + 1023;
                } else {
                    hulpbias = lowOrderZeros + 1;
                }
                B2 += hulpbias;
                D2 += hulpbias;
                int common2 = Math.min(B2, Math.min(D2, Ulp2));
                D2 -= common2;
                Ulp2 -= common2;
                FDBigInteger bigB = FDBigInteger.valueOfMulPow52(bigBbits, B5, B2 - common2);
                if (bigD == null || prevD2 != D2) {
                    bigD = bigD0.leftShift(D2);
                    prevD2 = D2;
                }
                int cmpResult = bigB.cmp(bigD);
                if (cmpResult <= 0) {
                    if (cmpResult >= 0) {
                        break;
                    }
                    overvalue = FloatingDecimal.-assertionsDisabled;
                    diff = bigD.rightInplaceSub(bigB);
                } else {
                    overvalue = true;
                    diff = bigB.leftInplaceSub(bigD);
                    if (bigIntNBits == 1 && bigIntExp > -1022) {
                        Ulp2--;
                        if (Ulp2 < 0) {
                            Ulp2 = 0;
                            diff = diff.leftShift(1);
                        }
                    }
                }
                cmpResult = diff.cmpPow52(B5, Ulp2);
                if (cmpResult >= 0) {
                    if (cmpResult != 0) {
                        ieeeBits += (long) (overvalue ? -1 : 1);
                        if (ieeeBits == 0) {
                            break;
                        }
                    } else if ((1 & ieeeBits) != 0) {
                        ieeeBits += (long) (overvalue ? -1 : 1);
                    }
                } else {
                    break;
                }
            } while (ieeeBits != DoubleConsts.EXP_BIT_MASK);
            if (this.isNegative) {
                ieeeBits |= Long.MIN_VALUE;
            }
            return Double.longBitsToDouble(ieeeBits);
        }

        public float floatValue() {
            int i;
            int kDigits = Math.min(this.nDigits, 8);
            int iValue = this.digits[0] - 48;
            for (i = 1; i < kDigits; i++) {
                iValue = ((iValue * 10) + this.digits[i]) - 48;
            }
            float fValue = (float) iValue;
            int exp = this.decExponent - kDigits;
            if (this.nDigits <= 7) {
                if (exp == 0 || fValue == 0.0f) {
                    if (this.isNegative) {
                        fValue = -fValue;
                    }
                    return fValue;
                } else if (exp >= 0) {
                    if (exp <= SINGLE_MAX_SMALL_TEN) {
                        fValue *= SINGLE_SMALL_10_POW[exp];
                        if (this.isNegative) {
                            fValue = -fValue;
                        }
                        return fValue;
                    }
                    int slop = 7 - kDigits;
                    if (exp <= SINGLE_MAX_SMALL_TEN + slop) {
                        fValue = (fValue * SINGLE_SMALL_10_POW[slop]) * SINGLE_SMALL_10_POW[exp - slop];
                        if (this.isNegative) {
                            fValue = -fValue;
                        }
                        return fValue;
                    }
                } else if (exp >= (-SINGLE_MAX_SMALL_TEN)) {
                    fValue /= SINGLE_SMALL_10_POW[-exp];
                    if (this.isNegative) {
                        fValue = -fValue;
                    }
                    return fValue;
                }
            } else if (this.decExponent >= this.nDigits && this.nDigits + this.decExponent <= 15) {
                long lValue = (long) iValue;
                for (i = kDigits; i < this.nDigits; i++) {
                    lValue = (10 * lValue) + ((long) (this.digits[i] - 48));
                }
                fValue = (float) (((double) lValue) * SMALL_10_POW[this.decExponent - this.nDigits]);
                if (this.isNegative) {
                    fValue = -fValue;
                }
                return fValue;
            }
            double dValue = (double) fValue;
            int j;
            if (exp > 0) {
                if (this.decExponent > 39) {
                    return this.isNegative ? Float.NEGATIVE_INFINITY : Float.POSITIVE_INFINITY;
                }
                if ((exp & 15) != 0) {
                    dValue *= SMALL_10_POW[exp & 15];
                }
                exp >>= 4;
                if (exp != 0) {
                    j = 0;
                    while (exp > 0) {
                        if ((exp & 1) != 0) {
                            dValue *= BIG_10_POW[j];
                        }
                        j++;
                        exp >>= 1;
                    }
                }
            } else if (exp < 0) {
                exp = -exp;
                if (this.decExponent < -46) {
                    return this.isNegative ? -0.0f : 0.0f;
                }
                if ((exp & 15) != 0) {
                    dValue /= SMALL_10_POW[exp & 15];
                }
                exp >>= 4;
                if (exp != 0) {
                    j = 0;
                    while (exp > 0) {
                        if ((exp & 1) != 0) {
                            dValue *= TINY_10_POW[j];
                        }
                        j++;
                        exp >>= 1;
                    }
                }
            }
            fValue = Math.max(Float.MIN_VALUE, Math.min(Float.MAX_VALUE, (float) dValue));
            if (this.nDigits > 200) {
                this.nDigits = HttpURLConnection.HTTP_CREATED;
                this.digits[200] = '1';
            }
            FDBigInteger bigD0 = new FDBigInteger((long) iValue, this.digits, kDigits, this.nDigits);
            exp = this.decExponent - this.nDigits;
            int ieeeBits = Float.floatToRawIntBits(fValue);
            int B5 = Math.max(0, -exp);
            int D5 = Math.max(0, exp);
            bigD0 = bigD0.multByPow52(D5, 0);
            bigD0.makeImmutable();
            FDBigInteger bigD = null;
            int prevD2 = 0;
            do {
                int hulpbias;
                boolean overvalue;
                FDBigInteger diff;
                int binexp = ieeeBits >>> 23;
                int bigBbits = ieeeBits & FloatConsts.SIGNIF_BIT_MASK;
                if (binexp > 0) {
                    bigBbits |= FloatingDecimal.SINGLE_FRACT_HOB;
                } else if (-assertionsDisabled || bigBbits != 0) {
                    int shift = Integer.numberOfLeadingZeros(bigBbits) - 8;
                    bigBbits <<= shift;
                    binexp = 1 - shift;
                } else {
                    throw new AssertionError(Integer.valueOf(bigBbits));
                }
                binexp -= 127;
                int lowOrderZeros = Integer.numberOfTrailingZeros(bigBbits);
                bigBbits >>>= lowOrderZeros;
                int bigIntExp = (binexp - 23) + lowOrderZeros;
                int bigIntNBits = 24 - lowOrderZeros;
                int B2 = B5;
                int D2 = D5;
                if (bigIntExp >= 0) {
                    B2 = B5 + bigIntExp;
                } else {
                    D2 = D5 - bigIntExp;
                }
                int Ulp2 = B2;
                if (binexp <= -127) {
                    hulpbias = (binexp + lowOrderZeros) + 127;
                } else {
                    hulpbias = lowOrderZeros + 1;
                }
                B2 += hulpbias;
                D2 += hulpbias;
                int common2 = Math.min(B2, Math.min(D2, Ulp2));
                D2 -= common2;
                Ulp2 -= common2;
                FDBigInteger bigB = FDBigInteger.valueOfMulPow52((long) bigBbits, B5, B2 - common2);
                if (bigD == null || prevD2 != D2) {
                    bigD = bigD0.leftShift(D2);
                    prevD2 = D2;
                }
                int cmpResult = bigB.cmp(bigD);
                if (cmpResult <= 0) {
                    if (cmpResult >= 0) {
                        break;
                    }
                    overvalue = FloatingDecimal.-assertionsDisabled;
                    diff = bigD.rightInplaceSub(bigB);
                } else {
                    overvalue = true;
                    diff = bigB.leftInplaceSub(bigD);
                    if (bigIntNBits == 1 && bigIntExp > -126) {
                        Ulp2--;
                        if (Ulp2 < 0) {
                            Ulp2 = 0;
                            diff = diff.leftShift(1);
                        }
                    }
                }
                cmpResult = diff.cmpPow52(B5, Ulp2);
                if (cmpResult >= 0) {
                    if (cmpResult != 0) {
                        ieeeBits += overvalue ? -1 : 1;
                        if (ieeeBits == 0) {
                            break;
                        }
                    } else if ((ieeeBits & 1) != 0) {
                        ieeeBits += overvalue ? -1 : 1;
                    }
                } else {
                    break;
                }
            } while (ieeeBits != 2139095040);
            if (this.isNegative) {
                ieeeBits |= Integer.MIN_VALUE;
            }
            return Float.intBitsToFloat(ieeeBits);
        }
    }

    public interface BinaryToASCIIConverter {
        void appendTo(Appendable appendable);

        boolean decimalDigitsExact();

        boolean digitsRoundedUp();

        int getDecimalExponent();

        int getDigits(char[] cArr);

        boolean isExceptional();

        boolean isNegative();

        String toJavaFormatString();
    }

    static class BinaryToASCIIBuffer implements BinaryToASCIIConverter {
        static final /* synthetic */ boolean -assertionsDisabled = (BinaryToASCIIBuffer.class.desiredAssertionStatus() ^ 1);
        private static final int[] N_5_BITS = new int[]{0, 3, 5, 7, 10, 12, 14, 17, 19, 21, 24, 26, 28, 31, 33, 35, 38, 40, 42, 45, 47, 49, FloatingDecimal.EXP_SHIFT, 54, 56, 59, 61};
        private static int[] insignificantDigitsNumber = new int[]{0, 0, 0, 0, 1, 1, 1, 2, 2, 2, 3, 3, 3, 3, 4, 4, 4, 5, 5, 5, 6, 6, 6, 6, 7, 7, 7, 8, 8, 8, 9, 9, 9, 9, 10, 10, 10, 11, 11, 11, 12, 12, 12, 12, 13, 13, 13, 14, 14, 14, 15, 15, 15, 15, 16, 16, 16, 17, 17, 17, 18, 18, 18, 19};
        private final char[] buffer;
        private int decExponent;
        private boolean decimalDigitsRoundedUp;
        private final char[] digits;
        private boolean exactDecimalConversion;
        private int firstDigitIndex;
        private boolean isNegative;
        private int nDigits;

        BinaryToASCIIBuffer() {
            this.buffer = new char[26];
            this.exactDecimalConversion = FloatingDecimal.-assertionsDisabled;
            this.decimalDigitsRoundedUp = FloatingDecimal.-assertionsDisabled;
            this.digits = new char[20];
        }

        BinaryToASCIIBuffer(boolean isNegative, char[] digits) {
            this.buffer = new char[26];
            this.exactDecimalConversion = FloatingDecimal.-assertionsDisabled;
            this.decimalDigitsRoundedUp = FloatingDecimal.-assertionsDisabled;
            this.isNegative = isNegative;
            this.decExponent = 0;
            this.digits = digits;
            this.firstDigitIndex = 0;
            this.nDigits = digits.length;
        }

        public String toJavaFormatString() {
            return new String(this.buffer, 0, getChars(this.buffer));
        }

        public void appendTo(Appendable buf) {
            int len = getChars(this.buffer);
            if (buf instanceof StringBuilder) {
                ((StringBuilder) buf).append(this.buffer, 0, len);
            } else if (buf instanceof StringBuffer) {
                ((StringBuffer) buf).append(this.buffer, 0, len);
            } else if (!-assertionsDisabled) {
                throw new AssertionError();
            }
        }

        public int getDecimalExponent() {
            return this.decExponent;
        }

        public int getDigits(char[] digits) {
            System.arraycopy(this.digits, this.firstDigitIndex, digits, 0, this.nDigits);
            return this.nDigits;
        }

        public boolean isNegative() {
            return this.isNegative;
        }

        public boolean isExceptional() {
            return FloatingDecimal.-assertionsDisabled;
        }

        public boolean digitsRoundedUp() {
            return this.decimalDigitsRoundedUp;
        }

        public boolean decimalDigitsExact() {
            return this.exactDecimalConversion;
        }

        private void setSign(boolean isNegative) {
            this.isNegative = isNegative;
        }

        private void developLongDigits(int decExponent, long lvalue, int insignificantDigits) {
            if (insignificantDigits != 0) {
                long pow10 = FDBigInteger.LONG_5_POW[insignificantDigits] << insignificantDigits;
                long residue = lvalue % pow10;
                lvalue /= pow10;
                decExponent += insignificantDigits;
                if (residue >= (pow10 >> 1)) {
                    lvalue++;
                }
            }
            int digitno = this.digits.length - 1;
            int c;
            int digitno2;
            if (lvalue > 2147483647L) {
                c = (int) (lvalue % 10);
                lvalue /= 10;
                while (c == 0) {
                    decExponent++;
                    c = (int) (lvalue % 10);
                    lvalue /= 10;
                }
                while (true) {
                    digitno2 = digitno;
                    if (lvalue == 0) {
                        break;
                    }
                    digitno = digitno2 - 1;
                    this.digits[digitno2] = (char) (c + 48);
                    decExponent++;
                    c = (int) (lvalue % 10);
                    lvalue /= 10;
                }
                this.digits[digitno2] = (char) (c + 48);
                digitno = digitno2;
            } else if (-assertionsDisabled || lvalue > 0) {
                int ivalue = (int) lvalue;
                c = ivalue % 10;
                ivalue /= 10;
                while (c == 0) {
                    decExponent++;
                    c = ivalue % 10;
                    ivalue /= 10;
                }
                while (true) {
                    digitno2 = digitno;
                    if (ivalue == 0) {
                        break;
                    }
                    digitno = digitno2 - 1;
                    this.digits[digitno2] = (char) (c + 48);
                    decExponent++;
                    c = ivalue % 10;
                    ivalue /= 10;
                }
                this.digits[digitno2] = (char) (c + 48);
                digitno = digitno2;
            } else {
                throw new AssertionError(Long.valueOf(lvalue));
            }
            this.decExponent = decExponent + 1;
            this.firstDigitIndex = digitno;
            this.nDigits = this.digits.length - digitno;
        }

        private void dtoa(int binExp, long fractBits, int nSignificantBits, boolean isCompatibleFormat) {
            if (!-assertionsDisabled && fractBits <= 0) {
                throw new AssertionError();
            } else if (-assertionsDisabled || (FloatingDecimal.FRACT_HOB & fractBits) != 0) {
                int tailZeros = Long.numberOfTrailingZeros(fractBits);
                int nFractBits = 53 - tailZeros;
                this.decimalDigitsRoundedUp = FloatingDecimal.-assertionsDisabled;
                this.exactDecimalConversion = FloatingDecimal.-assertionsDisabled;
                int nTinyBits = Math.max(0, (nFractBits - binExp) - 1);
                if (binExp > FloatingDecimal.MAX_SMALL_BIN_EXP || binExp < FloatingDecimal.MIN_SMALL_BIN_EXP || nTinyBits >= FDBigInteger.LONG_5_POW.length || N_5_BITS[nTinyBits] + nFractBits >= 64 || nTinyBits != 0) {
                    int i;
                    int ndigit;
                    boolean low;
                    boolean high;
                    long lowDigitDifference;
                    int decExp = estimateDecExp(fractBits, binExp);
                    int B5 = Math.max(0, -decExp);
                    int B2 = (B5 + nTinyBits) + binExp;
                    int S5 = Math.max(0, decExp);
                    int S2 = S5 + nTinyBits;
                    int M5 = B5;
                    int M2 = B2 - nSignificantBits;
                    fractBits >>>= tailZeros;
                    B2 -= nFractBits - 1;
                    int common2factor = Math.min(B2, S2);
                    B2 -= common2factor;
                    S2 -= common2factor;
                    M2 -= common2factor;
                    if (nFractBits == 1) {
                        M2--;
                    }
                    if (M2 < 0) {
                        B2 -= M2;
                        S2 -= M2;
                        M2 = 0;
                    }
                    int Bbits = (nFractBits + B2) + (B5 < N_5_BITS.length ? N_5_BITS[B5] : B5 * 3);
                    int i2 = S2 + 1;
                    if (S5 + 1 < N_5_BITS.length) {
                        i = N_5_BITS[S5 + 1];
                    } else {
                        i = (S5 + 1) * 3;
                    }
                    int tenSbits = i2 + i;
                    int q;
                    int ndigit2;
                    if (Bbits >= 64 || tenSbits >= 64) {
                        FDBigInteger Sval = FDBigInteger.valueOfPow52(S5, S2);
                        int shiftBias = Sval.getNormalizationBias();
                        Sval = Sval.leftShift(shiftBias);
                        FDBigInteger Bval = FDBigInteger.valueOfMulPow52(fractBits, B5, B2 + shiftBias);
                        FDBigInteger Mval = FDBigInteger.valueOfPow52(B5 + 1, (M2 + shiftBias) + 1);
                        FDBigInteger tenSval = FDBigInteger.valueOfPow52(S5 + 1, (S2 + shiftBias) + 1);
                        ndigit = 0;
                        q = Bval.quoRemIteration(Sval);
                        low = Bval.cmp(Mval) < 0 ? true : FloatingDecimal.-assertionsDisabled;
                        high = tenSval.addAndCmp(Bval, Mval) <= 0 ? true : FloatingDecimal.-assertionsDisabled;
                        if (-assertionsDisabled || q < 10) {
                            if (q != 0 || (high ^ 1) == 0) {
                                ndigit = 1;
                                this.digits[0] = (char) (q + 48);
                            } else {
                                decExp--;
                            }
                            if (!isCompatibleFormat || decExp < -3 || decExp >= 8) {
                                low = FloatingDecimal.-assertionsDisabled;
                                high = FloatingDecimal.-assertionsDisabled;
                                ndigit2 = ndigit;
                            } else {
                                ndigit2 = ndigit;
                            }
                            while (!low && (high ^ 1) != 0) {
                                q = Bval.quoRemIteration(Sval);
                                if (-assertionsDisabled || q < 10) {
                                    Mval = Mval.multBy10();
                                    low = Bval.cmp(Mval) < 0 ? true : FloatingDecimal.-assertionsDisabled;
                                    high = tenSval.addAndCmp(Bval, Mval) <= 0 ? true : FloatingDecimal.-assertionsDisabled;
                                    ndigit = ndigit2 + 1;
                                    this.digits[ndigit2] = (char) (q + 48);
                                    ndigit2 = ndigit;
                                } else {
                                    throw new AssertionError(Integer.valueOf(q));
                                }
                            }
                            if (high && low) {
                                Bval = Bval.leftShift(1);
                                lowDigitDifference = (long) Bval.cmp(tenSval);
                            } else {
                                lowDigitDifference = 0;
                            }
                            this.exactDecimalConversion = Bval.cmp(FDBigInteger.ZERO) == 0 ? true : FloatingDecimal.-assertionsDisabled;
                            ndigit = ndigit2;
                        } else {
                            throw new AssertionError(Integer.valueOf(q));
                        }
                    } else if (Bbits >= 32 || tenSbits >= 32) {
                        long b = (FDBigInteger.LONG_5_POW[B5] * fractBits) << B2;
                        long s = FDBigInteger.LONG_5_POW[S5] << S2;
                        long tens = s * 10;
                        ndigit = 0;
                        q = (int) (b / s);
                        b = 10 * (b % s);
                        long m = (FDBigInteger.LONG_5_POW[B5] << M2) * 10;
                        low = b < m ? true : FloatingDecimal.-assertionsDisabled;
                        high = b + m > tens ? true : FloatingDecimal.-assertionsDisabled;
                        if (-assertionsDisabled || q < 10) {
                            if (q != 0 || (high ^ 1) == 0) {
                                ndigit = 1;
                                this.digits[0] = (char) (q + 48);
                            } else {
                                decExp--;
                            }
                            if (!isCompatibleFormat || decExp < -3 || decExp >= 8) {
                                low = FloatingDecimal.-assertionsDisabled;
                                high = FloatingDecimal.-assertionsDisabled;
                                ndigit2 = ndigit;
                            } else {
                                ndigit2 = ndigit;
                            }
                            while (!low && (high ^ 1) != 0) {
                                q = (int) (b / s);
                                b = 10 * (b % s);
                                m *= 10;
                                if (-assertionsDisabled || q < 10) {
                                    if (m > 0) {
                                        low = b < m ? true : FloatingDecimal.-assertionsDisabled;
                                        high = b + m > tens ? true : FloatingDecimal.-assertionsDisabled;
                                    } else {
                                        low = true;
                                        high = true;
                                    }
                                    ndigit = ndigit2 + 1;
                                    this.digits[ndigit2] = (char) (q + 48);
                                    ndigit2 = ndigit;
                                } else {
                                    throw new AssertionError(Integer.valueOf(q));
                                }
                            }
                            lowDigitDifference = (b << 1) - tens;
                            this.exactDecimalConversion = b == 0 ? true : FloatingDecimal.-assertionsDisabled;
                            ndigit = ndigit2;
                        } else {
                            throw new AssertionError(Integer.valueOf(q));
                        }
                    } else {
                        int b2 = (((int) fractBits) * FDBigInteger.SMALL_5_POW[B5]) << B2;
                        int s2 = FDBigInteger.SMALL_5_POW[S5] << S2;
                        int tens2 = s2 * 10;
                        ndigit = 0;
                        q = b2 / s2;
                        b2 = (b2 % s2) * 10;
                        int m2 = (FDBigInteger.SMALL_5_POW[B5] << M2) * 10;
                        low = b2 < m2 ? true : FloatingDecimal.-assertionsDisabled;
                        high = b2 + m2 > tens2 ? true : FloatingDecimal.-assertionsDisabled;
                        if (-assertionsDisabled || q < 10) {
                            boolean z;
                            if (q != 0 || (high ^ 1) == 0) {
                                ndigit = 1;
                                this.digits[0] = (char) (q + 48);
                            } else {
                                decExp--;
                            }
                            if (!isCompatibleFormat || decExp < -3 || decExp >= 8) {
                                low = FloatingDecimal.-assertionsDisabled;
                                high = FloatingDecimal.-assertionsDisabled;
                                ndigit2 = ndigit;
                            } else {
                                ndigit2 = ndigit;
                            }
                            while (!low && (high ^ 1) != 0) {
                                q = b2 / s2;
                                b2 = (b2 % s2) * 10;
                                m2 *= 10;
                                if (-assertionsDisabled || q < 10) {
                                    if (((long) m2) > 0) {
                                        low = b2 < m2 ? true : FloatingDecimal.-assertionsDisabled;
                                        high = b2 + m2 > tens2 ? true : FloatingDecimal.-assertionsDisabled;
                                    } else {
                                        low = true;
                                        high = true;
                                    }
                                    ndigit = ndigit2 + 1;
                                    this.digits[ndigit2] = (char) (q + 48);
                                    ndigit2 = ndigit;
                                } else {
                                    throw new AssertionError(Integer.valueOf(q));
                                }
                            }
                            lowDigitDifference = (long) ((b2 << 1) - tens2);
                            if (b2 == 0) {
                                z = true;
                            } else {
                                z = FloatingDecimal.-assertionsDisabled;
                            }
                            this.exactDecimalConversion = z;
                            ndigit = ndigit2;
                        } else {
                            throw new AssertionError(Integer.valueOf(q));
                        }
                    }
                    this.decExponent = decExp + 1;
                    this.firstDigitIndex = 0;
                    this.nDigits = ndigit;
                    if (high) {
                        if (!low) {
                            roundup();
                        } else if (lowDigitDifference == 0) {
                            if ((this.digits[(this.firstDigitIndex + this.nDigits) - 1] & 1) != 0) {
                                roundup();
                            }
                        } else if (lowDigitDifference > 0) {
                            roundup();
                        }
                    }
                    return;
                }
                int insignificant;
                if (binExp > nSignificantBits) {
                    insignificant = insignificantDigitsForPow2((binExp - nSignificantBits) - 1);
                } else {
                    insignificant = 0;
                }
                if (binExp >= FloatingDecimal.EXP_SHIFT) {
                    fractBits <<= binExp - 52;
                } else {
                    fractBits >>>= 52 - binExp;
                }
                developLongDigits(0, fractBits, insignificant);
            } else {
                throw new AssertionError();
            }
        }

        private void roundup() {
            int i = (this.firstDigitIndex + this.nDigits) - 1;
            int q = this.digits[i];
            if (q == 57) {
                while (q == 57 && i > this.firstDigitIndex) {
                    this.digits[i] = '0';
                    i--;
                    q = this.digits[i];
                }
                if (q == 57) {
                    this.decExponent++;
                    this.digits[this.firstDigitIndex] = '1';
                    return;
                }
            }
            this.digits[i] = (char) (q + 1);
            this.decimalDigitsRoundedUp = true;
        }

        static int estimateDecExp(long fractBits, int binExp) {
            double d = (((Double.longBitsToDouble((DoubleConsts.SIGNIF_BIT_MASK & fractBits) | FloatingDecimal.EXP_ONE) - 1.5d) * 0.289529654d) + 0.176091259d) + (((double) binExp) * 0.301029995663981d);
            long dBits = Double.doubleToRawLongBits(d);
            int exponent = ((int) ((DoubleConsts.EXP_BIT_MASK & dBits) >> FloatingDecimal.EXP_SHIFT)) - 1023;
            boolean isNegative = (Long.MIN_VALUE & dBits) != 0 ? true : FloatingDecimal.-assertionsDisabled;
            if (exponent >= 0 && exponent < FloatingDecimal.EXP_SHIFT) {
                long mask = DoubleConsts.SIGNIF_BIT_MASK >> exponent;
                int r = (int) (((DoubleConsts.SIGNIF_BIT_MASK & dBits) | FloatingDecimal.FRACT_HOB) >> (52 - exponent));
                if (isNegative) {
                    r = (mask & dBits) == 0 ? -r : (-r) - 1;
                }
                return r;
            } else if (exponent >= 0) {
                return (int) d;
            } else {
                int i = (Long.MAX_VALUE & dBits) == 0 ? 0 : isNegative ? -1 : 0;
                return i;
            }
        }

        private static int insignificantDigits(int insignificant) {
            int i = 0;
            while (((long) insignificant) >= 10) {
                insignificant = (int) (((long) insignificant) / 10);
                i++;
            }
            return i;
        }

        private static int insignificantDigitsForPow2(int p2) {
            if (p2 <= 1 || p2 >= insignificantDigitsNumber.length) {
                return 0;
            }
            return insignificantDigitsNumber[p2];
        }

        private int getChars(char[] result) {
            if (-assertionsDisabled || this.nDigits <= 19) {
                int i = 0;
                if (this.isNegative) {
                    result[0] = '-';
                    i = 1;
                }
                int i2;
                if (this.decExponent > 0 && this.decExponent < 8) {
                    int charLength = Math.min(this.nDigits, this.decExponent);
                    System.arraycopy(this.digits, this.firstDigitIndex, result, i, charLength);
                    i += charLength;
                    if (charLength < this.decExponent) {
                        charLength = this.decExponent - charLength;
                        Arrays.fill(result, i, i + charLength, '0');
                        i += charLength;
                        i2 = i + 1;
                        result[i] = '.';
                        i = i2 + 1;
                        result[i2] = '0';
                        return i;
                    }
                    i2 = i + 1;
                    result[i] = '.';
                    if (charLength < this.nDigits) {
                        int t = this.nDigits - charLength;
                        System.arraycopy(this.digits, this.firstDigitIndex + charLength, result, i2, t);
                        return i2 + t;
                    }
                    i = i2 + 1;
                    result[i2] = '0';
                    return i;
                } else if (this.decExponent > 0 || this.decExponent <= -3) {
                    int e;
                    i2 = i + 1;
                    result[i] = this.digits[this.firstDigitIndex];
                    i = i2 + 1;
                    result[i2] = '.';
                    if (this.nDigits > 1) {
                        System.arraycopy(this.digits, this.firstDigitIndex + 1, result, i, this.nDigits - 1);
                        i += this.nDigits - 1;
                    } else {
                        i2 = i + 1;
                        result[i] = '0';
                        i = i2;
                    }
                    i2 = i + 1;
                    result[i] = 'E';
                    if (this.decExponent <= 0) {
                        i = i2 + 1;
                        result[i2] = '-';
                        e = (-this.decExponent) + 1;
                        i2 = i;
                    } else {
                        e = this.decExponent - 1;
                    }
                    if (e <= 9) {
                        i = i2 + 1;
                        result[i2] = (char) (e + 48);
                        return i;
                    } else if (e <= 99) {
                        i = i2 + 1;
                        result[i2] = (char) ((e / 10) + 48);
                        i2 = i + 1;
                        result[i] = (char) ((e % 10) + 48);
                        return i2;
                    } else {
                        i = i2 + 1;
                        result[i2] = (char) ((e / 100) + 48);
                        e %= 100;
                        i2 = i + 1;
                        result[i] = (char) ((e / 10) + 48);
                        i = i2 + 1;
                        result[i2] = (char) ((e % 10) + 48);
                        return i;
                    }
                } else {
                    i2 = i + 1;
                    result[i] = '0';
                    i = i2 + 1;
                    result[i2] = '.';
                    if (this.decExponent != 0) {
                        Arrays.fill(result, i, i - this.decExponent, '0');
                        i -= this.decExponent;
                    }
                    System.arraycopy(this.digits, this.firstDigitIndex, result, i, this.nDigits);
                    return i + this.nDigits;
                }
            }
            throw new AssertionError(Integer.valueOf(this.nDigits));
        }
    }

    private static class ExceptionalBinaryToASCIIBuffer implements BinaryToASCIIConverter {
        static final /* synthetic */ boolean -assertionsDisabled = (ExceptionalBinaryToASCIIBuffer.class.desiredAssertionStatus() ^ 1);
        private final String image;
        private boolean isNegative;

        public ExceptionalBinaryToASCIIBuffer(String image, boolean isNegative) {
            this.image = image;
            this.isNegative = isNegative;
        }

        public String toJavaFormatString() {
            return this.image;
        }

        public void appendTo(Appendable buf) {
            if (buf instanceof StringBuilder) {
                ((StringBuilder) buf).append(this.image);
            } else if (buf instanceof StringBuffer) {
                ((StringBuffer) buf).append(this.image);
            } else if (!-assertionsDisabled) {
                throw new AssertionError();
            }
        }

        public int getDecimalExponent() {
            throw new IllegalArgumentException("Exceptional value does not have an exponent");
        }

        public int getDigits(char[] digits) {
            throw new IllegalArgumentException("Exceptional value does not have digits");
        }

        public boolean isNegative() {
            return this.isNegative;
        }

        public boolean isExceptional() {
            return true;
        }

        public boolean digitsRoundedUp() {
            throw new IllegalArgumentException("Exceptional value is not rounded");
        }

        public boolean decimalDigitsExact() {
            throw new IllegalArgumentException("Exceptional value is not exact");
        }
    }

    private static class HexFloatPattern {
        private static final Pattern VALUE = Pattern.compile("([-+])?0[xX](((\\p{XDigit}+)\\.?)|((\\p{XDigit}*)\\.(\\p{XDigit}+)))[pP]([-+])?(\\p{Digit}+)[fFdD]?");

        private HexFloatPattern() {
        }
    }

    static class PreparedASCIIToBinaryBuffer implements ASCIIToBinaryConverter {
        private final double doubleVal;
        private final float floatVal;

        public PreparedASCIIToBinaryBuffer(double doubleVal, float floatVal) {
            this.doubleVal = doubleVal;
            this.floatVal = floatVal;
        }

        public double doubleValue() {
            return this.doubleVal;
        }

        public float floatValue() {
            return this.floatVal;
        }
    }

    public static String toJavaFormatString(double d) {
        return getBinaryToASCIIConverter(d).toJavaFormatString();
    }

    public static String toJavaFormatString(float f) {
        return getBinaryToASCIIConverter(f).toJavaFormatString();
    }

    public static void appendTo(double d, Appendable buf) {
        getBinaryToASCIIConverter(d).appendTo(buf);
    }

    public static void appendTo(float f, Appendable buf) {
        getBinaryToASCIIConverter(f).appendTo(buf);
    }

    public static double parseDouble(String s) throws NumberFormatException {
        return readJavaFormatString(s).doubleValue();
    }

    public static float parseFloat(String s) throws NumberFormatException {
        return readJavaFormatString(s).floatValue();
    }

    private static BinaryToASCIIBuffer getBinaryToASCIIBuffer() {
        return (BinaryToASCIIBuffer) threadLocalBinaryToASCIIBuffer.get();
    }

    public static BinaryToASCIIConverter getBinaryToASCIIConverter(double d) {
        return getBinaryToASCIIConverter(d, true);
    }

    static BinaryToASCIIConverter getBinaryToASCIIConverter(double d, boolean isCompatibleFormat) {
        long dBits = Double.doubleToRawLongBits(d);
        boolean isNegative = (Long.MIN_VALUE & dBits) != 0 ? true : -assertionsDisabled;
        long fractBits = dBits & DoubleConsts.SIGNIF_BIT_MASK;
        int binExp = (int) ((DoubleConsts.EXP_BIT_MASK & dBits) >> EXP_SHIFT);
        if (binExp != 2047) {
            int nSignificantBits;
            if (binExp != 0) {
                fractBits |= FRACT_HOB;
                nSignificantBits = 53;
            } else if (fractBits == 0) {
                return isNegative ? B2AC_NEGATIVE_ZERO : B2AC_POSITIVE_ZERO;
            } else {
                int leadingZeros = Long.numberOfLeadingZeros(fractBits);
                int shift = leadingZeros - 11;
                fractBits <<= shift;
                binExp = 1 - shift;
                nSignificantBits = 64 - leadingZeros;
            }
            binExp -= 1023;
            BinaryToASCIIBuffer buf = getBinaryToASCIIBuffer();
            buf.setSign(isNegative);
            buf.dtoa(binExp, fractBits, nSignificantBits, isCompatibleFormat);
            return buf;
        } else if (fractBits != 0) {
            return B2AC_NOT_A_NUMBER;
        } else {
            BinaryToASCIIConverter binaryToASCIIConverter;
            if (isNegative) {
                binaryToASCIIConverter = B2AC_NEGATIVE_INFINITY;
            } else {
                binaryToASCIIConverter = B2AC_POSITIVE_INFINITY;
            }
            return binaryToASCIIConverter;
        }
    }

    private static BinaryToASCIIConverter getBinaryToASCIIConverter(float f) {
        int fBits = Float.floatToRawIntBits(f);
        boolean isNegative = (Integer.MIN_VALUE & fBits) != 0 ? true : -assertionsDisabled;
        int fractBits = fBits & FloatConsts.SIGNIF_BIT_MASK;
        int binExp = (FloatConsts.EXP_BIT_MASK & fBits) >> 23;
        if (binExp != 255) {
            int nSignificantBits;
            if (binExp != 0) {
                fractBits |= SINGLE_FRACT_HOB;
                nSignificantBits = 24;
            } else if (fractBits == 0) {
                return isNegative ? B2AC_NEGATIVE_ZERO : B2AC_POSITIVE_ZERO;
            } else {
                int leadingZeros = Integer.numberOfLeadingZeros(fractBits);
                int shift = leadingZeros - 8;
                fractBits <<= shift;
                binExp = 1 - shift;
                nSignificantBits = 32 - leadingZeros;
            }
            binExp -= 127;
            BinaryToASCIIBuffer buf = getBinaryToASCIIBuffer();
            buf.setSign(isNegative);
            buf.dtoa(binExp, ((long) fractBits) << 29, nSignificantBits, true);
            return buf;
        } else if (((long) fractBits) != 0) {
            return B2AC_NOT_A_NUMBER;
        } else {
            BinaryToASCIIConverter binaryToASCIIConverter;
            if (isNegative) {
                binaryToASCIIConverter = B2AC_NEGATIVE_INFINITY;
            } else {
                binaryToASCIIConverter = B2AC_POSITIVE_INFINITY;
            }
            return binaryToASCIIConverter;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:141:0x0148 A:{SYNTHETIC} */
    /* JADX WARNING: Removed duplicated region for block: B:61:0x0104 A:{Catch:{ StringIndexOutOfBoundsException -> 0x0017 }} */
    /* JADX WARNING: Removed duplicated region for block: B:105:0x01ad A:{Catch:{ StringIndexOutOfBoundsException -> 0x0017 }} */
    /* JADX WARNING: Removed duplicated region for block: B:81:0x014c A:{Catch:{ StringIndexOutOfBoundsException -> 0x0017 }} */
    /* JADX WARNING: Removed duplicated region for block: B:106:0x01b0 A:{Catch:{ StringIndexOutOfBoundsException -> 0x0017 }} */
    /* JADX WARNING: Removed duplicated region for block: B:85:0x0154 A:{Catch:{ StringIndexOutOfBoundsException -> 0x0017 }} */
    /* JADX WARNING: Removed duplicated region for block: B:88:0x015a A:{Catch:{ StringIndexOutOfBoundsException -> 0x0017 }} */
    /* JADX WARNING: Removed duplicated region for block: B:133:0x021a A:{Catch:{ StringIndexOutOfBoundsException -> 0x0017 }} */
    /* JADX WARNING: Removed duplicated region for block: B:128:0x020d A:{Catch:{ StringIndexOutOfBoundsException -> 0x0017 }} */
    /* JADX WARNING: Missing block: B:114:0x01c7, code:
            if (r15 != r10) goto L_0x01c9;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    static ASCIIToBinaryConverter readJavaFormatString(String in) throws NumberFormatException {
        boolean isNegative = -assertionsDisabled;
        boolean signSeen = -assertionsDisabled;
        try {
            in = in.trim();
            int len = in.length();
            if (len == 0) {
                throw new NumberFormatException("empty String");
            }
            int i = 0;
            switch (in.charAt(0)) {
                case '+':
                    break;
                case '-':
                    isNegative = true;
                    break;
            }
            i = 1;
            signSeen = true;
            char c = in.charAt(i);
            if (c == 'N') {
                if (len - i == NAN_LENGTH && in.indexOf(NAN_REP, i) == i) {
                    return A2BC_NOT_A_NUMBER;
                }
            } else if (c == 'I') {
                if (len - i == INFINITY_LENGTH && in.indexOf(INFINITY_REP, i) == i) {
                    return isNegative ? A2BC_NEGATIVE_INFINITY : A2BC_POSITIVE_INFINITY;
                }
            } else {
                int nDigits;
                boolean isZero;
                if (c == '0' && len > i + 1) {
                    char ch = in.charAt(i + 1);
                    if (ch == 'x' || ch == 'X') {
                        return parseHexString(in);
                    }
                }
                char[] digits = new char[len];
                int nDigits2 = 0;
                boolean decSeen = -assertionsDisabled;
                int decPt = 0;
                int nLeadZero = 0;
                int nTrailZero = 0;
                while (i < len) {
                    c = in.charAt(i);
                    if (c == '0') {
                        nLeadZero++;
                    } else if (c != '.') {
                        while (true) {
                            nDigits = nDigits2;
                            if (i >= len) {
                                c = in.charAt(i);
                                if (c >= '1' && c <= '9') {
                                    nDigits2 = nDigits + 1;
                                    digits[nDigits] = c;
                                    nTrailZero = 0;
                                } else if (c == '0') {
                                    nDigits2 = nDigits + 1;
                                    digits[nDigits] = c;
                                    nTrailZero++;
                                } else if (c == '.') {
                                    if (decSeen) {
                                        throw new NumberFormatException("multiple points");
                                    }
                                    decPt = i;
                                    if (signSeen) {
                                        decPt--;
                                    }
                                    decSeen = true;
                                    nDigits2 = nDigits;
                                }
                                i++;
                            }
                        }
                        nDigits2 = nDigits - nTrailZero;
                        isZero = nDigits2 != 0 ? true : -assertionsDisabled;
                        if (!(isZero && nLeadZero == 0)) {
                            int decExp;
                            if (decSeen) {
                                decExp = nDigits2 + nTrailZero;
                            } else {
                                decExp = decPt - nLeadZero;
                            }
                            if (i < len) {
                                c = in.charAt(i);
                                if (c == 'e' || c == 'E') {
                                    int expSign = 1;
                                    int expVal = 0;
                                    boolean expOverflow = -assertionsDisabled;
                                    i++;
                                    switch (in.charAt(i)) {
                                        case '+':
                                            break;
                                        case '-':
                                            expSign = -1;
                                            break;
                                    }
                                    i++;
                                    int expAt = i;
                                    while (true) {
                                        int i2 = i;
                                        if (i2 < len) {
                                            if (expVal >= 214748364) {
                                                expOverflow = true;
                                            }
                                            i = i2 + 1;
                                            c = in.charAt(i2);
                                            if (c < '0' || c > '9') {
                                                i--;
                                            } else {
                                                expVal = (expVal * 10) + (c - 48);
                                            }
                                        } else {
                                            i = i2;
                                        }
                                    }
                                    i--;
                                    int expLimit = (nDigits2 + BIG_DECIMAL_EXPONENT) + nTrailZero;
                                    if (expOverflow || expVal > expLimit) {
                                        decExp = expSign * expLimit;
                                    } else {
                                        decExp += expSign * expVal;
                                    }
                                }
                            }
                            if (i >= len || (i == len - 1 && (in.charAt(i) == 'f' || in.charAt(i) == 'F' || in.charAt(i) == 'd' || in.charAt(i) == 'D'))) {
                                if (isZero) {
                                    return new ASCIIToBinaryBuffer(isNegative, decExp, digits, nDigits2);
                                }
                                ASCIIToBinaryConverter aSCIIToBinaryConverter;
                                if (isNegative) {
                                    aSCIIToBinaryConverter = A2BC_NEGATIVE_ZERO;
                                } else {
                                    aSCIIToBinaryConverter = A2BC_POSITIVE_ZERO;
                                }
                                return aSCIIToBinaryConverter;
                            }
                        }
                    } else if (decSeen) {
                        throw new NumberFormatException("multiple points");
                    } else {
                        decPt = i;
                        if (signSeen) {
                            decPt--;
                        }
                        decSeen = true;
                    }
                    i++;
                }
                while (true) {
                    nDigits = nDigits2;
                    if (i >= len) {
                    }
                    i++;
                }
                nDigits2 = nDigits - nTrailZero;
                if (nDigits2 != 0) {
                }
                if (decSeen) {
                }
                if (i < len) {
                }
                if (isZero) {
                }
            }
            throw new NumberFormatException("For input string: \"" + in + "\"");
        } catch (StringIndexOutOfBoundsException e) {
        }
    }

    /* JADX WARNING: Missing block: B:72:0x01e2, code:
            r19 = r19 + 1;
     */
    /* JADX WARNING: Missing block: B:73:0x01e6, code:
            if (r19 >= r35) goto L_0x027d;
     */
    /* JADX WARNING: Missing block: B:75:0x01ea, code:
            if ((r39 ^ 1) == 0) goto L_0x027d;
     */
    /* JADX WARNING: Missing block: B:76:0x01ec, code:
            r6 = (long) getHexDigit(r38, r19);
     */
    /* JADX WARNING: Missing block: B:77:0x01f7, code:
            if (r39 != false) goto L_0x01ff;
     */
    /* JADX WARNING: Missing block: B:79:0x01fd, code:
            if (r6 == 0) goto L_0x027a;
     */
    /* JADX WARNING: Missing block: B:80:0x01ff, code:
            r39 = true;
     */
    /* JADX WARNING: Missing block: B:107:0x027a, code:
            r39 = -assertionsDisabled;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    static ASCIIToBinaryConverter parseHexString(String s) {
        Matcher m = HexFloatPattern.VALUE.matcher(s);
        if (m.matches()) {
            boolean isNegative;
            String significandString;
            int leftDigits;
            int exponentAdjust;
            String group1 = m.group(1);
            if (group1 != null) {
                isNegative = group1.equals(LanguageTag.SEP);
            } else {
                isNegative = -assertionsDisabled;
            }
            int rightDigits = 0;
            String group4 = m.group(4);
            if (group4 != null) {
                significandString = stripLeadingZeros(group4);
                leftDigits = significandString.length();
            } else {
                String group6 = stripLeadingZeros(m.group(6));
                leftDigits = group6.length();
                String group7 = m.group(7);
                rightDigits = group7.length();
                StringBuilder stringBuilder = new StringBuilder();
                if (group6 == null) {
                    group6 = "";
                }
                significandString = stringBuilder.append(group6).append(group7).-java_util_stream_Collectors-mthref-7();
            }
            significandString = stripLeadingZeros(significandString);
            int signifLength = significandString.length();
            if (leftDigits >= 1) {
                exponentAdjust = (leftDigits - 1) * 4;
            } else {
                exponentAdjust = ((rightDigits - signifLength) + 1) * -4;
            }
            ASCIIToBinaryConverter aSCIIToBinaryConverter;
            if (signifLength == 0) {
                if (isNegative) {
                    aSCIIToBinaryConverter = A2BC_NEGATIVE_ZERO;
                } else {
                    aSCIIToBinaryConverter = A2BC_POSITIVE_ZERO;
                }
                return aSCIIToBinaryConverter;
            }
            String group8 = m.group(8);
            boolean positiveExponent = group8 != null ? group8.equals("+") : true;
            try {
                long significand;
                int nextShift;
                long exponent = ((positiveExponent ? 1 : -1) * ((long) Integer.parseInt(m.group(9)))) + ((long) exponentAdjust);
                boolean round = -assertionsDisabled;
                boolean sticky = -assertionsDisabled;
                long leadingDigit = (long) getHexDigit(significandString, 0);
                if (leadingDigit == 1) {
                    significand = 0 | (leadingDigit << EXP_SHIFT);
                    nextShift = 48;
                } else if (leadingDigit <= 3) {
                    significand = 0 | (leadingDigit << 51);
                    nextShift = 47;
                    exponent++;
                } else if (leadingDigit <= 7) {
                    significand = 0 | (leadingDigit << 50);
                    nextShift = 46;
                    exponent += 2;
                } else if (leadingDigit <= 15) {
                    significand = 0 | (leadingDigit << 49);
                    nextShift = 45;
                    exponent += 3;
                } else {
                    throw new AssertionError((Object) "Result from digit conversion too large!");
                }
                int i = 1;
                while (i < signifLength && nextShift >= 0) {
                    significand |= ((long) getHexDigit(significandString, i)) << nextShift;
                    nextShift -= 4;
                    i++;
                }
                if (i < signifLength) {
                    long currentDigit = (long) getHexDigit(significandString, i);
                    switch (nextShift) {
                        case -4:
                            round = (8 & currentDigit) != 0 ? true : -assertionsDisabled;
                            if ((7 & currentDigit) == 0) {
                                sticky = -assertionsDisabled;
                                break;
                            }
                            sticky = true;
                            break;
                        case -3:
                            significand |= (8 & currentDigit) >> 3;
                            round = (4 & currentDigit) != 0 ? true : -assertionsDisabled;
                            if ((3 & currentDigit) == 0) {
                                sticky = -assertionsDisabled;
                                break;
                            }
                            sticky = true;
                            break;
                        case -2:
                            significand |= (12 & currentDigit) >> 2;
                            round = (2 & currentDigit) != 0 ? true : -assertionsDisabled;
                            if ((1 & currentDigit) == 0) {
                                sticky = -assertionsDisabled;
                                break;
                            }
                            sticky = true;
                            break;
                        case -1:
                            significand |= (14 & currentDigit) >> 1;
                            if ((1 & currentDigit) == 0) {
                                round = -assertionsDisabled;
                                break;
                            }
                            round = true;
                            break;
                        default:
                            throw new AssertionError((Object) "Unexpected shift distance remainder.");
                    }
                }
                int floatBits = isNegative ? Integer.MIN_VALUE : 0;
                boolean floatSticky;
                int iValue;
                if (exponent >= -126) {
                    if (exponent > 127) {
                        floatBits |= FloatConsts.EXP_BIT_MASK;
                    } else {
                        floatSticky = ((((1 << 28) - 1) & significand) != 0 || round) ? true : sticky;
                        iValue = (int) (significand >>> 28);
                        if ((iValue & 3) != 1 || floatSticky) {
                            iValue++;
                        }
                        floatBits |= ((((int) exponent) + 126) << 23) + (iValue >> 1);
                    }
                } else if (exponent >= -150) {
                    int threshShift = (int) (-98 - exponent);
                    if (!-assertionsDisabled && threshShift < 29) {
                        throw new AssertionError();
                    } else if (-assertionsDisabled || threshShift < 53) {
                        floatSticky = ((((1 << threshShift) - 1) & significand) != 0 || round) ? true : sticky;
                        iValue = (int) (significand >>> threshShift);
                        if ((iValue & 3) != 1 || floatSticky) {
                            iValue++;
                        }
                        floatBits |= iValue >> 1;
                    } else {
                        throw new AssertionError();
                    }
                }
                float fValue = Float.intBitsToFloat(floatBits);
                if (exponent > 1023) {
                    if (isNegative) {
                        aSCIIToBinaryConverter = A2BC_NEGATIVE_INFINITY;
                    } else {
                        aSCIIToBinaryConverter = A2BC_POSITIVE_INFINITY;
                    }
                    return aSCIIToBinaryConverter;
                }
                double value;
                if (exponent <= 1023 && exponent >= -1022) {
                    significand = (((1023 + exponent) << EXP_SHIFT) & DoubleConsts.EXP_BIT_MASK) | (DoubleConsts.SIGNIF_BIT_MASK & significand);
                } else if (exponent < -1075) {
                    return isNegative ? A2BC_NEGATIVE_ZERO : A2BC_POSITIVE_ZERO;
                } else {
                    sticky = !sticky ? round : true;
                    int bitsDiscarded = 53 - ((((int) exponent) + 1074) + 1);
                    if (-assertionsDisabled || (bitsDiscarded >= 1 && bitsDiscarded <= 53)) {
                        round = ((1 << (bitsDiscarded + -1)) & significand) != 0 ? true : -assertionsDisabled;
                        if (bitsDiscarded > 1) {
                            sticky = (sticky || (significand & (~(-1 << (bitsDiscarded - 1)))) != 0) ? true : -assertionsDisabled;
                        }
                        significand = 0 | (DoubleConsts.SIGNIF_BIT_MASK & (significand >> bitsDiscarded));
                    } else {
                        throw new AssertionError();
                    }
                }
                boolean leastZero = (1 & significand) == 0 ? true : -assertionsDisabled;
                if ((leastZero && round && sticky) || (!leastZero && round)) {
                    significand++;
                }
                if (isNegative) {
                    value = Double.longBitsToDouble(Long.MIN_VALUE | significand);
                } else {
                    value = Double.longBitsToDouble(significand);
                }
                return new PreparedASCIIToBinaryBuffer(value, fValue);
            } catch (NumberFormatException e) {
                aSCIIToBinaryConverter = isNegative ? positiveExponent ? A2BC_NEGATIVE_INFINITY : A2BC_NEGATIVE_ZERO : positiveExponent ? A2BC_POSITIVE_INFINITY : A2BC_POSITIVE_ZERO;
                return aSCIIToBinaryConverter;
            }
        }
        throw new NumberFormatException("For input string: \"" + s + "\"");
    }

    static String stripLeadingZeros(String s) {
        if (s.isEmpty() || s.charAt(0) != '0') {
            return s;
        }
        for (int i = 1; i < s.length(); i++) {
            if (s.charAt(i) != '0') {
                return s.substring(i);
            }
        }
        return "";
    }

    static int getHexDigit(String s, int position) {
        int value = Character.digit(s.charAt(position), 16);
        if (value > -1 && value < 16) {
            return value;
        }
        throw new AssertionError("Unexpected failure of digit conversion of " + s.charAt(position));
    }
}
