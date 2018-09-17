package android.icu.util;

import android.icu.impl.CalendarUtil;
import android.icu.impl.Grego;
import android.icu.impl.ICUCache;
import android.icu.impl.ICUData;
import android.icu.impl.ICUResourceBundle;
import android.icu.impl.SimpleCache;
import android.icu.impl.SimpleFormatterImpl;
import android.icu.impl.SoftCache;
import android.icu.impl.locale.BaseLocale;
import android.icu.lang.UCharacter.UnicodeBlock;
import android.icu.text.DateFormat;
import android.icu.text.DateFormatSymbols;
import android.icu.text.SimpleDateFormat;
import android.icu.util.ULocale.Category;
import android.icu.util.ULocale.Type;
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
    private static final /* synthetic */ int[] -android-icu-util-Calendar$CalTypeSwitchesValues = null;
    static final /* synthetic */ boolean -assertionsDisabled = (Calendar.class.desiredAssertionStatus() ^ 1);
    public static final int AM = 0;
    public static final int AM_PM = 9;
    public static final int APRIL = 3;
    public static final int AUGUST = 7;
    @Deprecated
    protected static final int BASE_FIELD_COUNT = 23;
    public static final int DATE = 5;
    static final int[][][] DATE_PRECEDENCE;
    public static final int DAY_OF_MONTH = 5;
    public static final int DAY_OF_WEEK = 7;
    public static final int DAY_OF_WEEK_IN_MONTH = 8;
    public static final int DAY_OF_YEAR = 6;
    public static final int DECEMBER = 11;
    private static final String[] DEFAULT_PATTERNS = new String[]{"HH:mm:ss z", "HH:mm:ss z", "HH:mm:ss", "HH:mm", "EEEE, yyyy MMMM dd", "yyyy MMMM d", "yyyy MMM d", "yy/MM/dd", "{1} {0}", "{1} {0}", "{1} {0}", "{1} {0}", "{1} {0}"};
    public static final int DOW_LOCAL = 18;
    static final int[][][] DOW_PRECEDENCE;
    public static final int DST_OFFSET = 16;
    protected static final int EPOCH_JULIAN_DAY = 2440588;
    public static final int ERA = 0;
    public static final int EXTENDED_YEAR = 19;
    public static final int FEBRUARY = 1;
    private static final int FIELD_DIFF_MAX_INT = Integer.MAX_VALUE;
    private static final String[] FIELD_NAME = new String[]{"ERA", "YEAR", "MONTH", "WEEK_OF_YEAR", "WEEK_OF_MONTH", "DAY_OF_MONTH", "DAY_OF_YEAR", "DAY_OF_WEEK", "DAY_OF_WEEK_IN_MONTH", "AM_PM", "HOUR", "HOUR_OF_DAY", "MINUTE", "SECOND", "MILLISECOND", "ZONE_OFFSET", "DST_OFFSET", "YEAR_WOY", "DOW_LOCAL", "EXTENDED_YEAR", "JULIAN_DAY", "MILLISECONDS_IN_DAY"};
    private static final int[] FIND_ZONE_TRANSITION_TIME_UNITS = new int[]{3600000, 1800000, 60000, 1000};
    public static final int FRIDAY = 6;
    protected static final int GREATEST_MINIMUM = 1;
    private static final int[][] GREGORIAN_MONTH_COUNT = new int[][]{new int[]{31, 31, 0, 0}, new int[]{28, 29, 31, 31}, new int[]{31, 31, 59, 60}, new int[]{30, 30, 90, 91}, new int[]{31, 31, 120, 121}, new int[]{30, 30, 151, 152}, new int[]{31, 31, 181, 182}, new int[]{31, 31, 212, 213}, new int[]{30, 30, 243, 244}, new int[]{31, 31, UnicodeBlock.TANGUT_COMPONENTS_ID, UnicodeBlock.COUNT}, new int[]{30, 30, 304, 305}, new int[]{31, 31, 334, 335}};
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
    private static final int[][] LIMITS = new int[][]{new int[0], new int[0], new int[0], new int[0], new int[0], new int[0], new int[0], new int[]{1, 1, 7, 7}, new int[0], new int[]{0, 0, 1, 1}, new int[]{0, 0, 11, 11}, new int[]{0, 0, 23, 23}, new int[]{0, 0, 59, 59}, new int[]{0, 0, 59, 59}, new int[]{0, 0, 999, 999}, new int[]{-43200000, -43200000, 43200000, 43200000}, new int[]{0, 0, 3600000, 3600000}, new int[0], new int[]{1, 1, 7, 7}, new int[0], new int[]{MIN_JULIAN, MIN_JULIAN, 2130706432, 2130706432}, new int[]{0, 0, 86399999, 86399999}, new int[]{0, 0, 1, 1}};
    public static final int MARCH = 2;
    protected static final int MAXIMUM = 3;
    protected static final Date MAX_DATE = new Date(183882168921600000L);
    @Deprecated
    protected static final int MAX_FIELD_COUNT = 32;
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
    private static final ICUCache<String, PatternData> PATTERN_CACHE = new SimpleCache();
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

        private CalType(String id) {
            this.id = id;
        }
    }

    @Deprecated
    public static class FormatConfiguration {
        private Calendar cal;
        private DateFormatSymbols formatData;
        private ULocale loc;
        private String override;
        private String pattern;

        /* synthetic */ FormatConfiguration(FormatConfiguration -this0) {
            this();
        }

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
        private String[] overrides;
        private String[] patterns;

        public PatternData(String[] patterns, String[] overrides) {
            this.patterns = patterns;
            this.overrides = overrides;
        }

        private String getDateTimePattern(int dateStyle) {
            int glueIndex = 8;
            if (this.patterns.length >= 13) {
                glueIndex = (dateStyle + 1) + 8;
            }
            return this.patterns[glueIndex];
        }

        private static PatternData make(Calendar cal, ULocale loc) {
            return make(loc, cal.getType());
        }

        private static PatternData make(ULocale loc, String calType) {
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

        public WeekData(int fdow, int mdifw, int weekendOnset, int weekendOnsetMillis, int weekendCease, int weekendCeaseMillis) {
            this.firstDayOfWeek = fdow;
            this.minimalDaysInFirstWeek = mdifw;
            this.weekendOnset = weekendOnset;
            this.weekendOnsetMillis = weekendOnsetMillis;
            this.weekendCease = weekendCease;
            this.weekendCeaseMillis = weekendCeaseMillis;
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
            if (this.firstDayOfWeek != that.firstDayOfWeek || this.minimalDaysInFirstWeek != that.minimalDaysInFirstWeek || this.weekendOnset != that.weekendOnset || this.weekendOnsetMillis != that.weekendOnsetMillis || this.weekendCease != that.weekendCease) {
                z = false;
            } else if (this.weekendCeaseMillis != that.weekendCeaseMillis) {
                z = false;
            }
            return z;
        }

        public String toString() {
            return "{" + this.firstDayOfWeek + ", " + this.minimalDaysInFirstWeek + ", " + this.weekendOnset + ", " + this.weekendOnsetMillis + ", " + this.weekendCease + ", " + this.weekendCeaseMillis + "}";
        }
    }

    private static class WeekDataCache extends SoftCache<String, WeekData, String> {
        /* synthetic */ WeekDataCache(WeekDataCache -this0) {
            this();
        }

        private WeekDataCache() {
        }

        protected WeekData createInstance(String key, String data) {
            return Calendar.getWeekDataForRegionInternal(key);
        }
    }

    private static /* synthetic */ int[] -getandroid-icu-util-Calendar$CalTypeSwitchesValues() {
        if (-android-icu-util-Calendar$CalTypeSwitchesValues != null) {
            return -android-icu-util-Calendar$CalTypeSwitchesValues;
        }
        int[] iArr = new int[CalType.values().length];
        try {
            iArr[CalType.BUDDHIST.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[CalType.CHINESE.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[CalType.COPTIC.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[CalType.DANGI.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[CalType.ETHIOPIC.ordinal()] = 5;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[CalType.ETHIOPIC_AMETE_ALEM.ordinal()] = 6;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[CalType.GREGORIAN.ordinal()] = 7;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[CalType.HEBREW.ordinal()] = 8;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[CalType.INDIAN.ordinal()] = 9;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[CalType.ISLAMIC.ordinal()] = 10;
        } catch (NoSuchFieldError e10) {
        }
        try {
            iArr[CalType.ISLAMIC_CIVIL.ordinal()] = 11;
        } catch (NoSuchFieldError e11) {
        }
        try {
            iArr[CalType.ISLAMIC_RGSA.ordinal()] = 12;
        } catch (NoSuchFieldError e12) {
        }
        try {
            iArr[CalType.ISLAMIC_TBLA.ordinal()] = 13;
        } catch (NoSuchFieldError e13) {
        }
        try {
            iArr[CalType.ISLAMIC_UMALQURA.ordinal()] = 14;
        } catch (NoSuchFieldError e14) {
        }
        try {
            iArr[CalType.ISO8601.ordinal()] = 15;
        } catch (NoSuchFieldError e15) {
        }
        try {
            iArr[CalType.JAPANESE.ordinal()] = 16;
        } catch (NoSuchFieldError e16) {
        }
        try {
            iArr[CalType.PERSIAN.ordinal()] = 17;
        } catch (NoSuchFieldError e17) {
        }
        try {
            iArr[CalType.ROC.ordinal()] = 18;
        } catch (NoSuchFieldError e18) {
        }
        try {
            iArr[CalType.UNKNOWN.ordinal()] = 19;
        } catch (NoSuchFieldError e19) {
        }
        -android-icu-util-Calendar$CalTypeSwitchesValues = iArr;
        return iArr;
    }

    protected abstract int handleComputeMonthStart(int i, int i2, boolean z);

    protected abstract int handleGetExtendedYear();

    protected abstract int handleGetLimit(int i, int i2);

    static {
        int[][][] iArr = new int[2][][];
        int[][] iArr2 = new int[10][];
        iArr2[0] = new int[]{5};
        iArr2[1] = new int[]{3, 7};
        iArr2[2] = new int[]{4, 7};
        iArr2[3] = new int[]{8, 7};
        iArr2[4] = new int[]{3, 18};
        iArr2[5] = new int[]{4, 18};
        iArr2[6] = new int[]{8, 18};
        iArr2[7] = new int[]{6};
        iArr2[8] = new int[]{37, 1};
        iArr2[9] = new int[]{35, 17};
        iArr[0] = iArr2;
        iArr2 = new int[5][];
        iArr2[0] = new int[]{3};
        iArr2[1] = new int[]{4};
        iArr2[2] = new int[]{8};
        iArr2[3] = new int[]{40, 7};
        iArr2[4] = new int[]{40, 18};
        iArr[1] = iArr2;
        DATE_PRECEDENCE = iArr;
        iArr = new int[1][][];
        iArr2 = new int[2][];
        iArr2[0] = new int[]{7};
        iArr2[1] = new int[]{18};
        iArr[0] = iArr2;
        DOW_PRECEDENCE = iArr;
    }

    protected Calendar() {
        this(TimeZone.getDefault(), ULocale.getDefault(Category.FORMAT));
    }

    protected Calendar(TimeZone zone, Locale aLocale) {
        this(zone, ULocale.forLocale(aLocale));
    }

    protected Calendar(TimeZone zone, ULocale locale) {
        this.lenient = true;
        this.repeatedWallTime = 0;
        this.skippedWallTime = 0;
        this.nextStamp = 2;
        this.zone = zone;
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
                buf.append(BaseLocale.SEP).append(script);
            }
            String region = locale.getCountry();
            if (region.length() > 0) {
                buf.append(BaseLocale.SEP).append(region);
            }
            String calType = locale.getKeywordValue("calendar");
            if (calType != null) {
                buf.append("@calendar=").append(calType);
            }
            calLocale = new ULocale(buf.toString());
        }
        setLocale(calLocale, calLocale);
    }

    private void recalculateStamp() {
        this.nextStamp = 1;
        for (int j = 0; j < this.stamp.length; j++) {
            int currentValue = STAMP_MAX;
            int index = -1;
            int i = 0;
            while (i < this.stamp.length) {
                if (this.stamp[i] > this.nextStamp && this.stamp[i] < currentValue) {
                    currentValue = this.stamp[i];
                    index = i;
                }
                i++;
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
        if (this.fields == null || this.fields.length < 23 || this.fields.length > 32) {
            throw new IllegalStateException("Invalid fields[]");
        }
        this.stamp = new int[this.fields.length];
        int mask = 4718695;
        for (int i = 23; i < this.fields.length; i++) {
            mask |= 1 << i;
        }
        this.internalSetMask = mask;
    }

    public static Calendar getInstance() {
        return getInstanceInternal(null, null);
    }

    public static Calendar getInstance(TimeZone zone) {
        return getInstanceInternal(zone, null);
    }

    public static Calendar getInstance(Locale aLocale) {
        return getInstanceInternal(null, ULocale.forLocale(aLocale));
    }

    public static Calendar getInstance(ULocale locale) {
        return getInstanceInternal(null, locale);
    }

    public static Calendar getInstance(TimeZone zone, Locale aLocale) {
        return getInstanceInternal(zone, ULocale.forLocale(aLocale));
    }

    public static Calendar getInstance(TimeZone zone, ULocale locale) {
        return getInstanceInternal(zone, locale);
    }

    private static Calendar getInstanceInternal(TimeZone tz, ULocale locale) {
        if (locale == null) {
            locale = ULocale.getDefault(Category.FORMAT);
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
            s = s.toLowerCase(Locale.ENGLISH);
            for (CalType type : CalType.values()) {
                if (s.equals(type.id)) {
                    return type;
                }
            }
        }
        return CalType.UNKNOWN;
    }

    private static Calendar createInstance(ULocale locale) {
        TimeZone zone = TimeZone.getDefault();
        CalType calType = getCalendarTypeForLocale(locale);
        if (calType == CalType.UNKNOWN) {
            calType = CalType.GREGORIAN;
        }
        Calendar cal;
        switch (-getandroid-icu-util-Calendar$CalTypeSwitchesValues()[calType.ordinal()]) {
            case 1:
                return new BuddhistCalendar(zone, locale);
            case 2:
                return new ChineseCalendar(zone, locale);
            case 3:
                return new CopticCalendar(zone, locale);
            case 4:
                return new DangiCalendar(zone, locale);
            case 5:
                return new EthiopicCalendar(zone, locale);
            case 6:
                cal = new EthiopicCalendar(zone, locale);
                ((EthiopicCalendar) cal).setAmeteAlemEra(true);
                return cal;
            case 7:
                return new GregorianCalendar(zone, locale);
            case 8:
                return new HebrewCalendar(zone, locale);
            case 9:
                return new IndianCalendar(zone, locale);
            case 10:
            case 11:
            case 12:
            case 13:
            case 14:
                return new IslamicCalendar(zone, locale);
            case 15:
                cal = new GregorianCalendar(zone, locale);
                cal.setFirstDayOfWeek(2);
                cal.setMinimalDaysInFirstWeek(4);
                return cal;
            case 16:
                return new JapaneseCalendar(zone, locale);
            case 17:
                return new PersianCalendar(zone, locale);
            case 18:
                return new TaiwanCalendar(zone, locale);
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
        ArrayList<String> values = new ArrayList();
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
        for (Object add : caltypes) {
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

    protected final int internalGet(int field) {
        return this.fields[field];
    }

    protected final int internalGet(int field, int defaultValue) {
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
        int i = 1;
        int i2;
        if (year >= 1397) {
            i2 = ((year - 1397) / 67) * 2;
            if ((year - 1397) % 67 < 33) {
                i = 0;
            }
            shift = i2 + i;
        } else {
            i2 = (((year - 1396) / 67) - 1) * 2;
            if ((-(year - 1396)) % 67 > 33) {
                i = 0;
            }
            shift = i2 + i;
        }
        return (year + 579) - shift;
    }

    @Deprecated
    public final int getRelatedYear() {
        int year = get(19);
        CalType type = CalType.GREGORIAN;
        String typeString = getType();
        for (CalType testType : CalType.values()) {
            if (typeString.equals(testType.id)) {
                type = testType;
                break;
            }
        }
        switch (-getandroid-icu-util-Calendar$CalTypeSwitchesValues()[type.ordinal()]) {
            case 2:
                return year - 2637;
            case 3:
                return year + 284;
            case 4:
                return year - 2333;
            case 5:
                return year + 8;
            case 6:
                return year - 5492;
            case 8:
                return year - 3760;
            case 9:
                return year + 79;
            case 10:
            case 11:
            case 12:
            case 13:
            case 14:
                return gregoYearFromIslamicStart(year);
            case 17:
                return year + 622;
            default:
                return year;
        }
    }

    private static int firstIslamicStartYearFromGrego(int year) {
        int shift;
        int i = 1;
        int i2;
        if (year >= 1977) {
            i2 = ((year - 1977) / 65) * 2;
            if ((year - 1977) % 65 < 32) {
                i = 0;
            }
            shift = i2 + i;
        } else {
            i2 = (((year - 1976) / 65) - 1) * 2;
            if ((-(year - 1976)) % 65 > 32) {
                i = 0;
            }
            shift = i2 + i;
        }
        return (year - 579) + shift;
    }

    @Deprecated
    public final void setRelatedYear(int year) {
        CalType type = CalType.GREGORIAN;
        String typeString = getType();
        for (CalType testType : CalType.values()) {
            if (typeString.equals(testType.id)) {
                type = testType;
                break;
            }
        }
        switch (-getandroid-icu-util-Calendar$CalTypeSwitchesValues()[type.ordinal()]) {
            case 2:
                year += 2637;
                break;
            case 3:
                year -= 284;
                break;
            case 4:
                year += 2333;
                break;
            case 5:
                year -= 8;
                break;
            case 6:
                year += 5492;
                break;
            case 8:
                year += 3760;
                break;
            case 9:
                year -= 79;
                break;
            case 10:
            case 11:
            case 12:
            case 13:
            case 14:
                year = firstIslamicStartYearFromGrego(year);
                break;
            case 17:
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

    protected void complete() {
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
        boolean z = true;
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
        if (!isEquivalentTo(that)) {
            z = false;
        } else if (getTimeInMillis() != that.getTime().getTime()) {
            z = false;
        }
        return z;
    }

    public boolean isEquivalentTo(Calendar other) {
        return getClass() == other.getClass() && isLenient() == other.isLenient() && getFirstDayOfWeek() == other.getFirstDayOfWeek() && getMinimalDaysInFirstWeek() == other.getMinimalDaysInFirstWeek() && getTimeZone().equals(other.getTimeZone()) && getRepeatedWallTimeOption() == other.getRepeatedWallTimeOption() && getSkippedWallTimeOption() == other.getSkippedWallTimeOption();
    }

    public int hashCode() {
        return (((((this.lenient ? 1 : 0) | (this.firstDayOfWeek << 1)) | (this.minimalDaysInFirstWeek << 4)) | (this.repeatedWallTime << 7)) | (this.skippedWallTime << 9)) | (this.zone.hashCode() << 11);
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
        Calendar cal;
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
                cal = (Calendar) clone();
                cal.setLenient(true);
                cal.prepareGetActual(field, false);
                return handleGetMonthLength(cal.get(19), cal.get(2));
            case 6:
                cal = (Calendar) clone();
                cal.setLenient(true);
                cal.prepareGetActual(field, false);
                return handleGetYearLength(cal.get(19));
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

    protected void prepareGetActual(int field, boolean isMinimum) {
        set(21, 0);
        switch (field) {
            case 1:
            case 19:
                set(6, getGreatestMinimum(6));
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
            case 8:
                set(5, 1);
                set(7, get(7));
                break;
            case 17:
                set(3, getGreatestMinimum(3));
                break;
        }
        set(field, getGreatestMinimum(field));
    }

    private int getActualHelper(int field, int startValue, int endValue) {
        boolean z = true;
        if (startValue == endValue) {
            return startValue;
        }
        int delta = endValue > startValue ? 1 : -1;
        Calendar work = (Calendar) clone();
        work.complete();
        work.setLenient(true);
        if (delta >= 0) {
            z = false;
        }
        work.prepareGetActual(field, z);
        work.set(field, startValue);
        if (work.get(field) != startValue && field != 4 && delta > 0) {
            return startValue;
        }
        int result = startValue;
        while (true) {
            startValue += delta;
            work.add(field, delta);
            if (work.get(field) == startValue) {
                result = startValue;
                if (startValue == endValue) {
                    break;
                }
            } else {
                break;
            }
        }
        return result;
    }

    public final void roll(int field, boolean up) {
        roll(field, up ? 1 : -1);
    }

    public void roll(int field, int amount) {
        if (amount != 0) {
            complete();
            int gap;
            int max;
            int dow;
            int start;
            long delta;
            long min2;
            switch (field) {
                case 0:
                case 5:
                case 9:
                case 12:
                case 13:
                case 14:
                case 21:
                    int min = getActualMinimum(field);
                    gap = (getActualMaximum(field) - min) + 1;
                    int value = ((internalGet(field) + amount) - min) % gap;
                    if (value < 0) {
                        value += gap;
                    }
                    set(field, value + min);
                    return;
                case 1:
                case 17:
                    boolean era0WithYearsThatGoBackwards = false;
                    int era = get(0);
                    if (era == 0) {
                        String calType = getType();
                        if (calType.equals("gregorian") || calType.equals("roc") || calType.equals("coptic")) {
                            amount = -amount;
                            era0WithYearsThatGoBackwards = true;
                        }
                    }
                    int newYear = internalGet(field) + amount;
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
                    set(field, newYear);
                    pinField(2);
                    pinField(5);
                    return;
                case 2:
                    max = getActualMaximum(2);
                    int mon = (internalGet(2) + amount) % (max + 1);
                    if (mon < 0) {
                        mon += max + 1;
                    }
                    set(2, mon);
                    pinField(5);
                    return;
                case 3:
                    dow = internalGet(7) - getFirstDayOfWeek();
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
                    gap = ((yearLen + 7) - (((yearLen - internalGet(6)) + dow) % 7)) - start;
                    int day_of_year = ((internalGet(6) + (amount * 7)) - start) % gap;
                    if (day_of_year < 0) {
                        day_of_year += gap;
                    }
                    day_of_year += start;
                    if (day_of_year < 1) {
                        day_of_year = 1;
                    }
                    if (day_of_year > yearLen) {
                        day_of_year = yearLen;
                    }
                    set(6, day_of_year);
                    clear(2);
                    return;
                case 4:
                    dow = internalGet(7) - getFirstDayOfWeek();
                    if (dow < 0) {
                        dow += 7;
                    }
                    int fdm = ((dow - internalGet(5)) + 1) % 7;
                    if (fdm < 0) {
                        fdm += 7;
                    }
                    if (7 - fdm < getMinimalDaysInFirstWeek()) {
                        start = 8 - fdm;
                    } else {
                        start = 1 - fdm;
                    }
                    int monthLen = getActualMaximum(5);
                    gap = ((monthLen + 7) - (((monthLen - internalGet(5)) + dow) % 7)) - start;
                    int day_of_month = ((internalGet(5) + (amount * 7)) - start) % gap;
                    if (day_of_month < 0) {
                        day_of_month += gap;
                    }
                    day_of_month += start;
                    if (day_of_month < 1) {
                        day_of_month = 1;
                    }
                    if (day_of_month > monthLen) {
                        day_of_month = monthLen;
                    }
                    set(5, day_of_month);
                    return;
                case 6:
                    delta = ((long) amount) * 86400000;
                    min2 = this.time - (((long) (internalGet(6) - 1)) * 86400000);
                    int yearLength = getActualMaximum(6);
                    this.time = ((this.time + delta) - min2) % (((long) yearLength) * 86400000);
                    if (this.time < 0) {
                        this.time += ((long) yearLength) * 86400000;
                    }
                    setTimeInMillis(this.time + min2);
                    return;
                case 7:
                case 18:
                    delta = ((long) amount) * 86400000;
                    int leadDays = internalGet(field) - (field == 7 ? getFirstDayOfWeek() : 1);
                    if (leadDays < 0) {
                        leadDays += 7;
                    }
                    min2 = this.time - (((long) leadDays) * 86400000);
                    this.time = ((this.time + delta) - min2) % 604800000;
                    if (this.time < 0) {
                        this.time += 604800000;
                    }
                    setTimeInMillis(this.time + min2);
                    return;
                case 8:
                    int preWeeks = (internalGet(5) - 1) / 7;
                    min2 = this.time - (((long) preWeeks) * 604800000);
                    long gap2 = 604800000 * ((long) ((preWeeks + ((getActualMaximum(5) - internalGet(5)) / 7)) + 1));
                    this.time = ((this.time + (((long) amount) * 604800000)) - min2) % gap2;
                    if (this.time < 0) {
                        this.time += gap2;
                    }
                    setTimeInMillis(this.time + min2);
                    return;
                case 10:
                case 11:
                    long start2 = getTimeInMillis();
                    int oldHour = internalGet(field);
                    max = getMaximum(field);
                    int newHour = (oldHour + amount) % (max + 1);
                    if (newHour < 0) {
                        newHour += max + 1;
                    }
                    setTimeInMillis(((((long) newHour) - ((long) oldHour)) * RelativeDateTimeFormatter.HOUR_IN_MILLIS) + start2);
                    return;
                case 19:
                    set(field, internalGet(field) + amount);
                    pinField(2);
                    pinField(5);
                    return;
                case 20:
                    set(field, internalGet(field) + amount);
                    return;
                default:
                    throw new IllegalArgumentException("Calendar.roll(" + fieldName(field) + ") not supported");
            }
        }
    }

    /* JADX WARNING: Missing block: B:17:0x0082, code:
            r14 = isLenient();
            setLenient(true);
            set(r27, get(r27) + r28);
            pinField(5);
     */
    /* JADX WARNING: Missing block: B:18:0x00a7, code:
            if (r14 != false) goto L_0x00b1;
     */
    /* JADX WARNING: Missing block: B:19:0x00a9, code:
            complete();
            setLenient(r14);
     */
    /* JADX WARNING: Missing block: B:20:0x00b1, code:
            return;
     */
    /* JADX WARNING: Missing block: B:22:0x00b7, code:
            r15 = 0;
            r16 = 0;
     */
    /* JADX WARNING: Missing block: B:23:0x00ba, code:
            if (r11 == false) goto L_0x00dc;
     */
    /* JADX WARNING: Missing block: B:24:0x00bc, code:
            r15 = get(16) + get(15);
            r16 = get(21);
     */
    /* JADX WARNING: Missing block: B:25:0x00dc, code:
            setTimeInMillis(getTimeInMillis() + r8);
     */
    /* JADX WARNING: Missing block: B:26:0x00e9, code:
            if (r11 == false) goto L_0x0147;
     */
    /* JADX WARNING: Missing block: B:27:0x00eb, code:
            r13 = get(21);
     */
    /* JADX WARNING: Missing block: B:28:0x00f7, code:
            if (r13 == r16) goto L_0x0147;
     */
    /* JADX WARNING: Missing block: B:29:0x00f9, code:
            r18 = internalGetTimeInMillis();
            r12 = get(16) + get(15);
     */
    /* JADX WARNING: Missing block: B:30:0x0113, code:
            if (r12 == r15) goto L_0x0147;
     */
    /* JADX WARNING: Missing block: B:31:0x0115, code:
            r4 = ((long) (r15 - r12)) % 86400000;
     */
    /* JADX WARNING: Missing block: B:32:0x0125, code:
            if (r4 == 0) goto L_0x013a;
     */
    /* JADX WARNING: Missing block: B:33:0x0127, code:
            setTimeInMillis(r18 + r4);
            r13 = get(21);
     */
    /* JADX WARNING: Missing block: B:35:0x013c, code:
            if (r13 == r16) goto L_0x0147;
     */
    /* JADX WARNING: Missing block: B:37:0x0144, code:
            switch(r26.skippedWallTime) {
                case 0: goto L_0x017e;
                case 1: goto L_0x0170;
                case 2: goto L_0x018c;
                default: goto L_0x0147;
            };
     */
    /* JADX WARNING: Missing block: B:38:0x0147, code:
            return;
     */
    /* JADX WARNING: Missing block: B:46:0x0174, code:
            if (r4 <= 0) goto L_0x0147;
     */
    /* JADX WARNING: Missing block: B:47:0x0176, code:
            setTimeInMillis(r18);
     */
    /* JADX WARNING: Missing block: B:49:0x0182, code:
            if (r4 >= 0) goto L_0x0147;
     */
    /* JADX WARNING: Missing block: B:50:0x0184, code:
            setTimeInMillis(r18);
     */
    /* JADX WARNING: Missing block: B:52:0x0190, code:
            if (r4 <= 0) goto L_0x01ac;
     */
    /* JADX WARNING: Missing block: B:53:0x0192, code:
            r20 = internalGetTimeInMillis();
     */
    /* JADX WARNING: Missing block: B:54:0x0196, code:
            r10 = getImmediatePreviousZoneTransition(r20);
     */
    /* JADX WARNING: Missing block: B:55:0x019e, code:
            if (r10 == null) goto L_0x01af;
     */
    /* JADX WARNING: Missing block: B:56:0x01a0, code:
            setTimeInMillis(r10.longValue());
     */
    /* JADX WARNING: Missing block: B:57:0x01ac, code:
            r20 = r18;
     */
    /* JADX WARNING: Missing block: B:59:0x01d0, code:
            throw new java.lang.RuntimeException("Could not locate a time zone transition before " + r20);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void add(int field, int amount) {
        if (amount != 0) {
            long delta = (long) amount;
            boolean keepWallTimeInvariant = true;
            switch (field) {
                case 0:
                    set(field, get(field) + amount);
                    pinField(0);
                    return;
                case 1:
                case 17:
                    if (get(0) == 0) {
                        String calType = getType();
                        if (calType.equals("gregorian") || calType.equals("roc") || calType.equals("coptic")) {
                            amount = -amount;
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

    protected DateFormat handleGetDateFormat(String pattern, Locale locale) {
        return handleGetDateFormat(pattern, null, ULocale.forLocale(locale));
    }

    protected DateFormat handleGetDateFormat(String pattern, String override, Locale locale) {
        return handleGetDateFormat(pattern, override, ULocale.forLocale(locale));
    }

    protected DateFormat handleGetDateFormat(String pattern, ULocale locale) {
        return handleGetDateFormat(pattern, null, locale);
    }

    protected DateFormat handleGetDateFormat(String pattern, String override, ULocale locale) {
        FormatConfiguration fmtConfig = new FormatConfiguration();
        fmtConfig.pattern = pattern;
        fmtConfig.override = override;
        fmtConfig.formatData = new DateFormatSymbols(this, locale);
        fmtConfig.loc = locale;
        fmtConfig.cal = this;
        return SimpleDateFormat.getInstance(fmtConfig);
    }

    private static DateFormat formatHelper(Calendar cal, ULocale loc, int dateStyle, int timeStyle) {
        if (timeStyle < -1 || timeStyle > 3) {
            throw new IllegalArgumentException("Illegal time style " + timeStyle);
        } else if (dateStyle < -1 || dateStyle > 3) {
            throw new IllegalArgumentException("Illegal date style " + dateStyle);
        } else {
            String pattern;
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

    private static PatternData getPatternData(ULocale locale, String calType) {
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
            switch (concatenationPatternRb.getType()) {
                case 0:
                    dateTimePatterns[i] = concatenationPatternRb.getString();
                    break;
                case 8:
                    dateTimePatterns[i] = concatenationPatternRb.getString(0);
                    dateTimePatternsOverrides[i] = concatenationPatternRb.getString(1);
                    break;
                default:
                    break;
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
        int inQuotes = 0;
        char prevChar = ' ';
        StringBuilder result = new StringBuilder();
        StringCharacterIterator it = new StringCharacterIterator(pattern);
        char c = it.first();
        while (c != 65535) {
            if (c == '\'') {
                inQuotes ^= 1;
            } else if (inQuotes == 0 && c != prevChar) {
                if (result.length() > 0) {
                    result.append(";");
                }
                result.append(c);
                result.append("=");
                result.append(override);
            }
            prevChar = c;
            c = it.next();
        }
        return result.toString();
    }

    protected void pinField(int field) {
        int max = getActualMaximum(field);
        int min = getActualMinimum(field);
        if (this.fields[field] > max) {
            set(field, max);
        } else if (this.fields[field] < min) {
            set(field, min);
        }
    }

    protected int weekNumber(int desiredDay, int dayOfPeriod, int dayOfWeek) {
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

    protected final int weekNumber(int dayOfPeriod, int dayOfWeek) {
        return weekNumber(dayOfPeriod, dayOfPeriod, dayOfWeek);
    }

    public int fieldDifference(Date when, int field) {
        int min = 0;
        long startMs = getTimeInMillis();
        long targetMs = when.getTime();
        int max;
        long ms;
        int t;
        if (startMs < targetMs) {
            max = 1;
            while (true) {
                setTimeInMillis(startMs);
                add(field, max);
                ms = getTimeInMillis();
                if (ms == targetMs) {
                    return max;
                }
                if (ms > targetMs) {
                    while (max - min > 1) {
                        t = min + ((max - min) / 2);
                        setTimeInMillis(startMs);
                        add(field, t);
                        ms = getTimeInMillis();
                        if (ms == targetMs) {
                            return t;
                        }
                        if (ms > targetMs) {
                            max = t;
                        } else {
                            min = t;
                        }
                    }
                } else if (max < Integer.MAX_VALUE) {
                    min = max;
                    max <<= 1;
                    if (max < 0) {
                        max = Integer.MAX_VALUE;
                    }
                } else {
                    throw new RuntimeException();
                }
            }
        } else if (startMs > targetMs) {
            max = -1;
            do {
                setTimeInMillis(startMs);
                add(field, max);
                ms = getTimeInMillis();
                if (ms == targetMs) {
                    return max;
                }
                if (ms < targetMs) {
                    while (min - max > 1) {
                        t = min + ((max - min) / 2);
                        setTimeInMillis(startMs);
                        add(field, t);
                        ms = getTimeInMillis();
                        if (ms == targetMs) {
                            return t;
                        }
                        if (ms < targetMs) {
                            max = t;
                        } else {
                            min = t;
                        }
                    }
                } else {
                    min = max;
                    max <<= 1;
                }
            } while (max != 0);
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

    public void setLenient(boolean lenient) {
        this.lenient = lenient;
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

    protected int getLimit(int field, int limitType) {
        switch (field) {
            case 4:
                int limit;
                if (limitType == 0) {
                    if (getMinimalDaysInFirstWeek() == 1) {
                        limit = 1;
                    } else {
                        limit = 0;
                    }
                } else if (limitType == 1) {
                    limit = 1;
                } else {
                    int minDaysInFirst = getMinimalDaysInFirstWeek();
                    int daysInMonth = handleGetLimit(5, limitType);
                    if (limitType == 2) {
                        limit = ((7 - minDaysInFirst) + daysInMonth) / 7;
                    } else {
                        limit = ((daysInMonth + 6) + (7 - minDaysInFirst)) / 7;
                    }
                }
                return limit;
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
                if (this.weekendCeaseMillis < Grego.MILLIS_PER_DAY) {
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
        boolean z = true;
        int dow = get(7);
        int dowt = getDayOfWeekType(dow);
        switch (dowt) {
            case 0:
                return false;
            case 1:
                return true;
            default:
                int millisInDay = internalGet(14) + ((internalGet(13) + ((internalGet(12) + (internalGet(11) * 60)) * 60)) * 1000);
                int transition = getWeekendTransition(dow);
                if (dowt == 2) {
                    if (millisInDay < transition) {
                        z = false;
                    }
                } else if (millisInDay >= transition) {
                    z = false;
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
        } catch (Throwable e) {
            throw new ICUCloneNotSupportedException(e);
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
            buffer.append(',').append(fieldName(i)).append('=');
            buffer.append(isSet(i) ? String.valueOf(this.fields[i]) : "?");
        }
        buffer.append(']');
        return buffer.toString();
    }

    public static WeekData getWeekDataForRegion(String region) {
        return WEEK_DATA_CACHE.createInstance(region, region);
    }

    public WeekData getWeekData() {
        return new WeekData(this.firstDayOfWeek, this.minimalDaysInFirstWeek, this.weekendOnset, this.weekendOnsetMillis, this.weekendCease, this.weekendCeaseMillis);
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

    private static WeekData getWeekDataForRegionInternal(String region) {
        UResourceBundle weekDataBundle;
        if (region == null) {
            region = "001";
        }
        UResourceBundle weekDataInfo = UResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME, "supplementalData", ICUResourceBundle.ICU_DATA_CLASS_LOADER).get("weekData");
        try {
            weekDataBundle = weekDataInfo.get(region);
        } catch (MissingResourceException mre) {
            if (region.equals("001")) {
                throw mre;
            }
            weekDataBundle = weekDataInfo.get("001");
        }
        int[] wdi = weekDataBundle.getIntVector();
        return new WeekData(wdi[0], wdi[1], wdi[2], wdi[3], wdi[4], wdi[5]);
    }

    private void setWeekData(String region) {
        if (region == null) {
            region = "001";
        }
        setWeekData((WeekData) WEEK_DATA_CACHE.getInstance(region, region));
    }

    private void updateTime() {
        computeTime();
        if (isLenient() || (this.areAllFieldsSet ^ 1) != 0) {
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

    protected void computeFields() {
        int[] offsets = new int[2];
        getTimeZone().getOffset(this.time, false, offsets);
        long localMillis = (this.time + ((long) offsets[0])) + ((long) offsets[1]);
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
        millisInDay /= 1000;
        this.fields[13] = millisInDay % 60;
        millisInDay /= 60;
        this.fields[12] = millisInDay % 60;
        millisInDay /= 60;
        this.fields[11] = millisInDay;
        this.fields[9] = millisInDay / 12;
        this.fields[10] = millisInDay % 12;
        this.fields[15] = offsets[0];
        this.fields[16] = offsets[1];
    }

    private final void computeGregorianAndDOWFields(int julianDay) {
        computeGregorianFields(julianDay);
        int dow = julianDayToDayOfWeek(julianDay);
        this.fields[7] = dow;
        int dowLocal = (dow - getFirstDayOfWeek()) + 1;
        if (dowLocal < 1) {
            dowLocal += 7;
        }
        this.fields[18] = dowLocal;
    }

    protected final void computeGregorianFields(int julianDay) {
        int[] rem = new int[1];
        int n400 = floorDivide((long) (julianDay - JAN_1_1_JULIAN_DAY), 146097, rem);
        int n100 = floorDivide(rem[0], 36524, rem);
        int n4 = floorDivide(rem[0], 1461, rem);
        int n1 = floorDivide(rem[0], 365, rem);
        int year = (((n400 * 400) + (n100 * 100)) + (n4 * 4)) + n1;
        int dayOfYear = rem[0];
        if (n100 == 4 || n1 == 4) {
            dayOfYear = 365;
        } else {
            year++;
        }
        boolean isLeap = (year & 3) == 0 ? year % 100 != 0 || year % 400 == 0 : false;
        int correction = 0;
        if (dayOfYear >= (isLeap ? 60 : 59)) {
            correction = isLeap ? 1 : 2;
        }
        int month = (((dayOfYear + correction) * 12) + 6) / 367;
        int dayOfMonth = (dayOfYear - GREGORIAN_MONTH_COUNT[month][isLeap ? 3 : 2]) + 1;
        this.gregorianYear = year;
        this.gregorianMonth = month;
        this.gregorianDayOfMonth = dayOfMonth;
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
            woy = weekNumber(dayOfYear + handleGetYearLength(eyear - 1), dayOfWeek);
            yearOfWeekOfYear = eyear - 1;
        } else {
            int lastDoy = handleGetYearLength(eyear);
            if (dayOfYear >= lastDoy - 5) {
                int lastRelDow = ((relDow + lastDoy) - dayOfYear) % 7;
                if (lastRelDow < 0) {
                    lastRelDow += 7;
                }
                if (6 - lastRelDow >= getMinimalDaysInFirstWeek() && (dayOfYear + 7) - relDow > lastDoy) {
                    woy = 1;
                    yearOfWeekOfYear = eyear + 1;
                }
            }
        }
        this.fields[3] = woy;
        this.fields[17] = yearOfWeekOfYear;
        int dayOfMonth = this.fields[5];
        this.fields[4] = weekNumber(dayOfMonth, dayOfWeek);
        this.fields[8] = ((dayOfMonth - 1) / 7) + 1;
    }

    protected int resolveFields(int[][][] precedenceTable) {
        int bestField = -1;
        for (int g = 0; g < precedenceTable.length && bestField < 0; g++) {
            int[][] group = precedenceTable[g];
            int bestStamp = 0;
            for (int[] line : group) {
                int lineStamp = 0;
                int i = line[0] >= 32 ? 1 : 0;
                while (i < line.length) {
                    int s = this.stamp[line[i]];
                    if (s == 0) {
                        break;
                    }
                    lineStamp = Math.max(lineStamp, s);
                    i++;
                }
                if (lineStamp > bestStamp) {
                    int tempBestField = line[0];
                    if (tempBestField >= 32) {
                        tempBestField &= 31;
                        if (tempBestField != 5 || this.stamp[4] < this.stamp[tempBestField]) {
                            bestField = tempBestField;
                        }
                    } else {
                        bestField = tempBestField;
                    }
                    if (bestField == tempBestField) {
                        bestStamp = lineStamp;
                    }
                }
            }
        }
        return bestField >= 32 ? bestField & 31 : bestField;
    }

    protected int newestStamp(int first, int last, int bestStampSoFar) {
        int bestStamp = bestStampSoFar;
        for (int i = first; i <= last; i++) {
            if (this.stamp[i] > bestStamp) {
                bestStamp = this.stamp[i];
            }
        }
        return bestStamp;
    }

    protected final int getStamp(int field) {
        return this.stamp[field];
    }

    protected int newerField(int defaultField, int alternateField) {
        if (this.stamp[alternateField] > this.stamp[defaultField]) {
            return alternateField;
        }
        return defaultField;
    }

    protected void validateFields() {
        for (int field = 0; field < this.fields.length; field++) {
            if (this.stamp[field] >= 2) {
                validateField(field);
            }
        }
    }

    protected void validateField(int field) {
        switch (field) {
            case 5:
                validateField(field, 1, handleGetMonthLength(handleGetExtendedYear(), internalGet(2)));
                return;
            case 6:
                validateField(field, 1, handleGetYearLength(handleGetExtendedYear()));
                return;
            case 8:
                if (internalGet(field) == 0) {
                    throw new IllegalArgumentException("DAY_OF_WEEK_IN_MONTH cannot be zero");
                }
                validateField(field, getMinimum(field), getMaximum(field));
                return;
            default:
                validateField(field, getMinimum(field), getMaximum(field));
                return;
        }
    }

    protected final void validateField(int field, int min, int max) {
        int value = this.fields[field];
        if (value < min || value > max) {
            throw new IllegalArgumentException(fieldName(field) + '=' + value + ", valid range=" + min + ".." + max);
        }
    }

    protected void computeTime() {
        int millisInDay;
        if (!isLenient()) {
            validateFields();
        }
        long millis = julianDayToMillis(computeJulianDay());
        if (this.stamp[21] < 2 || newestStamp(9, 14, 0) > this.stamp[21]) {
            millisInDay = computeMillisInDay();
        } else {
            millisInDay = internalGet(21);
        }
        if (this.stamp[15] >= 2 || this.stamp[16] >= 2) {
            this.time = (((long) millisInDay) + millis) - ((long) (internalGet(15) + internalGet(16)));
        } else if (!this.lenient || this.skippedWallTime == 2) {
            int zoneOffset = computeZoneOffset(millis, millisInDay);
            long tmpTime = (((long) millisInDay) + millis) - ((long) zoneOffset);
            if (zoneOffset == this.zone.getOffset(tmpTime)) {
                this.time = tmpTime;
            } else if (!this.lenient) {
                throw new IllegalArgumentException("The specified wall time does not exist due to time zone offset transition.");
            } else if (-assertionsDisabled || this.skippedWallTime == 2) {
                Long immediatePrevTransition = getImmediatePreviousZoneTransition(tmpTime);
                if (immediatePrevTransition == null) {
                    throw new RuntimeException("Could not locate a time zone transition before " + tmpTime);
                }
                this.time = immediatePrevTransition.longValue();
            } else {
                throw new AssertionError(Integer.valueOf(this.skippedWallTime));
            }
        } else {
            this.time = (((long) millisInDay) + millis) - ((long) computeZoneOffset(millis, millisInDay));
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
        if (-assertionsDisabled || duration > 0) {
            long upper = base;
            long lower = (base - duration) - 1;
            int offsetU = tz.getOffset(base);
            if (offsetU == tz.getOffset(lower)) {
                return null;
            }
            return findPreviousZoneTransitionTime(tz, offsetU, base, lower);
        }
        throw new AssertionError();
    }

    private static Long findPreviousZoneTransitionTime(TimeZone tz, int upperOffset, long upper, long lower) {
        boolean onUnitTime = false;
        long mid = 0;
        for (int unit : FIND_ZONE_TRANSITION_TIME_UNITS) {
            long lunits = lower / ((long) unit);
            long uunits = upper / ((long) unit);
            if (uunits > lunits) {
                mid = (((lunits + uunits) + 1) >>> 1) * ((long) unit);
                onUnitTime = true;
                break;
            }
        }
        if (!onUnitTime) {
            mid = (upper + lower) >>> 1;
        }
        if (onUnitTime) {
            if (mid != upper) {
                if (tz.getOffset(mid) != upperOffset) {
                    return findPreviousZoneTransitionTime(tz, upperOffset, upper, mid);
                }
                upper = mid;
            }
            mid--;
        } else {
            mid = (upper + lower) >>> 1;
        }
        if (mid == lower) {
            return Long.valueOf(upper);
        }
        if (tz.getOffset(mid) == upperOffset) {
            return findPreviousZoneTransitionTime(tz, upperOffset, mid, lower);
        }
        if (onUnitTime) {
            return Long.valueOf(upper);
        }
        return findPreviousZoneTransitionTime(tz, upperOffset, upper, mid);
    }

    protected int computeMillisInDay() {
        int millisInDay = 0;
        int hourOfDayStamp = this.stamp[11];
        int hourStamp = Math.max(this.stamp[10], this.stamp[9]);
        int bestStamp = hourStamp > hourOfDayStamp ? hourStamp : hourOfDayStamp;
        if (bestStamp != 0) {
            if (bestStamp == hourOfDayStamp) {
                millisInDay = internalGet(11) + 0;
            } else {
                millisInDay = (internalGet(10) + 0) + (internalGet(9) * 12);
            }
        }
        return (((((millisInDay * 60) + internalGet(12)) * 60) + internalGet(13)) * 1000) + internalGet(14);
    }

    protected int computeZoneOffset(long millis, int millisInDay) {
        int[] offsets = new int[2];
        long wall = millis + ((long) millisInDay);
        if (this.zone instanceof BasicTimeZone) {
            ((BasicTimeZone) this.zone).getOffsetFromLocal(wall, this.skippedWallTime == 1 ? 12 : 4, this.repeatedWallTime == 1 ? 4 : 12, offsets);
        } else {
            this.zone.getOffset(wall, true, offsets);
            boolean sawRecentNegativeShift = false;
            if (this.repeatedWallTime == 1) {
                int offsetDelta = (offsets[0] + offsets[1]) - this.zone.getOffset((wall - ((long) (offsets[0] + offsets[1]))) - 21600000);
                if (!-assertionsDisabled && offsetDelta <= -21600000) {
                    throw new AssertionError(Integer.valueOf(offsetDelta));
                } else if (offsetDelta < 0) {
                    sawRecentNegativeShift = true;
                    this.zone.getOffset(((long) offsetDelta) + wall, true, offsets);
                }
            }
            if (!sawRecentNegativeShift && this.skippedWallTime == 1) {
                this.zone.getOffset(wall - ((long) (offsets[0] + offsets[1])), false, offsets);
            }
        }
        return offsets[0] + offsets[1];
    }

    protected int computeJulianDay() {
        if (this.stamp[20] >= 2 && newestStamp(17, 19, newestStamp(0, 8, 0)) <= this.stamp[20]) {
            return internalGet(20);
        }
        int bestField = resolveFields(getFieldResolutionTable());
        if (bestField < 0) {
            bestField = 5;
        }
        return handleComputeJulianDay(bestField);
    }

    protected int[][][] getFieldResolutionTable() {
        return DATE_PRECEDENCE;
    }

    protected int handleGetMonthLength(int extendedYear, int month) {
        return handleComputeMonthStart(extendedYear, month + 1, true) - handleComputeMonthStart(extendedYear, month, true);
    }

    protected int handleGetYearLength(int eyear) {
        return handleComputeMonthStart(eyear + 1, 0, false) - handleComputeMonthStart(eyear, 0, false);
    }

    protected int[] handleCreateFields() {
        return new int[23];
    }

    protected int getDefaultMonthInYear(int extendedYear) {
        return 0;
    }

    protected int getDefaultDayInMonth(int extendedYear, int month) {
        return 1;
    }

    protected int handleComputeJulianDay(int bestField) {
        int year;
        boolean useMonth = (bestField == 5 || bestField == 4) ? true : bestField == 8;
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
            switch (resolveFields(DOW_PRECEDENCE)) {
                case 7:
                    dowLocal = internalGet(7) - firstDOW;
                    break;
                case 18:
                    dowLocal = internalGet(18) - 1;
                    break;
            }
            dowLocal %= 7;
            if (dowLocal < 0) {
                dowLocal += 7;
            }
            int date = (1 - first) + dowLocal;
            if (bestField == 8) {
                if (date < 1) {
                    date += 7;
                }
                int dim = internalGet(8, 1);
                if (dim >= 0) {
                    date += (dim - 1) * 7;
                } else {
                    date += ((((handleGetMonthLength(year, internalGet(2, 0)) - date) / 7) + dim) + 1) * 7;
                }
            } else {
                if (7 - first < getMinimalDaysInFirstWeek()) {
                    date += 7;
                }
                date += (internalGet(bestField) - 1) * 7;
            }
            return julianDay + date;
        }
    }

    protected int computeGregorianMonthStart(int year, int month) {
        if (month < 0 || month > 11) {
            int[] rem = new int[1];
            year += floorDivide(month, 12, rem);
            month = rem[0];
        }
        boolean isLeap = year % 4 == 0 && (year % 100 != 0 || year % 400 == 0);
        int y = year - 1;
        int julianDay = (((((y * 365) + floorDivide(y, 4)) - floorDivide(y, 100)) + floorDivide(y, 400)) + JAN_1_1_JULIAN_DAY) - 1;
        if (month == 0) {
            return julianDay;
        }
        return julianDay + GREGORIAN_MONTH_COUNT[month][isLeap ? 3 : 2];
    }

    protected void handleComputeFields(int julianDay) {
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

    protected final int getGregorianYear() {
        return this.gregorianYear;
    }

    protected final int getGregorianMonth() {
        return this.gregorianMonth;
    }

    protected final int getGregorianDayOfYear() {
        return this.gregorianDayOfYear;
    }

    protected final int getGregorianDayOfMonth() {
        return this.gregorianDayOfMonth;
    }

    public final int getFieldCount() {
        return this.fields.length;
    }

    protected final void internalSet(int field, int value) {
        if (((1 << field) & this.internalSetMask) == 0) {
            throw new IllegalStateException("Subclass cannot set " + fieldName(field));
        }
        this.fields[field] = value;
        this.stamp[field] = 1;
    }

    protected static final boolean isGregorianLeapYear(int year) {
        return year % 4 == 0 && (year % 100 != 0 || year % 400 == 0);
    }

    protected static final int gregorianMonthLength(int y, int m) {
        return GREGORIAN_MONTH_COUNT[m][isGregorianLeapYear(y) ? 1 : 0];
    }

    protected static final int gregorianPreviousMonthLength(int y, int m) {
        return m > 0 ? gregorianMonthLength(y, m - 1) : 31;
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

    protected String fieldName(int field) {
        try {
            return FIELD_NAME[field];
        } catch (ArrayIndexOutOfBoundsException e) {
            return "Field " + field;
        }
    }

    protected static final int millisToJulianDay(long millis) {
        return (int) (floorDivide(millis, 86400000) + 2440588);
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

    protected final long internalGetTimeInMillis() {
        return this.time;
    }

    public String getType() {
        return "unknown";
    }

    @Deprecated
    public boolean haveDefaultCentury() {
        return true;
    }

    public final ULocale getLocale(Type type) {
        return type == ULocale.ACTUAL_LOCALE ? this.actualLocale : this.validLocale;
    }

    final void setLocale(ULocale valid, ULocale actual) {
        Object obj;
        Object obj2 = 1;
        if (valid == null) {
            obj = 1;
        } else {
            obj = null;
        }
        if (actual != null) {
            obj2 = null;
        }
        if (obj != obj2) {
            throw new IllegalArgumentException();
        }
        this.validLocale = valid;
        this.actualLocale = actual;
    }
}
