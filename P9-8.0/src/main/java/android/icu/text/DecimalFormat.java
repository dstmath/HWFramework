package android.icu.text;

import android.icu.impl.ICUConfig;
import android.icu.impl.PatternProps;
import android.icu.impl.Utility;
import android.icu.impl.locale.LanguageTag;
import android.icu.lang.UCharacter;
import android.icu.lang.UProperty;
import android.icu.math.MathContext;
import android.icu.text.NumberFormat.Field;
import android.icu.text.PluralRules.FixedDecimal;
import android.icu.util.Currency;
import android.icu.util.Currency.CurrencyUsage;
import android.icu.util.CurrencyAmount;
import android.icu.util.ULocale;
import android.icu.util.ULocale.Category;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.text.ChoiceFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class DecimalFormat extends NumberFormat {
    private static final char CURRENCY_SIGN = '¤';
    private static final int CURRENCY_SIGN_COUNT_IN_ISO_FORMAT = 2;
    private static final int CURRENCY_SIGN_COUNT_IN_PLURAL_FORMAT = 3;
    private static final int CURRENCY_SIGN_COUNT_IN_SYMBOL_FORMAT = 1;
    private static final int CURRENCY_SIGN_COUNT_ZERO = 0;
    static final int DOUBLE_FRACTION_DIGITS = 340;
    static final int DOUBLE_INTEGER_DIGITS = 309;
    static final int MAX_INTEGER_DIGITS = 2000000000;
    static final int MAX_SCIENTIFIC_INTEGER_DIGITS = 8;
    static final Unit NULL_UNIT = new Unit("", "");
    public static final int PAD_AFTER_PREFIX = 1;
    public static final int PAD_AFTER_SUFFIX = 3;
    public static final int PAD_BEFORE_PREFIX = 0;
    public static final int PAD_BEFORE_SUFFIX = 2;
    static final char PATTERN_DECIMAL_SEPARATOR = '.';
    static final char PATTERN_DIGIT = '#';
    static final char PATTERN_EIGHT_DIGIT = '8';
    static final char PATTERN_EXPONENT = 'E';
    static final char PATTERN_FIVE_DIGIT = '5';
    static final char PATTERN_FOUR_DIGIT = '4';
    static final char PATTERN_GROUPING_SEPARATOR = ',';
    static final char PATTERN_MINUS_SIGN = '-';
    static final char PATTERN_NINE_DIGIT = '9';
    static final char PATTERN_ONE_DIGIT = '1';
    static final char PATTERN_PAD_ESCAPE = '*';
    private static final char PATTERN_PERCENT = '%';
    private static final char PATTERN_PER_MILLE = '‰';
    static final char PATTERN_PLUS_SIGN = '+';
    private static final char PATTERN_SEPARATOR = ';';
    static final char PATTERN_SEVEN_DIGIT = '7';
    static final char PATTERN_SIGNIFICANT_DIGIT = '@';
    static final char PATTERN_SIX_DIGIT = '6';
    static final char PATTERN_THREE_DIGIT = '3';
    static final char PATTERN_TWO_DIGIT = '2';
    static final char PATTERN_ZERO_DIGIT = '0';
    private static final char QUOTE = '\'';
    private static final int STATUS_INFINITE = 0;
    private static final int STATUS_LENGTH = 3;
    private static final int STATUS_POSITIVE = 1;
    private static final int STATUS_UNDERFLOW = 2;
    private static final UnicodeSet commaEquivalents = new UnicodeSet(44, 44, 1548, 1548, 1643, 1643, UProperty.DOUBLE_LIMIT, UProperty.DOUBLE_LIMIT, 65040, 65041, 65104, 65105, 65292, 65292, 65380, 65380).freeze();
    static final int currentSerialVersion = 4;
    private static final UnicodeSet defaultGroupingSeparators = new UnicodeSet(32, 32, 39, 39, 44, 44, 46, 46, 160, 160, 1548, 1548, 1643, 1644, 8192, 8202, 8216, 8217, 8228, 8228, 8239, 8239, 8287, 8287, 12288, 12290, 65040, 65042, 65104, 65106, 65287, 65287, 65292, 65292, 65294, 65294, 65377, 65377, 65380, 65380).freeze();
    private static final UnicodeSet dotEquivalents = new UnicodeSet(46, 46, 8228, 8228, 12290, 12290, 65042, 65042, 65106, 65106, 65294, 65294, 65377, 65377).freeze();
    private static double epsilon = 1.0E-11d;
    static final UnicodeSet minusSigns = new UnicodeSet(45, 45, 8315, 8315, 8331, 8331, 8722, 8722, 10134, 10134, 65123, 65123, 65293, 65293).freeze();
    static final UnicodeSet plusSigns = new UnicodeSet(43, 43, 8314, 8314, 8330, 8330, 10133, 10133, 64297, 64297, 65122, 65122, 65291, 65291).freeze();
    static final double roundingIncrementEpsilon = 1.0E-9d;
    private static final long serialVersionUID = 864413376551465018L;
    static final boolean skipExtendedSeparatorParsing = ICUConfig.get("android.icu.text.DecimalFormat.SkipExtendedSeparatorParsing", "false").equals("true");
    private static final UnicodeSet strictCommaEquivalents = new UnicodeSet(44, 44, 1643, 1643, 65040, 65040, 65104, 65104, 65292, 65292).freeze();
    private static final UnicodeSet strictDefaultGroupingSeparators = new UnicodeSet(32, 32, 39, 39, 44, 44, 46, 46, 160, 160, 1643, 1644, 8192, 8202, 8216, 8217, 8228, 8228, 8239, 8239, 8287, 8287, 12288, 12288, 65040, 65040, 65104, 65104, 65106, 65106, 65287, 65287, 65292, 65292, 65294, 65294, 65377, 65377).freeze();
    private static final UnicodeSet strictDotEquivalents = new UnicodeSet(46, 46, 8228, 8228, 65106, 65106, 65294, 65294, 65377, 65377).freeze();
    private int PARSE_MAX_EXPONENT = 1000;
    private transient BigDecimal actualRoundingIncrement = null;
    private transient android.icu.math.BigDecimal actualRoundingIncrementICU = null;
    private transient Set<AffixForCurrency> affixPatternsForCurrency = null;
    private ArrayList<FieldPosition> attributes = new ArrayList();
    private ChoiceFormat currencyChoice;
    private CurrencyPluralInfo currencyPluralInfo = null;
    private int currencySignCount = 0;
    private CurrencyUsage currencyUsage = CurrencyUsage.STANDARD;
    private boolean decimalSeparatorAlwaysShown = false;
    private transient DigitList digitList = new DigitList();
    private boolean exponentSignAlwaysShown = false;
    private String formatPattern = "";
    private int formatWidth = 0;
    private byte groupingSize = (byte) 3;
    private byte groupingSize2 = (byte) 0;
    private transient boolean isReadyForParsing = false;
    private MathContext mathContext = new MathContext(0, 0);
    private int maxSignificantDigits = 6;
    private byte minExponentDigits;
    private int minSignificantDigits = 1;
    private int multiplier = 1;
    private String negPrefixPattern;
    private String negSuffixPattern;
    private String negativePrefix = LanguageTag.SEP;
    private String negativeSuffix = "";
    private char pad = ' ';
    private int padPosition = 0;
    private boolean parseBigDecimal = false;
    boolean parseRequireDecimalPoint = false;
    private String posPrefixPattern;
    private String posSuffixPattern;
    private String positivePrefix = "";
    private String positiveSuffix = "";
    private transient double roundingDouble = 0.0d;
    private transient double roundingDoubleReciprocal = 0.0d;
    private BigDecimal roundingIncrement = null;
    private transient android.icu.math.BigDecimal roundingIncrementICU = null;
    private int roundingMode = 6;
    private int serialVersionOnStream = 4;
    private int style = 0;
    private DecimalFormatSymbols symbols = null;
    private boolean useExponentialNotation;
    private boolean useSignificantDigits = false;

    private static final class AffixForCurrency {
        private String negPrefixPatternForCurrency = null;
        private String negSuffixPatternForCurrency = null;
        private final int patternType;
        private String posPrefixPatternForCurrency = null;
        private String posSuffixPatternForCurrency = null;

        public AffixForCurrency(String negPrefix, String negSuffix, String posPrefix, String posSuffix, int type) {
            this.negPrefixPatternForCurrency = negPrefix;
            this.negSuffixPatternForCurrency = negSuffix;
            this.posPrefixPatternForCurrency = posPrefix;
            this.posSuffixPatternForCurrency = posSuffix;
            this.patternType = type;
        }

        public String getNegPrefix() {
            return this.negPrefixPatternForCurrency;
        }

        public String getNegSuffix() {
            return this.negSuffixPatternForCurrency;
        }

        public String getPosPrefix() {
            return this.posPrefixPatternForCurrency;
        }

        public String getPosSuffix() {
            return this.posSuffixPatternForCurrency;
        }

        public int getPatternType() {
            return this.patternType;
        }
    }

    static class Unit {
        private final String prefix;
        private final String suffix;

        public Unit(String prefix, String suffix) {
            this.prefix = prefix;
            this.suffix = suffix;
        }

        public void writeSuffix(StringBuffer toAppendTo) {
            toAppendTo.append(this.suffix);
        }

        public void writePrefix(StringBuffer toAppendTo) {
            toAppendTo.append(this.prefix);
        }

        public boolean equals(Object obj) {
            boolean z = false;
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof Unit)) {
                return false;
            }
            Unit other = (Unit) obj;
            if (this.prefix.equals(other.prefix)) {
                z = this.suffix.equals(other.suffix);
            }
            return z;
        }

        public String toString() {
            return this.prefix + "/" + this.suffix;
        }
    }

    public DecimalFormat() {
        ULocale def = ULocale.getDefault(Category.FORMAT);
        String pattern = NumberFormat.getPattern(def, 0);
        this.symbols = new DecimalFormatSymbols(def);
        setCurrency(Currency.getInstance(def));
        applyPatternWithoutExpandAffix(pattern, false);
        if (this.currencySignCount == 3) {
            this.currencyPluralInfo = new CurrencyPluralInfo(def);
        } else {
            expandAffixAdjustWidth(null);
        }
    }

    public DecimalFormat(String pattern) {
        ULocale def = ULocale.getDefault(Category.FORMAT);
        this.symbols = new DecimalFormatSymbols(def);
        setCurrency(Currency.getInstance(def));
        applyPatternWithoutExpandAffix(pattern, false);
        if (this.currencySignCount == 3) {
            this.currencyPluralInfo = new CurrencyPluralInfo(def);
        } else {
            expandAffixAdjustWidth(null);
        }
    }

    public DecimalFormat(String pattern, DecimalFormatSymbols symbols) {
        createFromPatternAndSymbols(pattern, symbols);
    }

    private void createFromPatternAndSymbols(String pattern, DecimalFormatSymbols inputSymbols) {
        this.symbols = (DecimalFormatSymbols) inputSymbols.clone();
        if (pattern.indexOf(164) >= 0) {
            setCurrencyForSymbols();
        }
        applyPatternWithoutExpandAffix(pattern, false);
        if (this.currencySignCount == 3) {
            this.currencyPluralInfo = new CurrencyPluralInfo(this.symbols.getULocale());
        } else {
            expandAffixAdjustWidth(null);
        }
    }

    public DecimalFormat(String pattern, DecimalFormatSymbols symbols, CurrencyPluralInfo infoInput, int style) {
        CurrencyPluralInfo info = infoInput;
        if (style == 6) {
            info = (CurrencyPluralInfo) infoInput.clone();
        }
        create(pattern, symbols, info, style);
    }

    private void create(String pattern, DecimalFormatSymbols inputSymbols, CurrencyPluralInfo info, int inputStyle) {
        if (inputStyle != 6) {
            createFromPatternAndSymbols(pattern, inputSymbols);
        } else {
            this.symbols = (DecimalFormatSymbols) inputSymbols.clone();
            this.currencyPluralInfo = info;
            applyPatternWithoutExpandAffix(this.currencyPluralInfo.getCurrencyPluralPattern("other"), false);
            setCurrencyForSymbols();
        }
        this.style = inputStyle;
    }

    DecimalFormat(String pattern, DecimalFormatSymbols inputSymbols, int style) {
        CurrencyPluralInfo currencyPluralInfo = null;
        if (style == 6) {
            currencyPluralInfo = new CurrencyPluralInfo(inputSymbols.getULocale());
        }
        create(pattern, inputSymbols, currencyPluralInfo, style);
    }

    public StringBuffer format(double number, StringBuffer result, FieldPosition fieldPosition) {
        return format(number, result, fieldPosition, false);
    }

    private boolean isNegative(double number) {
        if (number >= 0.0d) {
            return number == 0.0d && 1.0d / number < 0.0d;
        } else {
            return true;
        }
    }

    private double round(double number) {
        boolean isNegative = isNegative(number);
        if (isNegative) {
            number = -number;
        }
        if (this.roundingDouble <= 0.0d) {
            return number;
        }
        return round(number, this.roundingDouble, this.roundingDoubleReciprocal, this.roundingMode, isNegative);
    }

    private double multiply(double number) {
        if (this.multiplier != 1) {
            return ((double) this.multiplier) * number;
        }
        return number;
    }

    private StringBuffer format(double number, StringBuffer result, FieldPosition fieldPosition, boolean parseAttr) {
        fieldPosition.setBeginIndex(0);
        fieldPosition.setEndIndex(0);
        if (Double.isNaN(number)) {
            if (fieldPosition.getField() == 0) {
                fieldPosition.setBeginIndex(result.length());
            } else if (fieldPosition.getFieldAttribute() == Field.INTEGER) {
                fieldPosition.setBeginIndex(result.length());
            }
            result.append(this.symbols.getNaN());
            if (parseAttr) {
                addAttribute(Field.INTEGER, result.length() - this.symbols.getNaN().length(), result.length());
            }
            if (fieldPosition.getField() == 0) {
                fieldPosition.setEndIndex(result.length());
            } else if (fieldPosition.getFieldAttribute() == Field.INTEGER) {
                fieldPosition.setEndIndex(result.length());
            }
            addPadding(result, fieldPosition, 0, 0);
            return result;
        }
        number = multiply(number);
        boolean isNegative = isNegative(number);
        number = round(number);
        if (Double.isInfinite(number)) {
            int prefixLen = appendAffix(result, isNegative, true, fieldPosition, parseAttr);
            if (fieldPosition.getField() == 0) {
                fieldPosition.setBeginIndex(result.length());
            } else if (fieldPosition.getFieldAttribute() == Field.INTEGER) {
                fieldPosition.setBeginIndex(result.length());
            }
            result.append(this.symbols.getInfinity());
            if (parseAttr) {
                addAttribute(Field.INTEGER, result.length() - this.symbols.getInfinity().length(), result.length());
            }
            if (fieldPosition.getField() == 0) {
                fieldPosition.setEndIndex(result.length());
            } else if (fieldPosition.getFieldAttribute() == Field.INTEGER) {
                fieldPosition.setEndIndex(result.length());
            }
            addPadding(result, fieldPosition, prefixLen, appendAffix(result, isNegative, false, fieldPosition, parseAttr));
            return result;
        }
        StringBuffer subformat;
        int precision = precision(false);
        if (this.useExponentialNotation && precision > 0 && number != 0.0d && this.roundingMode != 6) {
            int log10RoundingIncr = (1 - precision) + ((int) Math.floor(Math.log10(Math.abs(number))));
            double roundingIncReciprocal = 0.0d;
            double roundingInc = 0.0d;
            if (log10RoundingIncr < 0) {
                roundingIncReciprocal = android.icu.math.BigDecimal.ONE.movePointRight(-log10RoundingIncr).doubleValue();
            } else {
                roundingInc = android.icu.math.BigDecimal.ONE.movePointRight(log10RoundingIncr).doubleValue();
            }
            number = round(number, roundingInc, roundingIncReciprocal, this.roundingMode, isNegative);
        }
        synchronized (this.digitList) {
            boolean z;
            DigitList digitList = this.digitList;
            if (this.useExponentialNotation) {
                z = false;
            } else {
                z = areSignificantDigitsUsed() ^ 1;
            }
            digitList.set(number, precision, z);
            subformat = subformat(number, result, fieldPosition, isNegative, false, parseAttr);
        }
        return subformat;
    }

    @Deprecated
    double adjustNumberAsInFormatting(double number) {
        if (Double.isNaN(number)) {
            return number;
        }
        number = round(multiply(number));
        if (Double.isInfinite(number)) {
            return number;
        }
        return toDigitList(number).getDouble();
    }

    @Deprecated
    DigitList toDigitList(double number) {
        DigitList result = new DigitList();
        result.set(number, precision(false), false);
        return result;
    }

    @Deprecated
    boolean isNumberNegative(double number) {
        if (Double.isNaN(number)) {
            return false;
        }
        return isNegative(multiply(number));
    }

    private static double round(double number, double roundingInc, double roundingIncReciprocal, int mode, boolean isNegative) {
        double div = roundingIncReciprocal == 0.0d ? number / roundingInc : number * roundingIncReciprocal;
        switch (mode) {
            case 0:
                div = Math.ceil(div - epsilon);
                break;
            case 1:
                div = Math.floor(epsilon + div);
                break;
            case 2:
                if (!isNegative) {
                    div = Math.ceil(div - epsilon);
                    break;
                }
                div = Math.floor(epsilon + div);
                break;
            case 3:
                if (!isNegative) {
                    div = Math.floor(epsilon + div);
                    break;
                }
                div = Math.ceil(div - epsilon);
                break;
            case 7:
                if (div == Math.floor(div)) {
                    return number;
                }
                throw new ArithmeticException("Rounding necessary");
            default:
                double ceil = Math.ceil(div);
                double ceildiff = ceil - div;
                double floor = Math.floor(div);
                double floordiff = div - floor;
                switch (mode) {
                    case 4:
                        if (ceildiff > epsilon + floordiff) {
                            div = floor;
                            break;
                        }
                        div = ceil;
                        break;
                    case 5:
                        if (floordiff > epsilon + ceildiff) {
                            div = ceil;
                            break;
                        }
                        div = floor;
                        break;
                    case 6:
                        if (epsilon + floordiff >= ceildiff) {
                            if (epsilon + ceildiff >= floordiff) {
                                double testFloor = floor / 2.0d;
                                if (testFloor != Math.floor(testFloor)) {
                                    div = ceil;
                                    break;
                                }
                                div = floor;
                                break;
                            }
                            div = ceil;
                            break;
                        }
                        div = floor;
                        break;
                    default:
                        throw new IllegalArgumentException("Invalid rounding mode: " + mode);
                }
        }
        return roundingIncReciprocal == 0.0d ? div * roundingInc : div / roundingIncReciprocal;
    }

    public StringBuffer format(long number, StringBuffer result, FieldPosition fieldPosition) {
        return format(number, result, fieldPosition, false);
    }

    private StringBuffer format(long number, StringBuffer result, FieldPosition fieldPosition, boolean parseAttr) {
        fieldPosition.setBeginIndex(0);
        fieldPosition.setEndIndex(0);
        if (this.actualRoundingIncrementICU != null) {
            return format(android.icu.math.BigDecimal.valueOf(number), result, fieldPosition);
        }
        StringBuffer subformat;
        boolean isNegative = number < 0;
        if (isNegative) {
            number = -number;
        }
        if (this.multiplier != 1) {
            boolean tooBig = number < 0 ? number <= Long.MIN_VALUE / ((long) this.multiplier) : number > Long.MAX_VALUE / ((long) this.multiplier);
            if (tooBig) {
                if (isNegative) {
                    number = -number;
                }
                return format(BigInteger.valueOf(number), result, fieldPosition, parseAttr);
            }
        }
        number *= (long) this.multiplier;
        synchronized (this.digitList) {
            this.digitList.set(number, precision(true));
            if (this.digitList.wasRounded() && this.roundingMode == 7) {
                throw new ArithmeticException("Rounding necessary");
            }
            subformat = subformat((double) number, result, fieldPosition, isNegative, true, parseAttr);
        }
        return subformat;
    }

    public StringBuffer format(BigInteger number, StringBuffer result, FieldPosition fieldPosition) {
        return format(number, result, fieldPosition, false);
    }

    private StringBuffer format(BigInteger number, StringBuffer result, FieldPosition fieldPosition, boolean parseAttr) {
        boolean z = true;
        if (this.actualRoundingIncrementICU != null) {
            return format(new android.icu.math.BigDecimal(number), result, fieldPosition);
        }
        StringBuffer subformat;
        if (this.multiplier != 1) {
            number = number.multiply(BigInteger.valueOf((long) this.multiplier));
        }
        synchronized (this.digitList) {
            this.digitList.set(number, precision(true));
            if (this.digitList.wasRounded() && this.roundingMode == 7) {
                throw new ArithmeticException("Rounding necessary");
            }
            int intValue = number.intValue();
            if (number.signum() >= 0) {
                z = false;
            }
            subformat = subformat(intValue, result, fieldPosition, z, true, parseAttr);
        }
        return subformat;
    }

    public StringBuffer format(BigDecimal number, StringBuffer result, FieldPosition fieldPosition) {
        return format(number, result, fieldPosition, false);
    }

    private StringBuffer format(BigDecimal number, StringBuffer result, FieldPosition fieldPosition, boolean parseAttr) {
        StringBuffer subformat;
        boolean z = true;
        if (this.multiplier != 1) {
            number = number.multiply(BigDecimal.valueOf((long) this.multiplier));
        }
        if (this.actualRoundingIncrement != null) {
            number = number.divide(this.actualRoundingIncrement, 0, this.roundingMode).multiply(this.actualRoundingIncrement);
        }
        synchronized (this.digitList) {
            boolean z2;
            DigitList digitList = this.digitList;
            int precision = precision(false);
            if (this.useExponentialNotation) {
                z2 = false;
            } else {
                z2 = areSignificantDigitsUsed() ^ 1;
            }
            digitList.set(number, precision, z2);
            if (this.digitList.wasRounded() && this.roundingMode == 7) {
                throw new ArithmeticException("Rounding necessary");
            }
            double doubleValue = number.doubleValue();
            if (number.signum() >= 0) {
                z = false;
            }
            subformat = subformat(doubleValue, result, fieldPosition, z, false, parseAttr);
        }
        return subformat;
    }

    public StringBuffer format(android.icu.math.BigDecimal number, StringBuffer result, FieldPosition fieldPosition) {
        StringBuffer subformat;
        boolean z = true;
        if (this.multiplier != 1) {
            number = number.multiply(android.icu.math.BigDecimal.valueOf((long) this.multiplier), this.mathContext);
        }
        if (this.actualRoundingIncrementICU != null) {
            number = number.divide(this.actualRoundingIncrementICU, 0, this.roundingMode).multiply(this.actualRoundingIncrementICU, this.mathContext);
        }
        synchronized (this.digitList) {
            boolean z2;
            DigitList digitList = this.digitList;
            int precision = precision(false);
            if (this.useExponentialNotation) {
                z2 = false;
            } else {
                z2 = areSignificantDigitsUsed() ^ 1;
            }
            digitList.set(number, precision, z2);
            if (this.digitList.wasRounded() && this.roundingMode == 7) {
                throw new ArithmeticException("Rounding necessary");
            }
            double doubleValue = number.doubleValue();
            if (number.signum() >= 0) {
                z = false;
            }
            subformat = subformat(doubleValue, result, fieldPosition, z, false, false);
        }
        return subformat;
    }

    private boolean isGroupingPosition(int pos) {
        if (!isGroupingUsed() || pos <= 0 || this.groupingSize <= (byte) 0) {
            return false;
        }
        return (this.groupingSize2 <= (byte) 0 || pos <= this.groupingSize) ? pos % this.groupingSize == 0 : (pos - this.groupingSize) % this.groupingSize2 == 0;
    }

    private int precision(boolean isIntegral) {
        if (areSignificantDigitsUsed()) {
            return getMaximumSignificantDigits();
        }
        if (this.useExponentialNotation) {
            return getMinimumIntegerDigits() + getMaximumFractionDigits();
        }
        return isIntegral ? 0 : getMaximumFractionDigits();
    }

    private StringBuffer subformat(int number, StringBuffer result, FieldPosition fieldPosition, boolean isNegative, boolean isInteger, boolean parseAttr) {
        if (this.currencySignCount != 3) {
            return subformat(result, fieldPosition, isNegative, isInteger, parseAttr);
        }
        return subformat(this.currencyPluralInfo.select(getFixedDecimal((double) number)), result, fieldPosition, isNegative, isInteger, parseAttr);
    }

    FixedDecimal getFixedDecimal(double number) {
        return getFixedDecimal(number, this.digitList);
    }

    FixedDecimal getFixedDecimal(double number, DigitList dl) {
        int maxFractionalDigits;
        int minFractionalDigits;
        int fractionalDigitsInDigitList = dl.count - dl.decimalAt;
        if (this.useSignificantDigits) {
            maxFractionalDigits = this.maxSignificantDigits - dl.decimalAt;
            minFractionalDigits = this.minSignificantDigits - dl.decimalAt;
            if (minFractionalDigits < 0) {
                minFractionalDigits = 0;
            }
            if (maxFractionalDigits < 0) {
                maxFractionalDigits = 0;
            }
        } else {
            maxFractionalDigits = getMaximumFractionDigits();
            minFractionalDigits = getMinimumFractionDigits();
        }
        int v = fractionalDigitsInDigitList;
        if (fractionalDigitsInDigitList < minFractionalDigits) {
            v = minFractionalDigits;
        } else if (fractionalDigitsInDigitList > maxFractionalDigits) {
            v = maxFractionalDigits;
        }
        long f = 0;
        if (v > 0) {
            int i;
            for (i = Math.max(0, dl.decimalAt); i < dl.count; i++) {
                f = (f * 10) + ((long) (dl.digits[i] - 48));
            }
            for (i = v; i < fractionalDigitsInDigitList; i++) {
                f *= 10;
            }
        }
        return new FixedDecimal(number, v, f);
    }

    private StringBuffer subformat(double number, StringBuffer result, FieldPosition fieldPosition, boolean isNegative, boolean isInteger, boolean parseAttr) {
        if (this.currencySignCount != 3) {
            return subformat(result, fieldPosition, isNegative, isInteger, parseAttr);
        }
        return subformat(this.currencyPluralInfo.select(getFixedDecimal(number)), result, fieldPosition, isNegative, isInteger, parseAttr);
    }

    private StringBuffer subformat(String pluralCount, StringBuffer result, FieldPosition fieldPosition, boolean isNegative, boolean isInteger, boolean parseAttr) {
        if (this.style == 6) {
            String currencyPluralPattern = this.currencyPluralInfo.getCurrencyPluralPattern(pluralCount);
            if (!this.formatPattern.equals(currencyPluralPattern)) {
                applyPatternWithoutExpandAffix(currencyPluralPattern, false);
            }
        }
        expandAffixAdjustWidth(pluralCount);
        return subformat(result, fieldPosition, isNegative, isInteger, parseAttr);
    }

    private StringBuffer subformat(StringBuffer result, FieldPosition fieldPosition, boolean isNegative, boolean isInteger, boolean parseAttr) {
        if (this.digitList.isZero()) {
            this.digitList.decimalAt = 0;
        }
        int prefixLen = appendAffix(result, isNegative, true, fieldPosition, parseAttr);
        if (this.useExponentialNotation) {
            subformatExponential(result, fieldPosition, parseAttr);
        } else {
            subformatFixed(result, fieldPosition, isInteger, parseAttr);
        }
        addPadding(result, fieldPosition, prefixLen, appendAffix(result, isNegative, false, fieldPosition, parseAttr));
        return result;
    }

    private void subformatFixed(StringBuffer result, FieldPosition fieldPosition, boolean isInteger, boolean parseAttr) {
        String[] digits = this.symbols.getDigitStrings();
        String grouping = this.currencySignCount == 0 ? this.symbols.getGroupingSeparatorString() : this.symbols.getMonetaryGroupingSeparatorString();
        String decimal = this.currencySignCount == 0 ? this.symbols.getDecimalSeparatorString() : this.symbols.getMonetaryDecimalSeparatorString();
        boolean useSigDig = areSignificantDigitsUsed();
        int maxIntDig = getMaximumIntegerDigits();
        int minIntDig = getMinimumIntegerDigits();
        int intBegin = result.length();
        if (fieldPosition.getField() == 0 || fieldPosition.getFieldAttribute() == Field.INTEGER) {
            fieldPosition.setBeginIndex(intBegin);
        }
        long fractionalDigits = 0;
        int fractionalDigitsCount = 0;
        int sigCount = 0;
        int minSigDig = getMinimumSignificantDigits();
        int maxSigDig = getMaximumSignificantDigits();
        if (!useSigDig) {
            minSigDig = 0;
            maxSigDig = Integer.MAX_VALUE;
        }
        int count = useSigDig ? Math.max(1, this.digitList.decimalAt) : minIntDig;
        if (this.digitList.decimalAt > 0 && count < this.digitList.decimalAt) {
            count = this.digitList.decimalAt;
        }
        int digitIndex = 0;
        if (count > maxIntDig && maxIntDig >= 0) {
            count = maxIntDig;
            digitIndex = this.digitList.decimalAt - maxIntDig;
        }
        int sizeBeforeIntegerPart = result.length();
        int i = count - 1;
        int digitIndex2 = digitIndex;
        while (i >= 0) {
            if (i >= this.digitList.decimalAt || digitIndex2 >= this.digitList.count || sigCount >= maxSigDig) {
                result.append(digits[0]);
                if (sigCount > 0) {
                    sigCount++;
                    digitIndex = digitIndex2;
                } else {
                    digitIndex = digitIndex2;
                }
            } else {
                digitIndex = digitIndex2 + 1;
                result.append(digits[this.digitList.getDigitValue(digitIndex2)]);
                sigCount++;
            }
            if (isGroupingPosition(i)) {
                result.append(grouping);
                if (fieldPosition.getFieldAttribute() == Field.GROUPING_SEPARATOR && fieldPosition.getBeginIndex() == 0 && fieldPosition.getEndIndex() == 0) {
                    fieldPosition.setBeginIndex(result.length() - 1);
                    fieldPosition.setEndIndex(result.length());
                }
                if (parseAttr) {
                    addAttribute(Field.GROUPING_SEPARATOR, result.length() - 1, result.length());
                }
            }
            i--;
            digitIndex2 = digitIndex;
        }
        if (fieldPosition.getField() == 0 || fieldPosition.getFieldAttribute() == Field.INTEGER) {
            fieldPosition.setEndIndex(result.length());
        }
        if (sigCount == 0 && this.digitList.count == 0) {
            sigCount = 1;
        }
        boolean fractionPresent = (isInteger || digitIndex2 >= this.digitList.count) ? useSigDig ? sigCount >= minSigDig : getMinimumFractionDigits() <= 0 : true;
        if (!fractionPresent && result.length() == sizeBeforeIntegerPart) {
            result.append(digits[0]);
        }
        if (parseAttr) {
            addAttribute(Field.INTEGER, intBegin, result.length());
        }
        if (this.decimalSeparatorAlwaysShown || fractionPresent) {
            if (fieldPosition.getFieldAttribute() == Field.DECIMAL_SEPARATOR) {
                fieldPosition.setBeginIndex(result.length());
            }
            result.append(decimal);
            if (fieldPosition.getFieldAttribute() == Field.DECIMAL_SEPARATOR) {
                fieldPosition.setEndIndex(result.length());
            }
            if (parseAttr) {
                addAttribute(Field.DECIMAL_SEPARATOR, result.length() - 1, result.length());
            }
        }
        if (fieldPosition.getField() == 1) {
            fieldPosition.setBeginIndex(result.length());
        } else {
            if (fieldPosition.getFieldAttribute() == Field.FRACTION) {
                fieldPosition.setBeginIndex(result.length());
            }
        }
        int fracBegin = result.length();
        boolean recordFractionDigits = fieldPosition instanceof UFieldPosition;
        count = useSigDig ? Integer.MAX_VALUE : getMaximumFractionDigits();
        if (useSigDig && (sigCount == maxSigDig || (sigCount >= minSigDig && digitIndex2 == this.digitList.count))) {
            count = 0;
        }
        i = 0;
        while (i < count) {
            if (!useSigDig && i >= getMinimumFractionDigits() && (isInteger || digitIndex2 >= this.digitList.count)) {
                digitIndex = digitIndex2;
                break;
            }
            if (-1 - i <= this.digitList.decimalAt - 1) {
                if (isInteger || digitIndex2 >= this.digitList.count) {
                    result.append(digits[0]);
                    if (recordFractionDigits) {
                        fractionalDigitsCount++;
                        fractionalDigits *= 10;
                        digitIndex = digitIndex2;
                    } else {
                        digitIndex = digitIndex2;
                    }
                } else {
                    digitIndex = digitIndex2 + 1;
                    byte digit = this.digitList.getDigitValue(digitIndex2);
                    result.append(digits[digit]);
                    if (recordFractionDigits) {
                        fractionalDigitsCount++;
                        fractionalDigits = (fractionalDigits * 10) + ((long) digit);
                    }
                }
                sigCount++;
                if (useSigDig) {
                    if (sigCount != maxSigDig) {
                        if (digitIndex == this.digitList.count && sigCount >= minSigDig) {
                            break;
                        }
                    }
                    break;
                }
                continue;
            } else {
                result.append(digits[0]);
                if (recordFractionDigits) {
                    fractionalDigitsCount++;
                    fractionalDigits *= 10;
                }
                digitIndex = digitIndex2;
            }
            i++;
            digitIndex2 = digitIndex;
        }
        if (fieldPosition.getField() == 1) {
            fieldPosition.setEndIndex(result.length());
        } else {
            if (fieldPosition.getFieldAttribute() == Field.FRACTION) {
                fieldPosition.setEndIndex(result.length());
            }
        }
        if (recordFractionDigits) {
            ((UFieldPosition) fieldPosition).setFractionDigits(fractionalDigitsCount, fractionalDigits);
        }
        if (!parseAttr) {
            return;
        }
        if (this.decimalSeparatorAlwaysShown || fractionPresent) {
            addAttribute(Field.FRACTION, fracBegin, result.length());
        }
    }

    private void subformatExponential(StringBuffer result, FieldPosition fieldPosition, boolean parseAttr) {
        int minFracDig;
        String[] digits = this.symbols.getDigitStringsLocal();
        String decimal = this.currencySignCount == 0 ? this.symbols.getDecimalSeparatorString() : this.symbols.getMonetaryDecimalSeparatorString();
        boolean useSigDig = areSignificantDigitsUsed();
        int maxIntDig = getMaximumIntegerDigits();
        int minIntDig = getMinimumIntegerDigits();
        if (fieldPosition.getField() == 0) {
            fieldPosition.setBeginIndex(result.length());
            fieldPosition.setEndIndex(-1);
        } else if (fieldPosition.getField() == 1) {
            fieldPosition.setBeginIndex(-1);
        } else {
            if (fieldPosition.getFieldAttribute() == Field.INTEGER) {
                fieldPosition.setBeginIndex(result.length());
                fieldPosition.setEndIndex(-1);
            } else {
                if (fieldPosition.getFieldAttribute() == Field.FRACTION) {
                    fieldPosition.setBeginIndex(-1);
                }
            }
        }
        int intBegin = result.length();
        int intEnd = -1;
        int fracBegin = -1;
        if (useSigDig) {
            minIntDig = 1;
            maxIntDig = 1;
            minFracDig = getMinimumSignificantDigits() - 1;
        } else {
            minFracDig = getMinimumFractionDigits();
            if (maxIntDig > 8) {
                maxIntDig = 1;
                if (1 < minIntDig) {
                    maxIntDig = minIntDig;
                }
            }
            if (maxIntDig > minIntDig) {
                minIntDig = 1;
            }
        }
        long fractionalDigits = 0;
        int fractionalDigitsCount = 0;
        boolean recordFractionDigits = false;
        int exponent = this.digitList.decimalAt;
        if (maxIntDig <= 1 || maxIntDig == minIntDig) {
            int i = (minIntDig > 0 || minFracDig > 0) ? minIntDig : 1;
            exponent -= i;
        } else {
            exponent = (exponent > 0 ? (exponent - 1) / maxIntDig : (exponent / maxIntDig) - 1) * maxIntDig;
        }
        int minimumDigits = minIntDig + minFracDig;
        int integerDigits = this.digitList.isZero() ? minIntDig : this.digitList.decimalAt - exponent;
        int totalDigits = this.digitList.count;
        if (minimumDigits > totalDigits) {
            totalDigits = minimumDigits;
        }
        if (integerDigits > totalDigits) {
            totalDigits = integerDigits;
        }
        int i2 = 0;
        while (i2 < totalDigits) {
            if (i2 == integerDigits) {
                if (fieldPosition.getField() == 0) {
                    fieldPosition.setEndIndex(result.length());
                } else {
                    if (fieldPosition.getFieldAttribute() == Field.INTEGER) {
                        fieldPosition.setEndIndex(result.length());
                    }
                }
                if (parseAttr) {
                    intEnd = result.length();
                    addAttribute(Field.INTEGER, intBegin, result.length());
                }
                if (fieldPosition.getFieldAttribute() == Field.DECIMAL_SEPARATOR) {
                    fieldPosition.setBeginIndex(result.length());
                }
                result.append(decimal);
                if (fieldPosition.getFieldAttribute() == Field.DECIMAL_SEPARATOR) {
                    fieldPosition.setEndIndex(result.length());
                }
                fracBegin = result.length();
                if (parseAttr) {
                    addAttribute(Field.DECIMAL_SEPARATOR, result.length() - 1, result.length());
                }
                if (fieldPosition.getField() == 1) {
                    fieldPosition.setBeginIndex(result.length());
                } else {
                    if (fieldPosition.getFieldAttribute() == Field.FRACTION) {
                        fieldPosition.setBeginIndex(result.length());
                    }
                }
                recordFractionDigits = fieldPosition instanceof UFieldPosition;
            }
            int digit = i2 < this.digitList.count ? this.digitList.getDigitValue(i2) : 0;
            result.append(digits[digit]);
            if (recordFractionDigits) {
                fractionalDigitsCount++;
                fractionalDigits = (fractionalDigits * 10) + ((long) digit);
            }
            i2++;
        }
        if (this.digitList.isZero() && totalDigits == 0) {
            result.append(digits[0]);
        }
        if (fracBegin == -1 && this.decimalSeparatorAlwaysShown) {
            if (fieldPosition.getFieldAttribute() == Field.DECIMAL_SEPARATOR) {
                fieldPosition.setBeginIndex(result.length());
            }
            result.append(decimal);
            if (fieldPosition.getFieldAttribute() == Field.DECIMAL_SEPARATOR) {
                fieldPosition.setEndIndex(result.length());
            }
            if (parseAttr) {
                addAttribute(Field.DECIMAL_SEPARATOR, result.length() - 1, result.length());
            }
        }
        if (fieldPosition.getField() == 0) {
            if (fieldPosition.getEndIndex() < 0) {
                fieldPosition.setEndIndex(result.length());
            }
        } else if (fieldPosition.getField() == 1) {
            if (fieldPosition.getBeginIndex() < 0) {
                fieldPosition.setBeginIndex(result.length());
            }
            fieldPosition.setEndIndex(result.length());
        } else {
            if (fieldPosition.getFieldAttribute() != Field.INTEGER) {
                if (fieldPosition.getFieldAttribute() == Field.FRACTION) {
                    if (fieldPosition.getBeginIndex() < 0) {
                        fieldPosition.setBeginIndex(result.length());
                    }
                    fieldPosition.setEndIndex(result.length());
                }
            } else if (fieldPosition.getEndIndex() < 0) {
                fieldPosition.setEndIndex(result.length());
            }
        }
        if (recordFractionDigits) {
            ((UFieldPosition) fieldPosition).setFractionDigits(fractionalDigitsCount, fractionalDigits);
        }
        if (parseAttr) {
            if (intEnd < 0) {
                addAttribute(Field.INTEGER, intBegin, result.length());
            }
            if (fracBegin > 0) {
                addAttribute(Field.FRACTION, fracBegin, result.length());
            }
        }
        if (fieldPosition.getFieldAttribute() == Field.EXPONENT_SYMBOL) {
            fieldPosition.setBeginIndex(result.length());
        }
        result.append(this.symbols.getExponentSeparator());
        if (fieldPosition.getFieldAttribute() == Field.EXPONENT_SYMBOL) {
            fieldPosition.setEndIndex(result.length());
        }
        if (parseAttr) {
            addAttribute(Field.EXPONENT_SYMBOL, result.length() - this.symbols.getExponentSeparator().length(), result.length());
        }
        if (this.digitList.isZero()) {
            exponent = 0;
        }
        if (exponent < 0) {
            exponent = -exponent;
            if (fieldPosition.getFieldAttribute() == Field.EXPONENT_SIGN) {
                fieldPosition.setBeginIndex(result.length());
            }
            result.append(this.symbols.getMinusSignString());
            if (fieldPosition.getFieldAttribute() == Field.EXPONENT_SIGN) {
                fieldPosition.setEndIndex(result.length());
            }
            if (parseAttr) {
                addAttribute(Field.EXPONENT_SIGN, result.length() - 1, result.length());
            }
        } else if (this.exponentSignAlwaysShown) {
            if (fieldPosition.getFieldAttribute() == Field.EXPONENT_SIGN) {
                fieldPosition.setBeginIndex(result.length());
            }
            result.append(this.symbols.getPlusSignString());
            if (fieldPosition.getFieldAttribute() == Field.EXPONENT_SIGN) {
                fieldPosition.setEndIndex(result.length());
            }
            if (parseAttr) {
                addAttribute(Field.EXPONENT_SIGN, result.length() - 1, result.length());
            }
        }
        int expBegin = result.length();
        this.digitList.set((long) exponent);
        int expDig = this.minExponentDigits;
        if (this.useExponentialNotation && expDig < 1) {
            expDig = 1;
        }
        for (i2 = this.digitList.decimalAt; i2 < expDig; i2++) {
            result.append(digits[0]);
        }
        for (i2 = 0; i2 < this.digitList.decimalAt; i2++) {
            String str;
            if (i2 < this.digitList.count) {
                str = digits[this.digitList.getDigitValue(i2)];
            } else {
                str = digits[0];
            }
            result.append(str);
        }
        if (fieldPosition.getFieldAttribute() == Field.EXPONENT) {
            fieldPosition.setBeginIndex(expBegin);
            fieldPosition.setEndIndex(result.length());
        }
        if (parseAttr) {
            addAttribute(Field.EXPONENT, expBegin, result.length());
        }
    }

    private final void addPadding(StringBuffer result, FieldPosition fieldPosition, int prefixLen, int suffixLen) {
        if (this.formatWidth > 0) {
            int len = this.formatWidth - result.length();
            if (len > 0) {
                char[] padding = new char[len];
                for (int i = 0; i < len; i++) {
                    padding[i] = this.pad;
                }
                switch (this.padPosition) {
                    case 0:
                        result.insert(0, padding);
                        break;
                    case 1:
                        result.insert(prefixLen, padding);
                        break;
                    case 2:
                        result.insert(result.length() - suffixLen, padding);
                        break;
                    case 3:
                        result.append(padding);
                        break;
                }
                if (this.padPosition == 0 || this.padPosition == 1) {
                    fieldPosition.setBeginIndex(fieldPosition.getBeginIndex() + len);
                    fieldPosition.setEndIndex(fieldPosition.getEndIndex() + len);
                }
            }
        }
    }

    public Number parse(String text, ParsePosition parsePosition) {
        return (Number) parse(text, parsePosition, null);
    }

    public CurrencyAmount parseCurrency(CharSequence text, ParsePosition pos) {
        return (CurrencyAmount) parse(text.toString(), pos, new Currency[1]);
    }

    private Object parse(String text, ParsePosition parsePosition, Currency[] currency) {
        int backup = parsePosition.getIndex();
        int i = backup;
        if (this.formatWidth > 0 && (this.padPosition == 0 || this.padPosition == 1)) {
            i = skipPadding(text, backup);
        }
        if (text.regionMatches(i, this.symbols.getNaN(), 0, this.symbols.getNaN().length())) {
            i += this.symbols.getNaN().length();
            if (this.formatWidth > 0 && (this.padPosition == 2 || this.padPosition == 3)) {
                i = skipPadding(text, i);
            }
            parsePosition.setIndex(i);
            return new Double(Double.NaN);
        }
        Number n;
        i = backup;
        boolean[] status = new boolean[3];
        if (this.currencySignCount != 0) {
            if (!parseForCurrency(text, parsePosition, currency, status)) {
                return null;
            }
        } else if (currency != null) {
            return null;
        } else {
            if (!subparse(text, parsePosition, this.digitList, status, currency, this.negPrefixPattern, this.negSuffixPattern, this.posPrefixPattern, this.posSuffixPattern, false, 0)) {
                parsePosition.setIndex(backup);
                return null;
            }
        }
        Number d;
        if (status[0]) {
            double d2;
            if (status[1]) {
                d2 = Double.POSITIVE_INFINITY;
            } else {
                d2 = Double.NEGATIVE_INFINITY;
            }
            d = new Double(d2);
        } else if (status[2]) {
            if (status[1]) {
                d = new Double("0.0");
            } else {
                d = new Double("-0.0");
            }
        } else if (status[1] || !this.digitList.isZero()) {
            int mult = this.multiplier;
            while (mult % 10 == 0) {
                DigitList digitList = this.digitList;
                digitList.decimalAt--;
                mult /= 10;
            }
            if (this.parseBigDecimal || mult != 1 || !this.digitList.isIntegral()) {
                Number big = this.digitList.getBigDecimalICU(status[1]);
                n = big;
                if (mult != 1) {
                    n = big.divide(android.icu.math.BigDecimal.valueOf((long) mult), this.mathContext);
                }
            } else if (this.digitList.decimalAt < 12) {
                long l = 0;
                if (this.digitList.count > 0) {
                    int nx = 0;
                    while (nx < this.digitList.count) {
                        l = ((10 * l) + ((long) ((char) this.digitList.digits[nx]))) - 48;
                        nx++;
                    }
                    while (true) {
                        int nx2 = nx + 1;
                        if (nx >= this.digitList.decimalAt) {
                            break;
                        }
                        l *= 10;
                        nx = nx2;
                    }
                    if (!status[1]) {
                        l = -l;
                    }
                }
                n = Long.valueOf(l);
            } else {
                Number big2 = this.digitList.getBigInteger(status[1]);
                n = big2.bitLength() < 64 ? Long.valueOf(big2.longValue()) : big2;
            }
        } else {
            d = new Double("-0.0");
        }
        if (currency != null) {
            n = new CurrencyAmount(n, currency[0]);
        }
        return n;
    }

    private boolean parseForCurrency(String text, ParsePosition parsePosition, Currency[] currency, boolean[] status) {
        boolean found;
        int origPos = parsePosition.getIndex();
        if (!this.isReadyForParsing) {
            int savedCurrencySignCount = this.currencySignCount;
            setupCurrencyAffixForAllPatterns();
            if (savedCurrencySignCount == 3) {
                applyPatternWithoutExpandAffix(this.formatPattern, false);
            } else {
                applyPattern(this.formatPattern, false);
            }
            this.isReadyForParsing = true;
        }
        int maxPosIndex = origPos;
        int maxErrorPos = -1;
        boolean[] savedStatus = null;
        boolean[] tmpStatus = new boolean[3];
        ParsePosition tmpPos = new ParsePosition(origPos);
        DigitList tmpDigitList = new DigitList();
        if (this.style == 6) {
            found = subparse(text, tmpPos, tmpDigitList, tmpStatus, currency, this.negPrefixPattern, this.negSuffixPattern, this.posPrefixPattern, this.posSuffixPattern, true, 1);
        } else {
            found = subparse(text, tmpPos, tmpDigitList, tmpStatus, currency, this.negPrefixPattern, this.negSuffixPattern, this.posPrefixPattern, this.posSuffixPattern, true, 0);
        }
        if (!found) {
            maxErrorPos = tmpPos.getErrorIndex();
        } else if (tmpPos.getIndex() > origPos) {
            maxPosIndex = tmpPos.getIndex();
            savedStatus = tmpStatus;
            this.digitList = tmpDigitList;
        }
        for (AffixForCurrency affix : this.affixPatternsForCurrency) {
            tmpStatus = new boolean[3];
            tmpPos = new ParsePosition(origPos);
            tmpDigitList = new DigitList();
            if (subparse(text, tmpPos, tmpDigitList, tmpStatus, currency, affix.getNegPrefix(), affix.getNegSuffix(), affix.getPosPrefix(), affix.getPosSuffix(), true, affix.getPatternType())) {
                found = true;
                if (tmpPos.getIndex() > maxPosIndex) {
                    maxPosIndex = tmpPos.getIndex();
                    savedStatus = tmpStatus;
                    this.digitList = tmpDigitList;
                }
            } else if (tmpPos.getErrorIndex() > maxErrorPos) {
                maxErrorPos = tmpPos.getErrorIndex();
            }
        }
        tmpStatus = new boolean[3];
        tmpPos = new ParsePosition(origPos);
        tmpDigitList = new DigitList();
        if (subparse(text, tmpPos, tmpDigitList, tmpStatus, currency, this.negativePrefix, this.negativeSuffix, this.positivePrefix, this.positiveSuffix, false, 0)) {
            if (tmpPos.getIndex() > maxPosIndex) {
                maxPosIndex = tmpPos.getIndex();
                savedStatus = tmpStatus;
                this.digitList = tmpDigitList;
            }
            found = true;
        } else if (tmpPos.getErrorIndex() > maxErrorPos) {
            maxErrorPos = tmpPos.getErrorIndex();
        }
        if (found) {
            parsePosition.setIndex(maxPosIndex);
            parsePosition.setErrorIndex(-1);
            for (int index = 0; index < 3; index++) {
                status[index] = savedStatus[index];
            }
        } else {
            parsePosition.setErrorIndex(maxErrorPos);
        }
        return found;
    }

    private void setupCurrencyAffixForAllPatterns() {
        if (this.currencyPluralInfo == null) {
            this.currencyPluralInfo = new CurrencyPluralInfo(this.symbols.getULocale());
        }
        this.affixPatternsForCurrency = new HashSet();
        String savedFormatPattern = this.formatPattern;
        applyPatternWithoutExpandAffix(NumberFormat.getPattern(this.symbols.getULocale(), 1), false);
        this.affixPatternsForCurrency.add(new AffixForCurrency(this.negPrefixPattern, this.negSuffixPattern, this.posPrefixPattern, this.posSuffixPattern, 0));
        Iterator<String> iter = this.currencyPluralInfo.pluralPatternIterator();
        Set<String> currencyUnitPatternSet = new HashSet();
        while (iter.hasNext()) {
            String currencyPattern = this.currencyPluralInfo.getCurrencyPluralPattern((String) iter.next());
            if (!(currencyPattern == null || currencyUnitPatternSet.contains(currencyPattern))) {
                currencyUnitPatternSet.add(currencyPattern);
                applyPatternWithoutExpandAffix(currencyPattern, false);
                this.affixPatternsForCurrency.add(new AffixForCurrency(this.negPrefixPattern, this.negSuffixPattern, this.posPrefixPattern, this.posSuffixPattern, 1));
            }
        }
        this.formatPattern = savedFormatPattern;
    }

    private final boolean subparse(String text, ParsePosition parsePosition, DigitList digits, boolean[] status, Currency[] currency, String negPrefix, String negSuffix, String posPrefix, String posSuffix, boolean parseComplexCurrency, int type) {
        int position = parsePosition.getIndex();
        int oldStart = parsePosition.getIndex();
        if (this.formatWidth > 0 && this.padPosition == 0) {
            position = skipPadding(text, position);
        }
        int posMatch = compareAffix(text, position, false, true, posPrefix, parseComplexCurrency, type, currency);
        int negMatch = compareAffix(text, position, true, true, negPrefix, parseComplexCurrency, type, currency);
        if (posMatch >= 0 && negMatch >= 0) {
            if (posMatch > negMatch) {
                negMatch = -1;
            } else if (negMatch > posMatch) {
                posMatch = -1;
            }
        }
        if (posMatch >= 0) {
            position += posMatch;
        } else if (negMatch >= 0) {
            position += negMatch;
        } else {
            parsePosition.setErrorIndex(position);
            return false;
        }
        if (this.formatWidth > 0 && this.padPosition == 1) {
            position = skipPadding(text, position);
        }
        status[0] = false;
        if (text.regionMatches(position, this.symbols.getInfinity(), 0, this.symbols.getInfinity().length())) {
            position += this.symbols.getInfinity().length();
            status[0] = true;
        } else {
            digits.count = 0;
            digits.decimalAt = 0;
            String decimal = this.currencySignCount == 0 ? this.symbols.getDecimalSeparatorString() : this.symbols.getMonetaryDecimalSeparatorString();
            String grouping = this.currencySignCount == 0 ? this.symbols.getGroupingSeparatorString() : this.symbols.getMonetaryGroupingSeparatorString();
            String exponentSep = this.symbols.getExponentSeparator();
            boolean sawDecimal = false;
            boolean sawGrouping = false;
            boolean sawDigit = false;
            long exponent = 0;
            boolean strictParse = isParseStrict();
            boolean strictFail = false;
            int lastGroup = -1;
            int groupedDigitCount = 0;
            if (this.groupingSize2 == (byte) 0) {
                int gs2 = this.groupingSize;
            } else {
                byte gs22 = this.groupingSize2;
            }
            UnicodeSet decimalEquiv;
            if (skipExtendedSeparatorParsing) {
                decimalEquiv = UnicodeSet.EMPTY;
            } else {
                decimalEquiv = getEquivalentDecimals(decimal, strictParse);
            }
            UnicodeSet groupEquiv = skipExtendedSeparatorParsing ? UnicodeSet.EMPTY : strictParse ? strictDefaultGroupingSeparators : defaultGroupingSeparators;
            int digitCount = 0;
            int backup = -1;
            int[] parsedDigit = new int[]{-1};
            while (position < text.length()) {
                int matchLen = matchesDigit(text, position, parsedDigit);
                if (matchLen <= 0) {
                    int decimalStrLen = decimal.length();
                    if (text.regionMatches(position, decimal, 0, decimalStrLen)) {
                        if (!strictParse || (backup == -1 && (lastGroup == -1 || groupedDigitCount == this.groupingSize))) {
                            if (isParseIntegerOnly() || sawDecimal) {
                                break;
                            }
                            digits.decimalAt = digitCount;
                            sawDecimal = true;
                            position += decimalStrLen;
                        } else {
                            strictFail = true;
                            break;
                        }
                    }
                    if (isGroupingUsed()) {
                        int groupingStrLen = grouping.length();
                        if (text.regionMatches(position, grouping, 0, groupingStrLen)) {
                            if (!sawDecimal) {
                                if (strictParse && (!sawDigit || backup != -1)) {
                                    strictFail = true;
                                    break;
                                }
                                backup = position;
                                position += groupingStrLen;
                                sawGrouping = true;
                            } else {
                                break;
                            }
                        }
                    }
                    int cp = text.codePointAt(position);
                    if (!sawDecimal && decimalEquiv.contains(cp)) {
                        if (!strictParse || (backup == -1 && (lastGroup == -1 || groupedDigitCount == this.groupingSize))) {
                            if (isParseIntegerOnly()) {
                                break;
                            }
                            digits.decimalAt = digitCount;
                            decimal = String.valueOf(Character.toChars(cp));
                            sawDecimal = true;
                            position += Character.charCount(cp);
                        } else {
                            strictFail = true;
                            break;
                        }
                    } else if (isGroupingUsed() && (sawGrouping ^ 1) != 0 && groupEquiv.contains(cp)) {
                        if (!sawDecimal) {
                            if (strictParse && (!sawDigit || backup != -1)) {
                                strictFail = true;
                                break;
                            }
                            grouping = String.valueOf(Character.toChars(cp));
                            backup = position;
                            position += Character.charCount(cp);
                            sawGrouping = true;
                        } else {
                            break;
                        }
                    } else {
                        if (text.regionMatches(true, position, exponentSep, 0, exponentSep.length())) {
                            boolean negExp = false;
                            int pos = position + exponentSep.length();
                            if (pos < text.length()) {
                                String plusSign = this.symbols.getPlusSignString();
                                String minusSign = this.symbols.getMinusSignString();
                                if (text.regionMatches(pos, plusSign, 0, plusSign.length())) {
                                    pos += plusSign.length();
                                } else {
                                    if (text.regionMatches(pos, minusSign, 0, minusSign.length())) {
                                        pos += minusSign.length();
                                        negExp = true;
                                    }
                                }
                            }
                            DigitList exponentDigits = new DigitList();
                            exponentDigits.count = 0;
                            while (pos < text.length()) {
                                int digitMatchLen = matchesDigit(text, pos, parsedDigit);
                                if (digitMatchLen <= 0) {
                                    break;
                                }
                                exponentDigits.append((char) (parsedDigit[0] + 48));
                                pos += digitMatchLen;
                            }
                            if (exponentDigits.count > 0) {
                                if (strictParse && sawGrouping) {
                                    strictFail = true;
                                } else {
                                    if (exponentDigits.count <= 10) {
                                        exponentDigits.decimalAt = exponentDigits.count;
                                        exponent = exponentDigits.getLong();
                                        if (negExp) {
                                            exponent = -exponent;
                                        }
                                    } else if (negExp) {
                                        status[2] = true;
                                    } else {
                                        status[0] = true;
                                    }
                                    position = pos;
                                }
                            }
                        }
                    }
                } else {
                    if (backup != -1) {
                        if (strictParse && ((lastGroup != -1 && groupedDigitCount != gs22) || (lastGroup == -1 && groupedDigitCount > gs22))) {
                            strictFail = true;
                            break;
                        }
                        lastGroup = backup;
                        groupedDigitCount = 0;
                    }
                    groupedDigitCount++;
                    position += matchLen;
                    backup = -1;
                    sawDigit = true;
                    if (parsedDigit[0] != 0 || digits.count != 0) {
                        digitCount++;
                        digits.append((char) (parsedDigit[0] + 48));
                    } else if (sawDecimal) {
                        digits.decimalAt--;
                    }
                }
            }
            if (digits.decimalAt == 0 && isDecimalPatternMatchRequired() && this.formatPattern.indexOf(decimal) != -1) {
                parsePosition.setIndex(oldStart);
                parsePosition.setErrorIndex(position);
                return false;
            }
            if (backup != -1) {
                position = backup;
            }
            if (!sawDecimal) {
                digits.decimalAt = digitCount;
            }
            if (!(!strictParse || (sawDecimal ^ 1) == 0 || lastGroup == -1 || groupedDigitCount == this.groupingSize)) {
                strictFail = true;
            }
            if (strictFail) {
                parsePosition.setIndex(oldStart);
                parsePosition.setErrorIndex(position);
                return false;
            }
            exponent += (long) digits.decimalAt;
            if (exponent < ((long) (-getParseMaxDigits()))) {
                status[2] = true;
            } else if (exponent > ((long) getParseMaxDigits())) {
                status[0] = true;
            } else {
                digits.decimalAt = (int) exponent;
            }
            if (!sawDigit && digitCount == 0) {
                parsePosition.setIndex(oldStart);
                parsePosition.setErrorIndex(oldStart);
                return false;
            }
        }
        if (this.formatWidth > 0 && this.padPosition == 2) {
            position = skipPadding(text, position);
        }
        if (posMatch >= 0) {
            posMatch = compareAffix(text, position, false, false, posSuffix, parseComplexCurrency, type, currency);
        }
        if (negMatch >= 0) {
            negMatch = compareAffix(text, position, true, false, negSuffix, parseComplexCurrency, type, currency);
        }
        if (posMatch >= 0 && negMatch >= 0) {
            if (posMatch > negMatch) {
                negMatch = -1;
            } else if (negMatch > posMatch) {
                posMatch = -1;
            }
        }
        if ((posMatch >= 0 ? 1 : null) == (negMatch >= 0 ? 1 : null)) {
            parsePosition.setErrorIndex(position);
            return false;
        }
        if (posMatch >= 0) {
            negMatch = posMatch;
        }
        position += negMatch;
        if (this.formatWidth > 0 && this.padPosition == 3) {
            position = skipPadding(text, position);
        }
        parsePosition.setIndex(position);
        status[1] = posMatch >= 0;
        if (parsePosition.getIndex() != oldStart) {
            return true;
        }
        parsePosition.setErrorIndex(position);
        return false;
    }

    private int matchesDigit(String str, int start, int[] decVal) {
        String[] localeDigits = this.symbols.getDigitStringsLocal();
        for (int i = 0; i < 10; i++) {
            int digitStrLen = localeDigits[i].length();
            if (str.regionMatches(start, localeDigits[i], 0, digitStrLen)) {
                decVal[0] = i;
                return digitStrLen;
            }
        }
        int cp = str.codePointAt(start);
        decVal[0] = UCharacter.digit(cp, 10);
        if (decVal[0] >= 0) {
            return Character.charCount(cp);
        }
        return 0;
    }

    private UnicodeSet getEquivalentDecimals(String decimal, boolean strictParse) {
        UnicodeSet equivSet = UnicodeSet.EMPTY;
        if (strictParse) {
            if (strictDotEquivalents.contains((CharSequence) decimal)) {
                return strictDotEquivalents;
            }
            if (strictCommaEquivalents.contains((CharSequence) decimal)) {
                return strictCommaEquivalents;
            }
            return equivSet;
        } else if (dotEquivalents.contains((CharSequence) decimal)) {
            return dotEquivalents;
        } else {
            if (commaEquivalents.contains((CharSequence) decimal)) {
                return commaEquivalents;
            }
            return equivSet;
        }
    }

    private final int skipPadding(String text, int position) {
        while (position < text.length() && text.charAt(position) == this.pad) {
            position++;
        }
        return position;
    }

    private int compareAffix(String text, int pos, boolean isNegative, boolean isPrefix, String affixPat, boolean complexCurrencyParsing, int type, Currency[] currency) {
        if (currency != null || this.currencyChoice != null || (this.currencySignCount != 0 && complexCurrencyParsing)) {
            return compareComplexAffix(affixPat, text, pos, type, currency);
        }
        if (isPrefix) {
            return compareSimpleAffix(isNegative ? this.negativePrefix : this.positivePrefix, text, pos);
        }
        return compareSimpleAffix(isNegative ? this.negativeSuffix : this.positiveSuffix, text, pos);
    }

    private static boolean isBidiMark(int c) {
        return c == 8206 || c == 8207 || c == 1564;
    }

    private static String trimMarksFromAffix(String affix) {
        boolean hasBidiMark = false;
        int idx = 0;
        while (idx < affix.length()) {
            if (isBidiMark(affix.charAt(idx))) {
                hasBidiMark = true;
                break;
            }
            idx++;
        }
        if (!hasBidiMark) {
            return affix;
        }
        StringBuilder buf = new StringBuilder();
        buf.append(affix, 0, idx);
        while (true) {
            idx++;
            if (idx >= affix.length()) {
                return buf.toString();
            }
            char c = affix.charAt(idx);
            if (!isBidiMark(c)) {
                buf.append(c);
            }
        }
    }

    private static int compareSimpleAffix(String affix, String input, int pos) {
        int start = pos;
        String trimmedAffix = affix.length() > 1 ? trimMarksFromAffix(affix) : affix;
        int i = 0;
        while (i < trimmedAffix.length()) {
            int c = UTF16.charAt(trimmedAffix, i);
            int len = UTF16.getCharCount(c);
            int ic;
            if (PatternProps.isWhiteSpace(c)) {
                boolean literalMatch = false;
                while (pos < input.length()) {
                    ic = UTF16.charAt(input, pos);
                    if (ic != c) {
                        if (!isBidiMark(ic)) {
                            break;
                        }
                        pos++;
                    } else {
                        literalMatch = true;
                        i += len;
                        pos += len;
                        if (i == trimmedAffix.length()) {
                            break;
                        }
                        c = UTF16.charAt(trimmedAffix, i);
                        len = UTF16.getCharCount(c);
                        if (!PatternProps.isWhiteSpace(c)) {
                            break;
                        }
                    }
                }
                i = skipPatternWhiteSpace(trimmedAffix, i);
                int s = pos;
                pos = skipUWhiteSpace(input, pos);
                if (pos == s && (literalMatch ^ 1) != 0) {
                    return -1;
                }
                i = skipUWhiteSpace(trimmedAffix, i);
            } else {
                boolean match = false;
                while (pos < input.length()) {
                    ic = UTF16.charAt(input, pos);
                    if (match || !equalWithSignCompatibility(ic, c)) {
                        if (!isBidiMark(ic)) {
                            break;
                        }
                        pos++;
                    } else {
                        i += len;
                        pos += len;
                        match = true;
                    }
                }
                if (!match) {
                    return -1;
                }
            }
        }
        return pos - start;
    }

    private static boolean equalWithSignCompatibility(int lhs, int rhs) {
        if (lhs == rhs || (minusSigns.contains(lhs) && minusSigns.contains(rhs))) {
            return true;
        }
        return plusSigns.contains(lhs) ? plusSigns.contains(rhs) : false;
    }

    private static int skipPatternWhiteSpace(String text, int pos) {
        while (pos < text.length()) {
            int c = UTF16.charAt(text, pos);
            if (!PatternProps.isWhiteSpace(c)) {
                break;
            }
            pos += UTF16.getCharCount(c);
        }
        return pos;
    }

    private static int skipUWhiteSpace(String text, int pos) {
        while (pos < text.length()) {
            int c = UTF16.charAt(text, pos);
            if (!UCharacter.isUWhiteSpace(c)) {
                break;
            }
            pos += UTF16.getCharCount(c);
        }
        return pos;
    }

    private static int skipBidiMarks(String text, int pos) {
        while (pos < text.length()) {
            int c = UTF16.charAt(text, pos);
            if (!isBidiMark(c)) {
                break;
            }
            pos += UTF16.getCharCount(c);
        }
        return pos;
    }

    private int compareComplexAffix(String affixPat, String text, int pos, int type, Currency[] currency) {
        int start = pos;
        int i = 0;
        while (i < affixPat.length() && pos >= 0) {
            int i2 = i + 1;
            int c = affixPat.charAt(i);
            if (c == '\'') {
                i = i2;
                while (true) {
                    int j = affixPat.indexOf(39, i);
                    if (j == i) {
                        pos = match(text, pos, 39);
                        i = j + 1;
                    } else if (j > i) {
                        pos = match(text, pos, affixPat.substring(i, j));
                        i = j + 1;
                        if (i < affixPat.length() && affixPat.charAt(i) == '\'') {
                            pos = match(text, pos, 39);
                            i++;
                        }
                    } else {
                        throw new RuntimeException();
                    }
                }
            }
            String affix = null;
            switch (c) {
                case 37:
                    affix = this.symbols.getPercentString();
                    break;
                case 43:
                    affix = this.symbols.getPlusSignString();
                    break;
                case 45:
                    affix = this.symbols.getMinusSignString();
                    break;
                case 164:
                    boolean intl = i2 < affixPat.length() && affixPat.charAt(i2) == CURRENCY_SIGN;
                    if (intl) {
                        i = i2 + 1;
                    } else {
                        i = i2;
                    }
                    boolean plural = i < affixPat.length() && affixPat.charAt(i) == CURRENCY_SIGN;
                    if (plural) {
                        i++;
                    }
                    ULocale uloc = getLocale(ULocale.VALID_LOCALE);
                    if (uloc == null) {
                        uloc = this.symbols.getLocale(ULocale.VALID_LOCALE);
                    }
                    ParsePosition ppos = new ParsePosition(pos);
                    String iso = Currency.parse(uloc, text, type, ppos);
                    if (iso == null) {
                        pos = -1;
                        break;
                    }
                    if (currency == null) {
                        if (iso.compareTo(getEffectiveCurrency().getCurrencyCode()) != 0) {
                            pos = -1;
                            break;
                        }
                    }
                    currency[0] = Currency.getInstance(iso);
                    pos = ppos.getIndex();
                    continue;
                case 8240:
                    affix = this.symbols.getPerMillString();
                    break;
            }
            if (affix != null) {
                pos = match(text, pos, affix);
                i = i2;
            } else {
                pos = match(text, pos, c);
                i = PatternProps.isWhiteSpace(c) ? skipPatternWhiteSpace(affixPat, i2) : i2;
            }
        }
        return pos - start;
    }

    static final int match(String text, int pos, int ch) {
        if (pos < 0 || pos >= text.length()) {
            return -1;
        }
        pos = skipBidiMarks(text, pos);
        if (PatternProps.isWhiteSpace(ch)) {
            int s = pos;
            int pos2 = skipPatternWhiteSpace(text, pos);
            if (pos2 == pos) {
                return -1;
            }
            return pos2;
        } else if (pos >= text.length() || UTF16.charAt(text, pos) != ch) {
            return -1;
        } else {
            return skipBidiMarks(text, UTF16.getCharCount(ch) + pos);
        }
    }

    static final int match(String text, int pos, String str) {
        int i = 0;
        while (i < str.length() && pos >= 0) {
            int ch = UTF16.charAt(str, i);
            i += UTF16.getCharCount(ch);
            if (!isBidiMark(ch)) {
                pos = match(text, pos, ch);
                if (PatternProps.isWhiteSpace(ch)) {
                    i = skipPatternWhiteSpace(str, i);
                }
            }
        }
        return pos;
    }

    public DecimalFormatSymbols getDecimalFormatSymbols() {
        try {
            return (DecimalFormatSymbols) this.symbols.clone();
        } catch (Exception e) {
            return null;
        }
    }

    public void setDecimalFormatSymbols(DecimalFormatSymbols newSymbols) {
        this.symbols = (DecimalFormatSymbols) newSymbols.clone();
        setCurrencyForSymbols();
        expandAffixes(null);
    }

    private void setCurrencyForSymbols() {
        DecimalFormatSymbols def = new DecimalFormatSymbols(this.symbols.getULocale());
        if (this.symbols.getCurrencySymbol().equals(def.getCurrencySymbol()) && this.symbols.getInternationalCurrencySymbol().equals(def.getInternationalCurrencySymbol())) {
            setCurrency(Currency.getInstance(this.symbols.getULocale()));
        } else {
            setCurrency(null);
        }
    }

    public String getPositivePrefix() {
        return this.positivePrefix;
    }

    public void setPositivePrefix(String newValue) {
        this.positivePrefix = newValue;
        this.posPrefixPattern = null;
    }

    public String getNegativePrefix() {
        return this.negativePrefix;
    }

    public void setNegativePrefix(String newValue) {
        this.negativePrefix = newValue;
        this.negPrefixPattern = null;
    }

    public String getPositiveSuffix() {
        return this.positiveSuffix;
    }

    public void setPositiveSuffix(String newValue) {
        this.positiveSuffix = newValue;
        this.posSuffixPattern = null;
    }

    public String getNegativeSuffix() {
        return this.negativeSuffix;
    }

    public void setNegativeSuffix(String newValue) {
        this.negativeSuffix = newValue;
        this.negSuffixPattern = null;
    }

    public int getMultiplier() {
        return this.multiplier;
    }

    public void setMultiplier(int newValue) {
        if (newValue == 0) {
            throw new IllegalArgumentException("Bad multiplier: " + newValue);
        }
        this.multiplier = newValue;
    }

    public BigDecimal getRoundingIncrement() {
        if (this.roundingIncrementICU == null) {
            return null;
        }
        return this.roundingIncrementICU.toBigDecimal();
    }

    public void setRoundingIncrement(BigDecimal newValue) {
        if (newValue == null) {
            setRoundingIncrement((android.icu.math.BigDecimal) null);
        } else {
            setRoundingIncrement(new android.icu.math.BigDecimal(newValue));
        }
    }

    public void setRoundingIncrement(android.icu.math.BigDecimal newValue) {
        int i = newValue == null ? 0 : newValue.compareTo(android.icu.math.BigDecimal.ZERO);
        if (i < 0) {
            throw new IllegalArgumentException("Illegal rounding increment");
        }
        if (i == 0) {
            setInternalRoundingIncrement(null);
        } else {
            setInternalRoundingIncrement(newValue);
        }
        resetActualRounding();
    }

    public void setRoundingIncrement(double newValue) {
        if (newValue < 0.0d) {
            throw new IllegalArgumentException("Illegal rounding increment");
        }
        if (newValue == 0.0d) {
            setInternalRoundingIncrement((android.icu.math.BigDecimal) null);
        } else {
            setInternalRoundingIncrement(android.icu.math.BigDecimal.valueOf(newValue));
        }
        resetActualRounding();
    }

    public int getRoundingMode() {
        return this.roundingMode;
    }

    public void setRoundingMode(int roundingMode) {
        if (roundingMode < 0 || roundingMode > 7) {
            throw new IllegalArgumentException("Invalid rounding mode: " + roundingMode);
        }
        this.roundingMode = roundingMode;
        resetActualRounding();
    }

    public int getFormatWidth() {
        return this.formatWidth;
    }

    public void setFormatWidth(int width) {
        if (width < 0) {
            throw new IllegalArgumentException("Illegal format width");
        }
        this.formatWidth = width;
    }

    public char getPadCharacter() {
        return this.pad;
    }

    public void setPadCharacter(char padChar) {
        this.pad = padChar;
    }

    public int getPadPosition() {
        return this.padPosition;
    }

    public void setPadPosition(int padPos) {
        if (padPos < 0 || padPos > 3) {
            throw new IllegalArgumentException("Illegal pad position");
        }
        this.padPosition = padPos;
    }

    public boolean isScientificNotation() {
        return this.useExponentialNotation;
    }

    public void setScientificNotation(boolean useScientific) {
        this.useExponentialNotation = useScientific;
    }

    public byte getMinimumExponentDigits() {
        return this.minExponentDigits;
    }

    public void setMinimumExponentDigits(byte minExpDig) {
        if (minExpDig < (byte) 1) {
            throw new IllegalArgumentException("Exponent digits must be >= 1");
        }
        this.minExponentDigits = minExpDig;
    }

    public boolean isExponentSignAlwaysShown() {
        return this.exponentSignAlwaysShown;
    }

    public void setExponentSignAlwaysShown(boolean expSignAlways) {
        this.exponentSignAlwaysShown = expSignAlways;
    }

    public int getGroupingSize() {
        return this.groupingSize;
    }

    public void setGroupingSize(int newValue) {
        this.groupingSize = (byte) newValue;
    }

    public int getSecondaryGroupingSize() {
        return this.groupingSize2;
    }

    public void setSecondaryGroupingSize(int newValue) {
        this.groupingSize2 = (byte) newValue;
    }

    public MathContext getMathContextICU() {
        return this.mathContext;
    }

    public java.math.MathContext getMathContext() {
        java.math.MathContext mathContext = null;
        try {
            if (this.mathContext != null) {
                mathContext = new java.math.MathContext(this.mathContext.getDigits(), RoundingMode.valueOf(this.mathContext.getRoundingMode()));
            }
            return mathContext;
        } catch (Exception e) {
            return null;
        }
    }

    public void setMathContextICU(MathContext newValue) {
        this.mathContext = newValue;
    }

    public void setMathContext(java.math.MathContext newValue) {
        this.mathContext = new MathContext(newValue.getPrecision(), 1, false, newValue.getRoundingMode().ordinal());
    }

    public boolean isDecimalSeparatorAlwaysShown() {
        return this.decimalSeparatorAlwaysShown;
    }

    public void setDecimalPatternMatchRequired(boolean value) {
        this.parseRequireDecimalPoint = value;
    }

    public boolean isDecimalPatternMatchRequired() {
        return this.parseRequireDecimalPoint;
    }

    public void setDecimalSeparatorAlwaysShown(boolean newValue) {
        this.decimalSeparatorAlwaysShown = newValue;
    }

    public CurrencyPluralInfo getCurrencyPluralInfo() {
        try {
            CurrencyPluralInfo currencyPluralInfo;
            if (this.currencyPluralInfo == null) {
                currencyPluralInfo = null;
            } else {
                currencyPluralInfo = (CurrencyPluralInfo) this.currencyPluralInfo.clone();
            }
            return currencyPluralInfo;
        } catch (Exception e) {
            return null;
        }
    }

    public void setCurrencyPluralInfo(CurrencyPluralInfo newInfo) {
        this.currencyPluralInfo = (CurrencyPluralInfo) newInfo.clone();
        this.isReadyForParsing = false;
    }

    public Object clone() {
        try {
            DecimalFormat other = (DecimalFormat) super.clone();
            other.symbols = (DecimalFormatSymbols) this.symbols.clone();
            other.digitList = new DigitList();
            if (this.currencyPluralInfo != null) {
                other.currencyPluralInfo = (CurrencyPluralInfo) this.currencyPluralInfo.clone();
            }
            other.attributes = new ArrayList();
            other.currencyUsage = this.currencyUsage;
            return other;
        } catch (Exception e) {
            throw new IllegalStateException();
        }
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (obj == null || !super.equals(obj)) {
            return false;
        }
        DecimalFormat other = (DecimalFormat) obj;
        if (this.currencySignCount == other.currencySignCount && ((this.style != 6 || (equals(this.posPrefixPattern, other.posPrefixPattern) && equals(this.posSuffixPattern, other.posSuffixPattern) && equals(this.negPrefixPattern, other.negPrefixPattern) && equals(this.negSuffixPattern, other.negSuffixPattern))) && this.multiplier == other.multiplier && this.groupingSize == other.groupingSize && this.groupingSize2 == other.groupingSize2 && this.decimalSeparatorAlwaysShown == other.decimalSeparatorAlwaysShown && this.useExponentialNotation == other.useExponentialNotation && ((!this.useExponentialNotation || this.minExponentDigits == other.minExponentDigits) && this.useSignificantDigits == other.useSignificantDigits && ((!this.useSignificantDigits || (this.minSignificantDigits == other.minSignificantDigits && this.maxSignificantDigits == other.maxSignificantDigits)) && this.symbols.equals(other.symbols) && Utility.objectEquals(this.currencyPluralInfo, other.currencyPluralInfo))))) {
            z = this.currencyUsage.equals(other.currencyUsage);
        }
        return z;
    }

    private boolean equals(String pat1, String pat2) {
        boolean z = true;
        if (pat1 == null || pat2 == null) {
            if (!(pat1 == null && pat2 == null)) {
                z = false;
            }
            return z;
        } else if (pat1.equals(pat2)) {
            return true;
        } else {
            return unquote(pat1).equals(unquote(pat2));
        }
    }

    private String unquote(String pat) {
        StringBuilder buf = new StringBuilder(pat.length());
        int i = 0;
        while (i < pat.length()) {
            int i2 = i + 1;
            char ch = pat.charAt(i);
            if (ch != '\'') {
                buf.append(ch);
            }
            i = i2;
        }
        return buf.toString();
    }

    public int hashCode() {
        return (super.hashCode() * 37) + this.positivePrefix.hashCode();
    }

    public String toPattern() {
        if (this.style == 6) {
            return this.formatPattern;
        }
        return toPattern(false);
    }

    public String toLocalizedPattern() {
        if (this.style == 6) {
            return this.formatPattern;
        }
        return toPattern(true);
    }

    private void expandAffixes(String pluralCount) {
        this.currencyChoice = null;
        StringBuffer buffer = new StringBuffer();
        if (this.posPrefixPattern != null) {
            expandAffix(this.posPrefixPattern, pluralCount, buffer);
            this.positivePrefix = buffer.toString();
        }
        if (this.posSuffixPattern != null) {
            expandAffix(this.posSuffixPattern, pluralCount, buffer);
            this.positiveSuffix = buffer.toString();
        }
        if (this.negPrefixPattern != null) {
            expandAffix(this.negPrefixPattern, pluralCount, buffer);
            this.negativePrefix = buffer.toString();
        }
        if (this.negSuffixPattern != null) {
            expandAffix(this.negSuffixPattern, pluralCount, buffer);
            this.negativeSuffix = buffer.toString();
        }
    }

    private void expandAffix(String pattern, String pluralCount, StringBuffer buffer) {
        buffer.setLength(0);
        int i = 0;
        while (i < pattern.length()) {
            int i2 = i + 1;
            char c = pattern.charAt(i);
            if (c != '\'') {
                switch (c) {
                    case '%':
                        buffer.append(this.symbols.getPercentString());
                        i = i2;
                        break;
                    case '-':
                        buffer.append(this.symbols.getMinusSignString());
                        i = i2;
                        break;
                    case 164:
                        String s;
                        boolean intl = i2 < pattern.length() && pattern.charAt(i2) == CURRENCY_SIGN;
                        boolean plural = false;
                        if (intl) {
                            i = i2 + 1;
                            if (i < pattern.length() && pattern.charAt(i) == CURRENCY_SIGN) {
                                plural = true;
                                intl = false;
                                i++;
                            }
                        } else {
                            i = i2;
                        }
                        Currency currency = getCurrency();
                        if (currency != null) {
                            if (plural && pluralCount != null) {
                                s = currency.getName(this.symbols.getULocale(), 2, pluralCount, null);
                            } else if (intl) {
                                s = currency.getCurrencyCode();
                            } else {
                                s = currency.getName(this.symbols.getULocale(), 0, null);
                            }
                        } else if (intl) {
                            s = this.symbols.getInternationalCurrencySymbol();
                        } else {
                            s = this.symbols.getCurrencySymbol();
                        }
                        buffer.append(s);
                        break;
                    case 8240:
                        buffer.append(this.symbols.getPerMillString());
                        i = i2;
                        break;
                    default:
                        buffer.append(c);
                        i = i2;
                        break;
                }
            }
            i = i2;
            while (true) {
                int j = pattern.indexOf(39, i);
                if (j == i) {
                    buffer.append('\'');
                    i = j + 1;
                } else if (j > i) {
                    buffer.append(pattern.substring(i, j));
                    i = j + 1;
                    if (i < pattern.length() && pattern.charAt(i) == '\'') {
                        buffer.append('\'');
                        i++;
                    }
                } else {
                    throw new RuntimeException();
                }
            }
        }
    }

    private int appendAffix(StringBuffer buf, boolean isNegative, boolean isPrefix, FieldPosition fieldPosition, boolean parseAttr) {
        if (this.currencyChoice != null) {
            String affixPat = isPrefix ? isNegative ? this.negPrefixPattern : this.posPrefixPattern : isNegative ? this.negSuffixPattern : this.posSuffixPattern;
            StringBuffer affixBuf = new StringBuffer();
            expandAffix(affixPat, null, affixBuf);
            buf.append(affixBuf);
            return affixBuf.length();
        }
        String affix;
        String pattern;
        if (isPrefix) {
            affix = isNegative ? this.negativePrefix : this.positivePrefix;
            pattern = isNegative ? this.negPrefixPattern : this.posPrefixPattern;
        } else {
            affix = isNegative ? this.negativeSuffix : this.positiveSuffix;
            pattern = isNegative ? this.negSuffixPattern : this.posSuffixPattern;
        }
        if (parseAttr) {
            int offset = affix.indexOf(this.symbols.getCurrencySymbol());
            if (offset > -1) {
                formatAffix2Attribute(isPrefix, Field.CURRENCY, buf, offset, this.symbols.getCurrencySymbol().length());
            }
            offset = affix.indexOf(this.symbols.getMinusSignString());
            if (offset > -1) {
                formatAffix2Attribute(isPrefix, Field.SIGN, buf, offset, this.symbols.getMinusSignString().length());
            }
            offset = affix.indexOf(this.symbols.getPercentString());
            if (offset > -1) {
                formatAffix2Attribute(isPrefix, Field.PERCENT, buf, offset, this.symbols.getPercentString().length());
            }
            offset = affix.indexOf(this.symbols.getPerMillString());
            if (offset > -1) {
                formatAffix2Attribute(isPrefix, Field.PERMILLE, buf, offset, this.symbols.getPerMillString().length());
            }
            offset = pattern.indexOf("¤¤¤");
            if (offset > -1) {
                formatAffix2Attribute(isPrefix, Field.CURRENCY, buf, offset, affix.length() - offset);
            }
        }
        int firstPos;
        int startPos;
        if (fieldPosition.getFieldAttribute() == Field.SIGN) {
            String sign = isNegative ? this.symbols.getMinusSignString() : this.symbols.getPlusSignString();
            firstPos = affix.indexOf(sign);
            if (firstPos > -1) {
                startPos = buf.length() + firstPos;
                fieldPosition.setBeginIndex(startPos);
                fieldPosition.setEndIndex(sign.length() + startPos);
            }
        } else if (fieldPosition.getFieldAttribute() == Field.PERCENT) {
            firstPos = affix.indexOf(this.symbols.getPercentString());
            if (firstPos > -1) {
                startPos = buf.length() + firstPos;
                fieldPosition.setBeginIndex(startPos);
                fieldPosition.setEndIndex(this.symbols.getPercentString().length() + startPos);
            }
        } else if (fieldPosition.getFieldAttribute() == Field.PERMILLE) {
            firstPos = affix.indexOf(this.symbols.getPerMillString());
            if (firstPos > -1) {
                startPos = buf.length() + firstPos;
                fieldPosition.setBeginIndex(startPos);
                fieldPosition.setEndIndex(this.symbols.getPerMillString().length() + startPos);
            }
        } else if (fieldPosition.getFieldAttribute() == Field.CURRENCY) {
            String aff;
            int start;
            int end;
            if (affix.indexOf(this.symbols.getCurrencySymbol()) > -1) {
                aff = this.symbols.getCurrencySymbol();
                start = buf.length() + affix.indexOf(aff);
                end = start + aff.length();
                fieldPosition.setBeginIndex(start);
                fieldPosition.setEndIndex(end);
            } else if (affix.indexOf(this.symbols.getInternationalCurrencySymbol()) > -1) {
                aff = this.symbols.getInternationalCurrencySymbol();
                start = buf.length() + affix.indexOf(aff);
                end = start + aff.length();
                fieldPosition.setBeginIndex(start);
                fieldPosition.setEndIndex(end);
            } else if (pattern.indexOf("¤¤¤") > -1) {
                end = buf.length() + affix.length();
                fieldPosition.setBeginIndex(buf.length() + pattern.indexOf("¤¤¤"));
                fieldPosition.setEndIndex(end);
            }
        }
        buf.append(affix);
        return affix.length();
    }

    private void formatAffix2Attribute(boolean isPrefix, Field fieldType, StringBuffer buf, int offset, int symbolSize) {
        int begin = offset;
        if (!isPrefix) {
            begin = offset + buf.length();
        }
        addAttribute(fieldType, begin, begin + symbolSize);
    }

    private void addAttribute(Field field, int begin, int end) {
        FieldPosition pos = new FieldPosition(field);
        pos.setBeginIndex(begin);
        pos.setEndIndex(end);
        this.attributes.add(pos);
    }

    public AttributedCharacterIterator formatToCharacterIterator(Object obj) {
        return formatToCharacterIterator(obj, NULL_UNIT);
    }

    AttributedCharacterIterator formatToCharacterIterator(Object obj, Unit unit) {
        if (obj instanceof Number) {
            Number number = (Number) obj;
            StringBuffer text = new StringBuffer();
            unit.writePrefix(text);
            this.attributes.clear();
            if (obj instanceof BigInteger) {
                format((BigInteger) number, text, new FieldPosition(0), true);
            } else if (obj instanceof BigDecimal) {
                format((BigDecimal) number, text, new FieldPosition(0), true);
            } else if (obj instanceof Double) {
                format(number.doubleValue(), text, new FieldPosition(0), true);
            } else if ((obj instanceof Integer) || (obj instanceof Long)) {
                format(number.longValue(), text, new FieldPosition(0), true);
            } else {
                throw new IllegalArgumentException();
            }
            unit.writeSuffix(text);
            AttributedString as = new AttributedString(text.toString());
            for (int i = 0; i < this.attributes.size(); i++) {
                FieldPosition pos = (FieldPosition) this.attributes.get(i);
                Format.Field attribute = pos.getFieldAttribute();
                as.addAttribute(attribute, attribute, pos.getBeginIndex(), pos.getEndIndex());
            }
            return as.getIterator();
        }
        throw new IllegalArgumentException();
    }

    private void appendAffixPattern(StringBuffer buffer, boolean isNegative, boolean isPrefix, boolean localized) {
        String affixPat = isPrefix ? isNegative ? this.negPrefixPattern : this.posPrefixPattern : isNegative ? this.negSuffixPattern : this.posSuffixPattern;
        int i;
        char ch;
        if (affixPat == null) {
            String affix = isPrefix ? isNegative ? this.negativePrefix : this.positivePrefix : isNegative ? this.negativeSuffix : this.positiveSuffix;
            buffer.append('\'');
            for (i = 0; i < affix.length(); i++) {
                ch = affix.charAt(i);
                if (ch == '\'') {
                    buffer.append(ch);
                }
                buffer.append(ch);
            }
            buffer.append('\'');
            return;
        }
        if (localized) {
            i = 0;
            while (i < affixPat.length()) {
                ch = affixPat.charAt(i);
                switch (ch) {
                    case '%':
                        ch = this.symbols.getPercent();
                        break;
                    case '\'':
                        int j = affixPat.indexOf(39, i + 1);
                        if (j < 0) {
                            throw new IllegalArgumentException("Malformed affix pattern: " + affixPat);
                        }
                        buffer.append(affixPat.substring(i, j + 1));
                        i = j;
                        continue;
                    case '-':
                        ch = this.symbols.getMinusSign();
                        break;
                    case 8240:
                        ch = this.symbols.getPerMill();
                        break;
                }
                if (ch == this.symbols.getDecimalSeparator() || ch == this.symbols.getGroupingSeparator()) {
                    buffer.append('\'');
                    buffer.append(ch);
                    buffer.append('\'');
                    i++;
                } else {
                    buffer.append(ch);
                    i++;
                }
            }
        } else {
            buffer.append(affixPat);
        }
    }

    private String toPattern(boolean localized) {
        char padEscape;
        String padSpec;
        int i;
        StringBuffer result = new StringBuffer();
        char zero = localized ? this.symbols.getZeroDigit() : PATTERN_ZERO_DIGIT;
        char digit = localized ? this.symbols.getDigit() : PATTERN_DIGIT;
        char sigDigit = 0;
        boolean useSigDig = areSignificantDigitsUsed();
        if (useSigDig) {
            sigDigit = localized ? this.symbols.getSignificantDigit() : PATTERN_SIGNIFICANT_DIGIT;
        }
        char group = localized ? this.symbols.getGroupingSeparator() : PATTERN_GROUPING_SEPARATOR;
        int roundingDecimalPos = 0;
        String roundingDigits = null;
        int padPos = this.formatWidth > 0 ? this.padPosition : -1;
        if (this.formatWidth > 0) {
            StringBuffer stringBuffer = new StringBuffer(2);
            if (localized) {
                padEscape = this.symbols.getPadEscape();
            } else {
                padEscape = PATTERN_PAD_ESCAPE;
            }
            padSpec = stringBuffer.append(padEscape).append(this.pad).toString();
        } else {
            padSpec = null;
        }
        if (this.roundingIncrementICU != null) {
            i = this.roundingIncrementICU.scale();
            roundingDigits = this.roundingIncrementICU.movePointRight(i).toString();
            roundingDecimalPos = roundingDigits.length() - i;
        }
        int part = 0;
        while (part < 2) {
            int minDig;
            int maxDig;
            int pos;
            if (padPos == 0) {
                result.append(padSpec);
            }
            appendAffixPattern(result, part != 0, true, localized);
            if (padPos == 1) {
                result.append(padSpec);
            }
            int sub0Start = result.length();
            int g = isGroupingUsed() ? Math.max(0, this.groupingSize) : 0;
            if (g > 0 && this.groupingSize2 > (byte) 0 && this.groupingSize2 != this.groupingSize) {
                g += this.groupingSize2;
            }
            int maxSigDig = 0;
            if (useSigDig) {
                minDig = getMinimumSignificantDigits();
                maxSigDig = getMaximumSignificantDigits();
                maxDig = maxSigDig;
            } else {
                minDig = getMinimumIntegerDigits();
                maxDig = getMaximumIntegerDigits();
            }
            if (this.useExponentialNotation) {
                if (maxDig > 8) {
                    maxDig = 1;
                }
            } else if (useSigDig) {
                maxDig = Math.max(maxDig, g + 1);
            } else {
                maxDig = Math.max(Math.max(g, getMinimumIntegerDigits()), roundingDecimalPos) + 1;
            }
            i = maxDig;
            while (i > 0) {
                if (!this.useExponentialNotation && i < maxDig && isGroupingPosition(i)) {
                    result.append(group);
                }
                if (useSigDig) {
                    if (maxSigDig < i || i <= maxSigDig - minDig) {
                        padEscape = digit;
                    } else {
                        padEscape = sigDigit;
                    }
                    result.append(padEscape);
                } else {
                    if (roundingDigits != null) {
                        pos = roundingDecimalPos - i;
                        if (pos >= 0 && pos < roundingDigits.length()) {
                            result.append((char) ((roundingDigits.charAt(pos) - 48) + zero));
                        }
                    }
                    result.append(i <= minDig ? zero : digit);
                }
                i--;
            }
            if (!useSigDig) {
                if (getMaximumFractionDigits() > 0 || this.decimalSeparatorAlwaysShown) {
                    if (localized) {
                        padEscape = this.symbols.getDecimalSeparator();
                    } else {
                        padEscape = PATTERN_DECIMAL_SEPARATOR;
                    }
                    result.append(padEscape);
                }
                pos = roundingDecimalPos;
                i = 0;
                while (i < getMaximumFractionDigits()) {
                    if (roundingDigits == null || pos >= roundingDigits.length()) {
                        result.append(i < getMinimumFractionDigits() ? zero : digit);
                    } else {
                        if (pos < 0) {
                            padEscape = zero;
                        } else {
                            padEscape = (char) ((roundingDigits.charAt(pos) - 48) + zero);
                        }
                        result.append(padEscape);
                        pos++;
                    }
                    i++;
                }
            }
            if (this.useExponentialNotation) {
                if (localized) {
                    result.append(this.symbols.getExponentSeparator());
                } else {
                    result.append(PATTERN_EXPONENT);
                }
                if (this.exponentSignAlwaysShown) {
                    result.append(localized ? this.symbols.getPlusSign() : PATTERN_PLUS_SIGN);
                }
                for (byte i2 = (byte) 0; i2 < this.minExponentDigits; i2++) {
                    result.append(zero);
                }
            }
            if (!(padSpec == null || (this.useExponentialNotation ^ 1) == 0)) {
                int length;
                int length2 = (this.formatWidth - result.length()) + sub0Start;
                if (part == 0) {
                    length = this.positivePrefix.length() + this.positiveSuffix.length();
                } else {
                    length = this.negativePrefix.length() + this.negativeSuffix.length();
                }
                int add = length2 - length;
                while (add > 0) {
                    result.insert(sub0Start, digit);
                    maxDig++;
                    add--;
                    if (add > 1 && isGroupingPosition(maxDig)) {
                        result.insert(sub0Start, group);
                        add--;
                    }
                }
            }
            if (padPos == 2) {
                result.append(padSpec);
            }
            appendAffixPattern(result, part != 0, false, localized);
            if (padPos == 3) {
                result.append(padSpec);
            }
            if (part == 0) {
                if (this.negativeSuffix.equals(this.positiveSuffix) && this.negativePrefix.equals(PATTERN_MINUS_SIGN + this.positivePrefix)) {
                    break;
                }
                if (localized) {
                    padEscape = this.symbols.getPatternSeparator();
                } else {
                    padEscape = PATTERN_SEPARATOR;
                }
                result.append(padEscape);
            }
            part++;
        }
        return result.toString();
    }

    public void applyPattern(String pattern) {
        applyPattern(pattern, false);
    }

    public void applyLocalizedPattern(String pattern) {
        applyPattern(pattern, true);
    }

    private void applyPattern(String pattern, boolean localized) {
        applyPatternWithoutExpandAffix(pattern, localized);
        expandAffixAdjustWidth(null);
    }

    private void expandAffixAdjustWidth(String pluralCount) {
        expandAffixes(pluralCount);
        if (this.formatWidth > 0) {
            this.formatWidth += this.positivePrefix.length() + this.positiveSuffix.length();
        }
    }

    /* JADX WARNING: Missing block: B:13:0x00fa, code:
            r42 = r42 + 1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void applyPatternWithoutExpandAffix(String pattern, boolean localized) {
        String stringBuilder;
        char zeroDigit = PATTERN_ZERO_DIGIT;
        char sigDigit = PATTERN_SIGNIFICANT_DIGIT;
        char groupingSeparator = PATTERN_GROUPING_SEPARATOR;
        char decimalSeparator = PATTERN_DECIMAL_SEPARATOR;
        char percent = PATTERN_PERCENT;
        char perMill = PATTERN_PER_MILLE;
        char digit = PATTERN_DIGIT;
        char separator = PATTERN_SEPARATOR;
        String exponent = String.valueOf(PATTERN_EXPONENT);
        char plus = PATTERN_PLUS_SIGN;
        char padEscape = PATTERN_PAD_ESCAPE;
        char minus = PATTERN_MINUS_SIGN;
        if (localized) {
            zeroDigit = this.symbols.getZeroDigit();
            sigDigit = this.symbols.getSignificantDigit();
            groupingSeparator = this.symbols.getGroupingSeparator();
            decimalSeparator = this.symbols.getDecimalSeparator();
            percent = this.symbols.getPercent();
            perMill = this.symbols.getPerMill();
            digit = this.symbols.getDigit();
            separator = this.symbols.getPatternSeparator();
            exponent = this.symbols.getExponentSeparator();
            plus = this.symbols.getPlusSign();
            padEscape = this.symbols.getPadEscape();
            minus = this.symbols.getMinusSign();
        }
        char nineDigit = (char) (zeroDigit + 9);
        boolean gotNegative = false;
        int pos = 0;
        int part = 0;
        while (part < 2 && pos < pattern.length()) {
            int subpart = 1;
            int sub0Start = 0;
            int sub0Limit = 0;
            int sub2Limit = 0;
            StringBuilder prefix = new StringBuilder();
            StringBuilder suffix = new StringBuilder();
            int decimalPos = -1;
            int multpl = 1;
            int digitLeftCount = 0;
            int zeroDigitCount = 0;
            int digitRightCount = 0;
            int sigDigitCount = 0;
            byte groupingCount = (byte) -1;
            byte groupingCount2 = (byte) -1;
            int padPos = -1;
            char padChar = 0;
            int incrementPos = -1;
            long incrementVal = 0;
            byte expDigits = (byte) -1;
            boolean expSignAlways = false;
            int currencySignCnt = 0;
            StringBuilder affix = prefix;
            int start = pos;
            while (pos < pattern.length()) {
                char ch = pattern.charAt(pos);
                switch (subpart) {
                    case 0:
                        if (ch != digit) {
                            if ((ch < zeroDigit || ch > nineDigit) && ch != sigDigit) {
                                if (ch != groupingSeparator) {
                                    if (ch != decimalSeparator) {
                                        if (pattern.regionMatches(pos, exponent, 0, exponent.length())) {
                                            if (expDigits >= (byte) 0) {
                                                patternError("Multiple exponential symbols", pattern);
                                            }
                                            if (groupingCount >= (byte) 0) {
                                                patternError("Grouping separator in exponential", pattern);
                                            }
                                            pos += exponent.length();
                                            if (pos < pattern.length() && pattern.charAt(pos) == plus) {
                                                expSignAlways = true;
                                                pos++;
                                            }
                                            expDigits = (byte) 0;
                                            while (pos < pattern.length() && pattern.charAt(pos) == zeroDigit) {
                                                expDigits = (byte) (expDigits + 1);
                                                pos++;
                                            }
                                            if ((digitLeftCount + zeroDigitCount < 1 && sigDigitCount + digitRightCount < 1) || ((sigDigitCount > 0 && digitLeftCount > 0) || expDigits < (byte) 1)) {
                                                patternError("Malformed exponential", pattern);
                                            }
                                        }
                                        subpart = 2;
                                        affix = suffix;
                                        sub0Limit = pos;
                                        pos--;
                                        break;
                                    }
                                    if (decimalPos >= 0) {
                                        patternError("Multiple decimal separators", pattern);
                                    }
                                    decimalPos = (digitLeftCount + zeroDigitCount) + digitRightCount;
                                    break;
                                }
                                if (ch == '\'' && pos + 1 < pattern.length()) {
                                    char after = pattern.charAt(pos + 1);
                                    if (after != digit && (after < zeroDigit || after > nineDigit)) {
                                        if (after != '\'') {
                                            if (groupingCount >= (byte) 0) {
                                                subpart = 2;
                                                affix = suffix;
                                                sub0Limit = pos;
                                                pos--;
                                                break;
                                            }
                                            subpart = 3;
                                            break;
                                        }
                                        pos++;
                                    }
                                }
                                if (decimalPos >= 0) {
                                    patternError("Grouping separator after decimal", pattern);
                                }
                                groupingCount2 = groupingCount;
                                groupingCount = (byte) 0;
                                break;
                            }
                            if (digitRightCount > 0) {
                                patternError("Unexpected '" + ch + '\'', pattern);
                            }
                            if (ch == sigDigit) {
                                sigDigitCount++;
                            } else {
                                zeroDigitCount++;
                                if (ch != zeroDigit) {
                                    int p = (digitLeftCount + zeroDigitCount) + digitRightCount;
                                    if (incrementPos >= 0) {
                                        while (incrementPos < p) {
                                            incrementVal *= 10;
                                            incrementPos++;
                                        }
                                    } else {
                                        incrementPos = p;
                                    }
                                    incrementVal += (long) (ch - zeroDigit);
                                }
                            }
                            if (groupingCount >= (byte) 0 && decimalPos < 0) {
                                groupingCount = (byte) (groupingCount + 1);
                                break;
                            }
                        }
                        if (zeroDigitCount > 0 || sigDigitCount > 0) {
                            digitRightCount++;
                        } else {
                            digitLeftCount++;
                        }
                        if (groupingCount >= (byte) 0 && decimalPos < 0) {
                            groupingCount = (byte) (groupingCount + 1);
                            break;
                        }
                        break;
                    case 1:
                    case 2:
                        if (ch != digit && ch != groupingSeparator && ch != decimalSeparator && ((ch < zeroDigit || ch > nineDigit) && ch != sigDigit)) {
                            if (ch != 164) {
                                if (ch != '\'') {
                                    if (ch != separator) {
                                        if (ch != percent && ch != perMill) {
                                            if (ch != minus) {
                                                if (ch == padEscape) {
                                                    if (padPos >= 0) {
                                                        patternError("Multiple pad specifiers", pattern);
                                                    }
                                                    if (pos + 1 == pattern.length()) {
                                                        patternError("Invalid pad specifier", pattern);
                                                    }
                                                    padPos = pos;
                                                    pos++;
                                                    padChar = pattern.charAt(pos);
                                                    break;
                                                }
                                            }
                                            ch = PATTERN_MINUS_SIGN;
                                        } else {
                                            if (multpl != 1) {
                                                patternError("Too many percent/permille characters", pattern);
                                            }
                                            multpl = ch == percent ? 100 : 1000;
                                            ch = ch == percent ? PATTERN_PERCENT : PATTERN_PER_MILLE;
                                        }
                                    } else {
                                        if (subpart == 1 || part == 1) {
                                            patternError("Unquoted special character '" + ch + '\'', pattern);
                                        }
                                        sub2Limit = pos;
                                        pos++;
                                        break;
                                    }
                                }
                                if (pos + 1 >= pattern.length() || pattern.charAt(pos + 1) != '\'') {
                                    subpart += 2;
                                } else {
                                    pos++;
                                    affix.append(ch);
                                }
                            } else {
                                boolean doubled = pos + 1 < pattern.length() ? pattern.charAt(pos + 1) == 164 : false;
                                if (doubled) {
                                    pos++;
                                    affix.append(ch);
                                    if (pos + 1 >= pattern.length() || pattern.charAt(pos + 1) != 164) {
                                        currencySignCnt = 2;
                                    } else {
                                        pos++;
                                        affix.append(ch);
                                        currencySignCnt = 3;
                                    }
                                } else {
                                    currencySignCnt = 1;
                                }
                            }
                        } else if (subpart == 1) {
                            subpart = 0;
                            sub0Start = pos;
                            pos--;
                            break;
                        } else if (ch == '\'') {
                            if (pos + 1 < pattern.length() && pattern.charAt(pos + 1) == '\'') {
                                pos++;
                                affix.append(ch);
                                break;
                            }
                            subpart += 2;
                            break;
                        } else {
                            patternError("Unquoted special character '" + ch + '\'', pattern);
                        }
                        affix.append(ch);
                        break;
                    case 3:
                    case 4:
                        if (ch == '\'') {
                            if (pos + 1 >= pattern.length() || pattern.charAt(pos + 1) != '\'') {
                                subpart -= 2;
                            } else {
                                pos++;
                                affix.append(ch);
                            }
                        }
                        affix.append(ch);
                        break;
                    default:
                        break;
                }
            }
            if (subpart == 3 || subpart == 4) {
                patternError("Unterminated quote", pattern);
            }
            if (sub0Limit == 0) {
                sub0Limit = pattern.length();
            }
            if (sub2Limit == 0) {
                sub2Limit = pattern.length();
            }
            if (zeroDigitCount == 0 && sigDigitCount == 0 && digitLeftCount > 0 && decimalPos >= 0) {
                int n = decimalPos;
                if (decimalPos == 0) {
                    n++;
                }
                digitRightCount = digitLeftCount - n;
                digitLeftCount = n - 1;
                zeroDigitCount = 1;
            }
            if ((decimalPos < 0 && digitRightCount > 0 && sigDigitCount == 0) || ((decimalPos >= 0 && (sigDigitCount > 0 || decimalPos < digitLeftCount || decimalPos > digitLeftCount + zeroDigitCount)) || groupingCount == (byte) 0 || groupingCount2 == (byte) 0 || ((sigDigitCount > 0 && zeroDigitCount > 0) || subpart > 2))) {
                patternError("Malformed pattern", pattern);
            }
            if (padPos >= 0) {
                if (padPos == start) {
                    padPos = 0;
                } else if (padPos + 2 == sub0Start) {
                    padPos = 1;
                } else if (padPos == sub0Limit) {
                    padPos = 2;
                } else if (padPos + 2 == sub2Limit) {
                    padPos = 3;
                } else {
                    patternError("Illegal pad position", pattern);
                }
            }
            if (part == 0) {
                stringBuilder = prefix.toString();
                this.negPrefixPattern = stringBuilder;
                this.posPrefixPattern = stringBuilder;
                stringBuilder = suffix.toString();
                this.negSuffixPattern = stringBuilder;
                this.posSuffixPattern = stringBuilder;
                this.useExponentialNotation = expDigits >= (byte) 0;
                if (this.useExponentialNotation) {
                    this.minExponentDigits = expDigits;
                    this.exponentSignAlwaysShown = expSignAlways;
                }
                int digitTotalCount = (digitLeftCount + zeroDigitCount) + digitRightCount;
                int effectiveDecimalPos = decimalPos >= 0 ? decimalPos : digitTotalCount;
                boolean useSigDig = sigDigitCount > 0;
                setSignificantDigitsUsed(useSigDig);
                if (useSigDig) {
                    setMinimumSignificantDigits(sigDigitCount);
                    setMaximumSignificantDigits(sigDigitCount + digitRightCount);
                } else {
                    int i;
                    int minInt = effectiveDecimalPos - digitLeftCount;
                    setMinimumIntegerDigits(minInt);
                    if (this.useExponentialNotation) {
                        i = digitLeftCount + minInt;
                    } else {
                        i = DOUBLE_INTEGER_DIGITS;
                    }
                    setMaximumIntegerDigits(i);
                    _setMaximumFractionDigits(decimalPos >= 0 ? digitTotalCount - decimalPos : 0);
                    setMinimumFractionDigits(decimalPos >= 0 ? (digitLeftCount + zeroDigitCount) - decimalPos : 0);
                }
                setGroupingUsed(groupingCount > (byte) 0);
                this.groupingSize = groupingCount > (byte) 0 ? groupingCount : (byte) 0;
                if (groupingCount2 <= (byte) 0 || groupingCount2 == groupingCount) {
                    groupingCount2 = (byte) 0;
                }
                this.groupingSize2 = groupingCount2;
                this.multiplier = multpl;
                boolean z = decimalPos == 0 || decimalPos == digitTotalCount;
                setDecimalSeparatorAlwaysShown(z);
                if (padPos >= 0) {
                    this.padPosition = padPos;
                    this.formatWidth = sub0Limit - sub0Start;
                    this.pad = padChar;
                } else {
                    this.formatWidth = 0;
                }
                if (incrementVal != 0) {
                    int scale = incrementPos - effectiveDecimalPos;
                    this.roundingIncrementICU = android.icu.math.BigDecimal.valueOf(incrementVal, scale > 0 ? scale : 0);
                    if (scale < 0) {
                        this.roundingIncrementICU = this.roundingIncrementICU.movePointRight(-scale);
                    }
                    this.roundingMode = 6;
                } else {
                    setRoundingIncrement((android.icu.math.BigDecimal) null);
                }
                this.currencySignCount = currencySignCnt;
            } else {
                this.negPrefixPattern = prefix.toString();
                this.negSuffixPattern = suffix.toString();
                gotNegative = true;
            }
            part++;
        }
        if (pattern.length() == 0) {
            stringBuilder = "";
            this.posSuffixPattern = stringBuilder;
            this.posPrefixPattern = stringBuilder;
            setMinimumIntegerDigits(0);
            setMaximumIntegerDigits(DOUBLE_INTEGER_DIGITS);
            setMinimumFractionDigits(0);
            _setMaximumFractionDigits(DOUBLE_FRACTION_DIGITS);
        }
        if (!gotNegative || (this.negPrefixPattern.equals(this.posPrefixPattern) && this.negSuffixPattern.equals(this.posSuffixPattern))) {
            this.negSuffixPattern = this.posSuffixPattern;
            this.negPrefixPattern = PATTERN_MINUS_SIGN + this.posPrefixPattern;
        }
        setLocale(null, null);
        this.formatPattern = pattern;
        if (this.currencySignCount != 0) {
            Currency theCurrency = getCurrency();
            if (theCurrency != null) {
                setRoundingIncrement(theCurrency.getRoundingIncrement(this.currencyUsage));
                int d = theCurrency.getDefaultFractionDigits(this.currencyUsage);
                setMinimumFractionDigits(d);
                _setMaximumFractionDigits(d);
            }
            if (this.currencySignCount == 3 && this.currencyPluralInfo == null) {
                this.currencyPluralInfo = new CurrencyPluralInfo(this.symbols.getULocale());
            }
        }
        resetActualRounding();
    }

    private void patternError(String msg, String pattern) {
        throw new IllegalArgumentException(msg + " in pattern \"" + pattern + '\"');
    }

    public void setMaximumIntegerDigits(int newValue) {
        super.setMaximumIntegerDigits(Math.min(newValue, MAX_INTEGER_DIGITS));
    }

    public void setMinimumIntegerDigits(int newValue) {
        super.setMinimumIntegerDigits(Math.min(newValue, DOUBLE_INTEGER_DIGITS));
    }

    public int getMinimumSignificantDigits() {
        return this.minSignificantDigits;
    }

    public int getMaximumSignificantDigits() {
        return this.maxSignificantDigits;
    }

    public void setMinimumSignificantDigits(int min) {
        if (min < 1) {
            min = 1;
        }
        int max = Math.max(this.maxSignificantDigits, min);
        this.minSignificantDigits = min;
        this.maxSignificantDigits = max;
        setSignificantDigitsUsed(true);
    }

    public void setMaximumSignificantDigits(int max) {
        if (max < 1) {
            max = 1;
        }
        this.minSignificantDigits = Math.min(this.minSignificantDigits, max);
        this.maxSignificantDigits = max;
        setSignificantDigitsUsed(true);
    }

    public boolean areSignificantDigitsUsed() {
        return this.useSignificantDigits;
    }

    public void setSignificantDigitsUsed(boolean useSignificantDigits) {
        this.useSignificantDigits = useSignificantDigits;
    }

    public void setCurrency(Currency theCurrency) {
        super.setCurrency(theCurrency);
        if (theCurrency != null) {
            String s = theCurrency.getName(this.symbols.getULocale(), 0, null);
            this.symbols.setCurrency(theCurrency);
            this.symbols.setCurrencySymbol(s);
        }
        if (this.currencySignCount != 0) {
            if (theCurrency != null) {
                setRoundingIncrement(theCurrency.getRoundingIncrement(this.currencyUsage));
                int d = theCurrency.getDefaultFractionDigits(this.currencyUsage);
                setMinimumFractionDigits(d);
                setMaximumFractionDigits(d);
            }
            if (this.currencySignCount != 3) {
                expandAffixes(null);
            }
        }
    }

    public void setCurrencyUsage(CurrencyUsage newUsage) {
        if (newUsage == null) {
            throw new NullPointerException("return value is null at method AAA");
        }
        this.currencyUsage = newUsage;
        Currency theCurrency = getCurrency();
        if (theCurrency != null) {
            setRoundingIncrement(theCurrency.getRoundingIncrement(this.currencyUsage));
            int d = theCurrency.getDefaultFractionDigits(this.currencyUsage);
            setMinimumFractionDigits(d);
            _setMaximumFractionDigits(d);
        }
    }

    public CurrencyUsage getCurrencyUsage() {
        return this.currencyUsage;
    }

    @Deprecated
    protected Currency getEffectiveCurrency() {
        Currency c = getCurrency();
        if (c == null) {
            return Currency.getInstance(this.symbols.getInternationalCurrencySymbol());
        }
        return c;
    }

    public void setMaximumFractionDigits(int newValue) {
        _setMaximumFractionDigits(newValue);
        resetActualRounding();
    }

    private void _setMaximumFractionDigits(int newValue) {
        super.setMaximumFractionDigits(Math.min(newValue, DOUBLE_FRACTION_DIGITS));
    }

    public void setMinimumFractionDigits(int newValue) {
        super.setMinimumFractionDigits(Math.min(newValue, DOUBLE_FRACTION_DIGITS));
    }

    public void setParseBigDecimal(boolean value) {
        this.parseBigDecimal = value;
    }

    public boolean isParseBigDecimal() {
        return this.parseBigDecimal;
    }

    public void setParseMaxDigits(int newValue) {
        if (newValue > 0) {
            this.PARSE_MAX_EXPONENT = newValue;
        }
    }

    public int getParseMaxDigits() {
        return this.PARSE_MAX_EXPONENT;
    }

    private void writeObject(ObjectOutputStream stream) throws IOException {
        this.attributes.clear();
        stream.defaultWriteObject();
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        if (getMaximumIntegerDigits() > MAX_INTEGER_DIGITS) {
            setMaximumIntegerDigits(MAX_INTEGER_DIGITS);
        }
        if (getMaximumFractionDigits() > DOUBLE_FRACTION_DIGITS) {
            _setMaximumFractionDigits(DOUBLE_FRACTION_DIGITS);
        }
        if (this.serialVersionOnStream < 2) {
            this.exponentSignAlwaysShown = false;
            setInternalRoundingIncrement(null);
            this.roundingMode = 6;
            this.formatWidth = 0;
            this.pad = ' ';
            this.padPosition = 0;
            if (this.serialVersionOnStream < 1) {
                this.useExponentialNotation = false;
            }
        }
        if (this.serialVersionOnStream < 3) {
            setCurrencyForSymbols();
        }
        if (this.serialVersionOnStream < 4) {
            this.currencyUsage = CurrencyUsage.STANDARD;
        }
        this.serialVersionOnStream = 4;
        this.digitList = new DigitList();
        if (this.roundingIncrement != null) {
            setInternalRoundingIncrement(new android.icu.math.BigDecimal(this.roundingIncrement));
        }
        resetActualRounding();
    }

    private void setInternalRoundingIncrement(android.icu.math.BigDecimal value) {
        BigDecimal bigDecimal = null;
        this.roundingIncrementICU = value;
        if (value != null) {
            bigDecimal = value.toBigDecimal();
        }
        this.roundingIncrement = bigDecimal;
    }

    private void resetActualRounding() {
        if (this.roundingIncrementICU != null) {
            android.icu.math.BigDecimal byWidth = getMaximumFractionDigits() > 0 ? android.icu.math.BigDecimal.ONE.movePointLeft(getMaximumFractionDigits()) : android.icu.math.BigDecimal.ONE;
            if (this.roundingIncrementICU.compareTo(byWidth) >= 0) {
                this.actualRoundingIncrementICU = this.roundingIncrementICU;
            } else {
                if (byWidth.equals(android.icu.math.BigDecimal.ONE)) {
                    byWidth = null;
                }
                this.actualRoundingIncrementICU = byWidth;
            }
        } else if (this.roundingMode == 6 || isScientificNotation()) {
            this.actualRoundingIncrementICU = null;
        } else if (getMaximumFractionDigits() > 0) {
            this.actualRoundingIncrementICU = android.icu.math.BigDecimal.ONE.movePointLeft(getMaximumFractionDigits());
        } else {
            this.actualRoundingIncrementICU = android.icu.math.BigDecimal.ONE;
        }
        if (this.actualRoundingIncrementICU == null) {
            setRoundingDouble(0.0d);
            this.actualRoundingIncrement = null;
            return;
        }
        setRoundingDouble(this.actualRoundingIncrementICU.doubleValue());
        this.actualRoundingIncrement = this.actualRoundingIncrementICU.toBigDecimal();
    }

    private void setRoundingDouble(double newValue) {
        this.roundingDouble = newValue;
        if (this.roundingDouble > 0.0d) {
            double rawRoundedReciprocal = 1.0d / this.roundingDouble;
            this.roundingDoubleReciprocal = Math.rint(rawRoundedReciprocal);
            if (Math.abs(rawRoundedReciprocal - this.roundingDoubleReciprocal) > roundingIncrementEpsilon) {
                this.roundingDoubleReciprocal = 0.0d;
                return;
            }
            return;
        }
        this.roundingDoubleReciprocal = 0.0d;
    }
}
