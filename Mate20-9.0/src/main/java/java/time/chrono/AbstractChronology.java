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
import java.time.temporal.TemporalUnit;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
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
    private static final ConcurrentHashMap<String, Chronology> CHRONOS_BY_ID = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Chronology> CHRONOS_BY_TYPE = new ConcurrentHashMap<>();
    static final Comparator<ChronoLocalDate> DATE_ORDER = $$Lambda$AbstractChronology$j22w8kHhJoqCd56hhLQK1G0VLFw.INSTANCE;
    static final Comparator<ChronoLocalDateTime<? extends ChronoLocalDate>> DATE_TIME_ORDER = $$Lambda$AbstractChronology$onW9aZyLFliH5Gg1qLodD_GoPfA.INSTANCE;
    static final Comparator<ChronoZonedDateTime<?>> INSTANT_ORDER = $$Lambda$AbstractChronology$5b0W7uLeaWkn0HLPDKwPXzJ7HPo.INSTANCE;

    static /* synthetic */ int lambda$static$b5a61975$1(ChronoLocalDateTime dateTime1, ChronoLocalDateTime dateTime2) {
        int cmp = Long.compare(dateTime1.toLocalDate().toEpochDay(), dateTime2.toLocalDate().toEpochDay());
        if (cmp == 0) {
            return Long.compare(dateTime1.toLocalTime().toNanoOfDay(), dateTime2.toLocalTime().toNanoOfDay());
        }
        return cmp;
    }

    static /* synthetic */ int lambda$static$2241c452$1(ChronoZonedDateTime dateTime1, ChronoZonedDateTime dateTime2) {
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
        Chronology prev = CHRONOS_BY_ID.putIfAbsent(id, chrono);
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
        Iterator<AbstractChronology> it = ServiceLoader.load(AbstractChronology.class, null).iterator();
        while (it.hasNext()) {
            AbstractChronology chrono = it.next();
            String id = chrono.getId();
            if (id.equals("ISO") || registerChrono(chrono) != null) {
                PlatformLogger logger = PlatformLogger.getLogger("java.time.chrono");
                logger.warning("Ignoring duplicate Chronology, from ServiceLoader configuration " + id);
            }
        }
        registerChrono(IsoChronology.INSTANCE);
        return true;
    }

    static Chronology ofLocale(Locale locale) {
        Objects.requireNonNull(locale, "locale");
        String type = locale.getUnicodeLocaleType("ca");
        if (type == null || "iso".equals(type) || "iso8601".equals(type)) {
            return IsoChronology.INSTANCE;
        }
        do {
            Chronology chrono = CHRONOS_BY_TYPE.get(type);
            if (chrono != null) {
                return chrono;
            }
        } while (initCache());
        Iterator<Chronology> it = ServiceLoader.load(Chronology.class).iterator();
        while (it.hasNext()) {
            Chronology chrono2 = it.next();
            if (type.equals(chrono2.getCalendarType())) {
                return chrono2;
            }
        }
        throw new DateTimeException("Unknown calendar system: " + type);
    }

    static Chronology of(String id) {
        Objects.requireNonNull(id, PolicyInformation.ID);
        do {
            Chronology chrono = of0(id);
            if (chrono != null) {
                return chrono;
            }
        } while (initCache());
        Iterator<Chronology> it = ServiceLoader.load(Chronology.class).iterator();
        while (it.hasNext()) {
            Chronology chrono2 = it.next();
            if (!id.equals(chrono2.getId())) {
                if (id.equals(chrono2.getCalendarType())) {
                }
            }
            return chrono2;
        }
        throw new DateTimeException("Unknown chronology: " + id);
    }

    private static Chronology of0(String id) {
        Chronology chrono = CHRONOS_BY_ID.get(id);
        if (chrono == null) {
            return CHRONOS_BY_TYPE.get(id);
        }
        return chrono;
    }

    static Set<Chronology> getAvailableChronologies() {
        initCache();
        HashSet<Chronology> chronos = new HashSet<>((Collection<? extends Chronology>) CHRONOS_BY_ID.values());
        Iterator<Chronology> it = ServiceLoader.load(Chronology.class).iterator();
        while (it.hasNext()) {
            chronos.add(it.next());
        }
        return chronos;
    }

    protected AbstractChronology() {
    }

    public ChronoLocalDate resolveDate(Map<TemporalField, Long> fieldValues, ResolverStyle resolverStyle) {
        if (fieldValues.containsKey(ChronoField.EPOCH_DAY)) {
            return dateEpochDay(fieldValues.remove(ChronoField.EPOCH_DAY).longValue());
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

    /* access modifiers changed from: package-private */
    public void resolveProlepticMonth(Map<TemporalField, Long> fieldValues, ResolverStyle resolverStyle) {
        Long pMonth = fieldValues.remove(ChronoField.PROLEPTIC_MONTH);
        if (pMonth != null) {
            if (resolverStyle != ResolverStyle.LENIENT) {
                ChronoField.PROLEPTIC_MONTH.checkValidValue(pMonth.longValue());
            }
            ChronoLocalDate chronoDate = dateNow().with((TemporalField) ChronoField.DAY_OF_MONTH, 1).with((TemporalField) ChronoField.PROLEPTIC_MONTH, pMonth.longValue());
            addFieldValue(fieldValues, ChronoField.MONTH_OF_YEAR, (long) chronoDate.get(ChronoField.MONTH_OF_YEAR));
            addFieldValue(fieldValues, ChronoField.YEAR, (long) chronoDate.get(ChronoField.YEAR));
        }
    }

    /* access modifiers changed from: package-private */
    public ChronoLocalDate resolveYearOfEra(Map<TemporalField, Long> fieldValues, ResolverStyle resolverStyle) {
        int yoe;
        Long yoeLong = fieldValues.remove(ChronoField.YEAR_OF_ERA);
        if (yoeLong != null) {
            Long eraLong = fieldValues.remove(ChronoField.ERA);
            if (resolverStyle != ResolverStyle.LENIENT) {
                yoe = range(ChronoField.YEAR_OF_ERA).checkValidIntValue(yoeLong.longValue(), ChronoField.YEAR_OF_ERA);
            } else {
                yoe = Math.toIntExact(yoeLong.longValue());
            }
            if (eraLong != null) {
                addFieldValue(fieldValues, ChronoField.YEAR, (long) prolepticYear(eraOf(range(ChronoField.ERA).checkValidIntValue(eraLong.longValue(), ChronoField.ERA)), yoe));
            } else if (fieldValues.containsKey(ChronoField.YEAR)) {
                addFieldValue(fieldValues, ChronoField.YEAR, (long) prolepticYear(dateYearDay(range(ChronoField.YEAR).checkValidIntValue(fieldValues.get(ChronoField.YEAR).longValue(), ChronoField.YEAR), 1).getEra(), yoe));
            } else if (resolverStyle == ResolverStyle.STRICT) {
                fieldValues.put(ChronoField.YEAR_OF_ERA, yoeLong);
            } else {
                List<Era> eras = eras();
                if (eras.isEmpty()) {
                    addFieldValue(fieldValues, ChronoField.YEAR, (long) yoe);
                } else {
                    addFieldValue(fieldValues, ChronoField.YEAR, (long) prolepticYear(eras.get(eras.size() - 1), yoe));
                }
            }
        } else if (fieldValues.containsKey(ChronoField.ERA)) {
            range(ChronoField.ERA).checkValidValue(fieldValues.get(ChronoField.ERA).longValue(), ChronoField.ERA);
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public ChronoLocalDate resolveYMD(Map<TemporalField, Long> fieldValues, ResolverStyle resolverStyle) {
        int y = range(ChronoField.YEAR).checkValidIntValue(fieldValues.remove(ChronoField.YEAR).longValue(), ChronoField.YEAR);
        if (resolverStyle == ResolverStyle.LENIENT) {
            long months = Math.subtractExact(fieldValues.remove(ChronoField.MONTH_OF_YEAR).longValue(), 1);
            return date(y, 1, 1).plus(months, (TemporalUnit) ChronoUnit.MONTHS).plus(Math.subtractExact(fieldValues.remove(ChronoField.DAY_OF_MONTH).longValue(), 1), (TemporalUnit) ChronoUnit.DAYS);
        }
        int moy = range(ChronoField.MONTH_OF_YEAR).checkValidIntValue(fieldValues.remove(ChronoField.MONTH_OF_YEAR).longValue(), ChronoField.MONTH_OF_YEAR);
        int dom = range(ChronoField.DAY_OF_MONTH).checkValidIntValue(fieldValues.remove(ChronoField.DAY_OF_MONTH).longValue(), ChronoField.DAY_OF_MONTH);
        if (resolverStyle != ResolverStyle.SMART) {
            return date(y, moy, dom);
        }
        try {
            return date(y, moy, dom);
        } catch (DateTimeException e) {
            return date(y, moy, 1).with(TemporalAdjusters.lastDayOfMonth());
        }
    }

    /* access modifiers changed from: package-private */
    public ChronoLocalDate resolveYD(Map<TemporalField, Long> fieldValues, ResolverStyle resolverStyle) {
        int y = range(ChronoField.YEAR).checkValidIntValue(fieldValues.remove(ChronoField.YEAR).longValue(), ChronoField.YEAR);
        if (resolverStyle != ResolverStyle.LENIENT) {
            return dateYearDay(y, range(ChronoField.DAY_OF_YEAR).checkValidIntValue(fieldValues.remove(ChronoField.DAY_OF_YEAR).longValue(), ChronoField.DAY_OF_YEAR));
        }
        return dateYearDay(y, 1).plus(Math.subtractExact(fieldValues.remove(ChronoField.DAY_OF_YEAR).longValue(), 1), (TemporalUnit) ChronoUnit.DAYS);
    }

    /* access modifiers changed from: package-private */
    public ChronoLocalDate resolveYMAA(Map<TemporalField, Long> fieldValues, ResolverStyle resolverStyle) {
        int y = range(ChronoField.YEAR).checkValidIntValue(fieldValues.remove(ChronoField.YEAR).longValue(), ChronoField.YEAR);
        if (resolverStyle == ResolverStyle.LENIENT) {
            long months = Math.subtractExact(fieldValues.remove(ChronoField.MONTH_OF_YEAR).longValue(), 1);
            long weeks = Math.subtractExact(fieldValues.remove(ChronoField.ALIGNED_WEEK_OF_MONTH).longValue(), 1);
            return date(y, 1, 1).plus(months, (TemporalUnit) ChronoUnit.MONTHS).plus(weeks, (TemporalUnit) ChronoUnit.WEEKS).plus(Math.subtractExact(fieldValues.remove(ChronoField.ALIGNED_DAY_OF_WEEK_IN_MONTH).longValue(), 1), (TemporalUnit) ChronoUnit.DAYS);
        }
        int moy = range(ChronoField.MONTH_OF_YEAR).checkValidIntValue(fieldValues.remove(ChronoField.MONTH_OF_YEAR).longValue(), ChronoField.MONTH_OF_YEAR);
        ChronoLocalDate date = date(y, moy, 1).plus((long) (((range(ChronoField.ALIGNED_WEEK_OF_MONTH).checkValidIntValue(fieldValues.remove(ChronoField.ALIGNED_WEEK_OF_MONTH).longValue(), ChronoField.ALIGNED_WEEK_OF_MONTH) - 1) * 7) + (range(ChronoField.ALIGNED_DAY_OF_WEEK_IN_MONTH).checkValidIntValue(fieldValues.remove(ChronoField.ALIGNED_DAY_OF_WEEK_IN_MONTH).longValue(), ChronoField.ALIGNED_DAY_OF_WEEK_IN_MONTH) - 1)), (TemporalUnit) ChronoUnit.DAYS);
        if (resolverStyle != ResolverStyle.STRICT || date.get(ChronoField.MONTH_OF_YEAR) == moy) {
            return date;
        }
        throw new DateTimeException("Strict mode rejected resolved date as it is in a different month");
    }

    /* access modifiers changed from: package-private */
    public ChronoLocalDate resolveYMAD(Map<TemporalField, Long> fieldValues, ResolverStyle resolverStyle) {
        Map<TemporalField, Long> map = fieldValues;
        ResolverStyle resolverStyle2 = resolverStyle;
        int y = range(ChronoField.YEAR).checkValidIntValue(map.remove(ChronoField.YEAR).longValue(), ChronoField.YEAR);
        if (resolverStyle2 == ResolverStyle.LENIENT) {
            return resolveAligned(date(y, 1, 1), Math.subtractExact(map.remove(ChronoField.MONTH_OF_YEAR).longValue(), 1), Math.subtractExact(map.remove(ChronoField.ALIGNED_WEEK_OF_MONTH).longValue(), 1), Math.subtractExact(map.remove(ChronoField.DAY_OF_WEEK).longValue(), 1));
        }
        int moy = range(ChronoField.MONTH_OF_YEAR).checkValidIntValue(map.remove(ChronoField.MONTH_OF_YEAR).longValue(), ChronoField.MONTH_OF_YEAR);
        ChronoLocalDate date = date(y, moy, 1).plus((long) ((range(ChronoField.ALIGNED_WEEK_OF_MONTH).checkValidIntValue(map.remove(ChronoField.ALIGNED_WEEK_OF_MONTH).longValue(), ChronoField.ALIGNED_WEEK_OF_MONTH) - 1) * 7), (TemporalUnit) ChronoUnit.DAYS).with(TemporalAdjusters.nextOrSame(DayOfWeek.of(range(ChronoField.DAY_OF_WEEK).checkValidIntValue(map.remove(ChronoField.DAY_OF_WEEK).longValue(), ChronoField.DAY_OF_WEEK))));
        if (resolverStyle2 != ResolverStyle.STRICT || date.get(ChronoField.MONTH_OF_YEAR) == moy) {
            return date;
        }
        throw new DateTimeException("Strict mode rejected resolved date as it is in a different month");
    }

    /* access modifiers changed from: package-private */
    public ChronoLocalDate resolveYAA(Map<TemporalField, Long> fieldValues, ResolverStyle resolverStyle) {
        int y = range(ChronoField.YEAR).checkValidIntValue(fieldValues.remove(ChronoField.YEAR).longValue(), ChronoField.YEAR);
        if (resolverStyle == ResolverStyle.LENIENT) {
            long weeks = Math.subtractExact(fieldValues.remove(ChronoField.ALIGNED_WEEK_OF_YEAR).longValue(), 1);
            return dateYearDay(y, 1).plus(weeks, (TemporalUnit) ChronoUnit.WEEKS).plus(Math.subtractExact(fieldValues.remove(ChronoField.ALIGNED_DAY_OF_WEEK_IN_YEAR).longValue(), 1), (TemporalUnit) ChronoUnit.DAYS);
        }
        ChronoLocalDate date = dateYearDay(y, 1).plus((long) (((range(ChronoField.ALIGNED_WEEK_OF_YEAR).checkValidIntValue(fieldValues.remove(ChronoField.ALIGNED_WEEK_OF_YEAR).longValue(), ChronoField.ALIGNED_WEEK_OF_YEAR) - 1) * 7) + (range(ChronoField.ALIGNED_DAY_OF_WEEK_IN_YEAR).checkValidIntValue(fieldValues.remove(ChronoField.ALIGNED_DAY_OF_WEEK_IN_YEAR).longValue(), ChronoField.ALIGNED_DAY_OF_WEEK_IN_YEAR) - 1)), (TemporalUnit) ChronoUnit.DAYS);
        if (resolverStyle != ResolverStyle.STRICT || date.get(ChronoField.YEAR) == y) {
            return date;
        }
        throw new DateTimeException("Strict mode rejected resolved date as it is in a different year");
    }

    /* access modifiers changed from: package-private */
    public ChronoLocalDate resolveYAD(Map<TemporalField, Long> fieldValues, ResolverStyle resolverStyle) {
        Map<TemporalField, Long> map = fieldValues;
        ResolverStyle resolverStyle2 = resolverStyle;
        int y = range(ChronoField.YEAR).checkValidIntValue(map.remove(ChronoField.YEAR).longValue(), ChronoField.YEAR);
        if (resolverStyle2 == ResolverStyle.LENIENT) {
            return resolveAligned(dateYearDay(y, 1), 0, Math.subtractExact(map.remove(ChronoField.ALIGNED_WEEK_OF_YEAR).longValue(), 1), Math.subtractExact(map.remove(ChronoField.DAY_OF_WEEK).longValue(), 1));
        }
        ChronoLocalDate date = dateYearDay(y, 1).plus((long) ((range(ChronoField.ALIGNED_WEEK_OF_YEAR).checkValidIntValue(map.remove(ChronoField.ALIGNED_WEEK_OF_YEAR).longValue(), ChronoField.ALIGNED_WEEK_OF_YEAR) - 1) * 7), (TemporalUnit) ChronoUnit.DAYS).with(TemporalAdjusters.nextOrSame(DayOfWeek.of(range(ChronoField.DAY_OF_WEEK).checkValidIntValue(map.remove(ChronoField.DAY_OF_WEEK).longValue(), ChronoField.DAY_OF_WEEK))));
        if (resolverStyle2 != ResolverStyle.STRICT || date.get(ChronoField.YEAR) == y) {
            return date;
        }
        throw new DateTimeException("Strict mode rejected resolved date as it is in a different year");
    }

    /* access modifiers changed from: package-private */
    public ChronoLocalDate resolveAligned(ChronoLocalDate base, long months, long weeks, long dow) {
        ChronoLocalDate date = base.plus(months, (TemporalUnit) ChronoUnit.MONTHS).plus(weeks, (TemporalUnit) ChronoUnit.WEEKS);
        if (dow > 7) {
            date = date.plus((dow - 1) / 7, (TemporalUnit) ChronoUnit.WEEKS);
            dow = ((dow - 1) % 7) + 1;
        } else if (dow < 1) {
            date = date.plus(Math.subtractExact(dow, 7) / 7, (TemporalUnit) ChronoUnit.WEEKS);
            dow = ((6 + dow) % 7) + 1;
        }
        return date.with(TemporalAdjusters.nextOrSame(DayOfWeek.of((int) dow)));
    }

    /* access modifiers changed from: package-private */
    public void addFieldValue(Map<TemporalField, Long> fieldValues, ChronoField field, long value) {
        Long old = fieldValues.get(field);
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
        if (compareTo((Chronology) (AbstractChronology) obj) != 0) {
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

    /* access modifiers changed from: package-private */
    public Object writeReplace() {
        return new Ser((byte) 1, this);
    }

    private void readObject(ObjectInputStream s) throws ObjectStreamException {
        throw new InvalidObjectException("Deserialization via serialization delegate");
    }

    /* access modifiers changed from: package-private */
    public void writeExternal(DataOutput out) throws IOException {
        out.writeUTF(getId());
    }

    static Chronology readExternal(DataInput in) throws IOException {
        return Chronology.of(in.readUTF());
    }
}
