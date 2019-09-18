package android.icu.number;

public abstract class FractionRounder extends Rounder {
    FractionRounder() {
    }

    public Rounder withMinDigits(int minSignificantDigits) {
        if (minSignificantDigits > 0 && minSignificantDigits <= 100) {
            return constructFractionSignificant(this, minSignificantDigits, -1);
        }
        throw new IllegalArgumentException("Significant digits must be between 0 and 100");
    }

    public Rounder withMaxDigits(int maxSignificantDigits) {
        if (maxSignificantDigits > 0 && maxSignificantDigits <= 100) {
            return constructFractionSignificant(this, -1, maxSignificantDigits);
        }
        throw new IllegalArgumentException("Significant digits must be between 0 and 100");
    }
}
