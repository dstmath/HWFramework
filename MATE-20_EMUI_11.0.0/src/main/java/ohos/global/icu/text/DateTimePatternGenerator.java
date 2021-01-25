package ohos.global.icu.text;

import java.lang.reflect.Array;
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
import ohos.agp.styles.attributes.TimePickerAttrsConstants;
import ohos.devtools.JLogConstants;
import ohos.global.icu.impl.ICUCache;
import ohos.global.icu.impl.ICUData;
import ohos.global.icu.impl.ICUResourceBundle;
import ohos.global.icu.impl.PatternTokenizer;
import ohos.global.icu.impl.SimpleCache;
import ohos.global.icu.impl.SimpleFormatterImpl;
import ohos.global.icu.impl.UResource;
import ohos.global.icu.util.Calendar;
import ohos.global.icu.util.Freezable;
import ohos.global.icu.util.ICUCloneNotSupportedException;
import ohos.global.icu.util.Region;
import ohos.global.icu.util.ULocale;
import ohos.global.icu.util.UResourceBundle;

public class DateTimePatternGenerator implements Freezable<DateTimePatternGenerator>, Cloneable {
    private static final DisplayWidth APPENDITEM_WIDTH = DisplayWidth.WIDE;
    private static final int APPENDITEM_WIDTH_INT = APPENDITEM_WIDTH.ordinal();
    private static final String[] CANONICAL_ITEMS = {"G", DateFormat.YEAR, "Q", DateFormat.NUM_MONTH, "w", "W", DateFormat.ABBR_WEEKDAY, DateFormat.DAY, "D", "F", "a", DateFormat.HOUR24, DateFormat.MINUTE, DateFormat.SECOND, "S", DateFormat.ABBR_GENERIC_TZ};
    private static final Set<String> CANONICAL_SET = new HashSet(Arrays.asList(CANONICAL_ITEMS));
    private static final String[] CLDR_FIELD_APPEND = {"Era", "Year", "Quarter", "Month", "Week", "*", "Day-Of-Week", "Day", "*", "*", "*", "Hour", "Minute", "Second", "*", "Timezone"};
    private static final String[] CLDR_FIELD_NAME = {"era", "year", "quarter", "month", "week", "weekOfMonth", "weekday", "day", "dayOfYear", "weekdayOfMonth", "dayperiod", TimePickerAttrsConstants.HOUR, TimePickerAttrsConstants.MINUTE, TimePickerAttrsConstants.SECOND, "*", "zone"};
    private static final DisplayWidth[] CLDR_FIELD_WIDTH = DisplayWidth.values();
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
    private static final String[] FIELD_NAME = {"Era", "Year", "Quarter", "Month", "Week_in_Year", "Week_in_Month", "Weekday", "Day", "Day_Of_Year", "Day_of_Week_in_Month", "Dayperiod", "Hour", "Minute", "Second", "Fractional_Second", "Zone"};
    private static final int FRACTIONAL_MASK = 16384;
    public static final int FRACTIONAL_SECOND = 14;
    public static final int HOUR = 11;
    private static final String[] LAST_RESORT_ALLOWED_HOUR_FORMAT = {DateFormat.HOUR24};
    static final Map<String, String[]> LOCALE_TO_ALLOWED_HOUR;
    private static final int LONG = -260;
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
    private static final int SHORT = -259;
    private static final int SHORTER = -258;
    private static final int TIME_MASK = 64512;
    @Deprecated
    public static final int TYPE_LIMIT = 16;
    public static final int WEEKDAY = 6;
    public static final int WEEK_OF_MONTH = 5;
    public static final int WEEK_OF_YEAR = 4;
    public static final int YEAR = 1;
    public static final int ZONE = 15;
    private static final int[][] types = {new int[]{71, 0, SHORT, 1, 3}, new int[]{71, 0, LONG, 4}, new int[]{71, 0, NARROW, 5}, new int[]{121, 1, 256, 1, 20}, new int[]{89, 1, 272, 1, 20}, new int[]{117, 1, 288, 1, 20}, new int[]{114, 1, 304, 1, 20}, new int[]{85, 1, SHORT, 1, 3}, new int[]{85, 1, LONG, 4}, new int[]{85, 1, NARROW, 5}, new int[]{81, 2, 256, 1, 2}, new int[]{81, 2, SHORT, 3}, new int[]{81, 2, LONG, 4}, new int[]{81, 2, NARROW, 5}, new int[]{113, 2, 272, 1, 2}, new int[]{113, 2, -275, 3}, new int[]{113, 2, -276, 4}, new int[]{113, 2, -273, 5}, new int[]{77, 3, 256, 1, 2}, new int[]{77, 3, SHORT, 3}, new int[]{77, 3, LONG, 4}, new int[]{77, 3, NARROW, 5}, new int[]{76, 3, 272, 1, 2}, new int[]{76, 3, -275, 3}, new int[]{76, 3, -276, 4}, new int[]{76, 3, -273, 5}, new int[]{108, 3, 272, 1, 1}, new int[]{119, 4, 256, 1, 2}, new int[]{87, 5, 256, 1}, new int[]{69, 6, SHORT, 1, 3}, new int[]{69, 6, LONG, 4}, new int[]{69, 6, NARROW, 5}, new int[]{69, 6, SHORTER, 6}, new int[]{99, 6, 288, 1, 2}, new int[]{99, 6, -291, 3}, new int[]{99, 6, -292, 4}, new int[]{99, 6, -289, 5}, new int[]{99, 6, -290, 6}, new int[]{101, 6, 272, 1, 2}, new int[]{101, 6, -275, 3}, new int[]{101, 6, -276, 4}, new int[]{101, 6, -273, 5}, new int[]{101, 6, -274, 6}, new int[]{100, 7, 256, 1, 2}, new int[]{103, 7, 272, 1, 20}, new int[]{68, 8, 256, 1, 3}, new int[]{70, 9, 256, 1}, new int[]{97, 10, SHORT, 1, 3}, new int[]{97, 10, LONG, 4}, new int[]{97, 10, NARROW, 5}, new int[]{98, 10, -275, 1, 3}, new int[]{98, 10, -276, 4}, new int[]{98, 10, -273, 5}, new int[]{66, 10, -307, 1, 3}, new int[]{66, 10, -308, 4}, new int[]{66, 10, -305, 5}, new int[]{72, 11, JLogConstants.JLID_INPUTMETHOD_PRESS_KEY_END, 1, 2}, new int[]{107, 11, JLogConstants.JLID_ABILITY_ONSTOP, 1, 2}, new int[]{104, 11, 256, 1, 2}, new int[]{75, 11, 272, 1, 2}, new int[]{109, 12, 256, 1, 2}, new int[]{115, 13, 256, 1, 2}, new int[]{65, 13, 272, 1, 1000}, new int[]{83, 14, 256, 1, 1000}, new int[]{118, 15, -291, 1}, new int[]{118, 15, -292, 4}, new int[]{122, 15, SHORT, 1, 3}, new int[]{122, 15, LONG, 4}, new int[]{90, 15, -273, 1, 3}, new int[]{90, 15, -276, 4}, new int[]{90, 15, -275, 5}, new int[]{79, 15, -275, 1}, new int[]{79, 15, -276, 4}, new int[]{86, 15, -275, 1}, new int[]{86, 15, -276, 2}, new int[]{86, 15, -277, 3}, new int[]{86, 15, -278, 4}, new int[]{88, 15, -273, 1}, new int[]{88, 15, -275, 2}, new int[]{88, 15, -276, 4}, new int[]{120, 15, -273, 1}, new int[]{120, 15, -275, 2}, new int[]{120, 15, -276, 4}};
    private transient DistanceInfo _distanceInfo = new DistanceInfo();
    private String[] allowedHourFormats;
    private String[] appendItemFormats = new String[16];
    private TreeMap<String, PatternWithSkeletonFlag> basePattern_pattern = new TreeMap<>();
    private Set<String> cldrAvailableFormatKeys = new HashSet(20);
    private transient DateTimeMatcher current = new DateTimeMatcher();
    private String dateTimeFormat = "{1} {0}";
    private String decimal = "?";
    private char defaultHourFormatChar = 'H';
    private String[][] fieldDisplayNames = ((String[][]) Array.newInstance(String.class, 16, DisplayWidth.COUNT));
    private transient FormatParser fp = new FormatParser();
    private volatile boolean frozen = false;
    private TreeMap<DateTimeMatcher, PatternWithSkeletonFlag> skeleton2pattern = new TreeMap<>();

    /* access modifiers changed from: private */
    public enum DTPGflags {
        FIX_FRACTIONAL_SECONDS,
        SKELETON_USES_CAP_J
    }

    public static final class PatternInfo {
        public static final int BASE_CONFLICT = 1;
        public static final int CONFLICT = 2;
        public static final int OK = 0;
        public String conflictingPattern;
        public int status;
    }

    private int getTopBitNumber(int i) {
        int i2 = 0;
        while (i != 0) {
            i >>>= 1;
            i2++;
        }
        return i2 - 1;
    }

    public static DateTimePatternGenerator getEmptyInstance() {
        DateTimePatternGenerator dateTimePatternGenerator = new DateTimePatternGenerator();
        dateTimePatternGenerator.addCanonicalItems();
        dateTimePatternGenerator.fillInMissing();
        return dateTimePatternGenerator;
    }

    protected DateTimePatternGenerator() {
    }

    public static DateTimePatternGenerator getInstance() {
        return getInstance(ULocale.getDefault(ULocale.Category.FORMAT));
    }

    public static DateTimePatternGenerator getInstance(ULocale uLocale) {
        return getFrozenInstance(uLocale).cloneAsThawed();
    }

    public static DateTimePatternGenerator getInstance(Locale locale) {
        return getInstance(ULocale.forLocale(locale));
    }

    @Deprecated
    public static DateTimePatternGenerator getFrozenInstance(ULocale uLocale) {
        String uLocale2 = uLocale.toString();
        DateTimePatternGenerator dateTimePatternGenerator = DTPNG_CACHE.get(uLocale2);
        if (dateTimePatternGenerator != null) {
            return dateTimePatternGenerator;
        }
        DateTimePatternGenerator dateTimePatternGenerator2 = new DateTimePatternGenerator();
        dateTimePatternGenerator2.initData(uLocale);
        dateTimePatternGenerator2.freeze();
        DTPNG_CACHE.put(uLocale2, dateTimePatternGenerator2);
        return dateTimePatternGenerator2;
    }

    private void initData(ULocale uLocale) {
        PatternInfo patternInfo = new PatternInfo();
        addCanonicalItems();
        addICUPatterns(patternInfo, uLocale);
        addCLDRData(patternInfo, uLocale);
        setDateTimeFromCalendar(uLocale);
        setDecimalSymbols(uLocale);
        getAllowedHourFormats(uLocale);
        fillInMissing();
    }

    private void addICUPatterns(PatternInfo patternInfo, ULocale uLocale) {
        for (int i = 0; i <= 3; i++) {
            addPattern(((SimpleDateFormat) DateFormat.getDateInstance(i, uLocale)).toPattern(), false, patternInfo);
            SimpleDateFormat simpleDateFormat = (SimpleDateFormat) DateFormat.getTimeInstance(i, uLocale);
            addPattern(simpleDateFormat.toPattern(), false, patternInfo);
            if (i == 3) {
                consumeShortTimePattern(simpleDateFormat.toPattern(), patternInfo);
            }
        }
    }

    private String getCalendarTypeToUse(ULocale uLocale) {
        String keywordValue = uLocale.getKeywordValue("calendar");
        if (keywordValue == null) {
            keywordValue = Calendar.getKeywordValuesForLocale("calendar", uLocale, true)[0];
        }
        return keywordValue == null ? "gregorian" : keywordValue;
    }

    private void consumeShortTimePattern(String str, PatternInfo patternInfo) {
        hackTimes(patternInfo, str);
    }

    /* access modifiers changed from: private */
    public class AppendItemFormatsSink extends UResource.Sink {
        static final /* synthetic */ boolean $assertionsDisabled = false;

        private AppendItemFormatsSink() {
        }

        @Override // ohos.global.icu.impl.UResource.Sink
        public void put(UResource.Key key, UResource.Value value, boolean z) {
            UResource.Table table = value.getTable();
            for (int i = 0; table.getKeyAndValue(i, key, value); i++) {
                int appendFormatNumber = DateTimePatternGenerator.getAppendFormatNumber(key);
                if (DateTimePatternGenerator.this.getAppendItemFormat(appendFormatNumber) == null) {
                    DateTimePatternGenerator.this.setAppendItemFormat(appendFormatNumber, value.toString());
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public class AppendItemNamesSink extends UResource.Sink {
        private AppendItemNamesSink() {
        }

        @Override // ohos.global.icu.impl.UResource.Sink
        public void put(UResource.Key key, UResource.Value value, boolean z) {
            int cLDRFieldAndWidthNumber;
            UResource.Table table = value.getTable();
            for (int i = 0; table.getKeyAndValue(i, key, value); i++) {
                if (value.getType() == 2 && (cLDRFieldAndWidthNumber = DateTimePatternGenerator.getCLDRFieldAndWidthNumber(key)) != -1) {
                    int i2 = cLDRFieldAndWidthNumber / DisplayWidth.COUNT;
                    DisplayWidth displayWidth = DateTimePatternGenerator.CLDR_FIELD_WIDTH[cLDRFieldAndWidthNumber % DisplayWidth.COUNT];
                    UResource.Table table2 = value.getTable();
                    int i3 = 0;
                    while (true) {
                        if (!table2.getKeyAndValue(i3, key, value)) {
                            break;
                        } else if (!key.contentEquals("dn")) {
                            i3++;
                        } else if (DateTimePatternGenerator.this.getFieldDisplayName(i2, displayWidth) == null) {
                            DateTimePatternGenerator.this.setFieldDisplayName(i2, displayWidth, value.toString());
                        }
                    }
                }
            }
        }
    }

    private void fillInMissing() {
        for (int i = 0; i < 16; i++) {
            if (getAppendItemFormat(i) == null) {
                setAppendItemFormat(i, "{0} ├{2}: {1}┤");
            }
            if (getFieldDisplayName(i, DisplayWidth.WIDE) == null) {
                DisplayWidth displayWidth = DisplayWidth.WIDE;
                setFieldDisplayName(i, displayWidth, "F" + i);
            }
            if (getFieldDisplayName(i, DisplayWidth.ABBREVIATED) == null) {
                setFieldDisplayName(i, DisplayWidth.ABBREVIATED, getFieldDisplayName(i, DisplayWidth.WIDE));
            }
            if (getFieldDisplayName(i, DisplayWidth.NARROW) == null) {
                setFieldDisplayName(i, DisplayWidth.NARROW, getFieldDisplayName(i, DisplayWidth.ABBREVIATED));
            }
        }
    }

    /* access modifiers changed from: private */
    public class AvailableFormatsSink extends UResource.Sink {
        PatternInfo returnInfo;

        public AvailableFormatsSink(PatternInfo patternInfo) {
            this.returnInfo = patternInfo;
        }

        @Override // ohos.global.icu.impl.UResource.Sink
        public void put(UResource.Key key, UResource.Value value, boolean z) {
            UResource.Table table = value.getTable();
            for (int i = 0; table.getKeyAndValue(i, key, value); i++) {
                String key2 = key.toString();
                if (!DateTimePatternGenerator.this.isAvailableFormatSet(key2)) {
                    DateTimePatternGenerator.this.setAvailableFormat(key2);
                    DateTimePatternGenerator.this.addPatternWithSkeleton(value.toString(), key2, !z, this.returnInfo);
                }
            }
        }
    }

    private void addCLDRData(PatternInfo patternInfo, ULocale uLocale) {
        ICUResourceBundle bundleInstance = UResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME, uLocale);
        String calendarTypeToUse = getCalendarTypeToUse(uLocale);
        AppendItemFormatsSink appendItemFormatsSink = new AppendItemFormatsSink();
        try {
            bundleInstance.getAllItemsWithFallback("calendar/" + calendarTypeToUse + "/appendItems", appendItemFormatsSink);
        } catch (MissingResourceException unused) {
        }
        try {
            bundleInstance.getAllItemsWithFallback("fields", new AppendItemNamesSink());
        } catch (MissingResourceException unused2) {
        }
        AvailableFormatsSink availableFormatsSink = new AvailableFormatsSink(patternInfo);
        try {
            bundleInstance.getAllItemsWithFallback("calendar/" + calendarTypeToUse + "/availableFormats", availableFormatsSink);
        } catch (MissingResourceException unused3) {
        }
    }

    private void setDateTimeFromCalendar(ULocale uLocale) {
        setDateTimeFormat(Calendar.getDateTimePattern(Calendar.getInstance(uLocale), uLocale, 2));
    }

    private void setDecimalSymbols(ULocale uLocale) {
        setDecimal(String.valueOf(new DecimalFormatSymbols(uLocale).getDecimalSeparator()));
    }

    static {
        HashMap hashMap = new HashMap();
        ICUResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME, "supplementalData", ICUResourceBundle.ICU_DATA_CLASS_LOADER).getAllItemsWithFallback("timeData", new DayPeriodAllowedHoursSink(hashMap));
        LOCALE_TO_ALLOWED_HOUR = Collections.unmodifiableMap(hashMap);
    }

    private String[] getAllowedHourFormatsLangCountry(String str, String str2) {
        String[] strArr = LOCALE_TO_ALLOWED_HOUR.get(str + "_" + str2);
        return strArr == null ? LOCALE_TO_ALLOWED_HOUR.get(str2) : strArr;
    }

    private void getAllowedHourFormats(ULocale uLocale) {
        String language = uLocale.getLanguage();
        String country = uLocale.getCountry();
        if (language.isEmpty() || country.isEmpty()) {
            ULocale addLikelySubtags = ULocale.addLikelySubtags(uLocale);
            language = addLikelySubtags.getLanguage();
            country = addLikelySubtags.getCountry();
        }
        if (language.isEmpty()) {
            language = "und";
        }
        if (country.isEmpty()) {
            country = "001";
        }
        String[] allowedHourFormatsLangCountry = getAllowedHourFormatsLangCountry(language, country);
        if (allowedHourFormatsLangCountry == null) {
            try {
                allowedHourFormatsLangCountry = getAllowedHourFormatsLangCountry(language, Region.getInstance(country).toString());
            } catch (IllegalArgumentException unused) {
            }
        }
        if (allowedHourFormatsLangCountry != null) {
            this.defaultHourFormatChar = allowedHourFormatsLangCountry[0].charAt(0);
            this.allowedHourFormats = (String[]) Arrays.copyOfRange(allowedHourFormatsLangCountry, 1, allowedHourFormatsLangCountry.length - 1);
            return;
        }
        this.allowedHourFormats = LAST_RESORT_ALLOWED_HOUR_FORMAT;
        this.defaultHourFormatChar = this.allowedHourFormats[0].charAt(0);
    }

    private static class DayPeriodAllowedHoursSink extends UResource.Sink {
        HashMap<String, String[]> tempMap;

        private DayPeriodAllowedHoursSink(HashMap<String, String[]> hashMap) {
            this.tempMap = hashMap;
        }

        @Override // ohos.global.icu.impl.UResource.Sink
        public void put(UResource.Key key, UResource.Value value, boolean z) {
            String[] strArr;
            UResource.Table table = value.getTable();
            for (int i = 0; table.getKeyAndValue(i, key, value); i++) {
                String key2 = key.toString();
                UResource.Table table2 = value.getTable();
                String[] strArr2 = null;
                String str = null;
                for (int i2 = 0; table2.getKeyAndValue(i2, key, value); i2++) {
                    if (key.contentEquals("allowed")) {
                        strArr2 = value.getStringArrayOrStringAsArray();
                    } else if (key.contentEquals("preferred")) {
                        str = value.getString();
                    }
                }
                if (strArr2 == null || strArr2.length <= 0) {
                    strArr = new String[2];
                    if (str == null) {
                        str = DateTimePatternGenerator.LAST_RESORT_ALLOWED_HOUR_FORMAT[0];
                    }
                    strArr[0] = str;
                    strArr[1] = strArr[0];
                } else {
                    strArr = new String[(strArr2.length + 1)];
                    if (str == null) {
                        str = strArr2[0];
                    }
                    strArr[0] = str;
                    System.arraycopy(strArr2, 0, strArr, 1, strArr2.length);
                }
                this.tempMap.put(key2, strArr);
            }
        }
    }

    @Deprecated
    public char getDefaultHourFormatChar() {
        return this.defaultHourFormatChar;
    }

    @Deprecated
    public void setDefaultHourFormatChar(char c) {
        this.defaultHourFormatChar = c;
    }

    private void hackTimes(PatternInfo patternInfo, String str) {
        this.fp.set(str);
        StringBuilder sb = new StringBuilder();
        int i = 0;
        boolean z = false;
        while (true) {
            if (i >= this.fp.items.size()) {
                break;
            }
            Object obj = this.fp.items.get(i);
            if (!(obj instanceof String)) {
                char charAt = obj.toString().charAt(0);
                if (charAt == 'm') {
                    sb.append(obj);
                    z = true;
                } else if (charAt != 's') {
                    if (z || charAt == 'z' || charAt == 'Z' || charAt == 'v' || charAt == 'V') {
                        break;
                    }
                } else if (z) {
                    sb.append(obj);
                    addPattern(sb.toString(), false, patternInfo);
                }
            } else if (z) {
                sb.append(this.fp.quoteLiteral(obj.toString()));
            }
            i++;
        }
        BitSet bitSet = new BitSet();
        BitSet bitSet2 = new BitSet();
        for (int i2 = 0; i2 < this.fp.items.size(); i2++) {
            Object obj2 = this.fp.items.get(i2);
            if (obj2 instanceof VariableField) {
                bitSet.set(i2);
                char charAt2 = obj2.toString().charAt(0);
                if (charAt2 == 's' || charAt2 == 'S') {
                    bitSet2.set(i2);
                    int i3 = i2 - 1;
                    while (i3 >= 0 && !bitSet.get(i3)) {
                        bitSet2.set(i2);
                        i3++;
                    }
                }
            }
        }
        addPattern(getFilteredPattern(this.fp, bitSet2), false, patternInfo);
    }

    private static String getFilteredPattern(FormatParser formatParser, BitSet bitSet) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < formatParser.items.size(); i++) {
            if (!bitSet.get(i)) {
                Object obj = formatParser.items.get(i);
                if (obj instanceof String) {
                    sb.append(formatParser.quoteLiteral(obj.toString()));
                } else {
                    sb.append(obj.toString());
                }
            }
        }
        return sb.toString();
    }

    @Deprecated
    public static int getAppendFormatNumber(UResource.Key key) {
        int i = 0;
        while (true) {
            String[] strArr = CLDR_FIELD_APPEND;
            if (i >= strArr.length) {
                return -1;
            }
            if (key.contentEquals(strArr[i])) {
                return i;
            }
            i++;
        }
    }

    @Deprecated
    public static int getAppendFormatNumber(String str) {
        int i = 0;
        while (true) {
            String[] strArr = CLDR_FIELD_APPEND;
            if (i >= strArr.length) {
                return -1;
            }
            if (strArr[i].equals(str)) {
                return i;
            }
            i++;
        }
    }

    /* access modifiers changed from: private */
    public static int getCLDRFieldAndWidthNumber(UResource.Key key) {
        for (int i = 0; i < CLDR_FIELD_NAME.length; i++) {
            for (int i2 = 0; i2 < DisplayWidth.COUNT; i2++) {
                if (key.contentEquals(CLDR_FIELD_NAME[i].concat(CLDR_FIELD_WIDTH[i2].cldrKey()))) {
                    return (i * DisplayWidth.COUNT) + i2;
                }
            }
        }
        return -1;
    }

    public String getBestPattern(String str) {
        return getBestPattern(str, null, 0);
    }

    public String getBestPattern(String str, int i) {
        return getBestPattern(str, null, i);
    }

    private String getBestPattern(String str, DateTimeMatcher dateTimeMatcher, int i) {
        String bestAppending;
        String bestAppending2;
        EnumSet<DTPGflags> noneOf = EnumSet.noneOf(DTPGflags.class);
        String mapSkeletonMetacharacters = mapSkeletonMetacharacters(str, noneOf);
        synchronized (this) {
            this.current.set(mapSkeletonMetacharacters, this.fp, false);
            PatternWithMatcher bestRaw = getBestRaw(this.current, -1, this._distanceInfo, dateTimeMatcher);
            if (this._distanceInfo.missingFieldMask == 0 && this._distanceInfo.extraFieldMask == 0) {
                return adjustFieldTypes(bestRaw, this.current, noneOf, i);
            }
            int fieldMask = this.current.getFieldMask();
            bestAppending = getBestAppending(this.current, fieldMask & 1023, this._distanceInfo, dateTimeMatcher, noneOf, i);
            bestAppending2 = getBestAppending(this.current, fieldMask & 64512, this._distanceInfo, dateTimeMatcher, noneOf, i);
        }
        if (bestAppending == null) {
            return bestAppending2 == null ? "" : bestAppending2;
        }
        if (bestAppending2 == null) {
            return bestAppending;
        }
        return SimpleFormatterImpl.formatRawPattern(getDateTimeFormat(), 2, 2, bestAppending2, bestAppending);
    }

    private String mapSkeletonMetacharacters(String str, EnumSet<DTPGflags> enumSet) {
        char c;
        StringBuilder sb = new StringBuilder();
        int i = 0;
        boolean z = false;
        while (i < str.length()) {
            char charAt = str.charAt(i);
            if (charAt == '\'') {
                z = !z;
            } else if (!z) {
                if (charAt == 'j' || charAt == 'C') {
                    int i2 = 0;
                    while (true) {
                        int i3 = i + 1;
                        if (i3 >= str.length() || str.charAt(i3) != charAt) {
                            break;
                        }
                        i2++;
                        i = i3;
                    }
                    int i4 = (i2 & 1) + 1;
                    int i5 = i2 < 2 ? 1 : (i2 >> 1) + 3;
                    char c2 = 'a';
                    if (charAt == 'j') {
                        c = this.defaultHourFormatChar;
                    } else {
                        String str2 = this.allowedHourFormats[0];
                        c = str2.charAt(0);
                        char charAt2 = str2.charAt(str2.length() - 1);
                        if (charAt2 == 'b' || charAt2 == 'B') {
                            c2 = charAt2;
                        }
                    }
                    if (c == 'H' || c == 'k') {
                        i5 = 0;
                    }
                    while (true) {
                        int i6 = i5 - 1;
                        if (i5 <= 0) {
                            break;
                        }
                        sb.append(c2);
                        i5 = i6;
                    }
                    while (true) {
                        int i7 = i4 - 1;
                        if (i4 <= 0) {
                            break;
                        }
                        sb.append(c);
                        i4 = i7;
                    }
                } else if (charAt == 'J') {
                    sb.append('H');
                    enumSet.add(DTPGflags.SKELETON_USES_CAP_J);
                } else {
                    sb.append(charAt);
                }
            }
            i++;
        }
        return sb.toString();
    }

    public DateTimePatternGenerator addPattern(String str, boolean z, PatternInfo patternInfo) {
        return addPatternWithSkeleton(str, null, z, patternInfo);
    }

    @Deprecated
    public DateTimePatternGenerator addPatternWithSkeleton(String str, String str2, boolean z, PatternInfo patternInfo) {
        DateTimeMatcher dateTimeMatcher;
        checkFrozen();
        boolean z2 = false;
        if (str2 == null) {
            dateTimeMatcher = new DateTimeMatcher().set(str, this.fp, false);
        } else {
            dateTimeMatcher = new DateTimeMatcher().set(str2, this.fp, false);
        }
        String basePattern = dateTimeMatcher.getBasePattern();
        PatternWithSkeletonFlag patternWithSkeletonFlag = this.basePattern_pattern.get(basePattern);
        if (patternWithSkeletonFlag != null && (!patternWithSkeletonFlag.skeletonWasSpecified || (str2 != null && !z))) {
            patternInfo.status = 1;
            patternInfo.conflictingPattern = patternWithSkeletonFlag.pattern;
            if (!z) {
                return this;
            }
        }
        PatternWithSkeletonFlag patternWithSkeletonFlag2 = this.skeleton2pattern.get(dateTimeMatcher);
        if (patternWithSkeletonFlag2 != null) {
            patternInfo.status = 2;
            patternInfo.conflictingPattern = patternWithSkeletonFlag2.pattern;
            if (!z || (str2 != null && patternWithSkeletonFlag2.skeletonWasSpecified)) {
                return this;
            }
        }
        patternInfo.status = 0;
        patternInfo.conflictingPattern = "";
        if (str2 != null) {
            z2 = true;
        }
        PatternWithSkeletonFlag patternWithSkeletonFlag3 = new PatternWithSkeletonFlag(str, z2);
        this.skeleton2pattern.put(dateTimeMatcher, patternWithSkeletonFlag3);
        this.basePattern_pattern.put(basePattern, patternWithSkeletonFlag3);
        return this;
    }

    public String getSkeleton(String str) {
        String dateTimeMatcher;
        synchronized (this) {
            this.current.set(str, this.fp, false);
            dateTimeMatcher = this.current.toString();
        }
        return dateTimeMatcher;
    }

    @Deprecated
    public String getSkeletonAllowingDuplicates(String str) {
        String dateTimeMatcher;
        synchronized (this) {
            this.current.set(str, this.fp, true);
            dateTimeMatcher = this.current.toString();
        }
        return dateTimeMatcher;
    }

    @Deprecated
    public String getCanonicalSkeletonAllowingDuplicates(String str) {
        String canonicalString;
        synchronized (this) {
            this.current.set(str, this.fp, true);
            canonicalString = this.current.toCanonicalString();
        }
        return canonicalString;
    }

    public String getBaseSkeleton(String str) {
        String basePattern;
        synchronized (this) {
            this.current.set(str, this.fp, false);
            basePattern = this.current.getBasePattern();
        }
        return basePattern;
    }

    public Map<String, String> getSkeletons(Map<String, String> map) {
        if (map == null) {
            map = new LinkedHashMap<>();
        }
        for (DateTimeMatcher dateTimeMatcher : this.skeleton2pattern.keySet()) {
            String str = this.skeleton2pattern.get(dateTimeMatcher).pattern;
            if (!CANONICAL_SET.contains(str)) {
                map.put(dateTimeMatcher.toString(), str);
            }
        }
        return map;
    }

    public Set<String> getBaseSkeletons(Set<String> set) {
        if (set == null) {
            set = new HashSet<>();
        }
        set.addAll(this.basePattern_pattern.keySet());
        return set;
    }

    public String replaceFieldTypes(String str, String str2) {
        return replaceFieldTypes(str, str2, 0);
    }

    public String replaceFieldTypes(String str, String str2, int i) {
        String adjustFieldTypes;
        synchronized (this) {
            adjustFieldTypes = adjustFieldTypes(new PatternWithMatcher(str, null), this.current.set(str2, this.fp, false), EnumSet.noneOf(DTPGflags.class), i);
        }
        return adjustFieldTypes;
    }

    public void setDateTimeFormat(String str) {
        checkFrozen();
        this.dateTimeFormat = str;
    }

    public String getDateTimeFormat() {
        return this.dateTimeFormat;
    }

    public void setDecimal(String str) {
        checkFrozen();
        this.decimal = str;
    }

    public String getDecimal() {
        return this.decimal;
    }

    @Deprecated
    public Collection<String> getRedundants(Collection<String> collection) {
        synchronized (this) {
            if (collection == null) {
                collection = new LinkedHashSet<>();
            }
            for (DateTimeMatcher dateTimeMatcher : this.skeleton2pattern.keySet()) {
                String str = this.skeleton2pattern.get(dateTimeMatcher).pattern;
                if (!CANONICAL_SET.contains(str)) {
                    if (getBestPattern(dateTimeMatcher.toString(), dateTimeMatcher, 0).equals(str)) {
                        collection.add(str);
                    }
                }
            }
        }
        return collection;
    }

    public enum DisplayWidth {
        WIDE(""),
        ABBREVIATED("-short"),
        NARROW("-narrow");
        
        @Deprecated
        private static int COUNT = values().length;
        private final String cldrKey;

        private DisplayWidth(String str) {
            this.cldrKey = str;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private String cldrKey() {
            return this.cldrKey;
        }
    }

    public void setAppendItemFormat(int i, String str) {
        checkFrozen();
        this.appendItemFormats[i] = str;
    }

    public String getAppendItemFormat(int i) {
        return this.appendItemFormats[i];
    }

    public void setAppendItemName(int i, String str) {
        setFieldDisplayName(i, APPENDITEM_WIDTH, str);
    }

    public String getAppendItemName(int i) {
        return getFieldDisplayName(i, APPENDITEM_WIDTH);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @Deprecated
    private void setFieldDisplayName(int i, DisplayWidth displayWidth, String str) {
        checkFrozen();
        if (i < 16 && i >= 0) {
            this.fieldDisplayNames[i][displayWidth.ordinal()] = str;
        }
    }

    public String getFieldDisplayName(int i, DisplayWidth displayWidth) {
        return (i >= 16 || i < 0) ? "" : this.fieldDisplayNames[i][displayWidth.ordinal()];
    }

    @Deprecated
    public static boolean isSingleField(String str) {
        char charAt = str.charAt(0);
        for (int i = 1; i < str.length(); i++) {
            if (str.charAt(i) != charAt) {
                return false;
            }
        }
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setAvailableFormat(String str) {
        checkFrozen();
        this.cldrAvailableFormatKeys.add(str);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isAvailableFormatSet(String str) {
        return this.cldrAvailableFormatKeys.contains(str);
    }

    public boolean isFrozen() {
        return this.frozen;
    }

    public DateTimePatternGenerator freeze() {
        this.frozen = true;
        return this;
    }

    public DateTimePatternGenerator cloneAsThawed() {
        DateTimePatternGenerator dateTimePatternGenerator = (DateTimePatternGenerator) clone();
        this.frozen = false;
        return dateTimePatternGenerator;
    }

    @Override // java.lang.Object
    public Object clone() {
        try {
            DateTimePatternGenerator dateTimePatternGenerator = (DateTimePatternGenerator) super.clone();
            dateTimePatternGenerator.skeleton2pattern = (TreeMap) this.skeleton2pattern.clone();
            dateTimePatternGenerator.basePattern_pattern = (TreeMap) this.basePattern_pattern.clone();
            dateTimePatternGenerator.appendItemFormats = (String[]) this.appendItemFormats.clone();
            dateTimePatternGenerator.fieldDisplayNames = (String[][]) this.fieldDisplayNames.clone();
            dateTimePatternGenerator.current = new DateTimeMatcher();
            dateTimePatternGenerator.fp = new FormatParser();
            dateTimePatternGenerator._distanceInfo = new DistanceInfo();
            dateTimePatternGenerator.frozen = false;
            return dateTimePatternGenerator;
        } catch (CloneNotSupportedException e) {
            throw new ICUCloneNotSupportedException("Internal Error", e);
        }
    }

    @Deprecated
    public static class VariableField {
        private final int canonicalIndex;
        private final String string;

        @Deprecated
        public VariableField(String str) {
            this(str, false);
        }

        @Deprecated
        public VariableField(String str, boolean z) {
            this.canonicalIndex = DateTimePatternGenerator.getCanonicalIndex(str, z);
            if (this.canonicalIndex >= 0) {
                this.string = str;
                return;
            }
            throw new IllegalArgumentException("Illegal datetime field:\t" + str);
        }

        @Deprecated
        public int getType() {
            return DateTimePatternGenerator.types[this.canonicalIndex][1];
        }

        @Deprecated
        public static String getCanonicalCode(int i) {
            try {
                return DateTimePatternGenerator.CANONICAL_ITEMS[i];
            } catch (Exception unused) {
                return String.valueOf(i);
            }
        }

        @Deprecated
        public boolean isNumeric() {
            return DateTimePatternGenerator.types[this.canonicalIndex][2] > 0;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private int getCanonicalIndex() {
            return this.canonicalIndex;
        }

        @Deprecated
        public String toString() {
            return this.string;
        }
    }

    @Deprecated
    public static class FormatParser {
        private static final UnicodeSet QUOTING_CHARS = new UnicodeSet("[[[:script=Latn:][:script=Cyrl:]]&[[:L:][:M:]]]").freeze();
        private static final UnicodeSet SYNTAX_CHARS = new UnicodeSet("[a-zA-Z]").freeze();
        private List<Object> items = new ArrayList();
        private transient PatternTokenizer tokenizer = new PatternTokenizer().setSyntaxCharacters(SYNTAX_CHARS).setExtraQuotingCharacters(QUOTING_CHARS).setUsingQuote(true);

        @Deprecated
        public final FormatParser set(String str) {
            return set(str, false);
        }

        @Deprecated
        public FormatParser set(String str, boolean z) {
            this.items.clear();
            if (str.length() == 0) {
                return this;
            }
            this.tokenizer.setPattern(str);
            StringBuffer stringBuffer = new StringBuffer();
            StringBuffer stringBuffer2 = new StringBuffer();
            while (true) {
                stringBuffer.setLength(0);
                int next = this.tokenizer.next(stringBuffer);
                if (next == 0) {
                    addVariable(stringBuffer2, false);
                    return this;
                } else if (next == 1) {
                    if (!(stringBuffer2.length() == 0 || stringBuffer.charAt(0) == stringBuffer2.charAt(0))) {
                        addVariable(stringBuffer2, false);
                    }
                    stringBuffer2.append(stringBuffer);
                } else {
                    addVariable(stringBuffer2, false);
                    this.items.add(stringBuffer.toString());
                }
            }
        }

        private void addVariable(StringBuffer stringBuffer, boolean z) {
            if (stringBuffer.length() != 0) {
                this.items.add(new VariableField(stringBuffer.toString(), z));
                stringBuffer.setLength(0);
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
        public String toString(int i, int i2) {
            StringBuilder sb = new StringBuilder();
            while (i < i2) {
                Object obj = this.items.get(i);
                if (obj instanceof String) {
                    sb.append(this.tokenizer.quoteLiteral((String) obj));
                } else {
                    sb.append(this.items.get(i).toString());
                }
                i++;
            }
            return sb.toString();
        }

        @Deprecated
        public boolean hasDateAndTimeFields() {
            int i = 0;
            for (Object obj : this.items) {
                if (obj instanceof VariableField) {
                    i |= 1 << ((VariableField) obj).getType();
                }
            }
            return ((i & 1023) != 0) && ((i & 64512) != 0);
        }

        @Deprecated
        public Object quoteLiteral(String str) {
            return this.tokenizer.quoteLiteral(str);
        }
    }

    @Deprecated
    public boolean skeletonsAreSimilar(String str, String str2) {
        if (str.equals(str2)) {
            return true;
        }
        TreeSet<String> set = getSet(str);
        TreeSet<String> set2 = getSet(str2);
        if (set.size() != set2.size()) {
            return false;
        }
        Iterator<String> it = set2.iterator();
        Iterator<String> it2 = set.iterator();
        while (it2.hasNext()) {
            int canonicalIndex = getCanonicalIndex(it2.next(), false);
            int canonicalIndex2 = getCanonicalIndex(it.next(), false);
            int[][] iArr = types;
            if (iArr[canonicalIndex][1] != iArr[canonicalIndex2][1]) {
                return false;
            }
        }
        return true;
    }

    private TreeSet<String> getSet(String str) {
        List<Object> items = this.fp.set(str).getItems();
        TreeSet<String> treeSet = new TreeSet<>();
        for (Object obj : items) {
            String obj2 = obj.toString();
            if (!obj2.startsWith("G") && !obj2.startsWith("a")) {
                treeSet.add(obj2);
            }
        }
        return treeSet;
    }

    /* access modifiers changed from: private */
    public static class PatternWithMatcher {
        public DateTimeMatcher matcherWithSkeleton;
        public String pattern;

        public PatternWithMatcher(String str, DateTimeMatcher dateTimeMatcher) {
            this.pattern = str;
            this.matcherWithSkeleton = dateTimeMatcher;
        }
    }

    /* access modifiers changed from: private */
    public static class PatternWithSkeletonFlag {
        public String pattern;
        public boolean skeletonWasSpecified;

        public PatternWithSkeletonFlag(String str, boolean z) {
            this.pattern = str;
            this.skeletonWasSpecified = z;
        }

        public String toString() {
            return this.pattern + "," + this.skeletonWasSpecified;
        }
    }

    private void checkFrozen() {
        if (isFrozen()) {
            throw new UnsupportedOperationException("Attempt to modify frozen object");
        }
    }

    private String getBestAppending(DateTimeMatcher dateTimeMatcher, int i, DistanceInfo distanceInfo, DateTimeMatcher dateTimeMatcher2, EnumSet<DTPGflags> enumSet, int i2) {
        if (i == 0) {
            return null;
        }
        PatternWithMatcher bestRaw = getBestRaw(dateTimeMatcher, i, distanceInfo, dateTimeMatcher2);
        String adjustFieldTypes = adjustFieldTypes(bestRaw, dateTimeMatcher, enumSet, i2);
        while (distanceInfo.missingFieldMask != 0) {
            if ((distanceInfo.missingFieldMask & SECOND_AND_FRACTIONAL_MASK) == 16384 && (i & SECOND_AND_FRACTIONAL_MASK) == SECOND_AND_FRACTIONAL_MASK) {
                bestRaw.pattern = adjustFieldTypes;
                enumSet = EnumSet.copyOf((EnumSet) enumSet);
                enumSet.add(DTPGflags.FIX_FRACTIONAL_SECONDS);
                adjustFieldTypes = adjustFieldTypes(bestRaw, dateTimeMatcher, enumSet, i2);
                distanceInfo.missingFieldMask &= -16385;
            } else {
                int i3 = distanceInfo.missingFieldMask;
                String adjustFieldTypes2 = adjustFieldTypes(getBestRaw(dateTimeMatcher, distanceInfo.missingFieldMask, distanceInfo, dateTimeMatcher2), dateTimeMatcher, enumSet, i2);
                int topBitNumber = getTopBitNumber(i3 & (~distanceInfo.missingFieldMask));
                adjustFieldTypes = SimpleFormatterImpl.formatRawPattern(getAppendFormat(topBitNumber), 2, 3, adjustFieldTypes, adjustFieldTypes2, getAppendName(topBitNumber));
            }
        }
        return adjustFieldTypes;
    }

    private String getAppendName(int i) {
        return "'" + this.fieldDisplayNames[i][APPENDITEM_WIDTH_INT] + "'";
    }

    private String getAppendFormat(int i) {
        return this.appendItemFormats[i];
    }

    private void addCanonicalItems() {
        PatternInfo patternInfo = new PatternInfo();
        int i = 0;
        while (true) {
            String[] strArr = CANONICAL_ITEMS;
            if (i < strArr.length) {
                addPattern(String.valueOf(strArr[i]), false, patternInfo);
                i++;
            } else {
                return;
            }
        }
    }

    private PatternWithMatcher getBestRaw(DateTimeMatcher dateTimeMatcher, int i, DistanceInfo distanceInfo, DateTimeMatcher dateTimeMatcher2) {
        int distance;
        PatternWithMatcher patternWithMatcher = new PatternWithMatcher("", null);
        DistanceInfo distanceInfo2 = new DistanceInfo();
        int i2 = Integer.MAX_VALUE;
        for (DateTimeMatcher dateTimeMatcher3 : this.skeleton2pattern.keySet()) {
            if (!dateTimeMatcher3.equals(dateTimeMatcher2) && (distance = dateTimeMatcher.getDistance(dateTimeMatcher3, i, distanceInfo2)) < i2) {
                PatternWithSkeletonFlag patternWithSkeletonFlag = this.skeleton2pattern.get(dateTimeMatcher3);
                patternWithMatcher.pattern = patternWithSkeletonFlag.pattern;
                if (patternWithSkeletonFlag.skeletonWasSpecified) {
                    patternWithMatcher.matcherWithSkeleton = dateTimeMatcher3;
                } else {
                    patternWithMatcher.matcherWithSkeleton = null;
                }
                distanceInfo.setTo(distanceInfo2);
                if (distance == 0) {
                    break;
                }
                i2 = distance;
            }
        }
        return patternWithMatcher;
    }

    private String adjustFieldTypes(PatternWithMatcher patternWithMatcher, DateTimeMatcher dateTimeMatcher, EnumSet<DTPGflags> enumSet, int i) {
        this.fp.set(patternWithMatcher.pattern);
        StringBuilder sb = new StringBuilder();
        for (Object obj : this.fp.getItems()) {
            if (obj instanceof String) {
                sb.append(this.fp.quoteLiteral((String) obj));
            } else {
                VariableField variableField = (VariableField) obj;
                StringBuilder sb2 = new StringBuilder(variableField.toString());
                int type = variableField.getType();
                if (enumSet.contains(DTPGflags.FIX_FRACTIONAL_SECONDS) && type == 13) {
                    sb2.append(this.decimal);
                    dateTimeMatcher.original.appendFieldTo(14, sb2);
                } else if (dateTimeMatcher.type[type] != 0) {
                    char fieldChar = dateTimeMatcher.original.getFieldChar(type);
                    int fieldLength = dateTimeMatcher.original.getFieldLength(type);
                    if (fieldChar == 'E' && fieldLength < 3) {
                        fieldLength = 3;
                    }
                    DateTimeMatcher dateTimeMatcher2 = patternWithMatcher.matcherWithSkeleton;
                    if ((type == 11 && (i & 2048) == 0) || ((type == 12 && (i & 4096) == 0) || (type == 13 && (i & 8192) == 0))) {
                        fieldLength = sb2.length();
                    } else if (dateTimeMatcher2 != null) {
                        int fieldLength2 = dateTimeMatcher2.original.getFieldLength(type);
                        boolean isNumeric = variableField.isNumeric();
                        boolean fieldIsNumeric = dateTimeMatcher2.fieldIsNumeric(type);
                        if (fieldLength2 == fieldLength || ((isNumeric && !fieldIsNumeric) || (fieldIsNumeric && !isNumeric))) {
                            fieldLength = sb2.length();
                        }
                    }
                    if (type == 11 || type == 3 || type == 6 || (type == 1 && fieldChar != 'Y')) {
                        fieldChar = sb2.charAt(0);
                    }
                    if (type == 11 && enumSet.contains(DTPGflags.SKELETON_USES_CAP_J)) {
                        fieldChar = this.defaultHourFormatChar;
                    }
                    sb2 = new StringBuilder();
                    while (fieldLength > 0) {
                        sb2.append(fieldChar);
                        fieldLength--;
                    }
                }
                sb.append((CharSequence) sb2);
            }
        }
        return sb.toString();
    }

    @Deprecated
    public String getFields(String str) {
        this.fp.set(str);
        StringBuilder sb = new StringBuilder();
        for (Object obj : this.fp.getItems()) {
            if (obj instanceof String) {
                sb.append(this.fp.quoteLiteral((String) obj));
            } else {
                sb.append("{" + getName(obj.toString()) + "}");
            }
        }
        return sb.toString();
    }

    /* access modifiers changed from: private */
    public static String showMask(int i) {
        StringBuilder sb = new StringBuilder();
        for (int i2 = 0; i2 < 16; i2++) {
            if (((1 << i2) & i) != 0) {
                if (sb.length() != 0) {
                    sb.append(" | ");
                }
                sb.append(FIELD_NAME[i2]);
                sb.append(" ");
            }
        }
        return sb.toString();
    }

    private static String getName(String str) {
        int canonicalIndex = getCanonicalIndex(str, true);
        String[] strArr = FIELD_NAME;
        int[][] iArr = types;
        String str2 = strArr[iArr[canonicalIndex][1]];
        if (iArr[canonicalIndex][2] < 0) {
            return str2 + ":S";
        }
        return str2 + ":N";
    }

    /* access modifiers changed from: private */
    public static int getCanonicalIndex(String str, boolean z) {
        int length = str.length();
        if (length == 0) {
            return -1;
        }
        char charAt = str.charAt(0);
        for (int i = 1; i < length; i++) {
            if (str.charAt(i) != charAt) {
                return -1;
            }
        }
        int i2 = -1;
        int i3 = 0;
        while (true) {
            int[][] iArr = types;
            if (i3 < iArr.length) {
                int[] iArr2 = iArr[i3];
                if (iArr2[0] == charAt) {
                    if (iArr2[3] <= length && iArr2[iArr2.length - 1] >= length) {
                        return i3;
                    }
                    i2 = i3;
                }
                i3++;
            } else if (z) {
                return -1;
            } else {
                return i2;
            }
        }
    }

    /* access modifiers changed from: private */
    public static char getCanonicalChar(int i, char c) {
        if (c == 'h' || c == 'K') {
            return 'h';
        }
        int i2 = 0;
        while (true) {
            int[][] iArr = types;
            if (i2 < iArr.length) {
                int[] iArr2 = iArr[i2];
                if (iArr2[1] == i) {
                    return (char) iArr2[0];
                }
                i2++;
            } else {
                throw new IllegalArgumentException("Could not find field " + i);
            }
        }
    }

    /* access modifiers changed from: private */
    public static class SkeletonFields {
        static final /* synthetic */ boolean $assertionsDisabled = false;
        private static final byte DEFAULT_CHAR = 0;
        private static final byte DEFAULT_LENGTH = 0;
        private byte[] chars;
        private byte[] lengths;

        private SkeletonFields() {
            this.chars = new byte[16];
            this.lengths = new byte[16];
        }

        public void clear() {
            Arrays.fill(this.chars, (byte) 0);
            Arrays.fill(this.lengths, (byte) 0);
        }

        /* access modifiers changed from: package-private */
        public void copyFieldFrom(SkeletonFields skeletonFields, int i) {
            this.chars[i] = skeletonFields.chars[i];
            this.lengths[i] = skeletonFields.lengths[i];
        }

        /* access modifiers changed from: package-private */
        public void clearField(int i) {
            this.chars[i] = 0;
            this.lengths[i] = 0;
        }

        /* access modifiers changed from: package-private */
        public char getFieldChar(int i) {
            return (char) this.chars[i];
        }

        /* access modifiers changed from: package-private */
        public int getFieldLength(int i) {
            return this.lengths[i];
        }

        /* access modifiers changed from: package-private */
        public void populate(int i, String str) {
            char[] charArray = str.toCharArray();
            for (char c : charArray) {
            }
            populate(i, str.charAt(0), str.length());
        }

        /* access modifiers changed from: package-private */
        public void populate(int i, char c, int i2) {
            this.chars[i] = (byte) c;
            this.lengths[i] = (byte) i2;
        }

        public boolean isFieldEmpty(int i) {
            return this.lengths[i] == 0;
        }

        public String toString() {
            return appendTo(new StringBuilder(), false, false).toString();
        }

        public String toString(boolean z) {
            return appendTo(new StringBuilder(), false, z).toString();
        }

        public String toCanonicalString() {
            return appendTo(new StringBuilder(), true, false).toString();
        }

        public String toCanonicalString(boolean z) {
            return appendTo(new StringBuilder(), true, z).toString();
        }

        public StringBuilder appendTo(StringBuilder sb) {
            return appendTo(sb, false, false);
        }

        private StringBuilder appendTo(StringBuilder sb, boolean z, boolean z2) {
            for (int i = 0; i < 16; i++) {
                if (!z2 || i != 10) {
                    appendFieldTo(i, sb, z);
                }
            }
            return sb;
        }

        public StringBuilder appendFieldTo(int i, StringBuilder sb) {
            return appendFieldTo(i, sb, false);
        }

        private StringBuilder appendFieldTo(int i, StringBuilder sb, boolean z) {
            char c = (char) this.chars[i];
            byte b = this.lengths[i];
            if (z) {
                c = DateTimePatternGenerator.getCanonicalChar(i, c);
            }
            for (int i2 = 0; i2 < b; i2++) {
                sb.append(c);
            }
            return sb;
        }

        public int compareTo(SkeletonFields skeletonFields) {
            for (int i = 0; i < 16; i++) {
                int i2 = this.chars[i] - skeletonFields.chars[i];
                if (i2 != 0) {
                    return i2;
                }
                int i3 = this.lengths[i] - skeletonFields.lengths[i];
                if (i3 != 0) {
                    return i3;
                }
            }
            return 0;
        }

        public boolean equals(Object obj) {
            return this == obj || (obj != null && (obj instanceof SkeletonFields) && compareTo((SkeletonFields) obj) == 0);
        }

        public int hashCode() {
            return Arrays.hashCode(this.lengths) ^ Arrays.hashCode(this.chars);
        }
    }

    /* access modifiers changed from: private */
    public static class DateTimeMatcher implements Comparable<DateTimeMatcher> {
        private boolean addedDefaultDayPeriod;
        private SkeletonFields baseOriginal;
        private SkeletonFields original;
        private int[] type;

        private DateTimeMatcher() {
            this.type = new int[16];
            this.original = new SkeletonFields();
            this.baseOriginal = new SkeletonFields();
            this.addedDefaultDayPeriod = false;
        }

        public boolean fieldIsNumeric(int i) {
            return this.type[i] > 0;
        }

        @Override // java.lang.Object
        public String toString() {
            return this.original.toString(this.addedDefaultDayPeriod);
        }

        public String toCanonicalString() {
            return this.original.toCanonicalString(this.addedDefaultDayPeriod);
        }

        /* access modifiers changed from: package-private */
        public String getBasePattern() {
            return this.baseOriginal.toString(this.addedDefaultDayPeriod);
        }

        /* access modifiers changed from: package-private */
        public DateTimeMatcher set(String str, FormatParser formatParser, boolean z) {
            Arrays.fill(this.type, 0);
            this.original.clear();
            this.baseOriginal.clear();
            this.addedDefaultDayPeriod = false;
            formatParser.set(str);
            for (Object obj : formatParser.getItems()) {
                if (obj instanceof VariableField) {
                    VariableField variableField = (VariableField) obj;
                    String variableField2 = variableField.toString();
                    int[] iArr = DateTimePatternGenerator.types[variableField.getCanonicalIndex()];
                    int i = iArr[1];
                    if (!this.original.isFieldEmpty(i)) {
                        char fieldChar = this.original.getFieldChar(i);
                        char charAt = variableField2.charAt(0);
                        if (!z && !((fieldChar == 'r' && charAt == 'U') || (fieldChar == 'U' && charAt == 'r'))) {
                            throw new IllegalArgumentException("Conflicting fields:\t" + fieldChar + ", " + variableField2 + "\t in " + str);
                        }
                    } else {
                        this.original.populate(i, variableField2);
                        char c = (char) iArr[0];
                        int i2 = iArr[3];
                        if ("GEzvQ".indexOf(c) >= 0) {
                            i2 = 1;
                        }
                        this.baseOriginal.populate(i, c, i2);
                        int i3 = iArr[2];
                        if (i3 > 0) {
                            i3 += variableField2.length();
                        }
                        this.type[i] = i3;
                    }
                }
            }
            if (!this.original.isFieldEmpty(11)) {
                if (this.original.getFieldChar(11) == 'h' || this.original.getFieldChar(11) == 'K') {
                    if (this.original.isFieldEmpty(10)) {
                        int i4 = 0;
                        while (true) {
                            if (i4 >= DateTimePatternGenerator.types.length) {
                                break;
                            }
                            int[] iArr2 = DateTimePatternGenerator.types[i4];
                            if (iArr2[1] == 10) {
                                this.original.populate(10, (char) iArr2[0], iArr2[3]);
                                this.baseOriginal.populate(10, (char) iArr2[0], iArr2[3]);
                                this.type[10] = iArr2[2];
                                this.addedDefaultDayPeriod = true;
                                break;
                            }
                            i4++;
                        }
                    }
                } else if (!this.original.isFieldEmpty(10)) {
                    this.original.clearField(10);
                    this.baseOriginal.clearField(10);
                    this.type[10] = 0;
                }
            }
            return this;
        }

        /* access modifiers changed from: package-private */
        public int getFieldMask() {
            int i = 0;
            int i2 = 0;
            while (true) {
                int[] iArr = this.type;
                if (i >= iArr.length) {
                    return i2;
                }
                if (iArr[i] != 0) {
                    i2 |= 1 << i;
                }
                i++;
            }
        }

        /* access modifiers changed from: package-private */
        public void extractFrom(DateTimeMatcher dateTimeMatcher, int i) {
            int i2 = 0;
            while (true) {
                int[] iArr = this.type;
                if (i2 < iArr.length) {
                    if (((1 << i2) & i) != 0) {
                        iArr[i2] = dateTimeMatcher.type[i2];
                        this.original.copyFieldFrom(dateTimeMatcher.original, i2);
                    } else {
                        iArr[i2] = 0;
                        this.original.clearField(i2);
                    }
                    i2++;
                } else {
                    return;
                }
            }
        }

        /* access modifiers changed from: package-private */
        public int getDistance(DateTimeMatcher dateTimeMatcher, int i, DistanceInfo distanceInfo) {
            distanceInfo.clear();
            int i2 = 0;
            for (int i3 = 0; i3 < 16; i3++) {
                int i4 = ((1 << i3) & i) == 0 ? 0 : this.type[i3];
                int i5 = dateTimeMatcher.type[i3];
                if (i4 != i5) {
                    if (i4 == 0) {
                        i2 += 65536;
                        distanceInfo.addExtra(i3);
                    } else if (i5 == 0) {
                        i2 += 4096;
                        distanceInfo.addMissing(i3);
                    } else {
                        i2 += Math.abs(i4 - i5);
                    }
                }
            }
            return i2;
        }

        public int compareTo(DateTimeMatcher dateTimeMatcher) {
            int compareTo = this.original.compareTo(dateTimeMatcher.original);
            if (compareTo > 0) {
                return -1;
            }
            return compareTo < 0 ? 1 : 0;
        }

        @Override // java.lang.Object
        public boolean equals(Object obj) {
            return this == obj || (obj != null && (obj instanceof DateTimeMatcher) && this.original.equals(((DateTimeMatcher) obj).original));
        }

        @Override // java.lang.Object
        public int hashCode() {
            return this.original.hashCode();
        }
    }

    /* access modifiers changed from: private */
    public static class DistanceInfo {
        int extraFieldMask;
        int missingFieldMask;

        private DistanceInfo() {
        }

        /* access modifiers changed from: package-private */
        public void clear() {
            this.extraFieldMask = 0;
            this.missingFieldMask = 0;
        }

        /* access modifiers changed from: package-private */
        public void setTo(DistanceInfo distanceInfo) {
            this.missingFieldMask = distanceInfo.missingFieldMask;
            this.extraFieldMask = distanceInfo.extraFieldMask;
        }

        /* access modifiers changed from: package-private */
        public void addMissing(int i) {
            this.missingFieldMask = (1 << i) | this.missingFieldMask;
        }

        /* access modifiers changed from: package-private */
        public void addExtra(int i) {
            this.extraFieldMask = (1 << i) | this.extraFieldMask;
        }

        public String toString() {
            return "missingFieldMask: " + DateTimePatternGenerator.showMask(this.missingFieldMask) + ", extraFieldMask: " + DateTimePatternGenerator.showMask(this.extraFieldMask);
        }
    }
}
