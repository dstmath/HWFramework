package java.time.chrono;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalUnit;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.time.temporal.ValueRange;
import java.util.Objects;

public final class MinguoDate extends ChronoLocalDateImpl<MinguoDate> implements ChronoLocalDate, Serializable {
    private static final /* synthetic */ int[] -java-time-temporal-ChronoFieldSwitchesValues = null;
    private static final long serialVersionUID = 1300372329181994526L;
    private final transient LocalDate isoDate;

    private static /* synthetic */ int[] -getjava-time-temporal-ChronoFieldSwitchesValues() {
        if (-java-time-temporal-ChronoFieldSwitchesValues != null) {
            return -java-time-temporal-ChronoFieldSwitchesValues;
        }
        int[] iArr = new int[ChronoField.values().length];
        try {
            iArr[ChronoField.ALIGNED_DAY_OF_WEEK_IN_MONTH.ordinal()] = 8;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[ChronoField.ALIGNED_DAY_OF_WEEK_IN_YEAR.ordinal()] = 9;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[ChronoField.ALIGNED_WEEK_OF_MONTH.ordinal()] = 1;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[ChronoField.ALIGNED_WEEK_OF_YEAR.ordinal()] = 10;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[ChronoField.AMPM_OF_DAY.ordinal()] = 11;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[ChronoField.CLOCK_HOUR_OF_AMPM.ordinal()] = 12;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[ChronoField.CLOCK_HOUR_OF_DAY.ordinal()] = 13;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[ChronoField.DAY_OF_MONTH.ordinal()] = 2;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[ChronoField.DAY_OF_WEEK.ordinal()] = 14;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[ChronoField.DAY_OF_YEAR.ordinal()] = 3;
        } catch (NoSuchFieldError e10) {
        }
        try {
            iArr[ChronoField.EPOCH_DAY.ordinal()] = 15;
        } catch (NoSuchFieldError e11) {
        }
        try {
            iArr[ChronoField.ERA.ordinal()] = 4;
        } catch (NoSuchFieldError e12) {
        }
        try {
            iArr[ChronoField.HOUR_OF_AMPM.ordinal()] = 16;
        } catch (NoSuchFieldError e13) {
        }
        try {
            iArr[ChronoField.HOUR_OF_DAY.ordinal()] = 17;
        } catch (NoSuchFieldError e14) {
        }
        try {
            iArr[ChronoField.INSTANT_SECONDS.ordinal()] = 18;
        } catch (NoSuchFieldError e15) {
        }
        try {
            iArr[ChronoField.MICRO_OF_DAY.ordinal()] = 19;
        } catch (NoSuchFieldError e16) {
        }
        try {
            iArr[ChronoField.MICRO_OF_SECOND.ordinal()] = 20;
        } catch (NoSuchFieldError e17) {
        }
        try {
            iArr[ChronoField.MILLI_OF_DAY.ordinal()] = 21;
        } catch (NoSuchFieldError e18) {
        }
        try {
            iArr[ChronoField.MILLI_OF_SECOND.ordinal()] = 22;
        } catch (NoSuchFieldError e19) {
        }
        try {
            iArr[ChronoField.MINUTE_OF_DAY.ordinal()] = 23;
        } catch (NoSuchFieldError e20) {
        }
        try {
            iArr[ChronoField.MINUTE_OF_HOUR.ordinal()] = 24;
        } catch (NoSuchFieldError e21) {
        }
        try {
            iArr[ChronoField.MONTH_OF_YEAR.ordinal()] = 25;
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
            iArr[ChronoField.PROLEPTIC_MONTH.ordinal()] = 5;
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
            iArr[ChronoField.YEAR.ordinal()] = 6;
        } catch (NoSuchFieldError e29) {
        }
        try {
            iArr[ChronoField.YEAR_OF_ERA.ordinal()] = 7;
        } catch (NoSuchFieldError e30) {
        }
        -java-time-temporal-ChronoFieldSwitchesValues = iArr;
        return iArr;
    }

    public static MinguoDate now() {
        return now(Clock.systemDefaultZone());
    }

    public static MinguoDate now(ZoneId zone) {
        return now(Clock.system(zone));
    }

    public static MinguoDate now(Clock clock) {
        return new MinguoDate(LocalDate.now(clock));
    }

    public static MinguoDate of(int prolepticYear, int month, int dayOfMonth) {
        return new MinguoDate(LocalDate.of(prolepticYear + 1911, month, dayOfMonth));
    }

    public static MinguoDate from(TemporalAccessor temporal) {
        return MinguoChronology.INSTANCE.date(temporal);
    }

    MinguoDate(LocalDate isoDate) {
        Objects.requireNonNull((Object) isoDate, "isoDate");
        this.isoDate = isoDate;
    }

    public MinguoChronology getChronology() {
        return MinguoChronology.INSTANCE;
    }

    public MinguoEra getEra() {
        return getProlepticYear() >= 1 ? MinguoEra.ROC : MinguoEra.BEFORE_ROC;
    }

    public int lengthOfMonth() {
        return this.isoDate.lengthOfMonth();
    }

    public ValueRange range(TemporalField field) {
        if (!(field instanceof ChronoField)) {
            return field.rangeRefinedBy(this);
        }
        if (isSupported(field)) {
            ChronoField f = (ChronoField) field;
            switch (-getjava-time-temporal-ChronoFieldSwitchesValues()[f.ordinal()]) {
                case 1:
                case 2:
                case 3:
                    return this.isoDate.range(field);
                case 7:
                    ValueRange range = ChronoField.YEAR.range();
                    return ValueRange.of(1, getProlepticYear() <= 0 ? ((-range.getMinimum()) + 1) + 1911 : range.getMaximum() - 1911);
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
            case 4:
                return (long) (getProlepticYear() >= 1 ? 1 : 0);
            case 5:
                return getProlepticMonth();
            case 6:
                return (long) getProlepticYear();
            case 7:
                int prolepticYear = getProlepticYear();
                if (prolepticYear < 1) {
                    prolepticYear = 1 - prolepticYear;
                }
                return (long) prolepticYear;
            default:
                return this.isoDate.getLong(field);
        }
    }

    private long getProlepticMonth() {
        return ((((long) getProlepticYear()) * 12) + ((long) this.isoDate.getMonthValue())) - 1;
    }

    private int getProlepticYear() {
        return this.isoDate.getYear() - 1911;
    }

    public MinguoDate with(TemporalField field, long newValue) {
        if (!(field instanceof ChronoField)) {
            return (MinguoDate) super.with(field, newValue);
        }
        ChronoField f = (ChronoField) field;
        if (getLong(f) == newValue) {
            return this;
        }
        switch (-getjava-time-temporal-ChronoFieldSwitchesValues()[f.ordinal()]) {
            case 4:
            case 6:
            case 7:
                int nvalue = getChronology().range(f).checkValidIntValue(newValue, f);
                switch (-getjava-time-temporal-ChronoFieldSwitchesValues()[f.ordinal()]) {
                    case 4:
                        return with(this.isoDate.withYear((1 - getProlepticYear()) + 1911));
                    case 6:
                        return with(this.isoDate.withYear(nvalue + 1911));
                    case 7:
                        return with(this.isoDate.withYear(getProlepticYear() >= 1 ? nvalue + 1911 : (1 - nvalue) + 1911));
                }
                break;
            case 5:
                getChronology().range(f).checkValidValue(newValue, f);
                return plusMonths(newValue - getProlepticMonth());
        }
        return with(this.isoDate.with(field, newValue));
    }

    public MinguoDate with(TemporalAdjuster adjuster) {
        return (MinguoDate) super.with(adjuster);
    }

    public MinguoDate plus(TemporalAmount amount) {
        return (MinguoDate) super.plus(amount);
    }

    public MinguoDate minus(TemporalAmount amount) {
        return (MinguoDate) super.minus(amount);
    }

    MinguoDate plusYears(long years) {
        return with(this.isoDate.plusYears(years));
    }

    MinguoDate plusMonths(long months) {
        return with(this.isoDate.plusMonths(months));
    }

    MinguoDate plusWeeks(long weeksToAdd) {
        return (MinguoDate) super.plusWeeks(weeksToAdd);
    }

    MinguoDate plusDays(long days) {
        return with(this.isoDate.plusDays(days));
    }

    public MinguoDate plus(long amountToAdd, TemporalUnit unit) {
        return (MinguoDate) super.plus(amountToAdd, unit);
    }

    public MinguoDate minus(long amountToAdd, TemporalUnit unit) {
        return (MinguoDate) super.minus(amountToAdd, unit);
    }

    MinguoDate minusYears(long yearsToSubtract) {
        return (MinguoDate) super.minusYears(yearsToSubtract);
    }

    MinguoDate minusMonths(long monthsToSubtract) {
        return (MinguoDate) super.minusMonths(monthsToSubtract);
    }

    MinguoDate minusWeeks(long weeksToSubtract) {
        return (MinguoDate) super.minusWeeks(weeksToSubtract);
    }

    MinguoDate minusDays(long daysToSubtract) {
        return (MinguoDate) super.minusDays(daysToSubtract);
    }

    private MinguoDate with(LocalDate newDate) {
        return newDate.equals(this.isoDate) ? this : new MinguoDate(newDate);
    }

    public final ChronoLocalDateTime<MinguoDate> atTime(LocalTime localTime) {
        return super.atTime(localTime);
    }

    public ChronoPeriod until(ChronoLocalDate endDate) {
        Period period = this.isoDate.until(endDate);
        return getChronology().period(period.getYears(), period.getMonths(), period.getDays());
    }

    public long toEpochDay() {
        return this.isoDate.toEpochDay();
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof MinguoDate)) {
            return false;
        }
        return this.isoDate.equals(((MinguoDate) obj).isoDate);
    }

    public int hashCode() {
        return getChronology().getId().hashCode() ^ this.isoDate.hashCode();
    }

    private void readObject(ObjectInputStream s) throws InvalidObjectException {
        throw new InvalidObjectException("Deserialization via serialization delegate");
    }

    private Object writeReplace() {
        return new Ser((byte) 7, this);
    }

    void writeExternal(DataOutput out) throws IOException {
        out.writeInt(get(ChronoField.YEAR));
        out.writeByte(get(ChronoField.MONTH_OF_YEAR));
        out.writeByte(get(ChronoField.DAY_OF_MONTH));
    }

    static MinguoDate readExternal(DataInput in) throws IOException {
        return MinguoChronology.INSTANCE.date(in.readInt(), in.readByte(), in.readByte());
    }
}
