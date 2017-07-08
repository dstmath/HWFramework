package android.icu.text;

import java.text.ParsePosition;

/* compiled from: NFSubstitution */
class ModulusSubstitution extends NFSubstitution {
    double divisor;
    private final NFRule ruleToUse;

    ModulusSubstitution(int pos, double divisor, NFRule rulePredecessor, NFRuleSet ruleSet, String description) {
        super(pos, ruleSet, description);
        this.divisor = divisor;
        if (divisor == 0.0d) {
            throw new IllegalStateException("Substitution with bad divisor (" + divisor + ") " + description.substring(0, pos) + " | " + description.substring(pos));
        } else if (description.equals(">>>")) {
            this.ruleToUse = rulePredecessor;
        } else {
            this.ruleToUse = null;
        }
    }

    public void setDivisor(int radix, int exponent) {
        this.divisor = Math.pow((double) radix, (double) exponent);
        if (this.divisor == 0.0d) {
            throw new IllegalStateException("Substitution with bad divisor");
        }
    }

    public boolean equals(Object that) {
        boolean z = false;
        if (!super.equals(that)) {
            return false;
        }
        if (this.divisor == ((ModulusSubstitution) that).divisor) {
            z = true;
        }
        return z;
    }

    public void doSubstitution(long number, StringBuffer toInsertInto, int position, int recursionCount) {
        if (this.ruleToUse == null) {
            super.doSubstitution(number, toInsertInto, position, recursionCount);
            return;
        }
        this.ruleToUse.doFormat(transformNumber(number), toInsertInto, position + this.pos, recursionCount);
    }

    public void doSubstitution(double number, StringBuffer toInsertInto, int position, int recursionCount) {
        if (this.ruleToUse == null) {
            super.doSubstitution(number, toInsertInto, position, recursionCount);
            return;
        }
        this.ruleToUse.doFormat(transformNumber(number), toInsertInto, position + this.pos, recursionCount);
    }

    public long transformNumber(long number) {
        return (long) Math.floor(((double) number) % this.divisor);
    }

    public double transformNumber(double number) {
        return Math.floor(number % this.divisor);
    }

    public Number doParse(String text, ParsePosition parsePosition, double baseValue, double upperBound, boolean lenientParse) {
        if (this.ruleToUse == null) {
            return super.doParse(text, parsePosition, baseValue, upperBound, lenientParse);
        }
        Number tempResult = this.ruleToUse.doParse(text, parsePosition, false, upperBound);
        if (parsePosition.getIndex() == 0) {
            return tempResult;
        }
        double result = composeRuleValue(tempResult.doubleValue(), baseValue);
        if (result == ((double) ((long) result))) {
            return Long.valueOf((long) result);
        }
        return new Double(result);
    }

    public double composeRuleValue(double newRuleValue, double oldRuleValue) {
        return (oldRuleValue - (oldRuleValue % this.divisor)) + newRuleValue;
    }

    public double calcUpperBound(double oldUpperBound) {
        return this.divisor;
    }

    public boolean isModulusSubstitution() {
        return true;
    }

    char tokenChar() {
        return '>';
    }
}
