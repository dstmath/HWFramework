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
    private static final /* synthetic */ int[] -java-time-temporal-ChronoFieldSwitchesValues = null;
    private static final /* synthetic */ int[] -java-time-temporal-ChronoUnitSwitchesValues = null;
    public static final int MAX_VALUE = 999999999;
    public static final int MIN_VALUE = -999999999;
    private static final DateTimeFormatter PARSER = new DateTimeFormatterBuilder().appendValue(ChronoField.YEAR, 4, 10, SignStyle.EXCEEDS_PAD).toFormatter();
    private static final long serialVersionUID = -23038383694477807L;
    private final int year;

    private static /* synthetic */ int[] -getjava-time-temporal-ChronoFieldSwitchesValues() {
        if (-java-time-temporal-ChronoFieldSwitchesValues != null) {
            return -java-time-temporal-ChronoFieldSwitchesValues;
        }
        int[] iArr = new int[ChronoField.values().length];
        try {
            iArr[ChronoField.ALIGNED_DAY_OF_WEEK_IN_MONTH.ordinal()] = 9;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[ChronoField.ALIGNED_DAY_OF_WEEK_IN_YEAR.ordinal()] = 10;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[ChronoField.ALIGNED_WEEK_OF_MONTH.ordinal()] = 11;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[ChronoField.ALIGNED_WEEK_OF_YEAR.ordinal()] = 12;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[ChronoField.AMPM_OF_DAY.ordinal()] = 13;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[ChronoField.CLOCK_HOUR_OF_AMPM.ordinal()] = 14;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[ChronoField.CLOCK_HOUR_OF_DAY.ordinal()] = 15;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[ChronoField.DAY_OF_MONTH.ordinal()] = 16;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[ChronoField.DAY_OF_WEEK.ordinal()] = 17;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[ChronoField.DAY_OF_YEAR.ordinal()] = 18;
        } catch (NoSuchFieldError e10) {
        }
        try {
            iArr[ChronoField.EPOCH_DAY.ordinal()] = 19;
        } catch (NoSuchFieldError e11) {
        }
        try {
            iArr[ChronoField.ERA.ordinal()] = 1;
        } catch (NoSuchFieldError e12) {
        }
        try {
            iArr[ChronoField.HOUR_OF_AMPM.ordinal()] = 20;
        } catch (NoSuchFieldError e13) {
        }
        try {
            iArr[ChronoField.HOUR_OF_DAY.ordinal()] = 21;
        } catch (NoSuchFieldError e14) {
        }
        try {
            iArr[ChronoField.INSTANT_SECONDS.ordinal()] = 22;
        } catch (NoSuchFieldError e15) {
        }
        try {
            iArr[ChronoField.MICRO_OF_DAY.ordinal()] = 23;
        } catch (NoSuchFieldError e16) {
        }
        try {
            iArr[ChronoField.MICRO_OF_SECOND.ordinal()] = 24;
        } catch (NoSuchFieldError e17) {
        }
        try {
            iArr[ChronoField.MILLI_OF_DAY.ordinal()] = 25;
        } catch (NoSuchFieldError e18) {
        }
        try {
            iArr[ChronoField.MILLI_OF_SECOND.ordinal()] = 26;
        } catch (NoSuchFieldError e19) {
        }
        try {
            iArr[ChronoField.MINUTE_OF_DAY.ordinal()] = 27;
        } catch (NoSuchFieldError e20) {
        }
        try {
            iArr[ChronoField.MINUTE_OF_HOUR.ordinal()] = 28;
        } catch (NoSuchFieldError e21) {
        }
        try {
            iArr[ChronoField.MONTH_OF_YEAR.ordinal()] = 29;
        } catch (NoSuchFieldError e22) {
        }
        try {
            iArr[ChronoField.NANO_OF_DAY.ordinal()] = 30;
        } catch (NoSuchFieldError e23) {
        }
        try {
            iArr[ChronoField.NANO_OF_SECOND.ordinal()] = 31;
        } catch (NoSuchFieldError e24) {
        }
        try {
            iArr[ChronoField.OFFSET_SECONDS.ordinal()] = 32;
        } catch (NoSuchFieldError e25) {
        }
        try {
            iArr[ChronoField.PROLEPTIC_MONTH.ordinal()] = 33;
        } catch (NoSuchFieldError e26) {
        }
        try {
            iArr[ChronoField.SECOND_OF_DAY.ordinal()] = 34;
        } catch (NoSuchFieldError e27) {
        }
        try {
            iArr[ChronoField.SECOND_OF_MINUTE.ordinal()] = 35;
        } catch (NoSuchFieldError e28) {
        }
        try {
            iArr[ChronoField.YEAR.ordinal()] = 2;
        } catch (NoSuchFieldError e29) {
        }
        try {
            iArr[ChronoField.YEAR_OF_ERA.ordinal()] = 3;
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
            iArr[ChronoUnit.CENTURIES.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[ChronoUnit.DAYS.ordinal()] = 9;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[ChronoUnit.DECADES.ordinal()] = 2;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[ChronoUnit.ERAS.ordinal()] = 3;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[ChronoUnit.FOREVER.ordinal()] = 10;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[ChronoUnit.HALF_DAYS.ordinal()] = 11;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[ChronoUnit.HOURS.ordinal()] = 12;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[ChronoUnit.MICROS.ordinal()] = 13;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[ChronoUnit.MILLENNIA.ordinal()] = 4;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[ChronoUnit.MILLIS.ordinal()] = 14;
        } catch (NoSuchFieldError e10) {
        }
        try {
            iArr[ChronoUnit.MINUTES.ordinal()] = 15;
        } catch (NoSuchFieldError e11) {
        }
        try {
            iArr[ChronoUnit.MONTHS.ordinal()] = 16;
        } catch (NoSuchFieldError e12) {
        }
        try {
            iArr[ChronoUnit.NANOS.ordinal()] = 17;
        } catch (NoSuchFieldError e13) {
        }
        try {
            iArr[ChronoUnit.SECONDS.ordinal()] = 18;
        } catch (NoSuchFieldError e14) {
        }
        try {
            iArr[ChronoUnit.WEEKS.ordinal()] = 19;
        } catch (NoSuchFieldError e15) {
        }
        try {
            iArr[ChronoUnit.YEARS.ordinal()] = 5;
        } catch (NoSuchFieldError e16) {
        }
        -java-time-temporal-ChronoUnitSwitchesValues = iArr;
        return iArr;
    }

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
        Objects.requireNonNull((Object) temporal, "temporal");
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
        Objects.requireNonNull((Object) formatter, "formatter");
        return (Year) formatter.parse(text, new -$Lambda$fVh5P-wBj8FimrTYYrgyoPRHOwQ());
    }

    public static boolean isLeap(long year) {
        return (3 & year) == 0 && (year % 100 != 0 || year % 400 == 0);
    }

    private Year(int year) {
        this.year = year;
    }

    public int getValue() {
        return this.year;
    }

    public boolean isSupported(TemporalField field) {
        boolean z = true;
        boolean z2 = false;
        if (field instanceof ChronoField) {
            if (!(field == ChronoField.YEAR || field == ChronoField.YEAR_OF_ERA || field == ChronoField.ERA)) {
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
            if (!(unit == ChronoUnit.YEARS || unit == ChronoUnit.DECADES || unit == ChronoUnit.CENTURIES || unit == ChronoUnit.MILLENNIA || unit == ChronoUnit.ERAS)) {
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
        if (field != ChronoField.YEAR_OF_ERA) {
            return super.range(field);
        }
        return this.year <= 0 ? ValueRange.of(1, 1000000000) : ValueRange.of(1, 999999999);
    }

    public int get(TemporalField field) {
        return range(field).checkValidIntValue(getLong(field), field);
    }

    public long getLong(TemporalField field) {
        if (!(field instanceof ChronoField)) {
            return field.getFrom(this);
        }
        switch (-getjava-time-temporal-ChronoFieldSwitchesValues()[((ChronoField) field).ordinal()]) {
            case 1:
                return (long) (this.year < 1 ? 0 : 1);
            case 2:
                return (long) this.year;
            case 3:
                return (long) (this.year < 1 ? 1 - this.year : this.year);
            default:
                throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
        }
    }

    public boolean isLeap() {
        return isLeap((long) this.year);
    }

    public boolean isValidMonthDay(MonthDay monthDay) {
        return monthDay != null ? monthDay.isValidYear(this.year) : false;
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
        switch (-getjava-time-temporal-ChronoFieldSwitchesValues()[f.ordinal()]) {
            case 1:
                if (getLong(ChronoField.ERA) != newValue) {
                    this = of(1 - this.year);
                }
                return this;
            case 2:
                return of((int) newValue);
            case 3:
                if (this.year < 1) {
                    newValue = 1 - newValue;
                }
                return of((int) newValue);
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
        switch (-getjava-time-temporal-ChronoUnitSwitchesValues()[((ChronoUnit) unit).ordinal()]) {
            case 1:
                return plusYears(Math.multiplyExact(amountToAdd, 100));
            case 2:
                return plusYears(Math.multiplyExact(amountToAdd, 10));
            case 3:
                return with(ChronoField.ERA, Math.addExact(getLong(ChronoField.ERA), amountToAdd));
            case 4:
                return plusYears(Math.multiplyExact(amountToAdd, 1000));
            case 5:
                return plusYears(amountToAdd);
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
        switch (-getjava-time-temporal-ChronoUnitSwitchesValues()[((ChronoUnit) unit).ordinal()]) {
            case 1:
                return yearsUntil / 100;
            case 2:
                return yearsUntil / 10;
            case 3:
                return end.getLong(ChronoField.ERA) - getLong(ChronoField.ERA);
            case 4:
                return yearsUntil / 1000;
            case 5:
                return yearsUntil;
            default:
                throw new UnsupportedTemporalTypeException("Unsupported unit: " + unit);
        }
    }

    public String format(DateTimeFormatter formatter) {
        Objects.requireNonNull((Object) formatter, "formatter");
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

    void writeExternal(DataOutput out) throws IOException {
        out.writeInt(this.year);
    }

    static Year readExternal(DataInput in) throws IOException {
        return of(in.readInt());
    }
}
