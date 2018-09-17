package java.time.chrono;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalUnit;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.time.temporal.ValueRange;

public final class HijrahDate extends ChronoLocalDateImpl<HijrahDate> implements ChronoLocalDate, Serializable {
    private static final /* synthetic */ int[] -java-time-temporal-ChronoFieldSwitchesValues = null;
    private static final long serialVersionUID = -5207853542612002020L;
    private final transient HijrahChronology chrono;
    private final transient int dayOfMonth;
    private final transient int monthOfYear;
    private final transient int prolepticYear;

    private static /* synthetic */ int[] -getjava-time-temporal-ChronoFieldSwitchesValues() {
        if (-java-time-temporal-ChronoFieldSwitchesValues != null) {
            return -java-time-temporal-ChronoFieldSwitchesValues;
        }
        int[] iArr = new int[ChronoField.values().length];
        try {
            iArr[ChronoField.ALIGNED_DAY_OF_WEEK_IN_MONTH.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[ChronoField.ALIGNED_DAY_OF_WEEK_IN_YEAR.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[ChronoField.ALIGNED_WEEK_OF_MONTH.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[ChronoField.ALIGNED_WEEK_OF_YEAR.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[ChronoField.AMPM_OF_DAY.ordinal()] = 14;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[ChronoField.CLOCK_HOUR_OF_AMPM.ordinal()] = 15;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[ChronoField.CLOCK_HOUR_OF_DAY.ordinal()] = 16;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[ChronoField.DAY_OF_MONTH.ordinal()] = 5;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[ChronoField.DAY_OF_WEEK.ordinal()] = 6;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[ChronoField.DAY_OF_YEAR.ordinal()] = 7;
        } catch (NoSuchFieldError e10) {
        }
        try {
            iArr[ChronoField.EPOCH_DAY.ordinal()] = 8;
        } catch (NoSuchFieldError e11) {
        }
        try {
            iArr[ChronoField.ERA.ordinal()] = 9;
        } catch (NoSuchFieldError e12) {
        }
        try {
            iArr[ChronoField.HOUR_OF_AMPM.ordinal()] = 17;
        } catch (NoSuchFieldError e13) {
        }
        try {
            iArr[ChronoField.HOUR_OF_DAY.ordinal()] = 18;
        } catch (NoSuchFieldError e14) {
        }
        try {
            iArr[ChronoField.INSTANT_SECONDS.ordinal()] = 19;
        } catch (NoSuchFieldError e15) {
        }
        try {
            iArr[ChronoField.MICRO_OF_DAY.ordinal()] = 20;
        } catch (NoSuchFieldError e16) {
        }
        try {
            iArr[ChronoField.MICRO_OF_SECOND.ordinal()] = 21;
        } catch (NoSuchFieldError e17) {
        }
        try {
            iArr[ChronoField.MILLI_OF_DAY.ordinal()] = 22;
        } catch (NoSuchFieldError e18) {
        }
        try {
            iArr[ChronoField.MILLI_OF_SECOND.ordinal()] = 23;
        } catch (NoSuchFieldError e19) {
        }
        try {
            iArr[ChronoField.MINUTE_OF_DAY.ordinal()] = 24;
        } catch (NoSuchFieldError e20) {
        }
        try {
            iArr[ChronoField.MINUTE_OF_HOUR.ordinal()] = 25;
        } catch (NoSuchFieldError e21) {
        }
        try {
            iArr[ChronoField.MONTH_OF_YEAR.ordinal()] = 10;
        } catch (NoSuchFieldError e22) {
        }
        try {
            iArr[ChronoField.NANO_OF_DAY.ordinal()] = 26;
        } catch (NoSuchFieldError e23) {
        }
        try {
            iArr[ChronoField.NANO_OF_SECOND.ordinal()] = 27;
        } catch (NoSuchFieldError e24) {
        }
        try {
            iArr[ChronoField.OFFSET_SECONDS.ordinal()] = 28;
        } catch (NoSuchFieldError e25) {
        }
        try {
            iArr[ChronoField.PROLEPTIC_MONTH.ordinal()] = 11;
        } catch (NoSuchFieldError e26) {
        }
        try {
            iArr[ChronoField.SECOND_OF_DAY.ordinal()] = 29;
        } catch (NoSuchFieldError e27) {
        }
        try {
            iArr[ChronoField.SECOND_OF_MINUTE.ordinal()] = 30;
        } catch (NoSuchFieldError e28) {
        }
        try {
            iArr[ChronoField.YEAR.ordinal()] = 12;
        } catch (NoSuchFieldError e29) {
        }
        try {
            iArr[ChronoField.YEAR_OF_ERA.ordinal()] = 13;
        } catch (NoSuchFieldError e30) {
        }
        -java-time-temporal-ChronoFieldSwitchesValues = iArr;
        return iArr;
    }

    static HijrahDate of(HijrahChronology chrono, int prolepticYear, int monthOfYear, int dayOfMonth) {
        return new HijrahDate(chrono, prolepticYear, monthOfYear, dayOfMonth);
    }

    static HijrahDate ofEpochDay(HijrahChronology chrono, long epochDay) {
        return new HijrahDate(chrono, epochDay);
    }

    public static HijrahDate now() {
        return now(Clock.systemDefaultZone());
    }

    public static HijrahDate now(ZoneId zone) {
        return now(Clock.system(zone));
    }

    public static HijrahDate now(Clock clock) {
        return ofEpochDay(HijrahChronology.INSTANCE, LocalDate.now(clock).toEpochDay());
    }

    public static HijrahDate of(int prolepticYear, int month, int dayOfMonth) {
        return HijrahChronology.INSTANCE.date(prolepticYear, month, dayOfMonth);
    }

    public static HijrahDate from(TemporalAccessor temporal) {
        return HijrahChronology.INSTANCE.date(temporal);
    }

    private HijrahDate(HijrahChronology chrono, int prolepticYear, int monthOfYear, int dayOfMonth) {
        chrono.getEpochDay(prolepticYear, monthOfYear, dayOfMonth);
        this.chrono = chrono;
        this.prolepticYear = prolepticYear;
        this.monthOfYear = monthOfYear;
        this.dayOfMonth = dayOfMonth;
    }

    private HijrahDate(HijrahChronology chrono, long epochDay) {
        int[] dateInfo = chrono.getHijrahDateInfo((int) epochDay);
        this.chrono = chrono;
        this.prolepticYear = dateInfo[0];
        this.monthOfYear = dateInfo[1];
        this.dayOfMonth = dateInfo[2];
    }

    public HijrahChronology getChronology() {
        return this.chrono;
    }

    public HijrahEra getEra() {
        return HijrahEra.AH;
    }

    public int lengthOfMonth() {
        return this.chrono.getMonthLength(this.prolepticYear, this.monthOfYear);
    }

    public int lengthOfYear() {
        return this.chrono.getYearLength(this.prolepticYear);
    }

    public ValueRange range(TemporalField field) {
        if (!(field instanceof ChronoField)) {
            return field.rangeRefinedBy(this);
        }
        if (isSupported(field)) {
            ChronoField f = (ChronoField) field;
            switch (-getjava-time-temporal-ChronoFieldSwitchesValues()[f.ordinal()]) {
                case 3:
                    return ValueRange.of(1, 5);
                case 5:
                    return ValueRange.of(1, (long) lengthOfMonth());
                case 7:
                    return ValueRange.of(1, (long) lengthOfYear());
                default:
                    return getChronology().range(f);
            }
        }
        throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
    }

    public long getLong(TemporalField field) {
        if (!(field instanceof ChronoField)) {
            return field.getFrom(this);
        }
        switch (-getjava-time-temporal-ChronoFieldSwitchesValues()[((ChronoField) field).ordinal()]) {
            case 1:
                return (long) (((getDayOfWeek() - 1) % 7) + 1);
            case 2:
                return (long) (((getDayOfYear() - 1) % 7) + 1);
            case 3:
                return (long) (((this.dayOfMonth - 1) / 7) + 1);
            case 4:
                return (long) (((getDayOfYear() - 1) / 7) + 1);
            case 5:
                return (long) this.dayOfMonth;
            case 6:
                return (long) getDayOfWeek();
            case 7:
                return (long) getDayOfYear();
            case 8:
                return toEpochDay();
            case 9:
                return (long) getEraValue();
            case 10:
                return (long) this.monthOfYear;
            case 11:
                return getProlepticMonth();
            case 12:
                return (long) this.prolepticYear;
            case 13:
                return (long) this.prolepticYear;
            default:
                throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
        }
    }

    private long getProlepticMonth() {
        return ((((long) this.prolepticYear) * 12) + ((long) this.monthOfYear)) - 1;
    }

    public HijrahDate with(TemporalField field, long newValue) {
        if (!(field instanceof ChronoField)) {
            return (HijrahDate) super.with(field, newValue);
        }
        ChronoField f = (ChronoField) field;
        this.chrono.range(f).checkValidValue(newValue, f);
        int nvalue = (int) newValue;
        switch (-getjava-time-temporal-ChronoFieldSwitchesValues()[f.ordinal()]) {
            case 1:
                return plusDays(newValue - getLong(ChronoField.ALIGNED_DAY_OF_WEEK_IN_MONTH));
            case 2:
                return plusDays(newValue - getLong(ChronoField.ALIGNED_DAY_OF_WEEK_IN_YEAR));
            case 3:
                return plusDays((newValue - getLong(ChronoField.ALIGNED_WEEK_OF_MONTH)) * 7);
            case 4:
                return plusDays((newValue - getLong(ChronoField.ALIGNED_WEEK_OF_YEAR)) * 7);
            case 5:
                return resolvePreviousValid(this.prolepticYear, this.monthOfYear, nvalue);
            case 6:
                return plusDays(newValue - ((long) getDayOfWeek()));
            case 7:
                return plusDays((long) (Math.min(nvalue, lengthOfYear()) - getDayOfYear()));
            case 8:
                return new HijrahDate(this.chrono, newValue);
            case 9:
                return resolvePreviousValid(1 - this.prolepticYear, this.monthOfYear, this.dayOfMonth);
            case 10:
                return resolvePreviousValid(this.prolepticYear, nvalue, this.dayOfMonth);
            case 11:
                return plusMonths(newValue - getProlepticMonth());
            case 12:
                return resolvePreviousValid(nvalue, this.monthOfYear, this.dayOfMonth);
            case 13:
                if (this.prolepticYear < 1) {
                    nvalue = 1 - nvalue;
                }
                return resolvePreviousValid(nvalue, this.monthOfYear, this.dayOfMonth);
            default:
                throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
        }
    }

    private HijrahDate resolvePreviousValid(int prolepticYear, int month, int day) {
        int monthDays = this.chrono.getMonthLength(prolepticYear, month);
        if (day > monthDays) {
            day = monthDays;
        }
        return of(this.chrono, prolepticYear, month, day);
    }

    public HijrahDate with(TemporalAdjuster adjuster) {
        return (HijrahDate) super.with(adjuster);
    }

    public HijrahDate withVariant(HijrahChronology chronology) {
        if (this.chrono == chronology) {
            return this;
        }
        int monthDays = chronology.getDayOfYear(this.prolepticYear, this.monthOfYear);
        int i = this.prolepticYear;
        int i2 = this.monthOfYear;
        if (this.dayOfMonth <= monthDays) {
            monthDays = this.dayOfMonth;
        }
        return of(chronology, i, i2, monthDays);
    }

    public HijrahDate plus(TemporalAmount amount) {
        return (HijrahDate) super.plus(amount);
    }

    public HijrahDate minus(TemporalAmount amount) {
        return (HijrahDate) super.minus(amount);
    }

    public long toEpochDay() {
        return this.chrono.getEpochDay(this.prolepticYear, this.monthOfYear, this.dayOfMonth);
    }

    private int getDayOfYear() {
        return this.chrono.getDayOfYear(this.prolepticYear, this.monthOfYear) + this.dayOfMonth;
    }

    private int getDayOfWeek() {
        return ((int) Math.floorMod(toEpochDay() + 3, 7)) + 1;
    }

    private int getEraValue() {
        return this.prolepticYear > 1 ? 1 : 0;
    }

    public boolean isLeapYear() {
        return this.chrono.isLeapYear((long) this.prolepticYear);
    }

    HijrahDate plusYears(long years) {
        if (years == 0) {
            return this;
        }
        return resolvePreviousValid(Math.addExact(this.prolepticYear, (int) years), this.monthOfYear, this.dayOfMonth);
    }

    HijrahDate plusMonths(long monthsToAdd) {
        if (monthsToAdd == 0) {
            return this;
        }
        long calcMonths = ((((long) this.prolepticYear) * 12) + ((long) (this.monthOfYear - 1))) + monthsToAdd;
        return resolvePreviousValid(this.chrono.checkValidYear(Math.floorDiv(calcMonths, 12)), ((int) Math.floorMod(calcMonths, 12)) + 1, this.dayOfMonth);
    }

    HijrahDate plusWeeks(long weeksToAdd) {
        return (HijrahDate) super.plusWeeks(weeksToAdd);
    }

    HijrahDate plusDays(long days) {
        return new HijrahDate(this.chrono, toEpochDay() + days);
    }

    public HijrahDate plus(long amountToAdd, TemporalUnit unit) {
        return (HijrahDate) super.plus(amountToAdd, unit);
    }

    public HijrahDate minus(long amountToSubtract, TemporalUnit unit) {
        return (HijrahDate) super.minus(amountToSubtract, unit);
    }

    HijrahDate minusYears(long yearsToSubtract) {
        return (HijrahDate) super.minusYears(yearsToSubtract);
    }

    HijrahDate minusMonths(long monthsToSubtract) {
        return (HijrahDate) super.minusMonths(monthsToSubtract);
    }

    HijrahDate minusWeeks(long weeksToSubtract) {
        return (HijrahDate) super.minusWeeks(weeksToSubtract);
    }

    HijrahDate minusDays(long daysToSubtract) {
        return (HijrahDate) super.minusDays(daysToSubtract);
    }

    public final ChronoLocalDateTime<HijrahDate> atTime(LocalTime localTime) {
        return super.atTime(localTime);
    }

    public ChronoPeriod until(ChronoLocalDate endDate) {
        HijrahDate end = getChronology().date((TemporalAccessor) endDate);
        long totalMonths = (long) (((end.prolepticYear - this.prolepticYear) * 12) + (end.monthOfYear - this.monthOfYear));
        int days = end.dayOfMonth - this.dayOfMonth;
        if (totalMonths > 0 && days < 0) {
            totalMonths--;
            days = (int) (end.toEpochDay() - plusMonths(totalMonths).toEpochDay());
        } else if (totalMonths < 0 && days > 0) {
            totalMonths++;
            days -= end.lengthOfMonth();
        }
        return getChronology().period(Math.toIntExact(totalMonths / 12), (int) (totalMonths % 12), days);
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof HijrahDate)) {
            return false;
        }
        HijrahDate otherDate = (HijrahDate) obj;
        if (this.prolepticYear == otherDate.prolepticYear && this.monthOfYear == otherDate.monthOfYear && this.dayOfMonth == otherDate.dayOfMonth) {
            z = getChronology().equals(otherDate.getChronology());
        }
        return z;
    }

    public int hashCode() {
        int yearValue = this.prolepticYear;
        int monthValue = this.monthOfYear;
        return (getChronology().getId().hashCode() ^ (yearValue & -2048)) ^ (((yearValue << 11) + (monthValue << 6)) + this.dayOfMonth);
    }

    private void readObject(ObjectInputStream s) throws InvalidObjectException {
        throw new InvalidObjectException("Deserialization via serialization delegate");
    }

    private Object writeReplace() {
        return new Ser((byte) 6, this);
    }

    void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(getChronology());
        out.writeInt(get(ChronoField.YEAR));
        out.writeByte(get(ChronoField.MONTH_OF_YEAR));
        out.writeByte(get(ChronoField.DAY_OF_MONTH));
    }

    static HijrahDate readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        return ((HijrahChronology) in.readObject()).date(in.readInt(), in.readByte(), in.readByte());
    }
}
