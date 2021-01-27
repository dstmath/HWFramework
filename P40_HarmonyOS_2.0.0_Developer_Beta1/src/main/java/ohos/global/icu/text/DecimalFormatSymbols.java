package ohos.global.icu.text;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Locale;
import java.util.MissingResourceException;
import ohos.global.icu.impl.CacheBase;
import ohos.global.icu.impl.CurrencyData;
import ohos.global.icu.impl.ICUResourceBundle;
import ohos.global.icu.impl.SoftCache;
import ohos.global.icu.impl.UResource;
import ohos.global.icu.util.Currency;
import ohos.global.icu.util.ICUCloneNotSupportedException;
import ohos.global.icu.util.ULocale;
import ohos.global.icu.util.UResourceBundle;
import ohos.light.bean.LightEffect;
import ohos.telephony.TelephoneNumberUtils;

public class DecimalFormatSymbols implements Cloneable, Serializable {
    public static final int CURRENCY_SPC_CURRENCY_MATCH = 0;
    public static final int CURRENCY_SPC_INSERT = 2;
    public static final int CURRENCY_SPC_SURROUNDING_MATCH = 1;
    private static final char DEF_DECIMAL_SEPARATOR = '.';
    private static final char[] DEF_DIGIT_CHARS_ARRAY = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
    private static final String[] DEF_DIGIT_STRINGS_ARRAY = {LightEffect.LIGHT_ID_LED, "1", "2", "3", "4", "5", "6", "7", "8", "9"};
    private static final char DEF_GROUPING_SEPARATOR = ',';
    private static final char DEF_MINUS_SIGN = '-';
    private static final char DEF_PERCENT = '%';
    private static final char DEF_PERMILL = 8240;
    private static final char DEF_PLUS_SIGN = '+';
    private static final String LATIN_NUMBERING_SYSTEM = "latn";
    private static final String NUMBER_ELEMENTS = "NumberElements";
    private static final String SYMBOLS = "symbols";
    private static final String[] SYMBOL_DEFAULTS = {String.valueOf((char) DEF_DECIMAL_SEPARATOR), String.valueOf(','), String.valueOf((char) DEF_PERCENT), String.valueOf((char) DEF_MINUS_SIGN), String.valueOf((char) DEF_PLUS_SIGN), DateFormat.ABBR_WEEKDAY, String.valueOf((char) DEF_PERMILL), "∞", "NaN", null, null, "×"};
    private static final String[] SYMBOL_KEYS = {"decimal", "group", "percentSign", "minusSign", "plusSign", "exponential", "perMille", "infinity", "nan", "currencyDecimal", "currencyGroup", "superscriptingExponent"};
    private static final CacheBase<ULocale, CacheData, Void> cachedLocaleData = new SoftCache<ULocale, CacheData, Void>() {
        /* class ohos.global.icu.text.DecimalFormatSymbols.AnonymousClass1 */

        /* access modifiers changed from: protected */
        public CacheData createInstance(ULocale uLocale, Void r2) {
            return DecimalFormatSymbols.loadData(uLocale);
        }
    };
    private static final int currentSerialVersion = 8;
    private static final long serialVersionUID = 5772796243397350300L;
    private String NaN;
    private ULocale actualLocale;
    private transient int codePointZero;
    private transient Currency currency;
    private String currencyPattern;
    private String[] currencySpcAfterSym;
    private String[] currencySpcBeforeSym;
    private String currencySymbol;
    private char decimalSeparator;
    private String decimalSeparatorString;
    private char digit;
    private String[] digitStrings;
    private char[] digits;
    private String exponentMultiplicationSign;
    private String exponentSeparator;
    private char exponential;
    private char groupingSeparator;
    private String groupingSeparatorString;
    private String infinity;
    private String intlCurrencySymbol;
    private char minusSign;
    private String minusString;
    private char monetaryGroupingSeparator;
    private String monetaryGroupingSeparatorString;
    private char monetarySeparator;
    private String monetarySeparatorString;
    private char padEscape;
    private char patternSeparator;
    private char perMill;
    private String perMillString;
    private char percent;
    private String percentString;
    private char plusSign;
    private String plusString;
    private Locale requestedLocale;
    private int serialVersionOnStream;
    private char sigDigit;
    private ULocale ulocale;
    private ULocale validLocale;
    private char zeroDigit;

    public DecimalFormatSymbols() {
        this(ULocale.getDefault(ULocale.Category.FORMAT));
    }

    public DecimalFormatSymbols(Locale locale) {
        this(ULocale.forLocale(locale));
    }

    public DecimalFormatSymbols(ULocale uLocale) {
        this.exponentMultiplicationSign = null;
        this.serialVersionOnStream = 8;
        this.currencyPattern = null;
        initialize(uLocale, null);
    }

    private DecimalFormatSymbols(Locale locale, NumberingSystem numberingSystem) {
        this(ULocale.forLocale(locale), numberingSystem);
    }

    private DecimalFormatSymbols(ULocale uLocale, NumberingSystem numberingSystem) {
        this.exponentMultiplicationSign = null;
        this.serialVersionOnStream = 8;
        this.currencyPattern = null;
        initialize(uLocale, numberingSystem);
    }

    public static DecimalFormatSymbols getInstance() {
        return new DecimalFormatSymbols();
    }

    public static DecimalFormatSymbols getInstance(Locale locale) {
        return new DecimalFormatSymbols(locale);
    }

    public static DecimalFormatSymbols getInstance(ULocale uLocale) {
        return new DecimalFormatSymbols(uLocale);
    }

    public static DecimalFormatSymbols forNumberingSystem(Locale locale, NumberingSystem numberingSystem) {
        return new DecimalFormatSymbols(locale, numberingSystem);
    }

    public static DecimalFormatSymbols forNumberingSystem(ULocale uLocale, NumberingSystem numberingSystem) {
        return new DecimalFormatSymbols(uLocale, numberingSystem);
    }

    public static Locale[] getAvailableLocales() {
        return ICUResourceBundle.getAvailableLocales();
    }

    public static ULocale[] getAvailableULocales() {
        return ICUResourceBundle.getAvailableULocales();
    }

    public char getZeroDigit() {
        return this.zeroDigit;
    }

    public char[] getDigits() {
        return (char[]) this.digits.clone();
    }

    public void setZeroDigit(char c) {
        this.zeroDigit = c;
        this.digitStrings = (String[]) this.digitStrings.clone();
        this.digits = (char[]) this.digits.clone();
        this.digitStrings[0] = String.valueOf(c);
        this.digits[0] = c;
        for (int i = 1; i < 10; i++) {
            char c2 = (char) (c + i);
            this.digitStrings[i] = String.valueOf(c2);
            this.digits[i] = c2;
        }
        this.codePointZero = c;
    }

    public String[] getDigitStrings() {
        return (String[]) this.digitStrings.clone();
    }

    @Deprecated
    public String[] getDigitStringsLocal() {
        return this.digitStrings;
    }

    @Deprecated
    public int getCodePointZero() {
        return this.codePointZero;
    }

    public void setDigitStrings(String[] strArr) {
        int i;
        int i2;
        if (strArr == null) {
            throw new NullPointerException("The input digit string array is null");
        } else if (strArr.length == 10) {
            String[] strArr2 = new String[10];
            char[] cArr = new char[10];
            int i3 = -1;
            for (int i4 = 0; i4 < 10; i4++) {
                String str = strArr[i4];
                if (str != null) {
                    strArr2[i4] = str;
                    if (str.length() == 0) {
                        i2 = -1;
                        i = 0;
                    } else {
                        i2 = Character.codePointAt(strArr[i4], 0);
                        i = Character.charCount(i2);
                    }
                    if (i == str.length()) {
                        if (i != 1 || cArr == null) {
                            cArr = null;
                        } else {
                            cArr[i4] = (char) i2;
                        }
                        if (i4 == 0) {
                            i3 = i2;
                        } else if (i2 != i3 + i4) {
                            i3 = -1;
                        }
                    } else {
                        i3 = -1;
                        cArr = null;
                    }
                } else {
                    throw new IllegalArgumentException("The input digit string array contains a null element");
                }
            }
            this.digitStrings = strArr2;
            this.codePointZero = i3;
            if (cArr == null) {
                char[] cArr2 = DEF_DIGIT_CHARS_ARRAY;
                this.zeroDigit = cArr2[0];
                this.digits = cArr2;
                return;
            }
            this.zeroDigit = cArr[0];
            this.digits = cArr;
        } else {
            throw new IllegalArgumentException("Number of digit strings is not 10");
        }
    }

    public char getSignificantDigit() {
        return this.sigDigit;
    }

    public void setSignificantDigit(char c) {
        this.sigDigit = c;
    }

    public char getGroupingSeparator() {
        return this.groupingSeparator;
    }

    public void setGroupingSeparator(char c) {
        this.groupingSeparator = c;
        this.groupingSeparatorString = String.valueOf(c);
    }

    public String getGroupingSeparatorString() {
        return this.groupingSeparatorString;
    }

    public void setGroupingSeparatorString(String str) {
        if (str != null) {
            this.groupingSeparatorString = str;
            if (str.length() == 1) {
                this.groupingSeparator = str.charAt(0);
            } else {
                this.groupingSeparator = ',';
            }
        } else {
            throw new NullPointerException("The input grouping separator is null");
        }
    }

    public char getDecimalSeparator() {
        return this.decimalSeparator;
    }

    public void setDecimalSeparator(char c) {
        this.decimalSeparator = c;
        this.decimalSeparatorString = String.valueOf(c);
    }

    public String getDecimalSeparatorString() {
        return this.decimalSeparatorString;
    }

    public void setDecimalSeparatorString(String str) {
        if (str != null) {
            this.decimalSeparatorString = str;
            if (str.length() == 1) {
                this.decimalSeparator = str.charAt(0);
            } else {
                this.decimalSeparator = DEF_DECIMAL_SEPARATOR;
            }
        } else {
            throw new NullPointerException("The input decimal separator is null");
        }
    }

    public char getPerMill() {
        return this.perMill;
    }

    public void setPerMill(char c) {
        this.perMill = c;
        this.perMillString = String.valueOf(c);
    }

    public String getPerMillString() {
        return this.perMillString;
    }

    public void setPerMillString(String str) {
        if (str != null) {
            this.perMillString = str;
            if (str.length() == 1) {
                this.perMill = str.charAt(0);
            } else {
                this.perMill = DEF_PERMILL;
            }
        } else {
            throw new NullPointerException("The input permille string is null");
        }
    }

    public char getPercent() {
        return this.percent;
    }

    public void setPercent(char c) {
        this.percent = c;
        this.percentString = String.valueOf(c);
    }

    public String getPercentString() {
        return this.percentString;
    }

    public void setPercentString(String str) {
        if (str != null) {
            this.percentString = str;
            if (str.length() == 1) {
                this.percent = str.charAt(0);
            } else {
                this.percent = DEF_PERCENT;
            }
        } else {
            throw new NullPointerException("The input percent sign is null");
        }
    }

    public char getDigit() {
        return this.digit;
    }

    public void setDigit(char c) {
        this.digit = c;
    }

    public char getPatternSeparator() {
        return this.patternSeparator;
    }

    public void setPatternSeparator(char c) {
        this.patternSeparator = c;
    }

    public String getInfinity() {
        return this.infinity;
    }

    public void setInfinity(String str) {
        this.infinity = str;
    }

    public String getNaN() {
        return this.NaN;
    }

    public void setNaN(String str) {
        this.NaN = str;
    }

    public char getMinusSign() {
        return this.minusSign;
    }

    public void setMinusSign(char c) {
        this.minusSign = c;
        this.minusString = String.valueOf(c);
    }

    public String getMinusSignString() {
        return this.minusString;
    }

    public void setMinusSignString(String str) {
        if (str != null) {
            this.minusString = str;
            if (str.length() == 1) {
                this.minusSign = str.charAt(0);
            } else {
                this.minusSign = DEF_MINUS_SIGN;
            }
        } else {
            throw new NullPointerException("The input minus sign is null");
        }
    }

    public char getPlusSign() {
        return this.plusSign;
    }

    public void setPlusSign(char c) {
        this.plusSign = c;
        this.plusString = String.valueOf(c);
    }

    public String getPlusSignString() {
        return this.plusString;
    }

    public void setPlusSignString(String str) {
        if (str != null) {
            this.plusString = str;
            if (str.length() == 1) {
                this.plusSign = str.charAt(0);
            } else {
                this.plusSign = DEF_PLUS_SIGN;
            }
        } else {
            throw new NullPointerException("The input plus sign is null");
        }
    }

    public String getCurrencySymbol() {
        return this.currencySymbol;
    }

    public void setCurrencySymbol(String str) {
        this.currencySymbol = str;
    }

    public String getInternationalCurrencySymbol() {
        return this.intlCurrencySymbol;
    }

    public void setInternationalCurrencySymbol(String str) {
        this.intlCurrencySymbol = str;
    }

    public Currency getCurrency() {
        return this.currency;
    }

    public void setCurrency(Currency currency2) {
        if (currency2 != null) {
            this.currency = currency2;
            this.intlCurrencySymbol = currency2.getCurrencyCode();
            this.currencySymbol = currency2.getSymbol(this.requestedLocale);
            return;
        }
        throw new NullPointerException();
    }

    public char getMonetaryDecimalSeparator() {
        return this.monetarySeparator;
    }

    public void setMonetaryDecimalSeparator(char c) {
        this.monetarySeparator = c;
        this.monetarySeparatorString = String.valueOf(c);
    }

    public String getMonetaryDecimalSeparatorString() {
        return this.monetarySeparatorString;
    }

    public void setMonetaryDecimalSeparatorString(String str) {
        if (str != null) {
            this.monetarySeparatorString = str;
            if (str.length() == 1) {
                this.monetarySeparator = str.charAt(0);
            } else {
                this.monetarySeparator = DEF_DECIMAL_SEPARATOR;
            }
        } else {
            throw new NullPointerException("The input monetary decimal separator is null");
        }
    }

    public char getMonetaryGroupingSeparator() {
        return this.monetaryGroupingSeparator;
    }

    public void setMonetaryGroupingSeparator(char c) {
        this.monetaryGroupingSeparator = c;
        this.monetaryGroupingSeparatorString = String.valueOf(c);
    }

    public String getMonetaryGroupingSeparatorString() {
        return this.monetaryGroupingSeparatorString;
    }

    public void setMonetaryGroupingSeparatorString(String str) {
        if (str != null) {
            this.monetaryGroupingSeparatorString = str;
            if (str.length() == 1) {
                this.monetaryGroupingSeparator = str.charAt(0);
            } else {
                this.monetaryGroupingSeparator = ',';
            }
        } else {
            throw new NullPointerException("The input monetary grouping separator is null");
        }
    }

    /* access modifiers changed from: package-private */
    public String getCurrencyPattern() {
        return this.currencyPattern;
    }

    public String getExponentMultiplicationSign() {
        return this.exponentMultiplicationSign;
    }

    public void setExponentMultiplicationSign(String str) {
        this.exponentMultiplicationSign = str;
    }

    public String getExponentSeparator() {
        return this.exponentSeparator;
    }

    public void setExponentSeparator(String str) {
        this.exponentSeparator = str;
    }

    public char getPadEscape() {
        return this.padEscape;
    }

    public void setPadEscape(char c) {
        this.padEscape = c;
    }

    public String getPatternForCurrencySpacing(int i, boolean z) {
        if (i < 0 || i > 2) {
            throw new IllegalArgumentException("unknown currency spacing: " + i);
        } else if (z) {
            return this.currencySpcBeforeSym[i];
        } else {
            return this.currencySpcAfterSym[i];
        }
    }

    public void setPatternForCurrencySpacing(int i, boolean z, String str) {
        if (i < 0 || i > 2) {
            throw new IllegalArgumentException("unknown currency spacing: " + i);
        } else if (z) {
            this.currencySpcBeforeSym = (String[]) this.currencySpcBeforeSym.clone();
            this.currencySpcBeforeSym[i] = str;
        } else {
            this.currencySpcAfterSym = (String[]) this.currencySpcAfterSym.clone();
            this.currencySpcAfterSym[i] = str;
        }
    }

    public Locale getLocale() {
        return this.requestedLocale;
    }

    public ULocale getULocale() {
        return this.ulocale;
    }

    @Override // java.lang.Object
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new ICUCloneNotSupportedException(e);
        }
    }

    @Override // java.lang.Object
    public boolean equals(Object obj) {
        if (!(obj instanceof DecimalFormatSymbols)) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        DecimalFormatSymbols decimalFormatSymbols = (DecimalFormatSymbols) obj;
        for (int i = 0; i <= 2; i++) {
            if (!(this.currencySpcBeforeSym[i].equals(decimalFormatSymbols.currencySpcBeforeSym[i]) && this.currencySpcAfterSym[i].equals(decimalFormatSymbols.currencySpcAfterSym[i]))) {
                return false;
            }
        }
        char[] cArr = decimalFormatSymbols.digits;
        if (cArr == null) {
            for (int i2 = 0; i2 < 10; i2++) {
                if (this.digits[i2] != decimalFormatSymbols.zeroDigit + i2) {
                    return false;
                }
            }
        } else if (!Arrays.equals(this.digits, cArr)) {
            return false;
        }
        return this.groupingSeparator == decimalFormatSymbols.groupingSeparator && this.decimalSeparator == decimalFormatSymbols.decimalSeparator && this.percent == decimalFormatSymbols.percent && this.perMill == decimalFormatSymbols.perMill && this.digit == decimalFormatSymbols.digit && this.minusSign == decimalFormatSymbols.minusSign && this.minusString.equals(decimalFormatSymbols.minusString) && this.patternSeparator == decimalFormatSymbols.patternSeparator && this.infinity.equals(decimalFormatSymbols.infinity) && this.NaN.equals(decimalFormatSymbols.NaN) && this.currencySymbol.equals(decimalFormatSymbols.currencySymbol) && this.intlCurrencySymbol.equals(decimalFormatSymbols.intlCurrencySymbol) && this.padEscape == decimalFormatSymbols.padEscape && this.plusSign == decimalFormatSymbols.plusSign && this.plusString.equals(decimalFormatSymbols.plusString) && this.exponentSeparator.equals(decimalFormatSymbols.exponentSeparator) && this.monetarySeparator == decimalFormatSymbols.monetarySeparator && this.monetaryGroupingSeparator == decimalFormatSymbols.monetaryGroupingSeparator && this.exponentMultiplicationSign.equals(decimalFormatSymbols.exponentMultiplicationSign);
    }

    @Override // java.lang.Object
    public int hashCode() {
        return (((this.digits[0] * DEF_PERCENT) + this.groupingSeparator) * 37) + this.decimalSeparator;
    }

    /* access modifiers changed from: private */
    public static final class DecFmtDataSink extends UResource.Sink {
        private String[] numberElements;

        public DecFmtDataSink(String[] strArr) {
            this.numberElements = strArr;
        }

        public void put(UResource.Key key, UResource.Value value, boolean z) {
            UResource.Table table = value.getTable();
            for (int i = 0; table.getKeyAndValue(i, key, value); i++) {
                int i2 = 0;
                while (true) {
                    if (i2 >= DecimalFormatSymbols.SYMBOL_KEYS.length) {
                        break;
                    } else if (key.contentEquals(DecimalFormatSymbols.SYMBOL_KEYS[i2])) {
                        String[] strArr = this.numberElements;
                        if (strArr[i2] == null) {
                            strArr[i2] = value.toString();
                        }
                    } else {
                        i2++;
                    }
                }
            }
        }
    }

    private void initialize(ULocale uLocale, NumberingSystem numberingSystem) {
        ULocale uLocale2;
        this.requestedLocale = uLocale.toLocale();
        this.ulocale = uLocale;
        if (numberingSystem == null) {
            uLocale2 = uLocale;
        } else {
            uLocale2 = uLocale.setKeywordValue("numbers", numberingSystem.getName());
        }
        CacheData cacheData = (CacheData) cachedLocaleData.getInstance(uLocale2, (Object) null);
        setLocale(cacheData.validLocale, cacheData.validLocale);
        setDigitStrings(cacheData.digits);
        String[] strArr = cacheData.numberElements;
        setDecimalSeparatorString(strArr[0]);
        setGroupingSeparatorString(strArr[1]);
        this.patternSeparator = TelephoneNumberUtils.WAIT;
        setPercentString(strArr[2]);
        setMinusSignString(strArr[3]);
        setPlusSignString(strArr[4]);
        setExponentSeparator(strArr[5]);
        setPerMillString(strArr[6]);
        setInfinity(strArr[7]);
        setNaN(strArr[8]);
        setMonetaryDecimalSeparatorString(strArr[9]);
        setMonetaryGroupingSeparatorString(strArr[10]);
        setExponentMultiplicationSign(strArr[11]);
        this.digit = '#';
        this.padEscape = '*';
        this.sigDigit = '@';
        CurrencyData.CurrencyDisplayInfo instance = CurrencyData.provider.getInstance(uLocale, true);
        this.currency = Currency.getInstance(uLocale);
        Currency currency2 = this.currency;
        if (currency2 != null) {
            this.intlCurrencySymbol = currency2.getCurrencyCode();
            this.currencySymbol = this.currency.getName(uLocale, 0, (boolean[]) null);
            CurrencyData.CurrencyFormatInfo formatInfo = instance.getFormatInfo(this.intlCurrencySymbol);
            if (formatInfo != null) {
                this.currencyPattern = formatInfo.currencyPattern;
                setMonetaryDecimalSeparatorString(formatInfo.monetaryDecimalSeparator);
                setMonetaryGroupingSeparatorString(formatInfo.monetaryGroupingSeparator);
            }
        } else {
            this.intlCurrencySymbol = "XXX";
            this.currencySymbol = "¤";
        }
        initSpacingInfo(instance.getSpacingInfo());
    }

    /* access modifiers changed from: private */
    public static CacheData loadData(ULocale uLocale) {
        String str;
        boolean z;
        NumberingSystem instance = NumberingSystem.getInstance(uLocale);
        String[] strArr = new String[10];
        if (instance == null || instance.getRadix() != 10 || instance.isAlgorithmic() || !NumberingSystem.isValidDigitString(instance.getDescription())) {
            strArr = DEF_DIGIT_STRINGS_ARRAY;
            str = LATIN_NUMBERING_SYSTEM;
        } else {
            String description = instance.getDescription();
            int i = 0;
            int i2 = 0;
            while (i < 10) {
                int charCount = Character.charCount(description.codePointAt(i2)) + i2;
                strArr[i] = description.substring(i2, charCount);
                i++;
                i2 = charCount;
            }
            str = instance.getName();
        }
        ICUResourceBundle bundleInstance = UResourceBundle.getBundleInstance("ohos/global/icu/impl/data/icudt66b", uLocale);
        ULocale uLocale2 = bundleInstance.getULocale();
        String[] strArr2 = new String[SYMBOL_KEYS.length];
        DecFmtDataSink decFmtDataSink = new DecFmtDataSink(strArr2);
        try {
            bundleInstance.getAllItemsWithFallback("NumberElements/" + str + "/" + SYMBOLS, decFmtDataSink);
        } catch (MissingResourceException unused) {
        }
        int length = strArr2.length;
        int i3 = 0;
        while (true) {
            if (i3 >= length) {
                z = false;
                break;
            } else if (strArr2[i3] == null) {
                z = true;
                break;
            } else {
                i3++;
            }
        }
        if (z && !str.equals(LATIN_NUMBERING_SYSTEM)) {
            bundleInstance.getAllItemsWithFallback("NumberElements/latn/symbols", decFmtDataSink);
        }
        for (int i4 = 0; i4 < SYMBOL_KEYS.length; i4++) {
            if (strArr2[i4] == null) {
                strArr2[i4] = SYMBOL_DEFAULTS[i4];
            }
        }
        if (strArr2[9] == null) {
            strArr2[9] = strArr2[0];
        }
        if (strArr2[10] == null) {
            strArr2[10] = strArr2[1];
        }
        return new CacheData(uLocale2, strArr, strArr2);
    }

    private void initSpacingInfo(CurrencyData.CurrencySpacingInfo currencySpacingInfo) {
        this.currencySpcBeforeSym = currencySpacingInfo.getBeforeSymbols();
        this.currencySpcAfterSym = currencySpacingInfo.getAfterSymbols();
    }

    private void readObject(ObjectInputStream objectInputStream) throws IOException, ClassNotFoundException {
        objectInputStream.defaultReadObject();
        if (this.serialVersionOnStream < 1) {
            this.monetarySeparator = this.decimalSeparator;
            this.exponential = 'E';
        }
        if (this.serialVersionOnStream < 2) {
            this.padEscape = '*';
            this.plusSign = DEF_PLUS_SIGN;
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
            initSpacingInfo(CurrencyData.CurrencySpacingInfo.DEFAULT);
        }
        if (this.serialVersionOnStream < 7) {
            if (this.minusString == null) {
                this.minusString = String.valueOf(this.minusSign);
            }
            if (this.plusString == null) {
                this.plusString = String.valueOf(this.plusSign);
            }
        }
        if (this.serialVersionOnStream < 8 && this.exponentMultiplicationSign == null) {
            this.exponentMultiplicationSign = "×";
        }
        if (this.serialVersionOnStream < 9) {
            if (this.digitStrings == null) {
                this.digitStrings = new String[10];
                char[] cArr = this.digits;
                int i = 0;
                if (cArr == null || cArr.length != 10) {
                    char c = this.zeroDigit;
                    if (this.digits == null) {
                        this.digits = new char[10];
                    }
                    while (i < 10) {
                        this.digits[i] = c;
                        this.digitStrings[i] = String.valueOf(c);
                        c = (char) (c + 1);
                        i++;
                    }
                } else {
                    this.zeroDigit = cArr[0];
                    while (i < 10) {
                        this.digitStrings[i] = String.valueOf(this.digits[i]);
                        i++;
                    }
                }
            }
            if (this.decimalSeparatorString == null) {
                this.decimalSeparatorString = String.valueOf(this.decimalSeparator);
            }
            if (this.groupingSeparatorString == null) {
                this.groupingSeparatorString = String.valueOf(this.groupingSeparator);
            }
            if (this.percentString == null) {
                this.percentString = String.valueOf(this.percent);
            }
            if (this.perMillString == null) {
                this.perMillString = String.valueOf(this.perMill);
            }
            if (this.monetarySeparatorString == null) {
                this.monetarySeparatorString = String.valueOf(this.monetarySeparator);
            }
            if (this.monetaryGroupingSeparatorString == null) {
                this.monetaryGroupingSeparatorString = String.valueOf(this.monetaryGroupingSeparator);
            }
        }
        this.serialVersionOnStream = 8;
        this.currency = Currency.getInstance(this.intlCurrencySymbol);
        setDigitStrings(this.digitStrings);
    }

    public final ULocale getLocale(ULocale.Type type) {
        return type == ULocale.ACTUAL_LOCALE ? this.actualLocale : this.validLocale;
    }

    /* access modifiers changed from: package-private */
    public final void setLocale(ULocale uLocale, ULocale uLocale2) {
        boolean z = true;
        boolean z2 = uLocale == null;
        if (uLocale2 != null) {
            z = false;
        }
        if (z2 == z) {
            this.validLocale = uLocale;
            this.actualLocale = uLocale2;
            return;
        }
        throw new IllegalArgumentException();
    }

    /* access modifiers changed from: private */
    public static class CacheData {
        final String[] digits;
        final String[] numberElements;
        final ULocale validLocale;

        public CacheData(ULocale uLocale, String[] strArr, String[] strArr2) {
            this.validLocale = uLocale;
            this.digits = strArr;
            this.numberElements = strArr2;
        }
    }
}
