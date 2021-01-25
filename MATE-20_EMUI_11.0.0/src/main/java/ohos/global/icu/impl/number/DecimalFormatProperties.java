package ohos.global.icu.impl.number;

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
import ohos.global.icu.impl.number.Padder;
import ohos.global.icu.text.CompactDecimalFormat;
import ohos.global.icu.text.CurrencyPluralInfo;
import ohos.global.icu.text.PluralRules;
import ohos.global.icu.util.Currency;

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
    private transient boolean groupingUsed;
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
    private transient boolean parseIntegerOnly;
    private transient ParseMode parseMode;
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

    public enum ParseMode {
        LENIENT,
        STRICT,
        JAVA_COMPATIBILITY
    }

    private boolean _equalsHelper(int i, int i2) {
        return i == i2;
    }

    private boolean _equalsHelper(boolean z, boolean z2) {
        return z == z2;
    }

    private int _hashCodeHelper(int i) {
        return i * 13;
    }

    private int _hashCodeHelper(boolean z) {
        return z ? 1 : 0;
    }

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
        this.groupingUsed = true;
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

    private DecimalFormatProperties _copyFrom(DecimalFormatProperties decimalFormatProperties) {
        this.compactCustomData = decimalFormatProperties.compactCustomData;
        this.compactStyle = decimalFormatProperties.compactStyle;
        this.currency = decimalFormatProperties.currency;
        this.currencyPluralInfo = decimalFormatProperties.currencyPluralInfo;
        this.currencyUsage = decimalFormatProperties.currencyUsage;
        this.decimalPatternMatchRequired = decimalFormatProperties.decimalPatternMatchRequired;
        this.decimalSeparatorAlwaysShown = decimalFormatProperties.decimalSeparatorAlwaysShown;
        this.exponentSignAlwaysShown = decimalFormatProperties.exponentSignAlwaysShown;
        this.formatWidth = decimalFormatProperties.formatWidth;
        this.groupingSize = decimalFormatProperties.groupingSize;
        this.groupingUsed = decimalFormatProperties.groupingUsed;
        this.magnitudeMultiplier = decimalFormatProperties.magnitudeMultiplier;
        this.mathContext = decimalFormatProperties.mathContext;
        this.maximumFractionDigits = decimalFormatProperties.maximumFractionDigits;
        this.maximumIntegerDigits = decimalFormatProperties.maximumIntegerDigits;
        this.maximumSignificantDigits = decimalFormatProperties.maximumSignificantDigits;
        this.minimumExponentDigits = decimalFormatProperties.minimumExponentDigits;
        this.minimumFractionDigits = decimalFormatProperties.minimumFractionDigits;
        this.minimumGroupingDigits = decimalFormatProperties.minimumGroupingDigits;
        this.minimumIntegerDigits = decimalFormatProperties.minimumIntegerDigits;
        this.minimumSignificantDigits = decimalFormatProperties.minimumSignificantDigits;
        this.multiplier = decimalFormatProperties.multiplier;
        this.negativePrefix = decimalFormatProperties.negativePrefix;
        this.negativePrefixPattern = decimalFormatProperties.negativePrefixPattern;
        this.negativeSuffix = decimalFormatProperties.negativeSuffix;
        this.negativeSuffixPattern = decimalFormatProperties.negativeSuffixPattern;
        this.padPosition = decimalFormatProperties.padPosition;
        this.padString = decimalFormatProperties.padString;
        this.parseCaseSensitive = decimalFormatProperties.parseCaseSensitive;
        this.parseIntegerOnly = decimalFormatProperties.parseIntegerOnly;
        this.parseMode = decimalFormatProperties.parseMode;
        this.parseNoExponent = decimalFormatProperties.parseNoExponent;
        this.parseToBigDecimal = decimalFormatProperties.parseToBigDecimal;
        this.pluralRules = decimalFormatProperties.pluralRules;
        this.positivePrefix = decimalFormatProperties.positivePrefix;
        this.positivePrefixPattern = decimalFormatProperties.positivePrefixPattern;
        this.positiveSuffix = decimalFormatProperties.positiveSuffix;
        this.positiveSuffixPattern = decimalFormatProperties.positiveSuffixPattern;
        this.roundingIncrement = decimalFormatProperties.roundingIncrement;
        this.roundingMode = decimalFormatProperties.roundingMode;
        this.secondaryGroupingSize = decimalFormatProperties.secondaryGroupingSize;
        this.signAlwaysShown = decimalFormatProperties.signAlwaysShown;
        return this;
    }

    private boolean _equals(DecimalFormatProperties decimalFormatProperties) {
        return (((((((((((((((((((((((((((((((((((((((((_equalsHelper(this.compactCustomData, decimalFormatProperties.compactCustomData)) && _equalsHelper(this.compactStyle, decimalFormatProperties.compactStyle)) && _equalsHelper(this.currency, decimalFormatProperties.currency)) && _equalsHelper(this.currencyPluralInfo, decimalFormatProperties.currencyPluralInfo)) && _equalsHelper(this.currencyUsage, decimalFormatProperties.currencyUsage)) && _equalsHelper(this.decimalPatternMatchRequired, decimalFormatProperties.decimalPatternMatchRequired)) && _equalsHelper(this.decimalSeparatorAlwaysShown, decimalFormatProperties.decimalSeparatorAlwaysShown)) && _equalsHelper(this.exponentSignAlwaysShown, decimalFormatProperties.exponentSignAlwaysShown)) && _equalsHelper(this.formatWidth, decimalFormatProperties.formatWidth)) && _equalsHelper(this.groupingSize, decimalFormatProperties.groupingSize)) && _equalsHelper(this.groupingUsed, decimalFormatProperties.groupingUsed)) && _equalsHelper(this.magnitudeMultiplier, decimalFormatProperties.magnitudeMultiplier)) && _equalsHelper(this.mathContext, decimalFormatProperties.mathContext)) && _equalsHelper(this.maximumFractionDigits, decimalFormatProperties.maximumFractionDigits)) && _equalsHelper(this.maximumIntegerDigits, decimalFormatProperties.maximumIntegerDigits)) && _equalsHelper(this.maximumSignificantDigits, decimalFormatProperties.maximumSignificantDigits)) && _equalsHelper(this.minimumExponentDigits, decimalFormatProperties.minimumExponentDigits)) && _equalsHelper(this.minimumFractionDigits, decimalFormatProperties.minimumFractionDigits)) && _equalsHelper(this.minimumGroupingDigits, decimalFormatProperties.minimumGroupingDigits)) && _equalsHelper(this.minimumIntegerDigits, decimalFormatProperties.minimumIntegerDigits)) && _equalsHelper(this.minimumSignificantDigits, decimalFormatProperties.minimumSignificantDigits)) && _equalsHelper(this.multiplier, decimalFormatProperties.multiplier)) && _equalsHelper(this.negativePrefix, decimalFormatProperties.negativePrefix)) && _equalsHelper(this.negativePrefixPattern, decimalFormatProperties.negativePrefixPattern)) && _equalsHelper(this.negativeSuffix, decimalFormatProperties.negativeSuffix)) && _equalsHelper(this.negativeSuffixPattern, decimalFormatProperties.negativeSuffixPattern)) && _equalsHelper(this.padPosition, decimalFormatProperties.padPosition)) && _equalsHelper(this.padString, decimalFormatProperties.padString)) && _equalsHelper(this.parseCaseSensitive, decimalFormatProperties.parseCaseSensitive)) && _equalsHelper(this.parseIntegerOnly, decimalFormatProperties.parseIntegerOnly)) && _equalsHelper(this.parseMode, decimalFormatProperties.parseMode)) && _equalsHelper(this.parseNoExponent, decimalFormatProperties.parseNoExponent)) && _equalsHelper(this.parseToBigDecimal, decimalFormatProperties.parseToBigDecimal)) && _equalsHelper(this.pluralRules, decimalFormatProperties.pluralRules)) && _equalsHelper(this.positivePrefix, decimalFormatProperties.positivePrefix)) && _equalsHelper(this.positivePrefixPattern, decimalFormatProperties.positivePrefixPattern)) && _equalsHelper(this.positiveSuffix, decimalFormatProperties.positiveSuffix)) && _equalsHelper(this.positiveSuffixPattern, decimalFormatProperties.positiveSuffixPattern)) && _equalsHelper(this.roundingIncrement, decimalFormatProperties.roundingIncrement)) && _equalsHelper(this.roundingMode, decimalFormatProperties.roundingMode)) && _equalsHelper(this.secondaryGroupingSize, decimalFormatProperties.secondaryGroupingSize)) && _equalsHelper(this.signAlwaysShown, decimalFormatProperties.signAlwaysShown);
    }

    private boolean _equalsHelper(Object obj, Object obj2) {
        if (obj == obj2) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        return obj.equals(obj2);
    }

    private int _hashCode() {
        return _hashCodeHelper(this.signAlwaysShown) ^ (((((((((((((((((((((((((((((((((((((((((_hashCodeHelper(this.compactCustomData) ^ 0) ^ _hashCodeHelper(this.compactStyle)) ^ _hashCodeHelper(this.currency)) ^ _hashCodeHelper(this.currencyPluralInfo)) ^ _hashCodeHelper(this.currencyUsage)) ^ _hashCodeHelper(this.decimalPatternMatchRequired)) ^ _hashCodeHelper(this.decimalSeparatorAlwaysShown)) ^ _hashCodeHelper(this.exponentSignAlwaysShown)) ^ _hashCodeHelper(this.formatWidth)) ^ _hashCodeHelper(this.groupingSize)) ^ _hashCodeHelper(this.groupingUsed)) ^ _hashCodeHelper(this.magnitudeMultiplier)) ^ _hashCodeHelper(this.mathContext)) ^ _hashCodeHelper(this.maximumFractionDigits)) ^ _hashCodeHelper(this.maximumIntegerDigits)) ^ _hashCodeHelper(this.maximumSignificantDigits)) ^ _hashCodeHelper(this.minimumExponentDigits)) ^ _hashCodeHelper(this.minimumFractionDigits)) ^ _hashCodeHelper(this.minimumGroupingDigits)) ^ _hashCodeHelper(this.minimumIntegerDigits)) ^ _hashCodeHelper(this.minimumSignificantDigits)) ^ _hashCodeHelper(this.multiplier)) ^ _hashCodeHelper(this.negativePrefix)) ^ _hashCodeHelper(this.negativePrefixPattern)) ^ _hashCodeHelper(this.negativeSuffix)) ^ _hashCodeHelper(this.negativeSuffixPattern)) ^ _hashCodeHelper(this.padPosition)) ^ _hashCodeHelper(this.padString)) ^ _hashCodeHelper(this.parseCaseSensitive)) ^ _hashCodeHelper(this.parseIntegerOnly)) ^ _hashCodeHelper(this.parseMode)) ^ _hashCodeHelper(this.parseNoExponent)) ^ _hashCodeHelper(this.parseToBigDecimal)) ^ _hashCodeHelper(this.pluralRules)) ^ _hashCodeHelper(this.positivePrefix)) ^ _hashCodeHelper(this.positivePrefixPattern)) ^ _hashCodeHelper(this.positiveSuffix)) ^ _hashCodeHelper(this.positiveSuffixPattern)) ^ _hashCodeHelper(this.roundingIncrement)) ^ _hashCodeHelper(this.roundingMode)) ^ _hashCodeHelper(this.secondaryGroupingSize));
    }

    private int _hashCodeHelper(Object obj) {
        if (obj == null) {
            return 0;
        }
        return obj.hashCode();
    }

    public DecimalFormatProperties clear() {
        return _clear();
    }

    @Override // java.lang.Object
    public DecimalFormatProperties clone() {
        try {
            return (DecimalFormatProperties) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new UnsupportedOperationException(e);
        }
    }

    public DecimalFormatProperties copyFrom(DecimalFormatProperties decimalFormatProperties) {
        return _copyFrom(decimalFormatProperties);
    }

    @Override // java.lang.Object
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof DecimalFormatProperties)) {
            return false;
        }
        return _equals((DecimalFormatProperties) obj);
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

    public boolean getGroupingUsed() {
        return this.groupingUsed;
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

    public boolean getParseIntegerOnly() {
        return this.parseIntegerOnly;
    }

    public ParseMode getParseMode() {
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

    @Override // java.lang.Object
    public int hashCode() {
        return _hashCode();
    }

    private void readObject(ObjectInputStream objectInputStream) throws IOException, ClassNotFoundException {
        readObjectImpl(objectInputStream);
    }

    /* access modifiers changed from: package-private */
    public void readObjectImpl(ObjectInputStream objectInputStream) throws IOException, ClassNotFoundException {
        objectInputStream.defaultReadObject();
        clear();
        objectInputStream.readInt();
        int readInt = objectInputStream.readInt();
        for (int i = 0; i < readInt; i++) {
            String str = (String) objectInputStream.readObject();
            try {
                try {
                    DecimalFormatProperties.class.getDeclaredField(str).set(this, objectInputStream.readObject());
                } catch (IllegalArgumentException e) {
                    throw new AssertionError(e);
                } catch (IllegalAccessException e2) {
                    throw new AssertionError(e2);
                }
            } catch (NoSuchFieldException unused) {
            } catch (SecurityException e3) {
                throw new AssertionError(e3);
            }
        }
    }

    public DecimalFormatProperties setCompactCustomData(Map<String, Map<String, String>> map) {
        this.compactCustomData = map;
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

    public DecimalFormatProperties setCurrencyPluralInfo(CurrencyPluralInfo currencyPluralInfo2) {
        if (currencyPluralInfo2 != null) {
            currencyPluralInfo2 = (CurrencyPluralInfo) currencyPluralInfo2.clone();
        }
        this.currencyPluralInfo = currencyPluralInfo2;
        return this;
    }

    public DecimalFormatProperties setCurrencyUsage(Currency.CurrencyUsage currencyUsage2) {
        this.currencyUsage = currencyUsage2;
        return this;
    }

    public DecimalFormatProperties setDecimalPatternMatchRequired(boolean z) {
        this.decimalPatternMatchRequired = z;
        return this;
    }

    public DecimalFormatProperties setDecimalSeparatorAlwaysShown(boolean z) {
        this.decimalSeparatorAlwaysShown = z;
        return this;
    }

    public DecimalFormatProperties setExponentSignAlwaysShown(boolean z) {
        this.exponentSignAlwaysShown = z;
        return this;
    }

    public DecimalFormatProperties setFormatWidth(int i) {
        this.formatWidth = i;
        return this;
    }

    public DecimalFormatProperties setGroupingSize(int i) {
        this.groupingSize = i;
        return this;
    }

    public DecimalFormatProperties setGroupingUsed(boolean z) {
        this.groupingUsed = z;
        return this;
    }

    public DecimalFormatProperties setMagnitudeMultiplier(int i) {
        this.magnitudeMultiplier = i;
        return this;
    }

    public DecimalFormatProperties setMathContext(MathContext mathContext2) {
        this.mathContext = mathContext2;
        return this;
    }

    public DecimalFormatProperties setMaximumFractionDigits(int i) {
        this.maximumFractionDigits = i;
        return this;
    }

    public DecimalFormatProperties setMaximumIntegerDigits(int i) {
        this.maximumIntegerDigits = i;
        return this;
    }

    public DecimalFormatProperties setMaximumSignificantDigits(int i) {
        this.maximumSignificantDigits = i;
        return this;
    }

    public DecimalFormatProperties setMinimumExponentDigits(int i) {
        this.minimumExponentDigits = i;
        return this;
    }

    public DecimalFormatProperties setMinimumFractionDigits(int i) {
        this.minimumFractionDigits = i;
        return this;
    }

    public DecimalFormatProperties setMinimumGroupingDigits(int i) {
        this.minimumGroupingDigits = i;
        return this;
    }

    public DecimalFormatProperties setMinimumIntegerDigits(int i) {
        this.minimumIntegerDigits = i;
        return this;
    }

    public DecimalFormatProperties setMinimumSignificantDigits(int i) {
        this.minimumSignificantDigits = i;
        return this;
    }

    public DecimalFormatProperties setMultiplier(BigDecimal bigDecimal) {
        this.multiplier = bigDecimal;
        return this;
    }

    public DecimalFormatProperties setNegativePrefix(String str) {
        this.negativePrefix = str;
        return this;
    }

    public DecimalFormatProperties setNegativePrefixPattern(String str) {
        this.negativePrefixPattern = str;
        return this;
    }

    public DecimalFormatProperties setNegativeSuffix(String str) {
        this.negativeSuffix = str;
        return this;
    }

    public DecimalFormatProperties setNegativeSuffixPattern(String str) {
        this.negativeSuffixPattern = str;
        return this;
    }

    public DecimalFormatProperties setPadPosition(Padder.PadPosition padPosition2) {
        this.padPosition = padPosition2;
        return this;
    }

    public DecimalFormatProperties setPadString(String str) {
        this.padString = str;
        return this;
    }

    public DecimalFormatProperties setParseCaseSensitive(boolean z) {
        this.parseCaseSensitive = z;
        return this;
    }

    public DecimalFormatProperties setParseIntegerOnly(boolean z) {
        this.parseIntegerOnly = z;
        return this;
    }

    public DecimalFormatProperties setParseMode(ParseMode parseMode2) {
        this.parseMode = parseMode2;
        return this;
    }

    public DecimalFormatProperties setParseNoExponent(boolean z) {
        this.parseNoExponent = z;
        return this;
    }

    public DecimalFormatProperties setParseToBigDecimal(boolean z) {
        this.parseToBigDecimal = z;
        return this;
    }

    public DecimalFormatProperties setPluralRules(PluralRules pluralRules2) {
        this.pluralRules = pluralRules2;
        return this;
    }

    public DecimalFormatProperties setPositivePrefix(String str) {
        this.positivePrefix = str;
        return this;
    }

    public DecimalFormatProperties setPositivePrefixPattern(String str) {
        this.positivePrefixPattern = str;
        return this;
    }

    public DecimalFormatProperties setPositiveSuffix(String str) {
        this.positiveSuffix = str;
        return this;
    }

    public DecimalFormatProperties setPositiveSuffixPattern(String str) {
        this.positiveSuffixPattern = str;
        return this;
    }

    public DecimalFormatProperties setRoundingIncrement(BigDecimal bigDecimal) {
        this.roundingIncrement = bigDecimal;
        return this;
    }

    public DecimalFormatProperties setRoundingMode(RoundingMode roundingMode2) {
        this.roundingMode = roundingMode2;
        return this;
    }

    public DecimalFormatProperties setSecondaryGroupingSize(int i) {
        this.secondaryGroupingSize = i;
        return this;
    }

    public DecimalFormatProperties setSignAlwaysShown(boolean z) {
        this.signAlwaysShown = z;
        return this;
    }

    @Override // java.lang.Object
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("<Properties");
        toStringBare(sb);
        sb.append(">");
        return sb.toString();
    }

    public void toStringBare(StringBuilder sb) {
        Field[] declaredFields = DecimalFormatProperties.class.getDeclaredFields();
        for (Field field : declaredFields) {
            try {
                Object obj = field.get(this);
                Object obj2 = field.get(DEFAULT);
                if (obj != null || obj2 != null) {
                    if (obj == null || obj2 == null) {
                        sb.append(" " + field.getName() + ":" + obj);
                    } else {
                        if (!obj.equals(obj2)) {
                            sb.append(" " + field.getName() + ":" + obj);
                        }
                    }
                }
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e2) {
                e2.printStackTrace();
            }
        }
    }

    private void writeObject(ObjectOutputStream objectOutputStream) throws IOException {
        writeObjectImpl(objectOutputStream);
    }

    /* access modifiers changed from: package-private */
    public void writeObjectImpl(ObjectOutputStream objectOutputStream) throws IOException {
        objectOutputStream.defaultWriteObject();
        objectOutputStream.writeInt(0);
        ArrayList arrayList = new ArrayList();
        ArrayList arrayList2 = new ArrayList();
        Field[] declaredFields = DecimalFormatProperties.class.getDeclaredFields();
        for (Field field : declaredFields) {
            if (!Modifier.isStatic(field.getModifiers())) {
                try {
                    Object obj = field.get(this);
                    if (obj != null) {
                        if (!obj.equals(field.get(DEFAULT))) {
                            arrayList.add(field);
                            arrayList2.add(obj);
                        }
                    }
                } catch (IllegalArgumentException e) {
                    throw new AssertionError(e);
                } catch (IllegalAccessException e2) {
                    throw new AssertionError(e2);
                }
            }
        }
        int size = arrayList.size();
        objectOutputStream.writeInt(size);
        for (int i = 0; i < size; i++) {
            Object obj2 = arrayList2.get(i);
            objectOutputStream.writeObject(((Field) arrayList.get(i)).getName());
            objectOutputStream.writeObject(obj2);
        }
    }
}
