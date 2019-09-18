package java.time.format;

import java.text.DecimalFormatSymbols;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class DecimalStyle {
    private static final ConcurrentMap<Locale, DecimalStyle> CACHE = new ConcurrentHashMap(16, 0.75f, 2);
    public static final DecimalStyle STANDARD = new DecimalStyle('0', '+', '-', '.');
    private final char decimalSeparator;
    private final char negativeSign;
    private final char positiveSign;
    private final char zeroDigit;

    public static Set<Locale> getAvailableLocales() {
        Locale[] l = DecimalFormatSymbols.getAvailableLocales();
        Set<Locale> locales = new HashSet<>(l.length);
        Collections.addAll(locales, l);
        return locales;
    }

    public static DecimalStyle ofDefaultLocale() {
        return of(Locale.getDefault(Locale.Category.FORMAT));
    }

    public static DecimalStyle of(Locale locale) {
        Objects.requireNonNull(locale, "locale");
        DecimalStyle info = CACHE.get(locale);
        if (info != null) {
            return info;
        }
        CACHE.putIfAbsent(locale, create(locale));
        return CACHE.get(locale);
    }

    private static DecimalStyle create(Locale locale) {
        DecimalFormatSymbols oldSymbols = DecimalFormatSymbols.getInstance(locale);
        char zeroDigit2 = oldSymbols.getZeroDigit();
        char negativeSign2 = oldSymbols.getMinusSign();
        char decimalSeparator2 = oldSymbols.getDecimalSeparator();
        if (zeroDigit2 == '0' && negativeSign2 == '-' && decimalSeparator2 == '.') {
            return STANDARD;
        }
        return new DecimalStyle(zeroDigit2, '+', negativeSign2, decimalSeparator2);
    }

    private DecimalStyle(char zeroChar, char positiveSignChar, char negativeSignChar, char decimalPointChar) {
        this.zeroDigit = zeroChar;
        this.positiveSign = positiveSignChar;
        this.negativeSign = negativeSignChar;
        this.decimalSeparator = decimalPointChar;
    }

    public char getZeroDigit() {
        return this.zeroDigit;
    }

    public DecimalStyle withZeroDigit(char zeroDigit2) {
        if (zeroDigit2 == this.zeroDigit) {
            return this;
        }
        return new DecimalStyle(zeroDigit2, this.positiveSign, this.negativeSign, this.decimalSeparator);
    }

    public char getPositiveSign() {
        return this.positiveSign;
    }

    public DecimalStyle withPositiveSign(char positiveSign2) {
        if (positiveSign2 == this.positiveSign) {
            return this;
        }
        return new DecimalStyle(this.zeroDigit, positiveSign2, this.negativeSign, this.decimalSeparator);
    }

    public char getNegativeSign() {
        return this.negativeSign;
    }

    public DecimalStyle withNegativeSign(char negativeSign2) {
        if (negativeSign2 == this.negativeSign) {
            return this;
        }
        return new DecimalStyle(this.zeroDigit, this.positiveSign, negativeSign2, this.decimalSeparator);
    }

    public char getDecimalSeparator() {
        return this.decimalSeparator;
    }

    public DecimalStyle withDecimalSeparator(char decimalSeparator2) {
        if (decimalSeparator2 == this.decimalSeparator) {
            return this;
        }
        return new DecimalStyle(this.zeroDigit, this.positiveSign, this.negativeSign, decimalSeparator2);
    }

    /* access modifiers changed from: package-private */
    public int convertToDigit(char ch) {
        int val = ch - this.zeroDigit;
        if (val < 0 || val > 9) {
            return -1;
        }
        return val;
    }

    /* access modifiers changed from: package-private */
    public String convertNumberToI18N(String numericText) {
        if (this.zeroDigit == '0') {
            return numericText;
        }
        int diff = this.zeroDigit - '0';
        char[] array = numericText.toCharArray();
        for (int i = 0; i < array.length; i++) {
            array[i] = (char) (array[i] + diff);
        }
        return new String(array);
    }

    public boolean equals(Object obj) {
        boolean z = true;
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof DecimalStyle)) {
            return false;
        }
        DecimalStyle other = (DecimalStyle) obj;
        if (!(this.zeroDigit == other.zeroDigit && this.positiveSign == other.positiveSign && this.negativeSign == other.negativeSign && this.decimalSeparator == other.decimalSeparator)) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        return this.zeroDigit + this.positiveSign + this.negativeSign + this.decimalSeparator;
    }

    public String toString() {
        return "DecimalStyle[" + this.zeroDigit + this.positiveSign + this.negativeSign + this.decimalSeparator + "]";
    }
}
