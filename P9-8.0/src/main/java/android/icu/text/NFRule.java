package android.icu.text;

import android.icu.impl.PatternProps;
import android.icu.impl.PatternTokenizer;
import android.icu.impl.Utility;
import android.icu.text.PluralRules.PluralType;
import android.icu.util.ULocale;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.util.List;

final class NFRule {
    static final /* synthetic */ boolean -assertionsDisabled = (NFRule.class.desiredAssertionStatus() ^ 1);
    static final int IMPROPER_FRACTION_RULE = -2;
    static final int INFINITY_RULE = -5;
    static final int MASTER_RULE = -4;
    static final int NAN_RULE = -6;
    static final int NEGATIVE_NUMBER_RULE = -1;
    static final int PROPER_FRACTION_RULE = -3;
    private static final String[] RULE_PREFIXES = new String[]{"<<", "<%", "<#", "<0", ">>", ">%", ">#", ">0", "=%", "=#", "=0"};
    static final Long ZERO = Long.valueOf(0);
    private long baseValue;
    private char decimalPoint = 0;
    private short exponent = (short) 0;
    private final RuleBasedNumberFormat formatter;
    private int radix = 10;
    private PluralFormat rulePatternFormat = null;
    private String ruleText = null;
    private NFSubstitution sub1 = null;
    private NFSubstitution sub2 = null;

    public static void makeRules(String description, NFRuleSet owner, NFRule predecessor, RuleBasedNumberFormat ownersOwner, List<NFRule> returnList) {
        NFRule rule1 = new NFRule(ownersOwner, description);
        description = rule1.ruleText;
        int brack1 = description.indexOf(91);
        int brack2 = brack1 < 0 ? -1 : description.indexOf(93);
        if (brack2 < 0 || brack1 > brack2 || rule1.baseValue == -3 || rule1.baseValue == -1 || rule1.baseValue == -5 || rule1.baseValue == -6) {
            rule1.extractSubstitutions(owner, description, predecessor);
        } else {
            NFRule rule2 = null;
            StringBuilder sbuf = new StringBuilder();
            if ((rule1.baseValue > 0 && rule1.baseValue % power((long) rule1.radix, rule1.exponent) == 0) || rule1.baseValue == -2 || rule1.baseValue == -4) {
                rule2 = new NFRule(ownersOwner, null);
                if (rule1.baseValue >= 0) {
                    rule2.baseValue = rule1.baseValue;
                    if (!owner.isFractionSet()) {
                        rule1.baseValue++;
                    }
                } else if (rule1.baseValue == -2) {
                    rule2.baseValue = -3;
                } else if (rule1.baseValue == -4) {
                    rule2.baseValue = rule1.baseValue;
                    rule1.baseValue = -2;
                }
                rule2.radix = rule1.radix;
                rule2.exponent = rule1.exponent;
                sbuf.append(description.substring(0, brack1));
                if (brack2 + 1 < description.length()) {
                    sbuf.append(description.substring(brack2 + 1));
                }
                rule2.extractSubstitutions(owner, sbuf.toString(), predecessor);
            }
            sbuf.setLength(0);
            sbuf.append(description.substring(0, brack1));
            sbuf.append(description.substring(brack1 + 1, brack2));
            if (brack2 + 1 < description.length()) {
                sbuf.append(description.substring(brack2 + 1));
            }
            rule1.extractSubstitutions(owner, sbuf.toString(), predecessor);
            if (rule2 != null) {
                if (rule2.baseValue >= 0) {
                    returnList.add(rule2);
                } else {
                    owner.setNonNumericalRule(rule2);
                }
            }
        }
        if (rule1.baseValue >= 0) {
            returnList.add(rule1);
        } else {
            owner.setNonNumericalRule(rule1);
        }
    }

    public NFRule(RuleBasedNumberFormat formatter, String ruleText) {
        String str = null;
        this.formatter = formatter;
        if (ruleText != null) {
            str = parseRuleDescriptor(ruleText);
        }
        this.ruleText = str;
    }

    private String parseRuleDescriptor(String description) {
        int p = description.indexOf(":");
        if (p != -1) {
            int descriptorLength;
            char firstChar;
            char lastChar;
            String descriptor = description.substring(0, p);
            while (true) {
                p++;
                if (p >= description.length() || !PatternProps.isWhiteSpace(description.charAt(p))) {
                    description = description.substring(p);
                    descriptorLength = descriptor.length();
                    firstChar = descriptor.charAt(0);
                    lastChar = descriptor.charAt(descriptorLength - 1);
                }
            }
            description = description.substring(p);
            descriptorLength = descriptor.length();
            firstChar = descriptor.charAt(0);
            lastChar = descriptor.charAt(descriptorLength - 1);
            if (firstChar >= '0' && firstChar <= '9' && lastChar != ULocale.PRIVATE_USE_EXTENSION) {
                long tempValue = 0;
                char c = 0;
                p = 0;
                while (p < descriptorLength) {
                    c = descriptor.charAt(p);
                    if (c < '0' || c > '9') {
                        if (c == '/' || c == '>') {
                            break;
                        } else if (!(PatternProps.isWhiteSpace(c) || c == ',' || c == '.')) {
                            throw new IllegalArgumentException("Illegal character " + c + " in rule descriptor");
                        }
                    }
                    tempValue = (10 * tempValue) + ((long) (c - 48));
                    p++;
                }
                setBaseValue(tempValue);
                if (c == '/') {
                    tempValue = 0;
                    p++;
                    while (p < descriptorLength) {
                        c = descriptor.charAt(p);
                        if (c >= '0' && c <= '9') {
                            tempValue = (10 * tempValue) + ((long) (c - 48));
                        } else if (c == '>') {
                            break;
                        } else if (!(PatternProps.isWhiteSpace(c) || c == ',' || c == '.')) {
                            throw new IllegalArgumentException("Illegal character " + c + " in rule descriptor");
                        }
                        p++;
                    }
                    this.radix = (int) tempValue;
                    if (this.radix == 0) {
                        throw new IllegalArgumentException("Rule can't have radix of 0");
                    }
                    this.exponent = expectedExponent();
                }
                if (c == '>') {
                    for (p = 
/*
Method generation error in method: android.icu.text.NFRule.parseRuleDescriptor(java.lang.String):java.lang.String, dex: 
jadx.core.utils.exceptions.CodegenException: Error generate insn: PHI: (r5_9 'p' int) = (r5_4 'p' int), (r5_7 'p' int) binds: {(r5_4 'p' int)=B:27:0x006d, (r5_7 'p' int)=B:58:0x00fe} in method: android.icu.text.NFRule.parseRuleDescriptor(java.lang.String):java.lang.String, dex: 
	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:228)
	at jadx.core.codegen.RegionGen.makeLoop(RegionGen.java:183)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:61)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:87)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:53)
	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:93)
	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:118)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:57)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:87)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:53)
	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:93)
	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:118)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:57)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:87)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:53)
	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:93)
	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:118)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:57)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:87)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:53)
	at jadx.core.codegen.MethodGen.addInstructions(MethodGen.java:173)
	at jadx.core.codegen.ClassGen.addMethod(ClassGen.java:322)
	at jadx.core.codegen.ClassGen.addMethods(ClassGen.java:260)
	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:222)
	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:112)
	at jadx.core.codegen.ClassGen.makeClass(ClassGen.java:78)
	at jadx.core.codegen.CodeGen.visit(CodeGen.java:10)
	at jadx.core.ProcessClass.process(ProcessClass.java:38)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
Caused by: jadx.core.utils.exceptions.CodegenException: PHI can be used only in fallback mode
	at jadx.core.codegen.InsnGen.fallbackOnlyInsn(InsnGen.java:539)
	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:511)
	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:222)
	... 30 more

*/

    private void extractSubstitutions(NFRuleSet owner, String ruleText, NFRule predecessor) {
        this.ruleText = ruleText;
        this.sub1 = extractSubstitution(owner, predecessor);
        if (this.sub1 == null) {
            this.sub2 = null;
        } else {
            this.sub2 = extractSubstitution(owner, predecessor);
        }
        ruleText = this.ruleText;
        int pluralRuleStart = ruleText.indexOf("$(");
        int pluralRuleEnd = pluralRuleStart >= 0 ? ruleText.indexOf(")$", pluralRuleStart) : -1;
        if (pluralRuleEnd >= 0) {
            int endType = ruleText.indexOf(44, pluralRuleStart);
            if (endType < 0) {
                throw new IllegalArgumentException("Rule \"" + ruleText + "\" does not have a defined type");
            }
            PluralType pluralType;
            String type = this.ruleText.substring(pluralRuleStart + 2, endType);
            if ("cardinal".equals(type)) {
                pluralType = PluralType.CARDINAL;
            } else if ("ordinal".equals(type)) {
                pluralType = PluralType.ORDINAL;
            } else {
                throw new IllegalArgumentException(type + " is an unknown type");
            }
            this.rulePatternFormat = this.formatter.createPluralFormat(pluralType, ruleText.substring(endType + 1, pluralRuleEnd));
        }
    }

    private NFSubstitution extractSubstitution(NFRuleSet owner, NFRule predecessor) {
        int subStart = indexOfAnyRulePrefix(this.ruleText);
        if (subStart == -1) {
            return null;
        }
        int subEnd;
        if (this.ruleText.startsWith(">>>", subStart)) {
            subEnd = subStart + 2;
        } else {
            char c = this.ruleText.charAt(subStart);
            subEnd = this.ruleText.indexOf(c, subStart + 1);
            if (c == '<' && subEnd != -1 && subEnd < this.ruleText.length() - 1 && this.ruleText.charAt(subEnd + 1) == c) {
                subEnd++;
            }
        }
        if (subEnd == -1) {
            return null;
        }
        NFSubstitution result = NFSubstitution.makeSubstitution(subStart, this, predecessor, owner, this.formatter, this.ruleText.substring(subStart, subEnd + 1));
        this.ruleText = this.ruleText.substring(0, subStart) + this.ruleText.substring(subEnd + 1);
        return result;
    }

    final void setBaseValue(long newBaseValue) {
        this.baseValue = newBaseValue;
        this.radix = 10;
        if (this.baseValue >= 1) {
            this.exponent = expectedExponent();
            if (this.sub1 != null) {
                this.sub1.setDivisor(this.radix, this.exponent);
            }
            if (this.sub2 != null) {
                this.sub2.setDivisor(this.radix, this.exponent);
                return;
            }
            return;
        }
        this.exponent = (short) 0;
    }

    private short expectedExponent() {
        if (this.radix == 0 || this.baseValue < 1) {
            return (short) 0;
        }
        short tempResult = (short) ((int) (Math.log((double) this.baseValue) / Math.log((double) this.radix)));
        if (power((long) this.radix, (short) (tempResult + 1)) <= this.baseValue) {
            return (short) (tempResult + 1);
        }
        return tempResult;
    }

    private static int indexOfAnyRulePrefix(String ruleText) {
        int result = -1;
        if (ruleText.length() > 0) {
            for (String string : RULE_PREFIXES) {
                int pos = ruleText.indexOf(string);
                if (pos != -1 && (result == -1 || pos < result)) {
                    result = pos;
                }
            }
        }
        return result;
    }

    public boolean equals(Object that) {
        boolean z = false;
        if (!(that instanceof NFRule)) {
            return false;
        }
        NFRule that2 = (NFRule) that;
        if (this.baseValue == that2.baseValue && this.radix == that2.radix && this.exponent == that2.exponent && this.ruleText.equals(that2.ruleText) && Utility.objectEquals(this.sub1, that2.sub1)) {
            z = Utility.objectEquals(this.sub2, that2.sub2);
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
        char c = '.';
        StringBuilder result = new StringBuilder();
        StringBuilder append;
        if (this.baseValue == -1) {
            result.append("-x: ");
        } else if (this.baseValue == -2) {
            append = result.append(ULocale.PRIVATE_USE_EXTENSION);
            if (this.decimalPoint != 0) {
                c = this.decimalPoint;
            }
            append.append(c).append("x: ");
        } else if (this.baseValue == -3) {
            append = result.append('0');
            if (this.decimalPoint != 0) {
                c = this.decimalPoint;
            }
            append.append(c).append("x: ");
        } else if (this.baseValue == -4) {
            append = result.append(ULocale.PRIVATE_USE_EXTENSION);
            if (this.decimalPoint != 0) {
                c = this.decimalPoint;
            }
            append.append(c).append("0: ");
        } else if (this.baseValue == -5) {
            result.append("Inf: ");
        } else if (this.baseValue == -6) {
            result.append("NaN: ");
        } else {
            result.append(String.valueOf(this.baseValue));
            if (this.radix != 10) {
                result.append('/').append(this.radix);
            }
            int numCarets = expectedExponent() - this.exponent;
            for (int i = 0; i < numCarets; i++) {
                result.append('>');
            }
            result.append(PluralRules.KEYWORD_RULE_SEPARATOR);
        }
        if (this.ruleText.startsWith(" ") && (this.sub1 == null || this.sub1.getPos() != 0)) {
            result.append(PatternTokenizer.SINGLE_QUOTE);
        }
        StringBuilder ruleTextCopy = new StringBuilder(this.ruleText);
        if (this.sub2 != null) {
            ruleTextCopy.insert(this.sub2.getPos(), this.sub2.toString());
        }
        if (this.sub1 != null) {
            ruleTextCopy.insert(this.sub1.getPos(), this.sub1.toString());
        }
        result.append(ruleTextCopy.toString());
        result.append(';');
        return result.toString();
    }

    public final char getDecimalPoint() {
        return this.decimalPoint;
    }

    public final long getBaseValue() {
        return this.baseValue;
    }

    public long getDivisor() {
        return power((long) this.radix, this.exponent);
    }

    public void doFormat(long number, StringBuilder toInsertInto, int pos, int recursionCount) {
        int pluralRuleStart = this.ruleText.length();
        int lengthOffset = 0;
        if (this.rulePatternFormat == null) {
            toInsertInto.insert(pos, this.ruleText);
        } else {
            pluralRuleStart = this.ruleText.indexOf("$(");
            int pluralRuleEnd = this.ruleText.indexOf(")$", pluralRuleStart);
            int initialLength = toInsertInto.length();
            if (pluralRuleEnd < this.ruleText.length() - 1) {
                toInsertInto.insert(pos, this.ruleText.substring(pluralRuleEnd + 2));
            }
            toInsertInto.insert(pos, this.rulePatternFormat.format((double) (number / power((long) this.radix, this.exponent))));
            if (pluralRuleStart > 0) {
                toInsertInto.insert(pos, this.ruleText.substring(0, pluralRuleStart));
            }
            lengthOffset = this.ruleText.length() - (toInsertInto.length() - initialLength);
        }
        if (this.sub2 != null) {
            this.sub2.doSubstitution(number, toInsertInto, pos - (this.sub2.getPos() > pluralRuleStart ? lengthOffset : 0), recursionCount);
        }
        if (this.sub1 != null) {
            NFSubstitution nFSubstitution = this.sub1;
            if (this.sub1.getPos() <= pluralRuleStart) {
                lengthOffset = 0;
            }
            nFSubstitution.doSubstitution(number, toInsertInto, pos - lengthOffset, recursionCount);
        }
    }

    public void doFormat(double number, StringBuilder toInsertInto, int pos, int recursionCount) {
        int pluralRuleStart = this.ruleText.length();
        int lengthOffset = 0;
        if (this.rulePatternFormat == null) {
            toInsertInto.insert(pos, this.ruleText);
        } else {
            pluralRuleStart = this.ruleText.indexOf("$(");
            int pluralRuleEnd = this.ruleText.indexOf(")$", pluralRuleStart);
            int initialLength = toInsertInto.length();
            if (pluralRuleEnd < this.ruleText.length() - 1) {
                toInsertInto.insert(pos, this.ruleText.substring(pluralRuleEnd + 2));
            }
            double pluralVal = number;
            if (0.0d > number || number >= 1.0d) {
                pluralVal = number / ((double) power((long) this.radix, this.exponent));
            } else {
                pluralVal = (double) Math.round(((double) power((long) this.radix, this.exponent)) * number);
            }
            toInsertInto.insert(pos, this.rulePatternFormat.format((double) ((long) pluralVal)));
            if (pluralRuleStart > 0) {
                toInsertInto.insert(pos, this.ruleText.substring(0, pluralRuleStart));
            }
            lengthOffset = this.ruleText.length() - (toInsertInto.length() - initialLength);
        }
        if (this.sub2 != null) {
            this.sub2.doSubstitution(number, toInsertInto, pos - (this.sub2.getPos() > pluralRuleStart ? lengthOffset : 0), recursionCount);
        }
        if (this.sub1 != null) {
            NFSubstitution nFSubstitution = this.sub1;
            if (this.sub1.getPos() <= pluralRuleStart) {
                lengthOffset = 0;
            }
            nFSubstitution.doSubstitution(number, toInsertInto, pos - lengthOffset, recursionCount);
        }
    }

    static long power(long base, short exponent) {
        if (exponent < (short) 0) {
            throw new IllegalArgumentException("Exponent can not be negative");
        } else if (base < 0) {
            throw new IllegalArgumentException("Base can not be negative");
        } else {
            long result = 1;
            while (exponent > (short) 0) {
                if ((exponent & 1) == 1) {
                    result *= base;
                }
                base *= base;
                exponent = (short) (exponent >> 1);
            }
            return result;
        }
    }

    public boolean shouldRollBack(long number) {
        boolean z = true;
        boolean isModulusSubstitution = (this.sub1 == null || !this.sub1.isModulusSubstitution()) ? this.sub2 != null ? this.sub2.isModulusSubstitution() : false : true;
        if (!isModulusSubstitution) {
            return false;
        }
        long divisor = power((long) this.radix, this.exponent);
        if (number % divisor != 0 || this.baseValue % divisor == 0) {
            z = false;
        }
        return z;
    }

    public Number doParse(String text, ParsePosition parsePosition, boolean isFractionRule, double upperBound) {
        ParsePosition pp = new ParsePosition(0);
        int sub1Pos = this.sub1 != null ? this.sub1.getPos() : this.ruleText.length();
        int sub2Pos = this.sub2 != null ? this.sub2.getPos() : this.ruleText.length();
        String workText = stripPrefix(text, this.ruleText.substring(0, sub1Pos), pp);
        int prefixLength = text.length() - workText.length();
        if (pp.getIndex() == 0 && sub1Pos != 0) {
            return ZERO;
        }
        if (this.baseValue == -5) {
            parsePosition.setIndex(pp.getIndex());
            return Double.valueOf(Double.POSITIVE_INFINITY);
        } else if (this.baseValue == -6) {
            parsePosition.setIndex(pp.getIndex());
            return Double.valueOf(Double.NaN);
        } else {
            int highWaterMark = 0;
            double result = 0.0d;
            int start = 0;
            double tempBaseValue = (double) Math.max(0, this.baseValue);
            do {
                pp.setIndex(0);
                double partialResult = matchToDelimiter(workText, start, tempBaseValue, this.ruleText.substring(sub1Pos, sub2Pos), this.rulePatternFormat, pp, this.sub1, upperBound).doubleValue();
                if (pp.getIndex() != 0 || this.sub1 == null) {
                    start = pp.getIndex();
                    String workText2 = workText.substring(pp.getIndex());
                    ParsePosition parsePosition2 = new ParsePosition(0);
                    partialResult = matchToDelimiter(workText2, 0, partialResult, this.ruleText.substring(sub2Pos), this.rulePatternFormat, parsePosition2, this.sub2, upperBound).doubleValue();
                    if ((parsePosition2.getIndex() != 0 || this.sub2 == null) && (pp.getIndex() + prefixLength) + parsePosition2.getIndex() > highWaterMark) {
                        highWaterMark = (pp.getIndex() + prefixLength) + parsePosition2.getIndex();
                        result = partialResult;
                    }
                }
                if (sub1Pos == sub2Pos || pp.getIndex() <= 0 || pp.getIndex() >= workText.length()) {
                    parsePosition.setIndex(highWaterMark);
                }
            } while (pp.getIndex() != start);
            parsePosition.setIndex(highWaterMark);
            if (isFractionRule && highWaterMark > 0 && this.sub1 == null) {
                result = 1.0d / result;
            }
            if (result == ((double) ((long) result))) {
                return Long.valueOf((long) result);
            }
            return new Double(result);
        }
    }

    private String stripPrefix(String text, String prefix, ParsePosition pp) {
        if (prefix.length() == 0) {
            return text;
        }
        int pfl = prefixLength(text, prefix);
        if (pfl == 0) {
            return text;
        }
        pp.setIndex(pp.getIndex() + pfl);
        return text.substring(pfl);
    }

    private Number matchToDelimiter(String text, int startPos, double baseVal, String delimiter, PluralFormat pluralFormatDelimiter, ParsePosition pp, NFSubstitution sub, double upperBound) {
        ParsePosition tempPP;
        Number tempResult;
        if (!allIgnorable(delimiter)) {
            tempPP = new ParsePosition(0);
            int[] temp = findText(text, delimiter, pluralFormatDelimiter, startPos);
            int dPos = temp[0];
            int dLen = temp[1];
            while (dPos >= 0) {
                String subText = text.substring(0, dPos);
                if (subText.length() > 0) {
                    tempResult = sub.doParse(subText, tempPP, baseVal, upperBound, this.formatter.lenientParseEnabled());
                    if (tempPP.getIndex() == dPos) {
                        pp.setIndex(dPos + dLen);
                        return tempResult;
                    }
                }
                tempPP.setIndex(0);
                temp = findText(text, delimiter, pluralFormatDelimiter, dPos + dLen);
                dPos = temp[0];
                dLen = temp[1];
            }
            pp.setIndex(0);
            return ZERO;
        } else if (sub == null) {
            return Double.valueOf(baseVal);
        } else {
            tempPP = new ParsePosition(0);
            Number result = ZERO;
            tempResult = sub.doParse(text, tempPP, baseVal, upperBound, this.formatter.lenientParseEnabled());
            if (tempPP.getIndex() != 0) {
                pp.setIndex(tempPP.getIndex());
                if (tempResult != null) {
                    result = tempResult;
                }
            }
            return result;
        }
    }

    private int prefixLength(String str, String prefix) {
        if (prefix.length() == 0) {
            return 0;
        }
        RbnfLenientScanner scanner = this.formatter.getLenientScanner();
        if (scanner != null) {
            return scanner.prefixLength(str, prefix);
        }
        if (str.startsWith(prefix)) {
            return prefix.length();
        }
        return 0;
    }

    private int[] findText(String str, String key, PluralFormat pluralFormatKey, int startingAt) {
        RbnfLenientScanner scanner = this.formatter.getLenientScanner();
        if (pluralFormatKey != null) {
            FieldPosition position = new FieldPosition(0);
            position.setBeginIndex(startingAt);
            pluralFormatKey.parseType(str, scanner, position);
            int start = position.getBeginIndex();
            if (start >= 0) {
                int pluralRuleStart = this.ruleText.indexOf("$(");
                int pluralRuleSuffix = this.ruleText.indexOf(")$", pluralRuleStart) + 2;
                int matchLen = position.getEndIndex() - start;
                String prefix = this.ruleText.substring(0, pluralRuleStart);
                String suffix = this.ruleText.substring(pluralRuleSuffix);
                if (str.regionMatches(start - prefix.length(), prefix, 0, prefix.length()) && str.regionMatches(start + matchLen, suffix, 0, suffix.length())) {
                    return new int[]{start - prefix.length(), (prefix.length() + matchLen) + suffix.length()};
                }
            }
            return new int[]{-1, 0};
        } else if (scanner != null) {
            return scanner.findText(str, key, startingAt);
        } else {
            return new int[]{str.indexOf(key, startingAt), key.length()};
        }
    }

    private boolean allIgnorable(String str) {
        boolean z = false;
        if (str == null || str.length() == 0) {
            return true;
        }
        RbnfLenientScanner scanner = this.formatter.getLenientScanner();
        if (scanner != null) {
            z = scanner.allIgnorable(str);
        }
        return z;
    }

    public void setDecimalFormatSymbols(DecimalFormatSymbols newSymbols) {
        if (this.sub1 != null) {
            this.sub1.setDecimalFormatSymbols(newSymbols);
        }
        if (this.sub2 != null) {
            this.sub2.setDecimalFormatSymbols(newSymbols);
        }
    }
}
