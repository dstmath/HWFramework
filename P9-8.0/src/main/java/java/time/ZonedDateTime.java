package java.time;

import java.io.DataOutput;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.time.chrono.ChronoZonedDateTime;
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
import java.time.zone.ZoneOffsetTransition;
import java.time.zone.ZoneRules;
import java.util.List;
import java.util.Objects;

public final class ZonedDateTime implements Temporal, ChronoZonedDateTime<LocalDate>, Serializable {
    private static final /* synthetic */ int[] -java-time-temporal-ChronoFieldSwitchesValues = null;
    private static final long serialVersionUID = -6260982410461394882L;
    private final LocalDateTime dateTime;
    private final ZoneOffset offset;
    private final ZoneId zone;

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

    public static ZonedDateTime now() {
        return now(Clock.systemDefaultZone());
    }

    public static ZonedDateTime now(ZoneId zone) {
        return now(Clock.system(zone));
    }

    public static ZonedDateTime now(Clock clock) {
        Objects.requireNonNull((Object) clock, "clock");
        return ofInstant(clock.instant(), clock.getZone());
    }

    public static ZonedDateTime of(LocalDate date, LocalTime time, ZoneId zone) {
        return of(LocalDateTime.of(date, time), zone);
    }

    public static ZonedDateTime of(LocalDateTime localDateTime, ZoneId zone) {
        return ofLocal(localDateTime, zone, null);
    }

    public static ZonedDateTime of(int year, int month, int dayOfMonth, int hour, int minute, int second, int nanoOfSecond, ZoneId zone) {
        return ofLocal(LocalDateTime.of(year, month, dayOfMonth, hour, minute, second, nanoOfSecond), zone, null);
    }

    public static ZonedDateTime ofLocal(LocalDateTime localDateTime, ZoneId zone, ZoneOffset preferredOffset) {
        Objects.requireNonNull((Object) localDateTime, "localDateTime");
        Objects.requireNonNull((Object) zone, "zone");
        if (zone instanceof ZoneOffset) {
            return new ZonedDateTime(localDateTime, (ZoneOffset) zone, zone);
        }
        ZoneOffset offset;
        ZoneRules rules = zone.getRules();
        List<ZoneOffset> validOffsets = rules.getValidOffsets(localDateTime);
        if (validOffsets.size() == 1) {
            offset = (ZoneOffset) validOffsets.get(0);
        } else if (validOffsets.size() == 0) {
            ZoneOffsetTransition trans = rules.getTransition(localDateTime);
            localDateTime = localDateTime.plusSeconds(trans.getDuration().getSeconds());
            offset = trans.getOffsetAfter();
        } else if (preferredOffset == null || !validOffsets.contains(preferredOffset)) {
            offset = (ZoneOffset) Objects.requireNonNull((ZoneOffset) validOffsets.get(0), "offset");
        } else {
            offset = preferredOffset;
        }
        return new ZonedDateTime(localDateTime, offset, zone);
    }

    public static ZonedDateTime ofInstant(Instant instant, ZoneId zone) {
        Objects.requireNonNull((Object) instant, "instant");
        Objects.requireNonNull((Object) zone, "zone");
        return create(instant.getEpochSecond(), instant.getNano(), zone);
    }

    public static ZonedDateTime ofInstant(LocalDateTime localDateTime, ZoneOffset offset, ZoneId zone) {
        Objects.requireNonNull((Object) localDateTime, "localDateTime");
        Objects.requireNonNull((Object) offset, "offset");
        Objects.requireNonNull((Object) zone, "zone");
        if (zone.getRules().isValidOffset(localDateTime, offset)) {
            return new ZonedDateTime(localDateTime, offset, zone);
        }
        return create(localDateTime.toEpochSecond(offset), localDateTime.getNano(), zone);
    }

    private static ZonedDateTime create(long epochSecond, int nanoOfSecond, ZoneId zone) {
        ZoneOffset offset = zone.getRules().getOffset(Instant.ofEpochSecond(epochSecond, (long) nanoOfSecond));
        return new ZonedDateTime(LocalDateTime.ofEpochSecond(epochSecond, nanoOfSecond, offset), offset, zone);
    }

    public static ZonedDateTime ofStrict(LocalDateTime localDateTime, ZoneOffset offset, ZoneId zone) {
        Objects.requireNonNull((Object) localDateTime, "localDateTime");
        Objects.requireNonNull((Object) offset, "offset");
        Objects.requireNonNull((Object) zone, "zone");
        ZoneRules rules = zone.getRules();
        if (rules.isValidOffset(localDateTime, offset)) {
            return new ZonedDateTime(localDateTime, offset, zone);
        }
        ZoneOffsetTransition trans = rules.getTransition(localDateTime);
        if (trans == null || !trans.isGap()) {
            throw new DateTimeException("ZoneOffset '" + offset + "' is not valid for LocalDateTime '" + localDateTime + "' in zone '" + zone + "'");
        }
        throw new DateTimeException("LocalDateTime '" + localDateTime + "' does not exist in zone '" + zone + "' due to a gap in the local time-line, typically caused by daylight savings");
    }

    private static ZonedDateTime ofLenient(LocalDateTime localDateTime, ZoneOffset offset, ZoneId zone) {
        Objects.requireNonNull((Object) localDateTime, "localDateTime");
        Objects.requireNonNull((Object) offset, "offset");
        Objects.requireNonNull((Object) zone, "zone");
        if (!(zone instanceof ZoneOffset) || offset.equals(zone)) {
            return new ZonedDateTime(localDateTime, offset, zone);
        }
        throw new IllegalArgumentException("ZoneId must match ZoneOffset");
    }

    public static ZonedDateTime from(TemporalAccessor temporal) {
        if (temporal instanceof ZonedDateTime) {
            return (ZonedDateTime) temporal;
        }
        try {
            ZoneId zone = ZoneId.from(temporal);
            if (temporal.isSupported(ChronoField.INSTANT_SECONDS)) {
                return create(temporal.getLong(ChronoField.INSTANT_SECONDS), temporal.get(ChronoField.NANO_OF_SECOND), zone);
            }
            return of(LocalDate.from(temporal), LocalTime.from(temporal), zone);
        } catch (DateTimeException ex) {
            throw new DateTimeException("Unable to obtain ZonedDateTime from TemporalAccessor: " + temporal + " of type " + temporal.getClass().getName(), ex);
        }
    }

    public static ZonedDateTime parse(CharSequence text) {
        return parse(text, DateTimeFormatter.ISO_ZONED_DATE_TIME);
    }

    public static ZonedDateTime parse(CharSequence text, DateTimeFormatter formatter) {
        Objects.requireNonNull((Object) formatter, "formatter");
        return (ZonedDateTime) formatter.parse(text, new -$Lambda$NNxQ89IdKrbbkCWqmUk0ib3Mvno());
    }

    private ZonedDateTime(LocalDateTime dateTime, ZoneOffset offset, ZoneId zone) {
        this.dateTime = dateTime;
        this.offset = offset;
        this.zone = zone;
    }

    private ZonedDateTime resolveLocal(LocalDateTime newDateTime) {
        return ofLocal(newDateTime, this.zone, this.offset);
    }

    private ZonedDateTime resolveInstant(LocalDateTime newDateTime) {
        return ofInstant(newDateTime, this.offset, this.zone);
    }

    private ZonedDateTime resolveOffset(ZoneOffset offset) {
        if (offset.equals(this.offset) || !this.zone.getRules().isValidOffset(this.dateTime, offset)) {
            return this;
        }
        return new ZonedDateTime(this.dateTime, offset, this.zone);
    }

    public boolean isSupported(TemporalField field) {
        if (field instanceof ChronoField) {
            return true;
        }
        return field != null ? field.isSupportedBy(this) : false;
    }

    public boolean isSupported(TemporalUnit unit) {
        return super.isSupported(unit);
    }

    public ValueRange range(TemporalField field) {
        if (!(field instanceof ChronoField)) {
            return field.rangeRefinedBy(this);
        }
        if (field == ChronoField.INSTANT_SECONDS || field == ChronoField.OFFSET_SECONDS) {
            return field.range();
        }
        return this.dateTime.range(field);
    }

    public int get(TemporalField field) {
        if (!(field instanceof ChronoField)) {
            return super.get(field);
        }
        switch (-getjava-time-temporal-ChronoFieldSwitchesValues()[((ChronoField) field).ordinal()]) {
            case 1:
                throw new UnsupportedTemporalTypeException("Invalid field 'InstantSeconds' for get() method, use getLong() instead");
            case 2:
                return getOffset().getTotalSeconds();
            default:
                return this.dateTime.get(field);
        }
    }

    public long getLong(TemporalField field) {
        if (!(field instanceof ChronoField)) {
            return field.getFrom(this);
        }
        switch (-getjava-time-temporal-ChronoFieldSwitchesValues()[((ChronoField) field).ordinal()]) {
            case 1:
                return toEpochSecond();
            case 2:
                return (long) getOffset().getTotalSeconds();
            default:
                return this.dateTime.getLong(field);
        }
    }

    public ZoneOffset getOffset() {
        return this.offset;
    }

    public ZonedDateTime withEarlierOffsetAtOverlap() {
        ZoneOffsetTransition trans = getZone().getRules().getTransition(this.dateTime);
        if (trans != null && trans.isOverlap()) {
            ZoneOffset earlierOffset = trans.getOffsetBefore();
            if (!earlierOffset.equals(this.offset)) {
                return new ZonedDateTime(this.dateTime, earlierOffset, this.zone);
            }
        }
        return this;
    }

    public ZonedDateTime withLaterOffsetAtOverlap() {
        ZoneOffsetTransition trans = getZone().getRules().getTransition(toLocalDateTime());
        if (trans != null) {
            ZoneOffset laterOffset = trans.getOffsetAfter();
            if (!laterOffset.equals(this.offset)) {
                return new ZonedDateTime(this.dateTime, laterOffset, this.zone);
            }
        }
        return this;
    }

    public ZoneId getZone() {
        return this.zone;
    }

    public ZonedDateTime withZoneSameLocal(ZoneId zone) {
        Objects.requireNonNull((Object) zone, "zone");
        return this.zone.equals(zone) ? this : ofLocal(this.dateTime, zone, this.offset);
    }

    public ZonedDateTime withZoneSameInstant(ZoneId zone) {
        Objects.requireNonNull((Object) zone, "zone");
        if (this.zone.equals(zone)) {
            return this;
        }
        return create(this.dateTime.toEpochSecond(this.offset), this.dateTime.getNano(), zone);
    }

    public ZonedDateTime withFixedOffsetZone() {
        return this.zone.equals(this.offset) ? this : new ZonedDateTime(this.dateTime, this.offset, this.offset);
    }

    public LocalDateTime toLocalDateTime() {
        return this.dateTime;
    }

    public LocalDate toLocalDate() {
        return this.dateTime.toLocalDate();
    }

    public int getYear() {
        return this.dateTime.getYear();
    }

    public int getMonthValue() {
        return this.dateTime.getMonthValue();
    }

    public Month getMonth() {
        return this.dateTime.getMonth();
    }

    public int getDayOfMonth() {
        return this.dateTime.getDayOfMonth();
    }

    public int getDayOfYear() {
        return this.dateTime.getDayOfYear();
    }

    public DayOfWeek getDayOfWeek() {
        return this.dateTime.getDayOfWeek();
    }

    public LocalTime toLocalTime() {
        return this.dateTime.toLocalTime();
    }

    public int getHour() {
        return this.dateTime.getHour();
    }

    public int getMinute() {
        return this.dateTime.getMinute();
    }

    public int getSecond() {
        return this.dateTime.getSecond();
    }

    public int getNano() {
        return this.dateTime.getNano();
    }

    public ZonedDateTime with(TemporalAdjuster adjuster) {
        if (adjuster instanceof LocalDate) {
            return resolveLocal(LocalDateTime.of((LocalDate) adjuster, this.dateTime.toLocalTime()));
        }
        if (adjuster instanceof LocalTime) {
            return resolveLocal(LocalDateTime.of(this.dateTime.toLocalDate(), (LocalTime) adjuster));
        }
        if (adjuster instanceof LocalDateTime) {
            return resolveLocal((LocalDateTime) adjuster);
        }
        if (adjuster instanceof OffsetDateTime) {
            OffsetDateTime odt = (OffsetDateTime) adjuster;
            return ofLocal(odt.toLocalDateTime(), this.zone, odt.getOffset());
        } else if (adjuster instanceof Instant) {
            Instant instant = (Instant) adjuster;
            return create(instant.getEpochSecond(), instant.getNano(), this.zone);
        } else if (adjuster instanceof ZoneOffset) {
            return resolveOffset((ZoneOffset) adjuster);
        } else {
            return (ZonedDateTime) adjuster.adjustInto(this);
        }
    }

    public ZonedDateTime with(TemporalField field, long newValue) {
        if (!(field instanceof ChronoField)) {
            return (ZonedDateTime) field.adjustInto(this, newValue);
        }
        ChronoField f = (ChronoField) field;
        switch (-getjava-time-temporal-ChronoFieldSwitchesValues()[f.ordinal()]) {
            case 1:
                return create(newValue, getNano(), this.zone);
            case 2:
                return resolveOffset(ZoneOffset.ofTotalSeconds(f.checkValidIntValue(newValue)));
            default:
                return resolveLocal(this.dateTime.with(field, newValue));
        }
    }

    public ZonedDateTime withYear(int year) {
        return resolveLocal(this.dateTime.withYear(year));
    }

    public ZonedDateTime withMonth(int month) {
        return resolveLocal(this.dateTime.withMonth(month));
    }

    public ZonedDateTime withDayOfMonth(int dayOfMonth) {
        return resolveLocal(this.dateTime.withDayOfMonth(dayOfMonth));
    }

    public ZonedDateTime withDayOfYear(int dayOfYear) {
        return resolveLocal(this.dateTime.withDayOfYear(dayOfYear));
    }

    public ZonedDateTime withHour(int hour) {
        return resolveLocal(this.dateTime.withHour(hour));
    }

    public ZonedDateTime withMinute(int minute) {
        return resolveLocal(this.dateTime.withMinute(minute));
    }

    public ZonedDateTime withSecond(int second) {
        return resolveLocal(this.dateTime.withSecond(second));
    }

    public ZonedDateTime withNano(int nanoOfSecond) {
        return resolveLocal(this.dateTime.withNano(nanoOfSecond));
    }

    public ZonedDateTime truncatedTo(TemporalUnit unit) {
        return resolveLocal(this.dateTime.truncatedTo(unit));
    }

    public ZonedDateTime plus(TemporalAmount amountToAdd) {
        if (amountToAdd instanceof Period) {
            return resolveLocal(this.dateTime.plus((Period) amountToAdd));
        }
        Objects.requireNonNull((Object) amountToAdd, "amountToAdd");
        return (ZonedDateTime) amountToAdd.addTo(this);
    }

    public ZonedDateTime plus(long amountToAdd, TemporalUnit unit) {
        if (!(unit instanceof ChronoUnit)) {
            return (ZonedDateTime) unit.addTo(this, amountToAdd);
        }
        if (unit.isDateBased()) {
            return resolveLocal(this.dateTime.plus(amountToAdd, unit));
        }
        return resolveInstant(this.dateTime.plus(amountToAdd, unit));
    }

    public ZonedDateTime plusYears(long years) {
        return resolveLocal(this.dateTime.plusYears(years));
    }

    public ZonedDateTime plusMonths(long months) {
        return resolveLocal(this.dateTime.plusMonths(months));
    }

    public ZonedDateTime plusWeeks(long weeks) {
        return resolveLocal(this.dateTime.plusWeeks(weeks));
    }

    public ZonedDateTime plusDays(long days) {
        return resolveLocal(this.dateTime.plusDays(days));
    }

    public ZonedDateTime plusHours(long hours) {
        return resolveInstant(this.dateTime.plusHours(hours));
    }

    public ZonedDateTime plusMinutes(long minutes) {
        return resolveInstant(this.dateTime.plusMinutes(minutes));
    }

    public ZonedDateTime plusSeconds(long seconds) {
        return resolveInstant(this.dateTime.plusSeconds(seconds));
    }

    public ZonedDateTime plusNanos(long nanos) {
        return resolveInstant(this.dateTime.plusNanos(nanos));
    }

    public ZonedDateTime minus(TemporalAmount amountToSubtract) {
        if (amountToSubtract instanceof Period) {
            return resolveLocal(this.dateTime.minus((Period) amountToSubtract));
        }
        Objects.requireNonNull((Object) amountToSubtract, "amountToSubtract");
        return (ZonedDateTime) amountToSubtract.subtractFrom(this);
    }

    public ZonedDateTime minus(long amountToSubtract, TemporalUnit unit) {
        return amountToSubtract == Long.MIN_VALUE ? plus((long) Long.MAX_VALUE, unit).plus(1, unit) : plus(-amountToSubtract, unit);
    }

    public ZonedDateTime minusYears(long years) {
        return years == Long.MIN_VALUE ? plusYears(Long.MAX_VALUE).plusYears(1) : plusYears(-years);
    }

    public ZonedDateTime minusMonths(long months) {
        return months == Long.MIN_VALUE ? plusMonths(Long.MAX_VALUE).plusMonths(1) : plusMonths(-months);
    }

    public ZonedDateTime minusWeeks(long weeks) {
        return weeks == Long.MIN_VALUE ? plusWeeks(Long.MAX_VALUE).plusWeeks(1) : plusWeeks(-weeks);
    }

    public ZonedDateTime minusDays(long days) {
        return days == Long.MIN_VALUE ? plusDays(Long.MAX_VALUE).plusDays(1) : plusDays(-days);
    }

    public ZonedDateTime minusHours(long hours) {
        return hours == Long.MIN_VALUE ? plusHours(Long.MAX_VALUE).plusHours(1) : plusHours(-hours);
    }

    public ZonedDateTime minusMinutes(long minutes) {
        return minutes == Long.MIN_VALUE ? plusMinutes(Long.MAX_VALUE).plusMinutes(1) : plusMinutes(-minutes);
    }

    public ZonedDateTime minusSeconds(long seconds) {
        return seconds == Long.MIN_VALUE ? plusSeconds(Long.MAX_VALUE).plusSeconds(1) : plusSeconds(-seconds);
    }

    public ZonedDateTime minusNanos(long nanos) {
        return nanos == Long.MIN_VALUE ? plusNanos(Long.MAX_VALUE).plusNanos(1) : plusNanos(-nanos);
    }

    public <R> R query(TemporalQuery<R> query) {
        if (query == TemporalQueries.localDate()) {
            return toLocalDate();
        }
        return super.query(query);
    }

    public long until(Temporal endExclusive, TemporalUnit unit) {
        ZonedDateTime end = from(endExclusive);
        if (!(unit instanceof ChronoUnit)) {
            return unit.between(this, end);
        }
        end = end.withZoneSameInstant(this.zone);
        if (unit.isDateBased()) {
            return this.dateTime.until(end.dateTime, unit);
        }
        return toOffsetDateTime().until(end.toOffsetDateTime(), unit);
    }

    public String format(DateTimeFormatter formatter) {
        Objects.requireNonNull((Object) formatter, "formatter");
        return formatter.format(this);
    }

    public OffsetDateTime toOffsetDateTime() {
        return OffsetDateTime.of(this.dateTime, this.offset);
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ZonedDateTime)) {
            return false;
        }
        ZonedDateTime other = (ZonedDateTime) obj;
        if (this.dateTime.equals(other.dateTime) && this.offset.equals(other.offset)) {
            z = this.zone.equals(other.zone);
        }
        return z;
    }

    public int hashCode() {
        return (this.dateTime.hashCode() ^ this.offset.hashCode()) ^ Integer.rotateLeft(this.zone.hashCode(), 3);
    }

    public String toString() {
        String str = this.dateTime.toString() + this.offset.toString();
        if (this.offset != this.zone) {
            return str + '[' + this.zone.toString() + ']';
        }
        return str;
    }

    private Object writeReplace() {
        return new Ser((byte) 6, this);
    }

    private void readObject(ObjectInputStream s) throws InvalidObjectException {
        throw new InvalidObjectException("Deserialization via serialization delegate");
    }

    void writeExternal(DataOutput out) throws IOException {
        this.dateTime.writeExternal(out);
        this.offset.writeExternal(out);
        this.zone.write(out);
    }

    static ZonedDateTime readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        return ofLenient(LocalDateTime.readExternal(in), ZoneOffset.readExternal(in), (ZoneId) Ser.read(in));
    }
}
