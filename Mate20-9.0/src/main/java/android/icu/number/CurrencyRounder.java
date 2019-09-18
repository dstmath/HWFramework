package android.icu.number;

import android.icu.util.Currency;

public abstract class CurrencyRounder extends Rounder {
    CurrencyRounder() {
    }

    public Rounder withCurrency(Currency currency) {
        if (currency != null) {
            return constructFromCurrency(this, currency);
        }
        throw new IllegalArgumentException("Currency must not be null");
    }
}
