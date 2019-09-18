package java.time.chrono;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.time.Clock;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.temporal.ChronoField;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalUnit;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.time.temporal.ValueRange;
import java.util.Calendar;
import java.util.Objects;
import java.util.TimeZone;
import sun.util.calendar.CalendarDate;
import sun.util.calendar.Era;
import sun.util.calendar.LocalGregorianCalendar;

public final class JapaneseDate extends ChronoLocalDateImpl<JapaneseDate> implements ChronoLocalDate, Serializable {
    static final LocalDate MEIJI_6_ISODATE = LocalDate.of(1873, 1, 1);
    private static final long serialVersionUID = -305327627230580483L;
    private transient JapaneseEra era;
    private final transient LocalDate isoDate;
    private transient int yearOfEra;

    public /* bridge */ /* synthetic */ String toString() {
        return super.toString();
    }

    public /* bridge */ /* synthetic */ long until(Temporal temporal, TemporalUnit temporalUnit) {
        return super.until(temporal, temporalUnit);
    }

    public static JapaneseDate now() {
        return now(Clock.systemDefaultZone());
    }

    public static JapaneseDate now(ZoneId zone) {
        return now(Clock.system(zone));
    }

    public static JapaneseDate now(Clock clock) {
        return new JapaneseDate(LocalDate.now(clock));
    }

    public static JapaneseDate of(JapaneseEra era2, int yearOfEra2, int month, int dayOfMonth) {
        Objects.requireNonNull(era2, "era");
        LocalGregorianCalendar.Date jdate = JapaneseChronology.JCAL.newCalendarDate((TimeZone) null);
        jdate.setEra(era2.getPrivateEra()).setDate(yearOfEra2, month, dayOfMonth);
        if (JapaneseChronology.JCAL.validate(jdate)) {
            return new JapaneseDate(era2, yearOfEra2, LocalDate.of(jdate.getNormalizedYear(), month, dayOfMonth));
        }
        throw new DateTimeException("year, month, and day not valid for Era");
    }

    public static JapaneseDate of(int prolepticYear, int month, int dayOfMonth) {
        return new JapaneseDate(LocalDate.of(prolepticYear, month, dayOfMonth));
    }

    static JapaneseDate ofYearDay(JapaneseEra era2, int yearOfEra2, int dayOfYear) {
        Objects.requireNonNull(era2, "era");
        CalendarDate firstDay = era2.getPrivateEra().getSinceDate();
        LocalGregorianCalendar.Date jdate = JapaneseChronology.JCAL.newCalendarDate((TimeZone) null);
        jdate.setEra(era2.getPrivateEra());
        if (yearOfEra2 == 1) {
            jdate.setDate(yearOfEra2, firstDay.getMonth(), (firstDay.getDayOfMonth() + dayOfYear) - 1);
        } else {
            jdate.setDate(yearOfEra2, 1, dayOfYear);
        }
        JapaneseChronology.JCAL.normalize(jdate);
        if (era2.getPrivateEra() == jdate.getEra() && yearOfEra2 == jdate.getYear()) {
            return new JapaneseDate(era2, yearOfEra2, LocalDate.of(jdate.getNormalizedYear(), jdate.getMonth(), jdate.getDayOfMonth()));
        }
        throw new DateTimeException("Invalid parameters");
    }

    public static JapaneseDate from(TemporalAccessor temporal) {
        return JapaneseChronology.INSTANCE.date(temporal);
    }

    JapaneseDate(LocalDate isoDate2) {
        if (!isoDate2.isBefore(MEIJI_6_ISODATE)) {
            LocalGregorianCalendar.Date jdate = toPrivateJapaneseDate(isoDate2);
            this.era = JapaneseEra.toJapaneseEra(jdate.getEra());
            this.yearOfEra = jdate.getYear();
            this.isoDate = isoDate2;
            return;
        }
        throw new DateTimeException("JapaneseDate before Meiji 6 is not supported");
    }

    JapaneseDate(JapaneseEra era2, int year, LocalDate isoDate2) {
        if (!isoDate2.isBefore(MEIJI_6_ISODATE)) {
            this.era = era2;
            this.yearOfEra = year;
            this.isoDate = isoDate2;
            return;
        }
        throw new DateTimeException("JapaneseDate before Meiji 6 is not supported");
    }

    public JapaneseChronology getChronology() {
        return JapaneseChronology.INSTANCE;
    }

    public JapaneseEra getEra() {
        return this.era;
    }

    public int lengthOfMonth() {
        return this.isoDate.lengthOfMonth();
    }

    public int lengthOfYear() {
        Calendar jcal = JapaneseChronology.createCalendar();
        jcal.set(0, this.era.getValue() + 2);
        jcal.set(this.yearOfEra, this.isoDate.getMonthValue() - 1, this.isoDate.getDayOfMonth());
        return jcal.getActualMaximum(6);
    }

    public boolean isSupported(TemporalField field) {
        if (field == ChronoField.ALIGNED_DAY_OF_WEEK_IN_MONTH || field == ChronoField.ALIGNED_DAY_OF_WEEK_IN_YEAR || field == ChronoField.ALIGNED_WEEK_OF_MONTH || field == ChronoField.ALIGNED_WEEK_OF_YEAR) {
            return false;
        }
        return super.isSupported(field);
    }

    public ValueRange range(TemporalField field) {
        if (!(field instanceof ChronoField)) {
            return field.rangeRefinedBy(this);
        }
        if (isSupported(field)) {
            ChronoField f = (ChronoField) field;
            switch (f) {
                case DAY_OF_MONTH:
                    return ValueRange.of(1, (long) lengthOfMonth());
                case DAY_OF_YEAR:
                    return ValueRange.of(1, (long) lengthOfYear());
                case YEAR_OF_ERA:
                    Calendar jcal = JapaneseChronology.createCalendar();
                    jcal.set(0, this.era.getValue() + 2);
                    jcal.set(this.yearOfEra, this.isoDate.getMonthValue() - 1, this.isoDate.getDayOfMonth());
                    return ValueRange.of(1, (long) jcal.getActualMaximum(1));
                default:
                    return getChronology().range(f);
            }
        } else {
            throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
        }
    }

    public long getLong(TemporalField field) {
        if (!(field instanceof ChronoField)) {
            return field.getFrom(this);
        }
        switch ((ChronoField) field) {
            case DAY_OF_YEAR:
                Calendar jcal = JapaneseChronology.createCalendar();
                jcal.set(0, this.era.getValue() + 2);
                jcal.set(this.yearOfEra, this.isoDate.getMonthValue() - 1, this.isoDate.getDayOfMonth());
                return (long) jcal.get(6);
            case YEAR_OF_ERA:
                return (long) this.yearOfEra;
            case ALIGNED_DAY_OF_WEEK_IN_MONTH:
            case ALIGNED_DAY_OF_WEEK_IN_YEAR:
            case ALIGNED_WEEK_OF_MONTH:
            case ALIGNED_WEEK_OF_YEAR:
                throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
            case ERA:
                return (long) this.era.getValue();
            default:
                return this.isoDate.getLong(field);
        }
    }

    private static LocalGregorianCalendar.Date toPrivateJapaneseDate(LocalDate isoDate2) {
        LocalGregorianCalendar.Date jdate = JapaneseChronology.JCAL.newCalendarDate((TimeZone) null);
        Era sunEra = JapaneseEra.privateEraFrom(isoDate2);
        int year = isoDate2.getYear();
        if (sunEra != null) {
            year -= sunEra.getSinceDate().getYear() - 1;
        }
        jdate.setEra(sunEra).setYear(year).setMonth(isoDate2.getMonthValue()).setDayOfMonth(isoDate2.getDayOfMonth());
        JapaneseChronology.JCAL.normalize(jdate);
        return jdate;
    }

    public JapaneseDate with(TemporalField field, long newValue) {
        if (!(field instanceof ChronoField)) {
            return (JapaneseDate) super.with(field, newValue);
        }
        ChronoField f = (ChronoField) field;
        if (getLong(f) == newValue) {
            return this;
        }
        int i = AnonymousClass1.$SwitchMap$java$time$temporal$ChronoField[f.ordinal()];
        if (i != 3) {
            switch (i) {
                case 8:
                case 9:
                    break;
            }
        }
        int nvalue = getChronology().range(f).checkValidIntValue(newValue, f);
        int i2 = AnonymousClass1.$SwitchMap$java$time$temporal$ChronoField[f.ordinal()];
        if (i2 == 3) {
            return withYear(nvalue);
        }
        switch (i2) {
            case 8:
                return withYear(JapaneseEra.of(nvalue), this.yearOfEra);
            case 9:
                return with(this.isoDate.withYear(nvalue));
        }
        return with(this.isoDate.with(field, newValue));
    }

    public JapaneseDate with(TemporalAdjuster adjuster) {
        return (JapaneseDate) super.with(adjuster);
    }

    public JapaneseDate plus(TemporalAmount amount) {
        return (JapaneseDate) super.plus(amount);
    }

    public JapaneseDate minus(TemporalAmount amount) {
        return (JapaneseDate) super.minus(amount);
    }

    private JapaneseDate withYear(JapaneseEra era2, int yearOfEra2) {
        return with(this.isoDate.withYear(JapaneseChronology.INSTANCE.prolepticYear(era2, yearOfEra2)));
    }

    private JapaneseDate withYear(int year) {
        return withYear(getEra(), year);
    }

    /* access modifiers changed from: package-private */
    public JapaneseDate plusYears(long years) {
        return with(this.isoDate.plusYears(years));
    }

    /* access modifiers changed from: package-private */
    public JapaneseDate plusMonths(long months) {
        return with(this.isoDate.plusMonths(months));
    }

    /* access modifiers changed from: package-private */
    public JapaneseDate plusWeeks(long weeksToAdd) {
        return with(this.isoDate.plusWeeks(weeksToAdd));
    }

    /* access modifiers changed from: package-private */
    public JapaneseDate plusDays(long days) {
        return with(this.isoDate.plusDays(days));
    }

    public JapaneseDate plus(long amountToAdd, TemporalUnit unit) {
        return (JapaneseDate) super.plus(amountToAdd, unit);
    }

    public JapaneseDate minus(long amountToAdd, TemporalUnit unit) {
        return (JapaneseDate) super.minus(amountToAdd, unit);
    }

    /* access modifiers changed from: package-private */
    public JapaneseDate minusYears(long yearsToSubtract) {
        return (JapaneseDate) super.minusYears(yearsToSubtract);
    }

    /* access modifiers changed from: package-private */
    public JapaneseDate minusMonths(long monthsToSubtract) {
        return (JapaneseDate) super.minusMonths(monthsToSubtract);
    }

    /* access modifiers changed from: package-private */
    public JapaneseDate minusWeeks(long weeksToSubtract) {
        return (JapaneseDate) super.minusWeeks(weeksToSubtract);
    }

    /* access modifiers changed from: package-private */
    public JapaneseDate minusDays(long daysToSubtract) {
        return (JapaneseDate) super.minusDays(daysToSubtract);
    }

    private JapaneseDate with(LocalDate newDate) {
        return newDate.equals(this.isoDate) ? this : new JapaneseDate(newDate);
    }

    public final ChronoLocalDateTime<JapaneseDate> atTime(LocalTime localTime) {
        return super.atTime(localTime);
    }

    public ChronoPeriod until(ChronoLocalDate endDate) {
        Period period = this.isoDate.until(endDate);
        return getChronology().period(period.getYears(), period.getMonths(), period.getDays());
    }

    public long toEpochDay() {
        return this.isoDate.toEpochDay();
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof JapaneseDate) {
            return this.isoDate.equals(((JapaneseDate) obj).isoDate);
        }
        return false;
    }

    public int hashCode() {
        return getChronology().getId().hashCode() ^ this.isoDate.hashCode();
    }

    private void readObject(ObjectInputStream s) throws InvalidObjectException {
        throw new InvalidObjectException("Deserialization via serialization delegate");
    }

    private Object writeReplace() {
        return new Ser((byte) 4, this);
    }

    /* access modifiers changed from: package-private */
    public void writeExternal(DataOutput out) throws IOException {
        out.writeInt(get(ChronoField.YEAR));
        out.writeByte(get(ChronoField.MONTH_OF_YEAR));
        out.writeByte(get(ChronoField.DAY_OF_MONTH));
    }

    static JapaneseDate readExternal(DataInput in) throws IOException {
        return JapaneseChronology.INSTANCE.date(in.readInt(), in.readByte(), in.readByte());
    }
}
