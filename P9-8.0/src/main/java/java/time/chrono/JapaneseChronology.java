package java.time.chrono;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.time.Clock;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Year;
import java.time.ZoneId;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.TemporalField;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.time.temporal.ValueRange;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import sun.util.calendar.CalendarSystem;
import sun.util.calendar.LocalGregorianCalendar;
import sun.util.calendar.LocalGregorianCalendar.Date;

public final class JapaneseChronology extends AbstractChronology implements Serializable {
    private static final /* synthetic */ int[] -java-time-temporal-ChronoFieldSwitchesValues = null;
    public static final JapaneseChronology INSTANCE = new JapaneseChronology();
    static final LocalGregorianCalendar JCAL = ((LocalGregorianCalendar) CalendarSystem.forName("japanese"));
    private static final Locale LOCALE = Locale.forLanguageTag("ja-JP-u-ca-japanese");
    private static final long serialVersionUID = 459996390165777884L;

    private static /* synthetic */ int[] -getjava-time-temporal-ChronoFieldSwitchesValues() {
        if (-java-time-temporal-ChronoFieldSwitchesValues != null) {
            return -java-time-temporal-ChronoFieldSwitchesValues;
        }
        int[] iArr = new int[ChronoField.values().length];
        try {
            iArr[ChronoField.ALIGNED_DAY_OF_WEEK_IN_MONTH.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[ChronoField.ALIGNED_DAY_OF_WEEK_IN_YEAR.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[ChronoField.ALIGNED_WEEK_OF_MONTH.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[ChronoField.ALIGNED_WEEK_OF_YEAR.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[ChronoField.AMPM_OF_DAY.ordinal()] = 9;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[ChronoField.CLOCK_HOUR_OF_AMPM.ordinal()] = 10;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[ChronoField.CLOCK_HOUR_OF_DAY.ordinal()] = 11;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[ChronoField.DAY_OF_MONTH.ordinal()] = 12;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[ChronoField.DAY_OF_WEEK.ordinal()] = 13;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[ChronoField.DAY_OF_YEAR.ordinal()] = 5;
        } catch (NoSuchFieldError e10) {
        }
        try {
            iArr[ChronoField.EPOCH_DAY.ordinal()] = 14;
        } catch (NoSuchFieldError e11) {
        }
        try {
            iArr[ChronoField.ERA.ordinal()] = 6;
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
            iArr[ChronoField.YEAR.ordinal()] = 7;
        } catch (NoSuchFieldError e29) {
        }
        try {
            iArr[ChronoField.YEAR_OF_ERA.ordinal()] = 8;
        } catch (NoSuchFieldError e30) {
        }
        -java-time-temporal-ChronoFieldSwitchesValues = iArr;
        return iArr;
    }

    static Calendar createCalendar() {
        return Calendar.getJapaneseImperialInstance(TimeZone.getDefault(), LOCALE);
    }

    private JapaneseChronology() {
    }

    public String getId() {
        return "Japanese";
    }

    public String getCalendarType() {
        return "japanese";
    }

    public JapaneseDate date(Era era, int yearOfEra, int month, int dayOfMonth) {
        if (era instanceof JapaneseEra) {
            return JapaneseDate.of((JapaneseEra) era, yearOfEra, month, dayOfMonth);
        }
        throw new ClassCastException("Era must be JapaneseEra");
    }

    public JapaneseDate date(int prolepticYear, int month, int dayOfMonth) {
        return new JapaneseDate(LocalDate.of(prolepticYear, month, dayOfMonth));
    }

    public JapaneseDate dateYearDay(Era era, int yearOfEra, int dayOfYear) {
        return JapaneseDate.ofYearDay((JapaneseEra) era, yearOfEra, dayOfYear);
    }

    public JapaneseDate dateYearDay(int prolepticYear, int dayOfYear) {
        return new JapaneseDate(LocalDate.ofYearDay(prolepticYear, dayOfYear));
    }

    public JapaneseDate dateEpochDay(long epochDay) {
        return new JapaneseDate(LocalDate.ofEpochDay(epochDay));
    }

    public JapaneseDate dateNow() {
        return dateNow(Clock.systemDefaultZone());
    }

    public JapaneseDate dateNow(ZoneId zone) {
        return dateNow(Clock.system(zone));
    }

    public JapaneseDate dateNow(Clock clock) {
        return date(LocalDate.now(clock));
    }

    public JapaneseDate date(TemporalAccessor temporal) {
        if (temporal instanceof JapaneseDate) {
            return (JapaneseDate) temporal;
        }
        return new JapaneseDate(LocalDate.from(temporal));
    }

    public ChronoLocalDateTime<JapaneseDate> localDateTime(TemporalAccessor temporal) {
        return super.localDateTime(temporal);
    }

    public ChronoZonedDateTime<JapaneseDate> zonedDateTime(TemporalAccessor temporal) {
        return super.zonedDateTime(temporal);
    }

    public ChronoZonedDateTime<JapaneseDate> zonedDateTime(Instant instant, ZoneId zone) {
        return super.zonedDateTime(instant, zone);
    }

    public boolean isLeapYear(long prolepticYear) {
        return IsoChronology.INSTANCE.isLeapYear(prolepticYear);
    }

    public int prolepticYear(Era era, int yearOfEra) {
        if (era instanceof JapaneseEra) {
            JapaneseEra jera = (JapaneseEra) era;
            int gregorianYear = (jera.getPrivateEra().getSinceDate().getYear() + yearOfEra) - 1;
            if (yearOfEra == 1) {
                return gregorianYear;
            }
            if (gregorianYear >= Year.MIN_VALUE && gregorianYear <= Year.MAX_VALUE) {
                Date jdate = JCAL.newCalendarDate(null);
                jdate.setEra(jera.getPrivateEra()).setDate(yearOfEra, 1, 1);
                if (JCAL.validate(jdate)) {
                    return gregorianYear;
                }
            }
            throw new DateTimeException("Invalid yearOfEra value");
        }
        throw new ClassCastException("Era must be JapaneseEra");
    }

    public JapaneseEra eraOf(int eraValue) {
        return JapaneseEra.of(eraValue);
    }

    public List<Era> eras() {
        return Arrays.asList(JapaneseEra.values());
    }

    JapaneseEra getCurrentEra() {
        JapaneseEra[] eras = JapaneseEra.values();
        return eras[eras.length - 1];
    }

    public ValueRange range(ChronoField field) {
        Calendar jcal;
        switch (-getjava-time-temporal-ChronoFieldSwitchesValues()[field.ordinal()]) {
            case 1:
            case 2:
            case 3:
            case 4:
                throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
            case 5:
                jcal = createCalendar();
                return ValueRange.of((long) jcal.getMinimum(6), (long) jcal.getGreatestMinimum(6), (long) jcal.getLeastMaximum(6), (long) jcal.getMaximum(6));
            case 6:
                return ValueRange.of((long) JapaneseEra.MEIJI.getValue(), (long) getCurrentEra().getValue());
            case 7:
                return ValueRange.of((long) JapaneseDate.MEIJI_6_ISODATE.getYear(), 999999999);
            case 8:
                jcal = createCalendar();
                return ValueRange.of(1, (long) jcal.getGreatestMinimum(1), (long) (jcal.getLeastMaximum(1) + 1), (long) (Year.MAX_VALUE - getCurrentEra().getPrivateEra().getSinceDate().getYear()));
            default:
                return field.range();
        }
    }

    public JapaneseDate resolveDate(Map<TemporalField, Long> fieldValues, ResolverStyle resolverStyle) {
        return (JapaneseDate) super.resolveDate(fieldValues, resolverStyle);
    }

    ChronoLocalDate resolveYearOfEra(Map<TemporalField, Long> fieldValues, ResolverStyle resolverStyle) {
        Long eraLong = (Long) fieldValues.get(ChronoField.ERA);
        JapaneseEra era = null;
        if (eraLong != null) {
            era = eraOf(range(ChronoField.ERA).checkValidIntValue(eraLong.longValue(), ChronoField.ERA));
        }
        Long yoeLong = (Long) fieldValues.get(ChronoField.YEAR_OF_ERA);
        int yoe = 0;
        if (yoeLong != null) {
            yoe = range(ChronoField.YEAR_OF_ERA).checkValidIntValue(yoeLong.longValue(), ChronoField.YEAR_OF_ERA);
        }
        if (!(era != null || yoeLong == null || fieldValues.containsKey(ChronoField.YEAR) || resolverStyle == ResolverStyle.STRICT)) {
            era = JapaneseEra.values()[JapaneseEra.values().length - 1];
        }
        if (!(yoeLong == null || era == null)) {
            if (fieldValues.containsKey(ChronoField.MONTH_OF_YEAR) && fieldValues.containsKey(ChronoField.DAY_OF_MONTH)) {
                return resolveYMD(era, yoe, fieldValues, resolverStyle);
            }
            if (fieldValues.containsKey(ChronoField.DAY_OF_YEAR)) {
                return resolveYD(era, yoe, fieldValues, resolverStyle);
            }
        }
        return null;
    }

    private int prolepticYearLenient(JapaneseEra era, int yearOfEra) {
        return (era.getPrivateEra().getSinceDate().getYear() + yearOfEra) - 1;
    }

    private ChronoLocalDate resolveYMD(JapaneseEra era, int yoe, Map<TemporalField, Long> fieldValues, ResolverStyle resolverStyle) {
        fieldValues.remove(ChronoField.ERA);
        fieldValues.remove(ChronoField.YEAR_OF_ERA);
        int y;
        if (resolverStyle == ResolverStyle.LENIENT) {
            y = prolepticYearLenient(era, yoe);
            long months = Math.subtractExact(((Long) fieldValues.remove(ChronoField.MONTH_OF_YEAR)).longValue(), 1);
            return date(y, 1, 1).plus(months, ChronoUnit.MONTHS).plus(Math.subtractExact(((Long) fieldValues.remove(ChronoField.DAY_OF_MONTH)).longValue(), 1), ChronoUnit.DAYS);
        }
        int moy = range(ChronoField.MONTH_OF_YEAR).checkValidIntValue(((Long) fieldValues.remove(ChronoField.MONTH_OF_YEAR)).longValue(), ChronoField.MONTH_OF_YEAR);
        int dom = range(ChronoField.DAY_OF_MONTH).checkValidIntValue(((Long) fieldValues.remove(ChronoField.DAY_OF_MONTH)).longValue(), ChronoField.DAY_OF_MONTH);
        if (resolverStyle != ResolverStyle.SMART) {
            return date((Era) era, yoe, moy, dom);
        }
        if (yoe < 1) {
            throw new DateTimeException("Invalid YearOfEra: " + yoe);
        }
        JapaneseDate result;
        y = prolepticYearLenient(era, yoe);
        try {
            result = date(y, moy, dom);
        } catch (DateTimeException e) {
            result = date(y, moy, 1).with(TemporalAdjusters.lastDayOfMonth());
        }
        if (result.getEra() == era || result.get(ChronoField.YEAR_OF_ERA) <= 1 || yoe <= 1) {
            return result;
        }
        throw new DateTimeException("Invalid YearOfEra for Era: " + era + " " + yoe);
    }

    private ChronoLocalDate resolveYD(JapaneseEra era, int yoe, Map<TemporalField, Long> fieldValues, ResolverStyle resolverStyle) {
        fieldValues.remove(ChronoField.ERA);
        fieldValues.remove(ChronoField.YEAR_OF_ERA);
        if (resolverStyle != ResolverStyle.LENIENT) {
            return dateYearDay((Era) era, yoe, range(ChronoField.DAY_OF_YEAR).checkValidIntValue(((Long) fieldValues.remove(ChronoField.DAY_OF_YEAR)).longValue(), ChronoField.DAY_OF_YEAR));
        }
        int y = prolepticYearLenient(era, yoe);
        return dateYearDay(y, 1).plus(Math.subtractExact(((Long) fieldValues.remove(ChronoField.DAY_OF_YEAR)).longValue(), 1), ChronoUnit.DAYS);
    }

    Object writeReplace() {
        return super.writeReplace();
    }

    private void readObject(ObjectInputStream s) throws InvalidObjectException {
        throw new InvalidObjectException("Deserialization via serialization delegate");
    }
}
