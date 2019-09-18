package java.text;

import android.icu.text.DecimalFormat_ICU58_Android;
import android.icu.text.NumberFormat;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.AttributedCharacterIterator;
import java.text.Format;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import libcore.icu.LocaleData;
import sun.util.locale.LanguageTag;

public class DecimalFormat extends NumberFormat {
    static final int DOUBLE_FRACTION_DIGITS = 340;
    static final int DOUBLE_INTEGER_DIGITS = 309;
    static final int MAXIMUM_FRACTION_DIGITS = Integer.MAX_VALUE;
    static final int MAXIMUM_INTEGER_DIGITS = Integer.MAX_VALUE;
    static final int currentSerialVersion = 4;
    private static final ObjectStreamField[] serialPersistentFields = {new ObjectStreamField("positivePrefix", String.class), new ObjectStreamField("positiveSuffix", String.class), new ObjectStreamField("negativePrefix", String.class), new ObjectStreamField("negativeSuffix", String.class), new ObjectStreamField("posPrefixPattern", String.class), new ObjectStreamField("posSuffixPattern", String.class), new ObjectStreamField("negPrefixPattern", String.class), new ObjectStreamField("negSuffixPattern", String.class), new ObjectStreamField("multiplier", Integer.TYPE), new ObjectStreamField("groupingSize", Byte.TYPE), new ObjectStreamField("groupingUsed", Boolean.TYPE), new ObjectStreamField("decimalSeparatorAlwaysShown", Boolean.TYPE), new ObjectStreamField("parseBigDecimal", Boolean.TYPE), new ObjectStreamField("roundingMode", RoundingMode.class), new ObjectStreamField("symbols", DecimalFormatSymbols.class), new ObjectStreamField("useExponentialNotation", Boolean.TYPE), new ObjectStreamField("minExponentDigits", Byte.TYPE), new ObjectStreamField("maximumIntegerDigits", Integer.TYPE), new ObjectStreamField("minimumIntegerDigits", Integer.TYPE), new ObjectStreamField("maximumFractionDigits", Integer.TYPE), new ObjectStreamField("minimumFractionDigits", Integer.TYPE), new ObjectStreamField("serialVersionOnStream", Integer.TYPE)};
    static final long serialVersionUID = 864413376551465018L;
    private transient DecimalFormat_ICU58_Android icuDecimalFormat;
    private int maximumFractionDigits;
    private int maximumIntegerDigits;
    private int minimumFractionDigits;
    private int minimumIntegerDigits;
    private RoundingMode roundingMode;
    private DecimalFormatSymbols symbols;

    /* renamed from: java.text.DecimalFormat$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$java$math$RoundingMode = new int[RoundingMode.values().length];

        static {
            try {
                $SwitchMap$java$math$RoundingMode[RoundingMode.UP.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$java$math$RoundingMode[RoundingMode.DOWN.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$java$math$RoundingMode[RoundingMode.CEILING.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$java$math$RoundingMode[RoundingMode.FLOOR.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$java$math$RoundingMode[RoundingMode.HALF_UP.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$java$math$RoundingMode[RoundingMode.HALF_DOWN.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$java$math$RoundingMode[RoundingMode.HALF_EVEN.ordinal()] = 7;
            } catch (NoSuchFieldError e7) {
            }
            try {
                $SwitchMap$java$math$RoundingMode[RoundingMode.UNNECESSARY.ordinal()] = 8;
            } catch (NoSuchFieldError e8) {
            }
        }
    }

    public DecimalFormat() {
        this.symbols = null;
        this.roundingMode = RoundingMode.HALF_EVEN;
        Locale def = Locale.getDefault(Locale.Category.FORMAT);
        String pattern = LocaleData.get(def).numberPattern;
        this.symbols = DecimalFormatSymbols.getInstance(def);
        initPattern(pattern);
    }

    public DecimalFormat(String pattern) {
        this.symbols = null;
        this.roundingMode = RoundingMode.HALF_EVEN;
        this.symbols = DecimalFormatSymbols.getInstance(Locale.getDefault(Locale.Category.FORMAT));
        initPattern(pattern);
    }

    public DecimalFormat(String pattern, DecimalFormatSymbols symbols2) {
        this.symbols = null;
        this.roundingMode = RoundingMode.HALF_EVEN;
        this.symbols = (DecimalFormatSymbols) symbols2.clone();
        initPattern(pattern);
    }

    private void initPattern(String pattern) {
        this.icuDecimalFormat = new DecimalFormat_ICU58_Android(pattern, this.symbols.getIcuDecimalFormatSymbols());
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

    private static FieldPosition getIcuFieldPosition(FieldPosition fp) {
        NumberFormat.Field attribute;
        Format.Field fieldAttribute = fp.getFieldAttribute();
        if (fieldAttribute == null) {
            return fp;
        }
        if (fieldAttribute == NumberFormat.Field.INTEGER) {
            attribute = NumberFormat.Field.INTEGER;
        } else if (fieldAttribute == NumberFormat.Field.FRACTION) {
            attribute = NumberFormat.Field.FRACTION;
        } else if (fieldAttribute == NumberFormat.Field.DECIMAL_SEPARATOR) {
            attribute = NumberFormat.Field.DECIMAL_SEPARATOR;
        } else if (fieldAttribute == NumberFormat.Field.EXPONENT_SYMBOL) {
            attribute = NumberFormat.Field.EXPONENT_SYMBOL;
        } else if (fieldAttribute == NumberFormat.Field.EXPONENT_SIGN) {
            attribute = NumberFormat.Field.EXPONENT_SIGN;
        } else if (fieldAttribute == NumberFormat.Field.EXPONENT) {
            attribute = NumberFormat.Field.EXPONENT;
        } else if (fieldAttribute == NumberFormat.Field.GROUPING_SEPARATOR) {
            attribute = NumberFormat.Field.GROUPING_SEPARATOR;
        } else if (fieldAttribute == NumberFormat.Field.CURRENCY) {
            attribute = NumberFormat.Field.CURRENCY;
        } else if (fieldAttribute == NumberFormat.Field.PERCENT) {
            attribute = NumberFormat.Field.PERCENT;
        } else if (fieldAttribute == NumberFormat.Field.PERMILLE) {
            attribute = NumberFormat.Field.PERMILLE;
        } else if (fieldAttribute == NumberFormat.Field.SIGN) {
            attribute = NumberFormat.Field.SIGN;
        } else {
            throw new IllegalArgumentException("Unexpected field position attribute type.");
        }
        FieldPosition icuFieldPosition = new FieldPosition((Format.Field) attribute);
        icuFieldPosition.setBeginIndex(fp.getBeginIndex());
        icuFieldPosition.setEndIndex(fp.getEndIndex());
        return icuFieldPosition;
    }

    private static NumberFormat.Field toJavaFieldAttribute(AttributedCharacterIterator.Attribute icuAttribute) {
        String name = icuAttribute.getName();
        if (name.equals(NumberFormat.Field.INTEGER.getName())) {
            return NumberFormat.Field.INTEGER;
        }
        if (name.equals(NumberFormat.Field.CURRENCY.getName())) {
            return NumberFormat.Field.CURRENCY;
        }
        if (name.equals(NumberFormat.Field.DECIMAL_SEPARATOR.getName())) {
            return NumberFormat.Field.DECIMAL_SEPARATOR;
        }
        if (name.equals(NumberFormat.Field.EXPONENT.getName())) {
            return NumberFormat.Field.EXPONENT;
        }
        if (name.equals(NumberFormat.Field.EXPONENT_SIGN.getName())) {
            return NumberFormat.Field.EXPONENT_SIGN;
        }
        if (name.equals(NumberFormat.Field.EXPONENT_SYMBOL.getName())) {
            return NumberFormat.Field.EXPONENT_SYMBOL;
        }
        if (name.equals(NumberFormat.Field.FRACTION.getName())) {
            return NumberFormat.Field.FRACTION;
        }
        if (name.equals(NumberFormat.Field.GROUPING_SEPARATOR.getName())) {
            return NumberFormat.Field.GROUPING_SEPARATOR;
        }
        if (name.equals(NumberFormat.Field.SIGN.getName())) {
            return NumberFormat.Field.SIGN;
        }
        if (name.equals(NumberFormat.Field.PERCENT.getName())) {
            return NumberFormat.Field.PERCENT;
        }
        if (name.equals(NumberFormat.Field.PERMILLE.getName())) {
            return NumberFormat.Field.PERMILLE;
        }
        throw new IllegalArgumentException("Unrecognized attribute: " + name);
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
        if (obj != null) {
            AttributedCharacterIterator original = this.icuDecimalFormat.formatToCharacterIterator(obj);
            StringBuilder textBuilder = new StringBuilder(original.getEndIndex() - original.getBeginIndex());
            for (int i = original.getBeginIndex(); i < original.getEndIndex(); i++) {
                textBuilder.append(original.current());
                original.next();
            }
            AttributedString result = new AttributedString(textBuilder.toString());
            for (int i2 = original.getBeginIndex(); i2 < original.getEndIndex(); i2++) {
                original.setIndex(i2);
                for (AttributedCharacterIterator.Attribute attribute : original.getAttributes().keySet()) {
                    int start = original.getRunStart();
                    int end = original.getRunLimit();
                    NumberFormat.Field javaAttr = toJavaFieldAttribute(attribute);
                    result.addAttribute(javaAttr, javaAttr, start, end);
                }
            }
            return result.getIterator();
        }
        throw new NullPointerException("object == null");
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
        if (!isParseIntegerOnly() || !number.equals(new Double(-0.0d))) {
            return number;
        }
        return 0L;
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

    public void setGroupingUsed(boolean newValue) {
        this.icuDecimalFormat.setGroupingUsed(newValue);
    }

    public boolean isGroupingUsed() {
        return this.icuDecimalFormat.isGroupingUsed();
    }

    public int getGroupingSize() {
        return this.icuDecimalFormat.getGroupingSize();
    }

    public void setGroupingSize(int newValue) {
        this.icuDecimalFormat.setGroupingSize(newValue);
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

    public boolean isParseIntegerOnly() {
        return this.icuDecimalFormat.isParseIntegerOnly();
    }

    public void setParseIntegerOnly(boolean value) {
        super.setParseIntegerOnly(value);
        this.icuDecimalFormat.setParseIntegerOnly(value);
    }

    public Object clone() {
        try {
            DecimalFormat other = (DecimalFormat) super.clone();
            other.icuDecimalFormat = (DecimalFormat_ICU58_Android) this.icuDecimalFormat.clone();
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
        if (this.icuDecimalFormat.equals(other.icuDecimalFormat) && compareIcuRoundingIncrement(other.icuDecimalFormat)) {
            z = true;
        }
        return z;
    }

    private boolean compareIcuRoundingIncrement(DecimalFormat_ICU58_Android other) {
        BigDecimal increment = this.icuDecimalFormat.getRoundingIncrement();
        boolean z = false;
        if (increment != null) {
            if (other.getRoundingIncrement() != null && increment.equals(other.getRoundingIncrement())) {
                z = true;
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

    public void setMaximumIntegerDigits(int newValue) {
        this.maximumIntegerDigits = Math.min(Math.max(0, newValue), (int) Integer.MAX_VALUE);
        int i = this.maximumIntegerDigits;
        int i2 = DOUBLE_INTEGER_DIGITS;
        super.setMaximumIntegerDigits(i > DOUBLE_INTEGER_DIGITS ? DOUBLE_INTEGER_DIGITS : this.maximumIntegerDigits);
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
        this.minimumIntegerDigits = Math.min(Math.max(0, newValue), (int) Integer.MAX_VALUE);
        int i = this.minimumIntegerDigits;
        int i2 = DOUBLE_INTEGER_DIGITS;
        super.setMinimumIntegerDigits(i > DOUBLE_INTEGER_DIGITS ? DOUBLE_INTEGER_DIGITS : this.minimumIntegerDigits);
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
        this.maximumFractionDigits = Math.min(Math.max(0, newValue), (int) Integer.MAX_VALUE);
        int i = this.maximumFractionDigits;
        int i2 = DOUBLE_FRACTION_DIGITS;
        super.setMaximumFractionDigits(i > DOUBLE_FRACTION_DIGITS ? DOUBLE_FRACTION_DIGITS : this.maximumFractionDigits);
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
        this.minimumFractionDigits = Math.min(Math.max(0, newValue), (int) Integer.MAX_VALUE);
        int i = this.minimumFractionDigits;
        int i2 = DOUBLE_FRACTION_DIGITS;
        super.setMinimumFractionDigits(i > DOUBLE_FRACTION_DIGITS ? DOUBLE_FRACTION_DIGITS : this.minimumFractionDigits);
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
        switch (AnonymousClass1.$SwitchMap$java$math$RoundingMode[rm.ordinal()]) {
            case 1:
                return 0;
            case 2:
                return 1;
            case 3:
                return 2;
            case 4:
                return 3;
            case 5:
                return 4;
            case 6:
                return 5;
            case 7:
                return 6;
            case 8:
                return 7;
            default:
                throw new IllegalArgumentException("Invalid rounding mode specified");
        }
    }

    public void setRoundingMode(RoundingMode roundingMode2) {
        if (roundingMode2 != null) {
            this.roundingMode = roundingMode2;
            this.icuDecimalFormat.setRoundingMode(convertRoundingMode(roundingMode2));
            return;
        }
        throw new NullPointerException();
    }

    /* access modifiers changed from: package-private */
    public void adjustForCurrencyDefaultFractionDigits() {
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
        ObjectOutputStream.PutField fields = stream.putFields();
        fields.put("positivePrefix", (Object) this.icuDecimalFormat.getPositivePrefix());
        fields.put("positiveSuffix", (Object) this.icuDecimalFormat.getPositiveSuffix());
        fields.put("negativePrefix", (Object) this.icuDecimalFormat.getNegativePrefix());
        fields.put("negativeSuffix", (Object) this.icuDecimalFormat.getNegativeSuffix());
        String str = null;
        fields.put("posPrefixPattern", (Object) str);
        fields.put("posSuffixPattern", (Object) str);
        fields.put("negPrefixPattern", (Object) str);
        fields.put("negSuffixPattern", (Object) str);
        fields.put("multiplier", this.icuDecimalFormat.getMultiplier());
        fields.put("groupingSize", (byte) this.icuDecimalFormat.getGroupingSize());
        fields.put("groupingUsed", this.icuDecimalFormat.isGroupingUsed());
        fields.put("decimalSeparatorAlwaysShown", this.icuDecimalFormat.isDecimalSeparatorAlwaysShown());
        fields.put("parseBigDecimal", this.icuDecimalFormat.isParseBigDecimal());
        fields.put("roundingMode", (Object) this.roundingMode);
        fields.put("symbols", (Object) this.symbols);
        fields.put("useExponentialNotation", false);
        fields.put("minExponentDigits", (byte) 0);
        fields.put("maximumIntegerDigits", this.icuDecimalFormat.getMaximumIntegerDigits());
        fields.put("minimumIntegerDigits", this.icuDecimalFormat.getMinimumIntegerDigits());
        fields.put("maximumFractionDigits", this.icuDecimalFormat.getMaximumFractionDigits());
        fields.put("minimumFractionDigits", this.icuDecimalFormat.getMinimumFractionDigits());
        fields.put("serialVersionOnStream", 4);
        stream.writeFields();
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        ObjectInputStream.GetField fields = stream.readFields();
        this.symbols = (DecimalFormatSymbols) fields.get("symbols", (Object) null);
        initPattern("#");
        String positivePrefix = (String) fields.get("positivePrefix", (Object) "");
        if (!Objects.equals(positivePrefix, this.icuDecimalFormat.getPositivePrefix())) {
            this.icuDecimalFormat.setPositivePrefix(positivePrefix);
        }
        String positiveSuffix = (String) fields.get("positiveSuffix", (Object) "");
        if (!Objects.equals(positiveSuffix, this.icuDecimalFormat.getPositiveSuffix())) {
            this.icuDecimalFormat.setPositiveSuffix(positiveSuffix);
        }
        String negativePrefix = (String) fields.get("negativePrefix", (Object) LanguageTag.SEP);
        if (!Objects.equals(negativePrefix, this.icuDecimalFormat.getNegativePrefix())) {
            this.icuDecimalFormat.setNegativePrefix(negativePrefix);
        }
        String negativeSuffix = (String) fields.get("negativeSuffix", (Object) "");
        if (!Objects.equals(negativeSuffix, this.icuDecimalFormat.getNegativeSuffix())) {
            this.icuDecimalFormat.setNegativeSuffix(negativeSuffix);
        }
        int multiplier = fields.get("multiplier", 1);
        if (multiplier != this.icuDecimalFormat.getMultiplier()) {
            this.icuDecimalFormat.setMultiplier(multiplier);
        }
        boolean groupingUsed = fields.get("groupingUsed", true);
        if (groupingUsed != this.icuDecimalFormat.isGroupingUsed()) {
            this.icuDecimalFormat.setGroupingUsed(groupingUsed);
        }
        int groupingSize = fields.get("groupingSize", (byte) 3);
        if (groupingSize != this.icuDecimalFormat.getGroupingSize()) {
            this.icuDecimalFormat.setGroupingSize(groupingSize);
        }
        boolean decimalSeparatorAlwaysShown = fields.get("decimalSeparatorAlwaysShown", false);
        if (decimalSeparatorAlwaysShown != this.icuDecimalFormat.isDecimalSeparatorAlwaysShown()) {
            this.icuDecimalFormat.setDecimalSeparatorAlwaysShown(decimalSeparatorAlwaysShown);
        }
        RoundingMode roundingMode2 = (RoundingMode) fields.get("roundingMode", (Object) RoundingMode.HALF_EVEN);
        if (convertRoundingMode(roundingMode2) != this.icuDecimalFormat.getRoundingMode()) {
            setRoundingMode(roundingMode2);
        }
        int maximumIntegerDigits2 = fields.get("maximumIntegerDigits", (int) DOUBLE_INTEGER_DIGITS);
        if (maximumIntegerDigits2 != this.icuDecimalFormat.getMaximumIntegerDigits()) {
            this.icuDecimalFormat.setMaximumIntegerDigits(maximumIntegerDigits2);
        }
        int minimumIntegerDigits2 = fields.get("minimumIntegerDigits", (int) DOUBLE_INTEGER_DIGITS);
        if (minimumIntegerDigits2 != this.icuDecimalFormat.getMinimumIntegerDigits()) {
            this.icuDecimalFormat.setMinimumIntegerDigits(minimumIntegerDigits2);
        }
        int maximumFractionDigits2 = fields.get("maximumFractionDigits", (int) DOUBLE_FRACTION_DIGITS);
        if (maximumFractionDigits2 != this.icuDecimalFormat.getMaximumFractionDigits()) {
            this.icuDecimalFormat.setMaximumFractionDigits(maximumFractionDigits2);
        }
        int minimumFractionDigits2 = fields.get("minimumFractionDigits", (int) DOUBLE_FRACTION_DIGITS);
        if (minimumFractionDigits2 != this.icuDecimalFormat.getMinimumFractionDigits()) {
            this.icuDecimalFormat.setMinimumFractionDigits(minimumFractionDigits2);
        }
        String str = positivePrefix;
        boolean parseBigDecimal = fields.get("parseBigDecimal", true);
        if (parseBigDecimal != this.icuDecimalFormat.isParseBigDecimal()) {
            this.icuDecimalFormat.setParseBigDecimal(parseBigDecimal);
        }
        updateFieldsFromIcu();
        boolean z = parseBigDecimal;
        if (fields.get("serialVersionOnStream", 0) < 3) {
            setMaximumIntegerDigits(super.getMaximumIntegerDigits());
            setMinimumIntegerDigits(super.getMinimumIntegerDigits());
            setMaximumFractionDigits(super.getMaximumFractionDigits());
            setMinimumFractionDigits(super.getMinimumFractionDigits());
        }
    }
}
