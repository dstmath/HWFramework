package android.icu.number;

import android.icu.impl.number.CompactData;
import android.icu.impl.number.ConstantAffixModifier;
import android.icu.impl.number.DecimalQuantity;
import android.icu.impl.number.LongNameHandler;
import android.icu.impl.number.MacroProps;
import android.icu.impl.number.MicroProps;
import android.icu.impl.number.MicroPropsGenerator;
import android.icu.impl.number.MutablePatternModifier;
import android.icu.impl.number.NumberStringBuilder;
import android.icu.impl.number.Padder;
import android.icu.impl.number.PatternStringParser;
import android.icu.number.NumberFormatter;
import android.icu.text.DecimalFormatSymbols;
import android.icu.text.NumberFormat;
import android.icu.text.NumberingSystem;
import android.icu.text.PluralRules;
import android.icu.util.Currency;
import android.icu.util.MeasureUnit;

class NumberFormatterImpl {
    private static final Currency DEFAULT_CURRENCY = Currency.getInstance("XXX");
    final MicroPropsGenerator microPropsGenerator;

    public static NumberFormatterImpl fromMacros(MacroProps macros) {
        return new NumberFormatterImpl(macrosToMicroGenerator(macros, true));
    }

    public static MicroProps applyStatic(MacroProps macros, DecimalQuantity inValue, NumberStringBuilder outString) {
        MicroProps micros = macrosToMicroGenerator(macros, false).processQuantity(inValue);
        microsToString(micros, inValue, outString);
        return micros;
    }

    private NumberFormatterImpl(MicroPropsGenerator microPropsGenerator2) {
        this.microPropsGenerator = microPropsGenerator2;
    }

    public MicroProps apply(DecimalQuantity inValue, NumberStringBuilder outString) {
        MicroProps micros = this.microPropsGenerator.processQuantity(inValue);
        microsToString(micros, inValue, outString);
        return micros;
    }

    private static boolean unitIsCurrency(MeasureUnit unit) {
        return unit != null && "currency".equals(unit.getType());
    }

    private static boolean unitIsNoUnit(MeasureUnit unit) {
        return unit == null || "none".equals(unit.getType());
    }

    private static boolean unitIsPercent(MeasureUnit unit) {
        return unit != null && "percent".equals(unit.getSubtype());
    }

    private static boolean unitIsPermille(MeasureUnit unit) {
        return unit != null && "permille".equals(unit.getSubtype());
    }

    private static MicroPropsGenerator macrosToMicroGenerator(MacroProps macros, boolean safe) {
        NumberingSystem ns;
        int patternStyle;
        PatternStringParser.ParsedPatternInfo patternInfo;
        MicroPropsGenerator chain;
        CompactData.CompactType compactType;
        MacroProps macroProps = macros;
        boolean z = safe;
        MicroProps micros = new MicroProps(z);
        MicroPropsGenerator chain2 = micros;
        boolean isCurrency = unitIsCurrency(macroProps.unit);
        boolean isNoUnit = unitIsNoUnit(macroProps.unit);
        boolean isPercent = isNoUnit && unitIsPercent(macroProps.unit);
        boolean isPermille = isNoUnit && unitIsPermille(macroProps.unit);
        boolean isCldrUnit = !isCurrency && !isNoUnit;
        boolean isAccounting = macroProps.sign == NumberFormatter.SignDisplay.ACCOUNTING || macroProps.sign == NumberFormatter.SignDisplay.ACCOUNTING_ALWAYS;
        Currency currency = isCurrency ? (Currency) macroProps.unit : DEFAULT_CURRENCY;
        NumberFormatter.UnitWidth unitWidth = NumberFormatter.UnitWidth.SHORT;
        if (macroProps.unitWidth != null) {
            unitWidth = macroProps.unitWidth;
        }
        PluralRules rules = macroProps.rules;
        if (macroProps.symbols instanceof NumberingSystem) {
            ns = (NumberingSystem) macroProps.symbols;
        } else {
            ns = NumberingSystem.getInstance(macroProps.loc);
        }
        String nsName = ns.getName();
        if (isPercent || isPermille) {
            patternStyle = 2;
        } else if (!isCurrency || unitWidth == NumberFormatter.UnitWidth.FULL_NAME) {
            patternStyle = 0;
        } else if (isAccounting) {
            patternStyle = 7;
        } else {
            patternStyle = 1;
        }
        boolean z2 = isNoUnit;
        String pattern = NumberFormat.getPatternForStyleAndNumberingSystem(macroProps.loc, nsName, patternStyle);
        int i = patternStyle;
        PatternStringParser.ParsedPatternInfo patternInfo2 = PatternStringParser.parseToPatternInfo(pattern);
        String str = pattern;
        if (macroProps.symbols instanceof DecimalFormatSymbols) {
            micros.symbols = (DecimalFormatSymbols) macroProps.symbols;
        } else {
            micros.symbols = DecimalFormatSymbols.forNumberingSystem(macroProps.loc, ns);
        }
        if (macroProps.multiplier != null) {
            chain2 = macroProps.multiplier.copyAndChain(chain2);
        }
        if (macroProps.rounder != null) {
            micros.rounding = macroProps.rounder;
        } else if (macroProps.notation instanceof CompactNotation) {
            micros.rounding = Rounder.COMPACT_STRATEGY;
        } else if (isCurrency) {
            micros.rounding = Rounder.MONETARY_STANDARD;
        } else {
            micros.rounding = Rounder.MAX_FRAC_6;
        }
        micros.rounding = micros.rounding.withLocaleData(currency);
        if (macroProps.grouper != null) {
            micros.grouping = macroProps.grouper;
        } else if (macroProps.notation instanceof CompactNotation) {
            micros.grouping = Grouper.minTwoDigits();
        } else {
            micros.grouping = Grouper.defaults();
        }
        micros.grouping = micros.grouping.withLocaleData(patternInfo2);
        if (macroProps.padder != null) {
            micros.padding = macroProps.padder;
        } else {
            micros.padding = Padder.NONE;
        }
        if (macroProps.integerWidth != null) {
            micros.integerWidth = macroProps.integerWidth;
        } else {
            micros.integerWidth = IntegerWidth.DEFAULT;
        }
        if (macroProps.sign != null) {
            micros.sign = macroProps.sign;
        } else {
            micros.sign = NumberFormatter.SignDisplay.AUTO;
        }
        if (macroProps.decimal != null) {
            micros.decimal = macroProps.decimal;
        } else {
            micros.decimal = NumberFormatter.DecimalSeparatorDisplay.AUTO;
        }
        micros.useCurrency = isCurrency;
        if (macroProps.notation instanceof ScientificNotation) {
            patternInfo = patternInfo2;
            chain2 = ((ScientificNotation) macroProps.notation).withLocaleData(micros.symbols, z, chain2);
        } else {
            patternInfo = patternInfo2;
            micros.modInner = ConstantAffixModifier.EMPTY;
        }
        MutablePatternModifier patternMod = new MutablePatternModifier(false);
        patternMod.setPatternInfo(macroProps.affixProvider != null ? macroProps.affixProvider : patternInfo);
        patternMod.setPatternAttributes(micros.sign, isPermille);
        boolean z3 = isPercent;
        if (patternMod.needsPlurals()) {
            if (rules == null) {
                rules = PluralRules.forLocale(macroProps.loc);
            }
            patternMod.setSymbols(micros.symbols, currency, unitWidth, rules);
        } else {
            patternMod.setSymbols(micros.symbols, currency, unitWidth, null);
        }
        if (z) {
            chain = patternMod.createImmutableAndChain(chain2);
        } else {
            chain = patternMod.addToChain(chain2);
        }
        if (isCldrUnit) {
            if (rules == null) {
                rules = PluralRules.forLocale(macroProps.loc);
            }
            chain = LongNameHandler.forMeasureUnit(macroProps.loc, macroProps.unit, unitWidth, rules, chain);
        } else if (!isCurrency || unitWidth != NumberFormatter.UnitWidth.FULL_NAME) {
            micros.modOuter = ConstantAffixModifier.EMPTY;
        } else {
            if (rules == null) {
                rules = PluralRules.forLocale(macroProps.loc);
            }
            chain = LongNameHandler.forCurrencyLongNames(macroProps.loc, currency, rules, chain);
        }
        if (!(macroProps.notation instanceof CompactNotation)) {
            return chain;
        }
        if (rules == null) {
            rules = PluralRules.forLocale(macroProps.loc);
        }
        if (!(macroProps.unit instanceof Currency) || macroProps.unitWidth == NumberFormatter.UnitWidth.FULL_NAME) {
            compactType = CompactData.CompactType.DECIMAL;
        } else {
            compactType = CompactData.CompactType.CURRENCY;
        }
        return ((CompactNotation) macroProps.notation).withLocaleData(macroProps.loc, nsName, compactType, rules, z ? patternMod : null, chain);
    }

    private static void microsToString(MicroProps micros, DecimalQuantity quantity, NumberStringBuilder string) {
        micros.rounding.apply(quantity);
        if (micros.integerWidth.maxInt == -1) {
            quantity.setIntegerLength(micros.integerWidth.minInt, Integer.MAX_VALUE);
        } else {
            quantity.setIntegerLength(micros.integerWidth.minInt, micros.integerWidth.maxInt);
        }
        int length = writeNumber(micros, quantity, string);
        int length2 = length + micros.modInner.apply(string, 0, length);
        if (micros.padding.isValid()) {
            micros.padding.padAndApply(micros.modMiddle, micros.modOuter, string, 0, length2);
            return;
        }
        int length3 = length2 + micros.modMiddle.apply(string, 0, length2);
        int length4 = length3 + micros.modOuter.apply(string, 0, length3);
    }

    private static int writeNumber(MicroProps micros, DecimalQuantity quantity, NumberStringBuilder string) {
        String str;
        if (quantity.isInfinite()) {
            return 0 + string.insert(0, (CharSequence) micros.symbols.getInfinity(), NumberFormat.Field.INTEGER);
        }
        if (quantity.isNaN()) {
            return 0 + string.insert(0, (CharSequence) micros.symbols.getNaN(), NumberFormat.Field.INTEGER);
        }
        int length = 0 + writeIntegerDigits(micros, quantity, string);
        if (quantity.getLowerDisplayMagnitude() < 0 || micros.decimal == NumberFormatter.DecimalSeparatorDisplay.ALWAYS) {
            if (micros.useCurrency) {
                str = micros.symbols.getMonetaryDecimalSeparatorString();
            } else {
                str = micros.symbols.getDecimalSeparatorString();
            }
            length += string.insert(length, (CharSequence) str, NumberFormat.Field.DECIMAL_SEPARATOR);
        }
        return length + writeFractionDigits(micros, quantity, string);
    }

    private static int writeIntegerDigits(MicroProps micros, DecimalQuantity quantity, NumberStringBuilder string) {
        int i;
        String str;
        int integerCount = quantity.getUpperDisplayMagnitude() + 1;
        int length = 0;
        for (int i2 = 0; i2 < integerCount; i2++) {
            if (micros.grouping.groupAtPosition(i2, quantity)) {
                if (micros.useCurrency) {
                    str = micros.symbols.getMonetaryGroupingSeparatorString();
                } else {
                    str = micros.symbols.getGroupingSeparatorString();
                }
                length += string.insert(0, (CharSequence) str, NumberFormat.Field.GROUPING_SEPARATOR);
            }
            byte nextDigit = quantity.getDigit(i2);
            if (micros.symbols.getCodePointZero() != -1) {
                i = string.insertCodePoint(0, micros.symbols.getCodePointZero() + nextDigit, NumberFormat.Field.INTEGER);
            } else {
                i = string.insert(0, (CharSequence) micros.symbols.getDigitStringsLocal()[nextDigit], NumberFormat.Field.INTEGER);
            }
            length += i;
        }
        return length;
    }

    private static int writeFractionDigits(MicroProps micros, DecimalQuantity quantity, NumberStringBuilder string) {
        int i;
        int length = 0;
        int fractionCount = -quantity.getLowerDisplayMagnitude();
        for (int i2 = 0; i2 < fractionCount; i2++) {
            byte nextDigit = quantity.getDigit((-i2) - 1);
            if (micros.symbols.getCodePointZero() != -1) {
                i = string.appendCodePoint(micros.symbols.getCodePointZero() + nextDigit, NumberFormat.Field.FRACTION);
            } else {
                i = string.append((CharSequence) micros.symbols.getDigitStringsLocal()[nextDigit], NumberFormat.Field.FRACTION);
            }
            length += i;
        }
        return length;
    }
}
