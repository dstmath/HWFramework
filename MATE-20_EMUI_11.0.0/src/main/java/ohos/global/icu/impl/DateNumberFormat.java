package ohos.global.icu.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.util.Arrays;
import java.util.MissingResourceException;
import ohos.global.icu.impl.locale.LanguageTag;
import ohos.global.icu.lang.UCharacter;
import ohos.global.icu.text.NumberFormat;
import ohos.global.icu.util.ULocale;
import ohos.global.icu.util.UResourceBundle;

public final class DateNumberFormat extends NumberFormat {
    private static SimpleCache<ULocale, char[]> CACHE = new SimpleCache<>();
    private static final int DECIMAL_BUF_SIZE = 20;
    private static final long PARSE_THRESHOLD = 922337203685477579L;
    private static final long serialVersionUID = -6315692826916346953L;
    private transient char[] decimalBuf;
    private char[] digits;
    private int maxIntDigits;
    private int minIntDigits;
    private char minusSign;
    private boolean positiveOnly;
    private char zeroDigit;

    public DateNumberFormat(ULocale uLocale, String str, String str2) {
        this.positiveOnly = false;
        this.decimalBuf = new char[20];
        if (str.length() <= 10) {
            initialize(uLocale, str, str2);
            return;
        }
        throw new UnsupportedOperationException("DateNumberFormat does not support digits out of BMP.");
    }

    public DateNumberFormat(ULocale uLocale, char c, String str) {
        this.positiveOnly = false;
        this.decimalBuf = new char[20];
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < 10; i++) {
            stringBuffer.append((char) (c + i));
        }
        initialize(uLocale, stringBuffer.toString(), str);
    }

    private void initialize(ULocale uLocale, String str, String str2) {
        String str3 = LanguageTag.SEP;
        char[] cArr = CACHE.get(uLocale);
        if (cArr == null) {
            ICUResourceBundle bundleInstance = UResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME, uLocale);
            try {
                str3 = bundleInstance.getStringWithFallback("NumberElements/" + str2 + "/symbols/minusSign");
            } catch (MissingResourceException unused) {
                if (!str2.equals("latn")) {
                    try {
                        str3 = bundleInstance.getStringWithFallback("NumberElements/latn/symbols/minusSign");
                    } catch (MissingResourceException unused2) {
                    }
                }
            }
            cArr = new char[11];
            for (int i = 0; i < 10; i++) {
                cArr[i] = str.charAt(i);
            }
            cArr[10] = str3.charAt(0);
            CACHE.put(uLocale, cArr);
        }
        this.digits = new char[10];
        System.arraycopy(cArr, 0, this.digits, 0, 10);
        this.zeroDigit = this.digits[0];
        this.minusSign = cArr[10];
    }

    @Override // ohos.global.icu.text.NumberFormat
    public void setMaximumIntegerDigits(int i) {
        this.maxIntDigits = i;
    }

    @Override // ohos.global.icu.text.NumberFormat
    public int getMaximumIntegerDigits() {
        return this.maxIntDigits;
    }

    @Override // ohos.global.icu.text.NumberFormat
    public void setMinimumIntegerDigits(int i) {
        this.minIntDigits = i;
    }

    @Override // ohos.global.icu.text.NumberFormat
    public int getMinimumIntegerDigits() {
        return this.minIntDigits;
    }

    public void setParsePositiveOnly(boolean z) {
        this.positiveOnly = z;
    }

    public char getZeroDigit() {
        return this.zeroDigit;
    }

    public void setZeroDigit(char c) {
        this.zeroDigit = c;
        if (this.digits == null) {
            this.digits = new char[10];
        }
        this.digits[0] = c;
        for (int i = 1; i < 10; i++) {
            this.digits[i] = (char) (c + i);
        }
    }

    public char[] getDigits() {
        return (char[]) this.digits.clone();
    }

    @Override // ohos.global.icu.text.NumberFormat
    public StringBuffer format(double d, StringBuffer stringBuffer, FieldPosition fieldPosition) {
        throw new UnsupportedOperationException("StringBuffer format(double, StringBuffer, FieldPostion) is not implemented");
    }

    @Override // ohos.global.icu.text.NumberFormat
    public StringBuffer format(long j, StringBuffer stringBuffer, FieldPosition fieldPosition) {
        if (j < 0) {
            stringBuffer.append(this.minusSign);
            j = -j;
        }
        int i = (int) j;
        char[] cArr = this.decimalBuf;
        int length = cArr.length;
        int i2 = this.maxIntDigits;
        if (length < i2) {
            i2 = cArr.length;
        }
        int i3 = i2 - 1;
        while (true) {
            this.decimalBuf[i3] = this.digits[i % 10];
            i /= 10;
            if (i3 == 0 || i == 0) {
                break;
            }
            i3--;
        }
        for (int i4 = this.minIntDigits - (i2 - i3); i4 > 0; i4--) {
            i3--;
            this.decimalBuf[i3] = this.digits[0];
        }
        int i5 = i2 - i3;
        stringBuffer.append(this.decimalBuf, i3, i5);
        fieldPosition.setBeginIndex(0);
        if (fieldPosition.getField() == 0) {
            fieldPosition.setEndIndex(i5);
        } else {
            fieldPosition.setEndIndex(0);
        }
        return stringBuffer;
    }

    @Override // ohos.global.icu.text.NumberFormat
    public StringBuffer format(BigInteger bigInteger, StringBuffer stringBuffer, FieldPosition fieldPosition) {
        throw new UnsupportedOperationException("StringBuffer format(BigInteger, StringBuffer, FieldPostion) is not implemented");
    }

    @Override // ohos.global.icu.text.NumberFormat
    public StringBuffer format(BigDecimal bigDecimal, StringBuffer stringBuffer, FieldPosition fieldPosition) {
        throw new UnsupportedOperationException("StringBuffer format(BigDecimal, StringBuffer, FieldPostion) is not implemented");
    }

    @Override // ohos.global.icu.text.NumberFormat
    public StringBuffer format(ohos.global.icu.math.BigDecimal bigDecimal, StringBuffer stringBuffer, FieldPosition fieldPosition) {
        throw new UnsupportedOperationException("StringBuffer format(BigDecimal, StringBuffer, FieldPostion) is not implemented");
    }

    @Override // ohos.global.icu.text.NumberFormat
    public Number parse(String str, ParsePosition parsePosition) {
        int i;
        int index = parsePosition.getIndex();
        boolean z = false;
        boolean z2 = false;
        long j = 0;
        int i2 = 0;
        while (true) {
            i = index + i2;
            if (i >= str.length()) {
                break;
            }
            char charAt = str.charAt(i);
            if (i2 != 0 || charAt != this.minusSign) {
                int i3 = charAt - this.digits[0];
                if (i3 < 0 || 9 < i3) {
                    i3 = UCharacter.digit(charAt);
                }
                if (i3 < 0 || 9 < i3) {
                    i3 = 0;
                    while (i3 < 10 && charAt != this.digits[i3]) {
                        i3++;
                    }
                }
                if (i3 < 0 || i3 > 9 || j >= PARSE_THRESHOLD) {
                    break;
                }
                j = (j * 10) + ((long) i3);
                z = true;
            } else if (this.positiveOnly) {
                break;
            } else {
                z2 = true;
            }
            i2++;
        }
        if (!z) {
            return null;
        }
        if (z2) {
            j *= -1;
        }
        Long valueOf = Long.valueOf(j);
        parsePosition.setIndex(i);
        return valueOf;
    }

    @Override // ohos.global.icu.text.NumberFormat, java.lang.Object
    public boolean equals(Object obj) {
        if (obj == null || !super.equals(obj) || !(obj instanceof DateNumberFormat)) {
            return false;
        }
        DateNumberFormat dateNumberFormat = (DateNumberFormat) obj;
        if (this.maxIntDigits == dateNumberFormat.maxIntDigits && this.minIntDigits == dateNumberFormat.minIntDigits && this.minusSign == dateNumberFormat.minusSign && this.positiveOnly == dateNumberFormat.positiveOnly && Arrays.equals(this.digits, dateNumberFormat.digits)) {
            return true;
        }
        return false;
    }

    @Override // ohos.global.icu.text.NumberFormat, java.lang.Object
    public int hashCode() {
        return super.hashCode();
    }

    private void readObject(ObjectInputStream objectInputStream) throws IOException, ClassNotFoundException {
        objectInputStream.defaultReadObject();
        if (this.digits == null) {
            setZeroDigit(this.zeroDigit);
        }
        this.decimalBuf = new char[20];
    }

    @Override // ohos.global.icu.text.NumberFormat, java.text.Format, java.lang.Object
    public Object clone() {
        DateNumberFormat dateNumberFormat = (DateNumberFormat) super.clone();
        dateNumberFormat.digits = (char[]) this.digits.clone();
        dateNumberFormat.decimalBuf = new char[20];
        return dateNumberFormat;
    }
}
