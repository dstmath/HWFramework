package ohos.global.icu.number;

import java.text.Format;
import ohos.com.sun.org.apache.xalan.internal.templates.Constants;
import ohos.global.icu.impl.CurrencyData;
import ohos.global.icu.impl.FormattedStringBuilder;
import ohos.global.icu.impl.StandardPlural;
import ohos.global.icu.impl.number.CompactData;
import ohos.global.icu.impl.number.ConstantAffixModifier;
import ohos.global.icu.impl.number.DecimalQuantity;
import ohos.global.icu.impl.number.DecimalQuantity_DualStorageBCD;
import ohos.global.icu.impl.number.Grouper;
import ohos.global.icu.impl.number.LongNameHandler;
import ohos.global.icu.impl.number.MacroProps;
import ohos.global.icu.impl.number.MicroProps;
import ohos.global.icu.impl.number.MicroPropsGenerator;
import ohos.global.icu.impl.number.MultiplierFormatHandler;
import ohos.global.icu.impl.number.MutablePatternModifier;
import ohos.global.icu.impl.number.Padder;
import ohos.global.icu.impl.number.PatternStringParser;
import ohos.global.icu.impl.number.RoundingUtils;
import ohos.global.icu.number.NumberFormatter;
import ohos.global.icu.text.DecimalFormatSymbols;
import ohos.global.icu.text.NumberFormat;
import ohos.global.icu.text.NumberingSystem;
import ohos.global.icu.text.PluralRules;
import ohos.global.icu.util.Currency;
import ohos.global.icu.util.MeasureUnit;

/* access modifiers changed from: package-private */
public class NumberFormatterImpl {
    private static final Currency DEFAULT_CURRENCY = Currency.getInstance("XXX");
    final MicroPropsGenerator microPropsGenerator;
    final MicroProps micros = new MicroProps(true);

    public NumberFormatterImpl(MacroProps macroProps) {
        this.microPropsGenerator = macrosToMicroGenerator(macroProps, this.micros, true);
    }

    public static int formatStatic(MacroProps macroProps, DecimalQuantity decimalQuantity, FormattedStringBuilder formattedStringBuilder) {
        MicroProps preProcessUnsafe = preProcessUnsafe(macroProps, decimalQuantity);
        int writeNumber = writeNumber(preProcessUnsafe, decimalQuantity, formattedStringBuilder, 0);
        return writeNumber + writeAffixes(preProcessUnsafe, formattedStringBuilder, 0, writeNumber);
    }

    public static int getPrefixSuffixStatic(MacroProps macroProps, byte b, StandardPlural standardPlural, FormattedStringBuilder formattedStringBuilder) {
        return getPrefixSuffixImpl(macrosToMicroGenerator(macroProps, new MicroProps(false), false), b, formattedStringBuilder);
    }

    public int format(DecimalQuantity decimalQuantity, FormattedStringBuilder formattedStringBuilder) {
        MicroProps preProcess = preProcess(decimalQuantity);
        int writeNumber = writeNumber(preProcess, decimalQuantity, formattedStringBuilder, 0);
        return writeNumber + writeAffixes(preProcess, formattedStringBuilder, 0, writeNumber);
    }

    public MicroProps preProcess(DecimalQuantity decimalQuantity) {
        MicroProps processQuantity = this.microPropsGenerator.processQuantity(decimalQuantity);
        processQuantity.rounder.apply(decimalQuantity);
        if (processQuantity.integerWidth.maxInt == -1) {
            decimalQuantity.setMinInteger(processQuantity.integerWidth.minInt);
        } else {
            decimalQuantity.setMinInteger(processQuantity.integerWidth.minInt);
            decimalQuantity.applyMaxInteger(processQuantity.integerWidth.maxInt);
        }
        return processQuantity;
    }

    private static MicroProps preProcessUnsafe(MacroProps macroProps, DecimalQuantity decimalQuantity) {
        MicroProps processQuantity = macrosToMicroGenerator(macroProps, new MicroProps(false), false).processQuantity(decimalQuantity);
        processQuantity.rounder.apply(decimalQuantity);
        if (processQuantity.integerWidth.maxInt == -1) {
            decimalQuantity.setMinInteger(processQuantity.integerWidth.minInt);
        } else {
            decimalQuantity.setMinInteger(processQuantity.integerWidth.minInt);
            decimalQuantity.applyMaxInteger(processQuantity.integerWidth.maxInt);
        }
        return processQuantity;
    }

    public int getPrefixSuffix(byte b, StandardPlural standardPlural, FormattedStringBuilder formattedStringBuilder) {
        return getPrefixSuffixImpl(this.microPropsGenerator, b, formattedStringBuilder);
    }

    private static int getPrefixSuffixImpl(MicroPropsGenerator microPropsGenerator2, byte b, FormattedStringBuilder formattedStringBuilder) {
        DecimalQuantity_DualStorageBCD decimalQuantity_DualStorageBCD = new DecimalQuantity_DualStorageBCD(0);
        if (b < 0) {
            decimalQuantity_DualStorageBCD.negate();
        }
        MicroProps processQuantity = microPropsGenerator2.processQuantity(decimalQuantity_DualStorageBCD);
        processQuantity.modMiddle.apply(formattedStringBuilder, 0, 0);
        return processQuantity.modMiddle.getPrefixLength();
    }

    public MicroProps getRawMicroProps() {
        return this.micros;
    }

    private static boolean unitIsCurrency(MeasureUnit measureUnit) {
        return measureUnit != null && "currency".equals(measureUnit.getType());
    }

    private static boolean unitIsNoUnit(MeasureUnit measureUnit) {
        return measureUnit == null || "none".equals(measureUnit.getType());
    }

    private static boolean unitIsPercent(MeasureUnit measureUnit) {
        return measureUnit != null && Constants.ATTRNAME_PERCENT.equals(measureUnit.getSubtype());
    }

    private static boolean unitIsPermille(MeasureUnit measureUnit) {
        return measureUnit != null && "permille".equals(measureUnit.getSubtype());
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r5v17, types: [ohos.global.icu.impl.number.AffixPatternProvider] */
    /* JADX WARNING: Unknown variable types count: 1 */
    private static MicroPropsGenerator macrosToMicroGenerator(MacroProps macroProps, MicroProps microProps, boolean z) {
        NumberingSystem numberingSystem;
        String str;
        MutablePatternModifier mutablePatternModifier;
        MicroPropsGenerator microPropsGenerator2;
        MutablePatternModifier mutablePatternModifier2;
        CompactData.CompactType compactType;
        int i;
        CurrencyData.CurrencyFormatInfo formatInfo;
        boolean unitIsCurrency = unitIsCurrency(macroProps.unit);
        boolean unitIsNoUnit = unitIsNoUnit(macroProps.unit);
        boolean unitIsPercent = unitIsPercent(macroProps.unit);
        boolean unitIsPermille = unitIsPermille(macroProps.unit);
        boolean z2 = macroProps.sign == NumberFormatter.SignDisplay.ACCOUNTING || macroProps.sign == NumberFormatter.SignDisplay.ACCOUNTING_ALWAYS || macroProps.sign == NumberFormatter.SignDisplay.ACCOUNTING_EXCEPT_ZERO;
        Currency currency = unitIsCurrency ? (Currency) macroProps.unit : DEFAULT_CURRENCY;
        NumberFormatter.UnitWidth unitWidth = NumberFormatter.UnitWidth.SHORT;
        if (macroProps.unitWidth != null) {
            unitWidth = macroProps.unitWidth;
        }
        boolean z3 = !unitIsCurrency && !unitIsNoUnit && (unitWidth == NumberFormatter.UnitWidth.FULL_NAME || (!unitIsPercent && !unitIsPermille));
        PluralRules pluralRules = macroProps.rules;
        if (macroProps.symbols instanceof NumberingSystem) {
            numberingSystem = (NumberingSystem) macroProps.symbols;
        } else {
            numberingSystem = NumberingSystem.getInstance(macroProps.loc);
        }
        microProps.nsName = numberingSystem.getName();
        if (macroProps.symbols instanceof DecimalFormatSymbols) {
            microProps.symbols = (DecimalFormatSymbols) macroProps.symbols;
        } else {
            microProps.symbols = DecimalFormatSymbols.forNumberingSystem(macroProps.loc, numberingSystem);
        }
        if (!unitIsCurrency || (formatInfo = CurrencyData.provider.getInstance(macroProps.loc, true).getFormatInfo(currency.getCurrencyCode())) == null) {
            str = null;
        } else {
            str = formatInfo.currencyPattern;
            microProps.symbols = (DecimalFormatSymbols) microProps.symbols.clone();
            microProps.symbols.setMonetaryDecimalSeparatorString(formatInfo.monetaryDecimalSeparator);
            microProps.symbols.setMonetaryGroupingSeparatorString(formatInfo.monetaryGroupingSeparator);
        }
        if (str == null) {
            if (!z3) {
                if (unitIsPercent || unitIsPermille) {
                    i = 2;
                    str = NumberFormat.getPatternForStyleAndNumberingSystem(macroProps.loc, microProps.nsName, i);
                } else if (unitIsCurrency && unitWidth != NumberFormatter.UnitWidth.FULL_NAME) {
                    i = z2 ? 7 : 1;
                    str = NumberFormat.getPatternForStyleAndNumberingSystem(macroProps.loc, microProps.nsName, i);
                }
            }
            i = 0;
            str = NumberFormat.getPatternForStyleAndNumberingSystem(macroProps.loc, microProps.nsName, i);
        }
        PatternStringParser.ParsedPatternInfo parseToPatternInfo = PatternStringParser.parseToPatternInfo(str);
        MicroPropsGenerator multiplierFormatHandler = macroProps.scale != null ? new MultiplierFormatHandler(macroProps.scale, microProps) : microProps;
        if (macroProps.precision != null) {
            microProps.rounder = macroProps.precision;
        } else if (macroProps.notation instanceof CompactNotation) {
            microProps.rounder = Precision.COMPACT_STRATEGY;
        } else if (unitIsCurrency) {
            microProps.rounder = Precision.MONETARY_STANDARD;
        } else {
            microProps.rounder = Precision.DEFAULT_MAX_FRAC_6;
        }
        if (macroProps.roundingMode != null) {
            microProps.rounder = microProps.rounder.withMode(RoundingUtils.mathContextUnlimited(macroProps.roundingMode));
        }
        microProps.rounder = microProps.rounder.withLocaleData(currency);
        if (macroProps.grouping instanceof Grouper) {
            microProps.grouping = (Grouper) macroProps.grouping;
        } else if (macroProps.grouping instanceof NumberFormatter.GroupingStrategy) {
            microProps.grouping = Grouper.forStrategy((NumberFormatter.GroupingStrategy) macroProps.grouping);
        } else if (macroProps.notation instanceof CompactNotation) {
            microProps.grouping = Grouper.forStrategy(NumberFormatter.GroupingStrategy.MIN2);
        } else {
            microProps.grouping = Grouper.forStrategy(NumberFormatter.GroupingStrategy.AUTO);
        }
        microProps.grouping = microProps.grouping.withLocaleData(macroProps.loc, parseToPatternInfo);
        if (macroProps.padder != null) {
            microProps.padding = macroProps.padder;
        } else {
            microProps.padding = Padder.NONE;
        }
        if (macroProps.integerWidth != null) {
            microProps.integerWidth = macroProps.integerWidth;
        } else {
            microProps.integerWidth = IntegerWidth.DEFAULT;
        }
        if (macroProps.sign != null) {
            microProps.sign = macroProps.sign;
        } else {
            microProps.sign = NumberFormatter.SignDisplay.AUTO;
        }
        if (macroProps.decimal != null) {
            microProps.decimal = macroProps.decimal;
        } else {
            microProps.decimal = NumberFormatter.DecimalSeparatorDisplay.AUTO;
        }
        microProps.useCurrency = unitIsCurrency;
        if (macroProps.notation instanceof ScientificNotation) {
            multiplierFormatHandler = ((ScientificNotation) macroProps.notation).withLocaleData(microProps.symbols, z, multiplierFormatHandler);
        } else {
            microProps.modInner = ConstantAffixModifier.EMPTY;
        }
        MutablePatternModifier mutablePatternModifier3 = new MutablePatternModifier(false);
        PatternStringParser.ParsedPatternInfo parsedPatternInfo = parseToPatternInfo;
        if (macroProps.affixProvider != null) {
            parsedPatternInfo = macroProps.affixProvider;
        }
        mutablePatternModifier3.setPatternInfo(parsedPatternInfo, null);
        mutablePatternModifier3.setPatternAttributes(microProps.sign, unitIsPermille);
        if (mutablePatternModifier3.needsPlurals()) {
            if (pluralRules == null) {
                pluralRules = PluralRules.forLocale(macroProps.loc);
            }
            mutablePatternModifier3.setSymbols(microProps.symbols, currency, unitWidth, pluralRules);
            mutablePatternModifier = null;
        } else {
            mutablePatternModifier = null;
            mutablePatternModifier3.setSymbols(microProps.symbols, currency, unitWidth, null);
        }
        if (z) {
            microPropsGenerator2 = mutablePatternModifier3.createImmutableAndChain(multiplierFormatHandler);
        } else {
            microPropsGenerator2 = mutablePatternModifier3.addToChain(multiplierFormatHandler);
        }
        if (z3) {
            PluralRules forLocale = pluralRules == null ? PluralRules.forLocale(macroProps.loc) : pluralRules;
            mutablePatternModifier2 = mutablePatternModifier;
            microPropsGenerator2 = LongNameHandler.forMeasureUnit(macroProps.loc, macroProps.unit, macroProps.perUnit, unitWidth, forLocale, microPropsGenerator2);
            pluralRules = forLocale;
        } else {
            mutablePatternModifier2 = mutablePatternModifier;
            if (!unitIsCurrency || unitWidth != NumberFormatter.UnitWidth.FULL_NAME) {
                microProps.modOuter = ConstantAffixModifier.EMPTY;
            } else {
                if (pluralRules == null) {
                    pluralRules = PluralRules.forLocale(macroProps.loc);
                }
                microPropsGenerator2 = LongNameHandler.forCurrencyLongNames(macroProps.loc, currency, pluralRules, microPropsGenerator2);
            }
        }
        if (!(macroProps.notation instanceof CompactNotation)) {
            return microPropsGenerator2;
        }
        if (pluralRules == null) {
            pluralRules = PluralRules.forLocale(macroProps.loc);
        }
        if (!(macroProps.unit instanceof Currency) || macroProps.unitWidth == NumberFormatter.UnitWidth.FULL_NAME) {
            compactType = CompactData.CompactType.DECIMAL;
        } else {
            compactType = CompactData.CompactType.CURRENCY;
        }
        return ((CompactNotation) macroProps.notation).withLocaleData(macroProps.loc, microProps.nsName, compactType, pluralRules, z ? mutablePatternModifier3 : mutablePatternModifier2, microPropsGenerator2);
    }

    public static int writeAffixes(MicroProps microProps, FormattedStringBuilder formattedStringBuilder, int i, int i2) {
        int apply = microProps.modInner.apply(formattedStringBuilder, i, i2);
        if (microProps.padding.isValid()) {
            microProps.padding.padAndApply(microProps.modMiddle, microProps.modOuter, formattedStringBuilder, i, i2 + apply);
            return apply;
        }
        int apply2 = apply + microProps.modMiddle.apply(formattedStringBuilder, i, i2 + apply);
        return apply2 + microProps.modOuter.apply(formattedStringBuilder, i, i2 + apply2);
    }

    public static int writeNumber(MicroProps microProps, DecimalQuantity decimalQuantity, FormattedStringBuilder formattedStringBuilder, int i) {
        String str;
        int insert;
        if (decimalQuantity.isInfinite()) {
            insert = formattedStringBuilder.insert(i + 0, (CharSequence) microProps.symbols.getInfinity(), (Format.Field) NumberFormat.Field.INTEGER);
        } else if (decimalQuantity.isNaN()) {
            insert = formattedStringBuilder.insert(i + 0, (CharSequence) microProps.symbols.getNaN(), (Format.Field) NumberFormat.Field.INTEGER);
        } else {
            int writeIntegerDigits = writeIntegerDigits(microProps, decimalQuantity, formattedStringBuilder, i + 0) + 0;
            if (decimalQuantity.getLowerDisplayMagnitude() < 0 || microProps.decimal == NumberFormatter.DecimalSeparatorDisplay.ALWAYS) {
                int i2 = writeIntegerDigits + i;
                if (microProps.useCurrency) {
                    str = microProps.symbols.getMonetaryDecimalSeparatorString();
                } else {
                    str = microProps.symbols.getDecimalSeparatorString();
                }
                writeIntegerDigits += formattedStringBuilder.insert(i2, (CharSequence) str, (Format.Field) NumberFormat.Field.DECIMAL_SEPARATOR);
            }
            return writeFractionDigits(microProps, decimalQuantity, formattedStringBuilder, i + writeIntegerDigits) + writeIntegerDigits;
        }
        return insert + 0;
    }

    private static int writeIntegerDigits(MicroProps microProps, DecimalQuantity decimalQuantity, FormattedStringBuilder formattedStringBuilder, int i) {
        int i2;
        String str;
        int upperDisplayMagnitude = decimalQuantity.getUpperDisplayMagnitude() + 1;
        int i3 = 0;
        for (int i4 = 0; i4 < upperDisplayMagnitude; i4++) {
            if (microProps.grouping.groupAtPosition(i4, decimalQuantity)) {
                if (microProps.useCurrency) {
                    str = microProps.symbols.getMonetaryGroupingSeparatorString();
                } else {
                    str = microProps.symbols.getGroupingSeparatorString();
                }
                i3 += formattedStringBuilder.insert(i, (CharSequence) str, (Format.Field) NumberFormat.Field.GROUPING_SEPARATOR);
            }
            byte digit = decimalQuantity.getDigit(i4);
            if (microProps.symbols.getCodePointZero() != -1) {
                i2 = formattedStringBuilder.insertCodePoint(i, microProps.symbols.getCodePointZero() + digit, NumberFormat.Field.INTEGER);
            } else {
                i2 = formattedStringBuilder.insert(i, (CharSequence) microProps.symbols.getDigitStringsLocal()[digit], (Format.Field) NumberFormat.Field.INTEGER);
            }
            i3 += i2;
        }
        return i3;
    }

    private static int writeFractionDigits(MicroProps microProps, DecimalQuantity decimalQuantity, FormattedStringBuilder formattedStringBuilder, int i) {
        int i2;
        int i3 = -decimalQuantity.getLowerDisplayMagnitude();
        int i4 = 0;
        for (int i5 = 0; i5 < i3; i5++) {
            byte digit = decimalQuantity.getDigit((-i5) - 1);
            if (microProps.symbols.getCodePointZero() != -1) {
                i2 = formattedStringBuilder.insertCodePoint(i4 + i, microProps.symbols.getCodePointZero() + digit, NumberFormat.Field.FRACTION);
            } else {
                i2 = formattedStringBuilder.insert(i4 + i, (CharSequence) microProps.symbols.getDigitStringsLocal()[digit], (Format.Field) NumberFormat.Field.FRACTION);
            }
            i4 += i2;
        }
        return i4;
    }
}
