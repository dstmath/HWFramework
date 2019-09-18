package java.time.temporal;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.chrono.Chronology;

public final class TemporalQueries {
    static final TemporalQuery<Chronology> CHRONO = $$Lambda$TemporalQueries$thd4JmExRUYKd7nNlE7b5oT19ms.INSTANCE;
    static final TemporalQuery<LocalDate> LOCAL_DATE = $$Lambda$TemporalQueries$JPrXwgedeqexYxypO8VpPKV4l3c.INSTANCE;
    static final TemporalQuery<LocalTime> LOCAL_TIME = $$Lambda$TemporalQueries$WGGw7SkRcanjtxRiTk5p0dKf_jc.INSTANCE;
    static final TemporalQuery<ZoneOffset> OFFSET = $$Lambda$TemporalQueries$bI5NESEXE4DqyC7TnOvbkx1GlvM.INSTANCE;
    static final TemporalQuery<TemporalUnit> PRECISION = $$Lambda$TemporalQueries$okxqZ6ZoOhHd_zSzW7k5qRIaLxM.INSTANCE;
    static final TemporalQuery<ZoneId> ZONE = $$Lambda$TemporalQueries$PBpYKRiwkxqQNlcUBOJfaQoONg.INSTANCE;
    static final TemporalQuery<ZoneId> ZONE_ID = $$Lambda$TemporalQueries$IZUinmsZUz98YXPe0ftAd27ByiE.INSTANCE;

    private TemporalQueries() {
    }

    public static TemporalQuery<ZoneId> zoneId() {
        return ZONE_ID;
    }

    public static TemporalQuery<Chronology> chronology() {
        return CHRONO;
    }

    public static TemporalQuery<TemporalUnit> precision() {
        return PRECISION;
    }

    public static TemporalQuery<ZoneId> zone() {
        return ZONE;
    }

    public static TemporalQuery<ZoneOffset> offset() {
        return OFFSET;
    }

    public static TemporalQuery<LocalDate> localDate() {
        return LOCAL_DATE;
    }

    public static TemporalQuery<LocalTime> localTime() {
        return LOCAL_TIME;
    }

    static /* synthetic */ ZoneId lambda$static$0(TemporalAccessor temporal) {
        return (ZoneId) temporal.query(ZONE_ID);
    }

    static /* synthetic */ Chronology lambda$static$1(TemporalAccessor temporal) {
        return (Chronology) temporal.query(CHRONO);
    }

    static /* synthetic */ TemporalUnit lambda$static$2(TemporalAccessor temporal) {
        return (TemporalUnit) temporal.query(PRECISION);
    }

    static /* synthetic */ ZoneOffset lambda$static$3(TemporalAccessor temporal) {
        if (temporal.isSupported(ChronoField.OFFSET_SECONDS)) {
            return ZoneOffset.ofTotalSeconds(temporal.get(ChronoField.OFFSET_SECONDS));
        }
        return null;
    }

    static /* synthetic */ ZoneId lambda$static$4(TemporalAccessor temporal) {
        ZoneId zone = (ZoneId) temporal.query(ZONE_ID);
        return zone != null ? zone : (ZoneId) temporal.query(OFFSET);
    }

    static /* synthetic */ LocalDate lambda$static$5(TemporalAccessor temporal) {
        if (temporal.isSupported(ChronoField.EPOCH_DAY)) {
            return LocalDate.ofEpochDay(temporal.getLong(ChronoField.EPOCH_DAY));
        }
        return null;
    }

    static /* synthetic */ LocalTime lambda$static$6(TemporalAccessor temporal) {
        if (temporal.isSupported(ChronoField.NANO_OF_DAY)) {
            return LocalTime.ofNanoOfDay(temporal.getLong(ChronoField.NANO_OF_DAY));
        }
        return null;
    }
}
