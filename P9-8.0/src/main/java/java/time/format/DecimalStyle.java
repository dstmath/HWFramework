package java.time.format;

import java.text.DecimalFormatSymbols;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Locale.Category;
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
        Set<Locale> locales = new HashSet(l.length);
        Collections.addAll(locales, l);
        return locales;
    }

    public static DecimalStyle ofDefaultLocale() {
        return of(Locale.getDefault(Category.FORMAT));
    }

    public static DecimalStyle of(Locale locale) {
        Objects.requireNonNull((Object) locale, "locale");
        DecimalStyle info = (DecimalStyle) CACHE.get(locale);
        if (info != null) {
            return info;
        }
        CACHE.putIfAbsent(locale, create(locale));
        return (DecimalStyle) CACHE.get(locale);
    }

    private static DecimalStyle create(Locale locale) {
        DecimalFormatSymbols oldSymbols = DecimalFormatSymbols.getInstance(locale);
        char zeroDigit = oldSymbols.getZeroDigit();
        char negativeSign = oldSymbols.getMinusSign();
        char decimalSeparator = oldSymbols.getDecimalSeparator();
        if (zeroDigit == '0' && negativeSign == '-' && decimalSeparator == '.') {
            return STANDARD;
        }
        return new DecimalStyle(zeroDigit, '+', negativeSign, decimalSeparator);
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

    public DecimalStyle withZeroDigit(char zeroDigit) {
        if (zeroDigit == this.zeroDigit) {
            return this;
        }
        return new DecimalStyle(zeroDigit, this.positiveSign, this.negativeSign, this.decimalSeparator);
    }

    public char getPositiveSign() {
        return this.positiveSign;
    }

    public DecimalStyle withPositiveSign(char positiveSign) {
        if (positiveSign == this.positiveSign) {
            return this;
        }
        return new DecimalStyle(this.zeroDigit, positiveSign, this.negativeSign, this.decimalSeparator);
    }

    public char getNegativeSign() {
        return this.negativeSign;
    }

    public DecimalStyle withNegativeSign(char negativeSign) {
        if (negativeSign == this.negativeSign) {
            return this;
        }
        return new DecimalStyle(this.zeroDigit, this.positiveSign, negativeSign, this.decimalSeparator);
    }

    public char getDecimalSeparator() {
        return this.decimalSeparator;
    }

    public DecimalStyle withDecimalSeparator(char decimalSeparator) {
        if (decimalSeparator == this.decimalSeparator) {
            return this;
        }
        return new DecimalStyle(this.zeroDigit, this.positiveSign, this.negativeSign, decimalSeparator);
    }

    int convertToDigit(char ch) {
        int val = ch - this.zeroDigit;
        return (val < 0 || val > 9) ? -1 : val;
    }

    String convertNumberToI18N(String numericText) {
        if (this.zeroDigit == '0') {
            return numericText;
        }
        int diff = this.zeroDigit - 48;
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
        if (this.zeroDigit != other.zeroDigit || this.positiveSign != other.positiveSign || this.negativeSign != other.negativeSign) {
            z = false;
        } else if (this.decimalSeparator != other.decimalSeparator) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        return ((this.zeroDigit + this.positiveSign) + this.negativeSign) + this.decimalSeparator;
    }

    public String toString() {
        return "DecimalStyle[" + this.zeroDigit + this.positiveSign + this.negativeSign + this.decimalSeparator + "]";
    }
}
