package ohos.global.icu.text;

import java.text.FieldPosition;
import java.text.ParsePosition;
import java.util.List;
import java.util.Objects;
import ohos.global.icu.impl.PatternProps;
import ohos.global.icu.text.PluralRules;
import ohos.global.icu.util.ULocale;
import ohos.telephony.TelephoneNumberUtils;

/* access modifiers changed from: package-private */
public final class NFRule {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    static final int IMPROPER_FRACTION_RULE = -2;
    static final int INFINITY_RULE = -5;
    static final int MASTER_RULE = -4;
    static final int NAN_RULE = -6;
    static final int NEGATIVE_NUMBER_RULE = -1;
    static final int PROPER_FRACTION_RULE = -3;
    private static final String[] RULE_PREFIXES = {"<<", "<%", "<#", "<0", ">>", ">%", ">#", ">0", "=%", "=#", "=0"};
    static final Long ZERO = 0L;
    private long baseValue;
    private char decimalPoint = 0;
    private short exponent = 0;
    private final RuleBasedNumberFormat formatter;
    private int radix = 10;
    private PluralFormat rulePatternFormat;
    private String ruleText;
    private NFSubstitution sub1;
    private NFSubstitution sub2;

    public int hashCode() {
        return 42;
    }

    /* JADX WARNING: Removed duplicated region for block: B:41:0x00e1  */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x00f1  */
    /* JADX WARNING: Removed duplicated region for block: B:51:0x010c  */
    /* JADX WARNING: Removed duplicated region for block: B:52:0x0110  */
    public static void makeRules(String str, NFRuleSet nFRuleSet, NFRule nFRule, RuleBasedNumberFormat ruleBasedNumberFormat, List<NFRule> list) {
        int i;
        NFRule nFRule2;
        int i2;
        int i3;
        NFRule nFRule3 = new NFRule(ruleBasedNumberFormat, str);
        String str2 = nFRule3.ruleText;
        int indexOf = str2.indexOf(91);
        if (indexOf < 0) {
            i = -1;
        } else {
            i = str2.indexOf(93);
        }
        if (i >= 0 && indexOf <= i) {
            long j = nFRule3.baseValue;
            if (!(j == -3 || j == -1 || j == -5 || j == -6)) {
                StringBuilder sb = new StringBuilder();
                long j2 = nFRule3.baseValue;
                if (j2 <= 0 || j2 % power((long) nFRule3.radix, nFRule3.exponent) != 0) {
                    long j3 = nFRule3.baseValue;
                    if (!(j3 == -2 || j3 == -4)) {
                        i2 = 0;
                        nFRule2 = null;
                        sb.setLength(i2);
                        sb.append(str2.substring(i2, indexOf));
                        sb.append(str2.substring(indexOf + 1, i));
                        i3 = i + 1;
                        if (i3 < str2.length()) {
                            sb.append(str2.substring(i3));
                        }
                        nFRule3.extractSubstitutions(nFRuleSet, sb.toString(), nFRule);
                        if (nFRule2 != null) {
                            if (nFRule2.baseValue >= 0) {
                                list.add(nFRule2);
                            } else {
                                nFRuleSet.setNonNumericalRule(nFRule2);
                            }
                        }
                        if (nFRule3.baseValue >= 0) {
                            list.add(nFRule3);
                            return;
                        } else {
                            nFRuleSet.setNonNumericalRule(nFRule3);
                            return;
                        }
                    }
                }
                nFRule2 = new NFRule(ruleBasedNumberFormat, null);
                long j4 = nFRule3.baseValue;
                if (j4 >= 0) {
                    nFRule2.baseValue = j4;
                    if (!nFRuleSet.isFractionSet()) {
                        nFRule3.baseValue++;
                    }
                } else if (j4 == -2) {
                    nFRule2.baseValue = -3;
                } else if (j4 == -4) {
                    nFRule2.baseValue = j4;
                    nFRule3.baseValue = -2;
                }
                nFRule2.radix = nFRule3.radix;
                nFRule2.exponent = nFRule3.exponent;
                sb.append(str2.substring(0, indexOf));
                int i4 = i + 1;
                if (i4 < str2.length()) {
                    sb.append(str2.substring(i4));
                }
                nFRule2.extractSubstitutions(nFRuleSet, sb.toString(), nFRule);
                i2 = 0;
                sb.setLength(i2);
                sb.append(str2.substring(i2, indexOf));
                sb.append(str2.substring(indexOf + 1, i));
                i3 = i + 1;
                if (i3 < str2.length()) {
                }
                nFRule3.extractSubstitutions(nFRuleSet, sb.toString(), nFRule);
                if (nFRule2 != null) {
                }
                if (nFRule3.baseValue >= 0) {
                }
            }
        }
        nFRule3.extractSubstitutions(nFRuleSet, str2, nFRule);
        if (nFRule3.baseValue >= 0) {
        }
    }

    public NFRule(RuleBasedNumberFormat ruleBasedNumberFormat, String str) {
        String str2 = null;
        this.ruleText = null;
        this.rulePatternFormat = null;
        this.sub1 = null;
        this.sub2 = null;
        this.formatter = ruleBasedNumberFormat;
        this.ruleText = str != null ? parseRuleDescriptor(str) : str2;
    }

    private String parseRuleDescriptor(String str) {
        short s;
        String str2 = str;
        int indexOf = str2.indexOf(":");
        if (indexOf != -1) {
            String substring = str2.substring(0, indexOf);
            int i = indexOf + 1;
            while (i < str.length() && PatternProps.isWhiteSpace(str2.charAt(i))) {
                i++;
            }
            str2 = str2.substring(i);
            int length = substring.length();
            char charAt = substring.charAt(0);
            char charAt2 = substring.charAt(length - 1);
            char c = '0';
            if (charAt >= '0') {
                char c2 = '9';
                if (charAt <= '9' && charAt2 != 'x') {
                    int i2 = 0;
                    char c3 = 0;
                    long j = 0;
                    while (i2 < length) {
                        c3 = substring.charAt(i2);
                        if (c3 < '0' || c3 > '9') {
                            if (c3 == '/' || c3 == '>') {
                                break;
                            } else if (!(PatternProps.isWhiteSpace(c3) || c3 == ',' || c3 == '.')) {
                                throw new IllegalArgumentException("Illegal character " + c3 + " in rule descriptor");
                            }
                        } else {
                            j = (j * 10) + ((long) (c3 - '0'));
                        }
                        i2++;
                    }
                    setBaseValue(j);
                    if (c3 == '/') {
                        i2++;
                        long j2 = 0;
                        while (i2 < length) {
                            c3 = substring.charAt(i2);
                            if (c3 >= c && c3 <= c2) {
                                j2 = (j2 * 10) + ((long) (c3 - '0'));
                            } else if (c3 == '>') {
                                break;
                            } else if (!(PatternProps.isWhiteSpace(c3) || c3 == ',' || c3 == '.')) {
                                throw new IllegalArgumentException("Illegal character " + c3 + " in rule descriptor");
                            }
                            i2++;
                            c = '0';
                            c2 = '9';
                        }
                        this.radix = (int) j2;
                        if (this.radix != 0) {
                            this.exponent = expectedExponent();
                        } else {
                            throw new IllegalArgumentException("Rule can't have radix of 0");
                        }
                    }
                    if (c3 == '>') {
                        while (i2 < length) {
                            if (substring.charAt(i2) != '>' || (s = this.exponent) <= 0) {
                                throw new IllegalArgumentException("Illegal character in rule descriptor");
                            }
                            this.exponent = (short) (s - 1);
                            i2++;
                        }
                    }
                }
            }
            if (substring.equals("-x")) {
                setBaseValue(-1);
            } else if (length == 3) {
                if (charAt == '0' && charAt2 == 'x') {
                    setBaseValue(-3);
                    this.decimalPoint = substring.charAt(1);
                } else if (charAt == 'x' && charAt2 == 'x') {
                    setBaseValue(-2);
                    this.decimalPoint = substring.charAt(1);
                } else if (charAt == 'x' && charAt2 == '0') {
                    setBaseValue(-4);
                    this.decimalPoint = substring.charAt(1);
                } else if (substring.equals("NaN")) {
                    setBaseValue(-6);
                } else if (substring.equals("Inf")) {
                    setBaseValue(-5);
                }
            }
        }
        return (str2.length() <= 0 || str2.charAt(0) != '\'') ? str2 : str2.substring(1);
    }

    private void extractSubstitutions(NFRuleSet nFRuleSet, String str, NFRule nFRule) {
        PluralRules.PluralType pluralType;
        this.ruleText = str;
        this.sub1 = extractSubstitution(nFRuleSet, nFRule);
        if (this.sub1 == null) {
            this.sub2 = null;
        } else {
            this.sub2 = extractSubstitution(nFRuleSet, nFRule);
        }
        String str2 = this.ruleText;
        int indexOf = str2.indexOf("$(");
        int indexOf2 = indexOf >= 0 ? str2.indexOf(")$", indexOf) : -1;
        if (indexOf2 >= 0) {
            int indexOf3 = str2.indexOf(44, indexOf);
            if (indexOf3 >= 0) {
                String substring = this.ruleText.substring(indexOf + 2, indexOf3);
                if ("cardinal".equals(substring)) {
                    pluralType = PluralRules.PluralType.CARDINAL;
                } else if ("ordinal".equals(substring)) {
                    pluralType = PluralRules.PluralType.ORDINAL;
                } else {
                    throw new IllegalArgumentException(substring + " is an unknown type");
                }
                this.rulePatternFormat = this.formatter.createPluralFormat(pluralType, str2.substring(indexOf3 + 1, indexOf2));
                return;
            }
            throw new IllegalArgumentException("Rule \"" + str2 + "\" does not have a defined type");
        }
    }

    private NFSubstitution extractSubstitution(NFRuleSet nFRuleSet, NFRule nFRule) {
        int i;
        int indexOfAnyRulePrefix = indexOfAnyRulePrefix(this.ruleText);
        if (indexOfAnyRulePrefix == -1) {
            return null;
        }
        if (this.ruleText.startsWith(">>>", indexOfAnyRulePrefix)) {
            i = indexOfAnyRulePrefix + 2;
        } else {
            char charAt = this.ruleText.charAt(indexOfAnyRulePrefix);
            int indexOf = this.ruleText.indexOf(charAt, indexOfAnyRulePrefix + 1);
            if (charAt == '<' && indexOf != -1 && indexOf < this.ruleText.length() - 1) {
                int i2 = indexOf + 1;
                if (this.ruleText.charAt(i2) == charAt) {
                    i = i2;
                }
            }
            i = indexOf;
        }
        if (i == -1) {
            return null;
        }
        int i3 = i + 1;
        NFSubstitution makeSubstitution = NFSubstitution.makeSubstitution(indexOfAnyRulePrefix, this, nFRule, nFRuleSet, this.formatter, this.ruleText.substring(indexOfAnyRulePrefix, i3));
        this.ruleText = this.ruleText.substring(0, indexOfAnyRulePrefix) + this.ruleText.substring(i3);
        return makeSubstitution;
    }

    /* access modifiers changed from: package-private */
    public final void setBaseValue(long j) {
        this.baseValue = j;
        this.radix = 10;
        if (this.baseValue >= 1) {
            this.exponent = expectedExponent();
            NFSubstitution nFSubstitution = this.sub1;
            if (nFSubstitution != null) {
                nFSubstitution.setDivisor(this.radix, this.exponent);
            }
            NFSubstitution nFSubstitution2 = this.sub2;
            if (nFSubstitution2 != null) {
                nFSubstitution2.setDivisor(this.radix, this.exponent);
                return;
            }
            return;
        }
        this.exponent = 0;
    }

    private short expectedExponent() {
        if (this.radix == 0) {
            return 0;
        }
        long j = this.baseValue;
        if (j < 1) {
            return 0;
        }
        short log = (short) ((int) (Math.log((double) j) / Math.log((double) this.radix)));
        short s = (short) (log + 1);
        return power((long) this.radix, s) <= this.baseValue ? s : log;
    }

    private static int indexOfAnyRulePrefix(String str) {
        if (str.length() <= 0) {
            return -1;
        }
        int i = -1;
        for (String str2 : RULE_PREFIXES) {
            int indexOf = str.indexOf(str2);
            if (indexOf != -1 && (i == -1 || indexOf < i)) {
                i = indexOf;
            }
        }
        return i;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof NFRule)) {
            return false;
        }
        NFRule nFRule = (NFRule) obj;
        if (this.baseValue != nFRule.baseValue || this.radix != nFRule.radix || this.exponent != nFRule.exponent || !this.ruleText.equals(nFRule.ruleText) || !Objects.equals(this.sub1, nFRule.sub1) || !Objects.equals(this.sub2, nFRule.sub2)) {
            return false;
        }
        return true;
    }

    public String toString() {
        NFSubstitution nFSubstitution;
        StringBuilder sb = new StringBuilder();
        long j = this.baseValue;
        if (j == -1) {
            sb.append("-x: ");
        } else if (j == -2) {
            sb.append(ULocale.PRIVATE_USE_EXTENSION);
            char c = this.decimalPoint;
            if (c == 0) {
                c = '.';
            }
            sb.append(c);
            sb.append("x: ");
        } else if (j == -3) {
            sb.append('0');
            char c2 = this.decimalPoint;
            if (c2 == 0) {
                c2 = '.';
            }
            sb.append(c2);
            sb.append("x: ");
        } else if (j == -4) {
            sb.append(ULocale.PRIVATE_USE_EXTENSION);
            char c3 = this.decimalPoint;
            if (c3 == 0) {
                c3 = '.';
            }
            sb.append(c3);
            sb.append("0: ");
        } else if (j == -5) {
            sb.append("Inf: ");
        } else if (j == -6) {
            sb.append("NaN: ");
        } else {
            sb.append(String.valueOf(j));
            if (this.radix != 10) {
                sb.append('/');
                sb.append(this.radix);
            }
            int expectedExponent = expectedExponent() - this.exponent;
            for (int i = 0; i < expectedExponent; i++) {
                sb.append('>');
            }
            sb.append(PluralRules.KEYWORD_RULE_SEPARATOR);
        }
        if (this.ruleText.startsWith(" ") && ((nFSubstitution = this.sub1) == null || nFSubstitution.getPos() != 0)) {
            sb.append('\'');
        }
        StringBuilder sb2 = new StringBuilder(this.ruleText);
        NFSubstitution nFSubstitution2 = this.sub2;
        if (nFSubstitution2 != null) {
            sb2.insert(nFSubstitution2.getPos(), this.sub2.toString());
        }
        NFSubstitution nFSubstitution3 = this.sub1;
        if (nFSubstitution3 != null) {
            sb2.insert(nFSubstitution3.getPos(), this.sub1.toString());
        }
        sb.append(sb2.toString());
        sb.append(TelephoneNumberUtils.WAIT);
        return sb.toString();
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

    public void doFormat(long j, StringBuilder sb, int i, int i2) {
        int i3;
        int i4;
        int length = this.ruleText.length();
        if (this.rulePatternFormat == null) {
            sb.insert(i, this.ruleText);
            i4 = length;
            i3 = 0;
        } else {
            int indexOf = this.ruleText.indexOf("$(");
            int indexOf2 = this.ruleText.indexOf(")$", indexOf);
            int length2 = sb.length();
            if (indexOf2 < this.ruleText.length() - 1) {
                sb.insert(i, this.ruleText.substring(indexOf2 + 2));
            }
            sb.insert(i, this.rulePatternFormat.format((double) (j / power((long) this.radix, this.exponent))));
            if (indexOf > 0) {
                sb.insert(i, this.ruleText.substring(0, indexOf));
            }
            i4 = indexOf;
            i3 = this.ruleText.length() - (sb.length() - length2);
        }
        NFSubstitution nFSubstitution = this.sub2;
        if (nFSubstitution != null) {
            nFSubstitution.doSubstitution(j, sb, i - (nFSubstitution.getPos() > i4 ? i3 : 0), i2);
        }
        NFSubstitution nFSubstitution2 = this.sub1;
        if (nFSubstitution2 != null) {
            if (nFSubstitution2.getPos() <= i4) {
                i3 = 0;
            }
            nFSubstitution2.doSubstitution(j, sb, i - i3, i2);
        }
    }

    public void doFormat(double d, StringBuilder sb, int i, int i2) {
        int i3;
        int i4;
        double d2;
        int length = this.ruleText.length();
        if (this.rulePatternFormat == null) {
            sb.insert(i, this.ruleText);
            i4 = length;
            i3 = 0;
        } else {
            int indexOf = this.ruleText.indexOf("$(");
            int indexOf2 = this.ruleText.indexOf(")$", indexOf);
            int length2 = sb.length();
            if (indexOf2 < this.ruleText.length() - 1) {
                sb.insert(i, this.ruleText.substring(indexOf2 + 2));
            }
            if (0.0d > d || d >= 1.0d) {
                d2 = d / ((double) power((long) this.radix, this.exponent));
            } else {
                d2 = (double) Math.round(((double) power((long) this.radix, this.exponent)) * d);
            }
            sb.insert(i, this.rulePatternFormat.format((double) ((long) d2)));
            if (indexOf > 0) {
                sb.insert(i, this.ruleText.substring(0, indexOf));
            }
            i4 = indexOf;
            i3 = this.ruleText.length() - (sb.length() - length2);
        }
        NFSubstitution nFSubstitution = this.sub2;
        if (nFSubstitution != null) {
            nFSubstitution.doSubstitution(d, sb, i - (nFSubstitution.getPos() > i4 ? i3 : 0), i2);
        }
        NFSubstitution nFSubstitution2 = this.sub1;
        if (nFSubstitution2 != null) {
            if (nFSubstitution2.getPos() <= i4) {
                i3 = 0;
            }
            nFSubstitution2.doSubstitution(d, sb, i - i3, i2);
        }
    }

    static long power(long j, short s) {
        if (s < 0) {
            throw new IllegalArgumentException("Exponent can not be negative");
        } else if (j >= 0) {
            long j2 = 1;
            while (s > 0) {
                if ((s & 1) == 1) {
                    j2 *= j;
                }
                j *= j;
                s = (short) (s >> 1);
            }
            return j2;
        } else {
            throw new IllegalArgumentException("Base can not be negative");
        }
    }

    public boolean shouldRollBack(long j) {
        NFSubstitution nFSubstitution;
        NFSubstitution nFSubstitution2 = this.sub1;
        if ((nFSubstitution2 == null || !nFSubstitution2.isModulusSubstitution()) && ((nFSubstitution = this.sub2) == null || !nFSubstitution.isModulusSubstitution())) {
            return false;
        }
        long power = power((long) this.radix, this.exponent);
        if (j % power != 0 || this.baseValue % power == 0) {
            return false;
        }
        return true;
    }

    public Number doParse(String str, ParsePosition parsePosition, boolean z, double d, int i) {
        int i2;
        int i3;
        int i4;
        int i5;
        int i6 = 0;
        ParsePosition parsePosition2 = new ParsePosition(0);
        NFSubstitution nFSubstitution = this.sub1;
        int pos = nFSubstitution != null ? nFSubstitution.getPos() : this.ruleText.length();
        NFSubstitution nFSubstitution2 = this.sub2;
        int pos2 = nFSubstitution2 != null ? nFSubstitution2.getPos() : this.ruleText.length();
        String stripPrefix = stripPrefix(str, this.ruleText.substring(0, pos), parsePosition2);
        int length = str.length() - stripPrefix.length();
        if (parsePosition2.getIndex() == 0 && pos != 0) {
            return ZERO;
        }
        long j = this.baseValue;
        if (j == -5) {
            parsePosition.setIndex(parsePosition2.getIndex());
            return Double.valueOf(Double.POSITIVE_INFINITY);
        } else if (j == -6) {
            parsePosition.setIndex(parsePosition2.getIndex());
            return Double.valueOf(Double.NaN);
        } else {
            double max = (double) Math.max(0L, j);
            double d2 = 0.0d;
            int i7 = 0;
            int i8 = 0;
            while (true) {
                parsePosition2.setIndex(i6);
                double doubleValue = matchToDelimiter(stripPrefix, i8, max, this.ruleText.substring(pos, pos2), this.rulePatternFormat, parsePosition2, this.sub1, d, i).doubleValue();
                if (parsePosition2.getIndex() != 0 || this.sub1 == null) {
                    int index = parsePosition2.getIndex();
                    String substring = stripPrefix.substring(parsePosition2.getIndex());
                    ParsePosition parsePosition3 = new ParsePosition(0);
                    i2 = 0;
                    double doubleValue2 = matchToDelimiter(substring, 0, doubleValue, this.ruleText.substring(pos2), this.rulePatternFormat, parsePosition3, this.sub2, d, i).doubleValue();
                    if (parsePosition3.getIndex() != 0 || this.sub2 == null) {
                        i5 = i7;
                        if (length + parsePosition2.getIndex() + parsePosition3.getIndex() > i5) {
                            d2 = doubleValue2;
                            i7 = length + parsePosition2.getIndex() + parsePosition3.getIndex();
                            i3 = index;
                            i4 = pos2;
                        }
                    } else {
                        i5 = i7;
                    }
                    i7 = i5;
                    i3 = index;
                    i4 = pos2;
                } else {
                    i3 = i8;
                    i7 = i7;
                    i4 = pos2;
                    i2 = 0;
                }
                if (pos == i4 || parsePosition2.getIndex() <= 0 || parsePosition2.getIndex() >= stripPrefix.length() || parsePosition2.getIndex() == i3) {
                    break;
                }
                pos2 = i4;
                i8 = i3;
                pos = pos;
                stripPrefix = stripPrefix;
                i6 = i2;
                max = max;
            }
            parsePosition.setIndex(i7);
            if (z && i7 > 0 && this.sub1 == null) {
                d2 = 1.0d / d2;
            }
            long j2 = (long) d2;
            if (d2 == ((double) j2)) {
                return Long.valueOf(j2);
            }
            return new Double(d2);
        }
    }

    private String stripPrefix(String str, String str2, ParsePosition parsePosition) {
        int prefixLength;
        if (str2.length() == 0 || (prefixLength = prefixLength(str, str2)) == 0) {
            return str;
        }
        parsePosition.setIndex(parsePosition.getIndex() + prefixLength);
        return str.substring(prefixLength);
    }

    private Number matchToDelimiter(String str, int i, double d, String str2, PluralFormat pluralFormat, ParsePosition parsePosition, NFSubstitution nFSubstitution, double d2, int i2) {
        if (!allIgnorable(str2)) {
            ParsePosition parsePosition2 = new ParsePosition(0);
            int[] findText = findText(str, str2, pluralFormat, i);
            int i3 = findText[0];
            int i4 = findText[1];
            while (i3 >= 0) {
                String substring = str.substring(0, i3);
                if (substring.length() > 0) {
                    Number doParse = nFSubstitution.doParse(substring, parsePosition2, d, d2, this.formatter.lenientParseEnabled(), i2);
                    if (parsePosition2.getIndex() == i3) {
                        parsePosition.setIndex(i3 + i4);
                        return doParse;
                    }
                }
                parsePosition2.setIndex(0);
                int[] findText2 = findText(str, str2, pluralFormat, i3 + i4);
                i3 = findText2[0];
                i4 = findText2[1];
            }
            parsePosition.setIndex(0);
            return ZERO;
        } else if (nFSubstitution == null) {
            return Double.valueOf(d);
        } else {
            ParsePosition parsePosition3 = new ParsePosition(0);
            Long l = ZERO;
            Number doParse2 = nFSubstitution.doParse(str, parsePosition3, d, d2, this.formatter.lenientParseEnabled(), i2);
            if (parsePosition3.getIndex() == 0) {
                return l;
            }
            parsePosition.setIndex(parsePosition3.getIndex());
            return doParse2 != null ? doParse2 : l;
        }
    }

    private int prefixLength(String str, String str2) {
        if (str2.length() == 0) {
            return 0;
        }
        RbnfLenientScanner lenientScanner = this.formatter.getLenientScanner();
        if (lenientScanner != null) {
            return lenientScanner.prefixLength(str, str2);
        }
        if (str.startsWith(str2)) {
            return str2.length();
        }
        return 0;
    }

    private int[] findText(String str, String str2, PluralFormat pluralFormat, int i) {
        RbnfLenientScanner lenientScanner = this.formatter.getLenientScanner();
        if (pluralFormat == null) {
            return lenientScanner != null ? lenientScanner.findText(str, str2, i) : new int[]{str.indexOf(str2, i), str2.length()};
        }
        FieldPosition fieldPosition = new FieldPosition(0);
        fieldPosition.setBeginIndex(i);
        pluralFormat.parseType(str, lenientScanner, fieldPosition);
        int beginIndex = fieldPosition.getBeginIndex();
        if (beginIndex >= 0) {
            int indexOf = this.ruleText.indexOf("$(");
            int endIndex = fieldPosition.getEndIndex() - beginIndex;
            String substring = this.ruleText.substring(0, indexOf);
            String substring2 = this.ruleText.substring(this.ruleText.indexOf(")$", indexOf) + 2);
            if (str.regionMatches(beginIndex - substring.length(), substring, 0, substring.length()) && str.regionMatches(beginIndex + endIndex, substring2, 0, substring2.length())) {
                return new int[]{beginIndex - substring.length(), endIndex + substring.length() + substring2.length()};
            }
        }
        return new int[]{-1, 0};
    }

    private boolean allIgnorable(String str) {
        if (str == null || str.length() == 0) {
            return true;
        }
        RbnfLenientScanner lenientScanner = this.formatter.getLenientScanner();
        if (lenientScanner == null || !lenientScanner.allIgnorable(str)) {
            return false;
        }
        return true;
    }

    public void setDecimalFormatSymbols(DecimalFormatSymbols decimalFormatSymbols) {
        NFSubstitution nFSubstitution = this.sub1;
        if (nFSubstitution != null) {
            nFSubstitution.setDecimalFormatSymbols(decimalFormatSymbols);
        }
        NFSubstitution nFSubstitution2 = this.sub2;
        if (nFSubstitution2 != null) {
            nFSubstitution2.setDecimalFormatSymbols(decimalFormatSymbols);
        }
    }
}
