package ohos.global.icu.number;

public abstract class FractionPrecision extends Precision {
    FractionPrecision() {
    }

    public Precision withMinDigits(int i) {
        if (i >= 1 && i <= 999) {
            return constructFractionSignificant(this, i, -1);
        }
        throw new IllegalArgumentException("Significant digits must be between 1 and 999 (inclusive)");
    }

    public Precision withMaxDigits(int i) {
        if (i >= 1 && i <= 999) {
            return constructFractionSignificant(this, -1, i);
        }
        throw new IllegalArgumentException("Significant digits must be between 1 and 999 (inclusive)");
    }
}
