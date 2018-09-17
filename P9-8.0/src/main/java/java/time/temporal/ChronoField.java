package java.time.temporal;

import android.icu.text.DateTimePatternGenerator;
import android.icu.util.ULocale;
import java.util.Locale;
import java.util.Objects;

public enum ChronoField implements TemporalField {
    NANO_OF_SECOND("NanoOfSecond", ChronoUnit.NANOS, ChronoUnit.SECONDS, ValueRange.of(0, 999999999)),
    NANO_OF_DAY("NanoOfDay", ChronoUnit.NANOS, ChronoUnit.DAYS, ValueRange.of(0, 86399999999999L)),
    MICRO_OF_SECOND("MicroOfSecond", ChronoUnit.MICROS, ChronoUnit.SECONDS, ValueRange.of(0, 999999)),
    MICRO_OF_DAY("MicroOfDay", ChronoUnit.MICROS, ChronoUnit.DAYS, ValueRange.of(0, 86399999999L)),
    MILLI_OF_SECOND("MilliOfSecond", ChronoUnit.MILLIS, ChronoUnit.SECONDS, ValueRange.of(0, 999)),
    MILLI_OF_DAY("MilliOfDay", ChronoUnit.MILLIS, ChronoUnit.DAYS, ValueRange.of(0, 86399999)),
    SECOND_OF_MINUTE("SecondOfMinute", ChronoUnit.SECONDS, ChronoUnit.MINUTES, ValueRange.of(0, 59), "second"),
    SECOND_OF_DAY("SecondOfDay", ChronoUnit.SECONDS, ChronoUnit.DAYS, ValueRange.of(0, 86399)),
    MINUTE_OF_HOUR("MinuteOfHour", ChronoUnit.MINUTES, ChronoUnit.HOURS, ValueRange.of(0, 59), "minute"),
    MINUTE_OF_DAY("MinuteOfDay", ChronoUnit.MINUTES, ChronoUnit.DAYS, ValueRange.of(0, 1439)),
    HOUR_OF_AMPM("HourOfAmPm", ChronoUnit.HOURS, ChronoUnit.HALF_DAYS, ValueRange.of(0, 11)),
    CLOCK_HOUR_OF_AMPM("ClockHourOfAmPm", ChronoUnit.HOURS, ChronoUnit.HALF_DAYS, ValueRange.of(1, 12)),
    HOUR_OF_DAY("HourOfDay", ChronoUnit.HOURS, ChronoUnit.DAYS, ValueRange.of(0, 23), "hour"),
    CLOCK_HOUR_OF_DAY("ClockHourOfDay", ChronoUnit.HOURS, ChronoUnit.DAYS, ValueRange.of(1, 24)),
    AMPM_OF_DAY("AmPmOfDay", ChronoUnit.HALF_DAYS, ChronoUnit.DAYS, ValueRange.of(0, 1), "dayperiod"),
    DAY_OF_WEEK("DayOfWeek", ChronoUnit.DAYS, ChronoUnit.WEEKS, ValueRange.of(1, 7), "weekday"),
    ALIGNED_DAY_OF_WEEK_IN_MONTH("AlignedDayOfWeekInMonth", ChronoUnit.DAYS, ChronoUnit.WEEKS, ValueRange.of(1, 7)),
    ALIGNED_DAY_OF_WEEK_IN_YEAR("AlignedDayOfWeekInYear", ChronoUnit.DAYS, ChronoUnit.WEEKS, ValueRange.of(1, 7)),
    DAY_OF_MONTH("DayOfMonth", r11, ChronoUnit.MONTHS, ValueRange.of(1, 28, 31), "day"),
    DAY_OF_YEAR("DayOfYear", r10, ChronoUnit.YEARS, ValueRange.of(1, 365, 366)),
    EPOCH_DAY("EpochDay", ChronoUnit.DAYS, ChronoUnit.FOREVER, ValueRange.of(-365249999634L, 365249999634L)),
    ALIGNED_WEEK_OF_MONTH("AlignedWeekOfMonth", r10, ChronoUnit.MONTHS, ValueRange.of(1, 4, 5)),
    ALIGNED_WEEK_OF_YEAR("AlignedWeekOfYear", ChronoUnit.WEEKS, ChronoUnit.YEARS, ValueRange.of(1, 53)),
    MONTH_OF_YEAR("MonthOfYear", ChronoUnit.MONTHS, ChronoUnit.YEARS, ValueRange.of(1, 12), "month"),
    PROLEPTIC_MONTH("ProlepticMonth", ChronoUnit.MONTHS, ChronoUnit.FOREVER, ValueRange.of(-11999999988L, 11999999999L)),
    YEAR_OF_ERA("YearOfEra", r10, ChronoUnit.FOREVER, ValueRange.of(1, 999999999, 1000000000)),
    YEAR("Year", ChronoUnit.YEARS, ChronoUnit.FOREVER, ValueRange.of(-999999999, 999999999), "year"),
    ERA("Era", ChronoUnit.ERAS, ChronoUnit.FOREVER, ValueRange.of(0, 1), "era"),
    INSTANT_SECONDS("InstantSeconds", ChronoUnit.SECONDS, ChronoUnit.FOREVER, ValueRange.of(Long.MIN_VALUE, Long.MAX_VALUE)),
    OFFSET_SECONDS("OffsetSeconds", ChronoUnit.SECONDS, ChronoUnit.FOREVER, ValueRange.of(-64800, 64800));
    
    private final TemporalUnit baseUnit;
    private final String displayNameKey;
    private final String name;
    private final ValueRange range;
    private final TemporalUnit rangeUnit;

    private ChronoField(String name, TemporalUnit baseUnit, TemporalUnit rangeUnit, ValueRange range) {
        this.name = name;
        this.baseUnit = baseUnit;
        this.rangeUnit = rangeUnit;
        this.range = range;
        this.displayNameKey = null;
    }

    private ChronoField(String name, TemporalUnit baseUnit, TemporalUnit rangeUnit, ValueRange range, String displayNameKey) {
        this.name = name;
        this.baseUnit = baseUnit;
        this.rangeUnit = rangeUnit;
        this.range = range;
        this.displayNameKey = displayNameKey;
    }

    public String getDisplayName(Locale locale) {
        Objects.requireNonNull((Object) locale, "locale");
        if (this.displayNameKey == null) {
            return this.name;
        }
        String icuName = DateTimePatternGenerator.getFrozenInstance(ULocale.forLocale(locale)).getAppendItemName(getIcuFieldNumber(this));
        if (icuName == null || (icuName.isEmpty() ^ 1) == 0) {
            icuName = this.name;
        }
        return icuName;
    }

    private static int getIcuFieldNumber(ChronoField field) {
        switch (-getjava-time-temporal-ChronoFieldSwitchesValues()[field.ordinal()]) {
            case 1:
                return 10;
            case 2:
                return 7;
            case 3:
                return 6;
            case 4:
                return 0;
            case 5:
                return 11;
            case 6:
                return 12;
            case 7:
                return 3;
            case 8:
                return 13;
            case 9:
                return 1;
            default:
                throw new IllegalArgumentException("Unexpected ChronoField " + field.name());
        }
    }

    public TemporalUnit getBaseUnit() {
        return this.baseUnit;
    }

    public TemporalUnit getRangeUnit() {
        return this.rangeUnit;
    }

    public ValueRange range() {
        return this.range;
    }

    public boolean isDateBased() {
        return ordinal() >= DAY_OF_WEEK.ordinal() && ordinal() <= ERA.ordinal();
    }

    public boolean isTimeBased() {
        return ordinal() < DAY_OF_WEEK.ordinal();
    }

    public long checkValidValue(long value) {
        return range().checkValidValue(value, this);
    }

    public int checkValidIntValue(long value) {
        return range().checkValidIntValue(value, this);
    }

    public boolean isSupportedBy(TemporalAccessor temporal) {
        return temporal.isSupported(this);
    }

    public ValueRange rangeRefinedBy(TemporalAccessor temporal) {
        return temporal.range(this);
    }

    public long getFrom(TemporalAccessor temporal) {
        return temporal.getLong(this);
    }

    public <R extends Temporal> R adjustInto(R temporal, long newValue) {
        return temporal.with(this, newValue);
    }

    public String toString() {
        return this.name;
    }
}
