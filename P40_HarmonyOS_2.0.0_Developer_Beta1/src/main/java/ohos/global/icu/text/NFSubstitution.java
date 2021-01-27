package ohos.global.icu.text;

import java.text.ParsePosition;

/* access modifiers changed from: package-private */
public abstract class NFSubstitution {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private static final long MAX_INT64_IN_DOUBLE = 9007199254740991L;
    final DecimalFormat numberFormat;
    final int pos;
    final NFRuleSet ruleSet;

    public abstract double calcUpperBound(double d);

    public abstract double composeRuleValue(double d, double d2);

    public int hashCode() {
        return 42;
    }

    public boolean isModulusSubstitution() {
        return false;
    }

    public void setDivisor(int i, short s) {
    }

    /* access modifiers changed from: package-private */
    public abstract char tokenChar();

    public abstract double transformNumber(double d);

    public abstract long transformNumber(long j);

    public static NFSubstitution makeSubstitution(int i, NFRule nFRule, NFRule nFRule2, NFRuleSet nFRuleSet, RuleBasedNumberFormat ruleBasedNumberFormat, String str) {
        if (str.length() == 0) {
            return null;
        }
        switch (str.charAt(0)) {
            case '<':
                if (nFRule.getBaseValue() == -1) {
                    throw new IllegalArgumentException("<< not allowed in negative-number rule");
                } else if (nFRule.getBaseValue() == -2 || nFRule.getBaseValue() == -3 || nFRule.getBaseValue() == -4) {
                    return new IntegralPartSubstitution(i, nFRuleSet, str);
                } else {
                    if (nFRuleSet.isFractionSet()) {
                        return new NumeratorSubstitution(i, (double) nFRule.getBaseValue(), ruleBasedNumberFormat.getDefaultRuleSet(), str);
                    }
                    return new MultiplierSubstitution(i, nFRule, nFRuleSet, str);
                }
            case '=':
                return new SameValueSubstitution(i, nFRuleSet, str);
            case '>':
                if (nFRule.getBaseValue() == -1) {
                    return new AbsoluteValueSubstitution(i, nFRuleSet, str);
                }
                if (nFRule.getBaseValue() == -2 || nFRule.getBaseValue() == -3 || nFRule.getBaseValue() == -4) {
                    return new FractionalPartSubstitution(i, nFRuleSet, str);
                }
                if (!nFRuleSet.isFractionSet()) {
                    return new ModulusSubstitution(i, nFRule, nFRule2, nFRuleSet, str);
                }
                throw new IllegalArgumentException(">> not allowed in fraction rule set");
            default:
                throw new IllegalArgumentException("Illegal substitution character");
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:8:0x002a  */
    /* JADX WARNING: Removed duplicated region for block: B:9:0x002f  */
    NFSubstitution(int i, NFRuleSet nFRuleSet, String str) {
        this.pos = i;
        int length = str.length();
        if (length >= 2) {
            int i2 = length - 1;
            if (str.charAt(0) == str.charAt(i2)) {
                str = str.substring(1, i2);
                if (str.length() != 0) {
                    this.ruleSet = nFRuleSet;
                    this.numberFormat = null;
                    return;
                } else if (str.charAt(0) == '%') {
                    this.ruleSet = nFRuleSet.owner.findRuleSet(str);
                    this.numberFormat = null;
                    return;
                } else if (str.charAt(0) == '#' || str.charAt(0) == '0') {
                    this.ruleSet = null;
                    this.numberFormat = (DecimalFormat) nFRuleSet.owner.getDecimalFormat().clone();
                    this.numberFormat.applyPattern(str);
                    return;
                } else if (str.charAt(0) == '>') {
                    this.ruleSet = nFRuleSet;
                    this.numberFormat = null;
                    return;
                } else {
                    throw new IllegalArgumentException("Illegal substitution syntax");
                }
            }
        }
        if (length != 0) {
            throw new IllegalArgumentException("Illegal substitution syntax");
        }
        if (str.length() != 0) {
        }
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        NFSubstitution nFSubstitution = (NFSubstitution) obj;
        if (this.pos != nFSubstitution.pos) {
            return false;
        }
        if (this.ruleSet == null && nFSubstitution.ruleSet != null) {
            return false;
        }
        DecimalFormat decimalFormat = this.numberFormat;
        if (decimalFormat == null) {
            if (nFSubstitution.numberFormat != null) {
                return false;
            }
        } else if (!decimalFormat.equals(nFSubstitution.numberFormat)) {
            return false;
        }
        return true;
    }

    public String toString() {
        if (this.ruleSet != null) {
            return tokenChar() + this.ruleSet.getName() + tokenChar();
        }
        return tokenChar() + this.numberFormat.toPattern() + tokenChar();
    }

    public void doSubstitution(long j, StringBuilder sb, int i, int i2) {
        if (this.ruleSet != null) {
            this.ruleSet.format(transformNumber(j), sb, i + this.pos, i2);
        } else if (j <= MAX_INT64_IN_DOUBLE) {
            double transformNumber = transformNumber((double) j);
            if (this.numberFormat.getMaximumFractionDigits() == 0) {
                transformNumber = Math.floor(transformNumber);
            }
            sb.insert(i + this.pos, this.numberFormat.format(transformNumber));
        } else {
            sb.insert(i + this.pos, this.numberFormat.format(transformNumber(j)));
        }
    }

    public void doSubstitution(double d, StringBuilder sb, int i, int i2) {
        NFRuleSet nFRuleSet;
        double transformNumber = transformNumber(d);
        if (Double.isInfinite(transformNumber)) {
            this.ruleSet.findRule(Double.POSITIVE_INFINITY).doFormat(transformNumber, sb, i + this.pos, i2);
        } else if (transformNumber != Math.floor(transformNumber) || (nFRuleSet = this.ruleSet) == null) {
            NFRuleSet nFRuleSet2 = this.ruleSet;
            if (nFRuleSet2 != null) {
                nFRuleSet2.format(transformNumber, sb, i + this.pos, i2);
            } else {
                sb.insert(i + this.pos, this.numberFormat.format(transformNumber));
            }
        } else {
            nFRuleSet.format((long) transformNumber, sb, i + this.pos, i2);
        }
    }

    public Number doParse(String str, ParsePosition parsePosition, double d, double d2, boolean z, int i) {
        Number number;
        double calcUpperBound = calcUpperBound(d2);
        NFRuleSet nFRuleSet = this.ruleSet;
        if (nFRuleSet != null) {
            number = nFRuleSet.parse(str, parsePosition, calcUpperBound, i);
            if (z && !this.ruleSet.isFractionSet() && parsePosition.getIndex() == 0) {
                number = this.ruleSet.owner.getDecimalFormat().parse(str, parsePosition);
            }
        } else {
            number = this.numberFormat.parse(str, parsePosition);
        }
        if (parsePosition.getIndex() == 0) {
            return number;
        }
        double composeRuleValue = composeRuleValue(number.doubleValue(), d);
        long j = (long) composeRuleValue;
        if (composeRuleValue == ((double) j)) {
            return Long.valueOf(j);
        }
        return new Double(composeRuleValue);
    }

    public final int getPos() {
        return this.pos;
    }

    public void setDecimalFormatSymbols(DecimalFormatSymbols decimalFormatSymbols) {
        DecimalFormat decimalFormat = this.numberFormat;
        if (decimalFormat != null) {
            decimalFormat.setDecimalFormatSymbols(decimalFormatSymbols);
        }
    }
}
