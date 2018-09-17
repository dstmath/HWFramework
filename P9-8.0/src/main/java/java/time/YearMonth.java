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
    private static final /* synthetic */ int[] -java-time-temporal-ChronoFieldSwitchesValues = null;
    private static final /* synthetic */ int[] -java-time-temporal-ChronoUnitSwitchesValues = null;
    private static final DateTimeFormatter PARSER = new DateTimeFormatterBuilder().appendValue(ChronoField.YEAR, 4, 10, SignStyle.EXCEEDS_PAD).appendLiteral('-').appendValue(ChronoField.MONTH_OF_YEAR, 2).toFormatter();
    private static final long serialVersionUID = 4183400860270640070L;
    private final int month;
    private final int year;

    private static /* synthetic */ int[] -getjava-time-temporal-ChronoFieldSwitchesValues() {
        if (-java-time-temporal-ChronoFieldSwitchesValues != null) {
            return -java-time-temporal-ChronoFieldSwitchesValues;
        }
        int[] iArr = new int[ChronoField.values().length];
        try {
            iArr[ChronoField.ALIGNED_DAY_OF_WEEK_IN_MONTH.ordinal()] = 12;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[ChronoField.ALIGNED_DAY_OF_WEEK_IN_YEAR.ordinal()] = 13;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[ChronoField.ALIGNED_WEEK_OF_MONTH.ordinal()] = 14;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[ChronoField.ALIGNED_WEEK_OF_YEAR.ordinal()] = 15;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[ChronoField.AMPM_OF_DAY.ordinal()] = 16;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[ChronoField.CLOCK_HOUR_OF_AMPM.ordinal()] = 17;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[ChronoField.CLOCK_HOUR_OF_DAY.ordinal()] = 18;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[ChronoField.DAY_OF_MONTH.ordinal()] = 19;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[ChronoField.DAY_OF_WEEK.ordinal()] = 20;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[ChronoField.DAY_OF_YEAR.ordinal()] = 21;
        } catch (NoSuchFieldError e10) {
        }
        try {
            iArr[ChronoField.EPOCH_DAY.ordinal()] = 22;
        } catch (NoSuchFieldError e11) {
        }
        try {
            iArr[ChronoField.ERA.ordinal()] = 1;
        } catch (NoSuchFieldError e12) {
        }
        try {
            iArr[ChronoField.HOUR_OF_AMPM.ordinal()] = 23;
        } catch (NoSuchFieldError e13) {
        }
        try {
            iArr[ChronoField.HOUR_OF_DAY.ordinal()] = 24;
        } catch (NoSuchFieldError e14) {
        }
        try {
            iArr[ChronoField.INSTANT_SECONDS.ordinal()] = 25;
        } catch (NoSuchFieldError e15) {
        }
        try {
            iArr[ChronoField.MICRO_OF_DAY.ordinal()] = 26;
        } catch (NoSuchFieldError e16) {
        }
        try {
            iArr[ChronoField.MICRO_OF_SECOND.ordinal()] = 27;
        } catch (NoSuchFieldError e17) {
        }
        try {
            iArr[ChronoField.MILLI_OF_DAY.ordinal()] = 28;
        } catch (NoSuchFieldError e18) {
        }
        try {
            iArr[ChronoField.MILLI_OF_SECOND.ordinal()] = 29;
        } catch (NoSuchFieldError e19) {
        }
        try {
            iArr[ChronoField.MINUTE_OF_DAY.ordinal()] = 30;
        } catch (NoSuchFieldError e20) {
        }
        try {
            iArr[ChronoField.MINUTE_OF_HOUR.ordinal()] = 31;
        } catch (NoSuchFieldError e21) {
        }
        try {
            iArr[ChronoField.MONTH_OF_YEAR.ordinal()] = 2;
        } catch (NoSuchFieldError e22) {
        }
        try {
            iArr[ChronoField.NANO_OF_DAY.ordinal()] = 32;
        } catch (NoSuchFieldError e23) {
        }
        try {
            iArr[ChronoField.NANO_OF_SECOND.ordinal()] = 33;
        } catch (NoSuchFieldError e24) {
        }
        try {
            iArr[ChronoField.OFFSET_SECONDS.ordinal()] = 34;
        } catch (NoSuchFieldError e25) {
        }
        try {
            iArr[ChronoField.PROLEPTIC_MONTH.ordinal()] = 3;
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
            iArr[ChronoField.YEAR.ordinal()] = 4;
        } catch (NoSuchFieldError e29) {
        }
        try {
            iArr[ChronoField.YEAR_OF_ERA.ordinal()] = 5;
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
            iArr[ChronoUnit.DAYS.ordinal()] = 12;
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
            iArr[ChronoUnit.FOREVER.ordinal()] = 13;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[ChronoUnit.HALF_DAYS.ordinal()] = 14;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[ChronoUnit.HOURS.ordinal()] = 15;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[ChronoUnit.MICROS.ordinal()] = 16;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[ChronoUnit.MILLENNIA.ordinal()] = 4;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[ChronoUnit.MILLIS.ordinal()] = 17;
        } catch (NoSuchFieldError e10) {
        }
        try {
            iArr[ChronoUnit.MINUTES.ordinal()] = 18;
        } catch (NoSuchFieldError e11) {
        }
        try {
            iArr[ChronoUnit.MONTHS.ordinal()] = 5;
        } catch (NoSuchFieldError e12) {
        }
        try {
            iArr[ChronoUnit.NANOS.ordinal()] = 19;
        } catch (NoSuchFieldError e13) {
        }
        try {
            iArr[ChronoUnit.SECONDS.ordinal()] = 20;
        } catch (NoSuchFieldError e14) {
        }
        try {
            iArr[ChronoUnit.WEEKS.ordinal()] = 21;
        } catch (NoSuchFieldError e15) {
        }
        try {
            iArr[ChronoUnit.YEARS.ordinal()] = 6;
        } catch (NoSuchFieldError e16) {
        }
        -java-time-temporal-ChronoUnitSwitchesValues = iArr;
        return iArr;
    }

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

    public static YearMonth of(int year, Month month) {
        Objects.requireNonNull((Object) month, "month");
        return of(year, month.getValue());
    }

    public static YearMonth of(int year, int month) {
        ChronoField.YEAR.checkValidValue((long) year);
        ChronoField.MONTH_OF_YEAR.checkValidValue((long) month);
        return new YearMonth(year, month);
    }

    public static YearMonth from(TemporalAccessor temporal) {
        if (temporal instanceof YearMonth) {
            return (YearMonth) temporal;
        }
        Objects.requireNonNull((Object) temporal, "temporal");
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
        Objects.requireNonNull((Object) formatter, "formatter");
        return (YearMonth) formatter.parse(text, new -$Lambda$aELGMC1uCYxFpc5aQvo_XULMBBo());
    }

    private YearMonth(int year, int month) {
        this.year = year;
        this.month = month;
    }

    private YearMonth with(int newYear, int newMonth) {
        if (this.year == newYear && this.month == newMonth) {
            return this;
        }
        return new YearMonth(newYear, newMonth);
    }

    public boolean isSupported(TemporalField field) {
        boolean z = true;
        boolean z2 = false;
        if (field instanceof ChronoField) {
            if (!(field == ChronoField.YEAR || field == ChronoField.MONTH_OF_YEAR || field == ChronoField.PROLEPTIC_MONTH || field == ChronoField.YEAR_OF_ERA || field == ChronoField.ERA)) {
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
            if (!(unit == ChronoUnit.MONTHS || unit == ChronoUnit.YEARS || unit == ChronoUnit.DECADES || unit == ChronoUnit.CENTURIES || unit == ChronoUnit.MILLENNIA || unit == ChronoUnit.ERAS)) {
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
        return getYear() <= 0 ? ValueRange.of(1, 1000000000) : ValueRange.of(1, 999999999);
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
                return (long) this.month;
            case 3:
                return getProlepticMonth();
            case 4:
                return (long) this.year;
            case 5:
                return (long) (this.year < 1 ? 1 - this.year : this.year);
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
        switch (-getjava-time-temporal-ChronoFieldSwitchesValues()[f.ordinal()]) {
            case 1:
                if (getLong(ChronoField.ERA) != newValue) {
                    this = withYear(1 - this.year);
                }
                return this;
            case 2:
                return withMonth((int) newValue);
            case 3:
                return plusMonths(newValue - getProlepticMonth());
            case 4:
                return withYear((int) newValue);
            case 5:
                if (this.year < 1) {
                    newValue = 1 - newValue;
                }
                return withYear((int) newValue);
            default:
                throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
        }
    }

    public YearMonth withYear(int year) {
        ChronoField.YEAR.checkValidValue((long) year);
        return with(year, this.month);
    }

    public YearMonth withMonth(int month) {
        ChronoField.MONTH_OF_YEAR.checkValidValue((long) month);
        return with(this.year, month);
    }

    public YearMonth plus(TemporalAmount amountToAdd) {
        return (YearMonth) amountToAdd.addTo(this);
    }

    public YearMonth plus(long amountToAdd, TemporalUnit unit) {
        if (!(unit instanceof ChronoUnit)) {
            return (YearMonth) unit.addTo(this, amountToAdd);
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
                return plusMonths(amountToAdd);
            case 6:
                return plusYears(amountToAdd);
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
        long calcMonths = ((((long) this.year) * 12) + ((long) (this.month - 1))) + monthsToAdd;
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
        switch (-getjava-time-temporal-ChronoUnitSwitchesValues()[((ChronoUnit) unit).ordinal()]) {
            case 1:
                return monthsUntil / 1200;
            case 2:
                return monthsUntil / 120;
            case 3:
                return end.getLong(ChronoField.ERA) - getLong(ChronoField.ERA);
            case 4:
                return monthsUntil / 12000;
            case 5:
                return monthsUntil;
            case 6:
                return monthsUntil / 12;
            default:
                throw new UnsupportedTemporalTypeException("Unsupported unit: " + unit);
        }
    }

    public String format(DateTimeFormatter formatter) {
        Objects.requireNonNull((Object) formatter, "formatter");
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
            buf.append(this.year - 10000).deleteCharAt(1);
        } else {
            buf.append(this.year + 10000).deleteCharAt(0);
        }
        return buf.append(this.month < 10 ? "-0" : LanguageTag.SEP).append(this.month).toString();
    }

    private Object writeReplace() {
        return new Ser((byte) 12, this);
    }

    private void readObject(ObjectInputStream s) throws InvalidObjectException {
        throw new InvalidObjectException("Deserialization via serialization delegate");
    }

    void writeExternal(DataOutput out) throws IOException {
        out.writeInt(this.year);
        out.writeByte(this.month);
    }

    static YearMonth readExternal(DataInput in) throws IOException {
        return of(in.readInt(), in.readByte());
    }
}
