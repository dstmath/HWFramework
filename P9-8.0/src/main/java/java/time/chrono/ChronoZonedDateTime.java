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
import java.time.temporal.UnsupportedTemporalTypeException;
import java.time.temporal.ValueRange;
import java.util.Comparator;
import java.util.Objects;

public interface ChronoZonedDateTime<D extends ChronoLocalDate> extends Temporal, Comparable<ChronoZonedDateTime<?>> {
    public static final /* synthetic */ int[] -java-time-temporal-ChronoFieldSwitchesValues = null;

    private static /* synthetic */ int[] -getjava-time-temporal-ChronoFieldSwitchesValues() {
        if (-java-time-temporal-ChronoFieldSwitchesValues != null) {
            return -java-time-temporal-ChronoFieldSwitchesValues;
        }
        int[] iArr = new int[ChronoField.values().length];
        try {
            iArr[ChronoField.ALIGNED_DAY_OF_WEEK_IN_MONTH.ordinal()] = 3;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[ChronoField.ALIGNED_DAY_OF_WEEK_IN_YEAR.ordinal()] = 4;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[ChronoField.ALIGNED_WEEK_OF_MONTH.ordinal()] = 5;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[ChronoField.ALIGNED_WEEK_OF_YEAR.ordinal()] = 6;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[ChronoField.AMPM_OF_DAY.ordinal()] = 7;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[ChronoField.CLOCK_HOUR_OF_AMPM.ordinal()] = 8;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[ChronoField.CLOCK_HOUR_OF_DAY.ordinal()] = 9;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[ChronoField.DAY_OF_MONTH.ordinal()] = 10;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[ChronoField.DAY_OF_WEEK.ordinal()] = 11;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[ChronoField.DAY_OF_YEAR.ordinal()] = 12;
        } catch (NoSuchFieldError e10) {
        }
        try {
            iArr[ChronoField.EPOCH_DAY.ordinal()] = 13;
        } catch (NoSuchFieldError e11) {
        }
        try {
            iArr[ChronoField.ERA.ordinal()] = 14;
        } catch (NoSuchFieldError e12) {
        }
        try {
            iArr[ChronoField.HOUR_OF_AMPM.ordinal()] = 15;
        } catch (NoSuchFieldError e13) {
        }
        try {
            iArr[ChronoField.HOUR_OF_DAY.ordinal()] = 16;
        } catch (NoSuchFieldError e14) {
        }
        try {
            iArr[ChronoField.INSTANT_SECONDS.ordinal()] = 1;
        } catch (NoSuchFieldError e15) {
        }
        try {
            iArr[ChronoField.MICRO_OF_DAY.ordinal()] = 17;
        } catch (NoSuchFieldError e16) {
        }
        try {
            iArr[ChronoField.MICRO_OF_SECOND.ordinal()] = 18;
        } catch (NoSuchFieldError e17) {
        }
        try {
            iArr[ChronoField.MILLI_OF_DAY.ordinal()] = 19;
        } catch (NoSuchFieldError e18) {
        }
        try {
            iArr[ChronoField.MILLI_OF_SECOND.ordinal()] = 20;
        } catch (NoSuchFieldError e19) {
        }
        try {
            iArr[ChronoField.MINUTE_OF_DAY.ordinal()] = 21;
        } catch (NoSuchFieldError e20) {
        }
        try {
            iArr[ChronoField.MINUTE_OF_HOUR.ordinal()] = 22;
        } catch (NoSuchFieldError e21) {
        }
        try {
            iArr[ChronoField.MONTH_OF_YEAR.ordinal()] = 23;
        } catch (NoSuchFieldError e22) {
        }
        try {
            iArr[ChronoField.NANO_OF_DAY.ordinal()] = 24;
        } catch (NoSuchFieldError e23) {
        }
        try {
            iArr[ChronoField.NANO_OF_SECOND.ordinal()] = 25;
        } catch (NoSuchFieldError e24) {
        }
        try {
            iArr[ChronoField.OFFSET_SECONDS.ordinal()] = 2;
        } catch (NoSuchFieldError e25) {
        }
        try {
            iArr[ChronoField.PROLEPTIC_MONTH.ordinal()] = 26;
        } catch (NoSuchFieldError e26) {
        }
        try {
            iArr[ChronoField.SECOND_OF_DAY.ordinal()] = 27;
        } catch (NoSuchFieldError e27) {
        }
        try {
            iArr[ChronoField.SECOND_OF_MINUTE.ordinal()] = 28;
        } catch (NoSuchFieldError e28) {
        }
        try {
            iArr[ChronoField.YEAR.ordinal()] = 29;
        } catch (NoSuchFieldError e29) {
        }
        try {
            iArr[ChronoField.YEAR_OF_ERA.ordinal()] = 30;
        } catch (NoSuchFieldError e30) {
        }
        -java-time-temporal-ChronoFieldSwitchesValues = iArr;
        return iArr;
    }

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
        Objects.requireNonNull((Object) temporal, "temporal");
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
        switch (-getjava-time-temporal-ChronoFieldSwitchesValues()[((ChronoField) field).ordinal()]) {
            case 1:
                throw new UnsupportedTemporalTypeException("Invalid field 'InstantSeconds' for get() method, use getLong() instead");
            case 2:
                return getOffset().getTotalSeconds();
            default:
                return toLocalDateTime().get(field);
        }
    }

    long getLong(TemporalField field) {
        if (!(field instanceof ChronoField)) {
            return field.getFrom(this);
        }
        switch (-getjava-time-temporal-ChronoFieldSwitchesValues()[((ChronoField) field).ordinal()]) {
            case 1:
                return toEpochSecond();
            case 2:
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
        if (unit != null) {
            z = unit.isSupportedBy(this);
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
        Objects.requireNonNull((Object) formatter, "formatter");
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
        cmp = toLocalTime().getNano() - other.toLocalTime().getNano();
        if (cmp != 0) {
            return cmp;
        }
        cmp = toLocalDateTime().compareTo(other.toLocalDateTime());
        if (cmp != 0) {
            return cmp;
        }
        cmp = getZone().getId().compareTo(other.getZone().getId());
        if (cmp == 0) {
            return getChronology().compareTo(other.getChronology());
        }
        return cmp;
    }

    boolean isBefore(ChronoZonedDateTime<?> other) {
        long thisEpochSec = toEpochSecond();
        long otherEpochSec = other.toEpochSecond();
        if (thisEpochSec < otherEpochSec) {
            return true;
        }
        if (thisEpochSec != otherEpochSec || toLocalTime().getNano() >= other.toLocalTime().getNano()) {
            return false;
        }
        return true;
    }

    boolean isAfter(ChronoZonedDateTime<?> other) {
        long thisEpochSec = toEpochSecond();
        long otherEpochSec = other.toEpochSecond();
        if (thisEpochSec > otherEpochSec) {
            return true;
        }
        if (thisEpochSec != otherEpochSec || toLocalTime().getNano() <= other.toLocalTime().getNano()) {
            return false;
        }
        return true;
    }

    boolean isEqual(ChronoZonedDateTime<?> other) {
        if (toEpochSecond() == other.toEpochSecond() && toLocalTime().getNano() == other.toLocalTime().getNano()) {
            return true;
        }
        return false;
    }
}
