package sun.misc;

import java.util.Arrays;
import sun.misc.FloatingDecimal;

public class FormattedFloatingDecimal {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private static final ThreadLocal<Object> threadLocalCharBuffer = new ThreadLocal<Object>() {
        /* access modifiers changed from: protected */
        public Object initialValue() {
            return new char[20];
        }
    };
    private int decExponentRounded;
    private char[] exponent;
    private char[] mantissa;

    public enum Form {
        SCIENTIFIC,
        COMPATIBLE,
        DECIMAL_FLOAT,
        GENERAL
    }

    public static FormattedFloatingDecimal valueOf(double d, int precision, Form form) {
        return new FormattedFloatingDecimal(precision, form, FloatingDecimal.getBinaryToASCIIConverter(d, form == Form.COMPATIBLE));
    }

    private static char[] getBuffer() {
        return (char[]) threadLocalCharBuffer.get();
    }

    private FormattedFloatingDecimal(int precision, Form form, FloatingDecimal.BinaryToASCIIConverter fdConverter) {
        if (fdConverter.isExceptional()) {
            this.mantissa = fdConverter.toJavaFormatString().toCharArray();
            this.exponent = null;
            return;
        }
        char[] digits = getBuffer();
        int nDigits = fdConverter.getDigits(digits);
        int decExp = fdConverter.getDecimalExponent();
        boolean isNegative = fdConverter.isNegative();
        switch (form) {
            case COMPATIBLE:
                int exp = decExp;
                this.decExponentRounded = exp;
                fillCompatible(precision, digits, nDigits, exp, isNegative);
                break;
            case DECIMAL_FLOAT:
                int exp2 = applyPrecision(decExp, digits, nDigits, decExp + precision);
                fillDecimal(precision, digits, nDigits, exp2, isNegative);
                this.decExponentRounded = exp2;
                break;
            case SCIENTIFIC:
                int exp3 = applyPrecision(decExp, digits, nDigits, precision + 1);
                fillScientific(precision, digits, nDigits, exp3, isNegative);
                this.decExponentRounded = exp3;
                break;
            case GENERAL:
                int exp4 = applyPrecision(decExp, digits, nDigits, precision);
                if (exp4 - 1 < -4 || exp4 - 1 >= precision) {
                    fillScientific(precision - 1, digits, nDigits, exp4, isNegative);
                } else {
                    fillDecimal(precision - exp4, digits, nDigits, exp4, isNegative);
                }
                this.decExponentRounded = exp4;
                break;
        }
    }

    public int getExponentRounded() {
        return this.decExponentRounded - 1;
    }

    public char[] getMantissa() {
        return this.mantissa;
    }

    public char[] getExponent() {
        return this.exponent;
    }

    /* JADX WARNING: Incorrect type for immutable var: ssa=char, code=int, for r5v1, types: [char] */
    /* JADX WARNING: Incorrect type for immutable var: ssa=char, code=int, for r5v4, types: [char] */
    private static int applyPrecision(int decExp, char[] digits, int nDigits, int prec) {
        if (prec >= nDigits || prec < 0) {
            return decExp;
        }
        if (prec != 0) {
            if (digits[prec] >= '5') {
                int i = prec - 1;
                int q = digits[i];
                if (q == '9') {
                    while (q == 57 && i > 0) {
                        i--;
                        q = digits[i];
                    }
                    if (q == 57) {
                        digits[0] = '1';
                        Arrays.fill(digits, 1, nDigits, '0');
                        return decExp + 1;
                    }
                }
                digits[i] = (char) (q + 1);
                Arrays.fill(digits, i + 1, nDigits, '0');
            } else {
                Arrays.fill(digits, prec, nDigits, '0');
            }
            return decExp;
        } else if (digits[0] >= '5') {
            digits[0] = '1';
            Arrays.fill(digits, 1, nDigits, '0');
            return decExp + 1;
        } else {
            Arrays.fill(digits, 0, nDigits, '0');
            return decExp;
        }
    }

    private void fillCompatible(int precision, char[] digits, int nDigits, int exp, boolean isNegative) {
        int expStartIntex;
        int startIndex = isNegative;
        int e = 0;
        if (exp <= 0 || exp >= 8) {
            if (exp > 0 || exp <= -3) {
                if (nDigits > 1) {
                    this.mantissa = create(isNegative, nDigits + 1);
                    this.mantissa[startIndex] = digits[0];
                    this.mantissa[startIndex + 1] = '.';
                    System.arraycopy((Object) digits, 1, (Object) this.mantissa, startIndex + 2, nDigits - 1);
                } else {
                    this.mantissa = create(isNegative, 3);
                    this.mantissa[startIndex] = digits[0];
                    this.mantissa[startIndex + 1] = '.';
                    this.mantissa[startIndex + 2] = '0';
                }
                boolean isNegExp = exp <= 0;
                if (isNegExp) {
                    expStartIntex = (-exp) + 1;
                    e = 1;
                } else {
                    expStartIntex = exp - 1;
                }
                if (expStartIntex <= 9) {
                    this.exponent = create(isNegExp, 1);
                    this.exponent[e] = (char) (expStartIntex + 48);
                } else if (expStartIntex <= 99) {
                    this.exponent = create(isNegExp, 2);
                    this.exponent[e] = (char) ((expStartIntex / 10) + 48);
                    this.exponent[e + 1] = (char) ((expStartIntex % 10) + 48);
                } else {
                    this.exponent = create(isNegExp, 3);
                    this.exponent[e] = (char) ((expStartIntex / 100) + 48);
                    int e2 = expStartIntex % 100;
                    this.exponent[e + 1] = (char) ((e2 / 10) + 48);
                    this.exponent[e + 2] = (char) ((e2 % 10) + 48);
                }
            } else {
                int zeros = Math.max(0, Math.min(-exp, precision));
                int t = Math.max(0, Math.min(nDigits, precision + exp));
                if (zeros > 0) {
                    this.mantissa = create(isNegative, zeros + 2 + t);
                    this.mantissa[startIndex] = '0';
                    this.mantissa[startIndex + 1] = '.';
                    Arrays.fill(this.mantissa, startIndex + 2, startIndex + 2 + zeros, '0');
                    if (t > 0) {
                        System.arraycopy((Object) digits, 0, (Object) this.mantissa, startIndex + 2 + zeros, t);
                    }
                } else if (t > 0) {
                    this.mantissa = create(isNegative, zeros + 2 + t);
                    this.mantissa[startIndex] = '0';
                    this.mantissa[startIndex + 1] = '.';
                    System.arraycopy((Object) digits, 0, (Object) this.mantissa, startIndex + 2, t);
                } else {
                    this.mantissa = create(isNegative, 1);
                    this.mantissa[startIndex] = '0';
                }
            }
        } else if (nDigits < exp) {
            int extraZeros = exp - nDigits;
            this.mantissa = create(isNegative, nDigits + extraZeros + 2);
            System.arraycopy((Object) digits, 0, (Object) this.mantissa, startIndex, nDigits);
            Arrays.fill(this.mantissa, startIndex + nDigits, startIndex + nDigits + extraZeros, '0');
            this.mantissa[startIndex + nDigits + extraZeros] = '.';
            this.mantissa[startIndex + nDigits + extraZeros + 1] = '0';
        } else if (exp < nDigits) {
            int t2 = Math.min(nDigits - exp, precision);
            this.mantissa = create(isNegative, exp + 1 + t2);
            System.arraycopy((Object) digits, 0, (Object) this.mantissa, startIndex, exp);
            this.mantissa[startIndex + exp] = '.';
            System.arraycopy((Object) digits, exp, (Object) this.mantissa, startIndex + exp + 1, t2);
        } else {
            this.mantissa = create(isNegative, nDigits + 2);
            System.arraycopy((Object) digits, 0, (Object) this.mantissa, startIndex, nDigits);
            this.mantissa[startIndex + nDigits] = '.';
            this.mantissa[startIndex + nDigits + 1] = '0';
        }
    }

    private static char[] create(boolean isNegative, int size) {
        if (!isNegative) {
            return new char[size];
        }
        char[] r = new char[(size + 1)];
        r[0] = '-';
        return r;
    }

    private void fillDecimal(int precision, char[] digits, int nDigits, int exp, boolean isNegative) {
        int startIndex = isNegative;
        if (exp > 0) {
            if (nDigits < exp) {
                this.mantissa = create(isNegative, exp);
                System.arraycopy((Object) digits, 0, (Object) this.mantissa, startIndex, nDigits);
                Arrays.fill(this.mantissa, startIndex + nDigits, startIndex + exp, '0');
                return;
            }
            int t = Math.min(nDigits - exp, precision);
            this.mantissa = create(isNegative, (t > 0 ? t + 1 : 0) + exp);
            System.arraycopy((Object) digits, 0, (Object) this.mantissa, startIndex, exp);
            if (t > 0) {
                this.mantissa[startIndex + exp] = '.';
                System.arraycopy((Object) digits, exp, (Object) this.mantissa, startIndex + exp + 1, t);
            }
        } else if (exp <= 0) {
            int zeros = Math.max(0, Math.min(-exp, precision));
            int t2 = Math.max(0, Math.min(nDigits, precision + exp));
            if (zeros > 0) {
                this.mantissa = create(isNegative, zeros + 2 + t2);
                this.mantissa[startIndex] = '0';
                this.mantissa[startIndex + 1] = '.';
                Arrays.fill(this.mantissa, startIndex + 2, startIndex + 2 + zeros, '0');
                if (t2 > 0) {
                    System.arraycopy((Object) digits, 0, (Object) this.mantissa, startIndex + 2 + zeros, t2);
                }
            } else if (t2 > 0) {
                this.mantissa = create(isNegative, zeros + 2 + t2);
                this.mantissa[startIndex] = '0';
                this.mantissa[startIndex + 1] = '.';
                System.arraycopy((Object) digits, 0, (Object) this.mantissa, startIndex + 2, t2);
            } else {
                this.mantissa = create(isNegative, 1);
                this.mantissa[startIndex] = '0';
            }
        }
    }

    private void fillScientific(int precision, char[] digits, int nDigits, int exp, boolean isNegative) {
        int e;
        char expSign;
        char[] cArr = digits;
        int i = exp;
        boolean z = isNegative;
        int startIndex = z;
        int t = Math.max(0, Math.min(nDigits - 1, precision));
        if (t > 0) {
            this.mantissa = create(z, t + 2);
            this.mantissa[startIndex] = cArr[0];
            this.mantissa[startIndex + 1] = '.';
            System.arraycopy((Object) cArr, 1, (Object) this.mantissa, startIndex + 2, t);
        } else {
            this.mantissa = create(z, 1);
            this.mantissa[startIndex] = cArr[0];
        }
        if (i <= 0) {
            expSign = '-';
            e = (-i) + 1;
        } else {
            expSign = '+';
            e = i - 1;
        }
        if (e <= 9) {
            this.exponent = new char[]{expSign, '0', (char) (e + 48)};
        } else if (e <= 99) {
            this.exponent = new char[]{expSign, (char) ((e / 10) + 48), (char) ((e % 10) + 48)};
        } else {
            int e2 = e % 100;
            this.exponent = new char[]{expSign, (char) ((e / 100) + 48), (char) ((e2 / 10) + 48), (char) ((e2 % 10) + 48)};
        }
    }
}
