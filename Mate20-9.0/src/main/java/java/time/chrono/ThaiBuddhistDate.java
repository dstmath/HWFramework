package java.time.chrono;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.time.Clock;
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
import java.util.Objects;

public final class ThaiBuddhistDate extends ChronoLocalDateImpl<ThaiBuddhistDate> implements ChronoLocalDate, Serializable {
    private static final long serialVersionUID = -8722293800195731463L;
    private final transient LocalDate isoDate;

    public /* bridge */ /* synthetic */ String toString() {
        return super.toString();
    }

    public /* bridge */ /* synthetic */ long until(Temporal temporal, TemporalUnit temporalUnit) {
        return super.until(temporal, temporalUnit);
    }

    public static ThaiBuddhistDate now() {
        return now(Clock.systemDefaultZone());
    }

    public static ThaiBuddhistDate now(ZoneId zone) {
        return now(Clock.system(zone));
    }

    public static ThaiBuddhistDate now(Clock clock) {
        return new ThaiBuddhistDate(LocalDate.now(clock));
    }

    public static ThaiBuddhistDate of(int prolepticYear, int month, int dayOfMonth) {
        return new ThaiBuddhistDate(LocalDate.of(prolepticYear - 543, month, dayOfMonth));
    }

    public static ThaiBuddhistDate from(TemporalAccessor temporal) {
        return ThaiBuddhistChronology.INSTANCE.date(temporal);
    }

    ThaiBuddhistDate(LocalDate isoDate2) {
        Objects.requireNonNull(isoDate2, "isoDate");
        this.isoDate = isoDate2;
    }

    public ThaiBuddhistChronology getChronology() {
        return ThaiBuddhistChronology.INSTANCE;
    }

    public ThaiBuddhistEra getEra() {
        return getProlepticYear() >= 1 ? ThaiBuddhistEra.BE : ThaiBuddhistEra.BEFORE_BE;
    }

    public int lengthOfMonth() {
        return this.isoDate.lengthOfMonth();
    }

    public ValueRange range(TemporalField field) {
        if (!(field instanceof ChronoField)) {
            return field.rangeRefinedBy(this);
        }
        if (isSupported(field)) {
            ChronoField f = (ChronoField) field;
            switch (f) {
                case DAY_OF_MONTH:
                case DAY_OF_YEAR:
                case ALIGNED_WEEK_OF_MONTH:
                    return this.isoDate.range(field);
                case YEAR_OF_ERA:
                    ValueRange range = ChronoField.YEAR.range();
                    return ValueRange.of(1, getProlepticYear() <= 0 ? (-(range.getMinimum() + 543)) + 1 : 543 + range.getMaximum());
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
        int i = 1;
        switch ((ChronoField) field) {
            case YEAR_OF_ERA:
                int prolepticYear = getProlepticYear();
                return (long) (prolepticYear >= 1 ? prolepticYear : 1 - prolepticYear);
            case PROLEPTIC_MONTH:
                return getProlepticMonth();
            case YEAR:
                return (long) getProlepticYear();
            case ERA:
                if (getProlepticYear() < 1) {
                    i = 0;
                }
                return (long) i;
            default:
                return this.isoDate.getLong(field);
        }
    }

    private long getProlepticMonth() {
        return ((((long) getProlepticYear()) * 12) + ((long) this.isoDate.getMonthValue())) - 1;
    }

    private int getProlepticYear() {
        return this.isoDate.getYear() + 543;
    }

    public ThaiBuddhistDate with(TemporalField field, long newValue) {
        if (!(field instanceof ChronoField)) {
            return (ThaiBuddhistDate) super.with(field, newValue);
        }
        ChronoField f = (ChronoField) field;
        if (getLong(f) == newValue) {
            return this;
        }
        switch (f) {
            case YEAR_OF_ERA:
            case YEAR:
            case ERA:
                int nvalue = getChronology().range(f).checkValidIntValue(newValue, f);
                int i = AnonymousClass1.$SwitchMap$java$time$temporal$ChronoField[f.ordinal()];
                if (i != 4) {
                    switch (i) {
                        case 6:
                            return with(this.isoDate.withYear(nvalue - 543));
                        case 7:
                            return with(this.isoDate.withYear((1 - getProlepticYear()) - 543));
                    }
                } else {
                    return with(this.isoDate.withYear((getProlepticYear() >= 1 ? nvalue : 1 - nvalue) - 543));
                }
            case PROLEPTIC_MONTH:
                getChronology().range(f).checkValidValue(newValue, f);
                return plusMonths(newValue - getProlepticMonth());
        }
        return with(this.isoDate.with(field, newValue));
    }

    public ThaiBuddhistDate with(TemporalAdjuster adjuster) {
        return (ThaiBuddhistDate) super.with(adjuster);
    }

    public ThaiBuddhistDate plus(TemporalAmount amount) {
        return (ThaiBuddhistDate) super.plus(amount);
    }

    public ThaiBuddhistDate minus(TemporalAmount amount) {
        return (ThaiBuddhistDate) super.minus(amount);
    }

    /* access modifiers changed from: package-private */
    public ThaiBuddhistDate plusYears(long years) {
        return with(this.isoDate.plusYears(years));
    }

    /* access modifiers changed from: package-private */
    public ThaiBuddhistDate plusMonths(long months) {
        return with(this.isoDate.plusMonths(months));
    }

    /* access modifiers changed from: package-private */
    public ThaiBuddhistDate plusWeeks(long weeksToAdd) {
        return (ThaiBuddhistDate) super.plusWeeks(weeksToAdd);
    }

    /* access modifiers changed from: package-private */
    public ThaiBuddhistDate plusDays(long days) {
        return with(this.isoDate.plusDays(days));
    }

    public ThaiBuddhistDate plus(long amountToAdd, TemporalUnit unit) {
        return (ThaiBuddhistDate) super.plus(amountToAdd, unit);
    }

    public ThaiBuddhistDate minus(long amountToAdd, TemporalUnit unit) {
        return (ThaiBuddhistDate) super.minus(amountToAdd, unit);
    }

    /* access modifiers changed from: package-private */
    public ThaiBuddhistDate minusYears(long yearsToSubtract) {
        return (ThaiBuddhistDate) super.minusYears(yearsToSubtract);
    }

    /* access modifiers changed from: package-private */
    public ThaiBuddhistDate minusMonths(long monthsToSubtract) {
        return (ThaiBuddhistDate) super.minusMonths(monthsToSubtract);
    }

    /* access modifiers changed from: package-private */
    public ThaiBuddhistDate minusWeeks(long weeksToSubtract) {
        return (ThaiBuddhistDate) super.minusWeeks(weeksToSubtract);
    }

    /* access modifiers changed from: package-private */
    public ThaiBuddhistDate minusDays(long daysToSubtract) {
        return (ThaiBuddhistDate) super.minusDays(daysToSubtract);
    }

    private ThaiBuddhistDate with(LocalDate newDate) {
        return newDate.equals(this.isoDate) ? this : new ThaiBuddhistDate(newDate);
    }

    public final ChronoLocalDateTime<ThaiBuddhistDate> atTime(LocalTime localTime) {
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
        if (obj instanceof ThaiBuddhistDate) {
            return this.isoDate.equals(((ThaiBuddhistDate) obj).isoDate);
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
        return new Ser((byte) 8, this);
    }

    /* access modifiers changed from: package-private */
    public void writeExternal(DataOutput out) throws IOException {
        out.writeInt(get(ChronoField.YEAR));
        out.writeByte(get(ChronoField.MONTH_OF_YEAR));
        out.writeByte(get(ChronoField.DAY_OF_MONTH));
    }

    static ThaiBuddhistDate readExternal(DataInput in) throws IOException {
        return ThaiBuddhistChronology.INSTANCE.date(in.readInt(), in.readByte(), in.readByte());
    }
}
