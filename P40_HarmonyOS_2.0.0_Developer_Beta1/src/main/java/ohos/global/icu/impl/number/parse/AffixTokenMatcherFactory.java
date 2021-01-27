package ohos.global.icu.impl.number.parse;

import ohos.global.icu.text.DecimalFormatSymbols;
import ohos.global.icu.util.Currency;
import ohos.global.icu.util.ULocale;

public class AffixTokenMatcherFactory {
    public Currency currency;
    public IgnorablesMatcher ignorables;
    public ULocale locale;
    public int parseFlags;
    public DecimalFormatSymbols symbols;

    public MinusSignMatcher minusSign() {
        return MinusSignMatcher.getInstance(this.symbols, true);
    }

    public PlusSignMatcher plusSign() {
        return PlusSignMatcher.getInstance(this.symbols, true);
    }

    public PercentMatcher percent() {
        return PercentMatcher.getInstance(this.symbols);
    }

    public PermilleMatcher permille() {
        return PermilleMatcher.getInstance(this.symbols);
    }

    public CombinedCurrencyMatcher currency() {
        return CombinedCurrencyMatcher.getInstance(this.currency, this.symbols, this.parseFlags);
    }

    public IgnorablesMatcher ignorables() {
        return this.ignorables;
    }
}
