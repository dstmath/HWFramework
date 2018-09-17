package java.time.chrono;

import java.time.Clock;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.ResolverStyle;
import java.time.format.TextStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalQueries;
import java.time.temporal.TemporalQuery;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.time.temporal.ValueRange;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public interface Chronology extends Comparable<Chronology> {
    int compareTo(Chronology chronology);

    ChronoLocalDate date(int i, int i2, int i3);

    ChronoLocalDate date(TemporalAccessor temporalAccessor);

    ChronoLocalDate dateEpochDay(long j);

    ChronoLocalDate dateYearDay(int i, int i2);

    boolean equals(Object obj);

    Era eraOf(int i);

    List<Era> eras();

    String getCalendarType();

    String getId();

    int hashCode();

    boolean isLeapYear(long j);

    int prolepticYear(Era era, int i);

    ValueRange range(ChronoField chronoField);

    ChronoLocalDate resolveDate(Map<TemporalField, Long> map, ResolverStyle resolverStyle);

    String toString();

    static Chronology from(TemporalAccessor temporal) {
        Objects.requireNonNull((Object) temporal, "temporal");
        Chronology obj = (Chronology) temporal.query(TemporalQueries.chronology());
        return obj != null ? obj : IsoChronology.INSTANCE;
    }

    static Chronology ofLocale(Locale locale) {
        return AbstractChronology.ofLocale(locale);
    }

    static Chronology of(String id) {
        return AbstractChronology.of(id);
    }

    static Set<Chronology> getAvailableChronologies() {
        return AbstractChronology.getAvailableChronologies();
    }

    ChronoLocalDate date(Era era, int yearOfEra, int month, int dayOfMonth) {
        return date(prolepticYear(era, yearOfEra), month, dayOfMonth);
    }

    ChronoLocalDate dateYearDay(Era era, int yearOfEra, int dayOfYear) {
        return dateYearDay(prolepticYear(era, yearOfEra), dayOfYear);
    }

    ChronoLocalDate dateNow() {
        return dateNow(Clock.systemDefaultZone());
    }

    ChronoLocalDate dateNow(ZoneId zone) {
        return dateNow(Clock.system(zone));
    }

    ChronoLocalDate dateNow(Clock clock) {
        Objects.requireNonNull((Object) clock, "clock");
        return date(LocalDate.now(clock));
    }

    ChronoLocalDateTime<? extends ChronoLocalDate> localDateTime(TemporalAccessor temporal) {
        try {
            return date(temporal).atTime(LocalTime.from(temporal));
        } catch (DateTimeException ex) {
            throw new DateTimeException("Unable to obtain ChronoLocalDateTime from TemporalAccessor: " + temporal.getClass(), ex);
        }
    }

    ChronoZonedDateTime<? extends ChronoLocalDate> zonedDateTime(TemporalAccessor temporal) {
        try {
            ZoneId zone = ZoneId.from(temporal);
            try {
                return zonedDateTime(Instant.from(temporal), zone);
            } catch (DateTimeException e) {
                return ChronoZonedDateTimeImpl.ofBest(ChronoLocalDateTimeImpl.ensureValid(this, localDateTime(temporal)), zone, null);
            }
        } catch (DateTimeException ex) {
            throw new DateTimeException("Unable to obtain ChronoZonedDateTime from TemporalAccessor: " + temporal.getClass(), ex);
        }
    }

    ChronoZonedDateTime<? extends ChronoLocalDate> zonedDateTime(Instant instant, ZoneId zone) {
        return ChronoZonedDateTimeImpl.ofInstant(this, instant, zone);
    }

    String getDisplayName(TextStyle style, Locale locale) {
        return new DateTimeFormatterBuilder().appendChronologyText(style).toFormatter(locale).format(new TemporalAccessor() {
            public boolean isSupported(TemporalField field) {
                return false;
            }

            public long getLong(TemporalField field) {
                throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
            }

            public <R> R query(TemporalQuery<R> query) {
                if (query == TemporalQueries.chronology()) {
                    return Chronology.this;
                }
                return super.query(query);
            }
        });
    }

    ChronoPeriod period(int years, int months, int days) {
        return new ChronoPeriodImpl(this, years, months, days);
    }
}
