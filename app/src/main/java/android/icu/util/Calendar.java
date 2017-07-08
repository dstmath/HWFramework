package android.icu.util;

import android.icu.impl.CalendarData;
import android.icu.impl.CalendarUtil;
import android.icu.impl.Grego;
import android.icu.impl.ICUCache;
import android.icu.impl.ICUResourceBundle;
import android.icu.impl.SoftCache;
import android.icu.impl.locale.BaseLocale;
import android.icu.text.BreakIterator;
import android.icu.text.DateFormat;
import android.icu.text.DateFormatSymbols;
import android.icu.text.MessageFormat;
import android.icu.text.SimpleDateFormat;
import android.icu.text.UnicodeMatcher;
import android.icu.util.ULocale.Category;
import android.icu.util.ULocale.Type;
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
    static final /* synthetic */ boolean -assertionsDisabled = false;
    public static final int AM = 0;
    public static final int AM_PM = 9;
    public static final int APRIL = 3;
    public static final int AUGUST = 7;
    protected static final int BASE_FIELD_COUNT = 23;
    public static final int DATE = 5;
    static final int[][][] DATE_PRECEDENCE = null;
    public static final int DAY_OF_MONTH = 5;
    public static final int DAY_OF_WEEK = 7;
    public static final int DAY_OF_WEEK_IN_MONTH = 8;
    public static final int DAY_OF_YEAR = 6;
    public static final int DECEMBER = 11;
    private static final String[] DEFAULT_PATTERNS = null;
    public static final int DOW_LOCAL = 18;
    static final int[][][] DOW_PRECEDENCE = null;
    public static final int DST_OFFSET = 16;
    protected static final int EPOCH_JULIAN_DAY = 2440588;
    public static final int ERA = 0;
    public static final int EXTENDED_YEAR = 19;
    public static final int FEBRUARY = 1;
    private static final int FIELD_DIFF_MAX_INT = Integer.MAX_VALUE;
    private static final String[] FIELD_NAME = null;
    private static final int[] FIND_ZONE_TRANSITION_TIME_UNITS = null;
    public static final int FRIDAY = 6;
    protected static final int GREATEST_MINIMUM = 1;
    private static final int[][] GREGORIAN_MONTH_COUNT = null;
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
    private static final int[][] LIMITS = null;
    public static final int MARCH = 2;
    protected static final int MAXIMUM = 3;
    protected static final Date MAX_DATE = null;
    protected static final int MAX_FIELD_COUNT = 32;
    protected static final int MAX_JULIAN = 2130706432;
    protected static final long MAX_MILLIS = 183882168921600000L;
    public static final int MAY = 4;
    public static final int MILLISECOND = 14;
    public static final int MILLISECONDS_IN_DAY = 21;
    protected static final int MINIMUM = 0;
    protected static final int MINIMUM_USER_STAMP = 2;
    public static final int MINUTE = 12;
    protected static final Date MIN_DATE = null;
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
    private static final ICUCache<String, PatternData> PATTERN_CACHE = null;
    public static final int PM = 1;
    private static final char QUOTE = '\'';
    protected static final int RESOLVE_REMAP = 32;
    public static final int SATURDAY = 7;
    public static final int SECOND = 13;
    public static final int SEPTEMBER = 8;
    private static int STAMP_MAX = 0;
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
    private static final WeekDataCache WEEK_DATA_CACHE = null;
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
        ;
        
        String id;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.util.Calendar.CalType.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.util.Calendar.CalType.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.icu.util.Calendar.CalType.<clinit>():void");
        }

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

        /* synthetic */ FormatConfiguration(FormatConfiguration formatConfiguration) {
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
            int glueIndex = Calendar.SEPTEMBER;
            if (this.patterns.length >= Calendar.SECOND) {
                glueIndex = (dateStyle + Calendar.YEAR) + Calendar.SEPTEMBER;
            }
            return this.patterns[glueIndex];
        }

        private static PatternData make(Calendar cal, ULocale loc) {
            String calType = cal.getType();
            String key = loc.getBaseName() + "+" + calType;
            PatternData patternData = (PatternData) Calendar.PATTERN_CACHE.get(key);
            if (patternData == null) {
                try {
                    CalendarData calData = new CalendarData(loc, calType);
                    patternData = new PatternData(calData.getDateTimePatterns(), calData.getOverrides());
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
                return Calendar.-assertionsDisabled;
            }
            WeekData that = (WeekData) other;
            if (this.firstDayOfWeek != that.firstDayOfWeek || this.minimalDaysInFirstWeek != that.minimalDaysInFirstWeek || this.weekendOnset != that.weekendOnset || this.weekendOnsetMillis != that.weekendOnsetMillis || this.weekendCease != that.weekendCease) {
                z = Calendar.-assertionsDisabled;
            } else if (this.weekendCeaseMillis != that.weekendCeaseMillis) {
                z = Calendar.-assertionsDisabled;
            }
            return z;
        }

        public String toString() {
            return "{" + this.firstDayOfWeek + ", " + this.minimalDaysInFirstWeek + ", " + this.weekendOnset + ", " + this.weekendOnsetMillis + ", " + this.weekendCease + ", " + this.weekendCeaseMillis + "}";
        }
    }

    private static class WeekDataCache extends SoftCache<String, WeekData, String> {
        /* synthetic */ WeekDataCache(WeekDataCache weekDataCache) {
            this();
        }

        private WeekDataCache() {
        }

        protected /* bridge */ /* synthetic */ Object createInstance(Object key, Object data) {
            return createInstance((String) key, (String) data);
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
            iArr[CalType.BUDDHIST.ordinal()] = YEAR;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[CalType.CHINESE.ordinal()] = WEEKEND_ONSET;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[CalType.COPTIC.ordinal()] = WEEK_OF_YEAR;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[CalType.DANGI.ordinal()] = WEEK_OF_MONTH;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[CalType.ETHIOPIC.ordinal()] = THURSDAY;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[CalType.ETHIOPIC_AMETE_ALEM.ordinal()] = JULY;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[CalType.GREGORIAN.ordinal()] = SATURDAY;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[CalType.HEBREW.ordinal()] = SEPTEMBER;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[CalType.INDIAN.ordinal()] = OCTOBER;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[CalType.ISLAMIC.ordinal()] = NOVEMBER;
        } catch (NoSuchFieldError e10) {
        }
        try {
            iArr[CalType.ISLAMIC_CIVIL.ordinal()] = HOUR_OF_DAY;
        } catch (NoSuchFieldError e11) {
        }
        try {
            iArr[CalType.ISLAMIC_RGSA.ordinal()] = UNDECIMBER;
        } catch (NoSuchFieldError e12) {
        }
        try {
            iArr[CalType.ISLAMIC_TBLA.ordinal()] = SECOND;
        } catch (NoSuchFieldError e13) {
        }
        try {
            iArr[CalType.ISLAMIC_UMALQURA.ordinal()] = MILLISECOND;
        } catch (NoSuchFieldError e14) {
        }
        try {
            iArr[CalType.ISO8601.ordinal()] = ZONE_OFFSET;
        } catch (NoSuchFieldError e15) {
        }
        try {
            iArr[CalType.JAPANESE.ordinal()] = DST_OFFSET;
        } catch (NoSuchFieldError e16) {
        }
        try {
            iArr[CalType.PERSIAN.ordinal()] = YEAR_WOY;
        } catch (NoSuchFieldError e17) {
        }
        try {
            iArr[CalType.ROC.ordinal()] = DOW_LOCAL;
        } catch (NoSuchFieldError e18) {
        }
        try {
            iArr[CalType.UNKNOWN.ordinal()] = EXTENDED_YEAR;
        } catch (NoSuchFieldError e19) {
        }
        -android-icu-util-Calendar$CalTypeSwitchesValues = iArr;
        return iArr;
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.util.Calendar.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.util.Calendar.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.icu.util.Calendar.<clinit>():void");
    }

    protected abstract int handleComputeMonthStart(int i, int i2, boolean z);

    protected abstract int handleGetExtendedYear();

    protected abstract int handleGetLimit(int i, int i2);

    protected Calendar() {
        this(TimeZone.getDefault(), ULocale.getDefault(Category.FORMAT));
    }

    protected Calendar(TimeZone zone, Locale aLocale) {
        this(zone, ULocale.forLocale(aLocale));
    }

    protected Calendar(TimeZone zone, ULocale locale) {
        this.lenient = true;
        this.repeatedWallTime = WEEKDAY;
        this.skippedWallTime = WEEKDAY;
        this.nextStamp = WEEKEND_ONSET;
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
        this.nextStamp = YEAR;
        for (int j = WEEKDAY; j < this.stamp.length; j += YEAR) {
            int currentValue = STAMP_MAX;
            int index = -1;
            int i = WEEKDAY;
            while (i < this.stamp.length) {
                if (this.stamp[i] > this.nextStamp && this.stamp[i] < currentValue) {
                    currentValue = this.stamp[i];
                    index = i;
                }
                i += YEAR;
            }
            if (index < 0) {
                break;
            }
            int[] iArr = this.stamp;
            int i2 = this.nextStamp + YEAR;
            this.nextStamp = i2;
            iArr[index] = i2;
        }
        this.nextStamp += YEAR;
    }

    private void initInternal() {
        this.fields = handleCreateFields();
        if (this.fields == null || this.fields.length < BASE_FIELD_COUNT || this.fields.length > RESOLVE_REMAP) {
            throw new IllegalStateException("Invalid fields[]");
        }
        this.stamp = new int[this.fields.length];
        int mask = 4718695;
        for (int i = BASE_FIELD_COUNT; i < this.fields.length; i += YEAR) {
            mask |= YEAR << i;
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
        String region = loc.getCountry();
        if (region.length() != 0) {
            return region;
        }
        region = ULocale.addLikelySubtags(loc).getCountry();
        if (region.length() == 0) {
            return "001";
        }
        return region;
    }

    private static CalType getCalendarTypeForLocale(ULocale l) {
        String s = CalendarUtil.getCalendarType(l);
        if (s != null) {
            s = s.toLowerCase(Locale.ENGLISH);
            CalType[] values = CalType.values();
            int length = values.length;
            for (int i = WEEKDAY; i < length; i += YEAR) {
                CalType type = values[i];
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
            case YEAR /*1*/:
                return new BuddhistCalendar(zone, locale);
            case WEEKEND_ONSET /*2*/:
                return new ChineseCalendar(zone, locale);
            case WEEK_OF_YEAR /*3*/:
                return new CopticCalendar(zone, locale);
            case WEEK_OF_MONTH /*4*/:
                return new DangiCalendar(zone, locale);
            case THURSDAY /*5*/:
                return new EthiopicCalendar(zone, locale);
            case JULY /*6*/:
                cal = new EthiopicCalendar(zone, locale);
                ((EthiopicCalendar) cal).setAmeteAlemEra(true);
                return cal;
            case SATURDAY /*7*/:
                return new GregorianCalendar(zone, locale);
            case SEPTEMBER /*8*/:
                return new HebrewCalendar(zone, locale);
            case OCTOBER /*9*/:
                return new IndianCalendar(zone, locale);
            case NOVEMBER /*10*/:
            case HOUR_OF_DAY /*11*/:
            case UNDECIMBER /*12*/:
            case SECOND /*13*/:
            case MILLISECOND /*14*/:
                return new IslamicCalendar(zone, locale);
            case ZONE_OFFSET /*15*/:
                cal = new GregorianCalendar(zone, locale);
                cal.setFirstDayOfWeek(WEEKEND_ONSET);
                cal.setMinimalDaysInFirstWeek(WEEK_OF_MONTH);
                return cal;
            case DST_OFFSET /*16*/:
                return new JapaneseCalendar(zone, locale);
            case YEAR_WOY /*17*/:
                return new PersianCalendar(zone, locale);
            case DOW_LOCAL /*18*/:
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
        String prefRegion = locale.getCountry();
        if (prefRegion.length() == 0) {
            prefRegion = ULocale.addLikelySubtags(locale).getCountry();
        }
        ArrayList<String> values = new ArrayList();
        UResourceBundle calPref = UResourceBundle.getBundleInstance(ICUResourceBundle.ICU_BASE_NAME, "supplementalData", ICUResourceBundle.ICU_DATA_CLASS_LOADER).get("calendarPreferenceData");
        try {
            order = calPref.get(prefRegion);
        } catch (MissingResourceException e) {
            order = calPref.get("001");
        }
        String[] caltypes = order.getStringArray();
        if (commonlyUsed) {
            return caltypes;
        }
        for (int i = WEEKDAY; i < caltypes.length; i += YEAR) {
            values.add(caltypes[i]);
        }
        CalType[] values2 = CalType.values();
        int length = values2.length;
        for (int i2 = WEEKDAY; i2 < length; i2 += YEAR) {
            CalType t = values2[i2];
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
        if (millis > MAX_MILLIS) {
            if (isLenient()) {
                millis = MAX_MILLIS;
            } else {
                throw new IllegalArgumentException("millis value greater than upper bounds for a Calendar : " + millis);
            }
        } else if (millis < MIN_MILLIS) {
            if (isLenient()) {
                millis = MIN_MILLIS;
            } else {
                throw new IllegalArgumentException("millis value less than lower bounds for a Calendar : " + millis);
            }
        }
        this.time = millis;
        this.areAllFieldsSet = -assertionsDisabled;
        this.areFieldsSet = -assertionsDisabled;
        this.areFieldsVirtuallySet = true;
        this.isTimeSet = true;
        for (int i = WEEKDAY; i < this.fields.length; i += YEAR) {
            int[] iArr = this.fields;
            this.stamp[i] = WEEKDAY;
            iArr[i] = WEEKDAY;
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
        this.nextStamp = i + YEAR;
        iArr[field] = i;
        this.areFieldsVirtuallySet = -assertionsDisabled;
        this.areFieldsSet = -assertionsDisabled;
        this.isTimeSet = -assertionsDisabled;
    }

    public final void set(int year, int month, int date) {
        set(YEAR, year);
        set(WEEKEND_ONSET, month);
        set(THURSDAY, date);
    }

    public final void set(int year, int month, int date, int hour, int minute) {
        set(YEAR, year);
        set(WEEKEND_ONSET, month);
        set(THURSDAY, date);
        set(HOUR_OF_DAY, hour);
        set(UNDECIMBER, minute);
    }

    public final void set(int year, int month, int date, int hour, int minute, int second) {
        set(YEAR, year);
        set(WEEKEND_ONSET, month);
        set(THURSDAY, date);
        set(HOUR_OF_DAY, hour);
        set(UNDECIMBER, minute);
        set(SECOND, second);
    }

    private static int gregoYearFromIslamicStart(int year) {
        int shift;
        int i = YEAR;
        int i2;
        if (year >= 1397) {
            i2 = ((year - 1397) / 67) * WEEKEND_ONSET;
            if ((year - 1397) % 67 < 33) {
                i = WEEKDAY;
            }
            shift = i2 + i;
        } else {
            i2 = (((year - 1396) / 67) - 1) * WEEKEND_ONSET;
            if ((-(year - 1396)) % 67 > 33) {
                i = WEEKDAY;
            }
            shift = i2 + i;
        }
        return (year + 579) - shift;
    }

    @Deprecated
    public final int getRelatedYear() {
        int year = get(EXTENDED_YEAR);
        CalType type = CalType.GREGORIAN;
        String typeString = getType();
        CalType[] values = CalType.values();
        int length = values.length;
        for (int i = WEEKDAY; i < length; i += YEAR) {
            CalType testType = values[i];
            if (typeString.equals(testType.id)) {
                type = testType;
                break;
            }
        }
        switch (-getandroid-icu-util-Calendar$CalTypeSwitchesValues()[type.ordinal()]) {
            case WEEKEND_ONSET /*2*/:
                return year - 2637;
            case WEEK_OF_YEAR /*3*/:
                return year + 284;
            case WEEK_OF_MONTH /*4*/:
                return year - 2333;
            case THURSDAY /*5*/:
                return year + SEPTEMBER;
            case JULY /*6*/:
                return year - 5492;
            case SEPTEMBER /*8*/:
                return year - 3760;
            case OCTOBER /*9*/:
                return year + 79;
            case NOVEMBER /*10*/:
            case HOUR_OF_DAY /*11*/:
            case UNDECIMBER /*12*/:
            case SECOND /*13*/:
            case MILLISECOND /*14*/:
                return gregoYearFromIslamicStart(year);
            case YEAR_WOY /*17*/:
                return year + 622;
            default:
                return year;
        }
    }

    private static int firstIslamicStartYearFromGrego(int year) {
        int shift;
        int i = YEAR;
        int i2;
        if (year >= 1977) {
            i2 = ((year - 1977) / 65) * WEEKEND_ONSET;
            if ((year - 1977) % 65 < RESOLVE_REMAP) {
                i = WEEKDAY;
            }
            shift = i2 + i;
        } else {
            i2 = (((year - 1976) / 65) - 1) * WEEKEND_ONSET;
            if ((-(year - 1976)) % 65 > RESOLVE_REMAP) {
                i = WEEKDAY;
            }
            shift = i2 + i;
        }
        return (year - 579) + shift;
    }

    @Deprecated
    public final void setRelatedYear(int year) {
        CalType type = CalType.GREGORIAN;
        String typeString = getType();
        CalType[] values = CalType.values();
        int length = values.length;
        for (int i = WEEKDAY; i < length; i += YEAR) {
            CalType testType = values[i];
            if (typeString.equals(testType.id)) {
                type = testType;
                break;
            }
        }
        switch (-getandroid-icu-util-Calendar$CalTypeSwitchesValues()[type.ordinal()]) {
            case WEEKEND_ONSET /*2*/:
                year += 2637;
                break;
            case WEEK_OF_YEAR /*3*/:
                year -= 284;
                break;
            case WEEK_OF_MONTH /*4*/:
                year += 2333;
                break;
            case THURSDAY /*5*/:
                year -= 8;
                break;
            case JULY /*6*/:
                year += 5492;
                break;
            case SEPTEMBER /*8*/:
                year += 3760;
                break;
            case OCTOBER /*9*/:
                year -= 79;
                break;
            case NOVEMBER /*10*/:
            case HOUR_OF_DAY /*11*/:
            case UNDECIMBER /*12*/:
            case SECOND /*13*/:
            case MILLISECOND /*14*/:
                year = firstIslamicStartYearFromGrego(year);
                break;
            case YEAR_WOY /*17*/:
                year -= 622;
                break;
        }
        set(EXTENDED_YEAR, year);
    }

    public final void clear() {
        for (int i = WEEKDAY; i < this.fields.length; i += YEAR) {
            int[] iArr = this.fields;
            this.stamp[i] = WEEKDAY;
            iArr[i] = WEEKDAY;
        }
        this.areFieldsVirtuallySet = -assertionsDisabled;
        this.areAllFieldsSet = -assertionsDisabled;
        this.areFieldsSet = -assertionsDisabled;
        this.isTimeSet = -assertionsDisabled;
    }

    public final void clear(int field) {
        if (this.areFieldsVirtuallySet) {
            computeFields();
        }
        this.fields[field] = WEEKDAY;
        this.stamp[field] = WEEKDAY;
        this.areFieldsVirtuallySet = -assertionsDisabled;
        this.areAllFieldsSet = -assertionsDisabled;
        this.areFieldsSet = -assertionsDisabled;
        this.isTimeSet = -assertionsDisabled;
    }

    public final boolean isSet(int field) {
        return (this.areFieldsVirtuallySet || this.stamp[field] != 0) ? true : -assertionsDisabled;
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
            return -assertionsDisabled;
        }
        if (this == obj) {
            return true;
        }
        if (getClass() != obj.getClass()) {
            return -assertionsDisabled;
        }
        Calendar that = (Calendar) obj;
        if (!isEquivalentTo(that)) {
            z = -assertionsDisabled;
        } else if (getTimeInMillis() != that.getTime().getTime()) {
            z = -assertionsDisabled;
        }
        return z;
    }

    public boolean isEquivalentTo(Calendar other) {
        return (getClass() == other.getClass() && isLenient() == other.isLenient() && getFirstDayOfWeek() == other.getFirstDayOfWeek() && getMinimalDaysInFirstWeek() == other.getMinimalDaysInFirstWeek() && getTimeZone().equals(other.getTimeZone()) && getRepeatedWallTimeOption() == other.getRepeatedWallTimeOption() && getSkippedWallTimeOption() == other.getSkippedWallTimeOption()) ? true : -assertionsDisabled;
    }

    public int hashCode() {
        return (((((this.lenient ? YEAR : WEEKDAY) | (this.firstDayOfWeek << YEAR)) | (this.minimalDaysInFirstWeek << WEEK_OF_MONTH)) | (this.repeatedWallTime << SATURDAY)) | (this.skippedWallTime << OCTOBER)) | (this.zone.hashCode() << HOUR_OF_DAY);
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
        return compare(when) < 0 ? true : -assertionsDisabled;
    }

    public boolean after(Object when) {
        return compare(when) > 0 ? true : -assertionsDisabled;
    }

    public int getActualMaximum(int field) {
        Calendar cal;
        switch (field) {
            case WEEKDAY /*0*/:
            case SATURDAY /*7*/:
            case OCTOBER /*9*/:
            case NOVEMBER /*10*/:
            case HOUR_OF_DAY /*11*/:
            case UNDECIMBER /*12*/:
            case SECOND /*13*/:
            case MILLISECOND /*14*/:
            case ZONE_OFFSET /*15*/:
            case DST_OFFSET /*16*/:
            case DOW_LOCAL /*18*/:
            case JULIAN_DAY /*20*/:
            case MILLISECONDS_IN_DAY /*21*/:
                return getMaximum(field);
            case THURSDAY /*5*/:
                cal = (Calendar) clone();
                cal.setLenient(true);
                cal.prepareGetActual(field, -assertionsDisabled);
                return handleGetMonthLength(cal.get(EXTENDED_YEAR), cal.get(WEEKEND_ONSET));
            case JULY /*6*/:
                cal = (Calendar) clone();
                cal.setLenient(true);
                cal.prepareGetActual(field, -assertionsDisabled);
                return handleGetYearLength(cal.get(EXTENDED_YEAR));
            default:
                return getActualHelper(field, getLeastMaximum(field), getMaximum(field));
        }
    }

    public int getActualMinimum(int field) {
        switch (field) {
            case SATURDAY /*7*/:
            case OCTOBER /*9*/:
            case NOVEMBER /*10*/:
            case HOUR_OF_DAY /*11*/:
            case UNDECIMBER /*12*/:
            case SECOND /*13*/:
            case MILLISECOND /*14*/:
            case ZONE_OFFSET /*15*/:
            case DST_OFFSET /*16*/:
            case DOW_LOCAL /*18*/:
            case JULIAN_DAY /*20*/:
            case MILLISECONDS_IN_DAY /*21*/:
                return getMinimum(field);
            default:
                return getActualHelper(field, getGreatestMinimum(field), getMinimum(field));
        }
    }

    protected void prepareGetActual(int field, boolean isMinimum) {
        set(MILLISECONDS_IN_DAY, WEEKDAY);
        switch (field) {
            case YEAR /*1*/:
            case EXTENDED_YEAR /*19*/:
                set(JULY, getGreatestMinimum(JULY));
                break;
            case WEEKEND_ONSET /*2*/:
                set(THURSDAY, getGreatestMinimum(THURSDAY));
                break;
            case WEEK_OF_YEAR /*3*/:
            case WEEK_OF_MONTH /*4*/:
                int dow = this.firstDayOfWeek;
                if (isMinimum) {
                    dow = (dow + JULY) % SATURDAY;
                    if (dow < YEAR) {
                        dow += SATURDAY;
                    }
                }
                set(SATURDAY, dow);
                break;
            case SEPTEMBER /*8*/:
                set(THURSDAY, YEAR);
                set(SATURDAY, get(SATURDAY));
                break;
            case YEAR_WOY /*17*/:
                set(WEEK_OF_YEAR, getGreatestMinimum(WEEK_OF_YEAR));
                break;
        }
        set(field, getGreatestMinimum(field));
    }

    private int getActualHelper(int field, int startValue, int endValue) {
        boolean z = true;
        if (startValue == endValue) {
            return startValue;
        }
        int delta = endValue > startValue ? YEAR : -1;
        Calendar work = (Calendar) clone();
        work.complete();
        work.setLenient(true);
        if (delta >= 0) {
            z = -assertionsDisabled;
        }
        work.prepareGetActual(field, z);
        work.set(field, startValue);
        if (work.get(field) != startValue && field != WEEK_OF_MONTH && delta > 0) {
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
        roll(field, up ? YEAR : -1);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
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
                case WEEKDAY /*0*/:
                case THURSDAY /*5*/:
                case OCTOBER /*9*/:
                case UNDECIMBER /*12*/:
                case SECOND /*13*/:
                case MILLISECOND /*14*/:
                case MILLISECONDS_IN_DAY /*21*/:
                    int min = getActualMinimum(field);
                    gap = (getActualMaximum(field) - min) + YEAR;
                    int value = ((internalGet(field) + amount) - min) % gap;
                    if (value < 0) {
                        value += gap;
                    }
                    set(field, value + min);
                case YEAR /*1*/:
                case YEAR_WOY /*17*/:
                    boolean era0WithYearsThatGoBackwards = -assertionsDisabled;
                    int era = get(WEEKDAY);
                    if (era == 0) {
                        String calType = getType();
                        if (!calType.equals("gregorian")) {
                            if (!calType.equals("roc")) {
                                break;
                            }
                        }
                        amount = -amount;
                        era0WithYearsThatGoBackwards = true;
                    }
                    int newYear = internalGet(field) + amount;
                    if (era > 0 || newYear >= YEAR) {
                        int maxYear = getActualMaximum(field);
                        if (maxYear < 32768) {
                            if (newYear < YEAR) {
                                newYear = maxYear - ((-newYear) % maxYear);
                            } else if (newYear > maxYear) {
                                newYear = ((newYear - 1) % maxYear) + YEAR;
                            }
                        } else if (newYear < YEAR) {
                            newYear = YEAR;
                        }
                    } else if (era0WithYearsThatGoBackwards) {
                        newYear = YEAR;
                    }
                    set(field, newYear);
                    pinField(WEEKEND_ONSET);
                    pinField(THURSDAY);
                case WEEKEND_ONSET /*2*/:
                    max = getActualMaximum(WEEKEND_ONSET);
                    int mon = (internalGet(WEEKEND_ONSET) + amount) % (max + YEAR);
                    if (mon < 0) {
                        mon += max + YEAR;
                    }
                    set(WEEKEND_ONSET, mon);
                    pinField(THURSDAY);
                case WEEK_OF_YEAR /*3*/:
                    dow = internalGet(SATURDAY) - getFirstDayOfWeek();
                    if (dow < 0) {
                        dow += SATURDAY;
                    }
                    int fdy = ((dow - internalGet(JULY)) + YEAR) % SATURDAY;
                    if (fdy < 0) {
                        fdy += SATURDAY;
                    }
                    if (7 - fdy < getMinimalDaysInFirstWeek()) {
                        start = 8 - fdy;
                    } else {
                        start = 1 - fdy;
                    }
                    int yearLen = getActualMaximum(JULY);
                    gap = ((yearLen + SATURDAY) - (((yearLen - internalGet(JULY)) + dow) % SATURDAY)) - start;
                    int day_of_year = ((internalGet(JULY) + (amount * SATURDAY)) - start) % gap;
                    if (day_of_year < 0) {
                        day_of_year += gap;
                    }
                    day_of_year += start;
                    if (day_of_year < YEAR) {
                        day_of_year = YEAR;
                    }
                    if (day_of_year > yearLen) {
                        day_of_year = yearLen;
                    }
                    set(JULY, day_of_year);
                    clear(WEEKEND_ONSET);
                case WEEK_OF_MONTH /*4*/:
                    dow = internalGet(SATURDAY) - getFirstDayOfWeek();
                    if (dow < 0) {
                        dow += SATURDAY;
                    }
                    int fdm = ((dow - internalGet(THURSDAY)) + YEAR) % SATURDAY;
                    if (fdm < 0) {
                        fdm += SATURDAY;
                    }
                    if (7 - fdm < getMinimalDaysInFirstWeek()) {
                        start = 8 - fdm;
                    } else {
                        start = 1 - fdm;
                    }
                    int monthLen = getActualMaximum(THURSDAY);
                    gap = ((monthLen + SATURDAY) - (((monthLen - internalGet(THURSDAY)) + dow) % SATURDAY)) - start;
                    int day_of_month = ((internalGet(THURSDAY) + (amount * SATURDAY)) - start) % gap;
                    if (day_of_month < 0) {
                        day_of_month += gap;
                    }
                    day_of_month += start;
                    if (day_of_month < YEAR) {
                        day_of_month = YEAR;
                    }
                    if (day_of_month > monthLen) {
                        day_of_month = monthLen;
                    }
                    set(THURSDAY, day_of_month);
                case JULY /*6*/:
                    delta = ((long) amount) * ONE_DAY;
                    min2 = this.time - (((long) (internalGet(JULY) - 1)) * ONE_DAY);
                    int yearLength = getActualMaximum(JULY);
                    this.time = ((this.time + delta) - min2) % (((long) yearLength) * ONE_DAY);
                    if (this.time < 0) {
                        this.time += ((long) yearLength) * ONE_DAY;
                    }
                    setTimeInMillis(this.time + min2);
                case SATURDAY /*7*/:
                case DOW_LOCAL /*18*/:
                    delta = ((long) amount) * ONE_DAY;
                    int leadDays = internalGet(field) - (field == SATURDAY ? getFirstDayOfWeek() : YEAR);
                    if (leadDays < 0) {
                        leadDays += SATURDAY;
                    }
                    min2 = this.time - (((long) leadDays) * ONE_DAY);
                    this.time = ((this.time + delta) - min2) % ONE_WEEK;
                    if (this.time < 0) {
                        this.time += ONE_WEEK;
                    }
                    setTimeInMillis(this.time + min2);
                case SEPTEMBER /*8*/:
                    int preWeeks = (internalGet(THURSDAY) - 1) / SATURDAY;
                    min2 = this.time - (((long) preWeeks) * ONE_WEEK);
                    long gap2 = ONE_WEEK * ((long) ((preWeeks + ((getActualMaximum(THURSDAY) - internalGet(THURSDAY)) / SATURDAY)) + YEAR));
                    this.time = ((this.time + (((long) amount) * ONE_WEEK)) - min2) % gap2;
                    if (this.time < 0) {
                        this.time += gap2;
                    }
                    setTimeInMillis(this.time + min2);
                case NOVEMBER /*10*/:
                case HOUR_OF_DAY /*11*/:
                    long start2 = getTimeInMillis();
                    int oldHour = internalGet(field);
                    max = getMaximum(field);
                    int newHour = (oldHour + amount) % (max + YEAR);
                    if (newHour < 0) {
                        newHour += max + YEAR;
                    }
                    setTimeInMillis(((((long) newHour) - ((long) oldHour)) * RelativeDateTimeFormatter.HOUR_IN_MILLIS) + start2);
                case EXTENDED_YEAR /*19*/:
                    set(field, internalGet(field) + amount);
                    pinField(WEEKEND_ONSET);
                    pinField(THURSDAY);
                case JULIAN_DAY /*20*/:
                    set(field, internalGet(field) + amount);
                default:
                    throw new IllegalArgumentException("Calendar.roll(" + fieldName(field) + ") not supported");
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void add(int field, int amount) {
        if (amount != 0) {
            long delta = (long) amount;
            boolean keepWallTimeInvariant = true;
            switch (field) {
                case WEEKDAY /*0*/:
                    set(field, get(field) + amount);
                    pinField(WEEKDAY);
                case YEAR /*1*/:
                case YEAR_WOY /*17*/:
                    if (get(WEEKDAY) == 0) {
                        String calType = getType();
                        if (!calType.equals("gregorian")) {
                            if (!calType.equals("roc")) {
                                break;
                            }
                        }
                        amount = -amount;
                        break;
                    }
                    break;
                case WEEKEND_ONSET /*2*/:
                case EXTENDED_YEAR /*19*/:
                    break;
                case WEEK_OF_YEAR /*3*/:
                case WEEK_OF_MONTH /*4*/:
                case SEPTEMBER /*8*/:
                    delta *= ONE_WEEK;
                    break;
                case THURSDAY /*5*/:
                case JULY /*6*/:
                case SATURDAY /*7*/:
                case DOW_LOCAL /*18*/:
                case JULIAN_DAY /*20*/:
                    delta *= ONE_DAY;
                    break;
                case OCTOBER /*9*/:
                    delta *= 43200000;
                    break;
                case NOVEMBER /*10*/:
                case HOUR_OF_DAY /*11*/:
                    delta *= RelativeDateTimeFormatter.HOUR_IN_MILLIS;
                    keepWallTimeInvariant = -assertionsDisabled;
                    break;
                case UNDECIMBER /*12*/:
                    delta *= RelativeDateTimeFormatter.MINUTE_IN_MILLIS;
                    keepWallTimeInvariant = -assertionsDisabled;
                    break;
                case SECOND /*13*/:
                    delta *= 1000;
                    keepWallTimeInvariant = -assertionsDisabled;
                    break;
                case MILLISECOND /*14*/:
                case MILLISECONDS_IN_DAY /*21*/:
                    keepWallTimeInvariant = -assertionsDisabled;
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

    public /* bridge */ /* synthetic */ int compareTo(Object that) {
        return compareTo((Calendar) that);
    }

    public int compareTo(Calendar that) {
        long v = getTimeInMillis() - that.getTimeInMillis();
        if (v < 0) {
            return -1;
        }
        return v > 0 ? YEAR : WEEKDAY;
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
        if (timeStyle < -1 || timeStyle > WEEK_OF_YEAR) {
            throw new IllegalArgumentException("Illegal time style " + timeStyle);
        } else if (dateStyle < -1 || dateStyle > WEEK_OF_YEAR) {
            throw new IllegalArgumentException("Illegal date style " + dateStyle);
        } else {
            String pattern;
            PatternData patternData = PatternData.make(cal, loc);
            String override = null;
            if (timeStyle >= 0 && dateStyle >= 0) {
                String -wrap1 = patternData.getDateTimePattern(dateStyle);
                Object[] objArr = new Object[WEEKEND_ONSET];
                objArr[WEEKDAY] = patternData.patterns[timeStyle];
                objArr[YEAR] = patternData.patterns[dateStyle + WEEK_OF_MONTH];
                pattern = MessageFormat.format(-wrap1, objArr);
                if (patternData.overrides != null) {
                    override = mergeOverrideStrings(patternData.patterns[dateStyle + WEEK_OF_MONTH], patternData.patterns[timeStyle], patternData.overrides[dateStyle + WEEK_OF_MONTH], patternData.overrides[timeStyle]);
                }
            } else if (timeStyle >= 0) {
                pattern = patternData.patterns[timeStyle];
                if (patternData.overrides != null) {
                    override = patternData.overrides[timeStyle];
                }
            } else if (dateStyle >= 0) {
                pattern = patternData.patterns[dateStyle + WEEK_OF_MONTH];
                if (patternData.overrides != null) {
                    override = patternData.overrides[dateStyle + WEEK_OF_MONTH];
                }
            } else {
                throw new IllegalArgumentException("No date or time style specified");
            }
            DateFormat result = cal.handleGetDateFormat(pattern, override, loc);
            result.setCalendar(cal);
            return result;
        }
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
        boolean inQuotes = -assertionsDisabled;
        char prevChar = ' ';
        StringBuilder result = new StringBuilder();
        StringCharacterIterator it = new StringCharacterIterator(pattern);
        char c = it.first();
        while (c != UnicodeMatcher.ETHER) {
            if (c == QUOTE) {
                inQuotes = inQuotes ? -assertionsDisabled : true;
                prevChar = c;
            } else {
                if (!(inQuotes || c == r3)) {
                    if (result.length() > 0) {
                        result.append(";");
                    }
                    result.append(c);
                    result.append("=");
                    result.append(override);
                }
                prevChar = c;
            }
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
        int periodStartDayOfWeek = (((dayOfWeek - getFirstDayOfWeek()) - dayOfPeriod) + YEAR) % SATURDAY;
        if (periodStartDayOfWeek < 0) {
            periodStartDayOfWeek += SATURDAY;
        }
        int weekNo = ((desiredDay + periodStartDayOfWeek) - 1) / SATURDAY;
        if (7 - periodStartDayOfWeek >= getMinimalDaysInFirstWeek()) {
            return weekNo + YEAR;
        }
        return weekNo;
    }

    protected final int weekNumber(int dayOfPeriod, int dayOfWeek) {
        return weekNumber(dayOfPeriod, dayOfPeriod, dayOfWeek);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int fieldDifference(Date when, int field) {
        int min = WEEKDAY;
        long startMs = getTimeInMillis();
        long targetMs = when.getTime();
        int max;
        long ms;
        if (startMs < targetMs) {
            max = YEAR;
            while (true) {
                setTimeInMillis(startMs);
                add(field, max);
                ms = getTimeInMillis();
                if (ms != targetMs) {
                    if (ms <= targetMs) {
                        if (max >= FIELD_DIFF_MAX_INT) {
                            break;
                        }
                        min = max;
                        max <<= YEAR;
                        if (max < 0) {
                            max = FIELD_DIFF_MAX_INT;
                        }
                    } else {
                        break;
                    }
                }
                return max;
            }
            throw new RuntimeException();
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
                    while (min - max > YEAR) {
                        int t = min + ((max - min) / WEEKEND_ONSET);
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
                    max <<= YEAR;
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
        this.areFieldsSet = -assertionsDisabled;
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
        if (option == 0 || option == YEAR) {
            this.repeatedWallTime = option;
            return;
        }
        throw new IllegalArgumentException("Illegal repeated wall time option - " + option);
    }

    public int getRepeatedWallTimeOption() {
        return this.repeatedWallTime;
    }

    public void setSkippedWallTimeOption(int option) {
        if (option == 0 || option == YEAR || option == WEEKEND_ONSET) {
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
        if (value < YEAR || value > SATURDAY) {
            throw new IllegalArgumentException("Invalid day of week");
        }
        this.firstDayOfWeek = value;
        this.areFieldsSet = -assertionsDisabled;
    }

    public int getFirstDayOfWeek() {
        return this.firstDayOfWeek;
    }

    public void setMinimalDaysInFirstWeek(int value) {
        if (value < YEAR) {
            value = YEAR;
        } else if (value > SATURDAY) {
            value = SATURDAY;
        }
        if (this.minimalDaysInFirstWeek != value) {
            this.minimalDaysInFirstWeek = value;
            this.areFieldsSet = -assertionsDisabled;
        }
    }

    public int getMinimalDaysInFirstWeek() {
        return this.minimalDaysInFirstWeek;
    }

    protected int getLimit(int field, int limitType) {
        switch (field) {
            case WEEK_OF_MONTH /*4*/:
                int limit;
                if (limitType == 0) {
                    if (getMinimalDaysInFirstWeek() == YEAR) {
                        limit = YEAR;
                    } else {
                        limit = WEEKDAY;
                    }
                } else if (limitType == YEAR) {
                    limit = YEAR;
                } else {
                    int minDaysInFirst = getMinimalDaysInFirstWeek();
                    int daysInMonth = handleGetLimit(THURSDAY, limitType);
                    if (limitType == WEEKEND_ONSET) {
                        limit = ((7 - minDaysInFirst) + daysInMonth) / SATURDAY;
                    } else {
                        limit = ((daysInMonth + JULY) + (7 - minDaysInFirst)) / SATURDAY;
                    }
                }
                return limit;
            case SATURDAY /*7*/:
            case OCTOBER /*9*/:
            case NOVEMBER /*10*/:
            case HOUR_OF_DAY /*11*/:
            case UNDECIMBER /*12*/:
            case SECOND /*13*/:
            case MILLISECOND /*14*/:
            case ZONE_OFFSET /*15*/:
            case DST_OFFSET /*16*/:
            case DOW_LOCAL /*18*/:
            case JULIAN_DAY /*20*/:
            case MILLISECONDS_IN_DAY /*21*/:
            case IS_LEAP_MONTH /*22*/:
                return LIMITS[field][limitType];
            default:
                return handleGetLimit(field, limitType);
        }
    }

    public final int getMinimum(int field) {
        return getLimit(field, WEEKDAY);
    }

    public final int getMaximum(int field) {
        return getLimit(field, WEEK_OF_YEAR);
    }

    public final int getGreatestMinimum(int field) {
        return getLimit(field, YEAR);
    }

    public final int getLeastMaximum(int field) {
        return getLimit(field, WEEKEND_ONSET);
    }

    @Deprecated
    public int getDayOfWeekType(int dayOfWeek) {
        int i = YEAR;
        if (dayOfWeek < YEAR || dayOfWeek > SATURDAY) {
            throw new IllegalArgumentException("Invalid day of week");
        } else if (this.weekendOnset != this.weekendCease) {
            if (this.weekendOnset < this.weekendCease) {
                if (dayOfWeek < this.weekendOnset || dayOfWeek > this.weekendCease) {
                    return WEEKDAY;
                }
            } else if (dayOfWeek > this.weekendCease && dayOfWeek < this.weekendOnset) {
                return WEEKDAY;
            }
            if (dayOfWeek == this.weekendOnset) {
                if (this.weekendOnsetMillis != 0) {
                    i = WEEKEND_ONSET;
                }
                return i;
            } else if (dayOfWeek != this.weekendCease) {
                return YEAR;
            } else {
                if (this.weekendCeaseMillis < Grego.MILLIS_PER_DAY) {
                    i = WEEK_OF_YEAR;
                }
                return i;
            }
        } else if (dayOfWeek != this.weekendOnset) {
            return WEEKDAY;
        } else {
            if (this.weekendOnsetMillis != 0) {
                i = WEEKEND_ONSET;
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
        int dow = get(SATURDAY);
        int dowt = getDayOfWeekType(dow);
        switch (dowt) {
            case WEEKDAY /*0*/:
                return -assertionsDisabled;
            case YEAR /*1*/:
                return true;
            default:
                int millisInDay = internalGet(MILLISECOND) + ((internalGet(SECOND) + ((internalGet(UNDECIMBER) + (internalGet(HOUR_OF_DAY) * 60)) * 60)) * ONE_SECOND);
                int transition = getWeekendTransition(dow);
                if (dowt == WEEKEND_ONSET) {
                    if (millisInDay < transition) {
                        z = -assertionsDisabled;
                    }
                } else if (millisInDay >= transition) {
                    z = -assertionsDisabled;
                }
                return z;
        }
    }

    public Object clone() {
        try {
            Calendar other = (Calendar) super.clone();
            other.fields = new int[this.fields.length];
            other.stamp = new int[this.fields.length];
            System.arraycopy(this.fields, WEEKDAY, other.fields, WEEKDAY, this.fields.length);
            System.arraycopy(this.stamp, WEEKDAY, other.stamp, WEEKDAY, this.fields.length);
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
        for (int i = WEEKDAY; i < this.fields.length; i += YEAR) {
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
        UResourceBundle weekDataInfo = UResourceBundle.getBundleInstance(ICUResourceBundle.ICU_BASE_NAME, "supplementalData", ICUResourceBundle.ICU_DATA_CLASS_LOADER).get("weekData");
        try {
            weekDataBundle = weekDataInfo.get(region);
        } catch (MissingResourceException mre) {
            if (region.equals("001")) {
                throw mre;
            }
            weekDataBundle = weekDataInfo.get("001");
        }
        int[] wdi = weekDataBundle.getIntVector();
        return new WeekData(wdi[WEEKDAY], wdi[YEAR], wdi[WEEKEND_ONSET], wdi[WEEK_OF_YEAR], wdi[WEEK_OF_MONTH], wdi[THURSDAY]);
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
            this.areFieldsSet = -assertionsDisabled;
        }
        this.isTimeSet = true;
        this.areFieldsVirtuallySet = -assertionsDisabled;
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
        this.areAllFieldsSet = -assertionsDisabled;
        this.areFieldsSet = -assertionsDisabled;
        this.areFieldsVirtuallySet = true;
        this.nextStamp = WEEKEND_ONSET;
    }

    protected void computeFields() {
        int[] offsets = new int[WEEKEND_ONSET];
        getTimeZone().getOffset(this.time, -assertionsDisabled, offsets);
        long localMillis = (this.time + ((long) offsets[WEEKDAY])) + ((long) offsets[YEAR]);
        int mask = this.internalSetMask;
        for (int i = WEEKDAY; i < this.fields.length; i += YEAR) {
            if ((mask & YEAR) == 0) {
                this.stamp[i] = YEAR;
            } else {
                this.stamp[i] = WEEKDAY;
            }
            mask >>= YEAR;
        }
        long days = floorDivide(localMillis, (long) ONE_DAY);
        this.fields[JULIAN_DAY] = ((int) days) + EPOCH_JULIAN_DAY;
        computeGregorianAndDOWFields(this.fields[JULIAN_DAY]);
        handleComputeFields(this.fields[JULIAN_DAY]);
        computeWeekFields();
        int millisInDay = (int) (localMillis - (ONE_DAY * days));
        this.fields[MILLISECONDS_IN_DAY] = millisInDay;
        this.fields[MILLISECOND] = millisInDay % ONE_SECOND;
        millisInDay /= ONE_SECOND;
        this.fields[SECOND] = millisInDay % 60;
        millisInDay /= 60;
        this.fields[UNDECIMBER] = millisInDay % 60;
        millisInDay /= 60;
        this.fields[HOUR_OF_DAY] = millisInDay;
        this.fields[OCTOBER] = millisInDay / UNDECIMBER;
        this.fields[NOVEMBER] = millisInDay % UNDECIMBER;
        this.fields[ZONE_OFFSET] = offsets[WEEKDAY];
        this.fields[DST_OFFSET] = offsets[YEAR];
    }

    private final void computeGregorianAndDOWFields(int julianDay) {
        computeGregorianFields(julianDay);
        int dow = julianDayToDayOfWeek(julianDay);
        this.fields[SATURDAY] = dow;
        int dowLocal = (dow - getFirstDayOfWeek()) + YEAR;
        if (dowLocal < YEAR) {
            dowLocal += SATURDAY;
        }
        this.fields[DOW_LOCAL] = dowLocal;
    }

    protected final void computeGregorianFields(int julianDay) {
        int[] rem = new int[YEAR];
        int n400 = floorDivide((long) (julianDay - JAN_1_1_JULIAN_DAY), 146097, rem);
        int n100 = floorDivide(rem[WEEKDAY], 36524, rem);
        int n4 = floorDivide(rem[WEEKDAY], 1461, rem);
        int n1 = floorDivide(rem[WEEKDAY], 365, rem);
        int year = (((n400 * BreakIterator.WORD_KANA_LIMIT) + (n100 * 100)) + (n4 * WEEK_OF_MONTH)) + n1;
        int dayOfYear = rem[WEEKDAY];
        if (n100 == WEEK_OF_MONTH || n1 == WEEK_OF_MONTH) {
            dayOfYear = 365;
        } else {
            year += YEAR;
        }
        boolean isLeap = (year & WEEK_OF_YEAR) == 0 ? (year % 100 != 0 || year % BreakIterator.WORD_KANA_LIMIT == 0) ? true : -assertionsDisabled : -assertionsDisabled;
        int correction = WEEKDAY;
        if (dayOfYear >= (isLeap ? 60 : 59)) {
            correction = isLeap ? YEAR : WEEKEND_ONSET;
        }
        int month = (((dayOfYear + correction) * UNDECIMBER) + JULY) / 367;
        int dayOfMonth = (dayOfYear - GREGORIAN_MONTH_COUNT[month][isLeap ? WEEK_OF_YEAR : WEEKEND_ONSET]) + YEAR;
        this.gregorianYear = year;
        this.gregorianMonth = month;
        this.gregorianDayOfMonth = dayOfMonth;
        this.gregorianDayOfYear = dayOfYear + YEAR;
    }

    private final void computeWeekFields() {
        int eyear = this.fields[EXTENDED_YEAR];
        int dayOfWeek = this.fields[SATURDAY];
        int dayOfYear = this.fields[JULY];
        int yearOfWeekOfYear = eyear;
        int relDow = ((dayOfWeek + SATURDAY) - getFirstDayOfWeek()) % SATURDAY;
        int relDowJan1 = (((dayOfWeek - dayOfYear) + 7001) - getFirstDayOfWeek()) % SATURDAY;
        int woy = ((dayOfYear - 1) + relDowJan1) / SATURDAY;
        if (7 - relDowJan1 >= getMinimalDaysInFirstWeek()) {
            woy += YEAR;
        }
        if (woy == 0) {
            woy = weekNumber(dayOfYear + handleGetYearLength(eyear - 1), dayOfWeek);
            yearOfWeekOfYear = eyear - 1;
        } else {
            int lastDoy = handleGetYearLength(eyear);
            if (dayOfYear >= lastDoy - 5) {
                int lastRelDow = ((relDow + lastDoy) - dayOfYear) % SATURDAY;
                if (lastRelDow < 0) {
                    lastRelDow += SATURDAY;
                }
                if (6 - lastRelDow >= getMinimalDaysInFirstWeek() && (dayOfYear + SATURDAY) - relDow > lastDoy) {
                    woy = YEAR;
                    yearOfWeekOfYear = eyear + YEAR;
                }
            }
        }
        this.fields[WEEK_OF_YEAR] = woy;
        this.fields[YEAR_WOY] = yearOfWeekOfYear;
        int dayOfMonth = this.fields[THURSDAY];
        this.fields[WEEK_OF_MONTH] = weekNumber(dayOfMonth, dayOfWeek);
        this.fields[SEPTEMBER] = ((dayOfMonth - 1) / SATURDAY) + YEAR;
    }

    protected int resolveFields(int[][][] precedenceTable) {
        int bestField = -1;
        for (int g = WEEKDAY; g < precedenceTable.length && bestField < 0; g += YEAR) {
            int[][] group = precedenceTable[g];
            int bestStamp = WEEKDAY;
            for (int l = WEEKDAY; l < group.length; l += YEAR) {
                int[] line = group[l];
                int lineStamp = WEEKDAY;
                int i = line[WEEKDAY] >= RESOLVE_REMAP ? YEAR : WEEKDAY;
                while (i < line.length) {
                    int s = this.stamp[line[i]];
                    if (s == 0) {
                        break;
                    }
                    lineStamp = Math.max(lineStamp, s);
                    i += YEAR;
                }
                if (lineStamp > bestStamp) {
                    int tempBestField = line[WEEKDAY];
                    if (tempBestField >= RESOLVE_REMAP) {
                        tempBestField &= 31;
                        if (tempBestField != THURSDAY || this.stamp[WEEK_OF_MONTH] < this.stamp[tempBestField]) {
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
        return bestField >= RESOLVE_REMAP ? bestField & 31 : bestField;
    }

    protected int newestStamp(int first, int last, int bestStampSoFar) {
        int bestStamp = bestStampSoFar;
        for (int i = first; i <= last; i += YEAR) {
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
        for (int field = WEEKDAY; field < this.fields.length; field += YEAR) {
            if (this.stamp[field] >= WEEKEND_ONSET) {
                validateField(field);
            }
        }
    }

    protected void validateField(int field) {
        switch (field) {
            case THURSDAY /*5*/:
                validateField(field, YEAR, handleGetMonthLength(handleGetExtendedYear(), internalGet(WEEKEND_ONSET)));
            case JULY /*6*/:
                validateField(field, YEAR, handleGetYearLength(handleGetExtendedYear()));
            case SEPTEMBER /*8*/:
                if (internalGet(field) == 0) {
                    throw new IllegalArgumentException("DAY_OF_WEEK_IN_MONTH cannot be zero");
                }
                validateField(field, getMinimum(field), getMaximum(field));
            default:
                validateField(field, getMinimum(field), getMaximum(field));
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
        if (this.stamp[MILLISECONDS_IN_DAY] < WEEKEND_ONSET || newestStamp(OCTOBER, MILLISECOND, WEEKDAY) > this.stamp[MILLISECONDS_IN_DAY]) {
            millisInDay = computeMillisInDay();
        } else {
            millisInDay = internalGet(MILLISECONDS_IN_DAY);
        }
        if (this.stamp[ZONE_OFFSET] >= WEEKEND_ONSET || this.stamp[DST_OFFSET] >= WEEKEND_ONSET) {
            this.time = (((long) millisInDay) + millis) - ((long) (internalGet(ZONE_OFFSET) + internalGet(DST_OFFSET)));
        } else if (!this.lenient || this.skippedWallTime == WEEKEND_ONSET) {
            int zoneOffset = computeZoneOffset(millis, millisInDay);
            long tmpTime = (((long) millisInDay) + millis) - ((long) zoneOffset);
            if (zoneOffset == this.zone.getOffset(tmpTime)) {
                this.time = tmpTime;
            } else if (this.lenient) {
                if (!-assertionsDisabled) {
                    if ((this.skippedWallTime == WEEKEND_ONSET ? YEAR : null) == null) {
                        throw new AssertionError(Integer.valueOf(this.skippedWallTime));
                    }
                }
                Long immediatePrevTransition = getImmediatePreviousZoneTransition(tmpTime);
                if (immediatePrevTransition == null) {
                    throw new RuntimeException("Could not locate a time zone transition before " + tmpTime);
                }
                this.time = immediatePrevTransition.longValue();
            } else {
                throw new IllegalArgumentException("The specified wall time does not exist due to time zone offset transition.");
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
        if (!-assertionsDisabled) {
            if ((duration > 0 ? YEAR : null) == null) {
                throw new AssertionError();
            }
        }
        long upper = base;
        long lower = (base - duration) - 1;
        int offsetU = tz.getOffset(base);
        if (offsetU == tz.getOffset(lower)) {
            return null;
        }
        return findPreviousZoneTransitionTime(tz, offsetU, base, lower);
    }

    private static Long findPreviousZoneTransitionTime(TimeZone tz, int upperOffset, long upper, long lower) {
        boolean onUnitTime = -assertionsDisabled;
        long mid = 0;
        int[] iArr = FIND_ZONE_TRANSITION_TIME_UNITS;
        int length = iArr.length;
        for (int i = WEEKDAY; i < length; i += YEAR) {
            int unit = iArr[i];
            long lunits = lower / ((long) unit);
            long uunits = upper / ((long) unit);
            if (uunits > lunits) {
                mid = (((lunits + uunits) + 1) >>> YEAR) * ((long) unit);
                onUnitTime = true;
                break;
            }
        }
        if (!onUnitTime) {
            mid = (upper + lower) >>> YEAR;
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
            mid = (upper + lower) >>> YEAR;
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
        int bestStamp;
        int millisInDay = WEEKDAY;
        int hourOfDayStamp = this.stamp[HOUR_OF_DAY];
        int hourStamp = Math.max(this.stamp[NOVEMBER], this.stamp[OCTOBER]);
        if (hourStamp > hourOfDayStamp) {
            bestStamp = hourStamp;
        } else {
            bestStamp = hourOfDayStamp;
        }
        if (bestStamp != 0) {
            if (bestStamp == hourOfDayStamp) {
                millisInDay = internalGet(HOUR_OF_DAY) + WEEKDAY;
            } else {
                millisInDay = (internalGet(NOVEMBER) + WEEKDAY) + (internalGet(OCTOBER) * UNDECIMBER);
            }
        }
        return (((((millisInDay * 60) + internalGet(UNDECIMBER)) * 60) + internalGet(SECOND)) * ONE_SECOND) + internalGet(MILLISECOND);
    }

    protected int computeZoneOffset(long millis, int millisInDay) {
        int[] offsets = new int[WEEKEND_ONSET];
        long wall = millis + ((long) millisInDay);
        if (this.zone instanceof BasicTimeZone) {
            ((BasicTimeZone) this.zone).getOffsetFromLocal(wall, this.skippedWallTime == YEAR ? UNDECIMBER : WEEK_OF_MONTH, this.repeatedWallTime == YEAR ? WEEK_OF_MONTH : UNDECIMBER, offsets);
        } else {
            this.zone.getOffset(wall, true, offsets);
            boolean sawRecentNegativeShift = -assertionsDisabled;
            if (this.repeatedWallTime == YEAR) {
                int offsetDelta = (offsets[WEEKDAY] + offsets[YEAR]) - this.zone.getOffset((wall - ((long) (offsets[WEEKDAY] + offsets[YEAR]))) - 21600000);
                if (!-assertionsDisabled) {
                    if ((offsetDelta < -21600000 ? YEAR : null) == null) {
                        throw new AssertionError(Integer.valueOf(offsetDelta));
                    }
                }
                if (offsetDelta < 0) {
                    sawRecentNegativeShift = true;
                    this.zone.getOffset(((long) offsetDelta) + wall, true, offsets);
                }
            }
            if (!sawRecentNegativeShift && this.skippedWallTime == YEAR) {
                this.zone.getOffset(wall - ((long) (offsets[WEEKDAY] + offsets[YEAR])), -assertionsDisabled, offsets);
            }
        }
        return offsets[WEEKDAY] + offsets[YEAR];
    }

    protected int computeJulianDay() {
        if (this.stamp[JULIAN_DAY] >= WEEKEND_ONSET && newestStamp(YEAR_WOY, EXTENDED_YEAR, newestStamp(WEEKDAY, SEPTEMBER, WEEKDAY)) <= this.stamp[JULIAN_DAY]) {
            return internalGet(JULIAN_DAY);
        }
        int bestField = resolveFields(getFieldResolutionTable());
        if (bestField < 0) {
            bestField = THURSDAY;
        }
        return handleComputeJulianDay(bestField);
    }

    protected int[][][] getFieldResolutionTable() {
        return DATE_PRECEDENCE;
    }

    protected int handleGetMonthLength(int extendedYear, int month) {
        return handleComputeMonthStart(extendedYear, month + YEAR, true) - handleComputeMonthStart(extendedYear, month, true);
    }

    protected int handleGetYearLength(int eyear) {
        return handleComputeMonthStart(eyear + YEAR, WEEKDAY, -assertionsDisabled) - handleComputeMonthStart(eyear, WEEKDAY, -assertionsDisabled);
    }

    protected int[] handleCreateFields() {
        return new int[BASE_FIELD_COUNT];
    }

    protected int getDefaultMonthInYear(int extendedYear) {
        return WEEKDAY;
    }

    protected int getDefaultDayInMonth(int extendedYear, int month) {
        return YEAR;
    }

    protected int handleComputeJulianDay(int bestField) {
        int year;
        boolean useMonth = (bestField == THURSDAY || bestField == WEEK_OF_MONTH) ? true : bestField == SEPTEMBER ? true : -assertionsDisabled;
        if (bestField == WEEK_OF_YEAR) {
            year = internalGet(YEAR_WOY, handleGetExtendedYear());
        } else {
            year = handleGetExtendedYear();
        }
        internalSet(EXTENDED_YEAR, year);
        int month = useMonth ? internalGet(WEEKEND_ONSET, getDefaultMonthInYear(year)) : WEEKDAY;
        int julianDay = handleComputeMonthStart(year, month, useMonth);
        if (bestField == THURSDAY) {
            if (isSet(THURSDAY)) {
                return internalGet(THURSDAY, getDefaultDayInMonth(year, month)) + julianDay;
            }
            return getDefaultDayInMonth(year, month) + julianDay;
        } else if (bestField == JULY) {
            return internalGet(JULY) + julianDay;
        } else {
            int firstDOW = getFirstDayOfWeek();
            int first = julianDayToDayOfWeek(julianDay + YEAR) - firstDOW;
            if (first < 0) {
                first += SATURDAY;
            }
            int dowLocal = WEEKDAY;
            switch (resolveFields(DOW_PRECEDENCE)) {
                case SATURDAY /*7*/:
                    dowLocal = internalGet(SATURDAY) - firstDOW;
                    break;
                case DOW_LOCAL /*18*/:
                    dowLocal = internalGet(DOW_LOCAL) - 1;
                    break;
            }
            dowLocal %= SATURDAY;
            if (dowLocal < 0) {
                dowLocal += SATURDAY;
            }
            int date = (1 - first) + dowLocal;
            if (bestField == SEPTEMBER) {
                if (date < YEAR) {
                    date += SATURDAY;
                }
                int dim = internalGet(SEPTEMBER, YEAR);
                if (dim >= 0) {
                    date += (dim - 1) * SATURDAY;
                } else {
                    date += ((((handleGetMonthLength(year, internalGet(WEEKEND_ONSET, WEEKDAY)) - date) / SATURDAY) + dim) + YEAR) * SATURDAY;
                }
            } else {
                if (7 - first < getMinimalDaysInFirstWeek()) {
                    date += SATURDAY;
                }
                date += (internalGet(bestField) - 1) * SATURDAY;
            }
            return julianDay + date;
        }
    }

    protected int computeGregorianMonthStart(int year, int month) {
        if (month < 0 || month > HOUR_OF_DAY) {
            int[] rem = new int[YEAR];
            year += floorDivide(month, (int) UNDECIMBER, rem);
            month = rem[WEEKDAY];
        }
        boolean isLeap = (year % WEEK_OF_MONTH != 0 || (year % 100 == 0 && year % BreakIterator.WORD_KANA_LIMIT != 0)) ? -assertionsDisabled : true;
        int y = year - 1;
        int julianDay = (((((y * 365) + floorDivide(y, (int) WEEK_OF_MONTH)) - floorDivide(y, 100)) + floorDivide(y, (int) BreakIterator.WORD_KANA_LIMIT)) + JAN_1_1_JULIAN_DAY) - 1;
        if (month == 0) {
            return julianDay;
        }
        return julianDay + GREGORIAN_MONTH_COUNT[month][isLeap ? WEEK_OF_YEAR : WEEKEND_ONSET];
    }

    protected void handleComputeFields(int julianDay) {
        internalSet(WEEKEND_ONSET, getGregorianMonth());
        internalSet(THURSDAY, getGregorianDayOfMonth());
        internalSet(JULY, getGregorianDayOfYear());
        int eyear = getGregorianYear();
        internalSet(EXTENDED_YEAR, eyear);
        int era = YEAR;
        if (eyear < YEAR) {
            era = WEEKDAY;
            eyear = 1 - eyear;
        }
        internalSet(WEEKDAY, era);
        internalSet(YEAR, eyear);
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
        if (((YEAR << field) & this.internalSetMask) == 0) {
            throw new IllegalStateException("Subclass cannot set " + fieldName(field));
        }
        this.fields[field] = value;
        this.stamp[field] = YEAR;
    }

    protected static final boolean isGregorianLeapYear(int year) {
        return (year % WEEK_OF_MONTH != 0 || (year % 100 == 0 && year % BreakIterator.WORD_KANA_LIMIT != 0)) ? -assertionsDisabled : true;
    }

    protected static final int gregorianMonthLength(int y, int m) {
        return GREGORIAN_MONTH_COUNT[m][isGregorianLeapYear(y) ? YEAR : WEEKDAY];
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
        return ((numerator + YEAR) / denominator) - 1;
    }

    protected static final int floorDivide(int numerator, int denominator, int[] remainder) {
        if (numerator >= 0) {
            remainder[WEEKDAY] = numerator % denominator;
            return numerator / denominator;
        }
        int quotient = ((numerator + YEAR) / denominator) - 1;
        remainder[WEEKDAY] = numerator - (quotient * denominator);
        return quotient;
    }

    protected static final int floorDivide(long numerator, int denominator, int[] remainder) {
        if (numerator >= 0) {
            remainder[WEEKDAY] = (int) (numerator % ((long) denominator));
            return (int) (numerator / ((long) denominator));
        }
        int quotient = (int) (((numerator + 1) / ((long) denominator)) - 1);
        remainder[WEEKDAY] = (int) (numerator - (((long) quotient) * ((long) denominator)));
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
        return (int) (floorDivide(millis, (long) ONE_DAY) + 2440588);
    }

    protected static final long julianDayToMillis(int julian) {
        return ((long) (julian - EPOCH_JULIAN_DAY)) * ONE_DAY;
    }

    protected static final int julianDayToDayOfWeek(int julian) {
        int dayOfWeek = (julian + WEEKEND_ONSET) % SATURDAY;
        if (dayOfWeek < YEAR) {
            return dayOfWeek + SATURDAY;
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
        Object obj2 = YEAR;
        if (valid == null) {
            obj = YEAR;
        } else {
            obj = WEEKDAY;
        }
        if (actual != null) {
            obj2 = WEEKDAY;
        }
        if (obj != obj2) {
            throw new IllegalArgumentException();
        }
        this.validLocale = valid;
        this.actualLocale = actual;
    }
}
