package java.time.chrono;

import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.time.Clock;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalField;
import java.time.temporal.ValueRange;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Properties;
import sun.util.calendar.CalendarSystem;
import sun.util.logging.PlatformLogger;

public final class HijrahChronology extends AbstractChronology implements Serializable {
    private static final /* synthetic */ int[] -java-time-temporal-ChronoFieldSwitchesValues = null;
    public static final HijrahChronology INSTANCE;
    private static final String KEY_ID = "id";
    private static final String KEY_ISO_START = "iso-start";
    private static final String KEY_TYPE = "type";
    private static final String KEY_VERSION = "version";
    private static final String PROP_PREFIX = "calendar.hijrah.";
    private static final String PROP_TYPE_SUFFIX = ".type";
    private static final transient Properties calendarProperties;
    private static final long serialVersionUID = 3127340209035924785L;
    private final transient String calendarType;
    private transient int[] hijrahEpochMonthStartDays;
    private transient int hijrahStartEpochMonth;
    private volatile transient boolean initComplete;
    private transient int maxEpochDay;
    private transient int maxMonthLength;
    private transient int maxYearLength;
    private transient int minEpochDay;
    private transient int minMonthLength;
    private transient int minYearLength;
    private final transient String typeId;

    private static /* synthetic */ int[] -getjava-time-temporal-ChronoFieldSwitchesValues() {
        if (-java-time-temporal-ChronoFieldSwitchesValues != null) {
            return -java-time-temporal-ChronoFieldSwitchesValues;
        }
        int[] iArr = new int[ChronoField.values().length];
        try {
            iArr[ChronoField.ALIGNED_DAY_OF_WEEK_IN_MONTH.ordinal()] = 7;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[ChronoField.ALIGNED_DAY_OF_WEEK_IN_YEAR.ordinal()] = 8;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[ChronoField.ALIGNED_WEEK_OF_MONTH.ordinal()] = 1;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[ChronoField.ALIGNED_WEEK_OF_YEAR.ordinal()] = 9;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[ChronoField.AMPM_OF_DAY.ordinal()] = 10;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[ChronoField.CLOCK_HOUR_OF_AMPM.ordinal()] = 11;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[ChronoField.CLOCK_HOUR_OF_DAY.ordinal()] = 12;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[ChronoField.DAY_OF_MONTH.ordinal()] = 2;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[ChronoField.DAY_OF_WEEK.ordinal()] = 13;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[ChronoField.DAY_OF_YEAR.ordinal()] = 3;
        } catch (NoSuchFieldError e10) {
        }
        try {
            iArr[ChronoField.EPOCH_DAY.ordinal()] = 14;
        } catch (NoSuchFieldError e11) {
        }
        try {
            iArr[ChronoField.ERA.ordinal()] = 4;
        } catch (NoSuchFieldError e12) {
        }
        try {
            iArr[ChronoField.HOUR_OF_AMPM.ordinal()] = 15;
        } catch (NoSuchFieldError e13) {
        }
        try {
            iArr[ChronoField.HOUR_OF_DAY.ordinal()] = 16;
        } catch (NoSuchFieldError e14) {
        }
        try {
            iArr[ChronoField.INSTANT_SECONDS.ordinal()] = 17;
        } catch (NoSuchFieldError e15) {
        }
        try {
            iArr[ChronoField.MICRO_OF_DAY.ordinal()] = 18;
        } catch (NoSuchFieldError e16) {
        }
        try {
            iArr[ChronoField.MICRO_OF_SECOND.ordinal()] = 19;
        } catch (NoSuchFieldError e17) {
        }
        try {
            iArr[ChronoField.MILLI_OF_DAY.ordinal()] = 20;
        } catch (NoSuchFieldError e18) {
        }
        try {
            iArr[ChronoField.MILLI_OF_SECOND.ordinal()] = 21;
        } catch (NoSuchFieldError e19) {
        }
        try {
            iArr[ChronoField.MINUTE_OF_DAY.ordinal()] = 22;
        } catch (NoSuchFieldError e20) {
        }
        try {
            iArr[ChronoField.MINUTE_OF_HOUR.ordinal()] = 23;
        } catch (NoSuchFieldError e21) {
        }
        try {
            iArr[ChronoField.MONTH_OF_YEAR.ordinal()] = 24;
        } catch (NoSuchFieldError e22) {
        }
        try {
            iArr[ChronoField.NANO_OF_DAY.ordinal()] = 25;
        } catch (NoSuchFieldError e23) {
        }
        try {
            iArr[ChronoField.NANO_OF_SECOND.ordinal()] = 26;
        } catch (NoSuchFieldError e24) {
        }
        try {
            iArr[ChronoField.OFFSET_SECONDS.ordinal()] = 27;
        } catch (NoSuchFieldError e25) {
        }
        try {
            iArr[ChronoField.PROLEPTIC_MONTH.ordinal()] = 28;
        } catch (NoSuchFieldError e26) {
        }
        try {
            iArr[ChronoField.SECOND_OF_DAY.ordinal()] = 29;
        } catch (NoSuchFieldError e27) {
        }
        try {
            iArr[ChronoField.SECOND_OF_MINUTE.ordinal()] = 30;
        } catch (NoSuchFieldError e28) {
        }
        try {
            iArr[ChronoField.YEAR.ordinal()] = 5;
        } catch (NoSuchFieldError e29) {
        }
        try {
            iArr[ChronoField.YEAR_OF_ERA.ordinal()] = 6;
        } catch (NoSuchFieldError e30) {
        }
        -java-time-temporal-ChronoFieldSwitchesValues = iArr;
        return iArr;
    }

    static {
        try {
            calendarProperties = CalendarSystem.getCalendarProperties();
            try {
                INSTANCE = new HijrahChronology("Hijrah-umalqura");
                AbstractChronology.registerChrono(INSTANCE, "Hijrah");
                AbstractChronology.registerChrono(INSTANCE, "islamic");
                registerVariants();
            } catch (Throwable ex) {
                PlatformLogger.getLogger("java.time.chrono").severe("Unable to initialize Hijrah calendar: Hijrah-umalqura", ex);
                throw new RuntimeException("Unable to initialize Hijrah-umalqura calendar", ex.getCause());
            }
        } catch (IOException ioe) {
            throw new InternalError("Can't initialize lib/calendars.properties", ioe);
        }
    }

    private static void registerVariants() {
        for (String name : calendarProperties.stringPropertyNames()) {
            if (name.startsWith(PROP_PREFIX)) {
                String id = name.substring(PROP_PREFIX.length());
                if (id.indexOf(46) < 0 && !id.equals(INSTANCE.getId())) {
                    try {
                        AbstractChronology.registerChrono(new HijrahChronology(id));
                    } catch (Throwable ex) {
                        PlatformLogger.getLogger("java.time.chrono").severe("Unable to initialize Hijrah calendar: " + id, ex);
                    }
                }
            }
        }
    }

    private HijrahChronology(String id) throws DateTimeException {
        if (id.isEmpty()) {
            throw new IllegalArgumentException("calendar id is empty");
        }
        String propName = PROP_PREFIX + id + PROP_TYPE_SUFFIX;
        String calType = calendarProperties.getProperty(propName);
        if (calType == null || calType.isEmpty()) {
            throw new DateTimeException("calendarType is missing or empty for: " + propName);
        }
        this.typeId = id;
        this.calendarType = calType;
    }

    private void checkCalendarInit() {
        if (!this.initComplete) {
            loadCalendarData();
            this.initComplete = true;
        }
    }

    public String getId() {
        return this.typeId;
    }

    public String getCalendarType() {
        return this.calendarType;
    }

    public HijrahDate date(Era era, int yearOfEra, int month, int dayOfMonth) {
        return date(prolepticYear(era, yearOfEra), month, dayOfMonth);
    }

    public HijrahDate date(int prolepticYear, int month, int dayOfMonth) {
        return HijrahDate.of(this, prolepticYear, month, dayOfMonth);
    }

    public HijrahDate dateYearDay(Era era, int yearOfEra, int dayOfYear) {
        return dateYearDay(prolepticYear(era, yearOfEra), dayOfYear);
    }

    public HijrahDate dateYearDay(int prolepticYear, int dayOfYear) {
        HijrahDate date = HijrahDate.of(this, prolepticYear, 1, 1);
        if (dayOfYear <= date.lengthOfYear()) {
            return date.plusDays((long) (dayOfYear - 1));
        }
        throw new DateTimeException("Invalid dayOfYear: " + dayOfYear);
    }

    public HijrahDate dateEpochDay(long epochDay) {
        return HijrahDate.ofEpochDay(this, epochDay);
    }

    public HijrahDate dateNow() {
        return dateNow(Clock.systemDefaultZone());
    }

    public HijrahDate dateNow(ZoneId zone) {
        return dateNow(Clock.system(zone));
    }

    public HijrahDate dateNow(Clock clock) {
        return date(LocalDate.now(clock));
    }

    public HijrahDate date(TemporalAccessor temporal) {
        if (temporal instanceof HijrahDate) {
            return (HijrahDate) temporal;
        }
        return HijrahDate.ofEpochDay(this, temporal.getLong(ChronoField.EPOCH_DAY));
    }

    public ChronoLocalDateTime<HijrahDate> localDateTime(TemporalAccessor temporal) {
        return super.localDateTime(temporal);
    }

    public ChronoZonedDateTime<HijrahDate> zonedDateTime(TemporalAccessor temporal) {
        return super.zonedDateTime(temporal);
    }

    public ChronoZonedDateTime<HijrahDate> zonedDateTime(Instant instant, ZoneId zone) {
        return super.zonedDateTime(instant, zone);
    }

    public boolean isLeapYear(long prolepticYear) {
        boolean z = false;
        checkCalendarInit();
        if (prolepticYear < ((long) getMinimumYear()) || prolepticYear > ((long) getMaximumYear())) {
            return false;
        }
        if (getYearLength((int) prolepticYear) > 354) {
            z = true;
        }
        return z;
    }

    public int prolepticYear(Era era, int yearOfEra) {
        if (era instanceof HijrahEra) {
            return yearOfEra;
        }
        throw new ClassCastException("Era must be HijrahEra");
    }

    public HijrahEra eraOf(int eraValue) {
        switch (eraValue) {
            case 1:
                return HijrahEra.AH;
            default:
                throw new DateTimeException("invalid Hijrah era");
        }
    }

    public List<Era> eras() {
        return Arrays.asList(HijrahEra.values());
    }

    public ValueRange range(ChronoField field) {
        checkCalendarInit();
        if (!(field instanceof ChronoField)) {
            return field.range();
        }
        ChronoField f = field;
        switch (-getjava-time-temporal-ChronoFieldSwitchesValues()[field.ordinal()]) {
            case 1:
                return ValueRange.of(1, 5);
            case 2:
                return ValueRange.of(1, 1, (long) getMinimumMonthLength(), (long) getMaximumMonthLength());
            case 3:
                return ValueRange.of(1, (long) getMaximumDayOfYear());
            case 4:
                return ValueRange.of(1, 1);
            case 5:
            case 6:
                return ValueRange.of((long) getMinimumYear(), (long) getMaximumYear());
            default:
                return field.range();
        }
    }

    public HijrahDate resolveDate(Map<TemporalField, Long> fieldValues, ResolverStyle resolverStyle) {
        return (HijrahDate) super.resolveDate(fieldValues, resolverStyle);
    }

    int checkValidYear(long prolepticYear) {
        if (prolepticYear >= ((long) getMinimumYear()) && prolepticYear <= ((long) getMaximumYear())) {
            return (int) prolepticYear;
        }
        throw new DateTimeException("Invalid Hijrah year: " + prolepticYear);
    }

    void checkValidDayOfYear(int dayOfYear) {
        if (dayOfYear < 1 || dayOfYear > getMaximumDayOfYear()) {
            throw new DateTimeException("Invalid Hijrah day of year: " + dayOfYear);
        }
    }

    void checkValidMonth(int month) {
        if (month < 1 || month > 12) {
            throw new DateTimeException("Invalid Hijrah month: " + month);
        }
    }

    int[] getHijrahDateInfo(int epochDay) {
        checkCalendarInit();
        if (epochDay < this.minEpochDay || epochDay >= this.maxEpochDay) {
            throw new DateTimeException("Hijrah date out of range");
        }
        int epochMonth = epochDayToEpochMonth(epochDay);
        int year = epochMonthToYear(epochMonth);
        int month = epochMonthToMonth(epochMonth);
        int date = epochDay - epochMonthToEpochDay(epochMonth);
        return new int[]{year, month + 1, date + 1};
    }

    long getEpochDay(int prolepticYear, int monthOfYear, int dayOfMonth) {
        checkCalendarInit();
        checkValidMonth(monthOfYear);
        int epochMonth = yearToEpochMonth(prolepticYear) + (monthOfYear - 1);
        if (epochMonth < 0 || epochMonth >= this.hijrahEpochMonthStartDays.length) {
            throw new DateTimeException("Invalid Hijrah date, year: " + prolepticYear + ", month: " + monthOfYear);
        } else if (dayOfMonth >= 1 && dayOfMonth <= getMonthLength(prolepticYear, monthOfYear)) {
            return (long) (epochMonthToEpochDay(epochMonth) + (dayOfMonth - 1));
        } else {
            throw new DateTimeException("Invalid Hijrah day of month: " + dayOfMonth);
        }
    }

    int getDayOfYear(int prolepticYear, int month) {
        return yearMonthToDayOfYear(prolepticYear, month - 1);
    }

    int getMonthLength(int prolepticYear, int monthOfYear) {
        int epochMonth = yearToEpochMonth(prolepticYear) + (monthOfYear - 1);
        if (epochMonth >= 0 && epochMonth < this.hijrahEpochMonthStartDays.length) {
            return epochMonthLength(epochMonth);
        }
        throw new DateTimeException("Invalid Hijrah date, year: " + prolepticYear + ", month: " + monthOfYear);
    }

    int getYearLength(int prolepticYear) {
        return yearMonthToDayOfYear(prolepticYear, 12);
    }

    int getMinimumYear() {
        return epochMonthToYear(0);
    }

    int getMaximumYear() {
        return epochMonthToYear(this.hijrahEpochMonthStartDays.length - 1) - 1;
    }

    int getMaximumMonthLength() {
        return this.maxMonthLength;
    }

    int getMinimumMonthLength() {
        return this.minMonthLength;
    }

    int getMaximumDayOfYear() {
        return this.maxYearLength;
    }

    int getSmallestMaximumDayOfYear() {
        return this.minYearLength;
    }

    private int epochDayToEpochMonth(int epochDay) {
        int ndx = Arrays.binarySearch(this.hijrahEpochMonthStartDays, epochDay);
        if (ndx < 0) {
            return (-ndx) - 2;
        }
        return ndx;
    }

    private int epochMonthToYear(int epochMonth) {
        return (this.hijrahStartEpochMonth + epochMonth) / 12;
    }

    private int yearToEpochMonth(int year) {
        return (year * 12) - this.hijrahStartEpochMonth;
    }

    private int epochMonthToMonth(int epochMonth) {
        return (this.hijrahStartEpochMonth + epochMonth) % 12;
    }

    private int epochMonthToEpochDay(int epochMonth) {
        return this.hijrahEpochMonthStartDays[epochMonth];
    }

    private int yearMonthToDayOfYear(int prolepticYear, int month) {
        int epochMonthFirst = yearToEpochMonth(prolepticYear);
        return epochMonthToEpochDay(epochMonthFirst + month) - epochMonthToEpochDay(epochMonthFirst);
    }

    private int epochMonthLength(int epochMonth) {
        return this.hijrahEpochMonthStartDays[epochMonth + 1] - this.hijrahEpochMonthStartDays[epochMonth];
    }

    private static Properties readConfigProperties(String resource) throws Exception {
        Throwable th;
        Throwable th2 = null;
        Properties props = new Properties();
        InputStream inputStream = null;
        try {
            inputStream = ClassLoader.getSystemResourceAsStream(resource);
            props.load(inputStream);
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Throwable th3) {
                    th2 = th3;
                }
            }
            if (th2 == null) {
                return props;
            }
            throw th2;
        } catch (Throwable th22) {
            Throwable th4 = th22;
            th22 = th;
            th = th4;
        }
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (Throwable th5) {
                if (th22 == null) {
                    th22 = th5;
                } else if (th22 != th5) {
                    th22.addSuppressed(th5);
                }
            }
        }
        if (th22 != null) {
            throw th22;
        }
        throw th;
    }

    private void loadCalendarData() {
        String key;
        try {
            int year;
            String resourceName = calendarProperties.getProperty(PROP_PREFIX + this.typeId);
            Objects.requireNonNull((Object) resourceName, "Resource missing for calendar: calendar.hijrah." + this.typeId);
            Properties props = readConfigProperties(resourceName);
            Map<Integer, int[]> years = new HashMap();
            int minYear = Integer.MAX_VALUE;
            int maxYear = Integer.MIN_VALUE;
            String id = null;
            String type = null;
            String version = null;
            int isoStart = 0;
            for (Entry<Object, Object> entry : props.entrySet()) {
                key = (String) entry.getKey();
                if (key.equals("id")) {
                    id = (String) entry.getValue();
                } else if (key.equals(KEY_TYPE)) {
                    type = (String) entry.getValue();
                } else if (key.equals("version")) {
                    version = (String) entry.getValue();
                } else if (key.equals(KEY_ISO_START)) {
                    int[] ymd = parseYMD((String) entry.getValue());
                    isoStart = (int) LocalDate.of(ymd[0], ymd[1], ymd[2]).toEpochDay();
                } else {
                    year = Integer.valueOf(key).intValue();
                    years.put(Integer.valueOf(year), parseMonths((String) entry.getValue()));
                    maxYear = Math.max(maxYear, year);
                    minYear = Math.min(minYear, year);
                }
            }
            if (!getId().equals(id)) {
                throw new IllegalArgumentException("Configuration is for a different calendar: " + id);
            } else if (!getCalendarType().equals(type)) {
                throw new IllegalArgumentException("Configuration is for a different calendar type: " + type);
            } else if (version == null || version.isEmpty()) {
                throw new IllegalArgumentException("Configuration does not contain a version");
            } else if (isoStart == 0) {
                throw new IllegalArgumentException("Configuration does not contain a ISO start date");
            } else {
                this.hijrahStartEpochMonth = minYear * 12;
                this.minEpochDay = isoStart;
                this.hijrahEpochMonthStartDays = createEpochMonths(this.minEpochDay, minYear, maxYear, years);
                this.maxEpochDay = this.hijrahEpochMonthStartDays[this.hijrahEpochMonthStartDays.length - 1];
                for (year = minYear; year < maxYear; year++) {
                    int length = getYearLength(year);
                    this.minYearLength = Math.min(this.minYearLength, length);
                    this.maxYearLength = Math.max(this.maxYearLength, length);
                }
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("bad key: " + key);
        } catch (Throwable ex) {
            PlatformLogger.getLogger("java.time.chrono").severe("Unable to initialize Hijrah calendar proxy: " + this.typeId, ex);
            throw new DateTimeException("Unable to initialize HijrahCalendar: " + this.typeId, ex);
        }
    }

    private int[] createEpochMonths(int epochDay, int minYear, int maxYear, Map<Integer, int[]> years) {
        int epochMonth;
        int[] epochMonths = new int[((((maxYear - minYear) + 1) * 12) + 1)];
        this.minMonthLength = Integer.MAX_VALUE;
        this.maxMonthLength = Integer.MIN_VALUE;
        int epochMonth2 = 0;
        for (int year = minYear; year <= maxYear; year++) {
            int[] months = (int[]) years.get(Integer.valueOf(year));
            int month = 0;
            while (month < 12) {
                int length = months[month];
                epochMonth = epochMonth2 + 1;
                epochMonths[epochMonth2] = epochDay;
                if (length < 29 || length > 32) {
                    throw new IllegalArgumentException("Invalid month length in year: " + minYear);
                }
                epochDay += length;
                this.minMonthLength = Math.min(this.minMonthLength, length);
                this.maxMonthLength = Math.max(this.maxMonthLength, length);
                month++;
                epochMonth2 = epochMonth;
            }
        }
        epochMonth = epochMonth2 + 1;
        epochMonths[epochMonth2] = epochDay;
        if (epochMonth == epochMonths.length) {
            return epochMonths;
        }
        throw new IllegalStateException("Did not fill epochMonths exactly: ndx = " + epochMonth + " should be " + epochMonths.length);
    }

    private int[] parseMonths(String line) {
        int[] months = new int[12];
        Object[] numbers = line.split("\\s");
        if (numbers.length != 12) {
            throw new IllegalArgumentException("wrong number of months on line: " + Arrays.toString(numbers) + "; count: " + numbers.length);
        }
        int i = 0;
        while (i < 12) {
            try {
                months[i] = Integer.valueOf(numbers[i]).intValue();
                i++;
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("bad key: " + numbers[i]);
            }
        }
        return months;
    }

    private int[] parseYMD(String string) {
        string = string.trim();
        try {
            if (string.charAt(4) == '-' && string.charAt(7) == '-') {
                return new int[]{Integer.valueOf(string.substring(0, 4)).intValue(), Integer.valueOf(string.substring(5, 7)).intValue(), Integer.valueOf(string.substring(8, 10)).intValue()};
            }
            throw new IllegalArgumentException("date must be yyyy-MM-dd");
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("date must be yyyy-MM-dd", ex);
        }
    }

    Object writeReplace() {
        return super.writeReplace();
    }

    private void readObject(ObjectInputStream s) throws InvalidObjectException {
        throw new InvalidObjectException("Deserialization via serialization delegate");
    }
}
