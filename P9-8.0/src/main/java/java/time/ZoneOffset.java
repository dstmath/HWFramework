package java.time;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.time.temporal.ChronoField;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalQueries;
import java.time.temporal.TemporalQuery;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.time.temporal.ValueRange;
import java.time.zone.ZoneRules;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import sun.util.locale.LanguageTag;
import sun.util.logging.PlatformLogger;

public final class ZoneOffset extends ZoneId implements TemporalAccessor, TemporalAdjuster, Comparable<ZoneOffset>, Serializable {
    private static final ConcurrentMap<String, ZoneOffset> ID_CACHE = new ConcurrentHashMap(16, 0.75f, 4);
    public static final ZoneOffset MAX = ofTotalSeconds(MAX_SECONDS);
    private static final int MAX_SECONDS = 64800;
    public static final ZoneOffset MIN = ofTotalSeconds(-64800);
    private static final ConcurrentMap<Integer, ZoneOffset> SECONDS_CACHE = new ConcurrentHashMap(16, 0.75f, 4);
    public static final ZoneOffset UTC = ofTotalSeconds(0);
    private static final long serialVersionUID = 2357656521762053153L;
    private final transient String id;
    private final int totalSeconds;

    public static ZoneOffset of(String offsetId) {
        Objects.requireNonNull((Object) offsetId, "offsetId");
        ZoneOffset offset = (ZoneOffset) ID_CACHE.get(offsetId);
        if (offset != null) {
            return offset;
        }
        int hours;
        int minutes;
        int seconds;
        switch (offsetId.length()) {
            case 2:
                offsetId = offsetId.charAt(0) + "0" + offsetId.charAt(1);
                break;
            case 3:
                break;
            case 5:
                hours = parseNumber(offsetId, 1, false);
                minutes = parseNumber(offsetId, 3, false);
                seconds = 0;
                break;
            case 6:
                hours = parseNumber(offsetId, 1, false);
                minutes = parseNumber(offsetId, 4, true);
                seconds = 0;
                break;
            case 7:
                hours = parseNumber(offsetId, 1, false);
                minutes = parseNumber(offsetId, 3, false);
                seconds = parseNumber(offsetId, 5, false);
                break;
            case 9:
                hours = parseNumber(offsetId, 1, false);
                minutes = parseNumber(offsetId, 4, true);
                seconds = parseNumber(offsetId, 7, true);
                break;
            default:
                throw new DateTimeException("Invalid ID for ZoneOffset, invalid format: " + offsetId);
        }
        hours = parseNumber(offsetId, 1, false);
        minutes = 0;
        seconds = 0;
        char first = offsetId.charAt(0);
        if (first != '+' && first != '-') {
            throw new DateTimeException("Invalid ID for ZoneOffset, plus/minus not found when expected: " + offsetId);
        } else if (first == '-') {
            return ofHoursMinutesSeconds(-hours, -minutes, -seconds);
        } else {
            return ofHoursMinutesSeconds(hours, minutes, seconds);
        }
    }

    private static int parseNumber(CharSequence offsetId, int pos, boolean precededByColon) {
        if (!precededByColon || offsetId.charAt(pos - 1) == ':') {
            char ch1 = offsetId.charAt(pos);
            char ch2 = offsetId.charAt(pos + 1);
            if (ch1 >= '0' && ch1 <= '9' && ch2 >= '0' && ch2 <= '9') {
                return ((ch1 - 48) * 10) + (ch2 - 48);
            }
            throw new DateTimeException("Invalid ID for ZoneOffset, non numeric characters found: " + offsetId);
        }
        throw new DateTimeException("Invalid ID for ZoneOffset, colon not found when expected: " + offsetId);
    }

    public static ZoneOffset ofHours(int hours) {
        return ofHoursMinutesSeconds(hours, 0, 0);
    }

    public static ZoneOffset ofHoursMinutes(int hours, int minutes) {
        return ofHoursMinutesSeconds(hours, minutes, 0);
    }

    public static ZoneOffset ofHoursMinutesSeconds(int hours, int minutes, int seconds) {
        validate(hours, minutes, seconds);
        return ofTotalSeconds(totalSeconds(hours, minutes, seconds));
    }

    public static ZoneOffset from(TemporalAccessor temporal) {
        Objects.requireNonNull((Object) temporal, "temporal");
        ZoneOffset offset = (ZoneOffset) temporal.query(TemporalQueries.offset());
        if (offset != null) {
            return offset;
        }
        throw new DateTimeException("Unable to obtain ZoneOffset from TemporalAccessor: " + temporal + " of type " + temporal.getClass().getName());
    }

    private static void validate(int hours, int minutes, int seconds) {
        if (hours < -18 || hours > 18) {
            throw new DateTimeException("Zone offset hours not in valid range: value " + hours + " is not in the range -18 to 18");
        }
        if (hours > 0) {
            if (minutes < 0 || seconds < 0) {
                throw new DateTimeException("Zone offset minutes and seconds must be positive because hours is positive");
            }
        } else if (hours < 0) {
            if (minutes > 0 || seconds > 0) {
                throw new DateTimeException("Zone offset minutes and seconds must be negative because hours is negative");
            }
        } else if ((minutes > 0 && seconds < 0) || (minutes < 0 && seconds > 0)) {
            throw new DateTimeException("Zone offset minutes and seconds must have the same sign");
        }
        if (Math.abs(minutes) > 59) {
            throw new DateTimeException("Zone offset minutes not in valid range: abs(value) " + Math.abs(minutes) + " is not in the range 0 to 59");
        } else if (Math.abs(seconds) > 59) {
            throw new DateTimeException("Zone offset seconds not in valid range: abs(value) " + Math.abs(seconds) + " is not in the range 0 to 59");
        } else if (Math.abs(hours) != 18) {
        } else {
            if (Math.abs(minutes) > 0 || Math.abs(seconds) > 0) {
                throw new DateTimeException("Zone offset not in valid range: -18:00 to +18:00");
            }
        }
    }

    private static int totalSeconds(int hours, int minutes, int seconds) {
        return ((hours * 3600) + (minutes * 60)) + seconds;
    }

    public static ZoneOffset ofTotalSeconds(int totalSeconds) {
        if (Math.abs(totalSeconds) > MAX_SECONDS) {
            throw new DateTimeException("Zone offset not in valid range: -18:00 to +18:00");
        } else if (totalSeconds % PlatformLogger.WARNING != 0) {
            return new ZoneOffset(totalSeconds);
        } else {
            Integer totalSecs = Integer.valueOf(totalSeconds);
            ZoneOffset result = (ZoneOffset) SECONDS_CACHE.get(totalSecs);
            if (result == null) {
                SECONDS_CACHE.putIfAbsent(totalSecs, new ZoneOffset(totalSeconds));
                result = (ZoneOffset) SECONDS_CACHE.get(totalSecs);
                ID_CACHE.putIfAbsent(result.getId(), result);
            }
            return result;
        }
    }

    private ZoneOffset(int totalSeconds) {
        this.totalSeconds = totalSeconds;
        this.id = buildId(totalSeconds);
    }

    private static String buildId(int totalSeconds) {
        if (totalSeconds == 0) {
            return "Z";
        }
        int absTotalSeconds = Math.abs(totalSeconds);
        StringBuilder buf = new StringBuilder();
        int absHours = absTotalSeconds / 3600;
        int absMinutes = (absTotalSeconds / 60) % 60;
        buf.append(totalSeconds < 0 ? LanguageTag.SEP : "+").append(absHours < 10 ? "0" : "").append(absHours).append(absMinutes < 10 ? ":0" : ":").append(absMinutes);
        int absSeconds = absTotalSeconds % 60;
        if (absSeconds != 0) {
            buf.append(absSeconds < 10 ? ":0" : ":").append(absSeconds);
        }
        return buf.toString();
    }

    public int getTotalSeconds() {
        return this.totalSeconds;
    }

    public String getId() {
        return this.id;
    }

    public ZoneRules getRules() {
        return ZoneRules.of(this);
    }

    public boolean isSupported(TemporalField field) {
        boolean z = false;
        if (field instanceof ChronoField) {
            if (field == ChronoField.OFFSET_SECONDS) {
                z = true;
            }
            return z;
        }
        if (field != null) {
            z = field.isSupportedBy(this);
        }
        return z;
    }

    public ValueRange range(TemporalField field) {
        return super.range(field);
    }

    public int get(TemporalField field) {
        if (field == ChronoField.OFFSET_SECONDS) {
            return this.totalSeconds;
        }
        if (!(field instanceof ChronoField)) {
            return range(field).checkValidIntValue(getLong(field), field);
        }
        throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
    }

    public long getLong(TemporalField field) {
        if (field == ChronoField.OFFSET_SECONDS) {
            return (long) this.totalSeconds;
        }
        if (!(field instanceof ChronoField)) {
            return field.getFrom(this);
        }
        throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
    }

    public <R> R query(TemporalQuery<R> query) {
        if (query == TemporalQueries.offset() || query == TemporalQueries.zone()) {
            return this;
        }
        return super.query(query);
    }

    public Temporal adjustInto(Temporal temporal) {
        return temporal.with(ChronoField.OFFSET_SECONDS, (long) this.totalSeconds);
    }

    public int compareTo(ZoneOffset other) {
        return other.totalSeconds - this.totalSeconds;
    }

    public boolean equals(Object obj) {
        boolean z = true;
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ZoneOffset)) {
            return false;
        }
        if (this.totalSeconds != ((ZoneOffset) obj).totalSeconds) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        return this.totalSeconds;
    }

    public String toString() {
        return this.id;
    }

    private Object writeReplace() {
        return new Ser((byte) 8, this);
    }

    private void readObject(ObjectInputStream s) throws InvalidObjectException {
        throw new InvalidObjectException("Deserialization via serialization delegate");
    }

    void write(DataOutput out) throws IOException {
        out.writeByte(8);
        writeExternal(out);
    }

    void writeExternal(DataOutput out) throws IOException {
        int offsetSecs = this.totalSeconds;
        int offsetByte = offsetSecs % PlatformLogger.WARNING == 0 ? offsetSecs / PlatformLogger.WARNING : 127;
        out.writeByte(offsetByte);
        if (offsetByte == 127) {
            out.writeInt(offsetSecs);
        }
    }

    static ZoneOffset readExternal(DataInput in) throws IOException {
        int offsetByte = in.readByte();
        return offsetByte == 127 ? ofTotalSeconds(in.readInt()) : ofTotalSeconds(offsetByte * PlatformLogger.WARNING);
    }
}
