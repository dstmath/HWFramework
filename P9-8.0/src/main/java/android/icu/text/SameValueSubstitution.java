package android.icu.text;

/* compiled from: NFSubstitution */
class SameValueSubstitution extends NFSubstitution {
    SameValueSubstitution(int pos, NFRuleSet ruleSet, String description) {
        super(pos, ruleSet, description);
        if (description.equals("==")) {
            throw new IllegalArgumentException("== is not a legal token");
        }
    }

    public long transformNumber(long number) {
        return number;
    }

    public double transformNumber(double number) {
        return number;
    }

    public double composeRuleValue(double newRuleValue, double oldRuleValue) {
        return newRuleValue;
    }

    public double calcUpperBound(double oldUpperBound) {
        return oldUpperBound;
    }

    char tokenChar() {
        return '=';
    }
}
