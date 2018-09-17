package android.icu.text;

import android.icu.impl.PatternProps;
import android.icu.impl.Utility;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

final class NFRuleSet {
    static final /* synthetic */ boolean -assertionsDisabled = (NFRuleSet.class.desiredAssertionStatus() ^ 1);
    static final int IMPROPER_FRACTION_RULE_INDEX = 1;
    static final int INFINITY_RULE_INDEX = 4;
    static final int MASTER_RULE_INDEX = 3;
    static final int NAN_RULE_INDEX = 5;
    static final int NEGATIVE_RULE_INDEX = 0;
    static final int PROPER_FRACTION_RULE_INDEX = 2;
    private static final int RECURSION_LIMIT = 64;
    LinkedList<NFRule> fractionRules;
    private boolean isFractionRuleSet = false;
    private final boolean isParseable;
    private final String name;
    final NFRule[] nonNumericalRules = new NFRule[6];
    final RuleBasedNumberFormat owner;
    private NFRule[] rules;

    public NFRuleSet(RuleBasedNumberFormat owner, String[] descriptions, int index) throws IllegalArgumentException {
        this.owner = owner;
        String description = descriptions[index];
        if (description.length() == 0) {
            throw new IllegalArgumentException("Empty rule set description");
        }
        if (description.charAt(0) == '%') {
            int pos = description.indexOf(58);
            if (pos == -1) {
                throw new IllegalArgumentException("Rule set name doesn't end in colon");
            }
            String name = description.substring(0, pos);
            this.isParseable = name.endsWith("@noparse") ^ 1;
            if (!this.isParseable) {
                name = name.substring(0, name.length() - 8);
            }
            this.name = name;
            while (pos < description.length()) {
                pos++;
                if (!PatternProps.isWhiteSpace(description.charAt(pos))) {
                    break;
                }
            }
            description = description.substring(pos);
            descriptions[index] = description;
        } else {
            this.name = "%default";
            this.isParseable = true;
        }
        if (description.length() == 0) {
            throw new IllegalArgumentException("Empty rule set description");
        }
    }

    public void parseRules(String description) {
        List<NFRule> tempRules = new ArrayList();
        NFRule predecessor = null;
        int oldP = 0;
        int descriptionLen = description.length();
        do {
            int p = description.indexOf(59, oldP);
            if (p < 0) {
                p = descriptionLen;
            }
            NFRule.makeRules(description.substring(oldP, p), this, predecessor, this.owner, tempRules);
            if (!tempRules.isEmpty()) {
                predecessor = (NFRule) tempRules.get(tempRules.size() - 1);
            }
            oldP = p + 1;
        } while (oldP < descriptionLen);
        long defaultBaseValue = 0;
        for (NFRule rule : tempRules) {
            long baseValue = rule.getBaseValue();
            if (baseValue == 0) {
                rule.setBaseValue(defaultBaseValue);
            } else if (baseValue < defaultBaseValue) {
                throw new IllegalArgumentException("Rules are not in order, base: " + baseValue + " < " + defaultBaseValue);
            } else {
                defaultBaseValue = baseValue;
            }
            if (!this.isFractionRuleSet) {
                defaultBaseValue++;
            }
        }
        this.rules = new NFRule[tempRules.size()];
        tempRules.toArray(this.rules);
    }

    void setNonNumericalRule(NFRule rule) {
        long baseValue = rule.getBaseValue();
        if (baseValue == -1) {
            this.nonNumericalRules[0] = rule;
        } else if (baseValue == -2) {
            setBestFractionRule(1, rule, true);
        } else if (baseValue == -3) {
            setBestFractionRule(2, rule, true);
        } else if (baseValue == -4) {
            setBestFractionRule(3, rule, true);
        } else if (baseValue == -5) {
            this.nonNumericalRules[4] = rule;
        } else if (baseValue == -6) {
            this.nonNumericalRules[5] = rule;
        }
    }

    private void setBestFractionRule(int originalIndex, NFRule newRule, boolean rememberRule) {
        if (rememberRule) {
            if (this.fractionRules == null) {
                this.fractionRules = new LinkedList();
            }
            this.fractionRules.add(newRule);
        }
        if (this.nonNumericalRules[originalIndex] == null) {
            this.nonNumericalRules[originalIndex] = newRule;
        } else if (this.owner.getDecimalFormatSymbols().getDecimalSeparator() == newRule.getDecimalPoint()) {
            this.nonNumericalRules[originalIndex] = newRule;
        }
    }

    public void makeIntoFractionRuleSet() {
        this.isFractionRuleSet = true;
    }

    public boolean equals(Object that) {
        if (!(that instanceof NFRuleSet)) {
            return false;
        }
        NFRuleSet that2 = (NFRuleSet) that;
        if (!this.name.equals(that2.name) || this.rules.length != that2.rules.length || this.isFractionRuleSet != that2.isFractionRuleSet) {
            return false;
        }
        int i;
        for (i = 0; i < this.nonNumericalRules.length; i++) {
            if (!Utility.objectEquals(this.nonNumericalRules[i], that2.nonNumericalRules[i])) {
                return false;
            }
        }
        for (i = 0; i < this.rules.length; i++) {
            if (!this.rules[i].equals(that2.rules[i])) {
                return false;
            }
        }
        return true;
    }

    public int hashCode() {
        if (-assertionsDisabled) {
            return 42;
        }
        throw new AssertionError("hashCode not designed");
    }

    public String toString() {
        NFRule rule;
        int i = 0;
        StringBuilder result = new StringBuilder();
        result.append(this.name).append(":\n");
        for (NFRule rule2 : this.rules) {
            result.append(rule2.toString()).append("\n");
        }
        NFRule[] nFRuleArr = this.nonNumericalRules;
        int length = nFRuleArr.length;
        while (i < length) {
            rule2 = nFRuleArr[i];
            if (rule2 != null) {
                if (rule2.getBaseValue() == -2 || rule2.getBaseValue() == -3 || rule2.getBaseValue() == -4) {
                    for (NFRule fractionRule : this.fractionRules) {
                        if (fractionRule.getBaseValue() == rule2.getBaseValue()) {
                            result.append(fractionRule.toString()).append("\n");
                        }
                    }
                } else {
                    result.append(rule2.toString()).append("\n");
                }
            }
            i++;
        }
        return result.toString();
    }

    public boolean isFractionSet() {
        return this.isFractionRuleSet;
    }

    public String getName() {
        return this.name;
    }

    public boolean isPublic() {
        return this.name.startsWith("%%") ^ 1;
    }

    public boolean isParseable() {
        return this.isParseable;
    }

    public void format(long number, StringBuilder toInsertInto, int pos, int recursionCount) {
        if (recursionCount >= 64) {
            throw new IllegalStateException("Recursion limit exceeded when applying ruleSet " + this.name);
        }
        findNormalRule(number).doFormat(number, toInsertInto, pos, recursionCount + 1);
    }

    public void format(double number, StringBuilder toInsertInto, int pos, int recursionCount) {
        if (recursionCount >= 64) {
            throw new IllegalStateException("Recursion limit exceeded when applying ruleSet " + this.name);
        }
        findRule(number).doFormat(number, toInsertInto, pos, recursionCount + 1);
    }

    NFRule findRule(double number) {
        if (this.isFractionRuleSet) {
            return findFractionRuleSetRule(number);
        }
        NFRule rule;
        if (Double.isNaN(number)) {
            rule = this.nonNumericalRules[5];
            if (rule == null) {
                rule = this.owner.getDefaultNaNRule();
            }
            return rule;
        }
        if (number < 0.0d) {
            if (this.nonNumericalRules[0] != null) {
                return this.nonNumericalRules[0];
            }
            number = -number;
        }
        if (Double.isInfinite(number)) {
            rule = this.nonNumericalRules[4];
            if (rule == null) {
                rule = this.owner.getDefaultInfinityRule();
            }
            return rule;
        }
        if (number != Math.floor(number)) {
            if (number < 1.0d && this.nonNumericalRules[2] != null) {
                return this.nonNumericalRules[2];
            }
            if (this.nonNumericalRules[1] != null) {
                return this.nonNumericalRules[1];
            }
        }
        if (this.nonNumericalRules[3] != null) {
            return this.nonNumericalRules[3];
        }
        return findNormalRule(Math.round(number));
    }

    private NFRule findNormalRule(long number) {
        if (this.isFractionRuleSet) {
            return findFractionRuleSetRule((double) number);
        }
        if (number < 0) {
            if (this.nonNumericalRules[0] != null) {
                return this.nonNumericalRules[0];
            }
            number = -number;
        }
        int lo = 0;
        int hi = this.rules.length;
        if (hi <= 0) {
            return this.nonNumericalRules[3];
        }
        while (lo < hi) {
            int mid = (lo + hi) >>> 1;
            long ruleBaseValue = this.rules[mid].getBaseValue();
            if (ruleBaseValue == number) {
                return this.rules[mid];
            }
            if (ruleBaseValue > number) {
                hi = mid;
            } else {
                lo = mid + 1;
            }
        }
        if (hi == 0) {
            throw new IllegalStateException("The rule set " + this.name + " cannot format the value " + number);
        }
        NFRule result = this.rules[hi - 1];
        if (result.shouldRollBack(number)) {
            if (hi == 1) {
                throw new IllegalStateException("The rule set " + this.name + " cannot roll back from the rule '" + result + "'");
            }
            result = this.rules[hi - 2];
        }
        return result;
    }

    private NFRule findFractionRuleSetRule(double number) {
        int i;
        long leastCommonMultiple = this.rules[0].getBaseValue();
        for (i = 1; i < this.rules.length; i++) {
            leastCommonMultiple = lcm(leastCommonMultiple, this.rules[i].getBaseValue());
        }
        long numerator = Math.round(((double) leastCommonMultiple) * number);
        long difference = Long.MAX_VALUE;
        int winner = 0;
        for (i = 0; i < this.rules.length; i++) {
            long tempDifference = (this.rules[i].getBaseValue() * numerator) % leastCommonMultiple;
            if (leastCommonMultiple - tempDifference < tempDifference) {
                tempDifference = leastCommonMultiple - tempDifference;
            }
            if (tempDifference < difference) {
                difference = tempDifference;
                winner = i;
                if (tempDifference == 0) {
                    break;
                }
            }
        }
        if (winner + 1 < this.rules.length && this.rules[winner + 1].getBaseValue() == this.rules[winner].getBaseValue() && (Math.round(((double) this.rules[winner].getBaseValue()) * number) < 1 || Math.round(((double) this.rules[winner].getBaseValue()) * number) >= 2)) {
            winner++;
        }
        return this.rules[winner];
    }

    private static long lcm(long x, long y) {
        long t;
        long x1 = x;
        long y1 = y;
        int p2 = 0;
        while ((1 & x1) == 0 && (1 & y1) == 0) {
            p2++;
            x1 >>= 1;
            y1 >>= 1;
        }
        if ((1 & x1) == 1) {
            t = -y1;
        } else {
            t = x1;
        }
        while (t != 0) {
            while ((1 & t) == 0) {
                t >>= 1;
            }
            if (t > 0) {
                x1 = t;
            } else {
                y1 = -t;
            }
            t = x1 - y1;
        }
        return (x / (x1 << p2)) * y;
    }

    public Number parse(String text, ParsePosition parsePosition, double upperBound) {
        ParsePosition highWaterMark = new ParsePosition(0);
        Number result = NFRule.ZERO;
        if (text.length() == 0) {
            return result;
        }
        Number tempResult;
        for (NFRule fractionRule : this.nonNumericalRules) {
            if (fractionRule != null) {
                tempResult = fractionRule.doParse(text, parsePosition, false, upperBound);
                if (parsePosition.getIndex() > highWaterMark.getIndex()) {
                    result = tempResult;
                    highWaterMark.setIndex(parsePosition.getIndex());
                }
                parsePosition.setIndex(0);
            }
        }
        int i = this.rules.length - 1;
        while (i >= 0 && highWaterMark.getIndex() < text.length()) {
            if (this.isFractionRuleSet || ((double) this.rules[i].getBaseValue()) < upperBound) {
                tempResult = this.rules[i].doParse(text, parsePosition, this.isFractionRuleSet, upperBound);
                if (parsePosition.getIndex() > highWaterMark.getIndex()) {
                    result = tempResult;
                    highWaterMark.setIndex(parsePosition.getIndex());
                }
                parsePosition.setIndex(0);
            }
            i--;
        }
        parsePosition.setIndex(highWaterMark.getIndex());
        return result;
    }

    public void setDecimalFormatSymbols(DecimalFormatSymbols newSymbols) {
        NFRule rule;
        int i = 0;
        for (NFRule rule2 : this.rules) {
            rule2.setDecimalFormatSymbols(newSymbols);
        }
        if (this.fractionRules != null) {
            for (int nonNumericalIdx = 1; nonNumericalIdx <= 3; nonNumericalIdx++) {
                if (this.nonNumericalRules[nonNumericalIdx] != null) {
                    for (NFRule rule22 : this.fractionRules) {
                        if (this.nonNumericalRules[nonNumericalIdx].getBaseValue() == rule22.getBaseValue()) {
                            setBestFractionRule(nonNumericalIdx, rule22, false);
                        }
                    }
                }
            }
        }
        NFRule[] nFRuleArr = this.nonNumericalRules;
        int length = nFRuleArr.length;
        while (i < length) {
            rule22 = nFRuleArr[i];
            if (rule22 != null) {
                rule22.setDecimalFormatSymbols(newSymbols);
            }
            i++;
        }
    }
}
