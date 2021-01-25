package ohos.global.icu.number;

import ohos.global.icu.util.Currency;

public abstract class CurrencyPrecision extends Precision {
    CurrencyPrecision() {
    }

    public Precision withCurrency(Currency currency) {
        if (currency != null) {
            return constructFromCurrency(this, currency);
        }
        throw new IllegalArgumentException("Currency must not be null");
    }
}
