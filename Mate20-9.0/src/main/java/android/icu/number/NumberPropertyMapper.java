package android.icu.number;

import android.icu.impl.StandardPlural;
import android.icu.impl.locale.LanguageTag;
import android.icu.impl.number.AffixPatternProvider;
import android.icu.impl.number.AffixUtils;
import android.icu.impl.number.CustomSymbolCurrency;
import android.icu.impl.number.DecimalFormatProperties;
import android.icu.impl.number.MacroProps;
import android.icu.impl.number.MultiplierImpl;
import android.icu.impl.number.Padder;
import android.icu.impl.number.PatternStringParser;
import android.icu.impl.number.RoundingUtils;
import android.icu.number.NumberFormatter;
import android.icu.number.Rounder;
import android.icu.text.CompactDecimalFormat;
import android.icu.text.CurrencyPluralInfo;
import android.icu.text.DecimalFormatSymbols;
import android.icu.util.Currency;
import android.icu.util.ULocale;
import java.math.BigDecimal;
import java.math.MathContext;

final class NumberPropertyMapper {
    static final /* synthetic */ boolean $assertionsDisabled = false;

    private static class CurrencyPluralInfoAffixProvider implements AffixPatternProvider {
        private final AffixPatternProvider[] affixesByPlural = new PatternStringParser.ParsedPatternInfo[StandardPlural.COUNT];

        public CurrencyPluralInfoAffixProvider(CurrencyPluralInfo cpi) {
            for (StandardPlural plural : StandardPlural.VALUES) {
                this.affixesByPlural[plural.ordinal()] = PatternStringParser.parseToPatternInfo(cpi.getCurrencyPluralPattern(plural.getKeyword()));
            }
        }

        public char charAt(int flags, int i) {
            return this.affixesByPlural[flags & 255].charAt(flags, i);
        }

        public int length(int flags) {
            return this.affixesByPlural[flags & 255].length(flags);
        }

        public boolean positiveHasPlusSign() {
            return this.affixesByPlural[StandardPlural.OTHER.ordinal()].positiveHasPlusSign();
        }

        public boolean hasNegativeSubpattern() {
            return this.affixesByPlural[StandardPlural.OTHER.ordinal()].hasNegativeSubpattern();
        }

        public boolean negativeHasMinusSign() {
            return this.affixesByPlural[StandardPlural.OTHER.ordinal()].negativeHasMinusSign();
        }

        public boolean hasCurrencySign() {
            return this.affixesByPlural[StandardPlural.OTHER.ordinal()].hasCurrencySign();
        }

        public boolean containsSymbolType(int type) {
            return this.affixesByPlural[StandardPlural.OTHER.ordinal()].containsSymbolType(type);
        }
    }

    private static class PropertiesAffixPatternProvider implements AffixPatternProvider {
        private final String negPrefix;
        private final String negSuffix;
        private final String posPrefix;
        private final String posSuffix;

        public PropertiesAffixPatternProvider(DecimalFormatProperties properties) {
            String str;
            String ppo = AffixUtils.escape(properties.getPositivePrefix());
            String pso = AffixUtils.escape(properties.getPositiveSuffix());
            String npo = AffixUtils.escape(properties.getNegativePrefix());
            String nso = AffixUtils.escape(properties.getNegativeSuffix());
            String ppp = properties.getPositivePrefixPattern();
            String psp = properties.getPositiveSuffixPattern();
            String npp = properties.getNegativePrefixPattern();
            String nsp = properties.getNegativeSuffixPattern();
            if (ppo != null) {
                this.posPrefix = ppo;
            } else if (ppp != null) {
                this.posPrefix = ppp;
            } else {
                this.posPrefix = "";
            }
            if (pso != null) {
                this.posSuffix = pso;
            } else if (psp != null) {
                this.posSuffix = psp;
            } else {
                this.posSuffix = "";
            }
            if (npo != null) {
                this.negPrefix = npo;
            } else if (npp != null) {
                this.negPrefix = npp;
            } else {
                if (ppp == null) {
                    str = LanguageTag.SEP;
                } else {
                    str = LanguageTag.SEP + ppp;
                }
                this.negPrefix = str;
            }
            if (nso != null) {
                this.negSuffix = nso;
            } else if (nsp != null) {
                this.negSuffix = nsp;
            } else {
                this.negSuffix = psp == null ? "" : psp;
            }
        }

        public char charAt(int flags, int i) {
            return getStringForFlags(flags).charAt(i);
        }

        public int length(int flags) {
            return getStringForFlags(flags).length();
        }

        private String getStringForFlags(int flags) {
            boolean negative = false;
            boolean prefix = (flags & 256) != 0;
            if ((flags & 512) != 0) {
                negative = true;
            }
            if (prefix && negative) {
                return this.negPrefix;
            }
            if (prefix) {
                return this.posPrefix;
            }
            if (negative) {
                return this.negSuffix;
            }
            return this.posSuffix;
        }

        public boolean positiveHasPlusSign() {
            return AffixUtils.containsType(this.posPrefix, -2) || AffixUtils.containsType(this.posSuffix, -2);
        }

        public boolean hasNegativeSubpattern() {
            return true;
        }

        public boolean negativeHasMinusSign() {
            return AffixUtils.containsType(this.negPrefix, -1) || AffixUtils.containsType(this.negSuffix, -1);
        }

        public boolean hasCurrencySign() {
            return AffixUtils.hasCurrencySymbols(this.posPrefix) || AffixUtils.hasCurrencySymbols(this.posSuffix) || AffixUtils.hasCurrencySymbols(this.negPrefix) || AffixUtils.hasCurrencySymbols(this.negSuffix);
        }

        public boolean containsSymbolType(int type) {
            return AffixUtils.containsType(this.posPrefix, type) || AffixUtils.containsType(this.posSuffix, type) || AffixUtils.containsType(this.negPrefix, type) || AffixUtils.containsType(this.negSuffix, type);
        }
    }

    NumberPropertyMapper() {
    }

    public static UnlocalizedNumberFormatter create(DecimalFormatProperties properties, DecimalFormatSymbols symbols) {
        return (UnlocalizedNumberFormatter) NumberFormatter.with().macros(oldToNew(properties, symbols, null));
    }

    public static UnlocalizedNumberFormatter create(String pattern, DecimalFormatSymbols symbols) {
        return create(PatternStringParser.parseToProperties(pattern), symbols);
    }

    public static MacroProps oldToNew(DecimalFormatProperties properties, DecimalFormatSymbols symbols, DecimalFormatProperties exportedProperties) {
        AffixPatternProvider affixProvider;
        int minFrac;
        int maxFrac;
        int minInt;
        int maxInt;
        int i;
        NumberFormatter.DecimalSeparatorDisplay decimalSeparatorDisplay;
        Rounder rounding_;
        int maxSig_;
        BigDecimal increment_;
        int i2;
        DecimalFormatSymbols decimalFormatSymbols = symbols;
        DecimalFormatProperties decimalFormatProperties = exportedProperties;
        MacroProps macros = new MacroProps();
        ULocale locale = symbols.getULocale();
        macros.symbols = decimalFormatSymbols;
        macros.rules = properties.getPluralRules();
        if (properties.getCurrencyPluralInfo() == null) {
            affixProvider = new PropertiesAffixPatternProvider(properties);
        } else {
            DecimalFormatProperties decimalFormatProperties2 = properties;
            affixProvider = new CurrencyPluralInfoAffixProvider(properties.getCurrencyPluralInfo());
        }
        macros.affixProvider = affixProvider;
        boolean useCurrency = (properties.getCurrency() == null && properties.getCurrencyPluralInfo() == null && properties.getCurrencyUsage() == null && !affixProvider.hasCurrencySign()) ? false : true;
        Currency currency = CustomSymbolCurrency.resolve(properties.getCurrency(), locale, decimalFormatSymbols);
        Currency.CurrencyUsage currencyUsage = properties.getCurrencyUsage();
        boolean explicitCurrencyUsage = currencyUsage != null;
        if (!explicitCurrencyUsage) {
            currencyUsage = Currency.CurrencyUsage.STANDARD;
        }
        if (useCurrency) {
            macros.unit = currency;
        }
        int maxInt2 = properties.getMaximumIntegerDigits();
        int minInt2 = properties.getMinimumIntegerDigits();
        int maxFrac2 = properties.getMaximumFractionDigits();
        int minFrac2 = properties.getMinimumFractionDigits();
        int minSig = properties.getMinimumSignificantDigits();
        int maxSig = properties.getMaximumSignificantDigits();
        BigDecimal roundingIncrement = properties.getRoundingIncrement();
        ULocale uLocale = locale;
        MathContext mathContext = RoundingUtils.getMathContextOrUnlimited(properties);
        AffixPatternProvider affixPatternProvider = affixProvider;
        boolean explicitMinMaxFrac = (minFrac2 == -1 && maxFrac2 == -1) ? false : true;
        boolean explicitMinMaxSig = (minSig == -1 && maxSig == -1) ? false : true;
        if (useCurrency) {
            if (minFrac2 == -1 && maxFrac2 == -1) {
                minFrac2 = currency.getDefaultFractionDigits(currencyUsage);
                maxFrac2 = currency.getDefaultFractionDigits(currencyUsage);
            } else if (minFrac2 == -1) {
                minFrac2 = Math.min(maxFrac2, currency.getDefaultFractionDigits(currencyUsage));
            } else if (maxFrac2 == -1) {
                maxFrac2 = Math.max(minFrac2, currency.getDefaultFractionDigits(currencyUsage));
            }
        }
        if (minInt2 != 0 || maxFrac2 == 0) {
            minFrac = minFrac2 < 0 ? 0 : minFrac2;
            maxFrac = maxFrac2 < 0 ? Integer.MAX_VALUE : maxFrac2 < minFrac ? minFrac : maxFrac2;
            minInt = (minInt2 > 0 && minInt2 <= 100) ? minInt2 : 1;
            if (maxInt2 >= 0) {
                if (maxInt2 < minInt) {
                    i2 = minInt;
                } else if (maxInt2 <= 100) {
                    i2 = maxInt2;
                }
                maxInt = i2;
            }
            i2 = -1;
            maxInt = i2;
        } else {
            minFrac = minFrac2 <= 0 ? 1 : minFrac2;
            maxFrac = maxFrac2 < 0 ? Integer.MAX_VALUE : maxFrac2 < minFrac ? minFrac : maxFrac2;
            minInt = 0;
            maxInt = (maxInt2 >= 0 && maxInt2 <= 100) ? maxInt2 : -1;
        }
        Rounder rounding = null;
        if (explicitCurrencyUsage) {
            rounding = Rounder.constructCurrency(currencyUsage).withCurrency(currency);
        } else if (roundingIncrement != null) {
            rounding = Rounder.constructIncrement(roundingIncrement);
        } else if (explicitMinMaxSig) {
            minSig = minSig < 1 ? 1 : minSig > 100 ? 100 : minSig;
            if (maxSig < 0) {
                i = 100;
            } else if (maxSig < minSig) {
                i = minSig;
            } else {
                i = 100;
                if (maxSig <= 100) {
                    i = maxSig;
                }
            }
            maxSig = i;
            rounding = Rounder.constructSignificant(minSig, maxSig);
        } else if (explicitMinMaxFrac) {
            rounding = Rounder.constructFraction(minFrac, maxFrac);
        } else if (useCurrency) {
            rounding = Rounder.constructCurrency(currencyUsage);
        }
        Rounder rounding2 = rounding;
        if (rounding2 != null) {
            rounding2 = rounding2.withMode(mathContext);
            macros.rounder = rounding2;
        }
        BigDecimal bigDecimal = roundingIncrement;
        macros.integerWidth = IntegerWidth.zeroFillTo(minInt).truncateAt(maxInt);
        int grouping1 = properties.getGroupingSize();
        int grouping2 = properties.getSecondaryGroupingSize();
        boolean z = useCurrency;
        int minGrouping = properties.getMinimumGroupingDigits();
        int grouping12 = (grouping1 <= 0 && grouping2 > 0) ? grouping2 : grouping1;
        Currency.CurrencyUsage currencyUsage2 = currencyUsage;
        byte b = (byte) grouping12;
        int i3 = grouping12;
        boolean z2 = explicitCurrencyUsage;
        int grouping13 = grouping2 > 0 ? grouping2 : grouping12;
        int i4 = grouping13;
        macros.grouper = Grouper.getInstance(b, (byte) grouping13, minGrouping == 2);
        if (properties.getFormatWidth() != -1) {
            int i5 = minGrouping;
            macros.padder = new Padder(properties.getPadString(), properties.getFormatWidth(), properties.getPadPosition());
        }
        if (properties.getDecimalSeparatorAlwaysShown()) {
            decimalSeparatorDisplay = NumberFormatter.DecimalSeparatorDisplay.ALWAYS;
        } else {
            decimalSeparatorDisplay = NumberFormatter.DecimalSeparatorDisplay.AUTO;
        }
        macros.decimal = decimalSeparatorDisplay;
        macros.sign = properties.getSignAlwaysShown() ? NumberFormatter.SignDisplay.ALWAYS : NumberFormatter.SignDisplay.AUTO;
        if (properties.getMinimumExponentDigits() != -1) {
            if (maxInt > 8) {
                int maxInt3 = minInt;
                macros.integerWidth = IntegerWidth.zeroFillTo(minInt).truncateAt(maxInt3);
                maxInt = maxInt3;
            } else if (maxInt > minInt && minInt > 1) {
                minInt = 1;
                macros.integerWidth = IntegerWidth.zeroFillTo(1).truncateAt(maxInt);
            }
            int engineering = maxInt < 0 ? -1 : maxInt;
            int maxInt4 = maxInt;
            macros.notation = new ScientificNotation(engineering, engineering == minInt, properties.getMinimumExponentDigits(), properties.getExponentSignAlwaysShown() ? NumberFormatter.SignDisplay.ALWAYS : NumberFormatter.SignDisplay.AUTO);
            if (macros.rounder instanceof FractionRounder) {
                int minInt_ = properties.getMinimumIntegerDigits();
                int minFrac_ = properties.getMinimumFractionDigits();
                int maxFrac_ = properties.getMaximumFractionDigits();
                if (minInt_ == 0 && maxFrac_ == 0) {
                    macros.rounder = Rounder.constructInfinite().withMode(mathContext);
                } else if (minInt_ == 0 && minFrac_ == 0) {
                    int i6 = engineering;
                    macros.rounder = Rounder.constructSignificant(1, maxFrac_ + 1).withMode(mathContext);
                } else {
                    macros.rounder = Rounder.constructSignificant(minInt_ + minFrac_, minInt_ + maxFrac_).withMode(mathContext);
                }
            }
            maxInt = maxInt4;
        }
        if (properties.getCompactStyle() != null) {
            if (properties.getCompactCustomData() != null) {
                macros.notation = new CompactNotation(properties.getCompactCustomData());
            } else if (properties.getCompactStyle() == CompactDecimalFormat.CompactStyle.LONG) {
                macros.notation = Notation.compactLong();
            } else {
                macros.notation = Notation.compactShort();
            }
            macros.affixProvider = null;
        }
        if (properties.getMagnitudeMultiplier() != 0) {
            macros.multiplier = new MultiplierImpl(properties.getMagnitudeMultiplier());
        } else if (properties.getMultiplier() != null) {
            macros.multiplier = new MultiplierImpl(properties.getMultiplier());
        }
        if (decimalFormatProperties != null) {
            decimalFormatProperties.setMathContext(mathContext);
            decimalFormatProperties.setRoundingMode(mathContext.getRoundingMode());
            decimalFormatProperties.setMinimumIntegerDigits(minInt);
            decimalFormatProperties.setMaximumIntegerDigits(maxInt == -1 ? Integer.MAX_VALUE : maxInt);
            if (rounding2 instanceof CurrencyRounder) {
                rounding_ = ((CurrencyRounder) rounding2).withCurrency(currency);
            } else {
                rounding_ = rounding2;
            }
            int minFrac_2 = minFrac;
            int maxFrac_2 = maxFrac;
            int minSig_ = minSig;
            int maxSig_2 = maxSig;
            MathContext mathContext2 = mathContext;
            if (rounding_ instanceof Rounder.FractionRounderImpl) {
                minFrac_2 = ((Rounder.FractionRounderImpl) rounding_).minFrac;
                maxFrac_2 = ((Rounder.FractionRounderImpl) rounding_).maxFrac;
            } else if ((rounding_ instanceof Rounder.IncrementRounderImpl) != 0) {
                BigDecimal increment_2 = ((Rounder.IncrementRounderImpl) rounding_).increment;
                minFrac_2 = increment_2.scale();
                maxFrac_2 = increment_2.scale();
                Rounder rounder = rounding_;
                increment_ = increment_2;
                maxSig_ = maxSig_2;
                decimalFormatProperties.setMinimumFractionDigits(minFrac_2);
                decimalFormatProperties.setMaximumFractionDigits(maxFrac_2);
                decimalFormatProperties.setMinimumSignificantDigits(minSig_);
                decimalFormatProperties.setMaximumSignificantDigits(maxSig_);
                decimalFormatProperties.setRoundingIncrement(increment_);
            } else if (rounding_ instanceof Rounder.SignificantRounderImpl) {
                minSig_ = ((Rounder.SignificantRounderImpl) rounding_).minSig;
                maxSig_ = ((Rounder.SignificantRounderImpl) rounding_).maxSig;
                Rounder rounder2 = rounding_;
                increment_ = null;
                decimalFormatProperties.setMinimumFractionDigits(minFrac_2);
                decimalFormatProperties.setMaximumFractionDigits(maxFrac_2);
                decimalFormatProperties.setMinimumSignificantDigits(minSig_);
                decimalFormatProperties.setMaximumSignificantDigits(maxSig_);
                decimalFormatProperties.setRoundingIncrement(increment_);
            }
            maxSig_ = maxSig_2;
            increment_ = null;
            decimalFormatProperties.setMinimumFractionDigits(minFrac_2);
            decimalFormatProperties.setMaximumFractionDigits(maxFrac_2);
            decimalFormatProperties.setMinimumSignificantDigits(minSig_);
            decimalFormatProperties.setMaximumSignificantDigits(maxSig_);
            decimalFormatProperties.setRoundingIncrement(increment_);
        }
        return macros;
    }
}
