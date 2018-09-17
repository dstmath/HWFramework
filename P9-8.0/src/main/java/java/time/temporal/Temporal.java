package java.time.temporal;

public interface Temporal extends TemporalAccessor {
    boolean isSupported(TemporalUnit temporalUnit);

    Temporal plus(long j, TemporalUnit temporalUnit);

    long until(Temporal temporal, TemporalUnit temporalUnit);

    Temporal with(TemporalField temporalField, long j);

    Temporal with(TemporalAdjuster adjuster) {
        return adjuster.adjustInto(this);
    }

    Temporal plus(TemporalAmount amount) {
        return amount.addTo(this);
    }

    Temporal minus(TemporalAmount amount) {
        return amount.subtractFrom(this);
    }

    Temporal minus(long amountToSubtract, TemporalUnit unit) {
        return amountToSubtract == Long.MIN_VALUE ? plus(Long.MAX_VALUE, unit).plus(1, unit) : plus(-amountToSubtract, unit);
    }
}
