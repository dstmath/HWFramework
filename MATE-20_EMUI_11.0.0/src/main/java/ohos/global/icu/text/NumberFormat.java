package ohos.global.icu.text;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Collections;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Set;
import ohos.com.sun.org.apache.xalan.internal.templates.Constants;
import ohos.com.sun.org.apache.xpath.internal.compiler.PsuedoNames;
import ohos.global.icu.impl.ICUData;
import ohos.global.icu.impl.ICUResourceBundle;
import ohos.global.icu.text.DisplayContext;
import ohos.global.icu.util.Currency;
import ohos.global.icu.util.CurrencyAmount;
import ohos.global.icu.util.ULocale;
import ohos.global.icu.util.UResourceBundle;

public abstract class NumberFormat extends UFormat {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    public static final int ACCOUNTINGCURRENCYSTYLE = 7;
    public static final int CASHCURRENCYSTYLE = 8;
    public static final int CURRENCYSTYLE = 1;
    public static final int FRACTION_FIELD = 1;
    public static final int INTEGERSTYLE = 4;
    public static final int INTEGER_FIELD = 0;
    public static final int ISOCURRENCYSTYLE = 5;
    public static final int NUMBERSTYLE = 0;
    public static final int PERCENTSTYLE = 2;
    public static final int PLURALCURRENCYSTYLE = 6;
    public static final int SCIENTIFICSTYLE = 3;
    public static final int STANDARDCURRENCYSTYLE = 9;
    static final int currentSerialVersion = 2;
    private static final char[] doubleCurrencySign = {164, 164};
    private static final String doubleCurrencyStr = new String(doubleCurrencySign);
    private static final long serialVersionUID = -2308460125733713944L;
    private static NumberFormatShim shim;
    private DisplayContext capitalizationSetting = DisplayContext.CAPITALIZATION_NONE;
    private Currency currency;
    private boolean groupingUsed = true;
    private byte maxFractionDigits = 3;
    private byte maxIntegerDigits = 40;
    private int maximumFractionDigits = 3;
    private int maximumIntegerDigits = 40;
    private byte minFractionDigits = 0;
    private byte minIntegerDigits = 1;
    private int minimumFractionDigits = 0;
    private int minimumIntegerDigits = 1;
    private boolean parseIntegerOnly = false;
    private boolean parseStrict;
    private int serialVersionOnStream = 2;

    public abstract StringBuffer format(double d, StringBuffer stringBuffer, FieldPosition fieldPosition);

    public abstract StringBuffer format(long j, StringBuffer stringBuffer, FieldPosition fieldPosition);

    public abstract StringBuffer format(BigDecimal bigDecimal, StringBuffer stringBuffer, FieldPosition fieldPosition);

    public abstract StringBuffer format(BigInteger bigInteger, StringBuffer stringBuffer, FieldPosition fieldPosition);

    public abstract StringBuffer format(ohos.global.icu.math.BigDecimal bigDecimal, StringBuffer stringBuffer, FieldPosition fieldPosition);

    public abstract Number parse(String str, ParsePosition parsePosition);

    @Override // java.text.Format
    public StringBuffer format(Object obj, StringBuffer stringBuffer, FieldPosition fieldPosition) {
        if (obj instanceof Long) {
            return format(((Long) obj).longValue(), stringBuffer, fieldPosition);
        }
        if (obj instanceof BigInteger) {
            return format((BigInteger) obj, stringBuffer, fieldPosition);
        }
        if (obj instanceof BigDecimal) {
            return format((BigDecimal) obj, stringBuffer, fieldPosition);
        }
        if (obj instanceof ohos.global.icu.math.BigDecimal) {
            return format((ohos.global.icu.math.BigDecimal) obj, stringBuffer, fieldPosition);
        }
        if (obj instanceof CurrencyAmount) {
            return format((CurrencyAmount) obj, stringBuffer, fieldPosition);
        }
        if (obj instanceof Number) {
            return format(((Number) obj).doubleValue(), stringBuffer, fieldPosition);
        }
        throw new IllegalArgumentException("Cannot format given Object as a Number");
    }

    @Override // java.text.Format
    public final Object parseObject(String str, ParsePosition parsePosition) {
        return parse(str, parsePosition);
    }

    public final String format(double d) {
        return format(d, new StringBuffer(), new FieldPosition(0)).toString();
    }

    public final String format(long j) {
        StringBuffer stringBuffer = new StringBuffer(19);
        format(j, stringBuffer, new FieldPosition(0));
        return stringBuffer.toString();
    }

    public final String format(BigInteger bigInteger) {
        return format(bigInteger, new StringBuffer(), new FieldPosition(0)).toString();
    }

    public final String format(BigDecimal bigDecimal) {
        return format(bigDecimal, new StringBuffer(), new FieldPosition(0)).toString();
    }

    public final String format(ohos.global.icu.math.BigDecimal bigDecimal) {
        return format(bigDecimal, new StringBuffer(), new FieldPosition(0)).toString();
    }

    public final String format(CurrencyAmount currencyAmount) {
        return format(currencyAmount, new StringBuffer(), new FieldPosition(0)).toString();
    }

    public StringBuffer format(CurrencyAmount currencyAmount, StringBuffer stringBuffer, FieldPosition fieldPosition) {
        synchronized (this) {
            Currency currency2 = getCurrency();
            Currency currency3 = currencyAmount.getCurrency();
            boolean equals = currency3.equals(currency2);
            if (!equals) {
                setCurrency(currency3);
            }
            format(currencyAmount.getNumber(), stringBuffer, fieldPosition);
            if (!equals) {
                setCurrency(currency2);
            }
        }
        return stringBuffer;
    }

    public Number parse(String str) throws ParseException {
        ParsePosition parsePosition = new ParsePosition(0);
        Number parse = parse(str, parsePosition);
        if (parsePosition.getIndex() != 0) {
            return parse;
        }
        throw new ParseException("Unparseable number: \"" + str + '\"', parsePosition.getErrorIndex());
    }

    public CurrencyAmount parseCurrency(CharSequence charSequence, ParsePosition parsePosition) {
        Number parse = parse(charSequence.toString(), parsePosition);
        if (parse == null) {
            return null;
        }
        return new CurrencyAmount(parse, getEffectiveCurrency());
    }

    public boolean isParseIntegerOnly() {
        return this.parseIntegerOnly;
    }

    public void setParseIntegerOnly(boolean z) {
        this.parseIntegerOnly = z;
    }

    public void setParseStrict(boolean z) {
        this.parseStrict = z;
    }

    public boolean isParseStrict() {
        return this.parseStrict;
    }

    public void setContext(DisplayContext displayContext) {
        if (displayContext.type() == DisplayContext.Type.CAPITALIZATION) {
            this.capitalizationSetting = displayContext;
        }
    }

    public DisplayContext getContext(DisplayContext.Type type) {
        DisplayContext displayContext;
        return (type != DisplayContext.Type.CAPITALIZATION || (displayContext = this.capitalizationSetting) == null) ? DisplayContext.CAPITALIZATION_NONE : displayContext;
    }

    public static final NumberFormat getInstance() {
        return getInstance(ULocale.getDefault(ULocale.Category.FORMAT), 0);
    }

    public static NumberFormat getInstance(Locale locale) {
        return getInstance(ULocale.forLocale(locale), 0);
    }

    public static NumberFormat getInstance(ULocale uLocale) {
        return getInstance(uLocale, 0);
    }

    public static final NumberFormat getInstance(int i) {
        return getInstance(ULocale.getDefault(ULocale.Category.FORMAT), i);
    }

    public static NumberFormat getInstance(Locale locale, int i) {
        return getInstance(ULocale.forLocale(locale), i);
    }

    public static final NumberFormat getNumberInstance() {
        return getInstance(ULocale.getDefault(ULocale.Category.FORMAT), 0);
    }

    public static NumberFormat getNumberInstance(Locale locale) {
        return getInstance(ULocale.forLocale(locale), 0);
    }

    public static NumberFormat getNumberInstance(ULocale uLocale) {
        return getInstance(uLocale, 0);
    }

    public static final NumberFormat getIntegerInstance() {
        return getInstance(ULocale.getDefault(ULocale.Category.FORMAT), 4);
    }

    public static NumberFormat getIntegerInstance(Locale locale) {
        return getInstance(ULocale.forLocale(locale), 4);
    }

    public static NumberFormat getIntegerInstance(ULocale uLocale) {
        return getInstance(uLocale, 4);
    }

    public static final NumberFormat getCurrencyInstance() {
        return getInstance(ULocale.getDefault(ULocale.Category.FORMAT), 1);
    }

    public static NumberFormat getCurrencyInstance(Locale locale) {
        return getInstance(ULocale.forLocale(locale), 1);
    }

    public static NumberFormat getCurrencyInstance(ULocale uLocale) {
        return getInstance(uLocale, 1);
    }

    public static final NumberFormat getPercentInstance() {
        return getInstance(ULocale.getDefault(ULocale.Category.FORMAT), 2);
    }

    public static NumberFormat getPercentInstance(Locale locale) {
        return getInstance(ULocale.forLocale(locale), 2);
    }

    public static NumberFormat getPercentInstance(ULocale uLocale) {
        return getInstance(uLocale, 2);
    }

    public static final NumberFormat getScientificInstance() {
        return getInstance(ULocale.getDefault(ULocale.Category.FORMAT), 3);
    }

    public static NumberFormat getScientificInstance(Locale locale) {
        return getInstance(ULocale.forLocale(locale), 3);
    }

    public static NumberFormat getScientificInstance(ULocale uLocale) {
        return getInstance(uLocale, 3);
    }

    public static abstract class NumberFormatFactory {
        public static final int FORMAT_CURRENCY = 1;
        public static final int FORMAT_INTEGER = 4;
        public static final int FORMAT_NUMBER = 0;
        public static final int FORMAT_PERCENT = 2;
        public static final int FORMAT_SCIENTIFIC = 3;

        public abstract Set<String> getSupportedLocaleNames();

        public boolean visible() {
            return true;
        }

        public NumberFormat createFormat(ULocale uLocale, int i) {
            return createFormat(uLocale.toLocale(), i);
        }

        public NumberFormat createFormat(Locale locale, int i) {
            return createFormat(ULocale.forLocale(locale), i);
        }

        protected NumberFormatFactory() {
        }
    }

    public static abstract class SimpleNumberFormatFactory extends NumberFormatFactory {
        final Set<String> localeNames;
        final boolean visible;

        public SimpleNumberFormatFactory(Locale locale) {
            this(locale, true);
        }

        public SimpleNumberFormatFactory(Locale locale, boolean z) {
            this.localeNames = Collections.singleton(ULocale.forLocale(locale).getBaseName());
            this.visible = z;
        }

        public SimpleNumberFormatFactory(ULocale uLocale) {
            this(uLocale, true);
        }

        public SimpleNumberFormatFactory(ULocale uLocale, boolean z) {
            this.localeNames = Collections.singleton(uLocale.getBaseName());
            this.visible = z;
        }

        @Override // ohos.global.icu.text.NumberFormat.NumberFormatFactory
        public final boolean visible() {
            return this.visible;
        }

        @Override // ohos.global.icu.text.NumberFormat.NumberFormatFactory
        public final Set<String> getSupportedLocaleNames() {
            return this.localeNames;
        }
    }

    /* access modifiers changed from: package-private */
    public static abstract class NumberFormatShim {
        /* access modifiers changed from: package-private */
        public abstract NumberFormat createInstance(ULocale uLocale, int i);

        /* access modifiers changed from: package-private */
        public abstract Locale[] getAvailableLocales();

        /* access modifiers changed from: package-private */
        public abstract ULocale[] getAvailableULocales();

        /* access modifiers changed from: package-private */
        public abstract Object registerFactory(NumberFormatFactory numberFormatFactory);

        /* access modifiers changed from: package-private */
        public abstract boolean unregister(Object obj);

        NumberFormatShim() {
        }
    }

    private static NumberFormatShim getShim() {
        if (shim == null) {
            try {
                shim = (NumberFormatShim) Class.forName("ohos.global.icu.text.NumberFormatServiceShim").newInstance();
            } catch (MissingResourceException e) {
                throw e;
            } catch (Exception e2) {
                throw new RuntimeException(e2.getMessage());
            }
        }
        return shim;
    }

    public static Locale[] getAvailableLocales() {
        if (shim == null) {
            return ICUResourceBundle.getAvailableLocales();
        }
        return getShim().getAvailableLocales();
    }

    public static ULocale[] getAvailableULocales() {
        if (shim == null) {
            return ICUResourceBundle.getAvailableULocales();
        }
        return getShim().getAvailableULocales();
    }

    public static Object registerFactory(NumberFormatFactory numberFormatFactory) {
        if (numberFormatFactory != null) {
            return getShim().registerFactory(numberFormatFactory);
        }
        throw new IllegalArgumentException("factory must not be null");
    }

    public static boolean unregister(Object obj) {
        if (obj != null) {
            NumberFormatShim numberFormatShim = shim;
            if (numberFormatShim == null) {
                return false;
            }
            return numberFormatShim.unregister(obj);
        }
        throw new IllegalArgumentException("registryKey must not be null");
    }

    @Override // java.lang.Object
    public int hashCode() {
        return (this.maximumIntegerDigits * 37) + this.maxFractionDigits;
    }

    @Override // java.lang.Object
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        NumberFormat numberFormat = (NumberFormat) obj;
        return this.maximumIntegerDigits == numberFormat.maximumIntegerDigits && this.minimumIntegerDigits == numberFormat.minimumIntegerDigits && this.maximumFractionDigits == numberFormat.maximumFractionDigits && this.minimumFractionDigits == numberFormat.minimumFractionDigits && this.groupingUsed == numberFormat.groupingUsed && this.parseIntegerOnly == numberFormat.parseIntegerOnly && this.parseStrict == numberFormat.parseStrict && this.capitalizationSetting == numberFormat.capitalizationSetting;
    }

    @Override // java.text.Format, java.lang.Object
    public Object clone() {
        return (NumberFormat) super.clone();
    }

    public boolean isGroupingUsed() {
        return this.groupingUsed;
    }

    public void setGroupingUsed(boolean z) {
        this.groupingUsed = z;
    }

    public int getMaximumIntegerDigits() {
        return this.maximumIntegerDigits;
    }

    public void setMaximumIntegerDigits(int i) {
        this.maximumIntegerDigits = Math.max(0, i);
        int i2 = this.minimumIntegerDigits;
        int i3 = this.maximumIntegerDigits;
        if (i2 > i3) {
            this.minimumIntegerDigits = i3;
        }
    }

    public int getMinimumIntegerDigits() {
        return this.minimumIntegerDigits;
    }

    public void setMinimumIntegerDigits(int i) {
        this.minimumIntegerDigits = Math.max(0, i);
        int i2 = this.minimumIntegerDigits;
        if (i2 > this.maximumIntegerDigits) {
            this.maximumIntegerDigits = i2;
        }
    }

    public int getMaximumFractionDigits() {
        return this.maximumFractionDigits;
    }

    public void setMaximumFractionDigits(int i) {
        this.maximumFractionDigits = Math.max(0, i);
        int i2 = this.maximumFractionDigits;
        if (i2 < this.minimumFractionDigits) {
            this.minimumFractionDigits = i2;
        }
    }

    public int getMinimumFractionDigits() {
        return this.minimumFractionDigits;
    }

    public void setMinimumFractionDigits(int i) {
        this.minimumFractionDigits = Math.max(0, i);
        int i2 = this.maximumFractionDigits;
        int i3 = this.minimumFractionDigits;
        if (i2 < i3) {
            this.maximumFractionDigits = i3;
        }
    }

    public void setCurrency(Currency currency2) {
        this.currency = currency2;
    }

    public Currency getCurrency() {
        return this.currency;
    }

    /* access modifiers changed from: protected */
    @Deprecated
    public Currency getEffectiveCurrency() {
        Currency currency2 = getCurrency();
        if (currency2 != null) {
            return currency2;
        }
        ULocale locale = getLocale(ULocale.VALID_LOCALE);
        if (locale == null) {
            locale = ULocale.getDefault(ULocale.Category.FORMAT);
        }
        return Currency.getInstance(locale);
    }

    public int getRoundingMode() {
        throw new UnsupportedOperationException("getRoundingMode must be implemented by the subclass implementation.");
    }

    public void setRoundingMode(int i) {
        throw new UnsupportedOperationException("setRoundingMode must be implemented by the subclass implementation.");
    }

    public static NumberFormat getInstance(ULocale uLocale, int i) {
        if (i >= 0 && i <= 9) {
            return getShim().createInstance(uLocale, i);
        }
        throw new IllegalArgumentException("choice should be from NUMBERSTYLE to STANDARDCURRENCYSTYLE");
    }

    /* JADX DEBUG: Multi-variable search result rejected for r0v9, resolved type: ohos.global.icu.text.RuleBasedNumberFormat */
    /* JADX WARN: Multi-variable type inference failed */
    static NumberFormat createInstance(ULocale uLocale, int i) {
        DecimalFormat decimalFormat;
        String currencyPattern;
        String pattern = getPattern(uLocale, i);
        DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols(uLocale);
        if ((i == 1 || i == 5 || i == 7 || i == 8 || i == 9) && (currencyPattern = decimalFormatSymbols.getCurrencyPattern()) != null) {
            pattern = currencyPattern;
        }
        if (i == 5) {
            pattern = pattern.replace("¤", doubleCurrencyStr);
        }
        NumberingSystem instance = NumberingSystem.getInstance(uLocale);
        if (instance == null) {
            return null;
        }
        int i2 = 4;
        if (instance.isAlgorithmic()) {
            String description = instance.getDescription();
            int indexOf = description.indexOf(PsuedoNames.PSEUDONAME_ROOT);
            int lastIndexOf = description.lastIndexOf(PsuedoNames.PSEUDONAME_ROOT);
            if (lastIndexOf > indexOf) {
                String substring = description.substring(0, indexOf);
                String substring2 = description.substring(indexOf + 1, lastIndexOf);
                description = description.substring(lastIndexOf + 1);
                ULocale uLocale2 = new ULocale(substring);
                if (substring2.equals("SpelloutRules")) {
                    i2 = 1;
                }
                uLocale = uLocale2;
            }
            RuleBasedNumberFormat ruleBasedNumberFormat = new RuleBasedNumberFormat(uLocale, i2);
            ruleBasedNumberFormat.setDefaultRuleSet(description);
            decimalFormat = ruleBasedNumberFormat;
        } else {
            DecimalFormat decimalFormat2 = new DecimalFormat(pattern, decimalFormatSymbols, i);
            if (i == 4) {
                decimalFormat2.setMaximumFractionDigits(0);
                decimalFormat2.setDecimalSeparatorAlwaysShown(false);
                decimalFormat2.setParseIntegerOnly(true);
            }
            if (i == 8) {
                decimalFormat2.setCurrencyUsage(Currency.CurrencyUsage.CASH);
            }
            if (i == 6) {
                decimalFormat2.setCurrencyPluralInfo(CurrencyPluralInfo.getInstance(uLocale));
            }
            decimalFormat = decimalFormat2;
        }
        decimalFormat.setLocale(decimalFormatSymbols.getLocale(ULocale.VALID_LOCALE), decimalFormatSymbols.getLocale(ULocale.ACTUAL_LOCALE));
        return decimalFormat;
    }

    @Deprecated
    protected static String getPattern(Locale locale, int i) {
        return getPattern(ULocale.forLocale(locale), i);
    }

    protected static String getPattern(ULocale uLocale, int i) {
        return getPatternForStyle(uLocale, i);
    }

    @Deprecated
    public static String getPatternForStyle(ULocale uLocale, int i) {
        return getPatternForStyleAndNumberingSystem(uLocale, NumberingSystem.getInstance(uLocale).getName(), i);
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x0022, code lost:
        if (r5.equals("account") != false) goto L_0x0026;
     */
    @Deprecated
    public static String getPatternForStyleAndNumberingSystem(ULocale uLocale, String str, int i) {
        String str2 = "accountingFormat";
        switch (i) {
            case 0:
            case 4:
            case 6:
            default:
                str2 = "decimalFormat";
                break;
            case 1:
                String keywordValue = uLocale.getKeywordValue("cf");
                if (keywordValue != null) {
                    break;
                }
                str2 = "currencyFormat";
                break;
            case 2:
                str2 = "percentFormat";
                break;
            case 3:
                str2 = "scientificFormat";
                break;
            case 5:
            case 8:
            case 9:
                str2 = "currencyFormat";
                break;
            case 7:
                break;
        }
        ICUResourceBundle bundleInstance = UResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME, uLocale);
        String findStringWithFallback = bundleInstance.findStringWithFallback("NumberElements/" + str + "/patterns/" + str2);
        if (findStringWithFallback != null) {
            return findStringWithFallback;
        }
        return bundleInstance.getStringWithFallback("NumberElements/latn/patterns/" + str2);
    }

    private void readObject(ObjectInputStream objectInputStream) throws IOException, ClassNotFoundException {
        int i;
        objectInputStream.defaultReadObject();
        if (this.serialVersionOnStream < 1) {
            this.maximumIntegerDigits = this.maxIntegerDigits;
            this.minimumIntegerDigits = this.minIntegerDigits;
            this.maximumFractionDigits = this.maxFractionDigits;
            this.minimumFractionDigits = this.minFractionDigits;
        }
        if (this.serialVersionOnStream < 2) {
            this.capitalizationSetting = DisplayContext.CAPITALIZATION_NONE;
        }
        int i2 = this.minimumIntegerDigits;
        if (i2 > this.maximumIntegerDigits || (i = this.minimumFractionDigits) > this.maximumFractionDigits || i2 < 0 || i < 0) {
            throw new InvalidObjectException("Digit count range invalid");
        }
        this.serialVersionOnStream = 2;
    }

    private void writeObject(ObjectOutputStream objectOutputStream) throws IOException {
        int i = this.maximumIntegerDigits;
        byte b = Bidi.LEVEL_DEFAULT_RTL;
        this.maxIntegerDigits = i > 127 ? Byte.MAX_VALUE : (byte) i;
        int i2 = this.minimumIntegerDigits;
        this.minIntegerDigits = i2 > 127 ? Byte.MAX_VALUE : (byte) i2;
        int i3 = this.maximumFractionDigits;
        this.maxFractionDigits = i3 > 127 ? Byte.MAX_VALUE : (byte) i3;
        int i4 = this.minimumFractionDigits;
        if (i4 <= 127) {
            b = (byte) i4;
        }
        this.minFractionDigits = b;
        objectOutputStream.defaultWriteObject();
    }

    public static class Field extends Format.Field {
        public static final Field COMPACT = new Field("compact");
        public static final Field CURRENCY = new Field("currency");
        public static final Field DECIMAL_SEPARATOR = new Field("decimal separator");
        public static final Field EXPONENT = new Field("exponent");
        public static final Field EXPONENT_SIGN = new Field("exponent sign");
        public static final Field EXPONENT_SYMBOL = new Field("exponent symbol");
        public static final Field FRACTION = new Field("fraction");
        public static final Field GROUPING_SEPARATOR = new Field("grouping separator");
        public static final Field INTEGER = new Field("integer");
        public static final Field MEASURE_UNIT = new Field("measure unit");
        public static final Field PERCENT = new Field(Constants.ATTRNAME_PERCENT);
        public static final Field PERMILLE = new Field("per mille");
        public static final Field SIGN = new Field("sign");
        static final long serialVersionUID = -4516273749929385842L;

        protected Field(String str) {
            super(str);
        }

        /* access modifiers changed from: protected */
        @Override // java.text.AttributedCharacterIterator.Attribute
        public Object readResolve() throws InvalidObjectException {
            if (getName().equals(INTEGER.getName())) {
                return INTEGER;
            }
            if (getName().equals(FRACTION.getName())) {
                return FRACTION;
            }
            if (getName().equals(EXPONENT.getName())) {
                return EXPONENT;
            }
            if (getName().equals(EXPONENT_SIGN.getName())) {
                return EXPONENT_SIGN;
            }
            if (getName().equals(EXPONENT_SYMBOL.getName())) {
                return EXPONENT_SYMBOL;
            }
            if (getName().equals(CURRENCY.getName())) {
                return CURRENCY;
            }
            if (getName().equals(DECIMAL_SEPARATOR.getName())) {
                return DECIMAL_SEPARATOR;
            }
            if (getName().equals(GROUPING_SEPARATOR.getName())) {
                return GROUPING_SEPARATOR;
            }
            if (getName().equals(PERCENT.getName())) {
                return PERCENT;
            }
            if (getName().equals(PERMILLE.getName())) {
                return PERMILLE;
            }
            if (getName().equals(SIGN.getName())) {
                return SIGN;
            }
            if (getName().equals(MEASURE_UNIT.getName())) {
                return MEASURE_UNIT;
            }
            if (getName().equals(COMPACT.getName())) {
                return COMPACT;
            }
            throw new InvalidObjectException("An invalid object.");
        }
    }
}
