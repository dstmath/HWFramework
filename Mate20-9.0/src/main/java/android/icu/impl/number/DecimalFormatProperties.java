package android.icu.impl.number;

import android.icu.impl.number.Padder;
import android.icu.impl.number.Parse;
import android.icu.text.CompactDecimalFormat;
import android.icu.text.CurrencyPluralInfo;
import android.icu.text.PluralRules;
import android.icu.util.Currency;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Map;

public class DecimalFormatProperties implements Cloneable, Serializable {
    private static final DecimalFormatProperties DEFAULT = new DecimalFormatProperties();
    private static final long serialVersionUID = 4095518955889349243L;
    private transient Map<String, Map<String, String>> compactCustomData;
    private transient CompactDecimalFormat.CompactStyle compactStyle;
    private transient Currency currency;
    private transient CurrencyPluralInfo currencyPluralInfo;
    private transient Currency.CurrencyUsage currencyUsage;
    private transient boolean decimalPatternMatchRequired;
    private transient boolean decimalSeparatorAlwaysShown;
    private transient boolean exponentSignAlwaysShown;
    private transient int formatWidth;
    private transient int groupingSize;
    private transient int magnitudeMultiplier;
    private transient MathContext mathContext;
    private transient int maximumFractionDigits;
    private transient int maximumIntegerDigits;
    private transient int maximumSignificantDigits;
    private transient int minimumExponentDigits;
    private transient int minimumFractionDigits;
    private transient int minimumGroupingDigits;
    private transient int minimumIntegerDigits;
    private transient int minimumSignificantDigits;
    private transient BigDecimal multiplier;
    private transient String negativePrefix;
    private transient String negativePrefixPattern;
    private transient String negativeSuffix;
    private transient String negativeSuffixPattern;
    private transient Padder.PadPosition padPosition;
    private transient String padString;
    private transient boolean parseCaseSensitive;
    private transient Parse.GroupingMode parseGroupingMode;
    private transient boolean parseIntegerOnly;
    private transient Parse.ParseMode parseMode;
    private transient boolean parseNoExponent;
    private transient boolean parseToBigDecimal;
    private transient PluralRules pluralRules;
    private transient String positivePrefix;
    private transient String positivePrefixPattern;
    private transient String positiveSuffix;
    private transient String positiveSuffixPattern;
    private transient BigDecimal roundingIncrement;
    private transient RoundingMode roundingMode;
    private transient int secondaryGroupingSize;
    private transient boolean signAlwaysShown;

    public DecimalFormatProperties() {
        clear();
    }

    private DecimalFormatProperties _clear() {
        this.compactCustomData = null;
        this.compactStyle = null;
        this.currency = null;
        this.currencyPluralInfo = null;
        this.currencyUsage = null;
        this.decimalPatternMatchRequired = false;
        this.decimalSeparatorAlwaysShown = false;
        this.exponentSignAlwaysShown = false;
        this.formatWidth = -1;
        this.groupingSize = -1;
        this.magnitudeMultiplier = 0;
        this.mathContext = null;
        this.maximumFractionDigits = -1;
        this.maximumIntegerDigits = -1;
        this.maximumSignificantDigits = -1;
        this.minimumExponentDigits = -1;
        this.minimumFractionDigits = -1;
        this.minimumGroupingDigits = -1;
        this.minimumIntegerDigits = -1;
        this.minimumSignificantDigits = -1;
        this.multiplier = null;
        this.negativePrefix = null;
        this.negativePrefixPattern = null;
        this.negativeSuffix = null;
        this.negativeSuffixPattern = null;
        this.padPosition = null;
        this.padString = null;
        this.parseCaseSensitive = false;
        this.parseGroupingMode = null;
        this.parseIntegerOnly = false;
        this.parseMode = null;
        this.parseNoExponent = false;
        this.parseToBigDecimal = false;
        this.pluralRules = null;
        this.positivePrefix = null;
        this.positivePrefixPattern = null;
        this.positiveSuffix = null;
        this.positiveSuffixPattern = null;
        this.roundingIncrement = null;
        this.roundingMode = null;
        this.secondaryGroupingSize = -1;
        this.signAlwaysShown = false;
        return this;
    }

    private DecimalFormatProperties _copyFrom(DecimalFormatProperties other) {
        this.compactCustomData = other.compactCustomData;
        this.compactStyle = other.compactStyle;
        this.currency = other.currency;
        this.currencyPluralInfo = other.currencyPluralInfo;
        this.currencyUsage = other.currencyUsage;
        this.decimalPatternMatchRequired = other.decimalPatternMatchRequired;
        this.decimalSeparatorAlwaysShown = other.decimalSeparatorAlwaysShown;
        this.exponentSignAlwaysShown = other.exponentSignAlwaysShown;
        this.formatWidth = other.formatWidth;
        this.groupingSize = other.groupingSize;
        this.magnitudeMultiplier = other.magnitudeMultiplier;
        this.mathContext = other.mathContext;
        this.maximumFractionDigits = other.maximumFractionDigits;
        this.maximumIntegerDigits = other.maximumIntegerDigits;
        this.maximumSignificantDigits = other.maximumSignificantDigits;
        this.minimumExponentDigits = other.minimumExponentDigits;
        this.minimumFractionDigits = other.minimumFractionDigits;
        this.minimumGroupingDigits = other.minimumGroupingDigits;
        this.minimumIntegerDigits = other.minimumIntegerDigits;
        this.minimumSignificantDigits = other.minimumSignificantDigits;
        this.multiplier = other.multiplier;
        this.negativePrefix = other.negativePrefix;
        this.negativePrefixPattern = other.negativePrefixPattern;
        this.negativeSuffix = other.negativeSuffix;
        this.negativeSuffixPattern = other.negativeSuffixPattern;
        this.padPosition = other.padPosition;
        this.padString = other.padString;
        this.parseCaseSensitive = other.parseCaseSensitive;
        this.parseGroupingMode = other.parseGroupingMode;
        this.parseIntegerOnly = other.parseIntegerOnly;
        this.parseMode = other.parseMode;
        this.parseNoExponent = other.parseNoExponent;
        this.parseToBigDecimal = other.parseToBigDecimal;
        this.pluralRules = other.pluralRules;
        this.positivePrefix = other.positivePrefix;
        this.positivePrefixPattern = other.positivePrefixPattern;
        this.positiveSuffix = other.positiveSuffix;
        this.positiveSuffixPattern = other.positiveSuffixPattern;
        this.roundingIncrement = other.roundingIncrement;
        this.roundingMode = other.roundingMode;
        this.secondaryGroupingSize = other.secondaryGroupingSize;
        this.signAlwaysShown = other.signAlwaysShown;
        return this;
    }

    private boolean _equals(DecimalFormatProperties other) {
        boolean eq = false;
        if ((((((((((((((((((((((((((((((((((((((((((1 != 0 && _equalsHelper((Object) this.compactCustomData, (Object) other.compactCustomData)) && _equalsHelper((Object) this.compactStyle, (Object) other.compactStyle)) && _equalsHelper((Object) this.currency, (Object) other.currency)) && _equalsHelper((Object) this.currencyPluralInfo, (Object) other.currencyPluralInfo)) && _equalsHelper((Object) this.currencyUsage, (Object) other.currencyUsage)) && _equalsHelper(this.decimalPatternMatchRequired, other.decimalPatternMatchRequired)) && _equalsHelper(this.decimalSeparatorAlwaysShown, other.decimalSeparatorAlwaysShown)) && _equalsHelper(this.exponentSignAlwaysShown, other.exponentSignAlwaysShown)) && _equalsHelper(this.formatWidth, other.formatWidth)) && _equalsHelper(this.groupingSize, other.groupingSize)) && _equalsHelper(this.magnitudeMultiplier, other.magnitudeMultiplier)) && _equalsHelper((Object) this.mathContext, (Object) other.mathContext)) && _equalsHelper(this.maximumFractionDigits, other.maximumFractionDigits)) && _equalsHelper(this.maximumIntegerDigits, other.maximumIntegerDigits)) && _equalsHelper(this.maximumSignificantDigits, other.maximumSignificantDigits)) && _equalsHelper(this.minimumExponentDigits, other.minimumExponentDigits)) && _equalsHelper(this.minimumFractionDigits, other.minimumFractionDigits)) && _equalsHelper(this.minimumGroupingDigits, other.minimumGroupingDigits)) && _equalsHelper(this.minimumIntegerDigits, other.minimumIntegerDigits)) && _equalsHelper(this.minimumSignificantDigits, other.minimumSignificantDigits)) && _equalsHelper((Object) this.multiplier, (Object) other.multiplier)) && _equalsHelper((Object) this.negativePrefix, (Object) other.negativePrefix)) && _equalsHelper((Object) this.negativePrefixPattern, (Object) other.negativePrefixPattern)) && _equalsHelper((Object) this.negativeSuffix, (Object) other.negativeSuffix)) && _equalsHelper((Object) this.negativeSuffixPattern, (Object) other.negativeSuffixPattern)) && _equalsHelper((Object) this.padPosition, (Object) other.padPosition)) && _equalsHelper((Object) this.padString, (Object) other.padString)) && _equalsHelper(this.parseCaseSensitive, other.parseCaseSensitive)) && _equalsHelper((Object) this.parseGroupingMode, (Object) other.parseGroupingMode)) && _equalsHelper(this.parseIntegerOnly, other.parseIntegerOnly)) && _equalsHelper((Object) this.parseMode, (Object) other.parseMode)) && _equalsHelper(this.parseNoExponent, other.parseNoExponent)) && _equalsHelper(this.parseToBigDecimal, other.parseToBigDecimal)) && _equalsHelper((Object) this.pluralRules, (Object) other.pluralRules)) && _equalsHelper((Object) this.positivePrefix, (Object) other.positivePrefix)) && _equalsHelper((Object) this.positivePrefixPattern, (Object) other.positivePrefixPattern)) && _equalsHelper((Object) this.positiveSuffix, (Object) other.positiveSuffix)) && _equalsHelper((Object) this.positiveSuffixPattern, (Object) other.positiveSuffixPattern)) && _equalsHelper((Object) this.roundingIncrement, (Object) other.roundingIncrement)) && _equalsHelper((Object) this.roundingMode, (Object) other.roundingMode)) && _equalsHelper(this.secondaryGroupingSize, other.secondaryGroupingSize)) && _equalsHelper(this.signAlwaysShown, other.signAlwaysShown)) {
            eq = true;
        }
        return eq;
    }

    private boolean _equalsHelper(boolean mine, boolean theirs) {
        return mine == theirs;
    }

    private boolean _equalsHelper(int mine, int theirs) {
        return mine == theirs;
    }

    private boolean _equalsHelper(Object mine, Object theirs) {
        if (mine == theirs) {
            return true;
        }
        if (mine == null) {
            return false;
        }
        return mine.equals(theirs);
    }

    private int _hashCode() {
        return (((((((((((((((((((((((((((((((((((((((((0 ^ _hashCodeHelper((Object) this.compactCustomData)) ^ _hashCodeHelper((Object) this.compactStyle)) ^ _hashCodeHelper((Object) this.currency)) ^ _hashCodeHelper((Object) this.currencyPluralInfo)) ^ _hashCodeHelper((Object) this.currencyUsage)) ^ _hashCodeHelper(this.decimalPatternMatchRequired)) ^ _hashCodeHelper(this.decimalSeparatorAlwaysShown)) ^ _hashCodeHelper(this.exponentSignAlwaysShown)) ^ _hashCodeHelper(this.formatWidth)) ^ _hashCodeHelper(this.groupingSize)) ^ _hashCodeHelper(this.magnitudeMultiplier)) ^ _hashCodeHelper((Object) this.mathContext)) ^ _hashCodeHelper(this.maximumFractionDigits)) ^ _hashCodeHelper(this.maximumIntegerDigits)) ^ _hashCodeHelper(this.maximumSignificantDigits)) ^ _hashCodeHelper(this.minimumExponentDigits)) ^ _hashCodeHelper(this.minimumFractionDigits)) ^ _hashCodeHelper(this.minimumGroupingDigits)) ^ _hashCodeHelper(this.minimumIntegerDigits)) ^ _hashCodeHelper(this.minimumSignificantDigits)) ^ _hashCodeHelper((Object) this.multiplier)) ^ _hashCodeHelper((Object) this.negativePrefix)) ^ _hashCodeHelper((Object) this.negativePrefixPattern)) ^ _hashCodeHelper((Object) this.negativeSuffix)) ^ _hashCodeHelper((Object) this.negativeSuffixPattern)) ^ _hashCodeHelper((Object) this.padPosition)) ^ _hashCodeHelper((Object) this.padString)) ^ _hashCodeHelper(this.parseCaseSensitive)) ^ _hashCodeHelper((Object) this.parseGroupingMode)) ^ _hashCodeHelper(this.parseIntegerOnly)) ^ _hashCodeHelper((Object) this.parseMode)) ^ _hashCodeHelper(this.parseNoExponent)) ^ _hashCodeHelper(this.parseToBigDecimal)) ^ _hashCodeHelper((Object) this.pluralRules)) ^ _hashCodeHelper((Object) this.positivePrefix)) ^ _hashCodeHelper((Object) this.positivePrefixPattern)) ^ _hashCodeHelper((Object) this.positiveSuffix)) ^ _hashCodeHelper((Object) this.positiveSuffixPattern)) ^ _hashCodeHelper((Object) this.roundingIncrement)) ^ _hashCodeHelper((Object) this.roundingMode)) ^ _hashCodeHelper(this.secondaryGroupingSize)) ^ _hashCodeHelper(this.signAlwaysShown);
    }

    private int _hashCodeHelper(boolean value) {
        return value;
    }

    private int _hashCodeHelper(int value) {
        return value * 13;
    }

    private int _hashCodeHelper(Object value) {
        if (value == null) {
            return 0;
        }
        return value.hashCode();
    }

    public DecimalFormatProperties clear() {
        return _clear();
    }

    public DecimalFormatProperties clone() {
        try {
            return (DecimalFormatProperties) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new UnsupportedOperationException(e);
        }
    }

    public DecimalFormatProperties copyFrom(DecimalFormatProperties other) {
        return _copyFrom(other);
    }

    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (this == other) {
            return true;
        }
        if (!(other instanceof DecimalFormatProperties)) {
            return false;
        }
        return _equals((DecimalFormatProperties) other);
    }

    public Map<String, Map<String, String>> getCompactCustomData() {
        return this.compactCustomData;
    }

    public CompactDecimalFormat.CompactStyle getCompactStyle() {
        return this.compactStyle;
    }

    public Currency getCurrency() {
        return this.currency;
    }

    public CurrencyPluralInfo getCurrencyPluralInfo() {
        return this.currencyPluralInfo;
    }

    public Currency.CurrencyUsage getCurrencyUsage() {
        return this.currencyUsage;
    }

    public boolean getDecimalPatternMatchRequired() {
        return this.decimalPatternMatchRequired;
    }

    public boolean getDecimalSeparatorAlwaysShown() {
        return this.decimalSeparatorAlwaysShown;
    }

    public boolean getExponentSignAlwaysShown() {
        return this.exponentSignAlwaysShown;
    }

    public int getFormatWidth() {
        return this.formatWidth;
    }

    public int getGroupingSize() {
        return this.groupingSize;
    }

    public int getMagnitudeMultiplier() {
        return this.magnitudeMultiplier;
    }

    public MathContext getMathContext() {
        return this.mathContext;
    }

    public int getMaximumFractionDigits() {
        return this.maximumFractionDigits;
    }

    public int getMaximumIntegerDigits() {
        return this.maximumIntegerDigits;
    }

    public int getMaximumSignificantDigits() {
        return this.maximumSignificantDigits;
    }

    public int getMinimumExponentDigits() {
        return this.minimumExponentDigits;
    }

    public int getMinimumFractionDigits() {
        return this.minimumFractionDigits;
    }

    public int getMinimumGroupingDigits() {
        return this.minimumGroupingDigits;
    }

    public int getMinimumIntegerDigits() {
        return this.minimumIntegerDigits;
    }

    public int getMinimumSignificantDigits() {
        return this.minimumSignificantDigits;
    }

    public BigDecimal getMultiplier() {
        return this.multiplier;
    }

    public String getNegativePrefix() {
        return this.negativePrefix;
    }

    public String getNegativePrefixPattern() {
        return this.negativePrefixPattern;
    }

    public String getNegativeSuffix() {
        return this.negativeSuffix;
    }

    public String getNegativeSuffixPattern() {
        return this.negativeSuffixPattern;
    }

    public Padder.PadPosition getPadPosition() {
        return this.padPosition;
    }

    public String getPadString() {
        return this.padString;
    }

    public boolean getParseCaseSensitive() {
        return this.parseCaseSensitive;
    }

    public Parse.GroupingMode getParseGroupingMode() {
        return this.parseGroupingMode;
    }

    public boolean getParseIntegerOnly() {
        return this.parseIntegerOnly;
    }

    public Parse.ParseMode getParseMode() {
        return this.parseMode;
    }

    public boolean getParseNoExponent() {
        return this.parseNoExponent;
    }

    public boolean getParseToBigDecimal() {
        return this.parseToBigDecimal;
    }

    public PluralRules getPluralRules() {
        return this.pluralRules;
    }

    public String getPositivePrefix() {
        return this.positivePrefix;
    }

    public String getPositivePrefixPattern() {
        return this.positivePrefixPattern;
    }

    public String getPositiveSuffix() {
        return this.positiveSuffix;
    }

    public String getPositiveSuffixPattern() {
        return this.positiveSuffixPattern;
    }

    public BigDecimal getRoundingIncrement() {
        return this.roundingIncrement;
    }

    public RoundingMode getRoundingMode() {
        return this.roundingMode;
    }

    public int getSecondaryGroupingSize() {
        return this.secondaryGroupingSize;
    }

    public boolean getSignAlwaysShown() {
        return this.signAlwaysShown;
    }

    public int hashCode() {
        return _hashCode();
    }

    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        readObjectImpl(ois);
    }

    /* access modifiers changed from: package-private */
    public void readObjectImpl(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        clear();
        ois.readInt();
        int count = ois.readInt();
        for (int i = 0; i < count; i++) {
            String name = (String) ois.readObject();
            try {
                try {
                    DecimalFormatProperties.class.getDeclaredField(name).set(this, ois.readObject());
                } catch (IllegalArgumentException e) {
                    throw new AssertionError(e);
                } catch (IllegalAccessException e2) {
                    throw new AssertionError(e2);
                }
            } catch (NoSuchFieldException e3) {
            } catch (SecurityException e4) {
                throw new AssertionError(e4);
            }
        }
    }

    public DecimalFormatProperties setCompactCustomData(Map<String, Map<String, String>> compactCustomData2) {
        this.compactCustomData = compactCustomData2;
        return this;
    }

    public DecimalFormatProperties setCompactStyle(CompactDecimalFormat.CompactStyle compactStyle2) {
        this.compactStyle = compactStyle2;
        return this;
    }

    public DecimalFormatProperties setCurrency(Currency currency2) {
        this.currency = currency2;
        return this;
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v0, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v2, resolved type: android.icu.text.CurrencyPluralInfo} */
    /* JADX WARNING: Multi-variable type inference failed */
    public DecimalFormatProperties setCurrencyPluralInfo(CurrencyPluralInfo currencyPluralInfo2) {
        if (currencyPluralInfo2 != null) {
            currencyPluralInfo2 = currencyPluralInfo2.clone();
        }
        this.currencyPluralInfo = currencyPluralInfo2;
        return this;
    }

    public DecimalFormatProperties setCurrencyUsage(Currency.CurrencyUsage currencyUsage2) {
        this.currencyUsage = currencyUsage2;
        return this;
    }

    public DecimalFormatProperties setDecimalPatternMatchRequired(boolean decimalPatternMatchRequired2) {
        this.decimalPatternMatchRequired = decimalPatternMatchRequired2;
        return this;
    }

    public DecimalFormatProperties setDecimalSeparatorAlwaysShown(boolean alwaysShowDecimal) {
        this.decimalSeparatorAlwaysShown = alwaysShowDecimal;
        return this;
    }

    public DecimalFormatProperties setExponentSignAlwaysShown(boolean exponentSignAlwaysShown2) {
        this.exponentSignAlwaysShown = exponentSignAlwaysShown2;
        return this;
    }

    public DecimalFormatProperties setFormatWidth(int paddingWidth) {
        this.formatWidth = paddingWidth;
        return this;
    }

    public DecimalFormatProperties setGroupingSize(int groupingSize2) {
        this.groupingSize = groupingSize2;
        return this;
    }

    public DecimalFormatProperties setMagnitudeMultiplier(int magnitudeMultiplier2) {
        this.magnitudeMultiplier = magnitudeMultiplier2;
        return this;
    }

    public DecimalFormatProperties setMathContext(MathContext mathContext2) {
        this.mathContext = mathContext2;
        return this;
    }

    public DecimalFormatProperties setMaximumFractionDigits(int maximumFractionDigits2) {
        this.maximumFractionDigits = maximumFractionDigits2;
        return this;
    }

    public DecimalFormatProperties setMaximumIntegerDigits(int maximumIntegerDigits2) {
        this.maximumIntegerDigits = maximumIntegerDigits2;
        return this;
    }

    public DecimalFormatProperties setMaximumSignificantDigits(int maximumSignificantDigits2) {
        this.maximumSignificantDigits = maximumSignificantDigits2;
        return this;
    }

    public DecimalFormatProperties setMinimumExponentDigits(int minimumExponentDigits2) {
        this.minimumExponentDigits = minimumExponentDigits2;
        return this;
    }

    public DecimalFormatProperties setMinimumFractionDigits(int minimumFractionDigits2) {
        this.minimumFractionDigits = minimumFractionDigits2;
        return this;
    }

    public DecimalFormatProperties setMinimumGroupingDigits(int minimumGroupingDigits2) {
        this.minimumGroupingDigits = minimumGroupingDigits2;
        return this;
    }

    public DecimalFormatProperties setMinimumIntegerDigits(int minimumIntegerDigits2) {
        this.minimumIntegerDigits = minimumIntegerDigits2;
        return this;
    }

    public DecimalFormatProperties setMinimumSignificantDigits(int minimumSignificantDigits2) {
        this.minimumSignificantDigits = minimumSignificantDigits2;
        return this;
    }

    public DecimalFormatProperties setMultiplier(BigDecimal multiplier2) {
        this.multiplier = multiplier2;
        return this;
    }

    public DecimalFormatProperties setNegativePrefix(String negativePrefix2) {
        this.negativePrefix = negativePrefix2;
        return this;
    }

    public DecimalFormatProperties setNegativePrefixPattern(String negativePrefixPattern2) {
        this.negativePrefixPattern = negativePrefixPattern2;
        return this;
    }

    public DecimalFormatProperties setNegativeSuffix(String negativeSuffix2) {
        this.negativeSuffix = negativeSuffix2;
        return this;
    }

    public DecimalFormatProperties setNegativeSuffixPattern(String negativeSuffixPattern2) {
        this.negativeSuffixPattern = negativeSuffixPattern2;
        return this;
    }

    public DecimalFormatProperties setPadPosition(Padder.PadPosition paddingLocation) {
        this.padPosition = paddingLocation;
        return this;
    }

    public DecimalFormatProperties setPadString(String paddingString) {
        this.padString = paddingString;
        return this;
    }

    public DecimalFormatProperties setParseCaseSensitive(boolean parseCaseSensitive2) {
        this.parseCaseSensitive = parseCaseSensitive2;
        return this;
    }

    public DecimalFormatProperties setParseGroupingMode(Parse.GroupingMode parseGroupingMode2) {
        this.parseGroupingMode = parseGroupingMode2;
        return this;
    }

    public DecimalFormatProperties setParseIntegerOnly(boolean parseIntegerOnly2) {
        this.parseIntegerOnly = parseIntegerOnly2;
        return this;
    }

    public DecimalFormatProperties setParseMode(Parse.ParseMode parseMode2) {
        this.parseMode = parseMode2;
        return this;
    }

    public DecimalFormatProperties setParseNoExponent(boolean parseNoExponent2) {
        this.parseNoExponent = parseNoExponent2;
        return this;
    }

    public DecimalFormatProperties setParseToBigDecimal(boolean parseToBigDecimal2) {
        this.parseToBigDecimal = parseToBigDecimal2;
        return this;
    }

    public DecimalFormatProperties setPluralRules(PluralRules pluralRules2) {
        this.pluralRules = pluralRules2;
        return this;
    }

    public DecimalFormatProperties setPositivePrefix(String positivePrefix2) {
        this.positivePrefix = positivePrefix2;
        return this;
    }

    public DecimalFormatProperties setPositivePrefixPattern(String positivePrefixPattern2) {
        this.positivePrefixPattern = positivePrefixPattern2;
        return this;
    }

    public DecimalFormatProperties setPositiveSuffix(String positiveSuffix2) {
        this.positiveSuffix = positiveSuffix2;
        return this;
    }

    public DecimalFormatProperties setPositiveSuffixPattern(String positiveSuffixPattern2) {
        this.positiveSuffixPattern = positiveSuffixPattern2;
        return this;
    }

    public DecimalFormatProperties setRoundingIncrement(BigDecimal roundingIncrement2) {
        this.roundingIncrement = roundingIncrement2;
        return this;
    }

    public DecimalFormatProperties setRoundingMode(RoundingMode roundingMode2) {
        this.roundingMode = roundingMode2;
        return this;
    }

    public DecimalFormatProperties setSecondaryGroupingSize(int secondaryGroupingSize2) {
        this.secondaryGroupingSize = secondaryGroupingSize2;
        return this;
    }

    public DecimalFormatProperties setSignAlwaysShown(boolean signAlwaysShown2) {
        this.signAlwaysShown = signAlwaysShown2;
        return this;
    }

    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("<Properties");
        toStringBare(result);
        result.append(">");
        return result.toString();
    }

    public void toStringBare(StringBuilder result) {
        Field[] fields = DecimalFormatProperties.class.getDeclaredFields();
        int length = fields.length;
        int i = 0;
        while (i < length) {
            Field field = fields[i];
            try {
                Object myValue = field.get(this);
                Object defaultValue = field.get(DEFAULT);
                if (myValue == null && defaultValue == null) {
                    i++;
                } else if (myValue == null || defaultValue == null) {
                    result.append(Padder.FALLBACK_PADDING_STRING + field.getName() + ":" + myValue);
                    i++;
                } else {
                    if (!myValue.equals(defaultValue)) {
                        result.append(Padder.FALLBACK_PADDING_STRING + field.getName() + ":" + myValue);
                    }
                    i++;
                }
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e2) {
                e2.printStackTrace();
            }
        }
    }

    private void writeObject(ObjectOutputStream oos) throws IOException {
        writeObjectImpl(oos);
    }

    /* access modifiers changed from: package-private */
    public void writeObjectImpl(ObjectOutputStream oos) throws IOException {
        oos.defaultWriteObject();
        oos.writeInt(0);
        ArrayList<Field> fieldsToSerialize = new ArrayList<>();
        ArrayList<Object> valuesToSerialize = new ArrayList<>();
        for (Field field : DecimalFormatProperties.class.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers())) {
                try {
                    Object myValue = field.get(this);
                    if (myValue != null) {
                        if (!myValue.equals(field.get(DEFAULT))) {
                            fieldsToSerialize.add(field);
                            valuesToSerialize.add(myValue);
                        }
                    }
                } catch (IllegalArgumentException e) {
                    throw new AssertionError(e);
                } catch (IllegalAccessException e2) {
                    throw new AssertionError(e2);
                }
            }
        }
        int count = fieldsToSerialize.size();
        oos.writeInt(count);
        for (int i = 0; i < count; i++) {
            Object value = valuesToSerialize.get(i);
            oos.writeObject(fieldsToSerialize.get(i).getName());
            oos.writeObject(value);
        }
    }
}
