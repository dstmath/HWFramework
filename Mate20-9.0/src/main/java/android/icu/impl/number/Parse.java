package android.icu.impl.number;

import android.icu.impl.StandardPlural;
import android.icu.impl.TextTrieMap;
import android.icu.impl.locale.LanguageTag;
import android.icu.lang.UCharacter;
import android.icu.text.CurrencyPluralInfo;
import android.icu.text.DecimalFormatSymbols;
import android.icu.text.NumberFormat;
import android.icu.text.UnicodeSet;
import android.icu.util.Currency;
import android.icu.util.CurrencyAmount;
import android.icu.util.ULocale;
import java.math.BigDecimal;
import java.math.MathContext;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Parse {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    public static volatile boolean DEBUGGING = false;
    /* access modifiers changed from: private */
    public static final BigDecimal MAX_LONG_AS_BIG_DECIMAL = new BigDecimal(Long.MAX_VALUE);
    /* access modifiers changed from: private */
    public static final BigDecimal MIN_LONG_AS_BIG_DECIMAL = new BigDecimal(Long.MIN_VALUE);
    private static final UnicodeSet UNISET_BIDI = new UnicodeSet("[[\\u200E\\u200F\\u061C]]").freeze();
    /* access modifiers changed from: private */
    public static final UnicodeSet UNISET_COMMA_LIKE = new UnicodeSet("[,\\u060C\\u066B\\u3001\\uFE10\\uFE11\\uFE50\\uFE51\\uFF0C\\uFF64]").freeze();
    @Deprecated
    public static final UnicodeSet UNISET_MINUS = new UnicodeSet(45, 45, 8315, 8315, 8331, 8331, 8722, 8722, 10134, 10134, 65123, 65123, 65293, 65293).freeze();
    /* access modifiers changed from: private */
    public static final UnicodeSet UNISET_OTHER_GROUPING_SEPARATORS = new UnicodeSet("[\\ '\\u00A0\\u066C\\u2000-\\u200A\\u2018\\u2019\\u202F\\u205F\\u3000\\uFF07]").freeze();
    /* access modifiers changed from: private */
    public static final UnicodeSet UNISET_PERIOD_LIKE = new UnicodeSet("[.\\u2024\\u3002\\uFE12\\uFE52\\uFF0E\\uFF61]").freeze();
    @Deprecated
    public static final UnicodeSet UNISET_PLUS = new UnicodeSet(43, 43, 8314, 8314, 8330, 8330, 10133, 10133, 64297, 64297, 65122, 65122, 65291, 65291).freeze();
    /* access modifiers changed from: private */
    public static final UnicodeSet UNISET_STRICT_COMMA_LIKE = new UnicodeSet("[,\\u066B\\uFE10\\uFE50\\uFF0C]").freeze();
    /* access modifiers changed from: private */
    public static final UnicodeSet UNISET_STRICT_PERIOD_LIKE = new UnicodeSet("[.\\u2024\\uFE52\\uFF0E\\uFF61]").freeze();
    private static final UnicodeSet UNISET_WHITESPACE = new UnicodeSet("[[:Zs:][\\u0009]]").freeze();
    protected static final ThreadLocal<ParsePosition> threadLocalParsePosition = new ThreadLocal<ParsePosition>() {
        /* access modifiers changed from: protected */
        public ParsePosition initialValue() {
            return new ParsePosition(0);
        }
    };
    protected static final ThreadLocal<ParserState> threadLocalParseState = new ThreadLocal<ParserState>() {
        /* access modifiers changed from: protected */
        public ParserState initialValue() {
            return new ParserState();
        }
    };

    private static class AffixHolder {
        static final AffixHolder EMPTY_NEGATIVE = new AffixHolder("", "", true, true);
        static final AffixHolder EMPTY_POSITIVE = new AffixHolder("", "", true, false);
        final boolean negative;
        final String p;
        final String s;
        final boolean strings;

        static void addToState(ParserState state, DecimalFormatProperties properties) {
            AffixHolder pp = fromPropertiesPositivePattern(properties);
            AffixHolder np = fromPropertiesNegativePattern(properties);
            AffixHolder ps = fromPropertiesPositiveString(properties);
            AffixHolder ns = fromPropertiesNegativeString(properties);
            if (pp != null) {
                state.affixHolders.add(pp);
            }
            if (ps != null) {
                state.affixHolders.add(ps);
            }
            if (np != null) {
                state.affixHolders.add(np);
            }
            if (ns != null) {
                state.affixHolders.add(ns);
            }
        }

        static AffixHolder fromPropertiesPositivePattern(DecimalFormatProperties properties) {
            String ppp = properties.getPositivePrefixPattern();
            String psp = properties.getPositiveSuffixPattern();
            if (properties.getSignAlwaysShown()) {
                boolean foundSign = false;
                String npp = properties.getNegativePrefixPattern();
                String nsp = properties.getNegativeSuffixPattern();
                if (AffixUtils.containsType(npp, -1)) {
                    foundSign = true;
                    ppp = AffixUtils.replaceType(npp, -1, '+');
                }
                if (AffixUtils.containsType(nsp, -1)) {
                    foundSign = true;
                    psp = AffixUtils.replaceType(nsp, -1, '+');
                }
                if (!foundSign) {
                    ppp = "+" + ppp;
                }
            }
            return getInstance(ppp, psp, false, false);
        }

        static AffixHolder fromPropertiesNegativePattern(DecimalFormatProperties properties) {
            String npp = properties.getNegativePrefixPattern();
            String nsp = properties.getNegativeSuffixPattern();
            if (npp == null && nsp == null) {
                String npp2 = properties.getPositivePrefixPattern();
                nsp = properties.getPositiveSuffixPattern();
                if (npp2 == null) {
                    npp = LanguageTag.SEP;
                } else {
                    npp = LanguageTag.SEP + npp2;
                }
            }
            return getInstance(npp, nsp, false, true);
        }

        static AffixHolder fromPropertiesPositiveString(DecimalFormatProperties properties) {
            String pp = properties.getPositivePrefix();
            String ps = properties.getPositiveSuffix();
            if (pp == null && ps == null) {
                return null;
            }
            return getInstance(pp, ps, true, false);
        }

        static AffixHolder fromPropertiesNegativeString(DecimalFormatProperties properties) {
            String np = properties.getNegativePrefix();
            String ns = properties.getNegativeSuffix();
            if (np == null && ns == null) {
                return null;
            }
            return getInstance(np, ns, true, true);
        }

        static AffixHolder getInstance(String p2, String s2, boolean strings2, boolean negative2) {
            if (p2 == null && s2 == null) {
                return negative2 ? EMPTY_NEGATIVE : EMPTY_POSITIVE;
            }
            if (p2 == null) {
                p2 = "";
            }
            if (s2 == null) {
                s2 = "";
            }
            if (p2.length() != 0 || s2.length() != 0) {
                return new AffixHolder(p2, s2, strings2, negative2);
            }
            return negative2 ? EMPTY_NEGATIVE : EMPTY_POSITIVE;
        }

        AffixHolder(String pp, String sp, boolean strings2, boolean negative2) {
            this.p = pp;
            this.s = sp;
            this.strings = strings2;
            this.negative = negative2;
        }

        public boolean equals(Object other) {
            if (other == null) {
                return false;
            }
            if (this == other) {
                return true;
            }
            if (!(other instanceof AffixHolder)) {
                return false;
            }
            AffixHolder _other = (AffixHolder) other;
            if (this.p.equals(_other.p) && this.s.equals(_other.s) && this.strings == _other.strings && this.negative == _other.negative) {
                return true;
            }
            return false;
        }

        public int hashCode() {
            return this.p.hashCode() ^ this.s.hashCode();
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("{");
            sb.append(this.p);
            sb.append("|");
            sb.append(this.s);
            sb.append("|");
            sb.append(this.strings ? 'S' : 'P');
            sb.append("}");
            return sb.toString();
        }
    }

    private static class CurrencyAffixPatterns {
        private static final ConcurrentHashMap<ULocale, CurrencyAffixPatterns> currencyAffixPatterns = new ConcurrentHashMap<>();
        private static final ThreadLocal<DecimalFormatProperties> threadLocalProperties = new ThreadLocal<DecimalFormatProperties>() {
            /* access modifiers changed from: protected */
            public DecimalFormatProperties initialValue() {
                return new DecimalFormatProperties();
            }
        };
        private final Set<AffixHolder> set = new HashSet();

        static void addToState(ULocale uloc, ParserState state) {
            CurrencyAffixPatterns value = currencyAffixPatterns.get(uloc);
            if (value == null) {
                currencyAffixPatterns.putIfAbsent(uloc, new CurrencyAffixPatterns(uloc));
                value = currencyAffixPatterns.get(uloc);
            }
            state.affixHolders.addAll(value.set);
        }

        private CurrencyAffixPatterns(ULocale uloc) {
            addPattern(NumberFormat.getPatternForStyle(uloc, 1));
            CurrencyPluralInfo pluralInfo = CurrencyPluralInfo.getInstance(uloc);
            for (StandardPlural plural : StandardPlural.VALUES) {
                addPattern(pluralInfo.getCurrencyPluralPattern(plural.getKeyword()));
            }
        }

        private void addPattern(String pattern) {
            DecimalFormatProperties properties = threadLocalProperties.get();
            try {
                PatternStringParser.parseToExistingProperties(pattern, properties);
            } catch (IllegalArgumentException e) {
            }
            this.set.add(AffixHolder.fromPropertiesPositivePattern(properties));
            this.set.add(AffixHolder.fromPropertiesNegativePattern(properties));
        }
    }

    private enum DigitType {
        INTEGER,
        FRACTION,
        EXPONENT
    }

    public enum GroupingMode {
        DEFAULT,
        RESTRICTED
    }

    public enum ParseMode {
        LENIENT,
        STRICT,
        FAST
    }

    private static class ParserState {
        static final /* synthetic */ boolean $assertionsDisabled = false;
        Set<AffixHolder> affixHolders = new HashSet();
        boolean caseSensitive;
        int decimalCp1;
        int decimalCp2;
        SeparatorType decimalType1;
        SeparatorType decimalType2;
        TextTrieMap<Byte> digitTrie;
        int groupingCp1;
        int groupingCp2;
        GroupingMode groupingMode;
        SeparatorType groupingType1;
        SeparatorType groupingType2;
        StateItem[] items = new StateItem[16];
        int length;
        ParseMode mode;
        boolean parseCurrency;
        StateItem[] prevItems = new StateItem[16];
        int prevLength;
        DecimalFormatProperties properties;
        DecimalFormatSymbols symbols;

        static {
            Class<Parse> cls = Parse.class;
        }

        ParserState() {
            for (int i = 0; i < this.items.length; i++) {
                this.items[i] = new StateItem((char) (65 + i));
                this.prevItems[i] = new StateItem((char) (65 + i));
            }
        }

        /* access modifiers changed from: package-private */
        public ParserState clear() {
            this.length = 0;
            this.prevLength = 0;
            this.digitTrie = null;
            this.affixHolders.clear();
            return this;
        }

        /* access modifiers changed from: package-private */
        public void swap() {
            StateItem[] temp = this.prevItems;
            this.prevItems = this.items;
            this.items = temp;
            this.prevLength = this.length;
            this.length = 0;
        }

        /* access modifiers changed from: package-private */
        public void swapBack() {
            StateItem[] temp = this.prevItems;
            this.prevItems = this.items;
            this.items = temp;
            this.length = this.prevLength;
            this.prevLength = 0;
        }

        /* access modifiers changed from: package-private */
        public StateItem getNext() {
            if (this.length >= this.items.length) {
                this.length = this.items.length - 1;
            }
            StateItem item = this.items[this.length];
            this.length++;
            return item;
        }

        public int lastInsertedIndex() {
            return this.length - 1;
        }

        public StateItem getItem(int i) {
            return this.items[i];
        }

        public String toString() {
            return "<ParseState mode:" + this.mode + " caseSensitive:" + this.caseSensitive + " parseCurrency:" + this.parseCurrency + " groupingMode:" + this.groupingMode + " decimalCps:" + ((char) this.decimalCp1) + ((char) this.decimalCp2) + " groupingCps:" + ((char) this.groupingCp1) + ((char) this.groupingCp2) + " affixes:" + this.affixHolders + ">";
        }
    }

    private enum SeparatorType {
        COMMA_LIKE,
        PERIOD_LIKE,
        OTHER_GROUPING,
        UNKNOWN;

        static SeparatorType fromCp(int cp, ParseMode mode) {
            if (mode == ParseMode.FAST) {
                return UNKNOWN;
            }
            if (mode == ParseMode.STRICT) {
                if (Parse.UNISET_STRICT_COMMA_LIKE.contains(cp)) {
                    return COMMA_LIKE;
                }
                if (Parse.UNISET_STRICT_PERIOD_LIKE.contains(cp)) {
                    return PERIOD_LIKE;
                }
                if (Parse.UNISET_OTHER_GROUPING_SEPARATORS.contains(cp)) {
                    return OTHER_GROUPING;
                }
                return UNKNOWN;
            } else if (Parse.UNISET_COMMA_LIKE.contains(cp)) {
                return COMMA_LIKE;
            } else {
                if (Parse.UNISET_PERIOD_LIKE.contains(cp)) {
                    return PERIOD_LIKE;
                }
                if (Parse.UNISET_OTHER_GROUPING_SEPARATORS.contains(cp)) {
                    return OTHER_GROUPING;
                }
                return UNKNOWN;
            }
        }
    }

    private static class StateItem {
        static final /* synthetic */ boolean $assertionsDisabled = false;
        AffixHolder affix;
        CharSequence currentAffixPattern;
        TextTrieMap<Currency.CurrencyStringInfo>.ParseState currentCurrencyTrieState;
        TextTrieMap<Byte>.ParseState currentDigitTrieState;
        DigitType currentDigitType;
        int currentOffset;
        long currentStepwiseParserTag;
        CharSequence currentString;
        boolean currentTrailing;
        int exponent;
        DecimalQuantity_DualStorageBCD fq = new DecimalQuantity_DualStorageBCD();
        int groupingCp;
        long groupingWidths;
        final char id;
        String isoCode;
        StateName name;
        int numDigits;
        String path;
        StateName returnTo1;
        StateName returnTo2;
        boolean sawCurrency;
        boolean sawDecimalPoint;
        boolean sawExponentDigit;
        boolean sawInfinity;
        boolean sawNaN;
        boolean sawNegative;
        boolean sawNegativeExponent;
        boolean sawPrefix;
        boolean sawSuffix;
        int score;
        int trailingCount;
        int trailingZeros;

        static {
            Class<Parse> cls = Parse.class;
        }

        StateItem(char _id) {
            this.id = _id;
        }

        /* access modifiers changed from: package-private */
        public StateItem clear() {
            this.name = StateName.BEFORE_PREFIX;
            this.trailingCount = 0;
            this.score = 0;
            this.fq.clear();
            this.numDigits = 0;
            this.trailingZeros = 0;
            this.exponent = 0;
            this.groupingCp = -1;
            this.groupingWidths = 0;
            this.isoCode = null;
            this.sawNegative = false;
            this.sawNegativeExponent = false;
            this.sawCurrency = false;
            this.sawNaN = false;
            this.sawInfinity = false;
            this.affix = null;
            this.sawPrefix = false;
            this.sawSuffix = false;
            this.sawDecimalPoint = false;
            this.sawExponentDigit = false;
            this.returnTo1 = null;
            this.returnTo2 = null;
            this.currentString = null;
            this.currentOffset = 0;
            this.currentTrailing = false;
            this.currentAffixPattern = null;
            this.currentStepwiseParserTag = 0;
            this.currentCurrencyTrieState = null;
            this.currentDigitTrieState = null;
            this.currentDigitType = null;
            this.path = "";
            return this;
        }

        /* access modifiers changed from: package-private */
        public StateItem copyFrom(StateItem other, StateName newName, int trailing) {
            this.name = newName;
            this.score = other.score;
            this.trailingCount = trailing < 0 ? 0 : other.trailingCount + Character.charCount(trailing);
            this.fq.copyFrom(other.fq);
            this.numDigits = other.numDigits;
            this.trailingZeros = other.trailingZeros;
            this.exponent = other.exponent;
            this.groupingCp = other.groupingCp;
            this.groupingWidths = other.groupingWidths;
            this.isoCode = other.isoCode;
            this.sawNegative = other.sawNegative;
            this.sawNegativeExponent = other.sawNegativeExponent;
            this.sawCurrency = other.sawCurrency;
            this.sawNaN = other.sawNaN;
            this.sawInfinity = other.sawInfinity;
            this.affix = other.affix;
            this.sawPrefix = other.sawPrefix;
            this.sawSuffix = other.sawSuffix;
            this.sawDecimalPoint = other.sawDecimalPoint;
            this.sawExponentDigit = other.sawExponentDigit;
            this.returnTo1 = other.returnTo1;
            this.returnTo2 = other.returnTo2;
            this.currentString = other.currentString;
            this.currentOffset = other.currentOffset;
            this.currentTrailing = other.currentTrailing;
            this.currentAffixPattern = other.currentAffixPattern;
            this.currentStepwiseParserTag = other.currentStepwiseParserTag;
            this.currentCurrencyTrieState = other.currentCurrencyTrieState;
            this.currentDigitTrieState = other.currentDigitTrieState;
            this.currentDigitType = other.currentDigitType;
            if (Parse.DEBUGGING) {
                this.path = other.path + other.id;
            }
            return this;
        }

        /* access modifiers changed from: package-private */
        public void appendDigit(byte digit, DigitType type) {
            if (type == DigitType.EXPONENT) {
                this.sawExponentDigit = true;
                int newExponent = (this.exponent * 10) + digit;
                if (newExponent < this.exponent) {
                    this.exponent = Integer.MAX_VALUE;
                } else {
                    this.exponent = newExponent;
                }
            } else {
                this.numDigits++;
                if (type == DigitType.FRACTION && digit == 0) {
                    this.trailingZeros++;
                } else if (type == DigitType.FRACTION) {
                    this.fq.appendDigit(digit, this.trailingZeros, false);
                    this.trailingZeros = 0;
                } else {
                    this.fq.appendDigit(digit, 0, true);
                }
            }
        }

        public boolean hasNumber() {
            return this.numDigits > 0 || this.sawNaN || this.sawInfinity;
        }

        /* access modifiers changed from: package-private */
        public Number toNumber(DecimalFormatProperties properties) {
            if (this.sawNaN) {
                return Double.valueOf(Double.NaN);
            }
            if (this.sawInfinity) {
                if (this.sawNegative) {
                    return Double.valueOf(Double.NEGATIVE_INFINITY);
                }
                return Double.valueOf(Double.POSITIVE_INFINITY);
            } else if (this.fq.isZero() && this.sawNegative) {
                return Double.valueOf(-0.0d);
            } else {
                boolean forceBigDecimal = properties.getParseToBigDecimal();
                if (this.exponent != Integer.MAX_VALUE) {
                    if (this.exponent > 1000) {
                        forceBigDecimal = true;
                    }
                    BigDecimal multiplier = properties.getMultiplier();
                    if (properties.getMagnitudeMultiplier() != 0) {
                        if (multiplier == null) {
                            multiplier = BigDecimal.ONE;
                        }
                        multiplier = multiplier.scaleByPowerOfTen(properties.getMagnitudeMultiplier());
                    }
                    int delta = (this.sawNegativeExponent ? -1 : 1) * this.exponent;
                    MathContext mc = RoundingUtils.getMathContextOr34Digits(properties);
                    BigDecimal result = this.fq.toBigDecimal();
                    if (this.sawNegative) {
                        result = result.negate();
                    }
                    BigDecimal result2 = result.scaleByPowerOfTen(delta);
                    if (multiplier != null) {
                        result2 = result2.divide(multiplier, mc);
                    }
                    BigDecimal result3 = result2.stripTrailingZeros();
                    if (forceBigDecimal || result3.scale() > 0) {
                        return result3;
                    }
                    if (result3.compareTo(Parse.MIN_LONG_AS_BIG_DECIMAL) < 0 || result3.compareTo(Parse.MAX_LONG_AS_BIG_DECIMAL) > 0) {
                        return result3.toBigIntegerExact();
                    }
                    return Long.valueOf(result3.longValueExact());
                } else if (this.sawNegativeExponent && this.sawNegative) {
                    return Double.valueOf(-0.0d);
                } else {
                    if (this.sawNegativeExponent) {
                        return Double.valueOf(0.0d);
                    }
                    if (this.sawNegative) {
                        return Double.valueOf(Double.NEGATIVE_INFINITY);
                    }
                    return Double.valueOf(Double.POSITIVE_INFINITY);
                }
            }
        }

        public CurrencyAmount toCurrencyAmount(DecimalFormatProperties properties) {
            return new CurrencyAmount(toNumber(properties), Currency.getInstance(this.isoCode));
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            sb.append(this.path);
            sb.append("] ");
            sb.append(this.name.name());
            if (this.name == StateName.INSIDE_STRING) {
                sb.append("{");
                sb.append(this.currentString);
                sb.append(":");
                sb.append(this.currentOffset);
                sb.append("}");
            }
            if (this.name == StateName.INSIDE_AFFIX_PATTERN) {
                sb.append("{");
                sb.append(this.currentAffixPattern);
                sb.append(":");
                sb.append(AffixUtils.getOffset(this.currentStepwiseParserTag) - 1);
                sb.append("}");
            }
            sb.append(Padder.FALLBACK_PADDING_STRING);
            sb.append(this.fq.toBigDecimal());
            sb.append(" grouping:");
            sb.append(this.groupingCp == -1 ? new char[]{'?'} : Character.toChars(this.groupingCp));
            sb.append(" widths:");
            sb.append(Long.toHexString(this.groupingWidths));
            sb.append(" seen:");
            sb.append(this.sawNegative ? 1 : 0);
            sb.append(this.sawNegativeExponent ? 1 : 0);
            sb.append(this.sawNaN ? 1 : 0);
            sb.append(this.sawInfinity ? 1 : 0);
            sb.append(this.sawPrefix ? 1 : 0);
            sb.append(this.sawSuffix ? 1 : 0);
            sb.append(this.sawDecimalPoint ? 1 : 0);
            sb.append(" trailing:");
            sb.append(this.trailingCount);
            sb.append(" score:");
            sb.append(this.score);
            sb.append(" affix:");
            sb.append(this.affix);
            sb.append(" currency:");
            sb.append(this.isoCode);
            return sb.toString();
        }
    }

    private enum StateName {
        BEFORE_PREFIX,
        AFTER_PREFIX,
        AFTER_INTEGER_DIGIT,
        AFTER_FRACTION_DIGIT,
        AFTER_EXPONENT_SEPARATOR,
        AFTER_EXPONENT_DIGIT,
        BEFORE_SUFFIX,
        BEFORE_SUFFIX_SEEN_EXPONENT,
        AFTER_SUFFIX,
        INSIDE_CURRENCY,
        INSIDE_DIGIT,
        INSIDE_STRING,
        INSIDE_AFFIX_PATTERN
    }

    static TextTrieMap<Byte> makeDigitTrie(String[] digitStrings) {
        boolean requiresTrie = false;
        int i = 0;
        while (true) {
            if (i >= 10) {
                break;
            }
            String str = digitStrings[i];
            if (Character.charCount(Character.codePointAt(str, 0)) != str.length()) {
                requiresTrie = true;
                break;
            }
            i++;
        }
        if (!requiresTrie) {
            return null;
        }
        TextTrieMap<Byte> trieMap = new TextTrieMap<>(false);
        for (int i2 = 0; i2 < 10; i2++) {
            trieMap.put(digitStrings[i2], Byte.valueOf((byte) i2));
        }
        return trieMap;
    }

    public static Number parse(String input, DecimalFormatProperties properties, DecimalFormatSymbols symbols) {
        ParsePosition ppos = threadLocalParsePosition.get();
        ppos.setIndex(0);
        return parse(input, ppos, properties, symbols);
    }

    public static Number parse(CharSequence input, ParsePosition ppos, DecimalFormatProperties properties, DecimalFormatSymbols symbols) {
        StateItem best = _parse(input, ppos, false, properties, symbols);
        if (best == null) {
            return null;
        }
        return best.toNumber(properties);
    }

    public static CurrencyAmount parseCurrency(String input, DecimalFormatProperties properties, DecimalFormatSymbols symbols) throws ParseException {
        return parseCurrency(input, null, properties, symbols);
    }

    public static CurrencyAmount parseCurrency(CharSequence input, ParsePosition ppos, DecimalFormatProperties properties, DecimalFormatSymbols symbols) throws ParseException {
        if (ppos == null) {
            ppos = threadLocalParsePosition.get();
            ppos.setIndex(0);
            ppos.setErrorIndex(-1);
        }
        StateItem best = _parse(input, ppos, true, properties, symbols);
        if (best == null) {
            return null;
        }
        return best.toCurrencyAmount(properties);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:105:0x024a, code lost:
        if (r5 == android.icu.impl.number.Parse.ParseMode.FAST) goto L_0x0138;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:133:0x02a1, code lost:
        if (r5 == android.icu.impl.number.Parse.ParseMode.FAST) goto L_0x0138;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:138:0x02b2, code lost:
        if (r5 == android.icu.impl.number.Parse.ParseMode.FAST) goto L_0x0138;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:151:0x02e1, code lost:
        if (r5 == android.icu.impl.number.Parse.ParseMode.FAST) goto L_0x0138;
     */
    private static StateItem _parse(CharSequence input, ParsePosition ppos, boolean parseCurrency, DecimalFormatProperties properties, DecimalFormatSymbols symbols) {
        int i;
        StateItem initialStateItem;
        ParserState state;
        boolean ignoreGrouping;
        boolean ignoreExponent;
        boolean integerOnly;
        ParseMode mode;
        StateItem stateItem;
        int numGroupingRegions;
        boolean z;
        CharSequence charSequence = input;
        ParsePosition parsePosition = ppos;
        boolean z2 = parseCurrency;
        DecimalFormatProperties decimalFormatProperties = properties;
        DecimalFormatSymbols decimalFormatSymbols = symbols;
        if (charSequence == null || parsePosition == null || decimalFormatProperties == null || decimalFormatSymbols == null) {
            throw new IllegalArgumentException("All arguments are required for parse.");
        }
        ParseMode mode2 = properties.getParseMode();
        if (mode2 == null) {
            mode2 = ParseMode.LENIENT;
        }
        boolean integerOnly2 = properties.getParseIntegerOnly();
        boolean numGroupingRegions2 = properties.getParseNoExponent();
        int i2 = 0;
        boolean ignoreGrouping2 = properties.getGroupingSize() <= 0;
        ParserState state2 = threadLocalParseState.get().clear();
        state2.properties = decimalFormatProperties;
        state2.symbols = decimalFormatSymbols;
        state2.mode = mode2;
        state2.parseCurrency = z2;
        state2.groupingMode = properties.getParseGroupingMode();
        if (state2.groupingMode == null) {
            state2.groupingMode = GroupingMode.DEFAULT;
        }
        state2.caseSensitive = properties.getParseCaseSensitive();
        state2.decimalCp1 = Character.codePointAt(symbols.getDecimalSeparatorString(), 0);
        state2.decimalCp2 = Character.codePointAt(symbols.getMonetaryDecimalSeparatorString(), 0);
        state2.groupingCp1 = Character.codePointAt(symbols.getGroupingSeparatorString(), 0);
        state2.groupingCp2 = Character.codePointAt(symbols.getMonetaryGroupingSeparatorString(), 0);
        state2.decimalType1 = SeparatorType.fromCp(state2.decimalCp1, mode2);
        state2.decimalType2 = SeparatorType.fromCp(state2.decimalCp2, mode2);
        state2.groupingType1 = SeparatorType.fromCp(state2.groupingCp1, mode2);
        state2.groupingType2 = SeparatorType.fromCp(state2.groupingCp2, mode2);
        StateItem initialStateItem2 = state2.getNext().clear();
        initialStateItem2.name = StateName.BEFORE_PREFIX;
        if (mode2 == ParseMode.LENIENT || mode2 == ParseMode.STRICT) {
            state2.digitTrie = makeDigitTrie(symbols.getDigitStringsLocal());
            AffixHolder.addToState(state2, decimalFormatProperties);
            if (z2) {
                CurrencyAffixPatterns.addToState(symbols.getULocale(), state2);
            }
        }
        if (DEBUGGING) {
            System.out.println("Parsing: " + charSequence);
            System.out.println(decimalFormatProperties);
            System.out.println(state2);
        }
        int offset = ppos.getIndex();
        while (true) {
            if (offset < input.length()) {
                int cp = Character.codePointAt(charSequence, offset);
                state2.swap();
                int i3 = i2;
                while (i3 < state2.prevLength) {
                    StateItem item = state2.prevItems[i3];
                    if (DEBUGGING) {
                        System.out.println(":" + offset + item.id + Padder.FALLBACK_PADDING_STRING + item);
                    }
                    switch (item.name) {
                        case BEFORE_PREFIX:
                            if (mode2 == ParseMode.LENIENT || mode2 == ParseMode.FAST) {
                                z = false;
                                acceptMinusOrPlusSign(cp, StateName.BEFORE_PREFIX, state2, item, false);
                                if (state2.length > 0 && mode2 == ParseMode.FAST) {
                                    break;
                                }
                            } else {
                                z = false;
                            }
                            acceptIntegerDigit(cp, StateName.AFTER_INTEGER_DIGIT, state2, item);
                            if (state2.length > 0 && mode2 == ParseMode.FAST) {
                                break;
                            } else {
                                acceptBidi(cp, StateName.BEFORE_PREFIX, state2, item);
                                if (state2.length > 0 && mode2 == ParseMode.FAST) {
                                    break;
                                } else {
                                    acceptWhitespace(cp, StateName.BEFORE_PREFIX, state2, item);
                                    if (state2.length > 0 && mode2 == ParseMode.FAST) {
                                        break;
                                    } else {
                                        acceptPadding(cp, StateName.BEFORE_PREFIX, state2, item);
                                        if (state2.length > 0 && mode2 == ParseMode.FAST) {
                                            break;
                                        } else {
                                            acceptNan(cp, StateName.BEFORE_SUFFIX, state2, item);
                                            if (state2.length > 0 && mode2 == ParseMode.FAST) {
                                                break;
                                            } else {
                                                acceptInfinity(cp, StateName.BEFORE_SUFFIX, state2, item);
                                                if (state2.length > 0 && mode2 == ParseMode.FAST) {
                                                    break;
                                                } else {
                                                    if (!integerOnly2) {
                                                        acceptDecimalPoint(cp, StateName.AFTER_FRACTION_DIGIT, state2, item);
                                                        if (state2.length > 0 && mode2 == ParseMode.FAST) {
                                                            break;
                                                        }
                                                    }
                                                    if (mode2 == ParseMode.LENIENT || mode2 == ParseMode.STRICT) {
                                                        acceptPrefix(cp, StateName.AFTER_PREFIX, state2, item);
                                                    }
                                                    if (mode2 != ParseMode.LENIENT && mode2 != ParseMode.FAST) {
                                                        break;
                                                    } else {
                                                        if (!ignoreGrouping2) {
                                                            acceptGrouping(cp, StateName.AFTER_INTEGER_DIGIT, state2, item);
                                                            if (state2.length > 0 && mode2 == ParseMode.FAST) {
                                                                break;
                                                            }
                                                        }
                                                        if (!z2) {
                                                            break;
                                                        } else {
                                                            acceptCurrency(cp, StateName.BEFORE_PREFIX, state2, item);
                                                            break;
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        case AFTER_PREFIX:
                            acceptBidi(cp, StateName.AFTER_PREFIX, state2, item);
                            acceptPadding(cp, StateName.AFTER_PREFIX, state2, item);
                            acceptNan(cp, StateName.BEFORE_SUFFIX, state2, item);
                            acceptInfinity(cp, StateName.BEFORE_SUFFIX, state2, item);
                            acceptIntegerDigit(cp, StateName.AFTER_INTEGER_DIGIT, state2, item);
                            if (!integerOnly2) {
                                acceptDecimalPoint(cp, StateName.AFTER_FRACTION_DIGIT, state2, item);
                            }
                            if (mode2 == ParseMode.LENIENT || mode2 == ParseMode.FAST) {
                                acceptWhitespace(cp, StateName.AFTER_PREFIX, state2, item);
                                if (!ignoreGrouping2) {
                                    acceptGrouping(cp, StateName.AFTER_INTEGER_DIGIT, state2, item);
                                }
                                if (z2) {
                                    acceptCurrency(cp, StateName.AFTER_PREFIX, state2, item);
                                    break;
                                }
                            }
                            break;
                        case AFTER_INTEGER_DIGIT:
                            acceptIntegerDigit(cp, StateName.AFTER_INTEGER_DIGIT, state2, item);
                            if (state2.length <= 0 || mode2 != ParseMode.FAST) {
                                if (!integerOnly2) {
                                    acceptDecimalPoint(cp, StateName.AFTER_FRACTION_DIGIT, state2, item);
                                    if (state2.length > 0) {
                                        break;
                                    }
                                }
                                if (!ignoreGrouping2) {
                                    acceptGrouping(cp, StateName.AFTER_INTEGER_DIGIT, state2, item);
                                    if (state2.length > 0) {
                                        break;
                                    }
                                }
                                acceptBidi(cp, StateName.BEFORE_SUFFIX, state2, item);
                                if (state2.length <= 0 || mode2 != ParseMode.FAST) {
                                    acceptPadding(cp, StateName.BEFORE_SUFFIX, state2, item);
                                    if (state2.length <= 0 || mode2 != ParseMode.FAST) {
                                        if (!numGroupingRegions2) {
                                            acceptExponentSeparator(cp, StateName.AFTER_EXPONENT_SEPARATOR, state2, item);
                                            if (state2.length > 0) {
                                                break;
                                            }
                                        }
                                        if (mode2 == ParseMode.LENIENT || mode2 == ParseMode.STRICT) {
                                            acceptSuffix(cp, StateName.AFTER_SUFFIX, state2, item);
                                        }
                                        if (mode2 == ParseMode.LENIENT || mode2 == ParseMode.FAST) {
                                            acceptWhitespace(cp, StateName.BEFORE_SUFFIX, state2, item);
                                            if ((state2.length <= 0 || mode2 != ParseMode.FAST) && ((state2.length <= 0 || mode2 != ParseMode.FAST) && z2)) {
                                                acceptCurrency(cp, StateName.BEFORE_SUFFIX, state2, item);
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                        case AFTER_FRACTION_DIGIT:
                            acceptFractionDigit(cp, StateName.AFTER_FRACTION_DIGIT, state2, item);
                            if (state2.length <= 0 || mode2 != ParseMode.FAST) {
                                acceptBidi(cp, StateName.BEFORE_SUFFIX, state2, item);
                                if (state2.length <= 0 || mode2 != ParseMode.FAST) {
                                    acceptPadding(cp, StateName.BEFORE_SUFFIX, state2, item);
                                    if (state2.length <= 0 || mode2 != ParseMode.FAST) {
                                        if (!numGroupingRegions2) {
                                            acceptExponentSeparator(cp, StateName.AFTER_EXPONENT_SEPARATOR, state2, item);
                                            if (state2.length > 0) {
                                                break;
                                            }
                                        }
                                        if (mode2 == ParseMode.LENIENT || mode2 == ParseMode.STRICT) {
                                            acceptSuffix(cp, StateName.AFTER_SUFFIX, state2, item);
                                        }
                                        if (mode2 == ParseMode.LENIENT || mode2 == ParseMode.FAST) {
                                            acceptWhitespace(cp, StateName.BEFORE_SUFFIX, state2, item);
                                            if ((state2.length <= 0 || mode2 != ParseMode.FAST) && ((state2.length <= 0 || mode2 != ParseMode.FAST) && z2)) {
                                                acceptCurrency(cp, StateName.BEFORE_SUFFIX, state2, item);
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                        case AFTER_EXPONENT_SEPARATOR:
                            acceptBidi(cp, StateName.AFTER_EXPONENT_SEPARATOR, state2, item);
                            acceptMinusOrPlusSign(cp, StateName.AFTER_EXPONENT_SEPARATOR, state2, item, true);
                            acceptExponentDigit(cp, StateName.AFTER_EXPONENT_DIGIT, state2, item);
                            break;
                        case AFTER_EXPONENT_DIGIT:
                            acceptBidi(cp, StateName.BEFORE_SUFFIX_SEEN_EXPONENT, state2, item);
                            acceptPadding(cp, StateName.BEFORE_SUFFIX_SEEN_EXPONENT, state2, item);
                            acceptExponentDigit(cp, StateName.AFTER_EXPONENT_DIGIT, state2, item);
                            if (mode2 == ParseMode.LENIENT || mode2 == ParseMode.STRICT) {
                                acceptSuffix(cp, StateName.AFTER_SUFFIX, state2, item);
                            }
                            if (mode2 == ParseMode.LENIENT || mode2 == ParseMode.FAST) {
                                acceptWhitespace(cp, StateName.BEFORE_SUFFIX_SEEN_EXPONENT, state2, item);
                                if (z2) {
                                    acceptCurrency(cp, StateName.BEFORE_SUFFIX_SEEN_EXPONENT, state2, item);
                                    break;
                                }
                            }
                            break;
                        case BEFORE_SUFFIX:
                            acceptBidi(cp, StateName.BEFORE_SUFFIX, state2, item);
                            acceptPadding(cp, StateName.BEFORE_SUFFIX, state2, item);
                            if (!numGroupingRegions2) {
                                acceptExponentSeparator(cp, StateName.AFTER_EXPONENT_SEPARATOR, state2, item);
                            }
                            if (mode2 == ParseMode.LENIENT || mode2 == ParseMode.STRICT) {
                                acceptSuffix(cp, StateName.AFTER_SUFFIX, state2, item);
                            }
                            if (mode2 == ParseMode.LENIENT || mode2 == ParseMode.FAST) {
                                acceptWhitespace(cp, StateName.BEFORE_SUFFIX, state2, item);
                                if (z2) {
                                    acceptCurrency(cp, StateName.BEFORE_SUFFIX, state2, item);
                                    break;
                                }
                            }
                            break;
                        case BEFORE_SUFFIX_SEEN_EXPONENT:
                            acceptBidi(cp, StateName.BEFORE_SUFFIX_SEEN_EXPONENT, state2, item);
                            acceptPadding(cp, StateName.BEFORE_SUFFIX_SEEN_EXPONENT, state2, item);
                            if (mode2 == ParseMode.LENIENT || mode2 == ParseMode.STRICT) {
                                acceptSuffix(cp, StateName.AFTER_SUFFIX, state2, item);
                            }
                            if (mode2 == ParseMode.LENIENT || mode2 == ParseMode.FAST) {
                                acceptWhitespace(cp, StateName.BEFORE_SUFFIX_SEEN_EXPONENT, state2, item);
                                if (z2) {
                                    acceptCurrency(cp, StateName.BEFORE_SUFFIX_SEEN_EXPONENT, state2, item);
                                    break;
                                }
                            }
                            break;
                        case AFTER_SUFFIX:
                            if ((mode2 == ParseMode.LENIENT || mode2 == ParseMode.FAST) && z2) {
                                acceptBidi(cp, StateName.AFTER_SUFFIX, state2, item);
                                acceptPadding(cp, StateName.AFTER_SUFFIX, state2, item);
                                acceptWhitespace(cp, StateName.AFTER_SUFFIX, state2, item);
                                if (z2) {
                                    acceptCurrency(cp, StateName.AFTER_SUFFIX, state2, item);
                                    break;
                                }
                            }
                            break;
                        case INSIDE_CURRENCY:
                            acceptCurrencyOffset(cp, state2, item);
                            break;
                        case INSIDE_DIGIT:
                            acceptDigitTrieOffset(cp, state2, item);
                            break;
                        case INSIDE_STRING:
                            acceptStringOffset(cp, state2, item);
                            break;
                        case INSIDE_AFFIX_PATTERN:
                            acceptAffixPatternOffset(cp, state2, item);
                            break;
                    }
                    z = false;
                    i3++;
                    boolean z3 = z;
                    CharSequence charSequence2 = input;
                    DecimalFormatSymbols decimalFormatSymbols2 = symbols;
                }
                i = 0;
                if (state2.length == 0) {
                    state2.swapBack();
                } else {
                    offset += Character.charCount(cp);
                    i2 = 0;
                    charSequence = input;
                    DecimalFormatSymbols decimalFormatSymbols3 = symbols;
                }
            } else {
                i = i2;
            }
        }
        if (state2.length == 0) {
            if (DEBUGGING) {
                System.out.println("No matches found");
                System.out.println("- - - - - - - - - -");
            }
            return null;
        }
        StateItem best = null;
        int i4 = i;
        while (i4 < state2.length) {
            StateItem item2 = state2.items[i4];
            if (DEBUGGING) {
                System.out.println(":end " + item2);
            }
            if (!item2.hasNumber()) {
                if (DEBUGGING) {
                    System.out.println("-> rejected due to no number value");
                }
                mode = mode2;
            } else {
                if (mode2 == ParseMode.STRICT) {
                    boolean sawPrefix = item2.sawPrefix || (item2.affix != null && item2.affix.p.isEmpty());
                    boolean sawSuffix = item2.sawSuffix || (item2.affix != null && item2.affix.s.isEmpty());
                    mode = mode2;
                    boolean hasEmptyAffix = state2.affixHolders.contains(AffixHolder.EMPTY_POSITIVE) || state2.affixHolders.contains(AffixHolder.EMPTY_NEGATIVE);
                    if ((!sawPrefix || !sawSuffix) && (sawPrefix || sawSuffix || !hasEmptyAffix)) {
                        boolean z4 = hasEmptyAffix;
                        integerOnly = integerOnly2;
                        ignoreExponent = numGroupingRegions2;
                        ignoreGrouping = ignoreGrouping2;
                        boolean z5 = sawSuffix;
                        state = state2;
                        initialStateItem = initialStateItem2;
                        if (DEBUGGING) {
                            System.out.println("-> rejected due to mismatched prefix/suffix");
                        }
                        i4++;
                        mode2 = mode;
                        integerOnly2 = integerOnly;
                        numGroupingRegions2 = ignoreExponent;
                        ignoreGrouping2 = ignoreGrouping;
                        state2 = state;
                        initialStateItem2 = initialStateItem;
                    } else if (properties.getMinimumExponentDigits() <= 0 || item2.sawExponentDigit) {
                        int grouping1 = properties.getGroupingSize();
                        int grouping2 = properties.getSecondaryGroupingSize();
                        int grouping12 = grouping1 > 0 ? grouping1 : grouping2;
                        int grouping22 = grouping2 > 0 ? grouping2 : grouping12;
                        boolean z6 = hasEmptyAffix;
                        integerOnly = integerOnly2;
                        long groupingWidths = item2.groupingWidths;
                        int numGroupingRegions3 = 16 - (Long.numberOfLeadingZeros(groupingWidths) / 4);
                        while (true) {
                            ignoreExponent = numGroupingRegions2;
                            ignoreGrouping = ignoreGrouping2;
                            numGroupingRegions = numGroupingRegions3;
                            if (numGroupingRegions <= 1 || (groupingWidths & 15) != 0) {
                            } else if (!item2.sawDecimalPoint) {
                                groupingWidths >>>= 4;
                                numGroupingRegions3 = numGroupingRegions - 1;
                                numGroupingRegions2 = ignoreExponent;
                                ignoreGrouping2 = ignoreGrouping;
                            } else if (DEBUGGING) {
                                boolean z7 = sawSuffix;
                                System.out.println("-> rejected due to decimal point after grouping");
                            }
                        }
                        if (grouping12 <= 0 || numGroupingRegions <= 1) {
                            state = state2;
                            initialStateItem = initialStateItem2;
                        } else if ((groupingWidths & 15) != ((long) grouping12)) {
                            if (DEBUGGING) {
                                System.out.println("-> rejected due to first grouping violation");
                            }
                            state = state2;
                            initialStateItem = initialStateItem2;
                            i4++;
                            mode2 = mode;
                            integerOnly2 = integerOnly;
                            numGroupingRegions2 = ignoreExponent;
                            ignoreGrouping2 = ignoreGrouping;
                            state2 = state;
                            initialStateItem2 = initialStateItem;
                        } else {
                            state = state2;
                            initialStateItem = initialStateItem2;
                            if (((groupingWidths >>> ((numGroupingRegions - 1) * 4)) & 15) <= ((long) grouping22)) {
                                int j = 1;
                                while (true) {
                                    if (j < numGroupingRegions - 1) {
                                        int grouping13 = grouping12;
                                        long groupingWidths2 = groupingWidths;
                                        if (((groupingWidths >>> (j * 4)) & 15) == ((long) grouping22)) {
                                            j++;
                                            grouping12 = grouping13;
                                            groupingWidths = groupingWidths2;
                                        } else if (DEBUGGING) {
                                            System.out.println("-> rejected due to inner grouping violation");
                                        }
                                    }
                                }
                            } else if (DEBUGGING) {
                                System.out.println("-> rejected due to final grouping violation");
                            }
                            i4++;
                            mode2 = mode;
                            integerOnly2 = integerOnly;
                            numGroupingRegions2 = ignoreExponent;
                            ignoreGrouping2 = ignoreGrouping;
                            state2 = state;
                            initialStateItem2 = initialStateItem;
                        }
                    } else if (DEBUGGING) {
                        boolean z8 = sawPrefix;
                        System.out.println("-> reject due to lack of exponent");
                    }
                } else {
                    mode = mode2;
                    integerOnly = integerOnly2;
                    ignoreExponent = numGroupingRegions2;
                    ignoreGrouping = ignoreGrouping2;
                    state = state2;
                    initialStateItem = initialStateItem2;
                }
                if (properties.getDecimalPatternMatchRequired()) {
                    if (item2.sawDecimalPoint != (properties.getDecimalSeparatorAlwaysShown() || properties.getMaximumFractionDigits() != 0)) {
                        if (DEBUGGING) {
                            System.out.println("-> rejected due to decimal point violation");
                        }
                        i4++;
                        mode2 = mode;
                        integerOnly2 = integerOnly;
                        numGroupingRegions2 = ignoreExponent;
                        ignoreGrouping2 = ignoreGrouping;
                        state2 = state;
                        initialStateItem2 = initialStateItem;
                    }
                }
                if (!z2 || item2.sawCurrency) {
                    if (best == null) {
                        stateItem = item2;
                    } else if (item2.score > best.score) {
                        stateItem = item2;
                    } else if (item2.trailingCount < best.trailingCount) {
                        stateItem = item2;
                    } else {
                        i4++;
                        mode2 = mode;
                        integerOnly2 = integerOnly;
                        numGroupingRegions2 = ignoreExponent;
                        ignoreGrouping2 = ignoreGrouping;
                        state2 = state;
                        initialStateItem2 = initialStateItem;
                    }
                    best = stateItem;
                    i4++;
                    mode2 = mode;
                    integerOnly2 = integerOnly;
                    numGroupingRegions2 = ignoreExponent;
                    ignoreGrouping2 = ignoreGrouping;
                    state2 = state;
                    initialStateItem2 = initialStateItem;
                } else {
                    if (DEBUGGING) {
                        System.out.println("-> rejected due to lack of currency");
                    }
                    i4++;
                    mode2 = mode;
                    integerOnly2 = integerOnly;
                    numGroupingRegions2 = ignoreExponent;
                    ignoreGrouping2 = ignoreGrouping;
                    state2 = state;
                    initialStateItem2 = initialStateItem;
                }
            }
            integerOnly = integerOnly2;
            ignoreExponent = numGroupingRegions2;
            ignoreGrouping = ignoreGrouping2;
            state = state2;
            initialStateItem = initialStateItem2;
            i4++;
            mode2 = mode;
            integerOnly2 = integerOnly;
            numGroupingRegions2 = ignoreExponent;
            ignoreGrouping2 = ignoreGrouping;
            state2 = state;
            initialStateItem2 = initialStateItem;
        }
        boolean z9 = integerOnly2;
        boolean z10 = numGroupingRegions2;
        boolean z11 = ignoreGrouping2;
        ParserState parserState = state2;
        StateItem stateItem2 = initialStateItem2;
        if (DEBUGGING) {
            System.out.println("- - - - - - - - - -");
        }
        if (best != null) {
            parsePosition.setIndex(offset - best.trailingCount);
            return best;
        }
        parsePosition.setErrorIndex(offset);
        return null;
    }

    private static void acceptWhitespace(int cp, StateName nextName, ParserState state, StateItem item) {
        if (UNISET_WHITESPACE.contains(cp)) {
            state.getNext().copyFrom(item, nextName, cp);
        }
    }

    private static void acceptBidi(int cp, StateName nextName, ParserState state, StateItem item) {
        if (UNISET_BIDI.contains(cp)) {
            state.getNext().copyFrom(item, nextName, cp);
        }
    }

    private static void acceptPadding(int cp, StateName nextName, ParserState state, StateItem item) {
        CharSequence padding = state.properties.getPadString();
        if (!(padding == null || padding.length() == 0 || cp != Character.codePointAt(padding, 0))) {
            state.getNext().copyFrom(item, nextName, cp);
        }
    }

    private static void acceptIntegerDigit(int cp, StateName nextName, ParserState state, StateItem item) {
        acceptDigitHelper(cp, nextName, state, item, DigitType.INTEGER);
    }

    private static void acceptFractionDigit(int cp, StateName nextName, ParserState state, StateItem item) {
        acceptDigitHelper(cp, nextName, state, item, DigitType.FRACTION);
    }

    private static void acceptExponentDigit(int cp, StateName nextName, ParserState state, StateItem item) {
        acceptDigitHelper(cp, nextName, state, item, DigitType.EXPONENT);
    }

    private static void acceptDigitHelper(int cp, StateName nextName, ParserState state, StateItem item, DigitType type) {
        byte d = (byte) UCharacter.digit(cp, 10);
        StateItem next = null;
        if (d >= 0) {
            next = state.getNext().copyFrom(item, nextName, -1);
        }
        if (d < 0 && (state.mode == ParseMode.LENIENT || state.mode == ParseMode.STRICT)) {
            if (state.digitTrie == null) {
                byte digit = d;
                for (byte d2 = 0; d2 < 10; d2 = (byte) (d2 + 1)) {
                    if (cp == Character.codePointAt(state.symbols.getDigitStringsLocal()[d2], 0)) {
                        digit = d2;
                        next = state.getNext().copyFrom(item, nextName, -1);
                    }
                }
                d = digit;
            } else {
                acceptDigitTrie(cp, nextName, state, item, type);
            }
        }
        recordDigit(next, d, type);
    }

    private static void recordDigit(StateItem next, byte digit, DigitType type) {
        if (next != null) {
            next.appendDigit(digit, type);
            if (type == DigitType.INTEGER && (next.groupingWidths & 15) < 15) {
                next.groupingWidths++;
            }
        }
    }

    private static void acceptMinusOrPlusSign(int cp, StateName nextName, ParserState state, StateItem item, boolean exponent) {
        acceptMinusSign(cp, nextName, null, state, item, exponent);
        acceptPlusSign(cp, nextName, null, state, item, exponent);
    }

    private static long acceptMinusSign(int cp, StateName returnTo1, StateName returnTo2, ParserState state, StateItem item, boolean exponent) {
        if (!UNISET_MINUS.contains(cp)) {
            return 0;
        }
        StateItem next = state.getNext().copyFrom(item, returnTo1, -1);
        next.returnTo1 = returnTo2;
        if (exponent) {
            next.sawNegativeExponent = true;
        } else {
            next.sawNegative = true;
        }
        return 1 << state.lastInsertedIndex();
    }

    private static long acceptPlusSign(int cp, StateName returnTo1, StateName returnTo2, ParserState state, StateItem item, boolean exponent) {
        if (!UNISET_PLUS.contains(cp)) {
            return 0;
        }
        state.getNext().copyFrom(item, returnTo1, -1).returnTo1 = returnTo2;
        return 1 << state.lastInsertedIndex();
    }

    private static void acceptGrouping(int cp, StateName nextName, ParserState state, StateItem item) {
        if (item.groupingCp == -1) {
            SeparatorType cpType = SeparatorType.fromCp(cp, state.mode);
            if (!(cp == state.groupingCp1 || cp == state.groupingCp2)) {
                if (cpType != SeparatorType.UNKNOWN) {
                    if (state.groupingMode == GroupingMode.RESTRICTED) {
                        if (!(cpType == state.groupingType1 && cpType == state.groupingType2)) {
                            return;
                        }
                    } else if (cpType != SeparatorType.COMMA_LIKE || (state.decimalType1 != SeparatorType.COMMA_LIKE && state.decimalType2 != SeparatorType.COMMA_LIKE)) {
                        if (cpType == SeparatorType.PERIOD_LIKE && (state.decimalType1 == SeparatorType.PERIOD_LIKE || state.decimalType2 == SeparatorType.PERIOD_LIKE)) {
                            return;
                        }
                    } else {
                        return;
                    }
                } else {
                    return;
                }
            }
            StateItem next = state.getNext().copyFrom(item, nextName, cp);
            next.groupingCp = cp;
            next.groupingWidths <<= 4;
        } else if (cp == item.groupingCp) {
            state.getNext().copyFrom(item, nextName, cp).groupingWidths <<= 4;
        }
    }

    private static void acceptDecimalPoint(int cp, StateName nextName, ParserState state, StateItem item) {
        if (cp != item.groupingCp) {
            SeparatorType cpType = SeparatorType.fromCp(cp, state.mode);
            if (cpType != state.decimalType1 && cpType != state.decimalType2) {
                return;
            }
            if ((cpType != SeparatorType.OTHER_GROUPING && cpType != SeparatorType.UNKNOWN) || cp == state.decimalCp1 || cp == state.decimalCp2) {
                state.getNext().copyFrom(item, nextName, -1).sawDecimalPoint = true;
            }
        }
    }

    private static void acceptNan(int cp, StateName nextName, ParserState state, StateItem item) {
        long added = acceptString(cp, nextName, null, state, item, state.symbols.getNaN(), 0, false);
        for (int i = Long.numberOfTrailingZeros(added); (1 << i) <= added; i++) {
            if (((1 << i) & added) != 0) {
                state.getItem(i).sawNaN = true;
            }
        }
    }

    private static void acceptInfinity(int cp, StateName nextName, ParserState state, StateItem item) {
        long added = acceptString(cp, nextName, null, state, item, state.symbols.getInfinity(), 0, false);
        for (int i = Long.numberOfTrailingZeros(added); (1 << i) <= added; i++) {
            if (((1 << i) & added) != 0) {
                state.getItem(i).sawInfinity = true;
            }
        }
    }

    private static void acceptExponentSeparator(int cp, StateName nextName, ParserState state, StateItem item) {
        acceptString(cp, nextName, null, state, item, state.symbols.getExponentSeparator(), 0, true);
    }

    private static void acceptPrefix(int cp, StateName nextName, ParserState state, StateItem item) {
        for (AffixHolder holder : state.affixHolders) {
            acceptAffixHolder(cp, nextName, state, item, holder, true);
        }
    }

    private static void acceptSuffix(int cp, StateName nextName, ParserState state, StateItem item) {
        if (item.affix != null) {
            acceptAffixHolder(cp, nextName, state, item, item.affix, false);
            return;
        }
        for (AffixHolder holder : state.affixHolders) {
            acceptAffixHolder(cp, nextName, state, item, holder, false);
        }
    }

    private static void acceptAffixHolder(int cp, StateName nextName, ParserState state, StateItem item, AffixHolder holder, boolean prefix) {
        long added;
        AffixHolder affixHolder = holder;
        if (affixHolder != null) {
            String str = prefix ? affixHolder.p : affixHolder.s;
            if (affixHolder.strings) {
                added = acceptString(cp, nextName, null, state, item, str, 0, false);
            } else {
                added = acceptAffixPattern(cp, nextName, state, item, str, AffixUtils.nextToken(0, str));
            }
            for (int i = Long.numberOfTrailingZeros(added); (1 << i) <= added; i++) {
                if (((1 << i) & added) != 0) {
                    StateItem next = state.getItem(i);
                    next.affix = affixHolder;
                    if (prefix) {
                        next.sawPrefix = true;
                    }
                    if (!prefix) {
                        next.sawSuffix = true;
                    }
                    if (affixHolder.negative) {
                        next.sawNegative = true;
                    }
                    next.score += 10;
                    if (!affixHolder.negative) {
                        next.score++;
                    }
                    if (!next.sawPrefix && affixHolder.p.isEmpty()) {
                        next.score += 5;
                    }
                    if (!next.sawSuffix && affixHolder.s.isEmpty()) {
                        next.score += 5;
                    }
                } else {
                    ParserState parserState = state;
                }
            }
            ParserState parserState2 = state;
        }
    }

    private static long acceptStringOffset(int cp, ParserState state, StateItem item) {
        return acceptString(cp, item.returnTo1, item.returnTo2, state, item, item.currentString, item.currentOffset, item.currentTrailing);
    }

    private static long acceptString(int cp, StateName ret1, StateName ret2, ParserState state, StateItem item, CharSequence str, int offset, boolean trailing) {
        if (str == null || str.length() == 0) {
            int i = offset;
            return 0;
        }
        return acceptStringOrAffixPatternWithIgnorables(cp, ret1, ret2, state, item, str, (long) offset, trailing, true);
    }

    private static long acceptStringNonIgnorable(int cp, StateName ret1, StateName ret2, ParserState state, StateItem item, CharSequence str, boolean trailing, int referenceCp, long firstOffsetOrTag, long nextOffsetOrTag) {
        long added = 0;
        int firstOffset = (int) firstOffsetOrTag;
        int nextOffset = (int) nextOffsetOrTag;
        int i = cp;
        ParserState parserState = state;
        if (!codePointEquals(referenceCp, i, parserState)) {
            return 0;
        }
        if (firstOffset < str.length()) {
            added = 0 | acceptStringHelper(i, ret1, ret2, parserState, item, str, firstOffset, trailing);
        }
        if (nextOffset >= str.length()) {
            added |= acceptStringHelper(cp, ret1, ret2, state, item, str, nextOffset, trailing);
        }
        return added;
    }

    private static long acceptStringHelper(int cp, StateName returnTo1, StateName returnTo2, ParserState state, StateItem item, CharSequence str, int newOffset, boolean trailing) {
        StateItem next = state.getNext().copyFrom(item, null, cp);
        next.score++;
        if (newOffset < str.length()) {
            next.name = StateName.INSIDE_STRING;
            next.returnTo1 = returnTo1;
            next.returnTo2 = returnTo2;
            next.currentString = str;
            next.currentOffset = newOffset;
            next.currentTrailing = trailing;
        } else {
            next.name = returnTo1;
            if (!trailing) {
                next.trailingCount = 0;
            }
            next.returnTo1 = returnTo2;
            next.returnTo2 = null;
        }
        return 1 << state.lastInsertedIndex();
    }

    private static long acceptAffixPatternOffset(int cp, ParserState state, StateItem item) {
        return acceptAffixPattern(cp, item.returnTo1, state, item, item.currentAffixPattern, item.currentStepwiseParserTag);
    }

    private static long acceptAffixPattern(int cp, StateName ret1, ParserState state, StateItem item, CharSequence str, long tag) {
        if (str == null || str.length() == 0) {
            return 0;
        }
        return acceptStringOrAffixPatternWithIgnorables(cp, ret1, null, state, item, str, tag, false, false);
    }

    private static long acceptAffixPatternNonIgnorable(int cp, StateName returnTo, ParserState state, StateItem item, CharSequence str, int typeOrCp, long firstTag, long nextTag) {
        int i = cp;
        StateName stateName = returnTo;
        ParserState parserState = state;
        StateItem stateItem = item;
        int i2 = typeOrCp;
        long j = firstTag;
        int resolvedCp = -1;
        CharSequence resolvedStr = null;
        boolean resolvedMinusSign = false;
        boolean resolvedPlusSign = false;
        boolean resolvedCurrency = false;
        if (i2 < 0) {
            if (i2 != -15) {
                switch (i2) {
                    case AffixUtils.TYPE_CURRENCY_QUINT:
                    case AffixUtils.TYPE_CURRENCY_QUAD:
                    case AffixUtils.TYPE_CURRENCY_TRIPLE:
                    case AffixUtils.TYPE_CURRENCY_DOUBLE:
                    case AffixUtils.TYPE_CURRENCY_SINGLE:
                        break;
                    case AffixUtils.TYPE_PERMILLE:
                        resolvedStr = parserState.symbols.getPerMillString();
                        if (!(resolvedStr.length() == 1 && resolvedStr.charAt(0) == 8240)) {
                            resolvedCp = 8240;
                            break;
                        }
                    case AffixUtils.TYPE_PERCENT:
                        resolvedStr = parserState.symbols.getPercentString();
                        if (!(resolvedStr.length() == 1 && resolvedStr.charAt(0) == '%')) {
                            resolvedCp = 37;
                            break;
                        }
                    case -2:
                        resolvedPlusSign = true;
                        break;
                    case -1:
                        resolvedMinusSign = true;
                        break;
                    default:
                        throw new AssertionError();
                }
            }
            resolvedCurrency = true;
        } else {
            resolvedCp = i2;
        }
        int resolvedCp2 = resolvedCp;
        CharSequence resolvedStr2 = resolvedStr;
        boolean resolvedMinusSign2 = resolvedMinusSign;
        boolean resolvedPlusSign2 = resolvedPlusSign;
        boolean resolvedCurrency2 = resolvedCurrency;
        long added = 0;
        if (resolvedCp2 < 0 || !codePointEquals(i, resolvedCp2, parserState)) {
        } else {
            if (j >= 0) {
                int i3 = resolvedCp2;
                added = 0 | acceptAffixPatternHelper(i, stateName, parserState, stateItem, str, j);
            }
            if (nextTag < 0) {
                added |= acceptAffixPatternHelper(i, stateName, parserState, stateItem, str, nextTag);
            }
        }
        if (resolvedMinusSign2) {
            if (j >= 0) {
                added |= acceptMinusSign(i, StateName.INSIDE_AFFIX_PATTERN, stateName, parserState, stateItem, false);
            }
            if (nextTag < 0) {
                added |= acceptMinusSign(i, stateName, null, parserState, stateItem, false);
            }
            if (added == 0) {
                String mss = parserState.symbols.getMinusSignString();
                int mssCp = Character.codePointAt(mss, 0);
                if (mss.length() != Character.charCount(mssCp) || !UNISET_MINUS.contains(mssCp)) {
                    resolvedStr2 = mss;
                }
            }
        }
        if (resolvedPlusSign2) {
            if (j >= 0) {
                added |= acceptPlusSign(i, StateName.INSIDE_AFFIX_PATTERN, stateName, parserState, stateItem, false);
            }
            if (nextTag < 0) {
                added |= acceptPlusSign(i, stateName, null, parserState, stateItem, false);
            }
            if (added == 0) {
                String pss = parserState.symbols.getPlusSignString();
                int pssCp = Character.codePointAt(pss, 0);
                if (pss.length() != Character.charCount(pssCp) || !UNISET_MINUS.contains(pssCp)) {
                    resolvedStr2 = pss;
                }
            }
        }
        if (resolvedStr2 != null) {
            if (j >= 0) {
                added |= acceptString(i, StateName.INSIDE_AFFIX_PATTERN, stateName, parserState, stateItem, resolvedStr2, 0, false);
            }
            if (nextTag < 0) {
                added |= acceptString(i, stateName, null, parserState, stateItem, resolvedStr2, 0, false);
            }
        }
        if (resolvedCurrency2) {
            if (j >= 0) {
                added |= acceptCurrency(i, StateName.INSIDE_AFFIX_PATTERN, stateName, parserState, stateItem);
            }
            if (nextTag < 0) {
                added |= acceptCurrency(i, stateName, null, parserState, stateItem);
            }
        }
        long added2 = added;
        for (int i4 = Long.numberOfTrailingZeros(added2); (1 << i4) <= added2; i4++) {
            if (((1 << i4) & added2) != 0) {
                parserState.getItem(i4).currentAffixPattern = str;
                parserState.getItem(i4).currentStepwiseParserTag = j;
            } else {
                CharSequence charSequence = str;
            }
        }
        CharSequence charSequence2 = str;
        return added2;
    }

    private static long acceptAffixPatternHelper(int cp, StateName returnTo, ParserState state, StateItem item, CharSequence str, long newTag) {
        StateItem next = state.getNext().copyFrom(item, null, cp);
        next.score++;
        if (newTag >= 0) {
            next.name = StateName.INSIDE_AFFIX_PATTERN;
            next.returnTo1 = returnTo;
            next.currentAffixPattern = str;
            next.currentStepwiseParserTag = newTag;
        } else {
            next.name = returnTo;
            next.trailingCount = 0;
            next.returnTo1 = null;
        }
        return 1 << state.lastInsertedIndex();
    }

    /* JADX WARNING: Removed duplicated region for block: B:23:0x0054  */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x005a  */
    /* JADX WARNING: Removed duplicated region for block: B:51:0x0105 A[LOOP:0: B:7:0x0022->B:51:0x0105, LOOP_END] */
    /* JADX WARNING: Removed duplicated region for block: B:69:0x014c  */
    /* JADX WARNING: Removed duplicated region for block: B:70:0x0152  */
    /* JADX WARNING: Removed duplicated region for block: B:78:0x0189 A[LOOP:1: B:54:0x0119->B:78:0x0189, LOOP_END] */
    /* JADX WARNING: Removed duplicated region for block: B:79:0x0050 A[EDGE_INSN: B:79:0x0050->B:21:0x0050 ?: BREAK  , SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:84:0x015d A[SYNTHETIC] */
    private static long acceptStringOrAffixPatternWithIgnorables(int cp, StateName ret1, StateName ret2, ParserState state, StateItem item, CharSequence str, long offsetOrTag, boolean trailing, boolean isString) {
        long nextOffsetOrTag;
        int i;
        long offsetOrTag2;
        int typeOrCp;
        long j;
        long nextOffsetOrTag2;
        long firstOffsetOrTag;
        long j2;
        int i2;
        long prevOffsetOrTag;
        long j3;
        long nextOffsetOrTag3;
        long firstOffsetOrTag2;
        int i3;
        long j4;
        long firstOffsetOrTag3;
        long nextOffsetOrTag4;
        long j5;
        long j6;
        int i4 = cp;
        ParserState parserState = state;
        CharSequence charSequence = str;
        if (isString) {
            nextOffsetOrTag = offsetOrTag;
            i = Character.codePointAt(charSequence, (int) nextOffsetOrTag);
        } else {
            nextOffsetOrTag = offsetOrTag;
            i = AffixUtils.getTypeOrCp(offsetOrTag);
        }
        int typeOrCp2 = i;
        if (isIgnorable(typeOrCp2, parserState)) {
            int nextTypeOrCp = typeOrCp2;
            long nextOffsetOrTag5 = nextOffsetOrTag;
            long nextOffsetOrTag6 = 0;
            while (true) {
                prevOffsetOrTag = nextOffsetOrTag5;
                if (isString) {
                    j3 = ((long) Character.charCount(nextTypeOrCp)) + nextOffsetOrTag5;
                } else {
                    j3 = AffixUtils.nextToken(nextOffsetOrTag5, charSequence);
                }
                nextOffsetOrTag3 = j3;
                firstOffsetOrTag2 = nextOffsetOrTag6 == 0 ? nextOffsetOrTag3 : nextOffsetOrTag6;
                if (isString) {
                    if (nextOffsetOrTag3 >= ((long) str.length())) {
                        break;
                    }
                    if (!isString) {
                        i3 = Character.codePointAt(charSequence, (int) nextOffsetOrTag3);
                    } else {
                        i3 = AffixUtils.getTypeOrCp(nextOffsetOrTag3);
                    }
                    nextTypeOrCp = i3;
                    if (isIgnorable(nextTypeOrCp, parserState)) {
                        break;
                    }
                    nextOffsetOrTag5 = nextOffsetOrTag3;
                    nextOffsetOrTag6 = firstOffsetOrTag2;
                    long j7 = offsetOrTag;
                } else {
                    if (nextOffsetOrTag3 < 0) {
                        break;
                    }
                    if (!isString) {
                    }
                    nextTypeOrCp = i3;
                    if (isIgnorable(nextTypeOrCp, parserState)) {
                    }
                }
            }
            nextTypeOrCp = Integer.MIN_VALUE;
            int nextTypeOrCp2 = nextTypeOrCp;
            if (nextTypeOrCp2 != Integer.MIN_VALUE) {
                long nextOffsetOrTag7 = nextOffsetOrTag3;
                if (isIgnorable(i4, parserState)) {
                    if (isString) {
                        long prevOffsetOrTag2 = prevOffsetOrTag;
                        long j8 = prevOffsetOrTag2;
                        j4 = acceptStringHelper(i4, ret1, ret2, parserState, item, charSequence, (int) prevOffsetOrTag2, trailing);
                    } else {
                        j4 = acceptAffixPatternHelper(i4, ret1, parserState, item, charSequence, prevOffsetOrTag);
                    }
                    return j4;
                }
                typeOrCp = nextTypeOrCp2;
                offsetOrTag2 = nextOffsetOrTag7;
            } else if (codePointEquals(i4, typeOrCp2, parserState)) {
                if (isString) {
                    firstOffsetOrTag3 = firstOffsetOrTag2;
                    nextOffsetOrTag4 = nextOffsetOrTag3;
                    j5 = acceptStringHelper(i4, ret1, ret2, parserState, item, charSequence, (int) firstOffsetOrTag2, trailing);
                } else {
                    firstOffsetOrTag3 = firstOffsetOrTag2;
                    nextOffsetOrTag4 = nextOffsetOrTag3;
                    j5 = acceptAffixPatternHelper(i4, ret1, parserState, item, charSequence, firstOffsetOrTag3);
                }
                long added = 0 | j5;
                if (firstOffsetOrTag3 != nextOffsetOrTag4) {
                    if (isString) {
                        j6 = acceptStringHelper(i4, ret1, ret2, parserState, item, charSequence, (int) nextOffsetOrTag4, trailing);
                    } else {
                        j6 = acceptAffixPatternHelper(i4, ret1, parserState, item, charSequence, nextOffsetOrTag4);
                    }
                    added |= j6;
                }
                return added;
            } else {
                long j9 = nextOffsetOrTag3;
                return 0;
            }
        } else {
            typeOrCp = typeOrCp2;
            offsetOrTag2 = offsetOrTag;
        }
        int nextTypeOrCp3 = typeOrCp;
        long nextOffsetOrTag8 = offsetOrTag2;
        long firstOffsetOrTag4 = 0;
        while (true) {
            if (isString) {
                j = ((long) Character.charCount(nextTypeOrCp3)) + nextOffsetOrTag8;
            } else {
                j = AffixUtils.nextToken(nextOffsetOrTag8, charSequence);
            }
            nextOffsetOrTag2 = j;
            if (firstOffsetOrTag4 == 0) {
                firstOffsetOrTag4 = nextOffsetOrTag2;
            }
            firstOffsetOrTag = firstOffsetOrTag4;
            if (!isString) {
                if (nextOffsetOrTag2 < 0) {
                    break;
                }
                if (!isString) {
                }
                nextTypeOrCp3 = i2;
                if (isIgnorable(nextTypeOrCp3, parserState)) {
                }
            } else if (nextOffsetOrTag2 >= ((long) str.length())) {
                break;
            } else {
                if (!isString) {
                    i2 = Character.codePointAt(charSequence, (int) nextOffsetOrTag2);
                } else {
                    i2 = AffixUtils.getTypeOrCp(nextOffsetOrTag2);
                }
                nextTypeOrCp3 = i2;
                if (isIgnorable(nextTypeOrCp3, parserState)) {
                    int i5 = nextTypeOrCp3;
                    break;
                }
                nextOffsetOrTag8 = nextOffsetOrTag2;
                firstOffsetOrTag4 = firstOffsetOrTag;
            }
        }
        if (isString) {
            long j10 = nextOffsetOrTag2;
            j2 = acceptStringNonIgnorable(i4, ret1, ret2, parserState, item, charSequence, trailing, typeOrCp, firstOffsetOrTag, nextOffsetOrTag2);
        } else {
            j2 = acceptAffixPatternNonIgnorable(i4, ret1, parserState, item, charSequence, typeOrCp, firstOffsetOrTag, nextOffsetOrTag2);
        }
        return j2;
    }

    private static void acceptCurrency(int cp, StateName nextName, ParserState state, StateItem item) {
        acceptCurrency(cp, nextName, null, state, item);
    }

    private static long acceptCurrency(int cp, StateName returnTo1, StateName returnTo2, ParserState state, StateItem item) {
        String str1;
        String str2;
        String str22;
        int i = cp;
        ParserState parserState = state;
        StateItem stateItem = item;
        if (stateItem.sawCurrency) {
            return 0;
        }
        Currency currency = parserState.properties.getCurrency();
        if (currency != null) {
            str1 = currency.getName(parserState.symbols.getULocale(), 0, (boolean[]) null);
            str2 = currency.getCurrencyCode();
        } else {
            currency = parserState.symbols.getCurrency();
            str1 = parserState.symbols.getCurrencySymbol();
            str2 = parserState.symbols.getInternationalCurrencySymbol();
        }
        String str23 = str2;
        StateName stateName = returnTo2;
        ParserState parserState2 = parserState;
        StateItem stateItem2 = stateItem;
        String str24 = str23;
        long added = 0 | acceptString(i, returnTo1, stateName, parserState2, stateItem2, str1, 0, false) | acceptString(i, returnTo1, stateName, parserState2, stateItem2, str24, 0, false);
        int i2 = Long.numberOfTrailingZeros(added);
        while ((1 << i2) <= added) {
            if (((1 << i2) & added) != 0) {
                parserState.getItem(i2).sawCurrency = true;
                str22 = str24;
                parserState.getItem(i2).isoCode = str22;
            } else {
                str22 = str24;
            }
            i2++;
            str24 = str22;
        }
        if (parserState.parseCurrency) {
            ULocale uloc = parserState.symbols.getULocale();
            StateName stateName2 = returnTo2;
            ParserState parserState3 = parserState;
            StateItem stateItem3 = stateItem;
            added = added | acceptCurrencyHelper(i, returnTo1, stateName2, parserState3, stateItem3, Currency.openParseState(uloc, i, 1)) | acceptCurrencyHelper(i, returnTo1, stateName2, parserState3, stateItem3, Currency.openParseState(uloc, i, 0));
        }
        return added;
    }

    private static void acceptCurrencyOffset(int cp, ParserState state, StateItem item) {
        acceptCurrencyHelper(cp, item.returnTo1, item.returnTo2, state, item, item.currentCurrencyTrieState);
    }

    private static long acceptCurrencyHelper(int cp, StateName returnTo1, StateName returnTo2, ParserState state, StateItem item, TextTrieMap<Currency.CurrencyStringInfo>.ParseState trieState) {
        if (trieState == null) {
            return 0;
        }
        trieState.accept(cp);
        long added = 0;
        Iterator<Currency.CurrencyStringInfo> currentMatches = trieState.getCurrentMatches();
        if (currentMatches != null) {
            StateItem next = state.getNext().copyFrom(item, returnTo1, -1);
            next.returnTo1 = returnTo2;
            next.returnTo2 = null;
            next.sawCurrency = true;
            next.isoCode = currentMatches.next().getISOCode();
            added = 0 | (1 << state.lastInsertedIndex());
        }
        if (!trieState.atEnd()) {
            StateItem next2 = state.getNext().copyFrom(item, StateName.INSIDE_CURRENCY, -1);
            next2.returnTo1 = returnTo1;
            next2.returnTo2 = returnTo2;
            next2.currentCurrencyTrieState = trieState;
            added |= 1 << state.lastInsertedIndex();
        }
        return added;
    }

    private static long acceptDigitTrie(int cp, StateName nextName, ParserState state, StateItem item, DigitType type) {
        TextTrieMap<V>.ParseState openParseState = state.digitTrie.openParseState(cp);
        if (openParseState == null) {
            return 0;
        }
        return acceptDigitTrieHelper(cp, nextName, state, item, type, openParseState);
    }

    private static void acceptDigitTrieOffset(int cp, ParserState state, StateItem item) {
        acceptDigitTrieHelper(cp, item.returnTo1, state, item, item.currentDigitType, item.currentDigitTrieState);
    }

    private static long acceptDigitTrieHelper(int cp, StateName returnTo1, ParserState state, StateItem item, DigitType type, TextTrieMap<Byte>.ParseState trieState) {
        if (trieState == null) {
            return 0;
        }
        trieState.accept(cp);
        long added = 0;
        Iterator<Byte> currentMatches = trieState.getCurrentMatches();
        if (currentMatches != null) {
            byte digit = currentMatches.next().byteValue();
            StateItem next = state.getNext().copyFrom(item, returnTo1, -1);
            next.returnTo1 = null;
            recordDigit(next, digit, type);
            added = 0 | (1 << state.lastInsertedIndex());
        }
        if (trieState.atEnd() == 0) {
            StateItem next2 = state.getNext().copyFrom(item, StateName.INSIDE_DIGIT, -1);
            next2.returnTo1 = returnTo1;
            next2.currentDigitTrieState = trieState;
            next2.currentDigitType = type;
            added |= 1 << state.lastInsertedIndex();
        }
        return added;
    }

    private static boolean codePointEquals(int cp1, int cp2, ParserState state) {
        if (!state.caseSensitive) {
            cp1 = UCharacter.foldCase(cp1, true);
            cp2 = UCharacter.foldCase(cp2, true);
        }
        if (cp1 == cp2) {
            return true;
        }
        return false;
    }

    private static boolean isIgnorable(int cp, ParserState state) {
        boolean z = false;
        if (cp < 0) {
            return false;
        }
        if (UNISET_BIDI.contains(cp)) {
            return true;
        }
        if (state.mode == ParseMode.LENIENT && UNISET_WHITESPACE.contains(cp)) {
            z = true;
        }
        return z;
    }
}
