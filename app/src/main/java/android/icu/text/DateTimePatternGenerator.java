package android.icu.text;

import android.icu.impl.ICUCache;
import android.icu.impl.ICUResourceBundle;
import android.icu.impl.PatternTokenizer;
import android.icu.impl.Utility;
import android.icu.util.AnnualTimeZoneRule;
import android.icu.util.Calendar;
import android.icu.util.Freezable;
import android.icu.util.ICUCloneNotSupportedException;
import android.icu.util.ULocale;
import android.icu.util.ULocale.Category;
import android.icu.util.UResourceBundle;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import org.xmlpull.v1.XmlPullParser;

public class DateTimePatternGenerator implements Freezable<DateTimePatternGenerator>, Cloneable {
    private static final String[] CANONICAL_ITEMS = null;
    private static final Set<String> CANONICAL_SET = null;
    private static final String[] CLDR_FIELD_APPEND = null;
    private static final String[] CLDR_FIELD_NAME = null;
    private static final int DATE_MASK = 1023;
    public static final int DAY = 7;
    public static final int DAYPERIOD = 10;
    public static final int DAY_OF_WEEK_IN_MONTH = 9;
    public static final int DAY_OF_YEAR = 8;
    private static final boolean DEBUG = false;
    private static final int DELTA = 16;
    private static ICUCache<String, DateTimePatternGenerator> DTPNG_CACHE = null;
    public static final int ERA = 0;
    private static final int EXTRA_FIELD = 65536;
    private static final String[] FIELD_NAME = null;
    private static final int FRACTIONAL_MASK = 16384;
    public static final int FRACTIONAL_SECOND = 14;
    public static final int HOUR = 11;
    private static final int LONG = -259;
    public static final int MATCH_ALL_FIELDS_LENGTH = 65535;
    public static final int MATCH_HOUR_FIELD_LENGTH = 2048;
    @Deprecated
    public static final int MATCH_MINUTE_FIELD_LENGTH = 4096;
    public static final int MATCH_NO_OPTIONS = 0;
    @Deprecated
    public static final int MATCH_SECOND_FIELD_LENGTH = 8192;
    public static final int MINUTE = 12;
    private static final int MISSING_FIELD = 4096;
    public static final int MONTH = 3;
    private static final int NARROW = -257;
    private static final int NONE = 0;
    private static final int NUMERIC = 256;
    public static final int QUARTER = 2;
    public static final int SECOND = 13;
    private static final int SECOND_AND_FRACTIONAL_MASK = 24576;
    private static final int SHORT = -258;
    private static final int TIME_MASK = 64512;
    public static final int TYPE_LIMIT = 16;
    public static final int WEEKDAY = 6;
    public static final int WEEK_OF_MONTH = 5;
    public static final int WEEK_OF_YEAR = 4;
    public static final int YEAR = 1;
    public static final int ZONE = 15;
    private static final int[][] types = null;
    private transient DistanceInfo _distanceInfo;
    private String[] appendItemFormats;
    private String[] appendItemNames;
    private TreeMap<String, PatternWithSkeletonFlag> basePattern_pattern;
    private Set<String> cldrAvailableFormatKeys;
    private transient DateTimeMatcher current;
    private String dateTimeFormat;
    private String decimal;
    private char defaultHourFormatChar;
    private transient FormatParser fp;
    private volatile boolean frozen;
    private TreeMap<DateTimeMatcher, PatternWithSkeletonFlag> skeleton2pattern;

    private enum DTPGflags {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.DateTimePatternGenerator.DTPGflags.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.DateTimePatternGenerator.DTPGflags.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.icu.text.DateTimePatternGenerator.DTPGflags.<clinit>():void");
        }
    }

    private static class DateTimeMatcher implements Comparable<DateTimeMatcher> {
        private String[] baseOriginal;
        private String[] original;
        private int[] type;

        /* synthetic */ DateTimeMatcher(DateTimeMatcher dateTimeMatcher) {
            this();
        }

        private DateTimeMatcher() {
            this.type = new int[DateTimePatternGenerator.TYPE_LIMIT];
            this.original = new String[DateTimePatternGenerator.TYPE_LIMIT];
            this.baseOriginal = new String[DateTimePatternGenerator.TYPE_LIMIT];
        }

        public String origStringForField(int field) {
            return this.original[field];
        }

        public boolean fieldIsNumeric(int field) {
            return this.type[field] > 0 ? true : DateTimePatternGenerator.DEBUG;
        }

        public String toString() {
            StringBuilder result = new StringBuilder();
            for (int i = DateTimePatternGenerator.NONE; i < DateTimePatternGenerator.TYPE_LIMIT; i += DateTimePatternGenerator.YEAR) {
                if (this.original[i].length() != 0) {
                    result.append(this.original[i]);
                }
            }
            return result.toString();
        }

        public String toCanonicalString() {
            StringBuilder result = new StringBuilder();
            for (int i = DateTimePatternGenerator.NONE; i < DateTimePatternGenerator.TYPE_LIMIT; i += DateTimePatternGenerator.YEAR) {
                if (this.original[i].length() != 0) {
                    int j = DateTimePatternGenerator.NONE;
                    while (j < DateTimePatternGenerator.types.length) {
                        int[] row = DateTimePatternGenerator.types[j];
                        if (row[DateTimePatternGenerator.YEAR] == i) {
                            char originalChar = this.original[i].charAt(DateTimePatternGenerator.NONE);
                            char repeatChar = (originalChar == 'h' || originalChar == 'K') ? 'h' : (char) row[DateTimePatternGenerator.NONE];
                            result.append(Utility.repeat(String.valueOf(repeatChar), this.original[i].length()));
                        } else {
                            j += DateTimePatternGenerator.YEAR;
                        }
                    }
                }
            }
            return result.toString();
        }

        String getBasePattern() {
            StringBuilder result = new StringBuilder();
            for (int i = DateTimePatternGenerator.NONE; i < DateTimePatternGenerator.TYPE_LIMIT; i += DateTimePatternGenerator.YEAR) {
                if (this.baseOriginal[i].length() != 0) {
                    result.append(this.baseOriginal[i]);
                }
            }
            return result.toString();
        }

        DateTimeMatcher set(String pattern, FormatParser fp, boolean allowDuplicateFields) {
            for (int i = DateTimePatternGenerator.NONE; i < DateTimePatternGenerator.TYPE_LIMIT; i += DateTimePatternGenerator.YEAR) {
                this.type[i] = DateTimePatternGenerator.NONE;
                this.original[i] = XmlPullParser.NO_NAMESPACE;
                this.baseOriginal[i] = XmlPullParser.NO_NAMESPACE;
            }
            fp.set(pattern);
            for (VariableField obj : fp.getItems()) {
                if (obj instanceof VariableField) {
                    VariableField item = obj;
                    String field = item.toString();
                    if (field.charAt(DateTimePatternGenerator.NONE) != 'a') {
                        int[] row = DateTimePatternGenerator.types[item.getCanonicalIndex()];
                        int typeValue = row[DateTimePatternGenerator.YEAR];
                        if (this.original[typeValue].length() == 0) {
                            this.original[typeValue] = field;
                            char repeatChar = (char) row[DateTimePatternGenerator.NONE];
                            int repeatCount = row[DateTimePatternGenerator.MONTH];
                            if ("GEzvQ".indexOf(repeatChar) >= 0) {
                                repeatCount = DateTimePatternGenerator.YEAR;
                            }
                            this.baseOriginal[typeValue] = Utility.repeat(String.valueOf(repeatChar), repeatCount);
                            int subTypeValue = row[DateTimePatternGenerator.QUARTER];
                            if (subTypeValue > 0) {
                                subTypeValue += field.length();
                            }
                            this.type[typeValue] = subTypeValue;
                        } else if (!(allowDuplicateFields || ((this.original[typeValue].charAt(DateTimePatternGenerator.NONE) == 'r' && field.charAt(DateTimePatternGenerator.NONE) == 'U') || (this.original[typeValue].charAt(DateTimePatternGenerator.NONE) == 'U' && field.charAt(DateTimePatternGenerator.NONE) == 'r')))) {
                            throw new IllegalArgumentException("Conflicting fields:\t" + this.original[typeValue] + ", " + field + "\t in " + pattern);
                        }
                    }
                    continue;
                }
            }
            return this;
        }

        int getFieldMask() {
            int result = DateTimePatternGenerator.NONE;
            for (int i = DateTimePatternGenerator.NONE; i < this.type.length; i += DateTimePatternGenerator.YEAR) {
                if (this.type[i] != 0) {
                    result |= DateTimePatternGenerator.YEAR << i;
                }
            }
            return result;
        }

        void extractFrom(DateTimeMatcher source, int fieldMask) {
            for (int i = DateTimePatternGenerator.NONE; i < this.type.length; i += DateTimePatternGenerator.YEAR) {
                if (((DateTimePatternGenerator.YEAR << i) & fieldMask) != 0) {
                    this.type[i] = source.type[i];
                    this.original[i] = source.original[i];
                } else {
                    this.type[i] = DateTimePatternGenerator.NONE;
                    this.original[i] = XmlPullParser.NO_NAMESPACE;
                }
            }
        }

        int getDistance(DateTimeMatcher other, int includeMask, DistanceInfo distanceInfo) {
            int result = DateTimePatternGenerator.NONE;
            distanceInfo.clear();
            int i = DateTimePatternGenerator.NONE;
            while (i < this.type.length) {
                int myType = ((DateTimePatternGenerator.YEAR << i) & includeMask) == 0 ? DateTimePatternGenerator.NONE : this.type[i];
                int otherType = other.type[i];
                if (myType != otherType) {
                    if (myType == 0) {
                        result += DateTimePatternGenerator.EXTRA_FIELD;
                        distanceInfo.addExtra(i);
                    } else if (otherType == 0) {
                        result += DateTimePatternGenerator.MISSING_FIELD;
                        distanceInfo.addMissing(i);
                    } else {
                        result += Math.abs(myType - otherType);
                    }
                }
                i += DateTimePatternGenerator.YEAR;
            }
            return result;
        }

        public /* bridge */ /* synthetic */ int compareTo(Object that) {
            return compareTo((DateTimeMatcher) that);
        }

        public int compareTo(DateTimeMatcher that) {
            for (int i = DateTimePatternGenerator.NONE; i < this.original.length; i += DateTimePatternGenerator.YEAR) {
                int comp = this.original[i].compareTo(that.original[i]);
                if (comp != 0) {
                    return -comp;
                }
            }
            return DateTimePatternGenerator.NONE;
        }

        public boolean equals(Object other) {
            if (!(other instanceof DateTimeMatcher)) {
                return DateTimePatternGenerator.DEBUG;
            }
            DateTimeMatcher that = (DateTimeMatcher) other;
            for (int i = DateTimePatternGenerator.NONE; i < this.original.length; i += DateTimePatternGenerator.YEAR) {
                if (!this.original[i].equals(that.original[i])) {
                    return DateTimePatternGenerator.DEBUG;
                }
            }
            return true;
        }

        public int hashCode() {
            int result = DateTimePatternGenerator.NONE;
            for (int i = DateTimePatternGenerator.NONE; i < this.original.length; i += DateTimePatternGenerator.YEAR) {
                result ^= this.original[i].hashCode();
            }
            return result;
        }
    }

    private static class DistanceInfo {
        int extraFieldMask;
        int missingFieldMask;

        /* synthetic */ DistanceInfo(DistanceInfo distanceInfo) {
            this();
        }

        private DistanceInfo() {
        }

        void clear() {
            this.extraFieldMask = DateTimePatternGenerator.NONE;
            this.missingFieldMask = DateTimePatternGenerator.NONE;
        }

        void setTo(DistanceInfo other) {
            this.missingFieldMask = other.missingFieldMask;
            this.extraFieldMask = other.extraFieldMask;
        }

        void addMissing(int field) {
            this.missingFieldMask |= DateTimePatternGenerator.YEAR << field;
        }

        void addExtra(int field) {
            this.extraFieldMask |= DateTimePatternGenerator.YEAR << field;
        }

        public String toString() {
            return "missingFieldMask: " + DateTimePatternGenerator.showMask(this.missingFieldMask) + ", extraFieldMask: " + DateTimePatternGenerator.showMask(this.extraFieldMask);
        }
    }

    @Deprecated
    public static class FormatParser {
        private static final UnicodeSet QUOTING_CHARS = null;
        private static final UnicodeSet SYNTAX_CHARS = null;
        private List<Object> items;
        private transient PatternTokenizer tokenizer;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.DateTimePatternGenerator.FormatParser.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.DateTimePatternGenerator.FormatParser.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.icu.text.DateTimePatternGenerator.FormatParser.<clinit>():void");
        }

        @Deprecated
        public FormatParser() {
            this.tokenizer = new PatternTokenizer().setSyntaxCharacters(SYNTAX_CHARS).setExtraQuotingCharacters(QUOTING_CHARS).setUsingQuote(true);
            this.items = new ArrayList();
        }

        @Deprecated
        public final FormatParser set(String string) {
            return set(string, DateTimePatternGenerator.DEBUG);
        }

        @Deprecated
        public FormatParser set(String string, boolean strict) {
            this.items.clear();
            if (string.length() == 0) {
                return this;
            }
            this.tokenizer.setPattern(string);
            StringBuffer buffer = new StringBuffer();
            StringBuffer variable = new StringBuffer();
            while (true) {
                buffer.setLength(DateTimePatternGenerator.NONE);
                int status = this.tokenizer.next(buffer);
                if (status == 0) {
                    addVariable(variable, DateTimePatternGenerator.DEBUG);
                    return this;
                } else if (status == DateTimePatternGenerator.YEAR) {
                    if (!(variable.length() == 0 || buffer.charAt(DateTimePatternGenerator.NONE) == variable.charAt(DateTimePatternGenerator.NONE))) {
                        addVariable(variable, DateTimePatternGenerator.DEBUG);
                    }
                    variable.append(buffer);
                } else {
                    addVariable(variable, DateTimePatternGenerator.DEBUG);
                    this.items.add(buffer.toString());
                }
            }
        }

        private void addVariable(StringBuffer variable, boolean strict) {
            if (variable.length() != 0) {
                this.items.add(new VariableField(variable.toString(), strict));
                variable.setLength(DateTimePatternGenerator.NONE);
            }
        }

        @Deprecated
        public List<Object> getItems() {
            return this.items;
        }

        @Deprecated
        public String toString() {
            return toString(DateTimePatternGenerator.NONE, this.items.size());
        }

        @Deprecated
        public String toString(int start, int limit) {
            StringBuilder result = new StringBuilder();
            for (int i = start; i < limit; i += DateTimePatternGenerator.YEAR) {
                String item = this.items.get(i);
                if (item instanceof String) {
                    result.append(this.tokenizer.quoteLiteral(item));
                } else {
                    result.append(this.items.get(i).toString());
                }
            }
            return result.toString();
        }

        @Deprecated
        public boolean hasDateAndTimeFields() {
            int foundMask = DateTimePatternGenerator.NONE;
            for (Object item : this.items) {
                if (item instanceof VariableField) {
                    foundMask |= DateTimePatternGenerator.YEAR << ((VariableField) item).getType();
                }
            }
            boolean isDate = (foundMask & DateTimePatternGenerator.DATE_MASK) != 0 ? true : DateTimePatternGenerator.DEBUG;
            boolean isTime = (DateTimePatternGenerator.TIME_MASK & foundMask) != 0 ? true : DateTimePatternGenerator.DEBUG;
            if (isDate) {
                return isTime;
            }
            return DateTimePatternGenerator.DEBUG;
        }

        @Deprecated
        public Object quoteLiteral(String string) {
            return this.tokenizer.quoteLiteral(string);
        }
    }

    public static final class PatternInfo {
        public static final int BASE_CONFLICT = 1;
        public static final int CONFLICT = 2;
        public static final int OK = 0;
        public String conflictingPattern;
        public int status;

        public PatternInfo() {
        }
    }

    private static class PatternWithMatcher {
        public DateTimeMatcher matcherWithSkeleton;
        public String pattern;

        public PatternWithMatcher(String pat, DateTimeMatcher matcher) {
            this.pattern = pat;
            this.matcherWithSkeleton = matcher;
        }
    }

    private static class PatternWithSkeletonFlag {
        public String pattern;
        public boolean skeletonWasSpecified;

        public PatternWithSkeletonFlag(String pat, boolean skelSpecified) {
            this.pattern = pat;
            this.skeletonWasSpecified = skelSpecified;
        }

        public String toString() {
            return this.pattern + "," + this.skeletonWasSpecified;
        }
    }

    @Deprecated
    public static class VariableField {
        private final int canonicalIndex;
        private final String string;

        @Deprecated
        public VariableField(String string) {
            this(string, DateTimePatternGenerator.DEBUG);
        }

        @Deprecated
        public VariableField(String string, boolean strict) {
            this.canonicalIndex = DateTimePatternGenerator.getCanonicalIndex(string, strict);
            if (this.canonicalIndex < 0) {
                throw new IllegalArgumentException("Illegal datetime field:\t" + string);
            }
            this.string = string;
        }

        @Deprecated
        public int getType() {
            return DateTimePatternGenerator.types[this.canonicalIndex][DateTimePatternGenerator.YEAR];
        }

        @Deprecated
        public static String getCanonicalCode(int type) {
            try {
                return DateTimePatternGenerator.CANONICAL_ITEMS[type];
            } catch (Exception e) {
                return String.valueOf(type);
            }
        }

        @Deprecated
        public boolean isNumeric() {
            return DateTimePatternGenerator.types[this.canonicalIndex][DateTimePatternGenerator.QUARTER] > 0 ? true : DateTimePatternGenerator.DEBUG;
        }

        private int getCanonicalIndex() {
            return this.canonicalIndex;
        }

        @Deprecated
        public String toString() {
            return this.string;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.DateTimePatternGenerator.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.DateTimePatternGenerator.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.text.DateTimePatternGenerator.<clinit>():void");
    }

    private java.lang.String getBestAppending(android.icu.text.DateTimePatternGenerator.DateTimeMatcher r1, int r2, android.icu.text.DateTimePatternGenerator.DistanceInfo r3, android.icu.text.DateTimePatternGenerator.DateTimeMatcher r4, java.util.EnumSet<android.icu.text.DateTimePatternGenerator.DTPGflags> r5, int r6) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.DateTimePatternGenerator.getBestAppending(android.icu.text.DateTimePatternGenerator$DateTimeMatcher, int, android.icu.text.DateTimePatternGenerator$DistanceInfo, android.icu.text.DateTimePatternGenerator$DateTimeMatcher, java.util.EnumSet, int):java.lang.String
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 7 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.text.DateTimePatternGenerator.getBestAppending(android.icu.text.DateTimePatternGenerator$DateTimeMatcher, int, android.icu.text.DateTimePatternGenerator$DistanceInfo, android.icu.text.DateTimePatternGenerator$DateTimeMatcher, java.util.EnumSet, int):java.lang.String");
    }

    public static DateTimePatternGenerator getEmptyInstance() {
        return new DateTimePatternGenerator();
    }

    protected DateTimePatternGenerator() {
        this.skeleton2pattern = new TreeMap();
        this.basePattern_pattern = new TreeMap();
        this.decimal = "?";
        this.dateTimeFormat = "{1} {0}";
        this.appendItemFormats = new String[TYPE_LIMIT];
        this.appendItemNames = new String[TYPE_LIMIT];
        for (int i = NONE; i < TYPE_LIMIT; i += YEAR) {
            this.appendItemFormats[i] = "{0} \u251c{2}: {1}\u2524";
            this.appendItemNames[i] = "F" + i;
        }
        this.defaultHourFormatChar = 'H';
        this.frozen = DEBUG;
        this.current = new DateTimeMatcher();
        this.fp = new FormatParser();
        this._distanceInfo = new DistanceInfo();
        complete();
        this.cldrAvailableFormatKeys = new HashSet(20);
    }

    public static DateTimePatternGenerator getInstance() {
        return getInstance(ULocale.getDefault(Category.FORMAT));
    }

    public static DateTimePatternGenerator getInstance(ULocale uLocale) {
        return getFrozenInstance(uLocale).cloneAsThawed();
    }

    public static DateTimePatternGenerator getInstance(Locale locale) {
        return getInstance(ULocale.forLocale(locale));
    }

    @Deprecated
    public static DateTimePatternGenerator getFrozenInstance(ULocale uLocale) {
        String localeKey = uLocale.toString();
        DateTimePatternGenerator result = (DateTimePatternGenerator) DTPNG_CACHE.get(localeKey);
        if (result != null) {
            return result;
        }
        int i;
        ICUResourceBundle itemBundle;
        result = new DateTimePatternGenerator();
        PatternInfo returnInfo = new PatternInfo();
        String shortTimePattern = null;
        for (i = NONE; i <= MONTH; i += YEAR) {
            result.addPattern(((SimpleDateFormat) DateFormat.getDateInstance(i, uLocale)).toPattern(), DEBUG, returnInfo);
            SimpleDateFormat df = (SimpleDateFormat) DateFormat.getTimeInstance(i, uLocale);
            result.addPattern(df.toPattern(), DEBUG, returnInfo);
            if (i == MONTH) {
                shortTimePattern = df.toPattern();
                FormatParser fp = new FormatParser();
                fp.set(shortTimePattern);
                List<Object> items = fp.getItems();
                for (int idx = NONE; idx < items.size(); idx += YEAR) {
                    VariableField item = items.get(idx);
                    if (item instanceof VariableField) {
                        VariableField fld = item;
                        if (fld.getType() == HOUR) {
                            result.defaultHourFormatChar = fld.toString().charAt(NONE);
                            break;
                        }
                    }
                }
            }
        }
        ICUResourceBundle rb = (ICUResourceBundle) UResourceBundle.getBundleInstance(ICUResourceBundle.ICU_BASE_NAME, uLocale);
        String calendarTypeToUse = uLocale.getKeywordValue("calendar");
        if (calendarTypeToUse == null) {
            calendarTypeToUse = Calendar.getKeywordValuesForLocale("calendar", uLocale, true)[NONE];
        }
        if (calendarTypeToUse == null) {
            calendarTypeToUse = "gregorian";
        }
        try {
            itemBundle = rb.getWithFallback("calendar/" + calendarTypeToUse + "/appendItems");
            for (i = NONE; i < itemBundle.getSize(); i += YEAR) {
                ICUResourceBundle formatBundle = (ICUResourceBundle) itemBundle.get(i);
                String formatName = itemBundle.get(i).getKey();
                String value = formatBundle.getString();
                result.setAppendItemFormat(getAppendFormatNumber(formatName), value);
            }
        } catch (MissingResourceException e) {
        }
        try {
            itemBundle = rb.getWithFallback("fields");
            for (i = NONE; i < TYPE_LIMIT; i += YEAR) {
                if (isCLDRFieldName(i)) {
                    String str = "dn";
                    result.setAppendItemName(i, itemBundle.getWithFallback(CLDR_FIELD_NAME[i]).getWithFallback(r31).getString());
                }
            }
        } catch (MissingResourceException e2) {
        }
        ICUResourceBundle availFormatsBundle = null;
        try {
            availFormatsBundle = rb.getWithFallback("calendar/" + calendarTypeToUse + "/availableFormats");
        } catch (MissingResourceException e3) {
        }
        boolean override = true;
        while (availFormatsBundle != null) {
            for (i = NONE; i < availFormatsBundle.getSize(); i += YEAR) {
                String formatKey = availFormatsBundle.get(i).getKey();
                if (!result.isAvailableFormatSet(formatKey)) {
                    result.setAvailableFormat(formatKey);
                    result.addPatternWithSkeleton(availFormatsBundle.get(i).getString(), formatKey, override, returnInfo);
                }
            }
            ICUResourceBundle pbundle = (ICUResourceBundle) availFormatsBundle.getParent();
            if (pbundle == null) {
                break;
            }
            try {
                availFormatsBundle = pbundle.getWithFallback("calendar/" + calendarTypeToUse + "/availableFormats");
            } catch (MissingResourceException e4) {
                availFormatsBundle = null;
            }
            if (availFormatsBundle != null && pbundle.getULocale().getBaseName().equals("root")) {
                override = DEBUG;
            }
        }
        if (shortTimePattern != null) {
            hackTimes(result, returnInfo, shortTimePattern);
        }
        result.setDateTimeFormat(Calendar.getDateTimePattern(Calendar.getInstance(uLocale), uLocale, QUARTER));
        result.setDecimal(String.valueOf(new DecimalFormatSymbols(uLocale).getDecimalSeparator()));
        result.freeze();
        DTPNG_CACHE.put(localeKey, result);
        return result;
    }

    @Deprecated
    public char getDefaultHourFormatChar() {
        return this.defaultHourFormatChar;
    }

    @Deprecated
    public void setDefaultHourFormatChar(char defaultHourFormatChar) {
        this.defaultHourFormatChar = defaultHourFormatChar;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static void hackTimes(DateTimePatternGenerator result, PatternInfo returnInfo, String hackPattern) {
        int i;
        result.fp.set(hackPattern);
        StringBuilder mmss = new StringBuilder();
        boolean gotMm = DEBUG;
        for (i = NONE; i < result.fp.items.size(); i += YEAR) {
            char ch;
            Object item = result.fp.items.get(i);
            if (!(item instanceof String)) {
                ch = item.toString().charAt(NONE);
                if (ch == 'm') {
                    gotMm = true;
                    mmss.append(item);
                } else if (ch == 's') {
                    if (gotMm) {
                        mmss.append(item);
                        result.addPattern(mmss.toString(), DEBUG, returnInfo);
                    }
                } else if (!(gotMm || ch == 'z' || ch == 'Z' || ch == 'v')) {
                    if (ch == 'V') {
                        break;
                    }
                }
            } else if (gotMm) {
                mmss.append(result.fp.quoteLiteral(item.toString()));
            }
        }
        BitSet variables = new BitSet();
        BitSet nuke = new BitSet();
        for (i = NONE; i < result.fp.items.size(); i += YEAR) {
            item = result.fp.items.get(i);
            if (item instanceof VariableField) {
                variables.set(i);
                ch = item.toString().charAt(NONE);
                if (ch == 's' || ch == 'S') {
                    nuke.set(i);
                    int j = i - 1;
                    while (j >= 0 && !variables.get(j)) {
                        nuke.set(i);
                        j += YEAR;
                    }
                }
            }
        }
        result.addPattern(getFilteredPattern(result.fp, nuke), DEBUG, returnInfo);
    }

    private static String getFilteredPattern(FormatParser fp, BitSet nuke) {
        StringBuilder result = new StringBuilder();
        for (int i = NONE; i < fp.items.size(); i += YEAR) {
            if (!nuke.get(i)) {
                Object item = fp.items.get(i);
                if (item instanceof String) {
                    result.append(fp.quoteLiteral(item.toString()));
                } else {
                    result.append(item.toString());
                }
            }
        }
        return result.toString();
    }

    @Deprecated
    public static int getAppendFormatNumber(String string) {
        for (int i = NONE; i < CLDR_FIELD_APPEND.length; i += YEAR) {
            if (CLDR_FIELD_APPEND[i].equals(string)) {
                return i;
            }
        }
        return -1;
    }

    private static boolean isCLDRFieldName(int index) {
        if ((index >= 0 || index < TYPE_LIMIT) && CLDR_FIELD_NAME[index].charAt(NONE) != '*') {
            return true;
        }
        return DEBUG;
    }

    public String getBestPattern(String skeleton) {
        return getBestPattern(skeleton, null, NONE);
    }

    public String getBestPattern(String skeleton, int options) {
        return getBestPattern(skeleton, null, options);
    }

    private String getBestPattern(String skeleton, DateTimeMatcher skipMatcher, int options) {
        EnumSet<DTPGflags> flags = EnumSet.noneOf(DTPGflags.class);
        StringBuilder skeletonCopy = new StringBuilder(skeleton);
        boolean inQuoted = DEBUG;
        for (int patPos = NONE; patPos < skeletonCopy.length(); patPos += YEAR) {
            char patChr = skeletonCopy.charAt(patPos);
            if (patChr == PatternTokenizer.SINGLE_QUOTE) {
                inQuoted = inQuoted ? DEBUG : true;
            } else if (!inQuoted) {
                if (patChr == 'j') {
                    skeletonCopy.setCharAt(patPos, this.defaultHourFormatChar);
                } else if (patChr == 'J') {
                    skeletonCopy.setCharAt(patPos, 'H');
                    flags.add(DTPGflags.SKELETON_USES_CAP_J);
                }
            }
        }
        synchronized (this) {
            this.current.set(skeletonCopy.toString(), this.fp, DEBUG);
            PatternWithMatcher bestWithMatcher = getBestRaw(this.current, -1, this._distanceInfo, skipMatcher);
            if (this._distanceInfo.missingFieldMask == 0 && this._distanceInfo.extraFieldMask == 0) {
                String adjustFieldTypes = adjustFieldTypes(bestWithMatcher, this.current, flags, options);
                return adjustFieldTypes;
            }
            int neededFields = this.current.getFieldMask();
            String datePattern = getBestAppending(this.current, neededFields & DATE_MASK, this._distanceInfo, skipMatcher, flags, options);
            String timePattern = getBestAppending(this.current, neededFields & TIME_MASK, this._distanceInfo, skipMatcher, flags, options);
            if (datePattern == null) {
                if (timePattern == null) {
                    timePattern = XmlPullParser.NO_NAMESPACE;
                }
                return timePattern;
            } else if (timePattern == null) {
                return datePattern;
            } else {
                adjustFieldTypes = getDateTimeFormat();
                Object[] objArr = new Object[QUARTER];
                objArr[NONE] = timePattern;
                objArr[YEAR] = datePattern;
                return MessageFormat.format(adjustFieldTypes, objArr);
            }
        }
    }

    public DateTimePatternGenerator addPattern(String pattern, boolean override, PatternInfo returnInfo) {
        return addPatternWithSkeleton(pattern, null, override, returnInfo);
    }

    @Deprecated
    public DateTimePatternGenerator addPatternWithSkeleton(String pattern, String skeletonToUse, boolean override, PatternInfo returnInfo) {
        DateTimeMatcher matcher;
        boolean z = true;
        checkFrozen();
        if (skeletonToUse == null) {
            matcher = new DateTimeMatcher().set(pattern, this.fp, DEBUG);
        } else {
            matcher = new DateTimeMatcher().set(skeletonToUse, this.fp, DEBUG);
        }
        String basePattern = matcher.getBasePattern();
        PatternWithSkeletonFlag previousPatternWithSameBase = (PatternWithSkeletonFlag) this.basePattern_pattern.get(basePattern);
        if (!(previousPatternWithSameBase == null || (previousPatternWithSameBase.skeletonWasSpecified && (skeletonToUse == null || override)))) {
            returnInfo.status = YEAR;
            returnInfo.conflictingPattern = previousPatternWithSameBase.pattern;
            if (!override) {
                return this;
            }
        }
        PatternWithSkeletonFlag previousValue = (PatternWithSkeletonFlag) this.skeleton2pattern.get(matcher);
        if (previousValue != null) {
            returnInfo.status = QUARTER;
            returnInfo.conflictingPattern = previousValue.pattern;
            if (!override || (skeletonToUse != null && previousValue.skeletonWasSpecified)) {
                return this;
            }
        }
        returnInfo.status = NONE;
        returnInfo.conflictingPattern = XmlPullParser.NO_NAMESPACE;
        if (skeletonToUse == null) {
            z = DEBUG;
        }
        PatternWithSkeletonFlag patWithSkelFlag = new PatternWithSkeletonFlag(pattern, z);
        this.skeleton2pattern.put(matcher, patWithSkelFlag);
        this.basePattern_pattern.put(basePattern, patWithSkelFlag);
        return this;
    }

    public String getSkeleton(String pattern) {
        String dateTimeMatcher;
        synchronized (this) {
            this.current.set(pattern, this.fp, DEBUG);
            dateTimeMatcher = this.current.toString();
        }
        return dateTimeMatcher;
    }

    @Deprecated
    public String getSkeletonAllowingDuplicates(String pattern) {
        String dateTimeMatcher;
        synchronized (this) {
            this.current.set(pattern, this.fp, true);
            dateTimeMatcher = this.current.toString();
        }
        return dateTimeMatcher;
    }

    @Deprecated
    public String getCanonicalSkeletonAllowingDuplicates(String pattern) {
        String toCanonicalString;
        synchronized (this) {
            this.current.set(pattern, this.fp, true);
            toCanonicalString = this.current.toCanonicalString();
        }
        return toCanonicalString;
    }

    public String getBaseSkeleton(String pattern) {
        String basePattern;
        synchronized (this) {
            this.current.set(pattern, this.fp, DEBUG);
            basePattern = this.current.getBasePattern();
        }
        return basePattern;
    }

    public Map<String, String> getSkeletons(Map<String, String> result) {
        if (result == null) {
            result = new LinkedHashMap();
        }
        for (DateTimeMatcher item : this.skeleton2pattern.keySet()) {
            String pattern = ((PatternWithSkeletonFlag) this.skeleton2pattern.get(item)).pattern;
            if (!CANONICAL_SET.contains(pattern)) {
                result.put(item.toString(), pattern);
            }
        }
        return result;
    }

    public Set<String> getBaseSkeletons(Set<String> result) {
        if (result == null) {
            result = new HashSet();
        }
        result.addAll(this.basePattern_pattern.keySet());
        return result;
    }

    public String replaceFieldTypes(String pattern, String skeleton) {
        return replaceFieldTypes(pattern, skeleton, NONE);
    }

    public String replaceFieldTypes(String pattern, String skeleton, int options) {
        String adjustFieldTypes;
        synchronized (this) {
            adjustFieldTypes = adjustFieldTypes(new PatternWithMatcher(pattern, null), this.current.set(skeleton, this.fp, DEBUG), EnumSet.noneOf(DTPGflags.class), options);
        }
        return adjustFieldTypes;
    }

    public void setDateTimeFormat(String dateTimeFormat) {
        checkFrozen();
        this.dateTimeFormat = dateTimeFormat;
    }

    public String getDateTimeFormat() {
        return this.dateTimeFormat;
    }

    public void setDecimal(String decimal) {
        checkFrozen();
        this.decimal = decimal;
    }

    public String getDecimal() {
        return this.decimal;
    }

    @Deprecated
    public Collection<String> getRedundants(Collection<String> output) {
        synchronized (this) {
            if (output == null) {
                output = new LinkedHashSet();
            }
            for (DateTimeMatcher cur : this.skeleton2pattern.keySet()) {
                String pattern = ((PatternWithSkeletonFlag) this.skeleton2pattern.get(cur)).pattern;
                if (!CANONICAL_SET.contains(pattern) && getBestPattern(cur.toString(), cur, NONE).equals(pattern)) {
                    output.add(pattern);
                }
            }
        }
        return output;
    }

    public void setAppendItemFormat(int field, String value) {
        checkFrozen();
        this.appendItemFormats[field] = value;
    }

    public String getAppendItemFormat(int field) {
        return this.appendItemFormats[field];
    }

    public void setAppendItemName(int field, String value) {
        checkFrozen();
        this.appendItemNames[field] = value;
    }

    public String getAppendItemName(int field) {
        return this.appendItemNames[field];
    }

    @Deprecated
    public static boolean isSingleField(String skeleton) {
        char first = skeleton.charAt(NONE);
        for (int i = YEAR; i < skeleton.length(); i += YEAR) {
            if (skeleton.charAt(i) != first) {
                return DEBUG;
            }
        }
        return true;
    }

    private void setAvailableFormat(String key) {
        checkFrozen();
        this.cldrAvailableFormatKeys.add(key);
    }

    private boolean isAvailableFormatSet(String key) {
        return this.cldrAvailableFormatKeys.contains(key);
    }

    public boolean isFrozen() {
        return this.frozen;
    }

    public /* bridge */ /* synthetic */ Object m18freeze() {
        return freeze();
    }

    public DateTimePatternGenerator freeze() {
        this.frozen = true;
        return this;
    }

    public /* bridge */ /* synthetic */ Object m17cloneAsThawed() {
        return cloneAsThawed();
    }

    public DateTimePatternGenerator cloneAsThawed() {
        DateTimePatternGenerator result = (DateTimePatternGenerator) clone();
        this.frozen = DEBUG;
        return result;
    }

    public Object clone() {
        try {
            DateTimePatternGenerator result = (DateTimePatternGenerator) super.clone();
            result.skeleton2pattern = (TreeMap) this.skeleton2pattern.clone();
            result.basePattern_pattern = (TreeMap) this.basePattern_pattern.clone();
            result.appendItemFormats = (String[]) this.appendItemFormats.clone();
            result.appendItemNames = (String[]) this.appendItemNames.clone();
            result.current = new DateTimeMatcher();
            result.fp = new FormatParser();
            result._distanceInfo = new DistanceInfo();
            result.frozen = DEBUG;
            return result;
        } catch (CloneNotSupportedException e) {
            throw new ICUCloneNotSupportedException("Internal Error", e);
        }
    }

    @Deprecated
    public boolean skeletonsAreSimilar(String id, String skeleton) {
        if (id.equals(skeleton)) {
            return true;
        }
        TreeSet<String> parser1 = getSet(id);
        TreeSet<String> parser2 = getSet(skeleton);
        if (parser1.size() != parser2.size()) {
            return DEBUG;
        }
        Iterator<String> it2 = parser2.iterator();
        for (String item : parser1) {
            if (types[getCanonicalIndex(item, DEBUG)][YEAR] != types[getCanonicalIndex((String) it2.next(), DEBUG)][YEAR]) {
                return DEBUG;
            }
        }
        return true;
    }

    private TreeSet<String> getSet(String id) {
        List<Object> items = this.fp.set(id).getItems();
        TreeSet<String> result = new TreeSet();
        for (Object obj : items) {
            String item = obj.toString();
            if (!(item.startsWith("G") || item.startsWith("a"))) {
                result.add(item);
            }
        }
        return result;
    }

    private void checkFrozen() {
        if (isFrozen()) {
            throw new UnsupportedOperationException("Attempt to modify frozen object");
        }
    }

    private String getAppendName(int foundMask) {
        return "'" + this.appendItemNames[foundMask] + "'";
    }

    private String getAppendFormat(int foundMask) {
        return this.appendItemFormats[foundMask];
    }

    private int getTopBitNumber(int foundMask) {
        int i = NONE;
        while (foundMask != 0) {
            foundMask >>>= YEAR;
            i += YEAR;
        }
        return i - 1;
    }

    private void complete() {
        PatternInfo patternInfo = new PatternInfo();
        for (int i = NONE; i < CANONICAL_ITEMS.length; i += YEAR) {
            addPattern(String.valueOf(CANONICAL_ITEMS[i]), DEBUG, patternInfo);
        }
    }

    private PatternWithMatcher getBestRaw(DateTimeMatcher source, int includeMask, DistanceInfo missingFields, DateTimeMatcher skipMatcher) {
        int bestDistance = AnnualTimeZoneRule.MAX_YEAR;
        PatternWithMatcher bestPatternWithMatcher = new PatternWithMatcher(XmlPullParser.NO_NAMESPACE, null);
        DistanceInfo tempInfo = new DistanceInfo();
        for (DateTimeMatcher trial : this.skeleton2pattern.keySet()) {
            if (!trial.equals(skipMatcher)) {
                int distance = source.getDistance(trial, includeMask, tempInfo);
                if (distance < bestDistance) {
                    bestDistance = distance;
                    PatternWithSkeletonFlag patternWithSkelFlag = (PatternWithSkeletonFlag) this.skeleton2pattern.get(trial);
                    bestPatternWithMatcher.pattern = patternWithSkelFlag.pattern;
                    if (patternWithSkelFlag.skeletonWasSpecified) {
                        bestPatternWithMatcher.matcherWithSkeleton = trial;
                    } else {
                        bestPatternWithMatcher.matcherWithSkeleton = null;
                    }
                    missingFields.setTo(tempInfo);
                    if (distance == 0) {
                        break;
                    }
                } else {
                    continue;
                }
            }
        }
        return bestPatternWithMatcher;
    }

    private String adjustFieldTypes(PatternWithMatcher patternWithMatcher, DateTimeMatcher inputRequest, EnumSet<DTPGflags> flags, int options) {
        this.fp.set(patternWithMatcher.pattern);
        StringBuilder newPattern = new StringBuilder();
        for (VariableField item : this.fp.getItems()) {
            if (item instanceof String) {
                FormatParser formatParser = this.fp;
                newPattern.append(r0.quoteLiteral((String) item));
            } else {
                VariableField variableField = item;
                StringBuilder fieldBuilder = new StringBuilder(variableField.toString());
                int type = variableField.getType();
                if (flags.contains(DTPGflags.FIX_FRACTIONAL_SECONDS) && type == SECOND) {
                    String newField = inputRequest.original[FRACTIONAL_SECOND];
                    fieldBuilder.append(this.decimal);
                    fieldBuilder.append(newField);
                } else if (inputRequest.type[type] != 0) {
                    String reqField = inputRequest.original[type];
                    int reqFieldLen = reqField.length();
                    if (reqField.charAt(NONE) == 'E' && reqFieldLen < MONTH) {
                        reqFieldLen = MONTH;
                    }
                    int adjFieldLen = reqFieldLen;
                    DateTimeMatcher matcherWithSkeleton = patternWithMatcher.matcherWithSkeleton;
                    if ((type == HOUR && (options & MATCH_HOUR_FIELD_LENGTH) == 0) || ((type == MINUTE && (options & MISSING_FIELD) == 0) || (type == SECOND && (options & MATCH_SECOND_FIELD_LENGTH) == 0))) {
                        adjFieldLen = fieldBuilder.length();
                    } else if (matcherWithSkeleton != null) {
                        int skelFieldLen = matcherWithSkeleton.origStringForField(type).length();
                        boolean patFieldIsNumeric = variableField.isNumeric();
                        boolean skelFieldIsNumeric = matcherWithSkeleton.fieldIsNumeric(type);
                        if (skelFieldLen == reqFieldLen || ((patFieldIsNumeric && !skelFieldIsNumeric) || (skelFieldIsNumeric && !patFieldIsNumeric))) {
                            adjFieldLen = fieldBuilder.length();
                        }
                    }
                    char c = (type == HOUR || type == MONTH || type == WEEKDAY || (type == YEAR && reqField.charAt(NONE) != 'Y')) ? fieldBuilder.charAt(NONE) : reqField.charAt(NONE);
                    if (type == HOUR) {
                        if (flags.contains(DTPGflags.SKELETON_USES_CAP_J)) {
                            c = this.defaultHourFormatChar;
                        }
                    }
                    fieldBuilder = new StringBuilder();
                    for (int i = adjFieldLen; i > 0; i--) {
                        fieldBuilder.append(c);
                    }
                }
                newPattern.append(fieldBuilder);
            }
        }
        return newPattern.toString();
    }

    @Deprecated
    public String getFields(String pattern) {
        this.fp.set(pattern);
        StringBuilder newPattern = new StringBuilder();
        for (Object item : this.fp.getItems()) {
            if (item instanceof String) {
                newPattern.append(this.fp.quoteLiteral((String) item));
            } else {
                newPattern.append("{").append(getName(item.toString())).append("}");
            }
        }
        return newPattern.toString();
    }

    private static String showMask(int mask) {
        StringBuilder result = new StringBuilder();
        for (int i = NONE; i < TYPE_LIMIT; i += YEAR) {
            if (((YEAR << i) & mask) != 0) {
                if (result.length() != 0) {
                    result.append(" | ");
                }
                result.append(FIELD_NAME[i]);
                result.append(" ");
            }
        }
        return result.toString();
    }

    private static String getName(String s) {
        boolean string = true;
        int i = getCanonicalIndex(s, true);
        String name = FIELD_NAME[types[i][YEAR]];
        int subtype = types[i][QUARTER];
        if (subtype >= 0) {
            string = DEBUG;
        }
        if (string) {
            subtype = -subtype;
        }
        if (subtype < 0) {
            return name + ":S";
        }
        return name + ":N";
    }

    private static int getCanonicalIndex(String s, boolean strict) {
        int len = s.length();
        if (len == 0) {
            return -1;
        }
        int i;
        char ch = s.charAt(NONE);
        for (i = YEAR; i < len; i += YEAR) {
            if (s.charAt(i) != ch) {
                return -1;
            }
        }
        int bestRow = -1;
        for (i = NONE; i < types.length; i += YEAR) {
            int[] row = types[i];
            if (row[NONE] == ch) {
                bestRow = i;
                if (row[MONTH] <= len && row[row.length - 1] >= len) {
                    return i;
                }
            }
        }
        if (strict) {
            bestRow = -1;
        }
        return bestRow;
    }
}
