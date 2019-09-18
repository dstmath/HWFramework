package android.icu.text;

import android.icu.impl.ICUConfig;
import android.icu.impl.PatternProps;
import android.icu.impl.Utility;
import android.icu.impl.locale.LanguageTag;
import android.icu.lang.UCharacter;
import android.icu.lang.UProperty;
import android.icu.math.MathContext;
import android.icu.text.NumberFormat;
import android.icu.text.PluralRules;
import android.icu.util.Currency;
import android.icu.util.CurrencyAmount;
import android.icu.util.ULocale;
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

@Deprecated
public class DecimalFormat_ICU58_Android extends NumberFormat {
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
    private ArrayList<FieldPosition> attributes = new ArrayList<>();
    private ChoiceFormat currencyChoice;
    private CurrencyPluralInfo currencyPluralInfo = null;
    private int currencySignCount = 0;
    private Currency.CurrencyUsage currencyUsage = Currency.CurrencyUsage.STANDARD;
    private boolean decimalSeparatorAlwaysShown = false;
    private transient DigitList_Android digitList = new DigitList_Android();
    private boolean exponentSignAlwaysShown = false;
    private String formatPattern = "";
    private int formatWidth = 0;
    private byte groupingSize = 3;
    private byte groupingSize2 = 0;
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

        public Unit(String prefix2, String suffix2) {
            this.prefix = prefix2;
            this.suffix = suffix2;
        }

        public void writeSuffix(StringBuffer toAppendTo) {
            toAppendTo.append(this.suffix);
        }

        public void writePrefix(StringBuffer toAppendTo) {
            toAppendTo.append(this.prefix);
        }

        public boolean equals(Object obj) {
            boolean z = true;
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof Unit)) {
                return false;
            }
            Unit other = (Unit) obj;
            if (!this.prefix.equals(other.prefix) || !this.suffix.equals(other.suffix)) {
                z = false;
            }
            return z;
        }

        public String toString() {
            return this.prefix + "/" + this.suffix;
        }
    }

    public DecimalFormat_ICU58_Android() {
        ULocale def = ULocale.getDefault(ULocale.Category.FORMAT);
        String pattern = getPattern(def, 0);
        this.symbols = new DecimalFormatSymbols(def);
        setCurrency(Currency.getInstance(def));
        applyPatternWithoutExpandAffix(pattern, false);
        if (this.currencySignCount == 3) {
            this.currencyPluralInfo = new CurrencyPluralInfo(def);
        } else {
            expandAffixAdjustWidth(null);
        }
    }

    public DecimalFormat_ICU58_Android(String pattern) {
        ULocale def = ULocale.getDefault(ULocale.Category.FORMAT);
        this.symbols = new DecimalFormatSymbols(def);
        setCurrency(Currency.getInstance(def));
        applyPatternWithoutExpandAffix(pattern, false);
        if (this.currencySignCount == 3) {
            this.currencyPluralInfo = new CurrencyPluralInfo(def);
        } else {
            expandAffixAdjustWidth(null);
        }
    }

    public DecimalFormat_ICU58_Android(String pattern, DecimalFormatSymbols symbols2) {
        createFromPatternAndSymbols(pattern, symbols2);
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

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v8, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v3, resolved type: android.icu.text.CurrencyPluralInfo} */
    /* JADX WARNING: Multi-variable type inference failed */
    public DecimalFormat_ICU58_Android(String pattern, DecimalFormatSymbols symbols2, CurrencyPluralInfo infoInput, int style2) {
        create(pattern, symbols2, style2 == 6 ? infoInput.clone() : infoInput, style2);
    }

    private void create(String pattern, DecimalFormatSymbols inputSymbols, CurrencyPluralInfo info, int inputStyle) {
        if (inputStyle != 6) {
            createFromPatternAndSymbols(pattern, inputSymbols);
        } else {
            this.symbols = (DecimalFormatSymbols) inputSymbols.clone();
            this.currencyPluralInfo = info;
            applyPatternWithoutExpandAffix(this.currencyPluralInfo.getCurrencyPluralPattern(PluralRules.KEYWORD_OTHER), false);
            setCurrencyForSymbols();
        }
        this.style = inputStyle;
    }

    @Deprecated
    public DecimalFormat_ICU58_Android(String pattern, DecimalFormatSymbols inputSymbols, int style2) {
        create(pattern, inputSymbols, style2 == 6 ? new CurrencyPluralInfo(inputSymbols.getULocale()) : null, style2);
    }

    public StringBuffer format(double number, StringBuffer result, FieldPosition fieldPosition) {
        return format(number, result, fieldPosition, false);
    }

    private boolean isNegative(double number) {
        return number < 0.0d || (number == 0.0d && 1.0d / number < 0.0d);
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
        double number2;
        StringBuffer stringBuffer = result;
        FieldPosition fieldPosition2 = fieldPosition;
        boolean z = false;
        fieldPosition2.setBeginIndex(0);
        fieldPosition2.setEndIndex(0);
        if (Double.isNaN(number)) {
            if (fieldPosition.getField() == 0) {
                fieldPosition2.setBeginIndex(result.length());
            } else if (fieldPosition.getFieldAttribute() == NumberFormat.Field.INTEGER) {
                fieldPosition2.setBeginIndex(result.length());
            }
            stringBuffer.append(this.symbols.getNaN());
            if (parseAttr) {
                addAttribute(NumberFormat.Field.INTEGER, result.length() - this.symbols.getNaN().length(), result.length());
            }
            if (fieldPosition.getField() == 0) {
                fieldPosition2.setEndIndex(result.length());
            } else if (fieldPosition.getFieldAttribute() == NumberFormat.Field.INTEGER) {
                fieldPosition2.setEndIndex(result.length());
            }
            addPadding(stringBuffer, fieldPosition2, 0, 0);
            return stringBuffer;
        }
        double number3 = multiply(number);
        boolean isNegative = isNegative(number3);
        double number4 = round(number3);
        if (Double.isInfinite(number4)) {
            int prefixLen = appendAffix(stringBuffer, isNegative, true, fieldPosition2, parseAttr);
            if (fieldPosition.getField() == 0) {
                fieldPosition2.setBeginIndex(result.length());
            } else if (fieldPosition.getFieldAttribute() == NumberFormat.Field.INTEGER) {
                fieldPosition2.setBeginIndex(result.length());
            }
            stringBuffer.append(this.symbols.getInfinity());
            if (parseAttr) {
                addAttribute(NumberFormat.Field.INTEGER, result.length() - this.symbols.getInfinity().length(), result.length());
            }
            if (fieldPosition.getField() == 0) {
                fieldPosition2.setEndIndex(result.length());
            } else if (fieldPosition.getFieldAttribute() == NumberFormat.Field.INTEGER) {
                fieldPosition2.setEndIndex(result.length());
            }
            addPadding(stringBuffer, fieldPosition2, prefixLen, appendAffix(stringBuffer, isNegative, false, fieldPosition2, parseAttr));
            return stringBuffer;
        }
        int precision = precision(false);
        if (!this.useExponentialNotation || precision <= 0 || number4 == 0.0d || this.roundingMode == 6) {
            number2 = number4;
        } else {
            int log10RoundingIncr = (1 - precision) + ((int) Math.floor(Math.log10(Math.abs(number4))));
            double roundingIncReciprocal = 0.0d;
            double roundingInc = 0.0d;
            if (log10RoundingIncr < 0) {
                roundingIncReciprocal = android.icu.math.BigDecimal.ONE.movePointRight(-log10RoundingIncr).doubleValue();
            } else {
                roundingInc = android.icu.math.BigDecimal.ONE.movePointRight(log10RoundingIncr).doubleValue();
            }
            number2 = round(number4, roundingInc, roundingIncReciprocal, this.roundingMode, isNegative);
        }
        synchronized (this.digitList) {
            try {
                DigitList_Android digitList_Android = this.digitList;
                if (!this.useExponentialNotation) {
                    try {
                        if (!areSignificantDigitsUsed()) {
                            z = true;
                        }
                    } catch (Throwable th) {
                        th = th;
                        int i = precision;
                        throw th;
                    }
                }
                digitList_Android.set(number2, precision, z);
                int i2 = precision;
                StringBuffer subformat = subformat(number2, stringBuffer, fieldPosition2, isNegative, false, parseAttr);
                return subformat;
            } catch (Throwable th2) {
                th = th2;
                throw th;
            }
        }
    }

    /* access modifiers changed from: package-private */
    @Deprecated
    public double adjustNumberAsInFormatting(double number) {
        if (Double.isNaN(number)) {
            return number;
        }
        double number2 = round(multiply(number));
        if (Double.isInfinite(number2)) {
            return number2;
        }
        return toDigitList(number2).getDouble();
    }

    /* access modifiers changed from: package-private */
    @Deprecated
    public DigitList_Android toDigitList(double number) {
        DigitList_Android result = new DigitList_Android();
        result.set(number, precision(false), false);
        return result;
    }

    /* access modifiers changed from: package-private */
    @Deprecated
    public boolean isNumberNegative(double number) {
        if (Double.isNaN(number)) {
            return false;
        }
        return isNegative(multiply(number));
    }

    private static double round(double number, double roundingInc, double roundingIncReciprocal, int mode, boolean isNegative) {
        double div;
        int i = mode;
        double div2 = roundingIncReciprocal == 0.0d ? number / roundingInc : number * roundingIncReciprocal;
        if (i != 7) {
            switch (i) {
                case 0:
                    div = Math.ceil(div2 - epsilon);
                    break;
                case 1:
                    div = Math.floor(epsilon + div2);
                    break;
                case 2:
                    div = isNegative ? Math.floor(epsilon + div2) : Math.ceil(div2 - epsilon);
                    break;
                case 3:
                    div = isNegative ? Math.ceil(div2 - epsilon) : Math.floor(epsilon + div2);
                    break;
                default:
                    double ceil = Math.ceil(div2);
                    double ceildiff = ceil - div2;
                    double floor = Math.floor(div2);
                    double floordiff = div2 - floor;
                    switch (i) {
                        case 4:
                            div = ceildiff <= epsilon + floordiff ? ceil : floor;
                            break;
                        case 5:
                            div = floordiff <= epsilon + ceildiff ? floor : ceil;
                            break;
                        case 6:
                            if (epsilon + floordiff >= ceildiff) {
                                if (epsilon + ceildiff >= floordiff) {
                                    double testFloor = floor / 2.0d;
                                    div = testFloor == Math.floor(testFloor) ? floor : ceil;
                                    break;
                                } else {
                                    div = ceil;
                                    break;
                                }
                            } else {
                                div = floor;
                                break;
                            }
                        default:
                            StringBuilder sb = new StringBuilder();
                            double d = ceil;
                            sb.append("Invalid rounding mode: ");
                            sb.append(i);
                            throw new IllegalArgumentException(sb.toString());
                    }
            }
            return roundingIncReciprocal == 0.0d ? div * roundingInc : div / roundingIncReciprocal;
        } else if (div2 == Math.floor(div2)) {
            return number;
        } else {
            throw new ArithmeticException("Rounding necessary");
        }
    }

    public StringBuffer format(long number, StringBuffer result, FieldPosition fieldPosition) {
        return format(number, result, fieldPosition, false);
    }

    private StringBuffer format(long number, StringBuffer result, FieldPosition fieldPosition, boolean parseAttr) {
        DigitList_Android digitList_Android;
        long number2 = number;
        StringBuffer stringBuffer = result;
        FieldPosition fieldPosition2 = fieldPosition;
        boolean tooBig = false;
        fieldPosition2.setBeginIndex(0);
        fieldPosition2.setEndIndex(0);
        if (this.actualRoundingIncrementICU != null) {
            return format(android.icu.math.BigDecimal.valueOf(number), stringBuffer, fieldPosition2);
        }
        boolean isNegative = number2 < 0;
        if (isNegative) {
            number2 = -number2;
        }
        if (this.multiplier != 1) {
            if (number2 < 0) {
                if (number2 <= Long.MIN_VALUE / ((long) this.multiplier)) {
                    tooBig = true;
                }
            } else if (number2 > Long.MAX_VALUE / ((long) this.multiplier)) {
                tooBig = true;
            }
            if (tooBig) {
                return format(BigInteger.valueOf(isNegative ? -number2 : number2), stringBuffer, fieldPosition2, parseAttr);
            }
        }
        boolean z = parseAttr;
        long number3 = number2 * ((long) this.multiplier);
        DigitList_Android digitList_Android2 = this.digitList;
        synchronized (digitList_Android2) {
            try {
                this.digitList.set(number3, precision(true));
                if (this.digitList.wasRounded()) {
                    if (this.roundingMode == 7) {
                        throw new ArithmeticException("Rounding necessary");
                    }
                }
                digitList_Android = digitList_Android2;
                StringBuffer subformat = subformat((double) number3, stringBuffer, fieldPosition2, isNegative, true, z);
                return subformat;
            } catch (Throwable th) {
                th = th;
                throw th;
            }
        }
    }

    public StringBuffer format(BigInteger number, StringBuffer result, FieldPosition fieldPosition) {
        return format(number, result, fieldPosition, false);
    }

    private StringBuffer format(BigInteger number, StringBuffer result, FieldPosition fieldPosition, boolean parseAttr) {
        StringBuffer subformat;
        if (this.actualRoundingIncrementICU != null) {
            return format(new android.icu.math.BigDecimal(number), result, fieldPosition);
        }
        boolean z = true;
        if (this.multiplier != 1) {
            number = number.multiply(BigInteger.valueOf((long) this.multiplier));
        }
        synchronized (this.digitList) {
            this.digitList.set(number, precision(true));
            if (this.digitList.wasRounded()) {
                if (this.roundingMode == 7) {
                    throw new ArithmeticException("Rounding necessary");
                }
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
        if (this.multiplier != 1) {
            number = number.multiply(BigDecimal.valueOf((long) this.multiplier));
        }
        if (this.actualRoundingIncrement != null) {
            number = number.divide(this.actualRoundingIncrement, 0, this.roundingMode).multiply(this.actualRoundingIncrement);
        }
        synchronized (this.digitList) {
            this.digitList.set(number, precision(false), !this.useExponentialNotation && !areSignificantDigitsUsed());
            if (this.digitList.wasRounded()) {
                if (this.roundingMode == 7) {
                    throw new ArithmeticException("Rounding necessary");
                }
            }
            subformat = subformat(number.doubleValue(), result, fieldPosition, number.signum() < 0, false, parseAttr);
        }
        return subformat;
    }

    public StringBuffer format(android.icu.math.BigDecimal number, StringBuffer result, FieldPosition fieldPosition) {
        StringBuffer subformat;
        if (this.multiplier != 1) {
            number = number.multiply(android.icu.math.BigDecimal.valueOf((long) this.multiplier), this.mathContext);
        }
        if (this.actualRoundingIncrementICU != null) {
            number = number.divide(this.actualRoundingIncrementICU, 0, this.roundingMode).multiply(this.actualRoundingIncrementICU, this.mathContext);
        }
        synchronized (this.digitList) {
            this.digitList.set(number, precision(false), !this.useExponentialNotation && !areSignificantDigitsUsed());
            if (this.digitList.wasRounded()) {
                if (this.roundingMode == 7) {
                    throw new ArithmeticException("Rounding necessary");
                }
            }
            subformat = subformat(number.doubleValue(), result, fieldPosition, number.signum() < 0, false, false);
        }
        return subformat;
    }

    private boolean isGroupingPosition(int pos) {
        if (!isGroupingUsed() || pos <= 0 || this.groupingSize <= 0) {
            return false;
        }
        boolean result = false;
        if (this.groupingSize2 <= 0 || pos <= this.groupingSize) {
            if (pos % this.groupingSize == 0) {
                result = true;
            }
            return result;
        }
        if ((pos - this.groupingSize) % this.groupingSize2 == 0) {
            result = true;
        }
        return result;
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

    /* access modifiers changed from: package-private */
    public PluralRules.FixedDecimal getFixedDecimal(double number) {
        return getFixedDecimal(number, this.digitList);
    }

    /* access modifiers changed from: package-private */
    public PluralRules.FixedDecimal getFixedDecimal(double number, DigitList_Android dl) {
        int minFractionalDigits;
        int maxFractionalDigits;
        long f;
        DigitList_Android digitList_Android = dl;
        int fractionalDigitsInDigitList = digitList_Android.count - digitList_Android.decimalAt;
        if (this.useSignificantDigits) {
            maxFractionalDigits = this.maxSignificantDigits - digitList_Android.decimalAt;
            minFractionalDigits = this.minSignificantDigits - digitList_Android.decimalAt;
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
        if (v < minFractionalDigits) {
            v = minFractionalDigits;
        } else if (v > maxFractionalDigits) {
            v = maxFractionalDigits;
        }
        long f2 = 0;
        if (v > 0) {
            for (int i = Math.max(0, digitList_Android.decimalAt); i < digitList_Android.count; i++) {
                f2 = (f2 * 10) + ((long) (digitList_Android.digits[i] - 48));
            }
            long f3 = f2;
            for (int i2 = v; i2 < fractionalDigitsInDigitList; i2++) {
                f3 *= 10;
            }
            f = f3;
        } else {
            f = 0;
        }
        PluralRules.FixedDecimal fixedDecimal = new PluralRules.FixedDecimal(number, v, f);
        return fixedDecimal;
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

    /* JADX WARNING: Code restructure failed: missing block: B:108:0x020d, code lost:
        if (r13 == r0.digitList.count) goto L_0x0212;
     */
    /* JADX WARNING: Removed duplicated region for block: B:115:0x021b  */
    /* JADX WARNING: Removed duplicated region for block: B:148:0x02a5  */
    /* JADX WARNING: Removed duplicated region for block: B:149:0x02ad  */
    /* JADX WARNING: Removed duplicated region for block: B:153:0x02be  */
    /* JADX WARNING: Removed duplicated region for block: B:155:0x02c6  */
    /* JADX WARNING: Removed duplicated region for block: B:160:0x02dc  */
    /* JADX WARNING: Removed duplicated region for block: B:167:0x0292 A[SYNTHETIC] */
    private void subformatFixed(StringBuffer result, FieldPosition fieldPosition, boolean isInteger, boolean parseAttr) {
        int fractionalDigitsCount;
        int fractionalDigitsCount2;
        int count;
        int i;
        int fracBegin;
        int count2;
        int count3;
        int digitIndex;
        String grouping;
        StringBuffer stringBuffer = result;
        FieldPosition fieldPosition2 = fieldPosition;
        String[] digits = this.symbols.getDigitStrings();
        String grouping2 = this.currencySignCount == 0 ? this.symbols.getGroupingSeparatorString() : this.symbols.getMonetaryGroupingSeparatorString();
        String decimal = this.currencySignCount == 0 ? this.symbols.getDecimalSeparatorString() : this.symbols.getMonetaryDecimalSeparatorString();
        boolean useSigDig = areSignificantDigitsUsed();
        int maxIntDig = getMaximumIntegerDigits();
        int maxSigDig = getMinimumIntegerDigits();
        int intBegin = result.length();
        if (fieldPosition.getField() == 0 || fieldPosition.getFieldAttribute() == NumberFormat.Field.INTEGER) {
            fieldPosition2.setBeginIndex(intBegin);
        }
        int i2 = 0;
        int minSigDig = getMinimumSignificantDigits();
        int maxSigDig2 = getMaximumSignificantDigits();
        if (!useSigDig) {
            minSigDig = 0;
            maxSigDig2 = Integer.MAX_VALUE;
        }
        int minIntDig = maxSigDig;
        long fractionalDigits = 0;
        int minSigDig2 = minSigDig;
        int maxSigDig3 = maxSigDig2;
        if (useSigDig) {
            fractionalDigitsCount = 0;
            fractionalDigitsCount2 = Math.max(1, this.digitList.decimalAt);
        } else {
            fractionalDigitsCount = 0;
            fractionalDigitsCount2 = minIntDig;
        }
        if (this.digitList.decimalAt > 0 && fractionalDigitsCount2 < this.digitList.decimalAt) {
            fractionalDigitsCount2 = this.digitList.decimalAt;
        }
        int digitIndex2 = 0;
        if (fractionalDigitsCount2 <= maxIntDig || maxIntDig < 0) {
        } else {
            fractionalDigitsCount2 = maxIntDig;
            int i3 = maxIntDig;
            digitIndex2 = this.digitList.decimalAt - fractionalDigitsCount2;
        }
        int sizeBeforeIntegerPart = result.length();
        int i4 = fractionalDigitsCount2 - 1;
        int i5 = fractionalDigitsCount2;
        int sigCount = 0;
        while (true) {
            int recordFractionDigits = i2;
            int i6 = i4;
            if (i6 < 0) {
                break;
            }
            String decimal2 = decimal;
            if (i6 >= this.digitList.decimalAt || digitIndex2 >= this.digitList.count || sigCount >= maxSigDig3) {
                stringBuffer.append(digits[0]);
                if (sigCount > 0) {
                    sigCount++;
                }
            } else {
                stringBuffer.append(digits[this.digitList.getDigitValue(digitIndex2)]);
                sigCount++;
                digitIndex2++;
            }
            if (isGroupingPosition(i6)) {
                stringBuffer.append(grouping2);
                grouping = grouping2;
                if (fieldPosition.getFieldAttribute() == NumberFormat.Field.GROUPING_SEPARATOR && fieldPosition.getBeginIndex() == 0 && fieldPosition.getEndIndex() == 0) {
                    fieldPosition2.setBeginIndex(result.length() - 1);
                    fieldPosition2.setEndIndex(result.length());
                }
                if (parseAttr) {
                    digitIndex = digitIndex2;
                    addAttribute(NumberFormat.Field.GROUPING_SEPARATOR, result.length() - 1, result.length());
                } else {
                    digitIndex = digitIndex2;
                }
            } else {
                grouping = grouping2;
                digitIndex = digitIndex2;
            }
            i4 = i6 - 1;
            i2 = recordFractionDigits;
            decimal = decimal2;
            grouping2 = grouping;
            digitIndex2 = digitIndex;
        }
        String decimal3 = decimal;
        if (fieldPosition.getField() == 0 || fieldPosition.getFieldAttribute() == NumberFormat.Field.INTEGER) {
            fieldPosition2.setEndIndex(result.length());
        }
        if (sigCount == 0 && this.digitList.count == 0) {
            sigCount = 1;
        }
        boolean fractionPresent = (!isInteger && digitIndex2 < this.digitList.count) || (!useSigDig ? getMinimumFractionDigits() > 0 : sigCount < minSigDig2);
        if (!fractionPresent && result.length() == sizeBeforeIntegerPart) {
            stringBuffer.append(digits[0]);
        }
        if (parseAttr) {
            int i7 = sizeBeforeIntegerPart;
            addAttribute(NumberFormat.Field.INTEGER, intBegin, result.length());
        }
        if (this.decimalSeparatorAlwaysShown || fractionPresent) {
            if (fieldPosition.getFieldAttribute() == NumberFormat.Field.DECIMAL_SEPARATOR) {
                fieldPosition2.setBeginIndex(result.length());
            }
            String decimal4 = decimal3;
            stringBuffer.append(decimal4);
            String str = decimal4;
            if (fieldPosition.getFieldAttribute() == NumberFormat.Field.DECIMAL_SEPARATOR) {
                fieldPosition2.setEndIndex(result.length());
            }
            if (parseAttr) {
                int i8 = intBegin;
                addAttribute(NumberFormat.Field.DECIMAL_SEPARATOR, result.length() - 1, result.length());
            }
        } else {
            int i9 = intBegin;
            String str2 = decimal3;
        }
        if (fieldPosition.getField() == 1) {
            fieldPosition2.setBeginIndex(result.length());
        } else if (fieldPosition.getFieldAttribute() == NumberFormat.Field.FRACTION) {
            fieldPosition2.setBeginIndex(result.length());
        }
        int fracBegin2 = result.length();
        boolean recordFractionDigits2 = fieldPosition2 instanceof UFieldPosition;
        int count4 = useSigDig ? Integer.MAX_VALUE : getMaximumFractionDigits();
        if (useSigDig) {
            if (sigCount == maxSigDig3) {
            } else if (sigCount >= minSigDig2) {
                count3 = count4;
            }
            count = 0;
            i = 0;
            while (true) {
                if (i < count) {
                    fracBegin = fracBegin2;
                    int i10 = count;
                    break;
                }
                if (!useSigDig) {
                    count2 = count;
                    if (i >= getMinimumFractionDigits() && (isInteger || digitIndex2 >= this.digitList.count)) {
                        fracBegin = fracBegin2;
                    }
                } else {
                    count2 = count;
                }
                fracBegin = fracBegin2;
                if (-1 - i <= this.digitList.decimalAt - 1) {
                    if (isInteger || digitIndex2 >= this.digitList.count) {
                        stringBuffer.append(digits[0]);
                        if (recordFractionDigits2) {
                            fractionalDigitsCount++;
                            fractionalDigits *= 10;
                        }
                    } else {
                        int digitIndex3 = digitIndex2 + 1;
                        byte digit = this.digitList.getDigitValue(digitIndex2);
                        stringBuffer.append(digits[digit]);
                        if (recordFractionDigits2) {
                            fractionalDigitsCount++;
                            fractionalDigits = (fractionalDigits * 10) + ((long) digit);
                        }
                        digitIndex2 = digitIndex3;
                    }
                    sigCount++;
                    if (useSigDig) {
                        if (sigCount != maxSigDig3) {
                            if (digitIndex2 == this.digitList.count && sigCount >= minSigDig2) {
                                break;
                            }
                        } else {
                            break;
                        }
                    } else {
                        continue;
                    }
                } else {
                    stringBuffer.append(digits[0]);
                    if (recordFractionDigits2) {
                        fractionalDigitsCount++;
                        fractionalDigits *= 10;
                    }
                }
                i++;
                count = count2;
                fracBegin2 = fracBegin;
            }
            fracBegin = fracBegin2;
            int fracBegin3 = digitIndex2;
            long fractionalDigits2 = fractionalDigits;
            int fractionalDigitsCount3 = fractionalDigitsCount;
            int i11 = sigCount;
            if (fieldPosition.getField() != 1) {
                fieldPosition2.setEndIndex(result.length());
            } else if (fieldPosition.getFieldAttribute() == NumberFormat.Field.FRACTION) {
                fieldPosition2.setEndIndex(result.length());
            }
            if (recordFractionDigits2) {
                ((UFieldPosition) fieldPosition2).setFractionDigits(fractionalDigitsCount3, fractionalDigits2);
            }
            if (parseAttr) {
                int i12 = fracBegin;
                return;
            } else if (this.decimalSeparatorAlwaysShown || fractionPresent) {
                addAttribute(NumberFormat.Field.FRACTION, fracBegin, result.length());
                return;
            } else {
                int i13 = fracBegin;
                return;
            }
        }
        count3 = count4;
        count = count3;
        i = 0;
        while (true) {
            if (i < count) {
            }
            i++;
            count = count2;
            fracBegin2 = fracBegin;
        }
        fracBegin = fracBegin2;
        int fracBegin32 = digitIndex2;
        long fractionalDigits22 = fractionalDigits;
        int fractionalDigitsCount32 = fractionalDigitsCount;
        int i112 = sigCount;
        if (fieldPosition.getField() != 1) {
        }
        if (recordFractionDigits2) {
        }
        if (parseAttr) {
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:104:0x01fd  */
    /* JADX WARNING: Removed duplicated region for block: B:107:0x020b  */
    /* JADX WARNING: Removed duplicated region for block: B:125:0x025b  */
    /* JADX WARNING: Removed duplicated region for block: B:127:0x0263  */
    /* JADX WARNING: Removed duplicated region for block: B:133:0x0281  */
    /* JADX WARNING: Removed duplicated region for block: B:136:0x0299  */
    /* JADX WARNING: Removed duplicated region for block: B:138:0x02a2  */
    /* JADX WARNING: Removed duplicated region for block: B:141:0x02c2  */
    /* JADX WARNING: Removed duplicated region for block: B:142:0x02c4  */
    /* JADX WARNING: Removed duplicated region for block: B:144:0x02c8  */
    /* JADX WARNING: Removed duplicated region for block: B:145:0x02ca  */
    /* JADX WARNING: Removed duplicated region for block: B:147:0x02ce  */
    /* JADX WARNING: Removed duplicated region for block: B:157:0x0311  */
    /* JADX WARNING: Removed duplicated region for block: B:176:0x0374 A[LOOP:1: B:175:0x0372->B:176:0x0374, LOOP_END] */
    /* JADX WARNING: Removed duplicated region for block: B:180:0x0383  */
    /* JADX WARNING: Removed duplicated region for block: B:187:0x03a2  */
    /* JADX WARNING: Removed duplicated region for block: B:189:0x03ae  */
    /* JADX WARNING: Removed duplicated region for block: B:198:? A[RETURN, SYNTHETIC] */
    private void subformatExponential(StringBuffer result, FieldPosition fieldPosition, boolean parseAttr) {
        int minFracDig;
        int exponent;
        int exponent2;
        boolean negativeExponent;
        int exponent3;
        int expDig;
        int i;
        int i2;
        String str;
        int exponent4;
        int minFracDig2;
        int integerDigits;
        int fracBegin;
        int intEnd;
        int fracBegin2;
        StringBuffer stringBuffer = result;
        FieldPosition fieldPosition2 = fieldPosition;
        String[] digits = this.symbols.getDigitStringsLocal();
        String decimal = this.currencySignCount == 0 ? this.symbols.getDecimalSeparatorString() : this.symbols.getMonetaryDecimalSeparatorString();
        boolean useSigDig = areSignificantDigitsUsed();
        int maxIntDig = getMaximumIntegerDigits();
        int minIntDig = getMinimumIntegerDigits();
        if (fieldPosition.getField() == 0) {
            fieldPosition2.setBeginIndex(result.length());
            fieldPosition2.setEndIndex(-1);
        } else if (fieldPosition.getField() == 1) {
            fieldPosition2.setBeginIndex(-1);
        } else if (fieldPosition.getFieldAttribute() == NumberFormat.Field.INTEGER) {
            fieldPosition2.setBeginIndex(result.length());
            fieldPosition2.setEndIndex(-1);
        } else if (fieldPosition.getFieldAttribute() == NumberFormat.Field.FRACTION) {
            fieldPosition2.setBeginIndex(-1);
        }
        int intBegin = result.length();
        int fracBegin3 = -1;
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
        boolean recordFractionDigits = false;
        int exponent5 = this.digitList.decimalAt;
        if (maxIntDig <= 1 || maxIntDig == minIntDig) {
            exponent = exponent5 - ((minIntDig > 0 || minFracDig > 0) ? minIntDig : 1);
        } else {
            exponent = (exponent5 > 0 ? (exponent5 - 1) / maxIntDig : (exponent5 / maxIntDig) - 1) * maxIntDig;
        }
        int minimumDigits = minIntDig + minFracDig;
        boolean z = useSigDig;
        int integerDigits2 = this.digitList.isZero() ? minIntDig : this.digitList.decimalAt - exponent;
        int i3 = maxIntDig;
        int totalDigits = this.digitList.count;
        if (minimumDigits > totalDigits) {
            totalDigits = minimumDigits;
        }
        if (integerDigits2 > totalDigits) {
            totalDigits = integerDigits2;
        }
        int i4 = minIntDig;
        int exponent6 = exponent;
        int i5 = minimumDigits;
        int fractionalDigitsCount = 0;
        long fractionalDigits = 0;
        int intEnd2 = -1;
        int i6 = 0;
        while (i6 < totalDigits) {
            if (i6 == integerDigits2) {
                if (fieldPosition.getField() == 0) {
                    integerDigits = integerDigits2;
                    fieldPosition2.setEndIndex(result.length());
                    minFracDig2 = minFracDig;
                } else {
                    integerDigits = integerDigits2;
                    minFracDig2 = minFracDig;
                    if (fieldPosition.getFieldAttribute() == NumberFormat.Field.INTEGER) {
                        fieldPosition2.setEndIndex(result.length());
                    }
                }
                if (parseAttr) {
                    intEnd2 = result.length();
                    addAttribute(NumberFormat.Field.INTEGER, intBegin, result.length());
                }
                if (fieldPosition.getFieldAttribute() == NumberFormat.Field.DECIMAL_SEPARATOR) {
                    fieldPosition2.setBeginIndex(result.length());
                }
                stringBuffer.append(decimal);
                if (fieldPosition.getFieldAttribute() == NumberFormat.Field.DECIMAL_SEPARATOR) {
                    fieldPosition2.setEndIndex(result.length());
                }
                int fracBegin4 = result.length();
                if (parseAttr) {
                    fracBegin2 = fracBegin4;
                    addAttribute(NumberFormat.Field.DECIMAL_SEPARATOR, result.length() - 1, result.length());
                } else {
                    fracBegin2 = fracBegin4;
                }
                if (fieldPosition.getField() == 1) {
                    fieldPosition2.setBeginIndex(result.length());
                } else if (fieldPosition.getFieldAttribute() == NumberFormat.Field.FRACTION) {
                    fieldPosition2.setBeginIndex(result.length());
                }
                recordFractionDigits = fieldPosition2 instanceof UFieldPosition;
                fracBegin3 = fracBegin2;
            } else {
                integerDigits = integerDigits2;
                minFracDig2 = minFracDig;
            }
            byte digit = i6 < this.digitList.count ? this.digitList.getDigitValue(i6) : 0;
            stringBuffer.append(digits[digit]);
            if (recordFractionDigits) {
                fractionalDigitsCount++;
                fracBegin = fracBegin3;
                intEnd = intEnd2;
                fractionalDigits = (fractionalDigits * 10) + ((long) digit);
            } else {
                fracBegin = fracBegin3;
                intEnd = intEnd2;
            }
            i6++;
            integerDigits2 = integerDigits;
            minFracDig = minFracDig2;
            intEnd2 = intEnd;
            fracBegin3 = fracBegin;
        }
        int i7 = minFracDig;
        if (this.digitList.isZero() && totalDigits == 0) {
            stringBuffer.append(digits[0]);
        }
        if (fracBegin3 == -1 && this.decimalSeparatorAlwaysShown) {
            if (fieldPosition.getFieldAttribute() == NumberFormat.Field.DECIMAL_SEPARATOR) {
                fieldPosition2.setBeginIndex(result.length());
            }
            stringBuffer.append(decimal);
            if (fieldPosition.getFieldAttribute() == NumberFormat.Field.DECIMAL_SEPARATOR) {
                fieldPosition2.setEndIndex(result.length());
            }
            if (parseAttr) {
                String str2 = decimal;
                addAttribute(NumberFormat.Field.DECIMAL_SEPARATOR, result.length() - 1, result.length());
                if (fieldPosition.getField() != 0) {
                    if (fieldPosition.getEndIndex() < 0) {
                        fieldPosition2.setEndIndex(result.length());
                    }
                } else if (fieldPosition.getField() == 1) {
                    if (fieldPosition.getBeginIndex() < 0) {
                        fieldPosition2.setBeginIndex(result.length());
                    }
                    fieldPosition2.setEndIndex(result.length());
                } else if (fieldPosition.getFieldAttribute() == NumberFormat.Field.INTEGER) {
                    if (fieldPosition.getEndIndex() < 0) {
                        fieldPosition2.setEndIndex(result.length());
                    }
                } else if (fieldPosition.getFieldAttribute() == NumberFormat.Field.FRACTION) {
                    if (fieldPosition.getBeginIndex() < 0) {
                        fieldPosition2.setBeginIndex(result.length());
                    }
                    fieldPosition2.setEndIndex(result.length());
                }
                if (recordFractionDigits) {
                    ((UFieldPosition) fieldPosition2).setFractionDigits(fractionalDigitsCount, fractionalDigits);
                }
                if (parseAttr) {
                    if (intEnd2 < 0) {
                        addAttribute(NumberFormat.Field.INTEGER, intBegin, result.length());
                    }
                    if (fracBegin3 > 0) {
                        addAttribute(NumberFormat.Field.FRACTION, fracBegin3, result.length());
                    }
                }
                if (fieldPosition.getFieldAttribute() == NumberFormat.Field.EXPONENT_SYMBOL) {
                    fieldPosition2.setBeginIndex(result.length());
                }
                stringBuffer.append(this.symbols.getExponentSeparator());
                if (fieldPosition.getFieldAttribute() == NumberFormat.Field.EXPONENT_SYMBOL) {
                    fieldPosition2.setEndIndex(result.length());
                }
                if (parseAttr) {
                    addAttribute(NumberFormat.Field.EXPONENT_SYMBOL, result.length() - this.symbols.getExponentSeparator().length(), result.length());
                }
                if (!this.digitList.isZero()) {
                    exponent2 = 0;
                } else {
                    exponent2 = exponent6;
                }
                negativeExponent = exponent2 >= 0;
                if (!negativeExponent) {
                    int exponent7 = -exponent2;
                    if (fieldPosition.getFieldAttribute() == NumberFormat.Field.EXPONENT_SIGN) {
                        fieldPosition2.setBeginIndex(result.length());
                    }
                    stringBuffer.append(this.symbols.getMinusSignString());
                    if (fieldPosition.getFieldAttribute() == NumberFormat.Field.EXPONENT_SIGN) {
                        fieldPosition2.setEndIndex(result.length());
                    }
                    if (parseAttr) {
                        boolean z2 = negativeExponent;
                        addAttribute(NumberFormat.Field.EXPONENT_SIGN, result.length() - 1, result.length());
                    }
                    exponent3 = exponent7;
                } else {
                    if (this.exponentSignAlwaysShown) {
                        if (fieldPosition.getFieldAttribute() == NumberFormat.Field.EXPONENT_SIGN) {
                            fieldPosition2.setBeginIndex(result.length());
                        }
                        stringBuffer.append(this.symbols.getPlusSignString());
                        if (fieldPosition.getFieldAttribute() == NumberFormat.Field.EXPONENT_SIGN) {
                            fieldPosition2.setEndIndex(result.length());
                        }
                        if (parseAttr) {
                            exponent4 = exponent2;
                            addAttribute(NumberFormat.Field.EXPONENT_SIGN, result.length() - 1, result.length());
                            exponent3 = exponent4;
                        }
                    }
                    exponent4 = exponent2;
                    exponent3 = exponent4;
                }
                int expBegin = result.length();
                int i8 = totalDigits;
                int i9 = fractionalDigitsCount;
                this.digitList.set((long) exponent3);
                expDig = this.minExponentDigits;
                if (this.useExponentialNotation && expDig < 1) {
                    expDig = 1;
                }
                for (i = this.digitList.decimalAt; i < expDig; i++) {
                    stringBuffer.append(digits[0]);
                }
                for (i2 = 0; i2 < this.digitList.decimalAt; i2++) {
                    if (i2 < this.digitList.count) {
                        str = digits[this.digitList.getDigitValue(i2)];
                    } else {
                        str = digits[0];
                    }
                    stringBuffer.append(str);
                }
                if (fieldPosition.getFieldAttribute() == NumberFormat.Field.EXPONENT) {
                    fieldPosition2.setBeginIndex(expBegin);
                    fieldPosition2.setEndIndex(result.length());
                }
                if (!parseAttr) {
                    addAttribute(NumberFormat.Field.EXPONENT, expBegin, result.length());
                    return;
                }
                return;
            }
        }
        if (fieldPosition.getField() != 0) {
        }
        if (recordFractionDigits) {
        }
        if (parseAttr) {
        }
        if (fieldPosition.getFieldAttribute() == NumberFormat.Field.EXPONENT_SYMBOL) {
        }
        stringBuffer.append(this.symbols.getExponentSeparator());
        if (fieldPosition.getFieldAttribute() == NumberFormat.Field.EXPONENT_SYMBOL) {
        }
        if (parseAttr) {
        }
        if (!this.digitList.isZero()) {
        }
        if (exponent2 >= 0) {
        }
        if (!negativeExponent) {
        }
        int expBegin2 = result.length();
        int i82 = totalDigits;
        int i92 = fractionalDigitsCount;
        this.digitList.set((long) exponent3);
        expDig = this.minExponentDigits;
        expDig = 1;
        while (i < expDig) {
        }
        while (i2 < this.digitList.decimalAt) {
        }
        if (fieldPosition.getFieldAttribute() == NumberFormat.Field.EXPONENT) {
        }
        if (!parseAttr) {
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
        boolean[] status;
        char c;
        int i;
        Number n;
        double d;
        String str = text;
        ParsePosition parsePosition2 = parsePosition;
        Currency[] currencyArr = currency;
        int i2 = parsePosition.getIndex();
        int backup = i2;
        if (this.formatWidth > 0 && (this.padPosition == 0 || this.padPosition == 1)) {
            i2 = skipPadding(str, i2);
        }
        if (str.regionMatches(i2, this.symbols.getNaN(), 0, this.symbols.getNaN().length())) {
            int i3 = i2 + this.symbols.getNaN().length();
            if (this.formatWidth > 0 && (this.padPosition == 2 || this.padPosition == 3)) {
                i3 = skipPadding(str, i3);
            }
            parsePosition2.setIndex(i3);
            return new Double(Double.NaN);
        }
        int i4 = backup;
        boolean[] status2 = new boolean[3];
        if (this.currencySignCount != 0) {
            if (!parseForCurrency(str, parsePosition2, currencyArr, status2)) {
                return null;
            }
            status = status2;
            c = 2;
            i = 0;
            int i5 = backup;
        } else if (currencyArr != null) {
            return null;
        } else {
            status = status2;
            c = 2;
            i = 0;
            int backup2 = backup;
            if (!subparse(str, parsePosition2, this.digitList, status2, currencyArr, this.negPrefixPattern, this.negSuffixPattern, this.posPrefixPattern, this.posSuffixPattern, false, 0)) {
                parsePosition2.setIndex(backup2);
                return null;
            }
        }
        if (status[i]) {
            if (status[1]) {
                d = Double.POSITIVE_INFINITY;
            } else {
                d = Double.NEGATIVE_INFINITY;
            }
            n = new Double(d);
        } else if (status[c]) {
            n = status[1] ? new Double("0.0") : new Double("-0.0");
        } else if (status[1] || !this.digitList.isZero()) {
            int mult = this.multiplier;
            while (mult % 10 == 0) {
                this.digitList.decimalAt--;
                mult /= 10;
            }
            if (this.parseBigDecimal || mult != 1 || !this.digitList.isIntegral()) {
                android.icu.math.BigDecimal big = this.digitList.getBigDecimalICU(status[1]);
                n = big;
                if (mult != 1) {
                    n = big.divide(android.icu.math.BigDecimal.valueOf((long) mult), this.mathContext);
                }
            } else if (this.digitList.decimalAt < 12) {
                long l = 0;
                if (this.digitList.count > 0) {
                    long l2 = 0;
                    int nx = i;
                    while (nx < this.digitList.count) {
                        l2 = ((10 * l2) + ((long) ((char) this.digitList.digits[nx]))) - 48;
                        nx++;
                    }
                    while (true) {
                        int nx2 = nx + 1;
                        if (nx >= this.digitList.decimalAt) {
                            break;
                        }
                        l2 *= 10;
                        nx = nx2;
                    }
                    if (!status[1]) {
                        l = -l2;
                    } else {
                        l = l2;
                    }
                }
                n = Long.valueOf(l);
            } else {
                BigInteger big2 = this.digitList.getBigInteger(status[1]);
                n = big2.bitLength() < 64 ? Long.valueOf(big2.longValue()) : big2;
            }
        } else {
            n = new Double("-0.0");
        }
        return currencyArr != null ? new CurrencyAmount(n, currencyArr[i]) : n;
    }

    private boolean parseForCurrency(String text, ParsePosition parsePosition, Currency[] currency, boolean[] status) {
        ParsePosition tmpPos;
        DigitList_Android tmpDigitList;
        boolean[] tmpStatus;
        int maxPosIndex;
        int i;
        boolean found;
        int maxPosIndex2;
        int maxErrorPos;
        ParsePosition tmpPos2;
        ParsePosition parsePosition2 = parsePosition;
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
        int maxPosIndex3 = origPos;
        int maxErrorPos2 = -1;
        boolean[] savedStatus = null;
        boolean[] tmpStatus2 = new boolean[3];
        ParsePosition tmpPos3 = new ParsePosition(origPos);
        DigitList_Android tmpDigitList2 = new DigitList_Android();
        if (this.style == 6) {
            String str = this.negPrefixPattern;
            String str2 = this.negSuffixPattern;
            tmpDigitList = tmpDigitList2;
            tmpPos = tmpPos3;
            tmpStatus = tmpStatus2;
            maxPosIndex = maxPosIndex3;
            i = 3;
            found = subparse(text, tmpPos3, tmpDigitList2, tmpStatus2, currency, str, str2, this.posPrefixPattern, this.posSuffixPattern, true, 1);
        } else {
            tmpDigitList = tmpDigitList2;
            tmpPos = tmpPos3;
            tmpStatus = tmpStatus2;
            maxPosIndex = maxPosIndex3;
            i = 3;
            found = subparse(text, tmpPos, tmpDigitList, tmpStatus, currency, this.negPrefixPattern, this.negSuffixPattern, this.posPrefixPattern, this.posSuffixPattern, true, 0);
        }
        if (found) {
            ParsePosition tmpPos4 = tmpPos;
            if (tmpPos4.getIndex() > maxPosIndex) {
                int maxPosIndex4 = tmpPos4.getIndex();
                savedStatus = tmpStatus;
                this.digitList = tmpDigitList;
                maxPosIndex = maxPosIndex4;
            }
        } else {
            maxErrorPos2 = tmpPos.getErrorIndex();
        }
        Iterator<AffixForCurrency> it = this.affixPatternsForCurrency.iterator();
        int maxPosIndex5 = maxPosIndex;
        int maxErrorPos3 = maxErrorPos2;
        boolean found2 = found;
        while (it.hasNext()) {
            AffixForCurrency affix = it.next();
            boolean[] tmpStatus3 = new boolean[i];
            ParsePosition tmpPos5 = new ParsePosition(origPos);
            DigitList_Android tmpDigitList3 = new DigitList_Android();
            String negPrefix = affix.getNegPrefix();
            DigitList_Android tmpDigitList4 = tmpDigitList3;
            ParsePosition tmpPos6 = tmpPos5;
            String str3 = negPrefix;
            boolean[] tmpStatus4 = tmpStatus3;
            AffixForCurrency affixForCurrency = affix;
            boolean found3 = found2;
            int maxErrorPos4 = maxErrorPos3;
            int origPos2 = origPos;
            int origPos3 = maxPosIndex5;
            Iterator<AffixForCurrency> it2 = it;
            if (subparse(text, tmpPos5, tmpDigitList3, tmpStatus3, currency, str3, affix.getNegSuffix(), affix.getPosPrefix(), affix.getPosSuffix(), true, affix.getPatternType())) {
                tmpPos2 = tmpPos6;
                if (tmpPos2.getIndex() > origPos3) {
                    int maxPosIndex6 = tmpPos2.getIndex();
                    this.digitList = tmpDigitList4;
                    maxPosIndex5 = maxPosIndex6;
                    savedStatus = tmpStatus4;
                } else {
                    maxPosIndex5 = origPos3;
                }
                maxErrorPos3 = maxErrorPos4;
                found2 = true;
            } else {
                tmpPos2 = tmpPos6;
                if (tmpPos2.getErrorIndex() > maxErrorPos4) {
                    maxErrorPos3 = tmpPos2.getErrorIndex();
                } else {
                    maxErrorPos3 = maxErrorPos4;
                }
                int i2 = maxErrorPos3;
                maxPosIndex5 = origPos3;
                found2 = found3;
            }
            ParsePosition parsePosition3 = tmpPos2;
            DigitList_Android digitList_Android = tmpDigitList4;
            boolean[] zArr = tmpStatus4;
            it = it2;
            origPos = origPos2;
            i = 3;
        }
        int origPos4 = origPos;
        boolean found4 = found2;
        int maxErrorPos5 = maxErrorPos3;
        int origPos5 = maxPosIndex5;
        boolean[] tmpStatus5 = new boolean[3];
        int origPos6 = origPos4;
        ParsePosition tmpPos7 = new ParsePosition(origPos6);
        DigitList_Android tmpDigitList5 = new DigitList_Android();
        DigitList_Android tmpDigitList6 = tmpDigitList5;
        ParsePosition tmpPos8 = tmpPos7;
        int i3 = origPos6;
        boolean[] tmpStatus6 = tmpStatus5;
        if (subparse(text, tmpPos7, tmpDigitList5, tmpStatus5, currency, this.negativePrefix, this.negativeSuffix, this.positivePrefix, this.positiveSuffix, false, 0)) {
            ParsePosition tmpPos9 = tmpPos8;
            if (tmpPos9.getIndex() > origPos5) {
                maxPosIndex2 = tmpPos9.getIndex();
                savedStatus = tmpStatus6;
                this.digitList = tmpDigitList6;
            } else {
                maxPosIndex2 = origPos5;
            }
            found4 = true;
            maxErrorPos = maxErrorPos5;
        } else {
            ParsePosition tmpPos10 = tmpPos8;
            if (tmpPos10.getErrorIndex() > maxErrorPos5) {
                maxErrorPos = tmpPos10.getErrorIndex();
            } else {
                maxErrorPos = maxErrorPos5;
            }
            maxPosIndex2 = origPos5;
        }
        if (found4) {
            ParsePosition parsePosition4 = parsePosition;
            parsePosition4.setIndex(maxPosIndex2);
            parsePosition4.setErrorIndex(-1);
            int index = 0;
            while (true) {
                int index2 = index;
                if (index2 >= 3) {
                    break;
                }
                status[index2] = savedStatus[index2];
                index = index2 + 1;
            }
        } else {
            parsePosition.setErrorIndex(maxErrorPos);
        }
        return found4;
    }

    private void setupCurrencyAffixForAllPatterns() {
        if (this.currencyPluralInfo == null) {
            this.currencyPluralInfo = new CurrencyPluralInfo(this.symbols.getULocale());
        }
        this.affixPatternsForCurrency = new HashSet();
        String savedFormatPattern = this.formatPattern;
        applyPatternWithoutExpandAffix(getPattern(this.symbols.getULocale(), 1), false);
        AffixForCurrency affixForCurrency = new AffixForCurrency(this.negPrefixPattern, this.negSuffixPattern, this.posPrefixPattern, this.posSuffixPattern, 0);
        this.affixPatternsForCurrency.add(affixForCurrency);
        Iterator<String> iter = this.currencyPluralInfo.pluralPatternIterator();
        Set<String> currencyUnitPatternSet = new HashSet<>();
        while (iter.hasNext()) {
            String currencyPattern = this.currencyPluralInfo.getCurrencyPluralPattern(iter.next());
            if (currencyPattern != null && !currencyUnitPatternSet.contains(currencyPattern)) {
                currencyUnitPatternSet.add(currencyPattern);
                applyPatternWithoutExpandAffix(currencyPattern, false);
                AffixForCurrency affixes = new AffixForCurrency(this.negPrefixPattern, this.negSuffixPattern, this.posPrefixPattern, this.posSuffixPattern, 1);
                this.affixPatternsForCurrency.add(affixes);
            }
        }
        this.formatPattern = savedFormatPattern;
    }

    /* JADX WARNING: type inference failed for: r15v0, types: [boolean] */
    /* JADX WARNING: type inference failed for: r15v4 */
    /* JADX WARNING: type inference failed for: r15v5 */
    /* JADX WARNING: Code restructure failed: missing block: B:108:0x021a, code lost:
        r38 = r1;
        r29 = r2;
        r40 = r3;
        r41 = r4;
        r8 = r5;
        r26 = r25;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:109:0x0225, code lost:
        r43 = r31;
        r39 = r33;
        r30 = r35;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:133:0x02cb, code lost:
        r26 = r0;
        r37 = r31;
        r30 = r35;
        r38 = r36;
        r29 = r2;
        r39 = r33;
        r40 = r3;
        r41 = r4;
        r42 = r8;
        r8 = r5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:134:0x02f2, code lost:
        if (r10.regionMatches(true, r14, r5, 0, r5.length()) == false) goto L_0x0389;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:135:0x02f4, code lost:
        r0 = false;
        r1 = r8.length() + r14;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:136:0x02fe, code lost:
        if (r1 >= r50.length()) goto L_0x032d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:137:0x0300, code lost:
        r2 = r9.symbols.getPlusSignString();
        r3 = r9.symbols.getMinusSignString();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:138:0x0315, code lost:
        if (r10.regionMatches(r1, r2, 0, r2.length()) == false) goto L_0x031d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:139:0x0317, code lost:
        r1 = r1 + r2.length();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:141:0x0325, code lost:
        if (r10.regionMatches(r1, r3, 0, r3.length()) == false) goto L_0x032d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:142:0x0327, code lost:
        r1 = r1 + r3.length();
        r0 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:143:0x032d, code lost:
        r2 = new android.icu.text.DigitList_Android();
        r3 = 0;
        r2.count = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:145:0x0339, code lost:
        if (r1 >= r50.length()) goto L_0x0351;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:146:0x033b, code lost:
        r4 = r37;
        r5 = matchesDigit(r10, r1, r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:147:0x0341, code lost:
        if (r5 <= 0) goto L_0x0353;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:148:0x0343, code lost:
        r2.append((char) (r4[r3] + 48));
        r1 = r1 + r5;
        r37 = r4;
        r3 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:149:0x0351, code lost:
        r4 = r37;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:151:0x0355, code lost:
        if (r2.count <= 0) goto L_0x0386;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:152:0x0357, code lost:
        if (r6 == false) goto L_0x0361;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:153:0x0359, code lost:
        if (r21 == 0) goto L_0x0361;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:154:0x035b, code lost:
        r19 = true;
        r43 = r4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:156:0x0365, code lost:
        if (r2.count <= 10) goto L_0x0375;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:157:0x0367, code lost:
        if (r0 == false) goto L_0x036e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:158:0x0369, code lost:
        r53[2] = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:159:0x036e, code lost:
        r53[0] = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:160:0x0372, code lost:
        r43 = r4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:161:0x0375, code lost:
        r2.decimalAt = r2.count;
        r43 = r4;
        r3 = r2.getLong();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:162:0x037f, code lost:
        if (r0 == false) goto L_0x0382;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:163:0x0381, code lost:
        r3 = -r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:164:0x0382, code lost:
        r17 = r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:165:0x0384, code lost:
        r14 = r1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:166:0x0386, code lost:
        r43 = r4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:167:0x0389, code lost:
        r43 = r37;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:60:0x0124, code lost:
        r19 = true;
        r43 = r0;
        r38 = r1;
        r39 = r2;
        r40 = r3;
        r32 = r8;
        r26 = r25;
        r41 = r29;
        r8 = r5;
        r29 = r24;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:78:0x0184, code lost:
        r19 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:79:0x0187, code lost:
        r38 = r1;
        r39 = r2;
        r40 = r3;
        r41 = r4;
        r8 = r5;
        r29 = r24;
        r26 = r25;
        r43 = r31;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:93:0x01cf, code lost:
        r19 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:94:0x01d2, code lost:
        r30 = r0;
        r38 = r1;
        r40 = r3;
        r41 = r4;
        r8 = r5;
        r29 = r24;
        r26 = r25;
        r43 = r31;
        r39 = r33;
     */
    /* JADX WARNING: Removed duplicated region for block: B:113:0x0236  */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x004c  */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x004e  */
    /* JADX WARNING: Removed duplicated region for block: B:214:0x0438  */
    /* JADX WARNING: Removed duplicated region for block: B:215:0x044f  */
    /* JADX WARNING: Removed duplicated region for block: B:217:0x0456  */
    /* JADX WARNING: Removed duplicated region for block: B:218:0x046b  */
    /* JADX WARNING: Removed duplicated region for block: B:222:0x0474  */
    /* JADX WARNING: Removed duplicated region for block: B:223:0x0476  */
    /* JADX WARNING: Removed duplicated region for block: B:226:0x047c  */
    /* JADX WARNING: Removed duplicated region for block: B:227:0x047e  */
    /* JADX WARNING: Removed duplicated region for block: B:229:0x0482  */
    /* JADX WARNING: Removed duplicated region for block: B:230:0x0484  */
    /* JADX WARNING: Removed duplicated region for block: B:232:0x0488  */
    /* JADX WARNING: Removed duplicated region for block: B:234:0x048c  */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x0077  */
    /* JADX WARNING: Removed duplicated region for block: B:263:0x021a A[EDGE_INSN: B:263:0x021a->B:108:0x021a ?: BREAK  , SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x008b  */
    private final boolean subparse(String text, ParsePosition parsePosition, DigitList_Android digits, boolean[] status, Currency[] currency, String negPrefix, String negSuffix, String posPrefix, String posSuffix, boolean parseComplexCurrency, int type) {
        int negMatch;
        int position;
        boolean z;
        ? r15;
        ParsePosition parsePosition2;
        ParsePosition parsePosition3;
        int posMatch;
        int posMatch2;
        int negMatch2;
        UnicodeSet unicodeSet;
        UnicodeSet unicodeSet2;
        int lastGroup;
        String decimal;
        int backup;
        int digitCount;
        String decimal2;
        int i;
        int digitCount2;
        int lastGroup2;
        boolean z2;
        int[] parsedDigit;
        int digitCount3;
        String decimal3;
        String grouping;
        String grouping2;
        UnicodeSet decimalEquiv;
        int lastGroup3;
        UnicodeSet groupEquiv;
        int lastGroup4;
        int position2;
        String grouping3;
        String str = text;
        ParsePosition parsePosition4 = parsePosition;
        DigitList_Android digitList_Android = digits;
        int position3 = parsePosition.getIndex();
        int oldStart = parsePosition.getIndex();
        if (this.formatWidth > 0 && this.padPosition == 0) {
            position3 = skipPadding(str, position3);
        }
        int position4 = position3;
        String str2 = str;
        int i2 = position4;
        boolean z3 = parseComplexCurrency;
        int i3 = type;
        int posMatch3 = compareAffix(str2, i2, false, true, posPrefix, z3, i3, currency);
        int negMatch3 = compareAffix(str2, i2, true, true, negPrefix, z3, i3, currency);
        if (posMatch3 >= 0 && negMatch3 >= 0) {
            if (posMatch3 > negMatch3) {
                negMatch3 = -1;
            } else if (negMatch3 > posMatch3) {
                negMatch = negMatch3;
                posMatch3 = -1;
                if (posMatch3 < 0) {
                    position = position4 + posMatch3;
                } else if (negMatch >= 0) {
                    position = position4 + negMatch;
                } else {
                    int i4 = posMatch3;
                    parsePosition.setErrorIndex(position4);
                    return false;
                }
                if (this.formatWidth > 0 && this.padPosition == 1) {
                    position = skipPadding(str, position);
                }
                status[0] = false;
                if (!str.regionMatches(position, this.symbols.getInfinity(), 0, this.symbols.getInfinity().length())) {
                    position += this.symbols.getInfinity().length();
                    status[0] = true;
                    r15 = 1;
                    z = false;
                    parsePosition2 = parsePosition;
                } else {
                    digitList_Android.count = 0;
                    digitList_Android.decimalAt = 0;
                    String decimal4 = this.currencySignCount == 0 ? this.symbols.getDecimalSeparatorString() : this.symbols.getMonetaryDecimalSeparatorString();
                    String grouping4 = this.currencySignCount == 0 ? this.symbols.getGroupingSeparatorString() : this.symbols.getMonetaryGroupingSeparatorString();
                    String exponentSep = this.symbols.getExponentSeparator();
                    long exponent = 0;
                    boolean strictParse = isParseStrict();
                    boolean strictFail = false;
                    byte b = this.groupingSize2 == 0 ? this.groupingSize : this.groupingSize2;
                    if (skipExtendedSeparatorParsing) {
                        unicodeSet = UnicodeSet.EMPTY;
                    } else {
                        unicodeSet = getEquivalentDecimals(decimal4, strictParse);
                    }
                    UnicodeSet decimalEquiv2 = unicodeSet;
                    if (skipExtendedSeparatorParsing) {
                        unicodeSet2 = UnicodeSet.EMPTY;
                    } else {
                        unicodeSet2 = strictParse ? strictDefaultGroupingSeparators : defaultGroupingSeparators;
                    }
                    UnicodeSet groupEquiv2 = unicodeSet2;
                    String decimal5 = decimal4;
                    boolean sawDigit = false;
                    int[] parsedDigit2 = {-1};
                    String grouping5 = grouping4;
                    int lastGroup5 = -1;
                    int groupedDigitCount = 0;
                    int digitCount4 = 0;
                    int lastGroup6 = 0;
                    int groupedDigitCount2 = 0;
                    int backup2 = -1;
                    String decimal6 = decimal5;
                    while (true) {
                        if (position >= text.length()) {
                            lastGroup = lastGroup5;
                            decimal = decimal6;
                            backup = backup2;
                            byte b2 = b;
                            UnicodeSet unicodeSet3 = groupEquiv2;
                            digitCount = digitCount4;
                            String str3 = exponentSep;
                            UnicodeSet unicodeSet4 = decimalEquiv2;
                            break;
                        }
                        int matchLen = matchesDigit(str, position, parsedDigit2);
                        if (matchLen <= 0) {
                            parsedDigit = parsedDigit2;
                            digitCount3 = digitCount4;
                            byte b3 = b;
                            int decimalStrLen = decimal6.length();
                            if (!str.regionMatches(position, decimal6, 0, decimalStrLen)) {
                                if (isGroupingUsed()) {
                                    decimal3 = decimal6;
                                    grouping = grouping5;
                                    int groupingStrLen = grouping.length();
                                    int i5 = decimalStrLen;
                                    if (str.regionMatches(position, grouping, 0, groupingStrLen)) {
                                        if (lastGroup6 != 0) {
                                            break;
                                        } else if (!strictParse || (sawDigit && backup2 == -1)) {
                                            backup2 = position;
                                            position2 = position + groupingStrLen;
                                            groupedDigitCount2 = 1;
                                            grouping5 = grouping;
                                            digitCount4 = digitCount3;
                                            parsedDigit2 = parsedDigit;
                                            b = b3;
                                            decimal6 = decimal3;
                                        }
                                    }
                                } else {
                                    decimal3 = decimal6;
                                    int i6 = decimalStrLen;
                                    grouping = grouping5;
                                }
                                int cp = str.codePointAt(position);
                                if (lastGroup6 != 0) {
                                    grouping3 = grouping;
                                    decimalEquiv = decimalEquiv2;
                                    if (isGroupingUsed()) {
                                        break;
                                    }
                                    break;
                                }
                                decimalEquiv = decimalEquiv2;
                                if (!decimalEquiv.contains(cp)) {
                                    grouping3 = grouping;
                                    if (isGroupingUsed() && groupedDigitCount2 == 0) {
                                        groupEquiv = groupEquiv2;
                                        if (!groupEquiv.contains(cp)) {
                                            lastGroup3 = lastGroup5;
                                            break;
                                        } else if (lastGroup6 != 0) {
                                            UnicodeSet unicodeSet5 = groupEquiv;
                                            lastGroup = lastGroup5;
                                            UnicodeSet unicodeSet6 = decimalEquiv;
                                            backup = backup2;
                                            digitCount = digitCount3;
                                            String str4 = exponentSep;
                                            break;
                                        } else {
                                            if (strictParse) {
                                                if (!sawDigit) {
                                                    lastGroup4 = lastGroup5;
                                                    break;
                                                }
                                                lastGroup4 = lastGroup5;
                                                if (backup2 != -1) {
                                                    break;
                                                }
                                            } else {
                                                lastGroup4 = lastGroup5;
                                            }
                                            grouping5 = String.valueOf(Character.toChars(cp));
                                            backup2 = position;
                                            position2 = position + Character.charCount(cp);
                                            groupedDigitCount2 = 1;
                                            groupEquiv2 = groupEquiv;
                                            decimalEquiv2 = decimalEquiv;
                                            digitCount4 = digitCount3;
                                            parsedDigit2 = parsedDigit;
                                            b = b3;
                                            decimal6 = decimal3;
                                            lastGroup5 = lastGroup4;
                                        }
                                    } else {
                                        lastGroup3 = lastGroup5;
                                        groupEquiv = groupEquiv2;
                                    }
                                } else if (strictParse) {
                                    grouping2 = grouping;
                                    if (!(backup2 == -1 && (lastGroup5 == -1 || groupedDigitCount == this.groupingSize))) {
                                        strictFail = true;
                                    }
                                    if (!isParseIntegerOnly()) {
                                        break;
                                    }
                                    digitList_Android.decimalAt = digitCount3;
                                    String decimal7 = String.valueOf(Character.toChars(cp));
                                    lastGroup6 = 1;
                                    position2 = position + Character.charCount(cp);
                                    decimalEquiv2 = decimalEquiv;
                                    digitCount4 = digitCount3;
                                    b = b3;
                                    grouping5 = grouping2;
                                    decimal6 = decimal7;
                                    parsedDigit2 = parsedDigit;
                                } else {
                                    grouping2 = grouping;
                                    if (!isParseIntegerOnly()) {
                                    }
                                }
                            } else if (!strictParse || (backup2 == -1 && (lastGroup5 == -1 || groupedDigitCount == this.groupingSize))) {
                                if (isParseIntegerOnly() || lastGroup6 != 0) {
                                    break;
                                }
                                digitList_Android.decimalAt = digitCount3;
                                lastGroup6 = 1;
                                position2 = position + decimalStrLen;
                                digitCount4 = digitCount3;
                                parsedDigit2 = parsedDigit;
                                b = b3;
                            }
                        } else {
                            if (backup2 != -1) {
                                if (!strictParse || ((lastGroup5 == -1 || groupedDigitCount == b) && (lastGroup5 != -1 || groupedDigitCount <= b))) {
                                    lastGroup5 = backup2;
                                    groupedDigitCount = 0;
                                }
                            }
                            groupedDigitCount++;
                            position2 = position + matchLen;
                            backup2 = -1;
                            sawDigit = true;
                            if (parsedDigit2[0] != 0 || digitList_Android.count != 0) {
                                digitCount4++;
                                digitList_Android.append((char) (parsedDigit2[0] + 48));
                            } else if (lastGroup6 != 0) {
                                digitList_Android.decimalAt--;
                            }
                        }
                    }
                    strictFail = true;
                    UnicodeSet unicodeSet7 = groupEquiv;
                    UnicodeSet unicodeSet8 = decimalEquiv;
                    backup = backup2;
                    digitCount = digitCount3;
                    String str5 = exponentSep;
                    int[] iArr = parsedDigit;
                    decimal = decimal3;
                    String str6 = grouping2;
                    lastGroup = lastGroup4;
                    if (digitList_Android.decimalAt != 0 || !isDecimalPatternMatchRequired()) {
                        decimal2 = decimal;
                        i = -1;
                        parsePosition2 = parsePosition;
                    } else {
                        decimal2 = decimal;
                        i = -1;
                        if (this.formatPattern.indexOf(decimal2) != -1) {
                            ParsePosition parsePosition5 = parsePosition;
                            parsePosition5.setIndex(oldStart);
                            parsePosition5.setErrorIndex(position);
                            return false;
                        }
                        parsePosition2 = parsePosition;
                    }
                    int backup3 = backup;
                    if (backup3 != i) {
                        position = backup3;
                    }
                    if (lastGroup6 == 0) {
                        digitCount2 = digitCount;
                        digitList_Android.decimalAt = digitCount2;
                    } else {
                        digitCount2 = digitCount;
                    }
                    if (!strictParse || lastGroup6 != 0) {
                        lastGroup2 = lastGroup;
                    } else {
                        lastGroup2 = lastGroup;
                        if (!(lastGroup2 == i || groupedDigitCount == this.groupingSize)) {
                            strictFail = true;
                        }
                    }
                    if (strictFail) {
                        parsePosition2.setIndex(oldStart);
                        parsePosition2.setErrorIndex(position);
                        return false;
                    }
                    int i7 = lastGroup2;
                    long exponent2 = exponent + ((long) digitList_Android.decimalAt);
                    String str7 = decimal2;
                    int i8 = backup3;
                    if (exponent2 < ((long) (-getParseMaxDigits()))) {
                        z2 = true;
                        status[2] = true;
                    } else {
                        z2 = true;
                        if (exponent2 > ((long) getParseMaxDigits())) {
                            status[0] = true;
                        } else {
                            digitList_Android.decimalAt = (int) exponent2;
                        }
                    }
                    if (sawDigit || digitCount2 != 0) {
                        z = false;
                        r15 = z2;
                    } else {
                        parsePosition2.setIndex(oldStart);
                        parsePosition2.setErrorIndex(oldStart);
                        return false;
                    }
                }
                if (this.formatWidth > 0 && this.padPosition == 2) {
                    position = skipPadding(str, position);
                }
                if (posMatch3 < 0) {
                    int i9 = posMatch3;
                    parsePosition3 = parsePosition2;
                    posMatch = compareAffix(str, position, false, false, posSuffix, parseComplexCurrency, type, currency);
                } else {
                    int i10 = posMatch3;
                    parsePosition3 = parsePosition2;
                    posMatch = i10;
                }
                if (negMatch < 0) {
                    posMatch2 = posMatch;
                    negMatch2 = compareAffix(str, position, true, false, negSuffix, parseComplexCurrency, type, currency);
                } else {
                    posMatch2 = posMatch;
                    negMatch2 = negMatch;
                }
                if (posMatch2 >= 0 && negMatch2 >= 0) {
                    if (posMatch2 <= negMatch2) {
                        negMatch2 = -1;
                    } else if (negMatch2 > posMatch2) {
                        posMatch2 = -1;
                    }
                }
                if ((posMatch2 < 0 ? r15 : z) != (negMatch2 < 0 ? r15 : z)) {
                    parsePosition3.setErrorIndex(position);
                    return z;
                }
                int position5 = position + (posMatch2 >= 0 ? posMatch2 : negMatch2);
                if (this.formatWidth > 0 && this.padPosition == 3) {
                    position5 = skipPadding(str, position5);
                }
                parsePosition3.setIndex(position5);
                status[r15] = posMatch2 >= 0 ? r15 : z;
                if (parsePosition.getIndex() != oldStart) {
                    return r15;
                }
                parsePosition3.setErrorIndex(position5);
                return z;
            }
        }
        negMatch = negMatch3;
        if (posMatch3 < 0) {
        }
        position = skipPadding(str, position);
        status[0] = false;
        if (!str.regionMatches(position, this.symbols.getInfinity(), 0, this.symbols.getInfinity().length())) {
        }
        position = skipPadding(str, position);
        if (posMatch3 < 0) {
        }
        if (negMatch < 0) {
        }
        if (posMatch2 <= negMatch2) {
        }
        if ((posMatch2 < 0 ? r15 : z) != (negMatch2 < 0 ? r15 : z)) {
        }
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
        while (true) {
            if (idx >= affix.length()) {
                break;
            } else if (isBidiMark(affix.charAt(idx))) {
                hasBidiMark = true;
                break;
            } else {
                idx++;
            }
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
        int pos2 = pos;
        int pos3 = 0;
        while (pos3 < trimmedAffix.length()) {
            int c = UTF16.charAt(trimmedAffix, pos3);
            int len = UTF16.getCharCount(c);
            if (PatternProps.isWhiteSpace(c)) {
                int len2 = len;
                int i = pos3;
                boolean literalMatch = false;
                while (pos2 < input.length()) {
                    int ic = UTF16.charAt(input, pos2);
                    if (ic != c) {
                        if (!isBidiMark(ic)) {
                            break;
                        }
                        pos2++;
                    } else {
                        literalMatch = true;
                        i += len2;
                        pos2 += len2;
                        if (i == trimmedAffix.length()) {
                            break;
                        }
                        c = UTF16.charAt(trimmedAffix, i);
                        len2 = UTF16.getCharCount(c);
                        if (!PatternProps.isWhiteSpace(c)) {
                            break;
                        }
                    }
                }
                int i2 = skipPatternWhiteSpace(trimmedAffix, i);
                int s = pos2;
                pos2 = skipUWhiteSpace(input, pos2);
                if (pos2 == s && !literalMatch) {
                    return -1;
                }
                pos3 = skipUWhiteSpace(trimmedAffix, i2);
            } else {
                int i3 = pos3;
                boolean match = false;
                while (pos2 < input.length()) {
                    int ic2 = UTF16.charAt(input, pos2);
                    if (match || !equalWithSignCompatibility(ic2, c)) {
                        if (!isBidiMark(ic2)) {
                            break;
                        }
                        pos2++;
                    } else {
                        i3 += len;
                        pos2 += len;
                        match = true;
                    }
                }
                if (!match) {
                    return -1;
                }
                pos3 = i3;
            }
        }
        return pos2 - start;
    }

    private static boolean equalWithSignCompatibility(int lhs, int rhs) {
        return lhs == rhs || (minusSigns.contains(lhs) && minusSigns.contains(rhs)) || (plusSigns.contains(lhs) && plusSigns.contains(rhs));
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
        int pos2;
        int i;
        String str = affixPat;
        String str2 = text;
        int start = pos;
        char c = 0;
        int pos3 = pos;
        int i2 = 0;
        while (i2 < affixPat.length() && pos3 >= 0) {
            int i3 = i2 + 1;
            int i4 = str.charAt(i2);
            if (i4 == 39) {
                while (true) {
                    int j = str.indexOf(39, i3);
                    if (j == i3) {
                        pos3 = match(str2, pos3, 39);
                        i = j + 1;
                        break;
                    } else if (j > i3) {
                        pos3 = match(str2, pos3, str.substring(i3, j));
                        i = j + 1;
                        if (i >= affixPat.length() || str.charAt(i) != '\'') {
                            break;
                        }
                        pos3 = match(str2, pos3, 39);
                        i3 = i + 1;
                    } else {
                        throw new RuntimeException();
                    }
                }
                i2 = i;
            } else {
                String affix = null;
                if (i4 == 37) {
                    int i5 = type;
                    affix = this.symbols.getPercentString();
                } else if (i4 == 43) {
                    int i6 = type;
                    affix = this.symbols.getPlusSignString();
                } else if (i4 == 45) {
                    int i7 = type;
                    affix = this.symbols.getMinusSignString();
                } else if (i4 != 164) {
                    if (i4 == 8240) {
                        affix = this.symbols.getPerMillString();
                    }
                    int i8 = type;
                } else {
                    char c2 = 1;
                    if (((i3 >= affixPat.length() || str.charAt(i3) != 164) ? c : 1) != 0) {
                        i3++;
                    }
                    if (i3 >= affixPat.length() || str.charAt(i3) != 164) {
                        c2 = c;
                    }
                    if (c2 != 0) {
                        i3++;
                    }
                    ULocale uloc = getLocale(ULocale.VALID_LOCALE);
                    if (uloc == null) {
                        uloc = this.symbols.getLocale(ULocale.VALID_LOCALE);
                    }
                    ParsePosition ppos = new ParsePosition(pos3);
                    String iso = Currency.parse(uloc, str2, type, ppos);
                    if (iso != null) {
                        if (currency != null) {
                            currency[c] = Currency.getInstance(iso);
                        } else if (iso.compareTo(getEffectiveCurrency().getCurrencyCode()) != 0) {
                            pos2 = -1;
                        }
                        pos2 = ppos.getIndex();
                    } else {
                        pos2 = -1;
                    }
                    i2 = i3;
                    c = 0;
                }
                if (affix != null) {
                    pos2 = match(str2, pos3, affix);
                    i2 = i3;
                    c = 0;
                } else {
                    pos2 = match(str2, pos3, i4);
                    if (PatternProps.isWhiteSpace(i4)) {
                        i2 = skipPatternWhiteSpace(str, i3);
                    } else {
                        i2 = i3;
                    }
                    c = 0;
                }
            }
        }
        int i9 = type;
        return pos3 - start;
    }

    static final int match(String text, int pos, int ch) {
        if (pos < 0 || pos >= text.length()) {
            return -1;
        }
        int pos2 = skipBidiMarks(text, pos);
        if (PatternProps.isWhiteSpace(ch)) {
            int s = pos2;
            int pos3 = skipPatternWhiteSpace(text, pos2);
            if (pos3 == s) {
                return -1;
            }
            return pos3;
        } else if (pos2 >= text.length() || UTF16.charAt(text, pos2) != ch) {
            return -1;
        } else {
            return skipBidiMarks(text, UTF16.getCharCount(ch) + pos2);
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
        if (!this.symbols.getCurrencySymbol().equals(def.getCurrencySymbol()) || !this.symbols.getInternationalCurrencySymbol().equals(def.getInternationalCurrencySymbol())) {
            setCurrency(null);
        } else {
            setCurrency(Currency.getInstance(this.symbols.getULocale()));
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
        if (newValue != 0) {
            this.multiplier = newValue;
            return;
        }
        throw new IllegalArgumentException("Bad multiplier: " + newValue);
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
        if (i >= 0) {
            if (i == 0) {
                setInternalRoundingIncrement(null);
            } else {
                setInternalRoundingIncrement(newValue);
            }
            resetActualRounding();
            return;
        }
        throw new IllegalArgumentException("Illegal rounding increment");
    }

    public void setRoundingIncrement(double newValue) {
        if (newValue >= 0.0d) {
            if (newValue == 0.0d) {
                setInternalRoundingIncrement(null);
            } else {
                setInternalRoundingIncrement(android.icu.math.BigDecimal.valueOf(newValue));
            }
            resetActualRounding();
            return;
        }
        throw new IllegalArgumentException("Illegal rounding increment");
    }

    public int getRoundingMode() {
        return this.roundingMode;
    }

    public void setRoundingMode(int roundingMode2) {
        if (roundingMode2 < 0 || roundingMode2 > 7) {
            throw new IllegalArgumentException("Invalid rounding mode: " + roundingMode2);
        }
        this.roundingMode = roundingMode2;
        resetActualRounding();
    }

    public int getFormatWidth() {
        return this.formatWidth;
    }

    public void setFormatWidth(int width) {
        if (width >= 0) {
            this.formatWidth = width;
            return;
        }
        throw new IllegalArgumentException("Illegal format width");
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
        if (minExpDig >= 1) {
            this.minExponentDigits = minExpDig;
            return;
        }
        throw new IllegalArgumentException("Exponent digits must be >= 1");
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
        java.math.MathContext mathContext2 = null;
        try {
            if (this.mathContext != null) {
                mathContext2 = new java.math.MathContext(this.mathContext.getDigits(), RoundingMode.valueOf(this.mathContext.getRoundingMode()));
            }
            return mathContext2;
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
        CurrencyPluralInfo currencyPluralInfo2 = null;
        try {
            if (this.currencyPluralInfo != null) {
                currencyPluralInfo2 = (CurrencyPluralInfo) this.currencyPluralInfo.clone();
            }
            return currencyPluralInfo2;
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
            DecimalFormat_ICU58_Android other = (DecimalFormat_ICU58_Android) super.clone();
            other.symbols = (DecimalFormatSymbols) this.symbols.clone();
            other.digitList = new DigitList_Android();
            if (this.currencyPluralInfo != null) {
                other.currencyPluralInfo = (CurrencyPluralInfo) this.currencyPluralInfo.clone();
            }
            other.attributes = new ArrayList<>();
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
        DecimalFormat_ICU58_Android other = (DecimalFormat_ICU58_Android) obj;
        if (this.currencySignCount == other.currencySignCount && ((this.style != 6 || (equals(this.posPrefixPattern, other.posPrefixPattern) && equals(this.posSuffixPattern, other.posSuffixPattern) && equals(this.negPrefixPattern, other.negPrefixPattern) && equals(this.negSuffixPattern, other.negSuffixPattern))) && this.multiplier == other.multiplier && this.groupingSize == other.groupingSize && this.groupingSize2 == other.groupingSize2 && this.decimalSeparatorAlwaysShown == other.decimalSeparatorAlwaysShown && this.useExponentialNotation == other.useExponentialNotation && ((!this.useExponentialNotation || this.minExponentDigits == other.minExponentDigits) && this.useSignificantDigits == other.useSignificantDigits && ((!this.useSignificantDigits || (this.minSignificantDigits == other.minSignificantDigits && this.maxSignificantDigits == other.maxSignificantDigits)) && this.symbols.equals(other.symbols) && Utility.objectEquals(this.currencyPluralInfo, other.currencyPluralInfo) && this.currencyUsage.equals(other.currencyUsage))))) {
            z = true;
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
        String s;
        String str;
        int i;
        buffer.setLength(0);
        int i2 = 0;
        while (i2 < pattern.length()) {
            int i3 = i2 + 1;
            char c = pattern.charAt(i2);
            if (c == '\'') {
                while (true) {
                    int j = pattern.indexOf(39, i3);
                    if (j == i3) {
                        buffer.append('\'');
                        i = j + 1;
                        break;
                    } else if (j > i3) {
                        buffer.append(pattern.substring(i3, j));
                        i = j + 1;
                        if (i >= pattern.length() || pattern.charAt(i) != '\'') {
                            break;
                        }
                        buffer.append('\'');
                        i3 = i + 1;
                    } else {
                        throw new RuntimeException();
                    }
                }
                i2 = i;
            } else {
                if (c == '%') {
                    buffer.append(this.symbols.getPercentString());
                } else if (c == '-') {
                    buffer.append(this.symbols.getMinusSignString());
                } else if (c == 164) {
                    boolean intl = i3 < pattern.length() && pattern.charAt(i3) == 164;
                    boolean plural = false;
                    if (intl) {
                        i3++;
                        if (i3 < pattern.length() && pattern.charAt(i3) == 164) {
                            plural = true;
                            intl = false;
                            i3++;
                        }
                    }
                    Currency currency = getCurrency();
                    if (currency == null) {
                        if (intl) {
                            str = this.symbols.getInternationalCurrencySymbol();
                        } else {
                            str = this.symbols.getCurrencySymbol();
                        }
                        s = str;
                    } else if (plural && pluralCount != null) {
                        s = currency.getName(this.symbols.getULocale(), 2, pluralCount, (boolean[]) null);
                    } else if (!intl) {
                        s = currency.getName(this.symbols.getULocale(), 0, (boolean[]) null);
                    } else {
                        s = currency.getCurrencyCode();
                    }
                    buffer.append(s);
                } else if (c != 8240) {
                    buffer.append(c);
                } else {
                    buffer.append(this.symbols.getPerMillString());
                }
                i2 = i3;
            }
        }
    }

    private int appendAffix(StringBuffer buf, boolean isNegative, boolean isPrefix, FieldPosition fieldPosition, boolean parseAttr) {
        String affix;
        String pattern;
        String affixPat;
        StringBuffer stringBuffer = buf;
        FieldPosition fieldPosition2 = fieldPosition;
        if (this.currencyChoice != null) {
            if (isPrefix) {
                affixPat = isNegative ? this.negPrefixPattern : this.posPrefixPattern;
            } else {
                affixPat = isNegative ? this.negSuffixPattern : this.posSuffixPattern;
            }
            StringBuffer affixBuf = new StringBuffer();
            expandAffix(affixPat, null, affixBuf);
            stringBuffer.append(affixBuf);
            return affixBuf.length();
        }
        if (isPrefix) {
            affix = isNegative ? this.negativePrefix : this.positivePrefix;
            pattern = isNegative ? this.negPrefixPattern : this.posPrefixPattern;
        } else {
            affix = isNegative ? this.negativeSuffix : this.positiveSuffix;
            pattern = isNegative ? this.negSuffixPattern : this.posSuffixPattern;
        }
        String affix2 = affix;
        String pattern2 = pattern;
        if (parseAttr) {
            int offset = affix2.indexOf(this.symbols.getCurrencySymbol());
            if (offset > -1) {
                formatAffix2Attribute(isPrefix, NumberFormat.Field.CURRENCY, stringBuffer, offset, this.symbols.getCurrencySymbol().length());
            }
            int offset2 = affix2.indexOf(this.symbols.getMinusSignString());
            if (offset2 > -1) {
                formatAffix2Attribute(isPrefix, NumberFormat.Field.SIGN, stringBuffer, offset2, this.symbols.getMinusSignString().length());
            }
            int offset3 = affix2.indexOf(this.symbols.getPercentString());
            if (offset3 > -1) {
                formatAffix2Attribute(isPrefix, NumberFormat.Field.PERCENT, stringBuffer, offset3, this.symbols.getPercentString().length());
            }
            int offset4 = affix2.indexOf(this.symbols.getPerMillString());
            if (offset4 > -1) {
                formatAffix2Attribute(isPrefix, NumberFormat.Field.PERMILLE, stringBuffer, offset4, this.symbols.getPerMillString().length());
            }
            int offset5 = pattern2.indexOf("¤¤¤");
            if (offset5 > -1) {
                formatAffix2Attribute(isPrefix, NumberFormat.Field.CURRENCY, stringBuffer, offset5, affix2.length() - offset5);
            }
        }
        if (fieldPosition.getFieldAttribute() == NumberFormat.Field.SIGN) {
            String sign = isNegative ? this.symbols.getMinusSignString() : this.symbols.getPlusSignString();
            int firstPos = affix2.indexOf(sign);
            if (firstPos > -1) {
                int startPos = stringBuffer.length() + firstPos;
                fieldPosition2.setBeginIndex(startPos);
                fieldPosition2.setEndIndex(sign.length() + startPos);
            }
        } else if (fieldPosition.getFieldAttribute() == NumberFormat.Field.PERCENT) {
            int firstPos2 = affix2.indexOf(this.symbols.getPercentString());
            if (firstPos2 > -1) {
                int startPos2 = stringBuffer.length() + firstPos2;
                fieldPosition2.setBeginIndex(startPos2);
                fieldPosition2.setEndIndex(this.symbols.getPercentString().length() + startPos2);
            }
        } else if (fieldPosition.getFieldAttribute() == NumberFormat.Field.PERMILLE) {
            int firstPos3 = affix2.indexOf(this.symbols.getPerMillString());
            if (firstPos3 > -1) {
                int startPos3 = stringBuffer.length() + firstPos3;
                fieldPosition2.setBeginIndex(startPos3);
                fieldPosition2.setEndIndex(this.symbols.getPerMillString().length() + startPos3);
            }
        } else if (fieldPosition.getFieldAttribute() == NumberFormat.Field.CURRENCY) {
            if (affix2.indexOf(this.symbols.getCurrencySymbol()) > -1) {
                String aff = this.symbols.getCurrencySymbol();
                int start = stringBuffer.length() + affix2.indexOf(aff);
                fieldPosition2.setBeginIndex(start);
                fieldPosition2.setEndIndex(aff.length() + start);
            } else if (affix2.indexOf(this.symbols.getInternationalCurrencySymbol()) > -1) {
                String aff2 = this.symbols.getInternationalCurrencySymbol();
                int start2 = stringBuffer.length() + affix2.indexOf(aff2);
                fieldPosition2.setBeginIndex(start2);
                fieldPosition2.setEndIndex(aff2.length() + start2);
            } else if (pattern2.indexOf("¤¤¤") > -1) {
                fieldPosition2.setBeginIndex(stringBuffer.length() + pattern2.indexOf("¤¤¤"));
                fieldPosition2.setEndIndex(stringBuffer.length() + affix2.length());
            }
        }
        stringBuffer.append(affix2);
        return affix2.length();
    }

    private void formatAffix2Attribute(boolean isPrefix, NumberFormat.Field fieldType, StringBuffer buf, int offset, int symbolSize) {
        int begin = offset;
        if (!isPrefix) {
            begin += buf.length();
        }
        addAttribute(fieldType, begin, begin + symbolSize);
    }

    private void addAttribute(NumberFormat.Field field, int begin, int end) {
        FieldPosition pos = new FieldPosition(field);
        pos.setBeginIndex(begin);
        pos.setEndIndex(end);
        this.attributes.add(pos);
    }

    public AttributedCharacterIterator formatToCharacterIterator(Object obj) {
        return formatToCharacterIterator(obj, NULL_UNIT);
    }

    /* access modifiers changed from: package-private */
    public AttributedCharacterIterator formatToCharacterIterator(Object obj, Unit unit) {
        if (obj instanceof Number) {
            Number number = (Number) obj;
            StringBuffer text = new StringBuffer();
            unit.writePrefix(text);
            this.attributes.clear();
            int i = 0;
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
            while (true) {
                int i2 = i;
                if (i2 >= this.attributes.size()) {
                    return as.getIterator();
                }
                FieldPosition pos = this.attributes.get(i2);
                Format.Field attribute = pos.getFieldAttribute();
                as.addAttribute(attribute, attribute, pos.getBeginIndex(), pos.getEndIndex());
                i = i2 + 1;
            }
        } else {
            throw new IllegalArgumentException();
        }
    }

    private void appendAffixPattern(StringBuffer buffer, boolean isNegative, boolean isPrefix, boolean localized) {
        String affixPat;
        String affix;
        if (isPrefix) {
            affixPat = isNegative ? this.negPrefixPattern : this.posPrefixPattern;
        } else {
            affixPat = isNegative ? this.negSuffixPattern : this.posSuffixPattern;
        }
        int i = 0;
        if (affixPat == null) {
            if (isPrefix) {
                affix = isNegative ? this.negativePrefix : this.positivePrefix;
            } else {
                affix = isNegative ? this.negativeSuffix : this.positiveSuffix;
            }
            buffer.append('\'');
            while (i < affix.length()) {
                char ch = affix.charAt(i);
                if (ch == '\'') {
                    buffer.append(ch);
                }
                buffer.append(ch);
                i++;
            }
            buffer.append('\'');
            return;
        }
        if (!localized) {
            buffer.append(affixPat);
        } else {
            while (i < affixPat.length()) {
                char ch2 = affixPat.charAt(i);
                if (ch2 == '%') {
                    ch2 = this.symbols.getPercent();
                } else if (ch2 == '\'') {
                    int j = affixPat.indexOf(39, i + 1);
                    if (j >= 0) {
                        buffer.append(affixPat.substring(i, j + 1));
                        i = j;
                        i++;
                    } else {
                        throw new IllegalArgumentException("Malformed affix pattern: " + affixPat);
                    }
                } else if (ch2 == '-') {
                    ch2 = this.symbols.getMinusSign();
                } else if (ch2 == 8240) {
                    ch2 = this.symbols.getPerMill();
                }
                if (ch2 == this.symbols.getDecimalSeparator() || ch2 == this.symbols.getGroupingSeparator()) {
                    buffer.append('\'');
                    buffer.append(ch2);
                    buffer.append('\'');
                    i++;
                } else {
                    buffer.append(ch2);
                    i++;
                }
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:89:0x014d  */
    /* JADX WARNING: Removed duplicated region for block: B:90:0x014f  */
    private String toPattern(boolean localized) {
        String padSpec;
        char sigDigit;
        int minDig;
        int maxDig;
        int i;
        char zero;
        char digit;
        boolean z;
        int i2;
        char c;
        char c2;
        int maxDig2;
        char c3;
        boolean z2 = localized;
        StringBuffer result = new StringBuffer();
        char zero2 = z2 ? this.symbols.getZeroDigit() : PATTERN_ZERO_DIGIT;
        char digit2 = z2 ? this.symbols.getDigit() : PATTERN_DIGIT;
        char sigDigit2 = 0;
        boolean useSigDig = areSignificantDigitsUsed();
        if (useSigDig) {
            sigDigit2 = z2 ? this.symbols.getSignificantDigit() : PATTERN_SIGNIFICANT_DIGIT;
        }
        char group = z2 ? this.symbols.getGroupingSeparator() : PATTERN_GROUPING_SEPARATOR;
        int roundingDecimalPos = 0;
        String roundingDigits = null;
        int padPos = this.formatWidth > 0 ? this.padPosition : -1;
        int i3 = 2;
        if (this.formatWidth > 0) {
            StringBuffer stringBuffer = new StringBuffer(2);
            if (z2) {
                c3 = this.symbols.getPadEscape();
            } else {
                c3 = PATTERN_PAD_ESCAPE;
            }
            stringBuffer.append(c3);
            stringBuffer.append(this.pad);
            padSpec = stringBuffer.toString();
        } else {
            padSpec = null;
        }
        if (this.roundingIncrementICU != null) {
            int i4 = this.roundingIncrementICU.scale();
            roundingDigits = this.roundingIncrementICU.movePointRight(i4).toString();
            roundingDecimalPos = roundingDigits.length() - i4;
        }
        boolean z3 = false;
        int part = 0;
        while (true) {
            if (part >= i3) {
                char c4 = sigDigit2;
                int i5 = padPos;
                char sigDigit3 = digit2;
                break;
            }
            if (padPos == 0) {
                result.append(padSpec);
            }
            appendAffixPattern(result, part != 0 ? true : z3, true, z2);
            if (padPos == 1) {
                result.append(padSpec);
            }
            int sub0Start = result.length();
            int g = isGroupingUsed() ? Math.max(z3 ? 1 : 0, this.groupingSize) : z3;
            if (g <= 0 || this.groupingSize2 <= 0) {
                sigDigit = sigDigit2;
            } else {
                sigDigit = sigDigit2;
                if (this.groupingSize2 != this.groupingSize) {
                    g += this.groupingSize2;
                }
            }
            int maxSigDig = 0;
            if (useSigDig) {
                minDig = getMinimumSignificantDigits();
                int maximumSignificantDigits = getMaximumSignificantDigits();
                maxSigDig = maximumSignificantDigits;
                maxDig = maximumSignificantDigits;
            } else {
                minDig = getMinimumIntegerDigits();
                maxDig = getMaximumIntegerDigits();
            }
            int padPos2 = padPos;
            int maxSigDig2 = maxSigDig;
            char digit3 = digit2;
            if (this.useExponentialNotation != 0) {
                if (maxDig > 8) {
                    maxDig = 1;
                }
            } else if (useSigDig) {
                maxDig = Math.max(maxDig, g + 1);
            } else {
                maxDig = Math.max(Math.max(g, getMinimumIntegerDigits()), roundingDecimalPos) + 1;
            }
            int i6 = maxDig;
            while (i > 0) {
                int g2 = g;
                if (this.useExponentialNotation == 0 && i < maxDig && isGroupingPosition(i)) {
                    result.append(group);
                }
                if (useSigDig) {
                    result.append((maxSigDig2 < i || i <= maxSigDig2 - minDig) ? digit3 : sigDigit);
                    maxDig2 = maxDig;
                } else {
                    if (roundingDigits != null) {
                        int pos = roundingDecimalPos - i;
                        if (pos >= 0) {
                            maxDig2 = maxDig;
                            if (pos < roundingDigits.length()) {
                                result.append((char) ((roundingDigits.charAt(pos) - '0') + zero2));
                            }
                            result.append(i > minDig ? zero2 : digit3);
                        }
                    }
                    maxDig2 = maxDig;
                    result.append(i > minDig ? zero2 : digit3);
                }
                i6 = i - 1;
                g = g2;
                maxDig = maxDig2;
            }
            int maxDig3 = maxDig;
            int i7 = g;
            if (!useSigDig) {
                if (getMaximumFractionDigits() > 0 || this.decimalSeparatorAlwaysShown) {
                    if (z2) {
                        c2 = this.symbols.getDecimalSeparator();
                    } else {
                        c2 = PATTERN_DECIMAL_SEPARATOR;
                    }
                    result.append(c2);
                }
                int pos2 = roundingDecimalPos;
                i = 0;
                while (i < getMaximumFractionDigits()) {
                    if (roundingDigits == null || pos2 >= roundingDigits.length()) {
                        result.append(i < getMinimumFractionDigits() ? zero2 : digit3);
                    } else {
                        if (pos2 < 0) {
                            c = zero2;
                        } else {
                            c = (char) ((roundingDigits.charAt(pos2) - '0') + zero2);
                        }
                        result.append(c);
                        pos2++;
                    }
                    i++;
                }
            }
            if (this.useExponentialNotation) {
                if (z2) {
                    result.append(this.symbols.getExponentSeparator());
                } else {
                    result.append(PATTERN_EXPONENT);
                }
                if (this.exponentSignAlwaysShown) {
                    result.append(z2 ? this.symbols.getPlusSign() : PATTERN_PLUS_SIGN);
                }
                int i8 = 0;
                while (i < this.minExponentDigits) {
                    result.append(zero2);
                    i8 = i + 1;
                }
            }
            if (padSpec == null || this.useExponentialNotation) {
                zero = zero2;
                int i9 = i;
                digit = digit3;
                z = true;
            } else {
                int length = (this.formatWidth - result.length()) + sub0Start;
                if (part == 0) {
                    zero = zero2;
                    i2 = this.positivePrefix.length() + this.positiveSuffix.length();
                } else {
                    zero = zero2;
                    i2 = this.negativeSuffix.length() + this.negativePrefix.length();
                }
                int add = length - i2;
                while (true) {
                    int add2 = add;
                    if (add2 <= 0) {
                        break;
                    }
                    char c5 = digit3;
                    result.insert(sub0Start, c5);
                    int maxDig4 = maxDig3 + 1;
                    int add3 = add2 - 1;
                    int i10 = i;
                    if (add3 > 1 && isGroupingPosition(maxDig4)) {
                        result.insert(sub0Start, group);
                        add3--;
                    }
                    digit3 = c5;
                    maxDig3 = maxDig4;
                    i = i10;
                    add = add3;
                }
                int i11 = i;
                digit = digit3;
                z = true;
            }
            int padPos3 = padPos2;
            if (padPos3 == 2) {
                result.append(padSpec);
            }
            if (part == 0) {
                z = false;
            }
            appendAffixPattern(result, z, false, z2);
            if (padPos3 == 3) {
                result.append(padSpec);
            }
            if (part == 0) {
                if (this.negativeSuffix.equals(this.positiveSuffix)) {
                    String str = this.negativePrefix;
                    StringBuilder sb = new StringBuilder();
                    int i12 = sub0Start;
                    sb.append(PATTERN_MINUS_SIGN);
                    sb.append(this.positivePrefix);
                    if (str.equals(sb.toString())) {
                        break;
                    }
                }
                result.append(z2 ? this.symbols.getPatternSeparator() : PATTERN_SEPARATOR);
            }
            part++;
            padPos = padPos3;
            digit2 = digit;
            sigDigit2 = sigDigit;
            zero2 = zero;
            i3 = 2;
            z3 = false;
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

    /* JADX WARNING: Code restructure failed: missing block: B:240:0x05ad, code lost:
        if (r12 <= (r2 + r27)) goto L_0x05b9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:250:0x05ca, code lost:
        if (r14 > 2) goto L_0x05d4;
     */
    /* JADX WARNING: Removed duplicated region for block: B:121:0x0379  */
    /* JADX WARNING: Removed duplicated region for block: B:125:0x0392  */
    /* JADX WARNING: Removed duplicated region for block: B:255:0x05db  */
    /* JADX WARNING: Removed duplicated region for block: B:268:0x060d  */
    /* JADX WARNING: Removed duplicated region for block: B:270:0x0619  */
    /* JADX WARNING: Removed duplicated region for block: B:335:0x0721  */
    private void applyPatternWithoutExpandAffix(String pattern, boolean localized) {
        char plus;
        String exponent;
        int sub0Limit;
        int start;
        char zeroDigit;
        char sigDigit;
        char percent;
        char separator;
        int part;
        char sigDigit2;
        String exponent2;
        int sigDigitCount;
        int groupingCount;
        char padEscape;
        int decimalPos;
        byte expDigits;
        char multpl;
        int padPos;
        char minus;
        char perMill;
        char groupingSeparator;
        char plus2;
        int plus3;
        String exponent3;
        char decimalSeparator;
        int sub0Start;
        int padPos2;
        int pos;
        char digit;
        boolean expSignAlways;
        int sub0Start2;
        int i;
        int i2;
        char padPos3;
        char zeroDigit2;
        char sigDigit3;
        char percent2;
        int part2;
        char separator2;
        char padEscape2;
        char minus2;
        char perMill2;
        String exponent4;
        char sigDigit4;
        int sigDigitCount2;
        int padPos4;
        char zeroDigit3;
        char sigDigit5;
        int groupingCount2;
        int decimalPos2;
        int incrementPos;
        StringBuilder affix;
        char zeroDigit4;
        char sigDigit6;
        byte expDigits2;
        int groupingCount3;
        int sub0Limit2;
        int padPos5;
        int sigDigitCount3;
        int sigDigitCount4;
        int groupingCount4;
        char c;
        StringBuilder affix2;
        int i3;
        char zeroDigit5;
        char sigDigit7;
        char padPos6;
        int padPos7;
        char separator3;
        int padPos8;
        char percent3;
        char separator4;
        int part3;
        char padEscape3;
        char minus3;
        char perMill3;
        char separator5;
        int i4;
        char percent4;
        String str = pattern;
        char zeroDigit6 = PATTERN_ZERO_DIGIT;
        char sigDigit8 = PATTERN_SIGNIFICANT_DIGIT;
        char groupingSeparator2 = PATTERN_GROUPING_SEPARATOR;
        char decimalSeparator2 = PATTERN_DECIMAL_SEPARATOR;
        char percent5 = PATTERN_PERCENT;
        char perMill4 = PATTERN_PER_MILLE;
        char digit2 = PATTERN_DIGIT;
        char separator6 = PATTERN_SEPARATOR;
        String exponent5 = String.valueOf(PATTERN_EXPONENT);
        char plus4 = PATTERN_PLUS_SIGN;
        char padEscape4 = PATTERN_PAD_ESCAPE;
        char minus4 = PATTERN_MINUS_SIGN;
        if (localized) {
            zeroDigit6 = this.symbols.getZeroDigit();
            sigDigit8 = this.symbols.getSignificantDigit();
            groupingSeparator2 = this.symbols.getGroupingSeparator();
            decimalSeparator2 = this.symbols.getDecimalSeparator();
            percent5 = this.symbols.getPercent();
            perMill4 = this.symbols.getPerMill();
            digit2 = this.symbols.getDigit();
            separator6 = this.symbols.getPatternSeparator();
            exponent5 = this.symbols.getExponentSeparator();
            plus4 = this.symbols.getPlusSign();
            padEscape4 = this.symbols.getPadEscape();
            minus4 = this.symbols.getMinusSign();
        }
        char nineDigit = (char) (zeroDigit6 + 9);
        int pos2 = 0;
        boolean gotNegative = false;
        int part4 = 0;
        while (true) {
            plus = plus4;
            exponent = exponent5;
            int part5 = part4;
            if (part5 >= 2 || pos2 >= pattern.length()) {
                char c2 = sigDigit8;
                char c3 = groupingSeparator2;
                char c4 = decimalSeparator2;
                char c5 = percent5;
                char c6 = perMill4;
                char c7 = digit2;
                char c8 = separator6;
                char c9 = padEscape4;
                char c10 = minus4;
                int i5 = pos2;
                char c11 = plus;
                String str2 = exponent;
            } else {
                int sub0Limit3 = 0;
                int sub2Limit = 0;
                StringBuilder prefix = new StringBuilder();
                int pos3 = pos2;
                int digitLeftCount = 0;
                int zeroDigitCount = 0;
                int digitRightCount = 0;
                int incrementPos2 = -1;
                StringBuilder affix3 = prefix;
                StringBuilder prefix2 = prefix;
                char padEscape5 = padEscape4;
                StringBuilder suffix = new StringBuilder();
                int sub0Start3 = 0;
                int subpart = 1;
                int pos4 = pos3;
                int decimalPos3 = -1;
                char multpl2 = 1;
                int sigDigitCount5 = 0;
                int groupingCount5 = -1;
                int groupingCount22 = -1;
                int padPos9 = 65535;
                int padChar = 0;
                long incrementVal = 0;
                byte expDigits3 = -1;
                boolean expSignAlways2 = false;
                int currencySignCnt = 0;
                StringBuilder affix4 = affix3;
                while (true) {
                    start = pos3;
                    char minus5 = minus4;
                    char perMill5 = perMill4;
                    if (sub0Limit < pattern.length()) {
                        char ch = str.charAt(sub0Limit);
                        switch (subpart) {
                            case 0:
                                percent2 = percent5;
                                separator2 = separator6;
                                part2 = part5;
                                padEscape2 = padEscape5;
                                char separator7 = multpl2;
                                int padPos10 = padPos9;
                                minus2 = minus5;
                                perMill2 = perMill5;
                                if (ch == digit2) {
                                    if (zeroDigitCount <= 0) {
                                        sigDigitCount4 = sigDigitCount5;
                                        if (sigDigitCount4 <= 0) {
                                            digitLeftCount++;
                                            groupingCount4 = groupingCount5;
                                            if (groupingCount4 < 0) {
                                                affix2 = affix4;
                                                i3 = decimalPos3;
                                                if (i3 < 0) {
                                                    c = separator7;
                                                    zeroDigit5 = zeroDigit6;
                                                    sigDigit7 = sigDigit8;
                                                    sigDigitCount5 = sigDigitCount4;
                                                    padPos6 = padPos10;
                                                    groupingCount5 = (byte) (groupingCount4 + 1);
                                                    decimalPos3 = i3;
                                                    sigDigit4 = plus;
                                                    exponent4 = exponent;
                                                    affix4 = affix2;
                                                    multpl2 = c;
                                                    break;
                                                } else {
                                                    c = separator7;
                                                }
                                            } else {
                                                c = separator7;
                                                affix2 = affix4;
                                                i3 = decimalPos3;
                                            }
                                            zeroDigit5 = zeroDigit6;
                                            sigDigit7 = sigDigit8;
                                            sigDigitCount5 = sigDigitCount4;
                                            padPos6 = padPos10;
                                            groupingCount5 = groupingCount4;
                                            decimalPos3 = i3;
                                            sigDigit4 = plus;
                                            exponent4 = exponent;
                                            affix4 = affix2;
                                            multpl2 = c;
                                        }
                                    } else {
                                        sigDigitCount4 = sigDigitCount5;
                                    }
                                    digitRightCount++;
                                    groupingCount4 = groupingCount5;
                                    if (groupingCount4 < 0) {
                                    }
                                    zeroDigit5 = zeroDigit6;
                                    sigDigit7 = sigDigit8;
                                    sigDigitCount5 = sigDigitCount4;
                                    padPos6 = padPos10;
                                    groupingCount5 = groupingCount4;
                                    decimalPos3 = i3;
                                    sigDigit4 = plus;
                                    exponent4 = exponent;
                                    affix4 = affix2;
                                    multpl2 = c;
                                } else {
                                    char c12 = separator7;
                                    StringBuilder sb = affix4;
                                    int sigDigitCount6 = sigDigitCount5;
                                    int i6 = groupingCount5;
                                    int decimalPos4 = decimalPos3;
                                    if ((ch < zeroDigit6 || ch > nineDigit) && ch != sigDigit8) {
                                        sigDigitCount2 = sigDigitCount6;
                                        padPos4 = padPos10;
                                        if (ch == groupingSeparator2) {
                                            if (ch == '\'' && sub0Limit + 1 < pattern.length()) {
                                                char after = str.charAt(sub0Limit + 1);
                                                if (after != digit2 && (after < zeroDigit6 || after > nineDigit)) {
                                                    if (after == '\'') {
                                                        sub0Limit++;
                                                    } else if (i6 < 0) {
                                                        zeroDigit3 = zeroDigit6;
                                                        sigDigit5 = sigDigit8;
                                                        subpart = 3;
                                                    } else {
                                                        affix = suffix;
                                                        zeroDigit4 = zeroDigit6;
                                                        sigDigit6 = sigDigit8;
                                                        groupingCount3 = i6;
                                                        sub0Limit3 = sub0Limit;
                                                        decimalPos3 = decimalPos4;
                                                        sub0Limit2 = sub0Limit - 1;
                                                        sigDigit4 = plus;
                                                        exponent4 = exponent;
                                                        multpl2 = c12;
                                                        padPos5 = padPos4;
                                                        sigDigitCount3 = sigDigitCount2;
                                                        subpart = 2;
                                                    }
                                                }
                                            }
                                            if (decimalPos4 >= 0) {
                                                patternError("Grouping separator after decimal", str);
                                            }
                                            groupingCount22 = i6;
                                            groupingCount2 = 0;
                                            zeroDigit3 = zeroDigit6;
                                            sigDigit5 = sigDigit8;
                                            decimalPos2 = decimalPos4;
                                            sigDigit4 = plus;
                                            exponent4 = exponent;
                                            affix4 = sb;
                                            multpl2 = c12;
                                            padPos9 = padPos4;
                                            sigDigitCount5 = sigDigitCount2;
                                            break;
                                        } else if (ch == decimalSeparator2) {
                                            if (decimalPos4 >= 0) {
                                                patternError("Multiple decimal separators", str);
                                            }
                                            zeroDigit3 = zeroDigit6;
                                            sigDigit5 = sigDigit8;
                                            decimalPos2 = digitLeftCount + zeroDigitCount + digitRightCount;
                                            groupingCount2 = i6;
                                            sigDigit4 = plus;
                                            exponent4 = exponent;
                                            affix4 = sb;
                                            multpl2 = c12;
                                            padPos9 = padPos4;
                                            sigDigitCount5 = sigDigitCount2;
                                        } else {
                                            exponent4 = exponent;
                                            if (str.regionMatches(sub0Limit, exponent4, 0, exponent4.length())) {
                                                if (expDigits3 >= 0) {
                                                    patternError("Multiple exponential symbols", str);
                                                }
                                                if (i6 >= 0) {
                                                    patternError("Grouping separator in exponential", str);
                                                }
                                                sub0Limit += exponent4.length();
                                                if (sub0Limit < pattern.length()) {
                                                    sigDigit6 = sigDigit8;
                                                    sigDigit4 = plus;
                                                    if (str.charAt(sub0Limit) == sigDigit4) {
                                                        sub0Limit++;
                                                        expSignAlways2 = true;
                                                    }
                                                } else {
                                                    sigDigit6 = sigDigit8;
                                                    sigDigit4 = plus;
                                                }
                                                expDigits2 = 0;
                                                while (sub0Limit < pattern.length() && str.charAt(sub0Limit) == zeroDigit6) {
                                                    expDigits2 = (byte) (expDigits2 + 1);
                                                    sub0Limit++;
                                                }
                                                zeroDigit4 = zeroDigit6;
                                                if ((digitLeftCount + zeroDigitCount < 1 && sigDigitCount2 + digitRightCount < 1) || ((sigDigitCount2 > 0 && digitLeftCount > 0) || expDigits2 < 1)) {
                                                    patternError("Malformed exponential", str);
                                                }
                                            } else {
                                                zeroDigit4 = zeroDigit6;
                                                sigDigit6 = sigDigit8;
                                                sigDigit4 = plus;
                                                expDigits2 = expDigits3;
                                            }
                                            affix = suffix;
                                            expDigits3 = expDigits2;
                                            groupingCount3 = i6;
                                            sub0Limit3 = sub0Limit;
                                            decimalPos3 = decimalPos4;
                                            sub0Limit2 = sub0Limit - 1;
                                            multpl2 = c12;
                                            padPos5 = padPos4;
                                            sigDigitCount3 = sigDigitCount2;
                                            subpart = 2;
                                        }
                                        affix4 = affix;
                                        break;
                                    } else {
                                        if (digitRightCount > 0) {
                                            StringBuilder sb2 = new StringBuilder();
                                            padPos4 = padPos10;
                                            sb2.append("Unexpected '");
                                            sb2.append(ch);
                                            sb2.append('\'');
                                            patternError(sb2.toString(), str);
                                        } else {
                                            padPos4 = padPos10;
                                        }
                                        if (ch == sigDigit8) {
                                            sigDigitCount2 = sigDigitCount6 + 1;
                                        } else {
                                            zeroDigitCount++;
                                            if (ch != zeroDigit6) {
                                                int p = digitLeftCount + zeroDigitCount + digitRightCount;
                                                if (incrementPos2 >= 0) {
                                                    incrementPos = incrementPos2;
                                                    while (incrementPos < p) {
                                                        incrementVal *= 10;
                                                        incrementPos++;
                                                    }
                                                } else {
                                                    incrementPos = p;
                                                }
                                                incrementPos2 = incrementPos;
                                                sigDigitCount2 = sigDigitCount6;
                                                int i7 = p;
                                                incrementVal += (long) (ch - zeroDigit6);
                                            } else {
                                                sigDigitCount2 = sigDigitCount6;
                                            }
                                        }
                                        if (i6 < 0 || decimalPos4 >= 0) {
                                            zeroDigit3 = zeroDigit6;
                                            sigDigit5 = sigDigit8;
                                        } else {
                                            zeroDigit3 = zeroDigit6;
                                            sigDigit5 = sigDigit8;
                                            groupingCount2 = (byte) (i6 + 1);
                                            decimalPos2 = decimalPos4;
                                            sigDigit4 = plus;
                                            exponent4 = exponent;
                                            affix4 = sb;
                                            multpl2 = c12;
                                            padPos9 = padPos4;
                                            sigDigitCount5 = sigDigitCount2;
                                        }
                                    }
                                    groupingCount2 = i6;
                                    decimalPos2 = decimalPos4;
                                    sigDigit4 = plus;
                                    exponent4 = exponent;
                                    affix4 = sb;
                                    multpl2 = c12;
                                    padPos9 = padPos4;
                                    sigDigitCount5 = sigDigitCount2;
                                }
                                break;
                            case 1:
                            case 2:
                                char percent6 = percent5;
                                if (ch == digit2 || ch == groupingSeparator2 || ch == decimalSeparator2) {
                                    separator4 = separator6;
                                    part3 = part5;
                                    padEscape3 = padEscape5;
                                    separator3 = multpl2;
                                    padPos8 = padPos9;
                                    minus3 = minus5;
                                    perMill3 = perMill5;
                                    percent3 = percent6;
                                } else if ((ch < zeroDigit6 || ch > nineDigit) && ch != sigDigit8) {
                                    if (ch == 164) {
                                        boolean doubled = sub0Limit + 1 < pattern.length() && str.charAt(sub0Limit + 1) == 164;
                                        if (doubled) {
                                            sub0Limit++;
                                            affix4.append(ch);
                                            boolean z = doubled;
                                            if (sub0Limit + 1 >= pattern.length() || str.charAt(sub0Limit + 1) != 164) {
                                                i4 = 2;
                                            } else {
                                                sub0Limit++;
                                                affix4.append(ch);
                                                i4 = 3;
                                            }
                                        } else {
                                            boolean z2 = doubled;
                                            i4 = 1;
                                        }
                                        currencySignCnt = i4;
                                    } else if (ch == '\'') {
                                        if (sub0Limit + 1 >= pattern.length() || str.charAt(sub0Limit + 1) != '\'') {
                                            subpart += 2;
                                        } else {
                                            sub0Limit++;
                                            affix4.append(ch);
                                        }
                                    } else if (ch == separator6) {
                                        if (subpart == 1 || part5 == 1) {
                                            patternError("Unquoted special character '" + ch + '\'', str);
                                        }
                                        sub2Limit = sub0Limit;
                                        zeroDigit = zeroDigit6;
                                        sigDigit = sigDigit8;
                                        sub0Limit++;
                                        separator = separator6;
                                        part = part5;
                                        StringBuilder sb3 = affix4;
                                        sigDigit2 = plus;
                                        exponent2 = exponent;
                                        sigDigitCount = sigDigitCount5;
                                        groupingCount = groupingCount5;
                                        padEscape = padEscape5;
                                        decimalPos = decimalPos3;
                                        expDigits = expDigits3;
                                        multpl = multpl2;
                                        padPos = padPos9;
                                        minus = minus5;
                                        perMill = perMill5;
                                        percent = percent6;
                                        break;
                                    } else {
                                        char percent7 = percent6;
                                        if (ch != percent7) {
                                            char perMill6 = perMill5;
                                            if (ch == perMill6) {
                                                perMill2 = perMill6;
                                                separator5 = separator6;
                                                part2 = part5;
                                                padEscape2 = padEscape5;
                                                padPos7 = padPos9;
                                                minus2 = minus5;
                                            } else {
                                                perMill2 = perMill6;
                                                char minus6 = minus5;
                                                if (ch == minus6) {
                                                    ch = PATTERN_MINUS_SIGN;
                                                    percent2 = percent7;
                                                    minus2 = minus6;
                                                    separator2 = separator6;
                                                    part2 = part5;
                                                    padEscape2 = padEscape5;
                                                    padPos7 = padPos9;
                                                    affix4.append(ch);
                                                    zeroDigit2 = zeroDigit6;
                                                    sigDigit3 = sigDigit8;
                                                    padPos9 = padPos7;
                                                    sigDigit4 = plus;
                                                    exponent4 = exponent;
                                                    break;
                                                } else {
                                                    minus2 = minus6;
                                                    char minus7 = padEscape5;
                                                    if (ch == minus7) {
                                                        padEscape2 = minus7;
                                                        if (padPos9 >= 0) {
                                                            separator2 = separator6;
                                                            patternError("Multiple pad specifiers", str);
                                                        } else {
                                                            separator2 = separator6;
                                                        }
                                                        part2 = part5;
                                                        if (sub0Limit + 1 == pattern.length()) {
                                                            patternError("Invalid pad specifier", str);
                                                        }
                                                        int pos5 = sub0Limit + 1;
                                                        zeroDigit2 = zeroDigit6;
                                                        sigDigit3 = sigDigit8;
                                                        percent2 = percent7;
                                                        padPos9 = sub0Limit;
                                                        sub0Limit = pos5;
                                                        padChar = str.charAt(pos5);
                                                        sigDigit4 = plus;
                                                        exponent4 = exponent;
                                                    } else {
                                                        padEscape2 = minus7;
                                                        separator2 = separator6;
                                                        part2 = part5;
                                                        padPos8 = padPos9;
                                                        percent2 = percent7;
                                                        separator3 = multpl2;
                                                        multpl2 = separator3;
                                                        affix4.append(ch);
                                                        zeroDigit2 = zeroDigit6;
                                                        sigDigit3 = sigDigit8;
                                                        padPos9 = padPos7;
                                                        sigDigit4 = plus;
                                                        exponent4 = exponent;
                                                    }
                                                }
                                            }
                                        } else {
                                            separator5 = separator6;
                                            part2 = part5;
                                            padEscape2 = padEscape5;
                                            padPos7 = padPos9;
                                            minus2 = minus5;
                                            perMill2 = perMill5;
                                        }
                                        if (multpl2 != 1) {
                                            patternError("Too many percent/permille characters", str);
                                        }
                                        multpl2 = ch == percent7 ? 'd' : 1000;
                                        ch = ch == percent7 ? PATTERN_PERCENT : PATTERN_PER_MILLE;
                                        percent2 = percent7;
                                        affix4.append(ch);
                                        zeroDigit2 = zeroDigit6;
                                        sigDigit3 = sigDigit8;
                                        padPos9 = padPos7;
                                        sigDigit4 = plus;
                                        exponent4 = exponent;
                                    }
                                    separator2 = separator6;
                                    part2 = part5;
                                    padEscape2 = padEscape5;
                                    padPos7 = padPos9;
                                    minus2 = minus5;
                                    perMill2 = perMill5;
                                    percent2 = percent6;
                                    affix4.append(ch);
                                    zeroDigit2 = zeroDigit6;
                                    sigDigit3 = sigDigit8;
                                    padPos9 = padPos7;
                                    sigDigit4 = plus;
                                    exponent4 = exponent;
                                } else {
                                    separator4 = separator6;
                                    part3 = part5;
                                    padEscape3 = padEscape5;
                                    separator3 = multpl2;
                                    padPos8 = padPos9;
                                    minus3 = minus5;
                                    perMill3 = perMill5;
                                    percent3 = percent6;
                                }
                                if (subpart == 1) {
                                    zeroDigit2 = zeroDigit6;
                                    sigDigit3 = sigDigit8;
                                    percent2 = percent3;
                                    padPos9 = padPos8;
                                    multpl2 = separator3;
                                    sub0Start3 = sub0Limit;
                                    sub0Limit--;
                                    sigDigit4 = plus;
                                    exponent4 = exponent;
                                    subpart = 0;
                                    break;
                                } else if (ch == '\'') {
                                    percent2 = percent3;
                                    if (sub0Limit + 1 >= pattern.length() || str.charAt(sub0Limit + 1) != '\'') {
                                        subpart += 2;
                                    } else {
                                        sub0Limit++;
                                        affix4.append(ch);
                                    }
                                    zeroDigit2 = zeroDigit6;
                                    sigDigit3 = sigDigit8;
                                    padPos9 = padPos8;
                                    multpl2 = separator3;
                                    sigDigit4 = plus;
                                    exponent4 = exponent;
                                } else {
                                    percent2 = percent3;
                                    patternError("Unquoted special character '" + ch + '\'', str);
                                    multpl2 = separator3;
                                    affix4.append(ch);
                                    zeroDigit2 = zeroDigit6;
                                    sigDigit3 = sigDigit8;
                                    padPos9 = padPos7;
                                    sigDigit4 = plus;
                                    exponent4 = exponent;
                                }
                                break;
                            case 3:
                            case 4:
                                if (ch == '\'') {
                                    percent4 = percent5;
                                    if (sub0Limit + 1 >= pattern.length() || str.charAt(sub0Limit + 1) != '\'') {
                                        subpart -= 2;
                                    } else {
                                        sub0Limit++;
                                        affix4.append(ch);
                                    }
                                } else {
                                    percent4 = percent5;
                                }
                                affix4.append(ch);
                                zeroDigit2 = zeroDigit6;
                                sigDigit3 = sigDigit8;
                                separator2 = separator6;
                                part2 = part5;
                                sigDigit4 = plus;
                                exponent4 = exponent;
                                padEscape2 = padEscape5;
                                minus2 = minus5;
                                perMill2 = perMill5;
                                percent2 = percent4;
                                break;
                            default:
                                zeroDigit2 = zeroDigit6;
                                sigDigit3 = sigDigit8;
                                percent2 = percent5;
                                separator2 = separator6;
                                part2 = part5;
                                StringBuilder sb4 = affix4;
                                sigDigit4 = plus;
                                exponent4 = exponent;
                                int i8 = sigDigitCount5;
                                int part6 = groupingCount5;
                                padEscape2 = padEscape5;
                                int i9 = decimalPos3;
                                byte b = expDigits3;
                                char c13 = multpl2;
                                int i10 = padPos9;
                                minus2 = minus5;
                                perMill2 = perMill5;
                                affix4 = sb4;
                                break;
                        }
                    } else {
                        zeroDigit = zeroDigit6;
                        sigDigit = sigDigit8;
                        percent = percent5;
                        separator = separator6;
                        part = part5;
                        StringBuilder sb5 = affix4;
                        sigDigit2 = plus;
                        exponent2 = exponent;
                        sigDigitCount = sigDigitCount5;
                        groupingCount = groupingCount5;
                        padEscape = padEscape5;
                        decimalPos = decimalPos3;
                        expDigits = expDigits3;
                        multpl = multpl2;
                        padPos = padPos9;
                        minus = minus5;
                        perMill = perMill5;
                    }
                    pos4 = sub0Limit + 1;
                    plus = sigDigit4;
                    exponent = exponent4;
                    pos3 = start;
                    perMill4 = perMill2;
                    minus4 = minus2;
                    padEscape5 = padEscape2;
                    separator6 = separator2;
                    part5 = part2;
                    percent5 = percent2;
                    sigDigit8 = sigDigit3;
                    zeroDigit6 = zeroDigit2;
                }
                if (subpart == 3 || subpart == 4) {
                    patternError("Unterminated quote", str);
                }
                if (sub0Limit3 == 0) {
                    sub0Limit3 = pattern.length();
                }
                int sub0Limit4 = sub0Limit3;
                if (sub2Limit == 0) {
                    sub2Limit = pattern.length();
                }
                int sub2Limit2 = sub2Limit;
                if (zeroDigitCount == 0 && sigDigitCount == 0 && digitLeftCount > 0 && decimalPos >= 0) {
                    int n = decimalPos;
                    if (n == 0) {
                        n++;
                    }
                    digitRightCount = digitLeftCount - n;
                    digitLeftCount = n - 1;
                    zeroDigitCount = 1;
                }
                int digitLeftCount2 = digitLeftCount;
                if (decimalPos >= 0 || digitRightCount <= 0 || sigDigitCount != 0) {
                    if (decimalPos < 0) {
                        plus2 = sigDigit2;
                    } else if (sigDigitCount > 0 || decimalPos < digitLeftCount2) {
                        plus2 = sigDigit2;
                        groupingSeparator = groupingSeparator2;
                        plus3 = groupingCount22;
                        patternError("Malformed pattern", str);
                        if (padPos >= 0) {
                            decimalSeparator = decimalSeparator2;
                            int start2 = start;
                            padPos2 = padPos;
                            if (padPos2 == start2) {
                                padPos3 = 0;
                                int i11 = start2;
                                exponent3 = exponent2;
                                sub0Start = sub0Start3;
                            } else {
                                exponent3 = exponent2;
                                sub0Start = sub0Start3;
                                if (padPos2 + 2 == sub0Start) {
                                    padPos3 = 1;
                                } else if (padPos2 == sub0Limit4) {
                                    padPos3 = 2;
                                } else if (padPos2 + 2 == sub2Limit2) {
                                    padPos3 = 3;
                                } else {
                                    patternError("Illegal pad position", str);
                                }
                            }
                            padPos2 = padPos3;
                        } else {
                            decimalSeparator = decimalSeparator2;
                            exponent3 = exponent2;
                            sub0Start = sub0Start3;
                            int i12 = start;
                            padPos2 = padPos;
                        }
                        if (part == 0) {
                            digit = digit2;
                            String sb6 = prefix2.toString();
                            this.negPrefixPattern = sb6;
                            this.posPrefixPattern = sb6;
                            pos = sub0Limit;
                            String sb7 = suffix.toString();
                            this.negSuffixPattern = sb7;
                            this.posSuffixPattern = sb7;
                            this.useExponentialNotation = expDigits >= 0;
                            if (this.useExponentialNotation) {
                                this.minExponentDigits = expDigits;
                                expSignAlways = expSignAlways2;
                                this.exponentSignAlwaysShown = expSignAlways;
                            } else {
                                expSignAlways = expSignAlways2;
                            }
                            byte b2 = expDigits;
                            int digitTotalCount = digitLeftCount2 + zeroDigitCount + digitRightCount;
                            int effectiveDecimalPos = decimalPos >= 0 ? decimalPos : digitTotalCount;
                            boolean z3 = expSignAlways;
                            boolean expSignAlways3 = sigDigitCount > 0;
                            setSignificantDigitsUsed(expSignAlways3);
                            if (expSignAlways3) {
                                boolean z4 = expSignAlways3;
                                int sigDigitCount7 = sigDigitCount;
                                setMinimumSignificantDigits(sigDigitCount7);
                                int i13 = sub2Limit2;
                                setMaximumSignificantDigits(sigDigitCount7 + digitRightCount);
                                int i14 = sigDigitCount7;
                            } else {
                                boolean useSigDig = expSignAlways3;
                                int i15 = sub2Limit2;
                                int sub2Limit3 = effectiveDecimalPos - digitLeftCount2;
                                setMinimumIntegerDigits(sub2Limit3);
                                int i16 = sigDigitCount;
                                setMaximumIntegerDigits(this.useExponentialNotation != 0 ? digitLeftCount2 + sub2Limit3 : DOUBLE_INTEGER_DIGITS);
                                if (decimalPos >= 0) {
                                    i = digitTotalCount - decimalPos;
                                } else {
                                    i = 0;
                                }
                                _setMaximumFractionDigits(i);
                                if (decimalPos >= 0) {
                                    i2 = (digitLeftCount2 + zeroDigitCount) - decimalPos;
                                } else {
                                    i2 = 0;
                                }
                                setMinimumFractionDigits(i2);
                            }
                            setGroupingUsed(groupingCount > 0);
                            this.groupingSize = groupingCount > 0 ? groupingCount : 0;
                            this.groupingSize2 = (plus3 <= 0 || plus3 == groupingCount) ? 0 : plus3;
                            this.multiplier = multpl;
                            setDecimalSeparatorAlwaysShown(decimalPos == 0 || decimalPos == digitTotalCount);
                            if (padPos2 >= 0) {
                                this.padPosition = padPos2;
                                this.formatWidth = sub0Limit4 - sub0Start;
                                this.pad = padChar;
                                int i17 = digitLeftCount2;
                            } else {
                                int i18 = digitLeftCount2;
                                this.formatWidth = 0;
                            }
                            int i19 = plus3;
                            long incrementVal2 = incrementVal;
                            if (incrementVal2 != 0) {
                                int i20 = padPos2;
                                int scale = incrementPos2 - effectiveDecimalPos;
                                if (scale > 0) {
                                    int i21 = sub0Start;
                                    sub0Start2 = scale;
                                } else {
                                    int i22 = sub0Start;
                                    sub0Start2 = 0;
                                }
                                this.roundingIncrementICU = android.icu.math.BigDecimal.valueOf(incrementVal2, sub0Start2);
                                if (scale < 0) {
                                    long j = incrementVal2;
                                    this.roundingIncrementICU = this.roundingIncrementICU.movePointRight(-scale);
                                }
                                this.roundingMode = 6;
                            } else {
                                int i23 = padPos2;
                                int i24 = sub0Start;
                                setRoundingIncrement((android.icu.math.BigDecimal) null);
                            }
                            this.currencySignCount = currencySignCnt;
                        } else {
                            int i25 = plus3;
                            int i26 = padPos2;
                            int i27 = sub0Start;
                            byte b3 = expDigits;
                            digit = digit2;
                            pos = sub0Limit;
                            int i28 = sub2Limit2;
                            long j2 = incrementVal;
                            int digitLeftCount3 = currencySignCnt;
                            boolean z5 = expSignAlways2;
                            int sub2Limit4 = padChar;
                            char pos6 = multpl;
                            int i29 = sigDigitCount;
                            this.negPrefixPattern = prefix2.toString();
                            this.negSuffixPattern = suffix.toString();
                            gotNegative = true;
                        }
                        part4 = part + 1;
                        perMill4 = perMill;
                        minus4 = minus;
                        padEscape4 = padEscape;
                        separator6 = separator;
                        percent5 = percent;
                        sigDigit8 = sigDigit;
                        zeroDigit6 = zeroDigit;
                        plus4 = plus2;
                        groupingSeparator2 = groupingSeparator;
                        decimalSeparator2 = decimalSeparator;
                        exponent5 = exponent3;
                        digit2 = digit;
                        pos2 = pos;
                    } else {
                        plus2 = sigDigit2;
                    }
                    if (groupingCount != 0) {
                        plus3 = groupingCount22;
                        if (plus3 == 0) {
                            groupingSeparator = groupingSeparator2;
                        } else if (sigDigitCount <= 0 || zeroDigitCount <= 0) {
                            groupingSeparator = groupingSeparator2;
                        } else {
                            groupingSeparator = groupingSeparator2;
                        }
                    } else {
                        groupingSeparator = groupingSeparator2;
                        plus3 = groupingCount22;
                    }
                    patternError("Malformed pattern", str);
                    if (padPos >= 0) {
                    }
                    if (part == 0) {
                    }
                    part4 = part + 1;
                    perMill4 = perMill;
                    minus4 = minus;
                    padEscape4 = padEscape;
                    separator6 = separator;
                    percent5 = percent;
                    sigDigit8 = sigDigit;
                    zeroDigit6 = zeroDigit;
                    plus4 = plus2;
                    groupingSeparator2 = groupingSeparator;
                    decimalSeparator2 = decimalSeparator;
                    exponent5 = exponent3;
                    digit2 = digit;
                    pos2 = pos;
                } else {
                    plus2 = sigDigit2;
                }
                groupingSeparator = groupingSeparator2;
                plus3 = groupingCount22;
                patternError("Malformed pattern", str);
                if (padPos >= 0) {
                }
                if (part == 0) {
                }
                part4 = part + 1;
                perMill4 = perMill;
                minus4 = minus;
                padEscape4 = padEscape;
                separator6 = separator;
                percent5 = percent;
                sigDigit8 = sigDigit;
                zeroDigit6 = zeroDigit;
                plus4 = plus2;
                groupingSeparator2 = groupingSeparator;
                decimalSeparator2 = decimalSeparator;
                exponent5 = exponent3;
                digit2 = digit;
                pos2 = pos;
            }
        }
        char c22 = sigDigit8;
        char c32 = groupingSeparator2;
        char c42 = decimalSeparator2;
        char c52 = percent5;
        char c62 = perMill4;
        char c72 = digit2;
        char c82 = separator6;
        char c92 = padEscape4;
        char c102 = minus4;
        int i52 = pos2;
        char c112 = plus;
        String str22 = exponent;
        if (pattern.length() == 0) {
            this.posSuffixPattern = "";
            this.posPrefixPattern = "";
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
        this.formatPattern = str;
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

    public void setSignificantDigitsUsed(boolean useSignificantDigits2) {
        this.useSignificantDigits = useSignificantDigits2;
    }

    public void setCurrency(Currency theCurrency) {
        super.setCurrency(theCurrency);
        if (theCurrency != null) {
            String s = theCurrency.getName(this.symbols.getULocale(), 0, (boolean[]) null);
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

    public void setCurrencyUsage(Currency.CurrencyUsage newUsage) {
        if (newUsage != null) {
            this.currencyUsage = newUsage;
            Currency theCurrency = getCurrency();
            if (theCurrency != null) {
                setRoundingIncrement(theCurrency.getRoundingIncrement(this.currencyUsage));
                int d = theCurrency.getDefaultFractionDigits(this.currencyUsage);
                setMinimumFractionDigits(d);
                _setMaximumFractionDigits(d);
                return;
            }
            return;
        }
        throw new NullPointerException("return value is null at method AAA");
    }

    public Currency.CurrencyUsage getCurrencyUsage() {
        return this.currencyUsage;
    }

    /* access modifiers changed from: protected */
    @Deprecated
    public Currency getEffectiveCurrency() {
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
            this.currencyUsage = Currency.CurrencyUsage.STANDARD;
        }
        this.serialVersionOnStream = 4;
        this.digitList = new DigitList_Android();
        if (this.roundingIncrement != null) {
            setInternalRoundingIncrement(new android.icu.math.BigDecimal(this.roundingIncrement));
        }
        resetActualRounding();
    }

    private void setInternalRoundingIncrement(android.icu.math.BigDecimal value) {
        this.roundingIncrementICU = value;
        this.roundingIncrement = value == null ? null : value.toBigDecimal();
    }

    private void resetActualRounding() {
        if (this.roundingIncrementICU != null) {
            android.icu.math.BigDecimal byWidth = getMaximumFractionDigits() > 0 ? android.icu.math.BigDecimal.ONE.movePointLeft(getMaximumFractionDigits()) : android.icu.math.BigDecimal.ONE;
            if (this.roundingIncrementICU.compareTo(byWidth) >= 0) {
                this.actualRoundingIncrementICU = this.roundingIncrementICU;
            } else {
                this.actualRoundingIncrementICU = byWidth.equals(android.icu.math.BigDecimal.ONE) ? null : byWidth;
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
