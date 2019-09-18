package java.time.chrono;

import java.io.Serializable;
import java.time.chrono.ChronoLocalDate;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalUnit;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.util.Objects;
import sun.util.locale.LanguageTag;

abstract class ChronoLocalDateImpl<D extends ChronoLocalDate> implements ChronoLocalDate, Temporal, TemporalAdjuster, Serializable {
    private static final long serialVersionUID = 6282433883239719096L;

    /* access modifiers changed from: package-private */
    public abstract D plusDays(long j);

    /* access modifiers changed from: package-private */
    public abstract D plusMonths(long j);

    /* access modifiers changed from: package-private */
    public abstract D plusYears(long j);

    static <D extends ChronoLocalDate> D ensureValid(Chronology chrono, Temporal temporal) {
        D other = (ChronoLocalDate) temporal;
        if (chrono.equals(other.getChronology())) {
            return other;
        }
        throw new ClassCastException("Chronology mismatch, expected: " + chrono.getId() + ", actual: " + other.getChronology().getId());
    }

    ChronoLocalDateImpl() {
    }

    public D with(TemporalAdjuster adjuster) {
        return super.with(adjuster);
    }

    public D with(TemporalField field, long value) {
        return super.with(field, value);
    }

    public D plus(TemporalAmount amount) {
        return super.plus(amount);
    }

    public D plus(long amountToAdd, TemporalUnit unit) {
        if (!(unit instanceof ChronoUnit)) {
            return super.plus(amountToAdd, unit);
        }
        switch ((ChronoUnit) unit) {
            case DAYS:
                return plusDays(amountToAdd);
            case WEEKS:
                return plusDays(Math.multiplyExact(amountToAdd, 7));
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

    public D minus(TemporalAmount amount) {
        return super.minus(amount);
    }

    public D minus(long amountToSubtract, TemporalUnit unit) {
        return super.minus(amountToSubtract, unit);
    }

    /* access modifiers changed from: package-private */
    public D plusWeeks(long weeksToAdd) {
        return plusDays(Math.multiplyExact(weeksToAdd, 7));
    }

    /* access modifiers changed from: package-private */
    public D minusYears(long yearsToSubtract) {
        return yearsToSubtract == Long.MIN_VALUE ? ((ChronoLocalDateImpl) plusYears(Long.MAX_VALUE)).plusYears(1) : plusYears(-yearsToSubtract);
    }

    /* access modifiers changed from: package-private */
    public D minusMonths(long monthsToSubtract) {
        return monthsToSubtract == Long.MIN_VALUE ? ((ChronoLocalDateImpl) plusMonths(Long.MAX_VALUE)).plusMonths(1) : plusMonths(-monthsToSubtract);
    }

    /* access modifiers changed from: package-private */
    public D minusWeeks(long weeksToSubtract) {
        return weeksToSubtract == Long.MIN_VALUE ? ((ChronoLocalDateImpl) plusWeeks(Long.MAX_VALUE)).plusWeeks(1) : plusWeeks(-weeksToSubtract);
    }

    /* access modifiers changed from: package-private */
    public D minusDays(long daysToSubtract) {
        return daysToSubtract == Long.MIN_VALUE ? ((ChronoLocalDateImpl) plusDays(Long.MAX_VALUE)).plusDays(1) : plusDays(-daysToSubtract);
    }

    public long until(Temporal endExclusive, TemporalUnit unit) {
        Objects.requireNonNull(endExclusive, "endExclusive");
        ChronoLocalDate end = getChronology().date(endExclusive);
        if (unit instanceof ChronoUnit) {
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
        } else {
            Objects.requireNonNull(unit, "unit");
            return unit.between(this, end);
        }
    }

    private long daysUntil(ChronoLocalDate end) {
        return end.toEpochDay() - toEpochDay();
    }

    private long monthsUntil(ChronoLocalDate end) {
        if (getChronology().range(ChronoField.MONTH_OF_YEAR).getMaximum() == 12) {
            return (((end.getLong(ChronoField.PROLEPTIC_MONTH) * 32) + ((long) end.get(ChronoField.DAY_OF_MONTH))) - ((getLong(ChronoField.PROLEPTIC_MONTH) * 32) + ((long) get(ChronoField.DAY_OF_MONTH)))) / 32;
        }
        throw new IllegalStateException("ChronoLocalDateImpl only supports Chronologies with 12 months per year");
    }

    public boolean equals(Object obj) {
        boolean z = true;
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ChronoLocalDate)) {
            return false;
        }
        if (compareTo((ChronoLocalDate) obj) != 0) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        long epDay = toEpochDay();
        return getChronology().hashCode() ^ ((int) ((epDay >>> 32) ^ epDay));
    }

    public String toString() {
        long yoe = getLong(ChronoField.YEAR_OF_ERA);
        long moy = getLong(ChronoField.MONTH_OF_YEAR);
        long dom = getLong(ChronoField.DAY_OF_MONTH);
        StringBuilder buf = new StringBuilder(30);
        buf.append(getChronology().toString());
        buf.append(" ");
        buf.append((Object) getEra());
        buf.append(" ");
        buf.append(yoe);
        buf.append(moy < 10 ? "-0" : LanguageTag.SEP);
        buf.append(moy);
        buf.append(dom < 10 ? "-0" : LanguageTag.SEP);
        buf.append(dom);
        return buf.toString();
    }
}
