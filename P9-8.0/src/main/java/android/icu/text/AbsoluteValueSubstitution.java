package android.icu.text;

/* compiled from: NFSubstitution */
class AbsoluteValueSubstitution extends NFSubstitution {
    AbsoluteValueSubstitution(int pos, NFRuleSet ruleSet, String description) {
        super(pos, ruleSet, description);
    }

    public long transformNumber(long number) {
        return Math.abs(number);
    }

    public double transformNumber(double number) {
        return Math.abs(number);
    }

    public double composeRuleValue(double newRuleValue, double oldRuleValue) {
        return -newRuleValue;
    }

    public double calcUpperBound(double oldUpperBound) {
        return Double.MAX_VALUE;
    }

    char tokenChar() {
        return '>';
    }
}
