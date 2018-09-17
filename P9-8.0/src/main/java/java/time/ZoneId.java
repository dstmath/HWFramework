package java.time;

import java.io.DataOutput;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalQueries;
import java.time.temporal.TemporalQuery;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.time.zone.ZoneRules;
import java.time.zone.ZoneRulesException;
import java.time.zone.ZoneRulesProvider;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TimeZone;
import sun.util.locale.LanguageTag;

public abstract class ZoneId implements Serializable {
    public static final Map<String, String> SHORT_IDS;
    private static final long serialVersionUID = 8352817235686L;

    public abstract String getId();

    public abstract ZoneRules getRules();

    abstract void write(DataOutput dataOutput) throws IOException;

    static {
        Map<String, String> map = new HashMap(64);
        map.put("ACT", "Australia/Darwin");
        map.put("AET", "Australia/Sydney");
        map.put("AGT", "America/Argentina/Buenos_Aires");
        map.put("ART", "Africa/Cairo");
        map.put("AST", "America/Anchorage");
        map.put("BET", "America/Sao_Paulo");
        map.put("BST", "Asia/Dhaka");
        map.put("CAT", "Africa/Harare");
        map.put("CNT", "America/St_Johns");
        map.put("CST", "America/Chicago");
        map.put("CTT", "Asia/Shanghai");
        map.put("EAT", "Africa/Addis_Ababa");
        map.put("ECT", "Europe/Paris");
        map.put("IET", "America/Indiana/Indianapolis");
        map.put("IST", "Asia/Kolkata");
        map.put("JST", "Asia/Tokyo");
        map.put("MIT", "Pacific/Apia");
        map.put("NET", "Asia/Yerevan");
        map.put("NST", "Pacific/Auckland");
        map.put("PLT", "Asia/Karachi");
        map.put("PNT", "America/Phoenix");
        map.put("PRT", "America/Puerto_Rico");
        map.put("PST", "America/Los_Angeles");
        map.put("SST", "Pacific/Guadalcanal");
        map.put("VST", "Asia/Ho_Chi_Minh");
        map.put("EST", "-05:00");
        map.put("MST", "-07:00");
        map.put("HST", "-10:00");
        SHORT_IDS = Collections.unmodifiableMap(map);
    }

    public static ZoneId systemDefault() {
        return TimeZone.getDefault().toZoneId();
    }

    public static Set<String> getAvailableZoneIds() {
        return ZoneRulesProvider.getAvailableZoneIds();
    }

    public static ZoneId of(String zoneId, Map<String, String> aliasMap) {
        Objects.requireNonNull((Object) zoneId, "zoneId");
        Objects.requireNonNull((Object) aliasMap, "aliasMap");
        String id = (String) aliasMap.get(zoneId);
        if (id == null) {
            id = zoneId;
        }
        return of(id);
    }

    public static ZoneId of(String zoneId) {
        return of(zoneId, true);
    }

    public static ZoneId ofOffset(String prefix, ZoneOffset offset) {
        Objects.requireNonNull((Object) prefix, "prefix");
        Objects.requireNonNull((Object) offset, "offset");
        if (prefix.length() == 0) {
            return offset;
        }
        if (prefix.equals("GMT") || (prefix.equals("UTC") ^ 1) == 0 || (prefix.equals("UT") ^ 1) == 0) {
            if (offset.getTotalSeconds() != 0) {
                prefix = prefix.concat(offset.getId());
            }
            return new ZoneRegion(prefix, offset.getRules());
        }
        throw new IllegalArgumentException("prefix should be GMT, UTC or UT, is: " + prefix);
    }

    static ZoneId of(String zoneId, boolean checkAvailable) {
        Objects.requireNonNull((Object) zoneId, "zoneId");
        if (zoneId.length() <= 1 || zoneId.startsWith("+") || zoneId.startsWith(LanguageTag.SEP)) {
            return ZoneOffset.of(zoneId);
        }
        if (zoneId.startsWith("UTC") || zoneId.startsWith("GMT")) {
            return ofWithPrefix(zoneId, 3, checkAvailable);
        }
        if (zoneId.startsWith("UT")) {
            return ofWithPrefix(zoneId, 2, checkAvailable);
        }
        return ZoneRegion.ofId(zoneId, checkAvailable);
    }

    private static ZoneId ofWithPrefix(String zoneId, int prefixLength, boolean checkAvailable) {
        String prefix = zoneId.substring(0, prefixLength);
        if (zoneId.length() == prefixLength) {
            return ofOffset(prefix, ZoneOffset.UTC);
        }
        if (zoneId.charAt(prefixLength) != '+' && zoneId.charAt(prefixLength) != '-') {
            return ZoneRegion.ofId(zoneId, checkAvailable);
        }
        try {
            ZoneOffset offset = ZoneOffset.of(zoneId.substring(prefixLength));
            if (offset == ZoneOffset.UTC) {
                return ofOffset(prefix, offset);
            }
            return ofOffset(prefix, offset);
        } catch (DateTimeException ex) {
            throw new DateTimeException("Invalid ID for offset-based ZoneId: " + zoneId, ex);
        }
    }

    public static ZoneId from(TemporalAccessor temporal) {
        ZoneId obj = (ZoneId) temporal.query(TemporalQueries.zone());
        if (obj != null) {
            return obj;
        }
        throw new DateTimeException("Unable to obtain ZoneId from TemporalAccessor: " + temporal + " of type " + temporal.getClass().getName());
    }

    ZoneId() {
        if (getClass() != ZoneOffset.class && getClass() != ZoneRegion.class) {
            throw new AssertionError((Object) "Invalid subclass");
        }
    }

    public String getDisplayName(TextStyle style, Locale locale) {
        return new DateTimeFormatterBuilder().appendZoneText(style).toFormatter(locale).format(toTemporal());
    }

    private TemporalAccessor toTemporal() {
        return new TemporalAccessor() {
            public boolean isSupported(TemporalField field) {
                return false;
            }

            public long getLong(TemporalField field) {
                throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
            }

            public <R> R query(TemporalQuery<R> query) {
                if (query == TemporalQueries.zoneId()) {
                    return ZoneId.this;
                }
                return super.query(query);
            }
        };
    }

    public ZoneId normalized() {
        try {
            ZoneRules rules = getRules();
            if (rules.isFixedOffset()) {
                return rules.getOffset(Instant.EPOCH);
            }
        } catch (ZoneRulesException e) {
        }
        return this;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ZoneId)) {
            return false;
        }
        return getId().equals(((ZoneId) obj).getId());
    }

    public int hashCode() {
        return getId().hashCode();
    }

    private void readObject(ObjectInputStream s) throws InvalidObjectException {
        throw new InvalidObjectException("Deserialization via serialization delegate");
    }

    public String toString() {
        return getId();
    }

    private Object writeReplace() {
        return new Ser((byte) 7, this);
    }
}
