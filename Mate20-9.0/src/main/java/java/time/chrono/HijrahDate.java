package java.time.chrono;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
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

public final class HijrahDate extends ChronoLocalDateImpl<HijrahDate> implements ChronoLocalDate, Serializable {
    private static final long serialVersionUID = -5207853542612002020L;
    private final transient HijrahChronology chrono;
    private final transient int dayOfMonth;
    private final transient int monthOfYear;
    private final transient int prolepticYear;

    public /* bridge */ /* synthetic */ String toString() {
        return super.toString();
    }

    public /* bridge */ /* synthetic */ long until(Temporal temporal, TemporalUnit temporalUnit) {
        return super.until(temporal, temporalUnit);
    }

    static HijrahDate of(HijrahChronology chrono2, int prolepticYear2, int monthOfYear2, int dayOfMonth2) {
        return new HijrahDate(chrono2, prolepticYear2, monthOfYear2, dayOfMonth2);
    }

    static HijrahDate ofEpochDay(HijrahChronology chrono2, long epochDay) {
        return new HijrahDate(chrono2, epochDay);
    }

    public static HijrahDate now() {
        return now(Clock.systemDefaultZone());
    }

    public static HijrahDate now(ZoneId zone) {
        return now(Clock.system(zone));
    }

    public static HijrahDate now(Clock clock) {
        return ofEpochDay(HijrahChronology.INSTANCE, LocalDate.now(clock).toEpochDay());
    }

    public static HijrahDate of(int prolepticYear2, int month, int dayOfMonth2) {
        return HijrahChronology.INSTANCE.date(prolepticYear2, month, dayOfMonth2);
    }

    public static HijrahDate from(TemporalAccessor temporal) {
        return HijrahChronology.INSTANCE.date(temporal);
    }

    private HijrahDate(HijrahChronology chrono2, int prolepticYear2, int monthOfYear2, int dayOfMonth2) {
        chrono2.getEpochDay(prolepticYear2, monthOfYear2, dayOfMonth2);
        this.chrono = chrono2;
        this.prolepticYear = prolepticYear2;
        this.monthOfYear = monthOfYear2;
        this.dayOfMonth = dayOfMonth2;
    }

    private HijrahDate(HijrahChronology chrono2, long epochDay) {
        int[] dateInfo = chrono2.getHijrahDateInfo((int) epochDay);
        this.chrono = chrono2;
        this.prolepticYear = dateInfo[0];
        this.monthOfYear = dateInfo[1];
        this.dayOfMonth = dateInfo[2];
    }

    public HijrahChronology getChronology() {
        return this.chrono;
    }

    public HijrahEra getEra() {
        return HijrahEra.AH;
    }

    public int lengthOfMonth() {
        return this.chrono.getMonthLength(this.prolepticYear, this.monthOfYear);
    }

    public int lengthOfYear() {
        return this.chrono.getYearLength(this.prolepticYear);
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
                case ALIGNED_WEEK_OF_MONTH:
                    return ValueRange.of(1, 5);
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
            case DAY_OF_MONTH:
                return (long) this.dayOfMonth;
            case DAY_OF_YEAR:
                return (long) getDayOfYear();
            case ALIGNED_WEEK_OF_MONTH:
                return (long) (((this.dayOfMonth - 1) / 7) + 1);
            case DAY_OF_WEEK:
                return (long) getDayOfWeek();
            case ALIGNED_DAY_OF_WEEK_IN_MONTH:
                return (long) (((getDayOfWeek() - 1) % 7) + 1);
            case ALIGNED_DAY_OF_WEEK_IN_YEAR:
                return (long) (((getDayOfYear() - 1) % 7) + 1);
            case EPOCH_DAY:
                return toEpochDay();
            case ALIGNED_WEEK_OF_YEAR:
                return (long) (((getDayOfYear() - 1) / 7) + 1);
            case MONTH_OF_YEAR:
                return (long) this.monthOfYear;
            case PROLEPTIC_MONTH:
                return getProlepticMonth();
            case YEAR_OF_ERA:
                return (long) this.prolepticYear;
            case YEAR:
                return (long) this.prolepticYear;
            case ERA:
                return (long) getEraValue();
            default:
                throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
        }
    }

    private long getProlepticMonth() {
        return ((((long) this.prolepticYear) * 12) + ((long) this.monthOfYear)) - 1;
    }

    public HijrahDate with(TemporalField field, long newValue) {
        if (!(field instanceof ChronoField)) {
            return (HijrahDate) super.with(field, newValue);
        }
        ChronoField f = (ChronoField) field;
        this.chrono.range(f).checkValidValue(newValue, f);
        int nvalue = (int) newValue;
        switch (f) {
            case DAY_OF_MONTH:
                return resolvePreviousValid(this.prolepticYear, this.monthOfYear, nvalue);
            case DAY_OF_YEAR:
                return plusDays((long) (Math.min(nvalue, lengthOfYear()) - getDayOfYear()));
            case ALIGNED_WEEK_OF_MONTH:
                return plusDays((newValue - getLong(ChronoField.ALIGNED_WEEK_OF_MONTH)) * 7);
            case DAY_OF_WEEK:
                return plusDays(newValue - ((long) getDayOfWeek()));
            case ALIGNED_DAY_OF_WEEK_IN_MONTH:
                return plusDays(newValue - getLong(ChronoField.ALIGNED_DAY_OF_WEEK_IN_MONTH));
            case ALIGNED_DAY_OF_WEEK_IN_YEAR:
                return plusDays(newValue - getLong(ChronoField.ALIGNED_DAY_OF_WEEK_IN_YEAR));
            case EPOCH_DAY:
                return new HijrahDate(this.chrono, newValue);
            case ALIGNED_WEEK_OF_YEAR:
                return plusDays((newValue - getLong(ChronoField.ALIGNED_WEEK_OF_YEAR)) * 7);
            case MONTH_OF_YEAR:
                return resolvePreviousValid(this.prolepticYear, nvalue, this.dayOfMonth);
            case PROLEPTIC_MONTH:
                return plusMonths(newValue - getProlepticMonth());
            case YEAR_OF_ERA:
                return resolvePreviousValid(this.prolepticYear >= 1 ? nvalue : 1 - nvalue, this.monthOfYear, this.dayOfMonth);
            case YEAR:
                return resolvePreviousValid(nvalue, this.monthOfYear, this.dayOfMonth);
            case ERA:
                return resolvePreviousValid(1 - this.prolepticYear, this.monthOfYear, this.dayOfMonth);
            default:
                throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
        }
    }

    private HijrahDate resolvePreviousValid(int prolepticYear2, int month, int day) {
        int monthDays = this.chrono.getMonthLength(prolepticYear2, month);
        if (day > monthDays) {
            day = monthDays;
        }
        return of(this.chrono, prolepticYear2, month, day);
    }

    public HijrahDate with(TemporalAdjuster adjuster) {
        return (HijrahDate) super.with(adjuster);
    }

    public HijrahDate withVariant(HijrahChronology chronology) {
        if (this.chrono == chronology) {
            return this;
        }
        int monthDays = chronology.getDayOfYear(this.prolepticYear, this.monthOfYear);
        return of(chronology, this.prolepticYear, this.monthOfYear, this.dayOfMonth > monthDays ? monthDays : this.dayOfMonth);
    }

    public HijrahDate plus(TemporalAmount amount) {
        return (HijrahDate) super.plus(amount);
    }

    public HijrahDate minus(TemporalAmount amount) {
        return (HijrahDate) super.minus(amount);
    }

    public long toEpochDay() {
        return this.chrono.getEpochDay(this.prolepticYear, this.monthOfYear, this.dayOfMonth);
    }

    private int getDayOfYear() {
        return this.chrono.getDayOfYear(this.prolepticYear, this.monthOfYear) + this.dayOfMonth;
    }

    private int getDayOfWeek() {
        return ((int) Math.floorMod(toEpochDay() + 3, 7)) + 1;
    }

    private int getEraValue() {
        return this.prolepticYear > 1 ? 1 : 0;
    }

    public boolean isLeapYear() {
        return this.chrono.isLeapYear((long) this.prolepticYear);
    }

    /* access modifiers changed from: package-private */
    public HijrahDate plusYears(long years) {
        if (years == 0) {
            return this;
        }
        return resolvePreviousValid(Math.addExact(this.prolepticYear, (int) years), this.monthOfYear, this.dayOfMonth);
    }

    /* access modifiers changed from: package-private */
    public HijrahDate plusMonths(long monthsToAdd) {
        if (monthsToAdd == 0) {
            return this;
        }
        long calcMonths = (((long) this.prolepticYear) * 12) + ((long) (this.monthOfYear - 1)) + monthsToAdd;
        return resolvePreviousValid(this.chrono.checkValidYear(Math.floorDiv(calcMonths, 12)), ((int) Math.floorMod(calcMonths, 12)) + 1, this.dayOfMonth);
    }

    /* access modifiers changed from: package-private */
    public HijrahDate plusWeeks(long weeksToAdd) {
        return (HijrahDate) super.plusWeeks(weeksToAdd);
    }

    /* access modifiers changed from: package-private */
    public HijrahDate plusDays(long days) {
        return new HijrahDate(this.chrono, toEpochDay() + days);
    }

    public HijrahDate plus(long amountToAdd, TemporalUnit unit) {
        return (HijrahDate) super.plus(amountToAdd, unit);
    }

    public HijrahDate minus(long amountToSubtract, TemporalUnit unit) {
        return (HijrahDate) super.minus(amountToSubtract, unit);
    }

    /* access modifiers changed from: package-private */
    public HijrahDate minusYears(long yearsToSubtract) {
        return (HijrahDate) super.minusYears(yearsToSubtract);
    }

    /* access modifiers changed from: package-private */
    public HijrahDate minusMonths(long monthsToSubtract) {
        return (HijrahDate) super.minusMonths(monthsToSubtract);
    }

    /* access modifiers changed from: package-private */
    public HijrahDate minusWeeks(long weeksToSubtract) {
        return (HijrahDate) super.minusWeeks(weeksToSubtract);
    }

    /* access modifiers changed from: package-private */
    public HijrahDate minusDays(long daysToSubtract) {
        return (HijrahDate) super.minusDays(daysToSubtract);
    }

    public final ChronoLocalDateTime<HijrahDate> atTime(LocalTime localTime) {
        return super.atTime(localTime);
    }

    public ChronoPeriod until(ChronoLocalDate endDate) {
        HijrahDate end = getChronology().date((TemporalAccessor) endDate);
        long totalMonths = (long) (((end.prolepticYear - this.prolepticYear) * 12) + (end.monthOfYear - this.monthOfYear));
        int days = end.dayOfMonth - this.dayOfMonth;
        if (totalMonths > 0 && days < 0) {
            totalMonths--;
            days = (int) (end.toEpochDay() - plusMonths(totalMonths).toEpochDay());
        } else if (totalMonths < 0 && days > 0) {
            totalMonths++;
            days -= end.lengthOfMonth();
        }
        return getChronology().period(Math.toIntExact(totalMonths / 12), (int) (totalMonths % 12), days);
    }

    public boolean equals(Object obj) {
        boolean z = true;
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof HijrahDate)) {
            return false;
        }
        HijrahDate otherDate = (HijrahDate) obj;
        if (!(this.prolepticYear == otherDate.prolepticYear && this.monthOfYear == otherDate.monthOfYear && this.dayOfMonth == otherDate.dayOfMonth && getChronology().equals(otherDate.getChronology()))) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        int yearValue = this.prolepticYear;
        int monthValue = this.monthOfYear;
        return (getChronology().getId().hashCode() ^ (yearValue & -2048)) ^ (((yearValue << 11) + (monthValue << 6)) + this.dayOfMonth);
    }

    private void readObject(ObjectInputStream s) throws InvalidObjectException {
        throw new InvalidObjectException("Deserialization via serialization delegate");
    }

    private Object writeReplace() {
        return new Ser((byte) 6, this);
    }

    /* access modifiers changed from: package-private */
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(getChronology());
        out.writeInt(get(ChronoField.YEAR));
        out.writeByte(get(ChronoField.MONTH_OF_YEAR));
        out.writeByte(get(ChronoField.DAY_OF_MONTH));
    }

    static HijrahDate readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        return ((HijrahChronology) in.readObject()).date(in.readInt(), in.readByte(), in.readByte());
    }
}
