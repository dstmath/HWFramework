package android.icu.impl.number;

import android.icu.text.DecimalFormatSymbols;
import android.icu.util.Currency;
import android.icu.util.ULocale;

public class CustomSymbolCurrency extends Currency {
    private static final long serialVersionUID = 2497493016770137670L;
    private String symbol1;
    private String symbol2;

    public static Currency resolve(Currency currency, ULocale locale, DecimalFormatSymbols symbols) {
        if (currency == null) {
            currency = symbols.getCurrency();
        }
        String currency1Sym = symbols.getCurrencySymbol();
        String currency2Sym = symbols.getInternationalCurrencySymbol();
        if (currency == null) {
            return new CustomSymbolCurrency("XXX", currency1Sym, currency2Sym);
        }
        if (!currency.equals(symbols.getCurrency())) {
            return currency;
        }
        String currency1 = currency.getName(symbols.getULocale(), 0, (boolean[]) null);
        String currency2 = currency.getCurrencyCode();
        if (!currency1.equals(currency1Sym) || !currency2.equals(currency2Sym)) {
            return new CustomSymbolCurrency(currency2, currency1Sym, currency2Sym);
        }
        return currency;
    }

    public CustomSymbolCurrency(String isoCode, String currency1Sym, String currency2Sym) {
        super(isoCode);
        this.symbol1 = currency1Sym;
        this.symbol2 = currency2Sym;
    }

    public String getName(ULocale locale, int nameStyle, boolean[] isChoiceFormat) {
        if (nameStyle == 0) {
            return this.symbol1;
        }
        return super.getName(locale, nameStyle, isChoiceFormat);
    }

    public String getName(ULocale locale, int nameStyle, String pluralCount, boolean[] isChoiceFormat) {
        if (nameStyle != 2 || !this.subType.equals("XXX")) {
            return super.getName(locale, nameStyle, pluralCount, isChoiceFormat);
        }
        return this.symbol1;
    }

    public String getCurrencyCode() {
        return this.symbol2;
    }

    public int hashCode() {
        return (super.hashCode() ^ this.symbol1.hashCode()) ^ this.symbol2.hashCode();
    }

    public boolean equals(Object other) {
        return super.equals(other) && ((CustomSymbolCurrency) other).symbol1.equals(this.symbol1) && ((CustomSymbolCurrency) other).symbol2.equals(this.symbol2);
    }
}
