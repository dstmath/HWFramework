package android.icu.text;

import android.icu.impl.CacheBase;
import android.icu.impl.CalendarUtil;
import android.icu.impl.ICUData;
import android.icu.impl.ICUResourceBundle;
import android.icu.impl.SoftCache;
import android.icu.impl.UResource;
import android.icu.impl.Utility;
import android.icu.text.TimeZoneNames;
import android.icu.util.Calendar;
import android.icu.util.ICUCloneNotSupportedException;
import android.icu.util.ICUException;
import android.icu.util.TimeZone;
import android.icu.util.ULocale;
import android.icu.util.UResourceBundle;
import android.icu.util.UResourceBundleIterator;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeMap;

public class DateFormatSymbols implements Serializable, Cloneable {
    public static final int ABBREVIATED = 0;
    static final String ALTERNATE_TIME_SEPARATOR = ".";
    private static final String[][] CALENDAR_CLASSES = {new String[]{"GregorianCalendar", "gregorian"}, new String[]{"JapaneseCalendar", "japanese"}, new String[]{"BuddhistCalendar", "buddhist"}, new String[]{"TaiwanCalendar", "roc"}, new String[]{"PersianCalendar", "persian"}, new String[]{"IslamicCalendar", "islamic"}, new String[]{"HebrewCalendar", "hebrew"}, new String[]{"ChineseCalendar", "chinese"}, new String[]{"IndianCalendar", "indian"}, new String[]{"CopticCalendar", "coptic"}, new String[]{"EthiopicCalendar", "ethiopic"}};
    private static final String[] DAY_PERIOD_KEYS = {"midnight", "noon", "morning1", "afternoon1", "evening1", "night1", "morning2", "afternoon2", "evening2", "night2"};
    static final String DEFAULT_TIME_SEPARATOR = ":";
    private static CacheBase<String, DateFormatSymbols, ULocale> DFSCACHE = new SoftCache<String, DateFormatSymbols, ULocale>() {
        /* access modifiers changed from: protected */
        public DateFormatSymbols createInstance(String key, ULocale locale) {
            int typeStart = key.indexOf(43) + 1;
            int typeLimit = key.indexOf(43, typeStart);
            if (typeLimit < 0) {
                typeLimit = key.length();
            }
            return new DateFormatSymbols(locale, null, key.substring(typeStart, typeLimit));
        }
    };
    @Deprecated
    public static final int DT_CONTEXT_COUNT = 3;
    static final int DT_LEAP_MONTH_PATTERN_FORMAT_ABBREV = 1;
    static final int DT_LEAP_MONTH_PATTERN_FORMAT_NARROW = 2;
    static final int DT_LEAP_MONTH_PATTERN_FORMAT_WIDE = 0;
    static final int DT_LEAP_MONTH_PATTERN_NUMERIC = 6;
    static final int DT_LEAP_MONTH_PATTERN_STANDALONE_ABBREV = 4;
    static final int DT_LEAP_MONTH_PATTERN_STANDALONE_NARROW = 5;
    static final int DT_LEAP_MONTH_PATTERN_STANDALONE_WIDE = 3;
    static final int DT_MONTH_PATTERN_COUNT = 7;
    @Deprecated
    public static final int DT_WIDTH_COUNT = 4;
    public static final int FORMAT = 0;
    private static final String[] LEAP_MONTH_PATTERNS_PATHS = new String[7];
    public static final int NARROW = 2;
    @Deprecated
    public static final int NUMERIC = 2;
    public static final int SHORT = 3;
    public static final int STANDALONE = 1;
    public static final int WIDE = 1;
    private static final Map<String, CapitalizationContextUsage> contextUsageTypeMap = new HashMap();
    static final int millisPerHour = 3600000;
    static final String patternChars = "GyMdkHmsSEDFwWahKzYeugAZvcLQqVUOXxrbB";
    private static final long serialVersionUID = -5987973545549424702L;
    String[] abbreviatedDayPeriods;
    private ULocale actualLocale;
    String[] ampms;
    String[] ampmsNarrow;
    Map<CapitalizationContextUsage, boolean[]> capitalization;
    String[] eraNames;
    String[] eras;
    String[] leapMonthPatterns;
    String localPatternChars;
    String[] months;
    String[] narrowDayPeriods;
    String[] narrowEras;
    String[] narrowMonths;
    String[] narrowWeekdays;
    String[] quarters;
    private ULocale requestedLocale;
    String[] shortMonths;
    String[] shortQuarters;
    String[] shortWeekdays;
    String[] shortYearNames;
    String[] shortZodiacNames;
    String[] shorterWeekdays;
    String[] standaloneAbbreviatedDayPeriods;
    String[] standaloneMonths;
    String[] standaloneNarrowDayPeriods;
    String[] standaloneNarrowMonths;
    String[] standaloneNarrowWeekdays;
    String[] standaloneQuarters;
    String[] standaloneShortMonths;
    String[] standaloneShortQuarters;
    String[] standaloneShortWeekdays;
    String[] standaloneShorterWeekdays;
    String[] standaloneWeekdays;
    String[] standaloneWideDayPeriods;
    private String timeSeparator;
    private ULocale validLocale;
    String[] weekdays;
    String[] wideDayPeriods;
    private String[][] zoneStrings;

    private static final class CalendarDataSink extends UResource.Sink {
        static final /* synthetic */ boolean $assertionsDisabled = false;
        private static final String CALENDAR_ALIAS_PREFIX = "/LOCALE/calendar/";
        List<String> aliasPathPairs = new ArrayList();
        private String aliasRelativePath;
        Map<String, String[]> arrays = new TreeMap();
        String currentCalendarType = null;
        Map<String, Map<String, String>> maps = new TreeMap();
        String nextCalendarType = null;
        private Set<String> resourcesToVisit;

        private enum AliasType {
            SAME_CALENDAR,
            DIFFERENT_CALENDAR,
            GREGORIAN,
            NONE
        }

        static {
            Class<DateFormatSymbols> cls = DateFormatSymbols.class;
        }

        CalendarDataSink() {
        }

        /* access modifiers changed from: package-private */
        public void visitAllResources() {
            this.resourcesToVisit = null;
        }

        /* access modifiers changed from: package-private */
        public void preEnumerate(String calendarType) {
            this.currentCalendarType = calendarType;
            this.nextCalendarType = null;
            this.aliasPathPairs.clear();
        }

        public void put(UResource.Key key, UResource.Value value, boolean noFallback) {
            UResource.Table calendarData = value.getTable();
            Set<String> resourcesToVisitNext = null;
            for (int i = 0; calendarData.getKeyAndValue(i, key, value); i++) {
                String keyString = key.toString();
                AliasType aliasType = processAliasFromValue(keyString, value);
                if (aliasType != AliasType.GREGORIAN) {
                    if (aliasType == AliasType.DIFFERENT_CALENDAR) {
                        if (resourcesToVisitNext == null) {
                            resourcesToVisitNext = new HashSet<>();
                        }
                        resourcesToVisitNext.add(this.aliasRelativePath);
                    } else if (aliasType == AliasType.SAME_CALENDAR) {
                        if (!this.arrays.containsKey(keyString) && !this.maps.containsKey(keyString)) {
                            this.aliasPathPairs.add(this.aliasRelativePath);
                            this.aliasPathPairs.add(keyString);
                        }
                    } else if (this.resourcesToVisit == null || this.resourcesToVisit.isEmpty() || this.resourcesToVisit.contains(keyString) || keyString.equals("AmPmMarkersAbbr")) {
                        if (keyString.startsWith("AmPmMarkers")) {
                            if (!keyString.endsWith("%variant") && !this.arrays.containsKey(keyString)) {
                                this.arrays.put(keyString, value.getStringArray());
                            }
                        } else if (keyString.equals("eras") || keyString.equals("dayNames") || keyString.equals("monthNames") || keyString.equals("quarters") || keyString.equals("dayPeriod") || keyString.equals("monthPatterns") || keyString.equals("cyclicNameSets")) {
                            processResource(keyString, key, value);
                        }
                    }
                }
            }
            do {
                boolean modified = false;
                int i2 = 0;
                while (i2 < this.aliasPathPairs.size()) {
                    boolean mod = false;
                    String alias = this.aliasPathPairs.get(i2);
                    if (this.arrays.containsKey(alias)) {
                        this.arrays.put(this.aliasPathPairs.get(i2 + 1), this.arrays.get(alias));
                        mod = true;
                    } else if (this.maps.containsKey(alias)) {
                        this.maps.put(this.aliasPathPairs.get(i2 + 1), this.maps.get(alias));
                        mod = true;
                    }
                    if (mod) {
                        this.aliasPathPairs.remove(i2 + 1);
                        this.aliasPathPairs.remove(i2);
                        modified = true;
                    } else {
                        i2 += 2;
                    }
                }
                if (!modified) {
                    break;
                }
            } while (!this.aliasPathPairs.isEmpty());
            if (resourcesToVisitNext != null) {
                this.resourcesToVisit = resourcesToVisitNext;
            }
        }

        /* access modifiers changed from: protected */
        public void processResource(String path, UResource.Key key, UResource.Value value) {
            UResource.Table table = value.getTable();
            Map<String, String> stringMap = null;
            for (int i = 0; table.getKeyAndValue(i, key, value); i++) {
                if (!key.endsWith("%variant")) {
                    String keyString = key.toString();
                    if (value.getType() == 0) {
                        if (i == 0) {
                            stringMap = new HashMap<>();
                            this.maps.put(path, stringMap);
                        }
                        stringMap.put(keyString, value.getString());
                    } else {
                        String currentPath = path + "/" + keyString;
                        if ((!currentPath.startsWith("cyclicNameSets") || "cyclicNameSets/years/format/abbreviated".startsWith(currentPath) || "cyclicNameSets/zodiacs/format/abbreviated".startsWith(currentPath) || "cyclicNameSets/dayParts/format/abbreviated".startsWith(currentPath)) && !this.arrays.containsKey(currentPath) && !this.maps.containsKey(currentPath)) {
                            if (processAliasFromValue(currentPath, value) == AliasType.SAME_CALENDAR) {
                                this.aliasPathPairs.add(this.aliasRelativePath);
                                this.aliasPathPairs.add(currentPath);
                            } else if (value.getType() == 8) {
                                this.arrays.put(currentPath, value.getStringArray());
                            } else if (value.getType() == 2) {
                                processResource(currentPath, key, value);
                            }
                        }
                    }
                }
            }
        }

        private AliasType processAliasFromValue(String currentRelativePath, UResource.Value value) {
            if (value.getType() != 3) {
                return AliasType.NONE;
            }
            String aliasPath = value.getAliasString();
            if (aliasPath.startsWith(CALENDAR_ALIAS_PREFIX) && aliasPath.length() > CALENDAR_ALIAS_PREFIX.length()) {
                int typeLimit = aliasPath.indexOf(47, CALENDAR_ALIAS_PREFIX.length());
                if (typeLimit > CALENDAR_ALIAS_PREFIX.length()) {
                    String aliasCalendarType = aliasPath.substring(CALENDAR_ALIAS_PREFIX.length(), typeLimit);
                    this.aliasRelativePath = aliasPath.substring(typeLimit + 1);
                    if (this.currentCalendarType.equals(aliasCalendarType) && !currentRelativePath.equals(this.aliasRelativePath)) {
                        return AliasType.SAME_CALENDAR;
                    }
                    if (!this.currentCalendarType.equals(aliasCalendarType) && currentRelativePath.equals(this.aliasRelativePath)) {
                        if (aliasCalendarType.equals("gregorian")) {
                            return AliasType.GREGORIAN;
                        }
                        if (this.nextCalendarType == null || this.nextCalendarType.equals(aliasCalendarType)) {
                            this.nextCalendarType = aliasCalendarType;
                            return AliasType.DIFFERENT_CALENDAR;
                        }
                    }
                }
            }
            throw new ICUException("Malformed 'calendar' alias. Path: " + aliasPath);
        }
    }

    enum CapitalizationContextUsage {
        OTHER,
        MONTH_FORMAT,
        MONTH_STANDALONE,
        MONTH_NARROW,
        DAY_FORMAT,
        DAY_STANDALONE,
        DAY_NARROW,
        ERA_WIDE,
        ERA_ABBREV,
        ERA_NARROW,
        ZONE_LONG,
        ZONE_SHORT,
        METAZONE_LONG,
        METAZONE_SHORT
    }

    public DateFormatSymbols() {
        this(ULocale.getDefault(ULocale.Category.FORMAT));
    }

    public DateFormatSymbols(Locale locale) {
        this(ULocale.forLocale(locale));
    }

    public DateFormatSymbols(ULocale locale) {
        this.eras = null;
        this.eraNames = null;
        this.narrowEras = null;
        this.months = null;
        this.shortMonths = null;
        this.narrowMonths = null;
        this.standaloneMonths = null;
        this.standaloneShortMonths = null;
        this.standaloneNarrowMonths = null;
        this.weekdays = null;
        this.shortWeekdays = null;
        this.shorterWeekdays = null;
        this.narrowWeekdays = null;
        this.standaloneWeekdays = null;
        this.standaloneShortWeekdays = null;
        this.standaloneShorterWeekdays = null;
        this.standaloneNarrowWeekdays = null;
        this.ampms = null;
        this.ampmsNarrow = null;
        this.timeSeparator = null;
        this.shortQuarters = null;
        this.quarters = null;
        this.standaloneShortQuarters = null;
        this.standaloneQuarters = null;
        this.leapMonthPatterns = null;
        this.shortYearNames = null;
        this.shortZodiacNames = null;
        this.zoneStrings = null;
        this.localPatternChars = null;
        this.abbreviatedDayPeriods = null;
        this.wideDayPeriods = null;
        this.narrowDayPeriods = null;
        this.standaloneAbbreviatedDayPeriods = null;
        this.standaloneWideDayPeriods = null;
        this.standaloneNarrowDayPeriods = null;
        this.capitalization = null;
        initializeData(locale, CalendarUtil.getCalendarType(locale));
    }

    public static DateFormatSymbols getInstance() {
        return new DateFormatSymbols();
    }

    public static DateFormatSymbols getInstance(Locale locale) {
        return new DateFormatSymbols(locale);
    }

    public static DateFormatSymbols getInstance(ULocale locale) {
        return new DateFormatSymbols(locale);
    }

    public static Locale[] getAvailableLocales() {
        return ICUResourceBundle.getAvailableLocales();
    }

    public static ULocale[] getAvailableULocales() {
        return ICUResourceBundle.getAvailableULocales();
    }

    static {
        contextUsageTypeMap.put("month-format-except-narrow", CapitalizationContextUsage.MONTH_FORMAT);
        contextUsageTypeMap.put("month-standalone-except-narrow", CapitalizationContextUsage.MONTH_STANDALONE);
        contextUsageTypeMap.put("month-narrow", CapitalizationContextUsage.MONTH_NARROW);
        contextUsageTypeMap.put("day-format-except-narrow", CapitalizationContextUsage.DAY_FORMAT);
        contextUsageTypeMap.put("day-standalone-except-narrow", CapitalizationContextUsage.DAY_STANDALONE);
        contextUsageTypeMap.put("day-narrow", CapitalizationContextUsage.DAY_NARROW);
        contextUsageTypeMap.put("era-name", CapitalizationContextUsage.ERA_WIDE);
        contextUsageTypeMap.put("era-abbr", CapitalizationContextUsage.ERA_ABBREV);
        contextUsageTypeMap.put("era-narrow", CapitalizationContextUsage.ERA_NARROW);
        contextUsageTypeMap.put("zone-long", CapitalizationContextUsage.ZONE_LONG);
        contextUsageTypeMap.put("zone-short", CapitalizationContextUsage.ZONE_SHORT);
        contextUsageTypeMap.put("metazone-long", CapitalizationContextUsage.METAZONE_LONG);
        contextUsageTypeMap.put("metazone-short", CapitalizationContextUsage.METAZONE_SHORT);
        LEAP_MONTH_PATTERNS_PATHS[0] = "monthPatterns/format/wide";
        LEAP_MONTH_PATTERNS_PATHS[1] = "monthPatterns/format/abbreviated";
        LEAP_MONTH_PATTERNS_PATHS[2] = "monthPatterns/format/narrow";
        LEAP_MONTH_PATTERNS_PATHS[3] = "monthPatterns/stand-alone/wide";
        LEAP_MONTH_PATTERNS_PATHS[4] = "monthPatterns/stand-alone/abbreviated";
        LEAP_MONTH_PATTERNS_PATHS[5] = "monthPatterns/stand-alone/narrow";
        LEAP_MONTH_PATTERNS_PATHS[6] = "monthPatterns/numeric/all";
    }

    public String[] getEras() {
        return duplicate(this.eras);
    }

    public void setEras(String[] newEras) {
        this.eras = duplicate(newEras);
    }

    public String[] getEraNames() {
        return duplicate(this.eraNames);
    }

    public void setEraNames(String[] newEraNames) {
        this.eraNames = duplicate(newEraNames);
    }

    @Deprecated
    public String[] getNarrowEras() {
        return duplicate(this.narrowEras);
    }

    public String[] getMonths() {
        return duplicate(this.months);
    }

    public String[] getMonths(int context, int width) {
        String[] returnValue = null;
        switch (context) {
            case 0:
                switch (width) {
                    case 0:
                    case 3:
                        returnValue = this.shortMonths;
                        break;
                    case 1:
                        returnValue = this.months;
                        break;
                    case 2:
                        returnValue = this.narrowMonths;
                        break;
                }
            case 1:
                switch (width) {
                    case 0:
                    case 3:
                        returnValue = this.standaloneShortMonths;
                        break;
                    case 1:
                        returnValue = this.standaloneMonths;
                        break;
                    case 2:
                        returnValue = this.standaloneNarrowMonths;
                        break;
                }
        }
        if (returnValue != null) {
            return duplicate(returnValue);
        }
        throw new IllegalArgumentException("Bad context or width argument");
    }

    public void setMonths(String[] newMonths) {
        this.months = duplicate(newMonths);
    }

    public void setMonths(String[] newMonths, int context, int width) {
        switch (context) {
            case 0:
                switch (width) {
                    case 0:
                        this.shortMonths = duplicate(newMonths);
                        return;
                    case 1:
                        this.months = duplicate(newMonths);
                        return;
                    case 2:
                        this.narrowMonths = duplicate(newMonths);
                        return;
                    default:
                        return;
                }
            case 1:
                switch (width) {
                    case 0:
                        this.standaloneShortMonths = duplicate(newMonths);
                        return;
                    case 1:
                        this.standaloneMonths = duplicate(newMonths);
                        return;
                    case 2:
                        this.standaloneNarrowMonths = duplicate(newMonths);
                        return;
                    default:
                        return;
                }
            default:
                return;
        }
    }

    public String[] getShortMonths() {
        return duplicate(this.shortMonths);
    }

    public void setShortMonths(String[] newShortMonths) {
        this.shortMonths = duplicate(newShortMonths);
    }

    public String[] getWeekdays() {
        return duplicate(this.weekdays);
    }

    public String[] getWeekdays(int context, int width) {
        String[] returnValue = null;
        switch (context) {
            case 0:
                switch (width) {
                    case 0:
                        returnValue = this.shortWeekdays;
                        break;
                    case 1:
                        returnValue = this.weekdays;
                        break;
                    case 2:
                        returnValue = this.narrowWeekdays;
                        break;
                    case 3:
                        returnValue = this.shorterWeekdays != null ? this.shorterWeekdays : this.shortWeekdays;
                        break;
                }
            case 1:
                switch (width) {
                    case 0:
                        returnValue = this.standaloneShortWeekdays;
                        break;
                    case 1:
                        returnValue = this.standaloneWeekdays;
                        break;
                    case 2:
                        returnValue = this.standaloneNarrowWeekdays;
                        break;
                    case 3:
                        returnValue = this.standaloneShorterWeekdays != null ? this.standaloneShorterWeekdays : this.standaloneShortWeekdays;
                        break;
                }
        }
        if (returnValue != null) {
            return duplicate(returnValue);
        }
        throw new IllegalArgumentException("Bad context or width argument");
    }

    public void setWeekdays(String[] newWeekdays, int context, int width) {
        switch (context) {
            case 0:
                switch (width) {
                    case 0:
                        this.shortWeekdays = duplicate(newWeekdays);
                        return;
                    case 1:
                        this.weekdays = duplicate(newWeekdays);
                        return;
                    case 2:
                        this.narrowWeekdays = duplicate(newWeekdays);
                        return;
                    case 3:
                        this.shorterWeekdays = duplicate(newWeekdays);
                        return;
                    default:
                        return;
                }
            case 1:
                switch (width) {
                    case 0:
                        this.standaloneShortWeekdays = duplicate(newWeekdays);
                        return;
                    case 1:
                        this.standaloneWeekdays = duplicate(newWeekdays);
                        return;
                    case 2:
                        this.standaloneNarrowWeekdays = duplicate(newWeekdays);
                        return;
                    case 3:
                        this.standaloneShorterWeekdays = duplicate(newWeekdays);
                        return;
                    default:
                        return;
                }
            default:
                return;
        }
    }

    public void setWeekdays(String[] newWeekdays) {
        this.weekdays = duplicate(newWeekdays);
    }

    public String[] getShortWeekdays() {
        return duplicate(this.shortWeekdays);
    }

    public void setShortWeekdays(String[] newAbbrevWeekdays) {
        this.shortWeekdays = duplicate(newAbbrevWeekdays);
    }

    public String[] getQuarters(int context, int width) {
        String[] returnValue = null;
        switch (context) {
            case 0:
                switch (width) {
                    case 0:
                    case 3:
                        returnValue = this.shortQuarters;
                        break;
                    case 1:
                        returnValue = this.quarters;
                        break;
                    case 2:
                        returnValue = null;
                        break;
                }
            case 1:
                switch (width) {
                    case 0:
                    case 3:
                        returnValue = this.standaloneShortQuarters;
                        break;
                    case 1:
                        returnValue = this.standaloneQuarters;
                        break;
                    case 2:
                        returnValue = null;
                        break;
                }
        }
        if (returnValue != null) {
            return duplicate(returnValue);
        }
        throw new IllegalArgumentException("Bad context or width argument");
    }

    public void setQuarters(String[] newQuarters, int context, int width) {
        switch (context) {
            case 0:
                switch (width) {
                    case 0:
                        this.shortQuarters = duplicate(newQuarters);
                        return;
                    case 1:
                        this.quarters = duplicate(newQuarters);
                        return;
                    default:
                        return;
                }
            case 1:
                switch (width) {
                    case 0:
                        this.standaloneShortQuarters = duplicate(newQuarters);
                        return;
                    case 1:
                        this.standaloneQuarters = duplicate(newQuarters);
                        return;
                    default:
                        return;
                }
            default:
                return;
        }
    }

    public String[] getYearNames(int context, int width) {
        if (this.shortYearNames != null) {
            return duplicate(this.shortYearNames);
        }
        return null;
    }

    public void setYearNames(String[] yearNames, int context, int width) {
        if (context == 0 && width == 0) {
            this.shortYearNames = duplicate(yearNames);
        }
    }

    public String[] getZodiacNames(int context, int width) {
        if (this.shortZodiacNames != null) {
            return duplicate(this.shortZodiacNames);
        }
        return null;
    }

    public void setZodiacNames(String[] zodiacNames, int context, int width) {
        if (context == 0 && width == 0) {
            this.shortZodiacNames = duplicate(zodiacNames);
        }
    }

    @Deprecated
    public String getLeapMonthPattern(int context, int width) {
        if (this.leapMonthPatterns == null) {
            return null;
        }
        int leapMonthPatternIndex = -1;
        switch (context) {
            case 0:
                switch (width) {
                    case 0:
                    case 3:
                        leapMonthPatternIndex = 1;
                        break;
                    case 1:
                        leapMonthPatternIndex = 0;
                        break;
                    case 2:
                        leapMonthPatternIndex = 2;
                        break;
                }
            case 1:
                switch (width) {
                    case 0:
                    case 3:
                        leapMonthPatternIndex = 1;
                        break;
                    case 1:
                        leapMonthPatternIndex = 3;
                        break;
                    case 2:
                        leapMonthPatternIndex = 5;
                        break;
                }
            case 2:
                leapMonthPatternIndex = 6;
                break;
        }
        if (leapMonthPatternIndex >= 0) {
            return this.leapMonthPatterns[leapMonthPatternIndex];
        }
        throw new IllegalArgumentException("Bad context or width argument");
    }

    @Deprecated
    public void setLeapMonthPattern(String leapMonthPattern, int context, int width) {
        if (this.leapMonthPatterns != null) {
            int leapMonthPatternIndex = -1;
            switch (context) {
                case 0:
                    switch (width) {
                        case 0:
                            leapMonthPatternIndex = 1;
                            break;
                        case 1:
                            leapMonthPatternIndex = 0;
                            break;
                        case 2:
                            leapMonthPatternIndex = 2;
                            break;
                    }
                case 1:
                    switch (width) {
                        case 0:
                            leapMonthPatternIndex = 1;
                            break;
                        case 1:
                            leapMonthPatternIndex = 3;
                            break;
                        case 2:
                            leapMonthPatternIndex = 5;
                            break;
                    }
                case 2:
                    leapMonthPatternIndex = 6;
                    break;
            }
            if (leapMonthPatternIndex >= 0) {
                this.leapMonthPatterns[leapMonthPatternIndex] = leapMonthPattern;
            }
        }
    }

    public String[] getAmPmStrings() {
        return duplicate(this.ampms);
    }

    public void setAmPmStrings(String[] newAmpms) {
        this.ampms = duplicate(newAmpms);
    }

    @Deprecated
    public String getTimeSeparatorString() {
        return this.timeSeparator;
    }

    @Deprecated
    public void setTimeSeparatorString(String newTimeSeparator) {
        this.timeSeparator = newTimeSeparator;
    }

    public String[][] getZoneStrings() {
        if (this.zoneStrings != null) {
            return duplicate(this.zoneStrings);
        }
        String[] tzIDs = TimeZone.getAvailableIDs();
        TimeZoneNames tznames = TimeZoneNames.getInstance(this.validLocale);
        tznames.loadAllDisplayNames();
        TimeZoneNames.NameType[] types = {TimeZoneNames.NameType.LONG_STANDARD, TimeZoneNames.NameType.SHORT_STANDARD, TimeZoneNames.NameType.LONG_DAYLIGHT, TimeZoneNames.NameType.SHORT_DAYLIGHT};
        long now = System.currentTimeMillis();
        String[][] array = (String[][]) Array.newInstance(String.class, new int[]{tzIDs.length, 5});
        int i = 0;
        while (true) {
            int i2 = i;
            if (i2 < tzIDs.length) {
                String canonicalID = TimeZone.getCanonicalID(tzIDs[i2]);
                if (canonicalID == null) {
                    canonicalID = tzIDs[i2];
                }
                array[i2][0] = tzIDs[i2];
                tznames.getDisplayNames(canonicalID, types, now, array[i2], 1);
                i = i2 + 1;
            } else {
                this.zoneStrings = array;
                return this.zoneStrings;
            }
        }
    }

    public void setZoneStrings(String[][] newZoneStrings) {
        this.zoneStrings = duplicate(newZoneStrings);
    }

    public String getLocalPatternChars() {
        return this.localPatternChars;
    }

    public void setLocalPatternChars(String newLocalPatternChars) {
        this.localPatternChars = newLocalPatternChars;
    }

    public Object clone() {
        try {
            return (DateFormatSymbols) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new ICUCloneNotSupportedException((Throwable) e);
        }
    }

    public int hashCode() {
        return this.requestedLocale.toString().hashCode();
    }

    public boolean equals(Object obj) {
        boolean z = true;
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        DateFormatSymbols that = (DateFormatSymbols) obj;
        if (!Utility.arrayEquals((Object[]) this.eras, (Object) that.eras) || !Utility.arrayEquals((Object[]) this.eraNames, (Object) that.eraNames) || !Utility.arrayEquals((Object[]) this.months, (Object) that.months) || !Utility.arrayEquals((Object[]) this.shortMonths, (Object) that.shortMonths) || !Utility.arrayEquals((Object[]) this.narrowMonths, (Object) that.narrowMonths) || !Utility.arrayEquals((Object[]) this.standaloneMonths, (Object) that.standaloneMonths) || !Utility.arrayEquals((Object[]) this.standaloneShortMonths, (Object) that.standaloneShortMonths) || !Utility.arrayEquals((Object[]) this.standaloneNarrowMonths, (Object) that.standaloneNarrowMonths) || !Utility.arrayEquals((Object[]) this.weekdays, (Object) that.weekdays) || !Utility.arrayEquals((Object[]) this.shortWeekdays, (Object) that.shortWeekdays) || !Utility.arrayEquals((Object[]) this.shorterWeekdays, (Object) that.shorterWeekdays) || !Utility.arrayEquals((Object[]) this.narrowWeekdays, (Object) that.narrowWeekdays) || !Utility.arrayEquals((Object[]) this.standaloneWeekdays, (Object) that.standaloneWeekdays) || !Utility.arrayEquals((Object[]) this.standaloneShortWeekdays, (Object) that.standaloneShortWeekdays) || !Utility.arrayEquals((Object[]) this.standaloneShorterWeekdays, (Object) that.standaloneShorterWeekdays) || !Utility.arrayEquals((Object[]) this.standaloneNarrowWeekdays, (Object) that.standaloneNarrowWeekdays) || !Utility.arrayEquals((Object[]) this.ampms, (Object) that.ampms) || !Utility.arrayEquals((Object[]) this.ampmsNarrow, (Object) that.ampmsNarrow) || !Utility.arrayEquals((Object[]) this.abbreviatedDayPeriods, (Object) that.abbreviatedDayPeriods) || !Utility.arrayEquals((Object[]) this.wideDayPeriods, (Object) that.wideDayPeriods) || !Utility.arrayEquals((Object[]) this.narrowDayPeriods, (Object) that.narrowDayPeriods) || !Utility.arrayEquals((Object[]) this.standaloneAbbreviatedDayPeriods, (Object) that.standaloneAbbreviatedDayPeriods) || !Utility.arrayEquals((Object[]) this.standaloneWideDayPeriods, (Object) that.standaloneWideDayPeriods) || !Utility.arrayEquals((Object[]) this.standaloneNarrowDayPeriods, (Object) that.standaloneNarrowDayPeriods) || !Utility.arrayEquals((Object) this.timeSeparator, (Object) that.timeSeparator) || !arrayOfArrayEquals(this.zoneStrings, that.zoneStrings) || !this.requestedLocale.getDisplayName().equals(that.requestedLocale.getDisplayName()) || !Utility.arrayEquals((Object) this.localPatternChars, (Object) that.localPatternChars)) {
            z = false;
        }
        return z;
    }

    /* access modifiers changed from: protected */
    public void initializeData(ULocale desiredLocale, String type) {
        String key = desiredLocale.getBaseName() + '+' + type;
        String ns = desiredLocale.getKeywordValue("numbers");
        if (ns != null && ns.length() > 0) {
            key = key + '+' + ns;
        }
        initializeData(DFSCACHE.getInstance(key, desiredLocale));
    }

    /* access modifiers changed from: package-private */
    public void initializeData(DateFormatSymbols dfs) {
        this.eras = dfs.eras;
        this.eraNames = dfs.eraNames;
        this.narrowEras = dfs.narrowEras;
        this.months = dfs.months;
        this.shortMonths = dfs.shortMonths;
        this.narrowMonths = dfs.narrowMonths;
        this.standaloneMonths = dfs.standaloneMonths;
        this.standaloneShortMonths = dfs.standaloneShortMonths;
        this.standaloneNarrowMonths = dfs.standaloneNarrowMonths;
        this.weekdays = dfs.weekdays;
        this.shortWeekdays = dfs.shortWeekdays;
        this.shorterWeekdays = dfs.shorterWeekdays;
        this.narrowWeekdays = dfs.narrowWeekdays;
        this.standaloneWeekdays = dfs.standaloneWeekdays;
        this.standaloneShortWeekdays = dfs.standaloneShortWeekdays;
        this.standaloneShorterWeekdays = dfs.standaloneShorterWeekdays;
        this.standaloneNarrowWeekdays = dfs.standaloneNarrowWeekdays;
        this.ampms = dfs.ampms;
        this.ampmsNarrow = dfs.ampmsNarrow;
        this.timeSeparator = dfs.timeSeparator;
        this.shortQuarters = dfs.shortQuarters;
        this.quarters = dfs.quarters;
        this.standaloneShortQuarters = dfs.standaloneShortQuarters;
        this.standaloneQuarters = dfs.standaloneQuarters;
        this.leapMonthPatterns = dfs.leapMonthPatterns;
        this.shortYearNames = dfs.shortYearNames;
        this.shortZodiacNames = dfs.shortZodiacNames;
        this.abbreviatedDayPeriods = dfs.abbreviatedDayPeriods;
        this.wideDayPeriods = dfs.wideDayPeriods;
        this.narrowDayPeriods = dfs.narrowDayPeriods;
        this.standaloneAbbreviatedDayPeriods = dfs.standaloneAbbreviatedDayPeriods;
        this.standaloneWideDayPeriods = dfs.standaloneWideDayPeriods;
        this.standaloneNarrowDayPeriods = dfs.standaloneNarrowDayPeriods;
        this.zoneStrings = dfs.zoneStrings;
        this.localPatternChars = dfs.localPatternChars;
        this.capitalization = dfs.capitalization;
        this.actualLocale = dfs.actualLocale;
        this.validLocale = dfs.validLocale;
        this.requestedLocale = dfs.requestedLocale;
    }

    private DateFormatSymbols(ULocale desiredLocale, ICUResourceBundle b, String calendarType) {
        this.eras = null;
        this.eraNames = null;
        this.narrowEras = null;
        this.months = null;
        this.shortMonths = null;
        this.narrowMonths = null;
        this.standaloneMonths = null;
        this.standaloneShortMonths = null;
        this.standaloneNarrowMonths = null;
        this.weekdays = null;
        this.shortWeekdays = null;
        this.shorterWeekdays = null;
        this.narrowWeekdays = null;
        this.standaloneWeekdays = null;
        this.standaloneShortWeekdays = null;
        this.standaloneShorterWeekdays = null;
        this.standaloneNarrowWeekdays = null;
        this.ampms = null;
        this.ampmsNarrow = null;
        this.timeSeparator = null;
        this.shortQuarters = null;
        this.quarters = null;
        this.standaloneShortQuarters = null;
        this.standaloneQuarters = null;
        this.leapMonthPatterns = null;
        this.shortYearNames = null;
        this.shortZodiacNames = null;
        this.zoneStrings = null;
        this.localPatternChars = null;
        this.abbreviatedDayPeriods = null;
        this.wideDayPeriods = null;
        this.narrowDayPeriods = null;
        this.standaloneAbbreviatedDayPeriods = null;
        this.standaloneWideDayPeriods = null;
        this.standaloneNarrowDayPeriods = null;
        this.capitalization = null;
        initializeData(desiredLocale, b, calendarType);
    }

    /* JADX WARNING: type inference failed for: r4v32, types: [android.icu.util.UResourceBundle] */
    /* access modifiers changed from: protected */
    /* JADX WARNING: Multi-variable type inference failed */
    @Deprecated
    public void initializeData(ULocale desiredLocale, ICUResourceBundle b, String calendarType) {
        ICUResourceBundle b2;
        String calendarType2;
        ICUResourceBundle iCUResourceBundle;
        String[] snWeekdays;
        String calendarType3;
        ULocale uLocale = desiredLocale;
        CalendarDataSink calendarSink = new CalendarDataSink();
        if (b == null) {
            calendarType2 = calendarType;
            b2 = UResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME, uLocale);
        } else {
            b2 = b;
            calendarType2 = calendarType;
        }
        while (calendarType2 != null) {
            ICUResourceBundle dataForType = b2.findWithFallback("calendar/" + calendarType2);
            if (dataForType != null) {
                calendarSink.preEnumerate(calendarType2);
                dataForType.getAllItemsWithFallback("", calendarSink);
                if (calendarType2.equals("gregorian")) {
                    break;
                }
                calendarType3 = calendarSink.nextCalendarType;
                if (calendarType3 == null) {
                    calendarType3 = "gregorian";
                    calendarSink.visitAllResources();
                }
            } else if (!"gregorian".equals(calendarType2)) {
                calendarType3 = "gregorian";
                calendarSink.visitAllResources();
            } else {
                throw new MissingResourceException("The 'gregorian' calendar type wasn't found for the locale: " + desiredLocale.getBaseName(), getClass().getName(), "gregorian");
            }
        }
        Map<String, String[]> arrays = calendarSink.arrays;
        Map<String, Map<String, String>> maps = calendarSink.maps;
        this.eras = arrays.get("eras/abbreviated");
        this.eraNames = arrays.get("eras/wide");
        this.narrowEras = arrays.get("eras/narrow");
        this.months = arrays.get("monthNames/format/wide");
        this.shortMonths = arrays.get("monthNames/format/abbreviated");
        this.narrowMonths = arrays.get("monthNames/format/narrow");
        this.standaloneMonths = arrays.get("monthNames/stand-alone/wide");
        this.standaloneShortMonths = arrays.get("monthNames/stand-alone/abbreviated");
        this.standaloneNarrowMonths = arrays.get("monthNames/stand-alone/narrow");
        String[] lWeekdays = arrays.get("dayNames/format/wide");
        this.weekdays = new String[8];
        this.weekdays[0] = "";
        System.arraycopy(lWeekdays, 0, this.weekdays, 1, lWeekdays.length);
        String[] aWeekdays = arrays.get("dayNames/format/abbreviated");
        this.shortWeekdays = new String[8];
        this.shortWeekdays[0] = "";
        System.arraycopy(aWeekdays, 0, this.shortWeekdays, 1, aWeekdays.length);
        String[] sWeekdays = arrays.get("dayNames/format/short");
        this.shorterWeekdays = new String[8];
        this.shorterWeekdays[0] = "";
        System.arraycopy(sWeekdays, 0, this.shorterWeekdays, 1, sWeekdays.length);
        String[] nWeekdays = arrays.get("dayNames/format/narrow");
        if (nWeekdays == null) {
            nWeekdays = arrays.get("dayNames/stand-alone/narrow");
            if (nWeekdays == null) {
                nWeekdays = arrays.get("dayNames/format/abbreviated");
                if (nWeekdays == null) {
                    throw new MissingResourceException("Resource not found", getClass().getName(), "dayNames/format/abbreviated");
                }
            }
        }
        this.narrowWeekdays = new String[8];
        this.narrowWeekdays[0] = "";
        System.arraycopy(nWeekdays, 0, this.narrowWeekdays, 1, nWeekdays.length);
        String[] swWeekdays = arrays.get("dayNames/stand-alone/wide");
        this.standaloneWeekdays = new String[8];
        this.standaloneWeekdays[0] = "";
        System.arraycopy(swWeekdays, 0, this.standaloneWeekdays, 1, swWeekdays.length);
        String[] saWeekdays = arrays.get("dayNames/stand-alone/abbreviated");
        this.standaloneShortWeekdays = new String[8];
        this.standaloneShortWeekdays[0] = "";
        CalendarDataSink calendarDataSink = calendarSink;
        System.arraycopy(saWeekdays, 0, this.standaloneShortWeekdays, 1, saWeekdays.length);
        String[] ssWeekdays = arrays.get("dayNames/stand-alone/short");
        this.standaloneShorterWeekdays = new String[8];
        this.standaloneShorterWeekdays[0] = "";
        String str = calendarType2;
        System.arraycopy(ssWeekdays, 0, this.standaloneShorterWeekdays, 1, ssWeekdays.length);
        String[] snWeekdays2 = arrays.get("dayNames/stand-alone/narrow");
        this.standaloneNarrowWeekdays = new String[8];
        this.standaloneNarrowWeekdays[0] = "";
        String[] strArr = ssWeekdays;
        System.arraycopy(snWeekdays2, 0, this.standaloneNarrowWeekdays, 1, snWeekdays2.length);
        this.ampms = arrays.get("AmPmMarkers");
        this.ampmsNarrow = arrays.get("AmPmMarkersNarrow");
        this.quarters = arrays.get("quarters/format/wide");
        this.shortQuarters = arrays.get("quarters/format/abbreviated");
        this.standaloneQuarters = arrays.get("quarters/stand-alone/wide");
        this.standaloneShortQuarters = arrays.get("quarters/stand-alone/abbreviated");
        this.abbreviatedDayPeriods = loadDayPeriodStrings(maps.get("dayPeriod/format/abbreviated"));
        this.wideDayPeriods = loadDayPeriodStrings(maps.get("dayPeriod/format/wide"));
        this.narrowDayPeriods = loadDayPeriodStrings(maps.get("dayPeriod/format/narrow"));
        this.standaloneAbbreviatedDayPeriods = loadDayPeriodStrings(maps.get("dayPeriod/stand-alone/abbreviated"));
        this.standaloneWideDayPeriods = loadDayPeriodStrings(maps.get("dayPeriod/stand-alone/wide"));
        this.standaloneNarrowDayPeriods = loadDayPeriodStrings(maps.get("dayPeriod/stand-alone/narrow"));
        int i = 0;
        while (i < 7) {
            String monthPatternPath = LEAP_MONTH_PATTERNS_PATHS[i];
            if (monthPatternPath != null) {
                Map<String, String> monthPatternMap = maps.get(monthPatternPath);
                if (monthPatternMap != null) {
                    String leapMonthPattern = monthPatternMap.get("leap");
                    if (leapMonthPattern != null) {
                        snWeekdays = snWeekdays2;
                        if (this.leapMonthPatterns == null) {
                            this.leapMonthPatterns = new String[7];
                        }
                        this.leapMonthPatterns[i] = leapMonthPattern;
                        i++;
                        snWeekdays2 = snWeekdays;
                    }
                }
            }
            snWeekdays = snWeekdays2;
            i++;
            snWeekdays2 = snWeekdays;
        }
        this.shortYearNames = arrays.get("cyclicNameSets/years/format/abbreviated");
        this.shortZodiacNames = arrays.get("cyclicNameSets/zodiacs/format/abbreviated");
        this.requestedLocale = uLocale;
        ICUResourceBundle rb = (ICUResourceBundle) UResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME, uLocale);
        this.localPatternChars = patternChars;
        ULocale uloc = rb.getULocale();
        setLocale(uloc, uloc);
        this.capitalization = new HashMap();
        boolean[] noTransforms = {false, false};
        CapitalizationContextUsage[] allUsages = CapitalizationContextUsage.values();
        int length = allUsages.length;
        ULocale uLocale2 = uloc;
        int i2 = 0;
        while (i2 < length) {
            this.capitalization.put(allUsages[i2], noTransforms);
            i2++;
            length = length;
            b2 = b2;
        }
        try {
            iCUResourceBundle = rb.getWithFallback("contextTransforms");
        } catch (MissingResourceException e) {
            iCUResourceBundle = null;
        }
        UResourceBundle contextTransformsBundle = iCUResourceBundle;
        if (contextTransformsBundle != null) {
            UResourceBundleIterator ctIterator = contextTransformsBundle.getIterator();
            while (ctIterator.hasNext()) {
                UResourceBundle contextTransformUsage = ctIterator.next();
                UResourceBundleIterator ctIterator2 = ctIterator;
                int[] intVector = contextTransformUsage.getIntVector();
                UResourceBundle contextTransformsBundle2 = contextTransformsBundle;
                Map<String, String[]> arrays2 = arrays;
                if (intVector.length >= 2) {
                    String usageKey = contextTransformUsage.getKey();
                    CapitalizationContextUsage usage = contextUsageTypeMap.get(usageKey);
                    if (usage != null) {
                        String str2 = usageKey;
                        UResourceBundle uResourceBundle = contextTransformUsage;
                        boolean[] transforms = new boolean[2];
                        transforms[0] = intVector[0] != 0;
                        transforms[1] = intVector[1] != 0;
                        this.capitalization.put(usage, transforms);
                        ctIterator = ctIterator2;
                        contextTransformsBundle = contextTransformsBundle2;
                        arrays = arrays2;
                    }
                }
                ctIterator = ctIterator2;
                contextTransformsBundle = contextTransformsBundle2;
                arrays = arrays2;
            }
        }
        Map<String, String[]> map = arrays;
        NumberingSystem ns = NumberingSystem.getInstance(desiredLocale);
        String nsName = ns == null ? "latn" : ns.getName();
        try {
            setTimeSeparatorString(rb.getStringWithFallback("NumberElements/" + nsName + "/symbols/timeSeparator"));
        } catch (MissingResourceException e2) {
            MissingResourceException missingResourceException = e2;
            setTimeSeparatorString(DEFAULT_TIME_SEPARATOR);
        }
    }

    private static final boolean arrayOfArrayEquals(Object[][] aa1, Object[][] aa2) {
        if (aa1 == aa2) {
            return true;
        }
        if (aa1 == null || aa2 == null || aa1.length != aa2.length) {
            return false;
        }
        boolean equal = true;
        for (int i = 0; i < aa1.length; i++) {
            equal = Utility.arrayEquals(aa1[i], (Object) aa2[i]);
            if (!equal) {
                break;
            }
        }
        return equal;
    }

    private String[] loadDayPeriodStrings(Map<String, String> resourceMap) {
        String[] strings = new String[DAY_PERIOD_KEYS.length];
        if (resourceMap != null) {
            for (int i = 0; i < DAY_PERIOD_KEYS.length; i++) {
                strings[i] = resourceMap.get(DAY_PERIOD_KEYS[i]);
            }
        }
        return strings;
    }

    private final String[] duplicate(String[] srcArray) {
        return (String[]) srcArray.clone();
    }

    private final String[][] duplicate(String[][] srcArray) {
        String[][] aCopy = new String[srcArray.length][];
        for (int i = 0; i < srcArray.length; i++) {
            aCopy[i] = duplicate(srcArray[i]);
        }
        return aCopy;
    }

    public DateFormatSymbols(Calendar cal, Locale locale) {
        this.eras = null;
        this.eraNames = null;
        this.narrowEras = null;
        this.months = null;
        this.shortMonths = null;
        this.narrowMonths = null;
        this.standaloneMonths = null;
        this.standaloneShortMonths = null;
        this.standaloneNarrowMonths = null;
        this.weekdays = null;
        this.shortWeekdays = null;
        this.shorterWeekdays = null;
        this.narrowWeekdays = null;
        this.standaloneWeekdays = null;
        this.standaloneShortWeekdays = null;
        this.standaloneShorterWeekdays = null;
        this.standaloneNarrowWeekdays = null;
        this.ampms = null;
        this.ampmsNarrow = null;
        this.timeSeparator = null;
        this.shortQuarters = null;
        this.quarters = null;
        this.standaloneShortQuarters = null;
        this.standaloneQuarters = null;
        this.leapMonthPatterns = null;
        this.shortYearNames = null;
        this.shortZodiacNames = null;
        this.zoneStrings = null;
        this.localPatternChars = null;
        this.abbreviatedDayPeriods = null;
        this.wideDayPeriods = null;
        this.narrowDayPeriods = null;
        this.standaloneAbbreviatedDayPeriods = null;
        this.standaloneWideDayPeriods = null;
        this.standaloneNarrowDayPeriods = null;
        this.capitalization = null;
        initializeData(ULocale.forLocale(locale), cal.getType());
    }

    public DateFormatSymbols(Calendar cal, ULocale locale) {
        this.eras = null;
        this.eraNames = null;
        this.narrowEras = null;
        this.months = null;
        this.shortMonths = null;
        this.narrowMonths = null;
        this.standaloneMonths = null;
        this.standaloneShortMonths = null;
        this.standaloneNarrowMonths = null;
        this.weekdays = null;
        this.shortWeekdays = null;
        this.shorterWeekdays = null;
        this.narrowWeekdays = null;
        this.standaloneWeekdays = null;
        this.standaloneShortWeekdays = null;
        this.standaloneShorterWeekdays = null;
        this.standaloneNarrowWeekdays = null;
        this.ampms = null;
        this.ampmsNarrow = null;
        this.timeSeparator = null;
        this.shortQuarters = null;
        this.quarters = null;
        this.standaloneShortQuarters = null;
        this.standaloneQuarters = null;
        this.leapMonthPatterns = null;
        this.shortYearNames = null;
        this.shortZodiacNames = null;
        this.zoneStrings = null;
        this.localPatternChars = null;
        this.abbreviatedDayPeriods = null;
        this.wideDayPeriods = null;
        this.narrowDayPeriods = null;
        this.standaloneAbbreviatedDayPeriods = null;
        this.standaloneWideDayPeriods = null;
        this.standaloneNarrowDayPeriods = null;
        this.capitalization = null;
        initializeData(locale, cal.getType());
    }

    public DateFormatSymbols(Class<? extends Calendar> calendarClass, Locale locale) {
        this(calendarClass, ULocale.forLocale(locale));
    }

    public DateFormatSymbols(Class<? extends Calendar> calendarClass, ULocale locale) {
        this.eras = null;
        this.eraNames = null;
        this.narrowEras = null;
        this.months = null;
        this.shortMonths = null;
        this.narrowMonths = null;
        this.standaloneMonths = null;
        this.standaloneShortMonths = null;
        this.standaloneNarrowMonths = null;
        this.weekdays = null;
        this.shortWeekdays = null;
        this.shorterWeekdays = null;
        this.narrowWeekdays = null;
        this.standaloneWeekdays = null;
        this.standaloneShortWeekdays = null;
        this.standaloneShorterWeekdays = null;
        this.standaloneNarrowWeekdays = null;
        this.ampms = null;
        this.ampmsNarrow = null;
        this.timeSeparator = null;
        this.shortQuarters = null;
        this.quarters = null;
        this.standaloneShortQuarters = null;
        this.standaloneQuarters = null;
        this.leapMonthPatterns = null;
        this.shortYearNames = null;
        this.shortZodiacNames = null;
        this.zoneStrings = null;
        this.localPatternChars = null;
        this.abbreviatedDayPeriods = null;
        this.wideDayPeriods = null;
        this.narrowDayPeriods = null;
        this.standaloneAbbreviatedDayPeriods = null;
        this.standaloneWideDayPeriods = null;
        this.standaloneNarrowDayPeriods = null;
        this.capitalization = null;
        String fullName = calendarClass.getName();
        String className = fullName.substring(fullName.lastIndexOf(46) + 1);
        String calType = null;
        String[][] strArr = CALENDAR_CLASSES;
        int length = strArr.length;
        int i = 0;
        while (true) {
            if (i >= length) {
                break;
            }
            String[] calClassInfo = strArr[i];
            if (calClassInfo[0].equals(className)) {
                calType = calClassInfo[1];
                break;
            }
            i++;
        }
        initializeData(locale, calType == null ? className.replaceAll("Calendar", "").toLowerCase(Locale.ENGLISH) : calType);
    }

    @Deprecated
    public DateFormatSymbols(ULocale locale, String calType) {
        this.eras = null;
        this.eraNames = null;
        this.narrowEras = null;
        this.months = null;
        this.shortMonths = null;
        this.narrowMonths = null;
        this.standaloneMonths = null;
        this.standaloneShortMonths = null;
        this.standaloneNarrowMonths = null;
        this.weekdays = null;
        this.shortWeekdays = null;
        this.shorterWeekdays = null;
        this.narrowWeekdays = null;
        this.standaloneWeekdays = null;
        this.standaloneShortWeekdays = null;
        this.standaloneShorterWeekdays = null;
        this.standaloneNarrowWeekdays = null;
        this.ampms = null;
        this.ampmsNarrow = null;
        this.timeSeparator = null;
        this.shortQuarters = null;
        this.quarters = null;
        this.standaloneShortQuarters = null;
        this.standaloneQuarters = null;
        this.leapMonthPatterns = null;
        this.shortYearNames = null;
        this.shortZodiacNames = null;
        this.zoneStrings = null;
        this.localPatternChars = null;
        this.abbreviatedDayPeriods = null;
        this.wideDayPeriods = null;
        this.narrowDayPeriods = null;
        this.standaloneAbbreviatedDayPeriods = null;
        this.standaloneWideDayPeriods = null;
        this.standaloneNarrowDayPeriods = null;
        this.capitalization = null;
        initializeData(locale, calType);
    }

    public DateFormatSymbols(ResourceBundle bundle, Locale locale) {
        this(bundle, ULocale.forLocale(locale));
    }

    public DateFormatSymbols(ResourceBundle bundle, ULocale locale) {
        this.eras = null;
        this.eraNames = null;
        this.narrowEras = null;
        this.months = null;
        this.shortMonths = null;
        this.narrowMonths = null;
        this.standaloneMonths = null;
        this.standaloneShortMonths = null;
        this.standaloneNarrowMonths = null;
        this.weekdays = null;
        this.shortWeekdays = null;
        this.shorterWeekdays = null;
        this.narrowWeekdays = null;
        this.standaloneWeekdays = null;
        this.standaloneShortWeekdays = null;
        this.standaloneShorterWeekdays = null;
        this.standaloneNarrowWeekdays = null;
        this.ampms = null;
        this.ampmsNarrow = null;
        this.timeSeparator = null;
        this.shortQuarters = null;
        this.quarters = null;
        this.standaloneShortQuarters = null;
        this.standaloneQuarters = null;
        this.leapMonthPatterns = null;
        this.shortYearNames = null;
        this.shortZodiacNames = null;
        this.zoneStrings = null;
        this.localPatternChars = null;
        this.abbreviatedDayPeriods = null;
        this.wideDayPeriods = null;
        this.narrowDayPeriods = null;
        this.standaloneAbbreviatedDayPeriods = null;
        this.standaloneWideDayPeriods = null;
        this.standaloneNarrowDayPeriods = null;
        this.capitalization = null;
        initializeData(locale, (ICUResourceBundle) bundle, CalendarUtil.getCalendarType(locale));
    }

    @Deprecated
    public static ResourceBundle getDateFormatBundle(Class<? extends Calendar> cls, Locale locale) throws MissingResourceException {
        return null;
    }

    @Deprecated
    public static ResourceBundle getDateFormatBundle(Class<? extends Calendar> cls, ULocale locale) throws MissingResourceException {
        return null;
    }

    @Deprecated
    public static ResourceBundle getDateFormatBundle(Calendar cal, Locale locale) throws MissingResourceException {
        return null;
    }

    @Deprecated
    public static ResourceBundle getDateFormatBundle(Calendar cal, ULocale locale) throws MissingResourceException {
        return null;
    }

    public final ULocale getLocale(ULocale.Type type) {
        return type == ULocale.ACTUAL_LOCALE ? this.actualLocale : this.validLocale;
    }

    /* access modifiers changed from: package-private */
    public final void setLocale(ULocale valid, ULocale actual) {
        boolean z = false;
        boolean z2 = valid == null;
        if (actual == null) {
            z = true;
        }
        if (z2 == z) {
            this.validLocale = valid;
            this.actualLocale = actual;
            return;
        }
        throw new IllegalArgumentException();
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
    }
}
