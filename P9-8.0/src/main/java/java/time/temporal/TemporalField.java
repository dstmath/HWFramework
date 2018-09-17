package java.time.temporal;

import java.time.format.ResolverStyle;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public interface TemporalField {
    <R extends Temporal> R adjustInto(R r, long j);

    TemporalUnit getBaseUnit();

    long getFrom(TemporalAccessor temporalAccessor);

    TemporalUnit getRangeUnit();

    boolean isDateBased();

    boolean isSupportedBy(TemporalAccessor temporalAccessor);

    boolean isTimeBased();

    ValueRange range();

    ValueRange rangeRefinedBy(TemporalAccessor temporalAccessor);

    String toString();

    String getDisplayName(Locale locale) {
        Objects.requireNonNull((Object) locale, "locale");
        return toString();
    }

    TemporalAccessor resolve(Map<TemporalField, Long> map, TemporalAccessor partialTemporal, ResolverStyle resolverStyle) {
        return null;
    }
}
