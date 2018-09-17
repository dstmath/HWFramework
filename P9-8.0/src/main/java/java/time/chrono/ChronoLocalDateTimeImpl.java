package java.time.chrono;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalUnit;
import java.time.temporal.ValueRange;
import java.util.Objects;
import sun.security.x509.InvalidityDateExtension;

final class ChronoLocalDateTimeImpl<D extends ChronoLocalDate> implements ChronoLocalDateTime<D>, Temporal, TemporalAdjuster, Serializable {
    private static final /* synthetic */ int[] -java-time-temporal-ChronoUnitSwitchesValues = null;
    static final int HOURS_PER_DAY = 24;
    static final long MICROS_PER_DAY = 86400000000L;
    static final long MILLIS_PER_DAY = 86400000;
    static final int MINUTES_PER_DAY = 1440;
    static final int MINUTES_PER_HOUR = 60;
    static final long NANOS_PER_DAY = 86400000000000L;
    static final long NANOS_PER_HOUR = 3600000000000L;
    static final long NANOS_PER_MINUTE = 60000000000L;
    static final long NANOS_PER_SECOND = 1000000000;
    static final int SECONDS_PER_DAY = 86400;
    static final int SECONDS_PER_HOUR = 3600;
    static final int SECONDS_PER_MINUTE = 60;
    private static final long serialVersionUID = 4556003607393004514L;
    private final transient D date;
    private final transient LocalTime time;

    private static /* synthetic */ int[] -getjava-time-temporal-ChronoUnitSwitchesValues() {
        if (-java-time-temporal-ChronoUnitSwitchesValues != null) {
            return -java-time-temporal-ChronoUnitSwitchesValues;
        }
        int[] iArr = new int[ChronoUnit.values().length];
        try {
            iArr[ChronoUnit.CENTURIES.ordinal()] = 8;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[ChronoUnit.DAYS.ordinal()] = 9;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[ChronoUnit.DECADES.ordinal()] = 10;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[ChronoUnit.ERAS.ordinal()] = 11;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[ChronoUnit.FOREVER.ordinal()] = 12;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[ChronoUnit.HALF_DAYS.ordinal()] = 1;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[ChronoUnit.HOURS.ordinal()] = 2;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[ChronoUnit.MICROS.ordinal()] = 3;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[ChronoUnit.MILLENNIA.ordinal()] = 13;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[ChronoUnit.MILLIS.ordinal()] = 4;
        } catch (NoSuchFieldError e10) {
        }
        try {
            iArr[ChronoUnit.MINUTES.ordinal()] = 5;
        } catch (NoSuchFieldError e11) {
        }
        try {
            iArr[ChronoUnit.MONTHS.ordinal()] = 14;
        } catch (NoSuchFieldError e12) {
        }
        try {
            iArr[ChronoUnit.NANOS.ordinal()] = 6;
        } catch (NoSuchFieldError e13) {
        }
        try {
            iArr[ChronoUnit.SECONDS.ordinal()] = 7;
        } catch (NoSuchFieldError e14) {
        }
        try {
            iArr[ChronoUnit.WEEKS.ordinal()] = 15;
        } catch (NoSuchFieldError e15) {
        }
        try {
            iArr[ChronoUnit.YEARS.ordinal()] = 16;
        } catch (NoSuchFieldError e16) {
        }
        -java-time-temporal-ChronoUnitSwitchesValues = iArr;
        return iArr;
    }

    static <R extends ChronoLocalDate> ChronoLocalDateTimeImpl<R> of(R date, LocalTime time) {
        return new ChronoLocalDateTimeImpl(date, time);
    }

    static <R extends ChronoLocalDate> ChronoLocalDateTimeImpl<R> ensureValid(Chronology chrono, Temporal temporal) {
        ChronoLocalDateTimeImpl<R> other = (ChronoLocalDateTimeImpl) temporal;
        if (chrono.equals(other.getChronology())) {
            return other;
        }
        throw new ClassCastException("Chronology mismatch, required: " + chrono.getId() + ", actual: " + other.getChronology().getId());
    }

    private ChronoLocalDateTimeImpl(D date, LocalTime time) {
        Objects.requireNonNull((Object) date, InvalidityDateExtension.DATE);
        Objects.requireNonNull((Object) time, "time");
        this.date = date;
        this.time = time;
    }

    private ChronoLocalDateTimeImpl<D> with(Temporal newDate, LocalTime newTime) {
        if (this.date == newDate && this.time == newTime) {
            return this;
        }
        return new ChronoLocalDateTimeImpl(ChronoLocalDateImpl.ensureValid(this.date.getChronology(), newDate), newTime);
    }

    public D toLocalDate() {
        return this.date;
    }

    public LocalTime toLocalTime() {
        return this.time;
    }

    public boolean isSupported(TemporalField field) {
        if (field instanceof ChronoField) {
            ChronoField f = (ChronoField) field;
            return !f.isDateBased() ? f.isTimeBased() : true;
        }
        return field != null ? field.isSupportedBy(this) : false;
    }

    public ValueRange range(TemporalField field) {
        if (!(field instanceof ChronoField)) {
            return field.rangeRefinedBy(this);
        }
        return ((ChronoField) field).isTimeBased() ? this.time.range(field) : this.date.range(field);
    }

    public int get(TemporalField field) {
        if (!(field instanceof ChronoField)) {
            return range(field).checkValidIntValue(getLong(field), field);
        }
        return ((ChronoField) field).isTimeBased() ? this.time.get(field) : this.date.get(field);
    }

    public long getLong(TemporalField field) {
        if (!(field instanceof ChronoField)) {
            return field.getFrom(this);
        }
        return ((ChronoField) field).isTimeBased() ? this.time.getLong(field) : this.date.getLong(field);
    }

    public ChronoLocalDateTimeImpl<D> with(TemporalAdjuster adjuster) {
        if (adjuster instanceof ChronoLocalDate) {
            return with((ChronoLocalDate) adjuster, this.time);
        }
        if (adjuster instanceof LocalTime) {
            return with(this.date, (LocalTime) adjuster);
        }
        if (adjuster instanceof ChronoLocalDateTimeImpl) {
            return ensureValid(this.date.getChronology(), (ChronoLocalDateTimeImpl) adjuster);
        }
        return ensureValid(this.date.getChronology(), (ChronoLocalDateTimeImpl) adjuster.adjustInto(this));
    }

    public ChronoLocalDateTimeImpl<D> with(TemporalField field, long newValue) {
        if (!(field instanceof ChronoField)) {
            return ensureValid(this.date.getChronology(), field.adjustInto(this, newValue));
        }
        if (((ChronoField) field).isTimeBased()) {
            return with(this.date, this.time.with(field, newValue));
        }
        return with(this.date.with(field, newValue), this.time);
    }

    public ChronoLocalDateTimeImpl<D> plus(long amountToAdd, TemporalUnit unit) {
        if (!(unit instanceof ChronoUnit)) {
            return ensureValid(this.date.getChronology(), unit.addTo(this, amountToAdd));
        }
        switch (-getjava-time-temporal-ChronoUnitSwitchesValues()[((ChronoUnit) unit).ordinal()]) {
            case 1:
                return plusDays(amountToAdd / 256).plusHours((amountToAdd % 256) * 12);
            case 2:
                return plusHours(amountToAdd);
            case 3:
                return plusDays(amountToAdd / MICROS_PER_DAY).plusNanos((amountToAdd % MICROS_PER_DAY) * 1000);
            case 4:
                return plusDays(amountToAdd / MILLIS_PER_DAY).plusNanos((amountToAdd % MILLIS_PER_DAY) * 1000000);
            case 5:
                return plusMinutes(amountToAdd);
            case 6:
                return plusNanos(amountToAdd);
            case 7:
                return plusSeconds(amountToAdd);
            default:
                return with(this.date.plus(amountToAdd, unit), this.time);
        }
    }

    private ChronoLocalDateTimeImpl<D> plusDays(long days) {
        return with(this.date.plus(days, ChronoUnit.DAYS), this.time);
    }

    private ChronoLocalDateTimeImpl<D> plusHours(long hours) {
        return plusWithOverflow(this.date, hours, 0, 0, 0);
    }

    private ChronoLocalDateTimeImpl<D> plusMinutes(long minutes) {
        return plusWithOverflow(this.date, 0, minutes, 0, 0);
    }

    ChronoLocalDateTimeImpl<D> plusSeconds(long seconds) {
        return plusWithOverflow(this.date, 0, 0, seconds, 0);
    }

    private ChronoLocalDateTimeImpl<D> plusNanos(long nanos) {
        return plusWithOverflow(this.date, 0, 0, 0, nanos);
    }

    private ChronoLocalDateTimeImpl<D> plusWithOverflow(D newDate, long hours, long minutes, long seconds, long nanos) {
        if ((((hours | minutes) | seconds) | nanos) == 0) {
            return with((Temporal) newDate, this.time);
        }
        long totDays = (((nanos / NANOS_PER_DAY) + (seconds / 86400)) + (minutes / 1440)) + (hours / 24);
        long totNanos = (((nanos % NANOS_PER_DAY) + ((seconds % 86400) * NANOS_PER_SECOND)) + ((minutes % 1440) * NANOS_PER_MINUTE)) + ((hours % 24) * NANOS_PER_HOUR);
        long curNoD = this.time.toNanoOfDay();
        totNanos += curNoD;
        totDays += Math.floorDiv(totNanos, (long) NANOS_PER_DAY);
        long newNoD = Math.floorMod(totNanos, (long) NANOS_PER_DAY);
        return with(newDate.plus(totDays, ChronoUnit.DAYS), newNoD == curNoD ? this.time : LocalTime.ofNanoOfDay(newNoD));
    }

    public ChronoZonedDateTime<D> atZone(ZoneId zone) {
        return ChronoZonedDateTimeImpl.ofBest(this, zone, null);
    }

    public long until(Temporal endExclusive, TemporalUnit unit) {
        Objects.requireNonNull((Object) endExclusive, "endExclusive");
        ChronoLocalDateTime<D> end = getChronology().localDateTime(endExclusive);
        if (!(unit instanceof ChronoUnit)) {
            Objects.requireNonNull((Object) unit, "unit");
            return unit.between(this, end);
        } else if (unit.isTimeBased()) {
            long amount = end.getLong(ChronoField.EPOCH_DAY) - this.date.getLong(ChronoField.EPOCH_DAY);
            switch (-getjava-time-temporal-ChronoUnitSwitchesValues()[((ChronoUnit) unit).ordinal()]) {
                case 1:
                    amount = Math.multiplyExact(amount, 2);
                    break;
                case 2:
                    amount = Math.multiplyExact(amount, 24);
                    break;
                case 3:
                    amount = Math.multiplyExact(amount, (long) MICROS_PER_DAY);
                    break;
                case 4:
                    amount = Math.multiplyExact(amount, (long) MILLIS_PER_DAY);
                    break;
                case 5:
                    amount = Math.multiplyExact(amount, 1440);
                    break;
                case 6:
                    amount = Math.multiplyExact(amount, (long) NANOS_PER_DAY);
                    break;
                case 7:
                    amount = Math.multiplyExact(amount, 86400);
                    break;
            }
            return Math.addExact(amount, this.time.until(end.toLocalTime(), unit));
        } else {
            ChronoLocalDate endDate = end.toLocalDate();
            if (end.toLocalTime().isBefore(this.time)) {
                endDate = endDate.minus(1, ChronoUnit.DAYS);
            }
            return this.date.until(endDate, unit);
        }
    }

    private Object writeReplace() {
        return new Ser((byte) 2, this);
    }

    private void readObject(ObjectInputStream s) throws InvalidObjectException {
        throw new InvalidObjectException("Deserialization via serialization delegate");
    }

    void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(this.date);
        out.writeObject(this.time);
    }

    static ChronoLocalDateTime<?> readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        return ((ChronoLocalDate) in.readObject()).atTime((LocalTime) in.readObject());
    }

    public boolean equals(Object obj) {
        boolean z = true;
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ChronoLocalDateTime)) {
            return false;
        }
        if (compareTo((ChronoLocalDateTime) obj) != 0) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        return toLocalDate().hashCode() ^ toLocalTime().hashCode();
    }

    public String toString() {
        return toLocalDate().toString() + 'T' + toLocalTime().toString();
    }
}
