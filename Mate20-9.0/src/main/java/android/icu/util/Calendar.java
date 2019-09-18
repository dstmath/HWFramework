package android.icu.util;

import android.icu.impl.CalendarUtil;
import android.icu.impl.ICUCache;
import android.icu.impl.ICUData;
import android.icu.impl.ICUResourceBundle;
import android.icu.impl.SimpleCache;
import android.icu.impl.SimpleFormatterImpl;
import android.icu.impl.SoftCache;
import android.icu.impl.locale.BaseLocale;
import android.icu.lang.UCharacter;
import android.icu.text.DateFormat;
import android.icu.text.DateFormatSymbols;
import android.icu.text.SimpleDateFormat;
import android.icu.util.ULocale;
import dalvik.system.VMRuntime;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.MissingResourceException;
import libcore.icu.RelativeDateTimeFormatter;

public abstract class Calendar implements Serializable, Cloneable, Comparable<Calendar> {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    public static final int AM = 0;
    public static final int AM_PM = 9;
    public static final int APRIL = 3;
    public static final int AUGUST = 7;
    @Deprecated
    protected static final int BASE_FIELD_COUNT = 23;
    public static final int DATE = 5;
    static final int[][][] DATE_PRECEDENCE = {new int[][]{new int[]{5}, new int[]{3, 7}, new int[]{4, 7}, new int[]{8, 7}, new int[]{3, 18}, new int[]{4, 18}, new int[]{8, 18}, new int[]{6}, new int[]{37, 1}, new int[]{35, 17}}, new int[][]{new int[]{3}, new int[]{4}, new int[]{8}, new int[]{40, 7}, new int[]{40, 18}}};
    public static final int DAY_OF_MONTH = 5;
    public static final int DAY_OF_WEEK = 7;
    public static final int DAY_OF_WEEK_IN_MONTH = 8;
    public static final int DAY_OF_YEAR = 6;
    public static final int DECEMBER = 11;
    /* access modifiers changed from: private */
    public static final String[] DEFAULT_PATTERNS = {"HH:mm:ss z", "HH:mm:ss z", "HH:mm:ss", "HH:mm", "EEEE, yyyy MMMM dd", "yyyy MMMM d", "yyyy MMM d", "yy/MM/dd", "{1} {0}", "{1} {0}", "{1} {0}", "{1} {0}", "{1} {0}"};
    public static final int DOW_LOCAL = 18;
    static final int[][][] DOW_PRECEDENCE = {new int[][]{new int[]{7}, new int[]{18}}};
    public static final int DST_OFFSET = 16;
    protected static final int EPOCH_JULIAN_DAY = 2440588;
    public static final int ERA = 0;
    public static final int EXTENDED_YEAR = 19;
    public static final int FEBRUARY = 1;
    private static final int FIELD_DIFF_MAX_INT = Integer.MAX_VALUE;
    private static final String[] FIELD_NAME = {"ERA", "YEAR", "MONTH", "WEEK_OF_YEAR", "WEEK_OF_MONTH", "DAY_OF_MONTH", "DAY_OF_YEAR", "DAY_OF_WEEK", "DAY_OF_WEEK_IN_MONTH", "AM_PM", "HOUR", "HOUR_OF_DAY", "MINUTE", "SECOND", "MILLISECOND", "ZONE_OFFSET", "DST_OFFSET", "YEAR_WOY", "DOW_LOCAL", "EXTENDED_YEAR", "JULIAN_DAY", "MILLISECONDS_IN_DAY"};
    private static final int[] FIND_ZONE_TRANSITION_TIME_UNITS = {3600000, 1800000, 60000, 1000};
    public static final int FRIDAY = 6;
    protected static final int GREATEST_MINIMUM = 1;
    private static final int[][] GREGORIAN_MONTH_COUNT = {new int[]{31, 31, 0, 0}, new int[]{28, 29, 31, 31}, new int[]{31, 31, 59, 60}, new int[]{30, 30, 90, 91}, new int[]{31, 31, 120, 121}, new int[]{30, 30, 151, 152}, new int[]{31, 31, 181, 182}, new int[]{31, 31, 212, 213}, new int[]{30, 30, 243, 244}, new int[]{31, 31, UCharacter.UnicodeBlock.TANGUT_COMPONENTS_ID, UCharacter.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_F_ID}, new int[]{30, 30, 304, 305}, new int[]{31, 31, 334, 335}};
    public static final int HOUR = 10;
    public static final int HOUR_OF_DAY = 11;
    protected static final int INTERNALLY_SET = 1;
    public static final int IS_LEAP_MONTH = 22;
    public static final int JANUARY = 0;
    protected static final int JAN_1_1_JULIAN_DAY = 1721426;
    public static final int JULIAN_DAY = 20;
    public static final int JULY = 6;
    public static final int JUNE = 5;
    protected static final int LEAST_MAXIMUM = 2;
    private static final int[][] LIMITS = {new int[0], new int[0], new int[0], new int[0], new int[0], new int[0], new int[0], new int[]{1, 1, 7, 7}, new int[0], new int[]{0, 0, 1, 1}, new int[]{0, 0, 11, 11}, new int[]{0, 0, 23, 23}, new int[]{0, 0, 59, 59}, new int[]{0, 0, 59, 59}, new int[]{0, 0, 999, 999}, new int[]{-43200000, -43200000, 43200000, 43200000}, new int[]{0, 0, 3600000, 3600000}, new int[0], new int[]{1, 1, 7, 7}, new int[0], new int[]{MIN_JULIAN, MIN_JULIAN, 2130706432, 2130706432}, new int[]{0, 0, 86399999, 86399999}, new int[]{0, 0, 1, 1}};
    public static final int MARCH = 2;
    protected static final int MAXIMUM = 3;
    protected static final Date MAX_DATE = new Date(183882168921600000L);
    @Deprecated
    protected static final int MAX_FIELD_COUNT = 32;
    private static final int MAX_HOURS = 548;
    protected static final int MAX_JULIAN = 2130706432;
    protected static final long MAX_MILLIS = 183882168921600000L;
    public static final int MAY = 4;
    public static final int MILLISECOND = 14;
    public static final int MILLISECONDS_IN_DAY = 21;
    protected static final int MINIMUM = 0;
    protected static final int MINIMUM_USER_STAMP = 2;
    public static final int MINUTE = 12;
    protected static final Date MIN_DATE = new Date(-184303902528000000L);
    protected static final int MIN_JULIAN = -2130706432;
    protected static final long MIN_MILLIS = -184303902528000000L;
    public static final int MONDAY = 2;
    public static final int MONTH = 2;
    public static final int NOVEMBER = 10;
    public static final int OCTOBER = 9;
    protected static final long ONE_DAY = 86400000;
    protected static final int ONE_HOUR = 3600000;
    protected static final int ONE_MINUTE = 60000;
    protected static final int ONE_SECOND = 1000;
    protected static final long ONE_WEEK = 604800000;
    /* access modifiers changed from: private */
    public static final ICUCache<String, PatternData> PATTERN_CACHE = new SimpleCache();
    public static final int PM = 1;
    private static final char QUOTE = '\'';
    protected static final int RESOLVE_REMAP = 32;
    public static final int SATURDAY = 7;
    public static final int SECOND = 13;
    public static final int SEPTEMBER = 8;
    private static int STAMP_MAX = VMRuntime.SDK_VERSION_CUR_DEVELOPMENT;
    public static final int SUNDAY = 1;
    public static final int THURSDAY = 5;
    public static final int TUESDAY = 3;
    public static final int UNDECIMBER = 12;
    protected static final int UNSET = 0;
    public static final int WALLTIME_FIRST = 1;
    public static final int WALLTIME_LAST = 0;
    public static final int WALLTIME_NEXT_VALID = 2;
    public static final int WEDNESDAY = 4;
    @Deprecated
    public static final int WEEKDAY = 0;
    @Deprecated
    public static final int WEEKEND = 1;
    @Deprecated
    public static final int WEEKEND_CEASE = 3;
    @Deprecated
    public static final int WEEKEND_ONSET = 2;
    private static final WeekDataCache WEEK_DATA_CACHE = new WeekDataCache();
    public static final int WEEK_OF_MONTH = 4;
    public static final int WEEK_OF_YEAR = 3;
    public static final int YEAR = 1;
    public static final int YEAR_WOY = 17;
    public static final int ZONE_OFFSET = 15;
    private static final long serialVersionUID = 6222646104888790989L;
    private ULocale actualLocale;
    private transient boolean areAllFieldsSet;
    private transient boolean areFieldsSet;
    private transient boolean areFieldsVirtuallySet;
    private transient int[] fields;
    private int firstDayOfWeek;
    private transient int gregorianDayOfMonth;
    private transient int gregorianDayOfYear;
    private transient int gregorianMonth;
    private transient int gregorianYear;
    private transient int internalSetMask;
    private transient boolean isTimeSet;
    private boolean lenient;
    private int minimalDaysInFirstWeek;
    private transient int nextStamp;
    private int repeatedWallTime;
    private int skippedWallTime;
    private transient int[] stamp;
    private long time;
    private ULocale validLocale;
    private int weekendCease;
    private int weekendCeaseMillis;
    private int weekendOnset;
    private int weekendOnsetMillis;
    private TimeZone zone;

    private enum CalType {
        GREGORIAN("gregorian"),
        ISO8601("iso8601"),
        BUDDHIST("buddhist"),
        CHINESE("chinese"),
        COPTIC("coptic"),
        DANGI("dangi"),
        ETHIOPIC("ethiopic"),
        ETHIOPIC_AMETE_ALEM("ethiopic-amete-alem"),
        HEBREW("hebrew"),
        INDIAN("indian"),
        ISLAMIC("islamic"),
        ISLAMIC_CIVIL("islamic-civil"),
        ISLAMIC_RGSA("islamic-rgsa"),
        ISLAMIC_TBLA("islamic-tbla"),
        ISLAMIC_UMALQURA("islamic-umalqura"),
        JAPANESE("japanese"),
        PERSIAN("persian"),
        ROC("roc"),
        UNKNOWN("unknown");
        
        String id;

        private CalType(String id2) {
            this.id = id2;
        }
    }

    @Deprecated
    public static class FormatConfiguration {
        /* access modifiers changed from: private */
        public Calendar cal;
        /* access modifiers changed from: private */
        public DateFormatSymbols formatData;
        /* access modifiers changed from: private */
        public ULocale loc;
        /* access modifiers changed from: private */
        public String override;
        /* access modifiers changed from: private */
        public String pattern;

        private FormatConfiguration() {
        }

        @Deprecated
        public String getPatternString() {
            return this.pattern;
        }

        @Deprecated
        public String getOverrideString() {
            return this.override;
        }

        @Deprecated
        public Calendar getCalendar() {
            return this.cal;
        }

        @Deprecated
        public ULocale getLocale() {
            return this.loc;
        }

        @Deprecated
        public DateFormatSymbols getDateFormatSymbols() {
            return this.formatData;
        }
    }

    static class PatternData {
        /* access modifiers changed from: private */
        public String[] overrides;
        /* access modifiers changed from: private */
        public String[] patterns;

        public PatternData(String[] patterns2, String[] overrides2) {
            this.patterns = patterns2;
            this.overrides = overrides2;
        }

        /* access modifiers changed from: private */
        public String getDateTimePattern(int dateStyle) {
            int glueIndex = 8;
            if (this.patterns.length >= 13) {
                glueIndex = 8 + dateStyle + 1;
            }
            return this.patterns[glueIndex];
        }

        /* access modifiers changed from: private */
        public static PatternData make(Calendar cal, ULocale loc) {
            return make(loc, cal.getType());
        }

        /* access modifiers changed from: private */
        public static PatternData make(ULocale loc, String calType) {
            String key = loc.getBaseName() + "+" + calType;
            PatternData patternData = (PatternData) Calendar.PATTERN_CACHE.get(key);
            if (patternData == null) {
                try {
                    patternData = Calendar.getPatternData(loc, calType);
                } catch (MissingResourceException e) {
                    patternData = new PatternData(Calendar.DEFAULT_PATTERNS, null);
                }
                Calendar.PATTERN_CACHE.put(key, patternData);
            }
            return patternData;
        }
    }

    public static final class WeekData {
        public final int firstDayOfWeek;
        public final int minimalDaysInFirstWeek;
        public final int weekendCease;
        public final int weekendCeaseMillis;
        public final int weekendOnset;
        public final int weekendOnsetMillis;

        public WeekData(int fdow, int mdifw, int weekendOnset2, int weekendOnsetMillis2, int weekendCease2, int weekendCeaseMillis2) {
            this.firstDayOfWeek = fdow;
            this.minimalDaysInFirstWeek = mdifw;
            this.weekendOnset = weekendOnset2;
            this.weekendOnsetMillis = weekendOnsetMillis2;
            this.weekendCease = weekendCease2;
            this.weekendCeaseMillis = weekendCeaseMillis2;
        }

        public int hashCode() {
            return (((((((((this.firstDayOfWeek * 37) + this.minimalDaysInFirstWeek) * 37) + this.weekendOnset) * 37) + this.weekendOnsetMillis) * 37) + this.weekendCease) * 37) + this.weekendCeaseMillis;
        }

        public boolean equals(Object other) {
            boolean z = true;
            if (this == other) {
                return true;
            }
            if (!(other instanceof WeekData)) {
                return false;
            }
            WeekData that = (WeekData) other;
            if (!(this.firstDayOfWeek == that.firstDayOfWeek && this.minimalDaysInFirstWeek == that.minimalDaysInFirstWeek && this.weekendOnset == that.weekendOnset && this.weekendOnsetMillis == that.weekendOnsetMillis && this.weekendCease == that.weekendCease && this.weekendCeaseMillis == that.weekendCeaseMillis)) {
                z = false;
            }
            return z;
        }

        public String toString() {
            return "{" + this.firstDayOfWeek + ", " + this.minimalDaysInFirstWeek + ", " + this.weekendOnset + ", " + this.weekendOnsetMillis + ", " + this.weekendCease + ", " + this.weekendCeaseMillis + "}";
        }
    }

    private static class WeekDataCache extends SoftCache<String, WeekData, String> {
        private WeekDataCache() {
        }

        /* access modifiers changed from: protected */
        public WeekData createInstance(String key, String data) {
            return Calendar.getWeekDataForRegionInternal(key);
        }
    }

    /* access modifiers changed from: protected */
    public abstract int handleComputeMonthStart(int i, int i2, boolean z);

    /* access modifiers changed from: protected */
    public abstract int handleGetExtendedYear();

    /* access modifiers changed from: protected */
    public abstract int handleGetLimit(int i, int i2);

    protected Calendar() {
        this(TimeZone.getDefault(), ULocale.getDefault(ULocale.Category.FORMAT));
    }

    protected Calendar(TimeZone zone2, Locale aLocale) {
        this(zone2, ULocale.forLocale(aLocale));
    }

    protected Calendar(TimeZone zone2, ULocale locale) {
        this.lenient = true;
        this.repeatedWallTime = 0;
        this.skippedWallTime = 0;
        this.nextStamp = 2;
        this.zone = zone2;
        setWeekData(getRegionForCalendar(locale));
        setCalendarLocale(locale);
        initInternal();
    }

    private void setCalendarLocale(ULocale locale) {
        ULocale calLocale = locale;
        if (!(locale.getVariant().length() == 0 && locale.getKeywords() == null)) {
            StringBuilder buf = new StringBuilder();
            buf.append(locale.getLanguage());
            String script = locale.getScript();
            if (script.length() > 0) {
                buf.append(BaseLocale.SEP);
                buf.append(script);
            }
            String region = locale.getCountry();
            if (region.length() > 0) {
                buf.append(BaseLocale.SEP);
                buf.append(region);
            }
            String calType = locale.getKeywordValue("calendar");
            if (calType != null) {
                buf.append("@calendar=");
                buf.append(calType);
            }
            calLocale = new ULocale(buf.toString());
        }
        setLocale(calLocale, calLocale);
    }

    private void recalculateStamp() {
        this.nextStamp = 1;
        for (int j = 0; j < this.stamp.length; j++) {
            int index = -1;
            int currentValue = STAMP_MAX;
            for (int i = 0; i < this.stamp.length; i++) {
                if (this.stamp[i] > this.nextStamp && this.stamp[i] < currentValue) {
                    currentValue = this.stamp[i];
                    index = i;
                }
            }
            if (index < 0) {
                break;
            }
            int[] iArr = this.stamp;
            int i2 = this.nextStamp + 1;
            this.nextStamp = i2;
            iArr[index] = i2;
        }
        this.nextStamp++;
    }

    private void initInternal() {
        this.fields = handleCreateFields();
        if (this.fields != null) {
            if (this.fields.length >= 23 && this.fields.length <= 32) {
                this.stamp = new int[this.fields.length];
                int mask = 4718695;
                for (int i = 23; i < this.fields.length; i++) {
                    mask |= 1 << i;
                }
                this.internalSetMask = mask;
                return;
            }
        }
        throw new IllegalStateException("Invalid fields[]");
    }

    public static Calendar getInstance() {
        return getInstanceInternal(null, null);
    }

    public static Calendar getInstance(TimeZone zone2) {
        return getInstanceInternal(zone2, null);
    }

    public static Calendar getInstance(Locale aLocale) {
        return getInstanceInternal(null, ULocale.forLocale(aLocale));
    }

    public static Calendar getInstance(ULocale locale) {
        return getInstanceInternal(null, locale);
    }

    public static Calendar getInstance(TimeZone zone2, Locale aLocale) {
        return getInstanceInternal(zone2, ULocale.forLocale(aLocale));
    }

    public static Calendar getInstance(TimeZone zone2, ULocale locale) {
        return getInstanceInternal(zone2, locale);
    }

    private static Calendar getInstanceInternal(TimeZone tz, ULocale locale) {
        if (locale == null) {
            locale = ULocale.getDefault(ULocale.Category.FORMAT);
        }
        if (tz == null) {
            tz = TimeZone.getDefault();
        }
        Calendar cal = createInstance(locale);
        cal.setTimeZone(tz);
        cal.setTimeInMillis(System.currentTimeMillis());
        return cal;
    }

    private static String getRegionForCalendar(ULocale loc) {
        String region = ULocale.getRegionForSupplementalData(loc, true);
        if (region.length() == 0) {
            return "001";
        }
        return region;
    }

    private static CalType getCalendarTypeForLocale(ULocale l) {
        String s = CalendarUtil.getCalendarType(l);
        if (s != null) {
            String s2 = s.toLowerCase(Locale.ENGLISH);
            for (CalType type : CalType.values()) {
                if (s2.equals(type.id)) {
                    return type;
                }
            }
        }
        return CalType.UNKNOWN;
    }

    private static Calendar createInstance(ULocale locale) {
        TimeZone zone2 = TimeZone.getDefault();
        CalType calType = getCalendarTypeForLocale(locale);
        if (calType == CalType.UNKNOWN) {
            calType = CalType.GREGORIAN;
        }
        switch (calType) {
            case GREGORIAN:
                return new GregorianCalendar(zone2, locale);
            case ISO8601:
                Calendar cal = new GregorianCalendar(zone2, locale);
                cal.setFirstDayOfWeek(2);
                cal.setMinimalDaysInFirstWeek(4);
                return cal;
            case BUDDHIST:
                return new BuddhistCalendar(zone2, locale);
            case CHINESE:
                return new ChineseCalendar(zone2, locale);
            case COPTIC:
                return new CopticCalendar(zone2, locale);
            case DANGI:
                return new DangiCalendar(zone2, locale);
            case ETHIOPIC:
                return new EthiopicCalendar(zone2, locale);
            case ETHIOPIC_AMETE_ALEM:
                Calendar cal2 = new EthiopicCalendar(zone2, locale);
                ((EthiopicCalendar) cal2).setAmeteAlemEra(true);
                return cal2;
            case HEBREW:
                return new HebrewCalendar(zone2, locale);
            case INDIAN:
                return new IndianCalendar(zone2, locale);
            case ISLAMIC_CIVIL:
            case ISLAMIC_UMALQURA:
            case ISLAMIC_TBLA:
            case ISLAMIC_RGSA:
            case ISLAMIC:
                return new IslamicCalendar(zone2, locale);
            case JAPANESE:
                return new JapaneseCalendar(zone2, locale);
            case PERSIAN:
                return new PersianCalendar(zone2, locale);
            case ROC:
                return new TaiwanCalendar(zone2, locale);
            default:
                throw new IllegalArgumentException("Unknown calendar type");
        }
    }

    public static Locale[] getAvailableLocales() {
        return ICUResourceBundle.getAvailableLocales();
    }

    public static ULocale[] getAvailableULocales() {
        return ICUResourceBundle.getAvailableULocales();
    }

    public static final String[] getKeywordValuesForLocale(String key, ULocale locale, boolean commonlyUsed) {
        UResourceBundle order;
        String prefRegion = ULocale.getRegionForSupplementalData(locale, true);
        ArrayList<String> values = new ArrayList<>();
        UResourceBundle calPref = UResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME, "supplementalData", ICUResourceBundle.ICU_DATA_CLASS_LOADER).get("calendarPreferenceData");
        try {
            order = calPref.get(prefRegion);
        } catch (MissingResourceException e) {
            order = calPref.get("001");
        }
        String[] caltypes = order.getStringArray();
        if (commonlyUsed) {
            return caltypes;
        }
        for (String add : caltypes) {
            values.add(add);
        }
        for (CalType t : CalType.values()) {
            if (!values.contains(t.id)) {
                values.add(t.id);
            }
        }
        return (String[]) values.toArray(new String[values.size()]);
    }

    public final Date getTime() {
        return new Date(getTimeInMillis());
    }

    public final void setTime(Date date) {
        setTimeInMillis(date.getTime());
    }

    public long getTimeInMillis() {
        if (!this.isTimeSet) {
            updateTime();
        }
        return this.time;
    }

    public void setTimeInMillis(long millis) {
        if (millis > 183882168921600000L) {
            if (isLenient()) {
                millis = 183882168921600000L;
            } else {
                throw new IllegalArgumentException("millis value greater than upper bounds for a Calendar : " + millis);
            }
        } else if (millis < -184303902528000000L) {
            if (isLenient()) {
                millis = -184303902528000000L;
            } else {
                throw new IllegalArgumentException("millis value less than lower bounds for a Calendar : " + millis);
            }
        }
        this.time = millis;
        this.areAllFieldsSet = false;
        this.areFieldsSet = false;
        this.areFieldsVirtuallySet = true;
        this.isTimeSet = true;
        for (int i = 0; i < this.fields.length; i++) {
            int[] iArr = this.fields;
            this.stamp[i] = 0;
            iArr[i] = 0;
        }
    }

    public final int get(int field) {
        complete();
        return this.fields[field];
    }

    /* access modifiers changed from: protected */
    public final int internalGet(int field) {
        return this.fields[field];
    }

    /* access modifiers changed from: protected */
    public final int internalGet(int field, int defaultValue) {
        return this.stamp[field] > 0 ? this.fields[field] : defaultValue;
    }

    public final void set(int field, int value) {
        if (this.areFieldsVirtuallySet) {
            computeFields();
        }
        this.fields[field] = value;
        if (this.nextStamp == STAMP_MAX) {
            recalculateStamp();
        }
        int[] iArr = this.stamp;
        int i = this.nextStamp;
        this.nextStamp = i + 1;
        iArr[field] = i;
        this.areFieldsVirtuallySet = false;
        this.areFieldsSet = false;
        this.isTimeSet = false;
    }

    public final void set(int year, int month, int date) {
        set(1, year);
        set(2, month);
        set(5, date);
    }

    public final void set(int year, int month, int date, int hour, int minute) {
        set(1, year);
        set(2, month);
        set(5, date);
        set(11, hour);
        set(12, minute);
    }

    public final void set(int year, int month, int date, int hour, int minute, int second) {
        set(1, year);
        set(2, month);
        set(5, date);
        set(11, hour);
        set(12, minute);
        set(13, second);
    }

    private static int gregoYearFromIslamicStart(int year) {
        int shift;
        int i = 0;
        if (year >= 1397) {
            int i2 = 2 * ((year - 1397) / 67);
            if ((year - 1397) % 67 >= 33) {
                i = 1;
            }
            shift = i2 + i;
        } else {
            int i3 = 2 * (((year - 1396) / 67) - 1);
            if ((-(year - 1396)) % 67 <= 33) {
                i = 1;
            }
            shift = i3 + i;
        }
        return (year + 579) - shift;
    }

    @Deprecated
    public final int getRelatedYear() {
        int year = get(19);
        CalType type = CalType.GREGORIAN;
        String typeString = getType();
        CalType[] values = CalType.values();
        int length = values.length;
        int i = 0;
        while (true) {
            if (i >= length) {
                break;
            }
            CalType testType = values[i];
            if (typeString.equals(testType.id)) {
                type = testType;
                break;
            }
            i++;
        }
        switch (type) {
            case CHINESE:
                return year - 2637;
            case COPTIC:
                return year + 284;
            case DANGI:
                return year - 2333;
            case ETHIOPIC:
                return year + 8;
            case ETHIOPIC_AMETE_ALEM:
                return year - 5492;
            case HEBREW:
                return year - 3760;
            case INDIAN:
                return year + 79;
            case ISLAMIC_CIVIL:
            case ISLAMIC_UMALQURA:
            case ISLAMIC_TBLA:
            case ISLAMIC_RGSA:
            case ISLAMIC:
                return gregoYearFromIslamicStart(year);
            case PERSIAN:
                return year + 622;
            default:
                return year;
        }
    }

    private static int firstIslamicStartYearFromGrego(int year) {
        int shift;
        int i = 0;
        if (year >= 1977) {
            int i2 = 2 * ((year - 1977) / 65);
            if ((year - 1977) % 65 >= 32) {
                i = 1;
            }
            shift = i2 + i;
        } else {
            int i3 = 2 * (((year - 1976) / 65) - 1);
            if ((-(year - 1976)) % 65 <= 32) {
                i = 1;
            }
            shift = i3 + i;
        }
        return (year - 579) + shift;
    }

    @Deprecated
    public final void setRelatedYear(int year) {
        CalType type = CalType.GREGORIAN;
        String typeString = getType();
        CalType[] values = CalType.values();
        int length = values.length;
        int i = 0;
        while (true) {
            if (i >= length) {
                break;
            }
            CalType testType = values[i];
            if (typeString.equals(testType.id)) {
                type = testType;
                break;
            }
            i++;
        }
        switch (type) {
            case CHINESE:
                year += 2637;
                break;
            case COPTIC:
                year -= 284;
                break;
            case DANGI:
                year += 2333;
                break;
            case ETHIOPIC:
                year -= 8;
                break;
            case ETHIOPIC_AMETE_ALEM:
                year += 5492;
                break;
            case HEBREW:
                year += 3760;
                break;
            case INDIAN:
                year -= 79;
                break;
            case ISLAMIC_CIVIL:
            case ISLAMIC_UMALQURA:
            case ISLAMIC_TBLA:
            case ISLAMIC_RGSA:
            case ISLAMIC:
                year = firstIslamicStartYearFromGrego(year);
                break;
            case PERSIAN:
                year -= 622;
                break;
        }
        set(19, year);
    }

    public final void clear() {
        for (int i = 0; i < this.fields.length; i++) {
            int[] iArr = this.fields;
            this.stamp[i] = 0;
            iArr[i] = 0;
        }
        this.areFieldsVirtuallySet = false;
        this.areAllFieldsSet = false;
        this.areFieldsSet = false;
        this.isTimeSet = false;
    }

    public final void clear(int field) {
        if (this.areFieldsVirtuallySet) {
            computeFields();
        }
        this.fields[field] = 0;
        this.stamp[field] = 0;
        this.areFieldsVirtuallySet = false;
        this.areAllFieldsSet = false;
        this.areFieldsSet = false;
        this.isTimeSet = false;
    }

    public final boolean isSet(int field) {
        return this.areFieldsVirtuallySet || this.stamp[field] != 0;
    }

    /* access modifiers changed from: protected */
    public void complete() {
        if (!this.isTimeSet) {
            updateTime();
        }
        if (!this.areFieldsSet) {
            computeFields();
            this.areFieldsSet = true;
            this.areAllFieldsSet = true;
        }
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Calendar that = (Calendar) obj;
        if (isEquivalentTo(that) && getTimeInMillis() == that.getTime().getTime()) {
            z = true;
        }
        return z;
    }

    public boolean isEquivalentTo(Calendar other) {
        return getClass() == other.getClass() && isLenient() == other.isLenient() && getFirstDayOfWeek() == other.getFirstDayOfWeek() && getMinimalDaysInFirstWeek() == other.getMinimalDaysInFirstWeek() && getTimeZone().equals(other.getTimeZone()) && getRepeatedWallTimeOption() == other.getRepeatedWallTimeOption() && getSkippedWallTimeOption() == other.getSkippedWallTimeOption();
    }

    public int hashCode() {
        return ((((this.lenient | (this.firstDayOfWeek << 1)) | (this.minimalDaysInFirstWeek << 4)) | (this.repeatedWallTime << 7)) | (this.skippedWallTime << 9)) | (this.zone.hashCode() << 11) ? 1 : 0;
    }

    private long compare(Object that) {
        long thatMs;
        if (that instanceof Calendar) {
            thatMs = ((Calendar) that).getTimeInMillis();
        } else if (that instanceof Date) {
            thatMs = ((Date) that).getTime();
        } else {
            throw new IllegalArgumentException(that + "is not a Calendar or Date");
        }
        return getTimeInMillis() - thatMs;
    }

    public boolean before(Object when) {
        return compare(when) < 0;
    }

    public boolean after(Object when) {
        return compare(when) > 0;
    }

    public int getActualMaximum(int field) {
        switch (field) {
            case 0:
            case 7:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 14:
            case 15:
            case 16:
            case 18:
            case 20:
            case 21:
                return getMaximum(field);
            case 5:
                Calendar cal = (Calendar) clone();
                cal.setLenient(true);
                cal.prepareGetActual(field, false);
                return handleGetMonthLength(cal.get(19), cal.get(2));
            case 6:
                Calendar cal2 = (Calendar) clone();
                cal2.setLenient(true);
                cal2.prepareGetActual(field, false);
                return handleGetYearLength(cal2.get(19));
            default:
                return getActualHelper(field, getLeastMaximum(field), getMaximum(field));
        }
    }

    public int getActualMinimum(int field) {
        switch (field) {
            case 7:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 14:
            case 15:
            case 16:
            case 18:
            case 20:
            case 21:
                return getMinimum(field);
            default:
                return getActualHelper(field, getGreatestMinimum(field), getMinimum(field));
        }
    }

    /* access modifiers changed from: protected */
    public void prepareGetActual(int field, boolean isMinimum) {
        set(21, 0);
        if (field == 8) {
            set(5, 1);
            set(7, get(7));
        } else if (field != 17) {
            if (field != 19) {
                switch (field) {
                    case 1:
                        break;
                    case 2:
                        set(5, getGreatestMinimum(5));
                        break;
                    case 3:
                    case 4:
                        int dow = this.firstDayOfWeek;
                        if (isMinimum) {
                            dow = (dow + 6) % 7;
                            if (dow < 1) {
                                dow += 7;
                            }
                        }
                        set(7, dow);
                        break;
                }
            }
            set(6, getGreatestMinimum(6));
        } else {
            set(3, getGreatestMinimum(3));
        }
        set(field, getGreatestMinimum(field));
    }

    private int getActualHelper(int field, int result, int endValue) {
        if (result == endValue) {
            return result;
        }
        boolean z = true;
        int delta = endValue > result ? 1 : -1;
        Calendar work = (Calendar) clone();
        work.complete();
        work.setLenient(true);
        if (delta >= 0) {
            z = false;
        }
        work.prepareGetActual(field, z);
        work.set(field, result);
        if (work.get(field) != result && field != 4 && delta > 0) {
            return result;
        }
        int startValue = result;
        do {
            startValue += delta;
            work.add(field, delta);
            if (work.get(field) != startValue) {
                break;
            }
            result = startValue;
        } while (startValue != endValue);
        return result;
    }

    public final void roll(int field, boolean up) {
        roll(field, up ? 1 : -1);
    }

    public void roll(int field, int amount) {
        int start;
        int start2;
        int i = field;
        int amount2 = amount;
        if (amount2 != 0) {
            complete();
            int i2 = 1;
            switch (i) {
                case 0:
                case 5:
                case 9:
                case 12:
                case 13:
                case 14:
                case 21:
                    int min = getActualMinimum(field);
                    int gap = (getActualMaximum(field) - min) + 1;
                    int value = ((internalGet(field) + amount2) - min) % gap;
                    if (value < 0) {
                        value += gap;
                    }
                    set(i, value + min);
                    return;
                case 1:
                case 17:
                    boolean era0WithYearsThatGoBackwards = false;
                    int era = get(0);
                    if (era == 0) {
                        String calType = getType();
                        if (calType.equals("gregorian") || calType.equals("roc") || calType.equals("coptic")) {
                            amount2 = -amount2;
                            era0WithYearsThatGoBackwards = true;
                        }
                    }
                    int newYear = internalGet(field) + amount2;
                    if (era > 0 || newYear >= 1) {
                        int maxYear = getActualMaximum(field);
                        if (maxYear < 32768) {
                            if (newYear < 1) {
                                newYear = maxYear - ((-newYear) % maxYear);
                            } else if (newYear > maxYear) {
                                newYear = ((newYear - 1) % maxYear) + 1;
                            }
                        } else if (newYear < 1) {
                            newYear = 1;
                        }
                    } else if (era0WithYearsThatGoBackwards) {
                        newYear = 1;
                    }
                    set(i, newYear);
                    pinField(2);
                    pinField(5);
                    return;
                case 2:
                    int max = getActualMaximum(2);
                    int mon = (internalGet(2) + amount2) % (max + 1);
                    if (mon < 0) {
                        mon += max + 1;
                    }
                    set(2, mon);
                    pinField(5);
                    return;
                case 3:
                    int dow = internalGet(7) - getFirstDayOfWeek();
                    if (dow < 0) {
                        dow += 7;
                    }
                    int fdy = ((dow - internalGet(6)) + 1) % 7;
                    if (fdy < 0) {
                        fdy += 7;
                    }
                    if (7 - fdy < getMinimalDaysInFirstWeek()) {
                        start = 8 - fdy;
                    } else {
                        start = 1 - fdy;
                    }
                    int yearLen = getActualMaximum(6);
                    int gap2 = ((yearLen + 7) - (((yearLen - internalGet(6)) + dow) % 7)) - start;
                    int day_of_year = ((internalGet(6) + (amount2 * 7)) - start) % gap2;
                    if (day_of_year < 0) {
                        day_of_year += gap2;
                    }
                    int day_of_year2 = day_of_year + start;
                    if (day_of_year2 < 1) {
                        day_of_year2 = 1;
                    }
                    if (day_of_year2 > yearLen) {
                        day_of_year2 = yearLen;
                    }
                    set(6, day_of_year2);
                    clear(2);
                    return;
                case 4:
                    int dow2 = internalGet(7) - getFirstDayOfWeek();
                    if (dow2 < 0) {
                        dow2 += 7;
                    }
                    int fdm = ((dow2 - internalGet(5)) + 1) % 7;
                    if (fdm < 0) {
                        fdm += 7;
                    }
                    if (7 - fdm < getMinimalDaysInFirstWeek()) {
                        start2 = 8 - fdm;
                    } else {
                        start2 = 1 - fdm;
                    }
                    int monthLen = getActualMaximum(5);
                    int gap3 = ((monthLen + 7) - (((monthLen - internalGet(5)) + dow2) % 7)) - start2;
                    int day_of_month = ((internalGet(5) + (amount2 * 7)) - start2) % gap3;
                    if (day_of_month < 0) {
                        day_of_month += gap3;
                    }
                    int day_of_month2 = day_of_month + start2;
                    if (day_of_month2 < 1) {
                        day_of_month2 = 1;
                    }
                    if (day_of_month2 > monthLen) {
                        day_of_month2 = monthLen;
                    }
                    set(5, day_of_month2);
                    return;
                case 6:
                    long min2 = this.time - (((long) (internalGet(6) - 1)) * 86400000);
                    int yearLength = getActualMaximum(6);
                    this.time = ((this.time + (((long) amount2) * 86400000)) - min2) % (((long) yearLength) * 86400000);
                    if (this.time < 0) {
                        this.time += ((long) yearLength) * 86400000;
                    }
                    setTimeInMillis(this.time + min2);
                    return;
                case 7:
                case 18:
                    long delta = ((long) amount2) * 86400000;
                    int leadDays = internalGet(field);
                    if (i == 7) {
                        i2 = getFirstDayOfWeek();
                    }
                    int leadDays2 = leadDays - i2;
                    if (leadDays2 < 0) {
                        leadDays2 += 7;
                    }
                    long min22 = this.time - (((long) leadDays2) * 86400000);
                    this.time = ((this.time + delta) - min22) % 604800000;
                    if (this.time < 0) {
                        this.time += 604800000;
                    }
                    setTimeInMillis(this.time + min22);
                    return;
                case 8:
                    int preWeeks = (internalGet(5) - 1) / 7;
                    long min23 = this.time - (((long) preWeeks) * 604800000);
                    long gap22 = 604800000 * ((long) (preWeeks + ((getActualMaximum(5) - internalGet(5)) / 7) + 1));
                    this.time = ((this.time + (((long) amount2) * 604800000)) - min23) % gap22;
                    if (this.time < 0) {
                        this.time += gap22;
                    }
                    setTimeInMillis(this.time + min23);
                    return;
                case 10:
                case 11:
                    long start3 = getTimeInMillis();
                    int oldHour = internalGet(field);
                    int max2 = getMaximum(field);
                    int newHour = (oldHour + amount2) % (max2 + 1);
                    if (newHour < 0) {
                        newHour += max2 + 1;
                    }
                    setTimeInMillis((RelativeDateTimeFormatter.HOUR_IN_MILLIS * (((long) newHour) - ((long) oldHour))) + start3);
                    return;
                case 19:
                    set(i, internalGet(field) + amount2);
                    pinField(2);
                    pinField(5);
                    return;
                case 20:
                    set(i, internalGet(field) + amount2);
                    return;
                default:
                    throw new IllegalArgumentException("Calendar.roll(" + fieldName(field) + ") not supported");
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0051, code lost:
        r6 = 0;
        r9 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0059, code lost:
        if (r5 == false) goto L_0x0069;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x005b, code lost:
        r6 = get(16) + get(15);
        r9 = get(21);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0069, code lost:
        setTimeInMillis(getTimeInMillis() + r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0071, code lost:
        if (r5 == false) goto L_0x010a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0073, code lost:
        r13 = get(21);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0077, code lost:
        if (r13 == r9) goto L_0x010a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0079, code lost:
        r14 = internalGetTimeInMillis();
        r11 = get(16) + get(15);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0086, code lost:
        if (r11 == r6) goto L_0x010a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0088, code lost:
        r17 = r13;
        r12 = ((long) (r6 - r11)) % 86400000;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0092, code lost:
        if (r12 == 0) goto L_0x00a0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0094, code lost:
        setTimeInMillis(r14 + r12);
        r7 = get(21);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x00a0, code lost:
        r7 = r17;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x00a2, code lost:
        if (r7 == r9) goto L_0x010a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x00a6, code lost:
        switch(r0.skippedWallTime) {
            case 0: goto L_0x00fa;
            case 1: goto L_0x00ea;
            case 2: goto L_0x00aa;
            default: goto L_0x00a9;
        };
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x00ae, code lost:
        if (r12 <= 0) goto L_0x00b5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x00b0, code lost:
        r16 = internalGetTimeInMillis();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x00b5, code lost:
        r16 = r14;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x00b7, code lost:
        r20 = r3;
        r3 = r16;
        r8 = getImmediatePreviousZoneTransition(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x00c1, code lost:
        if (r8 == null) goto L_0x00cf;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x00c3, code lost:
        r22 = r5;
        r23 = r6;
        setTimeInMillis(r8.longValue());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x00cf, code lost:
        r22 = r5;
        r23 = r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x00e9, code lost:
        throw new java.lang.RuntimeException("Could not locate a time zone transition before " + r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x00ea, code lost:
        r20 = r3;
        r22 = r5;
        r23 = r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x00f4, code lost:
        if (r12 <= 0) goto L_0x0110;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x00f6, code lost:
        setTimeInMillis(r14);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x00fa, code lost:
        r20 = r3;
        r22 = r5;
        r23 = r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x0104, code lost:
        if (r12 >= 0) goto L_0x0110;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x0106, code lost:
        setTimeInMillis(r14);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x010a, code lost:
        r20 = r3;
        r22 = r5;
        r23 = r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x0110, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x0134, code lost:
        r6 = isLenient();
        setLenient(true);
        set(r1, get(r25) + r2);
        pinField(5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x0148, code lost:
        if (r6 != false) goto L_0x0150;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x014a, code lost:
        complete();
        setLenient(r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:59:0x0150, code lost:
        return;
     */
    public void add(int field, int amount) {
        int i = field;
        int amount2 = amount;
        if (amount2 != 0) {
            long delta = (long) amount2;
            boolean keepWallTimeInvariant = true;
            switch (i) {
                case 0:
                    set(i, get(field) + amount2);
                    pinField(0);
                    return;
                case 1:
                case 17:
                    if (get(0) == 0) {
                        String calType = getType();
                        if (calType.equals("gregorian") || calType.equals("roc") || calType.equals("coptic")) {
                            amount2 = -amount2;
                            break;
                        }
                    }
                    break;
                case 2:
                case 19:
                    break;
                case 3:
                case 4:
                case 8:
                    delta *= 604800000;
                    break;
                case 5:
                case 6:
                case 7:
                case 18:
                case 20:
                    delta *= 86400000;
                    break;
                case 9:
                    delta *= 43200000;
                    break;
                case 10:
                case 11:
                    delta *= RelativeDateTimeFormatter.HOUR_IN_MILLIS;
                    keepWallTimeInvariant = false;
                    break;
                case 12:
                    delta *= RelativeDateTimeFormatter.MINUTE_IN_MILLIS;
                    keepWallTimeInvariant = false;
                    break;
                case 13:
                    delta *= 1000;
                    keepWallTimeInvariant = false;
                    break;
                case 14:
                case 21:
                    keepWallTimeInvariant = false;
                    break;
                default:
                    throw new IllegalArgumentException("Calendar.add(" + fieldName(field) + ") not supported");
            }
        }
    }

    public String getDisplayName(Locale loc) {
        return getClass().getName();
    }

    public String getDisplayName(ULocale loc) {
        return getClass().getName();
    }

    public int compareTo(Calendar that) {
        long v = getTimeInMillis() - that.getTimeInMillis();
        if (v < 0) {
            return -1;
        }
        return v > 0 ? 1 : 0;
    }

    public DateFormat getDateTimeFormat(int dateStyle, int timeStyle, Locale loc) {
        return formatHelper(this, ULocale.forLocale(loc), dateStyle, timeStyle);
    }

    public DateFormat getDateTimeFormat(int dateStyle, int timeStyle, ULocale loc) {
        return formatHelper(this, loc, dateStyle, timeStyle);
    }

    /* access modifiers changed from: protected */
    public DateFormat handleGetDateFormat(String pattern, Locale locale) {
        return handleGetDateFormat(pattern, (String) null, ULocale.forLocale(locale));
    }

    /* access modifiers changed from: protected */
    public DateFormat handleGetDateFormat(String pattern, String override, Locale locale) {
        return handleGetDateFormat(pattern, override, ULocale.forLocale(locale));
    }

    /* access modifiers changed from: protected */
    public DateFormat handleGetDateFormat(String pattern, ULocale locale) {
        return handleGetDateFormat(pattern, (String) null, locale);
    }

    /* access modifiers changed from: protected */
    public DateFormat handleGetDateFormat(String pattern, String override, ULocale locale) {
        FormatConfiguration fmtConfig = new FormatConfiguration();
        String unused = fmtConfig.pattern = pattern;
        String unused2 = fmtConfig.override = override;
        DateFormatSymbols unused3 = fmtConfig.formatData = new DateFormatSymbols(this, locale);
        ULocale unused4 = fmtConfig.loc = locale;
        Calendar unused5 = fmtConfig.cal = this;
        return SimpleDateFormat.getInstance(fmtConfig);
    }

    private static DateFormat formatHelper(Calendar cal, ULocale loc, int dateStyle, int timeStyle) {
        String pattern;
        if (timeStyle < -1 || timeStyle > 3) {
            throw new IllegalArgumentException("Illegal time style " + timeStyle);
        } else if (dateStyle < -1 || dateStyle > 3) {
            throw new IllegalArgumentException("Illegal date style " + dateStyle);
        } else {
            PatternData patternData = PatternData.make(cal, loc);
            String override = null;
            if (timeStyle >= 0 && dateStyle >= 0) {
                pattern = SimpleFormatterImpl.formatRawPattern(patternData.getDateTimePattern(dateStyle), 2, 2, patternData.patterns[timeStyle], patternData.patterns[dateStyle + 4]);
                if (patternData.overrides != null) {
                    override = mergeOverrideStrings(patternData.patterns[dateStyle + 4], patternData.patterns[timeStyle], patternData.overrides[dateStyle + 4], patternData.overrides[timeStyle]);
                }
            } else if (timeStyle >= 0) {
                pattern = patternData.patterns[timeStyle];
                if (patternData.overrides != null) {
                    override = patternData.overrides[timeStyle];
                }
            } else if (dateStyle >= 0) {
                pattern = patternData.patterns[dateStyle + 4];
                if (patternData.overrides != null) {
                    override = patternData.overrides[dateStyle + 4];
                }
            } else {
                throw new IllegalArgumentException("No date or time style specified");
            }
            DateFormat result = cal.handleGetDateFormat(pattern, override, loc);
            result.setCalendar(cal);
            return result;
        }
    }

    public static String getDateTimeFormatString(ULocale loc, String calType, int dateStyle, int timeStyle) {
        if (timeStyle < -1 || timeStyle > 3) {
            throw new IllegalArgumentException("Illegal time style " + timeStyle);
        } else if (dateStyle < -1 || dateStyle > 3) {
            throw new IllegalArgumentException("Illegal date style " + dateStyle);
        } else {
            PatternData patternData = PatternData.make(loc, calType);
            if (timeStyle >= 0 && dateStyle >= 0) {
                return SimpleFormatterImpl.formatRawPattern(patternData.getDateTimePattern(dateStyle), 2, 2, patternData.patterns[timeStyle], patternData.patterns[dateStyle + 4]);
            } else if (timeStyle >= 0) {
                return patternData.patterns[timeStyle];
            } else {
                if (dateStyle >= 0) {
                    return patternData.patterns[dateStyle + 4];
                }
                throw new IllegalArgumentException("No date or time style specified");
            }
        }
    }

    /* access modifiers changed from: private */
    public static PatternData getPatternData(ULocale locale, String calType) {
        ICUResourceBundle rb = (ICUResourceBundle) UResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME, locale);
        ICUResourceBundle dtPatternsRb = rb.findWithFallback("calendar/" + calType + "/DateTimePatterns");
        if (dtPatternsRb == null) {
            dtPatternsRb = rb.getWithFallback("calendar/gregorian/DateTimePatterns");
        }
        int patternsSize = dtPatternsRb.getSize();
        String[] dateTimePatterns = new String[patternsSize];
        String[] dateTimePatternsOverrides = new String[patternsSize];
        for (int i = 0; i < patternsSize; i++) {
            ICUResourceBundle concatenationPatternRb = (ICUResourceBundle) dtPatternsRb.get(i);
            int type = concatenationPatternRb.getType();
            if (type == 0) {
                dateTimePatterns[i] = concatenationPatternRb.getString();
            } else if (type == 8) {
                dateTimePatterns[i] = concatenationPatternRb.getString(0);
                dateTimePatternsOverrides[i] = concatenationPatternRb.getString(1);
            }
        }
        return new PatternData(dateTimePatterns, dateTimePatternsOverrides);
    }

    @Deprecated
    public static String getDateTimePattern(Calendar cal, ULocale uLocale, int dateStyle) {
        return PatternData.make(cal, uLocale).getDateTimePattern(dateStyle);
    }

    private static String mergeOverrideStrings(String datePattern, String timePattern, String dateOverride, String timeOverride) {
        if (dateOverride == null && timeOverride == null) {
            return null;
        }
        if (dateOverride == null) {
            return expandOverride(timePattern, timeOverride);
        }
        if (timeOverride == null) {
            return expandOverride(datePattern, dateOverride);
        }
        if (dateOverride.equals(timeOverride)) {
            return dateOverride;
        }
        return expandOverride(datePattern, dateOverride) + ";" + expandOverride(timePattern, timeOverride);
    }

    private static String expandOverride(String pattern, String override) {
        if (override.indexOf(61) >= 0) {
            return override;
        }
        boolean inQuotes = false;
        char prevChar = ' ';
        StringBuilder result = new StringBuilder();
        StringCharacterIterator it = new StringCharacterIterator(pattern);
        for (char c = it.first(); c != 65535; c = it.next()) {
            if (c == '\'') {
                inQuotes = !inQuotes;
            } else if (!inQuotes && c != prevChar) {
                if (result.length() > 0) {
                    result.append(";");
                }
                result.append(c);
                result.append("=");
                result.append(override);
            }
            prevChar = c;
        }
        return result.toString();
    }

    /* access modifiers changed from: protected */
    public void pinField(int field) {
        int max = getActualMaximum(field);
        int min = getActualMinimum(field);
        if (this.fields[field] > max) {
            set(field, max);
        } else if (this.fields[field] < min) {
            set(field, min);
        }
    }

    /* access modifiers changed from: protected */
    public int weekNumber(int desiredDay, int dayOfPeriod, int dayOfWeek) {
        int periodStartDayOfWeek = (((dayOfWeek - getFirstDayOfWeek()) - dayOfPeriod) + 1) % 7;
        if (periodStartDayOfWeek < 0) {
            periodStartDayOfWeek += 7;
        }
        int weekNo = ((desiredDay + periodStartDayOfWeek) - 1) / 7;
        if (7 - periodStartDayOfWeek >= getMinimalDaysInFirstWeek()) {
            return weekNo + 1;
        }
        return weekNo;
    }

    /* access modifiers changed from: protected */
    public final int weekNumber(int dayOfPeriod, int dayOfWeek) {
        return weekNumber(dayOfPeriod, dayOfPeriod, dayOfWeek);
    }

    public int fieldDifference(Date when, int field) {
        int min = 0;
        long startMs = getTimeInMillis();
        long targetMs = when.getTime();
        if (startMs < targetMs) {
            int min2 = 0;
            int max = 1;
            while (true) {
                setTimeInMillis(startMs);
                add(field, max);
                long ms = getTimeInMillis();
                if (ms == targetMs) {
                    return max;
                }
                if (ms > targetMs) {
                    while (max - min2 > 1) {
                        int t = ((max - min2) / 2) + min2;
                        setTimeInMillis(startMs);
                        add(field, t);
                        long ms2 = getTimeInMillis();
                        if (ms2 == targetMs) {
                            return t;
                        }
                        if (ms2 > targetMs) {
                            max = t;
                        } else {
                            min2 = t;
                        }
                    }
                    min = min2;
                } else if (max < Integer.MAX_VALUE) {
                    min2 = max;
                    max <<= 1;
                    if (max < 0) {
                        max = Integer.MAX_VALUE;
                    }
                } else {
                    throw new RuntimeException();
                }
            }
        } else if (startMs > targetMs) {
            int max2 = -1;
            do {
                setTimeInMillis(startMs);
                add(field, max2);
                long ms3 = getTimeInMillis();
                if (ms3 == targetMs) {
                    return max2;
                }
                if (ms3 < targetMs) {
                    while (min - max2 > 1) {
                        int t2 = ((max2 - min) / 2) + min;
                        setTimeInMillis(startMs);
                        add(field, t2);
                        long ms4 = getTimeInMillis();
                        if (ms4 == targetMs) {
                            return t2;
                        }
                        if (ms4 < targetMs) {
                            max2 = t2;
                        } else {
                            min = t2;
                        }
                    }
                } else {
                    min = max2;
                    max2 <<= 1;
                }
            } while (max2 != 0);
            throw new RuntimeException();
        }
        setTimeInMillis(startMs);
        add(field, min);
        return min;
    }

    public void setTimeZone(TimeZone value) {
        this.zone = value;
        this.areFieldsSet = false;
    }

    public TimeZone getTimeZone() {
        return this.zone;
    }

    public void setLenient(boolean lenient2) {
        this.lenient = lenient2;
    }

    public boolean isLenient() {
        return this.lenient;
    }

    public void setRepeatedWallTimeOption(int option) {
        if (option == 0 || option == 1) {
            this.repeatedWallTime = option;
            return;
        }
        throw new IllegalArgumentException("Illegal repeated wall time option - " + option);
    }

    public int getRepeatedWallTimeOption() {
        return this.repeatedWallTime;
    }

    public void setSkippedWallTimeOption(int option) {
        if (option == 0 || option == 1 || option == 2) {
            this.skippedWallTime = option;
            return;
        }
        throw new IllegalArgumentException("Illegal skipped wall time option - " + option);
    }

    public int getSkippedWallTimeOption() {
        return this.skippedWallTime;
    }

    public void setFirstDayOfWeek(int value) {
        if (this.firstDayOfWeek == value) {
            return;
        }
        if (value < 1 || value > 7) {
            throw new IllegalArgumentException("Invalid day of week");
        }
        this.firstDayOfWeek = value;
        this.areFieldsSet = false;
    }

    public int getFirstDayOfWeek() {
        return this.firstDayOfWeek;
    }

    public void setMinimalDaysInFirstWeek(int value) {
        if (value < 1) {
            value = 1;
        } else if (value > 7) {
            value = 7;
        }
        if (this.minimalDaysInFirstWeek != value) {
            this.minimalDaysInFirstWeek = value;
            this.areFieldsSet = false;
        }
    }

    public int getMinimalDaysInFirstWeek() {
        return this.minimalDaysInFirstWeek;
    }

    /* access modifiers changed from: protected */
    public int getLimit(int field, int limitType) {
        switch (field) {
            case 4:
                int minDaysInFirst = 1;
                if (limitType == 0) {
                    if (getMinimalDaysInFirstWeek() != 1) {
                        minDaysInFirst = 0;
                    }
                } else if (limitType == 1) {
                    minDaysInFirst = 1;
                } else {
                    int minDaysInFirst2 = getMinimalDaysInFirstWeek();
                    int daysInMonth = handleGetLimit(5, limitType);
                    if (limitType == 2) {
                        minDaysInFirst = ((7 - minDaysInFirst2) + daysInMonth) / 7;
                    } else {
                        minDaysInFirst = ((daysInMonth + 6) + (7 - minDaysInFirst2)) / 7;
                    }
                }
                return minDaysInFirst;
            case 7:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 14:
            case 15:
            case 16:
            case 18:
            case 20:
            case 21:
            case 22:
                return LIMITS[field][limitType];
            default:
                return handleGetLimit(field, limitType);
        }
    }

    public final int getMinimum(int field) {
        return getLimit(field, 0);
    }

    public final int getMaximum(int field) {
        return getLimit(field, 3);
    }

    public final int getGreatestMinimum(int field) {
        return getLimit(field, 1);
    }

    public final int getLeastMaximum(int field) {
        return getLimit(field, 2);
    }

    @Deprecated
    public int getDayOfWeekType(int dayOfWeek) {
        int i = 1;
        if (dayOfWeek < 1 || dayOfWeek > 7) {
            throw new IllegalArgumentException("Invalid day of week");
        } else if (this.weekendOnset != this.weekendCease) {
            if (this.weekendOnset < this.weekendCease) {
                if (dayOfWeek < this.weekendOnset || dayOfWeek > this.weekendCease) {
                    return 0;
                }
            } else if (dayOfWeek > this.weekendCease && dayOfWeek < this.weekendOnset) {
                return 0;
            }
            if (dayOfWeek == this.weekendOnset) {
                if (this.weekendOnsetMillis != 0) {
                    i = 2;
                }
                return i;
            } else if (dayOfWeek != this.weekendCease) {
                return 1;
            } else {
                if (this.weekendCeaseMillis < 86400000) {
                    i = 3;
                }
                return i;
            }
        } else if (dayOfWeek != this.weekendOnset) {
            return 0;
        } else {
            if (this.weekendOnsetMillis != 0) {
                i = 2;
            }
            return i;
        }
    }

    @Deprecated
    public int getWeekendTransition(int dayOfWeek) {
        if (dayOfWeek == this.weekendOnset) {
            return this.weekendOnsetMillis;
        }
        if (dayOfWeek == this.weekendCease) {
            return this.weekendCeaseMillis;
        }
        throw new IllegalArgumentException("Not weekend transition day");
    }

    public boolean isWeekend(Date date) {
        setTime(date);
        return isWeekend();
    }

    public boolean isWeekend() {
        int dow = get(7);
        int dowt = getDayOfWeekType(dow);
        boolean z = false;
        switch (dowt) {
            case 0:
                return false;
            case 1:
                return true;
            default:
                int millisInDay = internalGet(14) + (1000 * (internalGet(13) + (60 * (internalGet(12) + (internalGet(11) * 60)))));
                int transition = getWeekendTransition(dow);
                if (dowt != 2 ? millisInDay < transition : millisInDay >= transition) {
                    z = true;
                }
                return z;
        }
    }

    public Object clone() {
        try {
            Calendar other = (Calendar) super.clone();
            other.fields = new int[this.fields.length];
            other.stamp = new int[this.fields.length];
            System.arraycopy(this.fields, 0, other.fields, 0, this.fields.length);
            System.arraycopy(this.stamp, 0, other.stamp, 0, this.fields.length);
            other.zone = (TimeZone) this.zone.clone();
            return other;
        } catch (CloneNotSupportedException e) {
            throw new ICUCloneNotSupportedException((Throwable) e);
        }
    }

    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append(getClass().getName());
        buffer.append("[time=");
        buffer.append(this.isTimeSet ? String.valueOf(this.time) : "?");
        buffer.append(",areFieldsSet=");
        buffer.append(this.areFieldsSet);
        buffer.append(",areAllFieldsSet=");
        buffer.append(this.areAllFieldsSet);
        buffer.append(",lenient=");
        buffer.append(this.lenient);
        buffer.append(",zone=");
        buffer.append(this.zone);
        buffer.append(",firstDayOfWeek=");
        buffer.append(this.firstDayOfWeek);
        buffer.append(",minimalDaysInFirstWeek=");
        buffer.append(this.minimalDaysInFirstWeek);
        buffer.append(",repeatedWallTime=");
        buffer.append(this.repeatedWallTime);
        buffer.append(",skippedWallTime=");
        buffer.append(this.skippedWallTime);
        for (int i = 0; i < this.fields.length; i++) {
            buffer.append(',');
            buffer.append(fieldName(i));
            buffer.append('=');
            buffer.append(isSet(i) ? String.valueOf(this.fields[i]) : "?");
        }
        buffer.append(']');
        return buffer.toString();
    }

    public static WeekData getWeekDataForRegion(String region) {
        return WEEK_DATA_CACHE.createInstance(region, region);
    }

    public WeekData getWeekData() {
        WeekData weekData = new WeekData(this.firstDayOfWeek, this.minimalDaysInFirstWeek, this.weekendOnset, this.weekendOnsetMillis, this.weekendCease, this.weekendCeaseMillis);
        return weekData;
    }

    public Calendar setWeekData(WeekData wdata) {
        setFirstDayOfWeek(wdata.firstDayOfWeek);
        setMinimalDaysInFirstWeek(wdata.minimalDaysInFirstWeek);
        this.weekendOnset = wdata.weekendOnset;
        this.weekendOnsetMillis = wdata.weekendOnsetMillis;
        this.weekendCease = wdata.weekendCease;
        this.weekendCeaseMillis = wdata.weekendCeaseMillis;
        return this;
    }

    /* access modifiers changed from: private */
    public static WeekData getWeekDataForRegionInternal(String region) {
        UResourceBundle weekDataBundle;
        if (region == null) {
            region = "001";
        }
        UResourceBundle weekDataInfo = UResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME, "supplementalData", ICUResourceBundle.ICU_DATA_CLASS_LOADER).get("weekData");
        try {
            weekDataBundle = weekDataInfo.get(region);
        } catch (MissingResourceException mre) {
            if (!region.equals("001")) {
                weekDataBundle = weekDataInfo.get("001");
            } else {
                throw mre;
            }
        }
        int[] wdi = weekDataBundle.getIntVector();
        WeekData weekData = new WeekData(wdi[0], wdi[1], wdi[2], wdi[3], wdi[4], wdi[5]);
        return weekData;
    }

    private void setWeekData(String region) {
        if (region == null) {
            region = "001";
        }
        setWeekData((WeekData) WEEK_DATA_CACHE.getInstance(region, region));
    }

    private void updateTime() {
        computeTime();
        if (isLenient() || !this.areAllFieldsSet) {
            this.areFieldsSet = false;
        }
        this.isTimeSet = true;
        this.areFieldsVirtuallySet = false;
    }

    private void writeObject(ObjectOutputStream stream) throws IOException {
        if (!this.isTimeSet) {
            try {
                updateTime();
            } catch (IllegalArgumentException e) {
            }
        }
        stream.defaultWriteObject();
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        initInternal();
        this.isTimeSet = true;
        this.areAllFieldsSet = false;
        this.areFieldsSet = false;
        this.areFieldsVirtuallySet = true;
        this.nextStamp = 2;
    }

    /* access modifiers changed from: protected */
    public void computeFields() {
        int[] offsets = new int[2];
        getTimeZone().getOffset(this.time, false, offsets);
        long localMillis = this.time + ((long) offsets[0]) + ((long) offsets[1]);
        int mask = this.internalSetMask;
        for (int i = 0; i < this.fields.length; i++) {
            if ((mask & 1) == 0) {
                this.stamp[i] = 1;
            } else {
                this.stamp[i] = 0;
            }
            mask >>= 1;
        }
        long days = floorDivide(localMillis, 86400000);
        this.fields[20] = ((int) days) + EPOCH_JULIAN_DAY;
        computeGregorianAndDOWFields(this.fields[20]);
        handleComputeFields(this.fields[20]);
        computeWeekFields();
        int millisInDay = (int) (localMillis - (86400000 * days));
        this.fields[21] = millisInDay;
        this.fields[14] = millisInDay % 1000;
        int millisInDay2 = millisInDay / 1000;
        this.fields[13] = millisInDay2 % 60;
        int millisInDay3 = millisInDay2 / 60;
        this.fields[12] = millisInDay3 % 60;
        int millisInDay4 = millisInDay3 / 60;
        this.fields[11] = millisInDay4;
        this.fields[9] = millisInDay4 / 12;
        this.fields[10] = millisInDay4 % 12;
        this.fields[15] = offsets[0];
        this.fields[16] = offsets[1];
    }

    private final void computeGregorianAndDOWFields(int julianDay) {
        computeGregorianFields(julianDay);
        int[] iArr = this.fields;
        int dow = julianDayToDayOfWeek(julianDay);
        iArr[7] = dow;
        int dowLocal = (dow - getFirstDayOfWeek()) + 1;
        if (dowLocal < 1) {
            dowLocal += 7;
        }
        this.fields[18] = dowLocal;
    }

    /* access modifiers changed from: protected */
    public final void computeGregorianFields(int julianDay) {
        int[] rem = new int[1];
        int n400 = floorDivide((long) (julianDay - JAN_1_1_JULIAN_DAY), 146097, rem);
        boolean isLeap = false;
        int n100 = floorDivide(rem[0], 36524, rem);
        int n4 = floorDivide(rem[0], 1461, rem);
        int n1 = floorDivide(rem[0], 365, rem);
        int year = (400 * n400) + (100 * n100) + (4 * n4) + n1;
        int dayOfYear = rem[0];
        if (n100 == 4 || n1 == 4) {
            dayOfYear = 365;
        } else {
            year++;
        }
        if ((year & 3) == 0 && (year % 100 != 0 || year % 400 == 0)) {
            isLeap = true;
        }
        int correction = 0;
        if (dayOfYear >= (isLeap ? 60 : 59)) {
            correction = isLeap ? 1 : 2;
        }
        int month = ((12 * (dayOfYear + correction)) + 6) / 367;
        int dayOfMonth = 1 + (dayOfYear - GREGORIAN_MONTH_COUNT[month][isLeap ? (char) 3 : 2]);
        this.gregorianYear = year;
        this.gregorianMonth = month;
        this.gregorianDayOfMonth = dayOfMonth;
        int i = dayOfMonth;
        this.gregorianDayOfYear = dayOfYear + 1;
    }

    private final void computeWeekFields() {
        int eyear = this.fields[19];
        int dayOfWeek = this.fields[7];
        int dayOfYear = this.fields[6];
        int yearOfWeekOfYear = eyear;
        int relDow = ((dayOfWeek + 7) - getFirstDayOfWeek()) % 7;
        int relDowJan1 = (((dayOfWeek - dayOfYear) + 7001) - getFirstDayOfWeek()) % 7;
        int woy = ((dayOfYear - 1) + relDowJan1) / 7;
        if (7 - relDowJan1 >= getMinimalDaysInFirstWeek()) {
            woy++;
        }
        if (woy == 0) {
            woy = weekNumber(handleGetYearLength(eyear - 1) + dayOfYear, dayOfWeek);
            yearOfWeekOfYear--;
        } else {
            int lastDoy = handleGetYearLength(eyear);
            if (dayOfYear >= lastDoy - 5) {
                int lastRelDow = ((relDow + lastDoy) - dayOfYear) % 7;
                if (lastRelDow < 0) {
                    lastRelDow += 7;
                }
                if (6 - lastRelDow >= getMinimalDaysInFirstWeek() && (dayOfYear + 7) - relDow > lastDoy) {
                    woy = 1;
                    yearOfWeekOfYear++;
                }
            }
        }
        this.fields[3] = woy;
        this.fields[17] = yearOfWeekOfYear;
        this.fields[4] = weekNumber(this.fields[5], dayOfWeek);
        this.fields[8] = ((dayOfMonth - 1) / 7) + 1;
    }

    /* access modifiers changed from: protected */
    public int resolveFields(int[][][] precedenceTable) {
        int bestField = -1;
        int g = 0;
        while (g < precedenceTable.length && bestField < 0) {
            int[][] group = precedenceTable[g];
            int bestStamp = 0;
            int bestField2 = bestField;
            for (int[] line : group) {
                int lineStamp = 0;
                int i = line[0] >= 32 ? 1 : 0;
                while (true) {
                    if (i < line.length) {
                        int s = this.stamp[line[i]];
                        if (s == 0) {
                            break;
                        }
                        lineStamp = Math.max(lineStamp, s);
                        i++;
                    } else if (lineStamp > bestStamp) {
                        int tempBestField = line[0];
                        if (tempBestField >= 32) {
                            tempBestField &= 31;
                            if (tempBestField != 5 || this.stamp[4] < this.stamp[tempBestField]) {
                                bestField2 = tempBestField;
                            }
                        } else {
                            bestField2 = tempBestField;
                        }
                        if (bestField2 == tempBestField) {
                            bestStamp = lineStamp;
                        }
                    }
                }
            }
            g++;
            bestField = bestField2;
        }
        return bestField >= 32 ? bestField & 31 : bestField;
    }

    /* access modifiers changed from: protected */
    public int newestStamp(int first, int last, int bestStampSoFar) {
        int bestStamp = bestStampSoFar;
        for (int i = first; i <= last; i++) {
            if (this.stamp[i] > bestStamp) {
                bestStamp = this.stamp[i];
            }
        }
        return bestStamp;
    }

    /* access modifiers changed from: protected */
    public final int getStamp(int field) {
        return this.stamp[field];
    }

    /* access modifiers changed from: protected */
    public int newerField(int defaultField, int alternateField) {
        if (this.stamp[alternateField] > this.stamp[defaultField]) {
            return alternateField;
        }
        return defaultField;
    }

    /* access modifiers changed from: protected */
    public void validateFields() {
        for (int field = 0; field < this.fields.length; field++) {
            if (this.stamp[field] >= 2) {
                validateField(field);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void validateField(int field) {
        switch (field) {
            case 5:
                validateField(field, 1, handleGetMonthLength(handleGetExtendedYear(), internalGet(2)));
                return;
            case 6:
                validateField(field, 1, handleGetYearLength(handleGetExtendedYear()));
                return;
            case 8:
                if (internalGet(field) != 0) {
                    validateField(field, getMinimum(field), getMaximum(field));
                    return;
                }
                throw new IllegalArgumentException("DAY_OF_WEEK_IN_MONTH cannot be zero");
            default:
                validateField(field, getMinimum(field), getMaximum(field));
                return;
        }
    }

    /* access modifiers changed from: protected */
    public final void validateField(int field, int min, int max) {
        int value = this.fields[field];
        if (value < min || value > max) {
            throw new IllegalArgumentException(fieldName(field) + '=' + value + ", valid range=" + min + ".." + max);
        }
    }

    /* access modifiers changed from: protected */
    public void computeTime() {
        long millisInDay;
        if (!isLenient()) {
            validateFields();
        }
        long millis = julianDayToMillis(computeJulianDay());
        if (this.stamp[21] >= 2 && newestStamp(9, 14, 0) <= this.stamp[21]) {
            millisInDay = (long) internalGet(21);
        } else if (Math.max(Math.abs(internalGet(11)), Math.abs(internalGet(10))) > MAX_HOURS) {
            millisInDay = computeMillisInDayLong();
        } else {
            millisInDay = (long) computeMillisInDay();
        }
        if (this.stamp[15] >= 2 || this.stamp[16] >= 2) {
            this.time = (millis + millisInDay) - ((long) (internalGet(15) + internalGet(16)));
        } else if (!this.lenient || this.skippedWallTime == 2) {
            int zoneOffset = computeZoneOffset(millis, millisInDay);
            long tmpTime = (millis + millisInDay) - ((long) zoneOffset);
            if (zoneOffset == this.zone.getOffset(tmpTime)) {
                this.time = tmpTime;
            } else if (this.lenient) {
                Long immediatePrevTransition = getImmediatePreviousZoneTransition(tmpTime);
                if (immediatePrevTransition != null) {
                    this.time = immediatePrevTransition.longValue();
                    return;
                }
                throw new RuntimeException("Could not locate a time zone transition before " + tmpTime);
            } else {
                throw new IllegalArgumentException("The specified wall time does not exist due to time zone offset transition.");
            }
        } else {
            this.time = (millis + millisInDay) - ((long) computeZoneOffset(millis, millisInDay));
        }
    }

    private Long getImmediatePreviousZoneTransition(long base) {
        if (this.zone instanceof BasicTimeZone) {
            TimeZoneTransition transition = ((BasicTimeZone) this.zone).getPreviousTransition(base, true);
            if (transition != null) {
                return Long.valueOf(transition.getTime());
            }
            return null;
        }
        Long transitionTime = getPreviousZoneTransitionTime(this.zone, base, 7200000);
        if (transitionTime == null) {
            return getPreviousZoneTransitionTime(this.zone, base, 108000000);
        }
        return transitionTime;
    }

    private static Long getPreviousZoneTransitionTime(TimeZone tz, long base, long duration) {
        TimeZone timeZone = tz;
        long upper = base;
        long lower = (base - duration) - 1;
        int offsetU = timeZone.getOffset(upper);
        int offsetL = timeZone.getOffset(lower);
        if (offsetU == offsetL) {
            return null;
        }
        int i = offsetL;
        return findPreviousZoneTransitionTime(timeZone, offsetU, upper, lower);
    }

    private static Long findPreviousZoneTransitionTime(TimeZone tz, int upperOffset, long upper, long lower) {
        boolean onUnitTime;
        long mid;
        long mid2;
        long mid3;
        long upper2;
        TimeZone timeZone = tz;
        int i = upperOffset;
        boolean onUnitTime2 = false;
        long mid4 = 0;
        int[] iArr = FIND_ZONE_TRANSITION_TIME_UNITS;
        int length = iArr.length;
        int i2 = 0;
        while (true) {
            if (i2 >= length) {
                onUnitTime = onUnitTime2;
                mid = mid4;
                break;
            }
            int unit = iArr[i2];
            long lunits = lower / ((long) unit);
            boolean onUnitTime3 = onUnitTime2;
            long mid5 = mid4;
            long uunits = upper / ((long) unit);
            if (uunits > lunits) {
                mid = (((lunits + uunits) + 1) >>> 1) * ((long) unit);
                onUnitTime = true;
                break;
            }
            i2++;
            onUnitTime2 = onUnitTime3;
            mid4 = mid5;
        }
        if (!onUnitTime) {
            mid2 = (upper + lower) >>> 1;
        } else {
            mid2 = mid;
        }
        if (onUnitTime) {
            if (mid2 == upper) {
                upper2 = upper;
            } else if (timeZone.getOffset(mid2) != i) {
                return findPreviousZoneTransitionTime(timeZone, i, upper, mid2);
            } else {
                upper2 = mid2;
            }
            mid3 = mid2 - 1;
        } else {
            upper2 = upper;
            mid3 = (upper + lower) >>> 1;
        }
        if (mid3 == lower) {
            return Long.valueOf(upper2);
        }
        if (timeZone.getOffset(mid3) == i) {
            return findPreviousZoneTransitionTime(timeZone, i, mid3, lower);
        }
        if (onUnitTime) {
            return Long.valueOf(upper2);
        }
        return findPreviousZoneTransitionTime(timeZone, i, upper2, mid3);
    }

    /* access modifiers changed from: protected */
    @Deprecated
    public int computeMillisInDay() {
        int millisInDay = 0;
        int hourOfDayStamp = this.stamp[11];
        int hourStamp = Math.max(this.stamp[10], this.stamp[9]);
        int bestStamp = hourStamp > hourOfDayStamp ? hourStamp : hourOfDayStamp;
        if (bestStamp != 0) {
            if (bestStamp == hourOfDayStamp) {
                millisInDay = 0 + internalGet(11);
            } else {
                millisInDay = 0 + internalGet(10) + (internalGet(9) * 12);
            }
        }
        return (((((millisInDay * 60) + internalGet(12)) * 60) + internalGet(13)) * 1000) + internalGet(14);
    }

    /* access modifiers changed from: protected */
    @Deprecated
    public long computeMillisInDayLong() {
        long millisInDay = 0;
        int hourOfDayStamp = this.stamp[11];
        int hourStamp = Math.max(this.stamp[10], this.stamp[9]);
        int bestStamp = hourStamp > hourOfDayStamp ? hourStamp : hourOfDayStamp;
        if (bestStamp != 0) {
            if (bestStamp == hourOfDayStamp) {
                millisInDay = 0 + ((long) internalGet(11));
            } else {
                millisInDay = 0 + ((long) internalGet(10)) + ((long) (internalGet(9) * 12));
            }
        }
        return (((((millisInDay * 60) + ((long) internalGet(12))) * 60) + ((long) internalGet(13))) * 1000) + ((long) internalGet(14));
    }

    /* access modifiers changed from: protected */
    @Deprecated
    public int computeZoneOffset(long millis, int millisInDay) {
        boolean sawRecentNegativeShift;
        int[] offsets = new int[2];
        long wall = millis + ((long) millisInDay);
        if (this.zone instanceof BasicTimeZone) {
            ((BasicTimeZone) this.zone).getOffsetFromLocal(wall, this.skippedWallTime == 1 ? 12 : 4, this.repeatedWallTime == 1 ? 4 : 12, offsets);
        } else {
            this.zone.getOffset(wall, true, offsets);
            if (this.repeatedWallTime == 1) {
                long tgmt = wall - ((long) (offsets[0] + offsets[1]));
                int offsetDelta = (offsets[0] + offsets[1]) - this.zone.getOffset(tgmt - 21600000);
                if (offsetDelta < 0) {
                    sawRecentNegativeShift = true;
                    long j = tgmt;
                    this.zone.getOffset(((long) offsetDelta) + wall, true, offsets);
                    if (!sawRecentNegativeShift && this.skippedWallTime == 1) {
                        this.zone.getOffset(wall - ((long) (offsets[0] + offsets[1])), false, offsets);
                    }
                }
            }
            sawRecentNegativeShift = false;
            this.zone.getOffset(wall - ((long) (offsets[0] + offsets[1])), false, offsets);
        }
        return offsets[0] + offsets[1];
    }

    /* access modifiers changed from: protected */
    @Deprecated
    public int computeZoneOffset(long millis, long millisInDay) {
        int[] offsets = new int[2];
        long wall = millis + millisInDay;
        if (this.zone instanceof BasicTimeZone) {
            ((BasicTimeZone) this.zone).getOffsetFromLocal(wall, this.skippedWallTime == 1 ? 12 : 4, this.repeatedWallTime == 1 ? 4 : 12, offsets);
        } else {
            this.zone.getOffset(wall, true, offsets);
            boolean sawRecentNegativeShift = false;
            if (this.repeatedWallTime == 1) {
                long tgmt = wall - ((long) (offsets[0] + offsets[1]));
                int offsetDelta = (offsets[0] + offsets[1]) - this.zone.getOffset(tgmt - 21600000);
                if (offsetDelta < 0) {
                    long j = tgmt;
                    this.zone.getOffset(((long) offsetDelta) + wall, true, offsets);
                    sawRecentNegativeShift = true;
                }
            }
            if (!sawRecentNegativeShift && this.skippedWallTime == 1) {
                this.zone.getOffset(wall - ((long) (offsets[0] + offsets[1])), false, offsets);
            }
        }
        return offsets[0] + offsets[1];
    }

    /* access modifiers changed from: protected */
    public int computeJulianDay() {
        if (this.stamp[20] >= 2 && newestStamp(17, 19, newestStamp(0, 8, 0)) <= this.stamp[20]) {
            return internalGet(20);
        }
        int bestField = resolveFields(getFieldResolutionTable());
        if (bestField < 0) {
            bestField = 5;
        }
        return handleComputeJulianDay(bestField);
    }

    /* access modifiers changed from: protected */
    public int[][][] getFieldResolutionTable() {
        return DATE_PRECEDENCE;
    }

    /* access modifiers changed from: protected */
    public int handleGetMonthLength(int extendedYear, int month) {
        return handleComputeMonthStart(extendedYear, month + 1, true) - handleComputeMonthStart(extendedYear, month, true);
    }

    /* access modifiers changed from: protected */
    public int handleGetYearLength(int eyear) {
        return handleComputeMonthStart(eyear + 1, 0, false) - handleComputeMonthStart(eyear, 0, false);
    }

    /* access modifiers changed from: protected */
    public int[] handleCreateFields() {
        return new int[23];
    }

    /* access modifiers changed from: protected */
    public int getDefaultMonthInYear(int extendedYear) {
        return 0;
    }

    /* access modifiers changed from: protected */
    public int getDefaultDayInMonth(int extendedYear, int month) {
        return 1;
    }

    /* access modifiers changed from: protected */
    public int handleComputeJulianDay(int bestField) {
        int year;
        int date;
        boolean useMonth = bestField == 5 || bestField == 4 || bestField == 8;
        if (bestField == 3) {
            year = internalGet(17, handleGetExtendedYear());
        } else {
            year = handleGetExtendedYear();
        }
        internalSet(19, year);
        int month = useMonth ? internalGet(2, getDefaultMonthInYear(year)) : 0;
        int julianDay = handleComputeMonthStart(year, month, useMonth);
        if (bestField == 5) {
            if (isSet(5)) {
                return internalGet(5, getDefaultDayInMonth(year, month)) + julianDay;
            }
            return getDefaultDayInMonth(year, month) + julianDay;
        } else if (bestField == 6) {
            return internalGet(6) + julianDay;
        } else {
            int firstDOW = getFirstDayOfWeek();
            int first = julianDayToDayOfWeek(julianDay + 1) - firstDOW;
            if (first < 0) {
                first += 7;
            }
            int dowLocal = 0;
            int resolveFields = resolveFields(DOW_PRECEDENCE);
            if (resolveFields == 7) {
                dowLocal = internalGet(7) - firstDOW;
            } else if (resolveFields == 18) {
                dowLocal = internalGet(18) - 1;
            }
            int dowLocal2 = dowLocal % 7;
            if (dowLocal2 < 0) {
                dowLocal2 += 7;
            }
            int date2 = (1 - first) + dowLocal2;
            if (bestField == 8) {
                if (date2 < 1) {
                    date2 += 7;
                }
                int dim = internalGet(8, 1);
                if (dim >= 0) {
                    date = date2 + (7 * (dim - 1));
                } else {
                    date = date2 + ((((handleGetMonthLength(year, internalGet(2, 0)) - date2) / 7) + dim + 1) * 7);
                }
            } else {
                if (7 - first < getMinimalDaysInFirstWeek()) {
                    date2 += 7;
                }
                date = date2 + (7 * (internalGet(bestField) - 1));
            }
            return julianDay + date;
        }
    }

    /* access modifiers changed from: protected */
    public int computeGregorianMonthStart(int year, int month) {
        boolean isLeap = false;
        if (month < 0 || month > 11) {
            int[] rem = new int[1];
            year += floorDivide(month, 12, rem);
            month = rem[0];
        }
        if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) {
            isLeap = true;
        }
        int y = year - 1;
        int julianDay = (((((365 * y) + floorDivide(y, 4)) - floorDivide(y, 100)) + floorDivide(y, 400)) + JAN_1_1_JULIAN_DAY) - 1;
        if (month == 0) {
            return julianDay;
        }
        return julianDay + GREGORIAN_MONTH_COUNT[month][isLeap ? (char) 3 : 2];
    }

    /* access modifiers changed from: protected */
    public void handleComputeFields(int julianDay) {
        internalSet(2, getGregorianMonth());
        internalSet(5, getGregorianDayOfMonth());
        internalSet(6, getGregorianDayOfYear());
        int eyear = getGregorianYear();
        internalSet(19, eyear);
        int era = 1;
        if (eyear < 1) {
            era = 0;
            eyear = 1 - eyear;
        }
        internalSet(0, era);
        internalSet(1, eyear);
    }

    /* access modifiers changed from: protected */
    public final int getGregorianYear() {
        return this.gregorianYear;
    }

    /* access modifiers changed from: protected */
    public final int getGregorianMonth() {
        return this.gregorianMonth;
    }

    /* access modifiers changed from: protected */
    public final int getGregorianDayOfYear() {
        return this.gregorianDayOfYear;
    }

    /* access modifiers changed from: protected */
    public final int getGregorianDayOfMonth() {
        return this.gregorianDayOfMonth;
    }

    public final int getFieldCount() {
        return this.fields.length;
    }

    /* access modifiers changed from: protected */
    public final void internalSet(int field, int value) {
        if (((1 << field) & this.internalSetMask) != 0) {
            this.fields[field] = value;
            this.stamp[field] = 1;
            return;
        }
        throw new IllegalStateException("Subclass cannot set " + fieldName(field));
    }

    protected static final boolean isGregorianLeapYear(int year) {
        return year % 4 == 0 && (year % 100 != 0 || year % 400 == 0);
    }

    protected static final int gregorianMonthLength(int y, int m) {
        return GREGORIAN_MONTH_COUNT[m][isGregorianLeapYear(y)];
    }

    protected static final int gregorianPreviousMonthLength(int y, int m) {
        if (m > 0) {
            return gregorianMonthLength(y, m - 1);
        }
        return 31;
    }

    protected static final long floorDivide(long numerator, long denominator) {
        if (numerator >= 0) {
            return numerator / denominator;
        }
        return ((numerator + 1) / denominator) - 1;
    }

    protected static final int floorDivide(int numerator, int denominator) {
        if (numerator >= 0) {
            return numerator / denominator;
        }
        return ((numerator + 1) / denominator) - 1;
    }

    protected static final int floorDivide(int numerator, int denominator, int[] remainder) {
        if (numerator >= 0) {
            remainder[0] = numerator % denominator;
            return numerator / denominator;
        }
        int quotient = ((numerator + 1) / denominator) - 1;
        remainder[0] = numerator - (quotient * denominator);
        return quotient;
    }

    protected static final int floorDivide(long numerator, int denominator, int[] remainder) {
        if (numerator >= 0) {
            remainder[0] = (int) (numerator % ((long) denominator));
            return (int) (numerator / ((long) denominator));
        }
        int quotient = (int) (((numerator + 1) / ((long) denominator)) - 1);
        remainder[0] = (int) (numerator - (((long) quotient) * ((long) denominator)));
        return quotient;
    }

    /* access modifiers changed from: protected */
    public String fieldName(int field) {
        try {
            return FIELD_NAME[field];
        } catch (ArrayIndexOutOfBoundsException e) {
            return "Field " + field;
        }
    }

    protected static final int millisToJulianDay(long millis) {
        return (int) (2440588 + floorDivide(millis, 86400000));
    }

    protected static final long julianDayToMillis(int julian) {
        return ((long) (julian - EPOCH_JULIAN_DAY)) * 86400000;
    }

    protected static final int julianDayToDayOfWeek(int julian) {
        int dayOfWeek = (julian + 2) % 7;
        if (dayOfWeek < 1) {
            return dayOfWeek + 7;
        }
        return dayOfWeek;
    }

    /* access modifiers changed from: protected */
    public final long internalGetTimeInMillis() {
        return this.time;
    }

    public String getType() {
        return "unknown";
    }

    @Deprecated
    public boolean haveDefaultCentury() {
        return true;
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
}
