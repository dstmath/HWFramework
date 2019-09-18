package android.icu.text;

import java.text.ParsePosition;

abstract class NFSubstitution {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private static final long MAX_INT64_IN_DOUBLE = 9007199254740991L;
    final DecimalFormat numberFormat;
    final int pos;
    final NFRuleSet ruleSet;

    public abstract double calcUpperBound(double d);

    public abstract double composeRuleValue(double d, double d2);

    /* access modifiers changed from: package-private */
    public abstract char tokenChar();

    public abstract double transformNumber(double d);

    public abstract long transformNumber(long j);

    public static NFSubstitution makeSubstitution(int pos2, NFRule rule, NFRule rulePredecessor, NFRuleSet ruleSet2, RuleBasedNumberFormat formatter, String description) {
        int i = pos2;
        NFRuleSet nFRuleSet = ruleSet2;
        String str = description;
        if (description.length() == 0) {
            return null;
        }
        switch (str.charAt(0)) {
            case '<':
                if (rule.getBaseValue() == -1) {
                    NFRule nFRule = rule;
                    throw new IllegalArgumentException("<< not allowed in negative-number rule");
                } else if (rule.getBaseValue() == -2 || rule.getBaseValue() == -3 || rule.getBaseValue() == -4) {
                    NFRule nFRule2 = rule;
                    return new IntegralPartSubstitution(i, nFRuleSet, str);
                } else if (!ruleSet2.isFractionSet()) {
                    return new MultiplierSubstitution(i, rule, nFRuleSet, str);
                } else {
                    NumeratorSubstitution numeratorSubstitution = new NumeratorSubstitution(i, (double) rule.getBaseValue(), formatter.getDefaultRuleSet(), str);
                    return numeratorSubstitution;
                }
            case '=':
                return new SameValueSubstitution(i, nFRuleSet, str);
            case '>':
                if (rule.getBaseValue() == -1) {
                    return new AbsoluteValueSubstitution(i, nFRuleSet, str);
                }
                if (rule.getBaseValue() == -2 || rule.getBaseValue() == -3 || rule.getBaseValue() == -4) {
                    return new FractionalPartSubstitution(i, nFRuleSet, str);
                }
                if (!ruleSet2.isFractionSet()) {
                    ModulusSubstitution modulusSubstitution = new ModulusSubstitution(i, rule, rulePredecessor, nFRuleSet, str);
                    return modulusSubstitution;
                }
                throw new IllegalArgumentException(">> not allowed in fraction rule set");
            default:
                NFRule nFRule3 = rule;
                throw new IllegalArgumentException("Illegal substitution character");
        }
    }

    NFSubstitution(int pos2, NFRuleSet ruleSet2, String description) {
        this.pos = pos2;
        int descriptionLen = description.length();
        if (descriptionLen >= 2 && description.charAt(0) == description.charAt(descriptionLen - 1)) {
            description = description.substring(1, descriptionLen - 1);
        } else if (descriptionLen != 0) {
            throw new IllegalArgumentException("Illegal substitution syntax");
        }
        if (description.length() == 0) {
            this.ruleSet = ruleSet2;
            this.numberFormat = null;
        } else if (description.charAt(0) == '%') {
            this.ruleSet = ruleSet2.owner.findRuleSet(description);
            this.numberFormat = null;
        } else if (description.charAt(0) == '#' || description.charAt(0) == '0') {
            this.ruleSet = null;
            this.numberFormat = (DecimalFormat) ruleSet2.owner.getDecimalFormat().clone();
            this.numberFormat.applyPattern(description);
        } else if (description.charAt(0) == '>') {
            this.ruleSet = ruleSet2;
            this.numberFormat = null;
        } else {
            throw new IllegalArgumentException("Illegal substitution syntax");
        }
    }

    public void setDivisor(int radix, short exponent) {
    }

    public boolean equals(Object that) {
        boolean z = false;
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
        if (this.pos == that2.pos && ((this.ruleSet != null || that2.ruleSet == null) && (this.numberFormat != null ? this.numberFormat.equals(that2.numberFormat) : that2.numberFormat == null))) {
            z = true;
        }
        return z;
    }

    public int hashCode() {
        return 42;
    }

    public String toString() {
        if (this.ruleSet != null) {
            return tokenChar() + this.ruleSet.getName() + tokenChar();
        }
        return tokenChar() + this.numberFormat.toPattern() + tokenChar();
    }

    public void doSubstitution(long number, StringBuilder toInsertInto, int position, int recursionCount) {
        if (this.ruleSet != null) {
            this.ruleSet.format(transformNumber(number), toInsertInto, position + this.pos, recursionCount);
        } else if (number <= MAX_INT64_IN_DOUBLE) {
            double numberToFormat = transformNumber((double) number);
            if (this.numberFormat.getMaximumFractionDigits() == 0) {
                numberToFormat = Math.floor(numberToFormat);
            }
            toInsertInto.insert(this.pos + position, this.numberFormat.format(numberToFormat));
        } else {
            toInsertInto.insert(this.pos + position, this.numberFormat.format(transformNumber(number)));
        }
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
            toInsertInto.insert(position + this.pos, this.numberFormat.format(numberToFormat));
        }
        StringBuilder sb = toInsertInto;
    }

    public Number doParse(String text, ParsePosition parsePosition, double baseValue, double upperBound, boolean lenientParse) {
        Number tempResult;
        double upperBound2 = calcUpperBound(upperBound);
        if (this.ruleSet != null) {
            tempResult = this.ruleSet.parse(text, parsePosition, upperBound2);
            if (lenientParse && !this.ruleSet.isFractionSet() && parsePosition.getIndex() == 0) {
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
