package java.time.chrono;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
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
import java.util.Comparator;
import java.util.Objects;

public interface ChronoLocalDateTime<D extends ChronoLocalDate> extends Temporal, TemporalAdjuster, Comparable<ChronoLocalDateTime<?>> {
    ChronoZonedDateTime<D> atZone(ZoneId zoneId);

    boolean equals(Object obj);

    int hashCode();

    boolean isSupported(TemporalField temporalField);

    ChronoLocalDateTime<D> plus(long j, TemporalUnit temporalUnit);

    D toLocalDate();

    LocalTime toLocalTime();

    String toString();

    ChronoLocalDateTime<D> with(TemporalField temporalField, long j);

    static Comparator<ChronoLocalDateTime<?>> timeLineOrder() {
        return AbstractChronology.DATE_TIME_ORDER;
    }

    static ChronoLocalDateTime<?> from(TemporalAccessor temporal) {
        if (temporal instanceof ChronoLocalDateTime) {
            return (ChronoLocalDateTime) temporal;
        }
        Objects.requireNonNull((Object) temporal, "temporal");
        Chronology chrono = (Chronology) temporal.query(TemporalQueries.chronology());
        if (chrono != null) {
            return chrono.localDateTime(temporal);
        }
        throw new DateTimeException("Unable to obtain ChronoLocalDateTime from TemporalAccessor: " + temporal.getClass());
    }

    Chronology getChronology() {
        return toLocalDate().getChronology();
    }

    boolean isSupported(TemporalUnit unit) {
        boolean z = false;
        if (unit instanceof ChronoUnit) {
            if (unit != ChronoUnit.FOREVER) {
                z = true;
            }
            return z;
        }
        if (unit != null) {
            z = unit.isSupportedBy(this);
        }
        return z;
    }

    ChronoLocalDateTime<D> with(TemporalAdjuster adjuster) {
        return ChronoLocalDateTimeImpl.ensureValid(getChronology(), super.with(adjuster));
    }

    ChronoLocalDateTime<D> plus(TemporalAmount amount) {
        return ChronoLocalDateTimeImpl.ensureValid(getChronology(), super.plus(amount));
    }

    ChronoLocalDateTime<D> minus(TemporalAmount amount) {
        return ChronoLocalDateTimeImpl.ensureValid(getChronology(), super.minus(amount));
    }

    ChronoLocalDateTime<D> minus(long amountToSubtract, TemporalUnit unit) {
        return ChronoLocalDateTimeImpl.ensureValid(getChronology(), super.minus(amountToSubtract, unit));
    }

    <R> R query(TemporalQuery<R> query) {
        if (query == TemporalQueries.zoneId() || query == TemporalQueries.zone() || query == TemporalQueries.offset()) {
            return null;
        }
        if (query == TemporalQueries.localTime()) {
            return toLocalTime();
        }
        if (query == TemporalQueries.chronology()) {
            return getChronology();
        }
        if (query == TemporalQueries.precision()) {
            return ChronoUnit.NANOS;
        }
        return query.queryFrom(this);
    }

    Temporal adjustInto(Temporal temporal) {
        return temporal.with(ChronoField.EPOCH_DAY, toLocalDate().toEpochDay()).with(ChronoField.NANO_OF_DAY, toLocalTime().toNanoOfDay());
    }

    String format(DateTimeFormatter formatter) {
        Objects.requireNonNull((Object) formatter, "formatter");
        return formatter.format(this);
    }

    Instant toInstant(ZoneOffset offset) {
        return Instant.ofEpochSecond(toEpochSecond(offset), (long) toLocalTime().getNano());
    }

    long toEpochSecond(ZoneOffset offset) {
        Objects.requireNonNull((Object) offset, "offset");
        return ((86400 * toLocalDate().toEpochDay()) + ((long) toLocalTime().toSecondOfDay())) - ((long) offset.getTotalSeconds());
    }

    int compareTo(ChronoLocalDateTime<?> other) {
        int cmp = toLocalDate().compareTo(other.toLocalDate());
        if (cmp != 0) {
            return cmp;
        }
        cmp = toLocalTime().compareTo(other.toLocalTime());
        if (cmp == 0) {
            return getChronology().compareTo(other.getChronology());
        }
        return cmp;
    }

    boolean isAfter(ChronoLocalDateTime<?> other) {
        long thisEpDay = toLocalDate().toEpochDay();
        long otherEpDay = other.toLocalDate().toEpochDay();
        if (thisEpDay > otherEpDay) {
            return true;
        }
        if (thisEpDay != otherEpDay || toLocalTime().toNanoOfDay() <= other.toLocalTime().toNanoOfDay()) {
            return false;
        }
        return true;
    }

    boolean isBefore(ChronoLocalDateTime<?> other) {
        long thisEpDay = toLocalDate().toEpochDay();
        long otherEpDay = other.toLocalDate().toEpochDay();
        if (thisEpDay < otherEpDay) {
            return true;
        }
        if (thisEpDay != otherEpDay || toLocalTime().toNanoOfDay() >= other.toLocalTime().toNanoOfDay()) {
            return false;
        }
        return true;
    }

    boolean isEqual(ChronoLocalDateTime<?> other) {
        if (toLocalTime().toNanoOfDay() == other.toLocalTime().toNanoOfDay() && toLocalDate().toEpochDay() == other.toLocalDate().toEpochDay()) {
            return true;
        }
        return false;
    }
}
