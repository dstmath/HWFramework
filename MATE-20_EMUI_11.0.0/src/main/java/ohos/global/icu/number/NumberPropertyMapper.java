package ohos.global.icu.number;

import java.math.BigDecimal;
import java.math.MathContext;
import ohos.global.icu.impl.number.AffixPatternProvider;
import ohos.global.icu.impl.number.CurrencyPluralInfoAffixProvider;
import ohos.global.icu.impl.number.CustomSymbolCurrency;
import ohos.global.icu.impl.number.DecimalFormatProperties;
import ohos.global.icu.impl.number.Grouper;
import ohos.global.icu.impl.number.MacroProps;
import ohos.global.icu.impl.number.Padder;
import ohos.global.icu.impl.number.PatternStringParser;
import ohos.global.icu.impl.number.PatternStringUtils;
import ohos.global.icu.impl.number.PropertiesAffixPatternProvider;
import ohos.global.icu.impl.number.RoundingUtils;
import ohos.global.icu.number.NumberFormatter;
import ohos.global.icu.number.Precision;
import ohos.global.icu.text.CompactDecimalFormat;
import ohos.global.icu.text.DecimalFormatSymbols;
import ohos.global.icu.text.PluralRules;
import ohos.global.icu.util.Currency;
import ohos.global.icu.util.ULocale;

/* access modifiers changed from: package-private */
public final class NumberPropertyMapper {
    NumberPropertyMapper() {
    }

    public static UnlocalizedNumberFormatter create(DecimalFormatProperties decimalFormatProperties, DecimalFormatSymbols decimalFormatSymbols) {
        return (UnlocalizedNumberFormatter) NumberFormatter.with().macros(oldToNew(decimalFormatProperties, decimalFormatSymbols, null));
    }

    public static UnlocalizedNumberFormatter create(DecimalFormatProperties decimalFormatProperties, DecimalFormatSymbols decimalFormatSymbols, DecimalFormatProperties decimalFormatProperties2) {
        return (UnlocalizedNumberFormatter) NumberFormatter.with().macros(oldToNew(decimalFormatProperties, decimalFormatSymbols, decimalFormatProperties2));
    }

    public static UnlocalizedNumberFormatter create(String str, DecimalFormatSymbols decimalFormatSymbols) {
        return create(PatternStringParser.parseToProperties(str), decimalFormatSymbols);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:70:0x0103, code lost:
        if (r9 > 999) goto L_0x00fd;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:89:0x0139, code lost:
        if (r14 > r4) goto L_0x0133;
     */
    public static MacroProps oldToNew(DecimalFormatProperties decimalFormatProperties, DecimalFormatSymbols decimalFormatSymbols, DecimalFormatProperties decimalFormatProperties2) {
        AffixPatternProvider affixPatternProvider;
        Precision precision;
        NumberFormatter.DecimalSeparatorDisplay decimalSeparatorDisplay;
        BigDecimal bigDecimal;
        int i;
        MacroProps macroProps = new MacroProps();
        ULocale uLocale = decimalFormatSymbols.getULocale();
        macroProps.symbols = decimalFormatSymbols;
        PluralRules pluralRules = decimalFormatProperties.getPluralRules();
        if (pluralRules == null && decimalFormatProperties.getCurrencyPluralInfo() != null) {
            pluralRules = decimalFormatProperties.getCurrencyPluralInfo().getPluralRules();
        }
        macroProps.rules = pluralRules;
        if (decimalFormatProperties.getCurrencyPluralInfo() == null) {
            affixPatternProvider = new PropertiesAffixPatternProvider(decimalFormatProperties);
        } else {
            affixPatternProvider = new CurrencyPluralInfoAffixProvider(decimalFormatProperties.getCurrencyPluralInfo(), decimalFormatProperties);
        }
        macroProps.affixProvider = affixPatternProvider;
        boolean z = (decimalFormatProperties.getCurrency() == null && decimalFormatProperties.getCurrencyPluralInfo() == null && decimalFormatProperties.getCurrencyUsage() == null && !affixPatternProvider.hasCurrencySign()) ? false : true;
        Currency resolve = CustomSymbolCurrency.resolve(decimalFormatProperties.getCurrency(), uLocale, decimalFormatSymbols);
        Currency.CurrencyUsage currencyUsage = decimalFormatProperties.getCurrencyUsage();
        boolean z2 = currencyUsage != null;
        if (!z2) {
            currencyUsage = Currency.CurrencyUsage.STANDARD;
        }
        if (z) {
            macroProps.unit = resolve;
        }
        int maximumIntegerDigits = decimalFormatProperties.getMaximumIntegerDigits();
        int minimumIntegerDigits = decimalFormatProperties.getMinimumIntegerDigits();
        int maximumFractionDigits = decimalFormatProperties.getMaximumFractionDigits();
        int minimumFractionDigits = decimalFormatProperties.getMinimumFractionDigits();
        int minimumSignificantDigits = decimalFormatProperties.getMinimumSignificantDigits();
        int maximumSignificantDigits = decimalFormatProperties.getMaximumSignificantDigits();
        BigDecimal roundingIncrement = decimalFormatProperties.getRoundingIncrement();
        MathContext mathContextOrUnlimited = RoundingUtils.getMathContextOrUnlimited(decimalFormatProperties);
        boolean z3 = (minimumFractionDigits == -1 && maximumFractionDigits == -1) ? false : true;
        boolean z4 = (minimumSignificantDigits == -1 && maximumSignificantDigits == -1) ? false : true;
        if (z) {
            if (minimumFractionDigits == -1 && maximumFractionDigits == -1) {
                minimumFractionDigits = resolve.getDefaultFractionDigits(currencyUsage);
                maximumFractionDigits = resolve.getDefaultFractionDigits(currencyUsage);
            } else if (minimumFractionDigits == -1) {
                minimumFractionDigits = Math.min(maximumFractionDigits, resolve.getDefaultFractionDigits(currencyUsage));
            } else if (maximumFractionDigits == -1) {
                maximumFractionDigits = Math.max(minimumFractionDigits, resolve.getDefaultFractionDigits(currencyUsage));
            }
        }
        if (minimumIntegerDigits != 0 || maximumFractionDigits == 0) {
            if (minimumFractionDigits < 0) {
                minimumFractionDigits = 0;
            }
            if (maximumFractionDigits < 0) {
                maximumFractionDigits = -1;
            } else if (maximumFractionDigits < minimumFractionDigits) {
                maximumFractionDigits = minimumFractionDigits;
            }
            if (minimumIntegerDigits <= 0 || minimumIntegerDigits > 999) {
                minimumIntegerDigits = 1;
            }
            if (maximumIntegerDigits >= 0) {
                if (maximumIntegerDigits < minimumIntegerDigits) {
                    maximumIntegerDigits = minimumIntegerDigits;
                }
            }
            maximumIntegerDigits = -1;
        } else {
            if (minimumFractionDigits <= 0) {
                minimumFractionDigits = 1;
            }
            if (maximumFractionDigits < 0) {
                maximumFractionDigits = -1;
            } else if (maximumFractionDigits < minimumFractionDigits) {
                maximumFractionDigits = minimumFractionDigits;
            }
            if (maximumIntegerDigits < 0 || maximumIntegerDigits > 999) {
                maximumIntegerDigits = -1;
            }
            minimumIntegerDigits = 0;
        }
        if (z2) {
            precision = Precision.constructCurrency(currencyUsage).withCurrency(resolve);
        } else if (roundingIncrement != null) {
            precision = PatternStringUtils.ignoreRoundingIncrement(roundingIncrement, maximumFractionDigits) ? Precision.constructFraction(minimumFractionDigits, maximumFractionDigits) : Precision.constructIncrement(roundingIncrement);
        } else if (z4) {
            if (minimumSignificantDigits < 1) {
                i = RoundingUtils.MAX_INT_FRAC_SIG;
                minimumSignificantDigits = 1;
            } else {
                i = RoundingUtils.MAX_INT_FRAC_SIG;
                if (minimumSignificantDigits > 999) {
                    minimumSignificantDigits = 999;
                }
            }
            if (maximumSignificantDigits >= 0) {
                if (maximumSignificantDigits < minimumSignificantDigits) {
                    maximumSignificantDigits = minimumSignificantDigits;
                }
                precision = Precision.constructSignificant(minimumSignificantDigits, maximumSignificantDigits);
            }
            maximumSignificantDigits = i;
            precision = Precision.constructSignificant(minimumSignificantDigits, maximumSignificantDigits);
        } else if (z3) {
            precision = Precision.constructFraction(minimumFractionDigits, maximumFractionDigits);
        } else {
            precision = z ? Precision.constructCurrency(currencyUsage) : null;
        }
        if (precision != null) {
            precision = precision.withMode(mathContextOrUnlimited);
            macroProps.precision = precision;
        }
        macroProps.integerWidth = IntegerWidth.zeroFillTo(minimumIntegerDigits).truncateAt(maximumIntegerDigits);
        macroProps.grouping = Grouper.forProperties(decimalFormatProperties);
        if (decimalFormatProperties.getFormatWidth() > 0) {
            macroProps.padder = Padder.forProperties(decimalFormatProperties);
        }
        if (decimalFormatProperties.getDecimalSeparatorAlwaysShown()) {
            decimalSeparatorDisplay = NumberFormatter.DecimalSeparatorDisplay.ALWAYS;
        } else {
            decimalSeparatorDisplay = NumberFormatter.DecimalSeparatorDisplay.AUTO;
        }
        macroProps.decimal = decimalSeparatorDisplay;
        macroProps.sign = decimalFormatProperties.getSignAlwaysShown() ? NumberFormatter.SignDisplay.ALWAYS : NumberFormatter.SignDisplay.AUTO;
        if (decimalFormatProperties.getMinimumExponentDigits() != -1) {
            if (maximumIntegerDigits > 8) {
                macroProps.integerWidth = IntegerWidth.zeroFillTo(minimumIntegerDigits).truncateAt(minimumIntegerDigits);
                maximumIntegerDigits = minimumIntegerDigits;
            } else if (maximumIntegerDigits > minimumIntegerDigits && minimumIntegerDigits > 1) {
                macroProps.integerWidth = IntegerWidth.zeroFillTo(1).truncateAt(maximumIntegerDigits);
                minimumIntegerDigits = 1;
            }
            int i2 = maximumIntegerDigits < 0 ? -1 : maximumIntegerDigits;
            macroProps.notation = new ScientificNotation(i2, i2 == minimumIntegerDigits, decimalFormatProperties.getMinimumExponentDigits(), decimalFormatProperties.getExponentSignAlwaysShown() ? NumberFormatter.SignDisplay.ALWAYS : NumberFormatter.SignDisplay.AUTO);
            if (macroProps.precision instanceof FractionPrecision) {
                int maximumIntegerDigits2 = decimalFormatProperties.getMaximumIntegerDigits();
                int minimumIntegerDigits2 = decimalFormatProperties.getMinimumIntegerDigits();
                int minimumFractionDigits2 = decimalFormatProperties.getMinimumFractionDigits();
                int maximumFractionDigits2 = decimalFormatProperties.getMaximumFractionDigits();
                if (minimumIntegerDigits2 == 0 && maximumFractionDigits2 == 0) {
                    macroProps.precision = Precision.constructInfinite().withMode(mathContextOrUnlimited);
                } else if (minimumIntegerDigits2 == 0 && minimumFractionDigits2 == 0) {
                    macroProps.precision = Precision.constructSignificant(1, maximumFractionDigits2 + 1).withMode(mathContextOrUnlimited);
                } else {
                    int i3 = 1;
                    int i4 = maximumFractionDigits2 + minimumIntegerDigits2;
                    if (maximumIntegerDigits2 <= minimumIntegerDigits2 || minimumIntegerDigits2 <= 1) {
                        i3 = minimumIntegerDigits2;
                    }
                    macroProps.precision = Precision.constructSignificant(i3 + minimumFractionDigits2, i4).withMode(mathContextOrUnlimited);
                }
            }
        }
        if (decimalFormatProperties.getCompactStyle() != null) {
            if (decimalFormatProperties.getCompactCustomData() != null) {
                macroProps.notation = new CompactNotation(decimalFormatProperties.getCompactCustomData());
            } else if (decimalFormatProperties.getCompactStyle() == CompactDecimalFormat.CompactStyle.LONG) {
                macroProps.notation = Notation.compactLong();
            } else {
                macroProps.notation = Notation.compactShort();
            }
            bigDecimal = null;
            macroProps.affixProvider = null;
        } else {
            bigDecimal = null;
        }
        macroProps.scale = RoundingUtils.scaleFromProperties(decimalFormatProperties);
        if (decimalFormatProperties2 != null) {
            decimalFormatProperties2.setCurrency(resolve);
            decimalFormatProperties2.setMathContext(mathContextOrUnlimited);
            decimalFormatProperties2.setRoundingMode(mathContextOrUnlimited.getRoundingMode());
            decimalFormatProperties2.setMinimumIntegerDigits(minimumIntegerDigits);
            if (maximumIntegerDigits == -1) {
                maximumIntegerDigits = Integer.MAX_VALUE;
            }
            decimalFormatProperties2.setMaximumIntegerDigits(maximumIntegerDigits);
            if (precision instanceof CurrencyPrecision) {
                precision = ((CurrencyPrecision) precision).withCurrency(resolve);
            }
            if (precision instanceof Precision.FractionRounderImpl) {
                Precision.FractionRounderImpl fractionRounderImpl = (Precision.FractionRounderImpl) precision;
                minimumFractionDigits = fractionRounderImpl.minFrac;
                maximumFractionDigits = fractionRounderImpl.maxFrac;
            } else if (precision instanceof Precision.IncrementRounderImpl) {
                BigDecimal bigDecimal2 = ((Precision.IncrementRounderImpl) precision).increment;
                minimumFractionDigits = bigDecimal2.scale();
                maximumFractionDigits = bigDecimal2.scale();
                bigDecimal = bigDecimal2;
            } else if (precision instanceof Precision.SignificantRounderImpl) {
                Precision.SignificantRounderImpl significantRounderImpl = (Precision.SignificantRounderImpl) precision;
                minimumSignificantDigits = significantRounderImpl.minSig;
                maximumSignificantDigits = significantRounderImpl.maxSig;
            }
            decimalFormatProperties2.setMinimumFractionDigits(minimumFractionDigits);
            decimalFormatProperties2.setMaximumFractionDigits(maximumFractionDigits);
            decimalFormatProperties2.setMinimumSignificantDigits(minimumSignificantDigits);
            decimalFormatProperties2.setMaximumSignificantDigits(maximumSignificantDigits);
            decimalFormatProperties2.setRoundingIncrement(bigDecimal);
        }
        return macroProps;
    }
}
