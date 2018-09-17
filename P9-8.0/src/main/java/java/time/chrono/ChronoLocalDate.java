package java.time.chrono;

import java.time.DateTimeException;
import java.time.LocalTime;
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
import java.util.Comparator;
import java.util.Objects;

public interface ChronoLocalDate extends Temporal, TemporalAdjuster, Comparable<ChronoLocalDate> {
    boolean equals(Object obj);

    Chronology getChronology();

    int hashCode();

    int lengthOfMonth();

    String toString();

    long until(Temporal temporal, TemporalUnit temporalUnit);

    ChronoPeriod until(ChronoLocalDate chronoLocalDate);

    static Comparator<ChronoLocalDate> timeLineOrder() {
        return AbstractChronology.DATE_ORDER;
    }

    static ChronoLocalDate from(TemporalAccessor temporal) {
        if (temporal instanceof ChronoLocalDate) {
            return (ChronoLocalDate) temporal;
        }
        Objects.requireNonNull((Object) temporal, "temporal");
        Chronology chrono = (Chronology) temporal.query(TemporalQueries.chronology());
        if (chrono != null) {
            return chrono.date(temporal);
        }
        throw new DateTimeException("Unable to obtain ChronoLocalDate from TemporalAccessor: " + temporal.getClass());
    }

    Era getEra() {
        return getChronology().eraOf(get(ChronoField.ERA));
    }

    boolean isLeapYear() {
        return getChronology().isLeapYear(getLong(ChronoField.YEAR));
    }

    int lengthOfYear() {
        return isLeapYear() ? 366 : 365;
    }

    boolean isSupported(TemporalField field) {
        if (field instanceof ChronoField) {
            return field.isDateBased();
        }
        return field != null ? field.isSupportedBy(this) : false;
    }

    boolean isSupported(TemporalUnit unit) {
        if (unit instanceof ChronoUnit) {
            return unit.isDateBased();
        }
        return unit != null ? unit.isSupportedBy(this) : false;
    }

    ChronoLocalDate with(TemporalAdjuster adjuster) {
        return ChronoLocalDateImpl.ensureValid(getChronology(), super.with(adjuster));
    }

    ChronoLocalDate with(TemporalField field, long newValue) {
        if (!(field instanceof ChronoField)) {
            return ChronoLocalDateImpl.ensureValid(getChronology(), field.adjustInto(this, newValue));
        }
        throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
    }

    ChronoLocalDate plus(TemporalAmount amount) {
        return ChronoLocalDateImpl.ensureValid(getChronology(), super.plus(amount));
    }

    ChronoLocalDate plus(long amountToAdd, TemporalUnit unit) {
        if (!(unit instanceof ChronoUnit)) {
            return ChronoLocalDateImpl.ensureValid(getChronology(), unit.addTo(this, amountToAdd));
        }
        throw new UnsupportedTemporalTypeException("Unsupported unit: " + unit);
    }

    ChronoLocalDate minus(TemporalAmount amount) {
        return ChronoLocalDateImpl.ensureValid(getChronology(), super.minus(amount));
    }

    ChronoLocalDate minus(long amountToSubtract, TemporalUnit unit) {
        return ChronoLocalDateImpl.ensureValid(getChronology(), super.minus(amountToSubtract, unit));
    }

    /* JADX WARNING: Missing block: B:4:0x000d, code:
            return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    <R> R query(TemporalQuery<R> query) {
        if (query == TemporalQueries.zoneId() || query == TemporalQueries.zone() || query == TemporalQueries.offset() || query == TemporalQueries.localTime()) {
            return null;
        }
        if (query == TemporalQueries.chronology()) {
            return getChronology();
        }
        if (query == TemporalQueries.precision()) {
            return ChronoUnit.DAYS;
        }
        return query.queryFrom(this);
    }

    Temporal adjustInto(Temporal temporal) {
        return temporal.with(ChronoField.EPOCH_DAY, toEpochDay());
    }

    String format(DateTimeFormatter formatter) {
        Objects.requireNonNull((Object) formatter, "formatter");
        return formatter.format(this);
    }

    ChronoLocalDateTime<?> atTime(LocalTime localTime) {
        return ChronoLocalDateTimeImpl.of(this, localTime);
    }

    long toEpochDay() {
        return getLong(ChronoField.EPOCH_DAY);
    }

    int compareTo(ChronoLocalDate other) {
        int cmp = Long.compare(toEpochDay(), other.toEpochDay());
        if (cmp == 0) {
            return getChronology().compareTo(other.getChronology());
        }
        return cmp;
    }

    boolean isAfter(ChronoLocalDate other) {
        return toEpochDay() > other.toEpochDay();
    }

    boolean isBefore(ChronoLocalDate other) {
        return toEpochDay() < other.toEpochDay();
    }

    boolean isEqual(ChronoLocalDate other) {
        return toEpochDay() == other.toEpochDay();
    }
}
