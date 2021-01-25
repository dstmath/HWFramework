package ohos.global.icu.impl.number;

import java.text.Format;
import ohos.global.icu.impl.FormattedStringBuilder;
import ohos.global.icu.impl.StandardPlural;
import ohos.global.icu.impl.number.AffixUtils;
import ohos.global.icu.impl.number.Modifier;
import ohos.global.icu.number.NumberFormatter;
import ohos.global.icu.text.DecimalFormatSymbols;
import ohos.global.icu.text.NumberFormat;
import ohos.global.icu.text.PluralRules;
import ohos.global.icu.util.Currency;

public class MutablePatternModifier implements Modifier, AffixUtils.SymbolProvider, MicroPropsGenerator {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    Currency currency;
    StringBuilder currentAffix;
    NumberFormat.Field field;
    final boolean isStrong;
    MicroPropsGenerator parent;
    AffixPatternProvider patternInfo;
    boolean perMilleReplacesPercent;
    StandardPlural plural;
    PluralRules rules;
    NumberFormatter.SignDisplay signDisplay;
    int signum;
    DecimalFormatSymbols symbols;
    NumberFormatter.UnitWidth unitWidth;

    @Override // ohos.global.icu.impl.number.Modifier
    public boolean containsField(Format.Field field2) {
        return false;
    }

    @Override // ohos.global.icu.impl.number.Modifier
    public Modifier.Parameters getParameters() {
        return null;
    }

    @Override // ohos.global.icu.impl.number.Modifier
    public boolean semanticallyEquivalent(Modifier modifier) {
        return false;
    }

    public MutablePatternModifier(boolean z) {
        this.isStrong = z;
    }

    public void setPatternInfo(AffixPatternProvider affixPatternProvider, NumberFormat.Field field2) {
        this.patternInfo = affixPatternProvider;
        this.field = field2;
    }

    public void setPatternAttributes(NumberFormatter.SignDisplay signDisplay2, boolean z) {
        this.signDisplay = signDisplay2;
        this.perMilleReplacesPercent = z;
    }

    public void setSymbols(DecimalFormatSymbols decimalFormatSymbols, Currency currency2, NumberFormatter.UnitWidth unitWidth2, PluralRules pluralRules) {
        this.symbols = decimalFormatSymbols;
        this.currency = currency2;
        this.unitWidth = unitWidth2;
        this.rules = pluralRules;
    }

    public void setNumberProperties(int i, StandardPlural standardPlural) {
        this.signum = i;
        this.plural = standardPlural;
    }

    public boolean needsPlurals() {
        return this.patternInfo.containsSymbolType(-7);
    }

    public ImmutablePatternModifier createImmutable() {
        return createImmutableAndChain(null);
    }

    public ImmutablePatternModifier createImmutableAndChain(MicroPropsGenerator microPropsGenerator) {
        FormattedStringBuilder formattedStringBuilder = new FormattedStringBuilder();
        FormattedStringBuilder formattedStringBuilder2 = new FormattedStringBuilder();
        if (needsPlurals()) {
            AdoptingModifierStore adoptingModifierStore = new AdoptingModifierStore();
            for (StandardPlural standardPlural : StandardPlural.VALUES) {
                setNumberProperties(1, standardPlural);
                adoptingModifierStore.setModifier(1, standardPlural, createConstantModifier(formattedStringBuilder, formattedStringBuilder2));
                setNumberProperties(0, standardPlural);
                adoptingModifierStore.setModifier(0, standardPlural, createConstantModifier(formattedStringBuilder, formattedStringBuilder2));
                setNumberProperties(-1, standardPlural);
                adoptingModifierStore.setModifier(-1, standardPlural, createConstantModifier(formattedStringBuilder, formattedStringBuilder2));
            }
            adoptingModifierStore.freeze();
            return new ImmutablePatternModifier(adoptingModifierStore, this.rules, microPropsGenerator);
        }
        setNumberProperties(1, null);
        ConstantMultiFieldModifier createConstantModifier = createConstantModifier(formattedStringBuilder, formattedStringBuilder2);
        setNumberProperties(0, null);
        ConstantMultiFieldModifier createConstantModifier2 = createConstantModifier(formattedStringBuilder, formattedStringBuilder2);
        setNumberProperties(-1, null);
        return new ImmutablePatternModifier(new AdoptingModifierStore(createConstantModifier, createConstantModifier2, createConstantModifier(formattedStringBuilder, formattedStringBuilder2)), null, microPropsGenerator);
    }

    private ConstantMultiFieldModifier createConstantModifier(FormattedStringBuilder formattedStringBuilder, FormattedStringBuilder formattedStringBuilder2) {
        insertPrefix(formattedStringBuilder.clear(), 0);
        insertSuffix(formattedStringBuilder2.clear(), 0);
        if (this.patternInfo.hasCurrencySign()) {
            return new CurrencySpacingEnabledModifier(formattedStringBuilder, formattedStringBuilder2, !this.patternInfo.hasBody(), this.isStrong, this.symbols);
        }
        return new ConstantMultiFieldModifier(formattedStringBuilder, formattedStringBuilder2, !this.patternInfo.hasBody(), this.isStrong);
    }

    public static class ImmutablePatternModifier implements MicroPropsGenerator {
        final MicroPropsGenerator parent;
        final AdoptingModifierStore pm;
        final PluralRules rules;

        ImmutablePatternModifier(AdoptingModifierStore adoptingModifierStore, PluralRules pluralRules, MicroPropsGenerator microPropsGenerator) {
            this.pm = adoptingModifierStore;
            this.rules = pluralRules;
            this.parent = microPropsGenerator;
        }

        @Override // ohos.global.icu.impl.number.MicroPropsGenerator
        public MicroProps processQuantity(DecimalQuantity decimalQuantity) {
            MicroProps processQuantity = this.parent.processQuantity(decimalQuantity);
            applyToMicros(processQuantity, decimalQuantity);
            return processQuantity;
        }

        public void applyToMicros(MicroProps microProps, DecimalQuantity decimalQuantity) {
            if (this.rules == null) {
                microProps.modMiddle = this.pm.getModifierWithoutPlural(decimalQuantity.signum());
                return;
            }
            microProps.modMiddle = this.pm.getModifier(decimalQuantity.signum(), RoundingUtils.getPluralSafe(microProps.rounder, this.rules, decimalQuantity));
        }
    }

    public MicroPropsGenerator addToChain(MicroPropsGenerator microPropsGenerator) {
        this.parent = microPropsGenerator;
        return this;
    }

    @Override // ohos.global.icu.impl.number.MicroPropsGenerator
    public MicroProps processQuantity(DecimalQuantity decimalQuantity) {
        MicroProps processQuantity = this.parent.processQuantity(decimalQuantity);
        if (needsPlurals()) {
            setNumberProperties(decimalQuantity.signum(), RoundingUtils.getPluralSafe(processQuantity.rounder, this.rules, decimalQuantity));
        } else {
            setNumberProperties(decimalQuantity.signum(), null);
        }
        processQuantity.modMiddle = this;
        return processQuantity;
    }

    @Override // ohos.global.icu.impl.number.Modifier
    public int apply(FormattedStringBuilder formattedStringBuilder, int i, int i2) {
        int insertPrefix = insertPrefix(formattedStringBuilder, i);
        int i3 = i2 + insertPrefix;
        int insertSuffix = insertSuffix(formattedStringBuilder, i3);
        int splice = !this.patternInfo.hasBody() ? formattedStringBuilder.splice(i + insertPrefix, i3, "", 0, 0, null) : 0;
        CurrencySpacingEnabledModifier.applyCurrencySpacing(formattedStringBuilder, i, insertPrefix, i3 + splice, insertSuffix, this.symbols);
        return insertPrefix + splice + insertSuffix;
    }

    @Override // ohos.global.icu.impl.number.Modifier
    public int getPrefixLength() {
        prepareAffix(true);
        return AffixUtils.unescapedCount(this.currentAffix, true, this);
    }

    @Override // ohos.global.icu.impl.number.Modifier
    public int getCodePointCount() {
        prepareAffix(true);
        int unescapedCount = AffixUtils.unescapedCount(this.currentAffix, false, this);
        prepareAffix(false);
        return unescapedCount + AffixUtils.unescapedCount(this.currentAffix, false, this);
    }

    @Override // ohos.global.icu.impl.number.Modifier
    public boolean isStrong() {
        return this.isStrong;
    }

    private int insertPrefix(FormattedStringBuilder formattedStringBuilder, int i) {
        prepareAffix(true);
        return AffixUtils.unescape(this.currentAffix, formattedStringBuilder, i, this, this.field);
    }

    private int insertSuffix(FormattedStringBuilder formattedStringBuilder, int i) {
        prepareAffix(false);
        return AffixUtils.unescape(this.currentAffix, formattedStringBuilder, i, this, this.field);
    }

    private void prepareAffix(boolean z) {
        if (this.currentAffix == null) {
            this.currentAffix = new StringBuilder();
        }
        PatternStringUtils.patternInfoToStringBuilder(this.patternInfo, z, this.signum, this.signDisplay, this.plural, this.perMilleReplacesPercent, this.currentAffix);
    }

    @Override // ohos.global.icu.impl.number.AffixUtils.SymbolProvider
    public CharSequence getSymbol(int i) {
        int i2 = 3;
        switch (i) {
            case AffixUtils.TYPE_CURRENCY_QUINT /* -9 */:
                return this.currency.getName(this.symbols.getULocale(), 3, (boolean[]) null);
            case AffixUtils.TYPE_CURRENCY_QUAD /* -8 */:
                return "�";
            case -7:
                return this.currency.getName(this.symbols.getULocale(), 2, this.plural.getKeyword(), (boolean[]) null);
            case -6:
                return this.currency.getCurrencyCode();
            case -5:
                if (this.unitWidth == NumberFormatter.UnitWidth.ISO_CODE) {
                    return this.currency.getCurrencyCode();
                }
                if (this.unitWidth == NumberFormatter.UnitWidth.HIDDEN) {
                    return "";
                }
                if (this.unitWidth != NumberFormatter.UnitWidth.NARROW) {
                    i2 = 0;
                }
                return this.currency.getName(this.symbols.getULocale(), i2, (boolean[]) null);
            case -4:
                return this.symbols.getPerMillString();
            case -3:
                return this.symbols.getPercentString();
            case -2:
                return this.symbols.getPlusSignString();
            case -1:
                return this.symbols.getMinusSignString();
            default:
                throw new AssertionError();
        }
    }
}
