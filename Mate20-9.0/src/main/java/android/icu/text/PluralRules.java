package android.icu.text;

import android.icu.impl.PluralRulesLoader;
import android.icu.impl.number.Padder;
import android.icu.util.Output;
import android.icu.util.ULocale;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

public class PluralRules implements Serializable {
    static final UnicodeSet ALLOWED_ID = new UnicodeSet("[a-z]").freeze();
    static final Pattern AND_SEPARATED = Pattern.compile("\\s*and\\s*");
    static final Pattern AT_SEPARATED = Pattern.compile("\\s*\\Q\\E@\\s*");
    @Deprecated
    public static final String CATEGORY_SEPARATOR = ";  ";
    static final Pattern COMMA_SEPARATED = Pattern.compile("\\s*,\\s*");
    public static final PluralRules DEFAULT = new PluralRules(new RuleList().addRule(DEFAULT_RULE));
    private static final Rule DEFAULT_RULE = new Rule(KEYWORD_OTHER, NO_CONSTRAINT, null, null);
    static final Pattern DOTDOT_SEPARATED = Pattern.compile("\\s*\\Q..\\E\\s*");
    public static final String KEYWORD_FEW = "few";
    public static final String KEYWORD_MANY = "many";
    public static final String KEYWORD_ONE = "one";
    public static final String KEYWORD_OTHER = "other";
    @Deprecated
    public static final String KEYWORD_RULE_SEPARATOR = ": ";
    public static final String KEYWORD_TWO = "two";
    public static final String KEYWORD_ZERO = "zero";
    private static final Constraint NO_CONSTRAINT = new Constraint() {
        private static final long serialVersionUID = 9163464945387899416L;

        public boolean isFulfilled(IFixedDecimal n) {
            return true;
        }

        public boolean isLimited(SampleType sampleType) {
            return false;
        }

        public String toString() {
            return "";
        }
    };
    public static final double NO_UNIQUE_VALUE = -0.00123456777d;
    static final Pattern OR_SEPARATED = Pattern.compile("\\s*or\\s*");
    static final Pattern SEMI_SEPARATED = Pattern.compile("\\s*;\\s*");
    static final Pattern TILDE_SEPARATED = Pattern.compile("\\s*~\\s*");
    private static final long serialVersionUID = 1;
    private final transient Set<String> keywords;
    private final RuleList rules;

    private static class AndConstraint extends BinaryConstraint {
        private static final long serialVersionUID = 7766999779862263523L;

        AndConstraint(Constraint a, Constraint b) {
            super(a, b);
        }

        public boolean isFulfilled(IFixedDecimal n) {
            return this.a.isFulfilled(n) && this.b.isFulfilled(n);
        }

        public boolean isLimited(SampleType sampleType) {
            return this.a.isLimited(sampleType) || this.b.isLimited(sampleType);
        }

        public String toString() {
            return this.a.toString() + " and " + this.b.toString();
        }
    }

    private static abstract class BinaryConstraint implements Constraint, Serializable {
        private static final long serialVersionUID = 1;
        protected final Constraint a;
        protected final Constraint b;

        protected BinaryConstraint(Constraint a2, Constraint b2) {
            this.a = a2;
            this.b = b2;
        }
    }

    private interface Constraint extends Serializable {
        boolean isFulfilled(IFixedDecimal iFixedDecimal);

        boolean isLimited(SampleType sampleType);
    }

    @Deprecated
    public static abstract class Factory {
        @Deprecated
        public abstract PluralRules forLocale(ULocale uLocale, PluralType pluralType);

        @Deprecated
        public abstract ULocale[] getAvailableULocales();

        @Deprecated
        public abstract ULocale getFunctionalEquivalent(ULocale uLocale, boolean[] zArr);

        @Deprecated
        public abstract boolean hasOverride(ULocale uLocale);

        @Deprecated
        protected Factory() {
        }

        @Deprecated
        public final PluralRules forLocale(ULocale locale) {
            return forLocale(locale, PluralType.CARDINAL);
        }

        @Deprecated
        public static PluralRulesLoader getDefaultFactory() {
            return PluralRulesLoader.loader;
        }
    }

    @Deprecated
    public static class FixedDecimal extends Number implements Comparable<FixedDecimal>, IFixedDecimal {
        static final long MAX = 1000000000000000000L;
        private static final long MAX_INTEGER_PART = 1000000000;
        private static final long serialVersionUID = -4756200506571685661L;
        /* access modifiers changed from: private */
        public final int baseFactor;
        final long decimalDigits;
        final long decimalDigitsWithoutTrailingZeros;
        final boolean hasIntegerValue;
        final long integerValue;
        final boolean isNegative;
        final double source;
        final int visibleDecimalDigitCount;
        final int visibleDecimalDigitCountWithoutTrailingZeros;

        @Deprecated
        public double getSource() {
            return this.source;
        }

        @Deprecated
        public int getVisibleDecimalDigitCount() {
            return this.visibleDecimalDigitCount;
        }

        @Deprecated
        public int getVisibleDecimalDigitCountWithoutTrailingZeros() {
            return this.visibleDecimalDigitCountWithoutTrailingZeros;
        }

        @Deprecated
        public long getDecimalDigits() {
            return this.decimalDigits;
        }

        @Deprecated
        public long getDecimalDigitsWithoutTrailingZeros() {
            return this.decimalDigitsWithoutTrailingZeros;
        }

        @Deprecated
        public long getIntegerValue() {
            return this.integerValue;
        }

        @Deprecated
        public boolean isHasIntegerValue() {
            return this.hasIntegerValue;
        }

        @Deprecated
        public boolean isNegative() {
            return this.isNegative;
        }

        @Deprecated
        public int getBaseFactor() {
            return this.baseFactor;
        }

        @Deprecated
        public FixedDecimal(double n, int v, long f) {
            long j;
            boolean z = true;
            this.isNegative = n < 0.0d;
            this.source = this.isNegative ? -n : n;
            this.visibleDecimalDigitCount = v;
            this.decimalDigits = f;
            if (n > 1.0E18d) {
                j = MAX;
            } else {
                j = (long) n;
            }
            this.integerValue = j;
            this.hasIntegerValue = this.source != ((double) this.integerValue) ? false : z;
            if (f == 0) {
                this.decimalDigitsWithoutTrailingZeros = 0;
                this.visibleDecimalDigitCountWithoutTrailingZeros = 0;
            } else {
                long fdwtz = f;
                int trimmedCount = v;
                while (fdwtz % 10 == 0) {
                    fdwtz /= 10;
                    trimmedCount--;
                }
                this.decimalDigitsWithoutTrailingZeros = fdwtz;
                this.visibleDecimalDigitCountWithoutTrailingZeros = trimmedCount;
            }
            this.baseFactor = (int) Math.pow(10.0d, (double) v);
        }

        @Deprecated
        public FixedDecimal(double n, int v) {
            this(n, v, (long) getFractionalDigits(n, v));
        }

        private static int getFractionalDigits(double n, int v) {
            if (v == 0) {
                return 0;
            }
            if (n < 0.0d) {
                n = -n;
            }
            int baseFactor2 = (int) Math.pow(10.0d, (double) v);
            return (int) (Math.round(((double) baseFactor2) * n) % ((long) baseFactor2));
        }

        @Deprecated
        public FixedDecimal(double n) {
            this(n, decimals(n));
        }

        @Deprecated
        public FixedDecimal(long n) {
            this((double) n, 0);
        }

        @Deprecated
        public static int decimals(double n) {
            if (Double.isInfinite(n) || Double.isNaN(n)) {
                return 0;
            }
            if (n < 0.0d) {
                n = -n;
            }
            if (n == Math.floor(n)) {
                return 0;
            }
            if (n < 1.0E9d) {
                long temp = ((long) (1000000.0d * n)) % 1000000;
                int mask = 10;
                for (int digits = 6; digits > 0; digits--) {
                    if (temp % ((long) mask) != 0) {
                        return digits;
                    }
                    mask *= 10;
                }
                return 0;
            }
            String buf = String.format(Locale.ENGLISH, "%1.15e", new Object[]{Double.valueOf(n)});
            int ePos = buf.lastIndexOf(101);
            int expNumPos = ePos + 1;
            if (buf.charAt(expNumPos) == '+') {
                expNumPos++;
            }
            int numFractionDigits = (ePos - 2) - Integer.parseInt(buf.substring(expNumPos));
            if (numFractionDigits < 0) {
                return 0;
            }
            int i = ePos - 1;
            while (numFractionDigits > 0 && buf.charAt(i) == '0') {
                numFractionDigits--;
                i--;
            }
            return numFractionDigits;
        }

        @Deprecated
        public FixedDecimal(String n) {
            this(Double.parseDouble(n), getVisibleFractionCount(n));
        }

        private static int getVisibleFractionCount(String value) {
            String value2 = value.trim();
            int decimalPos = value2.indexOf(46) + 1;
            if (decimalPos == 0) {
                return 0;
            }
            return value2.length() - decimalPos;
        }

        @Deprecated
        public double getPluralOperand(Operand operand) {
            switch (operand) {
                case n:
                    return this.source;
                case i:
                    return (double) this.integerValue;
                case f:
                    return (double) this.decimalDigits;
                case t:
                    return (double) this.decimalDigitsWithoutTrailingZeros;
                case v:
                    return (double) this.visibleDecimalDigitCount;
                case w:
                    return (double) this.visibleDecimalDigitCountWithoutTrailingZeros;
                default:
                    return this.source;
            }
        }

        @Deprecated
        public static Operand getOperand(String t) {
            return Operand.valueOf(t);
        }

        @Deprecated
        public int compareTo(FixedDecimal other) {
            int i = 1;
            if (this.integerValue != other.integerValue) {
                if (this.integerValue < other.integerValue) {
                    i = -1;
                }
                return i;
            } else if (this.source != other.source) {
                if (this.source < other.source) {
                    i = -1;
                }
                return i;
            } else if (this.visibleDecimalDigitCount != other.visibleDecimalDigitCount) {
                if (this.visibleDecimalDigitCount < other.visibleDecimalDigitCount) {
                    i = -1;
                }
                return i;
            } else {
                long diff = this.decimalDigits - other.decimalDigits;
                if (diff == 0) {
                    return 0;
                }
                if (diff < 0) {
                    i = -1;
                }
                return i;
            }
        }

        @Deprecated
        public boolean equals(Object arg0) {
            boolean z = false;
            if (arg0 == null) {
                return false;
            }
            if (arg0 == this) {
                return true;
            }
            if (!(arg0 instanceof FixedDecimal)) {
                return false;
            }
            FixedDecimal other = (FixedDecimal) arg0;
            if (this.source == other.source && this.visibleDecimalDigitCount == other.visibleDecimalDigitCount && this.decimalDigits == other.decimalDigits) {
                z = true;
            }
            return z;
        }

        @Deprecated
        public int hashCode() {
            return (int) (this.decimalDigits + ((long) (37 * (this.visibleDecimalDigitCount + ((int) (37.0d * this.source))))));
        }

        @Deprecated
        public String toString() {
            return String.format("%." + this.visibleDecimalDigitCount + "f", new Object[]{Double.valueOf(this.source)});
        }

        @Deprecated
        public boolean hasIntegerValue() {
            return this.hasIntegerValue;
        }

        @Deprecated
        public int intValue() {
            return (int) this.integerValue;
        }

        @Deprecated
        public long longValue() {
            return this.integerValue;
        }

        @Deprecated
        public float floatValue() {
            return (float) this.source;
        }

        @Deprecated
        public double doubleValue() {
            return this.isNegative ? -this.source : this.source;
        }

        @Deprecated
        public long getShiftedValue() {
            return (this.integerValue * ((long) this.baseFactor)) + this.decimalDigits;
        }

        private void writeObject(ObjectOutputStream out) throws IOException {
            throw new NotSerializableException();
        }

        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
            throw new NotSerializableException();
        }

        @Deprecated
        public boolean isNaN() {
            return Double.isNaN(this.source);
        }

        @Deprecated
        public boolean isInfinite() {
            return Double.isInfinite(this.source);
        }
    }

    @Deprecated
    public static class FixedDecimalRange {
        @Deprecated
        public final FixedDecimal end;
        @Deprecated
        public final FixedDecimal start;

        @Deprecated
        public FixedDecimalRange(FixedDecimal start2, FixedDecimal end2) {
            if (start2.visibleDecimalDigitCount == end2.visibleDecimalDigitCount) {
                this.start = start2;
                this.end = end2;
                return;
            }
            throw new IllegalArgumentException("Ranges must have the same number of visible decimals: " + start2 + "~" + end2);
        }

        @Deprecated
        public String toString() {
            String str;
            StringBuilder sb = new StringBuilder();
            sb.append(this.start);
            if (this.end == this.start) {
                str = "";
            } else {
                str = "~" + this.end;
            }
            sb.append(str);
            return sb.toString();
        }
    }

    @Deprecated
    public static class FixedDecimalSamples {
        @Deprecated
        public final boolean bounded;
        @Deprecated
        public final SampleType sampleType;
        @Deprecated
        public final Set<FixedDecimalRange> samples;

        private FixedDecimalSamples(SampleType sampleType2, Set<FixedDecimalRange> samples2, boolean bounded2) {
            this.sampleType = sampleType2;
            this.samples = samples2;
            this.bounded = bounded2;
        }

        static FixedDecimalSamples parse(String source) {
            SampleType sampleType2;
            Set<FixedDecimalRange> samples2 = new LinkedHashSet<>();
            if (source.startsWith("integer")) {
                sampleType2 = SampleType.INTEGER;
            } else if (source.startsWith("decimal")) {
                sampleType2 = SampleType.DECIMAL;
            } else {
                throw new IllegalArgumentException("Samples must start with 'integer' or 'decimal'");
            }
            boolean haveBound = false;
            boolean bounded2 = true;
            for (String range : PluralRules.COMMA_SEPARATED.split(source.substring(7).trim())) {
                if (range.equals("…") || range.equals("...")) {
                    bounded2 = false;
                    haveBound = true;
                } else if (!haveBound) {
                    String[] rangeParts = PluralRules.TILDE_SEPARATED.split(range);
                    switch (rangeParts.length) {
                        case 1:
                            FixedDecimal sample = new FixedDecimal(rangeParts[0]);
                            checkDecimal(sampleType2, sample);
                            samples2.add(new FixedDecimalRange(sample, sample));
                            break;
                        case 2:
                            FixedDecimal start = new FixedDecimal(rangeParts[0]);
                            FixedDecimal end = new FixedDecimal(rangeParts[1]);
                            checkDecimal(sampleType2, start);
                            checkDecimal(sampleType2, end);
                            samples2.add(new FixedDecimalRange(start, end));
                            break;
                        default:
                            throw new IllegalArgumentException("Ill-formed number range: " + range);
                    }
                } else {
                    throw new IllegalArgumentException("Can only have … at the end of samples: " + range);
                }
            }
            return new FixedDecimalSamples(sampleType2, Collections.unmodifiableSet(samples2), bounded2);
        }

        private static void checkDecimal(SampleType sampleType2, FixedDecimal sample) {
            boolean z = false;
            boolean z2 = sampleType2 == SampleType.INTEGER;
            if (sample.getVisibleDecimalDigitCount() == 0) {
                z = true;
            }
            if (z2 != z) {
                throw new IllegalArgumentException("Ill-formed number range: " + sample);
            }
        }

        @Deprecated
        public Set<Double> addSamples(Set<Double> result) {
            for (FixedDecimalRange item : this.samples) {
                long startDouble = item.start.getShiftedValue();
                long endDouble = item.end.getShiftedValue();
                for (long d = startDouble; d <= endDouble; d += PluralRules.serialVersionUID) {
                    result.add(Double.valueOf(((double) d) / ((double) item.start.baseFactor)));
                }
            }
            return result;
        }

        @Deprecated
        public String toString() {
            StringBuilder b = new StringBuilder("@").append(this.sampleType.toString().toLowerCase(Locale.ENGLISH));
            boolean first = true;
            for (FixedDecimalRange item : this.samples) {
                if (first) {
                    first = false;
                } else {
                    b.append(",");
                }
                b.append(' ');
                b.append(item);
            }
            if (!this.bounded) {
                b.append(", …");
            }
            return b.toString();
        }

        @Deprecated
        public Set<FixedDecimalRange> getSamples() {
            return this.samples;
        }

        @Deprecated
        public void getStartEndSamples(Set<FixedDecimal> target) {
            for (FixedDecimalRange item : this.samples) {
                target.add(item.start);
                target.add(item.end);
            }
        }
    }

    @Deprecated
    public interface IFixedDecimal {
        @Deprecated
        double getPluralOperand(Operand operand);

        @Deprecated
        boolean isInfinite();

        @Deprecated
        boolean isNaN();
    }

    public enum KeywordStatus {
        INVALID,
        SUPPRESSED,
        UNIQUE,
        BOUNDED,
        UNBOUNDED
    }

    @Deprecated
    public enum Operand {
        n,
        i,
        f,
        t,
        v,
        w,
        j
    }

    private static class OrConstraint extends BinaryConstraint {
        private static final long serialVersionUID = 1405488568664762222L;

        OrConstraint(Constraint a, Constraint b) {
            super(a, b);
        }

        public boolean isFulfilled(IFixedDecimal n) {
            return this.a.isFulfilled(n) || this.b.isFulfilled(n);
        }

        public boolean isLimited(SampleType sampleType) {
            return this.a.isLimited(sampleType) && this.b.isLimited(sampleType);
        }

        public String toString() {
            return this.a.toString() + " or " + this.b.toString();
        }
    }

    public enum PluralType {
        CARDINAL,
        ORDINAL
    }

    private static class RangeConstraint implements Constraint, Serializable {
        private static final long serialVersionUID = 1;
        private final boolean inRange;
        private final boolean integersOnly;
        private final double lowerBound;
        private final int mod;
        private final Operand operand;
        private final long[] range_list;
        private final double upperBound;

        RangeConstraint(int mod2, boolean inRange2, Operand operand2, boolean integersOnly2, double lowBound, double highBound, long[] vals) {
            this.mod = mod2;
            this.inRange = inRange2;
            this.integersOnly = integersOnly2;
            this.lowerBound = lowBound;
            this.upperBound = highBound;
            this.range_list = vals;
            this.operand = operand2;
        }

        public boolean isFulfilled(IFixedDecimal number) {
            double n = number.getPluralOperand(this.operand);
            if ((this.integersOnly && n - ((double) ((long) n)) != 0.0d) || (this.operand == Operand.j && number.getPluralOperand(Operand.v) != 0.0d)) {
                return !this.inRange;
            }
            if (this.mod != 0) {
                n %= (double) this.mod;
            }
            boolean z = false;
            boolean test = n >= this.lowerBound && n <= this.upperBound;
            if (test && this.range_list != null) {
                boolean test2 = false;
                int i = 0;
                while (!test2 && i < this.range_list.length) {
                    test2 = n >= ((double) this.range_list[i]) && n <= ((double) this.range_list[i + 1]);
                    i += 2;
                }
                test = test2;
            }
            if (this.inRange == test) {
                z = true;
            }
            return z;
        }

        public boolean isLimited(SampleType sampleType) {
            boolean z = true;
            boolean hasDecimals = (this.operand == Operand.v || this.operand == Operand.w || this.operand == Operand.f || this.operand == Operand.t) && this.inRange != ((this.lowerBound > this.upperBound ? 1 : (this.lowerBound == this.upperBound ? 0 : -1)) == 0 && (this.lowerBound > 0.0d ? 1 : (this.lowerBound == 0.0d ? 0 : -1)) == 0);
            switch (sampleType) {
                case INTEGER:
                    if (!hasDecimals && !((this.operand == Operand.n || this.operand == Operand.i || this.operand == Operand.j) && this.mod == 0 && this.inRange)) {
                        z = false;
                    }
                    return z;
                case DECIMAL:
                    if (!(!hasDecimals || this.operand == Operand.n || this.operand == Operand.j) || ((!this.integersOnly && this.lowerBound != this.upperBound) || this.mod != 0 || !this.inRange)) {
                        z = false;
                    }
                    return z;
                default:
                    return false;
            }
        }

        public String toString() {
            String str;
            StringBuilder result = new StringBuilder();
            result.append(this.operand);
            if (this.mod != 0) {
                result.append(" % ");
                result.append(this.mod);
            }
            if (!(this.lowerBound != this.upperBound)) {
                str = this.inRange ? " = " : " != ";
            } else if (this.integersOnly) {
                str = this.inRange ? " = " : " != ";
            } else {
                str = this.inRange ? " within " : " not within ";
            }
            result.append(str);
            if (this.range_list != null) {
                int i = 0;
                while (true) {
                    int i2 = i;
                    if (i2 >= this.range_list.length) {
                        break;
                    }
                    PluralRules.addRange(result, (double) this.range_list[i2], (double) this.range_list[i2 + 1], i2 != 0);
                    i = i2 + 2;
                }
            } else {
                PluralRules.addRange(result, this.lowerBound, this.upperBound, false);
            }
            return result.toString();
        }
    }

    private static class Rule implements Serializable {
        private static final long serialVersionUID = 1;
        private final Constraint constraint;
        /* access modifiers changed from: private */
        public final FixedDecimalSamples decimalSamples;
        /* access modifiers changed from: private */
        public final FixedDecimalSamples integerSamples;
        private final String keyword;

        public Rule(String keyword2, Constraint constraint2, FixedDecimalSamples integerSamples2, FixedDecimalSamples decimalSamples2) {
            this.keyword = keyword2;
            this.constraint = constraint2;
            this.integerSamples = integerSamples2;
            this.decimalSamples = decimalSamples2;
        }

        public Rule and(Constraint c) {
            return new Rule(this.keyword, new AndConstraint(this.constraint, c), this.integerSamples, this.decimalSamples);
        }

        public Rule or(Constraint c) {
            return new Rule(this.keyword, new OrConstraint(this.constraint, c), this.integerSamples, this.decimalSamples);
        }

        public String getKeyword() {
            return this.keyword;
        }

        public boolean appliesTo(IFixedDecimal n) {
            return this.constraint.isFulfilled(n);
        }

        public boolean isLimited(SampleType sampleType) {
            return this.constraint.isLimited(sampleType);
        }

        public String toString() {
            String str;
            String str2;
            StringBuilder sb = new StringBuilder();
            sb.append(this.keyword);
            sb.append(PluralRules.KEYWORD_RULE_SEPARATOR);
            sb.append(this.constraint.toString());
            if (this.integerSamples == null) {
                str = "";
            } else {
                str = Padder.FALLBACK_PADDING_STRING + this.integerSamples.toString();
            }
            sb.append(str);
            if (this.decimalSamples == null) {
                str2 = "";
            } else {
                str2 = Padder.FALLBACK_PADDING_STRING + this.decimalSamples.toString();
            }
            sb.append(str2);
            return sb.toString();
        }

        @Deprecated
        public int hashCode() {
            return this.keyword.hashCode() ^ this.constraint.hashCode();
        }

        public String getConstraint() {
            return this.constraint.toString();
        }
    }

    private static class RuleList implements Serializable {
        private static final long serialVersionUID = 1;
        /* access modifiers changed from: private */
        public boolean hasExplicitBoundingInfo;
        private final List<Rule> rules;

        private RuleList() {
            this.hasExplicitBoundingInfo = false;
            this.rules = new ArrayList();
        }

        /* JADX WARNING: type inference failed for: r0v2, types: [boolean, byte] */
        static /* synthetic */ boolean access$276(RuleList x0, int x1) {
            ? r0 = (byte) (x0.hasExplicitBoundingInfo | x1);
            x0.hasExplicitBoundingInfo = r0;
            return r0;
        }

        public RuleList addRule(Rule nextRule) {
            String keyword = nextRule.getKeyword();
            for (Rule rule : this.rules) {
                if (keyword.equals(rule.getKeyword())) {
                    throw new IllegalArgumentException("Duplicate keyword: " + keyword);
                }
            }
            this.rules.add(nextRule);
            return this;
        }

        public RuleList finish() throws ParseException {
            Rule otherRule = null;
            Iterator<Rule> it = this.rules.iterator();
            while (it.hasNext()) {
                Rule rule = it.next();
                if (PluralRules.KEYWORD_OTHER.equals(rule.getKeyword())) {
                    otherRule = rule;
                    it.remove();
                }
            }
            if (otherRule == null) {
                otherRule = PluralRules.parseRule("other:");
            }
            this.rules.add(otherRule);
            return this;
        }

        private Rule selectRule(IFixedDecimal n) {
            for (Rule rule : this.rules) {
                if (rule.appliesTo(n)) {
                    return rule;
                }
            }
            return null;
        }

        public String select(IFixedDecimal n) {
            if (n.isInfinite() || n.isNaN()) {
                return PluralRules.KEYWORD_OTHER;
            }
            return selectRule(n).getKeyword();
        }

        public Set<String> getKeywords() {
            Set<String> result = new LinkedHashSet<>();
            for (Rule rule : this.rules) {
                result.add(rule.getKeyword());
            }
            return result;
        }

        public boolean isLimited(String keyword, SampleType sampleType) {
            if (!this.hasExplicitBoundingInfo) {
                return computeLimited(keyword, sampleType);
            }
            FixedDecimalSamples mySamples = getDecimalSamples(keyword, sampleType);
            return mySamples == null ? true : mySamples.bounded;
        }

        public boolean computeLimited(String keyword, SampleType sampleType) {
            boolean result = false;
            for (Rule rule : this.rules) {
                if (keyword.equals(rule.getKeyword())) {
                    if (!rule.isLimited(sampleType)) {
                        return false;
                    }
                    result = true;
                }
            }
            return result;
        }

        public String toString() {
            StringBuilder builder = new StringBuilder();
            for (Rule rule : this.rules) {
                if (builder.length() != 0) {
                    builder.append(PluralRules.CATEGORY_SEPARATOR);
                }
                builder.append(rule);
            }
            return builder.toString();
        }

        public String getRules(String keyword) {
            for (Rule rule : this.rules) {
                if (rule.getKeyword().equals(keyword)) {
                    return rule.getConstraint();
                }
            }
            return null;
        }

        public boolean select(IFixedDecimal sample, String keyword) {
            for (Rule rule : this.rules) {
                if (rule.getKeyword().equals(keyword) && rule.appliesTo(sample)) {
                    return true;
                }
            }
            return false;
        }

        public FixedDecimalSamples getDecimalSamples(String keyword, SampleType sampleType) {
            for (Rule rule : this.rules) {
                if (rule.getKeyword().equals(keyword)) {
                    return sampleType == SampleType.INTEGER ? rule.integerSamples : rule.decimalSamples;
                }
            }
            return null;
        }
    }

    @Deprecated
    public enum SampleType {
        INTEGER,
        DECIMAL
    }

    static class SimpleTokenizer {
        static final UnicodeSet BREAK_AND_IGNORE = new UnicodeSet(9, 10, 12, 13, 32, 32).freeze();
        static final UnicodeSet BREAK_AND_KEEP = new UnicodeSet(33, 33, 37, 37, 44, 44, 46, 46, 61, 61).freeze();

        SimpleTokenizer() {
        }

        static String[] split(String source) {
            int last = -1;
            List<String> result = new ArrayList<>();
            for (int i = 0; i < source.length(); i++) {
                char ch = source.charAt(i);
                if (BREAK_AND_IGNORE.contains((int) ch)) {
                    if (last >= 0) {
                        result.add(source.substring(last, i));
                        last = -1;
                    }
                } else if (BREAK_AND_KEEP.contains((int) ch)) {
                    if (last >= 0) {
                        result.add(source.substring(last, i));
                    }
                    result.add(source.substring(i, i + 1));
                    last = -1;
                } else if (last < 0) {
                    last = i;
                }
            }
            if (last >= 0) {
                result.add(source.substring(last));
            }
            return (String[]) result.toArray(new String[result.size()]);
        }
    }

    public static PluralRules parseDescription(String description) throws ParseException {
        String description2 = description.trim();
        return description2.length() == 0 ? DEFAULT : new PluralRules(parseRuleChain(description2));
    }

    public static PluralRules createRules(String description) {
        try {
            return parseDescription(description);
        } catch (Exception e) {
            return null;
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r35v1, resolved type: android.icu.text.PluralRules$Constraint} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v10, resolved type: android.icu.text.PluralRules$RangeConstraint} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r22v4, resolved type: android.icu.text.PluralRules$RangeConstraint} */
    /* JADX WARNING: type inference failed for: r1v8, types: [android.icu.text.PluralRules$Constraint] */
    /* JADX WARNING: Code restructure failed: missing block: B:122:0x02a8, code lost:
        r21 = r1;
        r38 = r4;
        r32 = r5;
        r33 = r6;
        r5 = r7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:123:0x02b1, code lost:
        if (r38 != null) goto L_0x02b6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:124:0x02b3, code lost:
        r0 = r5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:125:0x02b4, code lost:
        r4 = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:126:0x02b6, code lost:
        r0 = new android.icu.text.PluralRules.OrConstraint(r38, r5);
     */
    /* JADX WARNING: Multi-variable type inference failed */
    private static Constraint parseConstraint(String description) throws ParseException {
        String[] or_together;
        int i;
        Constraint andConstraint;
        Constraint result;
        int j;
        String[] and_together;
        int i2;
        RangeConstraint andConstraint2;
        int x;
        String t;
        int x2;
        int x3;
        String t2;
        double highBound;
        long[] vals;
        String[] or_together2 = OR_SEPARATED.split(description);
        Constraint result2 = null;
        int i3 = 0;
        while (true) {
            int i4 = i3;
            if (i4 < or_together2.length) {
                String[] and_together2 = AND_SEPARATED.split(or_together2[i4]);
                Constraint andConstraint3 = null;
                int j2 = 0;
                while (true) {
                    int j3 = j2;
                    if (j3 >= and_together2.length) {
                        break;
                    }
                    Constraint newConstraint = NO_CONSTRAINT;
                    String condition = and_together2[j3].trim();
                    String[] tokens = SimpleTokenizer.split(condition);
                    int mod = 0;
                    boolean inRange = true;
                    boolean integersOnly = true;
                    int x4 = 0 + 1;
                    String[] or_together3 = or_together2;
                    String t3 = tokens[0];
                    boolean hackForCompatibility = false;
                    try {
                        Operand operand = FixedDecimal.getOperand(t3);
                        if (x4 < tokens.length) {
                            int x5 = x4 + 1;
                            String t4 = tokens[x4];
                            if ("mod".equals(t4) || "%".equals(t4)) {
                                int x6 = x5 + 1;
                                mod = Integer.parseInt(tokens[x5]);
                                x5 = x6 + 1;
                                t4 = nextToken(tokens, x6, condition);
                            }
                            boolean inRange2 = true;
                            if ("not".equals(t4)) {
                                inRange = 1 == 0;
                                x = x5 + 1;
                                t4 = nextToken(tokens, x5, condition);
                                if ("=".equals(t4)) {
                                    throw unexpected(t4, condition);
                                }
                            } else if ("!".equals(t4)) {
                                inRange = 1 == 0;
                                x = x5 + 1;
                                t4 = nextToken(tokens, x5, condition);
                                if (!"=".equals(t4)) {
                                    throw unexpected(t4, condition);
                                }
                            } else {
                                x = x5;
                            }
                            if ("is".equals(t4) || "in".equals(t4) || "=".equals(t4)) {
                                hackForCompatibility = "is".equals(t4);
                                if (!hackForCompatibility || inRange) {
                                    x2 = x + 1;
                                    t = nextToken(tokens, x, condition);
                                } else {
                                    throw unexpected(t4, condition);
                                }
                            } else if ("within".equals(t4)) {
                                integersOnly = false;
                                x2 = x + 1;
                                t = nextToken(tokens, x, condition);
                            } else {
                                throw unexpected(t4, condition);
                            }
                            if ("not".equals(t)) {
                                if (hackForCompatibility || inRange) {
                                    if (inRange) {
                                        inRange2 = false;
                                    }
                                    t = nextToken(tokens, x2, condition);
                                    x2++;
                                    inRange = inRange2;
                                } else {
                                    throw unexpected(t, condition);
                                }
                            }
                            List<Long> valueList = new ArrayList<>();
                            i2 = i4;
                            and_together = and_together2;
                            j = j3;
                            Constraint constraint = newConstraint;
                            double lowBound = 9.223372036854776E18d;
                            double highBound2 = -9.223372036854776E18d;
                            while (true) {
                                boolean integersOnly2 = integersOnly;
                                long low = Long.parseLong(t);
                                long high = low;
                                String t5 = t;
                                if (x2 < tokens.length) {
                                    int x7 = x2 + 1;
                                    String t6 = nextToken(tokens, x2, condition);
                                    if (t6.equals(".")) {
                                        int x8 = x7 + 1;
                                        String t7 = nextToken(tokens, x7, condition);
                                        if (t7.equals(".")) {
                                            x7 = x8 + 1;
                                            t6 = nextToken(tokens, x8, condition);
                                            high = Long.parseLong(t6);
                                            if (x7 < tokens.length) {
                                                int x9 = x7 + 1;
                                                String t8 = nextToken(tokens, x7, condition);
                                                if (t8.equals(",")) {
                                                    result = result2;
                                                    x3 = x9;
                                                    t2 = t8;
                                                } else {
                                                    throw unexpected(t8, condition);
                                                }
                                            }
                                        } else {
                                            throw unexpected(t7, condition);
                                        }
                                    } else if (!t6.equals(",")) {
                                        throw unexpected(t6, condition);
                                    }
                                    t2 = t6;
                                    result = result2;
                                    x3 = x7;
                                } else {
                                    result = result2;
                                    t2 = t5;
                                    x3 = x2;
                                }
                                long high2 = high;
                                if (low <= high2) {
                                    if (mod != 0) {
                                        andConstraint = andConstraint3;
                                        highBound = highBound2;
                                        if (high2 >= ((long) mod)) {
                                            throw unexpected(high2 + ">mod=" + mod, condition);
                                        }
                                    } else {
                                        andConstraint = andConstraint3;
                                        highBound = highBound2;
                                    }
                                    valueList.add(Long.valueOf(low));
                                    valueList.add(Long.valueOf(high2));
                                    lowBound = Math.min(lowBound, (double) low);
                                    long j4 = high2;
                                    highBound2 = Math.max(highBound, (double) high2);
                                    if (x3 < tokens.length) {
                                        x2 = x3 + 1;
                                        t = nextToken(tokens, x3, condition);
                                        integersOnly = integersOnly2;
                                        result2 = result;
                                        andConstraint3 = andConstraint;
                                        String str = description;
                                    } else if (!t2.equals(",")) {
                                        if (valueList.size() == 2) {
                                            vals = null;
                                        } else {
                                            vals = new long[valueList.size()];
                                            for (int k = 0; k < vals.length; k++) {
                                                vals[k] = valueList.get(k).longValue();
                                            }
                                        }
                                        if (lowBound == highBound2 || !hackForCompatibility || inRange) {
                                            RangeConstraint rangeConstraint = new RangeConstraint(mod, inRange, operand, integersOnly2, lowBound, highBound2, vals);
                                            int i5 = x3;
                                            double d = lowBound;
                                            double d2 = highBound2;
                                            boolean z = integersOnly2;
                                            andConstraint2 = rangeConstraint;
                                        } else {
                                            throw unexpected("is not <range>", condition);
                                        }
                                    } else {
                                        throw unexpected(t2, condition);
                                    }
                                } else {
                                    Constraint constraint2 = andConstraint3;
                                    double d3 = highBound2;
                                    throw unexpected(low + "~" + high, condition);
                                }
                            }
                        } else {
                            result = result2;
                            i2 = i4;
                            and_together = and_together2;
                            andConstraint = andConstraint3;
                            j = j3;
                            String str2 = t3;
                            andConstraint2 = newConstraint;
                        }
                        if (andConstraint == null) {
                            andConstraint3 = andConstraint2;
                        } else {
                            andConstraint3 = new AndConstraint(andConstraint, andConstraint2);
                        }
                        j2 = j + 1;
                        or_together2 = or_together3;
                        i4 = i2;
                        and_together2 = and_together;
                        result2 = result;
                        String str3 = description;
                    } catch (Exception e) {
                        Constraint constraint3 = result2;
                        int i6 = i4;
                        String[] strArr = and_together2;
                        Constraint constraint4 = andConstraint3;
                        int i7 = j3;
                        Constraint constraint5 = newConstraint;
                        Exception exc = e;
                        throw unexpected(t3, condition);
                    }
                }
            } else {
                return result2;
            }
            i3 = i + 1;
            or_together2 = or_together;
            String str4 = description;
        }
    }

    private static ParseException unexpected(String token, String context) {
        return new ParseException("unexpected token '" + token + "' in '" + context + "'", -1);
    }

    private static String nextToken(String[] tokens, int x, String context) throws ParseException {
        if (x < tokens.length) {
            return tokens[x];
        }
        throw new ParseException("missing token at end of '" + context + "'", -1);
    }

    /* access modifiers changed from: private */
    public static Rule parseRule(String description) throws ParseException {
        Constraint constraint;
        if (description.length() == 0) {
            return DEFAULT_RULE;
        }
        String description2 = description.toLowerCase(Locale.ENGLISH);
        int x = description2.indexOf(58);
        if (x != -1) {
            String keyword = description2.substring(0, x).trim();
            if (isValidKeyword(keyword)) {
                String[] constraintOrSamples = AT_SEPARATED.split(description2.substring(x + 1).trim());
                FixedDecimalSamples integerSamples = null;
                FixedDecimalSamples decimalSamples = null;
                boolean z = true;
                switch (constraintOrSamples.length) {
                    case 1:
                        break;
                    case 2:
                        integerSamples = FixedDecimalSamples.parse(constraintOrSamples[1]);
                        if (integerSamples.sampleType == SampleType.DECIMAL) {
                            decimalSamples = integerSamples;
                            integerSamples = null;
                            break;
                        }
                        break;
                    case 3:
                        integerSamples = FixedDecimalSamples.parse(constraintOrSamples[1]);
                        decimalSamples = FixedDecimalSamples.parse(constraintOrSamples[2]);
                        if (!(integerSamples.sampleType == SampleType.INTEGER && decimalSamples.sampleType == SampleType.DECIMAL)) {
                            throw new IllegalArgumentException("Must have @integer then @decimal in " + description);
                        }
                    default:
                        throw new IllegalArgumentException("Too many samples in " + description);
                }
                if (0 == 0) {
                    boolean isOther = keyword.equals(KEYWORD_OTHER);
                    if (constraintOrSamples[0].length() != 0) {
                        z = false;
                    }
                    if (isOther == z) {
                        if (isOther) {
                            constraint = NO_CONSTRAINT;
                        } else {
                            constraint = parseConstraint(constraintOrSamples[0]);
                        }
                        return new Rule(keyword, constraint, integerSamples, decimalSamples);
                    }
                    throw new IllegalArgumentException("The keyword 'other' must have no constraints, just samples.");
                }
                throw new IllegalArgumentException("Ill-formed samples—'@' characters.");
            }
            throw new ParseException("keyword '" + keyword + " is not valid", 0);
        }
        throw new ParseException("missing ':' in rule description '" + description2 + "'", 0);
    }

    private static RuleList parseRuleChain(String description) throws ParseException {
        RuleList result = new RuleList();
        if (description.endsWith(";")) {
            description = description.substring(0, description.length() - 1);
        }
        String[] rules2 = SEMI_SEPARATED.split(description);
        for (String trim : rules2) {
            Rule rule = parseRule(trim.trim());
            RuleList.access$276(result, (rule.integerSamples == null && rule.decimalSamples == null) ? 0 : 1);
            result.addRule(rule);
        }
        return result.finish();
    }

    /* access modifiers changed from: private */
    public static void addRange(StringBuilder result, double lb, double ub, boolean addSeparator) {
        if (addSeparator) {
            result.append(",");
        }
        if (lb == ub) {
            result.append(format(lb));
            return;
        }
        result.append(format(lb) + ".." + format(ub));
    }

    private static String format(double lb) {
        long lbi = (long) lb;
        return lb == ((double) lbi) ? String.valueOf(lbi) : String.valueOf(lb);
    }

    private boolean addConditional(Set<IFixedDecimal> toAddTo, Set<IFixedDecimal> others, double trial) {
        IFixedDecimal toAdd = new FixedDecimal(trial);
        if (toAddTo.contains(toAdd) || others.contains(toAdd)) {
            return false;
        }
        others.add(toAdd);
        return true;
    }

    public static PluralRules forLocale(ULocale locale) {
        return Factory.getDefaultFactory().forLocale(locale, PluralType.CARDINAL);
    }

    public static PluralRules forLocale(Locale locale) {
        return forLocale(ULocale.forLocale(locale));
    }

    public static PluralRules forLocale(ULocale locale, PluralType type) {
        return Factory.getDefaultFactory().forLocale(locale, type);
    }

    public static PluralRules forLocale(Locale locale, PluralType type) {
        return forLocale(ULocale.forLocale(locale), type);
    }

    private static boolean isValidKeyword(String token) {
        return ALLOWED_ID.containsAll(token);
    }

    private PluralRules(RuleList rules2) {
        this.rules = rules2;
        this.keywords = Collections.unmodifiableSet(rules2.getKeywords());
    }

    @Deprecated
    public int hashCode() {
        return this.rules.hashCode();
    }

    public String select(double number) {
        return this.rules.select(new FixedDecimal(number));
    }

    @Deprecated
    public String select(double number, int countVisibleFractionDigits, long fractionaldigits) {
        RuleList ruleList = this.rules;
        FixedDecimal fixedDecimal = new FixedDecimal(number, countVisibleFractionDigits, fractionaldigits);
        return ruleList.select(fixedDecimal);
    }

    @Deprecated
    public String select(IFixedDecimal number) {
        return this.rules.select(number);
    }

    @Deprecated
    public boolean matches(FixedDecimal sample, String keyword) {
        return this.rules.select(sample, keyword);
    }

    public Set<String> getKeywords() {
        return this.keywords;
    }

    public double getUniqueKeywordValue(String keyword) {
        Collection<Double> values = getAllKeywordValues(keyword);
        if (values == null || values.size() != 1) {
            return -0.00123456777d;
        }
        return values.iterator().next().doubleValue();
    }

    public Collection<Double> getAllKeywordValues(String keyword) {
        return getAllKeywordValues(keyword, SampleType.INTEGER);
    }

    @Deprecated
    public Collection<Double> getAllKeywordValues(String keyword, SampleType type) {
        Collection<Double> collection = null;
        if (!isLimited(keyword, type)) {
            return null;
        }
        Collection<Double> samples = getSamples(keyword, type);
        if (samples != null) {
            collection = Collections.unmodifiableCollection(samples);
        }
        return collection;
    }

    public Collection<Double> getSamples(String keyword) {
        return getSamples(keyword, SampleType.INTEGER);
    }

    /* JADX WARNING: Can't fix incorrect switch cases order */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x004b, code lost:
        if (r3 >= 2000) goto L_0x0060;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x005a, code lost:
        if (addSample(r11, new android.icu.text.PluralRules.FixedDecimal(((double) r3) / 10.0d, 1), r2, r0) != false) goto L_0x005d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0060, code lost:
        addSample(r11, new android.icu.text.PluralRules.FixedDecimal(1000000.0d, 1), r2, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0072, code lost:
        if (r3 >= 200) goto L_0x0082;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x007c, code lost:
        if (addSample(r11, java.lang.Integer.valueOf(r3), r2, r0) != false) goto L_0x007f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0082, code lost:
        addSample(r11, 1000000, r2, r0);
     */
    @Deprecated
    public Collection<Double> getSamples(String keyword, SampleType sampleType) {
        Set<T> set;
        Set<T> set2 = null;
        if (!this.keywords.contains(keyword)) {
            return null;
        }
        Set<Double> result = new TreeSet<>();
        if (this.rules.hasExplicitBoundingInfo) {
            FixedDecimalSamples samples = this.rules.getDecimalSamples(keyword, sampleType);
            if (samples == null) {
                set = Collections.unmodifiableSet(result);
            } else {
                set = Collections.unmodifiableSet(samples.addSamples(result));
            }
            return set;
        }
        int maxCount = isLimited(keyword, sampleType) ? Integer.MAX_VALUE : 20;
        int i = 0;
        switch (sampleType) {
            case INTEGER:
                while (true) {
                    int i2 = i;
                    i = i2 + 1;
                    break;
                }
            case DECIMAL:
                while (true) {
                    int i3 = i;
                    i = i3 + 1;
                    break;
                }
        }
        if (result.size() != 0) {
            set2 = Collections.unmodifiableSet(result);
        }
        return set2;
    }

    @Deprecated
    public boolean addSample(String keyword, Number sample, int maxCount, Set<Double> result) {
        if ((sample instanceof FixedDecimal ? select((IFixedDecimal) (FixedDecimal) sample) : select(sample.doubleValue())).equals(keyword)) {
            result.add(Double.valueOf(sample.doubleValue()));
            if (maxCount - 1 < 0) {
                return false;
            }
        }
        return true;
    }

    @Deprecated
    public FixedDecimalSamples getDecimalSamples(String keyword, SampleType sampleType) {
        return this.rules.getDecimalSamples(keyword, sampleType);
    }

    public static ULocale[] getAvailableULocales() {
        return Factory.getDefaultFactory().getAvailableULocales();
    }

    public static ULocale getFunctionalEquivalent(ULocale locale, boolean[] isAvailable) {
        return Factory.getDefaultFactory().getFunctionalEquivalent(locale, isAvailable);
    }

    public String toString() {
        return this.rules.toString();
    }

    public boolean equals(Object rhs) {
        return (rhs instanceof PluralRules) && equals((PluralRules) rhs);
    }

    public boolean equals(PluralRules rhs) {
        return rhs != null && toString().equals(rhs.toString());
    }

    public KeywordStatus getKeywordStatus(String keyword, int offset, Set<Double> explicits, Output<Double> uniqueValue) {
        return getKeywordStatus(keyword, offset, explicits, uniqueValue, SampleType.INTEGER);
    }

    @Deprecated
    public KeywordStatus getKeywordStatus(String keyword, int offset, Set<Double> explicits, Output<Double> uniqueValue, SampleType sampleType) {
        if (uniqueValue != null) {
            uniqueValue.value = null;
        }
        if (!this.keywords.contains(keyword)) {
            return KeywordStatus.INVALID;
        }
        if (!isLimited(keyword, sampleType)) {
            return KeywordStatus.UNBOUNDED;
        }
        Collection<Double> values = getSamples(keyword, sampleType);
        int originalSize = values.size();
        if (explicits == null) {
            explicits = Collections.emptySet();
        }
        if (originalSize <= explicits.size()) {
            HashSet<Double> subtractedSet = new HashSet<>(values);
            for (Double explicit : explicits) {
                subtractedSet.remove(Double.valueOf(explicit.doubleValue() - ((double) offset)));
            }
            if (subtractedSet.size() == 0) {
                return KeywordStatus.SUPPRESSED;
            }
            if (uniqueValue != null && subtractedSet.size() == 1) {
                uniqueValue.value = subtractedSet.iterator().next();
            }
            return originalSize == 1 ? KeywordStatus.UNIQUE : KeywordStatus.BOUNDED;
        } else if (originalSize != 1) {
            return KeywordStatus.BOUNDED;
        } else {
            if (uniqueValue != null) {
                uniqueValue.value = values.iterator().next();
            }
            return KeywordStatus.UNIQUE;
        }
    }

    @Deprecated
    public String getRules(String keyword) {
        return this.rules.getRules(keyword);
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        throw new NotSerializableException();
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        throw new NotSerializableException();
    }

    private Object writeReplace() throws ObjectStreamException {
        return new PluralRulesSerialProxy(toString());
    }

    @Deprecated
    public int compareTo(PluralRules other) {
        return toString().compareTo(other.toString());
    }

    @Deprecated
    public Boolean isLimited(String keyword) {
        return Boolean.valueOf(this.rules.isLimited(keyword, SampleType.INTEGER));
    }

    @Deprecated
    public boolean isLimited(String keyword, SampleType sampleType) {
        return this.rules.isLimited(keyword, sampleType);
    }

    @Deprecated
    public boolean computeLimited(String keyword, SampleType sampleType) {
        return this.rules.computeLimited(keyword, sampleType);
    }
}
