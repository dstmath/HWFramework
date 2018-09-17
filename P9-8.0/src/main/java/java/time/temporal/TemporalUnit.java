package java.time.temporal;

import java.time.Duration;
import java.time.LocalTime;
import java.time.chrono.ChronoLocalDate;
import java.time.chrono.ChronoLocalDateTime;
import java.time.chrono.ChronoZonedDateTime;

public interface TemporalUnit {
    <R extends Temporal> R addTo(R r, long j);

    long between(Temporal temporal, Temporal temporal2);

    Duration getDuration();

    boolean isDateBased();

    boolean isDurationEstimated();

    boolean isTimeBased();

    String toString();

    boolean isSupportedBy(Temporal temporal) {
        if (temporal instanceof LocalTime) {
            return isTimeBased();
        }
        if (temporal instanceof ChronoLocalDate) {
            return isDateBased();
        }
        if ((temporal instanceof ChronoLocalDateTime) || (temporal instanceof ChronoZonedDateTime)) {
            return true;
        }
        try {
            temporal.plus(1, this);
            return true;
        } catch (UnsupportedTemporalTypeException e) {
            return false;
        } catch (RuntimeException e2) {
            try {
                temporal.plus(-1, this);
                return true;
            } catch (RuntimeException e3) {
                return false;
            }
        }
    }
}
