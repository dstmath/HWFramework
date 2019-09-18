package java.time;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
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
import java.util.Objects;

public final class Instant implements Temporal, TemporalAdjuster, Comparable<Instant>, Serializable {
    public static final Instant EPOCH = new Instant(0, 0);
    public static final Instant MAX = ofEpochSecond(MAX_SECOND, 999999999);
    private static final long MAX_SECOND = 31556889864403199L;
    public static final Instant MIN = ofEpochSecond(MIN_SECOND, 0);
    private static final long MIN_SECOND = -31557014167219200L;
    private static final long serialVersionUID = -665713676816604388L;
    private final int nanos;
    private final long seconds;

    public static Instant now() {
        return Clock.systemUTC().instant();
    }

    public static Instant now(Clock clock) {
        Objects.requireNonNull(clock, "clock");
        return clock.instant();
    }

    public static Instant ofEpochSecond(long epochSecond) {
        return create(epochSecond, 0);
    }

    public static Instant ofEpochSecond(long epochSecond, long nanoAdjustment) {
        return create(Math.addExact(epochSecond, Math.floorDiv(nanoAdjustment, 1000000000)), (int) Math.floorMod(nanoAdjustment, 1000000000));
    }

    public static Instant ofEpochMilli(long epochMilli) {
        return create(Math.floorDiv(epochMilli, 1000), 1000000 * ((int) Math.floorMod(epochMilli, 1000)));
    }

    public static Instant from(TemporalAccessor temporal) {
        if (temporal instanceof Instant) {
            return (Instant) temporal;
        }
        Objects.requireNonNull(temporal, "temporal");
        try {
            return ofEpochSecond(temporal.getLong(ChronoField.INSTANT_SECONDS), (long) temporal.get(ChronoField.NANO_OF_SECOND));
        } catch (DateTimeException ex) {
            throw new DateTimeException("Unable to obtain Instant from TemporalAccessor: " + temporal + " of type " + temporal.getClass().getName(), ex);
        }
    }

    public static Instant parse(CharSequence text) {
        return (Instant) DateTimeFormatter.ISO_INSTANT.parse(text, $$Lambda$PTL8WkLA4o1z4zIUBjrvwi808w.INSTANCE);
    }

    private static Instant create(long seconds2, int nanoOfSecond) {
        if ((((long) nanoOfSecond) | seconds2) == 0) {
            return EPOCH;
        }
        if (seconds2 >= MIN_SECOND && seconds2 <= MAX_SECOND) {
            return new Instant(seconds2, nanoOfSecond);
        }
        throw new DateTimeException("Instant exceeds minimum or maximum instant");
    }

    private Instant(long epochSecond, int nanos2) {
        this.seconds = epochSecond;
        this.nanos = nanos2;
    }

    public boolean isSupported(TemporalField field) {
        boolean z = true;
        if (field instanceof ChronoField) {
            if (!(field == ChronoField.INSTANT_SECONDS || field == ChronoField.NANO_OF_SECOND || field == ChronoField.MICRO_OF_SECOND || field == ChronoField.MILLI_OF_SECOND)) {
                z = false;
            }
            return z;
        }
        if (field == null || !field.isSupportedBy(this)) {
            z = false;
        }
        return z;
    }

    public boolean isSupported(TemporalUnit unit) {
        boolean z = true;
        if (unit instanceof ChronoUnit) {
            if (!unit.isTimeBased() && unit != ChronoUnit.DAYS) {
                z = false;
            }
            return z;
        }
        if (unit == null || !unit.isSupportedBy(this)) {
            z = false;
        }
        return z;
    }

    public ValueRange range(TemporalField field) {
        return super.range(field);
    }

    public int get(TemporalField field) {
        if (!(field instanceof ChronoField)) {
            return range(field).checkValidIntValue(field.getFrom(this), field);
        }
        switch ((ChronoField) field) {
            case NANO_OF_SECOND:
                return this.nanos;
            case MICRO_OF_SECOND:
                return this.nanos / 1000;
            case MILLI_OF_SECOND:
                return this.nanos / 1000000;
            case INSTANT_SECONDS:
                ChronoField.INSTANT_SECONDS.checkValidIntValue(this.seconds);
                break;
        }
        throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
    }

    public long getLong(TemporalField field) {
        if (!(field instanceof ChronoField)) {
            return field.getFrom(this);
        }
        switch ((ChronoField) field) {
            case NANO_OF_SECOND:
                return (long) this.nanos;
            case MICRO_OF_SECOND:
                return (long) (this.nanos / 1000);
            case MILLI_OF_SECOND:
                return (long) (this.nanos / 1000000);
            case INSTANT_SECONDS:
                return this.seconds;
            default:
                throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
        }
    }

    public long getEpochSecond() {
        return this.seconds;
    }

    public int getNano() {
        return this.nanos;
    }

    public Instant with(TemporalAdjuster adjuster) {
        return (Instant) adjuster.adjustInto(this);
    }

    public Instant with(TemporalField field, long newValue) {
        if (!(field instanceof ChronoField)) {
            return (Instant) field.adjustInto(this, newValue);
        }
        ChronoField f = (ChronoField) field;
        f.checkValidValue(newValue);
        switch (f) {
            case NANO_OF_SECOND:
                return newValue != ((long) this.nanos) ? create(this.seconds, (int) newValue) : this;
            case MICRO_OF_SECOND:
                int nval = ((int) newValue) * 1000;
                return nval != this.nanos ? create(this.seconds, nval) : this;
            case MILLI_OF_SECOND:
                int nval2 = ((int) newValue) * 1000000;
                return nval2 != this.nanos ? create(this.seconds, nval2) : this;
            case INSTANT_SECONDS:
                return newValue != this.seconds ? create(newValue, this.nanos) : this;
            default:
                throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
        }
    }

    public Instant truncatedTo(TemporalUnit unit) {
        if (unit == ChronoUnit.NANOS) {
            return this;
        }
        Duration unitDur = unit.getDuration();
        if (unitDur.getSeconds() <= 86400) {
            long dur = unitDur.toNanos();
            if (86400000000000L % dur == 0) {
                long nod = ((this.seconds % 86400) * 1000000000) + ((long) this.nanos);
                return plusNanos(((nod / dur) * dur) - nod);
            }
            throw new UnsupportedTemporalTypeException("Unit must divide into a standard day without remainder");
        }
        throw new UnsupportedTemporalTypeException("Unit is too large to be used for truncation");
    }

    public Instant plus(TemporalAmount amountToAdd) {
        return (Instant) amountToAdd.addTo(this);
    }

    public Instant plus(long amountToAdd, TemporalUnit unit) {
        if (!(unit instanceof ChronoUnit)) {
            return (Instant) unit.addTo(this, amountToAdd);
        }
        switch ((ChronoUnit) unit) {
            case NANOS:
                return plusNanos(amountToAdd);
            case MICROS:
                return plus(amountToAdd / 1000000, (amountToAdd % 1000000) * 1000);
            case MILLIS:
                return plusMillis(amountToAdd);
            case SECONDS:
                return plusSeconds(amountToAdd);
            case MINUTES:
                return plusSeconds(Math.multiplyExact(amountToAdd, 60));
            case HOURS:
                return plusSeconds(Math.multiplyExact(amountToAdd, 3600));
            case HALF_DAYS:
                return plusSeconds(Math.multiplyExact(amountToAdd, 43200));
            case DAYS:
                return plusSeconds(Math.multiplyExact(amountToAdd, 86400));
            default:
                throw new UnsupportedTemporalTypeException("Unsupported unit: " + unit);
        }
    }

    public Instant plusSeconds(long secondsToAdd) {
        return plus(secondsToAdd, 0);
    }

    public Instant plusMillis(long millisToAdd) {
        return plus(millisToAdd / 1000, (millisToAdd % 1000) * 1000000);
    }

    public Instant plusNanos(long nanosToAdd) {
        return plus(0, nanosToAdd);
    }

    private Instant plus(long secondsToAdd, long nanosToAdd) {
        if ((secondsToAdd | nanosToAdd) == 0) {
            return this;
        }
        return ofEpochSecond(Math.addExact(Math.addExact(this.seconds, secondsToAdd), nanosToAdd / 1000000000), ((long) this.nanos) + (nanosToAdd % 1000000000));
    }

    public Instant minus(TemporalAmount amountToSubtract) {
        return (Instant) amountToSubtract.subtractFrom(this);
    }

    public Instant minus(long amountToSubtract, TemporalUnit unit) {
        return amountToSubtract == Long.MIN_VALUE ? plus((long) Long.MAX_VALUE, unit).plus(1, unit) : plus(-amountToSubtract, unit);
    }

    public Instant minusSeconds(long secondsToSubtract) {
        if (secondsToSubtract == Long.MIN_VALUE) {
            return plusSeconds(Long.MAX_VALUE).plusSeconds(1);
        }
        return plusSeconds(-secondsToSubtract);
    }

    public Instant minusMillis(long millisToSubtract) {
        if (millisToSubtract == Long.MIN_VALUE) {
            return plusMillis(Long.MAX_VALUE).plusMillis(1);
        }
        return plusMillis(-millisToSubtract);
    }

    public Instant minusNanos(long nanosToSubtract) {
        if (nanosToSubtract == Long.MIN_VALUE) {
            return plusNanos(Long.MAX_VALUE).plusNanos(1);
        }
        return plusNanos(-nanosToSubtract);
    }

    public <R> R query(TemporalQuery<R> query) {
        if (query == TemporalQueries.precision()) {
            return ChronoUnit.NANOS;
        }
        if (query == TemporalQueries.chronology() || query == TemporalQueries.zoneId() || query == TemporalQueries.zone() || query == TemporalQueries.offset() || query == TemporalQueries.localDate() || query == TemporalQueries.localTime()) {
            return null;
        }
        return query.queryFrom(this);
    }

    public Temporal adjustInto(Temporal temporal) {
        return temporal.with(ChronoField.INSTANT_SECONDS, this.seconds).with(ChronoField.NANO_OF_SECOND, (long) this.nanos);
    }

    public long until(Temporal endExclusive, TemporalUnit unit) {
        Instant end = from(endExclusive);
        if (!(unit instanceof ChronoUnit)) {
            return unit.between(this, end);
        }
        switch ((ChronoUnit) unit) {
            case NANOS:
                return nanosUntil(end);
            case MICROS:
                return nanosUntil(end) / 1000;
            case MILLIS:
                return Math.subtractExact(end.toEpochMilli(), toEpochMilli());
            case SECONDS:
                return secondsUntil(end);
            case MINUTES:
                return secondsUntil(end) / 60;
            case HOURS:
                return secondsUntil(end) / 3600;
            case HALF_DAYS:
                return secondsUntil(end) / 43200;
            case DAYS:
                return secondsUntil(end) / 86400;
            default:
                throw new UnsupportedTemporalTypeException("Unsupported unit: " + unit);
        }
    }

    private long nanosUntil(Instant end) {
        return Math.addExact(Math.multiplyExact(Math.subtractExact(end.seconds, this.seconds), 1000000000), (long) (end.nanos - this.nanos));
    }

    private long secondsUntil(Instant end) {
        long secsDiff = Math.subtractExact(end.seconds, this.seconds);
        long nanosDiff = (long) (end.nanos - this.nanos);
        if (secsDiff > 0 && nanosDiff < 0) {
            return secsDiff - 1;
        }
        if (secsDiff >= 0 || nanosDiff <= 0) {
            return secsDiff;
        }
        return secsDiff + 1;
    }

    public OffsetDateTime atOffset(ZoneOffset offset) {
        return OffsetDateTime.ofInstant(this, offset);
    }

    public ZonedDateTime atZone(ZoneId zone) {
        return ZonedDateTime.ofInstant(this, zone);
    }

    public long toEpochMilli() {
        if (this.seconds >= 0 || this.nanos <= 0) {
            return Math.addExact(Math.multiplyExact(this.seconds, 1000), (long) (this.nanos / 1000000));
        }
        return Math.addExact(Math.multiplyExact(this.seconds + 1, 1000), (long) ((this.nanos / 1000000) - 1000));
    }

    public int compareTo(Instant otherInstant) {
        int cmp = Long.compare(this.seconds, otherInstant.seconds);
        if (cmp != 0) {
            return cmp;
        }
        return this.nanos - otherInstant.nanos;
    }

    public boolean isAfter(Instant otherInstant) {
        return compareTo(otherInstant) > 0;
    }

    public boolean isBefore(Instant otherInstant) {
        return compareTo(otherInstant) < 0;
    }

    public boolean equals(Object otherInstant) {
        boolean z = true;
        if (this == otherInstant) {
            return true;
        }
        if (!(otherInstant instanceof Instant)) {
            return false;
        }
        Instant other = (Instant) otherInstant;
        if (!(this.seconds == other.seconds && this.nanos == other.nanos)) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        return ((int) (this.seconds ^ (this.seconds >>> 32))) + (51 * this.nanos);
    }

    public String toString() {
        return DateTimeFormatter.ISO_INSTANT.format(this);
    }

    private Object writeReplace() {
        return new Ser((byte) 2, this);
    }

    private void readObject(ObjectInputStream s) throws InvalidObjectException {
        throw new InvalidObjectException("Deserialization via serialization delegate");
    }

    /* access modifiers changed from: package-private */
    public void writeExternal(DataOutput out) throws IOException {
        out.writeLong(this.seconds);
        out.writeInt(this.nanos);
    }

    static Instant readExternal(DataInput in) throws IOException {
        return ofEpochSecond(in.readLong(), (long) in.readInt());
    }
}
