package java.text;

import android.icu.text.NumberFormat;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectInputStream.GetField;
import java.io.ObjectOutputStream;
import java.io.ObjectOutputStream.PutField;
import java.io.ObjectStreamField;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.AttributedCharacterIterator.Attribute;
import java.text.Format.Field;
import java.util.Currency;
import java.util.Locale;
import java.util.Locale.Category;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import libcore.icu.LocaleData;
import sun.util.calendar.BaseCalendar;
import sun.util.locale.LanguageTag;

public class DecimalFormat extends NumberFormat {
    private static final /* synthetic */ int[] -java-math-RoundingModeSwitchesValues = null;
    static final int DOUBLE_FRACTION_DIGITS = 340;
    static final int DOUBLE_INTEGER_DIGITS = 309;
    static final int MAXIMUM_FRACTION_DIGITS = Integer.MAX_VALUE;
    static final int MAXIMUM_INTEGER_DIGITS = Integer.MAX_VALUE;
    private static final ConcurrentMap<Locale, String> cachedLocaleData = null;
    private static final int currentSerialVersion = 4;
    private static final ObjectStreamField[] serialPersistentFields = null;
    static final long serialVersionUID = 864413376551465018L;
    private transient android.icu.text.DecimalFormat icuDecimalFormat;
    private int maximumFractionDigits;
    private int maximumIntegerDigits;
    private int minimumFractionDigits;
    private int minimumIntegerDigits;
    private RoundingMode roundingMode;
    private DecimalFormatSymbols symbols;

    private static /* synthetic */ int[] -getjava-math-RoundingModeSwitchesValues() {
        if (-java-math-RoundingModeSwitchesValues != null) {
            return -java-math-RoundingModeSwitchesValues;
        }
        int[] iArr = new int[RoundingMode.values().length];
        try {
            iArr[RoundingMode.CEILING.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[RoundingMode.DOWN.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[RoundingMode.FLOOR.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[RoundingMode.HALF_DOWN.ordinal()] = currentSerialVersion;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[RoundingMode.HALF_EVEN.ordinal()] = 5;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[RoundingMode.HALF_UP.ordinal()] = 6;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[RoundingMode.UNNECESSARY.ordinal()] = 7;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[RoundingMode.UP.ordinal()] = 8;
        } catch (NoSuchFieldError e8) {
        }
        -java-math-RoundingModeSwitchesValues = iArr;
        return iArr;
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.text.DecimalFormat.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.text.DecimalFormat.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: java.text.DecimalFormat.<clinit>():void");
    }

    public DecimalFormat() {
        this.roundingMode = RoundingMode.HALF_EVEN;
        Locale def = Locale.getDefault(Category.FORMAT);
        String pattern = (String) cachedLocaleData.get(def);
        if (pattern == null) {
            pattern = LocaleData.get(def).numberPattern;
            cachedLocaleData.putIfAbsent(def, pattern);
        }
        this.symbols = new DecimalFormatSymbols(def);
        init(pattern);
    }

    public DecimalFormat(String pattern) {
        this.roundingMode = RoundingMode.HALF_EVEN;
        this.symbols = new DecimalFormatSymbols(Locale.getDefault(Category.FORMAT));
        init(pattern);
    }

    public DecimalFormat(String pattern, DecimalFormatSymbols symbols) {
        this.roundingMode = RoundingMode.HALF_EVEN;
        this.symbols = (DecimalFormatSymbols) symbols.clone();
        init(pattern);
    }

    private void init(String pattern) {
        this.icuDecimalFormat = new android.icu.text.DecimalFormat(pattern, this.symbols.getIcuDecimalFormatSymbols());
        updateFieldsFromIcu();
    }

    private static FieldPosition getIcuFieldPosition(FieldPosition fp) {
        if (fp.getFieldAttribute() == null) {
            return fp;
        }
        Field attribute;
        if (fp.getFieldAttribute() == NumberFormat.Field.INTEGER) {
            attribute = NumberFormat.Field.INTEGER;
        } else if (fp.getFieldAttribute() == NumberFormat.Field.FRACTION) {
            attribute = NumberFormat.Field.FRACTION;
        } else if (fp.getFieldAttribute() == NumberFormat.Field.DECIMAL_SEPARATOR) {
            attribute = NumberFormat.Field.DECIMAL_SEPARATOR;
        } else if (fp.getFieldAttribute() == NumberFormat.Field.EXPONENT_SYMBOL) {
            attribute = NumberFormat.Field.EXPONENT_SYMBOL;
        } else if (fp.getFieldAttribute() == NumberFormat.Field.EXPONENT_SIGN) {
            attribute = NumberFormat.Field.EXPONENT_SIGN;
        } else if (fp.getFieldAttribute() == NumberFormat.Field.EXPONENT) {
            attribute = NumberFormat.Field.EXPONENT;
        } else if (fp.getFieldAttribute() == NumberFormat.Field.GROUPING_SEPARATOR) {
            attribute = NumberFormat.Field.GROUPING_SEPARATOR;
        } else if (fp.getFieldAttribute() == NumberFormat.Field.CURRENCY) {
            attribute = NumberFormat.Field.CURRENCY;
        } else if (fp.getFieldAttribute() == NumberFormat.Field.PERCENT) {
            attribute = NumberFormat.Field.PERCENT;
        } else if (fp.getFieldAttribute() == NumberFormat.Field.PERMILLE) {
            attribute = NumberFormat.Field.PERMILLE;
        } else if (fp.getFieldAttribute() == NumberFormat.Field.SIGN) {
            attribute = NumberFormat.Field.SIGN;
        } else {
            throw new IllegalArgumentException("Unexpected field position attribute type.");
        }
        FieldPosition icuFieldPosition = new FieldPosition(attribute);
        icuFieldPosition.setBeginIndex(fp.getBeginIndex());
        icuFieldPosition.setEndIndex(fp.getEndIndex());
        return icuFieldPosition;
    }

    private static NumberFormat.Field toJavaFieldAttribute(Attribute icuAttribute) {
        if (icuAttribute.getName().equals(NumberFormat.Field.INTEGER.getName())) {
            return NumberFormat.Field.INTEGER;
        }
        if (icuAttribute.getName().equals(NumberFormat.Field.CURRENCY.getName())) {
            return NumberFormat.Field.CURRENCY;
        }
        if (icuAttribute.getName().equals(NumberFormat.Field.DECIMAL_SEPARATOR.getName())) {
            return NumberFormat.Field.DECIMAL_SEPARATOR;
        }
        if (icuAttribute.getName().equals(NumberFormat.Field.EXPONENT.getName())) {
            return NumberFormat.Field.EXPONENT;
        }
        if (icuAttribute.getName().equals(NumberFormat.Field.EXPONENT_SIGN.getName())) {
            return NumberFormat.Field.EXPONENT_SIGN;
        }
        if (icuAttribute.getName().equals(NumberFormat.Field.EXPONENT_SYMBOL.getName())) {
            return NumberFormat.Field.EXPONENT_SYMBOL;
        }
        if (icuAttribute.getName().equals(NumberFormat.Field.FRACTION.getName())) {
            return NumberFormat.Field.FRACTION;
        }
        if (icuAttribute.getName().equals(NumberFormat.Field.GROUPING_SEPARATOR.getName())) {
            return NumberFormat.Field.GROUPING_SEPARATOR;
        }
        if (icuAttribute.getName().equals(NumberFormat.Field.SIGN.getName())) {
            return NumberFormat.Field.SIGN;
        }
        if (icuAttribute.getName().equals(NumberFormat.Field.PERCENT.getName())) {
            return NumberFormat.Field.PERCENT;
        }
        if (icuAttribute.getName().equals(NumberFormat.Field.PERMILLE.getName())) {
            return NumberFormat.Field.PERMILLE;
        }
        throw new IllegalArgumentException("Unrecognized attribute: " + icuAttribute.getName());
    }

    public final StringBuffer format(Object number, StringBuffer toAppendTo, FieldPosition pos) {
        if ((number instanceof Long) || (number instanceof Integer) || (number instanceof Short) || (number instanceof Byte) || (number instanceof AtomicInteger) || (number instanceof AtomicLong) || ((number instanceof BigInteger) && ((BigInteger) number).bitLength() < 64)) {
            return format(((Number) number).longValue(), toAppendTo, pos);
        }
        if (number instanceof BigDecimal) {
            return format((BigDecimal) number, toAppendTo, pos);
        }
        if (number instanceof BigInteger) {
            return format((BigInteger) number, toAppendTo, pos);
        }
        if (number instanceof Number) {
            return format(((Number) number).doubleValue(), toAppendTo, pos);
        }
        throw new IllegalArgumentException("Cannot format given Object as a Number");
    }

    public StringBuffer format(double number, StringBuffer result, FieldPosition fieldPosition) {
        FieldPosition icuFieldPosition = getIcuFieldPosition(fieldPosition);
        this.icuDecimalFormat.format(number, result, icuFieldPosition);
        fieldPosition.setBeginIndex(icuFieldPosition.getBeginIndex());
        fieldPosition.setEndIndex(icuFieldPosition.getEndIndex());
        return result;
    }

    public StringBuffer format(long number, StringBuffer result, FieldPosition fieldPosition) {
        FieldPosition icuFieldPosition = getIcuFieldPosition(fieldPosition);
        this.icuDecimalFormat.format(number, result, icuFieldPosition);
        fieldPosition.setBeginIndex(icuFieldPosition.getBeginIndex());
        fieldPosition.setEndIndex(icuFieldPosition.getEndIndex());
        return result;
    }

    private StringBuffer format(BigDecimal number, StringBuffer result, FieldPosition fieldPosition) {
        FieldPosition icuFieldPosition = getIcuFieldPosition(fieldPosition);
        this.icuDecimalFormat.format(number, result, fieldPosition);
        fieldPosition.setBeginIndex(icuFieldPosition.getBeginIndex());
        fieldPosition.setEndIndex(icuFieldPosition.getEndIndex());
        return result;
    }

    private StringBuffer format(BigInteger number, StringBuffer result, FieldPosition fieldPosition) {
        FieldPosition icuFieldPosition = getIcuFieldPosition(fieldPosition);
        this.icuDecimalFormat.format(number, result, fieldPosition);
        fieldPosition.setBeginIndex(icuFieldPosition.getBeginIndex());
        fieldPosition.setEndIndex(icuFieldPosition.getEndIndex());
        return result;
    }

    public AttributedCharacterIterator formatToCharacterIterator(Object obj) {
        if (obj == null) {
            throw new NullPointerException("object == null");
        }
        int i;
        AttributedCharacterIterator original = this.icuDecimalFormat.formatToCharacterIterator(obj);
        StringBuilder textBuilder = new StringBuilder(original.getEndIndex() - original.getBeginIndex());
        for (i = original.getBeginIndex(); i < original.getEndIndex(); i++) {
            textBuilder.append(original.current());
            original.next();
        }
        AttributedString result = new AttributedString(textBuilder.toString());
        for (i = original.getBeginIndex(); i < original.getEndIndex(); i++) {
            original.setIndex(i);
            for (Attribute attribute : original.getAttributes().keySet()) {
                int start = original.getRunStart();
                int end = original.getRunLimit();
                NumberFormat.Field javaAttr = toJavaFieldAttribute(attribute);
                result.addAttribute(javaAttr, javaAttr, start, end);
            }
        }
        return result.getIterator();
    }

    public Number parse(String text, ParsePosition pos) {
        if (pos.index < 0 || pos.index >= text.length()) {
            return null;
        }
        Number number = this.icuDecimalFormat.parse(text, pos);
        if (number == null) {
            return null;
        }
        if (isParseBigDecimal()) {
            if (number instanceof Long) {
                return new BigDecimal(number.longValue());
            }
            if ((number instanceof Double) && !((Double) number).isInfinite() && !((Double) number).isNaN()) {
                return new BigDecimal(number.toString());
            }
            if ((number instanceof Double) && (((Double) number).isNaN() || ((Double) number).isInfinite())) {
                return number;
            }
            if (number instanceof android.icu.math.BigDecimal) {
                return ((android.icu.math.BigDecimal) number).toBigDecimal();
            }
        }
        if ((number instanceof android.icu.math.BigDecimal) || (number instanceof BigInteger)) {
            return Double.valueOf(number.doubleValue());
        }
        if (isParseIntegerOnly() && number.equals(new Double(-0.0d))) {
            return Long.valueOf(0);
        }
        return number;
    }

    public DecimalFormatSymbols getDecimalFormatSymbols() {
        return DecimalFormatSymbols.fromIcuInstance(this.icuDecimalFormat.getDecimalFormatSymbols());
    }

    public void setDecimalFormatSymbols(DecimalFormatSymbols newSymbols) {
        try {
            this.symbols = (DecimalFormatSymbols) newSymbols.clone();
            this.icuDecimalFormat.setDecimalFormatSymbols(this.symbols.getIcuDecimalFormatSymbols());
        } catch (Exception e) {
        }
    }

    public String getPositivePrefix() {
        return this.icuDecimalFormat.getPositivePrefix();
    }

    public void setPositivePrefix(String newValue) {
        this.icuDecimalFormat.setPositivePrefix(newValue);
    }

    public String getNegativePrefix() {
        return this.icuDecimalFormat.getNegativePrefix();
    }

    public void setNegativePrefix(String newValue) {
        this.icuDecimalFormat.setNegativePrefix(newValue);
    }

    public String getPositiveSuffix() {
        return this.icuDecimalFormat.getPositiveSuffix();
    }

    public void setPositiveSuffix(String newValue) {
        this.icuDecimalFormat.setPositiveSuffix(newValue);
    }

    public String getNegativeSuffix() {
        return this.icuDecimalFormat.getNegativeSuffix();
    }

    public void setNegativeSuffix(String newValue) {
        this.icuDecimalFormat.setNegativeSuffix(newValue);
    }

    public int getMultiplier() {
        return this.icuDecimalFormat.getMultiplier();
    }

    public void setMultiplier(int newValue) {
        this.icuDecimalFormat.setMultiplier(newValue);
    }

    public int getGroupingSize() {
        return this.icuDecimalFormat.getGroupingSize();
    }

    public void setGroupingSize(int newValue) {
        this.icuDecimalFormat.setGroupingSize(newValue);
    }

    public boolean isGroupingUsed() {
        return this.icuDecimalFormat.isGroupingUsed();
    }

    public void setGroupingUsed(boolean newValue) {
        this.icuDecimalFormat.setGroupingUsed(newValue);
    }

    public boolean isDecimalSeparatorAlwaysShown() {
        return this.icuDecimalFormat.isDecimalSeparatorAlwaysShown();
    }

    public void setDecimalSeparatorAlwaysShown(boolean newValue) {
        this.icuDecimalFormat.setDecimalSeparatorAlwaysShown(newValue);
    }

    public boolean isParseBigDecimal() {
        return this.icuDecimalFormat.isParseBigDecimal();
    }

    public void setParseBigDecimal(boolean newValue) {
        this.icuDecimalFormat.setParseBigDecimal(newValue);
    }

    public void setParseIntegerOnly(boolean value) {
        super.setParseIntegerOnly(value);
        this.icuDecimalFormat.setParseIntegerOnly(value);
    }

    public boolean isParseIntegerOnly() {
        return this.icuDecimalFormat.isParseIntegerOnly();
    }

    public Object clone() {
        try {
            DecimalFormat other = (DecimalFormat) super.clone();
            other.icuDecimalFormat = (android.icu.text.DecimalFormat) this.icuDecimalFormat.clone();
            other.symbols = (DecimalFormatSymbols) this.symbols.clone();
            return other;
        } catch (Exception e) {
            throw new InternalError();
        }
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof DecimalFormat)) {
            return false;
        }
        DecimalFormat other = (DecimalFormat) obj;
        if (this.icuDecimalFormat.equals(other.icuDecimalFormat)) {
            z = compareIcuRoundingIncrement(other.icuDecimalFormat);
        }
        return z;
    }

    private boolean compareIcuRoundingIncrement(android.icu.text.DecimalFormat other) {
        boolean z = false;
        BigDecimal increment = this.icuDecimalFormat.getRoundingIncrement();
        if (increment != null) {
            if (other.getRoundingIncrement() != null) {
                z = increment.equals(other.getRoundingIncrement());
            }
            return z;
        }
        if (other.getRoundingIncrement() == null) {
            z = true;
        }
        return z;
    }

    public int hashCode() {
        return (super.hashCode() * 37) + getPositivePrefix().hashCode();
    }

    public String toPattern() {
        return this.icuDecimalFormat.toPattern();
    }

    public String toLocalizedPattern() {
        return this.icuDecimalFormat.toLocalizedPattern();
    }

    public void applyPattern(String pattern) {
        this.icuDecimalFormat.applyPattern(pattern);
        updateFieldsFromIcu();
    }

    public void applyLocalizedPattern(String pattern) {
        this.icuDecimalFormat.applyLocalizedPattern(pattern);
        updateFieldsFromIcu();
    }

    private void updateFieldsFromIcu() {
        if (this.icuDecimalFormat.getMaximumIntegerDigits() == DOUBLE_INTEGER_DIGITS) {
            this.icuDecimalFormat.setMaximumIntegerDigits(2000000000);
        }
        this.maximumIntegerDigits = this.icuDecimalFormat.getMaximumIntegerDigits();
        this.minimumIntegerDigits = this.icuDecimalFormat.getMinimumIntegerDigits();
        this.maximumFractionDigits = this.icuDecimalFormat.getMaximumFractionDigits();
        this.minimumFractionDigits = this.icuDecimalFormat.getMinimumFractionDigits();
    }

    public void setMaximumIntegerDigits(int newValue) {
        int i;
        int i2 = DOUBLE_INTEGER_DIGITS;
        this.maximumIntegerDigits = Math.min(Math.max(0, newValue), (int) MAXIMUM_INTEGER_DIGITS);
        if (this.maximumIntegerDigits > DOUBLE_INTEGER_DIGITS) {
            i = DOUBLE_INTEGER_DIGITS;
        } else {
            i = this.maximumIntegerDigits;
        }
        super.setMaximumIntegerDigits(i);
        if (this.minimumIntegerDigits > this.maximumIntegerDigits) {
            this.minimumIntegerDigits = this.maximumIntegerDigits;
            if (this.minimumIntegerDigits <= DOUBLE_INTEGER_DIGITS) {
                i2 = this.minimumIntegerDigits;
            }
            super.setMinimumIntegerDigits(i2);
        }
        this.icuDecimalFormat.setMaximumIntegerDigits(getMaximumIntegerDigits());
    }

    public void setMinimumIntegerDigits(int newValue) {
        int i;
        int i2 = DOUBLE_INTEGER_DIGITS;
        this.minimumIntegerDigits = Math.min(Math.max(0, newValue), (int) MAXIMUM_INTEGER_DIGITS);
        if (this.minimumIntegerDigits > DOUBLE_INTEGER_DIGITS) {
            i = DOUBLE_INTEGER_DIGITS;
        } else {
            i = this.minimumIntegerDigits;
        }
        super.setMinimumIntegerDigits(i);
        if (this.minimumIntegerDigits > this.maximumIntegerDigits) {
            this.maximumIntegerDigits = this.minimumIntegerDigits;
            if (this.maximumIntegerDigits <= DOUBLE_INTEGER_DIGITS) {
                i2 = this.maximumIntegerDigits;
            }
            super.setMaximumIntegerDigits(i2);
        }
        this.icuDecimalFormat.setMinimumIntegerDigits(getMinimumIntegerDigits());
    }

    public void setMaximumFractionDigits(int newValue) {
        int i;
        int i2 = DOUBLE_FRACTION_DIGITS;
        this.maximumFractionDigits = Math.min(Math.max(0, newValue), (int) MAXIMUM_INTEGER_DIGITS);
        if (this.maximumFractionDigits > DOUBLE_FRACTION_DIGITS) {
            i = DOUBLE_FRACTION_DIGITS;
        } else {
            i = this.maximumFractionDigits;
        }
        super.setMaximumFractionDigits(i);
        if (this.minimumFractionDigits > this.maximumFractionDigits) {
            this.minimumFractionDigits = this.maximumFractionDigits;
            if (this.minimumFractionDigits <= DOUBLE_FRACTION_DIGITS) {
                i2 = this.minimumFractionDigits;
            }
            super.setMinimumFractionDigits(i2);
        }
        this.icuDecimalFormat.setMaximumFractionDigits(getMaximumFractionDigits());
    }

    public void setMinimumFractionDigits(int newValue) {
        int i;
        int i2 = DOUBLE_FRACTION_DIGITS;
        this.minimumFractionDigits = Math.min(Math.max(0, newValue), (int) MAXIMUM_INTEGER_DIGITS);
        if (this.minimumFractionDigits > DOUBLE_FRACTION_DIGITS) {
            i = DOUBLE_FRACTION_DIGITS;
        } else {
            i = this.minimumFractionDigits;
        }
        super.setMinimumFractionDigits(i);
        if (this.minimumFractionDigits > this.maximumFractionDigits) {
            this.maximumFractionDigits = this.minimumFractionDigits;
            if (this.maximumFractionDigits <= DOUBLE_FRACTION_DIGITS) {
                i2 = this.maximumFractionDigits;
            }
            super.setMaximumFractionDigits(i2);
        }
        this.icuDecimalFormat.setMinimumFractionDigits(getMinimumFractionDigits());
    }

    public int getMaximumIntegerDigits() {
        return this.maximumIntegerDigits;
    }

    public int getMinimumIntegerDigits() {
        return this.minimumIntegerDigits;
    }

    public int getMaximumFractionDigits() {
        return this.maximumFractionDigits;
    }

    public int getMinimumFractionDigits() {
        return this.minimumFractionDigits;
    }

    public Currency getCurrency() {
        return this.symbols.getCurrency();
    }

    public void setCurrency(Currency currency) {
        if (currency != this.symbols.getCurrency() || !currency.getSymbol().equals(this.symbols.getCurrencySymbol())) {
            this.symbols.setCurrency(currency);
            this.icuDecimalFormat.setDecimalFormatSymbols(this.symbols.getIcuDecimalFormatSymbols());
            this.icuDecimalFormat.setMinimumFractionDigits(this.minimumFractionDigits);
            this.icuDecimalFormat.setMaximumFractionDigits(this.maximumFractionDigits);
        }
    }

    public RoundingMode getRoundingMode() {
        return this.roundingMode;
    }

    private static int convertRoundingMode(RoundingMode rm) {
        switch (-getjava-math-RoundingModeSwitchesValues()[rm.ordinal()]) {
            case BaseCalendar.SUNDAY /*1*/:
                return 2;
            case BaseCalendar.MONDAY /*2*/:
                return 1;
            case BaseCalendar.TUESDAY /*3*/:
                return 3;
            case currentSerialVersion /*4*/:
                return 5;
            case BaseCalendar.THURSDAY /*5*/:
                return 6;
            case BaseCalendar.JUNE /*6*/:
                return currentSerialVersion;
            case BaseCalendar.SATURDAY /*7*/:
                return 7;
            case BaseCalendar.AUGUST /*8*/:
                return 0;
            default:
                throw new IllegalArgumentException("Invalid rounding mode specified");
        }
    }

    public void setRoundingMode(RoundingMode roundingMode) {
        if (roundingMode == null) {
            throw new NullPointerException();
        }
        this.roundingMode = roundingMode;
        this.icuDecimalFormat.setRoundingMode(convertRoundingMode(roundingMode));
    }

    void adjustForCurrencyDefaultFractionDigits() {
        Currency currency = this.symbols.getCurrency();
        if (currency == null) {
            try {
                currency = Currency.getInstance(this.symbols.getInternationalCurrencySymbol());
            } catch (IllegalArgumentException e) {
            }
        }
        if (currency != null) {
            int digits = currency.getDefaultFractionDigits();
            if (digits != -1) {
                int oldMinDigits = getMinimumFractionDigits();
                if (oldMinDigits == getMaximumFractionDigits()) {
                    setMinimumFractionDigits(digits);
                    setMaximumFractionDigits(digits);
                    return;
                }
                setMinimumFractionDigits(Math.min(digits, oldMinDigits));
                setMaximumFractionDigits(digits);
            }
        }
    }

    private void writeObject(ObjectOutputStream stream) throws IOException, ClassNotFoundException {
        PutField fields = stream.putFields();
        fields.put("positivePrefix", this.icuDecimalFormat.getPositivePrefix());
        fields.put("positiveSuffix", this.icuDecimalFormat.getPositiveSuffix());
        fields.put("negativePrefix", this.icuDecimalFormat.getNegativePrefix());
        fields.put("negativeSuffix", this.icuDecimalFormat.getNegativeSuffix());
        fields.put("posPrefixPattern", (String) null);
        fields.put("posSuffixPattern", (String) null);
        fields.put("negPrefixPattern", (String) null);
        fields.put("negSuffixPattern", (String) null);
        fields.put("multiplier", this.icuDecimalFormat.getMultiplier());
        fields.put("groupingSize", (byte) this.icuDecimalFormat.getGroupingSize());
        fields.put("groupingUsed", this.icuDecimalFormat.isGroupingUsed());
        fields.put("decimalSeparatorAlwaysShown", this.icuDecimalFormat.isDecimalSeparatorAlwaysShown());
        fields.put("parseBigDecimal", this.icuDecimalFormat.isParseBigDecimal());
        fields.put("roundingMode", this.roundingMode);
        fields.put("symbols", this.symbols);
        fields.put("useExponentialNotation", false);
        fields.put("minExponentDigits", (byte) 0);
        fields.put("maximumIntegerDigits", this.icuDecimalFormat.getMaximumIntegerDigits());
        fields.put("minimumIntegerDigits", this.icuDecimalFormat.getMinimumIntegerDigits());
        fields.put("maximumFractionDigits", this.icuDecimalFormat.getMaximumFractionDigits());
        fields.put("minimumFractionDigits", this.icuDecimalFormat.getMinimumFractionDigits());
        fields.put("serialVersionOnStream", (int) currentSerialVersion);
        stream.writeFields();
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        GetField fields = stream.readFields();
        this.symbols = (DecimalFormatSymbols) fields.get("symbols", null);
        init("");
        this.icuDecimalFormat.setPositivePrefix((String) fields.get("positivePrefix", (Object) ""));
        this.icuDecimalFormat.setPositiveSuffix((String) fields.get("positiveSuffix", (Object) ""));
        this.icuDecimalFormat.setNegativePrefix((String) fields.get("negativePrefix", LanguageTag.SEP));
        this.icuDecimalFormat.setNegativeSuffix((String) fields.get("negativeSuffix", (Object) ""));
        this.icuDecimalFormat.setMultiplier(fields.get("multiplier", 1));
        this.icuDecimalFormat.setGroupingSize(fields.get("groupingSize", (byte) 3));
        this.icuDecimalFormat.setGroupingUsed(fields.get("groupingUsed", true));
        this.icuDecimalFormat.setDecimalSeparatorAlwaysShown(fields.get("decimalSeparatorAlwaysShown", false));
        setRoundingMode((RoundingMode) fields.get("roundingMode", RoundingMode.HALF_EVEN));
        int maximumIntegerDigits = fields.get("maximumIntegerDigits", (int) DOUBLE_INTEGER_DIGITS);
        int minimumIntegerDigits = fields.get("minimumIntegerDigits", (int) DOUBLE_INTEGER_DIGITS);
        int maximumFractionDigits = fields.get("maximumFractionDigits", (int) DOUBLE_FRACTION_DIGITS);
        int minimumFractionDigits = fields.get("minimumFractionDigits", (int) DOUBLE_FRACTION_DIGITS);
        this.icuDecimalFormat.setMaximumIntegerDigits(maximumIntegerDigits);
        super.setMaximumIntegerDigits(this.icuDecimalFormat.getMaximumIntegerDigits());
        setMinimumIntegerDigits(minimumIntegerDigits);
        setMinimumFractionDigits(minimumFractionDigits);
        setMaximumFractionDigits(maximumFractionDigits);
        setParseBigDecimal(fields.get("parseBigDecimal", false));
        if (fields.get("serialVersionOnStream", 0) < 3) {
            setMaximumIntegerDigits(super.getMaximumIntegerDigits());
            setMinimumIntegerDigits(super.getMinimumIntegerDigits());
            setMaximumFractionDigits(super.getMaximumFractionDigits());
            setMinimumFractionDigits(super.getMinimumFractionDigits());
        }
    }
}
