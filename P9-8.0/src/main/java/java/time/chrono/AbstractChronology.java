package java.time.chrono;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.TemporalField;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import sun.security.x509.PolicyInformation;
import sun.util.logging.PlatformLogger;

public abstract class AbstractChronology implements Chronology {
    private static final ConcurrentHashMap<String, Chronology> CHRONOS_BY_ID = new ConcurrentHashMap();
    private static final ConcurrentHashMap<String, Chronology> CHRONOS_BY_TYPE = new ConcurrentHashMap();
    static final Comparator<ChronoLocalDate> DATE_ORDER = new -$Lambda$2u9I1kadVYC2Q_h8lznNWkqzo1s();
    static final Comparator<ChronoLocalDateTime<? extends ChronoLocalDate>> DATE_TIME_ORDER = new Object() {
        public final int compare(Object obj, Object obj2) {
            return $m$0(obj, obj2);
        }
    };
    static final Comparator<ChronoZonedDateTime<?>> INSTANT_ORDER = new Object() {
        public final int compare(Object obj, Object obj2) {
            return $m$0(obj, obj2);
        }
    };

    static /* synthetic */ int lambda$-java_time_chrono_AbstractChronology_6277(ChronoLocalDateTime dateTime1, ChronoLocalDateTime dateTime2) {
        int cmp = Long.compare(dateTime1.toLocalDate().toEpochDay(), dateTime2.toLocalDate().toEpochDay());
        if (cmp == 0) {
            return Long.compare(dateTime1.toLocalTime().toNanoOfDay(), dateTime2.toLocalTime().toNanoOfDay());
        }
        return cmp;
    }

    static /* synthetic */ int lambda$-java_time_chrono_AbstractChronology_6799(ChronoZonedDateTime dateTime1, ChronoZonedDateTime dateTime2) {
        int cmp = Long.compare(dateTime1.toEpochSecond(), dateTime2.toEpochSecond());
        if (cmp == 0) {
            return Long.compare((long) dateTime1.toLocalTime().getNano(), (long) dateTime2.toLocalTime().getNano());
        }
        return cmp;
    }

    static Chronology registerChrono(Chronology chrono) {
        return registerChrono(chrono, chrono.getId());
    }

    static Chronology registerChrono(Chronology chrono, String id) {
        Chronology prev = (Chronology) CHRONOS_BY_ID.putIfAbsent(id, chrono);
        if (prev == null) {
            String type = chrono.getCalendarType();
            if (type != null) {
                CHRONOS_BY_TYPE.putIfAbsent(type, chrono);
            }
        }
        return prev;
    }

    private static boolean initCache() {
        if (CHRONOS_BY_ID.get("ISO") != null) {
            return false;
        }
        registerChrono(HijrahChronology.INSTANCE);
        registerChrono(JapaneseChronology.INSTANCE);
        registerChrono(MinguoChronology.INSTANCE);
        registerChrono(ThaiBuddhistChronology.INSTANCE);
        for (AbstractChronology chrono : ServiceLoader.load(AbstractChronology.class, null)) {
            String id = chrono.getId();
            if (id.equals("ISO") || registerChrono(chrono) != null) {
                PlatformLogger.getLogger("java.time.chrono").warning("Ignoring duplicate Chronology, from ServiceLoader configuration " + id);
            }
        }
        registerChrono(IsoChronology.INSTANCE);
        return true;
    }

    static Chronology ofLocale(Locale locale) {
        Objects.requireNonNull((Object) locale, "locale");
        String type = locale.getUnicodeLocaleType("ca");
        if (type == null || "iso".equals(type) || "iso8601".equals(type)) {
            return IsoChronology.INSTANCE;
        }
        Chronology chrono;
        do {
            chrono = (Chronology) CHRONOS_BY_TYPE.get(type);
            if (chrono != null) {
                return chrono;
            }
        } while (initCache());
        for (Chronology chrono2 : ServiceLoader.load(Chronology.class)) {
            if (type.equals(chrono2.getCalendarType())) {
                return chrono2;
            }
        }
        throw new DateTimeException("Unknown calendar system: " + type);
    }

    static Chronology of(String id) {
        Chronology chrono;
        Objects.requireNonNull((Object) id, PolicyInformation.ID);
        do {
            chrono = of0(id);
            if (chrono != null) {
                return chrono;
            }
        } while (initCache());
        for (Chronology chrono2 : ServiceLoader.load(Chronology.class)) {
            if (!id.equals(chrono2.getId())) {
                if (id.equals(chrono2.getCalendarType())) {
                }
            }
            return chrono2;
        }
        throw new DateTimeException("Unknown chronology: " + id);
    }

    private static Chronology of0(String id) {
        Chronology chrono = (Chronology) CHRONOS_BY_ID.get(id);
        if (chrono == null) {
            return (Chronology) CHRONOS_BY_TYPE.get(id);
        }
        return chrono;
    }

    static Set<Chronology> getAvailableChronologies() {
        initCache();
        HashSet<Chronology> chronos = new HashSet(CHRONOS_BY_ID.values());
        for (Chronology chrono : ServiceLoader.load(Chronology.class)) {
            chronos.add(chrono);
        }
        return chronos;
    }

    protected AbstractChronology() {
    }

    public ChronoLocalDate resolveDate(Map<TemporalField, Long> fieldValues, ResolverStyle resolverStyle) {
        if (fieldValues.containsKey(ChronoField.EPOCH_DAY)) {
            return dateEpochDay(((Long) fieldValues.remove(ChronoField.EPOCH_DAY)).longValue());
        }
        resolveProlepticMonth(fieldValues, resolverStyle);
        ChronoLocalDate resolved = resolveYearOfEra(fieldValues, resolverStyle);
        if (resolved != null) {
            return resolved;
        }
        if (fieldValues.containsKey(ChronoField.YEAR)) {
            if (fieldValues.containsKey(ChronoField.MONTH_OF_YEAR)) {
                if (fieldValues.containsKey(ChronoField.DAY_OF_MONTH)) {
                    return resolveYMD(fieldValues, resolverStyle);
                }
                if (fieldValues.containsKey(ChronoField.ALIGNED_WEEK_OF_MONTH)) {
                    if (fieldValues.containsKey(ChronoField.ALIGNED_DAY_OF_WEEK_IN_MONTH)) {
                        return resolveYMAA(fieldValues, resolverStyle);
                    }
                    if (fieldValues.containsKey(ChronoField.DAY_OF_WEEK)) {
                        return resolveYMAD(fieldValues, resolverStyle);
                    }
                }
            }
            if (fieldValues.containsKey(ChronoField.DAY_OF_YEAR)) {
                return resolveYD(fieldValues, resolverStyle);
            }
            if (fieldValues.containsKey(ChronoField.ALIGNED_WEEK_OF_YEAR)) {
                if (fieldValues.containsKey(ChronoField.ALIGNED_DAY_OF_WEEK_IN_YEAR)) {
                    return resolveYAA(fieldValues, resolverStyle);
                }
                if (fieldValues.containsKey(ChronoField.DAY_OF_WEEK)) {
                    return resolveYAD(fieldValues, resolverStyle);
                }
            }
        }
        return null;
    }

    void resolveProlepticMonth(Map<TemporalField, Long> fieldValues, ResolverStyle resolverStyle) {
        Long pMonth = (Long) fieldValues.remove(ChronoField.PROLEPTIC_MONTH);
        if (pMonth != null) {
            if (resolverStyle != ResolverStyle.LENIENT) {
                ChronoField.PROLEPTIC_MONTH.checkValidValue(pMonth.longValue());
            }
            ChronoLocalDate chronoDate = dateNow().with(ChronoField.DAY_OF_MONTH, 1).with(ChronoField.PROLEPTIC_MONTH, pMonth.longValue());
            addFieldValue(fieldValues, ChronoField.MONTH_OF_YEAR, (long) chronoDate.get(ChronoField.MONTH_OF_YEAR));
            addFieldValue(fieldValues, ChronoField.YEAR, (long) chronoDate.get(ChronoField.YEAR));
        }
    }

    ChronoLocalDate resolveYearOfEra(Map<TemporalField, Long> fieldValues, ResolverStyle resolverStyle) {
        Long yoeLong = (Long) fieldValues.remove(ChronoField.YEAR_OF_ERA);
        if (yoeLong != null) {
            int yoe;
            Long eraLong = (Long) fieldValues.remove(ChronoField.ERA);
            if (resolverStyle != ResolverStyle.LENIENT) {
                yoe = range(ChronoField.YEAR_OF_ERA).checkValidIntValue(yoeLong.longValue(), ChronoField.YEAR_OF_ERA);
            } else {
                yoe = Math.toIntExact(yoeLong.longValue());
            }
            if (eraLong != null) {
                addFieldValue(fieldValues, ChronoField.YEAR, (long) prolepticYear(eraOf(range(ChronoField.ERA).checkValidIntValue(eraLong.longValue(), ChronoField.ERA)), yoe));
            } else if (fieldValues.containsKey(ChronoField.YEAR)) {
                addFieldValue(fieldValues, ChronoField.YEAR, (long) prolepticYear(dateYearDay(range(ChronoField.YEAR).checkValidIntValue(((Long) fieldValues.get(ChronoField.YEAR)).longValue(), ChronoField.YEAR), 1).getEra(), yoe));
            } else if (resolverStyle == ResolverStyle.STRICT) {
                fieldValues.put(ChronoField.YEAR_OF_ERA, yoeLong);
            } else {
                List<Era> eras = eras();
                if (eras.isEmpty()) {
                    addFieldValue(fieldValues, ChronoField.YEAR, (long) yoe);
                } else {
                    addFieldValue(fieldValues, ChronoField.YEAR, (long) prolepticYear((Era) eras.get(eras.size() - 1), yoe));
                }
            }
        } else if (fieldValues.containsKey(ChronoField.ERA)) {
            range(ChronoField.ERA).checkValidValue(((Long) fieldValues.get(ChronoField.ERA)).longValue(), ChronoField.ERA);
        }
        return null;
    }

    ChronoLocalDate resolveYMD(Map<TemporalField, Long> fieldValues, ResolverStyle resolverStyle) {
        int y = range(ChronoField.YEAR).checkValidIntValue(((Long) fieldValues.remove(ChronoField.YEAR)).longValue(), ChronoField.YEAR);
        if (resolverStyle == ResolverStyle.LENIENT) {
            long months = Math.subtractExact(((Long) fieldValues.remove(ChronoField.MONTH_OF_YEAR)).longValue(), 1);
            return date(y, 1, 1).plus(months, ChronoUnit.MONTHS).plus(Math.subtractExact(((Long) fieldValues.remove(ChronoField.DAY_OF_MONTH)).longValue(), 1), ChronoUnit.DAYS);
        }
        int moy = range(ChronoField.MONTH_OF_YEAR).checkValidIntValue(((Long) fieldValues.remove(ChronoField.MONTH_OF_YEAR)).longValue(), ChronoField.MONTH_OF_YEAR);
        int dom = range(ChronoField.DAY_OF_MONTH).checkValidIntValue(((Long) fieldValues.remove(ChronoField.DAY_OF_MONTH)).longValue(), ChronoField.DAY_OF_MONTH);
        if (resolverStyle != ResolverStyle.SMART) {
            return date(y, moy, dom);
        }
        try {
            return date(y, moy, dom);
        } catch (DateTimeException e) {
            return date(y, moy, 1).with(TemporalAdjusters.lastDayOfMonth());
        }
    }

    ChronoLocalDate resolveYD(Map<TemporalField, Long> fieldValues, ResolverStyle resolverStyle) {
        int y = range(ChronoField.YEAR).checkValidIntValue(((Long) fieldValues.remove(ChronoField.YEAR)).longValue(), ChronoField.YEAR);
        if (resolverStyle != ResolverStyle.LENIENT) {
            return dateYearDay(y, range(ChronoField.DAY_OF_YEAR).checkValidIntValue(((Long) fieldValues.remove(ChronoField.DAY_OF_YEAR)).longValue(), ChronoField.DAY_OF_YEAR));
        }
        return dateYearDay(y, 1).plus(Math.subtractExact(((Long) fieldValues.remove(ChronoField.DAY_OF_YEAR)).longValue(), 1), ChronoUnit.DAYS);
    }

    ChronoLocalDate resolveYMAA(Map<TemporalField, Long> fieldValues, ResolverStyle resolverStyle) {
        int y = range(ChronoField.YEAR).checkValidIntValue(((Long) fieldValues.remove(ChronoField.YEAR)).longValue(), ChronoField.YEAR);
        if (resolverStyle == ResolverStyle.LENIENT) {
            long months = Math.subtractExact(((Long) fieldValues.remove(ChronoField.MONTH_OF_YEAR)).longValue(), 1);
            long weeks = Math.subtractExact(((Long) fieldValues.remove(ChronoField.ALIGNED_WEEK_OF_MONTH)).longValue(), 1);
            return date(y, 1, 1).plus(months, ChronoUnit.MONTHS).plus(weeks, ChronoUnit.WEEKS).plus(Math.subtractExact(((Long) fieldValues.remove(ChronoField.ALIGNED_DAY_OF_WEEK_IN_MONTH)).longValue(), 1), ChronoUnit.DAYS);
        }
        int moy = range(ChronoField.MONTH_OF_YEAR).checkValidIntValue(((Long) fieldValues.remove(ChronoField.MONTH_OF_YEAR)).longValue(), ChronoField.MONTH_OF_YEAR);
        ChronoLocalDate date = date(y, moy, 1).plus((long) (((range(ChronoField.ALIGNED_WEEK_OF_MONTH).checkValidIntValue(((Long) fieldValues.remove(ChronoField.ALIGNED_WEEK_OF_MONTH)).longValue(), ChronoField.ALIGNED_WEEK_OF_MONTH) - 1) * 7) + (range(ChronoField.ALIGNED_DAY_OF_WEEK_IN_MONTH).checkValidIntValue(((Long) fieldValues.remove(ChronoField.ALIGNED_DAY_OF_WEEK_IN_MONTH)).longValue(), ChronoField.ALIGNED_DAY_OF_WEEK_IN_MONTH) - 1)), ChronoUnit.DAYS);
        if (resolverStyle != ResolverStyle.STRICT || date.get(ChronoField.MONTH_OF_YEAR) == moy) {
            return date;
        }
        throw new DateTimeException("Strict mode rejected resolved date as it is in a different month");
    }

    ChronoLocalDate resolveYMAD(Map<TemporalField, Long> fieldValues, ResolverStyle resolverStyle) {
        int y = range(ChronoField.YEAR).checkValidIntValue(((Long) fieldValues.remove(ChronoField.YEAR)).longValue(), ChronoField.YEAR);
        if (resolverStyle == ResolverStyle.LENIENT) {
            return resolveAligned(date(y, 1, 1), Math.subtractExact(((Long) fieldValues.remove(ChronoField.MONTH_OF_YEAR)).longValue(), 1), Math.subtractExact(((Long) fieldValues.remove(ChronoField.ALIGNED_WEEK_OF_MONTH)).longValue(), 1), Math.subtractExact(((Long) fieldValues.remove(ChronoField.DAY_OF_WEEK)).longValue(), 1));
        }
        int moy = range(ChronoField.MONTH_OF_YEAR).checkValidIntValue(((Long) fieldValues.remove(ChronoField.MONTH_OF_YEAR)).longValue(), ChronoField.MONTH_OF_YEAR);
        int aw = range(ChronoField.ALIGNED_WEEK_OF_MONTH).checkValidIntValue(((Long) fieldValues.remove(ChronoField.ALIGNED_WEEK_OF_MONTH)).longValue(), ChronoField.ALIGNED_WEEK_OF_MONTH);
        int dow = range(ChronoField.DAY_OF_WEEK).checkValidIntValue(((Long) fieldValues.remove(ChronoField.DAY_OF_WEEK)).longValue(), ChronoField.DAY_OF_WEEK);
        ChronoLocalDate date = date(y, moy, 1).plus((long) ((aw - 1) * 7), ChronoUnit.DAYS).with(TemporalAdjusters.nextOrSame(DayOfWeek.of(dow)));
        if (resolverStyle != ResolverStyle.STRICT || date.get(ChronoField.MONTH_OF_YEAR) == moy) {
            return date;
        }
        throw new DateTimeException("Strict mode rejected resolved date as it is in a different month");
    }

    ChronoLocalDate resolveYAA(Map<TemporalField, Long> fieldValues, ResolverStyle resolverStyle) {
        int y = range(ChronoField.YEAR).checkValidIntValue(((Long) fieldValues.remove(ChronoField.YEAR)).longValue(), ChronoField.YEAR);
        if (resolverStyle == ResolverStyle.LENIENT) {
            long weeks = Math.subtractExact(((Long) fieldValues.remove(ChronoField.ALIGNED_WEEK_OF_YEAR)).longValue(), 1);
            return dateYearDay(y, 1).plus(weeks, ChronoUnit.WEEKS).plus(Math.subtractExact(((Long) fieldValues.remove(ChronoField.ALIGNED_DAY_OF_WEEK_IN_YEAR)).longValue(), 1), ChronoUnit.DAYS);
        }
        ChronoLocalDate date = dateYearDay(y, 1).plus((long) (((range(ChronoField.ALIGNED_WEEK_OF_YEAR).checkValidIntValue(((Long) fieldValues.remove(ChronoField.ALIGNED_WEEK_OF_YEAR)).longValue(), ChronoField.ALIGNED_WEEK_OF_YEAR) - 1) * 7) + (range(ChronoField.ALIGNED_DAY_OF_WEEK_IN_YEAR).checkValidIntValue(((Long) fieldValues.remove(ChronoField.ALIGNED_DAY_OF_WEEK_IN_YEAR)).longValue(), ChronoField.ALIGNED_DAY_OF_WEEK_IN_YEAR) - 1)), ChronoUnit.DAYS);
        if (resolverStyle != ResolverStyle.STRICT || date.get(ChronoField.YEAR) == y) {
            return date;
        }
        throw new DateTimeException("Strict mode rejected resolved date as it is in a different year");
    }

    ChronoLocalDate resolveYAD(Map<TemporalField, Long> fieldValues, ResolverStyle resolverStyle) {
        int y = range(ChronoField.YEAR).checkValidIntValue(((Long) fieldValues.remove(ChronoField.YEAR)).longValue(), ChronoField.YEAR);
        if (resolverStyle == ResolverStyle.LENIENT) {
            return resolveAligned(dateYearDay(y, 1), 0, Math.subtractExact(((Long) fieldValues.remove(ChronoField.ALIGNED_WEEK_OF_YEAR)).longValue(), 1), Math.subtractExact(((Long) fieldValues.remove(ChronoField.DAY_OF_WEEK)).longValue(), 1));
        }
        ChronoLocalDate date = dateYearDay(y, 1).plus((long) ((range(ChronoField.ALIGNED_WEEK_OF_YEAR).checkValidIntValue(((Long) fieldValues.remove(ChronoField.ALIGNED_WEEK_OF_YEAR)).longValue(), ChronoField.ALIGNED_WEEK_OF_YEAR) - 1) * 7), ChronoUnit.DAYS).with(TemporalAdjusters.nextOrSame(DayOfWeek.of(range(ChronoField.DAY_OF_WEEK).checkValidIntValue(((Long) fieldValues.remove(ChronoField.DAY_OF_WEEK)).longValue(), ChronoField.DAY_OF_WEEK))));
        if (resolverStyle != ResolverStyle.STRICT || date.get(ChronoField.YEAR) == y) {
            return date;
        }
        throw new DateTimeException("Strict mode rejected resolved date as it is in a different year");
    }

    ChronoLocalDate resolveAligned(ChronoLocalDate base, long months, long weeks, long dow) {
        ChronoLocalDate date = base.plus(months, ChronoUnit.MONTHS).plus(weeks, ChronoUnit.WEEKS);
        if (dow > 7) {
            date = date.plus((dow - 1) / 7, ChronoUnit.WEEKS);
            dow = ((dow - 1) % 7) + 1;
        } else if (dow < 1) {
            date = date.plus(Math.subtractExact(dow, 7) / 7, ChronoUnit.WEEKS);
            dow = ((6 + dow) % 7) + 1;
        }
        return date.with(TemporalAdjusters.nextOrSame(DayOfWeek.of((int) dow)));
    }

    void addFieldValue(Map<TemporalField, Long> fieldValues, ChronoField field, long value) {
        Object old = (Long) fieldValues.get(field);
        if (old == null || old.longValue() == value) {
            fieldValues.put(field, Long.valueOf(value));
            return;
        }
        throw new DateTimeException("Conflict found: " + field + " " + old + " differs from " + field + " " + value);
    }

    public int compareTo(Chronology other) {
        return getId().compareTo(other.getId());
    }

    public boolean equals(Object obj) {
        boolean z = true;
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof AbstractChronology)) {
            return false;
        }
        if (compareTo((AbstractChronology) obj) != 0) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        return getClass().hashCode() ^ getId().hashCode();
    }

    public String toString() {
        return getId();
    }

    Object writeReplace() {
        return new Ser((byte) 1, this);
    }

    private void readObject(ObjectInputStream s) throws ObjectStreamException {
        throw new InvalidObjectException("Deserialization via serialization delegate");
    }

    void writeExternal(DataOutput out) throws IOException {
        out.writeUTF(getId());
    }

    static Chronology readExternal(DataInput in) throws IOException {
        return Chronology.of(in.readUTF());
    }
}
