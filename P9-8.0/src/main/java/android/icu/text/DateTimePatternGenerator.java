package android.icu.text;

import android.icu.impl.ICUCache;
import android.icu.impl.ICUData;
import android.icu.impl.ICUResourceBundle;
import android.icu.impl.PatternTokenizer;
import android.icu.impl.SimpleCache;
import android.icu.impl.SimpleFormatterImpl;
import android.icu.impl.UResource.Key;
import android.icu.impl.UResource.Sink;
import android.icu.impl.UResource.Table;
import android.icu.impl.UResource.Value;
import android.icu.impl.locale.BaseLocale;
import android.icu.lang.UCharacter.UnicodeBlock;
import android.icu.util.Calendar;
import android.icu.util.Freezable;
import android.icu.util.ICUCloneNotSupportedException;
import android.icu.util.ULocale;
import android.icu.util.ULocale.Category;
import android.icu.util.UResourceBundle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
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

public class DateTimePatternGenerator implements Freezable<DateTimePatternGenerator>, Cloneable {
    private static final String[] CANONICAL_ITEMS = new String[]{"G", DateFormat.YEAR, "Q", DateFormat.NUM_MONTH, "w", "W", DateFormat.ABBR_WEEKDAY, DateFormat.DAY, "D", "F", DateFormat.HOUR24, DateFormat.MINUTE, DateFormat.SECOND, "S", DateFormat.ABBR_GENERIC_TZ};
    private static final Set<String> CANONICAL_SET = new HashSet(Arrays.asList(CANONICAL_ITEMS));
    private static final String[] CLDR_FIELD_APPEND = new String[]{"Era", "Year", "Quarter", "Month", "Week", "*", "Day-Of-Week", "Day", "*", "*", "*", "Hour", "Minute", "Second", "*", "Timezone"};
    private static final String[] CLDR_FIELD_NAME = new String[]{"era", "year", "*", "month", "week", "*", "weekday", "day", "*", "*", "dayperiod", "hour", "minute", "second", "*", "zone"};
    private static final int DATE_MASK = 1023;
    public static final int DAY = 7;
    public static final int DAYPERIOD = 10;
    public static final int DAY_OF_WEEK_IN_MONTH = 9;
    public static final int DAY_OF_YEAR = 8;
    private static final boolean DEBUG = false;
    private static final int DELTA = 16;
    private static ICUCache<String, DateTimePatternGenerator> DTPNG_CACHE = new SimpleCache();
    public static final int ERA = 0;
    private static final int EXTRA_FIELD = 65536;
    private static final String[] FIELD_NAME = new String[]{"Era", "Year", "Quarter", "Month", "Week_in_Year", "Week_in_Month", "Weekday", "Day", "Day_Of_Year", "Day_of_Week_in_Month", "Dayperiod", "Hour", "Minute", "Second", "Fractional_Second", "Zone"};
    private static final int FRACTIONAL_MASK = 16384;
    public static final int FRACTIONAL_SECOND = 14;
    public static final int HOUR = 11;
    private static final String[] LAST_RESORT_ALLOWED_HOUR_FORMAT = new String[]{DateFormat.HOUR24};
    static final Map<String, String[]> LOCALE_TO_ALLOWED_HOUR;
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
    @Deprecated
    public static final int TYPE_LIMIT = 16;
    public static final int WEEKDAY = 6;
    public static final int WEEK_OF_MONTH = 5;
    public static final int WEEK_OF_YEAR = 4;
    public static final int YEAR = 1;
    public static final int ZONE = 15;
    private static final int[][] types = new int[][]{new int[]{71, 0, SHORT, 1, 3}, new int[]{71, 0, LONG, 4}, new int[]{121, 1, 256, 1, 20}, new int[]{89, 1, UnicodeBlock.TANGUT_ID, 1, 20}, new int[]{117, 1, 288, 1, 20}, new int[]{114, 1, 304, 1, 20}, new int[]{85, 1, SHORT, 1, 3}, new int[]{85, 1, LONG, 4}, new int[]{85, 1, NARROW, 5}, new int[]{81, 2, 256, 1, 2}, new int[]{81, 2, SHORT, 3}, new int[]{81, 2, LONG, 4}, new int[]{113, 2, UnicodeBlock.TANGUT_ID, 1, 2}, new int[]{113, 2, -242, 3}, new int[]{113, 2, -243, 4}, new int[]{77, 3, 256, 1, 2}, new int[]{77, 3, SHORT, 3}, new int[]{77, 3, LONG, 4}, new int[]{77, 3, NARROW, 5}, new int[]{76, 3, UnicodeBlock.TANGUT_ID, 1, 2}, new int[]{76, 3, -274, 3}, new int[]{76, 3, -275, 4}, new int[]{76, 3, -273, 5}, new int[]{108, 3, UnicodeBlock.TANGUT_ID, 1, 1}, new int[]{119, 4, 256, 1, 2}, new int[]{87, 5, UnicodeBlock.TANGUT_ID, 1}, new int[]{69, 6, SHORT, 1, 3}, new int[]{69, 6, LONG, 4}, new int[]{69, 6, NARROW, 5}, new int[]{99, 6, 288, 1, 2}, new int[]{99, 6, -290, 3}, new int[]{99, 6, -291, 4}, new int[]{99, 6, -289, 5}, new int[]{101, 6, UnicodeBlock.TANGUT_ID, 1, 2}, new int[]{101, 6, -274, 3}, new int[]{101, 6, -275, 4}, new int[]{101, 6, -273, 5}, new int[]{100, 7, 256, 1, 2}, new int[]{68, 8, UnicodeBlock.TANGUT_ID, 1, 3}, new int[]{70, 9, 288, 1}, new int[]{103, 7, 304, 1, 20}, new int[]{97, 10, SHORT, 1}, new int[]{72, 11, 416, 1, 2}, new int[]{107, 11, 432, 1, 2}, new int[]{104, 11, 256, 1, 2}, new int[]{75, 11, UnicodeBlock.TANGUT_ID, 1, 2}, new int[]{109, 12, 256, 1, 2}, new int[]{115, 13, 256, 1, 2}, new int[]{83, 14, UnicodeBlock.TANGUT_ID, 1, 1000}, new int[]{65, 13, 288, 1, 1000}, new int[]{118, 15, -290, 1}, new int[]{118, 15, -291, 4}, new int[]{122, 15, SHORT, 1, 3}, new int[]{122, 15, LONG, 4}, new int[]{90, 15, -273, 1, 3}, new int[]{90, 15, -275, 4}, new int[]{90, 15, -274, 5}, new int[]{79, 15, -274, 1}, new int[]{79, 15, -275, 4}, new int[]{86, 15, -274, 1}, new int[]{86, 15, -275, 2}, new int[]{88, 15, -273, 1}, new int[]{88, 15, -274, 2}, new int[]{88, 15, -275, 4}, new int[]{120, 15, -273, 1}, new int[]{120, 15, -274, 2}, new int[]{120, 15, -275, 4}};
    private transient DistanceInfo _distanceInfo = new DistanceInfo();
    private String[] allowedHourFormats;
    private String[] appendItemFormats = new String[16];
    private String[] appendItemNames = new String[16];
    private TreeMap<String, PatternWithSkeletonFlag> basePattern_pattern = new TreeMap();
    private Set<String> cldrAvailableFormatKeys = new HashSet(20);
    private transient DateTimeMatcher current = new DateTimeMatcher();
    private String dateTimeFormat = "{1} {0}";
    private String decimal = "?";
    private char defaultHourFormatChar = 'H';
    private transient FormatParser fp = new FormatParser();
    private volatile boolean frozen = false;
    private TreeMap<DateTimeMatcher, PatternWithSkeletonFlag> skeleton2pattern = new TreeMap();

    private class AppendItemFormatsSink extends Sink {
        static final /* synthetic */ boolean -assertionsDisabled = (AppendItemFormatsSink.class.desiredAssertionStatus() ^ 1);
        final /* synthetic */ boolean $assertionsDisabled;

        /* synthetic */ AppendItemFormatsSink(DateTimePatternGenerator this$0, AppendItemFormatsSink -this1) {
            this();
        }

        private AppendItemFormatsSink() {
        }

        public void put(Key key, Value value, boolean noFallback) {
            Table itemsTable = value.getTable();
            int i = 0;
            while (itemsTable.getKeyAndValue(i, key, value)) {
                int field = DateTimePatternGenerator.getAppendFormatNumber(key);
                if (-assertionsDisabled || field != -1) {
                    if (DateTimePatternGenerator.this.getAppendItemFormat(field) == null) {
                        DateTimePatternGenerator.this.setAppendItemFormat(field, value.toString());
                    }
                    i++;
                } else {
                    throw new AssertionError();
                }
            }
        }
    }

    private class AppendItemNamesSink extends Sink {
        /* synthetic */ AppendItemNamesSink(DateTimePatternGenerator this$0, AppendItemNamesSink -this1) {
            this();
        }

        private AppendItemNamesSink() {
        }

        public void put(Key key, Value value, boolean noFallback) {
            Table itemsTable = value.getTable();
            for (int i = 0; itemsTable.getKeyAndValue(i, key, value); i++) {
                int field = DateTimePatternGenerator.getCLDRFieldNumber(key);
                if (field != -1) {
                    Table detailsTable = value.getTable();
                    int j = 0;
                    while (detailsTable.getKeyAndValue(j, key, value)) {
                        if (!key.contentEquals("dn")) {
                            j++;
                        } else if (DateTimePatternGenerator.this.getAppendItemName(field) == null) {
                            DateTimePatternGenerator.this.setAppendItemName(field, value.toString());
                        }
                    }
                }
            }
        }
    }

    private class AvailableFormatsSink extends Sink {
        PatternInfo returnInfo;

        public AvailableFormatsSink(PatternInfo returnInfo) {
            this.returnInfo = returnInfo;
        }

        public void put(Key key, Value value, boolean isRoot) {
            Table formatsTable = value.getTable();
            for (int i = 0; formatsTable.getKeyAndValue(i, key, value); i++) {
                String formatKey = key.toString();
                if (!DateTimePatternGenerator.this.isAvailableFormatSet(formatKey)) {
                    DateTimePatternGenerator.this.setAvailableFormat(formatKey);
                    DateTimePatternGenerator.this.addPatternWithSkeleton(value.toString(), formatKey, isRoot ^ 1, this.returnInfo);
                }
            }
        }
    }

    private enum DTPGflags {
        FIX_FRACTIONAL_SECONDS,
        SKELETON_USES_CAP_J,
        SKELETON_USES_b,
        SKELETON_USES_B;

        public static DTPGflags getFlag(String preferred) {
            switch (preferred.charAt(preferred.length() - 1)) {
                case 'B':
                    return SKELETON_USES_B;
                case 'b':
                    return SKELETON_USES_b;
                default:
                    return null;
            }
        }
    }

    private static class DateTimeMatcher implements Comparable<DateTimeMatcher> {
        private SkeletonFields baseOriginal;
        private SkeletonFields original;
        private int[] type;

        /* synthetic */ DateTimeMatcher(DateTimeMatcher -this0) {
            this();
        }

        private DateTimeMatcher() {
            this.type = new int[16];
            this.original = new SkeletonFields();
            this.baseOriginal = new SkeletonFields();
        }

        public boolean fieldIsNumeric(int field) {
            return this.type[field] > 0;
        }

        public String toString() {
            return this.original.toString();
        }

        public String toCanonicalString() {
            return this.original.toCanonicalString();
        }

        String getBasePattern() {
            return this.baseOriginal.toString();
        }

        DateTimeMatcher set(String pattern, FormatParser fp, boolean allowDuplicateFields) {
            Arrays.fill(this.type, 0);
            this.original.clear();
            this.baseOriginal.clear();
            fp.set(pattern);
            for (VariableField obj : fp.getItems()) {
                if (obj instanceof VariableField) {
                    VariableField item = obj;
                    String value = item.toString();
                    if (value.charAt(0) != 'a') {
                        int[] row = DateTimePatternGenerator.types[item.getCanonicalIndex()];
                        int field = row[1];
                        if (this.original.isFieldEmpty(field)) {
                            this.original.populate(field, value);
                            char repeatChar = (char) row[0];
                            int repeatCount = row[3];
                            if ("GEzvQ".indexOf(repeatChar) >= 0) {
                                repeatCount = 1;
                            }
                            this.baseOriginal.populate(field, repeatChar, repeatCount);
                            int subField = row[2];
                            if (subField > 0) {
                                subField += value.length();
                            }
                            this.type[field] = subField;
                        } else {
                            char ch1 = this.original.getFieldChar(field);
                            char ch2 = value.charAt(0);
                            if (!(allowDuplicateFields || ((ch1 == 'r' && ch2 == 'U') || (ch1 == 'U' && ch2 == 'r')))) {
                                throw new IllegalArgumentException("Conflicting fields:\t" + ch1 + ", " + value + "\t in " + pattern);
                            }
                        }
                    }
                    continue;
                }
            }
            return this;
        }

        int getFieldMask() {
            int result = 0;
            for (int i = 0; i < this.type.length; i++) {
                if (this.type[i] != 0) {
                    result |= 1 << i;
                }
            }
            return result;
        }

        void extractFrom(DateTimeMatcher source, int fieldMask) {
            for (int i = 0; i < this.type.length; i++) {
                if (((1 << i) & fieldMask) != 0) {
                    this.type[i] = source.type[i];
                    this.original.copyFieldFrom(source.original, i);
                } else {
                    this.type[i] = 0;
                    this.original.clearField(i);
                }
            }
        }

        int getDistance(DateTimeMatcher other, int includeMask, DistanceInfo distanceInfo) {
            int result = 0;
            distanceInfo.clear();
            int i = 0;
            while (i < 16) {
                int myType = ((1 << i) & includeMask) == 0 ? 0 : this.type[i];
                int otherType = other.type[i];
                if (myType != otherType) {
                    if (myType == 0) {
                        result += 65536;
                        distanceInfo.addExtra(i);
                    } else if (otherType == 0) {
                        result += 4096;
                        distanceInfo.addMissing(i);
                    } else {
                        result += Math.abs(myType - otherType);
                    }
                }
                i++;
            }
            return result;
        }

        public int compareTo(DateTimeMatcher that) {
            int result = this.original.compareTo(that.original);
            if (result > 0) {
                return -1;
            }
            if (result < 0) {
                return 1;
            }
            return 0;
        }

        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (other == null || !(other instanceof DateTimeMatcher)) {
                return false;
            }
            return this.original.equals(((DateTimeMatcher) other).original);
        }

        public int hashCode() {
            return this.original.hashCode();
        }
    }

    private static class DayPeriodAllowedHoursSink extends Sink {
        HashMap<String, String[]> tempMap;

        /* synthetic */ DayPeriodAllowedHoursSink(HashMap tempMap, DayPeriodAllowedHoursSink -this1) {
            this(tempMap);
        }

        private DayPeriodAllowedHoursSink(HashMap<String, String[]> tempMap) {
            this.tempMap = tempMap;
        }

        public void put(Key key, Value value, boolean noFallback) {
            Table timeData = value.getTable();
            for (int i = 0; timeData.getKeyAndValue(i, key, value); i++) {
                String regionOrLocale = key.toString();
                Table formatList = value.getTable();
                for (int j = 0; formatList.getKeyAndValue(j, key, value); j++) {
                    if (key.contentEquals("allowed")) {
                        this.tempMap.put(regionOrLocale, value.getStringArrayOrStringAsArray());
                    }
                }
            }
        }
    }

    private static class DistanceInfo {
        int extraFieldMask;
        int missingFieldMask;

        /* synthetic */ DistanceInfo(DistanceInfo -this0) {
            this();
        }

        private DistanceInfo() {
        }

        void clear() {
            this.extraFieldMask = 0;
            this.missingFieldMask = 0;
        }

        void setTo(DistanceInfo other) {
            this.missingFieldMask = other.missingFieldMask;
            this.extraFieldMask = other.extraFieldMask;
        }

        void addMissing(int field) {
            this.missingFieldMask |= 1 << field;
        }

        void addExtra(int field) {
            this.extraFieldMask |= 1 << field;
        }

        public String toString() {
            return "missingFieldMask: " + DateTimePatternGenerator.showMask(this.missingFieldMask) + ", extraFieldMask: " + DateTimePatternGenerator.showMask(this.extraFieldMask);
        }
    }

    @Deprecated
    public static class FormatParser {
        private static final UnicodeSet QUOTING_CHARS = new UnicodeSet("[[[:script=Latn:][:script=Cyrl:]]&[[:L:][:M:]]]").freeze();
        private static final UnicodeSet SYNTAX_CHARS = new UnicodeSet("[a-zA-Z]").freeze();
        private List<Object> items = new ArrayList();
        private transient PatternTokenizer tokenizer = new PatternTokenizer().setSyntaxCharacters(SYNTAX_CHARS).setExtraQuotingCharacters(QUOTING_CHARS).setUsingQuote(true);

        @Deprecated
        public final FormatParser set(String string) {
            return set(string, false);
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
                buffer.setLength(0);
                int status = this.tokenizer.next(buffer);
                if (status == 0) {
                    addVariable(variable, false);
                    return this;
                } else if (status == 1) {
                    if (!(variable.length() == 0 || buffer.charAt(0) == variable.charAt(0))) {
                        addVariable(variable, false);
                    }
                    variable.append(buffer);
                } else {
                    addVariable(variable, false);
                    this.items.add(buffer.toString());
                }
            }
        }

        private void addVariable(StringBuffer variable, boolean strict) {
            if (variable.length() != 0) {
                this.items.add(new VariableField(variable.toString(), strict));
                variable.setLength(0);
            }
        }

        @Deprecated
        public List<Object> getItems() {
            return this.items;
        }

        @Deprecated
        public String toString() {
            return toString(0, this.items.size());
        }

        @Deprecated
        public String toString(int start, int limit) {
            StringBuilder result = new StringBuilder();
            for (int i = start; i < limit; i++) {
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
            int foundMask = 0;
            for (Object item : this.items) {
                if (item instanceof VariableField) {
                    foundMask |= 1 << ((VariableField) item).getType();
                }
            }
            boolean isDate = (foundMask & 1023) != 0;
            boolean isTime = (DateTimePatternGenerator.TIME_MASK & foundMask) != 0;
            if (isDate) {
                return isTime;
            }
            return false;
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

    private static class SkeletonFields {
        static final /* synthetic */ boolean -assertionsDisabled = (SkeletonFields.class.desiredAssertionStatus() ^ 1);
        private static final byte DEFAULT_CHAR = (byte) 0;
        private static final byte DEFAULT_LENGTH = (byte) 0;
        private byte[] chars;
        private byte[] lengths;

        /* synthetic */ SkeletonFields(SkeletonFields -this0) {
            this();
        }

        private SkeletonFields() {
            this.chars = new byte[16];
            this.lengths = new byte[16];
        }

        public void clear() {
            Arrays.fill(this.chars, (byte) 0);
            Arrays.fill(this.lengths, (byte) 0);
        }

        void copyFieldFrom(SkeletonFields other, int field) {
            this.chars[field] = other.chars[field];
            this.lengths[field] = other.lengths[field];
        }

        void clearField(int field) {
            this.chars[field] = (byte) 0;
            this.lengths[field] = (byte) 0;
        }

        char getFieldChar(int field) {
            return (char) this.chars[field];
        }

        int getFieldLength(int field) {
            return this.lengths[field];
        }

        void populate(int field, String value) {
            char[] toCharArray = value.toCharArray();
            int length = toCharArray.length;
            int i = 0;
            while (i < length) {
                char ch = toCharArray[i];
                if (-assertionsDisabled || ch == value.charAt(0)) {
                    i++;
                } else {
                    throw new AssertionError();
                }
            }
            populate(field, value.charAt(0), value.length());
        }

        void populate(int field, char ch, int length) {
            if (!-assertionsDisabled && ch > 127) {
                throw new AssertionError();
            } else if (-assertionsDisabled || length <= 127) {
                this.chars[field] = (byte) ch;
                this.lengths[field] = (byte) length;
            } else {
                throw new AssertionError();
            }
        }

        public boolean isFieldEmpty(int field) {
            return this.lengths[field] == (byte) 0;
        }

        public String toString() {
            return appendTo(new StringBuilder()).toString();
        }

        public String toCanonicalString() {
            return appendTo(new StringBuilder(), true).toString();
        }

        public StringBuilder appendTo(StringBuilder sb) {
            return appendTo(sb, false);
        }

        private StringBuilder appendTo(StringBuilder sb, boolean canonical) {
            for (int i = 0; i < 16; i++) {
                appendFieldTo(i, sb, canonical);
            }
            return sb;
        }

        public StringBuilder appendFieldTo(int field, StringBuilder sb) {
            return appendFieldTo(field, sb, false);
        }

        private StringBuilder appendFieldTo(int field, StringBuilder sb, boolean canonical) {
            char ch = (char) this.chars[field];
            int length = this.lengths[field];
            if (canonical) {
                ch = DateTimePatternGenerator.getCanonicalChar(field, ch);
            }
            for (int i = 0; i < length; i++) {
                sb.append(ch);
            }
            return sb;
        }

        public int compareTo(SkeletonFields other) {
            for (int i = 0; i < 16; i++) {
                int charDiff = this.chars[i] - other.chars[i];
                if (charDiff != 0) {
                    return charDiff;
                }
                int lengthDiff = this.lengths[i] - other.lengths[i];
                if (lengthDiff != 0) {
                    return lengthDiff;
                }
            }
            return 0;
        }

        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (other == null || !(other instanceof SkeletonFields)) {
                return false;
            }
            return compareTo((SkeletonFields) other) == 0;
        }

        public int hashCode() {
            return Arrays.hashCode(this.chars) ^ Arrays.hashCode(this.lengths);
        }
    }

    @Deprecated
    public static class VariableField {
        private final int canonicalIndex;
        private final String string;

        @Deprecated
        public VariableField(String string) {
            this(string, false);
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
            return DateTimePatternGenerator.types[this.canonicalIndex][1];
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
            return DateTimePatternGenerator.types[this.canonicalIndex][2] > 0;
        }

        private int getCanonicalIndex() {
            return this.canonicalIndex;
        }

        @Deprecated
        public String toString() {
            return this.string;
        }
    }

    public static DateTimePatternGenerator getEmptyInstance() {
        DateTimePatternGenerator instance = new DateTimePatternGenerator();
        instance.addCanonicalItems();
        instance.fillInMissing();
        return instance;
    }

    protected DateTimePatternGenerator() {
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
        result = new DateTimePatternGenerator();
        result.initData(uLocale);
        result.freeze();
        DTPNG_CACHE.put(localeKey, result);
        return result;
    }

    private void initData(ULocale uLocale) {
        PatternInfo returnInfo = new PatternInfo();
        addCanonicalItems();
        addICUPatterns(returnInfo, uLocale);
        addCLDRData(returnInfo, uLocale);
        setDateTimeFromCalendar(uLocale);
        setDecimalSymbols(uLocale);
        getAllowedHourFormats(uLocale);
        fillInMissing();
    }

    private void addICUPatterns(PatternInfo returnInfo, ULocale uLocale) {
        for (int i = 0; i <= 3; i++) {
            addPattern(((SimpleDateFormat) DateFormat.getDateInstance(i, uLocale)).toPattern(), false, returnInfo);
            SimpleDateFormat df = (SimpleDateFormat) DateFormat.getTimeInstance(i, uLocale);
            addPattern(df.toPattern(), false, returnInfo);
            if (i == 3) {
                consumeShortTimePattern(df.toPattern(), returnInfo);
            }
        }
    }

    private String getCalendarTypeToUse(ULocale uLocale) {
        String calendarTypeToUse = uLocale.getKeywordValue("calendar");
        if (calendarTypeToUse == null) {
            calendarTypeToUse = Calendar.getKeywordValuesForLocale("calendar", uLocale, true)[0];
        }
        if (calendarTypeToUse == null) {
            return "gregorian";
        }
        return calendarTypeToUse;
    }

    private void consumeShortTimePattern(String shortTimePattern, PatternInfo returnInfo) {
        FormatParser fp = new FormatParser();
        fp.set(shortTimePattern);
        List<Object> items = fp.getItems();
        for (int idx = 0; idx < items.size(); idx++) {
            VariableField item = items.get(idx);
            if (item instanceof VariableField) {
                VariableField fld = item;
                if (fld.getType() == 11) {
                    this.defaultHourFormatChar = fld.toString().charAt(0);
                    break;
                }
            }
        }
        hackTimes(returnInfo, shortTimePattern);
    }

    private void fillInMissing() {
        for (int i = 0; i < 16; i++) {
            if (getAppendItemFormat(i) == null) {
                setAppendItemFormat(i, "{0} ├{2}: {1}┤");
            }
            if (getAppendItemName(i) == null) {
                setAppendItemName(i, "F" + i);
            }
        }
    }

    private void addCLDRData(PatternInfo returnInfo, ULocale uLocale) {
        ICUResourceBundle rb = (ICUResourceBundle) UResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME, uLocale);
        String calendarTypeToUse = getCalendarTypeToUse(uLocale);
        try {
            rb.getAllItemsWithFallback("calendar/" + calendarTypeToUse + "/appendItems", new AppendItemFormatsSink(this, null));
        } catch (MissingResourceException e) {
        }
        try {
            rb.getAllItemsWithFallback("fields", new AppendItemNamesSink(this, null));
        } catch (MissingResourceException e2) {
        }
        try {
            rb.getAllItemsWithFallback("calendar/" + calendarTypeToUse + "/availableFormats", new AvailableFormatsSink(returnInfo));
        } catch (MissingResourceException e3) {
        }
    }

    private void setDateTimeFromCalendar(ULocale uLocale) {
        setDateTimeFormat(Calendar.getDateTimePattern(Calendar.getInstance(uLocale), uLocale, 2));
    }

    private void setDecimalSymbols(ULocale uLocale) {
        setDecimal(String.valueOf(new DecimalFormatSymbols(uLocale).getDecimalSeparator()));
    }

    static {
        HashMap<String, String[]> temp = new HashMap();
        ((ICUResourceBundle) UResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME, "supplementalData", ICUResourceBundle.ICU_DATA_CLASS_LOADER)).getAllItemsWithFallback("timeData", new DayPeriodAllowedHoursSink(temp, null));
        LOCALE_TO_ALLOWED_HOUR = Collections.unmodifiableMap(temp);
    }

    private void getAllowedHourFormats(ULocale uLocale) {
        ULocale max = ULocale.addLikelySubtags(uLocale);
        String country = max.getCountry();
        if (country.isEmpty()) {
            country = "001";
        }
        String[] list = (String[]) LOCALE_TO_ALLOWED_HOUR.get(max.getLanguage() + BaseLocale.SEP + country);
        if (list == null) {
            list = (String[]) LOCALE_TO_ALLOWED_HOUR.get(country);
            if (list == null) {
                list = LAST_RESORT_ALLOWED_HOUR_FORMAT;
            }
        }
        this.allowedHourFormats = list;
    }

    @Deprecated
    public char getDefaultHourFormatChar() {
        return this.defaultHourFormatChar;
    }

    @Deprecated
    public void setDefaultHourFormatChar(char defaultHourFormatChar) {
        this.defaultHourFormatChar = defaultHourFormatChar;
    }

    private void hackTimes(PatternInfo returnInfo, String shortTimePattern) {
        int i;
        Object item;
        char ch;
        this.fp.set(shortTimePattern);
        StringBuilder mmss = new StringBuilder();
        boolean gotMm = false;
        for (i = 0; i < this.fp.items.size(); i++) {
            item = this.fp.items.get(i);
            if (!(item instanceof String)) {
                ch = item.toString().charAt(0);
                if (ch != 'm') {
                    if (ch != 's') {
                        if (gotMm || ch == 'z' || ch == 'Z' || ch == 'v' || ch == 'V') {
                            break;
                        }
                    } else if (gotMm) {
                        mmss.append(item);
                        addPattern(mmss.toString(), false, returnInfo);
                    }
                } else {
                    gotMm = true;
                    mmss.append(item);
                }
            } else if (gotMm) {
                mmss.append(this.fp.quoteLiteral(item.toString()));
            }
        }
        BitSet variables = new BitSet();
        BitSet nuke = new BitSet();
        for (i = 0; i < this.fp.items.size(); i++) {
            item = this.fp.items.get(i);
            if (item instanceof VariableField) {
                variables.set(i);
                ch = item.toString().charAt(0);
                if (ch == 's' || ch == 'S') {
                    nuke.set(i);
                    int j = i - 1;
                    while (j >= 0 && !variables.get(j)) {
                        nuke.set(i);
                        j++;
                    }
                }
            }
        }
        addPattern(getFilteredPattern(this.fp, nuke), false, returnInfo);
    }

    private static String getFilteredPattern(FormatParser fp, BitSet nuke) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < fp.items.size(); i++) {
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
    public static int getAppendFormatNumber(Key key) {
        for (int i = 0; i < CLDR_FIELD_APPEND.length; i++) {
            if (key.contentEquals(CLDR_FIELD_APPEND[i])) {
                return i;
            }
        }
        return -1;
    }

    @Deprecated
    public static int getAppendFormatNumber(String string) {
        for (int i = 0; i < CLDR_FIELD_APPEND.length; i++) {
            if (CLDR_FIELD_APPEND[i].equals(string)) {
                return i;
            }
        }
        return -1;
    }

    private static int getCLDRFieldNumber(Key key) {
        for (int i = 0; i < CLDR_FIELD_NAME.length; i++) {
            if (key.contentEquals(CLDR_FIELD_NAME[i])) {
                return i;
            }
        }
        return -1;
    }

    public String getBestPattern(String skeleton) {
        return getBestPattern(skeleton, null, 0);
    }

    public String getBestPattern(String skeleton, int options) {
        return getBestPattern(skeleton, null, options);
    }

    /* JADX WARNING: Missing block: B:31:0x00dc, code:
            if (r11 != null) goto L_0x00e7;
     */
    /* JADX WARNING: Missing block: B:32:0x00de, code:
            if (r18 != null) goto L_0x00e3;
     */
    /* JADX WARNING: Missing block: B:33:0x00e0, code:
            r18 = "";
     */
    /* JADX WARNING: Missing block: B:34:0x00e3, code:
            return r18;
     */
    /* JADX WARNING: Missing block: B:38:0x00e7, code:
            if (r18 != null) goto L_0x00ea;
     */
    /* JADX WARNING: Missing block: B:39:0x00e9, code:
            return r11;
     */
    /* JADX WARNING: Missing block: B:41:0x00fd, code:
            return android.icu.impl.SimpleFormatterImpl.formatRawPattern(getDateTimeFormat(), 2, 2, r18, r11);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private String getBestPattern(String skeleton, DateTimeMatcher skipMatcher, int options) {
        EnumSet<DTPGflags> flags = EnumSet.noneOf(DTPGflags.class);
        StringBuilder stringBuilder = new StringBuilder(skeleton);
        int inQuoted = 0;
        for (int patPos = 0; patPos < skeleton.length(); patPos++) {
            char patChr = skeleton.charAt(patPos);
            if (patChr == PatternTokenizer.SINGLE_QUOTE) {
                inQuoted ^= 1;
            } else if (inQuoted == 0) {
                if (patChr == 'j') {
                    stringBuilder.setCharAt(patPos, this.defaultHourFormatChar);
                } else if (patChr == 'C') {
                    String preferred = this.allowedHourFormats[0];
                    stringBuilder.setCharAt(patPos, preferred.charAt(0));
                    DTPGflags alt = DTPGflags.getFlag(preferred);
                    if (alt != null) {
                        flags.add(alt);
                    }
                } else if (patChr == 'J') {
                    stringBuilder.setCharAt(patPos, 'H');
                    flags.add(DTPGflags.SKELETON_USES_CAP_J);
                }
            }
        }
        synchronized (this) {
            this.current.set(stringBuilder.toString(), this.fp, false);
            PatternWithMatcher bestWithMatcher = getBestRaw(this.current, -1, this._distanceInfo, skipMatcher);
            if (this._distanceInfo.missingFieldMask == 0 && this._distanceInfo.extraFieldMask == 0) {
                String adjustFieldTypes = adjustFieldTypes(bestWithMatcher, this.current, flags, options);
                return adjustFieldTypes;
            }
            int neededFields = this.current.getFieldMask();
            String datePattern = getBestAppending(this.current, neededFields & 1023, this._distanceInfo, skipMatcher, flags, options);
            String timePattern = getBestAppending(this.current, neededFields & TIME_MASK, this._distanceInfo, skipMatcher, flags, options);
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
            matcher = new DateTimeMatcher().set(pattern, this.fp, false);
        } else {
            matcher = new DateTimeMatcher().set(skeletonToUse, this.fp, false);
        }
        String basePattern = matcher.getBasePattern();
        PatternWithSkeletonFlag previousPatternWithSameBase = (PatternWithSkeletonFlag) this.basePattern_pattern.get(basePattern);
        if (!(previousPatternWithSameBase == null || (previousPatternWithSameBase.skeletonWasSpecified && (skeletonToUse == null || (override ^ 1) == 0)))) {
            returnInfo.status = 1;
            returnInfo.conflictingPattern = previousPatternWithSameBase.pattern;
            if (!override) {
                return this;
            }
        }
        PatternWithSkeletonFlag previousValue = (PatternWithSkeletonFlag) this.skeleton2pattern.get(matcher);
        if (previousValue != null) {
            returnInfo.status = 2;
            returnInfo.conflictingPattern = previousValue.pattern;
            if (!override || (skeletonToUse != null && previousValue.skeletonWasSpecified)) {
                return this;
            }
        }
        returnInfo.status = 0;
        returnInfo.conflictingPattern = "";
        if (skeletonToUse == null) {
            z = false;
        }
        PatternWithSkeletonFlag patWithSkelFlag = new PatternWithSkeletonFlag(pattern, z);
        this.skeleton2pattern.put(matcher, patWithSkelFlag);
        this.basePattern_pattern.put(basePattern, patWithSkelFlag);
        return this;
    }

    public String getSkeleton(String pattern) {
        String dateTimeMatcher;
        synchronized (this) {
            this.current.set(pattern, this.fp, false);
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
            this.current.set(pattern, this.fp, false);
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
        return replaceFieldTypes(pattern, skeleton, 0);
    }

    public String replaceFieldTypes(String pattern, String skeleton, int options) {
        String adjustFieldTypes;
        synchronized (this) {
            adjustFieldTypes = adjustFieldTypes(new PatternWithMatcher(pattern, null), this.current.set(skeleton, this.fp, false), EnumSet.noneOf(DTPGflags.class), options);
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
                if (!CANONICAL_SET.contains(pattern) && getBestPattern(cur.toString(), cur, 0).equals(pattern)) {
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
        char first = skeleton.charAt(0);
        for (int i = 1; i < skeleton.length(); i++) {
            if (skeleton.charAt(i) != first) {
                return false;
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

    public DateTimePatternGenerator freeze() {
        this.frozen = true;
        return this;
    }

    public DateTimePatternGenerator cloneAsThawed() {
        DateTimePatternGenerator result = (DateTimePatternGenerator) clone();
        this.frozen = false;
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
            result.frozen = false;
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
            return false;
        }
        Iterator<String> it2 = parser2.iterator();
        for (String item : parser1) {
            if (types[getCanonicalIndex(item, false)][1] != types[getCanonicalIndex((String) it2.next(), false)][1]) {
                return false;
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

    private String getBestAppending(DateTimeMatcher source, int missingFields, DistanceInfo distInfo, DateTimeMatcher skipMatcher, EnumSet<DTPGflags> flags, int options) {
        String resultPattern = null;
        if (missingFields != 0) {
            PatternWithMatcher resultPatternWithMatcher = getBestRaw(source, missingFields, distInfo, skipMatcher);
            resultPattern = adjustFieldTypes(resultPatternWithMatcher, source, flags, options);
            while (distInfo.missingFieldMask != 0) {
                if ((distInfo.missingFieldMask & SECOND_AND_FRACTIONAL_MASK) == 16384 && (missingFields & SECOND_AND_FRACTIONAL_MASK) == SECOND_AND_FRACTIONAL_MASK) {
                    resultPatternWithMatcher.pattern = resultPattern;
                    flags = EnumSet.copyOf(flags);
                    flags.add(DTPGflags.FIX_FRACTIONAL_SECONDS);
                    resultPattern = adjustFieldTypes(resultPatternWithMatcher, source, flags, options);
                    distInfo.missingFieldMask &= -16385;
                } else {
                    int startingMask = distInfo.missingFieldMask;
                    String temp = adjustFieldTypes(getBestRaw(source, distInfo.missingFieldMask, distInfo, skipMatcher), source, flags, options);
                    resultPattern = SimpleFormatterImpl.formatRawPattern(getAppendFormat(getTopBitNumber(startingMask & (~distInfo.missingFieldMask))), 2, 3, resultPattern, temp, getAppendName(topField));
                }
            }
        }
        return resultPattern;
    }

    private String getAppendName(int foundMask) {
        return "'" + this.appendItemNames[foundMask] + "'";
    }

    private String getAppendFormat(int foundMask) {
        return this.appendItemFormats[foundMask];
    }

    private int getTopBitNumber(int foundMask) {
        int i = 0;
        while (foundMask != 0) {
            foundMask >>>= 1;
            i++;
        }
        return i - 1;
    }

    private void addCanonicalItems() {
        PatternInfo patternInfo = new PatternInfo();
        for (Object valueOf : CANONICAL_ITEMS) {
            addPattern(String.valueOf(valueOf), false, patternInfo);
        }
    }

    private PatternWithMatcher getBestRaw(DateTimeMatcher source, int includeMask, DistanceInfo missingFields, DateTimeMatcher skipMatcher) {
        int bestDistance = Integer.MAX_VALUE;
        PatternWithMatcher bestPatternWithMatcher = new PatternWithMatcher("", null);
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
                newPattern.append(this.fp.quoteLiteral((String) item));
            } else {
                char c;
                int i;
                VariableField variableField = item;
                StringBuilder fieldBuilder = new StringBuilder(variableField.toString());
                int type = variableField.getType();
                if (type == 10 && (flags.isEmpty() ^ 1) != 0) {
                    c = flags.contains(DTPGflags.SKELETON_USES_b) ? 'b' : flags.contains(DTPGflags.SKELETON_USES_B) ? 'B' : '0';
                    if (c != '0') {
                        int len = fieldBuilder.length();
                        fieldBuilder.setLength(0);
                        for (i = len; i > 0; i--) {
                            fieldBuilder.append(c);
                        }
                    }
                }
                if (flags.contains(DTPGflags.FIX_FRACTIONAL_SECONDS) && type == 13) {
                    fieldBuilder.append(this.decimal);
                    inputRequest.original.appendFieldTo(14, fieldBuilder);
                } else if (inputRequest.type[type] != 0) {
                    char reqFieldChar = inputRequest.original.getFieldChar(type);
                    int reqFieldLen = inputRequest.original.getFieldLength(type);
                    if (reqFieldChar == 'E' && reqFieldLen < 3) {
                        reqFieldLen = 3;
                    }
                    int adjFieldLen = reqFieldLen;
                    DateTimeMatcher matcherWithSkeleton = patternWithMatcher.matcherWithSkeleton;
                    if ((type == 11 && (options & 2048) == 0) || ((type == 12 && (options & 4096) == 0) || (type == 13 && (options & 8192) == 0))) {
                        adjFieldLen = fieldBuilder.length();
                    } else if (matcherWithSkeleton != null) {
                        int skelFieldLen = matcherWithSkeleton.original.getFieldLength(type);
                        boolean patFieldIsNumeric = variableField.isNumeric();
                        boolean skelFieldIsNumeric = matcherWithSkeleton.fieldIsNumeric(type);
                        if (skelFieldLen == reqFieldLen || ((patFieldIsNumeric && (skelFieldIsNumeric ^ 1) != 0) || (skelFieldIsNumeric && (patFieldIsNumeric ^ 1) != 0))) {
                            adjFieldLen = fieldBuilder.length();
                        }
                    }
                    if (type == 11 || type == 3 || type == 6 || (type == 1 && reqFieldChar != 'Y')) {
                        c = fieldBuilder.charAt(0);
                    } else {
                        c = reqFieldChar;
                    }
                    if (type == 11 && flags.contains(DTPGflags.SKELETON_USES_CAP_J)) {
                        c = this.defaultHourFormatChar;
                    }
                    fieldBuilder = new StringBuilder();
                    for (i = adjFieldLen; i > 0; i--) {
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
        for (int i = 0; i < 16; i++) {
            if (((1 << i) & mask) != 0) {
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
        int i = getCanonicalIndex(s, true);
        String name = FIELD_NAME[types[i][1]];
        if (types[i][2] < 0) {
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
        char ch = s.charAt(0);
        for (i = 1; i < len; i++) {
            if (s.charAt(i) != ch) {
                return -1;
            }
        }
        int bestRow = -1;
        for (i = 0; i < types.length; i++) {
            int[] row = types[i];
            if (row[0] == ch) {
                bestRow = i;
                if (row[3] <= len && row[row.length - 1] >= len) {
                    return i;
                }
            }
        }
        if (strict) {
            bestRow = -1;
        }
        return bestRow;
    }

    private static char getCanonicalChar(int field, char reference) {
        if (reference == 'h' || reference == 'K') {
            return 'h';
        }
        for (int[] row : types) {
            if (row[1] == field) {
                return (char) row[0];
            }
        }
        throw new IllegalArgumentException("Could not find field " + field);
    }
}
