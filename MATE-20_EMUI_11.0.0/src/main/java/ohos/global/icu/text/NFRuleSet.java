package ohos.global.icu.text;

import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Objects;
import ohos.com.sun.org.apache.xpath.internal.XPath;
import ohos.global.icu.impl.PatternProps;

/* access modifiers changed from: package-private */
public final class NFRuleSet {
    static final /* synthetic */ boolean $assertionsDisabled = false;
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

    public int hashCode() {
        return 42;
    }

    public NFRuleSet(RuleBasedNumberFormat ruleBasedNumberFormat, String[] strArr, int i) throws IllegalArgumentException {
        this.owner = ruleBasedNumberFormat;
        String str = strArr[i];
        if (str.length() != 0) {
            if (str.charAt(0) == '%') {
                int indexOf = str.indexOf(58);
                if (indexOf != -1) {
                    String substring = str.substring(0, indexOf);
                    this.isParseable = true ^ substring.endsWith("@noparse");
                    this.name = !this.isParseable ? substring.substring(0, substring.length() - 8) : substring;
                    while (indexOf < str.length()) {
                        indexOf++;
                        if (!PatternProps.isWhiteSpace(str.charAt(indexOf))) {
                            break;
                        }
                    }
                    str = str.substring(indexOf);
                    strArr[i] = str;
                } else {
                    throw new IllegalArgumentException("Rule set name doesn't end in colon");
                }
            } else {
                this.name = "%default";
                this.isParseable = true;
            }
            if (str.length() == 0) {
                throw new IllegalArgumentException("Empty rule set description");
            }
            return;
        }
        throw new IllegalArgumentException("Empty rule set description");
    }

    public void parseRules(String str) {
        ArrayList<NFRule> arrayList = new ArrayList();
        int length = str.length();
        NFRule nFRule = null;
        int i = 0;
        do {
            int indexOf = str.indexOf(59, i);
            if (indexOf < 0) {
                indexOf = length;
            }
            NFRule.makeRules(str.substring(i, indexOf), this, nFRule, this.owner, arrayList);
            if (!arrayList.isEmpty()) {
                nFRule = (NFRule) arrayList.get(arrayList.size() - 1);
            }
            i = indexOf + 1;
        } while (i < length);
        long j = 0;
        for (NFRule nFRule2 : arrayList) {
            long baseValue = nFRule2.getBaseValue();
            if (baseValue == 0) {
                nFRule2.setBaseValue(j);
            } else if (baseValue >= j) {
                j = baseValue;
            } else {
                throw new IllegalArgumentException("Rules are not in order, base: " + baseValue + " < " + j);
            }
            if (!this.isFractionRuleSet) {
                j++;
            }
        }
        this.rules = new NFRule[arrayList.size()];
        arrayList.toArray(this.rules);
    }

    /* access modifiers changed from: package-private */
    public void setNonNumericalRule(NFRule nFRule) {
        long baseValue = nFRule.getBaseValue();
        if (baseValue == -1) {
            this.nonNumericalRules[0] = nFRule;
        } else if (baseValue == -2) {
            setBestFractionRule(1, nFRule, true);
        } else if (baseValue == -3) {
            setBestFractionRule(2, nFRule, true);
        } else if (baseValue == -4) {
            setBestFractionRule(3, nFRule, true);
        } else if (baseValue == -5) {
            this.nonNumericalRules[4] = nFRule;
        } else if (baseValue == -6) {
            this.nonNumericalRules[5] = nFRule;
        }
    }

    private void setBestFractionRule(int i, NFRule nFRule, boolean z) {
        if (z) {
            if (this.fractionRules == null) {
                this.fractionRules = new LinkedList<>();
            }
            this.fractionRules.add(nFRule);
        }
        NFRule[] nFRuleArr = this.nonNumericalRules;
        if (nFRuleArr[i] == null) {
            nFRuleArr[i] = nFRule;
        } else if (this.owner.getDecimalFormatSymbols().getDecimalSeparator() == nFRule.getDecimalPoint()) {
            this.nonNumericalRules[i] = nFRule;
        }
    }

    public void makeIntoFractionRuleSet() {
        this.isFractionRuleSet = true;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof NFRuleSet)) {
            return false;
        }
        NFRuleSet nFRuleSet = (NFRuleSet) obj;
        if (!this.name.equals(nFRuleSet.name) || this.rules.length != nFRuleSet.rules.length || this.isFractionRuleSet != nFRuleSet.isFractionRuleSet) {
            return false;
        }
        int i = 0;
        while (true) {
            NFRule[] nFRuleArr = this.nonNumericalRules;
            if (i >= nFRuleArr.length) {
                int i2 = 0;
                while (true) {
                    NFRule[] nFRuleArr2 = this.rules;
                    if (i2 >= nFRuleArr2.length) {
                        return true;
                    }
                    if (!nFRuleArr2[i2].equals(nFRuleSet.rules[i2])) {
                        return false;
                    }
                    i2++;
                }
            } else if (!Objects.equals(nFRuleArr[i], nFRuleSet.nonNumericalRules[i])) {
                return false;
            } else {
                i++;
            }
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.name);
        sb.append(":\n");
        for (NFRule nFRule : this.rules) {
            sb.append(nFRule.toString());
            sb.append("\n");
        }
        NFRule[] nFRuleArr = this.nonNumericalRules;
        for (NFRule nFRule2 : nFRuleArr) {
            if (nFRule2 != null) {
                if (nFRule2.getBaseValue() == -2 || nFRule2.getBaseValue() == -3 || nFRule2.getBaseValue() == -4) {
                    Iterator<NFRule> it = this.fractionRules.iterator();
                    while (it.hasNext()) {
                        NFRule next = it.next();
                        if (next.getBaseValue() == nFRule2.getBaseValue()) {
                            sb.append(next.toString());
                            sb.append("\n");
                        }
                    }
                } else {
                    sb.append(nFRule2.toString());
                    sb.append("\n");
                }
            }
        }
        return sb.toString();
    }

    public boolean isFractionSet() {
        return this.isFractionRuleSet;
    }

    public String getName() {
        return this.name;
    }

    public boolean isPublic() {
        return !this.name.startsWith("%%");
    }

    public boolean isParseable() {
        return this.isParseable;
    }

    public void format(long j, StringBuilder sb, int i, int i2) {
        if (i2 < 64) {
            findNormalRule(j).doFormat(j, sb, i, i2 + 1);
            return;
        }
        throw new IllegalStateException("Recursion limit exceeded when applying ruleSet " + this.name);
    }

    public void format(double d, StringBuilder sb, int i, int i2) {
        if (i2 < 64) {
            findRule(d).doFormat(d, sb, i, i2 + 1);
            return;
        }
        throw new IllegalStateException("Recursion limit exceeded when applying ruleSet " + this.name);
    }

    /* access modifiers changed from: package-private */
    public NFRule findRule(double d) {
        if (this.isFractionRuleSet) {
            return findFractionRuleSetRule(d);
        }
        if (Double.isNaN(d)) {
            NFRule nFRule = this.nonNumericalRules[5];
            return nFRule == null ? this.owner.getDefaultNaNRule() : nFRule;
        }
        if (d < XPath.MATCH_SCORE_QNAME) {
            NFRule[] nFRuleArr = this.nonNumericalRules;
            if (nFRuleArr[0] != null) {
                return nFRuleArr[0];
            }
            d = -d;
        }
        if (Double.isInfinite(d)) {
            NFRule nFRule2 = this.nonNumericalRules[4];
            return nFRule2 == null ? this.owner.getDefaultInfinityRule() : nFRule2;
        }
        if (d != Math.floor(d)) {
            if (d < 1.0d) {
                NFRule[] nFRuleArr2 = this.nonNumericalRules;
                if (nFRuleArr2[2] != null) {
                    return nFRuleArr2[2];
                }
            }
            NFRule[] nFRuleArr3 = this.nonNumericalRules;
            if (nFRuleArr3[1] != null) {
                return nFRuleArr3[1];
            }
        }
        NFRule[] nFRuleArr4 = this.nonNumericalRules;
        if (nFRuleArr4[3] != null) {
            return nFRuleArr4[3];
        }
        return findNormalRule(Math.round(d));
    }

    private NFRule findNormalRule(long j) {
        if (this.isFractionRuleSet) {
            return findFractionRuleSetRule((double) j);
        }
        int i = 0;
        if (j < 0) {
            NFRule[] nFRuleArr = this.nonNumericalRules;
            if (nFRuleArr[0] != null) {
                return nFRuleArr[0];
            }
            j = -j;
        }
        int length = this.rules.length;
        if (length <= 0) {
            return this.nonNumericalRules[3];
        }
        while (i < length) {
            int i2 = (i + length) >>> 1;
            int i3 = (this.rules[i2].getBaseValue() > j ? 1 : (this.rules[i2].getBaseValue() == j ? 0 : -1));
            if (i3 == 0) {
                return this.rules[i2];
            }
            if (i3 > 0) {
                length = i2;
            } else {
                i = i2 + 1;
            }
        }
        if (length != 0) {
            NFRule nFRule = this.rules[length - 1];
            if (!nFRule.shouldRollBack(j)) {
                return nFRule;
            }
            if (length != 1) {
                return this.rules[length - 2];
            }
            throw new IllegalStateException("The rule set " + this.name + " cannot roll back from the rule '" + nFRule + "'");
        }
        throw new IllegalStateException("The rule set " + this.name + " cannot format the value " + j);
    }

    private NFRule findFractionRuleSetRule(double d) {
        int i = 0;
        long baseValue = this.rules[0].getBaseValue();
        int i2 = 1;
        while (true) {
            NFRule[] nFRuleArr = this.rules;
            if (i2 >= nFRuleArr.length) {
                break;
            }
            baseValue = lcm(baseValue, nFRuleArr[i2].getBaseValue());
            i2++;
        }
        long round = Math.round(((double) baseValue) * d);
        long j = Long.MAX_VALUE;
        int i3 = 0;
        while (true) {
            NFRule[] nFRuleArr2 = this.rules;
            if (i >= nFRuleArr2.length) {
                break;
            }
            long baseValue2 = (nFRuleArr2[i].getBaseValue() * round) % baseValue;
            long j2 = baseValue - baseValue2;
            if (j2 < baseValue2) {
                baseValue2 = j2;
            }
            if (baseValue2 < j) {
                if (baseValue2 == 0) {
                    i3 = i;
                    break;
                }
                i3 = i;
                j = baseValue2;
            }
            i++;
        }
        int i4 = i3 + 1;
        NFRule[] nFRuleArr3 = this.rules;
        if (i4 < nFRuleArr3.length && nFRuleArr3[i4].getBaseValue() == this.rules[i3].getBaseValue() && (Math.round(((double) this.rules[i3].getBaseValue()) * d) < 1 || Math.round(d * ((double) this.rules[i3].getBaseValue())) >= 2)) {
            i3 = i4;
        }
        return this.rules[i3];
    }

    private static long lcm(long j, long j2) {
        long j3;
        long j4;
        long j5;
        long j6 = j2;
        int i = 0;
        long j7 = j;
        while (true) {
            j3 = j7 & 1;
            if (j3 != 0 || (j6 & 1) != 0) {
                break;
            }
            i++;
            j7 >>= 1;
            j6 >>= 1;
        }
        if (j3 == 1) {
            j7 = -j6;
            j4 = j6;
            j5 = j7;
        } else {
            j4 = j6;
            j5 = j7;
        }
        while (j7 != 0) {
            while ((j7 & 1) == 0) {
                j7 >>= 1;
            }
            if (j7 > 0) {
                j5 = j7;
            } else {
                j4 = -j7;
            }
            j7 = j5 - j4;
        }
        return (j / (j5 << i)) * j2;
    }

    public Number parse(String str, ParsePosition parsePosition, double d, int i) {
        ParsePosition parsePosition2 = new ParsePosition(0);
        Long l = NFRule.ZERO;
        if (str.length() == 0) {
            return l;
        }
        int i2 = i;
        Long l2 = l;
        int i3 = 0;
        while (true) {
            NFRule[] nFRuleArr = this.nonNumericalRules;
            if (i3 >= nFRuleArr.length) {
                break;
            }
            NFRule nFRule = nFRuleArr[i3];
            if (nFRule != null && ((i2 >> i3) & 1) == 0) {
                i2 |= 1 << i3;
                Number doParse = nFRule.doParse(str, parsePosition, false, d, i2);
                if (parsePosition.getIndex() > parsePosition2.getIndex()) {
                    parsePosition2.setIndex(parsePosition.getIndex());
                    l2 = doParse;
                }
                parsePosition.setIndex(0);
            }
            i3++;
        }
        for (int length = this.rules.length - 1; length >= 0 && parsePosition2.getIndex() < str.length(); length--) {
            if (this.isFractionRuleSet || ((double) this.rules[length].getBaseValue()) < d) {
                Number doParse2 = this.rules[length].doParse(str, parsePosition, this.isFractionRuleSet, d, i2);
                if (parsePosition.getIndex() > parsePosition2.getIndex()) {
                    parsePosition2.setIndex(parsePosition.getIndex());
                    l2 = doParse2;
                }
                parsePosition.setIndex(0);
            }
        }
        parsePosition.setIndex(parsePosition2.getIndex());
        return l2;
    }

    public void setDecimalFormatSymbols(DecimalFormatSymbols decimalFormatSymbols) {
        for (NFRule nFRule : this.rules) {
            nFRule.setDecimalFormatSymbols(decimalFormatSymbols);
        }
        if (this.fractionRules != null) {
            for (int i = 1; i <= 3; i++) {
                if (this.nonNumericalRules[i] != null) {
                    Iterator<NFRule> it = this.fractionRules.iterator();
                    while (it.hasNext()) {
                        NFRule next = it.next();
                        if (this.nonNumericalRules[i].getBaseValue() == next.getBaseValue()) {
                            setBestFractionRule(i, next, false);
                        }
                    }
                }
            }
        }
        NFRule[] nFRuleArr = this.nonNumericalRules;
        for (NFRule nFRule2 : nFRuleArr) {
            if (nFRule2 != null) {
                nFRule2.setDecimalFormatSymbols(decimalFormatSymbols);
            }
        }
    }
}
