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
import java.time.temporal.TemporalUnit;
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

public final class JapaneseChronology extends AbstractChronology implements Serializable {
    public static final JapaneseChronology INSTANCE = new JapaneseChronology();
    static final LocalGregorianCalendar JCAL = ((LocalGregorianCalendar) CalendarSystem.forName("japanese"));
    private static final Locale LOCALE = Locale.forLanguageTag("ja-JP-u-ca-japanese");
    private static final long serialVersionUID = 459996390165777884L;

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
        return date((TemporalAccessor) LocalDate.now(clock));
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
            if (gregorianYear >= -999999999 && gregorianYear <= 999999999) {
                LocalGregorianCalendar.Date jdate = JCAL.newCalendarDate((TimeZone) null);
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

    /* access modifiers changed from: package-private */
    public JapaneseEra getCurrentEra() {
        JapaneseEra[] eras = JapaneseEra.values();
        return eras[eras.length - 1];
    }

    public ValueRange range(ChronoField field) {
        switch (field) {
            case ALIGNED_DAY_OF_WEEK_IN_MONTH:
            case ALIGNED_DAY_OF_WEEK_IN_YEAR:
            case ALIGNED_WEEK_OF_MONTH:
            case ALIGNED_WEEK_OF_YEAR:
                throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
            case YEAR_OF_ERA:
                Calendar jcal = createCalendar();
                return ValueRange.of(1, (long) jcal.getGreatestMinimum(1), (long) (jcal.getLeastMaximum(1) + 1), (long) (Year.MAX_VALUE - getCurrentEra().getPrivateEra().getSinceDate().getYear()));
            case DAY_OF_YEAR:
                Calendar jcal2 = createCalendar();
                return ValueRange.of((long) jcal2.getMinimum(6), (long) jcal2.getGreatestMinimum(6), (long) jcal2.getLeastMaximum(6), (long) jcal2.getMaximum(6));
            case YEAR:
                return ValueRange.of((long) JapaneseDate.MEIJI_6_ISODATE.getYear(), 999999999);
            case ERA:
                return ValueRange.of((long) JapaneseEra.MEIJI.getValue(), (long) getCurrentEra().getValue());
            default:
                return field.range();
        }
    }

    public JapaneseDate resolveDate(Map<TemporalField, Long> fieldValues, ResolverStyle resolverStyle) {
        return (JapaneseDate) super.resolveDate(fieldValues, resolverStyle);
    }

    /* access modifiers changed from: package-private */
    public ChronoLocalDate resolveYearOfEra(Map<TemporalField, Long> fieldValues, ResolverStyle resolverStyle) {
        Long eraLong = fieldValues.get(ChronoField.ERA);
        JapaneseEra era = null;
        if (eraLong != null) {
            era = eraOf(range(ChronoField.ERA).checkValidIntValue(eraLong.longValue(), ChronoField.ERA));
        }
        Long yoeLong = fieldValues.get(ChronoField.YEAR_OF_ERA);
        int yoe = 0;
        if (yoeLong != null) {
            yoe = range(ChronoField.YEAR_OF_ERA).checkValidIntValue(yoeLong.longValue(), ChronoField.YEAR_OF_ERA);
        }
        if (era == null && yoeLong != null && !fieldValues.containsKey(ChronoField.YEAR) && resolverStyle != ResolverStyle.STRICT) {
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
        JapaneseDate result;
        fieldValues.remove(ChronoField.ERA);
        fieldValues.remove(ChronoField.YEAR_OF_ERA);
        if (resolverStyle == ResolverStyle.LENIENT) {
            int y = prolepticYearLenient(era, yoe);
            long months = Math.subtractExact(fieldValues.remove(ChronoField.MONTH_OF_YEAR).longValue(), 1);
            return date(y, 1, 1).plus(months, (TemporalUnit) ChronoUnit.MONTHS).plus(Math.subtractExact(fieldValues.remove(ChronoField.DAY_OF_MONTH).longValue(), 1), (TemporalUnit) ChronoUnit.DAYS);
        }
        int moy = range(ChronoField.MONTH_OF_YEAR).checkValidIntValue(fieldValues.remove(ChronoField.MONTH_OF_YEAR).longValue(), ChronoField.MONTH_OF_YEAR);
        int dom = range(ChronoField.DAY_OF_MONTH).checkValidIntValue(fieldValues.remove(ChronoField.DAY_OF_MONTH).longValue(), ChronoField.DAY_OF_MONTH);
        if (resolverStyle != ResolverStyle.SMART) {
            return date((Era) era, yoe, moy, dom);
        }
        if (yoe >= 1) {
            int y2 = prolepticYearLenient(era, yoe);
            try {
                result = date(y2, moy, dom);
            } catch (DateTimeException e) {
                result = date(y2, moy, 1).with(TemporalAdjusters.lastDayOfMonth());
            }
            if (result.getEra() == era || result.get(ChronoField.YEAR_OF_ERA) <= 1 || yoe <= 1) {
                return result;
            }
            throw new DateTimeException("Invalid YearOfEra for Era: " + era + " " + yoe);
        }
        throw new DateTimeException("Invalid YearOfEra: " + yoe);
    }

    private ChronoLocalDate resolveYD(JapaneseEra era, int yoe, Map<TemporalField, Long> fieldValues, ResolverStyle resolverStyle) {
        fieldValues.remove(ChronoField.ERA);
        fieldValues.remove(ChronoField.YEAR_OF_ERA);
        if (resolverStyle != ResolverStyle.LENIENT) {
            return dateYearDay((Era) era, yoe, range(ChronoField.DAY_OF_YEAR).checkValidIntValue(fieldValues.remove(ChronoField.DAY_OF_YEAR).longValue(), ChronoField.DAY_OF_YEAR));
        }
        int y = prolepticYearLenient(era, yoe);
        return dateYearDay(y, 1).plus(Math.subtractExact(fieldValues.remove(ChronoField.DAY_OF_YEAR).longValue(), 1), (TemporalUnit) ChronoUnit.DAYS);
    }

    /* access modifiers changed from: package-private */
    public Object writeReplace() {
        return super.writeReplace();
    }

    private void readObject(ObjectInputStream s) throws InvalidObjectException {
        throw new InvalidObjectException("Deserialization via serialization delegate");
    }
}
