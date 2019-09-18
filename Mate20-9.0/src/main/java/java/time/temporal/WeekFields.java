package java.time.temporal;

import android.icu.text.DateTimePatternGenerator;
import android.icu.util.Calendar;
import android.icu.util.ULocale;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.chrono.ChronoLocalDate;
import java.time.chrono.Chronology;
import java.time.format.ResolverStyle;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class WeekFields implements Serializable {
    private static final ConcurrentMap<String, WeekFields> CACHE = new ConcurrentHashMap(4, 0.75f, 2);
    public static final WeekFields ISO = new WeekFields(DayOfWeek.MONDAY, 4);
    public static final WeekFields SUNDAY_START = of(DayOfWeek.SUNDAY, 1);
    public static final TemporalUnit WEEK_BASED_YEARS = IsoFields.WEEK_BASED_YEARS;
    private static final long serialVersionUID = -1177360819670808121L;
    /* access modifiers changed from: private */
    public final transient TemporalField dayOfWeek = ComputedDayOfField.ofDayOfWeekField(this);
    private final DayOfWeek firstDayOfWeek;
    private final int minimalDays;
    /* access modifiers changed from: private */
    public final transient TemporalField weekBasedYear = ComputedDayOfField.ofWeekBasedYearField(this);
    private final transient TemporalField weekOfMonth = ComputedDayOfField.ofWeekOfMonthField(this);
    /* access modifiers changed from: private */
    public final transient TemporalField weekOfWeekBasedYear = ComputedDayOfField.ofWeekOfWeekBasedYearField(this);
    private final transient TemporalField weekOfYear = ComputedDayOfField.ofWeekOfYearField(this);

    static class ComputedDayOfField implements TemporalField {
        private static final ValueRange DAY_OF_WEEK_RANGE = ValueRange.of(1, 7);
        private static final ValueRange WEEK_OF_MONTH_RANGE = ValueRange.of(0, 1, 4, 6);
        private static final ValueRange WEEK_OF_WEEK_BASED_YEAR_RANGE = ValueRange.of(1, 52, 53);
        private static final ValueRange WEEK_OF_YEAR_RANGE = ValueRange.of(0, 1, 52, 54);
        private final TemporalUnit baseUnit;
        private final String name;
        private final ValueRange range;
        private final TemporalUnit rangeUnit;
        private final WeekFields weekDef;

        static ComputedDayOfField ofDayOfWeekField(WeekFields weekDef2) {
            ComputedDayOfField computedDayOfField = new ComputedDayOfField("DayOfWeek", weekDef2, ChronoUnit.DAYS, ChronoUnit.WEEKS, DAY_OF_WEEK_RANGE);
            return computedDayOfField;
        }

        static ComputedDayOfField ofWeekOfMonthField(WeekFields weekDef2) {
            ComputedDayOfField computedDayOfField = new ComputedDayOfField("WeekOfMonth", weekDef2, ChronoUnit.WEEKS, ChronoUnit.MONTHS, WEEK_OF_MONTH_RANGE);
            return computedDayOfField;
        }

        static ComputedDayOfField ofWeekOfYearField(WeekFields weekDef2) {
            ComputedDayOfField computedDayOfField = new ComputedDayOfField("WeekOfYear", weekDef2, ChronoUnit.WEEKS, ChronoUnit.YEARS, WEEK_OF_YEAR_RANGE);
            return computedDayOfField;
        }

        static ComputedDayOfField ofWeekOfWeekBasedYearField(WeekFields weekDef2) {
            ComputedDayOfField computedDayOfField = new ComputedDayOfField("WeekOfWeekBasedYear", weekDef2, ChronoUnit.WEEKS, IsoFields.WEEK_BASED_YEARS, WEEK_OF_WEEK_BASED_YEAR_RANGE);
            return computedDayOfField;
        }

        static ComputedDayOfField ofWeekBasedYearField(WeekFields weekDef2) {
            ComputedDayOfField computedDayOfField = new ComputedDayOfField("WeekBasedYear", weekDef2, IsoFields.WEEK_BASED_YEARS, ChronoUnit.FOREVER, ChronoField.YEAR.range());
            return computedDayOfField;
        }

        private ChronoLocalDate ofWeekBasedYear(Chronology chrono, int yowby, int wowby, int dow) {
            ChronoLocalDate date = chrono.date(yowby, 1, 1);
            int offset = startOfWeekOffset(1, localizedDayOfWeek((TemporalAccessor) date));
            return date.plus((long) ((-offset) + (dow - 1) + ((Math.min(wowby, computeWeek(offset, this.weekDef.getMinimalDaysInFirstWeek() + date.lengthOfYear()) - 1) - 1) * 7)), (TemporalUnit) ChronoUnit.DAYS);
        }

        private ComputedDayOfField(String name2, WeekFields weekDef2, TemporalUnit baseUnit2, TemporalUnit rangeUnit2, ValueRange range2) {
            this.name = name2;
            this.weekDef = weekDef2;
            this.baseUnit = baseUnit2;
            this.rangeUnit = rangeUnit2;
            this.range = range2;
        }

        public long getFrom(TemporalAccessor temporal) {
            if (this.rangeUnit == ChronoUnit.WEEKS) {
                return (long) localizedDayOfWeek(temporal);
            }
            if (this.rangeUnit == ChronoUnit.MONTHS) {
                return localizedWeekOfMonth(temporal);
            }
            if (this.rangeUnit == ChronoUnit.YEARS) {
                return localizedWeekOfYear(temporal);
            }
            if (this.rangeUnit == WeekFields.WEEK_BASED_YEARS) {
                return (long) localizedWeekOfWeekBasedYear(temporal);
            }
            if (this.rangeUnit == ChronoUnit.FOREVER) {
                return (long) localizedWeekBasedYear(temporal);
            }
            throw new IllegalStateException("unreachable, rangeUnit: " + this.rangeUnit + ", this: " + this);
        }

        private int localizedDayOfWeek(TemporalAccessor temporal) {
            return Math.floorMod(temporal.get(ChronoField.DAY_OF_WEEK) - this.weekDef.getFirstDayOfWeek().getValue(), 7) + 1;
        }

        private int localizedDayOfWeek(int isoDow) {
            return Math.floorMod(isoDow - this.weekDef.getFirstDayOfWeek().getValue(), 7) + 1;
        }

        private long localizedWeekOfMonth(TemporalAccessor temporal) {
            int dow = localizedDayOfWeek(temporal);
            int dom = temporal.get(ChronoField.DAY_OF_MONTH);
            return (long) computeWeek(startOfWeekOffset(dom, dow), dom);
        }

        private long localizedWeekOfYear(TemporalAccessor temporal) {
            int dow = localizedDayOfWeek(temporal);
            int doy = temporal.get(ChronoField.DAY_OF_YEAR);
            return (long) computeWeek(startOfWeekOffset(doy, dow), doy);
        }

        private int localizedWeekBasedYear(TemporalAccessor temporal) {
            int dow = localizedDayOfWeek(temporal);
            int year = temporal.get(ChronoField.YEAR);
            int doy = temporal.get(ChronoField.DAY_OF_YEAR);
            int offset = startOfWeekOffset(doy, dow);
            int week = computeWeek(offset, doy);
            if (week == 0) {
                return year - 1;
            }
            if (week >= computeWeek(offset, this.weekDef.getMinimalDaysInFirstWeek() + ((int) temporal.range(ChronoField.DAY_OF_YEAR).getMaximum()))) {
                return year + 1;
            }
            return year;
        }

        private int localizedWeekOfWeekBasedYear(TemporalAccessor temporal) {
            int dow = localizedDayOfWeek(temporal);
            int doy = temporal.get(ChronoField.DAY_OF_YEAR);
            int offset = startOfWeekOffset(doy, dow);
            int week = computeWeek(offset, doy);
            if (week == 0) {
                return localizedWeekOfWeekBasedYear(Chronology.from(temporal).date(temporal).minus((long) doy, (TemporalUnit) ChronoUnit.DAYS));
            }
            if (week > 50) {
                int newYearWeek = computeWeek(offset, this.weekDef.getMinimalDaysInFirstWeek() + ((int) temporal.range(ChronoField.DAY_OF_YEAR).getMaximum()));
                if (week >= newYearWeek) {
                    week = (week - newYearWeek) + 1;
                }
            }
            return week;
        }

        private int startOfWeekOffset(int day, int dow) {
            int weekStart = Math.floorMod(day - dow, 7);
            int offset = -weekStart;
            if (weekStart + 1 > this.weekDef.getMinimalDaysInFirstWeek()) {
                return 7 - weekStart;
            }
            return offset;
        }

        private int computeWeek(int offset, int day) {
            return ((7 + offset) + (day - 1)) / 7;
        }

        public <R extends Temporal> R adjustInto(R temporal, long newValue) {
            int newVal = this.range.checkValidIntValue(newValue, this);
            int currentVal = temporal.get(this);
            if (newVal == currentVal) {
                return temporal;
            }
            if (this.rangeUnit != ChronoUnit.FOREVER) {
                return temporal.plus((long) (newVal - currentVal), this.baseUnit);
            }
            int idow = temporal.get(this.weekDef.dayOfWeek);
            return ofWeekBasedYear(Chronology.from(temporal), (int) newValue, temporal.get(this.weekDef.weekOfWeekBasedYear), idow);
        }

        public ChronoLocalDate resolve(Map<TemporalField, Long> fieldValues, TemporalAccessor partialTemporal, ResolverStyle resolverStyle) {
            int dow;
            Chronology chrono;
            Map<TemporalField, Long> map = fieldValues;
            long value = map.get(this).longValue();
            int newValue = Math.toIntExact(value);
            if (this.rangeUnit == ChronoUnit.WEEKS) {
                int checkedValue = this.range.checkValidIntValue(value, this);
                map.remove(this);
                map.put(ChronoField.DAY_OF_WEEK, Long.valueOf((long) (Math.floorMod((this.weekDef.getFirstDayOfWeek().getValue() - 1) + (checkedValue - 1), 7) + 1)));
                return null;
            } else if (!map.containsKey(ChronoField.DAY_OF_WEEK)) {
                return null;
            } else {
                int dow2 = localizedDayOfWeek(ChronoField.DAY_OF_WEEK.checkValidIntValue(map.get(ChronoField.DAY_OF_WEEK).longValue()));
                Chronology chrono2 = Chronology.from(partialTemporal);
                if (map.containsKey(ChronoField.YEAR)) {
                    int year = ChronoField.YEAR.checkValidIntValue(map.get(ChronoField.YEAR).longValue());
                    if (this.rangeUnit != ChronoUnit.MONTHS || !map.containsKey(ChronoField.MONTH_OF_YEAR)) {
                        chrono = chrono2;
                        dow = dow2;
                        if (this.rangeUnit == ChronoUnit.YEARS) {
                            return resolveWoY(map, chrono, year, (long) newValue, dow, resolverStyle);
                        }
                    } else {
                        Chronology chronology = chrono2;
                        int i = dow2;
                        return resolveWoM(map, chrono2, year, map.get(ChronoField.MONTH_OF_YEAR).longValue(), (long) newValue, dow2, resolverStyle);
                    }
                } else {
                    chrono = chrono2;
                    dow = dow2;
                    if (this.rangeUnit == WeekFields.WEEK_BASED_YEARS || this.rangeUnit == ChronoUnit.FOREVER) {
                        if (!map.containsKey(this.weekDef.weekBasedYear) || !map.containsKey(this.weekDef.weekOfWeekBasedYear)) {
                            ResolverStyle resolverStyle2 = resolverStyle;
                            Chronology chronology2 = chrono;
                            int i2 = dow;
                            return null;
                        }
                        return resolveWBY(map, chrono, dow, resolverStyle);
                    }
                }
                ResolverStyle resolverStyle3 = resolverStyle;
                return null;
            }
        }

        private ChronoLocalDate resolveWoM(Map<TemporalField, Long> fieldValues, Chronology chrono, int year, long month, long wom, int localDow, ResolverStyle resolverStyle) {
            ChronoLocalDate date;
            Map<TemporalField, Long> map = fieldValues;
            Chronology chronology = chrono;
            int i = year;
            long j = month;
            long j2 = wom;
            ResolverStyle resolverStyle2 = resolverStyle;
            if (resolverStyle2 == ResolverStyle.LENIENT) {
                ChronoLocalDate date2 = chronology.date(i, 1, 1).plus(Math.subtractExact(j, 1), (TemporalUnit) ChronoUnit.MONTHS);
                long weeks = Math.subtractExact(j2, localizedWeekOfMonth(date2));
                long j3 = weeks;
                date = date2.plus(Math.addExact(Math.multiplyExact(weeks, 7), (long) (localDow - localizedDayOfWeek((TemporalAccessor) date2))), (TemporalUnit) ChronoUnit.DAYS);
            } else {
                ChronoLocalDate date3 = chronology.date(i, ChronoField.MONTH_OF_YEAR.checkValidIntValue(j), 1);
                ChronoLocalDate date4 = date3.plus((long) ((((int) (((long) this.range.checkValidIntValue(j2, this)) - localizedWeekOfMonth(date3))) * 7) + (localDow - localizedDayOfWeek((TemporalAccessor) date3))), (TemporalUnit) ChronoUnit.DAYS);
                if (resolverStyle2 != ResolverStyle.STRICT || date4.getLong(ChronoField.MONTH_OF_YEAR) == j) {
                    date = date4;
                } else {
                    throw new DateTimeException("Strict mode rejected resolved date as it is in a different month");
                }
            }
            map.remove(this);
            map.remove(ChronoField.YEAR);
            map.remove(ChronoField.MONTH_OF_YEAR);
            map.remove(ChronoField.DAY_OF_WEEK);
            return date;
        }

        private ChronoLocalDate resolveWoY(Map<TemporalField, Long> fieldValues, Chronology chrono, int year, long woy, int localDow, ResolverStyle resolverStyle) {
            ChronoLocalDate date;
            ChronoLocalDate date2 = chrono.date(year, 1, 1);
            if (resolverStyle == ResolverStyle.LENIENT) {
                date = date2.plus(Math.addExact(Math.multiplyExact(Math.subtractExact(woy, localizedWeekOfYear(date2)), 7), (long) (localDow - localizedDayOfWeek((TemporalAccessor) date2))), (TemporalUnit) ChronoUnit.DAYS);
            } else {
                date = date2.plus((long) ((((int) (((long) this.range.checkValidIntValue(woy, this)) - localizedWeekOfYear(date2))) * 7) + (localDow - localizedDayOfWeek((TemporalAccessor) date2))), (TemporalUnit) ChronoUnit.DAYS);
                if (resolverStyle == ResolverStyle.STRICT && date.getLong(ChronoField.YEAR) != ((long) year)) {
                    throw new DateTimeException("Strict mode rejected resolved date as it is in a different year");
                }
            }
            fieldValues.remove(this);
            fieldValues.remove(ChronoField.YEAR);
            fieldValues.remove(ChronoField.DAY_OF_WEEK);
            return date;
        }

        private ChronoLocalDate resolveWBY(Map<TemporalField, Long> fieldValues, Chronology chrono, int localDow, ResolverStyle resolverStyle) {
            ChronoLocalDate date;
            int yowby = this.weekDef.weekBasedYear.range().checkValidIntValue(fieldValues.get(this.weekDef.weekBasedYear).longValue(), this.weekDef.weekBasedYear);
            if (resolverStyle == ResolverStyle.LENIENT) {
                date = ofWeekBasedYear(chrono, yowby, 1, localDow).plus(Math.subtractExact(fieldValues.get(this.weekDef.weekOfWeekBasedYear).longValue(), 1), (TemporalUnit) ChronoUnit.WEEKS);
            } else {
                ChronoLocalDate date2 = ofWeekBasedYear(chrono, yowby, this.weekDef.weekOfWeekBasedYear.range().checkValidIntValue(fieldValues.get(this.weekDef.weekOfWeekBasedYear).longValue(), this.weekDef.weekOfWeekBasedYear), localDow);
                if (resolverStyle != ResolverStyle.STRICT || localizedWeekBasedYear(date2) == yowby) {
                    date = date2;
                } else {
                    throw new DateTimeException("Strict mode rejected resolved date as it is in a different week-based-year");
                }
            }
            fieldValues.remove(this);
            fieldValues.remove(this.weekDef.weekBasedYear);
            fieldValues.remove(this.weekDef.weekOfWeekBasedYear);
            fieldValues.remove(ChronoField.DAY_OF_WEEK);
            return date;
        }

        public String getDisplayName(Locale locale) {
            Objects.requireNonNull(locale, "locale");
            if (this.rangeUnit != ChronoUnit.YEARS) {
                return this.name;
            }
            String icuName = DateTimePatternGenerator.getFrozenInstance(ULocale.forLocale(locale)).getAppendItemName(4);
            return (icuName == null || icuName.isEmpty()) ? this.name : icuName;
        }

        public TemporalUnit getBaseUnit() {
            return this.baseUnit;
        }

        public TemporalUnit getRangeUnit() {
            return this.rangeUnit;
        }

        public boolean isDateBased() {
            return true;
        }

        public boolean isTimeBased() {
            return false;
        }

        public ValueRange range() {
            return this.range;
        }

        public boolean isSupportedBy(TemporalAccessor temporal) {
            if (temporal.isSupported(ChronoField.DAY_OF_WEEK)) {
                if (this.rangeUnit == ChronoUnit.WEEKS) {
                    return true;
                }
                if (this.rangeUnit == ChronoUnit.MONTHS) {
                    return temporal.isSupported(ChronoField.DAY_OF_MONTH);
                }
                if (this.rangeUnit == ChronoUnit.YEARS) {
                    return temporal.isSupported(ChronoField.DAY_OF_YEAR);
                }
                if (this.rangeUnit == WeekFields.WEEK_BASED_YEARS) {
                    return temporal.isSupported(ChronoField.DAY_OF_YEAR);
                }
                if (this.rangeUnit == ChronoUnit.FOREVER) {
                    return temporal.isSupported(ChronoField.YEAR);
                }
            }
            return false;
        }

        public ValueRange rangeRefinedBy(TemporalAccessor temporal) {
            if (this.rangeUnit == ChronoUnit.WEEKS) {
                return this.range;
            }
            if (this.rangeUnit == ChronoUnit.MONTHS) {
                return rangeByWeek(temporal, ChronoField.DAY_OF_MONTH);
            }
            if (this.rangeUnit == ChronoUnit.YEARS) {
                return rangeByWeek(temporal, ChronoField.DAY_OF_YEAR);
            }
            if (this.rangeUnit == WeekFields.WEEK_BASED_YEARS) {
                return rangeWeekOfWeekBasedYear(temporal);
            }
            if (this.rangeUnit == ChronoUnit.FOREVER) {
                return ChronoField.YEAR.range();
            }
            throw new IllegalStateException("unreachable, rangeUnit: " + this.rangeUnit + ", this: " + this);
        }

        private ValueRange rangeByWeek(TemporalAccessor temporal, TemporalField field) {
            int offset = startOfWeekOffset(temporal.get(field), localizedDayOfWeek(temporal));
            ValueRange fieldRange = temporal.range(field);
            return ValueRange.of((long) computeWeek(offset, (int) fieldRange.getMinimum()), (long) computeWeek(offset, (int) fieldRange.getMaximum()));
        }

        private ValueRange rangeWeekOfWeekBasedYear(TemporalAccessor temporal) {
            if (!temporal.isSupported(ChronoField.DAY_OF_YEAR)) {
                return WEEK_OF_YEAR_RANGE;
            }
            int dow = localizedDayOfWeek(temporal);
            int doy = temporal.get(ChronoField.DAY_OF_YEAR);
            int offset = startOfWeekOffset(doy, dow);
            int week = computeWeek(offset, doy);
            if (week == 0) {
                return rangeWeekOfWeekBasedYear(Chronology.from(temporal).date(temporal).minus((long) (doy + 7), (TemporalUnit) ChronoUnit.DAYS));
            }
            int yearLen = (int) temporal.range(ChronoField.DAY_OF_YEAR).getMaximum();
            int newYearWeek = computeWeek(offset, this.weekDef.getMinimalDaysInFirstWeek() + yearLen);
            if (week >= newYearWeek) {
                return rangeWeekOfWeekBasedYear(Chronology.from(temporal).date(temporal).plus((long) ((yearLen - doy) + 1 + 7), (TemporalUnit) ChronoUnit.DAYS));
            }
            return ValueRange.of(1, (long) (newYearWeek - 1));
        }

        public String toString() {
            return this.name + "[" + this.weekDef.toString() + "]";
        }
    }

    public static WeekFields of(Locale locale) {
        Objects.requireNonNull(locale, "locale");
        Calendar.WeekData weekData = Calendar.getWeekDataForRegion(ULocale.getRegionForSupplementalData(ULocale.forLocale(locale), true));
        return of(DayOfWeek.SUNDAY.plus((long) (weekData.firstDayOfWeek - 1)), weekData.minimalDaysInFirstWeek);
    }

    public static WeekFields of(DayOfWeek firstDayOfWeek2, int minimalDaysInFirstWeek) {
        String key = firstDayOfWeek2.toString() + minimalDaysInFirstWeek;
        WeekFields rules = CACHE.get(key);
        if (rules != null) {
            return rules;
        }
        CACHE.putIfAbsent(key, new WeekFields(firstDayOfWeek2, minimalDaysInFirstWeek));
        return CACHE.get(key);
    }

    private WeekFields(DayOfWeek firstDayOfWeek2, int minimalDaysInFirstWeek) {
        Objects.requireNonNull(firstDayOfWeek2, "firstDayOfWeek");
        if (minimalDaysInFirstWeek < 1 || minimalDaysInFirstWeek > 7) {
            throw new IllegalArgumentException("Minimal number of days is invalid");
        }
        this.firstDayOfWeek = firstDayOfWeek2;
        this.minimalDays = minimalDaysInFirstWeek;
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException, InvalidObjectException {
        s.defaultReadObject();
        if (this.firstDayOfWeek == null) {
            throw new InvalidObjectException("firstDayOfWeek is null");
        } else if (this.minimalDays < 1 || this.minimalDays > 7) {
            throw new InvalidObjectException("Minimal number of days is invalid");
        }
    }

    private Object readResolve() throws InvalidObjectException {
        try {
            return of(this.firstDayOfWeek, this.minimalDays);
        } catch (IllegalArgumentException iae) {
            throw new InvalidObjectException("Invalid serialized WeekFields: " + iae.getMessage());
        }
    }

    public DayOfWeek getFirstDayOfWeek() {
        return this.firstDayOfWeek;
    }

    public int getMinimalDaysInFirstWeek() {
        return this.minimalDays;
    }

    public TemporalField dayOfWeek() {
        return this.dayOfWeek;
    }

    public TemporalField weekOfMonth() {
        return this.weekOfMonth;
    }

    public TemporalField weekOfYear() {
        return this.weekOfYear;
    }

    public TemporalField weekOfWeekBasedYear() {
        return this.weekOfWeekBasedYear;
    }

    public TemporalField weekBasedYear() {
        return this.weekBasedYear;
    }

    public boolean equals(Object object) {
        boolean z = true;
        if (this == object) {
            return true;
        }
        if (!(object instanceof WeekFields)) {
            return false;
        }
        if (hashCode() != object.hashCode()) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        return (this.firstDayOfWeek.ordinal() * 7) + this.minimalDays;
    }

    public String toString() {
        return "WeekFields[" + this.firstDayOfWeek + ',' + this.minimalDays + ']';
    }
}
