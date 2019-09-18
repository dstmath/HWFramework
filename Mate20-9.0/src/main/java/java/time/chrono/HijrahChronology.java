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
import java.util.Objects;
import java.util.Properties;
import sun.util.calendar.BaseCalendar;
import sun.util.logging.PlatformLogger;

public final class HijrahChronology extends AbstractChronology implements Serializable {
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

    static {
        try {
            calendarProperties = BaseCalendar.getCalendarProperties();
            try {
                INSTANCE = new HijrahChronology("Hijrah-umalqura");
                AbstractChronology.registerChrono(INSTANCE, "Hijrah");
                AbstractChronology.registerChrono(INSTANCE, "islamic");
                registerVariants();
            } catch (DateTimeException ex) {
                PlatformLogger.getLogger("java.time.chrono").severe("Unable to initialize Hijrah calendar: Hijrah-umalqura", (Throwable) ex);
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
                    } catch (DateTimeException ex) {
                        PlatformLogger logger = PlatformLogger.getLogger("java.time.chrono");
                        logger.severe("Unable to initialize Hijrah calendar: " + id, (Throwable) ex);
                    }
                }
            }
        }
    }

    private HijrahChronology(String id) throws DateTimeException {
        if (!id.isEmpty()) {
            String calType = calendarProperties.getProperty(PROP_PREFIX + id + PROP_TYPE_SUFFIX);
            if (calType == null || calType.isEmpty()) {
                throw new DateTimeException("calendarType is missing or empty for: " + propName);
            }
            this.typeId = id;
            this.calendarType = calType;
            return;
        }
        throw new IllegalArgumentException("calendar id is empty");
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
        return date((TemporalAccessor) LocalDate.now(clock));
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
        checkCalendarInit();
        boolean z = false;
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
        if (eraValue == 1) {
            return HijrahEra.AH;
        }
        throw new DateTimeException("invalid Hijrah era");
    }

    public List<Era> eras() {
        return Arrays.asList(HijrahEra.values());
    }

    public ValueRange range(ChronoField field) {
        checkCalendarInit();
        if (!(field instanceof ChronoField)) {
            return field.range();
        }
        switch (field) {
            case DAY_OF_MONTH:
                return ValueRange.of(1, 1, (long) getMinimumMonthLength(), (long) getMaximumMonthLength());
            case DAY_OF_YEAR:
                return ValueRange.of(1, (long) getMaximumDayOfYear());
            case ALIGNED_WEEK_OF_MONTH:
                return ValueRange.of(1, 5);
            case YEAR:
            case YEAR_OF_ERA:
                return ValueRange.of((long) getMinimumYear(), (long) getMaximumYear());
            case ERA:
                return ValueRange.of(1, 1);
            default:
                return field.range();
        }
    }

    public HijrahDate resolveDate(Map<TemporalField, Long> fieldValues, ResolverStyle resolverStyle) {
        return (HijrahDate) super.resolveDate(fieldValues, resolverStyle);
    }

    /* access modifiers changed from: package-private */
    public int checkValidYear(long prolepticYear) {
        if (prolepticYear >= ((long) getMinimumYear()) && prolepticYear <= ((long) getMaximumYear())) {
            return (int) prolepticYear;
        }
        throw new DateTimeException("Invalid Hijrah year: " + prolepticYear);
    }

    /* access modifiers changed from: package-private */
    public void checkValidDayOfYear(int dayOfYear) {
        if (dayOfYear < 1 || dayOfYear > getMaximumDayOfYear()) {
            throw new DateTimeException("Invalid Hijrah day of year: " + dayOfYear);
        }
    }

    /* access modifiers changed from: package-private */
    public void checkValidMonth(int month) {
        if (month < 1 || month > 12) {
            throw new DateTimeException("Invalid Hijrah month: " + month);
        }
    }

    /* access modifiers changed from: package-private */
    public int[] getHijrahDateInfo(int epochDay) {
        checkCalendarInit();
        if (epochDay < this.minEpochDay || epochDay >= this.maxEpochDay) {
            throw new DateTimeException("Hijrah date out of range");
        }
        int epochMonth = epochDayToEpochMonth(epochDay);
        return new int[]{epochMonthToYear(epochMonth), epochMonthToMonth(epochMonth) + 1, (epochDay - epochMonthToEpochDay(epochMonth)) + 1};
    }

    /* access modifiers changed from: package-private */
    public long getEpochDay(int prolepticYear, int monthOfYear, int dayOfMonth) {
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

    /* access modifiers changed from: package-private */
    public int getDayOfYear(int prolepticYear, int month) {
        return yearMonthToDayOfYear(prolepticYear, month - 1);
    }

    /* access modifiers changed from: package-private */
    public int getMonthLength(int prolepticYear, int monthOfYear) {
        int epochMonth = yearToEpochMonth(prolepticYear) + (monthOfYear - 1);
        if (epochMonth >= 0 && epochMonth < this.hijrahEpochMonthStartDays.length) {
            return epochMonthLength(epochMonth);
        }
        throw new DateTimeException("Invalid Hijrah date, year: " + prolepticYear + ", month: " + monthOfYear);
    }

    /* access modifiers changed from: package-private */
    public int getYearLength(int prolepticYear) {
        return yearMonthToDayOfYear(prolepticYear, 12);
    }

    /* access modifiers changed from: package-private */
    public int getMinimumYear() {
        return epochMonthToYear(0);
    }

    /* access modifiers changed from: package-private */
    public int getMaximumYear() {
        return epochMonthToYear(this.hijrahEpochMonthStartDays.length - 1) - 1;
    }

    /* access modifiers changed from: package-private */
    public int getMaximumMonthLength() {
        return this.maxMonthLength;
    }

    /* access modifiers changed from: package-private */
    public int getMinimumMonthLength() {
        return this.minMonthLength;
    }

    /* access modifiers changed from: package-private */
    public int getMaximumDayOfYear() {
        return this.maxYearLength;
    }

    /* access modifiers changed from: package-private */
    public int getSmallestMaximumDayOfYear() {
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

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0017, code lost:
        r2 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:6:0x0012, code lost:
        r2 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:7:0x0013, code lost:
        r3 = null;
     */
    private static Properties readConfigProperties(String resource) throws Exception {
        Properties props = new Properties();
        InputStream is = ClassLoader.getSystemResourceAsStream(resource);
        props.load(is);
        if (is != null) {
            is.close();
        }
        return props;
        throw th;
        if (is != null) {
            if (r3 != null) {
                try {
                    is.close();
                } catch (Throwable th) {
                    r3.addSuppressed(th);
                }
            } else {
                is.close();
            }
        }
        throw th;
    }

    /* JADX WARNING: Removed duplicated region for block: B:28:0x00b4  */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x00bd A[SYNTHETIC, Splitter:B:31:0x00bd] */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x00dd A[Catch:{ NumberFormatException -> 0x012b, Exception -> 0x01d7 }] */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x00ea A[Catch:{ NumberFormatException -> 0x012b, Exception -> 0x01d7 }] */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x00f7 A[Catch:{ NumberFormatException -> 0x012b, Exception -> 0x01d7 }] */
    private void loadCalendarData() {
        String key;
        char c;
        Properties props;
        String resourceName;
        try {
            String resourceName2 = calendarProperties.getProperty(PROP_PREFIX + this.typeId);
            Objects.requireNonNull(resourceName2, "Resource missing for calendar: calendar.hijrah." + this.typeId);
            Properties props2 = readConfigProperties(resourceName2);
            Map<Integer, int[]> years = new HashMap<>();
            String id = null;
            String type = null;
            String version = null;
            int isoStart = 0;
            int maxYear = Integer.MIN_VALUE;
            int minYear = Integer.MAX_VALUE;
            for (Map.Entry entry : props2.entrySet()) {
                key = (String) entry.getKey();
                int hashCode = key.hashCode();
                if (hashCode != -1117701862) {
                    if (hashCode != 3355) {
                        if (hashCode != 3575610) {
                            if (hashCode == 351608024) {
                                if (key.equals("version")) {
                                    c = 2;
                                    switch (c) {
                                        case 0:
                                            resourceName = resourceName2;
                                            props = props2;
                                            id = (String) entry.getValue();
                                            break;
                                        case 1:
                                            resourceName = resourceName2;
                                            props = props2;
                                            type = (String) entry.getValue();
                                            break;
                                        case 2:
                                            resourceName = resourceName2;
                                            props = props2;
                                            version = (String) entry.getValue();
                                            break;
                                        case 3:
                                            int[] ymd = parseYMD((String) entry.getValue());
                                            resourceName = resourceName2;
                                            props = props2;
                                            isoStart = (int) LocalDate.of(ymd[0], ymd[1], ymd[2]).toEpochDay();
                                            break;
                                        default:
                                            resourceName = resourceName2;
                                            props = props2;
                                            int year = Integer.valueOf(key).intValue();
                                            years.put(Integer.valueOf(year), parseMonths((String) entry.getValue()));
                                            maxYear = Math.max(maxYear, year);
                                            minYear = Math.min(minYear, year);
                                            break;
                                    }
                                    resourceName2 = resourceName;
                                    props2 = props;
                                }
                            }
                        } else if (key.equals(KEY_TYPE)) {
                            c = 1;
                            switch (c) {
                                case 0:
                                    break;
                                case 1:
                                    break;
                                case 2:
                                    break;
                                case 3:
                                    break;
                            }
                            resourceName2 = resourceName;
                            props2 = props;
                        }
                    } else if (key.equals("id")) {
                        c = 0;
                        switch (c) {
                            case 0:
                                break;
                            case 1:
                                break;
                            case 2:
                                break;
                            case 3:
                                break;
                        }
                        resourceName2 = resourceName;
                        props2 = props;
                    }
                } else if (key.equals(KEY_ISO_START)) {
                    c = 3;
                    switch (c) {
                        case 0:
                            break;
                        case 1:
                            break;
                        case 2:
                            break;
                        case 3:
                            break;
                    }
                    resourceName2 = resourceName;
                    props2 = props;
                }
                c = 65535;
                switch (c) {
                    case 0:
                        break;
                    case 1:
                        break;
                    case 2:
                        break;
                    case 3:
                        break;
                }
                resourceName2 = resourceName;
                props2 = props;
            }
            Properties properties = props2;
            if (!getId().equals(id)) {
                throw new IllegalArgumentException("Configuration is for a different calendar: " + id);
            } else if (!getCalendarType().equals(type)) {
                throw new IllegalArgumentException("Configuration is for a different calendar type: " + type);
            } else if (version == null || version.isEmpty()) {
                throw new IllegalArgumentException("Configuration does not contain a version");
            } else if (isoStart != 0) {
                this.hijrahStartEpochMonth = minYear * 12;
                this.minEpochDay = isoStart;
                this.hijrahEpochMonthStartDays = createEpochMonths(this.minEpochDay, minYear, maxYear, years);
                this.maxEpochDay = this.hijrahEpochMonthStartDays[this.hijrahEpochMonthStartDays.length - 1];
                for (int year2 = minYear; year2 < maxYear; year2++) {
                    int length = getYearLength(year2);
                    this.minYearLength = Math.min(this.minYearLength, length);
                    this.maxYearLength = Math.max(this.maxYearLength, length);
                }
            } else {
                throw new IllegalArgumentException("Configuration does not contain a ISO start date");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("bad key: " + key);
        } catch (Exception ex) {
            PlatformLogger.getLogger("java.time.chrono").severe("Unable to initialize Hijrah calendar proxy: " + this.typeId, (Throwable) ex);
            throw new DateTimeException("Unable to initialize HijrahCalendar: " + this.typeId, ex);
        }
    }

    private int[] createEpochMonths(int epochDay, int minYear, int maxYear, Map<Integer, int[]> years) {
        int epochMonth = 0;
        int[] epochMonths = new int[((((maxYear - minYear) + 1) * 12) + 1)];
        this.minMonthLength = Integer.MAX_VALUE;
        this.maxMonthLength = Integer.MIN_VALUE;
        int epochDay2 = epochDay;
        for (int year = minYear; year <= maxYear; year++) {
            int[] months = years.get(Integer.valueOf(year));
            int month = 0;
            while (month < 12) {
                int length = months[month];
                int epochMonth2 = epochMonth + 1;
                epochMonths[epochMonth] = epochDay2;
                if (length < 29 || length > 32) {
                    throw new IllegalArgumentException("Invalid month length in year: " + minYear);
                }
                epochDay2 += length;
                this.minMonthLength = Math.min(this.minMonthLength, length);
                this.maxMonthLength = Math.max(this.maxMonthLength, length);
                month++;
                epochMonth = epochMonth2;
            }
        }
        int epochMonth3 = epochMonth + 1;
        epochMonths[epochMonth] = epochDay2;
        if (epochMonth3 == epochMonths.length) {
            return epochMonths;
        }
        throw new IllegalStateException("Did not fill epochMonths exactly: ndx = " + epochMonth3 + " should be " + epochMonths.length);
    }

    private int[] parseMonths(String line) {
        int[] months = new int[12];
        String[] numbers = line.split("\\s");
        if (numbers.length == 12) {
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
        throw new IllegalArgumentException("wrong number of months on line: " + Arrays.toString((Object[]) numbers) + "; count: " + numbers.length);
    }

    private int[] parseYMD(String string) {
        String string2 = string.trim();
        try {
            if (string2.charAt(4) == '-' && string2.charAt(7) == '-') {
                return new int[]{Integer.valueOf(string2.substring(0, 4)).intValue(), Integer.valueOf(string2.substring(5, 7)).intValue(), Integer.valueOf(string2.substring(8, 10)).intValue()};
            }
            throw new IllegalArgumentException("date must be yyyy-MM-dd");
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("date must be yyyy-MM-dd", ex);
        }
    }

    /* access modifiers changed from: package-private */
    public Object writeReplace() {
        return super.writeReplace();
    }

    private void readObject(ObjectInputStream s) throws InvalidObjectException {
        throw new InvalidObjectException("Deserialization via serialization delegate");
    }
}
