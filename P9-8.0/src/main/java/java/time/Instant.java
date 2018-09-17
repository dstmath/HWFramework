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
    private static final /* synthetic */ int[] -java-time-temporal-ChronoFieldSwitchesValues = null;
    private static final /* synthetic */ int[] -java-time-temporal-ChronoUnitSwitchesValues = null;
    public static final Instant EPOCH = new Instant(0, 0);
    public static final Instant MAX = ofEpochSecond(MAX_SECOND, 999999999);
    private static final long MAX_SECOND = 31556889864403199L;
    public static final Instant MIN = ofEpochSecond(MIN_SECOND, 0);
    private static final long MIN_SECOND = -31557014167219200L;
    private static final long serialVersionUID = -665713676816604388L;
    private final int nanos;
    private final long seconds;

    private static /* synthetic */ int[] -getjava-time-temporal-ChronoFieldSwitchesValues() {
        if (-java-time-temporal-ChronoFieldSwitchesValues != null) {
            return -java-time-temporal-ChronoFieldSwitchesValues;
        }
        int[] iArr = new int[ChronoField.values().length];
        try {
            iArr[ChronoField.ALIGNED_DAY_OF_WEEK_IN_MONTH.ordinal()] = 13;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[ChronoField.ALIGNED_DAY_OF_WEEK_IN_YEAR.ordinal()] = 14;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[ChronoField.ALIGNED_WEEK_OF_MONTH.ordinal()] = 15;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[ChronoField.ALIGNED_WEEK_OF_YEAR.ordinal()] = 16;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[ChronoField.AMPM_OF_DAY.ordinal()] = 17;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[ChronoField.CLOCK_HOUR_OF_AMPM.ordinal()] = 18;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[ChronoField.CLOCK_HOUR_OF_DAY.ordinal()] = 19;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[ChronoField.DAY_OF_MONTH.ordinal()] = 20;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[ChronoField.DAY_OF_WEEK.ordinal()] = 21;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[ChronoField.DAY_OF_YEAR.ordinal()] = 22;
        } catch (NoSuchFieldError e10) {
        }
        try {
            iArr[ChronoField.EPOCH_DAY.ordinal()] = 23;
        } catch (NoSuchFieldError e11) {
        }
        try {
            iArr[ChronoField.ERA.ordinal()] = 24;
        } catch (NoSuchFieldError e12) {
        }
        try {
            iArr[ChronoField.HOUR_OF_AMPM.ordinal()] = 25;
        } catch (NoSuchFieldError e13) {
        }
        try {
            iArr[ChronoField.HOUR_OF_DAY.ordinal()] = 26;
        } catch (NoSuchFieldError e14) {
        }
        try {
            iArr[ChronoField.INSTANT_SECONDS.ordinal()] = 1;
        } catch (NoSuchFieldError e15) {
        }
        try {
            iArr[ChronoField.MICRO_OF_DAY.ordinal()] = 27;
        } catch (NoSuchFieldError e16) {
        }
        try {
            iArr[ChronoField.MICRO_OF_SECOND.ordinal()] = 2;
        } catch (NoSuchFieldError e17) {
        }
        try {
            iArr[ChronoField.MILLI_OF_DAY.ordinal()] = 28;
        } catch (NoSuchFieldError e18) {
        }
        try {
            iArr[ChronoField.MILLI_OF_SECOND.ordinal()] = 3;
        } catch (NoSuchFieldError e19) {
        }
        try {
            iArr[ChronoField.MINUTE_OF_DAY.ordinal()] = 29;
        } catch (NoSuchFieldError e20) {
        }
        try {
            iArr[ChronoField.MINUTE_OF_HOUR.ordinal()] = 30;
        } catch (NoSuchFieldError e21) {
        }
        try {
            iArr[ChronoField.MONTH_OF_YEAR.ordinal()] = 31;
        } catch (NoSuchFieldError e22) {
        }
        try {
            iArr[ChronoField.NANO_OF_DAY.ordinal()] = 32;
        } catch (NoSuchFieldError e23) {
        }
        try {
            iArr[ChronoField.NANO_OF_SECOND.ordinal()] = 4;
        } catch (NoSuchFieldError e24) {
        }
        try {
            iArr[ChronoField.OFFSET_SECONDS.ordinal()] = 33;
        } catch (NoSuchFieldError e25) {
        }
        try {
            iArr[ChronoField.PROLEPTIC_MONTH.ordinal()] = 34;
        } catch (NoSuchFieldError e26) {
        }
        try {
            iArr[ChronoField.SECOND_OF_DAY.ordinal()] = 35;
        } catch (NoSuchFieldError e27) {
        }
        try {
            iArr[ChronoField.SECOND_OF_MINUTE.ordinal()] = 36;
        } catch (NoSuchFieldError e28) {
        }
        try {
            iArr[ChronoField.YEAR.ordinal()] = 37;
        } catch (NoSuchFieldError e29) {
        }
        try {
            iArr[ChronoField.YEAR_OF_ERA.ordinal()] = 38;
        } catch (NoSuchFieldError e30) {
        }
        -java-time-temporal-ChronoFieldSwitchesValues = iArr;
        return iArr;
    }

    private static /* synthetic */ int[] -getjava-time-temporal-ChronoUnitSwitchesValues() {
        if (-java-time-temporal-ChronoUnitSwitchesValues != null) {
            return -java-time-temporal-ChronoUnitSwitchesValues;
        }
        int[] iArr = new int[ChronoUnit.values().length];
        try {
            iArr[ChronoUnit.CENTURIES.ordinal()] = 13;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[ChronoUnit.DAYS.ordinal()] = 1;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[ChronoUnit.DECADES.ordinal()] = 14;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[ChronoUnit.ERAS.ordinal()] = 15;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[ChronoUnit.FOREVER.ordinal()] = 16;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[ChronoUnit.HALF_DAYS.ordinal()] = 2;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[ChronoUnit.HOURS.ordinal()] = 3;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[ChronoUnit.MICROS.ordinal()] = 4;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[ChronoUnit.MILLENNIA.ordinal()] = 17;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[ChronoUnit.MILLIS.ordinal()] = 5;
        } catch (NoSuchFieldError e10) {
        }
        try {
            iArr[ChronoUnit.MINUTES.ordinal()] = 6;
        } catch (NoSuchFieldError e11) {
        }
        try {
            iArr[ChronoUnit.MONTHS.ordinal()] = 18;
        } catch (NoSuchFieldError e12) {
        }
        try {
            iArr[ChronoUnit.NANOS.ordinal()] = 7;
        } catch (NoSuchFieldError e13) {
        }
        try {
            iArr[ChronoUnit.SECONDS.ordinal()] = 8;
        } catch (NoSuchFieldError e14) {
        }
        try {
            iArr[ChronoUnit.WEEKS.ordinal()] = 19;
        } catch (NoSuchFieldError e15) {
        }
        try {
            iArr[ChronoUnit.YEARS.ordinal()] = 20;
        } catch (NoSuchFieldError e16) {
        }
        -java-time-temporal-ChronoUnitSwitchesValues = iArr;
        return iArr;
    }

    public static Instant now() {
        return Clock.systemUTC().instant();
    }

    public static Instant now(Clock clock) {
        Objects.requireNonNull((Object) clock, "clock");
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
        Objects.requireNonNull((Object) temporal, "temporal");
        try {
            return ofEpochSecond(temporal.getLong(ChronoField.INSTANT_SECONDS), (long) temporal.get(ChronoField.NANO_OF_SECOND));
        } catch (DateTimeException ex) {
            throw new DateTimeException("Unable to obtain Instant from TemporalAccessor: " + temporal + " of type " + temporal.getClass().getName(), ex);
        }
    }

    public static Instant parse(CharSequence text) {
        return (Instant) DateTimeFormatter.ISO_INSTANT.parse(text, new -$Lambda$CgZRCvRFe4TjYjhKtuznUEGaKyw());
    }

    private static Instant create(long seconds, int nanoOfSecond) {
        if ((((long) nanoOfSecond) | seconds) == 0) {
            return EPOCH;
        }
        if (seconds >= MIN_SECOND && seconds <= MAX_SECOND) {
            return new Instant(seconds, nanoOfSecond);
        }
        throw new DateTimeException("Instant exceeds minimum or maximum instant");
    }

    private Instant(long epochSecond, int nanos) {
        this.seconds = epochSecond;
        this.nanos = nanos;
    }

    public boolean isSupported(TemporalField field) {
        boolean z = true;
        boolean z2 = false;
        if (field instanceof ChronoField) {
            if (!(field == ChronoField.INSTANT_SECONDS || field == ChronoField.NANO_OF_SECOND || field == ChronoField.MICRO_OF_SECOND || field == ChronoField.MILLI_OF_SECOND)) {
                z = false;
            }
            return z;
        }
        if (field != null) {
            z2 = field.isSupportedBy(this);
        }
        return z2;
    }

    public boolean isSupported(TemporalUnit unit) {
        boolean z = true;
        boolean z2 = false;
        if (unit instanceof ChronoUnit) {
            if (!(unit.isTimeBased() || unit == ChronoUnit.DAYS)) {
                z = false;
            }
            return z;
        }
        if (unit != null) {
            z2 = unit.isSupportedBy(this);
        }
        return z2;
    }

    public ValueRange range(TemporalField field) {
        return super.range(field);
    }

    public int get(TemporalField field) {
        if (!(field instanceof ChronoField)) {
            return range(field).checkValidIntValue(field.getFrom(this), field);
        }
        switch (-getjava-time-temporal-ChronoFieldSwitchesValues()[((ChronoField) field).ordinal()]) {
            case 1:
                ChronoField.INSTANT_SECONDS.checkValidIntValue(this.seconds);
                break;
            case 2:
                return this.nanos / 1000;
            case 3:
                return this.nanos / 1000000;
            case 4:
                return this.nanos;
        }
        throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
    }

    public long getLong(TemporalField field) {
        if (!(field instanceof ChronoField)) {
            return field.getFrom(this);
        }
        switch (-getjava-time-temporal-ChronoFieldSwitchesValues()[((ChronoField) field).ordinal()]) {
            case 1:
                return this.seconds;
            case 2:
                return (long) (this.nanos / 1000);
            case 3:
                return (long) (this.nanos / 1000000);
            case 4:
                return (long) this.nanos;
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
        int nval;
        switch (-getjava-time-temporal-ChronoFieldSwitchesValues()[f.ordinal()]) {
            case 1:
                if (newValue != this.seconds) {
                    this = create(newValue, this.nanos);
                }
                return this;
            case 2:
                nval = ((int) newValue) * 1000;
                if (nval != this.nanos) {
                    this = create(this.seconds, nval);
                }
                return this;
            case 3:
                nval = ((int) newValue) * 1000000;
                if (nval != this.nanos) {
                    this = create(this.seconds, nval);
                }
                return this;
            case 4:
                if (newValue != ((long) this.nanos)) {
                    this = create(this.seconds, (int) newValue);
                }
                return this;
            default:
                throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
        }
    }

    public Instant truncatedTo(TemporalUnit unit) {
        if (unit == ChronoUnit.NANOS) {
            return this;
        }
        Duration unitDur = unit.getDuration();
        if (unitDur.getSeconds() > 86400) {
            throw new UnsupportedTemporalTypeException("Unit is too large to be used for truncation");
        }
        long dur = unitDur.toNanos();
        if (86400000000000L % dur != 0) {
            throw new UnsupportedTemporalTypeException("Unit must divide into a standard day without remainder");
        }
        long nod = ((this.seconds % 86400) * 1000000000) + ((long) this.nanos);
        return plusNanos(((nod / dur) * dur) - nod);
    }

    public Instant plus(TemporalAmount amountToAdd) {
        return (Instant) amountToAdd.addTo(this);
    }

    public Instant plus(long amountToAdd, TemporalUnit unit) {
        if (!(unit instanceof ChronoUnit)) {
            return (Instant) unit.addTo(this, amountToAdd);
        }
        switch (-getjava-time-temporal-ChronoUnitSwitchesValues()[((ChronoUnit) unit).ordinal()]) {
            case 1:
                return plusSeconds(Math.multiplyExact(amountToAdd, 86400));
            case 2:
                return plusSeconds(Math.multiplyExact(amountToAdd, 43200));
            case 3:
                return plusSeconds(Math.multiplyExact(amountToAdd, 3600));
            case 4:
                return plus(amountToAdd / 1000000, (amountToAdd % 1000000) * 1000);
            case 5:
                return plusMillis(amountToAdd);
            case 6:
                return plusSeconds(Math.multiplyExact(amountToAdd, 60));
            case 7:
                return plusNanos(amountToAdd);
            case 8:
                return plusSeconds(amountToAdd);
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
        switch (-getjava-time-temporal-ChronoUnitSwitchesValues()[((ChronoUnit) unit).ordinal()]) {
            case 1:
                return secondsUntil(end) / 86400;
            case 2:
                return secondsUntil(end) / 43200;
            case 3:
                return secondsUntil(end) / 3600;
            case 4:
                return nanosUntil(end) / 1000;
            case 5:
                return Math.subtractExact(end.toEpochMilli(), toEpochMilli());
            case 6:
                return secondsUntil(end) / 60;
            case 7:
                return nanosUntil(end);
            case 8:
                return secondsUntil(end);
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
        if (this.seconds != other.seconds) {
            z = false;
        } else if (this.nanos != other.nanos) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        return ((int) (this.seconds ^ (this.seconds >>> 32))) + (this.nanos * 51);
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

    void writeExternal(DataOutput out) throws IOException {
        out.writeLong(this.seconds);
        out.writeInt(this.nanos);
    }

    static Instant readExternal(DataInput in) throws IOException {
        return ofEpochSecond(in.readLong(), (long) in.readInt());
    }
}
