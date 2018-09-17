package java.time.temporal;

import android.icu.text.DateTimePatternGenerator;
import android.icu.util.ULocale;
import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.chrono.ChronoLocalDate;
import java.time.chrono.Chronology;
import java.time.chrono.IsoChronology;
import java.time.format.ResolverStyle;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public final class IsoFields {
    public static final TemporalField DAY_OF_QUARTER = Field.DAY_OF_QUARTER;
    public static final TemporalField QUARTER_OF_YEAR = Field.QUARTER_OF_YEAR;
    public static final TemporalUnit QUARTER_YEARS = Unit.QUARTER_YEARS;
    public static final TemporalField WEEK_BASED_YEAR = Field.WEEK_BASED_YEAR;
    public static final TemporalUnit WEEK_BASED_YEARS = Unit.WEEK_BASED_YEARS;
    public static final TemporalField WEEK_OF_WEEK_BASED_YEAR = Field.WEEK_OF_WEEK_BASED_YEAR;

    private enum Field implements TemporalField {
        DAY_OF_QUARTER {
            public TemporalUnit getBaseUnit() {
                return ChronoUnit.DAYS;
            }

            public TemporalUnit getRangeUnit() {
                return IsoFields.QUARTER_YEARS;
            }

            public ValueRange range() {
                return ValueRange.of(1, 90, 92);
            }

            public boolean isSupportedBy(TemporalAccessor temporal) {
                if (temporal.isSupported(ChronoField.DAY_OF_YEAR) && temporal.isSupported(ChronoField.MONTH_OF_YEAR) && temporal.isSupported(ChronoField.YEAR)) {
                    return Field.isIso(temporal);
                }
                return false;
            }

            public ValueRange rangeRefinedBy(TemporalAccessor temporal) {
                if (isSupportedBy(temporal)) {
                    long qoy = temporal.getLong(QUARTER_OF_YEAR);
                    if (qoy == 1) {
                        return IsoChronology.INSTANCE.isLeapYear(temporal.getLong(ChronoField.YEAR)) ? ValueRange.of(1, 91) : ValueRange.of(1, 90);
                    } else if (qoy == 2) {
                        return ValueRange.of(1, 91);
                    } else {
                        if (qoy == 3 || qoy == 4) {
                            return ValueRange.of(1, 92);
                        }
                        return range();
                    }
                }
                throw new UnsupportedTemporalTypeException("Unsupported field: DayOfQuarter");
            }

            public long getFrom(TemporalAccessor temporal) {
                if (isSupportedBy(temporal)) {
                    int doy = temporal.get(ChronoField.DAY_OF_YEAR);
                    int moy = temporal.get(ChronoField.MONTH_OF_YEAR);
                    return (long) (doy - Field.QUARTER_DAYS[(IsoChronology.INSTANCE.isLeapYear(temporal.getLong(ChronoField.YEAR)) ? 4 : 0) + ((moy - 1) / 3)]);
                }
                throw new UnsupportedTemporalTypeException("Unsupported field: DayOfQuarter");
            }

            public <R extends Temporal> R adjustInto(R temporal, long newValue) {
                long curValue = getFrom(temporal);
                range().checkValidValue(newValue, this);
                return temporal.with(ChronoField.DAY_OF_YEAR, temporal.getLong(ChronoField.DAY_OF_YEAR) + (newValue - curValue));
            }

            public ChronoLocalDate resolve(Map<TemporalField, Long> fieldValues, TemporalAccessor partialTemporal, ResolverStyle resolverStyle) {
                Long yearLong = (Long) fieldValues.get(ChronoField.YEAR);
                Long qoyLong = (Long) fieldValues.get(QUARTER_OF_YEAR);
                if (yearLong == null || qoyLong == null) {
                    return null;
                }
                LocalDate date;
                int y = ChronoField.YEAR.checkValidIntValue(yearLong.longValue());
                long doq = ((Long) fieldValues.get(DAY_OF_QUARTER)).longValue();
                Field.ensureIso(partialTemporal);
                if (resolverStyle == ResolverStyle.LENIENT) {
                    date = LocalDate.of(y, 1, 1).plusMonths(Math.multiplyExact(Math.subtractExact(qoyLong.longValue(), 1), 3));
                    doq = Math.subtractExact(doq, 1);
                } else {
                    date = LocalDate.of(y, ((QUARTER_OF_YEAR.range().checkValidIntValue(qoyLong.longValue(), QUARTER_OF_YEAR) - 1) * 3) + 1, 1);
                    if (doq < 1 || doq > 90) {
                        if (resolverStyle == ResolverStyle.STRICT) {
                            rangeRefinedBy(date).checkValidValue(doq, this);
                        } else {
                            range().checkValidValue(doq, this);
                        }
                    }
                    doq--;
                }
                fieldValues.remove(this);
                fieldValues.remove(ChronoField.YEAR);
                fieldValues.remove(QUARTER_OF_YEAR);
                return date.plusDays(doq);
            }

            public String toString() {
                return "DayOfQuarter";
            }
        },
        QUARTER_OF_YEAR {
            public TemporalUnit getBaseUnit() {
                return IsoFields.QUARTER_YEARS;
            }

            public TemporalUnit getRangeUnit() {
                return ChronoUnit.YEARS;
            }

            public ValueRange range() {
                return ValueRange.of(1, 4);
            }

            public boolean isSupportedBy(TemporalAccessor temporal) {
                return temporal.isSupported(ChronoField.MONTH_OF_YEAR) ? Field.isIso(temporal) : false;
            }

            public long getFrom(TemporalAccessor temporal) {
                if (isSupportedBy(temporal)) {
                    return (2 + temporal.getLong(ChronoField.MONTH_OF_YEAR)) / 3;
                }
                throw new UnsupportedTemporalTypeException("Unsupported field: QuarterOfYear");
            }

            public <R extends Temporal> R adjustInto(R temporal, long newValue) {
                long curValue = getFrom(temporal);
                range().checkValidValue(newValue, this);
                return temporal.with(ChronoField.MONTH_OF_YEAR, temporal.getLong(ChronoField.MONTH_OF_YEAR) + ((newValue - curValue) * 3));
            }

            public String toString() {
                return "QuarterOfYear";
            }
        },
        WEEK_OF_WEEK_BASED_YEAR {
            public String getDisplayName(Locale locale) {
                Objects.requireNonNull((Object) locale, "locale");
                String icuName = DateTimePatternGenerator.getFrozenInstance(ULocale.forLocale(locale)).getAppendItemName(4);
                return (icuName == null || (icuName.isEmpty() ^ 1) == 0) ? toString() : icuName;
            }

            public TemporalUnit getBaseUnit() {
                return ChronoUnit.WEEKS;
            }

            public TemporalUnit getRangeUnit() {
                return IsoFields.WEEK_BASED_YEARS;
            }

            public ValueRange range() {
                return ValueRange.of(1, 52, 53);
            }

            public boolean isSupportedBy(TemporalAccessor temporal) {
                return temporal.isSupported(ChronoField.EPOCH_DAY) ? Field.isIso(temporal) : false;
            }

            public ValueRange rangeRefinedBy(TemporalAccessor temporal) {
                if (isSupportedBy(temporal)) {
                    return Field.getWeekRange(LocalDate.from(temporal));
                }
                throw new UnsupportedTemporalTypeException("Unsupported field: WeekOfWeekBasedYear");
            }

            public long getFrom(TemporalAccessor temporal) {
                if (isSupportedBy(temporal)) {
                    return (long) Field.getWeek(LocalDate.from(temporal));
                }
                throw new UnsupportedTemporalTypeException("Unsupported field: WeekOfWeekBasedYear");
            }

            public <R extends Temporal> R adjustInto(R temporal, long newValue) {
                range().checkValidValue(newValue, this);
                return temporal.plus(Math.subtractExact(newValue, getFrom(temporal)), ChronoUnit.WEEKS);
            }

            public ChronoLocalDate resolve(Map<TemporalField, Long> fieldValues, TemporalAccessor partialTemporal, ResolverStyle resolverStyle) {
                Long wbyLong = (Long) fieldValues.get(WEEK_BASED_YEAR);
                Long dowLong = (Long) fieldValues.get(ChronoField.DAY_OF_WEEK);
                if (wbyLong == null || dowLong == null) {
                    return null;
                }
                int wby = WEEK_BASED_YEAR.range().checkValidIntValue(wbyLong.longValue(), WEEK_BASED_YEAR);
                long wowby = ((Long) fieldValues.get(WEEK_OF_WEEK_BASED_YEAR)).longValue();
                Field.ensureIso(partialTemporal);
                LocalDate date = LocalDate.of(wby, 1, 4);
                if (resolverStyle == ResolverStyle.LENIENT) {
                    long dow = dowLong.longValue();
                    if (dow > 7) {
                        date = date.plusWeeks((dow - 1) / 7);
                        dow = ((dow - 1) % 7) + 1;
                    } else if (dow < 1) {
                        date = date.plusWeeks(Math.subtractExact(dow, 7) / 7);
                        dow = ((6 + dow) % 7) + 1;
                    }
                    date = date.plusWeeks(Math.subtractExact(wowby, 1)).with(ChronoField.DAY_OF_WEEK, dow);
                } else {
                    int dow2 = ChronoField.DAY_OF_WEEK.checkValidIntValue(dowLong.longValue());
                    if (wowby < 1 || wowby > 52) {
                        if (resolverStyle == ResolverStyle.STRICT) {
                            Field.getWeekRange(date).checkValidValue(wowby, this);
                        } else {
                            range().checkValidValue(wowby, this);
                        }
                    }
                    date = date.plusWeeks(wowby - 1).with(ChronoField.DAY_OF_WEEK, (long) dow2);
                }
                fieldValues.remove(this);
                fieldValues.remove(WEEK_BASED_YEAR);
                fieldValues.remove(ChronoField.DAY_OF_WEEK);
                return date;
            }

            public String toString() {
                return "WeekOfWeekBasedYear";
            }
        },
        WEEK_BASED_YEAR {
            public TemporalUnit getBaseUnit() {
                return IsoFields.WEEK_BASED_YEARS;
            }

            public TemporalUnit getRangeUnit() {
                return ChronoUnit.FOREVER;
            }

            public ValueRange range() {
                return ChronoField.YEAR.range();
            }

            public boolean isSupportedBy(TemporalAccessor temporal) {
                return temporal.isSupported(ChronoField.EPOCH_DAY) ? Field.isIso(temporal) : false;
            }

            public long getFrom(TemporalAccessor temporal) {
                if (isSupportedBy(temporal)) {
                    return (long) Field.getWeekBasedYear(LocalDate.from(temporal));
                }
                throw new UnsupportedTemporalTypeException("Unsupported field: WeekBasedYear");
            }

            public <R extends Temporal> R adjustInto(R temporal, long newValue) {
                if (isSupportedBy(temporal)) {
                    int newWby = range().checkValidIntValue(newValue, WEEK_BASED_YEAR);
                    LocalDate date = LocalDate.from(temporal);
                    int dow = date.get(ChronoField.DAY_OF_WEEK);
                    int week = Field.getWeek(date);
                    if (week == 53 && Field.getWeekRange(newWby) == 52) {
                        week = 52;
                    }
                    LocalDate resolved = LocalDate.of(newWby, 1, 4);
                    return temporal.with(resolved.plusDays((long) ((dow - resolved.get(ChronoField.DAY_OF_WEEK)) + ((week - 1) * 7))));
                }
                throw new UnsupportedTemporalTypeException("Unsupported field: WeekBasedYear");
            }

            public String toString() {
                return "WeekBasedYear";
            }
        };
        
        private static final int[] QUARTER_DAYS = null;

        static {
            QUARTER_DAYS = new int[]{0, 90, 181, 273, 0, 91, 182, 274};
        }

        public boolean isDateBased() {
            return true;
        }

        public boolean isTimeBased() {
            return false;
        }

        public ValueRange rangeRefinedBy(TemporalAccessor temporal) {
            return range();
        }

        private static boolean isIso(TemporalAccessor temporal) {
            return Chronology.from(temporal).equals(IsoChronology.INSTANCE);
        }

        private static void ensureIso(TemporalAccessor temporal) {
            if (!isIso(temporal)) {
                throw new DateTimeException("Resolve requires IsoChronology");
            }
        }

        private static ValueRange getWeekRange(LocalDate date) {
            return ValueRange.of(1, (long) getWeekRange(getWeekBasedYear(date)));
        }

        private static int getWeekRange(int wby) {
            LocalDate date = LocalDate.of(wby, 1, 1);
            if (date.getDayOfWeek() == DayOfWeek.THURSDAY || (date.getDayOfWeek() == DayOfWeek.WEDNESDAY && date.isLeapYear())) {
                return 53;
            }
            return 52;
        }

        private static int getWeek(LocalDate date) {
            int doy0 = date.getDayOfYear() - 1;
            int doyThu0 = doy0 + (3 - date.getDayOfWeek().ordinal());
            int firstMonDoy0 = (doyThu0 - ((doyThu0 / 7) * 7)) - 3;
            if (firstMonDoy0 < -3) {
                firstMonDoy0 += 7;
            }
            if (doy0 < firstMonDoy0) {
                return (int) getWeekRange(date.withDayOfYear(180).minusYears(1)).getMaximum();
            }
            int week = ((doy0 - firstMonDoy0) / 7) + 1;
            if (week == 53) {
                boolean isLeapYear = firstMonDoy0 != -3 ? firstMonDoy0 == -2 ? date.isLeapYear() : false : true;
                if (!isLeapYear) {
                    week = 1;
                }
            }
            return week;
        }

        private static int getWeekBasedYear(LocalDate date) {
            int i = 0;
            int year = date.getYear();
            int doy = date.getDayOfYear();
            if (doy <= 3) {
                if (doy - date.getDayOfWeek().ordinal() < -2) {
                    return year - 1;
                }
                return year;
            } else if (doy < 363) {
                return year;
            } else {
                int dow = date.getDayOfWeek().ordinal();
                int i2 = doy - 363;
                if (date.isLeapYear()) {
                    i = 1;
                }
                if ((i2 - i) - dow >= 0) {
                    return year + 1;
                }
                return year;
            }
        }
    }

    private enum Unit implements TemporalUnit {
        WEEK_BASED_YEARS("WeekBasedYears", Duration.ofSeconds(31556952)),
        QUARTER_YEARS("QuarterYears", Duration.ofSeconds(7889238));
        
        private final Duration duration;
        private final String name;

        private Unit(String name, Duration estimatedDuration) {
            this.name = name;
            this.duration = estimatedDuration;
        }

        public Duration getDuration() {
            return this.duration;
        }

        public boolean isDurationEstimated() {
            return true;
        }

        public boolean isDateBased() {
            return true;
        }

        public boolean isTimeBased() {
            return false;
        }

        public boolean isSupportedBy(Temporal temporal) {
            return temporal.isSupported(ChronoField.EPOCH_DAY);
        }

        public <R extends Temporal> R addTo(R temporal, long amount) {
            switch (-getjava-time-temporal-IsoFields$UnitSwitchesValues()[ordinal()]) {
                case 1:
                    return temporal.plus(amount / 256, ChronoUnit.YEARS).plus((amount % 256) * 3, ChronoUnit.MONTHS);
                case 2:
                    return temporal.with(IsoFields.WEEK_BASED_YEAR, Math.addExact((long) temporal.get(IsoFields.WEEK_BASED_YEAR), amount));
                default:
                    throw new IllegalStateException("Unreachable");
            }
        }

        public long between(Temporal temporal1Inclusive, Temporal temporal2Exclusive) {
            if (temporal1Inclusive.getClass() != temporal2Exclusive.getClass()) {
                return temporal1Inclusive.until(temporal2Exclusive, this);
            }
            switch (-getjava-time-temporal-IsoFields$UnitSwitchesValues()[ordinal()]) {
                case 1:
                    return temporal1Inclusive.until(temporal2Exclusive, ChronoUnit.MONTHS) / 3;
                case 2:
                    return Math.subtractExact(temporal2Exclusive.getLong(IsoFields.WEEK_BASED_YEAR), temporal1Inclusive.getLong(IsoFields.WEEK_BASED_YEAR));
                default:
                    throw new IllegalStateException("Unreachable");
            }
        }

        public String toString() {
            return this.name;
        }
    }

    private IsoFields() {
        throw new AssertionError((Object) "Not instantiable");
    }
}
