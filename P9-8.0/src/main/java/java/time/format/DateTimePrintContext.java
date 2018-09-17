package java.time.format;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.chrono.ChronoLocalDate;
import java.time.chrono.Chronology;
import java.time.chrono.IsoChronology;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalQueries;
import java.time.temporal.TemporalQuery;
import java.time.temporal.ValueRange;
import java.util.Locale;
import java.util.Objects;

final class DateTimePrintContext {
    private DateTimeFormatter formatter;
    private int optional;
    private TemporalAccessor temporal;

    DateTimePrintContext(TemporalAccessor temporal, DateTimeFormatter formatter) {
        this.temporal = adjust(temporal, formatter);
        this.formatter = formatter;
    }

    private static TemporalAccessor adjust(final TemporalAccessor temporal, DateTimeFormatter formatter) {
        Object overrideChrono = formatter.getChronology();
        ZoneId overrideZone = formatter.getZone();
        if (overrideChrono == null && overrideZone == null) {
            return temporal;
        }
        Chronology temporalChrono = (Chronology) temporal.query(TemporalQueries.chronology());
        ZoneId temporalZone = (ZoneId) temporal.query(TemporalQueries.zoneId());
        if (Objects.equals(overrideChrono, temporalChrono)) {
            overrideChrono = null;
        }
        if (Objects.equals(overrideZone, temporalZone)) {
            overrideZone = null;
        }
        if (overrideChrono == null && overrideZone == null) {
            return temporal;
        }
        ChronoLocalDate effectiveDate;
        final Chronology effectiveChrono = overrideChrono != null ? overrideChrono : temporalChrono;
        if (overrideZone != null) {
            if (temporal.isSupported(ChronoField.INSTANT_SECONDS)) {
                return (effectiveChrono != null ? effectiveChrono : IsoChronology.INSTANCE).zonedDateTime(Instant.from(temporal), overrideZone);
            } else if ((overrideZone.normalized() instanceof ZoneOffset) && temporal.isSupported(ChronoField.OFFSET_SECONDS) && temporal.get(ChronoField.OFFSET_SECONDS) != overrideZone.getRules().getOffset(Instant.EPOCH).getTotalSeconds()) {
                throw new DateTimeException("Unable to apply override zone '" + overrideZone + "' because the temporal object being formatted has a different offset but" + " does not represent an instant: " + temporal);
            }
        }
        final ZoneId effectiveZone = overrideZone != null ? overrideZone : temporalZone;
        if (overrideChrono == null) {
            effectiveDate = null;
        } else if (temporal.isSupported(ChronoField.EPOCH_DAY)) {
            effectiveDate = effectiveChrono.date(temporal);
        } else {
            if (!(overrideChrono == IsoChronology.INSTANCE && temporalChrono == null)) {
                for (ChronoField f : ChronoField.values()) {
                    if (f.isDateBased() && temporal.isSupported(f)) {
                        throw new DateTimeException("Unable to apply override chronology '" + overrideChrono + "' because the temporal object being formatted contains date fields but" + " does not represent a whole date: " + temporal);
                    }
                }
            }
            effectiveDate = null;
        }
        return new TemporalAccessor() {
            public boolean isSupported(TemporalField field) {
                if (effectiveDate == null || !field.isDateBased()) {
                    return temporal.isSupported(field);
                }
                return effectiveDate.isSupported(field);
            }

            public ValueRange range(TemporalField field) {
                if (effectiveDate == null || !field.isDateBased()) {
                    return temporal.range(field);
                }
                return effectiveDate.range(field);
            }

            public long getLong(TemporalField field) {
                if (effectiveDate == null || !field.isDateBased()) {
                    return temporal.getLong(field);
                }
                return effectiveDate.getLong(field);
            }

            public <R> R query(TemporalQuery<R> query) {
                if (query == TemporalQueries.chronology()) {
                    return effectiveChrono;
                }
                if (query == TemporalQueries.zoneId()) {
                    return effectiveZone;
                }
                if (query == TemporalQueries.precision()) {
                    return temporal.query(query);
                }
                return query.queryFrom(this);
            }
        };
    }

    TemporalAccessor getTemporal() {
        return this.temporal;
    }

    Locale getLocale() {
        return this.formatter.getLocale();
    }

    DecimalStyle getDecimalStyle() {
        return this.formatter.getDecimalStyle();
    }

    void startOptional() {
        this.optional++;
    }

    void endOptional() {
        this.optional--;
    }

    <R> R getValue(TemporalQuery<R> query) {
        R result = this.temporal.query(query);
        if (result != null || this.optional != 0) {
            return result;
        }
        throw new DateTimeException("Unable to extract value: " + this.temporal.getClass());
    }

    Long getValue(TemporalField field) {
        try {
            return Long.valueOf(this.temporal.getLong(field));
        } catch (DateTimeException ex) {
            if (this.optional > 0) {
                return null;
            }
            throw ex;
        }
    }

    public String toString() {
        return this.temporal.toString();
    }
}
