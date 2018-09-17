package java.nio.file.attribute;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import sun.util.locale.LanguageTag;

public final class FileTime implements Comparable<FileTime> {
    private static final /* synthetic */ int[] -java-util-concurrent-TimeUnitSwitchesValues = null;
    private static final long DAYS_PER_10000_YEARS = 3652425;
    private static final long HOURS_PER_DAY = 24;
    private static final long MAX_SECOND = 31556889864403199L;
    private static final long MICROS_PER_SECOND = 1000000;
    private static final long MILLIS_PER_SECOND = 1000;
    private static final long MINUTES_PER_HOUR = 60;
    private static final long MIN_SECOND = -31557014167219200L;
    private static final int NANOS_PER_MICRO = 1000;
    private static final int NANOS_PER_MILLI = 1000000;
    private static final long NANOS_PER_SECOND = 1000000000;
    private static final long SECONDS_0000_TO_1970 = 62167219200L;
    private static final long SECONDS_PER_10000_YEARS = 315569520000L;
    private static final long SECONDS_PER_DAY = 86400;
    private static final long SECONDS_PER_HOUR = 3600;
    private static final long SECONDS_PER_MINUTE = 60;
    private Instant instant;
    private final TimeUnit unit;
    private final long value;
    private String valueAsString;

    private static /* synthetic */ int[] -getjava-util-concurrent-TimeUnitSwitchesValues() {
        if (-java-util-concurrent-TimeUnitSwitchesValues != null) {
            return -java-util-concurrent-TimeUnitSwitchesValues;
        }
        int[] iArr = new int[TimeUnit.values().length];
        try {
            iArr[TimeUnit.DAYS.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[TimeUnit.HOURS.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[TimeUnit.MICROSECONDS.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[TimeUnit.MILLISECONDS.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[TimeUnit.MINUTES.ordinal()] = 5;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[TimeUnit.NANOSECONDS.ordinal()] = 6;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[TimeUnit.SECONDS.ordinal()] = 7;
        } catch (NoSuchFieldError e7) {
        }
        -java-util-concurrent-TimeUnitSwitchesValues = iArr;
        return iArr;
    }

    private FileTime(long value, TimeUnit unit, Instant instant) {
        this.value = value;
        this.unit = unit;
        this.instant = instant;
    }

    public static FileTime from(long value, TimeUnit unit) {
        Objects.requireNonNull((Object) unit, "unit");
        return new FileTime(value, unit, null);
    }

    public static FileTime fromMillis(long value) {
        return new FileTime(value, TimeUnit.MILLISECONDS, null);
    }

    public static FileTime from(Instant instant) {
        Objects.requireNonNull((Object) instant, "instant");
        return new FileTime(0, null, instant);
    }

    public long to(TimeUnit unit) {
        Objects.requireNonNull((Object) unit, "unit");
        if (this.unit != null) {
            return unit.convert(this.value, this.unit);
        }
        long secs = unit.convert(this.instant.getEpochSecond(), TimeUnit.SECONDS);
        if (secs == Long.MIN_VALUE || secs == Long.MAX_VALUE) {
            return secs;
        }
        long nanos = unit.convert((long) this.instant.getNano(), TimeUnit.NANOSECONDS);
        long r = secs + nanos;
        if (((secs ^ r) & (nanos ^ r)) >= 0) {
            return r;
        }
        return secs < 0 ? Long.MIN_VALUE : Long.MAX_VALUE;
    }

    public long toMillis() {
        if (this.unit != null) {
            return this.unit.toMillis(this.value);
        }
        long secs = this.instant.getEpochSecond();
        int nanos = this.instant.getNano();
        long r = secs * MILLIS_PER_SECOND;
        if (((Math.abs(secs) | MILLIS_PER_SECOND) >>> 31) == 0 || r / MILLIS_PER_SECOND == secs) {
            return ((long) (nanos / NANOS_PER_MILLI)) + r;
        }
        return secs < 0 ? Long.MIN_VALUE : Long.MAX_VALUE;
    }

    private static long scale(long d, long m, long over) {
        if (d > over) {
            return Long.MAX_VALUE;
        }
        if (d < (-over)) {
            return Long.MIN_VALUE;
        }
        return d * m;
    }

    public Instant toInstant() {
        if (this.instant == null) {
            long secs;
            int nanos = 0;
            switch (-getjava-util-concurrent-TimeUnitSwitchesValues()[this.unit.ordinal()]) {
                case 1:
                    secs = scale(this.value, SECONDS_PER_DAY, 106751991167300L);
                    break;
                case 2:
                    secs = scale(this.value, SECONDS_PER_HOUR, 2562047788015215L);
                    break;
                case 3:
                    secs = Math.floorDiv(this.value, (long) MICROS_PER_SECOND);
                    nanos = ((int) Math.floorMod(this.value, (long) MICROS_PER_SECOND)) * 1000;
                    break;
                case 4:
                    secs = Math.floorDiv(this.value, (long) MILLIS_PER_SECOND);
                    nanos = ((int) Math.floorMod(this.value, (long) MILLIS_PER_SECOND)) * NANOS_PER_MILLI;
                    break;
                case 5:
                    secs = scale(this.value, 60, 153722867280912930L);
                    break;
                case 6:
                    secs = Math.floorDiv(this.value, (long) NANOS_PER_SECOND);
                    nanos = (int) Math.floorMod(this.value, (long) NANOS_PER_SECOND);
                    break;
                case 7:
                    secs = this.value;
                    break;
                default:
                    throw new AssertionError((Object) "Unit not handled");
            }
            if (secs <= MIN_SECOND) {
                this.instant = Instant.MIN;
            } else if (secs >= MAX_SECOND) {
                this.instant = Instant.MAX;
            } else {
                this.instant = Instant.ofEpochSecond(secs, (long) nanos);
            }
        }
        return this.instant;
    }

    public boolean equals(Object obj) {
        return (obj instanceof FileTime) && compareTo((FileTime) obj) == 0;
    }

    public int hashCode() {
        return toInstant().hashCode();
    }

    private long toDays() {
        if (this.unit != null) {
            return this.unit.toDays(this.value);
        }
        return TimeUnit.SECONDS.toDays(toInstant().getEpochSecond());
    }

    private long toExcessNanos(long days) {
        if (this.unit != null) {
            return this.unit.toNanos(this.value - this.unit.convert(days, TimeUnit.DAYS));
        }
        return TimeUnit.SECONDS.toNanos(toInstant().getEpochSecond() - TimeUnit.DAYS.toSeconds(days));
    }

    public int compareTo(FileTime other) {
        if (this.unit != null && this.unit == other.unit) {
            return Long.compare(this.value, other.value);
        }
        long secs = toInstant().getEpochSecond();
        int cmp = Long.compare(secs, other.toInstant().getEpochSecond());
        if (cmp != 0) {
            return cmp;
        }
        cmp = Long.compare((long) toInstant().getNano(), (long) other.toInstant().getNano());
        if (cmp != 0) {
            return cmp;
        }
        if (secs != MAX_SECOND && secs != MIN_SECOND) {
            return 0;
        }
        long days = toDays();
        long daysOther = other.toDays();
        if (days == daysOther) {
            return Long.compare(toExcessNanos(days), other.toExcessNanos(daysOther));
        }
        return Long.compare(days, daysOther);
    }

    private StringBuilder append(StringBuilder sb, int w, int d) {
        while (w > 0) {
            sb.append((char) ((d / w) + 48));
            d %= w;
            w /= 10;
        }
        return sb;
    }

    public String toString() {
        if (this.valueAsString == null) {
            long secs;
            LocalDateTime ldt;
            int year;
            int nanos = 0;
            if (this.instant != null || this.unit.compareTo(TimeUnit.SECONDS) < 0) {
                secs = toInstant().getEpochSecond();
                nanos = toInstant().getNano();
            } else {
                secs = this.unit.toSeconds(this.value);
            }
            long zeroSecs;
            long hi;
            if (secs >= -62167219200L) {
                zeroSecs = (secs - SECONDS_PER_10000_YEARS) + SECONDS_0000_TO_1970;
                hi = Math.floorDiv(zeroSecs, (long) SECONDS_PER_10000_YEARS) + 1;
                ldt = LocalDateTime.ofEpochSecond(Math.floorMod(zeroSecs, (long) SECONDS_PER_10000_YEARS) - SECONDS_0000_TO_1970, nanos, ZoneOffset.UTC);
                year = ldt.getYear() + (((int) hi) * 10000);
            } else {
                zeroSecs = secs + SECONDS_0000_TO_1970;
                hi = zeroSecs / SECONDS_PER_10000_YEARS;
                ldt = LocalDateTime.ofEpochSecond((zeroSecs % SECONDS_PER_10000_YEARS) - SECONDS_0000_TO_1970, nanos, ZoneOffset.UTC);
                year = ldt.getYear() + (((int) hi) * 10000);
            }
            if (year <= 0) {
                year--;
            }
            int fraction = ldt.getNano();
            StringBuilder sb = new StringBuilder(64);
            sb.append(year < 0 ? LanguageTag.SEP : "");
            year = Math.abs(year);
            if (year < 10000) {
                append(sb, 1000, Math.abs(year));
            } else {
                sb.append(String.valueOf(year));
            }
            sb.append('-');
            append(sb, 10, ldt.getMonthValue());
            sb.append('-');
            append(sb, 10, ldt.getDayOfMonth());
            sb.append('T');
            append(sb, 10, ldt.getHour());
            sb.append(':');
            append(sb, 10, ldt.getMinute());
            sb.append(':');
            append(sb, 10, ldt.getSecond());
            if (fraction != 0) {
                sb.append('.');
                int w = 100000000;
                while (fraction % 10 == 0) {
                    fraction /= 10;
                    w /= 10;
                }
                append(sb, w, fraction);
            }
            sb.append('Z');
            this.valueAsString = sb.toString();
        }
        return this.valueAsString;
    }
}
