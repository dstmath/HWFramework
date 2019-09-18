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
import java.time.format.SignStyle;
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
import sun.util.locale.LanguageTag;

public final class YearMonth implements Temporal, TemporalAdjuster, Comparable<YearMonth>, Serializable {
    private static final DateTimeFormatter PARSER = new DateTimeFormatterBuilder().appendValue(ChronoField.YEAR, 4, 10, SignStyle.EXCEEDS_PAD).appendLiteral('-').appendValue(ChronoField.MONTH_OF_YEAR, 2).toFormatter();
    private static final long serialVersionUID = 4183400860270640070L;
    private final int month;
    private final int year;

    public static YearMonth now() {
        return now(Clock.systemDefaultZone());
    }

    public static YearMonth now(ZoneId zone) {
        return now(Clock.system(zone));
    }

    public static YearMonth now(Clock clock) {
        LocalDate now = LocalDate.now(clock);
        return of(now.getYear(), now.getMonth());
    }

    public static YearMonth of(int year2, Month month2) {
        Objects.requireNonNull(month2, "month");
        return of(year2, month2.getValue());
    }

    public static YearMonth of(int year2, int month2) {
        ChronoField.YEAR.checkValidValue((long) year2);
        ChronoField.MONTH_OF_YEAR.checkValidValue((long) month2);
        return new YearMonth(year2, month2);
    }

    public static YearMonth from(TemporalAccessor temporal) {
        if (temporal instanceof YearMonth) {
            return (YearMonth) temporal;
        }
        Objects.requireNonNull(temporal, "temporal");
        try {
            if (!IsoChronology.INSTANCE.equals(Chronology.from(temporal))) {
                temporal = LocalDate.from(temporal);
            }
            return of(temporal.get(ChronoField.YEAR), temporal.get(ChronoField.MONTH_OF_YEAR));
        } catch (DateTimeException ex) {
            throw new DateTimeException("Unable to obtain YearMonth from TemporalAccessor: " + temporal + " of type " + temporal.getClass().getName(), ex);
        }
    }

    public static YearMonth parse(CharSequence text) {
        return parse(text, PARSER);
    }

    public static YearMonth parse(CharSequence text, DateTimeFormatter formatter) {
        Objects.requireNonNull(formatter, "formatter");
        return (YearMonth) formatter.parse(text, $$Lambda$102LKVjqD_Dw4HKR2kUwBMsRk.INSTANCE);
    }

    private YearMonth(int year2, int month2) {
        this.year = year2;
        this.month = month2;
    }

    private YearMonth with(int newYear, int newMonth) {
        if (this.year == newYear && this.month == newMonth) {
            return this;
        }
        return new YearMonth(newYear, newMonth);
    }

    public boolean isSupported(TemporalField field) {
        boolean z = true;
        if (field instanceof ChronoField) {
            if (!(field == ChronoField.YEAR || field == ChronoField.MONTH_OF_YEAR || field == ChronoField.PROLEPTIC_MONTH || field == ChronoField.YEAR_OF_ERA || field == ChronoField.ERA)) {
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
            if (!(unit == ChronoUnit.MONTHS || unit == ChronoUnit.YEARS || unit == ChronoUnit.DECADES || unit == ChronoUnit.CENTURIES || unit == ChronoUnit.MILLENNIA || unit == ChronoUnit.ERAS)) {
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
        if (field != ChronoField.YEAR_OF_ERA) {
            return super.range(field);
        }
        return ValueRange.of(1, getYear() <= 0 ? 1000000000 : 999999999);
    }

    public int get(TemporalField field) {
        return range(field).checkValidIntValue(getLong(field), field);
    }

    public long getLong(TemporalField field) {
        if (!(field instanceof ChronoField)) {
            return field.getFrom(this);
        }
        int i = 1;
        switch ((ChronoField) field) {
            case MONTH_OF_YEAR:
                return (long) this.month;
            case PROLEPTIC_MONTH:
                return getProlepticMonth();
            case YEAR_OF_ERA:
                return (long) (this.year < 1 ? 1 - this.year : this.year);
            case YEAR:
                return (long) this.year;
            case ERA:
                if (this.year < 1) {
                    i = 0;
                }
                return (long) i;
            default:
                throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
        }
    }

    private long getProlepticMonth() {
        return ((((long) this.year) * 12) + ((long) this.month)) - 1;
    }

    public int getYear() {
        return this.year;
    }

    public int getMonthValue() {
        return this.month;
    }

    public Month getMonth() {
        return Month.of(this.month);
    }

    public boolean isLeapYear() {
        return IsoChronology.INSTANCE.isLeapYear((long) this.year);
    }

    public boolean isValidDay(int dayOfMonth) {
        return dayOfMonth >= 1 && dayOfMonth <= lengthOfMonth();
    }

    public int lengthOfMonth() {
        return getMonth().length(isLeapYear());
    }

    public int lengthOfYear() {
        return isLeapYear() ? 366 : 365;
    }

    public YearMonth with(TemporalAdjuster adjuster) {
        return (YearMonth) adjuster.adjustInto(this);
    }

    public YearMonth with(TemporalField field, long newValue) {
        if (!(field instanceof ChronoField)) {
            return (YearMonth) field.adjustInto(this, newValue);
        }
        ChronoField f = (ChronoField) field;
        f.checkValidValue(newValue);
        switch (f) {
            case MONTH_OF_YEAR:
                return withMonth((int) newValue);
            case PROLEPTIC_MONTH:
                return plusMonths(newValue - getProlepticMonth());
            case YEAR_OF_ERA:
                return withYear((int) (this.year < 1 ? 1 - newValue : newValue));
            case YEAR:
                return withYear((int) newValue);
            case ERA:
                return getLong(ChronoField.ERA) == newValue ? this : withYear(1 - this.year);
            default:
                throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
        }
    }

    public YearMonth withYear(int year2) {
        ChronoField.YEAR.checkValidValue((long) year2);
        return with(year2, this.month);
    }

    public YearMonth withMonth(int month2) {
        ChronoField.MONTH_OF_YEAR.checkValidValue((long) month2);
        return with(this.year, month2);
    }

    public YearMonth plus(TemporalAmount amountToAdd) {
        return (YearMonth) amountToAdd.addTo(this);
    }

    public YearMonth plus(long amountToAdd, TemporalUnit unit) {
        if (!(unit instanceof ChronoUnit)) {
            return (YearMonth) unit.addTo(this, amountToAdd);
        }
        switch ((ChronoUnit) unit) {
            case MONTHS:
                return plusMonths(amountToAdd);
            case YEARS:
                return plusYears(amountToAdd);
            case DECADES:
                return plusYears(Math.multiplyExact(amountToAdd, 10));
            case CENTURIES:
                return plusYears(Math.multiplyExact(amountToAdd, 100));
            case MILLENNIA:
                return plusYears(Math.multiplyExact(amountToAdd, 1000));
            case ERAS:
                return with((TemporalField) ChronoField.ERA, Math.addExact(getLong(ChronoField.ERA), amountToAdd));
            default:
                throw new UnsupportedTemporalTypeException("Unsupported unit: " + unit);
        }
    }

    public YearMonth plusYears(long yearsToAdd) {
        if (yearsToAdd == 0) {
            return this;
        }
        return with(ChronoField.YEAR.checkValidIntValue(((long) this.year) + yearsToAdd), this.month);
    }

    public YearMonth plusMonths(long monthsToAdd) {
        if (monthsToAdd == 0) {
            return this;
        }
        long calcMonths = (((long) this.year) * 12) + ((long) (this.month - 1)) + monthsToAdd;
        return with(ChronoField.YEAR.checkValidIntValue(Math.floorDiv(calcMonths, 12)), ((int) Math.floorMod(calcMonths, 12)) + 1);
    }

    public YearMonth minus(TemporalAmount amountToSubtract) {
        return (YearMonth) amountToSubtract.subtractFrom(this);
    }

    public YearMonth minus(long amountToSubtract, TemporalUnit unit) {
        return amountToSubtract == Long.MIN_VALUE ? plus((long) Long.MAX_VALUE, unit).plus(1, unit) : plus(-amountToSubtract, unit);
    }

    public YearMonth minusYears(long yearsToSubtract) {
        return yearsToSubtract == Long.MIN_VALUE ? plusYears(Long.MAX_VALUE).plusYears(1) : plusYears(-yearsToSubtract);
    }

    public YearMonth minusMonths(long monthsToSubtract) {
        return monthsToSubtract == Long.MIN_VALUE ? plusMonths(Long.MAX_VALUE).plusMonths(1) : plusMonths(-monthsToSubtract);
    }

    public <R> R query(TemporalQuery<R> query) {
        if (query == TemporalQueries.chronology()) {
            return IsoChronology.INSTANCE;
        }
        if (query == TemporalQueries.precision()) {
            return ChronoUnit.MONTHS;
        }
        return super.query(query);
    }

    public Temporal adjustInto(Temporal temporal) {
        if (Chronology.from(temporal).equals(IsoChronology.INSTANCE)) {
            return temporal.with(ChronoField.PROLEPTIC_MONTH, getProlepticMonth());
        }
        throw new DateTimeException("Adjustment only supported on ISO date-time");
    }

    public long until(Temporal endExclusive, TemporalUnit unit) {
        YearMonth end = from(endExclusive);
        if (!(unit instanceof ChronoUnit)) {
            return unit.between(this, end);
        }
        long monthsUntil = end.getProlepticMonth() - getProlepticMonth();
        switch ((ChronoUnit) unit) {
            case MONTHS:
                return monthsUntil;
            case YEARS:
                return monthsUntil / 12;
            case DECADES:
                return monthsUntil / 120;
            case CENTURIES:
                return monthsUntil / 1200;
            case MILLENNIA:
                return monthsUntil / 12000;
            case ERAS:
                return end.getLong(ChronoField.ERA) - getLong(ChronoField.ERA);
            default:
                throw new UnsupportedTemporalTypeException("Unsupported unit: " + unit);
        }
    }

    public String format(DateTimeFormatter formatter) {
        Objects.requireNonNull(formatter, "formatter");
        return formatter.format(this);
    }

    public LocalDate atDay(int dayOfMonth) {
        return LocalDate.of(this.year, this.month, dayOfMonth);
    }

    public LocalDate atEndOfMonth() {
        return LocalDate.of(this.year, this.month, lengthOfMonth());
    }

    public int compareTo(YearMonth other) {
        int cmp = this.year - other.year;
        if (cmp == 0) {
            return this.month - other.month;
        }
        return cmp;
    }

    public boolean isAfter(YearMonth other) {
        return compareTo(other) > 0;
    }

    public boolean isBefore(YearMonth other) {
        return compareTo(other) < 0;
    }

    public boolean equals(Object obj) {
        boolean z = true;
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof YearMonth)) {
            return false;
        }
        YearMonth other = (YearMonth) obj;
        if (!(this.year == other.year && this.month == other.month)) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        return this.year ^ (this.month << 27);
    }

    public String toString() {
        int absYear = Math.abs(this.year);
        StringBuilder buf = new StringBuilder(9);
        if (absYear >= 1000) {
            buf.append(this.year);
        } else if (this.year < 0) {
            buf.append(this.year - 10000);
            buf.deleteCharAt(1);
        } else {
            buf.append(this.year + 10000);
            buf.deleteCharAt(0);
        }
        buf.append(this.month < 10 ? "-0" : LanguageTag.SEP);
        buf.append(this.month);
        return buf.toString();
    }

    private Object writeReplace() {
        return new Ser((byte) 12, this);
    }

    private void readObject(ObjectInputStream s) throws InvalidObjectException {
        throw new InvalidObjectException("Deserialization via serialization delegate");
    }

    /* access modifiers changed from: package-private */
    public void writeExternal(DataOutput out) throws IOException {
        out.writeInt(this.year);
        out.writeByte(this.month);
    }

    static YearMonth readExternal(DataInput in) throws IOException {
        return of(in.readInt(), (int) in.readByte());
    }
}
