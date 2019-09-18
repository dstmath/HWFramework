package android.icu.impl;

import android.icu.impl.locale.LanguageTag;
import android.icu.lang.UCharacter;
import android.icu.text.NumberFormat;
import android.icu.util.ULocale;
import android.icu.util.UResourceBundle;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.util.Arrays;
import java.util.MissingResourceException;

public final class DateNumberFormat extends NumberFormat {
    private static SimpleCache<ULocale, char[]> CACHE = new SimpleCache<>();
    private static final int DECIMAL_BUF_SIZE = 20;
    private static final long PARSE_THRESHOLD = 922337203685477579L;
    private static final long serialVersionUID = -6315692826916346953L;
    private transient char[] decimalBuf = new char[20];
    private char[] digits;
    private int maxIntDigits;
    private int minIntDigits;
    private char minusSign;
    private boolean positiveOnly = false;
    private char zeroDigit;

    public DateNumberFormat(ULocale loc, String digitString, String nsName) {
        if (digitString.length() <= 10) {
            initialize(loc, digitString, nsName);
            return;
        }
        throw new UnsupportedOperationException("DateNumberFormat does not support digits out of BMP.");
    }

    public DateNumberFormat(ULocale loc, char zeroDigit2, String nsName) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < 10; i++) {
            buf.append((char) (zeroDigit2 + i));
        }
        initialize(loc, buf.toString(), nsName);
    }

    private void initialize(ULocale loc, String digitString, String nsName) {
        String minusString;
        String str;
        char[] elems = CACHE.get(loc);
        if (elems == null) {
            ICUResourceBundle rb = (ICUResourceBundle) UResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME, loc);
            try {
                minusString = rb.getStringWithFallback("NumberElements/" + nsName + "/symbols/minusSign");
            } catch (MissingResourceException e) {
                if (!nsName.equals("latn")) {
                    try {
                        str = rb.getStringWithFallback("NumberElements/latn/symbols/minusSign");
                    } catch (MissingResourceException e2) {
                        str = LanguageTag.SEP;
                    }
                    minusString = str;
                } else {
                    minusString = LanguageTag.SEP;
                }
            }
            elems = new char[11];
            for (int i = 0; i < 10; i++) {
                elems[i] = digitString.charAt(i);
            }
            elems[10] = minusString.charAt(0);
            CACHE.put(loc, elems);
        }
        this.digits = new char[10];
        System.arraycopy(elems, 0, this.digits, 0, 10);
        this.zeroDigit = this.digits[0];
        this.minusSign = elems[10];
    }

    public void setMaximumIntegerDigits(int newValue) {
        this.maxIntDigits = newValue;
    }

    public int getMaximumIntegerDigits() {
        return this.maxIntDigits;
    }

    public void setMinimumIntegerDigits(int newValue) {
        this.minIntDigits = newValue;
    }

    public int getMinimumIntegerDigits() {
        return this.minIntDigits;
    }

    public void setParsePositiveOnly(boolean isPositiveOnly) {
        this.positiveOnly = isPositiveOnly;
    }

    public char getZeroDigit() {
        return this.zeroDigit;
    }

    public void setZeroDigit(char zero) {
        this.zeroDigit = zero;
        if (this.digits == null) {
            this.digits = new char[10];
        }
        this.digits[0] = zero;
        for (int i = 1; i < 10; i++) {
            this.digits[i] = (char) (zero + i);
        }
    }

    public char[] getDigits() {
        return (char[]) this.digits.clone();
    }

    public StringBuffer format(double number, StringBuffer toAppendTo, FieldPosition pos) {
        throw new UnsupportedOperationException("StringBuffer format(double, StringBuffer, FieldPostion) is not implemented");
    }

    public StringBuffer format(long numberL, StringBuffer toAppendTo, FieldPosition pos) {
        if (numberL < 0) {
            toAppendTo.append(this.minusSign);
            numberL = -numberL;
        }
        int number = (int) numberL;
        int limit = this.decimalBuf.length < this.maxIntDigits ? this.decimalBuf.length : this.maxIntDigits;
        int index = limit - 1;
        while (true) {
            this.decimalBuf[index] = this.digits[number % 10];
            number /= 10;
            if (index == 0 || number == 0) {
            } else {
                index--;
            }
        }
        for (int padding = this.minIntDigits - (limit - index); padding > 0; padding--) {
            index--;
            this.decimalBuf[index] = this.digits[0];
        }
        int length = limit - index;
        toAppendTo.append(this.decimalBuf, index, length);
        pos.setBeginIndex(0);
        if (pos.getField() == 0) {
            pos.setEndIndex(length);
        } else {
            pos.setEndIndex(0);
        }
        return toAppendTo;
    }

    public StringBuffer format(BigInteger number, StringBuffer toAppendTo, FieldPosition pos) {
        throw new UnsupportedOperationException("StringBuffer format(BigInteger, StringBuffer, FieldPostion) is not implemented");
    }

    public StringBuffer format(BigDecimal number, StringBuffer toAppendTo, FieldPosition pos) {
        throw new UnsupportedOperationException("StringBuffer format(BigDecimal, StringBuffer, FieldPostion) is not implemented");
    }

    public StringBuffer format(android.icu.math.BigDecimal number, StringBuffer toAppendTo, FieldPosition pos) {
        throw new UnsupportedOperationException("StringBuffer format(BigDecimal, StringBuffer, FieldPostion) is not implemented");
    }

    public Number parse(String text, ParsePosition parsePosition) {
        int digit;
        boolean sawNumber = false;
        boolean negative = false;
        int base = parsePosition.getIndex();
        long num = 0;
        int offset = 0;
        while (base + offset < text.length()) {
            char ch = text.charAt(base + offset);
            if (offset != 0 || ch != this.minusSign) {
                int digit2 = ch - this.digits[0];
                if (digit2 < 0 || 9 < digit2) {
                    digit2 = UCharacter.digit(ch);
                }
                if (digit < 0 || 9 < digit) {
                    digit = 0;
                    while (digit < 10 && ch != this.digits[digit]) {
                        digit++;
                    }
                }
                if (digit < 0 || digit > 9 || num >= PARSE_THRESHOLD) {
                    break;
                }
                sawNumber = true;
                num = (10 * num) + ((long) digit);
            } else if (this.positiveOnly) {
                break;
            } else {
                negative = true;
            }
            offset++;
        }
        if (!sawNumber) {
            return null;
        }
        Number result = Long.valueOf(negative ? -1 * num : num);
        parsePosition.setIndex(base + offset);
        return result;
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (obj == null || !super.equals(obj) || !(obj instanceof DateNumberFormat)) {
            return false;
        }
        DateNumberFormat other = (DateNumberFormat) obj;
        if (this.maxIntDigits == other.maxIntDigits && this.minIntDigits == other.minIntDigits && this.minusSign == other.minusSign && this.positiveOnly == other.positiveOnly && Arrays.equals(this.digits, other.digits)) {
            z = true;
        }
        return z;
    }

    public int hashCode() {
        return super.hashCode();
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        if (this.digits == null) {
            setZeroDigit(this.zeroDigit);
        }
        this.decimalBuf = new char[20];
    }

    public Object clone() {
        DateNumberFormat dnfmt = (DateNumberFormat) super.clone();
        dnfmt.digits = (char[]) this.digits.clone();
        dnfmt.decimalBuf = new char[20];
        return dnfmt;
    }
}
