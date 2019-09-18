package sun.misc;

import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import sun.util.locale.LanguageTag;

public class FloatingDecimal {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    static final ASCIIToBinaryConverter A2BC_NEGATIVE_INFINITY = new PreparedASCIIToBinaryBuffer(Double.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);
    static final ASCIIToBinaryConverter A2BC_NEGATIVE_ZERO = new PreparedASCIIToBinaryBuffer(-0.0d, -0.0f);
    static final ASCIIToBinaryConverter A2BC_NOT_A_NUMBER = new PreparedASCIIToBinaryBuffer(Double.NaN, Float.NaN);
    static final ASCIIToBinaryConverter A2BC_POSITIVE_INFINITY = new PreparedASCIIToBinaryBuffer(Double.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
    static final ASCIIToBinaryConverter A2BC_POSITIVE_ZERO = new PreparedASCIIToBinaryBuffer(0.0d, 0.0f);
    private static final BinaryToASCIIConverter B2AC_NEGATIVE_INFINITY = new ExceptionalBinaryToASCIIBuffer("-Infinity", true);
    private static final BinaryToASCIIConverter B2AC_NEGATIVE_ZERO = new BinaryToASCIIBuffer(true, new char[]{'0'});
    private static final BinaryToASCIIConverter B2AC_NOT_A_NUMBER = new ExceptionalBinaryToASCIIBuffer(NAN_REP, $assertionsDisabled);
    private static final BinaryToASCIIConverter B2AC_POSITIVE_INFINITY = new ExceptionalBinaryToASCIIBuffer(INFINITY_REP, $assertionsDisabled);
    private static final BinaryToASCIIConverter B2AC_POSITIVE_ZERO = new BinaryToASCIIBuffer($assertionsDisabled, new char[]{'0'});
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
        /* access modifiers changed from: protected */
        public BinaryToASCIIBuffer initialValue() {
            return new BinaryToASCIIBuffer();
        }
    };

    static class ASCIIToBinaryBuffer implements ASCIIToBinaryConverter {
        static final /* synthetic */ boolean $assertionsDisabled = false;
        private static final double[] BIG_10_POW = {1.0E16d, 1.0E32d, 1.0E64d, 1.0E128d, 1.0E256d};
        private static final int MAX_SMALL_TEN = (SMALL_10_POW.length - 1);
        private static final int SINGLE_MAX_SMALL_TEN = (SINGLE_SMALL_10_POW.length - 1);
        private static final float[] SINGLE_SMALL_10_POW = {1.0f, 10.0f, 100.0f, 1000.0f, 10000.0f, 100000.0f, 1000000.0f, 1.0E7f, 1.0E8f, 1.0E9f, 1.0E10f};
        private static final double[] SMALL_10_POW;
        private static final double[] TINY_10_POW = {1.0E-16d, 1.0E-32d, 1.0E-64d, 1.0E-128d, 1.0E-256d};
        int decExponent;
        char[] digits;
        boolean isNegative;
        int nDigits;

        static {
            Class<FloatingDecimal> cls = FloatingDecimal.class;
            double[] dArr = new double[FloatingDecimal.SINGLE_EXP_SHIFT];
            // fill-array-data instruction
            dArr[0] = 4607182418800017408;
            dArr[1] = 4621819117588971520;
            dArr[2] = 4636737291354636288;
            dArr[3] = 4652007308841189376;
            dArr[4] = 4666723172467343360;
            dArr[5] = 4681608360884174848;
            dArr[6] = 4696837146684686336;
            dArr[7] = 4711630319722168320;
            dArr[8] = 4726483295884279808;
            dArr[9] = 4741671816366391296;
            dArr[10] = 4756540486875873280;
            dArr[11] = 4771362005757984768;
            dArr[12] = 4786511204640096256;
            dArr[13] = 4801453603149578240;
            dArr[14] = 4816244402031689728;
            dArr[15] = 4831355200913801216;
            dArr[16] = 4846369599423283200;
            dArr[17] = 4861130398305394688;
            dArr[18] = 4876203697187506176;
            dArr[19] = 4891288408196988160;
            dArr[20] = 4906019910204099648;
            dArr[21] = 4921056587992461136;
            dArr[22] = 4936209963552724370;
            SMALL_10_POW = dArr;
        }

        ASCIIToBinaryBuffer(boolean negSign, int decExponent2, char[] digits2, int n) {
            this.isNegative = negSign;
            this.decExponent = decExponent2;
            this.digits = digits2;
            this.nDigits = n;
        }

        /* JADX WARNING: Removed duplicated region for block: B:102:0x01a6  */
        /* JADX WARNING: Removed duplicated region for block: B:103:0x01aa  */
        /* JADX WARNING: Removed duplicated region for block: B:106:0x01ce  */
        /* JADX WARNING: Removed duplicated region for block: B:107:0x01d1  */
        /* JADX WARNING: Removed duplicated region for block: B:110:0x01db  */
        /* JADX WARNING: Removed duplicated region for block: B:111:0x01e2  */
        /* JADX WARNING: Removed duplicated region for block: B:118:0x0216  */
        /* JADX WARNING: Removed duplicated region for block: B:127:0x0238  */
        /* JADX WARNING: Removed duplicated region for block: B:132:0x024c  */
        /* JADX WARNING: Removed duplicated region for block: B:148:0x0286  */
        /* JADX WARNING: Removed duplicated region for block: B:159:0x0282 A[EDGE_INSN: B:159:0x0282->B:146:0x0282 ?: BREAK  , SYNTHETIC] */
        /* JADX WARNING: Removed duplicated region for block: B:98:0x015d  */
        public double doubleValue() {
            double t;
            double dValue;
            int exp;
            long ieeeBits;
            int B5;
            int D5;
            FDBigInteger bigD0;
            FDBigInteger bigD;
            int binexp;
            long bigBbits;
            int binexp2;
            int bigIntExp;
            int hulpbias;
            int D2;
            int cmp;
            FDBigInteger bigD02;
            boolean overvalue;
            FDBigInteger diff;
            int cmpResult;
            double dValue2;
            int kDigits = Math.min(this.nDigits, 16);
            int prevD2 = 0;
            int D22 = Math.min(kDigits, 9);
            int iValue = this.digits[0] - '0';
            for (int i = 1; i < D22; i++) {
                iValue = ((iValue * 10) + this.digits[i]) - 48;
            }
            long lValue = (long) iValue;
            for (int i2 = D22; i2 < kDigits; i2++) {
                lValue = (10 * lValue) + ((long) (this.digits[i2] - '0'));
            }
            double dValue3 = (double) lValue;
            int exp2 = this.decExponent - kDigits;
            if (this.nDigits <= 15) {
                if (exp2 == 0 || dValue3 == 0.0d) {
                    return this.isNegative ? -dValue3 : dValue3;
                } else if (exp2 >= 0) {
                    if (exp2 <= MAX_SMALL_TEN) {
                        double rValue = SMALL_10_POW[exp2] * dValue3;
                        return this.isNegative ? -rValue : rValue;
                    }
                    int slop = 15 - kDigits;
                    if (exp2 <= MAX_SMALL_TEN + slop) {
                        double dValue4 = dValue3 * SMALL_10_POW[slop];
                        double rValue2 = SMALL_10_POW[exp2 - slop] * dValue4;
                        if (this.isNegative) {
                            double d = dValue4;
                            dValue2 = -rValue2;
                        } else {
                            dValue2 = rValue2;
                        }
                        return dValue2;
                    }
                } else if (exp2 >= (-MAX_SMALL_TEN)) {
                    double rValue3 = dValue3 / SMALL_10_POW[-exp2];
                    return this.isNegative ? -rValue3 : rValue3;
                }
            }
            if (exp2 > 0) {
                double d2 = Double.NEGATIVE_INFINITY;
                if (this.decExponent > 309) {
                    if (!this.isNegative) {
                        d2 = Double.POSITIVE_INFINITY;
                    }
                    return d2;
                }
                if ((exp2 & 15) != 0) {
                    dValue3 *= SMALL_10_POW[exp2 & 15];
                }
                int i3 = exp2 >> 4;
                exp2 = i3;
                if (i3 != 0) {
                    double dValue5 = dValue3;
                    int j = 0;
                    while (exp2 > 1) {
                        if ((exp2 & 1) != 0) {
                            dValue5 *= BIG_10_POW[j];
                        }
                        j++;
                        exp2 >>= 1;
                    }
                    t = dValue5 * BIG_10_POW[j];
                    if (Double.isInfinite(t)) {
                        if (Double.isInfinite((dValue5 / 2.0d) * BIG_10_POW[j])) {
                            if (!this.isNegative) {
                                d2 = Double.POSITIVE_INFINITY;
                            }
                            return d2;
                        }
                        t = Double.MAX_VALUE;
                    }
                    double d3 = t;
                    if (this.nDigits > FloatingDecimal.MAX_NDIGITS) {
                        this.nDigits = 1101;
                        this.digits[FloatingDecimal.MAX_NDIGITS] = '1';
                    }
                    int i4 = iValue;
                    dValue = t;
                    FDBigInteger bigD03 = new FDBigInteger(lValue, this.digits, kDigits, this.nDigits);
                    exp = this.decExponent - this.nDigits;
                    ieeeBits = Double.doubleToRawLongBits(dValue);
                    B5 = Math.max(0, -exp);
                    D5 = Math.max(0, exp);
                    bigD0 = bigD03.multByPow52(D5, 0);
                    bigD0.makeImmutable();
                    bigD = null;
                    while (true) {
                        int iDigits = D22;
                        double dValue6 = dValue;
                        binexp = (int) (ieeeBits >>> 52);
                        long bigBbits2 = DoubleConsts.SIGNIF_BIT_MASK & ieeeBits;
                        if (binexp <= 0) {
                            bigBbits = bigBbits2 | FloatingDecimal.FRACT_HOB;
                        } else {
                            int shift = Long.numberOfLeadingZeros(bigBbits2) - 11;
                            bigBbits = bigBbits2 << shift;
                            binexp = 1 - shift;
                        }
                        binexp2 = binexp - 1023;
                        int lowOrderZeros = Long.numberOfTrailingZeros(bigBbits);
                        long bigBbits3 = bigBbits >>> lowOrderZeros;
                        bigIntExp = (binexp2 - 52) + lowOrderZeros;
                        int kDigits2 = kDigits;
                        int bigIntNBits = 53 - lowOrderZeros;
                        int B2 = B5;
                        int D23 = D5;
                        if (bigIntExp < 0) {
                            B2 += bigIntExp;
                        } else {
                            D23 -= bigIntExp;
                        }
                        int Ulp2 = B2;
                        int exp3 = exp;
                        if (binexp2 > -1023) {
                            hulpbias = binexp2 + lowOrderZeros + 1023;
                        } else {
                            hulpbias = 1 + lowOrderZeros;
                        }
                        int hulpbias2 = hulpbias;
                        int D52 = D5;
                        int D53 = B2 + hulpbias2;
                        int i5 = binexp2;
                        int binexp3 = D23 + hulpbias2;
                        int i6 = hulpbias2;
                        long lValue2 = lValue;
                        int Ulp22 = Ulp2;
                        int common2 = Math.min(D53, Math.min(binexp3, Ulp22));
                        int B22 = D53 - common2;
                        D2 = binexp3 - common2;
                        int Ulp23 = Ulp22 - common2;
                        FDBigInteger bigB = FDBigInteger.valueOfMulPow52(bigBbits3, B5, B22);
                        if (bigD == null || prevD2 != D2) {
                            bigD = bigD0.leftShift(D2);
                            prevD2 = D2;
                        }
                        cmp = bigB.cmp(bigD);
                        int cmpResult2 = cmp;
                        if (cmp > 0) {
                            bigD02 = bigD0;
                            int i7 = B22;
                            if (cmpResult2 >= 0) {
                                break;
                            }
                            overvalue = FloatingDecimal.$assertionsDisabled;
                            diff = bigD.rightInplaceSub(bigB);
                        } else {
                            overvalue = true;
                            bigD02 = bigD0;
                            diff = bigB.leftInplaceSub(bigD);
                            int i8 = B22;
                            if (bigIntNBits == 1) {
                                if (bigIntExp > -1022) {
                                    Ulp23--;
                                    if (Ulp23 < 0) {
                                        Ulp23 = 0;
                                        diff = diff.leftShift(1);
                                    }
                                }
                            }
                        }
                        boolean overvalue2 = overvalue;
                        cmpResult = diff.cmpPow52(B5, Ulp23);
                        if (cmpResult < 0) {
                            long j2 = 1;
                            if (cmpResult != 0) {
                                if (overvalue2) {
                                    j2 = -1;
                                }
                                ieeeBits += j2;
                                if (ieeeBits == 0 || ieeeBits == DoubleConsts.EXP_BIT_MASK) {
                                    break;
                                }
                                D22 = iDigits;
                                dValue = dValue6;
                                kDigits = kDigits2;
                                exp = exp3;
                                D5 = D52;
                                lValue = lValue2;
                                bigD0 = bigD02;
                            } else if ((ieeeBits & 1) != 0) {
                                if (overvalue2) {
                                    j2 = -1;
                                }
                                ieeeBits += j2;
                            }
                        } else {
                            break;
                        }
                    }
                    if (this.isNegative) {
                        ieeeBits |= Long.MIN_VALUE;
                    }
                    return Double.longBitsToDouble(ieeeBits);
                }
            } else if (exp2 < 0) {
                int exp4 = -exp2;
                double d4 = -0.0d;
                if (this.decExponent < -325) {
                    if (!this.isNegative) {
                        d4 = 0.0d;
                    }
                    return d4;
                }
                if ((exp4 & 15) != 0) {
                    dValue3 /= SMALL_10_POW[exp4 & 15];
                }
                int i9 = exp4 >> 4;
                exp2 = i9;
                if (i9 != 0) {
                    double dValue7 = dValue3;
                    int j3 = 0;
                    while (exp2 > 1) {
                        if ((exp2 & 1) != 0) {
                            dValue7 *= TINY_10_POW[j3];
                        }
                        j3++;
                        exp2 >>= 1;
                    }
                    double t2 = TINY_10_POW[j3] * dValue7;
                    if (t2 == 0.0d) {
                        if (2.0d * dValue7 * TINY_10_POW[j3] == 0.0d) {
                            if (!this.isNegative) {
                                d4 = 0.0d;
                            }
                            return d4;
                        }
                        t2 = Double.MIN_VALUE;
                    }
                    dValue3 = t2;
                }
            }
            t = dValue3;
            if (this.nDigits > FloatingDecimal.MAX_NDIGITS) {
            }
            int i42 = iValue;
            dValue = t;
            FDBigInteger bigD032 = new FDBigInteger(lValue, this.digits, kDigits, this.nDigits);
            exp = this.decExponent - this.nDigits;
            ieeeBits = Double.doubleToRawLongBits(dValue);
            B5 = Math.max(0, -exp);
            D5 = Math.max(0, exp);
            bigD0 = bigD032.multByPow52(D5, 0);
            bigD0.makeImmutable();
            bigD = null;
            while (true) {
                int iDigits2 = D22;
                double dValue62 = dValue;
                binexp = (int) (ieeeBits >>> 52);
                long bigBbits22 = DoubleConsts.SIGNIF_BIT_MASK & ieeeBits;
                if (binexp <= 0) {
                }
                binexp2 = binexp - 1023;
                int lowOrderZeros2 = Long.numberOfTrailingZeros(bigBbits);
                long bigBbits32 = bigBbits >>> lowOrderZeros2;
                bigIntExp = (binexp2 - 52) + lowOrderZeros2;
                int kDigits22 = kDigits;
                int bigIntNBits2 = 53 - lowOrderZeros2;
                int B23 = B5;
                int D232 = D5;
                if (bigIntExp < 0) {
                }
                int Ulp24 = B23;
                int exp32 = exp;
                if (binexp2 > -1023) {
                }
                int hulpbias22 = hulpbias;
                int D522 = D5;
                int D532 = B23 + hulpbias22;
                int i52 = binexp2;
                int binexp32 = D232 + hulpbias22;
                int i62 = hulpbias22;
                long lValue22 = lValue;
                int Ulp222 = Ulp24;
                int common22 = Math.min(D532, Math.min(binexp32, Ulp222));
                int B222 = D532 - common22;
                D2 = binexp32 - common22;
                int Ulp232 = Ulp222 - common22;
                FDBigInteger bigB2 = FDBigInteger.valueOfMulPow52(bigBbits32, B5, B222);
                bigD = bigD0.leftShift(D2);
                prevD2 = D2;
                cmp = bigB2.cmp(bigD);
                int cmpResult22 = cmp;
                if (cmp > 0) {
                }
                boolean overvalue22 = overvalue;
                cmpResult = diff.cmpPow52(B5, Ulp232);
                if (cmpResult < 0) {
                }
                D22 = iDigits2;
                dValue = dValue62;
                kDigits = kDigits22;
                exp = exp32;
                D5 = D522;
                lValue = lValue22;
                bigD0 = bigD02;
            }
            if (this.isNegative) {
            }
            return Double.longBitsToDouble(ieeeBits);
        }

        /* JADX WARNING: Removed duplicated region for block: B:102:0x01a2  */
        /* JADX WARNING: Removed duplicated region for block: B:103:0x01a7  */
        /* JADX WARNING: Removed duplicated region for block: B:110:0x01df  */
        /* JADX WARNING: Removed duplicated region for block: B:119:0x01ff  */
        /* JADX WARNING: Removed duplicated region for block: B:124:0x0211  */
        /* JADX WARNING: Removed duplicated region for block: B:140:0x0244  */
        /* JADX WARNING: Removed duplicated region for block: B:152:0x0240 A[EDGE_INSN: B:152:0x0240->B:138:0x0240 ?: BREAK  , SYNTHETIC] */
        /* JADX WARNING: Removed duplicated region for block: B:90:0x0135  */
        /* JADX WARNING: Removed duplicated region for block: B:94:0x016e  */
        /* JADX WARNING: Removed duplicated region for block: B:95:0x0173  */
        /* JADX WARNING: Removed duplicated region for block: B:98:0x0195  */
        /* JADX WARNING: Removed duplicated region for block: B:99:0x0198  */
        public float floatValue() {
            double dValue;
            float fValue;
            int exp;
            int ieeeBits;
            int B5;
            int D5;
            FDBigInteger bigD0;
            FDBigInteger bigD;
            int binexp;
            int bigBbits;
            int binexp2;
            int bigIntExp;
            int hulpbias;
            int D2;
            int cmp;
            FDBigInteger bigD02;
            int i;
            boolean overvalue;
            FDBigInteger diff;
            int cmpResult;
            int exp2;
            int bigIntNBits = Math.min(this.nDigits, 8);
            int prevD2 = 0;
            int iValue = this.digits[0] - '0';
            for (int i2 = 1; i2 < bigIntNBits; i2++) {
                iValue = ((iValue * 10) + this.digits[i2]) - 48;
            }
            float fValue2 = (float) iValue;
            int exp3 = this.decExponent - bigIntNBits;
            float f = 0.0f;
            if (this.nDigits <= 7) {
                if (exp3 == 0 || fValue2 == 0.0f) {
                    return this.isNegative ? -fValue2 : fValue2;
                } else if (exp3 >= 0) {
                    if (exp3 <= SINGLE_MAX_SMALL_TEN) {
                        float fValue3 = fValue2 * SINGLE_SMALL_10_POW[exp3];
                        return this.isNegative ? -fValue3 : fValue3;
                    }
                    int slop = 7 - bigIntNBits;
                    if (exp3 <= SINGLE_MAX_SMALL_TEN + slop) {
                        float fValue4 = fValue2 * SINGLE_SMALL_10_POW[slop] * SINGLE_SMALL_10_POW[exp3 - slop];
                        return this.isNegative ? -fValue4 : fValue4;
                    }
                } else if (exp3 >= (-SINGLE_MAX_SMALL_TEN)) {
                    float fValue5 = fValue2 / SINGLE_SMALL_10_POW[-exp3];
                    return this.isNegative ? -fValue5 : fValue5;
                }
            } else if (this.decExponent >= this.nDigits && this.nDigits + this.decExponent <= 15) {
                long lValue = (long) iValue;
                for (int i3 = bigIntNBits; i3 < this.nDigits; i3++) {
                    lValue = (10 * lValue) + ((long) (this.digits[i3] - '0'));
                }
                float fValue6 = (float) (((double) lValue) * SMALL_10_POW[this.decExponent - this.nDigits]);
                return this.isNegative ? -fValue6 : fValue6;
            }
            double dValue2 = (double) fValue2;
            if (exp3 <= 0) {
                if (exp3 < 0) {
                    int exp4 = -exp3;
                    if (this.decExponent < -46) {
                        if (this.isNegative) {
                            f = -0.0f;
                        }
                        return f;
                    }
                    if ((exp4 & 15) != 0) {
                        dValue2 /= SMALL_10_POW[exp4 & 15];
                    }
                    int i4 = exp4 >> 4;
                    exp3 = i4;
                    if (i4 != 0) {
                        int exp5 = exp3;
                        int j = 0;
                        while (exp2 > 0) {
                            if ((exp2 & 1) != 0) {
                                dValue2 *= TINY_10_POW[j];
                            }
                            j++;
                            exp5 = exp2 >> 1;
                        }
                    }
                }
                int i5 = exp3;
                dValue = dValue2;
                fValue = Math.max(Float.MIN_VALUE, Math.min(Float.MAX_VALUE, (float) dValue));
                if (this.nDigits > 200) {
                }
                FDBigInteger bigD03 = new FDBigInteger((long) iValue, this.digits, bigIntNBits, this.nDigits);
                exp = this.decExponent - this.nDigits;
                ieeeBits = Float.floatToRawIntBits(fValue);
                B5 = Math.max(0, -exp);
                D5 = Math.max(0, exp);
                bigD0 = bigD03.multByPow52(D5, 0);
                bigD0.makeImmutable();
                bigD = null;
                while (true) {
                    binexp = ieeeBits >>> FloatingDecimal.SINGLE_EXP_SHIFT;
                    int bigBbits2 = 8388607 & ieeeBits;
                    if (binexp > 0) {
                    }
                    binexp2 = binexp - 127;
                    int lowOrderZeros = Integer.numberOfTrailingZeros(bigBbits);
                    int bigBbits3 = bigBbits >>> lowOrderZeros;
                    bigIntExp = (binexp2 - 23) + lowOrderZeros;
                    int kDigits = bigIntNBits;
                    int bigIntNBits2 = 24 - lowOrderZeros;
                    int B2 = B5;
                    int D22 = D5;
                    if (bigIntExp >= 0) {
                    }
                    int Ulp2 = B2;
                    float fValue7 = fValue;
                    if (binexp2 <= -127) {
                    }
                    int exp6 = exp;
                    int B22 = B2 + hulpbias;
                    int D52 = D5;
                    int D53 = D22 + hulpbias;
                    int i6 = hulpbias;
                    int iValue2 = iValue;
                    int Ulp22 = Ulp2;
                    int common2 = Math.min(B22, Math.min(D53, Ulp22));
                    D2 = D53 - common2;
                    int Ulp23 = Ulp22 - common2;
                    int i7 = common2;
                    double dValue3 = dValue;
                    FDBigInteger bigB = FDBigInteger.valueOfMulPow52((long) bigBbits3, B5, B22 - common2);
                    bigD = bigD0.leftShift(D2);
                    prevD2 = D2;
                    cmp = bigB.cmp(bigD);
                    int cmpResult2 = cmp;
                    if (cmp <= 0) {
                    }
                    boolean overvalue2 = overvalue;
                    cmpResult = diff.cmpPow52(B5, Ulp23);
                    if (cmpResult >= 0) {
                    }
                    bigIntNBits = kDigits;
                    fValue = fValue7;
                    exp = exp6;
                    D5 = D52;
                    iValue = iValue2;
                    dValue = dValue3;
                    bigD0 = bigD02;
                }
                if (this.isNegative) {
                }
                return Float.intBitsToFloat(ieeeBits);
            } else if (this.decExponent > 39) {
                return this.isNegative ? Float.NEGATIVE_INFINITY : Float.POSITIVE_INFINITY;
            } else {
                if ((exp3 & 15) != 0) {
                    dValue2 *= SMALL_10_POW[exp3 & 15];
                }
                int i8 = exp3 >> 4;
                exp3 = i8;
                if (i8 != 0) {
                    exp2 = exp3;
                    int j2 = 0;
                    while (exp2 > 0) {
                        if ((exp2 & 1) != 0) {
                            dValue2 *= BIG_10_POW[j2];
                        }
                        j2++;
                        exp2 >>= 1;
                    }
                }
                int i52 = exp3;
                dValue = dValue2;
                fValue = Math.max(Float.MIN_VALUE, Math.min(Float.MAX_VALUE, (float) dValue));
                if (this.nDigits > 200) {
                    this.nDigits = HttpURLConnection.HTTP_CREATED;
                    this.digits[200] = '1';
                }
                FDBigInteger bigD032 = new FDBigInteger((long) iValue, this.digits, bigIntNBits, this.nDigits);
                exp = this.decExponent - this.nDigits;
                ieeeBits = Float.floatToRawIntBits(fValue);
                B5 = Math.max(0, -exp);
                D5 = Math.max(0, exp);
                bigD0 = bigD032.multByPow52(D5, 0);
                bigD0.makeImmutable();
                bigD = null;
                while (true) {
                    binexp = ieeeBits >>> FloatingDecimal.SINGLE_EXP_SHIFT;
                    int bigBbits22 = 8388607 & ieeeBits;
                    if (binexp > 0) {
                        bigBbits = bigBbits22 | FloatingDecimal.SINGLE_FRACT_HOB;
                    } else {
                        int shift = Integer.numberOfLeadingZeros(bigBbits22) - 8;
                        bigBbits = bigBbits22 << shift;
                        binexp = 1 - shift;
                    }
                    binexp2 = binexp - 127;
                    int lowOrderZeros2 = Integer.numberOfTrailingZeros(bigBbits);
                    int bigBbits32 = bigBbits >>> lowOrderZeros2;
                    bigIntExp = (binexp2 - 23) + lowOrderZeros2;
                    int kDigits2 = bigIntNBits;
                    int bigIntNBits22 = 24 - lowOrderZeros2;
                    int B23 = B5;
                    int D222 = D5;
                    if (bigIntExp >= 0) {
                        B23 += bigIntExp;
                    } else {
                        D222 -= bigIntExp;
                    }
                    int Ulp24 = B23;
                    float fValue72 = fValue;
                    if (binexp2 <= -127) {
                        hulpbias = binexp2 + lowOrderZeros2 + 127;
                    } else {
                        hulpbias = 1 + lowOrderZeros2;
                    }
                    int exp62 = exp;
                    int B222 = B23 + hulpbias;
                    int D522 = D5;
                    int D532 = D222 + hulpbias;
                    int i62 = hulpbias;
                    int iValue22 = iValue;
                    int Ulp222 = Ulp24;
                    int common22 = Math.min(B222, Math.min(D532, Ulp222));
                    D2 = D532 - common22;
                    int Ulp232 = Ulp222 - common22;
                    int i72 = common22;
                    double dValue32 = dValue;
                    FDBigInteger bigB2 = FDBigInteger.valueOfMulPow52((long) bigBbits32, B5, B222 - common22);
                    if (bigD == null || prevD2 != D2) {
                        bigD = bigD0.leftShift(D2);
                        prevD2 = D2;
                    }
                    cmp = bigB2.cmp(bigD);
                    int cmpResult22 = cmp;
                    if (cmp <= 0) {
                        bigD02 = bigD0;
                        i = 1;
                        if (cmpResult22 >= 0) {
                            break;
                        }
                        overvalue = FloatingDecimal.$assertionsDisabled;
                        diff = bigD.rightInplaceSub(bigB2);
                    } else {
                        overvalue = true;
                        diff = bigB2.leftInplaceSub(bigD);
                        bigD02 = bigD0;
                        i = 1;
                        if (bigIntNBits22 == 1) {
                            if (bigIntExp > -126) {
                                Ulp232--;
                                if (Ulp232 < 0) {
                                    Ulp232 = 0;
                                    i = 1;
                                    diff = diff.leftShift(1);
                                }
                            }
                            i = 1;
                        }
                    }
                    boolean overvalue22 = overvalue;
                    cmpResult = diff.cmpPow52(B5, Ulp232);
                    if (cmpResult >= 0) {
                        int i9 = -1;
                        if (cmpResult != 0) {
                            if (!overvalue22) {
                                i9 = i;
                            }
                            ieeeBits += i9;
                            if (ieeeBits == 0 || ieeeBits == 2139095040) {
                                break;
                            }
                            bigIntNBits = kDigits2;
                            fValue = fValue72;
                            exp = exp62;
                            D5 = D522;
                            iValue = iValue22;
                            dValue = dValue32;
                            bigD0 = bigD02;
                        } else if ((ieeeBits & 1) != 0) {
                            if (!overvalue22) {
                                i9 = i;
                            }
                            ieeeBits += i9;
                        }
                    } else {
                        break;
                    }
                }
                if (this.isNegative) {
                    ieeeBits |= Integer.MIN_VALUE;
                }
                return Float.intBitsToFloat(ieeeBits);
            }
            dValue = dValue2;
            fValue = Math.max(Float.MIN_VALUE, Math.min(Float.MAX_VALUE, (float) dValue));
            if (this.nDigits > 200) {
            }
            FDBigInteger bigD0322 = new FDBigInteger((long) iValue, this.digits, bigIntNBits, this.nDigits);
            exp = this.decExponent - this.nDigits;
            ieeeBits = Float.floatToRawIntBits(fValue);
            B5 = Math.max(0, -exp);
            D5 = Math.max(0, exp);
            bigD0 = bigD0322.multByPow52(D5, 0);
            bigD0.makeImmutable();
            bigD = null;
            while (true) {
                binexp = ieeeBits >>> FloatingDecimal.SINGLE_EXP_SHIFT;
                int bigBbits222 = 8388607 & ieeeBits;
                if (binexp > 0) {
                }
                binexp2 = binexp - 127;
                int lowOrderZeros22 = Integer.numberOfTrailingZeros(bigBbits);
                int bigBbits322 = bigBbits >>> lowOrderZeros22;
                bigIntExp = (binexp2 - 23) + lowOrderZeros22;
                int kDigits22 = bigIntNBits;
                int bigIntNBits222 = 24 - lowOrderZeros22;
                int B232 = B5;
                int D2222 = D5;
                if (bigIntExp >= 0) {
                }
                int Ulp242 = B232;
                float fValue722 = fValue;
                if (binexp2 <= -127) {
                }
                int exp622 = exp;
                int B2222 = B232 + hulpbias;
                int D5222 = D5;
                int D5322 = D2222 + hulpbias;
                int i622 = hulpbias;
                int iValue222 = iValue;
                int Ulp2222 = Ulp242;
                int common222 = Math.min(B2222, Math.min(D5322, Ulp2222));
                D2 = D5322 - common222;
                int Ulp2322 = Ulp2222 - common222;
                int i722 = common222;
                double dValue322 = dValue;
                FDBigInteger bigB22 = FDBigInteger.valueOfMulPow52((long) bigBbits322, B5, B2222 - common222);
                bigD = bigD0.leftShift(D2);
                prevD2 = D2;
                cmp = bigB22.cmp(bigD);
                int cmpResult222 = cmp;
                if (cmp <= 0) {
                }
                boolean overvalue222 = overvalue;
                cmpResult = diff.cmpPow52(B5, Ulp2322);
                if (cmpResult >= 0) {
                }
                bigIntNBits = kDigits22;
                fValue = fValue722;
                exp = exp622;
                D5 = D5222;
                iValue = iValue222;
                dValue = dValue322;
                bigD0 = bigD02;
            }
            if (this.isNegative) {
            }
            return Float.intBitsToFloat(ieeeBits);
        }
    }

    interface ASCIIToBinaryConverter {
        double doubleValue();

        float floatValue();
    }

    static class BinaryToASCIIBuffer implements BinaryToASCIIConverter {
        static final /* synthetic */ boolean $assertionsDisabled = false;
        private static final int[] N_5_BITS = {0, 3, 5, 7, 10, 12, 14, 17, 19, 21, 24, 26, 28, 31, 33, 35, 38, 40, 42, 45, 47, 49, FloatingDecimal.EXP_SHIFT, 54, 56, 59, 61};
        private static int[] insignificantDigitsNumber = {0, 0, 0, 0, 1, 1, 1, 2, 2, 2, 3, 3, 3, 3, 4, 4, 4, 5, 5, 5, 6, 6, 6, 6, 7, 7, 7, 8, 8, 8, 9, 9, 9, 9, 10, 10, 10, 11, 11, 11, 12, 12, 12, 12, 13, 13, 13, 14, 14, 14, 15, 15, 15, 15, 16, 16, 16, 17, 17, 17, 18, 18, 18, 19};
        private final char[] buffer;
        private int decExponent;
        private boolean decimalDigitsRoundedUp;
        private final char[] digits;
        private boolean exactDecimalConversion;
        private int firstDigitIndex;
        private boolean isNegative;
        private int nDigits;

        static {
            Class<FloatingDecimal> cls = FloatingDecimal.class;
        }

        BinaryToASCIIBuffer() {
            this.buffer = new char[26];
            this.exactDecimalConversion = FloatingDecimal.$assertionsDisabled;
            this.decimalDigitsRoundedUp = FloatingDecimal.$assertionsDisabled;
            this.digits = new char[20];
        }

        BinaryToASCIIBuffer(boolean isNegative2, char[] digits2) {
            this.buffer = new char[26];
            this.exactDecimalConversion = FloatingDecimal.$assertionsDisabled;
            this.decimalDigitsRoundedUp = FloatingDecimal.$assertionsDisabled;
            this.isNegative = isNegative2;
            this.decExponent = 0;
            this.digits = digits2;
            this.firstDigitIndex = 0;
            this.nDigits = digits2.length;
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
            }
        }

        public int getDecimalExponent() {
            return this.decExponent;
        }

        public int getDigits(char[] digits2) {
            System.arraycopy((Object) this.digits, this.firstDigitIndex, (Object) digits2, 0, this.nDigits);
            return this.nDigits;
        }

        public boolean isNegative() {
            return this.isNegative;
        }

        public boolean isExceptional() {
            return FloatingDecimal.$assertionsDisabled;
        }

        public boolean digitsRoundedUp() {
            return this.decimalDigitsRoundedUp;
        }

        public boolean decimalDigitsExact() {
            return this.exactDecimalConversion;
        }

        /* access modifiers changed from: private */
        public void setSign(boolean isNegative2) {
            this.isNegative = isNegative2;
        }

        private void developLongDigits(int decExponent2, long lvalue, int insignificantDigits) {
            if (insignificantDigits != 0) {
                long pow10 = FDBigInteger.LONG_5_POW[insignificantDigits] << insignificantDigits;
                long residue = lvalue % pow10;
                lvalue /= pow10;
                decExponent2 += insignificantDigits;
                if (residue >= (pow10 >> 1)) {
                    lvalue++;
                }
            }
            int digitno = this.digits.length - 1;
            if (lvalue <= 2147483647L) {
                int ivalue = (int) lvalue;
                int c = ivalue % 10;
                int ivalue2 = ivalue / 10;
                while (c == 0) {
                    decExponent2++;
                    c = ivalue2 % 10;
                    ivalue2 /= 10;
                }
                while (ivalue2 != 0) {
                    this.digits[digitno] = (char) (c + 48);
                    decExponent2++;
                    c = ivalue2 % 10;
                    ivalue2 /= 10;
                    digitno--;
                }
                this.digits[digitno] = (char) (c + 48);
                int i = c;
            } else {
                int c2 = (int) (lvalue % 10);
                long lvalue2 = lvalue / 10;
                while (c2 == 0) {
                    decExponent2++;
                    c2 = (int) (lvalue2 % 10);
                    lvalue2 /= 10;
                }
                while (lvalue2 != 0) {
                    this.digits[digitno] = (char) (c2 + 48);
                    decExponent2++;
                    c2 = (int) (lvalue2 % 10);
                    lvalue2 /= 10;
                    digitno--;
                }
                this.digits[digitno] = (char) (c2 + 48);
            }
            this.decExponent = decExponent2 + 1;
            this.firstDigitIndex = digitno;
            this.nDigits = this.digits.length - digitno;
        }

        /* access modifiers changed from: private */
        /* JADX WARNING: Removed duplicated region for block: B:154:0x0306  */
        public void dtoa(int binExp, long fractBits, int nSignificantBits, boolean isCompatibleFormat) {
            boolean high;
            boolean low;
            long fractBits2;
            int ndigit;
            int ndigit2;
            boolean high2;
            int ndigit3;
            boolean low2;
            boolean low3;
            boolean low4;
            int ndigit4;
            boolean z;
            boolean high3;
            boolean low5;
            int m;
            boolean high4;
            boolean low6;
            int insignificant;
            long fractBits3;
            int i = binExp;
            long fractBits4 = fractBits;
            int i2 = nSignificantBits;
            int tailZeros = Long.numberOfTrailingZeros(fractBits);
            int ndigit5 = 53 - tailZeros;
            this.decimalDigitsRoundedUp = FloatingDecimal.$assertionsDisabled;
            this.exactDecimalConversion = FloatingDecimal.$assertionsDisabled;
            int nTinyBits = Math.max(0, (ndigit5 - i) - 1);
            if (i > FloatingDecimal.MAX_SMALL_BIN_EXP || i < FloatingDecimal.MIN_SMALL_BIN_EXP || nTinyBits >= FDBigInteger.LONG_5_POW.length || N_5_BITS[nTinyBits] + ndigit5 >= 64 || nTinyBits != 0) {
                int decExp = estimateDecExp(fractBits4, i);
                int B5 = Math.max(0, -decExp);
                int B2 = B5 + nTinyBits + i;
                int S5 = Math.max(0, decExp);
                int S2 = S5 + nTinyBits;
                int M5 = B5;
                int M2 = B2 - i2;
                long fractBits5 = fractBits4 >>> tailZeros;
                int B22 = B2 - (ndigit5 - 1);
                int common2factor = Math.min(B22, S2);
                int B23 = B22 - common2factor;
                int S22 = S2 - common2factor;
                int M22 = M2 - common2factor;
                if (ndigit5 == 1) {
                    M22--;
                }
                if (M22 < 0) {
                    B23 -= M22;
                    S22 -= M22;
                    M22 = 0;
                }
                int Bbits = ndigit5 + B23 + (B5 < N_5_BITS.length ? N_5_BITS[B5] : B5 * 3);
                int tenSbits = S22 + 1 + (S5 + 1 < N_5_BITS.length ? N_5_BITS[S5 + 1] : (S5 + 1) * 3);
                if (Bbits >= 64 || tenSbits >= 64) {
                    int i3 = tailZeros;
                    int i4 = ndigit5;
                    int i5 = nTinyBits;
                    int i6 = Bbits;
                    FDBigInteger Sval = FDBigInteger.valueOfPow52(S5, S22);
                    int shiftBias = Sval.getNormalizationBias();
                    FDBigInteger Sval2 = Sval.leftShift(shiftBias);
                    FDBigInteger Bval = FDBigInteger.valueOfMulPow52(fractBits5, B5, B23 + shiftBias);
                    FDBigInteger Mval = FDBigInteger.valueOfPow52(M5 + 1, M22 + shiftBias + 1);
                    FDBigInteger tenSval = FDBigInteger.valueOfPow52(S5 + 1, S22 + shiftBias + 1);
                    int ndigit6 = 0;
                    int q = Bval.quoRemIteration(Sval2);
                    boolean low7 = Bval.cmp(Mval) < 0 ? true : FloatingDecimal.$assertionsDisabled;
                    boolean high5 = tenSval.addAndCmp(Bval, Mval) <= 0 ? true : FloatingDecimal.$assertionsDisabled;
                    if (q != 0 || high5) {
                        long j = fractBits5;
                        this.digits[0] = (char) (48 + q);
                        ndigit6 = 0 + 1;
                    } else {
                        decExp--;
                        long j2 = fractBits5;
                    }
                    if (!isCompatibleFormat || decExp < -3 || decExp >= 8) {
                        low2 = false;
                        ndigit3 = ndigit6;
                        int i7 = q;
                        high2 = false;
                    } else {
                        ndigit3 = ndigit6;
                        int i8 = q;
                        low2 = low7;
                        high2 = high5;
                    }
                    while (!low && !high) {
                        int q2 = Bval.quoRemIteration(Sval2);
                        Mval = Mval.multBy10();
                        low2 = Bval.cmp(Mval) < 0 ? true : FloatingDecimal.$assertionsDisabled;
                        high2 = tenSval.addAndCmp(Bval, Mval) <= 0 ? true : FloatingDecimal.$assertionsDisabled;
                        this.digits[ndigit2] = (char) (48 + q2);
                        ndigit3 = ndigit2 + 1;
                    }
                    if (!high || !low) {
                        fractBits2 = 0;
                    } else {
                        Bval = Bval.leftShift(1);
                        fractBits2 = (long) Bval.cmp(tenSval);
                    }
                    this.exactDecimalConversion = Bval.cmp(FDBigInteger.ZERO) == 0 ? true : FloatingDecimal.$assertionsDisabled;
                } else if (Bbits >= 32 || tenSbits >= 32) {
                    int i9 = tailZeros;
                    int i10 = ndigit5;
                    long b = (FDBigInteger.LONG_5_POW[B5] * fractBits5) << B23;
                    long s = FDBigInteger.LONG_5_POW[S5] << S22;
                    long tens = s * 10;
                    int ndigit7 = 0;
                    int i11 = nTinyBits;
                    int i12 = Bbits;
                    int q3 = (int) (b / s);
                    long b2 = (b % s) * 10;
                    long m2 = (FDBigInteger.LONG_5_POW[M5] << M22) * 10;
                    boolean low8 = b2 < m2 ? true : FloatingDecimal.$assertionsDisabled;
                    boolean high6 = b2 + m2 > tens ? true : FloatingDecimal.$assertionsDisabled;
                    if (q3 != 0 || high6) {
                        low3 = low8;
                        this.digits[0] = (char) (48 + q3);
                        ndigit7 = 0 + 1;
                    } else {
                        decExp--;
                        low3 = low8;
                    }
                    if (!isCompatibleFormat || decExp < -3 || decExp >= 8) {
                        high6 = false;
                        low4 = false;
                    } else {
                        low4 = low3;
                    }
                    boolean high7 = high6;
                    long m3 = m2;
                    while (!low && !high) {
                        int decExp2 = decExp;
                        q3 = (int) (b2 / s);
                        b2 = 10 * (b2 % s);
                        long m4 = m3 * 10;
                        if (m4 > 0) {
                            low5 = b2 < m4 ? true : FloatingDecimal.$assertionsDisabled;
                            high3 = b2 + m4 > tens ? true : FloatingDecimal.$assertionsDisabled;
                        } else {
                            low5 = true;
                            high3 = true;
                        }
                        low4 = low5;
                        high7 = high3;
                        this.digits[ndigit7] = (char) (48 + q3);
                        ndigit7++;
                        decExp = decExp2;
                        m3 = m4;
                    }
                    int decExp3 = decExp;
                    long lowDigitDifference = (b2 << 1) - tens;
                    if (b2 == 0) {
                        ndigit4 = ndigit7;
                        z = true;
                    } else {
                        ndigit4 = ndigit7;
                        z = FloatingDecimal.$assertionsDisabled;
                    }
                    this.exactDecimalConversion = z;
                    long j3 = fractBits5;
                    int i13 = q3;
                    fractBits2 = lowDigitDifference;
                    decExp = decExp3;
                    ndigit = ndigit4;
                    this.decExponent = decExp + 1;
                    this.firstDigitIndex = 0;
                    this.nDigits = ndigit;
                    if (high) {
                        if (!low) {
                            roundup();
                        } else if (fractBits2 == 0) {
                            if ((this.digits[(this.firstDigitIndex + this.nDigits) - 1] & 1) != 0) {
                                roundup();
                            }
                        } else if (fractBits2 > 0) {
                            roundup();
                        }
                    }
                    return;
                } else {
                    int b3 = (((int) fractBits5) * FDBigInteger.SMALL_5_POW[B5]) << B23;
                    int s2 = FDBigInteger.SMALL_5_POW[S5] << S22;
                    int i14 = tenSbits;
                    int tens2 = s2 * 10;
                    int q4 = b3 / s2;
                    int b4 = 10 * (b3 % s2);
                    int m5 = (FDBigInteger.SMALL_5_POW[M5] << M22) * 10;
                    low = b4 < m5 ? true : FloatingDecimal.$assertionsDisabled;
                    int i15 = tailZeros;
                    boolean high8 = b4 + m5 > tens2 ? true : FloatingDecimal.$assertionsDisabled;
                    if (q4 != 0 || high8) {
                        m = m5;
                        ndigit2 = 0 + 1;
                        high = high8;
                        this.digits[0] = (char) (48 + q4);
                    } else {
                        decExp--;
                        m = m5;
                        high = high8;
                        ndigit2 = 0;
                    }
                    if (!isCompatibleFormat || decExp < -3 || decExp >= 8) {
                        low = false;
                        high = false;
                    }
                    while (!low && !high) {
                        int q5 = b4 / s2;
                        b4 = 10 * (b4 % s2);
                        int m6 = m * 10;
                        int nFractBits = ndigit5;
                        if (((long) m6) > 0) {
                            low6 = b4 < m6 ? true : FloatingDecimal.$assertionsDisabled;
                            high4 = b4 + m6 > tens2 ? true : FloatingDecimal.$assertionsDisabled;
                        } else {
                            low6 = true;
                            high4 = true;
                        }
                        low = low6;
                        high = high4;
                        this.digits[ndigit2] = (char) (48 + q5);
                        ndigit2++;
                        ndigit5 = nFractBits;
                        m = m6;
                    }
                    int nFractBits2 = ndigit5;
                    long lowDigitDifference2 = (long) ((b4 << 1) - tens2);
                    this.exactDecimalConversion = b4 == 0 ? true : FloatingDecimal.$assertionsDisabled;
                    long j4 = fractBits5;
                    fractBits2 = lowDigitDifference2;
                    int i16 = nTinyBits;
                    int i17 = Bbits;
                }
                ndigit = ndigit2;
                this.decExponent = decExp + 1;
                this.firstDigitIndex = 0;
                this.nDigits = ndigit;
                if (high) {
                }
                return;
            }
            if (i > i2) {
                insignificant = insignificantDigitsForPow2((i - i2) - 1);
            } else {
                insignificant = 0;
            }
            if (i >= FloatingDecimal.EXP_SHIFT) {
                fractBits3 = fractBits4 << (i - 52);
            } else {
                fractBits3 = fractBits4 >>> (FloatingDecimal.EXP_SHIFT - i);
            }
            developLongDigits(0, fractBits3, insignificant);
        }

        /* JADX WARNING: Incorrect type for immutable var: ssa=char, code=int, for r2v1, types: [char] */
        /* JADX WARNING: Incorrect type for immutable var: ssa=char, code=int, for r2v4, types: [char] */
        private void roundup() {
            int i = (this.firstDigitIndex + this.nDigits) - 1;
            int q = this.digits[i];
            if (q == '9') {
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
            double d = ((Double.longBitsToDouble((fractBits & DoubleConsts.SIGNIF_BIT_MASK) | FloatingDecimal.EXP_ONE) - 1.5d) * 0.289529654d) + 0.176091259d + (((double) binExp) * 0.301029995663981d);
            long dBits = Double.doubleToRawLongBits(d);
            int exponent = ((int) ((DoubleConsts.EXP_BIT_MASK & dBits) >> 52)) - 1023;
            int i = 0;
            boolean isNegative2 = (Long.MIN_VALUE & dBits) != 0;
            if (exponent >= 0 && exponent < FloatingDecimal.EXP_SHIFT) {
                long mask = DoubleConsts.SIGNIF_BIT_MASK >> exponent;
                int r = (int) (((DoubleConsts.SIGNIF_BIT_MASK & dBits) | FloatingDecimal.FRACT_HOB) >> (FloatingDecimal.EXP_SHIFT - exponent));
                return isNegative2 ? (mask & dBits) == 0 ? -r : (-r) - 1 : r;
            } else if (exponent >= 0) {
                return (int) d;
            } else {
                if ((Long.MAX_VALUE & dBits) != 0 && isNegative2) {
                    i = -1;
                }
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
            int i;
            int i2;
            int i3;
            int i4 = 0;
            if (this.isNegative) {
                result[0] = '-';
                i4 = 1;
            }
            if (this.decExponent > 0 && this.decExponent < 8) {
                int charLength = Math.min(this.nDigits, this.decExponent);
                System.arraycopy((Object) this.digits, this.firstDigitIndex, (Object) result, i4, charLength);
                int i5 = i4 + charLength;
                if (charLength < this.decExponent) {
                    int charLength2 = this.decExponent - charLength;
                    Arrays.fill(result, i5, i5 + charLength2, '0');
                    int i6 = i5 + charLength2;
                    int i7 = i6 + 1;
                    result[i6] = '.';
                    int i8 = i7 + 1;
                    result[i7] = '0';
                    return i8;
                }
                int charLength3 = i5 + 1;
                result[i5] = '.';
                if (charLength < this.nDigits) {
                    int t = this.nDigits - charLength;
                    System.arraycopy((Object) this.digits, this.firstDigitIndex + charLength, (Object) result, charLength3, t);
                    return t + charLength3;
                }
                int i9 = charLength3 + 1;
                result[charLength3] = '0';
                return i9;
            } else if (this.decExponent > 0 || this.decExponent <= -3) {
                int i10 = i4 + 1;
                result[i4] = this.digits[this.firstDigitIndex];
                int i11 = i10 + 1;
                result[i10] = '.';
                if (this.nDigits > 1) {
                    System.arraycopy((Object) this.digits, this.firstDigitIndex + 1, (Object) result, i11, this.nDigits - 1);
                    i = i11 + (this.nDigits - 1);
                } else {
                    result[i11] = '0';
                    i = i11 + 1;
                }
                int e = i + 1;
                result[i] = 'E';
                if (this.decExponent <= 0) {
                    result[e] = '-';
                    e++;
                    i2 = (-this.decExponent) + 1;
                } else {
                    i2 = this.decExponent - 1;
                }
                if (i2 <= 9) {
                    i3 = e + 1;
                    result[e] = (char) (i2 + 48);
                } else if (i2 <= 99) {
                    int i12 = e + 1;
                    result[e] = (char) ((i2 / 10) + 48);
                    result[i12] = (char) ((i2 % 10) + 48);
                    return i12 + 1;
                } else {
                    int i13 = e + 1;
                    result[e] = (char) ((i2 / 100) + 48);
                    int e2 = i2 % 100;
                    int i14 = i13 + 1;
                    result[i13] = (char) ((e2 / 10) + 48);
                    i3 = i14 + 1;
                    result[i14] = (char) ((e2 % 10) + 48);
                }
                return i3;
            } else {
                int i15 = i4 + 1;
                result[i4] = '0';
                int i16 = i15 + 1;
                result[i15] = '.';
                if (this.decExponent != 0) {
                    Arrays.fill(result, i16, i16 - this.decExponent, '0');
                    i16 -= this.decExponent;
                }
                System.arraycopy((Object) this.digits, this.firstDigitIndex, (Object) result, i16, this.nDigits);
                return i16 + this.nDigits;
            }
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

    private static class ExceptionalBinaryToASCIIBuffer implements BinaryToASCIIConverter {
        static final /* synthetic */ boolean $assertionsDisabled = false;
        private final String image;
        private boolean isNegative;

        static {
            Class<FloatingDecimal> cls = FloatingDecimal.class;
        }

        public ExceptionalBinaryToASCIIBuffer(String image2, boolean isNegative2) {
            this.image = image2;
            this.isNegative = isNegative2;
        }

        public String toJavaFormatString() {
            return this.image;
        }

        public void appendTo(Appendable buf) {
            if (buf instanceof StringBuilder) {
                ((StringBuilder) buf).append(this.image);
            } else if (buf instanceof StringBuffer) {
                ((StringBuffer) buf).append(this.image);
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
        /* access modifiers changed from: private */
        public static final Pattern VALUE = Pattern.compile("([-+])?0[xX](((\\p{XDigit}+)\\.?)|((\\p{XDigit}*)\\.(\\p{XDigit}+)))[pP]([-+])?(\\p{Digit}+)[fFdD]?");

        private HexFloatPattern() {
        }
    }

    static class PreparedASCIIToBinaryBuffer implements ASCIIToBinaryConverter {
        private final double doubleVal;
        private final float floatVal;

        public PreparedASCIIToBinaryBuffer(double doubleVal2, float floatVal2) {
            this.doubleVal = doubleVal2;
            this.floatVal = floatVal2;
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
        return threadLocalBinaryToASCIIBuffer.get();
    }

    public static BinaryToASCIIConverter getBinaryToASCIIConverter(double d) {
        return getBinaryToASCIIConverter(d, true);
    }

    static BinaryToASCIIConverter getBinaryToASCIIConverter(double d, boolean isCompatibleFormat) {
        long fractBits;
        int nSignificantBits;
        long dBits = Double.doubleToRawLongBits(d);
        boolean isNegative = (Long.MIN_VALUE & dBits) != 0 ? true : $assertionsDisabled;
        long fractBits2 = DoubleConsts.SIGNIF_BIT_MASK & dBits;
        int binExp = (int) ((DoubleConsts.EXP_BIT_MASK & dBits) >> 52);
        if (binExp != 2047) {
            if (binExp != 0) {
                fractBits = fractBits2 | FRACT_HOB;
                nSignificantBits = 53;
            } else if (fractBits2 == 0) {
                return isNegative ? B2AC_NEGATIVE_ZERO : B2AC_POSITIVE_ZERO;
            } else {
                int leadingZeros = Long.numberOfLeadingZeros(fractBits2);
                int shift = leadingZeros - 11;
                fractBits = fractBits2 << shift;
                binExp = 1 - shift;
                nSignificantBits = 64 - leadingZeros;
            }
            BinaryToASCIIBuffer buf = getBinaryToASCIIBuffer();
            buf.setSign(isNegative);
            buf.dtoa(binExp - 1023, fractBits, nSignificantBits, isCompatibleFormat);
            return buf;
        } else if (fractBits2 != 0) {
            return B2AC_NOT_A_NUMBER;
        } else {
            return isNegative ? B2AC_NEGATIVE_INFINITY : B2AC_POSITIVE_INFINITY;
        }
    }

    private static BinaryToASCIIConverter getBinaryToASCIIConverter(float f) {
        int fractBits;
        int i;
        int fBits = Float.floatToRawIntBits(f);
        boolean isNegative = (Integer.MIN_VALUE & fBits) != 0 ? true : $assertionsDisabled;
        int fractBits2 = 8388607 & fBits;
        int binExp = (2139095040 & fBits) >> SINGLE_EXP_SHIFT;
        if (binExp != 255) {
            if (binExp != 0) {
                fractBits = fractBits2 | SINGLE_FRACT_HOB;
                i = 24;
            } else if (fractBits2 == 0) {
                return isNegative ? B2AC_NEGATIVE_ZERO : B2AC_POSITIVE_ZERO;
            } else {
                int leadingZeros = Integer.numberOfLeadingZeros(fractBits2);
                int shift = leadingZeros - 8;
                fractBits = fractBits2 << shift;
                binExp = 1 - shift;
                i = 32 - leadingZeros;
            }
            int nSignificantBits = i;
            int binExp2 = binExp - 127;
            BinaryToASCIIBuffer buf = getBinaryToASCIIBuffer();
            buf.setSign(isNegative);
            buf.dtoa(binExp2, ((long) fractBits) << 29, nSignificantBits, true);
            return buf;
        } else if (((long) fractBits2) != 0) {
            return B2AC_NOT_A_NUMBER;
        } else {
            return isNegative ? B2AC_NEGATIVE_INFINITY : B2AC_POSITIVE_INFINITY;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:111:0x0130 A[Catch:{ StringIndexOutOfBoundsException -> 0x016a }] */
    /* JADX WARNING: Removed duplicated region for block: B:121:0x015b A[Catch:{ StringIndexOutOfBoundsException -> 0x016a }] */
    /* JADX WARNING: Removed duplicated region for block: B:126:0x0167 A[Catch:{ StringIndexOutOfBoundsException -> 0x016a }] */
    /* JADX WARNING: Removed duplicated region for block: B:127:0x0169 A[Catch:{ StringIndexOutOfBoundsException -> 0x016a }] */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x0027  */
    /* JADX WARNING: Removed duplicated region for block: B:174:0x0151 A[EDGE_INSN: B:174:0x0151->B:119:0x0151 ?: BREAK  , SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x003d A[Catch:{ StringIndexOutOfBoundsException -> 0x01b7 }] */
    static ASCIIToBinaryConverter readJavaFormatString(String in) throws NumberFormatException {
        String in2;
        char c;
        boolean signSeen;
        int decExp;
        int i;
        int expVal;
        int decExp2;
        int i2;
        char c2;
        int expSign;
        int nDigits;
        boolean isNegative = $assertionsDisabled;
        boolean signSeen2 = false;
        try {
            in2 = in.trim();
            try {
                int len = in2.length();
                if (len != 0) {
                    int i3 = 0;
                    char charAt = in2.charAt(0);
                    if (charAt != '+') {
                        if (charAt == '-') {
                            isNegative = true;
                        }
                        c = in2.charAt(i3);
                        if (c != 'N') {
                            if (len - i3 == NAN_LENGTH && in2.indexOf(NAN_REP, i3) == i3) {
                                return A2BC_NOT_A_NUMBER;
                            }
                        } else if (c != 'I') {
                            if (c == '0' && len > i3 + 1) {
                                char ch = in2.charAt(i3 + 1);
                                if (ch == 'x' || ch == 'X') {
                                    return parseHexString(in2);
                                }
                            }
                            char[] digits = new char[len];
                            int nDigits2 = 0;
                            int nLeadZero = 0;
                            int decPt = 0;
                            boolean decSeen = false;
                            char c3 = c;
                            int i4 = i3;
                            int nTrailZero = 0;
                            while (i4 < len) {
                                char c4 = in2.charAt(i4);
                                if (c4 != '0') {
                                    if (c4 != '.') {
                                        break;
                                    } else if (!decSeen) {
                                        int decPt2 = i4;
                                        if (signSeen2) {
                                            decPt2--;
                                        }
                                        decSeen = true;
                                        decPt = decPt2;
                                    } else {
                                        throw new NumberFormatException("multiple points");
                                    }
                                } else {
                                    nLeadZero++;
                                }
                                i4++;
                            }
                            while (i4 < len) {
                                char c5 = in2.charAt(i4);
                                if (c5 < '1' || c5 > '9') {
                                    if (c5 != '0') {
                                        if (c5 != '.') {
                                            break;
                                        } else if (!decSeen) {
                                            int decPt3 = i4;
                                            if (signSeen2) {
                                                decPt3--;
                                            }
                                            decPt = decPt3;
                                            decSeen = true;
                                            i4++;
                                        } else {
                                            throw new NumberFormatException("multiple points");
                                        }
                                    } else {
                                        nDigits = nDigits2 + 1;
                                        digits[nDigits2] = c5;
                                        nTrailZero++;
                                    }
                                } else {
                                    nDigits = nDigits2 + 1;
                                    digits[nDigits2] = c5;
                                    nTrailZero = 0;
                                }
                                nDigits2 = nDigits;
                                i4++;
                            }
                            int nDigits3 = nDigits2 - nTrailZero;
                            boolean isZero = nDigits3 == 0 ? true : $assertionsDisabled;
                            if (!isZero || nLeadZero != 0) {
                                if (decSeen) {
                                    decExp = decPt - nLeadZero;
                                } else {
                                    decExp = nDigits3 + nTrailZero;
                                }
                                if (i4 < len) {
                                    char charAt2 = in2.charAt(i4);
                                    char c6 = charAt2;
                                    if (charAt2 == 'e' || c6 == 'E') {
                                        signSeen = signSeen2;
                                        boolean expOverflow = $assertionsDisabled;
                                        int i5 = i4 + 1;
                                        int expSign2 = 1;
                                        try {
                                            int expSign3 = in2.charAt(i5);
                                            if (expSign3 == 43) {
                                                expSign = 1;
                                            } else if (expSign3 != 45) {
                                                i = i5;
                                                expVal = 0;
                                                while (true) {
                                                    if (i >= len) {
                                                        break;
                                                    }
                                                    if (expVal >= 214748364) {
                                                        expOverflow = true;
                                                    }
                                                    i2 = i + 1;
                                                    c2 = in2.charAt(i);
                                                    if (c2 < '0' || c2 > '9') {
                                                        i = i2 - 1;
                                                    } else {
                                                        expVal = (expVal * 10) + (c2 - '0');
                                                        i = i2;
                                                    }
                                                }
                                                int expLimit = BIG_DECIMAL_EXPONENT + nDigits3 + nTrailZero;
                                                if (!expOverflow) {
                                                    if (expVal <= expLimit) {
                                                        decExp2 = decExp + (expSign2 * expVal);
                                                        if (i != i5) {
                                                            i4 = i;
                                                        } else {
                                                            throw new NumberFormatException("For input string: \"" + in2 + "\"");
                                                        }
                                                    }
                                                }
                                                decExp2 = expSign2 * expLimit;
                                                if (i != i5) {
                                                }
                                            } else {
                                                expSign = -1;
                                            }
                                            i5++;
                                            expSign2 = expSign;
                                            i = i5;
                                            expVal = 0;
                                            while (true) {
                                                if (i >= len) {
                                                }
                                                expVal = (expVal * 10) + (c2 - '0');
                                                i = i2;
                                            }
                                            int expLimit2 = BIG_DECIMAL_EXPONENT + nDigits3 + nTrailZero;
                                            if (!expOverflow) {
                                            }
                                            decExp2 = expSign2 * expLimit2;
                                            if (i != i5) {
                                            }
                                        } catch (StringIndexOutOfBoundsException e) {
                                            boolean z = signSeen;
                                        }
                                    } else {
                                        signSeen = signSeen2;
                                    }
                                } else {
                                    signSeen = signSeen2;
                                }
                                if (i4 < len) {
                                    if (i4 == len - 1) {
                                        if (!(in2.charAt(i4) == 'f' || in2.charAt(i4) == 'F' || in2.charAt(i4) == 'd')) {
                                            if (in2.charAt(i4) == 'D') {
                                            }
                                        }
                                    }
                                    throw new NumberFormatException("For input string: \"" + in2 + "\"");
                                }
                                if (!isZero) {
                                    return new ASCIIToBinaryBuffer(isNegative, decExp, digits, nDigits3);
                                }
                                return isNegative ? A2BC_NEGATIVE_ZERO : A2BC_POSITIVE_ZERO;
                            }
                        } else if (len - i3 == INFINITY_LENGTH && in2.indexOf(INFINITY_REP, i3) == i3) {
                            return isNegative ? A2BC_NEGATIVE_INFINITY : A2BC_POSITIVE_INFINITY;
                        }
                        signSeen = signSeen2;
                        throw new NumberFormatException("For input string: \"" + in2 + "\"");
                    }
                    i3 = 0 + 1;
                    signSeen2 = true;
                    try {
                        c = in2.charAt(i3);
                        if (c != 'N') {
                        }
                        signSeen = signSeen2;
                    } catch (StringIndexOutOfBoundsException e2) {
                        boolean z2 = signSeen2;
                    }
                    throw new NumberFormatException("For input string: \"" + in2 + "\"");
                }
                throw new NumberFormatException("empty String");
            } catch (StringIndexOutOfBoundsException e3) {
            }
        } catch (StringIndexOutOfBoundsException e4) {
            in2 = in;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:137:0x0274  */
    /* JADX WARNING: Removed duplicated region for block: B:141:0x027c  */
    static ASCIIToBinaryConverter parseHexString(String s) {
        int leftDigits;
        String significandString;
        int exponentAdjust;
        ASCIIToBinaryConverter aSCIIToBinaryConverter;
        long significand;
        int nextShift;
        int i;
        boolean sticky;
        long significand2;
        long significand3;
        double value;
        int bitsDiscarded;
        Matcher m = HexFloatPattern.VALUE.matcher(s);
        boolean validInput = m.matches();
        if (validInput) {
            String group1 = m.group(1);
            boolean isNegative = (group1 == null || !group1.equals(LanguageTag.SEP)) ? $assertionsDisabled : true;
            int rightDigits = 0;
            String group = m.group(4);
            String group4 = group;
            if (group != null) {
                significandString = stripLeadingZeros(group4);
                leftDigits = significandString.length();
            } else {
                String group6 = stripLeadingZeros(m.group(6));
                leftDigits = group6.length();
                String group7 = m.group(7);
                rightDigits = group7.length();
                StringBuilder sb = new StringBuilder();
                sb.append(group6 == null ? "" : group6);
                sb.append(group7);
                significandString = sb.toString();
            }
            String significandString2 = stripLeadingZeros(significandString);
            int signifLength = significandString2.length();
            if (leftDigits >= 1) {
                exponentAdjust = 4 * (leftDigits - 1);
            } else {
                exponentAdjust = -4 * ((rightDigits - signifLength) + 1);
            }
            if (signifLength == 0) {
                return isNegative ? A2BC_NEGATIVE_ZERO : A2BC_POSITIVE_ZERO;
            }
            String group8 = m.group(8);
            boolean positiveExponent = (group8 == null || group8.equals("+")) ? true : $assertionsDisabled;
            try {
                long exponent = ((positiveExponent ? 1 : -1) * ((long) Integer.parseInt(m.group(9)))) + ((long) exponentAdjust);
                boolean round = $assertionsDisabled;
                boolean sticky2 = $assertionsDisabled;
                Matcher matcher = m;
                long leadingDigit = (long) getHexDigit(significandString2, 0);
                if (leadingDigit == 1) {
                    significand = 0 | (leadingDigit << 52);
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
                    boolean z = validInput;
                    String str = group1;
                    String str2 = significandString2;
                    throw new AssertionError((Object) "Result from digit conversion too large!");
                }
                int nextShift2 = nextShift;
                int i2 = 1;
                while (i < signifLength && nextShift2 >= 0) {
                    significand |= ((long) getHexDigit(significandString2, i)) << nextShift2;
                    nextShift2 -= 4;
                    i2 = i + 1;
                    leadingDigit = leadingDigit;
                }
                if (i < signifLength) {
                    long currentDigit = (long) getHexDigit(significandString2, i);
                    switch (nextShift2) {
                        case -4:
                            round = (currentDigit & 8) != 0 ? true : $assertionsDisabled;
                            sticky2 = (currentDigit & 7) != 0 ? true : $assertionsDisabled;
                            break;
                        case -3:
                            significand |= (currentDigit & 8) >> 3;
                            round = (currentDigit & 4) != 0 ? true : $assertionsDisabled;
                            sticky2 = (currentDigit & 3) != 0 ? true : $assertionsDisabled;
                            break;
                        case -2:
                            significand |= (currentDigit & 12) >> 2;
                            round = (currentDigit & 2) != 0 ? true : $assertionsDisabled;
                            sticky2 = (currentDigit & 1) != 0 ? true : $assertionsDisabled;
                            break;
                        case -1:
                            significand |= (currentDigit & 14) >> 1;
                            round = (currentDigit & 1) != 0 ? true : $assertionsDisabled;
                            break;
                        default:
                            long j = currentDigit;
                            throw new AssertionError((Object) "Unexpected shift distance remainder.");
                    }
                    i++;
                    while (i < signifLength && !sticky2) {
                        long j2 = currentDigit;
                        currentDigit = (long) getHexDigit(significandString2, i);
                        sticky2 = (sticky2 || currentDigit != 0) ? true : $assertionsDisabled;
                        i++;
                    }
                }
                int floatBits = isNegative ? Integer.MIN_VALUE : 0;
                if (exponent < -126) {
                    boolean z2 = validInput;
                    if (exponent >= -150) {
                        int threshShift = (int) (-98 - exponent);
                        boolean floatSticky = ((significand & ((1 << threshShift) - 1)) != 0 || round || sticky) ? true : $assertionsDisabled;
                        String str3 = group1;
                        int iValue = (int) (significand >>> threshShift);
                        int i3 = threshShift;
                        if ((iValue & 3) != 1 || floatSticky) {
                            iValue++;
                        }
                        floatBits |= iValue >> 1;
                        float fValue = Float.intBitsToFloat(floatBits);
                        if (exponent <= 1023) {
                            return isNegative ? A2BC_NEGATIVE_INFINITY : A2BC_POSITIVE_INFINITY;
                        }
                        if (exponent <= 1023 && exponent >= -1022) {
                            significand3 = (((1023 + exponent) << 52) & DoubleConsts.EXP_BIT_MASK) | (DoubleConsts.SIGNIF_BIT_MASK & significand);
                            significand2 = 0;
                        } else if (exponent < -1075) {
                            return isNegative ? A2BC_NEGATIVE_ZERO : A2BC_POSITIVE_ZERO;
                        } else {
                            boolean sticky3 = (sticky || round) ? true : $assertionsDisabled;
                            int bitsDiscarded2 = 53 - ((((int) exponent) + 1074) + 1);
                            round = (significand & (1 << (bitsDiscarded2 + -1))) != 0 ? true : $assertionsDisabled;
                            if (bitsDiscarded2 > 1) {
                                bitsDiscarded = bitsDiscarded2;
                                sticky = (sticky3 || (significand & (~(-1 << (bitsDiscarded2 + -1)))) != 0) ? true : $assertionsDisabled;
                            } else {
                                bitsDiscarded = bitsDiscarded2;
                                sticky = sticky3;
                            }
                            significand2 = 0;
                            significand3 = 0 | (DoubleConsts.SIGNIF_BIT_MASK & (significand >> bitsDiscarded));
                        }
                        boolean leastZero = (significand3 & 1) == significand2 ? true : $assertionsDisabled;
                        if ((leastZero && round && sticky) || (!leastZero && round)) {
                            significand3++;
                        }
                        if (isNegative) {
                            boolean z3 = leastZero;
                            String str4 = significandString2;
                            value = Double.longBitsToDouble(significand3 | Long.MIN_VALUE);
                        } else {
                            String str5 = significandString2;
                            value = Double.longBitsToDouble(significand3);
                        }
                        int i4 = floatBits;
                        return new PreparedASCIIToBinaryBuffer(value, fValue);
                    }
                } else if (exponent > 127) {
                    floatBits |= FloatConsts.EXP_BIT_MASK;
                    int i5 = i;
                    boolean z4 = validInput;
                } else {
                    boolean floatSticky2 = ((significand & ((1 << 28) - 1)) != 0 || round || sticky) ? true : $assertionsDisabled;
                    int i6 = i;
                    boolean z5 = validInput;
                    int iValue2 = (int) (significand >>> 28);
                    if ((iValue2 & 3) != 1 || floatSticky2) {
                        iValue2++;
                    }
                    floatBits |= ((((int) exponent) + 126) << SINGLE_EXP_SHIFT) + (iValue2 >> 1);
                }
                float fValue2 = Float.intBitsToFloat(floatBits);
                if (exponent <= 1023) {
                }
            } catch (NumberFormatException e) {
                Matcher matcher2 = m;
                boolean z6 = validInput;
                String str6 = group1;
                String str7 = significandString2;
                if (isNegative) {
                    aSCIIToBinaryConverter = positiveExponent ? A2BC_NEGATIVE_INFINITY : A2BC_NEGATIVE_ZERO;
                } else {
                    aSCIIToBinaryConverter = positiveExponent ? A2BC_POSITIVE_INFINITY : A2BC_POSITIVE_ZERO;
                }
                return aSCIIToBinaryConverter;
            }
        } else {
            boolean z7 = validInput;
            throw new NumberFormatException("For input string: \"" + s + "\"");
        }
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
        throw new AssertionError((Object) "Unexpected failure of digit conversion of " + s.charAt(position));
    }
}
