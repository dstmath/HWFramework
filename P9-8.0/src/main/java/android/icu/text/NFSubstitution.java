package android.icu.text;

import java.text.ParsePosition;

abstract class NFSubstitution {
    static final /* synthetic */ boolean -assertionsDisabled = (NFSubstitution.class.desiredAssertionStatus() ^ 1);
    final DecimalFormat numberFormat;
    final int pos;
    final NFRuleSet ruleSet;

    public abstract double calcUpperBound(double d);

    public abstract double composeRuleValue(double d, double d2);

    abstract char tokenChar();

    public abstract double transformNumber(double d);

    public abstract long transformNumber(long j);

    public static NFSubstitution makeSubstitution(int pos, NFRule rule, NFRule rulePredecessor, NFRuleSet ruleSet, RuleBasedNumberFormat formatter, String description) {
        if (description.length() == 0) {
            return null;
        }
        switch (description.charAt(0)) {
            case '<':
                if (rule.getBaseValue() == -1) {
                    throw new IllegalArgumentException("<< not allowed in negative-number rule");
                } else if (rule.getBaseValue() == -2 || rule.getBaseValue() == -3 || rule.getBaseValue() == -4) {
                    return new IntegralPartSubstitution(pos, ruleSet, description);
                } else {
                    if (!ruleSet.isFractionSet()) {
                        return new MultiplierSubstitution(pos, rule, ruleSet, description);
                    }
                    return new NumeratorSubstitution(pos, (double) rule.getBaseValue(), formatter.getDefaultRuleSet(), description);
                }
            case '=':
                return new SameValueSubstitution(pos, ruleSet, description);
            case '>':
                if (rule.getBaseValue() == -1) {
                    return new AbsoluteValueSubstitution(pos, ruleSet, description);
                }
                if (rule.getBaseValue() == -2 || rule.getBaseValue() == -3 || rule.getBaseValue() == -4) {
                    return new FractionalPartSubstitution(pos, ruleSet, description);
                }
                if (!ruleSet.isFractionSet()) {
                    return new ModulusSubstitution(pos, rule, rulePredecessor, ruleSet, description);
                }
                throw new IllegalArgumentException(">> not allowed in fraction rule set");
            default:
                throw new IllegalArgumentException("Illegal substitution character");
        }
    }

    NFSubstitution(int pos, NFRuleSet ruleSet, String description) {
        this.pos = pos;
        int descriptionLen = description.length();
        if (descriptionLen >= 2 && description.charAt(0) == description.charAt(descriptionLen - 1)) {
            description = description.substring(1, descriptionLen - 1);
        } else if (descriptionLen != 0) {
            throw new IllegalArgumentException("Illegal substitution syntax");
        }
        if (description.length() == 0) {
            this.ruleSet = ruleSet;
            this.numberFormat = null;
        } else if (description.charAt(0) == '%') {
            this.ruleSet = ruleSet.owner.findRuleSet(description);
            this.numberFormat = null;
        } else if (description.charAt(0) == '#' || description.charAt(0) == '0') {
            this.ruleSet = null;
            this.numberFormat = (DecimalFormat) ruleSet.owner.getDecimalFormat().clone();
            this.numberFormat.applyPattern(description);
        } else if (description.charAt(0) == '>') {
            this.ruleSet = ruleSet;
            this.numberFormat = null;
        } else {
            throw new IllegalArgumentException("Illegal substitution syntax");
        }
    }

    public void setDivisor(int radix, short exponent) {
    }

    public boolean equals(Object that) {
        boolean z = true;
        if (that == null) {
            return false;
        }
        if (this == that) {
            return true;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        NFSubstitution that2 = (NFSubstitution) that;
        if (this.pos != that2.pos || (this.ruleSet == null && that2.ruleSet != null)) {
            z = false;
        } else if (this.numberFormat != null) {
            z = this.numberFormat.equals(that2.numberFormat);
        } else if (that2.numberFormat != null) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        if (-assertionsDisabled) {
            return 42;
        }
        throw new AssertionError("hashCode not designed");
    }

    public String toString() {
        if (this.ruleSet != null) {
            return tokenChar() + this.ruleSet.getName() + tokenChar();
        }
        return tokenChar() + this.numberFormat.toPattern() + tokenChar();
    }

    public void doSubstitution(long number, StringBuilder toInsertInto, int position, int recursionCount) {
        long numberToFormat = transformNumber(number);
        if (this.ruleSet != null) {
            this.ruleSet.format(numberToFormat, toInsertInto, position + this.pos, recursionCount);
            return;
        }
        toInsertInto.insert(this.pos + position, this.numberFormat.format(numberToFormat));
    }

    public void doSubstitution(double number, StringBuilder toInsertInto, int position, int recursionCount) {
        double numberToFormat = transformNumber(number);
        if (Double.isInfinite(numberToFormat)) {
            this.ruleSet.findRule(Double.POSITIVE_INFINITY).doFormat(numberToFormat, toInsertInto, position + this.pos, recursionCount);
            return;
        }
        if (numberToFormat == Math.floor(numberToFormat) && this.ruleSet != null) {
            this.ruleSet.format((long) numberToFormat, toInsertInto, position + this.pos, recursionCount);
        } else if (this.ruleSet != null) {
            this.ruleSet.format(numberToFormat, toInsertInto, position + this.pos, recursionCount);
        } else {
            toInsertInto.insert(this.pos + position, this.numberFormat.format(numberToFormat));
        }
    }

    public Number doParse(String text, ParsePosition parsePosition, double baseValue, double upperBound, boolean lenientParse) {
        Number tempResult;
        upperBound = calcUpperBound(upperBound);
        if (this.ruleSet != null) {
            tempResult = this.ruleSet.parse(text, parsePosition, upperBound);
            if (lenientParse && (this.ruleSet.isFractionSet() ^ 1) != 0 && parsePosition.getIndex() == 0) {
                tempResult = this.ruleSet.owner.getDecimalFormat().parse(text, parsePosition);
            }
        } else {
            tempResult = this.numberFormat.parse(text, parsePosition);
        }
        if (parsePosition.getIndex() == 0) {
            return tempResult;
        }
        double result = composeRuleValue(tempResult.doubleValue(), baseValue);
        if (result == ((double) ((long) result))) {
            return Long.valueOf((long) result);
        }
        return new Double(result);
    }

    public final int getPos() {
        return this.pos;
    }

    public boolean isModulusSubstitution() {
        return false;
    }

    public void setDecimalFormatSymbols(DecimalFormatSymbols newSymbols) {
        if (this.numberFormat != null) {
            this.numberFormat.setDecimalFormatSymbols(newSymbols);
        }
    }
}
