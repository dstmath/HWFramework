package java.time.chrono;

import java.io.Serializable;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalUnit;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.util.Objects;
import sun.util.locale.LanguageTag;

abstract class ChronoLocalDateImpl<D extends ChronoLocalDate> implements ChronoLocalDate, Temporal, TemporalAdjuster, Serializable {
    private static final /* synthetic */ int[] -java-time-temporal-ChronoUnitSwitchesValues = null;
    private static final long serialVersionUID = 6282433883239719096L;

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
            iArr[ChronoUnit.DAYS.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[ChronoUnit.DECADES.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[ChronoUnit.ERAS.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[ChronoUnit.FOREVER.ordinal()] = 9;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[ChronoUnit.HALF_DAYS.ordinal()] = 10;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[ChronoUnit.HOURS.ordinal()] = 11;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[ChronoUnit.MICROS.ordinal()] = 12;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[ChronoUnit.MILLENNIA.ordinal()] = 5;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[ChronoUnit.MILLIS.ordinal()] = 13;
        } catch (NoSuchFieldError e10) {
        }
        try {
            iArr[ChronoUnit.MINUTES.ordinal()] = 14;
        } catch (NoSuchFieldError e11) {
        }
        try {
            iArr[ChronoUnit.MONTHS.ordinal()] = 6;
        } catch (NoSuchFieldError e12) {
        }
        try {
            iArr[ChronoUnit.NANOS.ordinal()] = 15;
        } catch (NoSuchFieldError e13) {
        }
        try {
            iArr[ChronoUnit.SECONDS.ordinal()] = 16;
        } catch (NoSuchFieldError e14) {
        }
        try {
            iArr[ChronoUnit.WEEKS.ordinal()] = 7;
        } catch (NoSuchFieldError e15) {
        }
        try {
            iArr[ChronoUnit.YEARS.ordinal()] = 8;
        } catch (NoSuchFieldError e16) {
        }
        -java-time-temporal-ChronoUnitSwitchesValues = iArr;
        return iArr;
    }

    abstract D plusDays(long j);

    abstract D plusMonths(long j);

    abstract D plusYears(long j);

    static <D extends ChronoLocalDate> D ensureValid(Chronology chrono, Temporal temporal) {
        ChronoLocalDate other = (ChronoLocalDate) temporal;
        if (chrono.equals(other.getChronology())) {
            return other;
        }
        throw new ClassCastException("Chronology mismatch, expected: " + chrono.getId() + ", actual: " + other.getChronology().getId());
    }

    ChronoLocalDateImpl() {
    }

    public D with(TemporalAdjuster adjuster) {
        return super.with(adjuster);
    }

    public D with(TemporalField field, long value) {
        return super.with(field, value);
    }

    public D plus(TemporalAmount amount) {
        return super.plus(amount);
    }

    public D plus(long amountToAdd, TemporalUnit unit) {
        if (!(unit instanceof ChronoUnit)) {
            return super.plus(amountToAdd, unit);
        }
        switch (-getjava-time-temporal-ChronoUnitSwitchesValues()[((ChronoUnit) unit).ordinal()]) {
            case 1:
                return plusYears(Math.multiplyExact(amountToAdd, 100));
            case 2:
                return plusDays(amountToAdd);
            case 3:
                return plusYears(Math.multiplyExact(amountToAdd, 10));
            case 4:
                return with(ChronoField.ERA, Math.addExact(getLong(ChronoField.ERA), amountToAdd));
            case 5:
                return plusYears(Math.multiplyExact(amountToAdd, 1000));
            case 6:
                return plusMonths(amountToAdd);
            case 7:
                return plusDays(Math.multiplyExact(amountToAdd, 7));
            case 8:
                return plusYears(amountToAdd);
            default:
                throw new UnsupportedTemporalTypeException("Unsupported unit: " + unit);
        }
    }

    public D minus(TemporalAmount amount) {
        return super.minus(amount);
    }

    public D minus(long amountToSubtract, TemporalUnit unit) {
        return super.minus(amountToSubtract, unit);
    }

    D plusWeeks(long weeksToAdd) {
        return plusDays(Math.multiplyExact(weeksToAdd, 7));
    }

    D minusYears(long yearsToSubtract) {
        return yearsToSubtract == Long.MIN_VALUE ? ((ChronoLocalDateImpl) plusYears(Long.MAX_VALUE)).plusYears(1) : plusYears(-yearsToSubtract);
    }

    D minusMonths(long monthsToSubtract) {
        return monthsToSubtract == Long.MIN_VALUE ? ((ChronoLocalDateImpl) plusMonths(Long.MAX_VALUE)).plusMonths(1) : plusMonths(-monthsToSubtract);
    }

    D minusWeeks(long weeksToSubtract) {
        return weeksToSubtract == Long.MIN_VALUE ? ((ChronoLocalDateImpl) plusWeeks(Long.MAX_VALUE)).plusWeeks(1) : plusWeeks(-weeksToSubtract);
    }

    D minusDays(long daysToSubtract) {
        return daysToSubtract == Long.MIN_VALUE ? ((ChronoLocalDateImpl) plusDays(Long.MAX_VALUE)).plusDays(1) : plusDays(-daysToSubtract);
    }

    public long until(Temporal endExclusive, TemporalUnit unit) {
        Objects.requireNonNull((Object) endExclusive, "endExclusive");
        ChronoLocalDate end = getChronology().date(endExclusive);
        if (unit instanceof ChronoUnit) {
            switch (-getjava-time-temporal-ChronoUnitSwitchesValues()[((ChronoUnit) unit).ordinal()]) {
                case 1:
                    return monthsUntil(end) / 1200;
                case 2:
                    return daysUntil(end);
                case 3:
                    return monthsUntil(end) / 120;
                case 4:
                    return end.getLong(ChronoField.ERA) - getLong(ChronoField.ERA);
                case 5:
                    return monthsUntil(end) / 12000;
                case 6:
                    return monthsUntil(end);
                case 7:
                    return daysUntil(end) / 7;
                case 8:
                    return monthsUntil(end) / 12;
                default:
                    throw new UnsupportedTemporalTypeException("Unsupported unit: " + unit);
            }
        }
        Objects.requireNonNull((Object) unit, "unit");
        return unit.between(this, end);
    }

    private long daysUntil(ChronoLocalDate end) {
        return end.toEpochDay() - toEpochDay();
    }

    private long monthsUntil(ChronoLocalDate end) {
        if (getChronology().range(ChronoField.MONTH_OF_YEAR).getMaximum() != 12) {
            throw new IllegalStateException("ChronoLocalDateImpl only supports Chronologies with 12 months per year");
        }
        return (((end.getLong(ChronoField.PROLEPTIC_MONTH) * 32) + ((long) end.get(ChronoField.DAY_OF_MONTH))) - ((getLong(ChronoField.PROLEPTIC_MONTH) * 32) + ((long) get(ChronoField.DAY_OF_MONTH)))) / 32;
    }

    public boolean equals(Object obj) {
        boolean z = true;
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ChronoLocalDate)) {
            return false;
        }
        if (compareTo((ChronoLocalDate) obj) != 0) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        long epDay = toEpochDay();
        return getChronology().hashCode() ^ ((int) ((epDay >>> 32) ^ epDay));
    }

    public String toString() {
        String str;
        long yoe = getLong(ChronoField.YEAR_OF_ERA);
        long moy = getLong(ChronoField.MONTH_OF_YEAR);
        long dom = getLong(ChronoField.DAY_OF_MONTH);
        StringBuilder buf = new StringBuilder(30);
        StringBuilder append = buf.append(getChronology().toString()).append(" ").append(getEra()).append(" ").append(yoe);
        if (moy < 10) {
            str = "-0";
        } else {
            str = LanguageTag.SEP;
        }
        append = append.append(str).append(moy);
        if (dom < 10) {
            str = "-0";
        } else {
            str = LanguageTag.SEP;
        }
        append.append(str).append(dom);
        return buf.toString();
    }
}
