package java.time.temporal;

import java.time.DateTimeException;
import java.util.Objects;

public interface TemporalAccessor {
    long getLong(TemporalField temporalField);

    boolean isSupported(TemporalField temporalField);

    ValueRange range(TemporalField field) {
        if (!(field instanceof ChronoField)) {
            Objects.requireNonNull((Object) field, "field");
            return field.rangeRefinedBy(this);
        } else if (isSupported(field)) {
            return field.range();
        } else {
            throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
        }
    }

    int get(TemporalField field) {
        Object range = range(field);
        if (range.isIntValue()) {
            long value = getLong(field);
            if (range.isValidValue(value)) {
                return (int) value;
            }
            throw new DateTimeException("Invalid value for " + field + " (valid values " + range + "): " + value);
        }
        throw new UnsupportedTemporalTypeException("Invalid field " + field + " for get() method, use getLong() instead");
    }

    <R> R query(TemporalQuery<R> query) {
        if (query == TemporalQueries.zoneId() || query == TemporalQueries.chronology() || query == TemporalQueries.precision()) {
            return null;
        }
        return query.queryFrom(this);
    }
}
