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

public final class LocalTime implements Temporal, TemporalAdjuster, Comparable<LocalTime>, Serializable {
    private static final LocalTime[] HOURS = new LocalTime[24];
    static final int HOURS_PER_DAY = 24;
    public static final LocalTime MAX = new LocalTime(23, 59, 59, Year.MAX_VALUE);
    static final long MICROS_PER_DAY = 86400000000L;
    public static final LocalTime MIDNIGHT = HOURS[0];
    static final long MILLIS_PER_DAY = 86400000;
    public static final LocalTime MIN = HOURS[0];
    static final int MINUTES_PER_DAY = 1440;
    static final int MINUTES_PER_HOUR = 60;
    static final long NANOS_PER_DAY = 86400000000000L;
    static final long NANOS_PER_HOUR = 3600000000000L;
    static final long NANOS_PER_MINUTE = 60000000000L;
    static final long NANOS_PER_SECOND = 1000000000;
    public static final LocalTime NOON = HOURS[12];
    static final int SECONDS_PER_DAY = 86400;
    static final int SECONDS_PER_HOUR = 3600;
    static final int SECONDS_PER_MINUTE = 60;
    private static final long serialVersionUID = 6414437269572265201L;
    private final byte hour;
    private final byte minute;
    private final int nano;
    private final byte second;

    static {
        for (int i = 0; i < HOURS.length; i++) {
            HOURS[i] = new LocalTime(i, 0, 0, 0);
        }
    }

    public static LocalTime now() {
        return now(Clock.systemDefaultZone());
    }

    public static LocalTime now(ZoneId zone) {
        return now(Clock.system(zone));
    }

    public static LocalTime now(Clock clock) {
        Objects.requireNonNull(clock, "clock");
        Instant now = clock.instant();
        return ofNanoOfDay((((long) ((int) Math.floorMod(now.getEpochSecond() + ((long) clock.getZone().getRules().getOffset(now).getTotalSeconds()), 86400))) * NANOS_PER_SECOND) + ((long) now.getNano()));
    }

    public static LocalTime of(int hour2, int minute2) {
        ChronoField.HOUR_OF_DAY.checkValidValue((long) hour2);
        if (minute2 == 0) {
            return HOURS[hour2];
        }
        ChronoField.MINUTE_OF_HOUR.checkValidValue((long) minute2);
        return new LocalTime(hour2, minute2, 0, 0);
    }

    public static LocalTime of(int hour2, int minute2, int second2) {
        ChronoField.HOUR_OF_DAY.checkValidValue((long) hour2);
        if ((minute2 | second2) == 0) {
            return HOURS[hour2];
        }
        ChronoField.MINUTE_OF_HOUR.checkValidValue((long) minute2);
        ChronoField.SECOND_OF_MINUTE.checkValidValue((long) second2);
        return new LocalTime(hour2, minute2, second2, 0);
    }

    public static LocalTime of(int hour2, int minute2, int second2, int nanoOfSecond) {
        ChronoField.HOUR_OF_DAY.checkValidValue((long) hour2);
        ChronoField.MINUTE_OF_HOUR.checkValidValue((long) minute2);
        ChronoField.SECOND_OF_MINUTE.checkValidValue((long) second2);
        ChronoField.NANO_OF_SECOND.checkValidValue((long) nanoOfSecond);
        return create(hour2, minute2, second2, nanoOfSecond);
    }

    public static LocalTime ofSecondOfDay(long secondOfDay) {
        ChronoField.SECOND_OF_DAY.checkValidValue(secondOfDay);
        int hours = (int) (secondOfDay / 3600);
        long secondOfDay2 = secondOfDay - ((long) (hours * SECONDS_PER_HOUR));
        int minutes = (int) (secondOfDay2 / 60);
        return create(hours, minutes, (int) (secondOfDay2 - ((long) (minutes * 60))), 0);
    }

    public static LocalTime ofNanoOfDay(long nanoOfDay) {
        ChronoField.NANO_OF_DAY.checkValidValue(nanoOfDay);
        int hours = (int) (nanoOfDay / NANOS_PER_HOUR);
        long nanoOfDay2 = nanoOfDay - (((long) hours) * NANOS_PER_HOUR);
        int minutes = (int) (nanoOfDay2 / NANOS_PER_MINUTE);
        long nanoOfDay3 = nanoOfDay2 - (((long) minutes) * NANOS_PER_MINUTE);
        int seconds = (int) (nanoOfDay3 / NANOS_PER_SECOND);
        return create(hours, minutes, seconds, (int) (nanoOfDay3 - (((long) seconds) * NANOS_PER_SECOND)));
    }

    public static LocalTime from(TemporalAccessor temporal) {
        Objects.requireNonNull(temporal, "temporal");
        LocalTime time = (LocalTime) temporal.query(TemporalQueries.localTime());
        if (time != null) {
            return time;
        }
        throw new DateTimeException("Unable to obtain LocalTime from TemporalAccessor: " + temporal + " of type " + temporal.getClass().getName());
    }

    public static LocalTime parse(CharSequence text) {
        return parse(text, DateTimeFormatter.ISO_LOCAL_TIME);
    }

    public static LocalTime parse(CharSequence text, DateTimeFormatter formatter) {
        Objects.requireNonNull(formatter, "formatter");
        return (LocalTime) formatter.parse(text, $$Lambda$2Dm_gBEmfWAFyI8wDj_HTrgAgUc.INSTANCE);
    }

    private static LocalTime create(int hour2, int minute2, int second2, int nanoOfSecond) {
        if ((minute2 | second2 | nanoOfSecond) == 0) {
            return HOURS[hour2];
        }
        return new LocalTime(hour2, minute2, second2, nanoOfSecond);
    }

    private LocalTime(int hour2, int minute2, int second2, int nanoOfSecond) {
        this.hour = (byte) hour2;
        this.minute = (byte) minute2;
        this.second = (byte) second2;
        this.nano = nanoOfSecond;
    }

    public boolean isSupported(TemporalField field) {
        if (field instanceof ChronoField) {
            return field.isTimeBased();
        }
        return field != null && field.isSupportedBy(this);
    }

    public boolean isSupported(TemporalUnit unit) {
        if (unit instanceof ChronoUnit) {
            return unit.isTimeBased();
        }
        return unit != null && unit.isSupportedBy(this);
    }

    public ValueRange range(TemporalField field) {
        return super.range(field);
    }

    public int get(TemporalField field) {
        if (field instanceof ChronoField) {
            return get0(field);
        }
        return super.get(field);
    }

    public long getLong(TemporalField field) {
        if (!(field instanceof ChronoField)) {
            return field.getFrom(this);
        }
        if (field == ChronoField.NANO_OF_DAY) {
            return toNanoOfDay();
        }
        if (field == ChronoField.MICRO_OF_DAY) {
            return toNanoOfDay() / 1000;
        }
        return (long) get0(field);
    }

    private int get0(TemporalField field) {
        int i = 12;
        switch ((ChronoField) field) {
            case NANO_OF_SECOND:
                return this.nano;
            case NANO_OF_DAY:
                throw new UnsupportedTemporalTypeException("Invalid field 'NanoOfDay' for get() method, use getLong() instead");
            case MICRO_OF_SECOND:
                return this.nano / 1000;
            case MICRO_OF_DAY:
                throw new UnsupportedTemporalTypeException("Invalid field 'MicroOfDay' for get() method, use getLong() instead");
            case MILLI_OF_SECOND:
                return this.nano / 1000000;
            case MILLI_OF_DAY:
                return (int) (toNanoOfDay() / 1000000);
            case SECOND_OF_MINUTE:
                return this.second;
            case SECOND_OF_DAY:
                return toSecondOfDay();
            case MINUTE_OF_HOUR:
                return this.minute;
            case MINUTE_OF_DAY:
                return (this.hour * 60) + this.minute;
            case HOUR_OF_AMPM:
                return this.hour % 12;
            case CLOCK_HOUR_OF_AMPM:
                int ham = this.hour % 12;
                if (ham % 12 != 0) {
                    i = ham;
                }
                return i;
            case HOUR_OF_DAY:
                return this.hour;
            case CLOCK_HOUR_OF_DAY:
                return this.hour == 0 ? 24 : this.hour;
            case AMPM_OF_DAY:
                return this.hour / 12;
            default:
                throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
        }
    }

    public int getHour() {
        return this.hour;
    }

    public int getMinute() {
        return this.minute;
    }

    public int getSecond() {
        return this.second;
    }

    public int getNano() {
        return this.nano;
    }

    public LocalTime with(TemporalAdjuster adjuster) {
        if (adjuster instanceof LocalTime) {
            return (LocalTime) adjuster;
        }
        return (LocalTime) adjuster.adjustInto(this);
    }

    public LocalTime with(TemporalField field, long newValue) {
        if (!(field instanceof ChronoField)) {
            return (LocalTime) field.adjustInto(this, newValue);
        }
        ChronoField f = (ChronoField) field;
        f.checkValidValue(newValue);
        long j = 0;
        switch (f) {
            case NANO_OF_SECOND:
                return withNano((int) newValue);
            case NANO_OF_DAY:
                return ofNanoOfDay(newValue);
            case MICRO_OF_SECOND:
                return withNano(((int) newValue) * 1000);
            case MICRO_OF_DAY:
                return ofNanoOfDay(1000 * newValue);
            case MILLI_OF_SECOND:
                return withNano(((int) newValue) * 1000000);
            case MILLI_OF_DAY:
                return ofNanoOfDay(1000000 * newValue);
            case SECOND_OF_MINUTE:
                return withSecond((int) newValue);
            case SECOND_OF_DAY:
                return plusSeconds(newValue - ((long) toSecondOfDay()));
            case MINUTE_OF_HOUR:
                return withMinute((int) newValue);
            case MINUTE_OF_DAY:
                return plusMinutes(newValue - ((long) ((this.hour * 60) + this.minute)));
            case HOUR_OF_AMPM:
                return plusHours(newValue - ((long) (this.hour % 12)));
            case CLOCK_HOUR_OF_AMPM:
                if (newValue != 12) {
                    j = newValue;
                }
                return plusHours(j - ((long) (this.hour % 12)));
            case HOUR_OF_DAY:
                return withHour((int) newValue);
            case CLOCK_HOUR_OF_DAY:
                if (newValue != 24) {
                    j = newValue;
                }
                return withHour((int) j);
            case AMPM_OF_DAY:
                return plusHours((newValue - ((long) (this.hour / 12))) * 12);
            default:
                throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
        }
    }

    public LocalTime withHour(int hour2) {
        if (this.hour == hour2) {
            return this;
        }
        ChronoField.HOUR_OF_DAY.checkValidValue((long) hour2);
        return create(hour2, this.minute, this.second, this.nano);
    }

    public LocalTime withMinute(int minute2) {
        if (this.minute == minute2) {
            return this;
        }
        ChronoField.MINUTE_OF_HOUR.checkValidValue((long) minute2);
        return create(this.hour, minute2, this.second, this.nano);
    }

    public LocalTime withSecond(int second2) {
        if (this.second == second2) {
            return this;
        }
        ChronoField.SECOND_OF_MINUTE.checkValidValue((long) second2);
        return create(this.hour, this.minute, second2, this.nano);
    }

    public LocalTime withNano(int nanoOfSecond) {
        if (this.nano == nanoOfSecond) {
            return this;
        }
        ChronoField.NANO_OF_SECOND.checkValidValue((long) nanoOfSecond);
        return create(this.hour, this.minute, this.second, nanoOfSecond);
    }

    public LocalTime truncatedTo(TemporalUnit unit) {
        if (unit == ChronoUnit.NANOS) {
            return this;
        }
        Duration unitDur = unit.getDuration();
        if (unitDur.getSeconds() <= 86400) {
            long dur = unitDur.toNanos();
            if (NANOS_PER_DAY % dur == 0) {
                return ofNanoOfDay((toNanoOfDay() / dur) * dur);
            }
            throw new UnsupportedTemporalTypeException("Unit must divide into a standard day without remainder");
        }
        throw new UnsupportedTemporalTypeException("Unit is too large to be used for truncation");
    }

    public LocalTime plus(TemporalAmount amountToAdd) {
        return (LocalTime) amountToAdd.addTo(this);
    }

    public LocalTime plus(long amountToAdd, TemporalUnit unit) {
        if (!(unit instanceof ChronoUnit)) {
            return (LocalTime) unit.addTo(this, amountToAdd);
        }
        switch ((ChronoUnit) unit) {
            case NANOS:
                return plusNanos(amountToAdd);
            case MICROS:
                return plusNanos((amountToAdd % MICROS_PER_DAY) * 1000);
            case MILLIS:
                return plusNanos((amountToAdd % MILLIS_PER_DAY) * 1000000);
            case SECONDS:
                return plusSeconds(amountToAdd);
            case MINUTES:
                return plusMinutes(amountToAdd);
            case HOURS:
                return plusHours(amountToAdd);
            case HALF_DAYS:
                return plusHours((amountToAdd % 2) * 12);
            default:
                throw new UnsupportedTemporalTypeException("Unsupported unit: " + unit);
        }
    }

    public LocalTime plusHours(long hoursToAdd) {
        if (hoursToAdd == 0) {
            return this;
        }
        return create(((((int) (hoursToAdd % 24)) + this.hour) + 24) % 24, this.minute, this.second, this.nano);
    }

    public LocalTime plusMinutes(long minutesToAdd) {
        if (minutesToAdd == 0) {
            return this;
        }
        int mofd = (this.hour * 60) + this.minute;
        int newMofd = ((((int) (minutesToAdd % 1440)) + mofd) + MINUTES_PER_DAY) % MINUTES_PER_DAY;
        if (mofd == newMofd) {
            return this;
        }
        return create(newMofd / 60, newMofd % 60, this.second, this.nano);
    }

    public LocalTime plusSeconds(long secondstoAdd) {
        if (secondstoAdd == 0) {
            return this;
        }
        int sofd = (this.hour * 3600) + (this.minute * 60) + this.second;
        int newSofd = ((((int) (secondstoAdd % 86400)) + sofd) + SECONDS_PER_DAY) % SECONDS_PER_DAY;
        if (sofd == newSofd) {
            return this;
        }
        return create(newSofd / SECONDS_PER_HOUR, (newSofd / 60) % 60, newSofd % 60, this.nano);
    }

    public LocalTime plusNanos(long nanosToAdd) {
        if (nanosToAdd == 0) {
            return this;
        }
        long nofd = toNanoOfDay();
        long newNofd = (((nanosToAdd % NANOS_PER_DAY) + nofd) + NANOS_PER_DAY) % NANOS_PER_DAY;
        if (nofd == newNofd) {
            return this;
        }
        return create((int) (newNofd / NANOS_PER_HOUR), (int) ((newNofd / NANOS_PER_MINUTE) % 60), (int) ((newNofd / NANOS_PER_SECOND) % 60), (int) (newNofd % NANOS_PER_SECOND));
    }

    public LocalTime minus(TemporalAmount amountToSubtract) {
        return (LocalTime) amountToSubtract.subtractFrom(this);
    }

    public LocalTime minus(long amountToSubtract, TemporalUnit unit) {
        return amountToSubtract == Long.MIN_VALUE ? plus((long) Long.MAX_VALUE, unit).plus(1, unit) : plus(-amountToSubtract, unit);
    }

    public LocalTime minusHours(long hoursToSubtract) {
        return plusHours(-(hoursToSubtract % 24));
    }

    public LocalTime minusMinutes(long minutesToSubtract) {
        return plusMinutes(-(minutesToSubtract % 1440));
    }

    public LocalTime minusSeconds(long secondsToSubtract) {
        return plusSeconds(-(secondsToSubtract % 86400));
    }

    public LocalTime minusNanos(long nanosToSubtract) {
        return plusNanos(-(nanosToSubtract % NANOS_PER_DAY));
    }

    public <R> R query(TemporalQuery<R> query) {
        if (query == TemporalQueries.chronology() || query == TemporalQueries.zoneId() || query == TemporalQueries.zone() || query == TemporalQueries.offset()) {
            return null;
        }
        if (query == TemporalQueries.localTime()) {
            return this;
        }
        if (query == TemporalQueries.localDate()) {
            return null;
        }
        if (query == TemporalQueries.precision()) {
            return ChronoUnit.NANOS;
        }
        return query.queryFrom(this);
    }

    public Temporal adjustInto(Temporal temporal) {
        return temporal.with(ChronoField.NANO_OF_DAY, toNanoOfDay());
    }

    public long until(Temporal endExclusive, TemporalUnit unit) {
        LocalTime end = from(endExclusive);
        if (!(unit instanceof ChronoUnit)) {
            return unit.between(this, end);
        }
        long nanosUntil = end.toNanoOfDay() - toNanoOfDay();
        switch ((ChronoUnit) unit) {
            case NANOS:
                return nanosUntil;
            case MICROS:
                return nanosUntil / 1000;
            case MILLIS:
                return nanosUntil / 1000000;
            case SECONDS:
                return nanosUntil / NANOS_PER_SECOND;
            case MINUTES:
                return nanosUntil / NANOS_PER_MINUTE;
            case HOURS:
                return nanosUntil / NANOS_PER_HOUR;
            case HALF_DAYS:
                return nanosUntil / 43200000000000L;
            default:
                throw new UnsupportedTemporalTypeException("Unsupported unit: " + unit);
        }
    }

    public String format(DateTimeFormatter formatter) {
        Objects.requireNonNull(formatter, "formatter");
        return formatter.format(this);
    }

    public LocalDateTime atDate(LocalDate date) {
        return LocalDateTime.of(date, this);
    }

    public OffsetTime atOffset(ZoneOffset offset) {
        return OffsetTime.of(this, offset);
    }

    public int toSecondOfDay() {
        return (this.hour * 3600) + (this.minute * 60) + this.second;
    }

    public long toNanoOfDay() {
        return (((long) this.hour) * NANOS_PER_HOUR) + (((long) this.minute) * NANOS_PER_MINUTE) + (((long) this.second) * NANOS_PER_SECOND) + ((long) this.nano);
    }

    public int compareTo(LocalTime other) {
        int cmp = Integer.compare(this.hour, other.hour);
        if (cmp != 0) {
            return cmp;
        }
        int cmp2 = Integer.compare(this.minute, other.minute);
        if (cmp2 != 0) {
            return cmp2;
        }
        int cmp3 = Integer.compare(this.second, other.second);
        if (cmp3 == 0) {
            return Integer.compare(this.nano, other.nano);
        }
        return cmp3;
    }

    public boolean isAfter(LocalTime other) {
        return compareTo(other) > 0;
    }

    public boolean isBefore(LocalTime other) {
        return compareTo(other) < 0;
    }

    public boolean equals(Object obj) {
        boolean z = true;
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof LocalTime)) {
            return false;
        }
        LocalTime other = (LocalTime) obj;
        if (!(this.hour == other.hour && this.minute == other.minute && this.second == other.second && this.nano == other.nano)) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        long nod = toNanoOfDay();
        return (int) ((nod >>> 32) ^ nod);
    }

    public String toString() {
        StringBuilder buf = new StringBuilder(18);
        int hourValue = this.hour;
        int minuteValue = this.minute;
        int secondValue = this.second;
        int nanoValue = this.nano;
        buf.append(hourValue < 10 ? "0" : "");
        buf.append(hourValue);
        buf.append(minuteValue < 10 ? ":0" : ":");
        buf.append(minuteValue);
        if (secondValue > 0 || nanoValue > 0) {
            buf.append(secondValue < 10 ? ":0" : ":");
            buf.append(secondValue);
            if (nanoValue > 0) {
                buf.append('.');
                if (nanoValue % 1000000 == 0) {
                    buf.append(Integer.toString((nanoValue / 1000000) + 1000).substring(1));
                } else if (nanoValue % 1000 == 0) {
                    buf.append(Integer.toString((nanoValue / 1000) + 1000000).substring(1));
                } else {
                    buf.append(Integer.toString(1000000000 + nanoValue).substring(1));
                }
            }
        }
        return buf.toString();
    }

    private Object writeReplace() {
        return new Ser((byte) 4, this);
    }

    private void readObject(ObjectInputStream s) throws InvalidObjectException {
        throw new InvalidObjectException("Deserialization via serialization delegate");
    }

    /* access modifiers changed from: package-private */
    public void writeExternal(DataOutput out) throws IOException {
        if (this.nano != 0) {
            out.writeByte(this.hour);
            out.writeByte(this.minute);
            out.writeByte(this.second);
            out.writeInt(this.nano);
        } else if (this.second != 0) {
            out.writeByte(this.hour);
            out.writeByte(this.minute);
            out.writeByte(~this.second);
        } else if (this.minute == 0) {
            out.writeByte(~this.hour);
        } else {
            out.writeByte(this.hour);
            out.writeByte(~this.minute);
        }
    }

    static LocalTime readExternal(DataInput in) throws IOException {
        int hour2 = in.readByte();
        int minute2 = 0;
        int second2 = 0;
        int nano2 = 0;
        if (hour2 < 0) {
            hour2 = ~hour2;
        } else {
            minute2 = in.readByte();
            if (minute2 < 0) {
                minute2 = ~minute2;
            } else {
                second2 = in.readByte();
                if (second2 < 0) {
                    second2 = ~second2;
                } else {
                    nano2 = in.readInt();
                }
            }
        }
        return of(hour2, minute2, second2, nano2);
    }
}
