package android.icu.text;

import android.icu.impl.ICUResourceBundle;
import android.icu.math.BigDecimal;
import android.icu.text.DisplayContext.Type;
import android.icu.util.Currency;
import android.icu.util.Currency.CurrencyUsage;
import android.icu.util.CurrencyAmount;
import android.icu.util.ULocale;
import android.icu.util.ULocale.Category;
import android.icu.util.UResourceBundle;
import dalvik.bytecode.Opcodes;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.text.FieldPosition;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Collections;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Set;

public abstract class NumberFormat extends UFormat {
    static final /* synthetic */ boolean -assertionsDisabled = false;
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
    private static final char[] doubleCurrencySign = null;
    private static final String doubleCurrencyStr = null;
    private static final long serialVersionUID = -2308460125733713944L;
    private static NumberFormatShim shim;
    private DisplayContext capitalizationSetting;
    private Currency currency;
    private boolean groupingUsed;
    private byte maxFractionDigits;
    private byte maxIntegerDigits;
    private int maximumFractionDigits;
    private int maximumIntegerDigits;
    private byte minFractionDigits;
    private byte minIntegerDigits;
    private int minimumFractionDigits;
    private int minimumIntegerDigits;
    private boolean parseIntegerOnly;
    private boolean parseStrict;
    private int serialVersionOnStream;

    public static class Field extends java.text.Format.Field {
        public static final Field CURRENCY = null;
        public static final Field DECIMAL_SEPARATOR = null;
        public static final Field EXPONENT = null;
        public static final Field EXPONENT_SIGN = null;
        public static final Field EXPONENT_SYMBOL = null;
        public static final Field FRACTION = null;
        public static final Field GROUPING_SEPARATOR = null;
        public static final Field INTEGER = null;
        public static final Field PERCENT = null;
        public static final Field PERMILLE = null;
        public static final Field SIGN = null;
        static final long serialVersionUID = -4516273749929385842L;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.NumberFormat.Field.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.NumberFormat.Field.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.text.NumberFormat.Field.<clinit>():void");
        }

        protected Field(String fieldName) {
            super(fieldName);
        }

        protected Object readResolve() throws InvalidObjectException {
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
            throw new InvalidObjectException("An invalid object.");
        }
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

        public NumberFormat createFormat(ULocale loc, int formatType) {
            return createFormat(loc.toLocale(), formatType);
        }

        public NumberFormat createFormat(Locale loc, int formatType) {
            return createFormat(ULocale.forLocale(loc), formatType);
        }

        protected NumberFormatFactory() {
        }
    }

    static abstract class NumberFormatShim {
        abstract NumberFormat createInstance(ULocale uLocale, int i);

        abstract Locale[] getAvailableLocales();

        abstract ULocale[] getAvailableULocales();

        abstract Object registerFactory(NumberFormatFactory numberFormatFactory);

        abstract boolean unregister(Object obj);

        NumberFormatShim() {
        }
    }

    public static abstract class SimpleNumberFormatFactory extends NumberFormatFactory {
        final Set<String> localeNames;
        final boolean visible;

        public SimpleNumberFormatFactory(Locale locale) {
            this(locale, true);
        }

        public SimpleNumberFormatFactory(Locale locale, boolean visible) {
            this.localeNames = Collections.singleton(ULocale.forLocale(locale).getBaseName());
            this.visible = visible;
        }

        public SimpleNumberFormatFactory(ULocale locale) {
            this(locale, true);
        }

        public SimpleNumberFormatFactory(ULocale locale, boolean visible) {
            this.localeNames = Collections.singleton(locale.getBaseName());
            this.visible = visible;
        }

        public final boolean visible() {
            return this.visible;
        }

        public final Set<String> getSupportedLocaleNames() {
            return this.localeNames;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.NumberFormat.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.NumberFormat.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.text.NumberFormat.<clinit>():void");
    }

    public abstract StringBuffer format(double d, StringBuffer stringBuffer, FieldPosition fieldPosition);

    public abstract StringBuffer format(long j, StringBuffer stringBuffer, FieldPosition fieldPosition);

    public abstract StringBuffer format(BigDecimal bigDecimal, StringBuffer stringBuffer, FieldPosition fieldPosition);

    public abstract StringBuffer format(java.math.BigDecimal bigDecimal, StringBuffer stringBuffer, FieldPosition fieldPosition);

    public abstract StringBuffer format(BigInteger bigInteger, StringBuffer stringBuffer, FieldPosition fieldPosition);

    public abstract Number parse(String str, ParsePosition parsePosition);

    public StringBuffer format(Object number, StringBuffer toAppendTo, FieldPosition pos) {
        if (number instanceof Long) {
            return format(((Long) number).longValue(), toAppendTo, pos);
        }
        if (number instanceof BigInteger) {
            return format((BigInteger) number, toAppendTo, pos);
        }
        if (number instanceof java.math.BigDecimal) {
            return format((java.math.BigDecimal) number, toAppendTo, pos);
        }
        if (number instanceof BigDecimal) {
            return format((BigDecimal) number, toAppendTo, pos);
        }
        if (number instanceof CurrencyAmount) {
            return format((CurrencyAmount) number, toAppendTo, pos);
        }
        if (number instanceof Number) {
            return format(((Number) number).doubleValue(), toAppendTo, pos);
        }
        throw new IllegalArgumentException("Cannot format given Object as a Number");
    }

    public final Object parseObject(String source, ParsePosition parsePosition) {
        return parse(source, parsePosition);
    }

    public final String format(double number) {
        return format(number, new StringBuffer(), new FieldPosition(NUMBERSTYLE)).toString();
    }

    public final String format(long number) {
        StringBuffer buf = new StringBuffer(19);
        format(number, buf, new FieldPosition(NUMBERSTYLE));
        return buf.toString();
    }

    public final String format(BigInteger number) {
        return format(number, new StringBuffer(), new FieldPosition(NUMBERSTYLE)).toString();
    }

    public final String format(java.math.BigDecimal number) {
        return format(number, new StringBuffer(), new FieldPosition(NUMBERSTYLE)).toString();
    }

    public final String format(BigDecimal number) {
        return format(number, new StringBuffer(), new FieldPosition(NUMBERSTYLE)).toString();
    }

    public final String format(CurrencyAmount currAmt) {
        return format(currAmt, new StringBuffer(), new FieldPosition(NUMBERSTYLE)).toString();
    }

    public StringBuffer format(CurrencyAmount currAmt, StringBuffer toAppendTo, FieldPosition pos) {
        Currency save = getCurrency();
        Currency curr = currAmt.getCurrency();
        boolean same = curr.equals(save);
        if (!same) {
            setCurrency(curr);
        }
        format(currAmt.getNumber(), toAppendTo, pos);
        if (!same) {
            setCurrency(save);
        }
        return toAppendTo;
    }

    public Number parse(String text) throws ParseException {
        ParsePosition parsePosition = new ParsePosition(NUMBERSTYLE);
        Number result = parse(text, parsePosition);
        if (parsePosition.getIndex() != 0) {
            return result;
        }
        throw new ParseException("Unparseable number: \"" + text + '\"', parsePosition.getErrorIndex());
    }

    public CurrencyAmount parseCurrency(CharSequence text, ParsePosition pos) {
        Number n = parse(text.toString(), pos);
        if (n == null) {
            return null;
        }
        return new CurrencyAmount(n, getEffectiveCurrency());
    }

    public boolean isParseIntegerOnly() {
        return this.parseIntegerOnly;
    }

    public void setParseIntegerOnly(boolean value) {
        this.parseIntegerOnly = value;
    }

    public void setParseStrict(boolean value) {
        this.parseStrict = value;
    }

    public boolean isParseStrict() {
        return this.parseStrict;
    }

    public void setContext(DisplayContext context) {
        if (context.type() == Type.CAPITALIZATION) {
            this.capitalizationSetting = context;
        }
    }

    public DisplayContext getContext(Type type) {
        return (type != Type.CAPITALIZATION || this.capitalizationSetting == null) ? DisplayContext.CAPITALIZATION_NONE : this.capitalizationSetting;
    }

    public static final NumberFormat getInstance() {
        return getInstance(ULocale.getDefault(Category.FORMAT), (int) NUMBERSTYLE);
    }

    public static NumberFormat getInstance(Locale inLocale) {
        return getInstance(ULocale.forLocale(inLocale), (int) NUMBERSTYLE);
    }

    public static NumberFormat getInstance(ULocale inLocale) {
        return getInstance(inLocale, (int) NUMBERSTYLE);
    }

    public static final NumberFormat getInstance(int style) {
        return getInstance(ULocale.getDefault(Category.FORMAT), style);
    }

    public static NumberFormat getInstance(Locale inLocale, int style) {
        return getInstance(ULocale.forLocale(inLocale), style);
    }

    public static final NumberFormat getNumberInstance() {
        return getInstance(ULocale.getDefault(Category.FORMAT), (int) NUMBERSTYLE);
    }

    public static NumberFormat getNumberInstance(Locale inLocale) {
        return getInstance(ULocale.forLocale(inLocale), (int) NUMBERSTYLE);
    }

    public static NumberFormat getNumberInstance(ULocale inLocale) {
        return getInstance(inLocale, (int) NUMBERSTYLE);
    }

    public static final NumberFormat getIntegerInstance() {
        return getInstance(ULocale.getDefault(Category.FORMAT), (int) INTEGERSTYLE);
    }

    public static NumberFormat getIntegerInstance(Locale inLocale) {
        return getInstance(ULocale.forLocale(inLocale), (int) INTEGERSTYLE);
    }

    public static NumberFormat getIntegerInstance(ULocale inLocale) {
        return getInstance(inLocale, (int) INTEGERSTYLE);
    }

    public static final NumberFormat getCurrencyInstance() {
        return getInstance(ULocale.getDefault(Category.FORMAT), (int) FRACTION_FIELD);
    }

    public static NumberFormat getCurrencyInstance(Locale inLocale) {
        return getInstance(ULocale.forLocale(inLocale), (int) FRACTION_FIELD);
    }

    public static NumberFormat getCurrencyInstance(ULocale inLocale) {
        return getInstance(inLocale, (int) FRACTION_FIELD);
    }

    public static final NumberFormat getPercentInstance() {
        return getInstance(ULocale.getDefault(Category.FORMAT), (int) currentSerialVersion);
    }

    public static NumberFormat getPercentInstance(Locale inLocale) {
        return getInstance(ULocale.forLocale(inLocale), (int) currentSerialVersion);
    }

    public static NumberFormat getPercentInstance(ULocale inLocale) {
        return getInstance(inLocale, (int) currentSerialVersion);
    }

    public static final NumberFormat getScientificInstance() {
        return getInstance(ULocale.getDefault(Category.FORMAT), (int) SCIENTIFICSTYLE);
    }

    public static NumberFormat getScientificInstance(Locale inLocale) {
        return getInstance(ULocale.forLocale(inLocale), (int) SCIENTIFICSTYLE);
    }

    public static NumberFormat getScientificInstance(ULocale inLocale) {
        return getInstance(inLocale, (int) SCIENTIFICSTYLE);
    }

    private static NumberFormatShim getShim() {
        if (shim == null) {
            try {
                shim = (NumberFormatShim) Class.forName("android.icu.text.NumberFormatServiceShim").newInstance();
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

    public static Object registerFactory(NumberFormatFactory factory) {
        if (factory != null) {
            return getShim().registerFactory(factory);
        }
        throw new IllegalArgumentException("factory must not be null");
    }

    public static boolean unregister(Object registryKey) {
        if (registryKey == null) {
            throw new IllegalArgumentException("registryKey must not be null");
        } else if (shim == null) {
            return -assertionsDisabled;
        } else {
            return shim.unregister(registryKey);
        }
    }

    public int hashCode() {
        return (this.maximumIntegerDigits * 37) + this.maxFractionDigits;
    }

    public boolean equals(Object obj) {
        boolean z = true;
        if (obj == null) {
            return -assertionsDisabled;
        }
        if (this == obj) {
            return true;
        }
        if (getClass() != obj.getClass()) {
            return -assertionsDisabled;
        }
        NumberFormat other = (NumberFormat) obj;
        if (this.maximumIntegerDigits != other.maximumIntegerDigits || this.minimumIntegerDigits != other.minimumIntegerDigits || this.maximumFractionDigits != other.maximumFractionDigits || this.minimumFractionDigits != other.minimumFractionDigits || this.groupingUsed != other.groupingUsed || this.parseIntegerOnly != other.parseIntegerOnly || this.parseStrict != other.parseStrict) {
            z = -assertionsDisabled;
        } else if (this.capitalizationSetting != other.capitalizationSetting) {
            z = -assertionsDisabled;
        }
        return z;
    }

    public Object clone() {
        return (NumberFormat) super.clone();
    }

    public boolean isGroupingUsed() {
        return this.groupingUsed;
    }

    public void setGroupingUsed(boolean newValue) {
        this.groupingUsed = newValue;
    }

    public int getMaximumIntegerDigits() {
        return this.maximumIntegerDigits;
    }

    public void setMaximumIntegerDigits(int newValue) {
        this.maximumIntegerDigits = Math.max(NUMBERSTYLE, newValue);
        if (this.minimumIntegerDigits > this.maximumIntegerDigits) {
            this.minimumIntegerDigits = this.maximumIntegerDigits;
        }
    }

    public int getMinimumIntegerDigits() {
        return this.minimumIntegerDigits;
    }

    public void setMinimumIntegerDigits(int newValue) {
        this.minimumIntegerDigits = Math.max(NUMBERSTYLE, newValue);
        if (this.minimumIntegerDigits > this.maximumIntegerDigits) {
            this.maximumIntegerDigits = this.minimumIntegerDigits;
        }
    }

    public int getMaximumFractionDigits() {
        return this.maximumFractionDigits;
    }

    public void setMaximumFractionDigits(int newValue) {
        this.maximumFractionDigits = Math.max(NUMBERSTYLE, newValue);
        if (this.maximumFractionDigits < this.minimumFractionDigits) {
            this.minimumFractionDigits = this.maximumFractionDigits;
        }
    }

    public int getMinimumFractionDigits() {
        return this.minimumFractionDigits;
    }

    public void setMinimumFractionDigits(int newValue) {
        this.minimumFractionDigits = Math.max(NUMBERSTYLE, newValue);
        if (this.maximumFractionDigits < this.minimumFractionDigits) {
            this.maximumFractionDigits = this.minimumFractionDigits;
        }
    }

    public void setCurrency(Currency theCurrency) {
        this.currency = theCurrency;
    }

    public Currency getCurrency() {
        return this.currency;
    }

    @Deprecated
    protected Currency getEffectiveCurrency() {
        Currency c = getCurrency();
        if (c != null) {
            return c;
        }
        ULocale uloc = getLocale(ULocale.VALID_LOCALE);
        if (uloc == null) {
            uloc = ULocale.getDefault(Category.FORMAT);
        }
        return Currency.getInstance(uloc);
    }

    public int getRoundingMode() {
        throw new UnsupportedOperationException("getRoundingMode must be implemented by the subclass implementation.");
    }

    public void setRoundingMode(int roundingMode) {
        throw new UnsupportedOperationException("setRoundingMode must be implemented by the subclass implementation.");
    }

    public static NumberFormat getInstance(ULocale desiredLocale, int choice) {
        if (choice >= 0 && choice <= STANDARDCURRENCYSTYLE) {
            return getShim().createInstance(desiredLocale, choice);
        }
        throw new IllegalArgumentException("choice should be from NUMBERSTYLE to STANDARDCURRENCYSTYLE");
    }

    static NumberFormat createInstance(ULocale desiredLocale, int choice) {
        NumberingSystem ns;
        NumberFormat format;
        NumberFormat f;
        String pattern = getPattern(desiredLocale, choice);
        DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols(desiredLocale);
        if (!(choice == FRACTION_FIELD || choice == ISOCURRENCYSTYLE || choice == ACCOUNTINGCURRENCYSTYLE || choice == CASHCURRENCYSTYLE)) {
            if (choice == STANDARDCURRENCYSTYLE) {
            }
            if (choice == ISOCURRENCYSTYLE) {
                pattern = pattern.replace("\u00a4", doubleCurrencyStr);
            }
            ns = NumberingSystem.getInstance(desiredLocale);
            if (ns == null) {
                return null;
            }
            if (ns == null && ns.isAlgorithmic()) {
                String nsRuleSetName;
                ULocale nsLoc;
                int desiredRulesType = INTEGERSTYLE;
                String nsDesc = ns.getDescription();
                int firstSlash = nsDesc.indexOf("/");
                int lastSlash = nsDesc.lastIndexOf("/");
                if (lastSlash > firstSlash) {
                    String nsLocID = nsDesc.substring(NUMBERSTYLE, firstSlash);
                    String nsRuleSetGroup = nsDesc.substring(firstSlash + FRACTION_FIELD, lastSlash);
                    nsRuleSetName = nsDesc.substring(lastSlash + FRACTION_FIELD);
                    nsLoc = new ULocale(nsLocID);
                    if (nsRuleSetGroup.equals("SpelloutRules")) {
                        desiredRulesType = FRACTION_FIELD;
                    }
                } else {
                    nsLoc = desiredLocale;
                    nsRuleSetName = nsDesc;
                }
                NumberFormat r = new RuleBasedNumberFormat(nsLoc, desiredRulesType);
                r.setDefaultRuleSet(nsRuleSetName);
                format = r;
            } else {
                f = new DecimalFormat(pattern, decimalFormatSymbols, choice);
                if (choice == INTEGERSTYLE) {
                    f.setMaximumFractionDigits(NUMBERSTYLE);
                    f.setDecimalSeparatorAlwaysShown(-assertionsDisabled);
                    f.setParseIntegerOnly(true);
                }
                if (choice == CASHCURRENCYSTYLE) {
                    f.setCurrencyUsage(CurrencyUsage.CASH);
                }
                format = f;
            }
            format.setLocale(decimalFormatSymbols.getLocale(ULocale.VALID_LOCALE), decimalFormatSymbols.getLocale(ULocale.ACTUAL_LOCALE));
            return format;
        }
        String temp = decimalFormatSymbols.getCurrencyPattern();
        if (temp != null) {
            pattern = temp;
        }
        if (choice == ISOCURRENCYSTYLE) {
            pattern = pattern.replace("\u00a4", doubleCurrencyStr);
        }
        ns = NumberingSystem.getInstance(desiredLocale);
        if (ns == null) {
            return null;
        }
        if (ns == null) {
        }
        f = new DecimalFormat(pattern, decimalFormatSymbols, choice);
        if (choice == INTEGERSTYLE) {
            f.setMaximumFractionDigits(NUMBERSTYLE);
            f.setDecimalSeparatorAlwaysShown(-assertionsDisabled);
            f.setParseIntegerOnly(true);
        }
        if (choice == CASHCURRENCYSTYLE) {
            f.setCurrencyUsage(CurrencyUsage.CASH);
        }
        format = f;
        format.setLocale(decimalFormatSymbols.getLocale(ULocale.VALID_LOCALE), decimalFormatSymbols.getLocale(ULocale.ACTUAL_LOCALE));
        return format;
    }

    @Deprecated
    protected static String getPattern(Locale forLocale, int choice) {
        return getPattern(ULocale.forLocale(forLocale), choice);
    }

    protected static String getPattern(ULocale forLocale, int choice) {
        String patternKey;
        switch (choice) {
            case NUMBERSTYLE /*0*/:
            case INTEGERSTYLE /*4*/:
                patternKey = "decimalFormat";
                break;
            case FRACTION_FIELD /*1*/:
                String cfKeyValue = forLocale.getKeywordValue("cf");
                if (cfKeyValue != null && cfKeyValue.equals("account")) {
                    patternKey = "accountingFormat";
                    break;
                }
                patternKey = "currencyFormat";
                break;
            case currentSerialVersion /*2*/:
                patternKey = "percentFormat";
                break;
            case SCIENTIFICSTYLE /*3*/:
                patternKey = "scientificFormat";
                break;
            case ISOCURRENCYSTYLE /*5*/:
            case PLURALCURRENCYSTYLE /*6*/:
            case CASHCURRENCYSTYLE /*8*/:
            case STANDARDCURRENCYSTYLE /*9*/:
                patternKey = "currencyFormat";
                break;
            case ACCOUNTINGCURRENCYSTYLE /*7*/:
                patternKey = "accountingFormat";
                break;
            default:
                if (-assertionsDisabled) {
                    patternKey = "decimalFormat";
                    break;
                }
                throw new AssertionError();
        }
        ICUResourceBundle rb = (ICUResourceBundle) UResourceBundle.getBundleInstance(ICUResourceBundle.ICU_BASE_NAME, forLocale);
        try {
            return rb.getStringWithFallback("NumberElements/" + NumberingSystem.getInstance(forLocale).getName() + "/patterns/" + patternKey);
        } catch (MissingResourceException e) {
            return rb.getStringWithFallback("NumberElements/latn/patterns/" + patternKey);
        }
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        if (this.serialVersionOnStream < FRACTION_FIELD) {
            this.maximumIntegerDigits = this.maxIntegerDigits;
            this.minimumIntegerDigits = this.minIntegerDigits;
            this.maximumFractionDigits = this.maxFractionDigits;
            this.minimumFractionDigits = this.minFractionDigits;
        }
        if (this.serialVersionOnStream < currentSerialVersion) {
            this.capitalizationSetting = DisplayContext.CAPITALIZATION_NONE;
        }
        if (this.minimumIntegerDigits > this.maximumIntegerDigits || this.minimumFractionDigits > this.maximumFractionDigits || this.minimumIntegerDigits < 0 || this.minimumFractionDigits < 0) {
            throw new InvalidObjectException("Digit count range invalid");
        }
        this.serialVersionOnStream = currentSerialVersion;
    }

    private void writeObject(ObjectOutputStream stream) throws IOException {
        byte b;
        byte b2 = Bidi.LEVEL_DEFAULT_RTL;
        if (this.maximumIntegerDigits > Opcodes.OP_NEG_FLOAT) {
            b = Bidi.LEVEL_DEFAULT_RTL;
        } else {
            b = (byte) this.maximumIntegerDigits;
        }
        this.maxIntegerDigits = b;
        if (this.minimumIntegerDigits > Opcodes.OP_NEG_FLOAT) {
            b = Bidi.LEVEL_DEFAULT_RTL;
        } else {
            b = (byte) this.minimumIntegerDigits;
        }
        this.minIntegerDigits = b;
        if (this.maximumFractionDigits > Opcodes.OP_NEG_FLOAT) {
            b = Bidi.LEVEL_DEFAULT_RTL;
        } else {
            b = (byte) this.maximumFractionDigits;
        }
        this.maxFractionDigits = b;
        if (this.minimumFractionDigits <= Opcodes.OP_NEG_FLOAT) {
            b2 = (byte) this.minimumFractionDigits;
        }
        this.minFractionDigits = b2;
        stream.defaultWriteObject();
    }

    public NumberFormat() {
        this.groupingUsed = true;
        this.maxIntegerDigits = (byte) 40;
        this.minIntegerDigits = (byte) 1;
        this.maxFractionDigits = (byte) 3;
        this.minFractionDigits = (byte) 0;
        this.parseIntegerOnly = -assertionsDisabled;
        this.maximumIntegerDigits = 40;
        this.minimumIntegerDigits = FRACTION_FIELD;
        this.maximumFractionDigits = SCIENTIFICSTYLE;
        this.minimumFractionDigits = NUMBERSTYLE;
        this.serialVersionOnStream = currentSerialVersion;
        this.capitalizationSetting = DisplayContext.CAPITALIZATION_NONE;
    }
}
