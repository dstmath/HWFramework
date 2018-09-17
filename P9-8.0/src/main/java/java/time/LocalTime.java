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
    private static final /* synthetic */ int[] -java-time-temporal-ChronoFieldSwitchesValues = null;
    private static final /* synthetic */ int[] -java-time-temporal-ChronoUnitSwitchesValues = null;
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

    private static /* synthetic */ int[] -getjava-time-temporal-ChronoFieldSwitchesValues() {
        if (-java-time-temporal-ChronoFieldSwitchesValues != null) {
            return -java-time-temporal-ChronoFieldSwitchesValues;
        }
        int[] iArr = new int[ChronoField.values().length];
        try {
            iArr[ChronoField.ALIGNED_DAY_OF_WEEK_IN_MONTH.ordinal()] = 23;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[ChronoField.ALIGNED_DAY_OF_WEEK_IN_YEAR.ordinal()] = 24;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[ChronoField.ALIGNED_WEEK_OF_MONTH.ordinal()] = 25;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[ChronoField.ALIGNED_WEEK_OF_YEAR.ordinal()] = 26;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[ChronoField.AMPM_OF_DAY.ordinal()] = 1;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[ChronoField.CLOCK_HOUR_OF_AMPM.ordinal()] = 2;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[ChronoField.CLOCK_HOUR_OF_DAY.ordinal()] = 3;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[ChronoField.DAY_OF_MONTH.ordinal()] = 27;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[ChronoField.DAY_OF_WEEK.ordinal()] = 28;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[ChronoField.DAY_OF_YEAR.ordinal()] = 29;
        } catch (NoSuchFieldError e10) {
        }
        try {
            iArr[ChronoField.EPOCH_DAY.ordinal()] = 30;
        } catch (NoSuchFieldError e11) {
        }
        try {
            iArr[ChronoField.ERA.ordinal()] = 31;
        } catch (NoSuchFieldError e12) {
        }
        try {
            iArr[ChronoField.HOUR_OF_AMPM.ordinal()] = 4;
        } catch (NoSuchFieldError e13) {
        }
        try {
            iArr[ChronoField.HOUR_OF_DAY.ordinal()] = 5;
        } catch (NoSuchFieldError e14) {
        }
        try {
            iArr[ChronoField.INSTANT_SECONDS.ordinal()] = 32;
        } catch (NoSuchFieldError e15) {
        }
        try {
            iArr[ChronoField.MICRO_OF_DAY.ordinal()] = 6;
        } catch (NoSuchFieldError e16) {
        }
        try {
            iArr[ChronoField.MICRO_OF_SECOND.ordinal()] = 7;
        } catch (NoSuchFieldError e17) {
        }
        try {
            iArr[ChronoField.MILLI_OF_DAY.ordinal()] = 8;
        } catch (NoSuchFieldError e18) {
        }
        try {
            iArr[ChronoField.MILLI_OF_SECOND.ordinal()] = 9;
        } catch (NoSuchFieldError e19) {
        }
        try {
            iArr[ChronoField.MINUTE_OF_DAY.ordinal()] = 10;
        } catch (NoSuchFieldError e20) {
        }
        try {
            iArr[ChronoField.MINUTE_OF_HOUR.ordinal()] = 11;
        } catch (NoSuchFieldError e21) {
        }
        try {
            iArr[ChronoField.MONTH_OF_YEAR.ordinal()] = 33;
        } catch (NoSuchFieldError e22) {
        }
        try {
            iArr[ChronoField.NANO_OF_DAY.ordinal()] = 12;
        } catch (NoSuchFieldError e23) {
        }
        try {
            iArr[ChronoField.NANO_OF_SECOND.ordinal()] = 13;
        } catch (NoSuchFieldError e24) {
        }
        try {
            iArr[ChronoField.OFFSET_SECONDS.ordinal()] = 34;
        } catch (NoSuchFieldError e25) {
        }
        try {
            iArr[ChronoField.PROLEPTIC_MONTH.ordinal()] = 35;
        } catch (NoSuchFieldError e26) {
        }
        try {
            iArr[ChronoField.SECOND_OF_DAY.ordinal()] = 14;
        } catch (NoSuchFieldError e27) {
        }
        try {
            iArr[ChronoField.SECOND_OF_MINUTE.ordinal()] = 15;
        } catch (NoSuchFieldError e28) {
        }
        try {
            iArr[ChronoField.YEAR.ordinal()] = 36;
        } catch (NoSuchFieldError e29) {
        }
        try {
            iArr[ChronoField.YEAR_OF_ERA.ordinal()] = 37;
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
            iArr[ChronoUnit.CENTURIES.ordinal()] = 23;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[ChronoUnit.DAYS.ordinal()] = 24;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[ChronoUnit.DECADES.ordinal()] = 25;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[ChronoUnit.ERAS.ordinal()] = 26;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[ChronoUnit.FOREVER.ordinal()] = 27;
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
            iArr[ChronoUnit.MILLENNIA.ordinal()] = 28;
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
            iArr[ChronoUnit.MONTHS.ordinal()] = 29;
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
            iArr[ChronoUnit.WEEKS.ordinal()] = 30;
        } catch (NoSuchFieldError e15) {
        }
        try {
            iArr[ChronoUnit.YEARS.ordinal()] = 31;
        } catch (NoSuchFieldError e16) {
        }
        -java-time-temporal-ChronoUnitSwitchesValues = iArr;
        return iArr;
    }

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
        Objects.requireNonNull((Object) clock, "clock");
        Instant now = clock.instant();
        return ofNanoOfDay((((long) ((int) Math.floorMod(now.getEpochSecond() + ((long) clock.getZone().getRules().getOffset(now).getTotalSeconds()), 86400))) * NANOS_PER_SECOND) + ((long) now.getNano()));
    }

    public static LocalTime of(int hour, int minute) {
        ChronoField.HOUR_OF_DAY.checkValidValue((long) hour);
        if (minute == 0) {
            return HOURS[hour];
        }
        ChronoField.MINUTE_OF_HOUR.checkValidValue((long) minute);
        return new LocalTime(hour, minute, 0, 0);
    }

    public static LocalTime of(int hour, int minute, int second) {
        ChronoField.HOUR_OF_DAY.checkValidValue((long) hour);
        if ((minute | second) == 0) {
            return HOURS[hour];
        }
        ChronoField.MINUTE_OF_HOUR.checkValidValue((long) minute);
        ChronoField.SECOND_OF_MINUTE.checkValidValue((long) second);
        return new LocalTime(hour, minute, second, 0);
    }

    public static LocalTime of(int hour, int minute, int second, int nanoOfSecond) {
        ChronoField.HOUR_OF_DAY.checkValidValue((long) hour);
        ChronoField.MINUTE_OF_HOUR.checkValidValue((long) minute);
        ChronoField.SECOND_OF_MINUTE.checkValidValue((long) second);
        ChronoField.NANO_OF_SECOND.checkValidValue((long) nanoOfSecond);
        return create(hour, minute, second, nanoOfSecond);
    }

    public static LocalTime ofSecondOfDay(long secondOfDay) {
        ChronoField.SECOND_OF_DAY.checkValidValue(secondOfDay);
        int hours = (int) (secondOfDay / 3600);
        secondOfDay -= (long) (hours * SECONDS_PER_HOUR);
        int minutes = (int) (secondOfDay / 60);
        return create(hours, minutes, (int) (secondOfDay - ((long) (minutes * 60))), 0);
    }

    public static LocalTime ofNanoOfDay(long nanoOfDay) {
        ChronoField.NANO_OF_DAY.checkValidValue(nanoOfDay);
        int hours = (int) (nanoOfDay / NANOS_PER_HOUR);
        nanoOfDay -= ((long) hours) * NANOS_PER_HOUR;
        int minutes = (int) (nanoOfDay / NANOS_PER_MINUTE);
        nanoOfDay -= ((long) minutes) * NANOS_PER_MINUTE;
        int seconds = (int) (nanoOfDay / NANOS_PER_SECOND);
        return create(hours, minutes, seconds, (int) (nanoOfDay - (((long) seconds) * NANOS_PER_SECOND)));
    }

    public static LocalTime from(TemporalAccessor temporal) {
        Objects.requireNonNull((Object) temporal, "temporal");
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
        Objects.requireNonNull((Object) formatter, "formatter");
        return (LocalTime) formatter.parse(text, new -$Lambda$Nb-ibgdTlWIhgjCJWHfrfU1wggY());
    }

    private static LocalTime create(int hour, int minute, int second, int nanoOfSecond) {
        if (((minute | second) | nanoOfSecond) == 0) {
            return HOURS[hour];
        }
        return new LocalTime(hour, minute, second, nanoOfSecond);
    }

    private LocalTime(int hour, int minute, int second, int nanoOfSecond) {
        this.hour = (byte) hour;
        this.minute = (byte) minute;
        this.second = (byte) second;
        this.nano = nanoOfSecond;
    }

    public boolean isSupported(TemporalField field) {
        if (field instanceof ChronoField) {
            return field.isTimeBased();
        }
        return field != null ? field.isSupportedBy(this) : false;
    }

    public boolean isSupported(TemporalUnit unit) {
        if (unit instanceof ChronoUnit) {
            return unit.isTimeBased();
        }
        return unit != null ? unit.isSupportedBy(this) : false;
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
        switch (-getjava-time-temporal-ChronoFieldSwitchesValues()[((ChronoField) field).ordinal()]) {
            case 1:
                return this.hour / 12;
            case 2:
                int ham = this.hour % 12;
                if (ham % 12 == 0) {
                    ham = 12;
                }
                return ham;
            case 3:
                return this.hour == (byte) 0 ? 24 : this.hour;
            case 4:
                return this.hour % 12;
            case 5:
                return this.hour;
            case 6:
                throw new UnsupportedTemporalTypeException("Invalid field 'MicroOfDay' for get() method, use getLong() instead");
            case 7:
                return this.nano / 1000;
            case 8:
                return (int) (toNanoOfDay() / 1000000);
            case 9:
                return this.nano / 1000000;
            case 10:
                return (this.hour * 60) + this.minute;
            case 11:
                return this.minute;
            case 12:
                throw new UnsupportedTemporalTypeException("Invalid field 'NanoOfDay' for get() method, use getLong() instead");
            case 13:
                return this.nano;
            case 14:
                return toSecondOfDay();
            case 15:
                return this.second;
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
        long j = 0;
        if (!(field instanceof ChronoField)) {
            return (LocalTime) field.adjustInto(this, newValue);
        }
        ChronoField f = (ChronoField) field;
        f.checkValidValue(newValue);
        switch (-getjava-time-temporal-ChronoFieldSwitchesValues()[f.ordinal()]) {
            case 1:
                return plusHours((newValue - ((long) (this.hour / 12))) * 12);
            case 2:
                if (newValue == 12) {
                    newValue = 0;
                }
                return plusHours(newValue - ((long) (this.hour % 12)));
            case 3:
                if (newValue != 24) {
                    j = newValue;
                }
                return withHour((int) j);
            case 4:
                return plusHours(newValue - ((long) (this.hour % 12)));
            case 5:
                return withHour((int) newValue);
            case 6:
                return ofNanoOfDay(1000 * newValue);
            case 7:
                return withNano(((int) newValue) * 1000);
            case 8:
                return ofNanoOfDay(1000000 * newValue);
            case 9:
                return withNano(((int) newValue) * 1000000);
            case 10:
                return plusMinutes(newValue - ((long) ((this.hour * 60) + this.minute)));
            case 11:
                return withMinute((int) newValue);
            case 12:
                return ofNanoOfDay(newValue);
            case 13:
                return withNano((int) newValue);
            case 14:
                return plusSeconds(newValue - ((long) toSecondOfDay()));
            case 15:
                return withSecond((int) newValue);
            default:
                throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
        }
    }

    public LocalTime withHour(int hour) {
        if (this.hour == hour) {
            return this;
        }
        ChronoField.HOUR_OF_DAY.checkValidValue((long) hour);
        return create(hour, this.minute, this.second, this.nano);
    }

    public LocalTime withMinute(int minute) {
        if (this.minute == minute) {
            return this;
        }
        ChronoField.MINUTE_OF_HOUR.checkValidValue((long) minute);
        return create(this.hour, minute, this.second, this.nano);
    }

    public LocalTime withSecond(int second) {
        if (this.second == second) {
            return this;
        }
        ChronoField.SECOND_OF_MINUTE.checkValidValue((long) second);
        return create(this.hour, this.minute, second, this.nano);
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
        if (unitDur.getSeconds() > 86400) {
            throw new UnsupportedTemporalTypeException("Unit is too large to be used for truncation");
        }
        long dur = unitDur.toNanos();
        if (NANOS_PER_DAY % dur == 0) {
            return ofNanoOfDay((toNanoOfDay() / dur) * dur);
        }
        throw new UnsupportedTemporalTypeException("Unit must divide into a standard day without remainder");
    }

    public LocalTime plus(TemporalAmount amountToAdd) {
        return (LocalTime) amountToAdd.addTo(this);
    }

    public LocalTime plus(long amountToAdd, TemporalUnit unit) {
        if (!(unit instanceof ChronoUnit)) {
            return (LocalTime) unit.addTo(this, amountToAdd);
        }
        switch (-getjava-time-temporal-ChronoUnitSwitchesValues()[((ChronoUnit) unit).ordinal()]) {
            case 1:
                return plusHours((amountToAdd % 2) * 12);
            case 2:
                return plusHours(amountToAdd);
            case 3:
                return plusNanos((amountToAdd % MICROS_PER_DAY) * 1000);
            case 4:
                return plusNanos((amountToAdd % MILLIS_PER_DAY) * 1000000);
            case 5:
                return plusMinutes(amountToAdd);
            case 6:
                return plusNanos(amountToAdd);
            case 7:
                return plusSeconds(amountToAdd);
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
        int sofd = ((this.hour * SECONDS_PER_HOUR) + (this.minute * 60)) + this.second;
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
        switch (-getjava-time-temporal-ChronoUnitSwitchesValues()[((ChronoUnit) unit).ordinal()]) {
            case 1:
                return nanosUntil / 43200000000000L;
            case 2:
                return nanosUntil / NANOS_PER_HOUR;
            case 3:
                return nanosUntil / 1000;
            case 4:
                return nanosUntil / 1000000;
            case 5:
                return nanosUntil / NANOS_PER_MINUTE;
            case 6:
                return nanosUntil;
            case 7:
                return nanosUntil / NANOS_PER_SECOND;
            default:
                throw new UnsupportedTemporalTypeException("Unsupported unit: " + unit);
        }
    }

    public String format(DateTimeFormatter formatter) {
        Objects.requireNonNull((Object) formatter, "formatter");
        return formatter.format(this);
    }

    public LocalDateTime atDate(LocalDate date) {
        return LocalDateTime.of(date, this);
    }

    public OffsetTime atOffset(ZoneOffset offset) {
        return OffsetTime.of(this, offset);
    }

    public int toSecondOfDay() {
        return ((this.hour * SECONDS_PER_HOUR) + (this.minute * 60)) + this.second;
    }

    public long toNanoOfDay() {
        return (((((long) this.hour) * NANOS_PER_HOUR) + (((long) this.minute) * NANOS_PER_MINUTE)) + (((long) this.second) * NANOS_PER_SECOND)) + ((long) this.nano);
    }

    public int compareTo(LocalTime other) {
        int cmp = Integer.compare(this.hour, other.hour);
        if (cmp != 0) {
            return cmp;
        }
        cmp = Integer.compare(this.minute, other.minute);
        if (cmp != 0) {
            return cmp;
        }
        cmp = Integer.compare(this.second, other.second);
        if (cmp == 0) {
            return Integer.compare(this.nano, other.nano);
        }
        return cmp;
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
        if (this.hour != other.hour || this.minute != other.minute || this.second != other.second) {
            z = false;
        } else if (this.nano != other.nano) {
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
        buf.append(hourValue < 10 ? "0" : "").append(hourValue).append(minuteValue < 10 ? ":0" : ":").append(minuteValue);
        if (secondValue > 0 || nanoValue > 0) {
            buf.append(secondValue < 10 ? ":0" : ":").append(secondValue);
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

    void writeExternal(DataOutput out) throws IOException {
        if (this.nano != 0) {
            out.writeByte(this.hour);
            out.writeByte(this.minute);
            out.writeByte(this.second);
            out.writeInt(this.nano);
        } else if (this.second != (byte) 0) {
            out.writeByte(this.hour);
            out.writeByte(this.minute);
            out.writeByte(~this.second);
        } else if (this.minute == (byte) 0) {
            out.writeByte(~this.hour);
        } else {
            out.writeByte(this.hour);
            out.writeByte(~this.minute);
        }
    }

    static LocalTime readExternal(DataInput in) throws IOException {
        int hour = in.readByte();
        int minute = 0;
        int second = 0;
        int nano = 0;
        if (hour < 0) {
            hour = ~hour;
        } else {
            minute = in.readByte();
            if (minute < 0) {
                minute = ~minute;
            } else {
                second = in.readByte();
                if (second < 0) {
                    second = ~second;
                } else {
                    nano = in.readInt();
                }
            }
        }
        return of(hour, minute, second, nano);
    }
}
