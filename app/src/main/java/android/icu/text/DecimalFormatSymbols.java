package android.icu.text;

import android.icu.impl.CurrencyData;
import android.icu.impl.CurrencyData.CurrencyDisplayInfo;
import android.icu.impl.CurrencyData.CurrencyFormatInfo;
import android.icu.impl.CurrencyData.CurrencySpacingInfo;
import android.icu.impl.ICUResourceBundle;
import android.icu.impl.SoftCache;
import android.icu.impl.locale.LanguageTag;
import android.icu.util.Currency;
import android.icu.util.ICUCloneNotSupportedException;
import android.icu.util.ULocale;
import android.icu.util.ULocale.Category;
import android.icu.util.ULocale.Type;
import android.icu.util.UResourceBundle;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.text.ChoiceFormat;
import java.util.Arrays;
import java.util.Locale;
import java.util.MissingResourceException;

public class DecimalFormatSymbols implements Cloneable, Serializable {
    public static final int CURRENCY_SPC_CURRENCY_MATCH = 0;
    public static final int CURRENCY_SPC_INSERT = 2;
    public static final int CURRENCY_SPC_SURROUNDING_MATCH = 1;
    private static final SoftCache<ULocale, CacheData, Void> cachedLocaleData = null;
    private static final int currentSerialVersion = 8;
    private static final long serialVersionUID = 5772796243397350300L;
    private String NaN;
    private ULocale actualLocale;
    private transient Currency currency;
    private String currencyPattern;
    private String[] currencySpcAfterSym;
    private String[] currencySpcBeforeSym;
    private String currencySymbol;
    private char decimalSeparator;
    private char digit;
    private char[] digits;
    private String exponentMultiplicationSign;
    private String exponentSeparator;
    private char exponential;
    private char groupingSeparator;
    private String infinity;
    private String intlCurrencySymbol;
    private char minusSign;
    private String minusString;
    private char monetaryGroupingSeparator;
    private char monetarySeparator;
    private char padEscape;
    private char patternSeparator;
    private char perMill;
    private char percent;
    private char plusSign;
    private String plusString;
    private Locale requestedLocale;
    private int serialVersionOnStream;
    private char sigDigit;
    private ULocale ulocale;
    private ULocale validLocale;
    private char zeroDigit;

    private static class CacheData {
        public final char[] digits;
        public final String[] symbolsArray;

        public CacheData(char[] digits, String[] symbolsArray) {
            this.digits = digits;
            this.symbolsArray = symbolsArray;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.DecimalFormatSymbols.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.DecimalFormatSymbols.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.icu.text.DecimalFormatSymbols.<clinit>():void");
    }

    public DecimalFormatSymbols() {
        this.minusString = null;
        this.plusString = null;
        this.exponentMultiplicationSign = null;
        this.serialVersionOnStream = currentSerialVersion;
        this.currencyPattern = null;
        initialize(ULocale.getDefault(Category.FORMAT));
    }

    public DecimalFormatSymbols(Locale locale) {
        this.minusString = null;
        this.plusString = null;
        this.exponentMultiplicationSign = null;
        this.serialVersionOnStream = currentSerialVersion;
        this.currencyPattern = null;
        initialize(ULocale.forLocale(locale));
    }

    public DecimalFormatSymbols(ULocale locale) {
        this.minusString = null;
        this.plusString = null;
        this.exponentMultiplicationSign = null;
        this.serialVersionOnStream = currentSerialVersion;
        this.currencyPattern = null;
        initialize(locale);
    }

    public static DecimalFormatSymbols getInstance() {
        return new DecimalFormatSymbols();
    }

    public static DecimalFormatSymbols getInstance(Locale locale) {
        return new DecimalFormatSymbols(locale);
    }

    public static DecimalFormatSymbols getInstance(ULocale locale) {
        return new DecimalFormatSymbols(locale);
    }

    public static Locale[] getAvailableLocales() {
        return ICUResourceBundle.getAvailableLocales();
    }

    public static ULocale[] getAvailableULocales() {
        return ICUResourceBundle.getAvailableULocales();
    }

    public char getZeroDigit() {
        if (this.digits != null) {
            return this.digits[CURRENCY_SPC_CURRENCY_MATCH];
        }
        return this.zeroDigit;
    }

    public char[] getDigits() {
        if (this.digits != null) {
            return (char[]) this.digits.clone();
        }
        char[] digitArray = new char[10];
        for (int i = CURRENCY_SPC_CURRENCY_MATCH; i < 10; i += CURRENCY_SPC_SURROUNDING_MATCH) {
            digitArray[i] = (char) (this.zeroDigit + i);
        }
        return digitArray;
    }

    char[] getDigitsLocal() {
        if (this.digits != null) {
            return this.digits;
        }
        char[] digitArray = new char[10];
        for (int i = CURRENCY_SPC_CURRENCY_MATCH; i < 10; i += CURRENCY_SPC_SURROUNDING_MATCH) {
            digitArray[i] = (char) (this.zeroDigit + i);
        }
        return digitArray;
    }

    public void setZeroDigit(char zeroDigit) {
        if (this.digits != null) {
            this.digits[CURRENCY_SPC_CURRENCY_MATCH] = zeroDigit;
            for (int i = CURRENCY_SPC_SURROUNDING_MATCH; i < 10; i += CURRENCY_SPC_SURROUNDING_MATCH) {
                this.digits[i] = (char) (zeroDigit + i);
            }
            return;
        }
        this.zeroDigit = zeroDigit;
    }

    public char getSignificantDigit() {
        return this.sigDigit;
    }

    public void setSignificantDigit(char sigDigit) {
        this.sigDigit = sigDigit;
    }

    public char getGroupingSeparator() {
        return this.groupingSeparator;
    }

    public void setGroupingSeparator(char groupingSeparator) {
        this.groupingSeparator = groupingSeparator;
    }

    public char getDecimalSeparator() {
        return this.decimalSeparator;
    }

    public void setDecimalSeparator(char decimalSeparator) {
        this.decimalSeparator = decimalSeparator;
    }

    public char getPerMill() {
        return this.perMill;
    }

    public void setPerMill(char perMill) {
        this.perMill = perMill;
    }

    public char getPercent() {
        return this.percent;
    }

    public void setPercent(char percent) {
        this.percent = percent;
    }

    public char getDigit() {
        return this.digit;
    }

    public void setDigit(char digit) {
        this.digit = digit;
    }

    public char getPatternSeparator() {
        return this.patternSeparator;
    }

    public void setPatternSeparator(char patternSeparator) {
        this.patternSeparator = patternSeparator;
    }

    public String getInfinity() {
        return this.infinity;
    }

    public void setInfinity(String infinity) {
        this.infinity = infinity;
    }

    public String getNaN() {
        return this.NaN;
    }

    public void setNaN(String NaN) {
        this.NaN = NaN;
    }

    public char getMinusSign() {
        return this.minusSign;
    }

    @Deprecated
    public String getMinusString() {
        return this.minusString;
    }

    public void setMinusSign(char minusSign) {
        this.minusSign = minusSign;
        char[] minusArray = new char[CURRENCY_SPC_SURROUNDING_MATCH];
        minusArray[CURRENCY_SPC_CURRENCY_MATCH] = minusSign;
        this.minusString = new String(minusArray);
    }

    public String getCurrencySymbol() {
        return this.currencySymbol;
    }

    public void setCurrencySymbol(String currency) {
        this.currencySymbol = currency;
    }

    public String getInternationalCurrencySymbol() {
        return this.intlCurrencySymbol;
    }

    public void setInternationalCurrencySymbol(String currency) {
        this.intlCurrencySymbol = currency;
    }

    public Currency getCurrency() {
        return this.currency;
    }

    public void setCurrency(Currency currency) {
        if (currency == null) {
            throw new NullPointerException();
        }
        this.currency = currency;
        this.intlCurrencySymbol = currency.getCurrencyCode();
        this.currencySymbol = currency.getSymbol(this.requestedLocale);
    }

    public char getMonetaryDecimalSeparator() {
        return this.monetarySeparator;
    }

    public char getMonetaryGroupingSeparator() {
        return this.monetaryGroupingSeparator;
    }

    String getCurrencyPattern() {
        return this.currencyPattern;
    }

    public void setMonetaryDecimalSeparator(char sep) {
        this.monetarySeparator = sep;
    }

    public void setMonetaryGroupingSeparator(char sep) {
        this.monetaryGroupingSeparator = sep;
    }

    public String getExponentMultiplicationSign() {
        return this.exponentMultiplicationSign;
    }

    public void setExponentMultiplicationSign(String exponentMultiplicationSign) {
        this.exponentMultiplicationSign = exponentMultiplicationSign;
    }

    public String getExponentSeparator() {
        return this.exponentSeparator;
    }

    public void setExponentSeparator(String exp) {
        this.exponentSeparator = exp;
    }

    public char getPlusSign() {
        return this.plusSign;
    }

    @Deprecated
    public String getPlusString() {
        return this.plusString;
    }

    public void setPlusSign(char plus) {
        this.plusSign = plus;
        char[] plusArray = new char[CURRENCY_SPC_SURROUNDING_MATCH];
        plusArray[CURRENCY_SPC_CURRENCY_MATCH] = this.plusSign;
        this.plusString = new String(plusArray);
    }

    public char getPadEscape() {
        return this.padEscape;
    }

    public void setPadEscape(char c) {
        this.padEscape = c;
    }

    public String getPatternForCurrencySpacing(int itemType, boolean beforeCurrency) {
        if (itemType < 0 || itemType > CURRENCY_SPC_INSERT) {
            throw new IllegalArgumentException("unknown currency spacing: " + itemType);
        } else if (beforeCurrency) {
            return this.currencySpcBeforeSym[itemType];
        } else {
            return this.currencySpcAfterSym[itemType];
        }
    }

    public void setPatternForCurrencySpacing(int itemType, boolean beforeCurrency, String pattern) {
        if (itemType < 0 || itemType > CURRENCY_SPC_INSERT) {
            throw new IllegalArgumentException("unknown currency spacing: " + itemType);
        } else if (beforeCurrency) {
            this.currencySpcBeforeSym[itemType] = pattern;
        } else {
            this.currencySpcAfterSym[itemType] = pattern;
        }
    }

    public Locale getLocale() {
        return this.requestedLocale;
    }

    public ULocale getULocale() {
        return this.ulocale;
    }

    public Object clone() {
        try {
            return (DecimalFormatSymbols) super.clone();
        } catch (Throwable e) {
            throw new ICUCloneNotSupportedException(e);
        }
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (!(obj instanceof DecimalFormatSymbols)) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        DecimalFormatSymbols other = (DecimalFormatSymbols) obj;
        int i = CURRENCY_SPC_CURRENCY_MATCH;
        while (i <= CURRENCY_SPC_INSERT) {
            if (!this.currencySpcBeforeSym[i].equals(other.currencySpcBeforeSym[i]) || !this.currencySpcAfterSym[i].equals(other.currencySpcAfterSym[i])) {
                return false;
            }
            i += CURRENCY_SPC_SURROUNDING_MATCH;
        }
        if (other.digits == null) {
            for (i = CURRENCY_SPC_CURRENCY_MATCH; i < 10; i += CURRENCY_SPC_SURROUNDING_MATCH) {
                if (this.digits[i] != other.zeroDigit + i) {
                    return false;
                }
            }
        } else if (!Arrays.equals(this.digits, other.digits)) {
            return false;
        }
        if (this.groupingSeparator == other.groupingSeparator && this.decimalSeparator == other.decimalSeparator && this.percent == other.percent && this.perMill == other.perMill && this.digit == other.digit && this.minusSign == other.minusSign && this.minusString.equals(other.minusString) && this.patternSeparator == other.patternSeparator && this.infinity.equals(other.infinity) && this.NaN.equals(other.NaN) && this.currencySymbol.equals(other.currencySymbol) && this.intlCurrencySymbol.equals(other.intlCurrencySymbol) && this.padEscape == other.padEscape && this.plusSign == other.plusSign && this.plusString.equals(other.plusString) && this.exponentSeparator.equals(other.exponentSeparator) && this.monetarySeparator == other.monetarySeparator && this.monetaryGroupingSeparator == other.monetaryGroupingSeparator) {
            z = this.exponentMultiplicationSign.equals(other.exponentMultiplicationSign);
        }
        return z;
    }

    public int hashCode() {
        return (((this.digits[CURRENCY_SPC_CURRENCY_MATCH] * 37) + this.groupingSeparator) * 37) + this.decimalSeparator;
    }

    private static boolean isBidiMark(char c) {
        return c == '\u200e' || c == '\u200f' || c == '\u061c';
    }

    private void initialize(ULocale locale) {
        this.requestedLocale = locale.toLocale();
        this.ulocale = locale;
        CacheData symbolData = (CacheData) cachedLocaleData.getInstance(locale, null);
        this.digits = (char[]) symbolData.digits.clone();
        String[] numberElements = symbolData.symbolsArray;
        ULocale uloc = ((ICUResourceBundle) UResourceBundle.getBundleInstance(ICUResourceBundle.ICU_BASE_NAME, locale)).getULocale();
        setLocale(uloc, uloc);
        this.decimalSeparator = numberElements[CURRENCY_SPC_CURRENCY_MATCH].charAt(CURRENCY_SPC_CURRENCY_MATCH);
        this.groupingSeparator = numberElements[CURRENCY_SPC_SURROUNDING_MATCH].charAt(CURRENCY_SPC_CURRENCY_MATCH);
        this.patternSeparator = numberElements[CURRENCY_SPC_INSERT].charAt(CURRENCY_SPC_CURRENCY_MATCH);
        this.percent = numberElements[3].charAt(numberElements[3].length() - 1);
        this.minusString = numberElements[4];
        char charAt = (this.minusString.length() <= CURRENCY_SPC_SURROUNDING_MATCH || !isBidiMark(this.minusString.charAt(CURRENCY_SPC_CURRENCY_MATCH))) ? this.minusString.charAt(CURRENCY_SPC_CURRENCY_MATCH) : this.minusString.charAt(CURRENCY_SPC_SURROUNDING_MATCH);
        this.minusSign = charAt;
        this.plusString = numberElements[5];
        charAt = (this.plusString.length() <= CURRENCY_SPC_SURROUNDING_MATCH || !isBidiMark(this.plusString.charAt(CURRENCY_SPC_CURRENCY_MATCH))) ? this.plusString.charAt(CURRENCY_SPC_CURRENCY_MATCH) : this.plusString.charAt(CURRENCY_SPC_SURROUNDING_MATCH);
        this.plusSign = charAt;
        this.exponentSeparator = numberElements[6];
        this.perMill = numberElements[7].charAt(CURRENCY_SPC_CURRENCY_MATCH);
        this.infinity = numberElements[currentSerialVersion];
        this.NaN = numberElements[9];
        if (numberElements[10] != null) {
            this.monetarySeparator = numberElements[10].charAt(CURRENCY_SPC_CURRENCY_MATCH);
        } else {
            this.monetarySeparator = this.decimalSeparator;
        }
        if (numberElements[11] != null) {
            this.monetaryGroupingSeparator = numberElements[11].charAt(CURRENCY_SPC_CURRENCY_MATCH);
        } else {
            this.monetaryGroupingSeparator = this.groupingSeparator;
        }
        if (numberElements[12] != null) {
            this.exponentMultiplicationSign = numberElements[12];
        } else {
            this.exponentMultiplicationSign = "\u00d7";
        }
        this.digit = '#';
        this.padEscape = '*';
        this.sigDigit = '@';
        CurrencyDisplayInfo info = CurrencyData.provider.getInstance(locale, true);
        this.currency = Currency.getInstance(locale);
        if (this.currency != null) {
            String format;
            this.intlCurrencySymbol = this.currency.getCurrencyCode();
            boolean[] isChoiceFormat = new boolean[CURRENCY_SPC_SURROUNDING_MATCH];
            String currname = this.currency.getName(locale, (int) CURRENCY_SPC_CURRENCY_MATCH, isChoiceFormat);
            if (isChoiceFormat[CURRENCY_SPC_CURRENCY_MATCH]) {
                format = new ChoiceFormat(currname).format(2.0d);
            } else {
                format = currname;
            }
            this.currencySymbol = format;
            CurrencyFormatInfo fmtInfo = info.getFormatInfo(this.intlCurrencySymbol);
            if (fmtInfo != null) {
                this.currencyPattern = fmtInfo.currencyPattern;
                this.monetarySeparator = fmtInfo.monetarySeparator;
                this.monetaryGroupingSeparator = fmtInfo.monetaryGroupingSeparator;
            }
        } else {
            this.intlCurrencySymbol = "XXX";
            this.currencySymbol = "\u00a4";
        }
        this.currencySpcBeforeSym = new String[3];
        this.currencySpcAfterSym = new String[3];
        initSpacingInfo(info.getSpacingInfo());
    }

    static CacheData loadSymbols(ULocale locale) {
        String nsName;
        NumberingSystem ns = NumberingSystem.getInstance(locale);
        char[] digits = new char[10];
        if (ns == null || ns.getRadix() != 10 || ns.isAlgorithmic() || !NumberingSystem.isValidDigitString(ns.getDescription())) {
            digits[CURRENCY_SPC_CURRENCY_MATCH] = '0';
            digits[CURRENCY_SPC_SURROUNDING_MATCH] = '1';
            digits[CURRENCY_SPC_INSERT] = '2';
            digits[3] = '3';
            digits[4] = '4';
            digits[5] = '5';
            digits[6] = '6';
            digits[7] = '7';
            digits[currentSerialVersion] = '8';
            digits[9] = '9';
            nsName = "latn";
        } else {
            String digitString = ns.getDescription();
            digits[CURRENCY_SPC_CURRENCY_MATCH] = digitString.charAt(CURRENCY_SPC_CURRENCY_MATCH);
            digits[CURRENCY_SPC_SURROUNDING_MATCH] = digitString.charAt(CURRENCY_SPC_SURROUNDING_MATCH);
            digits[CURRENCY_SPC_INSERT] = digitString.charAt(CURRENCY_SPC_INSERT);
            digits[3] = digitString.charAt(3);
            digits[4] = digitString.charAt(4);
            digits[5] = digitString.charAt(5);
            digits[6] = digitString.charAt(6);
            digits[7] = digitString.charAt(7);
            digits[currentSerialVersion] = digitString.charAt(currentSerialVersion);
            digits[9] = digitString.charAt(9);
            nsName = ns.getName();
        }
        ICUResourceBundle rb = (ICUResourceBundle) UResourceBundle.getBundleInstance(ICUResourceBundle.ICU_BASE_NAME, locale);
        boolean isLatn = nsName.equals("latn");
        String baseKey = "NumberElements/" + nsName + "/symbols/";
        String latnKey = "NumberElements/latn/symbols/";
        String[] symbolKeys = new String[]{"decimal", "group", "list", "percentSign", "minusSign", "plusSign", "exponential", "perMille", "infinity", "nan", "currencyDecimal", "currencyGroup", "superscriptingExponent"};
        String[] fallbackElements = new String[]{".", ",", ";", "%", LanguageTag.SEP, "+", DateFormat.ABBR_WEEKDAY, "\u2030", "\u221e", "NaN", null, null};
        String[] symbolsArray = new String[symbolKeys.length];
        for (int i = CURRENCY_SPC_CURRENCY_MATCH; i < symbolKeys.length; i += CURRENCY_SPC_SURROUNDING_MATCH) {
            try {
                symbolsArray[i] = rb.getStringWithFallback(baseKey + symbolKeys[i]);
            } catch (MissingResourceException e) {
                if (isLatn) {
                    symbolsArray[i] = fallbackElements[i];
                } else {
                    try {
                        symbolsArray[i] = rb.getStringWithFallback(latnKey + symbolKeys[i]);
                    } catch (MissingResourceException e2) {
                        symbolsArray[i] = fallbackElements[i];
                    }
                }
            }
        }
        return new CacheData(digits, symbolsArray);
    }

    private void initSpacingInfo(CurrencySpacingInfo spcInfo) {
        this.currencySpcBeforeSym[CURRENCY_SPC_CURRENCY_MATCH] = spcInfo.beforeCurrencyMatch;
        this.currencySpcBeforeSym[CURRENCY_SPC_SURROUNDING_MATCH] = spcInfo.beforeContextMatch;
        this.currencySpcBeforeSym[CURRENCY_SPC_INSERT] = spcInfo.beforeInsert;
        this.currencySpcAfterSym[CURRENCY_SPC_CURRENCY_MATCH] = spcInfo.afterCurrencyMatch;
        this.currencySpcAfterSym[CURRENCY_SPC_SURROUNDING_MATCH] = spcInfo.afterContextMatch;
        this.currencySpcAfterSym[CURRENCY_SPC_INSERT] = spcInfo.afterInsert;
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        if (this.serialVersionOnStream < CURRENCY_SPC_SURROUNDING_MATCH) {
            this.monetarySeparator = this.decimalSeparator;
            this.exponential = 'E';
        }
        if (this.serialVersionOnStream < CURRENCY_SPC_INSERT) {
            this.padEscape = '*';
            this.plusSign = '+';
            this.exponentSeparator = String.valueOf(this.exponential);
        }
        if (this.serialVersionOnStream < 3) {
            this.requestedLocale = Locale.getDefault();
        }
        if (this.serialVersionOnStream < 4) {
            this.ulocale = ULocale.forLocale(this.requestedLocale);
        }
        if (this.serialVersionOnStream < 5) {
            this.monetaryGroupingSeparator = this.groupingSeparator;
        }
        if (this.serialVersionOnStream < 6) {
            if (this.currencySpcBeforeSym == null) {
                this.currencySpcBeforeSym = new String[3];
            }
            if (this.currencySpcAfterSym == null) {
                this.currencySpcAfterSym = new String[3];
            }
            initSpacingInfo(CurrencySpacingInfo.DEFAULT);
        }
        if (this.serialVersionOnStream < 7) {
            if (this.minusString == null) {
                char[] minusArray = new char[CURRENCY_SPC_SURROUNDING_MATCH];
                minusArray[CURRENCY_SPC_CURRENCY_MATCH] = this.minusSign;
                this.minusString = new String(minusArray);
            }
            if (this.plusString == null) {
                char[] plusArray = new char[CURRENCY_SPC_SURROUNDING_MATCH];
                plusArray[CURRENCY_SPC_CURRENCY_MATCH] = this.plusSign;
                this.plusString = new String(plusArray);
            }
        }
        if (this.serialVersionOnStream < currentSerialVersion && this.exponentMultiplicationSign == null) {
            this.exponentMultiplicationSign = "\u00d7";
        }
        this.serialVersionOnStream = currentSerialVersion;
        this.currency = Currency.getInstance(this.intlCurrencySymbol);
    }

    public final ULocale getLocale(Type type) {
        return type == ULocale.ACTUAL_LOCALE ? this.actualLocale : this.validLocale;
    }

    final void setLocale(ULocale valid, ULocale actual) {
        Object obj;
        Object obj2 = CURRENCY_SPC_SURROUNDING_MATCH;
        if (valid == null) {
            obj = CURRENCY_SPC_SURROUNDING_MATCH;
        } else {
            obj = CURRENCY_SPC_CURRENCY_MATCH;
        }
        if (actual != null) {
            obj2 = CURRENCY_SPC_CURRENCY_MATCH;
        }
        if (obj != obj2) {
            throw new IllegalArgumentException();
        }
        this.validLocale = valid;
        this.actualLocale = actual;
    }
}
