package java.time;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.time.chrono.ChronoLocalDate;
import java.time.chrono.Era;
import java.time.chrono.IsoChronology;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalQueries;
import java.time.temporal.TemporalQuery;
import java.time.temporal.TemporalUnit;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.time.temporal.ValueRange;
import java.time.zone.ZoneOffsetTransition;
import java.util.Objects;
import sun.util.locale.LanguageTag;

public final class LocalDate implements Temporal, TemporalAdjuster, ChronoLocalDate, Serializable {
    static final long DAYS_0000_TO_1970 = 719528;
    private static final int DAYS_PER_CYCLE = 146097;
    public static final LocalDate MAX = of((int) Year.MAX_VALUE, 12, 31);
    public static final LocalDate MIN = of((int) Year.MIN_VALUE, 1, 1);
    private static final long serialVersionUID = 2942565459149668126L;
    private final short day;
    private final short month;
    private final int year;

    public static LocalDate now() {
        return now(Clock.systemDefaultZone());
    }

    public static LocalDate now(ZoneId zone) {
        return now(Clock.system(zone));
    }

    public static LocalDate now(Clock clock) {
        Objects.requireNonNull(clock, "clock");
        Instant now = clock.instant();
        return ofEpochDay(Math.floorDiv(now.getEpochSecond() + ((long) clock.getZone().getRules().getOffset(now).getTotalSeconds()), 86400));
    }

    public static LocalDate of(int year2, Month month2, int dayOfMonth) {
        ChronoField.YEAR.checkValidValue((long) year2);
        Objects.requireNonNull(month2, "month");
        ChronoField.DAY_OF_MONTH.checkValidValue((long) dayOfMonth);
        return create(year2, month2.getValue(), dayOfMonth);
    }

    public static LocalDate of(int year2, int month2, int dayOfMonth) {
        ChronoField.YEAR.checkValidValue((long) year2);
        ChronoField.MONTH_OF_YEAR.checkValidValue((long) month2);
        ChronoField.DAY_OF_MONTH.checkValidValue((long) dayOfMonth);
        return create(year2, month2, dayOfMonth);
    }

    public static LocalDate ofYearDay(int year2, int dayOfYear) {
        ChronoField.YEAR.checkValidValue((long) year2);
        ChronoField.DAY_OF_YEAR.checkValidValue((long) dayOfYear);
        boolean leap = IsoChronology.INSTANCE.isLeapYear((long) year2);
        if (dayOfYear != 366 || leap) {
            Month moy = Month.of(((dayOfYear - 1) / 31) + 1);
            if (dayOfYear > (moy.firstDayOfYear(leap) + moy.length(leap)) - 1) {
                moy = moy.plus(1);
            }
            return new LocalDate(year2, moy.getValue(), (dayOfYear - moy.firstDayOfYear(leap)) + 1);
        }
        throw new DateTimeException("Invalid date 'DayOfYear 366' as '" + year2 + "' is not a leap year");
    }

    public static LocalDate ofEpochDay(long epochDay) {
        long zeroDay = (epochDay + DAYS_0000_TO_1970) - 60;
        long adjust = 0;
        if (zeroDay < 0) {
            long adjustCycles = ((zeroDay + 1) / 146097) - 1;
            adjust = adjustCycles * 400;
            zeroDay += (-adjustCycles) * 146097;
        }
        long yearEst = ((400 * zeroDay) + 591) / 146097;
        long doyEst = zeroDay - ((((365 * yearEst) + (yearEst / 4)) - (yearEst / 100)) + (yearEst / 400));
        if (doyEst < 0) {
            yearEst--;
            doyEst = zeroDay - ((((365 * yearEst) + (yearEst / 4)) - (yearEst / 100)) + (yearEst / 400));
        }
        int marchDoy0 = (int) doyEst;
        int marchMonth0 = ((marchDoy0 * 5) + 2) / 153;
        return new LocalDate(ChronoField.YEAR.checkValidIntValue(yearEst + adjust + ((long) (marchMonth0 / 10))), ((marchMonth0 + 2) % 12) + 1, (marchDoy0 - (((marchMonth0 * 306) + 5) / 10)) + 1);
    }

    public static LocalDate from(TemporalAccessor temporal) {
        Objects.requireNonNull(temporal, "temporal");
        LocalDate date = (LocalDate) temporal.query(TemporalQueries.localDate());
        if (date != null) {
            return date;
        }
        throw new DateTimeException("Unable to obtain LocalDate from TemporalAccessor: " + temporal + " of type " + temporal.getClass().getName());
    }

    public static LocalDate parse(CharSequence text) {
        return parse(text, DateTimeFormatter.ISO_LOCAL_DATE);
    }

    public static LocalDate parse(CharSequence text, DateTimeFormatter formatter) {
        Objects.requireNonNull(formatter, "formatter");
        return (LocalDate) formatter.parse(text, $$Lambda$Bq8PKq1YWr8nyVk9SSfRYKrOu4A.INSTANCE);
    }

    private static LocalDate create(int year2, int month2, int dayOfMonth) {
        int i = 28;
        if (dayOfMonth > 28) {
            int dom = 31;
            if (month2 == 2) {
                if (IsoChronology.INSTANCE.isLeapYear((long) year2)) {
                    i = 29;
                }
                dom = i;
            } else if (month2 == 4 || month2 == 6 || month2 == 9 || month2 == 11) {
                dom = 30;
            }
            if (dayOfMonth > dom) {
                if (dayOfMonth == 29) {
                    throw new DateTimeException("Invalid date 'February 29' as '" + year2 + "' is not a leap year");
                }
                throw new DateTimeException("Invalid date '" + Month.of(month2).name() + " " + dayOfMonth + "'");
            }
        }
        return new LocalDate(year2, month2, dayOfMonth);
    }

    private static LocalDate resolvePreviousValid(int year2, int month2, int day2) {
        if (month2 == 2) {
            day2 = Math.min(day2, IsoChronology.INSTANCE.isLeapYear((long) year2) ? 29 : 28);
        } else if (month2 == 4 || month2 == 6 || month2 == 9 || month2 == 11) {
            day2 = Math.min(day2, 30);
        }
        return new LocalDate(year2, month2, day2);
    }

    private LocalDate(int year2, int month2, int dayOfMonth) {
        this.year = year2;
        this.month = (short) month2;
        this.day = (short) dayOfMonth;
    }

    public boolean isSupported(TemporalField field) {
        return super.isSupported(field);
    }

    public boolean isSupported(TemporalUnit unit) {
        return super.isSupported(unit);
    }

    public ValueRange range(TemporalField field) {
        if (!(field instanceof ChronoField)) {
            return field.rangeRefinedBy(this);
        }
        ChronoField f = (ChronoField) field;
        if (f.isDateBased()) {
            switch (f) {
                case DAY_OF_MONTH:
                    return ValueRange.of(1, (long) lengthOfMonth());
                case DAY_OF_YEAR:
                    return ValueRange.of(1, (long) lengthOfYear());
                case ALIGNED_WEEK_OF_MONTH:
                    return ValueRange.of(1, (getMonth() != Month.FEBRUARY || isLeapYear()) ? 5 : 4);
                case YEAR_OF_ERA:
                    return ValueRange.of(1, getYear() <= 0 ? 1000000000 : 999999999);
                default:
                    return field.range();
            }
        } else {
            throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
        }
    }

    public int get(TemporalField field) {
        if (field instanceof ChronoField) {
            return get0(field);
        }
        return super.get(field);
    }

    public long getLong(TemporalField field) {
        if (!(field instanceof ChronoField)) {
            return field.getFrom(this);
        }
        if (field == ChronoField.EPOCH_DAY) {
            return toEpochDay();
        }
        if (field == ChronoField.PROLEPTIC_MONTH) {
            return getProlepticMonth();
        }
        return (long) get0(field);
    }

    private int get0(TemporalField field) {
        int i = 1;
        switch ((ChronoField) field) {
            case DAY_OF_MONTH:
                return this.day;
            case DAY_OF_YEAR:
                return getDayOfYear();
            case ALIGNED_WEEK_OF_MONTH:
                return ((this.day - 1) / 7) + 1;
            case YEAR_OF_ERA:
                return this.year >= 1 ? this.year : 1 - this.year;
            case DAY_OF_WEEK:
                return getDayOfWeek().getValue();
            case ALIGNED_DAY_OF_WEEK_IN_MONTH:
                return ((this.day - 1) % 7) + 1;
            case ALIGNED_DAY_OF_WEEK_IN_YEAR:
                return ((getDayOfYear() - 1) % 7) + 1;
            case EPOCH_DAY:
                throw new UnsupportedTemporalTypeException("Invalid field 'EpochDay' for get() method, use getLong() instead");
            case ALIGNED_WEEK_OF_YEAR:
                return ((getDayOfYear() - 1) / 7) + 1;
            case MONTH_OF_YEAR:
                return this.month;
            case PROLEPTIC_MONTH:
                throw new UnsupportedTemporalTypeException("Invalid field 'ProlepticMonth' for get() method, use getLong() instead");
            case YEAR:
                return this.year;
            case ERA:
                if (this.year < 1) {
                    i = 0;
                }
                return i;
            default:
                throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
        }
    }

    private long getProlepticMonth() {
        return ((((long) this.year) * 12) + ((long) this.month)) - 1;
    }

    public IsoChronology getChronology() {
        return IsoChronology.INSTANCE;
    }

    public Era getEra() {
        return super.getEra();
    }

    public int getYear() {
        return this.year;
    }

    public int getMonthValue() {
        return this.month;
    }

    public Month getMonth() {
        return Month.of(this.month);
    }

    public int getDayOfMonth() {
        return this.day;
    }

    public int getDayOfYear() {
        return (getMonth().firstDayOfYear(isLeapYear()) + this.day) - 1;
    }

    public DayOfWeek getDayOfWeek() {
        return DayOfWeek.of(((int) Math.floorMod(toEpochDay() + 3, 7)) + 1);
    }

    public boolean isLeapYear() {
        return IsoChronology.INSTANCE.isLeapYear((long) this.year);
    }

    public int lengthOfMonth() {
        short s = this.month;
        if (s == 2) {
            return isLeapYear() ? 29 : 28;
        } else if (s == 4 || s == 6 || s == 9 || s == 11) {
            return 30;
        } else {
            return 31;
        }
    }

    public int lengthOfYear() {
        return isLeapYear() ? 366 : 365;
    }

    public LocalDate with(TemporalAdjuster adjuster) {
        if (adjuster instanceof LocalDate) {
            return (LocalDate) adjuster;
        }
        return (LocalDate) adjuster.adjustInto(this);
    }

    public LocalDate with(TemporalField field, long newValue) {
        if (!(field instanceof ChronoField)) {
            return (LocalDate) field.adjustInto(this, newValue);
        }
        ChronoField f = (ChronoField) field;
        f.checkValidValue(newValue);
        switch (f) {
            case DAY_OF_MONTH:
                return withDayOfMonth((int) newValue);
            case DAY_OF_YEAR:
                return withDayOfYear((int) newValue);
            case ALIGNED_WEEK_OF_MONTH:
                return plusWeeks(newValue - getLong(ChronoField.ALIGNED_WEEK_OF_MONTH));
            case YEAR_OF_ERA:
                return withYear((int) (this.year >= 1 ? newValue : 1 - newValue));
            case DAY_OF_WEEK:
                return plusDays(newValue - ((long) getDayOfWeek().getValue()));
            case ALIGNED_DAY_OF_WEEK_IN_MONTH:
                return plusDays(newValue - getLong(ChronoField.ALIGNED_DAY_OF_WEEK_IN_MONTH));
            case ALIGNED_DAY_OF_WEEK_IN_YEAR:
                return plusDays(newValue - getLong(ChronoField.ALIGNED_DAY_OF_WEEK_IN_YEAR));
            case EPOCH_DAY:
                return ofEpochDay(newValue);
            case ALIGNED_WEEK_OF_YEAR:
                return plusWeeks(newValue - getLong(ChronoField.ALIGNED_WEEK_OF_YEAR));
            case MONTH_OF_YEAR:
                return withMonth((int) newValue);
            case PROLEPTIC_MONTH:
                return plusMonths(newValue - getProlepticMonth());
            case YEAR:
                return withYear((int) newValue);
            case ERA:
                return getLong(ChronoField.ERA) == newValue ? this : withYear(1 - this.year);
            default:
                throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
        }
    }

    public LocalDate withYear(int year2) {
        if (this.year == year2) {
            return this;
        }
        ChronoField.YEAR.checkValidValue((long) year2);
        return resolvePreviousValid(year2, this.month, this.day);
    }

    public LocalDate withMonth(int month2) {
        if (this.month == month2) {
            return this;
        }
        ChronoField.MONTH_OF_YEAR.checkValidValue((long) month2);
        return resolvePreviousValid(this.year, month2, this.day);
    }

    public LocalDate withDayOfMonth(int dayOfMonth) {
        if (this.day == dayOfMonth) {
            return this;
        }
        return of(this.year, (int) this.month, dayOfMonth);
    }

    public LocalDate withDayOfYear(int dayOfYear) {
        if (getDayOfYear() == dayOfYear) {
            return this;
        }
        return ofYearDay(this.year, dayOfYear);
    }

    public LocalDate plus(TemporalAmount amountToAdd) {
        if (amountToAdd instanceof Period) {
            Period periodToAdd = (Period) amountToAdd;
            return plusMonths(periodToAdd.toTotalMonths()).plusDays((long) periodToAdd.getDays());
        }
        Objects.requireNonNull(amountToAdd, "amountToAdd");
        return (LocalDate) amountToAdd.addTo(this);
    }

    public LocalDate plus(long amountToAdd, TemporalUnit unit) {
        if (!(unit instanceof ChronoUnit)) {
            return (LocalDate) unit.addTo(this, amountToAdd);
        }
        switch ((ChronoUnit) unit) {
            case DAYS:
                return plusDays(amountToAdd);
            case WEEKS:
                return plusWeeks(amountToAdd);
            case MONTHS:
                return plusMonths(amountToAdd);
            case YEARS:
                return plusYears(amountToAdd);
            case DECADES:
                return plusYears(Math.multiplyExact(amountToAdd, 10));
            case CENTURIES:
                return plusYears(Math.multiplyExact(amountToAdd, 100));
            case MILLENNIA:
                return plusYears(Math.multiplyExact(amountToAdd, 1000));
            case ERAS:
                return with((TemporalField) ChronoField.ERA, Math.addExact(getLong(ChronoField.ERA), amountToAdd));
            default:
                throw new UnsupportedTemporalTypeException("Unsupported unit: " + unit);
        }
    }

    public LocalDate plusYears(long yearsToAdd) {
        if (yearsToAdd == 0) {
            return this;
        }
        return resolvePreviousValid(ChronoField.YEAR.checkValidIntValue(((long) this.year) + yearsToAdd), this.month, this.day);
    }

    public LocalDate plusMonths(long monthsToAdd) {
        if (monthsToAdd == 0) {
            return this;
        }
        long calcMonths = (((long) this.year) * 12) + ((long) (this.month - 1)) + monthsToAdd;
        return resolvePreviousValid(ChronoField.YEAR.checkValidIntValue(Math.floorDiv(calcMonths, 12)), ((int) Math.floorMod(calcMonths, 12)) + 1, this.day);
    }

    public LocalDate plusWeeks(long weeksToAdd) {
        return plusDays(Math.multiplyExact(weeksToAdd, 7));
    }

    public LocalDate plusDays(long daysToAdd) {
        if (daysToAdd == 0) {
            return this;
        }
        return ofEpochDay(Math.addExact(toEpochDay(), daysToAdd));
    }

    public LocalDate minus(TemporalAmount amountToSubtract) {
        if (amountToSubtract instanceof Period) {
            Period periodToSubtract = (Period) amountToSubtract;
            return minusMonths(periodToSubtract.toTotalMonths()).minusDays((long) periodToSubtract.getDays());
        }
        Objects.requireNonNull(amountToSubtract, "amountToSubtract");
        return (LocalDate) amountToSubtract.subtractFrom(this);
    }

    public LocalDate minus(long amountToSubtract, TemporalUnit unit) {
        return amountToSubtract == Long.MIN_VALUE ? plus((long) Long.MAX_VALUE, unit).plus(1, unit) : plus(-amountToSubtract, unit);
    }

    public LocalDate minusYears(long yearsToSubtract) {
        return yearsToSubtract == Long.MIN_VALUE ? plusYears(Long.MAX_VALUE).plusYears(1) : plusYears(-yearsToSubtract);
    }

    public LocalDate minusMonths(long monthsToSubtract) {
        return monthsToSubtract == Long.MIN_VALUE ? plusMonths(Long.MAX_VALUE).plusMonths(1) : plusMonths(-monthsToSubtract);
    }

    public LocalDate minusWeeks(long weeksToSubtract) {
        return weeksToSubtract == Long.MIN_VALUE ? plusWeeks(Long.MAX_VALUE).plusWeeks(1) : plusWeeks(-weeksToSubtract);
    }

    public LocalDate minusDays(long daysToSubtract) {
        return daysToSubtract == Long.MIN_VALUE ? plusDays(Long.MAX_VALUE).plusDays(1) : plusDays(-daysToSubtract);
    }

    public <R> R query(TemporalQuery<R> query) {
        if (query == TemporalQueries.localDate()) {
            return this;
        }
        return super.query(query);
    }

    public Temporal adjustInto(Temporal temporal) {
        return super.adjustInto(temporal);
    }

    public long until(Temporal endExclusive, TemporalUnit unit) {
        LocalDate end = from(endExclusive);
        if (!(unit instanceof ChronoUnit)) {
            return unit.between(this, end);
        }
        switch ((ChronoUnit) unit) {
            case DAYS:
                return daysUntil(end);
            case WEEKS:
                return daysUntil(end) / 7;
            case MONTHS:
                return monthsUntil(end);
            case YEARS:
                return monthsUntil(end) / 12;
            case DECADES:
                return monthsUntil(end) / 120;
            case CENTURIES:
                return monthsUntil(end) / 1200;
            case MILLENNIA:
                return monthsUntil(end) / 12000;
            case ERAS:
                return end.getLong(ChronoField.ERA) - getLong(ChronoField.ERA);
            default:
                throw new UnsupportedTemporalTypeException("Unsupported unit: " + unit);
        }
    }

    /* access modifiers changed from: package-private */
    public long daysUntil(LocalDate end) {
        return end.toEpochDay() - toEpochDay();
    }

    private long monthsUntil(LocalDate end) {
        return (((end.getProlepticMonth() * 32) + ((long) end.getDayOfMonth())) - ((getProlepticMonth() * 32) + ((long) getDayOfMonth()))) / 32;
    }

    public Period until(ChronoLocalDate endDateExclusive) {
        LocalDate end = from(endDateExclusive);
        long totalMonths = end.getProlepticMonth() - getProlepticMonth();
        int days = end.day - this.day;
        if (totalMonths > 0 && days < 0) {
            totalMonths--;
            days = (int) (end.toEpochDay() - plusMonths(totalMonths).toEpochDay());
        } else if (totalMonths < 0 && days > 0) {
            totalMonths++;
            days -= end.lengthOfMonth();
        }
        return Period.of(Math.toIntExact(totalMonths / 12), (int) (totalMonths % 12), days);
    }

    public String format(DateTimeFormatter formatter) {
        Objects.requireNonNull(formatter, "formatter");
        return formatter.format(this);
    }

    public LocalDateTime atTime(LocalTime time) {
        return LocalDateTime.of(this, time);
    }

    public LocalDateTime atTime(int hour, int minute) {
        return atTime(LocalTime.of(hour, minute));
    }

    public LocalDateTime atTime(int hour, int minute, int second) {
        return atTime(LocalTime.of(hour, minute, second));
    }

    public LocalDateTime atTime(int hour, int minute, int second, int nanoOfSecond) {
        return atTime(LocalTime.of(hour, minute, second, nanoOfSecond));
    }

    public OffsetDateTime atTime(OffsetTime time) {
        return OffsetDateTime.of(LocalDateTime.of(this, time.toLocalTime()), time.getOffset());
    }

    public LocalDateTime atStartOfDay() {
        return LocalDateTime.of(this, LocalTime.MIDNIGHT);
    }

    public ZonedDateTime atStartOfDay(ZoneId zone) {
        Objects.requireNonNull(zone, "zone");
        LocalDateTime ldt = atTime(LocalTime.MIDNIGHT);
        if (!(zone instanceof ZoneOffset)) {
            ZoneOffsetTransition trans = zone.getRules().getTransition(ldt);
            if (trans != null && trans.isGap()) {
                ldt = trans.getDateTimeAfter();
            }
        }
        return ZonedDateTime.of(ldt, zone);
    }

    public long toEpochDay() {
        long total;
        long y = (long) this.year;
        long m = (long) this.month;
        long total2 = 0 + (365 * y);
        if (y >= 0) {
            total = total2 + (((3 + y) / 4) - ((99 + y) / 100)) + ((399 + y) / 400);
        } else {
            total = total2 - (((y / -4) - (y / -100)) + (y / -400));
        }
        long total3 = total + (((367 * m) - 362) / 12) + ((long) (this.day - 1));
        if (m > 2) {
            total3--;
            if (!isLeapYear()) {
                total3--;
            }
        }
        return total3 - DAYS_0000_TO_1970;
    }

    public int compareTo(ChronoLocalDate other) {
        if (other instanceof LocalDate) {
            return compareTo0((LocalDate) other);
        }
        return super.compareTo(other);
    }

    /* access modifiers changed from: package-private */
    public int compareTo0(LocalDate otherDate) {
        int cmp = this.year - otherDate.year;
        if (cmp != 0) {
            return cmp;
        }
        int cmp2 = this.month - otherDate.month;
        if (cmp2 == 0) {
            return this.day - otherDate.day;
        }
        return cmp2;
    }

    public boolean isAfter(ChronoLocalDate other) {
        if (!(other instanceof LocalDate)) {
            return super.isAfter(other);
        }
        return compareTo0((LocalDate) other) > 0;
    }

    public boolean isBefore(ChronoLocalDate other) {
        if (!(other instanceof LocalDate)) {
            return super.isBefore(other);
        }
        return compareTo0((LocalDate) other) < 0;
    }

    public boolean isEqual(ChronoLocalDate other) {
        if (!(other instanceof LocalDate)) {
            return super.isEqual(other);
        }
        return compareTo0((LocalDate) other) == 0;
    }

    public boolean equals(Object obj) {
        boolean z = true;
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof LocalDate)) {
            return false;
        }
        if (compareTo0((LocalDate) obj) != 0) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        int yearValue = this.year;
        return (yearValue & -2048) ^ (((yearValue << 11) + (this.month << 6)) + this.day);
    }

    public String toString() {
        int yearValue = this.year;
        int monthValue = this.month;
        int dayValue = this.day;
        int absYear = Math.abs(yearValue);
        StringBuilder buf = new StringBuilder(10);
        if (absYear >= 1000) {
            if (yearValue > 9999) {
                buf.append('+');
            }
            buf.append(yearValue);
        } else if (yearValue < 0) {
            buf.append(yearValue - 10000);
            buf.deleteCharAt(1);
        } else {
            buf.append(yearValue + 10000);
            buf.deleteCharAt(0);
        }
        buf.append(monthValue < 10 ? "-0" : LanguageTag.SEP);
        buf.append(monthValue);
        buf.append(dayValue < 10 ? "-0" : LanguageTag.SEP);
        buf.append(dayValue);
        return buf.toString();
    }

    private Object writeReplace() {
        return new Ser((byte) 3, this);
    }

    private void readObject(ObjectInputStream s) throws InvalidObjectException {
        throw new InvalidObjectException("Deserialization via serialization delegate");
    }

    /* access modifiers changed from: package-private */
    public void writeExternal(DataOutput out) throws IOException {
        out.writeInt(this.year);
        out.writeByte(this.month);
        out.writeByte(this.day);
    }

    static LocalDate readExternal(DataInput in) throws IOException {
        return of(in.readInt(), in.readByte(), in.readByte());
    }
}
