package android.icu.text;

import android.icu.impl.PluralRulesLoader;
import android.icu.util.AnnualTimeZoneRule;
import android.icu.util.Output;
import android.icu.util.ULocale;
import dalvik.bytecode.Opcodes;
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
import org.w3c.dom.traversal.NodeFilter;
import org.xmlpull.v1.XmlPullParser;

public class PluralRules implements Serializable {
    private static final /* synthetic */ int[] -android-icu-text-PluralRules$SampleTypeSwitchesValues = null;
    static final UnicodeSet ALLOWED_ID = null;
    static final Pattern AND_SEPARATED = null;
    static final Pattern AT_SEPARATED = null;
    @Deprecated
    public static final String CATEGORY_SEPARATOR = ";  ";
    static final Pattern COMMA_SEPARATED = null;
    public static final PluralRules DEFAULT = null;
    private static final Rule DEFAULT_RULE = null;
    static final Pattern DOTDOT_SEPARATED = null;
    public static final String KEYWORD_FEW = "few";
    public static final String KEYWORD_MANY = "many";
    public static final String KEYWORD_ONE = "one";
    public static final String KEYWORD_OTHER = "other";
    @Deprecated
    public static final String KEYWORD_RULE_SEPARATOR = ": ";
    public static final String KEYWORD_TWO = "two";
    public static final String KEYWORD_ZERO = "zero";
    private static final Constraint NO_CONSTRAINT = null;
    public static final double NO_UNIQUE_VALUE = -0.00123456777d;
    static final Pattern OR_SEPARATED = null;
    static final Pattern SEMI_SEPARATED = null;
    static final Pattern TILDE_SEPARATED = null;
    private static final long serialVersionUID = 1;
    private final transient Set<String> keywords;
    private final RuleList rules;

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

    private interface Constraint extends Serializable {
        boolean isFulfilled(FixedDecimal fixedDecimal);

        boolean isLimited(SampleType sampleType);
    }

    private static abstract class BinaryConstraint implements Constraint, Serializable {
        private static final long serialVersionUID = 1;
        protected final Constraint a;
        protected final Constraint b;

        protected BinaryConstraint(Constraint a, Constraint b) {
            this.a = a;
            this.b = b;
        }
    }

    private static class AndConstraint extends BinaryConstraint {
        private static final long serialVersionUID = 7766999779862263523L;

        AndConstraint(Constraint a, Constraint b) {
            super(a, b);
        }

        public boolean isFulfilled(FixedDecimal n) {
            if (this.a.isFulfilled(n)) {
                return this.b.isFulfilled(n);
            }
            return false;
        }

        public boolean isLimited(SampleType sampleType) {
            if (this.a.isLimited(sampleType)) {
                return true;
            }
            return this.b.isLimited(sampleType);
        }

        public String toString() {
            return this.a.toString() + " and " + this.b.toString();
        }
    }

    @Deprecated
    public static class FixedDecimal extends Number implements Comparable<FixedDecimal> {
        private static final /* synthetic */ int[] -android-icu-text-PluralRules$OperandSwitchesValues = null;
        static final long MAX = 1000000000000000000L;
        private static final long MAX_INTEGER_PART = 1000000000;
        private static final long serialVersionUID = -4756200506571685661L;
        private final int baseFactor;
        @Deprecated
        public final long decimalDigits;
        @Deprecated
        public final long decimalDigitsWithoutTrailingZeros;
        @Deprecated
        public final boolean hasIntegerValue;
        @Deprecated
        public final long integerValue;
        @Deprecated
        public final boolean isNegative;
        @Deprecated
        public final double source;
        @Deprecated
        public final int visibleDecimalDigitCount;
        @Deprecated
        public final int visibleDecimalDigitCountWithoutTrailingZeros;

        private static /* synthetic */ int[] -getandroid-icu-text-PluralRules$OperandSwitchesValues() {
            if (-android-icu-text-PluralRules$OperandSwitchesValues != null) {
                return -android-icu-text-PluralRules$OperandSwitchesValues;
            }
            int[] iArr = new int[Operand.values().length];
            try {
                iArr[Operand.f.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[Operand.i.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                iArr[Operand.j.ordinal()] = 6;
            } catch (NoSuchFieldError e3) {
            }
            try {
                iArr[Operand.n.ordinal()] = 7;
            } catch (NoSuchFieldError e4) {
            }
            try {
                iArr[Operand.t.ordinal()] = 3;
            } catch (NoSuchFieldError e5) {
            }
            try {
                iArr[Operand.v.ordinal()] = 4;
            } catch (NoSuchFieldError e6) {
            }
            try {
                iArr[Operand.w.ordinal()] = 5;
            } catch (NoSuchFieldError e7) {
            }
            -android-icu-text-PluralRules$OperandSwitchesValues = iArr;
            return iArr;
        }

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
            double d;
            long j;
            boolean z;
            this.isNegative = n < 0.0d;
            if (this.isNegative) {
                d = -n;
            } else {
                d = n;
            }
            this.source = d;
            this.visibleDecimalDigitCount = v;
            this.decimalDigits = f;
            if (n > 1.0E18d) {
                j = MAX;
            } else {
                j = (long) n;
            }
            this.integerValue = j;
            if (this.source == ((double) this.integerValue)) {
                z = true;
            } else {
                z = false;
            }
            this.hasIntegerValue = z;
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
            int baseFactor = (int) Math.pow(10.0d, (double) v);
            return (int) (Math.round(((double) baseFactor) * n) % ((long) baseFactor));
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
            int ePos = buf.lastIndexOf(Opcodes.OP_SGET_CHAR);
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
            value = value.trim();
            int decimalPos = value.indexOf(46) + 1;
            if (decimalPos == 0) {
                return 0;
            }
            return value.length() - decimalPos;
        }

        @Deprecated
        public double get(Operand operand) {
            switch (-getandroid-icu-text-PluralRules$OperandSwitchesValues()[operand.ordinal()]) {
                case NodeFilter.SHOW_ELEMENT /*1*/:
                    return (double) this.decimalDigits;
                case NodeFilter.SHOW_ATTRIBUTE /*2*/:
                    return (double) this.integerValue;
                case XmlPullParser.END_TAG /*3*/:
                    return (double) this.decimalDigitsWithoutTrailingZeros;
                case NodeFilter.SHOW_TEXT /*4*/:
                    return (double) this.visibleDecimalDigitCount;
                case XmlPullParser.CDSECT /*5*/:
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
            int i = -1;
            if (this.integerValue != other.integerValue) {
                if (this.integerValue >= other.integerValue) {
                    i = 1;
                }
                return i;
            } else if (this.source != other.source) {
                if (this.source >= other.source) {
                    i = 1;
                }
                return i;
            } else if (this.visibleDecimalDigitCount != other.visibleDecimalDigitCount) {
                if (this.visibleDecimalDigitCount >= other.visibleDecimalDigitCount) {
                    i = 1;
                }
                return i;
            } else {
                long diff = this.decimalDigits - other.decimalDigits;
                if (diff == 0) {
                    return 0;
                }
                if (diff >= 0) {
                    i = 1;
                }
                return i;
            }
        }

        @Deprecated
        public boolean equals(Object arg0) {
            boolean z = true;
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
            if (!(this.source == other.source && this.visibleDecimalDigitCount == other.visibleDecimalDigitCount && this.decimalDigits == other.decimalDigits)) {
                z = false;
            }
            return z;
        }

        @Deprecated
        public int hashCode() {
            return (int) (this.decimalDigits + ((long) ((this.visibleDecimalDigitCount + ((int) (this.source * 37.0d))) * 37)));
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
    }

    @Deprecated
    public static class FixedDecimalRange {
        @Deprecated
        public final FixedDecimal end;
        @Deprecated
        public final FixedDecimal start;

        @Deprecated
        public FixedDecimalRange(FixedDecimal start, FixedDecimal end) {
            if (start.visibleDecimalDigitCount != end.visibleDecimalDigitCount) {
                throw new IllegalArgumentException("Ranges must have the same number of visible decimals: " + start + "~" + end);
            }
            this.start = start;
            this.end = end;
        }

        @Deprecated
        public String toString() {
            return this.start + (this.end == this.start ? XmlPullParser.NO_NAMESPACE : "~" + this.end);
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

        private FixedDecimalSamples(SampleType sampleType, Set<FixedDecimalRange> samples, boolean bounded) {
            this.sampleType = sampleType;
            this.samples = samples;
            this.bounded = bounded;
        }

        static FixedDecimalSamples parse(String source) {
            SampleType sampleType2;
            boolean bounded2 = true;
            boolean haveBound = false;
            Set<FixedDecimalRange> samples2 = new LinkedHashSet();
            if (source.startsWith("integer")) {
                sampleType2 = SampleType.INTEGER;
            } else if (source.startsWith("decimal")) {
                sampleType2 = SampleType.DECIMAL;
            } else {
                throw new IllegalArgumentException("Samples must start with 'integer' or 'decimal'");
            }
            for (String range : PluralRules.COMMA_SEPARATED.split(source.substring(7).trim())) {
                if (range.equals("\u2026") || range.equals("...")) {
                    bounded2 = false;
                    haveBound = true;
                } else if (haveBound) {
                    throw new IllegalArgumentException("Can only have \u2026 at the end of samples: " + range);
                } else {
                    String[] rangeParts = PluralRules.TILDE_SEPARATED.split(range);
                    switch (rangeParts.length) {
                        case NodeFilter.SHOW_ELEMENT /*1*/:
                            FixedDecimal sample = new FixedDecimal(rangeParts[0]);
                            checkDecimal(sampleType2, sample);
                            samples2.add(new FixedDecimalRange(sample, sample));
                            break;
                        case NodeFilter.SHOW_ATTRIBUTE /*2*/:
                            FixedDecimal start = new FixedDecimal(rangeParts[0]);
                            FixedDecimal end = new FixedDecimal(rangeParts[1]);
                            checkDecimal(sampleType2, start);
                            checkDecimal(sampleType2, end);
                            samples2.add(new FixedDecimalRange(start, end));
                            break;
                        default:
                            throw new IllegalArgumentException("Ill-formed number range: " + range);
                    }
                }
            }
            return new FixedDecimalSamples(sampleType2, Collections.unmodifiableSet(samples2), bounded2);
        }

        private static void checkDecimal(SampleType sampleType2, FixedDecimal sample) {
            Object obj;
            Object obj2 = 1;
            if (sampleType2 == SampleType.INTEGER) {
                obj = 1;
            } else {
                obj = null;
            }
            if (sample.getVisibleDecimalDigitCount() != 0) {
                obj2 = null;
            }
            if (obj != obj2) {
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
                b.append(' ').append(item);
            }
            if (!this.bounded) {
                b.append(", \u2026");
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

    public enum KeywordStatus {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.PluralRules.KeywordStatus.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.PluralRules.KeywordStatus.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.text.PluralRules.KeywordStatus.<clinit>():void");
        }
    }

    private enum Operand {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.PluralRules.Operand.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.PluralRules.Operand.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.text.PluralRules.Operand.<clinit>():void");
        }
    }

    private static class OrConstraint extends BinaryConstraint {
        private static final long serialVersionUID = 1405488568664762222L;

        OrConstraint(Constraint a, Constraint b) {
            super(a, b);
        }

        public boolean isFulfilled(FixedDecimal n) {
            if (this.a.isFulfilled(n)) {
                return true;
            }
            return this.b.isFulfilled(n);
        }

        public boolean isLimited(SampleType sampleType) {
            if (this.a.isLimited(sampleType)) {
                return this.b.isLimited(sampleType);
            }
            return false;
        }

        public String toString() {
            return this.a.toString() + " or " + this.b.toString();
        }
    }

    public enum PluralType {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.PluralRules.PluralType.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.PluralRules.PluralType.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.text.PluralRules.PluralType.<clinit>():void");
        }
    }

    private static class RangeConstraint implements Constraint, Serializable {
        private static final /* synthetic */ int[] -android-icu-text-PluralRules$SampleTypeSwitchesValues = null;
        private static final long serialVersionUID = 1;
        private final boolean inRange;
        private final boolean integersOnly;
        private final double lowerBound;
        private final int mod;
        private final Operand operand;
        private final long[] range_list;
        private final double upperBound;

        private static /* synthetic */ int[] -getandroid-icu-text-PluralRules$SampleTypeSwitchesValues() {
            if (-android-icu-text-PluralRules$SampleTypeSwitchesValues != null) {
                return -android-icu-text-PluralRules$SampleTypeSwitchesValues;
            }
            int[] iArr = new int[SampleType.values().length];
            try {
                iArr[SampleType.DECIMAL.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[SampleType.INTEGER.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            -android-icu-text-PluralRules$SampleTypeSwitchesValues = iArr;
            return iArr;
        }

        RangeConstraint(int mod, boolean inRange, Operand operand, boolean integersOnly, double lowBound, double highBound, long[] vals) {
            this.mod = mod;
            this.inRange = inRange;
            this.integersOnly = integersOnly;
            this.lowerBound = lowBound;
            this.upperBound = highBound;
            this.range_list = vals;
            this.operand = operand;
        }

        public boolean isFulfilled(FixedDecimal number) {
            boolean z = true;
            boolean z2 = false;
            double n = number.get(this.operand);
            if ((!this.integersOnly || n - ((double) ((long) n)) == 0.0d) && (this.operand != Operand.j || number.visibleDecimalDigitCount == 0)) {
                boolean z3;
                if (this.mod != 0) {
                    n %= (double) this.mod;
                }
                if (n < this.lowerBound || n > this.upperBound) {
                    z3 = false;
                } else {
                    z3 = true;
                }
                if (z3 && this.range_list != null) {
                    z3 = false;
                    int i = 0;
                    while (!z3 && i < this.range_list.length) {
                        z3 = n >= ((double) this.range_list[i]) && n <= ((double) this.range_list[i + 1]);
                        i += 2;
                    }
                }
                if (this.inRange != z3) {
                    z = false;
                }
                return z;
            }
            if (!this.inRange) {
                z2 = true;
            }
            return z2;
        }

        public boolean isLimited(SampleType sampleType) {
            boolean z = false;
            boolean valueIsZero = this.lowerBound == this.upperBound && this.lowerBound == 0.0d;
            boolean hasDecimals = (this.operand == Operand.v || this.operand == Operand.w || this.operand == Operand.f || this.operand == Operand.t) ? this.inRange != valueIsZero : false;
            switch (-getandroid-icu-text-PluralRules$SampleTypeSwitchesValues()[sampleType.ordinal()]) {
                case NodeFilter.SHOW_ELEMENT /*1*/:
                    if (hasDecimals && this.operand != Operand.n) {
                        if (this.operand == Operand.j) {
                        }
                        return z;
                    }
                    if ((this.integersOnly || this.lowerBound == this.upperBound) && this.mod == 0) {
                        z = this.inRange;
                    }
                    return z;
                case NodeFilter.SHOW_ATTRIBUTE /*2*/:
                    if (hasDecimals) {
                        z = true;
                    } else {
                        if (!(this.operand == Operand.n || this.operand == Operand.i)) {
                            if (this.operand == Operand.j) {
                            }
                        }
                        if (this.mod == 0) {
                            z = this.inRange;
                        }
                    }
                    return z;
                default:
                    return false;
            }
        }

        public String toString() {
            boolean isList;
            StringBuilder result = new StringBuilder();
            result.append(this.operand);
            if (this.mod != 0) {
                result.append(" % ").append(this.mod);
            }
            if (this.lowerBound != this.upperBound) {
                isList = true;
            } else {
                isList = false;
            }
            String str = !isList ? this.inRange ? " = " : " != " : this.integersOnly ? this.inRange ? " = " : " != " : this.inRange ? " within " : " not within ";
            result.append(str);
            if (this.range_list != null) {
                for (int i = 0; i < this.range_list.length; i += 2) {
                    boolean z;
                    double d = (double) this.range_list[i];
                    double d2 = (double) this.range_list[i + 1];
                    if (i != 0) {
                        z = true;
                    } else {
                        z = false;
                    }
                    PluralRules.addRange(result, d, d2, z);
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
        private final FixedDecimalSamples decimalSamples;
        private final FixedDecimalSamples integerSamples;
        private final String keyword;

        public Rule(String keyword, Constraint constraint, FixedDecimalSamples integerSamples, FixedDecimalSamples decimalSamples) {
            this.keyword = keyword;
            this.constraint = constraint;
            this.integerSamples = integerSamples;
            this.decimalSamples = decimalSamples;
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

        public boolean appliesTo(FixedDecimal n) {
            return this.constraint.isFulfilled(n);
        }

        public boolean isLimited(SampleType sampleType) {
            return this.constraint.isLimited(sampleType);
        }

        public String toString() {
            return this.keyword + PluralRules.KEYWORD_RULE_SEPARATOR + this.constraint.toString() + (this.integerSamples == null ? XmlPullParser.NO_NAMESPACE : " " + this.integerSamples.toString()) + (this.decimalSamples == null ? XmlPullParser.NO_NAMESPACE : " " + this.decimalSamples.toString());
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
        private boolean hasExplicitBoundingInfo;
        private final List<Rule> rules;

        /* synthetic */ RuleList(RuleList ruleList) {
            this();
        }

        private RuleList() {
            this.hasExplicitBoundingInfo = false;
            this.rules = new ArrayList();
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
            Object otherRule = null;
            Iterator<Rule> it = this.rules.iterator();
            while (it.hasNext()) {
                Rule rule = (Rule) it.next();
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

        private Rule selectRule(FixedDecimal n) {
            for (Rule rule : this.rules) {
                if (rule.appliesTo(n)) {
                    return rule;
                }
            }
            return null;
        }

        public String select(FixedDecimal n) {
            if (Double.isInfinite(n.source) || Double.isNaN(n.source)) {
                return PluralRules.KEYWORD_OTHER;
            }
            return selectRule(n).getKeyword();
        }

        public Set<String> getKeywords() {
            Set<String> result = new LinkedHashSet();
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

        public boolean select(FixedDecimal sample, String keyword) {
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
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.PluralRules.SampleType.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.PluralRules.SampleType.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.text.PluralRules.SampleType.<clinit>():void");
        }
    }

    static class SimpleTokenizer {
        static final UnicodeSet BREAK_AND_IGNORE = null;
        static final UnicodeSet BREAK_AND_KEEP = null;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.PluralRules.SimpleTokenizer.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.PluralRules.SimpleTokenizer.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.text.PluralRules.SimpleTokenizer.<clinit>():void");
        }

        SimpleTokenizer() {
        }

        static String[] split(String source) {
            int last = -1;
            List<String> result = new ArrayList();
            for (int i = 0; i < source.length(); i++) {
                int ch = source.charAt(i);
                if (BREAK_AND_IGNORE.contains(ch)) {
                    if (last >= 0) {
                        result.add(source.substring(last, i));
                        last = -1;
                    }
                } else if (BREAK_AND_KEEP.contains(ch)) {
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

    private static /* synthetic */ int[] -getandroid-icu-text-PluralRules$SampleTypeSwitchesValues() {
        if (-android-icu-text-PluralRules$SampleTypeSwitchesValues != null) {
            return -android-icu-text-PluralRules$SampleTypeSwitchesValues;
        }
        int[] iArr = new int[SampleType.values().length];
        try {
            iArr[SampleType.DECIMAL.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[SampleType.INTEGER.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        -android-icu-text-PluralRules$SampleTypeSwitchesValues = iArr;
        return iArr;
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.PluralRules.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.PluralRules.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.icu.text.PluralRules.<clinit>():void");
    }

    public static PluralRules parseDescription(String description) throws ParseException {
        description = description.trim();
        return description.length() == 0 ? DEFAULT : new PluralRules(parseRuleChain(description));
    }

    public static PluralRules createRules(String description) {
        try {
            return parseDescription(description);
        } catch (Exception e) {
            return null;
        }
    }

    private static Constraint parseConstraint(String description) throws ParseException {
        Constraint result = null;
        String[] or_together = OR_SEPARATED.split(description);
        loop0:
        for (CharSequence split : or_together) {
            Constraint andConstraint = null;
            String[] and_together = AND_SEPARATED.split(split);
            int j = 0;
            while (j < and_together.length) {
                Constraint newConstraint = NO_CONSTRAINT;
                String condition = and_together[j].trim();
                String[] tokens = SimpleTokenizer.split(condition);
                int mod = 0;
                boolean inRange = true;
                boolean integersOnly = true;
                double lowBound = 9.223372036854776E18d;
                double highBound = -9.223372036854776E18d;
                String t = tokens[0];
                boolean z = false;
                try {
                    Operand operand = FixedDecimal.getOperand(t);
                    if (1 < tokens.length) {
                        int i;
                        List<Long> valueList;
                        long low;
                        long high;
                        long[] vals;
                        int k;
                        int x = 1 + 1;
                        t = tokens[1];
                        if (!"mod".equals(t)) {
                            if (!"%".equals(t)) {
                                i = x;
                                if ("not".equals(t)) {
                                    if ("!".equals(t)) {
                                        inRange = 1 != null;
                                        x = i + 1;
                                        t = nextToken(tokens, i, condition);
                                        if (!"=".equals(t)) {
                                            throw unexpected(t, condition);
                                        }
                                    }
                                    if (!"is".equals(t)) {
                                        if (!"in".equals(t)) {
                                            if (!"=".equals(t)) {
                                                if ("within".equals(t)) {
                                                    throw unexpected(t, condition);
                                                }
                                                integersOnly = false;
                                                x = i + 1;
                                                t = nextToken(tokens, i, condition);
                                                i = x;
                                                if ("not".equals(t)) {
                                                    if (!z || inRange) {
                                                        inRange = inRange;
                                                        x = i + 1;
                                                        t = nextToken(tokens, i, condition);
                                                        i = x;
                                                    } else {
                                                        throw unexpected(t, condition);
                                                    }
                                                }
                                                valueList = new ArrayList();
                                                while (true) {
                                                    low = Long.parseLong(t);
                                                    high = low;
                                                    if (i < tokens.length) {
                                                        x = i + 1;
                                                        t = nextToken(tokens, i, condition);
                                                        if (t.equals(".")) {
                                                            i = x + 1;
                                                            t = nextToken(tokens, x, condition);
                                                            if (!t.equals(".")) {
                                                                x = i + 1;
                                                                t = nextToken(tokens, i, condition);
                                                                high = Long.parseLong(t);
                                                                if (x < tokens.length) {
                                                                    i = x + 1;
                                                                    t = nextToken(tokens, x, condition);
                                                                    if (!t.equals(",")) {
                                                                        break loop0;
                                                                    }
                                                                }
                                                            }
                                                            break loop0;
                                                        }
                                                        if (!t.equals(",")) {
                                                            break loop0;
                                                        }
                                                        if (low > high) {
                                                            break loop0;
                                                        }
                                                        if (mod != 0) {
                                                            if (high >= ((long) mod)) {
                                                                break loop0;
                                                            }
                                                        }
                                                        valueList.add(Long.valueOf(low));
                                                        valueList.add(Long.valueOf(high));
                                                        lowBound = Math.min(lowBound, (double) low);
                                                        highBound = Math.max(highBound, (double) high);
                                                        if (x >= tokens.length) {
                                                            break;
                                                        }
                                                        i = x + 1;
                                                        t = nextToken(tokens, x, condition);
                                                    }
                                                    x = i;
                                                    if (low > high) {
                                                        break loop0;
                                                    }
                                                    if (mod != 0) {
                                                        if (high >= ((long) mod)) {
                                                            break loop0;
                                                        }
                                                    }
                                                    valueList.add(Long.valueOf(low));
                                                    valueList.add(Long.valueOf(high));
                                                    lowBound = Math.min(lowBound, (double) low);
                                                    highBound = Math.max(highBound, (double) high);
                                                    if (x >= tokens.length) {
                                                        break;
                                                    }
                                                    i = x + 1;
                                                    t = nextToken(tokens, x, condition);
                                                }
                                                throw unexpected(high + ">mod=" + mod, condition);
                                            }
                                        }
                                    }
                                    z = "is".equals(t);
                                    if (z || inRange) {
                                        x = i + 1;
                                        t = nextToken(tokens, i, condition);
                                        i = x;
                                        if ("not".equals(t)) {
                                            if (z) {
                                            }
                                            if (inRange) {
                                            }
                                            x = i + 1;
                                            t = nextToken(tokens, i, condition);
                                            i = x;
                                        }
                                        valueList = new ArrayList();
                                        while (true) {
                                            low = Long.parseLong(t);
                                            high = low;
                                            if (i < tokens.length) {
                                                x = i + 1;
                                                t = nextToken(tokens, i, condition);
                                                if (t.equals(".")) {
                                                    i = x + 1;
                                                    t = nextToken(tokens, x, condition);
                                                    if (!t.equals(".")) {
                                                        x = i + 1;
                                                        t = nextToken(tokens, i, condition);
                                                        high = Long.parseLong(t);
                                                        if (x < tokens.length) {
                                                            i = x + 1;
                                                            t = nextToken(tokens, x, condition);
                                                            if (t.equals(",")) {
                                                                break loop0;
                                                                throw unexpected(t, condition);
                                                            }
                                                        }
                                                    }
                                                    break loop0;
                                                    throw unexpected(t, condition);
                                                }
                                                if (t.equals(",")) {
                                                    break loop0;
                                                    throw unexpected(t, condition);
                                                }
                                                if (low > high) {
                                                    break loop0;
                                                    throw unexpected(low + "~" + high, condition);
                                                }
                                                if (mod != 0) {
                                                    if (high >= ((long) mod)) {
                                                        break loop0;
                                                        throw unexpected(high + ">mod=" + mod, condition);
                                                    }
                                                }
                                                valueList.add(Long.valueOf(low));
                                                valueList.add(Long.valueOf(high));
                                                lowBound = Math.min(lowBound, (double) low);
                                                highBound = Math.max(highBound, (double) high);
                                                if (x >= tokens.length) {
                                                    break;
                                                    if (t.equals(",")) {
                                                        if (valueList.size() != 2) {
                                                            vals = null;
                                                        } else {
                                                            vals = new long[valueList.size()];
                                                            for (k = 0; k < vals.length; k++) {
                                                                vals[k] = ((Long) valueList.get(k)).longValue();
                                                            }
                                                        }
                                                        if (lowBound == highBound && z && !inRange) {
                                                            throw unexpected("is not <range>", condition);
                                                        }
                                                        newConstraint = new RangeConstraint(mod, inRange, operand, integersOnly, lowBound, highBound, vals);
                                                        i = x;
                                                    } else {
                                                        throw unexpected(t, condition);
                                                    }
                                                }
                                                i = x + 1;
                                                t = nextToken(tokens, x, condition);
                                            }
                                            x = i;
                                            if (low > high) {
                                                if (mod != 0) {
                                                    if (high >= ((long) mod)) {
                                                        break loop0;
                                                        throw unexpected(high + ">mod=" + mod, condition);
                                                    }
                                                }
                                                valueList.add(Long.valueOf(low));
                                                valueList.add(Long.valueOf(high));
                                                lowBound = Math.min(lowBound, (double) low);
                                                highBound = Math.max(highBound, (double) high);
                                                if (x >= tokens.length) {
                                                    i = x + 1;
                                                    t = nextToken(tokens, x, condition);
                                                } else {
                                                    break;
                                                    if (t.equals(",")) {
                                                        if (valueList.size() != 2) {
                                                            vals = new long[valueList.size()];
                                                            for (k = 0; k < vals.length; k++) {
                                                                vals[k] = ((Long) valueList.get(k)).longValue();
                                                            }
                                                        } else {
                                                            vals = null;
                                                        }
                                                        if (lowBound == highBound) {
                                                        }
                                                        newConstraint = new RangeConstraint(mod, inRange, operand, integersOnly, lowBound, highBound, vals);
                                                        i = x;
                                                    } else {
                                                        throw unexpected(t, condition);
                                                    }
                                                }
                                            }
                                            break loop0;
                                            throw unexpected(low + "~" + high, condition);
                                        }
                                    }
                                    throw unexpected(t, condition);
                                }
                                inRange = 1 != null;
                                x = i + 1;
                                t = nextToken(tokens, i, condition);
                                if ("=".equals(t)) {
                                    throw unexpected(t, condition);
                                }
                                i = x;
                                if ("is".equals(t)) {
                                    if ("in".equals(t)) {
                                        if ("=".equals(t)) {
                                            if ("within".equals(t)) {
                                                throw unexpected(t, condition);
                                            }
                                            integersOnly = false;
                                            x = i + 1;
                                            t = nextToken(tokens, i, condition);
                                            i = x;
                                            if ("not".equals(t)) {
                                                if (z) {
                                                }
                                                if (inRange) {
                                                }
                                                x = i + 1;
                                                t = nextToken(tokens, i, condition);
                                                i = x;
                                            }
                                            valueList = new ArrayList();
                                            while (true) {
                                                low = Long.parseLong(t);
                                                high = low;
                                                if (i < tokens.length) {
                                                    x = i + 1;
                                                    t = nextToken(tokens, i, condition);
                                                    if (t.equals(".")) {
                                                        i = x + 1;
                                                        t = nextToken(tokens, x, condition);
                                                        if (!t.equals(".")) {
                                                            break loop0;
                                                            throw unexpected(t, condition);
                                                        }
                                                        x = i + 1;
                                                        t = nextToken(tokens, i, condition);
                                                        high = Long.parseLong(t);
                                                        if (x < tokens.length) {
                                                            i = x + 1;
                                                            t = nextToken(tokens, x, condition);
                                                            if (t.equals(",")) {
                                                                break loop0;
                                                                throw unexpected(t, condition);
                                                            }
                                                        }
                                                    }
                                                    if (t.equals(",")) {
                                                        break loop0;
                                                        throw unexpected(t, condition);
                                                    }
                                                    if (low > high) {
                                                        break loop0;
                                                        throw unexpected(low + "~" + high, condition);
                                                    }
                                                    if (mod != 0) {
                                                        if (high >= ((long) mod)) {
                                                            break loop0;
                                                            throw unexpected(high + ">mod=" + mod, condition);
                                                        }
                                                    }
                                                    valueList.add(Long.valueOf(low));
                                                    valueList.add(Long.valueOf(high));
                                                    lowBound = Math.min(lowBound, (double) low);
                                                    highBound = Math.max(highBound, (double) high);
                                                    if (x >= tokens.length) {
                                                        break;
                                                        if (t.equals(",")) {
                                                            throw unexpected(t, condition);
                                                        }
                                                        if (valueList.size() != 2) {
                                                            vals = null;
                                                        } else {
                                                            vals = new long[valueList.size()];
                                                            for (k = 0; k < vals.length; k++) {
                                                                vals[k] = ((Long) valueList.get(k)).longValue();
                                                            }
                                                        }
                                                        if (lowBound == highBound) {
                                                        }
                                                        newConstraint = new RangeConstraint(mod, inRange, operand, integersOnly, lowBound, highBound, vals);
                                                        i = x;
                                                    } else {
                                                        i = x + 1;
                                                        t = nextToken(tokens, x, condition);
                                                    }
                                                }
                                                x = i;
                                                if (low > high) {
                                                    if (mod != 0) {
                                                        if (high >= ((long) mod)) {
                                                            break loop0;
                                                            throw unexpected(high + ">mod=" + mod, condition);
                                                        }
                                                    }
                                                    valueList.add(Long.valueOf(low));
                                                    valueList.add(Long.valueOf(high));
                                                    lowBound = Math.min(lowBound, (double) low);
                                                    highBound = Math.max(highBound, (double) high);
                                                    if (x >= tokens.length) {
                                                        i = x + 1;
                                                        t = nextToken(tokens, x, condition);
                                                    } else {
                                                        break;
                                                        if (t.equals(",")) {
                                                            if (valueList.size() != 2) {
                                                                vals = new long[valueList.size()];
                                                                for (k = 0; k < vals.length; k++) {
                                                                    vals[k] = ((Long) valueList.get(k)).longValue();
                                                                }
                                                            } else {
                                                                vals = null;
                                                            }
                                                            if (lowBound == highBound) {
                                                            }
                                                            newConstraint = new RangeConstraint(mod, inRange, operand, integersOnly, lowBound, highBound, vals);
                                                            i = x;
                                                        } else {
                                                            throw unexpected(t, condition);
                                                        }
                                                    }
                                                }
                                                break loop0;
                                                throw unexpected(low + "~" + high, condition);
                                            }
                                        }
                                    }
                                }
                                z = "is".equals(t);
                                if (z) {
                                }
                                x = i + 1;
                                t = nextToken(tokens, i, condition);
                                i = x;
                                if ("not".equals(t)) {
                                    if (z) {
                                    }
                                    if (inRange) {
                                    }
                                    x = i + 1;
                                    t = nextToken(tokens, i, condition);
                                    i = x;
                                }
                                valueList = new ArrayList();
                                while (true) {
                                    low = Long.parseLong(t);
                                    high = low;
                                    if (i < tokens.length) {
                                        x = i + 1;
                                        t = nextToken(tokens, i, condition);
                                        if (t.equals(".")) {
                                            if (t.equals(",")) {
                                                break loop0;
                                                throw unexpected(t, condition);
                                            }
                                        }
                                        i = x + 1;
                                        t = nextToken(tokens, x, condition);
                                        if (!t.equals(".")) {
                                            x = i + 1;
                                            t = nextToken(tokens, i, condition);
                                            high = Long.parseLong(t);
                                            if (x < tokens.length) {
                                                i = x + 1;
                                                t = nextToken(tokens, x, condition);
                                                if (t.equals(",")) {
                                                    break loop0;
                                                    throw unexpected(t, condition);
                                                }
                                            }
                                        }
                                        break loop0;
                                        throw unexpected(t, condition);
                                        if (low > high) {
                                            break loop0;
                                            throw unexpected(low + "~" + high, condition);
                                        }
                                        if (mod != 0) {
                                            if (high >= ((long) mod)) {
                                                break loop0;
                                                throw unexpected(high + ">mod=" + mod, condition);
                                            }
                                        }
                                        valueList.add(Long.valueOf(low));
                                        valueList.add(Long.valueOf(high));
                                        lowBound = Math.min(lowBound, (double) low);
                                        highBound = Math.max(highBound, (double) high);
                                        if (x >= tokens.length) {
                                            break;
                                            if (t.equals(",")) {
                                                throw unexpected(t, condition);
                                            }
                                            if (valueList.size() != 2) {
                                                vals = null;
                                            } else {
                                                vals = new long[valueList.size()];
                                                for (k = 0; k < vals.length; k++) {
                                                    vals[k] = ((Long) valueList.get(k)).longValue();
                                                }
                                            }
                                            if (lowBound == highBound) {
                                            }
                                            newConstraint = new RangeConstraint(mod, inRange, operand, integersOnly, lowBound, highBound, vals);
                                            i = x;
                                        } else {
                                            i = x + 1;
                                            t = nextToken(tokens, x, condition);
                                        }
                                    }
                                    x = i;
                                    if (low > high) {
                                        if (mod != 0) {
                                            if (high >= ((long) mod)) {
                                                break loop0;
                                                throw unexpected(high + ">mod=" + mod, condition);
                                            }
                                        }
                                        valueList.add(Long.valueOf(low));
                                        valueList.add(Long.valueOf(high));
                                        lowBound = Math.min(lowBound, (double) low);
                                        highBound = Math.max(highBound, (double) high);
                                        if (x >= tokens.length) {
                                            i = x + 1;
                                            t = nextToken(tokens, x, condition);
                                        } else {
                                            break;
                                            if (t.equals(",")) {
                                                if (valueList.size() != 2) {
                                                    vals = new long[valueList.size()];
                                                    for (k = 0; k < vals.length; k++) {
                                                        vals[k] = ((Long) valueList.get(k)).longValue();
                                                    }
                                                } else {
                                                    vals = null;
                                                }
                                                if (lowBound == highBound) {
                                                }
                                                newConstraint = new RangeConstraint(mod, inRange, operand, integersOnly, lowBound, highBound, vals);
                                                i = x;
                                            } else {
                                                throw unexpected(t, condition);
                                            }
                                        }
                                    }
                                    break loop0;
                                    throw unexpected(low + "~" + high, condition);
                                }
                            }
                        }
                        i = x + 1;
                        mod = Integer.parseInt(tokens[x]);
                        x = i + 1;
                        t = nextToken(tokens, i, condition);
                        i = x;
                        if ("not".equals(t)) {
                            if ("!".equals(t)) {
                                if (1 != null) {
                                }
                                x = i + 1;
                                t = nextToken(tokens, i, condition);
                                if ("=".equals(t)) {
                                    throw unexpected(t, condition);
                                }
                            }
                            if ("is".equals(t)) {
                                if ("in".equals(t)) {
                                    if ("=".equals(t)) {
                                        if ("within".equals(t)) {
                                            integersOnly = false;
                                            x = i + 1;
                                            t = nextToken(tokens, i, condition);
                                            i = x;
                                            if ("not".equals(t)) {
                                                if (z) {
                                                }
                                                if (inRange) {
                                                }
                                                x = i + 1;
                                                t = nextToken(tokens, i, condition);
                                                i = x;
                                            }
                                            valueList = new ArrayList();
                                            while (true) {
                                                low = Long.parseLong(t);
                                                high = low;
                                                if (i < tokens.length) {
                                                    x = i + 1;
                                                    t = nextToken(tokens, i, condition);
                                                    if (t.equals(".")) {
                                                        i = x + 1;
                                                        t = nextToken(tokens, x, condition);
                                                        if (!t.equals(".")) {
                                                            break loop0;
                                                            throw unexpected(t, condition);
                                                        }
                                                        x = i + 1;
                                                        t = nextToken(tokens, i, condition);
                                                        high = Long.parseLong(t);
                                                        if (x < tokens.length) {
                                                            i = x + 1;
                                                            t = nextToken(tokens, x, condition);
                                                            if (t.equals(",")) {
                                                                break loop0;
                                                                throw unexpected(t, condition);
                                                            }
                                                        }
                                                    }
                                                    if (t.equals(",")) {
                                                        break loop0;
                                                        throw unexpected(t, condition);
                                                    }
                                                    if (low > high) {
                                                        break loop0;
                                                        throw unexpected(low + "~" + high, condition);
                                                    }
                                                    if (mod != 0) {
                                                        if (high >= ((long) mod)) {
                                                            break loop0;
                                                            throw unexpected(high + ">mod=" + mod, condition);
                                                        }
                                                    }
                                                    valueList.add(Long.valueOf(low));
                                                    valueList.add(Long.valueOf(high));
                                                    lowBound = Math.min(lowBound, (double) low);
                                                    highBound = Math.max(highBound, (double) high);
                                                    if (x >= tokens.length) {
                                                        break;
                                                        if (t.equals(",")) {
                                                            throw unexpected(t, condition);
                                                        }
                                                        if (valueList.size() != 2) {
                                                            vals = null;
                                                        } else {
                                                            vals = new long[valueList.size()];
                                                            for (k = 0; k < vals.length; k++) {
                                                                vals[k] = ((Long) valueList.get(k)).longValue();
                                                            }
                                                        }
                                                        if (lowBound == highBound) {
                                                        }
                                                        newConstraint = new RangeConstraint(mod, inRange, operand, integersOnly, lowBound, highBound, vals);
                                                        i = x;
                                                    } else {
                                                        i = x + 1;
                                                        t = nextToken(tokens, x, condition);
                                                    }
                                                }
                                                x = i;
                                                if (low > high) {
                                                    if (mod != 0) {
                                                        if (high >= ((long) mod)) {
                                                            break loop0;
                                                            throw unexpected(high + ">mod=" + mod, condition);
                                                        }
                                                    }
                                                    valueList.add(Long.valueOf(low));
                                                    valueList.add(Long.valueOf(high));
                                                    lowBound = Math.min(lowBound, (double) low);
                                                    highBound = Math.max(highBound, (double) high);
                                                    if (x >= tokens.length) {
                                                        i = x + 1;
                                                        t = nextToken(tokens, x, condition);
                                                    } else {
                                                        break;
                                                        if (t.equals(",")) {
                                                            if (valueList.size() != 2) {
                                                                vals = new long[valueList.size()];
                                                                for (k = 0; k < vals.length; k++) {
                                                                    vals[k] = ((Long) valueList.get(k)).longValue();
                                                                }
                                                            } else {
                                                                vals = null;
                                                            }
                                                            if (lowBound == highBound) {
                                                            }
                                                            newConstraint = new RangeConstraint(mod, inRange, operand, integersOnly, lowBound, highBound, vals);
                                                            i = x;
                                                        } else {
                                                            throw unexpected(t, condition);
                                                        }
                                                    }
                                                }
                                                break loop0;
                                                throw unexpected(low + "~" + high, condition);
                                            }
                                        }
                                        throw unexpected(t, condition);
                                    }
                                }
                            }
                            z = "is".equals(t);
                            if (z) {
                            }
                            x = i + 1;
                            t = nextToken(tokens, i, condition);
                            i = x;
                            if ("not".equals(t)) {
                                if (z) {
                                }
                                if (inRange) {
                                }
                                x = i + 1;
                                t = nextToken(tokens, i, condition);
                                i = x;
                            }
                            valueList = new ArrayList();
                            while (true) {
                                low = Long.parseLong(t);
                                high = low;
                                if (i < tokens.length) {
                                    x = i + 1;
                                    t = nextToken(tokens, i, condition);
                                    if (t.equals(".")) {
                                        if (t.equals(",")) {
                                            break loop0;
                                            throw unexpected(t, condition);
                                        }
                                    }
                                    i = x + 1;
                                    t = nextToken(tokens, x, condition);
                                    if (!t.equals(".")) {
                                        x = i + 1;
                                        t = nextToken(tokens, i, condition);
                                        high = Long.parseLong(t);
                                        if (x < tokens.length) {
                                            i = x + 1;
                                            t = nextToken(tokens, x, condition);
                                            if (t.equals(",")) {
                                                break loop0;
                                                throw unexpected(t, condition);
                                            }
                                        }
                                    }
                                    break loop0;
                                    throw unexpected(t, condition);
                                    if (low > high) {
                                        break loop0;
                                        throw unexpected(low + "~" + high, condition);
                                    }
                                    if (mod != 0) {
                                        if (high >= ((long) mod)) {
                                            break loop0;
                                            throw unexpected(high + ">mod=" + mod, condition);
                                        }
                                    }
                                    valueList.add(Long.valueOf(low));
                                    valueList.add(Long.valueOf(high));
                                    lowBound = Math.min(lowBound, (double) low);
                                    highBound = Math.max(highBound, (double) high);
                                    if (x >= tokens.length) {
                                        break;
                                        if (t.equals(",")) {
                                            throw unexpected(t, condition);
                                        }
                                        if (valueList.size() != 2) {
                                            vals = null;
                                        } else {
                                            vals = new long[valueList.size()];
                                            for (k = 0; k < vals.length; k++) {
                                                vals[k] = ((Long) valueList.get(k)).longValue();
                                            }
                                        }
                                        if (lowBound == highBound) {
                                        }
                                        newConstraint = new RangeConstraint(mod, inRange, operand, integersOnly, lowBound, highBound, vals);
                                        i = x;
                                    } else {
                                        i = x + 1;
                                        t = nextToken(tokens, x, condition);
                                    }
                                }
                                x = i;
                                if (low > high) {
                                    if (mod != 0) {
                                        if (high >= ((long) mod)) {
                                            break loop0;
                                            throw unexpected(high + ">mod=" + mod, condition);
                                        }
                                    }
                                    valueList.add(Long.valueOf(low));
                                    valueList.add(Long.valueOf(high));
                                    lowBound = Math.min(lowBound, (double) low);
                                    highBound = Math.max(highBound, (double) high);
                                    if (x >= tokens.length) {
                                        i = x + 1;
                                        t = nextToken(tokens, x, condition);
                                    } else {
                                        break;
                                        if (t.equals(",")) {
                                            if (valueList.size() != 2) {
                                                vals = new long[valueList.size()];
                                                for (k = 0; k < vals.length; k++) {
                                                    vals[k] = ((Long) valueList.get(k)).longValue();
                                                }
                                            } else {
                                                vals = null;
                                            }
                                            if (lowBound == highBound) {
                                            }
                                            newConstraint = new RangeConstraint(mod, inRange, operand, integersOnly, lowBound, highBound, vals);
                                            i = x;
                                        } else {
                                            throw unexpected(t, condition);
                                        }
                                    }
                                }
                                break loop0;
                                throw unexpected(low + "~" + high, condition);
                            }
                        }
                        if (1 != null) {
                        }
                        x = i + 1;
                        t = nextToken(tokens, i, condition);
                        if ("=".equals(t)) {
                            throw unexpected(t, condition);
                        }
                        i = x;
                        if ("is".equals(t)) {
                            if ("in".equals(t)) {
                                if ("=".equals(t)) {
                                    if ("within".equals(t)) {
                                        throw unexpected(t, condition);
                                    }
                                    integersOnly = false;
                                    x = i + 1;
                                    t = nextToken(tokens, i, condition);
                                    i = x;
                                    if ("not".equals(t)) {
                                        if (z) {
                                        }
                                        if (inRange) {
                                        }
                                        x = i + 1;
                                        t = nextToken(tokens, i, condition);
                                        i = x;
                                    }
                                    valueList = new ArrayList();
                                    while (true) {
                                        low = Long.parseLong(t);
                                        high = low;
                                        if (i < tokens.length) {
                                            x = i + 1;
                                            t = nextToken(tokens, i, condition);
                                            if (t.equals(".")) {
                                                i = x + 1;
                                                t = nextToken(tokens, x, condition);
                                                if (!t.equals(".")) {
                                                    break loop0;
                                                    throw unexpected(t, condition);
                                                }
                                                x = i + 1;
                                                t = nextToken(tokens, i, condition);
                                                high = Long.parseLong(t);
                                                if (x < tokens.length) {
                                                    i = x + 1;
                                                    t = nextToken(tokens, x, condition);
                                                    if (t.equals(",")) {
                                                        break loop0;
                                                        throw unexpected(t, condition);
                                                    }
                                                }
                                            }
                                            if (t.equals(",")) {
                                                break loop0;
                                                throw unexpected(t, condition);
                                            }
                                            if (low > high) {
                                                break loop0;
                                                throw unexpected(low + "~" + high, condition);
                                            }
                                            if (mod != 0) {
                                                if (high >= ((long) mod)) {
                                                    break loop0;
                                                    throw unexpected(high + ">mod=" + mod, condition);
                                                }
                                            }
                                            valueList.add(Long.valueOf(low));
                                            valueList.add(Long.valueOf(high));
                                            lowBound = Math.min(lowBound, (double) low);
                                            highBound = Math.max(highBound, (double) high);
                                            if (x >= tokens.length) {
                                                break;
                                                if (t.equals(",")) {
                                                    throw unexpected(t, condition);
                                                }
                                                if (valueList.size() != 2) {
                                                    vals = null;
                                                } else {
                                                    vals = new long[valueList.size()];
                                                    for (k = 0; k < vals.length; k++) {
                                                        vals[k] = ((Long) valueList.get(k)).longValue();
                                                    }
                                                }
                                                if (lowBound == highBound) {
                                                }
                                                newConstraint = new RangeConstraint(mod, inRange, operand, integersOnly, lowBound, highBound, vals);
                                                i = x;
                                            } else {
                                                i = x + 1;
                                                t = nextToken(tokens, x, condition);
                                            }
                                        }
                                        x = i;
                                        if (low > high) {
                                            if (mod != 0) {
                                                if (high >= ((long) mod)) {
                                                    break loop0;
                                                    throw unexpected(high + ">mod=" + mod, condition);
                                                }
                                            }
                                            valueList.add(Long.valueOf(low));
                                            valueList.add(Long.valueOf(high));
                                            lowBound = Math.min(lowBound, (double) low);
                                            highBound = Math.max(highBound, (double) high);
                                            if (x >= tokens.length) {
                                                i = x + 1;
                                                t = nextToken(tokens, x, condition);
                                            } else {
                                                break;
                                                if (t.equals(",")) {
                                                    if (valueList.size() != 2) {
                                                        vals = new long[valueList.size()];
                                                        for (k = 0; k < vals.length; k++) {
                                                            vals[k] = ((Long) valueList.get(k)).longValue();
                                                        }
                                                    } else {
                                                        vals = null;
                                                    }
                                                    if (lowBound == highBound) {
                                                    }
                                                    newConstraint = new RangeConstraint(mod, inRange, operand, integersOnly, lowBound, highBound, vals);
                                                    i = x;
                                                } else {
                                                    throw unexpected(t, condition);
                                                }
                                            }
                                        }
                                        break loop0;
                                        throw unexpected(low + "~" + high, condition);
                                    }
                                }
                            }
                        }
                        z = "is".equals(t);
                        if (z) {
                        }
                        x = i + 1;
                        t = nextToken(tokens, i, condition);
                        i = x;
                        if ("not".equals(t)) {
                            if (z) {
                            }
                            if (inRange) {
                            }
                            x = i + 1;
                            t = nextToken(tokens, i, condition);
                            i = x;
                        }
                        valueList = new ArrayList();
                        while (true) {
                            low = Long.parseLong(t);
                            high = low;
                            if (i < tokens.length) {
                                x = i + 1;
                                t = nextToken(tokens, i, condition);
                                if (t.equals(".")) {
                                    if (t.equals(",")) {
                                        break loop0;
                                        throw unexpected(t, condition);
                                    }
                                }
                                i = x + 1;
                                t = nextToken(tokens, x, condition);
                                if (!t.equals(".")) {
                                    x = i + 1;
                                    t = nextToken(tokens, i, condition);
                                    high = Long.parseLong(t);
                                    if (x < tokens.length) {
                                        i = x + 1;
                                        t = nextToken(tokens, x, condition);
                                        if (t.equals(",")) {
                                            break loop0;
                                            throw unexpected(t, condition);
                                        }
                                    }
                                }
                                break loop0;
                                throw unexpected(t, condition);
                                if (low > high) {
                                    break loop0;
                                    throw unexpected(low + "~" + high, condition);
                                }
                                if (mod != 0) {
                                    if (high >= ((long) mod)) {
                                        break loop0;
                                        throw unexpected(high + ">mod=" + mod, condition);
                                    }
                                }
                                valueList.add(Long.valueOf(low));
                                valueList.add(Long.valueOf(high));
                                lowBound = Math.min(lowBound, (double) low);
                                highBound = Math.max(highBound, (double) high);
                                if (x >= tokens.length) {
                                    break;
                                    if (t.equals(",")) {
                                        throw unexpected(t, condition);
                                    }
                                    if (valueList.size() != 2) {
                                        vals = null;
                                    } else {
                                        vals = new long[valueList.size()];
                                        for (k = 0; k < vals.length; k++) {
                                            vals[k] = ((Long) valueList.get(k)).longValue();
                                        }
                                    }
                                    if (lowBound == highBound) {
                                    }
                                    newConstraint = new RangeConstraint(mod, inRange, operand, integersOnly, lowBound, highBound, vals);
                                    i = x;
                                } else {
                                    i = x + 1;
                                    t = nextToken(tokens, x, condition);
                                }
                            }
                            x = i;
                            if (low > high) {
                                if (mod != 0) {
                                    if (high >= ((long) mod)) {
                                        break loop0;
                                        throw unexpected(high + ">mod=" + mod, condition);
                                    }
                                }
                                valueList.add(Long.valueOf(low));
                                valueList.add(Long.valueOf(high));
                                lowBound = Math.min(lowBound, (double) low);
                                highBound = Math.max(highBound, (double) high);
                                if (x >= tokens.length) {
                                    i = x + 1;
                                    t = nextToken(tokens, x, condition);
                                } else {
                                    break;
                                    if (t.equals(",")) {
                                        if (valueList.size() != 2) {
                                            vals = new long[valueList.size()];
                                            for (k = 0; k < vals.length; k++) {
                                                vals[k] = ((Long) valueList.get(k)).longValue();
                                            }
                                        } else {
                                            vals = null;
                                        }
                                        if (lowBound == highBound) {
                                        }
                                        newConstraint = new RangeConstraint(mod, inRange, operand, integersOnly, lowBound, highBound, vals);
                                        i = x;
                                    } else {
                                        throw unexpected(t, condition);
                                    }
                                }
                            }
                            break loop0;
                            throw unexpected(low + "~" + high, condition);
                        }
                    }
                    if (andConstraint == null) {
                        andConstraint = newConstraint;
                    } else {
                        andConstraint = new AndConstraint(andConstraint, newConstraint);
                    }
                    j++;
                } catch (Exception e) {
                    throw unexpected(t, condition);
                }
            }
            if (result == null) {
                result = andConstraint;
            } else {
                result = new OrConstraint(result, andConstraint);
            }
        }
        return result;
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

    private static Rule parseRule(String description) throws ParseException {
        boolean z = true;
        if (description.length() == 0) {
            return DEFAULT_RULE;
        }
        description = description.toLowerCase(Locale.ENGLISH);
        int x = description.indexOf(58);
        if (x == -1) {
            throw new ParseException("missing ':' in rule description '" + description + "'", 0);
        }
        String keyword = description.substring(0, x).trim();
        if (isValidKeyword(keyword)) {
            description = description.substring(x + 1).trim();
            String[] constraintOrSamples = AT_SEPARATED.split(description);
            FixedDecimalSamples fixedDecimalSamples = null;
            FixedDecimalSamples fixedDecimalSamples2 = null;
            switch (constraintOrSamples.length) {
                case NodeFilter.SHOW_ELEMENT /*1*/:
                    break;
                case NodeFilter.SHOW_ATTRIBUTE /*2*/:
                    fixedDecimalSamples = FixedDecimalSamples.parse(constraintOrSamples[1]);
                    if (fixedDecimalSamples.sampleType == SampleType.DECIMAL) {
                        fixedDecimalSamples2 = fixedDecimalSamples;
                        fixedDecimalSamples = null;
                        break;
                    }
                    break;
                case XmlPullParser.END_TAG /*3*/:
                    fixedDecimalSamples = FixedDecimalSamples.parse(constraintOrSamples[1]);
                    fixedDecimalSamples2 = FixedDecimalSamples.parse(constraintOrSamples[2]);
                    if (!(fixedDecimalSamples.sampleType == SampleType.INTEGER && fixedDecimalSamples2.sampleType == SampleType.DECIMAL)) {
                        throw new IllegalArgumentException("Must have @integer then @decimal in " + description);
                    }
                default:
                    throw new IllegalArgumentException("Too many samples in " + description);
            }
            if (false) {
                throw new IllegalArgumentException("Ill-formed samples\u2014'@' characters.");
            }
            boolean isOther = keyword.equals(KEYWORD_OTHER);
            if (constraintOrSamples[0].length() != 0) {
                z = false;
            }
            if (isOther != z) {
                throw new IllegalArgumentException("The keyword 'other' must have no constraints, just samples.");
            }
            Constraint constraint;
            if (isOther) {
                constraint = NO_CONSTRAINT;
            } else {
                constraint = parseConstraint(constraintOrSamples[0]);
            }
            return new Rule(keyword, constraint, fixedDecimalSamples, fixedDecimalSamples2);
        }
        throw new ParseException("keyword '" + keyword + " is not valid", 0);
    }

    private static RuleList parseRuleChain(String description) throws ParseException {
        RuleList result = new RuleList();
        if (description.endsWith(";")) {
            description = description.substring(0, description.length() - 1);
        }
        String[] rules = SEMI_SEPARATED.split(description);
        for (String trim : rules) {
            int i;
            Rule rule = parseRule(trim.trim());
            boolean -get0 = result.hasExplicitBoundingInfo;
            if (rule.integerSamples == null && rule.decimalSamples == null) {
                i = 0;
            } else {
                i = 1;
            }
            result.hasExplicitBoundingInfo = i | -get0;
            result.addRule(rule);
        }
        return result.finish();
    }

    private static void addRange(StringBuilder result, double lb, double ub, boolean addSeparator) {
        if (addSeparator) {
            result.append(",");
        }
        if (lb == ub) {
            result.append(format(lb));
        } else {
            result.append(format(lb)).append("..").append(format(ub));
        }
    }

    private static String format(double lb) {
        long lbi = (long) lb;
        return lb == ((double) lbi) ? String.valueOf(lbi) : String.valueOf(lb);
    }

    private boolean addConditional(Set<FixedDecimal> toAddTo, Set<FixedDecimal> others, double trial) {
        FixedDecimal toAdd = new FixedDecimal(trial);
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

    private PluralRules(RuleList rules) {
        this.rules = rules;
        this.keywords = Collections.unmodifiableSet(rules.getKeywords());
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
        return this.rules.select(new FixedDecimal(number, countVisibleFractionDigits, fractionaldigits));
    }

    @Deprecated
    public String select(FixedDecimal number) {
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
            return NO_UNIQUE_VALUE;
        }
        return ((Double) values.iterator().next()).doubleValue();
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

    @Deprecated
    public Collection<Double> getSamples(String keyword, SampleType sampleType) {
        Collection<Double> collection = null;
        if (!this.keywords.contains(keyword)) {
            return null;
        }
        Set<Double> result = new TreeSet();
        if (this.rules.hasExplicitBoundingInfo) {
            FixedDecimalSamples samples = this.rules.getDecimalSamples(keyword, sampleType);
            if (samples == null) {
                collection = Collections.unmodifiableSet(result);
            } else {
                collection = Collections.unmodifiableSet(samples.addSamples(result));
            }
            return collection;
        }
        int maxCount = isLimited(keyword, sampleType) ? AnnualTimeZoneRule.MAX_YEAR : 20;
        int i;
        switch (-getandroid-icu-text-PluralRules$SampleTypeSwitchesValues()[sampleType.ordinal()]) {
            case NodeFilter.SHOW_ELEMENT /*1*/:
                i = 0;
                while (i < 2000 && addSample(keyword, new FixedDecimal(((double) i) / 10.0d, 1), maxCount, result)) {
                    i++;
                }
                addSample(keyword, new FixedDecimal(1000000.0d, 1), maxCount, result);
                break;
            case NodeFilter.SHOW_ATTRIBUTE /*2*/:
                i = 0;
                while (i < Opcodes.OP_MUL_FLOAT_2ADDR && addSample(keyword, Integer.valueOf(i), maxCount, result)) {
                    i++;
                }
                addSample(keyword, Integer.valueOf(1000000), maxCount, result);
                break;
        }
        if (result.size() != 0) {
            collection = Collections.unmodifiableSet(result);
        }
        return collection;
    }

    @Deprecated
    public boolean addSample(String keyword, Number sample, int maxCount, Set<Double> result) {
        if ((sample instanceof FixedDecimal ? select((FixedDecimal) sample) : select(sample.doubleValue())).equals(keyword)) {
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
        return rhs instanceof PluralRules ? equals((PluralRules) rhs) : false;
    }

    public boolean equals(PluralRules rhs) {
        return rhs != null ? toString().equals(rhs.toString()) : false;
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
            HashSet<Double> subtractedSet = new HashSet(values);
            for (Double explicit : explicits) {
                subtractedSet.remove(Double.valueOf(explicit.doubleValue() - ((double) offset)));
            }
            if (subtractedSet.size() == 0) {
                return KeywordStatus.SUPPRESSED;
            }
            if (uniqueValue != null && subtractedSet.size() == 1) {
                uniqueValue.value = (Double) subtractedSet.iterator().next();
            }
            return originalSize == 1 ? KeywordStatus.UNIQUE : KeywordStatus.BOUNDED;
        } else if (originalSize != 1) {
            return KeywordStatus.BOUNDED;
        } else {
            if (uniqueValue != null) {
                uniqueValue.value = (Double) values.iterator().next();
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
