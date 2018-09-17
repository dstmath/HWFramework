package android.icu.util;

public class CurrencyAmount extends Measure {
    public CurrencyAmount(Number number, Currency currency) {
        super(number, currency);
    }

    public CurrencyAmount(double number, Currency currency) {
        super(new Double(number), currency);
    }

    public Currency getCurrency() {
        return (Currency) getUnit();
    }
}
