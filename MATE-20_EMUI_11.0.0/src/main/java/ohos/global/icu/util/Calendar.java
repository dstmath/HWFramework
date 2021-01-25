package ohos.global.icu.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.MissingResourceException;
import ohos.global.icu.impl.CalType;
import ohos.global.icu.impl.CalendarUtil;
import ohos.global.icu.impl.ICUCache;
import ohos.global.icu.impl.ICUResourceBundle;
import ohos.global.icu.impl.SimpleCache;
import ohos.global.icu.impl.SimpleFormatterImpl;
import ohos.global.icu.impl.SoftCache;
import ohos.global.icu.text.DateFormat;
import ohos.global.icu.text.DateFormatSymbols;
import ohos.global.icu.text.SimpleDateFormat;
import ohos.global.icu.util.ULocale;
import ohos.miscservices.download.DownloadSession;
import ohos.miscservices.httpaccess.HttpConstant;
import ohos.telephony.TelephoneNumberUtils;
import ohos.workschedulerservice.controller.WorkStatus;

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
    private static final String[] DEFAULT_PATTERNS = {"HH:mm:ss z", "HH:mm:ss z", "HH:mm:ss", "HH:mm", "EEEE, yyyy MMMM dd", "yyyy MMMM d", "yyyy MMM d", "yy/MM/dd", "{1} {0}", "{1} {0}", "{1} {0}", "{1} {0}", "{1} {0}"};
    public static final int DOW_LOCAL = 18;
    static final int[][][] DOW_PRECEDENCE = {new int[][]{new int[]{7}, new int[]{18}}};
    public static final int DST_OFFSET = 16;
    protected static final int EPOCH_JULIAN_DAY = 2440588;
    public static final int ERA = 0;
    public static final int EXTENDED_YEAR = 19;
    public static final int FEBRUARY = 1;
    private static final int FIELD_DIFF_MAX_INT = Integer.MAX_VALUE;
    private static final String[] FIELD_NAME = {"ERA", "YEAR", "MONTH", "WEEK_OF_YEAR", "WEEK_OF_MONTH", "DAY_OF_MONTH", "DAY_OF_YEAR", "DAY_OF_WEEK", "DAY_OF_WEEK_IN_MONTH", "AM_PM", "HOUR", "HOUR_OF_DAY", "MINUTE", "SECOND", "MILLISECOND", "ZONE_OFFSET", "DST_OFFSET", "YEAR_WOY", "DOW_LOCAL", "EXTENDED_YEAR", "JULIAN_DAY", "MILLISECONDS_IN_DAY"};
    private static final int[] FIND_ZONE_TRANSITION_TIME_UNITS = {ONE_HOUR, 1800000, 60000, 1000};
    public static final int FRIDAY = 6;
    protected static final int GREATEST_MINIMUM = 1;
    private static final int[][] GREGORIAN_MONTH_COUNT = {new int[]{31, 31, 0, 0}, new int[]{28, 29, 31, 31}, new int[]{31, 31, 59, 60}, new int[]{30, 30, 90, 91}, new int[]{31, 31, 120, 121}, new int[]{30, 30, 151, 152}, new int[]{31, 31, 181, 182}, new int[]{31, 31, 212, 213}, new int[]{30, 30, 243, 244}, new int[]{31, 31, 273, 274}, new int[]{30, 30, DownloadSession.PAUSED_UNKNOWN, 305}, new int[]{31, 31, 334, 335}};
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
    private static final int[][] LIMITS = {new int[0], new int[0], new int[0], new int[0], new int[0], new int[0], new int[0], new int[]{1, 1, 7, 7}, new int[0], new int[]{0, 0, 1, 1}, new int[]{0, 0, 11, 11}, new int[]{0, 0, 23, 23}, new int[]{0, 0, 59, 59}, new int[]{0, 0, 59, 59}, new int[]{0, 0, 999, 999}, new int[]{-43200000, -43200000, 43200000, 43200000}, new int[]{0, 0, ONE_HOUR, ONE_HOUR}, new int[0], new int[]{1, 1, 7, 7}, new int[0], new int[]{MIN_JULIAN, MIN_JULIAN, MAX_JULIAN, MAX_JULIAN}, new int[]{0, 0, 86399999, 86399999}, new int[]{0, 0, 1, 1}};
    public static final int MARCH = 2;
    protected static final int MAXIMUM = 3;
    protected static final Date MAX_DATE = new Date((long) MAX_MILLIS);
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
    protected static final Date MIN_DATE = new Date((long) MIN_MILLIS);
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
    private static int STAMP_MAX = 10000;
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
    private static final WeekDataCache WEEK_DATA_CACHE = new WeekDataCache(null);
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

    protected static final long julianDayToMillis(int i) {
        return ((long) (i - EPOCH_JULIAN_DAY)) * 86400000;
    }

    /* access modifiers changed from: protected */
    public int getDefaultDayInMonth(int i, int i2) {
        return 1;
    }

    /* access modifiers changed from: protected */
    public int getDefaultMonthInYear(int i) {
        return 0;
    }

    public String getType() {
        return "unknown";
    }

    /* access modifiers changed from: protected */
    public abstract int handleComputeMonthStart(int i, int i2, boolean z);

    /* access modifiers changed from: protected */
    public int[] handleCreateFields() {
        return new int[23];
    }

    /* access modifiers changed from: protected */
    public abstract int handleGetExtendedYear();

    /* access modifiers changed from: protected */
    public abstract int handleGetLimit(int i, int i2);

    @Deprecated
    public boolean haveDefaultCentury() {
        return true;
    }

    protected Calendar() {
        this(TimeZone.getDefault(), ULocale.getDefault(ULocale.Category.FORMAT));
    }

    protected Calendar(TimeZone timeZone, Locale locale) {
        this(timeZone, ULocale.forLocale(locale));
    }

    protected Calendar(TimeZone timeZone, ULocale uLocale) {
        this.lenient = true;
        this.repeatedWallTime = 0;
        this.skippedWallTime = 0;
        this.nextStamp = 2;
        this.zone = timeZone;
        setWeekData(getRegionForCalendar(uLocale));
        setCalendarLocale(uLocale);
        initInternal();
    }

    private void setCalendarLocale(ULocale uLocale) {
        if (!(uLocale.getVariant().length() == 0 && uLocale.getKeywords() == null)) {
            StringBuilder sb = new StringBuilder();
            sb.append(uLocale.getLanguage());
            String script = uLocale.getScript();
            if (script.length() > 0) {
                sb.append("_");
                sb.append(script);
            }
            String country = uLocale.getCountry();
            if (country.length() > 0) {
                sb.append("_");
                sb.append(country);
            }
            String keywordValue = uLocale.getKeywordValue("calendar");
            if (keywordValue != null) {
                sb.append("@calendar=");
                sb.append(keywordValue);
            }
            uLocale = new ULocale(sb.toString());
        }
        setLocale(uLocale, uLocale);
    }

    private void recalculateStamp() {
        int[] iArr;
        this.nextStamp = 1;
        for (int i = 0; i < this.stamp.length; i++) {
            int i2 = -1;
            int i3 = STAMP_MAX;
            int i4 = 0;
            while (true) {
                iArr = this.stamp;
                if (i4 >= iArr.length) {
                    break;
                }
                if (iArr[i4] > this.nextStamp && iArr[i4] < i3) {
                    i3 = iArr[i4];
                    i2 = i4;
                }
                i4++;
            }
            if (i2 < 0) {
                break;
            }
            int i5 = this.nextStamp + 1;
            this.nextStamp = i5;
            iArr[i2] = i5;
        }
        this.nextStamp++;
    }

    private void initInternal() {
        this.fields = handleCreateFields();
        int[] iArr = this.fields;
        if (iArr != null) {
            if (iArr.length >= 23 && iArr.length <= 32) {
                this.stamp = new int[iArr.length];
                int i = 4718695;
                for (int i2 = 23; i2 < this.fields.length; i2++) {
                    i |= 1 << i2;
                }
                this.internalSetMask = i;
                return;
            }
        }
        throw new IllegalStateException("Invalid fields[]");
    }

    public static Calendar getInstance() {
        return getInstanceInternal(null, null);
    }

    public static Calendar getInstance(TimeZone timeZone) {
        return getInstanceInternal(timeZone, null);
    }

    public static Calendar getInstance(Locale locale) {
        return getInstanceInternal(null, ULocale.forLocale(locale));
    }

    public static Calendar getInstance(ULocale uLocale) {
        return getInstanceInternal(null, uLocale);
    }

    public static Calendar getInstance(TimeZone timeZone, Locale locale) {
        return getInstanceInternal(timeZone, ULocale.forLocale(locale));
    }

    public static Calendar getInstance(TimeZone timeZone, ULocale uLocale) {
        return getInstanceInternal(timeZone, uLocale);
    }

    private static Calendar getInstanceInternal(TimeZone timeZone, ULocale uLocale) {
        if (uLocale == null) {
            uLocale = ULocale.getDefault(ULocale.Category.FORMAT);
        }
        if (timeZone == null) {
            timeZone = TimeZone.getDefault();
        }
        Calendar createInstance = createInstance(uLocale);
        createInstance.setTimeZone(timeZone);
        createInstance.setTimeInMillis(System.currentTimeMillis());
        return createInstance;
    }

    private static String getRegionForCalendar(ULocale uLocale) {
        String regionForSupplementalData = ULocale.getRegionForSupplementalData(uLocale, true);
        return regionForSupplementalData.length() == 0 ? "001" : regionForSupplementalData;
    }

    private static CalType getCalendarTypeForLocale(ULocale uLocale) {
        String calendarType = CalendarUtil.getCalendarType(uLocale);
        if (calendarType != null) {
            String lowerCase = calendarType.toLowerCase(Locale.ENGLISH);
            CalType[] values = CalType.values();
            for (CalType calType : values) {
                if (lowerCase.equals(calType.getId())) {
                    return calType;
                }
            }
        }
        return CalType.UNKNOWN;
    }

    private static Calendar createInstance(ULocale uLocale) {
        TimeZone timeZone = TimeZone.getDefault();
        CalType calendarTypeForLocale = getCalendarTypeForLocale(uLocale);
        if (calendarTypeForLocale == CalType.UNKNOWN) {
            calendarTypeForLocale = CalType.GREGORIAN;
        }
        switch (AnonymousClass1.$SwitchMap$ohos$global$icu$impl$CalType[calendarTypeForLocale.ordinal()]) {
            case 1:
                return new GregorianCalendar(timeZone, uLocale);
            case 2:
                GregorianCalendar gregorianCalendar = new GregorianCalendar(timeZone, uLocale);
                gregorianCalendar.setFirstDayOfWeek(2);
                gregorianCalendar.setMinimalDaysInFirstWeek(4);
                return gregorianCalendar;
            case 3:
                return new BuddhistCalendar(timeZone, uLocale);
            case 4:
                return new ChineseCalendar(timeZone, uLocale);
            case 5:
                return new CopticCalendar(timeZone, uLocale);
            case 6:
                return new DangiCalendar(timeZone, uLocale);
            case 7:
                return new EthiopicCalendar(timeZone, uLocale);
            case 8:
                EthiopicCalendar ethiopicCalendar = new EthiopicCalendar(timeZone, uLocale);
                ethiopicCalendar.setAmeteAlemEra(true);
                return ethiopicCalendar;
            case 9:
                return new HebrewCalendar(timeZone, uLocale);
            case 10:
                return new IndianCalendar(timeZone, uLocale);
            case 11:
            case 12:
            case 13:
            case 14:
            case 15:
                return new IslamicCalendar(timeZone, uLocale);
            case 16:
                return new JapaneseCalendar(timeZone, uLocale);
            case 17:
                return new PersianCalendar(timeZone, uLocale);
            case 18:
                return new TaiwanCalendar(timeZone, uLocale);
            default:
                throw new IllegalArgumentException("Unknown calendar type");
        }
    }

    /* access modifiers changed from: package-private */
    /* renamed from: ohos.global.icu.util.Calendar$1  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$ohos$global$icu$impl$CalType = new int[CalType.values().length];

        static {
            try {
                $SwitchMap$ohos$global$icu$impl$CalType[CalType.GREGORIAN.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$ohos$global$icu$impl$CalType[CalType.ISO8601.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$ohos$global$icu$impl$CalType[CalType.BUDDHIST.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                $SwitchMap$ohos$global$icu$impl$CalType[CalType.CHINESE.ordinal()] = 4;
            } catch (NoSuchFieldError unused4) {
            }
            try {
                $SwitchMap$ohos$global$icu$impl$CalType[CalType.COPTIC.ordinal()] = 5;
            } catch (NoSuchFieldError unused5) {
            }
            try {
                $SwitchMap$ohos$global$icu$impl$CalType[CalType.DANGI.ordinal()] = 6;
            } catch (NoSuchFieldError unused6) {
            }
            try {
                $SwitchMap$ohos$global$icu$impl$CalType[CalType.ETHIOPIC.ordinal()] = 7;
            } catch (NoSuchFieldError unused7) {
            }
            try {
                $SwitchMap$ohos$global$icu$impl$CalType[CalType.ETHIOPIC_AMETE_ALEM.ordinal()] = 8;
            } catch (NoSuchFieldError unused8) {
            }
            try {
                $SwitchMap$ohos$global$icu$impl$CalType[CalType.HEBREW.ordinal()] = 9;
            } catch (NoSuchFieldError unused9) {
            }
            try {
                $SwitchMap$ohos$global$icu$impl$CalType[CalType.INDIAN.ordinal()] = 10;
            } catch (NoSuchFieldError unused10) {
            }
            try {
                $SwitchMap$ohos$global$icu$impl$CalType[CalType.ISLAMIC_CIVIL.ordinal()] = 11;
            } catch (NoSuchFieldError unused11) {
            }
            try {
                $SwitchMap$ohos$global$icu$impl$CalType[CalType.ISLAMIC_UMALQURA.ordinal()] = 12;
            } catch (NoSuchFieldError unused12) {
            }
            try {
                $SwitchMap$ohos$global$icu$impl$CalType[CalType.ISLAMIC_TBLA.ordinal()] = 13;
            } catch (NoSuchFieldError unused13) {
            }
            try {
                $SwitchMap$ohos$global$icu$impl$CalType[CalType.ISLAMIC_RGSA.ordinal()] = 14;
            } catch (NoSuchFieldError unused14) {
            }
            try {
                $SwitchMap$ohos$global$icu$impl$CalType[CalType.ISLAMIC.ordinal()] = 15;
            } catch (NoSuchFieldError unused15) {
            }
            try {
                $SwitchMap$ohos$global$icu$impl$CalType[CalType.JAPANESE.ordinal()] = 16;
            } catch (NoSuchFieldError unused16) {
            }
            try {
                $SwitchMap$ohos$global$icu$impl$CalType[CalType.PERSIAN.ordinal()] = 17;
            } catch (NoSuchFieldError unused17) {
            }
            try {
                $SwitchMap$ohos$global$icu$impl$CalType[CalType.ROC.ordinal()] = 18;
            } catch (NoSuchFieldError unused18) {
            }
        }
    }

    public static Locale[] getAvailableLocales() {
        return ICUResourceBundle.getAvailableLocales();
    }

    public static ULocale[] getAvailableULocales() {
        return ICUResourceBundle.getAvailableULocales();
    }

    public static final String[] getKeywordValuesForLocale(String str, ULocale uLocale, boolean z) {
        UResourceBundle uResourceBundle;
        String regionForSupplementalData = ULocale.getRegionForSupplementalData(uLocale, true);
        ArrayList arrayList = new ArrayList();
        UResourceBundle uResourceBundle2 = UResourceBundle.getBundleInstance("ohos/global/icu/impl/data/icudt66b", "supplementalData", ICUResourceBundle.ICU_DATA_CLASS_LOADER).get("calendarPreferenceData");
        try {
            uResourceBundle = uResourceBundle2.get(regionForSupplementalData);
        } catch (MissingResourceException unused) {
            uResourceBundle = uResourceBundle2.get("001");
        }
        String[] stringArray = uResourceBundle.getStringArray();
        if (z) {
            return stringArray;
        }
        for (String str2 : stringArray) {
            arrayList.add(str2);
        }
        CalType[] values = CalType.values();
        for (CalType calType : values) {
            if (!arrayList.contains(calType.getId())) {
                arrayList.add(calType.getId());
            }
        }
        return (String[]) arrayList.toArray(new String[arrayList.size()]);
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

    public void setTimeInMillis(long j) {
        if (j > MAX_MILLIS) {
            if (isLenient()) {
                j = 183882168921600000L;
            } else {
                throw new IllegalArgumentException("millis value greater than upper bounds for a Calendar : " + j);
            }
        } else if (j < MIN_MILLIS) {
            if (isLenient()) {
                j = -184303902528000000L;
            } else {
                throw new IllegalArgumentException("millis value less than lower bounds for a Calendar : " + j);
            }
        }
        this.time = j;
        this.areAllFieldsSet = false;
        this.areFieldsSet = false;
        this.areFieldsVirtuallySet = true;
        this.isTimeSet = true;
        int i = 0;
        while (true) {
            int[] iArr = this.fields;
            if (i < iArr.length) {
                this.stamp[i] = 0;
                iArr[i] = 0;
                i++;
            } else {
                return;
            }
        }
    }

    public final int get(int i) {
        complete();
        return this.fields[i];
    }

    /* access modifiers changed from: protected */
    public final int internalGet(int i) {
        return this.fields[i];
    }

    /* access modifiers changed from: protected */
    public final int internalGet(int i, int i2) {
        return this.stamp[i] > 0 ? this.fields[i] : i2;
    }

    public final void set(int i, int i2) {
        if (this.areFieldsVirtuallySet) {
            computeFields();
        }
        this.fields[i] = i2;
        if (this.nextStamp == STAMP_MAX) {
            recalculateStamp();
        }
        int[] iArr = this.stamp;
        int i3 = this.nextStamp;
        this.nextStamp = i3 + 1;
        iArr[i] = i3;
        this.areFieldsVirtuallySet = false;
        this.areFieldsSet = false;
        this.isTimeSet = false;
    }

    public final void set(int i, int i2, int i3) {
        set(1, i);
        set(2, i2);
        set(5, i3);
    }

    public final void set(int i, int i2, int i3, int i4, int i5) {
        set(1, i);
        set(2, i2);
        set(5, i3);
        set(11, i4);
        set(12, i5);
    }

    public final void set(int i, int i2, int i3, int i4, int i5, int i6) {
        set(1, i);
        set(2, i2);
        set(5, i3);
        set(11, i4);
        set(12, i5);
        set(13, i6);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:3:0x0010, code lost:
        if ((r2 % 67) >= 33) goto L_0x001f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:5:0x001d, code lost:
        if (((-r2) % 67) <= 33) goto L_0x001f;
     */
    private static int gregoYearFromIslamicStart(int i) {
        int i2;
        int i3 = 0;
        if (i >= 1397) {
            int i4 = i - 1397;
            i2 = (i4 / 67) * 2;
        } else {
            int i5 = i - 1396;
            i2 = ((i5 / 67) - 1) * 2;
        }
        i3 = 1;
        return (i + 579) - (i2 + i3);
    }

    @Deprecated
    public final int getRelatedYear() {
        int i = get(19);
        CalType calType = CalType.GREGORIAN;
        String type = getType();
        CalType[] values = CalType.values();
        int length = values.length;
        int i2 = 0;
        while (true) {
            if (i2 >= length) {
                break;
            }
            CalType calType2 = values[i2];
            if (type.equals(calType2.getId())) {
                calType = calType2;
                break;
            }
            i2++;
        }
        switch (AnonymousClass1.$SwitchMap$ohos$global$icu$impl$CalType[calType.ordinal()]) {
            case 4:
                return i - 2637;
            case 5:
                return i + 284;
            case 6:
                return i - 2333;
            case 7:
                return i + 8;
            case 8:
                return i - 5492;
            case 9:
                return i - 3760;
            case 10:
                return i + 79;
            case 11:
            case 12:
            case 13:
            case 14:
            case 15:
                return gregoYearFromIslamicStart(i);
            case 16:
            default:
                return i;
            case 17:
                return i + 622;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:3:0x0010, code lost:
        if ((r2 % 65) >= 32) goto L_0x001f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:5:0x001d, code lost:
        if (((-r2) % 65) <= 32) goto L_0x001f;
     */
    private static int firstIslamicStartYearFromGrego(int i) {
        int i2;
        int i3 = 0;
        if (i >= 1977) {
            int i4 = i - 1977;
            i2 = (i4 / 65) * 2;
        } else {
            int i5 = i - 1976;
            i2 = ((i5 / 65) - 1) * 2;
        }
        i3 = 1;
        return (i - 579) + i2 + i3;
    }

    @Deprecated
    public final void setRelatedYear(int i) {
        CalType calType = CalType.GREGORIAN;
        String type = getType();
        CalType[] values = CalType.values();
        int length = values.length;
        int i2 = 0;
        while (true) {
            if (i2 >= length) {
                break;
            }
            CalType calType2 = values[i2];
            if (type.equals(calType2.getId())) {
                calType = calType2;
                break;
            }
            i2++;
        }
        switch (AnonymousClass1.$SwitchMap$ohos$global$icu$impl$CalType[calType.ordinal()]) {
            case 4:
                i += 2637;
                break;
            case 5:
                i -= 284;
                break;
            case 6:
                i += 2333;
                break;
            case 7:
                i -= 8;
                break;
            case 8:
                i += 5492;
                break;
            case 9:
                i += 3760;
                break;
            case 10:
                i -= 79;
                break;
            case 11:
            case 12:
            case 13:
            case 14:
            case 15:
                i = firstIslamicStartYearFromGrego(i);
                break;
            case 17:
                i -= 622;
                break;
        }
        set(19, i);
    }

    public final void clear() {
        int i = 0;
        while (true) {
            int[] iArr = this.fields;
            if (i < iArr.length) {
                this.stamp[i] = 0;
                iArr[i] = 0;
                i++;
            } else {
                this.areFieldsVirtuallySet = false;
                this.areAllFieldsSet = false;
                this.areFieldsSet = false;
                this.isTimeSet = false;
                return;
            }
        }
    }

    public final void clear(int i) {
        if (this.areFieldsVirtuallySet) {
            computeFields();
        }
        this.fields[i] = 0;
        this.stamp[i] = 0;
        this.areFieldsVirtuallySet = false;
        this.areAllFieldsSet = false;
        this.areFieldsSet = false;
        this.isTimeSet = false;
    }

    public final boolean isSet(int i) {
        return this.areFieldsVirtuallySet || this.stamp[i] != 0;
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

    @Override // java.lang.Object
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Calendar calendar = (Calendar) obj;
        return isEquivalentTo(calendar) && getTimeInMillis() == calendar.getTime().getTime();
    }

    public boolean isEquivalentTo(Calendar calendar) {
        return getClass() == calendar.getClass() && isLenient() == calendar.isLenient() && getFirstDayOfWeek() == calendar.getFirstDayOfWeek() && getMinimalDaysInFirstWeek() == calendar.getMinimalDaysInFirstWeek() && getTimeZone().equals(calendar.getTimeZone()) && getRepeatedWallTimeOption() == calendar.getRepeatedWallTimeOption() && getSkippedWallTimeOption() == calendar.getSkippedWallTimeOption();
    }

    @Override // java.lang.Object
    public int hashCode() {
        int i = this.lenient ? 1 : 0;
        return (this.zone.hashCode() << 11) | i | (this.firstDayOfWeek << 1) | (this.minimalDaysInFirstWeek << 4) | (this.repeatedWallTime << 7) | (this.skippedWallTime << 9);
    }

    private long compare(Object obj) {
        long j;
        if (obj instanceof Calendar) {
            j = ((Calendar) obj).getTimeInMillis();
        } else if (obj instanceof Date) {
            j = ((Date) obj).getTime();
        } else {
            throw new IllegalArgumentException(obj + "is not a Calendar or Date");
        }
        return getTimeInMillis() - j;
    }

    public boolean before(Object obj) {
        return compare(obj) < 0;
    }

    public boolean after(Object obj) {
        return compare(obj) > 0;
    }

    public int getActualMaximum(int i) {
        if (!(i == 0 || i == 18)) {
            if (i == 5) {
                Calendar calendar = (Calendar) clone();
                calendar.setLenient(true);
                calendar.prepareGetActual(i, false);
                return handleGetMonthLength(calendar.get(19), calendar.get(2));
            } else if (i == 6) {
                Calendar calendar2 = (Calendar) clone();
                calendar2.setLenient(true);
                calendar2.prepareGetActual(i, false);
                return handleGetYearLength(calendar2.get(19));
            } else if (!(i == 7 || i == 20 || i == 21)) {
                switch (i) {
                    case 9:
                    case 10:
                    case 11:
                    case 12:
                    case 13:
                    case 14:
                    case 15:
                    case 16:
                        break;
                    default:
                        return getActualHelper(i, getLeastMaximum(i), getMaximum(i));
                }
            }
        }
        return getMaximum(i);
    }

    public int getActualMinimum(int i) {
        switch (i) {
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
                return getMinimum(i);
            case 8:
            case 17:
            case 19:
            default:
                return getActualHelper(i, getGreatestMinimum(i), getMinimum(i));
        }
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x001e, code lost:
        if (r6 != 19) goto L_0x0053;
     */
    public void prepareGetActual(int i, boolean z) {
        set(21, 0);
        if (i != 1) {
            if (i == 2) {
                set(5, getGreatestMinimum(5));
            } else if (i == 3 || i == 4) {
                int i2 = this.firstDayOfWeek;
                if (z && (i2 = (i2 + 6) % 7) < 1) {
                    i2 += 7;
                }
                set(7, i2);
            } else if (i == 8) {
                set(5, 1);
                set(7, get(7));
            } else if (i == 17) {
                set(3, getGreatestMinimum(3));
            }
            set(i, getGreatestMinimum(i));
        }
        set(6, getGreatestMinimum(6));
        set(i, getGreatestMinimum(i));
    }

    private int getActualHelper(int i, int i2, int i3) {
        int i4;
        if (i2 == i3) {
            return i2;
        }
        boolean z = true;
        int i5 = i3 > i2 ? 1 : -1;
        Calendar calendar = (Calendar) clone();
        calendar.complete();
        calendar.setLenient(true);
        if (i5 >= 0) {
            z = false;
        }
        calendar.prepareGetActual(i, z);
        calendar.set(i, i2);
        if (calendar.get(i) != i2 && i != 4 && i5 > 0) {
            return i2;
        }
        do {
            i4 = i2 + i5;
            calendar.add(i, i5);
            if (calendar.get(i) != i4) {
                break;
            }
            i2 = i4;
        } while (i4 != i3);
        return i2;
    }

    public final void roll(int i, boolean z) {
        roll(i, z ? 1 : -1);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:101:0x01ef, code lost:
        if (r13 < 1) goto L_0x01f1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:93:0x01d3, code lost:
        if (r0 != false) goto L_0x01f1;
     */
    public void roll(int i, int i2) {
        if (i2 != 0) {
            complete();
            int i3 = 1;
            switch (i) {
                case 0:
                case 5:
                case 9:
                case 12:
                case 13:
                case 14:
                case 21:
                    int actualMinimum = getActualMinimum(i);
                    int actualMaximum = (getActualMaximum(i) - actualMinimum) + 1;
                    int internalGet = ((internalGet(i) + i2) - actualMinimum) % actualMaximum;
                    if (internalGet < 0) {
                        internalGet += actualMaximum;
                    }
                    set(i, internalGet + actualMinimum);
                    return;
                case 1:
                case 17:
                    boolean z = false;
                    int i4 = get(0);
                    if (i4 == 0) {
                        String type = getType();
                        if (type.equals("gregorian") || type.equals("roc") || type.equals("coptic")) {
                            i2 = -i2;
                            z = true;
                        }
                    }
                    int internalGet2 = i2 + internalGet(i);
                    if (i4 > 0 || internalGet2 >= 1) {
                        int actualMaximum2 = getActualMaximum(i);
                        if (actualMaximum2 < 32768) {
                            if (internalGet2 < 1) {
                                i3 = actualMaximum2 - ((-internalGet2) % actualMaximum2);
                            } else if (internalGet2 > actualMaximum2) {
                                i3 = 1 + ((internalGet2 - 1) % actualMaximum2);
                            }
                            set(i, i3);
                            pinField(2);
                            pinField(5);
                            return;
                        }
                    }
                    i3 = internalGet2;
                    set(i, i3);
                    pinField(2);
                    pinField(5);
                    return;
                case 2:
                    int actualMaximum3 = getActualMaximum(2) + 1;
                    int internalGet3 = (internalGet(2) + i2) % actualMaximum3;
                    if (internalGet3 < 0) {
                        internalGet3 += actualMaximum3;
                    }
                    set(2, internalGet3);
                    pinField(5);
                    return;
                case 3:
                    int internalGet4 = internalGet(7) - getFirstDayOfWeek();
                    if (internalGet4 < 0) {
                        internalGet4 += 7;
                    }
                    int internalGet5 = ((internalGet4 - internalGet(6)) + 1) % 7;
                    if (internalGet5 < 0) {
                        internalGet5 += 7;
                    }
                    int i5 = 7 - internalGet5 < getMinimalDaysInFirstWeek() ? 8 - internalGet5 : 1 - internalGet5;
                    int actualMaximum4 = getActualMaximum(6);
                    int internalGet6 = ((actualMaximum4 + 7) - (((actualMaximum4 - internalGet(6)) + internalGet4) % 7)) - i5;
                    int internalGet7 = ((internalGet(6) + (i2 * 7)) - i5) % internalGet6;
                    if (internalGet7 < 0) {
                        internalGet7 += internalGet6;
                    }
                    int i6 = internalGet7 + i5;
                    if (i6 < 1) {
                        i6 = 1;
                    }
                    if (i6 > actualMaximum4) {
                        i6 = actualMaximum4;
                    }
                    set(6, i6);
                    clear(2);
                    return;
                case 4:
                    int internalGet8 = internalGet(7) - getFirstDayOfWeek();
                    if (internalGet8 < 0) {
                        internalGet8 += 7;
                    }
                    int internalGet9 = ((internalGet8 - internalGet(5)) + 1) % 7;
                    if (internalGet9 < 0) {
                        internalGet9 += 7;
                    }
                    int i7 = 7 - internalGet9 < getMinimalDaysInFirstWeek() ? 8 - internalGet9 : 1 - internalGet9;
                    int actualMaximum5 = getActualMaximum(5);
                    int internalGet10 = ((actualMaximum5 + 7) - (((actualMaximum5 - internalGet(5)) + internalGet8) % 7)) - i7;
                    int internalGet11 = ((internalGet(5) + (i2 * 7)) - i7) % internalGet10;
                    if (internalGet11 < 0) {
                        internalGet11 += internalGet10;
                    }
                    int i8 = internalGet11 + i7;
                    if (i8 < 1) {
                        i8 = 1;
                    }
                    if (i8 > actualMaximum5) {
                        i8 = actualMaximum5;
                    }
                    set(5, i8);
                    return;
                case 6:
                    long internalGet12 = this.time - (((long) (internalGet(6) - 1)) * 86400000);
                    long actualMaximum6 = ((long) getActualMaximum(6)) * 86400000;
                    this.time = ((this.time + (((long) i2) * 86400000)) - internalGet12) % actualMaximum6;
                    long j = this.time;
                    if (j < 0) {
                        this.time = j + actualMaximum6;
                    }
                    setTimeInMillis(this.time + internalGet12);
                    return;
                case 7:
                case 18:
                    long j2 = ((long) i2) * 86400000;
                    int internalGet13 = internalGet(i);
                    if (i == 7) {
                        i3 = getFirstDayOfWeek();
                    }
                    int i9 = internalGet13 - i3;
                    if (i9 < 0) {
                        i9 += 7;
                    }
                    long j3 = this.time;
                    long j4 = j3 - (((long) i9) * 86400000);
                    this.time = ((j3 + j2) - j4) % ONE_WEEK;
                    long j5 = this.time;
                    if (j5 < 0) {
                        this.time = j5 + ONE_WEEK;
                    }
                    setTimeInMillis(this.time + j4);
                    return;
                case 8:
                    long j6 = ((long) i2) * ONE_WEEK;
                    int internalGet14 = (internalGet(5) - 1) / 7;
                    long j7 = this.time;
                    long j8 = j7 - (((long) internalGet14) * ONE_WEEK);
                    long actualMaximum7 = ((long) (internalGet14 + ((getActualMaximum(5) - internalGet(5)) / 7) + 1)) * ONE_WEEK;
                    this.time = ((j7 + j6) - j8) % actualMaximum7;
                    long j9 = this.time;
                    if (j9 < 0) {
                        this.time = j9 + actualMaximum7;
                    }
                    setTimeInMillis(this.time + j8);
                    return;
                case 10:
                case 11:
                    long timeInMillis = getTimeInMillis();
                    int internalGet15 = internalGet(i);
                    int maximum = getMaximum(i) + 1;
                    int i10 = (i2 + internalGet15) % maximum;
                    if (i10 < 0) {
                        i10 += maximum;
                    }
                    setTimeInMillis(timeInMillis + ((((long) i10) - ((long) internalGet15)) * 3600000));
                    return;
                case 15:
                case 16:
                default:
                    throw new IllegalArgumentException("Calendar.roll(" + fieldName(i) + ") not supported");
                case 19:
                    set(i, internalGet(i) + i2);
                    pinField(2);
                    pinField(5);
                    return;
                case 20:
                    set(i, internalGet(i) + i2);
                    return;
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:18:0x004c  */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x005d  */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x0068  */
    /* JADX WARNING: Removed duplicated region for block: B:57:0x0109  */
    /* JADX WARNING: Removed duplicated region for block: B:61:? A[ADDED_TO_REGION, RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:71:? A[RETURN, SYNTHETIC] */
    public void add(int i, int i2) {
        boolean isLenient;
        boolean z;
        int i3;
        int i4;
        long j;
        long j2;
        if (i2 != 0) {
            long j3 = (long) i2;
            int i5 = 0;
            switch (i) {
                case 0:
                    set(i, get(i) + i2);
                    pinField(0);
                    return;
                case 1:
                case 17:
                    if (get(0) == 0) {
                        String type = getType();
                        if (type.equals("gregorian") || type.equals("roc") || type.equals("coptic")) {
                            i2 = -i2;
                        }
                    }
                    isLenient = isLenient();
                    setLenient(true);
                    set(i, get(i) + i2);
                    pinField(5);
                    if (isLenient) {
                        complete();
                        setLenient(isLenient);
                        return;
                    }
                    return;
                case 2:
                case 19:
                    isLenient = isLenient();
                    setLenient(true);
                    set(i, get(i) + i2);
                    pinField(5);
                    if (isLenient) {
                    }
                    break;
                case 3:
                case 4:
                case 8:
                    j = ONE_WEEK;
                    j3 *= j;
                    z = true;
                    if (z) {
                        i3 = get(16) + get(15);
                        i5 = get(21);
                    } else {
                        i3 = 0;
                    }
                    setTimeInMillis(getTimeInMillis() + j3);
                    if (!z && (i4 = get(21)) != i5) {
                        long internalGetTimeInMillis = internalGetTimeInMillis();
                        int i6 = get(16) + get(15);
                        if (i6 != i3) {
                            long j4 = ((long) (i3 - i6)) % 86400000;
                            int i7 = (j4 > 0 ? 1 : (j4 == 0 ? 0 : -1));
                            if (i7 != 0) {
                                setTimeInMillis(j4 + internalGetTimeInMillis);
                                i4 = get(21);
                            }
                            if (i4 != i5) {
                                int i8 = this.skippedWallTime;
                                if (i8 != 0) {
                                    if (i8 != 1) {
                                        if (i8 == 2) {
                                            if (i7 > 0) {
                                                internalGetTimeInMillis = internalGetTimeInMillis();
                                            }
                                            Long immediatePreviousZoneTransition = getImmediatePreviousZoneTransition(internalGetTimeInMillis);
                                            if (immediatePreviousZoneTransition != null) {
                                                setTimeInMillis(immediatePreviousZoneTransition.longValue());
                                                return;
                                            }
                                            throw new RuntimeException("Could not locate a time zone transition before " + internalGetTimeInMillis);
                                        }
                                        return;
                                    } else if (i7 > 0) {
                                        setTimeInMillis(internalGetTimeInMillis);
                                        return;
                                    } else {
                                        return;
                                    }
                                } else if (i7 < 0) {
                                    setTimeInMillis(internalGetTimeInMillis);
                                    return;
                                } else {
                                    return;
                                }
                            } else {
                                return;
                            }
                        } else {
                            return;
                        }
                    } else {
                        return;
                    }
                case 5:
                case 6:
                case 7:
                case 18:
                case 20:
                    j3 *= 86400000;
                    z = true;
                    if (z) {
                    }
                    setTimeInMillis(getTimeInMillis() + j3);
                    if (!z) {
                        return;
                    }
                    return;
                case 9:
                    j = 43200000;
                    j3 *= j;
                    z = true;
                    if (z) {
                    }
                    setTimeInMillis(getTimeInMillis() + j3);
                    if (!z) {
                    }
                    break;
                case 10:
                case 11:
                    j2 = 3600000;
                    j3 *= j2;
                    z = false;
                    if (z) {
                    }
                    setTimeInMillis(getTimeInMillis() + j3);
                    if (!z) {
                    }
                    break;
                case 12:
                    j2 = 60000;
                    j3 *= j2;
                    z = false;
                    if (z) {
                    }
                    setTimeInMillis(getTimeInMillis() + j3);
                    if (!z) {
                    }
                    break;
                case 13:
                    j2 = 1000;
                    j3 *= j2;
                    z = false;
                    if (z) {
                    }
                    setTimeInMillis(getTimeInMillis() + j3);
                    if (!z) {
                    }
                    break;
                case 14:
                case 21:
                    z = false;
                    if (z) {
                    }
                    setTimeInMillis(getTimeInMillis() + j3);
                    if (!z) {
                    }
                    break;
                case 15:
                case 16:
                default:
                    throw new IllegalArgumentException("Calendar.add(" + fieldName(i) + ") not supported");
            }
        }
    }

    public String getDisplayName(Locale locale) {
        return getClass().getName();
    }

    public String getDisplayName(ULocale uLocale) {
        return getClass().getName();
    }

    public int compareTo(Calendar calendar) {
        int i = ((getTimeInMillis() - calendar.getTimeInMillis()) > 0 ? 1 : ((getTimeInMillis() - calendar.getTimeInMillis()) == 0 ? 0 : -1));
        if (i < 0) {
            return -1;
        }
        return i > 0 ? 1 : 0;
    }

    public DateFormat getDateTimeFormat(int i, int i2, Locale locale) {
        return formatHelper(this, ULocale.forLocale(locale), i, i2);
    }

    public DateFormat getDateTimeFormat(int i, int i2, ULocale uLocale) {
        return formatHelper(this, uLocale, i, i2);
    }

    /* access modifiers changed from: protected */
    public DateFormat handleGetDateFormat(String str, Locale locale) {
        return handleGetDateFormat(str, (String) null, ULocale.forLocale(locale));
    }

    /* access modifiers changed from: protected */
    public DateFormat handleGetDateFormat(String str, String str2, Locale locale) {
        return handleGetDateFormat(str, str2, ULocale.forLocale(locale));
    }

    /* access modifiers changed from: protected */
    public DateFormat handleGetDateFormat(String str, ULocale uLocale) {
        return handleGetDateFormat(str, (String) null, uLocale);
    }

    /* access modifiers changed from: protected */
    public DateFormat handleGetDateFormat(String str, String str2, ULocale uLocale) {
        FormatConfiguration formatConfiguration = new FormatConfiguration(null);
        formatConfiguration.pattern = str;
        formatConfiguration.override = str2;
        formatConfiguration.formatData = new DateFormatSymbols(this, uLocale);
        formatConfiguration.loc = uLocale;
        formatConfiguration.cal = this;
        return SimpleDateFormat.getInstance(formatConfiguration);
    }

    private static DateFormat formatHelper(Calendar calendar, ULocale uLocale, int i, int i2) {
        String str;
        if (i2 < -1 || i2 > 3) {
            throw new IllegalArgumentException("Illegal time style " + i2);
        } else if (i < -1 || i > 3) {
            throw new IllegalArgumentException("Illegal date style " + i);
        } else {
            PatternData make = PatternData.make(calendar, uLocale);
            String str2 = null;
            if (i2 >= 0 && i >= 0) {
                String dateTimePattern = make.getDateTimePattern(i);
                int i3 = i + 4;
                str = SimpleFormatterImpl.formatRawPattern(dateTimePattern, 2, 2, new CharSequence[]{make.patterns[i2], make.patterns[i3]});
                if (make.overrides != null) {
                    str2 = mergeOverrideStrings(make.patterns[i3], make.patterns[i2], make.overrides[i3], make.overrides[i2]);
                }
            } else if (i2 >= 0) {
                str = make.patterns[i2];
                if (make.overrides != null) {
                    str2 = make.overrides[i2];
                }
            } else if (i >= 0) {
                int i4 = i + 4;
                str = make.patterns[i4];
                if (make.overrides != null) {
                    str2 = make.overrides[i4];
                }
            } else {
                throw new IllegalArgumentException("No date or time style specified");
            }
            DateFormat handleGetDateFormat = calendar.handleGetDateFormat(str, str2, uLocale);
            handleGetDateFormat.setCalendar(calendar);
            return handleGetDateFormat;
        }
    }

    /* access modifiers changed from: package-private */
    public static class PatternData {
        private String[] overrides;
        private String[] patterns;

        public PatternData(String[] strArr, String[] strArr2) {
            this.patterns = strArr;
            this.overrides = strArr2;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private String getDateTimePattern(int i) {
            int i2 = 8;
            if (this.patterns.length >= 13) {
                i2 = 8 + i + 1;
            }
            return this.patterns[i2];
        }

        /* access modifiers changed from: private */
        public static PatternData make(Calendar calendar, ULocale uLocale) {
            PatternData patternData;
            String type = calendar.getType();
            String str = uLocale.getBaseName() + "+" + type;
            PatternData patternData2 = (PatternData) Calendar.PATTERN_CACHE.get(str);
            if (patternData2 == null) {
                try {
                    patternData = Calendar.getPatternData(uLocale, type);
                } catch (MissingResourceException unused) {
                    patternData = new PatternData(Calendar.DEFAULT_PATTERNS, null);
                }
                patternData2 = patternData;
                Calendar.PATTERN_CACHE.put(str, patternData2);
            }
            return patternData2;
        }
    }

    /* access modifiers changed from: private */
    public static PatternData getPatternData(ULocale uLocale, String str) {
        ICUResourceBundle bundleInstance = UResourceBundle.getBundleInstance("ohos/global/icu/impl/data/icudt66b", uLocale);
        ICUResourceBundle findWithFallback = bundleInstance.findWithFallback("calendar/" + str + "/DateTimePatterns");
        if (findWithFallback == null) {
            findWithFallback = bundleInstance.getWithFallback("calendar/gregorian/DateTimePatterns");
        }
        int size = findWithFallback.getSize();
        String[] strArr = new String[size];
        String[] strArr2 = new String[size];
        for (int i = 0; i < size; i++) {
            ICUResourceBundle iCUResourceBundle = findWithFallback.get(i);
            int type = iCUResourceBundle.getType();
            if (type == 0) {
                strArr[i] = iCUResourceBundle.getString();
            } else if (type == 8) {
                strArr[i] = iCUResourceBundle.getString(0);
                strArr2[i] = iCUResourceBundle.getString(1);
            }
        }
        return new PatternData(strArr, strArr2);
    }

    @Deprecated
    public static String getDateTimePattern(Calendar calendar, ULocale uLocale, int i) {
        return PatternData.make(calendar, uLocale).getDateTimePattern(i);
    }

    private static String mergeOverrideStrings(String str, String str2, String str3, String str4) {
        if (str3 == null && str4 == null) {
            return null;
        }
        if (str3 == null) {
            return expandOverride(str2, str4);
        }
        if (str4 == null) {
            return expandOverride(str, str3);
        }
        if (str3.equals(str4)) {
            return str3;
        }
        return expandOverride(str, str3) + ";" + expandOverride(str2, str4);
    }

    private static String expandOverride(String str, String str2) {
        if (str2.indexOf(61) >= 0) {
            return str2;
        }
        boolean z = false;
        char c = ' ';
        StringBuilder sb = new StringBuilder();
        StringCharacterIterator stringCharacterIterator = new StringCharacterIterator(str);
        char first = stringCharacterIterator.first();
        while (true) {
            c = first;
            if (c == 65535) {
                return sb.toString();
            }
            if (c == '\'') {
                z = !z;
            } else if (!z && c != c) {
                if (sb.length() > 0) {
                    sb.append(";");
                }
                sb.append(c);
                sb.append("=");
                sb.append(str2);
            }
            first = stringCharacterIterator.next();
        }
    }

    @Deprecated
    public static class FormatConfiguration {
        private Calendar cal;
        private DateFormatSymbols formatData;
        private ULocale loc;
        private String override;
        private String pattern;

        /* synthetic */ FormatConfiguration(AnonymousClass1 r1) {
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

    /* access modifiers changed from: protected */
    public void pinField(int i) {
        int actualMaximum = getActualMaximum(i);
        int actualMinimum = getActualMinimum(i);
        int[] iArr = this.fields;
        if (iArr[i] > actualMaximum) {
            set(i, actualMaximum);
        } else if (iArr[i] < actualMinimum) {
            set(i, actualMinimum);
        }
    }

    /* access modifiers changed from: protected */
    public int weekNumber(int i, int i2, int i3) {
        int firstDayOfWeek2 = (((i3 - getFirstDayOfWeek()) - i2) + 1) % 7;
        if (firstDayOfWeek2 < 0) {
            firstDayOfWeek2 += 7;
        }
        int i4 = ((i + firstDayOfWeek2) - 1) / 7;
        return 7 - firstDayOfWeek2 >= getMinimalDaysInFirstWeek() ? i4 + 1 : i4;
    }

    /* access modifiers changed from: protected */
    public final int weekNumber(int i, int i2) {
        return weekNumber(i, i, i2);
    }

    public int fieldDifference(Date date, int i) {
        long timeInMillis = getTimeInMillis();
        long time2 = date.getTime();
        int i2 = (timeInMillis > time2 ? 1 : (timeInMillis == time2 ? 0 : -1));
        int i3 = 0;
        if (i2 < 0) {
            int i4 = 0;
            int i5 = 1;
            while (true) {
                setTimeInMillis(timeInMillis);
                add(i, i5);
                int i6 = (getTimeInMillis() > time2 ? 1 : (getTimeInMillis() == time2 ? 0 : -1));
                if (i6 == 0) {
                    return i5;
                }
                if (i6 > 0) {
                    while (true) {
                        int i7 = i5 - i4;
                        if (i7 <= 1) {
                            i3 = i4;
                            break;
                        }
                        int i8 = (i7 / 2) + i4;
                        setTimeInMillis(timeInMillis);
                        add(i, i8);
                        int i9 = (getTimeInMillis() > time2 ? 1 : (getTimeInMillis() == time2 ? 0 : -1));
                        if (i9 == 0) {
                            return i8;
                        }
                        if (i9 > 0) {
                            i5 = i8;
                        } else {
                            i4 = i8;
                        }
                    }
                } else {
                    int i10 = Integer.MAX_VALUE;
                    if (i5 < Integer.MAX_VALUE) {
                        int i11 = i5 << 1;
                        if (i11 >= 0) {
                            i10 = i11;
                        }
                        i5 = i10;
                        i4 = i5;
                    } else {
                        throw new RuntimeException();
                    }
                }
            }
        } else if (i2 > 0) {
            int i12 = -1;
            do {
                i3 = i12;
                setTimeInMillis(timeInMillis);
                add(i, i3);
                int i13 = (getTimeInMillis() > time2 ? 1 : (getTimeInMillis() == time2 ? 0 : -1));
                if (i13 == 0) {
                    return i3;
                }
                if (i13 < 0) {
                    i3 = i3;
                    int i14 = i3;
                    while (i3 - i14 > 1) {
                        int i15 = ((i14 - i3) / 2) + i3;
                        setTimeInMillis(timeInMillis);
                        add(i, i15);
                        int i16 = (getTimeInMillis() > time2 ? 1 : (getTimeInMillis() == time2 ? 0 : -1));
                        if (i16 == 0) {
                            return i15;
                        }
                        if (i16 < 0) {
                            i14 = i15;
                        } else {
                            i3 = i15;
                        }
                    }
                } else {
                    i12 = i3 << 1;
                }
            } while (i12 != 0);
            throw new RuntimeException();
        }
        setTimeInMillis(timeInMillis);
        add(i, i3);
        return i3;
    }

    public void setTimeZone(TimeZone timeZone) {
        this.zone = timeZone;
        this.areFieldsSet = false;
    }

    public TimeZone getTimeZone() {
        return this.zone;
    }

    public void setLenient(boolean z) {
        this.lenient = z;
    }

    public boolean isLenient() {
        return this.lenient;
    }

    public void setRepeatedWallTimeOption(int i) {
        if (i == 0 || i == 1) {
            this.repeatedWallTime = i;
            return;
        }
        throw new IllegalArgumentException("Illegal repeated wall time option - " + i);
    }

    public int getRepeatedWallTimeOption() {
        return this.repeatedWallTime;
    }

    public void setSkippedWallTimeOption(int i) {
        if (i == 0 || i == 1 || i == 2) {
            this.skippedWallTime = i;
            return;
        }
        throw new IllegalArgumentException("Illegal skipped wall time option - " + i);
    }

    public int getSkippedWallTimeOption() {
        return this.skippedWallTime;
    }

    public void setFirstDayOfWeek(int i) {
        if (this.firstDayOfWeek == i) {
            return;
        }
        if (i < 1 || i > 7) {
            throw new IllegalArgumentException("Invalid day of week");
        }
        this.firstDayOfWeek = i;
        this.areFieldsSet = false;
    }

    public int getFirstDayOfWeek() {
        return this.firstDayOfWeek;
    }

    public void setMinimalDaysInFirstWeek(int i) {
        if (i < 1) {
            i = 1;
        } else if (i > 7) {
            i = 7;
        }
        if (this.minimalDaysInFirstWeek != i) {
            this.minimalDaysInFirstWeek = i;
            this.areFieldsSet = false;
        }
    }

    public int getMinimalDaysInFirstWeek() {
        return this.minimalDaysInFirstWeek;
    }

    /* access modifiers changed from: protected */
    public int getLimit(int i, int i2) {
        switch (i) {
            case 4:
                if (i2 == 0) {
                    if (getMinimalDaysInFirstWeek() == 1) {
                        return 1;
                    }
                    return 0;
                } else if (i2 == 1) {
                    return 1;
                } else {
                    int minimalDaysInFirstWeek2 = getMinimalDaysInFirstWeek();
                    int handleGetLimit = handleGetLimit(5, i2);
                    if (i2 == 2) {
                        return (handleGetLimit + (7 - minimalDaysInFirstWeek2)) / 7;
                    }
                    return ((handleGetLimit + 6) + (7 - minimalDaysInFirstWeek2)) / 7;
                }
            case 5:
            case 6:
            case 8:
            case 17:
            case 19:
            default:
                return handleGetLimit(i, i2);
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
                return LIMITS[i][i2];
        }
    }

    public final int getMinimum(int i) {
        return getLimit(i, 0);
    }

    public final int getMaximum(int i) {
        return getLimit(i, 3);
    }

    public final int getGreatestMinimum(int i) {
        return getLimit(i, 1);
    }

    public final int getLeastMaximum(int i) {
        return getLimit(i, 2);
    }

    @Deprecated
    public int getDayOfWeekType(int i) {
        if (i < 1 || i > 7) {
            throw new IllegalArgumentException("Invalid day of week");
        }
        int i2 = this.weekendOnset;
        int i3 = this.weekendCease;
        if (i2 != i3) {
            if (i2 < i3) {
                if (i < i2 || i > i3) {
                    return 0;
                }
            } else if (i > i3 && i < i2) {
                return 0;
            }
            if (i == this.weekendOnset) {
                return this.weekendOnsetMillis == 0 ? 1 : 2;
            }
            if (i != this.weekendCease || this.weekendCeaseMillis >= 86400000) {
                return 1;
            }
            return 3;
        } else if (i != i2) {
            return 0;
        } else {
            return this.weekendOnsetMillis == 0 ? 1 : 2;
        }
    }

    @Deprecated
    public int getWeekendTransition(int i) {
        if (i == this.weekendOnset) {
            return this.weekendOnsetMillis;
        }
        if (i == this.weekendCease) {
            return this.weekendCeaseMillis;
        }
        throw new IllegalArgumentException("Not weekend transition day");
    }

    public boolean isWeekend(Date date) {
        setTime(date);
        return isWeekend();
    }

    public boolean isWeekend() {
        int i = get(7);
        int dayOfWeekType = getDayOfWeekType(i);
        if (dayOfWeekType == 0) {
            return false;
        }
        if (dayOfWeekType == 1) {
            return true;
        }
        int internalGet = internalGet(14) + ((internalGet(13) + ((internalGet(12) + (internalGet(11) * 60)) * 60)) * 1000);
        int weekendTransition = getWeekendTransition(i);
        if (dayOfWeekType == 2) {
            if (internalGet < weekendTransition) {
                return false;
            }
        } else if (internalGet >= weekendTransition) {
            return false;
        }
        return true;
    }

    @Override // java.lang.Object
    public Object clone() {
        try {
            Calendar calendar = (Calendar) super.clone();
            calendar.fields = new int[this.fields.length];
            calendar.stamp = new int[this.fields.length];
            System.arraycopy(this.fields, 0, calendar.fields, 0, this.fields.length);
            System.arraycopy(this.stamp, 0, calendar.stamp, 0, this.fields.length);
            calendar.zone = (TimeZone) this.zone.clone();
            return calendar;
        } catch (CloneNotSupportedException e) {
            throw new ICUCloneNotSupportedException(e);
        }
    }

    @Override // java.lang.Object
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getName());
        sb.append("[time=");
        sb.append(this.isTimeSet ? String.valueOf(this.time) : HttpConstant.URL_PARAM_SEPARATOR);
        sb.append(",areFieldsSet=");
        sb.append(this.areFieldsSet);
        sb.append(",areAllFieldsSet=");
        sb.append(this.areAllFieldsSet);
        sb.append(",lenient=");
        sb.append(this.lenient);
        sb.append(",zone=");
        sb.append(this.zone);
        sb.append(",firstDayOfWeek=");
        sb.append(this.firstDayOfWeek);
        sb.append(",minimalDaysInFirstWeek=");
        sb.append(this.minimalDaysInFirstWeek);
        sb.append(",repeatedWallTime=");
        sb.append(this.repeatedWallTime);
        sb.append(",skippedWallTime=");
        sb.append(this.skippedWallTime);
        for (int i = 0; i < this.fields.length; i++) {
            sb.append(TelephoneNumberUtils.PAUSE);
            sb.append(fieldName(i));
            sb.append('=');
            sb.append(isSet(i) ? String.valueOf(this.fields[i]) : HttpConstant.URL_PARAM_SEPARATOR);
        }
        sb.append(']');
        return sb.toString();
    }

    public static final class WeekData {
        public final int firstDayOfWeek;
        public final int minimalDaysInFirstWeek;
        public final int weekendCease;
        public final int weekendCeaseMillis;
        public final int weekendOnset;
        public final int weekendOnsetMillis;

        public WeekData(int i, int i2, int i3, int i4, int i5, int i6) {
            this.firstDayOfWeek = i;
            this.minimalDaysInFirstWeek = i2;
            this.weekendOnset = i3;
            this.weekendOnsetMillis = i4;
            this.weekendCease = i5;
            this.weekendCeaseMillis = i6;
        }

        public int hashCode() {
            return (((((((((this.firstDayOfWeek * 37) + this.minimalDaysInFirstWeek) * 37) + this.weekendOnset) * 37) + this.weekendOnsetMillis) * 37) + this.weekendCease) * 37) + this.weekendCeaseMillis;
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof WeekData)) {
                return false;
            }
            WeekData weekData = (WeekData) obj;
            return this.firstDayOfWeek == weekData.firstDayOfWeek && this.minimalDaysInFirstWeek == weekData.minimalDaysInFirstWeek && this.weekendOnset == weekData.weekendOnset && this.weekendOnsetMillis == weekData.weekendOnsetMillis && this.weekendCease == weekData.weekendCease && this.weekendCeaseMillis == weekData.weekendCeaseMillis;
        }

        public String toString() {
            return "{" + this.firstDayOfWeek + ", " + this.minimalDaysInFirstWeek + ", " + this.weekendOnset + ", " + this.weekendOnsetMillis + ", " + this.weekendCease + ", " + this.weekendCeaseMillis + "}";
        }
    }

    public static WeekData getWeekDataForRegion(String str) {
        return WEEK_DATA_CACHE.createInstance(str, str);
    }

    public WeekData getWeekData() {
        return new WeekData(this.firstDayOfWeek, this.minimalDaysInFirstWeek, this.weekendOnset, this.weekendOnsetMillis, this.weekendCease, this.weekendCeaseMillis);
    }

    public Calendar setWeekData(WeekData weekData) {
        setFirstDayOfWeek(weekData.firstDayOfWeek);
        setMinimalDaysInFirstWeek(weekData.minimalDaysInFirstWeek);
        this.weekendOnset = weekData.weekendOnset;
        this.weekendOnsetMillis = weekData.weekendOnsetMillis;
        this.weekendCease = weekData.weekendCease;
        this.weekendCeaseMillis = weekData.weekendCeaseMillis;
        return this;
    }

    /* access modifiers changed from: private */
    public static WeekData getWeekDataForRegionInternal(String str) {
        UResourceBundle uResourceBundle;
        if (str == null) {
            str = "001";
        }
        UResourceBundle uResourceBundle2 = UResourceBundle.getBundleInstance("ohos/global/icu/impl/data/icudt66b", "supplementalData", ICUResourceBundle.ICU_DATA_CLASS_LOADER).get("weekData");
        try {
            uResourceBundle = uResourceBundle2.get(str);
        } catch (MissingResourceException e) {
            if (!str.equals("001")) {
                uResourceBundle = uResourceBundle2.get("001");
            } else {
                throw e;
            }
        }
        int[] intVector = uResourceBundle.getIntVector();
        return new WeekData(intVector[0], intVector[1], intVector[2], intVector[3], intVector[4], intVector[5]);
    }

    /* access modifiers changed from: private */
    public static class WeekDataCache extends SoftCache<String, WeekData, String> {
        private WeekDataCache() {
        }

        /* synthetic */ WeekDataCache(AnonymousClass1 r1) {
            this();
        }

        /* access modifiers changed from: protected */
        public WeekData createInstance(String str, String str2) {
            return Calendar.getWeekDataForRegionInternal(str);
        }
    }

    private void setWeekData(String str) {
        if (str == null) {
            str = "001";
        }
        setWeekData((WeekData) WEEK_DATA_CACHE.getInstance(str, str));
    }

    private void updateTime() {
        computeTime();
        if (isLenient() || !this.areAllFieldsSet) {
            this.areFieldsSet = false;
        }
        this.isTimeSet = true;
        this.areFieldsVirtuallySet = false;
    }

    private void writeObject(ObjectOutputStream objectOutputStream) throws IOException {
        if (!this.isTimeSet) {
            try {
                updateTime();
            } catch (IllegalArgumentException unused) {
            }
        }
        objectOutputStream.defaultWriteObject();
    }

    private void readObject(ObjectInputStream objectInputStream) throws IOException, ClassNotFoundException {
        objectInputStream.defaultReadObject();
        initInternal();
        this.isTimeSet = true;
        this.areAllFieldsSet = false;
        this.areFieldsSet = false;
        this.areFieldsVirtuallySet = true;
        this.nextStamp = 2;
    }

    /* access modifiers changed from: protected */
    public void computeFields() {
        int[] iArr = new int[2];
        getTimeZone().getOffset(this.time, false, iArr);
        long j = this.time + ((long) iArr[0]) + ((long) iArr[1]);
        int i = this.internalSetMask;
        for (int i2 = 0; i2 < this.fields.length; i2++) {
            if ((i & 1) == 0) {
                this.stamp[i2] = 1;
            } else {
                this.stamp[i2] = 0;
            }
            i >>= 1;
        }
        long floorDivide = floorDivide(j, 86400000);
        int[] iArr2 = this.fields;
        iArr2[20] = ((int) floorDivide) + EPOCH_JULIAN_DAY;
        computeGregorianAndDOWFields(iArr2[20]);
        handleComputeFields(this.fields[20]);
        computeWeekFields();
        int i3 = (int) (j - (floorDivide * 86400000));
        int[] iArr3 = this.fields;
        iArr3[21] = i3;
        iArr3[14] = i3 % 1000;
        int i4 = i3 / 1000;
        iArr3[13] = i4 % 60;
        int i5 = i4 / 60;
        iArr3[12] = i5 % 60;
        int i6 = i5 / 60;
        iArr3[11] = i6;
        iArr3[9] = i6 / 12;
        iArr3[10] = i6 % 12;
        iArr3[15] = iArr[0];
        iArr3[16] = iArr[1];
    }

    private final void computeGregorianAndDOWFields(int i) {
        computeGregorianFields(i);
        int[] iArr = this.fields;
        int julianDayToDayOfWeek = julianDayToDayOfWeek(i);
        iArr[7] = julianDayToDayOfWeek;
        int firstDayOfWeek2 = (julianDayToDayOfWeek - getFirstDayOfWeek()) + 1;
        if (firstDayOfWeek2 < 1) {
            firstDayOfWeek2 += 7;
        }
        this.fields[18] = firstDayOfWeek2;
    }

    /* access modifiers changed from: protected */
    public final void computeGregorianFields(int i) {
        int[] iArr = new int[1];
        int floorDivide = floorDivide((long) (i - JAN_1_1_JULIAN_DAY), 146097, iArr);
        int i2 = 0;
        int floorDivide2 = floorDivide(iArr[0], 36524, iArr);
        int floorDivide3 = floorDivide(iArr[0], 1461, iArr);
        int floorDivide4 = floorDivide(iArr[0], 365, iArr);
        int i3 = (floorDivide * 400) + (floorDivide2 * 100) + (floorDivide3 * 4) + floorDivide4;
        int i4 = iArr[0];
        if (floorDivide2 == 4 || floorDivide4 == 4) {
            i4 = 365;
        } else {
            i3++;
        }
        boolean z = (i3 & 3) == 0 && (i3 % 100 != 0 || i3 % 400 == 0);
        char c = 2;
        if (i4 >= (z ? 60 : 59)) {
            i2 = z ? 1 : 2;
        }
        int i5 = (((i2 + i4) * 12) + 6) / 367;
        int[] iArr2 = GREGORIAN_MONTH_COUNT[i5];
        if (z) {
            c = 3;
        }
        this.gregorianYear = i3;
        this.gregorianMonth = i5;
        this.gregorianDayOfMonth = (i4 - iArr2[c]) + 1;
        this.gregorianDayOfYear = i4 + 1;
    }

    private final void computeWeekFields() {
        int[] iArr = this.fields;
        int i = iArr[19];
        int i2 = iArr[7];
        int i3 = iArr[6];
        int firstDayOfWeek2 = ((i2 + 7) - getFirstDayOfWeek()) % 7;
        int firstDayOfWeek3 = (((i2 - i3) + 7001) - getFirstDayOfWeek()) % 7;
        int i4 = ((i3 - 1) + firstDayOfWeek3) / 7;
        if (7 - firstDayOfWeek3 >= getMinimalDaysInFirstWeek()) {
            i4++;
        }
        if (i4 == 0) {
            i4 = weekNumber(i3 + handleGetYearLength(i - 1), i2);
            i--;
        } else {
            int handleGetYearLength = handleGetYearLength(i);
            if (i3 >= handleGetYearLength - 5) {
                int i5 = ((firstDayOfWeek2 + handleGetYearLength) - i3) % 7;
                if (i5 < 0) {
                    i5 += 7;
                }
                if (6 - i5 >= getMinimalDaysInFirstWeek() && (i3 + 7) - firstDayOfWeek2 > handleGetYearLength) {
                    i++;
                    i4 = 1;
                }
            }
        }
        int[] iArr2 = this.fields;
        iArr2[3] = i4;
        iArr2[17] = i;
        int i6 = iArr2[5];
        iArr2[4] = weekNumber(i6, i2);
        this.fields[8] = ((i6 - 1) / 7) + 1;
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0042, code lost:
        if (r8[4] < r8[r7]) goto L_0x0044;
     */
    public int resolveFields(int[][][] iArr) {
        int i = -1;
        int i2 = 0;
        while (i2 < iArr.length && i < 0) {
            int[][] iArr2 = iArr[i2];
            int i3 = 0;
            int i4 = i;
            for (int[] iArr3 : iArr2) {
                int i5 = iArr3[0] >= 32 ? 1 : 0;
                int i6 = 0;
                while (true) {
                    if (i5 < iArr3.length) {
                        int i7 = this.stamp[iArr3[i5]];
                        if (i7 == 0) {
                            break;
                        }
                        i6 = Math.max(i6, i7);
                        i5++;
                    } else if (i6 > i3) {
                        int i8 = iArr3[0];
                        if (i8 >= 32 && (i8 = i8 & 31) == 5) {
                            int[] iArr4 = this.stamp;
                        }
                        i4 = i8;
                        if (i4 == i8) {
                            i3 = i6;
                        }
                    }
                }
            }
            i2++;
            i = i4;
        }
        return i >= 32 ? i & 31 : i;
    }

    /* access modifiers changed from: protected */
    public int newestStamp(int i, int i2, int i3) {
        while (i <= i2) {
            int[] iArr = this.stamp;
            if (iArr[i] > i3) {
                i3 = iArr[i];
            }
            i++;
        }
        return i3;
    }

    /* access modifiers changed from: protected */
    public final int getStamp(int i) {
        return this.stamp[i];
    }

    /* access modifiers changed from: protected */
    public int newerField(int i, int i2) {
        int[] iArr = this.stamp;
        return iArr[i2] > iArr[i] ? i2 : i;
    }

    /* access modifiers changed from: protected */
    public void validateFields() {
        for (int i = 0; i < this.fields.length; i++) {
            if (this.stamp[i] >= 2) {
                validateField(i);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void validateField(int i) {
        if (i == 5) {
            validateField(i, 1, handleGetMonthLength(handleGetExtendedYear(), internalGet(2)));
        } else if (i == 6) {
            validateField(i, 1, handleGetYearLength(handleGetExtendedYear()));
        } else if (i != 8) {
            validateField(i, getMinimum(i), getMaximum(i));
        } else if (internalGet(i) != 0) {
            validateField(i, getMinimum(i), getMaximum(i));
        } else {
            throw new IllegalArgumentException("DAY_OF_WEEK_IN_MONTH cannot be zero");
        }
    }

    /* access modifiers changed from: protected */
    public final void validateField(int i, int i2, int i3) {
        int i4 = this.fields[i];
        if (i4 < i2 || i4 > i3) {
            throw new IllegalArgumentException(fieldName(i) + '=' + i4 + ", valid range=" + i2 + ".." + i3);
        }
    }

    /* access modifiers changed from: protected */
    public void computeTime() {
        long j;
        int[] iArr;
        int i;
        if (!isLenient()) {
            validateFields();
        }
        long julianDayToMillis = julianDayToMillis(computeJulianDay());
        if (this.stamp[21] >= 2 && newestStamp(9, 14, 0) <= this.stamp[21]) {
            i = internalGet(21);
        } else if (Math.max(Math.abs(internalGet(11)), Math.abs(internalGet(10))) > MAX_HOURS) {
            j = computeMillisInDayLong();
            iArr = this.stamp;
            if (iArr[15] < 2 || iArr[16] >= 2) {
                this.time = (julianDayToMillis + j) - ((long) (internalGet(15) + internalGet(16)));
            } else if (!this.lenient || this.skippedWallTime == 2) {
                int computeZoneOffset = computeZoneOffset(julianDayToMillis, j);
                long j2 = (julianDayToMillis + j) - ((long) computeZoneOffset);
                if (computeZoneOffset == this.zone.getOffset(j2)) {
                    this.time = j2;
                    return;
                } else if (this.lenient) {
                    Long immediatePreviousZoneTransition = getImmediatePreviousZoneTransition(j2);
                    if (immediatePreviousZoneTransition != null) {
                        this.time = immediatePreviousZoneTransition.longValue();
                        return;
                    }
                    throw new RuntimeException("Could not locate a time zone transition before " + j2);
                } else {
                    throw new IllegalArgumentException("The specified wall time does not exist due to time zone offset transition.");
                }
            } else {
                this.time = (julianDayToMillis + j) - ((long) computeZoneOffset(julianDayToMillis, j));
                return;
            }
        } else {
            i = computeMillisInDay();
        }
        j = (long) i;
        iArr = this.stamp;
        if (iArr[15] < 2) {
        }
        this.time = (julianDayToMillis + j) - ((long) (internalGet(15) + internalGet(16)));
    }

    private Long getImmediatePreviousZoneTransition(long j) {
        TimeZone timeZone = this.zone;
        if (timeZone instanceof BasicTimeZone) {
            TimeZoneTransition previousTransition = ((BasicTimeZone) timeZone).getPreviousTransition(j, true);
            if (previousTransition != null) {
                return Long.valueOf(previousTransition.getTime());
            }
            return null;
        }
        Long previousZoneTransitionTime = getPreviousZoneTransitionTime(timeZone, j, WorkStatus.WORKING_DELAY_TIME);
        return previousZoneTransitionTime == null ? getPreviousZoneTransitionTime(this.zone, j, 108000000) : previousZoneTransitionTime;
    }

    private static Long getPreviousZoneTransitionTime(TimeZone timeZone, long j, long j2) {
        long j3 = (j - j2) - 1;
        int offset = timeZone.getOffset(j);
        if (offset == timeZone.getOffset(j3)) {
            return null;
        }
        return findPreviousZoneTransitionTime(timeZone, offset, j, j3);
    }

    private static Long findPreviousZoneTransitionTime(TimeZone timeZone, int i, long j, long j2) {
        long j3;
        long j4;
        long j5;
        int[] iArr = FIND_ZONE_TRANSITION_TIME_UNITS;
        int length = iArr.length;
        boolean z = false;
        int i2 = 0;
        while (true) {
            if (i2 >= length) {
                j3 = 0;
                break;
            }
            long j6 = (long) iArr[i2];
            long j7 = j2 / j6;
            long j8 = j / j6;
            if (j8 > j7) {
                j3 = (((j7 + j8) + 1) >>> 1) * j6;
                z = true;
                break;
            }
            i2++;
        }
        if (!z) {
            j3 = (j + j2) >>> 1;
        }
        if (z) {
            if (j3 == j) {
                j5 = j;
            } else if (timeZone.getOffset(j3) != i) {
                return findPreviousZoneTransitionTime(timeZone, i, j, j3);
            } else {
                j5 = j3;
            }
            j4 = j3 - 1;
        } else {
            j4 = (j + j2) >>> 1;
            j5 = j;
        }
        if (j4 == j2) {
            return Long.valueOf(j5);
        }
        if (timeZone.getOffset(j4) == i) {
            return findPreviousZoneTransitionTime(timeZone, i, j4, j2);
        }
        if (z) {
            return Long.valueOf(j5);
        }
        return findPreviousZoneTransitionTime(timeZone, i, j5, j4);
    }

    /* access modifiers changed from: protected */
    @Deprecated
    public int computeMillisInDay() {
        int[] iArr = this.stamp;
        int i = iArr[11];
        int max = Math.max(iArr[10], iArr[9]);
        if (max <= i) {
            max = i;
        }
        int i2 = 0;
        if (max != 0) {
            if (max == i) {
                i2 = 0 + internalGet(11);
            } else {
                i2 = internalGet(10) + 0 + (internalGet(9) * 12);
            }
        }
        return (((((i2 * 60) + internalGet(12)) * 60) + internalGet(13)) * 1000) + internalGet(14);
    }

    /* access modifiers changed from: protected */
    @Deprecated
    public long computeMillisInDayLong() {
        int[] iArr = this.stamp;
        int i = iArr[11];
        int max = Math.max(iArr[10], iArr[9]);
        if (max <= i) {
            max = i;
        }
        long j = 0;
        if (max != 0) {
            if (max == i) {
                j = 0 + ((long) internalGet(11));
            } else {
                j = ((long) internalGet(10)) + 0 + ((long) (internalGet(9) * 12));
            }
        }
        return (((((j * 60) + ((long) internalGet(12))) * 60) + ((long) internalGet(13))) * 1000) + ((long) internalGet(14));
    }

    /* access modifiers changed from: protected */
    @Deprecated
    public int computeZoneOffset(long j, int i) {
        boolean z;
        int offset;
        int[] iArr = new int[2];
        long j2 = j + ((long) i);
        TimeZone timeZone = this.zone;
        if (timeZone instanceof BasicTimeZone) {
            ((BasicTimeZone) this.zone).getOffsetFromLocal(j2, this.skippedWallTime == 1 ? 12 : 4, this.repeatedWallTime == 1 ? 4 : 12, iArr);
        } else {
            timeZone.getOffset(j2, true, iArr);
            if (this.repeatedWallTime != 1 || (offset = (iArr[0] + iArr[1]) - this.zone.getOffset((j2 - ((long) (iArr[0] + iArr[1]))) - 21600000)) >= 0) {
                z = false;
            } else {
                this.zone.getOffset(((long) offset) + j2, true, iArr);
                z = true;
            }
            if (!z && this.skippedWallTime == 1) {
                this.zone.getOffset(j2 - ((long) (iArr[0] + iArr[1])), false, iArr);
            }
        }
        return iArr[0] + iArr[1];
    }

    /* access modifiers changed from: protected */
    @Deprecated
    public int computeZoneOffset(long j, long j2) {
        boolean z;
        int offset;
        int[] iArr = new int[2];
        long j3 = j + j2;
        TimeZone timeZone = this.zone;
        if (timeZone instanceof BasicTimeZone) {
            ((BasicTimeZone) this.zone).getOffsetFromLocal(j3, this.skippedWallTime == 1 ? 12 : 4, this.repeatedWallTime == 1 ? 4 : 12, iArr);
        } else {
            timeZone.getOffset(j3, true, iArr);
            if (this.repeatedWallTime != 1 || (offset = (iArr[0] + iArr[1]) - this.zone.getOffset((j3 - ((long) (iArr[0] + iArr[1]))) - 21600000)) >= 0) {
                z = false;
            } else {
                this.zone.getOffset(((long) offset) + j3, true, iArr);
                z = true;
            }
            if (!z && this.skippedWallTime == 1) {
                this.zone.getOffset(j3 - ((long) (iArr[0] + iArr[1])), false, iArr);
            }
        }
        return iArr[0] + iArr[1];
    }

    /* access modifiers changed from: protected */
    public int computeJulianDay() {
        if (this.stamp[20] >= 2 && newestStamp(17, 19, newestStamp(0, 8, 0)) <= this.stamp[20]) {
            return internalGet(20);
        }
        int resolveFields = resolveFields(getFieldResolutionTable());
        if (resolveFields < 0) {
            resolveFields = 5;
        }
        return handleComputeJulianDay(resolveFields);
    }

    /* access modifiers changed from: protected */
    public int[][][] getFieldResolutionTable() {
        return DATE_PRECEDENCE;
    }

    /* access modifiers changed from: protected */
    public int handleGetMonthLength(int i, int i2) {
        return handleComputeMonthStart(i, i2 + 1, true) - handleComputeMonthStart(i, i2, true);
    }

    /* access modifiers changed from: protected */
    public int handleGetYearLength(int i) {
        return handleComputeMonthStart(i + 1, 0, false) - handleComputeMonthStart(i, 0, false);
    }

    /* access modifiers changed from: protected */
    public int handleComputeJulianDay(int i) {
        int i2;
        int i3;
        int i4;
        int i5;
        int internalGet;
        boolean z = i == 5 || i == 4 || i == 8;
        if (i == 3 && newerField(17, 1) == 17) {
            i2 = internalGet(17);
        } else {
            i2 = handleGetExtendedYear();
        }
        internalSet(19, i2);
        int internalGet2 = z ? internalGet(2, getDefaultMonthInYear(i2)) : 0;
        int handleComputeMonthStart = handleComputeMonthStart(i2, internalGet2, z);
        if (i == 5) {
            if (isSet(5)) {
                internalGet = internalGet(5, getDefaultDayInMonth(i2, internalGet2));
            } else {
                internalGet = getDefaultDayInMonth(i2, internalGet2);
            }
        } else if (i == 6) {
            internalGet = internalGet(6);
        } else {
            int firstDayOfWeek2 = getFirstDayOfWeek();
            int julianDayToDayOfWeek = julianDayToDayOfWeek(handleComputeMonthStart + 1) - firstDayOfWeek2;
            if (julianDayToDayOfWeek < 0) {
                julianDayToDayOfWeek += 7;
            }
            int resolveFields = resolveFields(DOW_PRECEDENCE);
            if (resolveFields == 7) {
                i3 = internalGet(7) - firstDayOfWeek2;
            } else if (resolveFields != 18) {
                i3 = 0;
            } else {
                i3 = internalGet(18) - 1;
            }
            int i6 = i3 % 7;
            if (i6 < 0) {
                i6 += 7;
            }
            int i7 = (1 - julianDayToDayOfWeek) + i6;
            if (i == 8) {
                if (i7 < 1) {
                    i7 += 7;
                }
                int internalGet3 = internalGet(8, 1);
                if (internalGet3 >= 0) {
                    i4 = i7 + ((internalGet3 - 1) * 7);
                    return handleComputeMonthStart + i4;
                }
                i5 = ((handleGetMonthLength(i2, internalGet(2, 0)) - i7) / 7) + internalGet3 + 1;
            } else {
                if (7 - julianDayToDayOfWeek < getMinimalDaysInFirstWeek()) {
                    i7 += 7;
                }
                i5 = internalGet(i) - 1;
            }
            i4 = i7 + (i5 * 7);
            return handleComputeMonthStart + i4;
        }
        return handleComputeMonthStart + internalGet;
    }

    /* access modifiers changed from: protected */
    public int computeGregorianMonthStart(int i, int i2) {
        boolean z = false;
        if (i2 < 0 || i2 > 11) {
            int[] iArr = new int[1];
            i += floorDivide(i2, 12, iArr);
            i2 = iArr[0];
        }
        if (i % 4 == 0 && (i % 100 != 0 || i % 400 == 0)) {
            z = true;
        }
        int i3 = i - 1;
        int floorDivide = (((((i3 * 365) + floorDivide(i3, 4)) - floorDivide(i3, 100)) + floorDivide(i3, 400)) + JAN_1_1_JULIAN_DAY) - 1;
        if (i2 == 0) {
            return floorDivide;
        }
        return floorDivide + GREGORIAN_MONTH_COUNT[i2][z ? (char) 3 : 2];
    }

    /* access modifiers changed from: protected */
    public void handleComputeFields(int i) {
        int i2;
        int i3;
        internalSet(2, getGregorianMonth());
        internalSet(5, getGregorianDayOfMonth());
        internalSet(6, getGregorianDayOfYear());
        int gregorianYear2 = getGregorianYear();
        internalSet(19, gregorianYear2);
        if (gregorianYear2 < 1) {
            i3 = 1 - gregorianYear2;
            i2 = 0;
        } else {
            i3 = gregorianYear2;
            i2 = 1;
        }
        internalSet(0, i2);
        internalSet(1, i3);
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
    public final void internalSet(int i, int i2) {
        if (((1 << i) & this.internalSetMask) != 0) {
            this.fields[i] = i2;
            this.stamp[i] = 1;
            return;
        }
        throw new IllegalStateException("Subclass cannot set " + fieldName(i));
    }

    protected static final boolean isGregorianLeapYear(int i) {
        return i % 4 == 0 && (i % 100 != 0 || i % 400 == 0);
    }

    protected static final int gregorianMonthLength(int i, int i2) {
        return GREGORIAN_MONTH_COUNT[i2][isGregorianLeapYear(i) ? 1 : 0];
    }

    protected static final int gregorianPreviousMonthLength(int i, int i2) {
        if (i2 > 0) {
            return gregorianMonthLength(i, i2 - 1);
        }
        return 31;
    }

    protected static final long floorDivide(long j, long j2) {
        if (j >= 0) {
            return j / j2;
        }
        return ((j + 1) / j2) - 1;
    }

    protected static final int floorDivide(int i, int i2) {
        if (i >= 0) {
            return i / i2;
        }
        return ((i + 1) / i2) - 1;
    }

    protected static final int floorDivide(int i, int i2, int[] iArr) {
        if (i >= 0) {
            iArr[0] = i % i2;
            return i / i2;
        }
        int i3 = ((i + 1) / i2) - 1;
        iArr[0] = i - (i2 * i3);
        return i3;
    }

    protected static final int floorDivide(long j, int i, int[] iArr) {
        if (j >= 0) {
            long j2 = (long) i;
            iArr[0] = (int) (j % j2);
            return (int) (j / j2);
        }
        long j3 = (long) i;
        int i2 = (int) (((j + 1) / j3) - 1);
        iArr[0] = (int) (j - (((long) i2) * j3));
        return i2;
    }

    /* access modifiers changed from: protected */
    public String fieldName(int i) {
        try {
            return FIELD_NAME[i];
        } catch (ArrayIndexOutOfBoundsException unused) {
            return "Field " + i;
        }
    }

    protected static final int millisToJulianDay(long j) {
        return (int) (floorDivide(j, 86400000) + 2440588);
    }

    protected static final int julianDayToDayOfWeek(int i) {
        int i2 = (i + 2) % 7;
        return i2 < 1 ? i2 + 7 : i2;
    }

    /* access modifiers changed from: protected */
    public final long internalGetTimeInMillis() {
        return this.time;
    }

    public final ULocale getLocale(ULocale.Type type) {
        return type == ULocale.ACTUAL_LOCALE ? this.actualLocale : this.validLocale;
    }

    /* access modifiers changed from: package-private */
    public final void setLocale(ULocale uLocale, ULocale uLocale2) {
        boolean z = true;
        boolean z2 = uLocale == null;
        if (uLocale2 != null) {
            z = false;
        }
        if (z2 == z) {
            this.validLocale = uLocale;
            this.actualLocale = uLocale2;
            return;
        }
        throw new IllegalArgumentException();
    }
}
