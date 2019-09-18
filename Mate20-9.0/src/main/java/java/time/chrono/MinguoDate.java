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

public final class MinguoDate extends ChronoLocalDateImpl<MinguoDate> implements ChronoLocalDate, Serializable {
    private static final long serialVersionUID = 1300372329181994526L;
    private final transient LocalDate isoDate;

    public /* bridge */ /* synthetic */ String toString() {
        return super.toString();
    }

    public /* bridge */ /* synthetic */ long until(Temporal temporal, TemporalUnit temporalUnit) {
        return super.until(temporal, temporalUnit);
    }

    public static MinguoDate now() {
        return now(Clock.systemDefaultZone());
    }

    public static MinguoDate now(ZoneId zone) {
        return now(Clock.system(zone));
    }

    public static MinguoDate now(Clock clock) {
        return new MinguoDate(LocalDate.now(clock));
    }

    public static MinguoDate of(int prolepticYear, int month, int dayOfMonth) {
        return new MinguoDate(LocalDate.of(prolepticYear + 1911, month, dayOfMonth));
    }

    public static MinguoDate from(TemporalAccessor temporal) {
        return MinguoChronology.INSTANCE.date(temporal);
    }

    MinguoDate(LocalDate isoDate2) {
        Objects.requireNonNull(isoDate2, "isoDate");
        this.isoDate = isoDate2;
    }

    public MinguoChronology getChronology() {
        return MinguoChronology.INSTANCE;
    }

    public MinguoEra getEra() {
        return getProlepticYear() >= 1 ? MinguoEra.ROC : MinguoEra.BEFORE_ROC;
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
                    return ValueRange.of(1, getProlepticYear() <= 0 ? (-range.getMinimum()) + 1 + 1911 : range.getMaximum() - 1911);
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
        return this.isoDate.getYear() - 1911;
    }

    public MinguoDate with(TemporalField field, long newValue) {
        if (!(field instanceof ChronoField)) {
            return (MinguoDate) super.with(field, newValue);
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
                            return with(this.isoDate.withYear(nvalue + 1911));
                        case 7:
                            return with(this.isoDate.withYear((1 - getProlepticYear()) + 1911));
                    }
                } else {
                    return with(this.isoDate.withYear(getProlepticYear() >= 1 ? nvalue + 1911 : (1 - nvalue) + 1911));
                }
            case PROLEPTIC_MONTH:
                getChronology().range(f).checkValidValue(newValue, f);
                return plusMonths(newValue - getProlepticMonth());
        }
        return with(this.isoDate.with(field, newValue));
    }

    public MinguoDate with(TemporalAdjuster adjuster) {
        return (MinguoDate) super.with(adjuster);
    }

    public MinguoDate plus(TemporalAmount amount) {
        return (MinguoDate) super.plus(amount);
    }

    public MinguoDate minus(TemporalAmount amount) {
        return (MinguoDate) super.minus(amount);
    }

    /* access modifiers changed from: package-private */
    public MinguoDate plusYears(long years) {
        return with(this.isoDate.plusYears(years));
    }

    /* access modifiers changed from: package-private */
    public MinguoDate plusMonths(long months) {
        return with(this.isoDate.plusMonths(months));
    }

    /* access modifiers changed from: package-private */
    public MinguoDate plusWeeks(long weeksToAdd) {
        return (MinguoDate) super.plusWeeks(weeksToAdd);
    }

    /* access modifiers changed from: package-private */
    public MinguoDate plusDays(long days) {
        return with(this.isoDate.plusDays(days));
    }

    public MinguoDate plus(long amountToAdd, TemporalUnit unit) {
        return (MinguoDate) super.plus(amountToAdd, unit);
    }

    public MinguoDate minus(long amountToAdd, TemporalUnit unit) {
        return (MinguoDate) super.minus(amountToAdd, unit);
    }

    /* access modifiers changed from: package-private */
    public MinguoDate minusYears(long yearsToSubtract) {
        return (MinguoDate) super.minusYears(yearsToSubtract);
    }

    /* access modifiers changed from: package-private */
    public MinguoDate minusMonths(long monthsToSubtract) {
        return (MinguoDate) super.minusMonths(monthsToSubtract);
    }

    /* access modifiers changed from: package-private */
    public MinguoDate minusWeeks(long weeksToSubtract) {
        return (MinguoDate) super.minusWeeks(weeksToSubtract);
    }

    /* access modifiers changed from: package-private */
    public MinguoDate minusDays(long daysToSubtract) {
        return (MinguoDate) super.minusDays(daysToSubtract);
    }

    private MinguoDate with(LocalDate newDate) {
        return newDate.equals(this.isoDate) ? this : new MinguoDate(newDate);
    }

    public final ChronoLocalDateTime<MinguoDate> atTime(LocalTime localTime) {
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
        if (obj instanceof MinguoDate) {
            return this.isoDate.equals(((MinguoDate) obj).isoDate);
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
        return new Ser((byte) 7, this);
    }

    /* access modifiers changed from: package-private */
    public void writeExternal(DataOutput out) throws IOException {
        out.writeInt(get(ChronoField.YEAR));
        out.writeByte(get(ChronoField.MONTH_OF_YEAR));
        out.writeByte(get(ChronoField.DAY_OF_MONTH));
    }

    static MinguoDate readExternal(DataInput in) throws IOException {
        return MinguoChronology.INSTANCE.date(in.readInt(), in.readByte(), in.readByte());
    }
}
