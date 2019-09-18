package java.time.chrono;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.chrono.ChronoLocalDate;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalUnit;
import java.time.zone.ZoneOffsetTransition;
import java.time.zone.ZoneRules;
import java.util.List;
import java.util.Objects;

final class ChronoZonedDateTimeImpl<D extends ChronoLocalDate> implements ChronoZonedDateTime<D>, Serializable {
    private static final long serialVersionUID = -5261813987200935591L;
    private final transient ChronoLocalDateTimeImpl<D> dateTime;
    private final transient ZoneOffset offset;
    private final transient ZoneId zone;

    static <R extends ChronoLocalDate> ChronoZonedDateTime<R> ofBest(ChronoLocalDateTimeImpl<R> localDateTime, ZoneId zone2, ZoneOffset preferredOffset) {
        ZoneOffset offset2;
        Objects.requireNonNull(localDateTime, "localDateTime");
        Objects.requireNonNull(zone2, "zone");
        if (zone2 instanceof ZoneOffset) {
            return new ChronoZonedDateTimeImpl(localDateTime, (ZoneOffset) zone2, zone2);
        }
        ZoneRules rules = zone2.getRules();
        LocalDateTime isoLDT = LocalDateTime.from(localDateTime);
        List<ZoneOffset> validOffsets = rules.getValidOffsets(isoLDT);
        if (validOffsets.size() == 1) {
            offset2 = validOffsets.get(0);
        } else if (validOffsets.size() == 0) {
            ZoneOffsetTransition trans = rules.getTransition(isoLDT);
            localDateTime = localDateTime.plusSeconds(trans.getDuration().getSeconds());
            offset2 = trans.getOffsetAfter();
        } else if (preferredOffset == null || !validOffsets.contains(preferredOffset)) {
            offset2 = validOffsets.get(0);
        } else {
            offset2 = preferredOffset;
        }
        Objects.requireNonNull(offset2, "offset");
        return new ChronoZonedDateTimeImpl(localDateTime, offset2, zone2);
    }

    static ChronoZonedDateTimeImpl<?> ofInstant(Chronology chrono, Instant instant, ZoneId zone2) {
        ZoneOffset offset2 = zone2.getRules().getOffset(instant);
        Objects.requireNonNull(offset2, "offset");
        return new ChronoZonedDateTimeImpl<>((ChronoLocalDateTimeImpl) chrono.localDateTime(LocalDateTime.ofEpochSecond(instant.getEpochSecond(), instant.getNano(), offset2)), offset2, zone2);
    }

    private ChronoZonedDateTimeImpl<D> create(Instant instant, ZoneId zone2) {
        return ofInstant(getChronology(), instant, zone2);
    }

    static <R extends ChronoLocalDate> ChronoZonedDateTimeImpl<R> ensureValid(Chronology chrono, Temporal temporal) {
        ChronoZonedDateTimeImpl<R> other = (ChronoZonedDateTimeImpl) temporal;
        if (chrono.equals(other.getChronology())) {
            return other;
        }
        throw new ClassCastException("Chronology mismatch, required: " + chrono.getId() + ", actual: " + other.getChronology().getId());
    }

    private ChronoZonedDateTimeImpl(ChronoLocalDateTimeImpl<D> dateTime2, ZoneOffset offset2, ZoneId zone2) {
        this.dateTime = (ChronoLocalDateTimeImpl) Objects.requireNonNull(dateTime2, "dateTime");
        this.offset = (ZoneOffset) Objects.requireNonNull(offset2, "offset");
        this.zone = (ZoneId) Objects.requireNonNull(zone2, "zone");
    }

    public ZoneOffset getOffset() {
        return this.offset;
    }

    public ChronoZonedDateTime<D> withEarlierOffsetAtOverlap() {
        ZoneOffsetTransition trans = getZone().getRules().getTransition(LocalDateTime.from(this));
        if (trans != null && trans.isOverlap()) {
            ZoneOffset earlierOffset = trans.getOffsetBefore();
            if (!earlierOffset.equals(this.offset)) {
                return new ChronoZonedDateTimeImpl(this.dateTime, earlierOffset, this.zone);
            }
        }
        return this;
    }

    public ChronoZonedDateTime<D> withLaterOffsetAtOverlap() {
        ZoneOffsetTransition trans = getZone().getRules().getTransition(LocalDateTime.from(this));
        if (trans != null) {
            ZoneOffset offset2 = trans.getOffsetAfter();
            if (!offset2.equals(getOffset())) {
                return new ChronoZonedDateTimeImpl(this.dateTime, offset2, this.zone);
            }
        }
        return this;
    }

    public ChronoLocalDateTime<D> toLocalDateTime() {
        return this.dateTime;
    }

    public ZoneId getZone() {
        return this.zone;
    }

    public ChronoZonedDateTime<D> withZoneSameLocal(ZoneId zone2) {
        return ofBest(this.dateTime, zone2, this.offset);
    }

    public ChronoZonedDateTime<D> withZoneSameInstant(ZoneId zone2) {
        Objects.requireNonNull(zone2, "zone");
        return this.zone.equals(zone2) ? this : create(this.dateTime.toInstant(this.offset), zone2);
    }

    public boolean isSupported(TemporalField field) {
        return (field instanceof ChronoField) || (field != null && field.isSupportedBy(this));
    }

    public ChronoZonedDateTime<D> with(TemporalField field, long newValue) {
        if (!(field instanceof ChronoField)) {
            return ensureValid(getChronology(), field.adjustInto(this, newValue));
        }
        ChronoField f = (ChronoField) field;
        switch (f) {
            case INSTANT_SECONDS:
                return plus(newValue - toEpochSecond(), (TemporalUnit) ChronoUnit.SECONDS);
            case OFFSET_SECONDS:
                return create(this.dateTime.toInstant(ZoneOffset.ofTotalSeconds(f.checkValidIntValue(newValue))), this.zone);
            default:
                return ofBest(this.dateTime.with(field, newValue), this.zone, this.offset);
        }
    }

    public ChronoZonedDateTime<D> plus(long amountToAdd, TemporalUnit unit) {
        if (unit instanceof ChronoUnit) {
            return with((TemporalAdjuster) this.dateTime.plus(amountToAdd, unit));
        }
        return ensureValid(getChronology(), unit.addTo(this, amountToAdd));
    }

    public long until(Temporal endExclusive, TemporalUnit unit) {
        Objects.requireNonNull(endExclusive, "endExclusive");
        ChronoZonedDateTime<? extends ChronoLocalDate> zonedDateTime = getChronology().zonedDateTime(endExclusive);
        if (unit instanceof ChronoUnit) {
            return this.dateTime.until(zonedDateTime.withZoneSameInstant(this.offset).toLocalDateTime(), unit);
        }
        Objects.requireNonNull(unit, "unit");
        return unit.between(this, zonedDateTime);
    }

    private Object writeReplace() {
        return new Ser((byte) 3, this);
    }

    private void readObject(ObjectInputStream s) throws InvalidObjectException {
        throw new InvalidObjectException("Deserialization via serialization delegate");
    }

    /* access modifiers changed from: package-private */
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(this.dateTime);
        out.writeObject(this.offset);
        out.writeObject(this.zone);
    }

    static ChronoZonedDateTime<?> readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        return ((ChronoLocalDateTime) in.readObject()).atZone((ZoneOffset) in.readObject()).withZoneSameLocal((ZoneId) in.readObject());
    }

    public boolean equals(Object obj) {
        boolean z = true;
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ChronoZonedDateTime)) {
            return false;
        }
        if (compareTo((ChronoZonedDateTime<?>) (ChronoZonedDateTime) obj) != 0) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        return (toLocalDateTime().hashCode() ^ getOffset().hashCode()) ^ Integer.rotateLeft(getZone().hashCode(), 3);
    }

    public String toString() {
        String str = toLocalDateTime().toString() + getOffset().toString();
        if (getOffset() == getZone()) {
            return str;
        }
        return str + '[' + getZone().toString() + ']';
    }
}
