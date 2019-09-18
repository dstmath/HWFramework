package java.time.chrono;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.chrono.ChronoLocalDate;
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
        Objects.requireNonNull(temporal, "temporal");
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
        if (unit != null && unit.isSupportedBy(this)) {
            z = true;
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
        Objects.requireNonNull(formatter, "formatter");
        return formatter.format(this);
    }

    Instant toInstant(ZoneOffset offset) {
        return Instant.ofEpochSecond(toEpochSecond(offset), (long) toLocalTime().getNano());
    }

    long toEpochSecond(ZoneOffset offset) {
        Objects.requireNonNull(offset, "offset");
        return ((86400 * toLocalDate().toEpochDay()) + ((long) toLocalTime().toSecondOfDay())) - ((long) offset.getTotalSeconds());
    }

    /* JADX WARNING: type inference failed for: r4v0, types: [java.time.chrono.ChronoLocalDateTime<?>, java.time.chrono.ChronoLocalDateTime] */
    /* JADX WARNING: Unknown variable types count: 1 */
    int compareTo(ChronoLocalDateTime<?> r4) {
        int cmp = toLocalDate().compareTo(r4.toLocalDate());
        if (cmp != 0) {
            return cmp;
        }
        int cmp2 = toLocalTime().compareTo(r4.toLocalTime());
        if (cmp2 == 0) {
            return getChronology().compareTo(r4.getChronology());
        }
        return cmp2;
    }

    /* JADX WARNING: type inference failed for: r9v0, types: [java.time.chrono.ChronoLocalDateTime<?>, java.time.chrono.ChronoLocalDateTime] */
    /* JADX WARNING: Unknown variable types count: 1 */
    boolean isAfter(ChronoLocalDateTime<?> r9) {
        long thisEpDay = toLocalDate().toEpochDay();
        long otherEpDay = r9.toLocalDate().toEpochDay();
        return thisEpDay > otherEpDay || (thisEpDay == otherEpDay && toLocalTime().toNanoOfDay() > r9.toLocalTime().toNanoOfDay());
    }

    /* JADX WARNING: type inference failed for: r9v0, types: [java.time.chrono.ChronoLocalDateTime<?>, java.time.chrono.ChronoLocalDateTime] */
    /* JADX WARNING: Unknown variable types count: 1 */
    boolean isBefore(ChronoLocalDateTime<?> r9) {
        long thisEpDay = toLocalDate().toEpochDay();
        long otherEpDay = r9.toLocalDate().toEpochDay();
        return thisEpDay < otherEpDay || (thisEpDay == otherEpDay && toLocalTime().toNanoOfDay() < r9.toLocalTime().toNanoOfDay());
    }

    /* JADX WARNING: type inference failed for: r5v0, types: [java.time.chrono.ChronoLocalDateTime<?>, java.time.chrono.ChronoLocalDateTime] */
    /* JADX WARNING: Unknown variable types count: 1 */
    boolean isEqual(ChronoLocalDateTime<?> r5) {
        return toLocalTime().toNanoOfDay() == r5.toLocalTime().toNanoOfDay() && toLocalDate().toEpochDay() == r5.toLocalDate().toEpochDay();
    }
}
