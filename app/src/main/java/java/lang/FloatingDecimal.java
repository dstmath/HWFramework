package java.lang;

import java.util.regex.Pattern;
import sun.misc.DoubleConsts;
import sun.misc.FDBigInt;
import sun.misc.FloatConsts;

public class FloatingDecimal {
    static final /* synthetic */ boolean -assertionsDisabled = false;
    private static final ThreadLocal<FloatingDecimal> TL_INSTANCE = null;
    private static FDBigInt[] b5p = null;
    private static final double[] big10pow = null;
    static final int bigDecimalExponent = 324;
    static final int expBias = 1023;
    static final long expMask = 9218868437227405312L;
    static final long expOne = 4607182418800017408L;
    static final int expShift = 52;
    static final long fractHOB = 4503599627370496L;
    static final long fractMask = 4503599627370495L;
    private static Pattern hexFloatPattern = null;
    static final long highbit = Long.MIN_VALUE;
    static final long highbyte = -72057594037927936L;
    private static final char[] infinity = null;
    static final int intDecimalDigits = 9;
    private static final long[] long5pow = null;
    static final long lowbytes = 72057594037927935L;
    static final int maxDecimalDigits = 15;
    static final int maxDecimalExponent = 308;
    static final int maxSmallBinExp = 62;
    private static final int maxSmallTen = 0;
    static final int minDecimalExponent = -324;
    static final int minSmallBinExp = -21;
    private static final int[] n5bits = null;
    private static final char[] notANumber = null;
    private static ThreadLocal perThreadBuffer = null;
    static final long signMask = Long.MIN_VALUE;
    static final int singleExpBias = 127;
    static final int singleExpMask = 2139095040;
    static final int singleExpShift = 23;
    static final int singleFractHOB = 8388608;
    static final int singleFractMask = 8388607;
    static final int singleMaxDecimalDigits = 7;
    static final int singleMaxDecimalExponent = 38;
    private static final int singleMaxSmallTen = 0;
    static final int singleMinDecimalExponent = -45;
    static final int singleSignMask = Integer.MIN_VALUE;
    private static final float[] singleSmall10pow = null;
    private static final double[] small10pow = null;
    private static final int[] small5pow = null;
    private static final double[] tiny10pow = null;
    private static final char[] zero = null;
    int bigIntExp;
    int bigIntNBits;
    int decExponent;
    char[] digits;
    boolean fromHex;
    boolean isExceptional;
    boolean isNegative;
    boolean mustSetRoundDir;
    int nDigits;
    int roundDir;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.lang.FloatingDecimal.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.lang.FloatingDecimal.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: java.lang.FloatingDecimal.<clinit>():void");
    }

    java.lang.FloatingDecimal parseHexString(java.lang.String r1) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.lang.FloatingDecimal.parseHexString(java.lang.String):java.lang.FloatingDecimal
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
        throw new UnsupportedOperationException("Method not decompiled: java.lang.FloatingDecimal.parseHexString(java.lang.String):java.lang.FloatingDecimal");
    }

    private static int countBits(long v) {
        if (v == 0) {
            return singleMaxSmallTen;
        }
        while ((highbyte & v) == 0) {
            v <<= 8;
        }
        while (v > 0) {
            v <<= 1;
        }
        int n = singleMaxSmallTen;
        while ((lowbytes & v) != 0) {
            v <<= 8;
            n += 8;
        }
        while (v != 0) {
            v <<= 1;
            n++;
        }
        return n;
    }

    private static synchronized FDBigInt big5pow(int p) {
        Object obj = null;
        synchronized (FloatingDecimal.class) {
            if (!-assertionsDisabled) {
                if (p >= 0) {
                    obj = 1;
                }
                if (obj == null) {
                    throw new AssertionError(Integer.valueOf(p));
                }
            }
            if (b5p == null) {
                b5p = new FDBigInt[(p + 1)];
            } else if (b5p.length <= p) {
                Object t = new FDBigInt[(p + 1)];
                System.arraycopy(b5p, (int) singleMaxSmallTen, t, (int) singleMaxSmallTen, b5p.length);
                b5p = t;
            }
            FDBigInt fDBigInt;
            if (b5p[p] != null) {
                fDBigInt = b5p[p];
                return fDBigInt;
            } else if (p < small5pow.length) {
                fDBigInt = new FDBigInt(small5pow[p]);
                b5p[p] = fDBigInt;
                return fDBigInt;
            } else if (p < long5pow.length) {
                fDBigInt = new FDBigInt(long5pow[p]);
                b5p[p] = fDBigInt;
                return fDBigInt;
            } else {
                int q = p >> 1;
                int r = p - q;
                FDBigInt bigq = b5p[q];
                if (bigq == null) {
                    bigq = big5pow(q);
                }
                if (r < small5pow.length) {
                    fDBigInt = bigq.mult(small5pow[r]);
                    b5p[p] = fDBigInt;
                    return fDBigInt;
                }
                FDBigInt bigr = b5p[r];
                if (bigr == null) {
                    bigr = big5pow(r);
                }
                fDBigInt = bigq.mult(bigr);
                b5p[p] = fDBigInt;
                return fDBigInt;
            }
        }
    }

    private static FDBigInt multPow52(FDBigInt v, int p5, int p2) {
        if (p5 != 0) {
            if (p5 < small5pow.length) {
                v = v.mult(small5pow[p5]);
            } else {
                v = v.mult(big5pow(p5));
            }
        }
        if (p2 != 0) {
            v.lshiftMe(p2);
        }
        return v;
    }

    private static FDBigInt constructPow52(int p5, int p2) {
        FDBigInt v = new FDBigInt(big5pow(p5));
        if (p2 != 0) {
            v.lshiftMe(p2);
        }
        return v;
    }

    private FDBigInt doubleToBigInt(double dval) {
        long lbits = Double.doubleToLongBits(dval) & Long.MAX_VALUE;
        int binexp = (int) (lbits >>> expShift);
        lbits &= fractMask;
        if (binexp > 0) {
            lbits |= fractHOB;
        } else {
            if (!-assertionsDisabled) {
                if ((lbits != 0 ? 1 : null) == null) {
                    throw new AssertionError(Long.valueOf(lbits));
                }
            }
            binexp++;
            while ((fractHOB & lbits) == 0) {
                lbits <<= 1;
                binexp--;
            }
        }
        binexp -= 1023;
        int nbits = countBits(lbits);
        lbits >>>= 53 - nbits;
        this.bigIntExp = (binexp + 1) - nbits;
        this.bigIntNBits = nbits;
        return new FDBigInt(lbits);
    }

    private static double ulp(double dval, boolean subtracting) {
        double ulpval;
        long lbits = Double.doubleToLongBits(dval) & Long.MAX_VALUE;
        int binexp = (int) (lbits >>> 52);
        if (subtracting && binexp >= expShift && (fractMask & lbits) == 0) {
            binexp--;
        }
        if (binexp > expShift) {
            ulpval = Double.longBitsToDouble(((long) (binexp - 52)) << 52);
        } else if (binexp == 0) {
            ulpval = DoubleConsts.MIN_VALUE;
        } else {
            ulpval = Double.longBitsToDouble(1 << (binexp - 1));
        }
        if (subtracting) {
            return -ulpval;
        }
        return ulpval;
    }

    float stickyRound(double dval) {
        long lbits = Double.doubleToLongBits(dval);
        long binexp = lbits & expMask;
        if (binexp == 0 || binexp == expMask) {
            return (float) dval;
        }
        return (float) Double.longBitsToDouble(lbits + ((long) this.roundDir));
    }

    private void developLongDigits(int decExponent, long lvalue, long insignificant) {
        int ndigits;
        char[] digits;
        int digitno;
        int i = singleMaxSmallTen;
        while (insignificant >= 10) {
            insignificant /= 10;
            i++;
        }
        if (i != 0) {
            long pow10 = long5pow[i] << i;
            long residue = lvalue % pow10;
            lvalue /= pow10;
            decExponent += i;
            if (residue >= (pow10 >> 1)) {
                lvalue++;
            }
        }
        int c;
        int digitno2;
        if (lvalue <= 2147483647L) {
            if (!-assertionsDisabled) {
                if ((lvalue > 0 ? 1 : null) == null) {
                    throw new AssertionError(Long.valueOf(lvalue));
                }
            }
            int ivalue = (int) lvalue;
            ndigits = 10;
            digits = (char[]) perThreadBuffer.get();
            digitno = intDecimalDigits;
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
                digits[digitno2] = (char) (c + 48);
                decExponent++;
                c = ivalue % 10;
                ivalue /= 10;
            }
            digits[digitno2] = (char) (c + 48);
            digitno = digitno2;
        } else {
            ndigits = 20;
            digits = (char[]) perThreadBuffer.get();
            digitno = 19;
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
                digits[digitno2] = (char) (c + 48);
                decExponent++;
                c = (int) (lvalue % 10);
                lvalue /= 10;
            }
            digits[digitno2] = (char) (c + 48);
            digitno = digitno2;
        }
        ndigits -= digitno;
        char[] result = new char[ndigits];
        System.arraycopy(digits, digitno, result, (int) singleMaxSmallTen, ndigits);
        this.digits = result;
        this.decExponent = decExponent + 1;
        this.nDigits = ndigits;
    }

    private void roundup() {
        int i = this.nDigits - 1;
        int q = this.digits[i];
        if (q == 57) {
            while (q == 57 && i > 0) {
                this.digits[i] = '0';
                i--;
                q = this.digits[i];
            }
            if (q == 57) {
                this.decExponent++;
                this.digits[singleMaxSmallTen] = '1';
                return;
            }
        }
        this.digits[i] = (char) (q + 1);
    }

    private FloatingDecimal() {
        this.mustSetRoundDir = -assertionsDisabled;
        this.fromHex = -assertionsDisabled;
        this.roundDir = singleMaxSmallTen;
    }

    public static FloatingDecimal getThreadLocalInstance() {
        return (FloatingDecimal) TL_INSTANCE.get();
    }

    public FloatingDecimal loadDouble(double d) {
        long dBits = Double.doubleToLongBits(d);
        this.mustSetRoundDir = -assertionsDisabled;
        this.fromHex = -assertionsDisabled;
        this.roundDir = singleMaxSmallTen;
        if ((signMask & dBits) != 0) {
            this.isNegative = true;
            dBits ^= signMask;
        } else {
            this.isNegative = -assertionsDisabled;
        }
        int binExp = (int) ((expMask & dBits) >> expShift);
        long fractBits = dBits & fractMask;
        if (binExp == 2047) {
            this.isExceptional = true;
            if (fractBits == 0) {
                this.digits = infinity;
            } else {
                this.digits = notANumber;
                this.isNegative = -assertionsDisabled;
            }
            this.nDigits = this.digits.length;
            return this;
        }
        int nSignificantBits;
        this.isExceptional = -assertionsDisabled;
        if (binExp != 0) {
            fractBits |= fractHOB;
            nSignificantBits = 53;
        } else if (fractBits == 0) {
            this.decExponent = singleMaxSmallTen;
            this.digits = zero;
            this.nDigits = 1;
            return this;
        } else {
            while ((fractHOB & fractBits) == 0) {
                fractBits <<= 1;
                binExp--;
            }
            nSignificantBits = (binExp + expShift) + 1;
            binExp++;
        }
        dtoa(binExp - 1023, fractBits, nSignificantBits);
        return this;
    }

    public FloatingDecimal loadFloat(float f) {
        int fBits = Float.floatToIntBits(f);
        this.mustSetRoundDir = -assertionsDisabled;
        this.fromHex = -assertionsDisabled;
        this.roundDir = singleMaxSmallTen;
        if ((fBits & singleSignMask) != 0) {
            this.isNegative = true;
            fBits ^= singleSignMask;
        } else {
            this.isNegative = -assertionsDisabled;
        }
        int binExp = (singleExpMask & fBits) >> singleExpShift;
        int fractBits = fBits & singleFractMask;
        if (binExp == 255) {
            this.isExceptional = true;
            if (((long) fractBits) == 0) {
                this.digits = infinity;
            } else {
                this.digits = notANumber;
                this.isNegative = -assertionsDisabled;
            }
            this.nDigits = this.digits.length;
            return this;
        }
        int nSignificantBits;
        this.isExceptional = -assertionsDisabled;
        if (binExp != 0) {
            fractBits |= singleFractHOB;
            nSignificantBits = 24;
        } else if (fractBits == 0) {
            this.decExponent = singleMaxSmallTen;
            this.digits = zero;
            this.nDigits = 1;
            return this;
        } else {
            while ((fractBits & singleFractHOB) == 0) {
                fractBits <<= 1;
                binExp--;
            }
            nSignificantBits = (binExp + singleExpShift) + 1;
            binExp++;
        }
        dtoa(binExp - 127, ((long) fractBits) << 29, nSignificantBits);
        return this;
    }

    private void dtoa(int binExp, long fractBits, int nSignificantBits) {
        int nFractBits = countBits(fractBits);
        int nTinyBits = Math.max((int) singleMaxSmallTen, (nFractBits - binExp) - 1);
        if (binExp > maxSmallBinExp || binExp < minSmallBinExp || nTinyBits >= long5pow.length || n5bits[nTinyBits] + nFractBits >= 64 || nTinyBits != 0) {
            int i;
            int ndigit;
            boolean low;
            boolean high;
            long lowDigitDifference;
            int decExp = (int) Math.floor((((Double.longBitsToDouble((-4503599627370497L & fractBits) | expOne) - 1.5d) * 0.289529654d) + 0.176091259d) + (((double) binExp) * 0.301029995663981d));
            int B5 = Math.max((int) singleMaxSmallTen, -decExp);
            int B2 = (B5 + nTinyBits) + binExp;
            int S5 = Math.max((int) singleMaxSmallTen, decExp);
            int S2 = S5 + nTinyBits;
            int M5 = B5;
            int M2 = B2 - nSignificantBits;
            fractBits >>>= 53 - nFractBits;
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
                M2 = singleMaxSmallTen;
            }
            char[] digits = new char[18];
            this.digits = digits;
            int Bbits = (nFractBits + B2) + (B5 < n5bits.length ? n5bits[B5] : B5 * 3);
            int i2 = S2 + 1;
            if (S5 + 1 < n5bits.length) {
                i = n5bits[S5 + 1];
            } else {
                i = (S5 + 1) * 3;
            }
            int tenSbits = i2 + i;
            int q;
            int ndigit2;
            if (Bbits >= 64 || tenSbits >= 64) {
                FDBigInt Bval = multPow52(new FDBigInt(fractBits), B5, B2);
                FDBigInt Sval = constructPow52(S5, S2);
                FDBigInt Mval = constructPow52(B5, M2);
                int shiftBias = Sval.normalizeMe();
                Bval.lshiftMe(shiftBias);
                Mval.lshiftMe(shiftBias);
                FDBigInt tenSval = Sval.mult(10);
                ndigit = singleMaxSmallTen;
                q = Bval.quoRemIteration(Sval);
                Mval = Mval.mult(10);
                low = Bval.cmp(Mval) < 0 ? true : -assertionsDisabled;
                high = Bval.add(Mval).cmp(tenSval) > 0 ? true : -assertionsDisabled;
                if (!-assertionsDisabled) {
                    if ((q < 10 ? 1 : null) == null) {
                        throw new AssertionError(Integer.valueOf(q));
                    }
                }
                if (q != 0 || high) {
                    ndigit = 1;
                    digits[singleMaxSmallTen] = (char) (q + 48);
                } else {
                    decExp--;
                }
                if (decExp < -3 || decExp >= 8) {
                    low = -assertionsDisabled;
                    high = -assertionsDisabled;
                    ndigit2 = ndigit;
                } else {
                    ndigit2 = ndigit;
                }
                while (!low && !high) {
                    q = Bval.quoRemIteration(Sval);
                    Mval = Mval.mult(10);
                    if (!-assertionsDisabled) {
                        if ((q < 10 ? 1 : null) == null) {
                            throw new AssertionError(Integer.valueOf(q));
                        }
                    }
                    low = Bval.cmp(Mval) < 0 ? true : -assertionsDisabled;
                    high = Bval.add(Mval).cmp(tenSval) > 0 ? true : -assertionsDisabled;
                    ndigit = ndigit2 + 1;
                    digits[ndigit2] = (char) (q + 48);
                    ndigit2 = ndigit;
                }
                if (high && low) {
                    Bval.lshiftMe(1);
                    lowDigitDifference = (long) Bval.cmp(tenSval);
                    ndigit = ndigit2;
                } else {
                    lowDigitDifference = 0;
                    ndigit = ndigit2;
                }
            } else if (Bbits >= 32 || tenSbits >= 32) {
                long b = (long5pow[B5] * fractBits) << B2;
                long s = long5pow[S5] << S2;
                long m = long5pow[B5] << M2;
                long tens = s * 10;
                ndigit = singleMaxSmallTen;
                q = (int) (b / s);
                b = 10 * (b % s);
                m *= 10;
                low = b < m ? true : -assertionsDisabled;
                high = b + m > tens ? true : -assertionsDisabled;
                if (!-assertionsDisabled) {
                    if ((q < 10 ? 1 : null) == null) {
                        throw new AssertionError(Integer.valueOf(q));
                    }
                }
                if (q != 0 || high) {
                    ndigit = 1;
                    digits[singleMaxSmallTen] = (char) (q + 48);
                } else {
                    decExp--;
                }
                if (decExp < -3 || decExp >= 8) {
                    low = -assertionsDisabled;
                    high = -assertionsDisabled;
                    ndigit2 = ndigit;
                } else {
                    ndigit2 = ndigit;
                }
                while (!low && !high) {
                    q = (int) (b / s);
                    b = 10 * (b % s);
                    m *= 10;
                    if (!-assertionsDisabled) {
                        if ((q < 10 ? 1 : null) == null) {
                            throw new AssertionError(Integer.valueOf(q));
                        }
                    }
                    if (m > 0) {
                        low = b < m ? true : -assertionsDisabled;
                        high = b + m > tens ? true : -assertionsDisabled;
                    } else {
                        low = true;
                        high = true;
                    }
                    ndigit = ndigit2 + 1;
                    digits[ndigit2] = (char) (q + 48);
                    ndigit2 = ndigit;
                }
                lowDigitDifference = (b << 1) - tens;
                ndigit = ndigit2;
            } else {
                int b2 = (((int) fractBits) * small5pow[B5]) << B2;
                int s2 = small5pow[S5] << S2;
                int m2 = small5pow[B5] << M2;
                int tens2 = s2 * 10;
                ndigit = singleMaxSmallTen;
                q = b2 / s2;
                b2 = (b2 % s2) * 10;
                m2 *= 10;
                low = b2 < m2 ? true : -assertionsDisabled;
                high = b2 + m2 > tens2 ? true : -assertionsDisabled;
                if (!-assertionsDisabled) {
                    Object obj;
                    if (q < 10) {
                        obj = 1;
                    } else {
                        obj = null;
                    }
                    if (obj == null) {
                        throw new AssertionError(Integer.valueOf(q));
                    }
                }
                if (q != 0 || high) {
                    ndigit = 1;
                    digits[singleMaxSmallTen] = (char) (q + 48);
                } else {
                    decExp--;
                }
                if (decExp < -3 || decExp >= 8) {
                    low = -assertionsDisabled;
                    high = -assertionsDisabled;
                    ndigit2 = ndigit;
                } else {
                    ndigit2 = ndigit;
                }
                while (!low && !high) {
                    q = b2 / s2;
                    b2 = (b2 % s2) * 10;
                    m2 *= 10;
                    if (!-assertionsDisabled) {
                        if ((q < 10 ? 1 : null) == null) {
                            throw new AssertionError(Integer.valueOf(q));
                        }
                    }
                    if (((long) m2) > 0) {
                        low = b2 < m2 ? true : -assertionsDisabled;
                        high = b2 + m2 > tens2 ? true : -assertionsDisabled;
                    } else {
                        low = true;
                        high = true;
                    }
                    ndigit = ndigit2 + 1;
                    digits[ndigit2] = (char) (q + 48);
                    ndigit2 = ndigit;
                }
                lowDigitDifference = (long) ((b2 << 1) - tens2);
                ndigit = ndigit2;
            }
            this.decExponent = decExp + 1;
            this.digits = digits;
            this.nDigits = ndigit;
            if (high) {
                if (!low) {
                    roundup();
                } else if (lowDigitDifference == 0) {
                    if ((digits[this.nDigits - 1] & 1) != 0) {
                        roundup();
                    }
                } else if (lowDigitDifference > 0) {
                    roundup();
                }
            }
            return;
        }
        long halfULP;
        if (binExp > nSignificantBits) {
            halfULP = 1 << ((binExp - nSignificantBits) - 1);
        } else {
            halfULP = 0;
        }
        if (binExp >= expShift) {
            fractBits <<= binExp - 52;
        } else {
            fractBits >>>= 52 - binExp;
        }
        developLongDigits(singleMaxSmallTen, fractBits, halfULP);
    }

    public String toString() {
        StringBuffer result = new StringBuffer(this.nDigits + 8);
        if (this.isNegative) {
            result.append('-');
        }
        if (this.isExceptional) {
            result.append(this.digits, (int) singleMaxSmallTen, this.nDigits);
        } else {
            result.append("0.");
            result.append(this.digits, (int) singleMaxSmallTen, this.nDigits);
            result.append('e');
            result.append(this.decExponent);
        }
        return new String(result);
    }

    public String toJavaFormatString() {
        char[] result = (char[]) perThreadBuffer.get();
        return new String(result, (int) singleMaxSmallTen, getChars(result));
    }

    private int getChars(char[] result) {
        if (!-assertionsDisabled) {
            if ((this.nDigits <= 19 ? 1 : singleMaxSmallTen) == 0) {
                throw new AssertionError(Integer.valueOf(this.nDigits));
            }
        }
        int i = singleMaxSmallTen;
        if (this.isNegative) {
            result[singleMaxSmallTen] = '-';
            i = 1;
        }
        if (this.isExceptional) {
            System.arraycopy(this.digits, (int) singleMaxSmallTen, result, i, this.nDigits);
            return i + this.nDigits;
        } else if (this.decExponent > 0 && this.decExponent < 8) {
            int charLength = Math.min(this.nDigits, this.decExponent);
            System.arraycopy(this.digits, (int) singleMaxSmallTen, result, i, charLength);
            i += charLength;
            if (charLength < this.decExponent) {
                charLength = this.decExponent - charLength;
                System.arraycopy(zero, (int) singleMaxSmallTen, result, i, charLength);
                i += charLength;
                i = i + 1;
                result[i] = '.';
                i = i + 1;
                result[i] = '0';
                return i;
            }
            i = i + 1;
            result[i] = '.';
            if (charLength < this.nDigits) {
                int t = this.nDigits - charLength;
                System.arraycopy(this.digits, charLength, result, i, t);
                return i + t;
            }
            i = i + 1;
            result[i] = '0';
            return i;
        } else if (this.decExponent > 0 || this.decExponent <= -3) {
            int e;
            i = i + 1;
            result[i] = this.digits[singleMaxSmallTen];
            i = i + 1;
            result[i] = '.';
            if (this.nDigits > 1) {
                System.arraycopy(this.digits, 1, result, i, this.nDigits - 1);
                i += this.nDigits - 1;
            } else {
                i = i + 1;
                result[i] = '0';
                i = i;
            }
            i = i + 1;
            result[i] = 'E';
            if (this.decExponent <= 0) {
                i = i + 1;
                result[i] = '-';
                e = (-this.decExponent) + 1;
                i = i;
            } else {
                e = this.decExponent - 1;
            }
            if (e <= intDecimalDigits) {
                i = i + 1;
                result[i] = (char) (e + 48);
                return i;
            } else if (e <= 99) {
                i = i + 1;
                result[i] = (char) ((e / 10) + 48);
                i = i + 1;
                result[i] = (char) ((e % 10) + 48);
                return i;
            } else {
                i = i + 1;
                result[i] = (char) ((e / 100) + 48);
                e %= 100;
                i = i + 1;
                result[i] = (char) ((e / 10) + 48);
                i = i + 1;
                result[i] = (char) ((e % 10) + 48);
                return i;
            }
        } else {
            i = i + 1;
            result[i] = '0';
            i = i + 1;
            result[i] = '.';
            if (this.decExponent != 0) {
                System.arraycopy(zero, (int) singleMaxSmallTen, result, i, -this.decExponent);
                i -= this.decExponent;
            }
            System.arraycopy(this.digits, (int) singleMaxSmallTen, result, i, this.nDigits);
            return i + this.nDigits;
        }
    }

    public void appendTo(AbstractStringBuilder buf) {
        if (this.isNegative) {
            buf.append('-');
        }
        if (this.isExceptional) {
            buf.append(this.digits, (int) singleMaxSmallTen, this.nDigits);
            return;
        }
        if (this.decExponent > 0 && this.decExponent < 8) {
            int charLength = Math.min(this.nDigits, this.decExponent);
            buf.append(this.digits, (int) singleMaxSmallTen, charLength);
            if (charLength < this.decExponent) {
                buf.append(zero, (int) singleMaxSmallTen, this.decExponent - charLength);
                buf.append(".0");
            } else {
                buf.append('.');
                if (charLength < this.nDigits) {
                    buf.append(this.digits, charLength, this.nDigits - charLength);
                } else {
                    buf.append('0');
                }
            }
        } else if (this.decExponent > 0 || this.decExponent <= -3) {
            int e;
            buf.append(this.digits[singleMaxSmallTen]);
            buf.append('.');
            if (this.nDigits > 1) {
                buf.append(this.digits, 1, this.nDigits - 1);
            } else {
                buf.append('0');
            }
            buf.append('E');
            if (this.decExponent <= 0) {
                buf.append('-');
                e = (-this.decExponent) + 1;
            } else {
                e = this.decExponent - 1;
            }
            if (e <= intDecimalDigits) {
                buf.append((char) (e + 48));
            } else if (e <= 99) {
                buf.append((char) ((e / 10) + 48));
                buf.append((char) ((e % 10) + 48));
            } else {
                buf.append((char) ((e / 100) + 48));
                e %= 100;
                buf.append((char) ((e / 10) + 48));
                buf.append((char) ((e % 10) + 48));
            }
        } else {
            buf.append("0.");
            if (this.decExponent != 0) {
                buf.append(zero, (int) singleMaxSmallTen, -this.decExponent);
            }
            buf.append(this.digits, (int) singleMaxSmallTen, this.nDigits);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public FloatingDecimal readJavaFormatString(String in) throws NumberFormatException {
        boolean isNegative = -assertionsDisabled;
        boolean signSeen = -assertionsDisabled;
        try {
            in = in.trim();
            int l = in.length();
            if (l == 0) {
                throw new NumberFormatException("empty String");
            }
            int i = singleMaxSmallTen;
            switch (in.charAt(singleMaxSmallTen)) {
                case '+':
                    break;
                case '-':
                    isNegative = true;
                    break;
            }
            i = 1;
            signSeen = true;
            char c = in.charAt(i);
            if (c == 'N' || c == 'I') {
                char[] targetChars;
                boolean potentialNaN = -assertionsDisabled;
                if (c == 'N') {
                    targetChars = notANumber;
                    potentialNaN = true;
                } else {
                    targetChars = infinity;
                }
                int j = singleMaxSmallTen;
                while (i < l && j < targetChars.length) {
                    if (in.charAt(i) == targetChars[j]) {
                        i++;
                        j++;
                    } else {
                        throw new NumberFormatException("For input string: \"" + in + "\"");
                    }
                }
                if (j == targetChars.length && i == l) {
                    FloatingDecimal loadDouble;
                    if (potentialNaN) {
                        loadDouble = loadDouble(DoubleConsts.NaN);
                    } else {
                        double d;
                        if (isNegative) {
                            d = DoubleConsts.NEGATIVE_INFINITY;
                        } else {
                            d = DoubleConsts.POSITIVE_INFINITY;
                        }
                        loadDouble = loadDouble(d);
                    }
                    return loadDouble;
                }
                throw new NumberFormatException("For input string: \"" + in + "\"");
            }
            int decExp;
            int expSign;
            int expVal;
            boolean expOverflow;
            int expAt;
            int i2;
            int expLimit;
            if (c == '0' && l > i + 1) {
                char ch = in.charAt(i + 1);
                if (ch == 'x' || ch == 'X') {
                    return parseHexString(in);
                }
            }
            char[] digits = new char[l];
            int nDigits = singleMaxSmallTen;
            boolean decSeen = -assertionsDisabled;
            int decPt = singleMaxSmallTen;
            int nLeadZero = singleMaxSmallTen;
            int nTrailZero = singleMaxSmallTen;
            while (i < l) {
                c = in.charAt(i);
                switch (c) {
                    case ZipConstants.CENHDR /*46*/:
                        if (decSeen) {
                            throw new NumberFormatException("multiple points");
                        }
                        decPt = i;
                        if (signSeen) {
                            decPt--;
                        }
                        decSeen = true;
                        continue;
                    case '0':
                        if (nDigits <= 0) {
                            nLeadZero++;
                            break;
                        }
                        nTrailZero++;
                        continue;
                    case '1':
                    case '2':
                    case '3':
                    case expShift /*52*/:
                    case DoubleConsts.SIGNIFICAND_WIDTH /*53*/:
                    case '6':
                    case '7':
                    case '8':
                    case '9':
                        int nDigits2 = nDigits;
                        while (nTrailZero > 0) {
                            nDigits = nDigits2 + 1;
                            digits[nDigits2] = '0';
                            nTrailZero--;
                            nDigits2 = nDigits;
                        }
                        nDigits = nDigits2 + 1;
                        digits[nDigits2] = c;
                        continue;
                    default:
                        break;
                }
                if (nDigits == 0) {
                    digits = zero;
                    nDigits = 1;
                }
                if (decSeen) {
                    decExp = nDigits + nTrailZero;
                } else {
                    decExp = decPt - nLeadZero;
                }
                if (i < l) {
                    c = in.charAt(i);
                    if (c == 'e' || c == 'E') {
                        expSign = 1;
                        expVal = singleMaxSmallTen;
                        expOverflow = -assertionsDisabled;
                        i++;
                        switch (in.charAt(i)) {
                            case '+':
                                break;
                            case '-':
                                expSign = -1;
                                break;
                        }
                        i++;
                        expAt = i;
                        i2 = i;
                        while (i2 < l) {
                            if (expVal >= 214748364) {
                                expOverflow = true;
                            }
                            i = i2 + 1;
                            c = in.charAt(i2);
                            switch (c) {
                                case '0':
                                case '1':
                                case '2':
                                case '3':
                                case expShift /*52*/:
                                case DoubleConsts.SIGNIFICAND_WIDTH /*53*/:
                                case '6':
                                case '7':
                                case '8':
                                case '9':
                                    expVal = (expVal * 10) + (c - 48);
                                    i2 = i;
                                default:
                                    i--;
                                    break;
                            }
                            expLimit = (nDigits + bigDecimalExponent) + nTrailZero;
                            if (!expOverflow || expVal > expLimit) {
                                decExp = expSign * expLimit;
                            } else {
                                decExp += expSign * expVal;
                            }
                        }
                        i = i2;
                        expLimit = (nDigits + bigDecimalExponent) + nTrailZero;
                        if (expOverflow) {
                        }
                        decExp = expSign * expLimit;
                    }
                }
                if (i >= l || (i == l - 1 && (in.charAt(i) == 'f' || in.charAt(i) == 'F' || in.charAt(i) == 'd' || in.charAt(i) == 'D'))) {
                    this.isNegative = isNegative;
                    this.decExponent = decExp;
                    this.digits = digits;
                    this.nDigits = nDigits;
                    this.isExceptional = -assertionsDisabled;
                    return this;
                }
                throw new NumberFormatException("For input string: \"" + in + "\"");
            }
            if (nDigits == 0) {
                digits = zero;
                nDigits = 1;
            }
            if (decSeen) {
                decExp = nDigits + nTrailZero;
            } else {
                decExp = decPt - nLeadZero;
            }
            if (i < l) {
                c = in.charAt(i);
                expSign = 1;
                expVal = singleMaxSmallTen;
                expOverflow = -assertionsDisabled;
                i++;
                switch (in.charAt(i)) {
                    case '+':
                        break;
                    case '-':
                        expSign = -1;
                        break;
                }
                i++;
                expAt = i;
                i2 = i;
                while (i2 < l) {
                    if (expVal >= 214748364) {
                        expOverflow = true;
                    }
                    i = i2 + 1;
                    c = in.charAt(i2);
                    switch (c) {
                        case '0':
                        case '1':
                        case '2':
                        case '3':
                        case expShift /*52*/:
                        case DoubleConsts.SIGNIFICAND_WIDTH /*53*/:
                        case '6':
                        case '7':
                        case '8':
                        case '9':
                            expVal = (expVal * 10) + (c - 48);
                            i2 = i;
                        default:
                            i--;
                            break;
                    }
                    expLimit = (nDigits + bigDecimalExponent) + nTrailZero;
                    if (expOverflow) {
                    }
                    decExp = expSign * expLimit;
                }
                i = i2;
                expLimit = (nDigits + bigDecimalExponent) + nTrailZero;
                if (expOverflow) {
                }
                decExp = expSign * expLimit;
            }
            this.isNegative = isNegative;
            this.decExponent = decExp;
            this.digits = digits;
            this.nDigits = nDigits;
            this.isExceptional = -assertionsDisabled;
            return this;
        } catch (StringIndexOutOfBoundsException e) {
        }
    }

    public double doubleValue() {
        int kDigits = Math.min(this.nDigits, 16);
        if (this.digits != infinity && this.digits != notANumber) {
            int i;
            int i2;
            if (this.mustSetRoundDir) {
                this.roundDir = singleMaxSmallTen;
            }
            int iValue = this.digits[singleMaxSmallTen] - 48;
            int iDigits = Math.min(kDigits, (int) intDecimalDigits);
            for (i = 1; i < iDigits; i++) {
                iValue = ((iValue * 10) + this.digits[i]) - 48;
            }
            long lValue = (long) iValue;
            for (i = iDigits; i < kDigits; i++) {
                lValue = (10 * lValue) + ((long) (this.digits[i] - 48));
            }
            double dValue = (double) lValue;
            int exp = this.decExponent - kDigits;
            if (this.nDigits <= maxDecimalDigits) {
                if (exp == 0 || dValue == 0.0d) {
                    if (this.isNegative) {
                        dValue = -dValue;
                    }
                    return dValue;
                } else if (exp >= 0) {
                    if (exp <= maxSmallTen) {
                        rValue = dValue * small10pow[exp];
                        if (this.mustSetRoundDir) {
                            tValue = rValue / small10pow[exp];
                            if (tValue == dValue) {
                                i2 = singleMaxSmallTen;
                            } else if (tValue < dValue) {
                                i2 = 1;
                            } else {
                                i2 = -1;
                            }
                            this.roundDir = i2;
                        }
                        if (this.isNegative) {
                            rValue = -rValue;
                        }
                        return rValue;
                    }
                    int slop = 15 - kDigits;
                    if (exp <= maxSmallTen + slop) {
                        dValue *= small10pow[slop];
                        rValue = dValue * small10pow[exp - slop];
                        if (this.mustSetRoundDir) {
                            tValue = rValue / small10pow[exp - slop];
                            if (tValue == dValue) {
                                i2 = singleMaxSmallTen;
                            } else if (tValue < dValue) {
                                i2 = 1;
                            } else {
                                i2 = -1;
                            }
                            this.roundDir = i2;
                        }
                        if (this.isNegative) {
                            rValue = -rValue;
                        }
                        return rValue;
                    }
                } else if (exp >= (-maxSmallTen)) {
                    rValue = dValue / small10pow[-exp];
                    tValue = rValue * small10pow[-exp];
                    if (this.mustSetRoundDir) {
                        if (tValue == dValue) {
                            i2 = singleMaxSmallTen;
                        } else if (tValue < dValue) {
                            i2 = 1;
                        } else {
                            i2 = -1;
                        }
                        this.roundDir = i2;
                    }
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
                    return this.isNegative ? DoubleConsts.NEGATIVE_INFINITY : DoubleConsts.POSITIVE_INFINITY;
                }
                if ((exp & maxDecimalDigits) != 0) {
                    dValue *= small10pow[exp & maxDecimalDigits];
                }
                exp >>= 4;
                if (exp != 0) {
                    j = singleMaxSmallTen;
                    while (exp > 1) {
                        if ((exp & 1) != 0) {
                            dValue *= big10pow[j];
                        }
                        j++;
                        exp >>= 1;
                    }
                    t = dValue * big10pow[j];
                    if (Double.isInfinite(t)) {
                        if (Double.isInfinite((dValue / 2.0d) * big10pow[j])) {
                            return this.isNegative ? DoubleConsts.NEGATIVE_INFINITY : DoubleConsts.POSITIVE_INFINITY;
                        }
                        t = DoubleConsts.MAX_VALUE;
                    }
                    dValue = t;
                }
            } else if (exp < 0) {
                exp = -exp;
                if (this.decExponent < -325) {
                    return this.isNegative ? -0.0d : 0.0d;
                }
                if ((exp & maxDecimalDigits) != 0) {
                    dValue /= small10pow[exp & maxDecimalDigits];
                }
                exp >>= 4;
                if (exp != 0) {
                    j = singleMaxSmallTen;
                    while (exp > 1) {
                        if ((exp & 1) != 0) {
                            dValue *= tiny10pow[j];
                        }
                        j++;
                        exp >>= 1;
                    }
                    t = dValue * tiny10pow[j];
                    if (t == 0.0d) {
                        if ((dValue * 2.0d) * tiny10pow[j] == 0.0d) {
                            return this.isNegative ? -0.0d : 0.0d;
                        }
                        t = DoubleConsts.MIN_VALUE;
                    }
                    dValue = t;
                }
            }
            FDBigInt bigD0 = new FDBigInt(lValue, this.digits, kDigits, this.nDigits);
            exp = this.decExponent - this.nDigits;
            do {
                int B5;
                int B2;
                int D5;
                int D2;
                int hulpbias;
                boolean overvalue;
                FDBigInt diff;
                FDBigInt bigB = doubleToBigInt(dValue);
                if (exp >= 0) {
                    B5 = singleMaxSmallTen;
                    B2 = singleMaxSmallTen;
                    D5 = exp;
                    D2 = exp;
                } else {
                    B5 = -exp;
                    B2 = B5;
                    D5 = singleMaxSmallTen;
                    D2 = singleMaxSmallTen;
                }
                if (this.bigIntExp >= 0) {
                    B2 += this.bigIntExp;
                } else {
                    D2 -= this.bigIntExp;
                }
                int Ulp2 = B2;
                if (this.bigIntExp + this.bigIntNBits <= DoubleConsts.MIN_EXPONENT) {
                    hulpbias = (this.bigIntExp + expBias) + expShift;
                } else {
                    hulpbias = 54 - this.bigIntNBits;
                }
                B2 += hulpbias;
                D2 += hulpbias;
                int common2 = Math.min(B2, Math.min(D2, Ulp2));
                D2 -= common2;
                Ulp2 -= common2;
                bigB = multPow52(bigB, B5, B2 - common2);
                FDBigInt bigD = multPow52(new FDBigInt(bigD0), D5, D2);
                int cmpResult = bigB.cmp(bigD);
                if (cmpResult <= 0) {
                    if (cmpResult >= 0) {
                        break;
                    }
                    overvalue = -assertionsDisabled;
                    diff = bigD.sub(bigB);
                } else {
                    overvalue = true;
                    diff = bigB.sub(bigD);
                    if (this.bigIntNBits == 1 && this.bigIntExp > DoubleConsts.MIN_EXPONENT) {
                        Ulp2--;
                        if (Ulp2 < 0) {
                            Ulp2 = singleMaxSmallTen;
                            diff.lshiftMe(1);
                        }
                    }
                }
                cmpResult = diff.cmp(constructPow52(B5, Ulp2));
                if (cmpResult >= 0) {
                    if (cmpResult != 0) {
                        dValue += ulp(dValue, overvalue);
                        if (dValue == 0.0d) {
                            break;
                        }
                    } else {
                        dValue += ulp(dValue, overvalue) * 0.5d;
                        if (this.mustSetRoundDir) {
                            this.roundDir = overvalue ? -1 : 1;
                        }
                    }
                } else if (this.mustSetRoundDir) {
                    if (overvalue) {
                        i2 = -1;
                    } else {
                        i2 = 1;
                    }
                    this.roundDir = i2;
                }
            } while (dValue != DoubleConsts.POSITIVE_INFINITY);
            if (this.isNegative) {
                dValue = -dValue;
            }
            return dValue;
        } else if (this.digits == notANumber) {
            return DoubleConsts.NaN;
        } else {
            return this.isNegative ? DoubleConsts.NEGATIVE_INFINITY : DoubleConsts.POSITIVE_INFINITY;
        }
    }

    public float floatValue() {
        int kDigits = Math.min(this.nDigits, 8);
        if (this.digits != infinity && this.digits != notANumber) {
            int i;
            int iValue = this.digits[singleMaxSmallTen] - 48;
            for (i = 1; i < kDigits; i++) {
                iValue = ((iValue * 10) + this.digits[i]) - 48;
            }
            float fValue = (float) iValue;
            int exp = this.decExponent - kDigits;
            if (this.nDigits <= singleMaxDecimalDigits) {
                if (exp == 0 || fValue == 0.0f) {
                    if (this.isNegative) {
                        fValue = -fValue;
                    }
                    return fValue;
                } else if (exp >= 0) {
                    if (exp <= singleMaxSmallTen) {
                        fValue *= singleSmall10pow[exp];
                        if (this.isNegative) {
                            fValue = -fValue;
                        }
                        return fValue;
                    }
                    int slop = 7 - kDigits;
                    if (exp <= singleMaxSmallTen + slop) {
                        fValue = (fValue * singleSmall10pow[slop]) * singleSmall10pow[exp - slop];
                        if (this.isNegative) {
                            fValue = -fValue;
                        }
                        return fValue;
                    }
                } else if (exp >= (-singleMaxSmallTen)) {
                    fValue /= singleSmall10pow[-exp];
                    if (this.isNegative) {
                        fValue = -fValue;
                    }
                    return fValue;
                }
            } else if (this.decExponent >= this.nDigits && this.nDigits + this.decExponent <= maxDecimalDigits) {
                long lValue = (long) iValue;
                for (i = kDigits; i < this.nDigits; i++) {
                    lValue = (10 * lValue) + ((long) (this.digits[i] - 48));
                }
                fValue = (float) (((double) lValue) * small10pow[this.decExponent - this.nDigits]);
                if (this.isNegative) {
                    fValue = -fValue;
                }
                return fValue;
            }
            if (this.decExponent > 39) {
                return this.isNegative ? FloatConsts.NEGATIVE_INFINITY : FloatConsts.POSITIVE_INFINITY;
            } else if (this.decExponent < -46) {
                return this.isNegative ? -0.0f : 0.0f;
            } else {
                this.mustSetRoundDir = this.fromHex ? -assertionsDisabled : true;
                return stickyRound(doubleValue());
            }
        } else if (this.digits == notANumber) {
            return FloatConsts.NaN;
        } else {
            return this.isNegative ? FloatConsts.NEGATIVE_INFINITY : FloatConsts.POSITIVE_INFINITY;
        }
    }

    private static synchronized Pattern getHexFloatPattern() {
        Pattern pattern;
        synchronized (FloatingDecimal.class) {
            if (hexFloatPattern == null) {
                hexFloatPattern = Pattern.compile("([-+])?0[xX](((\\p{XDigit}+)\\.?)|((\\p{XDigit}*)\\.(\\p{XDigit}+)))[pP]([-+])?(\\p{Digit}+)[fFdD]?");
            }
            pattern = hexFloatPattern;
        }
        return pattern;
    }

    static String stripLeadingZeros(String s) {
        return s.replaceFirst("^0+", "");
    }

    static int getHexDigit(String s, int position) {
        int value = Character.digit(s.charAt(position), 16);
        if (value > -1 && value < 16) {
            return value;
        }
        throw new AssertionError("Unexpected failure of digit conversion of " + s.charAt(position));
    }
}
