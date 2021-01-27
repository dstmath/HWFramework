package ohos.global.icu.number;

import java.math.BigDecimal;
import java.math.RoundingMode;
import ohos.com.sun.org.apache.xalan.internal.templates.Constants;
import ohos.global.icu.impl.CacheBase;
import ohos.global.icu.impl.PatternProps;
import ohos.global.icu.impl.SoftCache;
import ohos.global.icu.impl.StringSegment;
import ohos.global.icu.impl.locale.LanguageTag;
import ohos.global.icu.impl.number.MacroProps;
import ohos.global.icu.impl.number.RoundingUtils;
import ohos.global.icu.number.NumberFormatter;
import ohos.global.icu.number.Precision;
import ohos.global.icu.text.NumberingSystem;
import ohos.global.icu.util.BytesTrie;
import ohos.global.icu.util.CharsTrie;
import ohos.global.icu.util.CharsTrieBuilder;
import ohos.global.icu.util.Currency;
import ohos.global.icu.util.MeasureUnit;
import ohos.global.icu.util.NoUnit;
import ohos.global.icu.util.StringTrieBuilder;

/* access modifiers changed from: package-private */
public class NumberSkeletonImpl {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    static final String SERIALIZED_STEM_TRIE = buildStemTrie();
    static final StemEnum[] STEM_ENUM_VALUES = StemEnum.values();
    private static final CacheBase<String, UnlocalizedNumberFormatter, Void> cache = new SoftCache<String, UnlocalizedNumberFormatter, Void>() {
        /* class ohos.global.icu.number.NumberSkeletonImpl.AnonymousClass1 */

        /* access modifiers changed from: protected */
        public UnlocalizedNumberFormatter createInstance(String str, Void r2) {
            return NumberSkeletonImpl.create(str);
        }
    };

    /* access modifiers changed from: package-private */
    public enum ParseState {
        STATE_NULL,
        STATE_SCIENTIFIC,
        STATE_FRACTION_PRECISION,
        STATE_INCREMENT_PRECISION,
        STATE_MEASURE_UNIT,
        STATE_PER_MEASURE_UNIT,
        STATE_CURRENCY_UNIT,
        STATE_INTEGER_WIDTH,
        STATE_NUMBERING_SYSTEM,
        STATE_SCALE
    }

    /* access modifiers changed from: package-private */
    public enum StemEnum {
        STEM_COMPACT_SHORT,
        STEM_COMPACT_LONG,
        STEM_SCIENTIFIC,
        STEM_ENGINEERING,
        STEM_NOTATION_SIMPLE,
        STEM_BASE_UNIT,
        STEM_PERCENT,
        STEM_PERMILLE,
        STEM_PRECISION_INTEGER,
        STEM_PRECISION_UNLIMITED,
        STEM_PRECISION_CURRENCY_STANDARD,
        STEM_PRECISION_CURRENCY_CASH,
        STEM_ROUNDING_MODE_CEILING,
        STEM_ROUNDING_MODE_FLOOR,
        STEM_ROUNDING_MODE_DOWN,
        STEM_ROUNDING_MODE_UP,
        STEM_ROUNDING_MODE_HALF_EVEN,
        STEM_ROUNDING_MODE_HALF_DOWN,
        STEM_ROUNDING_MODE_HALF_UP,
        STEM_ROUNDING_MODE_UNNECESSARY,
        STEM_GROUP_OFF,
        STEM_GROUP_MIN2,
        STEM_GROUP_AUTO,
        STEM_GROUP_ON_ALIGNED,
        STEM_GROUP_THOUSANDS,
        STEM_LATIN,
        STEM_UNIT_WIDTH_NARROW,
        STEM_UNIT_WIDTH_SHORT,
        STEM_UNIT_WIDTH_FULL_NAME,
        STEM_UNIT_WIDTH_ISO_CODE,
        STEM_UNIT_WIDTH_HIDDEN,
        STEM_SIGN_AUTO,
        STEM_SIGN_ALWAYS,
        STEM_SIGN_NEVER,
        STEM_SIGN_ACCOUNTING,
        STEM_SIGN_ACCOUNTING_ALWAYS,
        STEM_SIGN_EXCEPT_ZERO,
        STEM_SIGN_ACCOUNTING_EXCEPT_ZERO,
        STEM_DECIMAL_AUTO,
        STEM_DECIMAL_ALWAYS,
        STEM_PRECISION_INCREMENT,
        STEM_MEASURE_UNIT,
        STEM_PER_MEASURE_UNIT,
        STEM_CURRENCY,
        STEM_INTEGER_WIDTH,
        STEM_NUMBERING_SYSTEM,
        STEM_SCALE
    }

    NumberSkeletonImpl() {
    }

    static String buildStemTrie() {
        CharsTrieBuilder charsTrieBuilder = new CharsTrieBuilder();
        charsTrieBuilder.add("compact-short", StemEnum.STEM_COMPACT_SHORT.ordinal());
        charsTrieBuilder.add("compact-long", StemEnum.STEM_COMPACT_LONG.ordinal());
        charsTrieBuilder.add("scientific", StemEnum.STEM_SCIENTIFIC.ordinal());
        charsTrieBuilder.add("engineering", StemEnum.STEM_ENGINEERING.ordinal());
        charsTrieBuilder.add("notation-simple", StemEnum.STEM_NOTATION_SIMPLE.ordinal());
        charsTrieBuilder.add("base-unit", StemEnum.STEM_BASE_UNIT.ordinal());
        charsTrieBuilder.add(Constants.ATTRNAME_PERCENT, StemEnum.STEM_PERCENT.ordinal());
        charsTrieBuilder.add("permille", StemEnum.STEM_PERMILLE.ordinal());
        charsTrieBuilder.add("precision-integer", StemEnum.STEM_PRECISION_INTEGER.ordinal());
        charsTrieBuilder.add("precision-unlimited", StemEnum.STEM_PRECISION_UNLIMITED.ordinal());
        charsTrieBuilder.add("precision-currency-standard", StemEnum.STEM_PRECISION_CURRENCY_STANDARD.ordinal());
        charsTrieBuilder.add("precision-currency-cash", StemEnum.STEM_PRECISION_CURRENCY_CASH.ordinal());
        charsTrieBuilder.add("rounding-mode-ceiling", StemEnum.STEM_ROUNDING_MODE_CEILING.ordinal());
        charsTrieBuilder.add("rounding-mode-floor", StemEnum.STEM_ROUNDING_MODE_FLOOR.ordinal());
        charsTrieBuilder.add("rounding-mode-down", StemEnum.STEM_ROUNDING_MODE_DOWN.ordinal());
        charsTrieBuilder.add("rounding-mode-up", StemEnum.STEM_ROUNDING_MODE_UP.ordinal());
        charsTrieBuilder.add("rounding-mode-half-even", StemEnum.STEM_ROUNDING_MODE_HALF_EVEN.ordinal());
        charsTrieBuilder.add("rounding-mode-half-down", StemEnum.STEM_ROUNDING_MODE_HALF_DOWN.ordinal());
        charsTrieBuilder.add("rounding-mode-half-up", StemEnum.STEM_ROUNDING_MODE_HALF_UP.ordinal());
        charsTrieBuilder.add("rounding-mode-unnecessary", StemEnum.STEM_ROUNDING_MODE_UNNECESSARY.ordinal());
        charsTrieBuilder.add("group-off", StemEnum.STEM_GROUP_OFF.ordinal());
        charsTrieBuilder.add("group-min2", StemEnum.STEM_GROUP_MIN2.ordinal());
        charsTrieBuilder.add("group-auto", StemEnum.STEM_GROUP_AUTO.ordinal());
        charsTrieBuilder.add("group-on-aligned", StemEnum.STEM_GROUP_ON_ALIGNED.ordinal());
        charsTrieBuilder.add("group-thousands", StemEnum.STEM_GROUP_THOUSANDS.ordinal());
        charsTrieBuilder.add("latin", StemEnum.STEM_LATIN.ordinal());
        charsTrieBuilder.add("unit-width-narrow", StemEnum.STEM_UNIT_WIDTH_NARROW.ordinal());
        charsTrieBuilder.add("unit-width-short", StemEnum.STEM_UNIT_WIDTH_SHORT.ordinal());
        charsTrieBuilder.add("unit-width-full-name", StemEnum.STEM_UNIT_WIDTH_FULL_NAME.ordinal());
        charsTrieBuilder.add("unit-width-iso-code", StemEnum.STEM_UNIT_WIDTH_ISO_CODE.ordinal());
        charsTrieBuilder.add("unit-width-hidden", StemEnum.STEM_UNIT_WIDTH_HIDDEN.ordinal());
        charsTrieBuilder.add("sign-auto", StemEnum.STEM_SIGN_AUTO.ordinal());
        charsTrieBuilder.add("sign-always", StemEnum.STEM_SIGN_ALWAYS.ordinal());
        charsTrieBuilder.add("sign-never", StemEnum.STEM_SIGN_NEVER.ordinal());
        charsTrieBuilder.add("sign-accounting", StemEnum.STEM_SIGN_ACCOUNTING.ordinal());
        charsTrieBuilder.add("sign-accounting-always", StemEnum.STEM_SIGN_ACCOUNTING_ALWAYS.ordinal());
        charsTrieBuilder.add("sign-except-zero", StemEnum.STEM_SIGN_EXCEPT_ZERO.ordinal());
        charsTrieBuilder.add("sign-accounting-except-zero", StemEnum.STEM_SIGN_ACCOUNTING_EXCEPT_ZERO.ordinal());
        charsTrieBuilder.add("decimal-auto", StemEnum.STEM_DECIMAL_AUTO.ordinal());
        charsTrieBuilder.add("decimal-always", StemEnum.STEM_DECIMAL_ALWAYS.ordinal());
        charsTrieBuilder.add("precision-increment", StemEnum.STEM_PRECISION_INCREMENT.ordinal());
        charsTrieBuilder.add("measure-unit", StemEnum.STEM_MEASURE_UNIT.ordinal());
        charsTrieBuilder.add("per-measure-unit", StemEnum.STEM_PER_MEASURE_UNIT.ordinal());
        charsTrieBuilder.add("currency", StemEnum.STEM_CURRENCY.ordinal());
        charsTrieBuilder.add("integer-width", StemEnum.STEM_INTEGER_WIDTH.ordinal());
        charsTrieBuilder.add("numbering-system", StemEnum.STEM_NUMBERING_SYSTEM.ordinal());
        charsTrieBuilder.add("scale", StemEnum.STEM_SCALE.ordinal());
        return charsTrieBuilder.buildCharSequence(StringTrieBuilder.Option.FAST).toString();
    }

    /* access modifiers changed from: package-private */
    public static final class StemToObject {
        StemToObject() {
        }

        /* access modifiers changed from: private */
        public static Notation notation(StemEnum stemEnum) {
            int i = AnonymousClass2.$SwitchMap$ohos$global$icu$number$NumberSkeletonImpl$StemEnum[stemEnum.ordinal()];
            if (i == 1) {
                return Notation.compactShort();
            }
            if (i == 2) {
                return Notation.compactLong();
            }
            if (i == 3) {
                return Notation.scientific();
            }
            if (i == 4) {
                return Notation.engineering();
            }
            if (i == 5) {
                return Notation.simple();
            }
            throw new AssertionError();
        }

        /* access modifiers changed from: private */
        public static MeasureUnit unit(StemEnum stemEnum) {
            int i = AnonymousClass2.$SwitchMap$ohos$global$icu$number$NumberSkeletonImpl$StemEnum[stemEnum.ordinal()];
            if (i == 6) {
                return NoUnit.BASE;
            }
            if (i == 7) {
                return NoUnit.PERCENT;
            }
            if (i == 8) {
                return NoUnit.PERMILLE;
            }
            throw new AssertionError();
        }

        /* access modifiers changed from: private */
        public static Precision precision(StemEnum stemEnum) {
            switch (stemEnum) {
                case STEM_PRECISION_INTEGER:
                    return Precision.integer();
                case STEM_PRECISION_UNLIMITED:
                    return Precision.unlimited();
                case STEM_PRECISION_CURRENCY_STANDARD:
                    return Precision.currency(Currency.CurrencyUsage.STANDARD);
                case STEM_PRECISION_CURRENCY_CASH:
                    return Precision.currency(Currency.CurrencyUsage.CASH);
                default:
                    throw new AssertionError();
            }
        }

        /* access modifiers changed from: private */
        public static RoundingMode roundingMode(StemEnum stemEnum) {
            switch (stemEnum) {
                case STEM_ROUNDING_MODE_CEILING:
                    return RoundingMode.CEILING;
                case STEM_ROUNDING_MODE_FLOOR:
                    return RoundingMode.FLOOR;
                case STEM_ROUNDING_MODE_DOWN:
                    return RoundingMode.DOWN;
                case STEM_ROUNDING_MODE_UP:
                    return RoundingMode.UP;
                case STEM_ROUNDING_MODE_HALF_EVEN:
                    return RoundingMode.HALF_EVEN;
                case STEM_ROUNDING_MODE_HALF_DOWN:
                    return RoundingMode.HALF_DOWN;
                case STEM_ROUNDING_MODE_HALF_UP:
                    return RoundingMode.HALF_UP;
                case STEM_ROUNDING_MODE_UNNECESSARY:
                    return RoundingMode.UNNECESSARY;
                default:
                    throw new AssertionError();
            }
        }

        /* access modifiers changed from: private */
        public static NumberFormatter.GroupingStrategy groupingStrategy(StemEnum stemEnum) {
            switch (stemEnum) {
                case STEM_GROUP_OFF:
                    return NumberFormatter.GroupingStrategy.OFF;
                case STEM_GROUP_MIN2:
                    return NumberFormatter.GroupingStrategy.MIN2;
                case STEM_GROUP_AUTO:
                    return NumberFormatter.GroupingStrategy.AUTO;
                case STEM_GROUP_ON_ALIGNED:
                    return NumberFormatter.GroupingStrategy.ON_ALIGNED;
                case STEM_GROUP_THOUSANDS:
                    return NumberFormatter.GroupingStrategy.THOUSANDS;
                default:
                    return null;
            }
        }

        /* access modifiers changed from: private */
        public static NumberFormatter.UnitWidth unitWidth(StemEnum stemEnum) {
            switch (stemEnum) {
                case STEM_UNIT_WIDTH_NARROW:
                    return NumberFormatter.UnitWidth.NARROW;
                case STEM_UNIT_WIDTH_SHORT:
                    return NumberFormatter.UnitWidth.SHORT;
                case STEM_UNIT_WIDTH_FULL_NAME:
                    return NumberFormatter.UnitWidth.FULL_NAME;
                case STEM_UNIT_WIDTH_ISO_CODE:
                    return NumberFormatter.UnitWidth.ISO_CODE;
                case STEM_UNIT_WIDTH_HIDDEN:
                    return NumberFormatter.UnitWidth.HIDDEN;
                default:
                    return null;
            }
        }

        /* access modifiers changed from: private */
        public static NumberFormatter.SignDisplay signDisplay(StemEnum stemEnum) {
            switch (stemEnum) {
                case STEM_SIGN_AUTO:
                    return NumberFormatter.SignDisplay.AUTO;
                case STEM_SIGN_ALWAYS:
                    return NumberFormatter.SignDisplay.ALWAYS;
                case STEM_SIGN_NEVER:
                    return NumberFormatter.SignDisplay.NEVER;
                case STEM_SIGN_ACCOUNTING:
                    return NumberFormatter.SignDisplay.ACCOUNTING;
                case STEM_SIGN_ACCOUNTING_ALWAYS:
                    return NumberFormatter.SignDisplay.ACCOUNTING_ALWAYS;
                case STEM_SIGN_EXCEPT_ZERO:
                    return NumberFormatter.SignDisplay.EXCEPT_ZERO;
                case STEM_SIGN_ACCOUNTING_EXCEPT_ZERO:
                    return NumberFormatter.SignDisplay.ACCOUNTING_EXCEPT_ZERO;
                default:
                    return null;
            }
        }

        /* access modifiers changed from: private */
        public static NumberFormatter.DecimalSeparatorDisplay decimalSeparatorDisplay(StemEnum stemEnum) {
            int i = AnonymousClass2.$SwitchMap$ohos$global$icu$number$NumberSkeletonImpl$StemEnum[stemEnum.ordinal()];
            if (i == 38) {
                return NumberFormatter.DecimalSeparatorDisplay.AUTO;
            }
            if (i != 39) {
                return null;
            }
            return NumberFormatter.DecimalSeparatorDisplay.ALWAYS;
        }
    }

    /* access modifiers changed from: package-private */
    public static final class EnumToStemString {
        EnumToStemString() {
        }

        /* access modifiers changed from: private */
        public static void roundingMode(RoundingMode roundingMode, StringBuilder sb) {
            switch (AnonymousClass2.$SwitchMap$java$math$RoundingMode[roundingMode.ordinal()]) {
                case 1:
                    sb.append("rounding-mode-ceiling");
                    return;
                case 2:
                    sb.append("rounding-mode-floor");
                    return;
                case 3:
                    sb.append("rounding-mode-down");
                    return;
                case 4:
                    sb.append("rounding-mode-up");
                    return;
                case 5:
                    sb.append("rounding-mode-half-even");
                    return;
                case 6:
                    sb.append("rounding-mode-half-down");
                    return;
                case 7:
                    sb.append("rounding-mode-half-up");
                    return;
                case 8:
                    sb.append("rounding-mode-unnecessary");
                    return;
                default:
                    throw new AssertionError();
            }
        }

        /* access modifiers changed from: private */
        public static void groupingStrategy(NumberFormatter.GroupingStrategy groupingStrategy, StringBuilder sb) {
            int i = AnonymousClass2.$SwitchMap$ohos$global$icu$number$NumberFormatter$GroupingStrategy[groupingStrategy.ordinal()];
            if (i == 1) {
                sb.append("group-off");
            } else if (i == 2) {
                sb.append("group-min2");
            } else if (i == 3) {
                sb.append("group-auto");
            } else if (i == 4) {
                sb.append("group-on-aligned");
            } else if (i == 5) {
                sb.append("group-thousands");
            } else {
                throw new AssertionError();
            }
        }

        /* access modifiers changed from: private */
        public static void unitWidth(NumberFormatter.UnitWidth unitWidth, StringBuilder sb) {
            int i = AnonymousClass2.$SwitchMap$ohos$global$icu$number$NumberFormatter$UnitWidth[unitWidth.ordinal()];
            if (i == 1) {
                sb.append("unit-width-narrow");
            } else if (i == 2) {
                sb.append("unit-width-short");
            } else if (i == 3) {
                sb.append("unit-width-full-name");
            } else if (i == 4) {
                sb.append("unit-width-iso-code");
            } else if (i == 5) {
                sb.append("unit-width-hidden");
            } else {
                throw new AssertionError();
            }
        }

        /* access modifiers changed from: private */
        public static void signDisplay(NumberFormatter.SignDisplay signDisplay, StringBuilder sb) {
            switch (signDisplay) {
                case AUTO:
                    sb.append("sign-auto");
                    return;
                case ALWAYS:
                    sb.append("sign-always");
                    return;
                case NEVER:
                    sb.append("sign-never");
                    return;
                case ACCOUNTING:
                    sb.append("sign-accounting");
                    return;
                case ACCOUNTING_ALWAYS:
                    sb.append("sign-accounting-always");
                    return;
                case EXCEPT_ZERO:
                    sb.append("sign-except-zero");
                    return;
                case ACCOUNTING_EXCEPT_ZERO:
                    sb.append("sign-accounting-except-zero");
                    return;
                default:
                    throw new AssertionError();
            }
        }

        /* access modifiers changed from: private */
        public static void decimalSeparatorDisplay(NumberFormatter.DecimalSeparatorDisplay decimalSeparatorDisplay, StringBuilder sb) {
            int i = AnonymousClass2.$SwitchMap$ohos$global$icu$number$NumberFormatter$DecimalSeparatorDisplay[decimalSeparatorDisplay.ordinal()];
            if (i == 1) {
                sb.append("decimal-auto");
            } else if (i == 2) {
                sb.append("decimal-always");
            } else {
                throw new AssertionError();
            }
        }
    }

    public static UnlocalizedNumberFormatter getOrCreate(String str) {
        return cache.getInstance(str, null);
    }

    public static UnlocalizedNumberFormatter create(String str) {
        return (UnlocalizedNumberFormatter) NumberFormatter.with().macros(parseSkeleton(str));
    }

    public static String generate(MacroProps macroProps) {
        StringBuilder sb = new StringBuilder();
        generateSkeleton(macroProps, sb);
        return sb.toString();
    }

    private static MacroProps parseSkeleton(String str) {
        MacroProps macroProps = new MacroProps();
        StringSegment stringSegment = new StringSegment(str + " ", false);
        CharsTrie charsTrie = new CharsTrie(SERIALIZED_STEM_TRIE, 0);
        ParseState parseState = ParseState.STATE_NULL;
        int i = 0;
        while (i < stringSegment.length()) {
            int codePointAt = stringSegment.codePointAt(i);
            boolean isWhiteSpace = PatternProps.isWhiteSpace(codePointAt);
            boolean z = codePointAt == 47;
            if (isWhiteSpace || z) {
                if (i != 0) {
                    stringSegment.setLength(i);
                    if (parseState == ParseState.STATE_NULL) {
                        parseState = parseStem(stringSegment, charsTrie, macroProps);
                        charsTrie.reset();
                    } else {
                        parseState = parseOption(parseState, stringSegment, macroProps);
                    }
                    stringSegment.resetLength();
                    stringSegment.adjustOffset(i);
                    i = 0;
                } else if (parseState != ParseState.STATE_NULL) {
                    stringSegment.setLength(Character.charCount(codePointAt));
                    throw new SkeletonSyntaxException("Unexpected separator character", stringSegment);
                }
                if (!z || parseState != ParseState.STATE_NULL) {
                    if (isWhiteSpace && parseState != ParseState.STATE_NULL) {
                        switch (parseState) {
                            case STATE_INCREMENT_PRECISION:
                            case STATE_MEASURE_UNIT:
                            case STATE_PER_MEASURE_UNIT:
                            case STATE_CURRENCY_UNIT:
                            case STATE_INTEGER_WIDTH:
                            case STATE_NUMBERING_SYSTEM:
                            case STATE_SCALE:
                                stringSegment.setLength(Character.charCount(codePointAt));
                                throw new SkeletonSyntaxException("Stem requires an option", stringSegment);
                            default:
                                parseState = ParseState.STATE_NULL;
                                break;
                        }
                    }
                    stringSegment.adjustOffset(Character.charCount(codePointAt));
                } else {
                    stringSegment.setLength(Character.charCount(codePointAt));
                    throw new SkeletonSyntaxException("Unexpected option separator", stringSegment);
                }
            } else {
                i += Character.charCount(codePointAt);
                if (parseState == ParseState.STATE_NULL) {
                    charsTrie.nextForCodePoint(codePointAt);
                }
            }
        }
        return macroProps;
    }

    /* access modifiers changed from: package-private */
    /* renamed from: ohos.global.icu.number.NumberSkeletonImpl$2  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass2 {
        static final /* synthetic */ int[] $SwitchMap$java$math$RoundingMode = new int[RoundingMode.values().length];
        static final /* synthetic */ int[] $SwitchMap$ohos$global$icu$number$NumberFormatter$DecimalSeparatorDisplay = new int[NumberFormatter.DecimalSeparatorDisplay.values().length];
        static final /* synthetic */ int[] $SwitchMap$ohos$global$icu$number$NumberFormatter$GroupingStrategy = new int[NumberFormatter.GroupingStrategy.values().length];
        static final /* synthetic */ int[] $SwitchMap$ohos$global$icu$number$NumberFormatter$UnitWidth = new int[NumberFormatter.UnitWidth.values().length];

        static {
            $SwitchMap$ohos$global$icu$number$NumberSkeletonImpl$ParseState = new int[ParseState.values().length];
            try {
                $SwitchMap$ohos$global$icu$number$NumberSkeletonImpl$ParseState[ParseState.STATE_INCREMENT_PRECISION.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$ohos$global$icu$number$NumberSkeletonImpl$ParseState[ParseState.STATE_MEASURE_UNIT.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$ohos$global$icu$number$NumberSkeletonImpl$ParseState[ParseState.STATE_PER_MEASURE_UNIT.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                $SwitchMap$ohos$global$icu$number$NumberSkeletonImpl$ParseState[ParseState.STATE_CURRENCY_UNIT.ordinal()] = 4;
            } catch (NoSuchFieldError unused4) {
            }
            try {
                $SwitchMap$ohos$global$icu$number$NumberSkeletonImpl$ParseState[ParseState.STATE_INTEGER_WIDTH.ordinal()] = 5;
            } catch (NoSuchFieldError unused5) {
            }
            try {
                $SwitchMap$ohos$global$icu$number$NumberSkeletonImpl$ParseState[ParseState.STATE_NUMBERING_SYSTEM.ordinal()] = 6;
            } catch (NoSuchFieldError unused6) {
            }
            try {
                $SwitchMap$ohos$global$icu$number$NumberSkeletonImpl$ParseState[ParseState.STATE_SCALE.ordinal()] = 7;
            } catch (NoSuchFieldError unused7) {
            }
            try {
                $SwitchMap$ohos$global$icu$number$NumberSkeletonImpl$ParseState[ParseState.STATE_SCIENTIFIC.ordinal()] = 8;
            } catch (NoSuchFieldError unused8) {
            }
            try {
                $SwitchMap$ohos$global$icu$number$NumberSkeletonImpl$ParseState[ParseState.STATE_FRACTION_PRECISION.ordinal()] = 9;
            } catch (NoSuchFieldError unused9) {
            }
            try {
                $SwitchMap$ohos$global$icu$number$NumberFormatter$DecimalSeparatorDisplay[NumberFormatter.DecimalSeparatorDisplay.AUTO.ordinal()] = 1;
            } catch (NoSuchFieldError unused10) {
            }
            try {
                $SwitchMap$ohos$global$icu$number$NumberFormatter$DecimalSeparatorDisplay[NumberFormatter.DecimalSeparatorDisplay.ALWAYS.ordinal()] = 2;
            } catch (NoSuchFieldError unused11) {
            }
            $SwitchMap$ohos$global$icu$number$NumberFormatter$SignDisplay = new int[NumberFormatter.SignDisplay.values().length];
            try {
                $SwitchMap$ohos$global$icu$number$NumberFormatter$SignDisplay[NumberFormatter.SignDisplay.AUTO.ordinal()] = 1;
            } catch (NoSuchFieldError unused12) {
            }
            try {
                $SwitchMap$ohos$global$icu$number$NumberFormatter$SignDisplay[NumberFormatter.SignDisplay.ALWAYS.ordinal()] = 2;
            } catch (NoSuchFieldError unused13) {
            }
            try {
                $SwitchMap$ohos$global$icu$number$NumberFormatter$SignDisplay[NumberFormatter.SignDisplay.NEVER.ordinal()] = 3;
            } catch (NoSuchFieldError unused14) {
            }
            try {
                $SwitchMap$ohos$global$icu$number$NumberFormatter$SignDisplay[NumberFormatter.SignDisplay.ACCOUNTING.ordinal()] = 4;
            } catch (NoSuchFieldError unused15) {
            }
            try {
                $SwitchMap$ohos$global$icu$number$NumberFormatter$SignDisplay[NumberFormatter.SignDisplay.ACCOUNTING_ALWAYS.ordinal()] = 5;
            } catch (NoSuchFieldError unused16) {
            }
            try {
                $SwitchMap$ohos$global$icu$number$NumberFormatter$SignDisplay[NumberFormatter.SignDisplay.EXCEPT_ZERO.ordinal()] = 6;
            } catch (NoSuchFieldError unused17) {
            }
            try {
                $SwitchMap$ohos$global$icu$number$NumberFormatter$SignDisplay[NumberFormatter.SignDisplay.ACCOUNTING_EXCEPT_ZERO.ordinal()] = 7;
            } catch (NoSuchFieldError unused18) {
            }
            try {
                $SwitchMap$ohos$global$icu$number$NumberFormatter$UnitWidth[NumberFormatter.UnitWidth.NARROW.ordinal()] = 1;
            } catch (NoSuchFieldError unused19) {
            }
            try {
                $SwitchMap$ohos$global$icu$number$NumberFormatter$UnitWidth[NumberFormatter.UnitWidth.SHORT.ordinal()] = 2;
            } catch (NoSuchFieldError unused20) {
            }
            try {
                $SwitchMap$ohos$global$icu$number$NumberFormatter$UnitWidth[NumberFormatter.UnitWidth.FULL_NAME.ordinal()] = 3;
            } catch (NoSuchFieldError unused21) {
            }
            try {
                $SwitchMap$ohos$global$icu$number$NumberFormatter$UnitWidth[NumberFormatter.UnitWidth.ISO_CODE.ordinal()] = 4;
            } catch (NoSuchFieldError unused22) {
            }
            try {
                $SwitchMap$ohos$global$icu$number$NumberFormatter$UnitWidth[NumberFormatter.UnitWidth.HIDDEN.ordinal()] = 5;
            } catch (NoSuchFieldError unused23) {
            }
            try {
                $SwitchMap$ohos$global$icu$number$NumberFormatter$GroupingStrategy[NumberFormatter.GroupingStrategy.OFF.ordinal()] = 1;
            } catch (NoSuchFieldError unused24) {
            }
            try {
                $SwitchMap$ohos$global$icu$number$NumberFormatter$GroupingStrategy[NumberFormatter.GroupingStrategy.MIN2.ordinal()] = 2;
            } catch (NoSuchFieldError unused25) {
            }
            try {
                $SwitchMap$ohos$global$icu$number$NumberFormatter$GroupingStrategy[NumberFormatter.GroupingStrategy.AUTO.ordinal()] = 3;
            } catch (NoSuchFieldError unused26) {
            }
            try {
                $SwitchMap$ohos$global$icu$number$NumberFormatter$GroupingStrategy[NumberFormatter.GroupingStrategy.ON_ALIGNED.ordinal()] = 4;
            } catch (NoSuchFieldError unused27) {
            }
            try {
                $SwitchMap$ohos$global$icu$number$NumberFormatter$GroupingStrategy[NumberFormatter.GroupingStrategy.THOUSANDS.ordinal()] = 5;
            } catch (NoSuchFieldError unused28) {
            }
            try {
                $SwitchMap$java$math$RoundingMode[RoundingMode.CEILING.ordinal()] = 1;
            } catch (NoSuchFieldError unused29) {
            }
            try {
                $SwitchMap$java$math$RoundingMode[RoundingMode.FLOOR.ordinal()] = 2;
            } catch (NoSuchFieldError unused30) {
            }
            try {
                $SwitchMap$java$math$RoundingMode[RoundingMode.DOWN.ordinal()] = 3;
            } catch (NoSuchFieldError unused31) {
            }
            try {
                $SwitchMap$java$math$RoundingMode[RoundingMode.UP.ordinal()] = 4;
            } catch (NoSuchFieldError unused32) {
            }
            try {
                $SwitchMap$java$math$RoundingMode[RoundingMode.HALF_EVEN.ordinal()] = 5;
            } catch (NoSuchFieldError unused33) {
            }
            try {
                $SwitchMap$java$math$RoundingMode[RoundingMode.HALF_DOWN.ordinal()] = 6;
            } catch (NoSuchFieldError unused34) {
            }
            try {
                $SwitchMap$java$math$RoundingMode[RoundingMode.HALF_UP.ordinal()] = 7;
            } catch (NoSuchFieldError unused35) {
            }
            try {
                $SwitchMap$java$math$RoundingMode[RoundingMode.UNNECESSARY.ordinal()] = 8;
            } catch (NoSuchFieldError unused36) {
            }
            $SwitchMap$ohos$global$icu$number$NumberSkeletonImpl$StemEnum = new int[StemEnum.values().length];
            try {
                $SwitchMap$ohos$global$icu$number$NumberSkeletonImpl$StemEnum[StemEnum.STEM_COMPACT_SHORT.ordinal()] = 1;
            } catch (NoSuchFieldError unused37) {
            }
            try {
                $SwitchMap$ohos$global$icu$number$NumberSkeletonImpl$StemEnum[StemEnum.STEM_COMPACT_LONG.ordinal()] = 2;
            } catch (NoSuchFieldError unused38) {
            }
            try {
                $SwitchMap$ohos$global$icu$number$NumberSkeletonImpl$StemEnum[StemEnum.STEM_SCIENTIFIC.ordinal()] = 3;
            } catch (NoSuchFieldError unused39) {
            }
            try {
                $SwitchMap$ohos$global$icu$number$NumberSkeletonImpl$StemEnum[StemEnum.STEM_ENGINEERING.ordinal()] = 4;
            } catch (NoSuchFieldError unused40) {
            }
            try {
                $SwitchMap$ohos$global$icu$number$NumberSkeletonImpl$StemEnum[StemEnum.STEM_NOTATION_SIMPLE.ordinal()] = 5;
            } catch (NoSuchFieldError unused41) {
            }
            try {
                $SwitchMap$ohos$global$icu$number$NumberSkeletonImpl$StemEnum[StemEnum.STEM_BASE_UNIT.ordinal()] = 6;
            } catch (NoSuchFieldError unused42) {
            }
            try {
                $SwitchMap$ohos$global$icu$number$NumberSkeletonImpl$StemEnum[StemEnum.STEM_PERCENT.ordinal()] = 7;
            } catch (NoSuchFieldError unused43) {
            }
            try {
                $SwitchMap$ohos$global$icu$number$NumberSkeletonImpl$StemEnum[StemEnum.STEM_PERMILLE.ordinal()] = 8;
            } catch (NoSuchFieldError unused44) {
            }
            try {
                $SwitchMap$ohos$global$icu$number$NumberSkeletonImpl$StemEnum[StemEnum.STEM_PRECISION_INTEGER.ordinal()] = 9;
            } catch (NoSuchFieldError unused45) {
            }
            try {
                $SwitchMap$ohos$global$icu$number$NumberSkeletonImpl$StemEnum[StemEnum.STEM_PRECISION_UNLIMITED.ordinal()] = 10;
            } catch (NoSuchFieldError unused46) {
            }
            try {
                $SwitchMap$ohos$global$icu$number$NumberSkeletonImpl$StemEnum[StemEnum.STEM_PRECISION_CURRENCY_STANDARD.ordinal()] = 11;
            } catch (NoSuchFieldError unused47) {
            }
            try {
                $SwitchMap$ohos$global$icu$number$NumberSkeletonImpl$StemEnum[StemEnum.STEM_PRECISION_CURRENCY_CASH.ordinal()] = 12;
            } catch (NoSuchFieldError unused48) {
            }
            try {
                $SwitchMap$ohos$global$icu$number$NumberSkeletonImpl$StemEnum[StemEnum.STEM_ROUNDING_MODE_CEILING.ordinal()] = 13;
            } catch (NoSuchFieldError unused49) {
            }
            try {
                $SwitchMap$ohos$global$icu$number$NumberSkeletonImpl$StemEnum[StemEnum.STEM_ROUNDING_MODE_FLOOR.ordinal()] = 14;
            } catch (NoSuchFieldError unused50) {
            }
            try {
                $SwitchMap$ohos$global$icu$number$NumberSkeletonImpl$StemEnum[StemEnum.STEM_ROUNDING_MODE_DOWN.ordinal()] = 15;
            } catch (NoSuchFieldError unused51) {
            }
            try {
                $SwitchMap$ohos$global$icu$number$NumberSkeletonImpl$StemEnum[StemEnum.STEM_ROUNDING_MODE_UP.ordinal()] = 16;
            } catch (NoSuchFieldError unused52) {
            }
            try {
                $SwitchMap$ohos$global$icu$number$NumberSkeletonImpl$StemEnum[StemEnum.STEM_ROUNDING_MODE_HALF_EVEN.ordinal()] = 17;
            } catch (NoSuchFieldError unused53) {
            }
            try {
                $SwitchMap$ohos$global$icu$number$NumberSkeletonImpl$StemEnum[StemEnum.STEM_ROUNDING_MODE_HALF_DOWN.ordinal()] = 18;
            } catch (NoSuchFieldError unused54) {
            }
            try {
                $SwitchMap$ohos$global$icu$number$NumberSkeletonImpl$StemEnum[StemEnum.STEM_ROUNDING_MODE_HALF_UP.ordinal()] = 19;
            } catch (NoSuchFieldError unused55) {
            }
            try {
                $SwitchMap$ohos$global$icu$number$NumberSkeletonImpl$StemEnum[StemEnum.STEM_ROUNDING_MODE_UNNECESSARY.ordinal()] = 20;
            } catch (NoSuchFieldError unused56) {
            }
            try {
                $SwitchMap$ohos$global$icu$number$NumberSkeletonImpl$StemEnum[StemEnum.STEM_GROUP_OFF.ordinal()] = 21;
            } catch (NoSuchFieldError unused57) {
            }
            try {
                $SwitchMap$ohos$global$icu$number$NumberSkeletonImpl$StemEnum[StemEnum.STEM_GROUP_MIN2.ordinal()] = 22;
            } catch (NoSuchFieldError unused58) {
            }
            try {
                $SwitchMap$ohos$global$icu$number$NumberSkeletonImpl$StemEnum[StemEnum.STEM_GROUP_AUTO.ordinal()] = 23;
            } catch (NoSuchFieldError unused59) {
            }
            try {
                $SwitchMap$ohos$global$icu$number$NumberSkeletonImpl$StemEnum[StemEnum.STEM_GROUP_ON_ALIGNED.ordinal()] = 24;
            } catch (NoSuchFieldError unused60) {
            }
            try {
                $SwitchMap$ohos$global$icu$number$NumberSkeletonImpl$StemEnum[StemEnum.STEM_GROUP_THOUSANDS.ordinal()] = 25;
            } catch (NoSuchFieldError unused61) {
            }
            try {
                $SwitchMap$ohos$global$icu$number$NumberSkeletonImpl$StemEnum[StemEnum.STEM_UNIT_WIDTH_NARROW.ordinal()] = 26;
            } catch (NoSuchFieldError unused62) {
            }
            try {
                $SwitchMap$ohos$global$icu$number$NumberSkeletonImpl$StemEnum[StemEnum.STEM_UNIT_WIDTH_SHORT.ordinal()] = 27;
            } catch (NoSuchFieldError unused63) {
            }
            try {
                $SwitchMap$ohos$global$icu$number$NumberSkeletonImpl$StemEnum[StemEnum.STEM_UNIT_WIDTH_FULL_NAME.ordinal()] = 28;
            } catch (NoSuchFieldError unused64) {
            }
            try {
                $SwitchMap$ohos$global$icu$number$NumberSkeletonImpl$StemEnum[StemEnum.STEM_UNIT_WIDTH_ISO_CODE.ordinal()] = 29;
            } catch (NoSuchFieldError unused65) {
            }
            try {
                $SwitchMap$ohos$global$icu$number$NumberSkeletonImpl$StemEnum[StemEnum.STEM_UNIT_WIDTH_HIDDEN.ordinal()] = 30;
            } catch (NoSuchFieldError unused66) {
            }
            try {
                $SwitchMap$ohos$global$icu$number$NumberSkeletonImpl$StemEnum[StemEnum.STEM_SIGN_AUTO.ordinal()] = 31;
            } catch (NoSuchFieldError unused67) {
            }
            try {
                $SwitchMap$ohos$global$icu$number$NumberSkeletonImpl$StemEnum[StemEnum.STEM_SIGN_ALWAYS.ordinal()] = 32;
            } catch (NoSuchFieldError unused68) {
            }
            try {
                $SwitchMap$ohos$global$icu$number$NumberSkeletonImpl$StemEnum[StemEnum.STEM_SIGN_NEVER.ordinal()] = 33;
            } catch (NoSuchFieldError unused69) {
            }
            try {
                $SwitchMap$ohos$global$icu$number$NumberSkeletonImpl$StemEnum[StemEnum.STEM_SIGN_ACCOUNTING.ordinal()] = 34;
            } catch (NoSuchFieldError unused70) {
            }
            try {
                $SwitchMap$ohos$global$icu$number$NumberSkeletonImpl$StemEnum[StemEnum.STEM_SIGN_ACCOUNTING_ALWAYS.ordinal()] = 35;
            } catch (NoSuchFieldError unused71) {
            }
            try {
                $SwitchMap$ohos$global$icu$number$NumberSkeletonImpl$StemEnum[StemEnum.STEM_SIGN_EXCEPT_ZERO.ordinal()] = 36;
            } catch (NoSuchFieldError unused72) {
            }
            try {
                $SwitchMap$ohos$global$icu$number$NumberSkeletonImpl$StemEnum[StemEnum.STEM_SIGN_ACCOUNTING_EXCEPT_ZERO.ordinal()] = 37;
            } catch (NoSuchFieldError unused73) {
            }
            try {
                $SwitchMap$ohos$global$icu$number$NumberSkeletonImpl$StemEnum[StemEnum.STEM_DECIMAL_AUTO.ordinal()] = 38;
            } catch (NoSuchFieldError unused74) {
            }
            try {
                $SwitchMap$ohos$global$icu$number$NumberSkeletonImpl$StemEnum[StemEnum.STEM_DECIMAL_ALWAYS.ordinal()] = 39;
            } catch (NoSuchFieldError unused75) {
            }
            try {
                $SwitchMap$ohos$global$icu$number$NumberSkeletonImpl$StemEnum[StemEnum.STEM_LATIN.ordinal()] = 40;
            } catch (NoSuchFieldError unused76) {
            }
            try {
                $SwitchMap$ohos$global$icu$number$NumberSkeletonImpl$StemEnum[StemEnum.STEM_PRECISION_INCREMENT.ordinal()] = 41;
            } catch (NoSuchFieldError unused77) {
            }
            try {
                $SwitchMap$ohos$global$icu$number$NumberSkeletonImpl$StemEnum[StemEnum.STEM_MEASURE_UNIT.ordinal()] = 42;
            } catch (NoSuchFieldError unused78) {
            }
            try {
                $SwitchMap$ohos$global$icu$number$NumberSkeletonImpl$StemEnum[StemEnum.STEM_PER_MEASURE_UNIT.ordinal()] = 43;
            } catch (NoSuchFieldError unused79) {
            }
            try {
                $SwitchMap$ohos$global$icu$number$NumberSkeletonImpl$StemEnum[StemEnum.STEM_CURRENCY.ordinal()] = 44;
            } catch (NoSuchFieldError unused80) {
            }
            try {
                $SwitchMap$ohos$global$icu$number$NumberSkeletonImpl$StemEnum[StemEnum.STEM_INTEGER_WIDTH.ordinal()] = 45;
            } catch (NoSuchFieldError unused81) {
            }
            try {
                $SwitchMap$ohos$global$icu$number$NumberSkeletonImpl$StemEnum[StemEnum.STEM_NUMBERING_SYSTEM.ordinal()] = 46;
            } catch (NoSuchFieldError unused82) {
            }
            try {
                $SwitchMap$ohos$global$icu$number$NumberSkeletonImpl$StemEnum[StemEnum.STEM_SCALE.ordinal()] = 47;
            } catch (NoSuchFieldError unused83) {
            }
        }
    }

    private static ParseState parseStem(StringSegment stringSegment, CharsTrie charsTrie, MacroProps macroProps) {
        char charAt = stringSegment.charAt(0);
        if (charAt == '.') {
            checkNull(macroProps.precision, stringSegment);
            BlueprintHelpers.parseFractionStem(stringSegment, macroProps);
            return ParseState.STATE_FRACTION_PRECISION;
        } else if (charAt != '@') {
            BytesTrie.Result current = charsTrie.current();
            if (current == BytesTrie.Result.INTERMEDIATE_VALUE || current == BytesTrie.Result.FINAL_VALUE) {
                StemEnum stemEnum = STEM_ENUM_VALUES[charsTrie.getValue()];
                switch (stemEnum) {
                    case STEM_COMPACT_SHORT:
                    case STEM_COMPACT_LONG:
                    case STEM_SCIENTIFIC:
                    case STEM_ENGINEERING:
                    case STEM_NOTATION_SIMPLE:
                        checkNull(macroProps.notation, stringSegment);
                        macroProps.notation = StemToObject.notation(stemEnum);
                        int i = AnonymousClass2.$SwitchMap$ohos$global$icu$number$NumberSkeletonImpl$StemEnum[stemEnum.ordinal()];
                        if (i == 3 || i == 4) {
                            return ParseState.STATE_SCIENTIFIC;
                        }
                        return ParseState.STATE_NULL;
                    case STEM_BASE_UNIT:
                    case STEM_PERCENT:
                    case STEM_PERMILLE:
                        checkNull(macroProps.unit, stringSegment);
                        macroProps.unit = StemToObject.unit(stemEnum);
                        return ParseState.STATE_NULL;
                    case STEM_PRECISION_INTEGER:
                    case STEM_PRECISION_UNLIMITED:
                    case STEM_PRECISION_CURRENCY_STANDARD:
                    case STEM_PRECISION_CURRENCY_CASH:
                        checkNull(macroProps.precision, stringSegment);
                        macroProps.precision = StemToObject.precision(stemEnum);
                        if (AnonymousClass2.$SwitchMap$ohos$global$icu$number$NumberSkeletonImpl$StemEnum[stemEnum.ordinal()] != 9) {
                            return ParseState.STATE_NULL;
                        }
                        return ParseState.STATE_FRACTION_PRECISION;
                    case STEM_ROUNDING_MODE_CEILING:
                    case STEM_ROUNDING_MODE_FLOOR:
                    case STEM_ROUNDING_MODE_DOWN:
                    case STEM_ROUNDING_MODE_UP:
                    case STEM_ROUNDING_MODE_HALF_EVEN:
                    case STEM_ROUNDING_MODE_HALF_DOWN:
                    case STEM_ROUNDING_MODE_HALF_UP:
                    case STEM_ROUNDING_MODE_UNNECESSARY:
                        checkNull(macroProps.roundingMode, stringSegment);
                        macroProps.roundingMode = StemToObject.roundingMode(stemEnum);
                        return ParseState.STATE_NULL;
                    case STEM_GROUP_OFF:
                    case STEM_GROUP_MIN2:
                    case STEM_GROUP_AUTO:
                    case STEM_GROUP_ON_ALIGNED:
                    case STEM_GROUP_THOUSANDS:
                        checkNull(macroProps.grouping, stringSegment);
                        macroProps.grouping = StemToObject.groupingStrategy(stemEnum);
                        return ParseState.STATE_NULL;
                    case STEM_UNIT_WIDTH_NARROW:
                    case STEM_UNIT_WIDTH_SHORT:
                    case STEM_UNIT_WIDTH_FULL_NAME:
                    case STEM_UNIT_WIDTH_ISO_CODE:
                    case STEM_UNIT_WIDTH_HIDDEN:
                        checkNull(macroProps.unitWidth, stringSegment);
                        macroProps.unitWidth = StemToObject.unitWidth(stemEnum);
                        return ParseState.STATE_NULL;
                    case STEM_SIGN_AUTO:
                    case STEM_SIGN_ALWAYS:
                    case STEM_SIGN_NEVER:
                    case STEM_SIGN_ACCOUNTING:
                    case STEM_SIGN_ACCOUNTING_ALWAYS:
                    case STEM_SIGN_EXCEPT_ZERO:
                    case STEM_SIGN_ACCOUNTING_EXCEPT_ZERO:
                        checkNull(macroProps.sign, stringSegment);
                        macroProps.sign = StemToObject.signDisplay(stemEnum);
                        return ParseState.STATE_NULL;
                    case STEM_DECIMAL_AUTO:
                    case STEM_DECIMAL_ALWAYS:
                        checkNull(macroProps.decimal, stringSegment);
                        macroProps.decimal = StemToObject.decimalSeparatorDisplay(stemEnum);
                        return ParseState.STATE_NULL;
                    case STEM_LATIN:
                        checkNull(macroProps.symbols, stringSegment);
                        macroProps.symbols = NumberingSystem.LATIN;
                        return ParseState.STATE_NULL;
                    case STEM_PRECISION_INCREMENT:
                        checkNull(macroProps.precision, stringSegment);
                        return ParseState.STATE_INCREMENT_PRECISION;
                    case STEM_MEASURE_UNIT:
                        checkNull(macroProps.unit, stringSegment);
                        return ParseState.STATE_MEASURE_UNIT;
                    case STEM_PER_MEASURE_UNIT:
                        checkNull(macroProps.perUnit, stringSegment);
                        return ParseState.STATE_PER_MEASURE_UNIT;
                    case STEM_CURRENCY:
                        checkNull(macroProps.unit, stringSegment);
                        return ParseState.STATE_CURRENCY_UNIT;
                    case STEM_INTEGER_WIDTH:
                        checkNull(macroProps.integerWidth, stringSegment);
                        return ParseState.STATE_INTEGER_WIDTH;
                    case STEM_NUMBERING_SYSTEM:
                        checkNull(macroProps.symbols, stringSegment);
                        return ParseState.STATE_NUMBERING_SYSTEM;
                    case STEM_SCALE:
                        checkNull(macroProps.scale, stringSegment);
                        return ParseState.STATE_SCALE;
                    default:
                        throw new AssertionError();
                }
            } else {
                throw new SkeletonSyntaxException("Unknown stem", stringSegment);
            }
        } else {
            checkNull(macroProps.precision, stringSegment);
            BlueprintHelpers.parseDigitsStem(stringSegment, macroProps);
            return ParseState.STATE_NULL;
        }
    }

    private static ParseState parseOption(ParseState parseState, StringSegment stringSegment, MacroProps macroProps) {
        switch (parseState) {
            case STATE_INCREMENT_PRECISION:
                BlueprintHelpers.parseIncrementOption(stringSegment, macroProps);
                return ParseState.STATE_NULL;
            case STATE_MEASURE_UNIT:
                BlueprintHelpers.parseMeasureUnitOption(stringSegment, macroProps);
                return ParseState.STATE_NULL;
            case STATE_PER_MEASURE_UNIT:
                BlueprintHelpers.parseMeasurePerUnitOption(stringSegment, macroProps);
                return ParseState.STATE_NULL;
            case STATE_CURRENCY_UNIT:
                BlueprintHelpers.parseCurrencyOption(stringSegment, macroProps);
                return ParseState.STATE_NULL;
            case STATE_INTEGER_WIDTH:
                BlueprintHelpers.parseIntegerWidthOption(stringSegment, macroProps);
                return ParseState.STATE_NULL;
            case STATE_NUMBERING_SYSTEM:
                BlueprintHelpers.parseNumberingSystemOption(stringSegment, macroProps);
                return ParseState.STATE_NULL;
            case STATE_SCALE:
                BlueprintHelpers.parseScaleOption(stringSegment, macroProps);
                return ParseState.STATE_NULL;
            default:
                if (AnonymousClass2.$SwitchMap$ohos$global$icu$number$NumberSkeletonImpl$ParseState[parseState.ordinal()] == 8) {
                    if (BlueprintHelpers.parseExponentWidthOption(stringSegment, macroProps)) {
                        return ParseState.STATE_SCIENTIFIC;
                    }
                    if (BlueprintHelpers.parseExponentSignOption(stringSegment, macroProps)) {
                        return ParseState.STATE_SCIENTIFIC;
                    }
                }
                if (AnonymousClass2.$SwitchMap$ohos$global$icu$number$NumberSkeletonImpl$ParseState[parseState.ordinal()] == 9 && BlueprintHelpers.parseFracSigOption(stringSegment, macroProps)) {
                    return ParseState.STATE_NULL;
                }
                throw new SkeletonSyntaxException("Invalid option", stringSegment);
        }
    }

    private static void generateSkeleton(MacroProps macroProps, StringBuilder sb) {
        if (macroProps.notation != null && GeneratorHelpers.notation(macroProps, sb)) {
            sb.append(' ');
        }
        if (macroProps.unit != null && GeneratorHelpers.unit(macroProps, sb)) {
            sb.append(' ');
        }
        if (macroProps.perUnit != null && GeneratorHelpers.perUnit(macroProps, sb)) {
            sb.append(' ');
        }
        if (macroProps.precision != null && GeneratorHelpers.precision(macroProps, sb)) {
            sb.append(' ');
        }
        if (macroProps.roundingMode != null && GeneratorHelpers.roundingMode(macroProps, sb)) {
            sb.append(' ');
        }
        if (macroProps.grouping != null && GeneratorHelpers.grouping(macroProps, sb)) {
            sb.append(' ');
        }
        if (macroProps.integerWidth != null && GeneratorHelpers.integerWidth(macroProps, sb)) {
            sb.append(' ');
        }
        if (macroProps.symbols != null && GeneratorHelpers.symbols(macroProps, sb)) {
            sb.append(' ');
        }
        if (macroProps.unitWidth != null && GeneratorHelpers.unitWidth(macroProps, sb)) {
            sb.append(' ');
        }
        if (macroProps.sign != null && GeneratorHelpers.sign(macroProps, sb)) {
            sb.append(' ');
        }
        if (macroProps.decimal != null && GeneratorHelpers.decimal(macroProps, sb)) {
            sb.append(' ');
        }
        if (macroProps.scale != null && GeneratorHelpers.scale(macroProps, sb)) {
            sb.append(' ');
        }
        if (macroProps.padder != null) {
            throw new UnsupportedOperationException("Cannot generate number skeleton with custom padder");
        } else if (macroProps.affixProvider != null) {
            throw new UnsupportedOperationException("Cannot generate number skeleton with custom affix provider");
        } else if (macroProps.rules != null) {
            throw new UnsupportedOperationException("Cannot generate number skeleton with custom plural rules");
        } else if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }
    }

    /* access modifiers changed from: package-private */
    public static final class BlueprintHelpers {
        static final /* synthetic */ boolean $assertionsDisabled = false;

        BlueprintHelpers() {
        }

        /* access modifiers changed from: private */
        public static boolean parseExponentWidthOption(StringSegment stringSegment, MacroProps macroProps) {
            if (stringSegment.charAt(0) != '+') {
                return false;
            }
            int i = 0;
            int i2 = 1;
            while (i2 < stringSegment.length() && stringSegment.charAt(i2) == 'e') {
                i++;
                i2++;
            }
            if (i2 < stringSegment.length()) {
                return false;
            }
            macroProps.notation = ((ScientificNotation) macroProps.notation).withMinExponentDigits(i);
            return true;
        }

        /* access modifiers changed from: private */
        public static void generateExponentWidthOption(int i, StringBuilder sb) {
            sb.append('+');
            NumberSkeletonImpl.appendMultiple(sb, 101, i);
        }

        /* access modifiers changed from: private */
        public static boolean parseExponentSignOption(StringSegment stringSegment, MacroProps macroProps) {
            NumberFormatter.SignDisplay signDisplay;
            CharsTrie charsTrie = new CharsTrie(NumberSkeletonImpl.SERIALIZED_STEM_TRIE, 0);
            BytesTrie.Result next = charsTrie.next(stringSegment, 0, stringSegment.length());
            if ((next != BytesTrie.Result.INTERMEDIATE_VALUE && next != BytesTrie.Result.FINAL_VALUE) || (signDisplay = StemToObject.signDisplay(NumberSkeletonImpl.STEM_ENUM_VALUES[charsTrie.getValue()])) == null) {
                return false;
            }
            macroProps.notation = ((ScientificNotation) macroProps.notation).withExponentSignDisplay(signDisplay);
            return true;
        }

        /* access modifiers changed from: private */
        public static void parseCurrencyOption(StringSegment stringSegment, MacroProps macroProps) {
            try {
                macroProps.unit = Currency.getInstance(stringSegment.subSequence(0, stringSegment.length()).toString());
            } catch (IllegalArgumentException e) {
                throw new SkeletonSyntaxException("Invalid currency", stringSegment, e);
            }
        }

        /* access modifiers changed from: private */
        public static void generateCurrencyOption(Currency currency, StringBuilder sb) {
            sb.append(currency.getCurrencyCode());
        }

        /* access modifiers changed from: private */
        public static void parseMeasureUnitOption(StringSegment stringSegment, MacroProps macroProps) {
            int i = 0;
            while (i < stringSegment.length() && stringSegment.charAt(i) != '-') {
                i++;
            }
            if (i != stringSegment.length()) {
                String charSequence = stringSegment.subSequence(0, i).toString();
                String charSequence2 = stringSegment.subSequence(i + 1, stringSegment.length()).toString();
                for (MeasureUnit measureUnit : MeasureUnit.getAvailable(charSequence)) {
                    if (charSequence2.equals(measureUnit.getSubtype())) {
                        macroProps.unit = measureUnit;
                        return;
                    }
                }
                throw new SkeletonSyntaxException("Unknown measure unit", stringSegment);
            }
            throw new SkeletonSyntaxException("Invalid measure unit option", stringSegment);
        }

        /* access modifiers changed from: private */
        public static void generateMeasureUnitOption(MeasureUnit measureUnit, StringBuilder sb) {
            sb.append(measureUnit.getType());
            sb.append(LanguageTag.SEP);
            sb.append(measureUnit.getSubtype());
        }

        /* access modifiers changed from: private */
        public static void parseMeasurePerUnitOption(StringSegment stringSegment, MacroProps macroProps) {
            MeasureUnit measureUnit = macroProps.unit;
            parseMeasureUnitOption(stringSegment, macroProps);
            macroProps.perUnit = macroProps.unit;
            macroProps.unit = measureUnit;
        }

        /* access modifiers changed from: private */
        public static void parseFractionStem(StringSegment stringSegment, MacroProps macroProps) {
            int i;
            int i2 = 0;
            int i3 = 1;
            while (i3 < stringSegment.length() && stringSegment.charAt(i3) == '0') {
                i2++;
                i3++;
            }
            if (i3 >= stringSegment.length()) {
                i = i2;
            } else if (stringSegment.charAt(i3) == '+') {
                i3++;
                i = -1;
            } else {
                i = i2;
                while (i3 < stringSegment.length() && stringSegment.charAt(i3) == '#') {
                    i++;
                    i3++;
                }
            }
            if (i3 < stringSegment.length()) {
                throw new SkeletonSyntaxException("Invalid fraction stem", stringSegment);
            } else if (i == -1) {
                macroProps.precision = Precision.minFraction(i2);
            } else {
                macroProps.precision = Precision.minMaxFraction(i2, i);
            }
        }

        /* access modifiers changed from: private */
        public static void generateFractionStem(int i, int i2, StringBuilder sb) {
            if (i == 0 && i2 == 0) {
                sb.append("precision-integer");
                return;
            }
            sb.append('.');
            NumberSkeletonImpl.appendMultiple(sb, 48, i);
            if (i2 == -1) {
                sb.append('+');
            } else {
                NumberSkeletonImpl.appendMultiple(sb, 35, i2 - i);
            }
        }

        /* access modifiers changed from: private */
        public static void parseDigitsStem(StringSegment stringSegment, MacroProps macroProps) {
            int i;
            int i2 = 0;
            int i3 = 0;
            while (i2 < stringSegment.length() && stringSegment.charAt(i2) == '@') {
                i3++;
                i2++;
            }
            if (i2 >= stringSegment.length()) {
                i = i3;
            } else if (stringSegment.charAt(i2) == '+') {
                i2++;
                i = -1;
            } else {
                i = i3;
                while (i2 < stringSegment.length() && stringSegment.charAt(i2) == '#') {
                    i++;
                    i2++;
                }
            }
            if (i2 < stringSegment.length()) {
                throw new SkeletonSyntaxException("Invalid significant digits stem", stringSegment);
            } else if (i == -1) {
                macroProps.precision = Precision.minSignificantDigits(i3);
            } else {
                macroProps.precision = Precision.minMaxSignificantDigits(i3, i);
            }
        }

        /* access modifiers changed from: private */
        public static void generateDigitsStem(int i, int i2, StringBuilder sb) {
            NumberSkeletonImpl.appendMultiple(sb, 64, i);
            if (i2 == -1) {
                sb.append('+');
            } else {
                NumberSkeletonImpl.appendMultiple(sb, 35, i2 - i);
            }
        }

        /* access modifiers changed from: private */
        public static boolean parseFracSigOption(StringSegment stringSegment, MacroProps macroProps) {
            int i;
            int i2 = 0;
            if (stringSegment.charAt(0) != '@') {
                return false;
            }
            int i3 = 0;
            while (i2 < stringSegment.length() && stringSegment.charAt(i2) == '@') {
                i3++;
                i2++;
            }
            if (i2 < stringSegment.length()) {
                if (stringSegment.charAt(i2) == '+') {
                    i2++;
                    i = -1;
                } else if (i3 <= 1) {
                    i = i3;
                    while (i2 < stringSegment.length() && stringSegment.charAt(i2) == '#') {
                        i++;
                        i2++;
                    }
                } else {
                    throw new SkeletonSyntaxException("Invalid digits option for fraction rounder", stringSegment);
                }
                if (i2 >= stringSegment.length()) {
                    FractionPrecision fractionPrecision = (FractionPrecision) macroProps.precision;
                    if (i == -1) {
                        macroProps.precision = fractionPrecision.withMinDigits(i3);
                    } else {
                        macroProps.precision = fractionPrecision.withMaxDigits(i);
                    }
                    return true;
                }
                throw new SkeletonSyntaxException("Invalid digits option for fraction rounder", stringSegment);
            }
            throw new SkeletonSyntaxException("Invalid digits option for fraction rounder", stringSegment);
        }

        /* access modifiers changed from: private */
        public static void parseIncrementOption(StringSegment stringSegment, MacroProps macroProps) {
            try {
                macroProps.precision = Precision.increment(new BigDecimal(stringSegment.subSequence(0, stringSegment.length()).toString()));
            } catch (NumberFormatException e) {
                throw new SkeletonSyntaxException("Invalid rounding increment", stringSegment, e);
            }
        }

        /* access modifiers changed from: private */
        public static void generateIncrementOption(BigDecimal bigDecimal, StringBuilder sb) {
            sb.append(bigDecimal.toPlainString());
        }

        /* access modifiers changed from: private */
        public static void parseIntegerWidthOption(StringSegment stringSegment, MacroProps macroProps) {
            int i;
            int i2;
            int i3 = 0;
            if (stringSegment.charAt(0) == '+') {
                i2 = 1;
                i = -1;
            } else {
                i2 = 0;
                i = 0;
            }
            while (i2 < stringSegment.length() && i != -1 && stringSegment.charAt(i2) == '#') {
                i++;
                i2++;
            }
            if (i2 < stringSegment.length()) {
                while (i2 < stringSegment.length() && stringSegment.charAt(i2) == '0') {
                    i3++;
                    i2++;
                }
            }
            if (i != -1) {
                i += i3;
            }
            if (i2 < stringSegment.length()) {
                throw new SkeletonSyntaxException("Invalid integer width stem", stringSegment);
            } else if (i == -1) {
                macroProps.integerWidth = IntegerWidth.zeroFillTo(i3);
            } else {
                macroProps.integerWidth = IntegerWidth.zeroFillTo(i3).truncateAt(i);
            }
        }

        /* access modifiers changed from: private */
        public static void generateIntegerWidthOption(int i, int i2, StringBuilder sb) {
            if (i2 == -1) {
                sb.append('+');
            } else {
                NumberSkeletonImpl.appendMultiple(sb, 35, i2 - i);
            }
            NumberSkeletonImpl.appendMultiple(sb, 48, i);
        }

        /* access modifiers changed from: private */
        public static void parseNumberingSystemOption(StringSegment stringSegment, MacroProps macroProps) {
            NumberingSystem instanceByName = NumberingSystem.getInstanceByName(stringSegment.subSequence(0, stringSegment.length()).toString());
            if (instanceByName != null) {
                macroProps.symbols = instanceByName;
                return;
            }
            throw new SkeletonSyntaxException("Unknown numbering system", stringSegment);
        }

        /* access modifiers changed from: private */
        public static void generateNumberingSystemOption(NumberingSystem numberingSystem, StringBuilder sb) {
            sb.append(numberingSystem.getName());
        }

        /* access modifiers changed from: private */
        public static void parseScaleOption(StringSegment stringSegment, MacroProps macroProps) {
            try {
                macroProps.scale = Scale.byBigDecimal(new BigDecimal(stringSegment.subSequence(0, stringSegment.length()).toString()));
            } catch (NumberFormatException e) {
                throw new SkeletonSyntaxException("Invalid scale", stringSegment, e);
            }
        }

        /* access modifiers changed from: private */
        public static void generateScaleOption(Scale scale, StringBuilder sb) {
            BigDecimal bigDecimal = scale.arbitrary;
            if (bigDecimal == null) {
                bigDecimal = BigDecimal.ONE;
            }
            sb.append(bigDecimal.scaleByPowerOfTen(scale.magnitude).toPlainString());
        }
    }

    /* access modifiers changed from: package-private */
    public static final class GeneratorHelpers {
        static final /* synthetic */ boolean $assertionsDisabled = false;

        GeneratorHelpers() {
        }

        /* access modifiers changed from: private */
        public static boolean notation(MacroProps macroProps, StringBuilder sb) {
            if (macroProps.notation instanceof CompactNotation) {
                if (macroProps.notation == Notation.compactLong()) {
                    sb.append("compact-long");
                    return true;
                } else if (macroProps.notation == Notation.compactShort()) {
                    sb.append("compact-short");
                    return true;
                } else {
                    throw new UnsupportedOperationException("Cannot generate number skeleton with custom compact data");
                }
            } else if (!(macroProps.notation instanceof ScientificNotation)) {
                return false;
            } else {
                ScientificNotation scientificNotation = (ScientificNotation) macroProps.notation;
                if (scientificNotation.engineeringInterval == 3) {
                    sb.append("engineering");
                } else {
                    sb.append("scientific");
                }
                if (scientificNotation.minExponentDigits > 1) {
                    sb.append('/');
                    BlueprintHelpers.generateExponentWidthOption(scientificNotation.minExponentDigits, sb);
                }
                if (scientificNotation.exponentSignDisplay != NumberFormatter.SignDisplay.AUTO) {
                    sb.append('/');
                    EnumToStemString.signDisplay(scientificNotation.exponentSignDisplay, sb);
                }
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static boolean unit(MacroProps macroProps, StringBuilder sb) {
            if (macroProps.unit instanceof Currency) {
                sb.append("currency/");
                BlueprintHelpers.generateCurrencyOption(macroProps.unit, sb);
                return true;
            } else if (!(macroProps.unit instanceof NoUnit)) {
                sb.append("measure-unit/");
                BlueprintHelpers.generateMeasureUnitOption(macroProps.unit, sb);
                return true;
            } else if (macroProps.unit == NoUnit.PERCENT) {
                sb.append(Constants.ATTRNAME_PERCENT);
                return true;
            } else if (macroProps.unit != NoUnit.PERMILLE) {
                return false;
            } else {
                sb.append("permille");
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static boolean perUnit(MacroProps macroProps, StringBuilder sb) {
            if ((macroProps.perUnit instanceof Currency) || (macroProps.perUnit instanceof NoUnit)) {
                throw new UnsupportedOperationException("Cannot generate number skeleton with per-unit that is not a standard measure unit");
            }
            sb.append("per-measure-unit/");
            BlueprintHelpers.generateMeasureUnitOption(macroProps.perUnit, sb);
            return true;
        }

        /* access modifiers changed from: private */
        public static boolean precision(MacroProps macroProps, StringBuilder sb) {
            if (macroProps.precision instanceof Precision.InfiniteRounderImpl) {
                sb.append("precision-unlimited");
            } else if (macroProps.precision instanceof Precision.FractionRounderImpl) {
                Precision.FractionRounderImpl fractionRounderImpl = (Precision.FractionRounderImpl) macroProps.precision;
                BlueprintHelpers.generateFractionStem(fractionRounderImpl.minFrac, fractionRounderImpl.maxFrac, sb);
            } else if (macroProps.precision instanceof Precision.SignificantRounderImpl) {
                Precision.SignificantRounderImpl significantRounderImpl = (Precision.SignificantRounderImpl) macroProps.precision;
                BlueprintHelpers.generateDigitsStem(significantRounderImpl.minSig, significantRounderImpl.maxSig, sb);
            } else if (macroProps.precision instanceof Precision.FracSigRounderImpl) {
                Precision.FracSigRounderImpl fracSigRounderImpl = (Precision.FracSigRounderImpl) macroProps.precision;
                BlueprintHelpers.generateFractionStem(fracSigRounderImpl.minFrac, fracSigRounderImpl.maxFrac, sb);
                sb.append('/');
                if (fracSigRounderImpl.minSig == -1) {
                    BlueprintHelpers.generateDigitsStem(1, fracSigRounderImpl.maxSig, sb);
                } else {
                    BlueprintHelpers.generateDigitsStem(fracSigRounderImpl.minSig, -1, sb);
                }
            } else if (macroProps.precision instanceof Precision.IncrementRounderImpl) {
                sb.append("precision-increment/");
                BlueprintHelpers.generateIncrementOption(((Precision.IncrementRounderImpl) macroProps.precision).increment, sb);
            } else if (((Precision.CurrencyRounderImpl) macroProps.precision).usage == Currency.CurrencyUsage.STANDARD) {
                sb.append("precision-currency-standard");
            } else {
                sb.append("precision-currency-cash");
            }
            return true;
        }

        /* access modifiers changed from: private */
        public static boolean roundingMode(MacroProps macroProps, StringBuilder sb) {
            if (macroProps.roundingMode == RoundingUtils.DEFAULT_ROUNDING_MODE) {
                return false;
            }
            EnumToStemString.roundingMode(macroProps.roundingMode, sb);
            return true;
        }

        /* access modifiers changed from: private */
        public static boolean grouping(MacroProps macroProps, StringBuilder sb) {
            if (!(macroProps.grouping instanceof NumberFormatter.GroupingStrategy)) {
                throw new UnsupportedOperationException("Cannot generate number skeleton with custom Grouper");
            } else if (macroProps.grouping == NumberFormatter.GroupingStrategy.AUTO) {
                return false;
            } else {
                EnumToStemString.groupingStrategy((NumberFormatter.GroupingStrategy) macroProps.grouping, sb);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static boolean integerWidth(MacroProps macroProps, StringBuilder sb) {
            if (macroProps.integerWidth.equals(IntegerWidth.DEFAULT)) {
                return false;
            }
            sb.append("integer-width/");
            BlueprintHelpers.generateIntegerWidthOption(macroProps.integerWidth.minInt, macroProps.integerWidth.maxInt, sb);
            return true;
        }

        /* access modifiers changed from: private */
        public static boolean symbols(MacroProps macroProps, StringBuilder sb) {
            if (macroProps.symbols instanceof NumberingSystem) {
                NumberingSystem numberingSystem = (NumberingSystem) macroProps.symbols;
                if (numberingSystem.getName().equals("latn")) {
                    sb.append("latin");
                    return true;
                }
                sb.append("numbering-system/");
                BlueprintHelpers.generateNumberingSystemOption(numberingSystem, sb);
                return true;
            }
            throw new UnsupportedOperationException("Cannot generate number skeleton with custom DecimalFormatSymbols");
        }

        /* access modifiers changed from: private */
        public static boolean unitWidth(MacroProps macroProps, StringBuilder sb) {
            if (macroProps.unitWidth == NumberFormatter.UnitWidth.SHORT) {
                return false;
            }
            EnumToStemString.unitWidth(macroProps.unitWidth, sb);
            return true;
        }

        /* access modifiers changed from: private */
        public static boolean sign(MacroProps macroProps, StringBuilder sb) {
            if (macroProps.sign == NumberFormatter.SignDisplay.AUTO) {
                return false;
            }
            EnumToStemString.signDisplay(macroProps.sign, sb);
            return true;
        }

        /* access modifiers changed from: private */
        public static boolean decimal(MacroProps macroProps, StringBuilder sb) {
            if (macroProps.decimal == NumberFormatter.DecimalSeparatorDisplay.AUTO) {
                return false;
            }
            EnumToStemString.decimalSeparatorDisplay(macroProps.decimal, sb);
            return true;
        }

        /* access modifiers changed from: private */
        public static boolean scale(MacroProps macroProps, StringBuilder sb) {
            if (!macroProps.scale.isValid()) {
                return false;
            }
            sb.append("scale/");
            BlueprintHelpers.generateScaleOption(macroProps.scale, sb);
            return true;
        }
    }

    private static void checkNull(Object obj, CharSequence charSequence) {
        if (obj != null) {
            throw new SkeletonSyntaxException("Duplicated setting", charSequence);
        }
    }

    /* access modifiers changed from: private */
    public static void appendMultiple(StringBuilder sb, int i, int i2) {
        for (int i3 = 0; i3 < i2; i3++) {
            sb.appendCodePoint(i);
        }
    }
}
