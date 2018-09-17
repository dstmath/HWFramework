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
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalUnit;
import java.time.zone.ZoneOffsetTransition;
import java.time.zone.ZoneRules;
import java.util.List;
import java.util.Objects;

final class ChronoZonedDateTimeImpl<D extends ChronoLocalDate> implements ChronoZonedDateTime<D>, Serializable {
    private static final /* synthetic */ int[] -java-time-temporal-ChronoFieldSwitchesValues = null;
    private static final long serialVersionUID = -5261813987200935591L;
    private final transient ChronoLocalDateTimeImpl<D> dateTime;
    private final transient ZoneOffset offset;
    private final transient ZoneId zone;

    private static /* synthetic */ int[] -getjava-time-temporal-ChronoFieldSwitchesValues() {
        if (-java-time-temporal-ChronoFieldSwitchesValues != null) {
            return -java-time-temporal-ChronoFieldSwitchesValues;
        }
        int[] iArr = new int[ChronoField.values().length];
        try {
            iArr[ChronoField.ALIGNED_DAY_OF_WEEK_IN_MONTH.ordinal()] = 3;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[ChronoField.ALIGNED_DAY_OF_WEEK_IN_YEAR.ordinal()] = 4;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[ChronoField.ALIGNED_WEEK_OF_MONTH.ordinal()] = 5;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[ChronoField.ALIGNED_WEEK_OF_YEAR.ordinal()] = 6;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[ChronoField.AMPM_OF_DAY.ordinal()] = 7;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[ChronoField.CLOCK_HOUR_OF_AMPM.ordinal()] = 8;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[ChronoField.CLOCK_HOUR_OF_DAY.ordinal()] = 9;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[ChronoField.DAY_OF_MONTH.ordinal()] = 10;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[ChronoField.DAY_OF_WEEK.ordinal()] = 11;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[ChronoField.DAY_OF_YEAR.ordinal()] = 12;
        } catch (NoSuchFieldError e10) {
        }
        try {
            iArr[ChronoField.EPOCH_DAY.ordinal()] = 13;
        } catch (NoSuchFieldError e11) {
        }
        try {
            iArr[ChronoField.ERA.ordinal()] = 14;
        } catch (NoSuchFieldError e12) {
        }
        try {
            iArr[ChronoField.HOUR_OF_AMPM.ordinal()] = 15;
        } catch (NoSuchFieldError e13) {
        }
        try {
            iArr[ChronoField.HOUR_OF_DAY.ordinal()] = 16;
        } catch (NoSuchFieldError e14) {
        }
        try {
            iArr[ChronoField.INSTANT_SECONDS.ordinal()] = 1;
        } catch (NoSuchFieldError e15) {
        }
        try {
            iArr[ChronoField.MICRO_OF_DAY.ordinal()] = 17;
        } catch (NoSuchFieldError e16) {
        }
        try {
            iArr[ChronoField.MICRO_OF_SECOND.ordinal()] = 18;
        } catch (NoSuchFieldError e17) {
        }
        try {
            iArr[ChronoField.MILLI_OF_DAY.ordinal()] = 19;
        } catch (NoSuchFieldError e18) {
        }
        try {
            iArr[ChronoField.MILLI_OF_SECOND.ordinal()] = 20;
        } catch (NoSuchFieldError e19) {
        }
        try {
            iArr[ChronoField.MINUTE_OF_DAY.ordinal()] = 21;
        } catch (NoSuchFieldError e20) {
        }
        try {
            iArr[ChronoField.MINUTE_OF_HOUR.ordinal()] = 22;
        } catch (NoSuchFieldError e21) {
        }
        try {
            iArr[ChronoField.MONTH_OF_YEAR.ordinal()] = 23;
        } catch (NoSuchFieldError e22) {
        }
        try {
            iArr[ChronoField.NANO_OF_DAY.ordinal()] = 24;
        } catch (NoSuchFieldError e23) {
        }
        try {
            iArr[ChronoField.NANO_OF_SECOND.ordinal()] = 25;
        } catch (NoSuchFieldError e24) {
        }
        try {
            iArr[ChronoField.OFFSET_SECONDS.ordinal()] = 2;
        } catch (NoSuchFieldError e25) {
        }
        try {
            iArr[ChronoField.PROLEPTIC_MONTH.ordinal()] = 26;
        } catch (NoSuchFieldError e26) {
        }
        try {
            iArr[ChronoField.SECOND_OF_DAY.ordinal()] = 27;
        } catch (NoSuchFieldError e27) {
        }
        try {
            iArr[ChronoField.SECOND_OF_MINUTE.ordinal()] = 28;
        } catch (NoSuchFieldError e28) {
        }
        try {
            iArr[ChronoField.YEAR.ordinal()] = 29;
        } catch (NoSuchFieldError e29) {
        }
        try {
            iArr[ChronoField.YEAR_OF_ERA.ordinal()] = 30;
        } catch (NoSuchFieldError e30) {
        }
        -java-time-temporal-ChronoFieldSwitchesValues = iArr;
        return iArr;
    }

    static <R extends ChronoLocalDate> ChronoZonedDateTime<R> ofBest(ChronoLocalDateTimeImpl<R> localDateTime, ZoneId zone, ZoneOffset preferredOffset) {
        Objects.requireNonNull((Object) localDateTime, "localDateTime");
        Objects.requireNonNull((Object) zone, "zone");
        if (zone instanceof ZoneOffset) {
            return new ChronoZonedDateTimeImpl(localDateTime, (ZoneOffset) zone, zone);
        }
        Object offset;
        ZoneRules rules = zone.getRules();
        LocalDateTime isoLDT = LocalDateTime.from(localDateTime);
        List<ZoneOffset> validOffsets = rules.getValidOffsets(isoLDT);
        ZoneOffset offset2;
        if (validOffsets.size() == 1) {
            offset2 = (ZoneOffset) validOffsets.get(0);
        } else if (validOffsets.size() == 0) {
            ZoneOffsetTransition trans = rules.getTransition(isoLDT);
            localDateTime = localDateTime.plusSeconds(trans.getDuration().getSeconds());
            offset2 = trans.getOffsetAfter();
        } else if (preferredOffset == null || !validOffsets.contains(preferredOffset)) {
            offset2 = (ZoneOffset) validOffsets.get(0);
        } else {
            offset2 = preferredOffset;
        }
        Objects.requireNonNull(offset2, "offset");
        return new ChronoZonedDateTimeImpl(localDateTime, offset2, zone);
    }

    static ChronoZonedDateTimeImpl<?> ofInstant(Chronology chrono, Instant instant, ZoneId zone) {
        Object offset = zone.getRules().getOffset(instant);
        Objects.requireNonNull(offset, "offset");
        return new ChronoZonedDateTimeImpl((ChronoLocalDateTimeImpl) chrono.localDateTime(LocalDateTime.ofEpochSecond(instant.getEpochSecond(), instant.getNano(), offset)), offset, zone);
    }

    private ChronoZonedDateTimeImpl<D> create(Instant instant, ZoneId zone) {
        return ofInstant(getChronology(), instant, zone);
    }

    static <R extends ChronoLocalDate> ChronoZonedDateTimeImpl<R> ensureValid(Chronology chrono, Temporal temporal) {
        ChronoZonedDateTimeImpl<R> other = (ChronoZonedDateTimeImpl) temporal;
        if (chrono.equals(other.getChronology())) {
            return other;
        }
        throw new ClassCastException("Chronology mismatch, required: " + chrono.getId() + ", actual: " + other.getChronology().getId());
    }

    private ChronoZonedDateTimeImpl(ChronoLocalDateTimeImpl<D> dateTime, ZoneOffset offset, ZoneId zone) {
        this.dateTime = (ChronoLocalDateTimeImpl) Objects.requireNonNull((Object) dateTime, "dateTime");
        this.offset = (ZoneOffset) Objects.requireNonNull((Object) offset, "offset");
        this.zone = (ZoneId) Objects.requireNonNull((Object) zone, "zone");
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
            ZoneOffset offset = trans.getOffsetAfter();
            if (!offset.equals(getOffset())) {
                return new ChronoZonedDateTimeImpl(this.dateTime, offset, this.zone);
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

    public ChronoZonedDateTime<D> withZoneSameLocal(ZoneId zone) {
        return ofBest(this.dateTime, zone, this.offset);
    }

    public ChronoZonedDateTime<D> withZoneSameInstant(ZoneId zone) {
        Objects.requireNonNull((Object) zone, "zone");
        return this.zone.equals(zone) ? this : create(this.dateTime.toInstant(this.offset), zone);
    }

    public boolean isSupported(TemporalField field) {
        if (field instanceof ChronoField) {
            return true;
        }
        return field != null ? field.isSupportedBy(this) : false;
    }

    public ChronoZonedDateTime<D> with(TemporalField field, long newValue) {
        if (!(field instanceof ChronoField)) {
            return ensureValid(getChronology(), field.adjustInto(this, newValue));
        }
        ChronoField f = (ChronoField) field;
        switch (-getjava-time-temporal-ChronoFieldSwitchesValues()[f.ordinal()]) {
            case 1:
                return plus(newValue - toEpochSecond(), ChronoUnit.SECONDS);
            case 2:
                return create(this.dateTime.toInstant(ZoneOffset.ofTotalSeconds(f.checkValidIntValue(newValue))), this.zone);
            default:
                return ofBest(this.dateTime.with(field, newValue), this.zone, this.offset);
        }
    }

    public ChronoZonedDateTime<D> plus(long amountToAdd, TemporalUnit unit) {
        if (unit instanceof ChronoUnit) {
            return with(this.dateTime.plus(amountToAdd, unit));
        }
        return ensureValid(getChronology(), unit.addTo(this, amountToAdd));
    }

    public long until(Temporal endExclusive, TemporalUnit unit) {
        Objects.requireNonNull((Object) endExclusive, "endExclusive");
        ChronoZonedDateTime<D> end = getChronology().zonedDateTime(endExclusive);
        if (unit instanceof ChronoUnit) {
            return this.dateTime.until(end.withZoneSameInstant(this.offset).toLocalDateTime(), unit);
        }
        Objects.requireNonNull((Object) unit, "unit");
        return unit.between(this, end);
    }

    private Object writeReplace() {
        return new Ser((byte) 3, this);
    }

    private void readObject(ObjectInputStream s) throws InvalidObjectException {
        throw new InvalidObjectException("Deserialization via serialization delegate");
    }

    void writeExternal(ObjectOutput out) throws IOException {
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
        if (compareTo((ChronoZonedDateTime) obj) != 0) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        return (toLocalDateTime().hashCode() ^ getOffset().hashCode()) ^ Integer.rotateLeft(getZone().hashCode(), 3);
    }

    public String toString() {
        String str = toLocalDateTime().toString() + getOffset().toString();
        if (getOffset() != getZone()) {
            return str + '[' + getZone().toString() + ']';
        }
        return str;
    }
}
