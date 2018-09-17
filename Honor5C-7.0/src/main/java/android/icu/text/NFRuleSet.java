package android.icu.text;

import android.icu.impl.PatternProps;
import android.icu.impl.Utility;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

final class NFRuleSet {
    static final /* synthetic */ boolean -assertionsDisabled = false;
    static final int IMPROPER_FRACTION_RULE_INDEX = 1;
    static final int INFINITY_RULE_INDEX = 4;
    static final int MASTER_RULE_INDEX = 3;
    static final int NAN_RULE_INDEX = 5;
    static final int NEGATIVE_RULE_INDEX = 0;
    static final int PROPER_FRACTION_RULE_INDEX = 2;
    private static final int RECURSION_LIMIT = 64;
    LinkedList<NFRule> fractionRules;
    private boolean isFractionRuleSet;
    private final boolean isParseable;
    private final String name;
    final NFRule[] nonNumericalRules;
    final RuleBasedNumberFormat owner;
    private NFRule[] rules;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.NFRuleSet.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.NFRuleSet.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.text.NFRuleSet.<clinit>():void");
    }

    public NFRuleSet(RuleBasedNumberFormat owner, String[] descriptions, int index) throws IllegalArgumentException {
        boolean z = true;
        this.nonNumericalRules = new NFRule[6];
        this.isFractionRuleSet = -assertionsDisabled;
        this.owner = owner;
        String description = descriptions[index];
        if (description.length() == 0) {
            throw new IllegalArgumentException("Empty rule set description");
        }
        if (description.charAt(NEGATIVE_RULE_INDEX) == '%') {
            int pos = description.indexOf(58);
            if (pos == -1) {
                throw new IllegalArgumentException("Rule set name doesn't end in colon");
            }
            String name = description.substring(NEGATIVE_RULE_INDEX, pos);
            if (name.endsWith("@noparse")) {
                z = -assertionsDisabled;
            }
            this.isParseable = z;
            if (!this.isParseable) {
                name = name.substring(NEGATIVE_RULE_INDEX, name.length() - 8);
            }
            this.name = name;
            while (pos < description.length()) {
                pos += IMPROPER_FRACTION_RULE_INDEX;
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
        int oldP = NEGATIVE_RULE_INDEX;
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
            oldP = p + IMPROPER_FRACTION_RULE_INDEX;
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
            this.nonNumericalRules[NEGATIVE_RULE_INDEX] = rule;
        } else if (baseValue == -2) {
            setBestFractionRule(IMPROPER_FRACTION_RULE_INDEX, rule, true);
        } else if (baseValue == -3) {
            setBestFractionRule(PROPER_FRACTION_RULE_INDEX, rule, true);
        } else if (baseValue == -4) {
            setBestFractionRule(MASTER_RULE_INDEX, rule, true);
        } else if (baseValue == -5) {
            this.nonNumericalRules[INFINITY_RULE_INDEX] = rule;
        } else if (baseValue == -6) {
            this.nonNumericalRules[NAN_RULE_INDEX] = rule;
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
            return -assertionsDisabled;
        }
        NFRuleSet that2 = (NFRuleSet) that;
        if (!this.name.equals(that2.name) || this.rules.length != that2.rules.length || this.isFractionRuleSet != that2.isFractionRuleSet) {
            return -assertionsDisabled;
        }
        int i;
        for (i = NEGATIVE_RULE_INDEX; i < this.nonNumericalRules.length; i += IMPROPER_FRACTION_RULE_INDEX) {
            if (!Utility.objectEquals(this.nonNumericalRules[i], that2.nonNumericalRules[i])) {
                return -assertionsDisabled;
            }
        }
        for (i = NEGATIVE_RULE_INDEX; i < this.rules.length; i += IMPROPER_FRACTION_RULE_INDEX) {
            if (!this.rules[i].equals(that2.rules[i])) {
                return -assertionsDisabled;
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
        int i = NEGATIVE_RULE_INDEX;
        StringBuilder result = new StringBuilder();
        result.append(this.name).append(":\n");
        NFRule[] nFRuleArr = this.rules;
        int length = nFRuleArr.length;
        for (int i2 = NEGATIVE_RULE_INDEX; i2 < length; i2 += IMPROPER_FRACTION_RULE_INDEX) {
            result.append(nFRuleArr[i2].toString()).append("\n");
        }
        NFRule[] nFRuleArr2 = this.nonNumericalRules;
        int length2 = nFRuleArr2.length;
        while (i < length2) {
            NFRule rule = nFRuleArr2[i];
            if (rule != null) {
                if (rule.getBaseValue() == -2 || rule.getBaseValue() == -3 || rule.getBaseValue() == -4) {
                    for (NFRule fractionRule : this.fractionRules) {
                        if (fractionRule.getBaseValue() == rule.getBaseValue()) {
                            result.append(fractionRule.toString()).append("\n");
                        }
                    }
                } else {
                    result.append(rule.toString()).append("\n");
                }
            }
            i += IMPROPER_FRACTION_RULE_INDEX;
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
        return this.name.startsWith("%%") ? -assertionsDisabled : true;
    }

    public boolean isParseable() {
        return this.isParseable;
    }

    public void format(long number, StringBuffer toInsertInto, int pos, int recursionCount) {
        if (recursionCount >= RECURSION_LIMIT) {
            throw new IllegalStateException("Recursion limit exceeded when applying ruleSet " + this.name);
        }
        findNormalRule(number).doFormat(number, toInsertInto, pos, recursionCount + IMPROPER_FRACTION_RULE_INDEX);
    }

    public void format(double number, StringBuffer toInsertInto, int pos, int recursionCount) {
        if (recursionCount >= RECURSION_LIMIT) {
            throw new IllegalStateException("Recursion limit exceeded when applying ruleSet " + this.name);
        }
        findRule(number).doFormat(number, toInsertInto, pos, recursionCount + IMPROPER_FRACTION_RULE_INDEX);
    }

    NFRule findRule(double number) {
        if (this.isFractionRuleSet) {
            return findFractionRuleSetRule(number);
        }
        NFRule rule;
        if (Double.isNaN(number)) {
            rule = this.nonNumericalRules[NAN_RULE_INDEX];
            if (rule == null) {
                rule = this.owner.getDefaultNaNRule();
            }
            return rule;
        }
        if (number < 0.0d) {
            if (this.nonNumericalRules[NEGATIVE_RULE_INDEX] != null) {
                return this.nonNumericalRules[NEGATIVE_RULE_INDEX];
            }
            number = -number;
        }
        if (Double.isInfinite(number)) {
            rule = this.nonNumericalRules[INFINITY_RULE_INDEX];
            if (rule == null) {
                rule = this.owner.getDefaultInfinityRule();
            }
            return rule;
        }
        if (!(this.nonNumericalRules == null || number == Math.floor(number))) {
            if (number < 1.0d && this.nonNumericalRules[PROPER_FRACTION_RULE_INDEX] != null) {
                return this.nonNumericalRules[PROPER_FRACTION_RULE_INDEX];
            }
            if (this.nonNumericalRules[IMPROPER_FRACTION_RULE_INDEX] != null) {
                return this.nonNumericalRules[IMPROPER_FRACTION_RULE_INDEX];
            }
        }
        if (this.nonNumericalRules == null || this.nonNumericalRules[MASTER_RULE_INDEX] == null) {
            return findNormalRule(Math.round(number));
        }
        return this.nonNumericalRules[MASTER_RULE_INDEX];
    }

    private NFRule findNormalRule(long number) {
        if (this.isFractionRuleSet) {
            return findFractionRuleSetRule((double) number);
        }
        if (number < 0) {
            if (this.nonNumericalRules[NEGATIVE_RULE_INDEX] != null) {
                return this.nonNumericalRules[NEGATIVE_RULE_INDEX];
            }
            number = -number;
        }
        int lo = NEGATIVE_RULE_INDEX;
        int hi = this.rules.length;
        if (hi <= 0) {
            return this.nonNumericalRules[MASTER_RULE_INDEX];
        }
        while (lo < hi) {
            int mid = (lo + hi) >>> IMPROPER_FRACTION_RULE_INDEX;
            long ruleBaseValue = this.rules[mid].getBaseValue();
            if (ruleBaseValue == number) {
                return this.rules[mid];
            }
            if (ruleBaseValue > number) {
                hi = mid;
            } else {
                lo = mid + IMPROPER_FRACTION_RULE_INDEX;
            }
        }
        if (hi == 0) {
            throw new IllegalStateException("The rule set " + this.name + " cannot format the value " + number);
        }
        NFRule result = this.rules[hi - 1];
        if (result.shouldRollBack((double) number)) {
            if (hi == IMPROPER_FRACTION_RULE_INDEX) {
                throw new IllegalStateException("The rule set " + this.name + " cannot roll back from the rule '" + result + "'");
            }
            result = this.rules[hi - 2];
        }
        return result;
    }

    private NFRule findFractionRuleSetRule(double number) {
        int i;
        long leastCommonMultiple = this.rules[NEGATIVE_RULE_INDEX].getBaseValue();
        for (i = IMPROPER_FRACTION_RULE_INDEX; i < this.rules.length; i += IMPROPER_FRACTION_RULE_INDEX) {
            leastCommonMultiple = lcm(leastCommonMultiple, this.rules[i].getBaseValue());
        }
        long numerator = Math.round(((double) leastCommonMultiple) * number);
        long difference = Long.MAX_VALUE;
        int winner = NEGATIVE_RULE_INDEX;
        for (i = NEGATIVE_RULE_INDEX; i < this.rules.length; i += IMPROPER_FRACTION_RULE_INDEX) {
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
        if (winner + IMPROPER_FRACTION_RULE_INDEX < this.rules.length && this.rules[winner + IMPROPER_FRACTION_RULE_INDEX].getBaseValue() == this.rules[winner].getBaseValue() && (Math.round(((double) this.rules[winner].getBaseValue()) * number) < 1 || Math.round(((double) this.rules[winner].getBaseValue()) * number) >= 2)) {
            winner += IMPROPER_FRACTION_RULE_INDEX;
        }
        return this.rules[winner];
    }

    private static long lcm(long x, long y) {
        long t;
        long x1 = x;
        long y1 = y;
        int p2 = NEGATIVE_RULE_INDEX;
        while ((1 & x1) == 0 && (1 & y1) == 0) {
            p2 += IMPROPER_FRACTION_RULE_INDEX;
            x1 >>= IMPROPER_FRACTION_RULE_INDEX;
            y1 >>= IMPROPER_FRACTION_RULE_INDEX;
        }
        if ((1 & x1) == 1) {
            t = -y1;
        } else {
            t = x1;
        }
        while (t != 0) {
            while ((1 & t) == 0) {
                t >>= IMPROPER_FRACTION_RULE_INDEX;
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
        ParsePosition highWaterMark = new ParsePosition(NEGATIVE_RULE_INDEX);
        Number result = NFRule.ZERO;
        if (text.length() == 0) {
            return result;
        }
        NFRule[] nFRuleArr = this.nonNumericalRules;
        int length = nFRuleArr.length;
        for (int i = NEGATIVE_RULE_INDEX; i < length; i += IMPROPER_FRACTION_RULE_INDEX) {
            Number tempResult;
            NFRule fractionRule = nFRuleArr[i];
            if (fractionRule != null) {
                tempResult = fractionRule.doParse(text, parsePosition, -assertionsDisabled, upperBound);
                if (parsePosition.getIndex() > highWaterMark.getIndex()) {
                    result = tempResult;
                    highWaterMark.setIndex(parsePosition.getIndex());
                }
                parsePosition.setIndex(NEGATIVE_RULE_INDEX);
            }
        }
        int i2 = this.rules.length - 1;
        while (i2 >= 0 && highWaterMark.getIndex() < text.length()) {
            if (this.isFractionRuleSet || ((double) this.rules[i2].getBaseValue()) < upperBound) {
                tempResult = this.rules[i2].doParse(text, parsePosition, this.isFractionRuleSet, upperBound);
                if (parsePosition.getIndex() > highWaterMark.getIndex()) {
                    result = tempResult;
                    highWaterMark.setIndex(parsePosition.getIndex());
                }
                parsePosition.setIndex(NEGATIVE_RULE_INDEX);
            }
            i2--;
        }
        parsePosition.setIndex(highWaterMark.getIndex());
        return result;
    }

    public void setDecimalFormatSymbols(DecimalFormatSymbols newSymbols) {
        NFRule rule;
        int i = NEGATIVE_RULE_INDEX;
        NFRule[] nFRuleArr = this.rules;
        int length = nFRuleArr.length;
        for (int i2 = NEGATIVE_RULE_INDEX; i2 < length; i2 += IMPROPER_FRACTION_RULE_INDEX) {
            nFRuleArr[i2].setDecimalFormatSymbols(newSymbols);
        }
        if (this.fractionRules != null) {
            for (int nonNumericalIdx = IMPROPER_FRACTION_RULE_INDEX; nonNumericalIdx <= MASTER_RULE_INDEX; nonNumericalIdx += IMPROPER_FRACTION_RULE_INDEX) {
                if (this.nonNumericalRules[nonNumericalIdx] != null) {
                    for (NFRule rule2 : this.fractionRules) {
                        if (this.nonNumericalRules[nonNumericalIdx].getBaseValue() == rule2.getBaseValue()) {
                            setBestFractionRule(nonNumericalIdx, rule2, -assertionsDisabled);
                        }
                    }
                }
            }
        }
        NFRule[] nFRuleArr2 = this.nonNumericalRules;
        int length2 = nFRuleArr2.length;
        while (i < length2) {
            rule2 = nFRuleArr2[i];
            if (rule2 != null) {
                rule2.setDecimalFormatSymbols(newSymbols);
            }
            i += IMPROPER_FRACTION_RULE_INDEX;
        }
    }
}
