package java.time;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.time.chrono.ChronoLocalDate;
import java.time.chrono.Era;
import java.time.chrono.IsoChronology;
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
import java.util.Objects;
import sun.util.locale.LanguageTag;

public final class LocalDate implements Temporal, TemporalAdjuster, ChronoLocalDate, Serializable {
    private static final /* synthetic */ int[] -java-time-temporal-ChronoFieldSwitchesValues = null;
    private static final /* synthetic */ int[] -java-time-temporal-ChronoUnitSwitchesValues = null;
    static final long DAYS_0000_TO_1970 = 719528;
    private static final int DAYS_PER_CYCLE = 146097;
    public static final LocalDate MAX = of((int) Year.MAX_VALUE, 12, 31);
    public static final LocalDate MIN = of((int) Year.MIN_VALUE, 1, 1);
    private static final long serialVersionUID = 2942565459149668126L;
    private final short day;
    private final short month;
    private final int year;

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
            iArr[ChronoField.AMPM_OF_DAY.ordinal()] = 22;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[ChronoField.CLOCK_HOUR_OF_AMPM.ordinal()] = 23;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[ChronoField.CLOCK_HOUR_OF_DAY.ordinal()] = 24;
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
            iArr[ChronoField.HOUR_OF_AMPM.ordinal()] = 25;
        } catch (NoSuchFieldError e13) {
        }
        try {
            iArr[ChronoField.HOUR_OF_DAY.ordinal()] = 26;
        } catch (NoSuchFieldError e14) {
        }
        try {
            iArr[ChronoField.INSTANT_SECONDS.ordinal()] = 27;
        } catch (NoSuchFieldError e15) {
        }
        try {
            iArr[ChronoField.MICRO_OF_DAY.ordinal()] = 28;
        } catch (NoSuchFieldError e16) {
        }
        try {
            iArr[ChronoField.MICRO_OF_SECOND.ordinal()] = 29;
        } catch (NoSuchFieldError e17) {
        }
        try {
            iArr[ChronoField.MILLI_OF_DAY.ordinal()] = 30;
        } catch (NoSuchFieldError e18) {
        }
        try {
            iArr[ChronoField.MILLI_OF_SECOND.ordinal()] = 31;
        } catch (NoSuchFieldError e19) {
        }
        try {
            iArr[ChronoField.MINUTE_OF_DAY.ordinal()] = 32;
        } catch (NoSuchFieldError e20) {
        }
        try {
            iArr[ChronoField.MINUTE_OF_HOUR.ordinal()] = 33;
        } catch (NoSuchFieldError e21) {
        }
        try {
            iArr[ChronoField.MONTH_OF_YEAR.ordinal()] = 10;
        } catch (NoSuchFieldError e22) {
        }
        try {
            iArr[ChronoField.NANO_OF_DAY.ordinal()] = 34;
        } catch (NoSuchFieldError e23) {
        }
        try {
            iArr[ChronoField.NANO_OF_SECOND.ordinal()] = 35;
        } catch (NoSuchFieldError e24) {
        }
        try {
            iArr[ChronoField.OFFSET_SECONDS.ordinal()] = 36;
        } catch (NoSuchFieldError e25) {
        }
        try {
            iArr[ChronoField.PROLEPTIC_MONTH.ordinal()] = 11;
        } catch (NoSuchFieldError e26) {
        }
        try {
            iArr[ChronoField.SECOND_OF_DAY.ordinal()] = 37;
        } catch (NoSuchFieldError e27) {
        }
        try {
            iArr[ChronoField.SECOND_OF_MINUTE.ordinal()] = 38;
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
            iArr[ChronoUnit.FOREVER.ordinal()] = 22;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[ChronoUnit.HALF_DAYS.ordinal()] = 23;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[ChronoUnit.HOURS.ordinal()] = 24;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[ChronoUnit.MICROS.ordinal()] = 25;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[ChronoUnit.MILLENNIA.ordinal()] = 5;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[ChronoUnit.MILLIS.ordinal()] = 26;
        } catch (NoSuchFieldError e10) {
        }
        try {
            iArr[ChronoUnit.MINUTES.ordinal()] = 27;
        } catch (NoSuchFieldError e11) {
        }
        try {
            iArr[ChronoUnit.MONTHS.ordinal()] = 6;
        } catch (NoSuchFieldError e12) {
        }
        try {
            iArr[ChronoUnit.NANOS.ordinal()] = 28;
        } catch (NoSuchFieldError e13) {
        }
        try {
            iArr[ChronoUnit.SECONDS.ordinal()] = 29;
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

    public static LocalDate now() {
        return now(Clock.systemDefaultZone());
    }

    public static LocalDate now(ZoneId zone) {
        return now(Clock.system(zone));
    }

    public static LocalDate now(Clock clock) {
        Objects.requireNonNull((Object) clock, "clock");
        Instant now = clock.instant();
        return ofEpochDay(Math.floorDiv(now.getEpochSecond() + ((long) clock.getZone().getRules().getOffset(now).getTotalSeconds()), 86400));
    }

    public static LocalDate of(int year, Month month, int dayOfMonth) {
        ChronoField.YEAR.checkValidValue((long) year);
        Objects.requireNonNull((Object) month, "month");
        ChronoField.DAY_OF_MONTH.checkValidValue((long) dayOfMonth);
        return create(year, month.getValue(), dayOfMonth);
    }

    public static LocalDate of(int year, int month, int dayOfMonth) {
        ChronoField.YEAR.checkValidValue((long) year);
        ChronoField.MONTH_OF_YEAR.checkValidValue((long) month);
        ChronoField.DAY_OF_MONTH.checkValidValue((long) dayOfMonth);
        return create(year, month, dayOfMonth);
    }

    public static LocalDate ofYearDay(int year, int dayOfYear) {
        ChronoField.YEAR.checkValidValue((long) year);
        ChronoField.DAY_OF_YEAR.checkValidValue((long) dayOfYear);
        boolean leap = IsoChronology.INSTANCE.isLeapYear((long) year);
        if (dayOfYear != 366 || leap) {
            Month moy = Month.of(((dayOfYear - 1) / 31) + 1);
            if (dayOfYear > (moy.firstDayOfYear(leap) + moy.length(leap)) - 1) {
                moy = moy.plus(1);
            }
            return new LocalDate(year, moy.getValue(), (dayOfYear - moy.firstDayOfYear(leap)) + 1);
        }
        throw new DateTimeException("Invalid date 'DayOfYear 366' as '" + year + "' is not a leap year");
    }

    public static LocalDate ofEpochDay(long epochDay) {
        long zeroDay = (epochDay + DAYS_0000_TO_1970) - 60;
        long adjust = 0;
        if (zeroDay < 0) {
            long adjustCycles = ((1 + zeroDay) / 146097) - 1;
            adjust = adjustCycles * 400;
            zeroDay += (-adjustCycles) * 146097;
        }
        long yearEst = ((400 * zeroDay) + 591) / 146097;
        long doyEst = zeroDay - ((((365 * yearEst) + (yearEst / 4)) - (yearEst / 100)) + (yearEst / 400));
        if (doyEst < 0) {
            yearEst--;
            doyEst = zeroDay - ((((365 * yearEst) + (yearEst / 4)) - (yearEst / 100)) + (yearEst / 400));
        }
        int marchDoy0 = (int) doyEst;
        int marchMonth0 = ((marchDoy0 * 5) + 2) / 153;
        return new LocalDate(ChronoField.YEAR.checkValidIntValue((yearEst + adjust) + ((long) (marchMonth0 / 10))), ((marchMonth0 + 2) % 12) + 1, (marchDoy0 - (((marchMonth0 * 306) + 5) / 10)) + 1);
    }

    public static LocalDate from(TemporalAccessor temporal) {
        Objects.requireNonNull((Object) temporal, "temporal");
        LocalDate date = (LocalDate) temporal.query(TemporalQueries.localDate());
        if (date != null) {
            return date;
        }
        throw new DateTimeException("Unable to obtain LocalDate from TemporalAccessor: " + temporal + " of type " + temporal.getClass().getName());
    }

    public static LocalDate parse(CharSequence text) {
        return parse(text, DateTimeFormatter.ISO_LOCAL_DATE);
    }

    public static LocalDate parse(CharSequence text, DateTimeFormatter formatter) {
        Objects.requireNonNull((Object) formatter, "formatter");
        return (LocalDate) formatter.parse(text, new -$Lambda$BOzbf-cDF7D5c6v086Gncf-Nj34());
    }

    private static LocalDate create(int year, int month, int dayOfMonth) {
        if (dayOfMonth > 28) {
            int dom = 31;
            switch (month) {
                case 2:
                    if (!IsoChronology.INSTANCE.isLeapYear((long) year)) {
                        dom = 28;
                        break;
                    }
                    dom = 29;
                    break;
                case 4:
                case 6:
                case 9:
                case 11:
                    dom = 30;
                    break;
            }
            if (dayOfMonth > dom) {
                if (dayOfMonth == 29) {
                    throw new DateTimeException("Invalid date 'February 29' as '" + year + "' is not a leap year");
                }
                throw new DateTimeException("Invalid date '" + Month.of(month).name() + " " + dayOfMonth + "'");
            }
        }
        return new LocalDate(year, month, dayOfMonth);
    }

    private static LocalDate resolvePreviousValid(int year, int month, int day) {
        switch (month) {
            case 2:
                day = Math.min(day, IsoChronology.INSTANCE.isLeapYear((long) year) ? 29 : 28);
                break;
            case 4:
            case 6:
            case 9:
            case 11:
                day = Math.min(day, 30);
                break;
        }
        return new LocalDate(year, month, day);
    }

    private LocalDate(int year, int month, int dayOfMonth) {
        this.year = year;
        this.month = (short) month;
        this.day = (short) dayOfMonth;
    }

    public boolean isSupported(TemporalField field) {
        return super.isSupported(field);
    }

    public boolean isSupported(TemporalUnit unit) {
        return super.isSupported(unit);
    }

    public ValueRange range(TemporalField field) {
        if (!(field instanceof ChronoField)) {
            return field.rangeRefinedBy(this);
        }
        ChronoField f = (ChronoField) field;
        if (f.isDateBased()) {
            switch (-getjava-time-temporal-ChronoFieldSwitchesValues()[f.ordinal()]) {
                case 3:
                    int i = (getMonth() != Month.FEBRUARY || isLeapYear()) ? 5 : 4;
                    return ValueRange.of(1, (long) i);
                case 5:
                    return ValueRange.of(1, (long) lengthOfMonth());
                case 7:
                    return ValueRange.of(1, (long) lengthOfYear());
                case 13:
                    return getYear() <= 0 ? ValueRange.of(1, 1000000000) : ValueRange.of(1, 999999999);
                default:
                    return field.range();
            }
        }
        throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
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
        if (field == ChronoField.EPOCH_DAY) {
            return toEpochDay();
        }
        if (field == ChronoField.PROLEPTIC_MONTH) {
            return getProlepticMonth();
        }
        return (long) get0(field);
    }

    private int get0(TemporalField field) {
        switch (-getjava-time-temporal-ChronoFieldSwitchesValues()[((ChronoField) field).ordinal()]) {
            case 1:
                return ((this.day - 1) % 7) + 1;
            case 2:
                return ((getDayOfYear() - 1) % 7) + 1;
            case 3:
                return ((this.day - 1) / 7) + 1;
            case 4:
                return ((getDayOfYear() - 1) / 7) + 1;
            case 5:
                return this.day;
            case 6:
                return getDayOfWeek().getValue();
            case 7:
                return getDayOfYear();
            case 8:
                throw new UnsupportedTemporalTypeException("Invalid field 'EpochDay' for get() method, use getLong() instead");
            case 9:
                return this.year >= 1 ? 1 : 0;
            case 10:
                return this.month;
            case 11:
                throw new UnsupportedTemporalTypeException("Invalid field 'ProlepticMonth' for get() method, use getLong() instead");
            case 12:
                return this.year;
            case 13:
                return this.year >= 1 ? this.year : 1 - this.year;
            default:
                throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
        }
    }

    private long getProlepticMonth() {
        return ((((long) this.year) * 12) + ((long) this.month)) - 1;
    }

    public IsoChronology getChronology() {
        return IsoChronology.INSTANCE;
    }

    public Era getEra() {
        return super.getEra();
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

    public int getDayOfMonth() {
        return this.day;
    }

    public int getDayOfYear() {
        return (getMonth().firstDayOfYear(isLeapYear()) + this.day) - 1;
    }

    public DayOfWeek getDayOfWeek() {
        return DayOfWeek.of(((int) Math.floorMod(toEpochDay() + 3, 7)) + 1);
    }

    public boolean isLeapYear() {
        return IsoChronology.INSTANCE.isLeapYear((long) this.year);
    }

    public int lengthOfMonth() {
        switch (this.month) {
            case (short) 2:
                return isLeapYear() ? 29 : 28;
            case (short) 4:
            case (short) 6:
            case (short) 9:
            case (short) 11:
                return 30;
            default:
                return 31;
        }
    }

    public int lengthOfYear() {
        return isLeapYear() ? 366 : 365;
    }

    public LocalDate with(TemporalAdjuster adjuster) {
        if (adjuster instanceof LocalDate) {
            return (LocalDate) adjuster;
        }
        return (LocalDate) adjuster.adjustInto(this);
    }

    public LocalDate with(TemporalField field, long newValue) {
        if (!(field instanceof ChronoField)) {
            return (LocalDate) field.adjustInto(this, newValue);
        }
        ChronoField f = (ChronoField) field;
        f.checkValidValue(newValue);
        switch (-getjava-time-temporal-ChronoFieldSwitchesValues()[f.ordinal()]) {
            case 1:
                return plusDays(newValue - getLong(ChronoField.ALIGNED_DAY_OF_WEEK_IN_MONTH));
            case 2:
                return plusDays(newValue - getLong(ChronoField.ALIGNED_DAY_OF_WEEK_IN_YEAR));
            case 3:
                return plusWeeks(newValue - getLong(ChronoField.ALIGNED_WEEK_OF_MONTH));
            case 4:
                return plusWeeks(newValue - getLong(ChronoField.ALIGNED_WEEK_OF_YEAR));
            case 5:
                return withDayOfMonth((int) newValue);
            case 6:
                return plusDays(newValue - ((long) getDayOfWeek().getValue()));
            case 7:
                return withDayOfYear((int) newValue);
            case 8:
                return ofEpochDay(newValue);
            case 9:
                if (getLong(ChronoField.ERA) != newValue) {
                    this = withYear(1 - this.year);
                }
                return this;
            case 10:
                return withMonth((int) newValue);
            case 11:
                return plusMonths(newValue - getProlepticMonth());
            case 12:
                return withYear((int) newValue);
            case 13:
                if (this.year < 1) {
                    newValue = 1 - newValue;
                }
                return withYear((int) newValue);
            default:
                throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
        }
    }

    public LocalDate withYear(int year) {
        if (this.year == year) {
            return this;
        }
        ChronoField.YEAR.checkValidValue((long) year);
        return resolvePreviousValid(year, this.month, this.day);
    }

    public LocalDate withMonth(int month) {
        if (this.month == month) {
            return this;
        }
        ChronoField.MONTH_OF_YEAR.checkValidValue((long) month);
        return resolvePreviousValid(this.year, month, this.day);
    }

    public LocalDate withDayOfMonth(int dayOfMonth) {
        if (this.day == dayOfMonth) {
            return this;
        }
        return of(this.year, this.month, dayOfMonth);
    }

    public LocalDate withDayOfYear(int dayOfYear) {
        if (getDayOfYear() == dayOfYear) {
            return this;
        }
        return ofYearDay(this.year, dayOfYear);
    }

    public LocalDate plus(TemporalAmount amountToAdd) {
        if (amountToAdd instanceof Period) {
            Period periodToAdd = (Period) amountToAdd;
            return plusMonths(periodToAdd.toTotalMonths()).plusDays((long) periodToAdd.getDays());
        }
        Objects.requireNonNull((Object) amountToAdd, "amountToAdd");
        return (LocalDate) amountToAdd.addTo(this);
    }

    public LocalDate plus(long amountToAdd, TemporalUnit unit) {
        if (!(unit instanceof ChronoUnit)) {
            return (LocalDate) unit.addTo(this, amountToAdd);
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
                return plusWeeks(amountToAdd);
            case 8:
                return plusYears(amountToAdd);
            default:
                throw new UnsupportedTemporalTypeException("Unsupported unit: " + unit);
        }
    }

    public LocalDate plusYears(long yearsToAdd) {
        if (yearsToAdd == 0) {
            return this;
        }
        return resolvePreviousValid(ChronoField.YEAR.checkValidIntValue(((long) this.year) + yearsToAdd), this.month, this.day);
    }

    public LocalDate plusMonths(long monthsToAdd) {
        if (monthsToAdd == 0) {
            return this;
        }
        long calcMonths = ((((long) this.year) * 12) + ((long) (this.month - 1))) + monthsToAdd;
        return resolvePreviousValid(ChronoField.YEAR.checkValidIntValue(Math.floorDiv(calcMonths, 12)), ((int) Math.floorMod(calcMonths, 12)) + 1, this.day);
    }

    public LocalDate plusWeeks(long weeksToAdd) {
        return plusDays(Math.multiplyExact(weeksToAdd, 7));
    }

    public LocalDate plusDays(long daysToAdd) {
        if (daysToAdd == 0) {
            return this;
        }
        return ofEpochDay(Math.addExact(toEpochDay(), daysToAdd));
    }

    public LocalDate minus(TemporalAmount amountToSubtract) {
        if (amountToSubtract instanceof Period) {
            Period periodToSubtract = (Period) amountToSubtract;
            return minusMonths(periodToSubtract.toTotalMonths()).minusDays((long) periodToSubtract.getDays());
        }
        Objects.requireNonNull((Object) amountToSubtract, "amountToSubtract");
        return (LocalDate) amountToSubtract.subtractFrom(this);
    }

    public LocalDate minus(long amountToSubtract, TemporalUnit unit) {
        return amountToSubtract == Long.MIN_VALUE ? plus((long) Long.MAX_VALUE, unit).plus(1, unit) : plus(-amountToSubtract, unit);
    }

    public LocalDate minusYears(long yearsToSubtract) {
        return yearsToSubtract == Long.MIN_VALUE ? plusYears(Long.MAX_VALUE).plusYears(1) : plusYears(-yearsToSubtract);
    }

    public LocalDate minusMonths(long monthsToSubtract) {
        return monthsToSubtract == Long.MIN_VALUE ? plusMonths(Long.MAX_VALUE).plusMonths(1) : plusMonths(-monthsToSubtract);
    }

    public LocalDate minusWeeks(long weeksToSubtract) {
        return weeksToSubtract == Long.MIN_VALUE ? plusWeeks(Long.MAX_VALUE).plusWeeks(1) : plusWeeks(-weeksToSubtract);
    }

    public LocalDate minusDays(long daysToSubtract) {
        return daysToSubtract == Long.MIN_VALUE ? plusDays(Long.MAX_VALUE).plusDays(1) : plusDays(-daysToSubtract);
    }

    public <R> R query(TemporalQuery<R> query) {
        if (query == TemporalQueries.localDate()) {
            return this;
        }
        return super.query(query);
    }

    public Temporal adjustInto(Temporal temporal) {
        return super.adjustInto(temporal);
    }

    public long until(Temporal endExclusive, TemporalUnit unit) {
        LocalDate end = from(endExclusive);
        if (!(unit instanceof ChronoUnit)) {
            return unit.between(this, end);
        }
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

    long daysUntil(LocalDate end) {
        return end.toEpochDay() - toEpochDay();
    }

    private long monthsUntil(LocalDate end) {
        return (((end.getProlepticMonth() * 32) + ((long) end.getDayOfMonth())) - ((getProlepticMonth() * 32) + ((long) getDayOfMonth()))) / 32;
    }

    public Period until(ChronoLocalDate endDateExclusive) {
        LocalDate end = from(endDateExclusive);
        long totalMonths = end.getProlepticMonth() - getProlepticMonth();
        int days = end.day - this.day;
        if (totalMonths > 0 && days < 0) {
            totalMonths--;
            days = (int) (end.toEpochDay() - plusMonths(totalMonths).toEpochDay());
        } else if (totalMonths < 0 && days > 0) {
            totalMonths++;
            days -= end.lengthOfMonth();
        }
        return Period.of(Math.toIntExact(totalMonths / 12), (int) (totalMonths % 12), days);
    }

    public String format(DateTimeFormatter formatter) {
        Objects.requireNonNull((Object) formatter, "formatter");
        return formatter.format(this);
    }

    public LocalDateTime atTime(LocalTime time) {
        return LocalDateTime.of(this, time);
    }

    public LocalDateTime atTime(int hour, int minute) {
        return atTime(LocalTime.of(hour, minute));
    }

    public LocalDateTime atTime(int hour, int minute, int second) {
        return atTime(LocalTime.of(hour, minute, second));
    }

    public LocalDateTime atTime(int hour, int minute, int second, int nanoOfSecond) {
        return atTime(LocalTime.of(hour, minute, second, nanoOfSecond));
    }

    public OffsetDateTime atTime(OffsetTime time) {
        return OffsetDateTime.of(LocalDateTime.of(this, time.toLocalTime()), time.getOffset());
    }

    public LocalDateTime atStartOfDay() {
        return LocalDateTime.of(this, LocalTime.MIDNIGHT);
    }

    public ZonedDateTime atStartOfDay(ZoneId zone) {
        Objects.requireNonNull((Object) zone, "zone");
        LocalDateTime ldt = atTime(LocalTime.MIDNIGHT);
        if (!(zone instanceof ZoneOffset)) {
            ZoneOffsetTransition trans = zone.getRules().getTransition(ldt);
            if (trans != null && trans.isGap()) {
                ldt = trans.getDateTimeAfter();
            }
        }
        return ZonedDateTime.of(ldt, zone);
    }

    public long toEpochDay() {
        long y = (long) this.year;
        long m = (long) this.month;
        long total = 0 + (365 * y);
        if (y >= 0) {
            total += (((3 + y) / 4) - ((99 + y) / 100)) + ((399 + y) / 400);
        } else {
            total -= ((y / -4) - (y / -100)) + (y / -400);
        }
        total = (total + (((367 * m) - 362) / 12)) + ((long) (this.day - 1));
        if (m > 2) {
            total--;
            if (!isLeapYear()) {
                total--;
            }
        }
        return total - DAYS_0000_TO_1970;
    }

    public int compareTo(ChronoLocalDate other) {
        if (other instanceof LocalDate) {
            return compareTo0((LocalDate) other);
        }
        return super.compareTo(other);
    }

    int compareTo0(LocalDate otherDate) {
        int cmp = this.year - otherDate.year;
        if (cmp != 0) {
            return cmp;
        }
        cmp = this.month - otherDate.month;
        if (cmp == 0) {
            return this.day - otherDate.day;
        }
        return cmp;
    }

    public boolean isAfter(ChronoLocalDate other) {
        boolean z = false;
        if (!(other instanceof LocalDate)) {
            return super.isAfter(other);
        }
        if (compareTo0((LocalDate) other) > 0) {
            z = true;
        }
        return z;
    }

    public boolean isBefore(ChronoLocalDate other) {
        boolean z = false;
        if (!(other instanceof LocalDate)) {
            return super.isBefore(other);
        }
        if (compareTo0((LocalDate) other) < 0) {
            z = true;
        }
        return z;
    }

    public boolean isEqual(ChronoLocalDate other) {
        boolean z = false;
        if (!(other instanceof LocalDate)) {
            return super.isEqual(other);
        }
        if (compareTo0((LocalDate) other) == 0) {
            z = true;
        }
        return z;
    }

    public boolean equals(Object obj) {
        boolean z = true;
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof LocalDate)) {
            return false;
        }
        if (compareTo0((LocalDate) obj) != 0) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        int yearValue = this.year;
        return (yearValue & -2048) ^ (((yearValue << 11) + (this.month << 6)) + this.day);
    }

    public String toString() {
        int yearValue = this.year;
        int monthValue = this.month;
        int dayValue = this.day;
        int absYear = Math.abs(yearValue);
        StringBuilder buf = new StringBuilder(10);
        if (absYear >= 1000) {
            if (yearValue > 9999) {
                buf.append('+');
            }
            buf.append(yearValue);
        } else if (yearValue < 0) {
            buf.append(yearValue - 10000).deleteCharAt(1);
        } else {
            buf.append(yearValue + 10000).deleteCharAt(0);
        }
        return buf.append(monthValue < 10 ? "-0" : LanguageTag.SEP).append(monthValue).append(dayValue < 10 ? "-0" : LanguageTag.SEP).append(dayValue).toString();
    }

    private Object writeReplace() {
        return new Ser((byte) 3, this);
    }

    private void readObject(ObjectInputStream s) throws InvalidObjectException {
        throw new InvalidObjectException("Deserialization via serialization delegate");
    }

    void writeExternal(DataOutput out) throws IOException {
        out.writeInt(this.year);
        out.writeByte(this.month);
        out.writeByte(this.day);
    }

    static LocalDate readExternal(DataInput in) throws IOException {
        return of(in.readInt(), in.readByte(), in.readByte());
    }
}
