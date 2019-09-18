package android.icu.util;

import java.util.Currency;

public class CurrencyAmount extends Measure {
    public CurrencyAmount(Number number, Currency currency) {
        super(number, currency);
    }

    public CurrencyAmount(double number, Currency currency) {
        super(new Double(number), currency);
    }

    public CurrencyAmount(Number number, Currency currency) {
        this(number, Currency.fromJavaCurrency(currency));
    }

    public CurrencyAmount(double number, Currency currency) {
        this(number, Currency.fromJavaCurrency(currency));
    }

    public Currency getCurrency() {
        return (Currency) getUnit();
    }
}
