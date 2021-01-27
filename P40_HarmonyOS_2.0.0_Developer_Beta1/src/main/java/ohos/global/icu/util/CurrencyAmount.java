package ohos.global.icu.util;

import java.util.Currency;

public class CurrencyAmount extends Measure {
    public CurrencyAmount(Number number, Currency currency) {
        super(number, currency);
    }

    public CurrencyAmount(double d, Currency currency) {
        super(new Double(d), currency);
    }

    public CurrencyAmount(Number number, Currency currency) {
        this(number, Currency.fromJavaCurrency(currency));
    }

    public CurrencyAmount(double d, Currency currency) {
        this(d, Currency.fromJavaCurrency(currency));
    }

    public Currency getCurrency() {
        return (Currency) getUnit();
    }
}
