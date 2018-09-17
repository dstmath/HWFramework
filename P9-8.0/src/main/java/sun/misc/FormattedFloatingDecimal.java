package sun.misc;

import java.util.Arrays;
import sun.misc.FloatingDecimal.BinaryToASCIIConverter;

public class FormattedFloatingDecimal {
    static final /* synthetic */ boolean -assertionsDisabled = (FormattedFloatingDecimal.class.desiredAssertionStatus() ^ 1);
    private static final /* synthetic */ int[] -sun-misc-FormattedFloatingDecimal$FormSwitchesValues = null;
    private static final ThreadLocal<Object> threadLocalCharBuffer = new ThreadLocal<Object>() {
        protected Object initialValue() {
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

    private static /* synthetic */ int[] -getsun-misc-FormattedFloatingDecimal$FormSwitchesValues() {
        if (-sun-misc-FormattedFloatingDecimal$FormSwitchesValues != null) {
            return -sun-misc-FormattedFloatingDecimal$FormSwitchesValues;
        }
        int[] iArr = new int[Form.values().length];
        try {
            iArr[Form.COMPATIBLE.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[Form.DECIMAL_FLOAT.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[Form.GENERAL.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[Form.SCIENTIFIC.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        -sun-misc-FormattedFloatingDecimal$FormSwitchesValues = iArr;
        return iArr;
    }

    public static FormattedFloatingDecimal valueOf(double d, int precision, Form form) {
        return new FormattedFloatingDecimal(precision, form, FloatingDecimal.getBinaryToASCIIConverter(d, form == Form.COMPATIBLE));
    }

    private static char[] getBuffer() {
        return (char[]) threadLocalCharBuffer.get();
    }

    private FormattedFloatingDecimal(int precision, Form form, BinaryToASCIIConverter fdConverter) {
        if (fdConverter.isExceptional()) {
            this.mantissa = fdConverter.toJavaFormatString().toCharArray();
            this.exponent = null;
            return;
        }
        char[] digits = getBuffer();
        int nDigits = fdConverter.getDigits(digits);
        int decExp = fdConverter.getDecimalExponent();
        boolean isNegative = fdConverter.isNegative();
        int exp;
        switch (-getsun-misc-FormattedFloatingDecimal$FormSwitchesValues()[form.ordinal()]) {
            case 1:
                exp = decExp;
                this.decExponentRounded = decExp;
                fillCompatible(precision, digits, nDigits, decExp, isNegative);
                break;
            case 2:
                exp = applyPrecision(decExp, digits, nDigits, decExp + precision);
                fillDecimal(precision, digits, nDigits, exp, isNegative);
                this.decExponentRounded = exp;
                break;
            case 3:
                exp = applyPrecision(decExp, digits, nDigits, precision);
                if (exp - 1 < -4 || exp - 1 >= precision) {
                    fillScientific(precision - 1, digits, nDigits, exp, isNegative);
                } else {
                    fillDecimal(precision - exp, digits, nDigits, exp, isNegative);
                }
                this.decExponentRounded = exp;
                break;
            case 4:
                exp = applyPrecision(decExp, digits, nDigits, precision + 1);
                fillScientific(precision, digits, nDigits, exp, isNegative);
                this.decExponentRounded = exp;
                break;
            default:
                if (!-assertionsDisabled) {
                    throw new AssertionError();
                }
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

    private static int applyPrecision(int decExp, char[] digits, int nDigits, int prec) {
        if (prec >= nDigits || prec < 0) {
            return decExp;
        }
        if (prec != 0) {
            if (digits[prec] >= 53) {
                int i = prec;
                i = prec - 1;
                int q = digits[i];
                if (q == 57) {
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
        int startIndex = isNegative ? 1 : 0;
        int t;
        if (exp <= 0 || exp >= 8) {
            if (exp > 0 || exp <= -3) {
                int e;
                int expStartIntex;
                if (nDigits > 1) {
                    this.mantissa = create(isNegative, nDigits + 1);
                    this.mantissa[startIndex] = digits[0];
                    this.mantissa[startIndex + 1] = '.';
                    System.arraycopy(digits, 1, this.mantissa, startIndex + 2, nDigits - 1);
                } else {
                    this.mantissa = create(isNegative, 3);
                    this.mantissa[startIndex] = digits[0];
                    this.mantissa[startIndex + 1] = '.';
                    this.mantissa[startIndex + 2] = '0';
                }
                boolean isNegExp = exp <= 0;
                if (isNegExp) {
                    e = (-exp) + 1;
                    expStartIntex = 1;
                } else {
                    e = exp - 1;
                    expStartIntex = 0;
                }
                if (e <= 9) {
                    this.exponent = create(isNegExp, 1);
                    this.exponent[expStartIntex] = (char) (e + 48);
                    return;
                } else if (e <= 99) {
                    this.exponent = create(isNegExp, 2);
                    this.exponent[expStartIntex] = (char) ((e / 10) + 48);
                    this.exponent[expStartIntex + 1] = (char) ((e % 10) + 48);
                    return;
                } else {
                    this.exponent = create(isNegExp, 3);
                    this.exponent[expStartIntex] = (char) ((e / 100) + 48);
                    e %= 100;
                    this.exponent[expStartIntex + 1] = (char) ((e / 10) + 48);
                    this.exponent[expStartIntex + 2] = (char) ((e % 10) + 48);
                    return;
                }
            }
            int zeros = Math.max(0, Math.min(-exp, precision));
            t = Math.max(0, Math.min(nDigits, precision + exp));
            if (zeros > 0) {
                this.mantissa = create(isNegative, (zeros + 2) + t);
                this.mantissa[startIndex] = '0';
                this.mantissa[startIndex + 1] = '.';
                Arrays.fill(this.mantissa, startIndex + 2, (startIndex + 2) + zeros, '0');
                if (t > 0) {
                    System.arraycopy(digits, 0, this.mantissa, (startIndex + 2) + zeros, t);
                }
            } else if (t > 0) {
                this.mantissa = create(isNegative, (zeros + 2) + t);
                this.mantissa[startIndex] = '0';
                this.mantissa[startIndex + 1] = '.';
                System.arraycopy(digits, 0, this.mantissa, startIndex + 2, t);
            } else {
                this.mantissa = create(isNegative, 1);
                this.mantissa[startIndex] = '0';
            }
        } else if (nDigits < exp) {
            int extraZeros = exp - nDigits;
            this.mantissa = create(isNegative, (nDigits + extraZeros) + 2);
            System.arraycopy(digits, 0, this.mantissa, startIndex, nDigits);
            Arrays.fill(this.mantissa, startIndex + nDigits, (startIndex + nDigits) + extraZeros, '0');
            this.mantissa[(startIndex + nDigits) + extraZeros] = '.';
            this.mantissa[((startIndex + nDigits) + extraZeros) + 1] = '0';
        } else if (exp < nDigits) {
            t = Math.min(nDigits - exp, precision);
            this.mantissa = create(isNegative, (exp + 1) + t);
            System.arraycopy(digits, 0, this.mantissa, startIndex, exp);
            this.mantissa[startIndex + exp] = '.';
            System.arraycopy(digits, exp, this.mantissa, (startIndex + exp) + 1, t);
        } else {
            this.mantissa = create(isNegative, nDigits + 2);
            System.arraycopy(digits, 0, this.mantissa, startIndex, nDigits);
            this.mantissa[startIndex + nDigits] = '.';
            this.mantissa[(startIndex + nDigits) + 1] = '0';
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
        int startIndex = isNegative ? 1 : 0;
        int t;
        if (exp > 0) {
            if (nDigits < exp) {
                this.mantissa = create(isNegative, exp);
                System.arraycopy(digits, 0, this.mantissa, startIndex, nDigits);
                Arrays.fill(this.mantissa, startIndex + nDigits, startIndex + exp, '0');
                return;
            }
            t = Math.min(nDigits - exp, precision);
            this.mantissa = create(isNegative, (t > 0 ? t + 1 : 0) + exp);
            System.arraycopy(digits, 0, this.mantissa, startIndex, exp);
            if (t > 0) {
                this.mantissa[startIndex + exp] = '.';
                System.arraycopy(digits, exp, this.mantissa, (startIndex + exp) + 1, t);
            }
        } else if (exp <= 0) {
            int zeros = Math.max(0, Math.min(-exp, precision));
            t = Math.max(0, Math.min(nDigits, precision + exp));
            if (zeros > 0) {
                this.mantissa = create(isNegative, (zeros + 2) + t);
                this.mantissa[startIndex] = '0';
                this.mantissa[startIndex + 1] = '.';
                Arrays.fill(this.mantissa, startIndex + 2, (startIndex + 2) + zeros, '0');
                if (t > 0) {
                    System.arraycopy(digits, 0, this.mantissa, (startIndex + 2) + zeros, t);
                }
            } else if (t > 0) {
                this.mantissa = create(isNegative, (zeros + 2) + t);
                this.mantissa[startIndex] = '0';
                this.mantissa[startIndex + 1] = '.';
                System.arraycopy(digits, 0, this.mantissa, startIndex + 2, t);
            } else {
                this.mantissa = create(isNegative, 1);
                this.mantissa[startIndex] = '0';
            }
        }
    }

    private void fillScientific(int precision, char[] digits, int nDigits, int exp, boolean isNegative) {
        char expSign;
        int e;
        int startIndex = isNegative ? 1 : 0;
        int t = Math.max(0, Math.min(nDigits - 1, precision));
        if (t > 0) {
            this.mantissa = create(isNegative, t + 2);
            this.mantissa[startIndex] = digits[0];
            this.mantissa[startIndex + 1] = '.';
            System.arraycopy(digits, 1, this.mantissa, startIndex + 2, t);
        } else {
            this.mantissa = create(isNegative, 1);
            this.mantissa[startIndex] = digits[0];
        }
        if (exp <= 0) {
            expSign = '-';
            e = (-exp) + 1;
        } else {
            expSign = '+';
            e = exp - 1;
        }
        if (e <= 9) {
            this.exponent = new char[]{expSign, '0', (char) (e + 48)};
        } else if (e <= 99) {
            this.exponent = new char[]{expSign, (char) ((e / 10) + 48), (char) ((e % 10) + 48)};
        } else {
            char hiExpChar = (char) ((e / 100) + 48);
            e %= 100;
            this.exponent = new char[]{expSign, hiExpChar, (char) ((e / 10) + 48), (char) ((e % 10) + 48)};
        }
    }
}
