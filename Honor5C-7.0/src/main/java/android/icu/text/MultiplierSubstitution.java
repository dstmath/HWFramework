package android.icu.text;

/* compiled from: NFSubstitution */
class MultiplierSubstitution extends NFSubstitution {
    double divisor;

    MultiplierSubstitution(int pos, double divisor, NFRuleSet ruleSet, String description) {
        super(pos, ruleSet, description);
        this.divisor = divisor;
        if (divisor == 0.0d) {
            throw new IllegalStateException("Substitution with divisor 0 " + description.substring(0, pos) + " | " + description.substring(pos));
        }
    }

    public void setDivisor(int radix, int exponent) {
        this.divisor = Math.pow((double) radix, (double) exponent);
        if (this.divisor == 0.0d) {
            throw new IllegalStateException("Substitution with divisor 0");
        }
    }

    public boolean equals(Object that) {
        return super.equals(that) && this.divisor == ((MultiplierSubstitution) that).divisor;
    }

    public long transformNumber(long number) {
        return (long) Math.floor(((double) number) / this.divisor);
    }

    public double transformNumber(double number) {
        if (this.ruleSet == null) {
            return number / this.divisor;
        }
        return Math.floor(number / this.divisor);
    }

    public double composeRuleValue(double newRuleValue, double oldRuleValue) {
        return this.divisor * newRuleValue;
    }

    public double calcUpperBound(double oldUpperBound) {
        return this.divisor;
    }

    char tokenChar() {
        return '<';
    }
}
