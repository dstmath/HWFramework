package java.time;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.time.chrono.Chronology;
import java.time.chrono.IsoChronology;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalQueries;
import java.time.temporal.TemporalQuery;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.time.temporal.ValueRange;
import java.util.Objects;
import sun.util.locale.LanguageTag;

public final class MonthDay implements TemporalAccessor, TemporalAdjuster, Comparable<MonthDay>, Serializable {
    private static final DateTimeFormatter PARSER = new DateTimeFormatterBuilder().appendLiteral("--").appendValue(ChronoField.MONTH_OF_YEAR, 2).appendLiteral('-').appendValue(ChronoField.DAY_OF_MONTH, 2).toFormatter();
    private static final long serialVersionUID = -939150713474957432L;
    private final int day;
    private final int month;

    public static MonthDay now() {
        return now(Clock.systemDefaultZone());
    }

    public static MonthDay now(ZoneId zone) {
        return now(Clock.system(zone));
    }

    public static MonthDay now(Clock clock) {
        LocalDate now = LocalDate.now(clock);
        return of(now.getMonth(), now.getDayOfMonth());
    }

    public static MonthDay of(Month month2, int dayOfMonth) {
        Objects.requireNonNull(month2, "month");
        ChronoField.DAY_OF_MONTH.checkValidValue((long) dayOfMonth);
        if (dayOfMonth <= month2.maxLength()) {
            return new MonthDay(month2.getValue(), dayOfMonth);
        }
        throw new DateTimeException("Illegal value for DayOfMonth field, value " + dayOfMonth + " is not valid for month " + month2.name());
    }

    public static MonthDay of(int month2, int dayOfMonth) {
        return of(Month.of(month2), dayOfMonth);
    }

    public static MonthDay from(TemporalAccessor temporal) {
        if (temporal instanceof MonthDay) {
            return (MonthDay) temporal;
        }
        try {
            if (!IsoChronology.INSTANCE.equals(Chronology.from(temporal))) {
                temporal = LocalDate.from(temporal);
            }
            return of(temporal.get(ChronoField.MONTH_OF_YEAR), temporal.get(ChronoField.DAY_OF_MONTH));
        } catch (DateTimeException ex) {
            throw new DateTimeException("Unable to obtain MonthDay from TemporalAccessor: " + temporal + " of type " + temporal.getClass().getName(), ex);
        }
    }

    public static MonthDay parse(CharSequence text) {
        return parse(text, PARSER);
    }

    public static MonthDay parse(CharSequence text, DateTimeFormatter formatter) {
        Objects.requireNonNull(formatter, "formatter");
        return (MonthDay) formatter.parse(text, $$Lambda$sL_1zXqh7GXCv2G9X40ozp_OBMA.INSTANCE);
    }

    private MonthDay(int month2, int dayOfMonth) {
        this.month = month2;
        this.day = dayOfMonth;
    }

    public boolean isSupported(TemporalField field) {
        boolean z = true;
        if (field instanceof ChronoField) {
            if (!(field == ChronoField.MONTH_OF_YEAR || field == ChronoField.DAY_OF_MONTH)) {
                z = false;
            }
            return z;
        }
        if (field == null || !field.isSupportedBy(this)) {
            z = false;
        }
        return z;
    }

    public ValueRange range(TemporalField field) {
        if (field == ChronoField.MONTH_OF_YEAR) {
            return field.range();
        }
        if (field == ChronoField.DAY_OF_MONTH) {
            return ValueRange.of(1, (long) getMonth().minLength(), (long) getMonth().maxLength());
        }
        return super.range(field);
    }

    public int get(TemporalField field) {
        return range(field).checkValidIntValue(getLong(field), field);
    }

    public long getLong(TemporalField field) {
        if (!(field instanceof ChronoField)) {
            return field.getFrom(this);
        }
        switch ((ChronoField) field) {
            case DAY_OF_MONTH:
                return (long) this.day;
            case MONTH_OF_YEAR:
                return (long) this.month;
            default:
                throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
        }
    }

    public int getMonthValue() {
        return this.month;
    }

    public Month getMonth() {
        return Month.of(this.month);
    }

    public int getDayOfMonth() {
        return this.day;
    }

    public boolean isValidYear(int year) {
        return !(this.day == 29 && this.month == 2 && !Year.isLeap((long) year));
    }

    public MonthDay withMonth(int month2) {
        return with(Month.of(month2));
    }

    public MonthDay with(Month month2) {
        Objects.requireNonNull(month2, "month");
        if (month2.getValue() == this.month) {
            return this;
        }
        return new MonthDay(month2.getValue(), Math.min(this.day, month2.maxLength()));
    }

    public MonthDay withDayOfMonth(int dayOfMonth) {
        if (dayOfMonth == this.day) {
            return this;
        }
        return of(this.month, dayOfMonth);
    }

    public <R> R query(TemporalQuery<R> query) {
        if (query == TemporalQueries.chronology()) {
            return IsoChronology.INSTANCE;
        }
        return super.query(query);
    }

    public Temporal adjustInto(Temporal temporal) {
        if (Chronology.from(temporal).equals(IsoChronology.INSTANCE)) {
            Temporal temporal2 = temporal.with(ChronoField.MONTH_OF_YEAR, (long) this.month);
            return temporal2.with(ChronoField.DAY_OF_MONTH, Math.min(temporal2.range(ChronoField.DAY_OF_MONTH).getMaximum(), (long) this.day));
        }
        throw new DateTimeException("Adjustment only supported on ISO date-time");
    }

    public String format(DateTimeFormatter formatter) {
        Objects.requireNonNull(formatter, "formatter");
        return formatter.format(this);
    }

    public LocalDate atYear(int year) {
        return LocalDate.of(year, this.month, isValidYear(year) ? this.day : 28);
    }

    public int compareTo(MonthDay other) {
        int cmp = this.month - other.month;
        if (cmp == 0) {
            return this.day - other.day;
        }
        return cmp;
    }

    public boolean isAfter(MonthDay other) {
        return compareTo(other) > 0;
    }

    public boolean isBefore(MonthDay other) {
        return compareTo(other) < 0;
    }

    public boolean equals(Object obj) {
        boolean z = true;
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof MonthDay)) {
            return false;
        }
        MonthDay other = (MonthDay) obj;
        if (!(this.month == other.month && this.day == other.day)) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        return (this.month << 6) + this.day;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(10);
        sb.append("--");
        sb.append(this.month < 10 ? "0" : "");
        sb.append(this.month);
        sb.append(this.day < 10 ? "-0" : LanguageTag.SEP);
        sb.append(this.day);
        return sb.toString();
    }

    private Object writeReplace() {
        return new Ser((byte) 13, this);
    }

    private void readObject(ObjectInputStream s) throws InvalidObjectException {
        throw new InvalidObjectException("Deserialization via serialization delegate");
    }

    /* access modifiers changed from: package-private */
    public void writeExternal(DataOutput out) throws IOException {
        out.writeByte(this.month);
        out.writeByte(this.day);
    }

    static MonthDay readExternal(DataInput in) throws IOException {
        return of((int) in.readByte(), (int) in.readByte());
    }
}
