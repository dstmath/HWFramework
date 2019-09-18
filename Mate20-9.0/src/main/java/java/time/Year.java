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

public final class Year implements Temporal, TemporalAdjuster, Comparable<Year>, Serializable {
    public static final int MAX_VALUE = 999999999;
    public static final int MIN_VALUE = -999999999;
    private static final DateTimeFormatter PARSER = new DateTimeFormatterBuilder().appendValue(ChronoField.YEAR, 4, 10, SignStyle.EXCEEDS_PAD).toFormatter();
    private static final long serialVersionUID = -23038383694477807L;
    private final int year;

    public static Year now() {
        return now(Clock.systemDefaultZone());
    }

    public static Year now(ZoneId zone) {
        return now(Clock.system(zone));
    }

    public static Year now(Clock clock) {
        return of(LocalDate.now(clock).getYear());
    }

    public static Year of(int isoYear) {
        ChronoField.YEAR.checkValidValue((long) isoYear);
        return new Year(isoYear);
    }

    public static Year from(TemporalAccessor temporal) {
        if (temporal instanceof Year) {
            return (Year) temporal;
        }
        Objects.requireNonNull(temporal, "temporal");
        try {
            if (!IsoChronology.INSTANCE.equals(Chronology.from(temporal))) {
                temporal = LocalDate.from(temporal);
            }
            return of(temporal.get(ChronoField.YEAR));
        } catch (DateTimeException ex) {
            throw new DateTimeException("Unable to obtain Year from TemporalAccessor: " + temporal + " of type " + temporal.getClass().getName(), ex);
        }
    }

    public static Year parse(CharSequence text) {
        return parse(text, PARSER);
    }

    public static Year parse(CharSequence text, DateTimeFormatter formatter) {
        Objects.requireNonNull(formatter, "formatter");
        return (Year) formatter.parse(text, $$Lambda$1t2bycXU085eFZcwODXkbd0X4Bk.INSTANCE);
    }

    public static boolean isLeap(long year2) {
        return (3 & year2) == 0 && (year2 % 100 != 0 || year2 % 400 == 0);
    }

    private Year(int year2) {
        this.year = year2;
    }

    public int getValue() {
        return this.year;
    }

    public boolean isSupported(TemporalField field) {
        boolean z = true;
        if (field instanceof ChronoField) {
            if (!(field == ChronoField.YEAR || field == ChronoField.YEAR_OF_ERA || field == ChronoField.ERA)) {
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
            if (!(unit == ChronoUnit.YEARS || unit == ChronoUnit.DECADES || unit == ChronoUnit.CENTURIES || unit == ChronoUnit.MILLENNIA || unit == ChronoUnit.ERAS)) {
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
        return ValueRange.of(1, this.year <= 0 ? 1000000000 : 999999999);
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

    public boolean isLeap() {
        return isLeap((long) this.year);
    }

    public boolean isValidMonthDay(MonthDay monthDay) {
        return monthDay != null && monthDay.isValidYear(this.year);
    }

    public int length() {
        return isLeap() ? 366 : 365;
    }

    public Year with(TemporalAdjuster adjuster) {
        return (Year) adjuster.adjustInto(this);
    }

    public Year with(TemporalField field, long newValue) {
        if (!(field instanceof ChronoField)) {
            return (Year) field.adjustInto(this, newValue);
        }
        ChronoField f = (ChronoField) field;
        f.checkValidValue(newValue);
        switch (f) {
            case YEAR_OF_ERA:
                return of((int) (this.year < 1 ? 1 - newValue : newValue));
            case YEAR:
                return of((int) newValue);
            case ERA:
                return getLong(ChronoField.ERA) == newValue ? this : of(1 - this.year);
            default:
                throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
        }
    }

    public Year plus(TemporalAmount amountToAdd) {
        return (Year) amountToAdd.addTo(this);
    }

    public Year plus(long amountToAdd, TemporalUnit unit) {
        if (!(unit instanceof ChronoUnit)) {
            return (Year) unit.addTo(this, amountToAdd);
        }
        switch ((ChronoUnit) unit) {
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

    public Year plusYears(long yearsToAdd) {
        if (yearsToAdd == 0) {
            return this;
        }
        return of(ChronoField.YEAR.checkValidIntValue(((long) this.year) + yearsToAdd));
    }

    public Year minus(TemporalAmount amountToSubtract) {
        return (Year) amountToSubtract.subtractFrom(this);
    }

    public Year minus(long amountToSubtract, TemporalUnit unit) {
        return amountToSubtract == Long.MIN_VALUE ? plus((long) Long.MAX_VALUE, unit).plus(1, unit) : plus(-amountToSubtract, unit);
    }

    public Year minusYears(long yearsToSubtract) {
        return yearsToSubtract == Long.MIN_VALUE ? plusYears(Long.MAX_VALUE).plusYears(1) : plusYears(-yearsToSubtract);
    }

    public <R> R query(TemporalQuery<R> query) {
        if (query == TemporalQueries.chronology()) {
            return IsoChronology.INSTANCE;
        }
        if (query == TemporalQueries.precision()) {
            return ChronoUnit.YEARS;
        }
        return super.query(query);
    }

    public Temporal adjustInto(Temporal temporal) {
        if (Chronology.from(temporal).equals(IsoChronology.INSTANCE)) {
            return temporal.with(ChronoField.YEAR, (long) this.year);
        }
        throw new DateTimeException("Adjustment only supported on ISO date-time");
    }

    public long until(Temporal endExclusive, TemporalUnit unit) {
        Year end = from(endExclusive);
        if (!(unit instanceof ChronoUnit)) {
            return unit.between(this, end);
        }
        long yearsUntil = ((long) end.year) - ((long) this.year);
        switch ((ChronoUnit) unit) {
            case YEARS:
                return yearsUntil;
            case DECADES:
                return yearsUntil / 10;
            case CENTURIES:
                return yearsUntil / 100;
            case MILLENNIA:
                return yearsUntil / 1000;
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

    public LocalDate atDay(int dayOfYear) {
        return LocalDate.ofYearDay(this.year, dayOfYear);
    }

    public YearMonth atMonth(Month month) {
        return YearMonth.of(this.year, month);
    }

    public YearMonth atMonth(int month) {
        return YearMonth.of(this.year, month);
    }

    public LocalDate atMonthDay(MonthDay monthDay) {
        return monthDay.atYear(this.year);
    }

    public int compareTo(Year other) {
        return this.year - other.year;
    }

    public boolean isAfter(Year other) {
        return this.year > other.year;
    }

    public boolean isBefore(Year other) {
        return this.year < other.year;
    }

    public boolean equals(Object obj) {
        boolean z = true;
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Year)) {
            return false;
        }
        if (this.year != ((Year) obj).year) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        return this.year;
    }

    public String toString() {
        return Integer.toString(this.year);
    }

    private Object writeReplace() {
        return new Ser((byte) 11, this);
    }

    private void readObject(ObjectInputStream s) throws InvalidObjectException {
        throw new InvalidObjectException("Deserialization via serialization delegate");
    }

    /* access modifiers changed from: package-private */
    public void writeExternal(DataOutput out) throws IOException {
        out.writeInt(this.year);
    }

    static Year readExternal(DataInput in) throws IOException {
        return of(in.readInt());
    }
}
