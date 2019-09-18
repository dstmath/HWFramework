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
import java.time.temporal.UnsupportedTemporalTypeException;
import java.time.temporal.ValueRange;
import java.util.Comparator;
import java.util.Objects;

public interface ChronoZonedDateTime<D extends ChronoLocalDate> extends Temporal, Comparable<ChronoZonedDateTime<?>> {
    boolean equals(Object obj);

    ZoneOffset getOffset();

    ZoneId getZone();

    int hashCode();

    boolean isSupported(TemporalField temporalField);

    ChronoZonedDateTime<D> plus(long j, TemporalUnit temporalUnit);

    ChronoLocalDateTime<D> toLocalDateTime();

    String toString();

    ChronoZonedDateTime<D> with(TemporalField temporalField, long j);

    ChronoZonedDateTime<D> withEarlierOffsetAtOverlap();

    ChronoZonedDateTime<D> withLaterOffsetAtOverlap();

    ChronoZonedDateTime<D> withZoneSameInstant(ZoneId zoneId);

    ChronoZonedDateTime<D> withZoneSameLocal(ZoneId zoneId);

    static Comparator<ChronoZonedDateTime<?>> timeLineOrder() {
        return AbstractChronology.INSTANT_ORDER;
    }

    static ChronoZonedDateTime<?> from(TemporalAccessor temporal) {
        if (temporal instanceof ChronoZonedDateTime) {
            return (ChronoZonedDateTime) temporal;
        }
        Objects.requireNonNull(temporal, "temporal");
        Chronology chrono = (Chronology) temporal.query(TemporalQueries.chronology());
        if (chrono != null) {
            return chrono.zonedDateTime(temporal);
        }
        throw new DateTimeException("Unable to obtain ChronoZonedDateTime from TemporalAccessor: " + temporal.getClass());
    }

    ValueRange range(TemporalField field) {
        if (!(field instanceof ChronoField)) {
            return field.rangeRefinedBy(this);
        }
        if (field == ChronoField.INSTANT_SECONDS || field == ChronoField.OFFSET_SECONDS) {
            return field.range();
        }
        return toLocalDateTime().range(field);
    }

    int get(TemporalField field) {
        if (!(field instanceof ChronoField)) {
            return super.get(field);
        }
        switch ((ChronoField) field) {
            case INSTANT_SECONDS:
                throw new UnsupportedTemporalTypeException("Invalid field 'InstantSeconds' for get() method, use getLong() instead");
            case OFFSET_SECONDS:
                return getOffset().getTotalSeconds();
            default:
                return toLocalDateTime().get(field);
        }
    }

    long getLong(TemporalField field) {
        if (!(field instanceof ChronoField)) {
            return field.getFrom(this);
        }
        switch ((ChronoField) field) {
            case INSTANT_SECONDS:
                return toEpochSecond();
            case OFFSET_SECONDS:
                return (long) getOffset().getTotalSeconds();
            default:
                return toLocalDateTime().getLong(field);
        }
    }

    D toLocalDate() {
        return toLocalDateTime().toLocalDate();
    }

    LocalTime toLocalTime() {
        return toLocalDateTime().toLocalTime();
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

    ChronoZonedDateTime<D> with(TemporalAdjuster adjuster) {
        return ChronoZonedDateTimeImpl.ensureValid(getChronology(), super.with(adjuster));
    }

    ChronoZonedDateTime<D> plus(TemporalAmount amount) {
        return ChronoZonedDateTimeImpl.ensureValid(getChronology(), super.plus(amount));
    }

    ChronoZonedDateTime<D> minus(TemporalAmount amount) {
        return ChronoZonedDateTimeImpl.ensureValid(getChronology(), super.minus(amount));
    }

    ChronoZonedDateTime<D> minus(long amountToSubtract, TemporalUnit unit) {
        return ChronoZonedDateTimeImpl.ensureValid(getChronology(), super.minus(amountToSubtract, unit));
    }

    <R> R query(TemporalQuery<R> query) {
        if (query == TemporalQueries.zone() || query == TemporalQueries.zoneId()) {
            return getZone();
        }
        if (query == TemporalQueries.offset()) {
            return getOffset();
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

    String format(DateTimeFormatter formatter) {
        Objects.requireNonNull(formatter, "formatter");
        return formatter.format(this);
    }

    Instant toInstant() {
        return Instant.ofEpochSecond(toEpochSecond(), (long) toLocalTime().getNano());
    }

    long toEpochSecond() {
        return ((86400 * toLocalDate().toEpochDay()) + ((long) toLocalTime().toSecondOfDay())) - ((long) getOffset().getTotalSeconds());
    }

    int compareTo(ChronoZonedDateTime<?> other) {
        int cmp = Long.compare(toEpochSecond(), other.toEpochSecond());
        if (cmp != 0) {
            return cmp;
        }
        int cmp2 = toLocalTime().getNano() - other.toLocalTime().getNano();
        if (cmp2 != 0) {
            return cmp2;
        }
        int cmp3 = toLocalDateTime().compareTo(other.toLocalDateTime());
        if (cmp3 != 0) {
            return cmp3;
        }
        int cmp4 = getZone().getId().compareTo(other.getZone().getId());
        if (cmp4 == 0) {
            return getChronology().compareTo(other.getChronology());
        }
        return cmp4;
    }

    boolean isBefore(ChronoZonedDateTime<?> other) {
        long thisEpochSec = toEpochSecond();
        long otherEpochSec = other.toEpochSecond();
        return thisEpochSec < otherEpochSec || (thisEpochSec == otherEpochSec && toLocalTime().getNano() < other.toLocalTime().getNano());
    }

    boolean isAfter(ChronoZonedDateTime<?> other) {
        long thisEpochSec = toEpochSecond();
        long otherEpochSec = other.toEpochSecond();
        return thisEpochSec > otherEpochSec || (thisEpochSec == otherEpochSec && toLocalTime().getNano() > other.toLocalTime().getNano());
    }

    boolean isEqual(ChronoZonedDateTime<?> other) {
        return toEpochSecond() == other.toEpochSecond() && toLocalTime().getNano() == other.toLocalTime().getNano();
    }
}
