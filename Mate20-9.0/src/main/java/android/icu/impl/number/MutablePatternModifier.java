package android.icu.impl.number;

import android.icu.impl.StandardPlural;
import android.icu.impl.locale.XLocaleDistance;
import android.icu.impl.number.AffixUtils;
import android.icu.number.NumberFormatter;
import android.icu.text.DecimalFormatSymbols;
import android.icu.text.PluralRules;
import android.icu.util.Currency;

public class MutablePatternModifier implements Modifier, AffixUtils.SymbolProvider, CharSequence, MicroPropsGenerator {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    Currency currency;
    int flags;
    boolean inCharSequenceMode;
    boolean isNegative;
    final boolean isStrong;
    int length;
    MicroPropsGenerator parent;
    AffixPatternProvider patternInfo;
    boolean perMilleReplacesPercent;
    StandardPlural plural;
    boolean plusReplacesMinusSign;
    boolean prependSign;
    PluralRules rules;
    NumberFormatter.SignDisplay signDisplay;
    DecimalFormatSymbols symbols;
    NumberFormatter.UnitWidth unitWidth;

    public static class ImmutablePatternModifier implements MicroPropsGenerator {
        final MicroPropsGenerator parent;
        final ParameterizedModifier pm;
        final PluralRules rules;

        ImmutablePatternModifier(ParameterizedModifier pm2, PluralRules rules2, MicroPropsGenerator parent2) {
            this.pm = pm2;
            this.rules = rules2;
            this.parent = parent2;
        }

        public MicroProps processQuantity(DecimalQuantity quantity) {
            MicroProps micros = this.parent.processQuantity(quantity);
            applyToMicros(micros, quantity);
            return micros;
        }

        public void applyToMicros(MicroProps micros, DecimalQuantity quantity) {
            if (this.rules == null) {
                micros.modMiddle = this.pm.getModifier(quantity.isNegative());
                return;
            }
            DecimalQuantity copy = quantity.createCopy();
            copy.roundToInfinity();
            micros.modMiddle = this.pm.getModifier(quantity.isNegative(), copy.getStandardPlural(this.rules));
        }
    }

    public MutablePatternModifier(boolean isStrong2) {
        this.isStrong = isStrong2;
    }

    public void setPatternInfo(AffixPatternProvider patternInfo2) {
        this.patternInfo = patternInfo2;
    }

    public void setPatternAttributes(NumberFormatter.SignDisplay signDisplay2, boolean perMille) {
        this.signDisplay = signDisplay2;
        this.perMilleReplacesPercent = perMille;
    }

    public void setSymbols(DecimalFormatSymbols symbols2, Currency currency2, NumberFormatter.UnitWidth unitWidth2, PluralRules rules2) {
        this.symbols = symbols2;
        this.currency = currency2;
        this.unitWidth = unitWidth2;
        this.rules = rules2;
    }

    public void setNumberProperties(boolean isNegative2, StandardPlural plural2) {
        this.isNegative = isNegative2;
        this.plural = plural2;
    }

    public boolean needsPlurals() {
        return this.patternInfo.containsSymbolType(-7);
    }

    public ImmutablePatternModifier createImmutable() {
        return createImmutableAndChain(null);
    }

    public ImmutablePatternModifier createImmutableAndChain(MicroPropsGenerator parent2) {
        NumberStringBuilder a = new NumberStringBuilder();
        NumberStringBuilder b = new NumberStringBuilder();
        if (needsPlurals()) {
            ParameterizedModifier pm = new ParameterizedModifier();
            for (StandardPlural plural2 : StandardPlural.VALUES) {
                setNumberProperties(false, plural2);
                pm.setModifier(false, plural2, createConstantModifier(a, b));
                setNumberProperties(true, plural2);
                pm.setModifier(true, plural2, createConstantModifier(a, b));
            }
            pm.freeze();
            return new ImmutablePatternModifier(pm, this.rules, parent2);
        }
        setNumberProperties(false, null);
        Modifier positive = createConstantModifier(a, b);
        setNumberProperties(true, null);
        return new ImmutablePatternModifier(new ParameterizedModifier(positive, createConstantModifier(a, b)), null, parent2);
    }

    private ConstantMultiFieldModifier createConstantModifier(NumberStringBuilder a, NumberStringBuilder b) {
        insertPrefix(a.clear(), 0);
        insertSuffix(b.clear(), 0);
        if (this.patternInfo.hasCurrencySign()) {
            return new CurrencySpacingEnabledModifier(a, b, this.isStrong, this.symbols);
        }
        return new ConstantMultiFieldModifier(a, b, this.isStrong);
    }

    public MicroPropsGenerator addToChain(MicroPropsGenerator parent2) {
        this.parent = parent2;
        return this;
    }

    public MicroProps processQuantity(DecimalQuantity fq) {
        MicroProps micros = this.parent.processQuantity(fq);
        if (needsPlurals()) {
            DecimalQuantity copy = fq.createCopy();
            micros.rounding.apply(copy);
            setNumberProperties(fq.isNegative(), copy.getStandardPlural(this.rules));
        } else {
            setNumberProperties(fq.isNegative(), null);
        }
        micros.modMiddle = this;
        return micros;
    }

    public int apply(NumberStringBuilder output, int leftIndex, int rightIndex) {
        int prefixLen = insertPrefix(output, leftIndex);
        int suffixLen = insertSuffix(output, rightIndex + prefixLen);
        CurrencySpacingEnabledModifier.applyCurrencySpacing(output, leftIndex, prefixLen, rightIndex + prefixLen, suffixLen, this.symbols);
        return prefixLen + suffixLen;
    }

    public int getPrefixLength() {
        enterCharSequenceMode(true);
        int result = AffixUtils.unescapedCodePointCount(this, this);
        exitCharSequenceMode();
        return result;
    }

    public int getCodePointCount() {
        enterCharSequenceMode(true);
        int result = AffixUtils.unescapedCodePointCount(this, this);
        exitCharSequenceMode();
        enterCharSequenceMode(false);
        int result2 = result + AffixUtils.unescapedCodePointCount(this, this);
        exitCharSequenceMode();
        return result2;
    }

    public boolean isStrong() {
        return this.isStrong;
    }

    private int insertPrefix(NumberStringBuilder sb, int position) {
        enterCharSequenceMode(true);
        int length2 = AffixUtils.unescape(this, sb, position, this);
        exitCharSequenceMode();
        return length2;
    }

    private int insertSuffix(NumberStringBuilder sb, int position) {
        enterCharSequenceMode(false);
        int length2 = AffixUtils.unescape(this, sb, position, this);
        exitCharSequenceMode();
        return length2;
    }

    public CharSequence getSymbol(int type) {
        switch (type) {
            case AffixUtils.TYPE_CURRENCY_QUINT:
                return this.currency.getName(this.symbols.getULocale(), 3, (boolean[]) null);
            case AffixUtils.TYPE_CURRENCY_QUAD:
                return XLocaleDistance.ANY;
            case AffixUtils.TYPE_CURRENCY_TRIPLE:
                return this.currency.getName(this.symbols.getULocale(), 2, this.plural.getKeyword(), (boolean[]) null);
            case AffixUtils.TYPE_CURRENCY_DOUBLE:
                return this.currency.getCurrencyCode();
            case AffixUtils.TYPE_CURRENCY_SINGLE:
                if (this.unitWidth == NumberFormatter.UnitWidth.ISO_CODE) {
                    return this.currency.getCurrencyCode();
                }
                if (this.unitWidth == NumberFormatter.UnitWidth.HIDDEN) {
                    return "";
                }
                if (this.unitWidth == NumberFormatter.UnitWidth.NARROW) {
                    return this.currency.getName(this.symbols.getULocale(), 3, (boolean[]) null);
                }
                return this.currency.getName(this.symbols.getULocale(), 0, (boolean[]) null);
            case AffixUtils.TYPE_PERMILLE:
                return this.symbols.getPerMillString();
            case AffixUtils.TYPE_PERCENT:
                return this.symbols.getPercentString();
            case -2:
                return this.symbols.getPlusSignString();
            case -1:
                return this.symbols.getMinusSignString();
            default:
                throw new AssertionError();
        }
    }

    private void enterCharSequenceMode(boolean isPrefix) {
        boolean z = true;
        this.inCharSequenceMode = true;
        this.plusReplacesMinusSign = !this.isNegative && (this.signDisplay == NumberFormatter.SignDisplay.ALWAYS || this.signDisplay == NumberFormatter.SignDisplay.ACCOUNTING_ALWAYS) && !this.patternInfo.positiveHasPlusSign();
        boolean useNegativeAffixPattern = this.patternInfo.hasNegativeSubpattern() && (this.isNegative || (this.patternInfo.negativeHasMinusSign() && this.plusReplacesMinusSign));
        this.flags = 0;
        if (useNegativeAffixPattern) {
            this.flags |= 512;
        }
        if (isPrefix) {
            this.flags |= 256;
        }
        if (this.plural != null) {
            this.flags |= this.plural.ordinal();
        }
        if (!isPrefix || useNegativeAffixPattern) {
            this.prependSign = false;
        } else if (this.isNegative) {
            if (this.signDisplay == NumberFormatter.SignDisplay.NEVER) {
                z = false;
            }
            this.prependSign = z;
        } else {
            this.prependSign = this.plusReplacesMinusSign;
        }
        this.length = this.patternInfo.length(this.flags) + (this.prependSign ? 1 : 0);
    }

    private void exitCharSequenceMode() {
        this.inCharSequenceMode = false;
    }

    public int length() {
        return this.length;
    }

    public char charAt(int index) {
        char candidate;
        if (this.prependSign && index == 0) {
            candidate = '-';
        } else if (this.prependSign != 0) {
            candidate = this.patternInfo.charAt(this.flags, index - 1);
        } else {
            candidate = this.patternInfo.charAt(this.flags, index);
        }
        if (this.plusReplacesMinusSign && candidate == '-') {
            return '+';
        }
        if (!this.perMilleReplacesPercent || candidate != '%') {
            return candidate;
        }
        return 8240;
    }

    public CharSequence subSequence(int start, int end) {
        throw new AssertionError();
    }
}
