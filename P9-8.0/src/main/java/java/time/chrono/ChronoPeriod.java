package java.time.chrono;

import java.time.temporal.Temporal;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalUnit;
import java.util.List;
import java.util.Objects;

public interface ChronoPeriod extends TemporalAmount {
    Temporal addTo(Temporal temporal);

    boolean equals(Object obj);

    long get(TemporalUnit temporalUnit);

    Chronology getChronology();

    List<TemporalUnit> getUnits();

    int hashCode();

    ChronoPeriod minus(TemporalAmount temporalAmount);

    ChronoPeriod multipliedBy(int i);

    ChronoPeriod normalized();

    ChronoPeriod plus(TemporalAmount temporalAmount);

    Temporal subtractFrom(Temporal temporal);

    String toString();

    static ChronoPeriod between(ChronoLocalDate startDateInclusive, ChronoLocalDate endDateExclusive) {
        Objects.requireNonNull((Object) startDateInclusive, "startDateInclusive");
        Objects.requireNonNull((Object) endDateExclusive, "endDateExclusive");
        return startDateInclusive.until(endDateExclusive);
    }

    boolean isZero() {
        for (TemporalUnit unit : getUnits()) {
            if (get(unit) != 0) {
                return false;
            }
        }
        return true;
    }

    boolean isNegative() {
        for (TemporalUnit unit : getUnits()) {
            if (get(unit) < 0) {
                return true;
            }
        }
        return false;
    }

    ChronoPeriod negated() {
        return multipliedBy(-1);
    }
}
